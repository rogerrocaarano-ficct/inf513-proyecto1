# Design

## Context

Hoy cada subsistema guarda los bytes de sus archivos en su propia tabla (`received_email_attachment.data`, `initial_document.content`), sin versiones: reemplazar un documento inicial (FR-055) sobrescribe el anterior. La arquitectura de referencia de `AGENTS.md` no cubre el almacenamiento de archivos; el diseño de este change está en EA, paquete `Design/Storage`, diagrama "Storage", y se acordó en la exploración previa. Motivación y alcance: ver `proposal.md`.

Restricciones que dan forma al diseño:

- Sin SQL en `src/main`: toda consulta es un método derivado de Spring Data (no hay consultas recursivas para recorrer una cadena).
- `InboxPollerService` ejecuta cada comando dentro de una transacción; el almacenamiento debe guardarse o revertirse junto con ella.
- Los adjuntos llegan como `byte[]` de hasta 25 MB (NFR-005), ya cargados en memoria por el canal de correo.

## Goals / Non-Goals

**Goals:**

- Un único punto de entrada (`StoredFileService`) que los demás subsistemas usarán para guardar, versionar y leer archivos.
- Versiones inmutables con historial reconstruible en una sola consulta.
- Ninguna escritura en disco que sobreviva a un rollback, y ninguna que pise a otra.

**Non-Goals:**

- Integrar `mail` y `procedures`, y quitar los `BYTEA` (segunda parte del issue #9).
- Streaming de archivos grandes: con el límite de 25 MB, `byte[]` alcanza.
- Borrar archivos confirmados, deduplicar contenido o calcular sumas de control.
- Almacenamiento remoto (S3 u otro): el puerto `FileStorage` lo permitiría más adelante sin cambiar la aplicación.

## Decisions

### D1. `StoredFileService` como única entrada al almacenamiento

Los demás subsistemas usan solo `storage.application.StoredFileService` (`store`, `storeNewVersion`, `readContent`, `getHistory`). El servicio coordina el agregado `StoredFile`, el puerto de dominio `StoredFileRepository` y el puerto de salida `FileStorage`, que solo maneja bytes por clave. No abre transacciones: corre dentro de la de quien lo llama.

Alternativas descartadas:
- Que cada consumidor use el repositorio y `FileStorage` directamente: repetiría el flujo de versionado y el bloqueo en cada subsistema (regla 6 de la arquitectura de referencia).
- `FileStorage` como fachada completa implementada en infraestructura: el flujo quedaría fuera del modelo y solo se probaría con integración.

`StoredFileService` no es un `<Name>UseCase` porque no implementa un caso de uso ni recibe comandos por correo: es un servicio compartido, nombrado como `InboxPollerService`.

### D2. Cadena de versiones con `nextVersionId` y `firstVersionId`

Cada versión es un `StoredFile` distinto. `nextVersionId` apunta a la siguiente (vacío en la vigente) y `firstVersionId` identifica la cadena: en la primera versión es su propio id y `supersedeBy` lo copia a cada versión nueva. El historial es `getByFirstVersionId`, ordenado por `storedAt`, en una consulta derivada.

El dueño del archivo (por ejemplo, un documento inicial, en la segunda parte) guarda el id de la versión vigente y lo actualiza al versionar.

Alternativas descartadas:
- Puntero hacia atrás o solo `nextVersionId`: reconstruir el historial exigiría una consulta por versión o una consulta recursiva (SQL nativo).
- Guardar primera y última versión en cada dueño: repite la regla del versionado en cada subsistema.

### D3. Bloqueo pesimista antes de versionar

`storeNewVersion` lee la versión anterior con `getByIdForUpdate` (`@Lock(PESSIMISTIC_WRITE)` en el método derivado, como `findForUpdateByReferenceAndClosedAtIsNull` de `procedures`). Una segunda operación concurrente espera, lee la versión ya enlazada y `supersedeBy` la rechaza con `StoredFileNotCurrentException`. Además, `next_version_id` es `UNIQUE` como red de seguridad en la base.

### D4. Clave en disco `aaaa/mm/<id>`

`StoredFile.store()` asigna `path = aaaa/mm/<id>` a partir de `storedAt` (UTC) y del id, sin extensión. El nombre que envía el usuario nunca llega a la ruta, así que no hay riesgo de recorrer directorios con `../`. Una carpeta por mes evita directorios con cientos de miles de entradas. `LocalDiskFileStorage` resuelve la clave contra la raíz, normaliza la ruta y rechaza cualquiera que quede fuera de ella como defensa adicional.

### D5. Escritura sin sobrescribir y limpieza ante rollback

`LocalDiskFileStorage.write`:

1. Exige una transacción activa con sincronización (`TransactionSynchronizationManager.isSynchronizationActive()`); sin ella lanza `IllegalStateException`, porque no podría garantizar la limpieza.
2. Crea las carpetas de la clave, escribe en un archivo temporal de la misma carpeta y lo mueve a su clave sin reemplazo (`Files.move` sin `REPLACE_EXISTING`). Si la clave ya existe, falla.
3. Registra una `TransactionSynchronization` que, en `afterCompletion` con estado `STATUS_ROLLED_BACK`, borra el archivo.

Como los archivos nunca se sobrescriben, un rollback solo borra lo que su propia transacción escribió. Si la aplicación se detiene entre la escritura y el commit, puede quedar un archivo sin metadatos; es un caso raro y no afecta a los datos (ver Riesgos).

Alternativa descartada: escribir en disco después del commit. Si la escritura fallara, la base tendría metadatos de un archivo inexistente, que es peor que un archivo huérfano.

### D6. Errores

- Datos inválidos en `store()` (nombre original o tipo vacíos, tamaño 0): `DomainValidationException` con un mensaje en español por campo, como los demás agregados.
- `StoredFileNotFoundException` ("El archivo solicitado no existe.") y `StoredFileNotCurrentException` ("Solo se puede crear una versión nueva a partir de la versión vigente del archivo."): subclases de `DomainException` en `storage.domain`. Cuando un caso de uso las propague, el canal de correo enviará el mensaje al remitente.
- Errores de E/S del disco: `UncheckedIOException`, que el canal de correo responde como error interno.

### D7. Esquema y configuración

- `V5__stored_file.sql`: tabla `stored_file` (`id` PK, `path` único, `original_name`, `content_type`, `size` > 0, `stored_at`, `first_version_id` FK a sí misma, `next_version_id` FK a sí misma y único) e índice por `first_version_id`.
- Persistencia según la arquitectura de referencia: `JpaStoredFileRepository`, `StoredFileEntity`, `SpringDataStoredFileRepository` (`findForUpdateById`, `findByFirstVersionIdOrderByStoredAtAsc`), todos package-private en `storage.infrastructure.persistence`.
- `StorageProperties` (`@ConfigurationProperties("cpms.storage")`, como `MailProperties`) con `root`. `application.properties`: `cpms.storage.root=${CPMS_STORAGE_ROOT:}`; si queda vacía, la aplicación no arranca y lo dice en el log. `application-local.properties`: `./data/storage`, con `data/` en `.gitignore`. Las pruebas usan una carpeta temporal registrada en `TestcontainersConfiguration`, que también usa `bootTestRun`.
- `StorageConfiguration` (package-private) declara `StoredFileService` como `@Bean`.

## Risks / Trade-offs

- [Archivo sin metadatos si la aplicación cae entre la escritura y el commit] → No rompe nada: ningún registro lo referencia. Se puede limpiar a mano comparando la carpeta con `stored_file`; un proceso automático queda fuera de alcance.
- [El volumen de la carpeta no está respaldado junto con la base] → El README documenta que la carpeta forma parte de los datos del sistema y debe respaldarse con la base.
- [Contenido en memoria (`byte[]`)] → Aceptable con el límite de 25 MB y lotes pequeños de correos; si el límite creciera, el puerto puede pasar a flujos sin cambiar el dominio.
- [Dos `FileStorage` en EA hasta la segunda parte] → La nota del nuevo indica que reemplaza al de `Design/Mail/application`, que se elimina al integrar.
- [El change no tiene consumidores] → Las pruebas de integración ejercitan el servicio completo (disco, base y rollback) sin pasar por el correo.

## Migration Plan

1. Configurar `CPMS_STORAGE_ROOT` en el entorno de producción con una carpeta persistente y con permisos de escritura para la aplicación, antes de desplegar.
2. Desplegar: Flyway aplica `V5` y crea una tabla vacía. Ningún comando cambia su comportamiento.
3. Rollback: volver a la versión anterior de la aplicación; la tabla `stored_file` queda sin uso y no molesta.

## Diferencias con el modelo

Clases, atributos, operaciones y relaciones de `Design/Storage/domain` y `Design/Storage/application` coinciden con el código (comparación de la tarea 5.4). La infraestructura no se modela (`LocalDiskFileStorage`, `StorageProperties`, `StorageConfiguration` y la persistencia JPA). Las constantes con los nombres de campo de los errores (`StoredFile.ORIGINAL_NAME`, …) no se modelan, como en los demás agregados.

Diferencias de comportamiento respecto de las notas de EA, decididas al implementar:
- `StoredFileService.storeNewVersion`: llama a `previous.supersedeBy(next)` **antes** de `FileStorage.write`, no después. Así una versión no vigente se rechaza sin escribir nada en disco. La nota de EA lista `write` antes de `supersedeBy`.
- `StoredFileService.getHistory`: ordena la cadena siguiendo los enlaces `nextVersionId` desde la primera versión, no por `storedAt`. Dos versiones guardadas casi a la vez podrían tener el mismo `storedAt`; los enlaces dan siempre el orden correcto. `getByFirstVersionId` sigue devolviendo las versiones ordenadas por `storedAt`.
- `StoredFile.store()`: guarda `storedAt` con precisión de microsegundos, la de PostgreSQL, para que el valor leído de la base sea igual al asignado.

Detalles del modelo que se corrigen en EA, sin efecto en el código:
- `StoredFileService.store`: el parámetro `content` figura como `Byte[]`; el código usa `byte[]`, como el resto de las operaciones.
- `StoredFile.withSize`: el parámetro figura como `Long`; el código usa `long`, como el atributo `size`.

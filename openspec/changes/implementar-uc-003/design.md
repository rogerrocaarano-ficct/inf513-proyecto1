# Design

## Context

La realización de diseño de UC-003 (`docs/model/use-cases/UC-003-gestionar-tramite-de-titulacion/design/realization.md`) define las clases, operaciones, tablas y comandos. Es la referencia de este cambio. Este diseño agrega solo lo que la realización no resuelve y que salió de comparar el modelo con el código:

- `graduationprocesses` tiene solo la interfaz `GraduationParticipation` y la implementación provisional `NoGraduationProcessesParticipation` (D6 de `implementar-uc-001`). Las tablas de trámites solo existen en `database.md`: las migraciones llegan hasta `V3`.
- `Pop3MailboxPoller.collectAttachments` toma cualquier parte con nombre de archivo, incluidas las imágenes inline, y lee el nombre con `part.getFileName()` en una sesión que no configura `mail.mime.decodefilename`. Un nombre en RFC 2047 (`=?UTF-8?Q?C=C3=A9dula.pdf?=`) llega sin decodificar.
- `TransactionalNotifier` ya resuelve "actuar solo después de confirmar" con `TransactionSynchronization`. `CommandDispatcher` abre la transacción de cada comando con `TransactionTemplate` y la deshace ante cualquier excepción.
- La imagen Docker corre con el usuario `mailapp` (uid 1001), que no puede escribir en `/app`.
- Las pruebas de integración envían correos con GreenMail (`MailTestSupport`); los ejemplos manuales usan la API REST de Mailpit (`src/test/http/`).

## Goals / Non-Goals

**Goals:**

- Que el código siga la realización de diseño de UC-003, y que la realización se corrija donde la implementación lo exija.
- Que la detección de documentos por nombre funcione con los clientes de correo habituales.
- Que un rechazo no deje archivos, filas, registros de cambios ni avisos.

**Non-Goals:**

- Una forma de registrar la calificación final (UC-006): las pruebas y el ejemplo `.http` la simulan en la base de datos.
- Descargar o consultar los documentos guardados (UC-008).
- Limpiar archivos huérfanos si el proceso muere entre escribir el archivo y deshacer la transacción.

## Decisions

### D1. Nombres de archivo decodificados en Messaging

RFC 2231 ya se decodifica por defecto (`mail.mime.decodeparameters`). Para RFC 2047, `Pop3MailboxPoller` pasa el nombre por `MimeUtility.decodeText` antes de crear el `MailAttachment`. Si la decodificación falla, se usa el nombre tal como llegó.

No se usa `mail.mime.decodefilename`: Jakarta Mail la lee de las propiedades del sistema, no de las de la sesión, y activarla cambiaría el comportamiento de toda la JVM.

Alternativa: decodificar en GraduationProcesses. Se descarta: el problema es del transporte, y UC-004 y UC-006 también reciben adjuntos.

### D2. Solo partes adjuntas

`collectAttachments` entrega solo las partes con `Content-Disposition: attachment`. Las partes `inline` y las que no tienen esa cabecera se ignoran aunque tengan nombre de archivo. Así una firma con logo no hace fallar el comando, y GraduationProcesses puede rechazar todo adjunto que no reconoce.

Alternativa: ignorar en GraduationProcesses los adjuntos sin palabra de tipo. Se descarta por decisión del equipo: un archivo mal nombrado se reportaría como faltante en lugar de como no reconocido, y cada caso de uso repetiría el filtro.

### D3. Clasificación por palabra en `InitialDocumentType`

`fromFileName` normaliza el nombre: quita la extensión, lo pasa a minúsculas y quita las tildes (`Normalizer.Form.NFD` y la eliminación de las marcas diacríticas). Luego busca las palabras `carta`, `hojadevida`, `cedula` y `boleta` como subcadena, y `cv` como palabra separada por caracteres que no son letras ni dígitos (así `cv.jpg` y `cv_soto.pdf` coinciden, y `pcvx.pdf` no).

`classify(files)` recorre todos los adjuntos y lanza, en este orden:

1. `UNRECOGNIZED_DOCUMENT` si un nombre no coincide con ningún tipo o coincide con dos.
2. `DUPLICATE_DOCUMENT` si dos adjuntos coinciden con el mismo tipo.
3. `MISSING_DOCUMENT` con la lista de los tipos que faltan.

Para comparar las palabras de varias partes (`hoja de vida`, `hoja_de_vida`), las palabras distintas de `cv` se buscan en el nombre sin separadores.

`InitialDocument.check` decide el formato por la extensión (`.png`, `.jpg`, `.jpeg`, `.pdf`). El tipo de contenido declarado tiene que coincidir con ella, salvo que sea genérico (`application/octet-stream`), como lo mandan algunos clientes de correo. El tipo que se guarda sale de la extensión. Un archivo vacío también se rechaza.

### D4. Storage con compensación transaccional

Paquete `storage` con `FileStorage` (application) y `LocalFileStorage` (adapter/out), configurado con `app.files.root=${FILES_ROOT:./data/files}`:

- `store(category, fileName, content)` crea `<raíz>/<categoría>/` si no existe y escribe `<UUID>.<extensión en minúsculas>` con `Files.write` y la opción `CREATE_NEW`. Devuelve la ruta relativa con `/`. Si hay una transacción activa, registra una sincronización que borra el archivo en `afterCompletion` cuando el estado es `STATUS_ROLLED_BACK`.
- `delete(storageKey)` registra el borrado en `afterCommit`. Sin transacción, borra en el momento. Un fallo al borrar se registra en el log, como hace `TransactionalNotifier` con los envíos.
- Una clave con `..` o absoluta se rechaza con `IllegalArgumentException`, para no salir de la raíz.

Alternativa: escribir los archivos en `afterCommit`. Se descarta: si la escritura falla después de confirmar, el trámite queda con documentos sin archivo y el secretario(a) ya recibió la confirmación.

### D5. Puerto `StudentAccounts` y su implementación en Users

`StudentAccounts`, `StudentData` y `StudentAccount` van en `graduationprocesses/application`, como en la realización. `UserStudentAccounts` va en `users/adapter/in`, junto a `UserSenderAuthenticator`, y delega en `UserManagementService`:

- `obtainStudentAccount(requesterId, data)` sigue la regla de la realización: busca por dirección de correo (normalizada con `UserAccount.normalizeEmail`) y por registro universitario. Crea la cuenta con `UserAccount.create`, `save`, `changeLog.record` y `notifyCreated` si no hay ninguna; devuelve la existente si es la misma, activa y con `STUDENT`. En los demás casos rechaza con `INACTIVE_ACCOUNT`, `DUPLICATE_EMAIL` o `DUPLICATE_REGISTRATION_NUMBER`.
- `notifyCreated` se extrae de `createAccount` sin cambiar el texto del aviso, y `createAccount` lo usa.
- `findStudentAccount(registro)` usa el nuevo `findByStudentRegistrationNumber` y filtra las cuentas con `STUDENT`.

Los beans no forman un ciclo: `GraduationProcessService` → `StudentAccounts` (`UserStudentAccounts`) → `UserManagementService` → `GraduationParticipation`, cuya implementación usa solo el repositorio de trámites.

### D6. Entidades y migración `V4`

`V4__crear_tramites_de_titulacion.sql` crea `graduation_processes`, `graduation_process_participants` e `initial_documents` tal como están en `database.md`, incluido el índice único parcial `graduation_processes_student_in_progress_uk`.

- `GraduationProcess` es la raíz del agregado. `initialDocuments` es `@OneToMany(cascade = ALL, orphanRemoval = true)` con `@JoinColumn(name = "graduation_process_id")`. `replaceInitialDocument(type, file, storageKey, now)` actualiza la fila del documento de ese tipo, en lugar de quitarla y agregar otra: Hibernate inserta antes de borrar, y el índice `initial_documents_process_type_uk` rechazaría la fila nueva. Devuelve `DocumentReplacement(change, replacedStorageKey)` para registrar el cambio y borrar el archivo anterior.
- `GraduationStage` se guarda como texto. Este cambio define `OPENED` y `FINAL_GRADE_RECORDED`.
- La migración no tiene un `CHECK` sobre `stage`, porque cada caso de uso agrega etapas. Sí lo tiene sobre `status`, `kind`, `type`, `content_type` y `size_bytes`.
- `GraduationProcessService` guarda con `saveAndFlush` y traduce la violación de `graduation_processes_student_in_progress_uk` a `PROCESS_IN_PROGRESS`, como `UserManagementService` con sus índices únicos (D5 de `implementar-uc-001`).

### D7. `GraduationParticipation` real

`ProcessParticipation` (`graduationprocesses/application`) reemplaza a `NoGraduationProcessesParticipation`:

- Para `STUDENT`, busca los trámites `IN_PROGRESS` con ese `student_account_id`.
- Para `TUTOR` y `TRIBUNAL_MEMBER`, busca en `graduation_process_participants`, que estará vacía hasta UC-005.
- Marca `pending_reassignment` y devuelve un resumen de cada trámite.

El resumen actual, `GraduationProcessSummary(id, studentName)`, necesita el nombre del estudiante, que vive en Users. GraduationProcesses no puede pedírselo a Users mientras atiende una llamada de Users sin crear un ciclo. Por eso el record pasa a `GraduationProcessSummary(id, studentAccountId)`, y Users, que ya tiene las cuentas, resuelve los nombres:

- `UserManagementService` convierte cada resumen en un record propio `AffectedProcess(id, studentName)`, con `findById` sobre `UserAccountRepository`.
- `AccountChangeResult.affectedProcesses` y `UserCommandSupport.withAffectedProcesses` pasan a usar `AffectedProcess`. El texto de la respuesta de UC-001 no cambia.
- `AffectedProcessesTests` se ajusta al record nuevo.

Alternativa: guardar el nombre del estudiante en `graduation_processes`. Se descarta: queda desactualizado cuando la cuenta cambia de nombre (UC-001 A3).

### D8. Comandos y respuestas

Los tres manejadores van en `graduationprocesses/adapter/in` con `allowedRoles() = {PROGRAM_SECRETARY}`, y comparten `GraduationCommandSupport`:

- `AbrirTramite` exige exactamente 5 argumentos y responde `Trámite de titulación abierto: <nombre> (<registro>).`, y si la cuenta es nueva, `Se creó la cuenta de usuario del estudiante.`
- `ReemplazarDocumento` exige 1 argumento y un solo adjunto, y responde `Documento inicial reemplazado: <tipo>.`
- `CerrarTramite` exige 1 argumento y responde `Trámite de titulación cerrado: <nombre> (<registro>).`

Avisos al estudiante (texto plano, después de confirmar): `Trámite de titulación abierto`, con la lista de documentos recibidos, y `Trámite de titulación cerrado`.

Para la respuesta de `AbrirTramite`, `openProcess` devuelve un record `OpenedProcess(process, student, accountCreated)`, y `StudentAccount` incorpora `created: boolean`. Así la realización cambia en estos dos tipos (ver D9).

### D9. Modelo

Este cambio actualiza el modelo así:

- **Realización de diseño de UC-003:**
  - `openProcess` devuelve `OpenedProcess`, y `StudentAccount` suma `created` (D8).
  - `replaceInitialDocument` de `GraduationProcess` recibe el tipo y el archivo y devuelve `DocumentReplacement`, y `GraduationRejectionReason` suma `INVALID_DATA` y `ATTACHMENT_COUNT` (D6, D8).
  - Se nombran `LocalFileStorage` y `ProcessParticipation`.
- **Realización de diseño de UC-001:** `AffectedProcess` y el nuevo `GraduationProcessSummary` (D7).
- **`graduation-processes.md`:** `GraduationProcessSummary(id, studentAccountId)`, el campo `created` de `StudentAccount` y la implementación real de `GraduationParticipation`.
- **`messaging.md`:** el filtrado de partes adjuntas y la decodificación de nombres (D1, D2).
- **`use-cases.md`:** sin cambios de estado (el diseño sigue en Draft).

## Risks / Trade-offs

- [Un cliente que envía archivos sin `Content-Disposition: attachment` no puede abrir trámites] → Los clientes habituales (Gmail, Outlook, Thunderbird) marcan los adjuntos; queda escrito en la spec `mail-commands`.
- [Si el proceso muere después de escribir un archivo y antes de deshacer, el archivo queda huérfano] → Riesgo aceptado: no rompe la consistencia de la base, y los comandos se procesan de a uno.
- [El cierre no se puede ejercitar sin UC-006] → Las pruebas fijan `FINAL_GRADE_RECORDED` con un `UPDATE`; el ejemplo `.http` documenta el comando `psql` para hacerlo a mano.
- [Un correo con cuatro adjuntos de 1 MB pesa unos 5,5 MB en base64] → Dentro de lo que acepta Mailpit; el límite del servidor del docente queda para UC-004, que admite 25 MB.
- [Cambiar `GraduationProcessSummary` toca código de UC-001] → El cambio es interno a `mail-app`, el texto de las respuestas no cambia y `AffectedProcessesTests` lo cubre.

## Migration Plan

1. `V4` se aplica sola al arrancar (Flyway, AD-011). No modifica tablas existentes.
2. En Docker, el `Dockerfile` crea `/app/data/files` con dueño `mailapp`, y `docker-compose.yml` monta el volumen `files` ahí con `FILES_ROOT=/app/data/files`. Un volumen nuevo hereda ese dueño.
3. Rollback: no hay datos previos de trámites; basta con volver a la imagen anterior y borrar las tablas nuevas y el volumen.

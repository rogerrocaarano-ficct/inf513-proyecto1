# Design

## Context

Motivación y alcance: ver `proposal.md`. Estado actual que da forma al diseño:

- `Pop3EmailInbox.collect()` arma cada adjunto como `File.builder().withName(…).withContentType(…).withData(…)` y devuelve `ReceivedEmail` en `RECEIVED` (`ReceivedEmail.builder()` fija ese estado).
- `InboxPollerService.storeNew(email)`: si `email.findEmptyAttachment()` encuentra un adjunto vacío, vacía la lista (`withAttachments(List.of())`) y descarta el correo; si no, dentro de una transacción guarda cada adjunto con `storedFiles.store(…)`, le asigna `withStoredFileId` y guarda el correo. La validación cuenta `email.getAttachments().size()` y los handlers usan `email.getAttachmentIds()`.
- `JpaReceivedEmailRepository.store` rechaza un adjunto sin `storedFileId` y crea un `ReceivedEmailAttachmentEntity(id, storedFileId)` por adjunto, mapeado con `@OneToMany` + `@OrderColumn(name = "position")`. `toDomain` rehidrata `File` con `id` y `storedFileId`.
- `received_email_attachment` (V1 + V7): `id` PK, `received_email_id` FK con `ON DELETE CASCADE`, `position NOT NULL`, `stored_file_id NOT NULL UNIQUE` FK a `stored_file`.
- `EmailSender.send(to, subject, body, List<File>)`: solo `MailTutorAssignmentNotifier` envía adjuntos (el perfil a Taller, FR-057), que lee de `StoredFileService` y arma un `File` con nombre y bytes, sin tipo de contenido. `SmtpEmailSender` es un `@Component`.
- El modelo EA tiene diseño para `Design/Mail`: `ReceivedEmail` (78) con `attachments: List<File>`, `File` (79) en composición, `ReceivedEmailState` (87) y `EmailSender` (96) con `send(…, attachments: List<File>)`.

## Goals / Non-Goals

**Goals:**

- Un correo persistido solo tiene referencias a archivos guardados, y lo garantiza el agregado según su estado, no el poller ni el repositorio por separado.
- Ningún tipo con campos que quedan nulos según la etapa.
- El adjunto no tiene identidad propia, ni en el dominio ni en la base.

**Non-Goals:**

- Cambiar las transiciones existentes entre `RECEIVED`, `VALIDATED`, `PROCESSED` y `DISCARDED`.
- Cambiar `storage` o su API.
- Renombrar `ReceivedEmail` o la tabla `received_email_attachment`.

## Decisions

### D1. `Attachment`: objeto valor con dos variantes

En `mail.domain`:

```
sealed interface Attachment
    record Content(String name, String contentType, byte[] data)   // recién leído del buzón
        boolean isEmpty()                                          // data.length == 0
    record Stored(UUID storedFileId)                               // ya guardado
```

- Sin identidad: un adjunto se identifica por su posición en el correo.
- `Content` define `equals`, `hashCode` y `toString` propios: un record con `byte[]` compara el arreglo por referencia y lo imprime completo. `data` nulo se normaliza a un arreglo vacío, como hoy `File.withData`.
- `Stored` exige `storedFileId` no nulo.

Alternativas descartadas:
- Un solo record con todos los campos: vuelve a tener campos nulos según la etapa, que es el problema de `File`.
- Un VO solo de contenido y una `List<UUID>` aparte en el correo: el agregado tendría dos listas según el estado, una siempre vacía.

### D2. Estado `FETCHED` y transición en el agregado

`ReceivedEmailState` gana `FETCHED`, antes de `RECEIVED`: "Leído del servidor, con el contenido de sus adjuntos, aún sin guardar. Nunca se persiste." `isFinal()` no cambia.

```
            storeAttachments(storer)
 FETCHED ---------------------------> RECEIVED --> VALIDATED --> PROCESSED
    |                                     |            |
    | markAsDiscarded (adjunto vacío)     +------------+--> DISCARDED
    +------------------------------------------------------>
```

Operaciones de `ReceivedEmail`:

- `builder()` crea el correo en `FETCHED` (hoy en `RECEIVED`).
- `withAttachments(List<Attachment>)`: sin cambio de firma salvo el tipo.
- `findEmptyAttachment(): Optional<Attachment.Content>`: el primer `Content` vacío, en orden.
- `storeAttachments(Function<Attachment.Content, UUID> storer)`: exige `FETCHED` (si no, `IllegalStateException`); reemplaza cada `Content` por `Stored(storer.apply(content))`, en orden, y pasa a `RECEIVED`. El dominio no depende de `storage`: el poller pasa `content -> storedFiles.store(content.name(), content.contentType(), content.data()).getId()`.
- `markAsDiscarded()` desde `FETCHED`: quita los adjuntos (nunca se guardaron) y pasa a `DISCARDED`. Desde los otros estados no finales, igual que hoy.
- `getAttachmentIds(): List<UUID>`: exige que el correo no esté en `FETCHED`; devuelve los `storedFileId` en orden.
- `getAttachments()` se mantiene, devolviendo `List<Attachment>`, para el conteo de `requiredFiles` y para el repositorio.

Invariante: fuera de `FETCHED`, todos los adjuntos son `Stored`. La garantizan `storeAttachments` y `markAsDiscarded`; `rehydrate()` la recibe ya cumplida del repositorio.

`InboxPollerService.storeNew` queda:

```
 si email.findEmptyAttachment() presente:
     discard(email, "…el adjunto \"<nombre>\" está vacío.")    // FETCHED -> DISCARDED, sin adjuntos
 si no, en una transacción:
     email.storeAttachments(content -> storedFiles.store(…).getId())   // -> RECEIVED
     receivedEmails.store(email)
```

Si la transacción falla, el rollback borra los archivos escritos y el mensaje se relee en el ciclo siguiente, como hoy. El objeto en memoria queda descartado; el ciclo siguiente lo vuelve a leer del buzón.

Alternativa descartada: un registro de transporte (`MailboxMessage`) devuelto por `EmailInbox` y un agregado que nace ya con referencias. Mantiene al agregado sin bytes, pero duplica `mailboxId`, `from`, `subject` y `body` en dos tipos que describen el mismo correo; se prefirió un solo concepto con el estado explícito.

### D3. El repositorio no persiste correos en `FETCHED`

`JpaReceivedEmailRepository.store` lanza `IllegalStateException` si recibe un correo en `FETCHED`: reemplaza la comprobación actual de cada adjunto sin `storedFileId`. `ReceivedEmailEntity.state` nunca vale `FETCHED`, por lo que la columna `state` no cambia.

### D4. Adjuntos como colección de valores

`ReceivedEmailEntity` reemplaza el `@OneToMany` por:

```java
@ElementCollection
@CollectionTable(name = "received_email_attachment",
        joinColumns = @JoinColumn(name = "received_email_id"))
@OrderColumn(name = "position")
@Column(name = "stored_file_id", nullable = false)
private List<UUID> attachmentIds = new ArrayList<>();
```

- `ReceivedEmailAttachmentEntity` se elimina.
- `store` copia `getAttachmentIds()` solo al insertar el correo, como hoy (los adjuntos no cambian después).
- `toDomain` rehidrata cada id como `Attachment.Stored`.
- La colección se carga con el correo; hoy `getNotProcessed` ya recorre los adjuntos de cada correo dentro de su transacción de solo lectura.

Alternativa descartada: una columna `UUID[]` en `received_email`, que elimina la tabla pero pierde la FK a `stored_file` (PostgreSQL no admite FK sobre elementos de un arreglo).

### D5. Migración `V9__received_email_attachment_values.sql`

```
ALTER TABLE received_email_attachment DROP COLUMN id;          -- arrastra la PK actual
ALTER TABLE received_email_attachment ADD PRIMARY KEY (received_email_id, position);
```

Las filas existentes se conservan: ya tienen correo, posición y archivo. La FK a `stored_file`, el `UNIQUE` de `stored_file_id` y el `ON DELETE CASCADE` no se tocan. V1 y V7 no se editan.

### D6. `EmailSender` envía archivos guardados

- `EmailSender.send(String to, String subject, String body, List<UUID> attachmentIds)`: identificadores de archivos guardados, en orden; lista vacía si no hay.
- `SmtpEmailSender` recibe `StoredFileService` en su constructor y, por cada id, usa `get(id)` (nombre original y tipo de contenido) y `readContent(id)`, y adjunta con `helper.addAttachment(name, resource, contentType)`. Un archivo inexistente o ilegible lanza la excepción de `storage` antes de enviar.
- `MailTutorAssignmentNotifier.onProfileRegistered` pasa `List.of(event.profile().storedFileId())` y deja de depender de `StoredFileService`. Un error de lectura llega como excepción de `send`, que su método privado `send` ya registra en el log sin relanzar: el resultado es el mismo que hoy (no se envía nada).
- Los demás emisores (`InboxPollerService`, `MailDegreeProcedureNotifier`, `MailUserManagementNotifier`) pasan `List.of()`, sin cambios.

`mail.infrastructure` depende de `storage.application`, que la arquitectura de referencia permite; `mail.application` ya depende de él.

Alternativa descartada: un record de adjunto saliente con nombre y bytes en `mail.application`. Todo archivo que el sistema adjunta ya está en `storage`, y el record obligaría a cada notifier a leerlo por su cuenta.

### D7. Glosario

`GLOSSARY.md` suma "Adjunto": archivo que llega con un correo recibido o que se envía con una respuesta o notificación; se guarda en el almacenamiento de archivos y el correo conserva solo su referencia, en su orden → `Attachment`.

## Risks / Trade-offs

- [El agregado carga bytes mientras está en `FETCHED`] → Es el mismo volumen que hoy carga `File`, acotado por el tamaño del lote de lectura; la transición los descarta en cuanto se guardan.
- [La invariante "fuera de `FETCHED` todo es `Stored`" se comprueba en tiempo de ejecución] → La centralizan dos operaciones del agregado y el repositorio rechaza `FETCHED`; las pruebas del dominio cubren las transiciones.
- [Hibernate borra y reinserta una `@ElementCollection` con `@OrderColumn` cuando cambia] → Los adjuntos no cambian después de insertar el correo; `copyFields` no los toca.
- [Correos pendientes al aplicar `V9`] → No pierden nada: la migración solo quita la columna `id`.
- [`SmtpEmailSender` lee el archivo fuera de la transacción del caso de uso] → Hoy el notifier ya lo lee en `AFTER_COMMIT`; el archivo está confirmado.

## Migration Plan

1. Desplegar: Flyway aplica `V9`, que conserva todas las filas de `received_email_attachment`.
2. Rollback: volver a la versión anterior exige restaurar la columna `id` con valores nuevos (`gen_random_uuid()`) y su PK; no se pierden datos.

## Diferencias con el modelo

Cambios que el change hace sobre el diseño de `Design/Mail`, para llevarlos a EA:

**`Design/Mail/domain`:**

- `File` (79) se elimina, con su composición desde `ReceivedEmail` (conector 145).
- Elemento nuevo `Attachment` (interfaz sellada, objeto valor) con dos variantes:
  - `Attachment.Content`: atributos `name: String`, `contentType: String`, `data: byte[]`; operación `isEmpty(): boolean`.
  - `Attachment.Stored`: atributo `storedFileId: UUID`.
  - Composición `ReceivedEmail` → `Attachment`, 0..*, ordenada.
- `ReceivedEmail` (78):
  - `attachments` pasa a `List<Attachment>`; `withAttachments` y `getAttachments` cambian de tipo.
  - Operaciones nuevas `storeAttachments(storer: Function<Attachment.Content, UUID>)` y, ya presentes en el código pero no en el modelo, `findEmptyAttachment(): Optional<Attachment.Content>` y `getAttachmentIds(): List<UUID>`.
  - `builder()` crea el correo en `FETCHED`; `markAsDiscarded()` admite `FETCHED` y quita los adjuntos.
- `ReceivedEmailState` (87): valor nuevo `FETCHED`; el flujo de su descripción pasa a `FETCHED -> RECEIVED -> VALIDATED -> PROCESSED`, o `-> DISCARDED` desde `FETCHED`, `RECEIVED` o `VALIDATED`.

**`Design/Mail/application`:**

- `EmailSender.send` (96): el parámetro `attachments: List<File>` pasa a `attachmentIds: List<UUID>`, identificadores de archivos guardados.
- `EmailInbox.poll` (84): devuelve correos en `FETCHED`; la firma no cambia.

Infraestructura (no modelada): `SmtpEmailSender` depende de `StoredFileService`; `ReceivedEmailAttachmentEntity` se elimina; `MailTutorAssignmentNotifier` deja de depender de `StoredFileService`.

Comparación de la tarea 4.4, sin diferencias nuevas con lo anterior: la implementación coincide con `Design/Mail/domain` (`ReceivedEmail`, `ReceivedEmailState`) y `Design/Mail/application` (`EmailSender`, `EmailInbox`) salvo lo listado arriba. Detalles de infraestructura sin efecto en el modelo: `SmtpEmailSender` lee todos los adjuntos antes de armar el mensaje (record privado `OutgoingFile`), y `MailTutorAssignmentNotifier` registra el error de lectura del perfil con el mismo aviso que un error de envío.

# Proposal

## Why

Desde el issue #9, `mail.domain.File` ya no es dueño de nada del archivo: el nombre, el tipo, el tamaño y las versiones viven en `StoredFile`. Aun así sigue modelado como entidad, con un `id` que solo sirve para darle fila propia en `received_email_attachment`, y cumple tres papeles distintos con campos que quedan nulos según la etapa: adjunto recién leído del buzón (nombre, tipo, bytes), adjunto guardado (`id`, `storedFileId`) y adjunto saliente de `EmailSender` (nombre, bytes). La regla "un correo guardado solo tiene referencias a archivos" no la expresa el agregado: queda repartida entre `InboxPollerService` y `JpaReceivedEmailRepository`.

Pertenece al trabajo compartido **canal de correo** (`mail`): no corresponde a un caso de uso.

## What Changes

- **Adjunto como objeto valor**: `File` se elimina. `mail.domain.Attachment` es un objeto valor sin identidad con dos variantes: `Attachment.Content` (nombre, tipo de contenido y bytes, recién leído del buzón) y `Attachment.Stored` (identificador del archivo guardado).
- **Estado nuevo del correo**: `ReceivedEmailState` gana `FETCHED`, el correo recién leído del buzón con el contenido de sus adjuntos, que nunca se persiste. Una operación de `ReceivedEmail` guarda los adjuntos a través de una función que recibe del poller y deja el correo en `RECEIVED`, ya solo con referencias. Un correo con un adjunto vacío pasa de `FETCHED` a `DISCARDED` sin guardar nada.
- **Envío por referencia**: `EmailSender.send` recibe los identificadores de los archivos guardados en lugar de `File`. `SmtpEmailSender` lee el nombre, el tipo y el contenido con `StoredFileService`, y el adjunto sale con su tipo de contenido. `MailTutorAssignmentNotifier` deja de leer el archivo.
- **Persistencia**: los adjuntos de un correo se mapean como colección de valores (`@ElementCollection` de `UUID` con `@OrderColumn`); `ReceivedEmailAttachmentEntity` se elimina. Migración `V9` que quita la columna `id` de `received_email_attachment` y usa `(received_email_id, position)` como clave primaria. Se conservan la tabla, sus filas, la FK a `stored_file`, el `UNIQUE` y el borrado en cascada.
- Se mantiene el nombre `ReceivedEmail`: `Email` se confundiría con las direcciones de correo que el código ya llama `email`.

El comportamiento observable no cambia: el canal sigue guardando los adjuntos en la misma transacción que el correo, entrega los identificadores en orden y descarta los correos con un adjunto vacío.

Fuera de alcance: cambiar los demás estados del correo, la validación de cantidades o el subsistema `storage`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna. Los requisitos de `mail-commands` ("Adjuntos guardados en el almacenamiento de archivos", "Adjunto vacío", "Ejecución del comando") ya describen el comportamiento en términos de referencias en orden y no cambian. El change declara `skip_specs: true`.

## Impact

- **Elementos de EA en los que se basa**:
  - `Design/Mail/domain`: `ReceivedEmail` (cambian `attachments` y sus operaciones), `File` (se elimina, con su composición desde `ReceivedEmail`), `ReceivedEmailState` (valor nuevo `FETCHED`).
  - `Design/Mail/application`: `EmailSender.send` (cambia el tipo de `attachments`), `EmailInbox.poll` (devuelve correos en `FETCHED`), `InboxPollerService`, `ReceivedEmailRepository`.
  - `Design/Storage/application`: `StoredFileService` (`get`, `readContent`), sin cambios.
  - No hay diagramas en `Design/Mail/domain`.
- **Requisitos**: los de `mail-commands` citados arriba, sin cambios; FR-057 (aviso a Taller con el perfil), sin cambios. Issue #9.
- **Código**: `mail` (dominio, poller, POP3, SMTP, persistencia y pruebas), `procedures.infrastructure.MailTutorAssignmentNotifier`; migración `V9__received_email_attachment_values.sql`.
- **Dependencias**: ninguna nueva. `mail.infrastructure` pasa a usar `storage.application.StoredFileService`, como ya lo hace `mail.application`.
- **Documentos**: `GLOSSARY.md` (término "Adjunto" → `Attachment`).

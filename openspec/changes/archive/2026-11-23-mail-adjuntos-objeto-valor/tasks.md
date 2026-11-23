# Tasks

## 1. Dominio de `mail`

Los cambios de firma de este grupo y del siguiente se arrastran entre sí: el proyecto vuelve a compilar al terminar 2.3, y las pruebas de 1.1 y 1.2 se ejecutan a partir de ahí.

- [x] 1.1 Crear `mail.domain.Attachment` (interfaz sellada con `Content` y `Stored`) según D1, con `equals`, `hashCode` y `toString` propios en `Content` y `data` nulo normalizado a vacío; eliminar `File`. Verificar con `AttachmentTest`: igualdad por contenido, `isEmpty`, `Stored` sin id rechazado
- [x] 1.2 Agregar `FETCHED` a `ReceivedEmailState` y cambiar `ReceivedEmail` según D2 (`builder()` en `FETCHED`, `withAttachments(List<Attachment>)`, `findEmptyAttachment`, `storeAttachments`, `markAsDiscarded` desde `FETCHED`, `getAttachmentIds` fuera de `FETCHED`); verificar con `ReceivedEmailTest`: ids guardados en orden y paso a `RECEIVED`, `storeAttachments` rechazado fuera de `FETCHED`, descarte desde `FETCHED` sin adjuntos, `getAttachmentIds` rechazado en `FETCHED`, y las transiciones existentes sin cambios

## 2. Canal de correo

- [x] 2.1 En `Pop3EmailInbox.collect()`, armar `Attachment.Content` en lugar de `File`; verificar con `MailAdaptersTest` (GreenMail) que un correo leído queda en `FETCHED` con nombre, tipo base y contenido de cada adjunto, en orden
- [x] 2.2 En `InboxPollerService.storeNew`, descartar el correo con un adjunto vacío sin vaciar la lista a mano y guardar los adjuntos con `storeAttachments` dentro de la transacción (D2); contar `requiredFiles` con `getAttachments()`. Verificar con `InboxPollerServiceTest`: adjuntos guardados en orden y correo en `RECEIVED`, adjunto vacío descartado sin guardar archivos y con la respuesta, falla al guardar que no persiste el correo ni corta el ciclo
- [x] 2.3 Cambiar `EmailSender.send` a `List<UUID> attachmentIds` y `SmtpEmailSender` para leer nombre, tipo y contenido con `StoredFileService` según D6; actualizar los emisores (`InboxPollerService`, `MailDegreeProcedureNotifier`, `MailUserManagementNotifier`, `MailTutorAssignmentNotifier`). Verificar con `MailAdaptersTest` (GreenMail) que el adjunto enviado lleva el nombre original, el tipo de contenido y los bytes guardados, y que un id inexistente lanza la excepción sin enviar
- [x] 2.4 Quitar de `MailTutorAssignmentNotifier` la dependencia de `StoredFileService` y pasar `List.of(event.profile().storedFileId())`; actualizar su `@Bean` o constructor. Verificar con `MailTutorAssignmentNotifierTest` que envía el id del perfil a Taller y que un error de `send` se registra y no se propaga

## 3. Persistencia

- [x] 3.1 Crear `V9__received_email_attachment_values.sql` según D5, reemplazar el `@OneToMany` de `ReceivedEmailEntity` por la `@ElementCollection` de D4, eliminar `ReceivedEmailAttachmentEntity` y cambiar `JpaReceivedEmailRepository` (rechazo de `FETCHED` según D3, rehidratación como `Attachment.Stored`). Verificar con `JpaReceivedEmailRepositoryTest` (Testcontainers): ida y vuelta de los ids en orden, correo en `FETCHED` rechazado, `received_email_attachment` sin columna `id` y con PK `(received_email_id, position)`, y borrado en cascada de los adjuntos al borrar el correo

## 4. Documentación e integración

- [x] 4.1 Agregar "Adjunto" → `Attachment` a `GLOSSARY.md` según D7, en orden alfabético y con las columnas actuales; verificar que el término adoptado coincide con la clase
- [x] 4.2 Verificar con `MailCommandsIntegrationTest` (GreenMail) que los escenarios de `openspec/specs/mail-commands/spec.md` sobre adjuntos siguen pasando (dos adjuntos guardados en orden, comando pendiente tras un reinicio, correo descartado que conserva su archivo, adjunto vacío) y que la consulta SQL de la prueba sigue leyendo `received_email_attachment`
- [x] 4.3 Verificar con `DegreeProcedureByEmailIntegrationTest` (GreenMail), en el caso del perfil reenviado a Taller, que Taller recibe el PDF con su nombre y tipo `application/pdf`
- [x] 4.4 Comparar la implementación con `Design/Mail/domain` (`ReceivedEmail`, `ReceivedEmailState`, `File`) y `Design/Mail/application` (`EmailSender`, `EmailInbox`) en EA; registrar cualquier diferencia nueva en "Diferencias con el modelo" de `design.md`
- [x] 4.5 Comprobar con `git grep` que no queda `mail.domain.File` ni `ReceivedEmailAttachmentEntity` en `src/`, y ejecutar `./gradlew build` con todas las pruebas en verde

## Workflow follow-up

- Aplicar en EA las diferencias de "Diferencias con el modelo" (`ea-checklist`).
- Archivar el change cuando las tareas estén hechas y las pruebas pasen.

# Tasks

## 1. Almacenamiento de archivos

- [ ] 1.1 Crear `storage.domain.StoredFileAlreadyVersionedException` con el mensaje de la spec y agregar a `StoredFile.supersedeBy` la precondición de D4 (archivo suelto, distinto del anterior); verificar con `StoredFileExceptionsTest` y `StoredFileTest` que rechaza un archivo ya enlazado, uno con versión siguiente y el mismo archivo, y que `storeNewVersion` sigue funcionando
- [ ] 1.2 Agregar `get(id)` y `appendVersion(previousId, nextId)` a `StoredFileService` según D4 (bloqueo de los dos en orden de id, sin escritura en disco); verificar con `StoredFileServiceTest` (dobles en memoria): datos sin leer contenido, enlace y `update` de los dos, ninguna llamada a `FileStorage.write`, `StoredFileNotFoundException` para cada id inexistente y los rechazos de 1.1
- [ ] 1.3 Cubrir en `StoredFileServiceIntegrationTest` (Testcontainers, carpeta temporal) los escenarios nuevos de `specs/file-storage/spec.md`: archivo suelto agregado, historial desde cualquiera de los dos, versión anterior no vigente, archivo que ya pertenece a un historial, dos agregados simultáneos y consulta de datos; verificar que pasa

## 2. Canal de correo

- [ ] 2.1 En `mail.domain`, agregar a `File` `contentType` y `storedFileId`, y a `ReceivedEmail` `findEmptyAttachment()` y `getAttachmentIds()` según D2; verificar con `ReceivedEmailTest`
- [ ] 2.2 En `Pop3EmailInbox.collect()`, tomar el tipo base del adjunto o `application/octet-stream`; verificar con `MailAdaptersTest` (GreenMail) un adjunto `application/pdf; name=...` y uno sin tipo
- [ ] 2.3 En `InboxPollerService.storeNewMessages()`, guardar cada correo nuevo y sus adjuntos con `StoredFileService` en una transacción por correo, descartar con respuesta el correo con un adjunto vacío y registrar en el log y seguir si falla un correo (D1); actualizar el `@Bean` en `MailConfiguration`; verificar con `InboxPollerServiceTest`: adjuntos guardados en orden con su id en el correo, adjunto vacío descartado sin guardar archivos y con la respuesta, falla de un correo que no corta el ciclo ni lo guarda
- [ ] 2.4 Crear `V7__received_email_attachment_stored_file.sql` según D7 y cambiar `ReceivedEmailAttachmentEntity` y `JpaReceivedEmailRepository` para guardar y rehidratar solo `id`, `position` y `stored_file_id`; verificar con el test de `mail/infrastructure/persistence` (Testcontainers) la ida y vuelta de los ids en orden y que la entidad no tiene `byte[]`
- [ ] 2.5 Agregar a `USER-GUIDE.md`, en la tabla de respuestas del canal, la del adjunto vacío; verificar que el texto coincide con el de 2.3

Al terminar el grupo 2, los handlers de `procedures` todavía pasan `File` sin contenido: las pruebas de `procedures` vuelven a pasar al terminar el grupo 3.

## 3. `procedures`

Los cambios de firma de este grupo se arrastran entre sí: el proyecto vuelve a compilar al terminar 3.5, y las pruebas de 3.1 a 3.4 se ejecutan a partir de ahí.

- [ ] 3.1 Dominio según D5: `InitialDocument.requirePdf(StoredFile, byte[])` y `classify(StoredFile)`; `DegreeProcedure.open(User, User, List<StoredFile>)`, `replaceInitialDocument(User, StoredFile)`, `documentOf(InitialDocument)` y `Document(storedFileId, updatedBy, updatedAt)`; `ProjectProfile(storedFileId, approved)`, `pending(StoredFile)` y `DegreeProject.registerInitialProfile(StoredFile, …)`; sin imports de `mail`. Verificar con `DegreeProcedureTest` y `DegreeProjectTest` los mismos mensajes de validación que hoy (tamaño, PDF, tipo no identificado, duplicado, faltante)
- [ ] 3.2 Contratos y handlers según D3 (`attachmentIds`/`attachmentId` con `email.getAttachmentIds()`); verificar con `DegreeProcedureCommandHandlersTest` y `TutorAssignmentCommandHandlersTest` que cada handler pasa los ids en orden
- [ ] 3.3 `DegreeProcedureUseCase` y `TutorAssignmentUseCase` con `StoredFileService` según los flujos de D5; verificar con `DegreeProcedureUseCaseTest` y `TutorAssignmentUseCaseTest` (un `StoredFileService` real con dobles en memoria de sus puertos): apertura que adopta los cuatro ids sin escribir archivos nuevos, reemplazo que llama a `appendVersion` desde el documento vigente, perfil que adopta el id, y que un PDF inválido o un rechazo previo no cambian el trámite ni enlazan versiones
- [ ] 3.4 `MailTutorAssignmentNotifier` según D6; verificar con `MailTutorAssignmentNotifierTest` que el adjunto a Taller lleva el nombre y el contenido guardados y que un error de lectura no se propaga
- [ ] 3.5 Crear `V8__procedure_stored_files.sql` según D7, cambiar `InitialDocumentEntity`, `DegreeProjectEntity` y sus repositorios para guardar los ids, y actualizar `ProceduresConfiguration`; verificar con `JpaDegreeProcedureRepositoryTest`, `JpaDegreeProjectRepositoryTest` (Testcontainers) y `ProceduresWiringTest` la ida y vuelta de los ids y que ninguna entidad tiene `byte[]`

## 4. Integración y cierre

- [ ] 4.1 Verificar con `MailCommandsIntegrationTest` (GreenMail) los escenarios de `specs/mail-commands/spec.md`: correo con dos adjuntos guardados, comando pendiente tras un reinicio sin volver a guardar, correo descartado que conserva su archivo y adjunto vacío
- [ ] 4.2 Verificar con `DegreeProcedureByEmailIntegrationTest` (GreenMail, carpeta temporal de `cpms.storage.root`) la apertura con cuatro archivos adoptados (un archivo en disco por adjunto, sin copias), el reemplazo que deja la versión anterior intacta en el historial (`getHistory`), el reemplazo rechazado que no enlaza versiones, y el perfil reenviado a Taller con su PDF
- [ ] 4.3 Actualizar la sección "File storage" y el paso 2 de "How a use case is implemented" de `AGENTS.md` según D8; verificar que citan los nombres reales del código
- [ ] 4.4 Comparar la implementación de `mail` y `storage` con `Design/Mail` y `Design/Storage` en EA, y la de `procedures` con la arquitectura de referencia de `AGENTS.md`; registrar cualquier diferencia nueva en "Diferencias con el modelo" de `design.md`
- [ ] 4.5 Comprobar con `git grep` que no quedan `byte[]` en entidades JPA, ni imports de `mail` en `procedures.domain`, ni `BYTEA` fuera de `V1`, `V4` y `V5`; ejecutar `./gradlew build` con todas las pruebas en verde

## Workflow follow-up

- Archivar `uc-003-asignacion-tutor` antes de archivar este change.
- Aplicar en EA las diferencias de "Diferencias con el modelo" (`ea-checklist`), incluida la eliminación de `Design/Mail/application/FileStorage`.
- Archivar el change con `/opsx:archive` y cerrar el issue #9.

# Design

## Context

Motivación y alcance: ver `proposal.md`. Estado actual que da forma al diseño:

- `Pop3EmailInbox.collect()` arma cada adjunto como `mail.domain.File{id, name, data}`. `InboxPollerService.storeNewMessages()` guarda el correo con los bytes antes de procesarlo, y `getNotProcessed()` lo vuelve a leer de la base; así un reinicio no pierde los adjuntos.
- Los handlers de `procedures` pasan `email.getAttachments()` (`List<File>`) a los comandos, y el dominio (`DegreeProcedure`, `DegreeProject`, `InitialDocument`, `ProjectProfile`) valida y guarda los bytes sobre `mail.domain.File`.
- `StoredFileService` corre dentro de la transacción de quien lo llama; `LocalDiskFileStorage.write` exige una transacción activa, `read` no. `StoredFile.supersedeBy(next)` enlaza la cadena y copia `firstVersionId`.
- El modelo EA tiene diseño para `Mail` y `Storage`; su `Design/Mail/application/FileStorage`, asociado a `InboxPollerService`, ya preveía que el canal guardara los adjuntos. `procedures` (UC-002, UC-003) no tiene diseño en EA.

## Goals / Non-Goals

**Goals:**

- Ningún byte de un archivo en la base de datos.
- Cada subsistema es cliente de `storage` por su cuenta: `procedures` no depende del dominio de `mail` para recibir archivos.
- Un PDF recibido queda una sola vez en disco, también al reemplazar un documento.

**Non-Goals:**

- Retención o borrado de adjuntos guardados.
- Cambiar los estados del correo (`RECEIVED`, `VALIDATED`, `PROCESSED`, `DISCARDED`) o la validación de cantidades.

## Decisions

### D1. El canal guarda los adjuntos al recibir el correo

`InboxPollerService` recibe `StoredFileService`. En `storeNewMessages()`, por cada correo nuevo y dentro de una transacción propia (`transactions.executeWithoutResult`):

```
 si email.findEmptyAttachment() presente:
     email.withAttachments([]) ; store(email) ; discard(email, "El adjunto <nombre> está vacío.")
 si no:
     por cada adjunto:  stored = storedFiles.store(name, contentType, data)
                        file.withStoredFileId(stored.getId())
     receivedEmails.store(email)
```

Si algo falla, el rollback borra los archivos escritos (D5 de `almacenamiento-de-archivos`), se registra el error en el log y se sigue con el siguiente correo; el mensaje queda en el servidor y se reintenta en el ciclo siguiente. Hoy una excepción ahí corta el ciclo entero.

Desde ese momento el correo pendiente ya tiene sus archivos en disco: `validate()` y `execute()` no cambian, y un reinicio no pierde nada.

Los adjuntos de todos los correos se conservan, también los de correos descartados o con comandos rechazados: son el registro de lo recibido, referenciado por `received_email_attachment`. La retención queda fuera de alcance.

Alternativas descartadas:
- Guardar el archivo en el caso de uso, después de validarlo: el canal tendría que conservar los bytes en algún lado hasta ejecutar el comando (en la base, o releyendo el mensaje del servidor POP3), y `procedures` seguiría recibiendo los bytes desde `mail`.
- Borrar los adjuntos al llegar a un estado final: `storage` tendría que saber si un caso de uso adoptó el archivo, lo que lo acopla con sus clientes.

### D2. `File` de `mail`: referencia persistida, contenido solo en memoria

- `File` gana `contentType` y `storedFileId`. `data` solo existe en el correo recién leído del servidor; un `File` rehidratado tiene `id` y `storedFileId`.
- `Pop3EmailInbox.collect()` toma el tipo base de `part.getContentType()` en minúsculas, sin parámetros, o `application/octet-stream` si no lo tiene.
- `ReceivedEmail` gana `findEmptyAttachment(): Optional<File>` y `getAttachmentIds(): List<UUID>` (los `storedFileId`, en orden), que usan los handlers.
- `received_email_attachment` guarda `id`, `received_email_id`, `position` y `stored_file_id`. El nombre, el tipo y el tamaño están en `stored_file`.

### D3. El contrato de los comandos lleva identificadores

Los handlers dejan de pasar `File`: `OpenDegreeProcedureCommand(..., List<UUID> attachmentIds)`, `ReplaceInitialDocumentCommand(reference, UUID attachmentId)` y `RegisterInitialProfileCommand(title, UUID attachmentId)`, armados con `email.getAttachmentIds()`. Así `procedures.application` usa `mail` solo en los handlers (el punto de enchufe, `EmailCommandHandler`) y su dominio no importa nada de `mail`. Ningún record de comando se omite.

### D4. Operaciones nuevas de `StoredFileService`

- `get(id): StoredFile`: los datos del archivo sin su contenido; `StoredFileNotFoundException` si no existe. Los casos de uso leen el nombre y el tamaño sin cargar los bytes.
- `appendVersion(previousId, nextId): StoredFile`: bloquea los dos archivos con `getByIdForUpdate`, siempre en orden de id para evitar interbloqueos; `StoredFileNotFoundException` si falta alguno; `previous.supersedeBy(next)`; `update` de los dos. No escribe en disco.
- `StoredFile.supersedeBy(next)` exige además que `next` sea un archivo suelto (`firstVersionId == id`, vigente y distinto del anterior); si no, lanza `StoredFileAlreadyVersionedException` ("El archivo ya forma parte del historial de otro archivo."), subclase de `DomainException` en `storage.domain`. `storeNewVersion` cumple esa condición, porque su archivo nuevo es siempre suelto antes de enlazarse.

La restricción `UNIQUE` de `next_version_id` sigue siendo la red de seguridad en la base. `getHistory` no cambia: sigue la cadena por `firstVersionId` y `nextVersionId`.

### D5. `procedures` valida sobre `StoredFile` y adopta el archivo

Dominio (sin `mail`):

- `InitialDocument.requirePdf(StoredFile, byte[] content)`: tamaño ≤ 25 MB (por `getSize()`), nombre `.pdf` y cabecera `%PDF-` (NFR-005), con los mensajes actuales. La usan UC-002 y UC-003.
- `InitialDocument.classify(StoredFile)`: tipo por el nombre, como hoy.
- `DegreeProcedure.open(User, User, List<StoredFile>)`: clasifica, rechaza tipos duplicados o faltantes, como hoy. `replaceInitialDocument(User, StoredFile)` y `documentOf(InitialDocument)`. `Document(storedFileId, updatedBy, updatedAt)`.
- `ProjectProfile(storedFileId, approved)` con `pending(StoredFile)`; `DegreeProject.registerInitialProfile(StoredFile, …)`.

Casos de uso (después de las comprobaciones de remitente, cuenta y trámite que ya hacen):

```
 open:     por id: f = get(id); requirePdf(f, readContent(id))  -> DegreeProcedure.open(..., files)
 replace:  f = get(id); requirePdf(f, readContent(id))
           previous = procedure.documentOf(classify(f)).storedFileId()
           procedure.replaceInitialDocument(requester, f) ; appendVersion(previous, f.getId())
 profile:  f = get(id); requirePdf(f, readContent(id)) -> registerInitialProfile(f, ...)
```

Adoptar es guardar el id: el archivo no se copia.

Alternativa descartada: un tipo propio de `procedures` con nombre y bytes: no hace falta, porque `StoredFile` ya trae los datos y el contenido se lee del servicio.

### D6. Sin nombres de archivo repetidos

Como `get` devuelve el nombre original, `initial_document.filename` y `degree_project.profile_filename` se eliminan; el nombre vive solo en `stored_file`. `MailTutorAssignmentNotifier`, en el listener `AFTER_COMMIT`, llama a `get(storedFileId)` y `readContent(storedFileId)` para armar el adjunto a Taller (FR-057); un error de lectura se registra en el log y no se relanza, como un error de envío.

### D7. Migraciones `V7` (mail) y `V8` (procedures)

No hay datos que conservar. Una migración por subsistema, para que cada uno cambie su esquema junto con sus entidades JPA:

- `V7__received_email_attachment_stored_file.sql`: `DELETE FROM received_email_attachment`; `DROP COLUMN data`, `DROP COLUMN name`; `ADD COLUMN stored_file_id UUID NOT NULL UNIQUE REFERENCES stored_file (id)`.
- `V8__procedure_stored_files.sql`: `DELETE` de `degree_project`, `initial_document` y `degree_procedure`; en `initial_document`, `DROP COLUMN content`, `DROP COLUMN filename` y `ADD COLUMN stored_file_id UUID NOT NULL UNIQUE REFERENCES stored_file (id)`; en `degree_project`, `DROP COLUMN profile_content`, `DROP COLUMN profile_filename` y `ADD COLUMN profile_stored_file_id UUID NOT NULL UNIQUE REFERENCES stored_file (id)`.

Un mismo `stored_file` puede estar referenciado por un adjunto y por un documento: es el mismo archivo.

### D8. Documentación de la arquitectura de referencia

La sección "File storage" de `AGENTS.md` pasa a decir: el canal de correo guarda los adjuntos; el handler pasa sus identificadores; el caso de uso valida con `get` y `readContent` y adopta el archivo guardando su id, o lo enlaza con `appendVersion`. La lista de operaciones suma `get` y `appendVersion`. El paso 2 de "How a use case is implemented" menciona `email.getAttachmentIds()` en lugar de los adjuntos.

## Risks / Trade-offs

- [El disco crece con los adjuntos de correos descartados o rechazados] → Volumen acotado: los correos automáticos se ignoran y cada adjunto tiene como máximo 25 MB. La retención queda para otro change.
- [Correos pendientes en la base al aplicar `V7`] → Pierden sus adjuntos: si el comando requiere adjuntos, se descartan por cantidad incorrecta y el remitente recibe la respuesta. Solo afecta a entornos de desarrollo.
- [Un caso de uso lee hasta 4 × 25 MB para validar la cabecera] → Aceptable con el límite de NFR-005; si molesta, `storage` puede ofrecer una lectura parcial más adelante.
- [Un archivo de un correo podría adoptarse en dos documentos] → `appendVersion` rechaza un archivo que ya pertenece a un historial, y las columnas `stored_file_id` son `UNIQUE` en cada tabla. Entre tablas distintas no hay restricción; ningún flujo actual lo hace.
- [`appendVersion` deja el archivo del correo dentro de la cadena del documento] → Es intencional: es el mismo archivo. El correo sigue referenciándolo.

## Migration Plan

1. Configurar `CPMS_STORAGE_ROOT` en cada entorno, si todavía no está.
2. Desplegar: Flyway aplica `V7` y `V8`, que vacían los adjuntos registrados, los trámites y los proyectos.
3. Rollback: no hay vuelta atrás de datos (se eliminan las columnas `BYTEA`). Volver a la versión anterior exige restaurar la base previa a `V7`.

## Diferencias con el modelo

**`Design/Storage`:**

- `StoredFileService`: operaciones nuevas `get(id: UUID): StoredFile` y `appendVersion(previousId: UUID, nextId: UUID): StoredFile`.
- `StoredFile.supersedeBy`: precondición nueva sobre `next` (archivo suelto).
- Clase nueva `StoredFileAlreadyVersionedException` (subclase de `DomainException`).

**`Design/Mail`:**

- `File`: atributos nuevos `contentType: String` y `storedFileId: UUID`, con `withContentType`, `getContentType`, `withStoredFileId` y `getStoredFileId`; `data` deja de persistirse.
- `ReceivedEmail`: operaciones nuevas `findEmptyAttachment(): Optional<File>` y `getAttachmentIds(): List<UUID>`.
- `InboxPollerService`: dependencia nueva hacia `Design/Storage/application/StoredFileService`; guarda los adjuntos y descarta los correos con un adjunto vacío (D1).
- `FileStorage` (`Design/Mail/application`) se elimina con su asociación a `InboxPollerService`: la reemplaza la dependencia anterior.

**UC-002 y UC-003 sin diseño en EA; se sigue la arquitectura de referencia.** Clases, operaciones y relaciones que el change crea o cambia en `procedures`:

- `InitialDocument.requirePdf(StoredFile, byte[])`, `InitialDocument.classify(StoredFile)`.
- `DegreeProcedure.open(User, User, List<StoredFile>)`, `replaceInitialDocument(User, StoredFile)`, `documentOf(InitialDocument)`; `DegreeProcedure.Document(storedFileId, updatedBy, updatedAt)`.
- `ProjectProfile(storedFileId, approved)`, `ProjectProfile.pending(StoredFile)`; `DegreeProject.registerInitialProfile(StoredFile, …)`.
- `OpenDegreeProcedureCommand.attachmentIds`, `ReplaceInitialDocumentCommand.attachmentId`, `RegisterInitialProfileCommand.attachmentId` (`UUID`).
- Dependencias nuevas de `DegreeProcedureUseCase`, `TutorAssignmentUseCase` y `MailTutorAssignmentNotifier` hacia `storage.application.StoredFileService`, y de `procedures.domain` hacia `storage.domain.StoredFile`. Se elimina la de `procedures.domain` hacia `mail.domain.File`.

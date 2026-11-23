# Proposal

## Why

El subsistema `storage` (change archivado `almacenamiento-de-archivos`) ya guarda archivos en disco con versiones, pero nadie lo usa: los PDF siguen dentro de PostgreSQL como `BYTEA` en tres columnas (`received_email_attachment.data`, `initial_document.content` y `degree_project.profile_content`, esta última agregada por UC-003), y cada documento queda dos veces en la base. Además, `procedures` depende del dominio de `mail` (`mail.domain.File`) para recibir los archivos. Es la segunda parte del issue #9.

Pertenece al trabajo compartido **almacenamiento de archivos**: no corresponde a un caso de uso y toca el canal de correo, UC-002, UC-003 y el propio `storage`.

## What Changes

- **Canal de correo**: al guardar un correo recibido, guarda cada adjunto en el almacenamiento de archivos, en la misma transacción, y registra solo la referencia al archivo. Al ejecutar el comando entrega al caso de uso los identificadores de esos archivos, no su contenido. Los adjuntos de todos los correos se conservan, también los de correos descartados o rechazados.
- Un correo con un adjunto vacío se descarta y se avisa al remitente.
- **Casos de uso**: reciben identificadores de archivos y deciden qué hacer con ellos a través de `StoredFileService`; validan el PDF (NFR-005) leyendo sus datos y su contenido, y **adoptan** el archivo como documento sin copiarlo. `procedures` deja de depender de `mail.domain.File`: su dominio y sus casos de uso solo conocen `storage`.
- **UC-002**: el trámite guarda el id del archivo vigente de cada documento inicial. Reemplazar un documento (A2, FR-055) enlaza el archivo recibido como versión siguiente del vigente y **conserva el anterior**.
- **UC-003**: el perfil inicial guarda el id del archivo recibido; el aviso a Taller (FR-057) lee nombre y contenido del almacenamiento después del commit.
- **Almacenamiento de archivos**: dos operaciones nuevas en `StoredFileService`: `appendVersion`, que enlaza un archivo ya guardado como versión siguiente de otro sin copiar su contenido, y `get`, que devuelve los datos de un archivo sin su contenido.
- **BREAKING (datos)**: migraciones `V7` (mail) y `V8` (procedures) que borran los metadatos de adjuntos, trámites, documentos iniciales y proyectos existentes, quitan las tres columnas `BYTEA` y los nombres de archivo repetidos, y agregan las referencias a `stored_file`. No hay datos que conservar en ningún entorno.

Fuera de alcance: retención o limpieza de los adjuntos guardados, consultar o descargar versiones anteriores por correo.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `mail-commands`: los adjuntos se guardan en el almacenamiento de archivos al recibir el correo y el caso de uso recibe sus identificadores; un adjunto vacío descarta el correo.
- `degree-procedures`: reemplazar un documento inicial conserva el anterior como versión previa del nuevo.
- `file-storage`: agregar un archivo ya guardado como versión nueva de otro, y consultar los datos de un archivo sin su contenido.

La capability `degree-projects` (del change `uc-003-asignacion-tutor`, aún sin archivar) no cambia de comportamiento: el perfil se sigue enviando a Taller igual.

## Impact

- **Elementos de EA en los que se basa**:
  - `Design/Mail/domain`: `ReceivedEmail`, `File` (cambian: referencia al archivo guardado, tipo de contenido, contenido solo en memoria).
  - `Design/Mail/application`: `InboxPollerService` (pasa a usar `StoredFileService`), `ReceivedEmailRepository`, `EmailCommandHandler`; `FileStorage` se elimina, como preveía el change `almacenamiento-de-archivos`.
  - `Design/Storage/application`: `StoredFileService` (operaciones nuevas `appendVersion` y `get`); `Design/Storage/domain`: `StoredFile`, `StoredFileRepository`.
  - UC-002 y UC-003 no tienen diseño en EA (`procedures`): se sigue la arquitectura de referencia de `AGENTS.md`.
- **Requisitos**: NFR-005, FR-002, FR-055, FR-057. Issue #9.
- **Código**: `storage` (dominio y servicio), `mail` (dominio, poller, POP3, persistencia, wiring), `procedures` (dominio, contratos, handlers, casos de uso, notifier, persistencia, wiring); migraciones `V7__received_email_attachment_stored_file.sql` y `V8__procedure_stored_files.sql`.
- **Dependencias**: ninguna nueva.
- **Documentos**: `AGENTS.md` (sección "File storage": quién guarda los adjuntos y cómo un caso de uso adopta un archivo), `USER-GUIDE.md` (respuesta nueva del canal para un adjunto vacío).

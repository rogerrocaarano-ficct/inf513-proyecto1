# Proposal

## Why

Los PDF que recibe el sistema se guardan dentro de PostgreSQL como `BYTEA`, dos veces: en el adjunto del correo y en el documento inicial de UC-002 (issue #9). Hace falta un almacenamiento de archivos común para todo el sistema, que guarde los bytes en una carpeta del entorno, deje en la base solo los metadatos y conserve las versiones anteriores de un archivo, antes de que UC-003 y UC-005 agreguen más documentos.

Pertenece al trabajo compartido **almacenamiento de archivos**: no corresponde a un caso de uso y lo usarán todos los que reciben o generan documentos (UC-002, UC-003, UC-005, UC-006). Es la primera parte del issue #9.

## What Changes

- Subsistema nuevo `storage` (`domain`, `application`, `infrastructure`), la única forma de guardar y leer archivos en todo el sistema.
- Cada archivo se guarda en disco bajo una carpeta configurable, sin sobrescribir nunca un archivo existente; la base guarda solo sus metadatos: nombre original, tipo de contenido, tamaño, clave en disco y fecha.
- Versionado: una versión nueva de un archivo se enlaza a la anterior, que se conserva. Desde cualquier versión se obtiene la vigente y el historial completo.
- Coherencia con la transacción: si la transacción en la que se guardó un archivo se revierte, el archivo escrito en disco se borra.
- Migración Flyway con la tabla de metadatos de los archivos.
- Propiedad `cpms.storage.root` (variable `CPMS_STORAGE_ROOT`), con una carpeta por defecto para el perfil `local` ignorada por git.
- Sección "File storage" en la arquitectura de referencia de `AGENTS.md`: cómo un caso de uso guarda un archivo o crea una versión.

Fuera de alcance (segunda parte del issue #9): usar el almacenamiento desde el canal de correo y desde UC-002, quitar las columnas `BYTEA` (`received_email_attachment.data`, `initial_document.content`) y eliminar `Mail/application/FileStorage` del modelo. Este change no modifica el comportamiento de ningún comando por correo.

## Capabilities

### New Capabilities

- `file-storage`: guardado de archivos en disco con sus metadatos en la base, lectura de su contenido, versiones encadenadas con historial y limpieza de archivos ante un rollback.

### Modified Capabilities

Ninguna.

## Impact

- **Elementos de EA en los que se basa** (diagrama de clases `Design/Storage` "Storage"):
  - `Design/Storage/domain`: `StoredFile`, `StoredFileRepository`, `StoredFileNotFoundException`, `StoredFileNotCurrentException`.
  - `Design/Storage/application`: `StoredFileService`, `FileStorage`.
  - `Design/common`: `AggregateRoot`, `DomainException`.
  - `Design/Mail/application/FileStorage` queda en el modelo sin uso hasta la segunda parte.
- **Requisitos**: NFR-005 (tamaño de los PDF; lo sigue validando cada caso de uso). Issue #9.
- **Código**: paquete nuevo `bo.org.tecnoweb.cpms.storage`; migración `V6__stored_file.sql`; propiedades en `application.properties` y `application-local.properties`. No cambia código de `mail`, `users` ni `procedures`.
- **Dependencias**: ninguna nueva.
- **Documentos**: `AGENTS.md` (arquitectura de referencia), `README.md` (carpeta de archivos en "Entorno de desarrollo"), `GLOSSARY.md` (término "Archivo almacenado"), `.gitignore`.

# Proposal

## Why

Los cuatro documentos que Secretaría adjunta al abrir el trámite se modelan como `InitialDocument`, un nombre genérico que además está invertido: el enum es el *tipo* de documento y el documento en sí es `DegreeProcedure.Document`. El enum también valida PDF en general, y por eso UC-003 lo usa para el perfil de proyecto, que no es uno de esos documentos. En la base de datos, `initial_document` tiene un `id` que el dominio no usa y repite datos que ya guarda el almacenamiento de archivos (`updated_at`) o que corresponden a su auditoría (`updated_by`).

La facultad llama a esos documentos **requisitos para iniciar el trámite de titulación por Proyecto de Grado**. Este change adopta ese término y simplifica su modelo.

Pertenece a **UC-002 Habilitación académica de solicitudes**, flujo básico (apertura) y A2 (reemplazo), y toca a UC-003 solo por la validación de PDF.

## What Changes

- **Requisitos de apertura**: el enum `InitialDocument` pasa a `OpeningRequirement` (carta de solicitud, hoja de vida, cédula de identidad y boleta de inscripción), con la clasificación por nombre de archivo. El trámite guarda, por cada requisito, el archivo almacenado de su versión vigente; se elimina `DegreeProcedure.Document`.
- **Validación de PDF aparte**: el límite de 25 MB y la cabecera PDF (NFR-005) salen del enum a un tipo propio del dominio de `procedures`, usado por UC-002 y UC-003.
- **Sin auditoría propia**: se quitan quién entregó y cuándo cada documento. La fecha de cada versión ya está en el almacenamiento de archivos; quién la guardó se tratará en un cambio del almacenamiento.
- **Tabla**: `initial_document` se reemplaza por `degree_procedure_requirement(procedure_id, requirement, stored_file_id)`, sin `id` propio, con clave `(procedure_id, requirement)`. Migración nueva que conserva los archivos de los trámites existentes; no se editan migraciones anteriores.
- **Nombres del reemplazo**: la operación, el comando de aplicación, el handler, el evento y el permiso de A2 pasan a hablar de requisitos (`replaceRequirement`, `REPLACE_OPENING_REQUIREMENT`, …). El comando de correo `tramites.reemplazarDocumento`, sus argumentos y las palabras clave de los archivos (`carta`, `cv`, `ci`, `boleta`) no cambian.
- **Textos al usuario**: las confirmaciones por correo y los mensajes de error hablan de requisitos de apertura en lugar de documentos iniciales.

Fuera de alcance: la auditoría de archivos en el almacenamiento, el nombre del comando de correo y el nombre de UC-002 en `README.md`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `degree-procedures`: la apertura y el reemplazo se expresan en términos de requisitos de apertura; el reemplazo deja de registrar quién y cuándo, más allá de la versión en el almacenamiento.

## Impact

- **Modelo de EA**: UC-002 no tiene diseño en EA; por decisión del equipo el modelo no se consulta en este change y se sigue la arquitectura de referencia de `AGENTS.md`.
- **Requisitos**: FR-002, FR-055 y NFR-005 sin cambio de alcance; su texto y las notas de `REQUIREMENTS.md` pasan a decir "requisitos de apertura".
- **Código**: `procedures` (dominio, casos de uso de UC-002 y UC-003, handler, notificador, persistencia y pruebas), `users.domain.RolePermission`; migración `V11`.
- **Dependencias**: ninguna nueva.
- **Documentos**: `GLOSSARY.md` ("Documentos iniciales" → "Requisitos de apertura"), `REQUIREMENTS.md`, `USER-GUIDE.md`, `README.md` (fila de UC-002).

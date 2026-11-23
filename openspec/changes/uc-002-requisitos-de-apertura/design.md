# Diseño

## Context

Ver `proposal.md` (Why). Estado actual en la rama:

- `procedures.domain.InitialDocument` (enum `LETTER`, `RESUME`, `IDENTITY`, `ENROLLMENT`) tiene la palabra clave y la etiqueta de cada tipo, `classify(file)`, `requirePdf(file, content)` (25 MB y cabecera `%PDF-`, NFR-005) e `invalid(message)`. `requirePdf` lo usan `DegreeProcedureUseCase` y `TutorAssignmentUseCase` (perfil de proyecto, UC-003).
- `DegreeProcedure` guarda `Map<InitialDocument, Document>`, con `Document(storedFileId, updatedBy, updatedAt)`, y expone `getDocuments`, `documentOf` y `replaceInitialDocument`.
- `initial_document(id, procedure_id, document_type, stored_file_id, updated_by, updated_at)`, mapeada por `InitialDocumentEntity` en un `@OneToMany` de `DegreeProcedureEntity`.
- Las versiones de cada documento viven en `storage` (`StoredFile.storedAt`, `appendVersion`, `getHistory`).

## Goals / Non-Goals

**Objetivos.** Que el código y la base de datos usen el término de negocio "requisito de apertura"; que el trámite guarde solo el archivo vigente de cada requisito; que la validación de PDF no dependa de los requisitos.

**Fuera del alcance.** Cambiar cómo se clasifican los archivos, el comando de correo, la cadena de versiones en `storage` y la auditoría de quién guarda cada archivo.

## Decisions

### Requisito de apertura

`procedures.domain.OpeningRequirement` reemplaza a `InitialDocument`, con los mismos valores, palabras clave (`carta`, `cv`, `ci`, `boleta`) y etiquetas. Conserva `label()`, `classify(file)` e `invalid(message)` (campo `adjuntos`). Los mensajes dicen "requisito": «No se pudo identificar el requisito del archivo X.».

- *Alternativa descartada:* `InitialDocumentType` y renombrar el record a `InitialDocument`. Mantiene el término genérico que no usa la facultad.

### El trámite guarda un archivo por requisito

- `DegreeProcedure` guarda `Map<OpeningRequirement, UUID> requirements`: el archivo almacenado de la versión vigente de cada requisito. Se elimina el record `Document`.
- `open(student, requester, semester, attachments)` sigue exigiendo un archivo por requisito: «Falta el requisito Carta de solicitud.», «Se recibió más de un documento para el requisito Hoja de vida.».
- `replaceInitialDocument` pasa a `replaceRequirement(requester, file)`; `documentOf(type)` pasa a `requirementOf(requirement)` y devuelve el `UUID`; `getDocuments()` pasa a `getRequirements()`. `rehydrate` recibe el mapa de `UUID`.
- Se quitan `updatedBy` y `updatedAt`: la fecha está en la versión de `stored_file`, y quién la guardó será parte de la auditoría del almacenamiento.

### Validación de PDF

Nuevo `procedures.domain.PdfDocument` con `require(StoredFile file, byte[] content)`: el límite de 25 MB, la extensión `.pdf` y la cabecera, con los mensajes actuales en el campo `adjuntos`. Lo usan `DegreeProcedureUseCase` (requisitos) y `TutorAssignmentUseCase` (perfil, UC-003). El Javadoc de `DegreeProject` y `ProjectProfile` lo cita en lugar de `InitialDocument#requirePdf`.

### Reemplazo (A2)

| Hoy | Pasa a |
| --- | --- |
| `ReplaceInitialDocumentCommand` / `…CommandHandler` | `ReplaceOpeningRequirementCommand` / `…CommandHandler` (`ACTION` sigue siendo `tramites.reemplazarDocumento`) |
| `DegreeProcedureUseCase.replace` | `replaceRequirement` |
| `DegreeProcedureDocumentReplacedEvent(…, documentLabel, …)` | `DegreeProcedureRequirementReplacedEvent(…, requirementLabel, …)` |
| `DegreeProcedureNotifier.notifyDocumentReplaced` | `notifyRequirementReplaced` |
| `RolePermission.REPLACE_INITIAL_DOCUMENT` («reemplazar documentos iniciales») | `REPLACE_OPENING_REQUIREMENT` («reemplazar requisitos de apertura») |

Correos de `MailDegreeProcedureNotifier`: la apertura dice «Requisitos de apertura registrados: 4.»; el reemplazo tiene asunto «Requisito de apertura reemplazado: <referencia>» y cuerpo «Se reemplazó el requisito <etiqueta> en el expediente <referencia>.».

### Persistencia

- `DegreeProcedureEntity` mapea los requisitos como colección de valores: `@ElementCollection` de `Map<OpeningRequirement, UUID>` con `@CollectionTable(name = "degree_procedure_requirement", joinColumns = @JoinColumn(name = "procedure_id"))`, `@MapKeyEnumerated(STRING)` y `@MapKeyColumn(name = "requirement")`, valor en `stored_file_id`. Se elimina `InitialDocumentEntity`.
- Migración `V11__degree_procedure_requirement.sql`: crea `degree_procedure_requirement(procedure_id UUID NOT NULL REFERENCES degree_procedure(id), requirement VARCHAR(10) NOT NULL CHECK (…), stored_file_id UUID NOT NULL UNIQUE REFERENCES stored_file(id), PRIMARY KEY (procedure_id, requirement))`, copia las filas de `initial_document` (`document_type` → `requirement`) y elimina `initial_document`.
- Las pruebas de integración que limpian `initial_document` con `JdbcTemplate` pasan a limpiar `degree_procedure_requirement`.

## Risks / Trade-offs

- [Se pierde quién entregó o reemplazó cada documento hasta que exista la auditoría del almacenamiento] → Aceptado por el equipo; queda propuesto como un cambio de `storage`.
- [Una base local con datos de `initial_document`] → La migración copia las filas antes de eliminar la tabla.
- [El comando `tramites.reemplazarDocumento` y el código ya no usan el mismo término] → Se mantiene por compatibilidad con la guía y los usuarios; el nombre del comando no cambia en este change.

## Diferencias con el modelo

UC-002 sin diseño en EA; se sigue la arquitectura de referencia. El modelo se reconstruirá desde el código; este change crea, cambia y elimina:

- `procedures.domain`: nuevos `OpeningRequirement` y `PdfDocument`; se eliminan `InitialDocument` y `DegreeProcedure.Document`; `DegreeProcedure` (`requirements: Map<OpeningRequirement, UUID>`, `replaceRequirement`, `requirementOf`, `getRequirements`); `DegreeProcedureRequirementReplacedEvent` reemplaza a `DegreeProcedureDocumentReplacedEvent`.
- `procedures.application`: `ReplaceOpeningRequirementCommand` y `ReplaceOpeningRequirementCommandHandler` reemplazan a los de documento inicial; `DegreeProcedureUseCase.replaceRequirement`; `DegreeProcedureNotifier.notifyRequirementReplaced`.
- `users.domain`: `RolePermission.REPLACE_OPENING_REQUIREMENT` reemplaza a `REPLACE_INITIAL_DOCUMENT`.
- Infraestructura: se elimina `InitialDocumentEntity`; colección de valores en `DegreeProcedureEntity`.

# Tasks

## 1. Dominio

Los cambios de nombre de este grupo y del siguiente se arrastran entre sí: el proyecto vuelve a compilar al terminar el grupo 3, y las pruebas de este grupo se ejecutan a partir de ahí.

- [x] 1.1 Crear `procedures.domain.PdfDocument.require(file, content)` con la validación de `InitialDocument.requirePdf` y sus mensajes (NFR-005); verificar con `PdfDocumentTest` (archivo de más de 25 MB, nombre sin `.pdf`, contenido sin cabecera, PDF válido), que reemplaza los casos de PDF de `InitialDocumentTest`
- [x] 1.2 Renombrar `InitialDocument` a `OpeningRequirement` sin `requirePdf`, con los mensajes en términos de requisito; verificar con `OpeningRequirementTest` (clasificación por palabra clave, nombre ambiguo o sin palabra clave rechazado) y eliminar `InitialDocumentTest`
- [x] 1.3 Cambiar `DegreeProcedure` a `Map<OpeningRequirement, UUID>` (`open`, `replaceRequirement`, `requirementOf`, `getRequirements`, `rehydrate`), eliminar `Document` y renombrar el evento a `DegreeProcedureRequirementReplacedEvent` con `requirementLabel`; actualizar el Javadoc de `DegreeProject` y `ProjectProfile`. Verificar con `DegreeProcedureTest`: un archivo por requisito, requisito faltante o repetido rechazado con el mensaje nuevo, reemplazo de un solo requisito y evento con su etiqueta
- [x] 1.4 Renombrar `RolePermission.REPLACE_INITIAL_DOCUMENT` a `REPLACE_OPENING_REQUIREMENT` («reemplazar requisitos de apertura»); verificar con las pruebas de permisos existentes que solo `ADMINISTRATIVE` lo tiene

## 2. Aplicación y avisos

- [x] 2.1 Renombrar `ReplaceInitialDocumentCommand` y su handler a `ReplaceOpeningRequirementCommand` y `ReplaceOpeningRequirementCommandHandler` (`ACTION` sin cambios), y `DegreeProcedureUseCase.replace` a `replaceRequirement`; usar `PdfDocument.require` en `DegreeProcedureUseCase` y `TutorAssignmentUseCase`. Verificar con `DegreeProcedureUseCaseTest`, `DegreeProcedureCommandHandlersTest` y `TutorAssignmentUseCaseTest`
- [x] 2.2 Renombrar `DegreeProcedureNotifier.notifyDocumentReplaced` a `notifyRequirementReplaced` y cambiar los textos de `MailDegreeProcedureNotifier` según el diseño; verificar con `MailDegreeProcedureNotifierTest` los asuntos y cuerpos de apertura y reemplazo
- [x] 2.3 Actualizar `ProceduresConfiguration`; verificar con `ProceduresWiringTest`

## 3. Persistencia

- [x] 3.1 Crear `V11__degree_procedure_requirement.sql` según el diseño (tabla nueva sin `id`, copia de las filas de `initial_document`, eliminación de `initial_document`) sin editar migraciones anteriores; mapear la colección de valores en `DegreeProcedureEntity` y eliminar `InitialDocumentEntity`. Verificar con `JpaDegreeProcedureRepositoryTest` (Testcontainers): ida y vuelta de los cuatro requisitos, reemplazo de uno, `degree_procedure_requirement` con PK `(procedure_id, requirement)` y sin columna `id`, e `initial_document` inexistente
- [x] 3.2 Cambiar la limpieza con `JdbcTemplate` de `MailCommandsIntegrationTest`, `StoredFileServiceIntegrationTest` y `DegreeProcedureByEmailIntegrationTest` a `degree_procedure_requirement`, y las consultas de esta última que leen `initial_document`; verificar que las tres pasan

## 4. Documentación

- [x] 4.1 En `GLOSSARY.md`, reemplazar "Documentos iniciales" por "Requisitos de apertura" («requisitos para iniciar el trámite de titulación por Proyecto de Grado: carta de solicitud, hoja de vida, fotocopia de cédula de identidad y boleta de inscripción con Modalidad de Graduación», `OpeningRequirement`), en orden alfabético y con las columnas actuales, y ajustar "Solicitud de titulación"; verificar que el término adoptado coincide con el enum
- [x] 4.2 En `REQUIREMENTS.md` (FR-002, FR-055 y sus notas), `README.md` (fila de UC-002) y `USER-GUIDE.md` (`tramites.abrirTramite`, `tramites.reemplazarDocumento`), decir "requisitos de apertura" en lugar de "documentos iniciales" sin cambiar el alcance ni las columnas; verificar con `git grep -i "documento.* inicial"` que no quedan menciones fuera del historial de `openspec/changes/archive`

## 5. Integración y revisión

- [x] 5.1 Verificar con `DegreeProcedureByEmailIntegrationTest` (GreenMail) la apertura y el reemplazo de un requisito por correo, con la confirmación «Requisito de apertura reemplazado: <referencia>»
- [x] 5.2 Comparar la implementación con la "Reference Architecture" de `AGENTS.md` y con la lista de "Diferencias con el modelo" de `design.md`; registrar cualquier diferencia nueva
- [x] 5.3 Comprobar con `git grep` que no quedan `InitialDocument`, `initial_document` (salvo en migraciones anteriores), `REPLACE_INITIAL_DOCUMENT` ni `DocumentReplaced` en `src/`, y ejecutar `./gradlew build` con todas las pruebas en verde

## Workflow follow-up

- Al archivar, actualizar el `## Purpose` de `openspec/specs/degree-procedures/spec.md` para hablar de requisitos de apertura.
- Archivar el change cuando las tareas estén hechas y las pruebas pasen.

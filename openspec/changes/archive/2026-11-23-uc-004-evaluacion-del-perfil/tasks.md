# Tareas

## 1. Requisitos y glosario

- [x] 1.1 Actualizar `REQUIREMENTS.md`: FR-005 con "aprobación", dos FR nuevos (aviso al estudiante de cada evaluación de su perfil con las observaciones; aviso al director del perfil aprobado), NFR-003 con el texto libre en el cuerpo del correo y la nota de FR-003 citando UC-003; verificar que los IDs nuevos siguen la numeración y no se repiten.
- [x] 1.2 Actualizar `GLOSSARY.md` ("Visto bueno" solo para el documento, "Estado del perfil" → `ProjectProfileStatus`, "Evaluación del perfil", "Observación" → `ProjectProfile.observations`) y reemplazar en `use-cases/UC-004.md` la línea "Pendiente de agregar" por los IDs nuevos; verificar que cada nombre de código del design figura en el glosario.

## 2. Dominio

- [x] 2.1 Crear `ProjectProfileStatus` y convertir `ProjectProfile` en versión con título, estado, observaciones y fecha de evaluación; verificar con pruebas unitarias de sus validaciones.
- [x] 2.2 Cambiar `DegreeProject` a una lista de versiones con `registerProfile`, `approveProfile`, `observeProfile`, `rejectProfile`, `getProfile` y `getProfiles`, y `assignTutor` exigiendo la versión vigente `APPROVED`; verificar en `DegreeProjectTest` la regla de FR-058, cada transición, las observaciones vacías, los eventos levantados y el tutor sobre un perfil no aprobado.
- [x] 2.3 Crear `DegreeProjectProfileApprovedEvent`, `DegreeProjectProfileObservedEvent` y `DegreeProjectProfileRejectedEvent`; renombrar `REGISTER_INITIAL_PROFILE` a `REGISTER_PROJECT_PROFILE` y agregar `EVALUATE_PROJECT_PROFILE` en `RolePermission`; verificar en `RoleTest` qué roles tienen cada permiso.

## 3. Persistencia

- [x] 3.1 Escribir `V12__project_profile_version.sql` (tabla nueva, copia de los proyectos existentes como versión 1, baja de `profile_stored_file_id` y `profile_approved`) y mapear las versiones en `DegreeProjectEntity` con `@ElementCollection`; verificar en `JpaDegreeProjectRepositoryTest` que se guardan y leen varias versiones en orden, con estado y observaciones, y que un proyecto creado antes de V12 queda como versión 1.

## 4. Caso de uso y comandos

- [x] 4.1 Crear `ProjectProfileEvaluationUseCase` con `registerProfile` (incluye `appendVersion` en la versión nueva), `approveProfile`, `observeProfile` y `rejectProfile`, y su puerto `ProjectProfileEvaluationNotifier`; quitar de `TutorAssignmentUseCase` la presentación del perfil y el `StoredFileService`; verificar con `ProjectProfileEvaluationUseCaseTest` (dobles de los puertos y `ProcedureFixtures.Storage`) cada flujo y cada excepción E1 a E7 de `use-cases/UC-004.md`, y ajustar `TutorAssignmentUseCaseTest`.
- [x] 4.2 Crear `RegisterProfileCommand`, `ObserveProfileCommand`, `RejectProfileCommand` y los handlers `RegisterProfileCommandHandler`, `ApproveProfileCommandHandler`, `ObserveProfileCommandHandler` y `RejectProfileCommandHandler`; verificar en `ProjectProfileEvaluationCommandHandlersTest` la acción, los conteos de argumentos y archivos, y que observar y rechazar pasan el cuerpo del correo.
- [x] 4.3 Actualizar `USER-GUIDE.md` con `proyectos.aprobarPerfil`, `proyectos.observarPerfil`, `proyectos.rechazarPerfil`, la versión nueva con `proyectos.registrarPerfil` y la aprobación en `proyectos.asignarTutor`; verificar que cada comando del código tiene su sección con un ejemplo.

## 5. Notificación y wiring

- [x] 5.1 Crear `MailProjectProfileEvaluationNotifier` (perfil a Taller con su PDF, evaluación al estudiante con observaciones, aprobación al director) y dejar `MailTutorAssignmentNotifier` solo con el aviso del tutor; verificar en `MailProjectProfileEvaluationNotifierTest` la publicación, los destinatarios, los adjuntos y que un fallo de envío se registra sin propagarse.
- [x] 5.2 Declarar el caso de uso y los cuatro handlers en `ProceduresConfiguration`; verificar en `ProceduresWiringTest` que el canal descubre los cuatro comandos.

## 6. Verificación de extremo a extremo

- [x] 6.1 Ampliar `DegreeProcedureByEmailIntegrationTest` con GreenMail: presentar, observar, presentar la versión nueva, aprobar y asignar el tutor, comprobando los correos recibidos por estudiante, Taller y director; verificar que la prueba pasa.
- [x] 6.2 Comparar la implementación con la "Reference Architecture" de `AGENTS.md` (no hay diseño en EA) y con "Diferencias con el modelo" del design; ejecutar `./gradlew build` y `openspec validate uc-004-evaluacion-del-perfil --strict` y verificar que ambos terminan sin errores.

## 7. Versionado del perfil en el negocio

- [x] 7.1 Dejar en `AGENTS.md` ("File storage") el historial de archivos del almacenamiento (`appendVersion`, `storeNewVersion`, `getHistory`) como en desuso para código nuevo, con el versionado en la tabla del agregado dueño; verificar que la sección ya no indica `appendVersion` para documentos nuevos.
- [x] 7.2 Agregar `version` a `ProjectProfile`, asignarlo en `DegreeProject.registerProfile`, comprobar la secuencia en `rehydrate` y quitar `appendVersion` de `ProjectProfileEvaluationUseCase`; verificar en `DegreeProjectTest` y `ProjectProfileEvaluationUseCaseTest` la numeración y que el almacenamiento no encadena los PDF.
- [x] 7.3 Renombrar la migración y la tabla a `degree_project_profile_version`, con la columna `version` desde 1, y el embeddable a `DegreeProjectProfileVersion`; ajustar el glosario; verificar con `JpaDegreeProjectRepositoryTest`, la prueba de integración y `./gradlew build`.

## Workflow follow-up

- Actualizar el propósito de `openspec/specs/degree-projects/spec.md` para que cubra solo la asignación del tutor, al archivar el change.
- Archivar el change con `/opsx:archive` cuando las tareas estén hechas y las pruebas pasen.

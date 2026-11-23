# Tasks

## 1. Cuentas (`users`)

- [ ] 1.1 Agregar `RolePermission.HOLD_DEGREE_PROCEDURE` («tener un trámite de titulación», `Role.STUDENT`) con su Javadoc (UC-002, FR-056); verificar con `RolePermissionTest` (o la prueba de permisos existente) que solo `STUDENT` lo tiene
- [ ] 1.2 Reemplazar `UserRepository.getByEmailForUpdate` por `getByUagrmCodeForUpdate(String code)` y su método derivado con bloqueo pesimista en `SpringDataUserAccountRepository`; verificar con `JpaUserRepositoryTest` (Testcontainers) que encuentra la cuenta por registro y devuelve vacío para un registro inexistente

## 2. Semestre vigente

- [ ] 2.1 Crear `procedures.domain.Semester` (record `year`, `period`; `parse`, `toString`) según el diseño; verificar con `SemesterTest`: `2026-1` y `2026-2` válidos, `2026-3`, `26-2`, vacío y nulo rechazados, `toString()` → `2026-2`
- [ ] 2.2 Crear el puerto `procedures.application.AcademicCalendar` y el adaptador `procedures.infrastructure.ConfiguredAcademicCalendar`, que valida `cpms.academic.current-semester` al crearse; agregar la propiedad con `${CPMS_CURRENT_SEMESTER:2026-2}` a `application.properties`. Verificar con `ConfiguredAcademicCalendarTest` que devuelve el semestre configurado y que un valor inválido impide crearlo

## 3. Dominio del trámite

- [ ] 3.1 Cambiar `DegreeProcedure`: `builder().open(student, requester, semester, attachments)`, referencia `String` asignada con `referenceOf`, permiso `HOLD_DEGREE_PROCEDURE` en lugar de comparar roles, `parseReference(String)` con el formato `<año>-<periodo>-<registro>`; eliminar `withStudent`, `referenceLabel`, `requireMatchingStudent` e `incompatibleAccount`. Agregar `reference` a `DegreeProcedureOpenedEvent`. Verificar con `DegreeProcedureTest`: referencia `2026-2-218001234` y evento con ella, cuenta que no es de estudiante o desactivada rechazada, referencias válidas e inválidas en `parseReference`, reemplazo sin cambios
- [ ] 3.2 Ajustar `DegreeProcedureRepository` (`insert` sin retorno, `existsByReference`, `getOpenByReferenceForUpdate(String)`) y su Javadoc; el proyecto vuelve a compilar al terminar el grupo 4

## 4. Caso de uso, handlers y avisos

- [ ] 4.1 Reescribir `DegreeProcedureUseCase.open` según el flujo del diseño (cuenta por registro, trámite en curso, trámite en el semestre, PDF, apertura, inserción, aviso) y quitar `UserManagementNotifier` y `registerStudent`; `replace` usa la referencia `String`. Verificar con `DegreeProcedureUseCaseTest`: apertura sobre cuenta activa, registro sin cuenta, cuenta de otro rol, cuenta desactivada, trámite en curso, trámite cerrado en el mismo semestre, trámite cerrado en otro semestre permitido, remitente sin permiso; y reemplazo con la nueva referencia
- [ ] 4.2 Cambiar `OpenDegreeProcedureCommand` a `(code, attachmentIds)` y `OpenDegreeProcedureCommandHandler` a un argumento y cuatro archivos; `ReplaceInitialDocumentCommandHandler` sin cambios de forma. Verificar con `DegreeProcedureCommandHandlersTest`
- [ ] 4.3 Cambiar `DegreeProcedureNotifier.notifyOpened(event)` y `MailDegreeProcedureNotifier` para tomar la referencia del evento y eliminar `OpeningNotice`; verificar con `MailDegreeProcedureNotifierTest` que Secretaría y el estudiante reciben la referencia `2026-2-…` después del commit
- [ ] 4.4 Ajustar `TutorAssignmentUseCase` para usar `procedure.getReference()` y `parseReference(String)`; verificar con `TutorAssignmentUseCaseTest` y `TutorAssignmentCommandHandlersTest` que la asignación de tutor funciona con la nueva referencia
- [ ] 4.5 Actualizar `ProceduresConfiguration` (`AcademicCalendar` en lugar de `UserManagementNotifier`); verificar con `ProceduresWiringTest`

## 5. Persistencia

- [ ] 5.1 Crear `V10__degree_procedure_uuid_key.sql` según el diseño (referencia de texto rellenada desde `opened_at` y el registro, `initial_document.procedure_id`, PK en `id`, referencia `NOT NULL UNIQUE`) sin editar `V4`; cambiar `DegreeProcedureEntity` (`@Id UUID`), `InitialDocumentEntity` (unión por `procedure_id`), `SpringDataDegreeProcedureRepository` (`existsByReference`, `findForUpdateByReferenceAndClosedAtIsNull(String)`) y `JpaDegreeProcedureRepository`. Verificar con `JpaDegreeProcedureRepositoryTest` (Testcontainers): ida y vuelta con referencia y documentos, `existsByReference`, referencia repetida rechazada por la base, búsqueda por referencia solo de trámites en curso, y `JpaDegreeProjectRepositoryTest` sin cambios

## 6. Documentación

- [ ] 6.1 Actualizar `USER-GUIDE.md`: `tramites.abrirTramite[218001234]` con los cuatro PDF, la cuenta creada antes con `usuarios.crearCuenta`, los rechazos (sin cuenta, otro rol, desactivada, en curso, mismo semestre) y la referencia `2026-2-218001234` en `tramites.reemplazarDocumento` y `proyectos.asignarTutor`; verificar que cada ejemplo coincide con el handler
- [ ] 6.2 Actualizar `REQUIREMENTS.md`: nota de FR-051/FR-056 (la cuenta se crea por UC-001 y la apertura la referencia por su registro) y la regla de un trámite por estudiante y semestre, acordada el 2026-11-23, sin cambiar el texto de los FR ni las columnas; verificar que no queda texto sobre crear la cuenta al abrir el trámite
- [ ] 6.3 Actualizar `GLOSSARY.md` con las columnas actuales y en orden alfabético: "Referencia del trámite" (forma `<semestre>-<registro>`, `DegreeProcedure.reference`), "Semestre" → `Semester`, "Calendario académico" → `AcademicCalendar`, "Procedimientos" → subsistema `procedures`; verificar que cada término adoptado coincide con el código
- [ ] 6.4 Indicar en `README.md`, sección "Entorno de desarrollo", la variable `CPMS_CURRENT_SEMESTER` y cuándo cambiarla; verificar que `./gradlew bootRun` inicia con el valor por defecto

## 7. Integración y revisión

- [ ] 7.1 Actualizar `DegreeProcedureByEmailIntegrationTest` (GreenMail): crear la cuenta con `usuarios.crearCuenta`, abrir con el registro, recibir la referencia `2026-2-…`, rechazar el registro sin cuenta y el segundo trámite del semestre, reemplazar un documento y asignar tutor con la referencia
- [ ] 7.2 Comparar la implementación con la "Reference Architecture" de `AGENTS.md` (sin comparación directa de roles, sin SQL fuera de la migración, record de comando, notificador después del commit) y con la lista de "Diferencias con el modelo" de `design.md`; registrar cualquier diferencia nueva
- [ ] 7.3 Comprobar con `git grep` que no quedan `getByEmailForUpdate`, `referenceLabel`, `requireMatchingStudent`, `OpeningNotice` ni `TR-0` en `src/` ni en `USER-GUIDE.md`, y ejecutar `./gradlew build` con todas las pruebas en verde

## Workflow follow-up

- Archivar el change cuando las tareas estén hechas y las pruebas pasen.
- Reconstruir el modelo EA desde el código cuando el equipo retome EA (`ea-drift`).

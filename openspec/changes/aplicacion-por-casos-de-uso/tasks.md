# Tareas

## 1. Subsistema `users`

- [x] 1.1 Mover con `git mv` las 10 clases de `users.application` a `users.application.usermanagement` y sus 4 tests a `src/test/.../users/application/usermanagement`, ajustando `package` e imports en `UsersConfiguration`, `MailUserManagementNotifier` y `MailUserManagementNotifierTest`; verificar que `./gradlew compileTestJava` termina sin errores y que no queda ningún archivo en la raíz de `users/application`.

## 2. Subsistema `procedures`

- [x] 2.1 Pasar el helper `stored(UUID)` de `DegreeProcedureCommandHandlersTest` a `ProcedureFixtures` como `public static` y usarlo desde ambos tests de handlers; verificar que ningún test importa `DegreeProcedureCommandHandlersTest`.
- [x] 2.2 Mover con `git mv` las clases y tests de UC-002 a `procedures.application.degreeprocedure` (incluido `AcademicCalendar`), de UC-003 a `procedures.application.tutorassignment` y de UC-004 a `procedures.application.projectprofileevaluation`, ajustando `package` e imports en `ProceduresConfiguration`, `MailDegreeProcedureNotifier`, `MailTutorAssignmentNotifier`, `MailProjectProfileEvaluationNotifier`, `ConfiguredAcademicCalendar` y `ProceduresWiringTest`; verificar que `./gradlew compileTestJava` termina sin errores y que no queda ningún archivo en la raíz de `procedures/application`.

## 3. Documentación

- [x] 3.1 Actualizar `AGENTS.md`: la tabla de paquetes con `<subsystem>.application.<usecase>`, el punto 1 de "How a use case is implemented" con la regla de nombres y lo que reúne cada paquete, y los puntos 3 y 4 para que los contratos y el notifier vivan en el paquete del caso de uso; verificar que ninguna regla sigue indicando la raíz de `application` para piezas de un caso de uso.

## 4. Verificación

- [x] 4.1 Comparar el resultado con el design (tabla "Qué va en cada paquete") y con la "Reference Architecture" de `AGENTS.md` actualizada; ejecutar `./gradlew build` y `openspec validate aplicacion-por-casos-de-uso --strict` y verificar que ambos terminan sin errores, con el mismo número de tests que antes del change.

## Workflow follow-up

- Archivar el change con `/opsx:archive` cuando las tareas estén hechas y el build pase.

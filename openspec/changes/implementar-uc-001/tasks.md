# Tasks

## 1. Messaging: rechazos de comandos

- [ ] 1.1 Agregar `CommandRejectedException` a `messaging/application`, hacer que `CommandDispatcher` deshaga la transacción, registre el Message-ID y responda con su mensaje (D2), y documentarla en `docs/model/design/subsystems/messaging.md`; verificar con una prueba en `MailCommandsTests` que un comando de prueba que guarda un cambio y luego lanza la excepción no deja el cambio y responde el mensaje

## 2. GraduationProcesses mínimo

- [ ] 2.1 Crear `graduationprocesses/application` con `GraduationParticipation`, `ParticipantKind`, `GraduationProcessSummary` y `NoGraduationProcessesParticipation` (D6), y actualizar `docs/model/design/subsystems/graduation-processes.md`; verificar con una prueba de contexto que el bean existe y devuelve una lista vacía

## 3. Users: dominio

- [ ] 3.1 Agregar a `Role` `canManage`, `validateCombination` y `participantKinds` (D3), con `UserRuleViolationException` y `RejectionReason`; verificar con pruebas unitarias los escenarios de "Combinaciones de roles permitidas" y las reglas de alcance de "Alcance del director de carrera"
- [ ] 3.2 Agregar a `UserAccount` los datos del estudiante, `UserAccountData`, `AccountField`, la validación de datos obligatorios en `create` y las operaciones `changeField`, `changeRoles`, `deactivate` y `reactivate` que devuelven `FieldChange` (D4), y adaptar `InitialAdministratorCreator`; verificar con pruebas unitarias los escenarios de "Datos obligatorios y válidos" y los `FieldChange` de cada operación, y con `./gradlew test` que las pruebas del administrador inicial siguen pasando

## 4. Users: servicio de gestión

- [ ] 4.1 Implementar en `UserManagementService` `createAccount`, `listAccounts` y `findAccount`, con el alcance, la unicidad con índice único (D5), el único docente de Taller de Grado, el registro de cambios y el aviso de creación; verificar con pruebas de integración los escenarios de "Creación de una cuenta de usuario", "Registro universitario único", "Un solo docente de Taller de Grado activo" (creación) y "Consulta de cuentas de usuario"
- [ ] 4.2 Implementar `updateAccount` con las reglas de la creación, el último administrador, los trámites afectados de los roles quitados y el aviso de cambio de roles; verificar con pruebas de integración los escenarios de "Modificación de una cuenta de usuario" y "Quitar el rol al único administrador"
- [ ] 4.3 Implementar `deactivateAccount` y `reactivateAccount` con el último administrador, el único docente de Taller de Grado, los trámites afectados y sus avisos; verificar con pruebas de integración los escenarios de "Desactivación y reactivación", "Desactivar al único administrador", "Reactivación de un segundo docente de Taller de Grado" y "Trámites afectados", este último con un doble de `GraduationParticipation`

## 5. Users: comandos por correo

- [ ] 5.1 Implementar `UserCommandSupport` con los nombres de roles y campos en español, la conversión de argumentos y los textos de respuesta y de aviso (D1, D7); verificar con pruebas unitarias los escenarios de "Nombres de los roles en los comandos"
- [ ] 5.2 Implementar los seis manejadores de comandos (D1); verificar con pruebas de punta a punta en GreenMail los escenarios de "Comandos de gestión de usuarios", "Alcance del director de carrera" y "Aviso al usuario afectado", y que un comando enviado desde una cuenta desactivada no se ejecuta

## 6. Modelo

- [ ] 6.1 Actualizar la realización de diseño de UC-001 a D1, D2 y D6 (sin `authenticate` ni `UNAUTHORIZED_SENDER`, con `handle(command, sender)`) y `docs/model/design/subsystems/users.md`; verificar con los checks de `docs/model/use-cases/AGENTS.md` y `docs/model/design/AGENTS.md` que cada clase del código está en la realización y que los derivados (Used by, Use cases, Provides, Requires) están al día

## 7. Integración

- [ ] 7.1 Ejecutar `./gradlew test` completo y, con el stack de Docker Compose, crear un docente desde el administrador inicial por Mailpit y verificar que llegan la confirmación y el aviso

## Workflow follow-up

- Integrar `UC-001-gestionar-usuarios` en `develop` y archivar el cambio con `/opsx:archive`.

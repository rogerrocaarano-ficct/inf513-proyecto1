# Diseño

## Contexto

El motivo está en `proposal.md`. Estado actual de la capa `application`:

| Paquete | Clases | Casos de uso |
| --- | --- | --- |
| `users.application` | 10 | UC-001 (`UserManagementUseCase`) |
| `procedures.application` | 20 | UC-002 (`DegreeProcedureUseCase`), UC-003 (`TutorAssignmentUseCase`), UC-004 (`ProjectProfileEvaluationUseCase`) y el puerto `AcademicCalendar` |
| `mail.application` | 5 | Ninguno: canal de correo y sus puertos |
| `storage.application` | 2 | Ninguno: servicio de archivos |

Todas las clases de `users.application` y `procedures.application` son `public`, de modo que moverlas a subpaquetes no rompe visibilidad. Fuera de `application` las importan `UsersConfiguration`, `MailUserManagementNotifier`, `ProceduresConfiguration`, los tres `Mail…Notifier` de `procedures` y `ConfiguredAcademicCalendar`; en los tests, `ProceduresWiringTest` y `MailUserManagementNotifierTest`. El único acoplamiento de paquete es de test: `ProjectProfileEvaluationCommandHandlersTest` usa el helper package-private `DegreeProcedureCommandHandlersTest.stored`.

## Objetivos y no objetivos

**Objetivos:**
- Un paquete por caso de uso en `application`, con todas sus piezas.
- Una regla de nombres mecánica, que no dependa de los IDs de casos de uso.

**No objetivos:**
- Separar un subsistema para el proyecto de grado (`projects`): se decide más adelante, probablemente con UC-005.
- Reorganizar `domain` o `infrastructure`, que se siguen viendo como un todo por subsistema.
- Renombrar clases. `TutorAssignmentUseCase` se renombrará al incorporar el tribunal (UC-003), y su paquete con ella.
- Tocar `mail` y `storage`, que no tienen casos de uso.

## Decisiones

### Nombre del paquete

`<subsystem>.application.<caso>`, donde `<caso>` es el nombre de la clase del caso de uso en minúsculas y sin el sufijo `UseCase`:

| Clase | Paquete | Caso de uso |
| --- | --- | --- |
| `UserManagementUseCase` | `users.application.usermanagement` | UC-001 |
| `DegreeProcedureUseCase` | `procedures.application.degreeprocedure` | UC-002 |
| `TutorAssignmentUseCase` | `procedures.application.tutorassignment` | UC-003 |
| `ProjectProfileEvaluationUseCase` | `procedures.application.projectprofileevaluation` | UC-004 |

Alternativas descartadas:
- **Por ID** (`uc004`): los IDs y las fronteras de los casos de uso cambiaron el 2026-11-23; la clase ya es el nombre del caso de uso en el código.
- **Por tipo de mensaje** (`command/`, `query/`): reparte un caso de uso entre carpetas, y `procedures` no tiene consultas todavía. El sufijo `…Command`/`…Query` ya los distingue.

### Qué va en cada paquete

El caso de uso, su `<UseCase>Notifier`, sus contratos (`…Command`, `…Query`, `…QueryItem`, `…QueryResult`) y sus handlers (`…CommandHandler`, `…QueryHandler`). Un puerto que usa un solo caso de uso va con él: `AcademicCalendar` pasa a `degreeprocedure`. En la raíz de `application` quedan solo los puertos que comparten varios casos de uso; hoy no hay ninguno.

| Paquete | Clases |
| --- | --- |
| `users.application.usermanagement` | `UserManagementUseCase`, `UserManagementNotifier`, `SignUpUserCommand`, `SignUpUserCommandHandler`, `ListUsersQuery`, `ListUsersQueryHandler`, `ListUsersQueryItem`, `ListUsersQueryResult`, `DisableUserCommandHandler`, `EnableUserCommandHandler` |
| `procedures.application.degreeprocedure` | `DegreeProcedureUseCase`, `DegreeProcedureNotifier`, `AcademicCalendar`, `OpenDegreeProcedureCommand`, `OpenDegreeProcedureCommandHandler`, `ReplaceOpeningRequirementCommand`, `ReplaceOpeningRequirementCommandHandler` |
| `procedures.application.tutorassignment` | `TutorAssignmentUseCase`, `TutorAssignmentNotifier`, `AssignTutorCommand`, `AssignTutorCommandHandler` |
| `procedures.application.projectprofileevaluation` | `ProjectProfileEvaluationUseCase`, `ProjectProfileEvaluationNotifier`, `RegisterProfileCommand`, `RegisterProfileCommandHandler`, `ApproveProfileCommandHandler`, `ObserveProfileCommand`, `ObserveProfileCommandHandler`, `RejectProfileCommand`, `RejectProfileCommandHandler` |

### Tests

Los tests de `application` se mueven al mismo subpaquete que la clase que prueban (`UserManagementUseCaseTest`, `SignUpUserCommandHandlerTest`, `ListUsersQueryHandlerTest`, `ChangeUserStatusCommandHandlersTest`, `DegreeProcedureUseCaseTest`, `DegreeProcedureCommandHandlersTest`, `TutorAssignmentUseCaseTest`, `TutorAssignmentCommandHandlersTest`, `ProjectProfileEvaluationUseCaseTest`, `ProjectProfileEvaluationCommandHandlersTest`). Los tests de integración por correo, de dominio e infraestructura no se mueven. El helper `stored(UUID)` pasa de `DegreeProcedureCommandHandlersTest` a `ProcedureFixtures` como método `public static`, para que lo usen los handlers de cualquier subpaquete.

### Cómo se mueve

Con `git mv`, para conservar el historial de cada archivo, y ajustando la línea `package` y los imports. No cambia ningún nombre de clase, método, bean ni constante `ACTION`; el canal de correo sigue encontrando los handlers por tipo (`EmailCommandHandler`). El Javadoc no cambia salvo donde nombre el paquete.

### Documentos

`AGENTS.md`:
- La tabla de paquetes describe `<subsystem>.application.<usecase>`.
- "How a use case is implemented", punto 1: el caso de uso y sus piezas viven en `<subsystem>.application.<usecase>`, con la regla de nombres; el punto 3 deja de decir "in `application`" para los contratos y el 4 para el notifier.

## Riesgos y compensaciones

- [Ramas abiertas que tocan `application`] → El change parte de `develop` con el PR de UC-004 ya fusionado; conviene integrarlo antes de abrir trabajo nuevo de UC-003 o UC-005.
- [Un caso de uso de `users` en un paquete propio parece sobrado] → Se acepta para que la regla sea la misma en todos los subsistemas.
- [El nombre del paquete cambia si se renombra la clase del caso de uso] → Es el efecto buscado: paquete y clase dicen lo mismo.

## Plan de migración

No hay migración de datos ni de configuración. Revertir es deshacer el commit.

## Diferencias con el modelo

Ninguna. El modelo EA está en pausa; el change no crea ni cambia clases, operaciones ni relaciones, solo los paquetes de `application`. Cuando se reconstruya el modelo, `Design/<Subsystem>/application` tendrá un paquete por caso de uso.

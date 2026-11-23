# Propuesta

## Motivo

Trabajo compartido, sin caso de uso propio: afecta a UC-001, UC-002, UC-003 y UC-004. La capa `application` concentra las piezas de todos los casos de uso de un subsistema en un mismo paquete: `procedures.application` ya tiene 20 clases de tres casos de uso mezcladas, y crecerá con el tribunal (UC-003) y la revisión del documento (UC-005). Agrupar cada caso de uso en su propio paquete deja juntas las piezas que cambian juntas.

El modelo EA está en pausa y no se consulta; la capa de infraestructura no se modela, y este change no cambia clases ni operaciones, solo paquetes.

## Cambios

- Cada caso de uso pasa a su paquete `<subsystem>.application.<caso>`, donde `<caso>` es el nombre de la clase del caso de uso en minúsculas y sin `UseCase`. El paquete reúne el caso de uso, su notifier, sus contratos (`…Command`, `…Query` y sus resultados) y sus handlers:
  - `users.application.usermanagement` (UC-001).
  - `procedures.application.degreeprocedure` (UC-002), con el puerto `AcademicCalendar`, que solo usa este caso de uso.
  - `procedures.application.tutorassignment` (UC-003).
  - `procedures.application.projectprofileevaluation` (UC-004).
- Los tests de `application` siguen la misma estructura.
- La arquitectura de referencia de `AGENTS.md` incorpora la regla.
- Sin cambios: las capas `domain` e `infrastructure`, los subsistemas `mail` y `storage`, los comandos de correo, las tablas y las respuestas. No se separa todavía un subsistema para el proyecto de grado.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

Ninguna. El change no cambia comportamiento observable, así que declara `skip_specs: true`.

## Impacto

- `src/main/java`: se mueven 30 clases de `application` de `users` y `procedures`, y se ajustan los imports de las configuraciones (`UsersConfiguration`, `ProceduresConfiguration`), de los notifiers de correo y de `ConfiguredAcademicCalendar`.
- `src/test/java`: se mueven los tests de `application` y se ajustan los imports de `ProceduresWiringTest` y `MailUserManagementNotifierTest`. El helper `stored`, que hoy comparten dos tests de handlers de `procedures`, pasa a `ProcedureFixtures`.
- Documentos: `AGENTS.md` ("How a use case is implemented" y la tabla de paquetes).

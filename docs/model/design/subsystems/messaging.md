# Messaging

| | |
| --- | --- |
| Type | General |
| Comes from | [Unassigned](../../analysis/unassigned/package.md) (NotificacionUI), AM-002 |
| Status | Draft |
| Iteration | I1 |

## Responsibility

Sondea el buzón POP3, interpreta el comando del asunto y lo entrega al manejador que un subsistema de negocio registró para ese comando; envía por SMTP las respuestas y las notificaciones (AD-005, AD-006). Es general: no conoce los comandos de negocio, que los subsistemas registran en él.

## Provided Interfaces

### CommandRegistry

- **Used by (derived):** Users
- **Use cases (derived):** None

| Operation | Description | Errors |
| --- | --- | --- |
| Pending | Las operaciones se agregan con las realizaciones de diseño que las usan | — |

### Notifier

- **Used by (derived):** Users
- **Use cases (derived):** None

| Operation | Description | Errors |
| --- | --- | --- |
| Pending | Las operaciones se agregan con las realizaciones de diseño que las usan | — |

## Required Interfaces

None.

## Open Questions

None.

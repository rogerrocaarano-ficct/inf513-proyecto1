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
- **Use cases (derived):** UC-001

| Operation | Description | Errors |
| --- | --- | --- |
| register(commandName: String, handler: CommandHandler): void | Asocia un nombre de comando del asunto a su manejador; Messaging entrega a ese manejador cada correo con ese comando y envía al remitente la respuesta que devuelve | DuplicateCommandException: el nombre ya está registrado |

Tipos que forman parte de la interfaz:

- `CommandHandler` (interface): `handle(command: MailCommand): CommandReply`. La implementan los subsistemas de negocio.
- `MailCommand` (record): `name: String`, `arguments: List<String>`, `senderEmail: String`, `attachments: List<MailAttachment>`.
- `CommandReply` (record): `subject: String`, `body: String`. Messaging lo envía al remitente.

### Notifier

- **Used by (derived):** Users
- **Use cases (derived):** UC-001

| Operation | Description | Errors |
| --- | --- | --- |
| notify(recipientEmail: String, subject: String, body: String): void | Envía un aviso por correo; si la transacción en curso no se confirma, no lo envía | None: un fallo de envío se registra en el log y no deshace el cambio |

## Required Interfaces

None.

## Open Questions

None.

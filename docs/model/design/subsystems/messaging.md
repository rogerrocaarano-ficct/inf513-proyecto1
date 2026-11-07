# Messaging

| | |
| --- | --- |
| Type | General |
| Comes from | [Unassigned](../../analysis/unassigned/package.md) (NotificacionUI), AM-002 |
| Status | Draft |
| Iteration | I1 |

## Responsibility

Sondea el buzón POP3, interpreta el comando del asunto, identifica y autoriza al remitente, y entrega el comando al manejador que un subsistema de negocio registró para él; envía por SMTP las respuestas y las notificaciones (AD-005, AD-006, AD-007). Es general: no conoce los comandos de negocio ni las cuentas de usuario. Los subsistemas de negocio implementan sus puertos `CommandHandler` y `SenderAuthenticator` (inversión de dependencias), así que dependen de Messaging y no al revés.

## Provided Interfaces

### CommandRegistry

- **Used by (derived):** GraduationProcesses, Users
- **Use cases (derived):** UC-001, UC-003, UC-004

| Operation | Description | Errors |
| --- | --- | --- |
| register(commandName: String, handler: CommandHandler): void | Asocia un nombre de comando del asunto, sin distinguir mayúsculas, a su manejador. Al arrancar se registran todos los beans `CommandHandler` | DuplicateCommandException: el nombre ya está registrado |
| find(commandName: String): Optional<CommandHandler> | Devuelve el manejador de un comando, sin distinguir mayúsculas | None |

Tipos que forman parte de la interfaz:

- `CommandHandler` (interface), que implementan los subsistemas de negocio:
  - `commandName(): String`: el nombre del comando en el asunto.
  - `allowedRoles(): Set<String>`: los roles que pueden ejecutarlo; vacío significa cualquier cuenta de usuario activa.
  - `handle(command: MailCommand, sender: Sender): CommandReply`: se ejecuta dentro de la transacción del comando. Para rechazar el comando lanza `CommandRejectedException`: la transacción se deshace y el remitente recibe el mensaje de la excepción. Cualquier otra excepción también la deshace, y el remitente recibe un mensaje de error genérico.
- `MailCommand` (record): `name: String`, `arguments: List<String>`, `senderEmail: String`, `attachments: List<MailAttachment>`.
- `MailAttachment` (record): `fileName: String`, `contentType: String`, `content: byte[]`. Solo se entregan las partes del correo con `Content-Disposition: attachment`; las partes inline (imágenes de una firma o pegadas en el cuerpo) se ignoran. `fileName` llega decodificado, tanto en RFC 2231 como en RFC 2047, y `contentType` sin parámetros. El comando reconoce los adjuntos por su nombre, sin que un argumento los nombre (NFR-IN-001).
- `CommandReply` (record): `body: String`. Messaging lo envía al remitente con el asunto `Re: <asunto original>`.
- `CommandRejectedException` (excepción): `message: String`, el motivo del rechazo que recibe el remitente.
- `Sender` (record): `accountId: UUID`, `email: String`, `roles: Set<String>`.

### SenderAuthenticator

Puerto que define Messaging y que implementa Users (D5 de `esqueleto-mail-app`).

- **Used by (derived):** None: lo llama Messaging y lo implementa Users
- **Use cases (derived):** UC-001, UC-003, UC-004

| Operation | Description | Errors |
| --- | --- | --- |
| authenticate(senderEmail: String): Optional<Sender> | Devuelve la cuenta de usuario activa con esa dirección de correo, sin distinguir mayúsculas, o vacío si no existe o está desactivada | None |

### Notifier

- **Used by (derived):** GraduationProcesses, Users
- **Use cases (derived):** UC-001, UC-003, UC-004

| Operation | Description | Errors |
| --- | --- | --- |
| notify(recipientEmail: String, subject: String, body: String): void | Envía un aviso por correo; si la transacción en curso no se confirma, no lo envía | None: un fallo de envío se registra en el log y no deshace el cambio |

## Required Interfaces

None.

## Open Questions

None.

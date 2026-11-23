# Design

## Context

Las llamadas a los servidores de correo están en dos adaptadores de `mail.infrastructure`:

- `Pop3EmailInbox`: `poll(maxMessages)` y `delete(mailboxId)` abren cada uno su propia conexión con `withInbox`. Un ciclo con N correos en estado final hace 1 + N conexiones. La lectura se conecta en cada ciclo, aunque el buzón esté vacío (cada 30 s, o cada 10 s en el perfil `local`).
- `SmtpEmailSender.send(to, subject, body, attachments)`: lo usan las respuestas de error de `InboxPollerService` (asunto `"Re: " + asunto original`, con los argumentos) y los notifiers de los casos de uso (`MailUserManagementNotifier`).

Hoy los dos adaptadores solo registran errores, advertencias y el aviso `Ignoring automatic email`. `InboxPollerService.storeNewMessages` tiene un WARN para el UIDL ya procesado que incluye el asunto guardado y el leído completos. El asunto de un comando lleva sus argumentos (cédula, teléfono, registro universitario), por eso este change no registra asuntos (ver `specs/mail-commands/spec.md`).

## Goals / Non-Goals

**Goals:**

- Ver en la consola cada interacción con POP3 y SMTP sin cambiar el comportamiento del canal.
- No registrar argumentos ni asuntos.

**Non-Goals:**

- Registrar el ciclo de vida del correo dentro del poller (validado, descartado, ejecutado). Queda fuera de este change.
- Logs estructurados (JSON), archivos de log, rotación o envío a un servicio externo. Se usa la salida por omisión de Spring Boot.
- Registrar los avisos de cada caso de uso. Su envío ya queda visible en la línea de SMTP.
- Reducir la cantidad de conexiones POP3 por ciclo.

## Decisions

### Las líneas van en los adaptadores, no en los puertos ni en el poller

`EmailInbox` y `EmailSender` son puertos de aplicación, y POP3/SMTP son detalles de sus adaptadores. Registrar en `Pop3EmailInbox` y `SmtpEmailSender` cubre todo el tráfico, también los avisos de los casos de uso, que no pasan por el poller. Se descartó registrar en `InboxPollerService` porque no ve los envíos de los notifiers ni las conexiones.

Cada clase usa su propio `org.slf4j.Logger`, como ya hacen las dos con `LoggerFactory.getLogger`. No se agregan dependencias.

### Líneas y niveles

| Dónde | Nivel | Mensaje |
| --- | --- | --- |
| `Pop3EmailInbox.withInbox`, antes de `store.connect` | DEBUG | `Connecting to POP3 server {host}:{port} as {username}` |
| `Pop3EmailInbox.poll`, al terminar | DEBUG si no hay mensajes; INFO si hay | `POP3 inbox has {total} messages; read {read}` |
| `Pop3EmailInbox.poll`, por cada correo leído | INFO | `Received email {uid} from {from}: action {action} ({n} attachments)` o `Received email {uid} from {from}: no valid action ({n} attachments)` |
| `Pop3EmailInbox.poll`, correo automático (ya existe) | INFO | `Ignoring automatic email {uid} from {from}` |
| `Pop3EmailInbox.delete`, por cada mensaje marcado | INFO | `Deleted message {uid} from the POP3 server` |
| `SmtpEmailSender.send`, después de `mailSender.send` | INFO | `Sent email to {to} ({n} attachments)` |
| `InboxPollerService.storeNewMessages`, UIDL ya procesado (ya existe) | WARN | igual que hoy, pero con `stored action {a}` y `read action {b}` en lugar de los asuntos |

- La conexión y el buzón vacío van en DEBUG para que, con el nivel por omisión (INFO), un ciclo sin correo no escriba nada. `application-local.properties` agrega `logging.level.bo.org.tecnoweb.cpms.mail.infrastructure=DEBUG`, así en local se ve cada conexión. En producción queda en INFO.
- La contraseña de POP3 nunca se registra. El usuario sí, porque es la dirección del sistema y ayuda a detectar una configuración equivocada.
- La línea de envío se escribe después de `mailSender.send`, para que solo aparezca si el envío se completó. Si falla, la excepción sigue subiendo como hoy y la registra quien llama.
- `delete` registra solo si encontró el mensaje. Si el UIDL ya no está en el servidor no escribe nada en INFO, porque eso no es un borrado.

### La acción se obtiene con `ReceivedEmail.parseAction()`

El adaptador POP3 y el poller ya tienen un `ReceivedEmail`. Se usa `parseAction()` y se atrapa `DomainValidationException` para escribir `no valid action`. Cada clase tiene un helper privado (`actionForLog(ReceivedEmail)`) de pocas líneas.

Alternativa descartada: agregar al dominio un método como `Optional<String> findAction()`. Evitaría repetir el helper, pero cambia el dominio solo para un log. Si más adelante aparece otro uso, se mueve al dominio.

### Sin asunto en la línea de SMTP

`SmtpEmailSender` no sabe si el asunto es la respuesta a un comando (`Re: usuarios.crearCuenta[…]`) o un aviso con asunto fijo, así que no lo registra. Se descartó recortar el asunto en `[` porque depende de la sintaxis del comando y el adaptador no debería conocerla.

### Tests

- `MailAdaptersTest` (GreenMail) agrega `@ExtendWith(OutputCaptureExtension.class)` a los casos de lectura, borrado, correo automático y envío, y comprueba la línea esperada. Un caso con `usuarios.crearCuenta[...]` comprueba que ningún argumento aparece en la salida de lectura ni en la de la respuesta.
- `InboxPollerServiceTest` actualiza el caso existente del UIDL reutilizado, que ya usa `OutputCaptureExtension`, para comprobar las acciones y que no aparecen los asuntos.
- El caso del buzón vacío comprueba que con el nivel INFO no se escribe ninguna línea del canal.

### Diferencias con el modelo

Trabajo compartido del canal `mail`, sin diseño en EA. Se sigue la arquitectura de referencia de `AGENTS.md`. La consulta del modelo está en pausa.

El change solo toca adaptadores de infraestructura, que no se modelan en EA, y una línea de log de `InboxPollerService`. No crea clases, operaciones públicas ni relaciones. Los helpers `actionForLog` son privados. No hay nada que llevar al modelo con `ea-drift`.

No se omite ningún record `Command` ni `Query`, porque el change no agrega comandos.

## Risks / Trade-offs

- [La dirección del remitente y la del destinatario quedan en los logs de producción] → Es el mínimo para seguir un correo. Los datos del comando no se registran. Si hiciera falta, se puede bajar esas líneas a DEBUG en producción con `logging.level`, sin cambiar código.
- [En local, DEBUG escribe dos líneas en cada ciclo (cada 10 s)] → Es lo que se pidió: ver cada llamada a POP3. Se apaga quitando la propiedad de `application-local.properties`.
- [El nivel DEBUG de `mail.infrastructure` también muestra los DEBUG que se agreguen después en ese paquete] → Por ahora no hay otros. Si molestan, se fija el nivel por clase.

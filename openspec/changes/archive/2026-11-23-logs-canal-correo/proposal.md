# Proposal

## Why

Hoy el canal de correo solo deja en la consola errores y advertencias. Cuando se procesa un correo sin problemas no queda ningún rastro: no se ve cuándo el sistema consulta el servidor POP3, qué correo entra ni qué correo sale por SMTP. Por eso, en el entorno local y en el servidor del docente (NFR-007), no se puede comprobar si un comando llegó o si se respondió sin abrir el webmail o la base de datos.

## What Changes

- El adaptador POP3 registra en la consola cada conexión al servidor, el resultado de cada lectura del buzón, cada correo que entra (UIDL, remitente, acción del comando y cantidad de adjuntos) y cada mensaje que borra del servidor.
- El adaptador SMTP registra cada correo enviado: destinatario y cantidad de adjuntos.
- Los registros no incluyen los argumentos del comando ni el asunto completo, ni de entrada ni de salida, porque contienen datos personales (cédula, teléfono, registro universitario). Del correo entrante solo se registra la acción.
- La advertencia que ya existe para un mensaje con un UIDL ya procesado deja de mostrar el asunto guardado y el leído completos, y pasa a mostrar solo la acción de cada uno.
- Las conexiones y las lecturas del buzón vacío se registran en nivel DEBUG para no repetir una línea en cada ciclo. El perfil `local` activa ese nivel para el canal de correo.
- Estos registros son técnicos y están dirigidos a quien opera el sistema. No son el "registro de cambios" de FR-052, que es un historial de negocio guardado en la base de datos.

Pertenece al trabajo compartido del canal de correo (`mail`), no a un caso de uso en particular.

Base en el modelo EA: ninguna. Son registros de los adaptadores de infraestructura, que no se modelan en EA (la consulta del modelo está en pausa).

## Capabilities

### New Capabilities

_Ninguna._

### Modified Capabilities

- `mail-commands`: agrega un requisito de trazabilidad en la consola del tráfico POP3 y SMTP, sin datos personales del comando.

## Impact

- `mail.infrastructure.Pop3EmailInbox` y `mail.infrastructure.SmtpEmailSender`: nuevas líneas de log.
- `mail.application.InboxPollerService`: la advertencia del UIDL ya procesado muestra acciones en lugar de asuntos.
- `src/main/resources/application-local.properties`: nivel DEBUG para `bo.org.tecnoweb.cpms.mail.infrastructure`.
- Tests de los dos adaptadores con captura de la salida.
- Sin cambios en el dominio, en la aplicación, en el esquema ni en las dependencias.

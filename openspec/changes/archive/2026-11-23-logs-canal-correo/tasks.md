# Tasks

## 1. Lectura POP3

- [x] 1.1 En `Pop3EmailInbox.withInbox`, registrar en DEBUG la conexión (host, puerto y usuario, sin contraseña). Verificar con un test de `MailAdaptersTest` con `OutputCaptureExtension` y DEBUG activado para la clase
- [x] 1.2 En `Pop3EmailInbox.poll`, registrar el total de mensajes y los leídos (DEBUG si el buzón está vacío, INFO si no). Verificar que con INFO un ciclo con el buzón vacío no escribe ninguna línea del canal
- [x] 1.3 En `Pop3EmailInbox.poll`, registrar en INFO cada correo leído (UIDL, remitente, acción o `no valid action`, adjuntos) con un helper privado `actionForLog`, y agregar el remitente al aviso del correo automático. Verificar con tests de comando válido, asunto `hola` y rebote
- [x] 1.4 Comprobar con un test que la lectura de `usuarios.crearCuenta[…]` con datos personales no escribe ninguno de sus argumentos

## 2. Borrado POP3

- [x] 2.1 En `Pop3EmailInbox.delete`, registrar en INFO el UIDL de cada mensaje marcado para borrar, y nada si no lo encuentra. Verificar con un test de `MailAdaptersTest`

## 3. Envío SMTP

- [x] 3.1 En `SmtpEmailSender.send`, registrar en INFO el destinatario y la cantidad de adjuntos después de `mailSender.send`, sin el asunto. Verificar con un test de `MailAdaptersTest` que envía un correo con asunto `Re: usuarios.crearCuenta[…]` y comprueba que la salida tiene el destinatario y no tiene el asunto ni los argumentos

## 4. Advertencia del UIDL ya procesado

- [x] 4.1 En `InboxPollerService.storeNewMessages`, cambiar el WARN del UIDL ya procesado para que muestre la acción guardada y la leída (helper privado `actionForLog`) en lugar de los asuntos. Verificar actualizando el test de `InboxPollerServiceTest` que ya captura esa salida: muestra las acciones y no los asuntos

## 5. Entorno local

- [x] 5.1 Agregar `logging.level.bo.org.tecnoweb.cpms.mail.infrastructure=DEBUG` a `application-local.properties` y mencionar en `README.md`, sección "Entorno de desarrollo", qué muestra la consola del canal de correo. Verificar con `./gradlew bootRun`: con un correo enviado desde Roundcube, la consola muestra la conexión, la entrada, el borrado y el envío de la respuesta

## 6. Verificación

- [x] 6.1 Revisar la implementación contra la arquitectura de referencia de `AGENTS.md`: logs solo en adaptadores y en el poller, sin cambios en el dominio ni en los puertos, sin dependencias nuevas, Javadoc en inglés en lo que se toque. Revisar también que ninguna línea nueva registre asuntos, argumentos ni la contraseña
- [x] 6.2 Ejecutar `./gradlew build` y verificar que todos los tests pasan

## Workflow follow-up

- Archivar el change con `/opsx:archive` cuando las tareas estén hechas y los tests pasen.

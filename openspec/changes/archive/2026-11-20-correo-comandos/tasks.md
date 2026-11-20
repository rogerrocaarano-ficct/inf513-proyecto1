# Tasks

## 1. Estructura y dependencias

- [x] 1.1 Agregar `com.icegreen:greenmail` (y su integración con JUnit 5) como `testImplementation` en `build.gradle.kts`; verificar que `./gradlew dependencies --configuration testRuntimeClasspath` la resuelve
- [x] 1.2 Crear los paquetes `bo.org.tecnoweb.cpms.common` y `bo.org.tecnoweb.cpms.mail.{domain,application,infrastructure}` (D1) y registrar la estructura acordada en la sección Stack de `AGENTS.md`; verificar que `AGENTS.md` describe los paquetes que existen
- [x] 1.3 Agregar a `GLOSSARY.md` el término "Correo recibido" (`ReceivedEmail`) y completar el término adoptado de "Comando" y "Servicio de correo"; verificar que las filas conservan las tres columnas

## 2. Núcleo común

- [x] 2.1 Implementar `common.AggregateRoot`, `DomainEvent`, `DomainException` (con `errorCode`) y `DomainValidationException` (con `getErrors()`) según `Design/common`; verificar con pruebas unitarias que `DomainValidationException` conserva un error por campo y su `errorCode`

## 3. Dominio de correo

- [x] 3.1 Implementar `ReceivedEmailState` (`RECEIVED`, `VALIDATED`, `PROCESSED`, `DISCARDED`) y `File` (builder, `rehydrate`, getters); verificar que compila sin anotaciones JPA
- [x] 3.2 Implementar `ReceivedEmail` con builder, `rehydrate`, getters, `markAsProcessed`, `markAsDiscarded` y las transiciones de estado de las notas de EA; verificar con pruebas unitarias que una transición desde un estado final se rechaza
- [x] 3.3 Implementar `parseAction()` y `parseArgs()` con la expresión de D4; verificar con pruebas unitarias todos los escenarios de "Sintaxis del comando en el asunto" y "Argumentos del comando" de la spec (mayúsculas, prefijo `RE: Fwd:`, texto después de `]`, espacios, `cmd[]`, argumento vacío)

## 4. Puertos de aplicación

- [x] 4.1 Declarar las interfaces `EmailCommandHandler` (`actionKey`, `handle`, `requiredArgs`, `requiredFiles`), `EmailInbox` (`poll`, `delete`), `EmailSender` (`send`) y `ReceivedEmailRepository` (`store`, `getNotProcessed`, `findByMailboxId`) con las firmas de EA; verificar que `./gradlew compileJava` pasa

## 5. Persistencia

- [x] 5.1 Escribir la migración `db/migration/V1__received_email.sql` con `received_email` (`mailbox_id` único) y `received_email_attachment` (D2); verificar que Flyway la aplica en el arranque de las pruebas con Testcontainers
- [x] 5.2 Implementar en `mail.infrastructure` las entidades JPA, el mapeador hacia `ReceivedEmail`/`File` y el adaptador de `ReceivedEmailRepository`; verificar con pruebas de integración que `store` no duplica un `mailboxId`, que `findByMailboxId` devuelve el correo o ninguno y que `getNotProcessed` devuelve solo `RECEIVED` y `VALIDATED` en orden de llegada

## 6. Servicio de sondeo

- [x] 6.1 Implementar en `InboxPollerService` el mapa de handlers por `actionKey()` en minúsculas, con falla al iniciar si hay claves repetidas (D4); verificar con una prueba unitaria que dos handlers con la misma acción impiden crear el servicio
- [x] 6.2 Implementar los pasos 1, 2 y 6 de `pollAndProcess`: leer el lote, guardar los nuevos, borrar del servidor los ya finales (`findByMailboxId`, D6) y borrar los que terminan en estado final; verificar con dobles de los puertos los escenarios de "Lectura del buzón por lotes", "Un mensaje se procesa una sola vez" y "Borrado del mensaje en el servidor"
- [x] 6.3 Implementar la validación de `dispatch` (forma, handler, `requiredArgs`, `requiredFiles`) con `markAsDiscarded` y respuesta por `EmailSender`; verificar con dobles los escenarios de "Comando desconocido" (incluye la lista de comandos) y "Cantidad de argumentos y adjuntos"
- [x] 6.4 Implementar la ejecución con `TransactionTemplate` (transacción A: `handle` + `markAsProcessed`; transacción B ante excepción) y las respuestas de error de D3; verificar los escenarios de "Ejecución del comando", "Respuesta de errores de ejecución" y "Respuestas de éxito a cargo del caso de uso"

## 7. Adaptadores de correo

- [x] 7.1 Implementar el adaptador POP3 de `EmailInbox` con UIDL, decodificación del asunto, cuerpo `text/plain` y adjuntos (D5); verificar con GreenMail que `poll` no borra y que `delete` borra solo el mensaje con ese UIDL
- [x] 7.2 Agregar al adaptador POP3 el filtro de correos automáticos, que los borra en la misma sesión sin devolverlos (D5); verificar con GreenMail los escenarios de "Correos automáticos" (rebote, `Auto-Submitted`, remitente igual a la dirección del sistema)
- [x] 7.3 Implementar el adaptador SMTP de `EmailSender` con `JavaMailSender`, remitente configurado y asunto `Re: <asunto original>` en las respuestas (D7); verificar con GreenMail que el correo llega con ese remitente, asunto y cuerpo

## 8. Planificación y configuración

- [x] 8.1 Agregar las propiedades de D8 a `application.properties` (credenciales por variables de entorno) y el `@Scheduled` con `cpms.mail.polling-enabled`; verificar que las pruebas arrancan con el sondeo desactivado y que con el sondeo activado se ejecuta un ciclo según el intervalo configurado

## 9. Verificación integrada

- [x] 9.1 Escribir una prueba de extremo a extremo con GreenMail y PostgreSQL y un `EmailCommandHandler` definido solo en las fuentes de prueba: comando válido, comando desconocido, cantidad incorrecta, excepción de dominio y reinicio con un correo en `VALIDATED`; verificar que `./gradlew build` pasa
- [x] 9.2 Revisar la implementación contra los elementos de EA citados en el design (`Design/Mail/domain`, `Design/Mail/application`, `Design/common`, diagrama "Mail"): nombres de clases, interfaces, operaciones y atributos; verificar con la skill `ea-drift mail` que las únicas diferencias son las registradas en "Diferencias con el modelo"

## Workflow follow-up

- Archivar el change cuando las tareas estén hechas y las pruebas pasen (`/opsx:archive`).
- Agregar a mano en EA el parámetro `mailboxId: String` de `ReceivedEmailRepository.findByMailboxId`.

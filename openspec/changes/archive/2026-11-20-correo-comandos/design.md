# Design

## Context

El proyecto solo tiene el andamiaje (`CpmsApplication`, pruebas con Testcontainers PostgreSQL) y ya incluye los starters de mail, JPA y Flyway. El diseño sigue el modelo de EA, diagrama de clases `Design/Mail` "Mail":

- `Design/Mail/domain`: `ReceivedEmail` (agregado), `File`, `ReceivedEmailState`.
- `Design/Mail/application`: `InboxPollerService`, `EmailCommandHandler`, `EmailInbox`, `EmailSender`, `ReceivedEmailRepository`.
- `Design/common`: `AggregateRoot`, `DomainEvent`, `DomainException`, `DomainValidationException`.

Las notas de esos elementos en EA describen el ciclo de vida de los estados, los pasos de `pollAndProcess` y las reglas de parseo. Este documento no las repite: explica cómo se implementan y por qué. Los requisitos están en `specs/mail-commands/spec.md`.

Restricciones: servidor `mail.tecnoweb.org.bo`, POP3 por el puerto 110 y SMTP por el 25, sin cifrado (NFR-007); en EA solo se modelan `domain` y `application`, y la infraestructura queda en el código.

## Goals / Non-Goals

**Goals:**
- Canal de entrada y salida por correo reutilizable por todos los casos de uso, sin que `mail` dependa de ninguno.
- Procesamiento sin duplicados ante reinicios, apoyado en el UIDL y en las transacciones.
- Pruebas automáticas del ciclo completo (leer, validar, ejecutar, responder, borrar) sin depender del servidor real.

**Non-Goals:**
- Comandos de casos de uso (empiezan en `uc-001-crear-cuenta`).
- `FileStorage`: los adjuntos se guardan con el correo; un almacenamiento aparte llega cuando un caso de uso reciba archivos (UC-002, UC-003).
- Autenticación del remitente más allá de su dirección `From`, y autorización: las decide cada caso de uso.
- Recuperar el envío de una respuesta que falló después del commit.

## Decisions

### D1. Estructura de paquetes

```
bo.org.tecnoweb.cpms
 +-- common/                 AggregateRoot, DomainEvent, DomainException, DomainValidationException
 +-- mail/
      +-- domain/            ReceivedEmail, File, ReceivedEmailState
      +-- application/       InboxPollerService, EmailCommandHandler, EmailInbox, EmailSender,
      |                      ReceivedEmailRepository
      +-- infrastructure/    adaptadores POP3, SMTP y JPA, planificador, configuración
```

Acordada con el usuario y registrada en `AGENTS.md`. Cada paquete de caso de uso (`users`, …) sigue la misma forma. `application` define los puertos; `infrastructure` los implementa. Alternativa descartada: paquetes por capa en la raíz (`domain/`, `application/`), porque mezcla los subsistemas que EA separa.

### D2. Dominio sin anotaciones de persistencia

`ReceivedEmail` y `File` son clases Java sin JPA, construidas con su builder (`builder()`, `with*`) y reconstruidas con `rehydrate()`, como indica el modelo. La infraestructura tiene entidades JPA propias y un mapeador. Alternativa: anotar el dominio con JPA; se descarta porque acopla el modelo a la base y contradice `rehydrate()`.

Persistencia (migración Flyway `V1__received_email.sql`):
- `received_email`: `id uuid` (PK), `mailbox_id varchar` **único**, `from_address`, `subject`, `body text`, `state varchar`, `received_at timestamptz`.
- `received_email_attachment`: `id uuid` (PK), `received_email_id` (FK), `name`, `data bytea`.

La restricción única sobre `mailbox_id` es la que impide guardar dos veces el mismo mensaje (`ReceivedEmailRepository.store`).

### D3. Transacciones programáticas

`InboxPollerService.dispatch` es privado y necesita dos transacciones distintas (ejecución y registro de una falla), así que usa `TransactionTemplate` en lugar de `@Transactional`:

1. Transacción A: `handler.handle(email)` + `email.markAsProcessed()` + guardado. Si confirma, el correo y los cambios del caso de uso quedan juntos (spec: Ejecución del comando).
2. Si A lanza una excepción: rollback; transacción B: `markAsProcessed()` + guardado, y después se responde el error por `EmailSender`.

Alternativa: `@Transactional` en un bean aparte; se descarta porque agrega una clase que el modelo no tiene solo para atravesar el proxy.

Los eventos de éxito que publican los casos de uso se envían después del commit de A (cada caso de uso lo resuelve en su infraestructura, ver la nota de `UserManagementEventBus`).

### D4. Validación y despacho

- `InboxPollerService` recibe todos los beans `EmailCommandHandler` y arma un mapa por `actionKey()` en minúsculas. Una clave repetida lanza una excepción al iniciar, y la aplicación no arranca.
- `ReceivedEmail.parseAction()` y `parseArgs()` usan la expresión `([A-Za-z0-9_.]+)\s*\[(.*)\]\s*$` sobre el asunto. Si el asunto no la cumple, `parseAction()` lo informa y el correo queda `DISCARDED`.
- El orden de la validación es: forma del asunto → existencia del handler → `requiredArgs()` → `requiredFiles()`. El primer fallo decide el mensaje de respuesta. La respuesta de "comando desconocido" lista las claves del mapa.

### D5. Adaptador POP3 (`EmailInbox`)

Jakarta Mail (incluido en el starter de mail), protocolo `pop3`. El identificador es el UIDL (`POP3Folder.getUID`), nunca el número de mensaje de la sesión.

- `poll(maxMessages)` abre la carpeta, lee los primeros `maxMessages`, los convierte en `ReceivedEmail` (asunto decodificado, primera parte `text/plain` como `body`, adjuntos como `File`) y cierra sin borrar.
- Los correos automáticos (spec: Correos automáticos) se reconocen por cabeceras: `Auto-Submitted` distinto de `no`, `Precedence: bulk|junk|list`, remitente vacío o `MAILER-DAEMON`, o remitente igual a la dirección del sistema. El adaptador no los devuelve y **los borra en la misma sesión**: si quedaran en el servidor ocuparían el lote en cada ciclo y bloquearían el buzón.
- `delete(mailboxId)` abre la carpeta en lectura y escritura, busca el mensaje por UIDL, lo marca como borrado y cierra con `expunge`. En POP3 el borrado se hace efectivo al cerrar la sesión.

### D6. Mensajes leídos de nuevo y bloqueo del lote

Un mensaje cuyo correo ya está en un estado final pero sigue en el servidor (la aplicación se detuvo antes de borrarlo) volvería en cada `poll` y ocuparía un lugar del lote para siempre. Por eso, en el paso 2 de `pollAndProcess`, el servicio consulta `ReceivedEmailRepository.findByMailboxId` antes de `store`: si el correo no existe lo guarda; si existe en un estado final, llama a `EmailInbox.delete`; si existe en `RECEIVED` o `VALIDATED`, no hace nada, porque `getNotProcessed` lo retoma.

### D7. Adaptador SMTP (`EmailSender`)

`JavaMailSender` de Spring. Remitente: la dirección del sistema configurada. Las respuestas de error usan el asunto `Re: <asunto original>`, para que el usuario las relacione con su comando. Si el envío falla, se registra en el log y el correo conserva su estado: no se reintenta.

### D8. Planificación y configuración

`@Scheduled(fixedDelayString = "${cpms.mail.poll-interval}")` con `@EnableScheduling`. `fixedDelay` evita que dos ciclos se superpongan. Propiedades nuevas en `application.properties`:

| Propiedad | Valor por defecto |
| --- | --- |
| `cpms.mail.polling-enabled` | `true` (las pruebas lo desactivan y llaman a `pollAndProcess` directamente) |
| `cpms.mail.poll-interval` | `30s` (se ajusta al tratar RSK-003) |
| `cpms.mail.batch-size` | `10` |
| `cpms.mail.address` | dirección del sistema |
| `cpms.mail.pop3.host`, `.port`, `.username`, `.password` | host por `CPMS_MAIL_POP3_HOST` (por defecto `localhost`; en producción, `mail.tecnoweb.org.bo`), `110`, credenciales por variables de entorno |
| `spring.mail.host`, `spring.mail.port` | host por `CPMS_MAIL_SMTP_HOST` (por defecto `localhost`; en producción, `mail.tecnoweb.org.bo`), `25` |

Las credenciales nunca se escriben en el repositorio.

### D9. Pruebas

- Unitarias de `ReceivedEmail`: los escenarios de sintaxis y argumentos de la spec.
- De integración con **GreenMail** (POP3 y SMTP embebidos, dependencia nueva solo de prueba) más PostgreSQL de Testcontainers: un handler definido en las fuentes de prueba recorre los escenarios de la spec (comando válido, desconocido, cantidad incorrecta, excepción de dominio, error inesperado, correo automático, mensaje leído de nuevo, reinicio con correo en `VALIDATED`).

Alternativa: Mailpit en Testcontainers; se descarta porque no ofrece POP3.

## Risks / Trade-offs

- [El remitente se identifica solo por la cabecera `From`, que puede falsificarse] → Aceptado para la prueba de concepto; cada caso de uso autoriza por la cuenta asociada a esa dirección.
- [El SMTP del servidor rechaza los envíos de la IP de producción (RSK-001)] → Probar la cadena completa contra el servidor real antes de la presentación; el fallo de envío queda en el log.
- [Sondeo demasiado frecuente (RSK-003)] → Intervalo configurable sin recompilar.
- [Adjuntos de hasta 25 MB (NFR-005) se cargan en memoria y en `bytea`] → Lote pequeño por defecto; un almacenamiento aparte llegará con `FileStorage`.
- [Falla el envío de la respuesta después del commit] → El comando ya se ejecutó; el usuario no recibe respuesta. Se registra en el log y no se reintenta (Non-Goal).
- [`;` y `]` no se pueden escapar dentro de un argumento (RSK-004)] → Limitación conocida; los mensajes de error indican la sintaxis.

## Migration Plan

Primera versión: no hay datos que migrar. Flyway crea las tablas al arrancar. Para revertir, se despliega la versión anterior y se eliminan las tablas `received_email_attachment` y `received_email`.

## Diferencias con el modelo

1. **`FileStorage`**: el modelo lo asocia a `InboxPollerService` (atributo `files`). Queda fuera de este change: el atributo no se implementa y los adjuntos se guardan con el correo.
2. **Parámetro de `findByMailboxId`**: la operación se agregó en EA a pedido del usuario, pero el MCP no crea parámetros; `mailboxId: String` figura solo en su nota hasta que se agregue a mano.
3. **Análisis sin correspondencia en el diseño**: la entidad de análisis `ComandoEjecutado` (`Análisis/Subsistemas funcionales/Administración`) no tiene clase de diseño. Este change no registra los comandos ejecutados más allá del estado del correo.
4. **Filtro de correos automáticos y conversión de mensajes**: viven en el adaptador POP3 (infraestructura), que no se modela; el modelo los menciona solo en las notas de `InboxPollerService` y `EmailInbox`.
5. **Agregados durante la implementación** (detectados con `ea-drift mail`; pendientes de pasar a EA):
   - `ReceivedEmailState.isFinal(): Boolean`, usado por el servicio y el repositorio para decidir el borrado y la recuperación.
   - `ReceivedEmail.events: DomainEvent` (0..*), que respalda `pullEvents()`, como `User.events` en `Design/Users/domain`.
   - Constantes `ReceivedEmail.SUBJECT_FIELD` y `ReceivedEmail.SYNTAX_HELP` (texto del error de sintaxis) y `DomainValidationException.ERROR_CODE`.
   - `File.rehydrate()` devuelve `File` (en el modelo, `void`), igual que `ReceivedEmail.rehydrate()`.
   - `InboxPollerService.transactions: TransactionOperations` (D3), tipo de Spring.

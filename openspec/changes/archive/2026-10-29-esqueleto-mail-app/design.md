# Design

## Context

El modelo de diseño ya fija la arquitectura (`docs/model/design/architecture.md`): una sola aplicación backend, `mail-app` (AD-001), hexagonal (AD-002), con Java 25 y Spring Boot 4 (AD-003), PostgreSQL con Spring Data JPA (AD-004), sondeo POP3 y envío SMTP (AD-005), analizador de comandos propio (AD-006), autorización propia por remitente (AD-007), auditoría propia (AD-008) y claves UUID versión 7 (AD-010). Los subsistemas y sus interfaces están en `docs/model/design/subsystems/`, y las tablas en `docs/model/design/database.md`. Este diseño solo decide lo que el modelo deja abierto para construir el esqueleto. La motivación está en `proposal.md`.

## Goals / Non-Goals

**Goals:**

- Un circuito de correo completo y probado: un correo con `Ping()` produce una respuesta, localmente con Mailpit y en las pruebas con GreenMail.
- La estructura de código que seguirán todos los casos de uso: un paquete por subsistema y las capas hexagonales dentro de cada uno.
- Un stack de pruebas de desarrollo que se levanta con `docker compose up`.

**Non-Goals:**

- Los comandos de gestión de usuarios de UC-001 y el subsistema GraduationProcesses.
- El despliegue en el servidor de la presentación (laboratorio o servidor de contingencia), que sigue como pregunta abierta en `architecture.md`.
- Cifrado de las conexiones de correo: CON-006 impone POP3 y SMTP sin cifrado.

## Decisions

### D1. Build con Gradle (Kotlin DSL)

Gradle con `build.gradle.kts`, el wrapper `gradlew` y el toolchain de Java 25. Elegido por el equipo. Alternativa: Maven, más común en Spring Boot pero más verboso.

### D2. Estructura del código

```
mail-app/
  build.gradle.kts, settings.gradle.kts, gradlew, Dockerfile
  src/main/java/grupo03sc/mailapp/
    MailAppApplication.java
    messaging/  adapter/in (sondeo POP3), adapter/out (SMTP),
                application (registro y despacho de comandos, puertos), domain (MailCommand, analizador, filtro)
    audit/      application (ChangeLog, repositorio), domain (ChangeLogEntry)
    users/      adapter/in (comando Ping, autenticador de remitentes),
                application (administrador inicial, repositorio), domain (UserAccount, Role, AccountStatus)
  src/main/resources/application.properties, db/migration/V*.sql
  src/test/java/... (misma estructura)
```

Un paquete por subsistema de `subsystems.md` y, dentro, las capas de AD-002. Un subsistema usa a otro solo a través de las interfaces de su paquete `application`. Los repositorios son puertos declarados en `application` que Spring Data JPA implementa, así que no hace falta un paquete `adapter/out` para la persistencia; las entidades JPA están en `domain`. El paquete base `grupo03sc.mailapp` toma el nombre del grupo de la base de datos de CON-007.

### D3. Migraciones con Flyway (nueva AD-011)

Flyway, con migraciones SQL versionadas en `db/migration` que se aplican al arrancar. Es lo más común en Spring Boot y las migraciones son SQL legible. Alternativa: Liquibase, útil para varios motores o para deshacer migraciones, que este proyecto no necesita. Se registra como AD-011 en `architecture.md`.

### D4. Procesamiento de cada correo

Una tarea `@Scheduled(fixedDelay)` con un solo hilo abre el buzón POP3 (Jakarta Mail), y procesa los mensajes uno por uno:

```
por cada mensaje del buzón:
  no es para nosotros / lo enviamos nosotros  --> dejarlo
  respuesta automática                         --> borrarlo
  ya procesado (Message-ID registrado)         --> borrarlo
  si no:
    [transacción] autenticar remitente --> autorizar roles --> ejecutar manejador
                  --> registrar Message-ID como procesado
    después de confirmar: enviar respuesta y avisos; borrar el mensaje
```

- **Idempotencia:** el `Message-ID` de cada correo procesado se guarda en la tabla `processed_mail_messages` dentro de la misma transacción. Si la aplicación se cae después de confirmar y antes de borrar el mensaje, el siguiente sondeo lo reconoce y no lo ejecuta dos veces. Alternativa: confiar solo en el borrado POP3, que puede ejecutar un comando dos veces.
- **Un hilo:** los comandos se procesan en orden de llegada. Eso hace aceptable el riesgo de concurrencia que la realización de UC-001 ya asumió.
- **Errores:** un error interno deshace la transacción y responde un mensaje genérico. El detalle queda en el log.

### D5. Autorización con inversión de dependencias

Messaging es un subsistema general y no puede depender de Users. Por eso Messaging define dos puertos:

- `SenderAuthenticator`: recibe la dirección del remitente y devuelve un `Sender` (identificador, dirección y nombres de roles) si hay una cuenta de usuario activa. Lo implementa Users.
- `CommandHandler`: declara sus roles permitidos como nombres (`Set<String>`, vacío significa cualquier cuenta activa), y su operación `handle(command: MailCommand, sender: Sender): CommandReply`.

Messaging autentica y autoriza una sola vez para todos los comandos, en lugar de que cada manejador lo haga. Esto cambia la interfaz `CommandHandler` de `docs/model/design/subsystems/messaging.md` y simplifica la realización de diseño de UC-001, donde `authenticate` deja de estar en el manejador. Este cambio actualiza `messaging.md`; la realización de UC-001 se ajusta en su propio cambio.

### D6. Avisos después de confirmar

`Notifier.notify` registra el aviso en la transacción en curso con `TransactionSynchronization` y lo envía en `afterCommit`. Sin transacción activa, lo envía de inmediato. Un fallo de SMTP se registra en el log y no se reintenta. Alternativa: una tabla de salida (outbox) con reintentos, más robusta pero innecesaria para la prueba de concepto.

### D7. Registro de cambios inalterable

`ChangeLog.record` inserta filas en `change_log_entries` en la transacción en curso. Su repositorio no expone actualización ni borrado, y la migración crea un trigger que rechaza `UPDATE` y `DELETE` en esa tabla. El trigger es necesario porque la aplicación usa un solo usuario de base de datos (CON-007), así que no se pueden quitar permisos.

### D8. Administrador inicial

Un `ApplicationRunner` de Users comprueba al arrancar si existe una cuenta activa con `ADMINISTRATOR`. Si no existe, la crea con `app.initial-admin.name` y `app.initial-admin.email`, y registra la creación en el registro de cambios con el identificador de la propia cuenta como autor. Si faltan esos datos, el arranque falla con un mensaje claro.

### D9. Configuración por variables de entorno

`application.properties` lee todo de variables de entorno, con valores por defecto para el stack local:

| Variable | Uso |
| --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `MAIL_ADDRESS` | Dirección del sistema: filtro de destinatarios y remitente de las respuestas |
| `POP3_HOST`, `POP3_PORT`, `POP3_USER`, `POP3_PASSWORD` | Buzón |
| `SMTP_HOST`, `SMTP_PORT` | Envío |
| `POLL_INTERVAL` | Intervalo de sondeo (por defecto `30s`) |
| `INITIAL_ADMIN_NAME`, `INITIAL_ADMIN_EMAIL` | Administrador inicial |

Para apuntar a `mail.tecnoweb.org.bo` y `db.tecnoweb.org.bo` basta con cambiar estas variables (puertos 110, 25 y 5432).

### D10. Docker Compose del servidor de pruebas

`docker-compose.yml` en la raíz del repositorio, con tres servicios:

| Servicio | Imagen | Puertos publicados | Notas |
| --- | --- | --- | --- |
| `postgres` | `postgres:17` | 5432 | Volumen `pgdata`; base, usuario y contraseña desde `.env` |
| `mailpit` | `axllent/mailpit` | 8025 (web) | SMTP 1025 y POP3 1110 dentro de la red; POP3 con usuario y contraseña de `.env` |
| `mail-app` | construida desde `mail-app/Dockerfile` | — | Depende de `postgres` (healthcheck) y de `mailpit` |

El `Dockerfile` es de dos etapas: build con Gradle sobre JDK 25 y ejecución sobre `eclipse-temurin:25-jre`. `.env.example` lista todas las variables sin secretos reales, y `.env` queda en `.gitignore`. Para enviar un comando de prueba se usa la interfaz web de Mailpit o cualquier cliente SMTP apuntado al puerto 1025.

### D11. Pruebas

- **Unitarias:** analizador de comandos, filtros de correo y reglas de autorización, sin Spring.
- **Integración:** `@SpringBootTest` con GreenMail (POP3 y SMTP en memoria) y Testcontainers (PostgreSQL 17). La prueba de punta a punta envía `Ping()` a GreenMail y espera la respuesta `Pong`.
- Se ejecutan con `./gradlew test`. Las de Testcontainers necesitan Docker.

## Risks / Trade-offs

- [Spring Boot 4 o alguna librería no funciona con Java 25] → La primera tarea compila y arranca un proyecto vacío con esas versiones. Si falla, se vuelve a decidir AD-003 antes de seguir.
- [Mailpit guarda todos los correos en un solo buzón, a diferencia del servidor del docente] → El filtro por destinatario y remitente evita que el sistema procese sus propias respuestas. Las pruebas con GreenMail usan buzones separados, como el servidor real.
- [El servidor SMTP del docente valida la IP de origen (CON-006)] → Solo afecta las pruebas contra los servidores reales. El stack local no lo necesita.
- [Credenciales de correo y de base de datos en texto plano] → Solo en `.env`, fuera de git, y CON-006 ya impone conexiones sin cifrado.
- [Avisos perdidos si el SMTP falla, porque no hay reintentos] → Aceptado para la prueba de concepto. El fallo queda en el log.

## Migration Plan

1. En el servidor de pruebas: copiar `.env.example` a `.env`, completarlo y ejecutar `docker compose up -d --build`.
2. Flyway crea las tablas al primer arranque, y `mail-app` crea el administrador inicial.
3. Rollback: `docker compose down` (con `-v` para borrar también los datos).

## Open Questions

- ¿Qué versión de PostgreSQL tiene `db.tecnoweb.org.bo`? El stack local usa la 17. Si el servidor tiene una versión anterior, las migraciones deben evitar funciones nuevas. No cambia las tareas.

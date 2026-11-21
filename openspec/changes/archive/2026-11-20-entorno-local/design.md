# Design

## Context

Ver `proposal.md` (Why). Estado actual:

- `./gradlew bootTestRun` arranca `TestCpmsApplication` con un PostgreSQL de Testcontainers que se pierde al detenerse, y carga `src/test/resources/config/application.properties`, que desactiva el sondeo.
- `application.properties` toma los servidores de correo de `CPMS_MAIL_POP3_HOST` y `CPMS_MAIL_SMTP_HOST` (por defecto `localhost`) y las credenciales de variables de entorno; los puertos son 110 y 25 (NFR-007).
- Las pruebas ya usan GreenMail como servidor POP3/SMTP embebido.

## Goals / Non-Goals

**Goals:**
- Un solo comando (`./gradlew bootRun`) levanta la base, el servidor de correo y el webmail, y arranca la aplicación conectada a ellos.
- El desarrollador envía comandos y lee las respuestas desde el navegador, con un buzón por cada dirección.
- Los datos de la base se conservan entre arranques.

**Non-Goals:**
- Reemplazar `bootTestRun` o las pruebas con Testcontainers: siguen como están.
- Persistir los correos de GreenMail cuando se reinicia su contenedor.
- Cualquier configuración de producción.

## Decisions

### D1. Servicios de `compose.yaml`

```
 navegador --> roundcube :8025 --IMAP / SMTP (red interna)--> greenmail <--POP3 1110 / SMTP 2525-- CPMS (bootRun)
                                                                                                   |
                                                                              postgres (volumen) <-+
```

| Servicio | Imagen | Puertos en el host | Configuración |
| --- | --- | --- | --- |
| `postgres` | `postgres:17` | asignado por Docker | Base `cpms`, volumen con nombre para los datos |
| `greenmail` | `greenmail/standalone:2.1.15` | 2525 (SMTP), 1110 (POP3), 1143 (IMAP), hacia 3025, 3110 y 3143 del contenedor | `-Dgreenmail.setup.test.all -Dgreenmail.hostname=0.0.0.0 -Dgreenmail.auth.disabled -Dgreenmail.users=sistema:secret@cpms.local -Dgreenmail.users.login=email` |
| `roundcube` | `roundcube/roundcubemail:1.6.19-apache` | 8025 | IMAP y SMTP apuntan a `greenmail` por la red interna; base SQLite propia |

- GreenMail ofrece un buzón por dirección y crea los usuarios al recibir correo. Con la autenticación desactivada, en Roundcube se entra con cualquier dirección (`ana@cpms.local`, `director@cpms.local`…) y cualquier contraseña. El buzón del sistema, `sistema@cpms.local`, tiene el usuario `sistema@cpms.local` y la contraseña `secret`.
- PostgreSQL publica su puerto en uno libre que elige Docker: `spring-boot-docker-compose` lo lee y configura la conexión, así que no choca con un PostgreSQL instalado.
- En el host, GreenMail usa 2525, 1110 y 1143, sin privilegios: no chocan con 25 ni 110, ni con 3025, 3110 y 3143, que usa el GreenMail embebido de las pruebas. Así `./gradlew build` pasa con el entorno local levantado (al implementar, los puertos 3025/3110 publicados hacían fallar 17 pruebas).
- Las versiones son fijas. GreenMail es la misma versión que usan las pruebas.

Alternativas descartadas:
- **Mailpit**: tiene webmail y POP3, pero guarda todo en un solo buzón; el sondeo leería las respuestas del sistema, las descartaría por venir de su propia dirección y las borraría, y el desarrollador no las vería.
- **docker-mailserver**: un servidor real, pero necesita dominio, certificados y más configuración de la que justifica un entorno local.

### D2. Arranque con `spring-boot-docker-compose`

Dependencia `org.springframework.boot:spring-boot-docker-compose` como `developmentOnly`: solo está en el classpath de `bootRun` (y de `bootTestRun`), nunca en el JAR de producción.

- Al iniciar, levanta `compose.yaml` y crea la conexión a PostgreSQL por reconocimiento de la imagen. GreenMail y Roundcube no tienen conexión de servicio y se ignoran.
- `spring.docker.compose.lifecycle-management=start-only` en el perfil `local`: los contenedores siguen corriendo al detener la aplicación. Arrancar de nuevo es más rápido y los correos de GreenMail no se pierden entre reinicios de la aplicación.
- Roundcube no expone nada que la aplicación use, así que se desactiva su chequeo de disponibilidad (`org.springframework.boot.readiness-check.tcp.disable`) para no demorar el arranque.

Alternativa: `docker compose up` a mano antes de `bootRun`; se descarta porque agrega un paso que es fácil olvidar.

### D3. Perfil `local`

`bootRun` activa el perfil `local` (`--spring.profiles.active=local` en la tarea de Gradle). `application-local.properties`:

| Propiedad | Valor |
| --- | --- |
| `cpms.mail.address` | `sistema@cpms.local` |
| `cpms.mail.pop3.host`, `.port` | `localhost`, `1110` |
| `cpms.mail.pop3.username`, `.password` | `sistema@cpms.local`, `secret` |
| `spring.mail.host`, `spring.mail.port` | `localhost`, `2525` |
| `cpms.mail.poll-interval` | `10s` |
| `spring.docker.compose.lifecycle-management` | `start-only` |

Las credenciales del perfil son de prueba y solo sirven para el GreenMail local; las de producción siguen en variables de entorno. Alternativa: variables de entorno o un `.env` también en local; se descarta porque obliga a cada desarrollador a definir los mismos valores fijos.

### D4. Convivencia con `bootTestRun` y las pruebas

- Las pruebas no levantan `compose.yaml`: Spring Boot omite el soporte de Docker Compose en pruebas.
- `bootTestRun` sí tiene la dependencia en su classpath y ya obtiene la base de Testcontainers. Para que no levante además `compose.yaml`, `src/test/resources/config/application.properties` agrega `spring.docker.compose.enabled=false`.

### D5. Documentación

Una sección "Entorno de desarrollo" en `README.md`, en español, para el equipo:

1. Requisitos: Git, JDK 25, Docker y un navegador; puertos que deben estar libres.
2. Comprobar la instalación con `./gradlew build`.
3. Levantar el stack de pruebas manuales con `./gradlew bootRun` (tabla de servicios y direcciones).
4. Probar un comando desde Roundcube.
5. Detener y reiniciar: `docker compose down`, `docker compose down -v`, y la advertencia sobre los UIDL de GreenMail (D6).
6. Problemas frecuentes y variables de entorno para ejecutar contra el servidor de producción.

`AGENTS.md` mantiene en "Local environment" solo un resumen y el enlace al README, para que cada dato esté en un solo archivo.

### D6. UIDL reutilizados: aviso en el log

Al implementar apareció un caso que `correo-comandos` no contemplaba: GreenMail guarda los correos en memoria y, al recrear su contenedor, vuelve a numerar los UIDL desde `1`. Si la base conserva correos de la instancia anterior, un comando nuevo recibe un UIDL ya guardado en estado final, `pollAndProcess` lo trata como "leído de nuevo tras un reinicio" y lo borra sin ejecutarlo ni responder. Lo mismo pasaría con un servidor real que renumerara los UIDL.

Decisión del usuario: no cambiar la identificación de mensajes, sino hacer visible el caso.
- `InboxPollerService` registra un `WARN` cada vez que borra un mensaje que ya estaba guardado en estado final, con el UIDL, el asunto del mensaje leído y el del correo guardado, para que se note si no coinciden.
- `AGENTS.md` indica que, después de recrear el contenedor de GreenMail, hay que borrar también la base (`docker compose down -v`).

Alternativa descartada por ahora: identificar el mensaje por UIDL y `Message-ID`. Resuelve el caso, pero cambia el modelo de EA, la migración y la spec `mail-commands`.

Es una línea de log en `mail.application`; no cambia ninguna operación ni atributo del modelo.

## Risks / Trade-offs

- [Los puertos 2525, 1110, 1143 u 8025 están ocupados en la máquina del desarrollador] → Documentado en AGENTS.md; se cambian en `compose.yaml` y en el perfil `local`.
- [Los correos de GreenMail se pierden si se reinicia su contenedor] → Aceptado: es un entorno de prueba. `start-only` evita perderlos al reiniciar solo la aplicación.
- [La versión de PostgreSQL local difiere de la del servidor del docente] → Ver Open Questions; las migraciones de Flyway usan SQL estándar.
- [Roundcube pesa unos 150 MB y tarda en arrancar la primera vez] → Se descarga una sola vez; su chequeo de disponibilidad no bloquea el arranque de la aplicación.
- [Docker Desktop no está corriendo] → `bootRun` falla con un error claro de Docker Compose; AGENTS.md lo indica como requisito.
- [Un comando con un UIDL reutilizado se descarta sin ejecutarse (D6)] → Queda un `WARN` en el log y AGENTS.md indica borrar la base al recrear GreenMail. En producción sigue siendo posible si el servidor renumera los UIDL.

## Migration Plan

No hay datos que migrar. Para revertir, se elimina `compose.yaml`, el perfil `local` y la dependencia, y se borran los contenedores con `docker compose down -v`.

## Open Questions

- Versión de PostgreSQL del servidor `db.tecnoweb.org.bo` (NFR-008). Si se conoce, se fija la misma en `compose.yaml`; no cambia el enfoque ni las tareas.

## Diferencias con el modelo

Ninguna. El change toca infraestructura, configuración y documentación, que no se modelan en EA, y agrega una línea de log en `InboxPollerService` (D6) que no cambia sus operaciones ni atributos.

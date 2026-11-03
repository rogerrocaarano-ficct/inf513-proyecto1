# mail-app

Prueba de concepto del sistema de titulación que se opera por correo electrónico (AD-001). El diseño está en [`docs/model/design/`](docs/model/design/architecture.md).

## Requisitos

- Java 25, para compilar y probar sin Docker.
- Docker con Docker Compose, para las pruebas de integración (Testcontainers) y el stack de pruebas.

## Pruebas

```bash
./gradlew test
```

Las pruebas de integración levantan PostgreSQL 17 con Testcontainers y un servidor de correo en memoria con GreenMail.

## Desarrollo local

El archivo `docker-compose.dev.yml` levanta solo PostgreSQL y Mailpit, para ejecutar `mail-app` desde el IDE o con Gradle. Sus puertos y credenciales coinciden con los valores por defecto de `application.properties`, así que `mail-app` no necesita otra configuración que el administrador inicial.

1. Levantar la base de datos y el correo:

   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```

2. Ejecutar `mail-app` con los datos del administrador inicial:

   ```bash
   INITIAL_ADMIN_NAME=Administrador INITIAL_ADMIN_EMAIL=admin@mailapp.local ./gradlew bootRun
   ```

   En IntelliJ, agregar esas dos variables de entorno a la configuración de ejecución de `MailAppApplication`.

3. Enviar los comandos por SMTP a `localhost:1025` hacia `sistema@mailapp.local` y ver las respuestas en `http://localhost:8025`, como en el stack de pruebas.

Los servicios usan los mismos puertos del host que el stack de pruebas (5432, 1025, 1110 y 8025), así que no se pueden levantar los dos a la vez. Para detenerlos: `docker compose -f docker-compose.dev.yml down` (con `-v` también se borran los datos).

### Comandos de prueba con archivos `.http`

`src/test/http/` tiene un archivo `.http` por comando, que lo envía a `mail-app` con la API de Mailpit (`POST /api/v1/send`), desde el administrador inicial hacia `sistema@mailapp.local`. Los archivos solo usan el formato común de los clientes HTTP de los IDEs, así que se ejecutan sin configuración en IntelliJ y los demás IDEs de JetBrains, en VS Code con REST Client o httpyac, en Neovim con kulala y en Zed. Cada archivo declara sus variables al inicio (`@baseUrl`, `@systemAddress`, `@adminAddress`).

1. Levantar los servicios y ejecutar `mail-app` como arriba, con `POLL_INTERVAL=5s` para no esperar 30 segundos por cada respuesta.
2. Ejecutar los archivos en orden, de `01-ping.http` a `07-reactivate-user.http`. Trabajan sobre la cuenta de ejemplo `ana.perez@mailapp.local`. `02-create-user.http` tiene una petición por cada forma de `CrearUsuario`: con los 3 argumentos obligatorios y con los 6.
   Luego, de `08-open-graduation-process.http` a `10-close-graduation-process.http`, para el trámite de titulación (UC-003): `08` crea la cuenta del secretario(a) `secretaria@mailapp.local`, que envía estos comandos, y abre el trámite del estudiante `juan.soto@mailapp.local` con los cuatro documentos iniciales adjuntos en base64. También muestra los rechazos y que una imagen inline se ignora. `10` explica cómo fijar a mano la calificación final, que todavía no registra ningún caso de uso, antes del cierre.
3. Ver las respuestas con `mailbox.http`: su primera petición trae los últimos 10 mensajes recibidos por la casilla `@mailbox` y la segunda vacía el buzón. También se ven en `http://localhost:8025`.

Los archivos de UC-001 cubren solo el caso feliz, sobre datos fijos. Al repetir la secuencia, `CrearUsuario` responde que la cuenta ya existe: para empezar de nuevo, borrar los datos con `docker compose -f docker-compose.dev.yml down -v` y volver a levantar los servicios.

## Stack de pruebas de desarrollo

El archivo `docker-compose.yml` levanta `mail-app` con PostgreSQL y Mailpit, un servidor de correo local con SMTP, POP3 e interfaz web.

1. Desde la raíz del repositorio, crear el archivo de configuración. Sus valores por defecto sirven para el stack local; en un servidor compartido, cambiar las contraseñas:

   ```bash
   cp .env.example .env
   ```

2. Levantar el stack:

   ```bash
   docker compose up -d --build
   ```

   Al primer arranque, Flyway crea las tablas y `mail-app` crea la cuenta del administrador inicial con `INITIAL_ADMIN_NAME` e `INITIAL_ADMIN_EMAIL`.

3. Enviar un comando: con cualquier cliente de correo o script, enviar un correo por SMTP a `localhost:1025` (`MAILPIT_SMTP_PORT`), desde la dirección del administrador inicial hacia `MAIL_ADDRESS`, con el asunto `Ping()`.

4. Ver la respuesta en la interfaz web de Mailpit, en `http://localhost:8025` (`MAILPIT_WEB_PORT`): después del siguiente sondeo (`POLL_INTERVAL`) aparece un correo `Re: Ping()` con el cuerpo `Pong`, dirigido al administrador.

Para revisar la base de datos, conectarse a `localhost:5432` (`POSTGRES_PORT`) con `DB_NAME`, `DB_USER` y `DB_PASSWORD` de `.env`.

Los archivos que envían los usuarios (por ejemplo, los documentos iniciales de un trámite) se guardan en el volumen `files`, montado en `/app/data/files` (`FILES_ROOT`, AD-012). Fuera de Docker se guardan en `./data/files`.

Para detener el stack: `docker compose down` (con `-v` también se borran los datos y los archivos).

## Servidores del docente

Para usar `mail.tecnoweb.org.bo` y `db.tecnoweb.org.bo` (CON-006, CON-007) en lugar del stack local, ejecutar `mail-app` con estas variables de entorno:

| Variable | Valor |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://db.tecnoweb.org.bo:5432/db_grupo03sc` |
| `DB_USER`, `DB_PASSWORD` | Los que entrega el docente |
| `POP3_HOST`, `POP3_PORT` | `mail.tecnoweb.org.bo`, `110` |
| `POP3_USER`, `POP3_PASSWORD` | El buzón del grupo |
| `SMTP_HOST`, `SMTP_PORT` | `mail.tecnoweb.org.bo`, `25` |
| `MAIL_ADDRESS` | La dirección del buzón del grupo |
| `FILES_ROOT` | Un directorio persistente del servidor para los archivos que envían los usuarios (AD-012) |

El servidor SMTP valida la dirección IP de origen: la IP desde la que corre `mail-app` debe estar en la lista del docente.

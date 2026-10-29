# mail-app

Prueba de concepto del sistema de titulación que se opera por correo electrónico (AD-001). El diseño está en [`docs/model/design/`](../docs/model/design/architecture.md).

## Requisitos

- Java 25, para compilar y probar sin Docker.
- Docker con Docker Compose, para las pruebas de integración (Testcontainers) y el stack de pruebas.

## Pruebas

```bash
./gradlew test
```

Las pruebas de integración levantan PostgreSQL 17 con Testcontainers y un servidor de correo en memoria con GreenMail.

## Stack de pruebas de desarrollo

El archivo `docker-compose.yml` de la raíz del repositorio levanta `mail-app` con PostgreSQL y Mailpit, un servidor de correo local con SMTP, POP3 e interfaz web.

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

Para detener el stack: `docker compose down` (con `-v` también se borran los datos).

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

El servidor SMTP valida la dirección IP de origen: la IP desde la que corre `mail-app` debe estar en la lista del docente.

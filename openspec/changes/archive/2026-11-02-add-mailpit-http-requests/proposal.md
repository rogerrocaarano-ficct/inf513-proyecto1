# Proposal

## Why

Para probar a mano los comandos de `mail-app` en desarrollo local hay que armar un correo con un cliente SMTP o con la colección de Bruno de la API de Mailpit. Esa colección se está retirando: es pesada, solo sirve en Bruno y su carpeta de pruebas de UC-001 nunca llegó al repositorio, aunque el `README.md` la describe. Hace falta una forma simple y portable de enviar cada comando y revisar el buzón desde cualquier IDE.

## What Changes

- Una carpeta `src/test/http/` con un archivo `.http` por comando, que envía el comando a `mail-app` con `POST /api/v1/send` de Mailpit en el caso feliz:
  - `01-ping.http`, `02-create-user.http`, `03-view-user.http`, `04-list-users.http`, `05-update-user.http`, `06-deactivate-user.http` y `07-reactivate-user.http`.
  - Numerados en el orden en que se ejecutan, sobre una misma cuenta de ejemplo.
  - Un comando que acepta distintas cantidades de argumentos trae una petición por cada forma. Hoy es solo `CrearUsuario`: una petición con los 3 argumentos obligatorios y otra con los 6.
- Un archivo `mailbox.http` con dos peticiones generales:
  - Los últimos 10 mensajes recibidos por una casilla, con `GET /api/v1/search?query=to:<casilla>&limit=10`.
  - Eliminar todos los mensajes del buzón, con `DELETE /api/v1/messages`.
- Se elimina la colección de Bruno `src/test/Mailpit API/`, que hoy está solo en el índice de git, y la sección "Pruebas por correo con Bruno" del `README.md`. En su lugar, el `README.md` explica cómo usar los archivos `.http`.

Fuera de esta propuesta:

- Los flujos alternativos y de error de los comandos: los archivos cubren solo el caso feliz. Las pruebas automáticas siguen en `UserCommandsTests` y `MailCommandsTests`.
- Comprobar las respuestas de forma automática: los archivos solo envían el comando, y la respuesta se revisa con `mailbox.http` o en la interfaz web de Mailpit.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna. Es herramienta de desarrollo: no cambia el comportamiento de `mail-app`, así que la change declara `skip_specs: true`.

## Impact

- Archivos nuevos en `src/test/http/`. No se compilan ni se ejecutan con `./gradlew test`.
- Se borran los archivos de `src/test/Mailpit API/` del índice y del disco.
- `README.md`, sección "Desarrollo local".
- No cambia código de producción, ni dependencias, ni el modelo en `docs/model/`.

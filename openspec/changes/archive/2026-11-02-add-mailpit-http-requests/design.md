# Design

## Context

`mail-app` recibe los comandos por POP3 desde Mailpit (`localhost:1110`) en cada sondeo (`POLL_INTERVAL`, 30 s por defecto) y responde por SMTP. Mailpit permite inyectar un correo con `POST /api/v1/send`, sin cliente SMTP. Para que `mail-app` procese un correo, este debe cumplir tres condiciones:

- Estar dirigido a la dirección del sistema (`MAIL_ADDRESS`, por defecto `sistema@mailapp.local`).
- No llevar la cabecera `Auto-Submitted`, porque `IncomingMailFilter` descarta esos correos.
- Venir de una cuenta registrada y activa. Los comandos de gestión de usuarios exigen además el rol de administrador o de director de carrera.

El asunto tiene la forma `Comando(arg1; arg2; ...)` (`CommandSubjectParser`). Los comandos y su sintaxis están en los manejadores de `users/adapter/in/`.

## Goals / Non-Goals

**Goals:**
- Que los archivos se abran y ejecuten sin cambios en el mayor número posible de IDEs y clientes.
- Que la secuencia completa funcione contra el stack de `docker-compose.dev.yml` con sus valores por defecto.

**Non-Goals:**
- Esperar la respuesta o comprobarla desde el archivo.
- Hacer la secuencia repetible sin reiniciar la base de datos.

## Decisions

### D1. Formato `.http` mínimo y portable

Cada archivo usa solo lo que comparten los clientes HTTP de JetBrains, VS Code REST Client, httpyac, kulala (Neovim) y Zed: variables en el mismo archivo (`@name = value`), separadores `###` entre peticiones y comentarios con `#`.

No se usan scripts de respuesta (`> {% %}`), variables dinámicas (`{{$uuid}}`, `{{$guid}}`) ni archivos de entorno (`http-client.env.json`, `settings.json` de VS Code), porque cada cliente los escribe de forma distinta.

Alternativa descartada: un archivo de entorno compartido, que evita repetir las variables pero ata los archivos a un solo IDE.

### D2. Variables repetidas en cada archivo

Cada archivo declara `@baseUrl` (`http://localhost:8025`), `@systemAddress` (`sistema@mailapp.local`) y `@adminAddress` (`admin@mailapp.local`), con los valores por defecto de `application.properties` y del `README.md`. `mailbox.http` declara `@baseUrl` y `@mailbox`. Repetir tres líneas en ocho archivos es más simple que mantener un entorno por cada IDE.

### D3. Cuerpo del correo mínimo

El cuerpo JSON de `POST /api/v1/send` lleva solo `From`, `To`, `Subject` y `Text` vacío. El ejemplo de la colección de Bruno trae `Cc`, `Bcc`, `ReplyTo`, `Headers`, etiquetas y un adjunto; nada de eso hace falta, y una cabecera de más podría hacer que `IncomingMailFilter` descarte el correo.

### D4. Secuencia numerada sobre una cuenta de ejemplo fija

Los archivos de comandos llevan un número de orden y trabajan sobre la misma cuenta `ana.perez@mailapp.local`:

| Archivo | Asunto |
| --- | --- |
| `01-ping.http` | `Ping()` |
| `02-create-user.http` | `CrearUsuario(Ana Pérez; ana.perez@mailapp.local; docente)` |
| `03-view-user.http` | `VerUsuario(ana.perez@mailapp.local)` |
| `04-list-users.http` | `ListarUsuarios()` |
| `05-update-user.http` | `ModificarUsuario(ana.perez@mailapp.local; telefono; 70000000)` |
| `06-deactivate-user.http` | `DesactivarUsuario(ana.perez@mailapp.local)` |
| `07-reactivate-user.http` | `ReactivarUsuario(ana.perez@mailapp.local)` |

La cuenta de ejemplo tiene el rol `docente` porque el rol `estudiante` exige además registro universitario, cédula y teléfono, y el caso feliz se mantiene corto.

### D4.1. Una petición por cada forma de un comando

Cuando un comando acepta distintas cantidades de argumentos, su archivo trae una petición por cada forma, separadas con `###` y todas del caso feliz. Hoy solo `CrearUsuario` tiene argumentos opcionales (`nombreCompleto; correo; roles[; registroUniversitario; cedula; telefono]`), así que `02-create-user.http` lleva dos peticiones:

| Forma | Asunto |
| --- | --- |
| 3 argumentos, solo los obligatorios | `CrearUsuario(Ana Pérez; ana.perez@mailapp.local; docente)` |
| 6 argumentos, con los opcionales | `CrearUsuario(Luis Rojas; luis.rojas@mailapp.local; estudiante; 2026001; 1234567; 70000001)` |

La segunda usa el rol `estudiante`, que necesita los tres datos opcionales. Los demás archivos de la secuencia siguen trabajando sobre `ana.perez@mailapp.local`.

Los otros comandos tienen una sola forma: `Ping()` y `ListarUsuarios()` sin argumentos, `VerUsuario`, `DesactivarUsuario` y `ReactivarUsuario` con uno, y `ModificarUsuario` con tres. Entre todos los archivos quedan ejemplos con 0, 1, 3 y 6 argumentos.

Un comando que se agregue más adelante sigue la misma regla.

Alternativa descartada: direcciones aleatorias en cada ejecución, que no tienen una sintaxis común entre clientes (D1).

### D5. Nombres en inglés, asuntos en español

Los nombres de archivo y los comentarios están en inglés, en kebab-case, como el resto del código. Se toman de los manejadores (`CreateUserCommandHandler` → `create-user`). Los asuntos y los datos de ejemplo siguen en español porque son la sintaxis de los comandos que entiende `mail-app`.

### D6. Consulta del buzón por casilla

`GET /api/v1/messages` solo acepta `start` y `limit`, así que la consulta usa `GET /api/v1/search?query=to:{{mailbox}}&limit=10`. Devuelve del más nuevo al más antiguo, igual que el listado. Los caracteres `:` y `@` son válidos en una URL, así que no hace falta codificarlos.

El borrado usa `DELETE /api/v1/messages` sin cuerpo: sin `IDs`, Mailpit borra todos los mensajes.

## Risks / Trade-offs

- [Al repetir la secuencia, `CrearUsuario` falla porque la cuenta ya existe] → El `README.md` indica reiniciar los datos con `docker compose -f docker-compose.dev.yml down -v`.
- [La respuesta tarda hasta `POLL_INTERVAL` en llegar] → El `README.md` recomienda `POLL_INTERVAL=5s`, como hacía la sección de Bruno.
- [Las variables quedan repetidas en ocho archivos y pueden desalinearse] → Son tres valores estables, los de `application.properties`.
- [Un cliente que no soporte variables en el archivo] → Los clientes listados en D1 las soportan. Uno que no, igual puede usar la petición reemplazando los valores a mano.

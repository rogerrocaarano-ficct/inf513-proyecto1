# Proposal

## Why

Hoy un desarrollador no tiene cómo ejecutar el sistema completo en su máquina: `bootTestRun` levanta una base que se pierde al detenerse, y no hay un servidor de correo local, así que los comandos solo se prueban con las pruebas automáticas o contra el servidor del docente (RSK-001). Hace falta un entorno local que se levante con un comando y permita probar los comandos por correo como lo hará un usuario.

Pertenece al trabajo compartido **entorno de desarrollo**: no corresponde a un caso de uso y lo usan todos.

## What Changes

- `compose.yaml` en la raíz con tres servicios para desarrollo:
  - **PostgreSQL** con volumen, para que los datos se conserven entre arranques.
  - **GreenMail**: servidor SMTP, POP3 e IMAP local, con un buzón por usuario.
  - **Roundcube**: webmail conectado a GreenMail, para enviar comandos y leer las respuestas desde el navegador con cualquier dirección (`ana@cpms.local`, …).
- `./gradlew bootRun` levanta los contenedores y conecta la base sola, mediante la dependencia de desarrollo `spring-boot-docker-compose`.
- Perfil Spring `local` (`application-local.properties`) con los valores del entorno local: servidores en `localhost`, puertos sin privilegios, dirección y credenciales del buzón del sistema. `bootRun` lo activa; producción sigue configurándose con variables de entorno.
- Requisitos e instrucciones del entorno de desarrollo en `README.md` (sección "Entorno de desarrollo"), con un resumen y un enlace en `AGENTS.md`.
- Aviso en el log cuando se borra sin ejecutar un mensaje cuyo UIDL ya estaba guardado, caso que aparece al recrear GreenMail (ver design, D6).

Fuera de alcance: cambios de comportamiento del sistema, despliegue en producción y persistencia de los correos de GreenMail entre reinicios.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna. El change no altera el comportamiento del sistema; declara `skip_specs: true`.

## Impact

- **Elementos de EA**: ninguno. Solo toca infraestructura y configuración, que no se modelan.
- **Requisitos y riesgos**: NFR-007 y NFR-008 no cambian; el perfil `local` no reemplaza la configuración de producción. Reduce RSK-001 y RSK-004, porque los comandos se pueden probar de punta a punta antes de usar el servidor real.
- **Archivos**: `compose.yaml`, `src/main/resources/application-local.properties`, `build.gradle.kts` (dependencia y configuración de `bootRun`), `README.md`, `AGENTS.md`.
- **Dependencias**: `org.springframework.boot:spring-boot-docker-compose` como `developmentOnly`. Imágenes Docker: `postgres`, `greenmail/standalone`, `roundcube/roundcubemail`.
- **Requisito para el desarrollador**: Docker en ejecución, como ya exigen las pruebas.

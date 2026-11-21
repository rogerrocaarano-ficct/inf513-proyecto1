# Tasks

## 1. Contenedores

- [x] 1.1 Escribir `compose.yaml` con `postgres:17` (volumen con nombre, puerto asignado por Docker), `greenmail/standalone:2.1.15` (puertos 2525, 1110 y 1143 en el host, usuario `sistema@cpms.local`) y `roundcube/roundcubemail:1.6.19-apache` (puerto 8025, apuntando a `greenmail`), según D1; verificar con `docker compose config` y con `docker compose up -d` que los tres servicios quedan en ejecución
- [x] 1.2 Comprobar GreenMail y Roundcube: entrar a `http://localhost:8025` como `ana@cpms.local`, enviar un correo a `sistema@cpms.local` y verificar que llega al buzón del sistema (por POP3 con `sistema@cpms.local`/`secret`) y que una respuesta enviada a `ana@cpms.local` aparece en Roundcube

## 2. Arranque y perfil local

- [x] 2.1 Agregar `org.springframework.boot:spring-boot-docker-compose` como `developmentOnly` y hacer que `bootRun` active el perfil `local` en `build.gradle.kts` (D2, D3); verificar que la dependencia no queda en el JAR de producción (`./gradlew bootJar` y listar `BOOT-INF/lib`)
- [x] 2.2 Crear `src/main/resources/application-local.properties` con los valores de D3 y desactivar el chequeo de disponibilidad de Roundcube en `compose.yaml`; verificar que `./gradlew bootRun` levanta los contenedores, aplica las migraciones en la base de `compose.yaml` y arranca sin errores
- [x] 2.3 Agregar `spring.docker.compose.enabled=false` a `src/test/resources/config/application.properties` (D4); verificar que `./gradlew build` pasa y que `./gradlew bootTestRun` arranca con Testcontainers sin levantar `compose.yaml`

## 3. Verificación integrada y documentación

- [x] 3.1 Con `./gradlew bootRun` en marcha, enviar desde Roundcube como `ana@cpms.local` el comando `usuarios.listar[]` a `sistema@cpms.local`; verificar que en el siguiente ciclo llega a `ana@cpms.local` la respuesta "comando desconocido" y que, tras detener y volver a arrancar la aplicación, el correo sigue registrado en la base
- [x] 3.2 Agregar a `README.md` la sección "Entorno de desarrollo" de D5 (requisitos, comprobación, arranque, uso del webmail, detención y borrado de datos, problemas frecuentes, variables de producción) y dejar en `AGENTS.md` un resumen con el enlace; verificar siguiendo los pasos tal como quedaron escritos
- [x] 3.4 Registrar un `WARN` en `InboxPollerService` al borrar un mensaje que ya estaba guardado en estado final, y documentar en `AGENTS.md` que al recrear GreenMail hay que borrar la base (D6); verificar con una prueba unitaria que el aviso aparece con el UIDL y los dos asuntos
- [x] 3.3 Revisar contra el design que el único cambio en Java es el aviso de D6 en `InboxPollerService`, sin operaciones ni atributos nuevos respecto de `Design/Mail/application/InboxPollerService`; verificar con `git diff --stat` que el resto son `compose.yaml`, `build.gradle.kts`, archivos de configuración, `README.md` y `AGENTS.md`

## Workflow follow-up

- Archivar el change cuando las tareas estén hechas (`/opsx:archive`).
- Integrar la rama `correo-comandos` en `develop`.

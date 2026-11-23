# Tasks

## 1. Imagen de la aplicación

- [x] 1.1 Crear `.dockerignore` (excluye `build/`, `.gradle/`, `data/`, `bin/`, `.git/`, `openspec/` y la documentación) y verificar que el contexto de `docker build` no incluye esas carpetas
- [x] 1.2 Crear el `Dockerfile` de dos etapas (`eclipse-temurin:25-jdk` con `./gradlew bootJar --no-daemon`, `eclipse-temurin:25-jre` con usuario sin privilegios y `/var/lib/cpms/storage`, sin `VOLUME`) y verificar que `docker build -t cpms:staging .` termina y la imagen arranca `java -jar`

## 2. Perfil `staging`

- [x] 2.1 Crear `src/main/resources/application-staging.properties` con los valores de la tabla del design (datasource, correo, administrador y almacenamiento), con comentarios en inglés, y verificar con `./gradlew build` que no rompe las pruebas
- [x] 2.2 Activar `LocalTestAccounts` con `@Profile({"local", "staging"})` y actualizar su Javadoc (UC-001), y verificar con un test que el perfil `staging` crea las cuentas de prueba (extender `LocalTestAccountsTest` o el wiring test de `users`) y `./gradlew build` pasa
- [x] 2.3 Crear `StagingDatabaseReset` en el paquete raíz (`@Profile("staging")`, `FlywayMigrationStrategy` con `clean()` y `migrate()`, Javadoc en inglés) y `spring.flyway.clean-disabled=false` en `application-staging.properties`, y verificar con un test de contexto que la estrategia existe solo con el perfil `staging` y que ejecuta clean antes de migrate

## 3. Compose de despliegue

- [x] 3.1 Crear `compose.staging.yaml` con `app`, `postgres`, `greenmail` y `roundcube` según el design (sin `ports`, sin volúmenes con nombre, `tmpfs` para el almacenamiento, `restart: true` sobre `greenmail`, `healthcheck` de PostgreSQL, variables con valores por defecto) y verificar con `docker compose -f compose.staging.yaml config` que es válido
- [x] 3.2 Levantarlo en local con `docker compose -f compose.staging.yaml up --build` y verificar: `Started CpmsApplication` y las cuentas de prueba creadas en los logs de `app`, y `docker compose -f compose.staging.yaml ps` sin puertos publicados
- [x] 3.3 Con el stack anterior, enviar por SMTP dentro de la red un comando desde `admin@test.local` a `sistema@cpms.local` y verificar que la respuesta llega a su buzón y que Roundcube responde HTTP 200 dentro de la red; luego desactivar una cuenta de prueba y dejar un archivo en el almacenamiento, reiniciar `app` y verificar que la cuenta vuelve a estar activa, que las migraciones se aplican desde cero y que el archivo ya no existe

## 4. Documentación

- [x] 4.1 Agregar a `README.md` (sección "Entorno de desarrollo" o una nueva "Entorno de pruebas desplegado") cómo configurar el proyecto Compose en Dokploy (rama `develop`, `compose.staging.yaml`, dominio de `roundcube` puerto 80, variables opcionales) y cómo lo usan los testers (URL, cuentas, buzón del sistema, entorno que se vacía en cada build o reinicio, qué hacer ante el `WARN` "Deleting message … without executing it", no usar datos reales), y verificar que los nombres y valores coinciden con los archivos
- [x] 4.2 Agregar `Dockerfile`, `.dockerignore`, `compose.staging.yaml` y el perfil `staging` a la tabla "Layout" y a "Local environment" de `AGENTS.md`, y verificar que la tabla conserva sus columnas

## 5. Verificación

- [x] 5.1 Comparar la implementación con la arquitectura de referencia de `AGENTS.md` (sin SQL en `src/main`, sin dependencias nuevas, `spring-boot-docker-compose` fuera de la imagen, Javadoc en inglés) y verificar que `./gradlew build` pasa completo

## Workflow follow-up

- Integrar en `develop` por PR y crear el proyecto Compose en Dokploy siguiendo el `README.md`; comprobar el primer despliegue con un comando desde el webmail.
- Archivar el change cuando las tareas estén hechas y las pruebas pasen.

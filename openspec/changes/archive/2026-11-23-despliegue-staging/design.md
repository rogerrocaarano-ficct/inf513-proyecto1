# Design

## Context

El entorno local (`compose.yaml` + perfil `local`) ya tiene todo lo que un tester necesita: PostgreSQL 17, GreenMail sin autenticación con el buzón `sistema@cpms.local`, Roundcube y las cuentas de prueba de `LocalTestAccounts` (`@Profile("local")`). La aplicación lo usa a través de `spring-boot-docker-compose`, que es `developmentOnly` y no existe fuera de `bootRun`. El perfil por defecto (`application.properties`) lee el correo y el almacenamiento de variables de entorno y fija los puertos de producción (POP3 110, SMTP 25), que GreenMail no usa (3110, 3025). No hay `Dockerfile` ni imagen de la aplicación.

Dokploy despliega un proyecto de tipo Docker Compose clonando una rama del repositorio, ejecutando `docker compose` sobre un archivo del repositorio y publicando por Traefik los dominios que se configuran en su interfaz para un servicio y un puerto.

## Goals / Non-Goals

**Goals:**

- Un solo archivo Compose que Dokploy levanta sin pasos manuales en el servidor, salvo crear el proyecto y el dominio.
- La misma experiencia de prueba que en local: mismas cuentas, mismo buzón del sistema, mismo webmail.

**Non-Goals:**

- Un entorno de producción (servidor de correo real, TLS en SMTP/POP3, copias de seguridad). Este Compose es solo para pruebas.
- Pipeline de CI o registro de imágenes: Dokploy construye la imagen desde el repositorio.
- Conservar datos entre arranques: el entorno es desechable (requisito "Entorno limpio en cada arranque").

## Decisions

### Imagen de la aplicación: `Dockerfile` de dos etapas en la raíz

- Etapa `build` sobre `eclipse-temurin:25-jdk`: copia el wrapper y los archivos de Gradle primero (capa de dependencias reutilizable), luego `src`, y ejecuta `./gradlew bootJar --no-daemon`. No corre las pruebas: necesitan Docker (Testcontainers) y ya se ejecutan antes de integrar en `develop`.
- Etapa final sobre `eclipse-temurin:25-jre`, con un usuario sin privilegios, la carpeta de almacenamiento `/var/lib/cpms/storage` y `java -jar app.jar`. Sin `VOLUME`: Compose conserva el volumen anónimo que crea al recrear el contenedor, y los archivos sobrevivirían a la limpieza de la base.
- `.dockerignore` excluye `build/`, `.gradle/`, `data/`, `bin/`, `.git/`, `openspec/` y la documentación, para que el contexto sea chico y no se copie el almacenamiento local.
- Alternativa descartada: `bootBuildImage` (buildpacks). Necesita Docker dentro del build de Dokploy y da menos control sobre el usuario y las carpetas.

### Compose de despliegue: `compose.staging.yaml` en la raíz, separado de `compose.yaml`

- `compose.yaml` lo usa `bootRun` y publica puertos en la máquina del desarrollador; mezclar ambos con perfiles de Compose complica el uso local. Un archivo aparte, en la raíz para que el contexto de build sea `.`.
- Servicios:
  - `app`: `build: .`, `SPRING_PROFILES_ACTIVE=staging`, `depends_on` con `condition: service_healthy` sobre `postgres` y `service_started` con `restart: true` sobre `greenmail` (cuando Compose reinicia GreenMail, reinicia también la app, que limpia la base), `tmpfs` en `/var/lib/cpms/storage` (propiedad del usuario `cpms`), `restart: unless-stopped`. Sin puertos: no tiene interfaz HTTP.
  - `postgres`: `postgres:17` sin volumen con nombre, `healthcheck` con `pg_isready`. Usuario, base y contraseña con valores por defecto `cpms` sobreescribibles desde las variables de entorno de Dokploy (`${POSTGRES_PASSWORD:-cpms}`).
  - `greenmail`: misma imagen y `GREENMAIL_OPTS` que `compose.yaml`, sin `ports`.
  - `roundcube`: misma imagen y configuración que `compose.yaml`, sin `ports`; Dokploy le asigna el dominio en el puerto 80. Se agrega `ROUNDCUBEMAIL_DES_KEY` desde variable de entorno (con valor por defecto) para que la sesión no dependa de la clave generada en cada contenedor.
- Solo `expose`/red interna: el aislamiento de SMTP, POP3, IMAP y PostgreSQL resulta de no declarar `ports` (requisito "Servicios internos no expuestos").
- Sin volúmenes con nombre: nada del entorno se conserva a propósito.

### Reinicio de datos en cada arranque: `StagingDatabaseReset`

- GreenMail guarda los correos en memoria y, al reiniciarse, numera de nuevo los mensajes POP3 desde 1. Si la base conserva correos con esos UIDL, `InboxPollerService` borra los comandos nuevos sin ejecutarlos. Además, Dokploy en un redespliegue solo recrea `app` (su imagen cambia); `postgres` y `greenmail` siguen corriendo.
- Por eso la limpieza la hace la aplicación al arrancar: una clase `@Configuration(proxyBeanMethods = false)` con `@Profile("staging")`, `StagingDatabaseReset`, en el paquete raíz `bo.org.tecnoweb.cpms` (junto a `CpmsApplication`: es del entorno, no de un subsistema), que declara un `FlywayMigrationStrategy` que ejecuta `flyway.clean()` y luego `flyway.migrate()`. Luego `InitialAdminAccount` y `LocalTestAccounts` crean las cuentas.
- `spring.flyway.clean-disabled=false` solo en `application-staging.properties`; en los demás perfiles `clean` sigue deshabilitado.
- Los archivos guardados están en `tmpfs` del contenedor `app`, que se vacía cada vez que el contenedor arranca, junto con la base.
- No es SQL en `src/main`: la limpieza la hace Flyway.
- Alternativa descartada: solo quitar los volúmenes. PostgreSQL no se recrea en cada build, así que la base sobreviviría a los despliegues.

### Perfil `staging`: `application-staging.properties`

Fija lo que el Compose determina y no es secreto, con los mismos valores que el perfil `local` salvo los hosts:

| Propiedad | Valor |
| --- | --- |
| `spring.datasource.url` | `jdbc:postgresql://postgres:5432/${POSTGRES_DB:cpms}` |
| `spring.datasource.username` / `password` | `${POSTGRES_USER:cpms}` / `${POSTGRES_PASSWORD:cpms}` |
| `cpms.mail.address` | `sistema@cpms.local` |
| `cpms.mail.poll-interval` | `10s` |
| `cpms.mail.pop3.host` / `port` | `greenmail` / `3110` |
| `cpms.mail.pop3.username` / `password` | `sistema@cpms.local` / `secret` |
| `spring.mail.host` / `port` | `greenmail` / `3025` |
| `cpms.users.admin-email` | `admin@test.local` |
| `cpms.storage.root` | `/var/lib/cpms/storage` |
| `spring.flyway.clean-disabled` | `false` |

- No activa el log `DEBUG` del canal de correo del perfil `local`: ensuciaría los logs de Dokploy con cada ciclo de POP3.
- `cpms.academic.current-semester` sigue viniendo de `CPMS_CURRENT_SEMESTER` como en producción.
- Alternativa descartada: activar el perfil `local` y sobreescribir con variables. Arrastra el `DEBUG`, la ruta relativa `./data/storage` y `start-only` de docker-compose, y ata el servidor a cambios pensados para la máquina del desarrollador.

### Cuentas de prueba: `@Profile({"local", "staging"})` en `LocalTestAccounts`

Se reutiliza el runner existente en vez de duplicarlo; solo cambia la anotación y el Javadoc. El nombre de la clase se mantiene: siguen siendo las cuentas del entorno local, ahora también en el de pruebas. El administrador lo sigue creando `InitialAdminAccount` con `cpms.users.admin-email`.

### Despliegue en Dokploy

Proyecto de tipo Compose, fuente el repositorio de GitHub, rama `develop`, archivo `compose.staging.yaml`, con despliegue automático al recibir un push. En la pestaña Domains, un dominio para el servicio `roundcube`, puerto `80`. Las variables opcionales (`POSTGRES_PASSWORD`, `ROUNDCUBEMAIL_DES_KEY`, `CPMS_CURRENT_SEMESTER`) se cargan en la pestaña Environment de Dokploy, nunca en git.

## Diferencias con el modelo

Trabajo compartido sin caso de uso y sin diseño en EA; se sigue la arquitectura de referencia. No crea clases, operaciones ni relaciones de dominio o aplicación: cambia el perfil en el que se activa `users.infrastructure.LocalTestAccounts` y agrega la configuración `StagingDatabaseReset` en el paquete raíz; ambas son infraestructura y no se modelan en EA.

## Risks / Trade-offs

- [Los testers pierden sus trámites y correos en cada build o reinicio] → Decisión del equipo: el entorno es desechable; el `README.md` lo advierte.
- [Si GreenMail se cae y Docker lo reinicia por su cuenta, `restart: true` no aplica y la app sigue con la base anterior: los comandos nuevos se descartarían] → Reiniciar el servicio `app` desde Dokploy (limpia la base); el `README.md` lo indica junto al `WARN` "Deleting message … without executing it".
- [`flyway.clean()` borra toda la base] → Solo con el perfil `staging`, y la propiedad que lo habilita está solo en `application-staging.properties`; un test comprueba que la estrategia no existe sin ese perfil.
- [Webmail público sin autenticación: cualquiera con la URL lee y envía correos de cualquier cuenta de prueba] → Decisión del equipo: los datos no son reales. No se debe cargar información personal real; el `README.md` lo indica.
- [El build en Dokploy no ejecuta las pruebas] → Las pruebas corren antes de integrar en `develop`; un JAR que no arranca se ve en los logs de Dokploy y el contenedor anterior sigue disponible hasta corregir.
- [Construir con Gradle en el servidor consume CPU y memoria en cada despliegue] → Capa de dependencias cacheada en el `Dockerfile`; si el servidor no da abasto, más adelante se puede construir en CI y publicar la imagen.
- [Las contraseñas por defecto (`cpms`, `secret`) quedan en el repositorio] → Los servicios no están expuestos; `POSTGRES_PASSWORD` se puede cambiar desde Dokploy.

## Migration Plan

1. Integrar el change en `develop`.
2. En Dokploy, crear el proyecto Compose (rama `develop`, `compose.staging.yaml`), el dominio de `roundcube` y, si se quiere, las variables de entorno; desplegar.
3. Comprobar en los logs de `app` `Successfully applied N migrations`, `Started CpmsApplication` y `Created 11 local test accounts`, y probar un comando desde el webmail.

Rollback: volver a desplegar un commit anterior desde Dokploy, o detener el proyecto; no afecta el entorno local.

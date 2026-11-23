# Design

## Context

`StagingDatabaseReset` (paquete raíz, `@Profile("staging")`) declara un `FlywayMigrationStrategy` que ejecuta `clean()` y `migrate()`; `application-staging.properties` habilita `spring.flyway.clean-disabled=false`. En staging los archivos están en un `tmpfs`, así que se vacían solos al recrear el contenedor; en local están en `./data/storage`, en el disco del desarrollador. `LocalTestAccounts` ya corre con `local` y `staging`. El perfil `local` usa `spring.docker.compose.lifecycle-management=start-only`: los contenedores siguen corriendo entre arranques, y GreenMail conserva los correos mientras no se recree.

## Goals / Non-Goals

**Goals:**

- El mismo comportamiento de datos en `local` y `staging`, con una sola implementación.
- Archivos y base vacíos juntos en ambos perfiles.

**Non-Goals:**

- Vaciar los buzones de GreenMail: los correos ya atendidos se borran del servidor; los pendientes se procesan sobre la base nueva.
- Cambiar `bootTestRun` (`TestCpmsApplication`) o las pruebas automáticas, que no usan estos perfiles.

## Decisions

### `DisposableDatabaseReset` en lugar de `StagingDatabaseReset`

Se renombra la clase y su test, y se cambia a `@Profile({"local", "staging"})`. El nombre describe el comportamiento (entorno desechable) en vez del entorno. `spring.flyway.clean-disabled=false` se agrega a `application-local.properties`; el perfil por defecto sigue sin habilitarlo, así que producción no puede limpiar aunque alguien declarara la estrategia.

Alternativa descartada: una segunda clase para `local`. Duplica el mismo `@Bean`.

### `storage.infrastructure.DisposableStorageReset`

- `@Component` con `@Profile({"local", "staging"})` en el subsistema `storage`, que es el dueño de la carpeta (`StorageProperties.root()`): ningún otro subsistema toca los archivos.
- Al construirse, borra el contenido de la carpeta raíz (no la carpeta) y registra en el log cuántos archivos quitó. Se construye durante el arranque del contexto, antes de que el canal de correo empiece a leer el buzón, así que nadie escribe mientras se vacía.
- En staging la carpeta ya está vacía (`tmpfs`); la clase no hace nada visible, pero mantiene la regla igual en ambos perfiles.
- Alternativa descartada: vaciar la carpeta desde `DisposableDatabaseReset`. Pondría el conocimiento de la carpeta de `storage` fuera de su subsistema.

### Orden con las cuentas de prueba

Flyway corre al crear el `EntityManagerFactory`; `InitialAdminAccount` y `LocalTestAccounts` son `ApplicationRunner`, que corren después de refrescar el contexto, sobre la base ya migrada. No hace falta ordenar nada.

## Diferencias con el modelo

Trabajo compartido sin caso de uso y sin diseño en EA; se sigue la arquitectura de referencia. Renombra `StagingDatabaseReset` a `DisposableDatabaseReset` y agrega `storage.infrastructure.DisposableStorageReset`; ambas son infraestructura y no se modelan en EA.

## Risks / Trade-offs

- [Spring Boot DevTools reinicia el contexto cuando cambia el código compilado: cada reinicio vacía la base y los archivos] → Es el comportamiento pedido (cada arranque de la aplicación); el `README.md` lo advierte para que el desarrollador lo sepa al probar varios comandos seguidos mientras edita código.
- [Un desarrollador pierde datos que quería conservar] → Decisión del equipo; quien necesite datos estables los vuelve a crear por correo o con las cuentas de prueba.
- [Borrar el contenido de una carpeta mal configurada] → Solo con `local` (carpeta `./data/storage` fija en el perfil) o `staging` (`/var/lib/cpms/storage` en `tmpfs`); en el perfil por defecto la clase no existe. El test comprueba que no se crea sin esos perfiles.

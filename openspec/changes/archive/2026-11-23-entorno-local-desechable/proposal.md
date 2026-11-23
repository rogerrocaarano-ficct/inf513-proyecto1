# Proposal

## Why

En el entorno local (`./gradlew bootRun`) la base y la carpeta `data/storage` se conservan entre arranques, y GreenMail guarda los correos en memoria: si su contenedor se recrea, numera de nuevo los mensajes y la aplicación descarta comandos nuevos hasta que alguien borra la base a mano (`docker compose down -v`, sección "Problemas frecuentes" del `README.md`). El entorno de pruebas desplegado ya resuelve esto arrancando limpio cada vez; el equipo quiere el mismo comportamiento en desarrollo. Es trabajo compartido, sin caso de uso propio.

## What Changes

- Con el perfil `local`, cada arranque de la aplicación vacía la base (Flyway `clean` + `migrate`) igual que con `staging`. La configuración que lo hace deja de ser solo de staging y pasa a llamarse `DisposableDatabaseReset`.
- Con los perfiles `local` y `staging`, cada arranque vacía también la carpeta de archivos guardados, para que base y archivos sigan yendo juntos.
- Las cuentas de prueba se crean de nuevo en cada arranque (ya lo hace `LocalTestAccounts` sobre una base vacía).
- `README.md`: el entorno local pasa a ser desechable; se simplifican "Detener y reiniciar" y "Problemas frecuentes". `AGENTS.md`: el nombre nuevo de la clase.

## Capabilities

### New Capabilities

- `local-environment`: entorno de desarrollo local (`./gradlew bootRun`) que arranca con datos limpios y las cuentas de prueba en cada arranque de la aplicación.

### Modified Capabilities

Ninguna: `test-environment` ya exige vaciar la base y los archivos en cada arranque; este change solo cambia cómo se implementa para los archivos.

## Impact

- Código: `StagingDatabaseReset` se renombra a `DisposableDatabaseReset` y se activa con `local` y `staging`; clase nueva `storage.infrastructure.DisposableStorageReset`; `spring.flyway.clean-disabled=false` también en `application-local.properties`; comentarios de `compose.staging.yaml` y `application-staging.properties`.
- Tests: el de la estrategia de Flyway (perfiles) y uno nuevo para la limpieza de la carpeta.
- Documentación: `README.md` y `AGENTS.md`.
- Sin caso de uso: no hay elementos de EA que realicen este trabajo (el modelo EA está en pausa y no modela la infraestructura).

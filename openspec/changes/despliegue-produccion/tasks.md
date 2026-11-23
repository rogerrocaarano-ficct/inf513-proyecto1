# Tasks

## 1. Compose de producción

- [x] 1.1 Crear `compose.production.yaml` con el único servicio `app` según el design (`build: .`, perfil por defecto, variables obligatorias con `${VAR:?}`, valores por defecto de tecnoweb, `CPMS_STORAGE_ROOT=/var/lib/cpms/storage`, volumen `cpms-storage`, sin `ports`) y verificar con `docker compose -f compose.production.yaml config` que es válido con todas las variables definidas y que falla nombrando la variable cuando falta una obligatoria
- [x] 1.2 Levantarlo en local apuntando a una base PostgreSQL desechable (variables de entorno en la línea de comandos, sin archivos versionados) y verificar que la aplicación arranca con el perfil por defecto, sin `Successfully cleaned schema` ni cuentas de prueba en el log, y que un archivo escrito en `/var/lib/cpms/storage` sigue después de `down` sin `-v` y `up`

## 2. Documentación

- [x] 2.1 Actualizar `README.md`, sección "Ejecutar contra el servidor de producción": cómo configurar el proyecto Compose en Dokploy (rama `main`, `compose.production.yaml`, variables obligatorias y opcionales, sin dominio), los requisitos previos (IP aceptada por el SMTP de tecnoweb, conectividad a correo y base) y que el volumen `cpms-storage` se respalda con la base; verificar que nombres y valores coinciden con el Compose
- [x] 2.2 Agregar `compose.production.yaml` a la tabla "Layout" de `AGENTS.md` y verificar que la tabla conserva sus columnas

## 3. Verificación

- [x] 3.1 Comparar la implementación con la arquitectura de referencia de `AGENTS.md` (sin cambios de código ni dependencias, `StagingDatabaseReset` y `LocalTestAccounts` inactivos en producción) y verificar que `./gradlew build` pasa

## Workflow follow-up

- Integrar en `develop` por PR, luego `develop` en `main`, y crear el proyecto Compose en Dokploy siguiendo el `README.md`.
- Archivar el change cuando las tareas estén hechas y las pruebas pasen.

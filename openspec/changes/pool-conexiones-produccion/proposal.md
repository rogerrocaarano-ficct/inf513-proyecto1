# Proposal

## Why

En producción la aplicación mantiene 10 conexiones abiertas contra `db_grupo03sc` de `db.tecnoweb.org.bo` (NFR-008), aunque estén ociosas. Esa base la comparten todos los grupos de la materia, y esas 10 conexiones son el valor por defecto del pool. La carga real no las necesita: los correos se procesan de uno en uno. Además, configurar el tamaño del pool solo con variables de Dokploy no funciona, porque Dokploy usa esas variables para resolver `compose.production.yaml` y el contenedor recibe únicamente las que el archivo declara en `environment`.

## What Changes

- `compose.production.yaml` declara en `environment` el tamaño máximo del pool (`SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE`, 3 por defecto) y el mínimo de conexiones ociosas (`SPRING_DATASOURCE_HIKARI_MINIMUMIDLE`, 1 por defecto). Las dos se pueden cambiar desde Dokploy.
- `README.md`, en "Ejecutar contra el servidor de producción", agrega las dos variables opcionales a la tabla de configuración.
- Los perfiles `local` y `staging` y los tests no cambian: usan bases propias y desechables.

Trabajo compartido, sin caso de uso: configuración del despliegue de producción.

Elementos de EA: ninguno. La infraestructura y el despliegue no se modelan en EA.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `production-environment`: se agrega el requisito de limitar las conexiones que la aplicación mantiene abiertas contra la base compartida de tecnoweb, con un límite configurable desde el servidor de despliegue.

## Impact

- `compose.production.yaml` y `README.md`.
- Sin cambios en `src/`, en las dependencias ni en las migraciones.
- Se despliega cuando el cambio llegue a `main`.

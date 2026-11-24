# Tasks

## 1. Pool de producción

- [x] 1.1 Declarar `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE` (`${…:-3}`) y `SPRING_DATASOURCE_HIKARI_MINIMUMIDLE` (`${…:-1}`) en `environment` del servicio `app` de `compose.production.yaml`, y verificar con `docker compose -f compose.production.yaml config` (con las variables obligatorias definidas) que el contenedor recibe 3 y 1 sin definirlas, y el valor definido cuando se definen
- [x] 1.2 Agregar las dos variables opcionales a la tabla de "Configurar el despliegue en Dokploy" de `README.md`, con sus valores por defecto, y verificar que la tabla conserva sus columnas
- [x] 1.3 Comprobar el cambio contra la arquitectura de referencia de `AGENTS.md` (no aplica EA): sin cambios en `src/`, ni SQL, ni dependencias, y verificar con `git diff --stat` que solo cambian `compose.production.yaml`, `README.md` y el cambio OpenSpec

## Workflow follow-up

- Tras desplegar desde `main`, comprobar en `pg_stat_activity` de tecnoweb que `grupo03sc` tiene como máximo 3 sesiones `PostgreSQL JDBC Driver`.
- Archivar el cambio cuando esté integrado.

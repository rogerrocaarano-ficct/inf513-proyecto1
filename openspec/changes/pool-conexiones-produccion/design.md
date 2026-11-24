# Design

## Context

La aplicación no configura el pool de conexiones, así que Spring Boot usa HikariCP con sus valores por defecto: `maximumPoolSize = 10` y `minimumIdle = maximumPoolSize`. Con eso, al arrancar abre 10 conexiones y las mantiene abiertas. En `pg_stat_activity` de tecnoweb se ven 10 sesiones `PostgreSQL JDBC Driver` de `grupo03sc` desde la IP del servidor de Dokploy.

En un servicio Compose, Dokploy escribe las variables del proyecto en un `.env` que Docker Compose usa solo para interpolar el archivo. El contenedor recibe únicamente lo que lista `environment:` en `compose.production.yaml`.

## Goals / Non-Goals

**Goals:**

- Limitar el pool de producción a valores adecuados a la carga y permitir ajustarlos desde Dokploy.

**Non-Goals:**

- Cambiar el pool de los perfiles `local` y `staging` o de los tests: usan bases propias y desechables.
- Configurar otros parámetros de Hikari (`idleTimeout`, `maxLifetime`, …).

## Decisions

- **Configurar el pool en `compose.production.yaml`, no en `application.properties`.** El límite responde a una restricción del entorno de producción (base compartida, NFR-008), no del sistema. Ponerlo en el compose lo deja junto a la configuración de la base (`SPRING_DATASOURCE_*`) y no afecta a los demás perfiles. Alternativa descartada: `spring.datasource.hikari.*` en `application.properties`, que también cambiaría `local`, `staging` y los tests.
- **Usar los nombres de variables que Spring entiende por *relaxed binding*** (`SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE`, `SPRING_DATASOURCE_HIKARI_MINIMUMIDLE`), con valores por defecto `${…:-3}` y `${…:-1}`, igual que `SPRING_DATASOURCE_URL`. Así funcionan sin configurar nada en Dokploy y se pueden ajustar desde ahí.
- **Valores 3 y 1.** `InboxPollerService` procesa los correos de uno en uno en una transacción. Las conexiones adicionales cubren Flyway al arrancar y el envío posterior al commit. Con `minimumIdle = 1`, Hikari cierra las conexiones sobrantes tras `idleTimeout`, que por defecto es de 10 minutos.

## Risks / Trade-offs

- [Pool agotado si crece la concurrencia] → hoy el procesamiento es secuencial. Si cambia, se sube el valor desde Dokploy sin tocar el repositorio. Si el pool se agota, Hikari espera `connectionTimeout` (30 s) y el error aparece en el log.

## Migration Plan

Se integra `develop` en `main` y se vuelve a desplegar en Dokploy. Para volver atrás basta con definir `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=10` en Dokploy, o con revertir el commit.

## Diferencias con el modelo

Ninguna: la infraestructura y el despliegue no se modelan en EA, y el cambio no crea clases, operaciones ni relaciones.

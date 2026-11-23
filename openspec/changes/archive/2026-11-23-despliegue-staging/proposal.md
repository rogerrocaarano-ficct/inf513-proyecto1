# Proposal

## Why

Hoy, para probar los comandos por correo, cada tester tiene que instalar JDK 25 y Docker y levantar el stack local (`./gradlew bootRun`). Un entorno de pruebas desplegado en el servidor Dokploy del equipo, parecido al de desarrollo, les permite probar con solo un navegador. Es trabajo compartido, sin caso de uso propio: sirve para probar todos los `UC-xxx` ya implementados.

## What Changes

- Imagen de contenedor de la aplicación (`Dockerfile` de dos etapas: compila el JAR con el wrapper de Gradle y lo ejecuta en un JRE 25).
- Archivo Compose para Dokploy (`compose.staging.yaml`) con la aplicación, PostgreSQL 17, GreenMail y Roundcube, equivalentes a los de `compose.yaml`.
  - Solo Roundcube se publica, con un dominio de Dokploy, sin autenticación adicional: los datos son de prueba.
  - GreenMail y PostgreSQL quedan en la red interna del Compose, sin puertos publicados.
  - Entorno desechable: la base de datos y los archivos se vacían en cada arranque de la aplicación (cada build o reinicio). GreenMail guarda los correos en memoria y numera de nuevo los mensajes al reiniciarse; con una base conservada, el sistema descartaría comandos nuevos.
- Perfil de Spring nuevo `staging` (`application-staging.properties`) que apunta a los contenedores internos y, solo en ese perfil, limpia el esquema con Flyway antes de migrar.
- Las cuentas de prueba (`LocalTestAccounts`) se crean también con el perfil `staging`, además de `local`.
- Dokploy despliega desde la rama `develop`.
- `README.md`: cómo configurar el despliegue en Dokploy y cómo lo usan los testers. `AGENTS.md`: los archivos nuevos en "Layout".

## Capabilities

### New Capabilities

- `test-environment`: entorno de pruebas compartido y desechable donde los testers envían comandos por un webmail, con cuentas de prueba para cada rol, sin exponer el servidor de correo ni la base de datos, y que arranca limpio en cada build.

### Modified Capabilities

Ninguna: los comandos y sus reglas no cambian.

## Impact

- Código: `LocalTestAccounts` (perfiles en los que se activa) y su Javadoc; configuración nueva `StagingDatabaseReset` en el paquete raíz (solo perfil `staging`); recurso nuevo `application-staging.properties`.
- Build y despliegue: `Dockerfile`, `.dockerignore` y `compose.staging.yaml` nuevos en la raíz. No se agregan dependencias de Gradle; `spring-boot-docker-compose` sigue siendo `developmentOnly` y no entra en la imagen.
- Infraestructura: un proyecto Compose en Dokploy que sigue `develop`, con un dominio para Roundcube.
- Documentación: `README.md` y `AGENTS.md`.
- Sin caso de uso: no hay elementos de EA que realicen este trabajo (el modelo EA está en pausa y no modela la infraestructura).

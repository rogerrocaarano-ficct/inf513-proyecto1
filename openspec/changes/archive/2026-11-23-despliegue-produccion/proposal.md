# Proposal

## Why

La aplicación ya tiene imagen (`Dockerfile`) y un entorno de pruebas en Dokploy, pero no hay una forma repetible de desplegar la versión entregada contra los servidores del docente de Tecnología Web: hoy solo está documentado qué variables de entorno necesita. Es trabajo compartido, sin caso de uso propio.

## What Changes

- Archivo Compose de producción (`compose.production.yaml`) con un solo servicio: la aplicación, construida con el mismo `Dockerfile` y el perfil por defecto. No levanta base de datos ni servidor de correo: usa los de tecnoweb (NFR-007, NFR-008).
- La configuración del correo, de la base y del buzón del sistema viene de variables de entorno cargadas en Dokploy; las obligatorias hacen fallar el despliegue si faltan.
- Los archivos guardados van en un volumen con nombre persistente, que sobrevive a los despliegues.
- Sin puertos ni dominio: la aplicación no tiene interfaz HTTP.
- Dokploy despliega desde la rama `main`.
- `README.md` ("Ejecutar contra el servidor de producción") y `AGENTS.md` documentan el despliegue.

## Capabilities

### New Capabilities

- `production-environment`: despliegue de la versión entregada contra el correo y la base de datos de tecnoweb, con configuración obligatoria fuera del repositorio y archivos que se conservan entre despliegues.

### Modified Capabilities

Ninguna.

## Impact

- Despliegue: `compose.production.yaml` nuevo en la raíz; reutiliza el `Dockerfile` existente sin cambios.
- Código Java y `application.properties`: sin cambios; el perfil por defecto ya lee todo de variables de entorno.
- Infraestructura: un segundo proyecto Compose en Dokploy que sigue `main`; el servidor de Dokploy debe poder llegar a `mail.tecnoweb.org.bo` (110, 25) y `db.tecnoweb.org.bo` (5432), y el SMTP de tecnoweb debe aceptar su IP (NFR-007).
- Documentación: `README.md` y `AGENTS.md`.
- Sin caso de uso: no hay elementos de EA que realicen este trabajo (el modelo EA está en pausa y no modela la infraestructura).

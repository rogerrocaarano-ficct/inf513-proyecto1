# production-environment Specification

## Purpose

Desplegar la versión entregada del sistema contra el servidor de correo y la base de datos de tecnoweb, con la configuración sensible fuera del repositorio y los archivos guardados conservados entre despliegues.

## Requirements

### Requirement: Servicios externos de tecnoweb

El entorno de producción SHALL ejecutar solo la aplicación, usando el servidor de correo (NFR-007) y la base de datos (NFR-008) de tecnoweb; no SHALL levantar una base de datos ni un servidor de correo propios.

#### Scenario: Despliegue de producción

- **WHEN** se despliega el entorno de producción
- **THEN** corre un único contenedor, el de la aplicación, que lee el buzón del sistema en `mail.tecnoweb.org.bo` y guarda sus datos en `db_grupo03sc` de `db.tecnoweb.org.bo`

### Requirement: Configuración obligatoria fuera del repositorio

El entorno de producción SHALL tomar las direcciones, usuarios y contraseñas del correo y de la base de variables de entorno del servidor de despliegue, y SHALL negarse a desplegar si falta alguna obligatoria.

#### Scenario: Variable obligatoria ausente

- **WHEN** se despliega sin definir la contraseña de la base de datos
- **THEN** el despliegue falla indicando la variable que falta, y no arranca ninguna versión con configuración incompleta

### Requirement: Archivos conservados entre despliegues

El entorno de producción SHALL conservar los archivos guardados (documentos adjuntos y sus versiones) cuando se vuelve a desplegar o se reinicia.

#### Scenario: Redespliegue con documentos registrados

- **WHEN** hay trámites con documentos y se despliega una versión nueva
- **THEN** los documentos siguen disponibles para la versión nueva

### Requirement: Despliegue desde la rama entregada

El entorno de producción SHALL ejecutar la versión de la aplicación que está en la rama `main`.

#### Scenario: Entrega integrada en main

- **WHEN** se integra `develop` en `main` y se despliega el entorno de producción
- **THEN** el entorno ejecuta la aplicación construida desde ese commit de `main`

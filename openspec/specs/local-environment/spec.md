# local-environment Specification

## Purpose

Entorno de desarrollo local que levanta la base, el correo y el webmail con `./gradlew bootRun` y arranca la aplicación con datos limpios y cuentas de prueba, como el entorno de pruebas desplegado.

## Requirements

### Requirement: Entorno local limpio en cada arranque

En el entorno local, cada vez que arranca la aplicación el sistema SHALL vaciar la base de datos y la carpeta de archivos guardados, aplicar todas las migraciones desde cero y volver a crear las cuentas de prueba.

#### Scenario: Nuevo arranque con datos de una sesión anterior

- **WHEN** hay trámites con documentos de una sesión anterior y se vuelve a ejecutar `./gradlew bootRun`
- **THEN** al arrancar no queda ningún trámite, la carpeta de archivos está vacía y solo existen las cuentas de prueba

#### Scenario: GreenMail recreado

- **WHEN** el contenedor de GreenMail se recreó y vuelve a numerar los mensajes desde 1, y luego arranca la aplicación
- **THEN** los comandos nuevos se ejecutan, porque la base no conserva correos con esos números

### Requirement: Solo en entornos desechables

El sistema SHALL vaciar la base y la carpeta de archivos solo en el entorno local y en el entorno de pruebas desplegado; en cualquier otra configuración, como producción, SHALL conservarlos.

#### Scenario: Arranque en producción

- **WHEN** la aplicación arranca sin los perfiles del entorno local ni del entorno de pruebas
- **THEN** la base y la carpeta de archivos quedan como estaban

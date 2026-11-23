# Spec Delta

## Purpose

Ofrecer un entorno de pruebas compartido, desplegado en un servidor, donde los testers prueban los comandos por correo con solo un navegador, sin instalar el stack de desarrollo.

## ADDED Requirements

### Requirement: Acceso de los testers por webmail

El entorno de pruebas SHALL publicar un webmail en un dominio accesible desde internet, sin autenticación adicional, desde el que se puede entrar al buzón de cualquier dirección del entorno, enviar comandos al buzón del sistema y leer sus respuestas.

#### Scenario: Comando enviado desde el webmail

- **WHEN** un tester entra al webmail como `admin@test.local` y envía un comando válido a `sistema@cpms.local`
- **THEN** el sistema procesa el comando y la respuesta llega al buzón de `admin@test.local`, visible en el mismo webmail

### Requirement: Servicios internos no expuestos

El entorno de pruebas SHALL mantener el servidor de correo (SMTP, POP3, IMAP) y la base de datos accesibles solo desde los servicios del propio entorno; ningún puerto de ellos se publica en el servidor.

#### Scenario: Conexión externa al servidor de correo

- **WHEN** alguien intenta conectarse desde fuera del servidor a SMTP, POP3, IMAP o PostgreSQL del entorno
- **THEN** la conexión no es posible, porque esos puertos no están publicados

### Requirement: Cuentas de prueba del entorno

Al arrancar, el entorno de pruebas SHALL crear las mismas cuentas de prueba activas que el entorno local (administrador, administrativo, director, docente de Taller de Grado, cinco docentes y tres estudiantes del dominio `test.local`), sin enviar avisos.

#### Scenario: Arranque

- **WHEN** el entorno arranca
- **THEN** existen las cuentas de prueba de cada rol, activas, y ningún buzón recibe aviso de alta

### Requirement: Entorno limpio en cada arranque

El entorno de pruebas SHALL ser desechable: cada vez que arranca la aplicación (por un despliegue o un reinicio), SHALL vaciar la base de datos y los archivos guardados, aplicar todas las migraciones desde cero y volver a crear las cuentas de prueba.

#### Scenario: Redespliegue con trámites abiertos

- **WHEN** hay trámites con documentos registrados y el entorno se despliega de nuevo
- **THEN** al arrancar no queda ningún trámite ni documento, y solo existen las cuentas de prueba

#### Scenario: Cuenta modificada por un tester

- **WHEN** un tester desactivó `docente1@test.local` y la aplicación se reinicia
- **THEN** `docente1@test.local` vuelve a existir activa, como las demás cuentas de prueba

### Requirement: Despliegue desde la rama de integración

El entorno de pruebas SHALL ejecutar la versión de la aplicación que está en la rama `develop`.

#### Scenario: Cambio integrado en develop

- **WHEN** se integra un cambio en `develop` y se despliega el entorno
- **THEN** el entorno ejecuta la aplicación construida desde ese commit de `develop`

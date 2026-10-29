# Proposal

## Why

Todavía no hay código: `mail-app`, la prueba de concepto por correo (CON-001, AD-001), solo existe en el modelo de diseño. Antes de implementar el primer caso de uso (UC-001) hace falta el esqueleto que comparten todos los casos de uso, y conviene probar primero, aislados, los riesgos técnicos: el sondeo POP3 y el envío SMTP sin cifrado (CON-006), y Java 25 con Spring Boot 4 (AD-003).

## What Changes

- Nuevo proyecto `mail-app` (Java 25, Spring Boot 4) con la arquitectura hexagonal de AD-002 y un paquete por subsistema (`messaging`, `audit`, `users`).
- Messaging: sondeo del buzón POP3 cada 30 segundos (configurable), interpretación del comando del asunto `Comando(arg1; arg2; …)`, registro de comandos con sus manejadores, respuesta al remitente y envío de notificaciones por SMTP (AD-005, AD-006).
- Identificación del remitente por su dirección de correo y autorización por los roles que declara cada comando (AD-007).
- Audit: servicio de registro de cambios (AD-008).
- Persistencia en PostgreSQL con Spring Data JPA y migraciones con Flyway (AD-004, nueva AD-011); claves UUID versión 7 (AD-010). Esta propuesta crea las tablas `user_accounts`, `user_account_roles` y `change_log_entries`.
- Cuenta de administrador inicial creada al arrancar desde la configuración (FR-038).
- Comando de diagnóstico `Ping()` para comprobar el circuito completo de correo sin lógica de negocio.
- Docker Compose para el servidor de pruebas de desarrollo: `mail-app`, PostgreSQL y un servidor de correo local con POP3, SMTP e interfaz web. La configuración por variables de entorno permite apuntar a `mail.tecnoweb.org.bo` y `db.tecnoweb.org.bo` sin cambiar el código.
- Pruebas automáticas con GreenMail (correo) y Testcontainers (PostgreSQL).

Fuera de esta propuesta: los comandos de gestión de usuarios de UC-001 y el subsistema GraduationProcesses, que llegan con el cambio de UC-001.

## Capabilities

### New Capabilities

- `mail-commands`: recepción de comandos por correo, interpretación del asunto, identificación y autorización del remitente, y respuesta por correo.
- `mail-notifications`: envío de avisos por correo a un usuario, solo cuando el cambio que los origina se confirma.
- `change-log`: registro de cada cambio del sistema con quién lo hizo, cuándo y sobre qué dato (NFR-SE-002).
- `user-accounts`: cuentas de usuario usadas para identificar al remitente; en esta propuesta, solo la cuenta de administrador inicial. UC-001 agregará la gestión de cuentas.

### Modified Capabilities

None.

## Impact

- Código nuevo: carpeta `mail-app/` en la raíz del repositorio, con su build, código, migraciones, pruebas y `Dockerfile`.
- Despliegue nuevo: `docker-compose.yml` y `.env.example` para el servidor de pruebas de desarrollo.
- Modelo: `docs/model/design/architecture.md` (AD-011 Flyway), `docs/model/design/deployment.md` (nodos, conexiones y entornos) y `docs/model/design/database.md` (tablas que esta propuesta crea).
- Dependencias externas: Spring Boot 4, Spring Data JPA, Flyway, Jakarta Mail, driver de PostgreSQL, GreenMail y Testcontainers.

# Tasks

## 1. Proyecto base

- [x] 1.1 Crear `mail-app/` con Gradle (Kotlin DSL), el wrapper, el toolchain de Java 25 y Spring Boot 4 (web no, solo starter base, Data JPA, Mail, Flyway, PostgreSQL), y verificar que `./gradlew build` compila y que la aplicación vacía arranca
- [x] 1.2 Crear la estructura de paquetes de D2 (`messaging`, `audit`, `users` con sus capas) y `application.properties` con las variables de entorno de D9, y verificar que `./gradlew build` sigue pasando
- [x] 1.3 Agregar las dependencias de prueba (JUnit 5, GreenMail, Testcontainers PostgreSQL) y una prueba `@SpringBootTest` que arranca el contexto contra PostgreSQL 17 en Testcontainers, y verificar que pasa con `./gradlew test`

## 2. Persistencia y migraciones

- [x] 2.1 Escribir `V1__crear_cuentas_de_usuario.sql` con `user_accounts` y `user_account_roles` según `database.md` (UUID, índices únicos, checks), y verificar con una prueba de integración que Flyway la aplica y que un correo repetido viola el índice único
- [x] 2.2 Escribir `V2__crear_registro_de_cambios.sql` con `change_log_entries`, sus índices y el trigger que rechaza `UPDATE` y `DELETE` (D7), y verificar con una prueba que un `UPDATE` directo falla
- [x] 2.3 Escribir `V3__crear_correos_procesados.sql` con `processed_mail_messages` (Message-ID único y fecha), y verificar que Flyway aplica las tres migraciones en orden

## 3. Users: cuentas y remitentes

- [x] 3.1 Implementar `UserAccount` (entidad JPA con UUID versión 7), `Role`, `AccountStatus` y `UserAccountRepository`, guardando la dirección de correo en minúsculas, y verificar con pruebas de integración los escenarios de "Dirección de correo única" de `user-accounts`
- [x] 3.2 Implementar el `SenderAuthenticator` de Users (solo cuentas activas, dirección sin distinguir mayúsculas), y verificar con pruebas que un remitente desconocido o desactivado no se autentica
- [x] 3.3 Implementar el administrador inicial (D8), y verificar con pruebas los tres escenarios de "Administrador inicial" de `user-accounts`, incluida la entrada en el registro de cambios

## 4. Audit: registro de cambios

- [x] 4.1 Implementar `ChangeLog.record` y `FieldChange` según `subsystems/audit.md`, y verificar con pruebas los escenarios de "Registro de cada cambio" y "Registro en la misma transacción" de `change-log`
- [x] 4.2 Verificar con una prueba el escenario de "Registro inalterable" de `change-log` a través del repositorio y de SQL directo

## 5. Messaging: comandos y respuestas

- [x] 5.1 Implementar el analizador del asunto `Comando(arg1; arg2; …)` y `MailCommand`, y verificar con pruebas unitarias los escenarios de "Interpretación del comando del asunto" de `mail-commands`
- [x] 5.2 Implementar los filtros de correos que no se atienden (destinatario, remitente propio, `Auto-Submitted`), y verificar con pruebas unitarias los escenarios de "Correos que no se atienden"
- [x] 5.3 Implementar `CommandRegistry`, `CommandHandler` con sus roles permitidos y `SenderAuthenticator` como puertos (D5), con el despacho que autentica, autoriza y ejecuta en una transacción, y verificar con pruebas los escenarios de "Identificación del remitente" y "Autorización por roles del comando"
- [x] 5.4 Implementar el envío de respuestas por SMTP (`Re: <asunto>`, mensaje genérico ante errores internos), y verificar con GreenMail los escenarios de "Respuesta por correo"
- [x] 5.5 Implementar el sondeo POP3 con `@Scheduled` y un solo hilo, la idempotencia por Message-ID y el borrado después de confirmar (D4), y verificar con GreenMail los escenarios de "Sondeo del buzón" y "Adjuntos del comando"
- [x] 5.6 Implementar `Notifier` con envío después de confirmar (D6), y verificar con GreenMail los escenarios de `mail-notifications`

## 6. Comando Ping

- [x] 6.1 Implementar el comando `Ping()` en Users (cualquier cuenta activa, responde `Pong`), y verificar con una prueba de punta a punta en GreenMail el escenario "Ping" de `mail-commands`

## 7. Stack de pruebas de desarrollo

- [x] 7.1 Escribir el `Dockerfile` de dos etapas de `mail-app` (D10), y verificar que `docker build` produce una imagen que arranca
- [x] 7.2 Escribir `docker-compose.yml` (postgres, mailpit y mail-app), `.env.example` y la entrada de `.env` en `.gitignore`, y verificar que `docker compose up -d --build` deja los tres servicios sanos
- [x] 7.3 Documentar en `mail-app/README.md` cómo levantar el stack y enviar un comando por Mailpit, y verificar siguiendo esos pasos que `Ping()` desde la dirección del administrador inicial recibe `Pong` en la interfaz web de Mailpit

## 8. Modelo

- [x] 8.1 Registrar AD-011 (Flyway) en `docs/model/design/architecture.md`, y verificar que la tabla de decisiones la cita
- [x] 8.2 Actualizar `docs/model/design/subsystems/messaging.md` con `CommandHandler` (roles permitidos y `Sender`) y el puerto `SenderAuthenticator` implementado por Users (D5), y `users.md` con el puerto que implementa, y verificar que no hay ciclos ni dependencias de un subsistema general hacia uno de negocio
- [x] 8.3 Agregar `processed_mail_messages` y el trigger de `change_log_entries` a `docs/model/design/database.md`, y verificar que coincide con las migraciones
- [x] 8.4 Completar `docs/model/design/deployment.md` con el servidor de pruebas de desarrollo (nodos, asignación, conexiones y entorno), y verificar que cada aplicación de `architecture.md` está asignada a un nodo

## Workflow follow-up

- Archivar el cambio con `/opsx:archive` después de integrarlo en `develop`, para que las especificaciones pasen a `openspec/specs/`.
- Crear el cambio de UC-001, que ajusta su realización de diseño a D5.

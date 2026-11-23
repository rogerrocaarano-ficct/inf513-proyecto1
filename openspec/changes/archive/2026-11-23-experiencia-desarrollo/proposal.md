# Proposal

## Why

El entorno local de desarrollo tiene tres fricciones. El puerto de PostgreSQL cambia en cada arranque, así que hay que reconfigurar el cliente de base de datos cada vez. Dos tests no compilan en el compilador del IDE (Eclipse/ECJ, el de la extensión Java de VS Code), aunque Gradle sí los compila, y el editor los marca como errores. Y la base local arranca solo con el administrador: para probar cualquier comando hay que crear antes a mano, por correo, las cuentas de cada rol.

Es trabajo compartido, sin caso de uso propio. El seed apoya la prueba manual de todos los casos de uso, en especial UC-001 (gestión de usuarios) y UC-003 (asignación de tutor).

## What Changes

- `compose.yaml`: PostgreSQL publica siempre en el puerto 15432 del host, en lugar de uno libre que elige Docker. No se usa el 5432 para no chocar con un PostgreSQL instalado en la máquina. `README.md` documenta el puerto y las credenciales.
- Tests `JpaReceivedEmailRepositoryTest.attachmentTableKeepsNoContent` y `JpaDegreeProcedureRepositoryTest.entitiesKeepNoFileContent`: se declara el tipo del `map` para que compilen también con ECJ. No cambia lo que comprueban.
- Cuentas de prueba del entorno local: con el perfil `local` (`./gradlew bootRun`), el sistema crea al arrancar un administrador, un administrativo, un docente de Taller de Grado, un director, cinco docentes y tres estudiantes, todos con direcciones `@test.local`. No envía avisos por correo y no toca las cuentas que ya existen. En producción y en los tests no se crea ninguna.
- El administrador inicial del perfil `local` pasa de `admin@cpms.local` a `admin@test.local`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `user-accounts`: se añade el requisito de las cuentas de prueba que se crean al arrancar con el perfil local.

## Impact

- Código: una clase nueva en `users.infrastructure`, que arranca solo con el perfil `local`; `application-local.properties`; dos tests de persistencia.
- Entorno: `compose.yaml` y `README.md` (sección "Entorno de desarrollo"). Quien tenga levantado el stack tiene que recrear el contenedor de PostgreSQL (`docker compose up -d postgres`) para que tome el puerto nuevo. Los datos se conservan en el volumen.
- Las bases locales que ya existen conservan `admin@cpms.local` junto al nuevo `admin@test.local`.
- Sin dependencias nuevas ni migraciones.
- Modelo EA: es trabajo compartido sin caso de uso, y todo lo que se crea está en infraestructura, que no se modela en EA. No hay elementos ni diagramas de EA en los que se base.

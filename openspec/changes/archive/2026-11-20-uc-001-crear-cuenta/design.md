# Design

## Context

Ver `proposal.md` (Why) y `specs/user-accounts/spec.md`. El canal de correo (`mail-commands`) ya está implementado: un caso de uso se conecta declarando un bean `EmailCommandHandler`; el canal valida la cantidad de argumentos y adjuntos, ejecuta `handle` en una transacción junto con el estado del correo, y responde los errores (`DomainException` con su mensaje, `DomainValidationException` con un error por campo). Las respuestas de éxito las envía el caso de uso, después del commit.

El diseño sigue el diagrama de clases `Design/Users` "Users" y las notas de sus elementos en EA, que describen las reglas de `User`, `Role`, `RolePermission` y `UserRepository`. Este documento no las repite: explica cómo se implementan.

## Goals / Non-Goals

**Goals:**
- Flujo básico de UC-001 completo por correo, de punta a punta, con las clases del modelo.
- Reglas de autorización, validación, normalización y unicidad en el dominio y el repositorio, como dice el modelo.
- Un administrador inicial para poder usar el sistema desde el primer arranque.

**Non-Goals:**
- Flujos alternos A1 a A5, estado de la cuenta (FR-032), roles múltiples.
- Contraseñas: el usuario se identifica por su dirección de correo (nota de FR-035).

## Decisions

### D1. Flujo entre las clases del modelo

```
 InboxPollerService (mail) --handle(email)--> SignUpUserCommandHandler       users.application
                                                 | SignUpUserCommand(args en orden)
                                                 v
                                          UserManagementUseCase.signUpUser(requesterEmail, command)
                                                 | requester = users.getByEmail(requesterEmail)   --> vacío: UserNotAuthorizedException
                                                 | role = Role por etiqueta                        --> desconocida: DomainValidationException
                                                 | user = User.builder().with*(...).withRole(role)
                                                 | user.signUp(requester)                          --> permiso, validaciones, evento
                                                 | users.insert(user)                              --> duplicados: DomainValidationException
                                                 | eventBus.notifyUserSignedUpEvent(evento)
                                                 v
                                          commit (junto con PROCESSED) --> aviso por correo (D5)
```

- `SignUpUserCommandHandler`: `actionKey()` = `usuarios.crearCuenta`, `requiredArgs()` = 8, `requiredFiles()` = 0. Toma los argumentos de `ReceivedEmail.parseArgs()` en el orden de la nota de `SignUpUserCommand` (`firstName; lastName; secondLastName; ci; code; email; phoneNumber; role`) y pasa `email.getFrom()` como solicitante.
- `SignUpUserCommand` es un `record` de Java con esos 8 campos.
- `UserManagementUseCase` es un servicio sin anotaciones de Spring, creado en la configuración de infraestructura, como `InboxPollerService`.

### D2. `Role` y `RolePermission` como enums

En el modelo, los valores son operaciones constantes en mayúsculas (`STUDENT(): Role`, `SIGNUP_STUDENT(): RolePermission`): en Java son `enum`.

- `Role(label)`: `STUDENT("estudiante")`, `ADMINISTRATIVE("administrativo")`, `PROFESSOR("docente")`, `CAPSTONE_PROJECT_PROFESSOR("docente_taller")`, `DIRECTOR("director")`, `SYSTEM_ADMIN("administrador")`. `isAllowedTo(permission)` delega en `permission.isGrantedTo(this)`; `getPermissions()` devuelve los permisos que incluyen al rol.
- `RolePermission(label, allowedRoles)`: los seis `SIGNUP_*` con los conjuntos de la nota de `RolePermission`. `isGrantedTo(role)` = `allowedRoles.contains(role)`.
- `User.hasPermissionTo(permission)` = `role.isAllowedTo(permission)`.
- Para elegir el permiso de la cuenta nueva, `signUp` usa el permiso `SIGNUP_` cuyo nombre corresponde al rol pedido (un mapa fijo `Role -> RolePermission`).
- La conversión de la etiqueta del comando a `Role` la hace la operación estática `Role.fromLabel(label)`, sin distinguir mayúsculas; una etiqueta desconocida es una `DomainValidationException` en el campo `rol`.

### D3. Validación en `User.signUp`

Orden: primero el permiso (un solicitante sin permiso no recibe detalles de validación), después los datos.

1. Si `!requester.hasPermissionTo(permiso del rol nuevo)`: `UserNotAuthorizedException(requesterEmail, acción)`, con el mensaje "No tiene permiso para crear cuentas con rol <etiqueta>.".
2. Se normalizan nombres y apellidos: `strip`, espacios internos colapsados a uno, y cada palabra con su primer carácter en mayúscula (`Character.toTitleCase`) y el resto en minúscula (`Locale` de español).
3. Se validan todos los campos y se acumulan los errores en un `Map` ordenado por campo; si hay alguno, `DomainValidationException(errores)`:
   - nombres y apellidos: `^[\p{L}]+( [\p{L}]+)*$` tras normalizar; `firstName` y `lastName` obligatorios;
   - correo: `^[^@\s]+@[^@\s]+\.[^@\s]+$`;
   - teléfono `^\d{1,8}$`, código `^\d{1,10}$`, CI `^\d{1,7}$`; obligatorios para `STUDENT`; en los demás roles, vacío se guarda como ausente y, si tiene valor, se valida igual.
4. Se registra `UserSignedUpEvent` con los datos de la cuenta nueva y del solicitante.

Los nombres de campo en los errores son los que ve el usuario, en español: `nombres`, `primer apellido`, `segundo apellido`, `CI`, `código`, `correo`, `teléfono`, `rol`.

El correo se guarda y se busca en minúsculas (nota de `User.emailAddress`): las direcciones llegan del encabezado `From`, que puede variar en mayúsculas, y la cuenta debe reconocerse igual.

### D4. Persistencia

Migración `V2__user_account.sql`. La tabla se llama `user_account` porque `user` es palabra reservada en PostgreSQL.

| Columna | Tipo | Restricción |
| --- | --- | --- |
| `id` | `uuid` | PK |
| `email_address` | `varchar(320)` | `NOT NULL UNIQUE`, en minúsculas |
| `role` | `varchar(30)` | `NOT NULL`, nombre de la constante |
| `first_name`, `last_name` | `varchar(100)` | `NOT NULL` |
| `second_last_name` | `varchar(100)` | nulo si vacío |
| `ci` | `varchar(7)` | nulo si vacío |
| `uagrm_code` | `varchar(10)` | `UNIQUE`, nulo si vacío (varias cuentas sin código no chocan) |
| `phone_number` | `varchar(8)` | nulo si vacío |
| `created_at` | `timestamptz` | `DEFAULT now()` |

`JpaUserRepository` (en `users.infrastructure`) implementa `UserRepository` con una entidad JPA propia y un mapeador, como en `mail`. `insert` y `update` consultan antes si el correo o el código pertenecen a otra cuenta y, si es así, lanzan `DomainValidationException` con un error por dato repetido; las restricciones `UNIQUE` quedan como respaldo. `getByEmail` y `getById` devuelven `Optional<User>` (0..1 en el modelo).

### D5. Aviso de la cuenta creada

`UserManagementEventBus` lo implementa `MailUserManagementEventBus` en `users.infrastructure`:

- `notifyUserSignedUpEvent(event)` publica el evento como `ApplicationEvent` de Spring.
- Un método `@TransactionalEventListener(phase = AFTER_COMMIT)` envía dos correos por `EmailSender`:
  - al solicitante (`requesterUserEmail`): "Cuenta creada", con nombre completo, correo y rol de la cuenta nueva;
  - a la cuenta nueva (`signedUpUser`): "Su cuenta en el sistema", con su rol y el aviso de que puede enviar comandos desde esa dirección.
- Si la transacción hace rollback, el listener no se ejecuta y no sale ningún correo. Un fallo de envío se registra en el log y no deshace la cuenta.

### D6. Administrador inicial

`InitialAdminAccount` (`ApplicationRunner` en `users.infrastructure`) lee `cpms.users.admin-email` (`${CPMS_ADMIN_EMAIL:}`). Si tiene valor y `getByEmail` no encuentra la cuenta, crea con `User.builder()` una cuenta `SYSTEM_ADMIN` con nombres `Administrador` y apellido `Sistema` y la guarda con `insert`, sin `signUp` (no hay solicitante ni evento). En `application-local.properties`: `cpms.users.admin-email=admin@cpms.local`. Es infraestructura: no se modela en EA.

### D7. Documentos

- REQUIREMENTS.md: FR-026 (un solo rol), FR-036 (se reemplaza la regla de combinaciones por "un solo rol por cuenta"), FR-050 (también estudiantes y administrativos), FR-051 (también en UC-001) y las notas correspondientes.
- GLOSSARY.md: término adoptado para "Cuenta de usuario" (`User`), "Rol" (`Role`), "Registro universitario" (`uagrmCode`) y "Cédula de identidad" (`ci`).
- README.md: la cuenta `admin@cpms.local` del entorno local y un comando de ejemplo.
- USER-GUIDE.md (nuevo): cómo se escriben los comandos por correo, las respuestas del sistema y `usuarios.crearCuenta` con sus argumentos, formatos, roles y ejemplos.

## Risks / Trade-offs

- [El remitente se identifica solo por el encabezado `From`, que puede falsificarse] → Aceptado para la prueba de concepto (igual que en `mail-commands`).
- [Un solo rol por cuenta cambia requisitos aprobados (FR-026, FR-036)] → Se actualiza REQUIREMENTS.md en el mismo change; el director actúa como docente porque los permisos de docente incluirán a DIRECTOR cuando se definan.
- [Validar el correo con una expresión simple acepta direcciones raras] → Suficiente para la prueba de concepto; si la dirección no existe, el aviso rebota y el canal de correo lo ignora.
- [El administrador inicial no tiene datos reales] → Se pueden completar más adelante con A1 (actualizar información).

## Migration Plan

Flyway aplica `V2__user_account.sql` al arrancar. Al primer arranque con `CPMS_ADMIN_EMAIL` definido se crea la cuenta de administrador. Para revertir: desplegar la versión anterior y eliminar la tabla `user_account`.

## Diferencias con el modelo

1. **`SignUpUserCommand`** es un `record`: sus atributos en EA son públicos y sin operaciones; en Java se leen con los accesores del record (`firstName()`, …).
2. **Búsquedas devuelven `Optional<User>`**: en EA, `getByEmail` y `getById` devuelven `User` con multiplicidad 0..1 en la nota.
3. **`MailUserManagementEventBus` e `InitialAdminAccount`** son infraestructura y no se modelan.
4. **Agregados durante la implementación** (detectados con `ea-drift users` y ya agregados en EA): `User.withCi` y `User.getFullName`; `RolePermission.toSignUp`; `UserNotAuthorizedException.notRegistered`. Quedan por completar a mano en EA los parámetros de `withCi` (`ci: string`) y `toSignUp` (`role: Role`), y la marca de estáticos en `toSignUp` y `notRegistered`, porque el MCP no puede hacerlo.
5. **Constantes de apoyo que no se modelan a propósito**: los nombres de campo de los errores de validación (`User.FIRST_NAME` … `User.PHONE`, `Role.FIELD`), `UserNotAuthorizedException.ERROR_CODE` y `SignUpUserCommandHandler.ACTION`. Son detalles de implementación para armar mensajes y validaciones, no parte del diseño.
6. **`Role.Equals(role)`** del modelo lo cubre la igualdad propia de un `enum` de Java; no se escribe.

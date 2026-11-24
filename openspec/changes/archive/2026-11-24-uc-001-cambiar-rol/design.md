# Design

## Context

El subsistema `users` ya tiene el flujo básico (crear), A2 (desactivar y activar) y A3 (listar), y la ayuda (`CommandHelpUseCase`). Las piezas que este cambio reutiliza:

- `UserManagementUseCase` tiene `requireActiveRequester` (E1), y en `disableUser` las comprobaciones de la única cuenta de administrador activa (FR-038) y de la propia cuenta, en ese orden. `requireAffectedUser` busca la cuenta afectada (E4) y comprueba un permiso `toChangeStatus` fijo.
- `RolePermission` tiene una familia de permisos por operación y rol (`SIGNUP_*`, `LIST_*`, `CHANGE_STATUS_*`) con una fábrica estática por familia (`toSignUp`, `toList`, `toChangeStatus`).
- `User.validate()` es privado y solo lo llama `signUp`. Exige CI, código y teléfono cuando `role == STUDENT`, con el mensaje «Es obligatorio para estudiantes.» por campo.
- `Role.fromLabel` traduce la etiqueta sin distinguir mayúsculas y rechaza una desconocida con la lista de roles aceptados.
- `JpaUserRepository.update` ya copia el rol (`entity.setRole`). No hace falta migración.
- Cada handler documentado expone un `CommandGuide`; `CommandGuide.forEveryRole(fábrica)` arma el conjunto de permisos del comando, y la ayuda lo muestra a quien tiene alguno.

UC-001 A1 no tiene diseño en EA, y el modelo no se consulta mientras está en pausa: este diseño sigue la arquitectura de referencia de AGENTS.md.

## Goals / Non-Goals

**Goals:**

- Cambiar el rol de una cuenta por correo con el permiso doble, los rechazos y los avisos de la spec.
- Que la ayuda muestre el comando solo a quien puede usarlo.

**Non-Goals:**

- Modificar los datos de la cuenta (resto de A1).
- FR-034: el subsistema `users` no sabe en qué trámites participa una cuenta, y consultarlo exige un puerto que `procedures` implemente (hoy `procedures` depende de `users`, no al revés). Queda para un cambio que lo resuelva en A1 y A2.
- Historial de cambios de rol (FR-052, UC-007).

## Decisions

### D1. Flujo de `changeUserRole`

`UserManagementUseCase.changeUserRole(requesterEmail, ChangeUserRoleCommand command)` es el flujo A1 (solo rol). Las comprobaciones que no leen la cuenta afectada van primero, como en `disableUser`:

```
requester = requireActiveRequester(requesterEmail)               // E1
newRole   = Role.fromLabel(command.role())                       // rol desconocido: error en "rol"
user      = requireUser(command.userEmail())                     // E4: error en "correo"
requirePermission(requester, RolePermission.toChangeRole(user.getRole()))   // E1, rol actual
requirePermission(requester, RolePermission.toChangeRole(newRole))          // E1, rol nuevo
si isOnlyActiveAdmin(user) y newRole != SYSTEM_ADMIN: error en "correo" (E5, FR-038)
si user es el requester: error en "correo" (E6)
user.changeRole(requester, newRole)                              // mismo rol, datos del estudiante
users.update(user)
evento -> notifier.notifyUserRoleChanged
```

- **Rol actual antes que el nuevo:** quien no puede gestionar la cuenta recibe el rechazo sobre su rol actual, sin enterarse de nada más.
- **FR-038 antes que la propia cuenta:** el mismo argumento que en A2. Solo un administrador puede quitar el rol de administrador, así que la única cuenta de administrador activa solo puede pedirlo para sí misma; si la propia cuenta fuera primero, FR-038 nunca respondería con su mensaje.
- **Cuenta afectada desactivada:** se le puede cambiar el rol. A1 no lo prohíbe, y FR-038 solo cuenta las cuentas activas.
- **Alternativa descartada:** reutilizar `toSignUp` para los dos permisos. Coincide en los roles del director y del administrador, pero el administrativo tiene `SIGNUP_STUDENT` y la ayuda le mostraría un comando que nunca puede usar (de estudiante a estudiante no hay cambio), y el rechazo diría «crear cuentas de X» (D3).

### D2. Comprobaciones compartidas con A2

Se separa `requireAffectedUser` en dos métodos privados de `UserManagementUseCase`: `requireUser(email)` (E4) y `requirePermission(requester, permission)` (E1). Los usan `disableUser`, `enableUser` y `changeUserRole`. La condición de FR-038 pasa a `isOnlyActiveAdmin(user)` (cuenta de administrador activa y única activa), que usan `disableUser` y `changeUserRole` con su propio mensaje. Los mensajes de A2 no cambian.

### D3. Permisos `CHANGE_ROLE_*`

Seis constantes nuevas en `RolePermission`, con la etiqueta «cambiar el rol de cuentas de <rol>», y `toChangeRole(role)` estática con `switch` exhaustivo, como `toChangeStatus`:

| Permiso | Roles |
| --- | --- |
| `CHANGE_ROLE_STUDENTS`, `CHANGE_ROLE_ADMINISTRATIVES`, `CHANGE_ROLE_PROFESSORS`, `CHANGE_ROLE_CAPSTONE_PROJECT_PROFESSORS` | `DIRECTOR`, `SYSTEM_ADMIN` |
| `CHANGE_ROLE_DIRECTORS`, `CHANGE_ROLE_ADMINS` | `SYSTEM_ADMIN` |

`CHANGE_ROLE_STUDENTS` no incluye `ADMINISTRATIVE`, aunque UC-001 diga que la secretaría «modifica» cuentas de estudiante: esa parte de A1 es la de los datos, fuera de este cambio.

### D4. El agregado cambia el rol y emite el evento

- `User.changeRole(requester, newRole)`:
  - rechaza con error en `rol` («La cuenta X ya tiene el rol Y.») si `newRole` es el rol actual;
  - valida la cuenta con el rol nuevo antes de asignarlo: `validate()` pasa a `validate(Role role)`, y `signUp` lo llama con su propio rol. Así, si pasa a estudiante sin CI, código o teléfono, el error trae un mensaje por campo («Es obligatorio para estudiantes.») y el rol no cambia;
  - asigna el rol y emite `UserRoleChangedEvent`.
- El permiso, la propia cuenta y FR-038 los comprueba el caso de uso (D1), como en A2: dependen del solicitante o de otras cuentas.
- `UserRoleChangedEvent(changedUserEmail, changedUserFullName, previousRole, newRole, occurredOn, requesterUserFullName, requesterUserEmail)` es un record en `users.domain`; los roles van como etiqueta (`String`), igual que en `UserSignedUpEvent`.

### D5. Los rechazos van a un campo del comando

Como en A2, los rechazos son `DomainValidationException` sobre el campo del argumento al que se refieren: E4, E5 y E6 sobre `correo` (`User.EMAIL`); el rol desconocido y el mismo rol sobre `rol` (`Role.FIELD`); los datos del estudiante sobre `CI`, `código` y `teléfono`. Mensajes:

| Caso | Campo | Mensaje |
| --- | --- | --- |
| E4 | `correo` | «No existe una cuenta con el correo X.» (el de A2) |
| E5 | `correo` | «No se puede quitar el rol de administrador a la única cuenta de administrador activa.» |
| E6 | `correo` | «No puede cambiar el rol de su propia cuenta.» |
| Mismo rol | `rol` | «La cuenta X ya tiene el rol Y.» |

### D6. Contrato y handler

- `ChangeUserRoleCommand(userEmail, role)`, record en `users.application.usermanagement`: el comando tiene dos argumentos, así que no cabe la excepción de argumento único.
- `ChangeUserRoleCommandHandler` (`usuarios.cambiarRol`): 2 argumentos, 0 adjuntos; arma el record con `parseArgs()` y llama a `changeUserRole(email.getFrom(), command)`. Implementa `DocumentedCommand` con el guía `usuarios.cambiarRol[correo; rol]`, «Cambiar el rol de una cuenta.» y `CommandGuide.forEveryRole(RolePermission::toChangeRole)`. El director, que tiene cuatro de los seis permisos, ve en la ayuda los roles sobre los que puede actuar.
- Se declara como `@Bean` en `UsersConfiguration`, junto a los de A2.

### D7. Avisos

`UserManagementNotifier.notifyUserRoleChanged(event)`; `MailUserManagementNotifier` lo implementa como `notifyUserDisabled`: publica el evento y envía después del commit un correo al solicitante y otro a la cuenta afectada.

| Destino | Asunto | Cuerpo |
| --- | --- | --- |
| Solicitante | `Rol cambiado: <nombre>` | «Se cambió el rol de la cuenta de <nombre> (<correo>) de <rol anterior> a <rol nuevo>.» |
| Afectado | `Su rol en el sistema de trámites de titulación cambió` | «Hola, <nombre>: <solicitante> cambió el rol de su cuenta de <rol anterior> a <rol nuevo>. Desde ahora puede usar los comandos de ese rol; envíe ayuda[] para verlos. Si cree que es un error, comuníquese con la Dirección de Carrera.» |

## Risks / Trade-offs

- [Cambio de rol con trámites en curso] → Un docente que es tutor y pasa a estudiante deja de tener `BE_PROJECT_TUTOR`, sin aviso (FR-034 fuera de alcance). Los trámites no cambian (postcondición de A1), pero sus comandos de tutor se rechazarán. Mitigación: el cambio pendiente de FR-034 para A1 y A2.
- [Mensaje de FR-038 sobre `correo`] → Igual que en A2, el usuario ve «- correo: No se puede quitar…», a cambio de no agregar una excepción nueva.
- [Carrera entre dos administradores que se quitan el rol mutuamente] → La misma de A2: el buzón se procesa en un solo hilo; si se paraleliza, hará falta un bloqueo.

## Migration Plan

Sin migración de datos ni de esquema. Se despliega con el resto de la aplicación; para volver atrás basta el despliegue anterior.

## Diferencias con el modelo

UC-001 sin diseño en EA; se sigue la arquitectura de referencia. Lo que el cambio crea o cambia, para llevarlo a EA con `ea-drift`:

- `Design/Users/domain`:
  - `User.changeRole(requester: User, newRole: Role)` (nueva).
  - `User.validate()` pasa a `validate(role: Role)` (privada).
  - `UserRoleChangedEvent` (record nuevo) con `changedUserEmail`, `changedUserFullName`, `previousRole`, `newRole`, `occurredOn`, `requesterUserFullName`, `requesterUserEmail`.
  - `RolePermission`: `CHANGE_ROLE_STUDENTS`, `CHANGE_ROLE_ADMINISTRATIVES`, `CHANGE_ROLE_PROFESSORS`, `CHANGE_ROLE_CAPSTONE_PROJECT_PROFESSORS`, `CHANGE_ROLE_DIRECTORS`, `CHANGE_ROLE_ADMINS` y `toChangeRole(role: Role)`.
- `Design/Users/application`:
  - `UserManagementUseCase.changeUserRole(requesterEmail, command: ChangeUserRoleCommand)` (nueva).
  - Métodos privados `requireUser`, `requirePermission` e `isOnlyActiveAdmin` en lugar de `requireAffectedUser` (D2).
  - `ChangeUserRoleCommand` (record nuevo: `userEmail`, `role`).
  - `ChangeUserRoleCommandHandler` (nuevo), que realiza `Design/Mail/application/EmailCommandHandler` y `DocumentedCommand`.
  - `UserManagementNotifier.notifyUserRoleChanged(event: UserRoleChangedEvent)` (nueva).

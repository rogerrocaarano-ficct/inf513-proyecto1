# Design

## Context

El subsistema `users` ya tiene el flujo básico (crear cuenta) y A3 (listar). Las piezas que A2 reutiliza:

- `UserManagementUseCase` busca al solicitante con `UserRepository.getByEmail` y, si no existe, lanza `UserNotAuthorizedException.notRegistered`. Lo hace por separado en `signUpUser` y en `listUsers`.
- `UserRepository.update(user)` ya existe en el código y en EA (`Design/Users/domain/UserRepository`), con el control de correo y código únicos; hasta ahora no lo usaba nadie.
- `InboxPollerService` ejecuta cada handler en una transacción. Una `DomainValidationException` se responde como «No se pudo ejecutar el comando "x":» con una línea `- campo: mensaje` por error; cualquier otra `DomainException`, con su mensaje.
- `MailUserManagementNotifier` publica eventos de Spring y envía los correos en un `@TransactionalEventListener(AFTER_COMMIT)`.
- La tabla `user_account` (migración `V2`) no tiene estado.

Elementos de EA que realizan A2 (diagrama `Design/Users` "UC-001-A2" y escenario "A2 Desactivar/Activar cuenta" de `UC-001 Gestión de usuarios`):

- `Design/Users/application`: `DisableUserCommandHandler`, `EnableUserCommandHandler` (realizan `Design/Mail/application/EmailCommandHandler`), `UserManagementUseCase.disableUser(requesterEmail, userEmail)` y `enableUser(requesterEmail, userEmail)`, `UserManagementNotifier.notifyUserDisabled(event)` y `notifyUserEnabled(event)`, `ListUsersQueryItem.active`.
- `Design/Users/domain`: `User` (`active`, `disable()`, `enable()`, `isActive()`, `withActive(active)`), `UserDisabledEvent` (`requesterUserEmail`, `requesterUserFullName`, `disabledUserEmail`, `disabledUserFullName`, `occurredOn`), `UserEnabledEvent` (`requesterUserEmail`, `requesterUserFullName`, `enabledUserEmail`, `enabledUserFullName`, `occurredOn`), `RolePermission` (`CHANGE_STATUS_*`, `toChangeStatus(role)`), `UserRepository` (`update`, `getByEmail`, `getByRole`).

## Goals / Non-Goals

**Goals:**

- Desactivar y activar cuentas por correo, con el alcance por rol, los rechazos y los avisos de la spec.
- Que una cuenta desactivada no pueda ejecutar ningún comando de `users`.
- Mostrar el estado en la lista de A3, incluidas las cuentas desactivadas.

**Non-Goals:**

- FR-034 (trámites en curso del usuario desactivado): los trámites todavía no existen.
- Bloquear a una cuenta desactivada en comandos de otros subsistemas: hoy solo existen los de `users`. Cada subsistema nuevo que reciba comandos tendrá que comprobar el estado del remitente.
- Guardar quién y cuándo cambió el estado (historial de cambios).

## Decisions

### D1. Flujo de `disableUser` y `enableUser`

Los dos métodos de `UserManagementUseCase` siguen el mismo orden; las comprobaciones que no leen la cuenta afectada van primero:

```
requester = requireActiveRequester(requesterEmail)        // E1: no registrado o desactivado
user      = users.getByEmail(userEmail)                   // si no existe: error en "correo"
permiso   = RolePermission.toChangeStatus(user.getRole())
si !requester.hasPermissionTo(permiso): UserNotAuthorizedException(email, permiso.getLabel())   // E1
solo disableUser:
  si user es SYSTEM_ADMIN activo y es el único activo: error en "correo" (FR-038)
  si user es el requester: error en "correo" (propia cuenta)
user.disable(requester) | user.enable(requester)          // rechaza si ya está en ese estado
users.update(user)
evento -> notifier.notifyUserDisabled | notifyUserEnabled
```

- **Última cuenta de administrador:** se cuenta con `users.getByRole(SYSTEM_ADMIN)` filtrando `isActive()`. No hace falta una operación nueva en `UserRepository`: hay pocas cuentas de administrador.
- **Último administrador antes que la propia cuenta:** solo un administrador activo puede desactivar a otro, así que la única cuenta de administrador activa solo puede pedirse a sí misma. Si la regla de la propia cuenta fuera primero, FR-038 nunca respondería con su propio mensaje.
- **Permiso antes que la propia cuenta y el último administrador:** quien no tiene permiso sobre el rol recibe E1 sin saber nada más de la cuenta. Un director nunca llega a las dos comprobaciones de `disableUser`, porque no tiene permiso sobre administradores ni directores.

### D2. `requireActiveRequester`, una sola verificación del solicitante

Un método privado de `UserManagementUseCase`, `requireActiveRequester(requesterEmail)`, devuelve la cuenta del solicitante o lanza `UserNotAuthorizedException.notRegistered(email)` si no existe y `UserNotAuthorizedException.disabled(email)` si está desactivada. Lo usan `signUpUser`, `listUsers`, `disableUser` y `enableUser`, en lugar de la búsqueda que hoy se repite en cada flujo.

`disabled(email)` es una fábrica nueva junto a `notRegistered`, con el mensaje «La cuenta X está desactivada. Comuníquese con la Dirección de Carrera.» y el mismo código de error (`USER_NOT_AUTHORIZED`), porque es el mismo E1.

### D3. El agregado cambia el estado y emite el evento

- `User.disable(requester)` y `User.enable(requester)` cambian `active`, rechazan si la cuenta ya estaba en ese estado y emiten `UserDisabledEvent` o `UserEnabledEvent`. Reciben al solicitante como `signUp(requester)`, porque el evento lleva su correo y su nombre completo. El permiso, la propia cuenta y el último administrador los comprueba el caso de uso (D1), como lo describe la nota de `UserManagementUseCase` en EA: dependen de otras cuentas o del rol de quien pide.
- `builder()` deja la cuenta activa; `rehydrate()` no asume nada y el repositorio llama a `withActive(entity.isActive())`.
- `isActive()` es el getter.
- Los eventos son records en `users.domain`, como `UserSignedUpEvent`: `UserDisabledEvent(disabledUserEmail, disabledUserFullName, occurredOn, requesterUserFullName, requesterUserEmail)` y `UserEnabledEvent(enabledUserEmail, enabledUserFullName, occurredOn, requesterUserFullName, requesterUserEmail)`.

### D4. Los rechazos de A2 son errores del campo `correo`

«No existe una cuenta con el correo X.», «La cuenta X ya está desactivada.» / «… ya está activa.», «No puede desactivar su propia cuenta.» y «No se puede desactivar la única cuenta de administrador activa.» se lanzan como `DomainValidationException` sobre el campo `User.EMAIL` («correo»), el único argumento del comando. La respuesta queda así:

```
No se pudo ejecutar el comando "usuarios.desactivar":
- correo: La cuenta ana@uagrm.bo ya está desactivada.
```

- **Alternativa descartada:** una excepción nueva de dominio para las reglas de estado. Daría un mensaje sin el prefijo del campo, pero es una clase que EA no tiene y que no agrega nada a lo que el usuario ve.

### D5. Permisos `CHANGE_STATUS_*`

Seis constantes nuevas en `RolePermission`, con la etiqueta «cambiar el estado de cuentas de <rol>»:

| Permiso | Roles |
| --- | --- |
| `CHANGE_STATUS_STUDENTS`, `CHANGE_STATUS_ADMINISTRATIVES`, `CHANGE_STATUS_PROFESSORS`, `CHANGE_STATUS_CAPSTONE_PROJECT_PROFESSORS` | `DIRECTOR`, `SYSTEM_ADMIN` |
| `CHANGE_STATUS_DIRECTORS`, `CHANGE_STATUS_ADMINS` | `SYSTEM_ADMIN` |

`toChangeStatus(role)` es estática, con `switch` exhaustivo, como `toSignUp` y `toList`. A diferencia de `SIGNUP_STUDENT`, `CHANGE_STATUS_STUDENTS` no incluye `ADMINISTRATIVE`: el escenario A2 solo nombra al administrador y al director.

### D6. Persistencia del estado

- Migración `V3__user_account_active.sql`: `ALTER TABLE user_account ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE`. Las cuentas existentes quedan activas sin un `UPDATE` aparte.
- `UserAccountEntity` gana el campo `active`; `JpaUserRepository.copyFields` lo copia y `toDomain` lo rehidrata con `withActive`.
- `getByEmail` sigue devolviendo también las cuentas desactivadas: el caso de uso necesita encontrarlas para responder «está desactivada» y para activarlas.

### D7. Avisos y lista

- `MailUserManagementNotifier` implementa `notifyUserDisabled` y `notifyUserEnabled` como `notifyUserSignedUp`: publica el evento y lo envía después del commit, un correo al solicitante y otro a la cuenta afectada.

  | Destino | Asunto | Cuerpo |
  | --- | --- | --- |
  | Solicitante, desactivar | `Cuenta desactivada: <nombre>` | «Se desactivó la cuenta de <nombre> (<correo>). Esa persona ya no puede enviar comandos al sistema; su historial se conserva.» |
  | Afectado, desactivar | `Su cuenta en el sistema de trámites de titulación fue desactivada` | «Hola, <nombre>: <solicitante> desactivó su cuenta en el sistema de trámites de titulación. Desde ahora el sistema no atenderá los comandos que envíe desde esta dirección. Si cree que es un error, comuníquese con la Dirección de Carrera.» |
  | Solicitante, activar | `Cuenta activada: <nombre>` | «Se activó la cuenta de <nombre> (<correo>). Esa persona ya puede volver a enviar comandos al sistema.» |
  | Afectado, activar | `Su cuenta en el sistema de trámites de titulación fue activada` | «Hola, <nombre>: <solicitante> activó nuevamente su cuenta en el sistema de trámites de titulación. Desde esta dirección (<correo>) puede volver a enviar solicitudes al sistema.» |

- `ListUsersQueryItem` gana el componente `active`; `listUsers` lo copia de `user.isActive()` y `userListBody` agrega después del teléfono la línea `   Estado: activa` o `   Estado: desactivada`. `getByRole` ya devuelve todas las cuentas del rol, activas o no.

### D8. Handlers

`DisableUserCommandHandler` (`usuarios.desactivar`) y `EnableUserCommandHandler` (`usuarios.activar`): 1 argumento, 0 adjuntos; pasan el remitente y `parseArgs().get(0)` al caso de uso. Se registran como beans en `UsersConfiguration`, como `ListUsersQueryHandler`. No hay records de comando: el único dato es un correo, y así lo modela EA (`disableUser(requesterEmail, userEmail)`).

## Risks / Trade-offs

- [Comprobar el estado del remitente en cada caso de uso] → Un subsistema nuevo que olvide comprobarlo dejaría actuar a una cuenta desactivada. Mitigación: D2 lo concentra en `users`; cuando llegue el segundo subsistema con comandos, conviene mover la comprobación al canal de correo o a un servicio compartido.
- [Carrera entre dos desactivaciones de administradores] → Si dos administradores se desactivan mutuamente a la vez, cada transacción ve al otro activo y los dos quedan desactivados, rompiendo FR-038. El buzón se procesa en un solo hilo (`InboxPollerService`), así que hoy no ocurre; si se paraleliza, hará falta un bloqueo.
- [Rechazos de estado bajo el prefijo «correo»] → El usuario ve «- correo: La cuenta X ya está desactivada.», algo menos natural que una frase sola, a cambio de no agregar una clase sin modelo (D4).

## Diferencias con el modelo

- `User.disable()` y `User.enable()` no tienen parámetros en EA; el código los define como `disable(requester: User)` y `enable(requester: User)`, porque el evento necesita el correo y el nombre del solicitante (D3). Hay que agregar el parámetro en EA.
- `UserManagementUseCase.requireActiveRequester(requesterEmail)` es un método privado que EA no modela; su comportamiento está descrito en la nota de `UserManagementUseCase` (D2).
- `UserNotAuthorizedException.disabled(email)` (estática) no está en EA, que sí tiene `notRegistered(email)`; el mensaje está en la nota de `UserManagementUseCase`. Hay que agregar la operación en EA junto a `notRegistered`.
- Las etiquetas de los permisos `CHANGE_STATUS_*` (D5) no están en el modelo, igual que las de `SIGNUP_*` y `LIST_*`.

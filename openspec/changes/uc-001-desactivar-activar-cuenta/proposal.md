# Proposal

## Why

Hoy una cuenta de usuario, una vez creada, puede usar el sistema para siempre: no hay forma de impedir que alguien que dejó la carrera o el cargo siga enviando comandos, ni de deshacerlo después. FR-032 pide desactivar y reactivar cuentas, conservando su historial.

Pertenece a **UC-001 Gestión de usuarios**, flujo alterno **A2 Desactivar/Activar cuenta**.

## What Changes

- Comandos por correo `usuarios.desactivar[email]` y `usuarios.activar[email]`, con exactamente un argumento (el correo de la cuenta afectada) y ningún adjunto.
- Alcance por rol del solicitante, con un mismo permiso para desactivar y activar: el administrador cambia el estado de cuentas de todos los roles; el director, de estudiante, administrativo, docente y docente de Taller de Grado (FR-050). El administrativo y los demás roles no cambian estados. Sin permiso, el rechazo es E1.
- Rechazos: correo sin cuenta; cuenta que ya está en el estado pedido (sin aviso); desactivar la propia cuenta; desactivar la única cuenta de administrador activa (FR-038).
- Una cuenta desactivada no puede usar el sistema (FR-032): cualquier comando de su dirección (crear, listar, desactivar, activar) se rechaza con «La cuenta X está desactivada. Comuníquese con la Dirección de Carrera.». La cuenta y sus datos se conservan.
- Avisos después de confirmar el cambio: al solicitante y a la cuenta afectada, al desactivar y al activar (FR-035).
- La lista de `usuarios.listar[rol]` incluye las cuentas desactivadas y muestra el estado de cada cuenta en una línea «Estado: activa» o «Estado: desactivada» (completa FR-030).
- Las cuentas que ya existen, incluido el administrador inicial, quedan activas; una cuenta nueva nace activa.

Fuera de alcance: FR-034 (advertir y dejar pendientes de reasignación los trámites en curso del usuario desactivado), porque los trámites todavía no existen; los flujos A1, A4 y A5.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `user-accounts`: se agregan los requisitos de desactivar y activar cuentas (comandos, alcance por rol, rechazos, cuenta desactivada como remitente, avisos y estado inicial) y cambia el requisito de los datos de cada cuenta listada para mostrar el estado.

## Impact

- **Elementos de EA en los que se basa:**
  - Requisitos: caso de uso `UC-001 Gestión de usuarios`, escenario alterno "A2 Desactivar/Activar cuenta" y excepción "E1 usuario no registrado o permisos insuficientes".
  - `Design/Users/application`: `DisableUserCommandHandler`, `EnableUserCommandHandler`, `UserManagementUseCase` (`disableUser`, `enableUser`), `UserManagementNotifier` (`notifyUserDisabled`, `notifyUserEnabled`, `sendUserList`), `ListUsersQueryItem` (`active`).
  - `Design/Users/domain`: `User` (`active`, `disable`, `enable`, `isActive`, `withActive`), `UserDisabledEvent`, `UserEnabledEvent`, `RolePermission` (`CHANGE_STATUS_STUDENTS`, `CHANGE_STATUS_ADMINISTRATIVES`, `CHANGE_STATUS_PROFESSORS`, `CHANGE_STATUS_CAPSTONE_PROJECT_PROFESSORS`, `CHANGE_STATUS_DIRECTORS`, `CHANGE_STATUS_ADMINS`, `toChangeStatus`), `UserRepository` (`update`, `getByEmail`, `getByRole`), `UserNotAuthorizedException`.
  - `Design/Mail/application`: `EmailCommandHandler`, ya implementado.
  - Diagrama de clases `Design/Users` "UC-001-A2".
- **Requisitos:** FR-030 (se completa), FR-032, FR-035, FR-038, FR-050. FR-034 queda pendiente.
- **Código:** `bo.org.tecnoweb.cpms.users` (`domain`, `application`, `infrastructure`). Migración Flyway `V3` con la columna de estado de `user_account`.
- **Documentos:** USER-GUIDE.md (comandos `usuarios.desactivar` y `usuarios.activar`, estado en `usuarios.listar`, respuesta a una cuenta desactivada).
- **Dependencias:** ninguna nueva.

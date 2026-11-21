# Proposal

## Why

Quien administra cuentas necesita ver qué cuentas existen para revisarlas y para identificar la cuenta sobre la que actuarán los demás flujos de gestión (actualizar, desactivar). Hoy el sistema solo crea cuentas: no hay forma de consultarlas por correo.

Pertenece a **UC-001 Gestión de usuarios**, flujo alterno **A3 Listar usuarios**.

## What Changes

- Comando por correo `usuarios.listar[rol]`, con exactamente un argumento (el rol es obligatorio) y ningún adjunto, que responde al remitente con la lista de las cuentas de ese rol.
- Alcance por rol del solicitante, igual que al crear cuentas: el administrador lista todos los roles; el director, docente, docente de Taller de Grado, administrativo y estudiante; el administrativo, estudiante. Un remitente sin cuenta o sin permiso para el rol pedido recibe el rechazo (E1).
- Cada cuenta de la lista muestra nombre completo, correo, CI, código y teléfono. El correo es el dato con el que el actor identificará la cuenta en los comandos que la modifiquen (A1, A2); la lista no muestra el identificador interno.
- La respuesta va en texto plano en el cuerpo del correo, ordenada por primer apellido, segundo apellido y nombres. Si no hay cuentas del rol, se responde un aviso, no un error.
- Renombre del puerto de salida de Users, que ya no solo publica eventos: `UserManagementEventBus` pasa a `UserManagementNotifier` (y `notifyUserSignedUpEvent` a `notifyUserSignedUp`), como está en el modelo de EA. No cambia el comportamiento de crear cuenta.

Cobertura parcial de FR-030: la lista no muestra el estado de la cuenta porque la cuenta todavía no tiene estado; llega con UC-001 A2 (FR-032).

Fuera de alcance: listar todos los roles a la vez (sin argumento), el estado de la cuenta, la consulta de una sola cuenta y los flujos A1, A2, A4 y A5.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `user-accounts`: se agregan los requisitos de listado de cuentas por rol (comando, alcance por rol, datos de cada cuenta, orden, lista vacía y respuesta solo al solicitante).

## Impact

- **Elementos de EA en los que se basa:**
  - Requisitos: caso de uso `UC-001 Gestión de usuarios`, escenario alterno "A3 Listar usuarios" y excepción "E1 usuario no registrado o permisos insuficientes".
  - Análisis: `Análisis/Subsistemas funcionales/Administración` no tiene diagrama de A3; se usan los mismos elementos que en el flujo básico (`ClienteEmail`, `GestorEmail`, `InterpreteComandos`, `GestorUsuarios`, `Usuario`, `Rol`).
  - `Design/Users/application`: `ListUsersQueryHandler`, `ListUsersQuery`, `ListUsersQueryResult`, `ListUsersQueryItem`, `UserManagementUseCase.listUsers`, `UserManagementNotifier` (`notifyUserSignedUp`, `sendUserList`).
  - `Design/Users/domain`: `UserRepository.getByRole`, `RolePermission` (`LIST_STUDENTS`, `LIST_ADMINISTRATIVES`, `LIST_PROFESSORS`, `LIST_CAPSTONE_PROJECT_PROFESSORS`, `LIST_DIRECTORS`, `LIST_ADMINS`), `Role`, `User`, `UserNotAuthorizedException`.
  - `Design/Mail/application`: `EmailCommandHandler`, ya implementado.
  - Diagrama de clases `Design/Users` "UC-001A3".
- **Requisitos:** FR-030 (en parte), FR-050, FR-053.
- **Código:** `bo.org.tecnoweb.cpms.users` (`domain`, `application`, `infrastructure`). Sin migración: la tabla `user_account` ya tiene los datos necesarios.
- **Documentos:** USER-GUIDE.md (comando `usuarios.listar`).
- **Dependencias:** ninguna nueva.

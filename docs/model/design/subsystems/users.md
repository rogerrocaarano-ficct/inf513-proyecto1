# Users

| | |
| --- | --- |
| Type | Business |
| Comes from | [Unassigned](../../analysis/unassigned/package.md) (GestionUsuariosUI, GestorUsuarios, CuentaUsuario, Rol) |
| Status | Draft |
| Iteration | I1 |

## Responsibility

Gestiona las cuentas de usuario y sus roles, con las reglas de alcance y de combinación de roles, e identifica al usuario que envía cada comando por su dirección de correo (AM-001). Atiende los comandos de gestión de usuarios que le entrega Messaging.

## Provided Interfaces

None yet: otros subsistemas la necesitarán para identificar al remitente (AM-001) cuando se diseñen sus casos de uso.

## Required Interfaces

| Interface | Provided by | Used for | Comes from |
| --- | --- | --- | --- |
| ChangeLog | [Audit](audit.md) | Registrar los cambios de las cuentas de usuario | Split: Unassigned::GestorUsuarios → Unassigned::RegistroCambios; AM-003 |
| CommandRegistry | [Messaging](messaging.md) | Recibir los comandos de gestión de usuarios y responder a quien los envió | AM-002 |
| GraduationParticipation | [GraduationProcesses](graduation-processes.md) | Saber en qué trámites en curso participa una cuenta de usuario y dejarlos pendientes de reasignación | Split: Unassigned::GestorUsuarios → Unassigned::TramiteTitulacion |
| Notifier | [Messaging](messaging.md) | Avisar al usuario de los cambios de su cuenta de usuario | Split: Unassigned::GestorUsuarios → Unassigned::NotificacionUI; AM-002 |

## Open Questions

None.

# Subsystems

## Subsystem Catalog

| Subsystem | Type | Comes from | Responsibility | Provides (derived) | Requires (derived) |
| --- | --- | --- | --- | --- | --- |
| [Audit](audit.md) | General | Unassigned (RegistroCambios), AM-003 | Registra cada cambio del sistema con quién lo hizo, cuándo y sobre qué dato | ChangeLog | None |
| [GraduationProcesses](graduation-processes.md) | Business | Unassigned (TramiteTitulacion) | Lleva los trámites de titulación y sus participantes | GraduationParticipation | None |
| [Messaging](messaging.md) | General | Unassigned (NotificacionUI), AM-002 | Recibe los correos con comandos, los entrega a quien los atiende y envía respuestas y notificaciones | CommandRegistry, Notifier, SenderAuthenticator | None |
| [Users](users.md) | Business | Unassigned (GestionUsuariosUI, GestorUsuarios, CuentaUsuario, Rol) | Gestiona las cuentas de usuario, sus roles y la identificación del remitente | None | ChangeLog, CommandRegistry, GraduationParticipation, Notifier, SenderAuthenticator |

## Open Questions

- [ ] ¿Se confirman estos subsistemas cuando el análisis de la arquitectura reemplace el paquete provisional Unassigned? (ask: Equipo de desarrollo)

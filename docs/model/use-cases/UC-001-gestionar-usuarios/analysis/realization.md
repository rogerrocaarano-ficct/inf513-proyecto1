# UC-001 Gestionar usuarios: Analysis Realization

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |
| Use case | [UC-001](../use-case.md) |

## Participating Classes

| Class | Stereotype | Package | Responsibility in this use case |
| --- | --- | --- | --- |
| Administrador | actor | — | Solicita crear, consultar, modificar, desactivar y reactivar cuentas de usuario |
| Director de carrera | actor | — | Solicita lo mismo que el administrador, dentro de su alcance |
| Usuario | actor | — | Recibe el aviso de los cambios en su cuenta de usuario |
| GestionUsuariosUI | boundary | [Unassigned](../../../analysis/unassigned/package.md) | Recibe las solicitudes del administrador y del director de carrera y les devuelve resultados, advertencias y rechazos |
| NotificacionUI | boundary | [Unassigned](../../../analysis/unassigned/package.md) | Avisa por correo al usuario de los cambios en su cuenta de usuario |
| GestorUsuarios | control | [Unassigned](../../../analysis/unassigned/package.md) | Coordina cada solicitud: verifica el alcance y las reglas, aplica el cambio, lo registra y notifica |
| CuentaUsuario | entity | [Unassigned](../../../analysis/unassigned/package.md) | Guarda los datos, roles y estado de la cuenta, y verifica que sus datos únicos no estén repetidos |
| Rol | entity | [Unassigned](../../../analysis/unassigned/package.md) | Conoce qué roles puede asignar cada rol gestor y qué combinaciones de roles están permitidas |
| RegistroCambios | entity | [Unassigned](../../../analysis/unassigned/package.md) | Guarda quién cambió qué cuenta de usuario y cuándo |
| TramiteTitulacion | entity | [Unassigned](../../../analysis/unassigned/package.md) | Indica en qué trámites en curso participa una cuenta de usuario y queda pendiente de reasignación |

## Structural View

| From | To | Meaning |
| --- | --- | --- |
| Administrador | GestionUsuariosUI | Interactúa con |
| Director de carrera | GestionUsuariosUI | Interactúa con |
| GestionUsuariosUI | GestorUsuarios | Pide gestionar cuentas de usuario |
| GestorUsuarios | NotificacionUI | Pide avisar al usuario |
| NotificacionUI | Usuario | Avisa por correo |
| GestorUsuarios | CuentaUsuario | Crea, consulta y actualiza |
| GestorUsuarios | Rol | Consulta el alcance y las reglas de roles |
| GestorUsuarios | RegistroCambios | Registra cada cambio |
| GestorUsuarios | TramiteTitulacion | Consulta la participación y marca la reasignación pendiente |
| CuentaUsuario | Rol | Cada cuenta de usuario tiene uno o más roles (see entity.md) |
| RegistroCambios | CuentaUsuario | Cada cambio indica quién lo hizo y a qué cuenta de usuario afecta (see entity.md) |
| TramiteTitulacion | CuentaUsuario | Cada trámite tiene un estudiante y docentes designados (see entity.md) |

## Behavioral View

### Basic Flow

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | 1 | Administrador | GestionUsuariosUI | crear cuenta |
| 2 | 1 | GestionUsuariosUI | GestorUsuarios | crear cuenta |
| 3 | 2 | GestorUsuarios | Rol | verificar alcance |
| 4 | 3 | GestorUsuarios | CuentaUsuario | verificar datos únicos |
| 5 | 4 | GestorUsuarios | Rol | verificar combinación |
| 6 | 5 | GestorUsuarios | CuentaUsuario | crear |
| 7 | 5 | GestorUsuarios | RegistroCambios | registrar cambio |
| 8 | 6 | GestorUsuarios | NotificacionUI | notificar creación |
| 9 | 6 | NotificacionUI | Usuario | avisar |
| 10 | 7 | GestorUsuarios | GestionUsuariosUI | confirmar |
| 11 | 7 | GestionUsuariosUI | Administrador | mostrar confirmación |

### A1 El director de carrera gestiona la cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A1.1 | Director de carrera | GestionUsuariosUI | gestionar cuenta |
| 2 | A1.1 | GestionUsuariosUI | GestorUsuarios | gestionar cuenta |
| 3 | A1.2 | GestorUsuarios | Rol | verificar alcance |

### A2 Consultar cuentas de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A2.1 | GestionUsuariosUI | GestorUsuarios | consultar cuentas |
| 2 | A2.1 | GestorUsuarios | Rol | obtener alcance |
| 3 | A2.1 | GestorUsuarios | CuentaUsuario | buscar cuentas |
| 4 | A2.1 | GestorUsuarios | GestionUsuariosUI | mostrar cuentas |

### A3 Modificar una cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A3.1 | GestionUsuariosUI | GestorUsuarios | modificar cuenta |
| 2 | A3.2 | GestorUsuarios | Rol | verificar roles |
| 3 | A3.2 | GestorUsuarios | CuentaUsuario | verificar modificación |
| 4 | A3.3 | GestorUsuarios | TramiteTitulacion | marcar reasignación |
| 5 | A3.3 | GestorUsuarios | GestionUsuariosUI | advertir trámites |
| 6 | A3.4 | GestorUsuarios | CuentaUsuario | modificar |
| 7 | A3.4 | GestorUsuarios | RegistroCambios | registrar cambio |
| 8 | A3.5 | GestorUsuarios | NotificacionUI | notificar roles |
| 9 | A3.5 | NotificacionUI | Usuario | avisar |
| 10 | A3.6 | GestorUsuarios | GestionUsuariosUI | confirmar |

### A4 Desactivar una cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A4.1 | GestionUsuariosUI | GestorUsuarios | desactivar cuenta |
| 2 | A4.1 | GestorUsuarios | Rol | verificar alcance |
| 3 | A4.1 | GestorUsuarios | CuentaUsuario | verificar administrador |
| 4 | A4.2 | GestorUsuarios | TramiteTitulacion | marcar reasignación |
| 5 | A4.2 | GestorUsuarios | GestionUsuariosUI | advertir trámites |
| 6 | A4.3 | GestorUsuarios | CuentaUsuario | desactivar |
| 7 | A4.3 | GestorUsuarios | RegistroCambios | registrar cambio |
| 8 | A4.4 | GestorUsuarios | NotificacionUI | notificar desactivación |
| 9 | A4.4 | NotificacionUI | Usuario | avisar |
| 10 | A4.5 | GestorUsuarios | GestionUsuariosUI | confirmar |

### A5 Reactivar una cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A5.1 | GestionUsuariosUI | GestorUsuarios | reactivar cuenta |
| 2 | A5.1 | GestorUsuarios | Rol | verificar reactivación |
| 3 | A5.2 | GestorUsuarios | CuentaUsuario | reactivar |
| 4 | A5.2 | GestorUsuarios | RegistroCambios | registrar cambio |
| 5 | A5.3 | GestorUsuarios | NotificacionUI | notificar reactivación |
| 6 | A5.3 | NotificacionUI | Usuario | avisar |
| 7 | A5.4 | GestorUsuarios | GestionUsuariosUI | confirmar |

### E1 a E5 Rechazos

Las excepciones E1 a E5 siguen la misma colaboración: la verificación que falla termina el flujo sin cambios.

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | E1.1, E2.1, E3.1, E4.1, E5.1 | GestorUsuarios | GestionUsuariosUI | rechazar |
| 2 | E1.1, E2.1, E3.1, E4.1, E5.1 | GestionUsuariosUI | Administrador | mostrar rechazo |

## Special Requirements

| Requirement | Concern | Handled by |
| --- | --- | --- |
| NFR-SE-001 | Security | AM-001 |
| NFR-IN-001 | Distribution | AM-002 |
| NFR-SE-002 | Audit | AM-003 |

## Feedback to Requirements

- El usuario afectado recibe notificaciones (FR-035) pero no era actor de UC-001: se agregó ACT-010 Usuario como actor de apoyo, que recibe los avisos a través de NotificacionUI.
- A3.3 y A4.2 marcan trámites pendientes de reasignación; el destino de esos trámites sigue abierto (FR-034).

## Open Questions

- [ ] A3.3, A4.2: ¿quién reasigna los trámites pendientes y qué pasa con el trámite de un estudiante desactivado? (ask: Secretario(a) de dirección de carrera)

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
| InterfazGestionUsuarios | boundary | [Unassigned](../../../analysis/unassigned/package.md) | Recibe las solicitudes del administrador y del director de carrera, les devuelve resultados, advertencias y rechazos, y avisa por correo al usuario afectado |
| GestorUsuarios | control | [Unassigned](../../../analysis/unassigned/package.md) | Coordina cada solicitud: verifica el alcance y las reglas, aplica el cambio, lo registra y notifica |
| CuentaUsuario | entity | [Unassigned](../../../analysis/unassigned/package.md) | Guarda los datos, roles y estado de la cuenta, y verifica que sus datos únicos no estén repetidos |
| Rol | entity | [Unassigned](../../../analysis/unassigned/package.md) | Conoce qué roles puede asignar cada rol gestor y qué combinaciones de roles están permitidas |
| RegistroCambios | entity | [Unassigned](../../../analysis/unassigned/package.md) | Guarda quién cambió qué cuenta de usuario y cuándo |
| TramiteTitulacion | entity | [Unassigned](../../../analysis/unassigned/package.md) | Indica en qué trámites en curso participa una cuenta de usuario y queda pendiente de reasignación |

## Structural View

| From | To | Meaning |
| --- | --- | --- |
| Administrador | InterfazGestionUsuarios | Interactúa con |
| Director de carrera | InterfazGestionUsuarios | Interactúa con |
| InterfazGestionUsuarios | GestorUsuarios | Pide gestionar cuentas de usuario; recibe resultados y avisos para notificar |
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
| 1 | 1 | Administrador | InterfazGestionUsuarios | `solicitarCreacion(datos, roles)` |
| 2 | 1 | InterfazGestionUsuarios | GestorUsuarios | `crearCuenta(solicitante, datos, roles)` |
| 3 | 2 | GestorUsuarios | Rol | `verificarAsignacion(solicitante, roles)` |
| 4 | 3 | GestorUsuarios | CuentaUsuario | `verificarDatosUnicos(correo, registroUniversitario)` |
| 5 | 4 | GestorUsuarios | Rol | `verificarCombinacion(roles)` |
| 6 | 5 | GestorUsuarios | CuentaUsuario | `crear(datos, roles)` |
| 7 | 5 | GestorUsuarios | RegistroCambios | `registrarCambio(solicitante, cuenta, "creación")` |
| 8 | 6 | GestorUsuarios | InterfazGestionUsuarios | `notificarCreacion(cuenta, roles)` |
| 9 | 7 | GestorUsuarios | InterfazGestionUsuarios | `confirmarCreacion(cuenta)` |
| 10 | 7 | InterfazGestionUsuarios | Administrador | `mostrarConfirmacion(cuenta)` |

### A1 El director de carrera gestiona la cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A1.1 | Director de carrera | InterfazGestionUsuarios | `solicitarGestion(solicitud)` |
| 2 | A1.1 | InterfazGestionUsuarios | GestorUsuarios | `gestionarCuenta(solicitante, solicitud)` |
| 3 | A1.2 | GestorUsuarios | Rol | `verificarAlcance(solicitante, cuenta, roles)` |

### A2 Consultar cuentas de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A2.1 | InterfazGestionUsuarios | GestorUsuarios | `consultarCuentas(solicitante, filtro)` |
| 2 | A2.1 | GestorUsuarios | Rol | `obtenerAlcance(solicitante)` |
| 3 | A2.1 | GestorUsuarios | CuentaUsuario | `buscarCuentas(alcance, filtro)` |
| 4 | A2.1 | GestorUsuarios | InterfazGestionUsuarios | `mostrarCuentas(cuentas)` |

### A3 Modificar una cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A3.1 | InterfazGestionUsuarios | GestorUsuarios | `modificarCuenta(solicitante, cuenta, datos, roles)` |
| 2 | A3.2 | GestorUsuarios | Rol | `verificarRoles(solicitante, cuenta, roles)` |
| 3 | A3.2 | GestorUsuarios | CuentaUsuario | `verificarModificacion(cuenta, datos, roles)` |
| 4 | A3.3 | GestorUsuarios | TramiteTitulacion | `marcarPendientesReasignacion(cuenta, rolQuitado)` |
| 5 | A3.3 | GestorUsuarios | InterfazGestionUsuarios | `advertirTramitesAfectados(tramites)` |
| 6 | A3.4 | GestorUsuarios | CuentaUsuario | `modificar(datos, roles)` |
| 7 | A3.4 | GestorUsuarios | RegistroCambios | `registrarCambio(solicitante, cuenta, cambios)` |
| 8 | A3.5 | GestorUsuarios | InterfazGestionUsuarios | `notificarCambioRoles(cuenta, roles)` |
| 9 | A3.6 | GestorUsuarios | InterfazGestionUsuarios | `confirmarModificacion(cuenta)` |

### A4 Desactivar una cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A4.1 | InterfazGestionUsuarios | GestorUsuarios | `desactivarCuenta(solicitante, cuenta)` |
| 2 | A4.1 | GestorUsuarios | Rol | `verificarAlcance(solicitante, cuenta)` |
| 3 | A4.1 | GestorUsuarios | CuentaUsuario | `verificarNoUltimoAdministrador(cuenta)` |
| 4 | A4.2 | GestorUsuarios | TramiteTitulacion | `marcarPendientesReasignacion(cuenta)` |
| 5 | A4.2 | GestorUsuarios | InterfazGestionUsuarios | `advertirTramitesAfectados(tramites)` |
| 6 | A4.3 | GestorUsuarios | CuentaUsuario | `desactivar()` |
| 7 | A4.3 | GestorUsuarios | RegistroCambios | `registrarCambio(solicitante, cuenta, "desactivación")` |
| 8 | A4.4 | GestorUsuarios | InterfazGestionUsuarios | `notificarDesactivacion(cuenta)` |
| 9 | A4.5 | GestorUsuarios | InterfazGestionUsuarios | `confirmarDesactivacion(cuenta)` |

### A5 Reactivar una cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A5.1 | InterfazGestionUsuarios | GestorUsuarios | `reactivarCuenta(solicitante, cuenta)` |
| 2 | A5.1 | GestorUsuarios | Rol | `verificarReactivacion(solicitante, cuenta)` |
| 3 | A5.2 | GestorUsuarios | CuentaUsuario | `reactivar()` |
| 4 | A5.2 | GestorUsuarios | RegistroCambios | `registrarCambio(solicitante, cuenta, "reactivación")` |
| 5 | A5.3 | GestorUsuarios | InterfazGestionUsuarios | `notificarReactivacion(cuenta)` |
| 6 | A5.4 | GestorUsuarios | InterfazGestionUsuarios | `confirmarReactivacion(cuenta)` |

### E1 a E5 Rechazos

Las excepciones E1 a E5 siguen la misma colaboración: la verificación que falla termina el flujo sin cambios.

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | E1.1, E2.1, E3.1, E4.1, E5.1 | GestorUsuarios | InterfazGestionUsuarios | `rechazarSolicitud(motivo)` |
| 2 | E1.1, E2.1, E3.1, E4.1, E5.1 | InterfazGestionUsuarios | Administrador | `mostrarRechazo(motivo)` |

## Special Requirements

| Requirement | Concern | Handled by |
| --- | --- | --- |
| NFR-SE-001 | Security | AM-001 |
| NFR-IN-001 | Distribution | AM-002 |
| FR-052 | Audit | AM-003 |

## Feedback to Requirements

- El usuario afectado recibe notificaciones (FR-035) pero no es actor de UC-001; se decidió no agregarlo como actor de apoyo y modelar la notificación como una salida de InterfazGestionUsuarios.
- A3.3 y A4.2 marcan trámites pendientes de reasignación; el destino de esos trámites sigue abierto (FR-034).

## Open Questions

- [ ] A3.3, A4.2: ¿quién reasigna los trámites pendientes y qué pasa con el trámite de un estudiante desactivado? (ask: Secretario(a) de dirección de carrera)

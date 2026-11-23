# Delta de specs

## MODIFIED Requirements

### Requirement: Asignación del tutor

El sistema SHALL permitir que Dirección designe a un docente activo como tutor de un trámite en curso cuyo perfil vigente está aprobado (FR-003, FR-005).

#### Scenario: Perfil aprobado y proyecto sin tutor

- **WHEN** Dirección indica la referencia del trámite y el correo del docente elegible
- **THEN** se registra la designación con independencia del tribunal y se avisa al estudiante después del commit (FR-020).

#### Scenario: Designación rechazada

- **WHEN** falta autorización, el trámite o docente no es elegible, el perfil vigente no está aprobado o el proyecto ya tiene tutor
- **THEN** se informa el motivo sin cambiar el proyecto.

## REMOVED Requirements

### Requirement: Primera entrega del perfil

**Reason**: La presentación del perfil pertenece a UC-004 y ahora admite versiones nuevas tras una observación o un rechazo.
**Migration**: Se reemplaza por "Presentación del perfil" en `project-profiles`; el comando `proyectos.registrarPerfil` no cambia.

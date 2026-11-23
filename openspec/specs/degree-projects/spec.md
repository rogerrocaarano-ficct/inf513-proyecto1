# degree-projects Specification

## Purpose

Registrar la asignación del tutor del proyecto de grado de un estudiante (UC-003), una vez aprobado su perfil de proyecto (UC-004, capacidad `project-profiles`).

## Requirements

### Requirement: Asignación del tutor

El sistema SHALL permitir que Dirección designe a un docente activo como tutor de un trámite en curso cuyo perfil vigente está aprobado (FR-003, FR-005).

#### Scenario: Perfil aprobado y proyecto sin tutor

- **WHEN** Dirección indica la referencia del trámite y el correo del docente elegible
- **THEN** se registra la designación con independencia del tribunal y se avisa al estudiante después del commit (FR-020).

#### Scenario: Designación rechazada

- **WHEN** falta autorización, el trámite o docente no es elegible, el perfil vigente no está aprobado o el proyecto ya tiene tutor
- **THEN** se informa el motivo sin cambiar el proyecto.

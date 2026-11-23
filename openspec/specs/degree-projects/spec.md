# degree-projects Specification

## Purpose

Registrar la primera propuesta del estudiante y la asignación del tutor de su proyecto de grado.

## Requirements

### Requirement: Primera entrega del perfil

El sistema SHALL admitir por correo el título y un PDF del estudiante activo con trámite en curso y sin perfil registrado (FR-004, FR-058).

#### Scenario: Primera entrega válida

- **WHEN** el estudiante cumple las condiciones y hay un docente de Taller activo
- **THEN** se crea el proyecto con su perfil pendiente de evaluación y se envía a Taller el perfil después del commit (FR-057).

#### Scenario: Entrega rechazada

- **WHEN** falta el trámite en curso, ya hay un perfil, el título o PDF es inválido, el remitente carece de permiso o no hay docente de Taller activo
- **THEN** se informa el motivo y no se registra el perfil, de modo que el estudiante pueda reintentar una entrega fallida.

### Requirement: Asignación del tutor

El sistema SHALL permitir que Dirección designe a un docente activo como tutor de un trámite en curso con perfil aprobado (FR-003, FR-005).

#### Scenario: Perfil aprobado y proyecto sin tutor

- **WHEN** Dirección indica la referencia del trámite y el correo del docente elegible
- **THEN** se registra la designación con independencia del tribunal y se avisa al estudiante después del commit (FR-020).

#### Scenario: Designación rechazada

- **WHEN** falta autorización, el trámite o docente no es elegible, el perfil carece de visto bueno o el proyecto ya tiene tutor
- **THEN** se informa el motivo sin cambiar el proyecto.

# Analysis Architecture

## Analysis Packages

| Package | Responsibility | Actors served (derived) | Depends on (derived) |
| --- | --- | --- | --- |
| [Unassigned](unassigned/package.md) | Paquete provisional de Inception con las clases candidatas | ACT-008, ACT-003, ACT-010, ACT-002, ACT-001 | None |

## Common Special Requirements

| ID | Concern | Description | Requirements | Use cases (derived) |
| --- | --- | --- | --- | --- |
| AM-001 | Security | Cada solicitud se atribuye al usuario por la dirección de correo del remitente, y solo se atiende si sus roles y su participación en el trámite lo permiten | NFR-SE-001 | UC-001, UC-003, UC-004 |
| AM-002 | Distribution | Los usuarios envían solicitudes y reciben respuestas y notificaciones por correo electrónico | NFR-IN-001 | UC-001, UC-003, UC-004 |
| AM-003 | Audit | Cada cambio queda registrado con quién lo hizo, cuándo y sobre qué dato | NFR-SE-002 | UC-001, UC-003, UC-004 |
| AM-004 | Persistence | Los archivos que envían los usuarios (documentos iniciales, perfil de proyecto, documento del proyecto, PDF anotado) se guardan con el trámite de titulación y se pueden recuperar mientras exista | CON-004, CON-005 | UC-003, UC-004 |

## Open Questions

- [ ] ¿Qué paquetes reemplazan al paquete provisional Unassigned? (ask: Equipo de desarrollo)

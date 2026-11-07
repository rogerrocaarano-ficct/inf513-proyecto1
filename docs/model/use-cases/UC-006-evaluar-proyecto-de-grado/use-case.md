# UC-006 Evaluar proyecto de grado

| | |
| --- | --- |
| Status | Briefly described |
| Iteration | I1 |
| Primary actor | ACT-005 Docente de Taller de Grado, ACT-006 Docente Tutor, ACT-007 Docente del Tribunal |
| Supporting actors | None |
| Priority | Must |
| Architecturally significant | No: la revisión en dos etapas y las aprobaciones son reglas del dominio; el PDF anotado reutiliza la recepción de adjuntos de UC-004, y el control de plazos de FR-024 reutiliza las tareas programadas de AD-005 (`@Scheduled` y `Clock`) |
| Feature | FEAT-004, FEAT-005, FEAT-006 |
| Functional requirements (derived) | FR-005, FR-009, FR-011, FR-014, FR-016, FR-022, FR-024, FR-043, FR-044, FR-045, FR-046, FR-047, FR-048 |

## Brief Description

Los docentes evalúan el proyecto de grado en cada etapa. El docente de Taller de Grado evalúa la viabilidad del perfil de proyecto. El docente tutor y los docentes del tribunal revisan cada versión del documento del proyecto y la aprueban u observan. Los docentes del tribunal registran el resultado de la predefensa y la calificación final de la defensa final. Así el estudiante conoce las observaciones que debe subsanar y el proyecto avanza hasta la titulación.

## Preconditions

- TBD

## Basic Flow

1. TBD

## Alternative Flows

None.

## Exceptions

None.

## Postconditions

- **Success:** TBD
- **Failure:** TBD

## Special Requirements (derived)

- CON-004

## Open Questions

- [ ] FR-024: ¿cuántos días tiene cada revisor para revisar una versión, y qué hace el sistema cuando vence el plazo: solo avisa, o cambia el estado de la versión? Si cambia el estado, el registro de cambios necesita un autor que no es un usuario (AD-008) (ask: Docente)

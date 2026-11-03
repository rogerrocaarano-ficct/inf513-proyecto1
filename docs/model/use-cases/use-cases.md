# Use Cases

## Use-Case Catalog

| ID | Use case | Actors (derived) | Priority (derived) | Arch. significant (derived) | Requirements (derived) | Analysis (derived) | Design (derived) | Iteration (derived) |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [UC-001](UC-001-gestionar-usuarios/use-case.md) | Gestionar usuarios | ACT-008, ACT-003, ACT-010 | Must | Yes | Detailed | Draft | Draft | I1 |
| [UC-002](UC-002-gestionar-datos-personales/use-case.md) | Gestionar datos personales | ACT-010 | Must | No | Briefly described | — | — | I1 |
| [UC-003](UC-003-gestionar-tramite-de-titulacion/use-case.md) | Gestionar trámite de titulación | ACT-002, ACT-001 | Must | Yes | Detailed | Draft | — | I1 |
| [UC-004](UC-004-gestionar-proyecto-de-grado/use-case.md) | Gestionar proyecto de grado | ACT-001 | Must | Yes | Briefly described | — | — | I1 |
| [UC-005](UC-005-gestionar-tribunal-evaluador-y-tutor/use-case.md) | Gestionar tribunal evaluador y tutor | ACT-003 | Must | No | Briefly described | — | — | I1 |
| [UC-006](UC-006-evaluar-proyecto-de-grado/use-case.md) | Evaluar proyecto de grado | ACT-005, ACT-006, ACT-007 | Must | No | Briefly described | — | — | I1 |
| [UC-007](UC-007-gestionar-cronograma-de-defensas/use-case.md) | Gestionar cronograma de defensas | ACT-003 | Must | No | Briefly described | — | — | I1 |
| [UC-008](UC-008-consultar-tramite-de-titulacion/use-case.md) | Consultar trámite de titulación | ACT-001, ACT-002, ACT-004 | Must | No | Briefly described | — | — | I1 |
| [UC-009](UC-009-consultar-registro-de-cambios/use-case.md) | Consultar registro de cambios | ACT-008, ACT-003, ACT-002 | Must | No | Briefly described | — | — | I1 |
| [UC-010](UC-010-consultar-estadisticas/use-case.md) | Consultar estadísticas | ACT-003 | Must | No | Briefly described | — | — | I1 |

## Relationships

| Use case | Relationship | Target | Condition or reason |
| --- | --- | --- | --- |

## Packages

| Package | Use cases | Description |
| --- | --- | --- |
| Usuarios | UC-001, UC-002 | Cuentas de usuario, sus roles y sus datos personales |
| Trámite de titulación | UC-003, UC-005, UC-007, UC-008 | El trámite de titulación de cada estudiante, de la apertura al cierre: designaciones, cronograma de defensas y su consulta |
| Proyecto de grado | UC-004, UC-006 | El proyecto de grado del estudiante, del perfil de proyecto a la defensa final, y su evaluación |
| Supervisión | UC-009, UC-010 | Consultas de supervisión sobre todos los contextos: el registro de cambios y las estadísticas de titulación |

## Open Questions

- [ ] UC-001 … UC-010: ¿el docente de Tecnología Web acepta derivar estos casos de uso de sus CU1–CU8 aprobados? Ver la reestructuración en `docs/sources/2026-10-06.md` (ask: Docente de Tecnología Web)

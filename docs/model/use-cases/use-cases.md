# Use Cases

## Use-Case Catalog

| ID | Use case | Actors (derived) | Priority (derived) | Arch. significant (derived) | Requirements (derived) | Analysis (derived) | Design (derived) | Iteration (derived) |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [UC-001](UC-001-administrar-usuarios/use-case.md) | Administrar usuarios | ACT-008 | Must | Yes | Detailed | — | — | I1 |
| [UC-002](UC-002-registrar-solicitud-de-titulacion/use-case.md) | Registrar solicitud de titulación | ACT-002, ACT-001 | Must | No | Briefly described | — | — | I1 |
| [UC-003](UC-003-registrar-perfil-de-proyecto/use-case.md) | Registrar perfil de proyecto | ACT-001 | Must | No | Briefly described | — | — | I1 |
| [UC-004](UC-004-asignar-tutor/use-case.md) | Asignar tutor | ACT-003 | Must | No | Briefly described | — | — | I1 |
| [UC-005](UC-005-evaluar-viabilidad-del-perfil/use-case.md) | Evaluar viabilidad del perfil | ACT-005 | Must | No | Briefly described | — | — | I1 |
| [UC-006](UC-006-designar-tribunal-evaluador/use-case.md) | Designar tribunal evaluador | ACT-003 | Must | No | Briefly described | — | — | I1 |
| [UC-007](UC-007-entregar-documento-del-proyecto/use-case.md) | Entregar documento del proyecto | ACT-001 | Must | Yes | Detailed | — | — | I1 |
| [UC-008](UC-008-revisar-documento-del-proyecto/use-case.md) | Revisar documento del proyecto | ACT-006, ACT-007 | Must | Yes | Detailed | — | — | I1 |
| [UC-009](UC-009-programar-evaluacion/use-case.md) | Programar evaluación | ACT-003 | Must | No | Briefly described | — | — | I1 |
| [UC-010](UC-010-registrar-resultado-de-evaluacion/use-case.md) | Registrar resultado de evaluación | ACT-007 | Must | No | Briefly described | — | — | I1 |
| [UC-011](UC-011-cerrar-expediente-de-titulacion/use-case.md) | Cerrar expediente de titulación | ACT-002 | Must | No | Briefly described | — | — | I1 |
| [UC-012](UC-012-consultar-estado-del-tramite/use-case.md) | Consultar estado del trámite | ACT-001, ACT-004, ACT-002, ACT-003 | Must | No | Briefly described | — | — | I1 |
| [UC-013](UC-013-consultar-calendario-academico/use-case.md) | Consultar calendario académico | ACT-001, ACT-004, ACT-002, ACT-003 | Must | No | Briefly described | — | — | I1 |
| [UC-014](UC-014-consultar-dashboard/use-case.md) | Consultar dashboard | ACT-003, ACT-002 | Must | No | Briefly described | — | — | I1 |
| [UC-015](UC-015-generar-reportes-y-estadisticas/use-case.md) | Generar reportes y estadísticas | ACT-003 | Must | No | Briefly described | — | — | I1 |
| [UC-016](UC-016-actualizar-foto-de-perfil/use-case.md) | Actualizar foto de perfil | ACT-001, ACT-004, ACT-002, ACT-003, ACT-008 | Must | No | Briefly described | — | — | I1 |

## Relationships

| Use case | Relationship | Target | Condition or reason |
| --- | --- | --- | --- |

## Packages

| Package | Use cases | Description |
| --- | --- | --- |
| Gestión de usuarios | UC-001, UC-016 | Casos de uso de FEAT-001, que corresponde al CU1 aprobado por el docente de Tecnología Web (SRC-002) |
| Habilitación académica de solicitudes | UC-002 | Casos de uso de FEAT-002, que corresponde al CU2 aprobado por el docente de Tecnología Web (SRC-002) |
| Asignación de tutor y registro del perfil de proyecto | UC-003, UC-004 | Casos de uso de FEAT-003, que corresponde al CU3 aprobado por el docente de Tecnología Web (SRC-002) |
| Evaluación de viabilidad y conformación del tribunal | UC-005, UC-006 | Casos de uso de FEAT-004, que corresponde al CU4 aprobado por el docente de Tecnología Web (SRC-002) |
| Revisión, versionado y control de plazos del tribunal | UC-007, UC-008 | Casos de uso de FEAT-005, que corresponde al CU5 aprobado por el docente de Tecnología Web (SRC-002) |
| Programación y registro de evaluaciones | UC-009, UC-010, UC-011 | Casos de uso de FEAT-006, que corresponde al CU6 aprobado por el docente de Tecnología Web (SRC-002) |
| Monitoreo, trazabilidad, dashboard y calendario académico | UC-012, UC-013, UC-014 | Casos de uso de FEAT-007, que corresponde al CU7 aprobado por el docente de Tecnología Web (SRC-002) |
| Reportes y estadísticas | UC-015 | Casos de uso de FEAT-008, que corresponde al CU8 aprobado por el docente de Tecnología Web (SRC-002) |

## Open Questions

- [ ] UC-001 … UC-015: ¿el docente de Tecnología Web acepta derivar estos casos de uso de sus CU1–CU8 aprobados? Ver la decisión de modelado en `docs/sources/2026-10-06.md` (ask: Docente de Tecnología Web)

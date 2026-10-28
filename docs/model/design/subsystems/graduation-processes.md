# GraduationProcesses

| | |
| --- | --- |
| Type | Business |
| Comes from | [Unassigned](../../analysis/unassigned/package.md) (TramiteTitulacion) |
| Status | Draft |
| Iteration | I1 |

## Responsibility

Lleva los trámites de titulación desde la solicitud hasta el archivo del expediente, con el estudiante y los docentes que participan en cada uno.

## Provided Interfaces

### GraduationParticipation

- **Used by (derived):** Users
- **Use cases (derived):** None

| Operation | Description | Errors |
| --- | --- | --- |
| Pending | Las operaciones se agregan con las realizaciones de diseño que las usan | — |

## Required Interfaces

None. El trámite guarda el identificador de las cuentas de usuario del estudiante y de los docentes, sin depender de Users; así se evita un ciclo con Users, que usa GraduationParticipation.

## Open Questions

None.

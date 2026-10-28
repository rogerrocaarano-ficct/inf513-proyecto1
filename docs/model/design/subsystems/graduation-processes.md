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
- **Use cases (derived):** UC-001

| Operation | Description | Errors |
| --- | --- | --- |
| markPendingReassignment(userAccountId: UUID, kinds: Set<ParticipantKind>): List<GraduationProcessSummary> | Busca los trámites en curso en los que la cuenta de usuario participa con alguno de esos tipos de participación, los marca pendientes de reasignación y los devuelve | None |

Tipos que forman parte de la interfaz:

- `ParticipantKind` (enum): `STUDENT`, `TUTOR`, `TRIBUNAL_MEMBER`.
- `GraduationProcessSummary` (record): `id: UUID`, `studentName: String`.

## Required Interfaces

None. El trámite guarda el identificador de las cuentas de usuario del estudiante y de los docentes, sin depender de Users; así se evita un ciclo con Users, que usa GraduationParticipation.

## Open Questions

None.

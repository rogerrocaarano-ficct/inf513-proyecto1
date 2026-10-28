# Audit

| | |
| --- | --- |
| Type | General |
| Comes from | [Unassigned](../../analysis/unassigned/package.md) (RegistroCambios), AM-003 |
| Status | Draft |
| Iteration | I1 |

## Responsibility

Guarda el registro de cambios de todo el sistema (AD-008). Es general: guarda el identificador de la cuenta de usuario que hizo cada cambio y del registro afectado, sin depender de los subsistemas de negocio.

## Provided Interfaces

### ChangeLog

- **Used by (derived):** Users
- **Use cases (derived):** UC-001

| Operation | Description | Errors |
| --- | --- | --- |
| record(actorId: UUID, targetType: String, targetId: UUID, changes: List<FieldChange>): void | Registra en la transacción en curso un cambio hecho por una cuenta de usuario sobre un registro, con la fecha y hora actual | None |

Tipos que forman parte de la interfaz:

- `FieldChange` (record): `field: String`, `oldValue: String`, `newValue: String`. En una creación, `oldValue` es nulo.

## Required Interfaces

None.

## Open Questions

None.

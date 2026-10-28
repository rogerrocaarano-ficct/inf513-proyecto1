# Logical Database Model

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |
| Database | PostgreSQL, base `db_grupo03sc` en `db.tecnoweb.org.bo` (AD-004, AD-010) |

Convención: `snake_case`, nombres de tabla en plural, claves primarias `id UUID` ordenadas por tiempo (UUID versión 7), generadas por la aplicación (AD-010).

## Tables

| Table | Comes from | Description |
| --- | --- | --- |
| [user_accounts](#user_accounts) | Unassigned::CuentaUsuario | Una fila por cuenta de usuario |
| [user_account_roles](#user_account_roles) | Relationship Unassigned::CuentaUsuario – Unassigned::Rol | Una fila por rol de una cuenta de usuario |
| [change_log_entries](#change_log_entries) | Unassigned::RegistroCambios | Una fila por dato cambiado |
| [graduation_processes](#graduation_processes) | Unassigned::TramiteTitulacion | Una fila por trámite de titulación. Parcial: solo las columnas que usa UC-001; UC-003 la completa |
| [graduation_process_participants](#graduation_process_participants) | Relationship Unassigned::TramiteTitulacion – Unassigned::CuentaUsuario | Una fila por participante de un trámite. Parcial: UC-005 la completa |

## Table Details

### user_accounts

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| id | UUID | No | PK | | Identificador de la cuenta de usuario |
| full_name | VARCHAR(150) | No | | | Nombre completo |
| email | VARCHAR(254) | No | UK | | Dirección de correo, guardada en minúsculas |
| student_registration_number | VARCHAR(20) | Yes | UK | | Registro universitario, solo de un estudiante |
| id_card | VARCHAR(20) | Yes | | | Cédula de identidad, solo de un estudiante |
| phone | VARCHAR(20) | Yes | | | Teléfono, solo de un estudiante |
| profile_photo | BYTEA | Yes | | | Foto de perfil (UC-002) |
| status | VARCHAR(10) | No | | 'ACTIVE' | `ACTIVE` o `INACTIVE` |
| created_at | TIMESTAMPTZ | No | | now() | Fecha de creación |

**Constraints:**

- CHECK (status IN ('ACTIVE', 'INACTIVE'))

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| user_accounts_email_uk | email | Yes | Identificar al remitente (AM-001) y rechazar correos repetidos (UC-001 E2) |
| user_accounts_registration_uk | student_registration_number | Yes | Rechazar registros universitarios repetidos (UC-001 E2) |

### user_account_roles

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| user_account_id | UUID | No | PK, FK → user_accounts.id | | Cuenta de usuario |
| role | VARCHAR(30) | No | PK | | Constante de `Role` |

**Constraints:**

- CHECK (role IN ('STUDENT', 'PROFESSOR', 'THESIS_WORKSHOP_PROFESSOR', 'PROGRAM_SECRETARY', 'PROGRAM_DIRECTOR', 'ADMINISTRATOR'))

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| user_account_roles_role_idx | role | No | Contar cuentas activas por rol (UC-001: único docente de Taller de Grado, último administrador) |

### change_log_entries

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| id | UUID | No | PK | | Identificador del cambio |
| changed_at | TIMESTAMPTZ | No | | now() | Cuándo se hizo el cambio |
| actor_id | UUID | No | | | Cuenta de usuario que hizo el cambio (sin FK: Audit no depende de Users) |
| target_type | VARCHAR(50) | No | | | Tipo de registro cambiado, p. ej. `UserAccount` |
| target_id | UUID | No | | | Identificador del registro cambiado |
| field | VARCHAR(50) | No | | | Dato cambiado |
| old_value | TEXT | Yes | | | Valor anterior; nulo en una creación |
| new_value | TEXT | Yes | | | Valor nuevo |

**Constraints:**

- Ninguna; las filas nunca se modifican ni se borran.

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| change_log_entries_target_idx | target_type, target_id, changed_at | No | Consultar el historial de un registro (UC-009) |
| change_log_entries_actor_idx | actor_id, changed_at | No | Consultar los cambios de un usuario (UC-009) |

### graduation_processes

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| id | UUID | No | PK | | Identificador del trámite |
| status | VARCHAR(20) | No | | 'IN_PROGRESS' | `IN_PROGRESS` o `CLOSED`; UC-003 define el resto de los estados |
| pending_reassignment | BOOLEAN | No | | false | Falta reasignar a un participante (FR-034) |

**Constraints:**

- None.

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| None | | | |

### graduation_process_participants

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| graduation_process_id | UUID | No | PK, FK → graduation_processes.id | | Trámite |
| user_account_id | UUID | No | PK | | Cuenta de usuario del participante (sin FK: GraduationProcesses no depende de Users) |
| kind | VARCHAR(20) | No | PK | | `STUDENT`, `TUTOR` o `TRIBUNAL_MEMBER` |

**Constraints:**

- CHECK (kind IN ('STUDENT', 'TUTOR', 'TRIBUNAL_MEMBER'))

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| graduation_process_participants_user_idx | user_account_id, kind | No | Buscar los trámites de una cuenta de usuario (UC-001 A3, A4) |

## Relationships

| From | To | Cardinality | On delete | Description |
| --- | --- | --- | --- | --- |
| user_account_roles.user_account_id | user_accounts.id | many to one | Cascade | Un rol pertenece a una cuenta de usuario |
| graduation_process_participants.graduation_process_id | graduation_processes.id | many to one | Cascade | Un participante pertenece a un trámite |

## Open Questions

- [ ] change_log_entries.actor_id, graduation_process_participants.user_account_id: no tienen FK a `user_accounts` para mantener los subsistemas desacoplados; ¿se prefiere la integridad de la base de datos (agregar las FK, ya que las cuentas de usuario nunca se borran)? (ask: Equipo de desarrollo)

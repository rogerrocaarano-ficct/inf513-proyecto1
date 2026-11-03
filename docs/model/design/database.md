# Logical Database Model

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |
| Database | PostgreSQL, base `db_grupo03sc` en `db.tecnoweb.org.bo` (AD-004, AD-010); migraciones con Flyway (AD-011) en `src/main/resources/db/migration` |

Convención: `snake_case`, nombres de tabla en plural, claves primarias `id UUID` ordenadas por tiempo (UUID versión 7), generadas por la aplicación (AD-010).

## Tables

| Table | Comes from | Description |
| --- | --- | --- |
| [user_accounts](#user_accounts) | Unassigned::CuentaUsuario | Una fila por cuenta de usuario |
| [user_account_roles](#user_account_roles) | Relationship Unassigned::CuentaUsuario – Unassigned::Rol | Una fila por rol de una cuenta de usuario |
| [change_log_entries](#change_log_entries) | Unassigned::RegistroCambios | Una fila por dato cambiado |
| [processed_mail_messages](#processed_mail_messages) | Mecanismo AM-002 (D4 de `esqueleto-mail-app`) | Una fila por correo de comando ya procesado, para no ejecutarlo dos veces |
| [graduation_processes](#graduation_processes) | Unassigned::TramiteTitulacion | Una fila por trámite de titulación, que es también su expediente |
| [graduation_process_participants](#graduation_process_participants) | Relationship Unassigned::TramiteTitulacion – Unassigned::CuentaUsuario | Una fila por docente designado en un trámite. Parcial: UC-005 la completa |
| [initial_documents](#initial_documents) | Unassigned::DocumentoInicial | Una fila por documento inicial de un trámite; el archivo está en el sistema de archivos (AD-012) |
| [graduation_projects](#graduation_projects) | Unassigned::ProyectoGrado | Una fila por proyecto de grado, con su documento vigente; el historial está en `change_log_entries` y los archivos en el sistema de archivos (AD-012) |

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
- CHECK (email = lower(email))

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

- Trigger `change_log_entries_append_only`: rechaza todo `UPDATE` y `DELETE` (AD-008, NFR-SE-002). La aplicación usa un solo usuario de base de datos (CON-007), así que no basta con quitar permisos.

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| change_log_entries_target_idx | target_type, target_id, changed_at | No | Consultar el historial de un registro (UC-009) y de un proyecto de grado (UC-004 A4) |
| change_log_entries_actor_idx | actor_id, changed_at | No | Consultar los cambios de un usuario (UC-009) |

### processed_mail_messages

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| id | UUID | No | PK | | Identificador |
| message_id | VARCHAR(998) | No | UK | | Cabecera `Message-ID` del correo, o un hash SHA-256 si no la tiene |
| processed_at | TIMESTAMPTZ | No | | now() | Cuándo se procesó |

**Constraints:**

- None.

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| processed_mail_messages_message_id_uk | message_id | Yes | Reconocer un correo ya procesado en cada sondeo |

### graduation_processes

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| id | UUID | No | PK | | Identificador del trámite |
| student_account_id | UUID | No | | | Cuenta de usuario del estudiante (sin FK: GraduationProcesses no depende de Users) |
| status | VARCHAR(20) | No | | 'IN_PROGRESS' | `IN_PROGRESS` o `CLOSED`; `CLOSED` significa que el expediente está archivado |
| stage | VARCHAR(30) | No | | 'OPENED' | Constante de `GraduationStage`; UC-003 usa `OPENED` y `FINAL_GRADE_RECORDED`, y los demás casos de uso agregan las suyas |
| opened_at | TIMESTAMPTZ | No | | | Cuándo se abrió el trámite |
| closed_at | TIMESTAMPTZ | Yes | | | Cuándo se cerró y se archivó el expediente |
| physical_copies_received_at | TIMESTAMPTZ | Yes | | | Cuándo se registró la recepción de los dos ejemplares físicos |
| pending_reassignment | BOOLEAN | No | | false | Falta reasignar a un participante (FR-034) |

**Constraints:**

- CHECK (status IN ('IN_PROGRESS', 'CLOSED'))
- CHECK (status = 'IN_PROGRESS' OR (closed_at IS NOT NULL AND physical_copies_received_at IS NOT NULL))

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| graduation_processes_student_in_progress_uk | student_account_id, parcial `WHERE status = 'IN_PROGRESS'` | Yes | Un solo trámite en curso por estudiante (UC-003 E4, FR-056) y buscarlo por su cuenta de usuario (UC-003 A2, A3; UC-001 A3, A4) |

### graduation_process_participants

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| graduation_process_id | UUID | No | PK, FK → graduation_processes.id | | Trámite |
| user_account_id | UUID | No | PK | | Cuenta de usuario del docente (sin FK: GraduationProcesses no depende de Users) |
| kind | VARCHAR(20) | No | PK | | `TUTOR` o `TRIBUNAL_MEMBER`; el estudiante está en `graduation_processes.student_account_id` |

**Constraints:**

- CHECK (kind IN ('TUTOR', 'TRIBUNAL_MEMBER'))

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| graduation_process_participants_user_idx | user_account_id, kind | No | Buscar los trámites de una cuenta de usuario (UC-001 A3, A4) |

### initial_documents

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| id | UUID | No | PK | | Identificador del documento inicial |
| graduation_process_id | UUID | No | FK → graduation_processes.id | | Trámite al que pertenece |
| type | VARCHAR(20) | No | | | Constante de `InitialDocumentType` |
| file_name | VARCHAR(255) | No | | | Nombre original del adjunto |
| content_type | VARCHAR(100) | No | | | `image/png`, `image/jpeg` o `application/pdf` |
| size_bytes | INTEGER | No | | | Tamaño del archivo |
| storage_key | VARCHAR(500) | No | UK | | Ruta relativa del archivo bajo `app.files.root` (AD-012) |
| uploaded_at | TIMESTAMPTZ | No | | | Cuándo se guardó |

**Constraints:**

- CHECK (type IN ('REQUEST_LETTER', 'CURRICULUM_VITAE', 'ID_CARD_COPY', 'ENROLLMENT_SLIP'))
- CHECK (content_type IN ('image/png', 'image/jpeg', 'application/pdf'))
- CHECK (size_bytes BETWEEN 1 AND 1048576) (CON-005)

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| initial_documents_process_type_uk | graduation_process_id, type | Yes | Un documento de cada tipo por trámite (UC-003 2, A2.3) |
| initial_documents_storage_key_uk | storage_key | Yes | Cada archivo pertenece a un solo documento |

### graduation_projects

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| id | UUID | No | PK | | Identificador del proyecto de grado |
| graduation_process_id | UUID | No | FK → graduation_processes.id, UK | | Trámite al que pertenece |
| title | VARCHAR(300) | No | | | Título del proyecto, del último perfil de proyecto |
| document_kind | VARCHAR(30) | No | | | Constante de `ProjectDocumentKind` del documento vigente |
| file_name | VARCHAR(255) | No | | | Nombre original del adjunto vigente |
| size_bytes | INTEGER | No | | | Tamaño del archivo vigente |
| storage_key | VARCHAR(500) | No | UK | | Ruta relativa del archivo vigente bajo `app.files.root` (AD-012) |
| version_number | INTEGER | Yes | | | Número de la versión vigente; nulo mientras es un perfil de proyecto |
| general_comment | TEXT | Yes | | | Comentario general del estudiante sobre la versión vigente |
| submitted_at | TIMESTAMPTZ | No | | | Cuándo se entregó el documento vigente |
| status | VARCHAR(30) | No | | 'PROFILE_PENDING' | Constante de `ProjectStatus` |
| version | BIGINT | No | | 0 | Bloqueo optimista (`@Version`) |

**Constraints:**

- CHECK (document_kind IN ('PROJECT_PROFILE', 'PROJECT_DOCUMENT', 'FINAL_DEFENSE_VERSION'))
- CHECK (status IN ('PROFILE_PENDING', 'PROFILE_OBSERVED', 'PROFILE_REJECTED', 'PROFILE_APPROVED', 'TUTOR_REVIEW', 'TUTOR_OBSERVED', 'TRIBUNAL_REVIEW', 'TRIBUNAL_OBSERVED', 'APPROVED'))
- CHECK ((document_kind = 'PROJECT_PROFILE') = (version_number IS NULL)): un perfil no tiene número de versión, y una versión sí
- CHECK (document_kind = 'PROJECT_PROFILE' OR general_comment IS NOT NULL)
- CHECK ((document_kind = 'PROJECT_PROFILE') = (status LIKE 'PROFILE\_%')): los estados `PROFILE_` solo para un perfil, y los de revisión solo para una versión
- CHECK (size_bytes BETWEEN 1 AND 26214400) (CON-004)

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| graduation_projects_process_uk | graduation_process_id | Yes | Un proyecto de grado por trámite y buscarlo por su trámite (UC-004) |
| graduation_projects_storage_key_uk | storage_key | Yes | Cada archivo pertenece a un solo documento |

## Relationships

| From | To | Cardinality | On delete | Description |
| --- | --- | --- | --- | --- |
| user_account_roles.user_account_id | user_accounts.id | many to one | Cascade | Un rol pertenece a una cuenta de usuario |
| graduation_process_participants.graduation_process_id | graduation_processes.id | many to one | Cascade | Un participante pertenece a un trámite |
| initial_documents.graduation_process_id | graduation_processes.id | many to one | Cascade | Un documento inicial pertenece a un trámite; los trámites no se borran |
| graduation_projects.graduation_process_id | graduation_processes.id | one to one (optional) | Cascade | Un proyecto de grado pertenece a un trámite; los trámites no se borran |

## Open Questions

- [ ] change_log_entries.actor_id, graduation_processes.student_account_id, graduation_process_participants.user_account_id: no tienen FK a `user_accounts` para mantener los subsistemas desacoplados; ¿se prefiere la integridad de la base de datos (agregar las FK, ya que las cuentas de usuario nunca se borran)? (ask: Equipo de desarrollo)

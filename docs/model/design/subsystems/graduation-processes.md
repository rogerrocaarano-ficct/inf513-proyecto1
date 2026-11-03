# GraduationProcesses

| | |
| --- | --- |
| Type | Business |
| Comes from | [Unassigned](../../analysis/unassigned/package.md) (GestionTramiteUI, GestorTramites, TramiteTitulacion, DocumentoInicial) |
| Status | Draft |
| Iteration | I1 |

## Responsibility

Lleva los trámites de titulación desde la solicitud hasta el archivo del expediente, con el estudiante y los docentes que participan en cada uno, y sus documentos iniciales. Atiende los comandos del trámite, que registra en Messaging como `CommandHandler`. Guarda el identificador de la cuenta de usuario del estudiante y de los docentes, sin depender de Users: la cuenta del estudiante la pide por el puerto `StudentAccounts`, que define y que Users implementa (inversión de dependencias, como `SenderAuthenticator` en Messaging). Así se evita un ciclo con Users, que usa GraduationParticipation.

## Provided Interfaces

### GraduationParticipation

- **Used by (derived):** Users
- **Use cases (derived):** UC-001

| Operation | Description | Errors |
| --- | --- | --- |
| markPendingReassignment(userAccountId: UUID, kinds: Set<ParticipantKind>): List<GraduationProcessSummary> | Busca los trámites en curso en los que la cuenta de usuario participa con alguno de esos tipos de participación, los marca pendientes de reasignación y los devuelve | None |

Tipos que forman parte de la interfaz:

- `ParticipantKind` (enum): `STUDENT`, `TUTOR`, `TRIBUNAL_MEMBER`.
- `GraduationProcessSummary` (record): `id: UUID`, `studentAccountId: UUID`. El nombre del estudiante lo resuelve Users, porque GraduationProcesses no depende de Users.

Implementación: UC-003 reemplaza la implementación provisional de UC-001, que no encontraba trámites (D6 de `implementar-uc-001`), por `ProcessParticipation`, que busca en `graduation_processes.student_account_id` para `STUDENT` y en `graduation_process_participants` para `TUTOR` y `TRIBUNAL_MEMBER`. Solo cambia el contenido de `GraduationProcessSummary` (D7 de `implementar-uc-003`).

### StudentAccounts

Puerto que define GraduationProcesses y que implementa Users (`UserStudentAccounts`).

- **Used by (derived):** None: lo llama GraduationProcesses y lo implementa Users
- **Use cases (derived):** UC-003

| Operation | Description | Errors |
| --- | --- | --- |
| obtain(requesterId: UUID, student: StudentData): StudentAccount | Devuelve la cuenta de usuario activa con el rol de estudiante cuya dirección de correo y registro universitario son los indicados, o la crea con el rol de estudiante, la registra y avisa al estudiante, con las reglas de UC-001 | CommandRejectedException (UserRuleViolationException): datos que faltan o no válidos, dirección de correo o registro universitario de otra cuenta, cuenta desactivada |
| findByRegistrationNumber(studentRegistrationNumber: String): Optional<StudentAccount> | Devuelve la cuenta de usuario con el rol de estudiante y ese registro universitario | None |

Tipos que forman parte de la interfaz:

- `StudentData` (record): `fullName: String`, `email: String`, `studentRegistrationNumber: String`, `idCard: String`, `phone: String`.
- `StudentAccount` (record): `id: UUID`, `fullName: String`, `email: String`, `created: boolean` (si `obtain` la acaba de crear).

## Required Interfaces

| Interface | Provided by | Used for | Comes from |
| --- | --- | --- | --- |
| ChangeLog | [Audit](audit.md) | Registrar los cambios de los trámites de titulación | Split: Unassigned::GestorTramites → Unassigned::RegistroCambios; AM-003 |
| CommandRegistry | [Messaging](messaging.md) | Recibir los comandos del trámite y responder a quien los envió (implementa `CommandHandler`); rechazar con `CommandRejectedException` | AM-002 |
| Notifier | [Messaging](messaging.md) | Avisar al estudiante de la apertura y el cierre de su trámite | Split: Unassigned::GestorTramites → Unassigned::NotificacionUI; AM-002 |
| FileStorage | [Storage](storage.md) | Guardar y borrar los archivos de los documentos iniciales | AM-004 |

## Open Questions

None.

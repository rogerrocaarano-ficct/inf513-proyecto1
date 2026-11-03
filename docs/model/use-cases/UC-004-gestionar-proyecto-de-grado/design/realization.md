# UC-004 Gestionar proyecto de grado: Design Realization

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |
| Use case | [UC-004](../use-case.md) |
| Analysis realization | [analysis/realization.md](../analysis/realization.md) |

## Traceability from Analysis

| Analysis class | Stereotype | Design elements |
| --- | --- | --- |
| Unassigned::GestionProyectoUI | boundary | SubmitProjectProfileCommandHandler, SubmitDocumentVersionCommandHandler, ViewGraduationProjectCommandHandler, GraduationCommandSupport (implementan CommandHandler de Messaging) |
| Unassigned::NotificacionUI | boundary | Notifier (interface de Messaging) |
| Unassigned::GestorProyectos | control | GraduationProjectService, ProjectView, GraduationRuleViolationException, GraduationRejectionReason |
| Unassigned::TramiteTitulacion | entity | GraduationProcess, GraduationStage, GraduationProcessRepository |
| Unassigned::ProyectoGrado | entity | GraduationProject, ProjectDocumentKind, ProjectStatus, ProjectHistory, ProjectHistoryItem, DocumentFile, GraduationProjectRepository, FileStorage (interface de Storage) |
| Unassigned::CuentaUsuario | entity | ProfessorContacts y StudentAccounts (puertos de GraduationProcesses), ProfessorContact, StudentAccount, UserProfessorContacts, UserStudentAccounts, UserAccountRepository |
| Unassigned::RegistroCambios | entity | ChangeLog, FieldChange, LoggedChange (interface de Audit) |

## Structural View

### Participating Elements

| Element | Kind | Subsystem | Application | Role in this use case |
| --- | --- | --- | --- | --- |
| SubmitProjectProfileCommandHandler | Class | GraduationProcesses | mail-app | Adaptador de entrada del comando `PresentarPerfil` |
| SubmitDocumentVersionCommandHandler | Class | GraduationProcesses | mail-app | Adaptador de entrada del comando `EntregarVersion` |
| ViewGraduationProjectCommandHandler | Class | GraduationProcesses | mail-app | Adaptador de entrada del comando `ConsultarProyecto` |
| GraduationCommandSupport | Class | GraduationProcesses | mail-app | Lectura de los argumentos y de los adjuntos, compartida con UC-003; suma el rol del estudiante |
| GraduationProjectService | Class | GraduationProcesses | mail-app | Servicio de aplicación: busca el trámite del estudiante, aplica las reglas del proyecto de grado, guarda el archivo, registra el cambio y avisa |
| ProjectView | Class | GraduationProcesses | mail-app | Record con el proyecto de grado y su historial, para la consulta |
| GraduationProject | Class | GraduationProcesses | mail-app | Entidad JPA: proyecto de grado con su documento vigente, que evoluciona de perfil de proyecto a versiones, y sus reglas |
| ProjectDocumentKind | Enumeration | GraduationProcesses | mail-app | Tipo del documento vigente: `PROJECT_PROFILE`, `PROJECT_DOCUMENT`, `FINAL_DEFENSE_VERSION` |
| ProjectStatus | Enumeration | GraduationProcesses | mail-app | Estado del documento vigente; UC-004 deja `PROFILE_PENDING` y `TUTOR_REVIEW`, UC-006 los demás |
| ProjectHistory | Class | GraduationProcesses | mail-app | Arma el historial de perfiles y versiones a partir del registro de cambios |
| ProjectHistoryItem | Class | GraduationProcesses | mail-app | Record con un perfil o versión del historial |
| GraduationProjectRepository | Interface | GraduationProcesses | mail-app | Puerto de salida de persistencia, implementado por Spring Data JPA |
| GraduationProcess | Class | GraduationProcesses | mail-app | Entidad JPA del trámite (UC-003); indica si la predefensa está aprobada |
| GraduationStage | Enumeration | GraduationProcesses | mail-app | Etapa del trámite; suma `PREDEFENSE_APPROVED`, que registra UC-006 |
| GraduationProcessRepository | Interface | GraduationProcesses | mail-app | Busca el trámite en curso del estudiante y su tutor |
| DocumentFile | Class | GraduationProcesses | mail-app | Record con un archivo recibido (UC-003) |
| GraduationRuleViolationException, GraduationRejectionReason | Class y Enumeration | GraduationProcesses | mail-app | Rechazos, con los motivos nuevos de UC-004 |
| StudentAccounts, StudentAccount | Interface y Class | GraduationProcesses | mail-app | Puerto de UC-003; suma `findById` para el nombre del estudiante en los avisos |
| ProfessorContacts | Interface | GraduationProcesses | mail-app | Puerto que define GraduationProcesses e implementa Users: el docente de Taller de Grado y los datos de contacto de un docente |
| ProfessorContact | Class | GraduationProcesses | mail-app | Record con el nombre y la dirección de correo de un docente |
| UserProfessorContacts | Class | Users | mail-app | Implementa `ProfessorContacts` con `UserAccountRepository` |
| UserStudentAccounts | Class | Users | mail-app | Implementa `StudentAccounts.findById` |
| UserAccountRepository | Interface | Users | mail-app | Suma la búsqueda de cuentas activas por rol |
| CommandHandler, Sender, MailCommand, MailAttachment, CommandReply, CommandRejectedException | Interface y Class | Messaging | mail-app | Contrato de los manejadores de comandos (ver `messaging.md`) |
| Notifier | Interface | Messaging | mail-app | Avisa al docente de Taller de Grado y al tutor |
| ChangeLog, FieldChange, LoggedChange | Interface y Class | Audit | mail-app | Registra los cambios y devuelve el historial de un registro |
| FileStorage | Interface | Storage | mail-app | Guarda los archivos del perfil y de las versiones (AM-004) |

### Command handlers

Los tres manejadores (`SubmitProjectProfileCommandHandler`, `SubmitDocumentVersionCommandHandler`, `ViewGraduationProjectCommandHandler`) tienen los mismos miembros:

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| FORM | Attribute | static String | ~ |
| service | Attribute | GraduationProjectService | - |
| commandName | Operation | commandName(): String | + |
| allowedRoles | Operation | allowedRoles(): Set<String> | + |
| handle | Operation | handle(command: MailCommand, sender: Sender): CommandReply | + |

### GraduationCommandSupport

Solo los miembros que usa este caso de uso; `STUDENT_ROLES` es nuevo.

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| STUDENT_ROLES | Attribute | static Set<String> = {STUDENT} | ~ |
| required | Operation | static required(arguments: List<String>, form: String, names: String...): List<String> | ~ |
| files | Operation | static files(attachments: List<MailAttachment>): List<DocumentFile> | ~ |

### GraduationProjectService

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| projects | Attribute | GraduationProjectRepository | - |
| processes | Attribute | GraduationProcessRepository | - |
| studentAccounts | Attribute | StudentAccounts | - |
| professorContacts | Attribute | ProfessorContacts | - |
| fileStorage | Attribute | FileStorage | - |
| changeLog | Attribute | ChangeLog | - |
| notifier | Attribute | Notifier | - |
| clock | Attribute | Clock | - |
| submitProfile | Operation | submitProfile(studentId: UUID, title: String, files: List<DocumentFile>): GraduationProject | + |
| submitVersion | Operation | submitVersion(studentId: UUID, generalComment: String, files: List<DocumentFile>): GraduationProject | + |
| viewProject | Operation | viewProject(studentId: UUID): Optional<ProjectView> | + |
| processInProgress | Operation | processInProgress(studentId: UUID): GraduationProcess | - |
| singleFile | Operation | singleFile(files: List<DocumentFile>): DocumentFile | - |
| notifyThesisWorkshopProfessor | Operation | notifyThesisWorkshopProfessor(studentId: UUID, project: GraduationProject): boolean | - |
| notifyTutor | Operation | notifyTutor(studentId: UUID, tutorId: UUID, project: GraduationProject): void | - |

### GraduationProject

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| id | Attribute | UUID | - |
| graduationProcessId | Attribute | UUID | - |
| title | Attribute | String | - |
| documentKind | Attribute | ProjectDocumentKind | - |
| fileName | Attribute | String | - |
| sizeBytes | Attribute | int | - |
| storageKey | Attribute | String | - |
| versionNumber | Attribute | Integer | - |
| generalComment | Attribute | String | - |
| submittedAt | Attribute | Instant | - |
| status | Attribute | ProjectStatus | - |
| version | Attribute | long (`@Version`, bloqueo optimista) | - |
| MAX_SIZE_BYTES | Attribute | static long = 26214400 | + |
| checkFile | Operation | static checkFile(file: DocumentFile): void | + |
| createWithProfile | Operation | static createWithProfile(graduationProcessId: UUID, title: String, file: DocumentFile, storageKey: String, now: Instant): GraduationProject | + |
| submitProfile | Operation | submitProfile(title: String, file: DocumentFile, storageKey: String, now: Instant): List<FieldChange> | + |
| checkVersionAllowed | Operation | checkVersionAllowed(predefenseApproved: boolean): void | + |
| submitVersion | Operation | submitVersion(generalComment: String, file: DocumentFile, storageKey: String, predefenseApproved: boolean, now: Instant): List<FieldChange> | + |
| describe | Operation | describe(): List<FieldChange> | + |
| getId, getTitle, getDocumentKind, getVersionNumber, getStatus | Operation | getters | + |

### ProjectDocumentKind, ProjectStatus

| Element | Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- | --- |
| ProjectDocumentKind | PROJECT_PROFILE, PROJECT_DOCUMENT, FINAL_DEFENSE_VERSION | Attribute | ProjectDocumentKind (constantes) | + |
| ProjectDocumentKind | label | Attribute | String: `perfil de proyecto`, `documento del proyecto`, `versión para la defensa final` | - |
| ProjectStatus | PROFILE_PENDING, PROFILE_OBSERVED, PROFILE_REJECTED, PROFILE_APPROVED, TUTOR_REVIEW, TUTOR_OBSERVED, TRIBUNAL_REVIEW, TRIBUNAL_OBSERVED, APPROVED | Attribute | ProjectStatus (constantes) | + |
| ProjectStatus | label | Attribute | String: `pendiente de evaluación`, `observado`, `rechazado`, `con visto bueno`, `en revisión del tutor`, `observada por el tutor`, `en revisión del tribunal`, `observada por el tribunal`, `aprobada` | - |
| ProjectDocumentKind, ProjectStatus | label | Operation | label(): String | + |

### ProjectHistory, ProjectHistoryItem, ProjectView

| Element | Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- | --- |
| ProjectHistory | from | Operation | static from(changes: List<LoggedChange>): List<ProjectHistoryItem> | + |
| ProjectHistoryItem | documentKind | Attribute | ProjectDocumentKind | + |
| ProjectHistoryItem | versionNumber | Attribute | Integer | + |
| ProjectHistoryItem | title, generalComment, fileName | Attribute | String | + |
| ProjectHistoryItem | submittedAt | Attribute | Instant | + |
| ProjectHistoryItem | status | Attribute | ProjectStatus | + |
| ProjectView | project | Attribute | GraduationProject | + |
| ProjectView | history | Attribute | List<ProjectHistoryItem> | + |

### GraduationProjectRepository

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| findByGraduationProcessId | Operation | findByGraduationProcessId(graduationProcessId: UUID): Optional<GraduationProject> | + |
| saveAndFlush | Operation | saveAndFlush(project: GraduationProject): GraduationProject | + |

### GraduationProcess, GraduationStage, GraduationProcessRepository

Solo los miembros que usa este caso de uso; `isPredefenseApproved`, `PREDEFENSE_APPROVED` y `findParticipantIds` son nuevos.

| Element | Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- | --- |
| GraduationProcess | isPredefenseApproved | Operation | isPredefenseApproved(): boolean | + |
| GraduationProcess | getId | Operation | getId(): UUID | + |
| GraduationStage | OPENED, PREDEFENSE_APPROVED, FINAL_GRADE_RECORDED | Attribute | GraduationStage (constantes, en este orden) | + |
| GraduationProcessRepository | findByStudentAccountIdAndStatus | Operation | findByStudentAccountIdAndStatus(studentAccountId: UUID, status: GraduationProcessStatus): Optional<GraduationProcess> | + |
| GraduationProcessRepository | findParticipantIds | Operation | findParticipantIds(graduationProcessId: UUID, kind: String): List<UUID> | + |

### ProfessorContacts, ProfessorContact, UserProfessorContacts

| Element | Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- | --- |
| ProfessorContacts | thesisWorkshopProfessor | Operation | thesisWorkshopProfessor(): Optional<ProfessorContact> | + |
| ProfessorContacts | findById | Operation | findById(userAccountId: UUID): Optional<ProfessorContact> | + |
| ProfessorContact | id | Attribute | UUID | + |
| ProfessorContact | fullName, email | Attribute | String | + |
| UserProfessorContacts | accounts | Attribute | UserAccountRepository | - |
| UserProfessorContacts | thesisWorkshopProfessor, findById | Operation | las de `ProfessorContacts` | + |

### StudentAccounts, UserStudentAccounts, UserAccountRepository

Solo los miembros nuevos; los de UC-003 están en su realización.

| Element | Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- | --- |
| StudentAccounts, UserStudentAccounts | findById | Operation | findById(userAccountId: UUID): Optional<StudentAccount> | + |
| UserAccountRepository | findActiveWithRole | Operation | findActiveWithRole(role: Role): List<UserAccount> | + |
| UserAccountRepository | findById | Operation | findById(id: UUID): Optional<UserAccount> (existe desde UC-001) | + |

### LoggedChange

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| changedAt | Attribute | Instant | + |
| actorId | Attribute | UUID | + |
| field, oldValue, newValue | Attribute | String | + |

### Relationships

| From | To | Kind | Multiplicity | Navigability |
| --- | --- | --- | --- | --- |
| Cada manejador de comando | CommandHandler | Realization | — | From → To |
| Cada manejador de comando | GraduationProjectService | Association | 1 to 1 | From → To |
| Cada manejador de comando | GraduationCommandSupport | Dependency | — | From → To |
| GraduationProjectService | GraduationProjectRepository | Association | 1 to 1 | From → To |
| GraduationProjectService | GraduationProcessRepository | Association | 1 to 1 | From → To |
| GraduationProjectService | StudentAccounts | Association | 1 to 1 | From → To |
| GraduationProjectService | ProfessorContacts | Association | 1 to 1 | From → To |
| GraduationProjectService | FileStorage | Association | 1 to 1 | From → To |
| GraduationProjectService | ChangeLog | Association | 1 to 1 | From → To |
| GraduationProjectService | Notifier | Association | 1 to 1 | From → To |
| GraduationProjectService | ProjectHistory, ProjectView | Dependency | — | From → To |
| GraduationProjectRepository | GraduationProject | Dependency | — | From → To |
| GraduationProject | GraduationProcess | Association (por `graduationProcessId`) | 0..1 to 1 | From → To |
| GraduationProject | ProjectDocumentKind | Association | 1 to 1 | From → To |
| GraduationProject | ProjectStatus | Association | 1 to 1 | From → To |
| GraduationProject | DocumentFile | Dependency | — | From → To |
| ProjectHistory | LoggedChange, ProjectHistoryItem | Dependency | — | From → To |
| ProjectView | GraduationProject, ProjectHistoryItem | Association | 1 to 1, 1 to many | From → To |
| GraduationProcess | GraduationStage | Association | 1 to 1 | From → To |
| UserProfessorContacts | ProfessorContacts | Realization | — | From → To |
| UserProfessorContacts | UserAccountRepository | Association | 1 to 1 | From → To |
| UserStudentAccounts | StudentAccounts | Realization | — | From → To |

## Behavioral View

Los comandos llegan por correo. Messaging sondea el buzón, interpreta el asunto, identifica al remitente, comprueba que tiene el rol de `allowedRoles()` (estudiante, AD-007), llama a `handle(command, sender)` dentro de una transacción y envía la respuesta. Las filas "envía correo" y "recibe respuesta" resumen ese recorrido.

### Basic Flow

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | Estudiante | SubmitProjectProfileCommandHandler | envía correo con asunto `PresentarPerfil(titulo)` y el perfil de proyecto en PDF adjunto | |
| 2 | 1 | SubmitProjectProfileCommandHandler | GraduationCommandSupport | required(arguments, FORM, "título del proyecto"), files(command.attachments()) | List<String>, List<DocumentFile> |
| 3 | 1 | SubmitProjectProfileCommandHandler | GraduationProjectService | submitProfile(sender.accountId(), title, files) | GraduationProject |
| 4 | 2 | GraduationProjectService | GraduationProcessRepository | findByStudentAccountIdAndStatus(studentId, IN_PROGRESS), desde processInProgress(studentId) | Optional<GraduationProcess> |
| 5 | 2 | GraduationProjectService | GraduationProjectRepository | findByGraduationProcessId(process.getId()): vacío | Optional<GraduationProject> |
| 6 | 3 | GraduationProjectService | GraduationProject | checkFile(singleFile(files)) | |
| 7 | 4 | GraduationProjectService | FileStorage | store("graduation-projects", file.fileName(), file.content()) | String |
| 8 | 4 | GraduationProjectService | GraduationProject | createWithProfile(process.getId(), title, file, storageKey, now) | GraduationProject |
| 9 | 4 | GraduationProjectService | GraduationProjectRepository | saveAndFlush(project) | GraduationProject |
| 10 | 4 | GraduationProjectService | ChangeLog | record(studentId, "GraduationProject", project.getId(), project.describe()) | |
| 11 | 5 | GraduationProjectService | ProfessorContacts | thesisWorkshopProfessor(), desde notifyThesisWorkshopProfessor(studentId, project) | Optional<ProfessorContact> |
| 12 | 5 | UserProfessorContacts | UserAccountRepository | findActiveWithRole(THESIS_WORKSHOP_PROFESSOR) | List<UserAccount> |
| 13 | 5 | GraduationProjectService | StudentAccounts | findById(studentId) | Optional<StudentAccount> |
| 14 | 5 | GraduationProjectService | Notifier | notify(professor.email(), "Perfil de proyecto para evaluar", body con el estudiante y el título) | |
| 15 | 6 | SubmitProjectProfileCommandHandler | Estudiante | recibe respuesta "Perfil de proyecto presentado: <título>. Se avisó al docente de Taller de Grado." | |

### A1 Presentar un perfil de proyecto nuevo

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | 2 | GraduationProjectService | GraduationProjectRepository | findByGraduationProcessId(process.getId()): el proyecto existente | Optional<GraduationProject> |
| 2 | A1.1 | GraduationProjectService | GraduationProject | checkFile(singleFile(files)) | |
| 3 | A1.2 | GraduationProjectService | FileStorage | store("graduation-projects", file.fileName(), file.content()) | String |
| 4 | 2, A1.2 | GraduationProjectService | GraduationProject | submitProfile(title, file, storageKey, now): verifica `PROFILE_OBSERVED` o `PROFILE_REJECTED` | List<FieldChange> |
| 5 | A1.2 | GraduationProjectService | GraduationProjectRepository | saveAndFlush(project) | GraduationProject |
| 6 | A1.2 | GraduationProjectService | ChangeLog | record(studentId, "GraduationProject", project.getId(), changes) | |
| 7 | A1.3 | GraduationProjectService | GraduationProjectService | sigue en las filas 11 a 15 del flujo básico | |

### A2 Entregar una versión del documento del proyecto

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A2 | Estudiante | SubmitDocumentVersionCommandHandler | envía correo con asunto `EntregarVersion(comentario)` y el documento del proyecto en PDF adjunto | |
| 2 | A2 | SubmitDocumentVersionCommandHandler | GraduationCommandSupport | required(arguments, FORM, "comentario general"), files(command.attachments()) | List<String>, List<DocumentFile> |
| 3 | A2 | SubmitDocumentVersionCommandHandler | GraduationProjectService | submitVersion(sender.accountId(), generalComment, files) | GraduationProject |
| 4 | A2.1 | GraduationProjectService | GraduationProcessRepository | findByStudentAccountIdAndStatus(studentId, IN_PROGRESS), desde processInProgress(studentId) | Optional<GraduationProcess> |
| 5 | A2.1 | GraduationProjectService | GraduationProjectRepository | findByGraduationProcessId(process.getId()) | Optional<GraduationProject> |
| 6 | A2.1 | GraduationProjectService | GraduationProcessRepository | findParticipantIds(process.getId(), "TUTOR") | List<UUID> |
| 7 | A2.2 | GraduationProjectService | GraduationProcess | isPredefenseApproved() | boolean |
| 8 | A2.1, A2.2 | GraduationProjectService | GraduationProject | checkVersionAllowed(predefenseApproved) | |
| 9 | A2.3 | GraduationProjectService | GraduationProject | checkFile(singleFile(files)) | |
| 10 | A2.4 | GraduationProjectService | FileStorage | store("graduation-projects", file.fileName(), file.content()) | String |
| 11 | A2.4 | GraduationProjectService | GraduationProject | submitVersion(generalComment, file, storageKey, predefenseApproved, now) | List<FieldChange> |
| 12 | A2.4 | GraduationProjectService | GraduationProjectRepository | saveAndFlush(project) | GraduationProject |
| 13 | A2.4 | GraduationProjectService | ChangeLog | record(studentId, "GraduationProject", project.getId(), changes) | |
| 14 | A2.5 | GraduationProjectService | ProfessorContacts | findById(tutorId), desde notifyTutor(studentId, tutorId, project) | Optional<ProfessorContact> |
| 15 | A2.5 | UserProfessorContacts | UserAccountRepository | findById(tutorId) | Optional<UserAccount> |
| 16 | A2.5 | GraduationProjectService | StudentAccounts | findById(studentId) | Optional<StudentAccount> |
| 17 | A2.5 | GraduationProjectService | Notifier | notify(tutor.email(), "Versión del documento del proyecto para revisar", body con el estudiante, el título, el número y el comentario general) | |
| 18 | A2.6 | SubmitDocumentVersionCommandHandler | Estudiante | recibe respuesta "Versión <número> del documento del proyecto entregada." | |

### A3 Entregar la versión para la defensa final

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A3.1 | GraduationProjectService | GraduationProject | submitVersion(generalComment, file, storageKey, true, now): deja `FINAL_DEFENSE_VERSION`; sigue en las filas 12 a 17 de A2 | List<FieldChange> |
| 2 | A2.6 | SubmitDocumentVersionCommandHandler | Estudiante | recibe respuesta "Versión <número> del documento del proyecto entregada como versión para la defensa final." | |

### A4 Consultar el proyecto de grado

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A4 | Estudiante | ViewGraduationProjectCommandHandler | envía correo con asunto `ConsultarProyecto()` | |
| 2 | A4 | ViewGraduationProjectCommandHandler | GraduationProjectService | viewProject(sender.accountId()) | Optional<ProjectView> |
| 3 | A4.1 | GraduationProjectService | GraduationProcessRepository | findByStudentAccountIdAndStatus(studentId, IN_PROGRESS), desde processInProgress(studentId) | Optional<GraduationProcess> |
| 4 | A4.1 | GraduationProjectService | GraduationProjectRepository | findByGraduationProcessId(process.getId()) | Optional<GraduationProject> |
| 5 | A4.1 | GraduationProjectService | ChangeLog | history("GraduationProject", project.getId()) | List<LoggedChange> |
| 6 | A4.1 | GraduationProjectService | ProjectHistory | from(changes) | List<ProjectHistoryItem> |
| 7 | A4.1 | ViewGraduationProjectCommandHandler | Estudiante | recibe respuesta con el título, el documento vigente con su estado y el historial; o "Todavía no presentó un perfil de proyecto." si no hay proyecto | |

### E1 a E5 Rechazos

Cada verificación que falla lanza `GraduationRuleViolationException`, que extiende `CommandRejectedException`.

| Excepción | Motivo |
| --- | --- |
| E1 | `PROFILE_NOT_ALLOWED`: el perfil vigente está `PROFILE_PENDING` o `PROFILE_APPROVED`, o el documento ya es una versión |
| E2 | `VERSION_PREREQUISITES`: no hay proyecto de grado, el perfil vigente no está `PROFILE_APPROVED`, o el trámite no tiene tutor |
| E3 | `VERSION_NOT_ALLOWED`, con el estado de la versión vigente: `TUTOR_REVIEW`, `TRIBUNAL_REVIEW` (los revisores que faltan los conocerá UC-006), `APPROVED` sin predefensa aprobada, o `APPROVED` de la versión para la defensa final |
| E4 | `ATTACHMENT_COUNT` si no llega un solo adjunto; `INVALID_FILE` si no es PDF, está vacío o supera 25 MB |
| E5 | `MISSING_DATA` sin título o sin comentario general; `INVALID_DATA` si sobran argumentos (GraduationCommandSupport) |

La precondición (trámite en curso) que falla responde `PROCESS_NOT_FOUND`. Messaging deshace la transacción del comando: no queda el proyecto, ni su registro, ni los avisos; el archivo ya guardado se borra. Luego responde con el mensaje de la excepción.

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | E1.1, E2.1, E3.1, E4.1, E5.1 | GraduationProjectService, GraduationProject o GraduationCommandSupport | Messaging | throw GraduationRuleViolationException(reason, detail) | |
| 2 | E1.1, E2.1, E3.1, E4.1, E5.1 | Messaging | Estudiante | recibe respuesta `<motivo>: <detalle>.` | |

## Implementation Requirements

- **Comandos:** van en el asunto; el estudiante es el remitente y su trámite es el que tiene en curso, así que no se identifica con argumentos. El título y el comentario general no pueden contener punto y coma, que separa los argumentos (NFR-IN-001).
  - `PresentarPerfil(titulo)`, con el perfil de proyecto en PDF como único adjunto.
  - `EntregarVersion(comentario)`, con el documento del proyecto en PDF como único adjunto.
  - `ConsultarProyecto()`, sin adjuntos.
- **Autorización (AD-007):** `allowedRoles() = {STUDENT}` en los tres comandos. Cada estudiante solo actúa sobre su propio trámite (NFR-SE-001), porque el servicio lo busca por `sender.accountId()`.
- **Formato y tamaño (CON-004):** `GraduationProject.checkFile` exige la extensión `.pdf` y un tipo de contenido declarado `application/pdf` o genérico (`application/octet-stream`), y rechaza un archivo vacío o de más de 26 214 400 bytes (25 MB). Los adjuntos no se reconocen por su nombre: el comando espera uno solo.
- **Reglas del documento (FR-040, FR-058):** el estado y el tipo del documento vigente deciden qué se admite.
  - Perfil nuevo: solo con `PROJECT_PROFILE` en `PROFILE_OBSERVED` o `PROFILE_REJECTED`. Reemplaza el título, el archivo y la fecha, y deja `PROFILE_PENDING`.
  - Versión nueva: con `PROJECT_PROFILE` en `PROFILE_APPROVED` (primera versión, número 1); con `TUTOR_OBSERVED` o `TRIBUNAL_OBSERVED`; o con `APPROVED` de `PROJECT_DOCUMENT` y la predefensa aprobada. Suma uno al número, guarda el comentario general, deja `TUTOR_REVIEW`, y el tipo `FINAL_DEFENSE_VERSION` si la predefensa está aprobada (A3), o `PROJECT_DOCUMENT` si no.
  - Los demás estados (`TRIBUNAL_REVIEW`, `TUTOR_REVIEW`, `PROFILE_PENDING`, `APPROVED` sin predefensa o de la versión para la defensa final) rechazan la entrega. Los cambia UC-006.
- **Predefensa:** `GraduationProcess.isPredefenseApproved()` es verdadero cuando la etapa es `PREDEFENSE_APPROVED` o una posterior; por eso las constantes de `GraduationStage` se declaran en el orden del trámite. UC-006 deja la etapa `PREDEFENSE_APPROVED`.
- **Tutor:** `findParticipantIds(processId, "TUTOR")` lee `graduation_process_participants`, que llena UC-005. Sin tutor, E2. Si la cuenta del tutor está desactivada, `ProfessorContacts.findById` devuelve vacío y el aviso se omite: el trámite ya está pendiente de reasignación (FR-034).
- **Docente de Taller de Grado:** `UserProfessorContacts.thesisWorkshopProfessor()` devuelve la única cuenta activa con `THESIS_WORKSHOP_PROFESSOR` (FR-039). Si no hay ninguna, el perfil se registra igual, el aviso se omite con una advertencia en el log, y la respuesta al estudiante dice que no hay docente de Taller de Grado para avisar.
- **Historial (FR-010, A4):** no hay tabla de versiones. Cada entrega registra en `ChangeLog` los datos del documento entregado (`documentKind`, `title`, `fileName`, `storageKey`, `versionNumber`, `generalComment`, `status`), con su valor anterior y nuevo. `ProjectHistory.from` agrupa las entradas por `changedAt`: un grupo con `storageKey` es una entrega; los grupos siguientes sin `storageKey` (los cambios de estado de UC-006) actualizan el estado de esa entrega. Todas las entradas de un `record` comparten la fecha y hora.
- **Archivos (AM-004, AD-012):** categoría `graduation-projects`. Los archivos de los perfiles y versiones anteriores no se borran nunca: el historial los cita por su clave. Un archivo guardado en un comando que se deshace se borra.
- **Transacciones:** cada comando corre en la transacción de Messaging; `GraduationProjectService` (`@Transactional`) participa en ella. El proyecto, su registro de cambios y los avisos se confirman o se deshacen juntos. El archivo se guarda después de todas las verificaciones.
- **Concurrencia:** `graduation_projects_process_uk` impide dos proyectos de grado en un trámite si llegan dos perfiles a la vez. Dos entregas a la vez sobre el mismo proyecto se resuelven con bloqueo optimista (`@Version` en `GraduationProject`): la segunda falla y el estudiante recibe el error genérico.
- **Notificaciones:** `Notifier` envía después de confirmar. El aviso al docente de Taller de Grado lleva el nombre del estudiante, su registro universitario y el título del proyecto; el aviso al tutor, además el número de la versión, el comentario general y si es la versión para la defensa final. Los textos están en `GraduationProjectService`.
- **Registro de cambios:** con `targetType = "GraduationProject"` y `actorId` el estudiante. `describe()` registra además `graduationProcessId` al crear el proyecto.

## Open Questions

- [ ] `ProjectStatus` y `PREDEFENSE_APPROVED`: los estados que cambia UC-006 (evaluación del perfil, revisiones del tutor y del tribunal, predefensa) se confirman al diseñar UC-006, incluido cómo registra qué miembros del tribunal ya revisaron. (ask: Equipo de desarrollo)
- [ ] Sin docente de Taller de Grado activo, ¿basta con registrar el perfil y avisar al estudiante en la respuesta, o se debe rechazar? (ask: Director de carrera)

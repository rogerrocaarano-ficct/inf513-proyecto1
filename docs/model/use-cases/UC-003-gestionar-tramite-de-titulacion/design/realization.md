# UC-003 Gestionar trámite de titulación: Design Realization

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |
| Use case | [UC-003](../use-case.md) |
| Analysis realization | [analysis/realization.md](../analysis/realization.md) |

## Traceability from Analysis

| Analysis class | Stereotype | Design elements |
| --- | --- | --- |
| Unassigned::GestionTramiteUI | boundary | OpenGraduationProcessCommandHandler, ReplaceInitialDocumentCommandHandler, CloseGraduationProcessCommandHandler, GraduationCommandSupport (implementan CommandHandler de Messaging) |
| Unassigned::NotificacionUI | boundary | Notifier (interface de Messaging) |
| Unassigned::GestorTramites | control | GraduationProcessService, GraduationRuleViolationException, GraduationRejectionReason |
| Unassigned::GestorUsuarios | control | StudentAccounts (puerto de GraduationProcesses), UserStudentAccounts, UserManagementService |
| Unassigned::TramiteTitulacion | entity | GraduationProcess, GraduationProcessStatus, GraduationStage, GraduationProcessRepository |
| Unassigned::DocumentoInicial | entity | InitialDocument, InitialDocumentType, DocumentFile, FileStorage (interface de Storage) |
| Unassigned::CuentaUsuario | entity | UserAccount, UserAccountRepository, StudentData, StudentAccount |
| Unassigned::RegistroCambios | entity | ChangeLog, FieldChange (interface de Audit) |

## Structural View

### Participating Elements

| Element | Kind | Subsystem | Application | Role in this use case |
| --- | --- | --- | --- | --- |
| OpenGraduationProcessCommandHandler | Class | GraduationProcesses | mail-app | Adaptador de entrada del comando `AbrirTramite` |
| ReplaceInitialDocumentCommandHandler | Class | GraduationProcesses | mail-app | Adaptador de entrada del comando `ReemplazarDocumento` |
| CloseGraduationProcessCommandHandler | Class | GraduationProcesses | mail-app | Adaptador de entrada del comando `CerrarTramite` |
| GraduationCommandSupport | Class | GraduationProcesses | mail-app | Lectura de los argumentos y de los adjuntos, compartida por los tres comandos |
| GraduationProcessService | Class | GraduationProcesses | mail-app | Servicio de aplicación: coordina cada operación, pide la cuenta del estudiante, guarda los archivos, registra el cambio y notifica |
| GraduationProcess | Class | GraduationProcesses | mail-app | Entidad JPA: trámite de titulación y su expediente, con sus reglas de apertura, reemplazo y cierre |
| InitialDocument | Class | GraduationProcesses | mail-app | Entidad JPA: un documento inicial del expediente, con la clave de su archivo |
| InitialDocumentType | Enumeration | GraduationProcesses | mail-app | Tipos de documento inicial, con la palabra que los reconoce en el nombre del archivo |
| GraduationProcessStatus | Enumeration | GraduationProcesses | mail-app | Estado del trámite: `IN_PROGRESS`, `CLOSED` |
| GraduationStage | Enumeration | GraduationProcesses | mail-app | Etapa del trámite; UC-003 usa `OPENED` y `FINAL_GRADE_RECORDED` |
| DocumentFile | Class | GraduationProcesses | mail-app | Record con un archivo recibido: nombre, tipo de contenido y contenido |
| GraduationRuleViolationException | Class | GraduationProcesses | mail-app | Rechazo de una solicitud, con su motivo; extiende `CommandRejectedException` |
| GraduationRejectionReason | Enumeration | GraduationProcesses | mail-app | Motivos de rechazo, con el texto que inicia la respuesta |
| GraduationProcessRepository | Interface | GraduationProcesses | mail-app | Puerto de salida de persistencia, implementado por Spring Data JPA |
| StudentAccounts | Interface | GraduationProcesses | mail-app | Puerto que define GraduationProcesses e implementa Users: obtiene o crea la cuenta de usuario del estudiante |
| StudentData | Class | GraduationProcesses | mail-app | Record con los datos del estudiante recibidos en el comando |
| StudentAccount | Class | GraduationProcesses | mail-app | Record con la cuenta de usuario del estudiante que devuelve el puerto |
| UserStudentAccounts | Class | Users | mail-app | Implementa `StudentAccounts` con `UserManagementService` |
| UserManagementService | Class | Users | mail-app | Obtiene o crea la cuenta de usuario del estudiante con las reglas y el aviso de UC-001 |
| UserAccount | Class | Users | mail-app | Entidad JPA de la cuenta de usuario |
| UserAccountRepository | Interface | Users | mail-app | Puerto de salida de persistencia de las cuentas de usuario |
| RejectionReason | Enumeration | Users | mail-app | Motivos de rechazo de Users; suma `INACTIVE_ACCOUNT` |
| CommandHandler, Sender, MailCommand, MailAttachment, CommandReply, CommandRejectedException | Interface y Class | Messaging | mail-app | Contrato de los manejadores de comandos (ver `messaging.md`) |
| Notifier | Interface | Messaging | mail-app | Envía los avisos al estudiante |
| ChangeLog, FieldChange | Interface y Class | Audit | mail-app | Registra los cambios |
| FileStorage | Interface | Storage | mail-app | Guarda y borra los archivos de los documentos iniciales (AM-004) |

### Command handlers

Los tres manejadores (`OpenGraduationProcessCommandHandler`, `ReplaceInitialDocumentCommandHandler`, `CloseGraduationProcessCommandHandler`) tienen los mismos miembros:

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| service | Attribute | GraduationProcessService | - |
| commandName | Operation | commandName(): String | + |
| allowedRoles | Operation | allowedRoles(): Set<String> | + |
| handle | Operation | handle(command: MailCommand, sender: Sender): CommandReply | + |

### GraduationCommandSupport

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| SECRETARY_ROLES | Attribute | static Set<String> = {PROGRAM_SECRETARY} | ~ |
| required | Operation | static required(arguments: List<String>, index: int, name: String): String | ~ |
| exactly | Operation | static exactly(arguments: List<String>, count: int, form: String): void | ~ |
| files | Operation | static files(attachments: List<MailAttachment>): List<DocumentFile> | ~ |

### GraduationProcessService

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| processes | Attribute | GraduationProcessRepository | - |
| studentAccounts | Attribute | StudentAccounts | - |
| fileStorage | Attribute | FileStorage | - |
| changeLog | Attribute | ChangeLog | - |
| notifier | Attribute | Notifier | - |
| clock | Attribute | Clock | - |
| openProcess | Operation | openProcess(requesterId: UUID, student: StudentData, files: List<DocumentFile>): GraduationProcess | + |
| replaceInitialDocument | Operation | replaceInitialDocument(requesterId: UUID, studentRegistrationNumber: String, files: List<DocumentFile>): InitialDocumentType | + |
| closeProcess | Operation | closeProcess(requesterId: UUID, studentRegistrationNumber: String): StudentAccount | + |
| processInProgress | Operation | processInProgress(studentRegistrationNumber: String): GraduationProcess | - |
| storeDocument | Operation | storeDocument(type: InitialDocumentType, file: DocumentFile): InitialDocument | - |

### GraduationProcess

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| id | Attribute | UUID | - |
| studentAccountId | Attribute | UUID | - |
| status | Attribute | GraduationProcessStatus | - |
| stage | Attribute | GraduationStage | - |
| openedAt | Attribute | Instant | - |
| closedAt | Attribute | Instant | - |
| physicalCopiesReceivedAt | Attribute | Instant | - |
| initialDocuments | Attribute | List<InitialDocument> | - |
| open | Operation | static open(studentAccountId: UUID, documents: List<InitialDocument>, now: Instant): GraduationProcess | + |
| replaceInitialDocument | Operation | replaceInitialDocument(document: InitialDocument): InitialDocument | + |
| close | Operation | close(now: Instant): List<FieldChange> | + |
| describe | Operation | describe(): List<FieldChange> | + |
| checkInProgress | Operation | checkInProgress(): void | - |
| getId, getStudentAccountId, getStatus, getStage | Operation | getters | + |

### InitialDocument

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| id | Attribute | UUID | - |
| type | Attribute | InitialDocumentType | - |
| fileName | Attribute | String | - |
| contentType | Attribute | String | - |
| sizeBytes | Attribute | long | - |
| storageKey | Attribute | String | - |
| uploadedAt | Attribute | Instant | - |
| MAX_SIZE_BYTES | Attribute | static long = 1048576 | + |
| check | Operation | static check(file: DocumentFile): void | + |
| of | Operation | static of(type: InitialDocumentType, file: DocumentFile, storageKey: String, now: Instant): InitialDocument | + |
| getType, getFileName, getStorageKey | Operation | getters | + |

### InitialDocumentType

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| REQUEST_LETTER, CURRICULUM_VITAE, ID_CARD_COPY, ENROLLMENT_SLIP | Attribute | InitialDocumentType (constantes) | + |
| keywords | Attribute | List<String>: `carta`; `hojadevida`, `cv`; `cedula`; `boleta` | - |
| label | Attribute | String: `carta de solicitud`, `hoja de vida`, `fotocopia de cédula de identidad`, `boleta de inscripción` | - |
| fromFileName | Operation | static fromFileName(fileName: String): Optional<InitialDocumentType> | + |
| classify | Operation | static classify(files: List<DocumentFile>): Map<InitialDocumentType, DocumentFile> | + |

### DocumentFile, StudentData, StudentAccount

| Element | Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- | --- |
| DocumentFile | fileName, contentType | Attribute | String | + |
| DocumentFile | content | Attribute | byte[] | + |
| StudentData | fullName, email, studentRegistrationNumber, idCard, phone | Attribute | String | + |
| StudentAccount | id | Attribute | UUID | + |
| StudentAccount | fullName, email | Attribute | String | + |

### GraduationRuleViolationException

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| reason | Attribute | GraduationRejectionReason | - |
| GraduationRuleViolationException | Operation | GraduationRuleViolationException(reason: GraduationRejectionReason, detail: String) | + |
| getReason | Operation | getReason(): GraduationRejectionReason | + |

### GraduationRejectionReason

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| MISSING_DATA, MISSING_DOCUMENT, UNRECOGNIZED_DOCUMENT, DUPLICATE_DOCUMENT, INVALID_DOCUMENT, PROCESS_IN_PROGRESS, PROCESS_NOT_FOUND, NO_FINAL_GRADE | Attribute | GraduationRejectionReason (constantes) | + |
| message | Operation | message(detail: String): String | + |

### GraduationProcessRepository

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| existsByStudentAccountIdAndStatus | Operation | existsByStudentAccountIdAndStatus(studentAccountId: UUID, status: GraduationProcessStatus): boolean | + |
| findByStudentAccountIdAndStatus | Operation | findByStudentAccountIdAndStatus(studentAccountId: UUID, status: GraduationProcessStatus): Optional<GraduationProcess> | + |
| saveAndFlush | Operation | saveAndFlush(process: GraduationProcess): GraduationProcess | + |

### UserStudentAccounts

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| service | Attribute | UserManagementService | - |
| obtain | Operation | obtain(requesterId: UUID, student: StudentData): StudentAccount | + |
| findByRegistrationNumber | Operation | findByRegistrationNumber(studentRegistrationNumber: String): Optional<StudentAccount> | + |

### UserManagementService

Solo los miembros nuevos; los de UC-001 están en su realización.

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| obtainStudentAccount | Operation | obtainStudentAccount(requesterId: UUID, data: UserAccountData): UserAccount | + |
| findStudentAccount | Operation | findStudentAccount(studentRegistrationNumber: String): Optional<UserAccount> | + |
| notifyCreated | Operation | notifyCreated(account: UserAccount): void | - |

### UserAccount

Solo los miembros que usa este caso de uso; todos existen desde UC-001.

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| create | Operation | static create(data: UserAccountData): UserAccount | + |
| describe | Operation | describe(): List<FieldChange> | + |
| isActive | Operation | isActive(): boolean | + |
| hasRole | Operation | hasRole(role: Role): boolean | + |
| getId, getFullName, getEmail | Operation | getters | + |

### UserAccountRepository

Solo los miembros que usa este caso de uso; `findByStudentRegistrationNumber` es nuevo.

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| findByEmail | Operation | findByEmail(email: String): Optional<UserAccount> | + |
| findByStudentRegistrationNumber | Operation | findByStudentRegistrationNumber(number: String): Optional<UserAccount> | + |
| saveAndFlush | Operation | saveAndFlush(account: UserAccount): UserAccount | + |

### Relationships

| From | To | Kind | Multiplicity | Navigability |
| --- | --- | --- | --- | --- |
| Cada manejador de comando | CommandHandler | Realization | — | From → To |
| Cada manejador de comando | GraduationProcessService | Association | 1 to 1 | From → To |
| Cada manejador de comando | GraduationCommandSupport | Dependency | — | From → To |
| GraduationProcessService | GraduationProcessRepository | Association | 1 to 1 | From → To |
| GraduationProcessService | StudentAccounts | Association | 1 to 1 | From → To |
| GraduationProcessService | FileStorage | Association | 1 to 1 | From → To |
| GraduationProcessService | ChangeLog | Association | 1 to 1 | From → To |
| GraduationProcessService | Notifier | Association | 1 to 1 | From → To |
| GraduationProcessRepository | GraduationProcess | Dependency | — | From → To |
| GraduationProcess | InitialDocument | Composition | 1 to 0..4 | From → To |
| GraduationProcess | GraduationProcessStatus | Association | 1 to 1 | From → To |
| GraduationProcess | GraduationStage | Association | 1 to 1 | From → To |
| InitialDocument | InitialDocumentType | Association | 1 to 1 | From → To |
| InitialDocumentType | DocumentFile | Dependency | — | From → To |
| UserStudentAccounts | StudentAccounts | Realization | — | From → To |
| UserStudentAccounts | UserManagementService | Association | 1 to 1 | From → To |
| UserStudentAccounts | StudentData, StudentAccount | Dependency | — | From → To |
| UserManagementService | UserAccountRepository | Association | 1 to 1 | From → To |
| GraduationRuleViolationException | CommandRejectedException | Generalization | — | From → To |
| GraduationRuleViolationException | GraduationRejectionReason | Association | 1 to 1 | From → To |

## Behavioral View

Los comandos llegan por correo. Messaging sondea el buzón, interpreta el asunto, identifica al remitente, comprueba que tiene el rol de `allowedRoles()` (secretario(a) de dirección de carrera, AD-007), llama a `handle(command, sender)` dentro de una transacción y envía la respuesta. Las filas "envía correo" y "recibe respuesta" resumen ese recorrido.

### Basic Flow

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | Secretario(a) de dirección de carrera | OpenGraduationProcessCommandHandler | envía correo con asunto `AbrirTramite(nombreCompleto; correo; registro; cedula; telefono)` y los cuatro documentos iniciales adjuntos | |
| 2 | 1 | OpenGraduationProcessCommandHandler | GraduationCommandSupport | required(arguments, i, nombre), para los cinco datos | String |
| 3 | 1 | OpenGraduationProcessCommandHandler | GraduationCommandSupport | files(command.attachments()) | List<DocumentFile> |
| 4 | 1 | OpenGraduationProcessCommandHandler | GraduationProcessService | openProcess(sender.accountId(), student, files) | GraduationProcess |
| 5 | 2 | GraduationProcessService | InitialDocumentType | classify(files) | Map<InitialDocumentType, DocumentFile> |
| 6 | 2 | GraduationProcessService | InitialDocument | check(file), para cada documento | |
| 7 | 3 | GraduationProcessService | StudentAccounts | obtain(requesterId, student) | StudentAccount |
| 8 | 3 | UserStudentAccounts | UserManagementService | obtainStudentAccount(requesterId, data con el rol STUDENT) | UserAccount |
| 9 | 3 | UserManagementService | UserAccountRepository | findByEmail(email), findByStudentRegistrationNumber(registro) | Optional<UserAccount> |
| 10 | 4 | UserManagementService | UserAccount | create(data) | UserAccount |
| 11 | 4 | UserManagementService | UserAccountRepository | saveAndFlush(account) | UserAccount |
| 12 | 4 | UserManagementService | ChangeLog | record(requesterId, "UserAccount", account.getId(), account.describe()) | |
| 13 | 4 | UserManagementService | Notifier | notify(account.getEmail(), "Cuenta de usuario creada", body) | |
| 14 | 5 | GraduationProcessService | GraduationProcessRepository | existsByStudentAccountIdAndStatus(student.id(), IN_PROGRESS) | boolean |
| 15 | 5 | GraduationProcessService | FileStorage | store("initial-documents", file.fileName(), file.content()), para cada documento | String |
| 16 | 5 | GraduationProcessService | InitialDocument | of(type, file, storageKey, now), para cada documento | InitialDocument |
| 17 | 5 | GraduationProcessService | GraduationProcess | open(student.id(), documents, now) | GraduationProcess |
| 18 | 5 | GraduationProcessService | GraduationProcessRepository | saveAndFlush(process) | GraduationProcess |
| 19 | 5 | GraduationProcessService | ChangeLog | record(requesterId, "GraduationProcess", process.getId(), process.describe()) | |
| 20 | 6 | GraduationProcessService | Notifier | notify(student.email(), "Trámite de titulación abierto", body) | |
| 21 | 7 | OpenGraduationProcessCommandHandler | Secretario(a) de dirección de carrera | recibe respuesta "Trámite de titulación abierto" (CommandReply) | |

### A1 El estudiante ya tiene una cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A1.1 | UserManagementService | UserAccountRepository | findByEmail(email), findByStudentRegistrationNumber(registro): la misma cuenta, activa y con el rol STUDENT | Optional<UserAccount> |
| 2 | A1.1 | UserStudentAccounts | GraduationProcessService | devuelve la cuenta existente, sin crear otra ni notificar | StudentAccount |
| 3 | A1.2 | GraduationProcessService | GraduationProcessRepository | existsByStudentAccountIdAndStatus(student.id(), IN_PROGRESS) | boolean |
| 4 | A1.2 | GraduationProcessService | GraduationProcess | sigue en las filas 15 a 21 del flujo básico | |

### A2 Reemplazar un documento inicial

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A2.1 | Secretario(a) de dirección de carrera | ReplaceInitialDocumentCommandHandler | envía correo con asunto `ReemplazarDocumento(registro)` y un adjunto | |
| 2 | A2.1 | ReplaceInitialDocumentCommandHandler | GraduationCommandSupport | exactly(arguments, 1, forma), files(command.attachments()) | List<DocumentFile> |
| 3 | A2.1 | ReplaceInitialDocumentCommandHandler | GraduationProcessService | replaceInitialDocument(sender.accountId(), registro, files) | InitialDocumentType |
| 4 | A2.2 | GraduationProcessService | StudentAccounts | findByRegistrationNumber(registro) | Optional<StudentAccount> |
| 5 | A2.2 | GraduationProcessService | GraduationProcessRepository | findByStudentAccountIdAndStatus(student.id(), IN_PROGRESS) | Optional<GraduationProcess> |
| 6 | A2.2 | GraduationProcessService | InitialDocumentType | fromFileName(file.fileName()) | Optional<InitialDocumentType> |
| 7 | A2.2 | GraduationProcessService | InitialDocument | check(file) | |
| 8 | A2.3 | GraduationProcessService | FileStorage | store("initial-documents", file.fileName(), file.content()) | String |
| 9 | A2.3 | GraduationProcessService | GraduationProcess | replaceInitialDocument(InitialDocument.of(type, file, storageKey, now)) | InitialDocument |
| 10 | A2.3 | GraduationProcessService | FileStorage | delete(replaced.getStorageKey()) | |
| 11 | A2.3 | GraduationProcessService | ChangeLog | record(requesterId, "GraduationProcess", process.getId(), List.of(change del documento)) | |
| 12 | A2.4 | ReplaceInitialDocumentCommandHandler | Secretario(a) de dirección de carrera | recibe respuesta "Documento inicial reemplazado: <tipo>" | |

### A3 Registrar los ejemplares físicos y cerrar el trámite

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A3.1 | Secretario(a) de dirección de carrera | CloseGraduationProcessCommandHandler | envía correo con asunto `CerrarTramite(registro)` | |
| 2 | A3.1 | CloseGraduationProcessCommandHandler | GraduationProcessService | closeProcess(sender.accountId(), registro) | StudentAccount |
| 3 | A3.2 | GraduationProcessService | GraduationProcessService | processInProgress(registro): filas 4 y 5 de A2 | GraduationProcess |
| 4 | A3.2, A3.3 | GraduationProcessService | GraduationProcess | close(now) | List<FieldChange> |
| 5 | A3.3 | GraduationProcessService | GraduationProcessRepository | saveAndFlush(process) | GraduationProcess |
| 6 | A3.3 | GraduationProcessService | ChangeLog | record(requesterId, "GraduationProcess", process.getId(), changes) | |
| 7 | A3.4 | GraduationProcessService | Notifier | notify(student.email(), "Trámite de titulación cerrado", body) | |
| 8 | A3.5 | CloseGraduationProcessCommandHandler | Secretario(a) de dirección de carrera | recibe respuesta "Trámite de titulación cerrado" | |

### E1 a E6 Rechazos

Cada verificación que falla lanza una excepción que extiende `CommandRejectedException`: `GraduationRuleViolationException` en GraduationProcesses, o `UserRuleViolationException` en Users, que pasa por el puerto sin cambios.

| Excepción | Motivo |
| --- | --- |
| E1 | `MISSING_DATA` (GraduationCommandSupport); `MISSING_DATA` o `INVALID_DATA` de Users si un dato no tiene el formato de UC-001 |
| E2 | `MISSING_DOCUMENT`, `UNRECOGNIZED_DOCUMENT` (un adjunto sin palabra de tipo), `DUPLICATE_DOCUMENT` (dos adjuntos del mismo tipo) o `INVALID_DOCUMENT` (formato o tamaño) |
| E3 | `DUPLICATE_EMAIL` o `DUPLICATE_REGISTRATION_NUMBER` de Users |
| E4 | `INACTIVE_ACCOUNT` de Users, o `PROCESS_IN_PROGRESS` |
| E5 | `PROCESS_NOT_FOUND`: el registro universitario no es de un estudiante con un trámite en curso |
| E6 | `NO_FINAL_GRADE`, con la etapa del trámite en el detalle |

Messaging deshace la transacción del comando: no queda la cuenta, ni el trámite, ni su registro, ni los avisos; los archivos ya guardados se borran (ver Implementation Requirements). Luego responde con el mensaje de la excepción.

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | E1.1, E2.1, E3.1, E4.1, E5.1, E6.1 | GraduationProcessService, GraduationProcess, InitialDocument, InitialDocumentType, GraduationCommandSupport o UserManagementService | Messaging | throw GraduationRuleViolationException(reason, detail) o UserRuleViolationException(reason, detail) | |
| 2 | E1.1, E2.1, E3.1, E4.1, E5.1, E6.1 | Messaging | Secretario(a) de dirección de carrera | recibe respuesta `<motivo>: <detalle>.` | |

## Implementation Requirements

- **Comandos:** van en el asunto, con los argumentos separados por punto y coma; el estudiante y su trámite se identifican por el registro universitario, porque tiene a lo sumo un trámite en curso.
  - `AbrirTramite(nombreCompleto; correo; registro; cedula; telefono)`, con los cuatro documentos iniciales adjuntos.
  - `ReemplazarDocumento(registro)`, con un solo adjunto.
  - `CerrarTramite(registro)`: registra la recepción de los dos ejemplares físicos y cierra el trámite.
- **Adjuntos (NFR-IN-001):** los adjuntos no se nombran en el comando. `InitialDocumentType.fromFileName` reconoce el tipo por una palabra del nombre del archivo, sin distinguir mayúsculas ni tildes y sin contar la extensión: `carta`; `hojadevida` o `cv`; `cedula`; `boleta`. Un adjunto que no contiene ninguna palabra, o que contiene la de dos tipos, se rechaza con `UNRECOGNIZED_DOCUMENT`; dos adjuntos del mismo tipo, con `DUPLICATE_DOCUMENT`; un tipo sin adjunto, con `MISSING_DOCUMENT`. Para `cv` se exige la palabra completa, no una parte de otra.
- **Formato y tamaño (CON-005):** `InitialDocument.check` acepta la extensión `.png`, `.jpg`, `.jpeg` o `.pdf` con un tipo de contenido `image/png`, `image/jpeg` o `application/pdf`, y un contenido de hasta 1 048 576 bytes.
- **Autorización (AD-007):** `allowedRoles() = {PROGRAM_SECRETARY}` en los tres comandos. La creación de la cuenta del estudiante no pasa por `checkCanAssign`: la autoriza la apertura del trámite (FR-051).
- **Cuenta del estudiante (UC-001, FR-056):** `obtainStudentAccount` busca por dirección de correo y por registro universitario:
  - Ninguna cuenta: crea la cuenta con el rol `STUDENT`, igual que `createAccount` pero sin `checkCanAssign`, y avisa con el mismo texto (`notifyCreated`, extraído de `createAccount`).
  - Las dos búsquedas devuelven la misma cuenta con el rol `STUDENT`: si está activa la devuelve sin cambios; si no, `INACTIVE_ACCOUNT`.
  - Cualquier otro caso (cuentas distintas, una sola coincidencia o una cuenta sin el rol `STUDENT`): `DUPLICATE_EMAIL` o `DUPLICATE_REGISTRATION_NUMBER`.
- **Transacciones:** cada comando corre en la transacción de Messaging; `GraduationProcessService` y `UserManagementService` (`@Transactional`) participan en ella. La cuenta de usuario, el trámite, sus documentos y sus registros de cambios se confirman o se deshacen juntos.
- **Archivos (AM-004, AD-012):** `FileStorage.store` escribe el archivo al llamarse y lo borra si la transacción se deshace; `FileStorage.delete` lo borra después de confirmar. Así un rechazo no deja archivos sueltos, y un reemplazo no borra el archivo anterior si el cambio se deshace. Los archivos se escriben después de todas las verificaciones (filas 15 y A2.8), para no escribir en vano.
- **Notificaciones:** `Notifier` envía después de confirmar. El estudiante recibe hasta dos avisos al abrir el trámite: la creación de su cuenta de usuario (Users) y la apertura del trámite (GraduationProcesses). Los textos están en cada servicio.
- **Concurrencia:** la regla de un solo trámite en curso por estudiante la asegura también el índice único parcial `graduation_processes_student_in_progress_uk` (ver `database.md`); `GraduationProcessService` traduce su violación a `PROCESS_IN_PROGRESS`. La unicidad de los tipos de documento por trámite la asegura `initial_documents_process_type_uk`.
- **Etapa:** `GraduationProcess.open` deja el trámite en `IN_PROGRESS` y `OPENED`. `close` exige `FINAL_GRADE_RECORDED`, que registrará UC-006; los demás valores de `GraduationStage` los agregan los casos de uso que los usan.
- **Registro de cambios:** con `targetType = "GraduationProcess"`. `describe()` registra el estado, la etapa, la cuenta de usuario del estudiante y el nombre de archivo de cada documento inicial; el reemplazo registra el documento con su nombre anterior y nuevo; el cierre, el estado, la fecha de recepción de los ejemplares físicos y la fecha de cierre.
- **GraduationParticipation:** con los trámites ya creados, su implementación provisional de UC-001 se reemplaza por una que busca en `graduation_processes.student_account_id` para `STUDENT` y en `graduation_process_participants` para `TUTOR` y `TRIBUNAL_MEMBER`. No debe depender de `GraduationProcessService`, para que no haya un ciclo entre beans (`GraduationProcessService` → `UserStudentAccounts` → `UserManagementService` → `GraduationParticipation`).

## Open Questions

- [ ] A3.2: `close` exige la etapa `FINAL_GRADE_RECORDED`, que registra UC-006; ¿se confirma esa etapa al diseñar UC-006? (ask: Equipo de desarrollo)

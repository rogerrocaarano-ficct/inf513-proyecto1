# UC-001 Gestionar usuarios: Design Realization

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |
| Use case | [UC-001](../use-case.md) |
| Analysis realization | [analysis/realization.md](../analysis/realization.md) |

## Traceability from Analysis

| Analysis class | Stereotype | Design elements |
| --- | --- | --- |
| Unassigned::GestionUsuariosUI | boundary | UserCommandHandler (implementa CommandHandler de Messaging) |
| Unassigned::NotificacionUI | boundary | Notifier (interface de Messaging) |
| Unassigned::GestorUsuarios | control | UserManagementService, UserRuleViolationException, AccountChangeResult |
| Unassigned::CuentaUsuario | entity | UserAccount, AccountStatus, UserAccountData, UserAccountRepository |
| Unassigned::Rol | entity | Role |
| Unassigned::RegistroCambios | entity | ChangeLog, FieldChange (interface de Audit) |
| Unassigned::TramiteTitulacion | entity | GraduationParticipation, ParticipantKind, GraduationProcessSummary (interface de GraduationProcesses) |

## Structural View

### Participating Elements

| Element | Kind | Subsystem | Application | Role in this use case |
| --- | --- | --- | --- | --- |
| UserCommandHandler | Class | Users | mail-app | Adaptador de entrada: recibe los comandos de gestión de usuarios, identifica y autoriza al remitente, llama al servicio y arma la respuesta |
| UserManagementService | Class | Users | mail-app | Servicio de aplicación: coordina cada operación, aplica las reglas, registra el cambio y notifica |
| UserAccount | Class | Users | mail-app | Entidad JPA: datos, roles y estado de la cuenta de usuario |
| AccountStatus | Enumeration | Users | mail-app | Estado de la cuenta de usuario: `ACTIVE`, `INACTIVE` |
| Role | Enumeration | Users | mail-app | Roles y sus reglas de alcance y de combinación |
| UserAccountData | Class | Users | mail-app | Record con los datos de una cuenta de usuario recibidos en un comando |
| AccountChangeResult | Class | Users | mail-app | Record con la cuenta de usuario cambiada y los trámites afectados |
| UserRuleViolationException | Class | Users | mail-app | Rechazo de una solicitud, con su motivo |
| RejectionReason | Enumeration | Users | mail-app | Motivos de rechazo (E1 a E5 y remitente no autorizado) |
| UserAccountRepository | Interface | Users | mail-app | Puerto de salida de persistencia, implementado por Spring Data JPA |
| CommandRegistry | Interface | Messaging | mail-app | Registra los comandos de este caso de uso |
| CommandHandler | Interface | Messaging | mail-app | Contrato que UserCommandHandler implementa |
| MailCommand | Class | Messaging | mail-app | Record con el comando recibido |
| CommandReply | Class | Messaging | mail-app | Record con la respuesta que Messaging envía al remitente |
| Notifier | Interface | Messaging | mail-app | Envía los avisos al usuario afectado |
| ChangeLog | Interface | Audit | mail-app | Registra los cambios |
| FieldChange | Class | Audit | mail-app | Record con un dato cambiado |
| GraduationParticipation | Interface | GraduationProcesses | mail-app | Marca los trámites pendientes de reasignación |
| ParticipantKind | Enumeration | GraduationProcesses | mail-app | Tipo de participación en un trámite |
| GraduationProcessSummary | Class | GraduationProcesses | mail-app | Record con un trámite afectado |

### UserCommandHandler

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| userManagementService | Attribute | UserManagementService | - |
| registerCommands | Operation | registerCommands(registry: CommandRegistry): void | + |
| handle | Operation | handle(command: MailCommand): CommandReply | + |
| toAccountData | Operation | toAccountData(arguments: List<String>): UserAccountData | - |
| rejectionReply | Operation | rejectionReply(e: UserRuleViolationException): CommandReply | - |

### UserManagementService

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| userAccountRepository | Attribute | UserAccountRepository | - |
| changeLog | Attribute | ChangeLog | - |
| notifier | Attribute | Notifier | - |
| graduationParticipation | Attribute | GraduationParticipation | - |
| authenticate | Operation | authenticate(senderEmail: String): UserAccount | + |
| createAccount | Operation | createAccount(requester: UserAccount, data: UserAccountData): UserAccount | + |
| listAccounts | Operation | listAccounts(requester: UserAccount): List<UserAccount> | + |
| findAccount | Operation | findAccount(requester: UserAccount, email: String): UserAccount | + |
| updateAccount | Operation | updateAccount(requester: UserAccount, email: String, field: String, value: String): AccountChangeResult | + |
| deactivateAccount | Operation | deactivateAccount(requester: UserAccount, email: String): AccountChangeResult | + |
| reactivateAccount | Operation | reactivateAccount(requester: UserAccount, email: String): UserAccount | + |
| checkScope | Operation | checkScope(requester: UserAccount, roles: Set<Role>): void | - |
| checkUniqueness | Operation | checkUniqueness(email: String, studentRegistrationNumber: String, excludedId: UUID): void | - |
| checkSingleThesisWorkshopProfessor | Operation | checkSingleThesisWorkshopProfessor(roles: Set<Role>, excludedId: UUID): void | - |
| checkNotLastAdministrator | Operation | checkNotLastAdministrator(account: UserAccount): void | - |

### UserAccount

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| id | Attribute | UUID | - |
| fullName | Attribute | String | - |
| email | Attribute | String | - |
| roles | Attribute | Set<Role> | - |
| studentRegistrationNumber | Attribute | String | - |
| idCard | Attribute | String | - |
| phone | Attribute | String | - |
| status | Attribute | AccountStatus | - |
| create | Operation | static create(data: UserAccountData): UserAccount | + |
| changeField | Operation | changeField(field: String, value: String): FieldChange | + |
| changeRoles | Operation | changeRoles(roles: Set<Role>): FieldChange | + |
| deactivate | Operation | deactivate(): FieldChange | + |
| reactivate | Operation | reactivate(): FieldChange | + |
| describe | Operation | describe(): List<FieldChange> | + |
| isActive | Operation | isActive(): boolean | + |
| hasRole | Operation | hasRole(role: Role): boolean | + |
| getId, getEmail, getRoles | Operation | getters | + |

### Role

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| STUDENT, PROFESSOR, THESIS_WORKSHOP_PROFESSOR, PROGRAM_SECRETARY, PROGRAM_DIRECTOR, ADMINISTRATOR | Attribute | Role (constantes) | + |
| canManage | Operation | static canManage(managerRoles: Set<Role>, targetRoles: Set<Role>): boolean | + |
| validateCombination | Operation | static validateCombination(roles: Set<Role>): void | + |
| participantKinds | Operation | participantKinds(): Set<ParticipantKind> | + |

### UserAccountData

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| fullName, email, studentRegistrationNumber, idCard, phone | Attribute | String | + |
| roles | Attribute | Set<Role> | + |

### AccountChangeResult

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| account | Attribute | UserAccount | + |
| affectedProcesses | Attribute | List<GraduationProcessSummary> | + |

### UserRuleViolationException

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| reason | Attribute | RejectionReason | - |
| detail | Attribute | String | - |
| getReason | Operation | getReason(): RejectionReason | + |

### RejectionReason

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| UNAUTHORIZED_SENDER, OUT_OF_SCOPE, DUPLICATE_EMAIL, DUPLICATE_REGISTRATION_NUMBER, INVALID_ROLE_COMBINATION, SECOND_THESIS_WORKSHOP_PROFESSOR, LAST_ADMINISTRATOR, MISSING_DATA, ACCOUNT_NOT_FOUND | Attribute | RejectionReason (constantes) | + |

### UserAccountRepository

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| findByEmail | Operation | findByEmail(email: String): Optional<UserAccount> | + |
| findAll | Operation | findAll(): List<UserAccount> | + |
| existsByEmailAndIdNot | Operation | existsByEmailAndIdNot(email: String, id: UUID): boolean | + |
| existsByStudentRegistrationNumberAndIdNot | Operation | existsByStudentRegistrationNumberAndIdNot(number: String, id: UUID): boolean | + |
| countActiveWithRole | Operation | countActiveWithRole(role: Role, excludedId: UUID): long | + |
| save | Operation | save(account: UserAccount): UserAccount | + |

### Relationships

| From | To | Kind | Multiplicity | Navigability |
| --- | --- | --- | --- | --- |
| UserCommandHandler | CommandHandler | Realization | — | From → To |
| UserCommandHandler | CommandRegistry | Dependency | — | From → To |
| UserCommandHandler | UserManagementService | Association | 1 to 1 | From → To |
| UserManagementService | UserAccountRepository | Association | 1 to 1 | From → To |
| UserManagementService | ChangeLog | Association | 1 to 1 | From → To |
| UserManagementService | Notifier | Association | 1 to 1 | From → To |
| UserManagementService | GraduationParticipation | Association | 1 to 1 | From → To |
| UserAccountRepository | UserAccount | Dependency | — | From → To |
| UserAccount | Role | Association | 1 to 1..* | From → To |
| UserAccount | AccountStatus | Association | 1 to 1 | From → To |
| Role | ParticipantKind | Dependency | — | From → To |
| AccountChangeResult | UserAccount | Association | 1 to 1 | From → To |
| AccountChangeResult | GraduationProcessSummary | Association | 1 to 0..* | From → To |
| UserRuleViolationException | RejectionReason | Association | 1 to 1 | From → To |

## Behavioral View

Los comandos llegan por correo: Messaging sondea el buzón, interpreta el asunto y llama a `UserCommandHandler.handle(command)` a través de `CommandHandler`; la respuesta (`CommandReply`) la envía Messaging al remitente. Las filas "envía correo" y "recibe respuesta" resumen ese recorrido.

### Basic Flow

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | Administrador | UserCommandHandler | envía correo con asunto `CrearUsuario(nombre; correo; roles; registro; cédula; teléfono)` | |
| 2 | 1 | UserCommandHandler | UserManagementService | authenticate(command.senderEmail()) | UserAccount |
| 3 | 1 | UserCommandHandler | UserManagementService | createAccount(requester, toAccountData(command.arguments())) | UserAccount |
| 4 | 2 | UserManagementService | Role | canManage(requester.getRoles(), data.roles()) | boolean |
| 5 | 3 | UserManagementService | UserAccountRepository | existsByEmailAndIdNot(data.email(), null) | boolean |
| 6 | 3 | UserManagementService | UserAccountRepository | existsByStudentRegistrationNumberAndIdNot(data.studentRegistrationNumber(), null) | boolean |
| 7 | 4 | UserManagementService | Role | validateCombination(data.roles()) | |
| 8 | 4 | UserManagementService | UserAccountRepository | countActiveWithRole(THESIS_WORKSHOP_PROFESSOR, null) | long |
| 9 | 5 | UserManagementService | UserAccount | create(data) | UserAccount |
| 10 | 5 | UserManagementService | UserAccountRepository | save(account) | UserAccount |
| 11 | 5 | UserManagementService | ChangeLog | record(requester.getId(), "UserAccount", account.getId(), account.describe()) | |
| 12 | 6 | UserManagementService | Notifier | notify(account.getEmail(), subject, body) | |
| 13 | 7 | UserCommandHandler | Administrador | recibe respuesta "Cuenta de usuario creada" (CommandReply) | |

### A1 El director de carrera gestiona la cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A1.1 | Director de carrera | UserCommandHandler | envía correo con cualquiera de los comandos de este caso de uso | |
| 2 | A1.1 | UserCommandHandler | UserManagementService | authenticate(command.senderEmail()) | UserAccount |
| 3 | A1.2 | UserManagementService | Role | canManage(requester.getRoles(), roles) | boolean |

### A2 Consultar cuentas de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A2.1 | Administrador | UserCommandHandler | envía correo con asunto `ListarUsuarios()` o `VerUsuario(correo)` | |
| 2 | A2.1 | UserCommandHandler | UserManagementService | listAccounts(requester) / findAccount(requester, email) | List<UserAccount> / UserAccount |
| 3 | A2.1 | UserManagementService | UserAccountRepository | findAll() / findByEmail(email) | List<UserAccount> / Optional<UserAccount> |
| 4 | A2.1 | UserManagementService | Role | canManage(requester.getRoles(), account.getRoles()) | boolean |
| 5 | A2.1 | UserCommandHandler | Administrador | recibe respuesta con la lista o los datos | |

### A3 Modificar una cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A3.1 | Administrador | UserCommandHandler | envía correo con asunto `ModificarUsuario(correo; campo; valor)` | |
| 2 | A3.1 | UserCommandHandler | UserManagementService | updateAccount(requester, email, field, value) | AccountChangeResult |
| 3 | A3.2 | UserManagementService | UserAccountRepository | findByEmail(email) | Optional<UserAccount> |
| 4 | A3.2 | UserManagementService | Role | canManage(requester.getRoles(), newRoles) | boolean |
| 5 | A3.2 | UserManagementService | Role | validateCombination(newRoles) | |
| 6 | A3.2 | UserManagementService | UserAccountRepository | existsByEmailAndIdNot(value, account.getId()) | boolean |
| 7 | A3.2 | UserManagementService | UserAccountRepository | countActiveWithRole(ADMINISTRATOR, account.getId()) | long |
| 8 | A3.3 | UserManagementService | GraduationParticipation | markPendingReassignment(account.getId(), removedRole.participantKinds()) | List<GraduationProcessSummary> |
| 9 | A3.4 | UserManagementService | UserAccount | changeField(field, value) / changeRoles(newRoles) | FieldChange |
| 10 | A3.4 | UserManagementService | ChangeLog | record(requester.getId(), "UserAccount", account.getId(), changes) | |
| 11 | A3.5 | UserManagementService | Notifier | notify(account.getEmail(), subject, body) | |
| 12 | A3.3, A3.6 | UserCommandHandler | Administrador | recibe respuesta con la confirmación y los trámites afectados | |

### A4 Desactivar una cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A4.1 | Administrador | UserCommandHandler | envía correo con asunto `DesactivarUsuario(correo)` | |
| 2 | A4.1 | UserCommandHandler | UserManagementService | deactivateAccount(requester, email) | AccountChangeResult |
| 3 | A4.1 | UserManagementService | UserAccountRepository | findByEmail(email) | Optional<UserAccount> |
| 4 | A4.1 | UserManagementService | Role | canManage(requester.getRoles(), account.getRoles()) | boolean |
| 5 | A4.1 | UserManagementService | UserAccountRepository | countActiveWithRole(ADMINISTRATOR, account.getId()) | long |
| 6 | A4.2 | UserManagementService | GraduationParticipation | markPendingReassignment(account.getId(), all ParticipantKind) | List<GraduationProcessSummary> |
| 7 | A4.3 | UserManagementService | UserAccount | deactivate() | FieldChange |
| 8 | A4.3 | UserManagementService | ChangeLog | record(requester.getId(), "UserAccount", account.getId(), changes) | |
| 9 | A4.4 | UserManagementService | Notifier | notify(account.getEmail(), subject, body) | |
| 10 | A4.2, A4.5 | UserCommandHandler | Administrador | recibe respuesta con la confirmación y los trámites afectados | |

### A5 Reactivar una cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A5.1 | Administrador | UserCommandHandler | envía correo con asunto `ReactivarUsuario(correo)` | |
| 2 | A5.1 | UserCommandHandler | UserManagementService | reactivateAccount(requester, email) | UserAccount |
| 3 | A5.1 | UserManagementService | UserAccountRepository | findByEmail(email) | Optional<UserAccount> |
| 4 | A5.1 | UserManagementService | Role | canManage(requester.getRoles(), account.getRoles()) | boolean |
| 5 | A5.1 | UserManagementService | UserAccountRepository | countActiveWithRole(THESIS_WORKSHOP_PROFESSOR, account.getId()) | long |
| 6 | A5.2 | UserManagementService | UserAccount | reactivate() | FieldChange |
| 7 | A5.2 | UserManagementService | ChangeLog | record(requester.getId(), "UserAccount", account.getId(), changes) | |
| 8 | A5.3 | UserManagementService | Notifier | notify(account.getEmail(), subject, body) | |
| 9 | A5.4 | UserCommandHandler | Administrador | recibe respuesta "Cuenta de usuario reactivada" | |

### E1 a E5 Rechazos

Cada verificación que falla lanza `UserRuleViolationException` con su `RejectionReason`: E1 `OUT_OF_SCOPE`, E2 `DUPLICATE_EMAIL` o `DUPLICATE_REGISTRATION_NUMBER`, E3 `INVALID_ROLE_COMBINATION` o `SECOND_THESIS_WORKSHOP_PROFESSOR`, E4 `LAST_ADMINISTRATOR`, E5 `MISSING_DATA`. La transacción se deshace.

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | E1.1, E2.1, E3.1, E4.1, E5.1 | UserManagementService | UserCommandHandler | throw UserRuleViolationException(reason, detail) | |
| 2 | E1.1, E2.1, E3.1, E4.1, E5.1 | UserCommandHandler | Administrador | recibe respuesta con el motivo del rechazo (rejectionReply(e)) | |

## Implementation Requirements

- **Comandos** (asunto del correo, argumentos separados por punto y coma; los roles, separados por coma con su nombre en español; la cuenta de usuario se identifica por su dirección de correo):
  - `CrearUsuario(nombreCompleto; correo; roles[; registroUniversitario; cedula; telefono])`: los tres últimos son obligatorios si un rol es estudiante.
  - `ListarUsuarios()`, `VerUsuario(correo)`.
  - `ModificarUsuario(correo; campo; valor)`, con `campo` en `nombre`, `correo`, `roles`, `registro`, `cedula`, `telefono`.
  - `DesactivarUsuario(correo)`, `ReactivarUsuario(correo)`.
- **Autorización (AD-007):** `authenticate` rechaza con `UNAUTHORIZED_SENDER` si el remitente no tiene una cuenta de usuario activa con el rol de administrador o de director de carrera; `handle` responde con ese motivo sin ejecutar el comando.
- **Transacciones:** cada operación de `UserManagementService` es una transacción (`@Transactional`): el cambio de la cuenta de usuario, el registro de cambios (AD-008) y la marca de trámites pendientes de reasignación se confirman o se deshacen juntos.
- **Notificaciones:** `Notifier` envía después de confirmar la transacción; un fallo de envío no deshace el cambio.
- **Concurrencia:** la unicidad del correo y del registro universitario también la aseguran índices únicos de la base de datos (ver `database.md`); una violación se traduce a `DUPLICATE_EMAIL` o `DUPLICATE_REGISTRATION_NUMBER`. El único docente de Taller de Grado activo y el último administrador activo solo los verifica el servicio: dos solicitudes simultáneas de distintos gestores podrían saltarse la regla, un riesgo aceptado porque los comandos se procesan uno por uno desde el buzón.
- **Validaciones:** dirección de correo con formato válido; los datos obligatorios de E5; los nombres de roles y de campos, sin distinguir mayúsculas.
- **Alcance del director de carrera (FR-050, FR-053):** `Role.canManage` permite al director de carrera solo cuentas de usuario cuyos roles son `PROFESSOR`, `THESIS_WORKSHOP_PROFESSOR` o `PROGRAM_SECRETARY`; su propia cuenta tiene `PROGRAM_DIRECTOR`, así que no puede modificarla.
- **Administrador inicial (FR-038):** al iniciar, si no existe una cuenta de usuario activa con `ADMINISTRATOR`, `mail-app` la crea con el nombre y la dirección de correo de la configuración (`application.properties`).
- **Participación por rol:** `Role.participantKinds()` asocia `STUDENT` con `ParticipantKind.STUDENT` y `PROFESSOR` con `TUTOR` y `TRIBUNAL_MEMBER`; los demás roles no participan en trámites.

## Open Questions

- [ ] A3, A4: el destino de los trámites pendientes de reasignación sigue abierto (FR-034). (ask: Secretario(a) de dirección de carrera)

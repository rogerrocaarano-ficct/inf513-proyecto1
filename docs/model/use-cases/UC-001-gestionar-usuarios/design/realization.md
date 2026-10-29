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
| Unassigned::GestionUsuariosUI | boundary | CreateUserCommandHandler, ListUsersCommandHandler, ViewUserCommandHandler, UpdateUserCommandHandler, DeactivateUserCommandHandler, ReactivateUserCommandHandler, UserCommandSupport (implementan CommandHandler de Messaging) |
| Unassigned::NotificacionUI | boundary | Notifier (interface de Messaging) |
| Unassigned::GestorUsuarios | control | UserManagementService, UserRuleViolationException, RejectionReason, AccountChangeResult |
| Unassigned::CuentaUsuario | entity | UserAccount, AccountStatus, AccountField, UserAccountData, UserAccountRepository |
| Unassigned::Rol | entity | Role |
| Unassigned::RegistroCambios | entity | ChangeLog, FieldChange (interface de Audit) |
| Unassigned::TramiteTitulacion | entity | GraduationParticipation, ParticipantKind, GraduationProcessSummary (interface de GraduationProcesses) |

## Structural View

### Participating Elements

| Element | Kind | Subsystem | Application | Role in this use case |
| --- | --- | --- | --- | --- |
| CreateUserCommandHandler | Class | Users | mail-app | Adaptador de entrada del comando `CrearUsuario` |
| ListUsersCommandHandler | Class | Users | mail-app | Adaptador de entrada del comando `ListarUsuarios` |
| ViewUserCommandHandler | Class | Users | mail-app | Adaptador de entrada del comando `VerUsuario` |
| UpdateUserCommandHandler | Class | Users | mail-app | Adaptador de entrada del comando `ModificarUsuario` |
| DeactivateUserCommandHandler | Class | Users | mail-app | Adaptador de entrada del comando `DesactivarUsuario` |
| ReactivateUserCommandHandler | Class | Users | mail-app | Adaptador de entrada del comando `ReactivarUsuario` |
| UserCommandSupport | Class | Users | mail-app | Lectura de los argumentos y textos de las respuestas, compartidos por los seis comandos |
| UserManagementService | Class | Users | mail-app | Servicio de aplicación: coordina cada operación, aplica las reglas que consultan la base de datos, registra el cambio y notifica |
| UserAccount | Class | Users | mail-app | Entidad JPA: datos, roles y estado de la cuenta de usuario, con sus datos obligatorios |
| AccountStatus | Enumeration | Users | mail-app | Estado de la cuenta de usuario: `ACTIVE`, `INACTIVE` |
| Role | Enumeration | Users | mail-app | Roles, su nombre en español y sus reglas de alcance y de combinación |
| AccountField | Enumeration | Users | mail-app | Datos que `ModificarUsuario` puede cambiar, con su nombre en español |
| UserAccountData | Class | Users | mail-app | Record con los datos de una cuenta de usuario recibidos en un comando |
| AccountChangeResult | Class | Users | mail-app | Record con la cuenta de usuario cambiada y los trámites afectados |
| UserRuleViolationException | Class | Users | mail-app | Rechazo de una solicitud, con su motivo; extiende `CommandRejectedException` |
| RejectionReason | Enumeration | Users | mail-app | Motivos de rechazo (E1 a E5), con el texto que inicia la respuesta |
| UserAccountRepository | Interface | Users | mail-app | Puerto de salida de persistencia, implementado por Spring Data JPA |
| CommandHandler | Interface | Messaging | mail-app | Contrato que implementan los seis manejadores |
| Sender | Class | Messaging | mail-app | Record con el remitente identificado por Messaging |
| MailCommand | Class | Messaging | mail-app | Record con el comando recibido |
| CommandReply | Class | Messaging | mail-app | Record con la respuesta que Messaging envía al remitente |
| CommandRejectedException | Class | Messaging | mail-app | Rechazo que deshace el comando y responde su mensaje |
| Notifier | Interface | Messaging | mail-app | Envía los avisos al usuario afectado |
| ChangeLog | Interface | Audit | mail-app | Registra los cambios |
| FieldChange | Class | Audit | mail-app | Record con un dato cambiado |
| GraduationParticipation | Interface | GraduationProcesses | mail-app | Marca los trámites pendientes de reasignación |
| ParticipantKind | Enumeration | GraduationProcesses | mail-app | Tipo de participación en un trámite |
| GraduationProcessSummary | Class | GraduationProcesses | mail-app | Record con un trámite afectado |

### Command handlers

Los seis manejadores (`CreateUserCommandHandler`, `ListUsersCommandHandler`, `ViewUserCommandHandler`, `UpdateUserCommandHandler`, `DeactivateUserCommandHandler`, `ReactivateUserCommandHandler`) tienen los mismos miembros:

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| service | Attribute | UserManagementService | - |
| commandName | Operation | commandName(): String | + |
| allowedRoles | Operation | allowedRoles(): Set<String> | + |
| handle | Operation | handle(command: MailCommand, sender: Sender): CommandReply | + |

### UserCommandSupport

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| MANAGER_ROLES | Attribute | static Set<String> = {ADMINISTRATOR, PROGRAM_DIRECTOR} | ~ |
| argument | Operation | static argument(arguments: List<String>, index: int): String | ~ |
| required | Operation | static required(arguments: List<String>, index: int, name: String): String | ~ |
| atMost | Operation | static atMost(arguments: List<String>, count: int, form: String): void | ~ |
| roles | Operation | static roles(text: String): Set<Role> | ~ |
| field | Operation | static field(text: String): AccountField | ~ |
| summary | Operation | static summary(account: UserAccount): String | ~ |
| details | Operation | static details(account: UserAccount): String | ~ |
| withAffectedProcesses | Operation | static withAffectedProcesses(confirmation: String, processes: List<GraduationProcessSummary>): String | ~ |

### UserManagementService

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| accounts | Attribute | UserAccountRepository | - |
| changeLog | Attribute | ChangeLog | - |
| notifier | Attribute | Notifier | - |
| graduationParticipation | Attribute | GraduationParticipation | - |
| createAccount | Operation | createAccount(requesterId: UUID, data: UserAccountData): UserAccount | + |
| listAccounts | Operation | listAccounts(requesterId: UUID): List<UserAccount> | + |
| findAccount | Operation | findAccount(requesterId: UUID, email: String): UserAccount | + |
| updateField | Operation | updateField(requesterId: UUID, email: String, field: AccountField, value: String): AccountChangeResult | + |
| updateRoles | Operation | updateRoles(requesterId: UUID, email: String, newRoles: Set<Role>): AccountChangeResult | + |
| deactivateAccount | Operation | deactivateAccount(requesterId: UUID, email: String): AccountChangeResult | + |
| reactivateAccount | Operation | reactivateAccount(requesterId: UUID, email: String): UserAccount | + |
| requester | Operation | requester(requesterId: UUID): UserAccount | - |
| managedAccount | Operation | managedAccount(requester: UserAccount, email: String): UserAccount | - |
| checkCanAssign | Operation | static checkCanAssign(requester: UserAccount, roles: Set<Role>): void | - |
| checkNotLastAdministrator | Operation | checkNotLastAdministrator(account: UserAccount): void | - |
| save | Operation | save(account: UserAccount): UserAccount | - |

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
| normalizeEmail | Operation | static normalizeEmail(email: String): String | + |
| changeField | Operation | changeField(field: AccountField, value: String): FieldChange | + |
| changeRoles | Operation | changeRoles(newRoles: Set<Role>): FieldChange | + |
| deactivate | Operation | deactivate(): FieldChange | + |
| reactivate | Operation | reactivate(): FieldChange | + |
| describe | Operation | describe(): List<FieldChange> | + |
| isActive | Operation | isActive(): boolean | + |
| hasRole | Operation | hasRole(role: Role): boolean | + |
| checkComplete | Operation | checkComplete(): void | - |
| getId, getFullName, getEmail, getRoles, getStudentRegistrationNumber, getIdCard, getPhone | Operation | getters | + |

### Role

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| STUDENT, PROFESSOR, THESIS_WORKSHOP_PROFESSOR, PROGRAM_SECRETARY, PROGRAM_DIRECTOR, ADMINISTRATOR | Attribute | Role (constantes) | + |
| label | Attribute | String: `estudiante`, `docente`, `docente de taller de grado`, `secretario`, `director de carrera`, `administrador` | - |
| fromLabel | Operation | static fromLabel(text: String): Optional<Role> | + |
| labels | Operation | static labels(roles: Set<Role>): String | + |
| canManage | Operation | static canManage(managerRoles: Set<Role>, targetRoles: Set<Role>): boolean | + |
| validateCombination | Operation | static validateCombination(roles: Set<Role>): void | + |
| participantKinds | Operation | static participantKinds(roles: Set<Role>): Set<ParticipantKind> | + |

### AccountField

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| FULL_NAME, EMAIL, ROLES, STUDENT_REGISTRATION_NUMBER, ID_CARD, PHONE | Attribute | AccountField (constantes) | + |
| label | Attribute | String: `nombre`, `correo`, `roles`, `registro`, `cedula`, `telefono` | - |
| property | Attribute | String: nombre del dato en el registro de cambios | - |
| fromLabel | Operation | static fromLabel(text: String): Optional<AccountField> | + |

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
| UserRuleViolationException | Operation | UserRuleViolationException(reason: RejectionReason, detail: String) | + |
| getReason | Operation | getReason(): RejectionReason | + |

### RejectionReason

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| OUT_OF_SCOPE, DUPLICATE_EMAIL, DUPLICATE_REGISTRATION_NUMBER, INVALID_ROLE_COMBINATION, SECOND_THESIS_WORKSHOP_PROFESSOR, LAST_ADMINISTRATOR, MISSING_DATA, INVALID_DATA, ACCOUNT_NOT_FOUND | Attribute | RejectionReason (constantes) | + |
| message | Operation | message(detail: String): String | + |

### UserAccountRepository

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| findById | Operation | findById(id: UUID): Optional<UserAccount> | + |
| findByEmail | Operation | findByEmail(email: String): Optional<UserAccount> | + |
| findAllByOrderByFullNameAsc | Operation | findAllByOrderByFullNameAsc(): List<UserAccount> | + |
| existsByEmail | Operation | existsByEmail(email: String): boolean | + |
| existsByEmailAndIdNot | Operation | existsByEmailAndIdNot(email: String, id: UUID): boolean | + |
| existsByStudentRegistrationNumber | Operation | existsByStudentRegistrationNumber(number: String): boolean | + |
| existsByStudentRegistrationNumberAndIdNot | Operation | existsByStudentRegistrationNumberAndIdNot(number: String, id: UUID): boolean | + |
| existsWithRoleAndStatus | Operation | existsWithRoleAndStatus(role: Role, status: AccountStatus): boolean | + |
| existsOtherActiveWithRole | Operation | existsOtherActiveWithRole(role: Role, excludedId: UUID): boolean | + |
| saveAndFlush | Operation | saveAndFlush(account: UserAccount): UserAccount | + |

### Relationships

| From | To | Kind | Multiplicity | Navigability |
| --- | --- | --- | --- | --- |
| Cada manejador de comando | CommandHandler | Realization | — | From → To |
| Cada manejador de comando | UserManagementService | Association | 1 to 1 | From → To |
| Cada manejador de comando | UserCommandSupport | Dependency | — | From → To |
| UserManagementService | UserAccountRepository | Association | 1 to 1 | From → To |
| UserManagementService | ChangeLog | Association | 1 to 1 | From → To |
| UserManagementService | Notifier | Association | 1 to 1 | From → To |
| UserManagementService | GraduationParticipation | Association | 1 to 1 | From → To |
| UserAccountRepository | UserAccount | Dependency | — | From → To |
| UserAccount | Role | Association | 1 to 1..* | From → To |
| UserAccount | AccountStatus | Association | 1 to 1 | From → To |
| UserAccount | AccountField | Dependency | — | From → To |
| Role | ParticipantKind | Dependency | — | From → To |
| AccountChangeResult | UserAccount | Association | 1 to 1 | From → To |
| AccountChangeResult | GraduationProcessSummary | Association | 1 to 0..* | From → To |
| UserRuleViolationException | CommandRejectedException | Generalization | — | From → To |
| UserRuleViolationException | RejectionReason | Association | 1 to 1 | From → To |

## Behavioral View

Los comandos llegan por correo. Messaging:

1. Sondea el buzón e interpreta el asunto.
2. Identifica al remitente y comprueba que tiene alguno de los roles de `allowedRoles()` (administrador o director de carrera, AD-007).
3. Llama a `handle(command, sender)` del manejador del comando, dentro de una transacción.
4. Envía al remitente la respuesta (`CommandReply`).

Las filas "envía correo" y "recibe respuesta" resumen ese recorrido.

### Basic Flow

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | Administrador | CreateUserCommandHandler | envía correo con asunto `CrearUsuario(nombre; correo; roles[; registro; cédula; teléfono])` | |
| 2 | 1 | CreateUserCommandHandler | UserCommandSupport | roles(arguments[2]) | Set<Role> |
| 3 | 1 | CreateUserCommandHandler | UserManagementService | createAccount(sender.accountId(), data) | UserAccount |
| 4 | 1 | UserManagementService | UserAccount | create(data) | UserAccount |
| 5 | 2 | UserManagementService | UserManagementService | checkCanAssign(requester, account.getRoles()) | |
| 6 | 3 | UserManagementService | UserAccountRepository | existsByEmail(account.getEmail()) | boolean |
| 7 | 3 | UserManagementService | UserAccountRepository | existsByStudentRegistrationNumber(account.getStudentRegistrationNumber()) | boolean |
| 8 | 4 | UserManagementService | Role | validateCombination(account.getRoles()) | |
| 9 | 4 | UserManagementService | UserAccountRepository | existsWithRoleAndStatus(THESIS_WORKSHOP_PROFESSOR, ACTIVE) | boolean |
| 10 | 5 | UserManagementService | UserAccountRepository | saveAndFlush(account) | UserAccount |
| 11 | 5 | UserManagementService | ChangeLog | record(requesterId, "UserAccount", account.getId(), account.describe()) | |
| 12 | 6 | UserManagementService | Notifier | notify(account.getEmail(), "Cuenta de usuario creada", body) | |
| 13 | 7 | CreateUserCommandHandler | Administrador | recibe respuesta "Cuenta de usuario creada" (CommandReply) | |

### A1 El director de carrera gestiona la cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A1.1 | Director de carrera | CreateUserCommandHandler | envía correo con cualquiera de los comandos de este caso de uso | |
| 2 | A1.2 | UserManagementService | Role | canManage(requester.getRoles(), roles) | boolean |

### A2 Consultar cuentas de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A2.1 | Administrador | ListUsersCommandHandler / ViewUserCommandHandler | envía correo con asunto `ListarUsuarios()` o `VerUsuario(correo)` | |
| 2 | A2.1 | ListUsersCommandHandler / ViewUserCommandHandler | UserManagementService | listAccounts(sender.accountId()) / findAccount(sender.accountId(), email) | List<UserAccount> / UserAccount |
| 3 | A2.1 | UserManagementService | UserAccountRepository | findAllByOrderByFullNameAsc() / findByEmail(email) | List<UserAccount> / Optional<UserAccount> |
| 4 | A2.1 | UserManagementService | Role | canManage(requester.getRoles(), account.getRoles()) | boolean |
| 5 | A2.1 | ListUsersCommandHandler / ViewUserCommandHandler | UserCommandSupport | summary(account) / details(account) | String |
| 6 | A2.1 | ListUsersCommandHandler / ViewUserCommandHandler | Administrador | recibe respuesta con la lista o los datos | |

### A3 Modificar una cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A3.1 | Administrador | UpdateUserCommandHandler | envía correo con asunto `ModificarUsuario(correo; campo; valor)` | |
| 2 | A3.1 | UpdateUserCommandHandler | UserCommandSupport | field(arguments[1]) | AccountField |
| 3 | A3.1 | UpdateUserCommandHandler | UserManagementService | updateRoles(sender.accountId(), email, roles(value)) si el campo es `roles`; si no, updateField(sender.accountId(), email, field, value) | AccountChangeResult |
| 4 | A3.2 | UserManagementService | UserManagementService | managedAccount(requester, email) | UserAccount |
| 5 | A3.2 | UserManagementService | UserManagementService | checkCanAssign(requester, newRoles) | |
| 6 | A3.2 | UserManagementService | Role | validateCombination(newRoles) | |
| 7 | A3.2 | UserManagementService | UserAccountRepository | existsOtherActiveWithRole(THESIS_WORKSHOP_PROFESSOR, account.getId()) | boolean |
| 8 | A3.2 | UserManagementService | UserManagementService | checkNotLastAdministrator(account), si se quita el rol de administrador | |
| 9 | A3.2 | UserManagementService | UserAccountRepository | existsByEmailAndIdNot(value, account.getId()) o existsByStudentRegistrationNumberAndIdNot(value, account.getId()), al cambiar esos datos | boolean |
| 10 | A3.3 | UserManagementService | GraduationParticipation | markPendingReassignment(account.getId(), participantKinds(oldRoles) − participantKinds(newRoles)) | List<GraduationProcessSummary> |
| 11 | A3.4 | UserManagementService | UserAccount | changeRoles(newRoles) / changeField(field, value) | FieldChange |
| 12 | A3.4 | UserManagementService | UserAccountRepository | saveAndFlush(account) | UserAccount |
| 13 | A3.4 | UserManagementService | ChangeLog | record(requesterId, "UserAccount", account.getId(), List.of(change)) | |
| 14 | A3.5 | UserManagementService | Notifier | notify(account.getEmail(), "Roles actualizados", body), solo si cambiaron los roles | |
| 15 | A3.3, A3.6 | UpdateUserCommandHandler | UserCommandSupport | withAffectedProcesses(confirmation, result.affectedProcesses()) | String |
| 16 | A3.3, A3.6 | UpdateUserCommandHandler | Administrador | recibe respuesta con la confirmación y los trámites afectados | |

### A4 Desactivar una cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A4.1 | Administrador | DeactivateUserCommandHandler | envía correo con asunto `DesactivarUsuario(correo)` | |
| 2 | A4.1 | DeactivateUserCommandHandler | UserManagementService | deactivateAccount(sender.accountId(), email) | AccountChangeResult |
| 3 | A4.1 | UserManagementService | UserManagementService | managedAccount(requester, email) | UserAccount |
| 4 | A4.1 | UserManagementService | UserAccountRepository | existsOtherActiveWithRole(ADMINISTRATOR, account.getId()), si la cuenta es de administrador | boolean |
| 5 | A4.3 | UserManagementService | UserAccount | deactivate() | FieldChange |
| 6 | A4.2 | UserManagementService | GraduationParticipation | markPendingReassignment(account.getId(), todos los ParticipantKind) | List<GraduationProcessSummary> |
| 7 | A4.3 | UserManagementService | UserAccountRepository | saveAndFlush(account) | UserAccount |
| 8 | A4.3 | UserManagementService | ChangeLog | record(requesterId, "UserAccount", account.getId(), List.of(change)) | |
| 9 | A4.4 | UserManagementService | Notifier | notify(account.getEmail(), "Cuenta de usuario desactivada", body) | |
| 10 | A4.2, A4.5 | DeactivateUserCommandHandler | Administrador | recibe respuesta con la confirmación y los trámites afectados | |

### A5 Reactivar una cuenta de usuario

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | A5.1 | Administrador | ReactivateUserCommandHandler | envía correo con asunto `ReactivarUsuario(correo)` | |
| 2 | A5.1 | ReactivateUserCommandHandler | UserManagementService | reactivateAccount(sender.accountId(), email) | UserAccount |
| 3 | A5.1 | UserManagementService | UserManagementService | managedAccount(requester, email) | UserAccount |
| 4 | A5.1 | UserManagementService | UserAccountRepository | existsOtherActiveWithRole(THESIS_WORKSHOP_PROFESSOR, account.getId()), si la cuenta tiene ese rol | boolean |
| 5 | A5.2 | UserManagementService | UserAccount | reactivate() | FieldChange |
| 6 | A5.2 | UserManagementService | UserAccountRepository | saveAndFlush(account) | UserAccount |
| 7 | A5.2 | UserManagementService | ChangeLog | record(requesterId, "UserAccount", account.getId(), List.of(change)) | |
| 8 | A5.3 | UserManagementService | Notifier | notify(account.getEmail(), "Cuenta de usuario reactivada", body) | |
| 9 | A5.4 | ReactivateUserCommandHandler | Administrador | recibe respuesta "Cuenta de usuario reactivada" | |

### E1 a E5 Rechazos

Cada verificación que falla lanza `UserRuleViolationException` con su `RejectionReason`:

| Excepción | RejectionReason |
| --- | --- |
| E1 | `OUT_OF_SCOPE` |
| E2 | `DUPLICATE_EMAIL` o `DUPLICATE_REGISTRATION_NUMBER` |
| E3 | `INVALID_ROLE_COMBINATION` o `SECOND_THESIS_WORKSHOP_PROFESSOR` |
| E4 | `LAST_ADMINISTRATOR` |
| E5 | `MISSING_DATA`, o `INVALID_DATA` para un formato o un nombre que no se reconoce |

La cuenta de usuario inexistente se rechaza con `ACCOUNT_NOT_FOUND`.

Como la excepción extiende `CommandRejectedException`, Messaging deshace la transacción del comando: no queda el cambio, ni su registro, ni los avisos. Luego responde con el mensaje de la excepción.

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | E1.1, E2.1, E3.1, E4.1, E5.1 | UserManagementService, UserAccount, Role o UserCommandSupport | Messaging | throw UserRuleViolationException(reason, detail) | |
| 2 | E1.1, E2.1, E3.1, E4.1, E5.1 | Messaging | Administrador | recibe respuesta `<motivo>: <detalle>.` | |

## Implementation Requirements

- **Comandos:** van en el asunto del correo, con los argumentos separados por punto y coma. Los roles se separan por coma y se escriben con su nombre en español; los nombres de roles y de campos no distinguen mayúsculas. La cuenta de usuario se identifica por su dirección de correo.
  - `CrearUsuario(nombreCompleto; correo; roles[; registroUniversitario; cedula; telefono])`: los tres últimos son obligatorios si un rol es estudiante.
  - `ListarUsuarios()`, `VerUsuario(correo)`.
  - `ModificarUsuario(correo; campo; valor)`, con `campo` en `nombre`, `correo`, `roles`, `registro`, `cedula`, `telefono`.
  - `DesactivarUsuario(correo)`, `ReactivarUsuario(correo)`.
- **Autorización (AD-007):** la hace Messaging antes de llamar al manejador, con `allowedRoles() = {ADMINISTRATOR, PROGRAM_DIRECTOR}`. Un remitente sin cuenta de usuario activa, o sin esos roles, recibe el rechazo de Messaging, y el manejador no se ejecuta.
- **Transacciones:**
  - Cada comando corre en la transacción que abre Messaging, y `UserManagementService` (`@Transactional`) participa en ella.
  - El cambio de la cuenta de usuario, el registro de cambios (AD-008) y la marca de trámites pendientes de reasignación se confirman o se deshacen juntos.
  - Un rechazo deshace todo (D2 de `implementar-uc-001`).
- **Notificaciones:** `Notifier` envía después de confirmar la transacción, y un fallo de envío no deshace el cambio. Los textos están en `UserManagementService`. Solo se avisa por creación, cambio de roles, desactivación y reactivación (FR-035).
- **Concurrencia:**
  - La unicidad del correo y del registro universitario la aseguran también los índices únicos de la base de datos (ver `database.md`). `save` traduce una violación de `user_accounts_email_uk` o `user_accounts_registration_uk` a `DUPLICATE_EMAIL` o `DUPLICATE_REGISTRATION_NUMBER`.
  - El único docente de Taller de Grado activo y el último administrador activo solo los verifica el servicio. Dos solicitudes simultáneas de distintos gestores podrían saltarse esas reglas, un riesgo aceptado porque los comandos se procesan uno por uno desde el buzón.
- **Validaciones:** `UserAccount.checkComplete` rechaza los datos obligatorios que faltan (E5) y una dirección de correo sin el formato `nombre@dominio.ext`. La dirección de correo se guarda en minúsculas.
- **Alcance del director de carrera (FR-050, FR-053):**
  - `Role.canManage` le permite al director de carrera solo las cuentas de usuario cuyos roles son todos `PROFESSOR`, `THESIS_WORKSHOP_PROFESSOR` o `PROGRAM_SECRETARY`. Su propia cuenta tiene `PROGRAM_DIRECTOR`, así que no puede modificarla.
  - Al gestionar una cuenta de usuario existente, el alcance se comprueba sobre sus roles actuales. Al cambiar los roles, se comprueba también sobre los nuevos.
- **Combinación de roles (FR-036):** `STUDENT`, `PROGRAM_SECRETARY` y `ADMINISTRATOR` van solos, y `THESIS_WORKSHOP_PROFESSOR` solo se combina con `PROFESSOR`. `PROGRAM_DIRECTOR` puede combinarse con `PROFESSOR`.
- **Administrador inicial (FR-038):** al iniciar, si no existe una cuenta de usuario activa con `ADMINISTRATOR`, `mail-app` la crea con el nombre y la dirección de correo de la configuración (`application.properties`).
- **Participación por rol:** `Role.participantKinds()` asocia `STUDENT` con `ParticipantKind.STUDENT`, y `PROFESSOR` con `TUTOR` y `TRIBUNAL_MEMBER`. Los demás roles no participan en trámites. Al cambiar los roles, solo se marcan los tipos de participación que la cuenta de usuario deja de tener.
- **GraduationProcesses:** mientras no existan los trámites, la implementación de `GraduationParticipation` devuelve una lista vacía (D6 de `implementar-uc-001`).

## Open Questions

- [ ] A3, A4: el destino de los trámites pendientes de reasignación sigue abierto (FR-034). (ask: Secretario(a) de dirección de carrera)

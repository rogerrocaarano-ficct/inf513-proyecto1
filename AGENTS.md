# Repository guide

CPMS (Sistema de Gestión de Trámites de Proyecto de Grado) for INF513,
Tecnología Web. The work follows the Unified Software Development Process
(USDP) as a light frame: phases and iterations, driven by use cases and risks.
Produce what the iteration needs, and no artifact only because the process
names it.

## Layout

| Path | Content |
| --- | --- |
| `README.md` | Vision: problem, process flow, approved use cases (`UC-xxx`), scope, and the project plan: team, milestones, iterations and risks (`RSK-xxx`); and the development environment: requirements and how to run the project |
| `use-cases/UC-xxx.md` | Business description of each use case: goal, actors, preconditions, basic and alternative flows, exceptions, postconditions and business rules. It cites requirements by ID; commands and design stay out. Linked from the use case table of `README.md` |
| `USER-GUIDE.md` | User guide: how to write email commands, every command with its arguments, and the system's replies. Update it in the same change that adds or changes a command |
| `REQUIREMENTS.md` | Functional (`FR-xxx`) and non-functional (`NFR-xx-xxx`) requirements |
| `GLOSSARY.md` | Domain terms, their definitions and the name each one takes in the code |
| `USDP-FRAMEWORK.md` | Reference of the process: phases, workflows, artifacts and exit criteria |
| `build.gradle.kts`, `settings.gradle.kts`, `gradle/` | Gradle build of the application `cpms` |
| `compose.yaml` | Local environment started by `./gradlew bootRun` |
| `Dockerfile`, `.dockerignore`, `compose.staging.yaml` | Image of the application and the disposable test environment deployed with Dokploy from `develop` (profile `staging`) |
| `compose.production.yaml` | Production deployed with Dokploy from `main`: only the application, against the mail server and database of tecnoweb, configured by environment variables |
| `src/main/java/bo/org/tecnoweb/cpms/` | Application code |
| `src/main/resources/` | Configuration (`application.properties`) and, later, Flyway migrations in `db/migration/` |
| `src/test/java/bo/org/tecnoweb/cpms/` | Tests |
| `.agents/skills/` | Agent skills shared by every agent ([Agent Skills](https://agentskills.io) format). `ea-drift [use case \| package]` lists the deviations of the EA model from the current code, to fix them in EA. `ea-checklist [element \| package]` presents the changes still to make by hand in EA as tables per element, read from the current model |
| `.claude/skills/` | Pointers for Claude Code, which only reads this folder: each `SKILL.md` repeats the name and description and sends to the one in `.agents/skills/`. Edit the skill in `.agents/skills/` only |
| `openspec/` | OpenSpec changes (`changes/`), archived changes (`changes/archive/`), the specs of the system (`specs/`) and its configuration (`config.yaml`) |

Each fact lives in one file. Cite it by ID (`UC-003`, `FR-012`, `RSK-002`)
instead of copying it to another document.

## Stack

| Item | Value |
| --- | --- |
| Language | Java 25 (Gradle toolchain) |
| Framework | Spring Boot 4.2.0 |
| Build | Gradle 9.9.2, through the wrapper (`./gradlew`) |
| Persistence | Spring Data JPA, PostgreSQL, Flyway migrations |
| Mail | Spring Boot mail starter (JavaMail) |
| Tests | JUnit 5, Spring Boot test starters, Testcontainers (PostgreSQL) |
| Development | Spring Boot DevTools, Docker Compose (`compose.yaml`: PostgreSQL, GreenMail, Roundcube) |

- Build and run the tests: `./gradlew build` (`gradlew.bat build` on
  Windows). The tests start PostgreSQL in a container, so Docker must be
  running.
- Run the application in the local environment: `./gradlew bootRun` (see
  "Local environment" and `README.md`).
- `./gradlew bootTestRun` starts `TestCpmsApplication` against a throwaway
  Testcontainers database, without the mail server.
- The base package is `bo.org.tecnoweb.cpms`. Below it there is one package
  per EA design subsystem, each split in layers:

  | Package | Content |
  | --- | --- |
  | `common` | Shared domain types from `Design/common` (`AggregateRoot`, `DomainEvent`, `DomainException`, …), no layers |
  | `<subsystem>.domain` | Domain classes of the EA package `Design/<Subsystem>/domain`, without framework annotations |
  | `<subsystem>.application` | Application services and ports (interfaces) of `Design/<Subsystem>/application`: one package `<subsystem>.application.<usecase>` per use case (see "How a use case is implemented"), and at the root only the ports several use cases share. `mail` and `storage` have no use cases and keep a flat `application` |
  | `<subsystem>.infrastructure` | Adapters that implement the ports (JPA, POP3, SMTP, schedulers, configuration). Not modeled in EA |

  Subsystems: `mail`, `users`, `procedures`, `storage`. A new subsystem follows the same layers and
  the "Reference Architecture" below.
- Add a dependency only when a use case needs it, and say why in the commit.

### Local environment

The requirements and the steps to set up and use the local environment are in
`README.md`, section "Entorno de desarrollo". In short: `./gradlew bootRun`
starts `compose.yaml` (PostgreSQL, GreenMail, Roundcube) with the `local`
profile, and the webmail is at <http://localhost:8025>.

The deployed test environment (`compose.staging.yaml`, profile `staging`) runs
the same services on the team's Dokploy server and publishes only the webmail.
Both environments are disposable: with the `local` and `staging` profiles,
`DisposableDatabaseReset` cleans the database and
`storage.infrastructure.DisposableStorageReset` empties the files folder at
every startup, including DevTools restarts. See `README.md`, "Entorno de
pruebas desplegado". Production (`compose.production.yaml`, default profile)
never does either.

## Reference Architecture

This is the architecture that the subsystems `users` and `mail` already
follow. When the EA model has the design of a use case, the names come from
the model and the structure comes from this section. When the model has no
design for it, this section is the design (see "Working without the model").
Each rule names the file of `users` that shows it; read it before writing a
new one.

### Domain (`<subsystem>.domain`)

No Spring or JPA annotations.

- One aggregate root per concept, implementing `common.AggregateRoot`: a
  `UUID` id, its own rules and validations, and the domain events it raises,
  collected with `pullEvents()`. A new instance is built with `builder()` and
  `with*` methods and registered with an operation that validates it
  (`User.signUp(requester)`); an instance read from storage is built with
  `rehydrate()` and raises no events (`User`).
- Domain events are records that implement `DomainEvent`, named
  `<Aggregate><PastVerb>Event` (`UserSignedUpEvent`).
- The repository of the aggregate is a port in `domain`,
  `<Aggregate>Repository`, that takes and returns domain objects
  (`UserRepository`). `mail.application.ReceivedEmailRepository` is an older
  exception; do not copy it.
- Errors: `DomainValidationException` with one Spanish message per field,
  `UserNotAuthorizedException` when the sender may not do it, and a subclass of
  `DomainException` for anything else.
- Authorization goes through a permission enum that lists the allowed roles
  (`RolePermission`, `User.hasPermissionTo`). Never compare roles directly
  (`role == Role.ADMINISTRATIVE`) in a use case.

### How a use case is implemented

The mail channel (`mail`) already receives the emails, parses the subject,
checks the number of arguments and attachments, opens the transaction and
replies with the error when a `DomainException` is thrown. A use case does not
touch any of that: it plugs into the channel by implementing
`mail.application.EmailCommandHandler`.

1. **Use case.** One class `<Name>UseCase` per use case, in its own package
   `<subsystem>.application.<usecase>`, where `<usecase>` is the class name in
   lowercase without `UseCase` (`users.application.usermanagement`,
   `procedures.application.projectprofileevaluation`). The package holds the
   use case and everything that belongs only to it: its notifier, its
   contracts and its handlers (points 2 to 4), and a port that only it uses
   (`degreeprocedure.AcademicCalendar`). Renaming the class renames the
   package. The use case is plain Java, with its dependencies through the
   constructor. **Each flow of the
   use case (basic flow, alternative flows) is one public method**, and its
   Javadoc names the flow (`UserManagementUseCase.signUpUser` is the basic
   flow, `listUsers` is A3). Each method: gets the active requester
   (`requireActiveRequester`), checks the permission, runs the domain
   operation, saves through the repository, and passes the events from
   `pullEvents()` to the notifier.
2. **One handler per email command,** implementing `EmailCommandHandler`:
   - `<Verb><Object>CommandHandler` if the command changes state,
     `<Verb><Object>QueryHandler` if it only reads (`SignUpUserCommandHandler`,
     `ListUsersQueryHandler`).
   - A constant `public static final String ACTION` with the command name
     (`usuarios.crearCuenta`), returned by `actionKey()`.
   - `requiredArgs()` and `requiredFiles()` return the exact counts.
   - `handle(email)` has no logic: it reads `email.parseArgs()` (and
     `email.getAttachmentIds()`, the stored files of the attachments), builds
     the contract and calls one method of the use case with `email.getFrom()`
     as the requester.
3. **Contract.** As a rule, the arguments are grouped in a record
   `<Verb><Object>Command` or `<Verb><Object>Query` in the package of the use
   case (`SignUpUserCommand`; `ListUsersQuery` is used even with one argument). The
   only accepted exception is a command whose single argument identifies the
   affected entity, which may pass it directly
   (`DisableUserCommandHandler` calls `disableUser(requesterEmail, userEmail)`).
   Skipping the record is not the norm; the design of the change says why.
4. **Replies and notices.** An outgoing port `<UseCase>Notifier` in the
   package of the use case, with one method per reply or notice, taking domain events or
   result records (`UserManagementNotifier`). The use case and the handler
   never send email themselves.
5. **Wiring.** The use case and each handler are declared as `@Bean` in the
   subsystem configuration (see Infrastructure); the mail channel finds the
   handlers by itself. No generic handlers, lambdas or private records inside
   the configuration.
6. **Other subsystems.** Reuse the domain and application of another subsystem
   instead of repeating its rules or its messages (for instance, a student
   account created by another use case follows the rules and the notice of
   UC-001). Never depend on the infrastructure of another subsystem.

### Infrastructure (`<subsystem>.infrastructure`)

Not modeled in EA.

- `<Subsystem>Configuration`: package-private,
  `@Configuration(proxyBeanMethods = false)`, declares the use case and the
  handlers (`UsersConfiguration`).
- `Mail<UseCase>Notifier`: a `@Component` that implements the notifier,
  publishes each event with `ApplicationEventPublisher` and sends the emails
  with `EmailSender` in `@TransactionalEventListener(phase = AFTER_COMMIT)`
  methods. A failed send is logged and not rethrown
  (`MailUserManagementNotifier`).
- `infrastructure.persistence`: a package-private `Jpa<Aggregate>Repository`
  (`@Repository`, `@Transactional`) implements the repository port, maps the
  aggregate to and from a package-private JPA entity `<Table>Entity`, and
  queries through `SpringData<Table>Repository extends JpaRepository`
  (`JpaUserRepository`, `UserAccountEntity`,
  `SpringDataUserAccountRepository`).
- **No SQL in `src/main`.** All data access goes through the ORM, with Spring
  Data derived methods (`findBy…`, `existsBy…`). No `JdbcTemplate`, no
  `EntityManager`, no native queries, no SQL in strings. If a query cannot be
  written that way, ask the user before going on. The only SQL is the schema,
  in a new Flyway migration `V<n>__<name>.sql`; never edit a migration that is
  already in `develop`. In `src/test`, `JdbcTemplate` is allowed only to clean,
  prepare and check data, as the integration tests already do.

### Transactions

`InboxPollerService` runs `handle` inside the transaction that also marks the
email as processed. The use case does not open its own. A `DomainException`
rolls it back and its message goes to the sender. Emails leave only after the
commit, through the notifier.

### File storage

Files (attachments and documents) are kept on disk by the `storage`
subsystem; the database keeps only their metadata (`stored_file`).

- Every subsystem stores and reads files only through
  `storage.application.StoredFileService` (`store`, `get`, `readContent`).
  Never use `StoredFileRepository` or `FileStorage` from another subsystem.
- The mail channel stores every attachment when it receives the email, and
  the handler passes the identifiers of the stored files to the use case
  (`email.getAttachmentIds()`), never their content. The attachments of every
  email are kept, also when the email is discarded or its command fails.
- A use case that receives a document checks it with `get` (name, type,
  size) and `readContent`, and adopts it by keeping its `UUID`: the file is
  not copied (`DegreeProcedureUseCase.open`).
- **Versions are business data.** An aggregate that keeps versions of a
  document stores each one in its own table, numbered from 1, with the
  `UUID` of the stored file of that version and the data of the business
  (state, observations, dates): `DegreeProject` and
  `degree_project_profile_version` (UC-004). Every version is an ordinary
  stored file; the storage does not chain them.
- **Deprecated:** the version history of the storage (`appendVersion`,
  `storeNewVersion`, `getHistory`). New code does not use it. Only the
  replacement of opening requirements (`DegreeProcedureUseCase.replaceRequirement`,
  UC-002) still does, until it moves to versions in its own table.
- The service runs inside the caller's transaction, like a use case. A file
  written in a transaction that rolls back is deleted, so the use case does
  nothing to clean up.
- No bytes in the database: no `byte[]` field in a JPA entity and no new
  `BYTEA` column.
- Size and format limits of each document (NFR-005) are rules of the use case
  that receives it, checked before it adopts the file
  (`InitialDocument.requirePdf`).
- Tests of a use case build a real `StoredFileService` over in-memory doubles
  of its ports (`ProcedureFixtures.Storage`); its integration test uses the
  throwaway folder that `TestcontainersConfiguration` sets in
  `cpms.storage.root`.

### Documentation and tests

- Javadoc in English on every type and every public or package-private method,
  citing the use case and requirement IDs (`UC-001`, `FR-035`).
- The tests mirror the packages: unit tests of the domain, of the use case with
  test doubles of its ports, of each handler and of the notifier
  (`UserTest`, `UserManagementUseCaseTest`, `SignUpUserCommandHandlerTest`,
  `MailUserManagementNotifierTest`); a repository test with Testcontainers
  (`JpaUserRepositoryTest`); a wiring test (`UsersWiringTest`); and one
  integration test per command with GreenMail (`SignUpByEmailIntegrationTest`).

## Languages

| Setting | Value |
| --- | --- |
| Documentation language | Spanish |
| Code language | English |

- **Spanish:** the content of `README.md`, `REQUIREMENTS.md`, `GLOSSARY.md`,
  `USER-GUIDE.md` and `use-cases/`, branch names taken from a use case, and the description
  and body of commit messages.
- **English:** identifiers, comments, log messages and database names in the
  code; `AGENTS.md`, `USDP-FRAMEWORK.md` and the commit types.
- Text the system shows to its users (mail replies, error messages) is in
  Spanish.
- A domain term takes in the code the name in the column "Término adoptado en
  el código" of `GLOSSARY.md`. If the column is empty, propose a name and add
  it to the glossary in the same change.
- IDs keep their format in any language: `UC-001`, `FR-001`, `RSK-001`,
  lowercase kebab-case for branch names.

## Working Rules

- Work is driven by use cases. Before changing a document or code, know which
  use case it belongs to (`UC-xxx` in `README.md`), or whether it is shared
  work.
- Keep documents and code consistent in the same change: a change in behavior
  updates `REQUIREMENTS.md` if it touches a requirement; a new domain term
  goes to `GLOSSARY.md`.
- Do not invent requirements or business rules. When the documents do not
  answer a question, ask the user.
- Keep tables in their current columns. Do not add back columns that were
  removed (use case, priority, source, probability, impact…).

## Changes to the Code (OpenSpec)

Every change to the source code (`src/`, the build files and the Flyway
migrations) goes through an OpenSpec change. Changes that only touch the
documents in the root (`README.md`, `REQUIREMENTS.md`, `GLOSSARY.md`,
`USER-GUIDE.md`) and the use case descriptions (`use-cases/`) do not.

1. **Propose** (`/opsx:propose` or `/opsx:explore` first): create the change
   in `openspec/changes/<name>/` with its proposal, specs, design and tasks.
   Name it in lowercase kebab-case, starting with the use case ID when it has
   one (e.g. `uc-001-crear-cuenta`).
2. **Apply** (`/opsx:apply`): implement the tasks of the change, and only
   those. If the implementation needs something the change does not cover,
   update the change first (`/opsx:update`).
3. **Archive** (`/opsx:archive`): when the tasks are done and the tests pass,
   archive the change so its specs are merged into `openspec/specs/`.

- Do not edit code outside a change. If the user asks for a code change
  without one, propose creating it first.
- Create the change with the OpenSpec CLI or skills (`/opsx:propose`), never
  by hand, so that it has its `.openspec.yaml` and every artifact.
- Mark each task as done when it is done, not all of them at the end.
- One change belongs to one use case or to one piece of shared work, like the
  branches (see Git).
- OpenSpec artifacts are documentation: write them in Spanish, with
  requirement and scenario keywords as OpenSpec defines them.

## Enterprise Architect Model

The use cases, analysis and design are modeled in Enterprise Architect (EA).
The model is not in git: it is read from the EA instance open on the user's
machine, through the Enterprise Architect MCP server.

- When proposing, exploring or applying an OpenSpec change, read the model
  first, when it is available: find the use case by its ID and the analysis and design elements and
  diagrams that realize it (classes, interfaces, operations, attributes,
  relations). Cite them by package path and name in the proposal and the
  design of the change.
- The code follows the design in EA: class, interface, operation and attribute
  names come from the model.
- **Warn the user** before proposing or applying anything the model, when it
  has the design of the use case, does not back: a class, operation, attribute, relation or behavior that is not in EA,
  or that contradicts it. Say what is missing or different, and ask whether to
  update the model first or to go on. If they go on, record the gap in the
  design of the change under a heading "Diferencias con el modelo".
- Only read the model. Do not create or change EA elements unless the user
  asks for it.
- If the EA tools do not respond (EA closed, the project not loaded, or no EA
  on this machine), or the use case has no design in the model, follow
  "Working without the model". Never go on from memory or from guesses about
  the model.

### Working without the model

Not every member of the team can read or edit the EA model. Without it, the
work goes on with the "Reference Architecture" as its design:

- Tell the user once that the model is not available or has no design for
  the use case, and go on. Do not stop the change for that.
- The structure comes from "Reference Architecture"; the names come from the
  column "Término adoptado en el código" of `GLOSSARY.md` and from
  `REQUIREMENTS.md`. Propose the names that are missing and add them to the
  glossary in the same change.
- Do not invent a structure different from the reference. If the use case
  needs something the reference does not cover (a new kind of adapter, file
  storage, a new channel…), ask the user before designing it.
- In the design of the change, the heading "Diferencias con el modelo" says
  "UC-xxx sin diseño en EA; se sigue la arquitectura de referencia" and lists
  the classes, operations and relations the change creates, so that whoever
  keeps the model can bring them to EA later with `ea-drift`.
- The task that checks the implementation compares it with the "Reference
  Architecture" instead of the EA elements.

## Git

- Branches:
  - `main`: what was delivered. It only receives merges from `develop`.
  - `develop`: integration of the current work.
  - `UC-xxx-<name>` for a use case (e.g. `UC-001-gestion-de-usuarios`), or a
    short descriptive name for shared work (e.g. `glosario`, `build`), both
    created from `develop` and merged back into it.
- Never commit directly to `main` or `develop`.
- Commits follow Conventional Commits: `<type>(<scope>): <description>`.
  - Type: `feat`, `fix`, `docs`, `test`, `refactor`, `build`, `chore`.
  - Scope: the use case ID (`feat(UC-001): …`) or the area (`docs(glossary): …`).
  - Description: imperative, in Spanish, lowercase, no final period; first
    line at most 72 characters.
- One logical change per commit: do not mix two use cases.
- Before every commit, show the user the files and the message, and commit
  only after they confirm.

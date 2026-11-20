# Repository guide

CPMS (Sistema de Gestión de Trámites de Proyecto de Grado) for INF513,
Tecnología Web. The work follows the Unified Software Development Process
(USDP) as a light frame: phases and iterations, driven by use cases and risks.
Produce what the iteration needs, and no artifact only because the process
names it.

## Layout

| Path | Content |
| --- | --- |
| `README.md` | Vision: problem, process flow, approved use cases (`UC-xxx`), scope, and the project plan: team, milestones, iterations and risks (`RSK-xxx`) |
| `REQUIREMENTS.md` | Functional (`FR-xxx`) and non-functional (`NFR-xx-xxx`) requirements |
| `GLOSSARY.md` | Domain terms, their definitions and the name each one takes in the code |
| `USDP-FRAMEWORK.md` | Reference of the process: phases, workflows, artifacts and exit criteria |
| `build.gradle.kts`, `settings.gradle.kts`, `gradle/` | Gradle build of the application `cpms` |
| `src/main/java/bo/org/tecnoweb/cpms/` | Application code |
| `src/main/resources/` | Configuration (`application.properties`) and, later, Flyway migrations in `db/migration/` |
| `src/test/java/bo/org/tecnoweb/cpms/` | Tests |
| `.agents/skills/` | Agent skills shared by every agent ([Agent Skills](https://agentskills.io) format). `ea-drift [use case \| package]` lists the deviations of the EA model from the current code, to fix them in EA |
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
| Development | Spring Boot DevTools |

- Build and run the tests: `./gradlew build` (`gradlew.bat build` on
  Windows). The tests start PostgreSQL in a container, so Docker must be
  running.
- Run the application against a container database:
  `./gradlew bootTestRun` (it starts `TestCpmsApplication`).
- The base package is `bo.org.tecnoweb.cpms`. The package structure below it
  is not fixed yet: propose it to the user before creating the first
  packages, and record it here once it is agreed.
- Add a dependency only when a use case needs it, and say why in the commit.

## Languages

| Setting | Value |
| --- | --- |
| Documentation language | Spanish |
| Code language | English |

- **Spanish:** the content of `README.md`, `REQUIREMENTS.md` and
  `GLOSSARY.md`, branch names taken from a use case, and the description and
  body of commit messages.
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
documents in the root (`README.md`, `REQUIREMENTS.md`, `GLOSSARY.md`) do not.

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
- One change belongs to one use case or to one piece of shared work, like the
  branches (see Git).
- OpenSpec artifacts are documentation: write them in Spanish, with
  requirement and scenario keywords as OpenSpec defines them.

## Enterprise Architect Model

The use cases, analysis and design are modeled in Enterprise Architect (EA).
The model is not in git: it is read from the EA instance open on the user's
machine, through the Enterprise Architect MCP server.

- When proposing, exploring or applying an OpenSpec change, read the model
  first: find the use case by its ID and the analysis and design elements and
  diagrams that realize it (classes, interfaces, operations, attributes,
  relations). Cite them by package path and name in the proposal and the
  design of the change.
- The code follows the design in EA: class, interface, operation and attribute
  names come from the model.
- **Warn the user** before proposing or applying anything the model does not
  back: a class, operation, attribute, relation or behavior that is not in EA,
  or that contradicts it. Say what is missing or different, and ask whether to
  update the model first or to go on. If they go on, record the gap in the
  design of the change under a heading "Diferencias con el modelo".
- Only read the model. Do not create or change EA elements unless the user
  asks for it.
- If the EA tools do not respond (EA closed or the project not loaded), tell
  the user and ask them to open it. Do not go on from memory or from guesses
  about the model.

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

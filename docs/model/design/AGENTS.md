# Design model: filling instructions

Read `../AGENTS.md` first: it holds the rules common to the whole model (writing
rules, derived fields, IDs, references, status and iterations).

This folder holds the parts of the design model of the USDP (see
`../../usdp-guide.md`) that are shared by all use cases. The design starts from
the requirements (`../requirements/`) and the analysis model (`../analysis/`),
and gives the system a physical form according to the technology and the
non-functional requirements.

| Path | Design phase | Content |
| --- | --- | --- |
| `architecture.md` | Basis for all design phases | Architectural decisions, applications with their architectural style, and design mechanisms |
| `subsystems/subsystems.md` | Identification of interfaces | Subsystem catalog |
| `subsystems/_template.md` | Identification of interfaces | Template for one subsystem |
| `subsystems/<subsystem>.md` | Identification of interfaces | Provided and required interfaces of one subsystem |
| `deployment.md` | Deployment diagram | Nodes, infrastructure, allocation and connections |
| `database.md` | Logical database model | Tables, columns, keys, indexes and relationships |

The other design phases live in each use-case folder (see
`../use-cases/AGENTS.md`):

| Path | Design phase |
| --- | --- |
| `../use-cases/UC-xxx-<name>/design/realization.md` | Use cases – design: classes++ and sequence, as tables |
| `../use-cases/UC-xxx-<name>/design/ui.md` | User interface design (only for systems with a user interface) |

The final domain model (the complete class diagram of the system) is not kept
by hand. It is generated from the source code when it is needed; see "Final
Domain Model" below.

## Specific Rules

- Names in the design model are in the code language (`../../../AGENTS.md`,
  Languages). Domain names take the Implementation name of their glossary
  term; technical names (frameworks, DTOs, infrastructure classes) follow the
  conventions of the chosen technology.
- Every design decision must respect the non-functional requirements. If one
  cannot be met, record it in "Open Questions" and in the decision.

## Order of Work

1. Architectural decisions, applications and design mechanisms
   (`architecture.md`).
2. Subsystems and their interfaces (`subsystems/`).
3. Deployment (`deployment.md`) and logical database model (`database.md`).
4. Design realization of each use case, starting with the architecturally
   significant ones, and its user interface design.

Steps 1 to 3 must be stable when Elaboration closes. Step 4 covers the
significant use cases in Elaboration and all the others in Construction.

## architecture.md

The architecture of the system as a whole. Three concepts must not be mixed:

- **Subsystem:** a vertical part of the business (`Sales`, `Inventory`),
  realized from an analysis package. Subsystems are not created from the
  architectural style.
- **Application:** a deployable program (frontend, backend, mobile app, batch
  process). Which applications exist is an architectural decision.
- **Architectural style and technical layers:** they belong to each
  application. Different applications can have different styles.

A subsystem is usually spread across applications: its views in the frontend,
its logic and persistence in the backend. In each application, its part
follows the technical layers of that application.

### Architectural Decisions

- One row per decision that affects the structure of the system:
  applications and their architectural styles, frameworks, databases,
  communication, deployment.
- **Rationale:** why it was chosen, citing the requirements (especially those
  marked `(A)`) or risks it addresses.
- **Alternatives considered:** what else was evaluated and why it was
  rejected.
- **Status:** Proposed / Accepted / Superseded by AD-00x. Do not delete
  superseded decisions; they explain the history.

### Applications

- One row per deployable program. A system with a single program has one row.
- **Kind:** Frontend / Backend / Mobile / Desktop / Batch.
- **Architectural style** and **Technical layers:** the style of this
  application and its layers from top to bottom. They organize the code
  inside the application; they are not subsystems.
- **Subsystem parts it holds:** which part of which subsystems lives in this
  application (e.g. "Presentation of Sales"). Every subsystem must appear in
  at least one application.
- **Decision:** the `AD-xxx` that created the application and chose its
  style.

### Design Mechanisms

- One row for each analysis mechanism (`AM-xxx`) of
  `../analysis/architecture.md`: how the design solves it, with which
  technology, and which decision chose it.
- Every analysis mechanism must have a design mechanism before Elaboration
  closes.

### Architecture Views

- The architecture description of the USDP has four views. This table only
  says where each one is described; it has no content of its own and is not
  filled in per project, except to update a link if a file moves.
- The implementation view is added when the implementation workflow is
  modeled.

## subsystems/subsystems.md

### Subsystem Catalog

- One row per subsystem, linked to its file.
- **Type:** Business (implements the business, usually from an analysis
  package) or General (provides technical services such as persistence,
  security or messaging, usually from a design mechanism). A business
  subsystem may depend on general ones, not the other way around.
- **Comes from:** the analysis package it realizes, the analysis mechanism it
  implements, or `New` with the reason in the subsystem's Responsibility.
  When packages are merged or split, list the analysis classes the subsystem
  takes from each package: `Sales (SaleForm, SaleHandler, Sale)`. Without
  that list it is not possible to know which relationships cross subsystems.
  Every analysis package must end up in a subsystem: kept as is, merged with
  another or split.
- **Provides (derived)** and **Requires (derived):** the interface names,
  from the Provided Interfaces and Required Interfaces of the subsystem
  file, which is the source.

## Subsystem Files (from subsystems/_template.md)

This is where interfaces are identified. The analysis only recorded which
packages depend on which and why; the design turns every dependency that
crosses subsystems into an interface.

To add a subsystem:

1. Add a row to the Subsystem Catalog.
2. Copy `subsystems/_template.md` to `subsystems/<subsystem>.md`, in lowercase
   kebab-case (e.g. `sales.md`).
3. Link the name in the catalog to the file.

How to identify the interfaces:

1. Collect the needs of this subsystem from other subsystems, from three
   sources:
   - **Analysis dependencies:** the rows of the Dependencies table of each
     `package.md` the subsystem comes from (`Caused by`, `Reason`), but only
     when the other package ended up in a different subsystem. Dependencies
     between packages merged into this subsystem are internal and need no
     interface.
   - **Split packages:** when a package was split, the links of the
     Structural Views of the analysis realizations between classes that now
     live in different subsystems. The analysis did not record them as
     dependencies because they were inside one package.
   - **Design mechanisms:** what this subsystem needs from General subsystems
     (persistence, security, messaging), according to the Design Mechanisms
     of `architecture.md`.
2. Group those needs into interfaces of the subsystem that provides them.
   Name each interface after the service it offers (`ProductCatalog`), not
   after the class that implements it.
3. Add the interface to the Provided Interfaces of the provider, and to the
   Required Interfaces of every subsystem that uses it, with the source it
   comes from.
4. Add the operations the use-case realizations need. Add more only when a
   realization needs them.

Sections:

- **Responsibility:** one short paragraph. For a merged or split package,
  explain why.
- **Provided Interfaces:** one block per interface.
  - **Used by (derived):** the other subsystems that call it, from their
    Required Interfaces. A subsystem's own parts (e.g. its views in the
    frontend) call its classes directly and are not listed here.
  - **Use cases (derived):** the use cases whose design realization calls
    it.
  - **Operations:** full signature with types, what it does, and the errors
    it can raise.
- **Required Interfaces:** the interfaces of other subsystems this one uses,
  who provides them, what for, and where the need comes from:
  - `Sales::SaleHandler → Inventory::Product`: an analysis dependency.
  - `Split: Sales::ReportHandler → Sales::Sale`: a link between classes of a
    split package.
  - `AM-001`: a design mechanism.

Subsystems are parts of one system, not independent applications. They
share the architecture and the database, and live in the applications of
`architecture.md`. The interfaces keep them decoupled in the code: a subsystem
calls another one only through its provided interfaces. Internal classes are
not documented here; the significant ones appear in the design realizations
that use them, and the rest live in the code.

## deployment.md

How the software is deployed: servers, client devices and infrastructure.

- **Nodes:** every physical or virtual machine, container host, client device,
  managed service or external system the software runs on or talks to.
- **Infrastructure:** what supports the nodes: networks and zones, load
  balancers, DNS, firewalls, storage, cloud services.
- **Allocation:** each application of `architecture.md`, the deployable unit
  that packages it (executable, container image, bundle), the node where it
  runs and how many instances. Every application must be allocated. What each
  application contains is defined in `architecture.md`, not here.
- **Connections:** who talks to whom, with protocol, port and security.
- **Environments:** how development, test or staging differ from production.
  Omit if there is only one environment.
- **Requirements Check:** for each performance, reliability, availability or
  security requirement that depends on the deployment, how it is met.

## database.md

The logical database model, designed before writing migrations. It is derived
from the entity classes of the analysis and the design decisions on
persistence.

- **Database:** the database engine and the decision that chose it.
- **Tables:** one row per table, with the entity class (or relationship) it
  comes from. The database is one model for the whole system; tables are not
  split by subsystem.
- **Table Details:** one block per table, in the same order.
  - **Column types:** use the types of the chosen database engine.
  - **Key:** `PK`, `FK → table.column`, `UK` (unique) or empty.
  - **Constraints:** checks and rules that the columns alone do not show.
  - **Indexes:** each one with the use case or query that needs it.
- **Relationships:** every foreign key, with cardinality and delete rule.

Use one naming convention for tables and columns (e.g. `snake_case`,
singular table names) and keep it. Tables and columns that hold domain terms
are named after their glossary Implementation name.

## Final Domain Model

The complete class diagram of the system is derived from the code, not kept
by hand. When it is needed (e.g. as an evidence of the Construction or
Transition phase), generate it from the source code with a reverse
engineering tool and store the result outside this folder or attach it to the
iteration's deliverables.

## Checks

- Every analysis package is realized by a subsystem, and every analysis
  mechanism by a design mechanism.
- Every dependency that crosses subsystems is covered by a required
  interface: analysis dependencies between packages in different subsystems,
  links between classes of a split package that ended up in different
  subsystems, and needs towards General subsystems. Dependencies between
  packages merged into one subsystem are internal and need no interface.
- Every operation called between subsystems in a design realization exists in
  a provided interface.
- There are no dependency cycles between subsystems, and no General
  subsystem depends on a Business one.
- Every subsystem appears in at least one application, and every application
  is allocated to a node.
- Every entity class that must be persisted has a table, or a documented
  reason why not.

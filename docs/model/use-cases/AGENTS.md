# Use cases: filling instructions

Read `../AGENTS.md` first: it holds the rules common to the whole model (writing
rules, derived fields, IDs, references, status and iterations).

This folder holds the use-case model of the USDP (see `../../usdp-guide.md`)
and, for each use case, the artifacts of its analysis and design realizations.

| Path | Content |
| --- | --- |
| `use-cases.md` | Catalog of all use cases with their status per workflow, relationships and packages |
| `actors/` | Actors of the use-case model, with their own `AGENTS.md` |
| `_template/` | Folder copied for each new use case |
| `UC-xxx-<name>/` | One folder per use case |

Each use-case folder contains:

| File | Workflow | Content |
| --- | --- | --- |
| `use-case.md` | Requirements | Description: actors, flows, pre- and postconditions |
| `ui/` | Requirements | Optional. Interface sketches |
| `analysis/realization.md` | Analysis | Participating classes, structural view, behavioral view, special requirements |
| `design/realization.md` | Design | Traceability from analysis, structural view (classes++), behavioral view (sequence), implementation requirements |
| `design/ui.md` | Design | Optional. User interface design, for systems with a user interface |

The model is written in text only. Each realization describes the use case in
two views, as tables with enough detail to generate UML diagrams later without
interpretation:

- **Structural view:** which classes take part and how they are linked. It
  becomes a class diagram.
- **Behavioral view:** the messages they exchange in each flow. In analysis it
  becomes a communication diagram; in design, a sequence diagram.

Analysis classes are defined only once, in `../analysis/`. Subsystems and
their interfaces are defined only once, in `../design/subsystems/`. A
use-case folder only shows which elements take part in the use case and how
they collaborate. Design classes are not kept in a global catalog: each design
realization shows the members it uses, and the code holds the complete
class.

## Specific Rules

- Use-case names are verb + object and their key nouns must be glossary
  terms.

## Adding a Use Case

1. Add a row to the catalog in `use-cases.md`.
2. Copy `_template/` to `UC-<ID>-<name>/`, with the name in lowercase
   kebab-case (e.g. `UC-001-register-sale/`).
3. Link the ID in the catalog to `UC-<ID>-<name>/use-case.md`.
4. Add the use case to the Use cases column of its actors in
   `actors/actors.md`. The actors of a use case are decided in its
   `use-case.md` (Primary actor, Supporting actors); `actors.md` and the
   Actors column of `use-cases.md` are derived from it.
5. Delete the `analysis/` and `design/` folders if the use case is not going
   to be analyzed or designed in the current iteration. Copy them from
   `_template/` when that work starts. Delete `design/ui.md` if the system
   has no user interface or the use case has no screens.

## Evolution Across Iterations

- Every time an artifact of a use case is worked on, update its Status and
  Iteration fields, and then the derived columns of the catalog in
  `use-cases.md`.
- In the catalog, `—` means the realization has not started.

What each phase expects:

| Phase | use-case.md | analysis/ | design/ |
| --- | --- | --- | --- |
| Inception | All Identified or Briefly described; 1 or 2 critical ones Detailed | Draft for the critical ones | — |
| Elaboration | ~80% Detailed, all significant ones | Reviewed for the significant ones | Reviewed for the significant ones |
| Construction | All Detailed | All Baselined | All Baselined |

Every use case goes through the design realization before it is
implemented.

## use-cases.md

### Use-Case Catalog

- The catalog is an index. The ID is assigned here; every other column is
  derived from the use-case folder:
  - **Actors**, **Priority**, **Arch. significant:** from the fields of
    `use-case.md`.
  - **Requirements**, **Analysis**, **Design:** from the Status field of
    `use-case.md`, `analysis/realization.md` and `design/realization.md`.
  - **Iteration:** the latest Iteration field among those files.

- **Arch. significant:** Yes if the use case forces structural decisions: it
  crosses several subsystems or applications, touches persistence, integrations,
  security or performance, or is technically uncertain. These are realized
  first, in Elaboration.
- **Requirements**, **Analysis** and **Design:** the status of each artifact,
  as described above.
- **Iteration:** the last iteration that changed the use case.

### Relationships

- Use them only to avoid duplication, not to break a use case into functions.
- `include`: the base use case always runs the target.
- `extend`: the target is optionally added to the base when a condition holds.
  Write the condition.
- `generalization`: the child is a special case of the parent.
- Delete the example row if there are no relationships.

### Packages

- Optional. Group use cases when the model has many, usually by business area
  or by actor.
- These packages only organize the use-case model. They are not the analysis
  packages of `../analysis/`, which group classes and identify the candidate
  subsystems. Both often follow the same business areas, and a use-case
  package is a good first guess for an analysis package.

## use-case.md

- **Name:** verb + object, from the actor's point of view ("Register sale",
  not "Sale management").
- **Primary actor** and **Supporting actors:** the `ACT-xxx` of
  `actors/actors.md` that take part, with their name. This is the source of
  the actor ↔ use case relationship; the catalogs are derived from it.
- **Feature:** the `FEAT-xxx` of `../requirements/vision.md` the use case
  realizes. This is the source; the Use cases column of `vision.md` is
  derived from it.
- **Priority** and **Architecturally significant:** decided here; the catalog
  copies them.
- **Functional requirements (derived):** every `FR-xxx` of
  `../requirements/requirements.md` whose Use case column is this use case.
- **Brief Description:** one paragraph on the goal of the use case and the
  value it delivers to the actor.
- **Preconditions:** what must be true before it starts. Not the trigger.
- **Basic Flow:** the success path, as numbered steps that alternate between
  the actor and the system. Each step starts with "The <actor>" or "The
  system". No user interface details ("presses the button") and no technology.
- **Alternative Flows:** other valid paths that still reach the goal. State
  the step where it starts and the condition, and the step where it resumes.
  Name them `A1`, `A2`… in order, each with its own steps numbered from 1.
- **Exceptions:** paths where the goal is not reached. Name them `E1`,
  `E2`… in order, each with its own steps numbered from 1. State the step, the
  condition and how the use case ends.
- **Postconditions:** what is guaranteed at the end, on success and on
  failure.
- **Special Requirements (derived):** the `NFR-xxx` of
  `../requirements/requirements.md` whose Scope includes this use case.

### Step References

Every other artifact refers to the steps of `use-case.md` with one format:

| Reference | Meaning |
| --- | --- |
| `3` | Step 3 of the basic flow |
| `A1` | The whole alternative flow A1 |
| `A1.2` | Step 2 of alternative flow A1 |
| `E1.1` | Step 1 of exception E1 |

Steps can be renumbered freely until a realization or a user interface design
refers to them. After that, renumbering means updating every reference in
those artifacts in the same change.

## analysis/realization.md

How the use case is realized with analysis classes, without technology. It
describes the use case in a structural view (the participating classes and
their links) and a behavioral view (the messages of each flow).

### Participating Classes

The elements of both views.

- One row per actor and per analysis class taking part in the use case.
- **Stereotype:** `actor` / `boundary` / `control` / `entity`. Actors have no
  package.
- Every class must exist in the `boundary.md`, `control.md` or `entity.md`
  of its package in `../analysis/<package>/`; add it there first (see
  `../analysis/AGENTS.md`). This table is the source of which classes take
  part in which use case: update the derived Use cases fields of the class
  and of its `package.md`.
  - Boundary: interaction between an actor and the system. At least one per
    actor–use case pair.
  - Control: coordinates the flow of the use case. Usually one per use case.
  - Entity: persistent domain information.
- **Responsibility in this use case:** what the class does here, not its full
  definition.

### Structural View

The links between the participating classes. It becomes the class diagram of
the realization.

- One row per link. A link means the two classes know each other in this use
  case; it has no direction, kind or multiplicity.
- **Meaning:** a short phrase in domain terms ("Requests registration",
  "Creates and updates").
- Expected pattern: actor – boundary, boundary – control, control – entity,
  entity – entity. Any other link (boundary – entity, actor – control,
  boundary – boundary) must be justified in Meaning.
- Entity – entity links are not defined here: cite them from the `entity.md`
  of their package with "(see entity.md)". If the use case reveals a new
  relationship or attribute of an entity, add it to `entity.md` first. This is
  how the entities are modeled, one use case at a time.

### Behavioral View

The numbered messages of each flow, travelling over the links of the
structural view. It becomes the communication diagram of the realization.

- One table per flow: the basic flow and each alternative flow or exception
  that changes the collaboration.
- One row per message, in order.
- **UC step:** the step of `use-case.md` the message realizes, in the format
  of "Step References" above (`3`, `A1.2`, `E1.1`).
- **From** and **To:** names from Participating Classes. Every From–To pair
  must have a link in the Structural View; if it does not, the link is missing.
- **Message:** what is asked, in plain words ("add item"). No operation
  signatures.

### Other Sections

- **Special Requirements:** the non-functional requirements this use case
  must meet, the concern they belong to (persistence, security,
  distribution…) and the class or analysis mechanism (`AM-xxx`) that handles
  them.
- **Feedback to Requirements:** gaps or inconsistencies the analysis found in
  `use-case.md`, and how they were fixed. Fix `use-case.md` too.

### Checks

- Every class in Participating Classes appears in at least one link and one
  message.
- Every message travels over an existing link.
- Every step of the basic flow of `use-case.md` is realized by at least one
  message.

## design/realization.md

How the use case is realized with design elements, adapted to the technology
of `../design/architecture.md`. Every use case has one.

### Traceability from Analysis

- Every class of the analysis realization, and the design elements it became.
  None may be left without a design element.

### Structural View (classes++)

The classes that will practically go to the code, with types.

- **Participating Elements:** every class, interface and enumeration taking
  part, with its subsystem, the application it lives in (from
  `../design/architecture.md`) and its role in this use case. Interfaces of other
  subsystems must exist in the Provided Interfaces of that subsystem in
  `../design/subsystems/`.
- **One block per class:** the attributes and operations **this use case
  uses**, with types, full signatures and visibility (`+` public, `-`
  private, `#` protected). Do not copy members the use case does not use: the
  complete class is in the code. If two realizations show the same member
  differently, align them; the code is the reference.
- Interfaces of other subsystems do not need a block here; their operations
  are in the subsystem file.
- **Relationships:** the design formalizes them. Kind (association,
  aggregation, composition, generalization, realization, dependency), exact
  multiplicity and navigability.

### Behavioral View (sequence)

- One table per flow: the basic flow and each alternative flow or exception
  that changes the interaction.
- One row per call, in order.
- **UC step:** the step of `use-case.md` the call realizes, in the format of
  "Step References" above (`3`, `A1.2`, `E1.1`).
- **From** and **To:** names from Participating Elements, or the actor.
- **Operation:** the operation called, with its arguments. It must exist in a
  member block of the Structural View or in a provided interface of a
  subsystem. Calls from the actor are written in plain words ("enter product
  code").
- **Returns:** what the call returns, or empty if nothing.
- A call to another subsystem must go through one of its provided interfaces,
  never to its internal classes.
- A call between applications (e.g. a frontend view calling a backend API) is
  a normal row; the operation is the API exposed by the target class, and the
  protocol is in the Connections of `../design/deployment.md`.

### Other Sections

- **Implementation Requirements:** what the programmer must respect that the
  views do not show: transactions, concurrency, validations, performance.

### Checks

- Every analysis class is traced to at least one design element.
- Every operation in the Behavioral View exists in the Structural View or in
  a subsystem interface.
- Every step of the basic flow of `use-case.md` is realized by at least one
  call.
- A programmer could implement the use case without making new structural
  decisions.

## design/ui.md

The formal design of the user interface of the use case. Only for systems
with a user interface; it refines the sketches of `ui/`, if there are any.

- **Screens:** every screen, dialog or view the use case uses, with the
  boundary class it realizes, the design element that implements it, and the
  steps of `use-case.md` it covers.
- **One block per screen:**
  - **Fields:** each input or output, with its control, whether it is
    required, its validation and the data it shows or edits.
  - **Actions:** each button, link or key the actor uses, the step it
    realizes and what happens.
  - **Messages:** errors, warnings and confirmations, linked to the
    alternative flows and exceptions of the use case.
- **Navigation:** how the actor moves between screens.
- Keep it consistent with the usability requirements (`NFR-US-xxx`).

# Analysis model: filling instructions

Read `../AGENTS.md` first: it holds the rules common to the whole model (writing
rules, derived fields, IDs, references, status and iterations).

This folder holds the analysis model of the USDP (see `../../usdp-guide.md`),
organized by analysis package. The analysis realization of each use case
lives in `../use-cases/UC-xxx-<name>/analysis/realization.md`.

| Path | Content |
| --- | --- |
| `architecture.md` | Index of analysis packages and common special requirements (analysis mechanisms) |
| `_template/` | Folder copied for each new package |
| `<package>/package.md` | Use cases the package serves, responsibility, dependencies |
| `<package>/boundary.md` | Boundary classes of the package |
| `<package>/control.md` | Control classes of the package |
| `<package>/entity.md` | Entity classes of the package |

The analysis model describes the system conceptually, without technology: no
types, frameworks, databases or user interface details. Its class and package
names are glossary terms, in the documentation language; the glossary's
Implementation name is only used from design on.

Analysis packages are the mechanism the analysis uses to identify the
subsystems of the system. Each package is a candidate subsystem, and the
analysis records which packages depend on which and why. The design confirms,
merges or splits the packages; it does not start from scratch.

The analysis identifies dependencies, not interfaces. Defining the interfaces
between subsystems (what each one offers, with which operations) is design
work, done from these dependencies in `../design/subsystems/`.

## Specific Rules

- Open questions go in the `package.md` of the package they affect, or in
  `architecture.md` if they affect the package structure or the mechanisms.
- Each class is defined once, in the package and stereotype file it belongs
  to. Other files refer to it by name, or as `Package::Class` from outside
  its package.
- The **Use cases (derived)** field of each class comes from the Participating
  Classes of the analysis realizations.
- When a class represents a glossary term, the glossary's Model element
  column is derived from it.

## Adding a Package

1. Add a row to the Analysis Packages table of `architecture.md`.
2. Copy `_template/` to a folder named after the package in lowercase
   kebab-case (e.g. `sales/`, `customer-accounts/`).
3. Link the package name in `architecture.md` to `<package>/package.md`.
4. Delete the stereotype files the package does not need yet, and copy them
   from `_template/` when its first class of that stereotype appears.

Until the packages are defined (usually in Inception), put candidate classes
in a provisional package `unassigned/` and move them when the architecture
analysis defines the packages.

## Adding a Class

1. Decide its stereotype and package. If no package fits, the package
   structure may need to change: record it in `architecture.md` "Open
   Questions".
2. Add a `##` section with the class name to the matching file
   (`boundary.md`, `control.md` or `entity.md`), keeping the sections in
   alphabetical order.
3. Add it to the Participating Classes of the analysis realizations where
   it takes part. Those tables are the source; then update the derived Use
   cases fields of the class and of `package.md`.

## Granularity

Analysis classes are coarse-grained. Each one becomes one or more design
classes later; splitting them for technical reasons is design work.

Before splitting a class, ask: would a domain expert recognize the split? If
yes, it belongs to analysis. If only a programmer cares, leave it for design.

Split in analysis only for conceptual reasons:

- **Different interaction:** split a boundary class by actor only when the
  actors exchange different information or interact in a different way.
  Several actors may share one boundary class.
- **Hidden domain concept:** a control class that keeps data or has
  attributes is missing an entity class.
- **Logic shared by several use cases:** it may justify a shared control
  class, or moving the logic to an entity class.
- **Different packages:** responsibilities from different business areas
  mean the class mixes concepts.

Do not split in analysis for technical reasons:

- The class has many responsibilities or "too many methods".
- Separating validation, mapping, persistence or presentation.
- Applying patterns (service, repository, presenter, DTO).
- Code reuse, or user interface components (window, panel, dialog).

When a class gathers many responsibilities, fix the cause instead of creating
classes:

- **Responsibilities written as operations.** Rewrite them at a higher level.
  "Validate product code", "Get product price", "Compute line subtotal" and
  "Update sale total" are one responsibility: "Register the items of a sale".
  Aim for 3 to 7 responsibilities per class; more usually means they are too
  detailed or the class mixes concepts.
- **Business logic in the control class.** Domain rules ("compute the total",
  "apply the discount") belong to entity classes. The control class only
  coordinates the flow of the use case.

Responsibilities are short statements in natural language. No operations,
signatures, parameters or types.

## architecture.md

### Analysis Packages

Each package is a candidate subsystem. Identify them in the architecture
analysis (usually at the start of Elaboration), from the use cases and the
domain, and refine them as more use cases are analyzed.

Where packages come from:

- **Use cases:** use cases that support the same business process, or that
  serve the same actor, usually belong to the same package.
- **Domain:** entity classes that are closely related and change together
  belong to the same package, with the control and boundary classes that use
  them.

What makes a good package:

- **High cohesion:** its classes serve one business area. If its
  Responsibility needs to list unrelated things, split it.
- **Low coupling:** few dependencies on other packages, and no cycles. A cycle
  between two packages usually means they are one package, or that a class is
  in the wrong one.
- **A clear owner:** one person or team could be responsible for it, as a
  subsystem would be in design.
- **Not technical:** packages follow the business, not layers. "Sales" and
  "Inventory" are packages; "Screens", "Controllers" or "Database" are not.
  Layers are a design decision.

Columns:

- **Actors served (derived):** the actors of the boundary classes of the
  package. A package that serves very different actors may need to be
  split.
- **Depends on (derived):** the packages in the Dependencies table of the
  package's `package.md`, which is the source.

### Common Special Requirements

Analysis mechanisms: concerns that several use cases share and that the design
must solve once, the same way, for all of them.

- **Concern:** Persistence / Security / Distribution / Concurrency / Audit /
  other.
- **Description:** what is needed, without technology (e.g. "sales survive a
  system restart", not "use PostgreSQL").
- **Requirements:** the `NFR-xxx` that cause it.
- **Use cases (derived):** the use-case realizations whose Special
  Requirements cite this `AM-xxx`. Those realizations are the source.
- The use-case realizations cite these IDs (`AM-xxx`) in their Special
  Requirements. The design turns each one into a design mechanism in
  `../design/architecture.md`.

## package.md

- **Status** and **Iteration:** of the package as a whole (its
  responsibility and dependencies). Each class block has its own.
- **Use Cases:** first section. The list of use cases is derived: every use
  case whose analysis realization uses classes of this package, linked to its
  `use-case.md`. What this package provides to each one is written here.
- **Responsibility:** one short paragraph on what the package is about. If it
  needs to list unrelated things, the package may have to be split.
- **Dependencies:** each package this one depends on, one row per package.
  - **Caused by:** the links between classes that create the dependency,
    written as `UsingClass → Package::UsedClass` (e.g.
    `SaleHandler → Inventory::Product`). They
    come from the Structural Views of the use-case realizations.
  - **Reason:** why it is needed, in domain terms, in one sentence.
  - Do not define operations, services or what the other package "offers".
    That is an interface, and interfaces are identified in design.
  - A boundary class of another package in Caused by is a warning sign: a
    package should not depend on another package's interaction with its
    actors.

## boundary.md

Classes that model the interaction between an actor and the system: screens,
forms, reports, interfaces to external systems or devices.

- Usually one per use case, shared by its actors when they interact in the
  same way (see Granularity), and one per external system.
- Name them after what they do for the actor, not after a technology
  (`SaleWebPage`) or the stereotype (`SaleBoundary`), following the USDP
  convention:
  - **Human actor:** the `UI` suffix (`SaleRegistrationUI`). The design turns
    it into one class per presentation (e.g. mail or web).
  - **External system or device:** the `Interface` suffix
    (`PaymentGatewayInterface`).
- Describe what information is exchanged, not how it is presented.
- **Actor:** the actor (`ACT-xxx`) that interacts through it.
- **Responsibilities:** each one followed by the use case that requires it.
- **Information exchanged:** what goes in from the actor and out to it, in
  domain terms.

## control.md

Classes that coordinate the flow and logic of a use case.

- Usually one per use case (`SaleHandler`). Shared logic between use cases
  may justify one control class for several.
- It coordinates; it does not calculate. Domain rules go to entity classes.
- **Responsibilities:** each one followed by the use case that requires it.

## entity.md

Classes that model persistent domain information.

- Name them with domain nouns from the glossary (`Sale`, `Product`).
- They hold the domain rules: calculations, validations and constraints of
  the business. They are the classes that change least in design.
- **Responsibilities:** what the class knows and does, each one followed by
  the use case that requires it. When several use cases need the same thing,
  merge the responsibilities and list all the use cases.
- **Attributes:** name and meaning, without types.
- **Related to:** the conceptual relationships with other entity classes,
  one line each: the related class, what the relationship means in domain
  terms, and an approximate multiplicity (one to one, one to many, many to
  many, optional). If the related class is in another package, write
  `Package::Class`.
  - Do not decide the kind of relationship (association, aggregation,
    composition), navigability, exact multiplicity or how it is implemented.
    That is design.
  - A generalization is written as "is a kind of <Class>".

## Checks

- Every class has a clear responsibility and belongs to one package and one
  stereotype file.
- No two classes with different names represent the same concept.
- Every class takes part in at least one use case.
- Every use case of a `package.md` lists classes of that package in its
  analysis realization, and the other way around.
- Every link between classes of different packages in a Structural View has
  a matching row in the Dependencies table of the package that uses the other.
- There are no dependency cycles between packages.

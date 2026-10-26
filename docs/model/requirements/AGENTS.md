# Requirements artifacts: filling instructions

Read `../AGENTS.md` first: it holds the rules common to the whole model (writing
rules, derived fields, IDs, references, status and iterations).

This folder holds the artifacts of the requirements capture workflow of the
USDP (see `../../usdp-guide.md`):

| File | Artifact | Standard |
| --- | --- | --- |
| `vision.md` | Vision and scope | ISO/IEC/IEEE 29148 (business and stakeholder context) |
| `requirements.md` | Functional and non-functional requirements | ISO/IEC/IEEE 29148, FURPS+ |
| `glossary.md` | Glossary | ISO/IEC/IEEE 24765 (vocabulary) |

Actors live in `../use-cases/actors/` and have their own `AGENTS.md`.

## Specific Rules

- Requirements and vision items have their own status in their tables; they
  do not use Draft / Reviewed / Baselined.

## vision.md

What the product is, for whom, why it is worth building and where its limits
are. The stakeholders must accept it before Inception closes.

- Keep it short and at the level of needs and features. Detailed behavior goes
  into use cases; detailed requirements and constraints go into
  `requirements.md`.

### Problem

Two or three short paragraphs of prose:

- What happens today and who it affects: current process, tools, pain.
- What would change with the product.
- Success criterion: how the stakeholders will know it worked (e.g. "sales
  are registered in under 1 minute").

### Stakeholders

- List everyone with an interest in the result, including those who will not
  use the system (sponsor, instructor).
- Those who interact with the system are also actors and go in
  `../use-cases/actors/actors.md`.

### Key Features

- List the high-level capabilities of the product (the feature list). Name
  each one with a noun phrase, e.g. "Sales registration", not as a
  requirement.
- A feature is not a use case. Each feature is realized by one or more use
  cases, and those group the functional requirements.
- **Use cases (derived):** from the Feature field of each `use-case.md`. Each
  use case declares the feature it realizes.
- Every feature needs at least one use case before Elaboration closes. A
  feature without use cases means requirements work is missing.

### Scope

- List what this cycle delivers (feature IDs) and what it explicitly does not,
  so it is not raised again.
- For each out-of-scope item, give the reason and whether it is deferred to a
  later release.

### Open Questions

- Unknowns that block agreeing on the vision or the scope.
- They must be answered before Inception closes.

### Approval

- Record each stakeholder accepting the vision and scope. This is the
  Inception exit criterion.
- Add a new row if the scope changes.
- Decision: Accepted / Accepted with changes / Rejected.

## requirements.md

### Sources

- Where the requirements come from. Every requirement cites one or more of
  these IDs in its Source column.
- Type: Document / Interview / Workshop / Existing system / Regulation.

### Functional Requirements

What the system must do, from the user's point of view.

- One behavior per requirement, written as "The system shall <verb>
  <object>".
- Every functional requirement MUST be associated with a use case. The Use
  case column is the source of that relationship; the Functional requirements
  field of `use-case.md` is derived from it. If it fits none, either a use case is missing (add it to `../use-cases/`) or the
  requirement is out of scope (move it to "Out of Scope").
- Status: Candidate (not yet confirmed) / Accepted (confirmed by the
  stakeholders) / Rejected.

#### Notes

- Optional. Use it for business rules, calculations or data constraints that
  a single table row cannot hold.
- Reference the requirement ID, e.g. "FR-003: the discount is applied before
  taxes".
- Leave "None." if there are none.

### Non-Functional Requirements

Qualities and constraints of the system as a whole, grouped by FURPS+
category. Omit the categories that do not apply.

- Each requirement states a measurable fit criterion: a number, a threshold or
  a check someone can run (e.g. "95% of searches answer in < 2 s with 100
  concurrent users"). Avoid untestable words such as "fast", "user-friendly"
  or "secure" on their own.
- If the criterion is not known yet in Inception, write `TBD` and add the
  question to "Open Questions". Elaboration must resolve it.
- Scope: `Global` if it applies to the whole system, or the use case IDs it
  applies to. Scope is the source; the Special Requirements of each
  `use-case.md` are derived from it.
- Mark with `(A)` the requirements that will constrain the candidate
  architecture: technology, distribution, persistence, security,
  performance.
- ID prefix by category:

| Category | Prefix | Covers |
| --- | --- | --- |
| Usability | `NFR-US` | Ease of learning and use, accessibility |
| Reliability and Availability | `NFR-RE` | Uptime, recovery, fault tolerance |
| Performance | `NFR-PE` | Response time, throughput, capacity |
| Supportability | `NFR-SU` | Maintainability, configurability, portability, internationalization |
| Security and Privacy | `NFR-SE` | Authentication, authorization, data protection |
| Interfaces | `NFR-IN` | External systems, hardware or protocols the system must interact with |
| Constraints | `CON` | Restrictions imposed on the solution: mandated technology, platform, standards, legal or regulatory rules, budget, deadline |

- For constraints, the fit criterion is usually a compliance check.

### Out of Scope

- List requirements that were proposed and explicitly left out, so they are
  not raised again.
- Keep this list consistent with the Scope section of `vision.md`.

### Open Questions

- Unknowns that block confirming a requirement or setting a fit criterion.
- Each one names the requirement it affects and who can answer it.
- Questions that affect the critical use cases must be answered before
  Inception closes.

## glossary.md

The shared vocabulary of the problem domain. Every document, model and piece of
code must use these terms with these meanings. The names of actors, use cases
and analysis classes must match the glossary.

Definition style (ISO/IEC/IEEE 24765):

- One term, one concept. If two words name the same concept, pick one as the
  preferred term and list the other under Synonyms, marked "(avoid)".
- The definition can replace the term in a sentence without changing its
  meaning. Do not start it with "X is..." or "X means...".
- Define domain concepts, not general software words ("database", "user
  interface"), unless the project gives them a special meaning.
- Keep the terms and the acronyms in alphabetical order.

Columns:

- **Model element (derived):** the analysis element that represents the
  term, if any (e.g. `Sales::Sale (entity)`, `ACT-001 Cashier`), taken from the actor
  and class files. Leave it empty for concepts that do not become part of the
  model.
- **Implementation name:** the name of the term in the code language
  (`../../../AGENTS.md`, Languages), as the design model and the code write it
  (e.g. `Sale` for the term "Venta"). Required for every term that reaches
  the design or the code when the documentation and code languages differ.
  Empty means the term itself is used. The glossary is its source: the
  design and the code follow it.

Open Questions:

- Terms whose meaning is unclear or disputed between stakeholders.
- Each one names who can settle it.

The glossary starts in Inception and grows in every iteration. Analysis often
reveals new terms or conflicting meanings.

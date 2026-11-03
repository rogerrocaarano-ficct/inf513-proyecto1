# Model: common rules

This folder holds the models of the USDP (see `../usdp-guide.md`). These rules
apply to every file under it. Each subfolder has its own `AGENTS.md` with the
rules specific to its artifacts; read this file first. How to work with git
(branches per use case, iteration closes) is in the repository's
`../../AGENTS.md`.

## Map of the Model

| Folder | Workflow | Content |
| --- | --- | --- |
| `requirements/` | Requirements | Vision, requirements, glossary |
| `use-cases/` | Requirements, analysis, design | Use-case catalog, actors and one folder per use case with its description and its analysis and design realizations |
| `analysis/` | Analysis | Analysis packages, their classes and the analysis mechanisms |
| `design/` | Design | Architecture, subsystems and interfaces, deployment, logical database model |
| `management/` | Management (not a workflow) | Risk list |

Order of work: vision → actors and use cases → requirements → analysis
(packages, classes, analysis realizations) → design (architecture,
subsystems and interfaces, deployment and database, design realizations). The
glossary grows alongside all of them, and the risk list is reviewed in every
iteration. The order is a guide, not a waterfall:
each iteration goes through the workflows it needs, and later workflows feed
corrections back to earlier ones.

## Writing Rules

- Write the content in the documentation language (`../../AGENTS.md`,
  Languages). Headings, column names and fixed values stay as in the
  templates.
- Keep the structure of the templates: headings, table columns and their
  order. Omit a section only where the folder's `AGENTS.md` says it is
  optional.
- `<!-- ... -->` inside a cell or list item is a placeholder: an example or
  the allowed values. Replace it with the content. Remove the example row or
  block once a real one exists.
- Do not invent information. If something is unknown, write `TBD` and add an
  entry to the "Open Questions" section of the file (the folder's
  `AGENTS.md` may name another file for it).
- Open questions are checklist items: `- [ ] <IDs it affects>: <question>
  (ask: <who can answer it>)`. Tick them (`- [x]`) when answered and record
  the answer where it belongs.
- Domain names (actors, use cases, classes, packages, tables) must match
  `requirements/glossary.md`. When a new domain term appears, add it there.
  The design model and the code use the term's Implementation name instead.
- Do not add diagram files unless the project decides to. Everything a
  diagram would show must be in the text.
- Priority always uses MoSCoW: Must / Should / Could / Won't.

## Derived Fields

- A field or column marked `(derived)` copies a relationship whose source is
  another document; the folder's `AGENTS.md` names the source.
- Update it whenever the source changes, and never change it on its own. If
  they disagree, the source wins.

## IDs and References

- IDs are a prefix and three digits (`FR-001`). They are sequential and never
  reused, even if the item is deleted.

| Prefix | Item | Defined in |
| --- | --- | --- |
| `SRC` | Requirement source | `requirements/requirements.md` |
| `STK` | Stakeholder | `requirements/vision.md` |
| `FEAT` | Feature | `requirements/vision.md` |
| `FR` | Functional requirement | `requirements/requirements.md` |
| `NFR-xx` | Non-functional requirement (`US`, `RE`, `PE`, `SU`, `SE`, `IN`) | `requirements/requirements.md` |
| `CON` | Constraint | `requirements/requirements.md` |
| `ACT` | Actor | `use-cases/actors/actors.md` |
| `UC` | Use case | `use-cases/use-cases.md` |
| `AM` | Analysis mechanism | `analysis/architecture.md` |
| `AD` | Architectural decision | `design/architecture.md` |
| `RSK` | Risk | `management/risks.md` |

- Steps of a use case: `3` (basic flow), `A1`, `A1.2` (alternative flow),
  `E1`, `E1.1` (exception). See `use-cases/AGENTS.md`.
- Analysis classes: inside their own package, by name (`Sale`). From any other
  package or document, as `Package::Class` (`Sales::Sale`). When the
  stereotype matters, add it in parentheses: `Sales::Sale (entity)`. In a
  table that already has a Package column, the name alone is enough.
- Actors: `ACT-001 Cashier`, or just `Cashier` inside a use case.

## Status and Iterations

Artifacts that are refined across iterations have a Status and an Iteration
field.

| Status | Meaning |
| --- | --- |
| Draft | In progress |
| Reviewed | Checked against the artifacts it comes from (the use case for an analysis realization, the analysis for a design artifact) |
| Baselined | Part of an iteration baseline; any change needs a reason |

- `use-case.md` uses its own scale: Identified (name only) / Briefly
  described / Detailed (all flows written).
- Requirements items have their own status in their tables (e.g. Candidate /
  Accepted / Rejected for functional requirements), and the vision is
  accepted in its Approval table.
- Iterations are named by phase and number: `I1` (Inception), `E1`, `E2`
  (Elaboration), `C1`… (Construction), `T1` (Transition). The Iteration field
  holds the last iteration that changed the artifact.
- Edit artifacts in place. Do not create copies per iteration: git keeps the
  history, and each iteration ends with a merge into `main` tagged with its
  name (see `../../AGENTS.md`).

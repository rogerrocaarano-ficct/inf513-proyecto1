---
name: design
description: Run the USDP design workflow on one use case. Writes its design realization (classes++ and sequence as tables) and its user interface design, and updates the subsystems, interfaces and logical database model in docs/model/design/. Use it with a use-case ID, or without one to get the next use case to design.
argument-hint: "[UC-xxx]"
disable-model-invocation: true
---

# USDP Design of a Use Case

Design gives the use case a physical form according to the architecture and
the technology. Your job is to write the design realization of one use case
and to keep the shared design artifacts (subsystems, interfaces, database)
consistent with it.

Arguments: `$ARGUMENTS`. The ID of the use case to design (`UC-001`).

## Before Starting

1. Read, in this order: `AGENTS.md`, `docs/model/AGENTS.md`,
   `docs/model/use-cases/AGENTS.md`, `docs/model/design/AGENTS.md`,
   `docs/model/analysis/AGENTS.md`, and the "Design" section of
   `docs/usdp-guide.md`. Follow their rules; this skill only says what to do
   and in which order.
2. If no use case was given, read the catalog in
   `docs/model/use-cases/use-cases.md` and propose the next one: first the
   architecturally significant use cases whose analysis is done and design is
   not, then by priority. Wait for the user to choose.
3. Check that the use case has an analysis realization. If it does not, stop
   and suggest `/analysis UC-xxx` first. If it is still Draft, tell the
   user and ask whether to continue.
4. Check `docs/model/design/architecture.md`. If it has no accepted
   decisions or no applications, the architecture is missing: stop and
   offer to work on it first with the user, in a `shared/<name>` branch.
   Never choose technologies, frameworks or architectural styles on your own:
   propose options with their trade-offs, let the user decide, and record the
   decision as an `AD-xxx`.
5. Git: create or switch to the use-case branch, named like its folder
   (`UC-001-register-sale`), from `develop`.

## Steps

### 1. Subsystems and Interfaces → `design/subsystems/`

- Find the subsystems that realize the packages of the use case's analysis
  classes. If a package has no subsystem yet, propose one (kept as is, merged
  or split) and confirm it with the user.
- For every call that will cross subsystems, make sure the interface exists,
  following "How to identify the interfaces" in the design `AGENTS.md`. Add
  only the operations this use case needs, with full signatures and errors.

### 2. Write the Realization → `design/realization.md`

Create it from `docs/model/use-cases/_template/design/` if the use case folder
does not have it yet.

1. **Traceability from Analysis:** every analysis class of the analysis
   realization and the design elements it becomes.
2. **Structural View:** the participating elements with their subsystem and
   application; one block per class with only the members this use case
   uses, typed and with visibility; the relationships with kind,
   multiplicity and navigability. Reuse the names and signatures already used
   by other design realizations for the same class. Name domain elements
   after the Implementation name of their glossary term; if a term has none
   and the documentation and code languages differ, propose one, confirm it
   with the user and add it to `docs/model/requirements/glossary.md`.
3. **Behavioral View:** one table for the basic flow, and one for each
   alternative flow or exception that changes the interaction, with the step
   references of `use-case.md`. Calls to other subsystems go through their
   provided interfaces.
4. **Implementation Requirements:** transactions, concurrency, validations
   and performance rules the code must respect.

### 3. User Interface → `design/ui.md`

- Only if the system has a user interface and the use case has screens;
  otherwise delete the file.
- Design the screens, fields, actions, messages and navigation from the
  boundary classes and the flows. Ask the user about preferences the model
  does not settle (layout conventions, controls), and keep it consistent
  with the usability requirements.

### 4. Shared Design Artifacts

- **Database:** add or update the tables, columns, keys, indexes and
  relationships for the entities this use case persists, in
  `docs/model/design/database.md`.
- **Deployment:** change `docs/model/design/deployment.md` only if the use
  case needs a new application, node or connection; confirm it with the
  user first.
- Update the derived fields: Provides and Requires in the subsystem catalog,
  Used by and Use cases of each interface.

### 5. Check and Close

1. Run the checks of `docs/model/use-cases/AGENTS.md` (design realization)
   and of `docs/model/design/AGENTS.md`, and fix what does not hold.
2. Set the Status (Draft, or Reviewed if the user reviewed it with you) and
   the Iteration of every artifact you touched, and update the derived
   columns of the use-case catalog.
3. Commit on the use-case branch with the use case ID as the scope
   (`docs(UC-001): …`), following the commit rules of `AGENTS.md`.
   Show the user the changes and the message, and commit only after they
   confirm.
   Do not merge into `develop`, or push and open its pull request, without
   the user's confirmation (see Pull Requests in `AGENTS.md`).
4. Report to the user: the design elements and interfaces created or
   changed, the database changes, the decisions that need their approval,
   and the open questions.

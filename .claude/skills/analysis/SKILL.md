---
name: analysis
description: Run the USDP analysis workflow on one use case. Writes its analysis realization (participating classes, structural view, behavioral view) and updates the analysis packages and classes in docs/model/analysis/. Use it with a use-case ID, or without one to get the next use case to analyze.
argument-hint: "[UC-xxx]"
disable-model-invocation: true
---

# USDP Analysis of a Use Case

Analysis refines a use case into a conceptual model of boundary, control and
entity classes, without technology. Your job is to write the analysis
realization of one use case and to keep the analysis model (packages and
classes) consistent with it.

Arguments: `$ARGUMENTS`. The ID of the use case to analyze (`UC-001`).

## Before Starting

1. Read, in this order: `AGENTS.md`, `docs/model/AGENTS.md`,
   `docs/model/use-cases/AGENTS.md`, `docs/model/analysis/AGENTS.md`, and the
   "Analysis" section of `docs/usdp-guide.md`. Follow their rules; this skill
   only says what to do and in which order.
2. If no use case was given, read the catalog in
   `docs/model/use-cases/use-cases.md` and propose the next one: first the
   architecturally significant use cases without analysis, then by priority.
   Wait for the user to choose.
3. Read the use case's `use-case.md`. If it is not Detailed (all flows
   written), stop: tell the user which flows are missing and suggest
   `/requirements UC-xxx` to detail it first.
4. Git: create or switch to the use-case branch, named like its folder
   (`UC-001-register-sale`), from `develop`.

## Steps

### 1. Packages

- Read `docs/model/analysis/architecture.md` and the existing packages.
- If there are no packages yet, propose them from the use cases and the
  domain (see "Analysis Packages" in the analysis `AGENTS.md`) and confirm
  them with the user. In Inception, use the provisional package `unassigned/`
  instead.

### 2. Identify the Classes

- Read the existing boundary, control and entity classes of every package
  first. Reuse a class when it represents the same concept; never create a
  second class for it.
- Boundary: one per actor–use case pair, and one per external system.
- Control: usually one for the use case.
- Entity: the domain concepts the flows handle, named with glossary terms.
- Apply the Granularity rules of the analysis `AGENTS.md`: split only for
  conceptual reasons, and keep responsibilities at a high level.
- When a domain decision is ambiguous (is this a new concept or the same
  one?), ask the user.

### 3. Write the Realization → `analysis/realization.md`

Create it from `docs/model/use-cases/_template/analysis/` if the use case
folder does not have it yet.

1. **Participating Classes:** every actor and class taking part.
2. **Structural View:** the links between them, following the expected
   actor – boundary – control – entity pattern.
3. **Behavioral View:** one table for the basic flow, and one for each
   alternative flow or exception that changes the collaboration, using the
   step references of `use-case.md` (`3`, `A1.2`, `E1.1`).
4. **Special Requirements:** the non-functional requirements of the use case
   and the class or analysis mechanism (`AM-xxx`) that handles each one. Add
   new analysis mechanisms to `architecture.md` when several use cases share
   the concern.
5. **Feedback to Requirements:** every gap or inconsistency found in
   `use-case.md`. Fix `use-case.md` too, and tell the user.

### 4. Update the Analysis Model

- Add or update each class in the `boundary.md`, `control.md` or `entity.md`
  of its package: responsibilities with the use case that requires them,
  attributes and conceptual relationships (entities).
- Add the use case and what the package provides to it in `package.md`, and
  the dependencies on other packages that the Structural View reveals.
- Update the derived fields: Use cases of each class and package, Depends on
  and Actors served in `architecture.md`, Use cases of the analysis
  mechanisms, Model element in the glossary. Add new domain terms to the
  glossary.

### 5. Check and Close

1. Run the checks of `docs/model/use-cases/AGENTS.md` (analysis realization)
   and of `docs/model/analysis/AGENTS.md`, and fix what does not hold.
2. Set the Status (Draft, or Reviewed if the user reviewed it with you) and
   the Iteration of every artifact you touched, and update the derived
   columns of the use-case catalog.
3. Commit on the use-case branch with the use case ID as the scope
   (`docs(UC-001): …`), following the commit rules of `AGENTS.md`.
   Show the user the changes and the message, and commit only after they
   confirm.
   Do not merge into `develop`, or push and open its pull request, without
   the user's confirmation (see Pull Requests in `AGENTS.md`).
4. Report to the user: the classes created or reused, the changes to
   packages and dependencies, the feedback to requirements, the open
   questions, and the next step (`/design UC-xxx`).

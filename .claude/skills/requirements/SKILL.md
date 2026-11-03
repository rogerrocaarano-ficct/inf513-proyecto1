---
name: requirements
description: Run the USDP requirements workflow on this repository. Interviews the user to gather the project information (problem, stakeholders, features, scope, actors, use cases, requirements, glossary) and writes it into docs/model/. The first run starts the project (Inception); later runs extend the model. With a use-case ID, it details that use case.
argument-hint: "[project description, paths to source documents, or UC-xxx]"
disable-model-invocation: true
---

# USDP Requirements

The requirements workflow finds what the system must do and agrees it with
the stakeholders. Your job is to gather that information from the user and
from any source documents, and write it into the requirements and use-case
models. You do not analyze, design or build the system here.

The workflow runs in every phase. Its first run starts the project, in
Inception; later runs add or change what the model already holds.

Arguments: `$ARGUMENTS` (optional). One of:

- A free-text description of the project, written or pasted by the user.
- Paths to documents that describe the project (problem statement, brief,
  interview notes).
- A use-case ID (`UC-001`): detail that use case (see "Detail a Use Case").
- Nothing: continue from the current state of the model.

If the arguments are not a use-case ID and are not paths to existing files,
treat them as a project description.

## Before Starting

1. Read, in this order: `AGENTS.md`, `docs/model/AGENTS.md`,
   `docs/model/requirements/AGENTS.md`, `docs/model/use-cases/AGENTS.md`,
   `docs/model/use-cases/actors/AGENTS.md`, and the "Requirements capture"
   section and the section of the current phase in `docs/usdp-guide.md`.
   Follow their rules; this skill only says what to do and in which order.
2. Languages: read the Languages section of `AGENTS.md`. If the
   documentation or the code language is still a placeholder, ask the user
   for both before anything else and write them there.
3. Current phase: propose it from the latest iteration tag on `main` (no tag
   means Inception, `I1`) and confirm it with the user.
4. State of the model. If `docs/model/requirements/vision.md` still holds
   only placeholders, the project starts now: go through every step from the
   beginning. Otherwise, summarize what is already filled in, and propose
   what is missing or must change.
5. Git: check that `develop` exists; if it does not, ask the user before
   creating it from `main`. Then create or switch, from `develop`, to:
   - `shared/requirements` for the general run;
   - the use-case branch, named like its folder (`UC-001-register-sale`),
     when a use-case ID was given.
6. Use the arguments as context to draft answers before asking the user:
   - **Documents:** read them and register each one as a source (`SRC-xxx`)
     in `docs/model/requirements/requirements.md`, pointing to its path.
   - **Free text:** use it only as context. Do not save the text in the
     repository. Register it as one source row (e.g. "Project description
     given by the user", type Document, with the date), so the requirements
     taken from it can cite it.

## How to Interview

- Ask in small rounds (3 to 5 questions), one topic per round, in the user's
  language. Write the documents in the documentation language.
- When the sources already answer something, propose a draft and ask the user
  to confirm or correct it instead of asking an open question.
- Never invent information. What the user cannot answer is written as `TBD`
  and goes to the Open Questions of the file, with who can answer it.
- After each round, write what you learned into the files before moving on,
  so the work is never lost if the session ends.

## Steps

On the first run, go through every step. On later runs, only through the
steps with something new or changed.

### 1. Problem → `vision.md`

- What happens today, who it affects, what would change, and how the
  stakeholders will know it worked (success criterion).

### 2. Stakeholders → `vision.md`

- Always ask the user who has an interest in the result, including those who
  will not use the system (sponsor, instructor).
- Do not derive stakeholders on your own. When the sources name someone,
  show it in the question, and register it only once the user confirms it.

### 3. Key Features and Scope → `vision.md`

- The high-level capabilities the product must offer (5 to 15 features), the
  need each one addresses and its priority.
- What is in scope for this cycle and what is explicitly out of it, with the
  reason.

### 4. Actors → `use-cases/actors/`

- Look for who or what interacts with the system: human roles, external
  systems, devices, timers.
- Each time you find a candidate actor, stop and ask the user how to add it
  to the model:
  - **New actor**, with its type and kind;
  - **The same as an existing actor**: record the name as a synonym in the
    glossary;
  - **Generalization**: child or parent of an existing actor;
  - **Not an actor**: discard it, or keep it as a glossary term if it belongs
    to the domain.
- Ask the same when two registered actors could be a generalization. Never
  apply one without the user's answer.
- Create the catalog row and the actor file for each new actor, and the
  Generalizes field and the Actor Generalization row for each generalization,
  with the characteristics the user can give.

### 5. Use Cases → `use-cases/`

- For each actor, what it needs to achieve with the system. Name each use case
  with verb + object.
- Register every use case in the catalog, create its folder from
  `_template/` and write its Brief Description, actors, feature and
  priority. Delete the `analysis/` and `design/` folders for now.
- Show the user the complete list of use cases, with the criterion for
  architecturally significant ones (`docs/model/use-cases/AGENTS.md`), and
  let the user decide which ones are. Do not preselect any.
- Do not detail any use case in this step: detailing is a separate run
  that the user starts (see "Detail a Use Case").

### 6. Requirements → `requirements.md`

- Functional requirements that came up, each associated with its use case.
- Non-functional requirements and constraints the user knows (technology,
  platform, regulations, deadlines, budget), with a fit criterion or `TBD`.
  Mark with `(A)` those that will constrain the architecture.

### 7. Glossary → `glossary.md`

- Every domain term that appeared, defined following the glossary rules.
  Check that actor and use-case names use these terms.
- Leave the Implementation name empty: design fills it.

## Detail a Use Case

Only when the user runs this skill with a use-case ID.

1. Check that the use case is in the catalog. If it is not, stop and offer a
   general run to register it first.
2. Detail it in its `use-case.md`: preconditions, basic flow, alternative
   flows (`A1`…), exceptions (`E1`…), postconditions. Set its status to
   Detailed.
3. Add the requirements and glossary terms that come up, following steps 6
   and 7. They stay in the use-case branch, so they are merged with it.

## Close the Round

1. Update every derived field (catalogs, feature use cases, actor use cases,
   functional requirements of each use case).
2. Check the model against the rules and checks of the `AGENTS.md` files you
   read, and fix what does not hold.
3. Commit the work on the branch of the run, following the commit rules of
   `AGENTS.md`.
   Show the user the changes and the message, and commit only after they
   confirm.
   Do not merge into `develop`, or push and open its pull request, without
   the user's confirmation (see Pull Requests in `AGENTS.md`).
4. Report to the user:
   - What was written, and the open questions that remain, grouped by who
     must answer them.
   - The exit criteria of the current phase (`docs/usdp-guide.md`) and the
     use-case detail it expects (`docs/model/use-cases/AGENTS.md`), and which
     ones hold. The vision needs the stakeholders' acceptance in its Approval
     table.
   - In Inception, the management artifacts in `docs/model/management/`:
     whether the risk list (`risks.md`) has a strategy for each risk, and
     whether the plan (`plan.md`) has estimates and a credible Elaboration
     plan. Do not create them unless the user asks.
   - Next steps:
     - Use cases to detail with `/requirements UC-xxx`: first the
       architecturally significant ones, then by priority. The user chooses.
     - `/analysis UC-xxx` for the use cases already Detailed.
     - If the user already knows the technology, a first draft of
       `docs/model/design/architecture.md` with the decisions as Proposed.

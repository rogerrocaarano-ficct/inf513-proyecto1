# Repository guide

This repository follows the Unified Software Development Process (USDP): the
work is organized in phases and iterations, driven by use cases, and every
model traces back to them. This file explains how to work in the repository as
a whole. Read it first, then the `AGENTS.md` of the folder you work in.

## Layout

| Path | Content |
| --- | --- |
| `docs/usdp-guide.md` | The process: phases, workflows, artifacts and exit criteria |
| `docs/model/` | The models of the project, as text: requirements, use cases, analysis and design. Its `AGENTS.md` holds the rules common to all of them |
| `.claude/skills/` | Claude Code commands for the process: `/requirements [UC-xxx]`, `/analysis UC-xxx`, `/design UC-xxx`. They only order the steps; the rules stay in the `AGENTS.md` files |

The source code is added when the implementation workflow starts. Its layout
follows the applications defined in `docs/model/design/architecture.md`.

## Languages

| Setting | Value |
| --- | --- |
| Documentation language | Spanish |
| Code language | English |

`/requirements` sets both values the first time it runs. While one is
still a placeholder, ask the user for it before writing anything it covers.

- **Documentation language:** the content of every document in
  `docs/model/`, the names of folders, files and branches taken from a model
  element (e.g. `UC-001-registrar-venta`), and the description and body of
  commit messages.
- **Code language:** identifiers and comments in the source code, and the
  names in the design model (design classes, interfaces, operations, tables,
  columns). Domain terms take their name from the Implementation name column
  of `docs/model/requirements/glossary.md`.
- **Always in English:** the templates (headings, column names, placeholder
  structure), the fixed values they define (Status, Priority, Type and other
  allowed values), the `AGENTS.md` files, `docs/usdp-guide.md` and the
  skills.
- **Identifiers keep their format** in any language: ID prefixes and digits
  (`UC-001`, `FR-001`), step names (`3`, `A1`, `E1.1`), lowercase kebab-case
  for folder, file and branch names, the names of template files
  (`use-case.md`, `realization.md`), fixed prefixes (`shared/`) and the
  commit types (`feat`, `docs`…).

## Working Rules

- Write documentation, branch names and commit descriptions in the
  documentation language, and code in the code language (see Languages).
- Work is driven by use cases. Before changing code or models, know which use
  case the change belongs to, or whether it is shared work (see Branches).
- Keep the models and the code consistent in the same branch: a change in
  behavior updates the use case and its realizations; a change in the models
  that affects code updates the code.
- Do not leave derived fields stale after a merge: update them from their
  source (see `docs/model/AGENTS.md`).

## Git Flow

### Branches

| Branch | Purpose | Created from | Merged into |
| --- | --- | --- | --- |
| `main` | Closed iterations only. Every commit on `main` is the end of an iteration | — | — |
| `develop` | Main working branch: the integration of the current iteration | `main` | `main`, when the iteration closes |
| `UC-xxx-<name>` | Work on one use case: its description, its realizations, its code | `develop` | `develop` |
| `shared/<name>` | Shared work that belongs to no single use case | `develop` | `develop` |

- **Use-case branches** are named exactly like the use-case folder in
  `docs/model/use-cases/`: the ID and the short name in lowercase kebab-case
  (e.g. `UC-001-register-sale`).
- **Shared work** covers everything that is not part of one use case: model
  artifacts (vision, requirements, glossary, analysis packages and
  mechanisms, architecture, subsystems, deployment, database),
  infrastructure, pipelines and tooling, and defects that belong to no use
  case. Name the branch after what it does (e.g. `shared/architecture`,
  `shared/glossary-review`, `shared/staging-environment`,
  `shared/test-pipeline`).
- Branch names never include an iteration or a phase. Iterations are marked
  only by their merge into `main` and its tag (see Closing an Iteration).
- A use-case branch may also change shared artifacts when the use case needs
  it (e.g. a new entity class, a new interface operation). Keep those changes
  in the use-case branch, so they are merged together.
- Never commit or push directly to `main` or `develop`. They only receive
  merges.

### Pull Requests

How a merge reaches `develop` or `main` depends on the remote:

- **GitHub remote** (`git remote -v` shows `github.com`): every merge goes
  through a pull request, opened with `gh pr create`. A use-case or shared
  branch goes to `develop`; an iteration close goes from `develop` to
  `main`. Merge it with "Create a merge commit", never squash or rebase, so
  the history keeps each unit of work. Protect `main` and `develop` on
  GitHub so they only accept pull requests.
- **No GitHub remote:** merge locally with `git merge --no-ff`, as the steps
  below show.

A pull request follows the commit rules for its title (e.g. `docs(UC-001):
add analysis realization`). Its description lists the artifacts it changes
and their Status, and the open questions that remain.

### Working on a Use Case

1. Update `develop` and create the branch from it:
   `git switch develop && git pull && git switch -c UC-001-register-sale`.
2. Work on the use case: description, analysis and design realizations,
   code and tests, as the iteration requires.
3. Before merging, bring the latest `develop` into the branch and resolve the
   conflicts. Conflicts in catalogs and derived fields are resolved from their
   source, not by picking one side.
4. Merge into `develop` with a merge commit, so the history shows the work
   of each use case as one unit: push the branch and open a pull request to
   `develop` (`gh pr create --base develop`), or, without a GitHub remote,
   `git switch develop && git merge --no-ff UC-001-register-sale`.
5. Delete the branch after the merge. If the use case is worked on again in a
   later iteration, create the branch again from `develop` with the same
   name.

A use case may be merged into `develop` several times in one iteration (e.g.
analysis first, design later). It must be merged before the iteration closes;
unfinished work stays out of the iteration, and its Status says so.

### Closing an Iteration

1. Check the exit criteria of the iteration in `docs/usdp-guide.md` and the
   Status fields of the artifacts the iteration planned to deliver.
2. Merge `develop` into `main` with a merge commit:
   - GitHub remote: open a pull request from `develop` to `main` titled
     `chore(release): close iteration E1`, with the exit criteria and how
     they hold in its description
     (`gh pr create --base main --head develop`). After it is
     merged, update the local `main`: `git switch main && git pull`.
   - No GitHub remote: `git switch main`, then
     `git merge --no-ff develop -m "chore(release): close iteration E1"`.
3. Tag the merge commit on `main` with the iteration name and push the tag:
   `git tag -a E1 -m "Iteration E1" && git push origin main E1`.
4. Iteration names are defined in `docs/model/AGENTS.md` (Status and
   Iterations).

`main` is never changed between iteration closes. A defect found in a closed
iteration is fixed in a branch from `develop` and reaches `main` with the next
close.

### Commits

Commits follow Conventional Commits
(<https://www.conventionalcommits.org/>):

```
<type>(<scope>): <description>

[body]

[footers]
```

- **Type:** one of the table below, always in English.
- **Scope:** the use case ID when the commit belongs to one
  (`docs(UC-001): add analysis realization`). For shared work, the artifact
  or area it changes, in lowercase kebab-case (`docs(glossary): …`,
  `ci(test-pipeline): …`). Omit it only when no area fits.
- **Description:** imperative mood, in the documentation language, starting
  in lowercase and without a final period. The whole first line is at most
  72 characters.
- **Body and footers:** optional, in the documentation language. A change
  that breaks a published interface or the database schema adds `!` after
  the scope and a `BREAKING CHANGE:` footer that explains it.
- One logical change per commit: do not mix the work of two use cases.
- Before every commit, show the user the files and changes it includes and
  the message, and commit only after the user confirms both.

| Type | Use it for |
| --- | --- |
| `feat` | Code that adds behavior to the system |
| `fix` | Code that corrects a defect |
| `docs` | Model artifacts in `docs/model/` and other documentation |
| `test` | Tests only |
| `refactor` | Code that changes structure without changing behavior |
| `perf` | Code that improves performance |
| `build` | Build system and dependencies |
| `ci` | Pipelines and their configuration |
| `chore` | Other maintenance, and iteration closes |

A commit that changes both the model and the code of a use case takes the
type of its code change (`feat(UC-001): register sale with its lines`).
Merge commits keep the message that git or GitHub writes, except the
iteration close: `chore(release): close iteration E1`.

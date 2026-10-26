# Actors: filling instructions

Read `../../AGENTS.md` first: it holds the rules common to the whole model (writing
rules, derived fields, IDs, references, status and iterations).

This folder holds the actors of the use-case model of the USDP (see
`../../../usdp-guide.md`):

| File | Content |
| --- | --- |
| `actors.md` | Catalog of all actors, generalizations and open questions |
| `_template.md` | Template for the detailed description of one actor |
| `ACT-xxx-<name>.md` | Detailed description of one actor, created from `_template.md` |

An actor is a role played by someone or something outside the system that
interacts with it: a human user, an external system, a device or a timer.

## Specific Rules

- Open questions about actors go in `actors.md`, not in the actor files.
- Name actors after the role, not the person or job title: "Cashier", not
  "Maria" or "Employee".
- Every actor MUST take part in at least one use case. An actor without use
  cases is either missing use cases or not an actor.
- Which actors take part in a use case is decided in the Primary actor and
  Supporting actors fields of each `../UC-xxx-<name>/use-case.md`. That is the
  source; the Use cases column of `actors.md` is derived from it.

## Adding an Actor

1. Add a row to the catalog in `actors.md`.
2. Copy `_template.md` to `<ID>-<actor-name>.md`, with the name in lowercase
   kebab-case (e.g. `ACT-001-cashier.md`).
3. Link the ID in the catalog to that file: `[ACT-001](ACT-001-cashier.md)`.
4. Keep the ID, name, type and kind the same in the catalog and in the actor
   file.

## actors.md

### Actor Catalog

One row per actor.

- **Type:**
  - Primary: starts use cases to reach a goal of its own.
  - Supporting: the system calls on it to complete a use case (often an
    external system).
  - Someone with an interest in the result who does not interact with the
    system is not an actor: list them as a stakeholder in
    `../../requirements/vision.md`.
- **Kind:** Human / External system / Device / Time.
- **Description:** one sentence on the role it plays.
- **Use cases:** every use case the actor takes part in, derived from the
  Primary actor and Supporting actors fields of each `use-case.md`. Update it
  whenever those fields change. The check "every actor has a use case" is done
  against the `use-case.md` files.

### Actor Generalization

- Optional. Use it only when one actor is a specialization of another: the
  child can do everything the parent does, and more.
- Keep it consistent with the "Generalizes" field of the child's file.
- Delete the example row if there are no generalizations.

### Open Questions

- Unknowns about who the actors are, their characteristics or their
  interfaces.
- Each one names the actor it affects and who can answer it.

## Actor Files (from _template.md)

### Header

- **Type** and **Kind:** same values as in the catalog.
- **Generalizes:** the parent actor, if any. Delete the line if there is
  none.

### Description

Who or what plays this role and why it uses the system, in one short
paragraph.

### Goals

- What the actor wants to achieve with the system.
- Each goal maps to one or more use cases, written in parentheses.

### Responsibilities

What the actor is expected to do or provide: data, approvals, confirmations.

### User Characteristics

- Human actors only; delete the section for any other kind.
- Based on ISO/IEC/IEEE 29148 (user characteristics).
- Expertise levels: Novice / Intermediate / Expert.

### Interface

- External systems and devices only; delete the section for any other kind.
- Direction: Inbound / Outbound / Both.
- Link the interface requirement (`NFR-IN-xxx`) in
  `../../requirements/requirements.md`.

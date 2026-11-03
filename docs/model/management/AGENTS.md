# Management artifacts: filling instructions

Read `../AGENTS.md` first: it holds the rules common to the whole model (writing
rules, derived fields, IDs, references, status and iterations).

This folder holds the planning and control artifacts of the USDP. The guide
groups them as "Management" (see `../../usdp-guide.md`, Artifacts required and
produced by phase): they are not a workflow, but every phase produces or
updates them.

| File | Artifact | Standard |
| --- | --- | --- |
| `risks.md` | Risk list with strategy | ISO/IEC/IEEE 16085 (risk management) |
| `plan.md` | Project plan and iteration plans | ISO/IEC/IEEE 16326 (project management plan) |

## risks.md

The risks that threaten the project, prioritized, each one with a strategy.
Inception creates it, and every iteration updates it: the risks drive the
planning of the iterations (`../../usdp-guide.md`, Elaboration).

- A risk is something that may happen and harm the project (scope, schedule,
  technology, infrastructure, quality). It is not a requirement: what the
  system must do or comply with goes into `../requirements/requirements.md`,
  and the risk cites it.
- Write each risk as cause and consequence, e.g. "the mail server rejects the
  lab IP address, so the presentation cannot run".
- Do not duplicate open questions: when a risk depends on one, cite the
  artifact or the IDs that hold it in Related.

### Header

- Status and Iteration, as in `../AGENTS.md`. Update Iteration every time the
  list is reviewed, even if no row changes.

### Risks

- **ID:** `RSK-001`… Sequential and never reused.
- **Risk:** cause and consequence, in one or two sentences.
- **Category:** Requirements / Technical / Infrastructure / Security /
  Usability / Management.
- **Probability** and **Impact:** High / Medium / Low.
- **Strategy:** Avoid (change the plan so the risk cannot happen) / Mitigate
  (lower its probability or impact) / Transfer (someone else takes it) /
  Accept (live with it, with a contingency if it happens).
- **Response:** the concrete actions of the strategy, and what was already
  done. Name the iteration in which each pending action is planned when it is
  known.
- **Related:** the IDs (`CON`, `NFR`, `FR`, `AD`, `UC`) and artifacts the risk
  comes from or affects.
- **Status:** Open / Mitigated (the response is done and the remaining
  exposure is accepted) / Occurred (it happened; record what was done in
  Response) / Closed (it can no longer happen).
- Order the rows by exposure (Probability × Impact), highest first; on a tie,
  the one that blocks the current phase first. The order changes as the
  risks change; the IDs do not.
- Keep Mitigated, Occurred and Closed rows: they are the evidence that the
  risks were handled (`../../usdp-guide.md`, Traceability and final review).
  Move them below the Open rows.

### Open Questions

- Unknowns about the risks themselves: their probability, their impact or who
  owns a response. Questions about requirements or design stay in their own
  files.

## plan.md

The plan of the project: its milestones, its iterations and, for the current
and the next iteration, the work each one takes on. Inception creates it, and
every iteration updates it. The guide asks for a credible Elaboration plan to
close Inception and a realistic Construction plan to close Elaboration
(`../../usdp-guide.md`).

- Plan in detail the current iteration and the ones up to the next delivery
  milestone. Later iterations keep one row in Iterations until they come
  close.
- Plan by risk and value, not by the order of the requirements: each work item
  that answers a risk cites it.
- Keep closed iterations: their Work Items and Exit Criteria are the evidence
  of what each iteration delivered. Do not rewrite them after the close.

### Header

- Status and Iteration, as in `../AGENTS.md`.

### Team

- **Member:** a name, or a placeholder such as "Integrante A" until the team
  fills it.
- **Role:** what the member leads or does in the project.

### Milestones

- External dates the plan depends on: deliveries, presentations, stakeholder
  approvals. Cite the `CON` or `SRC` they come from.
- **Source:** the `CON` or `SRC` IDs the milestone comes from.
- **Date:** `YYYY-MM-DD`; when it is not fixed, the candidates (e.g.
  `2026-11-17 o 2026-11-19`) or `TBD`, with an open question.
- **Status:** Planned / Done / Moved (the date changed; record the new one).

### Iterations

- One row per iteration, in order, named as in `../AGENTS.md` (Status and
  Iterations).
- **Phase:** Inception / Elaboration / Construction / Transition.
- **Start** and **End:** `YYYY-MM-DD`; End is the planned close.
- **Goal:** what the iteration must leave, in one sentence.
- **Status:** Planned / In progress / Closed. An iteration is Closed when it is
  merged into `main` and tagged (`../../../AGENTS.md`, Closing an Iteration).

### Iteration sections

One section per detailed iteration, titled `## Iteration <name>`, with:

- **Work Items:** one row per unit of work.
  - **Item:** the use case ID when the work belongs to one, or a short name
    for shared work.
  - **Work:** what is done, in one sentence.
  - **Workflows:** the USDP workflows it covers: Requirements, Analysis,
    Design, Implementation, Testing; `All` when it covers the five.
  - **Owner:** a member from Team, or `Equipo`.
  - **Estimate:** effort in days.
  - **Due:** `YYYY-MM-DD`.
  - **Risks:** the `RSK` IDs it answers, or `—`.
  - **Status:** Pending / In progress / Done / Moved (it passed to another
    iteration; name it in Work).
- **Exit Criteria:** a checklist with the exit criteria of the phase
  (`../../usdp-guide.md`), stated for this iteration. Tick each one when it
  holds, and say how in one line, or justify why it is left open.

### Open Questions

- Unknowns that block planning: dates, availability, scope of a delivery.

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

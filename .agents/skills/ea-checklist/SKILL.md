---
name: ea-checklist
description: Present the changes the user still has to make by hand in an Enterprise Architect (EA) model as a checklist of tables, one section per element, read from the current state of the model. Use it, without being asked, whenever a reply hands the user EA changes to make (what the EA MCP server could not apply, a design proposal before applying it, or what is left at the end of a design step), or when the user asks what is missing in EA or says "ea-checklist". Takes an optional scope (an EA element or package). Read-only for EA.
compatibility: Requires the Enterprise Architect MCP server and EA open with the model loaded.
---

# EA checklist: changes to make in the model

Give the user the changes they still have to make in the Enterprise
Architect (EA) model as a checklist they can work through in EA, element by
element. The list comes from the model as it is now, not from memory.

This skill only reads EA and presents. Do not create, change or delete
anything in EA while running it. Applying an item is a separate request.

## 1. Know what is expected

Collect the target design for the scope: what was agreed in the
conversation, the design of an OpenSpec change, or the project's reference
architecture (`AGENTS.md`). Each item of the checklist is a difference
between that target and the model.

The scope is the argument (an element or a package), or else every element
touched in the current design step.

## 2. Read the model

Read every element in scope through the EA MCP server (attributes,
operations with their parameters and return types, connectors, owning
package) right before building the list. The user may have edited the model
since the last read: never list an item that is already done, and never
rely on an earlier read.

If the EA tools do not respond, say so and do not build the list from
memory.

## 3. Build the checklist

Write in the project's documentation language (Spanish in this repository).
Element, attribute, operation and type names stay exactly as in the model.

One section per element, headed by its name and package path
(`StoredFile` en `Design/Storage/domain`). Inside it, one table per kind of
change, only for the kinds that have items:

| Kind | Columns |
| --- | --- |
| Attributes | `Atributo \| Tipo \| Multiplicidad \| Qué hacer` |
| Operations to create or remove | `Operación \| Retorno \| Qué hacer` |
| Parameters | `Operación \| Parámetro \| Tipo` |
| Return types | `Operación \| Retorno \| Clasificador` |

- When a type is an element of the model, add "(clasificador: `X`)" so the
  user links it to the element and not only types the name.
- Say which operations need no change only when that avoids a doubt (for
  example, "`builder` y los getters no llevan parámetros").
- One row per parameter, in order.

Close with a table **Otros pendientes**, `Elemento | Qué falta`, for
everything that is not an attribute or an operation: relations (with their
type and direction), moves between packages, diagrams to review, tagged
values.

## 4. Point out inconsistencies apart

If reading the model shows something contradictory that is not part of the
target (a parameter `Long` where the attribute is `long`, a description that
no longer matches the operation, a duplicated attribute), list it after the
checklist under its own heading, with the fix you suggest. Do not fix it
without asking: it may be the user's own edit.

## 5. Keep it short

No introduction beyond one sentence on why the items are manual (for
example, the EA MCP server does not accept operation parameters). No
repetition of the design rationale: the checklist is for working in EA.

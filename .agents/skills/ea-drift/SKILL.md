---
name: ea-drift
description: Compare an Enterprise Architect (EA) design model with the current source code and list every deviation, so the user can update the model in EA. Use when the user asks to check, validate or audit the EA model against the code, to find what the model is missing or what changed in the code, or says "ea-drift". Takes an optional scope (a use case, an EA package or a code package/namespace). Read-only for both EA and the code.
compatibility: Requires the Enterprise Architect MCP server and EA open with the model loaded.
---

# EA drift: the model against the code

Compare the design model open in Enterprise Architect (EA) with the source
code of the current repository, and give the user the list of deviations so
they can fix the **model**. The code is the reference: every deviation is
described as a change the model needs, never as a code fix.

This skill only reads. Do not create, change or delete anything in EA, in the
code or in the documents. If the user then asks to fix a deviation in EA, that
is a separate request.

## 1. Learn the project

Before reading anything else, find out how this project is organized:

- **Conventions.** Read the repository instructions if they exist
  (`AGENTS.md`, `CLAUDE.md`, a modeling or contributing guide). Take from them
  the documentation and code languages, naming rules, how the EA model is
  structured, and what is deliberately not modeled. They override the
  defaults below.
- **Code.** Detect the language and the source roots from the build files
  (`build.gradle*`, `pom.xml`, `*.csproj`, `package.json`, `pyproject.toml`…)
  and the folder layout. Leave out tests, generated code and build output.
- **Base package or namespace.** The common root of the production code, so
  code packages can be mapped to EA packages relative to it.

If the conventions say nothing about something this skill needs (for example,
which EA package holds the design), find it in the model; ask the user only
when it cannot be found.

## 2. Scope

The argument sets what is compared:

| Argument | Scope |
| --- | --- |
| none | The whole design model and all the production code |
| use case (ID or name) | The EA elements that realize the use case (its diagrams and the elements on them) and the code they map to |
| EA package path | That package and its subpackages, and the code package it maps to |
| code package or namespace | That package, and the EA package it maps to |

If the argument matches nothing, say so and stop.

## 3. Check that EA is reachable

The model is read through the Enterprise Architect MCP server. Its tools are
named after the operation (`get_root_packages`, `find_packages_by_name`,
`get_elements_information`…); each agent prefixes them its own way (for
example `mcp__Enterprise_Architect__get_root_packages`). If the agent loads
MCP tools on demand, load the EA tools first.

Call `get_root_packages`. If the EA tools are not available, or the call
fails or returns no model, tell the user that EA must
be open with the project loaded, and stop. Never compare against memory or
guesses about the model.

## 4. Read the model

Find the design packages (`find_packages_by_name`,
`get_packages_information`) and read, for every element in scope:

- kind and stereotype (class, abstract class, interface, enumeration,
  data type…), with `get_elements_information`;
- attributes: name, type, multiplicity, visibility, static;
- operations: name, return type, parameters, visibility, abstract, static;
- enumeration literals;
- connectors, with `get_connectors_information`: generalization,
  realization, association, aggregation, composition (role names,
  multiplicity, navigability) and dependency;
- the package that contains it.

For a use-case scope, start from the use case's diagrams
(`get_diagrams_information`, `find_element_in_diagrams`) and take the elements
on them. Read package by package; do not request the whole model at once.

## 5. Read the code

For every type in scope, collect:

- package or namespace, kind (class, abstract class, interface, enum,
  record/struct/data class…), file and line;
- supertypes and implemented interfaces;
- fields or properties (name, type, static, read-only), including record or
  data-class components;
- public and protected methods (name, parameters, return type, abstract,
  static);
- enum constants;
- relations: a field typed with another type of the project is an
  association; a collection of them gives multiplicity `*`; an optional
  one, `0..1`.

Unless the project's conventions say otherwise, leave out what design models
usually do not hold:

- entry points, framework configuration and dependency-injection wiring;
- infrastructure: adapters, controllers, persistence and messaging
  implementations, schedulers, framework glue;
- constructors, accessors, `equals`/`hashCode`/`toString` and their
  equivalents, private helpers, annotations and imports.

If a type is ambiguous (it could be infrastructure or design), compare it
anyway and put its deviations under "To confirm".

## 6. Match and compare

Match types by name, then check their package. If the conventions map code
packages to EA packages in a specific way, use that mapping.

Do not report as deviations:

- language-specific type spellings that mean the same thing (`String`,
  `int`/`Integer`, `bool`/`boolean`, date and UUID types…);
- generic or collection types when the model expresses them its own way
  (for example `List<Foo>` in the code against `Foo` with multiplicity `*`,
  or with the collection described in the note), if the conventions say so
  or the model does it consistently. Report it only if neither the
  multiplicity nor the note says it is a collection or optional;
- anything the conventions say is deliberately not modeled.

Report every other difference, in these categories:

| Category | Example |
| --- | --- |
| Missing element in the model | The code has `Order`; EA does not |
| Extra element in the model | EA has `OrderHelper`; the code does not |
| Different name | EA `Ordr`, code `Order` |
| Different element kind | EA class, code enum or record |
| Different package | EA `sales::domain`, code `sales.application` |
| Attribute missing / extra / different | name, type, multiplicity, static |
| Operation missing / extra / different | name, return type, abstract, static |
| Parameters missing / different | names, types or order |
| Enumeration literal missing / extra | |
| Relation missing / extra / different | generalization, realization, association, multiplicity |

When two elements differ only by name, report one "Different name", not a
missing and an extra element. If parameters are missing everywhere in the
model (some tools cannot create them), group them in one line per element
instead of one row per parameter.

## 7. Report

Write the report in the project's documentation language if the
conventions set one, otherwise in the user's language; translate the
headings and categories below. Element names stay as they are in the code
and the model.

Start with one line with the scope and the counts (elements compared,
deviations found). If there are no deviations, say so and stop.

Then one table per EA package, ordered by element:

| ID | Category | Element | In the code | In the model | Action in EA |
| --- | --- | --- | --- | --- | --- |
| D-01 | Operation missing | `Order` | `cancel(): void` ([Order.java:42](path/to/Order.java:42)) | — | Add the operation `cancel(): void` to `Design/sales/domain/Order` |

- **ID:** `D-01`, `D-02`… in report order, so the user can refer to them.
- **In the code:** the code fact, with a link to the file and line.
- **In the model:** the EA fact with its package path, or `—`.
- **Action in EA:** the change the model needs, in one imperative sentence.

After the tables:

- **To confirm:** deviations on types that might be infrastructure, or that
  look like a code defect rather than a model gap, each with the question
  for the user.
- **Not compared:** what was left out (scope, ignored types) and anything the
  EA tools could not read.

Do not suggest code changes.

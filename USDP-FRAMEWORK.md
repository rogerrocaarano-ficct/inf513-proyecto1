# Guide to the Unified Software Development Process: phases, workflows and artifacts

## Overview

The Unified Software Development Process (USDP) is a generic software development framework. It organizes work into 4 sequential phases, each made of iterations that go through 5 workflows.

The three key ideas:

- **Use-case driven:** use cases guide the whole process. Every workflow starts from them, and all models trace back to them.
- **Architecture-centered:** the architecture is defined early, using the significant use cases, and validated with executable software.
- **Iterative and incremental:** the project is divided into iterations. Each one produces an evaluable increment, and the models are refined rather than redone.

Structure:

- **Cycle:** goes through the four phases and ends with a product release. If the product keeps evolving, a new cycle starts.
- **Phase:** ends with an evaluation against exit criteria. Phases are sequential.
- **Iteration:** a mini-project within a phase, with its own plan, that goes through the five workflows.
- **Increment:** an executable version that adds functionality to the previous one.

| Phase | Question it answers | Main result |
| --- | --- | --- |
| Inception | Is it worth doing? | Scope, risks and candidate architecture |
| Elaboration | Can it be built with this architecture? | Baseline of the executable architecture |
| Construction | Is the product built? | Beta version |
| Transition | Is it in the user's hands? | Product delivered and accepted |

## The five workflows

Each workflow produces a model and builds on the previous one. All of them run in every iteration, with different weight depending on the phase.

| Workflow | Goal | Model it produces | Main artifacts |
| --- | --- | --- | --- |
| Requirements capture | Determine what the system must do from the user's point of view | Use-case model | Actors, use cases and their descriptions, interface sketches, non-functional requirements, glossary |
| Analysis | Refine and structure the requirements into a conceptual model, without considering technology yet | Analysis model | Analysis classes (entity, control, boundary), analysis use-case realizations, analysis packages |
| Design | Give the system a physical form according to the technology and the non-functional requirements | Design and deployment models | Design classes, subsystems, interfaces, design use-case realizations, deployment nodes |
| Implementation | Build the system in terms of components | Implementation model | Source code, executables, scripts, build integration plan, unit tests |
| Testing | Verify that the system works as specified | Test model | Test cases, test procedures, test evaluation (results and defects) |

Chaining within an iteration: requirements open the iteration. Analysis refines them and often reveals missing requirements, which go back to the previous workflow. Design takes the analysis, implementation builds the design, and testing verifies the result against the use cases. The workflows overlap and feed back into each other: they are not a strict waterfall.

The steps below describe what happens in any iteration; the weight of each step depends on the phase (see the weight matrix further down).

### 1. Requirements capture: step by step

Who takes part: systems analyst (use-case model and glossary), use-case specifier (detailed descriptions), user interface designer (sketches) and architect (chooses the significant use cases).

Steps:

1. List the candidate requirements from the problem statement, interviews and available documents.
2. Find the actors: who uses the system or interacts with it, including external systems.
3. Find the use cases: what each actor needs to achieve. Name them with a verb and an object (for example, "Register sale").
4. Write a short description of each use case.
5. Prioritize the use cases: the architect chooses the significant ones based on risk, value and technical uncertainty.
6. Detail the prioritized use cases: preconditions, basic flow, alternative flows and exceptions, postconditions and special requirements.
7. Prototype the user interface: screen sketches associated with each use case.
8. Structure the use-case model: include, extend and generalization relationships, and grouping into packages to avoid duplication.
9. Complete the glossary and the non-functional requirements, and review the result with the client or instructor.

Inputs: problem statement, vision and user needs.

Outputs: use-case model (diagrams and descriptions), interface sketches, non-functional requirements and glossary.

How to know it is finished in this iteration:

- Every actor takes part in at least one use case.
- Every use case delivers a valuable result to an actor.
- The prioritized use cases have a basic flow and alternative flows.
- Every functional requirement is associated with a use case.

### 2. Analysis: step by step

Who takes part: architect (architecture analysis), use-case engineer (use-case realizations) and component engineer (classes and packages).

Steps:

1. Architecture analysis: define the initial analysis packages, the obvious analysis classes and the common special requirements (persistence, distribution, security).
2. Analyze each selected use case: identify which analysis classes take part in its realization.
3. Classify the classes with stereotypes:
   - Boundary: interaction between an actor and the system (screens, external interfaces).
   - Control: coordinates the flow and the logic of a use case.
   - Entity: persistent domain information.
4. Describe the use-case realization: a collaboration or sequence diagram with the event flow, plus a class diagram with the participating classes.
5. Analyze each class: define its responsibilities, attributes and relationships, unifying what the different use cases that use it require.
6. Analyze each package: check that it is cohesive, that its dependencies are minimal and that its content is coherent.
7. Feed back into the requirements: if the analysis reveals incomplete or inconsistent use cases, go back to requirements capture and correct them.

Inputs: use-case model and description of the current architecture.

Outputs: analysis model (analysis classes, use-case realizations, packages) and the first version of the logical view of the architecture.

How to know it is finished in this iteration:

- Every selected use case has its analysis realization.
- Every class has a clear responsibility and a stereotype.
- There are no duplicate classes with different names for the same concept.
- Names match the glossary.

### 3. Design: step by step

Who takes part: architect (architecture and deployment design), use-case engineer (design realizations) and component engineer (classes, subsystems and interfaces).

Steps:

1. Architecture design: identify the nodes and the network configuration (deployment model), the subsystems and their interfaces, and the significant design classes. Decide on technologies, architectural styles and generic mechanisms (persistence, security, communication).
2. Design each use case: turn its analysis realization into a design realization, with sequence diagrams showing the design objects and the operations invoked.
3. Design each class: define attributes with types, operations with signatures, relationships, methods and states if the behavior depends on state.
4. Design each subsystem: make sure it is as independent as possible, offers the right interfaces and meets what the use cases require of it.
5. Distribute the behavior across the nodes: assign subsystems and components to the deployment model and check that the non-functional requirements (performance, security, availability) can be met.
6. Update the architecture description: logical and deployment views, with the decisions and their justification.

Inputs: analysis model, non-functional requirements and technology decisions.

Outputs: design model (classes, subsystems, interfaces, use-case realizations), deployment model and the design part of the architecture description.

How to know it is finished in this iteration:

- Every analysis class is traced to one or more design classes.
- Every use case in the iteration has its design sequence diagram.
- The interfaces between subsystems are defined.
- The design respects the documented non-functional requirements.
- A programmer could implement the design without making new structural decisions.

### 4. Implementation: step by step

Who takes part: architect (architecture implementation), component engineer (classes and subsystems) and system integration engineer (plans and builds).

Steps:

1. Implement the architecture: identify the significant components (executables, libraries, tables) and assign them to the nodes of the deployment model.
2. Plan the system integration: decide the order in which increments are built and what each build contains.
3. Implement each subsystem: organize the code structure (modules, layers, projects) so that it reflects the subsystems of the design.
4. Implement each class: write the source code from the design class, respecting its interfaces and operations.
5. Run unit tests: test each component in isolation before integrating it.
6. Integrate the system: compile the iteration's build by combining the components in the planned order.
7. Version and record: store the code in a version-controlled repository and associate each build with the use cases it implements.

Inputs: design model and deployment model.

Outputs: implementation model (components and source code), build integration plan and unit tests.

How to know it is finished in this iteration:

- Every design class in the iteration is implemented and traced to its component.
- The build compiles and runs.
- The unit tests pass.
- The build can demonstrate the use cases of the iteration.
- The code is in the version-controlled repository.

### 5. Testing: step by step

Who takes part: test engineer (plans and designs), component engineer (unit and component tests and test components) and system integration engineer (integration and system tests).

Steps:

1. Plan the tests: define what is tested in the iteration, which test types (integration and system) and which resources are needed.
2. Design the test cases: derive them from the use cases (basic flow, alternative flows and exceptions) and from the non-functional requirements. Each case defines its inputs, conditions and expected result.
3. Design the test procedures: concrete steps to run one or several test cases.
4. Implement the tests: automate the cases when possible (test components) or keep the manual procedures documented.
5. Run the integration tests: verify that the components of the build work together.
6. Run the system tests: verify that the complete use cases work end to end.
7. Evaluate the results: record the defects, measure the coverage of the tested use cases and decide whether the build meets the iteration's goals.

Inputs: iteration build, use-case model and non-functional requirements.

Outputs: test model (test cases, procedures and test components) and test evaluation with the defects found.

How to know it is finished in this iteration:

- Every use case in the iteration has at least one test case.
- The test cases cover the basic flow and the main alternative flows.
- The results are recorded with their defects.
- Critical defects are fixed or planned for the next iteration.

## Phase 1: Inception

Inception decides whether the project is viable and sets its scope. It does not build the system; it understands it well enough to decide whether to continue.

Objectives:

- Agree on the vision and scope with the stakeholders (client or instructor).
- Identify the actors and the main use cases (\~10-20%) and describe 1 or 2 critical ones in detail.
- Propose a candidate architecture with justified technologies.
- Identify and prioritize the main risks, with a strategy for each one.
- Estimate cost and time and draw up a project plan.

Iterations: normally just one. Its result is not a functional increment of the product, but the scope, risk and candidate architecture artifacts.

| Workflow | Weight | What is done in Inception |
| --- | --- | --- |
| Requirements | High | Set the scope, identify the main actors and use cases, detail 1 or 2 critical ones, write the vision and initial glossary |
| Analysis | Low | Candidate analysis classes and a basic collaboration of the critical cases, to check that they are feasible |
| Design | Low | Architecture sketch: main subsystems, technologies and preliminary deployment view |
| Implementation | Minimal | Optional: exploratory prototype or disposable proof of concept to reduce a specific risk |
| Testing | Minimal | Define acceptance criteria for the critical cases |

Exit criteria:

- The stakeholders accept the scope.
- The critical use cases are identified and described.
- A feasible candidate architecture exists.
- The main risks are prioritized, with a strategy.
- The estimates are reasonable and the Elaboration plan is credible.

The evaluation can end in: continuing to Elaboration, rescoping, or repeating an Inception iteration; or cancelling the project if it is not viable.

## Phase 2: Elaboration

Elaboration shows that the architecture holds up. It ends with an executable baseline that implements the significant use cases and with the serious risks reduced. It is the most critical phase, because fixing a bad architecture during Construction is far more expensive.

Objectives:

- Define and validate a stable architecture.
- Capture about 80% of the use cases and the non-functional requirements.
- Reduce the most serious technical and requirements risks.
- Produce the baseline of the executable architecture.
- Prepare a credible plan for Construction.

Significant use case for the architecture: one that forces structural decisions (it crosses several layers or subsystems, touches persistence, integrations, security or performance, or is technically uncertain). It is implemented first.

Iterations: 1 to 3 (2 in a university project). They are planned by risk, not by the order of the requirements. Each one implements and tests use cases and leaves an executable build.

| Workflow | Weight | What is done in Elaboration |
| --- | --- | --- |
| Requirements | High | Complete the use-case model (\~80%), detail the significant ones with basic and alternative flows, document the non-functional requirements |
| Analysis | High | Analysis classes (entity, control, boundary) and realizations of the selected cases |
| Design | High | Design model (classes, subsystems, interfaces), detailed sequences, deployment model, architecture description |
| Implementation | Medium | Program the architecture skeleton and the selected cases, and integrate an executable build |
| Testing | Low | Test cases for what is implemented; verify that the architecture supports the critical cases and the non-functional requirements |

Exit criteria:

- The architecture is stable: no major structural changes are expected.
- The significant use cases are implemented and tested in the baseline.
- The main risks are mitigated or have a clear plan.
- The use-case model is nearly complete and the requirements are stable.
- The Construction plan is realistic and accepted.

Common mistakes: staying only in documents without executable code, choosing easy use cases for the first iteration, designing the whole system in detail, and leaving the non-functional requirements for later.

## Phase 3: Construction

Construction develops the complete product incrementally on top of an already stable architecture, and ends with a beta version ready to be tested with users. It is the phase with the most effort, but it is still iterative and driven by use cases.

Objectives:

- Complete analysis, design, implementation and testing of all pending use cases.
- Integrate the system incrementally, with frequent builds.
- Keep the architecture free of major structural changes.
- Reach a stable beta version.

Iterations: 2 to 4 in a university project. Each one takes a group of use cases (prioritizing risk and value), takes them through the five workflows, and leaves a functional increment that is added to the previous one.

| Workflow | Weight | What is done in Construction |
| --- | --- | --- |
| Requirements | Medium | Detail the remaining use cases; the requirements are already stable |
| Analysis | Medium | Analyze the use cases of each iteration, building on the classes already defined |
| Design | High | Refine the design model with each increment so that it reflects what has been built |
| Implementation | High | Program components and integrate builds: this is the center of the phase |
| Testing | Medium | Unit, integration and system tests for each increment |

Exit criteria:

- All agreed use cases are implemented.
- The product is stable and has passed the system tests.
- A beta version is ready for users.
- Preliminary user documentation exists.
- The stakeholders accept moving on to Transition.

## Phase 4: Transition

Transition delivers the product to users: it is tested with real users, defects are fixed, and the system is left deployed and accepted. This closes the cycle.

Objectives:

- Run beta and acceptance tests with real users.
- Fix defects and make fine adjustments, without adding important new functionality.
- Deploy the product to the final environment.
- Deliver the documentation: user manual and installation manual.
- Evaluate the project: goals against the vision, lessons learned and future work.

Iterations: normally one; a second only if user feedback is extensive.

| Workflow | Weight | What is done in Transition |
| --- | --- | --- |
| Requirements | Low | Touched only if a defect or feedback requires it |
| Analysis | Low | Only if a defect reveals a conceptual problem |
| Design | Low | Targeted adjustments to fix defects |
| Implementation | Medium | Fix defects and prepare the deployment |
| Testing | High | Beta and acceptance tests, and verification of the fixes |

Exit criteria:

- Acceptance tests are run and documented.
- Defects are fixed or recorded with justification.
- The product is deployed.
- The manuals are delivered.
- Formal acceptance by the user or instructor.

If the system keeps evolving, a new cycle starts with its four phases.

## Workflow weight by phase

Requirements weigh most at the start, design and analysis in Elaboration, implementation in Construction, and testing in Transition.

| Workflow | Inception | Elaboration | Construction | Transition |
| --- | --- | --- | --- | --- |
| Requirements | High | High | Medium | Low |
| Analysis | Low | High | Medium | Low |
| Design | Low | High | High | Low |
| Implementation | Minimal | Medium | High | Medium |
| Testing | Minimal | Low | Medium | High |

## Artifacts required and produced by phase

Each phase receives as input the artifacts accepted at the close of the previous one, and delivers new or updated artifacts. The models are not redone: they are completed and refined.

### Inception

Required (inputs):

- Problem statement and the needs of the client or instructor.
- Constraints (time, technology, resources) and domain knowledge.

Produced:

| Artifact | Flow | Status at close |
| --- | --- | --- |
| Vision and scope | Requirements | New, accepted |
| Initial use-case model (\~10-20%) with 1 or 2 critical use cases detailed | Requirements | New |
| Glossary | Requirements | Initial |
| Candidate analysis classes and basic collaboration of the critical cases | Analysis | Preliminary |
| Candidate architecture (sketch, technologies and preliminary deployment) | Design | Preliminary |
| Exploratory prototype (optional) | Implementation | Disposable |
| Acceptance criteria for the critical cases | Testing | New |
| Risk list with strategy | Management | New, prioritized |
| Business case or feasibility and project plan | Management | New |

### Elaboration

Required (inputs):

- Accepted vision and scope.
- Initial use-case model and glossary.
- Candidate architecture.
- Risk list and project plan.

Produced:

| Artifact | Flow | Status at close |
| --- | --- | --- |
| Use-case model (\~80%), with the significant ones detailed (basic and alternative flows) | Requirements | Nearly complete |
| Non-functional requirements | Requirements | Documented and verifiable |
| Analysis model (entity, control and boundary classes, and realizations) | Analysis | Significant cases complete |
| Design model (classes, subsystems, interfaces, sequences) | Design | Significant cases complete |
| Deployment model and data model (if applicable) | Design | Stable |
| Architecture description (use-case, logical, implementation and deployment views) | Design | Stable |
| Baseline of the executable architecture | Implementation | Executable |
| Test cases for what is implemented | Testing | Executed |
| Risks, glossary and Construction plan | Management | Updated |

### Construction

Required (inputs):

- Baseline of the executable architecture and architecture description.
- Nearly complete use-case model and non-functional requirements.
- Analysis and design models from Elaboration.
- Test cases, risk list and Construction plan.

Produced:

| Artifact | Flow | Status at close |
| --- | --- | --- |
| Use-case model | Requirements | Complete and stable |
| Analysis model | Analysis | Complete |
| Design model | Design | Refined, reflects what was built |
| Implementation model (source code and versioned components) | Implementation | Complete |
| Incremental builds | Implementation | One demonstrable per iteration |
| Test model (cases, procedures and unit, integration and system results) | Testing | Complete |
| Traceability use cases ↔ code ↔ tests | All | Complete |
| Preliminary user documentation | Management | Preliminary |
| Beta version of the product | Implementation | Stable |
| Risks and Transition plan | Management | Updated |

### Transition

Required (inputs):

- Stable beta version.
- Test model and system test results.
- Preliminary user documentation.
- Transition plan and risk list.

Produced:

| Artifact | Flow | Status at close |
| --- | --- | --- |
| Beta and acceptance test results | Testing | Documented |
| Defect log | Testing | Fixed or justified |
| Fixes and final version of the product | Implementation | Final |
| Product deployed to the final environment | Implementation | In production |
| User manual and installation or deployment manual | Management | Final |
| Final models consistent with the delivered product | All | Updated |
| Final evaluation (goals against the vision, lessons learned, future work) | Management | New |
| Formal acceptance by the user or instructor | Management | Obtained |

In the artifact tables, "Management" groups the planning and control artifacts (risks, plan, documentation and acceptance). It is not a sixth workflow.

## UML artifacts that support the USDP

The USDP uses UML as its modeling language: each model in the process is expressed with a concrete set of diagrams. The original book uses UML 1.x terminology (for example, collaboration diagrams); in UML 2 they are called communication diagrams.

| UML diagram | Flow | Purpose | Phases where it appears |
| --- | --- | --- | --- |
| Use case | Requirements | Actors, use cases and their relationships (include, extend, generalization); it is the central model of the process | Inception, Elaboration, Construction |
| Activity | Requirements, Analysis | Flow of a complex use case or process; basic and alternative paths | Inception, Elaboration |
| Analysis classes (entity, control and boundary stereotypes) | Analysis | Conceptual structure of the system, without technology | Inception (candidates), Elaboration, Construction |
| Collaboration or communication | Analysis | Realization of an analysis use case: objects and messages between them | Inception, Elaboration, Construction |
| Packages | Analysis, Design | Organization into analysis packages, subsystems and layers | Inception (sketch), Elaboration |
| Design classes | Design | Classes with attributes, operations and relationships, already adapted to the technology | Elaboration, Construction |
| Sequence | Analysis, Design, Testing | Interaction over time between objects; realization of design use cases and basis for test cases | Elaboration, Construction |
| State | Analysis, Design | Life cycle of a class or entity whose behavior depends on its state | Elaboration, Construction |
| Component | Design, Implementation | Components (source code, executables, libraries) and their dependencies | Elaboration, Construction |
| Deployment | Design, Implementation | Physical nodes and the assignment of components to nodes | Inception (preliminary), Elaboration, Transition |

How they relate to the USDP models:

- Use-case model: use-case diagrams, plus the textual descriptions and activity diagrams of the critical cases.
- Analysis model: analysis class, collaboration and package diagrams.
- Design model: design class, sequence, state and package or subsystem diagrams.
- Deployment model: deployment diagram.
- Implementation model: component diagram.
- Test model: has no diagrams of its own; test cases are derived from the use-case and sequence diagrams.

Architecture views and the diagrams that represent them:

- Use-case view: use-case diagram of the ones significant for the architecture.
- Logical view: design class, package and subsystem diagrams.
- Implementation view: component diagram.
- Deployment view: deployment diagram.

For a university project: the minimum the process needs is use cases, analysis classes, sequences of the critical cases, design classes, components and deployment. Add activities and states only when the use case or entity justifies them.

## Traceability and final review

Each model is derived from the previous one, and the use cases are the common thread of the whole project.

| Model | Flow | Based on |
| --- | --- | --- |
| Use cases | Requirements | User needs |
| Analysis | Analysis | Use-case model |
| Design and deployment | Design | Analysis model |
| Implementation | Implementation | Design model |
| Tests | Testing | All of the above |

Final review before delivering:

- Every functional requirement traces to at least one use case, and every use case to classes, components and test cases.
- A plan of iterations exists, and each iteration ended with an evaluable result.
- The risk list was updated in every iteration.
- Names, actors and use cases match across all documents.
- The four phases have their evidence, and their exit criteria are met or justified.
- The UML diagrams are readable and follow standard notation.
- You can explain why each artifact exists and which decision it supports.

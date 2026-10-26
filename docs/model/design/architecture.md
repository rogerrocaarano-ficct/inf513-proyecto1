# Architecture

| | |
| --- | --- |
| Status | <!-- Draft / Reviewed / Baselined --> |
| Iteration | <!-- E1 --> |

## Architectural Decisions

| ID | Decision | Rationale | Alternatives considered | Requirements | Status |
| --- | --- | --- | --- | --- | --- |
| AD-001 | <!-- e.g. Split the system into a web frontend and a backend API --> | <!-- why --> | <!-- e.g. Server-rendered monolith --> | <!-- NFR-SU-001 --> | <!-- Proposed / Accepted / Superseded by AD-00x --> |

## Applications

| Application | Kind | Architectural style | Technical layers | Subsystem parts it holds | Decision |
| --- | --- | --- | --- | --- | --- |
| <!-- web-app --> | <!-- Frontend / Backend / Mobile / Desktop / Batch --> | <!-- e.g. Component-based SPA --> | <!-- e.g. Views, State, API client --> | <!-- Presentation of Sales and Inventory --> | <!-- AD-001 --> |
| <!-- api --> | <!-- Backend --> | <!-- e.g. Layered --> | <!-- e.g. Controllers, Application services, Domain, Infrastructure --> | <!-- Application and domain logic of Sales and Inventory; Persistence --> | <!-- AD-002 --> |

## Design Mechanisms

| Analysis mechanism | Design mechanism | Technology | Decision |
| --- | --- | --- | --- |
| <!-- AM-001 Persistence --> | <!-- e.g. Repositories over a relational database --> | <!-- e.g. PostgreSQL 16 + JPA --> | <!-- AD-003 --> |

## Architecture Views

| View | Content | Described in |
| --- | --- | --- |
| Use-case view | Architecturally significant use cases | [Use-case catalog](../use-cases/use-cases.md) (Arch. significant = Yes) |
| Logical view | Subsystems, their interfaces and the applications they live in | [Subsystems](subsystems/subsystems.md), [Applications](#applications) |
| Implementation view | Components and their dependencies | Pending: implementation workflow |
| Deployment view | Nodes, infrastructure and allocation of applications | [Deployment](deployment.md) |

## Open Questions

- [ ] <!-- question --> (ask: <!-- stakeholder -->)

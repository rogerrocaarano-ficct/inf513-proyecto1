# UC-001 <!-- Use case name -->: User Interface Design

| | |
| --- | --- |
| Status | <!-- Draft / Reviewed / Baselined --> |
| Iteration | <!-- E1 --> |
| Use case | [UC-001](../use-case.md) |
| Sketches | <!-- ../ui/ or None --> |

## Screens

| Screen | Boundary class | Design element | UC steps |
| --- | --- | --- | --- |
| <!-- Sale screen --> | <!-- Sales::SaleForm --> | <!-- SaleView --> | <!-- 1–5, A1, E1 --> |

## <!-- Sale screen -->

**Purpose:** <!-- what the actor does here -->

**Fields:**

| Field | Control | Required | Validation | Data |
| --- | --- | --- | --- | --- |
| <!-- Product code --> | <!-- Text input with barcode scanner --> | <!-- Yes --> | <!-- 13 digits --> | <!-- Product.code --> |

**Actions:**

| Action | Trigger | UC step | Result |
| --- | --- | --- | --- |
| <!-- Add item --> | <!-- Enter key / Add button --> | <!-- 2 --> | <!-- Line added to the list; focus back on Product code --> |

**Messages:**

| Situation | Message | Kind |
| --- | --- | --- |
| <!-- E1 Product not found --> | <!-- "No product with code {code}" --> | <!-- Error / Warning / Confirmation / Information --> |

## Navigation

| From | Action | To |
| --- | --- | --- |
| <!-- Sale screen --> | <!-- Pay --> | <!-- Payment screen --> |

## Open Questions

- [ ] <!-- question --> (ask: <!-- stakeholder -->)

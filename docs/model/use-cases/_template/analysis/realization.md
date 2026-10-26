# UC-001 <!-- Use case name -->: Analysis Realization

| | |
| --- | --- |
| Status | <!-- Draft / Reviewed / Baselined --> |
| Iteration | <!-- E1 --> |
| Use case | [UC-001](../use-case.md) |

## Participating Classes

| Class | Stereotype | Package | Responsibility in this use case |
| --- | --- | --- | --- |
| <!-- Cashier --> | <!-- actor --> | — | <!-- e.g. Enters the items --> |
| <!-- SaleForm --> | <!-- boundary / control / entity --> | <!-- [Sales](../../../analysis/sales/package.md) --> | <!-- e.g. Captures the items the cashier enters --> |

## Structural View

| From | To | Meaning |
| --- | --- | --- |
| <!-- Cashier --> | <!-- SaleForm --> | <!-- Interacts with --> |
| <!-- SaleForm --> | <!-- SaleHandler --> | <!-- Requests registration --> |
| <!-- Sale --> | <!-- Product --> | <!-- Each sale refers to the products it sells (see entity.md) --> |

## Behavioral View

### Basic Flow

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | <!-- 1 --> | <!-- Cashier --> | <!-- SaleForm --> | <!-- enter item code --> |
| 2 | <!-- 1 --> | <!-- SaleForm --> | <!-- SaleHandler --> | <!-- add item --> |

### <!-- A1 Name -->

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | <!-- A1.1 --> | | | |

## Special Requirements

| Requirement | Concern | Handled by |
| --- | --- | --- |
| <!-- NFR-SE-001 --> | <!-- Persistence / Security / Distribution / ... --> | <!-- Class or AM-001 --> |

## Feedback to Requirements

- <!-- e.g. UC-001 step 4 does not say what happens when the product does not exist: added E2 -->

## Open Questions

- [ ] <!-- question --> (ask: <!-- stakeholder -->)

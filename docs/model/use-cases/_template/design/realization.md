# UC-001 <!-- Use case name -->: Design Realization

| | |
| --- | --- |
| Status | <!-- Draft / Reviewed / Baselined --> |
| Iteration | <!-- E1 --> |
| Use case | [UC-001](../use-case.md) |
| Analysis realization | [analysis/realization.md](../analysis/realization.md) |

## Traceability from Analysis

| Analysis class | Stereotype | Design elements |
| --- | --- | --- |
| <!-- Sales::SaleForm --> | <!-- boundary --> | <!-- SaleView, SaleController --> |
| <!-- Sales::SaleHandler --> | <!-- control --> | <!-- SaleService --> |
| <!-- Inventory::Product --> | <!-- entity --> | <!-- ProductCatalog (interface of Inventory) --> |

## Structural View

### Participating Elements

| Element | Kind | Subsystem | Application | Role in this use case |
| --- | --- | --- | --- | --- |
| <!-- SaleView --> | <!-- Class --> | <!-- Sales --> | <!-- web-app --> | <!-- e.g. Shows the sale and captures product codes --> |
| <!-- SaleController --> | <!-- Class / Interface / Enumeration --> | <!-- Sales --> | <!-- api --> | <!-- e.g. Receives the cashier's requests --> |
| <!-- SaleService --> | <!-- Class --> | <!-- Sales --> | <!-- api --> | <!-- e.g. Coordinates the registration of the sale --> |
| <!-- ProductCatalog --> | <!-- Interface --> | <!-- Inventory --> | <!-- api --> | <!-- e.g. Provides the product of a code --> |

### <!-- SaleController -->

| Member | Kind | Type or signature | Visibility |
| --- | --- | --- | --- |
| <!-- saleService --> | <!-- Attribute --> | <!-- SaleService --> | <!-- - --> |
| <!-- addItem --> | <!-- Operation --> | <!-- addItem(saleId: UUID, code: String): SaleLineDto --> | <!-- + --> |

### Relationships

| From | To | Kind | Multiplicity | Navigability |
| --- | --- | --- | --- | --- |
| <!-- SaleController --> | <!-- SaleService --> | <!-- Association / Aggregation / Composition / Generalization / Realization / Dependency --> | <!-- 1 to 1 --> | <!-- From → To --> |

## Behavioral View

### Basic Flow

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | <!-- 1 --> | <!-- Cashier --> | <!-- SaleView --> | <!-- enter product code --> | |
| 2 | <!-- 1 --> | <!-- SaleView --> | <!-- SaleController --> | <!-- addItem(saleId, code) --> | <!-- SaleLineDto --> |
| 3 | <!-- 2 --> | <!-- SaleController --> | <!-- SaleService --> | <!-- addItem(saleId, code) --> | <!-- SaleLine --> |
| 4 | <!-- 2 --> | <!-- SaleService --> | <!-- ProductCatalog --> | <!-- findByCode(code) --> | <!-- Product --> |

### <!-- A1 Name -->

| # | UC step | From | To | Operation | Returns |
| --- | --- | --- | --- | --- | --- |
| 1 | <!-- A1.1 --> | | | | |

## Implementation Requirements

- <!-- e.g. The sale and its lines are saved in a single transaction (NFR-RE-002) -->

## Open Questions

- [ ] <!-- question --> (ask: <!-- stakeholder -->)

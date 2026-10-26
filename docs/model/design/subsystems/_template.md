# <!-- Subsystem name, e.g. Sales -->

| | |
| --- | --- |
| Type | <!-- Business / General --> |
| Comes from | <!-- [Sales](../../analysis/sales/package.md) / Sales (SaleForm, SaleHandler, Sale) / AM-001 / New --> |
| Status | <!-- Draft / Reviewed / Baselined --> |
| Iteration | <!-- E1 --> |

## Responsibility

## Provided Interfaces

### <!-- SalesHistory -->

- **Used by (derived):** <!-- Subsystems, e.g. Reporting -->
- **Use cases (derived):** <!-- UC-007 -->

| Operation | Description | Errors |
| --- | --- | --- |
| <!-- findByDate(date: LocalDate): List<SaleSummary> --> | <!-- Returns the sales registered on a date --> | <!-- None --> |

## Required Interfaces

| Interface | Provided by | Used for | Comes from |
| --- | --- | --- | --- |
| <!-- ProductCatalog --> | <!-- [Inventory](inventory.md) --> | <!-- Get the price of each item --> | <!-- Sales::SaleHandler → Inventory::Product / Split: Sales::ReportHandler → Sales::Sale / AM-001 --> |

## Open Questions

- [ ] <!-- question --> (ask: <!-- stakeholder -->)

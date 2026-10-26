# Logical Database Model

| | |
| --- | --- |
| Status | <!-- Draft / Reviewed / Baselined --> |
| Iteration | <!-- E1 --> |
| Database | <!-- e.g. PostgreSQL 16 (AD-002) --> |

## Tables

| Table | Comes from | Description |
| --- | --- | --- |
| [<!-- sale -->](#<!-- sale -->) | <!-- Sales::Sale --> | <!-- One row per registered sale --> |
| [<!-- sale_line -->](#<!-- sale_line -->) | <!-- Relationship Sales::Sale – Inventory::Product --> | <!-- One row per product in a sale --> |

## Table Details

### <!-- sale -->

| Column | Type | Null | Key | Default | Description |
| --- | --- | --- | --- | --- | --- |
| <!-- id --> | <!-- UUID --> | <!-- No --> | <!-- PK --> | | <!-- Sale identifier --> |
| <!-- date --> | <!-- TIMESTAMP --> | <!-- No --> | | <!-- now() --> | <!-- When the sale was registered --> |
| <!-- total --> | <!-- NUMERIC(12,2) --> | <!-- No --> | | <!-- 0 --> | <!-- Sum of the line subtotals --> |

**Constraints:**

- <!-- e.g. CHECK (total >= 0) -->

**Indexes:**

| Index | Columns | Unique | Purpose |
| --- | --- | --- | --- |
| <!-- sale_date_idx --> | <!-- date --> | <!-- No --> | <!-- Daily sales report (UC-007) --> |

## Relationships

| From | To | Cardinality | On delete | Description |
| --- | --- | --- | --- | --- |
| <!-- sale_line.sale_id --> | <!-- sale.id --> | <!-- many to one --> | <!-- Cascade / Restrict / Set null --> | <!-- A line belongs to one sale --> |
| <!-- sale_line.product_id --> | <!-- product.id --> | <!-- many to one --> | <!-- Restrict --> | <!-- A line refers to one product --> |

## Open Questions

- [ ] <!-- question --> (ask: <!-- stakeholder -->)

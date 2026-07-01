# Performance Review — Phase 5

## Database Connection Management

| Issue | Severity | Detail |
|-------|----------|--------|
| No connection pooling | HIGH | `DBContext.getConnection()` returns a new physical JDBC connection each call. Tomcat connection pool (`context.xml`) is not configured. Each page load opens/closes at least 1-3 connections. |
| No JNDI datasource | MEDIUM | Connection string hardcoded with `localhost:1433`, no datasource abstraction. Environment vars supported (`DB_URL`, `DB_USER`, `DB_PASSWORD`). |
| Connection leak risk | MEDIUM | Several DAOs don't use try-with-resources for Connection (see `UserManagementDao.java` which manages autoCommit manually). |

## SQL Query Analysis

### SELECT * Occurrences

Found 12+ `SELECT *` queries across the DAO layer. While acceptable for narrow tables, some join queries select all columns from multiple tables unnecessarily:

```
// InventoryTicketDAO.java:47-53 — selects t.* plus 3 JOIN table columns
// PurchaseOrderDAO.java — select all columns from 4 joined tables
// CustomerDAO.java:800 lines — multiple SELECT * patterns
```

### N+1 Query Patterns

| Location | Pattern | Impact |
|----------|---------|--------|
| `InventoryTicketDAO.getTransferProgress` | Called inside loop in `handleTransferTab`, `handleHistoryTab` | Each call executes 1-2 queries |
| `InventoryDAO.getCurrentStock` | Called per detail in `InventoryController.doGet` viewTicket | N+1 for each product in ticket |

### Missing Index Candidates

Based on query patterns in the DAO layer:

| Table | Column(s) | Query Type | Priority |
|-------|-----------|------------|----------|
| `Employee` | `email`, `phone` | Login lookup `WHERE email = ? OR phone = ?` | HIGH |
| `Employee` | `status` | Filtering active/locked users | MEDIUM |
| `[order]` | `order_code`, `branch_id`, `created_at` | Order listing/sorting | HIGH |
| `[order]` | `customer_id` | Customer order history | MEDIUM |
| `inventory` | `(warehouse_id, product_id)` | Stock lookup (composite) | HIGH |
| `inventory` | `status` | Low-stock filtering | LOW |
| `inventory_ticket` | `ticket_code` | Code-based lookup | HIGH |
| `inventory_ticket` | `(from_warehouse_id, to_warehouse_id)` | Warehouse transfer queries | MEDIUM |
| `stock_transaction` | `(reference_type, reference_id)` | Transaction lookup by reference | HIGH |
| `product` | `product_name` | Search queries with LIKE | MEDIUM |
| `customer_point` | `cus_id` | Points lookup | HIGH |
| `inventory_ticket_detail` | `ticket_id` | Detail loading | HIGH |

## Startup & Request Performance

| Concern | Detail |
|---------|--------|
| Servlet init DB calls | `BaseController` doesn't preload. Each servlet creates DAOs in `init()` — lightweight. |
| Static resource size | FontAwesome: 29MB (5773 files). Removing unused glyphs would save ~28MB. |
| JSP compilation | ~57 JSPs compiled on first access. Tomcat JSP compiler does this lazily. |
| Pagination | Most list pages use OFFSET-FETCH pagination — adequate for <100K rows. Some DAOs lack pagination entirely (e.g., `CustomerDAO.getAll()`). |

## Memory Analysis

| Concern | Risk | Detail |
|---------|------|--------|
| Session size | LOW | Only stores `currentUser` (Employee POJO) and `cartTabs` (Map<Integer, OrderTab>). Cart could grow large for bulk orders. |
| No caching | MEDIUM | No application-level caching. Every DAO call hits the database. Dashboard/KPI queries re-execute on every page load. |
| Large collections in DAOs | LOW | `getAllSaleOrders()` returns full list without pagination — could OOM for 100K+ orders. |
| `sendError(500, stackTrace)` | MEDIUM | `InventoryController` sends stack trace in error response — memory/security concern. |

## Identified Bottlenecks

1. **InventoryController doGet()** — 225-line method handling 10+ actions, 4 tabs, role branching, warehouse resolution, KPI calculation. Should be decomposed.
2. **CustomerDAO** — 814 lines, mixed concerns (CRUD, loyalty, overview, search). Multiple N+1 patterns.
3. **BranchController** — 400+ lines, contains image upload/validation logic.
4. **PdfReportUtil** — in-memory PDF generation for large employee reports. No streaming or pagination for number of pages.

## Recommendations

1. Configure Tomcat JDBC connection pool in `context.xml` (single change, immediate impact)
2. Add composite index on `inventory(warehouse_id, product_id)` and `stock_transaction(reference_type, reference_id)`
3. Replace FontAwesome CDN/Material Icons with a single icon system (saves 29MB + 5773 files)
4. Decompose `InventoryController` into separate servlets per tab

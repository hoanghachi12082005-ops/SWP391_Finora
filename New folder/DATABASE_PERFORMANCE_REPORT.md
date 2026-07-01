# Database Performance Report — Phase 5

## Schema Overview

- **Database**: DBFinoraV3 (SQL Server)
- **Tables**: 26 (21 in V3 schema + 5 from migration_missing_tables.sql)
- **Engine**: SQL Server (via mssql-jdbc 12.6.1)

## Current Indexes (from DBFinoraV3.sql)

The schema defines primarily primary key indexes and foreign key constraints. No explicit non-clustered indexes for query performance are defined in the DDL.

## Query Performance Issues

### 1. Login Query (Hot Path)

**Location**: `EmployeeDAO.findByEmailOrPhone()`
```sql
SELECT * FROM Employee WHERE (email = ? OR phone = ?)
```
- No index on `email` or `phone` — full scan on every login
- `SELECT *` returns all columns when only `passwordHash`, `status`, `failedLoginCount` needed
- **Fix**: Index on `(email, phone)`, narrow projection

### 2. Order Listing

**Location**: `OrderDAO.getAllSaleOrders()`
```sql
SELECT o.*, c.full_name, e.fullName, b.branch_name
FROM [order] o
LEFT JOIN Customer c ON o.customer_id = c.cus_id
JOIN Employee e ON o.emp_id = e.emp_id
JOIN Branch b ON o.branch_id = b.branch_id
WHERE o.order_type = 'SALE'
ORDER BY o.order_id DESC
```
- No `OFFSET-FETCH` pagination — returns ALL orders
- Sorts by `order_id` (clustered PK) — OK for sort, but no WHERE filter index
- **Fix**: Add pagination, index on `(order_type, created_at)` for filtered sort

### 3. Inventory Stock Lookup

**Location**: `InventoryDAO.getStock()`, `deductStock()`, `increaseStock()`
```sql
SELECT quantity_in_stock FROM inventory WHERE warehouse_id = ? AND product_id = ?
UPDATE inventory SET quantity_in_stock = quantity_in_stock - ? WHERE warehouse_id = ? AND product_id = ?
```
- **Missing composite index** on `(warehouse_id, product_id)` — this is the most frequently executed query pattern
- Used for: checkout stock check, import, export, transfer, dispatch
- **Fix**: `CREATE INDEX idx_inventory_wh_product ON inventory(warehouse_id, product_id)`

### 4. Stock Transaction Lookups

**Location**: `StockTransactionDAO.findByReference()`
```sql
SELECT * FROM stock_transaction WHERE reference_type = ? AND reference_id = ?
```
- Full scan on every ticket detail view
- **Fix**: `CREATE INDEX idx_stock_tx_ref ON stock_transaction(reference_type, reference_id)`

### 5. Dashboard KPI Queries

**Location**: `InventoryDAO.getDashboardKPI()`
- Multiple aggregate queries: `COUNT(*)`, `SUM(quantity_in_stock)`, filtered by warehouse
- Executes on every inventory page load
- **Fix**: Materialized view or summary table for real-time dashboards

### 6. Search Performance

**Location**: `ProductDAO.findAll()`, `CustomerDAO.findByKeyword()`
```sql
WHERE p.product_name LIKE N'%keyword%'
```
- Leading wildcard `%keyword%` prevents index seek — always scans
- For <10K products this is acceptable; for >50K, consider full-text search

## Migration Script Analysis

| File | Purpose | Status |
|------|---------|--------|
| `DBFinoraV3.sql` | Full schema | Current active schema |
| `migration_missing_tables.sql` | 5 missing tables | Applied in Phase 1 |
| `migration_add_failed_login_count.sql` | Failed login tracking | Applied |
| `migration_alter_payment_for_cashbook.sql` | Payment table changes | Applied |
| `migration_hoang_product_image.sql` | Product image support | Applied |
| `patch_fix_product_v2.sql` | Product fixes | Applied |
| `patch_add_product_import_fields.sql` | Import fields | Applied |

## Recommended Indexes

```sql
-- High priority
CREATE INDEX idx_employee_email_phone ON Employee(email, phone);
CREATE INDEX idx_inventory_wh_product ON inventory(warehouse_id, product_id);
CREATE INDEX idx_stock_tx_ref ON stock_transaction(reference_type, reference_id);
CREATE INDEX idx_inventory_ticket_code ON inventory_ticket(ticket_code);
CREATE INDEX idx_order_branch_date ON [order](branch_id, created_at DESC);
CREATE INDEX idx_customer_point_cus ON customer_point(cus_id);

-- Medium priority (for search/like patterns)
CREATE INDEX idx_product_name ON product(product_name);
CREATE INDEX idx_order_customer ON [order](customer_id);
CREATE INDEX idx_ticket_warehouse ON inventory_ticket(from_warehouse_id, to_warehouse_id);
```

## Connection Management

- **Current**: `DriverManager.getConnection()` per DAO call (no pool)
- **Recommended**: Tomcat JDBC Pool in `context.xml`:
  ```xml
  <Resource name="jdbc/DBFinoraV3" auth="Container"
            type="javax.sql.DataSource" maxTotal="20" maxIdle="5"
            username="sa" password="123456"
            driverClassName="com.microsoft.sqlserver.jdbc.SQLServerDriver"
            url="jdbc:sqlserver://localhost:1433;databaseName=DBFinoraV3;encrypt=true;trustServerCertificate=true"/>
  ```
- **Impact**: Reduces connection creation overhead by ~99% (creates connections once, reuses them)

# DATABASE SYNCHRONIZATION REPORT — Phase 1

**Date:** 2026-07-01  
**Target Database:** DBFinoraV3 (DBFinoraV3.sql)  
**Reference:** 21 tables in V3 schema

---

## 1. MISSING TABLES (Created via migration)

| # | Table | Created By | Purpose | Status |
|---|-------|-----------|---------|--------|
| 1 | `shift` | `migration_missing_tables.sql` | POS shift management (ShiftDAO, ShiftServlet) | ✅ ADDED |
| 2 | `cash_transaction` | `migration_missing_tables.sql` | Cash drawer transactions within shifts (CashTransactionDAO) | ✅ ADDED |
| 3 | `inventory_ticket` | `migration_missing_tables.sql` | Stock transfer/exchange tickets (InventoryTicketDAO) | ✅ ADDED |
| 4 | `inventory_ticket_detail` | `migration_missing_tables.sql` | Line items for inventory tickets (InventoryTicketDAO) | ✅ ADDED |
| 5 | `supplier_product` | `migration_missing_tables.sql` | Supplier-to-product pricing bridge (SupplierProductDAO) | ✅ ADDED |

These tables existed in the V2 schema and were removed during the V3 refactor, but the code from merged branches still references them. All 5 tables are recreated with proper FKs matching V3 conventions.

---

## 2. COLUMN MISMATCHES FIXED IN DAO SQL

### 2.1 StockTransactionDAO (`dao/inventory/StockTransactionDAO.java`)

| Old (Broken) | New (Fixed) | Reason |
|-------------|------------|--------|
| `p.Name` | `p.product_name` | V3 product table column is `product_name` |
| `p.ProductID` | `p.product_id` | V3 product table PK is `product_id` |
| `e.EmployeeID` | `e.emp_id` | V3 Employee table PK is `emp_id` |
| `'' as product_codebar` | `p.product_codebar` | V3 product table HAS `product_codebar` column |

### 2.2 InventoryTicketDAO (`dao/inventory/InventoryTicketDAO.java`)

| Old (Broken) | New (Fixed) | Reason |
|-------------|------------|--------|
| `e.EmployeeID` (6 occurrences) | `e.emp_id` | V3 Employee PK is `emp_id` |
| `p.Name` | `p.product_name` | V3 product column is `product_name` |
| `p.ProductID` | `p.product_id` | V3 product PK is `product_id` |
| `Product p` | `product p` | Lowercase to match V3 convention |
| `min_stock_level, max_stock_level` (in INSERT) | `status, updated_at` | Columns don't exist in V3 `inventory` |

### 2.3 CategoryDAO (`dao/product/CategoryDAO.java`)

| Old (Broken) | New (Fixed) | Reason |
|-------------|------------|--------|
| `SELECT category_id` (no alias) | `SELECT category_id AS CategoryID` | `rs.getInt("CategoryID")` expects this alias |

### 2.4 BranchDAO (`dao/branch/BranchDAO.java`)

| Old (Broken) | New (Fixed) | Reason |
|-------------|------------|--------|
| `UPPER(status) = 'PAID'` (4 occurrences) | `UPPER(status) = 'COMPLETED'` | V3 order status values are PENDING/COMPLETED/CANCELLED |

---

## 3. SQL BUGS FIXED

### 3.1 ProductDAO.update — Missing Parameter

**File:** `dao/product/ProductDAO.java:157`  
**Bug:** `stmt.executeUpdate()` called without setting `product_id` parameter. SQL has 5 `?` but only 4 params were set.  
**Fix:** Added `stmt.setInt(5, product.getProductID())` before `executeUpdate()`.

### 3.2 ProductDAO.getTotalCount — Unused Parameter

**File:** `dao/product/ProductDAO.java:94`  
**Bug:** `supplierID` parameter was set via `stmt.setInt(idx++, supplierID)` but there is no corresponding `AND` clause in the SQL. This caused "parameter index out of range" when all 5 params were provided.  
**Fix:** Removed the `supplierID` parameter setting (the filter was dropped when V3 refactored the product table).

### 3.3 ProductDAO.findAll — Dead Filter

**File:** `dao/product/ProductDAO.java:43`  
**Bug:** `AND 'active' = ?` compares a string literal `'active'` to the parameter value instead of a column. Since V3 product table has no status column, this was always a no-op.  
**Fix:** Removed the filter entirely (V3 product table has no status column).

### 3.4 CheckoutServlet — Null Session Attribute Key

**File:** `controller/sales/CheckoutServlet.java:183,188`  
**Bug:** `String ACTIVE_TAB_ATTR = null; session.setAttribute(ACTIVE_TAB_ATTR, 1)` — sets session attribute with null key, which silently fails.  
**Fix:** Replaced with `session.setAttribute("activeTab", value)`.

### 3.5 InventoryTicketDAO.confirmReceiptWithDiscrepancy — Non-Existent Columns

**File:** `dao/inventory/InventoryTicketDAO.java:381`  
**Bug:** INSERT into `inventory` referenced `min_stock_level, max_stock_level` columns that don't exist in V3 `inventory` table.  
**Fix:** Changed to use `status, updated_at` columns that exist in V3.

---

## 4. MISSING `update_at` TIMESTAMP UPDATES FIXED

V3 Employee table has `update_at` column that was not being updated in several UPDATE statements:

| File | Method | Fix Applied |
|------|--------|-------------|
| `dao/user/ProfileDao.java` | `updateProfile()` | ✅ Added `update_at = GETDATE()` |
| `dao/user/ProfileDao.java` | `updatePasswordHash()` | ✅ Added `update_at = GETDATE()` |
| `dao/user/UserManagementDao.java` | `updateEmployee()` | ✅ Added `update_at = GETDATE()` |
| `dao/user/UserManagementDao.java` | `updateEmployeeStatus()` | ✅ Added `update_at = GETDATE()` |
| `dao/user/UserManagementDao.java` | `resetEmployeePassword()` | ✅ Added `update_at = GETDATE()` |
| `dao/product/CategoryDAO.java` | `updateCategory()` | ✅ Added `update_at = GETDATE()` |

Also fixed: `UserManagementDao.addEmployee()` INSERT was missing `created_at, update_at` columns — added `GETDATE()` defaults.

---

## 5. ENTITY vs DATABASE MAPPING

| Entity | Table | Status | Notes |
|--------|-------|--------|-------|
| Employee | Employee | ✅ SYNCED | All fields match after fixes |
| Role | Role | ✅ SYNCED | `discription` typo preserved (DB has same) |
| Branch | Branch | ✅ SYNCED | `image_URL` vs `imageUrl` (Java alias bridges) |
| Customer | customer | ✅ SYNCED | `cus_id` ↔ `customerId` (alias bridges) |
| Voucher | voucher | ✅ SYNCED | |
| Supplier | supplier | ⚠️ MINOR | `supplier_name`=name, `phone_number`=phone (aliases) |
| Warehouse | warehouse | ✅ SYNCED | |
| Unit | unit | ⚠️ MINOR | `description` field missing from model (not in DAO use) |
| Category | category | ⚠️ MINOR | `created_at`, `update_at` fields missing from model |
| Product | product | ⚠️ FIELDS EXTRA | `quantity`, `supplierIDs`, `importPrice`, `status` are extra/transient (not in V3 table) |
| Inventory | inventory | ✅ SYNCED | |
| Order | [order] | ✅ SYNCED | |
| OrderDetail | order_detail | ✅ SYNCED | |
| Payment | payment | ⚠️ EXTRA FIELDS | `method`, `paymentType`, `description`, `employeeId`, `branchId` not in V3 payment table |
| LoyaltyPointSetting | loyalty_point_setting | ✅ SYNCED | |
| AuditLog | audit_log | ✅ SYNCED | |
| StockTransaction | stock_transaction | ✅ SYNCED | |
| InventoryTicket | inventory_ticket | ✅ SYNCED | New table created |
| InventoryTicketDetail | inventory_ticket_detail | ✅ SYNCED | New table created |
| Shift | shift | ✅ SYNCED | New table created |
| CashTransaction | cash_transaction | ✅ SYNCED | New table created |
| SupplierProduct | supplier_product | ✅ SYNCED | New table created |

---

## 6. FOREIGN KEY ISSUES

| Table | FK Column | References | Status |
|-------|-----------|-----------|--------|
| Employee | branch_id | Branch(branch_id) | ✅ OK in V3 |
| Employee | role_id | Role(role_id) | ✅ OK in V3 |
| [order] | customer_id | customer(cus_id) | ✅ OK |
| [order] | emp_id | Employee(emp_id) | ✅ OK |
| [order] | branch_id | Branch(branch_id) | ✅ OK |
| [order] | supplier_id | Supplier(supplier_id) | ✅ OK |
| [order] | voucher_id | Voucher(voucher_id) | ✅ OK |
| [order] | warehouse_id | Warehouse(warehouse_id) | ✅ OK |
| inventory_ticket (new) | created_by | Employee(emp_id) | ✅ OK |
| inventory_ticket (new) | from_warehouse_id | Warehouse(warehouse_id) | ✅ OK |
| inventory_ticket (new) | to_warehouse_id | Warehouse(warehouse_id) | ✅ OK |
| inventory_ticket_detail (new) | ticket_id | inventory_ticket(ticket_id) | ✅ OK |
| inventory_ticket_detail (new) | product_id | product(product_id) | ✅ OK |
| shift (new) | emp_id | Employee(emp_id) | ✅ OK |
| shift (new) | branch_id | Branch(branch_id) | ✅ OK |
| cash_transaction (new) | shift_id | shift(shift_id) | ✅ OK |
| supplier_product (new) | supplier_id | Supplier(supplier_id) | ✅ OK |
| supplier_product (new) | product_id | product(product_id) | ✅ OK |

---

## 7. INDEX RECOMMENDATIONS

| Table | Column(s) | Index Type | Rationale |
|-------|-----------|-----------|-----------|
| Employee | email | UNIQUE | Used in login lookup (`WHERE email = ?`) |
| Employee | phone | NON-UNIQUE | Used in login lookup |
| Employee | branch_id | NON-CLUSTERED | FK join with Branch |
| [order] | created_at | NON-CLUSTERED | Date-range queries in reports |
| [order] | branch_id | NON-CLUSTERED | FK join + branch filtering |
| [order] | emp_id | NON-CLUSTERED | Employee sales summary |
| customer | phone | UNIQUE | Customer lookup by phone |
| customer | email | NON-UNIQUE | Customer lookup by email |
| product | product_name | NON-CLUSTERED | Search/filter by name |
| product | category_id | NON-CLUSTERED | FK join + category filter |
| inventory | product_id | NON-CLUSTERED | Stock lookup |
| inventory | warehouse_id | NON-CLUSTERED | Warehouse stock queries |
| stock_transaction | reference_type, reference_id | NON-CLUSTERED | findByReference queries |

---

## 8. REMAINING DB RISKS

| Risk | Impact | Mitigation |
|------|--------|------------|
| ProductDAO hardcodes `'active' AS Status` — V3 product has no status column | Low — always shows "active" | Acceptable since V3 has no product status |
| Customer model has `cusType` field not in V3 customer table | Low — hardcoded "REGULAR" | Acceptable, field is never persisted |
| Payment model has extra fields not in V3 payment table | Medium — may confuse developers | These fields were added by V2 migration scripts — keep for cashbook feature |
| SupplierDAO uses `supplier_name` as `Name` alias | Low — alias bridges the gap | Acceptable |
| No connection pooling (DriverManager) | Medium — performance | HikariCP needs to be added (Phase 2+) |

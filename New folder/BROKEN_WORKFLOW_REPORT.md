# BROKEN WORKFLOW REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## CRITICAL BREAKAGES (7)

### B1. Password Change is Permanently Broken
**Files:** `ProfileServlet.java:211-213`, `PasswordUtil.java`
**Root Cause:** Uses `PasswordUtil.hash(oldPassword)` (re-hashes with new salt) to compare against current hash, instead of `PasswordUtil.verify(oldPassword, currentHash)`. Since BCrypt generates a unique salt each time, the comparison ALWAYS fails.
**Impact:** No user can change their password through the profile page.
**Fix:** Replace `PasswordUtil.hash(oldPassword)` comparison with `PasswordUtil.verify(oldPassword, currentHash)`.

### B2. User Lock/Unlock Always Fails
**Files:** `AdminUserServlet.java:110-115`, `UserManagementDao.java:621-642`
**Root Cause:** `updateEmployeeStatus(status="locked")` writes `status='locked'` but V3 schema CHECK constraint only allows `'ACTIVE'` and `'INACTIVE'`. The UPDATE throws SQLException, silently swallowed by `e.printStackTrace()`.
**Impact:** Admins cannot lock or unlock any user. The UI shows success but the DB rejects the change.
**Fix:** Use status values `'ACTIVE'` and `'INACTIVE'` instead of `"active"` and `"locked"`.

### B3. Refund Destroys Data Integrity
**Files:** `OrdersServlet.java:148-185`, `OrderDAO.java`, `CustomerPointDAO.java`
**Root Cause:** Refund only changes order status to CANCELLED and inserts an audit log entry. It does NOT:
- Restore inventory stock
- Reverse customer loyalty points
- Void the payment record
- Decrement voucher usage count
- Wrap in a DB transaction
**Impact:** Every refund permanently corrupts inventory (+1 item), customer data (+points), and financial records (+payment).
**Fix:** Add a full reverse transaction: restore stock via `inventoryDAO.increaseStock()`, reverse points via `customerPointDAO.deductPoints()`, void payment, revert voucher, all in a single DB transaction.

### B4. VNPAY Payment Cannot Complete
**Files:** `util/vnpay/Config.java`
**Root Cause:** Config references `/vnpay_return` and `/vnpay_ipn` callback URLs, but NO servlet is mapped to these paths anywhere in the codebase. The payment flow stops after the user is redirected to VNPAY sandbox.
**Impact:** VNPAY payments can never be completed, verified, or recorded. Customers pay but the system never updates the order.
**Fix:** Create `VnpayReturnServlet` and `VnpayIpnServlet` with HMAC-SHA512 signature verification, order status update, and transaction recording.

### B5. Finance Module Shows Zero Balances
**Files:** `dao/finance/PaymentDAO.java:62-82`
**Root Cause:** `getTotalCashBalance()`, `getTotalBankBalance()`, `getSumIncome()`, `getSumExpense()` all return hardcoded 0.0. The `getWeeklyOverview()` hardcodes expense as 0. Filter parameters `type` and `paymentMethod` are received but never applied to SQL queries. Payment type (INCOME/EXPENSE) is not persisted in any DB column.
**Impact:** The cashbook page always shows zero fund balances. Expense chart always shows zero. Payments cannot be filtered by type. Income vs expense distinction is lost.
**Fix:** Implement real SQL aggregation queries, add `payment_type` column storage, wire filter parameters.

### B6. Purchase Order Module is Completely Non-Functional
**Files:** `PurchaseOrderController.java`, `PurchaseOrderDAO.java`, `PurchaseDetailDAO.java`, JSPs
**Root Cause:** Controller `doPost()` is a stub with a hardcoded message "Connect Service/DAO for real processing". DAO has no insert/update/delete methods. JSPs are static mockups with hardcoded sample data.
**Impact:** Users cannot create, view, approve, receive, or cancel purchase orders. No integration with inventory or suppliers.
**Fix:** Implement full PO creation, approval workflow, goods receipt with inventory update, status transitions.

### B7. Debug File Writes + Auth Bypass in Production Code
**Files:** `InventoryController.java:395-416`
**Root Cause:** Writes debug data to a hardcoded path `C:\Users\letha\.gemini\...\debug.txt`. When session has no `currentUser`, creates a mock Employee with `employeeId=1`, `roleName="Admin"`, `branchId=1` — bypassing authentication entirely.
**Impact:** Admin-level access without login. Debug leaks production data to a specific user's home directory.
**Fix:** Remove debug file I/O. Remove mock user fallback. Return 401 instead.

---

## HIGH-SEVERITY BREAKAGES (8)

### H1. Forgot-Password DB Update Before Email
**File:** `AuthServlet.java:handleForgotPassword()`
**Issue:** `employeeDAO.updatePasswordByEmail()` is called BEFORE `EmailUtil.sendPasswordEmail()`. If email fails, the password is already changed and unreachable.
**Impact:** Self-inflicted account lockout with no recovery path.
**Fix:** Send email first, then update DB. Or wrap in a transaction that rolls back on email failure.

### H2. Register Sets Invalid Status Case
**File:** `AuthService.java:register()`
**Issue:** Sets `employee.setStatus("active")` (lowercase). V3 CHECK constraint requires `'ACTIVE'`. INSERT will throw SQLException.
**Impact:** Registration fails silently.
**Fix:** Use `"ACTIVE"` to match schema.

### H3. Product Hard Delete Destroys History
**File:** `ProductDAO.java:delete()`
**Issue:** Transactional hard delete removes product, inventory records, stock_transaction entries, AND order_detail entries. Historical sales data is permanently destroyed.
**Impact:** Cannot report on or audit products that were deleted.
**Fix:** Add `status` column to product table. Implement soft delete. Remove cascading delete for stock_transaction/order_detail.

### H4. Checkout RuntimeExceptions Skip Rollback
**File:** `CheckoutServlet.java`
**Issue:** Only `catch (SQLException e)` triggers rollback. `RuntimeException` (NPE, ClassCastException, etc.) bypasses the catch block, leaving `conn.setAutoCommit(false)` with an orphaned connection. The `finally` block closes it without rollback.
**Impact:** Under unexpected errors, DB connection is orphaned (returned to pool with autoCommit=false). No rollback of partial transaction.
**Fix:** Add `catch (Exception e)` with rollback before the `finally` block.

### H5. No Negative Stock Guard in Transfer Dispatch
**File:** `InventoryTicketDAO.java:confirmDispatch()`
**Issue:** `UPDATE inventory SET quantity_in_stock = quantity_in_stock - ?` has no `WHERE quantity_in_stock >= ?` guard.
**Impact:** Stock can go negative during transfer dispatch.
**Fix:** Add `AND quantity_in_stock >= ?` to the UPDATE WHERE clause.

### H6. Connection Leaks in Transactional DAOs
**Files:** `UserManagementDao.java:addEmployee(), updateEmployee()`, `ProfileDao.java:4 methods`
**Issue:** These methods get a `Connection` inside a plain `try {}` block (not try-with-resources) and never close it.
**Impact:** Connection pool depletion, eventual application failure.
**Fix:** Use try-with-resources or add `finally { conn.close() }`.

### H7. OwnerUserServlet Mutations Commented Out
**File:** `OwnerUserServlet.java:146-320`
**Issue:** All mutation code (saveEmployee, updateStatus, resetPassword) is commented out.
**Impact:** Owner cannot manage employees through their dedicated endpoint.
**Fix:** Uncomment and adapt for Owner permissions, or remove the servlet.

### H8. Hardcoded Secrets in Source Code
**Files:** `EmailUtil.java:25-26`, `vnpay/Config.java:17-20`
**Issue:** Gmail app password and VNPAY hash secret hardcoded in plain text.
**Impact:** Anyone with source access has production credentials.
**Fix:** Externalize to environment variables, system properties, or encrypted config.

---

## MEDIUM-SEVERITY BREAKAGES (10)

### M1. CartServlet loads ALL products for single lookup
**File:** `CartServlet.java:handleAdd()`
**Issue:** Fetches all products via `getAllActiveByWarehouse()` then iterates to find the match.
**Impact:** O(n) over full catalog for every add-to-cart.
**Fix:** Use `findById()` or `findByCodebar()` directly.

### M2. No voucher re-validation at checkout
**File:** `CheckoutServlet.java`
**Issue:** Voucher validated at search time (`SalesServlet.checkVoucher`), but NOT re-checked during checkout. An expired/deactivated voucher could still be applied.
**Fix:** Re-validate voucher status and date range inside the checkout transaction.

### M3. Product warehouse hardcoded to ID 1
**File:** `ProductDAO.java:insert(), update()`
**Issue:** Both methods hardcode `warehouse_id = 1`.
**Impact:** Multi-warehouse setups broken. New products only created in warehouse 1.
**Fix:** Use the current user's branch warehouse or add warehouse selection to the product form.

### M4. OrderCode uses millisecond timestamp
**File:** `CheckoutServlet.java:124`
**Issue:** `"HD" + System.currentTimeMillis()` can collide on rapid concurrent orders.
**Fix:** Use `java.util.UUID` short form, DB sequence, or synchronized counter.

### M5. No audit logging for user management
**Files:** `AdminUserServlet.java`, `UserManagementDao.java`
**Issue:** `audit_log` table exists but is never written for user CRUD operations.
**Fix:** Call `ActivityLogService.log()` after every create/update/lock/reset action.

### M6. SalesServlet/OrdersServlet mock employee in production
**Files:** `SalesServlet.java:32-39`, `OrdersServlet.java:41-46`
**Issue:** Creates mock employee with empId=1 when session employee is null.
**Fix:** Return 401/redirect to login instead of using mock.

### M7. Two different session keys for employee
**Files:** `AuthServlet.java` sets both `"currentUser"` and `"employee"`; `ProductSearchServlet.java` reads `"employee"`; `SecurityFilter` checks `"currentUser"`.
**Impact:** Inconsistent access to the Employee object across modules.
**Fix:** Standardize on one key (preferably `"currentUser"`).

### M8. Register emits no duplicate field detail
**File:** `AuthService.java:register()`
**Issue:** Duplicate detection returns `"Email already exists"` but phone could also be the duplicate.
**Fix:** Check both email and phone separately, return the specific field.

### M9. Category parent can be inactive
**File:** `CategoryController.java:addCategory()/updateCategory()`
**Issue:** No check that parent category has status='active'.
**Fix:** Add active parent check.

### M10. Role-selection is non-functional
**File:** `AuthServlet.java`
**Issue:** No role-selection JSP. GET immediately redirects. POST expects non-existent form submission.
**Fix:** Create `role-selection.jsp` or remove the feature.

---

## STUBS / PLACEHOLDERS (9)

| Module | URL | Evidence |
|--------|-----|----------|
| Purchase Order | `/purchase-orders` | Controller stub, static JSP, read-only DAO |
| Payment Invoice | `/payments`, `/invoices` | Controller stub, InvoiceDAO wraps [order] table |
| Dashboard Inventory | `/dashboard/inventory` | JSP forward only, no data |
| Dashboard Financial | `/dashboard/financial` | JSP forward only, no data |
| Reports — Customer Loyal | `/reports/customer-loyal` | JSP forward only |
| Reports — Sales by Store | `/reports/sales-by-store` | JSP forward only |
| Reports — Inventory | `/reports/inventory` | JSP forward only |
| Reports — Export | `/reports/export` | JSP forward only |
| Settings | `/settings` | placeholder.jsp |
| Notifications | `/notifications` | JSP forward, no DAO |
| Configuration | `/configuration/business` | Controller stub |
| About/Contact/SEO | `/about`, `/contact`, `/seo` | Static JSPs, no CMS |
| ReportsServlet | `/reports` | placeholder.jsp |
| Inventory Check | `/inventory` (check action) | "Feature under maintenance" |
| WarehouseController | `/warehouse/*` | Stub forwards, no DAO |
| POS Controller | `/pos/*` | Stub (real work in CartServlet) |

---

## SUMMARY OF BUSINESS FLOW INTEGRITY

| Severity | Count | Description |
|----------|-------|-------------|
| **Critical** | 7 | Data loss, auth bypass, broken core features |
| **High** | 8 | Data integrity, security, missing features |
| **Medium** | 10 | Performance, code quality, edge cases |
| **Stubs** | 15 | Non-functional placeholder endpoints |
| **Total** | **40** | |

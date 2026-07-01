# BUSINESS IMPROVEMENT PLAN

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## Priority Legend

| Priority | Definition | Target Resolution |
|----------|-----------|------------------|
| **P0** | Data loss or security breach | Immediate |
| **P1** | Core feature non-functional | This sprint |
| **P2** | Important feature with workaround | Next sprint |
| **P3** | Quality / tech debt | Backlog |

---

## P0 — CRITICAL (Must Fix Now)

### 1. Fix Password Change in ProfileServlet
- **Issue:** Uses `hash()` instead of `verify()` — permanently broken
- **Files:** `ProfileServlet.java:211-213`
- **Fix:** Replace `PasswordUtil.hash(oldPassword)` with `PasswordUtil.verify(oldPassword, currentHash)`
- **Effort:** 1 line
- **Risk:** None

### 2. Fix User Lock/Unlock Status Values
- **Issue:** `"locked"` fails V3 CHECK constraint `IN ('ACTIVE','INACTIVE')`
- **Files:** `AdminUserServlet.java`, `UserManagementDao.java`
- **Fix:** Use `"INACTIVE"` instead of `"locked"`, `"ACTIVE"` instead of `"active"`
- **Effort:** 2 files, 4 lines
- **Risk:** Low — matches schema

### 3. Fix Register Status Case
- **Issue:** `register()` sets `"active"` (lowercase); schema requires `'ACTIVE'`
- **Files:** `AuthService.java`
- **Fix:** Change `employee.setStatus("active")` to `"ACTIVE"`
- **Effort:** 1 line
- **Risk:** None

### 4. Remove Production Debug File Writes + Auth Bypass
- **Issue:** Writes to hardcoded path; creates mock Admin when session null
- **Files:** `InventoryController.java:395-416`
- **Fix:** Delete debug blocks; return 401 on null session
- **Effort:** 20 lines
- **Risk:** Low — fixes security hole

### 5. Implement Full Refund Transaction
- **Issue:** Refund only changes status; no inventory/points/payment reversal
- **Files:** `OrdersServlet.java`, `OrderDAO.java`, `CustomerPointDAO.java`, `InventoryDAO.java`
- **Fix:** Create transactional refund flow: restore stock → reverse points → void payment → revert voucher → audit log
- **Effort:** 3-4 hours
- **Risk:** Medium — requires careful testing

### 6. Add Catch-Exception Rollback in CheckoutServlet
- **Issue:** Only catches SQLException; RuntimeExceptions skip rollback
- **Files:** `CheckoutServlet.java`
- **Fix:** Add `catch (Exception e)` with rollback
- **Effort:** 3 lines
- **Risk:** Low — closes a transaction integrity gap

### 7. Add Negative Stock Guard in Transfer Dispatch
- **Issue:** `confirmDispatch()` deducts without checking `quantity_in_stock >= ?`
- **Files:** `InventoryTicketDAO.java:confirmDispatch()`
- **Fix:** Add `AND quantity_in_stock >= ?` to UPDATE WHERE clause
- **Effort:** 1 line
- **Risk:** Low

---

## P1 — HIGH (Fix This Sprint)

### 8. Fix Connection Leaks in Transactional DAOs
- **Issues:**
  - `UserManagementDao.addEmployee()` — connection never closed
  - `UserManagementDao.updateEmployee()` — connection never closed
  - `ProfileDao.getProfileById()` — connection never closed
  - `ProfileDao.isEmailExists()` — connection never closed
  - `ProfileDao.getPasswordHash()` — connection never closed
  - `ProfileDao.updatePasswordHash()` — connection never closed
  - `UserManagementDao.rollbackQuietly()` — gets NEW connection (broken)
  - `UserManagementDao.setAutoCommitTrueQuietly()` — gets NEW connection (broken)
- **Fix:** Convert to try-with-resources; fix rollback/auto-commit to use the transaction connection
- **Effort:** 8 files, ~50 lines
- **Risk:** Medium — affects transaction behavior

### 9. Fix Forgot-Password Email Order
- **Issue:** DB updated before email sent; email failure = permanent lockout
- **Files:** `AuthServlet.java:handleForgotPassword()`
- **Fix:** Send email first, then update DB; or add rollback on failure
- **Effort:** 5 lines
- **Risk:** Low

### 10. Create VNPAY Return/IPN Servlets
- **Issue:** No endpoint handles payment callbacks
- **Files:** Create `VnpayReturnServlet.java`, `VnpayIpnServlet.java`
- **Fix:** Implement HMAC-SHA512 verification, order status update (PAID), transaction recording
- **Effort:** 4-6 hours
- **Risk:** Medium — payment code needs thorough testing

### 11. Implement Purchase Order CRUD
- **Issue:** Entire module is stub
- **Files:** `PurchaseOrderController.java`, `PurchaseOrderDAO.java`, `PurchaseDetailDAO.java`, JSPs
- **Fix:** Create PO creation form, DAO insert/update/delete, status transitions, goods receipt → inventory update
- **Effort:** 8-12 hours
- **Risk:** High — significant new code

### 12. Fix Finance DAO Stubs
- **Issues:**
  - Balance methods return 0
  - Filter parameters ignored
  - Payment type not persisted
- **Files:** `PaymentDAO.java`
- **Fix:** Implement real SQL aggregations; apply filter params; add `payment_type` column to INSERT
- **Effort:** 2-3 hours
- **Risk:** Medium

### 13. Externalize Hardcoded Secrets
- **Issues:** Gmail app password + VNPAY hash secret in source
- **Files:** `EmailUtil.java`, `vnpay/Config.java`
- **Fix:** Read from environment variables / system properties
- **Effort:** 1 hour
- **Risk:** Low

---

## P2 — MEDIUM (Next Sprint)

### 14. Add Soft Delete for Product
- **Issue:** Hard delete destroys historical data; no status column on product table
- **Files:** `database/migration_add_product_status.sql`, `ProductDAO.java`, `ProductController.java`
- **Fix:** Migrate: add `status` column; implement soft delete; update all queries
- **Effort:** 3-4 hours

### 15. Remove Mock Employee Fallbacks
- **Issues:** `SalesServlet`, `OrdersServlet`, `ShiftServlet`, `RevenueServlet`
- **Fix:** Return 401/redirect to login instead of creating phantom employee
- **Effort:** 1 hour

### 16. Standardize Session Key to "currentUser"
- **Issue:** Both `"currentUser"` and `"employee"` used; `ProductSearchServlet` uses `"employee"`
- **Fix:** Normalize all reads to `"currentUser"`; remove duplicate `"employee"` set in AuthServlet
- **Effort:** 1 hour

### 17. Add Voucher Re-validation at Checkout
- **Issue:** Session voucher not re-checked against DB for status+dates
- **Files:** `CheckoutServlet.java`
- **Fix:** Query `VoucherDAO.getValidByCode()` inside checkout transaction
- **Effort:** 2-3 lines

### 18. Implement Inventory Count/Check Feature
- **Issue:** "Feature under maintenance" stub
- **Files:** `InventoryController.java`, `InventoryTicketDAO.java`
- **Fix:** Wire createCheck/approveCheck with actual stock comparison
- **Effort:** 4-5 hours

### 19. Add Product Barcode/SKU Input to Management Form
- **Issue:** `buildProductFromRequest()` never sets barcode
- **Files:** `ProductController.java`, `product form JSP`
- **Fix:** Add barcode field to form + controller mapping
- **Effort:** 1 hour

### 20. Make Warehouse Configurable (Not Hardcoded to ID 1)
- **Issue:** ProductDAO hardcodes warehouse ID 1 for insert/update inventory
- **Files:** `ProductDAO.java`
- **Fix:** Use user's branch warehouse or add warehouse dropdown
- **Effort:** 2 hours

### 21. Implement Dashboard Inventory and Financial Pages
- **Issue:** Stub forwards with no data
- **Files:** `DashboardController.java`, new JSPs or reuse existing
- **Effort:** 3-4 hours

### 22. Implement Missing Report Endpoints
- **Issues:** customer-loyal, sales-by-store, inventory, export are all stubs
- **Files:** `ReportController.java`, new DAO queries
- **Effort:** 5-6 hours

### 23. Add Audit Logging for User Management
- **Issue:** No audit trail for create/update/lock/reset operations
- **Files:** `AdminUserServlet.java`
- **Fix:** Call `ActivityLogService.log()` after each action
- **Effort:** 30 minutes

---

## P3 — LOW (Backlog / Tech Debt)

### 24. Refactor getRedirectPath() and handleRoleSelection()
- **Issue:** Two different role mapping systems; getRedirectPath() default falls through
- **Effort:** 1 hour

### 25. Replace System.currentTimeMillis() with UUID for Codes
- **Issues:** Order code, ticket code collision risk
- **Effort:** 30 minutes

### 26. Replace java.util.Random with SecureRandom in EmailUtil
- **Issue:** Predictable password generation
- **Effort:** 1 line

### 27. Add SQL Wildcard Escaping to Keyword Searches
- **Issue:** User can inject `%` and `_` in LIKE patterns
- **Effort:** 30 minutes per module

### 28. Add Server-Side Validation for Email and Phone Formats
- **Issues:** No format validation anywhere
- **Effort:** 2 hours

### 29. Add Price Validation (sellingPrice > 0, sellingPrice >= importPrice)
- **Effort:** 30 minutes

### 30. Make VAT Configurable (Remove Hardcoded 8%)
- **Effort:** 2 hours (table + UI + service)

### 31. Add Category Active-Parent Validation
- **Effort:** 5 lines

### 32. Clean Up Dead Code
- **Items:**
  - Service layer (throws UnsupportedOperationException)
  - WarehouseController (stub)
  - Multiple getWarehouseId() duplications
  - resolvePageSize() duplicated in 4 servlets
  - parseInt/trim/isBlank/parseInt/parseDate utility duplicate methods
  - Commented-out multi-role code in UserManagementDao
- **Effort:** 4-5 hours

### 33. Add Transaction to saveImport Flow
- **Issue:** Ticket + stock + transaction inserts use separate connections
- **Effort:** 2 hours

### 34. Add Product Supplier Assignment to Product Form
- **Issue:** supplierIDs never captured in management
- **Effort:** 1 hour

### 35. Add Branch Authorization Check to BranchController
- **Issue:** Any authenticated user can manage branches
- **Effort:** 30 minutes

---

## EFFORT ESTIMATE SUMMARY

| Priority | Items | Est. Effort | Risk |
|----------|-------|-------------|------|
| **P0** | 7 | 1-2 days | Low-Medium |
| **P1** | 6 | 3-5 days | Medium |
| **P2** | 10 | 5-7 days | Low-Medium |
| **P3** | 12 | 3-4 days | Low |
| **Total** | **35** | **12-18 days** | |

## QUICK WINS (Can be done in under 30 minutes)

| # | Fix | Effort |
|---|-----|--------|
| 1 | Fix register status case | 1 line |
| 2 | Fix lock/unlock status values | 4 lines |
| 3 | Fix password change verify | 1 line |
| 4 | Add negative stock guard in dispatch | 1 line |
| 5 | Add Exception catch in CheckoutServlet rollback | 3 lines |
| 6 | Add voucher re-validation at checkout | 2 lines |
| 7 | Replace java.util.Random with SecureRandom | 1 line |
| 8 | Add category active-parent validation | 5 lines |
| 9 | Standardize session key | 5 lines |
| 10 | Add audit logging for user management | 10 lines |

## RESTORATION MILESTONE PLAN

```
Week 1: P0 fixes (7 critical bugs)
  → Application becomes stable and secure
  → No data loss scenarios remain
  → Basic auth/Core features work correctly

Week 2: P1 fixes + stub implementations
  → Connection leaks fixed
  → VNPAY callbacks functional
  → Purchase Order CRUD implemented
  → Finance DAO stubs fixed

Week 3: P2 feature completion
  → Product soft delete
  → Inventory count feature
  → Missing reports implemented
  → Dashboard pages functional

Week 4: P3 cleanup
  → Dead code removal
  → Config extraction
  → Validation hardening
  → Tech debt reduction
```

# BUSINESS FLOW REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01
**Phase:** Phase 3 — Business Logic & Module Validation

---

## 1. AUTHENTICATION FLOW (Complete)

```
POST /login → AuthServlet.handleLogin()
  ├── Validate username/password non-empty
  ├── AuthService.login() → EmployeeDAO.findByEmailOrPhone()
  │     ├── Employee not found → "Email/số điện thoại không tồn tại"
  │     ├── Status != "ACTIVE" → "Tài khoản đã bị khóa"
  │     ├── PasswordUtil.verify() fails → incrementFailedAttempts() → lock at 5
  │     └── Success → resetFailedAttempts() → return Employee
  ├── Cookie "remembered_username" (30 days)
  ├── Session regeneration (fixation protection) + CSRF token
  ├── session attributes: currentUser, employee, csrfToken
  └── Redirect via getRedirectPath(employee)

GET /logout → invalidate session → redirect /login
POST /forgot-password → EmployeeDAO.checkFullNameAndEmailMatch()
  → EmailUtil.generateRandomPassword() → hash → update DB → send email

Status: ✅ WORKS. Bug: register() sets "active" (lowercase) — CHECK constraint requires 'ACTIVE'.
Status: ✅ Session fixation fixed in Phase 2.
```

## 2. USER/EMPLOYEE MANAGEMENT FLOW (Partial)

```
AdminUserServlet (/admin/user)
  ├── action=create: validate → isEmailExists → gen password → hash → send email → addEmployee()
  ├── action=update: validate → isEmailExists(exclude self) → updateEmployee()
  ├── action=lock/unlock: updateEmployeeStatus(status="locked"/"active")
  ├── action=resetPassword: gen password → hash → update → send email
  └── action=list: paginated query with keyword/branch/role/status filters

OwnerUserServlet (/owner/emp) → VIEW-ONLY (all mutation code commented out)
ManagerEmployeeServlet (/manager/emp) → VIEW-ONLY (branch-scoped)
ProfileServlet (/profile) → update profile, change password
```

**Status:** ⚠️ BROKEN. `status="locked"` fails V3 CHECK constraint (`'ACTIVE','INACTIVE'`). Password change uses `hash()` instead of `verify()` — permanently broken. Connection leaks in 6 DAO methods. Owner cannot manage employees.

## 3. CUSTOMER MANAGEMENT FLOW (Complete)

```
CustomerController (/customers)
  ├── action=create/create-api: validate name+phone → isEmailOrPhoneExists() → insert()
  ├── action=update/update-api: role-gated fields → update()
  ├── action=delete: soft delete (status='INACTIVE')
  ├── action=redeem-points: validate balance → CustomerDAO.redeemPoints()
  ├── action=sync-loyalty: recalculate from paid orders
  └── action=list: paginated with keyword/status filters, overview stats
```

**Status:** ✅ WORKS. Full CRUD, soft delete, points redemption, loyalty sync, activity logging.

## 4. BRANCH MANAGEMENT FLOW (Partial)

```
BranchController (/branch)
  ├── action=insert: BranchValidator (code format, duplicate, image) → dao.insert()
  ├── action=update: validate → dao.update()
  ├── action=delete: HARD DELETE (not soft)
  └── action=list: paginated with city/status/keyword filters, KPIs
```

**Status:** ⚠️ Hard delete breaks FK constraints. No authorization check. No activity logging.

## 5. PRODUCT & CATEGORY FLOW (Partial)

```
ProductController (/products)
  ├── action=add: buildProductFromRequest() → verifyImage() → insert() + inventory(warehouse 1)
  ├── action=edit: update() + UPSERT inventory(warehouse 1)
  ├── action=delete: HARD DELETE (cascades: inventory→stock_tx→order_detail→product)
  └── action=list: paginated with keyword/category/unit filters

CategoryController (/category)
  ├── action=add: validate name uniqueness → addCategory()
  ├── action=update: self-reference + circular prevention → updateCategory()
  ├── action=delete: FK-dependent DELETE
  └── action=list: paginated with keyword/status, product count
```

**Status:** ⚠️ Product hard delete destroys history. No soft delete. Warehouse hardcoded to ID 1. No barcode/SKU input. Service layer dead code.

## 6. PURCHASE ORDER FLOW (STUB)

```
PurchaseOrderController (/purchase-orders) → STUB
  └── doGet(): forwards to JSP with NO data
  └── doPost(): placeholder message only

SupplierServlet (/suppliers) → COMPLETE
  ├── CRUD with duplicate prevention
  └── manage-products: supplier_product association with batch save

PurchaseOrderDAO → READ-ONLY (findAll, findById only, no insert/update/delete)
PurchaseDetailDAO → READ-ONLY (findByOrderId only)
```

**Status:** ❌ BROKEN. No PO creation, approval, goods receipt, or inventory update. Complete stub.

## 7. SALES / ORDER FLOW (Partial)

```
CartServlet (/cart)
  ├── newTab / switchTab / add / update / remove / selectCustomer
  ├── applyVoucher / hold / clear
  └── Multi-tab session-based cart management

CheckoutServlet (/checkout) → CRITICAL TRANSACTION
  ├── Validate: auth, tab, cart, payment, cash >= total
  ├── DB TRANSACTION: stock check → create order → order_detail → payment → deduct inventory
  │   → voucher usage → customer points → update status COMPLETED → COMMIT
  └── On SQLException: ROLLBACK

OrdersServlet (/orders, /orders/detail, /orders/refund)
  ├── List orders, detail JSON
  └── Refund: status→CANCELLED + audit_log ONLY (NO inventory restore, NO point reversal)

SalesServlet (/sales): product/customer/voucher search, customer creation
PosController (/pos/sale): stub (real work done by CartServlet+CheckoutServlet)
```

**Status:** ⚠️ Checkout transaction is well-structured but RuntimeExceptions skip rollback. Refund is incomplete (no inventory/points/payment reversal).

## 8. INVENTORY FLOW (Partial)

```
InventoryController (/inventory)
  ├── stock tab: paginated listing with filters, KPIs
  ├── transfer tab: create TRANSFER_REQUEST (PENDING) → confirmExport (IN_TRANSIT)
  │   → confirmDispatch (sender decrements) → confirmReceiveWithDiscrepancy (receiver increments)
  ├── import tab: saveImport (ticket + increaseStock + transaction insert — NO CROSS-TRANSACTION)
  ├── check tab: STUB ("Feature under maintenance")
  └── history tab: completed/rejected transfers

WarehouseController (/warehouse) → STUB (forwards to JSP, no DAO)
```

**Status:** ⚠️ saveImport has no cross-DAO transaction. confirmDispatch has no negative stock guard. Check/Count feature stubbed. Debug file writes + mock user bypass in production code.

## 9. FINANCE FLOW (Stub/Broken)

```
IncomeExpenseController (/cashbook)
  ├── showCashbook: paginated transactions, fund balances, weekly chart
  └── doPost: create receipt/payment

PaymentInvoiceController (/payments, /invoices) → STUB
InvoiceDAO → wraps [order] table (no dedicated invoice table)
```

**Status:** ❌ BROKEN. `getTotalCashBalance()`, `getBankBalance()`, `getSumIncome()`, `getSumExpense()` all return 0.0. Filter parameters type/paymentMethod ignored. Payment type (INCOME/EXPENSE) not persisted.

## 10. SHIFT MANAGEMENT FLOW (Complete)

```
ShiftServlet (/shift)
  ├── handleOpenShift: check no open shift → INSERT shift (OPEN)
  ├── handleCloseShift: get active shift → calculate expected → UPDATE shift (CLOSED)
  └── doGet: show active shift + cash txns, or last 10 shifts hist

CashTransactionServlet (/shift/cash)
  └── doPost: validate type/amount → insert cash_transaction → recalculate expected

ShiftDAO: expected_cash = opening_cash + cash_sales - withdraw + deposit
```

**Status:** ✅ WORKS. Hardcoded employee fallback bypasses auth. Race condition in multi-query expected_cash calc.

## 11. REVENUE / DASHBOARD / REPORTS FLOW

```
RevenueServlet (/revenue): KPIs, hourly chart, payment breakdown, top products, recent txns
DashboardController: Owner KPIs (20+ queries), inventory/financial stubs
ReportController: employee-sales (paginated + PDF export), other reports stubs
ActivityLogController: Owner-only, read-only, filtered, paginated audit log
```

**Status:** ⚠️ Revenue works. Dashboard inventory/financial stubs. Reports (customer-loyal, sales-by-store, inventory, export) all stubs.

## 12. VNPAY PAYMENT FLOW (Incomplete)

```
Config.java: HMAC-SHA512 signing, vnp_Returnurl="http://localhost:8080/vnpay_return"
             vnp_IpnUrl="http://localhost:8080/vnpay_ipn"
             vnp_TmnCode + vnp_HashSecret hardcoded
```

**Status:** ❌ No servlet for `/vnpay_return` or `/vnpay_ipn`. Payment flow cannot complete.

## 13. PROFILE FLOW

```
ProfileServlet (/profile)
  ├── action=updateProfile: fullName/email required, avatar upload (5MB, JPG/PNG/WEBP)
  └── action=changePassword: ⚠️ BROKEN (hash() instead of verify())
```

**Status:** ⚠️ Password change permanently broken.

## 14. SUPPLIER FLOW (Complete)

```
SupplierServlet (/suppliers): Full CRUD with duplicate prevention, product linkage
SupplierProductDAO: batch save with transaction (delete-all + insert-all)
```

**Status:** ✅ WORKS.

## 15. SEARCH, FILTER, PAGINATION

| Module | Search | Filter | Pagination | Status |
|--------|--------|--------|------------|--------|
| Employee | keyword (name/email/phone) | branch, role, status | OFFSET/FETCH | ✅ |
| Customer | keyword (phone/name/email) | branch, status | OFFSET/FETCH | ✅ |
| Product | keyword (name) | category, unit | OFFSET/FETCH 5/page | ⚠️ |
| Category | keyword (name) | status | OFFSET/FETCH 10/page | ✅ |
| Supplier | keyword (name/phone/address) | status | OFFSET/FETCH | ✅ |
| Branch | keyword | city, status | OFFSET/FETCH | ✅ |
| Inventory | keyword (product) | status, category, unit, warehouse | OFFSET/FETCH 20/page | ✅ |
| Stock Tx | type filter | date (today) | OFFSET/FETCH | ⚠️ |
| Purchase Order | — | — | — | ❌ stub |
| Finance | keyword | type, paymentMethod, timeRange | OFFSET/FETCH | ⚠️ ignored |
| Activity Log | keyword, tableName, actionName | date range | OFFSET/FETCH 10/page | ✅ |
| Employee Sales Report | keyword | branch, date range | OFFSET/FETCH | ✅ |
| Revenue | — | date, employee | no pagination | ✅ |
| Orders | keyword (order_code, customer) | — | no pagination | ⚠️ |

---

## SYSTEM WORKFLOW ARCHITECTURE

```
                   ┌──────────────┐
                   │  SecurityFilter  │  (auth, role, CSRF, headers)
                   └──────┬───────┘
                          │
            ┌─────────────┼──────────────┐
            ▼             ▼              ▼
    ┌────────────┐ ┌───────────┐ ┌──────────────┐
    │ Controllers │ │ Services  │ │    DAOs      │
    │ (32 servlets)│ │ (12 classes)│ │ (36 classes)  │
    └──────┬─────┘ └─────┬─────┘ └──────┬───────┘
           │             │              │
           │        (mostly dead        │
           │         code / pass-       │
           │         through)           │
           └─────────────┼──────────────┘
                         ▼
                   ┌─────────────┐
                   │  DBContext  │
                   │  (JDBC raw) │
                   └──────┬──────┘
                          ▼
                   ┌─────────────┐
                   │  SQL Server │
                   │  DBFinoraV3 │
                   └─────────────┘
```

**Key architectural issue:** Service layer is dead code (throws `UnsupportedOperationException`). All controllers call DAOs directly. No transaction management at the service layer.

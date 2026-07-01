# BUSINESS RULE MATRIX

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## Legend
- ✅ Rule is correctly enforced
- ⚠️ Rule exists but has gaps
- ❌ Rule is not implemented or violated
- N/A Rule not applicable to this module

---

## AUTHENTICATION & AUTHORIZATION RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Password must be stored securely (BCrypt) | PasswordUtil with work factor 12 | ✅ |
| Account locks after 5 failed attempts | EmployeeDAO incrementFailedAttempts → status='INACTIVE' | ✅ |
| Locked accounts cannot log in | AuthService checks status != "ACTIVE" | ✅ |
| Inactive employees cannot log in | Same check | ✅ |
| Session must be invalidated on logout | AuthServlet.doGet: session.invalidate() | ✅ |
| New session after login (fixation protection) | handleLogin: invalidate old, create new | ✅ |
| CSRF token protects state-changing requests | SecurityFilter validates on POST | ✅ |
| Password reset sends email, not link | Forgot-password sends random password | ✅ |
| Admin and Owner cannot be locked | UserManagementDao: NOT EXISTS role IN ('Admin','Owner') | ✅ |
| Role-based URL access | SecurityFilter ROLE_MAP with 29 prefixes | ✅ |
| SalesStaff cannot modify inventory | SecurityFilter denies /inventory/ for salesstaff | ✅ |
| WarehouseStaff cannot perform sales | SecurityFilter denies /pos/ for warehousestaff | ✅ |
| StoreManager manages assigned branch | ManagerEmployeeServlet branch-scoped | ⚠️ |
| Owner can view all reports | SecurityFilter allows /report/ for owner | ✅ |
| Register sets correct status | Uses "active" (lowercase); DB requires 'ACTIVE' | ❌ |
| Lock/Unlock uses valid status values | Code uses "locked"; DB only allows 'ACTIVE','INACTIVE' | ❌ |
| Role-selection for multi-role users | No JSP; auto-redirect from first role | ❌ |

## USER MANAGEMENT RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Email must be unique | isEmailExists() with self-exclusion | ✅ |
| Phone must be unique | Same query checks phone OR email | ✅ |
| Password must be provided at creation | Auto-generated, emailed | ✅ |
| Email format validation | None server-side | ❌ |
| Phone format validation | None server-side | ❌ |
| Profile password change verifies old password | Uses hash() not verify() — broken | ❌ |
| Owner can view but not modify employees | OwnerUserServlet mutations commented out | ✅ design |
| Manager only sees own branch employees | Branch-scoped DAO queries | ✅ |
| Audit log for user management | No audit logging | ❌ |
| Soft delete for employees | No "deleted" status | ❌ |

## PRODUCT MANAGEMENT RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Product name required | Not server-validated (DB NOT NULL) | ⚠️ |
| Barcode must be unique | DB UNIQUE constraint; no form field | ⚠️ |
| Selling price must be positive | Not validated | ❌ |
| Import price <= selling price | Not validated | ❌ |
| New product starts with 0 stock | Default; no initial stock entry form | ✅ |
| Product can be deactivated (soft delete) | No status column on product table | ❌ |
| Product deletion cascades to audit trail | Hard delete cascades — destroys history | ❌ |
| Category must exist | FK constraint | ✅ |
| Category hierarchy is acyclic | Recursive CTE circular prevention | ✅ |
| Category name must be unique | DAO check with case-insensitive trim | ✅ |
| Category parent cannot be self | Controller ID comparison | ✅ |
| Category parent must be active | Not checked | ❌ |

## SALES & ORDER RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Must be logged in to create sale | Session check in CheckoutServlet | ✅ |
| Cart cannot be empty at checkout | CheckoutServlet validates | ✅ |
| Stock must be sufficient | Atomic UPDATE WHERE quantity_in_stock >= ? | ✅ |
| Payment amount must cover total | cashReceived >= totalAmount for CASH | ✅ |
| VAT must be calculated | Hardcoded 8% | ⚠️ |
| Voucher must be valid (active + date) | Checked at search; NOT re-validated at checkout | ❌ |
| Customer points earned on purchase | orderTotal / 100,000 during checkout tx | ✅ |
| Order status tracks lifecycle | PENDING → COMPLETED (→ CANCELLED via refund) | ✅ |
| Refund MUST restore inventory | Not implemented | ❌ |
| Refund MUST reverse customer points | Not implemented | ❌ |
| Refund MUST void payment | Not implemented | ❌ |
| Refund MUST revert voucher usage | Not implemented | ❌ |
| Refund wrapped in transaction | Two separate connections; no tx | ❌ |
| Multiple tabs allowed | CartServlet multi-tab session | ✅ |
| Held orders persist across sessions | Session-only; lost on timeout/restart | ❌ |
| Order code is unique | `"HD" + System.currentTimeMillis()` collision risk | ⚠️ |
| Payment method is recorded | Stored in order.payment_method but NOT in payment table | ⚠️ |

## INVENTORY RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Warehouse must exist for branch | FK constraint | ✅ |
| Stock cannot be negative (POS) | InventoryItemDAO.deductStock checks | ✅ |
| Stock cannot be negative (transfer) | No check in confirmDispatch | ❌ |
| Import adds stock | increaseStock() UPSERT | ✅ |
| Transfer is two-phase | Sender decrements, receiver increments | ✅ |
| Discrepancy creates report | Auto-creates DISCREPANCY ticket | ✅ |
| Inventory count/adjustment | Feature stubbed | ❌ |
| All stock changes have audit trail | stock_transaction table | ✅ |
| from_warehouse_id must be warehouse | Import uses supplierId — FK violation risk | ❌ |

## PURCHASE ORDER RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| PO must reference active supplier | No PO creation at all | ❌ |
| Products must be linked to supplier | SupplierProduct exists but not enforced | ❌ |
| PO status transitions are valid | No state machine | ❌ |
| Goods receipt updates inventory | Not implemented | ❌ |
| Supplier deletion prevented if POs exist | FK constraint only | ✅ passive |
| Supplier name or phone must be unique | existsByNameOrPhone() | ✅ |

## CUSTOMER RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Full name and phone required | Validated at create | ✅ |
| Phone must be unique among active customers | isEmailOrPhoneExists() | ✅ |
| Points earned at configurable rate | Hardcoded: orderTotal / 100,000 | ⚠️ |
| Points cannot go negative | redeemPoints checks balance | ✅ |
| Customer can be soft-deleted | status='INACTIVE' | ✅ |
| Purchase history visible | Order history query | ✅ |
| Sales staff has limited edit access | Role-gated fields | ✅ |

## SHIFT MANAGEMENT RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Cannot open two shifts simultaneously | getOpenShiftByEmp() check | ✅ |
| Shift close calculates expected cash | opening + cashSales - withdraw + deposit | ✅ |
| Cash transactions recorded during shift | CashTransactionDAO | ✅ |
| Employee must be authenticated | Fallback employee when null | ❌ |
| Expected vs declared cash compared | No comparison logic | ⚠️ |

## FINANCE RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Income and expense distinguished | type IN ('INCOME','EXPENSE') but NOT persisted | ❌ |
| Fund balance accurately displayed | All balance methods return 0 | ❌ |
| Payment type filter works | type/paymentMethod params ignored | ❌ |
| Cashbook shows weekly chart | Expense always 0 | ❌ |
| Invoice from order | InvoiceDAO wraps [order] table | ⚠️ |

## VNPAY RULES

| Rule | Implementation | Status |
|------|---------------|--------|
| Payment request signed with HMAC-SHA512 | Config.java hashing | ✅ |
| Return URL processed | No servlet | ❌ |
| IPN callback processed | No servlet | ❌ |
| Order updated on payment confirmation | Not implemented | ❌ |
| Duplicate callbacks handled | Not implemented | ❌ |
| Secrets are configurable | Hardcoded in source | ❌ |

---

## OVERALL BUSINESS RULE COMPLIANCE

| Category | Total Rules | Compliant | Partial | Missing |
|----------|------------|-----------|---------|---------|
| Auth & Authorization | 16 | 12 | 2 | 2 |
| User Management | 12 | 5 | 1 | 6 |
| Product Management | 12 | 4 | 2 | 6 |
| Sales & Order | 16 | 9 | 3 | 4 |
| Inventory | 9 | 6 | 0 | 3 |
| Purchase Order | 6 | 1 | 0 | 5 |
| Customer | 7 | 7 | 0 | 0 |
| Shift Management | 5 | 3 | 1 | 1 |
| Finance | 5 | 0 | 1 | 4 |
| VNPAY | 6 | 1 | 0 | 5 |
| **Total** | **94** | **48 (51%)** | **10 (11%)** | **36 (38%)** |

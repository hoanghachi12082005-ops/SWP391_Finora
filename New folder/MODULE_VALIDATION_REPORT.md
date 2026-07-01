# MODULE VALIDATION REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## Validation Key
- ✅ Complete — tested and verified
- ⚠️ Partial — works but has issues
- ❌ Broken — non-functional or missing

---

## 1. AUTHENTICATION MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Login flow | ✅ | BCrypt, session, lockout at 5 failures |
| Logout flow | ✅ | Session invalidation, redirect |
| Forgot password | ✅ | Email verification, random password |
| Role selection | ⚠️ | No role-selection JSP; auto-redirect only |
| Session fixation | ✅ | Fixed in Phase 2 |
| CSRF token | ✅ | Generated on login + first GET, validated on POST |
| Register flow | ⚠️ | `status="active"` lowercase fails V3 CHECK constraint |
| Password change | ❌ | `hash()` used instead of `verify()` — permanently broken |
| Account lockout | ✅ | 5 failures → INACTIVE |
| Email credentials hardcoded | ❌ | Gmail app password in source |
| **Overall** | **⚠️ Partial** | 1 critical, 1 broken |

## 2. USER / EMPLOYEE MANAGEMENT MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Create user | ✅ | With email notification, BCrypt |
| Edit user | ✅ | Name/email/phone/role/branch/status |
| Deactivate user | ❌ | `status="locked"` violates CHECK constraint `IN ('ACTIVE','INACTIVE')` |
| Activate user | ⚠️ | `status="active"` works (case-insensitive DB) |
| Delete user | ❌ | No soft delete; only status="INACTIVE" |
| Restore user | ❌ | Not implemented |
| Search | ✅ | Keyword on name/email/phone |
| Filter | ✅ | Branch, role, status |
| Pagination | ✅ | OFFSET/FETCH, custom page sizes |
| Role assignment | ✅ | Single role_id |
| Branch assignment | ✅ | FK to Branch |
| Duplicate email/phone | ✅ | isEmailExists() with self-exclusion |
| Password reset | ⚠️ | Email sent AFTER DB update |
| Owner CRUD | ❌ | OwnerUserServlet has all mutation commented out |
| Connection leaks | ❌ | 6 methods never close connections |
| **Overall** | **⚠️ Partial** | 4 critical bugs |

## 3. EMPLOYEE MANAGEMENT MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Employee creation | ✅ | Admin only |
| Employee update | ✅ | |
| Role assignment | ✅ | |
| Branch assignment | ✅ | |
| Password reset | ⚠️ | Email after DB update |
| Status update | ❌ | "locked" fails CHECK |
| Soft delete | ❌ | Not implemented |
| Restore | ❌ | Not implemented |
| Employee search | ✅ | |
| Employee filtering | ✅ | |
| Pagination | ✅ | |
| Duplicate prevention | ✅ | |
| **Overall** | **⚠️ Partial** | |

## 4. CUSTOMER MANAGEMENT MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Customer CRUD | ✅ | Full create/read/update/soft-delete |
| Phone uniqueness | ✅ | |
| Email uniqueness | ✅ | |
| Customer points | ✅ | Earn, redeem, sync from paid orders |
| Customer ranking | ✅ | Top customer by points |
| Purchase history | ✅ | Order history with branch info |
| Soft delete | ✅ | status='INACTIVE' |
| Restore | ✅ | Admin can set active |
| Search | ✅ | priority: exact phone > like phone > like name > like email |
| Filter | ✅ | Branch filter |
| Role-based edit | ✅ | Sales staff limited to phone+email |
| **Overall** | **✅ Complete** | Best implemented module |

## 5. PRODUCT MANAGEMENT MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Category assignment | ✅ | FK to category |
| Supplier assignment | ⚠️ | `buildProductFromRequest()` never sets supplierIDs |
| SKU uniqueness | ❌ | Barcode not captured in management form |
| Barcode uniqueness | ❌ | Not validated at form level (DB has UNIQUE) |
| Price validation | ❌ | No check for sellingPrice > 0 or > importPrice |
| Cost validation | ❌ | Not checked |
| Stock validation | ❌ | quantity ignored on create (always 0) |
| Image upload | ✅ | 3MB max, ImageIO validation |
| Soft delete | ❌ | Hard delete cascades to inventory/stock_tx/order_detail |
| Restore | ❌ | Not possible (hard delete) |
| Search | ✅ | keyword on name |
| Filter | ✅ | category, unit |
| Pagination | ⚠️ | Hardcoded 5 per page (very small) |
| Warehouse hardcoded | ❌ | Always warehouse ID 1 |
| Service layer | ❌ | Throws UnsupportedOperationException |
| **Overall** | **⚠️ Partial** | 5 critical issues |

## 6. CATEGORY MANAGEMENT MODULE

| Check | Result | Notes |
|-------|--------|-------|
| CRUD | ✅ | |
| Hierarchy | ✅ | parent_category_id with circular prevention |
| Name uniqueness | ✅ | Case-insensitive, trimmed |
| Self-reference prevention | ✅ | |
| Circular hierarchy prevention | ✅ | Recursive CTE isDescendant() |
| Soft delete | ❌ | Hard DELETE (FK-protected) |
| Restore | ❌ | Not implemented |
| Search | ✅ | keyword on name |
| Filter | ✅ | status |
| Pagination | ✅ | |
| **Overall** | **✅ Complete** | |

## 7. PURCHASE ORDER MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Create PO | ❌ | Controller stub; no DAO insert |
| Read PO | ❌ | JSP is static mockup; DAO read-only |
| Update PO | ❌ | Not implemented |
| Delete PO | ❌ | Not implemented |
| Approve PO | ❌ | No status transition logic |
| Cancel PO | ❌ | No status transition logic |
| Receive goods | ❌ | No goods receipt flow |
| Supplier linkage | ✅ | SupplierProduct table + management UI |
| Inventory update | ❌ | No integration between PO and inventory |
| Status transitions | ❌ | No state machine |
| **Overall** | **❌ Broken** | Entire module is stub |

## 8. SUPPLIER MODULE

| Check | Result | Notes |
|-------|--------|-------|
| CRUD | ✅ | Full create/read/update/delete |
| Duplicate prevention | ✅ | name (case-insensitive) or phone |
| Product linkage | ✅ | SupplierProduct with batch transaction |
| Pagination | ✅ | |
| Search | ✅ | keyword on name/phone/address |
| Status filter | ✅ | Active/Inactive |
| **Overall** | **✅ Complete** | |

## 9. SALES / ORDER MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Create sale | ✅ | Multi-tab cart → checkout transaction |
| Add product | ✅ | Stock-checked |
| Remove product | ✅ | |
| Update quantity | ✅ | Stock-checked |
| Discount | ⚠️ | Via voucher only; no manual discount |
| Tax | ⚠️ | VAT hardcoded 8% |
| Voucher | ✅ | Validated at search; needs re-check at checkout |
| Customer points | ✅ | Earned during checkout transaction |
| Payment | ⚠️ | Cash (full) or Bank Transfer (no confirmation) |
| Invoice | ✅ | Order record serves as invoice |
| Inventory deduction | ✅ | Atomic WHERE quantity_in_stock >= ? |
| Refund | ❌ | Only status change; no inventory/points/payment reversal |
| Partial refund | ❌ | Not supported |
| Transaction rollback | ⚠️ | Only catches SQLException; NPE skips rollback |
| **Overall** | **⚠️ Partial** | 2 critical issues |

## 10. INVENTORY MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Stock in (import) | ⚠️ | No cross-DAO transaction |
| Stock out (dispatch) | ⚠️ | No negative stock guard |
| Inventory adjustment | ❌ | Check/Count feature stubbed |
| Current quantity | ✅ | inventory.quantity_in_stock |
| Negative stock prevention | ⚠️ | POS checkout has it; confirmDispatch does not |
| Branch inventory | ✅ | Per-warehouse tracking |
| Inventory history | ✅ | stock_transaction table |
| Transfer 2-phase | ✅ | sender dispatch + receiver confirm |
| Discrepancy handling | ✅ | Auto-creates DISCREPANCY ticket |
| Debug file writes in prod | ❌ | Hardcoded path C:\Users\...\debug.txt |
| Mock user bypass | ❌ | Creates Admin when session null |
| **Overall** | **⚠️ Partial** | 3 critical issues |

## 11. LOYALTY POINT MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Earn points | ✅ | At checkout (orderTotal / 100,000) |
| Redeem points | ✅ | Balance check + transactional |
| Adjust points | ✅ | Admin can edit via customer update |
| Expiration | ❌ | No point expiration logic |
| Ranking | ✅ | CustomerOverview has top customer |
| History | ✅ | point_transaction table |
| Sync from paid orders | ✅ | Recalculate from historical orders |
| **Overall** | **✅ Complete** | |

## 12. FINANCE / CASHBOOK MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Income entry | ⚠️ | PaymentDAO stubs break fund balance display |
| Expense entry | ⚠️ | Same stub issue |
| Payment type filter | ❌ | type and paymentMethod params ignored |
| Payment type persistence | ❌ | INCOME/EXPENSE not stored in DB column |
| Fund balance | ❌ | Always shows 0 |
| Weekly chart | ❌ | Expense always 0 |
| Payment invoice | ❌ | Stub controller |
| **Overall** | **❌ Broken** | 5 critical issues |

## 13. SHIFT MANAGEMENMT MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Open shift | ✅ | |
| Close shift | ✅ | expected cash calculated |
| Cash transaction | ✅ | WITHDRAW/DEPOSIT |
| Cash reconciliation | ⚠️ | Race condition in multi-query calc |
| Hardcoded employee | ❌ | Fallback employee if session null |
| **Overall** | **⚠️ Partial** | |

## 14. DASHBOARD MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Owner KPIs | ✅ | 20+ queries: revenue, orders, customers, inventory, stores |
| Inventory dashboard | ❌ | Stub forward to JSP |
| Financial dashboard | ❌ | Stub forward to JSP |
| **Overall** | **⚠️ Partial** | |

## 15. REPORTS MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Employee sales report | ✅ | Paginated list, full preview, PDF export |
| Customer loyalty report | ❌ | Stub JSP |
| Sales by store report | ❌ | Stub JSP |
| Inventory report | ❌ | Stub JSP |
| Export | ❌ | Stub JSP |
| **Overall** | **⚠️ Partial** | |

## 16. VNPAY PAYMENT MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Payment request | ⚠️ | Config.java exists with HMAC-SHA512 |
| Return URL handling | ❌ | No servlet for /vnpay_return |
| IPN callback | ❌ | No servlet for /vnpay_ipn |
| Payment verification | ❌ | Not implemented |
| Order update | ❌ | Not implemented |
| Transaction logging | ❌ | Not implemented |
| Duplicate callback | ❌ | Not implemented |
| Secrets hardcoded | ❌ | vnp_HashSecret in source |
| **Overall** | **❌ Broken** | Payment flow cannot complete |

## 17. SEARCH MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Keyword search | ✅ | In all implemented modules |
| Case insensitive | ✅ | SQL Server default, or LOWER() |
| Trim spaces | ✅ | trim() in most places |
| Partial matching | ✅ | LIKE %keyword% |
| Special characters | ⚠️ | No SQL wildcard escaping (% and _ not escaped) |
| **Overall** | **✅ Complete** | |

## 18. SOFT DELETE MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Customer | ✅ | status='INACTIVE' |
| Employee | ❌ | No soft delete; "locked" fails CHECK |
| Product | ❌ | Hard delete (cascading) |
| Category | ❌ | Hard DELETE (FK-protected) |
| Branch | ❌ | Hard DELETE |
| Supplier | ❌ | Hard DELETE |
| Purchase order | N/A | Module is stub |
| **Overall** | **❌ Partial** | Only customer has soft delete |

## 19. PAGINATION MODULE

| Check | Result | Notes |
|-------|--------|-------|
| Page size config | ✅ | 5/10 or percentage (30%/50%) |
| Navigation | ✅ | Previous/Next with page clamping |
| Empty page | ⚠️ | Clamps to page 1, but no explicit empty-message |
| Last page | ✅ | Clamped to totalPages |
| Sorting consistency | ⚠️ | Different sort orders per module (DESC vs ASC) |
| **Overall** | **✅ Complete** | |

## 20. ROLE-BASED ACCESS CONTROL

| Check | Result | Notes |
|-------|--------|-------|
| SecurityFilter coverage | ✅ | 33 URL prefixes mapped |
| Admin role | ✅ | System config, management, reports |
| Owner role | ✅ | All admin + owner-only pages |
| StoreManager role | ✅ | Branch-scoped employee view, reports |
| SalesStaff role | ✅ | POS, customers, orders |
| WarehouseStaff role | ✅ | Inventory, warehouse, purchase |
| Branch scoping | ⚠️ | ManagerEmployeeServlet has it; others don't auto-scope |
| **Overall** | **✅ Complete** | Phase 2 delivered this |

---

## OVERALL MODULE STATUS

| Module | Status | Critical Issues |
|--------|--------|----------------|
| Authentication | ⚠️ 87% | 1 (register status case) |
| User Management | ⚠️ 60% | 4 (status check, password change, connection leaks, owner stub) |
| Customer | ✅ 100% | 0 |
| Product | ⚠️ 55% | 5 (hard delete, no barcode, warehouse hardcoded, no price validation, service layer dead) |
| Category | ✅ 90% | 0 |
| Supplier | ✅ 100% | 0 |
| Purchase Order | ❌ 0% | 7 (entirely stub) |
| Sales/Order | ⚠️ 70% | 2 (refund incomplete, rollback gap) |
| Inventory | ⚠️ 65% | 3 (no cross-tx import, negative stock, debug/mock) |
| Finance | ❌ 20% | 5 (DAO stubs, filter ignored, type not persisted) |
| Shift | ⚠️ 80% | 1 (hardcoded employee) |
| Dashboard | ⚠️ 60% | 2 (inventory/financial stubs) |
| Reports | ⚠️ 40% | 4 (stubs: loyalty, sales-by-store, inventory, export) |
| VNPAY | ❌ 0% | 3 (no return/IPN servlets, secrets hardcoded) |
| Profile | ⚠️ 75% | 2 (password change broken) |
| Branch | ⚠️ 70% | 3 (hard delete, no auth, no audit) |
| Settings | ❌ 0% | Stub |
| Notifications | ❌ 0% | Stub |

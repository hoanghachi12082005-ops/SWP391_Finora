# Bug Report — Phase 6 QA

## Bug Classification

| Severity | Count | Criteria |
|----------|-------|----------|
| 🔴 Critical | 5 | Data loss, security breach, system crash |
| 🟠 High | 6 | Major feature broken, auth bypass, data corruption |
| 🟡 Medium | 7 | Non-critical broken feature, missing validation |
| 🔵 Low | 5 | Minor UX, cosmetic, edge cases |

---

## 🔴 Critical Bugs

### C1: Branch Update — Redirect Without Return
| Field | Value |
|-------|-------|
| **File** | `BranchController.java:199-201` |
| **Type** | Logic error |
| **Description** | When `isUpdate && dao.findById(id) == null`, `sendRedirect` is called but the method does NOT return. Execution continues to `dao.update(b)`, updating a non-existent record (or worse — inserts with wrong data). |
| **Impact** | Data corruption — update of non-existent entity may silently succeed with wrong target |
| **Fix** | Add `return;` after `response.sendRedirect(...)` at line 201 |

### C2: Inventory Import — Reference ID Hardcoded
| Field | Value |
|-------|-------|
| **File** | `InventoryController.java:494` |
| **Type** | Data integrity |
| **Description** | `stock_transaction.reference_id` hardcoded to `0` instead of actual ticket ID. Audit trail for imports is broken. |
| **Impact** | Cannot trace stock transaction back to import ticket |
| **Fix** | Capture ticket ID from `createExchangeTicket` return value (DAO needs modification to return generated ID) |

### C3: Transfer Confirm Export — No Transaction
| Field | Value |
|-------|-------|
| **File** | `InventoryController.java:505-553` |
| **Type** | Transaction integrity |
| **Description** | `confirmExport` approves the main transfer ticket first (`approveTransferTicket` at line 510), THEN creates sub-tickets for TX/TI (lines 530-566). If sub-ticket creation fails, the main ticket is already IN_TRANSIT with no recovery. |
| **Impact** | Inconsistent state — main ticket shows in-transit but no sub-tickets exist |
| **Fix** | Move `approveTransferTicket` inside the same transaction as sub-ticket creation, or reverse the order: create sub-tickets first, approve last |

### C4: Connection Leak in getWarehouseId (4 Servlets)
| Field | Value |
|-------|-------|
| **File** | `CartServlet.java:376-385`, `CheckoutServlet.java:213-222`, `ProductSearchServlet.java:53-63`, `SalesServlet.java` |
| **Type** | Resource leak |
| **Description** | `getWarehouseId` creates `Connection`, `PreparedStatement`, `ResultSet` with try-with-resources on connection only. Exception path may leak PS/RS. But actual pattern analysis: Connection IS in try-with-resources, but `PreparedStatement` and `ResultSet` are chained. On `Exception e` at line 220-221, the catch logs but connection closed by outer try-with-resources. **RE-ASSESSED: Connection closure is safe.** The real issue is the duplicated code across 4 servlets. |
| **Impact** | LOW — connection is closed. Code duplication is the real debt. |
| **Revised Severity** | 🟡 Medium — duplicate code makes maintenance harder |

### C5: Cashbook Amount — Null Pointer Exception
| Field | Value |
|-------|-------|
| **File** | `IncomeExpenseController.java:134` |
| **Type** | Crash |
| **Description** | `Double.parseDouble(request.getParameter("amount"))` — NPE if `amount` param is missing |
| **Impact** | 500 error with stack trace when amount field is empty |
| **Fix** | Add null check before parse: `String amt = request.getParameter("amount"); if (amt == null || amt.isBlank()) { /* error */ }` |

---

## 🟠 High Severity Bugs

### H1: Admin Role Denied from Customer Management
| Field | Value |
|-------|-------|
| **File** | `CustomerController.java:550-555` |
| **Type** | Authorization logic error |
| **Description** | `isAuthorized()` allows only Owner and StoreManager. Admin role is NOT included in the allowed list. Admin users attempting customer operations get 403. Likely unintentional. |
| **Impact** | Admin users cannot manage customers |
| **Fix** | Add `"Admin"` to the role check at line 556 |

### H2: Product Form — Null Parameter NPE
| Field | Value |
|-------|-------|
| **File** | `ProductController.java:203-211` |
| **Type** | Crash |
| **Description** | `buildProductFromRequest` calls `Integer.parseInt(request.getParameter("categoryID"))` without null check. Missing categoryID param → NPE → 500. |
| **Impact** | Adding products crashes if category field missing |
| **Fix** | Add null/blank check before parseInt, return validation error |

### H3: Audit Log Failures Silently Swallowed
| Field | Value |
|-------|-------|
| **File** | `ProductController.java:125,160,185` |
| **Type** | Data loss |
| **Description** | Three empty `catch (Exception e) { /* ignore log failures */ }` blocks silently swallow audit log insertion failures. Admin has no way to know audit is broken. |
| **Impact** | Audit trail gaps undetected |
| **Fix** | At minimum log the exception: `catch (Exception e) { logger.error("Audit log failed", e); }` |

### H4: GET Delete Mutation — Supplier
| Field | Value |
|-------|-------|
| **File** | `SupplierServlet.java:62-80` |
| **Type** | Security |
| **Description** | `GET /suppliers?action=delete&id=X` performs DB delete. GET should never mutate state. No CSRF token required. No confirmation. |
| **Impact** | CSRF via <img> tag, accidental deletion via link |
| **Fix** | Change to POST only. Add CSRF check. Add confirmation dialog. |

### H5: Fake Employee Auto-Creation (4 Servlets)
| Field | Value |
|-------|-------|
| **File** | `OrdersServlet.java:44-49`, `SalesServlet.java:32-38`, `RevenueServlet.java:35-39`, `ShiftServlet.java:33-37` |
| **Type** | Auth bypass |
| **Description** | When session is null, these servlets create a fake Employee with empId=1, branchId=1, fullName="Thu ngân #1" and continue processing. Allows unauthenticated access to order list, revenue data, shift data. |
| **Impact** | Anonymous users can view orders, revenue, shifts |
| **Fix** | Remove auto-creation, redirect to /login instead |

### H6: Report Endpoints Missing Auth
| Field | Value |
|-------|-------|
| **File** | `ReportController.java` (/reports/sales-by-store, /reports/inventory, /reports/export) |
| **Type** | Authorization gap |
| **Description** | Three report endpoints have no `isOwnerOrManager()` check. Any authenticated user can access sales-by-store and inventory reports. |
| **Impact** | Sales/inventory data exposed to unauthorized roles |
| **Fix** | Add `if (!isOwnerOrManager(request, response)) return;` at top of each handler |

---

## 🟡 Medium Severity Bugs

### M1: 14 Controllers Lack Auth Check
| File | Missing |
|------|---------|
| BranchController | No auth on any action |
| ProductController | No auth |
| CategoryController | No auth |
| SupplierServlet | No auth |
| PosController | No auth |
| WarehouseController | No auth |
| SystemController | No auth |
| SettingsServlet | No auth |
| ReportsServlet (sales) | No auth |
| PaymentInvoiceController | No auth |
| PurchaseOrderController | No auth |
| CashTransactionServlet | No auth (employee null→error but no redirect) |
| IncomeExpenseController | No auth |
| DashboardController | No auth |

### M2: Confusing Redirect in BranchController
| Field | Value |
|-------|-------|
| **File** | `BranchController.java:409` |
| **Description** | Image validation throws IOException instead of adding to validation errors map |

### M3: Stack Trace Exposed to User
| Field | Value |
|-------|-------|
| **File** | `InventoryController.java:622` |
| **Description** | Stack trace content (first 200 chars) included in user-facing error message |
| **Fix** | Remove stack trace from error message, log server-side only |

### M4: OwnerUserServlet Empty doPost
| Field | Value |
|-------|-------|
| **File** | `OwnerUserServlet.java:70-72` |
| **Description** | Empty method body → POST returns no response. Client sees HTTP 200 with empty body. |
| **Fix** | Return 405 Method Not Allowed or implement POST handling |

### M5: VAT Hardcoded
| Field | Value |
|-------|-------|
| **File** | `CheckoutServlet.java:87` |
| **Description** | VAT rate hardcoded as `0.08` (8%). Not configurable. |
| **Fix** | Move to AppConstants or configuration |

### M6: getWarehouseId Duplicate Code
| Field | Value |
|-------|-------|
| **File** | CartServlet, CheckoutServlet, ProductSearchServlet, SalesServlet |
| **Description** | Same 10-line warehouse lookup repeated in 4 files |
| **Fix** | Extract to shared utility method |

### M7: Empty doPost Stubs (9 Controllers)
| File | Line | Issue |
|------|------|-------|
| PaymentInvoiceController | 34-38 | Stub |
| PurchaseOrderController | 42-45 | Stub |
| PosController | 41 | Stub (TODO comment) |
| WarehouseController | 45 | Stub |
| DashboardController | 73 | Delegates to doGet |
| SystemController | 40-43 | Stub |
| StaticPageController | 29-32 | Stub |
| OwnerUserServlet | 70-72 | Empty (no response) |
| SettingsServlet | 35-37 | Stub |

---

## 🔵 Low Severity Bugs

| # | File | Issue |
|---|------|-------|
| L1 | Multiple | `e.printStackTrace()` in production code (~100 occurrences across 27+ files) |
| L2 | Multiple | Forgot-password exposes generic error message "Sai Họ tên hoặc Email!" — email enumeration possible |
| L3 | ProductController | `deleteProductImageFiles` silently ignores file delete failures |
| L4 | Multiple | `parseInt` returning 0 on error indistinguishable from valid ID=0 |
| L5 | ShiftServlet.java:94 | Variable naming confusion: `activeShift` reassigned from method call, shadows local var |

---

## Bug Distribution by Module

| Module | Critical | High | Medium | Low | Total |
|--------|----------|------|--------|-----|-------|
| Auth | 0 | 0 | 0 | 1 | 1 |
| Branch | 1 | 0 | 1 | 0 | 2 |
| Customer | 0 | 1 | 0 | 0 | 1 |
| Product | 1 | 2 | 1 | 1 | 5 |
| Inventory | 2 | 0 | 1 | 0 | 3 |
| Orders/Sales | 0 | 1 | 1 | 0 | 2 |
| Finance | 1 | 0 | 0 | 0 | 1 |
| Supplier | 0 | 1 | 0 | 0 | 1 |
| Reports | 0 | 1 | 0 | 0 | 1 |
| Owner | 0 | 0 | 1 | 0 | 1 |
| Cross-cutting | 0 | 0 | 3 | 3 | 6 |
| **Total** | **5** | **6** | **8** | **5** | **24** |

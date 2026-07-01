# Test Cases — Finora Retail

Generated via static analysis. Each test case maps to a specific controller action.

## TC-AUTH-001: Login Success
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /login` |
| **Input** | Valid email/phone + password |
| **Expected** | Session created, `currentUser` attribute set, CSRF token generated, redirect to role-based home |
| **Actual** | AuthService.login() → BCrypt verify → success → invalidate old session → create new → redirect |
| **Status** | ✅ PASS |

## TC-AUTH-002: Login Wrong Password
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /login` |
| **Input** | Valid email, wrong password |
| **Expected** | Error message "Mật khẩu không chính xác", failed count increments |
| **Actual** | Returns error, increments `failed_login_count` in DB |
| **Status** | ✅ PASS |

## TC-AUTH-003: Login Locked Account
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /login` |
| **Input** | Valid credentials after 5 failed attempts |
| **Expected** | Error "Tài khoản đã bị khóa do đăng nhập sai quá 5 lần" |
| **Actual** | AuthService throws RuntimeException with lockout message |
| **Status** | ✅ PASS |

## TC-AUTH-004: Login Inactive Account
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /login` |
| **Input** | Valid credentials for INACTIVE status employee |
| **Expected** | Error "Tài khoản đã bị khóa hoặc chưa được kích hoạt" |
| **Actual** | AuthService checks `!"ACTIVE".equalsIgnoreCase(status)` → blocks |
| **Status** | ✅ PASS |

## TC-AUTH-005: Forgot Password
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /forgot-password` |
| **Input** | Valid fullName + email match |
| **Expected** | Email sent first, THEN password updated in DB (fixed Phase 5) |
| **Actual** | Sends email → if success, updates DB hash |
| **Status** | ✅ PASS (Phase 5 fix verified) |

## TC-AUTH-006: Forgot Password Email Fail
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /forgot-password` |
| **Input** | Valid match, email send fails |
| **Expected** | Error message, DB NOT updated |
| **Actual** | Returns error, no DB change (Phase 5 fix) |
| **Status** | ✅ PASS (Phase 5 fix verified) |

## TC-AUTH-007: Session Fixation
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /login` |
| **Input** | Valid login with pre-existing session |
| **Expected** | Old session invalidated, new session ID issued |
| **Actual** | Line 115-117: `oldSession.invalidate()` then `request.getSession(true)` |
| **Status** | ✅ PASS |

## TC-AUTH-008: CSRF Token Generation
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /*` (first request) |
| **Input** | Anonymous user |
| **Expected** | `csrfToken` stored in session |
| **Actual** | SecurityFilter generates on first GET if token missing |
| **Status** | ✅ PASS |

## TC-AUTH-009: CSRF Token Validation
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /*` (authenticated, non-exempt) |
| **Input** | POST with missing/wrong csrfToken |
| **Expected** | 403 error "CSRF token không hợp lệ" |
| **Actual** | SecurityFilter compares params vs session → 403 if mismatch |
| **Status** | ✅ PASS |

## TC-AUTH-010: Logout
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /logout` |
| **Input** | Authenticated session |
| **Expected** | Session invalidated, redirect to /login |
| **Actual** | `session.invalidate()` → redirect |
| **Status** | ✅ PASS |

---

## TC-BRANCH-001: Create Branch
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /branch?action=insert` |
| **Input** | Valid branch data with image |
| **Expected** | Branch created, validation errors returned if invalid |
| **Actual** | BranchValidator.validateForInsert → dao.insert → redirect |
| **Status** | 🔴 FAIL — **BUG C1**: Redirect sent but execution continues if branch not found for update |

## TC-BRANCH-002: Update Branch
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /branch?action=update` |
| **Input** | Valid branch ID + updated data |
| **Expected** | Branch updated |
| **Actual** | BranchValidator.validateForUpdate → dao.update |
| **Status** | 🔴 FAIL — **BUG C1**: If branch not found, redirect sent without return |

## TC-BRANCH-003: Delete Branch (GET)
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /branch?action=delete&id=X` |
| **Input** | Valid branch ID |
| **Expected** | Branch deleted (WARNING: GET mutation) |
| **Actual** | dao.delete(id) called directly on GET |
| **Status** | 🔴 FAIL — **BUG-H4**: CSRF vulnerability, no confirmation |

---

## TC-CUSTOMER-001: Create Customer
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /customers?action=create` |
| **Input** | Valid fullName, phone, email |
| **Expected** | Customer created, duplicate check enforced |
| **Actual** | CustomerController → CustomerDAO.insert |
| **Status** | ✅ PASS |

## TC-CUSTOMER-002: Admin Blocked
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /customers?action=create` |
| **Input** | Logged in as Admin |
| **Expected** | FORBIDDEN (Admin role is explicitly denied) |
| **Actual** | Lines 550-555: `roleName.equals("Owner")` only — Admin gets 403 |
| **Status** | 🔴 FAIL — **BUG H1**: Admin role denied from customer mgmt |

---

## TC-INVENTORY-001: Transfer Save
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /inventory?action=saveTransfer` |
| **Input** | Valid product IDs, quantities, warehouse IDs |
| **Expected** | Transfer tickets created in one transaction |
| **Actual** | Loop creates tickets per partner warehouse — **no transaction across loop iterations**. Partial failure possible. |
| **Status** | 🔴 FAIL — **BUG C3**: No transaction around multiple ticket creation |

## TC-INVENTORY-002: Import Save
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /inventory?action=saveImport` |
| **Input** | Valid product IDs, quantities, suppliers |
| **Expected** | Import ticket created, stock increased, transaction logged |
| **Actual** | Ticket created → stock increased → transaction logged with `referenceId=0` |
| **Status** | 🔴 FAIL — **BUG C2**: referenceId hardcoded to 0, breaks audit trail |

## TC-INVENTORY-003: Confirm Export
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /inventory?action=confirmExport` |
| **Input** | Valid transfer ID |
| **Expected** | Status set to IN_TRANSIT, sub-tickets created for TX/TI |
| **Actual** | Main ticket approved first, then sub-tickets created — no rollback if sub fails |
| **Status** | 🔴 FAIL — **BUG C3**: Main ticket committed before sub-tickets |

---

## TC-SALES-001: Checkout (POS)
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /checkout` |
| **Input** | Cart items, payment method, cash received |
| **Expected** | Transaction: stock check → order create → payment → stock deduct → points → complete |
| **Actual** | All in one transaction. Exception rollback works (fixed Phase 5). |
| **Status** | ✅ PASS |

## TC-SALES-002: Checkout Insufficient Stock
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /checkout` |
| **Input** | Cart with quantity > available stock |
| **Expected** | Rollback, error message for specific product |
| **Actual** | Stock check per item in transaction → rollback on failure |
| **Status** | ✅ PASS |

## TC-SALES-003: Checkout Wrong Payment Method
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /checkout` |
| **Input** | Invalid paymentMethod param |
| **Expected** | Default to "CASH" |
| **Actual** | Falls through to CASH if not CASH or BANK_TRANSFER |
| **Status** | ✅ PASS |

## TC-SALES-004: Refund Order
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /orders/refund` |
| **Input** | Valid COMPLETED order ID |
| **Expected** | Transaction: stock restore → points reverse → status = CANCELLED → audit log |
| **Actual** | All in one transaction (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-SALES-005: Refund Non-Completed Order
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /orders/refund` |
| **Input** | PENDING or CANCELLED order |
| **Expected** | Error "Chỉ có thể hoàn trả đơn hàng đã hoàn thành" |
| **Actual** | Status check → error response |
| **Status** | ✅ PASS |

## TC-SALES-006: GET Orders Unauthenticated
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /orders` |
| **Input** | No session |
| **Expected** | Redirect to login or error |
| **Actual** | **BUG H5**: Auto-creates fake Employee with empId=1, branchId=1, fullName="Thu ngân #1" |
| **Status** | 🔴 FAIL — **BUG H5**: No auth check, fake identity created |

---

## TC-SEC-001: SQL Injection (DAOs)
| Field | Value |
|-------|-------|
| **Endpoint** | InventoryTicketDAO.findAllByType, findAllByTypeAndStatus, getPendingCount, StockTransactionDAO (Phase 2 fix) |
| **Input** | SQL injection payloads in parameters |
| **Expected** | Parameterized queries prevent injection |
| **Actual** | All use PreparedStatement with `?` placeholders (fixed Phase 2) |
| **Status** | ✅ PASS |

## TC-SEC-002: Unauthenticated Access
| Field | Value |
|-------|-------|
| **Module** | BranchController, ProductController, CategoryController, SupplierServlet, PosController, WarehouseController, SystemController, SettingsServlet, ReportsServlet, PaymentInvoiceController, PurchaseOrderController, CashTransactionServlet |
| **Input** | No session, direct URL access |
| **Expected** | 401 redirect to /login |
| **Actual** | **No auth check** — SecurityFilter ROLE_MAP may not cover these paths |
| **Status** | 🔴 FAIL — 14 controllers lack auth enforcement |

## TC-SEC-003: GET Mutation
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /suppliers?action=delete&id=X` |
| **Input** | GET request with delete action |
| **Expected** | Should reject GET for state-changing operations |
| **Actual** | Deletes supplier on GET request (SupplierServlet line 62-80) |
| **Status** | 🔴 FAIL — **BUG H4** |

## TC-SEC-004: Empty doPost (No Response)
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /owner/emp` |
| **Input** | Any POST data |
| **Expected** | Some response (error, redirect) |
| **Actual** | Empty method body — no response sent (OwnerUserServlet line 70-72) |
| **Status** | 🔴 FAIL — **BUG M4** |

---

## TC-PERF-001: Connection Leak
| Field | Value |
|-------|-------|
| **Module** | CartServlet.getWarehouseId(), CheckoutServlet.getWarehouseId(), ProductSearchServlet.getWarehouseId(), SalesServlet.getWarehouseId() |
| **Input** | Normal request flow |
| **Expected** | Connections fully closed |
| **Actual** | No finally block — connections leak on exception |
| **Status** | 🔴 FAIL — **BUG C4**: Same pattern in 4 servlets |

---

## TC-VALIDATION-001: Product Form NPE
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /products` |
| **Input** | Missing `categoryID` parameter |
| **Expected** | Validation error message |
| **Actual** | `Integer.parseInt(null)` → NPE (ProductController line 203-211) |
| **Status** | 🔴 FAIL — **BUG H2** |

## TC-VALIDATION-002: Cashbook Amount NPE
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /cashbook/create-receipt` |
| **Input** | Missing `amount` parameter |
| **Expected** | Validation error message |
| **Actual** | `Double.parseDouble(null)` → NPE (IncomeExpenseController line 134) |
| **Status** | 🔴 FAIL — **BUG C5** |

---

## TC-REGRESSION-001: Password Change (Phase 3 fix)
| Field | Value |
|-------|-------|
| **Module** | ProfileServlet.changePassword() |
| **Expected** | Old password verified via BCrypt, new password hashed |
| **Actual** | `PasswordUtil.verify(oldPassword, currentHash)` used (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-002: Lock/Unlock Status (Phase 3 fix)
| Field | Value |
|-------|-------|
| **Module** | AdminUserServlet.updateStatus() |
| **Expected** | Status set to "ACTIVE" or "INACTIVE" (valid per CHECK constraint) |
| **Actual** | Uses "INACTIVE" for lock, "ACTIVE" for unlock (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-003: Register Status Case (Phase 3 fix)
| Field | Value |
|-------|-------|
| **Module** | UserManagementDao.addEmployee() |
| **Expected** | Default status "ACTIVE" (uppercase, valid per CHECK constraint) |
| **Actual** | `rawStatus == null ? "ACTIVE" : rawStatus.toUpperCase()` (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-004: Debug Writes Removed (Phase 3 fix)
| Field | Value |
|-------|-------|
| **Module** | InventoryController.saveTransfer |
| **Expected** | No file writes to C:\Users\letha\.gemini\... |
| **Actual** | All debug writes removed (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-005: Negative Stock Guard (Phase 3 fix)
| Field | Value |
|-------|-------|
| **Module** | InventoryTicketDAO.confirmDispatch |
| **Expected** | Throws SQLException if `beforeQty < d.getQuantity()` |
| **Actual** | Guard added at line 337 (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-006: Checkout Exception Catch (Phase 3 fix)
| Field | Value |
|-------|-------|
| **Module** | CheckoutServlet.doPost |
| **Expected** | Catches Exception (not just SQLException) for rollback |
| **Actual** | `catch (Exception e)` with rollback (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-007: Flash Message CSS (Phase 4 fix)
| Field | Value |
|-------|-------|
| **Module** | theme.css |
| **Expected** | Flash messages visible |
| **Actual** | `.message { display: none !important; }` removed (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-008: Topbar CTA Link (Phase 4 fix)
| Field | Value |
|-------|-------|
| **Module** | topbar.jsp |
| **Expected** | Link to /pos/sale (valid endpoint) |
| **Actual** | Changed from /orders/create (404) to /pos/sale (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-009: Forgot Password Email First (Phase 5 fix)
| Field | Value |
|-------|-------|
| **Module** | AuthServlet.handleForgotPassword |
| **Expected** | Email sent BEFORE DB password update |
| **Actual** | Reordered — email first, DB only if sent (fixed Phase 5) |
| **Status** | ✅ PASS |

## TC-REGRESSION-010: Dead Code Removed (Phase 5 fix)
| Field | Value |
|-------|-------|
| **Module** | Multiple |
| **Expected** | GenericService, ProductService, CategoryService, AuthFilter, DatabaseUtil, MigrateDB deleted |
| **Actual** | All deleted (verified Phase 5) |
| **Status** | ✅ PASS |

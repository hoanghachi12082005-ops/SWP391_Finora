# Test Plan — Finora Retail Management System

## 1. Scope

### In Scope
- All 32 controllers and their endpoints
- Authentication & authorization flows
- All CRUD operations (where implemented)
- Business transaction integrity (checkout, refund, inventory transfer)
- VNPAY payment integration (stub)
- Security: CSRF, SQL injection, XSS, session management
- Reports (where implemented)
- UI consistency across 57 JSPs

### Out of Scope
- Database schema validation (covered in Phases 1/5)
- Third-party integrations (VNPAY sandbox not available)
- Load/stress testing (no deployed environment)
- Mobile responsiveness (no device lab)

## 2. Test Strategy

| Level | Approach | Tool/Method |
|-------|----------|-------------|
| Unit | Static analysis | Code review per controller |
| Integration | Transaction flow tracing | Path analysis across DAOs |
| System | End-to-end workflow | Business scenario tracing |
| Security | Vulnerability pattern scan | OWASP Top 10 checklist |
| Regression | Re-test of Phase 1-5 fixes | Fix validation per file |

## 3. Feature Inventory (32 Controllers / ~110 Endpoints)

| Module | Controller | Endpoints | Status |
|--------|-----------|-----------|--------|
| Auth | AuthServlet | 7 endpoints | Implemented |
| Branch | BranchController | 6 actions | Implemented |
| Customer | CustomerController | 12 actions | Implemented |
| Dashboard | DashboardController | 3 URLs | Implemented |
| Finance | IncomeExpenseController | 3 URLs | Implemented |
| Finance | PaymentInvoiceController | 2 URLs | **Stub** |
| Inventory | InventoryController | 15+ actions | Implemented |
| POS | PosController | 3 URLs | **Partial** |
| Product | ProductController | 3 actions | Implemented |
| Category | CategoryController | 4 actions | Implemented |
| Purchase | PurchaseOrderController | 2 URLs | **Stub** |
| Reports | ReportController | 6 URLs | Implemented |
| Cart | CartServlet | 11 actions | Implemented |
| Checkout | CheckoutServlet | 1 action | Implemented |
| Orders | OrdersServlet | 3 URLs | Implemented |
| Print | PrintPreviewServlet | 1 action | Implemented |
| ProductSearch | ProductSearchServlet | 1 action | Implemented |
| Revenue | RevenueServlet | 1 URL | Implemented |
| Sales | SalesServlet | 4 actions | Implemented |
| Settings | SettingsServlet | 1 URL | **Stub** |
| Shift | ShiftServlet | 2 actions | Implemented |
| Supplier | SupplierServlet | 6 actions | Implemented |
| Activity | ActivityLogController | 1 URL | Implemented |
| System | SystemController | 2 URLs | **Partial** |
| Admin | AdminUserServlet | 7 actions | Implemented |
| Manager | ManagerEmployeeServlet | 2 actions | Implemented |
| Owner | OwnerUserServlet | 2 actions | **Partial** |
| Profile | ProfileServlet | 2 actions | Implemented |
| Warehouse | WarehouseController | 4 URLs | **Stub** |
| Static | StaticPageController | 3 URLs | Implemented |
| CashTx | CashTransactionServlet | 1 action | Implemented |
| ReportsSales | ReportsServlet | 1 URL | **Stub** |

## 4. Test Schedule

| Phase | Activities | Duration |
|-------|-----------|----------|
| Static Analysis | Code review all 32 controllers | 8 hours |
| Bug Triage | Classify findings (C/H/M/L) | 2 hours |
| Fix Validation | Re-test Phase 1-5 fixes | 2 hours |
| Documentation | Generate QA reports | 4 hours |

## 5. Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| No running environment | Cannot execute dynamic tests | Static analysis + code tracing |
| 14 controllers lack auth checks | Unauthorized data access | Documented in BUG_REPORT.md |
| VNPAY integration incomplete | Payment flow cannot complete | Flagged as incomplete feature |
| Connection leaks in 4 servlets | Production outage under load | Documented with fix instructions |

# TECHNICAL DEBT — Phase 1 Remaining

**Date:** 2026-07-01  

---

## 1. REMAINING TECHNICAL DEBT

### 1.1 Runtime-Risk Items

| # | Item | File(s) | Risk | Effort | Notes |
|---|------|---------|------|--------|-------|
| T1 | `ShiftDAO` references `cash_transaction` for expected cash calculation | `dao/sales/ShiftDAO.java` | 🟡 MEDIUM | 1 day | Table `cash_transaction` now exists, but date range queries may need index |
| T2 | InventoryTicketDAO has SQL injection via string concatenation (5+ locations) | `dao/inventory/InventoryTicketDAO.java` | 🟡 MEDIUM | 4 hours | `warehouseId`, `ticketType`, `status` concatenated directly |
| T3 | StockTransactionDAO has SQL injection via `allowedWarehouseIds` | `dao/inventory/StockTransactionDAO.java:31-36` | 🟡 MEDIUM | 2 hours | List<Integer> concatenated into IN clause |
| T4 | `InventoryDAO.getDashboardKPI` has SQL injection via `allowedWarehouseIds` | `dao/inventory/InventoryDAO.java` | 🟡 MEDIUM | 2 hours | Same pattern |
| T5 | `OrdersServlet.getStatus().name()` NPE risk | `controller/sales/OrdersServlet.java:110` | 🟡 MEDIUM | 30 min | `getStatus()` could return null |
| T6 | `SalesServlet.setBod()` parse exception risk | `controller/sales/SalesServlet.java:133` | 🟢 LOW | 30 min | `LocalDate.parse()` can throw if format is wrong |

### 1.2 Code Quality Items

| # | Item | File(s) | Effort | Notes |
|---|------|---------|--------|-------|
| T7 | 26 JSPs use deprecated JSTL 1.x URIs | `web/WEB-INF/views/*.jsp` | 2 days | `http://java.sun.com/jsp/jstl/*` → `jakarta.tags.*` |
| T8 | AuthFilter only protects 4/33 URL prefixes | `filter/AuthFilter.java` | 3-4 days | 29 unprotected controller endpoints |
| T9 | No RoleFilter — only AuthFilter with hardcoded role strings | `filter/AuthFilter.java` | 2 days | Role checking should use annotation/filter pattern |
| T10 | Hardcoded database credentials in DBContext.java | `util/database/DBContext.java` | 1 day | Move to JNDI or environment variables |
| T11 | `DatabaseUtil.java` points to DBFinoraV2 (should be V3) | `util/DatabaseUtil.java:11` | 30 min | Fallback URL is V2 not V3 |
| T12 | No connection pooling (DriverManager) | `util/database/DBContext.java` | 1 day | HikariCP would improve performance |
| T13 | `CategoryDAO.isDescendant()` CTE logic inverted | `dao/product/CategoryDAO.java:179-197` | 1 hour | Seeds with `categoryId` instead of `candidateParentId` |
| T14 | No error handling in `UserManagementDao.deleteRoleSql` dead query | `dao/user/UserManagementDao.java:581-607` | 30 min | `SELECT 1 WHERE ? = 0` does nothing — dead code |
| T15 | ProfileDao resource leak (not using try-with-resources for Connection) | `dao/user/ProfileDao.java:38-58,105-128` | 1 hour | Connection/PS/RS not properly closed in some methods |
| T16 | `OrdersDAO` uses `SELECT o.*` pattern | `dao/sales/OrderDAO.java` | 1 day | Inefficient, should select specific columns |

### 1.3 Architecture Items

| # | Item | Effort | Notes |
|---|------|--------|-------|
| T17 | Service layer is mostly empty (12 services, avg ~20 lines) | 3-4 days | Business logic lives in controllers and DAOs |
| T18 | No dependency injection — `new DAO()` everywhere | 2-3 days | Manual DI or CDI |
| T19 | No tests anywhere (empty `src/test/`) | 6-8 weeks | Full testing needed |
| T20 | `UserManagementDao` is 935 lines — violates Single Responsibility | 2 days | Should be split into focused DAOs |
| T21 | `InventoryController` is 711 lines | 1 day | Should delegate more to service layer |
| T22 | `CustomerDAO` (customer package) has 712 lines | 1 day | Similar issue |

### 1.4 Security Items

| # | Item | Effort | Notes |
|---|------|--------|-------|
| T23 | No CSRF protection on any form | 1 day | CSRF filter needed |
| T24 | No session timeout in web.xml | 30 min | Already has `<session-timeout>30</session-timeout>` ✅ |
| T25 | Session ID not regenerated on login | 2 hours | Session fixation vulnerability |
| T26 | No XSS output encoding in JSP EL | 1 day | Add `fn:escapeXml()` |
| T27 | No HTTPS enforcement | 1 day | Web.xml security-constraint needed |
| T28 | No password complexity validation | 4 hours | Password policy enforcement |
| T29 | Password reset token not implemented | 1 day | Forgot password sends email without token |

---

## 2. KNOWN RISKS

### 2.1 High Risk

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| `ShiftDAO` and `CashTransactionDAO` have NO tests | HIGH | MEDIUM | These DAOs are only used by shift management — test at integration level |
| 29 unprotected controllers can be accessed without auth | HIGH | CRITICAL | Add role checks (Phase 2 security) |
| `InventoryTicketDAO` SQL injection via string concatenation | MEDIUM | HIGH | Switch to PreparedStatement parameters |
| `StockTransactionDAO` SQL injection in `allowedWarehouseIds` | MEDIUM | HIGH | Move to parameterized IN clause |

### 2.2 Medium Risk

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| `Payment` model has fields not in V3 payment table | MEDIUM | LOW | Works because DAO only inserts V3 columns |
| `Product` model `supplierIDs`, `importPrice` are stale | MEDIUM | LOW | Not persisted — only used for display |
| `Customer` model `passwordHash` is never persisted | MEDIUM | LOW | No customer auth implemented |
| JSTL URI issue may break in future Tomcat versions | LOW | MEDIUM | Replace deprecated URIs |

---

## 3. RECOMMENDATIONS

### 3.1 Before Production (Critical)

1. **Add role checks to all 29 unprotected controllers** — this is the #1 security risk
2. **Fix SQL injection** in InventoryTicketDAO and StockTransactionDAO
3. **Add CSRF filter** — all forms are vulnerable
4. **Fix `CategoryDAO.isDescendant()`** — CTE logic is inverted
5. **Fix `ProfileDao` resource leaks** — connections not properly closed
6. **Move DB credentials to JNDI** — hardcoded in source code

### 3.2 Phase 2 Priorities

1. **Add role-based authorization filter** — replace hardcoded AuthFilter role checks
2. **Add test infrastructure** — JUnit + Mockito + H2 for DAO testing
3. **Fix JSTL URIs** — migrate 26 JSPs from JSTL 1.x to Jakarta 3.0
4. **Add connection pooling** — HikariCP
5. **Split UserManagementDao** — 935 lines is unmaintainable

### 3.3 Phase 3+ Considerations

1. Implement proper service layer
2. Add CI/CD pipeline
3. Performance optimization (caching, pagination)
4. Full test coverage for critical flows (login, POS, inventory)

---

## 4. DEBT SUMMARY

| Category | Items | Estimated Effort | Priority |
|----------|-------|-----------------|----------|
| 🔴 Critical (runtime/security) | 7 | 2-3 weeks | Phase 2 |
| 🟡 High (stability/quality) | 10 | 2-3 weeks | Phase 2-3 |
| 🟢 Medium (improvement) | 7 | 1-2 weeks | Phase 3 |
| 🔵 Low (nice to have) | 5 | 1 week | Phase 4 |

**Total remaining debt:** ~7-9 weeks for full remediation  
**Phase 1 resolved:** 11 compilation fixes + 5 new tables + 20+ SQL column fixes + 9 runtime bug fixes

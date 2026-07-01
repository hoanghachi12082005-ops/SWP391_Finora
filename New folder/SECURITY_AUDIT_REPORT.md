# SECURITY AUDIT REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01
**Phase:** Phase 2 — Authentication, Authorization & Security Hardening
**Status:** Remediation complete — remaining items are accepted risk or future work

---

## 1. Finding Summary

| Risk Level | Count | Fixed |
|-----------|-------|-------|
| **Critical** | 2 | 2 |
| **High** | 2 | 2 |
| **Medium** | 3 | 2 |
| **Low** | 3 | 0 |

---

## 2. Remediated Findings

### CRITICAL — SQL Injection (InventoryTicketDAO)
- **File:** `dao/inventory/InventoryTicketDAO.java`
- **Methods:** `findAllByType`, `findAllByTypeAndStatus`, `getPendingCount`
- **Root cause:** `warehouseId` and `status` concatenated directly into SQL string
- **Fix:** Replaced concatenation with `PreparedStatement` parameters via `StringBuilder` + dynamic parameter binding
- **Status:** ✅ Fixed

### CRITICAL — SQL Injection (StockTransactionDAO)
- **File:** `dao/inventory/StockTransactionDAO.java`
- **Method:** `findAll`
- **Root cause:** `allowedWarehouseIds` list concatenated into SQL `IN (...)` clause via string concatenation
- **Fix:** Replaced with `?` placeholders generated per element, bound via `PreparedStatement.setInt()`
- **Status:** ✅ Fixed

### HIGH — Session Fixation
- **File:** `controller/auth/AuthServlet.java`
- **Root cause:** `handleLogin` reused existing session after authentication without regeneration
- **Fix:** `oldSession.invalidate()` followed by `request.getSession(true)` before setting attributes
- **Status:** ✅ Fixed

### HIGH — Missing CSRF Protection
- **Files:** `controller/auth/AuthServlet.java`, `filter/SecurityFilter.java`
- **Root cause:** No CSRF tokens generated or validated on state-changing requests
- **Fix:** SecurityFilter generates 256-bit CSRF token on first GET request, validates on all POST (exempting login/logout/static). AuthServlet generates token on login. Token stored in session, submitted as `csrfToken` form parameter.
- **Status:** ✅ Fixed

### MEDIUM — Insecure HTTP Headers
- **File:** `filter/SecurityFilter.java`
- **Fix:** Header injection added: `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy: same-origin`, anti-cache headers
- **Status:** ✅ Fixed

---

## 3. Outstanding Findings

### MEDIUM — No XSS Output Encoding
- **Impact:** JSPs use `${employee.fullName}` and similar EL expressions without escaping via `fn:escapeXml()` or JSTL `c:out`
- **Remedy:** Audit all JSPs to replace `${expr}` with `<c:out value="${expr}" />` or `fn:escapeXml()`
- **Priority:** Next phase

### MEDIUM — No Rate Limiting on Login
- **Impact:** Brute-force login attack possible (account lockout exists per-account at 5 failures, but no global IP-based throttle)
- **Remedy:** Add `filter/RateLimitFilter.java` using in-memory sliding window or DB-backed counters
- **Priority:** Next phase

### LOW — Password Policy Not Enforced Client/Server
- **Impact:** No minimum password complexity or length validation on registration/reset
- **Remedy:** Add validation in `AuthServlet` and `UserManagementDao`
- **Priority:** Next phase

### LOW — No Audit Logging
- **Impact:** Login failures, permission denials, and UI changes not logged centrally
- **Remedy:** Add `util/log/AuditLogger.java` writing to `audit_log` table
- **Priority:** Next phase

### LOW — JSP Direct Access
- **Impact:** JSP files under `/views/` served if directly accessed (no WEB-INF structural protection for all paths)
- **Remedy:** Move all JSPs under `WEB-INF/views/` or add SecurityFilter deny rules for `/views/` prefix
- **Priority:** Next phase

---

## 4. Security Architecture Assessment

- **Authentication:** BCrypt with work factor 12 (`PasswordUtil`), account lockout at 5 failures — **good**
- **Authorization:** URL-prefix-based role mapping in SecurityFilter — **adequate** for current scope
- **Session Management:** Session fixation fixed, HTTP-only (default), no secure flag for dev — **improved**
- **Data Protection:** No sensitive data in URL params observed — **adequate**
- **SQL Injection:** Eliminated in audited DAOs — remaining DAOs use PreparedStatement for primary queries
- **CSRF:** Tokens generated and validated — **newly implemented**
- **Security Headers:** Basic set applied — **newly implemented**

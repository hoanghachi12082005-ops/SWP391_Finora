# SECURITY FIX PLAN

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01
**Phase:** Phase 2 (Complete — next phase items listed)

---

## ✅ Completed Fixes (Phase 2)

| # | Finding | Change | Files |
|---|---------|--------|-------|
| 1 | SQL Injection — InventoryTicketDAO | Replaced string concatenation with `PreparedStatement` parameters in `findAllByType`, `findAllByTypeAndStatus`, `getPendingCount` | `dao/inventory/InventoryTicketDAO.java` |
| 2 | SQL Injection — StockTransactionDAO | Replaced `allowedWarehouseIds` IN clause concatenation with generated `?` placeholders + `setInt()` | `dao/inventory/StockTransactionDAO.java` |
| 3 | Session Fixation | Invalidate old session, create new session after successful login | `controller/auth/AuthServlet.java` |
| 4 | Missing CSRF Protection | Add CSRF token generation on every first GET + login session creation; validate on POST | `filter/SecurityFilter.java`, `controller/auth/AuthServlet.java` |
| 5 | Missing Security Headers | Add `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`, anti-cache headers | `filter/SecurityFilter.java` |

---

## 🔜 Phase 3 — Recommended Items

### P1 — XSS Output Encoding (High)
- **Problem:** JSPs use raw EL expressions (`${employee.fullName}`) that can render unescaped user input
- **Fix:** Replace all `${...}` with `<c:out value="..." />` in JSPs, or wrap with `fn:escapeXml()`
- **Effort:** Medium (scan all JSPs under `web/WEB-INF/views/`)
- **Justification:** OWASP #2 — reflected/stored XSS can steal session tokens

### P2 — Rate Limiting on Login (High)
- **Problem:** No IP-based brute-force throttle (only per-account lockout at 5 failures)
- **Fix:** Add `filter/RateLimitFilter.java` with in-memory sliding window (e.g., 5 attempts/15min per IP)
- **Effort:** Low (single filter class)
- **Justification:** Block distributed brute-force attacks

### P3 — Audit Logging (Medium)
- **Problem:** Login failures, permission denials, and sensitive operations not logged
- **Fix:** Add `util/log/AuditLogger.java` writing to `audit_log` table; call from SecurityFilter, AuthServlet
- **Effort:** Low (1 utility + 1 table if not exists)
- **Justification:** Incident investigation and compliance

### P4 — Password Policy (Medium)
- **Problem:** No minimum length/complexity validation on registration or password reset
- **Fix:** Add validation in AuthServlet (minimum 8 chars, at least 1 digit, 1 uppercase)
- **Effort:** Low (add validation block + user-facing error message)
- **Justification:** OWASP #2 — weak credentials

### P5 — JSP WEB-INF Protection (Low)
- **Problem:** Some JSPs may be accessible via direct URL
- **Fix:** Move all JSPs under `WEB-INF/views/` or add SecurityFilter deny rule for `/views/`
- **Effort:** Low
- **Justification:** Defense in depth — prevents direct JSP access bypassing servlets

### P6 — HSTS + CSP Headers (Low)
- **Problem:** Missing `Strict-Transport-Security` and `Content-Security-Policy` headers
- **Fix:** Add to SecurityFilter header set
- **Effort:** Trivial
- **Justification:** Defense in depth against MITM and XSS

---

## 📊 Risk Reduction Summary

| Before Phase 2 | After Phase 2 |
|---------------|---------------|
| SQL Injection (2 files) | ✅ Eliminated |
| Session Fixation | ✅ Fixed |
| No CSRF Protection | ✅ Implemented |
| No Security Headers | ✅ Implemented (basic set) |
| Auth guard on 4 URLs (AuthFilter) | ✅ 33 URL prefixes guarded |
| XSS Output Encoding | ❌ Outstanding (P3-1) |
| Rate Limiting | ❌ Outstanding (P3-2) |
| Audit Logging | ❌ Outstanding (P3-3) |

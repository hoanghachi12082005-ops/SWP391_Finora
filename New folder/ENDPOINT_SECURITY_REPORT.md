# ENDPOINT SECURITY REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## Filter Chain

```
Request → SecurityFilter (@WebFilter("/*"))
           ├── Public path? → skip all checks → chain
           ├── Authenticated? → 401 → /login
           ├── Security headers added
           ├── CSRF token generated (GET, no token)
           ├── Role authorized? → 403
           ├── CSRF token valid (POST)? → 403
           └── chain.doFilter()
```

**Note:** Old `AuthFilter` is disarmed (`@WebFilter` commented out). `SecurityFilter` handles all auth/authz/CSRF/security-headers.

---

## Endpoint Coverage

### Authenticated & Authorized Endpoints (29 URL prefixes mapped)

| # | Prefix | Method | Auth | Roles Checked | CSRF |
|---|--------|--------|------|---------------|------|
| 1 | `/system/` | ANY | ✅ | Admin, Owner | ✅ |
| 2 | `/management/` | ANY | ✅ | 4 roles | ✅ |
| 3 | `/pos/` | ANY | ✅ | 4 roles | ✅ |
| 4 | `/owner/` | ANY | ✅ | Owner only | ✅ |
| 5 | `/admin/` | ANY | ✅ | Admin, Owner | ✅ |
| 6 | `/manager/` | ANY | ✅ | 3 roles | ✅ |
| 7 | `/branch/` | ANY | ✅ | Admin, Owner | ✅ |
| 8 | `/supplier/` | ANY | ✅ | 4 roles | ✅ |
| 9 | `/purchase/` | ANY | ✅ | 4 roles | ✅ |
| 10 | `/finance/` | ANY | ✅ | Admin, Owner | ✅ |
| 11 | `/activity/` | ANY | ✅ | Admin, Owner | ✅ |
| 12 | `/settings` | ANY | ✅ | Admin, Owner | ✅ |
| 13 | `/report/` | ANY | ✅ | 3 roles | ✅ |
| 14 | `/inventory/` | ANY | ✅ | 4 roles | ✅ |
| 15 | `/warehouse/` | ANY | ✅ | 4 roles | ✅ |
| 16 | `/product/` | ANY | ✅ | 4 roles | ✅ |
| 17 | `/category/` | ANY | ✅ | 3 roles | ✅ |
| 18 | `/customer/` | ANY | ✅ | 4 roles | ✅ |
| 19 | `/sales/` | ANY | ✅ | 4 roles | ✅ |
| 20 | `/cart/` | ANY | ✅ | 4 roles | ✅ |
| 21 | `/checkout/` | ANY | ✅ | 4 roles | ✅ |
| 22 | `/orders/` | ANY | ✅ | 4 roles | ✅ |
| 23 | `/print/` | ANY | ✅ | 4 roles | ✅ |
| 24 | `/search-product` | ANY | ✅ | 4 roles | ✅ |
| 25 | `/cash-transaction` | ANY | ✅ | 3 roles | ✅ |
| 26 | `/dashboard/` | ANY | ✅ | 3 roles | ✅ |
| 27 | `/revenue/` | ANY | ✅ | 3 roles | ✅ |
| 28 | `/shift/` | ANY | ✅ | 4 roles | ✅ |
| 29 | `/profile/` | ANY | ✅ | All 5 roles | ✅ |

### Public (No Auth Required)

| # | Path | Notes |
|---|------|-------|
| 1 | `/login` | GET/POST — login page + authentication |
| 2 | `/logout` | POST — logout handler |
| 3 | `/forgot-password` | GET/POST — password reset |
| 4 | `/register` | GET/POST — registration (if enabled) |
| 5 | `/role-selection` | GET/POST — choose role post-login |
| 6 | `/assets/*` | Static resources |
| 7 | `/css/*` | Stylesheets |
| 8 | `/js/*` | JavaScript files |
| 9 | `/static/*` | Static resources |

---

## Security Headers Applied

| Header | Value | Where |
|--------|-------|-------|
| `Cache-Control` | `no-cache, no-store, must-revalidate` | SecurityFilter (all auth pages) |
| `Pragma` | `no-cache` | SecurityFilter |
| `Expires` | `0` (epoch) | SecurityFilter |
| `X-Frame-Options` | `DENY` | SecurityFilter |
| `X-Content-Type-Options` | `nosniff` | SecurityFilter |
| `Referrer-Policy` | `same-origin` | SecurityFilter |

**Missing:** `Strict-Transport-Security` (HSTS), `Content-Security-Policy` (CSP), `Permissions-Policy`

---

## CSRF Protection

| Aspect | Status |
|--------|--------|
| Token generation | ✅ On first GET (SecurityFilter) + on login (AuthServlet) |
| Token validation | ✅ On every POST (exempt: login, logout, static assets) |
| Token length | 256-bit (32 bytes), Base64-encoded |
| Token storage | Server: `HttpSession` attribute `csrfToken` |
| Token transport | Form field `csrfToken` (via JSP `sessionScope`) |
| Exempt endpoints | `/login`, `/logout`, `/static/*`, `/assets/*` |

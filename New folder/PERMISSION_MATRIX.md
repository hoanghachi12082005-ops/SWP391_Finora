# PERMISSION MATRIX

**Project:** KiotRetail (SWP391_Finora)
**Source:** `filter/SecurityFilter.java` — `ROLE_MAP` (static LinkedHashMap)
**Last updated:** 2026-07-01

---

## URL Prefix → Role Access Matrix

| URL Prefix | Admin | Owner | StoreManager | SalesStaff | WarehouseStaff |
|-----------|-------|-------|-------------|------------|---------------|
| `/system/` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `/management/` | ✅ | ✅ | ✅ | ❌ | ✅ |
| `/pos/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/owner/` | ❌ | ✅ | ❌ | ❌ | ❌ |
| `/admin/` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `/manager/` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `/branch/` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `/supplier/` | ✅ | ✅ | ✅ | ❌ | ✅ |
| `/purchase/` | ✅ | ✅ | ✅ | ❌ | ✅ |
| `/finance/` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `/activity/` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `/settings` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `/report/` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `/inventory/` | ✅ | ✅ | ✅ | ❌ | ✅ |
| `/warehouse/` | ✅ | ✅ | ✅ | ❌ | ✅ |
| `/product/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/category/` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `/customer/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/sales/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/cart/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/checkout/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/orders/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/print/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/search-product` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/cash-transaction` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `/dashboard/` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `/revenue/` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `/shift/` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/profile/` | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## Public (Unauthenticated) Paths

| Path | Purpose |
|------|---------|
| `/login` | Login page + POST |
| `/logout` | Logout handler |
| `/forgot-password` | Password reset flow |
| `/register` | Registration (if enabled) |
| `/role-selection` | Role selection after login |
| `/assets/` | Static assets (CSS, images, fonts) |
| `/css/` | CSS files |
| `/js/` | JavaScript files |
| `/static/` | Static resources |

---

## Access Count Summary

| Role | Accessible Prefixes |
|------|-------------------|
| **Owner** | 27 (excluded: `/owner/` is owner-only, so 26 non-owner prefixes + 1 owner-only = 27 total accessible via role mapping) |
| **Admin** | 25 (26 prefixes minus `/owner/` owner-only) |
| **StoreManager** | 22 (most operational functions) |
| **SalesStaff** | 12 (POS, customer, order, cart, checkout, print, product, profile, search, shift) |
| **WarehouseStaff** | 9 (inventory, warehouse, management, supplier, purchase, profile) |

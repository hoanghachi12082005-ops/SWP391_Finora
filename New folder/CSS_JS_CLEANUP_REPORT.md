# CSS & JS CLEANUP REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## 1. CSS FILE INVENTORY

### Project CSS (16 files, ~6,010 lines, ~170KB)

| File | Lines | Status | Action |
|------|-------|--------|--------|
| `base.css` | 11 | Useless (only imports) | ❌ Delete or merge |
| `layout.css` | 10 | Minified single line, partial overlap with components.css | ⚠️ Merge into components.css |
| `theme.css` | 82 | Colors + scrollbars + dangerous `.message { display: none }` | ✅ Keep, fix `.message` bug, unify colors |
| `components.css` | 962 | "Dung's Compatibility Layer" dead code, duplicate layout rules | ⚠️ Remove compatibility layer, deduplicate |
| `common.css` | 139 | Pre-dates components.css, mostly superseded | ❌ Delete (functionality in components.css) |
| `login-custom.css` | 139 | Auth page styling | ✅ Keep |
| `forgot-password.css` | 357 | Auth page styling | ✅ Keep |
| `role-selection.css` | 150 | Unused (no role-selection JSP exists) | ❌ Delete |
| `form-modal.css` | 61 | Custom modal styles (Gen 2 only) | ⚠️ Keep until Gen 2 modals migrated |
| `sales.css` | 1,186 | POS layout — independent variable system | ✅ Keep (standalone module) |
| `inventory.css` | 229 | Inventory module | ✅ Keep |
| `branch.css` | 560 | Branch module — hardcoded colors | ⚠️ Refactor to use theme.css vars |
| `customer-management.css` | 617 | Customer module | ✅ Keep |
| `user-management.css` | 648 | User module | ✅ Keep |
| `profile.css` | 528 | Profile module | ✅ Keep |
| `employee-sales-report.css` | 341 | Report module | ✅ Keep |

### Vendor CSS (Font Awesome 7.2.0 — 20 files)

**All vendor files are unmodified.** Only `all.min.css` or `all.css` is needed if Font Awesome icons are actually used in JSPs. Currently FontAwesome is only referenced in `branch-list.jsp` and `suppliers/list.jsp` — these could be migrated to Material Icons (already loaded on every page).

### Potential CSS Savings

| Action | Lines Removed | KB Saved |
|--------|--------------|----------|
| Delete `base.css` | 11 | ~0.3KB |
| Delete `common.css` | 139 | ~3KB |
| Delete `role-selection.css` | 150 | ~4KB |
| Remove compatibility layer from `components.css` | ~50 | ~1.5KB |
| Remove unused Font Awesome assets (CSS+JS+SVG) | ~10,000+ | ~5MB |
| **Total potential savings** | **~10,350** | **~5MB** |

---

## 2. CSS DUPLICATIONS

| Duplicated Rule | Files |
|----------------|-------|
| `.sidebar { width: 260px; ... }` | `common.css:25-50`, `components.css:1-30` |
| `.top-header { height: 70px; ... }` | `common.css:60-78`, `components.css:100-130` |
| `.kpi-card { ... }` | `components.css:250-300`, `customer-management.css:80-120`, `user-management.css:80-120`, `employee-sales-report.css:60-100` |
| `.filter-grid { ... }` | `customer-management.css:130-160`, `user-management.css:130-160`, `employee-sales-report.css:110-140` |
| `.table-card { ... }` | `customer-management.css:170-200`, `user-management.css:170-200`, `employee-sales-report.css:150-180` |
| Overview card section | 4 files have nearly identical structure (customer, user, report, profile) |

---

## 3. CSS ISSUES

| Issue | Severity | Location |
|-------|----------|----------|
| `.message { display: none !important; }` | **Critical** | `theme.css:81` — hides all flash messages on Gen 2 pages |
| `Dung's Compatibility Layer` — dead code | Medium | `components.css:798-850` — maps old→new class names, all unused |
| `!important` usage | Medium | `branch.css` has 15+ `!important` declarations |
| Hardcoded colors bypassing CSS variables | Medium | `branch.css` uses `#8b0000` instead of `var(--primary-color)`; `sales.css` uses `#af101a` instead of `#93000b` |
| WebKit-only scrollbar styling | Low | `theme.css:70-78` — `::-webkit-scrollbar` doesn't work in Firefox |
| `role-selection.css` defined but no JSP uses it | Low | 150 lines of dead CSS |

---

## 4. JS FILE INVENTORY

### Custom JS (2 files)

| File | Lines | Status | Action |
|------|-------|--------|--------|
| `assets/js/main.js` | 21 | Auto-dismiss toasts | ✅ Keep |
| Inline `<script>` blocks in JSPs | ~1,000+ total | Fragmented across 12+ files | ⚠️ Consolidate |

### Vendor JS (15 files)

| File | Size | Used? | Action |
|------|------|-------|--------|
| `jquery-3.7.0.min.js` (CDN) | ~87KB | ❌ Not used anywhere | ❌ Remove from `footer.jsp` |
| `bootstrap.bundle.min.js` (CDN) | ~72KB | ✅ Bootstrap modals, collapse | ✅ Keep |
| `sweetalert2@11` (CDN) | ~40KB | ✅ Toast messages | ✅ Keep |
| `chart.js` (CDN) | ~130KB | ⚠️ Only used in `payments/list.jsp` | ⚠️ Consider lazy loading |
| `tailwindcss` (CDN) | ~300KB | ⚠️ Only used in `sales/*.jsp` | ⚠️ Consider removing if migrating to common CSS |
| Font Awesome JS (14 files) | ~500KB | ❌ Only 2-3 icons used in branch/list | ❌ Replace with Material Icons |

### Potential JS Savings

| Action | KB Saved |
|--------|----------|
| Remove jQuery | ~87KB |
| Remove Font Awesome JS | ~500KB |
| Remove Tailwind CDN (if migrating) | ~300KB |
| Lazy-load Chart.js | ~130KB |
| **Total potential savings** | **~1MB per page load** |

---

## 5. JS DUPLICATIONS

| Duplicated Pattern | Files | Lines of Duplication |
|--------------------|-------|---------------------|
| Debounced search → dropdown → add row | `_tab_stock.jsp`, `_tab_transfer_create.jsp` | ~200 lines each (~70% overlap) |
| Modal CRUD open/close | `products/index.jsp`, `suppliers/list.jsp`, `categories/list.jsp` | ~30 lines each |
| `formatCurrency()` | `import.jsp`, `_tab_stock.jsp`, `payments/list.jsp`, `order-history.jsp` | ~5 lines each, 4 implementations |
| `console.warn("Bootstrap JS not loaded")` | `products/index.jsp`, `suppliers/list.jsp` | 2 copies of identical guard |

---

## 6. JS ISSUES

| Issue | Severity | Location |
|-------|----------|----------|
| jQuery loaded but never used | Medium | `footer.jsp:6` — dead dependency |
| `console.warn` in production | Low | `products/index.jsp:285`, `suppliers/list.jsp:315` |
| `alert()`/`confirm()` instead of SweetAlert2 | Medium | `products/index.jsp`, `inventory/import.jsp`, `suppliers/list.jsp` |
| `console.error` in production | Low | `order-history.jsp:350` |
| Duplicate AJAX search + drop-down logic | Medium | `_tab_stock.jsp` + `_tab_transfer_create.jsp` |
| No JS bundling/minification | Medium | All JS is raw `<script>` tags — no webpack/rollup/vite |
| No CSP nonce/hash on inline scripts | Medium | All pages execute inline `<script>` without CSP validation |
| Vanilla `fetch()` doesn't handle network errors | Low | `_tab_stock.jsp`, `_tab_transfer.jsp` — no `.catch()` on fetch calls |

---

## 7. CDN DEPENDENCY SUMMARY

| Library | Source | Version | Used By | Recommendation |
|---------|--------|---------|---------|---------------|
| Bootstrap CSS | CDN | 5.3.0 | Most pages | ✅ Keep (or vendor) |
| Bootstrap JS | CDN | 5.3.0 | Most pages | ✅ Keep |
| jQuery | CDN | 3.7.0 | **None** | ❌ Remove |
| SweetAlert2 | CDN | 11 | footer.jsp | ✅ Keep |
| Chart.js | CDN | Latest | payments/list.jsp only | ⚠️ Lazy-load |
| Tailwind CSS | CDN | Latest | sales/*.jsp only | ⚠️ Remove if sales migrated |
| Material Icons | Google Fonts | Latest | All pages | ✅ Keep |
| Material Symbols | Google Fonts | Latest | Some pages | ✅ Keep |
| Inter + Manrope | Google Fonts | Latest | header.jsp | ✅ Keep |
| Font Awesome | Local files | 7.2.0 | branch/list, suppliers/list | ❌ Replace with Material Icons |

---

## 8. CROSS-CUTTING REMEDIATIONS

| Priority | Action | Effort |
|----------|--------|--------|
| **P0** | Remove `.message { display: none !important; }` from theme.css | 1 line |
| **P0** | Remove jQuery from footer.jsp | 1 line |
| **P1** | Delete dead CSS files (base.css, common.css, role-selection.css) | 3 file deletions |
| **P1** | Remove Font Awesome vendor assets if icons migrated to Material Icons | ~5,000 files |
| **P2** | Consolidate `_tab_stock.jsp` and `_tab_transfer_create.jsp` JS into shared utility | ~150 lines saved |
| **P2** | Extract shared `formatCurrency()` to main.js | 5 lines added, 20 removed |
| **P2** | Remove unused Font Awesome SVGs from deployment | ~5MB savings |
| **P3** | Remove "Dung's Compatibility Layer" from components.css | ~50 lines |
| **P3** | Replace `console.warn`/`alert` with SweetAlert2 | 10 lines |
| **P3** | Extract shared modal CRUD functions to main.js | ~50 lines consolidated |

---

## 9. FILE CLEANUP PLAN

### Delete (confirmed unused/dead)
1. `assets/css/base.css` — only imports, delete and inline in `header.jsp`
2. `assets/css/common.css` — superseded by `components.css`
3. `assets/css/role-selection.css` — no JSP uses it
4. `assets/css/layout.css` — merge 10 lines into `components.css`
5. Font Awesome vendor files IF all icons migrated to Material Icons

### Refactor
1. `components.css` — remove Dung's Compatibility Layer (lines 798-850)
2. `branch.css` — replace `#8b0000` with `var(--primary-color)`, remove `!important` usage
3. `theme.css` — fix `.message { display: none }`, unify primary color to `#93000b`
4. `sales.css` — align `--primary` variable with `theme.css` `--primary-color`

### Merge
1. `profile.css` global classes (`.btn-primary`, `.btn-secondary`, `.alert`) — should be scoped or moved to `components.css`
2. Consolidate duplicated JS in `_tab_stock.jsp` and `_tab_transfer_create.jsp` into `main.js`

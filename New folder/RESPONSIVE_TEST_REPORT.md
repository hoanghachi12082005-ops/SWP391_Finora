# RESPONSIVE TEST REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01
**Method:** Static audit (no browser testing)

---

## 1. SIDEBAR BEHAVIOR

| Viewport | Expected Behavior | Current Behavior | Issue |
|----------|------------------|-----------------|-------|
| Desktop >1200px | Fixed sidebar visible | ✅ Fixed 260px sidebar | — |
| Laptop 1024-1199px | Sidebar visible | ✅ Sidebar visible | — |
| Tablet 768-1023px | Collapsed sidebar or overlay | ❌ Sidebar takes 260px, pushing content | Content area too narrow |
| Mobile <768px | Off-canvas/hamburger | ❌ No mobile navigation at all | Sidebar covers entire screen |

**No media queries for sidebar** in any CSS file. No hamburger toggle exists.

---

## 2. TABLE RESPONSIVENESS

| Page | Horizontal Scroll? | Responsive Behavior | Issue |
|------|-------------------|-------------------|-------|
| user-list | ❌ No wrapper | `table-card` has no overflow | Columns overflow on <1024px |
| customer-list | ❌ No wrapper | Same | Overflow |
| suppliers/list | ❌ No wrapper | Bootstrap table | Bootstrap tables don't wrap by default |
| products/index | ❌ No wrapper | Bootstrap table | Same |
| categories/list | ❌ No wrapper | Bootstrap table | Same |
| inventory/stock tab | ❌ No wrapper | Custom tab | 8+ columns, def overflows |
| branch-list | ❌ No wrapper | Custom | 6+ columns |
| employee-sales | ❌ No wrapper | Same as user-list | Overflow |

**No page uses `overflow-x: auto` on table containers.** Tables with 5+ columns will overflow on any screen <1024px.

---

## 3. RESPONSIVE CSS COVERAGE

| CSS File | Has Media Queries? | Breakpoints | What's Adjusted |
|----------|-------------------|-------------|-----------------|
| `components.css` | ✅ | 1024px, 640px | KPI grid columns |
| `forgot-password.css` | ✅ | 991px, 768px, 576px | Layout, font-size, button width |
| `customer-management.css` | ✅ | 1200px, 768px, 640px | Overview grid, filter, form, table footer |
| `user-management.css` | ✅ | 1200px, 768px, 640px | Same as customer |
| `profile.css` | ✅ | 992px, 768px, 480px | Profile grid, hero, form, avatar |
| `employee-sales-report.css` | ✅ | 1200px, 768px, 640px | Overview grid, table footer |
| `sales.css` | ❌ | None | POS layout — fixed panel widths |
| `inventory.css` | ❌ | None | Tab layout, warehouse cards |
| `branch.css` | ❌ | None | Fixed-width filter selects |
| `common.css` | ❌ | None | Fixed sidebar 260px |
| `login-custom.css` | ❌ | None | Two-column layout |
| `theme.css` | ❌ | None | Variables only |

---

## 4. LAYOUT BREAKDOWN POINTS

| Page Family | Breaks at | Reason |
|-------------|-----------|--------|
| **Gen 2 pages** (user, customer, report) | <1024px | `filter-grid` has fixed-width columns, `data-table` has no overflow, overview grid collapses at 1200px ✅ |
| **Gen 1 pages** (supplier, product, category) | <768px | Bootstrap `col-md-*` grid works; but overflowing tables still a problem |
| **Branch** | <1024px | Fixed 124px filter selects + no overflow on branch-table |
| **POS (sales.jsp)** | <1280px | Fixed 400px cart panel + 80px sidebar = barely fits at 1280px |
| **Auth (login/forgot)** | <768px | Two-column layout has no responsive adaptation — right column becomes very cramped |
| **Inventory** | <1024px | Tab navigation + inline filters no breakpoint handling |
| **Dashboard/owner** | <1024px | KPI grid collapses at 1024px ✅, but chart containers have fixed widths |

---

## 5. VIEWPORT META TAG

| File | Has `<meta name="viewport">`? |
|------|-------------------------------|
| `common/header.jsp` | ✅ Yes (`content="width=device-width, initial-scale=1.0"`) |
| Gen 2 self-hosted headers | ✅ Check needed per page |
| `branch/header.jsp` | Needs checking |

Pages using `common/header.jsp` have correct viewport meta. Gen 2 pages self-host their `<head>` and should be verified.

---

## 6. CRITICAL RESPONSIVE ISSUES

| Severity | Issue | Affected Pages |
|----------|-------|---------------|
| **Critical** | Sidebar has no collapse/overlay mechanism for tablets | All pages with sidebar |
| **Critical** | No mobile navigation (hamburger menu) | All pages |
| **High** | Tables have no horizontal scroll container | All table pages |
| **High** | POS sale page has fixed 400px panel — unusable below 1024px | sales.jsp |
| **High** | Auth pages' two-column layout has no single-column fallback | login.jsp, forgot-password.jsp |
| **Medium** | Inventory tab navigation may overflow on tablet | inventory.jsp |
| **Medium** | Branch filter selects have fixed width (124px) | branch-list.jsp |
| **Low** | Dashboard chart containers have hardcoded widths | dashboard/owner.jsp |
| **Low** | No `max-width: 100%` on product images | products/index.jsp |

---

## 7. RESPONSIVE SCORECARD

| Device | Sidebar | Tables | Forms | POS | Auth | Overall |
|--------|---------|--------|-------|-----|------|---------|
| Desktop 1920px | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Laptop 1366px | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Laptop 1024px | ⚠️ cramped | ⚠️ overflow | ✅ | ❌ fails | ⚠️ cramped | ⚠️ |
| Tablet 768px | ❌ broken | ❌ overflow | ✅ | ❌ fails | ❌ broken | ❌ |
| Mobile 375px | ❌ broken | ❌ overflow | ⚠️ | ❌ fails | ❌ broken | ❌ |

**Responsive Rating:** ❌ Poor. The application is effectively desktop-only.

# UI/UX AUDIT REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01
**Author:** Principal Frontend Architect

---

## 1. LAYOUT ARCHITECTURE

### Three Coexisting Layout Generations

The application has **3 distinct layout generations** across its 68 JSP files, indicating work by multiple developers without a unified template.

#### Generation 1 (Bootstrap-based, via `header.jsp`)
| Pages | Structure | CSS |
|-------|-----------|-----|
| suppliers/list, products/index, categories/list, inventory/inventory, activity-log/list, dashboard/owner, pos/sale | `app-container > main-content > container-fluid` | `theme.css`, `components.css`, Bootstrap 5 CDN |

#### Generation 2 (Custom CSS, self-contained)
| Pages | Structure | CSS |
|-------|-----------|-----|
| user-list, customer-list, employee-sales-report, profile | `app-layout > main-wrapper > page-content` | `base.css`, `layout.css`, `form-modal.css`, module CSS |

#### Generation 3 (Standalone)
| Pages | Structure | CSS |
|-------|-----------|-----|
| branch-list, sales, auth/login, forgot-password | Custom per-page | Branch: `common.css`+`branch.css`; Sales: Tailwind CDN; Auth: `login-custom.css`/`forgot-password.css` |

**Impact:** Users perceive inconsistent spacing, different button styles, and varying page layouts across modules. The same app feels like 3 different products.

### Component Inclusion Matrix

| Component | Gen 1 Pages | Gen 2 Pages | Branch | Sales |
|-----------|-------------|-------------|--------|-------|
| `common/header.jsp` | ✅ | ❌ | ❌ (own `branch/header.jsp`) | ❌ |
| `common/sidebar.jsp` | ✅ | ✅ | ✅ | ✅ |
| `common/topbar.jsp` | ✅ (most) | ❌ | ❌ | ❌ |
| `common/footer.jsp` | ✅ | ❌ | ❌ | ❌ |
| SweetAlert2 toasts | ✅ (via footer) | ❌ | ❌ | ❌ |

**Risk:** Gen 2 pages (user-list, customer-list, profile, employee-sales) lack SweetAlert2 session message handling. Flash messages may appear as raw text or be invisible.

---

## 2. SIDEBAR REVIEW

### Strengths
- Role-based menu visibility correctly implemented for all 5 roles
- Active page highlighting works via `originalUri` comparison
- Collapsible sub-menus for Products and Sales sections
- User profile block at bottom with avatar initials, name, role, profile/logout links
- Proper exit: all links use `${pageContext.request.contextPath}`

### Issues
- **Fallback defaults**: `fullName` defaults to "Lê Minh Quân" when null — should be "Người dùng"
- **Duplicate menu label**: Admin sees "Quản lý Owner" label for `/admin/user` but it manages all employees, not just owners
- **Missing menu items**: No link for `/customers`, `/cashbook` is present but `/payments`/`/invoices` are missing
- **Reports link** points to `/reports/export` (a stub) instead of `/reports/employee-sales`
- **SalesStaff** has no link to Customers (`/customers`) despite having role access
- **WarehouseStaff** missing product management link despite having access
- **StoreManager** sees "Quản lý Owner" incorrectly (Admin only menu)

### Line Count
`sidebar.jsp` = 188 lines, well-organized, no unnecessary duplication.

---

## 3. HEADER / TOPBAR REVIEW

### `common/topbar.jsp`
- Global search bar (placeholder only — no JS wire-up)
- Notification bell with dot badge (non-functional placeholder)
- Help and Settings buttons (non-functional placeholders)
- Role-based CTA: Sales roles get "Bán hàng" → `/orders/create` (404 — no such servlet); WarehouseStaff gets "Nhập kho" → `/inventory/import` (exists)
- **Broken link**: "Bán hàng" button points to `/orders/create` which does not exist

### `common/header.jsp`
- Uses JSTL 1.x URI (`http://java.sun.com/jsp/jstl/core`) instead of Jakarta URI (`jakarta.tags.core`)
- Loads 3 Google Font families (Inter, Manrope, Material Icons) + Material Symbols
- Bootstrap 5.3.0 CDN
- Missing: favicon link, no `<meta name="description">`, no `<meta name="theme-color">`

---

## 4. TABLE STANDARDIZATION

### Current State: 3 Table Systems

| System | Used By | Classes | Features |
|--------|---------|---------|----------|
| Custom `data-table` | user-list, customer-list, employee-sales | `data-table`, `table-card` | Rich empty states, action icons |
| Bootstrap `table` | suppliers, products, categories, activity-log | `table align-middle table-hover` | Standard Bootstrap |
| Custom `branch-table` | branch-list | `branch-table` | Unique color scheme |

### Missing Features (All Pages)
- ❌ No sticky headers
- ❌ No alternating row colors (no `:nth-child` or `table-striped`)
- ❌ No row hover highlighting (except Bootstrap `table-hover`)
- ❌ No responsive horizontal scroll wrapper
- ❌ No column sorting indicators
- ❌ No selectable rows (except order-history.jsp)

### Empty States
- Gen 2 pages have rich empty states with Material icon + `<h4>` title + `<p>` description
- Gen 1 pages have minimal text-only empty rows
- Branch has no empty state (loop with no `empty` fallback)

---

## 5. FORM STANDARDIZATION

### Current State: 3 Form Systems

| System | Used By | Layout | Controls |
|--------|---------|--------|----------|
| Custom `filter-card` | user-list, customer-list, employee-sales | `filter-grid` with `form-group` divs | `<input>`, `<select>`, custom `.btn-primary` |
| Bootstrap `card shadow-sm` | suppliers, products, categories, activity-log | Bootstrap `row/col` or `d-flex gap-2` | `form-control`, `form-select`, Bootstrap `.btn` |
| Custom `filter-branch` | branch-list | Inline `filter-branch` classes | `filter-branch-select`, custom `.btn-filter` |

### Validation Issues
- Most forms lack server-side validation feedback on the form itself (errors shown as flash messages, not inline)
- Client-side validation uses `alert()` in 3 files (products, import.jsp, suppliers)
- No `required` attributes on `<select>` dropdowns
- No `inputmode` or `type` attributes for phone/email fields
- Date inputs use `type="date"` (good) but no min/max constraints

---

## 6. BUTTON CONSISTENCY

### Current State: 4 Button Systems

| Style | Used By | Examples |
|-------|---------|----------|
| Custom `.btn-primary`/`.btn-secondary` | user-list, customer-list, employee-sales | Red primary, gray secondary |
| Bootstrap `.btn.btn-danger`/`.btn.btn-outline-secondary` | suppliers, products, categories | Bootstrap danger red |
| Custom `.btn-add`/`.btn-edit`/`.btn-delete`/`.btn-view` | branch-list | Unique per-action colors |
| Tailwind utility classes | sales.jsp | `bg-primary`, `text-on-primary` |

### Inconsistent Action Labels
| Action | Gen 2 (custom) | Gen 1 (Bootstrap) | Branch |
|--------|---------------|-------------------|--------|
| Create | "Thêm mới" (icon + text) | "+ Thêm mới" | "Thêm" |
| Edit | Material `edit` icon | "Sửa" text | FontAwesome `fa-pen` |
| Delete | Material `delete` icon | "Xóa" text | FontAwesome `fa-trash` |
| View | Material `visibility` icon | "Chi tiết" text | ✅ consistent |

---

## 7. MODAL CONSISTENCY

### Current State: 3 Modal Systems

| System | Used By | Implementation |
|--------|---------|----------------|
| Custom `modal-overlay` | user-list, customer-list | CSS fixed overlay + `<form class="modal-box">` |
| Bootstrap `modal fade` | suppliers, products, categories, inventory, activity-log | Bootstrap standard modal with backdrop |
| Custom JS + inline | branch-list | `openDeleteModal()` JS function, no Bootstrap |

### Issues
- Custom modals (Gen 2) lack: keyboard `Escape` support, focus trap, click-outside-to-close, animation
- Bootstrap modals are well-behaved but some have `z-index` overrides that break stacking
- No single shared modal component/tag file
- `branch-list.jsp` delete confirmation uses a unique implementation that shows "Gõ 'XÁC NHẬN' để xóa" (type CONFIRM to delete)

---

## 8. PAGINATION CONSISTENCY

### Current State: 3 Pagination Systems

| System | Used By | Implementation |
|--------|---------|----------------|
| `common/pagination.jsp` | suppliers, products, categories, activity-log, inventory | Bootstrap `pagination` classes, reusable include |
| Custom `page-btn` inline | user-list, customer-list, employee-sales | ~60 lines each of inline JSTL + custom CSS |
| Custom form-based | branch-list | Form submit with page number, custom styling |

**Issue:** 3 files clone ~60 lines of identical pagination code instead of reusing `common/pagination.jsp`.

---

## 9. CSS ARCHITECTURE

### File Organization
- 16 project CSS files totaling ~6,010 lines (~170KB)
- Font Awesome 7.2.0 vendor files (20 CSS + 14 JS + 4 webfonts + ~5,000 SVGs) — **mostly unused**

### Key Issues
1. **Two competing primary colors**: `#93000b` (theme.css) vs `#8b0000` (branch.css) vs `#af101a` (sales.css)
2. **Dung's Layout Compatibility Layer**: `components.css` has an entire compatibility block mapping two class naming systems — adds ~50 lines of dead complexity
3. **Hardcoded magic values throughout**: No consistent spacing/radius scale despite `--radius-*` vars existing
4. **`.message { display: none !important; }`** in `theme.css:81` — hides ALL elements with class `message`, likely breaking some UI feedback
5. **No print stylesheet** — printing any page may produce unpredictable results
6. **No dark mode** support
7. **Scrollbar styling**: WebKit-only (`::-webkit-scrollbar`), doesn't work in Firefox/Edge
8. **vendor/fontawesome/svgs/ + svgs-full/**: ~5,000 SVG icon files bundled with the app despite only a handful being used

---

## 10. JAVASCRIPT ARCHITECTURE

### Custom JS
- Only 1 custom JS file: `assets/js/main.js` (21 lines — auto-dismiss toasts)
- All other JS is inline `<script>` blocks in JSP files

### Issues
1. **jQuery 3.7.0 loaded but never used** — dead dependency adding ~87KB to every page
2. **2 `console.warn` statements left in production** (products/index.jsp, suppliers/list.jsp)
3. **`alert()` and `confirm()` in 3 files** instead of SweetAlert2 (already loaded)
4. **70% code duplication** between `_tab_stock.jsp` and `_tab_transfer_create.jsp` (search → dropdown → add row pattern)
5. **Modal CRUD pattern duplicated** across products, suppliers, and categories
6. **`formatCurrency()` reinvention**: 4+ files implement locale formatting independently
7. **No form validation library** — all validation is ad-hoc

### Hardcoded CDN Dependencies
| Library | Size | Actually Used? |
|---------|------|---------------|
| Bootstrap 5.3.0 bundle | ~72KB | ✅ Yes |
| jQuery 3.7.0 | ~87KB | ❌ No |
| SweetAlert2 11 | ~40KB | ✅ Yes |
| Chart.js | ~130KB | ✅ (payments/list.jsp only) |
| Tailwind CSS | ~300KB+ | ✅ (sales/*.jsp only) |

---

## 11. RESPONSIVE DESIGN

### Pages with Media Queries
- `components.css` (1024px, 640px) — KPI grid, dashboard grid
- `forgot-password.css` (991px, 768px, 576px) — layout adjustments
- `customer-management.css` (1200px, 768px, 640px) — overview grid, filter, form, table
- `user-management.css` (1200px, 768px, 640px) — same
- `profile.css` (992px, 768px, 480px) — profile grid, hero, avatar
- `employee-sales-report.css` (1200px, 768px, 640px) — overview grid, table

### Pages WITHOUT Media Queries
- ❌ `inventory.css` — no responsive adjustments
- ❌ `branch.css` — fixed layout, no breakpoints
- ❌ `sales.css` — POS layout with fixed-width panels (400px cart, 80px sidebar)
- ❌ `common.css` — fixed-width sidebar (260px), no responsive handling
- ❌ `login-custom.css` — no breakpoints despite having a two-column layout
- ❌ `suppliers/list.jsp`, `products/index.jsp`, `categories/list.jsp` — rely on Bootstrap's responsive grid (`col-md-*`), so they work at a basic level

### Critical Responsive Gaps
- Sidebar is fixed 260px with no collapse mechanism on tablets
- Inventory stock table has no horizontal scroll wrapper
- POS sale page has fixed `400px` cart panel — unusable on screens <1024px
- Branch list has no responsive table handling
- No mobile navigation (hamburger menu, off-canvas sidebar)

---

## 12. ACCESSIBILITY

### Issues Found
1. **No `lang` attribute on most pages** — only `header.jsp` sets `<html lang="vi">`, but Gen 2 pages self-host `<head>` and may skip it
2. **No skip-to-content links** — keyboard users must tab through entire sidebar on every page
3. **Low contrast** — `#64748b` (slate-500) on `#ffffff` fails WCAG AA for small text (contrast ratio ~4.1:1, needs 4.5:1)
4. **Missing `<label>` associations** — many `<input>` elements rely on placeholder as the only label (bad for screen readers)
5. **Empty `alt` attributes on decorative icons** — Material Icons and FontAwesome icons lack `aria-hidden="true"`
6. **No focus indicators** on custom-styled buttons and links
7. **Interactive elements not keyboard-accessible** — custom modals (Gen 2) don't trap focus or close on Escape
8. **Color-only status indicators** — stock warnings use red background/text only; no icon or text label alternative
9. **No `aria-live` regions** — dynamic content updates (AJAX search results, toast messages) not announced by screen readers
10. **Form error messages not associated with fields** — `aria-describedby` not used

---

## 13. LOCALIZATION

### Strengths
- All text is Vietnamese (as expected)
- Currency formatting uses Vietnamese locale (`vi_VN`)
- Date formatting in most places

### Issues
1. **Inconsistent terminology**: "Cửa hàng" (store) vs "Chi nhánh" (branch) used interchangeably
2. **Inconsistent "Xóa" vs "Xoá"**: Both diacritic variants appear
3. **Mixed English/Vietnamese in class names**: `data-table` (English) vs `btn-ban` (Vietnamese, meaning "deactivate")
4. **No i18n framework** — all strings hardcoded in JSPs
5. **Hardcoded numbers assumed VND** — no currency symbol/format abstraction

---

## 14. ERROR PAGES

### Status
- ❌ No custom 404 page
- ❌ No custom 403 page
- ❌ No custom 500 page
- ❌ No session-expired page
- ❌ No web.xml error-page configuration

All errors produce Tomcat's default HTML error pages, which don't match the app design and may leak server information.

---

## SUMMARY OF CRITICAL UI/UX ISSUES

| Severity | Issue | Pages Affected |
|----------|-------|---------------|
| **High** | 3 layout generations with different wrappers, CSS, and component includes | All 68 JSPs |
| **High** | Gen 2 pages lack common/footer.jsp — no SweetAlert2 flash messages | user-list, customer-list, profile, employee-sales |
| **High** | Topbar "Bán hàng" CTA → `/orders/create` (404) | All sales-role users see broken link |
| **High** | `.message { display: none !important; }` hides all flash message elements | Pages with `<div class="message">` |
| **Medium** | jQuery loaded but not used (+87KB per page) | All pages |
| **Medium** | No responsive sidebar / mobile navigation | All sidebar pages |
| **Medium** | Duplicate pagination code in 3 files (180 lines total) | user-list, customer-list, employee-sales |
| **Medium** | 3 modal systems (custom/Bootstrap/branch) | All modal-using pages |
| **Medium** | alert()/confirm() instead of SweetAlert2 | products, import, suppliers |
| **Medium** | console.warn in 2 production files | products, suppliers |
| **Low** | No error pages (403/404/500) | All |
| **Low** | No favicon | All |
| **Low** | Print stylesheet missing | All |
| **Low** | No link to Customers page for SalesStaff | sidebar.jsp |

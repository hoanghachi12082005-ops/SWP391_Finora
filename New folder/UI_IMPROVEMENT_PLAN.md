# UI IMPROVEMENT PLAN

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## Priority Legend

| Priority | Definition |
|----------|------------|
| **P0** | Bug that breaks UI functionality |
| **P1** | Major inconsistency affecting user experience |
| **P2** | Visual polish and code quality |
| **P3** | Enhancement / nice-to-have |

---

## P0 — CRITICAL UI BUGS (Fix Immediately)

### 1. Flash Messages Hidden by CSS
- **Issue:** `theme.css:81` — `.message { display: none !important; }` hides all flash messages on Gen 2 pages (user-list, customer-list, profile, employee-sales)
- **Fix:** Delete or comment out this line
- **Effort:** 1 line
- **Risk:** None

### 2. Topbar "Bán hàng" Button → 404
- **Issue:** `topbar.jsp` CTA links to `/orders/create` — no such servlet exists
- **Fix:** Change to `/sales`
- **Effort:** 1 line
- **Risk:** None

### 3. jQuery Loaded But Never Used
- **Issue:** `footer.jsp` loads jQuery 3.7.0 (~87KB) but no project code uses it
- **Fix:** Remove `<script src="...jquery-3.7.0.min.js">` from footer.jsp
- **Effort:** 1 line
- **Risk:** Low (verify no inline scripts use `$()` — none found)

---

## P1 — HIGH PRIORITY (This Sprint)

### 4. Add `footer.jsp` to All Pages Missing It
- **Issue:** Gen 2 pages (user-list, customer-list, profile, employee-sales) don't include `common/footer.jsp`, so SweetAlert2 toast messages and session flash handler are missing
- **Files:** user-list.jsp, customer-list.jsp, profile.jsp, employee-sales.jsp
- **Fix:** Add `<jsp:include page="/views/common/footer.jsp" />` before `</body>`
- **Effort:** 4 files, 1 line each

### 5. Standardize Wrapper to One Layout
- **Issue:** 3 different wrapper div patterns across pages
- **Fix:** Migrate Gen 2 pages from `app-layout > main-wrapper > page-content` to `app-container > main-content > container-fluid`
- **Effort:** ~2 hours (4 files: user-list, customer-list, profile, employee-sales)
- **Risk:** Low — structural div change only

### 6. Use `common/pagination.jsp` Everywhere
- **Issue:** 3 files (user-list, customer-list, employee-sales) have ~60 lines each of inline pagination
- **Fix:** Replace inline pagination with `<jsp:include page="/views/common/pagination.jsp">`
- **Effort:** ~1 hour (3 files)

### 7. Remove Font Awesome, Use Material Icons
- **Issue:** FontAwesome (~5,000 SVG files, ~500KB JS) only used in `branch-list.jsp` and `suppliers/list.jsp`. Material Icons already loaded on every page.
- **Fix:** Replace `<i class="fa-solid fa-*">` with `<span class="material-icons">*</span>` in branch-list.jsp
- **Effort:** ~30 minutes

### 8. Add Page-Scoped Responsive Table Wrapper
- **Issue:** All data tables lack `overflow-x: auto` — columns overflow on <1024px screens
- **Fix:** Wrap all `<table>` elements with `<div class="table-responsive">` (Bootstrap class, available)
- **Effort:** ~1 hour (all table JSPs)

### 9. Add Sidebar Collapse for Mobile
- **Issue:** Sidebar is fixed 260px with no collapse/overlay mechanism
- **Fix:** Add a hamburger toggle button + CSS overlay for <992px screens
- **Effort:** ~3-4 hours

### 10. Unify Button Classes Project-Wide
- **Issue:** 4 button systems coexist
- **Fix:** Migrate all buttons to Bootstrap `.btn` classes: primary action = `.btn.btn-danger`, secondary = `.btn.btn-outline-secondary`
- **Effort:** ~2 hours (all JSPs)

### 11. Add `aria-hidden="true"` to All Material Icons
- **Issue:** Screen readers announce every Material Icon name
- **Fix:** Search-and-replace `<span class="material-icons">` → `<span class="material-icons" aria-hidden="true">`
- **Effort:** 15 minutes

### 12. Remove `console.warn` Statements
- **Issue:** 2 files have development `console.warn("Bootstrap JS is not loaded!")`
- **Files:** `products/index.jsp:285`, `suppliers/list.jsp:315`
- **Fix:** Delete lines
- **Effort:** 2 lines

---

## P2 — MEDIUM PRIORITY (Next Sprint)

### 13. Remove Dead CSS Files
- **Files:** `base.css`, `common.css`, `role-selection.css`
- **Effort:** 3 file deletions + update header.jsp imports

### 14. Standardize Table Classes
- **Pick one:** Use `table align-middle table-hover` (Bootstrap, Gen 1) project-wide
- **Migrate:** Gen 2 `data-table` → Bootstrap table; branch `branch-table` → Bootstrap table
- **Effort:** ~2 hours

### 15. Standardize Modal System
- **Pick one:** Use Bootstrap `modal fade` project-wide
- **Migrate:** Gen 2 custom modals (user-list, customer-list) → Bootstrap modals
- **Effort:** ~3 hours

### 16. Fix `.message` Bug and Consolidate Flash Messages
- **Fix:** After removing `display: none`, ensure all pages use SweetAlert2 toasts (via footer.jsp) instead of inline `<div class="message">`
- **Effort:** ~1 hour

### 17. Consolidate `formatCurrency()` Into `main.js`
- **Issue:** Duplicated in 4 files
- **Fix:** Add one shared function to `main.js`, call from all JSPs
- **Effort:** 15 minutes

### 18. Consolidate `_tab_stock.jsp` and `_tab_transfer_create.jsp` JS
- **Issue:** ~70% code overlap in product search → dropdown → add row pattern
- **Fix:** Extract to shared include or `main.js` helper
- **Effort:** ~2 hours

### 19. Remove Tailwind CDN From Sales Pages (If Migrating)
- **Issue:** Tailwind CSS (~300KB) loaded from CDN only for `sales/*.jsp` pages
- **Fix:** If sales pages are migrated to Bootstrap, remove Tailwind CDN imports
- **Effort:** Tied to sales page migration

### 20. Add Error Pages (403, 404, 500)
- **Issue:** No custom error pages — Tomcat default shown
- **Fix:** Create 3 JSPs matching the app theme + add `<error-page>` to web.xml
- **Effort:** ~1 hour

---

## P3 — LOW PRIORITY (Backlog)

### 21. Add Favicon
- **Issue:** No `favicon.ico` at webapp root
- **Fix:** Add `favicon.ico` and `<link rel="icon">` to header.jsp
- **Effort:** 15 minutes

### 22. Add Print Stylesheet
- **Issue:** No `@media print` rules — printing any page unpredictable
- **Fix:** Add `print.css` with hidden sidebar/header, clean table formatting
- **Effort:** 1 hour

### 23. Add Skip-to-Content Link
- **Issue:** Keyboard users must tab through full sidebar
- **Fix:** Add `<a href="#main-content" class="skip-link">` as `<body>` first child
- **Effort:** 15 minutes

### 24. Add `role="alert"` to Toast Notifications
- **Issue:** SweetAlert2 toast not announced by screen readers
- **Fix:** Add `role: 'alert'` to Toast mixin config in footer.jsp
- **Effort:** 1 line

### 25. Implement Dark Mode
- **Issue:** No dark mode support
- **Fix:** Add `prefers-color-scheme: dark` media query block in theme.css
- **Effort:** ~2 hours

### 26. Replace `alert()` Calls With SweetAlert2
- **Issue:** 3 files still use native `alert()`/`confirm()` — SweetAlert2 already loaded
- **Fix:** Replace with `Swal.fire()` or `Toast.fire()`
- **Effort:** 30 minutes

### 27. Add Input Validation Attributes to Forms
- **Issue:** Missing `required`, `inputmode`, `autocomplete`, `aria-describedby` on form fields
- **Effort:** ~2 hours across all JSPs

### 28. Remove "Dung's Compatibility Layer" From components.css
- **Issue:** Dead code mapping old→new class names, ~50 lines
- **Effort:** 5 minutes

### 29. Unlink Sales Page From Tailwind
- **Issue:** Sales pages load Tailwind CDN separately — inconsistent with the rest of the app
- **Effort:** ~4 hours to re-theme sales pages using Bootstrap/Material Icons

### 30. Add Form Validation Feedback on JSPs
- **Issue:** Error messages shown as flash messages, not inline next to fields
- **Fix:** Add `<span class="field-error">${fieldError}</span>` next to each form field
- **Effort:** ~3 hours

---

## EFFORT ESTIMATE SUMMARY

| Priority | Items | Est. Effort | Risk |
|----------|-------|-------------|------|
| **P0** | 3 | 15 minutes | None |
| **P1** | 9 | 2-3 days | Low |
| **P2** | 8 | 3-4 days | Low-Medium |
| **P3** | 10 | 2-3 days | Low |
| **Total** | **30** | **7-10 days** | |

---

## QUICK WINS (Under 30 Minutes Each)

| # | Task | Time |
|---|------|------|
| 1 | Fix `.message { display: none }` | 10s |
| 2 | Fix topbar CTA link `orders/create` → `/sales` | 10s |
| 3 | Remove jQuery from footer.jsp | 10s |
| 4 | Add `aria-hidden="true"` to all Material Icons | 5 min |
| 5 | Remove `console.warn` from 2 files | 10s |
| 6 | Delete dead CSS files (3) | 1 min |
| 7 | Add footer.jsp to 4 pages | 5 min |
| 8 | Replace FontAwesome with Material Icons in branch | 15 min |
| 9 | Add `role="alert"` to Toast | 10s |
| 10 | Replace `alert()` with SweetAlert2 | 15 min |

---

## DESIGN SYSTEM UNIFICATION ROADMAP

```
Week 1: P0 fixes + P1 criticals
  → Flash messages visible again
  → Topbar button works
  → jQuery removed (-87KB)
  → footer.jsp on all pages
  → App wrapper standardized

Week 2: P1 remaining + P2 starters
  → Common pagination everywhere
  → Tables wrapped in table-responsive
  → FontAwesome removed (-5MB assets)
  → Button classes unified
  → Dead CSS removed

Week 3: P2 completion + P3
  → Bootstrap modals everywhere
  → Shared formatCurrency()
  → Tab stock/transfer JS consolidated
  → Error pages created
  → Input validation attributes added

Week 4: P3 polish
  → Print stylesheet
  → Skip-to-content link
  → Dark mode (optional)
  → Form validation feedback
  → Dung's compatibility layer removed
  → Sales page re-theme (optional)
```

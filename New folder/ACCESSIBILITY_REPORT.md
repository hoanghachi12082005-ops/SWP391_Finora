# ACCESSIBILITY REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## 1. SEMANTIC HTML

| Issue | Severity | Location | Description |
|-------|----------|----------|-------------|
| `<html lang="vi">` not on all pages | High | Gen 2 pages (self-hosted `<head>`) | Screen readers may use wrong language profile |
| Missing `<main>` landmark | Medium | All Gen 1 pages (`main-content` div, not `<main>`) | `<main>` is the primary landmark for screen readers |
| Heading hierarchy gaps | Medium | Various | Some pages jump from `<h4>` to `<h6>` without `<h5>` |
| No `<nav>` for sidebar | Medium | sidebar.jsp uses `<nav class="sidebar-menu">` correctly | ✅ Actually correct |
| No `<article>`/`<section>` landmarks | Low | Most pages use `<div>` for content sections | Should use semantic elements |

---

## 2. KEYBOARD NAVIGATION

| Issue | Severity | Location | Description |
|-------|----------|----------|-------------|
| No skip-to-content link | Critical | All pages | Keyboard users must tab through entire sidebar (30+ items) before reaching content |
| Custom modals not keyboard-accessible | High | user-list, customer-list | Modals don't trap focus, don't close on Escape |
| No visible focus indicators | High | All pages | Custom button/link styles remove or hide default `:focus` outlines |
| Branch delete modal keyboard broken | High | branch-list.jsp | Requires typing "XÁC NHẬN" into a text field — no keyboard alternative |
| Collapse toggles lack keyboard support | Medium | sidebar.jsp sub-menus | Bootstrap collapse handles keyboard, but custom sub-menu items may not |
| AJAX search results not focus-managed | Medium | _tab_stock.jsp, _tab_transfer.jsp | Search results dropdown doesn't trap focus, no keyboard navigation through results |

---

## 3. COLOR & CONTRAST

| Issue | Severity | Location | Description |
|-------|----------|----------|-------------|
| `#64748b` text on `#ffffff` background | High | components.css, all Gen 1 pages | Contrast ratio ~4.1:1 — fails WCAG AA (needs 4.5:1 for small text) |
| Low-stock red text `#dc2626` on `#fee2e2` | Medium | inventory.css | Contrast ratio ~4.0:1 — borderline for small text |
| Active pagination `#183153` on white | Medium | branch.css | Dark blue on white is fine, but surrounding text uses different blue |
| Color-only status indicators | High | inventory.css (stock levels) | Stock levels use ONLY color (red/orange/green backgrounds). Screen readers get no additional text |
| Status badges use color only | Medium | user-list, customer-list | Active/locked status uses green/red background without icon or text suffix |
| Button text on hover may lose contrast | Low | Various | No hover-contrast verification |

---

## 4. IMAGES & ICONS

| Issue | Severity | Location | Description |
|-------|----------|----------|-------------|
| Material Icons lack `aria-hidden="true"` | High | sidebar.jsp, all pages | Decorative icons read by screen readers as "store, dashboard, shopping_bag, ..." |
| Product images missing `alt` text | High | products/index.jsp | `<img>` elements have no `alt` attributes |
| Avatar images missing `alt` | Medium | sidebar.jsp, profile.jsp | "User avatar" or empty alt needed |
| Branch images missing `alt` | Medium | branch-list.jsp | Same issue |
| No `role="img"` on icon containers | Low | All | Icon containers should have explicit role |

---

## 5. FORMS & LABELS

| Issue | Severity | Location | Description |
|-------|----------|----------|-------------|
| Placeholder as only label | Critical | topbar.jsp, all search inputs | Search inputs have no visible `<label>`, only `placeholder="Tìm kiếm..."` |
| `<select>` elements lack labels | High | All filter forms | Filter dropdowns (status, branch, role) have no associated `<label>` |
| Missing `aria-describedby` on errors | High | All forms | Error messages displayed but not programmatically associated with inputs |
| No `required` attribute on required fields | Medium | Several forms | JSP validation expects non-empty but no `required` attribute or `aria-required` |
| No `inputmode` for phone/email fields | Medium | All forms | Phone inputs should have `inputmode="tel"`, email `inputmode="email"` |
| No `autocomplete` attributes | Medium | login.jsp, forms | Login form, address fields, and repeated info lack autocomplete hints |
| Filter forms not wrapped in `<form>` or `<fieldset>` | Medium | user-list, customer-list | Filter sections are `<div>` elements without form semantics |

---

## 6. DYNAMIC CONTENT & ARIA

| Issue | Severity | Location | Description |
|-------|----------|----------|-------------|
| Toast messages not announced | High | footer.jsp (SweetAlert2) | Toast notifications have no `role="alert"` or `aria-live` region |
| AJAX results not announced | High | _tab_stock.jsp, _tab_transfer.jsp | Dynamic search results update DOM without `aria-live="polite"` |
| Modal open/close not announced | Medium | All modals | No `aria-hidden` management on modals/overlays |
| No `aria-expanded` on collapse toggles | Medium | sidebar.jsp | Bootstrap handles this, but custom collapse items may not |
| Tab panels lack `role="tablist"` | Medium | inventory.jsp (sub-tabs) | Tab navigation uses `<div>` with click handlers, no ARIA tab roles |
| Loading states not announced | Low | AJAX calls | "Loading" text may appear but screen readers don't detect it |

---

## 7. FOCUS & TAB ORDER

| Issue | Severity | Location |
|-------|----------|----------|
| Tab order skips sidebar items | Medium | sidebar.jsp — sidebar comes before `<main>` in DOM; skip-to-content link needed |
| Custom modals don't trap focus | High | user-list, customer-list |
| Form validation errors not focus-managed | High | All forms — error message not focused on validation failure |
| Modal closes don't return focus | Medium | Bootstrap modals return focus correctly; custom modals don't |
| No `tabindex` management on search results | Medium | _tab_stock.jsp, _tab_transfer.jsp |

---

## 8. WCAG COMPLIANCE SUMMARY

| WCAG Level | Estimated Score | Key Gaps |
|------------|----------------|----------|
| **A** | ~40% | Missing skip links, form labels, keyboard modals, aria-hidden on icons |
| **AA** | ~25% | Contrast, focus indicators, error association, color-only status |
| **AAA** | ~5% | Requires enhanced contrast, extended audio descriptions, sign language |

---

## 9. QUICK WINS (Fix in <1 hour)

| # | Issue | Fix |
|---|-------|-----|
| 1 | Material Icons read aloud | Add `aria-hidden="true"` to all `<span class="material-icons">` |
| 2 | Search inputs lack labels | Add `<label class="visually-hidden">Tìm kiếm</label>` |
| 3 | No skip-to-content link | Add `<a href="#main-content" class="skip-link">` as first child of `<body>` |
| 4 | No focus outline | Add `:focus-visible { outline: 2px solid var(--primary-color); }` to theme.css |
| 5 | Images missing alt | Add `alt=""` for decorative, meaningful alt for product/avatar images |
| 6 | Toast not announced | Add `role="alert"` to SweetAlert2 toast config |

**Accessibility Rating:** ❌ Poor. Multiple WCAG A failures make the application partially usable with assistive technologies.

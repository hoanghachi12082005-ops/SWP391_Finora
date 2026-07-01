# FRONTEND CONSISTENCY REPORT

**Project:** KiotRetail (SWP391_Finora)
**Date:** 2026-07-01

---

## 1. PAGE LAYOUT CONSISTENCY

| Property | Gen 1 (Bootstrap) | Gen 2 (Custom) | Gen 3 (Standalone) |
|----------|------------------|----------------|-------------------|
| **Wrapper** | `app-container > main-content > container-fluid` | `app-layout > main-wrapper > page-content` | Varies |
| **Sidebar** | `common/sidebar.jsp` | `common/sidebar.jsp` | `common/sidebar.jsp` |
| **Topbar** | `common/topbar.jsp` | Not used | Not used |
| **Header** | `common/header.jsp` (Bootstrap CDN) | Self-hosted `base.css`+`layout.css` | Self-hosted |
| **Footer** | `common/footer.jsp` (Toast handler) | Not used | Not used |
| **Table style** | `table align-middle table-hover` | `data-table` | `branch-table` / Tailwind |
| **Button style** | `btn btn-danger` (Bootstrap) | `btn-primary`/`btn-secondary` (custom) | Per-action classes |
| **Modal style** | Bootstrap `modal fade` | Custom `modal-overlay`/`modal-box` | JS-driven inline |
| **Pagination** | `common/pagination.jsp` | Custom `page-btn` inline | Form-based |
| **Body class** | None | `user-page`, `customer-page`, etc. | None |

**Consistency Rating:** ⚠️ Low (3 distinct systems)

---

## 2. CSS VARIABLE USAGE

| CSS Variable | theme.css | components.css | customer.css | user.css | profile.css | branch.css | inventory.css |
|-------------|-----------|---------------|-------------|----------|------------|------------|--------------|
| `--primary-color` | `#93000b` | `#93000b` | ✅ uses | ✅ uses | ✅ uses | ❌ `#8b0000` | ✅ uses |
| `--sidebar-width` | `260px` | `260px` | ❌ hardcoded | ❌ hardcoded | ❌ | ❌ | ❌ hardcoded |
| `--header-height` | `70px` | `70px` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `--font-main` | `Inter` | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `--radius-md` | ❌ | `12px` | ✅ uses | ✅ uses | ✅ uses | ❌ | ❌ |
| `--shadow-sm` | ❌ | ✅ | ✅ uses | ✅ uses | ✅ uses | ❌ | ✅ uses |

**Consistency Rating:** ⚠️ Medium (partial adoption)

---

## 3. COMPONENT INCLUDE MATRIX

| JSP Page | sidebar | topbar/header | header.jsp | footer.jsp | pagination.jsp | Module CSS |
|----------|---------|---------------|------------|------------|----------------|------------|
| `users/user-list` | ✅ | ❌ | ❌ | ❌ | ❌ (inline) | `user-management.css` |
| `customers/customer-list` | ✅ | ❌ | ❌ | ❌ | ❌ (inline) | `customer-management.css` |
| `suppliers/list` | ✅ | ❌ | ✅ | ✅ | ✅ | — |
| `products/index` | ✅ | ❌ | ✅ | ✅ | ✅ | — |
| `categories/list` | ✅ | ❌ | ✅ | ✅ | ✅ | — |
| `inventory/inventory` | ✅ | ✅ | ✅ | ✅ | ✅ (in tabs) | `inventory.css` |
| `sales/sales` | ✅ | ❌ | ❌ | ❌ | ❌ | Tailwind CDN |
| `branch/branch-list` | ✅ | ❌ | ❌ (own header) | ❌ | ❌ (form-based) | `branch.css` |
| `profile/profile` | ✅ | ❌ | ❌ | ❌ | ❌ | `profile.css` |
| `reports/employee-sales` | ✅ | ❌ | ❌ | ❌ | ❌ (inline) | `employee-sales-report.css` |
| `activity-log/list` | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| `dashboard/owner` | ✅ | ✅ | ✅ | ✅ | ❌ | `components.css` |
| `pos/sale` | ✅ | ❌ | ✅ | ✅ | ❌ | — |

**Key:** Only 5/13 pages include `footer.jsp` (SweetAlert2 toast handler). Only 3/13 include `topbar.jsp`.

---

## 4. BUTTON STYLE COMPARISON

| Action | Gen 1 (Bootstrap) | Gen 2 (Custom) | Branch |
|--------|------------------|----------------|--------|
| **Add/Create** | `<a class="btn btn-danger">+ Thêm mới</a>` | `<a class="btn-primary"><span>add</span> Thêm mới</a>` | `<button class="btn-add">Thêm</button>` |
| **Edit** | `<a class="btn btn-sm btn-warning">Sửa</a>` | `<a class="table-actions"><span>edit</span></a>` | `<a class="btn-edit"><i class="fa-pen"></i></a>` |
| **Delete** | `<a class="btn btn-sm btn-danger">Xóa</a>` | `<a class="table-actions"><span>delete</span></a>` | `<button class="btn-delete"><i class="fa-trash"></i></button>` |
| **View/Detail** | `<a class="btn btn-sm btn-info">Chi tiết</a>` | `<a class="table-actions"><span>visibility</span></a>` | `<a class="btn-view">Xem</a>` |
| **Search** | `<button class="btn btn-danger"><i class="fa-search"></i></button>` | `<button class="btn-primary">Tìm kiếm</button>` | `<button class="btn-filter">Lọc</button>` |
| **Reset** | `<a class="btn btn-outline-secondary">Reset</a>` | `<a class="btn-secondary">Đặt lại</a>` | Not present |

**Consistency Rating:** ❌ Low (4 independent button systems)

---

## 5. TABLE ROW ACTION PATTERNS

| Feature | Gen 1 (Bootstrap) | Gen 2 (Custom) | Branch |
|---------|------------------|----------------|--------|
| Action icons | Text labels only ("Sửa", "Xóa") | Material Icons (edit, delete, visibility) | FontAwesome (fa-pen, fa-trash) |
| Action wrapper | `<td class="text-center">` | `<td><div class="table-actions">` | `<td class="text-center">` |
| Row click | None | None | None |

---

## 6. FORM CONTROLS CONSISTENCY

| Control | Gen 1 (Bootstrap) | Gen 2 (Custom) | Branch |
|---------|------------------|----------------|--------|
| Text input | `<input class="form-control">` | `<input class="form-control">` | `<input class="form-control">` |
| Select | `<select class="form-select">` | `<select class="form-control">` (wrong class) | `<select class="filter-branch-select">` |
| Search button | `<button class="btn btn-danger"><i class="fa-search"></i></button>` | `<button class="btn-primary">Tìm kiếm</button>` | Custom |
| Date input | `<input type="date" class="form-control">` | `<input type="date" class="form-control">` | Not used |

**Note:** Gen 2 uses `form-control` class on `<select>` elements — this works with custom CSS but is a Bootstrap class used outside Bootstrap context.

---

## 7. NULL/EMPTY DATA HANDLING PATTERNS

| Pattern | Gen 1 (Bootstrap) | Gen 2 (Custom) |
|---------|------------------|----------------|
| Empty table | `text-center text-muted py-4` | `<div class="empty-row"><span>inventory_2</span><h4>Trống</h4><p>Chưa có dữ liệu</p></div>` |
| Null field display | Scriptlet: `<%= x != null ? x : "" %>` | EL: `${empty x ? '—' : x}` |
| Session flash | Scriptlet: `session.getAttribute()` + `removeAttribute()` | JSTL: `sessionScope.msg` + `c:remove` |
| Null image | Not handled (broken image icon) | Not handled (broken image icon) |
| Avatar fallback | First 2 chars of name | First 2 chars of name |

**Observation:** Gen 2 has richer empty states but inconsistent null-field display.

---

## 8. MODAL BEHAVIOR COMPARISON

| Feature | Custom (Gen 2) | Bootstrap (Gen 1) | Branch |
|---------|---------------|-------------------|--------|
| Click-outside close | ❌ | ✅ | ❌ |
| Escape key close | ❌ | ✅ | ❌ |
| Focus trap | ❌ | ✅ (native) | ❌ |
| Animation | None | Fade | None |
| Overlay | `rgba(0,0,0,0.5)` | Bootstrap backdrop | Inline |
| Close button | `<a href="..."><span>close</span></a>` (page reload) | `<button class="btn-close" data-bs-dismiss>` | `<span onclick="closeModal()">` |

---

## 9. FLASH MESSAGE PATTERNS

| Mechanism | Gen 1 (Bootstrap) | Gen 2 (Custom) | Branch |
|-----------|------------------|----------------|--------|
| Toast | SweetAlert2 via `footer.jsp` | ❌ Not available | ❌ Not available |
| Inline alert | `<div class="alert alert-danger">` | `<div class="message">` (hidden by CSS!) | None |
| Session attrs | `message`, `error`, `successMessage`, `errorMessage` | Same attrs set but never displayed | Inline div |

**Critical Bug:** `theme.css:81` has `.message { display: none !important; }` — this HIDES all `<div class="message">` elements used by Gen 2 pages for flash messages. Flash messages on user-list, customer-list, profile, and employee-sales pages are invisible.

---

## 10. SUMMARY

| Consistency Dimension | Rating | Key Action Required |
|-----------------------|--------|-------------------|
| Page layout wrapper | ❌ 3 systems | Standardize on `app-container > main-content` |
| Component includes | ❌ Inconsistent | Add `footer.jsp` to all pages |
| Table styling | ⚠️ 2 systems | Pick `data-table` or `table` project-wide |
| Button styling | ❌ 4 systems | Standardize on Bootstrap `.btn` classes |
| Form controls | ⚠️ 2 systems | Normalize to Bootstrap `form-control`/`form-select` |
| Modals | ❌ 3 systems | Use Bootstrap modals everywhere |
| Pagination | ❌ 3 systems | Reuse `common/pagination.jsp` |
| Flash messages | ❌ Broken | Fix `.message { display: none }` bug |
| Empty states | ⚠️ Mixed | Use rich empty states (Gen 2 style) everywhere |
| Null handling | ⚠️ Mixed | Standardize on EL `${empty x ? '—' : x}` |
| CSS variables | ⚠️ Partial | All modules should use theme.css vars |
| JS frameworks | ❌ 3 CDNs | Remove jQuery; consolidate on Bootstrap+Swal |

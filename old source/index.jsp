<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="product.model.Product, java.util.List, java.text.NumberFormat, java.util.Locale" %>
<%
    List<Product> products   = (List<Product>) request.getAttribute("products");
    int currentPage          = (Integer) request.getAttribute("currentPage");
    int totalPages           = (Integer) request.getAttribute("totalPages");
    String ctx               = request.getContextPath();
    String keyword           = (String) request.getAttribute("keyword");
    String filterStatus      = (String) request.getAttribute("filterStatus");
    String viewMode          = (String) request.getAttribute("viewMode");
    if (viewMode == null) viewMode = "table";
    NumberFormat vndFormat   = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Sản phẩm - Finora</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/styles.css">
    <style>
        /* ── Layout ── */
        .pm-wrap { max-width:1300px; margin:2rem auto; padding:0 1.25rem; }

        /* ── Top bar ── */
        .topbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem; flex-wrap:wrap; gap:1rem; }
        .topbar h1 { margin:0; font-size:1.875rem; font-weight:800; color:var(--text); text-shadow:0 1px 10px rgba(63,231,255,.28); }

        /* ── Buttons ── */
        .btn { display:inline-flex; align-items:center; gap:.4rem; padding:.5rem 1.1rem; border-radius:999px; font-weight:700; cursor:pointer; border:none; transition:all .2s; font-size:.85rem; text-transform:uppercase; letter-spacing:.05em; text-decoration:none; }
        .btn-primary  { background:linear-gradient(135deg,var(--cyan),var(--violet)); color:#06101c; box-shadow:0 4px 15px rgba(63,231,255,.2); }
        .btn-primary:hover  { transform:translateY(-2px); box-shadow:0 6px 20px rgba(63,231,255,.3); }
        .btn-warning  { background:rgba(255,185,50,.12); color:#ffb932; border:1px solid rgba(255,185,50,.3); padding:.25rem .65rem !important; font-size:.78rem !important; }
        .btn-warning:hover  { background:rgba(255,185,50,.25); }
        .btn-danger   { background:rgba(255,107,138,.12); color:var(--danger); border:1px solid rgba(255,107,138,.28); padding:.25rem .65rem !important; font-size:.78rem !important; }
        .btn-danger:hover   { background:rgba(255,107,138,.25); }
        .btn-cancel   { background:var(--panel); color:var(--muted); border:1px solid var(--line); }
        .btn-cancel:hover   { background:var(--panel-strong); color:var(--text); }
        .btn-ghost    { background:transparent; color:var(--muted); border:1px solid var(--line); padding:.45rem .9rem; border-radius:999px; }
        .btn-ghost.active, .btn-ghost:hover { background:var(--panel); color:var(--text); border-color:var(--cyan); }

        /* ── Search & Filter Bar ── */
        .filter-bar { display:flex; gap:.75rem; align-items:center; flex-wrap:wrap; background:var(--panel); border:1px solid var(--line); border-radius:18px; padding:.85rem 1.25rem; margin-bottom:1.25rem; backdrop-filter:blur(8px); }
        .search-box { flex:1; min-width:200px; display:flex; align-items:center; gap:.5rem; background:var(--panel-strong); border:1px solid var(--line); border-radius:10px; padding:.45rem .85rem; transition:.2s; }
        .search-box:focus-within { border-color:var(--cyan); box-shadow:0 0 0 3px rgba(63,231,255,.12); }
        .search-box svg { color:var(--muted); flex-shrink:0; }
        .search-box input { background:none; border:none; outline:none; color:var(--text); font-size:.9rem; width:100%; }
        .search-box input::placeholder { color:rgba(255,255,255,.25); }
        .filter-select { background:var(--panel-strong); border:1px solid var(--line); border-radius:10px; color:var(--text); padding:.45rem .8rem; font-size:.875rem; cursor:pointer; transition:.2s; }
        .filter-select:focus { outline:none; border-color:var(--cyan); }
        .filter-select option { background:#07111f; }
        .view-toggle { display:flex; gap:.35rem; margin-left:auto; }

        /* ── Card/Table wrapper ── */
        .card { background:var(--panel); border:1px solid var(--line); border-radius:26px; overflow:hidden; box-shadow:0 24px 80px rgba(0,0,0,.28); backdrop-filter:blur(10px); }

        /* ── Table ── */
        table { width:100%; border-collapse:collapse; }
        th,td { padding:.9rem 1rem; text-align:left; border-bottom:1px solid var(--line); color:var(--text); }
        th { background:rgba(255,255,255,.02); font-weight:800; color:var(--cyan); text-transform:uppercase; font-size:.7rem; letter-spacing:.1em; }
        tr:last-child td { border-bottom:none; }
        tbody tr:hover td { background:var(--panel-strong); }
        .action-cell { display:flex; gap:.4rem; }

        /* ── Badges ── */
        .badge { padding:.28rem .65rem; border-radius:9999px; font-size:.7rem; font-weight:800; background:rgba(84,242,161,.12); color:var(--ok); border:1px solid rgba(84,242,161,.24); text-transform:uppercase; letter-spacing:.05em; }
        .badge.inactive { background:rgba(255,107,138,.12); color:var(--danger); border-color:rgba(255,107,138,.28); }

        /* ── Showcase (Card Grid) ── */
        .showcase-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(240px,1fr)); gap:1.25rem; padding:1.25rem; }
        .product-card { background:var(--panel-strong); border:1px solid var(--line); border-radius:18px; padding:1.25rem; display:flex; flex-direction:column; gap:.75rem; transition:all .22s; position:relative; overflow:hidden; }
        .product-card::before { content:''; position:absolute; inset:0; background:linear-gradient(135deg,rgba(63,231,255,.04),transparent); opacity:0; transition:.22s; }
        .product-card:hover { transform:translateY(-4px); border-color:rgba(63,231,255,.35); box-shadow:0 12px 40px rgba(63,231,255,.1); }
        .product-card:hover::before { opacity:1; }
        .card-sku { font-size:.7rem; font-weight:800; color:var(--cyan); text-transform:uppercase; letter-spacing:.1em; }
        .card-name { font-size:1rem; font-weight:700; color:var(--text); line-height:1.3; }
        .card-price { font-size:1.15rem; font-weight:800; background:linear-gradient(135deg,var(--cyan),var(--violet)); -webkit-background-clip:text; -webkit-text-fill-color:transparent; background-clip:text; }
        .card-meta { display:flex; justify-content:space-between; align-items:center; font-size:.78rem; color:var(--muted); }
        .card-actions { display:flex; gap:.4rem; margin-top:.25rem; }

        /* ── Pagination ── */
        .pagination { display:flex; justify-content:center; align-items:center; padding:1.25rem; gap:.4rem; border-top:1px solid var(--line); }
        .pagination a, .pagination span { padding:.45rem .8rem; border-radius:.5rem; border:1px solid var(--line); color:var(--muted); text-decoration:none; transition:all .2s; font-weight:600; font-size:.85rem; }
        .pagination a:hover { background:var(--panel); color:var(--text); border-color:var(--cyan); }
        .pagination .active { background:linear-gradient(135deg,var(--cyan),var(--violet)); color:#06101c; border-color:transparent; font-weight:800; }

        /* ── Empty state ── */
        .empty-state { text-align:center; padding:4rem 2rem; color:var(--muted); }
        .empty-state svg { width:56px; height:56px; opacity:.3; margin-bottom:1rem; }
        .empty-state p { font-size:1rem; }

        /* ── Modal ── */
        .modal { display:none; position:fixed; inset:0; background:rgba(7,17,31,.85); backdrop-filter:blur(8px); align-items:center; justify-content:center; z-index:9999; padding:1rem; }
        .modal-content { background:#07111f; border:1px solid rgba(63,231,255,.3); border-radius:28px; width:100%; max-width:540px; padding:2.25rem; box-shadow:0 24px 80px rgba(63,231,255,.15); max-height:90vh; overflow-y:auto; }
        .modal-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem; }
        .modal-header h2 { margin:0; font-size:1.4rem; color:var(--text); font-weight:800; }
        .close-btn { background:none; border:none; font-size:1.4rem; cursor:pointer; color:var(--muted); transition:.2s; }
        .close-btn:hover { color:var(--danger); }
        .form-group { margin-bottom:1.1rem; }
        .form-group label { display:block; margin-bottom:.4rem; font-weight:600; font-size:.82rem; color:var(--muted); text-transform:uppercase; letter-spacing:.04em; }
        .form-group input, .form-group select { width:100%; padding:.6rem .85rem; background:var(--panel); border:1px solid var(--line); border-radius:.6rem; font-size:.9rem; color:var(--text); transition:.2s; box-sizing:border-box; }
        .form-group input:focus, .form-group select:focus { outline:none; border-color:var(--cyan); background:var(--panel-strong); box-shadow:0 0 0 3px rgba(63,231,255,.12); }
        .form-group input::placeholder { color:rgba(255,255,255,.2); }
        .form-row { display:flex; gap:1rem; }
        .form-row .form-group { flex:1; }
        .form-actions { margin-top:1.75rem; display:flex; justify-content:flex-end; gap:.75rem; }
        option { background:#07111f; color:var(--text); }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <div class="pm-wrap">
        <!-- ====== TOP BAR ====== -->
        <div class="topbar">
            <h1>Quản lý Sản phẩm</h1>
            <button class="btn btn-primary" id="btnOpenAdd" onclick="openProductModal('add')">
                <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
                Thêm sản phẩm
            </button>
        </div>

        <!-- ====== SEARCH & FILTER BAR ====== -->
        <form id="filterForm" method="get" action="<%= ctx %>/product-management">
            <div class="filter-bar">
                <!-- Search -->
                <div class="search-box">
                    <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
                    <input type="text" name="keyword" id="searchInput" placeholder="Tìm theo tên hoặc SKU…" value="<%= keyword != null ? keyword : "" %>">
                </div>

                <!-- Status filter -->
                <select name="status" id="statusFilter" class="filter-select" onchange="document.getElementById('filterForm').submit()">
                    <option value="" <%="".equals(filterStatus) ? "selected" : ""%>>Tất cả trạng thái</option>
                    <option value="Active"   <%="Active".equals(filterStatus)   ? "selected" : ""%>>Active</option>
                    <option value="Inactive" <%="Inactive".equals(filterStatus) ? "selected" : ""%>>Inactive</option>
                </select>

                <!-- Search button -->
                <button type="submit" class="btn btn-primary" style="padding:.45rem 1rem;">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
                    Tìm
                </button>

                <% if ((keyword != null && !keyword.isBlank()) || (filterStatus != null && !filterStatus.isBlank())) { %>
                <a href="<%= ctx %>/product-management?view=<%= viewMode %>" class="btn btn-cancel" style="padding:.45rem 1rem; font-size:.8rem;">✕ Xóa lọc</a>
                <% } %>

                <!-- View toggle -->
                <input type="hidden" name="view" id="viewInput" value="<%= viewMode %>">
                <div class="view-toggle">
                    <button type="button" class="btn-ghost <%= "table".equals(viewMode) ? "active" : "" %>" onclick="switchView('table')" title="Bảng">
                        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M3 15h18M9 3v18"/></svg>
                    </button>
                    <button type="button" class="btn-ghost <%= "showcase".equals(viewMode) ? "active" : "" %>" onclick="switchView('showcase')" title="Showcase">
                        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
                    </button>
                </div>
            </div>
        </form>

        <!-- ====== PRODUCTS ====== -->
        <div class="card">
<%
    boolean empty = (products == null || products.isEmpty());
    if ("showcase".equals(viewMode)) {
%>
            <!-- ====== SHOWCASE VIEW ====== -->
            <div class="showcase-grid">
<%
        if (empty) {
%>
                <div class="empty-state" style="grid-column:1/-1;">
                    <svg fill="none" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24"><path d="M20 7H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2zM16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                    <p>Không tìm thấy sản phẩm nào.</p>
                </div>
<%
        } else {
            for (Product p : products) {
                String badgeClass = "Active".equalsIgnoreCase(p.getStatus()) ? "badge" : "badge inactive";
%>
                <div class="product-card">
                    <div class="card-sku"><%= p.getSku() != null ? p.getSku() : "—" %></div>
                    <div class="card-name"><%= p.getName() != null ? p.getName() : "—" %></div>
                    <div class="card-price"><%= p.getPrice() != null ? vndFormat.format(p.getPrice()) : "0 ₫" %></div>
                    <div class="card-meta">
                        <span>Cost: <%= p.getCostPrice() != null ? vndFormat.format(p.getCostPrice()) : "0 ₫" %></span>
                        <span class="<%= badgeClass %>"><%= p.getStatus() != null ? p.getStatus() : "" %></span>
                    </div>
                    <div class="card-meta">
                        <span>Alert: <%= p.getStockAlertQty() %> units</span>
                        <span style="color:var(--muted);font-size:.72rem;">Cat #<%= p.getCategoryID() %></span>
                    </div>
                    <div class="card-actions">
                        <button class="btn btn-warning" onclick="openProductModal('edit', 
                            '<%= p.getProductID() %>',
                            '<%= p.getCategoryID() %>',
                            '<%= (p.getName() != null ? p.getName() : "").replace("'", "\\'") %>',
                            '<%= (p.getSku() != null ? p.getSku() : "").replace("'", "\\'") %>',
                            '<%= p.getPrice() != null ? p.getPrice().toPlainString() : "0" %>',
                            '<%= p.getCostPrice() != null ? p.getCostPrice().toPlainString() : "0" %>',
                            '<%= p.getStockAlertQty() %>',
                            '<%= p.getStatus() != null ? p.getStatus() : "Active" %>'
                        )">Sửa</button>
                        <button type="button" class="btn btn-danger" onclick="deleteProduct('<%= p.getProductID() %>')">Xóa</button>
                    </div>
                </div>
<%
            }
        }
%>
            </div>
<%
    } else {
%>
            <!-- ====== TABLE VIEW ====== -->
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>SKU</th>
                        <th>Tên sản phẩm</th>
                        <th>Danh mục</th>
                        <th>Giá bán</th>
                        <th>Giá vốn</th>
                        <th>Alert Qty</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
<%
        if (empty) {
%>
                    <tr>
                        <td colspan="9" class="empty-state">
                            <svg fill="none" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24" style="width:40px;height:40px;display:block;margin:0 auto .75rem;opacity:.3;"><path d="M20 7H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2zM16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                            Không tìm thấy sản phẩm nào.
                        </td>
                    </tr>
<%
        } else {
            for (Product p : products) {
                String badgeClass = "Active".equalsIgnoreCase(p.getStatus()) ? "badge" : "badge inactive";
%>
                    <tr>
                        <td style="color:var(--muted);font-size:.82rem;">#<%= p.getProductID() %></td>
                        <td><strong style="color:var(--cyan);font-size:.82rem;"><%= p.getSku() != null ? p.getSku() : "" %></strong></td>
                        <td><strong><%= p.getName() != null ? p.getName() : "" %></strong></td>
                        <td style="color:var(--muted);">#<%= p.getCategoryID() %></td>
                        <td><%= p.getPrice() != null ? vndFormat.format(p.getPrice()) : "0 ₫" %></td>
                        <td style="color:var(--muted);"><%= p.getCostPrice() != null ? vndFormat.format(p.getCostPrice()) : "0 ₫" %></td>
                        <td><%= p.getStockAlertQty() %></td>
                        <td><span class="<%= badgeClass %>"><%= p.getStatus() != null ? p.getStatus() : "" %></span></td>
                        <td>
                            <div class="action-cell">
                                <button class="btn btn-warning" onclick="openProductModal('edit', 
                                    '<%= p.getProductID() %>',
                                    '<%= p.getCategoryID() %>',
                                    '<%= (p.getName() != null ? p.getName() : "").replace("'", "\\'") %>',
                                    '<%= (p.getSku() != null ? p.getSku() : "").replace("'", "\\'") %>',
                                    '<%= p.getPrice() != null ? p.getPrice().toPlainString() : "0" %>',
                                    '<%= p.getCostPrice() != null ? p.getCostPrice().toPlainString() : "0" %>',
                                    '<%= p.getStockAlertQty() %>',
                                    '<%= p.getStatus() != null ? p.getStatus() : "Active" %>'
                                )">Sửa</button>
                                <button type="button" class="btn btn-danger" onclick="deleteProduct('<%= p.getProductID() %>')">Xóa</button>
                            </div>
                        </td>
                    </tr>
<%
            }
        }
%>
                </tbody>
            </table>
<%
    }
%>
            <!-- ====== PAGINATION ====== -->
<%  if (totalPages > 1) { %>
            <div class="pagination">
<%      String baseUrl = ctx + "/product-management?view=" + viewMode
                + (keyword != null && !keyword.isBlank() ? "&keyword=" + keyword : "")
                + (filterStatus != null && !filterStatus.isBlank() ? "&status=" + filterStatus : "");
        if (currentPage > 1) { %>
                <a href="<%= baseUrl %>&page=<%= currentPage - 1 %>">&laquo; Trước</a>
<%      }
        for (int i = 1; i <= totalPages; i++) {
            if (i == currentPage) { %>
                <span class="active"><%= i %></span>
<%          } else { %>
                <a href="<%= baseUrl %>&page=<%= i %>"><%= i %></a>
<%          }
        }
        if (currentPage < totalPages) { %>
                <a href="<%= baseUrl %>&page=<%= currentPage + 1 %>">Tiếp &raquo;</a>
<%      } %>
            </div>
<%  } %>
        </div><!-- /.card -->
    </div><!-- /.pm-wrap -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

    <!-- ====== PRODUCT MODAL ====== -->
    <div id="productModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 id="modal-title">Thêm sản phẩm mới</h2>
                <button class="close-btn" onclick="closeProductModal()">&times;</button>
            </div>
            <form action="<%= ctx %>/product-management" method="post" id="product-form">
                <input type="hidden" name="action" id="modal-action" value="add">
                <input type="hidden" name="view" value="<%= viewMode %>">
                <input type="hidden" name="productID" id="modal-id">

                <div class="form-group">
                    <label for="modal-name">Tên sản phẩm</label>
                    <input type="text" id="modal-name" name="name" required placeholder="VD: Chuột không dây">
                </div>
                <div class="form-group">
                    <label for="modal-sku">SKU</label>
                    <input type="text" id="modal-sku" name="sku" required placeholder="VD: MOUSE-001">
                </div>
                <div class="form-group">
                    <label for="modal-cat">Mã danh mục (Category ID)</label>
                    <input type="number" id="modal-cat" name="categoryID" required value="1" min="1">
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="modal-price">Giá bán (VNĐ)</label>
                        <input type="number" id="modal-price" name="price" required placeholder="0" min="0">
                    </div>
                    <div class="form-group">
                        <label for="modal-cost">Giá vốn (VNĐ)</label>
                        <input type="number" id="modal-cost" name="costPrice" required placeholder="0" min="0">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="modal-alert">Mức cảnh báo tồn kho</label>
                        <input type="number" id="modal-alert" name="stockAlertQty" required value="10" min="0">
                    </div>
                    <div class="form-group">
                        <label for="modal-status">Trạng thái</label>
                        <select id="modal-status" name="status" required>
                            <option value="Active">Active</option>
                            <option value="Inactive">Inactive</option>
                        </select>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn btn-cancel" onclick="closeProductModal()">Huỷ</button>
                    <button type="submit" class="btn btn-primary" id="modal-submit">Lưu sản phẩm</button>
                </div>
            </form>
        </div>
    </div>

    <!-- ====== DELETE PRODUCT FORM ====== -->
    <form id="deleteProductForm" action="<%= ctx %>/product-management" method="post" style="display:none;">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" id="delete-id">
        <input type="hidden" name="view" value="<%= viewMode %>">
    </form>

    <script>
        /* ── Delete Product ── */
        function deleteProduct(id) {
            if (confirm('Xóa sản phẩm này?')) {
                document.getElementById('delete-id').value = id;
                document.getElementById('deleteProductForm').submit();
            }
        }

        /* ── Product Modal ── */
        const productModal = document.getElementById('productModal');

        function openProductModal(action, id, catId, name, sku, price, costPrice, alertQty, status) {
            document.getElementById('modal-action').value = action;
            if (action === 'edit') {
                document.getElementById('modal-title').innerText = 'Chỉnh sửa sản phẩm';
                document.getElementById('modal-submit').innerText = 'Cập nhật';
                document.getElementById('modal-id').value = id;
                document.getElementById('modal-cat').value = catId;
                document.getElementById('modal-name').value = name;
                document.getElementById('modal-sku').value = sku;
                document.getElementById('modal-price').value = price;
                document.getElementById('modal-cost').value = costPrice;
                document.getElementById('modal-alert').value = alertQty;
                document.getElementById('modal-status').value = status;
            } else {
                document.getElementById('modal-title').innerText = 'Thêm sản phẩm mới';
                document.getElementById('modal-submit').innerText = 'Lưu sản phẩm';
                document.getElementById('modal-id').value = '';
                document.getElementById('product-form').reset();
                document.getElementById('modal-cat').value = '1';
                document.getElementById('modal-alert').value = '10';
                document.getElementById('modal-status').value = 'Active';
            }
            productModal.style.display = 'flex';
        }

        function closeProductModal() { productModal.style.display = 'none'; }

        /* Close on outside click */
        window.addEventListener('click', e => {
            if (e.target === productModal) closeProductModal();
        });

        /* ── View toggle ── */
        function switchView(mode) {
            document.getElementById('viewInput').value = mode;
            document.getElementById('filterForm').submit();
        }

        /* ── Search on Enter ── */
        document.getElementById('searchInput').addEventListener('keydown', e => {
            if (e.key === 'Enter') document.getElementById('filterForm').submit();
        });
    </script>
</body>
</html>

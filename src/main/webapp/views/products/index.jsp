<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Product, model.Category, model.Unit, java.util.List, java.text.NumberFormat, java.util.Locale" %>
<%
    List<Product> products   = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    List<Unit> units         = (List<Unit>) request.getAttribute("units");
    int currentPage          = (Integer) request.getAttribute("currentPage");
    int totalPages           = (Integer) request.getAttribute("totalPages");
    String ctx               = request.getContextPath();
    String keyword           = (String) request.getAttribute("keyword");
    String filterStatus      = (String) request.getAttribute("filterStatus");
    Integer filterCategoryID = (Integer) request.getAttribute("filterCategoryID");
    Integer filterUnitID     = (Integer) request.getAttribute("filterUnitID");
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
        :root {
            --bg: #ffffff;
            --panel: rgba(220, 38, 38, 0.05);
            --panel-strong: rgba(220, 38, 38, 0.1);
            --line: rgba(220, 38, 38, 0.15);
            --text: #000000;
            --muted: #666666;
            --cyan: #dc2626;
            --violet: #b91c1c;
            --gold: #991b1b;
            --danger: #dc2626;
            --ok: #16a34a;
        }

        /* ── Light theme override for main content area ── */
        .main {
            background: #ffffff !important;
            color: var(--text) !important;
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            padding: 2.5rem !important;
            min-height: 100vh;
        }

        /* ── Sidebar Red Design ── */
        .sidebar {
            background: #dc2626 !important;
            border-right: 1px solid rgba(0, 0, 0, 0.1);
            padding: 2rem 1.25rem !important;
        }
        .sidebar h2 {
            font-family: 'Manrope', sans-serif;
            font-weight: 800;
            color: #ffffff;
            font-size: 1.5rem;
            margin-bottom: 2rem;
            text-shadow: none;
            letter-spacing: 0.05em;
        }
        .sidebar a {
            color: #fecaca !important;
            font-weight: 600;
            font-size: 0.9rem !important;
            padding: 0.75rem 1rem !important;
            margin-bottom: 0.35rem;
            border-radius: 12px !important;
            transition: all 0.25s ease !important;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .sidebar a:hover {
            color: #ffffff !important;
            background: rgba(0, 0, 0, 0.2) !important;
            transform: translateX(4px);
        }
        .sidebar a.active, .sidebar a:nth-child(7) { /* Highlight Product Manager link */
            color: #ffffff !important;
            background: rgba(0, 0, 0, 0.2) !important;
            box-shadow: none;
            font-weight: 700;
        }

        /* ── Layout ── */
        .pm-wrap { max-width: 1200px; margin: 0 auto; }

        /* ── Top bar ── */
        .topbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
        .topbar h1 { margin: 0; font-size: 2.25rem; font-weight: 800; color: var(--text); font-family: 'Manrope', sans-serif; text-shadow: none; }

        /* ── Buttons ── */
        .btn { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.65rem 1.25rem; border-radius: 14px; font-weight: 700; cursor: pointer; border: none; transition: all 0.22s ease; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; text-decoration: none; }
        .btn-primary  { background: #dc2626 !important; color: #ffffff !important; box-shadow: 0 2px 8px rgba(220, 38, 38, 0.2); }
        .btn-primary:hover  { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(220, 38, 38, 0.35); background: #b91c1c !important; }
        
        .btn-warning  { background: rgba(220, 38, 38, 0.12) !important; color: #991b1b !important; border: 1px solid rgba(220, 38, 38, 0.25) !important; padding: 0.4rem 0.85rem !important; border-radius: 8px !important; font-size: 0.78rem !important; }
        .btn-warning:hover  { background: rgba(220, 38, 38, 0.25) !important; transform: translateY(-1px); }
        
        .btn-danger   { background: rgba(220, 38, 38, 0.12) !important; color: #dc2626 !important; border: 1px solid rgba(220, 38, 38, 0.25) !important; padding: 0.4rem 0.85rem !important; border-radius: 8px !important; font-size: 0.78rem !important; }
        .btn-danger:hover   { background: rgba(220, 38, 38, 0.25) !important; transform: translateY(-1px); }
        
        .btn-cancel   { background: var(--panel-strong) !important; color: var(--text) !important; border: 1px solid var(--line) !important; }
        .btn-cancel:hover   { background: rgba(220, 38, 38, 0.2) !important; }
        
        .btn-ghost    { background: transparent !important; color: var(--muted) !important; border: 1px solid var(--line) !important; padding: 0.5rem 1rem; border-radius: 10px; }
        .btn-ghost.active, .btn-ghost:hover { background: var(--panel-strong) !important; color: var(--text) !important; border-color: #dc2626 !important; }

        /* ── Search & Filter Bar ── */
        .filter-bar { display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; background: rgba(220, 38, 38, 0.03) !important; border: 1px solid var(--line) !important; border-radius: 20px; padding: 1rem 1.5rem; margin-bottom: 1.5rem; backdrop-filter: blur(0px); }
        .search-box { flex: 1; min-width: 250px; display: flex; align-items: center; gap: 0.5rem; background: #ffffff !important; border: 1px solid var(--line) !important; border-radius: 12px; padding: 0.55rem 1rem; transition: 0.2s; }
        .search-box:focus-within { border-color: #dc2626 !important; box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1); }
        .search-box svg { color: var(--muted); }
        .search-box input { background: transparent !important; border: none !important; outline: none !important; color: var(--text) !important; font-size: 0.9rem; width: 100%; padding: 0 !important; }
        
        .filter-select { background: #ffffff !important; border: 1px solid var(--line) !important; border-radius: 12px; color: var(--text) !important; padding: 0.55rem 1rem !important; font-size: 0.875rem; cursor: pointer; height: auto !important; transition: all 0.25s ease; font-weight: 600; }
        .filter-select:hover { border-color: rgba(220, 38, 38, 0.4) !important; box-shadow: 0 2px 8px rgba(220, 38, 38, 0.1); }
        .filter-select:focus { border-color: #dc2626 !important; box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1) !important; outline: none; }
        .filter-select option { background: #ffffff !important; color: var(--text) !important; padding: 0.5rem 1rem !important; }
        .filter-select option:hover { background: rgba(220, 38, 38, 0.05) !important; }
        .filter-select optgroup { font-weight: 800; color: #991b1b !important; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.08em; background: rgba(220, 38, 38, 0.05) !important; margin-top: 0.25rem; padding: 0.4rem 0.5rem !important; }
        .view-toggle { display: flex; gap: 0.35rem; margin-left: auto; }

        /* ── Card Container ── */
        .card { background: #ffffff !important; border: 1px solid var(--line) !important; border-radius: 24px; overflow: hidden; box-shadow: 0 2px 8px rgba(220, 38, 38, 0.08) !important; backdrop-filter: none; }

        /* ── Table (Light theme overrides) ── */
        table { width: 100%; border-collapse: collapse; background: transparent !important; }
        th, td { padding: 1.1rem 1.25rem !important; text-align: left; border-bottom: 1px solid var(--line) !important; color: var(--text) !important; background: transparent !important; }
        th { background: rgba(220, 38, 38, 0.08) !important; font-weight: 800; color: #991b1b !important; text-transform: uppercase; font-size: 0.72rem; letter-spacing: 0.1em; }
        tbody tr { transition: background 0.2s ease; }
        tbody tr:hover td { background: rgba(220, 38, 38, 0.03) !important; }
        .action-cell { display: flex; gap: 0.4rem; }

        /* ── Badges ── */
        .badge { padding: 0.35rem 0.75rem; border-radius: 9999px; font-size: 0.72rem; font-weight: 800; background: rgba(22, 163, 74, 0.1) !important; color: var(--ok) !important; border: 1px solid rgba(22, 163, 74, 0.25) !important; text-transform: uppercase; letter-spacing: 0.05em; display: inline-block; }
        .badge.inactive { background: rgba(220, 38, 38, 0.1) !important; color: #dc2626 !important; border-color: rgba(220, 38, 38, 0.25) !important; }

        /* ── Showcase (Card Grid) ── */
        .showcase-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 1.5rem; padding: 1.5rem; }
        .product-card { background: #ffffff !important; border: 1px solid var(--line) !important; border-radius: 20px; padding: 1.5rem; display: flex; flex-direction: column; gap: 0.85rem; transition: all 0.25s ease; position: relative; overflow: hidden; }
        .product-card:hover { transform: translateY(-4px); border-color: rgba(220, 38, 38, 0.3) !important; box-shadow: 0 8px 24px rgba(220, 38, 38, 0.08); background: rgba(220, 38, 38, 0.02) !important; }
        .card-sku { font-size: 0.7rem; font-weight: 800; color: #dc2626; text-transform: uppercase; letter-spacing: 0.1em; }
        .card-name { font-size: 1.05rem; font-weight: 700; color: var(--text); line-height: 1.3; }
        .card-price { font-size: 1.2rem; font-weight: 800; color: #991b1b; }
        .card-meta { display: flex; justify-content: space-between; align-items: center; font-size: 0.8rem; color: var(--muted); }

        /* ── Pagination ── */
        .pagination { display: flex; justify-content: center; align-items: center; padding: 1.5rem; gap: 0.5rem; border-top: 1px solid var(--line); background: transparent !important; }
        .pagination a, .pagination span { padding: 0.5rem 0.9rem !important; border-radius: 10px !important; border: 1px solid var(--line) !important; color: var(--muted) !important; text-decoration: none; transition: all 0.2s; font-weight: 600; font-size: 0.85rem; background: transparent !important; }
        .pagination a:hover { background: var(--panel-strong) !important; color: var(--text) !important; border-color: #dc2626 !important; }
        .pagination .active { background: #dc2626 !important; color: #ffffff !important; border-color: transparent !important; font-weight: 800; }

        /* ── Modal Design (Light theme Overlay & Box) ── */
        .modal { background: rgba(255, 255, 255, 0.9) !important; backdrop-filter: blur(0px) !important; display: none; align-items: center; justify-content: center; }
        .modal-content { background: #ffffff !important; border: 1px solid rgba(220, 38, 38, 0.2) !important; border-radius: 28px !important; width: 100%; max-width: 520px; padding: 2.25rem !important; box-shadow: 0 12px 40px rgba(220, 38, 38, 0.1) !important; }
        .modal-header h2 { font-family: 'Manrope', sans-serif; font-weight: 800; color: var(--text) !important; }
        
        .form-group label { color: #666666 !important; font-size: 0.78rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; }
        .form-group input, .form-group select { background: #ffffff !important; border: 1px solid var(--line) !important; border-radius: 12px !important; color: var(--text) !important; padding: 0.7rem 1rem !important; font-size: 0.9rem; transition: 0.2s; }
        .form-group input:focus, .form-group select:focus { border-color: #dc2626 !important; box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1) !important; }
    </style>
</head>
<body>
    <jsp:include page="/views/common/header.jsp" />
    <jsp:include page="/views/common/sidebar.jsp" />
    <main class="main">

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
        <form id="filterForm" method="get" action="<%= ctx %>/products">
            <div class="filter-bar">
                <!-- Search -->
                <div class="search-box">
                    <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
                    <input type="text" name="keyword" id="searchInput" placeholder="Tìm theo tên hoặc SKU…" value="<%= keyword != null ? keyword : "" %>">
                </div>

                <!-- Unified Filter Dropdown -->
                <select id="unifiedFilter" class="filter-select" style="min-width: 200px;">
                    <option value="">Tất cả bộ lọc</option>
                    
                    <optgroup label="Danh mục">
                        <% if (categories != null) {
                             for (Category cat : categories) {
                                 boolean selected = (filterCategoryID != null && filterCategoryID == cat.getCategoryID());
                        %>
                            <option value="cat_<%= cat.getCategoryID() %>" <%= selected ? "selected" : "" %>>
                                <%= cat.getName() %>
                            </option>
                        <%   }
                           } %>
                    </optgroup>
                    
                    <optgroup label="Đơn vị tính">
                        <% if (units != null) {
                             for (Unit unit : units) {
                                 boolean selected = (filterUnitID != null && filterUnitID == unit.getUnitID());
                        %>
                            <option value="unit_<%= unit.getUnitID() %>" <%= selected ? "selected" : "" %>>
                                <%= unit.getName() %>
                            </option>
                        <%   }
                           } %>
                    </optgroup>
                    
                    <optgroup label="Trạng thái">
                        <option value="status_Active" <%="Active".equals(filterStatus) ? "selected" : ""%>>Active</option>
                        <option value="status_Inactive" <%="Inactive".equals(filterStatus) ? "selected" : ""%>>Inactive</option>
                    </optgroup>
                </select>

                <!-- Hidden inputs to preserve filter state -->
                <input type="hidden" name="status" id="statusInput" value="<%= filterStatus != null ? filterStatus : "" %>">
                <input type="hidden" name="categoryID" id="categoryInput" value="<%= filterCategoryID != null ? filterCategoryID : "" %>">
                <input type="hidden" name="unitID" id="unitInput" value="<%= filterUnitID != null ? filterUnitID : "" %>">

                <!-- Search button -->
                <button type="submit" class="btn btn-primary" style="padding:.45rem 1rem;">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
                    Tìm
                </button>

                <% if ((keyword != null && !keyword.isBlank()) || (filterStatus != null && !filterStatus.isBlank()) || filterCategoryID != null || filterUnitID != null) { %>
                <a href="<%= ctx %>/products?view=<%= viewMode %>" class="btn btn-cancel" style="padding:.45rem 1rem; font-size:.8rem;">Xóa lọc</a>
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
                    <div class="card-sku">Qty: <%= p.getQuantity() %></div>
                    <div class="card-name"><%= p.getName() != null ? p.getName() : "—" %></div>
                    <div class="card-price"><%= p.getSellingPrice() != null ? vndFormat.format(p.getSellingPrice()) : "0 ₫" %></div>
                    <div class="card-meta">
                        <span>Đơn vị: <%= p.getUnitName() != null ? p.getUnitName() : ("#" + p.getUnitID()) %></span>
                        <span class="<%= badgeClass %>"><%= p.getStatus() != null ? p.getStatus() : "" %></span>
                    </div>
                    <div class="card-meta">
                        <span style="color:var(--muted);font-size:.72rem;">Danh mục: <%= p.getCategoryName() != null ? p.getCategoryName() : ("#" + p.getCategoryID()) %></span>
                    </div>
                    <div class="card-actions">
                        <button class="btn btn-warning" onclick="openProductModal('edit', 
                            '<%= p.getProductID() %>',
                            '<%= p.getCategoryID() %>',
                            '<%= (p.getName() != null ? p.getName() : "").replace("'", "\\'") %>',
                            '<%= p.getQuantity() %>',
                            '<%= p.getUnitID() %>',
                            '<%= p.getSellingPrice() != null ? p.getSellingPrice().toPlainString() : "0" %>',
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
                        <th>Tên sản phẩm</th>
                        <th>Danh mục</th>
                        <th>Đơn vị</th>
                        <th>Số lượng</th>
                        <th>Giá bán</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
<%
        if (empty) {
%>
                    <tr>
                        <td colspan="8" class="empty-state">
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
                        <td><strong><%= p.getName() != null ? p.getName() : "" %></strong></td>
                        <td style="color:var(--text);"><%= p.getCategoryName() != null ? p.getCategoryName() : ("#" + p.getCategoryID()) %></td>
                        <td style="color:var(--text);"><%= p.getUnitName() != null ? p.getUnitName() : ("#" + p.getUnitID()) %></td>
                        <td><%= p.getQuantity() %></td>
                        <td><%= p.getSellingPrice() != null ? vndFormat.format(p.getSellingPrice()) : "0 ₫" %></td>
                        <td><span class="<%= badgeClass %>"><%= p.getStatus() != null ? p.getStatus() : "" %></span></td>
                        <td>
                            <div class="action-cell">
                                <button class="btn btn-warning" onclick="openProductModal('edit', 
                                    '<%= p.getProductID() %>',
                                    '<%= p.getCategoryID() %>',
                                    '<%= (p.getName() != null ? p.getName() : "").replace("'", "\\'") %>',
                                    '<%= p.getQuantity() %>',
                                    '<%= p.getUnitID() %>',
                                    '<%= p.getSellingPrice() != null ? p.getSellingPrice().toPlainString() : "0" %>',
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
<%      String baseUrl = ctx + "/products?view=" + viewMode
                + (keyword != null && !keyword.isBlank() ? "&keyword=" + keyword : "")
                + (filterStatus != null && !filterStatus.isBlank() ? "&status=" + filterStatus : "")
                + (filterCategoryID != null ? "&categoryID=" + filterCategoryID : "")
                + (filterUnitID != null ? "&unitID=" + filterUnitID : "");
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

    <!-- ====== PRODUCT MODAL ====== -->
    <div id="productModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 id="modal-title">Thêm sản phẩm mới</h2>
                <button class="close-btn" onclick="closeProductModal()">&times;</button>
            </div>
            <form action="<%= ctx %>/products" method="post" id="product-form">
                <input type="hidden" name="action" id="modal-action" value="add">
                <input type="hidden" name="view" value="<%= viewMode %>">
                <input type="hidden" name="productID" id="modal-id">

                <div class="form-group">
                    <label for="modal-name">Tên sản phẩm</label>
                    <input type="text" id="modal-name" name="name" required placeholder="VD: Chuột không dây">
                </div>
                <div class="form-group">
                    <label for="modal-cat">Danh mục</label>
                    <select id="modal-cat" name="categoryID" required>
                        <% if (categories != null) {
                             for (Category cat : categories) { %>
                            <option value="<%= cat.getCategoryID() %>"><%= cat.getName() %></option>
                        <%   }
                           } %>
                    </select>
                </div>
                <div class="form-group">
                    <label for="modal-unit">Đơn vị tính</label>
                    <select id="modal-unit" name="unitID" required>
                        <% if (units != null) {
                             for (Unit unit : units) { %>
                            <option value="<%= unit.getUnitID() %>"><%= unit.getName() %></option>
                        <%   }
                           } %>
                    </select>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="modal-quantity">Số lượng</label>
                        <input type="number" id="modal-quantity" name="quantity" required value="0" min="0">
                    </div>
                    <div class="form-group">
                        <label for="modal-sellingPrice">Giá bán (VNĐ)</label>
                        <input type="number" id="modal-sellingPrice" name="sellingPrice" required placeholder="0" min="0">
                    </div>
                </div>
                <div class="form-row">
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
    <form id="deleteProductForm" action="<%= ctx %>/products" method="post" style="display:none;">
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

        function openProductModal(action, id, catId, name, quantity, unitId, sellingPrice, status) {
            document.getElementById('modal-action').value = action;
            if (action === 'edit') {
                document.getElementById('modal-title').innerText = 'Chỉnh sửa sản phẩm';
                document.getElementById('modal-submit').innerText = 'Cập nhật';
                document.getElementById('modal-id').value = id;
                document.getElementById('modal-cat').value = catId;
                document.getElementById('modal-name').value = name;
                document.getElementById('modal-unit').value = unitId;
                document.getElementById('modal-quantity').value = quantity;
                document.getElementById('modal-sellingPrice').value = sellingPrice;
                
                // Chuẩn hóa status để khớp với "Active" hoặc "Inactive"
                let normStatus = "Active";
                if (status) {
                    let s = status.trim().toUpperCase();
                    if (s === "INACTIVE" || s === "DEACTIVE" || s === "DEACTIVE_OLD") {
                        normStatus = "Inactive";
                    }
                }
                document.getElementById('modal-status').value = normStatus;
            } else {
                document.getElementById('modal-title').innerText = 'Thêm sản phẩm mới';
                document.getElementById('modal-submit').innerText = 'Lưu sản phẩm';
                document.getElementById('modal-id').value = '';
                document.getElementById('product-form').reset();
                document.getElementById('modal-cat').value = '1';
                document.getElementById('modal-unit').value = '1';
                document.getElementById('modal-quantity').value = '0';
                document.getElementById('modal-sellingPrice').value = '0';
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

        /* ── Unified Filter Handler ── */
        document.getElementById('unifiedFilter').addEventListener('change', function() {
            const selectedValue = this.value;
            
            // Clear all filter inputs first
            document.getElementById('statusInput').value = '';
            document.getElementById('categoryInput').value = '';
            document.getElementById('unitInput').value = '';
            
            // Parse and set the appropriate filter
            if (selectedValue === '') {
                // "All filters" selected - clear everything and submit
                document.getElementById('filterForm').submit();
            } else if (selectedValue.startsWith('cat_')) {
                // Category filter selected
                const categoryID = selectedValue.substring(4); // Remove "cat_" prefix
                document.getElementById('categoryInput').value = categoryID;
                document.getElementById('filterForm').submit();
            } else if (selectedValue.startsWith('unit_')) {
                // Unit filter selected
                const unitID = selectedValue.substring(5); // Remove "unit_" prefix
                document.getElementById('unitInput').value = unitID;
                document.getElementById('filterForm').submit();
            } else if (selectedValue.startsWith('status_')) {
                // Status filter selected
                const status = selectedValue.substring(7); // Remove "status_" prefix
                document.getElementById('statusInput').value = status;
                document.getElementById('filterForm').submit();
            }
        });
    </script>
    </main>
    <jsp:include page="/views/common/footer.jsp" />
</body>
</html>

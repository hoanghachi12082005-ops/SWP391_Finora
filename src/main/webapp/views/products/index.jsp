<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Product, model.Category, model.Unit, java.util.List, java.text.NumberFormat, java.util.Locale" %>

<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Quản lý Sản phẩm"/>
</jsp:include>

<style>
/* Inline styles from product index */
.product-hero {
    margin-bottom: 24px;
}
.product-hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: rgba(147, 0, 11, 0.1);
    color: var(--primary-color);
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 12px;
}
.product-hero-badge .material-icons { font-size: 16px; }
.product-hero-title {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 8px 0;
}
.product-hero-desc {
    color: var(--text-secondary);
    margin: 0;
}
/* Override internal layout classes to match D-flex */
.header { display: none; } /* hide internal header if it exists */
.modal { z-index: 1050; }
</style>

<div class="d-flex">
    <jsp:include page="/views/common/sidebar.jsp">
        <jsp:param name="active" value="products"/>
    </jsp:include>

    <div class="main-content flex-grow-1">
        <div class="product-hero">
            <div class="product-hero-badge">
                <span class="material-icons">inventory_2</span> Quản lý Sản phẩm
            </div>
            <h1 class="product-hero-title">Danh sách sản phẩm</h1>
            <p class="product-hero-desc">Quản lý thông tin, giá bán và tồn kho</p>
        </div>
        
        

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
                <!-- Preserve filter state -->
                <input type="hidden" name="keyword" value="<%= keyword != null ? keyword : "" %>">
                <input type="hidden" name="filterStatus" value="<%= filterStatus != null ? filterStatus : "" %>">
                <input type="hidden" name="filterCategoryID" value="<%= filterCategoryID != null ? filterCategoryID : "" %>">
                <input type="hidden" name="filterUnitID" value="<%= filterUnitID != null ? filterUnitID : "" %>">

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
                        <input type="number" id="modal-quantity" name="quantity" required value="0" min="0"
                        >
                    </div>
                    <div class="form-group">
                        <label for="modal-sellingPrice">Giá bán (VNĐ)</label>
                        <input type="number" id="modal-sellingPrice" name="sellingPrice" required placeholder="0" >
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
        <!-- Preserve filter state -->
        <input type="hidden" name="keyword" value="<%= keyword != null ? keyword : "" %>">
        <input type="hidden" name="filterStatus" value="<%= filterStatus != null ? filterStatus : "" %>">
        <input type="hidden" name="filterCategoryID" value="<%= filterCategoryID != null ? filterCategoryID : "" %>">
        <input type="hidden" name="filterUnitID" value="<%= filterUnitID != null ? filterUnitID : "" %>">
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
    
    </div>
</div>

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

<jsp:include page="/views/common/footer.jsp"/>

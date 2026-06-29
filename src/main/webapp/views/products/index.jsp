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
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Quản lý Sản phẩm"/>
</jsp:include>
<div class="app-container">
    <jsp:include page="../common/sidebar.jsp"/>
    <main class="main-content">
        <div class="container-fluid py-4">
            
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold">Quản lý Sản phẩm</h2>
                    <small class="text-muted">Quản lý toàn bộ hàng hóa trong hệ thống</small>
                </div>
                <div>
                    <button class="btn btn-danger" onclick="openProductModal('add')">
                        + Thêm sản phẩm
                    </button>
                </div>
            </div>

            <!-- Search -->
            <div class="card shadow-sm border-0">
                <div class="card-body">
                    <form id="filterForm" method="get" action="<%= ctx %>/products">
                        <input type="hidden" name="view" id="viewInput" value="<%= viewMode %>">
                        <input type="hidden" name="status" id="statusInput" value="<%= filterStatus != null ? filterStatus : "" %>">
                        <input type="hidden" name="categoryID" id="categoryInput" value="<%= filterCategoryID != null ? filterCategoryID : "" %>">
                        <input type="hidden" name="unitID" id="unitInput" value="<%= filterUnitID != null ? filterUnitID : "" %>">

                        <div class="d-flex gap-2 mb-3">
                            <div class="flex-grow-1">
                                <input type="text" name="keyword" id="searchInput" class="form-control" placeholder="Tìm theo tên hoặc SKU…" value="<%= keyword != null ? keyword : "" %>">
                            </div>
                            <div style="width: 200px;">
                                <select id="unifiedFilter" class="form-select">
                                    <option value="">Tất cả bộ lọc</option>
                                    <optgroup label="Danh mục">
                                        <% if (categories != null) {
                                             for (Category cat : categories) {
                                                 boolean selected = (filterCategoryID != null && filterCategoryID == cat.getId());
                                        %>
                                            <option value="cat_<%= cat.getId() %>" <%= selected ? "selected" : "" %>><%= cat.getName() %></option>
                                        <%   } } %>
                                    </optgroup>
                                    <optgroup label="Đơn vị tính">
                                        <% if (units != null) {
                                             for (Unit unit : units) {
                                                 boolean selected = (filterUnitID != null && filterUnitID == unit.getUnitID());
                                        %>
                                            <option value="unit_<%= unit.getUnitID() %>" <%= selected ? "selected" : "" %>><%= unit.getName() %></option>
                                        <%   } } %>
                                    </optgroup>
                                    <optgroup label="Trạng thái">
                                        <option value="status_Active" <%="Active".equals(filterStatus) ? "selected" : ""%>>Active</option>
                                        <option value="status_Inactive" <%="Inactive".equals(filterStatus) ? "selected" : ""%>>Inactive</option>
                                    </optgroup>
                                </select>
                            </div>
                            <div style="width: 120px;">
                                <button type="submit" class="btn btn-danger w-100">Tìm kiếm</button>
                            </div>
                            <% if ((keyword != null && !keyword.isBlank()) || (filterStatus != null && !filterStatus.isBlank()) || filterCategoryID != null || filterUnitID != null) { %>
                            <div style="width: 100px;">
                                <a href="<%= ctx %>/products?view=<%= viewMode %>" class="btn btn-outline-secondary w-100">Xóa lọc</a>
                            </div>
                            <% } %>
                        </div>
                    </form>

                    <hr>

                    <table class="table align-middle table-hover">
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
        boolean empty = (products == null || products.isEmpty());
        if (empty) {
%>
                            <tr>
                                <td colspan="8" class="text-center text-muted py-4">Không tìm thấy sản phẩm nào.</td>
                            </tr>
<%
        } else {
            for (Product p : products) {
%>
                            <tr>
                                <td>#<%= p.getProductID() %></td>
                                <td><strong><%= p.getName() != null ? p.getName() : "" %></strong></td>
                                <td><%= p.getCategoryName() != null ? p.getCategoryName() : ("#" + p.getCategoryID()) %></td>
                                <td><%= p.getUnitName() != null ? p.getUnitName() : ("#" + p.getUnitID()) %></td>
                                <td><%= p.getQuantity() %></td>
                                <td><strong class="text-danger"><%= p.getSellingPrice() != null ? vndFormat.format(p.getSellingPrice()) : "0 ₫" %></strong></td>
                                <td>
                                    <% if ("Active".equalsIgnoreCase(p.getStatus())) { %>
                                        <span class="badge bg-success">Hoạt động</span>
                                    <% } else { %>
                                        <span class="badge bg-secondary">Ngừng hoạt động</span>
                                    <% } %>
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-warning" onclick="openProductModal('edit', 
                                        '<%= p.getProductID() %>',
                                        '<%= p.getCategoryID() %>',
                                        '<%= (p.getName() != null ? p.getName() : "").replace("'", "\\'") %>',
                                        '<%= p.getQuantity() %>',
                                        '<%= p.getUnitID() %>',
                                        '<%= p.getSellingPrice() != null ? p.getSellingPrice().toPlainString() : "0" %>',
                                        '<%= p.getStatus() != null ? p.getStatus() : "Active" %>'
                                    )">Sửa</button>
                                    <button type="button" class="btn btn-sm btn-danger" onclick="deleteProduct('<%= p.getProductID() %>')">Xóa</button>
                                </td>
                            </tr>
<%
            }
        }
%>
                        </tbody>
                    </table>

                    <!-- Pagination -->
<%  if (totalPages > 1) { %>
                    <div class="d-flex justify-content-between align-items-center mt-4">
                        <div class="text-muted small">
                            Trang <strong><%= currentPage %></strong> / <strong><%= totalPages %></strong>
                        </div>
<%      String baseUrl = ctx + "/products?view=" + viewMode
                + (keyword != null && !keyword.isBlank() ? "&keyword=" + keyword : "")
                + (filterStatus != null && !filterStatus.isBlank() ? "&status=" + filterStatus : "")
                + (filterCategoryID != null ? "&categoryID=" + filterCategoryID : "")
                + (filterUnitID != null ? "&unitID=" + filterUnitID : "")
                + "&page=";
%>
                        <jsp:include page="../common/pagination.jsp">
                            <jsp:param name="currentPage" value="<%= currentPage %>"/>
                            <jsp:param name="totalPages" value="<%= totalPages %>"/>
                            <jsp:param name="url" value="<%= baseUrl %>"/>
                        </jsp:include>
                    </div>
<%  } %>
                </div>
            </div>
        </div>
    </main>
</div>

<!-- Modal Thêm/Sửa sản phẩm -->
<div class="modal fade" id="productModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="modal-title">Thêm sản phẩm mới</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" onclick="closeProductModal()"></button>
      </div>
      <div class="modal-body">
        <form action="<%= ctx %>/products" method="post" id="product-form">
            <input type="hidden" name="action" id="modal-action" value="add">
            <input type="hidden" name="view" value="<%= viewMode %>">
            <input type="hidden" name="productID" id="modal-id">
            <input type="hidden" name="keyword" value="<%= keyword != null ? keyword : "" %>">
            <input type="hidden" name="filterStatus" value="<%= filterStatus != null ? filterStatus : "" %>">
            <input type="hidden" name="filterCategoryID" value="<%= filterCategoryID != null ? filterCategoryID : "" %>">
            <input type="hidden" name="filterUnitID" value="<%= filterUnitID != null ? filterUnitID : "" %>">

            <div class="mb-3">
                <label class="form-label">Tên sản phẩm</label>
                <input type="text" id="modal-name" name="name" class="form-control" required placeholder="VD: Chuột không dây">
            </div>
            <div class="mb-3">
                <label class="form-label">Danh mục</label>
                <select id="modal-cat" name="categoryID" class="form-select" required>
                    <% if (categories != null) {
                         for (Category cat : categories) { %>
                        <option value="<%= cat.getId() %>"><%= cat.getName() %></option>
                    <%   }
                       } %>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label">Đơn vị tính</label>
                <select id="modal-unit" name="unitID" class="form-select" required>
                    <% if (units != null) {
                         for (Unit unit : units) { %>
                        <option value="<%= unit.getUnitID() %>"><%= unit.getName() %></option>
                    <%   }
                       } %>
                </select>
            </div>
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label class="form-label">Số lượng</label>
                    <input type="number" id="modal-quantity" name="quantity" class="form-control" required value="0" min="0">
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label">Giá bán (VNĐ)</label>
                    <input type="number" id="modal-sellingPrice" name="sellingPrice" class="form-control" min="0" required placeholder="0">
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">Trạng thái</label>
                <select id="modal-status" name="status" class="form-select" required>
                    <option value="Active">Active</option>
                    <option value="Inactive">Inactive</option>
                </select>
            </div>
            <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal" onclick="closeProductModal()">Huỷ</button>
                <button type="submit" class="btn btn-danger" id="modal-submit">Lưu sản phẩm</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>

<form id="deleteProductForm" action="<%= ctx %>/products" method="post" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="id" id="delete-id">
    <input type="hidden" name="page" value="<%= currentPage %>">
    <input type="hidden" name="view" value="<%= viewMode %>">
    <input type="hidden" name="keyword" value="<%= keyword != null ? keyword : "" %>">
    <input type="hidden" name="filterStatus" value="<%= filterStatus != null ? filterStatus : "" %>">
    <input type="hidden" name="filterCategoryID" value="<%= filterCategoryID != null ? filterCategoryID : "" %>">
    <input type="hidden" name="filterUnitID" value="<%= filterUnitID != null ? filterUnitID : "" %>">
</form>

<script>
    function deleteProduct(id) {
        if (confirm('Xóa sản phẩm này?')) {
            document.getElementById('delete-id').value = id;
            document.getElementById('deleteProductForm').submit();
        }
    }

    let bsModal;
    document.addEventListener("DOMContentLoaded", function() {
        if (typeof bootstrap !== 'undefined') {
            bsModal = new bootstrap.Modal(document.getElementById('productModal'));
        } else {
            console.warn("Bootstrap JS is not loaded!");
        }
    });

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
        if(bsModal) bsModal.show();
    }

    function closeProductModal() { 
        if(bsModal) bsModal.hide(); 
    }

    document.getElementById('unifiedFilter').addEventListener('change', function() {
        const selectedValue = this.value;
        document.getElementById('statusInput').value = '';
        document.getElementById('categoryInput').value = '';
        document.getElementById('unitInput').value = '';
        
        if (selectedValue === '') {
            document.getElementById('filterForm').submit();
        } else if (selectedValue.startsWith('cat_')) {
            document.getElementById('categoryInput').value = selectedValue.substring(4);
            document.getElementById('filterForm').submit();
        } else if (selectedValue.startsWith('unit_')) {
            document.getElementById('unitInput').value = selectedValue.substring(5);
            document.getElementById('filterForm').submit();
        } else if (selectedValue.startsWith('status_')) {
            document.getElementById('statusInput').value = selectedValue.substring(7);
            document.getElementById('filterForm').submit();
        }
    });
</script>

<jsp:include page="../common/footer.jsp"/>

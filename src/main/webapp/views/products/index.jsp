<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Product, model.Category, model.Unit, java.util.List, java.text.NumberFormat, java.util.Locale" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    NumberFormat vndFormat   = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : 'Nhân viên'}" />
<c:set var="canManage" value="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager' || roleName == 'WarehouseStaff'}" />

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="${canManage ? 'Quản lý Sản phẩm' : 'Danh sách sản phẩm'}"/>
</jsp:include>
<div class="app-container">
    <jsp:include page="../common/sidebar.jsp"/>
    <main class="main-content">
        <div class="container-fluid py-4">

<%-- Flash message từ session --%>
<%
    String flashMsg  = (String) session.getAttribute("message");
    String flashType = (String) session.getAttribute("messageType");
    if (flashMsg != null) {
        session.removeAttribute("message");
        session.removeAttribute("messageType");
%>
            <div class="alert alert-<%= flashType != null ? flashType : "info" %> alert-dismissible fade show" role="alert">
                <%= flashMsg %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
<%  } %>

            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <c:choose>
                        <c:when test="${canManage}">
                            <h2 class="fw-bold text-danger">Quản lý Sản phẩm</h2>
                            <small class="text-muted">Quản lý toàn bộ hàng hóa trong hệ thống</small>
                        </c:when>
                        <c:otherwise>
                            <h2 class="fw-bold text-danger">Danh sách sản phẩm</h2>
                            <small class="text-muted">Xem thông tin và giá bán của toàn bộ hàng hóa trong hệ thống</small>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${canManage}">
                    <div>
                        <button class="btn btn-danger" onclick="openProductModal('add')">
                            + Thêm sản phẩm
                        </button>
                    </div>
                </c:if>
            </div>

            <!-- Search -->
            <div class="card shadow-sm border-0">
                <div class="card-body">
                    <form id="filterForm" method="get" action="<%= ctx %>/products">
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
                                <a href="<%= ctx %>/products" class="btn btn-outline-secondary w-100">Xóa lọc</a>
                            </div>
                            <% } %>
                        </div>
                    </form>

                    <hr>

                    <table class="table align-middle table-hover">
                        <thead>
                            <tr>
                                <th style="width: 80px;">ID</th>
                                <th style="width: 80px; text-align: center;">Ảnh</th>
                                <th>Tên sản phẩm</th>
                                <th style="width: 180px;">Danh mục</th>
                                <th style="width: 120px;">Đơn vị</th>
                                <th style="width: 140px; text-align: right;">Giá bán</th>
                                <th style="width: 160px; text-align: center;">Trạng thái</th>
                                <c:if test="${canManage}">
                                    <th style="width: 150px; text-align: center;">Thao tác</th>
                                </c:if>
                            </tr>
                        </thead>
                        <tbody>
<%
        boolean empty = (products == null || products.isEmpty());
        if (empty) {
%>
                            <tr>
                                <td colspan="${canManage ? 8 : 7}" class="text-center text-muted py-4">Không tìm thấy sản phẩm nào.</td>
                            </tr>
<%
        } else {
            for (Product p : products) {
                String imgUrl = p.getImageUrl();
%>
                            <tr>
                                <td>#<%= p.getProductID() %></td>
                                <td class="text-center">
                                    <% if (imgUrl != null && !imgUrl.isBlank()) { %>
                                        <img src="<%= imgUrl %>" alt="product" style="width:48px;height:48px;object-fit:cover;border-radius:6px;border:1px solid #eee;">
                                    <% } else { %>
                                        <span class="text-muted" style="font-size:12px;">No image</span>
                                    <% } %>
                                </td>
                                <td><strong><%= p.getName() != null ? p.getName() : "" %></strong></td>
                                <td><%= p.getCategoryName() != null ? p.getCategoryName() : ("#" + p.getCategoryID()) %></td>
                                <td><%= p.getUnitName() != null ? p.getUnitName() : ("#" + p.getUnitID()) %></td>
                                <td class="text-end"><strong class="text-danger"><%= p.getSellingPrice() != null ? vndFormat.format(p.getSellingPrice()) : "0 ₫" %></strong></td>
                                <td class="text-center">
                                    <% if ("Active".equalsIgnoreCase(p.getStatus())) { %>
                                        <span class="badge bg-success">Hoạt động</span>
                                    <% } else { %>
                                        <span class="badge bg-secondary">Ngừng hoạt động</span>
                                    <% } %>
                                </td>
                                <c:if test="${canManage}">
                                    <td class="text-center">
                                        <button class="btn btn-sm btn-warning me-1" onclick="openProductModal('edit',
                                            '<%= p.getProductID() %>',
                                            '<%= p.getCategoryID() %>',
                                            '<%= (p.getName() != null ? p.getName() : "").replace("'", "\\'") %>',
                                            '<%= p.getUnitID() %>',
                                            '<%= p.getSellingPrice() != null ? p.getSellingPrice().toPlainString() : "0" %>',
                                            '<%= p.getStatus() != null ? p.getStatus() : "Active" %>',
                                            '<%= imgUrl != null ? imgUrl : "" %>'
                                        )">Sửa</button>
                                        <button type="button" class="btn btn-sm btn-danger" onclick="deleteProduct('<%= p.getProductID() %>')">Xóa</button>
                                    </td>
                                </c:if>
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
<%      String baseUrl = ctx + "/products?"
                + (keyword != null && !keyword.isBlank() ? "keyword=" + keyword + "&" : "")
                + (filterStatus != null && !filterStatus.isBlank() ? "status=" + filterStatus + "&" : "")
                + (filterCategoryID != null ? "categoryID=" + filterCategoryID + "&" : "")
                + (filterUnitID != null ? "unitID=" + filterUnitID + "&" : "")
                + "page=";
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
        <form action="<%= ctx %>/products" method="post" id="product-form" enctype="multipart/form-data">
            <input type="hidden" name="action" id="modal-action" value="add">
            <input type="hidden" name="productID" id="modal-id">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
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
            <div class="mb-3">
                <label class="form-label">Giá bán (VNĐ)</label>
                <input type="number" id="modal-sellingPrice" name="sellingPrice" class="form-control" min="0" required placeholder="0">
            </div>
            <div class="mb-3">
                <label class="form-label">Trạng thái</label>
                <select id="modal-status" name="status" class="form-select" required>
                    <option value="Active">Active</option>
                    <option value="Inactive">Inactive</option>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label">Ảnh sản phẩm</label>
                <input type="file" id="modal-image" name="imageFile" class="form-control" accept=".jpg,.jpeg,.png,.webp,.gif,.bmp,image/*" onchange="previewProductImage(event)">
                <div class="form-text">Chỉ chấp nhận file ảnh: JPG, JPEG, PNG, WEBP, GIF, BMP. Tối đa 3MB. Có thể bỏ trống khi tạo mới.</div>
                <div class="mt-2">
                    <img id="modal-image-preview" src="" alt="preview" style="display:none;max-width:160px;max-height:160px;object-fit:cover;border-radius:8px;border:1px solid #eee;">
                </div>
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
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="page" value="<%= currentPage %>">
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
        // Chặn submit nếu file không phải ảnh hợp lệ
        const productForm = document.getElementById('product-form');
        if (productForm) {
            productForm.addEventListener('submit', function(e) {
                const fileInput = document.getElementById('modal-image');
                const file = fileInput && fileInput.files && fileInput.files[0];
                if (!file) return; // không có ảnh thì cho qua
                if (!isValidImageFile(file)) {
                    e.preventDefault();
                    alert('File tải lên không phải ảnh hợp lệ. Chỉ chấp nhận: ' + ALLOWED_IMAGE_EXT.join(', ') + '.');
                    fileInput.value = '';
                    return false;
                }
                if (file.size > 3 * 1024 * 1024) {
                    e.preventDefault();
                    alert('Ảnh vượt quá 3MB. Vui lòng chọn ảnh khác.');
                    fileInput.value = '';
                    return false;
                }
            });
        }
    });

    function openProductModal(action, id, catId, name, unitId, sellingPrice, status, imageUrl) {
        document.getElementById('modal-action').value = action;
        const fileInput = document.getElementById('modal-image');
        const preview = document.getElementById('modal-image-preview');
        if (fileInput) fileInput.value = '';
        if (action === 'edit') {
            document.getElementById('modal-title').innerText = 'Chỉnh sửa sản phẩm';
            document.getElementById('modal-submit').innerText = 'Cập nhật';
            document.getElementById('modal-id').value = id;
            document.getElementById('modal-cat').value = catId;
            document.getElementById('modal-name').value = name;
            document.getElementById('modal-unit').value = unitId;
            document.getElementById('modal-sellingPrice').value = sellingPrice;

            let normStatus = "Active";
            if (status) {
                let s = status.trim().toUpperCase();
                if (s === "INACTIVE" || s === "DEACTIVE" || s === "DEACTIVE_OLD") {
                    normStatus = "Inactive";
                }
            }
            document.getElementById('modal-status').value = normStatus;

            if (imageUrl && imageUrl.length > 0) {
                preview.src = imageUrl;
                preview.style.display = 'inline-block';
            } else {
                preview.src = '';
                preview.style.display = 'none';
            }
        } else {
            document.getElementById('modal-title').innerText = 'Thêm sản phẩm mới';
            document.getElementById('modal-submit').innerText = 'Lưu sản phẩm';
            document.getElementById('modal-id').value = '';
            document.getElementById('product-form').reset();
            document.getElementById('modal-cat').value = '1';
            document.getElementById('modal-unit').value = '1';
            document.getElementById('modal-sellingPrice').value = '0';
            document.getElementById('modal-status').value = 'Active';
            if (preview) {
                preview.src = '';
                preview.style.display = 'none';
            }
        }
        if(bsModal) bsModal.show();
    }

    // Các đuôi ảnh hợp lệ ở client
    const ALLOWED_IMAGE_EXT = ['jpg', 'jpeg', 'png', 'webp', 'gif', 'bmp'];

    function getFileExt(filename) {
        if (!filename) return '';
        const dot = filename.lastIndexOf('.');
        if (dot < 0 || dot === filename.length - 1) return '';
        return filename.substring(dot + 1).toLowerCase();
    }

    function isValidImageFile(file) {
        if (!file) return false;
        const ext = getFileExt(file.name);
        if (ALLOWED_IMAGE_EXT.indexOf(ext) === -1) return false;
        // kiểm tra thêm MIME type nếu có
        if (file.type && file.type.indexOf('image/') !== 0) return false;
        return true;
    }

    function previewProductImage(event) {
        const file = event.target.files && event.target.files[0];
        const preview = document.getElementById('modal-image-preview');
        if (!file) {
            preview.src = '';
            preview.style.display = 'none';
            return;
        }
        // 1) Kiểm tra đuôi file phải là ảnh
        if (!isValidImageFile(file)) {
            alert('File tải lên không phải ảnh hợp lệ. Chỉ chấp nhận: ' + ALLOWED_IMAGE_EXT.join(', ') + '.');
            event.target.value = '';
            preview.src = '';
            preview.style.display = 'none';
            return;
        }
        // 2) Chặn sớm phía client nếu vượt 3MB
        if (file.size > 3 * 1024 * 1024) {
            alert('Ảnh vượt quá 3MB. Vui lòng chọn ảnh khác.');
            event.target.value = '';
            preview.src = '';
            preview.style.display = 'none';
            return;
        }
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.style.display = 'inline-block';
        };
        reader.readAsDataURL(file);
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

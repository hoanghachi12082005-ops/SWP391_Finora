<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- 
  ==========================================================================
  TRANG TẠO PHIẾU NHẬP KHO ĐỘC LẬP (views/inventory/import.jsp)
  - Được gọi khi người dùng truy cập vào URL `/warehouse/import`.
  - Hỗ trợ chọn nhà cung cấp, lọc danh sách sản phẩm liên kết với NCC, tự động tính tổng giá trị đơn nhập và số lượng.
  ==========================================================================
--%>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Nhập Kho - Nhập hàng từ nhà cung cấp"/>
</jsp:include>

<link href="${pageContext.request.contextPath}/assets/css/inventory/inventory-import.css" rel="stylesheet">

<div class="app-container">
    <jsp:include page="../common/sidebar.jsp"/>

    <main class="main-content">
        <div class="container-fluid py-4">
            
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold">Nhập Hàng Vào Kho</h2>
                    <small class="text-muted">
                        Tạo phiếu nhập hàng từ các đối tác nhà cung cấp.
                    </small>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/inventory/dashboard" class="btn btn-outline-secondary">
                        <span class="material-icons align-middle" style="font-size: 1.1rem; margin-right: 4px;">arrow_back</span> Dashboard
                    </a>
                </div>
            </div>

            <!-- Main Form -->
            <form action="${pageContext.request.contextPath}/inventory" method="post" id="importForm">
                <input type="hidden" name="action" value="saveImport">
                <div class="row g-4">
                    
                    <!-- Left: Supplier and Metadata Settings -->
                    <div class="col-lg-4">
                        <div class="card shadow-sm border-0 mb-4">
                            <div class="card-header bg-white py-3 border-0">
                                <h5 class="card-title fw-bold mb-0 text-winered">Thông tin phiếu nhập</h5>
                            </div>
                            <div class="card-body">
                                
                                <!-- Supplier Selection -->
                                <div class="mb-3">
                                    <label class="form-label fw-bold">Nhà cung cấp <span class="text-danger">*</span></label>
                                    <select name="supplierId" id="supplierSelect" class="form-select" required onchange="filterProducts()">
                                        <option value="">-- Chọn nhà cung cấp --</option>
                                        <c:forEach var="s" items="${activeSuppliers}">
                                            <option value="${s.supplierID}">${s.name} (${s.phone})</option>
                                        </c:forEach>
                                    </select>
                                    <div class="form-text">Chọn nhà cung cấp để hiển thị danh sách sản phẩm và đơn giá tương ứng.</div>
                                </div>

                                <!-- Filter Override Checkbox -->
                                <div class="mb-3 form-check form-switch pt-2">
                                    <input class="form-check-input" type="checkbox" id="showAllProducts" onchange="filterProducts()">
                                    <label class="form-check-label fw-semibold" for="showAllProducts">Hiển thị tất cả sản phẩm</label>
                                    <div class="form-text small text-muted">Bật để hiển thị cả các sản phẩm chưa liên kết với nhà cung cấp này.</div>
                                </div>

                                <hr>

                                <!-- Notes -->
                                <div class="mb-3">
                                    <label class="form-label fw-bold">Ghi chú phiếu nhập</label>
                                    <textarea name="description" class="form-control" rows="3" placeholder="Nhập ghi chú, lý do nhập kho hoặc số hóa đơn nếu có..."></textarea>
                                </div>

                                <!-- Warehouse Selection -->
                                <div class="mb-3">
                                    <label class="form-label fw-bold">Nhập vào kho <span class="text-danger">*</span></label>
                                    <select name="currentWarehouseId" class="form-select" required>
                                        <option value="${not empty sessionScope.selectedWarehouseId ? sessionScope.selectedWarehouseId : 1}" selected>Kho hiện tại</option>
                                    </select>
                                </div>

                            </div>
                        </div>

                        <!-- Card Summary of totals -->
                        <div class="card shadow-sm border-0 bg-winered text-white">
                            <div class="card-body p-4">
                                <small class="text-uppercase fw-bold opacity-75">TỔNG GIÁ TRỊ NHẬP</small>
                                <h2 class="fw-extrabold my-2" id="grandTotalDisplay">0 đ</h2>
                                <hr class="bg-white opacity-25">
                                <div class="d-flex justify-content-between text-white-50 small">
                                    <span>Tổng số mặt hàng:</span>
                                    <span id="selectedCountDisplay" class="text-white fw-bold">0</span>
                                </div>
                                <div class="d-flex justify-content-between text-white-50 small mt-2">
                                    <span>Tổng số lượng nhập:</span>
                                    <span id="totalQtyDisplay" class="text-white fw-bold">0</span>
                                </div>
                                <button type="submit" class="btn btn-light w-100 mt-4 fw-bold text-winered py-2" id="btnSubmitImport">
                                    Hoàn tất nhập hàng
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Right: Product list selection -->
                    <div class="col-lg-8">
                        <div class="card shadow-sm border-0">
                            <div class="card-header bg-white py-3 border-0 d-flex justify-content-between align-items-center">
                                <h5 class="card-title fw-bold mb-0">Danh sách sản phẩm</h5>
                                <span class="badge bg-light text-dark" id="productCountBadge">0 sản phẩm</span>
                            </div>

                            <div class="card-body p-0">
                                <div class="table-responsive" style="max-height: 550px; overflow-y: auto;">
                                    <table class="table table-hover align-middle mb-0" id="productTable">
                                        <thead class="table-light sticky-top">
                                            <tr>
                                                <th width="70" class="text-center">Chọn</th>
                                                <th>Sản phẩm</th>
                                                <th width="140">Giá bán lẻ (đ)</th>
                                                <th width="160">Giá nhập (đ)</th>
                                                <th width="120">Số lượng</th>
                                                <th width="140" class="text-end" style="padding-right: 15px;">Thành tiền</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${not empty activeProducts}">
                                                    <c:forEach var="p" items="${activeProducts}">
                                                        <tr id="row_${p.productID}" data-product-id="${p.productID}" data-selling-price="${p.sellingPrice}" class="product-row">
                                                            <td class="text-center">
                                                                <input class="form-check-input check-import" type="checkbox" name="productIds" value="${p.productID}" id="check_${p.productID}" onchange="toggleProductRow(this.value)">
                                                            </td>
                                                            <td>
                                                                <div class="fw-bold">${p.name}</div>
                                                                <small class="text-muted">Mã: SP${p.productID} | ĐVT: ${p.unitName}</small>
                                                            </td>
                                                            <td class="text-muted">
                                                                <fmt:formatNumber value="${p.sellingPrice}" type="number" maxFractionDigits="0"/>
                                                            </td>
                                                            <td>
                                                                <div class="input-group input-group-sm">
                                                                    <input type="number" name="importPrice_${p.productID}" id="importPrice_${p.productID}" class="form-control import-price-input" value="0" disabled required min="0" step="1000" onchange="calculateRow(this.id.split('_')[1])" oninput="calculateRow(this.id.split('_')[1])">
                                                                </div>
                                                            </td>
                                                            <td>
                                                                <input type="number" name="quantity_${p.productID}" id="quantity_${p.productID}" class="form-control form-control-sm qty-input" value="1" disabled required min="1" onchange="calculateRow(this.id.split('_')[1])" oninput="calculateRow(this.id.split('_')[1])">
                                                            </td>
                                                            <td class="text-end fw-bold text-winered" id="subtotal_${p.productID}" style="padding-right: 15px;">
                                                                0 đ
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <tr>
                                                        <td colspan="6" class="text-center py-5 text-muted">
                                                            Không có sản phẩm nào hoạt động trong hệ thống.
                                                        </td>
                                                    </tr>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="p-3 text-end text-muted small bg-light border-top">
                                    * Tích chọn ô đầu dòng của sản phẩm để kích hoạt nhập số lượng và giá nhập.
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </form>
        </div>
    </main>
</div>

<!-- Dynamic Local Filtering Logic -->
<textarea id="importConfigData" style="display:none;">${supplierProductsJson != null ? supplierProductsJson : "{}"}</textarea>
<script>
    (function() {
        const el = document.getElementById('importConfigData');
        window.IMPORT_CONFIG = {
            supplierProducts: el ? JSON.parse(el.value) : {}
        };
    })();
</script>
<script src="${pageContext.request.contextPath}/assets/js/inventory/inventory-import.js"></script>

<jsp:include page="../common/footer.jsp" />

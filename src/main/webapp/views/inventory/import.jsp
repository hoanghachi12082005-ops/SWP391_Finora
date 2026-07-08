<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Nhập Kho - Nhập hàng từ nhà cung cấp"/>
</jsp:include>

<style>
    .import-row-active {
        background-color: rgba(25, 135, 84, 0.05) !important;
    }
    .text-winered {
        color: #8b0000 !important;
    }
    .bg-winered {
        background-color: #8b0000 !important;
        color: white;
    }
    .btn-winered {
        background-color: #8b0000;
        color: white;
        border: none;
    }
    .btn-winered:hover {
        background-color: #a00000;
        color: white;
    }
</style>

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
                                        <option value="1" selected>Kho chính (Chi nhánh Hà Nội)</option>
                                        <option value="2">Kho phụ (Chi nhánh Hà Nội)</option>
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
                                                                <input class="form-check-input check-import" type="checkbox" name="productIds" value="${p.productID}" id="check_${p.productID}" onchange="toggleProductRow(${p.productID})">
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
                                                                    <input type="number" name="importPrice_${p.productID}" id="importPrice_${p.productID}" class="form-control import-price-input" value="0" disabled required min="0" step="1000" onchange="calculateRow(${p.productID})" oninput="calculateRow(${p.productID})">
                                                                </div>
                                                            </td>
                                                            <td>
                                                                <input type="number" name="quantity_${p.productID}" id="quantity_${p.productID}" class="form-control form-control-sm qty-input" value="1" disabled required min="1" onchange="calculateRow(${p.productID})" oninput="calculateRow(${p.productID})">
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
<script>
    // Bản đồ nhà cung cấp - sản phẩm được serialize từ Controller
    const supplierProducts = ${supplierProductsJson != null ? supplierProductsJson : '{}'};

    document.addEventListener("DOMContentLoaded", function() {
        filterProducts();
    });

    /**
     * Lọc sản phẩm cục bộ dựa trên Nhà cung cấp được chọn
     */
    function filterProducts() {
        const supplierSelect = document.getElementById("supplierSelect");
        const supplierId = supplierSelect.value;
        const showAll = document.getElementById("showAllProducts").checked;
        const rows = document.querySelectorAll(".product-row");
        
        let visibleCount = 0;
        
        rows.forEach(row => {
            const productId = row.getAttribute("data-product-id");
            const checkbox = row.querySelector(".check-import");
            const importPriceInput = row.querySelector(".import-price-input");
            
            let isVisible = false;
            let preNegotiatedPrice = null;
            
            if (showAll || !supplierId) {
                // Nếu chưa chọn nhà cung cấp hoặc bật chế độ xem tất cả, cho hiển thị hết
                isVisible = true;
                
                // Nếu có liên kết với nhà cung cấp hiện tại, vẫn lấy giá đàm phán
                if (supplierId && supplierProducts[supplierId] && supplierProducts[supplierId][productId] !== undefined) {
                    preNegotiatedPrice = supplierProducts[supplierId][productId];
                }
            } else {
                // Lọc sản phẩm thuộc nhà cung cấp này
                if (supplierProducts[supplierId] && supplierProducts[supplierId][productId] !== undefined) {
                    isVisible = true;
                    preNegotiatedPrice = supplierProducts[supplierId][productId];
                }
            }
            
            if (isVisible) {
                row.classList.remove("d-none");
                visibleCount++;
                
                // Nếu sản phẩm được hiển thị và có giá đàm phán, tự điền sẵn vào ô giá nhập
                if (preNegotiatedPrice !== null && !checkbox.checked) {
                    importPriceInput.value = Math.round(preNegotiatedPrice);
                } else if (!checkbox.checked) {
                    // Nếu không có giá đàm phán và chưa được tick, đặt mặc định bằng 70% giá bán lẻ
                    const sellingPrice = parseFloat(row.getAttribute("data-selling-price"));
                    importPriceInput.value = Math.round(sellingPrice * 0.7);
                }
            } else {
                row.classList.add("d-none");
                // Nếu bị ẩn đi, bỏ tick checkbox
                if (checkbox.checked) {
                    checkbox.checked = false;
                    toggleProductRow(productId);
                }
            }
        });
        
        document.getElementById("productCountBadge").innerText = visibleCount + " sản phẩm";
        calculateGrandTotal();
    }

    /**
     * Bật/Tắt hoạt động dòng sản phẩm khi được tick chọn nhập hàng
     */
    function toggleProductRow(productId) {
        const row = document.getElementById("row_" + productId);
        const checkbox = document.getElementById("check_" + productId);
        const priceInput = document.getElementById("importPrice_" + productId);
        const qtyInput = document.getElementById("quantity_" + productId);
        
        if (checkbox.checked) {
            row.classList.add("import-row-active");
            priceInput.disabled = false;
            qtyInput.disabled = false;
            qtyInput.focus();
        } else {
            row.classList.remove("import-row-active");
            priceInput.disabled = true;
            qtyInput.disabled = true;
        }
        
        calculateRow(productId);
    }

    /**
     * Tính toán thành tiền của một dòng sản phẩm
     */
    function calculateRow(productId) {
        const checkbox = document.getElementById("check_" + productId);
        const priceInput = document.getElementById("importPrice_" + productId);
        const qtyInput = document.getElementById("quantity_" + productId);
        const subtotalTd = document.getElementById("subtotal_" + productId);
        
        if (!checkbox.checked) {
            subtotalTd.innerText = "0 đ";
            calculateGrandTotal();
            return;
        }
        
        const price = parseFloat(priceInput.value) || 0;
        const qty = parseInt(qtyInput.value) || 0;
        const subtotal = price * qty;
        
        subtotalTd.innerText = formatCurrency(subtotal);
        calculateGrandTotal();
    }

    /**
     * Tính toán tổng giá trị phiếu nhập
     */
    function calculateGrandTotal() {
        const checkboxes = document.querySelectorAll(".check-import:checked");
        let grandTotal = 0;
        let totalQty = 0;
        let selectedCount = checkboxes.length;
        
        checkboxes.forEach(checkbox => {
            const productId = checkbox.value;
            const priceInput = document.getElementById("importPrice_" + productId);
            const qtyInput = document.getElementById("quantity_" + productId);
            
            const price = parseFloat(priceInput.value) || 0;
            const qty = parseInt(qtyInput.value) || 0;
            
            grandTotal += price * qty;
            totalQty += qty;
        });
        
        document.getElementById("grandTotalDisplay").innerText = formatCurrency(grandTotal);
        document.getElementById("selectedCountDisplay").innerText = selectedCount;
        document.getElementById("totalQtyDisplay").innerText = totalQty;
    }

    function formatCurrency(amount) {
        return amount.toLocaleString('vi-VN') + " đ";
    }

    // Xác nhận khi submit form
    document.getElementById("importForm").addEventListener("submit", function(e) {
        const checkboxes = document.querySelectorAll(".check-import:checked");
        if (checkboxes.length === 0) {
            e.preventDefault();
            alert("Vui lòng tích chọn ít nhất 1 sản phẩm để nhập hàng!");
            return;
        }
        
        if (!confirm("Xác nhận hoàn tất phiếu nhập hàng?")) {
            e.preventDefault();
        }
    });
</script>

<jsp:include page="../common/footer.jsp" />

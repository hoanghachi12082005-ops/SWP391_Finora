<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Liên kết sản phẩm nhà cung cấp"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="../common/sidebar.jsp"/>

    <main class="main-content">
        <div class="container-fluid py-4">
            
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold">Sản Phẩm Cung Cấp</h2>
                    <small class="text-muted">
                        Quản lý danh mục sản phẩm và giá nhập đàm phán của nhà cung cấp: <strong>${supplier.name}</strong>
                    </small>
                </div>
                <div>
                    <a href="suppliers" class="btn btn-outline-secondary">
                        <span class="material-icons align-middle" style="font-size: 1.1rem; margin-right: 4px;">arrow_back</span> Quay lại
                    </a>
                </div>
            </div>

            <div class="card shadow-sm border-0">
                <div class="card-header bg-white py-3 border-0">
                    <h5 class="card-title fw-bold mb-0 text-primary">Danh sách sản phẩm liên kết</h5>
                </div>
                
                <div class="card-body">
                    <form action="suppliers" method="post">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="save-products">
                        <input type="hidden" name="id" value="${supplier.supplierID}">

                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th width="80" class="text-center">Chọn</th>
                                        <th width="100">Mã SP</th>
                                        <th>Tên sản phẩm</th>
                                        <th>Danh mục</th>
                                        <th>Đơn vị tính</th>
                                        <th width="200">Đơn giá bán lẻ (VNĐ)</th>
                                        <th width="220">Giá nhập đàm phán (VNĐ)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty allProducts}">
                                            <c:forEach var="p" items="${allProducts}">
                                                <c:set var="pid" value="${p.productID}"/>
                                                <c:set var="isLinked" value="${linkedProducts[pid] != null}"/>
                                                <c:set var="importPrice" value="${isLinked ? linkedProducts[pid] : p.sellingPrice}"/>
                                                
                                                <tr>
                                                    <td class="text-center">
                                                        <input class="form-check-input" type="checkbox" name="productIds" value="${p.productID}" id="check_${p.productID}" ${isLinked ? 'checked' : ''} onchange="togglePrice(${p.productID})">
                                                    </td>
                                                    <td>SP${p.productID}</td>
                                                    <td><strong>${p.name}</strong></td>
                                                    <td>${p.categoryName}</td>
                                                    <td><span class="badge bg-light text-dark">${p.unitName}</span></td>
                                                    <td>
                                                        <fmt:formatNumber value="${p.sellingPrice}" type="number" maxFractionDigits="0"/> đ
                                                    </td>
                                                    <td>
                                                        <div class="input-group input-group-sm">
                                                            <input type="number" name="price_${p.productID}" id="price_${p.productID}" class="form-control" value="<fmt:formatNumber value="${importPrice}" pattern="0"/>" ${isLinked ? '' : 'disabled'} required min="0" step="1000">
                                                            <span class="input-group-text">đ</span>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="7" class="text-center py-4 text-muted">Không có sản phẩm nào hoạt động trong hệ thống.</td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>

                        <div class="d-flex justify-content-end gap-2 mt-4">
                            <a href="suppliers" class="btn btn-outline-secondary px-4">Hủy</a>
                            <button type="submit" class="btn btn-primary px-4">Lưu liên kết sản phẩm</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>

<script>
    function togglePrice(productId) {
        const checkbox = document.getElementById('check_' + productId);
        const priceInput = document.getElementById('price_' + productId);
        
        if (checkbox.checked) {
            priceInput.disabled = false;
            priceInput.focus();
        } else {
            priceInput.disabled = true;
        }
    }
</script>

<jsp:include page="../common/footer.jsp" />

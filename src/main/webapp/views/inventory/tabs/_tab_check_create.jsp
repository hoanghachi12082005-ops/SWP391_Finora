<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- 
  ==========================================================================
  TAB GIAO DIỆN TẠO/SỬA PHIẾU KIỂM KHO (_tab_check_create.jsp)
  - Cung cấp form giao diện để nhân viên đếm hàng thực tế.
  - Cho phép quét mã/tìm kiếm sản phẩm, nhập số lượng tồn thực tế. Hệ thống tự động tính toán chênh lệch (lệch thừa/thiếu) so với tồn lý thuyết trong Database.
  ==========================================================================
--%>



<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-4 border-bottom-0 pb-0">
        <div>
            <h5 class="mb-0 fw-bold text-dark">${not empty check ? 'Chỉnh Sửa Phiếu Kiểm Kho' : 'Nhập Phiếu Kiểm Kho'}</h5>
            <small class="text-muted">${not empty check ? 'Mã phiếu: '.concat(check.checkCode) : 'Đang kiểm kê cho kho hiện tại'}</small>
        </div>
        <a href="${pageContext.request.contextPath}/inventory?tab=check&warehouseId=${selectedWarehouseId}" class="btn btn-outline-secondary btn-sm" style="border-radius: 8px;">
            <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">arrow_back</span>
            Quay Lại
        </a>
    </div>

    <div class="card-body">
        <!-- Action Cards Section -->
        <div class="row g-3 mb-4">
            <div class="col-md-6 text-center">
                <div class="action-card p-4 border bg-white h-100" onclick="exportCheckTemplate()">
                    <span class="material-icons text-primary mb-2" style="font-size: 44px;">download</span>
                    <h6 class="fw-bold text-dark mb-1">1. Lấy Tồn Kho Hiện Tại (Tải Excel)</h6>
                    <p class="text-muted small mb-0 px-3">Tải xuống tệp Excel chứa danh sách sản phẩm và số lượng hệ thống hiện tại để nhân viên kho điền số lượng đếm thực tế</p>
                </div>
            </div>
            <div class="col-md-6 text-center">
                <div class="action-card p-4 border bg-white h-100" onclick="triggerExcelImport()">
                    <span class="material-icons text-success mb-2" style="font-size: 44px;">cloud_upload</span>
                    <h6 class="fw-bold text-dark mb-1">2. Nhập Sau Khi Kiểm (Excel)</h6>
                    <p class="text-muted small mb-0 px-3">Tải lên tệp Excel chứa kết quả số lượng đếm thực tế sau khi nhân viên đã kiểm kho xong để đối chiếu tự động</p>
                </div>
            </div>
            <input type="file" id="excelImportInput" style="display:none;" accept=".csv,.xls,.xlsx" onchange="importCheckExcel(event)" />
        </div>

        <div class="mb-3 mt-4">
            <h6 class="fw-bold text-dark mb-0">Danh Sách Sản Phẩm Kiểm Kho</h6>
        </div>

        <!-- Form Section -->
        <form action="${pageContext.request.contextPath}/inventory" method="POST" id="checkForm">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="${not empty check ? 'updateCheck' : 'saveCheck'}">
            <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
            <c:if test="${not empty check}">
                <input type="hidden" name="checkId" value="${check.checkId}">
            </c:if>

            <div class="smart-table mb-4">
                <table class="table mb-0">
                    <thead>
                        <tr>
                            <th width="25%">Sản Phẩm</th>
                            <th width="12%">Danh Mục</th>
                            <th width="12%" class="text-center">Tồn Hệ Thống</th>
                            <th width="12%" class="text-center">Tồn Thực Tế</th>
                            <th width="11%" class="text-center">Chênh Lệch</th>
                            <th width="23%">Ghi Chú</th>
                            <th width="5%" class="text-center">Xóa</th>
                        </tr>
                    </thead>
                    <tbody id="checkTableBody">
                        <tr id="emptyRow">
                            <td colspan="7">
                                <div class="empty-state text-center py-5">
                                    <span class="material-icons mb-3" style="font-size: 48px; color: #cbd5e1;">description</span>
                                    <h6 class="fw-bold mb-2">Chưa có dữ liệu kiểm đếm</h6>
                                    <p class="text-muted small mb-0">Tải lên tệp Excel ở Thẻ số 2 để điền tự động danh sách đối chiếu và lưu phiếu</p>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="d-flex justify-content-end">
                <button type="submit" class="page-action-btn px-4 py-2" id="submitBtn" disabled>
                    <span class="material-icons" style="font-size: 18px; vertical-align: text-bottom; margin-right: 4px;">save</span>
                    ${not empty check ? 'Lưu Phiếu Cập Nhật' : 'Lưu Phiếu Nhập Kiểm Kho'}
                </button>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"></script>

<table id="checkDetailsTable" style="display:none;">
    <tbody>
        <c:forEach var="item" items="${checkDetails}">
            <tr data-product-id="${item.productId}"
                data-product-name="<c:out value='${item.productName}'/>"
                data-category-name="<c:out value='${item.categoryName}'/>"
                data-system-stock="${item.systemQty}"
                data-actual-qty="${item.actualQty}"
                data-note="<c:out value='${item.note}'/>">
            </tr>
        </c:forEach>
    </tbody>
</table>
<script>
    (function() {
        window.CHECK_DETAILS_DATA = [];
        const tableEl = document.getElementById('checkDetailsTable');
        if (tableEl) {
            tableEl.querySelectorAll('tr').forEach(tr => {
                window.CHECK_DETAILS_DATA.push({
                    productId: parseInt(tr.getAttribute('data-product-id')),
                    productName: tr.getAttribute('data-product-name'),
                    categoryName: tr.getAttribute('data-category-name'),
                    systemStock: parseInt(tr.getAttribute('data-system-stock')),
                    actualQty: parseInt(tr.getAttribute('data-actual-qty')),
                    note: tr.getAttribute('data-note')
                });
            });
        }
    })();
</script>

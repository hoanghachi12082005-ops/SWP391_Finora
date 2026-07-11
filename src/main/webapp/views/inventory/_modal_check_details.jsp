<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="modal-header border-bottom-0 pb-0">
    <h5 class="modal-title fw-bold text-dark" id="modalTitle">Chi Tiết Phiếu Kiểm Kê</h5>
    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>

<div class="modal-body pt-3">
    <!-- Metadata Info -->
    <div class="row g-3 mb-4 p-3 bg-light rounded-3 border">
        <div class="col-md-6 col-lg-3">
            <span class="text-muted small d-block">Mã Phiếu:</span>
            <strong class="text-primary fs-6">${check.checkCode}</strong>
        </div>
        <div class="col-md-6 col-lg-3">
            <span class="text-muted small d-block">Trạng Thái:</span>
            <c:choose>
                <c:when test="${check.status == 'PENDING'}">
                    <span class="badge bg-warning text-dark" style="border-radius: 6px; padding: 4px 8px; font-size:12px;">CHỜ DUYỆT</span>
                </c:when>
                <c:when test="${check.status == 'APPROVED'}">
                    <span class="badge bg-success text-white" style="border-radius: 6px; padding: 4px 8px; font-size:12px;">ĐÃ DUYỆT</span>
                </c:when>
                <c:when test="${check.status == 'CANCELLED'}">
                    <span class="badge bg-danger text-white" style="border-radius: 6px; padding: 4px 8px; font-size:12px;">ĐÃ HỦY</span>
                </c:when>
            </c:choose>
        </div>
        <div class="col-md-6 col-lg-3">
            <span class="text-muted small d-block">Thời Gian Lập:</span>
            <strong class="text-dark">${check.formattedCreatedAt}</strong>
        </div>
        <div class="col-md-6 col-lg-3">
            <span class="text-muted small d-block">Kho Hàng:</span>
            <strong class="text-dark">${check.warehouseName}</strong>
        </div>
        <div class="col-md-6 col-lg-4 mt-2">
            <span class="text-muted small d-block">Người Lập Phiếu:</span>
            <strong class="text-dark">${check.createdByName}</strong>
        </div>
        <div class="col-md-6 col-lg-4 mt-2">
            <span class="text-muted small d-block">Người Phê Duyệt:</span>
            <strong class="text-dark">${not empty check.approvedByName ? check.approvedByName : 'Chưa có'}</strong>
        </div>
        <div class="col-md-12 col-lg-4 mt-2">
            <span class="text-muted small d-block">Tổng Sai Lệch:</span>
            <strong class="text-danger">${check.totalDiscrepancy} SP</strong>
        </div>
    </div>

    <!-- Product list -->
    <h6 class="fw-bold text-dark mb-3">Danh Sách Sản Phẩm Kiểm Kê</h6>
    <div class="table-responsive border rounded-3 overflow-hidden">
        <table class="table table-hover align-middle mb-0" style="font-size:14px;">
            <thead class="table-light">
                <tr>
                    <th width="35%">Sản Phẩm</th>
                    <th width="15%">Danh Mục</th>
                    <th width="15%" class="text-center">Tồn Hệ Thống</th>
                    <th width="15%" class="text-center">Tồn Thực Tế</th>
                    <th width="15%" class="text-center">Chênh Lệch</th>
                    <th width="20%">Ghi Chú</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${checkDetails}">
                    <tr>
                        <td><strong class="text-dark">${item.productName}</strong></td>
                        <td><span class="text-muted small">${item.categoryName}</span></td>
                        <td class="text-center fw-medium">${item.systemQty}</td>
                        <td class="text-center fw-medium text-primary">${item.actualQty}</td>
                        <td class="text-center fw-bold ${item.discrepancy == 0 ? 'text-success' : 'text-danger'}">
                            <c:choose>
                                <c:when test="${item.discrepancy > 0}">+${item.discrepancy}</c:when>
                                <c:otherwise>${item.discrepancy}</c:otherwise>
                            </c:choose>
                        </td>
                        <td><span class="text-muted small">${not empty item.note ? item.note : '-'}</span></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<c:set var="role" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />
<div class="modal-footer border-top-0 pt-0">
    <button type="button" class="btn btn-outline-danger d-flex align-items-center gap-1" onclick="printCheckVoucher()" style="border-radius: 8px;">
        <span class="material-icons" style="font-size: 18px;">print</span>
        In Phiếu
    </button>
    <c:if test="${check.status == 'PENDING' && (role == 'Owner' || role == 'StoreManager')}">
        <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline">
            <input type="hidden" name="action" value="approveCheck">
            <input type="hidden" name="checkId" value="${check.checkId}">
            <input type="hidden" name="currentWarehouseId" value="${check.warehouseId}">
            <button type="submit" class="btn btn-success d-flex align-items-center gap-1" onclick="return confirm('Phê duyệt phiếu kiểm kho này và thực hiện cân bằng tồn kho?')" style="border-radius: 8px;">
                <span class="material-icons" style="font-size: 18px;">check</span> Duyệt Phiếu
            </button>
        </form>
        <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline ms-1">
            <input type="hidden" name="action" value="cancelCheck">
            <input type="hidden" name="checkId" value="${check.checkId}">
            <input type="hidden" name="currentWarehouseId" value="${check.warehouseId}">
            <button type="submit" class="btn btn-danger d-flex align-items-center gap-1" onclick="return confirm('Từ chối và hủy bỏ phiếu kiểm kho này?')" style="border-radius: 8px;">
                <span class="material-icons" style="font-size: 18px;">close</span> Hủy Phiếu
            </button>
        </form>
    </c:if>
    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style="border-radius: 8px;">Đóng</button>
</div>

<script>
function printCheckVoucher() {
    let printWindow = window.open('', '_blank');
    let html = '<html>'
             + '<head>'
             + '    <title>Phiếu Kiểm Kho - ' + '${check.checkCode}' + '</title>'
             + '    <style>'
             + '        body { font-family: "Arial", sans-serif; padding: 20px; color: #333; }'
             + '        .header { text-align: center; margin-bottom: 30px; }'
             + '        .header h2 { margin: 0 0 10px; text-transform: uppercase; }'
             + '        .info-table { width: 100%; margin-bottom: 20px; border-collapse: collapse; }'
             + '        .info-table td { padding: 6px; border: none; font-size: 14px; }'
             + '        .main-table { width: 100%; border-collapse: collapse; margin-top: 20px; }'
             + '        .main-table th, .main-table td { border: 1px solid #333; padding: 10px; text-align: left; font-size: 13px; }'
             + '        .main-table th { background-color: #f2f2f2; text-transform: uppercase; font-weight: bold; }'
             + '        .text-center { text-align: center; }'
             + '        .text-end { text-align: right; }'
             + '        .footer-sign { margin-top: 50px; display: flex; justify-content: space-between; }'
             + '        .sign-box { width: 45%; text-align: center; }'
             + '        @media print {'
             + '            @page { size: A4; margin: 15mm; }'
             + '            body { padding: 0; }'
             + '        }'
             + '    </style>'
             + '</head>'
             + '<body>'
             + '    <div class="header">'
             + '        <h2>Phiếu Kiểm Kho Sản Phẩm</h2>'
             + '        <div><strong>Mã phiếu:</strong> ' + '${check.checkCode}' + '</div>'
             + '    </div>'
             + '    '
             + '    <table class="info-table">'
             + '        <tr>'
             + '            <td width="50%"><strong>Kho hàng:</strong> ' + '${check.warehouseName}' + '</td>'
             + '            <td width="50%"><strong>Thời gian lập:</strong> ' + '${check.formattedCreatedAt}' + '</td>'
             + '        </tr>'
             + '        <tr>'
             + '            <td><strong>Người lập:</strong> ' + '${check.createdByName}' + '</td>'
             + '            <td><strong>Người duyệt:</strong> ' + '${not empty check.approvedByName ? check.approvedByName : "Chưa phê duyệt"}' + '</td>'
             + '        </tr>'
             + '        <tr>'
             + '            <td><strong>Trạng thái:</strong> ' + '${check.status == "PENDING" ? "Chờ duyệt" : (check.status == "APPROVED" ? "Đã duyệt" : "Đã hủy")}' + '</td>'
             + '            <td><strong>Tổng sai lệch:</strong> <span style="color: red; font-weight: bold;">' + '${check.totalDiscrepancy}' + ' SP</span></td>'
             + '        </tr>'
             + '    </table>'
             + '    '
             + '    <table class="main-table">'
             + '        <thead>'
             + '            <tr>'
             + '                <th width="5%" class="text-center">STT</th>'
             + '                <th width="35%">Sản Phẩm</th>'
             + '                <th width="15%">Danh Mục</th>'
             + '                <th width="15%" class="text-center">Tồn Hệ Thống</th>'
             + '                <th width="15%" class="text-center">Tồn Thực Tế</th>'
             + '                <th width="15%" class="text-center">Chênh Lệch</th>'
             + '            </tr>'
             + '        </thead>'
             + '        <tbody>';
             
    const productRows = document.querySelectorAll('.modal-body tbody tr');
    let stt = 1;
    productRows.forEach(tr => {
        const cols = tr.querySelectorAll('td');
        if (cols.length < 5) return;
        const name = cols[0].innerText.trim();
        const cat = cols[1].innerText.trim();
        const sys = cols[2].innerText.trim();
        const act = cols[3].innerText.trim();
        const diff = cols[4].innerText.trim();
        
        html += '<tr>'
             + '    <td class="text-center">' + stt + '</td>'
             + '    <td><strong>' + name + '</strong></td>'
             + '    <td>' + cat + '</td>'
             + '    <td class="text-center">' + sys + '</td>'
             + '    <td class="text-center">' + act + '</td>'
             + '    <td class="text-center" style="font-weight: bold; color: ' + (diff.includes("0") ? "green" : "red") + ';">' + diff + '</td>'
             + '</tr>';
        stt++;
    });
    
    html += '        </tbody>'
         + '    </table>'
         + '    '
         + '    <div class="footer-sign">'
         + '        <div class="sign-box">'
         + '            <strong>Nhân viên kiểm kho</strong><br>'
         + '            <small>(Ký và ghi rõ họ tên)</small>'
         + '            <br><br><br><br>'
         + '            ....................................................'
         + '        </div>'
         + '        <div class="sign-box">'
         + '            <strong>Quản lý / Người phê duyệt</strong><br>'
         + '            <small>(Ký và ghi rõ họ tên)</small>'
         + '            <br><br><br><br>'
         + '            ....................................................'
         + '        </div>'
         + '    </div>'
         + '    '
         + '    <' + 'script>'
         + '        window.onload = function() {'
         + '            window.print();'
         + '            setTimeout(function() { window.close(); }, 500);'
         + '        }'
         + '    </' + 'script>'
         + '</body>'
         + '</html>';
         
    printWindow.document.write(html);
    printWindow.document.close();
}
</script>

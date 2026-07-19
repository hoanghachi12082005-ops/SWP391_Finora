/**
 * Inventory Main Script
 * Handles ticket details, modal actions, and printing functionalities.
 */

// Ensure configuration is loaded
const contextPath = window.INVENTORY_CONFIG?.contextPath || '';
const selectedWarehouseId = window.INVENTORY_CONFIG?.selectedWarehouseId || '';

/**
 * View ticket details (Stock Transfer details) in modal
 */
function viewTicketDetails(ticketId, showAll) {
    const modalContent = document.getElementById('ticketDetailsModalContent');
    if (!modalContent) return;

    modalContent.innerHTML = `
        <div class="modal-body text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="mt-2 text-muted">Đang tải dữ liệu...</p>
        </div>
    `;
    
    const myModal = new bootstrap.Modal(document.getElementById('ticketDetailsModal'));
    myModal.show();
    
    let url = `${contextPath}/inventory?action=viewTicket&ticketId=${ticketId}`;
    if (selectedWarehouseId) {
        url += `&warehouseId=${selectedWarehouseId}`;
    }
    if (showAll) {
        url += '&showAll=true';
    }
    
    fetch(url)
        .then(response => response.text())
        .then(html => {
            modalContent.innerHTML = html;
        })
        .catch(err => {
            console.error('Error fetching ticket details:', err);
            modalContent.innerHTML = `
                <div class="modal-body py-5 text-center text-danger">
                    <i class="ph ph-warning-circle fs-1 mb-2"></i>
                    <p>Lỗi khi tải dữ liệu phiếu.</p>
                </div>
            `;
        });
}

/**
 * View stocktake (check) details in modal
 */
function viewCheckDetails(checkId) {
    const modalContent = document.getElementById('ticketDetailsModalContent');
    if (!modalContent) return;

    modalContent.innerHTML = `
        <div class="modal-body text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="mt-2 text-muted">Đang tải dữ liệu...</p>
        </div>
    `;
    
    const myModal = new bootstrap.Modal(document.getElementById('ticketDetailsModal'));
    myModal.show();
    
    const url = `${contextPath}/inventory?action=viewCheckDetails&checkId=${checkId}`;
    
    fetch(url)
        .then(response => response.text())
        .then(html => {
            modalContent.innerHTML = html;
        })
        .catch(err => {
            console.error('Error fetching check details:', err);
            modalContent.innerHTML = `
                <div class="modal-body py-5 text-center text-danger">
                    <i class="ph ph-warning-circle fs-1 mb-2"></i>
                    <p>Lỗi khi tải dữ liệu phiếu kiểm kê.</p>
                </div>
            `;
        });
}

/**
 * View purchase order details in modal
 */
function viewOrderDetails(orderId) {
    const modalContent = document.getElementById('ticketDetailsModalContent');
    if (!modalContent) return;

    modalContent.innerHTML = `
        <div class="modal-body text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="mt-2 text-muted">Đang tải dữ liệu...</p>
        </div>
    `;
    
    const myModal = new bootstrap.Modal(document.getElementById('ticketDetailsModal'));
    myModal.show();
    
    const url = `${contextPath}/inventory?action=viewOrderDetails&orderId=${orderId}`;
    
    fetch(url)
        .then(response => response.text())
        .then(html => {
            modalContent.innerHTML = html;
        })
        .catch(err => {
            console.error('Error fetching order details:', err);
            modalContent.innerHTML = `
                <div class="modal-body py-5 text-center text-danger">
                    <i class="ph ph-warning-circle fs-1 mb-2"></i>
                    <p>Lỗi khi tải dữ liệu phiếu.</p>
                </div>
            `;
        });
}

/**
 * Open reject modal for stock transfers
 */
function openRejectModal(ticketId) {
    const rejectIdInput = document.getElementById('rejectTransferId');
    if (rejectIdInput) {
        rejectIdInput.value = ticketId;
    }
    const myModal = new bootstrap.Modal(document.getElementById('rejectModal'));
    myModal.show();
}

/**
 * Open receipt verification modal with details of discrepancies
 */
function openReceiptModal(ticketId) {
    const modalContent = document.getElementById('receiptModalContent');
    if (!modalContent) return;

    modalContent.innerHTML = `
        <div class="modal-body text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="mt-2 text-muted">Đang tải dữ liệu phiếu...</p>
        </div>
    `;
    
    const myModal = new bootstrap.Modal(document.getElementById('receiptModal'));
    myModal.show();
    
    const url = `${contextPath}/inventory?action=viewReceiptForm&ticketId=${ticketId}&warehouseId=${selectedWarehouseId}`;
    
    fetch(url)
        .then(response => response.text())
        .then(html => {
            modalContent.innerHTML = html;
        })
        .catch(err => {
            console.error('Error fetching receipt form:', err);
            modalContent.innerHTML = `
                <div class="modal-body py-5 text-center text-danger">
                    <i class="ph ph-warning-circle fs-1 mb-2"></i>
                    <p>Lỗi khi tải dữ liệu phiếu.</p>
                </div>
            `;
        });
}

/**
 * Print stock check voucher
 */
function printCheckVoucher() {
    const code = document.getElementById('printCheckCode')?.innerText || '';
    const statusText = document.getElementById('printCheckStatus')?.innerText.trim() || '';
    const createdAt = document.getElementById('printCheckCreatedAt')?.innerText || '';
    const warehouseName = document.getElementById('printCheckWarehouseName')?.innerText || '';
    const createdByName = document.getElementById('printCheckCreatedByName')?.innerText || '';
    const approvedByName = document.getElementById('printCheckApprovedByName')?.innerText || '';
    const totalDiscrepancy = document.getElementById('printCheckTotalDiscrepancy')?.innerText || '';

    const printWindow = window.open('', '_blank');
    if (!printWindow) {
        alert('Vui lòng cho phép trình duyệt mở popup để in phiếu.');
        return;
    }

    let html = '<html>'
             + '<head>'
             + '    <title>Phiếu Kiểm Kho - ' + code + '</title>'
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
             + '        <div><strong>Mã phiếu:</strong> ' + code + '</div>'
             + '    </div>'
             + '    '
             + '    <table class="info-table">'
             + '        <tr>'
             + '            <td width="50%"><strong>Kho hàng:</strong> ' + warehouseName + '</td>'
             + '            <td width="50%"><strong>Thời gian lập:</strong> ' + createdAt + '</td>'
             + '        </tr>'
             + '        <tr>'
             + '            <td><strong>Người lập:</strong> ' + createdByName + '</td>'
             + '            <td><strong>Người duyệt:</strong> ' + approvedByName + '</td>'
             + '        </tr>'
             + '        <tr>'
             + '            <td><strong>Trạng thái:</strong> ' + statusText + '</td>'
             + '            <td><strong>Tổng sai lệch:</strong> <span style="color: red; font-weight: bold;">' + totalDiscrepancy + '</span></td>'
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
             
    const productRows = document.querySelectorAll('#ticketDetailsModalContent tbody tr');
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

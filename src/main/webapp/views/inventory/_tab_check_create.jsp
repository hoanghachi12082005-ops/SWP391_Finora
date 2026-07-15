<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    .action-card {
        cursor: pointer;
        border: 2px dashed #cbd5e1 !important;
        border-radius: 12px !important;
        transition: all 0.25s ease-in-out;
        min-height: 155px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
    }
    .action-card:hover {
        border-color: var(--primary-color, #800000) !important;
        background-color: #f8fafc !important;
        transform: translateY(-2px);
        box-shadow: 0 8px 16px -2px rgba(0, 0, 0, 0.05);
    }
    .smart-table { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e2e8f0; }
    .smart-table th { background: #f8fafc; color: #64748b; font-weight: 600; padding: 16px; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e2e8f0; }
    .smart-table td { padding: 16px; vertical-align: middle; border-bottom: 1px solid #f1f5f9; }
    .empty-state { padding: 40px; text-align: center; color: #94a3b8; }
</style>

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

<script>
    const tableBody = document.getElementById('checkTableBody');
    const emptyRow = document.getElementById('emptyRow');
    const submitBtn = document.getElementById('submitBtn');
    const selectedProductIds = new Set();

    function addProduct(p) {
        if (selectedProductIds.has(p.productId)) {
            Toast.fire({
                icon: 'warning',
                title: 'Sản phẩm này đã có trong danh sách kiểm kê!'
            });
            return;
        }

        if (emptyRow) {
            emptyRow.style.display = 'none';
        }

        selectedProductIds.add(p.productId);

        const row = document.createElement('tr');
        row.id = 'row-' + p.productId;
        row.setAttribute('data-id', p.productId);
        row.innerHTML = '<td>'
            + '    <input type="hidden" name="productId[]" value="' + p.productId + '">'
            + '    <div class="fw-semibold text-dark">' + p.productName + '</div>'
            + '</td>'
            + '<td>'
            + '    <span class="text-muted small">' + p.categoryName + '</span>'
            + '</td>'
            + '<td class="text-center fw-medium">'
            + '    <input type="hidden" name="systemQty[]" value="' + p.systemStock + '">'
            + '    <span>' + p.systemStock + '</span>'
            + '</td>'
            + '<td>'
            + '    <input type="text" name="actualQty[]" class="form-control form-control-sm text-center mx-auto actual-input actual-qty-input" '
            + '           value="' + p.systemStock + '" required style="width: 100px; border-radius: 6px;">'
            + '</td>'
            + '<td class="text-center discrepancy-cell fw-bold text-success">'
            + '    0'
            + '</td>'
            + '<td>'
            + '    <input type="text" name="note[]" class="form-control form-control-sm note-input" placeholder="Lý do lệch..." style="border-radius: 6px;">'
            + '</td>'
            + '<td class="text-center">'
            + '    <button type="button" class="btn btn-sm btn-link text-danger remove-btn" pId="' + p.productId + '" style="padding: 0;">'
            + '        <span class="material-icons">delete</span>'
            + '    </button>'
            + '</td>';

        const actualInput = row.querySelector('.actual-input');
        const discrepancyCell = row.querySelector('.discrepancy-cell');
        
        actualInput.addEventListener('input', function() {
            const rawVal = this.value;
            const trimmed = rawVal.trim();
            const isValid = /^\d+$/.test(trimmed);
            
            // Remove existing error message
            const existingError = row.querySelector('.qty-error-msg');
            if (existingError) {
                existingError.remove();
            }
            actualInput.classList.remove('border-danger', 'text-danger');
            row.classList.remove('table-danger');

            if (!isValid) {
                // Mark invalid
                row.classList.add('table-danger');
                actualInput.classList.add('border-danger', 'text-danger');
                discrepancyCell.textContent = 'Lỗi';
                discrepancyCell.className = "text-center discrepancy-cell fw-bold text-danger";
                
                // Add error message label
                const errMsg = document.createElement('div');
                errMsg.className = 'text-danger small mt-1 qty-error-msg';
                errMsg.style.fontSize = '11px';
                errMsg.style.fontWeight = '500';
                
                if (/[a-zA-Z]/.test(rawVal) && /\d/.test(rawVal)) {
                    errMsg.textContent = '⚠️ Không nhập chữ và số';
                } else if (/[a-zA-Z]/.test(rawVal)) {
                    errMsg.textContent = '⚠️ Không nhập chữ cái';
                } else if (/\s/.test(trimmed)) {
                    errMsg.textContent = '⚠️ Không có khoảng trắng';
                } else if (trimmed === '') {
                    errMsg.textContent = '⚠️ Không được để trống';
                } else {
                    errMsg.textContent = '⚠️ Số lượng không hợp lệ';
                }
                actualInput.parentNode.appendChild(errMsg);
            } else {
                const actVal = parseInt(trimmed, 10);
                const sysVal = p.systemStock;
                const diff = actVal - sysVal;
                discrepancyCell.textContent = diff > 0 ? '+' + diff : diff;
                
                if (diff === 0) {
                    discrepancyCell.className = "text-center discrepancy-cell fw-bold text-success";
                } else {
                    discrepancyCell.className = "text-center discrepancy-cell fw-bold text-danger";
                }
            }
            
            validateFormState();
        });

        row.querySelector('.remove-btn').addEventListener('click', function() {
            row.remove();
            selectedProductIds.delete(p.productId);
            validateFormState();
        });

        tableBody.appendChild(row);
        validateFormState();
    }

    const addProductToTable = addProduct;

    function validateFormState() {
        const hasErrors = tableBody.querySelectorAll('.table-danger').length > 0;
        const isEmpty = selectedProductIds.size === 0;

        if (isEmpty) {
            if (emptyRow) emptyRow.style.display = 'table-row';
            submitBtn.disabled = true;
            submitBtn.title = '';
        } else if (hasErrors) {
            submitBtn.disabled = true;
            submitBtn.title = 'Vui lòng sửa các dòng bị báo lỗi đỏ trước khi lưu phiếu.';
        } else {
            submitBtn.disabled = false;
            submitBtn.title = '';
        }
    }

    document.getElementById('checkForm').addEventListener('submit', function(e) {
        // Check for any validation errors first
        const hasErrors = tableBody.querySelectorAll('.table-danger').length > 0;
        if (hasErrors) {
            e.preventDefault();
            Swal.fire({
                icon: 'error',
                title: 'Dữ liệu không hợp lệ',
                text: 'Vui lòng sửa hoặc xóa các sản phẩm bị báo đỏ trước khi lưu phiếu.',
                confirmButtonColor: '#1e293b'
            });
            return;
        }

        let hasDiscrepancy = false;
        const discrepancyCells = document.querySelectorAll('.discrepancy-cell');
        discrepancyCells.forEach(cell => {
            const val = parseInt(cell.textContent.trim()) || 0;
            if (val !== 0) {
                hasDiscrepancy = true;
            }
        });

        if (!hasDiscrepancy) {
            e.preventDefault();
            Swal.fire({
                icon: 'warning',
                title: 'Không thể tạo phiếu',
                text: 'Không thể tạo phiếu kiểm kho khi số lượng thực tế trùng khớp hoàn toàn với hệ thống (không có chênh lệch tồn kho).',
                confirmButtonColor: '#1e293b'
            });
        }
    });

    // Pre-populate if editing
    <c:if test="${not empty checkDetails}">
        <c:forEach var="item" items="${checkDetails}">
            addProductToTable({
                productId: ${item.productId},
                productName: '<c:out value="${item.productName}"/>',
                categoryName: '<c:out value="${item.categoryName}"/>',
                systemStock: ${item.systemQty}
            });
            (function() {
                const row = document.querySelector('tr[data-id="${item.productId}"]');
                if (row) {
                    row.querySelector('.actual-qty-input').value = ${item.actualQty};
                    row.querySelector('.note-input').value = '<c:out value="${item.note}"/>';
                    row.querySelector('.actual-qty-input').dispatchEvent(new Event('input'));
                }
            })();
        </c:forEach>
    </c:if>

function exportCheckTemplate() {
    const warehouseId = '${selectedWarehouseId}';
    const url = '${pageContext.request.contextPath}/inventory?action=searchStockCheckProductsApi&keyword=&warehouseId=' + warehouseId;
    
    fetch(url)
        .then(response => response.json())
        .then(products => {
            if (!products || products.length === 0) {
                Toast.fire({ icon: 'error', title: 'Không có sản phẩm nào trong kho để tải mẫu.' });
                return;
            }
            
            const warehouseName = (document.querySelector('h4.mb-0')?.innerText || 'Cửa hàng Finora').trim();
            
            // Build data array of arrays
            const dataData = [
                ["MẪU PHIẾU NHẬP KIỂM KHO - " + warehouseName.toUpperCase()],
                ["Ngày tạo mẫu: " + new Date().toLocaleDateString('vi-VN') + " | Vui lòng điền số lượng đếm thực tế vào cột \"Tồn Thực Tế\""],
                ["STT", "Tên Sản Phẩm", "Mã SKU", "Danh Mục", "Tồn Hệ Thống", "Tồn Thực Tế", "Ghi Chú"]
            ];
            
            products.forEach((p, idx) => {
                dataData.push([
                    idx + 1,
                    p.productName,
                    p.productCodebar || '',
                    p.categoryName || '-',
                    p.systemStock,
                    '', // Empty for user input
                    ''  // Empty for notes
                ]);
            });
            
            // Create worksheet and workbook
            const worksheet = XLSX.utils.aoa_to_sheet(dataData);
            
            // Auto size column widths
            worksheet['!cols'] = [
                { wch: 6 },  // STT
                { wch: 35 }, // Tên Sản Phẩm
                { wch: 15 }, // Mã SKU
                { wch: 20 }, // Danh Mục
                { wch: 15 }, // Tồn Hệ Thống
                { wch: 15 }, // Tồn Thực Tế
                { wch: 25 }  // Ghi Chú
            ];
            
            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, "Kiem_Kho");
            
            // Generate binary XLSX
            const xlsxBin = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
            
            // Create blob and trigger download
            const blob = new Blob([xlsxBin], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
            const link = document.createElement("a");
            link.href = URL.createObjectURL(blob);
            link.download = "Mau_Kiem_Kho_" + warehouseName.replace(/\s+/g, '_') + "_" + new Date().toISOString().slice(0,10) + ".xlsx";
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        })
        .catch(err => {
            console.error(err);
            Toast.fire({ icon: 'error', title: 'Lỗi khi tải dữ liệu mẫu kiểm kho.' });
        });
}

function triggerExcelImport() {
    document.getElementById('excelImportInput').click();
}

function importCheckExcel(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    // Validate file type (Excel only)
    const allowedExtensions = /(\.xlsx|\.xls)$/i;
    if (!allowedExtensions.exec(file.name)) {
        Swal.fire({
            icon: 'error',
            title: 'Định dạng file không hợp lệ',
            text: 'Vui lòng chọn đúng định dạng file Excel (.xlsx hoặc .xls).',
            confirmButtonColor: '#1e293b'
        });
        event.target.value = '';
        return;
    }
    
    // Validate file size (under 5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB in bytes
    if (file.size > maxSize) {
        Swal.fire({
            icon: 'error',
            title: 'Kích thước file quá lớn',
            text: 'Dung lượng tệp tải lên không được vượt quá 5MB. Vui lòng chọn file nhỏ hơn.',
            confirmButtonColor: '#1e293b'
        });
        event.target.value = '';
        return;
    }
    
    // Check if the user is uploading the old .xls template
    if (file.name.toLowerCase().endsWith('.xls')) {
        Swal.fire({
            icon: 'warning',
            title: 'Tệp định dạng cũ (.xls)',
            text: 'Bạn đang tải lên tệp định dạng cũ (.xls). Do Excel lưu định dạng cũ thành nhiều thư mục liên kết nên không thể đọc trực tiếp. Vui lòng nhấp vào thẻ "1. Lấy Tồn Kho Hiện Tại (Tải Excel)" để tải mẫu mới (.xlsx) chuẩn và nhập lại.',
            confirmButtonColor: '#1e293b'
        });
        event.target.value = '';
        return;
    }
    
    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const data = new Uint8Array(e.target.result);
            const workbook = XLSX.read(data, { type: 'array' });
            
            const sheetName = workbook.SheetNames[0];
            const worksheet = workbook.Sheets[sheetName];
            
            // Convert to JSON array of arrays
            const rows = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
            console.log("Parsed Excel rows:", rows);
            
            parseCheckRows(rows);
        } catch (err) {
            console.error(err);
            Toast.fire({ icon: 'error', title: 'Lỗi khi đọc file Excel. Vui lòng đảm bảo tệp đúng định dạng.' });
        }
    };
    reader.readAsArrayBuffer(file);
    event.target.value = '';
}

function parseCheckRows(rows) {
    if (!rows || rows.length === 0) {
        Toast.fire({ icon: 'error', title: 'Tệp Excel không chứa dữ liệu hoặc sai định dạng.' });
        return;
    }
    
    // Find header row (usually the first row that contains "STT", "Tên Sản Phẩm", "Mã SKU", "Danh Mục")
    let headerRowIdx = -1;
    for (let i = 0; i < Math.min(10, rows.length); i++) {
        const row = rows[i];
        if (!row) continue;
        const rowStr = row.map(cell => String(cell || '').toLowerCase()).join(' ');
        if (rowStr.indexOf('sản phẩm') > -1 || rowStr.indexOf('sku') > -1 || rowStr.indexOf('tồn hệ thống') > -1) {
            headerRowIdx = i;
            break;
        }
    }
    
    if (headerRowIdx === -1) {
        Toast.fire({
            icon: 'error',
            title: 'Không tìm thấy dòng tiêu đề cột hợp lệ trong file Excel. Yêu cầu có các cột: "Tên Sản Phẩm", "Mã SKU", "Tồn Thực Tế".'
        });
        return;
    }
    
    const headers = rows[headerRowIdx];
    let skuIdx = -1;
    let nameIdx = -1;
    let actualQtyIdx = -1;
    let noteIdx = -1;
    
    for (let j = 0; j < headers.length; j++) {
        const headerVal = String(headers[j] || '').toLowerCase().trim();
        if (headerVal.indexOf('sku') > -1 || headerVal.indexOf('mã') > -1) skuIdx = j;
        if (headerVal.indexOf('sản phẩm') > -1 || headerVal.indexOf('tên') > -1) nameIdx = j;
        if (headerVal.indexOf('thực tế') > -1 || headerVal.indexOf('thực') > -1) actualQtyIdx = j;
        if (headerVal.indexOf('ghi chú') > -1 || headerVal.indexOf('lý do') > -1 || headerVal.indexOf('chú') > -1) noteIdx = j;
    }
    
    if (nameIdx === -1 || actualQtyIdx === -1) {
        Toast.fire({
            icon: 'error',
            title: 'Không tìm thấy các cột dữ liệu bắt buộc (Tên Sản Phẩm, Tồn Thực Tế) trong tệp Excel.'
        });
        return;
    }
    
    const warehouseId = '${selectedWarehouseId}';
    const url = '${pageContext.request.contextPath}/inventory?action=searchStockCheckProductsApi&keyword=&warehouseId=' + warehouseId;
    
    fetch(url)
        .then(response => response.json())
        .then(products => {
            let count = 0;
            
            // Clear existing table body before importing
            tableBody.innerHTML = '';
            selectedProductIds.clear();
            
            for (let i = headerRowIdx + 1; i < rows.length; i++) {
                const rowData = rows[i];
                if (!rowData || rowData.length === 0) continue;
                
                const sku = skuIdx > -1 && rowData[skuIdx] !== undefined ? String(rowData[skuIdx]).trim() : '';
                const productName = nameIdx > -1 && rowData[nameIdx] !== undefined ? String(rowData[nameIdx]).trim() : '';
                const actualQtyStr = actualQtyIdx > -1 && rowData[actualQtyIdx] !== undefined ? String(rowData[actualQtyIdx]).trim() : '';
                const note = noteIdx > -1 && rowData[noteIdx] !== undefined ? String(rowData[noteIdx]).trim() : '';
                
                if (actualQtyStr !== '' && productName !== '') {
                    const p = products.find(prod => (sku && prod.productCodebar === sku) || prod.productName === productName);
                    if (p) {
                        addProductToTable({
                            productId: p.productId,
                            productName: p.productName,
                            categoryName: p.categoryName || '-',
                            systemStock: p.systemStock
                        });
                        
                        const row = document.querySelector('tr[data-id="' + p.productId + '"]');
                        if (row) {
                            row.querySelector('.actual-qty-input').value = actualQtyStr;
                            row.querySelector('.note-input').value = note;
                            row.querySelector('.actual-qty-input').dispatchEvent(new Event('input'));
                            count++;
                        }
                    }
                }
            }
            
            if (count > 0) {
                Toast.fire({
                    icon: 'success',
                    title: 'Đã nhập thành công số lượng kiểm kê của ' + count + ' sản phẩm!'
                });
            } else {
                Toast.fire({
                    icon: 'warning',
                    title: 'Không khớp được sản phẩm nào từ file Excel. Vui lòng kiểm tra lại tên sản phẩm hoặc mã SKU.'
                });
            }
        })
        .catch(err => {
            console.error(err);
            Toast.fire({ icon: 'error', title: 'Lỗi khi khớp dữ liệu sản phẩm: ' + err.message });
        });
}
</script>

/**
 * Inventory Tabs Script
 * Handles interactivity for all sub-tabs: Stock, Transfers, Stocktake, History.
 */

function initTabs() {
    initHistoryTab();
    initTransferCreateTab();
    initCheckCreateTab();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTabs);
} else {
    initTabs();
}

/* ==========================================================================
   COMMON UTILS & CONFIG
   ========================================================================== */
window.getContextPath = window.getContextPath || (() => window.INVENTORY_CONFIG?.contextPath || '');
window.getSelectedWarehouseId = window.getSelectedWarehouseId || (() => window.INVENTORY_CONFIG?.selectedWarehouseId || '');
window.getCsrfToken = window.getCsrfToken || (() => window.INVENTORY_CONFIG?.csrfToken || '');

/* ==========================================================================
   TAB: STOCK
   ========================================================================== */

/**
 * Handle warehouse selection
 */
function selectWarehouse(id) {
    const input = document.getElementById('warehouseIdInput');
    if (input) {
        input.value = id;
        input.form.submit();
    }
}

/**
 * Export current stock table to Excel (.xls) format
 */
function exportStockExcel() {
    const table = document.querySelector('.premium-table');
    if (!table) return;
    
    const rows = table.querySelectorAll('tbody tr');
    if (rows.length === 0 || (rows.length === 1 && rows[0].querySelector('td').colSpan)) {
        alert('Không có dữ liệu tồn kho để xuất.');
        return;
    }
    
    const warehouseName = (document.querySelector('h4.mb-0')?.innerText || 'Cửa hàng Finora').trim();
    
    const htmlAttr = 'xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"';
    let excelHtml = '<html ' + htmlAttr + '><head><meta charset="utf-8" />'
        + '<style>'
        + 'table { border-collapse: collapse; width: 100%; font-family: "Segoe UI", -apple-system, BlinkMacSystemFont, sans-serif; }'
        + 'th { background-color: #1e293b; color: #ffffff; font-weight: bold; border: 1px solid #cbd5e1; padding: 12px 10px; font-size: 11pt; text-align: left; }'
        + 'td { border: 1px solid #cbd5e1; padding: 10px; font-size: 10pt; color: #334155; }'
        + '.text-center { text-align: center; }'
        + '.text-right { text-align: right; }'
        + 'tr:nth-child(even) { background-color: #f8fafc; }'
        + '.title-header { font-size: 16pt; font-weight: bold; color: #0f172a; margin-bottom: 5px; }'
        + '.meta-info { font-size: 10pt; color: #64748b; margin-bottom: 20px; }'
        + '</style>'
        + '</head><body>'
        + '<div class="title-header">BÁO CÁO TỒN KHO - ' + warehouseName.toUpperCase() + '</div>'
        + '<div class="meta-info">Ngày xuất file: ' + new Date().toLocaleDateString('vi-VN') + ' | Tổng cộng: ' + rows.length + ' mặt hàng</div>'
        + '<table>'
        + '<colgroup>'
        + '  <col width="60" />'
        + '  <col width="300" />'
        + '  <col width="150" />'
        + '  <col width="180" />'
        + '  <col width="120" />'
        + '</colgroup>'
        + '<thead><tr>'
        + '  <th class="text-center">STT</th>'
        + '  <th>Tên Sản Phẩm</th>'
        + '  <th>Mã SKU</th>'
        + '  <th>Danh Mục</th>'
        + '  <th class="text-right">Số Lượng Tồn</th>'
        + '</tr></thead>'
        + '<tbody>';
    
    let stt = 1;
    rows.forEach(tr => {
        const cols = tr.querySelectorAll('td');
        if (cols.length < 6) return;
        
        const productName = cols[0].querySelector('h6') ? cols[0].querySelector('h6').innerText.trim() : cols[0].innerText.trim();
        const sku = cols[1].innerText.trim();
        const category = cols[2].innerText.trim();
        const systemStock = cols[5].querySelector('span') ? cols[5].querySelector('span').innerText.trim() : cols[5].innerText.trim();
        
        excelHtml += '<tr>'
                   + '<td class="text-center">' + stt + '</td>'
                   + '<td>' + productName + '</td>'
                   + '<td>' + sku + '</td>'
                   + '<td>' + category + '</td>'
                   + '<td class="text-right">' + systemStock + '</td>'
                   + '</tr>';
        stt++;
    });
    
    excelHtml += '</tbody></table></body></html>';
    
    const blob = new Blob([excelHtml], { type: 'application/vnd.ms-excel;charset=utf-8;' });
    const link = document.createElement("a");
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", "Bao_Cao_Ton_Kho_" + warehouseName.replace(/\s+/g, '_') + "_" + new Date().toISOString().slice(0,10) + ".xls");
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * Edit stock directly (for Owners)
 */
function editStockDirectly(productId, warehouseId, currentQty, productName) {
    const newQtyStr = prompt("Nhập số lượng tồn kho mới cho sản phẩm '" + productName + "':", currentQty);
    if (newQtyStr === null) return;
    
    const newQty = parseInt(newQtyStr.trim());
    if (isNaN(newQty) || newQty < 0) {
        alert("Số lượng tồn kho phải là số nguyên lớn hơn hoặc bằng 0.");
        return;
    }
    
    if (newQty === currentQty) return;
    
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = getContextPath() + '/inventory';
    
    const actionInput = document.createElement('input');
    actionInput.type = 'hidden';
    actionInput.name = 'action';
    actionInput.value = 'updateStockDirectly';
    
    const productIdInput = document.createElement('input');
    productIdInput.type = 'hidden';
    productIdInput.name = 'productId';
    productIdInput.value = productId;
    
    const warehouseIdInput = document.createElement('input');
    warehouseIdInput.type = 'hidden';
    warehouseIdInput.name = 'warehouseId';
    warehouseIdInput.value = warehouseId;
    
    const quantityInput = document.createElement('input');
    quantityInput.type = 'hidden';
    quantityInput.name = 'quantity';
    quantityInput.value = newQty;
    
    const csrfInput = document.createElement('input');
    csrfInput.type = 'hidden';
    csrfInput.name = 'csrfToken';
    csrfInput.value = getCsrfToken();
    
    form.appendChild(actionInput);
    form.appendChild(productIdInput);
    form.appendChild(warehouseIdInput);
    form.appendChild(quantityInput);
    form.appendChild(csrfInput);
    
    document.body.appendChild(form);
    form.submit();
}

/* ==========================================================================
   TAB: TRANSFER CREATE
   ========================================================================== */
function initTransferCreateTab() {
    const searchInput = document.getElementById('productSearch');
    const searchResults = document.getElementById('searchResults');
    const transferForm = document.getElementById('transferForm');
    
    if (!searchInput || !searchResults) return;

    let searchTimeout;

    // Show suggestion automatically when focus
    searchInput.addEventListener('focus', function() {
        if (!this.value.trim()) {
            fetchResults('');
        }
    });

    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        const keyword = this.value.trim();
        searchTimeout = setTimeout(() => {
            fetchResults(keyword);
        }, 300);
    });

    // Hide search results when clicking outside
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
            searchResults.style.display = 'none';
        }
    });

    function fetchResults(keyword) {
        const url = getContextPath() + '/inventory?action=searchAllProductsApi&keyword=' + encodeURIComponent(keyword) + '&warehouseId=' + getSelectedWarehouseId();
        fetch(url)
            .then(res => res.json())
            .then(data => {
                searchResults.innerHTML = '';
                if (data.length === 0) {
                    searchResults.innerHTML = '<div class="p-3 text-center text-muted">Không tìm thấy sản phẩm nào</div>';
                } else {
                    if (keyword === '') {
                        const header = document.createElement('div');
                        header.className = 'suggestion-header';
                        header.innerHTML = '<span class="material-icons" style="font-size:18px;">warning</span> GỢI Ý HÀNG SẮP HẾT (CÓ THỂ NHẬP TỪ KHO KHÁC)';
                        searchResults.appendChild(header);
                    } else {
                        const header = document.createElement('div');
                        header.className = 'suggestion-header normal-header';
                        header.innerHTML = '<span class="material-icons" style="font-size:18px;">search</span> KẾT QUẢ TÌM KIẾM';
                        searchResults.appendChild(header);
                    }
                    
                    data.forEach(p => {
                        const item = document.createElement('div');
                        item.className = 'search-item';
                        item.style.cursor = 'default';
                        
                        const isLowStock = p.myStock <= 10;
                        const directionType = isLowStock ? 'RECEIVE' : 'SEND';
                        
                        let selectHtml = '<select class="partner-choice-select form-select form-select-sm" style="width: 320px;">';
                        p.partners.forEach(partner => {
                            selectHtml += '<option value="' + partner.warehouseId + '">' + partner.warehouseName + ' (Tồn: ' + partner.stock + ')</option>';
                        });
                        selectHtml += '</select>';

                        const stockColor = p.myStock > 0 ? 'text-success' : 'text-danger';
                        
                        item.innerHTML = 
                            '<div class="flex-grow-1" style="min-width: 0; padding-right: 15px;">' +
                                '<div class="fw-bold text-dark" style="font-size:15px; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="' + p.productName + '">' +
                                    p.productName +
                                '</div>' +
                                '<div class="small text-muted" style="font-size:13px;">' +
                                    'Tồn của bạn: <span class="fw-bold ' + stockColor + '">' + p.myStock + '</span>' +
                                '</div>' +
                             '</div>' +
                             '<div class="d-flex align-items-center gap-3 ms-auto" style="flex-shrink: 0;">' +
                                 selectHtml +
                                 '<span class="material-icons add-icon-btn" title="Thêm vào danh sách">add_circle</span>' +
                             '</div>';
                        
                        const addBtn = item.querySelector('.add-icon-btn');
                        const partnerSelect = item.querySelector('.partner-choice-select');
                        addBtn.onclick = () => {
                            addRow({
                                productId: p.productId,
                                productName: p.productName,
                                myStock: p.myStock,
                                partnerWarehouseId: partnerSelect.value,
                                direction: directionType,
                                partners: p.partners
                            });
                        };
                        searchResults.appendChild(item);
                    });
                }
                searchResults.style.display = 'block';
            });
    }

    if (transferForm) {
        transferForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            let hasWarning = false;
            this.querySelectorAll('.qty-input').forEach(input => {
                const qty = parseInt(input.value) || 0;
                const limit = parseInt(input.getAttribute('data-limit')) || 0;
                if (qty > limit) {
                    hasWarning = true;
                }
            });
            
            if (hasWarning) {
                if (confirm("Cảnh báo: Có sản phẩm vượt quá tồn kho thực tế của kho nguồn. Bạn có chắc chắn muốn tạo phiếu điều chuyển này?")) {
                    this.submit();
                }
            } else {
                this.submit();
            }
        });
    }
}

function addRow(product) {
    const tableBody = document.getElementById('transferTableBody');
    const emptyRow = document.getElementById('emptyRow');
    const submitBtn = document.getElementById('submitBtn');
    if (!tableBody) return;

    let isDuplicate = false;
    tableBody.querySelectorAll('tr').forEach(tr => {
        if (tr.id === 'emptyRow') return;
        const pidInput = tr.querySelector('input[name="productId[]"]');
        if (pidInput && pidInput.value == product.productId) {
            isDuplicate = true;
        }
    });

    if (isDuplicate) {
        alert('Sản phẩm này đã được chọn!');
        const searchResults = document.getElementById('searchResults');
        const searchInput = document.getElementById('productSearch');
        if (searchResults) searchResults.style.display = 'none';
        if (searchInput) searchInput.value = '';
        return;
    }

    if (emptyRow) emptyRow.style.display = 'none';
    if (submitBtn) submitBtn.disabled = false;
    
    const searchResults = document.getElementById('searchResults');
    const searchInput = document.getElementById('productSearch');
    if (searchResults) searchResults.style.display = 'none';
    if (searchInput) searchInput.value = '';

    const stockColor = product.myStock > 0 ? 'text-success' : 'text-danger';

    let partnerOptionsHtml = '';
    product.partners.forEach(partner => {
        partnerOptionsHtml += '<option value="' + partner.warehouseId + '">' + partner.warehouseName + ' (Tồn: ' + partner.stock + ')</option>';
    });

    const tr = document.createElement('tr');
    tr.innerHTML = 
        '<td style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 0;" title="' + product.productName + '">' +
            '<div class="fw-bold text-dark" style="font-size:14.5px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">' + product.productName + '</div>' +
            '<input type="hidden" name="productId[]" value="' + product.productId + '">' +
        '</td>' +
        '<td>' +
            '<span class="fw-bold ' + stockColor + '" style="font-size:14.5px;">' + product.myStock + '</span>' +
        '</td>' +
        '<td>' +
            '<select name="partnerWarehouseId[]" class="form-select form-select-sm partner-warehouse-select" style="border-radius: 8px; font-size:13.5px; height: 38px; border: 1px solid #cbd5e1; width: 100%;" required>' +
                partnerOptionsHtml +
            '</select>' +
        '</td>' +
        '<td>' +
            '<select name="actionType[]" class="form-select form-select-sm action-type-select" style="border-radius: 8px; font-size:13.5px; height: 38px; border: 1px solid #cbd5e1; width: 100%;" required>' +
                '<option value="SEND">Xuất</option>' +
                '<option value="RECEIVE">Nhập</option>' +
            '</select>' +
        '</td>' +
        '<td>' +
            '<div class="position-relative" style="width: 100%;">' +
                '<input type="number" name="quantity[]" class="form-control qty-input form-control-sm" required min="1" placeholder="Số lượng" style="border-radius: 8px; height: 38px; font-size:13.5px; border: 1px solid #cbd5e1; width: 100%;" data-limit="0">' +
            '</div>' +
        '</td>' +
        '<td class="text-center">' +
            '<button type="button" class="btn btn-link text-danger p-0" onclick="this.closest(\'tr\').remove(); checkEmpty();" style="text-decoration: none; display:inline-flex; align-items:center; justify-content:center; height:38px; width:38px;">' +
                '<span class="material-icons" style="font-size: 22px;">delete</span>' +
            '</button>' +
        '</td>';
    tableBody.appendChild(tr);

    const partnerSelect = tr.querySelector('.partner-warehouse-select');
    const actionSelect = tr.querySelector('.action-type-select');
    const qtyInput = tr.querySelector('.qty-input');

    if (product.partnerWarehouseId) {
        partnerSelect.value = product.partnerWarehouseId;
    }
    if (product.direction) {
        actionSelect.value = product.direction;
    }

    const updateStock = () => {
        const partnerWId = partnerSelect.value;
        const actionType = actionSelect.value;
        const targetWId = actionType === 'SEND' ? getSelectedWarehouseId() : partnerWId;
        qtyInput.setAttribute('data-limit', '0');

        if (!targetWId) return;

        const stockUrl = getContextPath() + '/inventory?action=getProductStockApi&productId=' + product.productId + '&warehouseId=' + targetWId;
        fetch(stockUrl)
            .then(res => res.json())
            .then(res => {
                qtyInput.setAttribute('data-limit', res.stock);
                validateInputWarning(qtyInput);
            });
    };

    partnerSelect.addEventListener('change', updateStock);
    actionSelect.addEventListener('change', updateStock);
    qtyInput.addEventListener('input', function() {
        validateInputWarning(this);
    });

    // Trigger initial load
    updateStock();
}

function checkEmpty() {
    const tableBody = document.getElementById('transferTableBody');
    const emptyRow = document.getElementById('emptyRow');
    const submitBtn = document.getElementById('submitBtn');
    if (!tableBody) return;

    if (tableBody.querySelectorAll('tr').length === 1 && tableBody.querySelector('#emptyRow')) {
        if (emptyRow) emptyRow.style.display = 'table-row';
        if (submitBtn) submitBtn.disabled = true;
    } else if (tableBody.querySelectorAll('tr').length === 0) {
        if (emptyRow) emptyRow.style.display = 'table-row';
        if (submitBtn) submitBtn.disabled = true;
    }
}

function validateInputWarning(input) {
    const tr = input.closest('tr');
    if (!tr || tr.id === 'emptyRow') return;
    const qty = parseInt(input.value) || 0;
    const limit = parseInt(input.getAttribute('data-limit')) || 0;
    
    let warningEl = tr.querySelector('.qty-warning');
    if (!warningEl) {
        warningEl = document.createElement('div');
        warningEl.className = 'qty-warning text-danger small mt-1';
        warningEl.style.fontSize = '11px';
        input.parentNode.appendChild(warningEl);
    }
    
    if (qty > limit) {
        input.style.borderColor = '#fd7e14';
        input.style.boxShadow = '0 0 0 0.2rem rgba(253, 126, 20, 0.25)';
        warningEl.innerHTML = '<span class="material-icons" style="font-size:12px;vertical-align:text-bottom;color:#fd7e14;">warning</span> Vượt tồn nguồn (' + limit + ')';
        warningEl.style.color = '#fd7e14';
    } else {
        input.style.borderColor = '';
        input.style.boxShadow = '';
        warningEl.innerHTML = "";
    }
}

/* ==========================================================================
   TAB: CHECK (STOCKTAKE) CREATE
   ========================================================================== */
const selectedProductIds = new Set();

function initCheckCreateTab() {
    const checkForm = document.getElementById('checkForm');
    const tableBody = document.getElementById('checkTableBody');
    
    if (!checkForm) return;

    checkForm.addEventListener('submit', function(e) {
        const hasErrors = tableBody?.querySelectorAll('.table-danger').length > 0;
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
    });

    // Populate editing check details if defined in window
    if (window.CHECK_DETAILS_DATA && window.CHECK_DETAILS_DATA.length > 0) {
        window.CHECK_DETAILS_DATA.forEach(item => {
            addProductToTable(item);
            const row = document.querySelector(`tr[data-id="${item.productId}"]`);
            if (row) {
                const actualInput = row.querySelector('.actual-qty-input');
                const noteInput = row.querySelector('.note-input');
                if (actualInput) {
                    actualInput.value = item.actualQty;
                }
                if (noteInput) {
                    noteInput.value = item.note;
                }
                if (actualInput) {
                    actualInput.dispatchEvent(new Event('input'));
                }
            }
        });
    }
}

function addProductToTable(p) {
    const tableBody = document.getElementById('checkTableBody');
    const emptyRow = document.getElementById('emptyRow');
    if (!tableBody) return;

    if (selectedProductIds.has(p.productId)) {
        if (window.Toast) {
            window.Toast.fire({
                icon: 'warning',
                title: 'Sản phẩm này đã có trong danh sách kiểm kê!'
            });
        } else {
            alert('Sản phẩm này đã có trong danh sách kiểm kê!');
        }
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
        
        const existingError = row.querySelector('.qty-error-msg');
        if (existingError) {
            existingError.remove();
        }
        actualInput.classList.remove('border-danger', 'text-danger');
        row.classList.remove('table-danger');

        if (!isValid) {
            row.classList.add('table-danger');
            actualInput.classList.add('border-danger', 'text-danger');
            discrepancyCell.textContent = 'Lỗi';
            discrepancyCell.className = "text-center discrepancy-cell fw-bold text-danger";
            
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

function validateFormState() {
    const tableBody = document.getElementById('checkTableBody');
    const emptyRow = document.getElementById('emptyRow');
    const submitBtn = document.getElementById('submitBtn');
    if (!tableBody || !submitBtn) return;

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

/**
 * Export Excel template for stock checking
 */
function exportCheckTemplate() {
    const warehouseId = getSelectedWarehouseId();
    const url = getContextPath() + '/inventory?action=searchStockCheckProductsApi&keyword=&warehouseId=' + warehouseId;
    
    fetch(url)
        .then(response => response.json())
        .then(products => {
            if (!products || products.length === 0) {
                if (window.Toast) {
                    window.Toast.fire({ icon: 'error', title: 'Không có sản phẩm nào trong kho để tải mẫu.' });
                } else {
                    alert('Không có sản phẩm nào trong kho để tải mẫu.');
                }
                return;
            }
            
            const warehouseName = (document.querySelector('h4.mb-0')?.innerText || 'Cửa hàng Finora').trim();
            
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
                    '', 
                    ''  
                ]);
            });
            
            const worksheet = XLSX.utils.aoa_to_sheet(dataData);
            
            worksheet['!cols'] = [
                { wch: 6 },  
                { wch: 35 }, 
                { wch: 15 }, 
                { wch: 20 }, 
                { wch: 15 }, 
                { wch: 15 }, 
                { wch: 25 }  
            ];
            
            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, "Kiem_Kho");
            
            const xlsxBin = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
            
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
            if (window.Toast) {
                window.Toast.fire({ icon: 'error', title: 'Lỗi khi tải dữ liệu mẫu kiểm kho.' });
            } else {
                alert('Lỗi khi tải dữ liệu mẫu kiểm kho.');
            }
        });
}

function triggerExcelImport() {
    const input = document.getElementById('excelImportInput');
    if (input) input.click();
}

function importCheckExcel(event) {
    const file = event.target.files[0];
    if (!file) return;
    
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
    
    const maxSize = 5 * 1024 * 1024; // 5MB
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
            const rows = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
            
            parseCheckRows(rows);
        } catch (err) {
            console.error(err);
            if (window.Toast) {
                window.Toast.fire({ icon: 'error', title: 'Lỗi khi đọc file Excel. Vui lòng đảm bảo tệp đúng định dạng.' });
            } else {
                alert('Lỗi khi đọc file Excel. Vui lòng đảm bảo tệp đúng định dạng.');
            }
        }
    };
    reader.readAsArrayBuffer(file);
    event.target.value = '';
}

function parseCheckRows(rows) {
    if (!rows || rows.length === 0) {
        if (window.Toast) {
            window.Toast.fire({ icon: 'error', title: 'Tệp Excel không chứa dữ liệu hoặc sai định dạng.' });
        }
        return;
    }
    
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
        if (window.Toast) {
            window.Toast.fire({
                icon: 'error',
                title: 'Không tìm thấy dòng tiêu đề cột hợp lệ trong file Excel. Yêu cầu có các cột: "Tên Sản Phẩm", "Mã SKU", "Tồn Thực Tế".'
            });
        }
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
        if (window.Toast) {
            window.Toast.fire({
                icon: 'error',
                title: 'Không tìm thấy các cột dữ liệu bắt buộc (Tên Sản Phẩm, Tồn Thực Tế) trong tệp Excel.'
            });
        }
        return;
    }

    // Check for blank rows between data rows
    let firstDataRowIdx = -1;
    let lastDataRowIdx = -1;
    for (let i = headerRowIdx + 1; i < rows.length; i++) {
        const r = rows[i];
        if (r && !r.every(cell => cell === null || cell === undefined || String(cell).trim() === '')) {
            if (firstDataRowIdx === -1) firstDataRowIdx = i;
            lastDataRowIdx = i;
        }
    }

    if (firstDataRowIdx !== -1 && lastDataRowIdx !== -1) {
        for (let i = firstDataRowIdx; i <= lastDataRowIdx; i++) {
            const r = rows[i];
            const isRowEmpty = !r || r.every(cell => cell === null || cell === undefined || String(cell).trim() === '');
            if (isRowEmpty) {
                const excelRowNumber = i + 1;
                if (typeof Swal !== 'undefined') {
                    Swal.fire({
                        icon: 'error',
                        title: 'Tệp Excel chứa dòng trống ở giữa',
                        text: 'Dòng ' + excelRowNumber + ' trong tệp Excel đang để trống. Vui lòng xóa các dòng trống đan xen trước khi tải lên.',
                        confirmButtonColor: '#1e293b'
                    });
                } else if (window.Toast) {
                    window.Toast.fire({
                        icon: 'error',
                        title: 'Tệp Excel chứa dòng trống ở vị trí dòng ' + excelRowNumber + '.'
                    });
                } else {
                    alert('Tệp Excel chứa dòng trống ở vị trí dòng ' + excelRowNumber + '. Vui lòng xóa các dòng trống đan xen trước khi tải lên.');
                }
                return;
            }
        }
    }
    
    const warehouseId = getSelectedWarehouseId();
    const url = getContextPath() + '/inventory?action=searchStockCheckProductsApi&keyword=&warehouseId=' + warehouseId;
    
    fetch(url)
        .then(response => response.json())
        .then(products => {
            let count = 0;
            const tableBody = document.getElementById('checkTableBody');
            if (tableBody) tableBody.innerHTML = '';
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
                if (window.Toast) {
                    window.Toast.fire({
                        icon: 'success',
                        title: 'Đã nhập thành công số lượng kiểm kê của ' + count + ' sản phẩm!'
                    });
                }
            } else {
                if (window.Toast) {
                    window.Toast.fire({
                        icon: 'warning',
                        title: 'Không khớp được sản phẩm nào từ file Excel. Vui lòng kiểm tra lại tên sản phẩm hoặc mã SKU.'
                    });
                }
            }
        })
        .catch(err => {
            console.error(err);
            if (window.Toast) {
                window.Toast.fire({ icon: 'error', title: 'Lỗi khi khớp dữ liệu sản phẩm: ' + err.message });
            }
        });
}

/* ==========================================================================
   TAB: HISTORY
   ========================================================================== */
function initHistoryTab() {
    const historyForm = document.getElementById('historyFilterForm');
    const fromDateEl = document.getElementById('fromDate');
    const toDateEl = document.getElementById('toDate');

    if (!historyForm || !fromDateEl || !toDateEl) return;

    historyForm.addEventListener('submit', function(e) {
        if (fromDateEl.value) {
            const fromYear = parseInt(fromDateEl.value.split('-')[0]);
            if (isNaN(fromYear) || fromYear < 1000 || fromYear > 9999) {
                e.preventDefault();
                alert('Lỗi validate: Năm của Từ ngày phải nằm trong khoảng từ 1000 đến 9999!');
                fromDateEl.style.borderColor = '#dc3545';
                return;
            }
        }
        
        if (toDateEl.value) {
            const toYear = parseInt(toDateEl.value.split('-')[0]);
            if (isNaN(toYear) || toYear < 1000 || toYear > 9999) {
                e.preventDefault();
                alert('Lỗi validate: Năm của Đến ngày phải nằm trong khoảng từ 1000 đến 9999!');
                toDateEl.style.borderColor = '#dc3545';
                return;
            }
        }
        
        if (fromDateEl.value && toDateEl.value) {
            if (fromDateEl.value > toDateEl.value) {
                e.preventDefault();
                alert('Lỗi validate: Ngày bắt đầu (Từ ngày) không được lớn hơn Ngày kết thúc (Đến ngày)!');
                fromDateEl.style.borderColor = '#dc3545';
                toDateEl.style.borderColor = '#dc3545';
            }
        }
    });
    
    fromDateEl.addEventListener('input', function() {
        this.style.borderColor = '';
        toDateEl.style.borderColor = '';
    });
    
    toDateEl.addEventListener('input', function() {
        this.style.borderColor = '';
        fromDateEl.style.borderColor = '';
    });
}

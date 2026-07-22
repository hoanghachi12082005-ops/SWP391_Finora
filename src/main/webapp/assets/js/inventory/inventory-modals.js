/**
 * Inventory Modals Script
 * Handles manual import, Excel import, and export modal logic.
 */

function initModals() {
    initImportStockModal();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initModals);
} else {
    initModals();
}

// Config helpers
window.getContextPath = window.getContextPath || (() => window.INVENTORY_CONFIG?.contextPath || '');
window.getSelectedWarehouseId = window.getSelectedWarehouseId || (() => window.INVENTORY_CONFIG?.selectedWarehouseId || '');

/* ==========================================================================
   MODAL: CREATE IMPORT
   ========================================================================== */
function initImportStockModal() {
    const iSearchInput = document.getElementById('importSearchInput');
    const iSearchResults = document.getElementById('importSearchResults');
    const iTableBody = document.getElementById('importProductTableBody');
    const iSubmitBtn = document.getElementById('importSubmitBtn');
    const importStockForm = document.getElementById('importStockForm');

    if (!iSearchInput || !iSearchResults || !iTableBody) return;

    let iSearchTimeout;
    let iSearchActive = false;

    // Trigger empty search on focus
    iSearchInput.addEventListener('focus', function() {
        iSearchActive = true;
        triggerSearch(this.value.trim());
    });

    iSearchInput.addEventListener('click', function() {
        iSearchActive = true;
        triggerSearch(this.value.trim());
    });

    iSearchInput.addEventListener('input', function() {
        iSearchActive = true;
        clearTimeout(iSearchTimeout);
        const keyword = this.value.trim();
        iSearchTimeout = setTimeout(() => {
            triggerSearch(keyword);
        }, 300);
    });

    // Hide search results when clicking outside
    document.addEventListener('click', function(e) {
        if (!iSearchInput.contains(e.target) && !iSearchResults.contains(e.target)) {
            iSearchActive = false;
            iSearchResults.style.display = 'none';
        }
    });

    function triggerSearch(keyword) {
        const url = getContextPath() + '/inventory?action=searchImportProductsApi&keyword=' + encodeURIComponent(keyword) + '&warehouseId=' + getSelectedWarehouseId();
        
        fetch(url)
            .then(res => res.json())
            .then(data => {
                iSearchResults.innerHTML = '';
                if(data.length === 0) {
                    iSearchResults.innerHTML = '<div class="p-3 text-center text-muted small">Không tìm thấy sản phẩm nào</div>';
                } else {
                    if (keyword === '') {
                        const header = document.createElement('div');
                        header.className = 'suggestion-header';
                        header.innerHTML = '<span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">warning</span> Đề xuất sản phẩm sắp hết cần nhập hàng';
                        iSearchResults.appendChild(header);
                    }
                    data.forEach(p => {
                        const item = document.createElement('div');
                        item.className = 'import-search-item';
                        item.style.cursor = 'default';
                        
                        const hasSuppliers = p.suppliers && p.suppliers.length > 0;
                        const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

                        if (hasSuppliers) {
                            let defaultPrice = p.suppliers[0].importPrice || 0;
                            let optionsHtml = '';
                            p.suppliers.forEach(sup => {
                                optionsHtml += `<option value="${sup.supplierId}" data-price="${sup.importPrice || 0}">${sup.supplierName} — ${formatCurrency(sup.importPrice || 0)}</option>`;
                            });

                            item.innerHTML = `
                                <div>
                                    <div class="fw-bold text-dark" style="font-size: 14.5px;">${p.productName}</div>
                                    <div class="d-flex align-items-center gap-2 mt-1" style="font-size: 12px;">
                                        <span class="badge bg-light text-secondary border">Mã: SP${p.productId}</span>
                                        <span class="text-muted">|</span>
                                        <span>Tồn kho: <strong class="${p.myStock > 0 ? 'text-success' : 'text-danger'}">${p.myStock} SP</strong></span>
                                    </div>
                                </div>
                                <div class="d-flex align-items-center gap-3">
                                    <div class="d-flex flex-column align-items-end">
                                        <span class="text-muted small" style="font-size: 11px; font-weight: 500;">Giá nhập đề xuất</span>
                                        <span class="text-success fw-bold i-price-display" style="font-size: 15px;">${formatCurrency(defaultPrice)}</span>
                                    </div>
                                    <div class="d-flex align-items-center gap-2">
                                        <select class="form-select form-select-sm i-supplier-select" style="width: 220px; font-size: 13px; border-radius: 8px; cursor: pointer;">
                                            ${optionsHtml}
                                        </select>
                                        <button type="button" class="btn-add-import i-add-btn">
                                            <span class="material-icons" style="font-size: 16px;">add</span>
                                            <span>Thêm</span>
                                        </button>
                                    </div>
                                </div>
                            `;

                            const selectEl = item.querySelector('.i-supplier-select');
                            const displayEl = item.querySelector('.i-price-display');
                            selectEl.onchange = function() {
                                const selectedOpt = this.options[this.selectedIndex];
                                const price = parseFloat(selectedOpt.getAttribute('data-price')) || 0;
                                displayEl.textContent = formatCurrency(price);
                            };

                            const addBtn = item.querySelector('.i-add-btn');
                            addBtn.onclick = function() {
                                const selectedOpt = selectEl.options[selectEl.selectedIndex];
                                addImportRow({
                                    productId: p.productId,
                                    productName: p.productName,
                                    supplierId: selectEl.value,
                                    supplierName: selectedOpt.text.split(' — ')[0],
                                    importPrice: parseFloat(selectedOpt.getAttribute('data-price')) || 0
                                });
                                iSearchActive = false;
                                iSearchResults.style.display = 'none';
                                iSearchInput.value = '';
                                iSearchInput.focus();
                            };
                        } else {
                            item.innerHTML = `
                                <div>
                                    <div class="fw-bold text-dark" style="font-size: 14.5px;">${p.productName}</div>
                                    <div class="text-danger small mt-1">Sản phẩm chưa được liên kết giá với nhà cung cấp nào!</div>
                                </div>
                                <span class="text-muted small">Cần cấu hình giá nhập</span>
                            `;
                        }

                        iSearchResults.appendChild(item);
                    });
                }
                if (iSearchActive) {
                    iSearchResults.style.display = 'block';
                }
            });
    }

    if (importStockForm) {
        importStockForm.addEventListener('submit', function(e) {
            const isExcelTabActive = document.getElementById('excelImportPane')?.classList.contains('active');
            if (isExcelTabActive) {
                const excelTableBody = document.getElementById('excelImportProductTableBody');
                const rowCount = excelTableBody ? excelTableBody.querySelectorAll('tr.excel-data-row').length : 0;
                if (rowCount === 0) {
                    e.preventDefault();
                    alert('Vui lòng tải lên và import ít nhất một sản phẩm hợp lệ từ file Excel.');
                    return;
                }
                const hasErrors = excelTableBody ? excelTableBody.querySelectorAll('tr[data-error="true"]').length > 0 : false;
                if (hasErrors) {
                    e.preventDefault();
                    alert('Vui lòng sửa hoặc xóa các sản phẩm bị báo đỏ trước khi nhập hàng.');
                    return;
                }
            } else {
                if (iTableBody.querySelectorAll('.i-qty-input').length === 0) {
                    e.preventDefault();
                    alert('Vui lòng tìm và thêm ít nhất một sản phẩm vào phiếu nhập.');
                }
            }
        });
    }

    // Tab switching event listeners
    const manualTab = document.getElementById('manual-tab');
    const excelTab = document.getElementById('excel-tab');
    if (manualTab && excelTab) {
        manualTab.addEventListener('shown.bs.tab', function() {
            const manualRows = iTableBody.querySelectorAll('.i-qty-input');
            if (iSubmitBtn) iSubmitBtn.disabled = manualRows.length === 0;

            document.querySelectorAll('#excelImportPane [name="productId[]"], #excelImportPane [name="supplierId[]"], #excelImportPane [name="importPrice[]"], #excelImportPane [name="quantity[]"]').forEach(el => el.disabled = true);
            document.querySelectorAll('#manualImportPane [name="productId[]"], #manualImportPane [name="supplierId[]"], #manualImportPane [name="importPrice[]"], #manualImportPane [name="quantity[]"]').forEach(el => el.disabled = false);
        });

        excelTab.addEventListener('shown.bs.tab', function() {
            checkExcelImportState();

            document.querySelectorAll('#manualImportPane [name="productId[]"], #manualImportPane [name="supplierId[]"], #manualImportPane [name="importPrice[]"], #manualImportPane [name="quantity[]"]').forEach(el => el.disabled = true);
            document.querySelectorAll('#excelImportPane [name="productId[]"], #excelImportPane [name="supplierId[]"], #excelImportPane [name="importPrice[]"], #excelImportPane [name="quantity[]"]').forEach(el => el.disabled = false);
        });
    }

    const importFormEl = document.getElementById('importStockForm');
    if (importFormEl) {
        importFormEl.addEventListener('submit', function(e) {
            const isExcelTabActive = document.getElementById('excelImportPane')?.classList.contains('active');
            if (isExcelTabActive) {
                const excelTableBody = document.getElementById('excelImportProductTableBody');
                if (excelTableBody) {
                    const errorRows = excelTableBody.querySelectorAll('tr.excel-data-row[data-error="true"]');
                    if (errorRows.length > 0) {
                        e.preventDefault();
                        alert('Vui lòng sửa các dòng có lỗi nhà cung cấp (viền đỏ) trước khi nhập hàng.\nHãy đổi sang nhà cung cấp có lịch sử giá nhập hợp lệ.');
                        return;
                    }
                    const dataRows = excelTableBody.querySelectorAll('tr.excel-data-row');
                    if (dataRows.length === 0) {
                        e.preventDefault();
                        alert('Vui lòng tải lên file Excel và thêm ít nhất một sản phẩm.');
                        return;
                    }
                }
                const noteTA = document.querySelector('#manualImportPane textarea[name="note"]');
                const excelNote = document.getElementById('excelImportNote');
                if (noteTA && excelNote) {
                    noteTA.value = excelNote.value;
                }
            }
        });
    }
}

function checkWarehouseAndOpenModal() {
    const currentWarehouseId = getSelectedWarehouseId();
    if (!currentWarehouseId || currentWarehouseId === '' || currentWarehouseId === 'null') {
        alert('Vui lòng chọn một kho hàng ở bộ lọc trước khi thực hiện nhập hàng!');
        return;
    }
    const modal = new bootstrap.Modal(document.getElementById('importStockModal'));
    modal.show();
}

function addImportRow(product) {
    const iTableBody = document.getElementById('importProductTableBody');
    const iEmptyRow = document.getElementById('importEmptyRow');
    const iSubmitBtn = document.getElementById('importSubmitBtn');

    if (!iTableBody) return;

    let isDuplicate = false;
    iTableBody.querySelectorAll('tr').forEach(tr => {
        if (tr.id === 'importEmptyRow') return;
        const pidInput = tr.querySelector('input[name="productId[]"]');
        const sidInput = tr.querySelector('input[name="supplierId[]"]');
        if (pidInput && sidInput && pidInput.value == product.productId && sidInput.value == product.supplierId) {
            isDuplicate = true;
            tr.style.backgroundColor = '#fef3c7';
            setTimeout(() => tr.style.backgroundColor = '', 600);
        }
    });

    if (isDuplicate) return;
    if (iEmptyRow) iEmptyRow.style.display = 'none';
    if (iSubmitBtn) iSubmitBtn.disabled = false;

    const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td class="ps-3 py-3">
            <div class="fw-bold text-dark">${product.productName}</div>
            <div class="text-muted small" style="font-size: 11px;">Mã SP: SP${product.productId}</div>
            <input type="hidden" name="productId[]" value="${product.productId}">
        </td>
        <td>
            <div class="fw-semibold text-dark" style="font-size: 14px;">${product.supplierName}</div>
            <input type="hidden" name="supplierId[]" value="${product.supplierId}">
        </td>
        <td>
            <div class="fw-bold text-success" style="font-size: 14.5px;">${formatCurrency(product.importPrice)}</div>
            <input type="hidden" name="importPrice[]" value="${product.importPrice}">
        </td>
        <td>
            <input type="number" name="quantity[]" class="form-control form-control-sm text-center fw-bold i-qty-input" required value="1" min="1" style="width: 80px; margin: 0 auto; border-radius: 8px;">
        </td>
        <td class="text-center">
            <button type="button" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-1" onclick="this.closest('tr').remove(); checkImportEmpty();" title="Xóa khỏi phiếu" style="width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center;">
                <span class="material-icons" style="font-size: 18px;">delete</span>
            </button>
        </td>
    `;
    
    iTableBody.appendChild(tr);
}

function checkImportEmpty() {
    const iTableBody = document.getElementById('importProductTableBody');
    const iEmptyRow = document.getElementById('importEmptyRow');
    const iSubmitBtn = document.getElementById('importSubmitBtn');
    if (!iTableBody) return;

    const rowCount = iTableBody.querySelectorAll('tr:not(#importEmptyRow)').length;
    if (rowCount === 0) {
        if (iEmptyRow) iEmptyRow.style.display = 'table-row';
        if (iSubmitBtn) iSubmitBtn.disabled = true;
    }
}

// ========== EXCEL IMPORT TAB LOGIC ==========

function downloadExcelTemplate() {
    if (typeof XLSX === 'undefined') {
        alert('Thư viện xử lý Excel chưa tải xong. Vui lòng thử lại sau vài giây.');
        return;
    }

    const url = getContextPath() + '/inventory?action=getImportTemplateDataApi';
    
    fetch(url)
        .then(res => res.json())
        .then(data => {
            const dataData = [
                ["Tên Sản Phẩm", "Mã NCC", "Tên NCC (Có thể bỏ qua)", "Số Lượng Nhập"]
            ];

            data.forEach(p => {
                // p[0]: Product Name, p[1]: Supplier ID, p[2]: Supplier Name
                dataData.push([
                    p[0],
                    parseInt(p[1]) || 0,
                    p[2],
                    ''
                ]);
            });

            const worksheet = XLSX.utils.aoa_to_sheet(dataData);

            worksheet['!cols'] = [
                { wch: 35 }, 
                { wch: 22 }, 
                { wch: 25 }, 
                { wch: 15 }  
            ];

            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, "Mau_Nhap_Hang");

            const xlsxBin = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
            const blob = new Blob([xlsxBin], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
            
            const link = document.createElement("a");
            link.href = URL.createObjectURL(blob);
            link.download = "Mau_Nhap_Hang_" + new Date().toISOString().slice(0, 10) + ".xlsx";
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        })
        .catch(err => {
            console.error('Error fetching template data:', err);
            alert('Lỗi tải dữ liệu mẫu nhập hàng.');
        });
}

function triggerExcelImportUpload() {
    const input = document.getElementById('excelImportFileInput');
    if (input) input.click();
}

function handleExcelUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.xlsx')) {
        alert('Vui lòng chọn đúng tệp Excel định dạng (.xlsx)');
        event.target.value = '';
        return;
    }

    if (file.size > 5 * 1024 * 1024) {
        alert('Dung lượng tệp quá lớn! Vui lòng tải lên tệp Excel có dung lượng nhỏ hơn 5MB.');
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
            
            parseImportExcel(rows);
        } catch (err) {
            console.error(err);
            alert('Lỗi khi đọc file Excel. V vui lòng đảm bảo tệp đúng định dạng.');
        }
    };
    reader.readAsArrayBuffer(file);
    event.target.value = '';
}

function parseImportExcel(rows) {
    const excelTableBody = document.getElementById('excelImportProductTableBody');
    const excelEmptyRow = document.getElementById('excelImportEmptyRow');
    if (!excelTableBody) return;

    if (!rows || rows.length < 2) {
        alert('Tệp Excel không chứa dữ liệu sản phẩm.');
        return;
    }

    const removeAccents = (str) => {
        return str.normalize('NFD')
                  .replace(/[\u0300-\u036f]/g, '')
                  .replace(/đ/g, 'd')
                  .replace(/Đ/g, 'D');
    };

    let headerRowIdx = -1;
    for (let i = 0; i < Math.min(10, rows.length); i++) {
        const r = rows[i];
        if (r) {
            const joinedText = removeAccents(r.map(c => String(c || '').toLowerCase()).join(' '));
            if (joinedText.indexOf('ma ncc') > -1 || joinedText.indexOf('supplier id') > -1 || joinedText.indexOf('ma nha cung cap') > -1 || joinedText.indexOf('ten san pham') > -1 || joinedText.indexOf('san pham') > -1) {
                headerRowIdx = i;
                break;
            }
        }
    }

    if (headerRowIdx === -1) {
        if (rows[0] && rows[0].length > 0) {
            headerRowIdx = 0;
        } else {
            alert('Không tìm thấy tiêu đề cột hợp lệ (Tên Sản Phẩm, Mã NCC (Supplier ID), Tên NCC, Số Lượng Nhập).');
            return;
        }
    }

    const headers = rows[headerRowIdx];
    let nameIdx = -1, supplierIdIdx = -1, supplierNameIdx = -1, qtyIdx = -1;
    for (let j = 0; j < headers.length; j++) {
        const val = removeAccents(String(headers[j] || '').toLowerCase().trim());
        
        // Match product name
        if (val.indexOf('ten san pham') > -1 || val.indexOf('ten sp') > -1) {
            nameIdx = j;
        } else if (val.indexOf('san pham') > -1 && nameIdx === -1) {
            nameIdx = j;
        } else if (val.indexOf('sp') > -1 && nameIdx === -1) {
            nameIdx = j;
        }
        
        // Match supplier ID
        if (val.indexOf('ma ncc') > -1 || val.indexOf('supplier id') > -1 || val.indexOf('ma nha cung cap') > -1 || val.indexOf('ma supplier') > -1) {
            supplierIdIdx = j;
        }
        
        // Match supplier name
        if (val.indexOf('ten ncc') > -1 || val.indexOf('ten nha cung cap') > -1 || val.indexOf('supplier name') > -1) {
            supplierNameIdx = j;
        }
        
        // Match quantity
        if (val.indexOf('so luong nhap') > -1 || val.indexOf('so luong') > -1 || val.indexOf('qty') > -1 || val.indexOf('quantity') > -1 || val.indexOf('sl') > -1) {
            qtyIdx = j;
        }
    }

    if (nameIdx === -1 || supplierIdIdx === -1 || qtyIdx === -1) {
        alert('Tệp thiếu cột bắt buộc. Vui lòng sử dụng file mẫu tải từ hệ thống hoặc tạo file có các cột: Tên sản phẩm, Mã NCC, Số lượng.');
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
                alert('Tệp Excel chứa dòng trống ở vị trí dòng ' + excelRowNumber + '. Vui lòng xóa các dòng trống đan xen trước khi tải lên.');
                return;
            }
        }
    }

    const url = getContextPath() + '/inventory?action=searchImportProductsApi&keyword=%25&warehouseId=' + getSelectedWarehouseId();
    fetch(url)
        .then(res => res.json())
        .then(apiProducts => {
            excelTableBody.innerHTML = '';
            if (excelEmptyRow) excelEmptyRow.style.display = 'none';

            let validCount = 0;
            const groups = {};

            for (let i = headerRowIdx + 1; i < rows.length; i++) {
                const r = rows[i];
                if (!r) continue;

                // Check if row is completely empty
                const isRowEmpty = r.every(cell => cell === null || cell === undefined || String(cell).trim() === '');
                if (isRowEmpty) continue;

                const excelProductName = (r[nameIdx] !== undefined && r[nameIdx] !== null) ? String(r[nameIdx]).trim() : '';
                const rawSupplierId = (r[supplierIdIdx] !== undefined && r[supplierIdIdx] !== null) ? String(r[supplierIdIdx]).trim() : '';
                const sId = parseInt(rawSupplierId);
                const rawQtyStr = (r[qtyIdx] !== undefined && r[qtyIdx] !== null) ? String(r[qtyIdx]).trim() : '';
                const qtyVal = parseInt(rawQtyStr);
                const isQtyValid = /^[1-9]\d*$/.test(rawQtyStr);

                const key = `${excelProductName.toLowerCase().trim()}||${rawSupplierId.toLowerCase().trim()}`;
                
                if (!groups[key]) {
                    groups[key] = {
                        excelProductName,
                        rawSupplierId,
                        sId,
                        qty: 0,
                        rawQtyStr: rawQtyStr,
                        hasInvalidQty: !isQtyValid
                    };
                }

                if (isQtyValid) {
                    groups[key].qty += qtyVal;
                    groups[key].rawQtyStr = String(groups[key].qty);
                } else {
                    groups[key].hasInvalidQty = true;
                    groups[key].rawQtyStr = rawQtyStr; // Keep original invalid string
                }
            }

            Object.values(groups).forEach(group => {
                const excelProductName = group.excelProductName;
                const rawSupplierId = group.rawSupplierId;
                const sId = group.sId;
                const rawQty = group.rawQtyStr;

                // Validate Product
                let apiProd = null;
                if (excelProductName !== '') {
                    apiProd = apiProducts.find(ap => ap.productName.toLowerCase().trim() === excelProductName.toLowerCase().trim());
                }
                const productValid = !!apiProd;

                // Validate Supplier
                let hasValidSupplier = false;
                let matchedPrice = 0;
                let selectOptions = '';
                const linkedSupplierIds = new Set();

                if (apiProd) {
                    if (apiProd.suppliers && apiProd.suppliers.length > 0) {
                        apiProd.suppliers.forEach(sup => {
                            linkedSupplierIds.add(sup.supplierId);
                            const isSelected = sup.supplierId === sId;
                            if (isSelected) {
                                matchedPrice = sup.importPrice || 0;
                                hasValidSupplier = true;
                            }
                            selectOptions += `<option value="${sup.supplierId}" ${isSelected ? 'selected' : ''} data-price="${sup.importPrice || 0}" data-linked="true">${sup.supplierName}</option>`;
                        });
                    }
                }

                if (!hasValidSupplier && !isNaN(sId) && sId > 0) {
                    const activeSuppliers = window.ACTIVE_SUPPLIERS || [];
                    const foundSup = activeSuppliers.find(sup => sup.supplierId === sId);
                    const supName = foundSup ? foundSup.supplierName : ('NCC #' + sId);
                    selectOptions += `<option value="${sId}" selected data-price="0" data-linked="false">${supName} (Lỗi: Nhà cung cấp này không có sản phẩm này)</option>`;
                }

                if (!hasValidSupplier) {
                    selectOptions = `<option value="" selected data-price="0" data-linked="false">Không có nhà cung cấp</option>` + selectOptions;
                } else {
                    selectOptions = `<option value="" data-price="0" data-linked="false">Không có nhà cung cấp</option>` + selectOptions;
                }

                const activeSuppliers = window.ACTIVE_SUPPLIERS || [];
                activeSuppliers.forEach(sup => {
                    if (!linkedSupplierIds.has(sup.supplierId) && sup.supplierId !== sId) {
                        selectOptions += `<option value="${sup.supplierId}" data-price="0" data-linked="false">${sup.supplierName} (Lỗi: Nhà cung cấp này không có sản phẩm này)</option>`;
                    }
                });

                const supplierValid = hasValidSupplier;

                // Validate Quantity
                const qtyValid = /^[1-9]\d*$/.test(rawQty);

                const hasError = !productValid || !supplierValid || !qtyValid;

                const tr = document.createElement('tr');
                tr.className = 'excel-data-row';
                tr.setAttribute('data-id', apiProd ? apiProd.productId : '0');
                tr.setAttribute('data-error', hasError ? 'true' : 'false');

                let productColHtml = '';
                if (productValid) {
                    productColHtml = `
                        <div class="fw-bold text-dark">${apiProd.productName}</div>
                        <input type="hidden" name="productId[]" value="${apiProd.productId}">
                        <small class="text-muted">Mã SP: SP${apiProd.productId}</small>
                    `;
                } else {
                    productColHtml = `
                        <div class="text-danger fw-bold">⚠️ ${excelProductName || 'Sản phẩm trống'}</div>
                        <input type="hidden" name="productId[]" value="0">
                        <small class="text-danger fw-semibold">không tồn tại sản phẩm</small>
                    `;
                }

                const supplierBorderStyle = supplierValid ? '' : 'border: 2px solid #dc3545; background-color: #fff5f5; color: #dc3545;';
                const selectHtml = `
                    <select name="supplierId[]" class="form-select form-select-sm e-row-supplier-select" style="border-radius: 8px; font-size:13.5px; ${supplierBorderStyle}" onchange="validateExcelRow(this.closest('tr'))">
                        ${selectOptions}
                    </select>
                `;

                const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

                const qtyBorderStyle = qtyValid ? '' : 'border: 2px solid #dc3545; background-color: #fff5f5; color: #dc3545;';

                tr.innerHTML = `
                    <td class="ps-3 py-3">
                        ${productColHtml}
                    </td>
                    <td>
                        ${selectHtml}
                    </td>
                    <td>
                        <span class="fw-bold text-dark">${formatCurrency(matchedPrice)}</span>
                        <input type="hidden" name="importPrice[]" class="e-row-price-input" value="${matchedPrice}">
                    </td>
                    <td>
                        <input type="text" name="quantity[]" class="form-control form-control-sm text-center fw-bold e-row-qty-input" value="${rawQty}" required style="width: 100px; margin: 0 auto; border-radius: 8px; ${qtyBorderStyle}" oninput="validateExcelRow(this.closest('tr'))">
                    </td>
                    <td class="text-center">
                        <button type="button" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-1" onclick="this.closest('tr').remove(); checkExcelImportState();" style="width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center;">
                            <span class="material-icons" style="font-size: 18px;">delete</span>
                        </button>
                    </td>
                `;

                excelTableBody.appendChild(tr);
                validCount++;
            });

            checkExcelImportState();
            const totalRows = excelTableBody.querySelectorAll('tr.excel-data-row').length;
            const errorRows = excelTableBody.querySelectorAll('tr[data-error="true"]').length;
            if (errorRows > 0) {
                alert(`Đã nạp ${totalRows} dòng từ Excel. Có ${errorRows} dòng bị lỗi (ô bị tô đỏ). Vui lòng sửa hoặc xóa chúng trước khi xác nhận nhập hàng.`);
            } else if (totalRows > 0) {
                alert(`Đã nạp thành công cả ${totalRows} dòng từ Excel không có lỗi!`);
            }
        });
}

function validateExcelRow(tr) {
    const pIdInput = tr.querySelector('input[name="productId[]"]');
    const selectEl = tr.querySelector('.e-row-supplier-select');
    const priceEl = tr.querySelector('.e-row-price-input');
    const qtyEl = tr.querySelector('.e-row-qty-input');

    const pId = parseInt(pIdInput.value) || 0;
    const sId = parseInt(selectEl.value) || 0;
    const rawQty = qtyEl.value.trim();

    let productValid = (pId > 0);
    
    // Check if the selected option is linked to the product
    const selectedOpt = selectEl.options[selectEl.selectedIndex];
    const isLinked = selectedOpt ? (selectedOpt.getAttribute('data-linked') === 'true') : false;
    let supplierValid = (sId > 0) && isLinked;
    
    let qtyValid = /^[1-9]\d*$/.test(rawQty);

    // Apply styles to supplier select
    if (supplierValid) {
        selectEl.style.border = '';
        selectEl.style.backgroundColor = '';
        selectEl.style.color = '';
    } else {
        selectEl.style.border = '2px solid #dc3545';
        selectEl.style.backgroundColor = '#fff5f5';
        selectEl.style.color = '#dc3545';
    }

    // Apply styles to qty input
    if (qtyValid) {
        qtyEl.style.border = '';
        qtyEl.style.backgroundColor = '';
        qtyEl.style.color = '';
    } else {
        qtyEl.style.border = '2px solid #dc3545';
        qtyEl.style.backgroundColor = '#fff5f5';
        qtyEl.style.color = '#dc3545';
    }

    // Update row state
    const hasError = !productValid || !supplierValid || !qtyValid;
    tr.setAttribute('data-error', hasError ? 'true' : 'false');
    
    // Update price if supplier is changed
    if (selectedOpt) {
        const price = parseFloat(selectedOpt.getAttribute('data-price')) || 0;
        priceEl.value = price;
        const priceSpan = tr.querySelector('td:nth-child(3) span');
        if (priceSpan) {
            priceSpan.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
        }
    }
}

function checkExcelImportState() {
    const excelTableBody = document.getElementById('excelImportProductTableBody');
    const excelEmptyRow = document.getElementById('excelImportEmptyRow');
    const iSubmitBtn = document.getElementById('importSubmitBtn');
    if (!excelTableBody) return;

    const rowCount = excelTableBody.querySelectorAll('tr.excel-data-row').length;
    if (rowCount === 0) {
        if (excelEmptyRow) excelEmptyRow.style.display = 'table-row';
        if (iSubmitBtn) iSubmitBtn.disabled = true;
    } else {
        if (excelEmptyRow) excelEmptyRow.style.display = 'none';
        if (iSubmitBtn) iSubmitBtn.disabled = false;
    }
}



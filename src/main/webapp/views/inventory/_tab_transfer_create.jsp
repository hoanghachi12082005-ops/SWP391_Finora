<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="selectedWarehouseName" value="Kho hiện tại" />
<c:forEach var="w" items="${warehouses}">
    <c:if test="${w.warehouseId == selectedWarehouseId}">
        <c:set var="selectedWarehouseName" value="${w.warehouseName}" />
    </c:if>
</c:forEach>

<style>
    .search-box { position: relative; max-width: 800px; margin: 0 auto 30px; }
    .search-input { width: 100%; padding: 16px 20px 16px 50px; border-radius: 12px; border: 1px solid #cbd5e1; font-size: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); transition: all 0.2s ease-in-out; }
    .search-input:focus { outline: none; border-color: var(--primary-color); box-shadow: 0 0 0 4px var(--primary-light); }
    .search-icon { position: absolute; left: 18px; top: 50%; transform: translateY(-50%); color: #64748b; font-size: 24px; }
    
    /* Autocomplete dropdown styles - Premium redesign */
    .search-results { 
        position: absolute; 
        top: 100%; 
        left: 0; 
        right: 0; 
        background: #ffffff; 
        border-radius: 12px; 
        box-shadow: 0 15px 35px rgba(0,0,0,0.1); 
        margin-top: 8px; 
        z-index: 100; 
        display: none; 
        max-height: 420px; 
        overflow-y: auto; 
        border: 1px solid #e2e8f0; 
    }
    
    .suggestion-header { 
        background: #fffbeb; 
        color: #b45309; 
        font-weight: 700; 
        font-size: 13px; 
        padding: 12px 20px; 
        border-bottom: 1px solid #fef3c7; 
        text-transform: uppercase; 
        letter-spacing: 0.5px; 
        display: flex;
        align-items: center;
        gap: 6px;
        white-space: nowrap;
    }

    .normal-header {
        background: #f8fafc; 
        color: #475569; 
        border-bottom: 1px solid #e2e8f0; 
    }
    
    .search-item { 
        padding: 14px 20px; 
        border-bottom: 1px solid #f1f5f9; 
        cursor: pointer; 
        display: flex; 
        justify-content: space-between; 
        align-items: center; 
        transition: all 0.2s ease-in-out; 
    }
    
    .search-item:hover { 
        background: rgba(147, 0, 11, 0.03); 
    }
    
    .search-item:last-child { border-bottom: none; }
    
    .partner-choice-select {
        border-radius: 8px;
        font-size: 13.5px;
        padding: 6px 12px;
        border: 1px solid #cbd5e1;
        outline: none;
        background-color: #fff;
        cursor: pointer;
        transition: border-color 0.2s;
    }
    .partner-choice-select:focus {
        border-color: var(--primary-color);
    }
    
    .add-icon-btn {
        color: var(--primary-color);
        font-size: 32px;
        cursor: pointer;
        transition: all 0.2s ease-in-out;
        vertical-align: middle;
    }
    .add-icon-btn:hover {
        transform: scale(1.15);
        color: #74000a;
    }

    /* Smart table */
    .smart-table { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); overflow-x: auto; border: 1px solid #e2e8f0; }
    .smart-table th { background: #f8fafc; color: #475569; font-weight: 600; padding: 12px 10px; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e2e8f0; white-space: nowrap; }
    .smart-table td { padding: 12px 10px; vertical-align: middle; border-bottom: 1px solid #f1f5f9; }
    .empty-state { padding: 40px; text-align: center; color: #94a3b8; }
</style>

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-4 border-bottom-0 pb-0">
        <div>
            <h5 class="mb-0 fw-bold" style="color: #93000b;">📋 Tạo Phiếu Điều Chuyển Kho</h5>
            <small class="text-muted">Kho hiện tại: <strong>${selectedWarehouseName}</strong></small>
        </div>
        <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${selectedWarehouseId}" class="btn btn-outline-secondary btn-sm" style="border-radius: 8px;">
            <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">arrow_back</span>
            Quay Lại
        </a>
    </div>

    <div class="card-body">
        <!-- Search Section -->
        <div class="search-box">
            <span class="material-icons search-icon">search</span>
            <input type="text" class="search-input" id="productSearch" placeholder="Tìm kiếm sản phẩm hoặc chi nhánh..." autocomplete="off">
            <div class="search-results" id="searchResults">
                <!-- JS Populated -->
            </div>
        </div>

        <!-- Form Section -->
        <form action="${pageContext.request.contextPath}/inventory" method="POST" id="transferForm">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="saveTransfer">
            <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">

            <div class="smart-table mb-4">
                <table class="table mb-0" style="table-layout: fixed; width: 100%; min-width: 850px;">
                    <thead>
                        <tr>
                            <th width="20%" style="white-space: nowrap;">Sản Phẩm</th>
                            <th width="15%" style="white-space: nowrap;">Tồn Của Bạn</th>
                            <th width="25%" style="white-space: nowrap;">Kho Đối Tác</th>
                            <th width="15%" style="white-space: nowrap;">Loại Giao Dịch</th>
                            <th width="18%" style="white-space: nowrap;">Số Lượng</th>
                            <th width="7%" class="text-center" style="white-space: nowrap;">Xóa</th>
                        </tr>
                    </thead>
                    <tbody id="transferTableBody">
                        <tr id="emptyRow">
                            <td colspan="6">
                                <div class="empty-state text-center py-5">
                                    <span class="material-icons mb-3" style="font-size: 48px; color: #cbd5e1;">inventory</span>
                                    <h6 class="fw-bold mb-2">Chưa có sản phẩm nào</h6>
                                    <p class="text-muted small mb-0">Tìm kiếm và chọn sản phẩm ở trên để thêm vào phiếu</p>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="d-flex justify-content-end">
                <button type="submit" class="page-action-btn px-4 py-2" id="submitBtn" disabled style="background-color: var(--primary-color); border: none; color: white; border-radius: 8px; font-weight: 600; display: flex; align-items: center; gap: 8px;">
                    <span class="material-icons" style="font-size: 18px;">send</span>
                    Khởi Tạo Lệnh Điều Chuyển
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    const searchInput = document.getElementById('productSearch');
    const searchResults = document.getElementById('searchResults');
    const tableBody = document.getElementById('transferTableBody');
    const emptyRow = document.getElementById('emptyRow');
    const submitBtn = document.getElementById('submitBtn');
    const contextPath = '${pageContext.request.contextPath}';
    const currentWarehouseId = '${selectedWarehouseId}';
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

    function fetchResults(keyword) {
        var url = contextPath + '/inventory?action=searchAllProductsApi&keyword=' + encodeURIComponent(keyword) + '&warehouseId=' + currentWarehouseId;
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
                        
                        // Generate select options for all partner warehouses
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

    // Hide search results when clicking outside
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
            searchResults.style.display = 'none';
        }
    });

    function addRow(product) {
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
            searchResults.style.display = 'none';
            searchInput.value = '';
            return;
        }

        if (emptyRow) emptyRow.style.display = 'none';
        submitBtn.disabled = false;
        searchResults.style.display = 'none';
        searchInput.value = '';

        const stockColor = product.myStock > 0 ? 'text-success' : 'text-danger';

        // Generate select options dynamically from the product's partners array to show stock levels!
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
            const targetWId = actionType === 'SEND' ? currentWarehouseId : partnerWId;
            qtyInput.setAttribute('data-limit', '0');

            if (!targetWId) {
                return;
            }

            var stockUrl = contextPath + '/inventory?action=getProductStockApi&productId=' + product.productId + '&warehouseId=' + targetWId;
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
        if (tableBody.querySelectorAll('tr').length === 1 && tableBody.querySelector('#emptyRow')) {
            emptyRow.style.display = 'table-row';
            submitBtn.disabled = true;
        } else if (tableBody.querySelectorAll('tr').length === 0) {
            if (emptyRow) emptyRow.style.display = 'table-row';
            submitBtn.disabled = true;
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

    document.getElementById('transferForm').addEventListener('submit', function(e) {
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
</script>

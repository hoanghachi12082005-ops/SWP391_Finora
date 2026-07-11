<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    .search-box { position: relative; max-width: 800px; margin: 0 auto 30px; }
    .search-input { width: 100%; padding: 16px 20px 16px 50px; border-radius: 12px; border: 1px solid #e2e8f0; font-size: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); transition: all 0.2s; }
    .search-input:focus { outline: none; border-color: var(--primary-color); box-shadow: 0 0 0 4px var(--primary-light-hover); }
    .search-icon { position: absolute; left: 16px; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 24px; }
    .search-results { position: absolute; top: 100%; left: 0; right: 0; background: #fff; border-radius: 12px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1); margin-top: 8px; z-index: 50; display: none; max-height: 400px; overflow-y: auto; border: 1px solid #e2e8f0; }
    .search-item { padding: 12px 20px; border-bottom: 1px solid #f1f5f9; cursor: pointer; display: flex; justify-content: space-between; align-items: center; transition: background 0.2s; }
    .search-item:hover { background: #f8fafc; }
    .search-item:last-child { border-bottom: none; }
    .smart-table { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e2e8f0; }
    .smart-table th { background: #f8fafc; color: #64748b; font-weight: 600; padding: 16px; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e2e8f0; }
    .smart-table td { padding: 16px; vertical-align: middle; border-bottom: 1px solid #f1f5f9; }
    .empty-state { padding: 40px; text-align: center; color: #94a3b8; }
    .suggestion-header { padding: 10px 20px; background: #fffbeb; color: #b45309; font-size: 13px; font-weight: 600; text-transform: uppercase; border-bottom: 1px solid #fde68a; }
</style>

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-4">
        <div>
            <h5 class="mb-0">Tạo Lệnh Điều Chuyển</h5>
            <small class="text-muted">Từ: <strong>Kho hiện tại</strong></small>
        </div>
        <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${selectedWarehouseId}" class="btn btn-outline-secondary btn-sm">
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
                <table class="table mb-0">
                    <thead>
                        <tr>
                            <th width="25%">Sản Phẩm</th>
                            <th width="10%" class="text-center">Tồn Kho</th>
                            <th width="25%">Chi Nhánh Đối Tác</th>
                            <th width="15%">Loại Giao Dịch</th>
                            <th width="20%">Số Lượng</th>
                            <th width="5%" class="text-center">Xóa</th>
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
                <button type="submit" class="page-action-btn px-4 py-2" id="submitBtn" disabled>
                    <span class="material-icons" style="font-size: 18px; vertical-align: text-bottom;">send</span>
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
        fetch(`${pageContext.request.contextPath}/inventory?action=searchProductsApi&keyword=` + encodeURIComponent(keyword) + `&warehouseId=${selectedWarehouseId}`)
            .then(res => res.json())
            .then(data => {
                searchResults.innerHTML = '';
                if (data.length === 0) {
                    if (keyword === '') {
                        searchResults.innerHTML = '<div class="p-3 text-center text-muted"><span class="material-icons" style="font-size:40px; color:#e2e8f0;">search</span><br>Gõ tên hoặc mã sản phẩm để tìm kiếm...</div>';
                    } else {
                        searchResults.innerHTML = '<div class="p-3 text-center text-muted">Không tìm thấy kho nào có sẵn sản phẩm này</div>';
                    }
                } else {
                    if (keyword === '') {
                        const header = document.createElement('div');
                        header.className = 'suggestion-header';
                        header.innerHTML = '<span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">warning</span> Gợi ý hàng sắp hết (Có thể nhập từ kho khác)';
                        searchResults.appendChild(header);
                    }
                    const grouped = {};
                    data.forEach(p => {
                        if (!grouped[p.productId]) {
                            grouped[p.productId] = {
                                productId: p.productId,
                                productName: p.productName,
                                myStock: p.myStock,
                                partners: []
                            };
                        }
                        grouped[p.productId].partners.push({
                            partnerWarehouseId: p.partnerWarehouseId,
                            partnerWarehouseName: p.partnerWarehouseName,
                            partnerStock: p.partnerStock
                        });
                    });

                    Object.values(grouped).forEach(g => {
                        const item = document.createElement('div');
                        item.className = 'search-item d-flex align-items-center justify-content-between p-2 border-bottom';
                        item.style.cursor = 'default';
                        
                        let optionsHtml = '';
                        g.partners.forEach(partner => {
                            optionsHtml += `<option value="\${partner.partnerWarehouseId}">\${partner.partnerWarehouseName} (Tồn: \${partner.partnerStock})</option>`;
                        });

                        item.innerHTML = `
                            <div>
                                <div class="fw-semibold text-dark">\${g.productName}</div>
                                <div class="small text-muted mt-1">
                                    Tồn của bạn: <strong class="\${g.myStock > 0 ? 'text-success' : 'text-danger'}">\${g.myStock}</strong>
                                </div>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <select class="form-select form-select-sm partner-select" style="width: 230px; font-size: 13px; cursor: pointer;">
                                    \${optionsHtml}
                                </select>
                                <span class="material-icons text-danger add-btn" style="font-size: 28px; cursor: pointer; transition: transform 0.2s;" onmouseover="this.style.transform='scale(1.1)'" onmouseout="this.style.transform='scale(1)'" title="Thêm vào danh sách">add_circle</span>
                            </div>
                        `;
                        
                        const selectEl = item.querySelector('.partner-select');
                        const addBtn = item.querySelector('.add-btn');
                        
                        addBtn.onclick = () => {
                            const selectedId = parseInt(selectEl.value);
                            const partner = g.partners.find(p => p.partnerWarehouseId === selectedId);
                            if (partner) {
                                addRow({
                                    productId: g.productId,
                                    productName: g.productName,
                                    myStock: g.myStock,
                                    partnerWarehouseId: partner.partnerWarehouseId,
                                    partnerWarehouseName: partner.partnerWarehouseName,
                                    partnerStock: partner.partnerStock,
                                    allPartners: g.partners
                                });
                            }
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
            const pwidInput = tr.querySelector('[name="partnerWarehouseId[]"]');
            if (pidInput && pwidInput) {
                if (pidInput.value == product.productId && pwidInput.value == product.partnerWarehouseId) {
                    isDuplicate = true;
                }
            }
        });

        if (isDuplicate) {
            Toast.fire({
                icon: 'warning',
                title: 'Đã chọn',
                text: 'Sản phẩm này từ kho ' + product.partnerWarehouseName + ' đã được chọn!'
            });
            searchResults.style.display = 'none';
            searchInput.value = '';
            return;
        }

        if (emptyRow) emptyRow.style.display = 'none';
        submitBtn.disabled = false;
        searchResults.style.display = 'none';
        searchInput.value = '';

        let partnerOptions = '';
        if (product.allPartners && product.allPartners.length > 0) {
            product.allPartners.forEach(p => {
                const selected = p.partnerWarehouseId == product.partnerWarehouseId ? 'selected' : '';
                partnerOptions += `<option value="\${p.partnerWarehouseId}" \${selected}>\${p.partnerWarehouseName} (Tồn: \${p.partnerStock})</option>`;
            });
        } else {
            partnerOptions = `<option value="\${product.partnerWarehouseId}">\${product.partnerWarehouseName} (Tồn: \${product.partnerStock})</option>`;
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div class="fw-medium text-dark">\${product.productName}</div>
                <input type="hidden" name="productId[]" value="\${product.productId}">
            </td>
            <td class="text-center fw-semibold \${product.myStock > 0 ? 'text-success' : 'text-danger'}">
                \${product.myStock}
            </td>
            <td>
                <select name="partnerWarehouseId[]" class="form-select form-select-sm" style="min-width: 180px; font-size: 13px; background-color: #f8fafc; font-weight: 500;">
                    \${partnerOptions}
                </select>
            </td>
            <td>
                <select name="actionType[]" class="form-select action-select" style="background-color: #f8fafc;" required>
                    <option value="RECEIVE" \${product.partnerStock > 0 ? 'selected' : ''}>Nhập</option>
                    <option value="SEND" \${product.partnerStock <= 0 ? 'selected' : ''}>Xuất</option>
                </select>
            </td>
            <td>
                <div class="position-relative">
                    <input type="number" name="quantity[]" class="form-control qty-input" required min="1" placeholder="Số lượng" data-mystock="\${product.myStock}">
                </div>
            </td>
            <td class="text-center">
                <button type="button" class="btn btn-sm btn-light text-danger" onclick="this.closest('tr').remove(); checkEmpty();">
                    <span class="material-icons" style="font-size: 18px;">delete</span>
                </button>
            </td>
        `;
        tableBody.appendChild(tr);
        validateRow(tr.querySelector('.qty-input')); // Validate initially
    }

    function checkEmpty() {
        if (tableBody.querySelectorAll('tr').length === 1 && tableBody.querySelector('#emptyRow')) {
            emptyRow.style.display = 'table-row';
            submitBtn.disabled = true;
        } else if (tableBody.querySelectorAll('tr').length === 0) {
            submitBtn.disabled = true;
        }
    }

    function validateRow(element) {
        if (!element) return true;
        const tr = element.closest('tr');
        if (!tr || tr.id === 'emptyRow') return true;
        const actionSelect = tr.querySelector('.action-select');
        const qtyInput = tr.querySelector('.qty-input');
        
        const qty = parseInt(qtyInput.value) || 0;
        
        if (qty <= 0 && qtyInput.value !== '') {
            return false;
        }
        
        if (actionSelect.value === 'SEND') {
            const myStock = parseInt(qtyInput.getAttribute('data-mystock')) || 0;
            if (qty > myStock) {
                return false;
            }
        }
        
        return true;
    }

    document.getElementById('transferForm').addEventListener('submit', function(e) {
        let valid = true;
        this.querySelectorAll('.qty-input').forEach(input => {
            if (!validateRow(input)) {
                valid = false;
            }
        });
        if (!valid) {
            e.preventDefault();
            Toast.fire({
                icon: 'error',
                title: 'Lỗi nhập liệu',
                text: 'Vui lòng kiểm tra lại: Không thể xuất số lượng lớn hơn tồn kho hiện tại!'
            });
        }
    });
</script>

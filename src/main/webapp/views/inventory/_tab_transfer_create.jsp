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
            <h5 class="mb-0">
                <c:choose>
                    <c:when test="${transferType == 'RECEIVE'}">
                        📥 Tạo Phiếu Nhập Chuyển Kho
                    </c:when>
                    <c:otherwise>
                        📤 Tạo Phiếu Xuất Chuyển Kho
                    </c:otherwise>
                </c:choose>
            </h5>
            <small class="text-muted">
                <c:choose>
                    <c:when test="${transferType == 'RECEIVE'}">
                        Nhập về: <strong>${selectedWarehouseName}</strong>
                    </c:when>
                    <c:otherwise>
                        Xuất từ: <strong>${selectedWarehouseName}</strong>
                    </c:otherwise>
                </c:choose>
            </small>
        </div>
        <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${selectedWarehouseId}" class="btn btn-outline-secondary btn-sm">
            <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">arrow_back</span>
            Quay Lại
        </a>
    </div>

    <div class="card-body">
        <!-- Warehouse Selector Section -->
        <div class="row align-items-center mb-4 bg-light p-3 rounded-3 mx-0">
            <div class="col-md-6 d-flex align-items-center gap-3">
                <c:choose>
                    <c:when test="${transferType == 'RECEIVE'}">
                        <span class="fw-semibold text-muted">Kho nhận (Đến):</span>
                        <strong class="text-dark fs-6">${selectedWarehouseName}</strong>
                    </c:when>
                    <c:otherwise>
                        <span class="fw-semibold text-muted">Kho xuất (Từ):</span>
                        <strong class="text-dark fs-6">${selectedWarehouseName}</strong>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-3 justify-content-md-end mt-2 mt-md-0">
                <c:choose>
                    <c:when test="${transferType == 'RECEIVE'}">
                        <span class="fw-semibold text-muted">Kho gửi (Từ):</span>
                    </c:when>
                    <c:otherwise>
                        <span class="fw-semibold text-muted">Kho nhận (Đến):</span>
                    </c:otherwise>
                </c:choose>
                <select id="partnerWarehouseSelect" class="form-select form-select-sm" style="width: 250px; font-weight: 600; cursor: pointer;">
                    <c:forEach var="w" items="${warehouses}">
                        <c:if test="${w.warehouseId != selectedWarehouseId}">
                            <option value="${w.warehouseId}">${w.warehouseName}</option>
                        </c:if>
                    </c:forEach>
                </select>
            </div>
        </div>

        <!-- Search Section -->
        <div class="search-box">
            <span class="material-icons search-icon">search</span>
            <input type="text" class="search-input" id="productSearch" placeholder="Tìm kiếm sản phẩm cần điều chuyển..." autocomplete="off">
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
                            <th width="20%" class="text-center">Tồn Kho (Gửi / Nhận)</th>
                            <th width="20%">Kho Đối Tác</th>
                            <th width="15%">Loại Giao Dịch</th>
                            <th width="15%">Số Lượng</th>
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
    const partnerSelect = document.getElementById('partnerWarehouseSelect');
    const contextPath = '${pageContext.request.contextPath}';
    const currentWarehouseId = '${selectedWarehouseId}';
    const transferType = '${transferType}';
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

    if (partnerSelect) {
        partnerSelect.addEventListener('change', function() {
            // Clear current items to avoid mix-up
            tableBody.querySelectorAll('tr').forEach(tr => {
                if (tr.id !== 'emptyRow') tr.remove();
            });
            checkEmpty();
            // Refetch suggestions
            fetchResults(searchInput.value.trim());
        });
    }

    function fetchResults(keyword) {
        const partnerId = partnerSelect ? partnerSelect.value : '0';
        const fromWId = transferType === 'RECEIVE' ? partnerId : currentWarehouseId;
        const toWId = transferType === 'RECEIVE' ? currentWarehouseId : partnerId;

        fetch(`${pageContext.request.contextPath}/inventory?action=searchProductsApi&keyword=` + encodeURIComponent(keyword) + `&fromWarehouseId=\${fromWId}&toWarehouseId=\${toWId}`)
            .then(res => res.json())
            .then(data => {
                searchResults.innerHTML = '';
                if (data.length === 0) {
                    if (keyword === '') {
                        searchResults.innerHTML = '<div class="p-3 text-center text-muted"><span class="material-icons" style="font-size:40px; color:#e2e8f0;">search</span><br>Gõ tên hoặc mã sản phẩm để tìm kiếm...</div>';
                    } else {
                        searchResults.innerHTML = '<div class="p-3 text-center text-muted">Không tìm thấy sản phẩm nào ở kho nguồn</div>';
                    }
                } else {
                    if (keyword === '') {
                        const header = document.createElement('div');
                        header.className = 'suggestion-header';
                        header.innerHTML = '<span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">warning</span> Gợi ý hàng sắp hết ở kho nhận';
                        searchResults.appendChild(header);
                    }
                    
                    data.forEach(p => {
                        const item = document.createElement('div');
                        item.className = 'search-item d-flex align-items-center justify-content-between p-2 border-bottom';
                        item.style.cursor = 'default';
                        
                        item.innerHTML = `
                            <div>
                                <div class="fw-semibold text-dark">\${p.productName}</div>
                                <div class="small text-muted mt-1">
                                    Tồn kho gửi: <strong class="\${p.myStock > 0 ? 'text-success' : 'text-danger'}">\${p.myStock}</strong> | 
                                    Tồn kho nhận: <strong class="\${p.partnerStock > 0 ? 'text-success' : 'text-danger'}">\${p.partnerStock}</strong>
                                </div>
                            </div>
                            <div>
                                <span class="material-icons text-danger add-btn" style="font-size: 28px; cursor: pointer; transition: transform 0.2s;" onmouseover="this.style.transform='scale(1.1)'" onmouseout="this.style.transform='scale(1)'" title="Thêm vào danh sách">add_circle</span>
                            </div>
                        `;
                        
                        const addBtn = item.querySelector('.add-btn');
                        addBtn.onclick = () => {
                            addRow({
                                productId: p.productId,
                                productName: p.productName,
                                myStock: p.myStock,
                                partnerStock: p.partnerStock
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

        const partnerName = partnerSelect.options[partnerSelect.selectedIndex].text;
        const partnerId = partnerSelect.value;

        const typeBadge = transferType === 'RECEIVE'
            ? '<span class="badge bg-success-subtle text-success border border-success-subtle py-1 px-2">NHẬP (Từ kho đối tác)</span><input type="hidden" name="actionType[]" class="action-select" value="RECEIVE">'
            : '<span class="badge bg-danger-subtle text-danger border border-danger-subtle py-1 px-2">XUẤT (Đi kho đối tác)</span><input type="hidden" name="actionType[]" class="action-select" value="SEND">';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div class="fw-medium text-dark">\${product.productName}</div>
                <input type="hidden" name="productId[]" value="\${product.productId}">
            </td>
            <td class="text-center fw-semibold">
                <span class="text-muted small">Gửi:</span> <strong class="\${product.myStock > 0 ? 'text-success' : 'text-danger'}">\${product.myStock}</strong><br>
                <span class="text-muted small">Nhận:</span> <strong class="\${product.partnerStock > 0 ? 'text-success' : 'text-danger'}">\${product.partnerStock}</strong>
            </td>
            <td>
                <span class="fw-semibold text-dark">\${partnerName}</span>
                <input type="hidden" name="partnerWarehouseId[]" value="\${partnerId}">
            </td>
            <td>
                \${typeBadge}
            </td>
            <td>
                <div class="position-relative">
                    <input type="number" name="quantity[]" class="form-control qty-input" required min="1" placeholder="Số lượng" 
                           data-mystock="\${product.myStock}" 
                           data-partnerstock="\${product.partnerStock}">
                </div>
            </td>
            <td class="text-center">
                <button type="button" class="btn btn-sm btn-light text-danger" onclick="this.closest('tr').remove(); checkEmpty();">
                    <span class="material-icons" style="font-size: 18px;">delete</span>
                </button>
            </td>
        `;
        tableBody.appendChild(tr);
        
        // Listeners for warning update
        const qtyInput = tr.querySelector('.qty-input');
        qtyInput.addEventListener('input', function() {
            validateInputWarning(this);
        });
        
        validateInputWarning(qtyInput);
    }

    function checkEmpty() {
        if (tableBody.querySelectorAll('tr').length === 1 && tableBody.querySelector('#emptyRow')) {
            emptyRow.style.display = 'table-row';
            submitBtn.disabled = true;
        } else if (tableBody.querySelectorAll('tr').length === 0) {
            submitBtn.disabled = true;
        }
    }

    function validateInputWarning(input) {
        const tr = input.closest('tr');
        if (!tr || tr.id === 'emptyRow') return;
        const qty = parseInt(input.value) || 0;
        
        let limit = transferType === 'SEND'
            ? (parseInt(input.getAttribute('data-mystock')) || 0)
            : (parseInt(input.getAttribute('data-partnerstock')) || 0);
        
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
            warningEl.innerHTML = `<span class="material-icons" style="font-size:12px;vertical-align:text-bottom;color:#fd7e14;">warning</span> Vượt tồn nguồn (\${limit})`;
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
            const tr = input.closest('tr');
            if (tr && tr.id !== 'emptyRow') {
                const qty = parseInt(input.value) || 0;
                let limit = transferType === 'SEND'
                    ? (parseInt(input.getAttribute('data-mystock')) || 0)
                    : (parseInt(input.getAttribute('data-partnerstock')) || 0);
                if (qty > limit) {
                    hasWarning = true;
                }
            }
        });
        
        if (hasWarning) {
            if (confirm("Cảnh báo: Có sản phẩm có số lượng điều chuyển vượt quá số lượng tồn kho thực tế của kho nguồn (kho gửi). Bạn có chắc chắn muốn tiếp tục tạo lệnh điều chuyển này?")) {
                this.submit();
            }
        } else {
            this.submit();
        }
    });
</script>

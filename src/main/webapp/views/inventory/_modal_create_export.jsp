<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!-- Modal Xuất Hàng -->
<div class="modal fade" id="exportStockModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/inventory" method="POST" id="exportStockForm">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="action" value="saveExport">
                <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                
                <div class="modal-header border-bottom-0 pb-0 flex-column align-items-start">
                    <div class="d-flex w-100 justify-content-between align-items-center">
                        <h5 class="modal-title fw-bold" style="color: #111827;">Xuất Hàng Khỏi Kho</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="small text-muted mt-1">Từ: <strong>Kho hiện tại</strong></div>
                </div>
                
                <div class="modal-body pt-4">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Tìm Sản Phẩm Xuất</label>
                        <div class="import-search-box">
                            <span class="material-icons import-search-icon">search</span>
                            <input type="text" id="exportSearchInput" class="import-search-input" placeholder="Gõ tên hoặc mã sản phẩm..." autocomplete="off">
                            <div class="import-search-results" id="exportSearchResults"></div>
                        </div>
                    </div>
                    
                    <div class="table-responsive" style="max-height: 300px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 8px;">
                        <table class="table table-sm align-middle mb-0" id="exportProductTable">
                            <thead style="background: #f8fafc; position: sticky; top: 0; z-index: 1;">
                                <tr>
                                    <th class="ps-3 py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="40%">Sản Phẩm</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="20%">Giá Ước Tính (Tùy chọn)</th>
                                    <th class="py-2 text-muted text-center" width="20%" style="font-weight: 600; font-size: 13px;">Số Lượng</th>
                                    <th class="py-2 text-center text-muted" width="10%" style="font-weight: 600; font-size: 13px;">Xóa</th>
                                </tr>
                            </thead>
                            <tbody id="exportProductTableBody">
                                <tr id="exportEmptyRow">
                                    <td colspan="4">
                                        <div class="text-center text-muted py-4">
                                            <span class="material-icons mb-2" style="font-size: 32px; color: #cbd5e1;">inventory_2</span>
                                            <p class="mb-0 small">Chưa có sản phẩm nào.<br>Tìm và chọn ở trên để xuất kho.</p>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    
                    <div class="mb-3 mt-4">
                        <label class="form-label fw-semibold text-muted small">Ghi chú xuất hàng / Lý do</label>
                        <textarea name="note" class="form-control" rows="2" placeholder="Ghi chú xuất kho (ví dụ: Xuất hủy hàng hỏng, Xuất trả hàng...)" style="border-radius: 8px;" required></textarea>
                    </div>
                </div>
                
                <div class="modal-footer border-top-0 pt-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: 8px; font-weight: 500;">Hủy</button>
                    <button type="submit" class="btn btn-primary" id="exportSubmitBtn" disabled style="border-radius: 8px; font-weight: 500;">
                        <span class="material-icons" style="font-size: 18px; vertical-align: text-bottom; margin-right: 6px;">logout</span>
                        Tạo Phiếu Xuất
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    const eSearchInput = document.getElementById('exportSearchInput');
    const eSearchResults = document.getElementById('exportSearchResults');
    const eTableBody = document.getElementById('exportProductTableBody');
    const eEmptyRow = document.getElementById('exportEmptyRow');
    const eSubmitBtn = document.getElementById('exportSubmitBtn');
    let eSearchTimeout;
    let eSearchActive = false;

    eSearchInput.addEventListener('focus', function() {
        eSearchActive = true;
        triggerExportSearch(this.value.trim());
    });

    eSearchInput.addEventListener('input', function() {
        eSearchActive = true;
        clearTimeout(eSearchTimeout);
        const keyword = this.value.trim();
        eSearchTimeout = setTimeout(() => {
            triggerExportSearch(keyword);
        }, 300);
    });

    function triggerExportSearch(keyword) {
        let url = '${pageContext.request.contextPath}/inventory?action=searchAllProductsApi&keyword=' + encodeURIComponent(keyword);
        
        fetch(url)
            .then(res => res.json())
            .then(data => {
                eSearchResults.innerHTML = '';
                if(data.length === 0) {
                    eSearchResults.innerHTML = '<div class="p-3 text-center text-muted small">Không tìm thấy sản phẩm nào</div>';
                } else {
                    data.forEach(p => {
                        const item = document.createElement('div');
                        item.className = 'import-search-item';
                        item.style.cursor = 'pointer';
                        
                        item.innerHTML = `
                            <div>
                                <div class="fw-bold text-dark" style="font-size: 14.5px;">\${p.productName}</div>
                                <div class="text-muted small">Mã: SP\${p.productId}</div>
                            </div>
                            <button type="button" class="btn-add-import i-add-btn">
                                <span class="material-icons" style="font-size: 16px;">add</span> Thêm
                            </button>
                        `;

                        item.onclick = () => {
                            addExportRow(p);
                            eSearchActive = false;
                            eSearchResults.style.display = 'none';
                            eSearchInput.value = '';
                            eSearchInput.focus();
                        };

                        eSearchResults.appendChild(item);
                    });
                }
                if (eSearchActive) {
                    eSearchResults.style.display = 'block';
                }
            });
    }

    document.addEventListener('click', function(e) {
        if (!eSearchInput.contains(e.target) && !eSearchResults.contains(e.target)) {
            eSearchActive = false;
            eSearchResults.style.display = 'none';
        }
    });

    function addExportRow(product) {
        let isDuplicate = false;
        eTableBody.querySelectorAll('tr').forEach(tr => {
            if (tr.id === 'exportEmptyRow') return;
            const pidInput = tr.querySelector('input[name="productId[]"]');
            if (pidInput && pidInput.value == product.productId) {
                isDuplicate = true;
                tr.style.backgroundColor = '#fef3c7';
                setTimeout(() => tr.style.backgroundColor = '', 600);
            }
        });

        if (isDuplicate) return;
        if (eEmptyRow) eEmptyRow.style.display = 'none';
        eSubmitBtn.disabled = false;

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="ps-3 py-3">
                <div class="fw-bold text-dark">\${product.productName}</div>
                <div class="text-muted small" style="font-size: 11px;">Mã SP: \${product.productId}</div>
                <input type="hidden" name="productId[]" value="\${product.productId}">
            </td>
            <td>
                <input type="number" name="importPrice[]" class="form-control form-control-sm text-end fw-bold" placeholder="0" min="0" step="1000" style="border-radius: 8px;">
            </td>
            <td>
                <input type="number" name="quantity[]" class="form-control form-control-sm text-center fw-bold e-qty-input" required value="1" min="1" style="width: 80px; margin: 0 auto; border-radius: 8px;">
            </td>
            <td class="text-center">
                <button type="button" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-1" onclick="this.closest('tr').remove(); checkExportEmpty();" title="Xóa khỏi phiếu" style="width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center;">
                    <span class="material-icons" style="font-size: 18px;">delete</span>
                </button>
            </td>
        `;
        
        eTableBody.appendChild(tr);
    }

    function checkExportEmpty() {
        if (eTableBody.querySelectorAll('tr').length === 1 && eTableBody.querySelector('#exportEmptyRow')) {
            eEmptyRow.style.display = 'table-row';
            eSubmitBtn.disabled = true;
        } else if (eTableBody.querySelectorAll('tr').length === 0) {
            eSubmitBtn.disabled = true;
        }
    }
</script>

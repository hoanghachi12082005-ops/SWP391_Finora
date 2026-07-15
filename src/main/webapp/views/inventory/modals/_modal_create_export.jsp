<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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

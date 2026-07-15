<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<link href="${pageContext.request.contextPath}/assets/css/inventory/inventory-modals.css?v=<%= System.currentTimeMillis() %>" rel="stylesheet">

<!-- Modal Nhập Hàng -->
<div class="modal fade" id="importStockModal" tabindex="-1" aria-labelledby="importStockModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/inventory" method="POST" id="importStockForm">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="action" value="saveImport">
                <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                
                <div class="modal-header border-bottom-0 pb-0 flex-column align-items-start">
                    <div class="d-flex w-100 justify-content-between align-items-center">
                        <h5 class="modal-title fw-bold" id="importStockModalLabel" style="color: #111827;">Nhập Hàng Từ Nhà Cung Cấp</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="small text-muted mt-1">Đến: <strong>Kho hiện tại</strong></div>
                    <!-- Tabs -->
                    <ul class="nav nav-tabs mt-3 w-100 border-bottom-0" id="importModeTabs" role="tablist" style="gap: 4px;">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link active px-3 py-2" id="manual-tab" data-bs-toggle="tab" data-bs-target="#manualImportPane" type="button" role="tab" aria-controls="manualImportPane" aria-selected="true" style="font-size: 14px; font-weight: 600; border-radius: 8px 8px 0 0;">
                                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">edit_note</span>
                                Nhập tay
                            </button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link px-3 py-2" id="excel-tab" data-bs-toggle="tab" data-bs-target="#excelImportPane" type="button" role="tab" aria-controls="excelImportPane" aria-selected="false" style="font-size: 14px; font-weight: 600; border-radius: 8px 8px 0 0;">
                                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">upload_file</span>
                                Nhập từ Excel
                            </button>
                        </li>
                    </ul>
                </div>
                
                <div class="modal-body pt-0">
                <div class="tab-content" id="importModeTabContent">
                <!-- ===== TAB 1: NHẬP TAY (existing, untouched) ===== -->
                <div class="tab-pane fade show active" id="manualImportPane" role="tabpanel" aria-labelledby="manual-tab">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Tìm Sản Phẩm Nhập</label>
                        <div class="import-search-box">
                            <span class="material-icons import-search-icon">search</span>
                            <input type="text" id="importSearchInput" class="import-search-input" placeholder="Gõ tên hoặc mã sản phẩm..." autocomplete="off">
                            <div class="import-search-results" id="importSearchResults"></div>
                        </div>
                    </div>
                    
                    <div class="table-responsive" style="max-height: 300px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 8px;">
                        <table class="table table-sm align-middle mb-0" id="importProductTable">
                            <thead style="background: #f8fafc; position: sticky; top: 0; z-index: 1;">
                                <tr>
                                    <th class="ps-3 py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="30%">Sản Phẩm</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="40%">Nhà Cung Cấp</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="18%">Giá Nhập</th>
                                    <th class="py-2 text-muted text-center" width="8%" style="font-weight: 600; font-size: 13px;">Số Lượng</th>
                                    <th class="py-2 text-center text-muted" width="4%" style="font-weight: 600; font-size: 13px;">Xóa</th>
                                </tr>
                            </thead>
                            <tbody id="importProductTableBody">
                                <tr id="importEmptyRow">
                                    <td colspan="5">
                                        <div class="text-center text-muted py-4">
                                            <span class="material-icons mb-2" style="font-size: 32px; color: #cbd5e1;">inventory</span>
                                            <p class="mb-0 small">Chưa có sản phẩm nào.<br>Tìm và chọn ở trên để thêm vào phiếu.</p>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    
                    <div class="mb-3 mt-4">
                        <label class="form-label fw-semibold text-muted small">Ghi chú (Tùy chọn)</label>
                        <textarea name="note" class="form-control" rows="2" placeholder="Ghi chú nhập hàng..." style="border-radius: 8px;"></textarea>
                    </div>
                </div><!-- end manualImportPane -->
                
                <!-- ===== TAB 2: NHẬP TỪ EXCEL (new) ===== -->
                <div class="tab-pane fade" id="excelImportPane" role="tabpanel" aria-labelledby="excel-tab">
                    <div class="mb-3 pt-3">
                        <div class="d-flex align-items-center gap-2 mb-3">
                            <button type="button" class="btn btn-outline-success btn-sm" onclick="downloadExcelTemplate()" style="border-radius: 8px; font-weight: 600;">
                                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">download</span>
                                Tải file mẫu
                            </button>
                            <button type="button" class="btn btn-primary btn-sm" onclick="triggerExcelImportUpload()" style="border-radius: 8px; font-weight: 600;">
                                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">upload_file</span>
                                Chọn file Excel
                            </button>
                            <input type="file" id="excelImportFileInput" accept=".xlsx,.xls" style="display:none;" onchange="handleExcelUpload(event)">
                            <span id="excelFileName" class="text-muted small" style="font-style: italic;"></span>
                        </div>
                        <div class="alert alert-info small py-2 px-3 mb-3" style="border-radius: 8px; font-size: 12.5px;">
                            <span class="material-icons" style="font-size: 14px; vertical-align: text-bottom; margin-right: 4px;">info</span>
                            File Excel cần có 4 cột: <strong>Tên sản phẩm</strong>, <strong>Mã NCC (Supplier ID)</strong>, <strong>Tên NCC (không cần điền)</strong>, <strong>Số lượng</strong>. Cột Tên NCC chỉ để tham khảo, hệ thống sẽ bỏ qua, và hiển thị số lượng hàng = số lượng nhà cc để hiện đầy đủ ncc để người khác tham khảo.
                        </div>
                    </div>

                    <div class="table-responsive" style="max-height: 300px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 8px;">
                        <table class="table table-sm align-middle mb-0" id="excelImportProductTable">
                            <thead style="background: #f8fafc; position: sticky; top: 0; z-index: 1;">
                                <tr>
                                    <th class="ps-3 py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="30%">Sản Phẩm</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="40%">Nhà Cung Cấp</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="18%">Giá Nhập</th>
                                    <th class="py-2 text-muted text-center" width="8%" style="font-weight: 600; font-size: 13px;">Số Lượng</th>
                                    <th class="py-2 text-center text-muted" width="4%" style="font-weight: 600; font-size: 13px;">Xóa</th>
                                </tr>
                            </thead>
                            <tbody id="excelImportProductTableBody">
                                <tr id="excelImportEmptyRow">
                                    <td colspan="5">
                                        <div class="text-center text-muted py-4">
                                            <span class="material-icons mb-2" style="font-size: 32px; color: #cbd5e1;">cloud_upload</span>
                                            <p class="mb-0 small">Chưa có dữ liệu.<br>Tải lên file Excel để bắt đầu.</p>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="mb-3 mt-4">
                        <label class="form-label fw-semibold text-muted small">Ghi chú (Tùy chọn)</label>
                        <textarea name="excelNote" class="form-control" rows="2" placeholder="Ghi chú nhập hàng từ Excel..." style="border-radius: 8px;" id="excelImportNote"></textarea>
                    </div>
                </div><!-- end excelImportPane -->

                </div><!-- end tab-content -->
                </div><!-- end modal-body -->
                
                <div class="modal-footer border-top-0 pt-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: 8px; font-weight: 500;">Hủy</button>
                    <button type="submit" class="btn btn-import-submit" id="importSubmitBtn" disabled>
                        <span class="material-icons" style="font-size: 18px; vertical-align: text-bottom; margin-right: 6px;">local_shipping</span>
                        Nhập hàng
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<select id="activeSuppliersSelect" style="display:none;">
    <c:forEach var="s" items="${suppliers}">
        <option value="${s.supplierID}"><c:out value="${s.name}"/></option>
    </c:forEach>
</select>
<script>
    (function() {
        window.ACTIVE_SUPPLIERS = [];
        const selectEl = document.getElementById('activeSuppliersSelect');
        if (selectEl) {
            Array.from(selectEl.options).forEach(opt => {
                window.ACTIVE_SUPPLIERS.push({
                    supplierId: parseInt(opt.value),
                    supplierName: opt.text,
                    importPrice: 0
                });
            });
        }
    })();
</script>

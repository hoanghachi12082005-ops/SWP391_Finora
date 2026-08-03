package controller.inventory;

// [MOVED FROM InventoryController] - Stock-related endpoints extracted for maintainability
import dao.inventory.InventoryDAO;
import dao.inventory.WarehouseDAO;
import dao.sales.OrderDAO;
import dao.supplier.SupplierDAO;
import model.Employee;
import model.Inventory;
import model.Supplier;
import model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import util.validation.InventoryValidator;
import util.validation.ValidationResult;

@WebServlet(name = "StockController", urlPatterns = {"/inventory-stock"})
public class StockController extends InventoryBaseController {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    /**
     * Xử lý yêu cầu GET: Trả về các thông tin dạng JSON API phục vụ giao diện Client 
     * (Lấy số lượng tồn kho sản phẩm, Tìm kiếm sản phẩm nhập kho, Lấy dữ liệu mẫu Excel).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            // [MOVED FROM InventoryController] - Original lines 140-150
            if ("getProductStockApi".equals(action)) {
                int pId = Integer.parseInt(request.getParameter("productId"));
                int wId = Integer.parseInt(request.getParameter("warehouseId"));
                int stock = 0;
                try (java.sql.Connection conn = util.database.DBContext.getConnection()) {
                    stock = inventoryDAO.getStockInTransaction(conn, pId, wId);
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
                sendJsonResponse(response, "{\"stock\":" + stock + "}");
                return;
            // [MOVED FROM InventoryController] - Original lines 151-153
            } else if ("searchImportProductsApi".equals(action)) {
                handleSearchImportProductsApi(request, response);
                return;
            // [MOVED FROM InventoryController] - Original lines 154-156
            } else if ("getImportTemplateDataApi".equals(action)) {
                handleGetImportTemplateDataApi(request, response);
                return;
            } else if ("exportStockExcel".equals(action) || "exportExcel".equals(action)) {
                handleExportStockExcel(request, response);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Xử lý yêu cầu POST: Thực hiện các thay đổi về dữ liệu tồn kho 
     * (Kiểm tra Excel nhập kho, Lưu phiếu nhập kho, Lưu phiếu xuất kho, Cập nhật tồn kho trực tiếp).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            doGet(request, response);
            return;
        }

        // [MOVED FROM InventoryController] - Original error handling pattern from lines 1119, 1955-1961
        try {
            switch (action) {
                // [MOVED FROM InventoryController] - Original lines 1121-1285
                case "checkImportExcel": {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    String data = request.getParameter("data");
                    int warehouseId = Integer.parseInt(request.getParameter("warehouseId"));
                    dao.inventory.InventoryDAO dao = new dao.inventory.InventoryDAO();

                    List<String> errors = new ArrayList<>();
                    List<String> rowJsons = new ArrayList<>();

                    if (data != null && !data.trim().isEmpty()) {
                        String[] lines = data.split("\\|");

                        // Check for blank lines between data lines
                        int firstLineIdx = -1;
                        int lastLineIdx = -1;
                        for (int i = 0; i < lines.length; i++) {
                            if (!lines[i].trim().isEmpty()) {
                                if (firstLineIdx == -1) firstLineIdx = i;
                                lastLineIdx = i;
                            }
                        }
                        if (firstLineIdx != -1 && lastLineIdx != -1) {
                            for (int i = firstLineIdx; i <= lastLineIdx; i++) {
                                if (lines[i].trim().isEmpty()) {
                                    int excelRow = i + 2;
                                    errors.add("Dòng " + excelRow + ": Tệp Excel có dòng trống ở giữa.");
                                }
                            }
                        }

                        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
                            String line = lines[lineIdx].trim();
                            if (line.isEmpty()) continue;
                            String[] parts = line.split("\\t", 3);
                            int excelRow = lineIdx + 2; // Excel row number (header=1, data starts at 2)

                            if (parts.length < 3) {
                                errors.add("Dòng " + excelRow + ": Thiếu dữ liệu (cần Tên sản phẩm, Mã NCC, Số lượng).");
                                continue;
                            }

                            String productName = parts[0].trim();
                            String supplierIdStr = parts[1].trim();
                            String quantityStr = parts[2].trim();

                            // Product name empty → skip (can't recover)
                            if (productName.isEmpty()) {
                                errors.add("Dòng " + excelRow + ": Tên sản phẩm trống.");
                                continue;
                            }

                            // Lookup product by exact name first
                            dto.inventory.ImportProductDTO product = dao.getImportProductByName(warehouseId, productName);
                            if (product == null) {
                                errors.add("Dòng " + excelRow + ": Không tìm thấy sản phẩm '" + productName + "'.");
                                continue;
                            }

                            // --- From here, product exists → row ALWAYS goes into table ---
                            boolean isErrorRow = false;
                            List<String> rowErrors = new ArrayList<>();

                            // Validate supplierId
                            int supplierId = -1;
                            boolean supplierIdValid = false;
                            try {
                                supplierId = Integer.parseInt(supplierIdStr);
                                if (supplierId <= 0) throw new NumberFormatException();
                                supplierIdValid = true;
                            } catch (NumberFormatException e) {
                                isErrorRow = true;
                                rowErrors.add("Mã NCC '" + supplierIdStr + "' không hợp lệ");
                            }

                            // Validate quantity
                            int quantity = 1;
                            boolean quantityValid = false;
                            try {
                                quantity = Integer.parseInt(quantityStr);
                                if (quantity <= 0) throw new NumberFormatException();
                                quantityValid = true;
                            } catch (NumberFormatException e) {
                                isErrorRow = true;
                                quantity = 1; // default
                                rowErrors.add("Số lượng '" + quantityStr + "' không hợp lệ");
                            }

                            // Check supplier linkage
                            dto.inventory.ImportProductDTO.SupplierInfo matchedSupplier = null;
                            boolean supplierLinked = false;
                            if (supplierIdValid) {
                                boolean supplierExists = false;
                                for (dto.inventory.ImportProductDTO.SupplierInfo si : product.getSuppliers()) {
                                    if (si.getSupplierId() == supplierId) {
                                        supplierExists = true;
                                        matchedSupplier = si;
                                        if (si.getImportPrice() != null && si.getImportPrice().doubleValue() > 0) {
                                            supplierLinked = true;
                                        }
                                        break;
                                    }
                                }
                                if (!supplierExists) {
                                    isErrorRow = true;
                                    String sName = dao.getSupplierName(supplierId);
                                    if (sName == null) {
                                        rowErrors.add("Mã NCC " + supplierId + " không tồn tại");
                                    } else {
                                        rowErrors.add("NCC '" + sName + "' (ID:" + supplierId + ") không hoạt động");
                                    }
                                } else if (!supplierLinked) {
                                    isErrorRow = true;
                                    rowErrors.add("Nhà cung cấp không có sản phẩm.");
                                }
                            }

                            // Use first supplier as fallback if supplier ID invalid or not found
                            if (matchedSupplier == null && !product.getSuppliers().isEmpty()) {
                                matchedSupplier = product.getSuppliers().get(0);
                            }

                            double price = (matchedSupplier != null && matchedSupplier.getImportPrice() != null && supplierLinked) 
                                ? matchedSupplier.getImportPrice().doubleValue() : 0;
                            String rowErrorMsg = String.join("; ", rowErrors);

                            // Build row JSON
                            StringBuilder rowJson = new StringBuilder("{");
                            rowJson.append("\"product\":{");
                            rowJson.append("\"productId\":").append(product.getProductId()).append(",");
                            rowJson.append("\"productName\":\"").append(escapeJson(product.getProductName())).append("\",");
                            rowJson.append("\"myStock\":").append(product.getMyStock());
                            rowJson.append("},");
                            rowJson.append("\"supplier\":{");
                            if (matchedSupplier != null) {
                                rowJson.append("\"supplierId\":").append(matchedSupplier.getSupplierId()).append(",");
                                rowJson.append("\"supplierName\":\"").append(escapeJson(matchedSupplier.getSupplierName())).append("\",");
                                rowJson.append("\"importPrice\":").append(price);
                            } else {
                                rowJson.append("\"supplierId\":0,\"supplierName\":\"\",\"importPrice\":0");
                            }
                            rowJson.append("},");
                            // All active suppliers for the dropdown
                            rowJson.append("\"allSuppliers\":[");
                            for (int s = 0; s < product.getSuppliers().size(); s++) {
                                dto.inventory.ImportProductDTO.SupplierInfo si = product.getSuppliers().get(s);
                                rowJson.append("{\"supplierId\":").append(si.getSupplierId());
                                rowJson.append(",\"supplierName\":\"").append(escapeJson(si.getSupplierName())).append("\"");
                                rowJson.append(",\"importPrice\":").append(si.getImportPrice() != null ? si.getImportPrice() : 0);
                                rowJson.append("}");
                                if (s < product.getSuppliers().size() - 1) rowJson.append(",");
                            }
                            rowJson.append("],");
                            rowJson.append("\"price\":").append(price).append(",");
                            rowJson.append("\"quantity\":").append(quantity).append(",");
                            rowJson.append("\"isErrorRow\":").append(isErrorRow).append(",");
                            rowJson.append("\"rowError\":\"").append(escapeJson(rowErrorMsg)).append("\"");
                            rowJson.append("}");

                            rowJsons.add(rowJson.toString());
                        }
                    }

                    // Build final response JSON
                    StringBuilder result = new StringBuilder("{");
                    result.append("\"success\":true,");
                    result.append("\"errors\":[");
                    for (int i = 0; i < errors.size(); i++) {
                        result.append("\"").append(escapeJson(errors.get(i))).append("\"");
                        if (i < errors.size() - 1) result.append(",");
                    }
                    result.append("],");
                    result.append("\"rows\":[");
                    for (int i = 0; i < rowJsons.size(); i++) {
                        result.append(rowJsons.get(i));
                        if (i < rowJsons.size() - 1) result.append(",");
                    }
                    result.append("]");
                    result.append("}");

                    response.getWriter().write(result.toString());
                    return;
                }
                // [MOVED FROM InventoryController] - Original lines 1373-1469
                case "saveImport": {
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    String note = request.getParameter("note");
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] quantities = request.getParameterValues("quantity[]");
                    String[] supplierIds = request.getParameterValues("supplierId[]");
                    String[] importPrices = request.getParameterValues("importPrice[]");
                    
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    boolean isOwner = "Owner".equalsIgnoreCase(currentUser.getRoleName()) || "Admin".equalsIgnoreCase(currentUser.getRoleName()) || "StoreManager".equalsIgnoreCase(currentUser.getRoleName());
                    
                    // Kiểm tra dung lượng yêu cầu gửi lên (Request Payload) phải dưới 5MB
                    if (request.getContentLengthLong() > 5 * 1024 * 1024) {
                        request.getSession().setAttribute("errorMessage", "Dung lượng dữ liệu gửi lên quá lớn (vượt quá 5MB).");
                        redirect(response, request.getContextPath() + "/inventory?tab=stock&warehouseId=" + currentWarehouseId);
                        break;
                    }

                    // Tầng Backend Validation
                    ValidationResult valResult = InventoryValidator.validateImportRequest(
                        currentWarehouseId, productIds, quantities, supplierIds, importPrices, note
                    );

                    if (!valResult.isValid()) {
                        request.getSession().setAttribute("errorMessage", valResult.getFirstError());
                        redirect(response, request.getContextPath() + "/inventory?tab=stock&warehouseId=" + currentWarehouseId);
                        break;
                    }
                    
                    if (productIds != null && productIds.length > 0) {
                        List<model.OrderDetail> allDetails = new ArrayList<>();
                        double totalCost = 0.0;
                        
                        try {
                            for (int i = 0; i < productIds.length; i++) {                           
                                int pId = Integer.parseInt(productIds[i].trim());
                                int qty = Integer.parseInt(quantities[i].trim());
                                int sId = Integer.parseInt(supplierIds[i].trim());
                                double price = 0.0;
                                if (importPrices != null && importPrices.length > i) {
                                    price = Double.parseDouble(importPrices[i].trim());
                                }
                                
                                model.OrderDetail detail = new model.OrderDetail();
                                detail.setProductId(pId);
                                detail.setQuantity(qty);
                                detail.setUnitPrice(price);
                                detail.setTotalPrice(qty * price);
                                detail.setImportPrice(price);
                                detail.setSupplierId(sId);
                                detail.setSupplierStatus("PENDING");
                                allDetails.add(detail);
                                totalCost += qty * price;
                            }
                        } catch (Exception ex) {
                            request.getSession().setAttribute("errorMessage", "Dữ liệu nhập hàng không hợp lệ. Vui lòng kiểm tra lại số lượng hoặc thông tin sản phẩm!");
                            redirect(response, request.getContextPath() + "/inventory?tab=stock&warehouseId=" + currentWarehouseId);
                            break;
                        }
                        
                        // Tạo DUY NHẤT 1 Order cho tất cả NCC
                        model.Order purchaseOrder = new model.Order();
                        purchaseOrder.setOrderCode("PO-" + System.currentTimeMillis());
                        purchaseOrder.setOrderType("PURCHASE");
                        purchaseOrder.setSupplierId(null); // Nhiều NCC -> không gán 1 NCC cụ thể
                        purchaseOrder.setEmpId(currentUser.getEmployeeId());
                        purchaseOrder.setBranchId(currentUser.getBranchId() != null ? currentUser.getBranchId() : 1);
                        purchaseOrder.setWarehouseId(currentWarehouseId);
                        purchaseOrder.setSubtotal(totalCost);
                        purchaseOrder.setDiscountAmount(0.0);
                        purchaseOrder.setTotalAmount(totalCost);
                        purchaseOrder.setPaymentMethod("BANK_TRANSFER");
                        // Owner tạo -> PENDING (chờ NCC xác nhận), Staff tạo -> PENDING (chờ Owner duyệt)
                        purchaseOrder.setStatus(model.Order.OrderStatus.PENDING);
                        
                        try (java.sql.Connection conn = util.database.DBContext.getConnection()) {
                            conn.setAutoCommit(false);
                            try {
                                int orderId = orderDAO.createOrderInTransaction(conn, purchaseOrder);
                                dao.sales.OrderDetailDAO detailDao = new dao.sales.OrderDetailDAO();
                                detailDao.insertBatchPurchase(conn, orderId, allDetails);
                                
                                // Owner tạo -> Tự động duyệt và chuyển sang IN_TRANSIT (Chờ kiểm tra xác nhận nhập kho thực tế)
                                if (isOwner) {
                                    orderDAO.updateStatus(conn, orderId, "IN_TRANSIT", currentUser.getEmployeeId());
                                }
                                
                                conn.commit();
                            } catch (Exception ex) {
                                conn.rollback();
                                throw ex;
                            }
                        }
                    }

                    if (isOwner) {
                        request.getSession().setAttribute("message", "Đã tạo đơn nhập hàng (Đã duyệt, chuyển sang Chờ Xác Nhận Nhập Kho).");
                    } else {
                        request.getSession().setAttribute("message", "Tạo yêu cầu nhập hàng thành công! Đã gửi cho quản lý duyệt.");
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + currentWarehouseId);
                    break;
                }
                // [MOVED FROM InventoryController] - Original lines 1871-1885
                case "updateStockDirectly": {
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"Admin".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    int productId = Integer.parseInt(request.getParameter("productId"));
                    int warehouseId = Integer.parseInt(request.getParameter("warehouseId"));
                    int newQty = Integer.parseInt(request.getParameter("quantity"));

                    inventoryDAO.updateStockQty(warehouseId, productId, newQty);
                    request.getSession().setAttribute("message", "Cập nhật số lượng tồn kho thành công!");
                    redirect(response, request.getContextPath() + "/inventory?tab=stock&warehouseId=" + warehouseId);
                    break;
                }
                default:
                    doGet(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            request.getSession().setAttribute("error", "Lỗi: " + e.getMessage() + " | StackTrace: " + sw.toString().substring(0, Math.min(200, sw.toString().length())));
            redirect(response, request.getContextPath() + "/inventory");
        }
    }

    /**
     * Chuẩn bị dữ liệu hiển thị cho Tab "Tồn kho" (Stock List).
     * Lấy danh sách sản phẩm tồn kho kèm phân trang, từ khóa tìm kiếm, bộ lọc trạng thái (Hết hàng, Sắp hết hàng).
     */
    void handleStockTab(HttpServletRequest request, Integer warehouseId) throws Exception {
        if (warehouseId == null) {
            // Dashboard mode: don't query products, skip to save DB calls.
            return;
        }

        String keyword = request.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }
        String status = request.getParameter("status");
        String sortParam = request.getParameter("sort");
        
        int page = 1;
        int limit = 20;
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        int offset = (page - 1) * limit;

        List<Inventory> stockList = inventoryDAO.findAll(offset, limit, keyword, status, null, null, warehouseId, sortParam);
        attachImageUrls(request, stockList);
        int totalCount = inventoryDAO.getTotalCount(keyword, status, null, null, warehouseId);
        int totalPages = (int) Math.ceil((double) totalCount / limit);

        request.setAttribute("stockList", stockList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("keyword", keyword);
        request.setAttribute("statusFilter", status);
        request.setAttribute("sortParam", sortParam);
        
        // Setup KPI Summary manually for simplicity
        request.setAttribute("totalItems", totalCount);
        int lowStockCount = inventoryDAO.getTotalCount(keyword, "LOW_STOCK", null, null, warehouseId);
        request.setAttribute("lowStockCount", lowStockCount);

        // Fetch Suppliers for Import Modal
        List<Supplier> suppliers = supplierDAO.getSuppliersPaging("", "active", 1, 1000);
        request.setAttribute("suppliers", suppliers);
    }

    /**
     * API Tìm kiếm sản phẩm phục vụ việc chọn sản phẩm đưa vào phiếu Nhập kho.
     * Trả về danh sách sản phẩm khớp từ khóa kèm thông tin các Nhà cung cấp của sản phẩm đó dưới dạng JSON.
     */
    private void handleSearchImportProductsApi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }
        
        int warehouseId = Integer.parseInt(request.getParameter("warehouseId"));
        
        dao.inventory.InventoryDAO dao = new dao.inventory.InventoryDAO();
        List<dto.inventory.ImportProductDTO> list = dao.searchImportProducts(warehouseId, keyword);
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            dto.inventory.ImportProductDTO p = list.get(i);
            json.append("{");
            json.append("\"productId\":").append(p.getProductId()).append(",");
            json.append("\"productName\":\"").append(escapeJson(p.getProductName())).append("\",");
            json.append("\"myStock\":").append(p.getMyStock()).append(",");
            json.append("\"suppliers\":[");
            if (p.getSuppliers() != null) {
                for (int j = 0; j < p.getSuppliers().size(); j++) {
                    dto.inventory.ImportProductDTO.SupplierInfo s = p.getSuppliers().get(j);
                    json.append("{");
                    json.append("\"supplierId\":").append(s.getSupplierId()).append(",");
                    json.append("\"supplierName\":\"").append(escapeJson(s.getSupplierName())).append("\",");
                    json.append("\"importPrice\":").append(s.getImportPrice() != null ? s.getImportPrice() : 0);
                    json.append("}");
                    if (j < p.getSuppliers().size() - 1) json.append(",");
                }
            }
            json.append("]");
            json.append("}");
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }

    /**
     * API Lấy dữ liệu sản phẩm và nhà cung cấp để tải về file Excel mẫu nhập kho.
     * Trả về danh sách gồm [Tên sản phẩm, ID nhà cung cấp, Tên nhà cung cấp] dưới dạng JSON Array.
     */
    private void handleGetImportTemplateDataApi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        dao.inventory.InventoryDAO dao = new dao.inventory.InventoryDAO();
        List<String[]> rows = dao.getAllProductsWithSuppliers();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            json.append("[\"").append(escapeJson(r[0])).append("\",");
            json.append(r[1]).append(",");
            json.append("\"").append(escapeJson(r[2])).append("\"]");
            if (i < rows.size() - 1) json.append(",");
        }
        json.append("]");
        response.getWriter().write(json.toString());
    }

    /**
     * Xuất báo cáo tồn kho hàng hóa thành tệp Excel (.xlsx) dựa trên bộ lọc hiện tại.
     */
    void handleExportStockExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String keyword = request.getParameter("keyword");
            if (keyword != null) {
                keyword = keyword.trim().replaceAll("\\s+", " ");
            }
            String status = request.getParameter("status");
            String sortParam = request.getParameter("sort");

            Integer warehouseId = null;
            String wIdStr = request.getParameter("warehouseId");
            if (wIdStr != null && !wIdStr.trim().isEmpty() && !"null".equalsIgnoreCase(wIdStr.trim())) {
                try {
                    warehouseId = Integer.parseInt(wIdStr.trim());
                } catch (NumberFormatException ignored) {}
            }
            if (warehouseId == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    warehouseId = (Integer) session.getAttribute("selectedWarehouseId");
                }
            }

            String warehouseName = "Tất cả kho";
            if (warehouseId != null) {
                dao.inventory.WarehouseDAO wDao = new dao.inventory.WarehouseDAO();
                List<model.Warehouse> allW = wDao.findAll();
                if (allW != null) {
                    final int targetWId = warehouseId;
                    model.Warehouse w = allW.stream().filter(item -> item.getWarehouseId() == targetWId).findFirst().orElse(null);
                    if (w != null) {
                        warehouseName = w.getWarehouseName();
                    }
                }
            }

            List<Inventory> stockList = inventoryDAO.findAll(0, 1000000, keyword, status, null, null, warehouseId, sortParam);

            String generatedBy = "Unknown";
            HttpSession session = request.getSession(false);
            if (session != null) {
                Employee currentUser = (Employee) session.getAttribute("currentUser");
                if (currentUser != null) generatedBy = currentUser.getFullName();
            }

            byte[] excelBytes = util.report.ExcelExportUtil.generateStockInventoryReport(
                    generatedBy, stockList, warehouseName, keyword, status);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    util.report.ExportUtil.buildExportFileName("BaoCaoTonKho") + ".xlsx\"");
            response.setContentLength(excelBytes.length);
            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Lỗi xuất file Excel tồn kho: " + e.getMessage());
        }
    }
}

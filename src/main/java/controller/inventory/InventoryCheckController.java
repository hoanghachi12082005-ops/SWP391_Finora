package controller.inventory;

import dao.inventory.InventoryDAO;
import dao.inventory.InventoryCheckDAO;
import model.Employee;
import model.InventoryCheck;
import model.InventoryCheckDetail;
import service.inventory.InventoryExecutionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Controller xử lý các thao tác kiểm kho (Inventory Check).
 * Được tách từ InventoryController.java để dễ bảo trì.
 */
@WebServlet(name = "InventoryCheckController", urlPatterns = {"/inventory-check"})
public class InventoryCheckController extends InventoryBaseController {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Xử lý yêu cầu GET: Lấy thông tin về kiểm kho
     * (Tìm sản phẩm phục vụ kiểm kho, Xem chi tiết phiếu kiểm kho).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session == null || session.getAttribute("currentUser") == null) {
            redirect(response, request.getContextPath() + "/login");
            return;
        }

        Employee currentUser = (Employee) session.getAttribute("currentUser");
        String role = currentUser.getRoleName() != null ? currentUser.getRoleName().toLowerCase() : "";

        String action = request.getParameter("action");

        // Determine selectedWarehouseId from session
        Integer selectedWarehouseId = (Integer) session.getAttribute("selectedWarehouseId");

        try {
            // action=searchStockCheckProductsApi → [MOVED FROM InventoryController] - Original lines 213-215
            if ("searchStockCheckProductsApi".equals(action)) {
                handleSearchStockCheckProductsApi(request, response, selectedWarehouseId);
                return;
            }

            // action=viewCheckDetails → [MOVED FROM InventoryController] - Original lines 216-227
            if ("viewCheckDetails".equals(action)) {
                int checkId = Integer.parseInt(request.getParameter("checkId"));
                dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                model.InventoryCheck check = checkDAO.findById(checkId);
                List<model.InventoryCheckDetail> details = checkDAO.getCheckDetails(checkId);
                
                request.setAttribute("check", check);
                request.setAttribute("checkDetails", details);
                
                forward(request, response, "inventory/modals/_modal_check_details");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Xử lý yêu cầu POST: Thực hiện các thay đổi dữ liệu liên quan đến kiểm kho
     * (Lưu phiếu kiểm kho mới, Cập nhật phiếu kiểm, Duyệt phiếu kiểm, Hủy hoặc Từ chối phiếu...).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            doGet(request, response);
            return;
        }

        // [MOVED FROM InventoryController] - Original try/catch pattern from lines 1119, 1955-1961
        try {
            switch (action) {
                // case "saveCheck" → [MOVED FROM InventoryController] - Original lines 1752-1818
                case "saveCheck": {
                    if (request.getContentLengthLong() > 5 * 1024 * 1024) {
                        request.getSession().setAttribute("error", "Dung lượng dữ liệu gửi lên quá lớn (vượt quá 5MB).");
                        int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                        redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                        return;
                    }
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] systemQtys = request.getParameterValues("systemQty[]");
                    String[] actualQtys = request.getParameterValues("actualQty[]");
                    String[] notes = request.getParameterValues("note[]");

                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");

                    if (productIds != null && productIds.length > 0) {
                        if (actualQtys == null || actualQtys.length != productIds.length) {
                            request.getSession().setAttribute("error", "Lỗi: Dữ liệu kiểm kho không hợp lệ.");
                            redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                            return;
                        }

                        for (String qty : actualQtys) {
                            if (qty == null || qty.trim().isEmpty() || !qty.trim().matches("^\\d+$")) {
                                request.getSession().setAttribute("error", "Lỗi: Số lượng thực tế phải là số nguyên dương hợp lệ và không chứa ký tự khác.");
                                redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                                return;
                            }
                        }

                        List<model.InventoryCheckDetail> details = new ArrayList<>();
                        int totalDiscrepancy = 0;

                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int sysQty = Integer.parseInt(systemQtys[i]);
                            int actQty = Integer.parseInt(actualQtys[i]);
                            String note = (notes != null && notes.length > i) ? notes[i] : "";

                            int disc = actQty - sysQty;
                            totalDiscrepancy += Math.abs(disc);

                            model.InventoryCheckDetail d = new model.InventoryCheckDetail();
                            d.setProductId(pId);
                            d.setSystemQty(sysQty);
                            d.setActualQty(actQty);
                            d.setDiscrepancy(disc);
                            d.setNote(note);
                            details.add(d);
                        }

                        model.InventoryCheck check = new model.InventoryCheck();
                        check.setCheckCode("IC-" + System.currentTimeMillis());
                        check.setWarehouseId(currentWarehouseId);
                        check.setCreatedBy(currentUser.getEmployeeId());
                        
                        boolean isApprover = "Owner".equals(currentUser.getRoleName()) || "StoreManager".equals(currentUser.getRoleName());
                        check.setStatus(isApprover ? "APPROVED" : "PENDING");
                        check.setTotalDiscrepancy(totalDiscrepancy);

                        dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                        int checkId = checkDAO.createCheck(check, details);

                        if (isApprover) {
                            service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                            executionService.executeStockBalance(checkId, currentUser.getEmployeeId());
                            request.getSession().setAttribute("message", "Đã lập và duyệt phiếu kiểm kho, hoàn tất cân bằng tồn kho!");
                        } else {
                            request.getSession().setAttribute("message", "Đã lập phiếu kiểm kho (Chờ quản lý phê duyệt)!");
                        }
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                    break;
                }
                // case "updateCheck" → [MOVED FROM InventoryController] - Original lines 1886-1946
                case "updateCheck": {
                    if (request.getContentLengthLong() > 5 * 1024 * 1024) {
                        request.getSession().setAttribute("error", "Dung lượng dữ liệu gửi lên quá lớn (vượt quá 5MB).");
                        int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                        redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                        return;
                    }
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] systemQtys = request.getParameterValues("systemQty[]");
                    String[] actualQtys = request.getParameterValues("actualQty[]");
                    String[] notes = request.getParameterValues("note[]");

                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"Admin".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }

                    if (productIds != null && productIds.length > 0) {
                        if (actualQtys == null || actualQtys.length != productIds.length) {
                            request.getSession().setAttribute("error", "Lỗi: Dữ liệu kiểm kho không hợp lệ.");
                            redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                            return;
                        }

                        for (String qty : actualQtys) {
                            if (qty == null || qty.trim().isEmpty() || !qty.trim().matches("^\\d+$")) {
                                request.getSession().setAttribute("error", "Lỗi: Số lượng thực tế phải là số nguyên dương hợp lệ và không chứa ký tự khác.");
                                redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                                return;
                            }
                        }

                        List<model.InventoryCheckDetail> details = new ArrayList<>();
                        int totalDiscrepancy = 0;

                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int sysQty = Integer.parseInt(systemQtys[i]);
                            int actQty = Integer.parseInt(actualQtys[i]);
                            String note = (notes != null && notes.length > i) ? notes[i] : "";

                            int disc = actQty - sysQty;
                            totalDiscrepancy += Math.abs(disc);

                            model.InventoryCheckDetail d = new model.InventoryCheckDetail();
                            d.setProductId(pId);
                            d.setSystemQty(sysQty);
                            d.setActualQty(actQty);
                            d.setDiscrepancy(disc);
                            d.setNote(note);
                            details.add(d);
                        }

                        dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                        checkDAO.updateCheck(checkId, totalDiscrepancy, details);

                        service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                        executionService.executeStockBalance(checkId, currentUser.getEmployeeId());
                    }

                    request.getSession().setAttribute("message", "Đã cập nhật phiếu nhập kiểm kho và cân bằng tồn kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                    break;
                }
                // case "approveCheck" → [MOVED FROM InventoryController] - Original lines 1820-1837
                case "approveCheck": {
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                    executionService.approveInventoryCheck(checkId, currentUser.getEmployeeId());

                    request.getSession().setAttribute("message", "Đã phê duyệt phiếu kiểm kho và cân bằng tồn kho thành công!");
                    String redirectTab = request.getParameter("tab");
                    if ("check".equals(redirectTab)) {
                        redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + request.getParameter("warehouseId"));
                    } else {
                        redirect(response, request.getContextPath() + "/inventory?tab=history&subtab=voucher");
                    }
                    break;
                }
                // case "rejectCheck" → [MOVED FROM InventoryController] - Original lines 1839-1857
                case "rejectCheck": {
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                    checkDAO.updateStatus(checkId, "REJECTED", currentUser.getEmployeeId());

                    request.getSession().setAttribute("message", "Đã từ chối phiếu kiểm kho.");
                    String redirectTab = request.getParameter("tab");
                    if ("check".equals(redirectTab)) {
                        redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + request.getParameter("warehouseId"));
                    } else {
                        redirect(response, request.getContextPath() + "/inventory?tab=history&subtab=voucher");
                    }
                    break;
                }
                // case "cancelCheck" → [MOVED FROM InventoryController] - Original lines 1858-1870
                case "cancelCheck": {
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                    checkDAO.updateStatus(checkId, "CANCELLED", null);

                    request.getSession().setAttribute("message", "Đã hủy phiếu kiểm kho thành công.");
                    String redirectTab = request.getParameter("tab");
                    if (redirectTab == null || redirectTab.isEmpty()) {
                        redirectTab = "check";
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=" + redirectTab + "&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                default:
                    doGet(request, response);
                    break;
            }
        } catch (Exception e) {
            // [MOVED FROM InventoryController] - Original error handling lines 1955-1961
            e.printStackTrace();
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            request.getSession().setAttribute("error", "Lỗi: " + e.getMessage() + " | StackTrace: " + sw.toString().substring(0, Math.min(200, sw.toString().length())));
            redirect(response, request.getContextPath() + "/inventory");
        }
    }

    /**
     * Chuẩn bị dữ liệu hiển thị cho Tab "Kiểm kho" (Stocktaking List).
     * Lấy danh sách phiếu kiểm kho của kho hàng được chọn và áp dụng các bộ lọc tìm kiếm 
     * (mã phiếu, trạng thái phiếu, độ chênh lệch thừa/thiếu).
     */
    void handleCheckTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        String subtab = "inventory_check";
        request.setAttribute("activeSubtab", subtab);

        String checkCodeQuery = request.getParameter("checkCodeQuery");
        String statusQuery = request.getParameter("statusQuery");
        String discrepancyQuery = request.getParameter("discrepancyQuery");

        dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
        List<model.InventoryCheck> checks = checkDAO.findAllByWarehouseFiltered(
            warehouseId != null ? warehouseId : 0,
            checkCodeQuery,
            statusQuery,
            discrepancyQuery
        );
        request.setAttribute("checks", checks);
    }

    /**
     * API tìm kiếm sản phẩm cho kiểm kho.
     * [MOVED FROM InventoryController] - Original lines 2169-2202
     */
    private void handleSearchStockCheckProductsApi(HttpServletRequest request, HttpServletResponse response, Integer warehouseId) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (warehouseId == null) {
            response.getWriter().write("[]");
            return;
        }

        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }

        List<dto.inventory.StockCheckProductDTO> list = inventoryDAO.searchStockCheckProducts(warehouseId, keyword);
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            dto.inventory.StockCheckProductDTO dto = list.get(i);
            json.append("{");
            json.append("\"productId\":").append(dto.getProductId()).append(",");
            json.append("\"productName\":\"").append(escapeJson(dto.getProductName())).append("\",");
            json.append("\"systemStock\":").append(dto.getSystemStock()).append(",");
            json.append("\"productCodebar\":\"").append(escapeJson(dto.getProductCodebar() != null ? dto.getProductCodebar() : "")).append("\",");
            json.append("\"categoryName\":\"").append(escapeJson(dto.getCategoryName())).append("\"");
            json.append("}");
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }
}

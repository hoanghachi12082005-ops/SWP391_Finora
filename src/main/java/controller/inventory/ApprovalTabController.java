package controller.inventory;

import dao.inventory.StockTransferDAO;
import dao.inventory.StockTransactionDAO;
import dao.sales.OrderDAO;
import model.Employee;
import model.StockTransfer;
import model.Order;
import model.InventoryCheck;
import service.inventory.TransferService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.validation.InventoryValidator;
import util.validation.ValidationResult;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller xử lý các phê duyệt trong phần Kho hàng (Ví dụ: duyệt phiếu chuyển kho, duyệt phiếu xuất/nhập hàng).
 * Chỉ chấp nhận các quyền quản trị như Owner hoặc StoreManager.
 */
@WebServlet(name = "ApprovalTabController", urlPatterns = {"/inventory-approval"})
public class ApprovalTabController extends InventoryBaseController {

    private final StockTransferDAO transferDAO = new StockTransferDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Approval tab is loaded by InventoryController during tab rendering.
        // This controller has no standalone doGet endpoints.
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Xử lý yêu cầu POST duyệt hoặc từ chối phiếu chuyển kho, phiếu nhập/xuất kho.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            doGet(request, response);
            return;
        }

        Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
        ValidationResult permCheck = InventoryValidator.validateStaffApprovalPermission(currentUser, action);
        if (!permCheck.isValid()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, permCheck.getFirstError());
            return;
        }

        try {
            switch (action) {
                // [MOVED FROM InventoryController] - Original lines 1598-1612
                case "approveTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        new service.inventory.TransferService().approveMasterTransfer(st.getTransferCode(), currentUser.getEmployeeId());
                    }
                    request.getSession().setAttribute("message", "Đã duyệt phiếu tổng điều chuyển thành công. (Chờ các đối tác duyệt)");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                // [MOVED FROM InventoryController] - Original lines 1613-1627
                case "rejectTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        new service.inventory.TransferService().rejectMasterTransfer(st.getTransferCode(), currentUser.getEmployeeId());
                    }
                    request.getSession().setAttribute("message", "Đã từ chối phiếu điều chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                // [MOVED FROM InventoryController] - Original lines 1628-1651
                case "partnerApproveTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    int currentWarehouseId = 0;
                    String cwIdStr = request.getParameter("currentWarehouseId");
                    if (cwIdStr != null && !cwIdStr.trim().isEmpty()) {
                        try { currentWarehouseId = Integer.parseInt(cwIdStr); } catch (Exception e) {}
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        if (currentWarehouseId == 0) {
                            boolean fromIsCreator = (st.getCreatorBranchId() == st.getFromBranchId());
                            currentWarehouseId = fromIsCreator ? st.getToWarehouseId() : st.getFromWarehouseId();
                        }
                        new service.inventory.TransferService().partnerApprove(st.getTransferCode(), currentWarehouseId);
                    }
                    request.getSession().setAttribute("message", "Đối tác đã phê duyệt phiếu thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                // [MOVED FROM InventoryController] - Original lines 1652-1675
                case "partnerRejectTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    int currentWarehouseId = 0;
                    String cwIdStr = request.getParameter("currentWarehouseId");
                    if (cwIdStr != null && !cwIdStr.trim().isEmpty()) {
                        try { currentWarehouseId = Integer.parseInt(cwIdStr); } catch (Exception e) {}
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        if (currentWarehouseId == 0) {
                            boolean fromIsCreator = (st.getCreatorBranchId() == st.getFromBranchId());
                            currentWarehouseId = fromIsCreator ? st.getToWarehouseId() : st.getFromWarehouseId();
                        }
                        new service.inventory.TransferService().partnerReject(st.getTransferCode(), currentWarehouseId);
                    }
                    request.getSession().setAttribute("message", "Đối tác đã từ chối phiếu thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                // [MOVED FROM InventoryController] - Original lines 1676-1690
                case "partnerApproveTransferAll": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        new service.inventory.TransferService().partnerApproveAll(st.getTransferCode());
                    }
                    request.getSession().setAttribute("message", "Đối tác đã phê duyệt toàn bộ phiếu thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                // [MOVED FROM InventoryController] - Original lines 1691-1705
                case "partnerRejectTransferAll": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        new service.inventory.TransferService().partnerRejectAll(st.getTransferCode());
                    }
                    request.getSession().setAttribute("message", "Đối tác đã từ chối toàn bộ phiếu thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
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
     * Chuẩn bị dữ liệu hiển thị cho Tab "Phê duyệt" (Approval Tab) của quản lý.
     * Thu thập danh sách tất cả các phiếu cần phê duyệt (Chuyển kho chờ duyệt, Nhập/Xuất kho chờ duyệt, 
     * Kiểm kho chờ duyệt), sắp xếp theo ngày tạo mới nhất và gom nhóm dữ liệu hiển thị hợp nhất.
     */
    void handleApprovalTab(HttpServletRequest request, String role) throws Exception {
        boolean isAuthorized = "Owner".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role) || "StoreManager".equalsIgnoreCase(role);
        if (!isAuthorized) {
            request.setAttribute("unifiedApprovals", new ArrayList<>());
            if ("approval".equals(request.getParameter("tab"))) {
                request.setAttribute("error", "Bạn không có quyền truy cập tab này.");
            }
            return;
        }

        String transferCodeQuery = request.getParameter("transferCodeQuery");
        String fromWarehouseQueryStr = request.getParameter("fromWarehouseQuery");
        String toWarehouseQueryStr = request.getParameter("toWarehouseQuery");

        Integer fromWarehouseQuery = null;
        Integer toWarehouseQuery = null;
        try {
            if (fromWarehouseQueryStr != null && !fromWarehouseQueryStr.trim().isEmpty()) {
                fromWarehouseQuery = Integer.parseInt(fromWarehouseQueryStr);
            }
            if (toWarehouseQueryStr != null && !toWarehouseQueryStr.trim().isEmpty()) {
                toWarehouseQuery = Integer.parseInt(toWarehouseQueryStr);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        if (transferCodeQuery != null) {
            transferCodeQuery = transferCodeQuery.trim();
        }

        List<model.StockTransfer> pendingTransfers = transferDAO.findPendingTransfersFiltered(
            transferCodeQuery,
            fromWarehouseQuery,
            toWarehouseQuery
        );
        
        Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
        int branchId = currentUser != null && currentUser.getBranchId() != null ? currentUser.getBranchId() : 0;
        List<model.Order> pendingOrders = orderDAO.getPendingInventoryOrders(branchId);
        
        List<model.InventoryCheck> pendingChecks = null;
        try {
            dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
            pendingChecks = checkDAO.findAllByWarehouseFiltered(0, null, "PENDING", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Build unified list of pending approvals
        List<Map<String, Object>> unifiedApprovals = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        java.text.SimpleDateFormat inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (pendingTransfers != null) {
            Integer selectedWarehouseId = (Integer) request.getSession().getAttribute("selectedWarehouseId");
            boolean isSystemOwner = "Owner".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);
            
            // Group pendingTransfers by transfer_code
            Map<String, List<model.StockTransfer>> groupedByCode = new java.util.LinkedHashMap<>();
            for (model.StockTransfer item : pendingTransfers) {
                groupedByCode.computeIfAbsent(item.getTransferCode(), k -> new ArrayList<>()).add(item);
            }
            
            for (Map.Entry<String, List<model.StockTransfer>> entry : groupedByCode.entrySet()) {
                String transferCode = entry.getKey();
                List<model.StockTransfer> subList = entry.getValue();
                if (subList.isEmpty()) continue;
                
                model.StockTransfer representative = subList.get(0);
                String status = representative.getStatus();
                
                if (isSystemOwner) {
                    // Owner/Admin view: 1 row per transfer_code
                    java.util.Set<String> partners = new java.util.LinkedHashSet<>();
                    String creatorWName = "";
                    for (model.StockTransfer item : subList) {
                        boolean fromIsCreator = (item.getCreatorBranchId() == item.getFromBranchId());
                        if (fromIsCreator) {
                            creatorWName = item.getFromWarehouseName();
                            partners.add(item.getToWarehouseName());
                        } else {
                            creatorWName = item.getToWarehouseName();
                            partners.add(item.getFromWarehouseName());
                        }
                    }
                    
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", representative.getStockTransferId());
                    map.put("code", transferCode);
                    map.put("type", "TRANSFER");
                    map.put("typeLabel", "Điều chuyển");
                    map.put("createdBy", representative.getCreatedByName());
                    map.put("createdAt", representative.getTransferDate() != null ? sdf.format(representative.getTransferDate()) : "");
                    map.put("amount", null);
                    map.put("idParamName", "transferId");
                    map.put("detailCallback", "viewTicketDetails(" + representative.getStockTransferId() + ", true)");
                    map.put("rawDate", representative.getTransferDate() != null ? representative.getTransferDate() : new java.util.Date(0));
                    map.put("warehouseName", creatorWName + " ➔ " + String.join(", ", partners));
                    
                    if ("PENDING_OWNER".equals(status)) {
                        map.put("actionApprove", "approveTransfer");
                        map.put("actionReject", "rejectTransfer");
                        unifiedApprovals.add(map);
                    } else if ("PENDING_PARTNER".equals(status)) {
                        // Approving on behalf of partner warehouses must be done inside the ticket details modal,
                        // so we add it to the list without quick actions.
                        unifiedApprovals.add(map);
                    } else if ("PENDING_DISPATCH".equals(status)) {
                        map.put("actionApprove", "approveTransfer");
                        map.put("actionReject", "rejectTransfer");
                        unifiedApprovals.add(map);
                    }
                } else {
                    // StoreManager view: 1 row per partner relation involving selectedWarehouseId
                    if ("PENDING_OWNER".equals(status)) {
                        int creatorBranchId = representative.getCreatorBranchId();
                        int currentBranchId = 0;
                        if (selectedWarehouseId != null) {
                            for (model.StockTransfer item : subList) {
                                if (selectedWarehouseId == item.getFromWarehouseId()) {
                                    currentBranchId = item.getFromBranchId();
                                    break;
                                } else if (selectedWarehouseId == item.getToWarehouseId()) {
                                    currentBranchId = item.getToBranchId();
                                    break;
                                }
                            }
                        }
                        
                        if (selectedWarehouseId != null && currentBranchId == creatorBranchId) {
                            java.util.Set<String> partners = new java.util.LinkedHashSet<>();
                            String creatorWName = "";
                            for (model.StockTransfer item : subList) {
                                boolean fromIsCreator = (item.getCreatorBranchId() == item.getFromBranchId());
                                if (fromIsCreator) {
                                    creatorWName = item.getFromWarehouseName();
                                    partners.add(item.getToWarehouseName());
                                } else {
                                    creatorWName = item.getToWarehouseName();
                                    partners.add(item.getFromWarehouseName());
                                }
                            }
                            
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", representative.getStockTransferId());
                            map.put("code", transferCode);
                            map.put("type", "TRANSFER");
                            map.put("typeLabel", "Điều chuyển");
                            map.put("createdBy", representative.getCreatedByName());
                            map.put("createdAt", representative.getTransferDate() != null ? sdf.format(representative.getTransferDate()) : "");
                            map.put("amount", null);
                            map.put("idParamName", "transferId");
                            map.put("detailCallback", "viewTicketDetails(" + representative.getStockTransferId() + ")");
                            map.put("rawDate", representative.getTransferDate() != null ? representative.getTransferDate() : new java.util.Date(0));
                            map.put("warehouseName", creatorWName + " ➔ " + String.join(", ", partners));
                            map.put("actionApprove", "approveTransfer");
                            map.put("actionReject", "rejectTransfer");
                            unifiedApprovals.add(map);
                        }
                    } else if ("PENDING_PARTNER".equals(status)) {
                        List<model.StockTransfer> partnerSubList = new ArrayList<>();
                        for (model.StockTransfer item : subList) {
                            if (selectedWarehouseId != null && (item.getFromWarehouseId() == selectedWarehouseId || item.getToWarehouseId() == selectedWarehouseId)) {
                                partnerSubList.add(item);
                            }
                        }
                        
                        if (!partnerSubList.isEmpty()) {
                            model.StockTransfer partnerRep = partnerSubList.get(0);
                            int currentBranchId = (selectedWarehouseId == partnerRep.getFromWarehouseId()) ? partnerRep.getFromBranchId() : partnerRep.getToBranchId();
                            boolean fromIsCreator = (partnerRep.getCreatorBranchId() == partnerRep.getFromBranchId());
                            int partnerBranchId = fromIsCreator ? partnerRep.getToBranchId() : partnerRep.getFromBranchId();
                            
                            if (currentBranchId == partnerBranchId) {
                                String creatorWName = fromIsCreator ? partnerRep.getFromWarehouseName() : partnerRep.getToWarehouseName();
                                String partnerWName = fromIsCreator ? partnerRep.getToWarehouseName() : partnerRep.getFromWarehouseName();
                                
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", partnerRep.getStockTransferId());
                                map.put("code", transferCode);
                                map.put("type", "TRANSFER");
                                map.put("typeLabel", "Điều chuyển");
                                map.put("createdBy", partnerRep.getCreatedByName());
                                map.put("createdAt", partnerRep.getTransferDate() != null ? sdf.format(partnerRep.getTransferDate()) : "");
                                map.put("amount", null);
                                map.put("idParamName", "transferId");
                                map.put("detailCallback", "viewTicketDetails(" + partnerRep.getStockTransferId() + ")");
                                map.put("rawDate", partnerRep.getTransferDate() != null ? partnerRep.getTransferDate() : new java.util.Date(0));
                                map.put("warehouseName", creatorWName + " ➔ " + partnerWName);
                                map.put("actionApprove", "partnerApproveTransfer");
                                map.put("actionReject", "partnerRejectTransfer");
                                unifiedApprovals.add(map);
                            }
                        }
                    } else if ("PENDING_DISPATCH".equals(status)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", representative.getStockTransferId());
                        map.put("code", transferCode);
                        map.put("type", "TRANSFER");
                        map.put("typeLabel", "Điều chuyển");
                        map.put("createdBy", representative.getCreatedByName());
                        map.put("createdAt", representative.getTransferDate() != null ? sdf.format(representative.getTransferDate()) : "");
                        map.put("amount", null);
                        map.put("idParamName", "transferId");
                        map.put("detailCallback", "viewTicketDetails(" + representative.getStockTransferId() + ")");
                        map.put("rawDate", representative.getTransferDate() != null ? representative.getTransferDate() : new java.util.Date(0));
                        map.put("warehouseName", representative.getFromWarehouseName() + " ➔ " + representative.getToWarehouseName());
                        map.put("actionApprove", "approveTransfer");
                        map.put("actionReject", "rejectTransfer");
                        unifiedApprovals.add(map);
                    }
                }
            }
        }

        if (pendingOrders != null) {
            Integer selectedWarehouseId = (Integer) request.getSession().getAttribute("selectedWarehouseId");
            for (model.Order order : pendingOrders) {
                if (selectedWarehouseId != null && order.getWarehouseId() != selectedWarehouseId) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", order.getOrderId());
                map.put("code", order.getOrderCode());
                map.put("type", order.getOrderType());
                map.put("typeLabel", "PURCHASE".equalsIgnoreCase(order.getOrderType()) ? "Nhập" : "Xuất");
                map.put("warehouseName", order.getCustomerName()); // customerName stores warehouseName
                map.put("createdBy", order.getEmployeeName());
                
                String dateStr = order.getCreatedAt();
                java.util.Date parsedDate = null;
                if (dateStr != null) {
                    try {
                        parsedDate = inputSdf.parse(dateStr);
                    } catch (Exception e) {
                        // ignore
                    }
                }
                map.put("createdAt", parsedDate != null ? sdf.format(parsedDate) : dateStr);
                map.put("amount", order.getTotalAmount());
                map.put("actionApprove", "approveOrder");
                map.put("actionReject", "rejectOrder");
                map.put("idParamName", "orderId");
                map.put("detailCallback", "viewOrderDetails(" + order.getOrderId() + ")");
                map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                unifiedApprovals.add(map);
            }
        }
        
        if (pendingChecks != null) {
            Integer selectedWarehouseId = (Integer) request.getSession().getAttribute("selectedWarehouseId");
            for (model.InventoryCheck check : pendingChecks) {
                if (selectedWarehouseId != null && check.getWarehouseId() != selectedWarehouseId) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", check.getCheckId());
                map.put("code", check.getCheckCode());
                map.put("type", "CHECK");
                map.put("typeLabel", "Kiểm Kho");
                map.put("warehouseName", check.getWarehouseName());
                map.put("createdBy", check.getCreatedByName());
                
                java.util.Date parsedDate = null;
                if (check.getCreatedAt() != null) {
                    try {
                        parsedDate = java.sql.Timestamp.valueOf(check.getCreatedAt());
                    } catch (Exception e) {
                        // ignore
                    }
                }
                map.put("createdAt", check.getFormattedCreatedAt());
                map.put("amount", null);
                map.put("actionApprove", "approveCheck");
                map.put("actionReject", "rejectCheck");
                map.put("idParamName", "checkId");
                map.put("detailCallback", "viewCheckDetails(" + check.getCheckId() + ")");
                map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                unifiedApprovals.add(map);
            }
        }

        // Sort by rawDate descending
        unifiedApprovals.sort((m1, m2) -> ((java.util.Date) m2.get("rawDate")).compareTo((java.util.Date) m1.get("rawDate")));

        request.setAttribute("pendingTransfers", pendingTransfers);
        request.setAttribute("pendingOrders", pendingOrders);
        request.setAttribute("unifiedApprovals", unifiedApprovals);
        request.setAttribute("transferCodeQuery", transferCodeQuery);
        request.setAttribute("fromWarehouseQuery", fromWarehouseQuery);
        request.setAttribute("toWarehouseQuery", toWarehouseQuery);
    }
}

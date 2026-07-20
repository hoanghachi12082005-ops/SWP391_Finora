package controller.inventory;

import dao.inventory.InventoryDAO;
import dao.inventory.WarehouseDAO;
import dao.inventory.StockTransferDAO;
import dao.inventory.StockTransactionDAO;
import model.Employee;
import model.StockTransfer;
import model.StockTransferDetail;
import model.Warehouse;
import model.StockTransaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Controller xử lý các thao tác điều chuyển kho (Stock Transfer).
 * Được tách từ InventoryController.java để dễ bảo trì.
 */
@WebServlet(name = "TransferController", urlPatterns = {"/inventory-transfer"})
public class TransferController extends InventoryBaseController {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final StockTransferDAO transferDAO = new StockTransferDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();

    /**
     * Xử lý yêu cầu GET: Xử lý các yêu cầu lấy thông tin liên quan đến chuyển kho
     * (Tìm kiếm sản phẩm trong kho, Xem chi tiết phiếu chuyển kho, In phiếu chuyển kho).
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
            // action=searchProductsApi → [MOVED FROM InventoryController] - Original lines 134-136
            if ("searchProductsApi".equals(action)) {
                handleSearchProductsApi(request, response, selectedWarehouseId);
                return;
            }

            // action=searchAllProductsApi → [MOVED FROM InventoryController] - Original lines 137-139
            if ("searchAllProductsApi".equals(action)) {
                handleSearchAllProductsApi(request, response);
                return;
            }

            // action=viewTicket → [MOVED FROM InventoryController] - Original lines 157-171
            if ("viewTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                String showAll = request.getParameter("showAll");
                model.StockTransfer transfer = transferDAO.findById(ticketId);
                if (transfer != null) {
                    List<model.StockTransfer> sub = transferDAO.findSubTransfersByCode(transfer.getTransferCode());
                    transfer.setSubTransfers(sub);
                    transfer.setDisplayStatus(transferDAO.calculateDisplayStatus(sub));
                    request.setAttribute("ticket", transfer);
                    request.setAttribute("subTransfers", sub);
                    request.setAttribute("showAll", "true".equals(showAll));
                }
                
                forward(request, response, "inventory/modals/_modal_ticket_details");
                return;
            }

            // action=printTicket → [MOVED FROM InventoryController] - Original lines 184-200
            if ("printTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                model.StockTransfer transfer = transferDAO.findById(ticketId);
                List<model.StockTransfer> sub = new ArrayList<>();
                if (transfer != null) {
                    sub = transferDAO.findSubTransfersByCode(transfer.getTransferCode());
                    transfer.setSubTransfers(sub);
                    transfer.setDisplayStatus(transferDAO.calculateDisplayStatus(sub));
                }
                List<model.StockTransaction> txs = transactionDAO.findByReference("STOCK_TRANSFER", ticketId);
                
                request.setAttribute("ticket", transfer);
                request.setAttribute("subTransfers", sub);
                request.setAttribute("transactions", txs);
                
                forward(request, response, "inventory/prints/_print_ticket");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Xử lý yêu cầu POST: Thực hiện các hành động làm thay đổi dữ liệu chuyển kho
     * (Tạo phiếu chuyển, Xác nhận gửi, Từ chối gửi, Xác nhận nhận, Hủy phiếu điều chuyển...).
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
                // case "saveTransfer" → [MOVED FROM InventoryController] - Original lines 1309-1372
                case "saveTransfer": {
                    if (request.getContentLengthLong() > 5 * 1024 * 1024) {
                        request.getSession().setAttribute("error", "Dung lượng dữ liệu gửi lên quá lớn (vượt quá 5MB).");
                        int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                        redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + currentWarehouseId);
                        return;
                    }
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] partnerWarehouseIds = request.getParameterValues("partnerWarehouseId[]");
                    String[] quantities = request.getParameterValues("quantity[]");
                    String[] actionTypes = request.getParameterValues("actionType[]");
                    
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    boolean isOwner = "Owner".equals(currentUser.getRoleName());
                    
                    if (productIds != null && productIds.length > 0) {
                        java.util.Map<String, List<model.StockTransferDetail>> groupedDetails = new java.util.HashMap<>();
                        java.util.Map<String, int[]> groupInfo = new java.util.HashMap<>();
                        
                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int partnerWId = Integer.parseInt(partnerWarehouseIds[i]);
                            int qty = Integer.parseInt(quantities[i]);
                            String actType = (actionTypes != null && actionTypes.length > i) ? actionTypes[i] : "RECEIVE";
                            
                            if (qty > 0) {
                                int fromWId = "RECEIVE".equals(actType) ? partnerWId : currentWarehouseId;
                                int toWId = "RECEIVE".equals(actType) ? currentWarehouseId : partnerWId;
                                String key = fromWId + "_" + toWId;
                                
                                model.StockTransferDetail detail = new model.StockTransferDetail();
                                detail.setProductId(pId);
                                detail.setQuantity(qty);
                                
                                groupedDetails.computeIfAbsent(key, k -> new ArrayList<>()).add(detail);
                                groupInfo.put(key, new int[]{fromWId, toWId});
                            }
                        }
                        
                        String transferCode = "TO-" + System.currentTimeMillis();
                        List<model.StockTransfer> transferList = new ArrayList<>();
                        for (java.util.Map.Entry<String, List<model.StockTransferDetail>> entry : groupedDetails.entrySet()) {
                            String key = entry.getKey();
                            List<model.StockTransferDetail> details = entry.getValue();
                            int[] info = groupInfo.get(key);
                            int fromWId = info[0];
                            int toWId = info[1];
                            
                            model.StockTransfer transfer = new model.StockTransfer();
                            transfer.setTransferCode(transferCode);
                            transfer.setFromWarehouseId(fromWId);
                            transfer.setToWarehouseId(toWId);
                            // Owner tạo -> tự động duyệt (PENDING_PARTNER), nhân viên tạo -> PENDING_OWNER
                            transfer.setStatus(isOwner ? "PENDING_PARTNER" : "PENDING_OWNER");
                            transfer.setCreatedBy(currentUser.getEmployeeId());
                            transfer.setDetails(details);
                            transferList.add(transfer);
                        }
                        transferDAO.createTransfers(transferList);
                    }

                    if (isOwner) {
                        request.getSession().setAttribute("message", "Đã tạo phiếu điều chuyển (Đã tự động duyệt, đang chờ các kho đối tác duyệt).");
                    } else {
                        request.getSession().setAttribute("message", "Đã tạo phiếu điều chuyển (Chờ duyệt).");
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + currentWarehouseId);
                    break;
                }
                // case "confirmDispatch" → [MOVED FROM InventoryController] - Original lines 1534-1543
                case "confirmDispatch": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    new service.inventory.TransferService().dispatchTransfer(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã xuất kho thành công. Hàng đang trên đường vận chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                // case "rejectDispatch" → [MOVED FROM InventoryController] - Original lines 1545-1555
                case "rejectDispatch": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    new service.inventory.TransferService().rejectDispatch(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã từ chối xuất kho trung chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                // case "confirmReceive" → [MOVED FROM InventoryController] - Original lines 1556-1566
                case "confirmReceive": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    new service.inventory.TransferService().receiveTransfer(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã nhập kho thành công. Phiếu điều chuyển hoàn tất.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                // case "rejectReceive" → [MOVED FROM InventoryController] - Original lines 1567-1577
                case "rejectReceive": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    new service.inventory.TransferService().rejectReceive(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã từ chối nhận kho trung chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                // case "cancelTransfer" → [MOVED FROM InventoryController] - Original lines 1578-1597
                case "cancelTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        transferDAO.updateStatusByCode(st.getTransferCode(), "CANCELLED", null);
                    }
                    
                    request.getSession().setAttribute("message", "Đã hủy phiếu điều chuyển.");
                    String redirectTab = request.getParameter("tab");
                    if (redirectTab == null || redirectTab.isEmpty()) {
                        redirectTab = "transfer";
                    }
                    String whIdParam = request.getParameter("warehouseId");
                    if (whIdParam == null || whIdParam.isEmpty()) {
                        whIdParam = request.getParameter("currentWarehouseId");
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=" + redirectTab + "&warehouseId=" + whIdParam);
                    break;
                }
                // case "confirmReceiveWithDiscrepancy" → [MOVED FROM InventoryController] - Original lines 1947-1950
                case "confirmReceiveWithDiscrepancy":
                    request.getSession().setAttribute("message", "Tính năng đang bảo trì cấu trúc database.");
                    redirect(response, request.getContextPath() + "/inventory?tab=stock");
                    break;
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
     * Chuẩn bị dữ liệu hiển thị cho Tab "Chuyển kho" (Stock Transfer).
     * Lấy danh sách phiếu chuyển kho, phân loại subtab (phiếu gửi/nhận, phiếu đang xử lý), 
     * thực hiện bộ lọc tìm kiếm theo mã phiếu, trạng thái và đối tác kho.
     */
    void handleTransferTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        String subtab = request.getParameter("subtab");
        if (subtab == null || subtab.isEmpty()) {
            subtab = "transfer_list";
        }
        request.setAttribute("activeSubtab", subtab);

        // Filters
        String transferCodeQuery = request.getParameter("transferCodeQuery");
        String partnerWQueryStr = request.getParameter("partnerWarehouseQuery");
        String statusQuery = request.getParameter("statusQuery");

        Integer partnerWarehouseQuery = null;
        if (partnerWQueryStr != null && !partnerWQueryStr.trim().isEmpty()) {
            try {
                partnerWarehouseQuery = Integer.parseInt(partnerWQueryStr);
            } catch (NumberFormatException e) {
                // Ignore invalid format
            }
        }

        // Clean & validate text filters
        if (transferCodeQuery != null) {
            transferCodeQuery = transferCodeQuery.trim();
        }

        List<StockTransfer> transfers;
        if ("transfer_process".equals(subtab)) {
            transfers = transferDAO.findAllByStatusFiltered(
                warehouseId != null ? warehouseId : 0,
                null,
                transferCodeQuery,
                partnerWarehouseQuery
            );
        } else {
            transfers = transferDAO.findAllGrouped(
                warehouseId != null ? warehouseId : 0,
                statusQuery,
                transferCodeQuery,
                partnerWarehouseQuery
            );
        }
        request.setAttribute("transfers", transfers);
        request.setAttribute("transferCodeQuery", transferCodeQuery);
        request.setAttribute("partnerWarehouseQuery", partnerWarehouseQuery);
        request.setAttribute("statusQuery", statusQuery);
    }

    /**
     * API tìm kiếm sản phẩm cho điều chuyển kho (theo cặp kho).
     * [MOVED FROM InventoryController] - Original lines 1964-2004
     */
    private void handleSearchProductsApi(HttpServletRequest request, HttpServletResponse response, Integer selectedWarehouseId) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String fromWIdParam = request.getParameter("fromWarehouseId");
        String toWIdParam = request.getParameter("toWarehouseId");
        
        int fromWarehouseId = (fromWIdParam != null && !fromWIdParam.isEmpty()) ? Integer.parseInt(fromWIdParam) : (selectedWarehouseId != null ? selectedWarehouseId : 0);
        int toWarehouseId = (toWIdParam != null && !toWIdParam.isEmpty()) ? Integer.parseInt(toWIdParam) : 0;
        
        if (fromWarehouseId == 0 || toWarehouseId == 0) {
            response.getWriter().write("[]");
            return;
        }

        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }

        List<dto.inventory.ExchangeProductDTO> list = inventoryDAO.searchTransferProducts(fromWarehouseId, toWarehouseId, keyword);
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            dto.inventory.ExchangeProductDTO dto = list.get(i);
            json.append("{");
            json.append("\"productId\":").append(dto.getProductId()).append(",");
            json.append("\"productName\":\"").append(escapeJson(dto.getProductName())).append("\",");
            json.append("\"myStock\":").append(dto.getMyStock()).append(",");
            json.append("\"partnerWarehouseId\":").append(dto.getPartnerWarehouseId()).append(",");
            json.append("\"partnerWarehouseName\":\"").append(escapeJson(dto.getPartnerWarehouseName())).append("\",");
            json.append("\"partnerStock\":").append(dto.getPartnerStock());
            json.append("}");
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }

    /**
     * API tìm kiếm tất cả sản phẩm cho điều chuyển kho (đa kho).
     * [MOVED FROM InventoryController] - Original lines 2006-2085
     */
    private void handleSearchAllProductsApi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }

        int myWarehouseId = 0;
        String wIdParam = request.getParameter("warehouseId");
        if (wIdParam != null && !wIdParam.trim().isEmpty()) {
            try {
                myWarehouseId = Integer.parseInt(wIdParam.trim());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (myWarehouseId == 0) {
            Integer selectedWarehouseId = (Integer) request.getSession().getAttribute("selectedWarehouseId");
            myWarehouseId = selectedWarehouseId != null ? selectedWarehouseId : 0;
        }
        
        List<dto.inventory.ExchangeProductDTO> list = inventoryDAO.searchExchangeProducts(myWarehouseId, keyword);
        
        // Group by productId
        java.util.Map<Integer, java.util.Map<String, Object>> productMap = new java.util.LinkedHashMap<>();
        for (dto.inventory.ExchangeProductDTO p : list) {
            java.util.Map<String, Object> prod = productMap.get(p.getProductId());
            if (prod == null) {
                prod = new java.util.LinkedHashMap<>();
                prod.put("productId", p.getProductId());
                prod.put("productName", p.getProductName());
                prod.put("myStock", p.getMyStock());
                prod.put("partners", new java.util.ArrayList<java.util.Map<String, Object>>());
                productMap.put(p.getProductId(), prod);
            }
            java.util.List<java.util.Map<String, Object>> partners = (java.util.List<java.util.Map<String, Object>>) prod.get("partners");
            java.util.Map<String, Object> partner = new java.util.LinkedHashMap<>();
            partner.put("warehouseId", p.getPartnerWarehouseId());
            partner.put("warehouseName", p.getPartnerWarehouseName());
            partner.put("stock", p.getPartnerStock());
            partners.add(partner);
        }

        // Sort partners of each product by stock descending
        for (java.util.Map<String, Object> prod : productMap.values()) {
            java.util.List<java.util.Map<String, Object>> partners = (java.util.List<java.util.Map<String, Object>>) prod.get("partners");
            partners.sort((a, b) -> Integer.compare((Integer) b.get("stock"), (Integer) a.get("stock")));
        }

        StringBuilder json = new StringBuilder("[");
        int prodIndex = 0;
        for (java.util.Map<String, Object> prod : productMap.values()) {
            json.append("{");
            json.append("\"productId\":").append(prod.get("productId")).append(",");
            json.append("\"productName\":\"").append(escapeJson((String) prod.get("productName"))).append("\",");
            json.append("\"myStock\":").append(prod.get("myStock")).append(",");
            json.append("\"partners\":[");
            java.util.List<java.util.Map<String, Object>> partners = (java.util.List<java.util.Map<String, Object>>) prod.get("partners");
            for (int j = 0; j < partners.size(); j++) {
                java.util.Map<String, Object> part = partners.get(j);
                json.append("{");
                json.append("\"warehouseId\":").append(part.get("warehouseId")).append(",");
                json.append("\"warehouseName\":\"").append(escapeJson((String) part.get("warehouseName"))).append("\",");
                json.append("\"stock\":").append(part.get("stock"));
                json.append("}");
                if (j < partners.size() - 1) json.append(",");
            }
            json.append("]");
            json.append("}");
            if (prodIndex < productMap.size() - 1) json.append(",");
            prodIndex++;
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }
}

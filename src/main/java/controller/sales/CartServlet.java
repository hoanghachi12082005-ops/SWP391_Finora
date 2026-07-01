package controller.sales;

import dao.sales.CustomerDAO;
import dao.sales.InventoryDAO;
import dao.sales.ProductDAO;
import dao.sales.VoucherDAO;
import model.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private static final String TABS_ATTR = "cartTabs";
    private static final String ACTIVE_TAB_ATTR = "activeTabId";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        int warehouseId = 1;
        if (emp != null) {
            warehouseId = getWarehouseId(emp.getBranchId());
        }

        // Khởi tạo Map các tab nếu chưa có
        @SuppressWarnings("unchecked")
        Map<Integer, OrderTab> tabs = (Map<Integer, OrderTab>) session.getAttribute(TABS_ATTR);
        if (tabs == null) {
            tabs = new LinkedHashMap<>();
            tabs.put(1, new OrderTab(1));
            session.setAttribute(TABS_ATTR, tabs);
        }

        // Lấy active tab ID
        Integer activeTabId = (Integer) session.getAttribute(ACTIVE_TAB_ATTR);
        if (activeTabId == null || !tabs.containsKey(activeTabId)) {
            activeTabId = tabs.keySet().iterator().next();
            session.setAttribute(ACTIVE_TAB_ATTR, activeTabId);
        }

        String action = req.getParameter("action");
        String tabIdStr = req.getParameter("tabId");
        int targetTabId = (tabIdStr != null && !tabIdStr.isBlank()) ? Integer.parseInt(tabIdStr) : activeTabId;

        // Đảm bảo targetTabId tồn tại trong Map
        if (!tabs.containsKey(targetTabId)) {
            targetTabId = activeTabId;
        }

        OrderTab targetTab = tabs.get(targetTabId);
        PrintWriter out = resp.getWriter();

        try {
            switch (action != null ? action : "") {
                case "newTab" -> {
                    // Tạo tab mới kế tiếp
                    int nextTabId = 1;
                    for (Integer id : tabs.keySet()) {
                        if (id >= nextTabId) nextTabId = id + 1;
                    }
                    tabs.put(nextTabId, new OrderTab(nextTabId));
                    session.setAttribute(ACTIVE_TAB_ATTR, nextTabId);
                    activeTabId = nextTabId;
                }
                case "switchTab" -> {
                    int switchId = Integer.parseInt(req.getParameter("tabId"));
                    if (tabs.containsKey(switchId)) {
                        session.setAttribute(ACTIVE_TAB_ATTR, switchId);
                        activeTabId = switchId;
                    }
                }
                case "add" -> {
                    handleAdd(req, targetTab, warehouseId, out);
                    // Dữ liệu đã ghi ra out trong handleAdd, ta chỉ cần return
                    return;
                }
                case "update" -> {
                    handleUpdate(req, targetTab, out);
                    return;
                }
                case "remove" -> {
                    handleRemove(req, targetTab, out);
                    return;
                }
                case "selectCustomer" -> {
                    String cusIdStr = req.getParameter("customerId");
                    if (cusIdStr != null && !cusIdStr.isBlank()) {
                        int cusId = Integer.parseInt(cusIdStr);
                        if (cusId == 0) {
                            targetTab.setSelectedCustomer(null);
                        } else {
                            CustomerDAO customerDao = new CustomerDAO();
                            List<Customer> allCus = customerDao.getAll();
                            Customer selected = null;
                            for (Customer c : allCus) {
                                if (c.getCusId() == cusId) {
                                    selected = c;
                                    break;
                                }
                            }
                            targetTab.setSelectedCustomer(selected);
                        }
                    }
                }
                case "applyVoucher" -> {
                    String voucherIdStr = req.getParameter("voucherId");
                    if (voucherIdStr == null || voucherIdStr.isBlank() || "0".equals(voucherIdStr)) {
                        targetTab.setAppliedVoucher(null);
                    } else {
                        int vId = Integer.parseInt(voucherIdStr);
                        VoucherDAO voucherDao = new VoucherDAO();
                        Voucher v = voucherDao.getById(vId);
                        targetTab.setAppliedVoucher(v);
                    }
                }
                case "hold" -> {
                    targetTab.setStatus("HOLD");
                }
                case "clear" -> {
                    // Xóa tab hiện tại khỏi Map
                    tabs.remove(targetTabId);
                    if (tabs.isEmpty()) {
                        tabs.put(1, new OrderTab(1));
                        session.setAttribute(ACTIVE_TAB_ATTR, 1);
                        activeTabId = 1;
                    } else {
                        // Chuyển activeTabId sang tab khác
                        activeTabId = tabs.keySet().iterator().next();
                        session.setAttribute(ACTIVE_TAB_ATTR, activeTabId);
                    }
                }
                default -> {
                    out.write("{\"error\":\"Action không hợp lệ.\"}");
                    return;
                }
            }

            // Trả về JSON trạng thái hiện tại
            writeResponseJson(out, tabs, activeTabId);

        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"error\":\"" + escJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        @SuppressWarnings("unchecked")
        Map<Integer, OrderTab> tabs = (Map<Integer, OrderTab>) session.getAttribute(TABS_ATTR);
        if (tabs == null) {
            tabs = new LinkedHashMap<>();
            tabs.put(1, new OrderTab(1));
            session.setAttribute(TABS_ATTR, tabs);
        }

        Integer activeTabId = (Integer) session.getAttribute(ACTIVE_TAB_ATTR);
        if (activeTabId == null || !tabs.containsKey(activeTabId)) {
            activeTabId = tabs.keySet().iterator().next();
            session.setAttribute(ACTIVE_TAB_ATTR, activeTabId);
        }

        writeResponseJson(resp.getWriter(), tabs, activeTabId);
    }

    // ── Handlers ────────────────────────────────────────────────

    private void handleAdd(HttpServletRequest req, OrderTab tab, int warehouseId, PrintWriter out) {
        String productIdStr = req.getParameter("productId");
        String codeParam = req.getParameter("code");
        String qtyStr = req.getParameter("quantity");
        int qty = 1;
        if (qtyStr != null) {
            try { qty = Integer.parseInt(qtyStr); } catch (NumberFormatException ignored) {}
        }

        ProductDAO productDao = new ProductDAO();
        Product product = null;

        if (productIdStr != null && !productIdStr.isBlank()) {
            int productId = Integer.parseInt(productIdStr);
            List<Product> all = productDao.getAllActiveByWarehouse(warehouseId);
            for (Product p : all) {
                if (p.getProductId() == productId) { product = p; break; }
            }
        } else if (codeParam != null && !codeParam.isBlank()) {
            product = productDao.findByCodebar(codeParam.trim(), warehouseId);
        }

        if (product == null) {
            out.write("{\"error\":\"Không tìm thấy sản phẩm.\"}");
            return;
        }

        int stock = product.getQuantityInStock();
        if (stock <= 0) {
            out.write("{\"error\":\"Sản phẩm này đã hết hàng (Tồn kho: 0).\"}");
            return;
        }

        CartItem existing = null;
        for (CartItem item : tab.getItems()) {
            if (item.getProductId() == product.getProductId()) {
                existing = item;
                break;
            }
        }

        int currentQtyInCart = existing != null ? existing.getQuantity() : 0;
        int newQty = currentQtyInCart + qty;

        if (newQty > stock) {
            out.write("{\"error\":\"Không đủ tồn kho. Tồn kho hiện tại: " + stock
                    + ", đã trong giỏ: " + currentQtyInCart + "\"}");
            return;
        }

        if (existing != null) {
            existing.setQuantity(newQty);
            existing.setStockAvailable(stock);
        } else {
            CartItem ci = new CartItem(
                product.getProductId(),
                product.getProductName(),
                product.getProductCode(),
                product.getSellingPrice(),
                qty,
                stock
            );
            tab.getItems().add(ci);
        }

        @SuppressWarnings("unchecked")
        Map<Integer, OrderTab> tabs = (Map<Integer, OrderTab>) req.getSession().getAttribute(TABS_ATTR);
        Integer activeTabId = (Integer) req.getSession().getAttribute(ACTIVE_TAB_ATTR);
        writeResponseJson(out, tabs, activeTabId);
    }

    private void handleUpdate(HttpServletRequest req, OrderTab tab, PrintWriter out) {
        int productId = Integer.parseInt(req.getParameter("productId"));
        int newQty = Integer.parseInt(req.getParameter("quantity"));

        for (int i = 0; i < tab.getItems().size(); i++) {
            CartItem item = tab.getItems().get(i);
            if (item.getProductId() == productId) {
                if (newQty <= 0) {
                    tab.getItems().remove(i);
                } else if (newQty > item.getStockAvailable()) {
                    out.write("{\"error\":\"Vượt quá tồn kho. Tối đa: " + item.getStockAvailable() + "\"}");
                    return;
                } else {
                    item.setQuantity(newQty);
                }
                break;
            }
        }

        @SuppressWarnings("unchecked")
        Map<Integer, OrderTab> tabs = (Map<Integer, OrderTab>) req.getSession().getAttribute(TABS_ATTR);
        Integer activeTabId = (Integer) req.getSession().getAttribute(ACTIVE_TAB_ATTR);
        writeResponseJson(out, tabs, activeTabId);
    }

    private void handleRemove(HttpServletRequest req, OrderTab tab, PrintWriter out) {
        int productId = Integer.parseInt(req.getParameter("productId"));
        tab.getItems().removeIf(item -> item.getProductId() == productId);

        @SuppressWarnings("unchecked")
        Map<Integer, OrderTab> tabs = (Map<Integer, OrderTab>) req.getSession().getAttribute(TABS_ATTR);
        Integer activeTabId = (Integer) req.getSession().getAttribute(ACTIVE_TAB_ATTR);
        writeResponseJson(out, tabs, activeTabId);
    }

    // ── Helper xuất JSON trạng thái đa tab ────────────────────────

    private void writeResponseJson(PrintWriter out, Map<Integer, OrderTab> tabs, int activeTabId) {
        OrderTab activeTab = tabs.get(activeTabId);
        
        out.write("{");
        out.write("\"activeTabId\":" + activeTabId + ",");
        
        // 1. Output danh sách tab
        out.write("\"tabs\":[");
        int index = 0;
        for (Map.Entry<Integer, OrderTab> entry : tabs.entrySet()) {
            if (index > 0) out.write(",");
            OrderTab t = entry.getValue();
            out.write("{");
            out.write("\"tabId\":" + t.getTabId() + ",");
            out.write("\"subtotal\":" + t.getSubtotal() + ",");
            out.write("\"totalAmount\":" + t.getTotalAmount() + ",");
            out.write("\"status\":\"" + t.getStatus() + "\",");
            out.write("\"active\":" + (t.getTabId() == activeTabId));
            out.write("}");
            index++;
        }
        out.write("],");

        // 2. Output thông tin chi tiết tab đang active
        out.write("\"activeTab\":{");
        out.write("\"tabId\":" + activeTab.getTabId() + ",");
        out.write("\"status\":\"" + activeTab.getStatus() + "\",");
        out.write("\"note\":\"" + escJson(activeTab.getNote()) + "\",");
        
        // selectedCustomer
        if (activeTab.getSelectedCustomer() != null) {
            Customer c = activeTab.getSelectedCustomer();
            out.write("\"selectedCustomer\":{");
            out.write("\"cusId\":" + c.getCusId() + ",");
            out.write("\"fullName\":\"" + escJson(c.getFullName()) + "\",");
            out.write("\"phone\":\"" + escJson(c.getPhone()) + "\"");
            out.write("},");
        } else {
            out.write("\"selectedCustomer\":null,");
        }

        // appliedVoucher
        if (activeTab.getAppliedVoucher() != null) {
            Voucher v = activeTab.getAppliedVoucher();
            out.write("\"appliedVoucher\":{");
            out.write("\"voucherId\":" + v.getVoucherId() + ",");
            out.write("\"voucherCode\":\"" + escJson(v.getVoucherCode()) + "\",");
            out.write("\"voucherName\":\"" + escJson(v.getVoucherName()) + "\",");
            out.write("\"discountType\":\"" + escJson(v.getDiscountType()) + "\",");
            out.write("\"discountValue\":" + v.getDiscountValue());
            out.write("},");
        } else {
            out.write("\"appliedVoucher\":null,");
        }

        // items list
        out.write("\"items\":[");
        for (int i = 0; i < activeTab.getItems().size(); i++) {
            if (i > 0) out.write(",");
            CartItem ci = activeTab.getItems().get(i);
            out.write("{");
            out.write("\"productId\":" + ci.getProductId() + ",");
            out.write("\"productName\":\"" + escJson(ci.getProductName()) + "\",");
            out.write("\"productCodebar\":\"" + escJson(ci.getProductCodebar()) + "\",");
            out.write("\"sellingPrice\":" + ci.getSellingPrice() + ",");
            out.write("\"quantity\":" + ci.getQuantity() + ",");
            out.write("\"lineTotal\":" + ci.getLineTotal() + ",");
            out.write("\"stockAvailable\":" + ci.getStockAvailable());
            out.write("}");
        }
        out.write("],");

        // totals
        out.write("\"subtotal\":" + activeTab.getSubtotal() + ",");
        out.write("\"discountAmount\":" + activeTab.getDiscountAmount() + ",");
        out.write("\"vatAmount\":" + activeTab.getVatAmount() + ",");
        out.write("\"totalAmount\":" + activeTab.getTotalAmount());
        
        out.write("}"); // end activeTab
        out.write("}"); // end main object
    }

    private int getWarehouseId(int branchId) {
        try (var conn = util.database.DBContext.getConnection();
             var ps = conn.prepareStatement("SELECT TOP 1 warehouse_id FROM warehouse WHERE branch_id = ?")) {
            ps.setInt(1, branchId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("warehouse_id");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return branchId;
    }

    private String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}

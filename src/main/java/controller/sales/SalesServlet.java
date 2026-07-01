package controller.sales;

import dao.sales.ProductDAO;
import dao.sales.CustomerDAO;
import dao.sales.VoucherDAO;
import model.Employee;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/sales")
public class SalesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        // Lấy employee từ session — nếu chưa đăng nhập, giả lập nhân viên mặc định
        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        if (emp == null) {
            // Giả lập nhân viên mặc định cho phát triển/testing
            emp = new Employee();
            emp.setEmpId(1);
            emp.setBranchId(1);
            emp.setFullName("Thu ngân #1");
            session.setAttribute("employee", emp);
        }

        // Xác định warehouse_id từ branch_id
        int warehouseId = getWarehouseId(emp.getBranchId());

        ProductDAO productDao = new ProductDAO();
        CustomerDAO customerDao = new CustomerDAO();

        if ("searchProduct".equals(action)) {
            // AJAX: tìm kiếm sản phẩm
            String keyword = req.getParameter("keyword");
            List<Product> results = productDao.searchByKeyword(
                    keyword != null ? keyword.trim() : "", warehouseId);
            writeProductListJson(resp, results);
            return;
        }

        if ("findByCode".equals(action)) {
            // AJAX: tìm sản phẩm theo mã barcode chính xác
            String code = req.getParameter("code");
            Product p = productDao.findByCodebar(code != null ? code.trim() : "", warehouseId);
            if (p != null) {
                writeProductJson(resp, p);
            } else {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write("{\"error\":\"Không tìm thấy sản phẩm với mã: " + escJson(code) + "\"}");
            }
            return;
        }

        if ("checkVoucher".equals(action)) {
            // AJAX: kiểm tra và lấy thông tin voucher
            String code = req.getParameter("code");
            dao.sales.VoucherDAO voucherDao = new dao.sales.VoucherDAO();
            model.Voucher v = voucherDao.getValidByCode(code != null ? code.trim() : "");
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            if (v != null) {
                resp.getWriter().write("{"
                    + "\"voucherId\":" + v.getVoucherId() + ","
                    + "\"voucherCode\":\"" + escJson(v.getVoucherCode()) + "\","
                    + "\"discountType\":\"" + escJson(v.getDiscountType()) + "\","
                    + "\"discountValue\":" + v.getDiscountValue()
                    + "}");
            } else {
                resp.getWriter().write("{\"error\":\"Mã giảm giá không hợp lệ hoặc đã hết hạn.\"}");
            }
            return;
        }

        // Default: load trang POS
        req.setAttribute("activePage", "sales");

        // Đảm bảo cartTabs và activeTabId đã được khởi tạo
        @SuppressWarnings("unchecked")
        Map<Integer, model.OrderTab> tabs = (Map<Integer, model.OrderTab>) session.getAttribute("cartTabs");
        if (tabs == null) {
            tabs = new java.util.LinkedHashMap<>();
            tabs.put(1, new model.OrderTab(1));
            session.setAttribute("cartTabs", tabs);
        }
        Integer activeTabId = (Integer) session.getAttribute("activeTabId");
        if (activeTabId == null || !tabs.containsKey(activeTabId)) {
            activeTabId = tabs.keySet().iterator().next();
            session.setAttribute("activeTabId", activeTabId);
        }

        // Lấy danh sách voucher hoạt động
        dao.sales.VoucherDAO voucherDao = new dao.sales.VoucherDAO();
        List<model.Voucher> vouchers = voucherDao.getAllValidVouchers();
        req.setAttribute("vouchers", vouchers);

        List<Product> productList = productDao.getAllActiveByWarehouse(warehouseId);
        req.setAttribute("productList", productList);
        req.setAttribute("customerList", customerDao.getAll());
        req.setAttribute("warehouseId", warehouseId);
        req.getRequestDispatcher("/WEB-INF/views/sales/sales.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("addCustomer".equals(action)) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            CustomerDAO customerDao = new CustomerDAO();
            model.Customer c = new model.Customer();
            c.setFullName(req.getParameter("fullName"));
            c.setPhone(req.getParameter("phone"));
            c.setEmail(req.getParameter("email"));
            c.setBod(req.getParameter("bod"));
            c.setGender(req.getParameter("gender"));
            c.setAddress(req.getParameter("address"));
            c.setCusType(model.Customer.CustomerType.REGULAR);

            int cusId = customerDao.insert(c);
            if (cusId > 0) {
                resp.getWriter().write("{\"cusId\":" + cusId + ",\"fullName\":\"" + escJson(c.getFullName()) + "\"}");
            } else {
                resp.getWriter().write("{\"error\":\"Không thể thêm khách hàng. Kiểm tra thông tin.\"}");
            }
            return;
        }

        // Default: redirect to GET
        resp.sendRedirect(req.getContextPath() + "/sales");
    }

    /**
     * Lấy warehouse_id tương ứng với branch_id.
     * Giả định: warehouse_id tương ứng 1-1 với branch_id (theo spec).
     */
    private int getWarehouseId(int branchId) {
        // Có thể query DB: SELECT warehouse_id FROM warehouse WHERE branch_id = ?
        // Nhưng theo spec giả định 1-1 mapping
        try (var conn = util.database.DBContext.getConnection();
             var ps = conn.prepareStatement("SELECT TOP 1 warehouse_id FROM warehouse WHERE branch_id = ?")) {
            ps.setInt(1, branchId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("warehouse_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return branchId; // fallback
    }

    // ── JSON helpers (thủ công, không phụ thuộc thư viện ngoài) ──

    private void writeProductListJson(HttpServletResponse resp, List<Product> list) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.write("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) out.write(",");
            writeProductObj(out, list.get(i));
        }
        out.write("]");
    }

    private void writeProductJson(HttpServletResponse resp, Product p) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        writeProductObj(resp.getWriter(), p);
    }

    private void writeProductObj(PrintWriter out, Product p) {
        out.write("{");
        out.write("\"productId\":" + p.getProductId() + ",");
        out.write("\"productName\":\"" + escJson(p.getProductName()) + "\",");
        out.write("\"productCodebar\":\"" + escJson(p.getProductCode()) + "\",");
        out.write("\"sellingPrice\":" + p.getSellingPrice() + ",");
        out.write("\"quantityInStock\":" + p.getQuantityInStock());
        out.write("}");
    }

    private String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}

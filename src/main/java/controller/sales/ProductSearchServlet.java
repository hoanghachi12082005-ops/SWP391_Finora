package controller.sales;

import dao.product.ProductDAO;
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

@WebServlet("/product/search")
public class ProductSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String keyword = req.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }

        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        int branchId = (emp != null) ? emp.getBranchId() : 1;
        int warehouseId = getWarehouseId(branchId);

        ProductDAO productDao = new ProductDAO();
        List<Product> results = productDao.searchByKeyword(keyword.trim(), warehouseId);

        PrintWriter out = resp.getWriter();
        out.write("[");
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) out.write(",");
            Product p = results.get(i);
            out.write("{");
            out.write("\"productId\":" + p.getProductId() + ",");
            out.write("\"productName\":\"" + escJson(p.getProductName()) + "\",");
            out.write("\"productCodebar\":\"" + escJson(p.getProductCode()) + "\",");
            out.write("\"sellingPrice\":" + p.getSellingPrice() + ",");
            out.write("\"quantityInStock\":" + p.getQuantityInStock());
            out.write("}");
        }
        out.write("]");
    }

    private int getWarehouseId(int branchId) {
        try (var conn = util.database.DBContext.getConnection();
             var ps = conn.prepareStatement("SELECT TOP 1 warehouse_id FROM warehouse WHERE branch_id = ?")) {
            ps.setInt(1, branchId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("warehouse_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return branchId;
    }

    private String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}

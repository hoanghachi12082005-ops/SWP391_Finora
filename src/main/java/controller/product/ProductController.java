package controller.product;

import controller.common.BaseController;
import dao.product.ProductDAO;
import model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;


@WebServlet(name = "ProductController", urlPatterns = {"/products"})
public class ProductController extends BaseController {
    private ProductDAO productDAO;
    private static final int ITEMS_PER_PAGE = 5;

    @Override
    public void init() throws ServletException {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword  = request.getParameter("keyword");
        String status   = request.getParameter("status");
        String categoryParam = request.getParameter("categoryID");
        String unitParam = request.getParameter("unitID");
        String viewMode = request.getParameter("view");
        if (viewMode == null) viewMode = "table";

        Integer categoryID = null;
        Integer unitID = null;
        try {
            if (categoryParam != null && !categoryParam.isBlank()) {
                categoryID = Integer.parseInt(categoryParam.trim());
            }
            if (unitParam != null && !unitParam.isBlank()) {
                unitID = Integer.parseInt(unitParam.trim());
            }
        } catch (NumberFormatException ignored) {}

        int page = 1;
        try {
            if (request.getParameter("page") != null)
                page = Integer.parseInt(request.getParameter("page").trim());
        } catch (NumberFormatException ignored) {}
        try {
            int totalCount = productDAO.getTotalCount(keyword, status, categoryID, unitID);
            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));

            request.setAttribute("products",    productDAO.findAll((page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE, keyword, status, categoryID, unitID));
            request.setAttribute("categories",  productDAO.findAllCategories());
            request.setAttribute("units",       productDAO.findAllUnits());
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages",  totalPages);
            request.setAttribute("keyword",     keyword != null ? keyword : "");
            request.setAttribute("filterStatus",status != null ? status : "");
            request.setAttribute("filterCategoryID", categoryID);
            request.setAttribute("filterUnitID", unitID);
            request.setAttribute("viewMode",    viewMode);

            forward(request, response, "products/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Database error retrieving products", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            String action = request.getParameter("action");
            if (action == null) action = "";
            if ("add".equals(action)) {
                Product p = buildProductFromRequest(request);
                productDAO.insert(p);
            } else if ("edit".equals(action)) {
                Product p = buildProductFromRequest(request);
                p.setProductID(Integer.parseInt(request.getParameter("productID")));
                productDAO.update(p);
            } else if ("delete".equals(action)) {
                try {
                    productDAO.delete(Integer.parseInt(request.getParameter("id")));
                    request.getSession().setAttribute("message", "Xóa sản phẩm thành công!");
                    request.getSession().setAttribute("messageType", "success");
                } catch (Exception e) {
                    e.printStackTrace();
                    request.getSession().setAttribute("message", "Không thể xóa sản phẩm này do đang có dữ liệu liên quan (giao dịch, đơn hàng...)!");
                    request.getSession().setAttribute("messageType", "danger");
                }
            }
            // Preserve search/filter params on redirect
            String keyword = request.getParameter("keyword");
            String status  = request.getParameter("filterStatus");
            String categoryID = request.getParameter("filterCategoryID");
            String unitID = request.getParameter("filterUnitID");
            String view    = request.getParameter("view");
            String page    = request.getParameter("page");
            StringBuilder redirect = new StringBuilder(request.getContextPath() + "/products?");
            if (keyword != null && !keyword.isBlank()) redirect.append("keyword=").append(keyword).append("&");
            if (status  != null && !status.isBlank())  redirect.append("status=").append(status).append("&");
            if (categoryID  != null && !categoryID.isBlank())  redirect.append("categoryID=").append(categoryID).append("&");
            if (unitID  != null && !unitID.isBlank())  redirect.append("unitID=").append(unitID).append("&");
            if (view    != null && !view.isBlank())    redirect.append("view=").append(view).append("&");
            if (page    != null && !page.isBlank())    redirect.append("page=").append(page);
            
            // Clean up trailing & if exists
            if (redirect.charAt(redirect.length() - 1) == '&' || redirect.charAt(redirect.length() - 1) == '?') {
                redirect.deleteCharAt(redirect.length() - 1);
            }
            
            response.sendRedirect(redirect.toString());
        } catch (Exception e) {
            throw new ServletException("Error processing request", e);
        }
    }

    private Product buildProductFromRequest(HttpServletRequest request) {
        Product p = new Product();
        p.setCategoryID(Integer.parseInt(request.getParameter("categoryID")));
        p.setName(request.getParameter("name"));
        p.setQuantity(Integer.parseInt(request.getParameter("quantity")));
        p.setUnitID(Integer.parseInt(request.getParameter("unitID")));
        p.setSellingPrice(new BigDecimal(request.getParameter("sellingPrice")));
        p.setStatus(request.getParameter("status"));
        return p;
    }
}

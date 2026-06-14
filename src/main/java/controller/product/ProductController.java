package controller.product;

import controller.common.BaseController;
import product.dao.ProductDAO;
import product.model.Product;

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
        String viewMode = request.getParameter("view");
        if (viewMode == null) viewMode = "table";

        int page = 1;
        try {
            if (request.getParameter("page") != null)
                page = Integer.parseInt(request.getParameter("page").trim());
        } catch (NumberFormatException ignored) {}
        try {
            int totalCount = productDAO.getTotalCount(keyword, status);
            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));

            request.setAttribute("products",    productDAO.findAll((page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE, keyword, status));
            request.setAttribute("categories",  productDAO.findAllCategories());
            request.setAttribute("units",       productDAO.findAllUnits());
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages",  totalPages);
            request.setAttribute("keyword",     keyword != null ? keyword : "");
            request.setAttribute("filterStatus",status != null ? status : "");
            request.setAttribute("viewMode",    viewMode);

            forward(request, response, "products/index");
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
                productDAO.delete(Integer.parseInt(request.getParameter("id")));
            }
            // Preserve search/filter params on redirect
            String keyword = request.getParameter("keyword");
            String status  = request.getParameter("filterStatus");
            String view    = request.getParameter("view");
            StringBuilder redirect = new StringBuilder(request.getContextPath() + "/products?");
            if (keyword != null && !keyword.isBlank()) redirect.append("keyword=").append(keyword).append("&");
            if (status  != null && !status.isBlank())  redirect.append("status=").append(status).append("&");
            if (view    != null && !view.isBlank())    redirect.append("view=").append(view);
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

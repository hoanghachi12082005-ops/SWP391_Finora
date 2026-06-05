package controller;

import dao.ProductDAO;
import model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductManagementServlet extends HttpServlet {
    private ProductDAO productDAO;
    private static final int ITEMS_PER_PAGE = 5;

    @Override
    public void init() throws ServletException {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int page = 1;
        try {
            if (request.getParameter("page") != null) page = Integer.parseInt(request.getParameter("page").trim());
        } catch (NumberFormatException ignored) {}

        try {
            int totalPages = (int) Math.ceil((double) productDAO.getTotalCount() / ITEMS_PER_PAGE);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));

            request.setAttribute("products", productDAO.findAll((page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE));
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            
            request.getRequestDispatcher("/WEB-INF/views/product-management/index.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Database error retrieving products", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            switch (action == null ? "" : action) {
                case "add" -> {
                    Product p = new Product();
                    p.setCategoryID(Integer.parseInt(request.getParameter("categoryID")));
                    p.setName(request.getParameter("name"));
                    p.setSku(request.getParameter("sku"));
                    p.setPrice(new BigDecimal(request.getParameter("price")));
                    p.setCostPrice(new BigDecimal(request.getParameter("costPrice")));
                    p.setStockAlertQty(Integer.parseInt(request.getParameter("stockAlertQty")));
                    p.setStatus(request.getParameter("status"));
                    productDAO.insert(p);
                }
                case "delete" -> productDAO.delete(Integer.parseInt(request.getParameter("id")));
            }
            response.sendRedirect(request.getContextPath() + "/product-management");
        } catch (Exception e) {
            throw new ServletException("Error processing request", e);
        }
    }
}

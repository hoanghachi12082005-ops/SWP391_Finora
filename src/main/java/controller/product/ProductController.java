package controller.product;

import controller.common.BaseController;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import model.Product;
import service.product.ProductService;

@WebServlet(name = "ProductController", urlPatterns = {"/products", "/products/detail", "/products/add", "/products/edit", "/products/search", "/products/filter", "/products/showcase", "/products/import-receipt"})
public class ProductController extends BaseController {
    private ProductService productService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/products": forward(request, response, "products/list"); break;
        case "/products/detail": forward(request, response, "products/detail"); break;
        case "/products/add": forward(request, response, "products/add"); break;
        case "/products/edit": forward(request, response, "products/edit"); break;
        case "/products/search": forward(request, response, "products/list"); break;
        case "/products/filter": forward(request, response, "products/list"); break;
        case "/products/showcase": forward(request, response, "products/showcase"); break;
        case "/products/import-receipt": forward(request, response, "products/import-receipt"); break;
            default: forward(request, response, "products/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) { doGet(request, response); return; }
        switch (action) {
        case "add":
        case "insert": {
            Product p = new Product();
            p.setProductCodebar(request.getParameter("productCodebar"));
            p.setProductName(request.getParameter("productName"));
            String catId = request.getParameter("categoryId");
            if (catId != null && !catId.isEmpty()) p.setCategoryId(Integer.parseInt(catId));
            String unitId = request.getParameter("unitId");
            if (unitId != null && !unitId.isEmpty()) p.setUnitId(Integer.parseInt(unitId));
            String price = request.getParameter("sellingPrice");
            if (price != null && !price.isEmpty()) p.setSellingPrice(new BigDecimal(price));
            productService.insert(p);
            break;
        }
        case "edit":
        case "update": {
            Product p = new Product();
            p.setProductId(Integer.parseInt(request.getParameter("productId")));
            p.setProductCodebar(request.getParameter("productCodebar"));
            p.setProductName(request.getParameter("productName"));
            String catId = request.getParameter("categoryId");
            if (catId != null && !catId.isEmpty()) p.setCategoryId(Integer.parseInt(catId));
            String unitId = request.getParameter("unitId");
            if (unitId != null && !unitId.isEmpty()) p.setUnitId(Integer.parseInt(unitId));
            String price = request.getParameter("sellingPrice");
            if (price != null && !price.isEmpty()) p.setSellingPrice(new BigDecimal(price));
            productService.update(p);
            break;
        }
        case "delete": {
            String id = request.getParameter("productId");
            if (id != null && !id.isEmpty()) productService.delete(Integer.parseInt(id));
            break;
        }
        default: break;
        }
        response.sendRedirect(request.getContextPath() + "/products");
    }
}

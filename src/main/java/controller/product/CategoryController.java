package controller.product;

import controller.common.BaseController;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Category;
import service.product.CategoryService;

@WebServlet(name = "CategoryController", urlPatterns = {"/categories", "/categories/add", "/categories/edit", "/categories/search", "/categories/filter"})
public class CategoryController extends BaseController {
    private CategoryService categoryService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        categoryService = new CategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/categories": forward(request, response, "categories/list"); break;
        case "/categories/add": forward(request, response, "categories/add"); break;
        case "/categories/edit": forward(request, response, "categories/edit"); break;
        case "/categories/search": forward(request, response, "categories/list"); break;
        case "/categories/filter": forward(request, response, "categories/list"); break;
            default: forward(request, response, "categories/list"); break;
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
            Category c = new Category();
            c.setCategoryName(request.getParameter("categoryName"));
            c.setDescription(request.getParameter("description"));
            String parentId = request.getParameter("parentCategoryId");
            if (parentId != null && !parentId.isEmpty()) c.setParentCategoryId(Integer.parseInt(parentId));
            c.setStatus(request.getParameter("status"));
            categoryService.insert(c);
            break;
        }
        case "edit":
        case "update": {
            Category c = new Category();
            c.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
            c.setCategoryName(request.getParameter("categoryName"));
            c.setDescription(request.getParameter("description"));
            String parentId = request.getParameter("parentCategoryId");
            if (parentId != null && !parentId.isEmpty()) c.setParentCategoryId(Integer.parseInt(parentId));
            c.setStatus(request.getParameter("status"));
            categoryService.update(c);
            break;
        }
        case "delete": {
            String id = request.getParameter("categoryId");
            if (id != null && !id.isEmpty()) categoryService.delete(Integer.parseInt(id));
            break;
        }
        default: break;
        }
        response.sendRedirect(request.getContextPath() + "/categories");
    }
}

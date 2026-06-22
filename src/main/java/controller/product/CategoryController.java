package controller.product;

import controller.common.BaseController;
import dao.product.CategoryDAO;
import model.Category;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "CategoryController", urlPatterns = {"/category"})
public class CategoryController extends BaseController {

    private CategoryDAO categoryDAO;

    @Override
    public void init() throws ServletException {
        categoryDAO = new CategoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.getSession().setAttribute("canManageCategory", true); // Ensure access

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "list":
                listCategories(request, response);
                break;
            default:
                listCategories(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");

        switch (action) {
            case "add":
                addCategory(request, response);
                break;
            case "update":
                updateCategory(request, response);
                break;
            default:
                doGet(request, response);
                break;
        }
    }

    private void listCategories(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String parentName = request.getParameter("parentName");
        
        int page = 1;
        int limit = 10;
        
        try {
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        List<Category> categories = categoryDAO.getCategories(keyword, status, parentName, page, limit);
        int totalItems = categoryDAO.countCategories(keyword, status, parentName);
        int totalRootCategories = categoryDAO.countRootCategories(keyword, status, parentName);
        int totalLinkedProducts = categoryDAO.countLinkedProducts(keyword, status, parentName);
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        
        List<Category> parentOptions = categoryDAO.getAllCategories();
        
        request.setAttribute("categories", categories);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalRootCategories", totalRootCategories);
        request.setAttribute("totalLinkedProducts", totalLinkedProducts);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("parentNameFilter", parentName);
        request.setAttribute("parentOptions", parentOptions);
        
        forward(request, response, "categories/list.jsp");
    }

    private void addCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String parentName = request.getParameter("parentName");
            String status = request.getParameter("status");

            if (name == null || name.trim().isEmpty()) {
                request.getSession().setAttribute("message", "Tên nhóm hàng không được để trống.");
                request.getSession().setAttribute("messageType", "danger");
                redirect(response, request.getContextPath() + "/category");
                return;
            }

            if (categoryDAO.isCategoryNameExists(name.trim(), null)) {
                request.getSession().setAttribute("message", "Tên nhóm hàng đã tồn tại.");
                request.getSession().setAttribute("messageType", "warning");
                redirect(response, request.getContextPath() + "/category");
                return;
            }

            Category category = new Category();
            category.setName(name.trim());
            category.setDescription(description != null ? description.trim() : null);
            category.setStatus(status != null ? status : "active");

            if (parentName != null && !parentName.trim().isEmpty()) {
                Integer parentId = categoryDAO.getCategoryIdByName(parentName.trim());
                if (parentId != null) {
                    category.setParentId(parentId);
                }
            }

            boolean success = categoryDAO.addCategory(category);
            if (success) {
                request.getSession().setAttribute("message", "Thêm nhóm hàng thành công.");
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "Thêm nhóm hàng thất bại.");
                request.getSession().setAttribute("messageType", "danger");
            }

        } catch (Exception e) {
            request.getSession().setAttribute("message", "Đã xảy ra lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "danger");
        }
        
        redirect(response, request.getContextPath() + "/category");
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String categoryIdStr = request.getParameter("categoryId");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String parentName = request.getParameter("parentName");
            String status = request.getParameter("status");

            if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
                 redirect(response, request.getContextPath() + "/category");
                 return;
            }
            
            int categoryId = Integer.parseInt(categoryIdStr);

            if (name == null || name.trim().isEmpty()) {
                request.getSession().setAttribute("message", "Tên nhóm hàng không được để trống.");
                request.getSession().setAttribute("messageType", "danger");
                redirect(response, request.getContextPath() + "/category");
                return;
            }

            if (categoryDAO.isCategoryNameExists(name.trim(), categoryId)) {
                request.getSession().setAttribute("message", "Tên nhóm hàng đã tồn tại.");
                request.getSession().setAttribute("messageType", "warning");
                redirect(response, request.getContextPath() + "/category");
                return;
            }

            Category category = categoryDAO.getCategoryById(categoryId);
            if (category == null) {
                request.getSession().setAttribute("message", "Không tìm thấy nhóm hàng.");
                request.getSession().setAttribute("messageType", "danger");
                redirect(response, request.getContextPath() + "/category");
                return;
            }

            category.setName(name.trim());
            category.setDescription(description != null ? description.trim() : null);
            category.setStatus(status != null ? status : "active");

            if (parentName != null && !parentName.trim().isEmpty()) {
                Integer parentId = categoryDAO.getCategoryIdByName(parentName.trim());
                if (parentId != null) {
                    if (parentId == categoryId) {
                        request.getSession().setAttribute("message", "Nhóm cha không thể là chính nó.");
                        request.getSession().setAttribute("messageType", "warning");
                        redirect(response, request.getContextPath() + "/category");
                        return;
                    }
                    if (categoryDAO.isDescendant(parentId, categoryId)) {
                        request.getSession().setAttribute("message", "Nhóm cha không thể là nhóm con của nhóm hiện tại.");
                        request.getSession().setAttribute("messageType", "warning");
                        redirect(response, request.getContextPath() + "/category");
                        return;
                    }
                    category.setParentId(parentId);
                } else {
                     category.setParentId(null);
                }
            } else {
                category.setParentId(null);
            }

            boolean success = categoryDAO.updateCategory(category);
            if (success) {
                request.getSession().setAttribute("message", "Cập nhật nhóm hàng thành công.");
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "Cập nhật nhóm hàng thất bại.");
                request.getSession().setAttribute("messageType", "danger");
            }

        } catch (Exception e) {
            request.getSession().setAttribute("message", "Đã xảy ra lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "danger");
        }
        
        redirect(response, request.getContextPath() + "/category");
    }
}

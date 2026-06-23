package controller.product;

import controller.common.BaseController;
import dao.product.CategoryDAO;
import model.Category;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
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
            case "delete":
                if (request.getParameter("id") != null) {
                    boolean success = categoryDAO.deleteCategory(Integer.parseInt(request.getParameter("id")));
                    if (success) {
                        request.getSession().setAttribute("message", "Đã xóa danh mục thành công.");
                        request.getSession().setAttribute("messageType", "success");
                    } else {
                        request.getSession().setAttribute("message", "Không thể xóa danh mục này do đang có sản phẩm hoặc danh mục con phụ thuộc.");
                        request.getSession().setAttribute("messageType", "danger");
                    }
                }
                String pageStr = request.getParameter("page");
                response.sendRedirect(request.getContextPath() + "/category" + (pageStr != null && !pageStr.isBlank() ? "?page=" + pageStr : ""));
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

    private void listCategories(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String parentName = request.getParameter("parentName");
        
        int page = parseOrDefault(request.getParameter("page"), 1);
        int limit = parseOrDefault(request.getParameter("limit"), 10);
        List<Category> allRawCategories = categoryDAO.getAllCategories();
        
        List<Category> filteredList = filterCategories(allRawCategories, keyword, status);
        int totalItems = filteredList.size();
        int fromIndex = 0;
        int toIndex = 0;
        int totalPages = 1;

        
        
        String percentAction = request.getParameter("percentAction");
        if (percentAction != null && !percentAction.isEmpty()) {
            limit = Math.max(1, (int) Math.ceil(totalItems * 0.3)); 
            totalPages = (int) Math.ceil((double) totalItems / limit);
            
            if ("first".equals(percentAction)) {
                fromIndex = 0;
                toIndex = Math.min(limit, totalItems);
                page = 1;
            } else if ("middle".equals(percentAction)) {
                fromIndex = Math.max(0, (totalItems - limit) / 2);
                toIndex = Math.min(fromIndex + limit, totalItems);
                page = Math.max(1, (fromIndex / limit) + 1); 
            } else if ("last".equals(percentAction)) {
                fromIndex = Math.max(0, totalItems - limit);
                toIndex = totalItems;
                page = totalPages;
            }
        } else {
            totalPages = (int) Math.ceil((double) totalItems / limit);
            if (page > totalPages && totalPages > 0) page = totalPages;
            
            fromIndex = Math.max(0, (page - 1) * limit);
            toIndex = Math.min(fromIndex + limit, totalItems);
        }
        List<Category> paginatedList;
        if (fromIndex <= totalItems && fromIndex <= toIndex) {
            paginatedList = filteredList.subList(fromIndex, toIndex);
        } else {
            paginatedList = new ArrayList<>();
        }

        int totalRootCategories = 0;
        int totalLinkedProducts = 0;
        for (Category c : filteredList) {
            if (c.getParentName() == null || c.getParentName().trim().isEmpty()) totalRootCategories++;
            totalLinkedProducts += c.getProductCount();
        }

        request.setAttribute("categories", paginatedList);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalRootCategories", totalRootCategories);
        request.setAttribute("totalLinkedProducts", totalLinkedProducts);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        request.setAttribute("currentLimit", limit);
        
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedStatus", status);  
        forward(request, response, "categories/list.jsp");
    }


    private int parseOrDefault(String param, int defaultValue) {
        try {
            return (param != null && !param.trim().isEmpty()) ? Integer.parseInt(param) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private List<Category> filterCategories(List<Category> rawList, String keyword, String status) {
        List<Category> result = new ArrayList<>();
        for (Category c : rawList) {
            boolean isMatch = true;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase(); 
                String catName = "";
                if (c.getName() != null) {
                    catName = c.getName().toLowerCase();
                }
                String catDesc = "";
                if (c.getDescription() != null) {
                    catDesc = c.getDescription().toLowerCase();
                }
                String catParentName = "";
                if (c.getParentName() != null) {
                    catParentName = c.getParentName().toLowerCase();
                }
                if (catName.contains(kw) == false && catDesc.contains(kw) == false && catParentName.contains(kw) == false) {
                    isMatch = false;
                }
            }
            if (status != null && !status.trim().isEmpty()) {
                String st = status.trim();
                if (st.equals(c.getStatus()) == false) {
                    isMatch = false;
                }
            }
            if (isMatch == true) {
                result.add(c);
            }
        }
        return result;
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

package controller.supplier;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import model.Supplier;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import service.supplier.SupplierService;
import dao.product.ProductDAO;

@WebServlet("/suppliers")
public class SupplierServlet extends HttpServlet {

    SupplierService service = new SupplierService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "get-products-api": {
                try {
                    int supplierId = Integer.parseInt(request.getParameter("id"));
                    List<dto.inventory.ImportProductDTO.SupplierInfo> list = service.getSupplierProductsHistory(supplierId);
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        dto.inventory.ImportProductDTO.SupplierInfo item = list.get(i);
                        json.append("{");
                        json.append("\"productId\":").append(item.getSupplierId()).append(",");
                        json.append("\"productName\":\"").append(item.getSupplierName().replace("\"", "\\\"")).append("\",");
                        json.append("\"importPrice\":").append(item.getImportPrice());
                        json.append("}");
                        if (i < list.size() - 1) json.append(",");
                    }
                    json.append("]");
                    
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(json.toString());
                } catch (Exception e) {
                    response.setContentType("application/json");
                    response.getWriter().write("[]");
                }
                return;
            }
            case "get-active-products-api": {
                try {
                    ProductDAO productDAO = new ProductDAO();
                    List<model.Product> products = productDAO.findAll(0, 10000, "", "ACTIVE", null, null);
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < products.size(); i++) {
                        model.Product p = products.get(i);
                        json.append("{");
                        json.append("\"productId\":").append(p.getProductId()).append(",");
                        json.append("\"productName\":\"").append(p.getProductName().replace("\"", "\\\"")).append("\",");
                        json.append("\"sellingPrice\":").append(p.getSellingPrice() != null ? p.getSellingPrice() : 0);
                        json.append("}");
                        if (i < products.size() - 1) json.append(",");
                    }
                    json.append("]");
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(json.toString());
                } catch (Exception e) {
                    response.setContentType("application/json");
                    response.getWriter().write("[]");
                }
                return;
            }
            case "add-product-api": {
                response.setContentType("application/json");
                try {
                    int supplierId = Integer.parseInt(request.getParameter("supplierId"));
                    int productId = Integer.parseInt(request.getParameter("productId"));
                    double price = Double.parseDouble(request.getParameter("price"));
                    boolean success = service.addOrUpdateSupplierProduct(supplierId, productId, price);
                    response.getWriter().write("{\"success\":" + success + "}");
                } catch (Exception e) {
                    response.getWriter().write("{\"success\":false}");
                }
                return;
            }
            case "delete-product-api": {
                response.setContentType("application/json");
                try {
                    int supplierId = Integer.parseInt(request.getParameter("supplierId"));
                    int productId = Integer.parseInt(request.getParameter("productId"));
                    boolean success = service.deleteSupplierProduct(supplierId, productId);
                    response.getWriter().write("{\"success\":" + success + "}");
                } catch (Exception e) {
                    response.getWriter().write("{\"success\":false}");
                }
                return;
            }
            case "update-price-api": {
                response.setContentType("application/json");
                try {
                    int supplierId = Integer.parseInt(request.getParameter("supplierId"));
                    int productId = Integer.parseInt(request.getParameter("productId"));
                    double price = Double.parseDouble(request.getParameter("price"));
                    boolean success = service.addOrUpdateSupplierProduct(supplierId, productId, price);
                    response.getWriter().write("{\"success\":" + success + "}");
                } catch (Exception e) {
                    response.getWriter().write("{\"success\":false}");
                }
                return;
            }
            case "create":
                request.getRequestDispatcher("/views/suppliers/create.jsp").forward(request, response);
                break;
            case "edit":
                int id = Integer.parseInt(request.getParameter("id"));
                Supplier supplier = service.getById(id);
                request.setAttribute("supplier", supplier);
                request.getRequestDispatcher("/views/suppliers/edit.jsp").forward(request, response);
                break;
            case "delete":
                boolean success = service.delete(Integer.parseInt(request.getParameter("id")));
                if (success) {
                    request.getSession().setAttribute("message", "Xóa đối tác thành công.");
                    request.getSession().setAttribute("messageType", "success");
                } else {
                    request.getSession().setAttribute("message", "Không thể xóa đối tác này do đang có dữ liệu liên quan (giao dịch, đơn hàng...)!");
                    request.getSession().setAttribute("messageType", "danger");
                }
                String page = request.getParameter("page");
                String keyword = request.getParameter("keyword");
                StringBuilder redirect = new StringBuilder("suppliers?");
                if (page != null && !page.isBlank()) redirect.append("page=").append(page).append("&");
                if (keyword != null && !keyword.isBlank()) redirect.append("keyword=").append(keyword);
                
                if (redirect.charAt(redirect.length() - 1) == '&' || redirect.charAt(redirect.length() - 1) == '?') {
                    redirect.deleteCharAt(redirect.length() - 1);
                }
                response.sendRedirect(redirect.toString());
                break;
            default:
                listSupplier(request, response);
        }
    }

    private void listSupplier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");

        if (keyword == null) {
            keyword = "";
        }
        if (status == null) {
            status = "";
        }

        keyword = keyword.trim().replaceAll("\\s+", " ");

        int total = service.countSuppliers(keyword, status);

        String pageSizeOption = request.getParameter("pageSize");
        if (pageSizeOption == null || pageSizeOption.trim().isEmpty()) {
            pageSizeOption = "10";
        }

        int pageSize = 10;
        String option = pageSizeOption.trim().toLowerCase();
        if ("30p".equals(option)) {
            pageSize = Math.max(1, (int) Math.ceil(total * 0.3));
        } else if ("50p".equals(option)) {
            pageSize = Math.max(1, (int) Math.ceil(total * 0.5));
        } else {
            try {
                pageSize = Integer.parseInt(option);
            } catch (NumberFormatException e) {
                pageSize = 10;
            }
        }

        int page = 1;
        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception ignored) {
        }

        int totalPage = (int) Math.ceil((double) total / pageSize);
        int activeCount = service.countActiveSuppliers();
        int inactiveCount = service.countInactiveSuppliers();

        List<Supplier> list = service.getSuppliersPaging(keyword, status, page, pageSize);

        request.setAttribute("list", list);
        request.setAttribute("page", page);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("pageSizeOption", pageSizeOption);
        request.setAttribute("totalSupplier", total);
        request.setAttribute("activeCount", activeCount);
        request.setAttribute("inactiveCount", inactiveCount);

        request.getRequestDispatcher("/views/suppliers/list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        Supplier s = new Supplier();

        s.setName(request.getParameter("name"));
        s.setPhone(request.getParameter("phone"));
        s.setAddress(request.getParameter("address"));
        s.setStatus(request.getParameter("status"));

        if ("create".equals(action)) {
            if (service.existsByNameOrPhone(s.getName(), s.getPhone())) {
                request.getSession().setAttribute("message", "Nhà cung cấp đã có trong hệ thống.");
                request.getSession().setAttribute("messageType", "danger");
                request.getSession().setAttribute("modalAction", "create");
                request.getSession().setAttribute("modalName", s.getName());
                request.getSession().setAttribute("modalPhone", s.getPhone());
                request.getSession().setAttribute("modalAddress", s.getAddress());
                request.getSession().setAttribute("modalStatus", s.getStatus());
            } else {
                boolean success = service.save(s);
                if (success) {
                    request.getSession().setAttribute("message", "Thêm nhà cung cấp thành công.");
                    request.getSession().setAttribute("messageType", "success");
                } else {
                    request.getSession().setAttribute("message", "Không thể thêm nhà cung cấp.");
                    request.getSession().setAttribute("messageType", "danger");
                }
            }
        } else if ("edit".equals(action)) {
            s.setSupplierID(
                    Integer.parseInt(request.getParameter("id")));
            boolean success = service.save(s);
            if (success) {
                request.getSession().setAttribute("message", "Cập nhật nhà cung cấp thành công.");
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "Không thể cập nhật nhà cung cấp.");
                request.getSession().setAttribute("messageType", "danger");
            }

        }
        response.sendRedirect("suppliers");
    }
}

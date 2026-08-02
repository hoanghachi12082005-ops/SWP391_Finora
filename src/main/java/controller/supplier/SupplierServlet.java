package controller.supplier;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import model.Supplier;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import service.supplier.SupplierService;
import service.supplier.SupplierProductService;
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
            case "manage-products": {
                try {
                    int supplierId = Integer.parseInt(request.getParameter("id"));
                    Supplier supplier = service.getById(supplierId);
                    if (supplier == null) {
                        response.sendRedirect("suppliers");
                        return;
                    }
                    ProductDAO productDAO = new ProductDAO();
                    List<model.Product> allProducts = productDAO.findAll(0, 10000, "", "ACTIVE", null, null);
                    SupplierProductService spService = new SupplierProductService();
                    Map<Integer, Double> linkedProducts = spService.getLinkedProductsWithPrices(supplierId);
                    
                    request.setAttribute("supplier", supplier);
                    request.setAttribute("allProducts", allProducts);
                    request.setAttribute("linkedProducts", linkedProducts);
                    request.getRequestDispatcher("/views/suppliers/manage_products.jsp").forward(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.sendRedirect("suppliers");
                }
                return;
            }
            case "create":
            case "edit":
            case "delete": {
                model.Employee currentUser = (model.Employee) request.getSession().getAttribute("currentUser");
                String roleName = (currentUser != null && currentUser.getRoleName() != null) ? currentUser.getRoleName().trim() : "";
                boolean canEdit = "Admin".equalsIgnoreCase(roleName) || "Owner".equalsIgnoreCase(roleName);
                if (!canEdit) {
                    request.getSession().setAttribute("message", "Bạn không có quyền thực hiện thao tác này.");
                    request.getSession().setAttribute("messageType", "danger");
                    response.sendRedirect("suppliers");
                    return;
                }
                if ("create".equals(action)) {
                    request.getRequestDispatcher("/views/suppliers/create.jsp").forward(request, response);
                } else if ("edit".equals(action)) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    Supplier supplier = service.getById(id);
                    request.setAttribute("supplier", supplier);
                    request.getRequestDispatcher("/views/suppliers/edit.jsp").forward(request, response);
                } else if ("delete".equals(action)) {
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
                }
                break;
            }
            case "export": {
                exportSupplierExcel(request, response);
                return;
            }
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

        int page = parseInt(request.getParameter("page"), 1);
        int sizeValue = parseInt(request.getParameter("sizeValue"), 30);

        util.pagination.PaginationHelper.PageResult pr = util.pagination.PaginationHelper.compute(total, page, sizeValue);
        pr.setAttributes(request);

        int activeCount = service.countActiveSuppliers();
        int inactiveCount = service.countInactiveSuppliers();

        List<Supplier> list = service.getSuppliersPaging(keyword, status, pr.getCurrentPage(), pr.getPageSize());

        request.setAttribute("list", list);
        request.setAttribute("page", pr.getCurrentPage());
        request.setAttribute("totalPage", pr.getTotalPages());
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("totalSupplier", total);
        request.setAttribute("activeCount", activeCount);
        request.setAttribute("inactiveCount", inactiveCount);
        request.setAttribute("baseUrl", request.getContextPath() + "/suppliers");

        request.getRequestDispatcher("/views/suppliers/list.jsp").forward(request, response);
    }

    private void exportSupplierExcel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String keyword = request.getParameter("keyword");
            String status = request.getParameter("status");

            if (keyword == null) {
                keyword = "";
            }
            if (status == null) {
                status = "";
            }

            keyword = keyword.trim().replaceAll("\\s+", " ");

            List<Supplier> allSuppliers = service.getSuppliersPaging(keyword, status, 1, 1000000);

            String generatedBy = "Unknown";
            model.Employee currentUser = (model.Employee) request.getSession().getAttribute("currentUser");
            if (currentUser != null) {
                generatedBy = currentUser.getFullName();
            }

            byte[] excelBytes = util.report.ExcelExportUtil.generateSupplierReport(
                    generatedBy, allSuppliers, keyword);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    util.report.ExportUtil.buildExportFileName("SupplierReport") + ".xlsx\"");
            response.setContentLength(excelBytes.length);
            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Xuất Excel thất bại: " + e.getMessage());
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        model.Employee currentUser = (model.Employee) request.getSession().getAttribute("currentUser");
        String roleName = (currentUser != null && currentUser.getRoleName() != null) ? currentUser.getRoleName().trim() : "";
        boolean canEdit = "Admin".equalsIgnoreCase(roleName) || "Owner".equalsIgnoreCase(roleName);
        if (!canEdit) {
            request.getSession().setAttribute("message", "Bạn không có quyền thực hiện thao tác này.");
            request.getSession().setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/suppliers");
            return;
        }

        String action = request.getParameter("action");

        if ("save-products".equals(action)) {
            try {
                int supplierId = Integer.parseInt(request.getParameter("id"));
                String[] productIdsStr = request.getParameterValues("productIds");
                List<Integer> productIds = new java.util.ArrayList<>();
                List<Double> prices = new java.util.ArrayList<>();
                
                if (productIdsStr != null) {
                    for (String pidStr : productIdsStr) {
                        int pid = Integer.parseInt(pidStr);
                        String priceStr = request.getParameter("price_" + pid);
                        double price = 0;
                        if (priceStr != null && !priceStr.isBlank()) {
                            price = Double.parseDouble(priceStr);
                        }
                        productIds.add(pid);
                        prices.add(price);
                    }
                }
                
                SupplierProductService spService = new SupplierProductService();
                boolean success = spService.saveAssociations(supplierId, productIds, prices);
                if (success) {
                    request.getSession().setAttribute("message", "Cập nhật liên kết sản phẩm thành công.");
                    request.getSession().setAttribute("messageType", "success");
                } else {
                    request.getSession().setAttribute("message", "Không thể cập nhật liên kết sản phẩm.");
                    request.getSession().setAttribute("messageType", "danger");
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("message", "Đã xảy ra lỗi khi lưu liên kết.");
                request.getSession().setAttribute("messageType", "danger");
            }
            response.sendRedirect(request.getContextPath() + "/suppliers");
            return;
        }

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
        response.sendRedirect(request.getContextPath() + "/suppliers");
    }
}

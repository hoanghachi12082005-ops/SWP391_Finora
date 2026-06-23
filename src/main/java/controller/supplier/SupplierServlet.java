package controller.supplier;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import model.Supplier;

import java.io.IOException;
import java.util.List;
import service.supplier.SupplierService;

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
                service.delete(Integer.parseInt(request.getParameter("id")));
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

        int page = 1;
        int pageSize = 10;

        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception ignored) {
        }

        int total = service.countSuppliers(keyword, status);
        int totalPage = (int) Math.ceil((double) total / pageSize);

        List<Supplier> list = service.getSuppliersPaging(keyword, status, page, pageSize);

        request.setAttribute("list", list);
        request.setAttribute("page", page);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("totalSupplier", total);

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
            service.insert(s);
        } else if ("edit".equals(action)) {
            s.setSupplierID(
                    Integer.parseInt(request.getParameter("id")));
            service.update(s);
        }
        response.sendRedirect("suppliers");
    }
}

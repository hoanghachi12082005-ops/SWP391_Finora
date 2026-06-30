package controller.supplier;

import service.supplier.SupplierService;
import model.Supplier;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SupplierController", urlPatterns = {"/suppliers", "/suppliers/create", "/suppliers/edit", "/suppliers/search", "/suppliers/filter"})
public class SupplierController extends jakarta.servlet.http.HttpServlet {
    private SupplierService supplierService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        supplierService = new SupplierService();
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/suppliers": request.setAttribute("suppliers", supplierService.findAll()); break;
        case "/suppliers/search": {
            String q = request.getParameter("q");
            if (q != null && !q.trim().isEmpty()) {
                request.setAttribute("suppliers", supplierService.findAll().stream()
                    .filter(s -> s.getSupplierName().toLowerCase().contains(q.toLowerCase()))
                    .toList());
            } else {
                request.setAttribute("suppliers", supplierService.findAll());
            }
            break;
        }
        case "/suppliers/filter": {
            String status = request.getParameter("status");
            if (status != null && !status.trim().isEmpty()) {
                request.setAttribute("suppliers", supplierService.findAll().stream()
                    .filter(s -> status.equals(s.getStatus()))
                    .toList());
            } else {
                request.setAttribute("suppliers", supplierService.findAll());
            }
            break;
        }
        }
        request.getRequestDispatcher("/WEB-INF/views/suppliers/list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/suppliers/create": {
            Supplier s = new Supplier();
            s.setSupplierName(request.getParameter("supplier_name"));
            s.setPhoneNumber(request.getParameter("phone_number"));
            s.setAddress(request.getParameter("address"));
            s.setStatus(request.getParameter("status"));
            supplierService.insert(s);
            response.sendRedirect(request.getContextPath() + "/suppliers");
            return;
        }
        case "/suppliers/edit": {
            Supplier s = supplierService.findById(Integer.parseInt(request.getParameter("supplier_id")));
            if (s != null) {
                s.setSupplierName(request.getParameter("supplier_name"));
                s.setPhoneNumber(request.getParameter("phone_number"));
                s.setAddress(request.getParameter("address"));
                s.setStatus(request.getParameter("status"));
                supplierService.update(s);
            }
            response.sendRedirect(request.getContextPath() + "/suppliers");
            return;
        }
        case "/suppliers/delete": {
            supplierService.softDelete(Integer.parseInt(request.getParameter("supplier_id")));
            response.sendRedirect(request.getContextPath() + "/suppliers");
            return;
        }
        default:
            response.sendRedirect(request.getContextPath() + "/suppliers");
        }
    }
}

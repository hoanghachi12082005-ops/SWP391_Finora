package controller.supplier;

import controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SupplierController", urlPatterns = {"/suppliers", "/suppliers/create", "/suppliers/edit", "/suppliers/search", "/suppliers/filter"})
public class SupplierController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/suppliers": forward(request, response, "suppliers/list"); break;
        case "/suppliers/create": forward(request, response, "suppliers/create"); break;
        case "/suppliers/edit": forward(request, response, "suppliers/edit"); break;
        case "/suppliers/search": forward(request, response, "suppliers/list"); break;
        case "/suppliers/filter": forward(request, response, "suppliers/list"); break;
            default: forward(request, response, "suppliers/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

package controller.product;

import controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CategoryController", urlPatterns = {"/categories", "/categories/add", "/categories/edit", "/categories/search", "/categories/filter"})
public class CategoryController extends BaseController {
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
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

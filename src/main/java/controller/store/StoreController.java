package controller.store;

import controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "StoreController", urlPatterns = {"/stores", "/stores/add", "/stores/edit", "/stores/search", "/stores/filter", "/stores/detail"})
public class StoreController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/stores": forward(request, response, "stores/list"); break;
        case "/stores/add": forward(request, response, "stores/add"); break;
        case "/stores/edit": forward(request, response, "stores/edit"); break;
        case "/stores/search": forward(request, response, "stores/list"); break;
        case "/stores/filter": forward(request, response, "stores/list"); break;
        case "/stores/detail": forward(request, response, "stores/detail"); break;
            default: forward(request, response, "stores/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

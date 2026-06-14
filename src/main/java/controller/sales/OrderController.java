package controller.sales;

import controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "OrderController", urlPatterns = {"/orders/create", "/orders/detail", "/orders/update", "/orders/cancel"})
public class OrderController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/orders/create": forward(request, response, "orders/create"); break;
        case "/orders/detail": forward(request, response, "orders/detail"); break;
        case "/orders/update": forward(request, response, "orders/update"); break;
        case "/orders/cancel": forward(request, response, "orders/cancel"); break;
            default: forward(request, response, "orders/create"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

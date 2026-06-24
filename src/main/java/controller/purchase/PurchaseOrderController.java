package       controller.purchase;

import       controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PurchaseOrderController", urlPatterns = {"/purchase-orders", "/purchase-orders/detail"})
public class PurchaseOrderController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/purchase-orders": forward(request, response, "purchase-orders/list"); break;
        case "/purchase-orders/detail": forward(request, response, "purchase-orders/detail"); break;
            default: forward(request, response, "purchase-orders/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

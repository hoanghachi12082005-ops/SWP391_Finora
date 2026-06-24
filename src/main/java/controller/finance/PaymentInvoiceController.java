package       controller.finance;

import       controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PaymentInvoiceController", urlPatterns = {"/payments", "/invoices"})
public class PaymentInvoiceController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/payments": forward(request, response, "payments/index"); break;
        case "/invoices": forward(request, response, "invoices/list"); break;
            default: forward(request, response, "payments/index"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

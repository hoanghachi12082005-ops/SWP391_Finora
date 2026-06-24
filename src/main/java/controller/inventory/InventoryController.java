package       controller.inventory;

import       controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "InventoryController", urlPatterns = {"/inventory/dashboard", "/inventory/import", "/inventory/export", "/inventory/transfer", "/inventory/report", "/inventory/adjustment"})
public class InventoryController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/inventory/dashboard": forward(request, response, "inventory/dashboard"); break;
        case "/inventory/import": forward(request, response, "inventory/import"); break;
        case "/inventory/export": forward(request, response, "inventory/export"); break;
        case "/inventory/transfer": forward(request, response, "inventory/transfer"); break;
        case "/inventory/report": forward(request, response, "inventory/report"); break;
        case "/inventory/adjustment": forward(request, response, "products/stock-adjustment"); break;
            default: forward(request, response, "inventory/dashboard"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

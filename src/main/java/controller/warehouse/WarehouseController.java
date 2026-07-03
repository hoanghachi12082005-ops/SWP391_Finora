package controller.warehouse;

import controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Warehouse Controller — xử lý luồng quản lý kho
 * Dành cho: Warehouse Staff, Store Manager, Owner, Admin
 */
@WebServlet(name = "WarehouseController", urlPatterns = {"/warehouse/dashboard", "/warehouse/import", "/warehouse/export", "/warehouse/stock"})
public class WarehouseController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/warehouse/dashboard":
                forward(request, response, "warehouse/dashboard");
                break;
            case "/warehouse/import":
                forward(request, response, "warehouse/import");
                break;
            case "/warehouse/export":
                forward(request, response, "warehouse/export");
                break;
            case "/warehouse/stock":
                forward(request, response, "warehouse/stock");
                break;
            default:
                forward(request, response, "warehouse/dashboard");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu Kho. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

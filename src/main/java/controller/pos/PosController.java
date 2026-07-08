package controller.pos;

import controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * POS (Point of Sale) Controller — xử lý luồng bán hàng tại quầy
 * Dành cho: Sales Staff, Store Manager, Owner, Admin
 */
@WebServlet(name = "PosController", urlPatterns = {"/pos/sale", "/pos/history", "/pos/shift"})
public class PosController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/pos/sale":
                forward(request, response, "pos/sale");
                break;
            case "/pos/history":
                forward(request, response, "pos/history");
                break;
            case "/pos/shift":
                forward(request, response, "pos/shift");
                break;
            default:
                forward(request, response, "pos/sale");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu POS. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

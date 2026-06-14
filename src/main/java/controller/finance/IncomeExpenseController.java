package controller.finance;

import controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "IncomeExpenseController", urlPatterns = {"/income", "/expenses", "/expenses/add"})
public class IncomeExpenseController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/income": forward(request, response, "income/list"); break;
        case "/expenses": forward(request, response, "expenses/list"); break;
        case "/expenses/add": forward(request, response, "expenses/add"); break;
            default: forward(request, response, "income/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

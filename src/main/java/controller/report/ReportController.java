package com.storemanagement.controller.report;

import com.storemanagement.controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ReportController", urlPatterns = {"/reports/employee-sales", "/reports/customer-loyal", "/reports/sales-by-store", "/reports/inventory", "/reports/export"})
public class ReportController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/reports/employee-sales": forward(request, response, "reports/employee-sales"); break;
        case "/reports/customer-loyal": forward(request, response, "reports/customer-loyal"); break;
        case "/reports/sales-by-store": forward(request, response, "reports/sales-by-store"); break;
        case "/reports/inventory": forward(request, response, "reports/inventory"); break;
        case "/reports/export": forward(request, response, "reports/export"); break;
            default: forward(request, response, "reports/employee-sales"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

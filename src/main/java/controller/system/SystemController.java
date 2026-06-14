package com.storemanagement.controller.system;

import com.storemanagement.controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SystemController", urlPatterns = {"/activity-log", "/notifications", "/configuration/business"})
public class SystemController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/activity-log": forward(request, response, "activity-log/list"); break;
        case "/notifications": forward(request, response, "notifications/list"); break;
        case "/configuration/business": forward(request, response, "configuration/business"); break;
            default: forward(request, response, "activity-log/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

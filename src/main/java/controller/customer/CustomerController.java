package com.storemanagement.controller.customer;

import com.storemanagement.controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CustomerController", urlPatterns = {"/customers", "/customers/add", "/customers/edit", "/customers/search", "/customers/filter", "/customers/detail", "/customers/loyal-ranking"})
public class CustomerController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/customers": forward(request, response, "customers/list"); break;
        case "/customers/add": forward(request, response, "customers/add"); break;
        case "/customers/edit": forward(request, response, "customers/edit"); break;
        case "/customers/search": forward(request, response, "customers/list"); break;
        case "/customers/filter": forward(request, response, "customers/list"); break;
        case "/customers/detail": forward(request, response, "customers/detail"); break;
        case "/customers/loyal-ranking": forward(request, response, "customers/ranking"); break;
            default: forward(request, response, "customers/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

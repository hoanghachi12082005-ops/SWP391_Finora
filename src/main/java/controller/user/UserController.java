package com.storemanagement.controller.user;

import com.storemanagement.controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserController", urlPatterns = {"/users", "/users/create", "/users/edit", "/users/search", "/users/filter", "/users/lock-unlock", "/roles"})
public class UserController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/users": forward(request, response, "users/list"); break;
        case "/users/create": forward(request, response, "users/create"); break;
        case "/users/edit": forward(request, response, "users/edit"); break;
        case "/users/search": forward(request, response, "users/list"); break;
        case "/users/filter": forward(request, response, "users/list"); break;
        case "/users/lock-unlock": forward(request, response, "users/list"); break;
        case "/roles": forward(request, response, "roles/list"); break;
            default: forward(request, response, "users/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

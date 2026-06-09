package com.storemanagement.controller.product;

import com.storemanagement.controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ProductController", urlPatterns = {"/products", "/products/detail", "/products/add", "/products/edit", "/products/search", "/products/filter", "/products/showcase", "/products/import-receipt"})
public class ProductController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/products": forward(request, response, "products/list"); break;
        case "/products/detail": forward(request, response, "products/detail"); break;
        case "/products/add": forward(request, response, "products/add"); break;
        case "/products/edit": forward(request, response, "products/edit"); break;
        case "/products/search": forward(request, response, "products/list"); break;
        case "/products/filter": forward(request, response, "products/list"); break;
        case "/products/showcase": forward(request, response, "products/showcase"); break;
        case "/products/import-receipt": forward(request, response, "products/import-receipt"); break;
            default: forward(request, response, "products/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}

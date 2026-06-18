package controller.auth;

import controller.common.BaseController;

import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AuthController", urlPatterns = {"/login", "/register", "/forgot-password", "/logout"})
public class AuthController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/register": forward(request, response, "auth/register"); break;
            case "/forgot-password": forward(request, response, "auth/forgot-password"); break;
            case "/logout":
                request.getSession().invalidate();
                redirect(request, response, "/login");
                break;
            default: forward(request, response, "auth/login"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/login".equals(path)) {
            User demoUser = new User();
            demoUser.setUsername(request.getParameter("username"));
            demoUser.setRole("OWNER");
            request.getSession().setAttribute("currentUser", demoUser);
            redirect(request, response, "/dashboard/owner");
            return;
        }
        request.setAttribute("message", "Chức năng đang là khung mẫu. Hãy viết service/dao để xử lý thật.");
        doGet(request, response);
    }
}

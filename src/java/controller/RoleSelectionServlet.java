package controller;

import util.RoleContextUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Shared development role selector endpoint. */
public class RoleSelectionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RoleContextUtil.setCurrentRole(request, request.getParameter("role"));
        String returnUrl = request.getParameter("returnUrl");
        if (returnUrl == null || returnUrl.isBlank() || !returnUrl.startsWith(request.getContextPath())) {
            returnUrl = request.getContextPath() + "/dashboard";
        }
        response.sendRedirect(returnUrl);
    }
}

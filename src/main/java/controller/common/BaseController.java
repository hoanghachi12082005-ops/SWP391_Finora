package controller.common;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class BaseController extends HttpServlet {
    
    protected void forward(HttpServletRequest request, HttpServletResponse response, String jspPath) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + jspPath).forward(request, response);
    }

    protected void redirect(HttpServletResponse response, String url) throws IOException {
        response.sendRedirect(url);
    }

    protected void sendJsonResponse(HttpServletResponse response, String jsonString) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(jsonString);
        out.flush();
    }
}

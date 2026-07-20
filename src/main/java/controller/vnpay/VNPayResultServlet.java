package controller.vnpay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet({"/payment/success", "/payment/failed"})
public class VNPayResultServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/payment/success".equals(path)) {
            req.setAttribute("status", "success");
            req.setAttribute("message", "Giao dịch thành công!");
        } else {
            req.setAttribute("status", "failed");
            req.setAttribute("message", "Giao dịch thất bại!");
        }

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }
}

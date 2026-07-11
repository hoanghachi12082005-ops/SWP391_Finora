package controller.sales;

import model.CartItem;
import model.Employee;
import model.OrderTab;
import dao.system.VatSettingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/print/preview")
public class PrintPreviewServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String tabIdStr = req.getParameter("tabId");
        int tabId = 1;
        try {
            if (tabIdStr != null) tabId = Integer.parseInt(tabIdStr);
        } catch (NumberFormatException ignored) {}

        HttpSession session = req.getSession();
        @SuppressWarnings("unchecked")
        Map<Integer, OrderTab> cartTabs = (Map<Integer, OrderTab>) session.getAttribute("cartTabs");
        OrderTab tab = null;
        if (cartTabs != null) {
            tab = cartTabs.get(tabId);
        }

        Employee emp = (Employee) session.getAttribute("employee");
        String empName = emp != null ? emp.getFullName() : "Nhân viên";

        double vatRate = VatSettingDAO.getVatRate();
        int vatPercent = (int) Math.round(vatRate * 100);

        PrintWriter out = resp.getWriter();
        out.write("<!DOCTYPE html>");
        out.write("<html><head><meta charset='utf-8'><title>In thử hóa đơn</title>");
        out.write("<style>");
        out.write("body { font-family: monospace; font-size: 12px; margin: 20px; width: 300px; }");
        out.write(".header { text-align: center; margin-bottom: 15px; }");
        out.write(".divider { border-bottom: 1px dashed #000; margin: 10px 0; }");
        out.write("table { width: 100%; border-collapse: collapse; }");
        out.write(".text-right { text-align: right; }");
        out.write(".bold { font-weight: bold; }");
        out.write(".footer { text-align: center; margin-top: 15px; font-size: 10px; }");
        out.write("</style></head><body onload='window.print()'>");

        out.write("<div class='header'>");
        out.write("<h2 style='margin:0;'>FINORA STORE</h2>");
        out.write("<p style='margin:3px 0;'>HÓA ĐƠN TẠM TÍNH (IN THỬ)</p>");
        out.write("<p style='margin:3px 0;'>Tab đơn: Đơn " + tabId + "</p>");
        out.write("</div>");

        out.write("<div class='divider'></div>");
        out.write("<p>NV: " + empName + "</p>");
        out.write("<p>Khách: " + (tab != null && tab.getSelectedCustomer() != null ? tab.getSelectedCustomer().getFullName() : "Khách vãng lai") + "</p>");
        out.write("<div class='divider'></div>");

        out.write("<table>");
        out.write("<thead><tr><th align='left'>Tên SP</th><th align='right'>SL</th><th align='right'>T.Tiền</th></tr></thead>");
        out.write("<tbody>");
        
        if (tab != null && !tab.getItems().isEmpty()) {
            for (CartItem item : tab.getItems()) {
                out.write("<tr>");
                out.write("<td>" + item.getProductName() + "<br/>" + String.format("%,.0f", item.getSellingPrice()) + "</td>");
                out.write("<td align='right' valign='bottom'>" + item.getQuantity() + "</td>");
                out.write("<td align='right' valign='bottom'>" + String.format("%,.0f", item.getLineTotal()) + "</td>");
                out.write("</tr>");
            }
            
            out.write("</tbody></table>");
            out.write("<div class='divider'></div>");

            out.write("<table>");
            out.write("<tr><td>Cộng tiền hàng:</td><td class='text-right'>" + String.format("%,.0f ₫", tab.getSubtotal()) + "</td></tr>");
            if (tab.getDiscountAmount() > 0) {
                out.write("<tr><td>Chiết khấu:</td><td class='text-right'>-" + String.format("%,.0f ₫", tab.getDiscountAmount()) + "</td></tr>");
            }
            out.write("<tr><td>Thuế VAT (" + vatPercent + "%):</td><td class='text-right'>" + String.format("%,.0f ₫", tab.getVatAmount()) + "</td></tr>");
            out.write("<tr class='bold'><td>TỔNG CỘNG:</td><td class='text-right'>" + String.format("%,.0f ₫", tab.getTotalAmount()) + "</td></tr>");
            out.write("</table>");
        } else {
            out.write("<tr><td colspan='3' align='center'>Không có sản phẩm</td></tr></tbody></table>");
        }

        out.write("<div class='divider'></div>");
        out.write("<div class='footer'>");
        out.write("<p>Cảm ơn Quý khách. Hẹn gặp lại!</p>");
        out.write("<p>Powered by Finora</p>");
        out.write("</div>");

        out.write("</body></html>");
    }
}

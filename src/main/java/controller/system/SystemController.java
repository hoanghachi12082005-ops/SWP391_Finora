package controller.system;

import controller.common.BaseController;
import dao.system.VatSettingDAO;
import dao.customer.LoyaltyPointSettingDAO;
import model.VatSetting;
import model.LoyaltyPointSetting;
import model.Employee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "SystemController", urlPatterns = {"/notifications", "/configuration/business"})
public class SystemController extends BaseController {

    private final LoyaltyPointSettingDAO loyaltyDao = new LoyaltyPointSettingDAO();
    private final VatSettingDAO vatDao = new VatSettingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/notifications":
                forward(request, response, "notifications/list");
                break;
            case "/configuration/business":
                // Load loyalty setting
                LoyaltyPointSetting loyaltySetting = loyaltyDao.getSetting();
                request.setAttribute("loyaltySetting", loyaltySetting);
                // Load VAT setting
                VatSetting vatSetting = vatDao.getSetting();
                request.setAttribute("vatSetting", vatSetting);
                forward(request, response, "configuration/business");
                break;
            default:
                forward(request, response, "notifications/list");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        HttpSession session = request.getSession();
        Employee emp = (Employee) session.getAttribute("currentUser");

        if ("/configuration/business".equals(path)) {
            // Xử lý cập nhật điểm tích lũy
            String amountPerPointStr = request.getParameter("amountPerPoint");
            if (amountPerPointStr != null && !amountPerPointStr.isBlank()) {
                try {
                    LoyaltyPointSetting loyaltySetting = loyaltyDao.getSetting();
                    loyaltySetting.setAmountPerPoint(new java.math.BigDecimal(amountPerPointStr));
                    if (emp != null) loyaltySetting.setUpdatedBy(emp.getEmployeeId());
                    loyaltyDao.update(loyaltySetting);
                    session.setAttribute("successMessage", "Cập nhật cài đặt điểm tích lũy thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi cập nhật điểm tích lũy: " + e.getMessage());
                }
            }

            // Xử lý cập nhật VAT
            String vatPercentageStr = request.getParameter("vatPercentage");
            if (vatPercentageStr != null && !vatPercentageStr.isBlank()) {
                try {
                    VatSetting vatSetting = vatDao.getSetting();
                    vatSetting.setVatPercentage(Double.parseDouble(vatPercentageStr));
                    if (emp != null) vatSetting.setUpdatedBy(emp.getEmployeeId());
                    vatDao.update(vatSetting);
                    session.setAttribute("successMessage", "Cập nhật cấu hình VAT thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi cập nhật VAT: " + e.getMessage());
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/configuration/business");
    }
}

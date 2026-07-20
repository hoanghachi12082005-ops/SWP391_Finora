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

    private final LoyaltyPointSettingDAO pointSettingDao = new LoyaltyPointSettingDAO();
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
                // Load cấu hình điểm từ bảng loyalty_point_setting
                LoyaltyPointSetting pointSetting = pointSettingDao.getSetting();
                request.setAttribute("pointSetting", pointSetting);

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

        if ("/configuration/business".equals(path)) {
            // Xử lý cập nhật tỉ lệ tích điểm (? VNĐ = 1 điểm)
            String earnValueStr = request.getParameter("earnValue");
            if (earnValueStr != null && !earnValueStr.isBlank()) {
                try {
                    Employee emp = (Employee) session.getAttribute("currentUser");
                    LoyaltyPointSetting setting = pointSettingDao.getSetting();
                    setting.setAmountPerPoint(new java.math.BigDecimal(earnValueStr));
                    if (emp != null) setting.setUpdatedBy(emp.getEmployeeId());
                    pointSettingDao.upsert(setting);
                    session.setAttribute("successMessage", "Cập nhật tỉ lệ tích điểm thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi cập nhật tỉ lệ tích điểm: " + e.getMessage());
                }
            }

            // Xử lý cập nhật tỉ lệ đổi điểm (1 điểm = ? VNĐ)
            String redeemValueStr = request.getParameter("redeemValue");
            if (redeemValueStr != null && !redeemValueStr.isBlank()) {
                try {
                    Employee emp = (Employee) session.getAttribute("currentUser");
                    LoyaltyPointSetting setting = pointSettingDao.getSetting();
                    setting.setPointToCurrency(new java.math.BigDecimal(redeemValueStr));
                    if (emp != null) setting.setUpdatedBy(emp.getEmployeeId());
                    pointSettingDao.upsert(setting);
                    session.setAttribute("successMessage", "Cập nhật tỉ lệ đổi điểm thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi cập nhật tỉ lệ đổi điểm: " + e.getMessage());
                }
            }

            // Xử lý cập nhật VAT
            String vatPercentageStr = request.getParameter("vatPercentage");
            if (vatPercentageStr != null && !vatPercentageStr.isBlank()) {
                try {
                    Employee emp = (Employee) session.getAttribute("currentUser");
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

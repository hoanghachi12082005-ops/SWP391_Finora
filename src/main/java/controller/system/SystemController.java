package controller.system;

import controller.common.BaseController;
import dao.system.VatSettingDAO;
import dao.sales.VoucherDAO;
import model.VatSetting;
import model.Voucher;
import model.Employee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "SystemController", urlPatterns = {"/notifications", "/configuration/business"})
public class SystemController extends BaseController {

    private final VoucherDAO voucherDao = new VoucherDAO();
    private final VatSettingDAO vatDao = new VatSettingDAO();
    private static final String POINT_CONFIG_CODE = "POINT_CONFIG";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/notifications":
                forward(request, response, "notifications/list");
                break;
            case "/configuration/business":
                // Load cấu hình đổi điểm từ voucher POINT_CONFIG
                Voucher pointConfig = voucherDao.getByCode(POINT_CONFIG_CODE);
                if (pointConfig == null) {
                    pointConfig = new Voucher();
                    pointConfig.setVoucherCode(POINT_CONFIG_CODE);
                    pointConfig.setVoucherName("Cấu hình đổi điểm ra tiền");
                    pointConfig.setDiscountType("FIXED");
                    pointConfig.setDiscountValue(1.00);
                }
                request.setAttribute("pointConfig", pointConfig);

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
            // Xử lý cập nhật tỉ lệ đổi điểm
            String pointValueStr = request.getParameter("pointValue");
            if (pointValueStr != null && !pointValueStr.isBlank()) {
                try {
                    double newValue = Double.parseDouble(pointValueStr);
                    voucherDao.updateDiscountValue(POINT_CONFIG_CODE, newValue);
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

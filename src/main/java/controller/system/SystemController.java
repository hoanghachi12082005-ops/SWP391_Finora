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
    private static final String POINT_REDEEM_CODE = "POINT_CONFIG";
    private static final String POINT_EARN_CODE = "POINT_EARN_CONFIG";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/notifications":
                forward(request, response, "notifications/list");
                break;
            case "/configuration/business":
                // Load cấu hình đổi điểm (1 điểm = ? VNĐ)
                Voucher pointRedeem = voucherDao.getByCode(POINT_REDEEM_CODE);
                if (pointRedeem == null) {
                    pointRedeem = new Voucher();
                    pointRedeem.setVoucherCode(POINT_REDEEM_CODE);
                    pointRedeem.setVoucherName("Cấu hình đổi điểm ra tiền");
                    pointRedeem.setDiscountType("FIXED");
                    pointRedeem.setDiscountValue(1.00);
                }
                request.setAttribute("pointRedeem", pointRedeem);

                // Load cấu hình tích điểm (? VNĐ = 1 điểm)
                Voucher pointEarn = voucherDao.getByCode(POINT_EARN_CODE);
                if (pointEarn == null) {
                    pointEarn = new Voucher();
                    pointEarn.setVoucherCode(POINT_EARN_CODE);
                    pointEarn.setVoucherName("Cấu hình tích điểm");
                    pointEarn.setDiscountType("FIXED");
                    pointEarn.setDiscountValue(100000);
                }
                request.setAttribute("pointEarn", pointEarn);

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
            // Xử lý cập nhật tỉ lệ đổi điểm (1 điểm = ? VNĐ)
            String redeemValueStr = request.getParameter("redeemValue");
            if (redeemValueStr != null && !redeemValueStr.isBlank()) {
                try {
                    double newValue = Double.parseDouble(redeemValueStr);
                    voucherDao.updateDiscountValue(POINT_REDEEM_CODE, newValue);
                    session.setAttribute("successMessage", "Cập nhật tỉ lệ đổi điểm thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi cập nhật tỉ lệ đổi điểm: " + e.getMessage());
                }
            }

            // Xử lý cập nhật tỉ lệ tích điểm (? VNĐ = 1 điểm)
            String earnValueStr = request.getParameter("earnValue");
            if (earnValueStr != null && !earnValueStr.isBlank()) {
                try {
                    double newValue = Double.parseDouble(earnValueStr);
                    voucherDao.updateDiscountValue(POINT_EARN_CODE, newValue);
                    session.setAttribute("successMessage", "Cập nhật tỉ lệ tích điểm thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi cập nhật tỉ lệ tích điểm: " + e.getMessage());
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

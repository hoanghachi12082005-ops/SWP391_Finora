package controller.system;

import controller.common.BaseController;
import dao.system.VatSettingDAO;
import dao.customer.LoyaltyPointSettingDAO;
import dao.product.CategoryDAO;
import model.VatSetting;
import model.LoyaltyPointSetting;
import model.Category;
import model.Employee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "SystemController", urlPatterns = {"/notifications", "/configuration/business"})
public class SystemController extends BaseController {

    private final LoyaltyPointSettingDAO pointSettingDao = new LoyaltyPointSettingDAO();
    private final VatSettingDAO vatDao = new VatSettingDAO();
    private final CategoryDAO categoryDao = new CategoryDAO();

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

                // Load tất cả VAT settings (cho từng category + default)
                List<VatSetting> vatSettings = vatDao.getAllSettings();
                request.setAttribute("vatSettings", vatSettings);

                // Load danh sách category active để cấu hình VAT
                List<Category> categories = categoryDao.getActiveCategories();
                request.setAttribute("categories", categories);

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

            // Xử lý cập nhật VAT (mặc định chung)
            String vatPercentageStr = request.getParameter("vatPercentage");
            if (vatPercentageStr != null && !vatPercentageStr.isBlank()) {
                try {
                    Employee emp = (Employee) session.getAttribute("currentUser");
                    VatSetting vatSetting = vatDao.getSetting();
                    vatSetting.setVatPercentage(Double.parseDouble(vatPercentageStr));
                    if (emp != null) vatSetting.setUpdatedBy(emp.getEmployeeId());
                    vatDao.update(vatSetting);
                    session.setAttribute("successMessage", "Cập nhật cấu hình VAT mặc định thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi cập nhật VAT: " + e.getMessage());
                }
            }

            // Xử lý thêm/cập nhật VAT cho từng category
            String categoryIdStr = request.getParameter("categoryId");
            String categoryVatStr = request.getParameter("categoryVatPercentage");
            String deleteCategoryVat = request.getParameter("deleteCategoryVat");
            if (deleteCategoryVat != null && !deleteCategoryVat.isBlank()) {
                try {
                    int catId = Integer.parseInt(deleteCategoryVat);
                    vatDao.deleteByCategoryId(catId);
                    session.setAttribute("successMessage", "Xóa cấu hình VAT cho ngành hàng thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi xóa VAT ngành hàng: " + e.getMessage());
                }
            } else if (categoryIdStr != null && !categoryIdStr.isBlank()
                    && categoryVatStr != null && !categoryVatStr.isBlank()) {
                try {
                    Employee emp = (Employee) session.getAttribute("currentUser");
                    int catId = Integer.parseInt(categoryIdStr);
                    double catVat = Double.parseDouble(categoryVatStr);

                    // Tìm xem đã có setting cho category này chưa
                    VatSetting existing = vatDao.getSettingByCategory(catId);
                    if (existing != null && existing.getCategoryId() != null && existing.getCategoryId() == catId) {
                        // Update
                        existing.setVatPercentage(catVat);
                        if (emp != null) existing.setUpdatedBy(emp.getEmployeeId());
                        vatDao.update(existing);
                    } else {
                        // Insert mới
                        VatSetting newSetting = new VatSetting();
                        newSetting.setVatPercentage(catVat);
                        newSetting.setCategoryId(catId);
                        if (emp != null) newSetting.setUpdatedBy(emp.getEmployeeId());
                        vatDao.insert(newSetting);
                    }
                    session.setAttribute("successMessage", "Cập nhật cấu hình VAT cho ngành hàng thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("errorMessage", "Lỗi cập nhật VAT ngành hàng: " + e.getMessage());
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/configuration/business");
    }
}

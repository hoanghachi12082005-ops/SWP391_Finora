package controller.system;

import controller.common.BaseController;
import dao.customer.LoyaltyPointSettingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import model.Employee;
import model.LoyaltyPointSetting;
import service.system.ActivityLogService;

@WebServlet(name = "SystemController", urlPatterns = {"/activity-log", "/notifications", "/configuration/business"})
public class SystemController extends BaseController {

    private LoyaltyPointSettingDAO loyaltyDAO;
    private ActivityLogService activityLogService;

    @Override
    public void init() throws ServletException {
        loyaltyDAO = new LoyaltyPointSettingDAO();
        activityLogService = new ActivityLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/configuration/business".equals(path)) {
            request.setAttribute("loyaltySetting", loyaltyDAO.getSetting());
            forward(request, response, "configuration/business");
            return;
        }
        switch (path) {
            case "/activity-log": forward(request, response, "activity-log/list"); break;
            case "/notifications": forward(request, response, "notifications/list"); break;
            default: forward(request, response, "activity-log/list"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/configuration/business".equals(path)) {
            updateLoyaltySetting(request);
            response.sendRedirect(request.getContextPath() + "/configuration/business");
            return;
        }
        request.setAttribute("message", "Received data. Connect Service/DAO for actual processing.");
        doGet(request, response);
    }

    private void updateLoyaltySetting(HttpServletRequest request) {
        String amountStr = request.getParameter("amountPerPoint");
        if (amountStr == null || amountStr.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "Amount per point is required.");
            return;
        }
        try {
            BigDecimal amountPerPoint = new BigDecimal(amountStr.trim());
            if (amountPerPoint.compareTo(BigDecimal.ZERO) <= 0) {
                request.getSession().setAttribute("errorMessage", "Amount per point must be positive.");
                return;
            }
            LoyaltyPointSetting setting = loyaltyDAO.getSetting();
            setting.setAmountPerPoint(amountPerPoint);
            HttpSession session = request.getSession(false);
            if (session != null) {
                Employee user = (Employee) session.getAttribute("currentUser");
                if (user != null) {
                    setting.setUpdatedBy(user.getEmployeeID());
                }
            }
            boolean ok = loyaltyDAO.update(setting);
            if (ok) {
                HttpSession session2 = request.getSession(false);
                if (session2 != null) {
                    Employee user = (Employee) session2.getAttribute("currentUser");
                    if (user != null) {
                        activityLogService.log(user.getEmployeeID(), "UPDATE", "LoyaltyPointSetting", setting.getSettingId(), null, amountPerPoint.toString());
                    }
                }
                request.getSession().setAttribute("successMessage", "Loyalty point setting updated.");
            } else {
                request.getSession().setAttribute("errorMessage", "Cannot update setting.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Invalid number format.");
        }
    }
}

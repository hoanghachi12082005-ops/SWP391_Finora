    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
     */
    package controller.user;

    import java.io.IOException;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServlet;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import jakarta.servlet.annotation.WebServlet;
    /**
     *
     * @author PCQN
     */
import dao.sales.OrderDAO;
import dao.user.UserManagementDao;
import dao.user.ProfileDao;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import util.auth.AuthUtil;
import util.email.EmailUtil;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;
import util.security.PasswordUtil;
import service.system.ActivityLogService;

    @WebServlet(name = "OwnerUserServlet", urlPatterns = {"/owner/emp"})
    public class OwnerUserServlet extends HttpServlet {

    private UserManagementDao ownerUserDao;
    private ProfileDao profileDao;
    private ActivityLogService activityLogService;

    @Override
    public void init() throws ServletException {
        ownerUserDao = new UserManagementDao();
        profileDao = new ProfileDao();
        activityLogService = new ActivityLogService();
    }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            if (!isOwner(request, response)) {
                return;
            }

            String action = getParam(request, "action", "list");

            switch (action) {
                case "add":
                    request.setAttribute("formMode", "add");
                    break;

                case "edit":
                    loadSelectedEmployee(request, "editingUser");
                    request.setAttribute("formMode", "edit");
                    break;

                case "reset":
                    loadSelectedEmployee(request, "resetUser");
                    request.setAttribute("formMode", "reset");
                    break;

                case "detail":
                    viewEmployeeProfile(request, response);
                    return;

                default:
                    request.setAttribute("formMode", "list");
                    break;
            }

            loadPageData(request);

            request.setAttribute("pageTitle", "Nhân viên toàn chi nhánh");
            request.setAttribute("pageSubtitle", "Danh sách nhân viên toàn bộ các chi nhánh");
            request.setAttribute("addButtonText", "Thêm nhân viên");
            request.setAttribute("baseUrl", request.getContextPath() + "/owner/emp");

            request.setAttribute("showBranch", true);
            request.setAttribute("canCreate", true);
            request.setAttribute("canEdit", true);
            request.setAttribute("canLock", true);
            request.setAttribute("canResetPassword", true);

            // FIX: JSP nằm tại webapp/views/users/ không phải /WEB-INF/views/
            request.getRequestDispatcher("/views/users/user-list.jsp")
                    .forward(request, response);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            if (!isOwner(request, response)) {
                return;
            }

            String action = getParam(request, "action", "list");

            switch (action) {
                case "create":
                case "update":
                    saveEmployee(request, action);
                    break;

                case "lock":
                    updateStatus(request, "INACTIVE");
                    break;

                case "unlock":
                    updateStatus(request, "ACTIVE");
                    break;

                case "resetPassword":
                    resetPassword(request);
                    break;

                default:
                    setFlash(request, "errorMessage", "Invalid action.");
                    break;
            }

            response.sendRedirect(request.getContextPath() + "/owner/emp");
        }

        private void loadPageData(HttpServletRequest request) {
            String keyword = request.getParameter("keyword");
            String branchId = request.getParameter("branchId");
            String roleId = request.getParameter("roleId");
            String status = request.getParameter("status");

            int page = parseInt(request.getParameter("page"), 1);
            int sizeValue = parseInt(request.getParameter("sizeValue"), 30);

            int totalRecords = ownerUserDao.countEmployees(keyword, branchId, roleId, status);
            PageResult pr = PaginationHelper.compute(totalRecords, page, sizeValue);
            pr.setAttributes(request);

            request.setAttribute(
                    "users",
                    ownerUserDao.getEmployees(keyword, branchId, roleId, status, pr.getCurrentPage(), pr.getPageSize())
            );

            request.setAttribute("branches", ownerUserDao.getAllBranches());
            request.setAttribute("roles", ownerUserDao.getEmployeeRoles());

            request.setAttribute("keyword", keyword);
            request.setAttribute("branchFilter", parseInt(branchId, -1));
            request.setAttribute("roleFilter", parseInt(roleId, -1));
            request.setAttribute("statusFilter", status);

            request.setAttribute("employeeOverview", ownerUserDao.getOwnerEmployeeOverview());
        }

        private void viewEmployeeProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

            int employeeId = parseInt(request.getParameter("id"), -1);

            if (employeeId <= 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee ID.");
                return;
            }

            Employee profile = ownerUserDao.getEmployeeById(employeeId);

            if (profile == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found.");
                return;
            }

            request.setAttribute("profile", profile);
            request.setAttribute("salesSummary", profileDao.getEmployeeSalesSummary(employeeId));
            request.setAttribute("orderHistory", new OrderDAO().findByEmployeeId(employeeId));
            request.setAttribute("showSalesSection", true);

            request.setAttribute("readOnlyProfile", true);
            request.setAttribute("profileTitle", "Hồ sơ nhân viên");
            request.setAttribute("profileSubtitle", "Owner views employee information and sales performance");
            request.setAttribute("backUrl", request.getContextPath() + "/owner/emp");

            // FIX: Đường dẫn đúng: /views/profile/ (có chữ 's', không phải /view/)
            request.getRequestDispatcher("/views/profile/profile.jsp")
                    .forward(request, response);
        }

        private void loadSelectedEmployee(HttpServletRequest request, String attributeName) {
            int employeeId = parseInt(request.getParameter("id"), -1);
            if (employeeId > 0) {
                request.setAttribute(attributeName, ownerUserDao.getEmployeeById(employeeId));
            }
        }

        private void saveEmployee(HttpServletRequest request, String action) {
            boolean isUpdate = "update".equals(action);

            int employeeId = parseInt(request.getParameter("employeeId"), -1);
            if (employeeId <= 0) {
                employeeId = parseInt(request.getParameter("employeeID"), -1);
            }

            String fullName = trim(request.getParameter("fullName"));
            String email = trim(request.getParameter("email"));
            String phone = trim(request.getParameter("phone"));
            String status = trim(request.getParameter("status"));

            int branchId = parseInt(request.getParameter("branchId"), -1);
            int roleId = parseInt(request.getParameter("roleId"), -1);

            if (isUpdate && employeeId <= 0) {
                setFlash(request, "errorMessage", "Invalid employee ID.");
                return;
            }

            if (isBlank(fullName) || isBlank(email) || branchId <= 0 || roleId <= 0) {
                setFlash(request, "errorMessage", "Please enter full name, email, branch and a role.");
                return;
            }

            Integer excludeEmployeeId = isUpdate ? employeeId : null;

            if (ownerUserDao.isEmailExists(email, phone, excludeEmployeeId)) {
                setFlash(request, "errorMessage", "Email/Phone already exists.");
                return;
            }

            Employee employee = new Employee();

            if (isUpdate) {
                employee.setEmployeeID(employeeId);
            }

            employee.setFullName(fullName);
            employee.setEmail(email);
            employee.setPhone(phone);
            employee.setBranchID(branchId);
            employee.setRoleID(roleId);

            boolean success;

            if (isUpdate) {
                success = ownerUserDao.updateEmployee(employee);
            } else {
                String autoGeneratedPassword = EmailUtil.generateRandomPassword();

                boolean isMailSent = EmailUtil.sendPasswordEmail(
                        email.trim(),
                        fullName.trim(),
                        autoGeneratedPassword
                );

                if (!isMailSent) {
                    setFlash(request, "errorMessage", "Cannot send password email. Please check email configuration.");
                    return;
                }

                String hashedPassword = PasswordUtil.hash(autoGeneratedPassword);

                success = ownerUserDao.addEmployee(employee, hashedPassword);
            }

            if (success) {
                Employee currentUser = getCurrentUser(request);
                if (isUpdate) {
                    activityLogService.log(currentUser.getEmployeeID(), "UPDATE", "Employee", employeeId, null, email);
                } else {
                    activityLogService.log(currentUser.getEmployeeID(), "CREATE", "Employee", null, null, email);
                }
            }

            setFlash(
                    request,
                    success ? "successMessage" : "errorMessage",
                    success
                            ? (isUpdate ? "Cập nhật tài khoản nhân viên thành công." : "Thêm tài khoản nhân viên thành công.")
                            : (isUpdate ? "Không thể cập nhật tài khoản nhân viên." : "Không thể thêm tài khoản nhân viên.")
            );
        }

        private void updateStatus(HttpServletRequest request, String status) {
            int employeeId = parseInt(request.getParameter("employeeId"), -1);
            if (employeeId <= 0) {
                employeeId = parseInt(request.getParameter("employeeID"), -1);
            }

            if (employeeId <= 0) {
                setFlash(request, "errorMessage", "ID nhân viên không hợp lệ.");
                return;
            }

            boolean success = ownerUserDao.updateEmployeeStatus(employeeId, status);

            if (success) {
                Employee currentUser = getCurrentUser(request);
                activityLogService.log(currentUser.getEmployeeID(), status.equals("ACTIVE") ? "UNLOCK" : "LOCK", "Employee", employeeId, null, null);
            }

            setFlash(
                    request,
                    success ? "successMessage" : "errorMessage",
                    success ? "Cập nhật trạng thái tài khoản thành công." : "Không thể cập nhật trạng thái tài khoản."
            );
        }

        private void resetPassword(HttpServletRequest request) {
            int employeeId = parseInt(request.getParameter("employeeId"), -1);
            if (employeeId <= 0) {
                employeeId = parseInt(request.getParameter("employeeID"), -1);
            }

            if (employeeId <= 0) {
                setFlash(request, "errorMessage", "Dữ liệu đặt lại mật khẩu không hợp lệ.");
                return;
            }

            Employee employee = ownerUserDao.getEmployeeById(employeeId);

            if (employee == null) {
                setFlash(request, "errorMessage", "Không tìm thấy nhân viên hoặc bạn không có quyền đặt lại mật khẩu này.");
                return;
            }

            String autoGeneratedPassword = EmailUtil.generateRandomPassword();
            String hashedPassword = PasswordUtil.hash(autoGeneratedPassword);

            boolean success = ownerUserDao.resetEmployeePassword(employeeId, hashedPassword);

            if (!success) {
                setFlash(request, "errorMessage", "Cannot reset employee password.");
                return;
            }

            boolean isMailSent = EmailUtil.sendPasswordEmail(
                    employee.getEmail(),
                    employee.getFullName(),
                    autoGeneratedPassword
            );

            if (!isMailSent) {
                setFlash(request, "errorMessage", "Password was reset, but email could not be sent. Please check email configuration.");
                return;
            }

            setFlash(request, "successMessage", "Employee password reset successfully.");

            Employee currentUser = getCurrentUser(request);
            activityLogService.log(currentUser.getEmployeeID(), "RESET_PASSWORD", "Employee", employeeId, null, employee.getEmail());
        }

        private Employee getCurrentUser(HttpServletRequest request) {
            return AuthUtil.getCurrentUser(request);
        }

        private boolean isOwner(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
            return AuthUtil.requireAdminOrOwner(request, response);
        }

        private int[] parseRoleIds(String[] values) {
            if (values == null || values.length == 0) {
                return new int[0];
            }

            java.util.List<Integer> list = new java.util.ArrayList<Integer>();

            for (String value : values) {
                int id = parseInt(value, -1);

                if (id > 0 && !list.contains(id)) {
                    list.add(id);
                }
            }

            int[] roleIds = new int[list.size()];

            for (int i = 0; i < list.size(); i++) {
                roleIds[i] = list.get(i);
            }

            return roleIds;
        }

        private void setFlash(HttpServletRequest request, String key, String message) {
            request.getSession().setAttribute(key, message);
        }

        private String getParam(HttpServletRequest request, String name, String defaultValue) {
            String value = request.getParameter(name);
            return isBlank(value) ? defaultValue : value.trim();
        }

        private int parseInt(String value, int defaultValue) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        private String trim(String value) {
            return value == null ? null : value.trim();
        }

        private boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }
    }
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
    import dao.user.UserManagementDao;
    import dao.user.ProfileDao;
    import jakarta.servlet.http.HttpSession;
    import model.Employee;

    @WebServlet(name = "OwnerUserServlet", urlPatterns = {"/owner/emp"})
    public class OwnerUserServlet extends HttpServlet {

        private UserManagementDao ownerUserDao;
        private ProfileDao profileDao;

        @Override
        public void init() throws ServletException {
            ownerUserDao = new UserManagementDao();
            profileDao = new ProfileDao();
        }   

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            if (!isOwner(request, response)) {
                return;
            }

            String action = getParam(request, "action", "list");

                if ("detail".equals(action)) {
                viewEmployeeProfile(request, response);
                return;
            }else {
                request.setAttribute("formMode", "list");
            }

            loadPageData(request);

            request.setAttribute("pageTitle", "Employee Management");
            request.setAttribute("pageSubtitle", "Owner views and manages all employee accounts across branches");
            request.setAttribute("addButtonText", "Add Employee");
            request.setAttribute("baseUrl", request.getContextPath() + "/owner/emp");

            request.setAttribute("showBranch", true);
            request.setAttribute("canCreate", false);
            request.setAttribute("canEdit", false);
            request.setAttribute("canLock", false);
            request.setAttribute("canResetPassword", false);

            // FIX: JSP nằm tại webapp/views/users/ không phải /WEB-INF/views/
            request.getRequestDispatcher("/views/users/user-list.jsp")
                    .forward(request, response);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
        }

        private void loadPageData(HttpServletRequest request) {
            String keyword = request.getParameter("keyword");
            String branchId = request.getParameter("branchId");
            String roleId = request.getParameter("roleId");
            String status = request.getParameter("status");

            String pageSizeOption = getParam(request, "pageSize", "5");

            int totalUsers = ownerUserDao.countEmployees(keyword, branchId, roleId, status);
            int pageSize = resolvePageSize(pageSizeOption, totalUsers);

            int currentPage = parseInt(request.getParameter("page"), 1);

            if (currentPage < 1) {
                currentPage = 1;
            }

            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

            if (totalPages < 1) {
                totalPages = 1;
            }

            if (currentPage > totalPages) {
                currentPage = totalPages;
            }

            request.setAttribute(
                    "users",
                    ownerUserDao.getEmployees(keyword, branchId, roleId, status, currentPage, pageSize)
            );

            request.setAttribute("branches", ownerUserDao.getAllBranches());
            request.setAttribute("roles", ownerUserDao.getEmployeeRoles());

            request.setAttribute("keyword", keyword);
            request.setAttribute("branchFilter", parseInt(branchId, -1));
            request.setAttribute("roleFilter", parseInt(roleId, -1));
            request.setAttribute("statusFilter", status);

            request.setAttribute("currentPage", currentPage);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("pageSizeOption", pageSizeOption);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("totalPages", totalPages);

            request.setAttribute("employeeOverview", ownerUserDao.getOwnerEmployeeOverview());
        }
        private int resolvePageSize(String pageSizeOption, int totalUsers) {
            if (isBlank(pageSizeOption)) {
                return 5;
            }

            String option = pageSizeOption.trim().toLowerCase();

            if ("30p".equals(option) || "30%".equals(option) || "30".equals(option)) {
                return Math.max(1, (int) Math.ceil(totalUsers * 0.3));
            }

            if ("50p".equals(option) || "50%".equals(option) || "50".equals(option)) {
                return Math.max(1, (int) Math.ceil(totalUsers * 0.5));
            }

            int size = parseInt(option, 5);

            if (size != 5 && size != 10) {
                size = 5;
            }

            return size;
        }

    //    private int[] getSelectedRoleIds(HttpServletRequest request) {
    //        int employeeId = parseInt(request.getParameter("id"), -1);
    //
    //        if (employeeId <= 0) {
    //            return new int[0];
    //        }
    //
    //        
    //        int[] roleIds = new int[roleIdList.size()];
    //
    //        for (int i = 0; i < roleIdList.size(); i++) {
    //            roleIds[i] = roleIdList.get(i);
    //        }
    //
    //        return roleIds;
    //    }
    //    private void saveEmployee(HttpServletRequest request, String action) {
    //        boolean isUpdate = "update".equals(action);
    //
    //        int employeeId = parseInt(request.getParameter("employeeId"), -1);
    //
    //        String fullName = trim(request.getParameter("fullName"));
    //        String email = trim(request.getParameter("email"));
    //        String phone = trim(request.getParameter("phone"));
    //        String status = trim(request.getParameter("status"));
    //
    //        int branchId = parseInt(request.getParameter("branchId"), -1);
    //        int[] roleIds = parseRoleIds(request.getParameterValues("roleIds"));
    //
    //        if (isUpdate && employeeId <= 0) {
    //            setFlash(request, "errorMessage", "Invalid employee ID.");
    //            return;
    //        }
    //
    //        if (isBlank(fullName) || isBlank(email) || branchId <= 0 || roleIds.length == 0) {
    //            setFlash(request, "errorMessage", "Please enter full name, email, branch and at least one role.");
    //            return;
    //        }
    //
    //        Integer excludeEmployeeId = isUpdate ? employeeId : null;
    //
    //        if (ownerUserDao.isEmailExists(email, phone, excludeEmployeeId)) {
    //            setFlash(request, "errorMessage", "Email/Phone already exists.");
    //            return;
    //        }
    //
    //        Employee employee = new Employee();
    //
    //        if (isUpdate) {
    //            employee.setEmployeeID(employeeId);
    //        }
    //
    //        employee.setFullName(fullName);
    //        employee.setEmail(email);
    //        employee.setPhone(phone);
    //        employee.setBranchID(branchId);
    //        employee.setStatus(isBlank(status) ? "active" : status);
    //
    //        boolean success;
    //
    //        if (isUpdate) {
    //            success = ownerUserDao.updateEmployee(employee, roleIds);
    //        } else {
    //            String autoGeneratedPassword = EmailUtil.generateRandomPassword();
    //
    //            boolean isMailSent = EmailUtil.sendPasswordEmail(
    //                    email.trim(),
    //                    fullName.trim(),
    //                    autoGeneratedPassword
    //            );
    //
    //            if (!isMailSent) {
    //                setFlash(request, "errorMessage", "Cannot send password email. Please check email configuration.");
    //                return;
    //            }
    //
    //            String hashedPassword = PasswordUtil.hash(autoGeneratedPassword);
    //
    //            success = ownerUserDao.addEmployee(employee, hashedPassword, roleIds);
    //        }
    //
    //        setFlash(
    //                request,
    //                success ? "successMessage" : "errorMessage",
    //                success
    //                        ? (isUpdate ? "Employee account updated successfully." : "Employee account created successfully.")
    //                        : (isUpdate ? "Cannot update employee account." : "Cannot create employee account.")
    //        );
    //    }

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

            request.setAttribute("readOnlyProfile", true);
            request.setAttribute("profileTitle", "Employee Profile");
            request.setAttribute("profileSubtitle", "Owner views employee information and sales performance");
            request.setAttribute("backUrl", request.getContextPath() + "/owner/emp");

            // FIX: Đường dẫn đúng: /views/profile/ (có chữ 's', không phải /view/)
            request.getRequestDispatcher("/views/profile/profile.jsp")
                    .forward(request, response);
        }

    //    private void updateStatus(HttpServletRequest request, String status) {
    //        int employeeId = parseInt(request.getParameter("employeeId"), -1);
    //
    //        if (employeeId <= 0) {
    //            setFlash(request, "errorMessage", "Invalid employee ID.");
    //            return;
    //        }
    //
    //        boolean success = ownerUserDao.updateEmployeeStatus(employeeId, status);
    //
    //        setFlash(
    //                request,
    //                success ? "successMessage" : "errorMessage",
    //                success ? "Employee status updated successfully." : "Cannot update employee status."
    //        );
    //    }

    //    private void resetPassword(HttpServletRequest request) {
    //        int employeeId = parseInt(request.getParameter("employeeId"), -1);
    //
    //        if (employeeId <= 0) {
    //            setFlash(request, "errorMessage", "Invalid reset password data.");
    //            return;
    //        }
    //
    //        Employee employee = ownerUserDao.getEmployeeById(employeeId);
    //
    //        if (employee == null) {
    //            setFlash(request, "errorMessage", "Employee not found or you are not allowed to reset this account.");
    //            return;
    //        }
    //
    //        String autoGeneratedPassword = EmailUtil.generateRandomPassword();
    //        String hashedPassword = PasswordUtil.hash(autoGeneratedPassword);
    //
    //        boolean success = ownerUserDao.resetEmployeePassword(employeeId, hashedPassword);
    //
    //        if (!success) {
    //            setFlash(request, "errorMessage", "Cannot reset employee password.");
    //            return;
    //        }
    //
    //        boolean isMailSent = EmailUtil.sendPasswordEmail(
    //                employee.getEmail(),
    //                employee.getFullName(),
    //                autoGeneratedPassword
    //        );
    //
    //        if (!isMailSent) {
    //            setFlash(request, "errorMessage", "Password was reset, but email could not be sent. Please check email configuration.");
    //            return;
    //        }
    //
    //        setFlash(request, "successMessage", "Employee password reset successfully.");
    //    }

        private boolean isOwner(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

            HttpSession session = request.getSession(false);

            if (session == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }

            Employee currentUser =
                    (Employee) session.getAttribute("currentUser");

            if (currentUser == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }

            // FIX: Admin cũng có quyền xem danh sách nhân viên của Owner
            String roleName = currentUser.getRoleName();
            if (!"Owner".equalsIgnoreCase(roleName) && !"Admin".equalsIgnoreCase(roleName)) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Owner or Admin only."
                );
                return false;
            }

            return true;
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
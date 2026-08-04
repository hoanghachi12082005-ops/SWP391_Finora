package controller.user;

import dao.sales.OrderDAO;
import dao.user.ProfileDao;
import model.Employee;
import util.auth.AuthUtil;
import util.security.PasswordUtil;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.io.File;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
    
public class ProfileServlet extends HttpServlet {

    private ProfileDao profileDao;
    private OrderDAO orderDao;

    @Override
    public void init() throws ServletException {
        profileDao = new ProfileDao();
        orderDao = new OrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request, response)) {
            return;
        }

        loadProfile(request);

        request.getRequestDispatcher("/views/profile/profile.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request, response)) {
            return;
        }

        String action = request.getParameter("action");

        if ("updateProfile".equals(action)) {
            updateProfile(request);
        } else if ("changePassword".equals(action)) {
            changePassword(request);
        } else {
            setFlash(request, "errorMessage", "Thao tác không hợp lệ.");
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void loadProfile(HttpServletRequest request) {
        int employeeID = getLoggedInEmployeeID(request);

        Employee profile = profileDao.getProfileById(employeeID);
        
        request.setAttribute("profile", profile);
        request.setAttribute("salesSummary", profileDao.getEmployeeSalesSummary(employeeID));
        request.setAttribute("showSalesSection", isSalesStaff(profile));

        if (isSalesStaff(profile)) {
            int page = parseInt(request.getParameter("page"), 1);
            int sizeValue = parseInt(request.getParameter("sizeValue"), 10);

            int totalRecords = orderDao.countByEmployeeId(employeeID);
            PageResult pr = PaginationHelper.compute(totalRecords, page, sizeValue);
            pr.setAttributes(request);

            int offset = (pr.getCurrentPage() - 1) * pr.getPageSize();
            request.setAttribute("orderHistory", orderDao.findByEmployeeIdPaged(employeeID, offset, pr.getPageSize()));
            request.setAttribute("baseUrl", request.getContextPath() + "/profile");
        }

        request.setAttribute("readOnlyProfile", false);
        request.setAttribute("profileTitle", "Hồ sơ của tôi");
        request.setAttribute("profileSubtitle", "Xem thông tin cá nhân và hiệu suất bán hàng");
    }

    private boolean isSalesStaff(Employee profile) {
        if (profile == null) {
            return false;
        }
        if (profile.getRoleID() == 4) {
            return true;
        }
        String roleName = profile.getRoleName();
        return roleName != null && !roleName.trim().isEmpty() && roleName.toLowerCase().contains("sales");
    }

    private void updateProfile(HttpServletRequest request) {
        int employeeID = getLoggedInEmployeeID(request);

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));

        if (AuthUtil.isBlank(fullName) || AuthUtil.isBlank(email)) {
            setFlash(request, "errorMessage", "Vui lòng nhập họ tên và email.");
            return;
        }

        if (profileDao.isEmailExists(email, employeeID)) {
            setFlash(request, "errorMessage", "Email đã tồn tại.");
            return;
        }

        Employee currentProfile = profileDao.getProfileById(employeeID);
        String avatarUrl = currentProfile != null ? currentProfile.getAvatarUrl() : null;

        try {
            Part avatarPart = request.getPart("avatarFile");
            String uploadedAvatarUrl = saveAvatarFile(request, avatarPart, employeeID);
            if (uploadedAvatarUrl != null && !uploadedAvatarUrl.trim().isEmpty()) {
                avatarUrl = uploadedAvatarUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setFlash(request, "errorMessage", "Không thể tải ảnh đại diện.");
            return;
        }

        boolean success = profileDao.updateProfile(employeeID, fullName, email, phone, avatarUrl);

        if (success) {
            HttpSession session = request.getSession(false);

            if (session != null) {
                session.setAttribute("employeeName", fullName);

                Object employeeObj = session.getAttribute("currentUser");

                if (employeeObj instanceof Employee) {
                    Employee employee = (Employee) employeeObj;

                    employee.setFullName(fullName);
                    employee.setEmail(email);
                    employee.setPhone(phone);

                    if (!AuthUtil.isBlank(avatarUrl)) {
                        employee.setAvatarUrl(avatarUrl);
                    }

                    session.setAttribute("currentUser", employee);
                }
            }
        }

        setFlash(
                request,
                success ? "successMessage" : "errorMessage",
                success ? "Cập nhật hồ sơ thành công." : "Không thể cập nhật hồ sơ."
        );
    }
    private String saveAvatarFile(HttpServletRequest request, Part avatarPart, int employeeID)
        throws IOException {

        if (avatarPart == null || avatarPart.getSize() <= 0) {
            return null;
        }

        String submittedFileName = avatarPart.getSubmittedFileName();

        if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
            return null;
        }

        String lowerName = submittedFileName.toLowerCase();

        if (!(lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".webp"))) {
            throw new IOException("Only JPG, PNG, WEBP images are allowed.");
        }

        File folder = resolvePersistentUploadFolder(request);

        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Unable to create upload folder: " + folder.getAbsolutePath());
        }

        String extension = submittedFileName.substring(submittedFileName.lastIndexOf("."));
        String fileName = "employee_" + employeeID + "_" + System.currentTimeMillis() + extension;

        File file = new File(folder, fileName);

        try (var input = avatarPart.getInputStream();
             var output = new java.io.FileOutputStream(file)) {
            input.transferTo(output);
        }

        return "/assets/images/avatars/" + fileName;
    }

    private File resolvePersistentUploadFolder(HttpServletRequest request) throws IOException {
        String appPath = request.getServletContext().getRealPath("");

        if (appPath == null) {
            throw new IOException("Unable to resolve application root path.");
        }

        File currentRoot = new File(appPath).getAbsoluteFile();
        File sourceFolder = findSourceUploadFolder(currentRoot);

        if (sourceFolder != null) {
            return sourceFolder;
        }

        String runtimePath = request.getServletContext().getRealPath("/assets/upload/avatars");

        if (runtimePath == null) {
            throw new IOException("Unable to resolve upload folder path.");
        }

        return new File(runtimePath);
    }

    private File findSourceUploadFolder(File currentRoot) {
        File folder = currentRoot;

        while (folder != null) {
            if ("target".equals(folder.getName())) {
                File projectRoot = folder.getParentFile();

                if (projectRoot != null) {
                    return new File(projectRoot,
                            "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "assets"
                            + File.separator + "images" + File.separator + "avatars");
                }
            }

            folder = folder.getParentFile();
        }

        File fallback = new File(currentRoot,
                "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "assets"
                + File.separator + "images" + File.separator + "avatars");

        return fallback.exists() ? fallback : null;
    }

    private void changePassword(HttpServletRequest request) {
        int employeeID = getLoggedInEmployeeID(request);

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (AuthUtil.isBlank(oldPassword) || AuthUtil.isBlank(newPassword) || AuthUtil.isBlank(confirmPassword)) {
            setFlash(request, "errorMessage", "Vui lòng nhập đầy đủ các trường mật khẩu.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            setFlash(request, "errorMessage", "Mật khẩu xác nhận không khớp.");
            return;
        }

        String currentHash = profileDao.getPasswordHash(employeeID);

        if (currentHash == null || !PasswordUtil.verify(oldPassword, currentHash)) {
            setFlash(request, "errorMessage", "Mật khẩu cũ không đúng.");
            return;
        }

        String newPasswordHash = PasswordUtil.hash(newPassword);

        boolean success = profileDao.updatePasswordHash(employeeID, newPasswordHash);

        setFlash(
                request,
                success ? "successMessage" : "errorMessage",
                success ? "Đổi mật khẩu thành công." : "Không thể đổi mật khẩu."
        );
    }

    private boolean isLoggedIn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        return AuthUtil.requireLoggedIn(request, response);
    }

    private int getLoggedInEmployeeID(HttpServletRequest request) {
        return AuthUtil.getEmployeeId(request);
    }

    private void setFlash(HttpServletRequest request, String key, String message) {
        request.getSession().setAttribute(key, message);
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
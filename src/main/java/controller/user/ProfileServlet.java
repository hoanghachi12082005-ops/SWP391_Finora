/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller.user;

/**
 *
 * @author PCQN
 */
import dao.user.ProfileDao;
import model.Employee;
import util.security.PasswordUtil;

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

    @Override
    public void init() throws ServletException {
        profileDao = new ProfileDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request, response)) {
            return;
        }

        loadProfile(request);

        // FIX: Đường dẫn đúng: /views/profile/ (có chữ 's', không phải /view/)
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
            setFlash(request, "errorMessage", "Invalid action.");
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void loadProfile(HttpServletRequest request) {
        int employeeID = getLoggedInEmployeeID(request);

        Employee profile = profileDao.getProfileById(employeeID);
        
        request.setAttribute("profile", profile);
        request.setAttribute("salesSummary", profileDao.getEmployeeSalesSummary(employeeID));

        request.setAttribute("readOnlyProfile", false);
        request.setAttribute("profileTitle", "My Profile");
        request.setAttribute("profileSubtitle", "View your personal information and sales performance");
    }

    private void updateProfile(HttpServletRequest request) {
        int employeeID = getLoggedInEmployeeID(request);

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));

        if (isBlank(fullName) || isBlank(email)) {
            setFlash(request, "errorMessage", "Full name and email are required.");
            return;
        }

        if (profileDao.isEmailExists(email, employeeID)) {
            setFlash(request, "errorMessage", "Email already exists.");
            return;
        }

        String avatarUrl = null;

        try {
            Part avatarPart = request.getPart("avatarFile");
            avatarUrl = saveAvatarFile(request, avatarPart, employeeID);
        } catch (Exception e) {
            e.printStackTrace();
            setFlash(request, "errorMessage", "Cannot upload avatar image.");
            return;
        }

        boolean success = profileDao.updateProfile(employeeID, fullName, email, phone, avatarUrl);

        if (success) {
            HttpSession session = request.getSession(false);

            if (session != null) {
                session.setAttribute("employeeName", fullName);

                // FIX: Đọc đúng key "currentUser" — nhất quán với AuthServlet
                Object employeeObj = session.getAttribute("currentUser");

                if (employeeObj instanceof Employee) {
                    Employee employee = (Employee) employeeObj;

                    employee.setFullName(fullName);
                    employee.setEmail(email);
                    employee.setPhone(phone);

                    if (!isBlank(avatarUrl)) {
                        employee.setAvatarUrl(avatarUrl);
                    }

                    // FIX: Cập nhật lại session với key đúng
                    session.setAttribute("currentUser", employee);
                }
            }
        }

        setFlash(
                request,
                success ? "successMessage" : "errorMessage",
                success ? "Profile updated successfully." : "Cannot update profile."
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

        String uploadFolder = request.getServletContext().getRealPath("/uploads/avatars");

        File folder = new File(uploadFolder);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        String extension = submittedFileName.substring(submittedFileName.lastIndexOf("."));
        String fileName = "employee_" + employeeID + "_" + System.currentTimeMillis() + extension;

        File file = new File(folder, fileName);

        avatarPart.write(file.getAbsolutePath());

        return "/uploads/avatars/" + fileName;
    }

    private void changePassword(HttpServletRequest request) {
        int employeeID = getLoggedInEmployeeID(request);

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (isBlank(oldPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            setFlash(request, "errorMessage", "Please fill all password fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            setFlash(request, "errorMessage", "Confirm password does not match.");
            return;
        }

        String currentHash = profileDao.getPasswordHash(employeeID);

        if (currentHash == null || !PasswordUtil.verify(oldPassword, currentHash)) {
            setFlash(request, "errorMessage", "Old password is incorrect.");
            return;
        }

        String newPasswordHash = PasswordUtil.hash(newPassword);

        boolean success = profileDao.updatePasswordHash(employeeID, newPasswordHash);

        setFlash(
                request,
                success ? "successMessage" : "errorMessage",
                success ? "Password changed successfully." : "Cannot change password."
        );
    }

    private boolean isLoggedIn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        // FIX: Đọc đúng key "currentUser" — nhất quán với AuthServlet
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        return true;
    }

    private int getLoggedInEmployeeID(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return -1;
        }

        // FIX: Lấy employeeID từ Employee object với key "currentUser"
        Object employeeObj = session.getAttribute("currentUser");

        if (employeeObj instanceof Employee) {
            return ((Employee) employeeObj).getEmployeeID();
        }

        return -1;
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

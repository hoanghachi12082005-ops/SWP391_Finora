package controller.branch;

import dao.branch.BranchDAO;
import dao.employee.EmployeeDAO;
import model.Branch;
import model.Employee;
import util.branch.BranchValidator;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;
import javax.imageio.ImageIO;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "BranchServlet", urlPatterns = {"/branch"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class BranchController extends HttpServlet {

    private final BranchDAO dao = new BranchDAO();

    // ════════════════════════ GET ════════════════════════════════
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {

            case "add":
                loadEmployeeList(req);
                forward(req, resp, "branch-form.jsp");
                break;

            case "edit":
                int editId = parseId(req.getParameter("id"));
                Branch toEdit = dao.findById(editId);
                if (toEdit == null) {
                    resp.sendRedirect(req.getContextPath() + "/branch?error=notfound");
                    return;
                }
                toEdit.setManagerId(dao.findManagerIdByBranchId(editId));
                req.setAttribute("branch", toEdit);
                loadEmployeeList(req);
                forward(req, resp, "branch-form.jsp");
                break;

            case "detail":
                int detailId = parseId(req.getParameter("id"));
                Branch detailBranch = dao.findById(detailId);
                if (detailBranch == null) {
                    resp.sendRedirect(req.getContextPath() + "/branch?error=notfound");
                    return;
                }
                loadBranchDetail(req, detailBranch);
                forward(req, resp, "branch-detail.jsp");
                break;

            case "delete":
                int deleteId = parseId(req.getParameter("id"));
                boolean isDeleted = dao.delete(deleteId);
                if (isDeleted) {
                    resp.sendRedirect(req.getContextPath() + "/branch?success=delete");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/branch?error=deletefailed");
                }
                break;

            default:
                loadListDashboard(req);
                String status = req.getParameter("status");
                String city = req.getParameter("city");
                String keyword = req.getParameter("keyword");

                if (keyword != null) {
                    keyword = keyword.trim().replaceAll("\\s+", " ");
                }

                int page = parseId(req.getParameter("page"));
                int sizeValue = parseId(req.getParameter("sizeValue"));
                if (page < 1) page = 1;
                if (sizeValue < 1) sizeValue = 30;

                int totalRecords = dao.countBranch(keyword, status, city);
                PageResult pr = PaginationHelper.compute(totalRecords, page, sizeValue);
                pr.setAttributes(req);

                List<Branch> branchList = dao.findBranchPaging(
                        keyword, status, city,
                        pr.getCurrentPage(), pr.getPageSize());

                boolean showEmployeeColumn = false;
                for (Branch branch : branchList) {
                    if (branch.getEmployeeCount() > 0) {
                        showEmployeeColumn = true;
                        break;
                    }
                }

                req.setAttribute("branchList", branchList);
                req.setAttribute("showEmployeeColumn", showEmployeeColumn);
                req.setAttribute("selectedStatus", status);
                req.setAttribute("selectedCity", city);
                req.setAttribute("cityList", dao.getCityWithBranch());
                req.setAttribute("baseUrl", req.getContextPath() + "/branch");

                forward(req, resp, "branch-list.jsp");
                break;
        }
    }

    // ════════════════════════ POST ═══════════════════════════════
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        // Ép parse multipart trước khi đọc parameter
        // (SecurityFilter gọi getParameter() trước nhưng không có @MultipartConfig
        //  nên Tomcat chỉ parse query string, bỏ qua body)
        req.getParts();

        String action = req.getParameter("action");

        if ("update".equals(action) || "insert".equals(action)) {
            saveBranch(req, resp);
        }
    }

    // ────────── Kết hợp sửa + cập nhật───────────────────────────────────────
    private void saveBranch(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        //Kiểm tra xem nó đang là edit hay là add
        boolean isUpdate = "update".equals(req.getParameter("action"));

        //Lấy cái ID nếu đó là update
        int id = isUpdate ? resolveBranchId(req) : 0;

        //Néu là update kiểm tra xem ID đó có tồn tại không
        if (isUpdate && dao.findById(id) == null) {
            resp.sendRedirect(req.getContextPath() + "/branch?error=notfound");
            return;
        }

        //Build object từ form
        Branch b = buildBranchFromRequest(req, id);

        if (isUpdate) {
            Branch oldBranch = dao.findById(id);
            if (oldBranch != null) {
                b.setImageUrl(oldBranch.getImageUrl());
            }
        }

        //validate
        Map<String, String> errors = isUpdate
                ? BranchValidator.validateForUpdate(b, id, dao::isCodeDuplicate)
                : BranchValidator.validateForInsert(b, dao::isCodeDuplicate);
        if (dao.isInforDuplicate(b.getEmail(), b.getPhone(), id)) {
            errors.put("general", "Email hoặc số điện thoại đã tồn tại trong hệ thống.");
        }

        Part imagePart = req.getPart("image");

        String imageError = BranchValidator.validateImage(imagePart);

        if (imageError != null) {
            errors.put("image", imageError);
        }

        //Nếu gặp lỗi thì quay lại branch-form.jsp
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("branch", b);
            loadEmployeeList(req);
            forward(req, resp, "branch-form.jsp");
            return;
        }

        if (imagePart != null && imagePart.getSize() > 0) {
            try {
                String imageUrl = saveBranchImage(req, imagePart, b.getBranchCode());
                if (imageUrl != null) {
                    b.setImageUrl(imageUrl);
                }
            } catch (IOException ex) {
                errors.put("image", ex.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("branch", b);
            loadEmployeeList(req);
            forward(req, resp, "branch-form.jsp");
            return;
        }

        //Lưu dữ liệu
        int branchId;
        if (isUpdate) {
            boolean success = dao.update(b);
            if (!success) {
                errors.put("general", "Cập nhật thất bại. Vui lòng thử lại.");
                req.setAttribute("errors", errors);
                req.setAttribute("branch", b);
                loadEmployeeList(req);
                forward(req, resp, "branch-form.jsp");
                return;
            }
            branchId = id;
        } else {
            branchId = dao.insert(b);
            if (branchId <= 0) {
                errors.put("branchCode", "Mã cửa hàng đã tồn tại trong hệ thống.");
                req.setAttribute("errors", errors);
                req.setAttribute("branch", b);
                loadEmployeeList(req);
                forward(req, resp, "branch-form.jsp");
                return;
            }
        }

        // Gán quản lý cho chi nhánh
        if (b.getManagerId() > 0) {
            if (isUpdate) {
                dao.unassignManager(branchId);
            }
            dao.assignManager(branchId, b.getManagerId());
        } else if (isUpdate) {
            dao.unassignManager(branchId);
        }

        //Redirect sang Detail
        resp.sendRedirect(
                req.getContextPath()
                + "/branch?action=detail&id="
                + branchId
                + "&success="
                + (isUpdate ? "edit" : "add")
        );
    }

    private void mergeMissingFieldsFromExisting(Branch submitted, Branch existing) {
        if (submitted.getOpeningTime() == null || submitted.getOpeningTime().isBlank()) {
            submitted.setOpeningTime(existing.getOpeningTime() != null ? existing.getOpeningTime() : "");
        }
        if (submitted.getClosingTime() == null || submitted.getClosingTime().isBlank()) {
            submitted.setClosingTime(existing.getClosingTime() != null ? existing.getClosingTime() : "");
        }
        if (submitted.getStatus() == null || submitted.getStatus().isBlank()) {
            submitted.setStatus(existing.getStatus());
        }
    }

    // ════════════════════════ Dashboard (danh sách) ══════════════
    //Nạp dữ liệu thống kê cho màn hình danh sách chi nhánh
    private void loadListDashboard(HttpServletRequest req) {
        req.setAttribute("totalBranch", dao.countAll());

        int totalEmployee = 0;
        try {
            totalEmployee = new dao.employee.EmployeeDAO().count();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        req.setAttribute("totalEmployee", totalEmployee);

        req.setAttribute("todayRevenue", formatCurrency(dao.sumTodayRevenue()));
        req.setAttribute("bestBranch", dao.findBestBranchNameToday());
    }

    /**
     * Nạp thống kê + nhân viên cho trang chi tiết chi nhánh.
     */
    private void loadBranchDetail(HttpServletRequest req, Branch branch) {
        int branchId = branch.getBranchId();

        List<Employee> employees = List.of();
        try {
            EmployeeDAO empDao = new EmployeeDAO();
            employees = empDao.getByBranch(branchId);
            branch.setEmployeeCount(empDao.countByBranch(branchId));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        req.setAttribute("branch", branch);
        req.setAttribute("employeeList", employees);
        req.setAttribute("employeeCount", branch.getEmployeeCount());

        // Nạp dữ liệu tài chính chi nhánh
        String range = req.getParameter("range");
        if (range == null || range.trim().isEmpty()) {
            range = "month";
        }
        dao.dashboard.DashboardDAO dashboardDAO = new dao.dashboard.DashboardDAO();
        try {
            dao.dashboard.DashboardDAO.FinancialData fd = dashboardDAO.getFinancialData(range, branchId);
            req.setAttribute("totalRevenue", fd.totalRevenue);
            req.setAttribute("totalExpenses", fd.totalExpenses);
            req.setAttribute("netProfit", fd.netProfit);
            req.setAttribute("totalInvoices", fd.totalInvoices);
            req.setAttribute("selectedRange", range);
            
            List<model.Payment> payments = dashboardDAO.getBranchPayments(range, branchId);
            req.setAttribute("branchPayments", payments);
        } catch (SQLException ex) {
            ex.printStackTrace();
            req.setAttribute("financialError", "Không thể tải dữ liệu tài chính chi nhánh.");
        }

        // Backward compatibility
        req.setAttribute("monthlyRevenue", formatCurrency(dao.sumMonthlyRevenue(branchId)));
        req.setAttribute("orderCount", dao.countMonthlyOrders(branchId));
        req.setAttribute("profit", formatCurrency(dao.sumMonthlyProfit(branchId)));
    }

    //Chuyển dổi tiền thành định dạng tiềng Việt
    private static String formatCurrency(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " ₫";
    }

    // ════════════════════════ Helpers ════════════════════════════
    private Branch buildBranchFromRequest(HttpServletRequest req, int id) {
        Branch b = new Branch();
        b.setBranchId(id);
        b.setBranchName(trim(req.getParameter("branchName")));
        b.setBranchCode(trim(req.getParameter("branchCode")));
        b.setAddress(trim(req.getParameter("address")));
        b.setPhone(trim(req.getParameter("phone")));
        b.setEmail(trim(req.getParameter("email")));
        b.setOpeningTime(trim(req.getParameter("openingTime")));
        b.setClosingTime(trim(req.getParameter("closingTime")));
        b.setStatus(trim(req.getParameter("status")));
        b.setCity(trim(req.getParameter("city")));
        b.setDistrict(trim(req.getParameter("district")));
        b.setManagerId(parseId(req.getParameter("managerId")));
        return b;
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/branch/" + view).forward(req, resp);
    }

    private int resolveBranchId(HttpServletRequest req) {
        int id = parseId(req.getParameter("id"));
        if (id <= 0) {
            id = parseId(req.getParameter("branchId"));
        }
        return id;
    }

    private int parseId(String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }

    private String trim(String val) {
        return val != null ? val.trim() : "";
    }

    private void loadEmployeeList(HttpServletRequest req) {
        try {
            req.setAttribute("employeeList", new dao.employee.EmployeeDAO().getStoreManagers());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String saveBranchImage(HttpServletRequest request,
            Part imagePart,
            String branchCode) throws IOException {

        if (imagePart == null || imagePart.getSize() <= 0) {
            return null;
        }

        String submittedFileName = imagePart.getSubmittedFileName();

        if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
            return null;
        }

        // Read all bytes from the input stream first
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (var input = imagePart.getInputStream()) {
            input.transferTo(buffer);
        }
        byte[] imageBytes = buffer.toByteArray();

        // Validate actual file content by attempting to read as image
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("File tải lên không phải hình ảnh hợp lệ.");
        }

        String extension = submittedFileName.substring(
                submittedFileName.lastIndexOf(".")).toLowerCase();

        // Force the extension to match the actual image type
        // This prevents a malicuous user from uploading a valid PNG with a .jpg extension
        String detectedType = getImageExtension(imageBytes);
        if (detectedType != null) {
            extension = detectedType;
        }

        String fileName = branchCode + extension;

        File folder = resolvePersistentUploadFolder(request);

        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Unable to create upload folder: "
                    + folder.getAbsolutePath());
        }

        // Save the validated image
        File file = new File(folder, fileName);
        // Use ImageIO to write the image, which ensures only valid image data is saved
        ImageIO.write(image, extension.substring(1), file);

        return fileName;
    }

    private String getImageExtension(byte[] imageBytes) {
        // Detect the actual image format from the byte content (magic bytes)
        if (imageBytes.length < 4) {
            return ".png";
        }
        // PNG: 89 50 4E 47
        if ((imageBytes[0] & 0xff) == 0x89 && imageBytes[1] == 0x50
                && imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
            return ".png";
        }
        // JPEG: FF D8 FF
        if ((imageBytes[0] & 0xff) == 0xFF && (imageBytes[1] & 0xff) == 0xD8
                && (imageBytes[2] & 0xff) == 0xFF) {
            return ".jpg";
        }
        // GIF: 47 49 46 38
        if (imageBytes[0] == 0x47 && imageBytes[1] == 0x49
                && imageBytes[2] == 0x46 && imageBytes[3] == 0x38) {
            return ".gif";
        }
        // WEBP: 52 49 46 46 ... 57 45 42 50
        if (imageBytes[0] == 0x52 && imageBytes[1] == 0x49
                && imageBytes[2] == 0x46 && imageBytes[3] == 0x46
                && imageBytes.length > 12
                && imageBytes[8] == 0x57 && imageBytes[9] == 0x45
                && imageBytes[10] == 0x42 && imageBytes[11] == 0x50) {
            return ".webp";
        }
        return ".png";
    }

    private File resolvePersistentUploadFolder(HttpServletRequest request)
            throws IOException {

        String appPath = request.getServletContext().getRealPath("");

        if (appPath == null) {
            throw new IOException("Unable to resolve application root path.");
        }

        File currentRoot = new File(appPath).getAbsoluteFile();

        File sourceFolder = findSourceUploadFolder(currentRoot);

        if (sourceFolder != null) {
            return sourceFolder;
        }

        String runtimePath = request.getServletContext()
                .getRealPath("/assets/images/images_branch");

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
                            "src"
                            + File.separator + "main"
                            + File.separator + "webapp"
                            + File.separator + "assets"
                            + File.separator + "images"
                            + File.separator + "images_branch");
                }
            }

            folder = folder.getParentFile();
        }

        File fallback = new File(currentRoot,
                "src"
                + File.separator + "main"
                + File.separator + "webapp"
                + File.separator + "assets"
                + File.separator + "images"
                + File.separator + "images_branch");

        return fallback.exists() ? fallback : null;
    }
}

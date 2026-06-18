package controller.branch;

import dao.branch.BranchDAO;
import dao.employee.EmployeeDAO;
import model.Branch;
import model.Employee;
import util.branch.BranchValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "BranchController", urlPatterns = {"/branch"})
@MultipartConfig
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
                    resp.sendRedirect("branch?error=notfound");
                    return;
                }
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
                dao.delete(deleteId);
                resp.sendRedirect("branch?success=delete");
                break;

            default:
                loadListDashboard(req);
                String status = req.getParameter("status");
                String city = req.getParameter("city");
                String keyword = req.getParameter("keyword");
                
                if(keyword != null){
                    keyword = keyword.trim().replaceAll("\\s+", " ");
                }
                 
                int page = 1;
                try {
                    page = Integer.parseInt(req.getParameter("page"));
                } catch (Exception ignored) {}
                
                int pageSize = 10;
                try {
                    pageSize = Integer.parseInt(req.getParameter("pageSize"));
                } catch (Exception ignored) {}
                
                if (pageSize != 10 &&
                    pageSize != 20 &&
                    pageSize != 50 &&
                    pageSize != 100) {
                    
                    pageSize = 10;
                }
                
                int totalRecords = dao.countBranch(keyword,status, city);
                int totalPages = (int) Math.ceil(totalRecords * 1.0 / pageSize);
                
                
                
                req.setAttribute(
                        "branchList",
                        dao.findBranchPaging(
                                keyword,
                                status,
                                city,
                                page,
                                pageSize));

                req.setAttribute("currentPage", page);
                req.setAttribute("totalPages", totalPages);

                req.setAttribute("selectedStatus", status);
                req.setAttribute("selectedCity", city);
                req.setAttribute("cityList", dao.getCityWithBranch());

                forward(req, resp, "branch-list.jsp");
                break;
        }
    }

    // ════════════════════════ POST ═══════════════════════════════
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("update".equals(action) || "insert".equals(action)) {
            saveBranch(req, resp);
        }
    }

//    // ────────── Xử lý Thêm ──────────────────────────────────────
//    private void handleAdd(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//
//        Branch b = buildBranchFromRequest(req, 0);
//        Map<String, String> errors = BranchValidator.validateForInsert(b, dao::isCodeDuplicate);
//
//        if (!errors.isEmpty()) {
//            req.setAttribute("errors", errors);
//            req.setAttribute("branch", b);
//            loadEmployeeList(req);
//            forward(req, resp, "branch-form.jsp");
//            return;
//        }
//
//        int newId = dao.insert(b);
//        if (newId > 0) {
//            resp.sendRedirect("branch?action=detail&id=" + newId + "&success=add");
//        } else {
//            errors.put("branchCode", "Mã cửa hàng đã tồn tại trong hệ thống.");
//            req.setAttribute("errors", errors);
//            req.setAttribute("branch", b);
//            loadEmployeeList(req);
//            forward(req, resp, "branch-form.jsp");
//        }
//    }
//
//    // ────────── Xử lý Sửa ───────────────────────────────────────
//    private void handleEdit(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//        int id = resolveBranchId(req);
//        if (id <= 0) {
//            resp.sendRedirect(req.getContextPath() + "/branch?error=invalid");
//            return;
//        }
//        Branch existing = dao.findById(id);
//        if (existing == null) {
//            resp.sendRedirect(req.getContextPath() + "/branch?error=notfound");
//            return;
//        }
//        Branch b = buildBranchFromRequest(req, id);
//        mergeMissingFieldsFromExisting(b, existing);
//        Map<String, String> errors = BranchValidator.validateForUpdate(b, id, dao::isCodeDuplicate);
//        if (!errors.isEmpty()) {
//            req.setAttribute("errors", errors);
//            req.setAttribute("branch", b);
//            loadEmployeeList(req);
//            forward(req, resp, "branch-form.jsp");
//            return;
//        }
//        if (!dao.update(b)) {
//            Map<String, String> updateErrors = new HashMap<>();
//            updateErrors.put("general", "Cập nhật thất bại. Vui lòng thử lại.");
//            req.setAttribute("errors", updateErrors);
//            req.setAttribute("branch", b);
//            loadEmployeeList(req);
//            forward(req, resp, "branch-form.jsp");
//            return;
//        }
//        resp.sendRedirect(req.getContextPath()
//                + "/branch?action=detail&id=" + b.getBranchId() + "&success=edit");
//    }
//    
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
        } 
        
        //Build object từ form
        Branch b = buildBranchFromRequest(req, id);
        
        //validate
        Map<String, String> errors = isUpdate
                ? BranchValidator.validateForUpdate(b, id, dao::isCodeDuplicate)
                : BranchValidator.validateForInsert(b, dao::isCodeDuplicate);
                if (dao.isInforDuplicate(b.getEmail(),b.getPhone(), id)) {
                    errors.put("general", "Email hoặc số điện thoại đã tồn tại trong hệ thống.");
                }
                
                
        //Nếu gặp lỗi thì quay lại branch-form.jsp
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
            req.setAttribute("employeeList", new dao.employee.EmployeeDAO().getAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

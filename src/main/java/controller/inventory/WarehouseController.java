package controller.inventory;

// [MOVED FROM InventoryController] - Warehouse setup/update endpoints extracted for maintainability
import dao.inventory.WarehouseDAO;
import model.Employee;
import model.Warehouse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller xử lý các thiết lập liên quan đến Kho hàng vật lý (Warehouse).
 * Hỗ trợ tạo kho hàng đầu tiên và cập nhật thông tin kho (tên, địa chỉ).
 */
@WebServlet(name = "WarehouseController", urlPatterns = {"/inventory-warehouse"})
public class WarehouseController extends InventoryBaseController {

    private final WarehouseDAO warehouseDAO = new WarehouseDAO();

    /**
     * Xử lý yêu cầu POST: thiết lập kho đầu tiên (setupWarehouse) hoặc cập nhật kho (updateWarehouse).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            return;
        }

        // [MOVED FROM InventoryController] - Original error handling pattern from lines 1119, 1955-1961
        try {
            switch (action) {
                // [MOVED FROM InventoryController] - Original lines 1286-1297
                case "setupWarehouse": {
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bạn chưa đăng nhập.");
                        return;
                    }
                    String role = currentUser.getRoleName() != null ? currentUser.getRoleName().trim().toLowerCase() : "";
                    if (!role.equals("storemanager") && !role.equals("owner") && !role.equals("admin")) {
                        request.getSession().setAttribute("error", "Bạn không có quyền thực hiện khởi tạo kho hàng. Vui lòng liên hệ Quản lý cửa hàng hoặc Chủ cửa hàng.");
                        redirect(response, request.getContextPath() + "/inventory");
                        return;
                    }

                    String wName = request.getParameter("warehouseName");
                    if (wName == null || wName.trim().isEmpty()) {
                        request.getSession().setAttribute("error", "Tên kho hàng không được để trống.");
                        redirect(response, request.getContextPath() + "/inventory");
                        return;
                    }
                    wName = wName.trim();
                    if (wName.length() > 150) {
                        request.getSession().setAttribute("error", "Tên kho hàng không được vượt quá 150 ký tự.");
                        redirect(response, request.getContextPath() + "/inventory");
                        return;
                    }

                    int branchId = currentUser.getBranchId() != null ? currentUser.getBranchId() : 0;
                    if (branchId <= 0 && !role.equals("admin") && !role.equals("owner")) {
                        request.getSession().setAttribute("error", "Tài khoản của bạn chưa được gán cho chi nhánh nào.");
                        redirect(response, request.getContextPath() + "/inventory");
                        return;
                    }

                    Warehouse newW = new Warehouse();
                    newW.setWarehouseName(wName);
                    String address = request.getParameter("address");
                    newW.setAddress(address != null ? address.trim() : "");
                    newW.setBranchId(branchId);
                    warehouseDAO.createWarehouse(newW);
                    request.getSession().setAttribute("message", "Khởi tạo kho đầu tiên thành công!");
                    redirect(response, request.getContextPath() + "/inventory");
                    break;
                }
                // [MOVED FROM InventoryController] - Original lines 1298-1308
                case "updateWarehouse": {
                    Warehouse updateW = new Warehouse();
                    int wId = Integer.parseInt(request.getParameter("warehouseId"));
                    updateW.setWarehouseId(wId);
                    updateW.setWarehouseName(request.getParameter("warehouseName"));
                    updateW.setAddress(request.getParameter("address"));
                    warehouseDAO.updateWarehouse(updateW);
                    request.getSession().setAttribute("message", "Cập nhật thông tin kho thành công!");
                    redirect(response, request.getContextPath() + "/inventory?warehouseId=" + wId);
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            request.getSession().setAttribute("error", "Lỗi: " + e.getMessage() + " | StackTrace: " + sw.toString().substring(0, Math.min(200, sw.toString().length())));
            redirect(response, request.getContextPath() + "/inventory");
        }
    }
}

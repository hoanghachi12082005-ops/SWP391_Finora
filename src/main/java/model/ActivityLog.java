package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Activity Log entity ánh xạ tới bảng audit_log trong DB.
 *
 * Ngoài các trường thô (action_name, table_name, record_id, old/new_data),
 * model cung cấp các "getter nghiệp vụ" để JSP hiển thị thân thiện với chủ chuỗi:
 *  - getEntityLabel()    : "Đơn hàng", "Sản phẩm", "Khách hàng"...
 *  - getEntityCode()     : mã hiển thị (HD000003, SP005, KH012...)
 *  - getActionLabel()    : "Tạo mới", "Cập nhật", "Xóa", "Đăng nhập"...
 *  - getDescription()    : câu mô tả hoàn chỉnh để Owner đọc 1 dòng là hiểu.
 */
public class ActivityLog {
    private int id;
    private int empId;
    private String empName;
    private Integer branchId;
    private String branchName;
    private String actionName;
    private String tableName;
    private Integer recordId;
    private String oldData;
    private String newData;
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }

    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }

    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getBranchLabel() {
        if (branchName != null && !branchName.isBlank()) return branchName;
        if (branchId != null) return "CN #" + branchId;
        return "—";
    }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public String getOldData() { return oldData; }
    public void setOldData(String oldData) { this.oldData = oldData; }

    public String getNewData() { return newData; }
    public void setNewData(String newData) { this.newData = newData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedAtFormatted() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // ====================================================================
    // MAPPING NGHIỆP VỤ (UI-facing)
    // ====================================================================

    /** Phân nhóm đối tượng theo nghiệp vụ: "Đơn hàng", "Sản phẩm"... */
    public String getEntityLabel() {
        if (tableName == null) return "Hệ thống";
        switch (tableName.toLowerCase()) {
            case "order":
            case "orders":            return "Đơn hàng";
            case "order_detail":      return "Chi tiết đơn";
            case "product":           return "Sản phẩm";
            case "category":          return "Danh mục";
            case "customer":          return "Khách hàng";
            case "supplier":          return "Nhà cung cấp";
            case "employee":          return "Nhân viên";
            case "branch":
            case "store":             return "Chi nhánh";
            case "inventory":         return "Tồn kho";
            case "stock_transaction": return "Giao dịch kho";
            case "stock_transfer":    return "Phiếu chuyển kho";
            case "purchase_order":
            case "purchase":          return "Phiếu nhập";
            case "purchase_detail":   return "Chi tiết phiếu nhập";
            case "payment":           return "Thanh toán";
            case "invoice":           return "Hóa đơn";
            case "expense":           return "Chi phí";
            case "income":            return "Thu nhập";
            case "voucher":           return "Voucher";
            case "point_transaction": return "Điểm thưởng";
            case "role":              return "Vai trò";
            case "auth":              return "Đăng nhập";
            default:                  return tableName;
        }
    }

    /** Mã đối tượng hiển thị, ví dụ HD000003, SP005, KH012, NV02. */
    public String getEntityCode() {
        if (recordId == null) return "";
        String prefix;
        if (tableName == null) prefix = "ID";
        else switch (tableName.toLowerCase()) {
            case "order": case "orders":   prefix = "HD"; break;
            case "order_detail":           prefix = "CT"; break;
            case "product":                prefix = "SP"; break;
            case "category":               prefix = "DM"; break;
            case "customer":               prefix = "KH"; break;
            case "supplier":               prefix = "NCC"; break;
            case "employee":               prefix = "NV"; break;
            case "branch": case "store":   prefix = "CN"; break;
            case "inventory":              prefix = "TK"; break;
            case "stock_transaction":      prefix = "GD"; break;
            case "stock_transfer":         prefix = "PCK"; break;
            case "purchase_order": case "purchase": prefix = "PN"; break;
            case "purchase_detail":        prefix = "CTPN"; break;
            case "payment":                prefix = "TT"; break;
            case "invoice":                prefix = "HĐ"; break;
            case "expense":                prefix = "CHI"; break;
            case "income":                 prefix = "THU"; break;
            case "voucher":                prefix = "VC"; break;
            case "point_transaction":      prefix = "DT"; break;
            case "role":                   prefix = "RL"; break;
            default:                       prefix = "#";
        }
        // HD000003 vs SP005: đơn hàng dùng 6 ký số, mặc định dùng 3 ký số.
        int width = (prefix.equals("HD") || prefix.equals("HĐ")) ? 6 : 3;
        return prefix + String.format("%0" + width + "d", recordId);
    }

    /** Loại thao tác bằng tiếng Việt. */
    public String getActionLabel() {
        if (actionName == null) return "Hoạt động";
        String a = actionName.toUpperCase();
        if (a.contains("LOGIN"))                          return "Đăng nhập";
        if (a.contains("LOGOUT"))                         return "Đăng xuất";
        if (a.contains("CANCEL"))                         return "Hủy";
        if (a.contains("APPROVE") || a.contains("CONFIRM")) return "Duyệt";
        if (a.contains("PAY") || a.contains("PAYMENT"))   return "Thanh toán";
        if (a.contains("INSERT") || a.contains("CREATE") || a.contains("ADD")) return "Tạo mới";
        if (a.contains("UPDATE") || a.contains("EDIT"))   return "Cập nhật";
        if (a.contains("DELETE") || a.contains("REMOVE")) return "Xóa";
        return actionName;
    }

    /** Mô tả nghiệp vụ một dòng. Vd: "Tạo đơn hàng HD000003" / "Cập nhật sản phẩm SP005". */
    public String getDescription() {
        String entity = getEntityLabel().toLowerCase();
        String code = getEntityCode();
        String act = getActionLabel();
        StringBuilder sb = new StringBuilder(act);
        if (entity != null && !entity.isBlank()) sb.append(' ').append(entity);
        if (code != null && !code.isBlank()) sb.append(' ').append(code);
        return sb.toString();
    }

    /** Màu icon hiển thị trong activity feed (blue/green/red/orange). */
    public String getIconColor() {
        if (actionName == null) return "blue";
        String a = actionName.toUpperCase();
        if (a.contains("DELETE") || a.contains("REMOVE") || a.contains("CANCEL")) return "red";
        if (a.contains("INSERT") || a.contains("CREATE") || a.contains("ADD"))    return "green";
        if (a.contains("UPDATE") || a.contains("EDIT"))                           return "orange";
        if (a.contains("PAY") || a.contains("PAYMENT") || a.contains("APPROVE")) return "green";
        return "blue";
    }

    /** Tên material-icons hiển thị cho activity feed. */
    public String getIconName() {
        if (actionName == null) return "receipt";
        String a = actionName.toUpperCase();
        if (a.contains("DELETE") || a.contains("REMOVE")) return "delete";
        if (a.contains("CANCEL"))                         return "cancel";
        if (a.contains("INSERT") || a.contains("CREATE") || a.contains("ADD")) return "add_circle";
        if (a.contains("UPDATE") || a.contains("EDIT"))   return "edit";
        if (a.contains("LOGIN"))                          return "login";
        if (a.contains("LOGOUT"))                         return "logout";
        if (a.contains("PAY") || a.contains("PAYMENT"))   return "payments";
        if (a.contains("APPROVE") || a.contains("CONFIRM")) return "verified";
        return "receipt";
    }

    /** Tên người thực hiện hoặc fallback. */
    public String getActorLabel() {
        if (empName != null && !empName.isBlank()) return empName;
        if (empId > 0) return "NV #" + empId;
        return "Hệ thống";
    }
}

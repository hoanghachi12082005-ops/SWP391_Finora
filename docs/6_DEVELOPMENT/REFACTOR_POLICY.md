# Chính Sách Tái Cấu Trúc - Refactor Policy

> **Mục đích:** Tài liệu này quy định nguyên tắc, quy trình, và ranh giới cho việc tái cấu trúc (refactoring) mã nguồn trong dự án FinoraRetail (SWP391_Finora). Tái cấu trúc phải phục vụ mục đích rõ ràng, bảo toàn hành vi hệ thống, và tuân thủ nghiêm ngặt các vùng được bảo vệ. Việc refactor vô căn cứ hoặc vi phạm protected areas là hành vi bị nghiêm cấm.

---

## 1. Nguyên Tắc Tái Cấu Trúc

### 1.1. Mọi Refactor Phải Có Mục Đích

**Tái cấu trúc chỉ được thực hiện khi phục vụ ít nhất một trong các mục đích sau:**

- **Tính năng mới (New Feature):** Code refactor để chuẩn bị nền tảng cho tính năng sắp triển khai.
- **Sửa lỗi (Bug Fix):** Cấu trúc hiện tại gây khó khăn cho việc sửa lỗi hoặc có rủi ro phá vỡ khi sửa.
- **Giảm rủi ro (Risk Reduction):** Code có debt cao, khó test, hoặc có lỗ hổng bảo mật tiềm ẩn.
- **Kỹ thuật nợ (Technical Debt):** Code khó đọc, thiếu documentation, hoặc không tuân thủ tiêu chuẩn đến mức ảnh hưởng tốc độ phát triển.

**Tuyệt đối không refactor vì:**

- Thích thú với việc "dọn dẹp" code không có vấn đề rõ ràng.
- Thay đổi chỉ để "hiện đại hóa" mà không có lý do cụ thể.
- Áp dụng pattern mới khi pattern hiện tại đã hoạt động tốt.

### 1.2. Ưu Tiên Refactor Nhỏ, Bảo Toàn Hành Vi

**Ưu tiên refactor nhỏ, có thể verify sau mỗi bước** thay vì broad rewrite:

```java
// ✅ TỐT - Thay đổi nhỏ, có thể test ngay
// Trước
public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    String name = request.getParameter("name");
    // xử lý...
}

// Sau - extract validation thành method riêng
public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    if (!validateCategoryInput(request)) {
        response.sendRedirect("categories?error=invalid");
        return;
    }
    String name = request.getParameter("name");
    // xử lý...
}

private boolean validateCategoryInput(HttpServletRequest request) {
    String name = request.getParameter("name");
    return name != null && !name.trim().isEmpty() && name.length() <= 100;
}
```

### 1.3. Không Thay Đổi Protected Modules Một Cách Tùy Tiện

**Xem danh sách protected modules tại `docs/rules/PROTECTED_MODULES.md`.**

Các module sau được bảo vệ nghiêm ngặt:

| Module | Lý do bảo vệ |
|---|---|
| Authentication & Session Flow | Bảo mật người dùng, ảnh hưởng toàn hệ thống |
| Authorization (RolePermissionUtil) | Kiểm soát quyền truy cập |
| Database Infrastructure (DatabaseUtil) | Tất cả DAO phụ thuộc vào đây |
| Payment & Finance Schema | Rủi ro tài chính cao |
| Build Configuration (pom.xml, web.xml) | Ảnh hưởng deployment |

**Thay đổi protected modules chỉ được thực hiện khi:**

- User chủ động yêu cầu, HOẶC
- Active task không thể hoàn thành an toàn mà không chạm vào protected area.

Trong cả hai trường hợp, thay đổi phải được ghi chép rõ ràng, tối thiểu hóa tác động, và có review.

---

## 2. Quy Trình Refactor An Toàn

### Bước 1: Xác Định Hành Vi Cần Bảo Toàn

Trước khi thay đổi bất cứ dòng nào, liệt kê rõ:

- Input mong đợi của method/class.
- Output mong đợi của method/class.
- Side effects (database changes, session changes, response changes).
- Các call sites hiện tại và cách chúng sử dụng kết quả.

### Bước 2: Đọc Tất Cả Call Sites

Tìm tất cả nơi sử dụng class/method dự định thay đổi:

```powershell
# Tìm tất cả references
rg "CategoryDAO" --type java
rg "getCategoryById" --type java
```

Đảm bảo không bỏ sót bất kỳ call site nào, đặc biệt trong các servlet và service.

### Bước 3: Kiểm Tra Protected Areas

Xác định liệu class/method dự định thay đổi có nằm trong protected area hay không.

Xác định liệu thay đổi có ảnh hưởng đến protected area hay không.

### Bước 4: Thực Hiện Thay Đổi Nhỏ Nhất

- Thực hiện thay đổi nhỏ nhất có thể đạt được mục đích.
- Mỗi thay đổi phải có thể verify riêng biệt.
- Giữ nguyên hành vi bên ngoài (external behavior) nếu có thể.

### Bước 5: Bảo Toàn UTF-8 Without BOM

- Kiểm tra file sau khi lưu.
- Đảm bảo không thêm BOM.

### Bước 6: Chạy Verification

```powershell
mvn clean package -DskipTests
```

Build phải thành công. Nếu thất bại, xem lại thay đổi.

### Bước 7: Cập Nhật Tài Liệu Liên Quan

- Cập nhật `docs/status/TECH_DEBT.md` nếu đã giải debt.
- Cập nhật kiến trúc, pattern docs nếu có thay đổi cấu trúc.
- Cập nhật `docs/status/IMPLEMENTED_FEATURES.md` nếu refactor phục vụ tính năng.

---

## 3. Hướng Dẫn Extract Method

### 3.1. Khi Nào Extract

**Chỉ extract method khi cùng một logic xuất hiện ở 2 hoặc nhiều nơi.**

```java
// ❌ CHƯA CẦN - Chỉ có 1 nơi sử dụng
private String buildCategoryOptions(List<Category> categories) {
    StringBuilder sb = new StringBuilder();
    for (Category c : categories) {
        sb.append("<option value='").append(c.getCategoryID());
        sb.append("'>").append(escapeXml(c.getCategoryName())).append("</option>");
    }
    return sb.toString();
}

// ✅ CẦN EXTRACT - Cùng logic ở 3 nơi
private String buildCategoryOptions(List<Category> categories) {
    StringBuilder sb = new StringBuilder();
    for (Category c : categories) {
        sb.append(buildCategoryOption(c));
    }
    return sb.toString();
}

private String buildCategoryOption(Category c) {
    return String.format("<option value='%d'>%s</option>",
        c.getCategoryID(), escapeXml(c.getCategoryName()));
}
```

### 3.2. Tiêu Chí Của Method Helper Tốt

- **Single responsibility:** Method chỉ làm một việc.
- **No side effects:** Method không thay đổi trạng thái bên ngoài (không sửa tham số, không ghi database).
- **Tên rõ ràng:** Tên method phản ánh chính xác chức năng.
- **Testable:** Có thể viết unit test cho method.

### 3.3. Không Extract Sớm

**Không tạo abstraction khi chưa có ít nhất 2 thực thể cùng sử dụng.** Pattern "YAGNI" (You Aren't Gonna Need It) áp dụng nghiêm ngặt.

---

## 4. Tiêu Chí Đưa Vào Service Layer

### 4.1. Điều Kiện Cần Service

**Chỉ tạo class Service mới khi thỏa mãn ÍT NHẤT một điều kiện:**

| Điều kiện | Mô tả |
|---|---|
| Multi-DAO coordination | Cần gọi 2+ DAO trong cùng nghiệp vụ |
| Transaction boundary | Nhiều thao tác SQL phải cùng commit hoặc rollback |
| Multi-controller reuse | Logic được sử dụng bởi nhiều servlet khác nhau |
| Complex business rules | Business rules quá phức tạp để đặt trong servlet |

### 4.2. Service KHÔNG ĐƯỢC Phép

- Phụ thuộc vào `HttpServletRequest`, `HttpServletResponse`, `HttpSession`.
- Lưu trạng thái phiên (session state).
- Tự xử lý redirect hoặc forward.
- Chứa SQL (phải delegate cho DAO).
- Truy cập trực tiếp vào request parameters.

### 4.3. Ví Dụ Service Hợp Lệ

```java
public class OrderService {
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final InventoryDAO inventoryDAO;

    public boolean createOrderWithInventoryUpdate(Order order, List<OrderItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            int orderId = orderDAO.insertOrder(conn, order);
            
            for (OrderItem item : items) {
                item.setOrderID(orderId);
                orderDetailDAO.insert(conn, item);
                inventoryDAO.decreaseStock(conn, item.getProductID(), item.getQuantity());
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            logger.error("Failed to create order with inventory update", e);
            return false;
        } finally {
            DatabaseUtil.close(conn);
        }
    }
}
```

---

## 5. Danh Sách Hành Vi Bị Cấm

### 5.1. Cấm Tuyệt Đối

| Hành vi | Lý do |
|---|---|
| Viết lại JSP sang framework khác (React, Vue, Angular) mà chưa có approval | Thay đổi kiến trúc lớn, cần review toàn bộ |
| Thay thế JDBC bằng ORM (Hibernate, JPA) mà chưa có approval | Thay đổi data layer toàn bộ, migration phức tạp |
| Đổi tên database mà không có migration plan | Phá vỡ toàn bộ kết nối |
| Thay đổi cơ chế auth/password mà chưa qua security review | Rủi ro bảo mật nghiêm trọng |
| Hand-edit file trong `build/`, `dist/`, `target/` | Đây là artifact, sẽ bị ghi đè khi build |
| Thay đổi `.git/` | Nguy hiểm đến repository |
| Tạo file mới trong thư mục IDE private (`nbproject/`, `.idea/`) | IDE-managed, sẽ bị ghi đè |

### 5.2. Cấm Khi Không Có Plan

| Hành vi | Yêu cầu |
|---|---|
| Tái cấu trúc multi-file mà không có plan document | Phải tạo plan tại `docs/planning/<TOPIC>/` |
| Đổi cấu trúc package lớn | Phải có plan + migration strategy |
| Thay đổi cấu hình database connection | Phải có plan + rollback strategy |
| Thay đổi session management | Phải có plan + impact analysis |

---

## 6. Checklist Trước Khi Refactor

- [ ] Mục đích refactor rõ ràng (feature, bug fix, debt, risk).
- [ ] Đã liệt kê tất cả call sites.
- [ ] Đã kiểm tra protected modules.
- [ ] Thay đổi tối thiểu, bảo toàn hành vi.
- [ ] Có plan document cho thay đổi non-trivial.
- [ ] Đã backup mental model của hành vi hiện tại.
- [ ] Có chiến lược verify sau mỗi bước.
- [ ] Không thuộc danh sách hành vi bị cấm.

---

## 7. Tài Liệu Liên Quan

- `docs/rules/PROTECTED_MODULES.md` — Danh sách module được bảo vệ.
- `docs/6_DEVELOPMENT/CODING_STANDARDS.md` — Tiêu chuẩn code.
- `docs/patterns/SERVICE_PATTERNS.md` — Pattern khi tạo service mới.
- `docs/patterns/REPOSITORY_PATTERNS.md` — Pattern DAO layer.
- `docs/planning/ACTIVE_TASKS.md` — Task đang thực hiện.
- `docs/status/TECH_DEBT.md` — Kỹ thuật nợ hiện tại.

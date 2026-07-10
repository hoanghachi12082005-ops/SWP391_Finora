# Technical Debt Register - FinoraRetail

## Tổng quan

Tài liệu này ghi nhận các khoản nợ kỹ thuật (technical debt) trong hệ thống FinoraRetail, được phân loại theo mức độ nghiêm trọng. Mỗi khoản nợ bao gồm mô tả vấn đề, rủi ro, và hành động khắc phục đề xuất.

---

## Mức độ CRITICAL — Cần giải quyết ngay

### CR-001: Thông tin đăng nhập Cơ sở dữ liệu hardcoded

**Vị trí:** `util/DatabaseUtil.java` hoặc `common/util/DBContext.java`

**Mô tả vấn đề:**
Thông tin đăng nhập database (username `sa`, password `12345`, server, database name) được hardcoded trực tiếp trong source code Java.

```java
// Ví dụ pattern cần thay đổi
private static final String DB_USER = "sa";
private static final String DB_PASS = "12345";
```

**Rủi ro:**
- Thông tin đăng nhập bị lộ trong source code (git history, build artifacts)
- Không thể chia sẻ code với đội nếu có credentials trong code
- Không đáp ứng yêu cầu bảo mật cho production
- Vi phạm nguyên tắc "không hardcode secrets"

**Hành động khắc phục:**
1. Di chuyển credentials ra biến môi trường hệ thống (`ENV variables`)
2. Hoặc sử dụng file cấu hình riêng (`context.xml` với `ResourceLink`)
3. Hoặc sử dụng JNDI DataSource từ Tomcat
4. Thêm validation để ứng dụng không khởi động nếu thiếu credentials

**Ưu tiên:** Cao nhất

---

### CR-002: Authentication ở chế độ Demo — Bảo mật nghiêm trọng

**Vị trí:** `controller/auth/AuthController.java`

**Mô tả vấn đề:**
AuthController demo mode — chấp nhận bất kỳ username/password nào, tạo session với role hardcoded là `OWNER`. Không có xác minh với cơ sở dữ liệu, không mã hóa password.

```java
// Pattern cần thay đổi
String username = request.getParameter("username");
String password = request.getParameter("password");
// Bỏ qua kiểm tra, tạo session trực tiếp
HttpSession session = request.getSession();
session.setAttribute("role", "OWNER");
```

**Rủi ro:**
- Bất kỳ ai cũng có thể đăng nhập với quyền OWNER (toàn quyền)
- Không có bảo vệ brute-force attack
- Password không được mã hóa trong database (nếu lưu)
- Không có cơ chế đăng xuất hoàn chỉnh

**Hành động khắc phục:**
1. Triển khai xác thực với cơ sở dữ liệu (query bảng `users`)
2. Mã hóa password với BCrypt hoặc Argon2
3. Thêm cơ chế chống brute-force (rate limiting, lockout)
4. Thêm salt cho password hashing
5. Triển khai logout đầy đủ (invalidate session)
6. Thêm "remember me" token nếu cần

**Ưu tiên:** Cao nhất

---

### CR-003: Không có Role-based Authorization

**Vị trí:** `AuthFilter.java`, `RolePermissionUtil.java`

**Mô tả vấn đề:**
AuthFilter chỉ kiểm tra session tồn tại (`userId` trong session), không kiểm tra role của user. Mọi user đã đăng nhập đều có thể truy cập tất cả routes được bảo vệ, bao gồm cả những route chỉ dành cho OWNER hoặc ADMIN.

```java
// Hiện tại: chỉ kiểm tra session
if (session == null || session.getAttribute("userId") == null) {
    response.sendRedirect("/FinoraRetail/login");
    return;
}
// Thiếu: kiểm tra role cho từng route
```

**Rủi ro:**
- Employee có thể truy cập trang quản trị
- Không có kiểm soát truy cập theo nguyên tắc least privilege
- Nguy cơ insider threat (nhân viên lợi dụng)

**Hành động khắc phục:**
1. Triển khai đầy đủ RolePermissionUtil để kiểm tra quyền
2. AuthFilter cần kiểm tra role trước khi cho phép truy cập
3. Thêm annotation `@RolesAllowed` hoặc tương đương
4. Controller cần kiểm tra role trước khi xử lý action

**Ưu tiên:** Cao nhất

---

## Mức độ HIGH — Cần giải quyết sớm

### HI-001: Tất cả DAOs đang ở trạng thái Skeleton

**Vị trí:** Tất cả các file `*DAO.java` trong `dao/` packages

**Mô tả vấn đề:**
Tất cả DAO classes có interface `ICrudDAO` và stub methods, nhưng không có SQL implementation thực tế. Các phương thức như `findAll()`, `findById()`, `insert()`, `update()`, `delete()` chỉ là placeholder.

```java
// Pattern skeleton cần thay thế
public List<Category> findAll() {
    // TODO: Implement SQL query
    return new ArrayList<>();
}
```

**Rủi ro:**
- Không có module nào có thể lưu hoặc truy xuất dữ liệu thực
- Ứng dụng không thể hoạt động ngoài chế độ demo
- Business logic không thể được triển khai vì thiếu data access

**Hành động khắc phục:**
1. Triển khai SQL queries cho từng DAO, bắt đầu từ core modules
2. Ưu tiên: Category, Product, Order, Inventory
3. Sử dụng try-with-resources cho PreparedStatement
4. Thêm proper exception handling
5. Thêm logging cho queries

**Ưu tiên:** Cao

---

### HI-002: Tất cả Services đang ở trạng thái Skeleton

**Vị trí:** Tất cả các file `*Service.java` trong `service/` packages

**Mô tả vấn đề:**
Service classes tồn tại với cấu trúc thư mục nhưng không có business logic. Không có quy tắc rõ ràng về khi nào cần thêm service layer thay vì gọi DAO trực tiếp từ controller.

**Rủi ro:**
- Business logic bị phân tán trong controllers và DAOs
- Khó maintain khi business rules phức tạp
- Khó viết unit tests vì logic nằm ở nhiều nơi

**Hành động khắc phục:**
1. Định nghĩa rõ tiêu chí khi nào cần service layer:
   - Khi cần gọi nhiều DAO
   - Khi có business rules phức tạp
   - Khi cần transaction management
2. Triển khai service layer cho Order, Payment, Inventory modules
3. Giữ controllers chỉ để request handling

**Ưu tiên:** Cao

---

### HI-003: 16 trong 17 Controllers là Skeleton

**Vị trí:** Tất cả controller files ngoại trừ AuthController

**Mô tả vấn đề:**
Hầu hết controllers chỉ có routing và forwarding tới JSP, không có business logic. Controllers không gọi DAO, không validate input, không xử lý errors.

```java
// Pattern skeleton cần thay thế
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    String action = request.getParameter("action");
    if ("list".equals(action)) {
        // TODO: Gọi DAO, lấy dữ liệu
        request.getRequestDispatcher("/WEB-INF/views/.../list.jsp")
            .forward(request, response);
    }
}
```

**Rủi tiên:**
- Không có module hoàn chỉnh ngoại trừ Category
- Người dùng không thể thực hiện các thao tác CRUD thực tế

**Hành động khắc phục:**
1. Triển khai business logic trong controllers
2. Gọi DAO để truy xuất/cập nhật dữ liệu
3. Validate input trước khi xử lý
4. Xử lý errors và hiển thị flash messages

**Ưu tiên:** Cao

---

### HI-004: Không có Transaction Management

**Vị trí:** Tầng DAO và Service

**Mô tả vấn đề:**
Các thao tác nhiều bước (multi-step operations) không được bảo vệ bởi database transactions. Ví dụ: tạo đơn hàng bao gồm insert order, insert order_details, update inventory — nếu bước cuối thất bại, dữ liệu sẽ không nhất quán.

```java
// Vấn đề: không có transaction
public void createOrder(Order order, List<OrderDetail> details) {
    orderDAO.insert(order);           // Thành công
    for (OrderDetail d : details) {
        orderDetailDAO.insert(d);    // Thất bại tại đây
    }
    inventoryDAO.update(d);          // Không chạy
    // Kết quả: order tồn tại không có details
}
```

**Rủi ro:**
- Dữ liệu không nhất quán (inconsistent data)
- Khó debug khi có lỗi xảy ra
- Không thể rollback khi có lỗi

**Hành động khắc phục:**
1. Sử dụng `connection.setAutoCommit(false)` trong multi-DAO operations
2. Triển khai transaction management trong service layer
3. Thêm rollback trong catch block
4. Cân nhắc sử dụng Spring @Transactional nếu chuyển sang Spring

**Ưu tiên:** Cao

---

### HI-005: Không có Input Validation Framework

**Vị trề:** Tầng Controller

**Mô tả vấn đề:**
Không có cơ chế validation dữ liệu đầu vào tập trung. Mỗi controller tự xử lý validation, dẫn đến code lặp lại và không nhất quán.

**Rủi ro:**
- SQL injection có thể xảy ra nếu input không được sanitize
- Dữ liệu không hợp lệ được lưu vào database
- XSS có thể xảy ra nếu input được hiển thị trực tiếp
- Business rules không được enforce

**Hành động khắc phục:**
1. Sử dụng Bean Validation (JSR-380) với `hibernate-validator`
2. Định nghĩa constraints trong model classes
3. Validate ở controller layer với `@Valid`
4. Thêm sanitize utility cho HTML output

**Ưu tiên:** Cao

---

## Mức độ MEDIUM — Cần lên kế hoạch

### ME-001: Service Layer Criteria không rõ ràng

**Vị trí:** `service/` packages

**Mô tả vấn đề:**
Không có quy tắc rõ ràng về khi nào nên thêm service layer. Quyết định này được để lại cho developer, dẫn đến không nhất quán.

**Hành động khắc phục:**
1. Định nghĩa tiêu chí trong AGENTS.md hoặc architecture docs:
   - Cần gọi ≥2 DAOs → Service
   - Có business rules phức tạp → Service
   - Cần transaction → Service
   - CRUD đơn giản → Gọi DAO trực tiếp từ Controller
2. Review và refactor khi triển khai các module lớn

---

### ME-002: Repeated Parameter Parsing trong Controllers

**Vị trí:** Tầng Controller

**Mô tả vấn đề:**
Các thao tác như parse page number, sort field, sort direction, search keyword được lặp lại ở nhiều controller.

```java
// Pattern lặp lại
int page = 1;
String pageStr = request.getParameter("page");
if (pageStr != null && !pageStr.isEmpty()) {
    page = Integer.parseInt(pageStr);
}
```

**Hành động khắc phục:**
1. Tạo utility class để parse common parameters
2. Hoặc sử dụng base controller class với helper methods
3. Cân nhắc sử dụng framework như Spring MVC với binding

---

### ME-003: Không có Error Handling Framework

**Vị trí:** Toàn bộ ứng dụng

**Mô tả vấn đề:**
Không có cơ chế xử lý lỗi tập trung. Mỗi servlet tự xử lý exceptions, dẫn đến:
- Error messages không nhất quán
- Không có logging tập trung
- Khó debug production issues

**Hành động khắc phục:**
1. Cấu hình error pages trong `web.xml`
2. Tạo exception hierarchy (BusinessException, DataAccessException)
3. Triển khai servlet exception handler
4. Thêm logging với SLF4J/Logback

---

### ME-004: Không có CSRF Protection

**Vị trí:** Tất cả POST forms

**Mô tả vấn đề:**
Các form POST không có CSRF token, cho phép Cross-Site Request Forgery attacks.

**Hành động khắc phục:**
1. Triển khai CSRF filter
2. Thêm hidden token field trong tất cả forms
3. Validate token trên server
4. Sử dụng Double Submit Cookie pattern

---

### ME-005: Print Functionality trong Category Module

**Vị trí:** `category/` folder (files cần tích hợp)

**Mô tả vấn đề:**
Module Category có chức năng in không mong muốn (print buttons, print CSS files) cần được loại bỏ trước khi tích hợp vào ứng dụng chính.

**Hành động khắc phục:**
1. Loại bỏ print buttons khỏi JSP files
2. Loại bỏ print CSS files
3. Loại bỏ JavaScript print functions
4. Verify sau khi tích hợp

---

## Mức độ LOW — Cải thiện khi có thời gian

### LO-001: Planning Documents tham chiếu NetBeans Ant Project

**Vị trí:** `docs/planning/*`

**Mô tả vấn đề:**
Một số tài liệu quy hoạch tham chiếu đến cấu trúc dự án cũ (NetBeans Ant) thay vì cấu trúc Maven hiện tại.

**Hành động khắc phục:**
1. Cập nhật references trong planning documents
2. Thay `build.xml`, `dist/` bằng `pom.xml`, `target/`
3. Thay `src/java/common` bằng `src/java/com.storemanagement/common`

---

### LO-002: Documentation cần đồng bộ với Implementation

**Vị trí:** Toàn bộ `docs/` folder

**Mô tả vấn đề:**
Tài liệu có thể không phản ánh đúng trạng thái implementation thực tế do tiến độ phát triển nhanh.

**Hành động khắc phục:**
1. Thêm quy tắc: cập nhật docs khi thay đổi architecture/feature
2. Review docs trước mỗi sprint
3. Đánh dấu version trong mỗi tài liệu

---

### LO-003: Không có Automated Test Coverage

**Vị trí:** Toàn bộ codebase

**Mô tả vấn đề:**
Không có unit tests hoặc integration tests cho bất kỳ module nào.

**Hành động khắc phục:**
1. Thêm JUnit 5 và Mockito vào pom.xml
2. Viết tests cho service layer và DAO layer
3. Thêm integration tests với H2 in-memory database
4. Target: 70% coverage cho business logic

---

### LO-004: Không có CI/CD Pipeline

**Vị trí:** Repository configuration

**Mô tả vấn đề:**
Không có automated build, test, và deployment pipeline.

**Hành động khắc phục:**
1. Cấu hình GitHub Actions hoặc GitLab CI
2. Pipeline stages: build → test → deploy to staging → manual approval → deploy to production
3. Thêm automated database migration (Flyway/Liquibase)
4. Thêm SonarQube cho code quality check

---

## Tổng kết Technical Debt

| Mức độ | Số lượng | Tổng điểm (3-2-1) |
|---|---|---|
| CRITICAL | 3 | 9 |
| HIGH | 5 | 10 |
| MEDIUM | 5 | 5 |
| LOW | 4 | 2 |
| **Tổng** | **17** | **26** |

### Top 3 ưu tiên giải quyết

1. **CR-002**: Triển khai Authentication thực (DB verification, password hashing)
2. **CR-001**: Externalize DB credentials (environment variables)
3. **CR-003**: Triển khai Role-based Authorization

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

# Authentication Module - FinoraRetail

## 1. Tổng quan Module

Module Authentication (xác thực) là module cốt lõi của hệ thống FinoraRetail, chịu trách nhiệm quản lý toàn bộ quy trình xác thực người dùng bao gồm đăng nhập, đăng ký, khôi phục mật khẩu và đăng xuất. Module này được bảo vệ bởi `AuthFilter` — một servlet filter kiểm soát quyền truy cập vào 21 route patterns quan trọng của hệ thống.

**Trạng thái hiện tại:** `Implemented` (Demo Mode — chưa kết nối cơ sở dữ liệu, mật khẩu không mã hóa)

**Package:** `controller.auth`

**Vị trí tệp:** `src/main/java/controller/auth/AuthController.java`

---

## 2. Kiến trúc Module

### 2.1 Các thành phần chính

| Thành phần | Loại | Vị trí |
|---|---|---|
| AuthController | Servlet | `controller.auth.AuthController` |
| AuthFilter | Filter | `filter.AuthFilter` |
| User | Model | `model.User` |
| login.jsp | View | `WEB-INF/views/auth/login.jsp` |
| register.jsp | View | `WEB-INF/views/auth/register.jsp` |
| forgot-password.jsp | View | `WEB-INF/views/auth/forgot-password.jsp` |

### 2.2 Sơ đồ luồng xác thực

```
Client Request
      │
      ▼
┌─────────────────┐
│   AuthFilter    │◄── Bảo vệ 21 route patterns
└────────┬────────┘
         │ (kiểm tra session)
         ▼
┌─────────────────┐     ┌──────────────────┐
│  AuthController │────►│  Session Attrs   │
│   (Servlet)     │     │  currentUser     │
└────────┬────────┘     └──────────────────┘
         │
    ┌────┴────┐
    ▼         ▼
  GET       POST
```

---

## 3. Routes và Endpoints

Module định nghĩa các route sau:

| Method | Route | Chức năng | Trạng thái |
|---|---|---|---|
| GET | `/login` | Hiển thị trang đăng nhập | Hoạt động |
| POST | `/login` | Xử lý đăng nhập | Hoạt động (demo) |
| GET | `/register` | Hiển thị trang đăng ký | Hoạt động |
| POST | `/register` | Xử lý đăng ký | Hoạt động (demo) |
| GET | `/forgot-password` | Hiển thị trang khôi phục mật khẩu | Hoạt động |
| POST | `/forgot-password` | Xử lý khôi phục mật khẩu | Hoạt động (demo) |
| GET | `/logout` | Đăng xuất và hủy session | Hoạt động |

### 3.1 Luồng xử lý GET request

```
GET /login ──► AuthController.doGet() ──► forward ──► login.jsp
GET /register ──► AuthController.doGet() ──► forward ──► register.jsp
GET /forgot-password ──► AuthController.doGet() ──► forward ──► forgot-password.jsp
GET /logout ──► invalidate session ──► redirect ──► /login
```

### 3.2 Luồng xử lý POST /login

```
POST /login ──► AuthController.doPost()
                        │
                        ▼
              ┌─────────────────────┐
              │  Lấy tham số:       │
              │  username, password │
              └──────────┬──────────┘
                         ▼
              ┌─────────────────────┐
              │  Demo Mode Check:   │
              │  Hardcoded validation│
              └──────────┬──────────┘
                         ▼
              ┌─────────────────────┐
              │  Tạo User object:   │
              │  role = "OWNER"     │
              │  (hardcoded)        │
              └──────────┬──────────┘
                         ▼
              ┌─────────────────────┐
              │  Set session attr:  │
              │  currentUser = User │
              └──────────┬──────────┘
                         ▼
              ┌─────────────────────┐
              │  Redirect to:      │
              │  /dashboard/owner   │
              └─────────────────────┘
```

---

## 4. AuthFilter — Bảo vệ Route

### 4.1 Giới thiệu

`AuthFilter` là một servlet filter triển khai interface `Filter` của Jakarta Servlet API, hoạt động như một tường lửa bảo vệ các route quan trọng của hệ thống. Filter này kiểm tra sự tồn tại của session attribute `currentUser` trước khi cho phép request tiến vào các tài nguyên được bảo vệ.

### 4.2 Các route được bảo vệ (21 patterns)

Filter bảo vệ các nhóm route sau:

| Nhóm | Route Patterns | Phạm vi |
|---|---|---|
| Dashboard | `/dashboard/*` | Tất cả dashboard (owner, inventory, financial) |
| Users | `/users/*` | Quản lý người dùng |
| Categories | `/categories/*` | Quản lý danh mục |
| Products | `/products/*` | Quản lý sản phẩm |
| Customers | `/customers/*` | Quản lý khách hàng |
| Suppliers | `/suppliers/*` | Quản lý nhà cung cấp |
| Stores | `/stores/*` | Quản lý cửa hàng |
| Orders | `/orders/*` | Quản lý đơn hàng |
| Inventory | `/inventory/*` | Quản lý tồn kho |
| Payments | `/payments/*` | Xử lý thanh toán (Protected Area) |
| Invoices | `/invoices/*` | Quản lý hóa đơn |
| Expenses | `/expenses/*` | Quản lý chi phí |
| Income | `/income/*` | Quản lý thu nhập |
| Purchase Orders | `/purchase-orders/*` | Quản lý đơn đặt hàng |
| Reports | `/reports/*` | Báo cáo |
| Roles | `/roles/*` | Quản lý vai trò (Protected Area) |
| Activity Logs | `/activity-logs/*` | Nhật ký hoạt động |
| Notifications | `/notifications/*` | Thông báo |
| Config | `/config/*` | Cấu hình hệ thống |

### 4.3 Logic kiểm tra

```java
// Kiểm tra sự tồn tại của currentUser trong session
HttpSession session = request.getSession(false);
User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

if (currentUser == null) {
    // Không có session hoặc chưa đăng nhập → chuyển hướng về /login
    response.sendRedirect(request.getContextPath() + "/login");
    return;
}

// Đã đăng nhập → cho phép request tiến tới
chain.doFilter(request, response);
```

### 4.4 Các route được loại trừ (public routes)

Các route sau **không** được AuthFilter bảo vệ:

- `/login` — Trang đăng nhập
- `/register` — Trang đăng ký
- `/forgot-password` — Trang khôi phục mật khẩu
- `/logout` — Xử lý đăng xuất
- `/` — Trang chủ
- Các tài nguyên tĩnh (CSS, JS, images)

---

## 5. Session Management

### 5.1 Session attributes

Module sử dụng các session attribute sau:

| Attribute | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `currentUser` | `User` | Đối tượng User của người dùng hiện tại |
| `successMessage` | `String` | Thông báo thành công (flash message) |
| `errorMessage` | `String` | Thông báo lỗi (flash message) |

### 5.2 Flash messages

Module sử dụng cơ chế flash message thông qua session để truyền thông báo phản hồi từ server về cho người dùng sau khi chuyển hướng:

```java
// Đặt flash message
session.setAttribute("successMessage", "Đăng nhập thành công!");
session.setAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng.");

// Sau khi redirect, hiển thị trong JSP và xóa
String success = (String) session.getAttribute("successMessage");
if (success != null) {
    session.removeAttribute("successMessage");
    // Hiển thị cho người dùng
}
```

---

## 6. Trạng thái Demo Mode

### 6.1 Hạn chế hiện tại

Module đang chạy ở chế độ demo với các hạn chế sau:

| Hạn chế | Mô tả | Hành động cần thiết |
|---|---|---|
| Không xác thực DB | Đăng nhập không kiểm tra cơ sở dữ liệu | Triển khai UserDAO để xác thực |
| Role hardcoded | Luôn gán role "OWNER" cho user đăng nhập | Truy vấn role từ bảng users |
| Mật khẩu không mã hóa | Password lưu dạng plain text | Triển khai BCrypt hashing |
| Không có đăng ký thực | POST /register chưa lưu vào DB | Triển khai UserDAO.insert() |
| Không có khôi phục mật khẩu | POST /forgot-password chưa xử lý | Triển khai email OTP/token |

### 6.2 Triển khai BCrypt (cần thiết khi nâng cấp)

```java
// Sử dụng BCrypt để hash mật khẩu
import org.mindrot.jbcrypt.BCrypt;

// Hash mật khẩu khi lưu
String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

// Xác thực mật khẩu khi đăng nhập
boolean isValid = BCrypt.checkpw(inputPassword, storedHash);
```

### 6.3 Đối tượng User trong session

```java
public class User {
    private int userId;
    private String username;
    private String email;
    private String fullName;
    private String role;        // "OWNER", "ADMIN", "STAFF"
    private String status;      // "ACTIVE", "INACTIVE"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 7. Bảng cơ sở dữ liệu

### 7.1 Bảng users (schema dự kiến)

```sql
CREATE TABLE users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100),
    role_id INT,
    store_id INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    last_login DATETIME,
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (store_id) REFERENCES stores(store_id)
);
```

---

## 8. Tích hợp với Role-Based Access Control

### 8.1 RolePermissionUtil

Module tích hợp với `RolePermissionUtil` để kiểm tra quyền truy cập dựa trên vai trò:

```java
// Kiểm tra quyền trong controller
if (!RolePermissionUtil.hasPermission(session, "VIEW_DASHBOARD")) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
    return;
}
```

### 8.2 Các vai trò dự kiến

| Vai trò | Mã | Mô tả |
|---|---|---|
| Chủ cửa hàng | `OWNER` | Toàn quyền truy cập hệ thống |
| Quản trị viên | `ADMIN` | Quản lý người dùng, cấu hình |
| Nhân viên | `STAFF` | Thao tác CRUD trên dữ liệu được phân công |

---

## 9. Protected Area

Module Authentication thuộc **Protected Area** của hệ thống. Các thành phần sau không được chỉnh sửa trừ khi có yêu cầu rõ ràng từ người dùng:

- Luồng xác thực và AuthFilter
- Cơ chế quản lý session
- RolePermissionUtil và kiểm soát truy cập
- web.xml và cấu hình bảo mật

---

## 10. Mở rộng trong tương lai

Các tính năng cần triển khai để nâng cấp từ demo lên production:

| Tính năng | Ưu tiên | Mô tả |
|---|---|---|
| BCrypt hashing | Cao | Mã hóa mật khẩu với BCrypt |
| Xác thực Database | Cao | Kiểm tra credentials trong bảng users |
| Phân quyền theo role | Cao | Role-based access control thực sự |
| Remember me | Trung bình | Token remember-me cho đăng nhập tự động |
| Đăng nhập 2 yếu tố (2FA) | Thấp | Xác thực hai bước qua email/SMS |
| OAuth2/Google Login | Thấp | Đăng nhập qua Google |
| Session timeout | Trung bình | Tự động hủy session sau thời gian không hoạt động |
| Đăng nhập đăng ký thực | Cao | Lưu người dùng mới vào database |
| Khôi phục mật khẩu | Cao | Gửi email với link đặt lại mật khẩu |

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*
*Module owner: Authentication Team*

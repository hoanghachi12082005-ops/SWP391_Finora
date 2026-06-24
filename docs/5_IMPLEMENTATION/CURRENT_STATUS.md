# Trạng thái triển khai hiện tại - FinoraRetail

## Tổng quan

Dự án **SWP391_Finora** (FinoraRetail) là một ứng dụng web Java dạng WAR được xây dựng trên nền tảng Maven, phục vụ cho hệ thống quản lý bán lẻ đa cửa hàng. Ứng dụng được thiết kế theo kiến trúc phân lớp MVC với Jakarta Servlet/JSP APIs, kết nối cơ sở dữ liệu SQL Server, và sử dụng Bootstrap 5 cho giao diện người dùng.

---

## Thông tin hệ thống

| Thuộc tính | Chi tiết |
|---|---|
| **Loại ứng dụng** | Maven Java WAR web application |
| **Môi trường Runtime** | Apache Tomcat 10.1 |
| **Java version** | JDK 17 |
| **API** | Jakarta Servlet / JSP (jakartaEE) |
| **Cơ sở dữ liệu** | SQL Server — `DBFinoraV2` |
| **Build System** | Maven (`pom.xml`) |
| **Output** | `target/StoreManagementNetBeans.war` |
| **Context Path** | `/FinoraRetail` |
| **Package gốc** | `com.storemanagement` với các subpackages theo feature |

---

## Cấu trúc Package

Ứng dụng sử dụng cấu trúc package theo nguyên tắc **feature-owned packages** — mỗi module sở hữu package riêng:

```
com.storemanagement
├── auth/           — Xác thực, AuthFilter
├── dashboard/      — Dashboard
├── category/       — Quản lý danh mục
├── product/        — Quản lý sản phẩm
├── customer/       — Quản lý khách hàng
├── supplier/       — Quản lý nhà cung cấp
├── store/          — Quản lý cửa hàng
├── order/          — Quản lý đơn hàng
├── payment/        — Thanh toán, hóa đơn
├── inventory/      — Quản lý tồn kho
├── finance/        — Thu nhập, chi phí
├── report/         — Báo cáo
├── user/           — Quản lý người dùng
├── role/           — Quản lý vai trò
├── common/         — Shared utilities
│   ├── util/
│   │   ├── DatabaseUtil.java
│   │   ├── AuthFilter.java
│   │   └── ModuleRegistry.java
│   └── dao/
│       └── ICrudDAO.java
```

---

## Ma trận trạng thái triển khai theo Module

### Module Authentication — `auth/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| AuthFilter | Hoạt động | Bảo vệ 21 route patterns, kiểm tra session |
| AuthController | Demo mode | Chấp nhận bất kỳ username/password nào |
| login.jsp | Hoạt động | Giao diện đăng nhập Bootstrap 5 |
| register.jsp | Skeleton | Form tồn tại, chưa xử lý |
| forgot-password.jsp | Skeleton | Form tồn tại, chưa xử lý |
| users table | Schema sẵn có | Password không mã hóa |

**Ghi chú:** Authentication đang ở chế độ demo. Không có xác thực cơ sở dữ liệu thực sự. Mọi username/password đều được chấp nhận và tạo session với role hardcoded là `OWNER`.

### Module Dashboard — `dashboard/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| DashboardController | Skeleton | Routing tới các view |
| owner.jsp | Skeleton template | HTML tĩnh, chưa có dữ liệu |
| inventory.jsp | Skeleton template | HTML tĩnh, chưa có dữ liệu |
| financial.jsp | Skeleton template | HTML tĩnh, chưa có dữ liệu |

**Ghi chú:** Các dashboard sử dụng template HTML tĩnh với Bootstrap cards, chưa kết nối dữ liệu thực từ database.

### Module Category — `category/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| CategoryServlet | Hoàn chỉnh | CRUD, tìm kiếm, phân trang |
| CategoryDAO | Hoàn chỉnh | SQL với CTE để ngăn circular reference |
| Category.java | Hoàn chỉnh | Getters/setters, basic validation |
| list.jsp, add.jsp, edit.jsp | Hoàn chỉnh | Giao diện đầy đủ |
| category table | Schema sẵn có | Có hỗ trợ parent_id cho cây phân cấp |
| **Vị trí code** | Thư mục `category/` bên ngoài | Chưa tích hợp vào `src/java/com.storemanagement` |

**Ghi chú:** Module Category có mã nguồn đầy đủ nhưng nằm trong thư mục `category/` ở gốc dự án thay vì trong `src/java`. Cần tích hợp vào cấu trúc Maven chuẩn.

### Module Product — `product/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| ProductServlet | Hoàn chỉnh | CRUD, tìm kiếm, phân trang |
| ProductDAO | Hoàn chỉnh | SQL cơ bản |
| Product.java | Hoàn chỉnh | Model với relationships |
| list.jsp, add.jsp, edit.jsp, detail.jsp | Hoàn chỉnh | Giao diện đầy đủ |
| products table | Schema sẵn có | Có product_images table riêng |
| **Vị trí code** | Thư mục `category/` bên ngoài | Chưa tích hợp vào `src/java/com.storemanagement` |

**Ghi chú:** Tương tự Category, mã nguồn Product nằm trong thư mục `category/`. Cần tích hợp và cấu trúc lại.

### Module Order — `order/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| OrderServlet | Skeleton | Routing, chưa có business logic |
| OrderDAO | Skeleton | Interface và stub methods |
| Order.java | Skeleton | Model với getters/setters |
| list.jsp, detail.jsp, create.jsp | Skeleton | UI template sẵn có |
| orders table | Schema sẵn có | Có order_details table |
| **Trạng thái** | Chưa triển khai | Business logic hoàn toàn chưa có |

### Module Inventory — `inventory/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| InventoryServlet | Skeleton | Routing, chưa có business logic |
| InventoryDAO | Skeleton | Interface và stub methods |
| Inventory.java | Skeleton | Model với getters/setters |
| list.jsp, adjustment.jsp, transfer.jsp | Skeleton | UI template sẵn có |
| inventory table | Schema sẵn có | Có inventory_logs table |
| **Trạng thái** | Chưa triển khai | Business logic hoàn toàn chưa có |

### Module Payment & Invoice — `payment/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| PaymentInvoiceController | Skeleton | Routing, chưa có business logic |
| PaymentDAO, InvoiceDAO | Skeleton | Interface và stub methods |
| Payment.java, Invoice.java | Skeleton | Model với getters/setters |
| views/payments/index.jsp, views/invoices/list.jsp | Skeleton | UI template sẵn có |
| payment table | Schema sẵn có | Có invoice table |
| **Trạng thái** | Chưa triển khai | Protected Area — chưa có logic |

### Module Finance — `finance/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| IncomeExpenseController | Skeleton | Routing, chưa có business logic |
| IncomeDAO, ExpenseDAO | Skeleton | Interface và stub methods |
| Income.java, Expense.java | Skeleton | Model với getters/setters |
| views/income/list.jsp, views/expenses/list.jsp, add.jsp | Skeleton | UI template sẵn có |
| **Trạng thái** | Chưa triển khai | Protected Area — chưa có logic |

### Module Report — `report/`

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| ReportController | Skeleton | Routing, chưa có logic tổng hợp |
| ExportUtil | Not implemented | File tồn tại nhưng chưa có code |
| views/reports/*.jsp | Skeleton | 6 view templates sẵn có |
| **Trạng thái** | Chưa triển khai | Báo cáo và xuất file chưa có logic |

### Các Module khác

| Module | Controller | DAO | Model | Views | Schema |
|---|---|---|---|---|---|
| Customer | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| Supplier | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| Store | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| User Management | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| Role Management | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| Activity Log | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| Notification | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| Configuration | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| SEO | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |
| Purchase Order | Skeleton | Skeleton | Skeleton | Skeleton | ✅ Có |

---

## Tổng kết theo trạng thái

| Trạng thái | Số lượng module | Chi tiết |
|---|---|---|
| **Hoàn chỉnh** | 1 | Authentication (demo mode) |
| **Hoàn chỉnh, cần tích hợp** | 1 | Category, Product (code ở thư mục ngoài, cần tích hợp) |
| **Skeleton — UI template** | 18 | Tất cả module còn lại |
| **Chưa triển khai** | 18 | Business logic, SQL, service layer |

---

## Kiến trúc hệ thống hiện tại

### Nền tảng đã hoàn thiện

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| Maven project structure | ✅ Hoàn chỉnh | pom.xml với dependencies |
| Tomcat 10.1 configuration | ✅ Hoàn chỉnh | web.xml, context.xml |
| Jakarta Servlet/JSP APIs | ✅ Hoàn chỉnh | Cấu hình đúng |
| SQL Server JDBC connection | ✅ Hoàn chỉnh | DatabaseUtil.java |
| AuthFilter protection | ✅ Hoạt động | Bảo vệ 21 route patterns |
| Bootstrap 5 design system | ✅ Hoàn chỉnh | Giao diện responsive |
| DAO pattern (ICrudDAO interface) | ✅ Hoàn chỉnh | Interface chuẩn |
| JSP views under WEB-INF/views | ✅ Hoàn chỉnh | Cấu trúc đúng |

### Kiến trúc cần hoàn thiện

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| Service layer logic | ❌ Skeleton | Không có business logic thực |
| DAO SQL implementation | ❌ Skeleton | Chỉ có stub methods |
| Controller business logic | ❌ Skeleton | Chỉ có routing |
| Database transactions | ❌ Chưa có | Không có transaction management |
| Input validation | ❌ Chưa có | Không có validation framework |
| Error handling | ❌ Không chuẩn | Không có error handling framework |
| CSRF protection | ❌ Chưa có | Form không có CSRF token |

---

## Các khoảng trống nghiêm trọng (Critical Gaps)

### 1. Không có xác thực thực sự

**Vấn đề:** AuthController demo mode — chấp nhận mọi username/password, tạo session với role hardcoded.

**Rủi ro:** Bất kỳ ai cũng có thể truy cập hệ thống với quyền OWNER.

**Cần:** Triển khai xác thực với cơ sở dữ liệu, mã hóa password (BCrypt), và logout.

### 2. Thông tin đăng nhập CSDL hardcoded

**Vấn đề:** DB credentials (sa/12345) nằm trong DBContext.java.

**Rủi ro:** Thông tin đăng nhập database bị lộ trong source code.

**Cần:** Di chuyển ra biến môi trường hoặc file cấu hình riêng.

### 3. Không có phân quyền theo vai trò

**Vấn đề:** AuthFilter chỉ kiểm tra session, không kiểm tra role.

**Rủi ro:** Mọi user đã đăng nhập đều có thể truy cập mọi route được bảo vệ.

**Cần:** Triển khai RolePermissionUtil để kiểm tra quyền theo role.

### 4. Không có Service Layer logic

**Vấn đề:** Tất cả service classes đều là skeleton rỗng.

**Rủi ro:** Business logic nằm rải rác trong controllers và DAOs, khó bảo trì.

**Cần:** Định nghĩa rõ khi nào cần thêm service layer và triển khai.

### 5. Không có quản lý transaction

**Vủi ro:** Các thao tác nhiều bước (order + inventory + payment) không được bảo vệ bởi transaction.

**Cần:** Triển khai transaction management trong các DAO hoặc service.

---

## Kế hoạch tiếp theo

1. **Tích hợp Category & Product** — Di chuyển code vào `src/java/com.storemanagement`
2. **Triển khai Authentication thực** — Xác thực database, mã hóa password, logout
3. **Triển khai Product Module** — CRUD đầy đủ với hình ảnh, tồn kho ban đầu
4. **Triển khai Order Module** — Tạo đơn, cập nhật tồn kho, thanh toán
5. **Triển khai Inventory Module** — Theo dõi tồn kho, cảnh báo, chuyển kho
6. **Externalize DB credentials** — Di chuyển ra biến môi trường
7. **Triển khai Role-based authorization** — Kiểm tra quyền theo vai trò

---

*Document version: 1.1*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

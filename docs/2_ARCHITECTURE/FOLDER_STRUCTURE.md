# Cấu Trúc Thư Mục Dự Án FinoraRetail

> **Mã tài liệu:** ARCH-FLD-001  
> **Phiên bản:** 1.0  
> **Ngày cập nhật:** 21/06/2026  
> **Tác giả:** Nhóm phát triển FinoraRetail  
> **Mục đích:** Mô tả kiến trúc thư mục và tổ chức mã nguồn của dự án SWP391_Finora (FinoraRetail)

---

## 1. Tổng Quan Kiến Trúc Thư Mục

Dự án FinoraRetail được tổ chức theo cấu trúc Maven WAR chuẩn cho ứng dụng web Java trên nền tảng Apache Tomcat 10.1 sử dụng Jakarta Servlet/JSP API. Toàn bộ mã nguồn tuân thủ quy tắc đặt tên theo PascalCase cho các class và camelCase cho các biến, method, tuân theo kiến trúc phân lớp MVC (Model-View-Controller) với lớp DAO (Data Access Object) riêng biệt cho truy xuất cơ sở dữ liệu.

```
FinoraRetail/
├── pom.xml                              # Cấu hình Maven build
├── database/
│   └── DBFinoraV3.sql                  # Schema SQL Server với 21 bảng
├── docs/                                # Tài liệu dự án
├── category/                            # Module ngoài (chờ tích hợp)
├── src/
│   └── main/
│       ├── java/                        # Mã nguồn Java
│       └── webapp/                      # Tài nguyên web
└── target/                              # Thư mục build output (không chỉnh sửa)
```

---

## 2. Cấp Độ Gốc (Root Level)

### 2.1. Tệp Cấu Hình Gốc

| Tệp | Mô tả |
|------|--------|
| `pom.xml` | Cấu hình Maven build, khai báo dependency, plugin compile, đầu ra WAR tại `target/StoreManagementNetBeans.war` |
| `database/DBFinoraV3.sql` | Script schema SQL Server chứa 21 bảng dữ liệu cho toàn bộ hệ thống |
| `docs/` | Thư mục tài liệu dự án, bao gồm kiến trúc, quy tắc, kế hoạch, và trạng thái triển khai |
| `category/` | Module category độc lập với package khác (pending integration) — cần tích hợp vào cấu trúc chính |
| `src/` | Thư mục gốc chứa toàn bộ mã nguồn ứng dụng |

---

## 3. Thư Mục Nguồn Java: `src/main/java/`

Tất cả các class Java được đặt trực tiếp dưới các package chức năng trong `src/main/java/` (không có tiền tố `com.storemanagement`). Kiến trúc phân lớp rõ ràng theo mô hình MVC + DAO + Service.

```
src/main/java/
├── constant/           # AppConstants.java
├── controller/         # Servlets xử lý HTTP requests (30+ servlets chia theo domain)
├── dao/                # Truy xuất dữ liệu SQL Server (Kế thừa DBContext)
├── dto/                # Data Transfer Objects
├── filter/             # SecurityFilter.java (RBAC, CSRF, Security Headers)
├── model/              # 51 POJO domain entities
├── service/            # Business logic layer (8 domain subpackages)
└── util/               # Subpackages tiện ích (database, security, email, finance, inventory, v.v.)
```

---

### 3.1. Package `controller/` — Tầng Điều Khiển (17 Servlet)

Package `controller` chứa các servlet xử lý yêu cầu HTTP, đóng vai trò điều phối giữa tầng View (JSP) và tầng Model/Service/DAO. Mỗi servlet được phân loại theo module nghiệp vụ tương ứng.

#### 3.1.1. Package Con của Controller

```
controller/
├── auth/
│   └── AuthController.java           # Đăng nhập, đăng xuất, đăng ký
├── common/
│   └── BaseController.java           # Servlet cơ sở với helper forward/redirect
├── dashboard/
│   └── DashboardController.java      # Điều hướng dashboard
├── product/
│   ├── CategoryServlet.java          # Quản lý danh mục (đã tích hợp mới)
│   ├── CategoryController.java       # Routing skeleton cho category
│   └── ProductController.java       # Routing skeleton cho product
├── customer/
│   └── CustomerController.java      # Routing skeleton cho customer
├── supplier/
│   └── SupplierController.java       # Routing skeleton cho supplier
├── store/
│   └── StoreController.java          # Routing skeleton cho store/branch
├── sales/
│   └── OrderController.java          # Routing skeleton cho order
├── inventory/
│   └── InventoryController.java      # Routing skeleton cho inventory
├── finance/
│   ├── IncomeExpenseController.java  # Routing skeleton cho finance
│   └── PaymentInvoiceController.java # Routing skeleton cho payment
├── purchase/
│   └── PurchaseOrderController.java  # Routing skeleton cho purchase
├── report/
│   └── ReportController.java          # Routing skeleton cho report
├── system/
│   └── SystemController.java          # Routing skeleton cho system
├── user/
│   └── UserController.java            # Routing skeleton cho user management
└── website/
    └── StaticPageController.java      # Trang tĩnh
```

#### 3.1.2. Class BaseController

`BaseController` là servlet trừu tượng cung cấp các phương thức helper dùng chung cho toàn bộ controller, bao gồm:

- `forward(String viewPath)` — Chuyển tiếp request đến JSP mà không thay đổi URL
- `redirect(String path)` — Chuyển hướng HTTP với mã 302
- `setAttribute(String key, Object value)` — Đặt attribute cho request
- `getSessionAttribute(String key)` — Lấy attribute từ session
- `setSessionAttribute(String key, Object value)` — Đặt attribute vào session

#### 3.1.3. Class AuthController

Xử lý các thao tác xác thực người dùng:

- `GET /login` — Hiển thị trang đăng nhập
- `POST /login` — Xử lý đăng nhập, thiết lập session `currentUser`
- `GET /logout` — Hủy session, chuyển hướng về trang đăng nhập
- `GET /register` — Hiển thị trang đăng ký
- `POST /register` — Xử lý đăng ký tài khoản mới

#### 3.1.4. Class CategoryServlet (Mới Tích Hợp)

Servlet quản lý danh mục sản phẩm, được tích hợp vào hệ thống với các chức năng:

- `GET /categories` — Danh sách tất cả danh mục (phân trang, tìm kiếm)
- `GET /categories/create` — Form tạo danh mục mới
- `POST /categories/create` — Xử lý tạo danh mục
- `GET /categories/edit?id=X` — Form chỉnh sửa danh mục
- `POST /categories/update` — Xử lý cập nhật danh mục
- `POST /categories/delete` — Xóa danh mục (soft delete theo trạng thái)

---

### 3.2. Package `dao/` — Tầng Truy Xuất Dữ Liệu (17 DAO)

Package `dao` chứa các class truy xuất cơ sở dữ liệu SQL Server thông qua JDBC. Mỗi DAO chịu trách nhiệm thao tác trên một bảng hoặc nhóm bảng liên quan trong schema.

```
dao/
├── common/
│   ├── BaseDAO.java                  # Class cơ sở với getConnection()
│   └── ICrudDAO.java                 # Interface CRUD generic
├── auth/
├── dashboard/
├── product/
│   ├── CategoryDAO.java             # CategoryDAO hoàn chỉnh
│   └── ProductDAO.java
├── customer/
│   └── CustomerDAO.java
├── supplier/
│   └── SupplierDAO.java
├── store/
│   └── StoreDAO.java
├── sales/
│   └── OrderDAO.java
├── inventory/
│   └── InventoryDAO.java
├── finance/
│   ├── IncomeExpenseDAO.java
│   └── PaymentInvoiceDAO.java
├── purchase/
│   └── PurchaseOrderDAO.java
├── report/
│   └── ReportDAO.java
├── system/
│   └── SystemDAO.java
├── user/
│   └── UserDAO.java
└── website/
    └── WebsiteDAO.java
```

#### 3.2.1. Class BaseDAO

Là class trừu tượng cung cấp phương thức `getConnection()` sử dụng `DBContext` để lấy kết nối JDBC đến SQL Server. Tất cả các DAO khác kế thừa từ `BaseDAO`.

```java
public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }
}
```

#### 3.2.2. Interface ICrudDAO

Interface generic định nghĩa các phương thức CRUD chuẩn:

- `T findById(int id)`
- `List<T> findAll()`
- `boolean insert(T entity)`
- `boolean update(T entity)`
- `boolean delete(int id)`

#### 3.2.3. Class CategoryDAO (Hoàn Chỉnh)

DAO duy nhất được triển khai hoàn chỉnh trong hệ thống hiện tại. Bao gồm:

- `List<Category> findAll()` — Truy vấn tất cả danh mục với JOIN lấy parentName
- `Category findById(int id)` — Tìm theo ID
- `List<Category> findByStatus(boolean active)` — Lọc theo trạng thái
- `boolean insert(Category category)` — Tạo mới
- `boolean update(Category category)` — Cập nhật
- `boolean delete(int id)` — Xóa mềm (cập nhật status)
- `List<Category> search(String keyword)` — Tìm kiếm theo tên
- `int countProducts(int categoryId)` — Đếm số sản phẩm trong danh mục
- `List<Category> findRootCategories()` — Danh mục gốc (không có parent)

---

### 3.3. Package `model/` — Tầng Mô Hình Nghiệp Vụ (19 Models)

Package `model` chứa các domain entity đại diện cho dữ liệu nghiệp vụ, được sử dụng xuyên suốt từ DAO đến Controller và JSP.

```
model/
├── BaseModel.java                     # Abstract base model
├── User.java
├── Role.java
├── Store.java
├── Customer.java
├── Supplier.java
├── Category.java                      # Category model
├── Product.java
├── Order.java
├── OrderDetail.java
├── Invoice.java
├── Payment.java
├── Income.java
├── Expense.java
├── PurchaseOrder.java
├── PurchaseDetail.java
├── ActivityLog.java
├── Notification.java
├── BusinessConfiguration.java
├── StockTransaction.java
└── InventoryItem.java
```

#### 3.3.1. Class BaseModel

Class trừu tượng làm cơ sở cho tất cả entity, chứa các trường chung:

```java
public abstract class BaseModel {
    protected int id;
    protected String name;
    protected boolean status;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}
```

#### 3.3.2. Class Category

Model danh mục sản phẩm với các thuộc tính:

| Thuộc tính | Kiểu | Mô tả |
|------------|------|--------|
| `categoryId` | int | Khóa chính |
| `name` | String | Tên danh mục |
| `description` | String | Mô tả chi tiết |
| `parentId` | Integer | ID danh mục cha (null nếu là danh mục gốc) |
| `parentName` | String | Tên danh mục cha (JOIN từ CategoryDAO) |
| `status` | boolean | Trạng thái hoạt động |
| `productCount` | int | Số sản phẩm trong danh mục |

---

### 3.4. Package `service/` — Tầng Nghiệp Vụ (19 Services)

Package `service` chứa các class xử lý logic nghiệp vụ, hiện tại đang ở dạng skeleton. Theo kiến trúc dự định, service layer sẽ đóng vai trò trung gian giữa controller và DAO khi cần các nghiệp vụ phức tạp.

```
service/
├── auth/
│   └── AuthService.java
├── dashboard/
│   └── DashboardService.java
├── product/
│   ├── CategoryService.java
│   └── ProductService.java
├── customer/
│   └── CustomerService.java
├── supplier/
│   └── SupplierService.java
├── store/
│   └── StoreService.java
├── sales/
│   └── OrderService.java
├── inventory/
│   └── InventoryService.java
├── finance/
│   ├── IncomeExpenseService.java
│   └── PaymentInvoiceService.java
├── purchase/
│   └── PurchaseOrderService.java
├── report/
│   └── ReportService.java
├── system/
│   └── SystemService.java
├── user/
│   └── UserService.java
└── website/
    └── WebsiteService.java
```

**Chính sách triển khai service layer:** Hiện tại, các controller có thể gọi trực tiếp DAO cho các thao tác CRUD đơn giản. Service layer chỉ cần triển khai thực sự khi:

- Cần phối hợp nhiều DAO trong một nghiệp vụ
- Có boundary giao dịch (transaction) bao gồm nhiều thao tác SQL
- Logic nghiệp vụ được tái sử dụng bởi nhiều controller
- Quy tắc nghiệp vụ quá phức tạp để đặt trong servlet

---

### 3.5. Package `filter/` — Bộ Lọc Bảo Mật & Phân Quyền

```
filter/
└── SecurityFilter.java                # Central Security Filter (urlPatterns = "/*")
```

#### 3.5.1. Class SecurityFilter

Filter central chịu trách nhiệm bảo vệ toàn bộ ứng dụng:

- **Phân quyền Role Matrix (`ROLE_MAP`):**
  - `/system/*` ➔ `admin`, `owner`
  - `/management/*` ➔ `admin`, `owner`, `storemanager`, `warehousestaff`
  - `/pos/*` ➔ `admin`, `owner`, `storemanager`, `salesstaff`
  - `/owner/*` ➔ `owner`, `storemanager`, `salesstaff`, `warehousestaff`
- **Đường dẫn công khai (`PUBLIC_PATHS`):** `/login`, `/logout`, `/forgot-password`, `/register`, `/role-selection`, `/assets/*`, `/css/*`, `/js/*`, `/static/*`, `/vnpay/ipn`, `/vnpay/return`, `/order/status`
- **Xác thực CSRF & Audit Logging:** Kiểm tra CSRF token đối với mọi request `POST`, ghi nhật ký truy cập vi phạm qua `ActivityLogDAO`.
- **Security Headers:** Thiết lập `Cache-Control`, `X-Frame-Options: SAMEORIGIN`, `X-Content-Type-Options: nosniff`.

---

### 3.6. Package `util/` — Các Tiện Ích Chung (10 Utilities)

```
util/
├── auth/
│   └── AuthUtil.java                 # Helper kiểm tra session
├── database/
│   └── DBContext.java                # Kết nối JDBC SQL Server
├── finance/
│   └── MoneyUtil.java                # Định dạng tiền tệng VND
├── inventory/
├── product/
├── report/
├── security/
├── system/
├── user/
├── validation/
└── website/
```

#### 3.6.1. Class DBContext

Cung cấp phương thức `getConnection()` trả về `java.sql.Connection` đến SQL Server database `DBFinoraV3`. Sử dụng JDBC driver với cấu hình từ `context.xml`.

#### 3.6.2. Class AuthUtil

Utility hỗ trợ kiểm tra phiên đăng nhập:

- `isLoggedIn(HttpSession session)` — Kiểm tra session có `currentUser`
- `getCurrentUser(HttpSession session)` — Lấy user object từ session
- `requireLogin(HttpServletRequest request, HttpServletResponse response)` — Chuyển hướng nếu chưa đăng nhập

#### 3.6.3. Class MoneyUtil

Xử lý định dạng tiền tệ Việt Nam Đồng:

- `formatVND(double amount)` — Định dạng số thành chuỗi `1.000.000 ₫`
- `parseVND(String amount)` — Parse chuỗi VND thành số
- `roundToNearestThousand(int amount)` — Làm tròn đến hàng nghìn

---

## 4. Thư Mục Web: `src/main/webapp/`

Thư mục chứa toàn bộ tài nguyên phía client bao gồm JSP views, CSS, JavaScript, hình ảnh, và cấu hình deployment.

```
src/main/webapp/
├── WEB-INF/
│   ├── web.xml                       # Cấu hình servlet 6.0
│   └── views/                        # 65+ file JSP
├── META-INF/
│   └── context.xml                   # Context path: /FinoraRetail
├── assets/
│   ├── css/                          # 5 file CSS
│   └── js/                           # 2 file JavaScript
└── index.jsp                         # Welcome file
```

---

### 4.1. Cấu Hình Deployment

#### 4.1.1. `WEB-INF/web.xml`

- **Servlet API:** 6.0 (Jakarta EE 10)
- **Welcome file:** `index.jsp`
- **Session timeout:** 30 phút
- **Character encoding:** UTF-8
- **MIME type mappings:** Xử lý các loại tài nguyên tĩnh

#### 4.1.2. `META-INF/context.xml`

- **Context path:** `/FinoraRetail`
- **Database resource:** JDBC DataSource (nếu sử dụng connection pooling)
- **Session configuration:** Cookie settings cho production

---

### 4.2. Package Views: `WEB-INF/views/`

Tất cả JSP được đặt trong `WEB-INF/views` để ngăn truy cập trực tiếp từ trình duyệt, đảm bảo mọi request phải qua controller.

```
WEB-INF/views/
├── auth/                              # Trang xác thực
│   ├── login.jsp
│   ├── register.jsp
│   └── forgot-password.jsp
├── common/                            # Component dùng chung
│   ├── header.jsp
│   ├── sidebar.jsp
│   └── footer.jsp
├── dashboard/                        # Trang tổng quan
│   ├── owner.jsp
│   ├── inventory.jsp
│   └── financial.jsp
├── categories/                       # Quản lý danh mục (MỚI)
│   └── list.jsp
├── customers/
├── products/
├── suppliers/
├── stores/
├── orders/
├── inventory/
├── payments/
├── invoices/
├── expenses/
├── income/
├── purchase-orders/
├── reports/
├── users/
├── roles/
├── profile/
├── activity-log/
├── notifications/
├── configuration/
├── seo/
└── about/
```

#### 4.2.1. Trang Auth (`auth/`)

| File | Chức năng |
|------|-----------|
| `login.jsp` | Form đăng nhập với email và mật khẩu |
| `register.jsp` | Form đăng ký tài khoản mới |
| `forgot-password.jsp` | Form khôi phục mật khẩu |

#### 4.2.2. Component Common (`common/`)

| File | Chức năng |
|------|-----------|
| `header.jsp` | Thanh header dùng chung, bao gồm navigation và user menu |
| `sidebar.jsp` | Thanh sidebar điều hướng theo role |
| `footer.jsp` | Phần footer chung cho các trang quản trị |

#### 4.2.3. Trang Dashboard (`dashboard/`)

| File | Chức năng |
|------|-----------|
| `owner.jsp` | Dashboard tổng quan cho chủ cửa hàng |
| `inventory.jsp` | Dashboard thống kê tồn kho |
| `financial.jsp` | Dashboard thống kê tài chính |

#### 4.2.4. Trang Categories (`categories/`)

| File | Chức năng |
|------|-----------|
| `list.jsp` | Trang danh sách danh mục với phân trang, tìm kiếm, thêm/sửa/xóa |

---

### 4.3. Tài Nguyên Tĩnh: `assets/`

#### 4.3.1. CSS (`assets/css/`)

| File | Mô tả |
|------|--------|
| `theme.css` | Biến CSS custom properties, dark/light mode, theme variables |
| `categories.css` | Styles riêng cho trang quản lý danh mục |
| `style.css` | Styles chung cho toàn bộ ứng dụng |
| `login-custom.css` | Styles bổ sung cho trang đăng nhập |
| `forgot-password.css` | Styles cho trang quên mật khẩu |
| `register.css` | Styles cho trang đăng ký |

#### 4.3.2. JavaScript (`assets/js/`)

| File | Mô tả |
|------|--------|
| `register.js` | Xử lý validation và submit form đăng ký phía client |
| `app.js` | Mã JavaScript chung cho ứng dụng |

---

### 4.4. External Libraries (CDN)

Dự án sử dụng các thư viện CSS và font từ CDN:

| Thư viện | Mục đích |
|----------|----------|
| Bootstrap 5 | CSS framework, responsive grid system, component library |
| Material Icons | Bộ biểu tượng theo material design |
| Google Fonts (Inter, Manrope) | Font chữ chính cho giao diện |

---

## 5. Thư Mục Build: `target/`

```
target/
├── StoreManagementNetBeans.war       # WAR file đã đóng gói
├── classes/                           # Các class Java đã compile
├── generated-sources/
├── maven-archiver/
└── maven-status/
```

Thư mục `target/` chứa toàn bộ artifact được Maven tạo ra trong quá trình build. **Không chỉnh sửa bất kỳ tệp nào trong thư mục này** — mọi thay đổi cần thực hiện ở mã nguồn gốc và rebuild bằng Maven.

---

## 6. Cấu Trúc Database Schema

Database `DBFinoraV3` trên SQL Server chứa 21 bảng theo kiến trúc:

| STT | Bảng | Mô tả |
|-----|------|--------|
| 1 | Users | Tài khoản người dùng hệ thống |
| 2 | Roles | Vai trò (Admin, Manager, Staff) |
| 3 | UserRoles | Liên kết user-role (nhiều-nhiều) |
| 4 | Stores | Cửa hàng/chi nhánh |
| 5 | Customers | Khách hàng |
| 6 | Suppliers | Nhà cung cấp |
| 7 | Categories | Danh mục sản phẩm (phân cấp) |
| 8 | Products | Sản phẩm |
| 9 | Orders | Đơn hàng bán |
| 10 | OrderDetails | Chi tiết đơn hàng |
| 11 | Invoices | Hóa đơn |
| 12 | Payments | Thanh toán |
| 13 | Income | Thu nhập |
| 14 | Expenses | Chi phí |
| 15 | PurchaseOrders | Đơn đặt hàng nhập |
| 16 | PurchaseDetails | Chi tiết đơn nhập |
| 17 | ActivityLogs | Nhật ký hoạt động |
| 18 | Notifications | Thông báo |
| 19 | BusinessConfigurations | Cấu hình hệ thống |
| 20 | StockTransactions | Giao dịch tồn kho |
| 21 | InventoryItems | Tồn kho theo cửa hàng |

Schema được quản lý tập trung trong `database/DBFinoraV3.sql` và thuộc **Protected Area** — không được sửa đổi trực tiếp mà phải thông qua review.

---

## 7. Sơ Đồ Quan Hệ Package

```
┌─────────────────────────────────────────────────────┐
│                    Web Layer                        │
│                  (JSP Views)                        │
│         assets/css/ · assets/js/ · views/           │
└──────────────────────┬──────────────────────────────┘
                       │ Forward/Redirect
┌──────────────────────▼──────────────────────────────┐
│               Controller Layer                      │
│            com.storemanagement.controller/          │
│   (AuthController, CategoryServlet, BaseController)│
└──────┬───────────────┬─────────────────┬───────────┘
       │               │                 │
       ▼               ▼                 ▼
┌──────────────┐ ┌──────────────┐ ┌────────────────┐
│   Service    │ │     DAO      │ │     Model      │
│     Layer    │ │    Layer     │ │    Entities    │
│ (19 services)│ │ (17 DAOs)    │ │  (19 models)   │
└──────┬───────┘ └──────┬───────┘ └────────────────┘
       │                │
       │    ┌───────────┘
       │    │
       ▼    ▼
┌──────────────────┐
│   Util Layer     │
│  (DBContext,     │
│   AuthUtil,      │
│   MoneyUtil)     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  SQL Server      │
│  DBFinoraV3      │
│  (21 tables)     │
└──────────────────┘
```

---

## 8. Bảng Tóm Tắt Số Lượng Thành Phần

| Thành phần | Số lượng | Trạng thái |
|------------|----------|------------|
| Servlet Controllers | 17 | 1 hoàn chỉnh (CategoryServlet), 16 skeleton |
| DAO Classes | 17 | 1 hoàn chỉnh (CategoryDAO), 16 skeleton |
| Domain Models | 19 | Hoàn chỉnh (bao gồm BaseModel, Category) |
| Service Classes | 19 | Tất cả skeleton |
| JSP Views | 65+ | Đang phát triển |
| Database Tables | 21 | Hoàn chỉnh trong DBFinoraV3.sql |
| Protected Route Patterns | 21 | Được AuthFilter bảo vệ |
| Utility Classes | 10 | 3 hoàn chỉnh, 7 skeleton |

---

## 9. Quy Ước Đặt Tên

| Loại | Quy ước | Ví dụ |
|------|---------|-------|
| Java Class | PascalCase | `CategoryServlet`, `BaseDAO` |
| Java Method | camelCase | `findById`, `getConnection` |
| Java Variable | camelCase | `categoryId`, `currentUser` |
| Package | lowercase | `controller`, `dao`, `model` |
| JSP File | kebab-case | `list.jsp`, `forgot-password.jsp` |
| CSS Class | kebab-case | `.category-card`, `.sidebar-menu` |
| Database Table | PascalCase | `Categories`, `OrderDetails` |
| Database Column | PascalCase | `CategoryId`, `CreatedAt` |

---

## 10. Quy Tắc Quan Trọng

1. **Không đặt SQL trong Controller hoặc JSP** — Tất cả câu lệnh SQL phải nằm trong DAO
2. **Không mở kết nối DB trong JSP** — JSP chỉ nhận dữ liệu từ request attribute
3. **Không khởi tạo DAO trong JSP** — Sử dụng servlet để chuẩn bị dữ liệu
4. **Sử dụng JSTL thay vì Scriptlet** — Dùng `<c:forEach>`, `<c:if>` thay vì `<% %>`
5. **Dùng PreparedStatement** — Tất cả truy vấn có tham số phải dùng `?` placeholder
6. **Giải phóng tài nguyên** — Sử dụng try-with-resources cho Connection, PreparedStatement, ResultSet
7. **UTF-8 không BOM** — Mọi tệp text phải được lưu dưới mã hóa UTF-8 không có BOM
8. **BaseModel làm cơ sở** — Tất cả entity nên kế thừa BaseModel để đảm bảo tính nhất quán

---

*Lưu ý: Thư mục `category/` ở cấp root là module ngoài với package khác (`Category.java`, `CategoryDAO.java`, `ProductServlet.java`, `categories.jsp`) — cần được tích hợp vào cấu trúc chính hoặc xóa bỏ để tránh trùng lặp với `CategoryServlet.java` và `CategoryDAO.java` trong package chuẩn `com.storemanagement`.*

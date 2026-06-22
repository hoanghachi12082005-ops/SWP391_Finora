# Tính Năng Đã Triển Khai

**Dự án:** FinoraRetail (SWP391_Finora)  
**Phiên bản:** 1.0  
**Ngày cập nhật:** 21/06/2026

---

## Mục Lục

1. [Foundation (Nền tảng)](#1-foundation-nền-tảng)
2. [Database (Cơ sở dữ liệu)](#2-database-cơ-sở-dữ-liệu)
3. [Models (Mô hình dữ liệu)](#3-models-mô-hình-dữ-liệu)
4. [Authentication (Xác thực)](#4-authentication-xác-thực)
5. [UI/UX (Giao diện)](#5-uiux-giao-diện)
6. [Category Module (Module Danh mục)](#6-category-module-module-danh-mục)
7. [Các Module Khác Đang Phát Triển](#7-các-module-khác-đang-phát-triển)

---

## 1. Foundation (Nền tảng)

### 1.1. Cấu Trúc Dự Án Maven

Dự án sử dụng Apache Maven để quản lý dependency và build:

```xml
<!-- pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.finora</groupId>
    <artifactId>FinoraRetail</artifactId>
    <version>1.0.0</version>
    <packaging>war</packaging>
    
    <!-- Jakarta EE 10 -->
    <properties>
        <jakartaee.version>10.0.0</jakartaee.version>
        <jakarta.servlet.version>6.0.0</jakarta.servlet.version>
        <jakarta.jsp.version>3.0.0</jakarta.jsp.version>
        <jstl.version>3.0.0</jstl.version>
        <mssql.version>12.4.0.jre11</mssql.version>
    </properties>
    
    <dependencies>
        <!-- Jakarta Servlet API -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>${jakarta.servlet.version}</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Jakarta JSP API -->
        <dependency>
            <groupId>jakarta.servlet.jsp</groupId>
            <artifactId>jakarta.servlet.jsp-api</artifactId>
            <version>${jakarta.jsp.version}</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSTL for JSP -->
        <dependency>
            <groupId>jakarta.servlet.jsp.jstl</groupId>
            <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
            <version>${jstl.version}</version>
        </dependency>
        
        <!-- SQL Server JDBC Driver -->
        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>mssql-jdbc</artifactId>
            <version>${mssql.version}</version>
        </dependency>
    </dependencies>
</project>
```

**Tính năng:**
- **Jakarta EE 10**: Sử dụng các API mới nhất của Jakarta EE
- **Servlet 6.0**: Hỗ trợ Servlet API mới nhất
- **JSP 3.0**: Hỗ trợ JSP với Expression Language 4.0
- **JSTL 3.0**: Tag library cho JSP
- **MSSQL JDBC 12.4**: Driver kết nối SQL Server

---

### 1.2. Cấu Hình Apache Tomcat 10.1

Cấu hình context và resource cho Tomcat:

```xml
<!-- META-INF/context.xml -->
<Context>
    <Resource name="jdbc/DBFinoraV2"
              auth="Container"
              type="javax.sql.DataSource"
              driverClassName="com.microsoft.sqlserver.jdbc.SQLServerDriver"
              url="jdbc:sqlserver://localhost:1433;databaseName=DBFinoraV2;encrypt=true;trustServerCertificate=true"
              username="sa"
              password="your_password"
              maxTotal="20"
              maxIdle="10"
              maxWaitMillis="10000"/>
</Context>
```

---

### 1.3. Kết Nối Database Qua DBContext

Lớp tiện ích để quản lý kết nối database:

```java
// util/DBContext.java
public class DBContext {
    
    private static final String DRIVER = 
        "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL = 
        "jdbc:sqlserver://localhost:1433;databaseName=DBFinoraV2;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "your_password";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server Driver not found", e);
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

---

### 1.4. AuthFilter Bảo Vệ Route

Filter bảo vệ các route yêu cầu đăng nhập:

```java
// filter/AuthFilter.java
@WebFilter(urlPatterns = {
    "/CategoryServlet/*",
    "/ProductServlet/*",
    "/OrderServlet/*",
    "/CustomerServlet/*",
    "/DashboardServlet/*",
    "/EmployeeServlet/*",
    "/SupplierServlet/*",
    "/WarehouseServlet/*",
    "/InventoryServlet/*",
    "/PaymentServlet/*",
    "/ReportServlet/*",
    "/VoucherServlet/*",
    "/TransferServlet/*",
    "/BranchServlet/*",
    "/RoleServlet/*",
    "/PointServlet/*",
    "/UnitServlet/*",
    "/SettingsServlet/*",
    "/ProfileServlet/*",
    "/PasswordServlet/*",
    "/ExportServlet/*",
    "/ImportServlet/*",
    "/admin/*"
})
public class AuthFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, 
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        
        // Cho phép các trang công khai
        String uri = request.getRequestURI();
        if (uri.endsWith("/login") || 
            uri.endsWith(".css") || 
            uri.endsWith(".js") ||
            uri.endsWith(".png") ||
            uri.endsWith(".jpg")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Kiểm tra đăng nhập
        User user = (session != null) ? 
            (User) session.getAttribute("currentUser") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            chain.doFilter(request, response);
        }
    }
}
```

**Các route được bảo vệ (21 route patterns):**
- CategoryServlet
- ProductServlet
- OrderServlet
- CustomerServlet
- DashboardServlet
- EmployeeServlet
- SupplierServlet
- WarehouseServlet
- InventoryServlet
- PaymentServlet
- ReportServlet
- VoucherServlet
- TransferServlet
- BranchServlet
- RoleServlet
- PointServlet
- UnitServlet
- SettingsServlet
- ProfileServlet
- PasswordServlet
- ExportServlet
- ImportServlet
- /admin/*

---

### 1.5. BaseController Với Helper Methods

Lớp cơ sở cho các Servlet với các phương thức tiện ích:

```java
// controller/BaseController.java
public abstract class BaseController extends HttpServlet {
    
    protected void forward(String path, HttpServletRequest request, 
                          HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("/views" + path);
        rd.forward(request, response);
    }
    
    protected void redirect(String url, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + url);
    }
    
    protected void setFlashMessage(HttpSession session, String type, 
                                   String message) {
        session.setAttribute(type, message);
    }
    
    protected void setFlashError(HttpSession session, String message) {
        session.setAttribute("error", message);
    }
    
    protected void setFlashSuccess(HttpSession session, String message) {
        session.setAttribute("message", message);
    }
    
    protected void setFlashWarning(HttpSession session, String message) {
        session.setAttribute("warning", message);
    }
    
    protected void setFlashInfo(HttpSession session, String message) {
        session.setAttribute("info", message);
    }
}
```

---

### 1.6. BaseModel Abstract Class

Lớp cơ sở cho các Model với các trường chung:

```java
// model/BaseModel.java
public abstract class BaseModel {
    
    protected Integer createdAt;
    protected Integer updatedAt;
    protected String status;
    
    public Integer getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }
    
    public Integer getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Integer updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
```

---

### 1.7. ICrudDAO Generic Interface

Interface chuẩn cho các DAO CRUD operations:

```java
// dao/ICrudDAO.java
public interface ICrudDAO<T, ID> {
    
    /**
     * Insert a new record
     * @param entity The entity to insert
     * @return The generated ID, or 0 if failed
     */
    int insert(T entity);
    
    /**
     * Update an existing record
     * @param entity The entity with updated values
     * @return true if successful
     */
    boolean update(T entity);
    
    /**
     * Delete a record by ID (soft delete)
     * @param id The ID of the record to delete
     * @return true if successful
     */
    boolean delete(ID id);
    
    /**
     * Find a record by ID
     * @param id The ID to search
     * @return The entity, or null if not found
     */
    T findById(ID id);
    
    /**
     * Get all records
     * @return List of all entities
     */
    List<T> findAll();
}
```

---

### 1.8. BaseDAO Abstract Class

Lớp cơ sở cho các DAO với connection helper:

```java
// dao/BaseDAO.java
public abstract class BaseDAO {
    
    protected Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }
    
    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected void closeResources(Connection conn, PreparedStatement ps, 
                                   ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

## 2. Database (Cơ sở dữ liệu)

### 2.1. Cấu Trúc Schema 21 Bảng

Cơ sở dữ liệu DBFinoraV2 bao gồm 21 bảng được tổ chức theo 5 nhóm chức năng:

#### Nhóm Identity & Access (4 bảng)
- **role**: Vai trò người dùng (OWNER, ADMIN, CASHIER,...)
- **branch**: Chi nhánh/cửa hàng
- **employee**: Nhân viên
- **employee_role**: Liên kết nhân viên - vai trò (nhiều-nhiều)

#### Nhóm Business Partners (4 bảng)
- **customer**: Khách hàng
- **customer_point**: Điểm thưởng khách hàng
- **voucher**: Mã giảm giá
- **supplier**: Nhà cung cấp

#### Nhóm Commerce (4 bảng)
- **[order]**: Đơn hàng (6 khóa ngoại)
- **order_detail**: Chi tiết đơn hàng
- **payment**: Thanh toán
- **point_transaction**: Giao dịch điểm thưởng

#### Nhóm Warehouse & Stock (8 bảng)
- **warehouse**: Kho hàng
- **unit**: Đơn vị tính
- **category**: Danh mục sản phẩm (tự tham chiếu)
- **product**: Sản phẩm
- **inventory**: Tồn kho
- **stock_transfer**: Chuyển kho
- **stock_transfer_detail**: Chi tiết chuyển kho
- **stock_transaction**: Giao dịch tồn kho

#### Nhóm System (1 bảng)
- **audit_log**: Nhật ký kiểm toán

---

### 2.2. Hỗ Trợ Tiếng Việt (NVARCHAR)

Tất cả các cột văn bản sử dụng kiểu `NVARCHAR` để hỗ trợ đầy đủ ký tự tiếng Việt:

```sql
-- Ví dụ
category_name NVARCHAR(150) NOT NULL
description NVARCHAR(255)
address NVARCHAR(300)
```

**Đặc điểm:**
- `N` prefix đảm bảo lưu trữ Unicode
- Hỗ trợ đầy đủ dấu tiếng Việt (ă, â, đ, ê, ô, ơ, ư, ơ,...)
- Sử dụng collation Vietnamese_CI_AS mặc định

---

### 2.3. Cấu Trúc Phân Cấp Danh Mục (Self-Referential)

Bảng `category` có quan hệ tự tham chiếu cho phép xây dựng cây danh mục đa cấp:

```sql
CREATE TABLE category (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    category_name NVARCHAR(150) NOT NULL,
    description NVARCHAR(255),
    parent_category_id INT REFERENCES category(category_id),
    status NVARCHAR(20) DEFAULT 'active',
    created_at DATETIME DEFAULT GETDATE(),
    update_at DATETIME DEFAULT GETDATE()
);
```

**Ví dụ cấu trúc:**
```
Thực phẩm (ID: 1, parent: NULL)
├── Đồ uống (ID: 2, parent: 1)
│   ├── Nước giải khát (ID: 5, parent: 2)
│   └── Trà, cà phê (ID: 6, parent: 2)
└── Đồ ăn vặt (ID: 3, parent: 1)
    ├── Bánh (ID: 7, parent: 3)
    └── Kẹo (ID: 8, parent: 3)
```

---

### 2.4. Bảng Audit Log Cho Theo Dõi Thay Đổi

Bảng `audit_log` ghi nhận tất cả thay đổi trong hệ thống:

```sql
CREATE TABLE audit_log (
    audit_log_id INT IDENTITY(1,1) PRIMARY KEY,
    emp_id INT REFERENCES employee(emp_id),
    action_name NVARCHAR(100),
    table_name NVARCHAR(100),
    record_id INT,
    old_data NVARCHAR(MAX),
    new_data NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE()
);
```

**Thông tin được ghi nhận:**
- Nhân viên thực hiện (emp_id)
- Tên hành động (INSERT, UPDATE, DELETE)
- Bảng bị tác động (table_name)
- ID bản ghi (record_id)
- Dữ liệu trước/sau thay đổi (old_data, new_data)
- Thời điểm thực hiện (created_at)

---

## 3. Models (Mô hình dữ liệu)

Hệ thống có 19 domain models được thiết kế theo nguyên tắc POJO (Plain Old Java Object):

### 3.1. Danh Sách Models

| # | Tên Model | Mô tả | Thuộc nhóm |
|---|-----------|-------|-----------|
| 1 | User | Người dùng đăng nhập | Auth |
| 2 | Role | Vai trò | Identity |
| 3 | Branch | Chi nhánh | Identity |
| 4 | Employee | Nhân viên | Identity |
| 5 | Customer | Khách hàng | Partner |
| 6 | Supplier | Nhà cung cấp | Partner |
| 7 | Category | Danh mục sản phẩm | Product |
| 8 | Product | Sản phẩm | Product |
| 9 | Unit | Đơn vị tính | Product |
| 10 | Order | Đơn hàng | Commerce |
| 11 | OrderDetail | Chi tiết đơn hàng | Commerce |
| 12 | Payment | Thanh toán | Commerce |
| 13 | Voucher | Voucher | Commerce |
| 14 | Inventory | Tồn kho | Stock |
| 15 | StockTransfer | Chuyển kho | Stock |
| 16 | StockTransferDetail | Chi tiết chuyển kho | Stock |
| 17 | StockTransaction | Giao dịch tồn kho | Stock |
| 18 | CustomerPoint | Điểm thưởng | Loyalty |
| 19 | PointTransaction | Giao dịch điểm | Loyalty |

### 3.2. Ví Dụ Model Category

```java
// model/Category.java
public class Category {
    
    // Primary Key
    private int categoryId;
    
    // Basic Fields
    private String categoryName;
    private String description;
    
    // Self-referential FK (for hierarchical structure)
    private Integer parentCategoryId;
    private String parentName;  // UI convenience field
    
    // Status
    private String status;
    
    // Metadata
    private Timestamp createdAt;
    private Timestamp updateAt;
    
    // UI convenience
    private int productCount;  // Number of products in this category
    
    // Constructors
    public Category() {}
    
    public Category(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
        this.status = "active";
    }
    
    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getParentCategoryId() {
        return parentCategoryId;
    }
    
    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
    
    public String getParentName() {
        return parentName;
    }
    
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdateAt() {
        return updateAt;
    }
    
    public void setUpdateAt(Timestamp updateAt) {
        this.updateAt = updateAt;
    }
    
    public int getProductCount() {
        return productCount;
    }
    
    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
    
    // Helper methods
    public boolean isActive() {
        return "active".equals(this.status);
    }
    
    public boolean isRootCategory() {
        return this.parentCategoryId == null;
    }
}
```

---

## 4. Authentication (Xác thực)

### 4.1. Đăng Nhập/Đăng Xuất Qua AuthController

```java
// controller/AuthController.java
@WebServlet("/login")
public class AuthController extends HttpServlet {
    
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Nếu đã đăng nhập, chuyển về dashboard
        if (session != null && session.getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            return;
        }
        
        // Hiển thị trang login
        request.getRequestDispatcher("/views/auth/login.jsp")
               .forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
                         HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Validate
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            
            request.setAttribute("error", "Email và mật khẩu không được để trống");
            request.getRequestDispatcher("/views/auth/login.jsp")
                   .forward(request, response);
            return;
        }
        
        // Authenticate
        User user = userDAO.findByEmail(email);
        
        if (user != null && userDAO.verifyPassword(password, user.getPasswordHash())) {
            
            // Đăng nhập thành công
            HttpSession session = request.getSession();
            session.setAttribute("currentUser", user);
            
            // Redirect về trang yêu cầu trước đó (nếu có)
            String redirect = (String) session.getAttribute("redirectAfterLogin");
            if (redirect != null) {
                session.removeAttribute("redirectAfterLogin");
                response.sendRedirect(redirect);
            } else {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            }
            
        } else {
            // Đăng nhập thất bại
            request.setAttribute("error", "Email hoặc mật khẩu không đúng");
            request.getRequestDispatcher("/views/auth/login.jsp")
                   .forward(request, response);
        }
    }
}

// Logout
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
```

---

### 4.2. Session-Based Authentication

```java
// Lưu user vào session sau khi đăng nhập
HttpSession session = request.getSession();
session.setAttribute("currentUser", user);

// Kiểm tra user trong session (AuthFilter)
User user = (User) session.getAttribute("currentUser");

// Lấy thông tin user từ session
public class UserSessionUtil {
    
    public static User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
    
    public static int getCurrentUserId(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getUserId() : 0;
    }
    
    public static String getCurrentUserRole(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getRole() : null;
    }
    
    public static boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(getCurrentUserRole(session));
    }
    
    public static boolean isOwner(HttpSession session) {
        return "OWNER".equals(getCurrentUserRole(session));
    }
}
```

---

### 4.3. Demo Mode

Chế độ demo cho phép đăng nhập với bất kỳ email nào:

```java
// AuthController - Demo mode
if (user == null) {
    // Demo mode: accept any login
    user = new User();
    user.setUserId(999);
    user.setFullName("Demo User");
    user.setEmail(email);
    user.setRole("OWNER");
    user.setBranchId(1);
    
    // Create session for demo user
    HttpSession session = request.getSession();
    session.setAttribute("currentUser", user);
    session.setAttribute("isDemo", true);
    
    request.getSession().setAttribute("message", 
        "Demo Mode: Bạn đang đăng nhập với quyền OWNER");
    response.sendRedirect(request.getContextPath() + "/DashboardServlet");
    return;
}
```

---

### 4.4. Flash Message Pattern

```java
// POST action - Set message
request.getSession().setAttribute("message", "Thao tác thành công!");
response.sendRedirect("CategoryServlet?action=list");

// JSP - Display and remove message
<c:if test="${not empty message}">
    <div class="alert alert-success">${message}</div>
    <c:remove var="message" scope="session"/>
</c:if>
```

---

## 5. UI/UX (Giao diện)

### 5.1. Bootstrap 5 Responsive Design

Giao diện sử dụng Bootstrap 5 với responsive grid system:

```jsp
<!-- Layout cơ bản -->
<div class="container-fluid">
    <div class="row">
        <!-- Sidebar -->
        <nav class="col-md-2 d-md-block bg-dark sidebar">
            <%@ include file="../common/sidebar.jsp" %>
        </nav>
        
        <!-- Main Content -->
        <main class="col-md-10 ms-sm-auto px-md-4">
            <!-- Header -->
            <%@ include file="../common/header.jsp" %>
            
            <!-- Content -->
            <div class="content-wrapper">
                <!-- Flash Messages -->
                <jsp:include page="../common/flash-messages.jsp"/>
                
                <!-- Page Content -->
                <sitemesh:write property='body'/>
            </div>
        </main>
    </div>
</div>
```

---

### 5.2. Material Icons Integration

```html
<!-- Material Icons CDN -->
<link href="https://fonts.googleapis.com/icon?family=Material+Icons" 
      rel="stylesheet">

<!-- Sử dụng icons -->
<span class="material-icons">dashboard</span>
<span class="material-icons">add</span>
<span class="material-icons">edit</span>
<span class="material-icons">delete</span>
<span class="material-icons">search</span>
<span class="material-icons">filter_list</span>
```

---

### 5.3. Google Fonts

```html
<!-- Google Fonts -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Manrope:wght@400;500;600;700&display=swap" rel="stylesheet">

<style>
    body {
        font-family: 'Inter', sans-serif;
    }
    
    h1, h2, h3, h4, h5, h6 {
        font-family: 'Manrope', sans-serif;
    }
</style>
```

---

### 5.4. Consistent Sidebar Navigation

```jsp
<!-- sidebar.jsp -->
<nav class="sidebar">
    <div class="sidebar-header">
        <h3>FinoraRetail</h3>
    </div>
    
    <ul class="nav flex-column">
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/DashboardServlet">
                <span class="material-icons">dashboard</span>
                Dashboard
            </a>
        </li>
        
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/CategoryServlet">
                <span class="material-icons">category</span>
                Danh mục
            </a>
        </li>
        
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/ProductServlet">
                <span class="material-icons">inventory_2</span>
                Sản phẩm
            </a>
        </li>
        
        <!-- ... 21 menu items total -->
    </ul>
</nav>
```

**21 Menu Items:**
1. Dashboard
2. Danh mục (Categories)
3. Sản phẩm (Products)
4. Đơn hàng (Orders)
5. Khách hàng (Customers)
6. Nhà cung cấp (Suppliers)
7. Kho hàng (Warehouses)
8. Tồn kho (Inventory)
9. Chuyển kho (Stock Transfer)
10. Thanh toán (Payments)
11. Voucher
12. Điểm thưởng (Points)
13. Nhân viên (Employees)
14. Vai trò (Roles)
15. Chi nhánh (Branches)
16. Báo cáo (Reports)
17. Nhật ký (Audit Log)
18. Cài đặt (Settings)
19. Hồ sơ (Profile)
20. Xuất/Nhập (Export/Import)
21. Trợ giúp (Help)

---

### 5.5. Modal Dialogs

```jsp
<!-- Add/Edit Modal -->
<div class="modal fade" id="categoryModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="modalTitle">Thêm Danh Mục Mới</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            
            <form action="${pageContext.request.contextPath}/CategoryServlet" method="POST">
                <input type="hidden" name="action" id="formAction" value="create">
                <input type="hidden" name="id" id="categoryId">
                
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="categoryName" class="form-label">Tên danh mục *</label>
                        <input type="text" class="form-control" id="categoryName" 
                               name="categoryName" required maxlength="150">
                    </div>
                    
                    <div class="mb-3">
                        <label for="description" class="form-label">Mô tả</label>
                        <textarea class="form-control" id="description" 
                                  name="description" rows="3" maxlength="255"></textarea>
                    </div>
                    
                    <div class="mb-3">
                        <label for="parentCategory" class="form-label">Danh mục cha</label>
                        <select class="form-select" id="parentCategory" name="parentCategoryId">
                            <option value="">-- Danh mục gốc --</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}">${cat.categoryName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <div class="mb-3">
                        <label for="status" class="form-label">Trạng thái</label>
                        <select class="form-select" id="status" name="status">
                            <option value="active">Hoạt động</option>
                            <option value="inactive">Không hoạt động</option>
                        </select>
                    </div>
                </div>
                
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary" id="submitBtn">Thêm mới</button>
                </div>
            </form>
        </div>
    </div>
</div>
```

---

### 5.6. Pagination, Search, và Filter

```jsp
<!-- Search and Filter Bar -->
<div class="row mb-3">
    <div class="col-md-6">
        <form action="${pageContext.request.contextPath}/CategoryServlet" method="GET">
            <input type="hidden" name="action" value="list">
            <div class="input-group">
                <input type="text" class="form-control" name="search" 
                       value="${search}" placeholder="Tìm kiếm danh mục...">
                <button class="btn btn-primary" type="submit">
                    <span class="material-icons">search</span>
                </button>
            </div>
        </form>
    </div>
    
    <div class="col-md-4">
        <form action="${pageContext.request.contextPath}/CategoryServlet" method="GET">
            <input type="hidden" name="action" value="list">
            <input type="hidden" name="search" value="${search}">
            <select class="form-select" name="status" onchange="this.form.submit()">
                <option value="">Tất cả trạng thái</option>
                <option value="active" ${status == 'active' ? 'selected' : ''}>Hoạt động</option>
                <option value="inactive" ${status == 'inactive' ? 'selected' : ''}>Không hoạt động</option>
            </select>
        </form>
    </div>
    
    <div class="col-md-2">
        <button class="btn btn-success w-100" data-bs-toggle="modal" 
                data-bs-target="#categoryModal">
            <span class="material-icons">add</span> Thêm mới
        </button>
    </div>
</div>
```

```jsp
<!-- Pagination -->
<c:if test="${totalPages > 1}">
    <nav aria-label="Page navigation">
        <ul class="pagination justify-content-center">
            <!-- Previous -->
            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                <a class="page-link" 
                   href="?action=list&page=${currentPage - 1}&search=${search}&status=${status}">
                    Previous
                </a>
            </li>
            
            <!-- Page Numbers -->
            <c:forEach begin="1" end="${totalPages}" var="i">
                <li class="page-item ${i == currentPage ? 'active' : ''}">
                    <a class="page-link" 
                       href="?action=list&page=${i}&search=${search}&status=${status}">
                        ${i}
                    </a>
                </li>
            </c:forEach>
            
            <!-- Next -->
            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                <a class="page-link" 
                   href="?action=list&page=${currentPage + 1}&search=${search}&status=${status}">
                    Next
                </a>
            </li>
        </ul>
    </nav>
</c:if>
```

---

### 5.7. Toast Notifications

```jsp
<!-- Toast Container -->
<div class="toast-container position-fixed top-0 end-0 p-3">
    <div id="toast" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-header">
            <span class="material-icons text-success me-2" id="toastIcon">check_circle</span>
            <strong class="me-auto" id="toastTitle">Thành công</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body" id="toastBody">
            <!-- Message here -->
        </div>
    </div>
</div>

<!-- JavaScript to show toast -->
<script>
function showToast(type, message) {
    const toast = document.getElementById('toast');
    const toastTitle = document.getElementById('toastTitle');
    const toastBody = document.getElementById('toastBody');
    const toastIcon = document.getElementById('toastIcon');
    
    toastBody.textContent = message;
    
    if (type === 'success') {
        toastTitle.textContent = 'Thành công';
        toastIcon.textContent = 'check_circle';
        toastIcon.className = 'material-icons text-success me-2';
    } else if (type === 'error') {
        toastTitle.textContent = 'Lỗi';
        toastIcon.textContent = 'error';
        toastIcon.className = 'material-icons text-danger me-2';
    }
    
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
}
</script>
```

---

## 6. Category Module (Module Danh Mục)

Module danh mục là module đầu tiên được triển khai đầy đủ với đầy đủ chức năng CRUD.

### 6.1. CategoryServlet - Servlet Đầy Đủ

```java
// controller/product/CategoryServlet.java
@WebServlet("/CategoryServlet")
public class CategoryServlet extends BaseController {
    
    private CategoryDAO categoryDAO = new CategoryDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, 
                        HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) action = "list";
        
        switch (action) {
            case "list":
                doList(request, response);
                break;
            case "new":
                doNew(request, response);
                break;
            case "edit":
                doEdit(request, response);
                break;
            case "delete":
                doDelete(request, response);
                break;
            default:
                doList(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
                         HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        switch (action) {
            case "create":
                doCreate(request, response);
                break;
            case "update":
                doUpdate(request, response);
                break;
            default:
                response.sendRedirect("CategoryServlet?action=list");
        }
    }
    
    private void doList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Extract filter parameters
        String search = request.getParameter("search");
        String status = request.getParameter("status");
        String pageParam = request.getParameter("page");
        int page = (pageParam != null) ? Integer.parseInt(pageParam) : 1;
        
        // Build filter
        CategoryFilter filter = new CategoryFilter();
        filter.setSearchTerm(search);
        filter.setStatus(status);
        filter.setPage(page);
        filter.setPageSize(10);
        
        // Fetch data
        List<Category> categories = categoryDAO.search(filter);
        int totalRecords = categoryDAO.count(filter);
        int totalPages = (int) Math.ceil((double) totalRecords / filter.getPageSize());
        
        // Set attributes
        request.setAttribute("categories", categories);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("search", search);
        request.setAttribute("status", status);
        
        // Statistics for cards
        request.setAttribute("totalCategories", categoryDAO.countAll());
        request.setAttribute("activeCategories", categoryDAO.countActive());
        
        // Forward
        forward("/categories/list.jsp", request, response);
    }
    
    private void doNew(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get all categories for parent selection
        List<Category> allCategories = categoryDAO.findAllActive();
        request.setAttribute("allCategories", allCategories);
        
        forward("/categories/form.jsp", request, response);
    }
    
    private void doEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        Category category = categoryDAO.findById(id);
        
        if (category == null) {
            setFlashError(request.getSession(), "Danh mục không tồn tại!");
            redirect("CategoryServlet?action=list", response);
            return;
        }
        
        request.setAttribute("category", category);
        
        // Get all categories for parent selection
        List<Category> allCategories = categoryDAO.findAllActive();
        request.setAttribute("allCategories", allCategories);
        
        forward("/categories/form.jsp", request, response);
    }
    
    private void doCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Validate
        List<String> errors = validateCategory(request);
        if (!errors.isEmpty()) {
            request.getSession().setAttribute("errors", errors);
            response.sendRedirect("CategoryServlet?action=new");
            return;
        }
        
        // Build category
        Category category = new Category();
        category.setCategoryName(request.getParameter("categoryName").trim());
        category.setDescription(request.getParameter("description"));
        
        String parentId = request.getParameter("parentCategoryId");
        if (parentId != null && !parentId.isEmpty()) {
            category.setParentCategoryId(Integer.parseInt(parentId));
        }
        
        category.setStatus(request.getParameter("status"));
        
        // Insert
        int id = categoryDAO.insert(category);
        
        if (id > 0) {
            setFlashSuccess(request.getSession(), 
                "Thêm danh mục '" + category.getCategoryName() + "' thành công!");
        } else {
            setFlashError(request.getSession(), 
                "Thêm danh mục thất bại. Vui lòng thử lại!");
        }
        
        redirect("CategoryServlet?action=list", response);
    }
    
    private void doUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        
        // Validate
        List<String> errors = validateCategory(request);
        if (!errors.isEmpty()) {
            request.getSession().setAttribute("errors", errors);
            redirect("CategoryServlet?action=edit&id=" + id, response);
            return;
        }
        
        // Build category
        Category category = new Category();
        category.setCategoryId(id);
        category.setCategoryName(request.getParameter("categoryName").trim());
        category.setDescription(request.getParameter("description"));
        
        String parentId = request.getParameter("parentCategoryId");
        if (parentId != null && !parentId.isEmpty()) {
            category.setParentCategoryId(Integer.parseInt(parentId));
        }
        
        category.setStatus(request.getParameter("status"));
        
        // Update
        boolean success = categoryDAO.update(category);
        
        if (success) {
            setFlashSuccess(request.getSession(), 
                "Cập nhật danh mục '" + category.getCategoryName() + "' thành công!");
        } else {
            setFlashError(request.getSession(), 
                "Cập nhật danh mục thất bại. Vui lòng thử lại!");
        }
        
        redirect("CategoryServlet?action=list", response);
    }
    
    private void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        Category category = categoryDAO.findById(id);
        
        if (category == null) {
            setFlashError(request.getSession(), "Danh mục không tồn tại!");
            redirect("CategoryServlet?action=list", response);
            return;
        }
        
        // Soft delete - update status
        boolean success = categoryDAO.delete(id);
        
        if (success) {
            setFlashSuccess(request.getSession(), 
                "Xóa danh mục '" + category.getCategoryName() + "' thành công!");
        } else {
            setFlashError(request.getSession(), 
                "Xóa danh mục thất bại. Vui lòng thử lại!");
        }
        
        redirect("CategoryServlet?action=list", response);
    }
    
    private List<String> validateCategory(HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        
        String name = request.getParameter("categoryName");
        if (name == null || name.trim().isEmpty()) {
            errors.add("Tên danh mục không được để trống");
        } else if (name.trim().length() > 150) {
            errors.add("Tên danh mục không được vượt quá 150 ký tự");
        }
        
        String description = request.getParameter("description");
        if (description != null && description.length() > 255) {
            errors.add("Mô tả không được vượt quá 255 ký tự");
        }
        
        return errors;
    }
}
```

---

### 6.2. CategoryDAO - DAO Đầy Đủ

```java
// dao/product/CategoryDAO.java
public class CategoryDAO extends BaseDAO implements ICrudDAO<Category, Integer> {
    
    @Override
    public int insert(Category category) {
        String sql = """
            INSERT INTO category (category_name, description, parent_category_id, status)
            VALUES (?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, 
                     Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            
            if (category.getParentCategoryId() != null) {
                ps.setInt(3, category.getParentCategoryId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            ps.setString(4, category.getStatus());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public boolean update(Category category) {
        String sql = """
            UPDATE category 
            SET category_name = ?, description = ?, 
                parent_category_id = ?, status = ?, update_at = GETDATE()
            WHERE category_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            
            if (category.getParentCategoryId() != null) {
                ps.setInt(3, category.getParentCategoryId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            ps.setString(4, category.getStatus());
            ps.setInt(5, category.getCategoryId());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean delete(Integer id) {
        // Soft delete - set status to inactive
        String sql = "UPDATE category SET status = 'inactive', update_at = GETDATE() " +
                     "WHERE category_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Category findById(Integer id) {
        String sql = """
            SELECT c.*, p.category_name as parent_name
            FROM category c
            LEFT JOIN category p ON c.parent_category_id = p.category_id
            WHERE c.category_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
            return null;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Category> findAll() {
        String sql = """
            SELECT c.*, p.category_name as parent_name,
                   (SELECT COUNT(*) FROM product WHERE category_id = c.category_id) as product_count
            FROM category c
            LEFT JOIN category p ON c.parent_category_id = p.category_id
            ORDER BY c.category_name
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            return executeQuery(ps);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public List<Category> search(CategoryFilter filter) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT c.*, p.category_name as parent_name,
                   (SELECT COUNT(*) FROM product WHERE category_id = c.category_id) as product_count
            FROM category c
            LEFT JOIN category p ON c.parent_category_id = p.category_id
            WHERE 1=1
            """);
        
        List<Object> params = new ArrayList<>();
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            sql.append(" AND c.category_name LIKE ?");
            params.add("%" + filter.getSearchTerm() + "%");
        }
        
        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            sql.append(" AND c.status = ?");
            params.add(filter.getStatus());
        }
        
        sql.append(" ORDER BY ");
        sql.append(filter.getSortColumn() != null ? filter.getSortColumn() : "c.category_id");
        sql.append(" ");
        sql.append(filter.isSortAscending() ? "ASC" : "DESC");
        
        // Pagination
        int offset = (filter.getPage() - 1) * filter.getPageSize();
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(filter.getPageSize());
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            return executeQuery(ps);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public int count(CategoryFilter filter) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM category c WHERE 1=1");
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            sql.append(" AND c.category_name LIKE ?");
        }
        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            sql.append(" AND c.status = ?");
        }
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
                ps.setString(idx++, "%" + filter.getSearchTerm() + "%");
            }
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                ps.setString(idx, filter.getStatus());
            }
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // Count methods for statistics
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM category";
        return countBySql(sql);
    }
    
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM category WHERE status = 'active'";
        return countBySql(sql);
    }
    
    public List<Category> findAllActive() {
        String sql = "SELECT * FROM category WHERE status = 'active' ORDER BY category_name";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            return executeQuery(ps);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    // Hierarchical category tree using CTE
    public List<Category> getCategoryTree() {
        String sql = """
            WITH CategoryTree AS (
                SELECT category_id, category_name, parent_category_id, 
                       0 as level, CAST(category_name AS NVARCHAR(500)) as path
                FROM category
                WHERE parent_category_id IS NULL AND status = 'active'
                
                UNION ALL
                
                SELECT c.category_id, c.category_name, c.parent_category_id,
                       ct.level + 1, CAST(ct.path + ' > ' + c.category_name AS NVARCHAR(500))
                FROM category c
                INNER JOIN CategoryTree ct ON c.parent_category_id = ct.category_id
                WHERE c.status = 'active'
            )
            SELECT * FROM CategoryTree ORDER BY path
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            List<Category> tree = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Category cat = new Category();
                cat.setCategoryId(rs.getInt("category_id"));
                cat.setCategoryName(rs.getString("category_name"));
                cat.setParentCategoryId(rs.getInt("parent_category_id"));
                tree.add(cat);
            }
            
            return tree;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    // Check for cycle in category hierarchy
    public boolean wouldCreateCycle(int categoryId, Integer newParentId) {
        if (newParentId == null) return false;
        if (categoryId == newParentId) return true;
        
        String sql = """
            WITH CategoryAncestors AS (
                SELECT category_id, parent_category_id
                FROM category
                WHERE category_id = ?
                
                UNION ALL
                
                SELECT c.category_id, c.parent_category_id
                FROM category c
                INNER JOIN CategoryAncestors ca ON c.category_id = ca.parent_category_id
            )
            SELECT COUNT(*) FROM CategoryAncestors WHERE category_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, newParentId);
            ps.setInt(2, categoryId);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Fail safe
        }
    }
    
    // Private helper methods
    private List<Category> executeQuery(PreparedStatement ps) throws SQLException {
        List<Category> categories = new ArrayList<>();
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            categories.add(mapResultSetToCategory(rs));
        }
        
        return categories;
    }
    
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));
        
        int parentId = rs.getInt("parent_category_id");
        category.setParentCategoryId(rs.wasNull() ? null : parentId);
        
        category.setParentName(rs.getString("parent_name"));
        category.setStatus(rs.getString("status"));
        category.setCreatedAt(rs.getTimestamp("created_at"));
        category.setUpdateAt(rs.getTimestamp("update_at"));
        
        try {
            category.setProductCount(rs.getInt("product_count"));
        } catch (SQLException ignored) {}
        
        return category;
    }
    
    private int countBySql(String sql) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
```

---

### 6.3. JSP List View Hoàn Chỉnh

```jsp
<%-- list.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Danh mục - FinoraRetail</title>
    
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" 
          rel="stylesheet">
    <!-- Material Icons -->
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" 
          rel="stylesheet">
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/categories.css" 
          rel="stylesheet">
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <nav class="col-md-2 d-md-block bg-dark sidebar">
                <jsp:include page="../common/sidebar.jsp"/>
            </nav>
            
            <!-- Main Content -->
            <main class="col-md-10 ms-sm-auto px-md-4">
                <!-- Header -->
                <jsp:include page="../common/header.jsp"/>
                
                <!-- Page Title -->
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap 
                            align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">Quản lý Danh mục</h1>
                    <div class="btn-toolbar mb-2 mb-md-0">
                        <button type="button" class="btn btn-sm btn-success" 
                                data-bs-toggle="modal" data-bs-target="#categoryModal">
                            <span class="material-icons">add</span> Thêm mới
                        </button>
                    </div>
                </div>
                
                <!-- Flash Messages -->
                <jsp:include page="../common/flash-messages.jsp"/>
                
                <!-- Statistics Cards -->
                <div class="row mb-4">
                    <div class="col-md-4">
                        <div class="card bg-primary text-white">
                            <div class="card-body">
                                <h5 class="card-title">
                                    <span class="material-icons">category</span>
                                    Tổng số danh mục
                                </h5>
                                <h2 class="mb-0">${totalCategories}</h2>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card bg-success text-white">
                            <div class="card-body">
                                <h5 class="card-title">
                                    <span class="material-icons">check_circle</span>
                                    Đang hoạt động
                                </h5>
                                <h2 class="mb-0">${activeCategories}</h2>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card bg-secondary text-white">
                            <div class="card-body">
                                <h5 class="card-title">
                                    <span class="material-icons">paused</span>
                                    Không hoạt động
                                </h5>
                                <h2 class="mb-0">${totalCategories - activeCategories}</h2>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Search and Filter -->
                <div class="row mb-3">
                    <div class="col-md-6">
                        <form action="${pageContext.request.contextPath}/CategoryServlet" 
                              method="GET">
                            <input type="hidden" name="action" value="list">
                            <div class="input-group">
                                <input type="text" class="form-control" name="search" 
                                       value="${search}" 
                                       placeholder="Tìm kiếm danh mục...">
                                <button class="btn btn-primary" type="submit">
                                    <span class="material-icons">search</span>
                                </button>
                                <c:if test="${not empty search}">
                                    <a href="${pageContext.request.contextPath}/CategoryServlet?action=list" 
                                       class="btn btn-secondary">
                                        <span class="material-icons">clear</span>
                                    </a>
                                </c:if>
                            </div>
                        </form>
                    </div>
                    
                    <div class="col-md-4">
                        <form action="${pageContext.request.contextPath}/CategoryServlet" 
                              method="GET">
                            <input type="hidden" name="action" value="list">
                            <input type="hidden" name="search" value="${search}">
                            <select class="form-select" name="status" 
                                    onchange="this.form.submit()">
                                <option value="">Tất cả trạng thái</option>
                                <option value="active" ${status == 'active' ? 'selected' : ''}>
                                    Hoạt động
                                </option>
                                <option value="inactive" ${status == 'inactive' ? 'selected' : ''}>
                                    Không hoạt động
                                </option>
                            </select>
                        </form>
                    </div>
                </div>
                
                <!-- Data Table -->
                <div class="table-responsive">
                    <table class="table table-striped table-hover">
                        <thead class="table-dark">
                            <tr>
                                <th>ID</th>
                                <th>Tên danh mục</th>
                                <th>Danh mục cha</th>
                                <th>Mô tả</th>
                                <th>Sản phẩm</th>
                                <th>Trạng thái</th>
                                <th>Ngày tạo</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cat" items="${categories}">
                                <tr>
                                    <td>${cat.categoryId}</td>
                                    <td>
                                        <strong>${cat.categoryName}</strong>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty cat.parentName}">
                                                <span class="badge bg-info">${cat.parentName}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">Danh mục gốc</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty cat.description}">
                                                ${cat.description.length() > 50 ? 
                                                  cat.description.substring(0, 50) : cat.description}...
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">Không có</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge bg-primary">${cat.productCount}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${cat.status == 'active'}">
                                                <span class="badge bg-success">Hoạt động</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">Không hoạt động</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${cat.createdAt}" 
                                                        pattern="dd/MM/yyyy"/>
                                    </td>
                                    <td>
                                        <div class="btn-group btn-group-sm">
                                            <a href="?action=edit&id=${cat.categoryId}" 
                                               class="btn btn-outline-primary"
                                               title="Sửa">
                                                <span class="material-icons">edit</span>
                                            </a>
                                            <a href="?action=delete&id=${cat.categoryId}" 
                                               class="btn btn-outline-danger"
                                               title="Xóa"
                                               onclick="return confirm('Bạn có chắc muốn xóa danh mục này?')">
                                                <span class="material-icons">delete</span>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            
                            <c:if test="${empty categories}">
                                <tr>
                                    <td colspan="8" class="text-center py-4">
                                        <div class="text-muted">
                                            <span class="material-icons" style="font-size: 48px;">
                                                folder_open
                                            </span>
                                            <p class="mt-2">Không tìm thấy danh mục nào!</p>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
                
                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <nav aria-label="Page navigation">
                        <ul class="pagination justify-content-center">
                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                <a class="page-link" 
                                   href="?action=list&page=${currentPage - 1}&search=${search}&status=${status}">
                                    Previous
                                </a>
                            </li>
                            
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <li class="page-item ${i == currentPage ? 'active' : ''}">
                                    <a class="page-link" 
                                       href="?action=list&page=${i}&search=${search}&status=${status}">
                                        ${i}
                                    </a>
                                </li>
                            </c:forEach>
                            
                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                <a class="page-link" 
                                   href="?action=list&page=${currentPage + 1}&search=${search}&status=${status}">
                                    Next
                                </a>
                            </li>
                        </ul>
                    </nav>
                </c:if>
                
                <!-- Showing records info -->
                <div class="text-center text-muted mb-3">
                    Hiển thị ${categories.size()} trong tổng số ${totalRecords} danh mục
                </div>
            </main>
        </div>
    </div>
    
    <!-- Add/Edit Modal -->
    <jsp:include page="modal-form.jsp"/>
    
    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js">
    </script>
    
    <!-- Custom JS -->
    <script src="${pageContext.request.contextPath}/assets/js/categories.js"></script>
</body>
</html>
```

---

### 6.4. Separate CSS File

```css
/* categories.css */

/* Page-specific styles */
.content-wrapper {
    padding: 20px;
}

/* Statistics Cards */
.card .material-icons {
    vertical-align: middle;
    margin-right: 8px;
}

/* Table Styles */
.table th {
    white-space: nowrap;
}

.table td {
    vertical-align: middle;
}

/* Badge Styles */
.badge.bg-info {
    background-color: #0dcaf0 !important;
}

/* Modal Form Styles */
#categoryModal .form-label {
    font-weight: 500;
}

#categoryModal .modal-body {
    padding: 20px;
}

/* Button Group */
.btn-group-sm .material-icons {
    font-size: 18px;
}

/* Empty State */
.empty-state {
    padding: 60px 20px;
}

.empty-state .material-icons {
    font-size: 64px;
    opacity: 0.5;
}

/* Responsive */
@media (max-width: 768px) {
    .table-responsive {
        font-size: 14px;
    }
    
    .btn-group-sm .btn {
        padding: 2px 6px;
    }
}
```

---

### 6.5. Features Đặc Biệt

#### 6.5.1. Hierarchical Category Tree (CTE Query)

```java
// Sử dụng Common Table Expression để truy vấn cây danh mục
public List<Category> getCategoryTree() {
    String sql = """
        WITH CategoryTree AS (
            -- Base case: Root categories (no parent)
            SELECT category_id, category_name, parent_category_id, 
                   0 as level, 
                   CAST(category_name AS NVARCHAR(500)) as path
            FROM category
            WHERE parent_category_id IS NULL AND status = 'active'
            
            UNION ALL
            
            -- Recursive case: Child categories
            SELECT c.category_id, c.category_name, c.parent_category_id,
                   ct.level + 1, 
                   CAST(ct.path + ' > ' + c.category_name AS NVARCHAR(500))
            FROM category c
            INNER JOIN CategoryTree ct ON c.parent_category_id = ct.category_id
            WHERE c.status = 'active'
        )
        SELECT * FROM CategoryTree ORDER BY path
        """;
    // Execute and return...
}
```

**Kết quả:**
```
Thực phẩm
  > Đồ uống
    > Nước giải khát
    > Trà, cà phê
  > Đồ ăn vặt
    > Bánh
    > Kẹo
Đồ gia dụng
```

---

#### 6.5.2. Cycle Prevention

```java
// Kiểm tra trước khi cập nhật parent_category_id
public boolean wouldCreateCycle(int categoryId, Integer newParentId) {
    if (newParentId == null) return false;
    if (categoryId == newParentId) return true;
    
    // Tìm tất cả ancestors của newParentId
    // Nếu categoryId nằm trong đó, thì sẽ tạo cycle
    String sql = """
        WITH CategoryAncestors AS (
            SELECT category_id, parent_category_id
            FROM category
            WHERE category_id = ?
            
            UNION ALL
            
            SELECT c.category_id, c.parent_category_id
            FROM category c
            INNER JOIN CategoryAncestors ca ON c.category_id = ca.parent_category_id
        )
        SELECT COUNT(*) FROM CategoryAncestors WHERE category_id = ?
        """;
    // Execute and return result...
}
```

**Ví dụ:**
- categoryId = 5 (Nước giải khát)
- newParentId = 8 (Kẹo - thuộc nhánh "Đồ ăn vặt")
- Nếu 5 có parent là 2 (Đồ uống), và 2 không phải là ancestor của 8 => OK
- Nếu 5 có parent là 7 (Bánh), và 7 là ancestor của 8 => CYCLE!

---

#### 6.5.3. Product Count Per Category

```sql
-- Subquery trong SELECT để đếm số sản phẩm
SELECT c.*,
       (SELECT COUNT(*) FROM product WHERE category_id = c.category_id) as product_count
FROM category c
```

---

#### 6.5.4. Active/Inactive Status Management

```java
// Soft delete - chỉ cập nhật status
public boolean delete(Integer id) {
    String sql = "UPDATE category SET status = 'inactive', update_at = GETDATE() " +
                 "WHERE category_id = ?";
    // Execute...
}

// Toggle status
public boolean toggleStatus(Integer id) {
    String sql = """
        UPDATE category 
        SET status = CASE WHEN status = 'active' THEN 'inactive' ELSE 'active' END,
            update_at = GETDATE()
        WHERE category_id = ?
        """;
    // Execute...
}
```

---

## 7. Các Module Khác Đang Phát Triển

### 7.1. Module Đã Có Cấu Trúc Cơ Bản

| Module | Trạng thái | Mô tả |
|--------|------------|-------|
| **Category** | Hoàn thành | CRUD đầy đủ với hierarchical support |
| **Auth** | Hoàn thành | Login/Logout/Demo mode |
| **Dashboard** | Cơ bản | Trang tổng quan |
| **Product** | Đang phát triển | Servlet và DAO cơ bản |
| **Order** | Đang phát triển | Servlet và DAO cơ bản |
| **Customer** | Đang phát triển | Servlet và DAO cơ bản |

### 7.2. Module Sắp Triển Khai

| Module | Ưu tiên | Ghi chú |
|--------|---------|---------|
| **Payment** | Cao | Thanh toán đơn hàng, tích hợp cổng thanh toán |
| **Inventory** | Cao | Quản lý tồn kho |
| **Stock Transfer** | Trung bình | Chuyển kho giữa các chi nhánh |
| **Voucher** | Trung bình | Quản lý mã giảm giá |
| **Point System** | Trung bình | Tích điểm, đổi điểm |
| **Employee** | Thấp | Quản lý nhân viên |
| **Report** | Thấp | Báo cáo doanh thu, tồn kho |

---

## Tổng Kết

Hệ thống FinoraRetail đã triển khai các thành phần nền tảng và module danh mục một cách hoàn chỉnh. Các đặc điểm nổi bật:

1. **Kiến trúc chuẩn**: MVC với Servlet, DAO, JSP, phân tách concerns rõ ràng
2. **Database 21 bảng**: Thiết kế quan hệ đầy đủ với FK, constraint, audit trail
3. **Hỗ trợ tiếng Việt**: NVARCHAR, collation Vietnamese
4. **Bảo mật**: AuthFilter bảo vệ 21 routes, session-based auth
5. **UI/UX**: Bootstrap 5, Material Icons, responsive design, toast notifications
6. **Module Category**: CRUD hoàn chỉnh, hierarchical tree, cycle prevention, pagination, search, filter

Các module khác đang trong giai đoạn phát triển và sẽ được triển khai theo lộ trình ưu tiên.

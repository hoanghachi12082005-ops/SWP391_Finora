# Quy Tắc Phụ Thuộc và Ràng Buộc Kiến Trúc

> **Mã tài liệu:** ARCH-DEP-001  
> **Phiên bản:** 1.0  
> **Ngày cập nhật:** 21/06/2026  
> **Tác giả:** Nhóm phát triển FinoraRetail  
> **Mục đích:** Định nghĩa các quy tắc phụ thuộc giữa các tầng, ràng buộc kiến trúc, và chính sách triển khai cho dự án SWP391_Finora (FinoraRetail)

---

## 1. Mục Đích và Phạm Vi

Tài liệu này thiết lập các quy tắc phụ thuộc (dependency rules) nhằm đảm bảo tính toàn vẹn kiến trúc của hệ thống FinoraRetail. Việc tuân thủ các quy tắc này giúp:

- Duy trì sự phân tách rõ ràng giữa các tầng (layer isolation)
- Ngăn chặn sự phụ thuộc vòng (circular dependency)
- Bảo vệ tính nhất quán của kiến trúc MVC
- Đảm bảo khả năng bảo trì và mở rộng mã nguồn về lâu dài
- Giảm thiểu rủi ro khi thực hiện refactoring

Mọi thay đổi kiến trúc liên quan đến phụ thuộc cần tuân theo quy trình được mô tả trong `docs/rules/REFACTOR_POLICY.md`.

---

## 2. Ma Trận Phụ Thuộc Cho Phép (Allowed Dependencies)

### 2.1. Tổng Quan Sơ Đồ Phụ Thuộc

```
┌──────────────────────────────────────────┐
│              JSP Views                    │
│         (No direct dependencies)          │
└──────────────┬───────────────────────────┘
               │ receives data via request/session attributes
┌──────────────▼───────────────────────────┐
│         Servlet Controllers               │
│  (extends BaseController)                 │
│                                           │
│  Allowed: → service                      │
│           → dao                          │
│           → model                        │
│           → util                         │
└──────────────┬───────────────────────────┘
               │ direct queries / complex orchestration
       ┌───────┴───────┐
       ▼               ▼
┌──────────────┐ ┌──────────────┐
│   Service    │ │     DAO      │
│   Layer      │ │   Layer      │
└──────────────┘ └──────┬───────┘
       │                 │
       │ (only for       │ (only for
       │  multi-DAO)      │  DBContext)
       ▼                 ▼
┌──────────────────────────────┐
│         Util Layer           │
│  (DBContext, AuthUtil, etc.) │
└──────────────┬───────────────┘
               │ JDBC connection
               ▼
┌──────────────────────────────┐
│        SQL Server DB         │
│       DBFinoraV2             │
└──────────────────────────────┘
```

### 2.2. Chi Tiết Các Phụ Thuộc Cho Phép

#### 2.2.1. Controller Kế Thừa BaseController

**Quy tắc:** Mọi servlet controller đều phải kế thừa từ `BaseController`.

```java
// ✅ HỢP LỆ
public class CategoryServlet extends BaseController { ... }
public class ProductController extends BaseController { ... }

// ❌ KHÔNG HỢP LỆ
public class ProductServlet extends HttpServlet { ... }  // Cần dùng BaseController
```

**Lý do:** `BaseController` cung cấp các helper method chuẩn hóa cho forward, redirect, và quản lý request/session attributes. Việc kế thừa từ `HttpServlet` trực tiếp bỏ qua các abstraction này và dẫn đến mã lặp lại.

#### 2.2.2. Controller Gọi Service

**Quy tắc:** Controller được phép gọi service layer thông qua composition hoặc dependency injection.

```java
// ✅ HỢP LỆ - Composition
public class CategoryServlet extends BaseController {
    private final CategoryService categoryService = new CategoryService();
}

// ✅ HỢP LỆ - Dependency Injection (nếu triển khai IoC container)
public class CategoryServlet extends BaseController {
    private final CategoryService categoryService;
    public CategoryServlet(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
}
```

**Lưu ý:** Hiện tại hệ thống sử dụng composition đơn giản. Khi nào cần mock trong unit test, có thể chuyển sang dependency injection.

#### 2.2.3. Controller Gọi DAO Trực Tiếp

**Quy tắc:** Controller được phép gọi DAO trực tiếp cho các thao tác CRUD đơn giản trên một entity duy nhất, khi không cần logic nghiệp vụ phức tạp.

```java
// ✅ HỢP LỆ - CRUD đơn giản trong controller
public class CategoryServlet extends BaseController {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        List<Category> categories = categoryDAO.findAll();
        req.setAttribute("categories", categories);
        forward("/views/categories/list.jsp", req, resp);
    }
}
```

**Khi nào nên dùng service thay vì DAO trực tiếp:** Xem phần **Service Layer Policy** (mục 5).

#### 2.2.4. Controller Sử Dụng Model và Util

```java
// ✅ HỢP LỆ
public class CategoryServlet extends BaseController {
    private final CategoryService categoryService = new CategoryService();
    private final AuthUtil authUtil = new AuthUtil();
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        User currentUser = authUtil.getCurrentUser(req.getSession());
        // ...
    }
}
```

#### 2.2.5. Service Gọi DAO và Model

```java
// ✅ HỢP LỆ - Service orchestration
public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    
    public boolean createOrder(Order order) {
        // Phối hợp nhiều DAO trong một transaction
        orderDAO.insert(order);
        inventoryDAO.updateStock(order.getItems());
        return true;
    }
}
```

#### 2.2.6. DAO Gọi Model và DBContext

```java
// ✅ HỢP LỆ
public class CategoryDAO extends BaseDAO {
    public List<Category> findAll() {
        String sql = "SELECT * FROM Categories WHERE Status = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(extractCategory(rs));
            }
            return categories;
        }
    }
    
    private Category extractCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("CategoryId"));
        c.setName(rs.getString("Name"));
        // ...
        return c;
    }
}
```

#### 2.2.7. Filter Phụ Thuộc BaseController

```java
// ✅ HỢP LỆ - AuthFilter có thể sử dụng helper từ BaseController
// nhưng KHÔNG được kế thừa BaseController (Filter không phải Servlet)
public class AuthFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("currentUser") == null) {
            ((HttpServletResponse) res).sendRedirect(request.getContextPath() + "/login");
            return;
        }
        chain.doFilter(req, res);
    }
}
```

---

## 3. Các Phụ Thuộc Bị Cấm (Disallowed Dependencies)

### 3.1. Cấm DAO Import Servlet API

**Quy tắc:** Các class trong package `dao` không được import bất kỳ class nào từ `jakarta.servlet.*` hoặc `javax.servlet.*`.

```java
// ❌ KHÔNG HỢP LỆ
package dao.product;

import jakarta.servlet.ServletException;       // Cấm
import jakarta.servlet.http.HttpServletRequest; // Cấm
import javax.servlet.http.HttpSession;          // Cấm

public class CategoryDAO extends BaseDAO {
    public void someMethod(HttpServletRequest req) { }  // Cấm
}
```

**Lý do:** DAO thuộc tầng truy xuất dữ liệu, hoàn toàn độc lập với HTTP. Việc phụ thuộc servlet API là vi phạm nguyên tắc separation of concerns và làm giảm khả năng tái sử dụng DAO trong các context khác (batch processing, scheduled jobs).

**Giải pháp thay thế:** Nếu cần truyền tham số từ request, hãy truyền giá trị primitive hoặc object thuần:

```java
// ✅ THAY THẾ HỢP LỆ
public class CategoryDAO extends BaseDAO {
    public List<Category> findByName(String name) { ... }
    public boolean updateStatus(int categoryId, boolean status) { ... }
}
```

### 3.2. Cấm JSP Mở Kết Nối Database

**Quy tắc:** JSP không được mở kết nối database, gọi `getConnection()`, hoặc sử dụng JDBC trực tiếp.

```jsp
<%-- ❌ KHÔNG HỢP LỆ --%>
<%@ page import="java.sql.*" %>
<% 
    Connection conn = DriverManager.getConnection(url, user, pass);
    // ... database operations
%>

<%-- ❌ KHÔNG HỢP LỆ - Cố gắng dùng DAO --%>
<% 
    CategoryDAO dao = new CategoryDAO();
    List<Category> categories = dao.findAll();
%>
```

**Giải pháp:** JSP chỉ nhận dữ liệu đã được chuẩn bị bởi controller:

```jsp
<%-- ✅ HỢP LỆ - JSP nhận dữ liệu từ request attribute --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:forEach var="category" items="${categories}">
    <div class="category-item">${fn:escapeXml(category.name)}</div>
</c:forEach>
```

### 3.3. Cấm JSP Khởi Tạo DAO

**Quy tắc:** JSP không được khởi tạo bất kỳ instance nào của DAO class.

```jsp
<%-- ❌ KHÔNG HỢP LỆ: JSP khởi tạo DAO trực tiếp --%>
<%-- <jsp:useBean id="categoryDAO" class="dao.product.CategoryDAO"/> --%>

<%-- ❌ KHÔNG HỢP LỆ --%>
<%
    CategoryDAO dao = new CategoryDAO();
    dao.delete(123);
%>
```

**Giải pháp:** Mọi thao tác database phải được thực hiện trong servlet controller, sau đó đặt kết quả vào request attribute hoặc session.

### 3.4. Cấm Model Phụ Thuộc DAO

**Quy tắc:** Các class trong package `model` không được phụ thuộc vào bất kỳ class nào trong package `dao`.

```java
// ❌ KHÔNG HỢP LỆ: Model chứa Servlet/JSP dependency
package model;

public class Category {
    private CategoryDAO dao;  // Cấm - model không được biết về DAO
    
    public List<Category> findAll() {
        return dao.findAll();  // Cấm
    }
}
```

**Lý do:** Model đại diện cho domain entity, phản ánh nghiệp vụ thực tế. DAO là tầng truy xuất dữ liệu. Để model phụ thuộc dao tạo ra sự phụ thuộc vòng tiềm ẩn và vi phạm nguyên tắc single responsibility.

### 3.5. Cấm Controller Phụ Thuộc JSP Trực Tiếp

**Quy tắc:** Controller (servlet) không được import JSP class hoặc phụ thuộc vào implementation của JSP.

```java
// ❌ KHÔNG HỢP LỆ
import com.storemanagement.views.categories.ListJSP;  // Cấm

public class CategoryServlet extends BaseController {
    public void someMethod() {
        ListJSP jsp = new ListJSP();  // Cấm
    }
}
```

**Quy tắc đúng:** Controller forward đến JSP thông qua đường dẫn view, không phải thông qua class reference:

```java
// ✅ HỢP LỆ
public class CategoryServlet extends BaseController {
    public void listCategories(HttpServletRequest req, HttpServletResponse resp) {
        // Chuẩn bị dữ liệu
        List<Category> categories = categoryDAO.findAll();
        req.setAttribute("categories", categories);
        
        // Forward đến view qua đường dẫn
        forward("/views/categories/list.jsp", req, resp);
    }
}
```

### 3.6. Cấm Phụ Thuộc Vòng (Circular Dependencies)

**Quy tắc:** Không được phép tồn tại phụ thuộc vòng giữa bất kỳ hai package nào.

**Các cấu hình bị cấm:**

```
controller → service → dao → controller  (vòng)
service → dao → service                   (vòng)
model → dao → model                       (vòng)
```

**Kiểm tra:** Khi thêm dependency mới, cần đảm bảo không tạo ra chu trình phụ thuộc. Có thể sử dụng công cụ phân tích dependency như Maven Dependency Plugin để phát hiện vòng lặp.

---

## 4. Quy Tắc JSP

### 4.1. Nguyên Tắc Chung

JSP là tầng View trong kiến trúc MVC. Trách nhiệm duy nhất của JSP là nhận dữ liệu từ request/session và render HTML output. Mọi business logic, database access, và data transformation phải thực hiện ở tầng controller hoặc service.

### 4.2. Cấm Scriptlet

**Quy tắc:** Không sử dụng scriptlet (`<% %>`) cho business logic hoặc truy xuất database.

```jsp
<%-- ❌ KHÔNG HỢP LỆ --%>
<%
    CategoryDAO dao = new CategoryDAO();
    List<Category> list = dao.findAll();
    for (Category c : list) {
%>
        <li><%= c.getName() %></li>
<% } %>

<%-- ✅ HỢP LỆ - Sử dụng JSTL --%>
<c:forEach var="category" items="${categories}">
    <li>${fn:escapeXml(category.name)}</li>
</c:forEach>
```

### 4.3. Sử Dụng JSTL Thay Scriptlet

**JSTL Core Tags (bắt buộc):**

| Tag | Mục đích | Ví dụ |
|-----|----------|-------|
| `<c:if>` | Điều kiện | `<c:if test="${not empty categories}">` |
| `<c:forEach>` | Vòng lặp | `<c:forEach var="c" items="${categories}">` |
| `<c:choose>` | Switch-case | `<c:choose><c:when>` |
| `<c:set>` | Đặt biến | `<c:set var="total" value="${total + c.price}"/>` |

### 4.4. Xử Lý XSS — Escaping User Content

**Quy tắc:** Mọi nội dung do người dùng cung cấp phải được escape trước khi render để ngăn XSS (Cross-Site Scripting).

```jsp
<%-- ✅ HỢP LỆ - Sử dụng fn:escapeXml() --%>
<div class="category-name">${fn:escapeXml(category.name)}</div>
<input type="text" value="${fn:escapeXml(category.description)}"/>

<%-- ⚠️ Chỉ không escape khi nội dung đã được sanitize từ server --%>
<div class="html-content">${category.htmlContent}</div>
```

### 4.5. Giữ Business Logic Trong Servlet

**Quy tắc:** Nếu cần thực hiện tính toán hoặc biến đổi dữ liệu phục vụ hiển thị, thực hiện trong servlet trước khi forward.

```java
// ✅ HỢP LỆ - Tính toán trong servlet
protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    List<Category> categories = categoryDAO.findAll();
    
    // Tính toán phục vụ hiển thị trong servlet
    int totalProducts = categories.stream()
        .mapToInt(Category::getProductCount)
        .sum();
    
    req.setAttribute("categories", categories);
    req.setAttribute("totalProducts", totalProducts);
    forward("/views/categories/list.jsp", req, resp);
}
```

```jsp
<%-- ✅ HỢP LỆ - JSP chỉ render --%>
<p>Tổng sản phẩm: ${totalProducts}</p>
```

### 4.6. Mã Hóa Ký Tự — UTF-8 Không BOM

**Quy tắc:** Mọi file JSP phải được lưu với mã hóa UTF-8 không có BOM (Byte Order Mark).

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
```

**Kiểm tra trong IDE:** Đảm bảo encoding settings của project đặt là UTF-8 và không có BOM.

---

## 5. Chính Sách Service Layer

### 5.1. Nguyên Tắc Quyết Định Khi Nào Cần Service

Service layer không phải lúc nào cũng bắt buộc. Dưới đây là ma trận quyết định:

| Tình huống | Gọi DAO trực tiếp từ Controller | Cần Service Layer |
|------------|-----------------------------------|-------------------|
| CRUD đơn giản trên 1 entity | ✅ | ❌ |
| Truy vấn với filter/pagination | ✅ | ❌ |
| Thao tác trên 1 entity + validation | ✅ | ❌ |
| Phối hợp 2+ DAO trong 1 nghiệp vụ | ❌ | ✅ |
| Cần transaction boundary | ❌ | ✅ |
| Logic được tái sử dụng bởi nhiều controller | ❌ | ✅ |
| Business rule phức tạp | ❌ | ✅ |
| Integration với external API | ❌ | ✅ |

### 5.2. Ví Dụ: Khi Nào Dùng Service

#### Trường Hợp 1: Gọi DAO Trực Tiếp (Hợp Lệ)

```java
// CategoryServlet.java - CRUD đơn giản
public class CategoryServlet extends BaseController {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    
    // GET /categories
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        List<Category> categories = categoryDAO.findAll();  // ✅ Gọi DAO trực tiếp
        req.setAttribute("categories", categories);
        forward("/views/categories/list.jsp", req, resp);
    }
    
    // POST /categories/create
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        Category category = extractFromRequest(req);
        boolean success = categoryDAO.insert(category);  // ✅ Gọi DAO trực tiếp
        if (success) {
            redirect("/categories", req, resp);
        } else {
            req.setAttribute("error", "Tạo danh mục thất bại");
            forward("/views/categories/create.jsp", req, resp);
        }
    }
}
```

#### Trường Hợp 2: Cần Service Layer (Bắt Buộc)

```java
// OrderServlet.java - Nghiệp vụ phức tạp
public class OrderServlet extends BaseController {
    private final OrderService orderService = new OrderService();  // ✅ Service
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        Order order = extractFromRequest(req);
        
        // ✅ Service xử lý: validate + cập nhật order + cập nhật inventory + gửi notification
        ServiceResult result = orderService.createOrder(order);
        
        if (result.isSuccess()) {
            req.setAttribute("orderId", result.getOrderId());
            forward("/views/orders/confirmation.jsp", req, resp);
        } else {
            req.setAttribute("errors", result.getErrors());
            forward("/views/orders/create.jsp", req, resp);
        }
    }
}
```

```java
// OrderService.java - Xử lý nghiệp vụ phức tạp
public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final NotificationService notificationService = new NotificationService();
    
    public ServiceResult createOrder(Order order) {
        // 1. Validate tất cả sản phẩm trong đơn
        for (OrderDetail detail : order.getItems()) {
            if (!productDAO.exists(detail.getProductId())) {
                return ServiceResult.error("Sản phẩm không tồn tại: " + detail.getProductId());
            }
            if (!inventoryDAO.checkStock(detail.getProductId(), detail.getQuantity())) {
                return ServiceResult.error("Sản phẩm không đủ tồn kho: " + detail.getProductName());
            }
        }
        
        // 2. Tính tổng tiền
        order.calculateTotal();
        
        // 3. Insert order trong transaction
        orderDAO.insert(order);
        
        // 4. Cập nhật tồn kho
        for (OrderDetail detail : order.getItems()) {
            inventoryDAO.decreaseStock(detail.getProductId(), detail.getQuantity());
        }
        
        // 5. Gửi notification
        notificationService.sendOrderConfirmation(order);
        
        return ServiceResult.success(order.getOrderId());
    }
}
```

### 5.3. Ràng Buộc Của Service Layer

**Quy tắc:** Service class không được phụ thuộc vào:

```java
// ❌ KHÔNG HỢP LỆ - Service phụ thuộc web layer
public class OrderService {
    private HttpServletResponse response;  // Cấm
    private HttpServletRequest request;    // Cấm
    
    public void processOrder(HttpServletRequest req) { }  // Cấm
}

// ❌ KHÔNG HỢP LỆ - Service lưu trạng thái session
public class OrderService {
    private HttpSession session;  // Cấm
    
    public void setSession(HttpSession session) { }  // Cấm
}
```

**Giải pháp đúng:** Service nhận và trả về plain objects, không biết gì về HTTP:

```java
// ✅ HỢP LỆ
public class OrderService {
    public ServiceResult createOrder(Order order) { ... }
    public OrderDTO getOrderSummary(int orderId) { ... }
    public boolean cancelOrder(int orderId, String reason) { ... }
}
```

### 5.4. Quy Tắc Đặt Tên Service

- Tên class: `XxxService` (đuôi Service)
- Tên package: `service.xxx` (match với module tương ứng)
- Interface (nếu có): `XxxServiceInterface` hoặc `IXxxService`

---

## 6. Quy Tắc DAO

### 6.1. Nguyên Tắc Sở Hữu SQL

**Quy tắc:** Tất cả câu lệnh SQL phải được định nghĩa và sở hữu hoàn toàn bởi class DAO. Không có SQL string được phép xuất hiện bên ngoài DAO.

```java
// ✅ HỢP LỆ - SQL được đóng gói trong DAO
public class CategoryDAO extends BaseDAO {
    public List<Category> findByParentId(Integer parentId) {
        String sql = "SELECT c.*, p.Name AS ParentName " +
                     "FROM Categories c " +
                     "LEFT JOIN Categories p ON c.ParentId = p.CategoryId " +
                     "WHERE c.ParentId " + (parentId == null ? "IS NULL" : "= ?");
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parentId != null) {
                ps.setInt(1, parentId);
            }
            // ...
        }
    }
}
```

### 6.2. Sử Dụng Private Extract Methods

**Quy tắc:** Ánh xạ ResultSet sang entity phải được thực hiện bằng private method riêng cho mỗi entity.

```java
// ✅ HỢP LỆ - extract method riêng cho Category
public class CategoryDAO extends BaseDAO {
    
    private Category extractCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("CategoryId"));
        c.setName(rs.getString("Name"));
        c.setDescription(rs.getString("Description"));
        c.setParentId((Integer) rs.getObject("ParentId"));
        c.setStatus(rs.getBoolean("Status"));
        c.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
        
        // Lấy parentName từ JOIN column nếu có
        try {
            c.setParentName(rs.getString("ParentName"));
        } catch (SQLException ignored) {
            // Column không tồn tại trong result set
        }
        
        return c;
    }
    
    private Category extractCategorySimple(ResultSet rs) throws SQLException {
        // Phiên bản đơn giản không có JOIN
        Category c = new Category();
        c.setCategoryId(rs.getInt("CategoryId"));
        c.setName(rs.getString("Name"));
        // ...
        return c;
    }
}
```

### 6.3. Sử Dụng PreparedStatement — Cấm String Concatenation

**Quy tắc:** Tất cả truy vấn có tham số đầu vào từ người dùng bắt buộc phải sử dụng `PreparedStatement` với placeholder `?`. Tuyệt đối cấm nối chuỗi SQL.

```java
// ❌ KHÔNG HỢP LỆ - SQL Injection vulnerability
public List<Category> search(String keyword) {
    String sql = "SELECT * FROM Categories WHERE Name LIKE '%" + keyword + "%'";
    // attacker input: "'; DROP TABLE Categories; --"
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
}

// ✅ HỢP LỆ - Sử dụng PreparedStatement
public List<Category> search(String keyword) {
    String sql = "SELECT * FROM Categories WHERE Name LIKE ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();
        // ...
    }
}
```

### 6.4. Sử Dụng Try-With-Resources

**Quy tắc:** Mọi tài nguyên `Connection`, `PreparedStatement`, `ResultSet` phải được giải phóng tự động bằng try-with-resources.

```java
// ❌ KHÔNG HỢP LỆ - Rò rỉ tài nguyên
public Category findById(int id) {
    Connection conn = getConnection();
    PreparedStatement ps = conn.prepareStatement("SELECT * FROM Categories WHERE CategoryId = ?");
    ps.setInt(1, id);
    ResultSet rs = ps.executeQuery();
    // Nếu exception xảy ra, conn và ps không được đóng
}

// ✅ HỢP LỆ - Try-with-resources tự động đóng
public Category findById(int id) {
    String sql = "SELECT * FROM Categories WHERE CategoryId = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return extractCategory(rs);
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException("Lỗi truy vấn danh mục", e);
    }
    return null;
}
```

### 6.5. Quy Ước Trả Về Của DAO

| Thao tác | Kiểu trả về | Mô tả |
|----------|-------------|--------|
| `insert()` | `boolean` | `true` nếu thành công |
| `update()` | `boolean` | `true` nếu thành công |
| `delete()` | `boolean` | `true` nếu thành công |
| `findById()` | `T` hoặc `null` | Entity hoặc null nếu không tìm thấy |
| `findAll()` | `List<T>` | Danh sách entity, có thể rỗng |
| `count()` | `int` | Số lượng bản ghi |
| `exists()` | `boolean` | Kiểm tra tồn tại |

### 6.6. Ví Dụ DAO Hoàn Chỉnh

```java
package dao.product;

import util.database.DBContext;
import model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO extends BaseDAO {
    
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = """
            SELECT c.CategoryId, c.Name, c.Description, c.ParentId,
                   p.Name AS ParentName, c.Status, c.CreatedAt, c.UpdatedAt,
                   (SELECT COUNT(*) FROM Products WHERE CategoryId = c.CategoryId) AS ProductCount
            FROM Categories c
            LEFT JOIN Categories p ON c.ParentId = p.CategoryId
            ORDER BY c.CategoryId
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(extractCategory(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn danh mục", e);
        }
        return categories;
    }
    
    public Category findById(int id) {
        String sql = "SELECT * FROM Categories WHERE CategoryId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCategorySimple(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm danh mục theo ID", e);
        }
        return null;
    }
    
    public boolean insert(Category category) {
        String sql = """
            INSERT INTO Categories (Name, Description, ParentId, Status, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, GETDATE(), GETDATE())
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            if (category.getParentId() != null) {
                ps.setInt(3, category.getParentId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBoolean(4, category.isStatus());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setCategoryId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tạo danh mục", e);
        }
        return false;
    }
    
    public boolean update(Category category) {
        String sql = """
            UPDATE Categories
            SET Name = ?, Description = ?, ParentId = ?, Status = ?, UpdatedAt = GETDATE()
            WHERE CategoryId = ?
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            if (category.getParentId() != null) {
                ps.setInt(3, category.getParentId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBoolean(4, category.isStatus());
            ps.setInt(5, category.getCategoryId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật danh mục", e);
        }
    }
    
    public boolean delete(int id) {
        // Soft delete - chỉ cập nhật status
        String sql = "UPDATE Categories SET Status = 0, UpdatedAt = GETDATE() WHERE CategoryId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa danh mục", e);
        }
    }
    
    private Category extractCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("CategoryId"));
        c.setName(rs.getString("Name"));
        c.setDescription(rs.getString("Description"));
        c.setParentId((Integer) rs.getObject("ParentId"));
        c.setParentName(rs.getString("ParentName"));
        c.setStatus(rs.getBoolean("Status"));
        c.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
        c.setProductCount(rs.getInt("ProductCount"));
        return c;
    }
    
    private Category extractCategorySimple(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("CategoryId"));
        c.setName(rs.getString("Name"));
        c.setDescription(rs.getString("Description"));
        c.setParentId((Integer) rs.getObject("ParentId"));
        c.setStatus(rs.getBoolean("Status"));
        c.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
        return c;
    }
}
```

---

## 7. Ma Trận Phụ Thuộc Tổng Hợp

| Từ \ Đến | Controller | Service | DAO | Model | Util | JSP | Servlet API | Filter |
|----------|-----------|---------|-----|-------|------|-----|-------------|--------|
| **Controller** | ✅ (inherit) | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ (use Base) | ❌ |
| **Service** | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| **DAO** | ❌ | ❌ | ✅ | ✅ | ✅ (DBContext) | ❌ | ❌ | ❌ |
| **Model** | ❌ | ❌ | ❌ | ✅ | ✅ (if needed) | ❌ | ❌ | ❌ |
| **Filter** | ❌ | ❌ | ✅ (AuthUtil) | ❌ | ✅ | ❌ | ✅ | ✅ |
| **JSP** | ❌ | ❌ | ❌ | ✅ (EL) | ✅ (EL) | ✅ | ❌ | ❌ |

**Chú thích:**
- ✅ = Được phép
- ❌ = Bị cấm
- Controller → Controller (inherit) = Cho phép kế thừa BaseController

---

## 8. Checklist Tuân Thủ

Trước khi commit hoặc merge, kiểm tra:

### Controller
- [ ] Kế thừa `BaseController` (không phải `HttpServlet` trực tiếp)
- [ ] Không chứa câu lệnh SQL
- [ ] Không khởi tạo DAO nếu nghiệp vụ phức tạp (dùng Service)
- [ ] Đặt dữ liệu vào request attribute trước khi forward
- [ ] Sử dụng `fn:escapeXml()` khi truyền user input sang JSP

### DAO
- [ ] Không import `jakarta.servlet.*` hoặc `javax.servlet.*`
- [ ] Sử dụng `PreparedStatement` cho mọi truy vấn có tham số
- [ ] Sử dụng try-with-resources cho `Connection`, `PreparedStatement`, `ResultSet`
- [ ] Có private `extractXxx(ResultSet)` method
- [ ] SQL strings không có nối chuỗi với input

### Service
- [ ] Không phụ thuộc `HttpServletRequest`, `HttpServletResponse`, `HttpSession`
- [ ] Nhận và trả về plain objects (model, DTO)
- [ ] Chỉ tồn tại khi cần phối hợp nhiều DAO hoặc có business logic phức tạp

### JSP
- [ ] Không có scriptlet chứa logic
- [ ] Sử dụng JSTL (`<c:forEach>`, `<c:if>`)
- [ ] Mọi user input được escape bằng `fn:escapeXml()`
- [ ] Không khởi tạo DAO hoặc mở kết nối
- [ ] File được lưu với encoding UTF-8 không BOM

---

## 9. Xử Lý Vi Phạm

Khi phát hiện vi phạm dependency rules:

1. **Trong code review:** Đánh dấu là violation, yêu cầu refactor trước khi approve
2. **Trong CI/CD:** Có thể cấu hình static analysis (ví dụ: PMD, SpotBugs) để phát hiện tự động
3. **Trong IDE:** Cài đặt plugin architecture visualization để hiển thị dependency graph

---

## 10. Cập Nhật Quy Tắc

Các thay đổi đối với tài liệu này cần:

- Được讨论 và approve trong team
- Cập nhật vào `docs/rules/REFACTOR_POLICY.md`
- Được ghi nhận trong `docs/decisions/` nếu là quyết định kiến trúc quan trọng
- Áp dụng cho tất cả code mới sau ngày có hiệu lực

---

*Lưu ý: Các quy tắc trong tài liệu này là bắt buộc. Vi phạm nghiêm trọng có thể dẫn đến từ chối merge cho đến khi được sửa.*

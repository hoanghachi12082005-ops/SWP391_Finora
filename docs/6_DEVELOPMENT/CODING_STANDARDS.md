# Tiêu Chuẩn Lập Trình - Coding Standards

> **Mục đích:** Tài liệu này quy định các quy ước và tiêu chuẩn lập trình bắt buộc áp dụng cho toàn bộ mã nguồn dự án FinoraRetail (SWP391_Finora). Mọi thành viên phát triển và agent AI đều phải tuân thủ nghiêm ngặt các tiêu chuẩn được nêu tại đây.

---

## 1. Tiêu Chuẩn Java

### 1.1. Quy Ước Đặt Tên Package

- Tên package viết **thường hoàn toàn**, phân cách bằng dấu chấm.
- Cấu trúc theo feature/module, không theo tầng (layer).
- Package gốc: `com.storemanagement`

```
com.storemanagement.controller
com.storemanagement.controller.product
com.storemanagement.dao
com.storemanagement.dao.product
com.storemanagement.model
com.storemanagement.service
com.storemanagement.util
com.storemanagement.filter
com.storemanagement.dto
```

**Nguyên tắc:**

- Không đặt class trực tiếp vào package gốc `com.storemanagement`.
- Mỗi module/feature có package con riêng nếu cần phân tách rõ ràng.
- Package `dto` dùng cho data-transfer object phục vụ view hoặc API response.

### 1.2. Quy Ước Đặt Tên Class

- Sử dụng **PascalCase**.
- Thêm hậu tố phù hợp để phản ánh loại class:

| Loại class | Hậu tố | Ví dụ |
|---|---|---|
| Servlet / Controller | `Servlet` hoặc theo chức năng | `CategoryServlet`, `ProductController` |
| Data Access Object | `DAO` | `CategoryDAO`, `ProductDAO` |
| Service Layer | `Service` | `CategoryService`, `ReportService` |
| Utility | `Util` | `ValidationUtil`, `DateUtil` |
| Filter | `Filter` | `AuthFilter`, `EncodingFilter` |
| Model / Entity | không hậu tố | `Category`, `Product`, `Employee` |
| DTO | theo ngữ cảnh | `CategoryDTO`, `OrderSummaryDTO` |

### 1.3. Quy Ước Đặt Tên Method

Sử dụng **camelCase**, tiền tố hành động phản ánh rõ nghiệp vụ:

| Tiền tố | Mục đích | Ví dụ |
|---|---|---|
| `get` | Lấy thông tin theo khóa chính hoặc thuộc tính duy nhất | `getCategoryById(int id)` |
| `find` | Tìm kiếm theo nhiều tiêu chí | `findProductsByName(String name)` |
| `search` | Tìm kiếm với bộ lọc phức tạp | `searchProducts(String keyword, Integer categoryId)` |
| `count` | Đếm số lượng bản ghi | `countActiveProducts()` |
| `list` | Lấy danh sách không có điều kiện đặc biệt | `listAllCategories()` |
| `add` / `insert` / `create` | Thêm bản ghi mới | `addProduct(Product p)`, `insertCategory(Category c)` |
| `update` / `edit` | Cập nhật bản ghi hiện có | `updateProduct(Product p)` |
| `delete` / `remove` | Xóa bản ghi | `deleteCategory(int id)` |
| `validate` | Kiểm tra tính hợp lệ | `validateCategoryInput(HttpServletRequest)` |

### 1.4. Xử Lý JDBC và PreparedStatement

**BẮT BUỘC sử dụng `PreparedStatement` cho mọi câu truy vấn có dữ liệu người dùng.**

```java
// ❌ SAI - String concatenation (SQL Injection)
String sql = "SELECT * FROM Category WHERE CategoryName = '" + name + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);

// ✅ ĐÚNG - PreparedStatement
String sql = "SELECT * FROM Category WHERE CategoryName = ?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, name);
    try (ResultSet rs = ps.executeQuery()) {
        // xử lý
    }
}
```

**Nguyên tắc:**

- Tất cả tham số từ `HttpServletRequest` phải được bind qua `PreparedStatement`.
- Không nối chuỗi SQL với biến request dưới bất kỳ hình thức nào.
- Đặt tên tham số `?` theo thứ tự logic, comment nếu cần.

### 1.5. Xử Lý Tài Nguyên (try-with-resources)

**BẮT BUỘC sử dụng try-with-resources cho mọi thao tác JDBC.**

```java
// ❌ SAI
Connection conn = null;
PreparedStatement ps = null;
ResultSet rs = null;
try {
    conn = getConnection();
    ps = conn.prepareStatement(sql);
    // ...
} finally {
    if (rs != null) rs.close();
    if (ps != null) ps.close();
    if (conn != null) conn.close();
}

// ✅ ĐÚNG
try (Connection conn = getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // ...
}
```

### 1.6. Xử Lý Ngoại Lệ

**Nghiêm cấm để empty catch block.**

```java
// ❌ SAI
} catch (SQLException e) {
    // do nothing
}

// ✅ TỐI THIỂU phải log
} catch (SQLException e) {
    e.printStackTrace();
}

// ✅ TỐT hơn - log có ngữ cảnh
} catch (SQLException e) {
    logger.error("Failed to retrieve category with ID: " + id, e);
    throw new RuntimeException("Database error while fetching category", e);
}
```

### 1.7. Mã Hóa Ký Tự

**Tất cả file Java phải được lưu dưới dạng UTF-8 without BOM.**

- Cấu hình IDE: `File Encoding = UTF-8`, tắt `UTF-8 BOM`.
- Kiểm tra bằng hex editor nếu nghi ngờ.
- Các file `src/java/**/*.java` khi tạo mới phải đảm bảo không có BOM.

---

## 2. Tiêu Chuẩn JSP

### 2.1. Nguyên Tắc Quan Trọng

**JSP chỉ phụ trách hiển thị. Tuyệt đối không:**

- Viết SQL hoặc gọi DAO trực tiếp trong JSP.
- Tạo đối tượng kết nối database trong JSP.
- Chứa logic nghiệp vụ phức tạp.

```jsp
<%-- ❌ NGUY HIỂM - SQL trong JSP --%>
<%
    CategoryDAO dao = new CategoryDAO();
    List<Category> list = dao.listAllCategories();
%>

<%-- ✅ ĐÚNG - Nhận dữ liệu từ request attribute do servlet gửi xuống --%>
<c:forEach var="cat" items="${categories}">
    <option value="${cat.categoryID}">${fn:escapeXml(cat.categoryName)}</option>
</c:forEach>
```

### 2.2. Sử Dụng JSTL Thay Cho Scriptlet

Thay thế `<% %>` bằng JSTL tags:

| Scriptlet | JSTL |
|---|---|
| `<% if (x) { %>` | `<c:if test="${x}">` |
| `<% for (Item i : list) { %>` | `<c:forEach var="i" items="${list}">` |
| `<%= request.getAttribute("x") %>` | `${x}` |
| `<%= request.getParameter("x") %>` | `${param.x}` |

### 2.3. Xử Lý XSS (Cross-Site Scripting)

**BẮT BUỘC sử dụng `${fn:escapeXml()}` cho mọi nội dung do người dùng cung cấp.**

```jsp
<%-- ❌ NGUY HIỂM --%>
<span>${product.productName}</span>
<input value="${param.searchKeyword}">

<%-- ✅ ĐÚNG --%>
<span>${fn:escapeXml(product.productName)}</span>
<input value="${fn:escapeXml(param.searchKeyword)}">
```

### 2.4. Mã Hóa Ký Tự JSP

Khai báo đầu file JSP:

```jsp
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
```

**Lưu file dưới dạng UTF-8 without BOM.**

### 2.5. Cấu Trúc Thư Mục JSP

```
web/WEB-INF/views/
├── categories/
│   ├── list.jsp
│   ├── detail.jsp
│   └── form.jsp
├── products/
│   ├── list.jsp
│   ├── detail.jsp
│   └── form.jsp
├── layouts/
│   └── admin-layout.jsp
└── common/
    ├── header.jsp
    └── footer.jsp
```

- Thư mục `WEB-INF/views` để tránh truy cập trực tiếp qua URL.
- Mỗi module có thư mục con riêng.

---

## 3. Tiêu Chuẩn Servlet

### 3.1. Validate Tham Số Trước Khi Gọi DAO

```java
// ❌ SAI - gọi DAO không kiểm tra
CategoryDAO dao = new CategoryDAO();
Category c = dao.getCategoryById(request.getParameter("id"));

// ✅ ĐÚNG - validate trước
String idParam = request.getParameter("id");
if (idParam == null || idParam.trim().isEmpty()) {
    response.sendRedirect("categories");
    return;
}
int id;
try {
    id = Integer.parseInt(idParam);
} catch (NumberFormatException e) {
    response.sendRedirect("categories");
    return;
}
```

### 3.2. Mẫu POST-Redirect-GET (PRG)

**Áp dụng cho mọi thao tác thay đổi trạng thái (thêm, sửa, xóa).**

```java
// Servlet xử lý POST (thêm mới)
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    // Validate và xử lý
    boolean success = categoryDAO.addCategory(category);
    
    // Flash message qua session
    HttpSession session = request.getSession();
    if (success) {
        session.setAttribute("flashMessage", "Thêm danh mục thành công!");
        session.setAttribute("flashType", "success");
    } else {
        session.setAttribute("flashMessage", "Thêm danh mục thất bại!");
        session.setAttribute("flashType", "danger");
    }
    
    // ✅ Redirect thay vì forward để tránh double-submit
    response.sendRedirect(request.getContextPath() + "/categories");
}
```

```java
// Servlet xử lý GET (hiển thị danh sách)
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    List<Category> categories = categoryDAO.listAllCategories();
    request.setAttribute("categories", categories);
    
    // Hiển thị flash message từ session rồi xóa
    HttpSession session = request.getSession();
    request.setAttribute("flashMessage", session.getAttribute("flashMessage"));
    session.removeAttribute("flashMessage");
    session.removeAttribute("flashType");
    
    // ✅ Forward cho GET request
    request.getRequestDispatcher("/WEB-INF/views/categories/list.jsp").forward(request, response);
}
```

### 3.3. Đặt Request Attributes

Servlet chịu trách nhiệm chuẩn bị dữ liệu cho JSP:

```java
request.setAttribute("categories", categories);
request.setAttribute("currentPage", page);
request.setAttribute("totalPages", totalPages);
```

### 3.4. Xử Lý Tham Số UTF-8

```java
// Đặt ở đầu doGet/doPost
request.setCharacterEncoding("UTF-8");
response.setCharacterEncoding("UTF-8");
```

Hoặc sử dụng `EncodingFilter`:

```java
@Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    chain.doFilter(request, response);
}
```

---

## 4. Tiêu Chuẩn DAO

### 4.1. SQL thuộc về DAO

**Tất cả câu lệnh SQL phải được định nghĩa và quản lý trong class DAO.** Không truyền SQL từ servlet hay service vào DAO.

### 4.2. Private Extract Method cho ResultSet Mapping

```java
private Category extractCategory(ResultSet rs) throws SQLException {
    Category c = new Category();
    c.setCategoryID(rs.getInt("CategoryID"));
    c.setCategoryName(rs.getString("CategoryName"));
    c.setDescription(rs.getString("Description"));
    c.setStatus(rs.getBoolean("Status"));
    c.setCreatedAt(rs.getTimestamp("CreatedAt"));
    return c;
}
```

### 4.3. PreparedStatement cho Mọi Tham Số Người Dùng

```java
public Category getCategoryById(int id) {
    String sql = "SELECT * FROM Category WHERE CategoryID = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return extractCategory(rs);
            }
        }
    } catch (SQLException e) {
        logger.error("Error getting category by ID: " + id, e);
    }
    return null;
}
```

### 4.4. Quy Ước Trả Về

| Thao tác | Kiểu trả về |
|---|---|
| `INSERT` | `boolean` (thành công/thất bại) hoặc `int` (số bản ghi bị ảnh hưởng) |
| `UPDATE` | `boolean` hoặc `int` |
| `DELETE` | `boolean` hoặc `int` |
| `SELECT` một bản ghi | `Entity` hoặc `null` |
| `SELECT` nhiều bản ghi | `List<Entity>` |
| `SELECT` đếm | `int` |

### 4.5. Tên Phương Thức DAO

```
get<Entity>ById(int id)          → getCategoryById
listAll<Entity>()                 → listAllCategories
find<Entity>By...()               → findProductsByName
count<Entity>By...()              → countActiveProducts
add<Entity>(Entity e)             → addProduct
update<Entity>(Entity e)          → updateProduct
delete<Entity>(int id)            → deleteProduct
```

---

## 5. Tiêu Chuẩn Service

### 5.1. Khi Nào Cần Service

**Chỉ tạo service khi thỏa mãn ít nhất một trong các điều kiện:**

- Cần phối hợp nhiều DAO trong cùng một giao dịch.
- Cần transaction boundary cho nhiều thao tác SQL.
- Logic nghiệp vụ được sử dụng bởi nhiều servlet khác nhau.
- Business rules quá phức tạp để đặt trong servlet.

### 5.2. Service KHÔNG ĐƯỢC

- Phụ thuộc vào `JSP`, `HttpServletRequest`, `HttpServletResponse`, hoặc `HttpSession`.
- Lưu trạng thái phiên (session state).
- Xử lý redirect hoặc forward.
- Chứa SQL (phải ủy thác cho DAO).

### 5.3. Ví Dụ Service

```java
public class OrderService {
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    
    public boolean createOrder(Order order, List<OrderDetail> details) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            int orderId = orderDAO.insertOrder(conn, order);
            for (OrderDetail detail : details) {
                detail.setOrderID(orderId);
                orderDetailDAO.insert(conn, detail);
                productDAO.decreaseStock(detail.getProductID(), detail.getQuantity());
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            logger.error("Failed to create order", e);
            return false;
        } finally {
            if (conn != null) DatabaseUtil.close(conn);
        }
    }
}
```

---

## 6. Tiêu Chuẩn Bảo Mật

### 6.1. Không Hardcode Thông Tin Nhạy Cảm

**Tuyệt đối không ghi credentials trong source code:**

```java
// ❌ NGUY HIỂM
private static final String DB_PASSWORD = "admin123";
String token = "sk-abc123xyz";

// ✅ ĐÚNG - Đọc từ context.xml hoặc environment variable
String password = System.getenv("DB_PASSWORD");
```

### 6.2. Không Log Thông Tin Nhạy Cảm

```java
// ❌ NGUY HIỂM
logger.info("User login: " + username + ", password: " + password);

// ✅ ĐÚNG
logger.info("User login attempt: " + username);
```

### 6.3. Validate và Sanitize Mọi Dữ Liệu Người Dùng

- Validate phía server (không chỉ client-side).
- Kiểm tra null, rỗng, kiểu dữ liệu, giới hạn độ dài.
- Sanitize output bằng `${fn:escapeXml()}` trong JSP.

### 6.4. Bảo Vệ Thao Tác Thay Đổi Trạng Thái

- Các thao tác POST/PUT/DELETE phải được bảo vệ sau authentication.
- Kiểm tra session/user trong filter hoặc servlet trước khi xử lý.
- Áp dụng PRG pattern để tránh double-submit và refresh gây duplicate action.

---

## 7. Tổng Kết Checklist

Trước khi commit hoặc tạo PR, đảm bảo:

- [ ] Tất cả SQL sử dụng `PreparedStatement`.
- [ ] Tất cả tài nguyên JDBC trong try-with-resources.
- [ ] Không có empty catch block.
- [ ] File Java/JSP lưu UTF-8 without BOM.
- [ ] JSP không chứa SQL hay DAO.
- [ ] Sử dụng `${fn:escapeXml()}` cho dữ liệu người dùng.
- [ ] Thao tác state-changing sử dụng PRG pattern.
- [ ] Không hardcode credentials.
- [ ] Validate tham số trước khi gọi DAO.
- [ ] Method nhỏ, dễ review.

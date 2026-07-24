# Luồng Dữ Liệu Trong Hệ Thống

**Dự án:** FinoraRetail (SWP391_Finora)  
**Cơ sở dữ liệu:** DBFinoraV3 trên SQL Server  
**Kiến trúc:** Layered MVC với Servlet Controller, DAO, JSP View  
**Phiên bản tài liệu:** 1.0  
**Ngày cập nhật:** 21/06/2026

---

## Mục Lục

1. [Tổng Quan Luồng Dữ Liệu](#1-tổng-quan-luồng-dữ-liệu)
2. [Luồng Nhập Dữ Liệu (Data Entry Flow)](#2-luồng-nhập-dữ-liệu-data-entry-flow)
3. [Luồng Truy Xuất Dữ Liệu (Data Retrieval Flow)](#3-luồng-truy-xuất-dữ-liệu-data-retrieval-flow)
4. [Luồng Dữ Liệu Theo Chức Năng](#4-luồng-dữ-liệu-theo-chức-năng)
5. [Mẫu Flash Message](#5-mẫu-flash-message)
6. [Ranh Giới Giao Dịch (Transaction Boundaries)](#6-ranh-giới-giao-dịch-transaction-boundaries)
7. [Mẫu Thiết Kế DAO](#7-mẫu-thiết-kế-dao)
8. [Xử Lý Lỗi](#8-xử-lý-lỗi)

---

## 1. Tổng Quan Luồng Dữ Liệu

Hệ thống FinoraRetail sử dụng kiến trúc phân lớp MVC (Model-View-Controller) với luồng dữ liệu đi qua các tầng theo thứ tự:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              TRÌNH DUYỆT (Browser)                                │
│  ┌─────────────┐    HTTP Request    ┌─────────────┐    HTTP Response             │
│  │   HTML Form │ ─────────────────► │   Servlet   │ ──────────────────────►       │
│  │   / JSP     │ ◄───────────────── │  Controller │ ◄──────────────────────       │
│  └─────────────┘    Rendered HTML   └──────┬──────┘    with Data                 │
└────────────────────────────────────────────┼─────────────────────────────────────┘
                                             │
                                             ▼
                              ┌────────────────────────────┐
                              │      SERVICE LAYER          │
                              │    (Business Logic)        │
                              │    Tùy chọn, hiện tại      │
                              │    chủ yếu xử lý trong     │
                              │    Controller               │
                              └────────────┬───────────────┘
                                           │
                                           ▼
                              ┌────────────────────────────┐
                              │         DAO LAYER          │
                              │  (Data Access Object)       │
                              │  - PreparedStatement        │
                              │  - SQL Execution            │
                              │  - ResultSet Mapping        │
                              └────────────┬───────────────┘
                                           │
                                           ▼
                              ┌────────────────────────────┐
                              │      DATABASE              │
                              │   SQL Server DBFinoraV3    │
                              │   - 21 Tables              │
                              │   - Foreign Keys           │
                              │   - Constraints            │
                              └────────────────────────────┘
```

### Các Thành Phần Chính

| Tầng | Thành phần | Trách nhiệm |
|------|-----------|-------------|
| **View** | JSP + JSTL | Hiển thị dữ liệu, nhận input từ người dùng |
| **Controller** | Servlet | Xử lý request, validation, điều hướng |
| **Service** | Service Class | Business logic (đang phát triển) |
| **DAO** | DAO Class | Truy xuất database, SQL execution |
| **Model** | Java Bean | Đối tượng dữ liệu (DTO) |

---

## 2. Luồng Nhập Dữ Liệu (Data Entry Flow)

Luồng nhập dữ liệu bắt đầu từ form trên trình duyệt, đi qua Servlet, DAO và kết thúc tại Database.

### 2.1. Sơ Đồ Luồng POST

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              LUỒNG NHẬP DỮ LIỆU (POST)                               │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  1. Người dùng nhấn nút Submit trên form                                             │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  <form action="CategoryServlet" method="POST">                          │     │
│     │    <input name="categoryName" value="Đồ uống">                           │     │
│     │    <input name="description" value="Các loại nước uống">                 │     │
│     │    <button type="submit">Thêm mới</button>                               │     │
│     │  </form>                                                                   │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ HTTP POST Request                        │
│  2. HTTP POST Request gửi đến Servlet                                                  │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  POST /Finora/CategoryServlet                                           │     │
│     │  Parameters:                                                             │     │
│     │    action=create                                                        │     │
│     │    categoryName=Đồ uống                                                   │     │
│     │    description=Các loại nước uống                                        │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ doPost() được gọi                        │
│  3. Servlet xử lý POST request                                                         │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  @Override                                                               │     │
│     │  protected void doPost(HttpServletRequest request,                       │     │
│     │                        HttpServletResponse response)                      │     │
│     │      throws ServletException, IOException {                              │     │
│     │                                                                              │     │
│     │      // 3.1. Extract parameters                                                   │     │
│     │      String categoryName = request.getParameter("categoryName");          │     │
│     │      String description = request.getParameter("description");            │     │
│     │                                                                              │     │
│     │      // 3.2. Validate input                                                       │     │
│     │      if (categoryName == null || categoryName.trim().isEmpty()) {        │     │
│     │          request.getSession().setAttribute("error", "Tên danh mục trống");│     │
│     │          response.sendRedirect("CategoryServlet?action=list");            │     │
│     │          return;                                                          │     │
│     │      }                                                                   │     │
│     │                                                                              │     │
│     │      // 3.3. Build model object                                              │     │
│     │      Category category = new Category();                                  │     │
│     │      category.setCategoryName(categoryName.trim());                      │     │
│     │      category.setDescription(description);                                │     │
│     │      category.setStatus("active");                                        │     │
│     │                                                                              │     │
│     │      // 3.4. Call DAO to insert                                             │     │
│     │      CategoryDAO dao = new CategoryDAO();                                 │     │
│     │      boolean success = dao.insert(category);                              │     │
│     │                                                                              │     │
│     │      // 3.5. Set flash message                                              │     │
│     │      if (success) {                                                       │     │
│     │          request.getSession().setAttribute("message", "Thêm thành công!");│     │
│     │      } else {                                                             │     │
│     │          request.getSession().setAttribute("error", "Thêm thất bại!");    │     │
│     │      }                                                                    │     │
│     │                                                                              │     │
│     │      // 3.6. POST-Redirect-GET pattern                                       │     │
│     │      response.sendRedirect("CategoryServlet?action=list");               │     │
│     │  }                                                                         │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ DAO.insert()                             │
│  4. DAO thực thi SQL                                                                          │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  public boolean insert(Category category) {                            │     │
│     │      String sql = "INSERT INTO category (category_name, description,    │     │
│     │                         status) VALUES (?, ?, ?)";                     │     │
│     │                                                                              │     │
│     │      try (Connection conn = DBContext.getConnection();                    │     │
│     │           PreparedStatement ps = conn.prepareStatement(sql)) {         │     │
│     │                                                                              │     │
│     │          // 4.1. Set parameters with PreparedStatement                       │     │
│     │          ps.setString(1, category.getCategoryName());                    │     │
│     │          ps.setString(2, category.getDescription());                     │     │
│     │          ps.setString(3, category.getStatus());                          │     │
│     │                                                                              │     │
│     │          // 4.2. Execute update                                                │     │
│     │          int rows = ps.executeUpdate();                                   │     │
│     │          return rows > 0;                                                 │     │
│     │                                                                              │     │
│     │      } catch (SQLException e) {                                           │     │
│     │          e.printStackTrace();                                             │     │
│     │          return false;                                                    │     │
│     │      }                                                                    │     │
│     │  }                                                                         │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ INSERT statement                         │
│  5. SQL Server thực thi INSERT query                                                    │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  INSERT INTO category (category_name, description, status)              │     │
│     │  VALUES (N'Đồ uống', N'Các loại nước uống', N'active');                  │     │
│     │                                                                              │     │
│     │  -- Result: 1 row inserted, category_id generated (e.g., 5)              │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ HTTP 302 Redirect                        │
│  6. Trình duyệt chuyển hướng đến trang danh sách                                        │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  GET /Finora/CategoryServlet?action=list                                │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ JSTL hiển thị message                   │
│  7. JSP hiển thị kết quả với flash message                                              │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  <c:if test="${not empty message}">                                    │     │
│     │    <div class="alert alert-success">${message}</div>                    │     │
│     │  </c:if>                                                                │     │
│     │  <c:remove var="message" scope="session"/>                              │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2. Chi Tiết Từng Bước

#### Bước 1: Người dùng Submit Form

Người dùng điền thông tin vào form HTML và nhấn nút Submit. Form gửi HTTP POST request với các tham số được encode theo content-type của form.

```html
<!-- Ví dụ form tạo danh mục -->
<form action="CategoryServlet" method="POST">
    <input type="hidden" name="action" value="create">
    <div class="mb-3">
        <label for="categoryName" class="form-label">Tên danh mục</label>
        <input type="text" class="form-control" id="categoryName" 
               name="categoryName" required maxlength="150">
    </div>
    <div class="mb-3">
        <label for="description" class="form-label">Mô tả</label>
        <textarea class="form-control" id="description" 
                  name="description" maxlength="255"></textarea>
    </div>
    <button type="submit" class="btn btn-primary">Thêm mới</button>
</form>
```

#### Bước 2: Servlet Nhận Request

Servlet nhận HTTP POST request vào method `doPost()`. Đường dẫn URL xác định Servlet nào xử lý dựa trên cấu hình trong `web.xml`.

```java
@WebServlet("/CategoryServlet")
public class CategoryServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, 
                          HttpServletResponse response)
            throws ServletException, IOException {
        
        // Lấy action từ parameter hoặc path info
        String action = request.getParameter("action");
        
        switch (action) {
            case "create":
                doCreate(request, response);
                break;
            case "update":
                doUpdate(request, response);
                break;
            case "delete":
                doDelete(request, response);
                break;
            default:
                response.sendRedirect("CategoryServlet?action=list");
        }
    }
}
```

#### Bước 3: Servlet Validate Input

Validation được thực hiện tại Servlet để đảm bảo dữ liệu hợp lệ trước khi gọi DAO.

```java
private void doCreate(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    
    // Lấy và trim parameters
    String categoryName = request.getParameter("categoryName");
    String description = request.getParameter("description");
    
    // Validation
    List<String> errors = new ArrayList<>();
    
    if (categoryName == null || categoryName.trim().isEmpty()) {
        errors.add("Tên danh mục không được để trống");
    } else if (categoryName.trim().length() > 150) {
        errors.add("Tên danh mục không được vượt quá 150 ký tự");
    }
    
    if (description != null && description.length() > 255) {
        errors.add("Mô tả không được vượt quá 255 ký tự");
    }
    
    // Nếu có lỗi, quay lại trang với thông báo
    if (!errors.isEmpty()) {
        request.getSession().setAttribute("errors", errors);
        response.sendRedirect("CategoryServlet?action=new");
        return;
    }
    
    // Tiếp tục xử lý...
}
```

#### Bước 4: Servlet Gọi DAO

Servlet tạo đối tượng Model và gọi DAO để thực hiện thao tác database.

```java
// Tạo đối tượng Category
Category category = new Category();
category.setCategoryName(categoryName.trim());
category.setDescription(description != null ? description.trim() : null);
category.setStatus("active");

// Gọi DAO
CategoryDAO dao = new CategoryDAO();
boolean success = dao.insert(category);

// Xử lý kết quả
if (success) {
    request.getSession().setAttribute("message", "Thêm danh mục thành công!");
} else {
    request.getSession().setAttribute("error", "Thêm danh mục thất bại!");
}
```

#### Bước 5: DAO Thực Thi SQL

DAO sử dụng `PreparedStatement` để thực thi SQL với parameters được bind an toàn.

```java
public class CategoryDAO {
    
    public boolean insert(Category category) {
        String sql = "INSERT INTO category (category_name, description, status, " +
                     "created_at) VALUES (?, ?, ?, GETDATE())";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Bind parameters (sử dụng index-based)
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getStatus());
            
            // Execute và lấy kết quả
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // Log error
            e.printStackTrace();
            return false;
        }
    }
}
```

#### Bước 6: Database Thực Thi

SQL Server nhận và thực thi câu lệnh SQL:

```sql
-- Câu lệnh thực tế được gửi đến SQL Server
INSERT INTO category (category_name, description, status, created_at) 
VALUES (N'Đồ uống', N'Các loại nước uống', N'active', GETDATE())

-- Kết quả: 1 row affected
-- category_id mới được sinh ra: SCOPE_IDENTITY() hoặc OUTPUT clause
```

#### Bước 7: POST-Redirect-GET Pattern

Sau khi xử lý thành công, Servlet redirect về trang danh sách thay vì forward trực tiếp. Điều này ngăn chặn việc submit form trùng lặp khi người dùng refresh trang.

```java
// POST-Redirect-GET Pattern
response.sendRedirect(request.getContextPath() + "/CategoryServlet?action=list");

// KHÔNG NÊN dùng forward vì sẽ gây duplicate submission:
// request.getRequestDispatcher("/CategoryServlet?action=list").forward(request, response);
```

---

## 3. Luồng Truy Xuất Dữ Liệu (Data Retrieval Flow)

Luồng truy xuất dữ liệu cho phép người dùng xem danh sách, chi tiết hoặc tìm kiếm dữ liệu từ database.

### 3.1. Sơ Đồ Luồng GET

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           LUỒNG TRUY XUẤT DỮ LIỆU (GET)                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  1. Người dùng truy cập URL                                                          │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  GET /Finora/CategoryServlet?action=list                               │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ HTTP GET Request                        │
│  2. Servlet nhận request                                                                  │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  @Override                                                               │     │
│     │  protected void doGet(HttpServletRequest request,                        │     │
│     │                        HttpServletResponse response)                     │     │
│     │      throws ServletException, IOException {                               │     │
│     │                                                                              │     │
│     │      String action = request.getParameter("action");                     │     │
│     │                                                                              │     │
│     │      switch (action) {                                                   │     │
│     │          case "list":                                                     │     │
│     │              doList(request, response);                                   │     │
│     │              break;                                                       │     │
│     │          case "detail":                                                   │     │
│     │              doDetail(request, response);                                 │     │
│     │              break;                                                      │     │
│     │          default:                                                        │     │
│     │              doList(request, response);                                   │     │
│     │      }                                                                    │     │
│     │  }                                                                         │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ doList()                               │
│  3. Servlet xử lý truy vấn danh sách                                                │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  private void doList(HttpServletRequest request,                         │     │
│     │                        HttpServletResponse response)                      │     │
│     │      throws ServletException, IOException {                               │     │
│     │                                                                              │     │
│     │      // 3.1. Extract query parameters                                             │     │
│     │      String search = request.getParameter("search");                      │     │
│     │      String status = request.getParameter("status");                     │     │
│     │      String pageParam = request.getParameter("page");                    │     │
│     │      int page = (pageParam != null) ? Integer.parseInt(pageParam) : 1;    │     │
│     │                                                                              │     │
│     │      // 3.2. Build filter object                                                 │     │
│     │      CategoryFilter filter = new CategoryFilter();                        │     │
│     │      filter.setSearchTerm(search);                                        │     │
│     │      filter.setStatus(status);                                            │     │
│     │      filter.setPage(page);                                                │     │
│     │      filter.setPageSize(10);                                             │     │
│     │                                                                              │     │
│     │      // 3.3. Call DAO                                                           │     │
│     │      CategoryDAO dao = new CategoryDAO();                                 │     │
│     │      List<Category> categories = dao.search(filter);                     │     │
│     │      int totalRecords = dao.count(filter);                                │     │
│     │                                                                              │     │
│     │      // 3.4. Set request attributes                                              │     │
│     │      request.setAttribute("categories", categories);                       │     │
│     │      request.setAttribute("currentPage", page);                           │     │
│     │      request.setAttribute("totalPages",                                  │     │
│     │          (int) Math.ceil((double) totalRecords / filter.getPageSize()));   │     │
│     │      request.setAttribute("search", search);                              │     │
│     │      request.setAttribute("status", status);                             │     │
│     │                                                                              │     │
│     │      // 3.5. Forward to JSP                                                       │     │
│     │      request.getRequestDispatcher("/views/categories/list.jsp")         │     │
│     │          .forward(request, response);                                       │     │
│     │  }                                                                         │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ dao.search(filter)                    │
│  4. DAO thực thi SELECT query                                                       │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  public List<Category> search(CategoryFilter filter) {                   │     │
│     │      StringBuilder sql = new StringBuilder();                           │     │
│     │      sql.append("SELECT * FROM category WHERE 1=1");                     │     │
│     │                                                                              │     │
│     │      // 4.1. Dynamic WHERE clause                                                │     │
│     │      if (filter.getSearchTerm() != null && !filter.getSearchTerm()      │     │
│     │          .isEmpty()) {                                                   │     │
│     │          sql.append(" AND category_name LIKE ?");                         │     │
│     │      }                                                                   │     │
│     │      if (filter.getStatus() != null && !filter.getStatus().isEmpty()) { │     │
│     │          sql.append(" AND status = ?");                                   │     │
│     │      }                                                                   │     │
│     │                                                                              │     │
│     │      sql.append(" ORDER BY category_id DESC");                           │     │
│     │      sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");               │     │
│     │                                                                              │     │
│     │      try (Connection conn = DBContext.getConnection();                    │     │
│     │           PreparedStatement ps = conn.prepareStatement(sql.toString())) │     │
│     │                                                                              │     │
│     │          // 4.2. Set parameters                                                   │     │
│     │          int paramIndex = 1;                                              │     │
│     │          if (filter.getSearchTerm() != null) {                           │     │
│     │              ps.setString(paramIndex++, "%" + filter.getSearchTerm()      │     │
│     │                  + "%");                                                  │     │
│     │          }                                                                │     │
│     │          if (filter.getStatus() != null) {                               │     │
│     │              ps.setString(paramIndex++, filter.getStatus());            │     │
│     │          }                                                                │     │
│     │          ps.setInt(paramIndex++, (filter.getPage() - 1) * filter          │     │
│     │              .getPageSize());                                            │     │
│     │          ps.setInt(paramIndex, filter.getPageSize());                    │     │
│     │                                                                              │     │
│     │          // 4.3. Execute query                                                  │     │
│     │          ResultSet rs = ps.executeQuery();                                │     │
│     │                                                                              │     │
│     │          // 4.4. Map ResultSet to List<Category>                                 │     │
│     │          List<Category> categories = new ArrayList<>();                   │     │
│     │          while (rs.next()) {                                              │     │
│     │              Category c = mapResultSetToCategory(rs);                    │     │
│     │              categories.add(c);                                          │     │
│     │          }                                                                │     │
│     │          return categories;                                               │     │
│     │      } catch (SQLException e) {                                           │     │
│     │          e.printStackTrace();                                             │     │
│     │          return Collections.emptyList();                                  │     │
│     │      }                                                                    │     │
│     │  }                                                                         │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ ResultSet returned                      │
│  5. Servlet nhận kết quả và forward đến JSP                                          │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  // categories: List<Category> với dữ liệu từ database                 │     │
│     │  // currentPage, totalPages: cho pagination                            │     │
│     │  request.getRequestDispatcher("/views/categories/list.jsp")            │     │
│     │      .forward(request, response);                                        │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ JSP renders HTML                        │
│  6. JSP sử dụng JSTL để hiển thị dữ liệu                                              │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  <!-- Search Bar -->                                                    │     │
│     │  <form action="CategoryServlet" method="GET">                           │     │
│     │    <input type="hidden" name="action" value="list">                     │     │
│     │    <input type="text" name="search" value="${search}"                   │     │
│     │           placeholder="Tìm kiếm danh mục...">                          │     │
│     │    <button type="submit">Tìm kiếm</button>                              │     │
│     │  </form>                                                                 │     │
│     │                                                                              │     │
│     │  <!-- Data Table -->                                                     │     │
│     │  <table class="table">                                                   │     │
│     │    <thead>                                                               │     │
│     │      <tr>                                                                │     │
│     │        <th>ID</th>                                                       │     │
│     │        <th>Tên danh mục</th>                                             │     │
│     │        <th>Mô tả</th>                                                    │     │
│     │        <th>Trạng thái</th>                                               │     │
│     │        <th>Thao tác</th>                                                 │     │
│     │      </tr>                                                               │     │
│     │    </thead>                                                              │     │
│     │    <tbody>                                                               │     │
│     │      <c:forEach var="cat" items="${categories}">                         │     │
│     │        <tr>                                                              │     │
│     │          <td>${cat.categoryId}</td>                                      │     │
│     │          <td>${cat.categoryName}</td>                                    │     │
│     │          <td>${cat.description}</td>                                      │     │
│     │          <td>                                                            │     │
│     │            <span class="badge bg-${cat.status == 'active' ?            │     │
│     │                'success' : 'secondary'}">                               │     │
│     │              ${cat.status}</span>                                        │     │
│     │          </td>                                                          │     │
│     │          <td>                                                            │     │
│     │            <a href="CategoryServlet?action=edit&id=${cat.categoryId}"   │     │
│     │               class="btn btn-sm btn-primary">Sửa</a>                      │     │
│     │            <a href="CategoryServlet?action=delete&id=${cat.categoryId}" │     │
│     │               class="btn btn-sm btn-danger"                              │     │
│     │               onclick="return confirm('Xóa?')">Xóa</a>                 │     │
│     │          </td>                                                           │     │
│     │        </tr>                                                             │     │
│     │      </c:forEach>                                                        │     │
│     │    </tbody>                                                              │     │
│     │  </table>                                                                 │     │
│     │                                                                              │     │
│     │  <!-- Pagination -->                                                      │     │
│     │  <c:if test="${totalPages > 1}">                                        │     │
│     │    <nav>                                                                 │     │
│     │      <ul class="pagination">                                             │     │
│     │        <c:forEach begin="1" end="${totalPages}" var="i">                 │     │
│     │          <li class="page-item ${i == currentPage ? 'active' : ''}">     │     │
│     │            <a class="page-link"                                          │     │
│     │               href="CategoryServlet?action=list&page=${i}&search=${search}&status=${status}">  │
│     │              ${i}</a>                                                     │     │
│     │          </li>                                                           │     │
│     │        </c:forEach>                                                      │     │
│     │      </ul>                                                               │     │
│     │    </nav>                                                                 │     │
│     │  </c:if>                                                                 │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼ Browser renders HTML                    │
│  7. Trình duyệt hiển thị trang web hoàn chỉnh                                          │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 3.2. Mẫu Hàm Mapping ResultSet

```java
private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
    Category category = new Category();
    category.setCategoryId(rs.getInt("category_id"));
    category.setCategoryName(rs.getString("category_name"));
    category.setDescription(rs.getString("description"));
    
    // Xử lý parent_category_id có thể là NULL
    int parentId = rs.getInt("parent_category_id");
    category.setParentCategoryId(rs.wasNull() ? null : parentId);
    
    category.setStatus(rs.getString("status"));
    category.setCreatedAt(rs.getTimestamp("created_at"));
    category.setUpdateAt(rs.getTimestamp("update_at"));
    
    return category;
}
```

---

## 4. Luồng Dữ Liệu Theo Chức Năng

### 4.1. Luồng Đăng Ký Khách Hàng

**Mô tả:** Khách hàng đăng ký tài khoản mới, hệ thống tự động tạo bản ghi điểm thưởng.

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                          LUỒNG ĐĂNG KÝ KHÁCH HÀNG                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  Form ──POST──► CustomerServlet?action=register                                      │
│                    │                                                                 │
│                    ▼                                                                 │
│              Validate input                                                          │
│                    │                                                                 │
│                    ▼                                                                 │
│         ┌──────────────────┐                                                        │
│         │ CustomerDAO       │                                                        │
│         │ .insert(customer) │                                                        │
│         └────────┬─────────┘                                                        │
│                  │ INSERT vào bảng customer                                         │
│                  │ Lấy customer_id vừa sinh                                         │
│                  ▼                                                                   │
│         ┌──────────────────┐                                                        │
│         │ CustomerPointDAO  │                                                        │
│         │ .insert(points)   │                                                        │
│         └────────┬─────────┘                                                        │
│                  │ INSERT vào bảng customer_point với:                              │
│                  │ - cus_id = customer_id vừa lấy                                  │
│                  │ - current_points = 0                                             │
│                  │ - level_name = 'Bronze'                                          │
│                  ▼                                                                   │
│         ┌──────────────────┐                                                        │
│         │ Session + Redirect│                                                        │
│         └────────┬─────────┘                                                        │
│                  │                                                                 │
│                  ▼ GET trang danh sách khách hàng                                    │
│              list.jsp hiển thị thông báo thành công                                 │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

**Chi tiết các bảng liên quan:**

| Bước | Bảng | Thao tác | Dữ liệu |
|------|------|----------|----------|
| 1 | `customer` | INSERT | full_name, gender, bod, address, email, phone, cus_type |
| 2 | `customer_point` | INSERT | cus_id (FK), current_points=0, level_name='Bronze' |

**Code mẫu:**

```java
@WebServlet("/CustomerServlet")
public class CustomerServlet extends HttpServlet {
    
    private CustomerDAO customerDAO = new CustomerDAO();
    private CustomerPointDAO pointDAO = new CustomerPointDAO();
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("register".equals(action)) {
            doRegister(request, response);
        }
    }
    
    private void doRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Extract parameters
        Customer customer = new Customer();
        customer.setFullName(request.getParameter("fullName"));
        customer.setGender(request.getParameter("gender"));
        customer.setBod(request.getParameter("bod"));
        customer.setEmail(request.getParameter("email"));
        customer.setPhone(request.getParameter("phone"));
        customer.setCusType("RETAIL");
        
        // 2. Insert customer
        int cusId = customerDAO.insertReturnId(customer);
        
        if (cusId > 0) {
            // 3. Auto-create customer point record
            CustomerPoint points = new CustomerPoint();
            points.setCusId(cusId);
            points.setCurrentPoints(0);
            points.setLevelName("Bronze");
            pointDAO.insert(points);
            
            request.getSession().setAttribute("message", 
                "Đăng ký thành công! Mã khách hàng: " + cusId);
        } else {
            request.getSession().setAttribute("error", 
                "Đăng ký thất bại!");
        }
        
        response.sendRedirect("CustomerServlet?action=list");
    }
}
```

---

### 4.2. Luồng Quản Lý Danh Mục

**Mô tả:** Người quản lý thêm, sửa, xóa và xem danh mục sản phẩm.

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              LUỒNG QUẢN LÝ DANH MỤC                                  │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  ╔═══════════════════════════════════════════════════════════════════════════════╗ │
│  ║                                    GET REQUESTS                                   ║ │
│  ╠═══════════════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                                   ║ │
│  ║  list.jsp ──GET──► CategoryServlet?action=list                                  ║ │
│  ║                    │                                                              ║ │
│  ║                    ▼                                                              ║ │
│  ║              CategoryDAO                                                          ║ │
│  ║              .search(filter) ──SELECT──► category table                          ║ │
│  ║                    │                                                              ║ │
│  ║                    ▼                                                              ║ │
│  ║              request.setAttribute("categories", list)                           ║ │
│  ║                    │                                                              ║ │
│  ║                    ▼                                                              ║ │
│  ║              forward to list.jsp                                                 ║ │
│  ║                                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════════════════╝ │
│                                                                                      │
│  ╔═══════════════════════════════════════════════════════════════════════════════╗ │
│  ║                                    POST REQUESTS                                  ║ │
│  ╠═══════════════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                                   ║ │
│  ║  ┌─────────────────────────────────────────────────────────────────────────┐    ║ │
│  ║  │ action=create                                                           │    ║ │
│  ║  │   CategoryDAO.insert(category) ──INSERT──► category table               │    ║ │
│  ║  └─────────────────────────────────────────────────────────────────────────┘    ║ │
│  ║                                    │                                             ║ │
│  ║  ┌─────────────────────────────────────────────────────────────────────────┐    ║ │
│  ║  │ action=update                                                           │    ║ │
│  ║  │   CategoryDAO.update(category) ──UPDATE──► category table               │    ║ │
│  ║  └─────────────────────────────────────────────────────────────────────────┘    ║ │
│  ║                                    │                                             ║ │
│  ║  ┌─────────────────────────────────────────────────────────────────────────┐    ║ │
│  ║  │ action=delete                                                           │    ║ │
│  ║  │   CategoryDAO.delete(id) ──UPDATE──► category SET status='inactive'     │    ║ │
│  ║  │   (Soft delete - không xóa vật lý)                                      │    ║ │
│  ║  └─────────────────────────────────────────────────────────────────────────┘    ║ │
│  ║                                    │                                             ║ │
│  ║                                    ▼                                             ║ │
│  ║                          POST-Redirect-GET                                      ║ │
│  ║                                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════════════════╝ │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

**Chi tiết các bảng liên quan:**

| Bước | Bảng | Thao tác | Ghi chú |
|------|------|----------|----------|
| 1 | `category` | SELECT | Với điều kiện search, filter status, phân trang |
| 2 | `category` | INSERT | Thêm danh mục mới |
| 3 | `category` | UPDATE | Cập nhật thông tin danh mục |
| 4 | `category` | UPDATE status='inactive' | Xóa mềm, không xóa vật lý |

**Tính năng đặc biệt - Cấu trúc phân cấp:**

```java
// Sử dụng CTE (Common Table Expression) để lấy cây danh mục
public List<Category> getCategoryTree() {
    String sql = """
        WITH CategoryTree AS (
            -- Root categories
            SELECT category_id, category_name, parent_category_id, 
                   0 as level, CAST(category_name AS NVARCHAR(500)) as path
            FROM category
            WHERE parent_category_id IS NULL AND status = 'active'
            
            UNION ALL
            
            -- Child categories
            SELECT c.category_id, c.category_name, c.parent_category_id,
                   ct.level + 1, CAST(ct.path + ' > ' + c.category_name AS NVARCHAR(500))
            FROM category c
            INNER JOIN CategoryTree ct ON c.parent_category_id = ct.category_id
            WHERE c.status = 'active'
        )
        SELECT * FROM CategoryTree ORDER BY path
        """;
    // Execute and map results...
}
```

---

### 4.3. Luồng Tạo Đơn Hàng

**Mô tả:** Nhân viên tạo đơn hàng bán hàng cho khách hàng, bao gồm nhiều sản phẩm.

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                 LUỒNG TẠO ĐƠN HÀNG                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  ╔═══════════════════════════════════════════════════════════════════════════════╗ │
│  ║                              BƯỚC 1: TẠO ĐƠN HÀNG                                ║ │
│  ╠═══════════════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                                   ║ │
│  ║  order.jsp ──POST──► OrderServlet?action=create                                  ║ │
│  ║                    │                                                              ║ │
│  ║                    ▼                                                              ║ │
│  ║              Validate và build Order object                                      ║ │
│  ║                    │                                                              ║ │
│  ║                    ▼                                                              ║ │
│  ║         ┌─────────────────────┐                                                   ║ │
│  ║         │     OrderDAO        │                                                   ║ │
│  ║         │ .insertReturnId()   │                                                   ║ │
│  ║         └──────────┬──────────┘                                                   ║ │
│  ║                    │ INSERT vào [order], trả về order_id                            ║ │
│  ║                    ▼                                                               ║ │
│  ║         ┌─────────────────────┐                                                   ║ │
│  ║         │  OrderDetailDAO     │                                                   ║ │
│  ║         │  .batchInsert()     │                                                   ║ │
│  ║         └──────────┬──────────┘                                                   ║ │
│  ║                    │ INSERT nhiều dòng vào order_detail                           ║ │
│  ║                    │ cho mỗi sản phẩm trong đơn hàng                               ║ │
│  ║                    ▼                                                               ║ │
│  ╚═══════════════════════════════════════════════════════════════════════════════╝ │
│                                                                                      │
│  ╔═══════════════════════════════════════════════════════════════════════════════╗ │
│  ║                         BƯỚC 2: CẬP NHẬT TỒN KHO                                ║ │
│  ╠═══════════════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                                   ║ │
│  ║         ┌─────────────────────┐                                                   ║ │
│  ║         │ InventoryDAO        │                                                   ║ │
│  ║         │ .decrementStock()   │                                                   ║ │
│  ║         └──────────┬──────────┘                                                   ║ │
│  ║                    │ UPDATE inventory SET quantity_in_stock -= qty               ║ │
│  ║                    │ tại warehouse_id của đơn hàng                               ║ │
│  ║                    ▼                                                               ║ │
│  ║         ┌─────────────────────┐                                                   ║ │
│  ║         │ StockTransactionDAO │                                                   ║ │
│  ║         │ .insert()           │                                                   ║ │
│  ║         └──────────┬──────────┘                                                   ║ │
│  ║                    │ INSERT vào stock_transaction (type='OUT')                    ║ │
│  ║                    ▼                                                               ║ │
│  ╚═══════════════════════════════════════════════════════════════════════════════╝ │
│                                                                                      │
│  ╔═══════════════════════════════════════════════════════════════════════════════╗ │
│  ║                         BƯỚC 3: TÍCH ĐIỂM (nếu có)                               ║ │
│  ╠═══════════════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                                   ║ │
│  ║         ┌─────────────────────┐                                                   ║ │
│  ║         │ CustomerPointDAO    │                                                   ║ │
│  ║         │ .addPoints()        │                                                   ║ │
│  ║         └──────────┬──────────┘                                                   ║ │
│  ║                    │ UPDATE customer_point                                        ║ │
│  ║                    │ SET current_points += earned                                 ║ │
│  ║                    ▼                                                               ║ │
│  ║         ┌─────────────────────┐                                                   ║ │
│  ║         │ PointTransactionDAO │                                                   ║ │
│  ║         │ .insert()           │                                                   ║ │
│  ║         └──────────┬──────────┘                                                   ║ │
│  ║                    │ INSERT vào point_transaction để ghi nhận                    ║ │
│  ║                    ▼                                                               ║ │
│  ╚═══════════════════════════════════════════════════════════════════════════════╝ │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

**Chi tiết các bảng liên quan:**

| Bước | Bảng | Thao tác | Dữ liệu |
|------|------|----------|----------|
| 1 | `[order]` | INSERT | order_code, order_type='SALE', customer_id, branch_id, emp_id, warehouse_id, subtotal, discount_amount, total_amount |
| 2 | `order_detail` | INSERT (nhiều dòng) | order_id, product_id, quantity, unit_price, total_price |
| 3 | `inventory` | UPDATE | quantity_in_stock -= quantity (tại warehouse_id) |
| 4 | `stock_transaction` | INSERT | warehouse_id, product_id, 'ORDER', order_id, 'OUT', quantity |
| 5 | `customer_point` | UPDATE | current_points += earned |
| 6 | `point_transaction` | INSERT | cus_point_id, order_id, before_points, after_points, description |


---

### 4.4. Luồng Thanh Toán

**Mô tả:** Xử lý thanh toán cho đơn hàng với nhiều phương thức.

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                               LUỒNG THANH TOÁN                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  order.jsp ──POST──► PaymentServlet?action=process                                    │
│                    │                                                                 │
│                    ▼                                                                 │
│              Validate payment info                                                   │
│                    │                                                                 │
│                    ▼                                                                 │
│         ┌─────────────────────┐                                                      │
│         │ PaymentDAO          │                                                      │
│         │ .insert()           │                                                      │
│         └──────────┬──────────┘                                                      │
│                    │ INSERT vào payment table                                        │
│                    │ Lấy payment_id                                                  │
│                    ▼                                                                  │
│         ┌─────────────────────┐                                                      │
│         │ OrderDAO            │                                                      │
│         │ .updateStatus()     │                                                      │
│         └──────────┬──────────┘                                                      │
│                    │ UPDATE [order] SET status='COMPLETED'                           │
│                    ▼                                                                  │
│         ┌─────────────────────┐                                                      │
│         │ (Optional)          │                                                      │
│         │ Integration với     │                                                      │
│         │ cổng thanh toán     │                                                      │
│         │ (VNPAY, MOMO...)    │                                                      │
│         └──────────┬──────────┘                                                      │
│                    │                                                                 │
│                    ▼                                                                 │
│         ┌─────────────────────┐                                                      │
│         │ Session + Redirect   │                                                      │
│         │ to order detail     │                                                      │
│         └─────────────────────┘                                                      │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

**Chi tiết các bảng liên quan:**

| Bước | Bảng | Thao tác | Dữ liệu |
|------|------|----------|----------|
| 1 | `payment` | INSERT | order_id, payment_method, payment_amount, payment_date, payment_status, transaction_code |
| 2 | `[order]` | UPDATE | status = 'COMPLETED' (nếu đã thanh toán đủ) |

**Code mẫu:**

```java
@WebServlet("/PaymentServlet")
public class PaymentServlet extends HttpServlet {
    
    private PaymentDAO paymentDAO = new PaymentDAO();
    private OrderDAO orderDAO = new OrderDAO();
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("process".equals(action)) {
            doProcessPayment(request, response);
        }
    }
    
    private void doProcessPayment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        String paymentMethod = request.getParameter("paymentMethod");
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));
        
        // Lấy order để kiểm tra
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            request.getSession().setAttribute("error", "Đơn hàng không tồn tại!");
            response.sendRedirect("OrderServlet?action=list");
            return;
        }
        
        // Tạo bản ghi thanh toán
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentAmount(amount);
        payment.setPaymentDate(new Date());
        payment.setPaymentStatus("COMPLETED");
        
        // Xử lý theo phương thức thanh toán
        if ("VNPAY".equals(paymentMethod)) {
            // Gọi API VNPAY, lấy transaction code
            String vnpTxnRef = generateVNPayTxnRef();
            payment.setTransactionCode(vnpTxnRef);
        } else if ("MOMO".equals(paymentMethod)) {
            // Gọi API MOMO, lấy transaction code
            String momoTransId = generateMoMoTxnRef();
            payment.setTransactionCode(momoTransId);
        } else {
            // Tiền mặt hoặc thẻ - không cần mã giao dịch bên thứ 3
            payment.setTransactionCode("CASH-" + System.currentTimeMillis());
        }
        
        // Lưu payment
        int paymentId = paymentDAO.insertReturnId(payment);
        
        // Kiểm tra đã thanh toán đủ chưa
        BigDecimal totalPaid = paymentDAO.getTotalPaid(orderId);
        if (totalPaid.compareTo(order.getTotalAmount()) >= 0) {
            orderDAO.updateStatus(orderId, "COMPLETED");
        } else {
            orderDAO.updateStatus(orderId, "PARTIALLY_PAID");
        }
        
        request.getSession().setAttribute("message", 
            "Thanh toán thành công! Mã giao dịch: " + payment.getTransactionCode());
        response.sendRedirect("OrderServlet?action=detail&id=" + orderId);
    }
}
```

---

### 4.5. Luồng Cập Nhật Tồn Kho

**Mô tả:** Xử lý các giao dịch tồn kho: nhập kho, xuất kho, điều chỉnh.

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                             LUỒNG CẬP NHẬT TỒN KHO                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  ╔═══════════════════════════════════════════════════════════════════════════════╗ │
│  ║                               NHẬP KHO (PURCHASE)                               ║ │
│  ╠═══════════════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                                   ║ │
│  ║  purchase.jsp ──POST──► PurchaseServlet?action=create                           ║ │
│  ║                      │                                                            ║ │
│  ║                      ▼                                                            ║ │
│  ║              1. OrderDAO.insert(order) ──INSERT──► [order]                       ║ │
│  ║                      │                                                            ║ │
│  ║                      ▼                                                            ║ │
│  ║              2. OrderDetailDAO.batchInsert() ──INSERT──► order_detail            ║ │
│  ║                      │                                                            ║ │
│  ║                      ▼                                                            ║ │
│  ║              3. InventoryDAO.increment() ──UPDATE──► inventory                  ║ │
│  ║                      │ (quantity_in_stock += qty)                               ║ │
│  ║                      ▼                                                            ║ │
│  ║              4. StockTransactionDAO.insert() ──INSERT──► stock_transaction    ║ │
│  ║                      │ (type='IN', before/after quantity)                        ║ │
│  ║                                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════════════════╝ │
│                                                                                      │
│  ╔═══════════════════════════════════════════════════════════════════════════════╗ │
│  ║                            XUẤT KHO (SALE ORDER)                                 ║ │
│  ╠═══════════════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                                   ║ │
│  ║  sale.jsp ──POST──► OrderServlet?action=create                                   ║ │
│  ║                    │                                                              ║ │
│  ║                    ▼                                                              ║ │
│  ║              1. OrderDAO.insert(order) ──INSERT──► [order]                       ║ │
│  ║                    │                                                              ║ │
│  ║                    ▼                                                              ║ │
│  ║              2. OrderDetailDAO.batchInsert() ──INSERT──► order_detail          ║ │
│  ║                    │                                                              ║ │
│  ║                    ▼                                                              ║ │
│  ║              3. InventoryDAO.decrement() ──UPDATE──► inventory                 ║ │
│  ║                    │ (quantity_in_stock -= qty)                                 ║ │
│  ║                    ▼                                                              ║ │
│  ║              4. StockTransactionDAO.insert() ──INSERT──► stock_transaction    ║ │
│  ║                    │ (type='OUT', before/after quantity)                         ║ │
│  ║                                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════════════════╝ │
│                                                                                      │
│  ╔═══════════════════════════════════════════════════════════════════════════════╗ │
│  ║                           CHUYỂN KHO (STOCK TRANSFER)                            ║ │
│  ╠═══════════════════════════════════════════════════════════════════════════════╣ │
│  ║                                                                                   ║ │
│  ║  transfer.jsp ──POST──► TransferServlet?action=create                            ║ │
│  ║                       │                                                           ║ │
│  ║                       ▼                                                           ║ │
│  ║              1. StockTransferDAO.insert() ──INSERT──► stock_transfer           ║ │
│  ║                       │                                                           ║ │
│  ║                       ▼                                                           ║ │
│  ║              2. StockTransferDetailDAO.batchInsert()                            ║ │
│  ║                       │ ──INSERT──► stock_transfer_detail                       ║ │
│  ║                       ▼                                                           ║ │
│  ║              3. InventoryDAO.decrement(from_warehouse)                           ║ │
│  ║                       │ (quantity_in_stock -= qty tại kho nguồn)                ║ │
│  ║                       ▼                                                           ║ │
│  ║              4. InventoryDAO.increment(to_warehouse)                           ║ │
│  ║                       │ (quantity_in_stock += qty tại kho đích)                 ║ │
│  ║                       ▼                                                           ║ │
│  ║              5. StockTransactionDAO.batchInsert()                                ║ │
│  ║                       │ ──INSERT──► stock_transaction (2 dòng: OUT + IN)       ║ │
│  ║                                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════════════════╝ │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

**Chi tiết các bảng liên quan:**

| Bước | Bảng | Thao tác | Dữ liệu |
|------|------|----------|----------|
| 1 | `[order]` | INSERT | order_type='PURCHASE', supplier_id, warehouse_id, ... |
| 2 | `order_detail` | INSERT (nhiều dòng) | order_id, product_id, quantity, unit_price |
| 3 | `inventory` | UPDATE | quantity_in_stock ± quantity |
| 4 | `stock_transaction` | INSERT | warehouse_id, product_id, 'PURCHASE', order_id, 'IN', before/after |
| 5 | `stock_transfer` | INSERT | from_warehouse_id, to_warehouse_id, transfer_code, status |
| 6 | `stock_transfer_detail` | INSERT | stock_transfer_id, product_id, quantity |

---

## 5. Mẫu Flash Message

Flash message là thông báo tạm thời được hiển thị sau khi thực hiện action và tự động xóa sau khi hiển thị.

### 5.1. Cơ Chế Hoạt Động

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                               CƠ CHẾ FLASH MESSAGE                                   │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  1. POST Request (Action)                                                           │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  POST /Finora/CategoryServlet                                           │     │
│     │  Servlet xử lý và lưu message vào session:                              │     │
│     │                                                                              │     │
│     │  request.getSession().setAttribute("message", "Thêm thành công!");     │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼                                          │
│  2. Redirect to GET Page                                                                │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  response.sendRedirect("CategoryServlet?action=list");                 │     │
│     │                                                                              │     │
│     │  Browser gửi GET request mới:                                           │     │
│     │  GET /Finora/CategoryServlet?action=list                                │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼                                          │
│  3. JSP Hiển Thị Message                                                              │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  <%@ taglib prefix="c" uri="jakarta.tags.core" %>                       │     │
│     │                                                                              │     │
│     │  <!-- Success Message -->                                                │     │
│     │  <c:if test="${not empty message}">                                      │     │
│     │    <div class="alert alert-success alert-dismissible fade show"          │     │
│     │         role="alert">                                                      │     │
│     │      ${message}                                                            │     │
│     │      <button type="button" class="btn-close" data-bs-dismiss="alert">    │     │
│     │      </button>                                                            │     │
│     │    </div>                                                                  │     │
│     │    <c:remove var="message" scope="session"/>                              │     │
│     │  </c:if>                                                                  │     │
│     │                                                                              │     │
│     │  <!-- Error Message -->                                                   │     │
│     │  <c:if test="${not empty error}">                                         │     │
│     │    <div class="alert alert-danger alert-dismissible fade show"           │     │
│     │         role="alert">                                                      │     │
│     │      ${error}                                                              │     │
│     │      <button type="button" class="btn-close" data-bs-dismiss="alert">    │     │
│     │      </button>                                                            │     │
│     │    </div>                                                                  │     │
│     │    <c:remove var="error" scope="session"/>                                │     │
│     │  </c:if>                                                                  │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                           │                                          │
│                                           ▼                                          │
│  4. Message Đã Được Xóa                                                              │
│     ┌─────────────────────────────────────────────────────────────────────────┐     │
│     │  Sau khi JSP hiển thị xong, message bị xóa khỏi session:                │     │
│     │                                                                              │     │
│     │  <c:remove var="message" scope="session"/>                               │     │
│     │                                                                              │     │
│     │  => Refresh trang sẽ KHÔNG hiển thị lại message                          │     │
│     └─────────────────────────────────────────────────────────────────────────┘     │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 5.2. Các Loại Flash Message

```java
// Thành công
request.getSession().setAttribute("message", "Thao tác thành công!");

// Lỗi
request.getSession().setAttribute("error", "Đã xảy ra lỗi!");

// Cảnh báo
request.getSession().setAttribute("warning", "Cảnh báo: Dữ liệu sắp hết hạn!");

// Thông tin
request.getSession().setAttribute("info", "Thông tin: Hệ thống sẽ bảo trì lúc 23:00.");

// Với danh sách lỗi validation
List<String> errors = new ArrayList<>();
errors.add("Tên không được trống");
errors.add("Email không hợp lệ");
request.getSession().setAttribute("errors", errors);
```

### 5.3. JSTL Template

```jsp
<%-- File: /views/common/flash-messages.jsp --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Success Alert -->
<c:if test="${not empty message}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" 
                aria-label="Close"></button>
    </div>
    <c:remove var="message" scope="session"/>
</c:if>

<!-- Error Alert -->
<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        ${error}
        <button type="button" class="btn-close" data-bs-dismiss="alert" 
                aria-label="Close"></button>
    </div>
    <c:remove var="error" scope="session"/>
</c:if>

<!-- Warning Alert -->
<c:if test="${not empty warning}">
    <div class="alert alert-warning alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-circle-fill me-2"></i>
        ${warning}
        <button type="button" class="btn-close" data-bs-dismiss="alert" 
                aria-label="Close"></button>
    </div>
    <c:remove var="warning" scope="session"/>
</c:if>

<!-- Info Alert -->
<c:if test="${not empty info}">
    <div class="alert alert-info alert-dismissible fade show" role="alert">
        <i class="bi bi-info-circle-fill me-2"></i>
        ${info}
        <button type="button" class="btn-close" data-bs-dismiss="alert" 
                aria-label="Close"></button>
    </div>
    <c:remove var="info" scope="session"/>
</c:if>

<!-- Validation Errors List -->
<c:if test="${not empty errors}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <h5><i class="bi bi-x-circle-fill me-2"></i>Vui lòng kiểm tra lại:</h5>
        <ul class="mb-0">
            <c:forEach var="err" items="${errors}">
                <li>${err}</li>
            </c:forEach>
        </ul>
        <button type="button" class="btn-close" data-bs-dismiss="alert" 
                aria-label="Close"></button>
    </div>
    <c:remove var="errors" scope="session"/>
</c:if>
```

---

## 6. Ranh Giới Giao Dịch (Transaction Boundaries)

### 6.1. Tình Trạng Hiện Tại

Hiện tại, mỗi phương thức DAO mở và đóng kết nối riêng, không có transaction management tập trung.

```java
public class CategoryDAO {
    
    // Mỗi method tự quản lý connection
    public boolean insert(Category category) {
        // Mở connection
        try (Connection conn = DBContext.getConnection()) {
            // Thực thi SQL
            // Đóng connection tự động khi try-with-resources kết thúc
        }
    }
    
    public boolean update(Category category) {
        // Mở connection khác
        try (Connection conn = DBContext.getConnection()) {
            // ...
        }
    }
    
    public boolean delete(int id) {
        // Mở connection khác
        try (Connection conn = DBContext.getConnection()) {
            // ...
        }
    }
}
```

### 6.2. Vấn Đề Với Thiết Kế Hiện Tại

**Ví dụ: Tạo đơn hàng (có vấn đề)**

```java
// Servlet gọi nhiều DAO riêng lẻ
OrderDAO orderDAO = new OrderDAO();
OrderDetailDAO detailDAO = new OrderDetailDAO();
InventoryDAO inventoryDAO = new InventoryDAO();

// Nếu order insert thành công nhưng detail insert thất bại:
// => Đơn hàng tồn tại nhưng không có sản phẩm!
// => Inconsistent data
```

### 6.3. Giải Pháp Tương Lai: Service Layer Với Transaction

Khi hệ thống phức tạp hơn, cần có Service Layer để quản lý transaction:

```java
public class OrderService {
    
    private OrderDAO orderDAO = new OrderDAO();
    private OrderDetailDAO detailDAO = new OrderDetailDAO();
    private InventoryDAO inventoryDAO = new InventoryDAO();
    
    public boolean createOrder(Order order, List<OrderDetail> details) {
        Connection conn = null;
        try {
            // Bắt đầu transaction
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);
            
            // Insert order
            int orderId = orderDAO.insertReturnId(order, conn);
            
            // Insert details với orderId
            for (OrderDetail detail : details) {
                detail.setOrderId(orderId);
                detailDAO.insert(detail, conn);
                
                // Update inventory
                inventoryDAO.decrementStock(
                    detail.getProductId(), 
                    order.getWarehouseId(),
                    detail.getQuantity(),
                    conn
                );
            }
            
            // Commit transaction
            conn.commit();
            return true;
            
        } catch (Exception e) {
            // Rollback nếu có lỗi
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
            
        } finally {
            // Đóng connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

### 6.4. Các Phương Thức DAO Nhận Connection

```java
public class OrderDAO {
    
    // Phương thức không có connection (tự quản lý)
    public int insertReturnId(Order order) {
        try (Connection conn = DBContext.getConnection()) {
            return insertReturnId(order, conn);
        }
    }
    
    // Phương thức nhận connection (dùng trong transaction)
    public int insertReturnId(Order order, Connection conn) {
        String sql = "INSERT INTO [order] (order_code, order_type, ...) VALUES (...)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, 
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters...
            ps.executeUpdate();
            
            // Lấy generated key
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
```

---

## 7. Mẫu Thiết Kế DAO

### 7.1. Cấu Trúc DAO Chuẩn

```java
public class CategoryDAO {
    
    // ==================== CRUD Operations ====================
    
    /**
     * Insert a new category and return the generated ID
     */
    public int insert(Category category) {
        String sql = "INSERT INTO category (category_name, description, " +
                     "parent_category_id, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBContext.getConnection();
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
    
    /**
     * Update an existing category
     */
    public boolean update(Category category) {
        String sql = "UPDATE category SET category_name = ?, description = ?, " +
                     "parent_category_id = ?, status = ?, update_at = GETDATE() " +
                     "WHERE category_id = ?";
        
        try (Connection conn = DBContext.getConnection();
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
    
    /**
     * Find category by ID
     */
    public Category findById(int id) {
        String sql = "SELECT * FROM category WHERE category_id = ?";
        
        try (Connection conn = DBContext.getConnection();
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
    
    /**
     * Search categories with filter and pagination
     */
    public List<Category> search(CategoryFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM category WHERE 1=1");
        
        List<Object> params = new ArrayList<>();
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            sql.append(" AND category_name LIKE ?");
            params.add("%" + filter.getSearchTerm() + "%");
        }
        
        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(filter.getStatus());
        }
        
        sql.append(" ORDER BY ");
        sql.append(filter.getSortColumn() != null ? filter.getSortColumn() : "category_id");
        sql.append(" ");
        sql.append(filter.isSortAscending() ? "ASC" : "DESC");
        
        // Pagination
        int offset = (filter.getPage() - 1) * filter.getPageSize();
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(filter.getPageSize());
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            List<Category> categories = new ArrayList<>();
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            
            return categories;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Count total records matching filter
     */
    public int count(CategoryFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM category WHERE 1=1");
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            sql.append(" AND category_name LIKE ?");
        }
        
        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            sql.append(" AND status = ?");
        }
        
        try (Connection conn = DBContext.getConnection();
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
    
    // ==================== Helper Methods ====================
    
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));
        
        int parentId = rs.getInt("parent_category_id");
        category.setParentCategoryId(rs.wasNull() ? null : parentId);
        
        category.setStatus(rs.getString("status"));
        category.setCreatedAt(rs.getTimestamp("created_at"));
        category.setUpdateAt(rs.getTimestamp("update_at"));
        
        return category;
    }
}
```

### 7.2. Interface ICrudDAO

```java
public interface ICrudDAO<T, ID> {
    
    /**
     * Insert a new record
     * @return number of rows affected, or 0 if failed
     */
    int insert(T entity);
    
    /**
     * Update an existing record
     * @return true if successful
     */
    boolean update(T entity);
    
    /**
     * Delete a record by ID
     * @return true if successful
     */
    boolean delete(ID id);
    
    /**
     * Find a record by ID
     * @return the entity, or null if not found
     */
    T findById(ID id);
    
    /**
     * Get all records
     * @return list of all entities
     */
    List<T> findAll();
}
```

---

## 8. Xử Lý Lỗi

### 8.1. Các Loại Lỗi Thường Gặp

| Loại lỗi | Nguyên nhân | Xử lý |
|----------|-------------|--------|
| **Validation Error** | Dữ liệu không hợp lệ | Hiển thị thông báo, giữ lại form |
| **SQL Exception** | Lỗi database (khóa ngoại, unique constraint) | Log lỗi, hiển thị thông báo chung |
| **NullPointerException** | Dữ liệu null không mong đợi | Kiểm tra null trước khi sử dụng |
| **NumberFormatException** | Parse số thất bại | Validate input trước parse |
| **Connection Error** | Không kết nối được database | Thông báo cho admin, retry |

### 8.2. Mẫu Xử Lý Lỗi Trong Servlet

```java
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    
    List<String> errors = new ArrayList<>();
    
    // 1. Validate input
    String name = request.getParameter("name");
    if (name == null || name.trim().isEmpty()) {
        errors.add("Tên không được để trống");
    }
    
    String priceStr = request.getParameter("price");
    BigDecimal price;
    try {
        price = new BigDecimal(priceStr);
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Giá không được âm");
        }
    } catch (NumberFormatException e) {
        errors.add("Giá phải là số hợp lệ");
    }
    
    // 2. Nếu có lỗi validation
    if (!errors.isEmpty()) {
        request.getSession().setAttribute("errors", errors);
        // Giữ lại dữ liệu đã nhập
        request.getSession().setAttribute("formData", request.getParameterMap());
        response.sendRedirect(request.getContextPath() + "/ProductServlet?action=new");
        return;
    }
    
    // 3. Xử lý nghiệp vụ
    try {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        
        ProductDAO dao = new ProductDAO();
        boolean success = dao.insert(product);
        
        if (success) {
            request.getSession().setAttribute("message", "Thêm sản phẩm thành công!");
            response.sendRedirect(request.getContextPath() + "/ProductServlet?action=list");
        } else {
            errors.add("Không thể thêm sản phẩm vào database");
            request.getSession().setAttribute("errors", errors);
            response.sendRedirect(request.getContextPath() + "/ProductServlet?action=new");
        }
        
    } catch (Exception e) {
        // 4. Xử lý exception không mong đợi
        e.printStackTrace(); // Log cho developer
        
        // Thông báo chung cho user
        request.getSession().setAttribute("error", 
            "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        response.sendRedirect(request.getContextPath() + "/ProductServlet?action=list");
    }
}
```

### 8.3. Error Page JSP

```jsp
<%-- file: /views/common/error-page.jsp --%>
<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Lỗi - FinoraRetail</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" 
          rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card border-danger">
                    <div class="card-header bg-danger text-white">
                        <h4 class="mb-0">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>
                            Đã Xảy Ra Lỗi
                        </h4>
                    </div>
                    <div class="card-body">
                        <h5 class="card-title">Rất tiếc, đã có lỗi xảy ra!</h5>
                        <p class="card-text">
                            Chúng tôi đã ghi nhận lỗi này và sẽ xử lý trong thời gian sớm nhất.
                        </p>
                        
                        <c:if test="${pageContext.errorData.statusCode == 404}">
                            <div class="alert alert-warning">
                                <strong>Lỗi 404:</strong> Trang bạn tìm kiếm không tồn tại.
                            </div>
                        </c:if>
                        
                        <c:if test="${pageContext.errorData.statusCode == 500}">
                            <div class="alert alert-danger">
                                <strong>Lỗi 500:</strong> Lỗi server nội bộ.
                            </div>
                        </c:if>
                        
                        <hr>
                        
                        <div class="d-grid gap-2">
                            <a href="${pageContext.request.contextPath}/" 
                               class="btn btn-primary">
                                <i class="bi bi-house-fill me-2"></i>
                                Về Trang Chủ
                            </a>
                            <a href="javascript:history.back()" 
                               class="btn btn-outline-secondary">
                                <i class="bi bi-arrow-left me-2"></i>
                                Quay Lại
                            </a>
                        </div>
                    </div>
                </div>
                
                <%-- Chỉ hiển thị chi tiết lỗi khi debug --%>
                <c:if test="${applicationScope.debugMode == true}">
                    <div class="mt-3">
                        <pre class="bg-dark text-light p-3 rounded"><%= exception %></pre>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</body>
</html>
```

---

## Tổng Kết

Luồng dữ liệu trong hệ thống FinoraRetail tuân theo mô hình MVC với các đặc điểm:

1. **POST-Redirect-GET Pattern**: Ngăn chặn duplicate submission khi refresh trang
2. **Flash Message**: Thông báo tạm thời được lưu trong session và tự động xóa sau khi hiển thị
3. **PreparedStatement**: Ngăn chặn SQL Injection bằng parameterized queries
4. **Connection Management**: Mỗi DAO method tự quản lý connection (trong tương lai sẽ chuyển sang Service Layer với transaction management)
5. **Soft Delete**: Các bản ghi không bị xóa vật lý mà chỉ cập nhật status
6. **Audit Trail**: Tất cả thay đổi được ghi nhận trong bảng `audit_log` và `stock_transaction`

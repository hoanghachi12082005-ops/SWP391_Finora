# Module Quản Lý Sản Phẩm (Product Management) — Hướng Dẫn Chi Tiết

> **Dành cho người mới học Java Web.** Tài liệu này giải thích từng dòng code, cách các file liên kết nhau, luồng hoạt động (flow), và cú pháp Java/JSP được dùng trong module Quản Lý Sản Phẩm của dự án KiotRetail (SWP391_Finora).

---

## Mục Lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Sơ đồ flow hoạt động](#2-sơ-đồ-flow-hoạt-động)
3. [Cấu trúc thư mục các file liên quan](#3-cấu-trúc-thư-mục)
4. [File 1 — Product.java (Model)](#4-file-1--productjava-model)
5. [File 2 — DatabaseUtil.java (Kết nối DB)](#5-file-2--databaseutiljava-tiện-ích-kết-nối-db)
6. [File 3 — ProductDAO.java (Data Access Object)](#6-file-3--productdaojava-data-access-object)
7. [File 4 — ProductManagementServlet.java (Controller)](#7-file-4--productmanagementservletjava-controller)
8. [File 5 — web.xml (Cấu hình routing)](#8-file-5--webxml-cấu-hình-routing)
9. [File 6 — index.jsp (View)](#9-file-6--indexjsp-view--giao-diện)
10. [Cách các file liên kết với nhau](#10-cách-các-file-liên-kết-với-nhau)
11. [Các tính năng đã triển khai](#11-các-tính-năng-đã-triển-khai)
12. [Khái niệm Java quan trọng tổng hợp](#12-khái-niệm-java-quan-trọng-tổng-hợp)

---

## 1. Tổng Quan Kiến Trúc

Dự án dùng mô hình **MVC (Model – View – Controller)**:

```
Trình duyệt (Browser)
      │
      ▼
  web.xml  ──── Nhận HTTP request, tìm servlet phù hợp
      │
      ▼
ProductManagementServlet.java  ──── CONTROLLER
      │                             (điều phối logic)
      ├──► ProductDAO.java  ──────── DATA ACCESS (đọc/ghi DB)
      │         │
      │         ▼
      │    DatabaseUtil.java  ────── KẾT NỐI SQL Server
      │
      ├──► Product.java  ─────────── MODEL (đối tượng dữ liệu)
      │
      └──► index.jsp  ────────────── VIEW (hiển thị HTML)
```

| Lớp | Trách nhiệm |
|-----|------------|
| **Model** (`Product.java`) | Chứa dữ liệu một sản phẩm |
| **DAO** (`ProductDAO.java`) | Thực thi các câu lệnh SQL |
| **Controller** (`ProductManagementServlet.java`) | Nhận request, gọi DAO, truyền data sang JSP |
| **View** (`index.jsp`) | Render HTML để trình duyệt hiển thị |
| **Utility** (`DatabaseUtil.java`) | Tạo kết nối đến SQL Server |
| **Config** (`web.xml`) | Map URL → Servlet |

---

## 2. Sơ Đồ Flow Hoạt Động

### Khi người dùng MỞ trang (GET request)

```
Người dùng nhập URL:
http://localhost:8080/SWP391_Finora/product-management
        │
        ▼
[web.xml] → map /product-management → ProductManagementServlet
        │
        ▼
[ProductManagementServlet.doGet()]
  ├─ Đọc tham số từ URL: ?keyword=...&status=...&page=...&view=...
  ├─ Gọi productDAO.getTotalCount(keyword, status) → tính tổng trang
  ├─ Gọi productDAO.findAll(offset, limit, keyword, status) → lấy danh sách
  ├─ Đặt data vào request.setAttribute(...)
  └─ Forward tới /WEB-INF/views/product-management/index.jsp
        │
        ▼
[index.jsp]
  ├─ Lấy data từ request.getAttribute(...)
  ├─ Render HTML: bảng sản phẩm / card grid / phân trang / modal
  └─ Trả HTML về trình duyệt → người dùng thấy giao diện
```

### Khi người dùng THÊM / SỬA / XÓA (POST request)

```
Người dùng submit form (Thêm / Sửa / Xóa)
        │
        ▼
[web.xml] → map /product-management → ProductManagementServlet
        │
        ▼
[ProductManagementServlet.doPost()]
  ├─ Đọc action = "add" / "edit" / "delete"
  ├─ case "add"    → buildProductFromRequest() → productDAO.insert(p)
  ├─ case "edit"   → buildProductFromRequest() → productDAO.update(p)
  ├─ case "delete" → productDAO.delete(id)
  └─ response.sendRedirect(...) → chuyển về trang danh sách (GET)
```

> **Tại sao redirect sau POST?** Đây là pattern "Post/Redirect/Get" (PRG). Nếu không redirect, người dùng bấm F5 sẽ submit form lại, gây thêm/xóa trùng dữ liệu.

---

## 3. Cấu Trúc Thư Mục

```
SWP391_Finora/
├── src/java/
│   ├── product/
│   │   ├── model/
│   │   │   └── Product.java                   ← Model: dữ liệu 1 sản phẩm
│   │   ├── dao/
│   │   │   └── ProductDAO.java                ← DAO: truy vấn SQL
│   │   ├── controller/
│   │   │   └── ProductManagementServlet.java  ← Controller
│   │   └── service/
│   │       └── ProductManagementService.java  ← Service (skeleton, chưa dùng)
│   └── common/
│       └── util/
│           └── DatabaseUtil.java              ← Tiện ích kết nối DB
├── web/
│   ├── WEB-INF/
│   │   ├── web.xml                            ← Cấu hình URL mapping
│   │   └── views/
│   │       └── product-management/
│   │           └── index.jsp                  ← Giao diện HTML/JSP
│   └── assets/
│       └── css/
│           └── styles.css                     ← CSS global
└── docs/
    └── product-module-readme.md               ← File này
```

---

## 4. File 1 — `Product.java` (Model)

**Đường dẫn:** `src/java/product/model/Product.java`

**Mục đích:** Là một "bản thiết kế" cho đối tượng sản phẩm. Mỗi sản phẩm trong DB sẽ được ánh xạ thành một object `Product` trong Java.

```java
package product.model;
// Khai báo package: file này thuộc nhóm "product.model"
// Package giúp tổ chức code theo chức năng, tránh trùng tên class

import java.math.BigDecimal;
// BigDecimal dùng cho số tiền (thay vì double) vì BigDecimal
// không bị sai số khi tính toán tài chính
// (double có thể bị: 0.1 + 0.2 = 0.30000000004)

import java.time.LocalDateTime;
// LocalDateTime dùng cho ngày giờ (thay vì Date cũ)

public class Product {
// "public class": mọi class khác đều có thể sử dụng class này
// Tên class phải bằng tên file (Product.java)

    // ===== FIELDS (Thuộc tính / Biến thành viên) =====
    private int productID;
    // "private": chỉ code trong class này mới được truy cập trực tiếp
    // Điều này gọi là "Encapsulation" (đóng gói) - 1 trong 4 trụ cột OOP
    // int = kiểu số nguyên (không có số thập phân)

    private int categoryID;          // ID danh mục cha
    private String name;             // Tên sản phẩm (String = chuỗi ký tự)
    private String sku;              // Mã SKU (Stock Keeping Unit)
    private BigDecimal price;        // Giá bán
    private BigDecimal costPrice;    // Giá vốn
    private int stockAlertQty;       // Số lượng tồn kho tối thiểu cần cảnh báo
    private String status;           // "Active" hoặc "Inactive"
    private LocalDateTime createdAt; // Thời điểm tạo

    // ===== CONSTRUCTOR (Hàm khởi tạo) =====

    public Product() {
        // Constructor rỗng: tạo Product mà chưa có dữ liệu
        // Cần thiết vì DAO sẽ tạo Product() rỗng rồi set từng field sau
    }

    public Product(int productID, int categoryID, String name, String sku,
                   BigDecimal price, BigDecimal costPrice,
                   int stockAlertQty, String status, LocalDateTime createdAt) {
        // Constructor có tham số: tạo Product với đầy đủ dữ liệu ngay
        this.productID     = productID;
        // "this.productID" = biến thành viên của class
        // "productID" (bên phải) = tham số truyền vào constructor
        // Dùng "this." để phân biệt khi tên trùng nhau
        this.categoryID    = categoryID;
        this.name          = name;
        this.sku           = sku;
        this.price         = price;
        this.costPrice     = costPrice;
        this.stockAlertQty = stockAlertQty;
        this.status        = status;
        this.createdAt     = createdAt;
    }

    // ===== GETTERS & SETTERS (Phương thức truy cập) =====

    public int getProductID() { return productID; }
    // Getter: cho phép class khác ĐỌC giá trị productID
    // Quy tắc đặt tên: "get" + Tên field viết hoa chữ đầu

    public void setProductID(int productID) { this.productID = productID; }
    // Setter: cho phép class khác GHI giá trị productID
    // "void" = phương thức không trả về gì
    // Quy tắc đặt tên: "set" + Tên field viết hoa chữ đầu

    public int getCategoryID()                        { return categoryID; }
    public void setCategoryID(int categoryID)         { this.categoryID = categoryID; }

    public String getName()                           { return name; }
    public void setName(String name)                  { this.name = name; }

    public String getSku()                            { return sku; }
    public void setSku(String sku)                    { this.sku = sku; }

    public BigDecimal getPrice()                      { return price; }
    public void setPrice(BigDecimal price)            { this.price = price; }

    public BigDecimal getCostPrice()                  { return costPrice; }
    public void setCostPrice(BigDecimal costPrice)    { this.costPrice = costPrice; }

    public int getStockAlertQty()                     { return stockAlertQty; }
    public void setStockAlertQty(int stockAlertQty)   { this.stockAlertQty = stockAlertQty; }

    public String getStatus()                         { return status; }
    public void setStatus(String status)              { this.status = status; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ===== toString() =====
    @Override
    public String toString() {
        // @Override: phương thức này ghi đè (override) toString() của class Object
        // toString() được tự động gọi khi System.out.println(product)
        // Hữu ích khi debug — in thông tin sản phẩm ra console
        return "Product{productID=" + productID
             + ", name='" + name + "'"
             + ", sku='" + sku + "'"
             + ", price=" + price + "}";
    }
}
```

**Tóm tắt:** `Product.java` chỉ là một "hộp chứa dữ liệu" (Data Holder). Nó không có logic, chỉ có fields + getters/setters. Trong Java, loại class này gọi là **POJO** (Plain Old Java Object).

---

## 5. File 2 — `DatabaseUtil.java` (Tiện Ích Kết Nối DB)

**Đường dẫn:** `src/java/common/util/DatabaseUtil.java`

**Mục đích:** Là "cầu nối" duy nhất giữa Java và SQL Server. Mọi DAO đều gọi `DatabaseUtil.getConnection()` để lấy kết nối.

```java
package common.util;

import java.sql.Connection;
// Connection = đối tượng đại diện cho một kết nối đến database
// Giống như "đường dây điện thoại" đang mở giữa Java và SQL Server

import java.sql.DriverManager;
// DriverManager = "tổng đài", giúp tạo Connection đến đúng database

import java.sql.SQLException;
// Exception được ném ra khi có lỗi liên quan đến SQL

import jakarta.servlet.ServletContext;
// ServletContext = thông tin chung của toàn ứng dụng web
// Dùng để đọc cấu hình từ web.xml

public final class DatabaseUtil {
// "final": class này không thể bị kế thừa (extend)
// Best practice cho utility class — không ai cần extend nó

    // Cấu hình mặc định (fallback nếu web.xml không cấu hình)
    private static final String DEFAULT_DRIVER   = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DEFAULT_URL      =
        "jdbc:sqlserver://localhost:1433;databaseName=DBFinora;encrypt=true;trustServerCertificate=true";
    // JDBC URL có cấu trúc: jdbc:[loại_DB]://[host]:[port];[tham_số]
    // localhost:1433 = SQL Server chạy trên máy local, cổng mặc định 1433
    private static final String DEFAULT_USERNAME = "sa";
    private static final String DEFAULT_PASSWORD = "1234";

    private static ServletContext servletContext;
    // "static": biến này thuộc về class (không phải object cụ thể)
    // Tất cả mọi nơi gọi DatabaseUtil đều dùng chung 1 servletContext

    private DatabaseUtil() {}
    // Constructor private: ngăn không cho tạo object (new DatabaseUtil())
    // Vì class này chỉ có static methods, không cần tạo object
    // Gọi trực tiếp: DatabaseUtil.getConnection()

    public static void configure(ServletContext context) {
        servletContext = context;
        // Được gọi 1 lần khi app khởi động (từ AppStartupListener)
        // để lưu lại servletContext dùng cho việc đọc cấu hình sau này
    }

    public static Connection getConnection() throws SQLException {
    // "static": gọi trực tiếp qua class mà không cần new DatabaseUtil()
    // "throws SQLException": phương thức có thể ném lỗi, caller phải xử lý

        // Bước 1: Đọc cấu hình (từ web.xml nếu có, không thì dùng default)
        String driver   = getInitParameter("db.driver",   DEFAULT_DRIVER);
        String url      = getInitParameter("db.url",      DEFAULT_URL);
        String username = getInitParameter("db.username", DEFAULT_USERNAME);
        String password = getInitParameter("db.password", DEFAULT_PASSWORD);

        // Bước 2: Nạp JDBC driver vào JVM
        try {
            Class.forName(driver);
            // Class.forName() yêu cầu JVM tải class driver JDBC
            // Driver tự đăng ký với DriverManager sau khi được tải
            // Nếu file JAR driver không có trong classpath → lỗi ClassNotFoundException
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Database driver not found: " + driver, ex);
            // Wrap exception: chuyển ClassNotFoundException → SQLException
            // để caller chỉ cần xử lý 1 loại exception
        }

        // Bước 3: Tạo và trả về Connection thực sự
        return DriverManager.getConnection(url, username, password);
        // Đây là nơi thực sự "mở cổng" kết nối đến SQL Server
        // Nếu thông tin sai → ném SQLException
    }

    private static String getInitParameter(String name, String defaultValue) {
        if (servletContext == null) return defaultValue;
        // Nếu chạy không có Tomcat (test thuần Java), dùng giá trị default

        String value = servletContext.getInitParameter(name);
        // Đọc giá trị <context-param> từ web.xml theo tên

        return value == null || value.isBlank() ? defaultValue : value;
        // Toán tử 3 ngôi (ternary operator):
        //   điều_kiện ? giá_trị_nếu_đúng : giá_trị_nếu_sai
        // Nếu không tìm thấy tham số → dùng giá trị mặc định
    }
}
```

**Cách liên kết với web.xml:**

```xml
<!-- web.xml định nghĩa cấu hình DB -->
<context-param>
    <param-name>db.password</param-name>   <!-- tên tham số -->
    <param-value>1234</param-value>         <!-- giá trị -->
</context-param>
```

```java
// DatabaseUtil đọc tham số này qua:
servletContext.getInitParameter("db.password");  // → trả về "1234"
```

---

## 6. File 3 — `ProductDAO.java` (Data Access Object)

**Đường dẫn:** `src/java/product/dao/ProductDAO.java`

**Mục đích:** Là lớp DUY NHẤT được phép chứa câu lệnh SQL. DAO = Data Access Object.

**Nguyên tắc vàng:** Không có SQL trong Servlet. Không có SQL trong JSP. Tất cả SQL chỉ được viết trong DAO.

### 6.1 Hằng số SELECT_COLUMNS

```java
private static final String SELECT_COLUMNS =
    "ProductID, CategoryID, Name, SKU, Price, CostPrice, StockAlertQty, Status, CreatedAt";
// private: chỉ dùng trong class này
// static: thuộc về class, không phải object
// final: không thể thay đổi giá trị (hằng số)
// Đặt tên hằng bằng CHU_HOA_GACH_DUOI theo quy ước Java
//
// Lợi ích: tránh viết lại danh sách cột nhiều lần.
// Nếu cần sửa chỉ sửa 1 chỗ (DRY principle - Don't Repeat Yourself)
```

### 6.2 findAll() — Đọc danh sách sản phẩm

```java
public List<Product> findAll(int offset, int limit, String keyword, String status)
    throws SQLException {
// List<Product>: trả về một danh sách các Product
// <Product>: "Generic" — chỉ định kiểu phần tử trong List
// offset: bỏ qua bao nhiêu dòng đầu (phân trang)
// limit: lấy tối đa bao nhiêu dòng (kích thước trang)
// keyword: từ khóa tìm kiếm (null = không tìm)
// status: lọc theo trạng thái (null = tất cả)
// throws SQLException: caller phải try-catch hoặc khai báo throws tiếp

    List<Product> items = new ArrayList<>();
    // Tạo danh sách rỗng để chứa kết quả
    // ArrayList là implement phổ biến nhất của interface List

    // Xây dựng câu SQL động (thay đổi tùy tham số)
    StringBuilder sql = new StringBuilder(
        "SELECT " + SELECT_COLUMNS + " FROM Product WHERE 1=1"
    );
    // StringBuilder: hiệu quả hơn String khi nối chuỗi nhiều lần
    // "WHERE 1=1": trick phổ biến — luôn đúng, giúp dễ dàng
    // thêm "AND ..." phía sau mà không cần kiểm tra dấu WHERE

    if (keyword != null && !keyword.isBlank())
        sql.append(" AND (Name LIKE ? OR SKU LIKE ?)");
    // isBlank(): trả về true nếu chuỗi rỗng hoặc chỉ có khoảng trắng
    // LIKE ?: tìm kiếm mờ trong SQL (? là placeholder — chống SQL Injection)
    // OR: tìm trong cả tên lẫn SKU

    if (status != null && !status.isBlank())
        sql.append(" AND Status = ?");
    // Chỉ thêm điều kiện lọc status nếu người dùng có chọn

    sql.append(" ORDER BY ProductID ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    // ORDER BY ProductID ASC: sắp xếp theo ID tăng dần
    // OFFSET ? ROWS: bỏ qua ? dòng đầu (SQL Server syntax cho phân trang)
    // FETCH NEXT ? ROWS ONLY: lấy ? dòng tiếp theo

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
    // try-with-resources: tự động đóng conn và stmt sau khi dùng xong
    // (tương đương finally { conn.close(); stmt.close(); })
    // PreparedStatement: câu lệnh SQL đã biên dịch sẵn với placeholders (?)

        // Điền giá trị vào các placeholder (?) theo thứ tự
        int idx = 1;
        // idx: chỉ số placeholder, bắt đầu từ 1 (không phải 0!)

        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword + "%";
            // % trong SQL LIKE: đại diện cho bất kỳ chuỗi nào
            // "%ao%" tìm tất cả có chứa "ao" ở bất kỳ vị trí nào
            stmt.setString(idx++, like);  // placeholder 1: Name LIKE ?
            stmt.setString(idx++, like);  // placeholder 2: SKU LIKE ?
            // idx++ : dùng idx hiện tại rồi tăng lên 1
        }

        if (status != null && !status.isBlank())
            stmt.setString(idx++, status); // placeholder: Status = ?

        stmt.setInt(idx++, offset); // placeholder: OFFSET ?
        stmt.setInt(idx,   limit);  // placeholder: FETCH NEXT ?

        try (ResultSet rs = stmt.executeQuery()) {
        // ResultSet: "con trỏ" trỏ vào kết quả truy vấn
        // Ban đầu trỏ trước dòng đầu tiên

            while (rs.next()) {
            // rs.next(): di chuyển con trỏ xuống dòng tiếp theo
            // Trả về true nếu còn dòng, false nếu hết
                items.add(extractProduct(rs));
                // Gọi hàm helper chuyển 1 dòng ResultSet → 1 object Product
            }
        }
    }
    return items;
    // Trả danh sách (có thể rỗng nếu không có sản phẩm nào)
}
```

### 6.3 getTotalCount() — Đếm tổng số bản ghi

```java
public int getTotalCount(String keyword, String status) throws SQLException {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Product WHERE 1=1");
    // COUNT(*): hàm tổng hợp SQL, đếm số dòng thỏa mãn điều kiện

    if (keyword != null && !keyword.isBlank()) sql.append(" AND (Name LIKE ? OR SKU LIKE ?)");
    if (status != null && !status.isBlank())   sql.append(" AND Status = ?");

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        // (Điền placeholder tương tự findAll)
        int idx = 1;
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword + "%";
            stmt.setString(idx++, like);
            stmt.setString(idx++, like);
        }
        if (status != null && !status.isBlank()) stmt.setString(idx, status);

        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
            // COUNT(*) trả về đúng 1 dòng, 1 cột
            // rs.getInt(1): lấy giá trị cột thứ 1 dưới dạng int
            // Toán tử 3 ngôi: nếu có dòng → trả số đếm, ngược lại → 0
        }
    }
}
```

### 6.4 insert() — Thêm sản phẩm mới

```java
public void insert(Product product) throws SQLException {
    String sql = "INSERT INTO Product (CategoryID, Name, SKU, Price, CostPrice, StockAlertQty, Status)"
               + " VALUES (?, ?, ?, ?, ?, ?, ?)";
    // INSERT INTO: câu lệnh thêm dữ liệu vào bảng
    // Không có ProductID: SQL Server tự sinh (IDENTITY/AUTO_INCREMENT)
    // Không có CreatedAt: SQL Server tự điền (DEFAULT GETDATE())
    // 7 cột → 7 dấu ? tương ứng

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1,        product.getCategoryID());
        stmt.setString(2,     product.getName());
        stmt.setString(3,     product.getSku());
        stmt.setBigDecimal(4, product.getPrice());
        // setBigDecimal: dùng đúng kiểu cho cột DECIMAL/MONEY trong DB
        stmt.setBigDecimal(5, product.getCostPrice());
        stmt.setInt(6,        product.getStockAlertQty());
        stmt.setString(7,     product.getStatus());
        stmt.executeUpdate();
        // executeUpdate(): thực thi INSERT/UPDATE/DELETE
        // (khác executeQuery() dùng cho SELECT)
    }
}
```

### 6.5 update() — Cập nhật sản phẩm

```java
public void update(Product product) throws SQLException {
    String sql = "UPDATE Product SET CategoryID=?, Name=?, SKU=?, Price=?, "
               + "CostPrice=?, StockAlertQty=?, Status=? WHERE ProductID=?";
    // UPDATE: sửa dữ liệu đã có
    // SET: danh sách field cần cập nhật
    // WHERE ProductID=?: chỉ cập nhật đúng sản phẩm có ID này
    // QUAN TRỌNG: luôn có WHERE trong UPDATE để không cập nhật toàn bảng!

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1,        product.getCategoryID());
        stmt.setString(2,     product.getName());
        stmt.setString(3,     product.getSku());
        stmt.setBigDecimal(4, product.getPrice());
        stmt.setBigDecimal(5, product.getCostPrice());
        stmt.setInt(6,        product.getStockAlertQty());
        stmt.setString(7,     product.getStatus());
        stmt.setInt(8,        product.getProductID());
        // ProductID là placeholder thứ 8 (cuối cùng, trong mệnh đề WHERE)
        stmt.executeUpdate();
    }
}
```

### 6.6 delete() — Xóa sản phẩm

```java
public void delete(int id) throws SQLException {
    String sql = "DELETE FROM Product WHERE ProductID = ?";
    // DELETE: xóa dòng khỏi bảng
    // QUAN TRỌNG: luôn có WHERE để không xóa toàn bộ bảng!

    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }
}
```

### 6.7 extractProduct() — Helper chuyển ResultSet → Product

```java
private Product extractProduct(ResultSet rs) throws SQLException {
// "private": chỉ dùng nội bộ trong DAO, không expose ra ngoài
// Đây là "helper method" - phương thức hỗ trợ, tránh lặp code

    Product item = new Product();  // Tạo Product rỗng

    item.setProductID(rs.getInt("ProductID"));
    // rs.getInt("ProductID"): lấy giá trị cột tên "ProductID" trong ResultSet
    // Có thể dùng tên cột (String) hoặc chỉ số (int), dùng tên dễ đọc hơn

    item.setCategoryID(rs.getInt("CategoryID"));
    item.setName(rs.getString("Name"));
    item.setSku(rs.getString("SKU"));
    item.setPrice(rs.getBigDecimal("Price"));
    item.setCostPrice(rs.getBigDecimal("CostPrice"));
    item.setStockAlertQty(rs.getInt("StockAlertQty"));
    item.setStatus(rs.getString("Status"));

    Timestamp ts = rs.getTimestamp("CreatedAt");
    // Timestamp: kiểu thời gian của JDBC (khác LocalDateTime của Java 8+)
    if (ts != null) item.setCreatedAt(ts.toLocalDateTime());
    // Kiểm tra null TRƯỚC khi gọi method, vì CreatedAt có thể NULL trong DB
    // toLocalDateTime(): chuyển Timestamp → LocalDateTime (Java 8+)

    return item;
}
```

**Tổng kết ProductDAO — Bảng CRUD:**

| Method | Câu SQL | Mô tả |
|--------|---------|-------|
| `findAll(offset, limit, keyword, status)` | SELECT + OFFSET/FETCH | Đọc danh sách có phân trang + tìm kiếm + lọc |
| `getTotalCount(keyword, status)` | SELECT COUNT(*) | Đếm tổng để tính số trang |
| `findById(id)` | SELECT WHERE ID = ? | Tìm 1 sản phẩm theo ID |
| `insert(product)` | INSERT INTO | Thêm sản phẩm mới |
| `update(product)` | UPDATE SET WHERE | Cập nhật sản phẩm |
| `delete(id)` | DELETE WHERE | Xóa sản phẩm |

---

## 7. File 4 — `ProductManagementServlet.java` (Controller)

**Đường dẫn:** `src/java/product/controller/ProductManagementServlet.java`

**Mục đích:** Là "não" của module. Nhận HTTP request, gọi DAO lấy/sửa dữ liệu, gửi kết quả sang JSP để hiển thị.

```java
package product.controller;

import product.dao.ProductDAO;
import product.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
// HttpServlet: class cha mà Servlet phải kế thừa (extend)
// Cung cấp sẵn các method doGet(), doPost(), init()... để override
import jakarta.servlet.http.HttpServletRequest;
// HttpServletRequest: đại diện cho HTTP request từ trình duyệt gửi lên
// Chứa: URL params, form data, session, cookies, headers...
import jakarta.servlet.http.HttpServletResponse;
// HttpServletResponse: đại diện cho HTTP response gửi về trình duyệt
// Dùng để: redirect, set header, write HTML...
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ProductManagementServlet extends HttpServlet {
// "extends HttpServlet": kế thừa HttpServlet
// Kế thừa (Inheritance) = 1 trong 4 trụ cột OOP

    private ProductDAO productDAO;
    // Field cấp class: tồn tại suốt vòng đời Servlet (lâu hơn 1 request)
    // Khởi tạo 1 lần trong init(), tái dùng trong mọi request sau

    private static final int ITEMS_PER_PAGE = 5;
    // Hằng số: số sản phẩm hiển thị mỗi trang
    // static final = hằng số cấp class, giá trị không thể thay đổi

    // === init(): Được gọi 1 LẦN DUY NHẤT khi Servlet khởi động ===
    @Override
    public void init() throws ServletException {
    // @Override: khai báo method này ghi đè (override) method cùng tên của class cha
    // Nếu đặt tên sai (ví dụ: Init()), compiler báo lỗi — bảo vệ tránh bug

        productDAO = new ProductDAO();
        // Khởi tạo DAO 1 lần (không phải mỗi request)
        // Tiết kiệm thời gian tạo object với nhiều request đồng thời
    }

    // === doGet(): Xử lý GET request (mở URL, click link, chuyển trang) ===
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Bước 1: Đọc tham số từ URL (?key=value)
        String keyword  = request.getParameter("keyword");
        // URL: /product-management?keyword=ao → keyword = "ao"
        // Không có ?keyword= trong URL → trả về null

        String status   = request.getParameter("status");
        // URL: ?status=Active → status = "Active"

        String viewMode = request.getParameter("view");
        if (viewMode == null) viewMode = "table";
        // Mặc định hiển thị dạng bảng nếu chưa chọn chế độ xem

        // Bước 2: Xử lý số trang (pagination)
        int page = 1; // Mặc định trang 1
        try {
            if (request.getParameter("page") != null)
                page = Integer.parseInt(request.getParameter("page").trim());
            // Integer.parseInt(): chuyển String "3" → int 3
            // .trim(): xóa khoảng trắng thừa ở đầu/cuối chuỗi
        } catch (NumberFormatException ignored) {}
        // Nếu "page=abc" (không phải số), bắt lỗi và giữ page = 1
        // "ignored": tên biến exception, đặt tên này để rõ ý "cố tình bỏ qua"

        // Bước 3: Truy vấn database
        try {
            int totalCount = productDAO.getTotalCount(keyword, status);
            // Đếm tổng sản phẩm thỏa mãn điều kiện tìm kiếm/lọc

            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            // Math.ceil(): làm tròn lên
            // (double) totalCount: ép kiểu int → double để phép chia ra số thập phân
            // Ví dụ: 11 sản phẩm / 5 mỗi trang = 2.2 → ceil = 3 trang
            // Nếu không ép kiểu: 11 / 5 = 2 (int division, bỏ phần lẻ) → sai!

            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));
            // Giới hạn page trong khoảng hợp lệ [1 .. totalPages]
            // Math.min(page, totalPages): không vượt quá trang cuối
            // Math.max(1, ...): không nhỏ hơn trang 1
            // Tránh trường hợp người dùng nhập ?page=999 khi chỉ có 2 trang

            // Bước 4: Đặt data vào request để truyền sang JSP
            request.setAttribute("products",
                productDAO.findAll((page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE, keyword, status));
            // Công thức offset phân trang:
            //   Trang 1: (1-1)*5 = 0  → bắt đầu từ dòng 0, lấy 5 dòng
            //   Trang 2: (2-1)*5 = 5  → bỏ qua 5 dòng, lấy 5 dòng tiếp
            //   Trang 3: (3-1)*5 = 10 → bỏ qua 10 dòng, lấy 5 dòng tiếp

            request.setAttribute("currentPage",  page);
            request.setAttribute("totalPages",   totalPages);
            request.setAttribute("keyword",      keyword != null ? keyword : "");
            // Truyền "" thay vì null để JSP không phải kiểm tra null
            request.setAttribute("filterStatus", status != null ? status : "");
            request.setAttribute("viewMode",     viewMode);
            // setAttribute("tên", giá_trị): đặt biến vào "túi" của request
            // JSP lấy ra bằng request.getAttribute("tên")

            // Bước 5: Chuyển sang JSP để render HTML
            request.getRequestDispatcher("/WEB-INF/views/product-management/index.jsp")
                   .forward(request, response);
            // forward(): chuyển toàn bộ request (kèm data) sang JSP
            // URL trên trình duyệt KHÔNG thay đổi (khác redirect)
            // /WEB-INF/: thư mục bảo mật, người dùng không thể truy cập trực tiếp

        } catch (SQLException e) {
            throw new ServletException("Database error retrieving products", e);
            // Wrap exception: chuyển SQLException → ServletException
            // Tomcat sẽ hiển thị trang lỗi 500 cho người dùng
        }
    }

    // === doPost(): Xử lý POST request (submit form Thêm/Sửa/Xóa) ===
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        // BẮT BUỘC set trước khi đọc bất kỳ getParameter() nào
        // Để đọc đúng tiếng Việt từ form (không bị lỗi mã hóa)

        try {
            String action = request.getParameter("action");
            // Form gửi lên: <input type="hidden" name="action" value="add">
            // Tùy loại form: "add", "edit", hoặc "delete"

            switch (action == null ? "" : action) {
            // Switch-expression (Java 14+): chọn nhánh theo giá trị action
            // action == null ? "" : action: nếu null thì dùng "" để switch không lỗi

                case "add" -> {
                // Arrow syntax (Java 14+): không cần "break", sạch hơn switch cũ
                    Product p = buildProductFromRequest(request);
                    productDAO.insert(p);
                    // Tạo Product từ form data → gọi DAO thêm vào DB
                }

                case "edit" -> {
                    Product p = buildProductFromRequest(request);
                    p.setProductID(Integer.parseInt(request.getParameter("productID")));
                    // Edit cần productID để biết cập nhật sản phẩm nào
                    // productID được gửi qua: <input type="hidden" name="productID" value="...">
                    productDAO.update(p);
                }

                case "delete" -> productDAO.delete(
                    Integer.parseInt(request.getParameter("id"))
                );
                // Delete chỉ cần id, không cần tạo Product object
                // id được gửi qua: <input type="hidden" name="id" value="...">

            } // end switch

            // Sau khi xử lý xong: Redirect về trang danh sách (Pattern PRG)
            String keyword = request.getParameter("keyword");
            String status  = request.getParameter("filterStatus");
            String view    = request.getParameter("view");
            StringBuilder redirect = new StringBuilder(
                request.getContextPath() + "/product-management?"
            );
            // getContextPath(): lấy prefix của app = "/SWP391_Finora"
            // StringBuilder để nối chuỗi URL hiệu quả

            if (keyword != null && !keyword.isBlank())
                redirect.append("keyword=").append(keyword).append("&");
            if (status  != null && !status.isBlank())
                redirect.append("status=").append(status).append("&");
            if (view    != null && !view.isBlank())
                redirect.append("view=").append(view);
            // Giữ nguyên các filter/search params sau khi submit

            response.sendRedirect(redirect.toString());
            // sendRedirect(): bảo trình duyệt gửi GET request mới đến URL này
            // Sau redirect, URL đổi → người dùng bấm F5 không submit lại form

        } catch (Exception e) {
            throw new ServletException("Error processing request", e);
        }
    }

    // === buildProductFromRequest(): Helper tạo Product từ form data ===
    /*
     * 1. Dùng làm gì?
     *    Hàm này có chức năng thu thập các dữ liệu mà người dùng đã nhập trên form (giao diện HTML) 
     *    thông qua đối tượng request, sau đó gộp và chuyển đổi chúng thành một đối tượng Product hoàn chỉnh trong Java.
     * 
     * 2. Dùng khi nào?
     *    Được gọi khi Servlet nhận một yêu cầu dạng POST từ form trên trình duyệt, cụ thể là:
     *    - Thêm sản phẩm mới (action="add")
     *    - Cập nhật/sửa thông tin sản phẩm (action="edit")
     *    Việc tách logic tạo object ra thành 1 hàm riêng giúp tái sử dụng code ở cả 2 chức năng trên 
     *    (Áp dụng nguyên tắc DRY - Don't Repeat Yourself).
     * 
     * 3. Hoạt động ra sao?
     *    - Khởi tạo một đối tượng Product rỗng (new Product()).
     *    - Lấy từng giá trị từ form gửi lên bằng `request.getParameter(...)` (kết quả luôn là kiểu String).
     *    - Thực hiện ép kiểu (parsing) từ String sang đúng kiểu dữ liệu tương ứng trong Model 
     *      (dùng Integer.parseInt cho số nguyên, new BigDecimal cho số tiền).
     *    - Gắn các giá trị đã ép kiểu vào object Product thông qua các hàm setter (setters).
     *    - Cuối cùng trả về đối tượng Product để DAO có thể chèn (insert) hoặc cập nhật (update) vào database.
     * 
     * 4. SO SÁNH: TẠI SAO PHẢI CÓ HÀM NÀY?
     * 
     *   [NẾU KHÔNG CÓ HÀM NÀY] Code trong doPost() sẽ lặp lại khổng lồ:
     *   switch (action) {
     *       case "add" -> {
     *           Product p1 = new Product();
     *           p1.setCategoryID(Integer.parseInt(request.getParameter("categoryID")));
     *           p1.setName(request.getParameter("name"));
     *           p1.setPrice(new BigDecimal(request.getParameter("price")));
     *           // ... ép kiểu 4 trường nữa
     *           productDAO.insert(p1);
     *       }
     *       case "edit" -> {
     *           Product p2 = new Product();
     *           p2.setCategoryID(Integer.parseInt(request.getParameter("categoryID"))); // CODE LẶP LẠI!
     *           p2.setName(request.getParameter("name")); // CODE LẶP LẠI!
     *           p2.setPrice(new BigDecimal(request.getParameter("price"))); // CODE LẶP LẠI!
     *           // ... ép kiểu 4 trường nữa (LẶP LẠI!)
     *           p2.setProductID(Integer.parseInt(request.getParameter("productID"))); // Chỉ khác dòng này
     *           productDAO.update(p2);
     *       }
     *   }
     * 
     *   [KHI CÓ HÀM NÀY] Code doPost() gọn hơn và tái sử dụng tốt hơn:
     *   switch (action) {
     *       case "add" -> {
     *           productDAO.insert(buildProductFromRequest(request)); // Gọi 1 dòng
     *       }
     *       case "edit" -> {
     *           Product p = buildProductFromRequest(request); // Gọi 1 dòng (tái sử dụng)
     *           p.setProductID(Integer.parseInt(request.getParameter("productID")));
     *           productDAO.update(p);
     *       }
     *   }
     */
    private Product buildProductFromRequest(HttpServletRequest request) {
    // "private": chỉ dùng nội bộ trong Servlet
        Product p = new Product();
        p.setCategoryID(Integer.parseInt(request.getParameter("categoryID")));
        // Integer.parseInt(): chuyển String → int
        // Nếu người dùng nhập ký tự không phải số → NumberFormatException 
        // (lỗi này sẽ được bắt ở catch(Exception e) của hàm doPost bên ngoài)

        p.setName(request.getParameter("name"));
        p.setSku(request.getParameter("sku"));
        p.setPrice(new BigDecimal(request.getParameter("price")));
        // new BigDecimal("150000"): chuyển String → BigDecimal
        p.setCostPrice(new BigDecimal(request.getParameter("costPrice")));
        p.setStockAlertQty(Integer.parseInt(request.getParameter("stockAlertQty")));
        p.setStatus(request.getParameter("status"));
        return p;
    }
}
```

---

## 8. File 5 — `web.xml` (Cấu Hình Routing)

**Đường dẫn:** `web/WEB-INF/web.xml`

**Mục đích:** Là "bản đồ" của ứng dụng. Tomcat tra cứu web.xml khi nhận request để biết gửi đến Servlet nào.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee" version="6.0">
<!-- Jakarta EE 6.0 (Tomcat 10.1 dùng jakarta.*, không phải javax.* cũ) -->

    <!-- === CẤU HÌNH DATABASE === -->
    <!-- Các tham số này được đọc bởi DatabaseUtil.getInitParameter() -->
    <context-param>
        <param-name>db.driver</param-name>
        <param-value>com.microsoft.sqlserver.jdbc.SQLServerDriver</param-value>
    </context-param>
    <context-param>
        <param-name>db.url</param-name>
        <param-value>jdbc:sqlserver://localhost:1433;databaseName=DBFinora;encrypt=true;trustServerCertificate=true</param-value>
    </context-param>
    <context-param>
        <param-name>db.username</param-name>
        <param-value>sa</param-value>
    </context-param>
    <context-param>
        <param-name>db.password</param-name>
        <param-value>1234</param-value>
    </context-param>

    <!-- === KHAI BÁO SERVLET === -->
    <servlet>
        <servlet-name>ProductManagementServlet</servlet-name>
        <!-- Tên logic (tùy đặt), dùng để liên kết với servlet-mapping bên dưới -->
        <servlet-class>product.controller.ProductManagementServlet</servlet-class>
        <!-- Fully Qualified Class Name: package.subpackage.ClassName -->
        <!-- Tomcat dùng tên này để tìm file .class và tạo object Servlet -->
    </servlet>

    <!-- === MAPPING: URL nào → Servlet nào === -->
    <servlet-mapping>
        <servlet-name>ProductManagementServlet</servlet-name>
        <!-- Phải khớp với servlet-name ở trên -->
        <url-pattern>/product-management</url-pattern>
        <!-- Khi URL = .../product-management → gọi ProductManagementServlet -->
        <url-pattern>/product-management/*</url-pattern>
        <!-- * là wildcard: .../product-management/bất_kỳ cũng đến đây -->
    </servlet-mapping>

</web-app>
```

**Luồng routing cụ thể:**

```
URL nhập vào: http://localhost:8080/SWP391_Finora/product-management?keyword=ao
                                    ├─────────────┤ ├─────────────────┤ ├──────┤
                                    Context path   URL pattern         Query string
                                    
Tomcat:
  1. Nhận request cho path: /product-management?keyword=ao
  2. Tra web.xml: /product-management → ProductManagementServlet
  3. Kiểm tra: đã có instance của ProductManagementServlet chưa?
     - Chưa → new ProductManagementServlet() → gọi init()
     - Rồi → dùng lại instance cũ
  4. Gọi servlet.service(request, response)
     → Tự động dispatch sang doGet() (vì là GET request)
```

---

## 9. File 6 — `index.jsp` (View / Giao Diện)

**Đường dẫn:** `web/WEB-INF/views/product-management/index.jsp`

**Mục đích:** Render HTML giao diện cho người dùng. Nhận data từ Servlet qua `request.getAttribute()`.

### 9.1 Khai báo đầu file (Page Directives)

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Directive: chỉ thị cho JSP compiler
     contentType: kiểu nội dung trả về (HTML, mã hóa UTF-8)
     pageEncoding: mã hóa của chính file .jsp này --%>

<%@ page import="product.model.Product, java.util.List, java.text.NumberFormat, java.util.Locale" %>
<%-- Import: giống "import" trong Java file
     Cần import các class muốn dùng trong Scriptlet <% ... %> --%>
```

### 9.2 Scriptlet — code Java nhúng trong JSP

```jsp
<%
    // Lấy data từ Servlet (Servlet đã setAttribute trước khi forward)
    List<Product> products = (List<Product>) request.getAttribute("products");
    // (List<Product>): ép kiểu (cast) từ Object về List<Product>
    // Phải cast vì getAttribute() luôn trả về Object

    int currentPage     = (Integer) request.getAttribute("currentPage");
    int totalPages      = (Integer) request.getAttribute("totalPages");
    String ctx          = request.getContextPath();
    // ctx = "/SWP391_Finora" — dùng để tạo URL tuyệt đối trong HTML

    String keyword      = (String) request.getAttribute("keyword");
    String filterStatus = (String) request.getAttribute("filterStatus");
    String viewMode     = (String) request.getAttribute("viewMode");
    if (viewMode == null) viewMode = "table";

    NumberFormat vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    // Tạo formatter tiền VNĐ: 150000 → "150.000 ₫"
    // Locale("vi", "VN"): ngôn ngữ tiếng Việt, quốc gia Việt Nam
%>
```

### 9.3 Các loại tag trong JSP

```jsp
<%@ ... %>   ← Page Directive: cấu hình cho JSP
<%  ... %>   ← Scriptlet: code Java thuần, không tự in ra
<%= ... %>   ← Expression: tính biểu thức và IN giá trị ra HTML
<%-- ... --%> ← Comment: chú thích (không xuất hiện trong HTML)
```

**Ví dụ thực tế:**
```jsp
<%
    // Scriptlet — câu lệnh (statement), không in gì
    String greeting = "Xin chào";
    int count = products.size();
%>

<p><%= greeting %>, có <%= count %> sản phẩm.</p>
<%-- Expression: in giá trị ra HTML --%>
<%-- Kết quả: <p>Xin chào, có 12 sản phẩm.</p> --%>
```

### 9.4 Vòng lặp hiển thị danh sách

```jsp
<%
    for (Product p : products) {
    // Enhanced for loop (for-each): duyệt từng Product trong List
        String badgeClass = "Active".equalsIgnoreCase(p.getStatus()) ? "badge" : "badge inactive";
        // "Active".equalsIgnoreCase(...): so sánh không phân biệt hoa thường
        // Viết "Active".equals(...) thay vì p.getStatus().equals("Active")
        // → Tránh NullPointerException nếu getStatus() trả về null
%>
    <tr>
        <td>#<%= p.getProductID() %></td>
        <td><strong><%= p.getName() %></strong></td>
        <td><%= vndFormat.format(p.getPrice()) %></td>
        <%-- format() chuyển 150000 → "150.000 ₫" --%>
        <td><span class="<%= badgeClass %>"><%= p.getStatus() %></span></td>
    </tr>
<%
    } // kết thúc for loop
%>
```

### 9.5 Phân trang

```jsp
<% if (totalPages > 1) { %>
    <%-- Chỉ hiển thị thanh phân trang khi có nhiều hơn 1 trang --%>
    <div class="pagination">
<%
    String baseUrl = ctx + "/product-management?view=" + viewMode
        + (keyword != null && !keyword.isBlank() ? "&keyword=" + keyword : "")
        + (filterStatus != null && !filterStatus.isBlank() ? "&status=" + filterStatus : "");
    // Xây dựng URL nền giữ nguyên filter/search khi chuyển trang
    // Ví dụ: /SWP391_Finora/product-management?view=table&keyword=ao

    if (currentPage > 1) { %>
        <a href="<%= baseUrl %>&page=<%= currentPage - 1 %>">&laquo; Trước</a>
    <% }
    for (int i = 1; i <= totalPages; i++) {
        if (i == currentPage) { %>
            <span class="active"><%= i %></span>  <%-- Trang hiện tại --%>
        <% } else { %>
            <a href="<%= baseUrl %>&page=<%= i %>"><%= i %></a>
        <% }
    }
    if (currentPage < totalPages) { %>
        <a href="<%= baseUrl %>&page=<%= currentPage + 1 %>">Tiếp &raquo;</a>
    <% } %>
    </div>
<% } %>
```

### 9.6 Truyền data Java vào JavaScript (Edit Modal)

```jsp
<button onclick="openEditModal(
    '<%= p.getProductID() %>',
    '<%= p.getCategoryID() %>',
    '<%= (p.getName() != null ? p.getName() : "").replace("'", "\\'") %>',
    '<%= p.getPrice().toPlainString() %>'
)">Sửa</button>
<%-- TRICK QUAN TRỌNG: data từ Java nhúng trực tiếp vào JS call --%>
<%-- .replace("'", "\\'") tránh lỗi nếu tên sản phẩm có dấu nháy đơn ' --%>
<%-- Ví dụ tên "Áo trẻ em" → JS: openEditModal('123', ..., 'Áo trẻ em', ...) --%>
```

```javascript
// Hàm JS nhận data từ JSP
function openEditModal(id, catId, name, sku, price, costPrice, alertQty, status) {
    // Điền vào các ô input trong modal form
    document.getElementById('edit-id').value    = id;
    document.getElementById('edit-name').value  = name;
    document.getElementById('edit-price').value = price;
    // ... (các field còn lại tương tự)
    editModal.style.display = 'flex'; // Hiện modal
}
```

---

## 10. Cách Các File Liên Kết Với Nhau

```
web.xml
  └── Khai báo: ProductManagementServlet ← URL /product-management
       │
       └── ProductManagementServlet.java
             │   import → ProductDAO, Product
             │
             ├── init() → new ProductDAO()
             │
             ├── doGet()
             │     ├── productDAO.getTotalCount(keyword, status) → int
             │     ├── productDAO.findAll(offset, limit, keyword, status) → List<Product>
             │     ├── request.setAttribute("products", ...)
             │     └── forward → index.jsp
             │
             └── doPost()
                   ├── buildProductFromRequest() → Product object
                   ├── productDAO.insert(p)    [action=add]
                   ├── productDAO.update(p)    [action=edit]
                   ├── productDAO.delete(id)   [action=delete]
                   └── response.sendRedirect(...)

ProductDAO.java
  │   import → Product, DatabaseUtil
  │
  ├── findAll() → DatabaseUtil.getConnection() → SQL SELECT → List<Product>
  ├── getTotalCount() → DatabaseUtil.getConnection() → SQL COUNT → int
  ├── insert() → DatabaseUtil.getConnection() → SQL INSERT
  ├── update() → DatabaseUtil.getConnection() → SQL UPDATE
  ├── delete() → DatabaseUtil.getConnection() → SQL DELETE
  └── extractProduct(ResultSet) → Product

DatabaseUtil.java
  │   reads config from → web.xml (context-param: db.url, db.password, ...)
  └── getConnection() → DriverManager.getConnection() → SQL Server (DBFinora)

Product.java
  ← Được tạo bởi: ProductDAO.extractProduct() (từ DB → Java)
  ← Được tạo bởi: ProductManagementServlet.buildProductFromRequest() (từ form → Java)
  → Được đọc bởi: index.jsp (p.getName(), p.getPrice(), ...)

index.jsp
  ← Nhận data từ: request.getAttribute("products"), "currentPage", "totalPages", ...
  → Render: bảng sản phẩm, form tìm kiếm, modal thêm/sửa, phân trang
  → Gửi form POST đến: /product-management (quay lại ProductManagementServlet.doPost())
```

---

## 11. Các Tính Năng Đã Triển Khai

| Tính năng | HTTP | URL / Form params | DAO method gọi |
|-----------|------|-------------------|---------------|
| Xem danh sách | GET | `?page=1` | `findAll()`, `getTotalCount()` |
| Tìm kiếm | GET | `?keyword=ao` | `findAll(keyword=ao, ...)` |
| Lọc trạng thái | GET | `?status=Active` | `findAll(..., status=Active)` |
| Phân trang | GET | `?page=2` | `findAll(offset=5, limit=5, ...)` |
| Chuyển chế độ xem | GET | `?view=showcase` | *(chỉ ở JSP, không gọi DB)* |
| Thêm sản phẩm | POST | `action=add, name, sku, ...` | `insert(product)` |
| Sửa sản phẩm | POST | `action=edit, productID, ...` | `update(product)` |
| Xóa sản phẩm | POST | `action=delete, id=5` | `delete(5)` |

---

## 12. Khái Niệm Java Quan Trọng Tổng Hợp

### 4 Trụ Cột OOP trong code này

| Khái niệm | Ví dụ thực tế trong module |
|-----------|--------------------------|
| **Encapsulation** (Đóng gói) | `private` fields + public getters/setters trong `Product.java` |
| **Inheritance** (Kế thừa) | `ProductManagementServlet extends HttpServlet` |
| **Polymorphism** (Đa hình) | `doGet()`, `doPost()` override method của `HttpServlet` |
| **Abstraction** (Trừu tượng) | DAO ẩn chi tiết SQL, Servlet chỉ gọi `.findAll()` mà không biết SQL bên trong |

### Exception Handling (Xử Lý Ngoại Lệ)

```java
try {
    // Code có thể gây lỗi
    int totalCount = productDAO.getTotalCount(keyword, status);
} catch (SQLException e) {
    // Xử lý lỗi cụ thể (lỗi SQL)
    throw new ServletException("Database error", e);
} catch (NumberFormatException ignored) {
    // Có thể bắt nhiều loại exception khác nhau
    // Bắt NumberFormatException khi parseInt() thất bại
}
```

**Hierarchy Exception:**
```
Throwable
├── Error (lỗi hệ thống, không handle)
└── Exception
    ├── RuntimeException (không bắt buộc try-catch)
    │   ├── NullPointerException
    │   ├── NumberFormatException
    │   └── ClassCastException
    └── Checked Exception (BẮT BUỘC try-catch hoặc throws)
        ├── IOException
        └── SQLException ← DAO dùng cái này
```

### Try-with-Resources

```java
// Cách cũ — dễ quên đóng connection → memory leak!
Connection conn = null;
try {
    conn = DatabaseUtil.getConnection();
    // ... dùng conn
} finally {
    if (conn != null) conn.close(); // phải tự đóng
}

// Cách mới (Java 7+) — tự động đóng sau khi ra khỏi block
try (Connection conn = DatabaseUtil.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    // ... dùng conn và stmt
} // conn.close() và stmt.close() được gọi tự động ở đây
  // Kể cả khi có exception xảy ra!
```

### Tại sao dùng PreparedStatement?

```java
// NGUY HIỂM - SQL Injection:
String name = request.getParameter("name"); // "'; DROP TABLE Product; --"
String sql = "SELECT * FROM Product WHERE Name = '" + name + "'";
// SQL thực thi: SELECT * FROM Product WHERE Name = ''; DROP TABLE Product; --'
// Hacker có thể xóa toàn bộ bảng Product!

// AN TOÀN - PreparedStatement:
String sql = "SELECT * FROM Product WHERE Name LIKE ?";
stmt.setString(1, "%" + name + "%");
// ? là placeholder được xử lý riêng biệt với câu SQL
// Dù name = "'; DROP TABLE Product; --"
// SQL Server hiểu đây là dữ liệu, không phải câu lệnh SQL → an toàn!
```

### Static vs Instance

```java
// STATIC: thuộc về CLASS, dùng chung cho mọi object
private static final int ITEMS_PER_PAGE = 5;
// Gọi: ProductManagementServlet.ITEMS_PER_PAGE (không cần new)

public static Connection getConnection() { ... }
// Gọi: DatabaseUtil.getConnection() (không cần new DatabaseUtil())

// INSTANCE: thuộc về từng OBJECT cụ thể
private ProductDAO productDAO;
// Mỗi ProductManagementServlet instance có productDAO riêng

private int productID;
// Mỗi Product instance có productID riêng
```

### String vs StringBuilder

```java
// String: bất biến (immutable), nối nhiều lần tạo nhiều object mới
String s = "";
s = s + "SELECT ";  // Tạo String mới: "SELECT "
s = s + "* ";       // Tạo String mới: "SELECT * "
s = s + "FROM Product"; // Tạo String mới: "SELECT * FROM Product"
// 3 lần nối = 3 String mới được tạo (kém hiệu quả)

// StringBuilder: có thể thay đổi, nối nhiều lần không tạo object mới
StringBuilder sb = new StringBuilder("SELECT ");
sb.append("* ");
sb.append("FROM Product");
// Chỉ 1 object StringBuilder, hiệu quả hơn
String sql = sb.toString(); // Chuyển sang String khi cần
```

---

## Kết Luận

Module Product Management trong KiotRetail là ví dụ điển hình của kiến trúc **MVC layered** trong Java Web:

| File | Vai trò | Tương tác với |
|------|---------|---------------|
| `Product.java` | Data carrier (POJO) | Được tạo và đọc bởi DAO, Servlet, JSP |
| `DatabaseUtil.java` | Kết nối DB | Được gọi bởi DAO, đọc config từ web.xml |
| `ProductDAO.java` | SQL execution | Gọi DatabaseUtil, tạo Product objects |
| `ProductManagementServlet.java` | Logic điều phối | Gọi DAO, forward sang JSP |
| `web.xml` | Routing config | Map URL → Servlet, cung cấp DB config |
| `index.jsp` | HTML rendering | Đọc data từ Servlet, gửi form về Servlet |

**Nguyên tắc cốt lõi được áp dụng:**
- **Single Responsibility**: Mỗi class chỉ làm 1 việc
- **DRY (Don't Repeat Yourself)**: `extractProduct()`, `buildProductFromRequest()` tránh lặp code
- **Encapsulation**: `private` fields + getters/setters
- **No SQL in Servlet/JSP**: SQL chỉ ở DAO
- **PreparedStatement**: chống SQL Injection
- **Try-with-Resources**: tự động đóng Connection
- **PRG Pattern (Post/Redirect/Get)**: tránh double submit



Viewed ProductManagementServlet.java:1-97
Viewed ProductDAO.java:1-137
Viewed ProductManagementService.java:1-7

------------------

### 1. Tiếp nhận tham số từ Request (Controller)
Khi người dùng thực hiện tìm kiếm, lọc hoặc chuyển trang, trình duyệt sẽ gửi các tham số lên `ProductManagementServlet` qua phương thức `doGet`. Các tham số này bao gồm:
- `keyword`: Từ khóa tìm kiếm.
- `status`: Trạng thái để lọc (ví dụ: đang bán, ngừng kinh doanh...).
- `page`: Trang hiện tại đang xem (mặc định là 1 nếu không truyền).
- `view`: Chế độ hiển thị (mặc định là `table`).

### 2. Xử lý Logic Phân trang (Controller)
Servlet sử dụng biến cố định `ITEMS_PER_PAGE = 5` (hiển thị 5 sản phẩm trên mỗi trang).
- Đầu tiên, nó gọi `productDAO.getTotalCount(keyword, status)` để đếm tổng số lượng sản phẩm thỏa mãn điều kiện tìm kiếm và lọc.
- Tính toán tổng số trang: `totalPages = Math.ceil(totalCount / ITEMS_PER_PAGE)`.
- Tính toán vị trí bắt đầu lấy dữ liệu (offset) để truyền xuống DB: `(page - 1) * ITEMS_PER_PAGE`.

### 3. Xử lý Truy vấn Database (DAO)
Lớp `ProductDAO` là nơi thực thi trực tiếp các câu lệnh SQL để truy xuất dữ liệu:
- **Tìm kiếm (Search):** Nếu có `keyword`, nó thêm điều kiện vào câu lệnh SQL sử dụng `LIKE`: `AND (Name LIKE ? OR SKU LIKE ?)`. Từ khóa sẽ được tự động thêm `%` ở 2 đầu (ví dụ: `%keyword%`) để tìm chuỗi chứa từ khóa đó trong Tên sản phẩm hoặc Mã SKU.
- **Lọc (Filter):** Nếu có `status`, nó thêm điều kiện: `AND Status = ?`.
- **Phân trang (Table):** Nó sử dụng cú pháp của SQL Server để lấy chính xác số bản ghi cho trang đó: `ORDER BY ProductID ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY` (Trong đó `OFFSET` là điểm bắt đầu và `FETCH NEXT` là số lượng bản ghi tối đa, tương đương `limit`).

### 4. Truyền dữ liệu về View (JSP)
Sau khi lấy được danh sách `products` từ DAO, Servlet sẽ đặt tất cả các thông tin này vào `request.setAttribute(...)`:
- Danh sách sản phẩm của trang hiện tại (`products`).
- Trang hiện tại (`currentPage`) và tổng số trang (`totalPages`) để vẽ thanh phân trang (nút Next/Prev, số trang...).
- Các tham số `keyword`, `filterStatus`, `viewMode` để khi load lại trang, các ô tìm kiếm và dropdown lọc vẫn giữ nguyên giá trị mà người dùng vừa chọn.
- Cuối cùng `forward` request sang file giao diện `/WEB-INF/views/product-management/index.jsp` để hiển thị thành bảng (table).

**Tóm lại:** Nó hoạt động theo luồng: **Giao diện truyền tham số lên -> Servlet tiếp nhận và tính toán vị trí trang -> DAO nối chuỗi SQL để lọc và lấy đúng 5 sản phẩm của trang đó -> Servlet trả dữ liệu về lại cho Giao diện vẽ thành bảng.**
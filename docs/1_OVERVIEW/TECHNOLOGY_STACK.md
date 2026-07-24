# Technology Stack — FinoraRetail

> **Tên dự án:** FinoraRetail (SWP391_Finora)  
> **Phiên bản:** 1.0  
> **Ngày cập nhật:** 21/06/2026  
> **Ngôn ngữ tài liệu:** Tiếng Việt

---

## 1. Giới thiệu

Tài liệu này mô tả chi tiết toàn bộ các thành phần công nghệ (technology stack) được sử dụng trong hệ thống FinoraRetail. Mỗi thành phần được trình bày với phiên bản sử dụng, vai trò trong kiến trúc hệ thống, và lý do lựa chọn. Việc nắm vững technology stack là điều kiện tiên quyết để tham gia phát triển hoặc bảo trì hệ thống.

---

## 2. Ngôn ngữ lập trình

### 2.1. Java SE 17

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Oracle Corporation |
| **Phiên bản sử dụng** | JDK 17 (Java SE 17) |
| **Loại** | Ngôn ngữ lập trình biên dịch, hướng đối tượng |
| **LTS** | Có (Long-Term Support — phiên bản được hỗ trợ dài hạn) |

**Vai trò trong hệ thống:** Java 17 là ngôn ngữ lập trình chính của toàn bộ backend cho hệ thống FinoraRetail. Toàn bộ mã nguồn Java, bao gồm các Servlet, DAO, Service, Model, và Utility classes, đều được viết bằng Java 17.

**Lý do lựa chọn:** JDK 17 được chọn vì đây là phiên bản LTS ổn định, được Oracle hỗ trợ dài hạn, có hiệu năng cao, và tương thích tốt với Apache Tomcat 10.1 (yêu cầu Java 9 trở lên). JDK 17 cung cấp nhiều cải tiến về ngôn ngữ như `sealed classes`, `pattern matching for switch`, `record classes`, và các API mới trong `java.util`.

**Vị trí mã nguồn:** Tất cả các file Java nằm trong `src/main/java/` với package root `com.storemanagement.*`.

---

## 3. Nền tảng Jakarta EE

### 3.1. Jakarta Servlet API 6.0

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Eclipse Foundation (Jakarta EE) |
| **Phiên bản** | Jakarta Servlet 6.0 |
| **Specification** | `jakarta.servlet:jakarta.servlet-api:6.0.0` |
| **Phụ thuộc** | Yêu cầu Java 9+ |

**Vai trò trong hệ thống:** Jakarta Servlet API là nền tảng cốt lõi cho tầng điều khiển (Controller Layer) của hệ thống. Tất cả 17 controller classes đều kế thừa từ `jakarta.servlet.http.HttpServlet` và override các phương thức `doGet()`, `doPost()` để xử lý các yêu cầu HTTP. AuthFilter cũng implement `jakarta.servlet.Filter`.

**Các đặc điểm quan trọng:**
- Hỗ trợ ServletContext và ServletConfig
- Hỗ trợ session management qua `HttpSession`
- Hỗ trợ request dispatcher (forward/include)
- Hỗ trợ multipart requests cho file uploads
- Hỗ trợ async servlet cho xử lý bất đồng bộ

### 3.2. Jakarta Server Pages (JSP) 3.0

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Eclipse Foundation (Jakarta EE) |
| **Phiên bản** | Jakarta Server Pages 3.0 |
| **Specification** | `jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.0.0` |

**Vai trò trong hệ thống:** JSP là công nghệ trình diễn (presentation technology) chính của hệ thống. 65 file JSP nằm trong thư mục `views/` được sử dụng để render giao diện người dùng. JSP kết hợp với JSTL cho phép nhúng logic trình diễn đơn giản trong HTML markup mà không cần viết scriptlet Java trực tiếp.

**Các đặc điểm quan trọng:**
- Expression Language (EL) `${...}` cho data binding
- Directives: `<%@ page %>`, `<%@ include %>`, `<%@ taglib %>`
- Standard actions: `<jsp:forward>`, `<jsp:include>`, `<jsp:useBean>`
- Custom tags thông qua JSTL

### 3.3. Jakarta Standard Tag Library (JSTL) 3.0

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Eclipse Foundation (Jakarta EE) |
| **Phiên bản** | Jakarta Standard Tag Library 3.0 |
| **Specification** | `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1` |
| **Phụ thuộc** | `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.0` |

**Vai trò trong hệ thống:** JSTL cung cấp bộ thẻ chuẩn (standard tags) phục vụ các tác vụ phổ biến trong JSP như vòng lặp, điều kiện, format ngày tháng, format số, internationalization, và quản lý XML. JSTL là công cụ chính để tránh việc sử dụng Java scriptlet trong JSP.

**Các thư viện thẻ JSTL được sử dụng:**

| Namespace | URI | Mục đích |
|---|---|---|
| Core | `http://java.sun.com/jsp/jstl/core` | Vòng lặp (`<c:forEach>`), điều kiện (`<c:if>`, `<c:choose>`), URL (`<c:url>`) |
| Formatting | `http://java.sun.com/jsp/jstl/fmt` | Format số, ngày tháng, locale |
| Functions | `http://java.sun.com/jsp/jstl/functions` | String functions (`fn:escapeXml`, `fn:contains`) |
| SQL | `http://java.sun.com/jsp/jstl/sql` | SQL queries trong JSP (tránh dùng trong production) |

---

## 4. Server ứng dụng

### 4.1. Apache Tomcat 10.1

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Apache Software Foundation |
| **Phiên bản** | Tomcat 10.1.x |
| **Java version** | Yêu cầu Java 11+ (khuyến nghị Java 17) |
| **Jakarta EE** | Jakarta EE 10 / Servlet 6.0, JSP 3.0 |
| **Protocol** | HTTP/1.1, HTTP/2, AJP |

**Vai trò trong hệ thống:** Apache Tomcat 10.1 đóng vai trò là Servlet container và JSP container, cung cấp môi trường thực thi cho toàn bộ ứng dụng web FinoraRetail. Tomcat nhận các HTTP requests từ client, dispatching đến các Servlet tương ứng, quản lý vòng đời của các đối tượng Servlet và Filter, và render kết quả JSP trả về cho client.

**Lý do lựa chọn:** Tomcat 10.1 là phiên bản đầu tiên của Tomcat hỗ trợ Jakarta EE (thay vì Java EE), phù hợp với Jakarta Servlet 6.0 và JSP 3.0 được sử dụng trong dự án. Tomcat nhẹ, dễ cấu hình, và là lựa chọn tiêu chuẩn cho các ứng dụng web Java không yêu cầu full Java EE application server.

**Cấu hình liên quan:**
- **WAR deployment:** Ứng dụng được đóng gói thành WAR và deploy vào Tomcat
- **Context path:** `/FinoraRetail`
- **Tomcat lib path:** `C:/Tomcat 10.1_Tomcat/lib/` (chứa servlet-api.jar, jsp-api.jar, el-api.jar)
- **Deployment descriptor:** `web/WEB-INF/web.xml`

### 4.2. Context Path và Deployment

Hệ thống FinoraRetail được deploy trên Tomcat với context path là `/FinoraRetail`. Điều này có nghĩa là người dùng truy cập ứng dụng thông qua URL có dạng: `http://localhost:8080/FinoraRetail/...`. Context path được cấu hình trong file deployment descriptor hoặc thông qua cấu hình context trong Tomcat.

---

## 5. Công cụ xây dựng (Build System)

### 5.1. Apache Maven 3.x

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Apache Software Foundation |
| **Phiên bản** | Maven 3.x |
| **Build file** | `pom.xml` tại thư mục gốc dự án |
| **Artifact output** | `target/StoreManagementNetBeans.war` |

**Vai trò trong hệ thống:** Maven là công cụ quản lý xây dựng và phụ thuộc cho dự án FinoraRetail. Maven quản lý tất cả các thư viện bên thứ ba (third-party dependencies), thực hiện quá trình biên dịch mã nguồn, chạy unit tests, và đóng gói ứng dụng thành file WAR.

**Các Maven dependencies chính:**

| Dependency | Phiên bản | Mục đích |
|---|---|---|
| `jakarta.servlet:jakarta.servlet-api` | 6.0.0 | Jakarta Servlet API |
| `jakarta.servlet.jsp:jakarta.servlet.jsp-api` | 3.0.0 | Jakarta JSP API |
| `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api` | 3.0.0 | JSTL API |
| `org.glassfish.web:jakarta.servlet.jsp.jstl` | 3.0.1 | JSTL Implementation |
| `com.microsoft.sqlserver:mssql-jdbc` | 12.6.1.jre11 | SQL Server JDBC Driver |
| `junit:junit` | 4.13.2 | Unit Testing Framework |
| `org.mockito:mockito-core` | 5.8.0 | Mocking Framework cho tests |

**Maven build lifecycle:**

| Phase | Mô tả |
|---|---|
| `validate` | Kiểm tra POM và project structure |
| `compile` | Biên dịch mã nguồn Java từ `src/main/java` |
| `test` | Chạy các unit tests |
| `package` | Đóng gói thành WAR file |
| `install` | Cài đặt package vào local repository |
| `deploy` | Deploy lên remote repository |

**Cấu trúc Maven tiêu chuẩn:**

```
FinoraRetail/
├── src/main/java/         # Mã nguồn Java chính
├── src/main/webapp/        # Web resources (JSP, CSS, JS, web.xml)
├── src/main/resources/     # Resource files (properties, XML configs)
├── src/test/java/         # Unit test sources
├── target/                # Build output (generated)
│   └── StoreManagementNetBeans.war
├── pom.xml                # Maven Project Object Model
└── pom.xml                # Maven Project Object Model
```

---

## 6. Hệ quản trị cơ sở dữ liệu

### 6.1. Microsoft SQL Server

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Microsoft Corporation |
| **Phiên bản** | SQL Server (phiên bản server không giới hạn trong tài liệu này) |
| **Database name** | `DBFinoraV3` |
| **Số lượng bảng** | 21 tables |
| **JDBC Driver** | `com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11` |

**Vai trò trong hệ thống:** SQL Server là hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) duy nhất của hệ thống FinoraRetail. Tất cả dữ liệu nghiệp vụ bao gồm sản phẩm, khách hàng, nhà cung cấp, đơn hàng, thanh toán, và tồn kho được lưu trữ và quản lý trong SQL Server. Kết nối từ ứng dụng Java đến SQL Server được thực hiện thông qua JDBC API.

### 6.2. JDBC Driver

| Thuộc tính | Chi tiết |
|---|---|
| **Driver class** | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| **Maven artifact** | `com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11` |
| **JDBC version** | JDBC 4.2 (tương thích Java 8+) |

**Cách sử dụng:** JDBC driver được khai báo như một dependency trong `pom.xml` và được tự động đóng gói vào WAR file. Kết nối cơ sở dữ liệu được quản lý thông qua `DatabaseUtil` — một utility class cung cấp các phương thức tiện ích để lấy và đóng kết nối JDBC.

**Cấu hình kết nối:** Thông tin kết nối (JDBC URL, username, password) được lưu trong `web/META-INF/context.xml`. Đây là vùng bảo vệ — không được hardcode credentials trong mã nguồn.

### 6.3. Database Schema

Cơ sở dữ liệu `DBFinoraV3` chứa 21 bảng, được thiết kế theo mô hình quan hệ chuẩn hóa (3NF trở lên) để đảm bảo tính toàn vẹn dữ liệu. Các script SQL schema được quản lý trong thư mục `sql/` của repository. Mỗi bảng tương ứng với một thực thể nghiệp vụ chính hoặc bảng liên kết (junction table) phục vụ quan hệ nhiều-nhiều.

---

## 7. Giao diện người dùng (Frontend)

### 7.1. Bootstrap 5

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Twitter, Inc. / Bootstrap Team |
| **Phiên bản** | Bootstrap 5.x |
| **License** | MIT License |
| **CDN** | `https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css` |

**Vai trò trong hệ thống:** Bootstrap 5 là CSS framework chính được sử dụng để xây dựng giao diện người dùng. Tất cả 65 JSP views sử dụng Bootstrap 5 CSS classes để đảm bảo giao diện responsive trên mọi thiết bị (desktop, tablet, mobile). Bootstrap 5 cung cấp hệ thống grid system, typography, form controls, buttons, tables, cards, navigation components, và nhiều utility classes khác.

**Lý do lựa chọn:** Bootstrap 5 là framework CSS phổ biến nhất, có tài liệu phong phú, dễ học, và không yêu cầu JavaScript ( Bootstrap 5 không còn yêu cầu jQuery — điểm khác biệt quan trọng so với Bootstrap 4). Framework này cho phép phát triển giao diện nhanh chóng mà vẫn đảm bảo tính chuyên nghiệp và nhất quán.

**Các thành phần Bootstrap 5 được sử dụng:**

| Thành phần | Mô tả |
|---|---|
| Grid System | Bố cục responsive 12-column |
| Navbar | Thanh điều hướng chính |
| Cards | Hiển thị thông tin sản phẩm, đơn hàng |
| Tables | Danh sách sản phẩm, khách hàng, đơn hàng |
| Forms | Form nhập liệu với validation |
| Modals | Hộp thoại xác nhận, thêm/sửa |
| Buttons | Các nút hành động |
| Badges | Thể hiện trạng thái |
| Pagination | Phân trang danh sách |

### 7.2. Material Icons

| Thuộc tính | Chi tiết |
|---|---|
| **Nhà phát triển** | Google |
| **Phiên bản** | Material Icons (regular) |
| **CDN** | `https://fonts.googleapis.com/icon?family=Material+Icons` |
| **Số lượng biểu tượng** | 9.000+ icons |

**Vai trò trong hệ thống:** Material Icons là bộ biểu tượng (icon set) được sử dụng xuyên suốt hệ thống để tạo giao diện trực quan và dễ hiểu. Các icons được sử dụng trong navigation, buttons, tables, cards, và các thành phần giao diện khác. Việc sử dụng một bộ icon nhất quán giúp tăng tính thẩm mỹ và trải nghiệm người dùng.

**Cách sử dụng trong JSP:**

```html
<!-- Material Icons via Google Fonts -->
<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

<!-- Sử dụng icon trong HTML -->
<span class="material-icons">inventory</span>
<span class="material-icons">shopping_cart</span>
<span class="material-icons">person</span>
```

### 7.3. Vanilla JavaScript (Không có framework JS)

| Thuộc tính | Chi tiết |
|---|---|
| **Ngôn ngữ** | JavaScript (ECMAScript) |
| **Framework** | Không có |
| **Mục đích** | Tương tác client-side đơn giản |

**Vai trò trong hệ thống:** Hệ thống sử dụng JavaScript thuần (vanilla JS) cho các tương tác client-side đơn giản như form validation, toggle visibility, confirm dialogs, và AJAX requests cơ bản. Không có framework JavaScript phía client (như React, Vue, Angular) được sử dụng.

**Lý do lựa chọn:** Dự án tập trung vào server-side rendering với JSP, không yêu cầu SPA (Single Page Application) hay rich interactive UI. Việc sử dụng vanilla JS giảm thiểu độ phức tạp của build pipeline, giảm kích thước bundle, và phù hợp với quy mô dự án hiện tại.

---

## 8. Không sử dụng

### 8.1. ORM Frameworks (Không sử dụng Hibernate / JPA)

Hệ thống FinoraRetail **không sử dụng** Hibernate, JPA, MyBatis, hay bất kỳ ORM framework nào. Toàn bộ tương tác với cơ sở dữ liệu được thực hiện thông qua JDBC API thuần (plain JDBC). Điều này có nghĩa là:

- Các câu lệnh SQL được viết trực tiếp trong các DAO classes
- Không có entity mapping giữa Java objects và database tables
- Connection management được thực hiện thủ công thông qua `DatabaseUtil`
- Result sets được xử lý thủ công để map thành Model objects

**Lý do:** Việc sử dụng JDBC thuần cho phép kiểm soát hoàn toàn câu SQL, tối ưu hóa các truy vấn phức tạp, và giảm phụ thuộc vào framework. Đây là lựa chọn phù hợp cho dự án học tập và các ứng dụng quy mô vừa.

### 8.2. Spring Framework (Không sử dụng)

Hệ thống **không sử dụng** Spring Framework. Thay vào đó, dự án sử dụng kiến trúc Java EE / Jakarta EE thuần với Servlet API. Điều này bao gồm:

- Không có Spring MVC
- Không có Spring Boot
- Không có Spring Data
- Không có Spring Security (sử dụng custom AuthFilter thay thế)

### 8.3. Frontend Frameworks (Không sử dụng React, Vue, Angular)

Hệ thống **không sử dụng** bất kỳ JavaScript frontend framework nào. Giao diện được xây dựng hoàn toàn bằng JSP + Bootstrap 5 + Vanilla JavaScript.

---

## 9. Cấu trúc dự án

### 9.1. Maven Standard Directory Layout

Dự án tuân theo Maven Standard Directory Layout:

```
FinoraRetail/
├── pom.xml                        # Maven Project Object Model
├── src/
│   ├── main/
│   │   ├── java/                  # Java source files
│   │   │   └── com/storemanagement/
│   │   │       ├── controller/    # 17 Servlet controllers
│   │   │       ├── dao/           # 17 Data Access Objects
│   │   │       ├── dto/           # Data Transfer Objects
│   │   │       ├── filter/        # Filters (AuthFilter)
│   │   │       ├── model/         # 19 Domain models
│   │   │       ├── service/       # 19 Service classes
│   │   │       └── util/          # Utility classes (DatabaseUtil, etc.)
│   │   ├── resources/             # Config files, properties
│   │   └── webapp/                # Web resources
│   │       ├── WEB-INF/
│   │       │   ├── web.xml        # Deployment descriptor
│   │       │   └── views/         # 65 JSP views
│   │       ├── META-INF/
│   │       │   └── context.xml    # DB configuration (protected)
│   │       ├── css/                # Custom CSS files
│   │       ├── js/                 # Custom JavaScript files
│   │       └── assets/            # Images, fonts
│   └── test/
│       └── java/                  # Unit tests
└── target/                        # Build output
    └── StoreManagementNetBeans.war
```

### 9.2. Package Root

Tất cả các class Java trong dự án nằm dưới package root `com.storemanagement`. Cấu trúc package theo từng lớp kiến trúc:

| Package | Mô tả | Số lượng class |
|---|---|---|
| `com.storemanagement.controller` | Servlet controllers | 17 |
| `com.storemanagement.dao` | Data Access Objects | 17 |
| `com.storemanagement.model` | Domain models | 19 |
| `com.storemanagement.service` | Business logic services | 19 |
| `com.storemanagement.dto` | Data Transfer Objects | (theo nhu cầu) |
| `com.storemanagement.filter` | Servlet Filters | (AuthFilter) |
| `com.storemanagement.util` | Utility classes | (DatabaseUtil, RolePermissionUtil) |

### 9.3. Web Resources

| Thư mục | Mô tả |
|---|---|
| `web/WEB-INF/views/` | 65 JSP view files — thư mục được bảo vệ, không truy cập trực tiếp từ URL |
| `web/WEB-INF/web.xml` | Deployment descriptor — cấu hình servlets, filters, welcome files |
| `web/META-INF/context.xml` | Tomcat context configuration — cấu hình datasource (protected) |
| `web/css/` | Custom CSS overrides nếu cần |
| `web/js/` | Custom JavaScript files |
| `web/assets/` | Static assets (images, fonts) |

---

## 10. Tóm tắt Technology Stack

| Tầng | Công nghệ | Phiên bản |
|---|---|---|
| **Ngôn ngữ** | Java | JDK 17 |
| **Platform** | Jakarta EE | 10 |
| **Servlet** | Jakarta Servlet | 6.0.0 |
| **JSP** | Jakarta Server Pages | 3.0.0 |
| **Tag Library** | Jakarta Standard Tag Library | 3.0.1 |
| **Server** | Apache Tomcat | 10.1 |
| **Build Tool** | Apache Maven | 3.x |
| **Database** | Microsoft SQL Server | (DBFinoraV3) |
| **JDBC Driver** | mssql-jdbc | 12.6.1.jre11 |
| **CSS Framework** | Bootstrap | 5.x |
| **Icons** | Material Icons (Google) | Regular |
| **Client-side JS** | Vanilla JavaScript | ES6+ |
| **ORM** | Không sử dụng | JDBC thuần |
| **Spring** | Không sử dụng | Jakarta EE thuần |
| **Frontend Framework** | Không sử dụng | JSP + Bootstrap |

---

## 11. Thông tin bổ sung

| Tài liệu liên quan | Mô tả |
|---|---|
| `1_OVERVIEW/SYSTEM_OVERVIEW.md` | Tổng quan hệ thống, mô hình kinh doanh, user roles |
| `2_ARCHITECTURE/FOLDER_STRUCTURE.md` | Giải thích chi tiết cấu trúc thư mục mã nguồn |
| `2_ARCHITECTURE/DEPENDENCY_FLOW.md` | Luồng phụ thuộc giữa các tầng |
| `2_ARCHITECTURE/MODULE_BOUNDARIES.md` | Ranh giới và trách nhiệm từng module |
| `3_RULES/PROTECTED_MODULES.md` | Các vùng mã nguồn được bảo vệ |
| `pom.xml` | Maven configuration với toàn bộ dependencies |

---

*Cập nhật lần cuối: 21/06/2026*

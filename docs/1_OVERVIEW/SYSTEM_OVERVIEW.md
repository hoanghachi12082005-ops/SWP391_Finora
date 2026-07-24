# Tổng quan Hệ thống FinoraRetail

> **Tên dự án:** FinoraRetail (SWP391_Finora)  
> **Phiên bản:** 1.0  
> **Ngày cập nhật:** 21/06/2026  
> **Ngôn ngữ tài liệu:** Tiếng Việt

---

## 1. Giới thiệu chung

### 1.1. Tên dự án và loại ứng dụng

**FinoraRetail** (tên mã nguồn: SWP391_Finora) là một hệ thống quản lý cửa hàng bán lẻ (Retail Store Management System) được phát triển theo mô hình ứng dụng web Java dạng WAR (Web Application Archive). Dự án được xây dựng trên nền tảng Maven với kiến trúc phân lớp MVC (Model-View-Controller), sử dụng Jakarta Servlet/JSP API để xử lý các yêu cầu HTTP từ phía người dùng.

Hệ thống được thiết kế nhằm đáp ứng các nhu cầu vận hành cơ bản của một cửa hàng bán lẻ, bao gồm quản lý sản phẩm, quản lý khách hàng, quản lý nhà cung cấp, quản lý tồn kho, xử lý đơn hàng, thanh toán, và báo cáo thống kê.

### 1.2. Mục tiêu hệ thống

Mục tiêu chính của hệ thống FinoraRetail là cung cấp một giải pháp phần mềm toàn diện giúp các chủ cửa hàng bán lẻ quản lý hoạt động kinh doanh một cách hiệu quả, chính xác và minh bạch. Hệ thống hướng đến việc đơn giản hóa các quy trình nghiệp vụ hàng ngày, giảm thiểu sai sót do thao tác thủ công, và cung cấp các báo cáo kịp thời phục vụ ra quyết định quản lý.

---

## 2. Môi trường vận hành

### 2.1. Nền tảng phần mềm

| Thành phần | Phiên bản / Thông số |
|---|---|
| **Java Development Kit** | JDK 17 |
| **Servlet API** | Jakarta Servlet 6.0 |
| **JSP API** | Jakarta Server Pages 3.0 |
| **JSTL** | Jakarta Standard Tag Library 3.0 |
| **Server ứng dụng** | Apache Tomcat 10.1 |
| **Hệ quản trị CSDL** | Microsoft SQL Server — Database: `DBFinoraV3` |
| **JDBC Driver** | mssql-jdbc 12.6.1.jre11 |
| **Công cụ build** | Apache Maven 3.x |
| **Hệ điều hành đích** | Windows Server / Windows 10+ |

### 2.2. Thông số triển khai

| Thông số | Giá trị |
|---|---|
| **WAR artifact** | `target/StoreManagementNetBeans.war` |
| **Context path** | `/FinoraRetail` |
| **Package gốc** | `com.storemanagement` |
| **Java source root** | `src/main/java` |
| **Web source root** | `src/main/webapp` |

---

## 3. Kiến trúc hệ thống

### 3.1. Mô hình phân lớp

Hệ thống FinoraRetail tuân theo mô hình kiến trúc phân lớp (Layered Architecture) với các tầng rõ ràng và trách nhiệm được phân định cụ thể cho từng tầng. Mỗi tầng chỉ giao tiếp với tầng ngay bên dưới nó, đảm bảo tính ghép nối lỏng lẻo (loose coupling) và dễ bảo trì.

```
┌─────────────────────────────────────────────┐
│           Web Client (Browser)              │
│      Bootstrap 5 + Material Icons           │
└────────────────────┬────────────────────────┘
                     │ HTTP Request/Response
┌────────────────────▼────────────────────────┐
│        Presentation Layer (JSP)             │
│   65 JSP Views dưới thư mục views/          │
└────────────────────┬────────────────────────┘
                     │ Forward / Include
┌────────────────────▼────────────────────────┐
│         Controller Layer (Servlet)          │
│  17 Controller classes (trong package       │
│  controller, extends HttpServlet)          │
└────────────────────┬────────────────────────┘
                     │ Method calls
┌────────────────────▼────────────────────────┐
│          Service Layer (Skeleton)          │
│  19 Service classes (trong package          │
│  service, business logic)                  │
└────────────────────┬────────────────────────┘
                     │ Data access calls
┌────────────────────▼────────────────────────┐
│            DAO Layer (Skeleton)             │
│  17 DAO classes (trong package dao,        │
│  JDBC-based data access)                   │
└────────────────────┬────────────────────────┘
                     │ JDBC / SQL
┌────────────────────▼────────────────────────┐
│         Database: SQL Server               │
│         DBFinoraV3 (21 tables)              │
└─────────────────────────────────────────────┘
```

### 3.2. Các thành phần kiến trúc

#### Tầng trình diễn (Presentation Layer)

Tầng trình diễn được xây dựng hoàn toàn bằng JSP (JavaServer Pages) kết hợp JSTL (Jakarta Standard Tag Library). Hệ thống sử dụng Bootstrap 5 làm framework CSS để đảm bảo giao diện người dùng responsive và thân thiện trên nhiều thiết bị. Material Icons từ Google Fonts được sử dụng làm bộ biểu tượng đồng nhất cho toàn bộ ứng dụng. Tầng này chịu trách nhiệm hiển thị dữ liệu, thu thập đầu vào từ người dùng, và truyền tải các thông báo phản hồi.

#### Tầng điều khiển (Controller Layer)

Tầng điều khiển bao gồm 17 lớp Servlet, mỗi lớp tương ứng với một nhóm chức năng nghiệp vụ cụ thể. Các Servlet này nằm trong package `controller` và kế thành `HttpServlet`. Trong số 17 controller, `AuthController` đã được triển khai hoàn chỉnh với chức năng đăng nhập demo; 16 controller còn lại đang ở trạng thái skeleton (chỉ có cấu trúc cơ bản, chưa triển khai logic nghiệp vụ). Tầng điều khiển đóng vai trò trung gian giữa tầng trình diễn và tầng dịch vụ, tiếp nhận yêu cầu HTTP, xác thực dữ liệu đầu vào, gọi tầng dịch vụ tương ứng, và chuyển tiếp kết quả về tầng trình diễn.

#### Tầng dịch vụ (Service Layer)

Tầng dịch vụ chứa 19 lớp service nằm trong package `service`. Các lớp này hiện đang ở trạng thái skeleton, đóng vai trò định nghĩa ranh giới nghiệp vụ và chuẩn bị cho việc triển khai logic nghiệp vụ trong các giai đoạn phát triển tiếp theo. Khi được triển khai đầy đủ, tầng dịch vụ sẽ chứa các quy tắc nghiệp vụ, validation logic, và là điểm giao tiếp duy nhất giữa tầng điều khiển và tầng truy cập dữ liệu.

#### Tầng truy cập dữ liệu (DAO Layer)

Tầng truy cập dữ liệu bao gồm 17 lớp DAO (Data Access Object) nằm trong package `dao`. Tất cả các DAO hiện đang ở trạng thái skeleton, chỉ định nghĩa cấu trúc interface và các phương thức trừu tượng mà chưa triển khai chi tiết. Khi hoàn thiện, tầng này sẽ chứa toàn bộ logic tương tác với cơ sở dữ liệu SQL Server thông qua JDBC API.

#### Tầng mô hình dữ liệu (Model Layer)

Tầng mô hình dữ liệu chứa 19 lớp Model nằm trong package `model`. Các lớp này đóng vai trò là các POJO (Plain Old Java Object) chứa dữ liệu, được sử dụng để truyền dữ liệu giữa các tầng trong hệ thống. Mỗi model tương ứng với một thực thể nghiệp vụ hoặc một bảng trong cơ sở dữ liệu.

### 3.3. Bộ lọc bảo mật (AuthFilter)

Hệ thống triển khai `AuthFilter` để bảo vệ các tuyến đường (route) yêu cầu xác thực. Hiện tại, AuthFilter bảo vệ 21 pattern tuyến đường khác nhau, đảm bảo rằng chỉ những người dùng đã đăng nhập mới có thể truy cập các tài nguyên được bảo vệ. AuthFilter kiểm tra session của người dùng và chuyển hướng đến trang đăng nhập nếu chưa xác thực.

---

## 4. Chức năng nghiệp vụ cốt lõi

### 4.1. Quản lý sản phẩm (Product Management)

Chức năng quản lý sản phẩm cho phép người dùng thực hiện các thao tác CRUD (Create, Read, Update, Delete) trên danh mục sản phẩm của cửa hàng. Người dùng có thể thêm sản phẩm mới với các thông tin như tên, mô tả, giá bán, giá nhập, số lượng tồn kho, đơn vị tính, và danh mục. Hệ thống hỗ trợ tìm kiếm và lọc sản phẩm theo nhiều tiêu chí khác nhau, giúp người dùng nhanh chóng tìm được sản phẩm cần thiết.

### 4.2. Quản lý khách hàng (Customer Management)

Chức năng quản lý khách hàng cho phép lưu trữ và quản lý thông tin khách hàng, bao gồm họ tên, địa chỉ, số điện thoại, email, và các thông tin liên quan khác. Hệ thống phân loại khách hàng để hỗ trợ các chương trình khuyến mãi và chăm sóc khách hàng khác nhau.

### 4.3. Quản lý nhà cung cấp (Supplier Management)

Chức năng quản lý nhà cung cấp cho phép quản lý danh sách các nhà cung cấp sản phẩm cho cửa hàng. Thông tin nhà cung cấp bao gồm tên công ty, người liên hệ, địa chỉ, số điện thoại, email, và các điều khoản hợp đồng.

### 4.4. Quản lý tồn kho (Inventory Management)

Chức năng quản lý tồn kho theo dõi số lượng sản phẩm hiện có trong kho, các giao dịch nhập xuất hàng hóa, và cảnh báo khi số lượng tồn kho xuống thấp dưới ngưỡng quy định. Hệ thống cung cấp báo cáo tồn kho theo thời gian thực, giúp người quản lý nắm bắt tình trạng hàng hóa một cách chính xác.

### 4.5. Xử lý đơn hàng (Order Processing)

Chức năng xử lý đơn hàng cho phép tạo mới, xem, cập nhật, và hủy đơn hàng. Hệ thống quản lý đầy đủ thông tin đơn hàng bao gồm danh sách sản phẩm, số lượng, đơn giá, tổng tiền, thông tin khách hàng, trạng thái đơn hàng, và lịch sử xử lý.

### 4.6. Thanh toán (Payment Processing)

Chức năng thanh toán xử lý các giao dịch thanh toán cho đơn hàng, bao gồm nhiều phương thức thanh toán khác nhau. Hệ thống ghi nhận thông tin thanh toán, cập nhật trạng thái đơn hàng, và lưu trữ lịch sử giao dịch tài chính.

### 4.7. Báo cáo và thống kê (Reporting)

Hệ thống cung cấp các báo cáo thống kê phục vụ công tác quản lý, bao gồm báo cáo doanh thu, báo cáo tồn kho, báo cáo khách hàng, và các báo cáo chuyên đề khác. Các báo cáo có thể được xem trực tiếp trên giao diện web và hỗ trợ xuất dữ liệu.

---

## 5. Vai trò người dùng và phân quyền

### 5.1. Các vai trò trong hệ thống

Hệ thống FinoraRetail xác định hai vai trò người dùng chính, được phân quyền truy cập thông qua AuthFilter và RolePermissionUtil:

| Vai trò | Mô tả |
|---|---|
| **Chủ cửa hàng (Owner)** | Người có toàn quyền quản trị hệ thống, bao gồm quản lý nhân viên, xem báo cáo tài chính, quản lý cấu hình hệ thống |
| **Nhân viên (Employee)** | Người thực hiện các nghiệp vụ hàng ngày như bán hàng, quản lý tồn kho, tiếp nhận hàng nhập |

### 5.2. Cơ chế phân quyền

AuthFilter kiểm tra session của người dùng để xác định vai trò và quyền truy cập trước khi cho phép truy cập vào các tài nguyên được bảo vệ. RolePermissionUtil cung cấp các phương thức tiện ích để kiểm tra và xác thực quyền hạn cụ thể của người dùng trong từng nghiệp vụ.

---

## 6. Cơ sở dữ liệu

### 6.1. Thông tin kết nối

Hệ thống sử dụng Microsoft SQL Server làm hệ quản trị cơ sở dữ liệu, với database có tên là `DBFinoraV3`. Kết nối được thiết lập thông qua JDBC driver `mssql-jdbc 12.6.1.jre11`. Thông tin kết nối (URL, username, password) được cấu hình trong file `context.xml` tại thư mục `META-INF` và được quản lý như một vùng bảo vệ của hệ thống.

### 6.2. Lược đồ cơ sở dữ liệu

Cơ sở dữ liệu `DBFinoraV3` bao gồm 21 bảng, được thiết kế để lưu trữ toàn bộ dữ liệu nghiệp vụ của hệ thống. Các bảng được phân loại theo chức năng nghiệp vụ tương ứng với các module của hệ thống. Schema và các script SQL được quản lý trong thư mục `sql/` của repository.

### 6.3. Vùng bảo vệ

Cấu hình kết nối cơ sở dữ liệu và các file schema SQL thuộc vùng bảo vệ của hệ thống. Việc sửa đổi các file này phải tuân theo quy trình phê duyệt nghiêm ngặt và được ghi nhận đầy đủ trong biên bản quyết định kiến trúc.

---

## 7. Trạng thái phát triển hiện tại

### 7.1. Tổng quan trạng thái

Hệ thống FinoraRetail đã hoàn thành giai đoạn xây dựng nền tảng (foundation phase). Kiến trúc phân lớp đã được thiết lập, các cấu trúc package và class đã được định nghĩa, và hệ thống có thể triển khai trên Tomcat 10.1. Tuy nhiên, phần lớn các module nghiệp vụ vẫn đang ở trạng thái skeleton, chờ triển khai chi tiết trong các giai đoạn phát triển tiếp theo.

### 7.2. Các thành phần đã hoàn thiện

| Thành phần | Trạng thái |
|---|---|
| Kiến trúc phân lớp MVC | Hoàn thiện |
| Cấu trúc package và source root | Hoàn thiện |
| Maven build configuration (pom.xml) | Hoàn thiện |
| AuthController (demo login) | Hoàn thiện |
| AuthFilter (21 route patterns) | Hoàn thiện |
| 65 JSP Views | Hoàn thiện (cấu trúc) |
| Cơ sở dữ liệu (21 bảng) | Hoàn thiện (schema) |
| Bootstrap 5 + Material Icons | Hoàn thiện |

### 7.3. Các thành phần đang phát triển

| Thành phần | Trạng thái |
|---|---|
| 16 Controller (ngoại trừ AuthController) | Skeleton |
| 17 DAO classes | Skeleton |
| 19 Service classes | Skeleton |
| Logic nghiệp vụ chi tiết | Chưa triển khai |

---

## 8. Khả năng hệ thống tổng thể

### 8.1. Tính năng đã xác định

Hệ thống FinoraRetail được thiết kế để cung cấp các khả năng sau:

**Quản lý sản phẩm toàn diện:** Cho phép quản lý danh mục sản phẩm với đầy đủ thông tin, hỗ trợ tìm kiếm và lọc nâng cao.

**Quản lý quan hệ khách hàng và nhà cung cấp:** Lưu trữ và quản lý thông tin đối tác kinh doanh một cách có hệ thống.

**Theo dõi tồn kho thời gian thực:** Cung cấp cái nhìn tức thì về tình trạng hàng hóa trong kho.

**Xử lý đơn hàng linh hoạt:** Hỗ trợ toàn bộ vòng đời đơn hàng từ tạo mới đến hoàn thành hoặc hủy.

**Thanh toán đa phương thức:** Xử lý các giao dịch thanh toán với nhiều hình thức khác nhau.

**Báo cáo thông minh:** Cung cấp các báo cáo và biểu đồ phục vụ ra quyết định quản lý.

**Bảo mật theo vai trò:** Kiểm soát truy cập dựa trên vai trò người dùng với AuthFilter.

### 8.2. Ràng buộc và giả định

- Hệ thống được thiết kế cho một cửa hàng bán lẻ đơn lẻ (single-store deployment).
- Không hỗ trợ đa tenant hoặc triển khai cloud-native trong phiên bản hiện tại.
- Không sử dụng ORM framework; tương tác cơ sở dữ liệu thông qua JDBC thuần.
- Không có API JSON RESTful trong phiên bản hiện tại; giao diện web là cổng giao tiếp chính.

---

## 9. Thông tin bổ sung

### 9.1. Liên quan đến tài liệu khác

| Tài liệu liên quan | Mô tả |
|---|---|
| `1_OVERVIEW/TECHNOLOGY_STACK.md` | Chi tiết về các thành phần công nghệ |
| `2_ARCHITECTURE/FOLDER_STRUCTURE.md` | Giải thích cấu trúc thư mục mã nguồn |
| `2_ARCHITECTURE/MODULE_BOUNDARIES.md` | Ranh giới và trách nhiệm từng module |
| `3_RULES/PROTECTED_MODULES.md` | Các vùng bảo vệ trong mã nguồn |
| `8_STATUS/CURRENT_STATUS.md` | Trạng thái chi tiết của dự án |
| `5_PLANNING/ROADMAP.md` | Lộ trình phát triển tổng thể |

---

*Cập nhật lần cuối: 21/06/2026*

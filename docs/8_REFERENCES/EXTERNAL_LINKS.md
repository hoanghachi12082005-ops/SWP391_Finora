# Liên Kết Tham Khảo - External Links

> **Mục đích:** Tài liệu này tổng hợp các liên kết tham khảo nội bộ và bên ngoài liên quan đến dự án FinoraRetail (SWP391_Finora). Mục tiêu là cung cấp một danh sách tài nguyên đáng tin cậy, có kiểm soát, giúp thành viên phát triển nhanh chóng tìm được nguồn chính thức của các công nghệ đang sử dụng.

---

## 1. Tham Chiếu Nội Bộ (Internal References)

> **Quan trọng:** Các file bên dưới là tham chiếu nội bộ của dự án. Chúng phải được duy trì cập nhật cùng với mã nguồn. Khi thay đổi cấu hình hoặc cấu trúc, các tham chiếu này phải được cập nhật tương ứng.

### 1.1. Cấu Hình Build và Deployment

| File | Mô tả | Duy trì bởi |
|---|---|---|
| `pom.xml` | Cấu hình Maven: dependencies, plugins, build target | Developer |
| `web/WEB-INF/web.xml` | Servlet configuration, URL mappings, welcome files, filters | Developer |
| `web/META-INF/context.xml` | Tomcat context: data source, database credentials, connection pool | Developer |
| `build.xml` | Cấu hình Ant build (nếu có) | Developer |

### 1.2. Cơ Sở Dữ Liệu

| File | Mô tả | Duy trì bởi |
|---|---|---|
| `database/DBFInoraV2.sql` | Schema database: tables, constraints, indexes, stored procedures | Developer |
| `sql/` | Các script SQL bổ sung (migrations, seed data) | Developer |

### 1.3. Tài Liệu Governance

| File | Mô tả |
|---|---|
| `docs/README.md` | Chỉ mục chính của toàn bộ tài liệu |
| `docs/6_DEVELOPMENT/CODING_STANDARDS.md` | Tiêu chuẩn lập trình |
| `docs/6_DEVELOPMENT/AI_WORKFLOW.md` | Quy trình làm việc cho agent AI |
| `docs/6_DEVELOPMENT/NAMING_CONVENTIONS.md` | Quy ước đặt tên |
| `docs/6_DEVELOPMENT/REFACTOR_POLICY.md` | Chính sách tái cấu trúc |
| `docs/architecture/SYSTEM_ARCHITECTURE.md` | Kiến trúc hệ thống |
| `docs/architecture/MODULE_BOUNDARIES.md` | Ranh giới giữa các module |
| `docs/architecture/FOLDER_STRUCTURE.md` | Cấu trúc thư mục |
| `docs/rules/PROTECTED_MODULES.md` | Danh sách module được bảo vệ |
| `docs/security/SECURITY_RULES.md` | Quy tắc bảo mật |
| `docs/status/CURRENT_STATUS.md` | Trạng thái hiện tại dự án |
| `docs/status/IMPLEMENTED_FEATURES.md` | Tính năng đã triển khai |
| `docs/status/TECH_DEBT.md` | Kỹ thuật nợ |
| `AGENTS.md` | Hợp đồng vận hành cho agent AI |

---

## 2. Thư Mục Cấm Lưu Trữ

**Tuyệt đối không lưu trữ các loại thông tin sau trong repository hoặc tài liệu dự án:**

### 2.1. Thông Tin Nhạy Cảm

| Loại | Ví dụ | Lý do cấm |
|---|---|---|
| Credentials | Username, password database, API key, token | Rủi ro bảo mật nghiêm trọng |
| Secrets | JWT secret, encryption key, private key | Có thể bị lộ nếu commit vào git |
| Production credentials | Server IP, production DB connection string | Không bao giờ đưa vào source control |

### 2.2. Nội Dung Tạm Thời

| Loại | Xử lý |
|---|---|
| Ghi chú tạm (draft notes) | Xóa sau khi hoàn thành task |
| Brainstorming content | Chuyển thành plan document hoặc xóa |
| TODO/FIXME không rõ ngữ cảnh | Chuyển thành task trong backlog |
| Nội dung thử nghiệm (test code thất bại) | Xóa hoặc ghi rõ là test draft |

### 2.3. Liên Kết Không Bền Vững

- Liên kết tới tài liệu nội bộ không có version control.
- Liên kết tới forum, Stack Overflow (sử dụng nguồn chính thức thay thế).
- Liên kết có thể hết hạn mà không có mirror.

---

## 3. Dependencies Bên Ngoài

### 3.1. Frontend Libraries (CDN)

| Thư viện | Phiên bản | CDN URL | Mục đích |
|---|---|---|---|
| Bootstrap | 5.3.0 | `https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css` | CSS framework, responsive layout |
| Bootstrap JS | 5.3.0 | `https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js` | JavaScript components, dropdown, modal |
| jQuery | 3.7.0 | `https://code.jquery.com/jquery-3.7.0.min.js` | DOM manipulation, AJAX (nếu cần) |
| Material Icons | — | `https://fonts.googleapis.com/icon?family=Material+Icons` | Icon library |
| Google Fonts (Inter) | — | `https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600` | Typography |
| Google Fonts (Manrope) | — | `https://fonts.googleapis.com/css2?family=Manrope:wght@600;700;800` | Display typography |

### 3.2. Jakarta EE (Server-side)

| Component | Phiên bản | Nguồn | Mục đích |
|---|---|---|---|
| Jakarta Servlet API | 6.0.0 | `jakarta.servlet:jakarta.servlet-api:6.0.0` (Maven) | Servlet development |
| Jakarta JSP API | 3.1.1 | `jakarta.servlet.jsp-api:3.1.1` (Maven) | JSP development |
| Jakarta EL API | 5.0.1 | `jakarta.el:jakarta.el-api:5.0.1` (Maven) | Expression Language |
| JSTL | 2.0 | `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:2.0.0` + `org.glassfish.web:jakarta.servlet.jsp.jstl:2.0.0` | JSP Standard Tag Library |

### 3.3. Database Driver

| Driver | Phiên bản | Maven Coordinates | Mục đích |
|---|---|---|---|
| Microsoft JDBC Driver for SQL Server | 12.6.1 | `com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11` | Kết nối Java → SQL Server |

### 3.4. Logging

| Library | Phiên bản | Maven Coordinates | Mục đích |
|---|---|---|---|
| SLF4J API | 2.0.x | `org.slf4j:slf4j-api:2.0.x` | Logging facade |
| SLF4J Simple | 2.0.x | `org.slf4j:slf4j-simple:2.0.x` | SLF4J implementation đơn giản |

### 3.5. Testing

| Library | Phiên bản | Maven Coordinates | Mục đích |
|---|---|---|---|
| JUnit | 5.x | `org.junit.jupiter:junit-jupiter-api:5.x` | Unit testing framework |
| Mockito | 5.x | `org.mockito:mockito-core:5.x` | Mocking framework |

---

## 4. Tài Liệu Tham Khảo Công Nghệ

### 4.1. Jakarta EE / Java EE

| Nguồn | URL |
|---|---|
| Jakarta EE Official Documentation | `https://jakarta.ee/specifications/` |
| Jakarta Servlet Specification | `https://jakarta.ee/specifications/servlet/` |
| Jakarta JSP Specification | `https://jakarta.ee/specifications/pages/` |
| JSTL Specification | `https://jakarta.ee/specifications/tags/` |

### 4.2. SQL Server

| Nguồn | URL |
|---|---|
| SQL Server Documentation | `https://learn.microsoft.com/en-us/sql/` |
| JDBC Driver Documentation | `https://learn.microsoft.com/en-us/sql/connect/jdbc/` |

### 4.3. Maven

| Nguồn | URL |
|---|---|
| Maven Central Repository | `https://search.maven.org/` |
| Maven POM Reference | `https://maven.apache.org/pom.html` |

### 4.4. Bootstrap

| Nguồn | URL |
|---|---|
| Bootstrap Documentation | `https://getbootstrap.com/docs/5.3/` |
| Bootstrap Examples | `https://getbootstrap.com/docs/5.3/examples/` |

---

## 5. Hướng Dẫn Sử Dụng Tài Liệu Này

### 5.1. Khi Thêm Dependency Mới

1. Kiểm tra xem dependency đã có trong danh sách trên chưa.
2. Nếu chưa, thêm vào bảng tương ứng với đầy đủ thông tin.
3. Cập nhật `pom.xml` với dependency mới.
4. Ghi chú lý do thêm dependency trong commit message.

### 5.2. Khi Cập Nhật Phiên Bản

1. Kiểm tra changelog của phiên bản mới.
2. Đánh giá backward compatibility.
3. Cập nhật phiên bản trong bảng trên.
4. Chạy full build (`mvn clean package`) sau khi cập nhật.
5. Cập nhật `docs/status/TECH_DEBT.md` nếu có breaking changes.

### 5.3. Khi Phát Hiện Liên Kết Hết Hạn

1. Tìm liên kết mới hoặc nguồn thay thế chính thức.
2. Cập nhật liên kết trong tài liệu.
3. Nếu không tìm được thay thế, ghi nhận và báo cáo trong team.

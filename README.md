<div align="center">

# 🛒 FinoraRetail (SWP391_Finora)

### *Hệ Thống Quản Lý Bán Lẻ & Chuỗi Cửa Hàng Đa Chi Nhánh Thế Hệ Mới*

[![Java 17](https://img.shields.io/badge/Java-17%2B-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Jakarta EE 10](https://img.shields.io/badge/Jakarta_EE-10.0-EC2027?style=for-the-badge&logo=jakartaee&logoColor=white)](https://jakarta.ee/)
[![Apache Tomcat](https://img.shields.io/badge/Apache_Tomcat-10.1%2B-F8DC75?style=for-the-badge&logo=apache-tomcat&logoColor=black)](https://tomcat.apache.org/)
[![SQL Server DBFinoraV3](https://img.shields.io/badge/SQL_Server-DBFinoraV3-CC292B?style=for-the-badge&logo=microsoftsqlserver&logoColor=white)](https://www.microsoft.com/sql-server)
[![Maven Build](https://img.shields.io/badge/Maven-Build_Passing-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Security Filter](https://img.shields.io/badge/Bảo_mật-RBAC_%7C_CSRF_%7C_BCrypt-2ea44f?style=for-the-badge&logo=springsecurity&logoColor=white)](#-kiến-trúc-bảo-mật--rbac)

---

[🚀 Hướng Dẫn Chạy Dự Án Chi Tiết](#-hướng-dẫn-cài-đặt--vận-hành-chi-tiết) • [✨ Tính Năng Nổi Bật](#-tính-năng-nổi-bật) • [🔄 Luồng Hoạt Động (JSP-JS-Java-DB)](#-luồng-hoạt-động-chi-tiết-jsp--js--java--db) • [🔒 Bảo Mật & RBAC](#-kiến-trúc-bảo-mật--rbac) • [📚 Tài Liệu Dự Án](#-bộ-tài-liệu-dự-án-docs)

</div>

---

## 📖 Giới Thiệu Dự Án

**FinoraRetail** (`SWP391_Finora`) là hệ thống phần mềm quản trị doanh nghiệp bán lẻ & chuỗi cửa hàng (ERP / POS / WMS) đa chi nhánh. Ứng dụng xử lý mượt mà toàn bộ quy trình: **Bán hàng POS tại quầy**, **Thanh toán QR VNPay**, **Điều chuyển tồn kho & Kiểm kê**, **Quản lý phiếu nhập/xuất kho**, **Báo cáo doanh thu & xuất dữ liệu Excel/PDF**, **Phân quyền vai trò tài khoản & Lịch sử hoạt động (Audit Log)**.

Ứng dụng được xây dựng theo kiến trúc phân lớp chuẩn mực **MVC + DAO + Service Layer**, chạy trên môi trường **Java 17** và **Jakarta Servlet 6.0 (Tomcat 10.1+)**, kết nối cơ sở dữ liệu **Microsoft SQL Server (`DBFinoraV3`)**.

---

## ✨ Tính Năng Nổi Bật

<table>
  <tr>
    <td width="50%">
      <h3>🏬 Quản Lý Đa Chi Nhánh & Kho Bãi</h3>
      <ul>
        <li><b>Điều chuyển tồn kho (Stock Transfer):</b> Luồng đề xuất, phê duyệt và xác nhận nhập kho giữa các chi nhánh.</li>
        <li><b>Kiểm kê định kỳ (Stocktaking):</b> Cân bằng kho, đối soát chênh lệch giữa số lượng thực tế và hệ thống.</li>
        <li><b>Phân quyền kho (Receipt Isolation):</b> Cách ly dữ liệu phiếu nhập/xuất kho theo đúng chi nhánh được phân công.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>💳 Bán Hàng POS & Thanh Toán VNPay</h3>
      <ul>
        <li><b>Màn hình POS tối ưu:</b> Tìm kiếm sản phẩm siêu tốc, quản lý giỏ hàng realtime, áp dụng voucher giảm giá.</li>
        <li><b>Cổng thanh toán VNPay:</b> Tích hợp mã QR VNPay, checksum HMAC-SHA512, tự động xử lý IPN & Return callback.</li>
        <li><b>In hóa đơn & Đơn hàng:</b> Tự động tính thuế VAT, điểm thưởng tích lũy cho khách hàng thân thiết.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>📊 Báo Cáo Tài Chính & Doanh Thu</h3>
      <ul>
        <li><b>Thống kê kinh doanh:</b> Biểu đồ KPI doanh thu, chi phí, lợi nhuận theo ca làm việc (Shift) và chi nhánh.</li>
        <li><b>Xuất báo cáo đa định dạng:</b> Xuất dữ liệu Excel chuẩn với <b>Apache POI 5.2.5</b> và tệp PDF với <b>OpenPDF 1.3.39</b>.</li>
        <li><b>Sổ nhật ký thu chi:</b> Quản lý chi tiết phiếu thu, phiếu chi, hóa đơn thanh toán.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>🛡 Phân Quyền Vận Hành & Audit Log</h3>
      <ul>
        <li><b>Ma trận phân quyền (RBAC):</b> 5 nhóm vai trò chi tiết: <code>owner</code>, <code>admin</code>, <code>storemanager</code>, <code>warehousestaff</code>, <code>salesstaff</code>.</li>
        <li><b>Bảo mật toàn diện:</b> Mã hóa mật khẩu BCrypt, chống tấn công CSRF, cấu hình Security Headers chống XSS/Framing.</li>
        <li><b>Nhật ký hoạt động (Activity Log):</b> Ghi vết tự động mọi thao tác hệ thống và cảnh báo truy cập trái phép qua <code>sp_set_session_context</code>.</li>
      </ul>
    </td>
  </tr>
</table>

---

## 🔄 Luồng Hoạt Động Chi Tiết (JSP ➔ JS ➔ Java ➔ DB)

Mọi yêu cầu từ trình duyệt được xử lý khép kín qua các lớp được phân định rõ ràng:

```mermaid
sequenceDiagram
    autonumber
    actor User as Client (Browser / POS)
    participant View as JSP View / JS (assets/js/)
    participant Filter as SecurityFilter (/*)
    participant Controller as Servlet Controller (@WebServlet)
    participant Service as Service Layer
    participant DAO as DAO Class (DBContext)
    participant DB as SQL Server (DBFinoraV3)

    User->>View: 1. Thao tác trên UI (Click button / Submit form / AJAX Fetch)
    View->>Filter: 2. Gửi Request + Session Cookie & CSRF Token (Header / Param)
    Filter->>Filter: 3. Kiểm tra Session Auth, Role Matrix & Validate CSRF Token
    alt Không hợp lệ (Chưa đăng nhập / Không đúng vai trò / Sai CSRF Token)
        Filter-->>User: Trả về 401 Unauthorized / 403 Forbidden (Ghi Audit Log)
    else Hợp lệ
        Filter->>DB: Set Session Context: EXEC sp_set_session_context N'EmployeeID', empId
        Filter->>Controller: Chuyển tiếp Request đến Controller thích hợp
        Controller->>Service: Gọi xử lý logic nghiệp vụ
        Service->>DAO: Thực thi phương thức truy xuất dữ liệu
        DAO->>DB: Thực thi PreparedStatement SQL trên DBFinoraV3
        DB-->>DAO: Trả về ResultSet
        DAO-->>Service: Ánh xạ thành Domain Model (POJO)
        Service-->>Controller: Trả về DTO / Model kết quả
        Controller->>View: req.setAttribute() & Forward tới JSP View
        View-->>User: Trình duyệt hiển thị kết quả HTML JSTL
    end
```

---

## 🛠 Thư viện & Công Nghệ Sử Dụng

### Backend & Core Frameworks
* **Ngôn ngữ:** Java SE 17 (JDK 17)
* **Web Standard:** Jakarta Servlet API 6.0.0 (Jakarta EE 10) & JSP JSTL 3.0 / GlassFish 3.0.1
* **Web Server:** Apache Tomcat 10.1+
* **Build Tool:** Apache Maven 3.x (`pom.xml`)

### Database & Security Libraries
* **Cơ sở dữ liệu:** Microsoft SQL Server (Database: `DBFinoraV3`)
* **Script CSDL chuẩn:** [docs/3_DATABASE/Finora.sql](file:///d:/Thangdev/SWP/SWP391_Finora-thang/docs/3_DATABASE/Finora.sql)
* **JDBC Driver:** `mssql-jdbc` 12.6.1.jre11
* **Mã hóa mật khẩu:** `jbcrypt` 0.4 (BCrypt Hashing)
* **Xử lý Excel:** Apache POI 5.2.5 (`poi` & `poi-ooxml`)
* **Xử lý PDF:** OpenPDF 1.3.39
* **Email SMTP:** Jakarta Mail 2.0.1
* **Kiểm thử:** JUnit 5 Jupiter 5.10.1

---

## 📁 Tổ Chức Thu Mục Mã Nguồn (`src/main/java/`)

```text
src/main/java/
├── constant/          # Hằng số toàn ứng dụng (AppConstants.java)
├── controller/        # 30+ Servlets xử lý HTTP Requests (phân theo domain)
│   ├── auth/          # AuthServlet.java (/login, /logout, /forgot-password)
│   ├── branch/        # BranchController.java
│   ├── customer/      # CustomerController.java
│   ├── dashboard/     # DashboardController.java
│   ├── finance/       # IncomeExpenseController, PaymentInvoiceController
│   ├── inventory/     # StockController, TransferController, InventoryCheckController...
│   ├── pos/           # PosController, CartServlet, CheckoutServlet
│   ├── product/       # ProductController, CategoryServlet
│   ├── sales/         # SalesServlet, RevenueServlet, ShiftServlet
│   ├── system/        # ActivityLogController, SystemController
│   ├── user/          # AdminUserServlet, OwnerUserServlet, ManagerEmployeeServlet...
│   └── vnpay/         # VNPayServlet, VNPayResultServlet, VNPayReturnServlet
├── dao/               # Data Access Objects (truy vấn DBFinoraV3, kế thừa DBContext)
├── dto/               # Data Transfer Objects (inventory DTOs, report filters)
├── filter/            # SecurityFilter.java (Bộ lọc bảo mật central /*)
├── model/             # 51 Domain POJO Entities (Product, Order, Employee, Inventory...)
├── service/           # Tầng nghiệp vụ trung gian (customer, inventory, finance, system...)
└── util/              # Tiện ích hệ thống (DBContext, PasswordUtil, ExcelImportUtil, MoneyUtil...)
```

---

## 🔒 Kiến Trúc Bảo Mật & RBAC

Bảo mật ứng dụng được quản lý tập trung qua **`filter.SecurityFilter`** (`urlPatterns = {"/*"}`):

### 1. Phân Quyền Role Matrix (`ROLE_MAP`)
| Đường dẫn URL | Vai trò được phép truy cập |
|---|---|
| `/system/*`, `/admin/*`, `/configuration/*`, `/activity/*` | `admin`, `owner` |
| `/management/*`, `/manager/*`, `/branch` | `admin`, `owner`, `storemanager` |
| `/inventory/*`, `/warehouse/*`, `/product/*`, `/products`, `/supplier`, `/purchase/*` | `admin`, `owner`, `storemanager`, `warehousestaff` |
| `/pos/*`, `/customer/*`, `/sales/*`, `/cart/*`, `/checkout/*`, `/orders/*`, `/shift/*` | `admin`, `owner`, `storemanager`, `salesstaff` |
| `/owner/*` | `admin`, `owner`, `storemanager`, `salesstaff`, `warehousestaff` |

### 2. Các Tính Năng Bảo Mật Bắt Buộc
- **Xác thực CSRF:** Mọi form gửi qua phương thức `POST` đều phải chứa `csrfToken` hoặc header `X-CSRF-Token` hợp lệ.
- **Mã hóa BCrypt:** Mật khẩu người dùng được băm an toàn với salt ngẫu nhiên qua `PasswordUtil.java`.
- **Security Headers:** Đặt tự động `Cache-Control: no-cache, no-store`, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy: same-origin`.
- **Nhật ký Audit:** Tự động ghi nhận lịch sử thất bại hoặc vi phạm quyền truy cập vào bảng `ActivityLogs` và thiết lập `sp_set_session_context` cho DB Triggers.

---

## 🚀 Hướng Dẫn Cài Đặt & Vận Hành Chi Tiết

### 📋 Yêu Cầu Tiền Đề
- **Java Development Kit (JDK):** Version 17+ (đặt biến môi trường `JAVA_HOME`)
- **Build Tool:** Apache Maven 3.8+ (`mvn -v`)
- **Application Server:** Apache Tomcat 10.1+ (Hỗ trợ Jakarta EE 10 / Servlet 6.0)
- **Database Server:** Microsoft SQL Server 2019+

---

### Bước 1: Khởi Tạo Cơ Sở Dữ Liệu SQL Server (`DBFinoraV3`)

1. Mở **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Kết nối tới SQL Server instance của bạn (ví dụ `localhost:1433` hoặc IP server `160.191.242.124`).
3. Mở file script SQL chuẩn tại đường dẫn:
   ```file
   docs/3_DATABASE/Finora.sql
   ```
4. Thực thi toàn bộ script SQL (Script sẽ tự động chạy `CREATE DATABASE [DBFinoraV3]`, tạo 21 bảng, bổ sung stored procedures, triggers audit log và chèn dữ liệu mẫu).
5. Kiểm tra file kết nối CSDL tại [src/main/java/util/database/DBContext.java](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/util/database/DBContext.java):
   - Mặc định kết nối tới database `DBFinoraV3`.
   - Có thể cấu hình thông qua biến môi trường hệ thống:
     * `DB_URL` (ví dụ: `jdbc:sqlserver://localhost:1433;databaseName=DBFinoraV3;encrypt=false;trustServerCertificate=true;`)
     * `DB_USER` (mặc định: `sa`)
     * `DB_PASSWORD` (mặc định: `a12345A@`)
6. **Kiểm tra kết nối CSDL:** Chạy hàm `main` trong `DBContext.java` bằng lệnh:
   ```powershell
   mvn exec:java -Dexec.mainClass="util.database.DBContext"
   ```
   Nếu màn hình báo `Connection to SQL Server successful.` là kết nối thành công!

---

### Bước 2: Biên Dịch & Đóng Gói Dự Án (Maven)

Mở Terminal / PowerShell tại thư mục gốc dự án:

```powershell
# Clean và build đóng gói file WAR
mvn clean package -DskipTests
```

Sau khi quá trình build hoàn tất (`BUILD SUCCESS`), tệp WAR đã đóng gói sẽ nằm tại:
`target/StoreManagementNetBeans.war`

---

### Bước 3: Triển Khai Lên Web Server (Apache Tomcat 10.1+)

Bạn có thể chạy ứng dụng theo 4 cách dưới đây:

#### Cách 1: Sử dụng NetBeans IDE (Khuyên dùng)
1. Mở NetBeans IDE 17+.
2. Chọn `File` ➔ `Open Project` ➔ Chọn thư mục dự án `SWP391_Finora-thang`.
3. Vào tab `Services` ➔ `Servers` ➔ Thêm **Apache Tomcat 10.1+**.
4. Nhấn phím `F6` (hoặc click phải dự án ➔ `Run`). NetBeans sẽ tự động build và deploy lên Tomcat với context path `/StoreManagementNetBeans` hoặc `/FinoraRetail`.

#### Cách 2: Sử dụng IntelliJ IDEA Ultimate
1. Mở dự án trong IntelliJ IDEA.
2. Click `Run` ➔ `Edit Configurations...` ➔ Click `+` ➔ Chọn `Tomcat Server` ➔ `Local`.
3. Trong tab `Server`: Chọn Tomcat 10.1 Server.
4. Trong tab `Deployment`: Click `+` ➔ Chọn `Artifact...` ➔ Chọn `StoreManagementNetBeans:war exploded`.
5. Đặt `Application context` thành `/FinoraRetail`.
6. Nhấn `Run` (Shift + F10).

#### Cách 3: Sử dụng VS Code + SmartTomcat Extension
1. Cài đặt Extension **SmartTomcat** trong VS Code.
2. Click biểu tượng SmartTomcat bên thanh sidebar ➔ Select Tomcat Directory (Tomcat 10.1).
3. Set `Set Config Directory`: `src/main/webapp`.
4. Set `Context Path`: `/FinoraRetail`.
5. Click `Run`.

#### Cách 4: Triển Khai Thủ Công Lên Tomcat Độc Lập (Manual Deploy)
1. Copy file `target/StoreManagementNetBeans.war` vào thư mục `webapps/` của Tomcat:
   ```powershell
   Copy-Item target/StoreManagementNetBeans.war -Destination "C:\Tomcat 10.1\webapps\FinoraRetail.war"
   ```
2. Khởi động Tomcat bằng cách chạy lệnh:
   ```powershell
   C:\Tomcat 10.1\bin\startup.bat
   ```
3. Tomcat sẽ tự giải nén file WAR và chạy ứng dụng.

---

### Bước 4: Truy Cập & Kiểm Thử Ứng Dụng

Mở trình duyệt web và truy cập địa chỉ:
```text
http://localhost:8080/FinoraRetail/login
```
*(Hoặc `http://localhost:8080/StoreManagementNetBeans/login` tùy thuộc vào Context Path đã cấu hình)*

#### 🔑 Tài khoản mẫu cho từng Vai trò (xem dữ liệu trong `Finora.sql`):
* **Owner / Admin (Chủ hệ thống / Quản trị):**
  - Route: `/dashboard/owner`, `/system/*`, `/finance/*`
* **Store Manager (Quản lý cửa hàng):**
  - Route: `/dashboard/`, `/management/*`, `/sales/*`, `/branch`
* **Warehouse Staff (Nhân viên kho):**
  - Route: `/inventory/*`, `/warehouse/*`, `/supplier`, `/purchase/*`
* **Sales Staff (Nhân viên bán hàng):**
  - Route: `/pos/*`, `/sales/*`, `/customer/*`, `/cart/*`, `/checkout/*`

---

## 📚 Bộ Tài Liệu Dự Án (`docs/`)

Toàn bộ tài liệu chi tiết của dự án được duy trì đồng bộ 100% với mã nguồn:

| Thư mục tài liệu | Mô tả nội dung |
|---|---|
| 📜 **[AGENTS.md](file:///d:/Thangdev/SWP/SWP391_Finora-thang/AGENTS.md)** | **Hợp đồng quy tắc làm việc bắt buộc cho AI Agent & Lập trình viên** |
| 📑 **[docs/README.md](file:///d:/Thangdev/SWP/SWP391_Finora-thang/docs/README.md)** | Chỉ mục tổng quan bộ tài liệu |
| 🌐 **`docs/1_OVERVIEW/`** | System Overview & Technology Stack chi tiết |
| 🏛 **`docs/2_ARCHITECTURE/`** | Sơ đồ kiến trúc MVC, FOLDER_STRUCTURE & DEPENDENCY_RULES |
| 💾 **`docs/3_DATABASE/`** | Database Schema Overview (`DBFinoraV3`), Table Details & Script SQL `Finora.sql` |
| 📦 **`docs/4_MODULES/`** | Tài liệu chi tiết 13 module chức năng hệ thống |
| 📈 **`docs/5_IMPLEMENTATION/`** | Trạng thái triển khai thực tế & Implemented Features |
| ⚙️ **`docs/6_DEVELOPMENT/`** | Tiêu chuẩn viết code & AI Agent Workflow rules |

---

<div align="center">

**FinoraRetail — SWP391 Project**  
*Phát triển bởi Đội ngũ Finora Software Group*

</div>

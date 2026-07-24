# Trạng thái triển khai hiện tại - FinoraRetail

## Tổng quan

Dự án **SWP391_Finora** (FinoraRetail) là một ứng dụng web Java dạng WAR được xây dựng trên nền tảng Maven, phục vụ cho hệ thống quản lý bán lẻ đa cửa hàng và kho bãi. Ứng dụng được thiết kế theo kiến trúc phân lớp MVC + DAO + Service với Jakarta Servlet/JSP APIs, kết nối cơ sở dữ liệu SQL Server (`DBFinoraV2`), và sử dụng Bootstrap 5 cùng bộ bộ lọc bảo mật tập trung `SecurityFilter`.

---

## Thông tin hệ thống

| Thuộc tính | Chi tiết |
|---|---|
| **Loại ứng dụng** | Maven Java WAR web application |
| **Môi trường Runtime** | Apache Tomcat 10.1+ |
| **Java version** | JDK 17 |
| **API** | Jakarta Servlet 6.0 / JSP 3.0 (Jakarta EE 10) |
| **Cơ sở dữ liệu** | SQL Server — `DBFinoraV2` |
| **Build System** | Maven (`pom.xml`) |
| **Output** | `target/StoreManagementNetBeans.war` |
| **Context Path** | `/FinoraRetail` |
| **Package gốc** | Direct root packages (`controller`, `dao`, `model`, `service`, `filter`, `util`, `dto`, `constant`) |
| **Bảo mật & Phân quyền** | `filter.SecurityFilter` (RBAC ROLE_MAP, CSRF Tokens, Security Headers, Audit Logging) |

---

## Cấu trúc Package Mã Nguồn

```
src/main/java/
├── constant/        — Application Constants (AppConstants)
├── controller/      — HTTP Servlet Handlers (30+ servlets chia theo domain)
├── dao/             — Data Access Objects (kế thừa DBContext)
├── dto/             — Data Transfer Objects
├── filter/          — SecurityFilter.java (Bảo mật tập trung)
├── model/           — 51 Domain POJO Entity Classes
├── service/         — Tầng xử lý nghiệp vụ (8 domain subpackages)
└── util/            — Tiện ích hệ thống (DBContext, PasswordUtil, POI Excel, OpenPDF, VNPay, Mail)
```

---

## Ma trận trạng thái triển khai theo Module

### 1. Bảo mật & Xác thực (`filter/`, `controller/auth/`, `util/security/`)
- **SecurityFilter**: Central Filter cho toàn bộ route `/*`. Quản lý `ROLE_MAP` (Owner, Admin, StoreManager, WarehouseStaff, SalesStaff), kiểm tra CSRF token cho POST requests, thiết lập Security Headers (`Cache-Control`, `X-Frame-Options`, `X-Content-Type-Options`).
- **Activity Log & Audit**: Tự động ghi nhận log hành vi và truy cập vi phạm qua `ActivityLogDAO`.
- **Mật khẩu**: Mã hóa BCrypt với `PasswordUtil.java`.

### 2. Danh mục & Sản phẩm (`controller/product/`, `dao/product/`, `model/Category.java`, `model/Product.java`)
- **Quản lý Category**: Phân cấp cây danh mục (parent/child), CRUD, tìm kiếm, phân trang.
- **Quản lý Sản phẩm**: Quản lý thông tin sản phẩm, đơn vị tính (`Unit`), hình ảnh, tồn kho ban đầu.

### 3. Quản lý Tồn kho & Kho bãi (`controller/inventory/`, `controller/warehouse/`, `dao/inventory/`)
- **Điều chuyển Kho (Stock Transfer)**: Luồng điều chuyển hàng hóa giữa các kho, phê duyệt phiếu chuyển.
- **Kiểm kê Kho (Inventory Check)**: Tạo phiếu kiểm kê, đối soát chênh lệch số lượng thực tế và trên sổ sách.
- **Cách ly Kho (Warehouse Receipt Isolation)**: Đảm bảo nhân viên kho chỉ truy cập dữ liệu phiếu nhập/xuất thuộc kho được phân công.
- **Import/Export Excel**: Sử dụng Apache POI 5.2.5 để import/export danh sách kho và tồn kho.

### 4. Bán hàng & Thanh toán (`controller/pos/`, `controller/sales/`, `controller/vnpay/`)
- **Màn hình POS**: Màn hình bán hàng POS, giỏ hàng (`CartItem`), tạo đơn hàng (`Order`, `OrderDetail`).
- **Tích hợp VNPay**: Cổng thanh toán VNPay (IPN callback, Return URL, checksum hashing).
- **Báo cáo Giao dịch Bán hàng (Sales Transaction Report)**: Thống kê doanh thu, KPI, bộ lọc thời gian và xuất báo cáo PDF/Excel.

### 5. Quản lý Khách hàng, Nhà cung cấp & Nhân sự (`controller/customer/`, `supplier/`, `user/`)
- **Khách hàng & Loyal Points**: Quản lý điểm thưởng (`LoyaltyPointSetting`), thông tin khách hàng.
- **Nhân viên & Phân quyền**: Quản lý tài khoản nhân viên (`AdminUserServlet`, `ManagerEmployeeServlet`, `OwnerUserServlet`), phân bổ chi nhánh.

---

## Tổng kết Kiến Trúc
1. **51 Domain Models** đại diện đầy đủ cho DB schema `DBFinoraV2`.
2. **SecurityFilter** bảo mật nhiều lớp (RBAC, CSRF, Audit log, Security headers).
3. **Thư viện tích hợp đầy đủ**: Apache POI 5.2.5, OpenPDF 1.3.39, jBCrypt 0.4, mssql-jdbc 12.6.1, Jakarta Mail 2.0.1.
4. **Hợp đồng AI Agent**: Đã thiết lập master [AGENTS.md](file:///d:/Thangdev/SWP/SWP391_Finora-thang/AGENTS.md) ở gốc repository.

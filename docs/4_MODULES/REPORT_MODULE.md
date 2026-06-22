# Report Module - FinoraRetail

## Tổng quan Module

Module Report (Báo cáo) cung cấp các báo cáo phân tích và tổng hợp dữ liệu từ nhiều nguồn trong hệ thống FinoraRetail. Module này phục vụ cho việc ra quyết định quản lý thông qua các báo cáo về doanh số, tồn kho, khách hàng thân thiết, và hiệu suất nhân viên. Ngoài ra, module cung cấp chức năng xuất dữ liệu ra các định dạng phổ biến phục vụ nhu cầu sử dụng đa nền tảng.

---

## Thông tin cơ bản

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Report (Báo cáo) |
| **Trạng thái** | `Skeleton` |
| **Package** | `controller.report`, `dao.report`, `model.report` |
| **Route chính** | `/reports/*` |
| **Bảng DB** | Nhiều bảng (orders, inventory, customers, employees, income, expenses) |
| **Module cha** | Report & Analytics |

---

## Routes và Endpoints

### Report Routes (`/reports/*`)

| Route | Method | Mô tả | Trạng thái |
|---|---|---|---|
| `/reports` | GET | Trang chính danh sách báo cáo | Skeleton |
| `/reports/sales-by-store` | GET | Báo cáo doanh số theo cửa hàng | Skeleton |
| `/reports/inventory` | GET | Báo cáo mức tồn kho | Skeleton |
| `/reports/customer-loyal` | GET | Báo cáo khách hàng thân thiết | Skeleton |
| `/reports/employee-sales` | GET | Báo cáo hiệu suất nhân viên | Skeleton |
| `/reports/financial` | GET | Báo cáo tài chính tổng hợp | Skeleton |
| `/reports/export` | GET | Trang xuất dữ liệu | Skeleton |
| `/reports/export/download` | GET/POST | Tải xuống file xuất (PDF/Excel) | Skeleton |
| `/reports/sales-by-store/export` | GET | Xuất báo cáo doanh số | Skeleton |
| `/reports/inventory/export` | GET | Xuất báo cáo tồn kho | Skeleton |

---

## Controller

### ReportController

**Package:** `controller.report`

**Tệp:** `ReportController.java`

**Mô tả:** Controller trung tâm xử lý tất cả các yêu cầu liên quan đến báo cáo. Dựa trên action parameter và URL path, controller sẽ gọi các DAO tương ứng để tổng hợp dữ liệu và trả về view hoặc file xuất.

**Trách nhiệm:**
- Điều phối request theo loại báo cáo (sales, inventory, customer, employee)
- Phân tích tham số bộ lọc (dateFrom, dateTo, storeId, categoryId)
- Gọi nhiều DAO để tổng hợp dữ liệu phức tạp
- Xây dựng DTO chứa dữ liệu báo cáo
- Gọi ExportUtil để xuất file (Excel, PDF)
- Đặt attributes cho JSP hoặc trả về binary file

**Trạng thái hiện tại:** Skeleton — routing và forward tồn tại, logic tổng hợp và xuất chưa triển khai.

```java
@WebServlet(name = "ReportController", urlPatterns = {
    "/reports/*"
})
public class ReportController extends HttpServlet {
    // Skeleton implementation
}
```

---

## Các loại báo cáo

### 1. Báo cáo doanh số theo cửa hàng (Sales by Store)

**Tệp View:** `views/reports/sales-by-store.jsp`

**Nguồn dữ liệu:** Bảng `orders`, `order_details`, `stores`

**Bộ lọc:**
- Khoảng ngày (mặc định: tháng hiện tại)
- Cửa hàng (mặc định: tất cả)
- Trạng thái đơn hàng

**Chỉ số chính:**
- Tổng doanh số theo cửa hàng
- Số lượng đơn hàng
- Giá trị trung bình mỗi đơn
- So sánh với kỳ trước
- Top sản phẩm bán chạy theo cửa hàng

**Trạng thái:** Skeleton.

### 2. Báo cáo mức tồn kho (Inventory Report)

**Tệp View:** `views/reports/inventory.jsp`

**Nguồn dữ liệu:** Bảng `inventory`, `products`, `stores`, `warehouses`

**Bộ lọc:**
- Cửa hàng / Kho
- Danh mục sản phẩm
- Trạng thái tồn kho (bình thường, thấp, hết hàng)
- Khoảng giá trị

**Chỉ số chính:**
- Tổng số sản phẩm trong kho
- Sản phẩm sắp hết (dưới ngưỡng tối thiểu)
- Sản phẩm không có trong kho
- Giá trị tồn kho tổng cộng
- Sản phẩm không di chuyển trong X ngày

**Trạng thái:** Skeleton.

### 3. Báo cáo khách hàng thân thiết (Customer Loyalty)

**Tệp View:** `views/reports/customer-loyal.jsp`

**Nguồn dữ liệu:** Bảng `customers`, `orders`, `order_details`

**Bộ lọc:**
- Khoảng ngày
- Cấp độ khách hàng (Bronze, Silver, Gold, Platinum)
- Cửa hàng

**Chỉ số chính:**
- Số lượng khách hàng theo cấp độ
- Tổng chi tiêu theo cấp độ
- Tần suất mua hàng trung bình
- Top khách hàng VIP (theo doanh số)
- Khách hàng có nguy cơ rời bỏ (lâu không mua)
- Tỷ lệ giữ chân khách hàng

**Trạng thái:** Skeleton.

### 4. Báo cáo hiệu suất nhân viên (Employee Sales Performance)

**Tệp View:** `views/reports/employee-sales.jsp`

**Nguồn dữ liệu:** Bảng `employees`, `orders`, `order_details`

**Bộ lọc:**
- Khoảng ngày
- Cửa hàng
- Vai trò nhân viên

**Chỉ số chính:**
- Tổng doanh số theo nhân viên
- Số lượng đơn hàng xử lý
- Giá trị trung bình mỗi đơn
- Xếp hạng nhân viên theo doanh số
- So sánh với target đặt ra

**Trạng thái:** Skeleton.

### 5. Báo cáo tài chính (Financial Report)

**Tệp View:** `views/reports/financial.jsp`

**Nguồn dữ liệu:** Bảng `orders`, `payments`, `expenses` (khi có)

**Bộ lọc:**
- Khoảng ngày
- Cửa hàng

**Chỉ số chính:**
- Tổng thu (từ đơn hàng hoàn tất)
- Tổng chi (từ chi phí)
- Lợi nhuận gộp = Tổng thu - Tổng chi
- Tỷ lệ lợi nhuận = Lợi nhuận gộp / Tổng thu
- Biểu đồ thu chi theo thời gian

**Trạng thái:** Skeleton.

---

## Export System

### views/reports/export.jsp

**Vị trí:** `web/WEB-INF/views/reports/export.jsp`

**Mô tả:** Trang tổng hợp cho phép người dùng chọn loại báo cáo, bộ lọc, định dạng xuất (Excel, PDF), và tải xuống.

**Các thành phần UI dự kiến:**
- Dropdown chọn loại báo cáo
- Bộ lọc theo ngày, cửa hàng, danh mục
- Dropdown chọn định dạng (Excel .xlsx, PDF)
- Dropdown chọn cột muốn xuất
- Nút Preview (xem trước)
- Nút Export (tải xuống)

**Trạng thái:** Skeleton.

### ExportUtil.java

**Vị trí:** `util/ExportUtil.java`

**Trạng thái:** Skeleton — file tồn tại trong `util/` nhưng chưa triển khai.

**Các phương thức dự kiến:**

| Phương thức | Mô tả | Trạng thái |
|---|---|---|
| `exportToExcel(List<?> data, String[] columns, String sheetName)` | Xuất danh sách ra Excel | Not implemented |
| `exportToPDF(JasperReport report, Map<String, Object> params, JRDataSource dataSource)` | Xuất PDF với JasperReports | Not implemented |
| `exportSalesReport(SalesReportDTO data, String format)` | Xuất báo cáo doanh số | Not implemented |
| `exportInventoryReport(InventoryReportDTO data, String format)` | Xuất báo cáo tồn kho | Not implemented |

**Công nghệ dự kiến:**
- **Excel:** Apache POI (`org.apache.poi:xssf-workbook`)
- **PDF:** JasperReports hoặc iText

---

## Phụ thuộc vào Module khác

| Module | Mối quan hệ |
|---|---|
| **Order Module** | Dữ liệu doanh số, chi tiết đơn hàng. Cần OrderDAO, OrderDetailDAO |
| **Inventory Module** | Dữ liệu tồn kho, sản phẩm. Cần InventoryDAO, ProductDAO |
| **Customer Module** | Dữ liệu khách hàng, lịch sử mua hàng. Cần CustomerDAO |
| **Employee Module** | Dữ liệu nhân viên, target. Cần EmployeeDAO |
| **Store Module** | Dữ liệu cửa hàng. Cần StoreDAO |
| **Finance Module** | Dữ liệu thu chi. Cần IncomeDAO, ExpenseDAO |
| **Category Module** | Phân loại sản phẩm. Cần CategoryDAO |
| **ExportUtil** | Công cụ xuất file. Cần triển khai trong util/ |

---

## Report DTOs

Module báo cáo sử dụng các DTO chuyên biệt để đóng gói dữ liệu báo cáo:

```
dto/report/
├── SalesReportDTO.java         — Dữ liệu báo cáo doanh số
├── InventoryReportDTO.java     — Dữ liệu báo cáo tồn kho
├── CustomerLoyaltyDTO.java     — Dữ liệu khách hàng thân thiết
├── EmployeeSalesDTO.java       — Dữ liệu hiệu suất nhân viên
└── FinancialSummaryDTO.java   — Dữ liệu tổng hợp tài chính
```

**Trạng thái:** Các DTO chưa được tạo, cần định nghĩa khi triển khai báo cáo cụ thể.

---

## Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| ReportController | Skeleton | Cần triển khai logic tổng hợp báo cáo |
| SalesReportDTO | Not created | Cần định nghĩa |
| InventoryReportDTO | Not created | Cần định nghĩa |
| CustomerLoyaltyDTO | Not created | Cần định nghĩa |
| EmployeeSalesDTO | Not created | Cần định nghĩa |
| FinancialSummaryDTO | Not created | Cần định nghĩa |
| views/reports/sales-by-store.jsp | Skeleton | UI template sẵn có |
| views/reports/inventory.jsp | Skeleton | UI template sẵn có |
| views/reports/customer-loyal.jsp | Skeleton | UI template sẵn có |
| views/reports/employee-sales.jsp | Skeleton | UI template sẵn có |
| views/reports/financial.jsp | Skeleton | UI template sẵn có |
| views/reports/export.jsp | Skeleton | UI template sẵn có |
| ExportUtil.java | Not implemented | Cần triển khai với Apache POI / JasperReports |

---

## Open Questions

1. Có cần báo cáo theo lịch trình tự động (email report hàng tuần/tháng) không?
2. Có cần tích hợp với công cụ BI như Power BI, Tableau không?
3. Định dạng PDF có cần template định sẵn (letterhead, logo công ty) không?
4. Có cần bảo vệ file Excel bằng mật khẩu không?
5. ExportUtil nên dùng thư viện nào: Apache POI (Excel), iText (PDF), hay JasperReports?
6. Có cần cache dữ liệu báo cáo (refresh mỗi X phút) cho các báo cáo nặng không?

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

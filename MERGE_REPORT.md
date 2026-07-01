# MERGE REPORT

## Tổng quan

- **Repository**: https://github.com/hoanghachi12082005-ops/SWP391_Finora.git
- **Merge vào**: `main`
- **Thời gian**: 2026-07-01
- **Người thực hiện**: Senior Software Engineer (AI-assisted)

---

## 1. Branch đã merge

| # | Branch | Ahead | Behind | Focus |
|---|--------|-------|--------|-------|
| 1 | `Hoàng` | 10 | 6 | VNPAY config, Activity Log improvements |
| 2 | `Chi` | 5 | 18 | Sales module, Branch management, Sidebar |
| 3 | `Phúc` | 6 | 23 | POS, Branch UI, Preview images (no new changes) |
| 4 | `Thắng` | 10 | 18 | Inventory/Warehouse, Supplier products, Auth improvements |
| 5 | `Dũng` | 81 | 8 | V3 database refactor, User management, POS, Warehouse |
| - | `Hoang` | - | - | Integration branch (skipped - already merged others) |

### Thứ tự merge đề xuất

1. **Hoàng** → nhỏ nhất, ít conflict
2. **Chi** → sales + branch (cần thiết cho Phúc)
3. **Phúc** → POS (phụ thuộc sales)
4. **Thắng** → inventory/warehouse (độc lập)
5. **Dũng** → lớn nhất, refactor V3 (merge cuối với đầy đủ context)

---

## 2. Commit được merge

```
8aeb28c Merge branch 'Hoàng' into main       (2026-07-01)
b8a190a Merge branch 'Chi' into main         (2026-07-01)
14a58d6 Merge branch 'Thắng' into main       (2026-07-01)
7635596 Merge branch 'Dũng' into main        (2026-07-01)
```

- `Phúc` đã up-to-date với main, không cần merge commit riêng.

## 3. Conflict gặp phải

### Merge `Thắng` (5 conflicts)

| File | Loại | Cách giải quyết |
|------|------|-----------------|
| `dao/employee/EmployeeDAO.java` | modify/modify | Giữ HEAD (V3 schema) |
| `dao/product/ProductDAO.java` | modify/modify | Giữ HEAD + thêm `supplierID` từ Thắng |
| `dao/system/ActivityLogDAO.java` | modify/modify | Giữ HEAD (V3 schema) |
| `util/Config.java` | modify/delete | Thắng sửa file cũ; HEAD đã move sang `util/vnpay/Config.java` → xóa file cũ |
| `views/orders/create.jsp` | modify/delete | HEAD đã xóa (chuyển sang sales views) → giữ xóa |

### Merge `Dũng` (80+ conflicts)

| Nhóm | Số lượng | Cách giải quyết |
|------|----------|-----------------|
| modify/modify (Java) | ~30 file | Giữ HEAD (phiên bản mới hơn sau merge các branch khác) |
| add/add | ~20 file | Giữ HEAD (phiên bản từ merged branches) |
| modify/delete (HEAD deleted) | ~25 file | Giữ HEAD (cấu trúc mới) |
| modify/delete (Dũng deleted) | ~7 file | Giữ HEAD (cần cho business logic) |

## 4. File thay đổi

### File mới (tiêu biểu)
- `database/DBFinoraV3.sql`, `database/seed_data.sql`
- `controller/pos/PosController.java`, `controller/warehouse/WarehouseController.java`
- `controller/user/AdminUserServlet.java`, `ManagerEmployeeServlet.java`, `OwnerUserServlet.java`, `ProfileServlet.java`
- `dao/inventory/InventoryDAO.java`, `InventoryTicketDAO.java`, `WarehouseDAO.java`
- `dao/customer/LoyaltyPointSettingDAO.java`
- `dto/inventory/ExchangeProductDTO.java`, `ImportProductDTO.java`
- `model/Inventory.java`, `InventoryTicket.java`, `InventoryTicketDetail.java`, `Warehouse.java`
- `model/CustomerOverview.java`, `EmployeeOverview.java`, `EmployeeSalesSummary.java`, `LoyaltyPointSetting.java`
- `util/report/PdfReportUtil.java`, `util/vnpay/Config.java`
- Views: branch, customers, inventory (7 tabs), pos, sales, warehouse, profile
- CSS: base, components, customer-management, employee-sales-report, form-modal, inventory, layout, profile, theme, user-management
- Font Awesome assets

### File bị xóa
- `BaseModel.java`, `BaseDAO.java`
- `Store.java`, `StoreDAO.java`, `StoreService.java`, `StoreController.java`
- `Expense.java`, `Income.java`, `Notification.java`, `BusinessConfiguration.java`
- `ExpenseDAO.java`, `IncomeDAO.java`, `NotificationDAO.java`, `BusinessConfigurationDAO.java`
- `CustomerService.java`, `EmployeeService.java`, `InvoiceService.java`, `PaymentService.java`
- `OrderService.java`, `OrderDetailService.java`
- `AuthUtil.java`, `ProductCodeUtil.java`, `BCryptTest.java`, `TestHash.java`, `LogUtil.java`, `SeoUtil.java`
- Views: stores (add/detail/edit/list), orders (create/detail/update/cancel), roles/list, profile (old)
- Old JSPs: customers (add/detail/edit/list/ranking), suppliers (create/edit/list - old), products (add/detail/edit/list/showcase/stock-adjustment/import-receipt)
- `web/WEB-INF/web.xml` (chuyển cấu hình sang Jakarta)
- Temp files: `_d_*`, `create_tests.py`, `implementation_plan.md`, `opencode.json`, `test.jsp`

### File sửa đổi
- `pom.xml`, `index.jsp`
- Controllers: `AuthServlet`, `BaseController`, `CategoryController`, `CustomerController`, `ProductController`, `ReportController`, `SystemController`, `InventoryController`
- DAOs: `ICrudDAO`, `CustomerDAO`, `EmployeeDAO`, `PaymentDAO`, `InvoiceDAO`, `InventoryItemDAO`, `StockTransactionDAO`, `CategoryDAO`, `ProductDAO`, `OrderDAO`, `OrderDetailDAO`, `SupplierDAO`, `ActivityLogDAO`
- Models: `Branch`, `Category`, `Customer`, `Employee`, `Order`, `OrderDetail`, `Payment`, `Product`, `StockTransaction`, `Supplier`, `InventoryItem`, `Invoice`, `Role`, `PurchaseDetail`, `PurchaseOrder`
- Services: `GenericService`, `AuthService`, `InventoryItemService`, `StockTransactionService`, `CategoryService`, `ProductService`, `SupplierService`, `ActivityLogService`
- Filters: `AuthFilter`
- `DBContext.java`, `PasswordUtil.java`
- Views: sidebar, login, forgot-password, categories/list, dashboard/*, inventory/*, products/index, invoices/list, notifications/list, purchase-orders/*, reports/*, profile, users/user-list

## 5. Các lỗi đã sửa

### Lỗi biên dịch (71 errors → 0)

| Mô tả | File ảnh hưởng |
|-------|----------------|
| Duplicate field `quantity` trong `Product.java` | `model/Product.java` |
| `BaseModel` bị xóa → `Category`, `Payment` không extends | `model/Category.java`, `model/Payment.java` |
| Thiếu field `id`, `name`, `status` trong Category/Payment | `model/Category.java`, `model/Payment.java` |
| `ActivityLogService.log()` → `insertLog()` | `service/system/ActivityLogService.java` |
| `StockTransactionService` sai method signature | `service/inventory/StockTransactionService.java` |
| `CategoryController` gọi sai method | `controller/product/CategoryController.java` |
| `CategoryDAO` gọi sai getter/setter | `dao/product/CategoryDAO.java` |
| `ProductService`/`CategoryService` incompatible với ICrudDAO | `service/common/GenericService.java` |
| `PaymentDAO` gọi sai method | `dao/finance/PaymentDAO.java` |
| `Customer` model field mismatch | `model/Customer.java`, `dao/customer/CustomerDAO.java`, `controller/customer/CustomerController.java` |
| `EmployeeSalesReportDAO` gọi sai method | `model/EmployeeSalesSummary.java`, `model/EmployeeOverview.java`, `dao/report/EmployeeSalesReportDAO.java` |
| `PdfReportUtil` gọi sai method | `util/report/PdfReportUtil.java` |
| Enum CustomerType bị thay đổi | `controller/sales/SalesServlet.java`, `dao/sales/CustomerDAO.java` |

## 6. Những phần cần kiểm thử thủ công

- **Authentication**: Đăng nhập, đăng xuất, forgot password, failed login count
- **Authorization**: Role-based access (Admin/Owner/Manager/Employee)
- **User Management**: CRUD user, phân quyền
- **Employee Management**: CRUD employee, gán branch
- **Customer Management**: CRUD customer, loyalty points
- **Product Management**: CRUD product, category, unit, supplier product
- **Inventory**: Nhập/xuất/kiểm kê/chuyển kho
- **Supplier**: CRUD supplier, quản lý sản phẩm nhà cung cấp
- **Purchase Order**: Tạo đơn mua hàng
- **POS**: Bán hàng tại quầy
- **Sales**: Order history, revenue, shift management
- **Dashboard**: Owner dashboard, inventory dashboard, financial dashboard
- **Report**: Employee sales report, inventory report, customer loyalty
- **Branch Management**: CRUD branch
- **VNPAY**: Cấu hình VNPAY (cần deploy URL thật)
- **Activity Log**: Audit trail
- **Upload**: Avatar upload cho employee
- **Pagination**: Tất cả các module có phân trang
- **Soft Delete**: Product, Supplier, Category
- **Responsive UI**: Tất cả JSP views với Bootstrap 5

## 7. Backup

Branch backup `backup-before-merge` đã được tạo trước khi merge.

## 8. Ghi chú

- Maven build thành công: `mvn clean compile -DskipTests` (134 Java files)
- Jakarta Servlet API đã được chuyển đổi (Tomcat 10+)
- Font Awesome SVGs đã được thêm vào assets
- Cơ sở dữ liệu đã chuyển sang V3 (`database/DBFinoraV3.sql`)
- File tạm thời của nhà phát triển (`_d_*`, `test.jsp`, `create_tests.py`, `implementation_plan.md`) đã được dọn dẹp
- Cần cập nhật `vnp_Returnurl` trong `util/vnpay/Config.java` trước khi deploy

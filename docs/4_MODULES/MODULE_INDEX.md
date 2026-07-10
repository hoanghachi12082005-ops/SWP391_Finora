# Module Index - FinoraRetail

## Tổng quan

Tài liệu này liệt kê toàn bộ các module trong hệ thống FinoraRetail (SWP391_Finora), bao gồm trạng thái triển khai, các tệp chính, và các bảng cơ sở dữ liệu tương ứng. Mỗi module sở hữu các subpackage riêng theo quy tắc đóng gói (`ownership rule`).

---

## 1. Authentication Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Authentication |
| **Trạng thái** | `Implemented` (Demo Mode) |
| **Package** | `controller.auth` |
| **Tệp chính** | `AuthController.java` |
| **Tệp filter** | `AuthFilter.java` |
| **Views** | `login.jsp`, `register.jsp`, `forgot-password.jsp` |
| **Route chính** | `/login`, `/logout`, `/register`, `/forgot-password` |
| **Bảng DB** | `users` |

**Mô tả:** Module xác thực quản lý đăng nhập, đăng ký, quên mật khẩu và đăng xuất. Hiện đang chạy ở chế độ demo với dữ liệu hardcoded và không mã hóa mật khẩu. AuthFilter bảo vệ 21 route patterns bao gồm `/dashboard/*`, `/users/*`, `/categories/*`, `/products/*`, và nhiều nhóm khác.

---

## 2. Dashboard Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Dashboard |
| **Trạng thái** | `Skeleton` |
| **Package** | `controller.dashboard` |
| **Tệp chính** | `DashboardController.java` |
| **Views** | `owner.jsp`, `inventory.jsp`, `financial.jsp` |
| **Route chính** | `/dashboard/owner`, `/dashboard/inventory`, `/dashboard/financial` |
| **Bảng DB** | N/A (chưa kết nối) |

**Mô tả:** Module dashboard cung cấp ba giao diện tổng quan: Owner Dashboard (doanh số, đơn hàng, khách hàng), Inventory Dashboard (mức tồn kho, cảnh báo hết hàng), và Financial Dashboard (thu nhập, chi phí, lợi nhuận). Hiện là template HTML tĩnh chưa có dữ liệu thực.

---

## 3. Category Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Category |
| **Trạng thái** | `Implemented` (Ready for Integration) |
| **Package** | `controller.category`, `dao.category`, `model.category` |
| **Tệp chính** | `CategoryServlet.java`, `CategoryDAO.java`, `Category.java` |
| **Views** | `list.jsp`, `add.jsp`, `edit.jsp` |
| **Route chính** | `/admin/categories` |
| **Bảng DB** | `category` |

**Mô tả:** Module quản lý danh mục sản phẩm với đầy đủ chức năng: cây phân cấp danh mục, tìm kiếm theo từ khóa/tên/mô tả, lọc theo trạng thái và danh mục cha, phân trang, thống kê (tổng danh mục, danh mục gốc, sản phẩm liên kết), quản lý trạng thái active/inactive. Sử dụng CTE để ngăn tham chiếu vòng tròn cha-con.

---

## 4. Product Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Product |
| **Trạng thái** | `Planned` |
| **Package** | `controller.product`, `dao.product`, `model.product` |
| **Tệp chính** | `ProductServlet.java`, `ProductDAO.java`, `Product.java` |
| **Views** | `list.jsp`, `add.jsp`, `edit.jsp`, `detail.jsp` |
| **Route chính** | `/admin/products` |
| **Bảng DB** | `products`, `product_images` |

**Mô tả:** Module quản lý sản phẩm bao gồm CRUD sản phẩm, quản lý hình ảnh, phân loại theo danh mục, thiết lập giá và tồn kho ban đầu.

---

## 5. Customer Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Customer |
| **Trạng thái** | `Planned` |
| **Package** | `controller.customer`, `dao.customer`, `model.customer` |
| **Tệp chính** | `CustomerServlet.java`, `CustomerDAO.java`, `Customer.java` |
| **Views** | `list.jsp`, `add.jsp`, `edit.jsp`, `detail.jsp` |
| **Route chính** | `/admin/customers` |
| **Bảng DB** | `customers` |

**Mô tả:** Module quản lý khách hàng bao gồm hồ sơ khách hàng, lịch sử giao dịch, và phân loại khách hàng.

---

## 6. Supplier Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Supplier |
| **Trạng thái** | `Planned` |
| **Package** | `controller.supplier`, `dao.supplier`, `model.supplier` |
| **Tệp chính** | `SupplierServlet.java`, `SupplierDAO.java`, `Supplier.java` |
| **Views** | `list.jsp`, `add.jsp`, `edit.jsp`, `detail.jsp` |
| **Route chính** | `/admin/suppliers` |
| **Bảng DB** | `suppliers` |

**Mô tả:** Module quản lý nhà cung cấp bao gồm thông tin liên hệ, điều khoản giao hàng, và lịch sử cung ứng.

---

## 7. Store Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Store |
| **Trạng thái** | `Planned` |
| **Package** | `controller.store`, `dao.store`, `model.store` |
| **Tệp chính** | `StoreServlet.java`, `StoreDAO.java`, `Store.java` |
| **Views** | `list.jsp`, `add.jsp`, `edit.jsp`, `detail.jsp` |
| **Route chính** | `/admin/stores` |
| **Bảng DB** | `stores` |

**Mô tả:** Module quản lý cửa hàng bao gồm thông tin chi nhánh, địa chỉ, giờ mở cửa và cấu hình cửa hàng.

---

## 8. Order Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Order |
| **Trạng thái** | `Planned` |
| **Package** | `controller.order`, `dao.order`, `model.order` |
| **Tệp chính** | `OrderServlet.java`, `OrderDAO.java`, `Order.java` |
| **Views** | `list.jsp`, `detail.jsp`, `create.jsp` |
| **Route chính** | `/admin/orders` |
| **Bảng DB** | `orders`, `order_details` |

**Mô tả:** Module quản lý đơn hàng bao gồm tạo đơn, cập nhật trạng thái, chi tiết sản phẩm trong đơn, và tính tổng tiền.

---

## 9. Inventory Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Inventory |
| **Trạng thái** | `Planned` |
| **Package** | `controller.inventory`, `dao.inventory`, `model.inventory` |
| **Tệp chính** | `InventoryServlet.java`, `InventoryDAO.java`, `Inventory.java` |
| **Views** | `list.jsp`, `adjustment.jsp`, `transfer.jsp` |
| **Route chính** | `/admin/inventory` |
| **Bảng DB** | `inventory`, `inventory_logs` |

**Mô tả:** Module quản lý tồn kho bao gồm theo dõi số lượng, điều chỉnh tồn kho, chuyển kho giữa cửa hàng, và cảnh báo hết hàng.

---

## 10. Payment Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Payment |
| **Trạng thái** | `Planned` |
| **Package** | `controller.payment`, `dao.payment`, `model.payment` |
| **Tệp chính** | `PaymentServlet.java`, `PaymentDAO.java`, `Payment.java` |
| **Views** | `list.jsp`, `detail.jsp`, `process.jsp` |
| **Route chính** | `/admin/payments` |
| **Bảng DB** | `payments`, `payment_methods` |

**Mô tả:** Module xử lý thanh toán (Protected Area). Quản lý các phương thức thanh toán, giao dịch, và hóa đơn thanh toán. **Không chỉnh sửa trừ khi có yêu cầu rõ ràng từ người dùng.**

---

## 11. Invoice Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Invoice |
| **Trạng thái** | `Planned` |
| **Package** | `controller.invoice`, `dao.invoice`, `model.invoice` |
| **Tệp chính** | `InvoiceServlet.java`, `InvoiceDAO.java`, `Invoice.java` |
| **Views** | `list.jsp`, `detail.jsp`, `print.jsp` |
| **Route chính** | `/admin/invoices` |
| **Bảng DB** | `invoices`, `invoice_items` |

**Mô tả:** Module quản lý hóa đơn bao gồm tạo hóa đơn, xem chi tiết, và in hóa đơn.

---

## 12. Expense Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Expense |
| **Trạng thái** | `Planned` |
| **Package** | `controller.expense`, `dao.expense`, `model.expense` |
| **Tệp chính** | `ExpenseServlet.java`, `ExpenseDAO.java`, `Expense.java` |
| **Views** | `list.jsp`, `add.jsp`, `edit.jsp` |
| **Route chính** | `/admin/expenses` |
| **Bảng DB** | `expenses`, `expense_categories` |

**Mô tả:** Module quản lý chi phí bao gồm ghi nhận chi phí hoạt động, phân loại chi phí, và báo cáo chi phí theo kỳ.

---

## 13. Income Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Income |
| **Trạng thái** | `Planned` |
| **Package** | `controller.income`, `dao.income`, `model.income` |
| **Tệp chính** | `IncomeServlet.java`, `IncomeDAO.java`, `Income.java` |
| **Views** | `list.jsp`, `detail.jsp`, `report.jsp` |
| **Route chính** | `/admin/income` |
| **Bảng DB** | `income`, `income_categories` |

**Mô tả:** Module quản lý thu nhập bao gồm ghi nhận thu nhập từ bán hàng, dịch vụ, và các nguồn khác.

---

## 14. Purchase Order Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Purchase Order |
| **Trạng thái** | `Planned` |
| **Package** | `controller.purchase`, `dao.purchase`, `model.purchase` |
| **Tệp chính** | `PurchaseOrderServlet.java`, `PurchaseOrderDAO.java`, `PurchaseOrder.java` |
| **Views** | `list.jsp`, `create.jsp`, `detail.jsp` |
| **Route chính** | `/admin/purchase-orders` |
| **Bảng DB** | `purchase_orders`, `purchase_order_items` |

**Mô tả:** Module quản lý đơn đặt hàng với nhà cung cấp bao gồm tạo đơn, theo dõi trạng thái, và nhận hàng.

---

## 15. Report Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Report |
| **Trạng thái** | `Planned` |
| **Package** | `controller.report`, `dao.report`, `model.report` |
| **Tệp chính** | `ReportServlet.java`, `ReportDAO.java`, `Report.java` |
| **Views** | `sales.jsp`, `inventory.jsp`, `financial.jsp`, `export.jsp` |
| **Route chính** | `/admin/reports` |
| **Bảng DB** | Nhiều bảng (orders, inventory, income, expenses) |

**Mô tả:** Module báo cáo bao gồm báo cáo doanh số, báo cáo tồn kho, báo cáo tài chính, và xuất dữ liệu ra Excel/PDF.

---

## 16. User Management Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | User Management |
| **Trạng thái** | `Planned` |
| **Package** | `controller.user`, `dao.user`, `model.user` |
| **Tệp chính** | `UserServlet.java`, `UserDAO.java`, `User.java` |
| **Views** | `list.jsp`, `add.jsp`, `edit.jsp`, `profile.jsp` |
| **Route chính** | `/admin/users` |
| **Bảng DB** | `users` |

**Mô tả:** Module quản lý người dùng hệ thống bao gồm CRUD tài khoản, quản lý trạng thái, và thiết lập mật khẩu.

---

## 17. Role Management Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Role Management |
| **Trạng thái** | `Planned` |
| **Package** | `controller.role`, `dao.role`, `model.role` |
| **Tệp chính** | `RoleServlet.java`, `RoleDAO.java`, `Role.java` |
| **Views** | `list.jsp`, `add.jsp`, `edit.jsp`, `permissions.jsp` |
| **Route chính** | `/admin/roles` |
| **Bảng DB** | `roles`, `role_permissions` |

**Mô tả:** Module quản lý vai trò và phân quyền (Protected Area). Quản lý vai trò người dùng, quyền hạn theo vai trò, và kiểm soát truy cập. **Không chỉnh sửa trừ khi có yêu cầu rõ ràng từ người dùng.**

---

## 18. Activity Log Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Activity Log |
| **Trạng thái** | `Planned` |
| **Package** | `controller.log`, `dao.log`, `model.log` |
| **Tệp chính** | `ActivityLogServlet.java`, `ActivityLogDAO.java`, `ActivityLog.java` |
| **Views** | `list.jsp`, `detail.jsp` |
| **Route chính** | `/admin/activity-logs` |
| **Bảng DB** | `activity_logs` |

**Mô tả:** Module ghi nhận và theo dõi hoạt động của người dùng trong hệ thống, bao gồm đăng nhập, thao tác CRUD, và các sự kiện quan trọng.

---

## 19. Notification Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Notification |
| **Trạng thái** | `Planned` |
| **Package** | `controller.notification`, `dao.notification`, `model.notification` |
| **Tệp chính** | `NotificationServlet.java`, `NotificationDAO.java`, `Notification.java` |
| **Views** | `list.jsp`, `settings.jsp` |
| **Route chính** | `/admin/notifications` |
| **Bảng DB** | `notifications` |

**Mô tả:** Module quản lý thông báo bao gồm thông báo hệ thống, thông báo đơn hàng, cảnh báo tồn kho, và cài đặt kênh thông báo.

---

## 20. Configuration Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Configuration |
| **Trạng thái** | `Planned` |
| **Package** | `controller.config`, `dao.config`, `model.config` |
| **Tệp chính** | `ConfigServlet.java`, `ConfigDAO.java`, `Config.java` |
| **Views** | `general.jsp`, `payment.jsp`, `notification.jsp` |
| **Route chính** | `/admin/config` |
| **Bảng DB** | `configurations` |

**Mô tả:** Module cấu hình hệ thống bao gồm cài đặt chung, cấu hình thanh toán, và cấu hình thông báo.

---

## 21. SEO Module

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | SEO |
| **Trạng thái** | `Planned` |
| **Package** | `controller.seo`, `dao.seo`, `model.seo` |
| **Tệp chính** | `SeoServlet.java`, `SeoDAO.java`, `Seo.java` |
| **Views** | `meta.jsp`, `sitemap.jsp`, `redirects.jsp` |
| **Route chính** | `/admin/seo` |
| **Bảng DB** | `seo_metadata` |

**Mô tả:** Module quản lý SEO bao gồm metadata trang, sitemap XML, và quản lý redirect URL.

---

## Tổng kết theo trạng thái

| Trạng thái | Số lượng | Modules |
|---|---|---|
| `Implemented` | 1 | Authentication |
| `Implemented` (sẵn sàng tích hợp) | 1 | Category |
| `Skeleton` | 1 | Dashboard |
| `Planned` | 18 | Product, Customer, Supplier, Store, Order, Inventory, Payment, Invoice, Expense, Income, Purchase Order, Report, User Management, Role Management, Activity Log, Notification, Configuration, SEO |

**Tổng: 21 modules**

---

## Ownership Rule

Mỗi module sở hữu các subpackage riêng theo cấu trúc:

```
controller/<module>/
dao/<module>/
model/<module>/
dto/<module>/
service/<module>/
```

 Ví dụ: Module Category sở hữu `controller.category`, `dao.category`, `model.category`. Không module nào được phép truy cập trực tiếp vào package nội bộ của module khác mà không thông qua giao diện công khai (public interface/service).

---

## Protected Areas

Các module sau được xếp vào **Protected Areas** — không chỉnh sửa trừ khi người dùng yêu cầu rõ ràng:

- **Payment Module** — schema thanh toán và tài chính
- **Role Management Module** — phân quyền và kiểm soát truy cập
- **Authentication flow** — AuthFilter, session handling
- **Database infrastructure** — DatabaseUtil, schema SQL

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

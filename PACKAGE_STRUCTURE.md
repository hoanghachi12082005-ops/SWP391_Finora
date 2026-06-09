# Package Structure theo nhóm chức năng

Project đã được tách package theo module để dễ mở rộng trong NetBeans.

```text
src/main/java/com/storemanagement/
├── controller/
│   ├── common/       BaseController
│   ├── auth/         Login, Register, Forgot Password, Logout, Profile, Change Password
│   ├── dashboard/    Owner, Inventory, Financial Dashboard
│   ├── user/         User Manager, Lock/Unlock, Role Management
│   ├── supplier/     Supplier Manager
│   ├── customer/     Customer Manager, Detail, Ranking
│   ├── product/      Product, Category, Showcase
│   ├── inventory/    Import, Export, Transfer, Adjustment, Report
│   ├── store/        Store Manager, Store Detail
│   ├── sales/        Create/Update/Cancel Order, Order Detail
│   ├── purchase/     Purchase Order, Purchase Detail
│   ├── finance/      Income, Expense, Payment, Invoice
│   ├── report/       Employee Sales, Customer Loyal, Sales by Store, Export
│   ├── website/      About, Contact, SEO Website
│   └── system/       Activity Log, Notification, Business Configuration
│
├── dao/
│   ├── common/       BaseDAO, ICrudDAO
│   ├── user/         UserDAO, RoleDAO
│   ├── supplier/     SupplierDAO
│   ├── customer/     CustomerDAO
│   ├── product/      ProductDAO, CategoryDAO
│   ├── inventory/    InventoryItemDAO, StockTransactionDAO
│   ├── store/        StoreDAO
│   ├── sales/        OrderDAO, OrderDetailDAO
│   ├── purchase/     PurchaseOrderDAO, PurchaseDetailDAO
│   ├── finance/      IncomeDAO, ExpenseDAO, PaymentDAO, InvoiceDAO
│   └── system/       ActivityLogDAO, NotificationDAO, BusinessConfigurationDAO
│
├── service/
│   ├── common/       GenericService
│   └── ...           Chia giống DAO theo từng module
│
├── util/
│   ├── database/     DBContext
│   ├── security/     PasswordUtil
│   ├── validation/   ValidationUtil
│   ├── auth/         AuthUtil
│   ├── user/         UserStatusUtil
│   ├── product/      ProductCodeUtil
│   ├── inventory/    InventoryUtil
│   ├── finance/      MoneyUtil
│   ├── report/       ExportUtil
│   ├── website/      SeoUtil
│   └── system/       LogUtil
│
├── model/            Entity / JavaBean
└── filter/           AuthFilter
```

## Quy tắc đặt package

- Controller của chức năng nào nằm trong `controller.<module>`.
- DAO của bảng nào nằm trong `dao.<module>`.
- Service của nghiệp vụ nào nằm trong `service.<module>`.
- Các class dùng chung đặt trong `common`, `database`, `security`, `validation`.
- Các util riêng cho module đặt trong `util.<module>`.

Ví dụ khi làm Product:

```java
controller.product.ProductController
service.product.ProductService
dao.product.ProductDAO
model.Product
util.product.ProductCodeUtil
```

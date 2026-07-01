# Feature Mapping

## Auth
- Login: `/login`
- Register Account: `/register`
- Forgot Password: `/forgot-password`
- Logout: `/logout`

## Supplier
- Supplier Manager: `/suppliers`
- Create Supplier: `/suppliers/create`
- Edit Supplier: `/suppliers/edit`
- Search Supplier: `/suppliers/search`
- Filter Supplier: `/suppliers/filter`

## User / Role
- User Manager: `/users`
- Create User: `/users/create`
- Edit User: `/users/edit`
- Search/Filter User: `/users/search`, `/users/filter`
- Lock / Unlock User: `/users/lock-unlock`
- Role Management: `/roles`

## Customer
- Customer Manager: `/customers`
- Add Customer: `/customers/add`
- Edit Customer: `/customers/edit`
- Search/Filter Customer: `/customers/search`, `/customers/filter`
- Customer Detail: `/customers/detail`
- Loyal Customer Ranking: `/customers/loyal-ranking`
- Loyal Customer Report: `/reports/customer-loyal`

## Product / Category
- Product Manager: `/products`
- Product Detail: `/products/detail`
- Add Product: `/products/add`
- Edit Product: `/products/edit`
- Search/Filter Product: `/products/search`, `/products/filter`
- Product Showcase: `/products/showcase`
- Category Manager: `/categories`
- Add Category: `/categories/add`
- Edit Category: `/categories/edit`
- Search/Filter Category: `/categories/search`, `/categories/filter`

## Store / Order / Purchase
- Store Manager: `/stores`
- Add Store: `/stores/add`
- Edit Store: `/stores/edit`
- Search/Filter Store: `/stores/search`, `/stores/filter`
- Store Detail: `/stores/detail`
- Create Order: `/orders/create`
- Order Detail: `/orders/detail`
- Update Order: `/orders/update`
- Cancel Order: `/orders/cancel`
- Purchase Order: `/purchase-orders`
- Purchase Detail: `/purchase-orders/detail`

## Inventory / Finance / Report
- Inventory Dashboard: `/inventory/dashboard`
- Inventory Import: `/inventory/import`
- Inventory Export: `/inventory/export`
- Inventory Transfer: `/inventory/transfer`
- Inventory Report: `/inventory/report`
- Import Receipt: `/products/import-receipt`
- Stock Adjustment: `/inventory/adjustment`
- Income Manager: `/income`
- Expense Manager: `/expenses`
- Add Expense: `/expenses/add`
- Payment: `/payments`
- Invoice Management: `/invoices`
- Financial Dashboard: `/dashboard/financial`
- Employee Sales Report: `/reports/employee-sales`
- Sales Report by Store: `/reports/sales-by-store`
- Export Report: `/reports/export`

## System / Website
- Dashboard Owner Overview: `/dashboard/owner`
- Profile: `/profile`
- Change Password: `/profile/change-password`
- About Us: `/about`
- Contact Page: `/contact`
- SEO Website: `/seo`
- Activity Log: `/activity-log`
- Notification Center: `/notifications`
- Business Configuration: `/configuration/business`


# Package Mapping nhanh

| Nhóm chức năng | Controller package | DAO package | Service package | Util package nếu có |
|---|---|---|---|---|
| Auth/Profile | `controller.auth` | `dao.user` | `service.user` | `util.auth`, `util.security` |
| User/Role | `controller.user` | `dao.user` | `service.user` | `util.user` |
| Supplier | `controller.supplier` | `dao.supplier` | `service.supplier` | - |
| Customer | `controller.customer` | `dao.customer` | `service.customer` | - |
| Product/Category | `controller.product` | `dao.product` | `service.product` | `util.product` |
| Inventory | `controller.inventory` | `dao.inventory` | `service.inventory` | `util.inventory` |
| Store | `controller.store` | `dao.store` | `service.store` | - |
| Sales/Order | `controller.sales` | `dao.sales` | `service.sales` | - |
| Purchase | `controller.purchase` | `dao.purchase` | `service.purchase` | - |
| Finance | `controller.finance` | `dao.finance` | `service.finance` | `util.finance` |
| Report | `controller.report` | Có thể dùng DAO từng module | Có thể dùng Service từng module | `util.report` |
| Website/SEO | `controller.website` | - | - | `util.website` |
| System | `controller.system` | `dao.system` | `service.system` | `util.system` |

# Module Index

This directory tracks module ownership and boundaries.

## Current Modules

| Module | Primary Files | Status |
| --- | --- | --- |
| Dashboard/Foundation | `HomeDashboardServlet`, `SkeletonModuleServlet`, `ModuleRegistry`, dashboard/common JSPs | Implemented foundation |
| Role Selection | `RoleSelectionServlet`, `RoleContextUtil`, `RolePermissionUtil`, role selector JSP | Implemented development role flow, protected |
| Product | `ProductManagementServlet`, `ProductDAO`, `Product`, module skeleton JSP flow | DAO/model skeleton and route foundation |
| Category | `CategoryManagementServlet`, `CategoryDAO`, `Category`, module skeleton JSP flow | DAO/model skeleton and route foundation |
| Customer/Supplier | module servlets, DAOs, models, services | Skeleton foundation |
| Purchase/Sales/Order/Payment/Invoice | module servlets, DAOs/models where present, services | Skeleton foundation, protected where financial |
| Inventory/Warehouse/Stock Transfer | module servlets, DAOs/models where present, services | Skeleton foundation |
| Database | `DatabaseUtil`, `sql/DBFinora.sql` | Implemented foundation, needs hardening |
| Build/Deploy | `build.xml`, `nbproject`, `web.xml`, `context.xml` | NetBeans Ant/Tomcat foundation, protected |

Create a dedicated module file here when a module requires ownership notes, invariants, or workflow details.

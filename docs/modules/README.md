# Module Index

This directory tracks module ownership and boundaries.

## Current Modules

| Module | Primary Package / Files | Status |
| --- | --- | --- |
| Dashboard/Foundation | `dashboard`, `foundation`, `common.util.ModuleRegistry`, dashboard/common JSPs | Implemented foundation |
| Role Selection | `auth`, `common.util.RoleContextUtil`, `common.util.RolePermissionUtil`, role selector JSP | Implemented development role flow, protected |
| Product | `product` | DAO/model skeleton and route foundation |
| Category | `category` | DAO/model skeleton and route foundation |
| Customer/Supplier | `customer`, `supplier` | Skeleton foundation |
| Purchase/Sales/Order/Payment/Invoice | `purchase`, `sales`, `order`, `payment`, `invoice` | Skeleton foundation, protected where financial |
| Inventory/Warehouse/Stock Transfer | `inventory`, `warehouse`, `stocktransfer` | Skeleton foundation |
| Database/Common | `common.util.DatabaseUtil`, `sql/DBFinora.sql` | Implemented foundation, needs hardening |
| Build/Deploy | `build.xml`, `nbproject`, `web.xml`, `context.xml` | NetBeans Ant/Tomcat foundation, protected |

## Ownership Rule

Each feature owns its package under `src/java/<feature>`. Team members should work primarily inside the package for their assigned module and avoid touching unrelated feature packages.

`common` is reserved for shared infrastructure and should be changed only when the change is truly cross-feature.

Create a dedicated module file here when a module requires ownership notes, invariants, or workflow details.

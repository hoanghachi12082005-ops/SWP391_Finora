# Architecture Review — Phase 5

## MVC Separation

| Layer | Status | Issues |
|-------|--------|--------|
| **Controller** | 20 servlets in `controller/` | Some contain business logic (validation, transaction mgmt) that belongs in Service layer |
| **Service** | 8 classes in `service/` | 5 deleted as dead code (GenericService + subclasses threw `UnsupportedOperationException`). Only `AuthService`, `SupplierService`, `ActivityLogService`, `PaymentService` have real implementations. Most controllers bypass service layer entirely. |
| **DAO** | 27 classes in `dao/` | Mix of `dao.<domain>.*` (full-featured) and `dao.sales.*` (legacy helpers). Not duplicates but confusing naming. |
| **Model** | 30 POJOs in `model/` | Clean POJOs, some with enum definitions mixed in (e.g. `Employee.EmployeeStatus`) |
| **View** | 57 JSPs + 16 CSS + 1 JS | 3 layout generations coexist (Bootstrap, Custom, Standalone) |

## SOLID Violations

| Principle | Violation | Location |
|-----------|-----------|----------|
| **SRP** | Controllers do too much: auth, validation, DAO calls, flash mgmt, JSON serialization | Every servlet |
| **OCP** | Role-based logic scattered across servlets via `if-else` role checks | `InventoryController.java:76`, `SecurityFilter.java:28-58` |
| **DIP** | Controllers instantiate DAOs directly via `new` instead of injection | All 20 controllers |
| **ISP** | `GenericService<T>` forced all subclasses to implement unused methods | Deleted in this phase |
| **LSP** | Service subclasses that throw `UnsupportedOperationException` break substitution | Deleted in this phase |

## Package Organization

```
src/main/java/
├── constant/          ← NEW (AppConstants.java)
├── controller/        ← 20 servlets across 14 sub-packages
├── dao/               ← 27 classes across 12 sub-packages + common/
├── dto/inventory/     ← 2 DTOs (ExchangeProductDTO, ImportProductDTO)
├── filter/            ← 1 active (SecurityFilter), 1 deleted (AuthFilter)
├── model/             ← 30 POJOs
├── service/           ← 7 classes (after cleanup: AuthService, SupplierService,
│                        SupplierProductService, PurchaseOrderService,
│                        PurchaseDetailService, PaymentService, ActivityLogService)
├── util/              ← 9 utilities
└── temp/              ← DELETED (MigrateDB.java)
```

## Dependencies Between Layers

- **Controller → DAO** (bypasses Service): `CustomerController`, `ProductController`, `DashboardController`, `ProfileServlet`, `AdminUserServlet`, `OrdersServlet`, `CheckoutServlet`
- **Controller → Service → DAO**: `AuthServlet` → `AuthService`, `SupplierServlet` → `SupplierService`
- **No circular dependencies** detected
- **No DI/IoC** — all dependencies are manually instantiated via `new`

## Key Architecture Decisions

1. **SecurityFilter** centralizes auth/authz/CSRF — good. `AuthFilter` deleted.
2. **BaseController** provides `forward()`, `redirect()`, `sendJsonResponse()` — adopted by 5 controllers.
3. **Service layer is effectively dead** — controllers went directly to DAOs during development, services were never populated.
4. **No connection pooling** — `DBContext.getConnection()` creates a new physical JDBC connection each call.
5. **DB credentials** are hardcoded in `DBContext.java` with env var override support (`DB_URL`, `DB_USER`, `DB_PASSWORD`).

## Improvements Made

- Deleted dead code: `GenericService.java`, `ProductService.java`, `CategoryService.java`, `AuthFilter.java`, `DatabaseUtil.java`, `MigrateDB.java`, `persistence.xml`, `beans.xml`, `old source/` directory
- Created `constant/AppConstants.java` for centralized session keys, role names, statuses
- Removed empty directories: `service/common/`, `service/product/`, `test/`, `views/stores/`, `views/notifications/list.jsp`

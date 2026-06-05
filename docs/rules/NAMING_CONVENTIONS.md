# Naming Conventions

## Java Packages

- Root package: none; use direct module packages under `src/java`.
- Servlet controllers: `controller`.
- DAOs: `dao`.
- DTOs: `dto`.
- Models: `model`.
- Services: `service`.
- Utilities: `util`.
- Future JSON API code, if implemented, should use `api`, `api.action`, and `api.dto`.
- Future servlet filters, if implemented, should use `filter`.

## Java Classes

- Servlet controllers end with `Servlet`, for example `ProductManagementServlet`.
- DAO classes end with `DAO`, for example `CategoryDAO`.
- Domain models use entity names, for example `Employee`, `Product`, `Category`.
- DTO classes end with `DTO`, for example `ModuleDTO`.
- Service classes end with `Service`, for example `PaymentManagementService`.
- Utility classes end with `Util`, for example `DatabaseUtil`.
- Registry-style shared metadata classes may end with `Registry`, for example `ModuleRegistry`.

## Java Methods

- Query methods use `get`, `find`, `search`, `count`, or `exists` prefixes.
- Mutation methods use `add`, `update`, `delete`, `register`, or domain-specific verbs.
- Result-set mapping helpers use `extract<Entity>`.
- Request parsing helpers should use explicit names such as `buildCategoryFromRequest` or `normalizeStatus`.

## Web Routes

- Page routes are lowercase and hyphenated where needed, for example `/role-selection`.
- Module routes are grouped by feature area, for example `/product-management`.
- Future API routes should be grouped under `/api`.
- Authentication routes should use simple root paths such as `/login`, `/logout`, `/register`, and `/forgot-password` when implemented.

## JSP Files

- JSP files use lowercase kebab-case where names contain multiple words, for example `role-selection.jsp`.
- Shared JSP fragments live under `web/WEB-INF/views/common`.
- Future auth JSPs should live under `web/WEB-INF/views/auth`.

## Database Naming

The active SQL script uses PascalCase singular table and column names such as `Employee`, `Product`, `Category`, `EmployeeID`, and `CreatedAt`.

Current DAO code must align with `sql/DBFinora.sql` unless a migration plan explicitly changes the convention.

## Build Naming

- WAR name is configured in `nbproject/project.properties` as `SWP391_Finora.war`.
- Generated WAR output belongs under `dist/`.
- Generated classes and copied web content belong under `build/`.

## Documentation Naming

- Governance files use uppercase snake case, for example `SYSTEM_ARCHITECTURE.md`.
- Feature plan files use deterministic uppercase names under topic folders, for example `docs/planning/invoice/INVOICE_IMPLEMENTATION_PLAN.md`.
- Architecture decision records should use `YYYY-MM-DD-short-title.md` under `docs/decisions`.

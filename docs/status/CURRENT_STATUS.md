# Current Status

## Snapshot

- Application type: NetBeans Ant Java Servlet/JSP WAR.
- Runtime target: Apache Tomcat 10.1 with Jakarta Servlet/JSP APIs.
- Java level: JDK 17.
- Database target: SQL Server database `DBFinora`.
- Primary UI: JSP pages with assets under `web/assets`.
- Primary backend access: JDBC DAOs through `DatabaseUtil`.
- Java source root: `src/java` with direct packages `controller`, `dao`, `dto`, `model`, `service`, and `util`.
- Web source root: `web`.
- Build output: `build/` and `dist/SWP391_Finora.war`.

## Implemented Source Areas

- Development dashboard and module skeleton routing exist through `HomeDashboardServlet`, `SkeletonModuleServlet`, and module-specific servlet subclasses.
- Role-selection flow exists through `RoleSelectionServlet`, `RoleContextUtil`, `RolePermissionUtil`, and shared JSP fragments.
- DAO/model skeletons exist for core retail entities such as products, categories, customers, suppliers, orders, payments, warehouses, finance, and inventory movements.
- Service skeletons exist for future module workflows.
- SQL script defines broad retail domain schema including roles, branches, employees, products, categories, orders, payments, finance, warehouse, and related entities.

## Current Architecture State

The application is a development foundation rather than a production-complete system. It is structured for continued module implementation but requires security, validation, database consistency, and full workflow implementation before production use.

## Current High-Risk Items

- Database credentials/configuration must be externalized before production.
- Authentication/password behavior is not production-hardened.
- Many module servlets and services are skeleton routes, not complete business workflows.
- DAO/schema mapping methods are incomplete in several skeleton DAOs.
- No visible automated test suite or CI pipeline.
- Ant is not available in PATH in the observed local shell, so full WAR build may need NetBeans or Ant setup.
- No formal database migration tool.

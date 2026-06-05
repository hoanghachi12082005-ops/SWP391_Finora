# Implemented Features

## Development Foundation

- NetBeans Ant Java WAR project structure is present.
- Tomcat context is configured at `/SWP391_Finora`.
- Java source uses direct packages under `src/java`.
- Web source lives under `web` with JSP views under `web/WEB-INF/views`.

## Dashboard And Module Skeletons

- `HomeDashboardServlet` renders a development dashboard.
- `SkeletonModuleServlet` renders module skeleton pages.
- Module-specific servlet subclasses exist for many business areas.
- `ModuleRegistry` defines module metadata, routes, owner suggestions, database mappings, and grouped actions.

## Role Selection / Authorization Foundation

- `RoleSelectionServlet` supports shared development role switching.
- `RoleContextUtil` reads the current role from request/session state.
- `RolePermissionUtil` defines role-based allowed actions for module skeletons.
- Shared role selector JSP fragment exists.

## Persistence Foundation

- SQL Server schema and seed script exists at `sql/DBFinora.sql`.
- `DatabaseUtil` centralizes JDBC connection creation.
- DAO skeletons exist for core retail entities.
- Model classes exist for core retail entities.

## Presentation Foundation

- Dashboard JSP exists.
- Shared header, footer, role selector, and skeleton page JSP fragments exist.
- Static assets live under `web/assets`.

## Not Yet Complete

- Full authentication/login/register/reset flows are not complete source features in the current tree.
- Full CRUD screens are not complete for all modules.
- Future JSON API routing is not currently present in source.
- Production-grade security, test automation, and CI are not yet implemented.

# Module Boundaries

## Current Modules

| Module | Source Areas | Responsibility | Protection Level |
| --- | --- | --- | --- |
| Dashboard/Foundation | `dashboard`, `foundation`, `common.util.ModuleRegistry`, dashboard/common JSPs | Development landing page and module skeleton routing | Normal |
| Role Selection | `auth`, `common.util.RoleContextUtil`, `common.util.RolePermissionUtil`, role selector JSP | Shared development role switching and allowed-action display | Protected authorization dependency |
| Product Management | `product` | Product data access foundation | Normal |
| Category Management | `category` | Category data access foundation | Normal with authorization dependency |
| Persistence | feature DAOs, `common.util.DatabaseUtil`, `sql/DBFinora.sql` | JDBC access and database schema | Protected |
| Presentation | JSPs, CSS, JS | Server-rendered pages and client behavior | Normal except auth/session views when added |
| Build/Deploy | `build.xml`, `nbproject`, `web.xml`, `context.xml` | Ant WAR build and Tomcat deployment | Protected infrastructure |

## Boundary Rules

- A module owns its package under `src/java/<feature>`.
- A module may own its servlet, DAO, model, JSP, and future API/action code when applicable.
- Developers should modify only the package for their assigned feature plus docs for that feature.
- Changes to `common` require a clear cross-feature use case or team approval.
- Cross-module data access must go through DAOs or future service methods, not direct table access from unrelated controllers.
- Authorization checks must stay centralized through `common.util.RolePermissionUtil` or a future dedicated authorization component.
- Module-specific validation should remain near the controller until it becomes shared or complex enough to justify a service.
- Schema changes must be reflected in DAOs and database documentation in the same change.
- Build/deploy configuration changes must be documented because this is a NetBeans Ant/Tomcat project.

## Protected Module Change Process

Before changing a protected module:

1. Identify all direct imports and route mappings.
2. Identify all JSP/session attributes used by the flow.
3. Identify database tables and columns touched by the change.
4. Identify build/deploy impact when touching Ant, NetBeans, Tomcat, or `web.xml` config.
5. Describe risks in the plan or implementation summary.
6. Make the smallest possible source change.
7. Run relevant verification or document why it could not run.

## Current Boundary Risks

- Authentication and password storage rules are not production-hardened.
- Database schema naming must remain aligned with DAO SQL as module implementations grow.
- Some controllers may grow large once real CRUD flows are implemented.
- Service packages exist as skeletons and should not be treated as completed business logic.

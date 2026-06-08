# System Architecture

## Current System

KiotRetail / SWP391_Finora is a NetBeans Ant Java WAR application using a traditional server-rendered MVC structure.

The application now uses **feature-owned Java packages** under `src/java`. Each business feature owns its controller, DAO, model, service, and DTO files inside one package tree where applicable.

The current application surface includes:

- Feature packages in `src/java/<feature>` such as `product`, `category`, `customer`, `sales`, `order`, and `payment`
- Shared infrastructure in `src/java/common`
- Servlet controllers in each feature's `controller` subpackage
- JDBC DAOs in each feature's `dao` subpackage
- Domain models in each feature's `model` subpackage
- Service skeletons in each feature's `service` subpackage
- JSP views in `web/WEB-INF/views`
- Static assets in `web/assets`
- SQL Server schema scripts in `sql`
- NetBeans Ant build configuration through `build.xml` and `nbproject/project.properties`

## Runtime Flow

1. Browser requests enter Tomcat under the `/SWP391_Finora` context.
2. `web/WEB-INF/web.xml` configures listeners, servlets, database context params, and welcome files.
3. Servlet mappings route page requests to feature controllers or shared foundation controllers.
4. Controllers parse request parameters, call utilities/DAOs as needed, set request/session attributes, and forward or redirect.
5. DAOs use `common.util.DatabaseUtil.getConnection()` and JDBC prepared statements to query SQL Server.
6. JSP views under `WEB-INF/views` render server-side HTML using request/session state.
7. Static assets are served from `web/assets`.

## Architectural Layers

| Layer | Package/Path | Responsibility |
| --- | --- | --- |
| Web entry | `web.xml`, servlet mappings | URL mapping, listener setup, session and context configuration |
| Feature controllers | `<feature>.controller` | Request flow, validation, forwarding, redirects for one feature |
| Feature DTO | `<feature>.dto` when needed | Feature-specific view/module data shapes |
| Shared DTO | `common.dto` | Cross-feature DTOs used by dashboard/foundation flows |
| Feature DAO | `<feature>.dao` | SQL, JDBC, result-set mapping, persistence operations for one feature |
| Feature model | `<feature>.model` | Domain data carriers for one feature |
| Feature service | `<feature>.service` | Future multi-DAO workflow/business services for one feature |
| Common utility | `common.util` | Focused shared helpers for database, module registry, roles, permissions |
| Common web | `common.web` | Application startup/listener infrastructure |
| Foundation | `foundation.controller` | Shared skeleton module routing |
| View | `WEB-INF/views` | JSP rendering |
| Assets | `assets` | CSS and browser JavaScript |
| Database | `sql` | Schema and seed scripts |

## Current Patterns

- MVC servlet + JSP pages for dashboard and module skeleton UI.
- Feature packages own their controller, DAO, model, and service files.
- DAO classes own SQL and map `ResultSet` values into model objects.
- `common.util.ModuleRegistry` centralizes development module metadata for the dashboard and skeleton pages.
- Role selection uses session/request state and `common.util.RolePermissionUtil` to expose allowed module actions.
- Flash/session patterns should stay consistent until a shared helper is introduced.
- Soft delete is represented through `Status` updates for selected entities.

## Known Architecture Risks

- Database credentials are configured in source/config and must be externalized before production use.
- SQL naming must stay aligned with `sql/DBFinora.sql` as DAOs are completed.
- Service classes are mostly skeletons and must not be treated as complete business workflows.
- Authentication/password behavior is not production-hardened yet.
- Controllers may grow large as features are implemented; extract helpers/services only when reuse or complexity is proven.
- No formal automated test suite or CI pipeline is currently configured.

## Evolution Direction

Do not introduce architecture layers speculatively. Add or expand service classes only when a business workflow is reused by multiple controllers/API actions or needs transaction boundaries across multiple DAOs.

The preferred evolution path is:

1. Stabilize schema naming and credential configuration.
2. Standardize authentication/password behavior before production use.
3. Implement DAOs and servlet flows incrementally inside feature-owned packages.
4. Extract repeated request parsing and validation only after repeated patterns are proven.
5. Introduce service-layer boundaries for multi-step workflows such as checkout, inventory movement, payments, and reporting.
6. Add tests around protected modules before refactoring them.

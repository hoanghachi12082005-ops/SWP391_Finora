# System Architecture

## Current System

KiotRetail / SWP391_Finora is a NetBeans Ant Java WAR application using a traditional server-rendered MVC structure.

The current application surface includes:

- Servlet controllers in `src/java/controller`
- JDBC DAOs in `src/java/dao`
- View/module DTOs in `src/java/dto`
- Domain models in `src/java/model`
- Service skeletons in `src/java/service`
- Shared utility classes in `src/java/util`
- JSP views in `web/WEB-INF/views`
- Static assets in `web/assets`
- SQL Server schema scripts in `sql`
- NetBeans Ant build configuration through `build.xml` and `nbproject/project.properties`

## Runtime Flow

1. Browser requests enter Tomcat under the `/SWP391_Finora` context.
2. `web/WEB-INF/web.xml` configures listeners, servlets, database context params, and welcome files.
3. Servlet mappings route page requests to controller classes under `controller`.
4. Controllers parse request parameters, call utilities/DAOs as needed, set request/session attributes, and forward or redirect.
5. DAOs use `DatabaseUtil.getConnection()` and JDBC prepared statements to query SQL Server.
6. JSP views under `WEB-INF/views` render server-side HTML using request/session state.
7. Static assets are served from `web/assets`.

## Architectural Layers

| Layer | Package/Path | Responsibility |
| --- | --- | --- |
| Web entry | `web.xml`, servlet mappings | URL mapping, listener setup, session and context configuration |
| Controllers | `controller` | Request flow, validation, forwarding, redirects |
| DTO | `dto` | View/module data shapes that are not domain entities |
| DAO | `dao` | SQL, JDBC, result-set mapping, persistence operations |
| Model | `model` | Domain data carriers |
| Service | `service` | Future multi-DAO workflow/business services; currently skeletons |
| Utility | `util` | Focused shared helpers for database, module registry, roles, permissions |
| View | `WEB-INF/views` | JSP rendering |
| Assets | `assets` | CSS and browser JavaScript |
| Database | `sql` | Schema and seed scripts |

## Current Patterns

- MVC servlet + JSP pages for dashboard and module skeleton UI.
- DAO classes own SQL and map `ResultSet` values into model objects.
- `ModuleRegistry` centralizes development module metadata for the dashboard and skeleton pages.
- Role selection uses session/request state and `RolePermissionUtil` to expose allowed module actions.
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
3. Implement DAOs and servlet flows incrementally by module.
4. Extract repeated request parsing and validation only after repeated patterns are proven.
5. Introduce service-layer boundaries for multi-step workflows such as checkout, inventory movement, payments, and reporting.
6. Add tests around protected modules before refactoring them.

# Dependency Flow

## Allowed Direction

Dependencies must flow from web entry points toward persistence helpers. DAOs stay isolated from servlet/JSP concerns.

```text
JSP views
  ↑ request attributes/session attributes
Controllers
  → DTOs / Models
  → focused Utilities
  → DAOs
Services (when real workflows are implemented)
  → DAOs
  → Models / DTOs
DAOs
  → DatabaseUtil
  → Models
DatabaseUtil
  → JDBC driver
```

## Allowed Dependencies

| From | May Depend On |
| --- | --- |
| `controller` | `dao`, `dto`, `model`, focused `util`, `service` only for real workflows, servlet APIs |
| `service` | `dao`, `dto`, `model`, focused `util`, Java standard APIs |
| `dao` | `model`, `util.DatabaseUtil`, JDBC APIs |
| `dto` | Java standard library and other DTOs only |
| `model` | Java standard library only |
| `util` | Java/Jakarta/JDBC libraries as needed for focused cross-cutting helpers |
| `JSP` | request/session attributes, JSP/JSTL/taglibs when available, static assets |

Future `api` or `filter` packages must follow the same inward dependency rule and be documented when introduced.

## Disallowed Dependencies

- DAO classes must not depend on servlet request, response, session, JSP APIs, or controller classes.
- Model classes must not depend on DAOs, controllers, servlet APIs, or database utilities.
- DTO classes must not open database connections or access request/session state.
- JSP files must not open database connections, instantiate DAOs, or execute SQL.
- Controllers must not contain raw JDBC calls.
- Utility classes must not become hidden service layers.
- Service classes must not depend on JSP or servlet request/response objects.

## Protected Dependency Rules

- Authentication/session flow depends on session attributes and role-selection behavior. Changes must preserve role selection and protected route behavior when implemented.
- Database access depends on `DatabaseUtil`. Credential or connection changes affect every DAO.
- Build/runtime compatibility depends on `build.xml`, `nbproject/project.properties`, `web.xml`, and Tomcat 10.1 APIs.

## Service Layer Policy

The `service` package currently contains skeletons for future workflows. Do not add real service behavior for a single controller calling a single DAO.

Introduce or expand service logic only when at least one condition is true:

- A workflow spans multiple DAOs.
- Logic must be reused by JSP controllers and future JSON API actions.
- A transaction boundary must cover multiple persistence operations.
- Complex domain validation no longer belongs in a servlet.

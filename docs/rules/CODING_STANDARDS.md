# Coding Standards

## Java Standards

- Use feature-owned Java packages under `src/java`, for example `product.controller`, `product.dao`, `product.model`, and `product.service`.
- Use `common` only for cross-feature infrastructure, shared DTOs, or focused utilities.
- Keep servlet methods focused on request handling and delegation.
- Keep DAO methods focused on a single persistence operation.
- Use `PreparedStatement` for all SQL with user input.
- Use try-with-resources for `Connection`, `PreparedStatement`, `Statement`, and `ResultSet`.
- Avoid `System.out.println` for production diagnostics in new code.
- Prefer clear, direct code over premature abstractions.
- Do not swallow exceptions silently. If no logging framework exists, return safe failure values and plan logging improvement.
- Save Java files as UTF-8 without BOM.

## Package Ownership Standards

- Developers should primarily edit the package for their assigned feature.
- Do not modify another feature package without coordination or a documented reason.
- Do not move code into `common` unless at least two real features need it or the code is application infrastructure.
- Do not call another feature's DAO directly from a controller without an approved workflow plan.

## JSP Standards

- JSPs render data passed by controllers.
- Do not execute SQL or instantiate DAOs in JSPs.
- Keep shared layout fragments under `web/WEB-INF/views/common`.
- Use existing CSS assets before creating new style files.
- Preserve UTF-8 handling for Vietnamese text.
- Save JSP files as UTF-8 without BOM.

## DAO Standards

- DAO classes own SQL strings, parameter binding, and result mapping.
- Private `extract<Entity>` methods are the preferred mapping pattern.
- Do not return raw `ResultSet` or JDBC objects outside DAOs.
- Avoid dynamic SQL unless it is parameterized and narrowly scoped.
- Keep table/column naming aligned with `sql/DBFinora.sql`.

## Controller Standards

- Validate request parameters before calling DAOs.
- Use redirects after successful POST operations.
- Use forwards for initial page rendering and validation failure when preserving request attributes.
- Use session flash messages consistently through `message` and `messageType` until a shared helper is introduced.
- Do not put database access or multi-table business transactions in controllers.

## Service Standards

- Feature service packages currently contain workflow skeletons.
- Add real service behavior only for multi-DAO workflows, transaction boundaries, or logic reused by multiple controllers/future APIs.
- Services must not depend on JSPs or servlet request/response/session objects.

## Future API Standards

- Add an `api` package only when JSON endpoints are implemented.
- API actions should use DTOs for response shapes that differ from domain models.
- Register and document every future API route in `docs/api/API_STANDARDS.md`.

## Security Standards

- Do not hardcode secrets in new code.
- Never log passwords, reset tokens, session identifiers, or credentials.
- Use centralized password hashing behavior before production deployment.
- Protect admin, POS, payment, finance, and database administration routes.

# Service Patterns

## Current Status

The repository currently has a `service` package with skeleton service classes for future module workflows.

Controllers may call DAOs directly for simple skeleton or CRUD flows. Do not expand services into generic pass-through wrappers.

## When To Add Real Service Logic

Add real service behavior only when a workflow:

- Coordinates multiple DAOs.
- Requires a transaction across multiple SQL operations.
- Is reused by multiple servlet controllers or future API actions.
- Contains business rules that would make a servlet large or hard to test.

## Service Responsibilities

Services may:

- Validate domain-level business rules.
- Coordinate DAO calls.
- Own transaction boundaries once transaction support is introduced.
- Return domain models, DTOs, or operation result objects.

Services must not:

- Depend on JSPs.
- Write directly to `HttpServletResponse`.
- Store or read servlet session state.
- Hide database credentials or connection creation outside the established persistence strategy.
- Duplicate DAO methods without adding workflow value.

## Candidate Future Services

- `AuthenticationService` after password behavior is standardized.
- `InventoryManagementService` for stock movement and warehouse rules.
- `SalesManagementService` or checkout workflow service for order, order detail, inventory, payment, and finance transaction coordination.
- `ReportService` for aggregate reads and report generation.

# Database Architecture

## Current Database Target

The primary database target is SQL Server with database name `DBFinora`.

The authoritative active schema script is:

- `sql/DBFinora.sql`

## Core Tables In Current Schema

- `Role`
- `Branch`
- `Employee`
- `Customer`
- `Supplier`
- `Category`
- `Product`
- `Warehouse`
- `Orders`
- `OrderDetail`
- `Payments`
- `FinanceTransaction`
- `StockTransfer`
- `WarehouseTransaction`
- `AuditLog`

Review the full SQL script before database changes.

## Persistence Pattern

- DAOs use `DatabaseUtil.getConnection()`.
- DAOs use JDBC and `PreparedStatement`.
- Result mapping is handled inside DAO-private methods.
- No migration framework is currently present.
- DAO source lives under `src/java/dao`.
- Domain models live under `src/java/model`.

## Naming Rule

The active SQL Server schema uses PascalCase singular table and column names such as `Employee`, `Product`, `Category`, `EmployeeID`, and `CreatedAt`.

DAO code must align with `sql/DBFinora.sql` unless a migration plan explicitly changes the convention.

## Database Governance Rules

- Do not change schema without updating DAO SQL and database docs.
- Do not introduce new schema files without documenting which file is authoritative.
- Do not store production credentials in SQL scripts, Java files, XML files, or README examples.
- Use soft delete through status fields where existing patterns already use `Status = 'inactive'`.
- Payment, finance, order, and inventory schema changes require explicit planning and audit review.

## Migration Direction

Before production use, add a database migration workflow. Candidate approaches:

- Manual numbered SQL migrations under `sql/migrations`.
- Flyway if the team accepts an added dependency and process.
- Liquibase if rollback metadata and change tracking are required.

Do not introduce a migration tool without an architecture decision record.

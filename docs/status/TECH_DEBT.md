# Technical Debt

## Critical

- Database credentials/configuration must be externalized before production use.
- Authentication/password behavior is not production-hardened.
- Protected route/session behavior needs full implementation and regression tests before production.

## High

- DAO SQL and result mapping are incomplete in multiple skeleton DAOs.
- Module-specific servlets and services are mostly skeletons and need real workflows.
- Error handling strategy is not production-grade.
- There is no migration/versioning system for database changes.
- No CSRF protection strategy is documented for POST forms.
- Full Ant WAR build cannot be run from the observed shell until Ant is available in PATH or NetBeans build is used.

## Medium

- Controllers may accumulate repeated parameter parsing, validation, and flash-message logic as modules are implemented.
- The service package exists but should only be expanded for real multi-DAO workflows.
- Future API conventions are documented but no current `api` source package exists.
- JSP compilation is not covered by the fallback Java compile smoke test.
- No CI pipeline or automated regression suite is configured.

## Low

- Some detailed planning documents are business-analysis notes and may not exactly match implemented source state.
- Documentation must stay synchronized with the NetBeans Ant layout and direct Java package convention.

# Security Rules

## Current Security Model

- `RoleSelectionServlet` and `RoleContextUtil` provide a development role-selection flow.
- `RolePermissionUtil` centralizes role permission checks for module skeleton actions.
- `web.xml` marks cookies as HTTP-only and sets a 30-minute default session timeout.
- Full authentication/login/logout/register/reset flows are not complete source features in the current tree.

## Mandatory Rules

- Do not log credentials or secrets.
- Do not hardcode production credentials.
- Do not store raw passwords in production-ready flows.
- Do not compare raw passwords once password hashing is standardized.
- Do not bypass `RolePermissionUtil` for role-sensitive checks.
- Do not expose JSPs directly outside `WEB-INF/views`.
- Do not add state-changing GET endpoints.
- Validate all request parameters before persistence calls.
- Keep database credentials out of committed production configuration.

## Known Security Gaps

- Database credentials/configuration are local-development oriented and must be externalized before production.
- Full authentication/session security behavior is not production-hardened.
- Password hashing and account lifecycle flows need a defined implementation and migration strategy.
- CSRF protection is not documented or implemented for forms.
- Cookie `secure` settings must be reviewed for HTTPS production.
- Error handling may expose stack traces in logs without structured sanitization.

## Security Refactor Priority

1. Externalize database credentials.
2. Implement and standardize authentication/session flows.
3. Standardize password hashing and migration strategy.
4. Add CSRF protection to POST forms.
5. Review role coverage for all management and POS features.
6. Add audit logging for authentication, authorization denial, finance, inventory, and payment operations.

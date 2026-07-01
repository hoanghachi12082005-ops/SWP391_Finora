# Refactoring Plan — Phase 5

## Completed Refactoring

### File Deletions (11 files, 4 directories)
| Path | Reason |
|------|--------|
| `service/common/GenericService.java` | Dead code — all methods throw UnsupportedOperationException |
| `service/product/ProductService.java` | Dead code — extended GenericService, never used |
| `service/product/CategoryService.java` | Dead code — extended GenericService, never used |
| `service/common/` (dir) | Empty after deletion |
| `service/product/` (dir) | Empty after deletion |
| `filter/AuthFilter.java` | Dead code — @WebFilter commented out, superseded by SecurityFilter |
| `util/DatabaseUtil.java` | Dead code — duplicate of DBContext with stale DBFinoraV2 config |
| `temp/MigrateDB.java` | One-time migration script, no longer needed |
| `resources/META-INF/persistence.xml` | Empty JPA config — project uses raw JDBC |
| `WEB-INF/beans.xml` | Empty CDI config — CDI not used |
| `old source/` (dir + 4 files) | Stale files from previous version |
| `test/` (dir) | Empty |
| `views/stores/` (dir) | Empty |
| `views/notifications/list.jsp` | Unused JSP |

### Code Created
| Path | Purpose |
|------|---------|
| `constant/AppConstants.java` | Centralized constants for session keys, role names, statuses, pagination defaults, flash message keys |

### Config Externalization
- `DBContext.java` already supports env var overrides (`DB_URL`, `DB_USER`, `DB_PASSWORD`) — documented in codebase
- VNPAY credentials in `util/vnpay/Config.java` still hardcoded — needs env var migration (documented below)

## Remaining Refactoring (Priority Order)

### Sprint 1: Configuration & Security (4h)

| Task | Effort | Description |
|------|--------|-------------|
| 1. Add Tomcat JDBC pool in context.xml | 30min | Replace DriverManager with JNDI datasource, add `maxTotal=20` |
| 2. Move VNPAY secrets to env vars | 30min | `Config.java`: read `VNP_TMNCODE`, `VNP_HASHSECRET`, `VNP_PAYURL`, `VNP_RETURNURL` from env |
| 3. Add SLF4J+Logback dependency | 1h | Add to pom.xml, replace top 20 `e.printStackTrace()` with logger.error() |
| 4. Replace hardcoded strings with AppConstants refs | 2h | Status values in DAOs, role names in controllers, session keys |

### Sprint 2: DAO & Query Optimization (8h)

| Task | Effort | Description |
|------|--------|-------------|
| 5. Add pagination to OrderDAO.getAllSaleOrders() | 1h | Add OFFSET-FETCH, page/limit params |
| 6. Add pagination to CustomerDAO.findByKeyword() | 1h | Same pattern |
| 7. Create DB migration SQL for recommended indexes | 30min | Generate `phase5_indexes.sql` |
| 8. Add connection-pool-aware DBContext with JNDI | 2h | Update DBContext to support `java:comp/env/jdbc/DBFinoraV3` |
| 9. Consolidate `dao.sales.*` DAOs into domain DAOs | 3h | Move shared methods from `sales.InventoryDAO.getStock()` → `inventory.InventoryDAO`, retire sales package |

### Sprint 3: Service Layer Activation (8h)

| Task | Effort | Description |
|------|--------|-------------|
| 10. Refactor AuthServlet → delegate validation to AuthService | 1h | Move forgot-password validation to AuthService |
| 11. Refactor CheckoutServlet transaction logic into service | 3h | Extract `CheckoutService.processCheckout()` |
| 12. Refactor OrdersServlet.refund into service | 1h | Extract `RefundService.processRefund()` |
| 13. Refactor ProfileServlet password change into service | 1h | Move to `AuthService.changePassword()` |
| 14. Create BaseService with common transaction handling | 2h | Centralize `getConnection()`, `beginTransaction()`, `commit()`, `rollback()` |

### Sprint 4: Code Quality (12h)

| Task | Effort | Description |
|------|--------|-------------|
| 15. Decompose InventoryController (225+280 line methods) | 4h | Split into `StockController`, `TransferController`, `CheckController` or separate handlers |
| 16. Decompose CustomerDAO (814 lines) | 2h | Split loyalty/overview/search into separate DAOs |
| 17. Remove backward-compat aliases from Order model | 1h | Remove getOrderID, getCusID, getEmpID, getOrderDate |
| 18. Standardize logging across all DAOs | 2h | Replace printStackTrace with log.error, add log.debug for SQL |
| 19. Remove FontAwesome, use Material Icons everywhere | 3h | Replace 7 icon references in 2 JSPs, delete `assets/fontawesome/` |

## Phase 5 Architecture Principles (for future development)

1. **Controllers** — receive request, delegate to service, handle response. No business logic.
2. **Services** — contain business rules, validation, transaction management. Call DAOs.
3. **DAOs** — one SQL concern per method. No business logic. Use try-with-resources for connections.
4. **Constants** — never hardcode status/role/session/URL strings. Use `AppConstants.*`.
5. **Config** — DB credentials, API keys, URLs go to env vars. Never in source code.
6. **Exception handling** — controllers catch and translate to user messages. Services throw business exceptions. DAOs throw SQLException (let service layer wrap).
7. **Logging** — SLF4J with parameterized logging. DEBUG for SQL/trace, INFO for biz events, WARN for validation fails, ERROR for failures.
8. **No dead code** — if it's not used, delete it. Don't comment it out.

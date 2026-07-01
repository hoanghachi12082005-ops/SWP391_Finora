# Code Quality Report — Phase 5

## Clean Code Issues

### Naming

| Issue | Example | Count |
|-------|---------|-------|
| Inconsistent method naming | `getEmployeeId()` vs `getEmpId()`, `getEmployeeID()` vs `getEmployeeId()` | 10+ variants across Employee model |
| Duplicate model methods | `getEmployeeID()` → delegates to `getEmployeeId()`, `getCusID()` → delegates to `getCustomerId()` | 6 backward-compat aliases in Order.java |
| Unclear DAO names | `dao.sales.InventoryDAO` vs `dao.inventory.InventoryDAO` | 5 confusing package pairs |
| Inconsistent case | `ProfileDao.java` (capital D, small o) vs other DAOs (`OrderDAO.java`) | 1 outlier |

### Magic Numbers & Hardcoded Strings

| Value | Location | Recommendation |
|-------|----------|----------------|
| `8` (VAT %) | `CheckoutServlet.java:87` | Constant |
| `100_000` (point conversion) | `CustomerPointDAO.java:56` | Constant |
| `5` (max failed login) | `Employee.MAX_FAILED_LOGIN` | OK — already constant |
| `12` (BCrypt work factor) | `PasswordUtil.java:10` | OK — private constant |
| `20` (page size) | `InventoryController.java:273` | Should use AppConstants |
| Role strings | 15+ servlets | `Admin`, `Owner`, `StoreManager` hardcoded |
| Status strings | 20+ DAOs | `ACTIVE`, `INACTIVE`, `COMPLETED`, `PENDING` hardcoded |

### Long Methods

| Method | Lines | Location |
|--------|-------|----------|
| `InventoryController.doGet()` | ~225 | Lines 35-259 |
| `InventoryController.doPost()` | ~280 | Lines 361-643 |
| `UserManagementDao` (various) | ~1087 total | Whole file is too large |
| `BranchController.doPost()` | ~170 | BranchController.java |
| `CashTransactionDAO.executeTransaction` | ~80 | Complex transaction logic |

### Deep Nesting

| Location | Max Depth | Issue |
|----------|-----------|-------|
| `InventoryTicketDAO.confirmReceiptWithDiscrepancy` | 6 levels | try > try > for > try > if > try |
| `InventoryController.doGet` | 5 levels | try > if > if > switch > for |
| `BranchValidator.validate` | 5 levels | Multiple if-else chains |

### Comment Quality

- Mixed Vietnamese/English comments — 60% Vietnamese, 40% English
- Some auto-generated NetBeans headers (`@author PCQN`, license templates)
- Several `// FIX:` comments in ProfileServlet and AuthServlet from earlier phases
- Commented-out code blocks in `OwnerUserServlet.java` (~50 lines), `UserManagementDao.java` (~10 lines)

### Duplicate Utility Code

| Pattern | Occurrences | Location |
|---------|-------------|----------|
| `isBlank()` method | 6+ | ProfileServlet, AdminUserServlet, OwnerUserServlet, ValidationUtil, BranchValidator |
| `trim()` helper | 4 | ProfileServlet, AdminUserServlet, etc. |
| `parseInt()` with default | 4 | AdminUserServlet, OwnerUserServlet, etc. |
| `getConnection()` pattern | 27 DAOs | Every DAO has same try-with-resources pattern |
| `setFlash()` pattern | 8+ servlets | Flash message via session attributes |
| `escJson()` method | 2 | CheckoutServlet, OrdersServlet |

## Improvements Made

- Created `AppConstants.java` — target for replacing hardcoded strings incrementally
- Deleted 5 dead service classes that were empty shells
- Removed 4 stale config files (persistence.xml, beans.xml, old source)
- Removed `AuthFilter.java` (dead code, superseded by SecurityFilter)
- Removed `DatabaseUtil.java` (dead code, duplicate of DBContext with stale DBFinoraV2 config)

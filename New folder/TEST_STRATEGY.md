# Test Strategy

## 1. Approach

Static analysis-based QA with business flow tracing. Since no running deployment is available, each test case is validated by code inspection:

1. **Code path analysis** — trace each request from controller → DAO → database
2. **Input validation audit** — check each parameter for null/format/boundary handling
3. **Transaction integrity review** — verify rollback on failure for multi-step operations
4. **Permission matrix verification** — compare SecurityFilter role map against actual controller access
5. **Error handling review** — classify each catch block (logged, exposed, swallowed)

## 2. Test Environment

| Parameter | Value |
|-----------|-------|
| Application Server | Apache Tomcat 10.1 |
| Java Version | 17 |
| Database | SQL Server (DBFinoraV3) |
| JDBC Driver | mssql-jdbc 12.6.1 |
| Build Tool | Maven 3.x |

## 3. Test Data Requirements

| Entity | Test Data Needed | Notes |
|--------|-----------------|-------|
| Employee | 5+ records across roles | Admin, Owner, StoreManager, Sales, Warehouse |
| Customer | 3+ records | Mix of active/inactive, with/without points |
| Product | 5+ records | Mix of statuses, categories, with inventory |
| Branch | 2+ branches | With warehouses |
| Order | 3+ records | PENDING, COMPLETED, CANCELLED statuses |
| Inventory | Records per warehouse | Mix of normal/low-stock/OOS |
| Voucher | 2+ records | Active and expired |

## 4. Test Types

### 4.1 Functional Testing (22 modules)
Each module tested for: Create, Read, Update, Delete, Search, Filter, Pagination, Validation, Permissions

### 4.2 Security Testing
- **SQL Injection**: 4 DAO methods had injection vulnerabilities (fixed Phase 2).
- **CSRF**: SecurityFilter validates tokens. 5 GET actions still perform mutations (SupplierServlet.delete).
- **XSS**: JSON builders use `escJson()`. JSP EL expressions auto-escape. Risk: LOW.
- **Auth bypass**: 14 controllers have no SecurityFilter role mapping.
- **Session fixation**: Fixed Phase 2 (session invalidation on login).

### 4.3 Transaction Testing
| Flow | Steps | Rollback Risk |
|------|-------|---------------|
| Checkout | Stock check → Order create → Detail insert → Payment → Stock deduct → Points → Voucher → Complete | All in one transaction (fixed Phase 5 to catch Exception) |
| Refund | Status check → Stock restore → Points reverse → Status update → Audit log | In one transaction (Phase 5 fix) |
| Transfer dispatch | Ticket status → Detail update → Stock deduct → Transaction log → Complete check | In one transaction with guard (Phase 5 fix) |

### 4.4 Boundary Testing
| Input | Test Values |
|-------|-------------|
| String length | 0, 1, 255, 1000, Unicode, emoji |
| Numbers | 0, -1, MAX_INT, MIN_INT, decimals |
| Dates | null, invalid format, leap year, future, past |
| Email | valid@a.co, no@, @nouser.com, empty |
| Phone | "", "0", "0123456789", "abc" |

## 5. Pass/Fail Criteria

| Criteria | Threshold |
|----------|-----------|
| Critical bugs | 0 |
| High severity bugs | 0 |
| Medium severity bugs | ≤ 5 |
| Auth endpoints with missing checks | 0 |
| SQL injection vulnerabilities | 0 |
| Transaction rollback failures | 0 |

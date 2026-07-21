# Sales & Transaction Report — Design Spec

## Objective

Move the placeholder `/reports/finance-detail` from a redirect to a fully implemented Sales & Transaction Report in the Reports module. Reuse existing `PaymentDAO`/`PaymentService` infrastructure. No new business logic duplication.

## Architecture

```
SalesTransactionReportController  (controller.report)
  ├─ uses → PaymentService         (service.finance)     — passthrough to DAO
  │     └─ uses → PaymentDAO       (dao.finance)         — payment CRUD (unchanged)
  ├─ uses → SalesTransactionReportDAO (dao.report)       — report-specific queries (KPI, enhanced filter, distinct types)
  └─ renders → JSP partials        (views/reports/)
        ├─ sales-transaction-report.jsp    (layout)
        ├─ _transaction_filter.jsp         (form)
        ├─ _transaction_kpi.jsp            (6 cards)
        └─ _transaction_table.jsp          (data table)
```

### Key rules
- `SalesTransactionReportDAO` is new — does NOT modify `PaymentDAO`
- `PaymentService` gets new passthrough methods for report queries
- Excel/PDF export utilities receive pre-fetched data from Service, never query themselves
- KPI computed in a single SQL pass (one query for payment aggregates, one for order count)

## Data Model

### SalesTransactionFilter
```
datePreset: String       // today, yesterday, this_week, this_month, custom
dateFrom/to: LocalDate   // custom range
transactionCode: String  // exact match on transaction_code
transactionType: String  // INCOME, EXPENSE, etc. (from DISTINCT query)
paymentMethod: String
amountFrom/to: Double
branchId: Integer
empId: Integer
keyword: String          // searches transaction_code, description, employee name
sortBy: String           // whitelisted: payment_date, payment_amount, PaymentType, branch_name, employee_name
sortDir: String          // ASC, DESC
```

### SalesTransactionKpi
```
totalTransactions: int
totalRevenue: double
totalExpense: double
netCashFlow: double      // Revenue - Expense (Java)
avgTransactionValue: double
totalSalesOrders: int
```

### SalesTransaction (display row)
```
rowNum: int
transactionCode: String
paymentDate: String
transactionType: String
paymentMethod: String
amount: double
description: String
branchName: String
employeeName: String
status: String
orderId: Integer
```

## SQL Strategy

### Single KPI query (payment table)
```sql
SELECT
  COUNT(*) AS total_transactions,
  SUM(CASE WHEN PaymentType = 'INCOME' THEN payment_amount ELSE 0 END) AS total_revenue,
  SUM(CASE WHEN PaymentType = 'EXPENSE' THEN payment_amount ELSE 0 END) AS total_expense,
  AVG(payment_amount) AS avg_transaction_value
FROM payment p
LEFT JOIN Employee e ON p.EmployeeID = e.emp_id
WHERE 1=1
  [AND time-range / amount-range / type / method / branch / employee / keyword filters]
```

### Sales orders count
```sql
SELECT COUNT(*) FROM [Order] WHERE order_type = 'SALE'
  [AND time-range / branch / employee filters matching payment filters]
```

### Transaction types (distinct)
```sql
SELECT DISTINCT PaymentType FROM payment ORDER BY PaymentType
```

### Transaction listing (enhanced from existing PaymentDAO)
Same base query as PaymentDAO.getTransactionsPaging but with:
- Enhanced keyword search: transaction_code, description, e.fullName
- Additional filters: amount range, employee ID, branch ID
- Sort whitelist enforcement in DAO

## Controller

`SalesTransactionReportController` mapped to:
- `/reports/finance-detail` — GET: show report page
- `/reports/finance-detail/export-excel` — GET: download Excel
- `/reports/finance-detail/export-pdf` — GET: download PDF

Flow:
1. Auth check (`isOwnerOrManager`)
2. Branch constraint for StoreManager
3. Build `SalesTransactionFilter` from request params
4. Call `PaymentService.getTransactionKpi(filter)` + `salesTransactionReportDAO.searchTransactions(filter, page, pageSize)`
5. Set request attributes, forward to JSP

Export flow:
1. Same auth + filter building
2. Call Service to get ALL data (KPI + full transaction list)
3. Pass to `ExcelExportUtil`/`PdfReportUtil` — these only render, never query

## KPI Formulas

| Metric | Formula | Source |
|--------|---------|--------|
| Total Transactions | `COUNT(*)` | payment table |
| Total Revenue | `SUM(amount) WHERE PaymentType='INCOME'` | payment table |
| Total Expense | `SUM(amount) WHERE PaymentType='EXPENSE'` | payment table |
| Net Cash Flow | Revenue − Expense | Java calculation |
| Avg Transaction Value | `AVG(amount)` | payment table |
| Total Sales Orders | `COUNT(*) WHERE order_type='SALE'` | order table (separate query) |

## JSP Layout

```
Title + Subtitle
  ├─ [Export Excel] [Export PDF]
  └─ Filter form
        ├─ Date preset + Custom range
        ├─ Transaction ID
        ├─ Transaction Type (dropdown from DISTINCT data)
        ├─ Payment Method
        ├─ Amount range
        ├─ Branch (hidden if StoreManager)
        ├─ Employee
        ├─ Keyword
        └─ Sort controls
  └─ KPI Cards (6)
  └─ Transaction Table
        └─ Columns: #, Code, Date, Type, Method, Amount, Description, Branch, Employee, Status
  └─ Pagination
```

## Permission

- Owner: all branches
- StoreManager: own branch only (enforced in controller, same as OrderReportController)
- Admin: all branches
- SecurityFilter already allows `admin/owner/storemanager` for `/reports/`

## Files Changed

### New files:
1. `src/main/java/model/SalesTransactionFilter.java`
2. `src/main/java/model/SalesTransactionKpi.java`
3. `src/main/java/model/SalesTransaction.java`
4. `src/main/java/dao/report/SalesTransactionReportDAO.java`
5. `src/main/java/controller/report/SalesTransactionReportController.java`
6. `src/main/webapp/views/reports/sales-transaction-report.jsp`
7. `src/main/webapp/views/reports/_transaction_filter.jsp`
8. `src/main/webapp/views/reports/_transaction_kpi.jsp`
9. `src/main/webapp/views/reports/_transaction_table.jsp`

### Modified files:
1. `src/main/java/service/finance/PaymentService.java` — add passthrough methods
2. `src/main/java/controller/report/ReportController.java` — remove finance-detail redirect
3. `src/main/webapp/views/common/sidebar.jsp` — add report link
4. `src/main/java/util/report/ExcelExportUtil.java` — add export method
5. `src/main/java/util/report/PdfReportUtil.java` — add export method

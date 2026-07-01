# LOGIN FAILED ATTEMPTS AND SUPPLIER MANAGEMENT IMPROVEMENTS PLAN

## Scope
1. Account lockout flow on 5 failed password attempts.
2. Supplier management list page enhancements:
   - Display active and inactive supplier counts.
   - Sort supplier IDs in ascending order (1, 2, 3, 4...).
   - Use an inline Bootstrap modal for adding and editing suppliers on the same page instead of redirecting.

## Affected Modules
- `service.employee.AuthService`
- `dao.user.UserManagementDao`
- `service.supplier.SupplierService`
- `dao.supplier.SupplierDAO`
- `controller.supplier.SupplierServlet`
- `web/views/suppliers/list.jsp`

## Implementation Steps
1. Modify `AuthService.login` to increment failed attempts on incorrect password and lockout if count reaches 5. Reset to 0 on success.
2. Modify `UserManagementDao.updateEmployee` and `UserManagementDao.updateEmployeeStatus` to reset failed login attempts to 0 when status is updated to `'ACTIVE'`.
3. Add `countActiveSuppliers` and `countInactiveSuppliers` to `SupplierService`.
4. Modify `SupplierDAO.getSuppliersPaging` to sort by `SupplierID ASC`.
5. Modify `SupplierServlet.listSupplier` to fetch active/inactive counts and set request attributes.
6. Modify `list.jsp` to display statistic cards correctly, add Bootstrap modal, change buttons to open modal, and handle submission/retrieval via Javascript.
7. Run a compile smoke test.

## Validation Strategy
- Verify project compilation via local Java build.
- Manually test account lockout and reactivation.
- Manually check supplier counts, sorting, and inline addition/modification.

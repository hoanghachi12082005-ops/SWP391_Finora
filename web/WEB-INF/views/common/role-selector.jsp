<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<form id="shared-role-selector" class="role-selector" method="post" action="${pageContext.request.contextPath}/role-selection">
    <input type="hidden" name="returnUrl" value="${pageContext.request.contextPath}/dashboard">
    <label for="current-role-select">Current Role</label>
    <select id="current-role-select" name="role" onchange="this.form.submit()">
        <option value="Admin" ${currentRole == 'Admin' ? 'selected' : ''}>Admin</option>
        <option value="Owner" ${currentRole == 'Owner' ? 'selected' : ''}>Owner</option>
        <option value="Store Manager" ${currentRole == 'Store Manager' ? 'selected' : ''}>Store Manager</option>
        <option value="Sales Staff" ${currentRole == 'Sales Staff' ? 'selected' : ''}>Sales Staff</option>
        <option value="Warehouse Staff" ${currentRole == 'Warehouse Staff' ? 'selected' : ''}>Warehouse Staff</option>
        <option value="Guest" ${currentRole == 'Guest' ? 'selected' : ''}>Guest</option>
    </select>
</form>

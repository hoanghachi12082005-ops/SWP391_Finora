<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.ActivityLog, java.util.List, java.time.format.DateTimeFormatter" %>
<%
    List<ActivityLog> logs = (List<ActivityLog>) request.getAttribute("logs");
    List<String> tables    = (List<String>) request.getAttribute("tables");
    List<String> actions   = (List<String>) request.getAttribute("actions");
    int currentPage        = (Integer) request.getAttribute("currentPage");
    int totalPages         = (Integer) request.getAttribute("totalPages");
    Integer totalCount     = (Integer) request.getAttribute("totalCount");
    String keyword         = (String) request.getAttribute("keyword");
    String filterTable     = (String) request.getAttribute("filterTable");
    String filterAction    = (String) request.getAttribute("filterAction");
    String ctx             = request.getContextPath();
    DateTimeFormatter df   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
%>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Activity Center"/>
</jsp:include>
<div class="app-container">
    <jsp:include page="../common/sidebar.jsp"/>
    <main class="main-content">
        <jsp:include page="../common/topbar.jsp"/>
        <div class="container-fluid py-4">

<%-- Flash message --%>
<%
    String flashMsg  = (String) session.getAttribute("message");
    String flashType = (String) session.getAttribute("messageType");
    if (flashMsg != null) {
        session.removeAttribute("message");
        session.removeAttribute("messageType");
%>
            <div class="alert alert-<%= flashType != null ? flashType : "info" %> alert-dismissible fade show" role="alert">
                <%= flashMsg %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
<%  } %>

            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold mb-1">Activity Center</h2>
                    <small class="text-muted">Nhật ký hoạt động toàn hệ thống (tổng: <strong><%= totalCount != null ? totalCount : 0 %></strong> bản ghi)</small>
                </div>
                <div>
                    <button class="btn btn-danger" onclick="openLogModal('add')">
                        <span class="material-icons" style="vertical-align:middle;font-size:18px;">add</span>
                        Thêm hoạt động
                    </button>
                </div>
            </div>

            <!-- Filter -->
            <div class="card shadow-sm border-0 mb-3">
                <div class="card-body">
                    <form method="get" action="<%= ctx %>/activity-log" class="row g-2 align-items-end">
                        <div class="col-md-5">
                            <label class="form-label small text-muted mb-1">Từ khóa</label>
                            <input type="text" name="keyword" class="form-control"
                                   placeholder="Tìm theo hành động, bảng, nhân viên, dữ liệu mới..."
                                   value="<%= keyword != null ? keyword : "" %>">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label small text-muted mb-1">Bảng dữ liệu</label>
                            <select name="tableName" class="form-select">
                                <option value="">Tất cả</option>
                                <% if (tables != null) for (String t : tables) { %>
                                    <option value="<%= t %>" <%= t.equals(filterTable) ? "selected" : "" %>><%= t %></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label small text-muted mb-1">Hành động</label>
                            <select name="actionName" class="form-select">
                                <option value="">Tất cả</option>
                                <% if (actions != null) for (String a : actions) { %>
                                    <option value="<%= a %>" <%= a.equals(filterAction) ? "selected" : "" %>><%= a %></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="col-md-2 d-flex gap-2">
                            <button type="submit" class="btn btn-danger flex-grow-1">Lọc</button>
                            <a href="<%= ctx %>/activity-log" class="btn btn-outline-secondary">Xóa</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Table -->
            <div class="card shadow-sm border-0">
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table align-middle table-hover">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Thời gian</th>
                                    <th>Nhân viên</th>
                                    <th>Hành động</th>
                                    <th>Bảng</th>
                                    <th>Record ID</th>
                                    <th>Dữ liệu mới</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
<%
        if (logs == null || logs.isEmpty()) {
%>
                                <tr>
                                    <td colspan="8" class="text-center text-muted py-4">Chưa có hoạt động nào.</td>
                                </tr>
<%
        } else {
            for (ActivityLog log : logs) {
                String createdAt = log.getCreatedAt() != null ? df.format(log.getCreatedAt()) : "";
                String newDataDisplay = log.getNewData() != null
                        ? (log.getNewData().length() > 60 ? log.getNewData().substring(0, 60) + "..." : log.getNewData())
                        : "";
                String empName = log.getEmpName() != null ? log.getEmpName() : (log.getEmpId() > 0 ? "#" + log.getEmpId() : "(Hệ thống)");
                String actionBadge;
                if (log.getActionName() != null) {
                    String a = log.getActionName().toUpperCase();
                    if (a.contains("INSERT") || a.contains("CREATE") || a.contains("ADD")) actionBadge = "bg-success";
                    else if (a.contains("UPDATE") || a.contains("EDIT")) actionBadge = "bg-warning text-dark";
                    else if (a.contains("DELETE") || a.contains("REMOVE")) actionBadge = "bg-danger";
                    else actionBadge = "bg-secondary";
                } else actionBadge = "bg-secondary";
                String safeOld = log.getOldData() != null ? log.getOldData().replace("\"", "&quot;").replace("\n", " ") : "";
                String safeNew = log.getNewData() != null ? log.getNewData().replace("\"", "&quot;").replace("\n", " ") : "";
                String safeAction = log.getActionName() != null ? log.getActionName().replace("\"", "&quot;") : "";
                String safeTable = log.getTableName() != null ? log.getTableName().replace("\"", "&quot;") : "";
%>
                                <tr>
                                    <td>#<%= log.getId() %></td>
                                    <td><small><%= createdAt %></small></td>
                                    <td><%= empName %></td>
                                    <td><span class="badge <%= actionBadge %>"><%= log.getActionName() != null ? log.getActionName() : "" %></span></td>
                                    <td><code><%= log.getTableName() != null ? log.getTableName() : "" %></code></td>
                                    <td><%= log.getRecordId() != null ? log.getRecordId() : "" %></td>
                                    <td><small class="text-muted"><%= newDataDisplay %></small></td>
                                    <td class="text-end">
                                        <button class="btn btn-sm btn-outline-primary"
                                                onclick="viewLog('<%= log.getId() %>','<%= createdAt %>','<%= empName.replace("'", "\\'") %>','<%= safeAction.replace("'", "\\'") %>','<%= safeTable.replace("'", "\\'") %>','<%= log.getRecordId() != null ? log.getRecordId() : "" %>','<%= safeOld.replace("'", "\\'") %>','<%= safeNew.replace("'", "\\'") %>')">
                                            Xem
                                        </button>
                                        <button class="btn btn-sm btn-warning"
                                                onclick="openLogModal('edit','<%= log.getId() %>','<%= log.getEmpId() %>','<%= safeAction.replace("'", "\\'") %>','<%= safeTable.replace("'", "\\'") %>','<%= log.getRecordId() != null ? log.getRecordId() : "" %>','<%= safeOld.replace("'", "\\'") %>','<%= safeNew.replace("'", "\\'") %>')">
                                            Sửa
                                        </button>
                                        <button class="btn btn-sm btn-danger" onclick="deleteLog('<%= log.getId() %>')">Xóa</button>
                                    </td>
                                </tr>
<%
            }
        }
%>
                            </tbody>
                        </table>
                    </div>

<% if (totalPages > 1) {
        String baseUrl = ctx + "/activity-log?"
                + (keyword != null && !keyword.isBlank() ? "keyword=" + keyword + "&" : "")
                + (filterTable != null && !filterTable.isBlank() ? "tableName=" + filterTable + "&" : "")
                + (filterAction != null && !filterAction.isBlank() ? "actionName=" + filterAction + "&" : "");
%>
                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <div class="text-muted small">Trang <strong><%= currentPage %></strong> / <strong><%= totalPages %></strong></div>
                        <ul class="pagination mb-0">
                            <li class="page-item <%= currentPage <= 1 ? "disabled" : "" %>">
                                <a class="page-link" href="<%= baseUrl %>page=<%= currentPage - 1 %>">Trước</a>
                            </li>
<%      for (int i = 1; i <= totalPages; i++) { %>
                            <li class="page-item <%= i == currentPage ? "active" : "" %>">
                                <a class="page-link" href="<%= baseUrl %>page=<%= i %>"><%= i %></a>
                            </li>
<%      } %>
                            <li class="page-item <%= currentPage >= totalPages ? "disabled" : "" %>">
                                <a class="page-link" href="<%= baseUrl %>page=<%= currentPage + 1 %>">Tiếp</a>
                            </li>
                        </ul>
                    </div>
<% } %>
                </div>
            </div>

        </div>
    </main>
</div>

<!-- Add/Edit Modal -->
<div class="modal fade" id="logModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form method="post" action="<%= ctx %>/activity-log">
                <div class="modal-header">
                    <h5 class="modal-title" id="logModalTitle">Thêm hoạt động</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="action" id="logAction" value="add">
                    <input type="hidden" name="id" id="logId">
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label">Nhân viên (emp_id)</label>
                            <input type="number" min="0" class="form-control" name="empId" id="logEmpId" placeholder="VD: 1">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Hành động <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="actionName" id="logActionName" required placeholder="INSERT / UPDATE / DELETE / LOGIN ...">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Bảng dữ liệu</label>
                            <input type="text" class="form-control" name="tableName" id="logTableName" placeholder="product, order, ...">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Record ID</label>
                            <input type="number" min="0" class="form-control" name="recordId" id="logRecordId">
                        </div>
                        <div class="col-12">
                            <label class="form-label">Dữ liệu cũ (old_data)</label>
                            <textarea class="form-control" rows="3" name="oldData" id="logOldData" placeholder="JSON hoặc mô tả..."></textarea>
                        </div>
                        <div class="col-12">
                            <label class="form-label">Dữ liệu mới (new_data)</label>
                            <textarea class="form-control" rows="3" name="newData" id="logNewData" placeholder="JSON hoặc mô tả..."></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger">Lưu</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- View Modal -->
<div class="modal fade" id="viewModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Chi tiết hoạt động <span id="vId"></span></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <dl class="row mb-0">
                    <dt class="col-sm-3">Thời gian</dt><dd class="col-sm-9" id="vTime"></dd>
                    <dt class="col-sm-3">Nhân viên</dt><dd class="col-sm-9" id="vEmp"></dd>
                    <dt class="col-sm-3">Hành động</dt><dd class="col-sm-9" id="vAction"></dd>
                    <dt class="col-sm-3">Bảng</dt><dd class="col-sm-9" id="vTable"></dd>
                    <dt class="col-sm-3">Record ID</dt><dd class="col-sm-9" id="vRecord"></dd>
                    <dt class="col-sm-3">Dữ liệu cũ</dt>
                    <dd class="col-sm-9"><pre class="bg-light p-2 rounded small mb-0" id="vOld"></pre></dd>
                    <dt class="col-sm-3">Dữ liệu mới</dt>
                    <dd class="col-sm-9"><pre class="bg-light p-2 rounded small mb-0" id="vNew"></pre></dd>
                </dl>
            </div>
        </div>
    </div>
</div>

<!-- Delete hidden form -->
<form id="deleteForm" method="post" action="<%= ctx %>/activity-log" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="id" id="deleteId">
</form>

<script>
function openLogModal(mode, id, empId, actionName, tableName, recordId, oldData, newData) {
    document.getElementById('logAction').value = mode;
    document.getElementById('logModalTitle').innerText = (mode === 'edit') ? 'Cập nhật hoạt động' : 'Thêm hoạt động';
    document.getElementById('logId').value = id || '';
    document.getElementById('logEmpId').value = empId || '';
    document.getElementById('logActionName').value = actionName || '';
    document.getElementById('logTableName').value = tableName || '';
    document.getElementById('logRecordId').value = recordId || '';
    document.getElementById('logOldData').value = oldData || '';
    document.getElementById('logNewData').value = newData || '';
    new bootstrap.Modal(document.getElementById('logModal')).show();
}

function viewLog(id, time, emp, actionName, tableName, recordId, oldData, newData) {
    document.getElementById('vId').innerText = '#' + id;
    document.getElementById('vTime').innerText = time;
    document.getElementById('vEmp').innerText = emp;
    document.getElementById('vAction').innerText = actionName;
    document.getElementById('vTable').innerText = tableName;
    document.getElementById('vRecord').innerText = recordId;
    document.getElementById('vOld').innerText = oldData || '(trống)';
    document.getElementById('vNew').innerText = newData || '(trống)';
    new bootstrap.Modal(document.getElementById('viewModal')).show();
}

function deleteLog(id) {
    if (confirm('Bạn chắc chắn muốn xóa hoạt động #' + id + ' khỏi nhật ký?')) {
        document.getElementById('deleteId').value = id;
        document.getElementById('deleteForm').submit();
    }
}
</script>

<jsp:include page="../common/footer.jsp"/>

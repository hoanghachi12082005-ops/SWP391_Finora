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

            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold mb-1">Activity Center</h2>
                    <small class="text-muted">
                        Nhật ký hoạt động hệ thống (chỉ đọc) - Tổng:
                        <strong><%= totalCount != null ? totalCount : 0 %></strong> bản ghi
                    </small>
                </div>
                <span class="badge bg-secondary d-flex align-items-center gap-1" style="padding: 8px 12px;">
                    <span class="material-icons" style="font-size:16px;">lock</span>
                    Read-only - Chỉ Owner
                </span>
            </div>

            <div class="alert alert-info d-flex align-items-center" role="alert">
                <span class="material-icons me-2">info</span>
                <div>
                    Đây là <strong>audit log</strong> bất biến. Mọi hành động trong hệ thống được tự động ghi lại
                    và <strong>không thể chỉnh sửa hay xóa</strong> từ giao diện để bảo toàn dấu vết kiểm toán.
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
                                    <th class="text-end">Chi tiết</th>
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
                String empName = log.getActorLabel();
                String actionBadge;
                if (log.getActionName() != null) {
                    String a = log.getActionName().toUpperCase();
                    if (a.contains("INSERT") || a.contains("CREATE") || a.contains("ADD")) actionBadge = "bg-success";
                    else if (a.contains("UPDATE") || a.contains("EDIT")) actionBadge = "bg-warning text-dark";
                    else if (a.contains("DELETE") || a.contains("REMOVE")) actionBadge = "bg-danger";
                    else actionBadge = "bg-secondary";
                } else actionBadge = "bg-secondary";
                String safeOld = log.getOldData() != null ? log.getOldData().replace("\"", "&quot;").replace("\n", " ").replace("\r", " ") : "";
                String safeNew = log.getNewData() != null ? log.getNewData().replace("\"", "&quot;").replace("\n", " ").replace("\r", " ") : "";
                String safeAction = log.getActionName() != null ? log.getActionName().replace("\"", "&quot;") : "";
                String safeTable = log.getTableName() != null ? log.getTableName().replace("\"", "&quot;") : "";
                String safeEmp = empName.replace("\"", "&quot;");
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
                                                onclick="viewLog('<%= log.getId() %>','<%= createdAt %>','<%= safeEmp.replace("'", "\\'") %>','<%= safeAction.replace("'", "\\'") %>','<%= safeTable.replace("'", "\\'") %>','<%= log.getRecordId() != null ? log.getRecordId() : "" %>','<%= safeOld.replace("'", "\\'") %>','<%= safeNew.replace("'", "\\'") %>')">
                                            Xem
                                        </button>
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

<!-- View Modal (chỉ đọc) -->
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
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            </div>
        </div>
    </div>
</div>

<script>
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
</script>

<jsp:include page="../common/footer.jsp"/>

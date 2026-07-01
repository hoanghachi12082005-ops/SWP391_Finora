<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.ActivityLog, java.util.List, java.util.Map" %>
<%
    List<ActivityLog> logs = (List<ActivityLog>) request.getAttribute("logs");
    Map<String,String> entityOptions = (Map<String,String>) request.getAttribute("entityOptions");
    Map<String,String> actionOptions = (Map<String,String>) request.getAttribute("actionOptions");
    int currentPage    = (Integer) request.getAttribute("currentPage");
    int totalPages     = (Integer) request.getAttribute("totalPages");
    Integer totalCount = (Integer) request.getAttribute("totalCount");
    String keyword     = (String) request.getAttribute("keyword");
    String filterTable = (String) request.getAttribute("filterTable");
    String filterAction= (String) request.getAttribute("filterAction");
    String filterDateFrom = (String) request.getAttribute("filterDateFrom");
    String filterDateTo   = (String) request.getAttribute("filterDateTo");
    String ctx         = request.getContextPath();
%>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Trung tâm hoạt động"/>
</jsp:include>
<div class="app-container">
    <jsp:include page="../common/sidebar.jsp"/>
    <main class="main-content">
        <jsp:include page="../common/topbar.jsp"/>
        <div class="container-fluid py-4">

            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold mb-1">Trung tâm hoạt động</h2>
                    <small class="text-muted">
                        Theo dõi toàn bộ thao tác bán hàng &amp; quản trị trên hệ thống &middot;
                        Tổng <strong><%= totalCount != null ? totalCount : 0 %></strong> hoạt động
                    </small>
                </div>
            </div>

            <!-- Bộ lọc nghiệp vụ -->
            <div class="card shadow-sm border-0 mb-3">
                <div class="card-body">
                    <form method="get" action="<%= ctx %>/activity-log" class="row g-2 align-items-end">
                        <div class="col-md-4">
                            <label class="form-label small text-muted mb-1">Tìm kiếm</label>
                            <input type="text" name="keyword" class="form-control"
                                   placeholder="Tìm theo tên nhân viên, đối tượng, nội dung..."
                                   value="<%= keyword != null ? keyword : "" %>">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label small text-muted mb-1">Đối tượng</label>
                            <select name="tableName" class="form-select">
                                <option value="">Tất cả đối tượng</option>
<%                              if (entityOptions != null) {
                                    for (Map.Entry<String,String> e : entityOptions.entrySet()) {
                                        boolean sel = e.getKey().equals(filterTable);
%>
                                    <option value="<%= e.getKey() %>" <%= sel ? "selected" : "" %>><%= e.getValue() %></option>
<%                                  }
                                }
%>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label small text-muted mb-1">Thao tác</label>
                            <select name="actionName" class="form-select">
                                <option value="">Tất cả thao tác</option>
<%                              if (actionOptions != null) {
                                    for (Map.Entry<String,String> e : actionOptions.entrySet()) {
                                        boolean sel = e.getKey().equals(filterAction);
%>
                                    <option value="<%= e.getKey() %>" <%= sel ? "selected" : "" %>><%= e.getValue() %></option>
<%                                  }
                                }
%>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label small text-muted mb-1">Từ ngày</label>
                            <input type="date" name="dateFrom" class="form-control"
                                   value="<%= filterDateFrom != null ? filterDateFrom : "" %>">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label small text-muted mb-1">Đến ngày</label>
                            <input type="date" name="dateTo" class="form-control"
                                   value="<%= filterDateTo != null ? filterDateTo : "" %>">
                        </div>
                        <div class="col-md-12 d-flex gap-2 justify-content-end">
                            <button type="submit" class="btn btn-danger">
                                <span class="material-icons align-middle me-1" style="font-size:16px;">filter_alt</span>
                                Lọc
                            </button>
                            <a href="<%= ctx %>/activity-log" class="btn btn-outline-secondary">Xóa bộ lọc</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Bảng hoạt động -->
            <div class="card shadow-sm border-0">
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table align-middle table-hover">
                            <thead>
                                <tr>
                                    <th style="width: 160px;">Thời gian</th>
                                    <th>Nhân viên thực hiện</th>
                                    <th>Hoạt động</th>
                                    <th>Đối tượng</th>
                                    <th>Mã</th>
                                    <th class="text-end">Chi tiết</th>
                                </tr>
                            </thead>
                            <tbody>
<%
        if (logs == null || logs.isEmpty()) {
%>
                                <tr>
                                    <td colspan="6" class="text-center text-muted py-4">Chưa có hoạt động nào.</td>
                                </tr>
<%
        } else {
            for (ActivityLog log : logs) {
                String when      = log.getCreatedAtFormatted();
                String actor     = log.getActorLabel();
                String actLabel  = log.getActionLabel();
                String entity    = log.getEntityLabel();
                String code      = log.getEntityCode();
                String desc      = log.getDescription();
                String iconColor = log.getIconColor();
                String iconName  = log.getIconName();
                String badge;
                switch (iconColor) {
                    case "green":  badge = "bg-success"; break;
                    case "orange": badge = "bg-warning text-dark"; break;
                    case "red":    badge = "bg-danger"; break;
                    default:       badge = "bg-primary"; break;
                }
                String safeDesc   = desc != null ? desc.replace("'", "\\'") : "";
                String safeActor  = actor != null ? actor.replace("'", "\\'") : "";
                String safeAction = actLabel != null ? actLabel.replace("'", "\\'") : "";
                String safeEntity = entity != null ? entity.replace("'", "\\'") : "";
                String safeCode   = code != null ? code.replace("'", "\\'") : "";
                String safeOld    = log.getOldData() != null ? log.getOldData().replace("'", "\\'").replace("\n"," ").replace("\r"," ") : "";
                String safeNew    = log.getNewData() != null ? log.getNewData().replace("'", "\\'").replace("\n"," ").replace("\r"," ") : "";
%>
                                <tr>
                                    <td><small class="text-muted"><%= when %></small></td>
                                    <td><strong><%= actor %></strong></td>
                                    <td>
                                        <span class="d-inline-flex align-items-center gap-2">
                                            <span class="badge <%= badge %> d-inline-flex align-items-center gap-1">
                                                <span class="material-icons" style="font-size:14px;"><%= iconName %></span>
                                                <%= actLabel %>
                                            </span>
                                            <span><%= desc %></span>
                                        </span>
                                    </td>
                                    <td><%= entity %></td>
                                    <td><code><%= (code != null && !code.isEmpty()) ? code : "—" %></code></td>
                                    <td class="text-end">
                                        <button class="btn btn-sm btn-outline-primary"
                                                onclick="viewLog('<%= when %>','<%= safeActor %>','<%= safeAction %>','<%= safeEntity %>','<%= safeCode %>','<%= safeDesc %>','<%= safeOld %>','<%= safeNew %>')">
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
                + (filterAction != null && !filterAction.isBlank() ? "actionName=" + filterAction + "&" : "")
                + (filterDateFrom != null && !filterDateFrom.isBlank() ? "dateFrom=" + filterDateFrom + "&" : "")
                + (filterDateTo != null && !filterDateTo.isBlank() ? "dateTo=" + filterDateTo + "&" : "");
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

<!-- Modal chi tiết hoạt động (chỉ đọc) -->
<div class="modal fade" id="viewModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Chi tiết hoạt động</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p class="mb-3" id="vSummary" style="font-size: 15px;"></p>
                <dl class="row mb-0">
                    <dt class="col-sm-3">Thời gian</dt><dd class="col-sm-9" id="vTime"></dd>
                    <dt class="col-sm-3">Nhân viên</dt><dd class="col-sm-9" id="vActor"></dd>
                    <dt class="col-sm-3">Thao tác</dt><dd class="col-sm-9" id="vAction"></dd>
                    <dt class="col-sm-3">Đối tượng</dt><dd class="col-sm-9" id="vEntity"></dd>
                    <dt class="col-sm-3">Mã đối tượng</dt><dd class="col-sm-9" id="vCode"></dd>
                    <dt class="col-sm-3">Dữ liệu trước</dt>
                    <dd class="col-sm-9"><pre class="bg-light p-2 rounded small mb-0" id="vOld" style="max-height: 200px; overflow:auto;"></pre></dd>
                    <dt class="col-sm-3">Dữ liệu sau</dt>
                    <dd class="col-sm-9"><pre class="bg-light p-2 rounded small mb-0" id="vNew" style="max-height: 200px; overflow:auto;"></pre></dd>
                </dl>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            </div>
        </div>
    </div>
</div>

<script>
function viewLog(time, actor, action, entity, code, summary, oldData, newData) {
    document.getElementById('vSummary').innerText = summary;
    document.getElementById('vTime').innerText = time;
    document.getElementById('vActor').innerText = actor;
    document.getElementById('vAction').innerText = action;
    document.getElementById('vEntity').innerText = entity;
    document.getElementById('vCode').innerText = code || '—';
    document.getElementById('vOld').innerText = oldData || '(trống)';
    document.getElementById('vNew').innerText = newData || '(trống)';
    new bootstrap.Modal(document.getElementById('viewModal')).show();
}
</script>

<jsp:include page="../common/footer.jsp"/>

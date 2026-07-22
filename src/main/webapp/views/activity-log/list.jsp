<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.ActivityLog, java.util.List, java.util.Map" %>
<%
    List<ActivityLog> logs = (List<ActivityLog>) request.getAttribute("logs");
    Map<String,String> entityOptions = (Map<String,String>) request.getAttribute("entityOptions");
    Map<String,String> actionOptions = (Map<String,String>) request.getAttribute("actionOptions");
    boolean hasNext   = Boolean.TRUE.equals(request.getAttribute("hasNext"));
    boolean hasPrev   = Boolean.TRUE.equals(request.getAttribute("hasPrev"));
    int firstId       = request.getAttribute("firstId") != null ? (Integer) request.getAttribute("firstId") : 0;
    int lastId        = request.getAttribute("lastId") != null ? (Integer) request.getAttribute("lastId") : 0;
    Integer totalCount= (Integer) request.getAttribute("totalCount");
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
                String branch    = log.getBranchLabel();
                String empIdStr  = log.getEmpId() > 0 ? "NV #" + log.getEmpId() : "";
                String badge;
                switch (iconColor) {
                    case "green":  badge = "bg-success"; break;
                    case "orange": badge = "bg-warning text-dark"; break;
                    case "red":    badge = "bg-danger"; break;
                    default:       badge = "bg-primary"; break;
                }
                String safeDesc   = desc != null ? desc.replace("'", "\\'") : "";
                String safeActor  = actor != null ? actor.replace("'", "\\'").replace("\"", "&quot;") : "";
                String safeAction = actLabel != null ? actLabel.replace("'", "\\'").replace("\"", "&quot;") : "";
                String safeEntity = entity != null ? entity.replace("'", "\\'").replace("\"", "&quot;") : "";
                String safeCode   = code != null ? code.replace("'", "\\'").replace("\"", "&quot;") : "";
                String safeOld    = log.getOldData() != null ? log.getOldData().replace("\"", "&quot;").replace("\n"," ").replace("\r"," ") : "";
                String safeNew    = log.getNewData() != null ? log.getNewData().replace("\"", "&quot;").replace("\n"," ").replace("\r"," ") : "";
%>
                                <tr>
                                    <td><small class="text-muted"><%= when %></small></td>
                                    <td>
                                        <strong><%= actor %></strong>
                                        <% if (!empIdStr.isEmpty()) { %><br><small class="text-muted"><%= empIdStr %></small><% } %>
                                    </td>
                                    <td><span class="badge bg-secondary"><%= branch %></span></td>
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
                                        <button class="btn btn-sm btn-outline-primary view-log-btn"
                                                data-time="<%= when %>"
                                                data-actor="<%= safeActor %>"
                                                data-action="<%= safeAction %>"
                                                data-entity="<%= safeEntity %>"
                                                data-code="<%= safeCode %>"
                                                data-summary="<%= safeDesc %>"
                                                data-old="<%= safeOld %>"
                                                data-new="<%= safeNew %>"
                                                data-branch="<%= branch %>"
                                                data-empid="<%= empIdStr %>">
                                            Xem nội dung
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

<% if (totalCount > 0) {
        String baseUrl = ctx + "/activity-log?"
                + (keyword != null && !keyword.isBlank() ? "keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8") + "&" : "")
                + (filterTable != null && !filterTable.isBlank() ? "tableName=" + filterTable + "&" : "")
                + (filterAction != null && !filterAction.isBlank() ? "actionName=" + filterAction + "&" : "")
                + (filterDateFrom != null && !filterDateFrom.isBlank() ? "dateFrom=" + filterDateFrom + "&" : "")
                + (filterDateTo != null && !filterDateTo.isBlank() ? "dateTo=" + filterDateTo + "&" : "");
%>
                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <div class="text-muted small">Tổng <strong><%= totalCount %></strong> hoạt động</div>
                        <ul class="pagination mb-0">
                            <li class="page-item <%= hasPrev ? "" : "disabled" %>">
                                <a class="page-link" href="<%= hasPrev ? baseUrl + "after=" + firstId : "#" %>">← Mới hơn</a>
                            </li>
                            <li class="page-item <%= hasNext ? "" : "disabled" %>">
                                <a class="page-link" href="<%= hasNext ? baseUrl + "before=" + lastId : "#" %>">Cũ hơn →</a>
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
                    <dt class="col-sm-3">Mã NV</dt><dd class="col-sm-9" id="vEmpId"></dd>
                    <dt class="col-sm-3">Chi nhánh</dt><dd class="col-sm-9" id="vBranch"></dd>
                    <dt class="col-sm-3">Thao tác</dt><dd class="col-sm-9" id="vAction"></dd>
                    <dt class="col-sm-3">Đối tượng</dt><dd class="col-sm-9" id="vEntity"></dd>
                    <dt class="col-sm-3">Mã đối tượng</dt><dd class="col-sm-9" id="vCode"></dd>
                    <dt class="col-sm-3">Dữ liệu trước</dt>
                    <dd class="col-sm-9"><div class="bg-light p-2 rounded small mb-0" id="vOld" style="max-height: 250px; overflow:auto;"></div></dd>
                    <dt class="col-sm-3">Dữ liệu sau</dt>
                    <dd class="col-sm-9"><div class="bg-light p-2 rounded small mb-0" id="vNew" style="max-height: 250px; overflow:auto;"></div></dd>
                </dl>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            </div>
        </div>
    </div>
</div>

<script>
function formatDataTable(raw) {
    if (!raw || raw === '(trống)') return '<span class="text-muted fst-italic">(trống)</span>';

    // Try JSON format: {"key":"value", "key2":"value2"}
    if (raw.trim().startsWith('{')) {
        try {
            var obj = JSON.parse(raw);
            var html = '<table class="table table-sm table-borderless mb-0" style="font-size:13px;">';
            for (var key in obj) {
                var val = obj[key] != null ? obj[key] : '';
                html += '<tr><td class="text-muted pe-3" style="width:1%;white-space:nowrap;vertical-align:top;">'
                      + key + '</td><td style="vertical-align:top;">' + val + '</td></tr>';
            }
            html += '</table>';
            return html;
        } catch (e) {
            // fallthrough to legacy parser
        }
    }

    // Legacy format: ClassName{key=value, key2='value2', key3=123.00}
    var match = raw.match(/\w+\{(.+)\}$/);
    var body = match ? match[1] : raw.trim();

    // Split by comma, but respect single-quoted strings
    var pairs = [];
    var current = '';
    var inQuote = false;
    for (var i = 0; i < body.length; i++) {
        var ch = body[i];
        if (ch === "'") inQuote = !inQuote;
        if (ch === ',' && !inQuote) { pairs.push(current); current = ''; continue; }
        current += ch;
    }
    if (current.trim()) pairs.push(current);

    var html = '<table class="table table-sm table-borderless mb-0" style="font-size:13px;">';
    pairs.forEach(function(p) {
        var eqIdx = p.indexOf('=');
        if (eqIdx < 0) return;
        var key = p.substring(0, eqIdx).trim();
        var val = p.substring(eqIdx + 1).trim();
        if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith('"') && val.endsWith('"'))) {
            val = val.substring(1, val.length - 1);
        }
        html += '<tr><td class="text-muted pe-3" style="width:1%;white-space:nowrap;vertical-align:top;">'
              + key + '</td><td style="vertical-align:top;">' + val + '</td></tr>';
    });
    html += '</table>';
    return html;
}

// Click handler cho buttons xem log (dung data-* attributes thay vi onclick)
document.addEventListener('click', function(e) {
    var btn = e.target.closest('.view-log-btn');
    if (!btn) return;

    document.getElementById('vSummary').innerText = btn.dataset.summary;
    document.getElementById('vTime').innerText = btn.dataset.time;
    document.getElementById('vActor').innerText = btn.dataset.actor;
    document.getElementById('vEmpId').innerText = btn.dataset.empid || '—';
    document.getElementById('vBranch').innerText = btn.dataset.branch || '—';
    document.getElementById('vAction').innerText = btn.dataset.action;
    document.getElementById('vEntity').innerText = btn.dataset.entity;
    document.getElementById('vCode').innerText = btn.dataset.code || '—';
    document.getElementById('vOld').innerHTML = formatDataTable(btn.dataset.old);
    document.getElementById('vNew').innerHTML = formatDataTable(btn.dataset.new);

    var modal = new bootstrap.Modal(document.getElementById('viewModal'));
    modal.show();
});
</script>

<jsp:include page="../common/footer.jsp"/>

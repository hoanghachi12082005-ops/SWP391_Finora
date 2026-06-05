<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dto.ModuleDTO" %>
<%@ page import="dto.ModuleActionDTO" %>
<%
    ModuleDTO module = (ModuleDTO) request.getAttribute("module");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= module.getName() %> | Finora</title>
    <meta name="description" content="Grouped module workspace for <%= module.getName() %> in the Finora project foundation.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="shell">
        <section class="skeleton-hero <%= module.isAllowed() ? "" : "locked" %>">
            <p class="eyebrow"><%= module.getOwnerSuggestion() %> workspace</p>
            <h1><%= module.getName() %></h1>
            <p><%= module.getDescription() %></p>
            <span class="access-badge"><%= module.getAllowedActionCount() %>/<%= module.getActions().size() %> actions accessible for current role</span>
        </section>

        <section class="detail-grid">
            <article class="detail-panel">
                <h2>Database Mapping</h2>
                <p><%= module.getDatabaseMapping() %></p>
            </article>
            <article class="detail-panel">
                <h2>Foundation Contract</h2>
                <ul>
                    <li>One module page owns related CRUD/actions.</li>
                    <li>Do not create separate dashboards or role selectors.</li>
                    <li>Controller routes, service orchestrates, DAO owns SQL.</li>
                    <li>Action visibility reads current role from the shared selector.</li>
                </ul>
            </article>
        </section>

        <section class="action-board" aria-label="Grouped module actions">
            <h2>Grouped RDS Actions / Screens</h2>
            <div class="action-grid">
                <% for (ModuleActionDTO action : module.getActions()) { %>
                <article class="action-card <%= action.isAllowed() ? "is-allowed" : "is-locked" %>">
                    <div class="card-topline">
                        <strong><%= action.getName() %></strong>
                        <span class="access-badge"><%= action.isAllowed() ? "Allowed" : "Locked" %></span>
                    </div>
                    <p><%= action.getDescription() %></p>
                    <dl>
                        <dt>Database</dt>
                        <dd><%= action.getDatabaseMapping() %></dd>
                        <dt>Todo</dt>
                        <dd><%= action.getTodo() %></dd>
                    </dl>
                </article>
                <% } %>
            </div>
        </section>
    </main>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>

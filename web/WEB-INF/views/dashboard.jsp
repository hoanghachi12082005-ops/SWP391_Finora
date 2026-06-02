<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="common.dto.ModuleDTO" %>
<%@ page import="common.dto.ModuleActionDTO" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Finora Development Dashboard</title>
    <meta name="description" content="Development dashboard listing RDS-defined Finora modules grouped by team workspace.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="shell">
        <section class="hero">
            <div>
                <p class="eyebrow">RDS-driven project foundation</p>
                <h1>Module Dashboard</h1>
                <p>Each card is one development workspace. CRUD screens/actions are grouped inside the module to avoid duplicate UI work.</p>
            </div>
            <div class="hero-card">
                <span>Current role</span>
                <strong><%= request.getAttribute("currentRole") %></strong>
                <small>Role source: shared development selector</small>
            </div>
        </section>

        <section class="card-grid" aria-label="RDS module list">
            <%
                List<ModuleDTO> modules = (List<ModuleDTO>) request.getAttribute("modules");
                for (ModuleDTO module : modules) {
            %>
            <article class="module-card <%= module.isAllowed() ? "is-allowed" : "is-locked" %>">
                <div class="card-topline">
                    <span class="module-name"><%= module.getOwnerSuggestion() %></span>
                    <span class="access-badge"><%= module.getAllowedActionCount() %>/<%= module.getActions().size() %> actions</span>
                </div>
                <h2><%= module.getName() %></h2>
                <p><%= module.getDescription() %></p>
                <dl>
                    <dt>Database</dt>
                    <dd><%= module.getDatabaseMapping() %></dd>
                    <dt>Grouped actions</dt>
                    <dd>
                        <%
                            int shown = 0;
                            for (ModuleActionDTO action : module.getActions()) {
                                if (shown++ > 0) { out.print(", "); }
                                out.print(action.getName());
                                if (shown == 5 && module.getActions().size() > 5) {
                                    out.print(", +" + (module.getActions().size() - 5) + " more");
                                    break;
                                }
                            }
                        %>
                    </dd>
                </dl>
                <a class="card-action" href="${pageContext.request.contextPath}<%= module.getRoute() %>">Open module</a>
            </article>
            <% } %>
        </section>
    </main>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>

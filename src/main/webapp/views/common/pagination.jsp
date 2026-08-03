<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="table-footer mt-3" style="width: 100%; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
    <div class="pagination-info d-flex align-items-center">
        <form method="get" action="${pageContext.request.contextPath}/inventory" class="d-flex align-items-center gap-2 m-0">
            <input type="hidden" name="tab" value="${not empty activeTab ? activeTab : (not empty param.tab ? param.tab : 'stock')}" />
            <c:if test="${not empty currentSubtab}">
                <input type="hidden" name="subtab" value="${currentSubtab}" />
            </c:if>
            <%
                String qs = (String) request.getAttribute("queryString");
                if (qs == null || qs.trim().isEmpty()) {
                    qs = request.getParameter("queryString");
                }
                if (qs != null) {
                    String s = qs.startsWith("&") ? qs.substring(1) : (qs.startsWith("?") ? qs.substring(1) : qs);
                    if (!s.isEmpty()) {
                        for (String pair : s.split("&")) {
                            int eq = pair.indexOf('=');
                            if (eq > 0) {
                                String name = pair.substring(0, eq);
                                String value = (eq + 1 < pair.length()) ? pair.substring(eq + 1) : "";
                                if (!"page".equals(name) && !"sizeValue".equals(name) && !"tab".equals(name) && !"subtab".equals(name)) {
                                    out.write("<input type=\"hidden\" name=\"");
                                    out.write(name.replace("\"", "&quot;"));
                                    out.write("\" value=\"");
                                    out.write(value.replace("\"", "&quot;"));
                                    out.write("\"/>");
                                }
                            }
                        }
                    }
                }
            %>
            <select name="sizeValue" class="form-select form-select-sm" style="width: auto; cursor: pointer; border-radius: 6px; font-size: 13px;" onchange="this.form.submit()">
                <option value="10" ${sizeValue == 10 || empty sizeValue ? 'selected' : ''}>10 / trang</option>
                <option value="30" ${sizeValue == 30 ? 'selected' : ''}>30 / trang</option>
                <option value="100" ${sizeValue == 100 ? 'selected' : ''}>100 / trang</option>
                <option value="100000" ${sizeValue >= 1000 ? 'selected' : ''}>Tất cả</option>
            </select>
            <span class="pagination-summary text-muted small ms-1">
                Hiển thị <strong>${startRecord}</strong> - <strong>${endRecord}</strong> trong số <strong>${totalRecords}</strong> bản ghi
            </span>
        </form>
    </div>

    <c:if test="${totalPages > 1}">
        <div class="pagination" style="display: flex; gap: 4px; align-items: center; margin: 0;">
            <c:choose>
                <c:when test="${currentPage > 1}">
                    <a href="${baseUrl}?page=${currentPage - 1}&sizeValue=${sizeValue}${queryString}" class="btn btn-sm btn-outline-secondary" style="border-radius: 6px; font-size: 13px; padding: 3px 10px;">«</a>
                </c:when>
                <c:otherwise>
                    <span class="btn btn-sm btn-outline-secondary disabled" style="border-radius: 6px; font-size: 13px; padding: 3px 10px; opacity: 0.5;">«</span>
                </c:otherwise>
            </c:choose>

            <c:choose>
                <c:when test="${totalPages <= 5}">
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <a href="${baseUrl}?page=${i}&sizeValue=${sizeValue}${queryString}"
                           class="btn btn-sm ${i == currentPage ? 'btn-danger text-white' : 'btn-outline-secondary'}"
                           style="border-radius: 6px; font-size: 13px; padding: 3px 10px; ${i == currentPage ? 'background-color: var(--primary-color, #800000); border-color: var(--primary-color, #800000);' : ''}">${i}</a>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <a href="${baseUrl}?page=1&sizeValue=${sizeValue}${queryString}"
                       class="btn btn-sm ${currentPage == 1 ? 'btn-danger text-white' : 'btn-outline-secondary'}"
                       style="border-radius: 6px; font-size: 13px; padding: 3px 10px; ${currentPage == 1 ? 'background-color: var(--primary-color, #800000); border-color: var(--primary-color, #800000);' : ''}">1</a>
                    <c:if test="${currentPage > 3}"><span class="px-1 text-muted">...</span></c:if>
                    <c:forEach begin="${currentPage - 1 < 2 ? 2 : currentPage - 1}"
                               end="${currentPage + 1 > totalPages - 1 ? totalPages - 1 : currentPage + 1}" var="i">
                        <a href="${baseUrl}?page=${i}&sizeValue=${sizeValue}${queryString}"
                           class="btn btn-sm ${i == currentPage ? 'btn-danger text-white' : 'btn-outline-secondary'}"
                           style="border-radius: 6px; font-size: 13px; padding: 3px 10px; ${i == currentPage ? 'background-color: var(--primary-color, #800000); border-color: var(--primary-color, #800000);' : ''}">${i}</a>
                    </c:forEach>
                    <c:if test="${currentPage < totalPages - 2}"><span class="px-1 text-muted">...</span></c:if>
                    <a href="${baseUrl}?page=${totalPages}&sizeValue=${sizeValue}${queryString}"
                       class="btn btn-sm ${currentPage == totalPages ? 'btn-danger text-white' : 'btn-outline-secondary'}"
                       style="border-radius: 6px; font-size: 13px; padding: 3px 10px; ${currentPage == totalPages ? 'background-color: var(--primary-color, #800000); border-color: var(--primary-color, #800000);' : ''}">${totalPages}</a>
                </c:otherwise>
            </c:choose>

            <c:choose>
                <c:when test="${currentPage < totalPages}">
                    <a href="${baseUrl}?page=${currentPage + 1}&sizeValue=${sizeValue}${queryString}" class="btn btn-sm btn-outline-secondary" style="border-radius: 6px; font-size: 13px; padding: 3px 10px;">»</a>
                </c:when>
                <c:otherwise>
                    <span class="btn btn-sm btn-outline-secondary disabled" style="border-radius: 6px; font-size: 13px; padding: 3px 10px; opacity: 0.5;">»</span>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>

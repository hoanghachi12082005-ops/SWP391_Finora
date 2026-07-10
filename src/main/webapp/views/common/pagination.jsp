<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
  Shared pagination links component.
  Requires these request attributes (set by PaginationHelper.setAttributes):
    currentPage, totalPages, sizeValue, startRecord, endRecord, totalRecords

  Parameters (passed via jsp:include jsp:param):
    baseUrl     — the base action URL for links
    queryString — URL-encoded filter params (e.g., "&keyword=abc&branchId=1")

  Also renders the pagination-info bar (dropdown + summary).
--%>
<div class="pagination-info">
    <form method="get" action="${baseUrl}">
        <select name="sizeValue" onchange="this.form.submit()">
            <option value="30" ${sizeValue == 30 ? 'selected' : ''}>${option30}</option>
            <option value="50" ${sizeValue == 50 ? 'selected' : ''}>${option50}</option>
            <option value="70" ${sizeValue == 70 ? 'selected' : ''}>${option70}</option>
            <option value="100" ${sizeValue == 100 ? 'selected' : ''}>${option100}</option>
        </select>
        <span class="pagination-summary">
            ${startRecord} - ${endRecord} trong số ${totalRecords}
        </span>
    </form>
</div>

<c:if test="${totalPages > 1}">
    <div class="pagination">
        <c:if test="${currentPage > 1}">
            <a href="${baseUrl}?page=${currentPage - 1}&sizeValue=${sizeValue}${queryString}">&lt;&lt;</a>
        </c:if>

        <c:choose>
            <c:when test="${totalPages <= 5}">
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <a href="${baseUrl}?page=${i}&sizeValue=${sizeValue}${queryString}"
                       class="${i == currentPage ? 'active-page' : ''}">${i}</a>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <a href="${baseUrl}?page=1&sizeValue=${sizeValue}${queryString}"
                   class="${currentPage == 1 ? 'active-page' : ''}">1</a>
                <c:if test="${currentPage > 3}"><span class="dots">...</span></c:if>
                <c:forEach begin="${currentPage - 1 < 2 ? 2 : currentPage - 1}"
                           end="${currentPage + 1 > totalPages - 1 ? totalPages - 1 : currentPage + 1}" var="i">
                    <a href="${baseUrl}?page=${i}&sizeValue=${sizeValue}${queryString}"
                       class="${i == currentPage ? 'active-page' : ''}">${i}</a>
                </c:forEach>
                <c:if test="${currentPage < totalPages - 2}"><span class="dots">...</span></c:if>
                <a href="${baseUrl}?page=${totalPages}&sizeValue=${sizeValue}${queryString}"
                   class="${currentPage == totalPages ? 'active-page' : ''}">${totalPages}</a>
            </c:otherwise>
        </c:choose>

        <c:if test="${currentPage < totalPages}">
            <a href="${baseUrl}?page=${currentPage + 1}&sizeValue=${sizeValue}${queryString}">&gt;&gt;</a>
        </c:if>
    </div>
</c:if>

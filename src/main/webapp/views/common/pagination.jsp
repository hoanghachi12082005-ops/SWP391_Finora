<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="currentPageStr" value="${param.currentPage}" />
<c:set var="totalPagesStr" value="${param.totalPages}" />
<c:set var="url" value="${param.url}" />

<fmt:parseNumber var="currentPage" type="number" value="${not empty currentPageStr ? currentPageStr : '1'}" />
<fmt:parseNumber var="totalPages" type="number" value="${not empty totalPagesStr ? totalPagesStr : '1'}" />

<c:if test="${totalPages > 1}">
    <ul class="pagination mb-0">
        <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
            <a class="page-link" href="${url}${currentPage - 1}">&lt;</a>
        </li>

        <c:set var="startPage" value="${currentPage - 2}" />
        <c:set var="endPage" value="${currentPage + 2}" />
        
        <c:if test="${startPage < 1}">
            <c:set var="endPage" value="${endPage + (1 - startPage)}" />
            <c:set var="startPage" value="1" />
        </c:if>
        <c:if test="${endPage > totalPages}">
            <c:set var="startPage" value="${startPage - (endPage - totalPages)}" />
            <c:if test="${startPage < 1}"><c:set var="startPage" value="1" /></c:if>
            <c:set var="endPage" value="${totalPages}" />
        </c:if>

        <c:if test="${startPage > 1}">
            <li class="page-item"><a class="page-link" href="${url}1">1</a></li>
            <c:if test="${startPage > 2}">
                <li class="page-item disabled"><span class="page-link">...</span></li>
            </c:if>
        </c:if>

        <c:forEach begin="${startPage}" end="${endPage}" var="i">
            <li class="page-item ${currentPage == i ? 'active' : ''}">
                <a class="page-link" href="${url}${i}">${i}</a>
            </li>
        </c:forEach>

        <c:if test="${endPage < totalPages}">
            <c:if test="${endPage < totalPages - 1}">
                <li class="page-item disabled"><span class="page-link">...</span></li>
            </c:if>
            <li class="page-item"><a class="page-link" href="${url}${totalPages}">${totalPages}</a></li>
        </c:if>

        <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
            <a class="page-link" href="${url}${currentPage + 1}">&gt;</a>
        </li>
    </ul>
</c:if>

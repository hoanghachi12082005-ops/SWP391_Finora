<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    request.setAttribute("pageTitle", "Cấu hình kinh doanh");
    String successMessage = (String) session.getAttribute("successMessage");
    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("successMessage");
    session.removeAttribute("errorMessage");
    if (successMessage != null) request.setAttribute("_success", successMessage);
    if (errorMessage != null) request.setAttribute("_error", errorMessage);
%>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Cấu hình kinh doanh"/>
</jsp:include>
<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    <div class="main-content">

        <c:if test="${not empty _success}"><div class="alert alert-success">${_success}</div></c:if>
        <c:if test="${not empty _error}"><div class="alert alert-error">${_error}</div></c:if>

        <div class="p-4">
            <h1>Cấu hình kinh doanh</h1>
            <p>Cấu hình các thiết lập hệ thống</p>

            <!-- Cấu hình điểm tích lũy -->
            <div style="background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb; margin-top: 1.5rem;">
                <h3>Cài đặt điểm tích lũy</h3>
                <form method="post" action="${pageContext.request.contextPath}/configuration/business" style="margin-top: 1rem;">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <!-- Tỉ lệ tích điểm -->
                    <div style="margin-bottom: 1rem;">
                        <label style="display: block; font-weight: 600; margin-bottom: 0.25rem;">Tích điểm: bao nhiêu VNĐ được 1 điểm?</label>
                        <input type="number" name="earnValue" class="form-control"
                               value="<fmt:formatNumber value="${pointSetting.amountPerPoint}" type="number" groupingUsed="false" maxFractionDigits="0"/>"
                               min="1" required/>
                        <small class="form-text text-muted">Số tiền chi tiêu (VNĐ) để nhận 1 điểm tích lũy. Mặc định: 100.000 VNĐ</small>
                    </div>
                    <!-- Tỉ lệ đổi điểm -->
                    <div style="margin-bottom: 1rem;">
                        <label style="display: block; font-weight: 600; margin-bottom: 0.25rem;">Đổi điểm: 1 điểm = ? VNĐ</label>
                        <input type="number" name="redeemValue" class="form-control"
                               value="<fmt:formatNumber value="${pointSetting.pointToCurrency}" type="number" groupingUsed="false" maxFractionDigits="2"/>"
                               min="0" step="0.01" required/>
                        <small class="form-text text-muted">Giá trị quy đổi của 1 điểm ra tiền VNĐ. Mặc định: 1.00</small>
                    </div>
                    <button class="btn btn-danger" type="submit">Lưu</button>
                </form>
            </div>

            <!-- Cấu hình VAT -->
            <div style="background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb; margin-top: 1.5rem;">
                <h3>Cấu hình VAT</h3>

                <!-- VAT mặc định chung -->
                <h4 style="margin-top: 1rem; margin-bottom: 0.5rem;">VAT mặc định (áp dụng cho tất cả ngành hàng không có cấu hình riêng)</h4>
                <form method="post" action="${pageContext.request.contextPath}/configuration/business" style="margin-top: 1rem;">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <div style="margin-bottom: 1rem; display: flex; gap: 1rem; align-items: center;">
                        <input type="number" name="vatPercentage" class="form-control"
                               value="<fmt:formatNumber value="${vatSetting.vatPercentage}" type="number" groupingUsed="false" maxFractionDigits="2"/>"
                               min="0" max="100" step="0.1" required style="width: 150px;"/>
                        <span>%</span>
                        <button class="btn btn-danger" type="submit">Lưu VAT mặc định</button>
                    </div>
                </form>

                <hr style="margin: 1.5rem 0;">

                <!-- VAT cho từng ngành hàng -->
                <h4 style="margin-bottom: 0.75rem;">VAT theo ngành hàng</h4>
                <p style="color: #6b7280; font-size: 0.875rem; margin-bottom: 1rem;">Thiết lập tỷ lệ VAT riêng cho từng ngành hàng. Nếu chưa thiết lập, hệ thống sẽ dùng VAT mặc định.</p>

                <!-- Ô tìm kiếm ngành hàng -->
                <div style="margin-bottom: 1rem;">
                    <input type="text" id="vatCategorySearch" class="form-control"
                           placeholder="Tìm kiếm ngành hàng..."
                           style="max-width: 350px; padding: 0.5rem 0.75rem;"
                           onkeyup="filterVatCategories()">
                </div>

                <div style="overflow-x: auto;">
                <table id="vatCategoryTable" style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background: #f9fafb; text-align: left;">
                            <th style="padding: 0.75rem 1rem; border-bottom: 2px solid #e5e7eb; font-weight: 600;">Ngành hàng</th>
                            <th style="padding: 0.75rem 1rem; border-bottom: 2px solid #e5e7eb; font-weight: 600;">VAT (%)</th>
                            <th style="padding: 0.75rem 1rem; border-bottom: 2px solid #e5e7eb; font-weight: 600;">Trạng thái</th>
                            <th style="padding: 0.75rem 1rem; border-bottom: 2px solid #e5e7eb; font-weight: 600;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cat" items="${categories}">
                            <tr style="border-bottom: 1px solid #e5e7eb;">
                                <td style="padding: 0.75rem 1rem;">${cat.name}</td>
                                <td style="padding: 0.75rem 1rem;">
                                    <c:set var="foundCatVat" value="false"/>
                                    <c:forEach var="vs" items="${vatSettings}">
                                        <c:if test="${vs.categoryId eq cat.id}">
                                            <c:set var="catVatVal" value="${vs.vatPercentage}"/>
                                            <c:set var="catSettingId" value="${vs.settingId}"/>
                                            <c:set var="foundCatVat" value="true"/>
                                        </c:if>
                                    </c:forEach>
                                    <form method="post" action="${pageContext.request.contextPath}/configuration/business" style="display: flex; gap: 0.5rem; align-items: center;">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                        <input type="hidden" name="categoryId" value="${cat.id}">
                                        <input type="number" name="categoryVatPercentage" class="form-control"
                                               value="<c:choose><c:when test="${foundCatVat}"><fmt:formatNumber value="${catVatVal}" type="number" groupingUsed="false" maxFractionDigits="2"/></c:when><c:otherwise></c:otherwise></c:choose>"
                                               min="0" max="100" step="0.1" style="width: 100px;"
                                               placeholder="Mặc định">
                                        <span>%</span>
                                        <button class="btn btn-danger" type="submit" style="padding: 0.25rem 0.75rem; font-size: 0.8rem;">
                                            <c:choose><c:when test="${foundCatVat}">Cập nhật</c:when><c:otherwise>Thêm</c:otherwise></c:choose>
                                        </button>
                                    </form>
                                </td>
                                <td style="padding: 0.75rem 1rem;">
                                    <c:choose>
                                        <c:when test="${foundCatVat}">
                                            <span style="color: #059669; font-weight: 500;">Đã cấu hình</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #9ca3af;">Dùng mặc định</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="padding: 0.75rem 1rem;">
                                    <c:if test="${foundCatVat}">
                                        <form method="post" action="${pageContext.request.contextPath}/configuration/business" style="display: inline;">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="deleteCategoryVat" value="${cat.id}">
                                            <button type="submit" style="background: none; border: none; color: #dc2626; cursor: pointer; text-decoration: underline; font-size: 0.8rem;"
                                                    onclick="return confirm('Xóa cấu hình VAT cho ngành hàng này?')">Xóa</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                </div>
            </div>
        </div>

    </div>
</div>
<script>
function filterVatCategories() {
    var input = document.getElementById('vatCategorySearch');
    var filter = input.value.toLowerCase().trim();
    var table = document.getElementById('vatCategoryTable');
    var rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');

    for (var i = 0; i < rows.length; i++) {
        var nameCell = rows[i].getElementsByTagName('td')[0];
        if (nameCell) {
            var text = nameCell.textContent || nameCell.innerText;
            if (text.toLowerCase().indexOf(filter) > -1) {
                rows[i].style.display = '';
            } else {
                rows[i].style.display = 'none';
            }
        }
    }
}
</script>
<jsp:include page="/views/common/footer.jsp" />

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Danh sách chi nhánh"/>
    <jsp:param name="additionalCSS" value="branch.css"/>
</jsp:include>



<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp"/> 
    <main class="main-content">


        <div class="container-fluid py-4">
            <c:if test="${param.success == 'delete'}">
                <div style="background-color: #dcfce7; color: #15803d; padding: 15px; margin-bottom: 20px; border-radius: 8px; font-weight: 600;">
                    ✅ Xóa chi nhánh thành công!
                </div>
            </c:if>
            <c:if test="${param.error == 'deletefailed'}">
                <div style="background-color: #fee2e2; color: #b91c1c; padding: 15px; margin-bottom: 20px; border-radius: 8px; font-weight: 600;">
                    ❌ Không thể xóa chi nhánh này vì có dữ liệu liên quan (nhân viên, kho hàng, đơn hàng...)!
                </div>
            </c:if>

            <div class="page-header">

                <div class="page-title">

                    <h1>Quản lý cửa hàng</h1>

                    <p>
                        Quản lý toàn bộ chi nhánh trong hệ thống
                    </p>

                </div>

                <a href="${pageContext.request.contextPath}/branch?action=add"
                   class="btn-add">

                    + Thêm cửa hàng

                </a>

            </div>

            <!-- KPI Cards -->
            <div class="kpi-grid">
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng cửa hàng</p>
                        <h3>${totalBranch}</h3>
                    </div>
                    <div class="kpi-card-icon red">
                        <span class="material-icons">storefront</span>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Doanh thu hôm nay</p>
                        <h3>${todayRevenue}</h3>
                    </div>
                    <div class="kpi-card-icon orange">
                        <span class="material-icons">payments</span>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng nhân viên</p>
                        <h3>${totalEmployee}</h3>
                    </div>
                    <div class="kpi-card-icon green">
                        <span class="material-icons">group</span>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Cửa hàng tốt nhất</p>
                        <h3>${bestBranch}</h3>
                    </div>
                    <div class="kpi-card-icon blue">
                        <span class="material-icons">star</span>
                    </div>
                </div>
            </div>

            <!-- Search & Filter Card -->
            <div class="card shadow-sm border-0 mb-4" style="border-radius: 12px;">
                <div class="card-body p-4">
                    <form method="GET" action="${pageContext.request.contextPath}/branch" class="row g-3 align-items-center">
                        <input type="hidden" name="action" value="list">

                        <!-- Keyword Search -->
                        <div class="col-md-4">
                            <div class="branch-search-wrapper">
                                <span class="material-icons branch-search-icon">search</span>
                                <input type="text" class="branch-search-input" name="keyword"
                                       value="${param.keyword}"
                                       placeholder="Tìm kiếm cửa hàng, mã số...">
                            </div>
                        </div>

                        <!-- Status Filter -->
                        <div class="col-md-3">
                            <select class="form-select" name="status">
                                <option value="">Tất cả trạng thái</option>
                                <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                                <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>Ngừng hoạt động</option>
                            </select>
                        </div>

                        <!-- City Filter -->
                        <div class="col-md-3">
                            <select class="form-select" name="city">
                                <option value="">Tất cả thành phố</option>
                                <c:forEach items="${cityList}" var="city">
                                    <option value="${city}" ${city == selectedCity ? 'selected' : ''}>${city}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Submit & Reset Buttons -->
                        <div class="col-md-2 d-flex gap-2">
                            <button type="submit" class="btn btn-danger flex-grow-1" style="background-color: var(--primary-color); border-color: var(--primary-color);">Lọc</button>
                            <a href="${pageContext.request.contextPath}/branch?action=list" class="btn btn-outline-secondary" title="Xóa bộ lọc">
                                <span class="material-icons" style="font-size: 18px; line-height: 1.5;">refresh</span>
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <div class="table-card">

                <div class="table-header">
                    <h3>Danh sách chi nhánh</h3>
                </div>
                <table class="table branch-table">
                    <thead>
                        <tr> 
                            <th class="col-code">Mã</th>
                            <th class="col-name">Tên chi nhánh</th>
                            <th class="col-address">Địa chỉ</th>
                            <th class="col-phone">Điện thoại</th>
                            <th class="col-manager">Quản lý</th>
                                <c:if test="${showEmployeeColumn}">
                                <th class="col-employee">Nhân viên</th>
                                </c:if>
                            <th class="col-status">Trạng thái</th>
                            <th class="col-actions">Thao tác</th>
                        </tr>
                    </thead>

                    <tbody>
                        <c:forEach items="${branchList}" var="b">
                            <tr>
                                <td>
                                    <div class="branch-code-cell">

                                        <c:choose>
                                            <c:when test="${not empty b.imageUrl}">
                                                <img
                                                    src="${pageContext.request.contextPath}/assets/images/images_branch/${b.imageUrl}"
                                                    alt="${b.branchName}"
                                                    class="branch-avatar">
                                            </c:when>

                                            <c:otherwise>
                                                <div class="branch-avatar-placeholder">
                                                    <span class="material-icons">store</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <span>${b.branchCode}</span>

                                    </div>
                                </td>
                                <td class="col-name">
                                    <span class="branch-name" title="${fn:escapeXml(b.branchName)}">${b.branchName}</span>
                                </td>
                                <td class="col-address">
                                    <span class="branch-address">${b.fullAddress}</span>
                                </td>
                                <td>${b.phone}</td>
                                <td>${empty b.managerName ? 'Chưa phân công' : b.managerName}</td>
                                <c:if test="${showEmployeeColumn}">
                                    <td>${b.employeeCount}</td>
                                </c:if>
                                <td>
                                    <c:choose>
                                        <c:when test="${fn:toLowerCase(b.status) == 'active'}">
                                            <span class="branch-badge active">Hoạt động</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="branch-badge locked">Ngừng hoạt động</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <div class="action-group">
                                        <a class="btn-view"
                                           href="branch?action=detail&id=${b.branchId}">
                                            <span class="material-icons" style="color: rgb(24, 49, 83); font-size: 1.0rem; vertical-align: middle;">visibility</span>
                                        </a>

                                        <a class="btn-edit"
                                           href="branch?action=edit&id=${b.branchId}">
                                            <span class="material-icons" style="color: rgb(24, 49, 83); font-size: 1.0rem; vertical-align: middle;">edit</span>
                                        </a>

                                        <button type="button"
                                                class="btn-delete"
                                                data-id="${b.branchId}"
                                                data-name="${fn:escapeXml(b.branchName)}"
                                                onclick="openDeleteModal(this)">
                                            <span class="material-icons" style="color: rgb(24, 49, 83); font-size: 1.0rem; vertical-align: middle;">delete</span>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>

                </table>

                <jsp:include page="/views/common/pagination.jsp">
                    <jsp:param name="baseUrl" value="${baseUrl}"/>
                    <jsp:param name="queryString" value="&action=list&status=${empty selectedStatus ? '' : selectedStatus}&city=${empty selectedCity ? '' : selectedCity}"/>
                </jsp:include>
            </div>

        </div>

        <jsp:include page="delete-modal.jsp"/>


        <script>
            (function () {
                const CONFIRM_PHRASE = 'XAC NHAN';
                const contextPath = '${pageContext.request.contextPath}';

                const modal = document.getElementById('deleteModal');
                const nameEl = document.getElementById('deleteBranchName');
                const inputEl = document.getElementById('deleteConfirmInput');
                const confirmBtn = document.getElementById('deleteConfirmBtn');
                const errorEl = document.getElementById('deleteConfirmError');

                let pendingDeleteId = null;

                function isPhraseMatched() {
                    return inputEl.value.trim().toUpperCase() === CONFIRM_PHRASE;
                }

                function updateConfirmButton() {
                    const matched = isPhraseMatched();
                    confirmBtn.disabled = !matched;
                    errorEl.hidden = matched || inputEl.value.trim() === '';
                }

                window.openDeleteModal = function (trigger) {
                    pendingDeleteId = trigger.getAttribute('data-id');
                    nameEl.textContent = trigger.getAttribute('data-name') || '';
                    inputEl.value = '';
                    errorEl.hidden = true;
                    confirmBtn.disabled = true;
                    modal.classList.add('is-open');
                    inputEl.focus();
                };

                window.closeDeleteModal = function () {
                    modal.classList.remove('is-open');
                    pendingDeleteId = null;
                    inputEl.value = '';
                    confirmBtn.disabled = true;
                    errorEl.hidden = true;
                };

                inputEl.addEventListener('input', updateConfirmButton);

                inputEl.addEventListener('keydown', function (e) {
                    if (e.key === 'Enter' && isPhraseMatched() && pendingDeleteId) {
                        confirmBtn.click();
                    }
                    if (e.key === 'Escape') {
                        closeDeleteModal();
                    }
                });

                confirmBtn.addEventListener('click', function () {
                    if (!pendingDeleteId || !isPhraseMatched()) {
                        errorEl.hidden = false;
                        return;
                    }
                    window.location.href = contextPath + '/branch?action=delete&id=' + pendingDeleteId;
                });

                modal.addEventListener('click', function (e) {
                    if (e.target === modal) {
                        closeDeleteModal();
                    }
                });
            })();
        </script>
</div>
</main>
</div>
<jsp:include page="/views/common/footer.jsp"/>
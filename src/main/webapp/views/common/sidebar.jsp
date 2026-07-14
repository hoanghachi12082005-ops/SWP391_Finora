<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="roleName"
       value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : 'Nhân viên'}" />
<c:set var="fullName"
       value="${sessionScope.currentUser.fullName != null ? sessionScope.currentUser.fullName : 'Lê Minh Quân'}" />

<c:choose>
    <%-- PhucHoang: Tự động hiển thị sidebar-pos cho role SalesStaff --%>
    <c:when test="${roleName == 'SalesStaff'}">
        <jsp:include page="/views/common/sidebar-pos.jsp" />
    </c:when>
    <c:otherwise>
        <aside class="sidebar">
            <!-- Brand Logo -->
            <div class="sidebar-brand">
                <div class="sidebar-brand-icon">
                    <span class="material-icons">store</span>
                </div>
                <div class="sidebar-brand-text">
                    <h4>FINORA</h4>
                    <small>Hệ thống Quản trị Bán hàng</small>
        <div class="sidebar-menu-title">Chức năng chính</div>

        <c:set var="originalUri"
               value="${requestScope['jakarta.servlet.forward.request_uri'] != null ? requestScope['jakarta.servlet.forward.request_uri'] : pageContext.request.requestURI}" />

        <!-- Dashboard Owner Overview (Admin, Owner, StoreManager) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/dashboard/owner"
               class="sidebar-menu-item ${originalUri.contains('/dashboard/owner') ? 'active' : ''}">
                <span class="material-icons">dashboard</span>
                <span>Tổng quan</span>
            </a>
        </c:if>

        <!-- Customer Management (Owner, StoreManager) -->
        <c:if test="${roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/customers" 
               class="sidebar-menu-item ${originalUri.contains('/customers') ? 'active' : ''}">
                <span class="material-icons">people</span>
                <span>Khách hàng</span>
            </a>
        </c:if>

        <!-- Inventory Management (Owner, WarehouseStaff, StoreManager, Admin) -->
        <c:if test="${roleName == 'Owner' || roleName == 'WarehouseStaff' || roleName == 'StoreManager' || roleName == 'Admin'}">
            <c:set var="isInventoryActive" value="${originalUri.contains('/inventory') || originalUri.contains('/approval')}" />
            <a href="#inventoryCollapse" data-bs-toggle="collapse" role="button"
               aria-expanded="${isInventoryActive ? 'true' : 'false'}" aria-controls="inventoryCollapse"
               class="sidebar-menu-item ${isInventoryActive ? 'active' : ''} d-flex align-items-center">
                <span class="material-icons">inventory_2</span>
                <span>Kho hàng</span>
                <span class="material-icons ms-auto transition-icon"
                      style="font-size: 1.2rem;">expand_more</span>
            </a>
            <div class="collapse ${isInventoryActive ? 'show' : ''}" id="inventoryCollapse">
                <div class="sidebar-submenu">
                    <c:choose>
                        <c:when test="${roleName == 'Owner' || roleName == 'Admin'}">
                            <a href="${pageContext.request.contextPath}/inventory?tab=stock"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && (empty activeTab || activeTab == 'stock') ? 'active' : ''}">
                                Danh sách Kho
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=approval"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'approval' ? 'active' : ''}">
                                Xử Lý Phiếu (Duyệt)
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=history"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'history' ? 'active' : ''}">
                                Lịch sử
                            </a>
                        </c:when>
                        <c:when test="${roleName == 'StoreManager'}">
                            <a href="${pageContext.request.contextPath}/inventory?tab=stock&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && (empty activeTab || activeTab == 'stock') ? 'active' : ''}">
                                Tồn Kho
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=approval"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'approval' ? 'active' : ''}">
                                Xử Lý Phiếu (Duyệt)
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && (activeTab == 'transfer' || activeTab == 'createTransfer') ? 'active' : ''}">
                                Điều Chuyển
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=check&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'check' ? 'active' : ''}">
                                Kiểm kho
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=history&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'history' ? 'active' : ''}">
                                Lịch sử
                            </a>
                        </c:when>
                        <c:when test="${roleName == 'WarehouseStaff'}">
                            <a href="${pageContext.request.contextPath}/inventory?tab=stock&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && (empty activeTab || activeTab == 'stock') ? 'active' : ''}">
                                Tồn Kho
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && (activeTab == 'transfer' || activeTab == 'createTransfer') ? 'active' : ''}">
                                Điều Chuyển
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=check&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'check' ? 'active' : ''}">
                                Kiểm kho
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=pending_vouchers&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'pending_vouchers' ? 'active' : ''}">
                                Phiếu Chờ Duyệt
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=history&warehouseId=${sessionScope.selectedWarehouseId}"
                               class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'history' ? 'active' : ''}">
                                Lịch sử
                            </a>
                        </c:when>
                    </c:choose>
>>>>>>> origin/Thắng
                </div>
            </div>

            <!-- Navigation Menu -->
            <nav class="sidebar-menu">
                <div class="sidebar-menu-title">Chức năng chính</div>

                <c:set var="originalUri"
                       value="${requestScope['jakarta.servlet.forward.request_uri'] != null ? requestScope['jakarta.servlet.forward.request_uri'] : pageContext.request.requestURI}" />

                <!-- Dashboard Owner Overview (Admin, Owner, StoreManager) -->
                <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
                    <a href="${pageContext.request.contextPath}/dashboard/owner"
                       class="sidebar-menu-item ${originalUri.contains('/dashboard/owner') ? 'active' : ''}">
                        <span class="material-icons">dashboard</span>
                        <span>Tổng quan</span>
                    </a>
                </c:if>

                <!-- Customer Management (Owner, StoreManager) -->
                <c:if test="${roleName == 'Owner' || roleName == 'StoreManager'}">
                    <a href="${pageContext.request.contextPath}/customers" 
                       class="sidebar-menu-item ${originalUri.contains('/customers') ? 'active' : ''}">
                        <span class="material-icons">people</span>
                        <span>Khách hàng</span>
                    </a>
                </c:if>

                <!-- Inventory Management (Owner, WarehouseStaff, StoreManager, Admin) -->
                <c:if test="${roleName == 'Owner' || roleName == 'WarehouseStaff' || roleName == 'StoreManager' || roleName == 'Admin'}">
                    <c:set var="isInventoryActive" value="${originalUri.contains('/inventory') || originalUri.contains('/approval')}" />
                    <a href="#inventoryCollapse" data-bs-toggle="collapse" role="button"
                       aria-expanded="${isInventoryActive ? 'true' : 'false'}" aria-controls="inventoryCollapse"
                       class="sidebar-menu-item ${isInventoryActive ? 'active' : ''} d-flex align-items-center">
                        <span class="material-icons">inventory_2</span>
                        <span>Kho hàng</span>
                        <span class="material-icons ms-auto transition-icon"
                              style="font-size: 1.2rem;">expand_more</span>
                    </a>
                    <div class="collapse ${isInventoryActive ? 'show' : ''}" id="inventoryCollapse">
                        <div class="sidebar-submenu">
                            <c:choose>
                                <c:when test="${roleName == 'Owner' || roleName == 'Admin'}">
                                    <a href="${pageContext.request.contextPath}/inventory?tab=stock"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && (empty activeTab || activeTab == 'stock') ? 'active' : ''}">
                                        Danh sách Kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=approval"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'approval' ? 'active' : ''}">
                                        Xử Lý Phiếu (Duyệt)
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=history"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'history' ? 'active' : ''}">
                                        Lịch sử xuất nhập kho
                                    </a>
                                </c:when>
                                <c:when test="${roleName == 'StoreManager'}">
                                    <a href="${pageContext.request.contextPath}/inventory?tab=stock&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && (empty activeTab || activeTab == 'stock') ? 'active' : ''}">
                                        Tồn Kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=approval"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'approval' ? 'active' : ''}">
                                        Xử Lý Phiếu (Duyệt)
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && (activeTab == 'transfer' || activeTab == 'createTransfer') ? 'active' : ''}">
                                        Điều Chuyển
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=check&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'check' ? 'active' : ''}">
                                        Kiểm kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=import&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && (activeTab == 'import' || activeTab == 'export') ? 'active' : ''}">
                                        Phiếu Nhập / Xuất
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=history&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'history' ? 'active' : ''}">
                                        Lịch sử xuất nhập kho
                                    </a>
                                </c:when>
                                <c:when test="${roleName == 'WarehouseStaff'}">
                                    <a href="${pageContext.request.contextPath}/inventory?tab=stock&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && (empty activeTab || activeTab == 'stock') ? 'active' : ''}">
                                        Tồn Kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && (activeTab == 'transfer' || activeTab == 'createTransfer') ? 'active' : ''}">
                                        Điều Chuyển
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=check&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'check' ? 'active' : ''}">
                                        Kiểm kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=pending_vouchers&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'pending_vouchers' ? 'active' : ''}">
                                        Phiếu Chờ Duyệt
                                    </a>
                                    <a href="${pageContext.request.contextPath}/inventory?tab=history&warehouseId=${sessionScope.selectedWarehouseId}"
                                       class="sidebar-submenu-item ${originalUri.contains('/inventory') && activeTab == 'history' ? 'active' : ''}">
                                        Lịch sử xuất nhập kho
                                    </a>
                                </c:when>
                            </c:choose>
                        </div>
                    </div>
                </c:if>

                <!-- Product Management -->
                <c:if
                    test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager' || roleName == 'WarehouseStaff'}">
                    <c:set var="isProductActive"
                           value="${originalUri.contains('/products') || originalUri.contains('/category')}" />
                    <a href="#productsCollapse" data-bs-toggle="collapse" role="button"
                       aria-expanded="${isProductActive ? 'true' : 'false'}" aria-controls="productsCollapse"
                       class="sidebar-menu-item ${isProductActive ? 'active' : ''} d-flex align-items-center">
                        <span class="material-icons">shopping_bag</span>
                        <span>Hàng hóa</span>
                        <span class="material-icons ms-auto transition-icon"
                              style="font-size: 1.2rem;">expand_more</span>
                    </a>
                    <div class="collapse ${isProductActive ? 'show' : ''}" id="productsCollapse">
                        <div class="sidebar-submenu">
                            <a href="${pageContext.request.contextPath}/products"
                               class="sidebar-submenu-item ${originalUri.contains('/products') ? 'active' : ''}">
                                Sản phẩm
                            </a>
                            <a href="${pageContext.request.contextPath}/category"
                               class="sidebar-submenu-item ${originalUri.contains('/category') ? 'active' : ''}">
                                Danh mục
                            </a>
                        </div>
                    </div>
                </c:if>

                <!-- Cashbook (Sổ Quỹ) (Admin, Owner, StoreManager) -->
                <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
                    <a href="${pageContext.request.contextPath}/cashbook"
                       class="sidebar-menu-item ${originalUri.contains('/cashbook') ? 'active' : ''}">
                        <span class="material-icons">account_balance_wallet</span>
                        <span>Sổ Quỹ</span>
                    </a>
                </c:if>

                <!-- Suppliers / Partners -->
                <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager' || roleName == 'WarehouseStaff'}">
                    <a href="${pageContext.request.contextPath}/suppliers"
                       class="sidebar-menu-item ${pageContext.request.requestURI.contains('/suppliers') ? 'active' : ''}">
                        <span class="material-icons">handshake</span>
                        <span>Đối tác</span>
                    </a>
                </c:if>

                <!-- Branch / Store Management (Admin, Owner) -->
                <c:if test="${roleName == 'Admin' || roleName == 'Owner'}">
                    <a href="${pageContext.request.contextPath}/branch"
                       class="sidebar-menu-item ${originalUri.contains('/branch') ? 'active' : ''}">
                        <span class="material-icons">storefront</span>
                        <span>Chi nhánh</span>
                    </a>
                </c:if>

                <!-- User / Role Manager -->
                <c:if test="${roleName == 'Admin'}">
                    <a href="${pageContext.request.contextPath}/admin/user"
                       class="sidebar-menu-item ${originalUri.contains('/admin/user') ? 'active' : ''}">
                        <span class="material-icons">manage_accounts</span>
                        <span>Quản lý Nhân viên</span>
                    </a>
                </c:if>
                <c:if test="${roleName == 'Owner'}">
                    <a href="${pageContext.request.contextPath}/owner/emp"
                       class="sidebar-menu-item ${originalUri.contains('/owner/emp') ? 'active' : ''}">
                        <span class="material-icons">badge</span>
                        <span>Nhân viên</span>
                    </a>
                </c:if>
                <c:if test="${roleName == 'StoreManager'}">
                    <a href="${pageContext.request.contextPath}/manager/emp"
                       class="sidebar-menu-item ${originalUri.contains('/manager/emp') ? 'active' : ''}">
                        <span class="material-icons">badge</span>
                        <span>Nhân viên chi nhánh</span>
                    </a>
                </c:if>

                <!-- Reports -->
                <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
                    <c:set var="isReportsActive"
                           value="${originalUri.contains('/reports/') || originalUri.contains('/reports/employee-sales')}" />
                    <a href="#reportsCollapse" data-bs-toggle="collapse" role="button"
                       aria-expanded="${isReportsActive ? 'true' : 'false'}" aria-controls="reportsCollapse"
                       class="sidebar-menu-item ${isReportsActive ? 'active' : ''} d-flex align-items-center">
                        <span class="material-icons">bar_chart</span>
                        <span>Báo cáo</span>
                        <span class="material-icons ms-auto transition-icon"
                              style="font-size: 1.2rem;">expand_more</span>
                    </a>
                    <div class="collapse ${isReportsActive ? 'show' : ''}" id="reportsCollapse">
                        <div class="sidebar-submenu">
                            <a href="${pageContext.request.contextPath}/reports/employee-sales"
                               class="sidebar-submenu-item ${originalUri.contains('/reports/employee-sales') ? 'active' : ''}">
                                <span class="material-icons"
                                      style="font-size: 1rem; margin-right: 4px;">bar_chart</span>
                                Doanh thu nhân viên
                            </a>
                            <a href="${pageContext.request.contextPath}/reports/sales-by-store"
                               class="sidebar-submenu-item ${originalUri.contains('/reports/sales-by-store') ? 'active' : ''}">
                                <span class="material-icons"
                                       style="font-size: 1rem; margin-right: 4px;">storefront</span>
                                Doanh thu chi nhánh
                            </a>
                            <a href="${pageContext.request.contextPath}/reports/inventory"
                               class="sidebar-submenu-item ${originalUri.contains('/reports/inventory') ? 'active' : ''}">
                                <span class="material-icons"
                                       style="font-size: 1rem; margin-right: 4px;">inventory_2</span>
                                Hàng tồn kho
                            </a>
                            <a href="${pageContext.request.contextPath}/reports/customer-loyal"
                               class="sidebar-submenu-item ${originalUri.contains('/reports/customer-loyal') ? 'active' : ''}">
                                <span class="material-icons"
                                       style="font-size: 1rem; margin-right: 4px;">groups</span>
                                Khách hàng thân thiết
                            </a>
                        </div>
                    </div>
                </c:if>

                <!-- Configuration -->
                <c:if test="${roleName == 'Admin' || roleName == 'Owner'}">
                    <div class="sidebar-menu-title" style="margin-top: 16px;">Hệ thống</div>
                    <c:if test="${roleName == 'Owner'}">
                        <a href="${pageContext.request.contextPath}/activity-log"
                           class="sidebar-menu-item ${originalUri.contains('/activity-log') ? 'active' : ''}">
                            <span class="material-icons">history</span>
                            <span>Trung tâm hoạt động</span>
                        </a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/configuration/business"
                       class="sidebar-menu-item ${pageContext.request.requestURI.contains('/configuration/') ? 'active' : ''}">
                        <span class="material-icons">settings</span>
                        <span>Cấu hình</span>
                    </a>
                </c:if>
            </nav>

            <!-- User Profile block at bottom -->
            <div class="sidebar-user">
                <div class="sidebar-user-info">
                    <div class="sidebar-user-avatar">
                        ${fn:toUpperCase(fn:substring(fullName, 0, 2))}
                    </div>
                    <div class="sidebar-user-details">
                        <h6>
                            <c:out value="${fullName}" />
                        </h6>
                        <small>
                            <c:out value="${roleName}" />
                        </small>
                    </div>
                </div>
                <a href="${pageContext.request.contextPath}/profile" class="sidebar-user-logout" title="Hồ sơ">
                    <span class="material-icons">person</span>
                </a>
                <a href="${pageContext.request.contextPath}/logout" class="sidebar-user-logout" title="Đăng xuất">
                    <span class="material-icons">logout</span>
                </a>
            </div>

            <!-- Fallback style for pages without Bootstrap CSS -->
            <style>
                .collapse:not(.show) {
                    display: none !important;
                }
                .collapsing {
                    height: 0;
                    overflow: hidden;
                    transition: height 0.35s ease;
                }
                /* Reset sales.css max-height overrides */
                .sidebar-submenu {
                    max-height: none !important;
                    overflow: visible !important;
                    display: flex !important;
                    flex-direction: column !important;
                    gap: 4px !important;
                }
            </style>

            <!-- Fallback script for pages without Bootstrap JS -->
            <script>
                // JS hiển thị toast "chức năng đang phát triển" cho POS
                function showComingSoon(event) {
                    if (event) event.preventDefault();
                    const toast = document.getElementById('comingSoonToast');
                    if (toast) {
                        toast.classList.add('show');
                        setTimeout(() => {
                            toast.classList.remove('show');
                        }, 2500);
                    }
                }

                document.addEventListener("DOMContentLoaded", function() {
                    const toggles = document.querySelectorAll('[data-bs-toggle="collapse"]');
                    toggles.forEach(function(toggle) {
                        toggle.addEventListener('click', function(e) {
                            if (typeof bootstrap !== 'undefined') {
                                return;
                            }
                            e.preventDefault();
                            const targetSelector = toggle.getAttribute('href') || toggle.getAttribute('data-bs-target');
                            if (targetSelector) {
                                const targetEl = document.querySelector(targetSelector);
                                if (targetEl) {
                                    const isShown = targetEl.classList.contains('show');
                                    if (isShown) {
                                        targetEl.classList.remove('show');
                                        toggle.setAttribute('aria-expanded', 'false');
                                    } else {
                                        targetEl.classList.add('show');
                                        toggle.setAttribute('aria-expanded', 'true');
                                    }
                                }
                            }
                        });
                    });
                });
            </script>
        </aside>
    </c:otherwise>
</c:choose>

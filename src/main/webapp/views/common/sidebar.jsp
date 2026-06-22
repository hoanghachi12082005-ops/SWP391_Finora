<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
/* KiotViet-style Sidebar */
.sidebar {
    width: var(--sidebar-width, 250px);
    background: #ffffff;
    border-right: 1px solid var(--border-color);
    display: flex;
    flex-direction: column;
    height: 100vh;
    position: sticky;
    top: 0;
    box-shadow: 2px 0 8px rgba(0,0,0,0.02);
    z-index: 100;
}

.sidebar-header {
    padding: 20px 24px;
    display: flex;
    align-items: center;
    gap: 12px;
    border-bottom: 1px solid var(--border-color);
}

.brand-logo {
    width: 36px;
    height: 36px;
    background: var(--primary-color, #93000b);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 18px;
    flex-shrink: 0;
}

.brand-info {
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.brand-name {
    font-weight: 700;
    font-size: 15px;
    color: var(--primary-color, #93000b);
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
}

.branch-name {
    font-size: 12px;
    color: var(--text-secondary);
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
}

.sidebar-nav {
    flex: 1;
    overflow-y: auto;
    padding: 16px 0;
}

.nav-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.nav-item {
    margin-bottom: 4px;
    padding: 0 12px;
}

.nav-link {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 14px;
    border-radius: var(--radius-sm);
    color: var(--text-primary);
    text-decoration: none;
    font-weight: 500;
    font-size: 14px;
    transition: all 0.2s ease;
}

.nav-link:hover {
    background: var(--primary-bg, #fff1f0);
    color: var(--primary-color, #93000b);
}

.nav-link .material-icons {
    font-size: 20px;
    color: var(--text-secondary);
    transition: color 0.2s ease;
}

.nav-link:hover .material-icons {
    color: var(--primary-color, #93000b);
}

.nav-item.active .nav-link {
    background: var(--primary-color, #93000b);
    color: white;
    box-shadow: 0 2px 8px rgba(147, 0, 11, 0.25);
}

.nav-item.active .nav-link .material-icons {
    color: white;
}

/* Submenu */
.nav-item-has-children .nav-link {
    justify-content: space-between;
}

.nav-link-content {
    display: flex;
    align-items: center;
    gap: 12px;
}

.nav-submenu {
    list-style: none;
    padding: 4px 0 4px 44px;
    margin: 0;
    display: none;
}

.nav-item.expanded .nav-submenu {
    display: block;
}

.nav-item.active .nav-submenu {
    display: block;
}

.submenu-link {
    display: block;
    padding: 8px 12px;
    color: var(--text-secondary);
    text-decoration: none;
    font-size: 13px;
    border-radius: var(--radius-sm);
    transition: all 0.2s ease;
}

.submenu-link:hover {
    color: var(--primary-color, #93000b);
    background: rgba(0,0,0,0.02);
}

.submenu-item.active .submenu-link {
    color: var(--primary-color, #93000b);
    font-weight: 600;
}

.sidebar-footer {
    padding: 16px 20px;
    border-top: 1px solid var(--border-color);
    background: #fafafa;
}

.pos-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    padding: 10px;
    background: var(--primary-color, #93000b);
    color: white;
    border-radius: var(--radius-sm);
    text-decoration: none;
    font-weight: bold;
    font-size: 14px;
    margin-bottom: 12px;
    transition: all 0.2s ease;
}

.pos-button:hover {
    background: var(--primary-dark, #760008);
    color: white;
}

.footer-link {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--text-secondary);
    text-decoration: none;
    font-size: 13px;
    padding: 8px 0;
    transition: color 0.2s ease;
}

.footer-link:hover {
    color: var(--text-primary);
}

.footer-link .material-icons {
    font-size: 18px;
}
</style>

<aside class="sidebar">
    <div class="sidebar-header">
        <div class="brand-logo">K</div>
        <div class="brand-info">
            <span class="brand-name">Hệ thống Quản lý</span>
            <span class="branch-name">Chi nhánh Trung tâm</span>
        </div>
    </div>
    
    <nav class="sidebar-nav">
        <ul class="nav-list">
            <!-- Tổng quan -->
            <li class="nav-item ${param.active == 'dashboard' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/dashboard/owner" class="nav-link">
                    <span class="material-icons">dashboard</span>
                    <span>Tổng quan</span>
                </a>
            </li>
            
            <!-- Hàng hóa -->
            <li class="nav-item nav-item-has-children ${param.active == 'categories' || param.active == 'products' ? 'active expanded' : ''}">
                <a href="#" class="nav-link" onclick="this.parentElement.classList.toggle('expanded'); return false;">
                    <div class="nav-link-content">
                        <span class="material-icons">inventory_2</span>
                        <span>Hàng hóa</span>
                    </div>
                    <span class="material-icons" style="font-size: 16px;">expand_more</span>
                </a>
                <ul class="nav-submenu">
                    <li class="submenu-item ${param.active == 'categories' ? 'active' : ''}">
                        <a href="${pageContext.request.contextPath}/category" class="submenu-link">Danh mục</a>
                    </li>
                    <li class="submenu-item ${param.active == 'products' ? 'active' : ''}">
                        <a href="${pageContext.request.contextPath}/products" class="submenu-link">Sản phẩm</a>
                    </li>
                </ul>
            </li>
            
            <!-- Giao dịch -->
            <li class="nav-item ${param.active == 'transactions' ? 'active' : ''}">
                <a href="#" class="nav-link">
                    <span class="material-icons">receipt_long</span>
                    <span>Giao dịch</span>
                </a>
            </li>
            
            <!-- Đối tác -->
            <li class="nav-item ${param.active == 'customers' || param.active == 'suppliers' ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/customers" class="nav-link">
                    <span class="material-icons">handshake</span>
                    <span>Đối tác</span>
                </a>
            </li>
            
            <!-- Nhân viên -->
            <li class="nav-item ${param.active == 'employees' ? 'active' : ''}">
                <a href="#" class="nav-link">
                    <span class="material-icons">badge</span>
                    <span>Nhân viên</span>
                </a>
            </li>
            
            <!-- Báo cáo -->
            <li class="nav-item ${param.active == 'reports' ? 'active' : ''}">
                <a href="#" class="nav-link">
                    <span class="material-icons">assessment</span>
                    <span>Báo cáo</span>
                </a>
            </li>
        </ul>
    </nav>
    
    <div class="sidebar-footer">
        <a href="#" class="pos-button">Vào POS</a>
        <a href="${pageContext.request.contextPath}/role-selection" class="footer-link">
            <span class="material-icons">manage_accounts</span>
            Chuyển vai trò
        </a>
        <a href="${pageContext.request.contextPath}/logout" class="footer-link">
            <span class="material-icons">logout</span>
            Đăng xuất
        </a>
    </div>
</aside>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<%@ page import="util.database.DBContext" %>
<%@ page import="java.util.*" %>
<%
    // Define target roles and their localized display names
    Map<String, String> roleLabels = new LinkedHashMap<>();
    roleLabels.put("Owner", "Chủ cửa hàng");
    roleLabels.put("StoreManager", "Quản lý chi nhánh");
    roleLabels.put("SalesStaff", "Nhân viên bán hàng");
    roleLabels.put("WarehouseStaff", "Nhân viên kho");
    
    // Map to hold actual queried username/email from the DB
    Map<String, String> credentials = new LinkedHashMap<>();
    
    try (Connection conn = DBContext.getConnection()) {
        String sql = "SELECT TOP 1 e.email, r.role_name " +
                     "FROM Employee e " +
                     "JOIN Role r ON e.role_id = r.role_id " +
                     "WHERE LOWER(r.role_name) = LOWER(?) AND e.status = 'ACTIVE' " +
                     "ORDER BY e.emp_id ASC";
        
        for (String roleName : roleLabels.keySet()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, roleName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        credentials.put(roleName, rs.getString("email"));
                    }
                }
            }
        }
    } catch (Exception e) {
        // Fallback or print log in development
        System.err.println("QuickLogin Helper: Database connection failed, using default fallback. Error: " + e.getMessage());
    }
    
    // Fallback defaults if database is not active, empty or fails to connect
    if (credentials.isEmpty()) {
        credentials.put("Owner", "owner@finora.vn");
        credentials.put("StoreManager", "cuong.lv@finora.vn");
        credentials.put("SalesStaff", "phuong.nt@finora.vn");
        credentials.put("WarehouseStaff", "quan.tv@finora.vn");
    }
%>

<div class="quick-login-container mt-4 pt-3 border-top">
    <div class="text-center mb-3">
        <span class="text-muted small fw-semibold">Đăng nhập nhanh vai trò Demo:</span>
    </div>
    <div class="row g-2">
        <% 
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                String roleKey = entry.getKey();
                String email = entry.getValue();
                String label = roleLabels.get(roleKey);
                
                // Map icons for each role
                String icon = "account_circle";
                if ("Owner".equals(roleKey)) icon = "store";
                else if ("StoreManager".equals(roleKey)) icon = "manage_accounts";
                else if ("SalesStaff".equals(roleKey)) icon = "point_of_sale";
                else if ("WarehouseStaff".equals(roleKey)) icon = "inventory_2";
        %>
            <div class="col-6">
                <button type="button" class="btn btn-outline-danger btn-sm w-100 py-2 d-flex align-items-center justify-content-center gap-2 quick-login-btn"
                        data-username="<%= email %>" 
                        data-password="123456"
                        style="font-size: 12px; border-radius: 8px; font-weight: 500; transition: all 0.2s;">
                    <span class="material-icons" style="font-size: 16px;"><%= icon %></span>
                    <%= label %>
                </button>
            </div>
        <% 
            } 
        %>
    </div>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        const quickLoginBtns = document.querySelectorAll(".quick-login-btn");
        const usernameInput = document.getElementById("username");
        const passwordInput = document.getElementById("password");
        
        quickLoginBtns.forEach(btn => {
            btn.addEventListener("click", function() {
                const u = this.getAttribute("data-username");
                const p = this.getAttribute("data-password");
                
                if (usernameInput && passwordInput) {
                    usernameInput.value = u;
                    passwordInput.value = p;
                    
                    // Visual feedback: brief highlight
                    usernameInput.style.borderColor = "#93000b";
                    usernameInput.style.boxShadow = "0 0 0 0.2rem rgba(147, 0, 11, 0.25)";
                    passwordInput.style.borderColor = "#93000b";
                    passwordInput.style.boxShadow = "0 0 0 0.2rem rgba(147, 0, 11, 0.25)";
                    
                    setTimeout(() => {
                        usernameInput.style.borderColor = "";
                        usernameInput.style.boxShadow = "";
                        passwordInput.style.borderColor = "";
                        passwordInput.style.boxShadow = "";
                    }, 1000);
                }
            });
        });
    });
</script>

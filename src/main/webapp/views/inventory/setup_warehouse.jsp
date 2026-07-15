<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 
  ==========================================================================
  TRANG THIẾT LẬP KHO HÀNG ĐẦU TIÊN (setup_warehouse.jsp)
  - Được kích hoạt từ `InventoryController` khi chi nhánh hiện tại của người dùng chưa cấu hình bất kỳ kho hàng nào.
  - Hiển thị giao diện chào mừng và form tạo nhanh kho hàng đầu tiên để đi vào hoạt động.
  ==========================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thiết lập kho hàng - KiotRetail</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/inventory/inventory-setup-warehouse.css" rel="stylesheet">
</head>
<body>

    <div class="setup-card">
        <span class="material-icons setup-icon">inventory_2</span>
        <h2>Thiết lập kho hàng</h2>
        <p>Chào mừng bạn đến với module Quản lý Kho! Hãy khởi tạo kho hàng đầu tiên để bắt đầu lưu trữ sản phẩm.</p>
        
        <form action="${pageContext.request.contextPath}/inventory" method="POST">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="setupWarehouse">
            
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Tên Kho Hàng <span class="text-danger">*</span></label>
                <input type="text" name="warehouseName" class="form-control premium-input" required 
                       placeholder="VD: Kho Trung Tâm Cầu Giấy" value="Kho trung tâm">
            </div>
            
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Địa Chỉ Kho</label>
                <input type="text" name="address" class="form-control premium-input" 
                       placeholder="Nhập địa chỉ cụ thể của kho...">
            </div>
            
            <button type="submit" class="btn btn-premium">Hoàn tất khởi tạo</button>
        </form>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

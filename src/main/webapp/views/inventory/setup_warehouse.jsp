<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thiết lập kho hàng - KiotRetail</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f8f9fc;
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .setup-card {
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
            padding: 40px;
            max-width: 500px;
            width: 100%;
            text-align: center;
        }
        .setup-card h2 {
            font-weight: 700;
            color: #1a1f36;
            margin-bottom: 10px;
        }
        .setup-card p {
            color: #697386;
            margin-bottom: 30px;
        }
        .setup-icon {
            font-size: 64px;
            color: #0052cc;
            margin-bottom: 20px;
            background: rgba(0, 82, 204, 0.1);
            padding: 20px;
            border-radius: 50%;
            display: inline-block;
        }
        .premium-input {
            border-radius: 8px;
            border: 1px solid #e2e8f0;
            padding: 12px 16px;
            box-shadow: 0 1px 2px rgba(0,0,0,0.05);
            transition: all 0.2s;
            text-align: left;
        }
        .premium-input:focus {
            border-color: #0052cc;
            box-shadow: 0 0 0 3px rgba(0,82,204,0.1);
            outline: none;
        }
        .btn-premium {
            background-color: #0052cc;
            color: white;
            border-radius: 8px;
            padding: 12px 24px;
            font-weight: 600;
            width: 100%;
            border: none;
            transition: background 0.2s;
            margin-top: 20px;
        }
        .btn-premium:hover {
            background-color: #0043a8;
        }
    </style>
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kho hàng chưa được tạo - Finora Retail</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f4f6f9;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .notice-card {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
            max-width: 520px;
            width: 100%;
            padding: 40px 30px;
            text-align: center;
        }
        .notice-icon-wrapper {
            width: 80px;
            height: 80px;
            background-color: #fff3cd;
            color: #856404;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 24px auto;
        }
        .notice-icon-wrapper .material-icons {
            font-size: 44px;
        }
        .notice-title {
            font-size: 22px;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 12px;
        }
        .notice-desc {
            font-size: 15px;
            color: #64748b;
            line-height: 1.6;
            margin-bottom: 28px;
        }
        .contact-box {
            background-color: #f8fafc;
            border: 1px dashed #cbd5e1;
            border-radius: 10px;
            padding: 16px;
            margin-bottom: 28px;
            text-align: left;
        }
        .contact-box div {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 14px;
            color: #334155;
        }
        .contact-box .material-icons {
            font-size: 20px;
            color: #0284c7;
        }
        .btn-action {
            padding: 10px 24px;
            font-weight: 600;
            border-radius: 8px;
        }
    </style>
</head>
<body>

    <div class="notice-card">
        <div class="notice-icon-wrapper">
            <span class="material-icons">storefront</span>
        </div>
        <h2 class="notice-title">Kho Hàng Chưa Được Khởi Tạo</h2>
        <p class="notice-desc">
            Cửa hàng hiện tại của bạn chưa được thiết lập kho hàng trên hệ thống. 
            Do đó, các chức năng quản lý tồn kho và bán hàng tạm thời chưa thể sử dụng.
        </p>

        <div class="contact-box">
            <div>
                <span class="material-icons">info</span>
                <span>Vui lòng liên hệ <strong>Quản lý cửa hàng (Store Manager)</strong> hoặc <strong>Chủ cửa hàng (Owner)</strong> để khởi tạo kho hàng đầu tiên.</span>
            </div>
        </div>

        <div class="d-flex justify-content-center">
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-primary btn-action px-4">
                <span class="material-icons align-middle fs-6 me-1">logout</span>Đăng xuất
            </a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

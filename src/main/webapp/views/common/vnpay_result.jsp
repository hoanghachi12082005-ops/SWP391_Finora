<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết quả thanh toán VNPAY - Finora</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>body{font-family:'Inter',sans-serif}</style>
</head>
<body class="bg-gray-50 min-h-screen flex items-center justify-center p-6">
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8 text-center">
        <c:choose>
            <c:when test="${status == 'success'}">
                <div class="w-20 h-20 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-5">
                    <svg class="w-10 h-10 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
                <h1 class="text-2xl font-bold text-amber-700 mb-2">Đơn hàng đang được xử lý</h1>
                <p class="text-gray-500 mb-6">Chúng tôi sẽ liên hệ với bạn sau</p>
            </c:when>
            <c:otherwise>
                <div class="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-5">
                    <svg class="w-10 h-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                    </svg>
                </div>
                <h1 class="text-2xl font-bold text-red-700 mb-2">Thanh toán thất bại</h1>
            </c:otherwise>
        </c:choose>
        
        <c:if test="${status != 'success'}">
        <p class="text-gray-500 mb-6">${message}</p>
        </c:if>
        
        <div class="bg-gray-50 rounded-xl p-5 text-left space-y-3 mb-6">
            <div class="flex justify-between">
                <span class="text-gray-500">Mã đơn hàng:</span>
                <span class="font-semibold">${orderCode}</span>
            </div>
            <div class="flex justify-between">
                <span class="text-gray-500">Số tiền:</span>
                <span class="font-semibold"><fmt:formatNumber value="${amount}" type="number" maxFractionDigits="0"/> ₫</span>
            </div>
            <c:if test="${not empty transactionNo}">
            <div class="flex justify-between">
                <span class="text-gray-500">Mã giao dịch VNPAY:</span>
                <span class="font-semibold">${transactionNo}</span>
            </div>
            </c:if>
            <c:if test="${not empty bankCode}">
            <div class="flex justify-between">
                <span class="text-gray-500">Ngân hàng:</span>
                <span class="font-semibold">${bankCode}</span>
            </div>
            </c:if>
        </div>
        
        <div class="flex gap-3 justify-center">
            <a href="${pageContext.request.contextPath}/sales" 
               class="px-6 py-3 bg-primary text-white rounded-xl font-semibold hover:bg-secondary transition-colors">
                Quay lại bán hàng
            </a>
            <a href="${pageContext.request.contextPath}/orders" 
               class="px-6 py-3 border border-gray-300 rounded-xl font-semibold hover:bg-gray-50 transition-colors">
                Xem đơn hàng
            </a>
        </div>
    </div>
</body>
</html>
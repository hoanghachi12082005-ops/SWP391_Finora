<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Finora — Chức năng đang phát triển</title>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
</head>
<body class="bg-[#f8f9fa] text-[#191c1d] flex flex-col justify-between min-h-screen font-['Inter']">
    <div class="flex-1 flex flex-col items-center justify-center p-6 text-center">
        <span class="material-symbols-outlined text-[96px] text-[#af101a] mb-4">construction</span>
        <h1 class="text-3xl font-bold mb-2">Chức năng đang phát triển</h1>
        <p class="text-lg text-gray-500 mb-6">Trang này hiện đang được hoàn thiện. Vui lòng quay lại sau.</p>
        <a href="${pageContext.request.contextPath}/sales" class="bg-[#af101a] text-white px-6 py-3 rounded-xl font-semibold flex items-center gap-2 hover:bg-[#b51a1b] transition-colors shadow">
            <span class="material-symbols-outlined">arrow_back</span>
            Quay lại trang Bán hàng (POS)
        </a>
    </div>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Finora — Lịch sử đơn hàng</title>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <script>
    tailwind.config={darkMode:"class",theme:{extend:{colors:{"secondary":"#b51a1b","primary-fixed-dim":"#ffb3ac","tertiary-fixed-dim":"#88d982","on-tertiary-container":"#d8ffd0","tertiary":"#11651d","background":"#f8f9fa","tertiary-fixed":"#a3f69c","inverse-on-surface":"#f0f1f2","error-container":"#ffdad6","surface-dim":"#d9dadb","on-background":"#191c1d","on-surface":"#191c1d","primary-fixed":"#ffdad6","on-secondary-container":"#fffbff","surface-container-lowest":"#ffffff","surface-container-highest":"#e1e3e4","surface-variant":"#e1e3e4","surface-container-high":"#e7e8e9","on-secondary":"#ffffff","inverse-surface":"#2e3132","on-tertiary":"#ffffff","on-secondary-fixed-variant":"#93000b","surface-tint":"#ba1a20","surface":"#f8f9fa","error":"#ba1a1a","on-error-container":"#93000a","primary-container":"#d32f2f","surface-container-low":"#f3f4f5","on-surface-variant":"#5b403d","on-primary-fixed-variant":"#930010","on-primary-container":"#fff2f0","surface-container":"#edeeef","surface-bright":"#f8f9fa","on-error":"#ffffff","on-tertiary-fixed":"#002204","tertiary-container":"#307f34","inverse-primary":"#ffb3ac","on-tertiary-fixed-variant":"#005312","on-primary-fixed":"#410003","outline":"#8f6f6c","secondary-fixed-dim":"#ffb4ab","outline-variant":"#e4beba","on-primary":"#ffffff","secondary-fixed":"#ffdad6","secondary-container":"#d93630","on-secondary-fixed":"#410002","primary":"#af101a"},borderRadius:{DEFAULT:"0.25rem",lg:"0.5rem",xl:"0.75rem",full:"9999px"},spacing:{"stack-lg":"24px","container-padding":"32px","stack-sm":"8px","gutter":"24px","section-gap":"48px","unit":"8px","stack-md":"16px"},fontFamily:{"body-md":["Inter"],"label-md":["Inter"],"caption":["Inter"],"headline-md":["Inter"],"headline-lg":["Inter"],"button-text":["Inter"],"title-lg":["Inter"],"display-lg":["Inter"],"body-lg":["Inter"]},fontSize:{"body-md":["16px",{lineHeight:"24px",fontWeight:"500"}],"label-md":["14px",{lineHeight:"20px",fontWeight:"600"}],"caption":["12px",{lineHeight:"16px",fontWeight:"400"}],"headline-md":["24px",{lineHeight:"32px",fontWeight:"700"}],"headline-lg":["32px",{lineHeight:"40px",letterSpacing:"-0.01em",fontWeight:"700"}],"button-text":["16px",{lineHeight:"24px",fontWeight:"600"}],"title-lg":["20px",{lineHeight:"28px",fontWeight:"600"}],"display-lg":["48px",{lineHeight:"56px",letterSpacing:"-0.02em",fontWeight:"700"}],"body-lg":["18px",{lineHeight:"26px",fontWeight:"500"}]}}}};
    </script>
    <style>
        body{font-family:'Inter',sans-serif}
        .material-symbols-outlined{font-variation-settings:'FILL' 0,'wght' 500,'GRAD' 0,'opsz' 24;vertical-align:middle}
        .scrollbar-thin::-webkit-scrollbar{width:6px}
        .scrollbar-thin::-webkit-scrollbar-track{background:transparent}
        .scrollbar-thin::-webkit-scrollbar-thumb{background:#d9dadb;border-radius:999px}
    </style>
</head>
<body class="bg-background text-on-surface overflow-hidden h-screen">
<div class="flex h-screen w-screen">

    <!-- Include Sidebar -->
    <jsp:include page="/common/sidebar.jsp" />

    <!-- Main Workspace -->
    <div class="flex-1 flex flex-col min-w-0 h-screen relative">
        
        <!-- Header (72px) -->
        <header class="h-[72px] bg-surface border-b border-outline-variant flex items-center px-6 gap-4 shrink-0 z-10">
            <h1 class="text-title-lg font-bold text-primary mr-4">Lịch sử đơn hàng</h1>
            
            <form action="${pageContext.request.contextPath}/orders" method="GET" class="w-96 relative">
                <div class="flex items-center bg-surface-container-low rounded-full px-4 h-11 gap-3 border border-transparent focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/10 transition-all">
                    <span class="material-symbols-outlined text-outline text-[20px]">search</span>
                    <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Tìm mã đơn, khách hàng..." class="bg-transparent outline-none border-none flex-1 text-sm placeholder:text-outline focus:ring-0 focus:border-none" autocomplete="off">
                </div>
            </form>

            <jsp:include page="/common/header.jsp" />
        </header>

        <!-- Main content area split in two -->
        <div class="flex flex-1 overflow-hidden relative">
            
            <!-- Left Pane: Orders Table -->
            <div class="flex-1 overflow-y-auto p-6 scrollbar-thin">
                <div class="bg-surface-container-lowest rounded-2xl border border-outline-variant shadow-sm overflow-hidden">
                    <table class="w-full text-left border-collapse text-sm">
                        <thead>
                            <tr class="bg-surface-container border-b border-outline-variant text-on-surface-variant font-semibold">
                                <th class="py-4 px-6">Mã đơn hàng</th>
                                <th class="py-4 px-6">Thời gian</th>
                                <th class="py-4 px-6">Khách hàng</th>
                                <th class="py-4 px-6">Thu ngân</th>
                                <th class="py-4 px-6 text-right">Tổng tiền</th>
                                <th class="py-4 px-6 text-center">Trạng thái</th>
                                <th class="py-4 px-6 text-center">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-outline-variant">
                            <c:forEach items="${orders}" var="o">
                                <tr onclick="showOrderDetails(${o.orderId})" class="hover:bg-surface-container-low/50 cursor-pointer transition-colors duration-150" id="row-order-${o.orderId}">
                                    <td class="py-4 px-6 text-primary font-bold">${o.orderCode}</td>
                                    <td class="py-4 px-6 text-on-surface-variant">${o.createdAt}</td>
                                    <td class="py-4 px-6 font-medium">${not empty o.customerName ? o.customerName : 'Khách vãng lai'}</td>
                                    <td class="py-4 px-6 text-on-surface-variant">${o.employeeName}</td>
                                    <td class="py-4 px-6 text-right font-semibold">
                                        <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0" /> đ
                                    </td>
                                    <td class="py-4 px-6 text-center" id="badge-container-${o.orderId}">
                                        <c:choose>
                                            <c:when test="${o.status == 'COMPLETED'}">
                                                <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-tertiary-fixed text-on-tertiary-fixed">COMPLETED</span>
                                            </c:when>
                                            <c:when test="${o.status == 'CANCELLED'}">
                                                <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-error-container text-on-error-container">CANCELLED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-surface-container-highest text-on-surface">PENDING</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="py-4 px-6 text-center" onclick="event.stopPropagation(); showOrderDetails(${o.orderId})">
                                        <button class="text-primary hover:text-secondary inline-flex items-center">
                                            <span class="material-symbols-outlined text-[20px]">visibility</span>
                                        </button>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty orders}">
                                <tr>
                                    <td colspan="7" class="py-12 text-center text-on-surface-variant">Không tìm thấy đơn hàng nào.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Right Pane: Slide-out Panel (Width: 480px) -->
            <div id="detailPanel" class="w-[480px] bg-surface-container border-l border-outline-variant h-full absolute right-0 top-0 z-30 shadow-2xl flex flex-col translate-x-full transition-transform duration-300 ease-out">
                
                <!-- Panel Header -->
                <div class="h-[72px] border-b border-outline-variant px-6 flex items-center justify-between shrink-0 bg-surface">
                    <div>
                        <span class="text-caption text-outline">CHI TIẾT ĐƠN HÀNG</span>
                        <h2 id="detailOrderCode" class="text-title-lg font-bold text-primary">HD--</h2>
                    </div>
                    <button onclick="closeDetails()" class="w-10 h-10 rounded-full hover:bg-surface-container-high flex items-center justify-center transition-colors">
                        <span class="material-symbols-outlined text-[22px]">close</span>
                    </button>
                </div>

                <!-- Panel Content -->
                <div class="flex-1 overflow-y-auto p-6 space-y-6 scrollbar-thin">
                    
                    <!-- Bento Grid 2x2 -->
                    <div class="grid grid-cols-2 gap-4">
                        <div class="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant/60">
                            <span class="text-caption text-outline flex items-center gap-1.5"><span class="material-symbols-outlined text-sm">schedule</span>Thời gian</span>
                            <div id="detailTime" class="text-sm font-semibold mt-1 text-on-surface">--</div>
                        </div>
                        <div class="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant/60">
                            <span class="text-caption text-outline flex items-center gap-1.5"><span class="material-symbols-outlined text-sm">storefront</span>Kênh bán</span>
                            <div class="text-sm font-semibold mt-1 text-on-surface">Bán trực tiếp</div>
                        </div>
                        <div class="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant/60">
                            <span class="text-caption text-outline flex items-center gap-1.5"><span class="material-symbols-outlined text-sm">person</span>Nhân viên</span>
                            <div id="detailEmployee" class="text-sm font-semibold mt-1 text-on-surface">--</div>
                        </div>
                        <div class="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant/60">
                            <span class="text-caption text-outline flex items-center gap-1.5"><span class="material-symbols-outlined text-sm">info</span>Trạng thái</span>
                            <div class="mt-1" id="detailStatusBadge">--</div>
                        </div>
                    </div>

                    <!-- Customer Info -->
                    <div class="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant/60 flex items-center gap-4">
                        <div class="w-12 h-12 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center font-bold text-lg" id="detailCustomerAvatar">K</div>
                        <div class="flex-1 leading-tight">
                            <h4 id="detailCustomerName" class="text-label-md font-bold text-on-surface">Khách vãng lai</h4>
                            <p id="detailCustomerPhone" class="text-caption text-outline mt-0.5">SĐT: --</p>
                            <span class="inline-flex items-center text-[11px] font-semibold text-tertiary bg-on-tertiary-container/30 px-2 py-0.5 rounded-full mt-1.5" id="detailCustomerPoints">Điểm tích lũy: 0</span>
                        </div>
                    </div>

                    <!-- Product List -->
                    <div class="bg-surface-container-lowest rounded-xl border border-outline-variant/60 overflow-hidden">
                        <div class="px-4 py-3 bg-surface-container/40 border-b border-outline-variant/60 text-xs font-bold text-on-surface-variant">SẢN PHẨM TRONG ĐƠN</div>
                        <table class="w-full text-xs text-left border-collapse">
                            <thead>
                                <tr class="bg-surface-container/20 text-on-surface-variant border-b border-outline-variant/60 font-semibold">
                                    <th class="py-2.5 px-4">Sản phẩm</th>
                                    <th class="py-2.5 px-4 text-center">SL</th>
                                    <th class="py-2.5 px-4 text-right">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody id="detailProductList" class="divide-y divide-outline-variant/40">
                                <!-- Dynamic -->
                            </tbody>
                        </table>
                    </div>

                    <!-- Breakdown -->
                    <div class="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant/60 space-y-2.5 text-sm">
                        <div class="flex justify-between text-on-surface-variant">
                            <span>Tạm tính</span>
                            <span id="detailSubtotal">0 đ</span>
                        </div>
                        <div class="flex justify-between text-on-surface-variant">
                            <span>Giảm giá</span>
                            <span id="detailDiscount" class="text-error font-medium">-0 đ</span>
                        </div>
                        <div class="flex justify-between text-on-surface-variant">
                            <span>VAT (8%)</span>
                            <span id="detailTax">0 đ</span>
                        </div>
                        <hr class="border-outline-variant/60 my-1">
                        <div class="flex justify-between text-on-surface font-bold text-base">
                            <span>Tổng cộng</span>
                            <span id="detailTotal" class="text-primary">0 đ</span>
                        </div>
                    </div>
                </div>

                <!-- Panel Footer -->
                <div class="p-6 border-t border-outline-variant bg-surface space-y-3 shrink-0">
                    <div class="grid grid-cols-2 gap-3">
                        <button class="h-11 border border-outline-variant text-on-surface rounded-xl hover:bg-surface-container-high transition-colors font-semibold text-sm flex items-center justify-center gap-2">
                            <span class="material-symbols-outlined text-[20px]">print</span>
                            <span>In hóa đơn</span>
                        </button>
                        <button class="h-11 border border-outline-variant text-on-surface rounded-xl hover:bg-surface-container-high transition-colors font-semibold text-sm flex items-center justify-center gap-2">
                            <span class="material-symbols-outlined text-[20px]">mail</span>
                            <span>Gửi Email</span>
                        </button>
                    </div>
                    <button id="btnRefundOrder" onclick="refundOrder()" class="w-full h-12 bg-error text-on-error rounded-xl font-bold text-sm hover:bg-secondary transition-colors shadow-sm flex items-center justify-center gap-2">
                        <span class="material-symbols-outlined text-[20px]">assignment_return</span>
                        <span>Hoàn trả đơn hàng</span>
                    </button>
                </div>

            </div>

        </div>

    </div>
</div>

<script>
    let activeSelectedOrderId = 0;

    function formatVND(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
    }

    function showOrderDetails(orderId) {
        activeSelectedOrderId = orderId;
        
        fetch('${pageContext.request.contextPath}/orders/detail?id=' + orderId)
            .then(res => res.json())
            .then(data => {
                if (data.error) {
                    alert(data.error);
                    return;
                }

                document.getElementById('detailOrderCode').innerText = data.orderCode;
                document.getElementById('detailTime').innerText = data.createdAt;
                document.getElementById('detailEmployee').innerText = data.employeeName;

                // Status Badge in panel
                let badgeHtml = '';
                if (data.status === 'COMPLETED') {
                    badgeHtml = '<span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-tertiary-fixed text-on-tertiary-fixed">COMPLETED</span>';
                } else if (data.status === 'CANCELLED') {
                    badgeHtml = '<span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-error-container text-on-error-container">CANCELLED</span>';
                } else {
                    badgeHtml = '<span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-surface-container-highest text-on-surface">PENDING</span>';
                }
                document.getElementById('detailStatusBadge').innerHTML = badgeHtml;

                // Customer details
                document.getElementById('detailCustomerAvatar').innerText = data.customerName.charAt(0).toUpperCase();
                document.getElementById('detailCustomerName').innerText = data.customerName;
                document.getElementById('detailCustomerPhone').innerText = 'SĐT: ' + (data.customerPhone ? data.customerPhone : '---');
                document.getElementById('detailCustomerPoints').innerText = 'Điểm tích lũy: ' + (data.customerPoints ? data.customerPoints : '0');

                // Items list
                let itemsHtml = '';
                data.items.forEach(item => {
                    itemsHtml += `
                        <tr class="hover:bg-surface-container-low/20">
                            <td class="py-3 px-4">
                                <div class="font-semibold text-on-surface">\${item.productName}</div>
                                <div class="text-[10px] text-outline mt-0.5">\${item.productCode}</div>
                            </td>
                            <td class="py-3 px-4 text-center text-on-surface font-medium">\${item.quantity}</td>
                            <td class="py-3 px-4 text-right font-bold text-on-surface">\${formatVND(item.totalPrice)}</td>
                        </tr>
                    `;
                });
                document.getElementById('detailProductList').innerHTML = itemsHtml;

                // Calculation Breakdown
                document.getElementById('detailSubtotal').innerText = formatVND(data.subtotal);
                document.getElementById('detailDiscount').innerText = '-' + formatVND(data.discountAmount);
                
                // VAT calculation
                let totalBeforeTax = data.subtotal - data.discountAmount;
                let vat = totalBeforeTax * 0.08;
                document.getElementById('detailTax').innerText = formatVND(vat);
                document.getElementById('detailTotal').innerText = formatVND(data.totalAmount);

                // Refund button visibility
                let refundBtn = document.getElementById('btnRefundOrder');
                if (data.status === 'COMPLETED') {
                    refundBtn.classList.remove('hidden');
                } else {
                    refundBtn.classList.add('hidden');
                }

                // Slide panel in
                document.getElementById('detailPanel').classList.remove('translate-x-full');
            })
            .catch(err => {
                console.error(err);
                alert("Lỗi tải thông tin chi tiết đơn hàng.");
            });
    }

    function closeDetails() {
        document.getElementById('detailPanel').classList.add('translate-x-full');
    }

    function refundOrder() {
        if (!activeSelectedOrderId) return;
        if (!confirm("Bạn có chắc chắn muốn hoàn trả đơn hàng này? Việc hoàn trả sẽ hủy đơn và ghi nhận lịch sử.")) return;

        let params = new URLSearchParams();
        params.append('orderId', activeSelectedOrderId);

        fetch('${pageContext.request.contextPath}/orders/refund', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        })
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success') {
                alert(data.message);
                
                // Update table badge immediately without reloading
                let tableBadge = document.getElementById('badge-container-' + activeSelectedOrderId);
                if (tableBadge) {
                    tableBadge.innerHTML = '<span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-error-container text-on-error-container">CANCELLED</span>';
                }

                // Update detail status badge and refund button
                document.getElementById('detailStatusBadge').innerHTML = '<span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-error-container text-on-error-container">CANCELLED</span>';
                document.getElementById('btnRefundOrder').classList.add('hidden');
            } else {
                alert(data.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert("Đã xảy ra lỗi khi thực hiện hoàn trả đơn hàng.");
        });
    }
</script>
</body>
</html>

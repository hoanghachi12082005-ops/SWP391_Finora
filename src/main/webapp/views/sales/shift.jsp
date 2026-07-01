<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Finora — Quản lý ca làm</title>
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
        .modal-blur{backdrop-filter:blur(6px);background-color:rgba(25,28,29,0.3)}
    </style>
</head>
<body class="bg-background text-on-surface overflow-hidden h-screen">
<div class="flex h-screen w-screen">

    <!-- Include Sidebar -->
    <jsp:include page="/views/common/sidebar.jsp" />

    <!-- Main Workspace -->
    <div class="flex-1 flex flex-col min-w-0 h-screen relative">

        <!-- Header (72px) -->
        <header class="h-[72px] bg-surface border-b border-outline-variant flex items-center px-6 gap-4 shrink-0 z-10">
            <div class="flex items-center gap-6">
                <h1 class="text-title-lg font-bold text-primary">Quản lý Ca làm</h1>
                <div class="flex h-11 items-end gap-1">
                    <button class="px-4 pb-2 border-b-2 border-primary text-primary font-bold text-sm">Ca hiện tại</button>
                    <button class="px-4 pb-2 text-on-surface-variant hover:bg-surface-container-low text-sm rounded-t-lg transition-colors" onclick="scrollToHistory()">Lịch sử ca</button>
                </div>
            </div>

            <jsp:include page="/common/header.jsp" />
        </header>

        <!-- Sub-Header Actions -->
        <div class="h-14 bg-surface border-b border-outline-variant px-6 flex items-center justify-between shrink-0">
            <span class="text-sm text-outline font-semibold">
                <c:choose>
                    <c:when test="${not empty activeShift}">
                        Ca làm việc đang hoạt động của <b>${sessionScope.employee.fullName}</b>
                    </c:when>
                    <c:otherwise>Không có ca làm việc nào đang mở.</c:otherwise>
                </c:choose>
            </span>
            <div class="flex items-center gap-3">
                <c:choose>
                    <c:when test="${not empty activeShift}">
                        <button class="h-9 px-4 border border-secondary text-secondary rounded-lg hover:bg-error-container/20 transition-colors font-semibold text-xs flex items-center gap-1.5">
                            <span class="material-symbols-outlined text-[16px]">print</span>
                            <span>In báo cáo ca</span>
                        </button>
                        <button onclick="openCloseShiftModal()" class="h-9 px-4 bg-secondary text-white rounded-lg hover:bg-secondary/90 transition-colors font-semibold text-xs flex items-center gap-1.5 shadow-sm">
                            <span class="material-symbols-outlined text-[16px]">lock_open</span>
                            <span>Kết thúc ca</span>
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button onclick="openOpenShiftModal()" class="h-9 px-4 bg-primary text-white rounded-lg hover:bg-primary/95 transition-colors font-semibold text-xs flex items-center gap-1.5 shadow-sm">
                            <span class="material-symbols-outlined text-[16px]">add_circle</span>
                            <span>Mở ca mới</span>
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Scrollable Dashboard / Form Content -->
        <div class="flex-1 overflow-y-auto p-6 scrollbar-thin space-y-6">

            <c:choose>
                <c:when test="${not empty activeShift}">
                    <!-- ============================================== -->
                    <!-- ACTIVE SHIFT BENTO GRID                        -->
                    <!-- ============================================== -->
                    <div class="grid grid-cols-12 gap-6">

                        <!-- Left Panel (col-span-4): Cashier Profile / Meta -->
                        <div class="col-span-4 bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between min-h-[340px]">
                            <div class="flex flex-col items-center text-center">
                                <!-- Pulsing Avatar Container -->
                                <div class="relative">
                                    <div class="w-20 h-20 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center font-bold text-3xl shadow-md border-2 border-primary">
                                        ${fn:substring(sessionScope.employee.fullName, 0, 1)}
                                    </div>
                                    <span class="absolute bottom-1.5 right-1.5 w-4 h-4 bg-tertiary border-2 border-white rounded-full flex items-center justify-center">
                                        <span class="w-1.5 h-1.5 bg-white rounded-full animate-pulse"></span>
                                    </span>
                                </div>

                                <div class="mt-4 leading-tight">
                                    <h3 class="text-lg font-bold text-on-surface">${sessionScope.employee.fullName}</h3>
                                    <span class="text-caption text-outline uppercase font-semibold mt-1 block">Nhân viên Thu ngân</span>
                                </div>

                                <div class="w-full border-t border-outline-variant/50 my-5 pt-4 space-y-3 text-sm text-left">
                                    <div class="flex justify-between">
                                        <span class="text-on-surface-variant">Chi nhánh</span>
                                        <span class="font-bold text-on-surface">${shiftSummary.branchName}</span>
                                    </div>
                                    <div class="flex justify-between">
                                        <span class="text-on-surface-variant">Mã máy thu ngân</span>
                                        <span class="font-semibold text-on-surface">MAY-01</span>
                                    </div>
                                    <div class="flex justify-between">
                                        <span class="text-on-surface-variant">Giờ bắt đầu ca</span>
                                        <span class="font-semibold text-on-surface">${activeShift.openedAt}</span>
                                    </div>
                                </div>
                            </div>

                            <button onclick="openCashTxModal()" class="w-full h-11 border-2 border-dashed border-outline hover:border-primary text-on-surface hover:text-primary rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2">
                                <span class="material-symbols-outlined text-[20px]">account_balance_wallet</span>
                                <span>Rút/Nạp tiền ngăn kéo</span>
                            </button>
                        </div>

                        <!-- Right Panel (col-span-8): Cash Flow & Totals -->
                        <div class="col-span-8 space-y-6">

                            <!-- Two KPI Summary cards -->
                            <div class="grid grid-cols-2 gap-6">
                                <div class="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant shadow-sm">
                                    <span class="text-xs font-bold text-outline uppercase tracking-wider">Tiền mặt đầu ca</span>
                                    <div class="text-2xl font-bold text-primary mt-2">
                                        <fmt:formatNumber value="${shiftSummary.openingCash}" pattern="#,##0" />đ
                                    </div>
                                    <!-- Progress bar design -->
                                    <div class="w-full bg-surface-container-high h-2.5 rounded-full mt-4 overflow-hidden">
                                        <div class="bg-primary h-full rounded-full" style="width: 35%"></div>
                                    </div>
                                </div>

                                <div class="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant shadow-sm">
                                    <span class="text-xs font-bold text-outline uppercase tracking-wider">Doanh thu tiền mặt</span>
                                    <div class="text-2xl font-bold text-tertiary mt-2">
                                        +<fmt:formatNumber value="${shiftSummary.cashSales}" pattern="#,##0" />đ
                                    </div>
                                    <!-- Progress bar design -->
                                    <div class="w-full bg-surface-container-high h-2.5 rounded-full mt-4 overflow-hidden">
                                        <div class="bg-tertiary h-full rounded-full" style="width: 60%"></div>
                                    </div>
                                </div>
                            </div>

                            <!-- Cash Ledger Table Card -->
                            <div class="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between">
                                <div class="border-b border-outline-variant/60 pb-3 mb-4">
                                    <h3 class="text-label-md font-bold text-on-surface uppercase">Chi tiết dòng tiền mặt</h3>
                                </div>
                                <div class="space-y-3.5 text-sm">
                                    <div class="flex justify-between font-medium text-on-surface-variant">
                                        <span>Tiền mặt đầu ca (+)</span>
                                        <span><fmt:formatNumber value="${shiftSummary.openingCash}" pattern="#,##0" />đ</span>
                                    </div>
                                    <div class="flex justify-between font-medium text-tertiary">
                                        <span>Doanh thu bán hàng tiền mặt (+)</span>
                                        <span>+<fmt:formatNumber value="${shiftSummary.cashSales}" pattern="#,##0" />đ</span>
                                    </div>

                                    <!-- Transactions list -->
                                    <div id="ajaxTxList" class="space-y-3">
                                        <c:forEach items="${transactions}" var="tx">
                                            <div class="flex justify-between font-medium ${tx.type == 'DEPOSIT' ? 'text-tertiary' : 'text-error'}">
                                                <span>
                                                    ${tx.type == 'DEPOSIT' ? 'Nạp tiền lẻ (+)' : 'Rút tiền mặt (-)'}
                                                    <c:if test="${not empty tx.note}">
                                                        <span class="text-xs text-outline font-normal">(${tx.note})</span>
                                                    </c:if>
                                                </span>
                                                <span>
                                                    ${tx.type == 'DEPOSIT' ? '+' : '-'}<fmt:formatNumber value="${tx.amount}" pattern="#,##0" />đ
                                                </span>
                                            </div>
                                        </c:forEach>
                                    </div>

                                    <hr class="border-outline-variant/60">
                                    
                                    <!-- Expected Cash display -->
                                    <div class="flex justify-between font-bold text-base bg-primary-container/20 p-4 rounded-xl text-primary">
                                        <span>Dự kiến trong két (Tiền mặt lý thuyết)</span>
                                        <span id="expectedCashText"><fmt:formatNumber value="${shiftSummary.expectedCash}" pattern="#,##0" />đ</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                    <!-- Bottom row KPI breakdown (Full Width, grid-cols-3) -->
                    <div class="grid grid-cols-3 gap-6">
                        
                        <!-- Support / Discrepancy Alert Card -->
                        <div class="p-6 rounded-2xl border shadow-sm flex flex-col justify-between h-[130px] bg-surface-container-lowest border-outline-variant" id="cardSupportAlert">
                            <div>
                                <h4 class="text-xs font-bold text-outline uppercase tracking-wider">Cần hỗ trợ?</h4>
                                <p class="text-xs text-outline mt-1.5">
                                    Liên hệ Quản lý chi nhánh qua số nội bộ <b>#101</b> để xử lý chênh lệch quỹ hoặc gửi yêu cầu nạp tiền khẩn cấp.
                                </p>
                            </div>
                        </div>

                        <!-- Card/Bank Transfer sales -->
                        <div class="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between h-[130px]">
                            <h4 class="text-xs font-bold text-outline uppercase tracking-wider">Thẻ / Chuyển khoản</h4>
                            <div>
                                <div class="text-[28px] font-bold text-primary">
                                    <fmt:formatNumber value="${shiftSummary.bankSales}" pattern="#,##0" />đ
                                </div>
                                <span class="text-[10px] text-outline font-semibold">Tự động đối soát ngân hàng liên kết</span>
                            </div>
                        </div>

                        <!-- Total Shift Revenue -->
                        <div class="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between h-[130px]">
                            <h4 class="text-xs font-bold text-outline uppercase tracking-wider">Tổng doanh thu ca</h4>
                            <div>
                                <div class="text-[28px] font-bold text-primary">
                                    <fmt:formatNumber value="${shiftSummary.totalRevenue}" pattern="#,##0" />đ
                                </div>
                                <span class="text-[10px] text-outline font-semibold">Bao gồm toàn bộ phương thức thanh toán</span>
                            </div>
                        </div>

                    </div>
                </c:when>

                <c:otherwise>
                    <!-- ============================================== -->
                    <!-- CLOSED STATE: OPEN SHIFT PORTAL / HISTORY      -->
                    <!-- ============================================== -->
                    <div class="max-w-xl mx-auto bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant shadow-lg text-center space-y-6">
                        <div class="w-16 h-16 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center mx-auto text-3xl">
                            <span class="material-symbols-outlined text-[32px]">lock</span>
                        </div>
                        <div class="leading-tight">
                            <h2 class="text-xl font-bold text-on-surface">Mở ca làm việc mới</h2>
                            <p class="text-xs text-outline mt-1.5">Vui lòng kiểm đếm tiền mặt thực tế trong két và nhập số dư đầu ca.</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/shift" method="POST" class="space-y-4 text-left">
                            <input type="hidden" name="action" value="open">
                            <div>
                                <label class="text-xs font-bold text-on-surface-variant block mb-1.5 uppercase">Số tiền mặt đầu ca (đ):</label>
                                <input type="text" name="openingCash" value="1,000,000" onkeyup="formatInputCurrency(this)" class="w-full h-12 px-4 rounded-xl border border-outline-variant bg-surface-container-low font-bold text-primary focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none text-lg" autocomplete="off" required>
                            </div>
                            <button type="submit" class="w-full h-12 bg-primary text-on-primary rounded-xl font-bold hover:bg-secondary transition-colors shadow-md flex items-center justify-center gap-2">
                                <span class="material-symbols-outlined text-[20px]">play_circle</span>
                                <span>Xác nhận Mở ca làm việc</span>
                            </button>
                        </form>
                    </div>

                    <!-- Shift History List Section -->
                    <div id="shiftHistorySection" class="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm mt-10">
                        <div class="border-b border-outline-variant/60 pb-3 mb-4">
                            <h3 class="text-label-md font-bold text-on-surface uppercase">Lịch sử ca làm việc chi nhánh</h3>
                        </div>
                        <table class="w-full text-left text-xs border-collapse">
                            <thead>
                                <tr class="bg-surface-container border-b border-outline-variant text-on-surface-variant font-semibold">
                                    <th class="py-3 px-4">Mã ca</th>
                                    <th class="py-3 px-4">Nhân viên</th>
                                    <th class="py-3 px-4">Bắt đầu</th>
                                    <th class="py-3 px-4">Kết thúc</th>
                                    <th class="py-3 px-4 text-right">Tiền đầu ca</th>
                                    <th class="py-3 px-4 text-right">Tiền cuối ca</th>
                                    <th class="py-3 px-4 text-right">Tiền dự kiến</th>
                                    <th class="py-3 px-4 text-center">Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-outline-variant">
                                <c:forEach items="${shiftHistory}" var="s">
                                    <tr class="hover:bg-surface-container-low/30">
                                        <td class="py-3 px-4 font-bold text-primary">#CA-${s.shiftId}</td>
                                        <td class="py-3 px-4 font-semibold text-on-surface">${s.employeeName}</td>
                                        <td class="py-3 px-4 text-on-surface-variant">${s.openedAt}</td>
                                        <td class="py-3 px-4 text-on-surface-variant">${not empty s.closedAt ? s.closedAt : '---'}</td>
                                        <td class="py-3 px-4 text-right font-medium">
                                            <fmt:formatNumber value="${s.openingCash}" pattern="#,##0" />đ
                                        </td>
                                        <td class="py-3 px-4 text-right font-bold text-tertiary">
                                            <c:choose>
                                                <c:when test="${not empty s.closingCash}">
                                                    <fmt:formatNumber value="${s.closingCash}" pattern="#,##0" />đ
                                                </c:when>
                                                <c:otherwise>---</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="py-3 px-4 text-right font-medium text-on-surface-variant">
                                            <fmt:formatNumber value="${s.expectedCash}" pattern="#,##0" />đ
                                        </td>
                                        <td class="py-3 px-4 text-center">
                                            <c:choose>
                                                <c:when test="${s.status == 'OPEN'}">
                                                    <span class="inline-flex items-center px-2 py-0.5 rounded-full font-bold bg-tertiary-fixed text-on-tertiary-fixed text-[10px]">OPEN</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center px-2 py-0.5 rounded-full font-bold bg-surface-container-highest text-on-surface text-[10px]">CLOSED</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>

    </div>
</div>

<!-- ============================================== -->
<!-- MODAL: DEPOSIT / WITHDRAW                      -->
<!-- ============================================== -->
<div id="cashTxModal" class="hidden fixed inset-0 z-50 flex items-center justify-center modal-blur">
    <div class="bg-surface-container-lowest w-full max-w-md p-6 rounded-2xl border border-outline-variant shadow-2xl space-y-4">
        <div class="flex justify-between items-center border-b border-outline-variant/60 pb-3">
            <h3 class="text-label-md font-bold text-on-surface uppercase">Rút/Nạp tiền mặt ngăn kéo</h3>
            <button onclick="closeCashTxModal()" class="w-8 h-8 rounded-full hover:bg-surface-container-high flex items-center justify-center">
                <span class="material-symbols-outlined text-[20px]">close</span>
            </button>
        </div>

        <div class="space-y-4">
            <div>
                <label class="text-xs font-bold text-on-surface-variant block mb-1.5 uppercase">Loại giao dịch:</label>
                <div class="grid grid-cols-2 gap-3">
                    <button type="button" id="tabWithdraw" onclick="selectTxType('WITHDRAW')" class="h-10 rounded-lg border-2 border-primary bg-on-primary-container text-primary font-bold text-sm flex items-center justify-center gap-1.5">
                        <span class="material-symbols-outlined text-[18px]">remove_circle</span>
                        <span>Rút tiền</span>
                    </button>
                    <button type="button" id="tabDeposit" onclick="selectTxType('DEPOSIT')" class="h-10 rounded-lg border border-outline-variant hover:bg-surface-container-high text-on-surface font-semibold text-sm flex items-center justify-center gap-1.5">
                        <span class="material-symbols-outlined text-[18px]">add_circle</span>
                        <span>Nạp thêm tiền</span>
                    </button>
                </div>
            </div>

            <div>
                <label class="text-xs font-bold text-on-surface-variant block mb-1.5 uppercase">Số tiền (đ):</label>
                <input type="text" id="txAmount" placeholder="Nhập số tiền..." onkeyup="formatInputCurrency(this)" class="w-full h-11 px-4 rounded-xl border border-outline-variant bg-surface-container-low font-bold text-primary outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 text-base" autocomplete="off">
            </div>

            <div>
                <label class="text-xs font-bold text-on-surface-variant block mb-1.5 uppercase">Ghi chú / Lý do:</label>
                <textarea id="txNote" rows="2" placeholder="Ví dụ: Nộp tiền doanh thu về két chính / Bổ sung tiền lẻ đầu ca..." class="w-full p-3 rounded-xl border border-outline-variant bg-surface-container-low text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 resize-none"></textarea>
            </div>
        </div>

        <div class="flex justify-end gap-3 border-t border-outline-variant/60 pt-4">
            <button onclick="closeCashTxModal()" class="h-11 px-5 border border-outline-variant rounded-xl font-semibold text-sm hover:bg-surface-container-high transition-colors">Hủy bỏ</button>
            <button onclick="submitCashTx()" class="h-11 px-6 bg-primary text-on-primary rounded-xl font-bold text-sm hover:bg-secondary transition-colors shadow-md">Xác nhận</button>
        </div>
    </div>
</div>

<!-- ============================================== -->
<!-- MODAL: CLOSE SHIFT                             -->
<!-- ============================================== -->
<div id="closeShiftModal" class="hidden fixed inset-0 z-50 flex items-center justify-center modal-blur">
    <div class="bg-surface-container-lowest w-full max-w-md p-6 rounded-2xl border border-outline-variant shadow-2xl space-y-4">
        <div class="flex justify-between items-center border-b border-outline-variant/60 pb-3">
            <h3 class="text-label-md font-bold text-on-surface uppercase">Kiểm đếm kết thúc ca làm</h3>
            <button onclick="closeCloseShiftModal()" class="w-8 h-8 rounded-full hover:bg-surface-container-high flex items-center justify-center">
                <span class="material-symbols-outlined text-[20px]">close</span>
            </button>
        </div>

        <form action="${pageContext.request.contextPath}/shift" method="POST" class="space-y-4">
            <input type="hidden" name="action" value="close">
            <div class="bg-primary-container/10 p-4 rounded-xl border border-primary/20 leading-snug">
                <div class="text-xs text-outline">Số dư tiền mặt lý thuyết trong ngăn kéo:</div>
                <div class="text-xl font-bold text-primary mt-1">
                    <fmt:formatNumber value="${shiftSummary.expectedCash}" pattern="#,##0" />đ
                </div>
            </div>

            <div>
                <label class="text-xs font-bold text-on-surface-variant block mb-1.5 uppercase">Số tiền mặt kiểm đếm thực tế (đ):</label>
                <input type="text" name="closingCash" id="closingCashInput" onkeyup="formatInputCurrency(this)" class="w-full h-11 px-4 rounded-xl border border-outline-variant bg-surface-container-low font-bold text-primary outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 text-base" autocomplete="off" required>
            </div>

            <div class="flex justify-end gap-3 border-t border-outline-variant/60 pt-4">
                <button type="button" onclick="closeCloseShiftModal()" class="h-11 px-5 border border-outline-variant rounded-xl font-semibold text-sm hover:bg-surface-container-high transition-colors">Hủy bỏ</button>
                <button type="submit" class="h-11 px-6 bg-secondary text-white rounded-xl font-bold text-sm hover:bg-secondary/90 transition-colors shadow-md">Xác nhận Kết thúc ca</button>
            </div>
        </form>
    </div>
</div>

<script>
    let currentTxType = 'WITHDRAW';

    function formatVND(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + 'đ';
    }

    function formatInputCurrency(input) {
        let val = input.value.replace(/\D/g, "");
        if (val) {
            input.value = new Intl.NumberFormat('vi-VN').format(parseInt(val));
        } else {
            input.value = "";
        }
    }

    function scrollToHistory() {
        let historyEl = document.getElementById('shiftHistorySection');
        if (historyEl) {
            historyEl.scrollIntoView({ behavior: 'smooth' });
        }
    }

    // Cash transaction modal handlers
    function openCashTxModal() {
        document.getElementById('cashTxModal').classList.remove('hidden');
        document.getElementById('txAmount').value = "";
        document.getElementById('txNote').value = "";
        selectTxType('WITHDRAW');
    }

    function closeCashTxModal() {
        document.getElementById('cashTxModal').classList.add('hidden');
    }

    function selectTxType(type) {
        currentTxType = type;
        let tabW = document.getElementById('tabWithdraw');
        let tabD = document.getElementById('tabDeposit');
        
        if (type === 'WITHDRAW') {
            tabW.className = "h-10 rounded-lg border-2 border-primary bg-on-primary-container text-primary font-bold text-sm flex items-center justify-center gap-1.5";
            tabD.className = "h-10 rounded-lg border border-outline-variant hover:bg-surface-container-high text-on-surface font-semibold text-sm flex items-center justify-center gap-1.5";
        } else {
            tabD.className = "h-10 rounded-lg border-2 border-primary bg-on-primary-container text-primary font-bold text-sm flex items-center justify-center gap-1.5";
            tabW.className = "h-10 rounded-lg border border-outline-variant hover:bg-surface-container-high text-on-surface font-semibold text-sm flex items-center justify-center gap-1.5";
        }
    }

    function submitCashTx() {
        let amtRaw = document.getElementById('txAmount').value;
        let amt = amtRaw.replace(/\D/g, "");
        if (!amt || parseInt(amt) <= 0) {
            alert("Vui lòng nhập số tiền hợp lệ lớn hơn 0.");
            return;
        }

        let note = document.getElementById('txNote').value.trim();

        let params = new URLSearchParams();
        params.append('type', currentTxType);
        params.append('amount', amt);
        params.append('note', note);

        fetch('${pageContext.request.contextPath}/shift/cash', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        })
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success') {
                // Update Expected Cash UI
                document.getElementById('expectedCashText').innerText = formatVND(data.expected_cash);

                // Add row to cash ledger on UI dynamically
                let txList = document.getElementById('ajaxTxList');
                let newRow = document.createElement('div');
                newRow.className = `flex justify-between font-medium \${currentTxType === 'DEPOSIT' ? 'text-tertiary' : 'text-error'}`;
                
                let txLabel = currentTxType === 'DEPOSIT' ? 'Nạp tiền lẻ (+)' : 'Rút tiền mặt (-)';
                let noteLabel = note ? ` <span class="text-xs text-outline font-normal">(\${note})</span>` : '';
                let txSign = currentTxType === 'DEPOSIT' ? '+' : '-';
                
                newRow.innerHTML = `
                    <span>\${txLabel}\${noteLabel}</span>
                    <span>\${txSign}\${formatVND(amt)}</span>
                `;
                txList.appendChild(newRow);

                closeCashTxModal();
            } else {
                alert(data.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert("Lỗi thực hiện giao dịch.");
        });
    }

    // Close Shift modal handlers
    function openCloseShiftModal() {
        document.getElementById('closeShiftModal').classList.remove('hidden');
        document.getElementById('closingCashInput').value = "";
    }

    function closeCloseShiftModal() {
        document.getElementById('closeShiftModal').classList.add('hidden');
    }

    // Handle discrepancy alerts if shift was closed and discrepancy exists (> 500,000đ)
    window.addEventListener('DOMContentLoaded', () => {
        // Only run if shift info is available
        <c:if test="${not empty activeShift}">
            let expectedCash = ${shiftSummary.expectedCash};
            let closingCash = ${not empty shiftSummary.closingCash ? shiftSummary.closingCash : -1};
            let status = '${shiftSummary.status}';

            if (status === 'CLOSED' && closingCash !== -1) {
                let diff = Math.abs(closingCash - expectedCash);
                if (diff > 500000) {
                    let alertCard = document.getElementById('cardSupportAlert');
                    if (alertCard) {
                        alertCard.className = "p-6 rounded-2xl border shadow-sm flex flex-col justify-between min-h-[130px] bg-error-container border-error text-on-error-container";
                        alertCard.innerHTML = `
                            <div>
                                <h4 class="text-xs font-bold uppercase tracking-wider text-error">CẢNH BÁO CHÊNH LỆCH QUỸ CA</h4>
                                <p class="text-xs mt-1.5 leading-snug">
                                    Phát hiện lệch két lớn hơn quy định: <b>\${formatVND(diff)}</b>.<br>
                                    Yêu cầu thủ quỹ liên hệ ngay với Trưởng ca/Quản lý chi nhánh để đối soát hóa đơn ca làm việc.
                                </p>
                            </div>
                        `;
                    }
                }
            }
        </c:if>
    });
</script>
</body>
</html>

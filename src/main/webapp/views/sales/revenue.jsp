<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
                <!DOCTYPE html>
                <html lang="vi">

                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Finora — Phân tích doanh thu</title>
                    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
                        rel="stylesheet">
                    <link
                        href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
                        rel="stylesheet">
                    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
                    <link href="${pageContext.request.contextPath}/assets/css/base.css" rel="stylesheet">
                    <link href="${pageContext.request.contextPath}/assets/css/sales.css?v=2" rel="stylesheet">
                    <script>
                        tailwind.config = { darkMode: "class", theme: { extend: { colors: { "secondary": "#b51a1b", "primary-fixed-dim": "#ffb3ac", "tertiary-fixed-dim": "#88d982", "on-tertiary-container": "#d8ffd0", "tertiary": "#11651d", "background": "#f8f9fa", "tertiary-fixed": "#a3f69c", "inverse-on-surface": "#f0f1f2", "error-container": "#ffdad6", "surface-dim": "#d9dadb", "on-background": "#191c1d", "on-surface": "#191c1d", "primary-fixed": "#ffdad6", "on-secondary-container": "#fffbff", "surface-container-lowest": "#ffffff", "surface-container-highest": "#e1e3e4", "surface-variant": "#e1e3e4", "surface-container-high": "#e7e8e9", "on-secondary": "#ffffff", "inverse-surface": "#2e3132", "on-tertiary": "#ffffff", "on-secondary-fixed-variant": "#93000b", "surface-tint": "#ba1a20", "surface": "#f8f9fa", "error": "#ba1a1a", "on-error-container": "#93000a", "primary-container": "#d32f2f", "surface-container-low": "#f3f4f5", "on-surface-variant": "#5b403d", "on-primary-fixed-variant": "#930010", "on-primary-container": "#fff2f0", "surface-container": "#edeeef", "surface-bright": "#f8f9fa", "on-error": "#ffffff", "on-tertiary-fixed": "#002204", "tertiary-container": "#307f34", "inverse-primary": "#ffb3ac", "on-tertiary-fixed-variant": "#005312", "on-primary-fixed": "#410003", "outline": "#8f6f6c", "secondary-fixed-dim": "#ffb4ab", "outline-variant": "#e4beba", "on-primary": "#ffffff", "secondary-fixed": "#ffdad6", "secondary-container": "#d93630", "on-secondary-fixed": "#410002", "primary": "#af101a" }, borderRadius: { DEFAULT: "0.25rem", lg: "0.5rem", xl: "0.75rem", full: "9999px" }, spacing: { "stack-lg": "24px", "container-padding": "32px", "stack-sm": "8px", "gutter": "24px", "section-gap": "48px", "unit": "8px", "stack-md": "16px" }, fontFamily: { "body-md": ["Inter"], "label-md": ["Inter"], "caption": ["Inter"], "headline-md": ["Inter"], "headline-lg": ["Inter"], "button-text": ["Inter"], "title-lg": ["Inter"], "display-lg": ["Inter"], "body-lg": ["Inter"] }, fontSize: { "body-md": ["16px", { lineHeight: "24px", fontWeight: "500" }], "label-md": ["14px", { lineHeight: "20px", fontWeight: "600" }], "caption": ["12px", { lineHeight: "16px", fontWeight: "400" }], "headline-md": ["24px", { lineHeight: "32px", fontWeight: "700" }], "headline-lg": ["32px", { lineHeight: "40px", letterSpacing: "-0.01em", fontWeight: "700" }], "button-text": ["16px", { lineHeight: "24px", fontWeight: "600" }], "title-lg": ["20px", { lineHeight: "28px", fontWeight: "600" }], "display-lg": ["48px", { lineHeight: "56px", letterSpacing: "-0.02em", fontWeight: "700" }], "body-lg": ["18px", { lineHeight: "26px", fontWeight: "500" }] } } } };
                    </script>
                    <style>
                        body {
                            font-family: 'Inter', sans-serif
                        }

                        .material-symbols-outlined {
                            font-variation-settings: 'FILL' 0, 'wght' 500, 'GRAD' 0, 'opsz' 24;
                            vertical-align: middle
                        }

                        .scrollbar-thin::-webkit-scrollbar {
                            width: 6px
                        }

                        .scrollbar-thin::-webkit-scrollbar-track {
                            background: transparent
                        }

                        .scrollbar-thin::-webkit-scrollbar-thumb {
                            background: #d9dadb;
                            border-radius: 999px
                        }
                    </style>
                </head>

                <body class="bg-background text-on-surface overflow-hidden h-screen">
                    <div class="flex h-screen w-screen pl-[var(--sidebar-width,260px)] pr-6">

                        <!-- Include POS Sidebar -->
                        <jsp:include page="/views/common/sidebar.jsp" />

                        <!-- Main Workspace -->
                        <div class="flex-1 flex flex-col min-w-0 h-screen relative">

                            <!-- Header (72px) -->
                            <header
                                class="h-[72px] bg-surface border-b border-outline-variant flex items-center px-6 gap-4 shrink-0 z-10">
                                <div class="flex items-center gap-2">
                                    <h1 class="text-title-lg font-bold text-primary">Phân tích Doanh thu</h1>

                                    <!-- Date Selector Overlay trigger -->
                                    <div class="relative cursor-pointer hover:bg-surface-container rounded-lg px-3 py-1.5 flex items-center gap-2 border border-outline-variant transition-colors"
                                        onclick="document.getElementById('datePickerInput').showPicker()">
                                        <span
                                            class="material-symbols-outlined text-[18px] text-primary">calendar_today</span>
                                        <span class="text-sm font-semibold text-on-surface"
                                            id="dateDisplayLabel">${selectedDateFormatted}</span>
                                        <span
                                            class="material-symbols-outlined text-[18px] text-outline">expand_more</span>
                                        <input type="date" id="datePickerInput" value="${selectedDate}"
                                            onchange="changeFilterDate(this.value)"
                                            class="absolute inset-0 opacity-0 cursor-pointer pointer-events-none">
                                    </div>
                                </div>

                                <jsp:include page="/common/header.jsp" />
                            </header>

                            <!-- Sub-Header / Filters Bar -->
                            <div
                                class="h-14 bg-surface border-b border-outline-variant px-6 flex items-center justify-between shrink-0">
                                <form id="filterForm" action="${pageContext.request.contextPath}/revenue" method="GET"
                                    class="flex items-center gap-4">
                                    <input type="hidden" name="date" id="formDateInput" value="${selectedDate}">
                                    <div class="flex items-center gap-2">
                                        <span class="text-xs font-bold text-on-surface-variant uppercase">Nhân
                                            viên:</span>
                                        <select name="empId" onchange="document.getElementById('filterForm').submit()"
                                            class="h-9 px-3 rounded-lg border border-outline-variant bg-surface-container-low text-sm focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none">
                                            <option value="0">Tất cả nhân viên</option>
                                            <c:forEach items="${employeeList}" var="e">
                                                <option value="${e.empId}" ${e.empId==selectedEmpId ? 'selected' : '' }>
                                                    ${e.fullName}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </form>
                                <div class="flex items-center gap-3">
                                    <button
                                        class="h-9 px-4 border border-outline-variant text-on-surface rounded-lg hover:bg-surface-container-high transition-colors font-semibold text-xs flex items-center gap-1.5">
                                        <span class="material-symbols-outlined text-[16px]">filter_list</span>
                                        <span>Lọc nâng cao</span>
                                    </button>
                                    <button
                                        class="h-9 px-4 border border-outline-variant text-on-surface rounded-lg hover:bg-surface-container-high transition-colors font-semibold text-xs flex items-center gap-1.5">
                                        <span class="material-symbols-outlined text-[16px]">download</span>
                                        <span>Xuất báo cáo</span>
                                    </button>
                                </div>
                            </div>

                            <!-- Scrollable Dashboard Content -->
                            <div class="flex-1 overflow-y-auto p-6 space-y-6 scrollbar-thin">

                                <!-- 5 KPI Cards Grid -->
                                <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
                                    <!-- 1. Total Revenue -->
                                    <div
                                        class="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between h-[120px] relative overflow-hidden">
                                        <div class="flex items-start justify-between">
                                            <span class="text-xs font-bold text-outline uppercase tracking-wider">Tổng
                                                doanh thu</span>
                                            <div
                                                class="w-8 h-8 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                                                <span class="material-symbols-outlined text-[18px]">payments</span>
                                            </div>
                                        </div>
                                        <div class="mt-2">
                                            <div class="text-[26px] font-bold text-primary leading-tight">
                                                <fmt:formatNumber value="${kpi.totalRevenue}" pattern="#,##0" />đ
                                            </div>
                                            <div
                                                class="flex items-center gap-1 mt-1 text-[11px] font-semibold ${kpi.totalRevenueChange >= 0 ? 'text-tertiary' : 'text-error'}">
                                                <span class="material-symbols-outlined text-xs">${kpi.totalRevenueChange
                                                    >= 0 ? 'trending_up' : 'trending_down'}</span>
                                                <span>
                                                    <fmt:formatNumber
                                                        value="${kpi.totalRevenueChange >= 0 ? kpi.totalRevenueChange : -kpi.totalRevenueChange}"
                                                        pattern="0.0" />%
                                                </span>
                                                <span class="text-outline font-normal">so với hôm qua</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 2. Cash Revenue -->
                                    <div
                                        class="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between h-[120px] relative overflow-hidden">
                                        <div class="flex items-start justify-between">
                                            <span class="text-xs font-bold text-outline uppercase tracking-wider">Tiền
                                                mặt</span>
                                            <div
                                                class="w-8 h-8 rounded-full bg-tertiary/10 text-tertiary flex items-center justify-center">
                                                <span class="material-symbols-outlined text-[18px]">payments</span>
                                            </div>
                                        </div>
                                        <div class="mt-2">
                                            <div class="text-[26px] font-bold text-primary leading-tight">
                                                <fmt:formatNumber value="${kpi.cashRevenue}" pattern="#,##0" />đ
                                            </div>
                                            <div
                                                class="flex items-center gap-1 mt-1 text-[11px] font-semibold ${kpi.cashRevenueChange >= 0 ? 'text-tertiary' : 'text-error'}">
                                                <span class="material-symbols-outlined text-xs">${kpi.cashRevenueChange
                                                    >= 0 ? 'trending_up' : 'trending_down'}</span>
                                                <span>
                                                    <fmt:formatNumber
                                                        value="${kpi.cashRevenueChange >= 0 ? kpi.cashRevenueChange : -kpi.cashRevenueChange}"
                                                        pattern="0.0" />%
                                                </span>
                                                <span class="text-outline font-normal">so với hôm qua</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 3. Bank Transfer Revenue -->
                                    <div
                                        class="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between h-[120px] relative overflow-hidden">
                                        <div class="flex items-start justify-between">
                                            <span class="text-xs font-bold text-outline uppercase tracking-wider">Chuyển
                                                khoản</span>
                                            <div
                                                class="w-8 h-8 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                                                <span class="material-symbols-outlined text-[18px]">credit_card</span>
                                            </div>
                                        </div>
                                        <div class="mt-2">
                                            <div class="text-[26px] font-bold text-primary leading-tight">
                                                <fmt:formatNumber value="${kpi.bankRevenue}" pattern="#,##0" />đ
                                            </div>
                                            <div
                                                class="flex items-center gap-1 mt-1 text-[11px] font-semibold ${kpi.bankRevenueChange >= 0 ? 'text-tertiary' : 'text-error'}">
                                                <span class="material-symbols-outlined text-xs">${kpi.bankRevenueChange
                                                    >= 0 ? 'trending_up' : 'trending_down'}</span>
                                                <span>
                                                    <fmt:formatNumber
                                                        value="${kpi.bankRevenueChange >= 0 ? kpi.bankRevenueChange : -kpi.bankRevenueChange}"
                                                        pattern="0.0" />%
                                                </span>
                                                <span class="text-outline font-normal">so với hôm qua</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 4. Total Orders -->
                                    <div
                                        class="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between h-[120px] relative overflow-hidden">
                                        <div class="flex items-start justify-between">
                                            <span class="text-xs font-bold text-outline uppercase tracking-wider">Tổng
                                                đơn hàng</span>
                                            <div
                                                class="w-8 h-8 rounded-full bg-surface-container-highest text-on-surface-variant flex items-center justify-center">
                                                <span class="material-symbols-outlined text-[18px]">receipt_long</span>
                                            </div>
                                        </div>
                                        <div class="mt-2">
                                            <div class="text-[26px] font-bold text-primary leading-tight">
                                                ${kpi.totalOrders} đơn</div>
                                            <div
                                                class="flex items-center gap-1 mt-1 text-[11px] font-semibold ${kpi.totalOrdersChange >= 0 ? 'text-tertiary' : 'text-error'}">
                                                <span class="material-symbols-outlined text-xs">${kpi.totalOrdersChange
                                                    >= 0 ? 'trending_up' : 'trending_down'}</span>
                                                <span>
                                                    <fmt:formatNumber
                                                        value="${kpi.totalOrdersChange >= 0 ? kpi.totalOrdersChange : -kpi.totalOrdersChange}"
                                                        pattern="0.0" />%
                                                </span>
                                                <span class="text-outline font-normal">so với hôm qua</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 5. Average Order Value (AOV) -->
                                    <div
                                        class="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant shadow-sm flex flex-col justify-between h-[120px] relative overflow-hidden">
                                        <div class="flex items-start justify-between">
                                            <span class="text-xs font-bold text-outline uppercase tracking-wider">AOV
                                                (Trung bình/đơn)</span>
                                            <div
                                                class="w-8 h-8 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                                                <span class="material-symbols-outlined text-[18px]">equalizer</span>
                                            </div>
                                        </div>
                                        <div class="mt-2">
                                            <div class="text-[26px] font-bold text-primary leading-tight">
                                                <fmt:formatNumber value="${kpi.aov}" pattern="#,##0" />đ
                                            </div>
                                            <div
                                                class="flex items-center gap-1 mt-1 text-[11px] font-semibold ${kpi.aovChange >= 0 ? 'text-tertiary' : 'text-error'}">
                                                <span class="material-symbols-outlined text-xs">${kpi.aovChange >= 0 ?
                                                    'trending_up' : 'trending_down'}</span>
                                                <span>
                                                    <fmt:formatNumber
                                                        value="${kpi.aovChange >= 0 ? kpi.aovChange : -kpi.aovChange}"
                                                        pattern="0.0" />%
                                                </span>
                                                <span class="text-outline font-normal">so với hôm qua</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Charts Section (Doanh thu theo giờ + Donut) -->
                                <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

                                    <!-- Hourly Bar Chart (Col span 2) -->
                                    <div
                                        class="lg:col-span-2 bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm flex flex-col h-[400px]">
                                        <div
                                            class="flex items-center justify-between border-b border-outline-variant/60 pb-4 mb-6 shrink-0">
                                            <h3 class="text-label-md font-bold text-on-surface uppercase">Doanh thu theo
                                                giờ</h3>
                                            <div class="flex items-center gap-4 text-xs font-semibold">
                                                <div class="flex items-center gap-1.5">
                                                    <div class="w-3 h-3 bg-primary rounded"></div>
                                                    <span>Hôm nay</span>
                                                </div>
                                                <div class="flex items-center gap-1.5">
                                                    <div class="w-3 h-3 bg-primary/20 rounded"></div>
                                                    <span>Hôm qua</span>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- CSS Columns Hourly Representation -->
                                        <div class="flex-1 flex items-end justify-between gap-2 px-2"
                                            id="hourlyChartContainer">
                                            <!-- Hour columns are dynamically rendered by JS heights below -->
                                            <c:forEach items="${hourlyLabels}" var="label" varStatus="st">
                                                <div class="flex-1 flex flex-col items-center group h-full justify-end">
                                                    <div
                                                        class="w-full flex items-end justify-center gap-1 h-[80%] relative">
                                                        <!-- Tooltip -->
                                                        <div
                                                            class="absolute bottom-full mb-2 bg-inverse-surface text-inverse-on-surface text-[10px] py-1 px-2 rounded opacity-0 group-hover:opacity-100 transition-opacity z-10 whitespace-nowrap shadow pointer-events-none">
                                                            Hôm nay:
                                                            <fmt:formatNumber value="${hourlyToday[st.index]}"
                                                                pattern="#,##0" />đ<br>
                                                            Hôm qua:
                                                            <fmt:formatNumber value="${hourlyYesterday[st.index]}"
                                                                pattern="#,##0" />đ
                                                        </div>
                                                        <!-- Yesterday Col (Faded Red) -->
                                                        <div class="w-3 bg-primary/20 rounded-t transition-all duration-500"
                                                            style="height: 0%" id="bar-yesterday-${st.index}"></div>
                                                        <!-- Today Col (Solid Red) -->
                                                        <div class="w-3 bg-primary rounded-t transition-all duration-500"
                                                            style="height: 0%" id="bar-today-${st.index}"></div>
                                                    </div>
                                                    <span
                                                        class="text-[10px] text-outline mt-2 font-medium">${label}</span>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>

                                    <!-- Donut Chart (Col span 1) -->
                                    <div
                                        class="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm flex flex-col items-center justify-between h-[400px]">
                                        <div class="w-full border-b border-outline-variant/60 pb-4 shrink-0 text-left">
                                            <h3 class="text-label-md font-bold text-on-surface uppercase">Phương thức
                                                thanh toán</h3>
                                        </div>

                                        <!-- SVG Donut -->
                                        <div class="relative flex items-center justify-center flex-1 my-4">
                                            <svg width="180" height="180" viewBox="0 0 180 180"
                                                class="transform -rotate-90">
                                                <!-- Background base circle -->
                                                <circle cx="90" cy="90" r="70" fill="transparent" stroke="#f3f4f5"
                                                    stroke-width="18" />
                                                <!-- Cash Circle (Primary #af101a) -->
                                                <circle id="donut-cash" cx="90" cy="90" r="70" fill="transparent"
                                                    stroke="#af101a" stroke-width="18" stroke-dasharray="439.8"
                                                    stroke-dashoffset="439.8"
                                                    class="transition-all duration-1000 ease-out" />
                                                <!-- Bank Transfer Circle (Tertiary #11651d) -->
                                                <circle id="donut-bank" cx="90" cy="90" r="70" fill="transparent"
                                                    stroke="#11651d" stroke-width="18" stroke-dasharray="439.8"
                                                    stroke-dashoffset="439.8"
                                                    class="transition-all duration-1000 ease-out" />
                                            </svg>
                                            <div class="absolute text-center leading-none">
                                                <span class="text-caption text-outline uppercase block">100%</span>
                                                <span class="text-label-md font-bold text-on-surface mt-1 block">Tổng
                                                    cộng</span>
                                            </div>
                                        </div>

                                        <!-- Donut Legend -->
                                        <div
                                            class="w-full grid grid-cols-2 gap-4 border-t border-outline-variant/40 pt-4 shrink-0 text-sm font-semibold">
                                            <div class="flex items-center gap-2">
                                                <div class="w-3 h-3 bg-primary rounded"></div>
                                                <div class="leading-tight text-left">
                                                    <div class="text-[11px] text-outline font-medium">Tiền mặt</div>
                                                    <div class="text-on-surface font-bold mt-0.5">
                                                        <fmt:formatNumber value="${cashPct}" pattern="0" />%
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="flex items-center gap-2">
                                                <div class="w-3 h-3 bg-tertiary rounded"></div>
                                                <div class="leading-tight text-left">
                                                    <div class="text-[11px] text-outline font-medium">Chuyển khoản</div>
                                                    <div class="text-on-surface font-bold mt-0.5">
                                                        <fmt:formatNumber value="${bankPct}" pattern="0" />%
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                </div>

                                <!-- Bottom Lists (Top Products + Recent Transactions) -->
                                <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

                                    <!-- Top Products List -->
                                    <div
                                        class="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm flex flex-col min-h-[360px]">
                                        <div
                                            class="border-b border-outline-variant/60 pb-4 mb-4 flex items-center justify-between">
                                            <h3 class="text-label-md font-bold text-on-surface uppercase">Sản phẩm bán
                                                chạy</h3>
                                            <span class="text-[11px] text-outline font-medium">Sắp xếp theo số lượng
                                                bán</span>
                                        </div>
                                        <div class="flex-1 divide-y divide-outline-variant/40">
                                            <c:forEach items="${topProducts}" var="prod">
                                                <div class="py-3.5 flex items-center gap-4">
                                                    <!-- Dummy image frame -->
                                                    <div
                                                        class="w-11 h-11 rounded-lg bg-surface-container flex items-center justify-center font-bold text-on-surface-variant text-base select-none shrink-0 border border-outline-variant/30">
                                                        ${fn:substring(prod.productName, 0, 1)}
                                                    </div>
                                                    <div class="flex-1 leading-tight text-left">
                                                        <h4 class="text-sm font-semibold text-on-surface">
                                                            ${prod.productName}</h4>
                                                        <p class="text-[10px] text-outline mt-0.5">Barcode:
                                                            ${prod.productCode}</p>
                                                    </div>
                                                    <div class="text-right shrink-0">
                                                        <div class="text-sm font-bold text-primary">
                                                            <fmt:formatNumber value="${prod.totalRev}"
                                                                pattern="#,##0" />đ
                                                        </div>
                                                        <div class="text-[10px] text-outline font-semibold mt-0.5">Đã
                                                            bán: ${prod.totalQty} chiếc</div>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                            <c:if test="${empty topProducts}">
                                                <div class="py-12 text-center text-on-surface-variant">Không có sản phẩm
                                                    nào bán chạy trong ngày này.</div>
                                            </c:if>
                                        </div>
                                    </div>

                                    <!-- Recent Transactions List -->
                                    <div
                                        class="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm flex flex-col min-h-[360px]">
                                        <div
                                            class="border-b border-outline-variant/60 pb-4 mb-4 flex items-center justify-between">
                                            <h3 class="text-label-md font-bold text-on-surface uppercase">Giao dịch gần
                                                đây</h3>
                                            <span class="text-[11px] text-outline font-medium">Xem thêm ở Đơn
                                                hàng</span>
                                        </div>
                                        <div class="flex-1 divide-y divide-outline-variant/40">
                                            <c:forEach items="${recentTransactions}" var="tx">
                                                <div class="py-3 flex items-center justify-between gap-4">
                                                    <div class="text-left leading-tight">
                                                        <div class="flex items-center gap-2">
                                                            <span
                                                                class="text-sm font-bold text-primary">${tx.orderCode}</span>
                                                            <span
                                                                class="text-[10px] text-outline font-medium">
                                                                <c:choose>
                                                                    <c:when test="${fn:length(tx.createdAt) >= 19}">
                                                                        <fmt:parseDate value="${fn:substring(tx.createdAt, 0, 19)}" pattern="yyyy-MM-dd HH:mm:ss" var="txCreated"/>
                                                                        <fmt:formatDate value="${txCreated}" pattern="dd/MM/yyyy HH:mm"/>
                                                                    </c:when>
                                                                    <c:otherwise>—</c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                        </div>
                                                        <div class="text-xs text-on-surface-variant mt-1">
                                                            Khách hàng: <span class="font-medium text-on-surface">${not
                                                                empty tx.customerName ? tx.customerName : 'Khách vãng
                                                                lai'}</span>
                                                            <span class="mx-1.5 text-outline-variant">|</span>
                                                            ${tx.totalItems} SP
                                                        </div>
                                                    </div>
                                                    <div class="text-right shrink-0">
                                                        <div class="text-sm font-bold text-primary">
                                                            <fmt:formatNumber value="${tx.totalAmount}"
                                                                pattern="#,##0" />đ
                                                        </div>
                                                        <div class="flex items-center gap-1.5 justify-end mt-1">
                                                            <!-- Payment method badge -->
                                                            <span
                                                                class="text-[9px] font-semibold bg-surface-container text-outline px-1.5 py-0.5 rounded">
                                                                ${tx.paymentMethod == 'CASH' ? 'TIỀN MẶT' : 'CHUYỂN
                                                                KHOẢN'}
                                                            </span>
                                                            <!-- Status badge -->
                                                            <c:choose>
                                                                <c:when test="${tx.status == 'COMPLETED'}">
                                                                    <span
                                                                        class="text-[9px] font-bold bg-tertiary-fixed text-on-tertiary-fixed px-1.5 py-0.5 rounded">Hoàn thành</span>
                                                                </c:when>
                                                                <c:when test="${tx.status == 'CANCELLED'}">
                                                                    <span
                                                                        class="text-[9px] font-bold bg-error-container text-on-error-container px-1.5 py-0.5 rounded">Đã hủy</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span
                                                                        class="text-[9px] font-bold bg-surface-container-highest text-on-surface px-1.5 py-0.5 rounded">Chờ thanh toán</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                            <c:if test="${empty recentTransactions}">
                                                <div class="py-12 text-center text-on-surface-variant">Không có giao
                                                    dịch nào được ghi nhận gần đây.</div>
                                            </c:if>
                                        </div>
                                    </div>

                                </div>

                            </div>

                        </div>
                    </div>

                    <script>
                        function changeFilterDate(newDate) {
                            document.getElementById('formDateInput').value = newDate;
                            document.getElementById('filterForm').submit();
                        }

                        // Chart Animation / Calculations
                        window.addEventListener('DOMContentLoaded', () => {
                            // --- 1. HOURLY BAR CHART HEIGHTS ---
                            const todayData = [
                                <c:forEach items="${hourlyToday}" var="val" varStatus="st">
                                    ${val}${not st.last ? ',' : ''}
                                </c:forEach>
                            ];
                            const yesterdayData = [
                                <c:forEach items="${hourlyYesterday}" var="val" varStatus="st">
                                    ${val}${not st.last ? ',' : ''}
                                </c:forEach>
                            ];

                            // Find max value to normalize heights
                            let maxVal = 0;
                            todayData.forEach(v => { if (v > maxVal) maxVal = v; });
                            yesterdayData.forEach(v => { if (v > maxVal) maxVal = v; });

                            if (maxVal > 0) {
                                todayData.forEach((val, idx) => {
                                    let todayHeight = (val / maxVal) * 100;
                                    let yesterdayHeight = (yesterdayData[idx] / maxVal) * 100;

                                    // Set styles dynamically with a small delay for fade in animation
                                    setTimeout(() => {
                                        let barToday = document.getElementById('bar-today-' + idx);
                                        let barYesterday = document.getElementById('bar-yesterday-' + idx);
                                        if (barToday) barToday.style.height = todayHeight + '%';
                                        if (barYesterday) barYesterday.style.height = yesterdayHeight + '%';
                                    }, 100 + (idx * 30));
                                });
                            }

                            // --- 2. DONUT CHART SVG RENDERING ---
                            const cashPct = ${ cashPct };
                            const bankPct = ${ bankPct };

                            // Circumference of radius 70 is 2 * PI * 70 = 439.82
                            const circleLength = 439.8;

                            // Cash circle starts at rotation -90deg. Fills up to cashPct length.
                            let cashOffset = circleLength * (1 - cashPct / 100);
                            let cashCircle = document.getElementById('donut-cash');
                            if (cashCircle) {
                                cashCircle.style.strokeDashoffset = cashOffset;
                            }

                            // Bank circle needs to sit right after the cash circle. 
                            // We set stroke-dashoffset to fill bankPct, and rotate it.
                            let bankOffset = circleLength * (1 - bankPct / 100);
                            let bankCircle = document.getElementById('donut-bank');
                            if (bankCircle) {
                                bankCircle.style.strokeDashoffset = bankOffset;

                                // Rotate the bank circle by (cashPct * 3.6deg) so it continues where cash ended
                                let rotateAngle = -90 + (cashPct * 3.6);
                                bankCircle.setAttribute('transform', `rotate(${rotateAngle} 90 90)`);
                            }
                        });
                    </script>
                    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                </body>

                </html>
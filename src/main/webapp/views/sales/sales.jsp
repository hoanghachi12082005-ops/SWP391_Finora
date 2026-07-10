<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="jakarta.tags.core" %>
        <%@taglib prefix="fn" uri="jakarta.tags.functions" %>
            <%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
                <!DOCTYPE html>
                <html lang="vi">

                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Finora POS — Bán hàng</title>
                    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
                        rel="stylesheet">
                    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
                    <link
                        href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
                        rel="stylesheet">
                    <link href="${pageContext.request.contextPath}/assets/css/base.css" rel="stylesheet">
                    <script>
                        tailwind.config = { darkMode: "class", theme: { extend: { colors: { "secondary": "#b51a1b", "primary-fixed-dim": "#ffb3ac", "tertiary-fixed-dim": "#88d982", "on-tertiary-container": "#d8ffd0", "tertiary": "#11651d", "background": "#f8f9fa", "tertiary-fixed": "#a3f69c", "inverse-on-surface": "#f0f1f2", "error-container": "#ffdad6", "surface-dim": "#d9dadb", "on-background": "#191c1d", "on-surface": "#191c1d", "primary-fixed": "#ffdad6", "on-secondary-container": "#fffbff", "surface-container-lowest": "#ffffff", "surface-container-highest": "#e1e3e4", "surface-variant": "#e1e3e4", "surface-container-high": "#e7e8e9", "on-secondary": "#ffffff", "inverse-surface": "#2e3132", "on-tertiary": "#ffffff", "on-secondary-fixed-variant": "#93000b", "surface-tint": "#ba1a20", "surface": "#f8f9fa", "error": "#ba1a1a", "on-error-container": "#93000a", "primary-container": "#d32f2f", "surface-container-low": "#f3f4f5", "on-surface-variant": "#5b403d", "on-primary-fixed-variant": "#930010", "on-primary-container": "#fff2f0", "surface-container": "#edeeef", "surface-bright": "#f8f9fa", "on-error": "#ffffff", "on-tertiary-fixed": "#002204", "tertiary-container": "#307f34", "inverse-primary": "#ffb3ac", "on-tertiary-fixed-variant": "#005312", "on-primary-fixed": "#410003", "outline": "#8f6f6c", "secondary-fixed-dim": "#ffb4ab", "outline-variant": "#e4beba", "on-primary": "#ffffff", "secondary-fixed": "#ffdad6", "secondary-container": "#d93630", "on-secondary-fixed": "#410002", "primary": "#af101a" }, borderRadius: { DEFAULT: "0.25rem", lg: "0.5rem", xl: "0.75rem", full: "9999px" }, spacing: { "stack-lg": "24px", "container-padding": "32px", "stack-sm": "8px", "gutter": "24px", "section-gap": "48px", "unit": "8px", "stack-md": "16px" }, fontFamily: { "body-md": ["Inter"], "label-md": ["Inter"], "caption": ["Inter"], "headline-md": ["Inter"], "headline-lg": ["Inter"], "button-text": ["Inter"], "title-lg": ["Inter"], "display-lg": ["Inter"], "body-lg": ["Inter"] }, fontSize: { "body-md": ["16px", { lineHeight: "24px", fontWeight: "500" }], "label-md": ["14px", { lineHeight: "20px", fontWeight: "600" }], "caption": ["12px", { lineHeight: "16px", fontWeight: "400" }], "headline-md": ["24px", { lineHeight: "32px", fontWeight: "700" }], "headline-lg": ["32px", { lineHeight: "40px", letterSpacing: "-0.01em", fontWeight: "700" }], "button-text": ["16px", { lineHeight: "24px", fontWeight: "600" }], "title-lg": ["20px", { lineHeight: "28px", fontWeight: "600" }], "display-lg": ["48px", { lineHeight: "56px", letterSpacing: "-0.02em", fontWeight: "700" }], "body-lg": ["18px", { lineHeight: "26px", fontWeight: "500" }] } } } };
                    </script>
                    <link href="${pageContext.request.contextPath}/assets/css/sales.css" rel="stylesheet">
                </head>

                <body class="bg-background text-on-surface overflow-hidden h-screen">
                    <div class="flex h-screen pl-[var(--sidebar-width,260px)] pr-6">

                        <!-- ═══════════════ SIDEBAR POS ═══════════════ -->
                        <jsp:include page="/views/common/sidebar-pos.jsp" />


                        <!-- ═══════════════ MAIN AREA ═══════════════ -->
                        <div class="flex-1 flex flex-col min-w-0">

                            <!-- ─── HEADER (72px) ─── -->
                            <header
                                class="h-[72px] bg-surface border-b border-outline-variant flex items-center px-6 gap-4 shrink-0 z-10">
                                <div class="flex-1 relative" id="searchContainer">
                                    <div
                                        class="flex items-center bg-surface-container-low rounded-xl px-4 h-11 gap-3 border border-transparent focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/10 transition-all">
                                        <span class="material-symbols-outlined text-outline text-[20px]">search</span>
                                        <input id="searchInput" type="text"
                                            placeholder="Nhập hoặc quét mã vạch sản phẩm..."
                                            class="bg-transparent outline-none flex-1 text-body-md placeholder:text-outline"
                                            autocomplete="off">
                                    </div>
                                    <div id="searchDropdown"
                                        class="hidden absolute top-full left-0 right-0 mt-1 bg-surface-container-lowest rounded-xl shadow-xl border border-outline-variant search-dropdown z-50 animate-fadeIn">
                                    </div>
                                </div>
                                <button onclick="newTab()"
                                    class="h-11 px-5 bg-primary text-on-primary rounded-xl font-button-text flex items-center gap-2 hover:bg-secondary transition-colors shadow-sm">
                                    <span class="material-symbols-outlined text-[20px]">add</span>
                                    <span>Thêm đơn mới</span>
                                </button>
                                <button
                                    class="w-11 h-11 rounded-xl bg-surface-container-low flex items-center justify-center text-on-surface-variant hover:bg-surface-container-high transition-colors relative">
                                    <span class="material-symbols-outlined text-[20px]">notifications</span>
                                </button>
                                <div class="flex items-center gap-3 pl-3 border-l border-outline-variant">
                                    <div
                                        class="w-10 h-10 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center font-bold text-sm">
                                        ${fn:substring(sessionScope.employee.fullName, 0, 1)}
                                    </div>
                                    <div class="leading-tight">
                                        <div class="text-label-md">${sessionScope.employee.fullName}</div>
                                        <div class="text-caption text-outline">Thu ngân</div>
                                    </div>
                                </div>
                            </header>

                            <!-- ─── TAB ROW ─── -->
                            <div id="tabRowContainer"
                                class="h-11 bg-surface border-b border-outline-variant flex items-end px-6 gap-1 shrink-0">
                                <c:forEach items="${sessionScope.cartTabs}" var="tabEntry">
                                    <button onclick="switchTab(${tabEntry.key})"
                                        class="px-5 pb-2.5 pt-2 text-label-md transition-all ${tabEntry.key == sessionScope.activeTabId ? 'text-primary border-b-2 border-primary font-bold' : 'text-on-surface-variant hover:bg-surface-container-low'}">
                                        Đơn ${tabEntry.key}
                                    </button>
                                </c:forEach>
                            </div>

                            <!-- ─── CONTENT SPLIT ─── -->
                            <div class="flex flex-1 overflow-hidden">

                                <!-- ═══ LEFT: ORDER TABLE & PRODUCT GRID ═══ -->
                                <div class="flex-1 flex flex-col overflow-hidden">
                                    <!-- Cart Table Area (Top 55%) -->
                                    <div
                                        class="flex-[11] flex flex-col min-h-0 border-b border-outline-variant/60 overflow-hidden">
                                        <!-- Empty State -->
                                        <div id="emptyCartState"
                                            class="flex-1 flex flex-col items-center justify-center text-outline gap-3">
                                            <span
                                                class="material-symbols-outlined text-[80px] opacity-40">barcode_scanner</span>
                                            <h3 class="text-title-lg text-on-surface font-semibold">Chưa có sản phẩm nào
                                            </h3>
                                            <p class="text-body-md text-outline max-w-sm text-center">Nhập mã vạch hoặc
                                                tên sản phẩm vào thanh tìm kiếm phía trên để thêm vào đơn hàng.</p>
                                        </div>
                                        <!-- Cart Table -->
                                        <div id="cartTableWrapper" class="hidden flex-1 overflow-auto">
                                            <table class="w-full">
                                                <thead class="bg-surface-container-low sticky top-0 z-[5]">
                                                    <tr class="text-label-md text-on-surface-variant">
                                                        <th class="py-3 px-4 text-left w-12">#</th>
                                                        <th class="py-3 px-4 text-left">Sản phẩm</th>
                                                        <th class="py-3 px-4 text-right w-32">Đơn giá</th>
                                                        <th class="py-3 px-4 text-center w-40">Số lượng</th>
                                                        <th class="py-3 px-4 text-right w-36">Thành tiền</th>
                                                        <th class="py-3 px-4 text-center w-16"></th>
                                                    </tr>
                                                </thead>
                                                <tbody id="cartTableBody" class="divide-y divide-outline-variant/50">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>

                                    <!-- Quick Product Select Area (Bottom 45%) -->
                                    <div
                                        class="flex-[9] flex flex-col min-h-0 bg-surface-container-low/40 p-4 overflow-hidden">
                                        <div class="flex items-center justify-between mb-3 shrink-0">
                                            <h4 class="text-label-md font-bold flex items-center gap-1.5 text-primary">
                                                <span class="material-symbols-outlined text-[18px]">grid_view</span>
                                                <span>Chọn nhanh sản phẩm từ kho</span>
                                            </h4>
                                            <span class="text-caption text-outline">Tồn kho cập nhật thực tế</span>
                                        </div>
                                        <div class="flex-1 overflow-y-auto scrollbar-thin">
                                            <div
                                                class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-3">
                                                <c:forEach items="${productList}" var="p">
                                                    <c:choose>
                                                        <c:when test="${p.quantityInStock <= 0}">
                                                            <button disabled
                                                                class="bg-gray-100 border border-outline-variant/40 rounded-xl p-3 text-left flex flex-col justify-between h-[105px] opacity-50 cursor-not-allowed shadow-none">
                                                                <div class="w-full">
                                                                    <div
                                                                        class="text-[11px] text-outline font-medium truncate mb-0.5">
                                                                        ${p.productCodebar}</div>
                                                                    <div
                                                                        class="text-caption text-on-surface/60 font-semibold line-clamp-2 leading-tight">
                                                                        ${p.productName}</div>
                                                                </div>
                                                                <div
                                                                    class="w-full flex items-end justify-between mt-2 pt-1 border-t border-dashed border-outline-variant/40">
                                                                    <span
                                                                        class="text-[11px] text-error font-medium">Kho:
                                                                        0</span>
                                                                    <span class="text-caption font-bold text-gray-400">
                                                                        <fmt:formatNumber value="${p.sellingPrice}"
                                                                            type="number" maxFractionDigits="0" /> ₫
                                                                    </span>
                                                                </div>
                                                            </button>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <button onclick="addToCart(${p.productId}, null)"
                                                                class="bg-white hover:bg-primary-container/10 border border-outline-variant/60 rounded-xl p-3 text-left transition-all hover:border-primary flex flex-col justify-between h-[105px] group shadow-sm hover:shadow">
                                                                <div class="w-full">
                                                                    <div
                                                                        class="text-[11px] text-outline font-medium truncate mb-0.5">
                                                                        ${p.productCodebar}</div>
                                                                    <div
                                                                        class="text-caption text-on-surface font-semibold line-clamp-2 leading-tight group-hover:text-primary transition-colors">
                                                                        ${p.productName}</div>
                                                                </div>
                                                                <div
                                                                    class="w-full flex items-end justify-between mt-2 pt-1 border-t border-dashed border-outline-variant/40">
                                                                    <span class="text-[11px] text-outline">Kho:
                                                                        ${p.quantityInStock}</span>
                                                                    <span class="text-caption font-bold text-primary">
                                                                        <fmt:formatNumber value="${p.sellingPrice}"
                                                                            type="number" maxFractionDigits="0" /> ₫
                                                                    </span>
                                                                </div>
                                                            </button>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- ═══ RIGHT: CART PANEL (340px) ═══ -->
                                <aside
                                    class="w-[340px] bg-white border-l border-outline-variant flex flex-col shrink-0">

                                    <!-- Customer Section -->
                                    <div class="p-3 border-b border-outline-variant">
                                        <div class="flex items-center gap-2.5">
                                            <div
                                                class="w-8 h-8 rounded-full bg-surface-container-high flex items-center justify-center text-on-surface-variant">
                                                <span class="material-symbols-outlined text-[18px]">person</span>
                                            </div>
                                            <div class="flex-1 min-w-0">
                                                <div class="text-[11px] text-outline leading-tight">Khách hàng</div>
                                                <div id="selectedCustomerName"
                                                    class="text-label-md text-on-surface truncate leading-tight">Khách vãng lai</div>
                                                <div id="customerPointsDisplay" class="text-caption text-primary hidden mt-0.5">
                                                    <span class="material-symbols-outlined text-[12px] align-text-bottom">stars</span>
                                                    Điểm khả dụng: <span id="customerPointsValue">0</span>
                                                </div>
                                            </div>
                                            <button onclick="openCustomerModal()"
                                                class="text-primary text-label-md hover:underline">Thay đổi</button>
                                        </div>
                                        <input type="hidden" id="selectedCustomerId" value="">
                                    </div>

                                    <!-- Summary Section -->
                                    <div class="flex-1 overflow-y-auto scrollbar-thin p-3 flex flex-col justify-between text-label-md">
                                        <!-- Top Details -->
                                        <div class="space-y-2">
                                            <div class="flex justify-between">
                                                <span class="text-outline">Số lượng sản phẩm</span>
                                                <span id="summaryItemCount" class="font-semibold">0</span>
                                            </div>
                                            <div class="flex justify-between">
                                                <span class="text-outline">Tổng tiền hàng</span>
                                                <span id="summarySubtotal" class="font-semibold">0 ₫</span>
                                            </div>
                                            <div class="flex justify-between">
                                                <span class="text-outline">Chiết khấu đơn hàng</span>
                                                <span id="summaryDiscount" class="text-error font-semibold">0 ₫</span>
                                            </div>
                                            <div class="flex justify-between">
                                                <span class="text-outline">Thuế (VAT 8%)</span>
                                                <span id="summaryVat" class="font-semibold">0 ₫</span>
                                            </div>

                                            <!-- Voucher -->
                                            <div class="pt-2 border-t border-outline-variant">
                                                <div class="flex items-center gap-2 relative">
                                                    <span
                                                        class="material-symbols-outlined text-outline text-[16px] absolute left-2.5 top-1/2 -translate-y-1/2">sell</span>
                                                    <select id="voucherSelect" onchange="changeVoucher(this.value)"
                                                        class="w-full text-label-md bg-surface-container-low rounded-lg pl-8 pr-3 py-1.5 border border-transparent focus:border-primary focus:ring-1 focus:ring-primary/10 outline-none appearance-none cursor-pointer">
                                                        <option value="0">-- Chọn mã giảm giá --</option>
                                                        <c:forEach items="${vouchers}" var="v">
                                                            <option value="${v.voucherId}">
                                                                ${v.voucherCode} - ${v.voucherName}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                                <div id="voucherMessage" class="hidden text-caption mt-1 px-6"></div>
                                            </div>
                                        </div>

                                        <!-- Bottom Details & Notes -->
                                        <div class="space-y-2 mt-4">
                                            <!-- Grand Total -->
                                            <div class="pt-2 border-t border-outline-variant">
                                                <div class="flex justify-between items-center">
                                                    <span class="text-body-lg font-bold">Tổng cộng</span>
                                                    <span id="summaryTotal"
                                                        class="text-headline-lg text-primary font-bold leading-none">0 <span
                                                            class="text-title-lg">₫</span></span>
                                                </div>
                                            </div>

                                            <!-- Notes -->
                                            <div class="pt-1">
                                                <textarea id="orderNotes" rows="1" placeholder="Ghi chú đơn hàng..."
                                                    class="w-full text-label-md bg-surface-container-low rounded-lg px-3 py-1.5 border border-transparent focus:border-primary focus:ring-1 focus:ring-primary/10 outline-none resize-none"></textarea>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Payment Buttons (sticky bottom) -->
                                    <div class="bg-surface-container-low border-t border-outline-variant p-3 space-y-2">
                                        <!-- Payment Method Toggle -->
                                        <div class="flex gap-2">
                                            <button id="btnCash" onclick="selectPayMethod('CASH')"
                                                class="flex-1 flex items-center justify-center gap-1.5 py-1.5 rounded-lg border-2 border-primary ring-2 ring-primary/10 text-primary bg-white font-label-md transition-all text-sm">
                                                <span class="material-symbols-outlined text-[16px]">payments</span> Tiền
                                                mặt
                                            </button>
                                            <button id="btnBank" onclick="selectPayMethod('BANK_TRANSFER')"
                                                class="flex-1 flex items-center justify-center gap-1.5 py-1.5 rounded-lg border-2 border-outline-variant text-on-surface-variant bg-white font-label-md transition-all hover:border-outline text-sm">
                                                <span class="material-symbols-outlined text-[16px]">credit_card</span>
                                                Thẻ/CK
                                            </button>
                                        </div>
                                        <!-- Checkout Button -->
                                        <button onclick="openPaymentModal()" id="btnCheckout"
                                            class="w-full h-11 bg-primary text-on-primary rounded-lg font-button-text text-sm flex items-center justify-center gap-2 hover:bg-secondary transition-colors shadow-md">
                                            <span
                                                class="material-symbols-outlined text-[20px]">shopping_cart_checkout</span>
                                            THANH TOÁN
                                        </button>
                                        <!-- Small Actions -->
                                        <div class="flex gap-1 text-[11px]">
                                            <button onclick="holdOrder()"
                                                class="flex-1 py-1 rounded bg-white border border-outline-variant/30 text-on-surface-variant hover:bg-surface-container-high transition-colors font-semibold">LƯU
                                                TẠM</button>
                                            <button onclick="printPreview()"
                                                class="flex-1 py-1 rounded bg-white border border-outline-variant/30 text-on-surface-variant hover:bg-surface-container-high transition-colors font-semibold">IN
                                                THỬ</button>
                                            <button onclick="cancelOrder()"
                                                class="flex-1 py-1 rounded bg-error-container/20 border border-error/20 text-error hover:bg-error-container transition-colors font-semibold">HUỶ
                                                ĐƠN</button>
                                        </div>
                                    </div>
                                </aside>
                            </div>
                        </div>
                    </div>

                    <!-- ═══════════════════════════════════════════════════════ -->
                    <!-- ═══════════════ PAYMENT MODAL ═════════════════════════ -->
                    <!-- ═══════════════════════════════════════════════════════ -->
                    <div id="paymentModal"
                        class="hidden fixed inset-0 z-50 modal-blur flex items-center justify-center">
                        <div
                            class="bg-surface-container-lowest rounded-xl shadow-2xl w-[900px] max-h-[90vh] flex flex-col animate-fadeIn">
                            <!-- Header -->
                            <div class="flex items-center justify-between px-6 py-4 border-b border-outline-variant">
                                <h2 class="text-headline-md text-primary">Thanh toán đơn hàng</h2>
                                <button onclick="closePaymentModal()"
                                    class="w-10 h-10 rounded-full hover:bg-surface-container-high flex items-center justify-center text-on-surface-variant transition-colors">
                                    <span class="material-symbols-outlined">close</span>
                                </button>
                            </div>
                            <!-- Body -->
                            <div class="flex flex-1 overflow-hidden">
                                <!-- Left: Payment Methods -->
                                <div
                                    class="w-[320px] bg-surface-container-low p-6 border-r border-outline-variant space-y-3 shrink-0">
                                    <p class="text-label-md text-outline mb-2">Hình thức thanh toán</p>
                                    <button onclick="selectModalPayMethod('CASH')" id="modalBtnCash"
                                        class="w-full flex items-center gap-4 p-4 rounded-xl border-2 transition-all active-payment-method">
                                        <div
                                            class="w-12 h-12 rounded-lg bg-surface-container-high flex items-center justify-center">
                                            <span
                                                class="material-symbols-outlined text-[28px] text-primary">payments</span>
                                        </div>
                                        <div class="text-left">
                                            <div class="text-label-md font-bold">Tiền mặt</div>
                                            <div class="text-caption text-outline">Thanh toán bằng tiền mặt</div>
                                        </div>
                                    </button>
                                    <button onclick="selectModalPayMethod('BANK_TRANSFER')" id="modalBtnBank"
                                        class="w-full flex items-center gap-4 p-4 rounded-xl border-2 border-outline-variant bg-white transition-all hover:border-outline">
                                        <div
                                            class="w-12 h-12 rounded-lg bg-surface-container-high flex items-center justify-center">
                                            <span
                                                class="material-symbols-outlined text-[28px] text-on-surface-variant">credit_card</span>
                                        </div>
                                        <div class="text-left">
                                            <div class="text-label-md font-bold">Thẻ ngân hàng / Chuyển khoản</div>
                                            <div class="text-caption text-outline">Quẹt thẻ hoặc chuyển khoản</div>
                                        </div>
                                    </button>
                                </div>
                                <!-- Right: Calculation -->
                                <div class="flex-1 p-6 space-y-5 overflow-y-auto">
                                    <div class="text-center">
                                        <p class="text-label-md text-outline mb-1">Tổng số tiền cần thanh toán</p>
                                        <p id="modalTotalDisplay" class="text-display-lg text-primary font-bold">0 <span
                                                class="text-headline-md text-outline">₫</span></p>
                                    </div>
                                    <div id="cashInputSection">
                                        <label class="text-label-md text-on-surface-variant block mb-2">Khách thanh
                                            toán</label>
                                        <div class="relative">
                                            <input id="modalCashInput" type="text"
                                                class="w-full text-headline-lg text-primary font-bold bg-surface-container-low rounded-xl pl-5 pr-14 py-4 border-2 border-primary outline-none text-right"
                                                oninput="formatInputCurrency(this); calcChange()">
                                            <span
                                                class="absolute right-5 top-1/2 -translate-y-1/2 text-headline-md text-outline font-bold">₫</span>
                                        </div>
                                    </div>
                                    <div>
                                        <label class="text-label-md text-on-surface-variant block mb-2">Tiền thừa trả
                                            khách</label>
                                        <div id="changeBox" class="rounded-xl px-5 py-4 bg-tertiary-fixed text-center">
                                            <p id="changeDisplay" class="text-headline-lg font-bold text-tertiary">0 ₫
                                            </p>
                                        </div>
                                    </div>
                                    <div id="quickCashSection" class="space-y-2">
                                        <p class="text-label-md text-outline">Gợi ý tiền mặt nhanh</p>
                                        <div class="grid grid-cols-4 gap-2">
                                            <button onclick="setQuickCash(50000)"
                                                class="py-2.5 rounded-lg bg-surface-container-high text-label-md font-bold hover:bg-surface-container-highest transition-colors">50.000</button>
                                            <button onclick="setQuickCash(100000)"
                                                class="py-2.5 rounded-lg bg-surface-container-high text-label-md font-bold hover:bg-surface-container-highest transition-colors">100.000</button>
                                            <button onclick="setQuickCash(200000)"
                                                class="py-2.5 rounded-lg bg-surface-container-high text-label-md font-bold hover:bg-surface-container-highest transition-colors">200.000</button>
                                            <button onclick="setQuickCash(500000)"
                                                class="py-2.5 rounded-lg bg-surface-container-high text-label-md font-bold hover:bg-surface-container-highest transition-colors">500.000</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- Footer -->
                            <div
                                class="flex items-center justify-between px-6 py-4 border-t border-outline-variant bg-surface-container-low">
                                <label class="flex items-center gap-3 cursor-pointer">
                                    <input type="checkbox" id="autoPrintCheckbox" checked
                                        class="w-5 h-5 rounded text-primary focus:ring-primary border-outline-variant">
                                    <span class="text-label-md">In hóa đơn tự động</span>
                                </label>
                                <div class="flex gap-3">
                                    <button onclick="closePaymentModal()"
                                        class="px-5 py-2.5 rounded-xl border border-outline-variant text-on-surface-variant font-button-text hover:bg-surface-container-high transition-colors">Bỏ
                                        qua (Esc)</button>
                                    <button onclick="submitCheckout()"
                                        class="px-6 py-2.5 rounded-xl bg-primary text-on-primary font-button-text flex items-center gap-2 hover:bg-secondary transition-colors shadow-md">
                                        <span>Xác nhận thanh toán (F12)</span>
                                        <span class="material-symbols-outlined text-[20px]">check_circle</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- ═══════════════════════════════════════════════════════ -->
                    <!-- ═══════════════ CUSTOMER MODAL ════════════════════════ -->
                    <!-- ═══════════════════════════════════════════════════════ -->
                    <div id="customerModal"
                        class="hidden fixed inset-0 z-50 modal-blur flex items-center justify-center">
                        <div
                            class="bg-surface-container-lowest rounded-xl shadow-2xl w-[600px] max-h-[80vh] flex flex-col animate-fadeIn">
                            <div class="flex items-center justify-between px-6 py-4 border-b border-outline-variant">
                                <div class="flex items-center gap-2"><span
                                        class="material-symbols-outlined text-primary">group</span>
                                    <h3 class="text-title-lg font-bold">Chọn khách hàng</h3>
                                </div>
                                <button onclick="closeCustomerModal()"
                                    class="w-10 h-10 rounded-full hover:bg-surface-container-high flex items-center justify-center text-on-surface-variant"><span
                                        class="material-symbols-outlined">close</span></button>
                            </div>
                            <div class="px-6 py-4 border-b border-outline-variant flex gap-3">
                                <div
                                    class="flex-1 flex items-center bg-surface-container-low rounded-xl px-4 h-11 gap-2 border border-transparent focus-within:border-primary focus-within:ring-1 focus-within:ring-primary/10">
                                    <span class="material-symbols-outlined text-outline text-[18px]">search</span>
                                    <input id="customerSearchInput" type="text"
                                        placeholder="Tìm theo tên, số điện thoại hoặc mã khách hàng (F4)"
                                        class="bg-transparent outline-none flex-1 text-body-md">
                                </div>
                                <button onclick="openAddCustomerModal()"
                                    class="px-4 h-11 bg-primary text-on-primary rounded-xl font-label-md flex items-center gap-2 hover:bg-secondary transition-colors">
                                    <span class="material-symbols-outlined text-[18px]">person_add</span> Thêm mới
                                </button>
                            </div>
                            <div class="flex-1 overflow-y-auto scrollbar-thin px-2 py-2" id="customerListContainer">
                                <!-- Khách vãng lai -->
                                <button onclick="pickCustomer(0, 'Khách vãng lai')"
                                    class="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-surface-container-high transition-colors group">
                                    <div
                                        class="w-10 h-10 rounded-full bg-surface-container-high flex items-center justify-center text-outline font-bold text-sm">
                                        ?</div>
                                    <div class="flex-1 text-left">
                                        <div class="text-label-md">Khách vãng lai</div>
                                        <div class="text-caption text-outline">Không lưu thông tin</div>
                                    </div>
                                    <span
                                        class="text-label-md text-primary opacity-0 group-hover:opacity-100 transition-opacity px-3 py-1 rounded-lg">Chọn</span>
                                </button>
                                <c:forEach items="${customerList}" var="c">
                                    <button onclick="pickCustomer(${c.cusId}, '${fn:escapeXml(c.fullName)}')"
                                        class="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-surface-container-high transition-colors group"
                                        data-search="${fn:toLowerCase(c.fullName)} ${c.phone}">
                                        <div
                                            class="w-10 h-10 rounded-full bg-primary-fixed text-on-primary-fixed flex items-center justify-center font-bold text-sm">
                                            ${fn:substring(c.fullName, 0, 1)}</div>
                                        <div class="flex-1 text-left min-w-0">
                                            <div class="text-label-md truncate">${c.fullName}</div>
                                            <div class="flex items-center gap-3 text-caption text-outline">
                                                <span class="flex items-center gap-1"><span
                                                        class="material-symbols-outlined text-[14px]">call</span>${c.phone}</span>
                                                <span class="flex items-center gap-1"><span
                                                        class="material-symbols-outlined text-[14px]">stars</span>${c.loyaltyPoint} điểm</span>
                                            </div>
                                        </div>
                                        <span
                                            class="text-label-md text-primary opacity-0 group-hover:opacity-100 transition-opacity px-3 py-1.5 rounded-lg group-hover:bg-primary group-hover:text-white">Chọn</span>
                                    </button>
                                </c:forEach>
                            </div>
                            <div class="flex justify-end gap-3 px-6 py-4 border-t border-outline-variant">
                                <button onclick="closeCustomerModal()"
                                    class="px-5 py-2.5 rounded-xl border border-outline-variant text-on-surface-variant font-button-text hover:bg-surface-container-high transition-colors">Hủy</button>
                            </div>
                        </div>
                    </div>

                    <!-- ═══════════════════════════════════════════════════════ -->
                    <!-- ═══════════════ ADD CUSTOMER MODAL ════════════════════ -->
                    <!-- ═══════════════════════════════════════════════════════ -->
                    <div id="addCustomerModal"
                        class="hidden fixed inset-0 z-[60] modal-blur flex items-center justify-center">
                        <div
                            class="bg-surface-container-lowest rounded-xl shadow-2xl w-[640px] max-h-[85vh] flex flex-col animate-fadeIn">
                            <div class="flex items-center justify-between px-6 py-4 border-b border-outline-variant">
                                <h3 class="text-title-lg font-bold">Thêm khách hàng mới</h3>
                                <button onclick="closeAddCustomerModal()"
                                    class="w-10 h-10 rounded-full hover:bg-surface-container-high flex items-center justify-center text-on-surface-variant"><span
                                        class="material-symbols-outlined">close</span></button>
                            </div>
                            <div class="flex-1 overflow-y-auto px-6 py-5">
                                <div class="grid grid-cols-2 gap-x-8 gap-y-6">
                                    <div>
                                        <label class="text-label-md text-on-surface-variant block mb-1.5">Họ tên <span
                                                class="text-error">*</span></label>
                                        <input id="newCusName" type="text"
                                            class="w-full rounded-xl border border-outline-variant px-4 py-2.5 text-body-md focus:border-primary focus:ring-2 focus:ring-primary/10 outline-none"
                                            placeholder="Nhập họ tên">
                                    </div>
                                    <div>
                                        <label class="text-label-md text-on-surface-variant block mb-1.5">Số điện thoại
                                            <span class="text-error">*</span></label>
                                        <input id="newCusPhone" type="text"
                                            class="w-full rounded-xl border border-outline-variant px-4 py-2.5 text-body-md focus:border-primary focus:ring-2 focus:ring-primary/10 outline-none"
                                            placeholder="0912 345 678">
                                    </div>
                                    <div>
                                        <label class="text-label-md text-on-surface-variant block mb-1.5">Email</label>
                                        <input id="newCusEmail" type="email"
                                            class="w-full rounded-xl border border-outline-variant px-4 py-2.5 text-body-md focus:border-primary focus:ring-2 focus:ring-primary/10 outline-none"
                                            placeholder="email@example.com">
                                    </div>
                                    <div>
                                        <label class="text-label-md text-on-surface-variant block mb-1.5">Ngày
                                            sinh</label>
                                        <input id="newCusBod" type="date"
                                            class="w-full rounded-xl border border-outline-variant px-4 py-2.5 text-body-md focus:border-primary focus:ring-2 focus:ring-primary/10 outline-none">
                                    </div>
                                    <div>
                                        <label class="text-label-md text-on-surface-variant block mb-1.5">Giới
                                            tính</label>
                                        <div class="flex gap-4 mt-1.5">
                                            <label class="flex items-center gap-2 cursor-pointer"><input type="radio"
                                                    name="newCusGender" value="Nam" checked
                                                    class="text-primary focus:ring-primary"><span
                                                    class="text-body-md">Nam</span></label>
                                            <label class="flex items-center gap-2 cursor-pointer"><input type="radio"
                                                    name="newCusGender" value="Nữ"
                                                    class="text-primary focus:ring-primary"><span
                                                    class="text-body-md">Nữ</span></label>
                                            <label class="flex items-center gap-2 cursor-pointer"><input type="radio"
                                                    name="newCusGender" value="Khác"
                                                    class="text-primary focus:ring-primary"><span
                                                    class="text-body-md">Khác</span></label>
                                        </div>
                                    </div>
                                    <div class="col-span-2">
                                        <label class="text-label-md text-on-surface-variant block mb-1.5">Địa
                                            chỉ</label>
                                        <input id="newCusAddress" type="text"
                                            class="w-full rounded-xl border border-outline-variant px-4 py-2.5 text-body-md focus:border-primary focus:ring-2 focus:ring-primary/10 outline-none"
                                            placeholder="Nhập địa chỉ">
                                    </div>
                                    <div class="col-span-2">
                                        <label class="text-label-md text-on-surface-variant block mb-1.5">Ghi
                                            chú</label>
                                        <textarea id="newCusNote" rows="2"
                                            class="w-full rounded-xl border border-outline-variant px-4 py-2.5 text-body-md focus:border-primary focus:ring-2 focus:ring-primary/10 outline-none resize-none"
                                            placeholder="Ghi chú về khách hàng..."></textarea>
                                    </div>
                                </div>
                            </div>
                            <div class="flex justify-end gap-3 px-6 py-4 border-t border-outline-variant">
                                <button onclick="closeAddCustomerModal()"
                                    class="px-5 py-2.5 rounded-xl border border-primary text-primary font-button-text hover:bg-primary-fixed transition-colors">Hủy</button>
                                <button onclick="saveNewCustomer()"
                                    class="px-6 py-2.5 rounded-xl bg-primary text-on-primary font-button-text flex items-center gap-2 hover:bg-secondary transition-colors">
                                    <span class="material-symbols-outlined text-[18px]">save</span> Lưu khách hàng
                                </button>
                            </div>
                        </div>
                    </div>

                    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

                    <!-- ═══════════════ SUCCESS TOAST ═══════════════ -->
                    <div id="successToast"
                        class="hidden fixed top-6 right-6 z-[70] bg-tertiary-container text-on-tertiary-container px-6 py-4 rounded-xl shadow-xl flex items-center gap-3 animate-fadeIn">
                        <span class="material-symbols-outlined text-[24px]">check_circle</span>
                        <div>
                            <div id="toastTitle" class="font-bold">Thanh toán thành công!</div>
                            <div id="toastMessage" class="text-caption"></div>
                        </div>
                    </div>

                    <!-- ═══════════════════════════════════════════════════════ -->
                    <!-- ═══════════════ JAVASCRIPT ════════════════════════════ -->
                    <!-- ═══════════════════════════════════════════════════════ -->
                    <script>
                        const CTX = '${pageContext.request.contextPath}';
                        const CSRF_TOKEN = '${sessionScope.csrfToken}';
                        let currentPayMethod = 'CASH';
                        let modalPayMethod = 'CASH';
                        let cartState = null; // Dữ liệu chứa tabs, activeTabId, activeTab

                        // ── Init ────────────────────────────────────────────────
                        document.addEventListener('DOMContentLoaded', () => { loadCart(); });

                        // ── Cart AJAX ───────────────────────────────────────────
                        async function loadCart() {
                            try {
                                const res = await fetch(CTX + '/cart');
                                cartState = await res.json();
                                renderUI();
                            } catch (e) { console.error('Load cart error:', e); }
                        }

                        async function addToCart(productId, code) {
                            if (!cartState) return;
                            const body = new URLSearchParams();
                            body.append('action', 'add');
                            if (productId) body.append('productId', productId);
                            if (code) body.append('code', code);
                            body.append('tabId', cartState.activeTabId);
                            body.append('csrfToken', CSRF_TOKEN);

                            try {
                                const res = await fetch(CTX + '/cart', { method: 'POST', body });
                                const data = await res.json();
                                if (data.error) { showAlert(data.error); return; }
                                cartState = data;
                                renderUI();
                            } catch (e) { console.error(e); }
                        }

                        async function updateCartQty(productId, qty) {
                            if (!cartState) return;
                            if (qty <= 0) {
                                if (confirm("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")) {
                                    removeCartItem(productId, true);
                                } else {
                                    renderUI();
                                }
                                return;
                            }

                            const body = new URLSearchParams({
                                action: 'update',
                                productId: productId,
                                quantity: qty,
                                tabId: cartState.activeTabId,
                                csrfToken: CSRF_TOKEN
                            });
                            try {
                                const res = await fetch(CTX + '/cart', { method: 'POST', body });
                                const data = await res.json();
                                if (data.error) { showAlert(data.error); return; }
                                cartState = data;
                                renderUI();
                            } catch (e) { console.error(e); }
                        }

                        async function removeCartItem(productId, force = false) {
                            if (!cartState) return;
                            if (!force && !confirm("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")) {
                                return;
                            }
                            const body = new URLSearchParams({
                                action: 'remove',
                                productId: productId,
                                tabId: cartState.activeTabId,
                                csrfToken: CSRF_TOKEN
                            });
                            try {
                                const res = await fetch(CTX + '/cart', { method: 'POST', body });
                                cartState = await res.json();
                                renderUI();
                            } catch (e) { console.error(e); }
                        }

                        async function newTab() {
                            try {
                                const res = await fetch(CTX + '/cart', {
                                    method: 'POST',
                                    body: new URLSearchParams({ action: 'newTab', csrfToken: CSRF_TOKEN })
                                });
                                cartState = await res.json();
                                renderUI();
                            } catch (e) { console.error(e); }
                        }

                        async function switchTab(tabId) {
                            try {
                                const res = await fetch(CTX + '/cart', {
                                    method: 'POST',
                                    body: new URLSearchParams({ action: 'switchTab', tabId: tabId, csrfToken: CSRF_TOKEN })
                                });
                                cartState = await res.json();
                                renderUI();
                            } catch (e) { console.error(e); }
                        }

                        async function holdOrder() {
                            if (!cartState) return;
                            try {
                                const res = await fetch(CTX + '/cart', {
                                    method: 'POST',
                                    body: new URLSearchParams({ action: 'hold', tabId: cartState.activeTabId, csrfToken: CSRF_TOKEN })
                                });
                                cartState = await res.json();
                                renderUI();
                                showToast('Lưu tạm thành công!', 'Đơn hàng đã được chuyển sang trạng thái chờ.');
                            } catch (e) { console.error(e); }
                        }

                        async function cancelOrder() {
                            if (!cartState) return;
                            if (!confirm("Bạn có chắc chắn muốn hủy đơn hàng này không?")) return;
                            try {
                                const res = await fetch(CTX + '/cart', {
                                    method: 'POST',
                                    body: new URLSearchParams({ action: 'clear', tabId: cartState.activeTabId, csrfToken: CSRF_TOKEN })
                                });
                                cartState = await res.json();
                                renderUI();
                                showToast('Đã hủy đơn hàng!', 'Dữ liệu đơn hiện tại đã được xóa.');
                            } catch (e) { console.error(e); }
                        }

                        async function changeVoucher(voucherId) {
                            if (!cartState) return;
                            try {
                                const res = await fetch(CTX + '/cart', {
                                    method: 'POST',
                                    body: new URLSearchParams({
                                        action: 'applyVoucher',
                                        voucherId: voucherId,
                                        tabId: cartState.activeTabId,
                                        csrfToken: CSRF_TOKEN
                                    })
                                });
                                cartState = await res.json();
                                renderUI();
                            } catch (e) { console.error(e); }
                        }

                        function printPreview() {
                            if (!cartState) return;
                            window.open(CTX + '/print/preview?tabId=' + cartState.activeTabId, '_blank');
                        }

                        // ── Render UI ───────────────────────────────────────────
                        function renderUI() {
                            if (!cartState) return;
                            const tabRow = document.getElementById('tabRowContainer');
                            if (tabRow) {
                                tabRow.innerHTML = '';
                                cartState.tabs.forEach(tab => {
                                    const btn = document.createElement('button');
                                    btn.onclick = () => switchTab(tab.tabId);
                                    btn.className = 'px-5 pb-2.5 pt-2 text-label-md transition-all ' +
                                        (tab.tabId === cartState.activeTabId
                                            ? 'text-primary border-b-2 border-primary font-bold'
                                            : 'text-on-surface-variant hover:bg-surface-container-low');
                                    btn.textContent = 'Đơn ' + tab.tabId + (tab.status === 'HOLD' ? ' (Tạm)' : '');
                                    tabRow.appendChild(btn);
                                });
                            }

                            const activeTab = cartState.activeTab;
                            const empty = document.getElementById('emptyCartState');
                            const wrapper = document.getElementById('cartTableWrapper');
                            const tbody = document.getElementById('cartTableBody');

                            if (!activeTab.items || activeTab.items.length === 0) {
                                empty.classList.remove('hidden');
                                wrapper.classList.add('hidden');
                                tbody.innerHTML = '';
                            } else {
                                empty.classList.add('hidden');
                                wrapper.classList.remove('hidden');
                                tbody.innerHTML = '';
                                activeTab.items.forEach((item, idx) => {
                                    const tr = document.createElement('tr');
                                    tr.className = 'hover:bg-surface-container-low/50 transition-colors';
                                    tr.innerHTML =
                                        '<td class="py-3 px-4 text-caption text-outline">' + (idx + 1) + '</td>' +
                                        '<td class="py-3 px-4"><div class="flex items-center gap-3">' +
                                        '<div class="w-10 h-10 rounded-lg bg-surface-container-high flex items-center justify-center shrink-0"><span class="material-symbols-outlined text-outline text-[20px]">image</span></div>' +
                                        '<div><div class="text-label-md">' + esc(item.productName) + '</div><div class="text-caption text-outline">' + esc(item.productCodebar) + '</div></div></div></td>' +
                                        '<td class="py-3 px-4 text-right text-body-md">' + fmt(item.sellingPrice) + '</td>' +
                                        '<td class="py-3 px-4"><div class="flex items-center justify-center gap-1">' +
                                        '<button onclick="updateCartQty(' + item.productId + ',' + (item.quantity - 1) + ')" class="w-8 h-8 rounded-lg bg-surface-container-high flex items-center justify-center hover:bg-surface-dim transition-colors"><span class="material-symbols-outlined text-[16px]">remove</span></button>' +
                                        '<input type="number" value="' + item.quantity + '" min="1" max="' + item.stockAvailable + '" onchange="updateCartQty(' + item.productId + ',parseInt(this.value)||1)" class="w-12 h-8 text-center text-label-md bg-surface-container-high rounded-lg border-0 outline-none">' +
                                        '<button onclick="updateCartQty(' + item.productId + ',' + (item.quantity + 1) + ')" class="w-8 h-8 rounded-lg bg-surface-container-high flex items-center justify-center hover:bg-surface-dim transition-colors"><span class="material-symbols-outlined text-[16px]">add</span></button></div></td>' +
                                        '<td class="py-3 px-4 text-right text-body-md font-semibold">' + fmt(item.lineTotal) + '</td>' +
                                        '<td class="py-3 px-4 text-center"><button onclick="removeCartItem(' + item.productId + ')" class="text-outline hover:text-error transition-colors"><span class="material-symbols-outlined text-[20px]">delete</span></button></td>';
                                    tbody.appendChild(tr);
                                });
                            }

                            const customerNameDiv = document.getElementById('selectedCustomerName');
                            const customerIdInput = document.getElementById('selectedCustomerId');
                            const pointsDisplay = document.getElementById('customerPointsDisplay');
                            const pointsValue = document.getElementById('customerPointsValue');
                            if (activeTab.selectedCustomer) {
                                customerNameDiv.textContent = activeTab.selectedCustomer.fullName + ' - ' + activeTab.selectedCustomer.phone;
                                customerIdInput.value = activeTab.selectedCustomer.cusId;
                                pointsValue.textContent = activeTab.selectedCustomer.loyaltyPoint || 0;
                                pointsDisplay.classList.remove('hidden');
                            } else {
                                customerNameDiv.textContent = 'Khách vãng lai';
                                customerIdInput.value = '';
                                pointsDisplay.classList.add('hidden');
                            }

                            document.getElementById('summaryItemCount').textContent = activeTab.items.reduce((sum, item) => sum + item.quantity, 0);
                            document.getElementById('summarySubtotal').textContent = fmt(activeTab.subtotal);
                            document.getElementById('summaryDiscount').textContent = activeTab.discountAmount > 0 ? '-' + fmt(activeTab.discountAmount) : '0 ₫';
                            document.getElementById('summaryVat').textContent = fmt(activeTab.vatAmount);
                            document.getElementById('summaryTotal').innerHTML = fmt(activeTab.totalAmount).replace('₫', '') + '<span class="text-headline-md"> ₫</span>';
                        }

                        // ── Product Search ──────────────────────────────────────
                        const searchInput = document.getElementById('searchInput');
                        const searchDropdown = document.getElementById('searchDropdown');
                        let searchTimeout = null;

                        searchInput.addEventListener('input', function () {
                            clearTimeout(searchTimeout);
                            const q = this.value.trim();
                            if (q.length < 1) { searchDropdown.classList.add('hidden'); return; }
                            searchTimeout = setTimeout(() => searchProducts(q), 300);
                        });

                        searchInput.addEventListener('keydown', function (e) {
                            if (e.key === 'Enter') {
                                e.preventDefault();
                                const q = this.value.trim();
                                if (!q) return;
                                addToCart(null, q);
                                this.value = '';
                                searchDropdown.classList.add('hidden');
                            }
                        });

                        async function searchProducts(query) {
                            try {
                                const res = await fetch(CTX + '/product/search?keyword=' + encodeURIComponent(query));
                                const list = await res.json();
                                if (!list.length) { searchDropdown.classList.add('hidden'); return; }
                                searchDropdown.innerHTML = '';
                                list.forEach(p => {
                                    const btn = document.createElement('button');
                                    btn.className = 'w-full flex items-center gap-3 px-4 py-3 hover:bg-surface-container-high transition-colors text-left border-b border-outline-variant/50 last:border-0';
                                    btn.innerHTML = '<div class="w-10 h-10 rounded-lg bg-surface-container-high flex items-center justify-center shrink-0"><span class="material-symbols-outlined text-outline text-[20px]">image</span></div>' +
                                        '<div class="flex-1 min-w-0"><div class="text-label-md truncate">' + esc(p.productName) + '</div><div class="text-caption text-outline">' + esc(p.productCodebar) + '</div></div>' +
                                        '<div class="text-right shrink-0"><div class="text-label-md font-semibold text-primary">' + fmt(p.sellingPrice) + '</div><div class="text-caption text-outline">Kho: ' + p.quantityInStock + '</div></div>';
                                    btn.onclick = () => { addToCart(p.productId, null); searchInput.value = ''; searchDropdown.classList.add('hidden'); };
                                    searchDropdown.appendChild(btn);
                                });
                                searchDropdown.classList.remove('hidden');
                            } catch (e) { console.error(e); }
                        }

                        // ── Payment Modal ───────────────────────────────────────
                        function openPaymentModal() {
                            if (!cartState || !cartState.activeTab.items || cartState.activeTab.items.length === 0) {
                                showAlert('Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán.');
                                return;
                            }
                            const total = cartState.activeTab.totalAmount;
                            document.getElementById('modalTotalDisplay').innerHTML = fmt(total).replace('₫', '') + ' <span class="text-headline-md text-outline">₫</span>';
                            document.getElementById('modalCashInput').value = new Intl.NumberFormat('vi-VN').format(Math.ceil(total));
                            document.getElementById('paymentModal').classList.remove('hidden');
                            calcChange();
                        }

                        function closePaymentModal() { document.getElementById('paymentModal').classList.add('hidden'); }

                        function selectModalPayMethod(method) {
                            modalPayMethod = method;
                            document.getElementById('modalBtnCash').className = 'w-full flex items-center gap-4 p-4 rounded-xl border-2 transition-all ' + (method === 'CASH' ? 'active-payment-method' : 'border-outline-variant bg-white hover:border-outline');
                            document.getElementById('modalBtnBank').className = 'w-full flex items-center gap-4 p-4 rounded-xl border-2 transition-all ' + (method === 'BANK_TRANSFER' ? 'active-payment-method' : 'border-outline-variant bg-white hover:border-outline');
                            document.getElementById('cashInputSection').style.display = method === 'CASH' ? '' : 'none';
                            document.getElementById('quickCashSection').style.display = method === 'CASH' ? '' : 'none';
                            if (method !== 'CASH') {
                                document.getElementById('changeDisplay').textContent = 'Thanh toán qua thẻ/CK';
                            } else { calcChange(); }
                        }

                        function setQuickCash(amount) { document.getElementById('modalCashInput').value = new Intl.NumberFormat('vi-VN').format(amount); calcChange(); }

                        function calcChange() {
                            if (!cartState) return;
                            const total = cartState.activeTab.totalAmount;
                            const paidVal = document.getElementById('modalCashInput').value.replace(/\D/g, "");
                            const paid = parseFloat(paidVal) || 0;
                            const change = paid - total;
                            const display = document.getElementById('changeDisplay');
                            display.textContent = change >= 0 ? fmt(change) : 'Còn thiếu ' + fmt(Math.abs(change));
                        }

                        async function submitCheckout() {
                            if (!cartState) return;
                            const body = new URLSearchParams();
                            body.append('paymentMethod', modalPayMethod);
                            body.append('cashReceived', modalPayMethod === 'CASH' ? document.getElementById('modalCashInput').value.replace(/\D/g, "") : '999999999');
                            body.append('tabId', cartState.activeTabId);
                            body.append('csrfToken', CSRF_TOKEN);
                            try {
                                const res = await fetch(CTX + '/checkout', { method: 'POST', body });
                                const data = await res.json();
                                if (data.status === 'success') {
                                    closePaymentModal();
                                    showToast('Thanh toán thành công!', 'Mã đơn: ' + data.orderCode);
                                    loadCart();
                                    // Reload trang sau 1.5s để cập nhật tồn kho ở phần Chọn nhanh sản phẩm
                                    setTimeout(() => { window.location.reload(); }, 1500);
                                } else { showAlert(data.message || 'Lỗi thanh toán.'); }
                            } catch (e) { console.error(e); }
                        }

                        // ── Customer Modal ──────────────────────────────────────
                        function openCustomerModal() { document.getElementById('customerModal').classList.remove('hidden'); }
                        function closeCustomerModal() { document.getElementById('customerModal').classList.add('hidden'); }
                        async function pickCustomer(id, name) {
                            try {
                                const res = await fetch(CTX + '/cart', { method: 'POST', body: new URLSearchParams({ action: 'selectCustomer', customerId: id, tabId: cartState.activeTabId, csrfToken: CSRF_TOKEN }) });
                                cartState = await res.json();
                                renderUI();
                                closeCustomerModal();
                            } catch (e) { console.error(e); }
                        }

                        // ── Add Customer ────────────────────────────────────────
                        function openAddCustomerModal() { document.getElementById('addCustomerModal').classList.remove('hidden'); }
                        function closeAddCustomerModal() { document.getElementById('addCustomerModal').classList.add('hidden'); }
                        async function saveNewCustomer() {
                            const name = document.getElementById('newCusName').value.trim();
                            const phone = document.getElementById('newCusPhone').value.trim();
                            if (!name || !phone) { showAlert('Nhập đủ tên và SĐT.'); return; }
                            const body = new URLSearchParams({
                                action: 'addCustomer', fullName: name, phone: phone,
                                email: document.getElementById('newCusEmail').value.trim(),
                                bod: document.getElementById('newCusBod').value,
                                gender: document.querySelector('input[name="newCusGender"]:checked').value,
                                address: document.getElementById('newCusAddress').value.trim(),
                                csrfToken: CSRF_TOKEN
                            });
                            try {
                                const res = await fetch(CTX + '/sales', { method: 'POST', body });
                                const data = await res.json();
                                if (data.cusId) {
                                    const listContainer = document.getElementById('customerListContainer');
                                    if (listContainer) {
                                        const btn = document.createElement('button');
                                        btn.onclick = () => pickCustomer(data.cusId, name);
                                        btn.className = 'w-full flex items-center gap-3 p-3 rounded-xl hover:bg-surface-container-high transition-colors group';
                                        btn.setAttribute('data-search', name.toLowerCase() + ' ' + phone);
                                        btn.innerHTML = '<div class="w-10 h-10 rounded-full bg-primary-fixed text-on-primary-fixed flex items-center justify-center font-bold text-sm">' + name.substring(0, 1) + '</div>' +
                                            '<div class="flex-1 text-left min-w-0">' +
                                            '<div class="text-label-md truncate">' + esc(name) + '</div>' +
                                            '<div class="flex items-center gap-3 text-caption text-outline">' +
                                            '<span class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">call</span>' + esc(phone) + '</span>' +
                                            '<span class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">stars</span>Thường</span>' +
                                            '</div>' +
                                            '</div>' +
                                            '<span class="text-label-md text-primary opacity-0 group-hover:opacity-100 transition-opacity px-3 py-1.5 rounded-lg group-hover:bg-primary group-hover:text-white">Chọn</span>';
                                        listContainer.appendChild(btn);
                                    }
                                    pickCustomer(data.cusId, name);
                                    closeAddCustomerModal();
                                    closeCustomerModal();
                                    showToast('Thêm khách hàng thành công!', name + ' — ' + phone);
                                } else {
                                    showAlert(data.error || 'Không thể thêm khách hàng.');
                                }
                            } catch (e) { showAlert('Lỗi kết nối.'); console.error(e); }
                        }

                        // ── Keyboard shortcuts ──────────────────────────────────
                        window.addEventListener('keydown', e => {
                            if (e.key === 'Escape') { closePaymentModal(); closeCustomerModal(); closeAddCustomerModal(); }
                            if (e.key === 'F12') { e.preventDefault(); if (!document.getElementById('paymentModal').classList.contains('hidden')) submitCheckout(); else openPaymentModal(); }
                            if (e.key === 'F4') { e.preventDefault(); openCustomerModal(); }
                        });

                        // ── Helpers ─────────────────────────────────────────────
                        function formatInputCurrency(input) {
                            let val = input.value.replace(/\D/g, "");
                            if (val) {
                                input.value = new Intl.NumberFormat('vi-VN').format(parseInt(val));
                            } else {
                                input.value = "";
                            }
                        }
                        function fmt(n) { return Math.round(n).toLocaleString('vi-VN') + ' ₫'; }
                        function esc(s) { if (!s) return ''; const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
                        function showAlert(msg) { alert(msg); }
                        function showToast(title, message) {
                            const t = document.getElementById('successToast');
                            document.getElementById('toastTitle').textContent = title;
                            document.getElementById('toastMessage').textContent = message;
                            t.classList.remove('hidden');
                            setTimeout(() => t.classList.add('hidden'), 4000);
                        }
                    </script>
                </body>

                </html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>Lịch sử đơn hàng | POS System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sales.css">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet">
</head>
<body>
    <div class="pos-layout">
        <!-- Sidebar Navigation -->
        <aside class="pos-sidebar"
            <div class="sidebar-logo">
                <h2>POS</h2>
                <span>V1.0</span>
            </div>
            <nav class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/sales" class="sidebar-btn">
                    <span class="material-symbols-outlined">point_of_sale</span>
                    <span class="label">Bán hàng</span>
                </a>
                <a href="${pageContext.request.contextPath}/sales?action=history" class="sidebar-btn active">
                    <span class="material-symbols-outlined">receipt_long</span>
                    <span class="label">Đơn hàng</span>
                </a>
                <a href="${pageContext.request.contextPath}/branch" class="sidebar-btn">
                    <span class="material-symbols-outlined">storefront</span>
                    <span class="label">Chi nhánh</span>
                </a>
            </nav>
        </aside>

        <!-- Main Content Area -->
        <main class="pos-main">
            <!-- Top App Bar -->
            <header class="pos-header">
                <div class="header-left">
                    <h1 class="header-title">Lịch sử đơn hàng</h1>
                    <div class="search-wrapper" style="width: 320px;">
                        <span class="material-symbols-outlined">search</span>
                        <input id="historySearchInput" class="search-input" placeholder="Tìm mã đơn hàng..." type="text">
                    </div>
                </div>
                <div class="header-right">
                    <a href="${pageContext.request.contextPath}/sales" class="btn-primary">
                        <span class="material-symbols-outlined">add</span>
                        <span>Thêm đơn mới</span>
                    </a>
                </div>
            </header>

            <!-- Content Body -->
            <div class="history-layout">
                <!-- Order List Side -->
                <section class="history-table-section">
                    <div class="history-table-card">
                        <table class="pos-table">
                            <thead>
                                <tr>
                                    <th>Mã đơn hàng</th>
                                    <th>Khách hàng</th>
                                    <th>Thời gian</th>
                                    <th>Tổng tiền</th>
                                    <th>Phương thức</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody id="ordersTableBody">
                                <c:forEach items="${orderList}" var="o">
                                    <tr class="order-row"
                                        data-id="${o.orderId}" 
                                        data-code="${o.orderCode}" 
                                        data-customer="${fn:escapeXml(empty o.customerName ? 'Khách lẻ' : o.customerName)}" 
                                        data-employee="${fn:escapeXml(empty o.employeeName ? 'Thu ngân' : o.employeeName)}" 
                                        data-date="${o.createdAt}" 
                                        data-total="${o.totalAmount}" 
                                        data-subtotal="${o.subtotal}" 
                                        data-discount="${o.discountAmount}" 
                                        data-method="${o.paymentMethod}" 
                                        data-status="${o.status}">
                                        <td style="font-weight: 700; color: var(--primary);">${o.orderCode}</td>
                                        <td>${empty o.customerName ? 'Khách lẻ' : o.customerName}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${fn:length(o.createdAt) >= 19}">
                                                    <fmt:parseDate value="${fn:substring(o.createdAt, 0, 19)}" pattern="yyyy-MM-dd HH:mm:ss" var="oCreated"/>
                                                    <fmt:formatDate value="${oCreated}" pattern="dd/MM/yyyy HH:mm"/>
                                                </c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-weight: 600;">
                                            <fmt:formatNumber value="${o.totalAmount}" type="number" maxFractionDigits="0"/> ₫
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${o.paymentMethod == 'CASH'}">Tiền mặt</c:when>
                                                <c:when test="${o.paymentMethod == 'CARD'}">Thẻ ngân hàng</c:when>
                                                <c:when test="${o.paymentMethod == 'TRANSFER'}">Chuyển khoản</c:when>
                                                <c:otherwise>${o.paymentMethod}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="status-badge 
                                                ${o.status == 'PAID' ? 'paid' : ''}
                                                ${o.status == 'PENDING' ? 'pending' : ''}
                                                ${o.status == 'CANCELLED' ? 'cancelled' : ''}">
                                                ${o.status.displayName}
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty orderList}">
                                    <tr>
                                        <td colspan="6" style="text-align: center; padding: 48px; color: var(--text-outline);">
                                            <span class="material-symbols-outlined" style="font-size:48px; margin-bottom: 8px;">receipt</span>
                                            <p>Chưa có hóa đơn nào được bán ra.</p>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </section>

                <!-- Order Detail Sidebar -->
                <aside class="history-detail-sidebar">
                    <div id="noOrderSelectedState" class="detail-placeholder">
                        <span class="material-symbols-outlined">receipt_long</span>
                        <h3 style="font-size: 18px; font-weight: 700; color: var(--text-main);">Chi tiết đơn hàng</h3>
                        <p style="font-size: 14px; margin-top: 4px;">Chọn một đơn hàng bên danh sách để xem chi tiết hóa đơn.</p>
                    </div>

                    <div id="orderDetailContent" class="detail-content hidden">
                        <!-- Detail Header -->
                        <div class="detail-header">
                            <div>
                                <h2 id="detailOrderCode">HD171203498</h2>
                                <p id="detailOrderDate">2026-06-30 00:54:12</p>
                            </div>
                            <button onclick="printInvoice()" class="btn-secondary" style="padding: 8px 12px; font-size: 12px;">
                                <span class="material-symbols-outlined" style="font-size: 16px;">print</span>
                                <span>In hóa đơn</span>
                            </button>
                        </div>

                        <!-- Meta Information -->
                        <div class="detail-meta">
                            <div class="meta-row">
                                <span class="meta-label">Thu ngân:</span>
                                <span id="detailEmployee" class="meta-value">Nguyễn Văn A</span>
                            </div>
                            <div class="meta-row">
                                <span class="meta-label">Khách hàng:</span>
                                <span id="detailCustomer" class="meta-value">Khách lẻ</span>
                            </div>
                            <div class="meta-row">
                                <span class="meta-label">Thanh toán:</span>
                                <span id="detailMethod" class="meta-value">Tiền mặt</span>
                            </div>
                        </div>

                        <!-- Items List -->
                        <div class="detail-items-section">
                            <h4 class="detail-section-title">Danh sách sản phẩm</h4>
                            <div id="detailItemsContainer" class="detail-items-list">
                                <!-- Dynamic items injected here -->
                            </div>
                        </div>

                        <!-- Financial Summary -->
                        <div class="detail-summary">
                            <div class="summary-row">
                                <span>Tạm tính:</span>
                                <span id="detailSubtotal">0 đ</span>
                            </div>
                            <div class="summary-row" style="color: var(--error);">
                                <span>Chiết khấu:</span>
                                <span id="detailDiscount">-0 đ</span>
                            </div>
                            <div class="summary-row total-row">
                                <span style="font-size: 16px; font-weight: 700;">Tổng cộng:</span>
                                <span id="detailTotal" class="summary-total-val">0 đ</span>
                            </div>
                        </div>
                    </div>
                </aside>
            </div>
        </main>
    </div>

    <!-- Hidden invoice print template (styled for standard 80mm thermal receipt) -->
    <div id="printReceiptTemplate" class="hidden">
        <style>
            @media print {
                body * {
                    visibility: hidden;
                }
                #printReceiptTemplate, #printReceiptTemplate * {
                    visibility: visible;
                }
                #printReceiptTemplate {
                    position: absolute;
                    left: 0;
                    top: 0;
                    width: 80mm;
                    font-family: 'Courier New', monospace;
                    font-size: 12px;
                    color: black;
                    padding: 5mm;
                }
                .text-center { text-align: center; }
                .text-right { text-align: right; }
                .bold { font-weight: bold; }
                .divider { border-top: 1px dashed black; margin: 5px 0; }
                table { width: 100%; border-collapse: collapse; }
                th, td { padding: 3px 0; }
            }
        </style>
        <div class="text-center">
            <h2 class="bold" style="font-size: 16px; margin: 0 0 5px;">LUMINA POS</h2>
            <p style="margin: 2px 0;">Hóa đơn bán hàng</p>
            <p id="printReceiptDate" style="margin: 2px 0;">Ngày: --/--/----</p>
            <p id="printReceiptCode" class="bold" style="margin: 2px 0;">HD123456</p>
        </div>
        <div class="divider"></div>
        <div style="margin: 5px 0;">
            <p style="margin: 2px 0;">Khách hàng: <span id="printReceiptCustomer">Khách lẻ</span></p>
            <p style="margin: 2px 0;">Thu ngân: <span id="printReceiptEmployee">Thu ngân #1</span></p>
        </div>
        <div class="divider"></div>
        <table>
            <thead>
                <tr>
                    <th class="bold" style="text-align: left;">Sản phẩm</th>
                    <th class="bold text-center">SL</th>
                    <th class="bold text-right">T.Tiền</th>
                </tr>
            </thead>
            <tbody id="printReceiptItems">
                <!-- Dynamic print rows -->
            </tbody>
        </table>
        <div class="divider"></div>
        <div style="margin: 5px 0; font-size: 13px;">
            <div style="display: flex; justify-content: space-between;">
                <span>Tạm tính:</span>
                <span id="printReceiptSubtotal">0 đ</span>
            </div>
            <div style="display: flex; justify-content: space-between;">
                <span>Chiết khấu:</span>
                <span id="printReceiptDiscount">-0 đ</span>
            </div>
            <div style="display: flex; justify-content: space-between;" class="bold">
                <span>TỔNG CỘNG:</span>
                <span id="printReceiptTotal">0 đ</span>
            </div>
        </div>
        <div class="divider"></div>
        <div class="text-center" style="margin-top: 15px;">
            <p style="font-style: italic;">Cảm ơn quý khách và hẹn gặp lại!</p>
        </div>
    </div>

    <script>
        let currentOrderDetails = [];
        let currentSelectedOrder = null;

        // 1. Search order history client-side
        const historySearchInput = document.getElementById("historySearchInput");
        historySearchInput.addEventListener("input", function() {
            const query = this.value.toLowerCase().trim();
            const rows = document.querySelectorAll(".order-row");
            rows.forEach(row => {
                const code = row.getAttribute("data-code").toLowerCase();
                if (code.includes(query)) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            });
        });

        // 2. Select order row to show details
        const orderRows = document.querySelectorAll(".order-row");
        orderRows.forEach(row => {
            row.addEventListener("click", function() {
                orderRows.forEach(r => r.classList.remove("active"));
                this.classList.add("active");

                const id = this.getAttribute("data-id");
                const code = this.getAttribute("data-code");
                const date = this.getAttribute("data-date");
                const customer = this.getAttribute("data-customer");
                const employee = this.getAttribute("data-employee");
                const subtotal = parseFloat(this.getAttribute("data-subtotal")) || 0;
                const discount = parseFloat(this.getAttribute("data-discount")) || 0;
                const total = parseFloat(this.getAttribute("data-total")) || 0;
                const method = this.getAttribute("data-method");

                currentSelectedOrder = { id, code, date, customer, employee, subtotal, discount, total, method };

                document.getElementById("detailOrderCode").innerText = code;
                document.getElementById("detailOrderDate").innerText = date;
                document.getElementById("detailEmployee").innerText = employee;
                document.getElementById("detailCustomer").innerText = customer;
                
                let methodText = "Tiền mặt";
                if (method === 'CARD') methodText = "Thẻ ngân hàng";
                if (method === 'TRANSFER') methodText = "Chuyển khoản";
                document.getElementById("detailMethod").innerText = methodText;

                document.getElementById("detailSubtotal").innerText = subtotal.toLocaleString('vi-VN') + " đ";
                document.getElementById("detailDiscount").innerText = "-" + discount.toLocaleString('vi-VN') + " đ";
                document.getElementById("detailTotal").innerText = total.toLocaleString('vi-VN') + " đ";

                fetchOrderDetails(id);
            });
        });

        async function fetchOrderDetails(orderId) {
            const container = document.getElementById("detailItemsContainer");
            container.innerHTML = `<div style="text-align: center; padding: 24px; color: var(--text-outline);">Đang tải chi tiết...</div>`;
            
            document.getElementById("noOrderSelectedState").style.display = "none";
            document.getElementById("orderDetailContent").style.display = "flex";

            try {
                const res = await fetch(`${pageContext.request.contextPath}/sales?action=getOrderDetail&orderId=\${orderId}`);
                const data = await res.json();
                currentOrderDetails = data;
                
                container.innerHTML = "";
                if (data.length === 0) {
                    container.innerHTML = `<div style="text-align: center; padding: 24px; color: var(--text-outline);">Không có sản phẩm nào.</div>`;
                    return;
                }

                data.forEach(item => {
                    const row = document.createElement("div");
                    row.className = "detail-item-row";
                    row.innerHTML = `
                        <div>
                            <div class="detail-item-name">\${item.productName}</div>
                            <div class="detail-item-qty">\${item.quantity} x \${item.unitPrice.toLocaleString('vi-VN')} đ</div>
                        </div>
                        <div class="detail-item-total">\${item.totalPrice.toLocaleString('vi-VN')} đ</div>
                    `;
                    container.appendChild(row);
                });
            } catch (err) {
                console.error("Lỗi tải chi tiết đơn hàng:", err);
                container.innerHTML = `<div style="text-align: center; padding: 24px; color: var(--error);">Lỗi tải dữ liệu. Vui lòng thử lại.</div>`;
            }
        }

        window.printInvoice = function() {
            if (!currentSelectedOrder) return;

            document.getElementById("printReceiptDate").innerText = "Ngày: " + currentSelectedOrder.date;
            document.getElementById("printReceiptCode").innerText = currentSelectedOrder.code;
            document.getElementById("printReceiptCustomer").innerText = currentSelectedOrder.customer;
            document.getElementById("printReceiptEmployee").innerText = currentSelectedOrder.employee;
            
            const tbody = document.getElementById("printReceiptItems");
            tbody.innerHTML = "";
            currentOrderDetails.forEach(item => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td style="text-align: left; max-width: 45mm; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">\${item.productName}</td>
                    <td class="text-center">\${item.quantity}</td>
                    <td class="text-right">\${item.totalPrice.toLocaleString('vi-VN')}</td>
                `;
                tbody.appendChild(tr);
            });

            document.getElementById("printReceiptSubtotal").innerText = currentSelectedOrder.subtotal.toLocaleString('vi-VN') + " đ";
            document.getElementById("printReceiptDiscount").innerText = "-" + currentSelectedOrder.discount.toLocaleString('vi-VN') + " đ";
            document.getElementById("printReceiptTotal").innerText = currentSelectedOrder.total.toLocaleString('vi-VN') + " đ";

            window.print();
        };

        window.addEventListener("DOMContentLoaded", () => {
            const firstRow = document.querySelector(".order-row");
            if (firstRow) {
                firstRow.click();
            }
        });
    </script>
</body>
</html>

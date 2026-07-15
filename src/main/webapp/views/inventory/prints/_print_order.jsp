<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%-- 
  ==========================================================================
  TRANG IN PHIẾU NHẬP/XUẤT (_print_order.jsp)
  - Cung cấp giao diện sạch sẽ, chỉ có text đen trắng và kẻ bảng để tối ưu hóa khi xuất bản in hóa đơn nhập/xuất ra máy in giấy.
  ==========================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>In Phiếu ${order.orderType == 'PURCHASE' ? 'Nhập Hàng' : 'Xuất Hàng'} ${order.orderCode}</title>
    <style>
        body { font-family: 'Times New Roman', serif; font-size: 14pt; line-height: 1.5; margin: 0; padding: 20px; }
        .header { text-align: center; margin-bottom: 30px; }
        .header h1 { font-size: 20pt; margin: 0 0 5px 0; text-transform: uppercase; }
        .header p { margin: 0; font-size: 12pt; font-style: italic; }
        .info-table { width: 100%; margin-bottom: 20px; border-collapse: collapse; }
        .info-table td { padding: 6px; vertical-align: top; }
        .info-table td.label { width: 150px; font-weight: bold; }
        .detail-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }
        .detail-table th, .detail-table td { border: 1px solid #000; padding: 8px; text-align: center; }
        .detail-table th { font-weight: bold; background-color: #f2f2f2; }
        .detail-table td.text-left { text-align: left; }
        .signatures { width: 100%; margin-top: 50px; page-break-inside: avoid; }
        .signatures td { width: 50%; text-align: center; vertical-align: top; }
        .signatures .sign-title { font-weight: bold; margin-bottom: 80px; }
        @media print {
            body { padding: 0; }
            .no-print { display: none; }
        }
    </style>
</head>
<body onload="window.print()">
    <div class="no-print" style="text-align: right; margin-bottom: 20px;">
        <button onclick="window.print()" style="padding: 8px 16px; font-size: 12pt; cursor: pointer; background: #007bff; color: white; border: none; border-radius: 4px;">🖨️ In Hóa Đơn</button>
        <button onclick="window.close()" style="padding: 8px 16px; font-size: 12pt; cursor: pointer; background: #6c757d; color: white; border: none; border-radius: 4px; margin-left: 8px;">Đóng</button>
    </div>

    <div class="header">
        <h1>PHIẾU ${order.orderType == 'PURCHASE' ? 'NHẬP HÀNG' : 'XUẤT HÀNG'}</h1>
        <p>Số: ${order.orderCode}</p>
        <p>
            Ngày lập: 
            <fmt:parseDate value="${order.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" var="parsedDateTime" type="both" />
            <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
        </p>
    </div>

    <table class="info-table">
        <tr>
            <td class="label">Chi Nhánh:</td>
            <td>${order.branchName}</td>
            <td class="label">Đối Tác:</td>
            <td>${not empty order.customerName ? order.customerName : 'Nhiều Nhà Cung Cấp / Vãng Lai'}</td>
        </tr>
        <tr>
            <td class="label">Người lập phiếu:</td>
            <td>${order.employeeName}</td>
            <td class="label">Trạng thái:</td>
            <td>
                <c:choose>
                    <c:when test="${order.status == 'PENDING'}">CHỜ DUYỆT</c:when>
                    <c:when test="${order.status == 'COMPLETED'}">HOÀN THÀNH</c:when>
                    <c:when test="${order.status == 'CANCELLED'}">ĐÃ HỦY</c:when>
                    <c:otherwise>${order.status}</c:otherwise>
                </c:choose>
            </td>
        </tr>
    </table>

    <h3 style="margin-bottom: 10px; font-size: 14pt;">DANH SÁCH HÀNG HÓA CHI TIẾT</h3>
    <table class="detail-table">
        <thead>
            <tr>
                <th width="50px">STT</th>
                <th>Tên Hàng Hóa / Mã Sản Phẩm</th>
                <th>Nhà Cung Cấp</th>
                <th width="150px">Đơn Giá</th>
                <th width="100px">Số Lượng</th>
                <th width="150px">Thành Tiền</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="d" items="${orderDetails}" varStatus="loop">
                <tr>
                    <td>${loop.index + 1}</td>
                    <td class="text-left">
                        ${d.productName}<br>
                        <small style="color: #666;">${d.productCode}</small>
                    </td>
                    <td>${not empty d.supplierName ? d.supplierName : '-'}</td>
                    <td><fmt:formatNumber value="${d.unitPrice}" type="currency" currencySymbol="₫"/></td>
                    <td><strong>${d.quantity}</strong></td>
                    <td><strong><fmt:formatNumber value="${d.totalPrice}" type="currency" currencySymbol="₫"/></strong></td>
                </tr>
            </c:forEach>
            <tr style="font-weight: bold; background-color: #f9f9f9;">
                <td colspan="5" style="text-align: right; padding: 10px;">Tổng tiền thanh toán:</td>
                <td style="color: red; font-size: 15pt;">
                    <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫"/>
                </td>
            </tr>
        </tbody>
    </table>

    <table class="signatures">
        <tr>
            <td>
                <div class="sign-title">Người lập phiếu</div>
                <div>(Ký, ghi rõ họ tên)</div>
            </td>
            <td>
                <div class="sign-title">Người nhận / Giao dịch</div>
                <div>(Ký, ghi rõ họ tên)</div>
            </td>
        </tr>
    </table>
</body>
</html>

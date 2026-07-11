<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>In Phiếu Điều Chuyển ${ticket.transferCode}</title>
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
        .signatures td { width: 33%; text-align: center; vertical-align: top; }
        .signatures .sign-title { font-weight: bold; margin-bottom: 80px; }
        @media print {
            body { padding: 0; }
            .no-print { display: none; }
        }
    </style>
</head>
<body onload="window.print()">
    <div class="no-print" style="text-align: right; margin-bottom: 20px;">
        <button onclick="window.print()" style="padding: 8px 16px; font-size: 12pt; cursor: pointer; background: #007bff; color: white; border: none; border-radius: 4px;">🖨️ In Phiếu Này</button>
        <button onclick="window.close()" style="padding: 8px 16px; font-size: 12pt; cursor: pointer; background: #6c757d; color: white; border: none; border-radius: 4px; margin-left: 8px;">Đóng</button>
    </div>

    <div class="header">
        <h1>PHIẾU ĐIỀU CHUYỂN KHO CHÉO</h1>
        <p>Số: ${ticket.transferCode}</p>
        <p>Ngày lập: <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${ticket.transferDate}" /></p>
    </div>

    <table class="info-table">
        <tr>
            <td class="label">Kho Xuất (Gửi):</td>
            <td>${ticket.fromWarehouseName}</td>
            <td class="label">Kho Nhận (Đích):</td>
            <td>${ticket.toWarehouseName}</td>
        </tr>
        <tr>
            <td class="label">Người lập phiếu:</td>
            <td>${ticket.createdByName}</td>
            <td class="label">Trạng thái phiếu:</td>
            <td>
                <c:choose>
                    <c:when test="${ticket.status == 'PENDING_DISPATCH'}">CHỜ DUYỆT</c:when>
                    <c:when test="${ticket.status == 'APPROVED_DISPATCH'}">CHỜ XUẤT KHO</c:when>
                    <c:when test="${ticket.status == 'IN_TRANSIT'}">ĐANG VẬN CHUYỂN</c:when>
                    <c:when test="${ticket.status == 'COMPLETED'}">HOÀN THÀNH</c:when>
                    <c:when test="${ticket.status == 'CANCELLED'}">ĐÃ HỦY</c:when>
                    <c:otherwise>${ticket.status}</c:otherwise>
                </c:choose>
            </td>
        </tr>
    </table>

    <h3 style="margin-bottom: 10px; font-size: 14pt;">DANH SÁCH SẢN PHẨM ĐIỀU CHUYỂN</h3>
    <table class="detail-table">
        <thead>
            <tr>
                <th width="50px">STT</th>
                <th>Tên Sản Phẩm</th>
                <th width="180px">Mã Vạch</th>
                <th width="120px">Số Lượng</th>
                <th width="120px">Đơn Vị</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="td" items="${ticketDetails}" varStatus="loop">
                <tr>
                    <td>${loop.index + 1}</td>
                    <td class="text-left">${td.productName}</td>
                    <td>${td.productCodebar}</td>
                    <td><strong>${td.quantity}</strong></td>
                    <td>${not empty td.unitName ? td.unitName : 'Cái'}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <c:if test="${not empty ticket.note}">
        <p><strong>Ghi chú:</strong> ${ticket.note}</p>
    </c:if>

    <table class="signatures">
        <tr>
            <td>
                <div class="sign-title">Người lập phiếu</div>
                <div>(Ký, ghi rõ họ tên)</div>
            </td>
            <td>
                <div class="sign-title">Thủ kho xuất</div>
                <div>(Ký, ghi rõ họ tên)</div>
            </td>
            <td>
                <div class="sign-title">Thủ kho nhận</div>
                <div>(Ký, ghi rõ họ tên)</div>
            </td>
        </tr>
    </table>
</body>
</html>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>In Phiếu ${ticket.ticketCode}</title>
    <style>
        body { font-family: 'Times New Roman', serif; font-size: 14pt; line-height: 1.5; margin: 0; padding: 20px; }
        .header { text-align: center; margin-bottom: 30px; }
        .header h1 { font-size: 20pt; margin: 0 0 5px 0; text-transform: uppercase; }
        .header p { margin: 0; font-size: 12pt; font-style: italic; }
        .info-table { width: 100%; margin-bottom: 20px; }
        .info-table td { padding: 5px; vertical-align: top; }
        .info-table td.label { width: 120px; font-weight: bold; }
        .detail-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }
        .detail-table th, .detail-table td { border: 1px solid #000; padding: 8px; text-align: center; }
        .detail-table th { font-weight: bold; background-color: #f0f0f0; }
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
        <button onclick="window.print()" style="padding: 8px 16px; font-size: 14pt; cursor: pointer;">🖨️ In Phiếu Này</button>
        <button onclick="window.close()" style="padding: 8px 16px; font-size: 14pt; cursor: pointer;">Đóng</button>
    </div>

    <div class="header">
        <h1>PHIẾU GIAO DỊCH CHÉO</h1>
        <p>Số: ${ticket.ticketCode}</p>
        <p>
            Ngày: 
            <fmt:parseDate value="${ticket.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
            <fmt:formatDate pattern="dd/MM/yyyy" value="${parsedDateTime}" />
        </p>
    </div>

    <table class="info-table">
        <tr>
            <td class="label">Kho Đề Xuất:</td>
            <td>${ticket.fromWarehouseName}</td>
            <td class="label">Kho Xử Lý:</td>
            <td>${ticket.toWarehouseName}</td>
        </tr>
        <tr>
            <td class="label">Người lập phiếu:</td>
            <td>${ticket.createdByName}</td>
            <td class="label">Trạng thái:</td>
            <td>
                <c:choose>
                    <c:when test="${ticket.status == 'PENDING'}">CHỜ DUYỆT</c:when>
                    <c:when test="${ticket.status == 'COMPLETED'}">ĐÃ DUYỆT</c:when>
                    <c:otherwise>${ticket.status}</c:otherwise>
                </c:choose>
            </td>
        </tr>
    </table>

    <c:choose>
        <c:when test="${ticket.ticketType == 'TRANSFER_REQUEST' && not empty txTicket && not empty tiTicket}">
            <h3 style="margin-bottom: 10px; font-size: 14pt;">1. CHI TIẾT XUẤT KHO (Từ: ${ticket.fromWarehouseName})</h3>
            <table class="detail-table">
                <thead>
                    <tr>
                        <th class="text-left">Sản Phẩm</th>
                        <th width="100px">Tồn Trước</th>
                        <th width="100px">Yêu Cầu</th>
                        <th width="120px">Thực Tế Xuất</th>
                        <th width="100px">Tồn Sau</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="td" items="${txDetails}">
                        <c:set var="myTx" value="${null}"/>
                        <c:if test="${not empty txTransactions}">
                            <c:forEach var="trx" items="${txTransactions}">
                                <c:if test="${trx.productId == td.productId}">
                                    <c:set var="myTx" value="${trx}"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <tr>
                            <td class="text-left">${td.productName}</td>
                            <c:choose>
                                <c:when test="${not empty myTx}">
                                    <td>${myTx.beforeQuantity}</td>
                                    <td>${td.quantity}</td>
                                    <td><strong>${td.actualQuantity != null ? td.actualQuantity : ''}</strong></td>
                                    <td><strong>${myTx.afterQuantity}</strong></td>
                                </c:when>
                                <c:otherwise>
                                    <td>${txCurrentStock[td.productId] != null ? txCurrentStock[td.productId] : '-'}</td>
                                    <td>${td.quantity}</td>
                                    <td><strong>${td.actualQuantity != null ? td.actualQuantity : ''}</strong></td>
                                    <td><strong>${txCurrentStock[td.productId] != null ? txCurrentStock[td.productId] - td.quantity : '-'}</strong></td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <div style="margin-bottom: 30px; font-size: 12pt;">
                <span>Nhân sự xuất: <strong>${txTicket.createdByName}</strong></span>
                <span style="margin-left: 20px;">Trạng thái: <strong>${txTicket.status == 'COMPLETED' ? 'Hoàn Tất' : (txTicket.status == 'COMPLETED_WITH_ERROR' ? 'Hoàn Tất (Lệch)' : txTicket.status)}</strong></span>
            </div>

            <h3 style="margin-bottom: 10px; font-size: 14pt;">2. CHI TIẾT NHẬP KHO (Đến: ${ticket.toWarehouseName})</h3>
            <table class="detail-table">
                <thead>
                    <tr>
                        <th class="text-left">Sản Phẩm</th>
                        <th width="100px">Tồn Trước</th>
                        <th width="100px">Yêu Cầu</th>
                        <th width="120px">Thực Tế Nhập</th>
                        <th width="100px">Tồn Sau</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="tid" items="${tiDetails}">
                        <c:set var="myTi" value="${null}"/>
                        <c:if test="${not empty tiTransactions}">
                            <c:forEach var="trx" items="${tiTransactions}">
                                <c:if test="${trx.productId == tid.productId}">
                                    <c:set var="myTi" value="${trx}"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <tr>
                            <td class="text-left">${tid.productName}</td>
                            <c:choose>
                                <c:when test="${not empty myTi}">
                                    <td>${myTi.beforeQuantity}</td>
                                    <td>${tid.quantity}</td>
                                    <td><strong>${tid.actualQuantity != null ? tid.actualQuantity : ''}</strong></td>
                                    <td><strong>${myTi.afterQuantity}</strong></td>
                                </c:when>
                                <c:otherwise>
                                    <td>${tiCurrentStock[tid.productId] != null ? tiCurrentStock[tid.productId] : '-'}</td>
                                    <td>${tid.quantity}</td>
                                    <td><strong>${tid.actualQuantity != null ? tid.actualQuantity : ''}</strong></td>
                                    <td><strong>${tiCurrentStock[tid.productId] != null ? tiCurrentStock[tid.productId] + tid.quantity : '-'}</strong></td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <div style="margin-bottom: 30px; font-size: 12pt;">
                <span>Nhân sự nhập: <strong>${tiTicket.createdByName}</strong></span>
                <span style="margin-left: 20px;">Trạng thái: <strong>${tiTicket.status == 'COMPLETED' ? 'Hoàn Tất' : (tiTicket.status == 'COMPLETED_WITH_ERROR' ? 'Hoàn Tất (Lệch)' : tiTicket.status)}</strong></span>
            </div>
        </c:when>
        <c:otherwise>
            <table class="detail-table">
                <thead>
                    <tr>
                        <th width="50px">STT</th>
                        <th class="text-left">Tên sản phẩm</th>
                        <th width="150px">Loại Giao Dịch</th>
                        <th width="100px">Số Lượng</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="d" items="${ticketDetails}" varStatus="loop">
                        <tr>
                            <td>${loop.index + 1}</td>
                            <td class="text-left">${d.productName}</td>
                            <td>${d.actionType == 'SEND' ? 'XUẤT' : 'NHẬP'}</td>
                            <td><strong>${d.quantity}</strong></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <table class="signatures">
        <tr>
            <td>
                <div class="sign-title">Người lập phiếu</div>
                <div>(Ký, họ tên)</div>
            </td>
            <td>
                <div class="sign-title">Đại diện Kho Đề Xuất</div>
                <div>(Ký, họ tên)</div>
            </td>
            <td>
                <div class="sign-title">Đại diện Kho Xử Lý</div>
                <div>(Ký, họ tên)</div>
            </td>
        </tr>
    </table>
</body>
</html>

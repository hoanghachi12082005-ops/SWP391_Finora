<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%-- 
  ==========================================================================
  TRANG IN PHIẾU ĐIỀU CHUYỂN KHO (_print_ticket.jsp)
  - Cung cấp giao diện in chuyên dùng cho phiếu chuyển kho (không màu mè, khoảng cách tối ưu để in hóa đơn giấy).
  ==========================================================================
--%>
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
            <td class="label">Kho Đề Xuất:</td>
            <td>${ticket.creatorBranchId == ticket.fromBranchId ? ticket.fromWarehouseName : ticket.toWarehouseName}</td>
            <td class="label">Người lập phiếu:</td>
            <td>${ticket.createdByName}</td>
        </tr>
        <tr>
            <td class="label">Trạng thái tổng hợp:</td>
            <td>
                <c:choose>
                    <c:when test="${ticket.displayStatus == 'PENDING_OWNER'}">CHỜ DUYỆT</c:when>
                    <c:when test="${ticket.displayStatus == 'PENDING_PARTNER'}">CHỜ ĐỐI TÁC DUYỆT</c:when>
                    <c:when test="${ticket.displayStatus == 'APPROVED_DISPATCH' || ticket.displayStatus == 'IN_PROGRESS'}">ĐANG XỬ LÝ</c:when>
                    <c:when test="${ticket.displayStatus == 'IN_TRANSIT'}">ĐANG TRUNG CHUYỂN</c:when>
                    <c:when test="${ticket.displayStatus == 'COMPLETED'}">HOÀN THÀNH</c:when>
                    <c:when test="${ticket.displayStatus == 'PARTIAL_COMPLETE'}">HOÀN THÀNH (CÓ LỖI)</c:when>
                    <c:when test="${ticket.displayStatus == 'CANCELLED'}">ĐÃ HỦY / BỊ TỪ CHỐI</c:when>
                    <c:otherwise>${ticket.displayStatus}</c:otherwise>
                </c:choose>
            </td>
            <td class="label"></td>
            <td></td>
        </tr>
    </table>

    <%
        model.StockTransfer ticket = (model.StockTransfer) request.getAttribute("ticket");
        java.util.List<model.StockTransfer> subTransfers = (java.util.List<model.StockTransfer>) request.getAttribute("subTransfers");
        
        java.util.Map<Integer, java.util.Map<String, Object>> groups = new java.util.LinkedHashMap<>();
        int ticketCreatorBranchId = (ticket != null) ? ticket.getCreatorBranchId() : 0;
        
        if (subTransfers != null) {
            for (model.StockTransfer sub : subTransfers) {
                boolean isExp = (sub.getFromBranchId() == ticketCreatorBranchId);
                int partnerId = isExp ? sub.getToWarehouseId() : sub.getFromWarehouseId();
                String partnerName = isExp ? sub.getToWarehouseName() : sub.getFromWarehouseName();
                
                java.util.Map<String, Object> group = groups.get(partnerId);
                if (group == null) {
                    group = new java.util.HashMap<>();
                    group.put("partnerId", partnerId);
                    group.put("partnerName", partnerName);
                    group.put("items", new java.util.ArrayList<java.util.Map<String, Object>>());
                    group.put("statuses", new java.util.ArrayList<String>());
                    group.put("repSub", sub);
                    groups.put(partnerId, group);
                }
                
                java.util.List<String> statuses = (java.util.List<String>) group.get("statuses");
                if (!statuses.contains(sub.getStatus())) {
                    statuses.add(sub.getStatus());
                }
                
                java.util.List<java.util.Map<String, Object>> items = (java.util.List<java.util.Map<String, Object>>) group.get("items");
                if (sub.getDetails() != null) {
                    for (model.StockTransferDetail d : sub.getDetails()) {
                        java.util.Map<String, Object> item = new java.util.HashMap<>();
                        item.put("productName", d.getProductName());
                        item.put("productCodebar", d.getProductCodebar());
                        item.put("quantity", d.getQuantity());
                        item.put("unitName", d.getUnitName());
                        item.put("direction", isExp ? "SEND" : "RECEIVE");
                        item.put("status", sub.getStatus());
                        items.add(item);
                    }
                }
            }
        }
        pageContext.setAttribute("partnerGroups", groups.values());
    %>

    <c:forEach var="group" items="${partnerGroups}">
        <div style="margin-top: 30px; page-break-inside: avoid;">
            <h3 style="font-size: 13pt; margin-bottom: 8px; font-weight: bold; border-bottom: 1px solid #000; padding-bottom: 4px;">
                🏢 Kho đối tác: ${group.partnerName} 
                <span style="font-size: 10pt; font-weight: normal; margin-left: 10px;">
                    (Trạng thái: 
                    <c:forEach var="status" items="${group.statuses}" varStatus="stLoop">
                        <c:choose>
                            <c:when test="${status == 'PENDING_OWNER'}">Chờ Duyệt</c:when>
                            <c:when test="${status == 'PENDING_PARTNER'}">Chờ đối tác duyệt</c:when>
                            <c:when test="${status == 'APPROVED_DISPATCH'}">Chờ xuất kho</c:when>
                            <c:when test="${status == 'IN_TRANSIT'}">Đang vận chuyển</c:when>
                            <c:when test="${status == 'COMPLETED'}">Hoàn thành</c:when>
                            <c:when test="${status == 'PARTIAL_COMPLETE'}">Hoàn thành (có lỗi)</c:when>
                            <c:when test="${status == 'PARTNER_REJECTED'}">Đối tác từ chối</c:when>
                            <c:when test="${status == 'REJECTED'}">Bị từ chối</c:when>
                            <c:when test="${status == 'CANCELLED'}">Đã hủy</c:when>
                            <c:otherwise>${status}</c:otherwise>
                        </c:choose>
                        <c:if test="${!stLoop.last}">, </c:if>
                    </c:forEach>
                    )
                </span>
            </h3>
            <table class="detail-table">
                <thead>
                    <tr>
                        <th width="50px">STT</th>
                        <th>Tên Sản Phẩm</th>
                        <th width="180px">Mã Vạch</th>
                        <th width="100px">Số Lượng</th>
                        <th width="100px">Đơn Vị</th>
                        <th width="100px">Phân Loại</th>
                        <th width="150px">Trạng Thái</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${group.items}" varStatus="loop">
                        <tr>
                            <td>${loop.index + 1}</td>
                            <td class="text-left">${item.productName}</td>
                            <td>${item.productCodebar}</td>
                            <td><strong>${item.quantity}</strong></td>
                            <td>${not empty item.unitName ? item.unitName : 'Cái'}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.direction == 'SEND'}">Xuất</c:when>
                                    <c:otherwise>Nhập</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.status == 'PENDING_OWNER'}">Chờ Owner duyệt</c:when>
                                    <c:when test="${item.status == 'PENDING_PARTNER'}">Chờ đối tác duyệt</c:when>
                                    <c:when test="${item.status == 'APPROVED_DISPATCH'}">Chờ xuất kho</c:when>
                                    <c:when test="${item.status == 'IN_TRANSIT'}">Đang vận chuyển</c:when>
                                    <c:when test="${item.status == 'COMPLETED'}">Hoàn thành</c:when>
                                    <c:when test="${item.status == 'PARTIAL_COMPLETE'}">Hoàn thành (có lỗi)</c:when>
                                    <c:when test="${item.status == 'PARTNER_REJECTED'}">Đối tác từ chối</c:when>
                                    <c:when test="${item.status == 'REJECTED'}">Bị từ chối</c:when>
                                    <c:when test="${item.status == 'CANCELLED'}">Đã hủy</c:when>
                                    <c:otherwise>${item.status}</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:forEach>

    <c:if test="${not empty ticket.note}">
        <p style="margin-top: 20px;"><strong>Ghi chú:</strong> ${ticket.note}</p>
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

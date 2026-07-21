<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div id="orderDetailModal" class="modal" style="display:none;">
    <div class="modal-overlay" onclick="closeOrderDetail()"></div>
    <div class="modal-content modal-lg">
        <div class="modal-header">
            <h3 id="modalOrderCode">Chi tiết đơn hàng</h3>
            <button type="button" class="modal-close" onclick="closeOrderDetail()">&times;</button>
        </div>
        <div class="modal-body" id="modalBody">
            <div class="detail-loading">Đang tải...</div>
        </div>
    </div>
</div>

<style>
.modal { position:fixed; inset:0; z-index:1000; display:flex; align-items:center; justify-content:center; }
.modal-overlay { position:absolute; inset:0; background:rgba(0,0,0,0.4); }
.modal-content { position:relative; background:#fff; border-radius:12px; max-width:800px; width:90%; max-height:85vh; overflow-y:auto; box-shadow:0 8px 32px rgba(0,0,0,0.15); }
.modal-lg { max-width:800px; }
.modal-header { display:flex; justify-content:space-between; align-items:center; padding:16px 20px; border-bottom:1px solid #e5e7eb; }
.modal-header h3 { margin:0; font-size:16px; font-weight:600; }
.modal-close { background:none; border:none; font-size:24px; cursor:pointer; color:#6b7280; padding:0; line-height:1; }
.modal-body { padding:20px; }
.detail-loading { text-align:center; padding:40px; color:#6b7280; }
.detail-grid { display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-bottom:20px; }
.detail-field { display:flex; flex-direction:column; }
.detail-field label { font-size:11px; color:#6b7280; font-weight:500; text-transform:uppercase; }
.detail-field span { font-size:14px; color:#111827; font-weight:500; }
.detail-table { width:100%; border-collapse:collapse; font-size:13px; }
.detail-table th { text-align:left; padding:8px 10px; background:#f9fafb; border-bottom:1px solid #e5e7eb; font-weight:600; color:#374151; }
.detail-table td { padding:8px 10px; border-bottom:1px solid #f3f4f6; }
.detail-table tfoot td { font-weight:600; border-top:2px solid #e5e7eb; }
</style>

<script>
function openOrderDetail(orderId) {
    var modal = document.getElementById('orderDetailModal');
    var body = document.getElementById('modalBody');
    modal.style.display = 'flex';
    body.innerHTML = '<div class="detail-loading">Đang tải...</div>';

    fetch('${pageContext.request.contextPath}/orders/detail?id=' + orderId)
        .then(function(r) { return r.json(); })
        .then(function(d) {
            if (d.error) { body.innerHTML = '<p class="detail-loading">' + d.error + '</p>'; return; }
            var statusMap = {PENDING:'Chờ thanh toán',PAID:'Đã thanh toán',COMPLETED:'Hoàn thành',CANCELLED:'Đã hủy'};
            document.getElementById('modalOrderCode').textContent = 'Đơn hàng ' + d.orderCode;

            var itemsHtml = '';
            for (var i = 0; i < d.items.length; i++) {
                var item = d.items[i];
                itemsHtml += '<tr><td>' + item.productName + '</td><td class="text-right">' + item.quantity + '</td><td class="text-right">' + fmt(item.unitPrice) + ' ₫</td><td class="text-right">' + fmt(item.totalPrice) + ' ₫</td></tr>';
            }

            body.innerHTML =
                '<div class="detail-grid">' +
                    '<div class="detail-field"><label>Mã đơn</label><span>' + d.orderCode + '</span></div>' +
                    '<div class="detail-field"><label>Ngày tạo</label><span>' + d.createdAt + '</span></div>' +
                    '<div class="detail-field"><label>Chi nhánh</label><span>' + d.branchName + '</span></div>' +
                    '<div class="detail-field"><label>Nhân viên</label><span>' + d.employeeName + '</span></div>' +
                    '<div class="detail-field"><label>Khách hàng</label><span>' + d.customerName + '</span></div>' +
                    '<div class="detail-field"><label>Phương thức</label><span>' + d.paymentMethod + '</span></div>' +
                    '<div class="detail-field"><label>Trạng thái</label><span>' + (statusMap[d.status] || d.status) + '</span></div>' +
                '</div>' +
                '<h4 style="font-size:14px;margin:16px 0 8px;font-weight:600;">Sản phẩm</h4>' +
                '<table class="detail-table"><thead><tr><th>Sản phẩm</th><th class="text-right">SL</th><th class="text-right">Đơn giá</th><th class="text-right">Thành tiền</th></tr></thead><tbody>' + itemsHtml + '</tbody>' +
                '<tfoot><tr><td colspan="3" class="text-right">Tạm tính</td><td class="text-right">' + fmt(d.subtotal) + ' ₫</td></tr>' +
                '<tr><td colspan="3" class="text-right">Giảm giá</td><td class="text-right">- ' + fmt(d.discountAmount) + ' ₫</td></tr>' +
                '<tr><td colspan="3" class="text-right"><strong>Tổng cộng</strong></td><td class="text-right"><strong>' + fmt(d.totalAmount) + ' ₫</strong></td></tr></tfoot></table>';
        })
        .catch(function() { body.innerHTML = '<p class="detail-loading">Lỗi tải dữ liệu.</p>'; });
}

function closeOrderDetail() {
    document.getElementById('orderDetailModal').style.display = 'none';
}

function fmt(n) { return n.toLocaleString('en-US'); }

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') closeOrderDetail();
});
</script>

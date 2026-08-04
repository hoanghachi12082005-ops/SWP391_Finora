package dto.inventory;

import model.PurchaseOrder;
import model.StockTransfer;
import java.util.Date;
import java.time.ZoneId;

/**
 * Wrapper DTO dùng để gộp cả Phiếu Nhập Hàng (PurchaseOrder) và Phiếu Điều Chuyển (StockTransfer)
 * vào cùng một danh sách hiển thị trên tab "Đơn Điều Chuyển", giúp sắp xếp chính xác theo thời gian (giảm dần).
 */
public class TransferTabItem {
    private String itemType; // "PO" hoặc "TRANSFER"
    private Date date;
    private PurchaseOrder purchaseOrder;
    private StockTransfer stockTransfer;

    public TransferTabItem(PurchaseOrder po) {
        this.itemType = "PO";
        if (po != null && po.getCreatedAt() != null) {
            this.date = Date.from(po.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        } else {
            this.date = new Date(0);
        }
        this.purchaseOrder = po;
    }

    public TransferTabItem(StockTransfer st) {
        this.itemType = "TRANSFER";
        if (st != null && st.getTransferDate() != null) {
            this.date = st.getTransferDate();
        } else {
            this.date = new Date(0);
        }
        this.stockTransfer = st;
    }

    public String getItemType() {
        return itemType;
    }

    public Date getDate() {
        return date;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public StockTransfer getStockTransfer() {
        return stockTransfer;
    }
}

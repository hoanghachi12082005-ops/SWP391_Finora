package dto.inventory;

import java.math.BigDecimal;
import java.util.List;

public class ImportProductDTO {
    private int productId;
    private String productName;
    private int myStock;
    private List<SupplierInfo> suppliers;

    public static class SupplierInfo {
        private int supplierId;
        private String supplierName;
        private BigDecimal importPrice;

        public SupplierInfo(int supplierId, String supplierName, BigDecimal importPrice) {
            this.supplierId = supplierId;
            this.supplierName = supplierName;
            this.importPrice = importPrice;
        }

        public int getSupplierId() { return supplierId; }
        public String getSupplierName() { return supplierName; }
        public BigDecimal getImportPrice() { return importPrice; }
        public void setImportPrice(BigDecimal importPrice) { this.importPrice = importPrice; }
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getMyStock() { return myStock; }
    public void setMyStock(int myStock) { this.myStock = myStock; }

    public List<SupplierInfo> getSuppliers() { return suppliers; }
    public void setSuppliers(List<SupplierInfo> suppliers) { this.suppliers = suppliers; }
}

package dto.inventory;

public class ExchangeProductDTO {
    private int productId;
    private String productName;
    private int myStock;
    private int partnerWarehouseId;
    private String partnerWarehouseName;
    private int partnerStock;
    private double sellingPrice;
    private double importPrice;

    public ExchangeProductDTO() {}

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    public double getImportPrice() { return importPrice; }
    public void setImportPrice(double importPrice) { this.importPrice = importPrice; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getMyStock() { return myStock; }
    public void setMyStock(int myStock) { this.myStock = myStock; }
    public int getPartnerWarehouseId() { return partnerWarehouseId; }
    public void setPartnerWarehouseId(int partnerWarehouseId) { this.partnerWarehouseId = partnerWarehouseId; }
    public String getPartnerWarehouseName() { return partnerWarehouseName; }
    public void setPartnerWarehouseName(String partnerWarehouseName) { this.partnerWarehouseName = partnerWarehouseName; }
    public int getPartnerStock() { return partnerStock; }
    public void setPartnerStock(int partnerStock) { this.partnerStock = partnerStock; }
}

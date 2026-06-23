package model;

    public class StockTransaction{
    private int productId;
private int fromStoreId;
private int toStoreId;
private int quantity;
private String transactionType;

        public StockTransaction() {}

    public int getProductId() { return productId; }
public void setProductId(int productId) { this.productId = productId; }
public int getFromStoreId() { return fromStoreId; }
public void setFromStoreId(int fromStoreId) { this.fromStoreId = fromStoreId; }
public int getToStoreId() { return toStoreId; }
public void setToStoreId(int toStoreId) { this.toStoreId = toStoreId; }
public int getQuantity() { return quantity; }
public void setQuantity(int quantity) { this.quantity = quantity; }
public String getTransactionType() { return transactionType; }
public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    }

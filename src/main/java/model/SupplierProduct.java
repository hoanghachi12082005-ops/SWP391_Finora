package model;

public class SupplierProduct {
    private int supplierID;
    private int productID;
    private double importPrice;

    public SupplierProduct() {}

    public SupplierProduct(int supplierID, int productID, double importPrice) {
        this.supplierID = supplierID;
        this.productID = productID;
        this.importPrice = importPrice;
    }

    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public double getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(double importPrice) {
        this.importPrice = importPrice;
    }
}

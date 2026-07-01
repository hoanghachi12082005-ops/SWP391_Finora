-- Script cập nhật cho Database V2: Bảng Product
-- Thêm cột id nhà cung cấp và giá nhập để hỗ trợ tính năng Nhập kho

-- 1. Thêm cột SupplierID và ImportPrice
ALTER TABLE Product
ADD SupplierID INT NULL,
    ImportPrice DECIMAL(18,2) DEFAULT 0;
GO

-- 2. Thêm khóa ngoại kết nối SupplierID với bảng Supplier
ALTER TABLE Product
ADD CONSTRAINT FK_Product_Supplier
    FOREIGN KEY (SupplierID)
    REFERENCES Supplier(SupplierID);
GO

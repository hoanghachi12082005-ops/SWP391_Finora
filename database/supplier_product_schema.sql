-- ============================================================
-- Migration: Tạo bảng SupplierProduct để quản lý quan hệ Nhiều-Nhiều
-- Mục đích: Lưu trữ thông tin sản phẩm cung cấp bởi mỗi nhà cung cấp
--           và đơn giá nhập đàm phán.
-- ============================================================

USE DBFinoraV3;
GO

IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'SupplierProduct') AND type = 'U')
BEGIN
    CREATE TABLE SupplierProduct (
        SupplierID  INT NOT NULL FOREIGN KEY REFERENCES Supplier(SupplierID),
        ProductID   INT NOT NULL FOREIGN KEY REFERENCES Product(ProductID),
        ImportPrice DECIMAL(18,2) DEFAULT 0,
        PRIMARY KEY (SupplierID, ProductID)
    );
    PRINT 'Đã tạo bảng SupplierProduct thành công.';
END
ELSE
BEGIN
    PRINT 'Bảng SupplierProduct đã tồn tại.';
END
GO

-- Seed dữ liệu mẫu cho SupplierProduct
-- Nhà cung cấp 1 (Công ty TNHH Thời Trang Việt) cung ứng Áo Thun (1), Áo Sơ mi (2), Quần jean (3)
IF EXISTS (SELECT 1 FROM Supplier WHERE SupplierID = 1) AND EXISTS (SELECT 1 FROM Product WHERE ProductID = 1)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM SupplierProduct WHERE SupplierID = 1 AND ProductID = 1)
        INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (1, 1, 100000.00);
    IF NOT EXISTS (SELECT 1 FROM SupplierProduct WHERE SupplierID = 1 AND ProductID = 2)
        INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (1, 2, 200000.00);
    IF NOT EXISTS (SELECT 1 FROM SupplierProduct WHERE SupplierID = 1 AND ProductID = 3)
        INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (1, 3, 250000.00);
END

-- Nhà cung cấp 2 (Công ty Mỹ Phẩm Á Châu) cung ứng Sữa rửa mặt (4), Combo chăm sóc cá nhân (8)
IF EXISTS (SELECT 1 FROM Supplier WHERE SupplierID = 2) AND EXISTS (SELECT 1 FROM Product WHERE ProductID = 4)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM SupplierProduct WHERE SupplierID = 2 AND ProductID = 4)
        INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (2, 4, 80000.00);
    IF NOT EXISTS (SELECT 1 FROM SupplierProduct WHERE SupplierID = 2 AND ProductID = 8)
        INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (2, 8, 300000.00);
END

-- Nhà cung cấp 3 (Nhà phân phối Phụ Kiện Fino) cung ứng Túi tote (6), Ví da (7)
IF EXISTS (SELECT 1 FROM Supplier WHERE SupplierID = 3) AND EXISTS (SELECT 1 FROM Product WHERE ProductID = 6)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM SupplierProduct WHERE SupplierID = 3 AND ProductID = 6)
        INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (3, 6, 120000.00);
    IF NOT EXISTS (SELECT 1 FROM SupplierProduct WHERE SupplierID = 3 AND ProductID = 7)
        INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (3, 7, 150000.00);
END
GO

PRINT 'Hoàn thành nạp dữ liệu mẫu SupplierProduct.';
GO

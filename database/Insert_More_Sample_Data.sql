USE DBFinoraV2
GO

-- 1. Insert 5 New Warehouses
DECLARE @BranchID INT = 1;
INSERT INTO warehouse (warehouse_name, branch_id, address, status, created_at) VALUES 
(N'Kho Miền Bắc 1', @BranchID, N'Từ Liêm, Hà Nội', 'ACTIVE', GETDATE()),
(N'Kho Miền Bắc 2', @BranchID, N'Long Biên, Hà Nội', 'ACTIVE', GETDATE()),
(N'Kho Miền Nam 1', @BranchID, N'Quận 1, HCM', 'ACTIVE', GETDATE()),
(N'Kho Miền Nam 2', @BranchID, N'Quận 7, HCM', 'ACTIVE', GETDATE()),
(N'Kho Miền Trung', @BranchID, N'Hải Châu, Đà Nẵng', 'ACTIVE', GETDATE());
GO

-- 2. Insert 15 New Products
DECLARE @CatID INT = 1; -- Assuming 1 is a valid category
DECLARE @UnitID INT = 1; -- Assuming 1 is a valid unit
INSERT INTO Product (Name, CategoryID, UnitID, SellingPrice, Quantity, Status, CreatedAt) VALUES 
(N'Áo Khoác Nam', @CatID, @UnitID, 500000, 100, 'ACTIVE', GETDATE()),
(N'Quần Kaki Nam', @CatID, @UnitID, 300000, 100, 'ACTIVE', GETDATE()),
(N'Váy Chữ A', @CatID, @UnitID, 250000, 100, 'ACTIVE', GETDATE()),
(N'Đầm Dự Tiệc', @CatID, @UnitID, 800000, 100, 'ACTIVE', GETDATE()),
(N'Áo Len Cổ Lọ', @CatID, @UnitID, 400000, 100, 'ACTIVE', GETDATE()),
(N'Quần Tây Nữ', @CatID, @UnitID, 350000, 100, 'ACTIVE', GETDATE()),
(N'Sơ Mi Trắng Nam', @CatID, @UnitID, 200000, 100, 'ACTIVE', GETDATE()),
(N'Sơ Mi Lụa Nữ', @CatID, @UnitID, 320000, 100, 'ACTIVE', GETDATE()),
(N'Giày Thể Thao', @CatID, @UnitID, 900000, 100, 'ACTIVE', GETDATE()),
(N'Giày Cao Gót', @CatID, @UnitID, 750000, 100, 'ACTIVE', GETDATE()),
(N'Thắt Lưng Da', @CatID, @UnitID, 150000, 100, 'ACTIVE', GETDATE()),
(N'Ví Nam Cầm Tay', @CatID, @UnitID, 450000, 100, 'ACTIVE', GETDATE()),
(N'Mũ Lưỡi Trai', @CatID, @UnitID, 120000, 100, 'ACTIVE', GETDATE()),
(N'Kính Mát Thời Trang', @CatID, @UnitID, 250000, 100, 'ACTIVE', GETDATE()),
(N'Đồng Hồ Nữ', @CatID, @UnitID, 1200000, 100, 'ACTIVE', GETDATE());
GO

-- 3. Populate Inventory table cross join
INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status)
SELECT w.warehouse_id, p.ProductID, ROUND(RAND(CHECKSUM(NEWID())) * 100, 0), 'ACTIVE'
FROM warehouse w CROSS JOIN Product p
WHERE NOT EXISTS (
    SELECT 1 FROM inventory i WHERE i.warehouse_id = w.warehouse_id AND i.product_id = p.ProductID
);
GO

PRINT N'Thêm thành công dữ liệu mẫu!';

USE DBFinoraV2;
GO

-- Drop foreign key for SupplierID if exists
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_Product_Supplier' AND parent_object_id = OBJECT_ID('Product'))
BEGIN
    ALTER TABLE Product DROP CONSTRAINT FK_Product_Supplier;
END
GO

-- Xoá các ràng buộc mặc định (Default Constraints) tự sinh trước khi drop cột
DECLARE @sql NVARCHAR(MAX);

-- 1. Xoá Constraint cho supplier_id
SELECT @sql = 'ALTER TABLE Product DROP CONSTRAINT [' + name + '];'
FROM sys.default_constraints 
WHERE parent_object_id = OBJECT_ID('Product') AND parent_column_id = COLUMNPROPERTY(OBJECT_ID('Product'), 'supplier_id', 'ColumnId');
IF @sql IS NOT NULL EXEC sp_executesql @sql;

-- 2. Xoá Constraint cho import_price
SET @sql = NULL;
SELECT @sql = 'ALTER TABLE Product DROP CONSTRAINT [' + name + '];'
FROM sys.default_constraints 
WHERE parent_object_id = OBJECT_ID('Product') AND parent_column_id = COLUMNPROPERTY(OBJECT_ID('Product'), 'import_price', 'ColumnId');
IF @sql IS NOT NULL EXEC sp_executesql @sql;

-- 3. Xoá Constraint cho SupplierID
SET @sql = NULL;
SELECT @sql = 'ALTER TABLE Product DROP CONSTRAINT [' + name + '];'
FROM sys.default_constraints 
WHERE parent_object_id = OBJECT_ID('Product') AND parent_column_id = COLUMNPROPERTY(OBJECT_ID('Product'), 'SupplierID', 'ColumnId');
IF @sql IS NOT NULL EXEC sp_executesql @sql;
GO

-- Drop các cột bị lỗi do trộn lẫn v3 và v2
IF COL_LENGTH('Product', 'supplier_id') IS NOT NULL
BEGIN
    ALTER TABLE Product DROP COLUMN supplier_id;
END
GO

IF COL_LENGTH('Product', 'import_price') IS NOT NULL
BEGIN
    ALTER TABLE Product DROP COLUMN import_price;
END
GO

IF COL_LENGTH('Product', 'SupplierID') IS NOT NULL
BEGIN
    ALTER TABLE Product DROP COLUMN SupplierID;
END
GO

-- Đảm bảo chỉ giữ lại đúng 1 cột ImportPrice
IF COL_LENGTH('Product', 'ImportPrice') IS NULL
BEGIN
    ALTER TABLE Product ADD ImportPrice DECIMAL(18,2) NULL;
END
GO

-- Thêm cột SupplierIDs dạng JSON
IF COL_LENGTH('Product', 'SupplierIDs') IS NULL
BEGIN
    ALTER TABLE Product ADD SupplierIDs NVARCHAR(500) NULL;
END
GO

-- Cập nhật dữ liệu mẫu (Sample data) cho giá nhập và ID nhà cung cấp
-- Ví dụ: ProductID 1,2,3... sẽ thuộc về nhà cung cấp 1, 2, hoặc cả 2

UPDATE Product SET ImportPrice = 25000000, SupplierIDs = '[1]' WHERE ProductID = 1;
UPDATE Product SET ImportPrice = 23000000, SupplierIDs = '[2]' WHERE ProductID = 2;
UPDATE Product SET ImportPrice = 35000000, SupplierIDs = '[1]' WHERE ProductID = 3;
UPDATE Product SET ImportPrice = 300000,   SupplierIDs = '[1,2]' WHERE ProductID = 4;
UPDATE Product SET ImportPrice = 5000000,  SupplierIDs = '[3]' WHERE ProductID = 5;

-- Cập nhật cho các sản phẩm còn lại (nếu có)
UPDATE Product SET ImportPrice = SellingPrice * 0.7, SupplierIDs = '[1,2,3]' 
WHERE ImportPrice IS NULL OR ImportPrice = 0;
GO

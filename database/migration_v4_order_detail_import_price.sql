-- ============================================================
-- Migration: Thêm import_price vào order_detail và xoá supplier_product
-- ============================================================

USE DBFinoraV3;
GO

-- 1. Thêm cột import_price vào bảng order_detail nếu chưa có
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'order_detail') AND name = N'import_price')
BEGIN
    ALTER TABLE order_detail ADD import_price DECIMAL(18,2) NOT NULL DEFAULT 0.0;
    PRINT 'Đã thêm cột import_price vào bảng order_detail.';
END
ELSE
BEGIN
    PRINT 'Cột import_price đã tồn tại trong bảng order_detail.';
END
GO

-- 2. Xoá bảng supplier_product nếu tồn tại
IF EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'supplier_product') AND type = 'U')
BEGIN
    DROP TABLE supplier_product;
    PRINT 'Đã xoá bảng supplier_product thành công.';
END
ELSE
BEGIN
    PRINT 'Bảng supplier_product không tồn tại hoặc đã bị xoá.';
END
GO

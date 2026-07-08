-- ============================================================
-- MIGRATION: REMOVE SUPPLIER_PRODUCT TABLE & ADD IMPORT_PRICE
-- ============================================================

-- 1. Thêm cột import_price vào bảng order_detail nếu chưa tồn tại
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'order_detail') AND name = 'import_price')
BEGIN
    ALTER TABLE order_detail ADD import_price DECIMAL(18,2) DEFAULT 0;
    PRINT 'Đã thêm cột import_price vào bảng order_detail.';
END
ELSE
BEGIN
    PRINT 'Cột import_price đã tồn tại trong bảng order_detail.';
END
GO

-- 2. Xóa bảng supplier_product nếu tồn tại
IF EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'supplier_product') AND type = 'U')
BEGIN
    DROP TABLE supplier_product;
    PRINT 'Đã xóa bảng supplier_product.';
END
ELSE
BEGIN
    PRINT 'Bảng supplier_product không tồn tại hoặc đã được xóa trước đó.';
END
GO

-- 3. Tạo dữ liệu mẫu giao dịch nhập kho để làm lịch sử giá chéo phong phú
-- Nhập kho từ Nhà Cung Cấp 1 (supplier_id = 1)
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 1) 
   AND EXISTS (SELECT 1 FROM product WHERE product_id = 1)
   AND NOT EXISTS (SELECT 1 FROM [order] WHERE order_code = 'IMP-SEED-001')
BEGIN
    INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
    VALUES ('IMP-SEED-001', 'PURCHASE', 1, 1, 1, 1, 550000.00, 0, 550000.00, 'BANK_TRANSFER', 'COMPLETED', GETDATE() - 5);

    DECLARE @OrderId1 INT = SCOPE_IDENTITY();

    INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price)
    VALUES (@OrderId1, 1, 1, 100000.00, 100000.00, 100000.00), -- Áo Thun giá 100k
           (@OrderId1, 2, 1, 200000.00, 200000.00, 200000.00), -- Áo Sơ mi giá 200k
           (@OrderId1, 3, 1, 250000.00, 250000.00, 250000.00); -- Quần Jean giá 250k
           
    PRINT 'Đã chèn dữ liệu mẫu cho Đơn nhập kho 1.';
END

-- Nhập kho từ Nhà Cung Cấp 2 (supplier_id = 2) - Đợt 1
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 2) 
   AND EXISTS (SELECT 1 FROM product WHERE product_id = 4)
   AND NOT EXISTS (SELECT 1 FROM [order] WHERE order_code = 'IMP-SEED-002')
BEGIN
    INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
    VALUES ('IMP-SEED-002', 'PURCHASE', 2, 1, 1, 1, 80000.00, 0, 80000.00, 'BANK_TRANSFER', 'COMPLETED', GETDATE() - 4);

    DECLARE @OrderId2 INT = SCOPE_IDENTITY();

    INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price)
    VALUES (@OrderId2, 4, 1, 80000.00, 80000.00, 80000.00); -- Sữa rửa mặt giá 80k

    PRINT 'Đã chèn dữ liệu mẫu cho Đơn nhập kho 2.';
END

-- Nhập kho từ Nhà Cung Cấp 3 (supplier_id = 3) - Đợt 1
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 3) 
   AND EXISTS (SELECT 1 FROM product WHERE product_id = 6)
   AND NOT EXISTS (SELECT 1 FROM [order] WHERE order_code = 'IMP-SEED-003')
BEGIN
    INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
    VALUES ('IMP-SEED-003', 'PURCHASE', 3, 1, 1, 1, 120000.00, 0, 120000.00, 'BANK_TRANSFER', 'COMPLETED', GETDATE() - 3);

    DECLARE @OrderId3 INT = SCOPE_IDENTITY();

    INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price)
    VALUES (@OrderId3, 6, 1, 120000.00, 120000.00, 120000.00); -- Túi tote giá 120k

    PRINT 'Đã chèn dữ liệu mẫu cho Đơn nhập kho 3.';
END

-- ĐƠN HÀNG MỚI ĐỂ LÀM GIÁ CHÉO:
-- Nhập kho từ Nhà Cung Cấp 2 (supplier_id = 2) - Đợt 2 (Chéo sản phẩm và đổi giá)
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 2) 
   AND EXISTS (SELECT 1 FROM product WHERE product_id = 1)
   AND NOT EXISTS (SELECT 1 FROM [order] WHERE order_code = 'IMP-SEED-004')
BEGIN
    INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
    VALUES ('IMP-SEED-004', 'PURCHASE', 2, 1, 1, 1, 420000.00, 0, 420000.00, 'BANK_TRANSFER', 'COMPLETED', GETDATE() - 2);

    DECLARE @OrderId4 INT = SCOPE_IDENTITY();

    INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price)
    VALUES (@OrderId4, 1, 1, 105000.00, 105000.00, 105000.00), -- Áo Thun giá 105k từ NCC 2
           (@OrderId4, 2, 1, 190000.00, 190000.00, 190000.00), -- Áo Sơ mi giá 190k từ NCC 2
           (@OrderId4, 6, 1, 125000.00, 125000.00, 125000.00); -- Túi tote giá 125k từ NCC 2

    PRINT 'Đã chèn dữ liệu mẫu cho Đơn nhập kho 4 (Chéo giá NCC 2).';
END

-- Nhập kho từ Nhà Cung Cấp 3 (supplier_id = 3) - Đợt 2 (Chéo sản phẩm và đổi giá)
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 3) 
   AND EXISTS (SELECT 1 FROM product WHERE product_id = 1)
   AND NOT EXISTS (SELECT 1 FROM [order] WHERE order_code = 'IMP-SEED-005')
BEGIN
    INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
    VALUES ('IMP-SEED-005', 'PURCHASE', 3, 1, 1, 1, 343000.00, 0, 343000.00, 'BANK_TRANSFER', 'COMPLETED', GETDATE() - 1);

    DECLARE @OrderId5 INT = SCOPE_IDENTITY();

    INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price)
    VALUES (@OrderId5, 1, 1, 98000.00, 98000.00, 98000.00), -- Áo Thun giá 98k từ NCC 3
           (@OrderId5, 3, 1, 245000.00, 245000.00, 245000.00); -- Quần Jean giá 245k từ NCC 3

    PRINT 'Đã chèn dữ liệu mẫu cho Đơn nhập kho 5 (Chéo giá NCC 3).';
END

-- Nhập kho từ Nhà Cung Cấp 1 (supplier_id = 1) - Đợt 2 (Chéo sản phẩm khác)
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 1) 
   AND EXISTS (SELECT 1 FROM product WHERE product_id = 4)
   AND NOT EXISTS (SELECT 1 FROM [order] WHERE order_code = 'IMP-SEED-006')
BEGIN
    INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
    VALUES ('IMP-SEED-006', 'PURCHASE', 1, 1, 1, 1, 85000.00, 0, 85000.00, 'BANK_TRANSFER', 'COMPLETED', GETDATE());

    DECLARE @OrderId6 INT = SCOPE_IDENTITY();

    INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price)
    VALUES (@OrderId6, 4, 1, 85000.00, 85000.00, 85000.00); -- Sữa rửa mặt giá 85k từ NCC 1

    PRINT 'Đã chèn dữ liệu mẫu cho Đơn nhập kho 6 (Chéo giá NCC 1).';
END
GO

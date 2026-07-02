-- ============================================================
-- Migration: Cập nhật DBFinoraV3 (snake_case)
-- Mục đích: Bù đắp các cột/bảng phục vụ tính năng Khóa tài khoản, 
--           Sổ quỹ và Sản phẩm cung cấp theo định dạng snake_case.
-- ============================================================

USE DBFinoraV3;
GO

-- 1. Thêm cột failed_login_count vào bảng Employee nếu chưa có
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Employee') AND name = N'failed_login_count')
BEGIN
    ALTER TABLE Employee ADD failed_login_count INT NOT NULL DEFAULT 0;
    PRINT 'Đã thêm cột failed_login_count vào bảng Employee.';
END
ELSE
BEGIN
    PRINT 'Cột failed_login_count đã tồn tại trong bảng Employee.';
END
GO

-- 2. Cập nhật bảng payment
-- Cho phép order_id NULL (giao dịch thu chi thủ công)
ALTER TABLE payment ALTER COLUMN order_id INT NULL;
GO

-- Thêm cột payment_type
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'payment') AND name = N'payment_type')
BEGIN
    ALTER TABLE payment ADD payment_type NVARCHAR(20) NULL;
    PRINT 'Đã thêm cột payment_type vào bảng payment.';
END
GO

-- Thêm cột description
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'payment') AND name = N'description')
BEGIN
    ALTER TABLE payment ADD description NVARCHAR(500) NULL;
    PRINT 'Đã thêm cột description vào bảng payment.';
END
GO

-- Thêm cột emp_id
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'payment') AND name = N'emp_id')
BEGIN
    ALTER TABLE payment ADD emp_id INT NULL FOREIGN KEY REFERENCES Employee(emp_id);
    PRINT 'Đã thêm cột emp_id vào bảng payment.';
END
GO

-- Thêm cột branch_id
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'payment') AND name = N'branch_id')
BEGIN
    ALTER TABLE payment ADD branch_id INT NULL FOREIGN KEY REFERENCES Branch(branch_id);
    PRINT 'Đã thêm cột branch_id vào bảng payment.';
END
GO

-- Cập nhật dữ liệu cũ của bảng payment
UPDATE payment 
SET 
    payment_type = 'INCOME', 
    description = N'Thanh toán đơn hàng' 
WHERE payment_type IS NULL;
GO

-- Đổi cột sang NOT NULL và thêm CHECK constraint
ALTER TABLE payment ALTER COLUMN payment_type NVARCHAR(20) NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE parent_object_id = OBJECT_ID(N'payment') AND type = 'C' AND name LIKE '%CK%payment_type%')
BEGIN
    ALTER TABLE payment ADD CONSTRAINT CK_payment_payment_type CHECK (payment_type IN ('INCOME', 'EXPENSE'));
    PRINT 'Đã tạo CHECK constraint cho payment_type.';
END
GO

-- 3. Tạo bảng supplier_product
IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'supplier_product') AND type = 'U')
BEGIN
    CREATE TABLE supplier_product (
        supplier_id INT NOT NULL FOREIGN KEY REFERENCES supplier(supplier_id),
        product_id  INT NOT NULL FOREIGN KEY REFERENCES product(product_id),
        import_price DECIMAL(18,2) DEFAULT 0,
        PRIMARY KEY (supplier_id, product_id)
    );
    PRINT 'Đã tạo bảng supplier_product thành công.';
END
ELSE
BEGIN
    PRINT 'Bảng supplier_product đã tồn tại.';
END
GO

-- Seed dữ liệu mẫu cho supplier_product
-- Nhà cung cấp 1 cung ứng Áo Thun (1), Áo Sơ mi (2), Quần jean (3)
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 1) AND EXISTS (SELECT 1 FROM product WHERE product_id = 1)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM supplier_product WHERE supplier_id = 1 AND product_id = 1)
        INSERT INTO supplier_product (supplier_id, product_id, import_price) VALUES (1, 1, 100000.00);
    IF NOT EXISTS (SELECT 1 FROM supplier_product WHERE supplier_id = 1 AND product_id = 2)
        INSERT INTO supplier_product (supplier_id, product_id, import_price) VALUES (1, 2, 200000.00);
    IF NOT EXISTS (SELECT 1 FROM supplier_product WHERE supplier_id = 1 AND product_id = 3)
        INSERT INTO supplier_product (supplier_id, product_id, import_price) VALUES (1, 3, 250000.00);
END

-- Nhà cung cấp 2 cung ứng Sữa rửa mặt (4), Combo chăm sóc cá nhân (8)
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 2) AND EXISTS (SELECT 1 FROM product WHERE product_id = 4)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM supplier_product WHERE supplier_id = 2 AND product_id = 4)
        INSERT INTO supplier_product (supplier_id, product_id, import_price) VALUES (2, 4, 80000.00);
    IF NOT EXISTS (SELECT 1 FROM supplier_product WHERE supplier_id = 2 AND product_id = 8)
        INSERT INTO supplier_product (supplier_id, product_id, import_price) VALUES (2, 8, 300000.00);
END

-- Nhà cung cấp 3 cung ứng Túi tote (6), Ví da (7)
IF EXISTS (SELECT 1 FROM supplier WHERE supplier_id = 3) AND EXISTS (SELECT 1 FROM product WHERE product_id = 6)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM supplier_product WHERE supplier_id = 3 AND product_id = 6)
        INSERT INTO supplier_product (supplier_id, product_id, import_price) VALUES (3, 6, 120000.00);
    IF NOT EXISTS (SELECT 1 FROM supplier_product WHERE supplier_id = 3 AND product_id = 7)
        INSERT INTO supplier_product (supplier_id, product_id, import_price) VALUES (3, 7, 150000.00);
END
GO

PRINT 'Migration hoàn tất thành công.';
GO

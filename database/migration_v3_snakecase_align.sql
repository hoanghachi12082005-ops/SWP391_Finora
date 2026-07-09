-- ============================================================
-- Migration: Cập nhật DBFinoraV3 (snake_case)
-- Mục đích: Bù đắp các cột/bảng phục vụ tính năng Khóa tài khoản, 
--           Sổ quỹ và Sản phẩm cung cấp theo định dạng snake_case.
-- ============================================================

USE DBFinoraV3;
GO

-- 1. Xóa cột failed_login_count và FailedLoginCount khỏi bảng Employee nếu có
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Employee') AND name = N'failed_login_count')
BEGIN
    ALTER TABLE Employee DROP COLUMN failed_login_count;
    PRINT 'Đã xóa cột failed_login_count khỏi bảng Employee.';
END
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Employee') AND name = N'FailedLoginCount')
BEGIN
    ALTER TABLE Employee DROP COLUMN FailedLoginCount;
    PRINT 'Đã xóa cột FailedLoginCount khỏi bảng Employee.';
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

PRINT 'Migration hoàn tất thành công.';
GO


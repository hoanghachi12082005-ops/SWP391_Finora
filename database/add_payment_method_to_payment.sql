-- =============================================
-- Script: Thêm cột payment_method vào bảng payment
-- Mục đích: Hỗ trợ phân loại thu/chi theo phương thức (CASH, BANK_TRANSFER)
-- =============================================
USE [DBFinoraV3];
GO

-- Bước 1: Thêm cột payment_method vào payment (nếu chưa có)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[payment]') AND name = 'payment_method')
BEGIN
    ALTER TABLE [payment] ADD payment_method NVARCHAR(50) NULL
        CONSTRAINT CK_Payment_PaymentMethod CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'BANKING', 'CARD'));
    PRINT N'Đã thêm cột payment_method vào bảng payment.';
END
ELSE
    PRINT N'Cột payment_method đã tồn tại.';
GO

-- Bước 2: Cập nhật dữ liệu cũ (batch riêng để thấy cột mới)
UPDATE p
SET p.payment_method = o.payment_method
FROM payment p
LEFT JOIN [order] o ON p.order_id = o.order_id
WHERE p.payment_method IS NULL;
GO

-- Với các bản ghi không có order_id, mặc định là CASH
UPDATE payment
SET payment_method = 'CASH'
WHERE payment_method IS NULL;
GO

-- Đặt NOT NULL sau khi đã cập nhật dữ liệu
ALTER TABLE [payment] ALTER COLUMN payment_method NVARCHAR(50) NOT NULL;
GO

PRINT N'Hoàn tất: cột payment_method đã được thêm và cập nhật dữ liệu.';

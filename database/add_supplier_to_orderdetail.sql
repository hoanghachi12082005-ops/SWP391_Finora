-- =============================================
-- Script: Thêm cột supplier_id và supplier_status vào order_detail
-- Mục đích: Hỗ trợ 1 Phiếu Nhập có nhiều Nhà Cung Cấp
-- =============================================

-- Thêm cột supplier_id vào order_detail
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[order_detail]') AND name = 'supplier_id')
BEGIN
    ALTER TABLE [order_detail] ADD supplier_id INT NULL;
    ALTER TABLE [order_detail] ADD CONSTRAINT FK_OrderDetail_Supplier FOREIGN KEY (supplier_id) REFERENCES [supplier](supplier_id);
    PRINT N'Đã thêm cột supplier_id vào order_detail.';
END
ELSE
    PRINT N'Cột supplier_id đã tồn tại.';

-- Thêm cột supplier_status vào order_detail
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[order_detail]') AND name = 'supplier_status')
BEGIN
    ALTER TABLE [order_detail] ADD supplier_status VARCHAR(50) DEFAULT 'PENDING';
    PRINT N'Đã thêm cột supplier_status vào order_detail.';
END
ELSE
    PRINT N'Cột supplier_status đã tồn tại.';

PRINT N'Script hoàn tất.';

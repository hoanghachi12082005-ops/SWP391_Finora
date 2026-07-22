-- Migration: Thêm category_id vào bảng vat_setting để hỗ trợ VAT theo ngành hàng
-- Ngày: 2026-07-22

-- 1. Thêm cột category_id (NULL = VAT mặc định cho tất cả ngành hàng)
ALTER TABLE [dbo].[vat_setting] ADD [category_id] [int] NULL;
GO

-- 2. Thêm FK constraint tham chiếu đến bảng category
ALTER TABLE [dbo].[vat_setting] WITH CHECK ADD FOREIGN KEY([category_id])
REFERENCES [dbo].[category] ([category_id]);
GO

-- 3. Add unique constraint để mỗi category chỉ có 1 VAT setting
CREATE UNIQUE NONCLUSTERED INDEX [IX_vat_setting_category] ON [dbo].[vat_setting] ([category_id])
WHERE [category_id] IS NOT NULL;
GO

-- Migration: Xoá trigger audit log cho các bảng nghiệp vụ không cần thiết
-- Lý do: Dữ liệu đơn hàng, thanh toán, tồn kho, chuyển kho đã có thể xem
--        được ở các tính năng tương ứng, không cần ghi audit log riêng.
-- Ngày: 2026-07-22

-- Xoá trigger cũ (nếu còn tồn tại)
DROP TRIGGER IF EXISTS [dbo].[trg_voucher_audit];
GO
DROP TRIGGER IF EXISTS [dbo].[trg_order_audit];
GO
DROP TRIGGER IF EXISTS [dbo].[trg_payment_audit];
GO
DROP TRIGGER IF EXISTS [dbo].[trg_inventory_audit];
GO
DROP TRIGGER IF EXISTS [dbo].[trg_stock_transfer_audit];
GO
DROP TRIGGER IF EXISTS [dbo].[trg_stock_transaction_audit];
GO

PRINT N'=== Da xoa 6 trigger audit log cho bang nghiep vu thanh cong! ===';
GO

-- ============================================================
-- Migration: Add status column to product table + trigger
-- Khi category chuyển sang INACTIVE -> product cũng INACTIVE theo
-- ============================================================
USE DBFinoraV3;
GO

-- 1. Thêm cột status cho bảng product
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'product') AND name = N'status'
)
BEGIN
    ALTER TABLE product
    ADD status NVARCHAR(20) DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE'));
    
    PRINT N'Đã thêm cột status cho bảng product.';
END
ELSE
    PRINT N'Cột status đã tồn tại.';
GO

-- Set mặc định ACTIVE cho các product hiện có
UPDATE product SET status = 'ACTIVE' WHERE status IS NULL;
GO

-- 2. Trigger: khi category update status, cập nhật product theo
CREATE OR ALTER TRIGGER trg_Category_Update_ProductStatus
ON category
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Chỉ xử lý khi cột status bị thay đổi
    IF UPDATE(status)
    BEGIN
        -- Nếu category chuyển sang INACTIVE, set product.status = 'INACTIVE'
        UPDATE p
        SET p.status = i.status,
            p.update_at = GETDATE()
        FROM product p
        INNER JOIN inserted i ON p.category_id = i.category_id
        WHERE i.status = 'INACTIVE'
          AND (p.status IS NULL OR p.status <> 'INACTIVE');

        -- Nếu category chuyển từ INACTIVE -> ACTIVE, KHÔNG tự động set product về ACTIVE
        -- (để tránh active lại những product đã bị inactive thủ công)
        -- Chỉ log ra nếu cần
    END
END;
GO

PRINT N'Tạo trigger trg_Category_Update_ProductStatus thành công.';
GO

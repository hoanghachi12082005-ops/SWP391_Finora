-- ============================================================
-- Migration: Thêm cột failed_login_count vào bảng Employee
-- Mục đích: Đếm số lần đăng nhập sai liên tiếp.
--           Khi đạt 5 lần, tài khoản bị chuyển sang INACTIVE.
--           Admin mở lại bằng cách set status = 'ACTIVE',
--           đồng thời reset cột này về 0.
-- ============================================================

-- Bước 1: Thêm cột failed_login_count nếu chưa có
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'Employee')
    AND name = N'FailedLoginCount'
)
BEGIN
    ALTER TABLE Employee
    ADD FailedLoginCount INT NOT NULL DEFAULT 0;

    PRINT 'Đã thêm cột FailedLoginCount vào bảng Employee.';
END
ELSE
BEGIN
    PRINT 'Cột FailedLoginCount đã tồn tại, bỏ qua.';
END
GO

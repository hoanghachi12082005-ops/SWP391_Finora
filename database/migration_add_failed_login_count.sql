-- ============================================================
-- Migration: Xoá cột failed_login_count và FailedLoginCount khỏi bảng Employee
-- Mục đích: Lưu số lần đăng nhập sai bằng session thay vì database.
-- ============================================================

USE DBFinoraV3;
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Employee') AND name = N'failed_login_count')
BEGIN
    ALTER TABLE Employee DROP COLUMN failed_login_count;
    PRINT 'Đã xóa cột failed_login_count khỏi bảng Employee.';
END
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Employee') AND name = N'FailedLoginCount')
BEGIN
    ALTER TABLE Employee DROP COLUMN FailedLoginCount;
    PRINT 'Đã xóa cột FailedLoginCount khỏi bảng Employee.';
END
GO

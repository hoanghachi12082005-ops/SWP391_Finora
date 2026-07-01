-- ============================================================
-- Migration: Cập nhật bảng Payment để hỗ trợ Sổ Quỹ (Cashbook)
-- Mục đích: Cho phép OrderID NULL (giao dịch thu chi thủ công),
--           Thêm cột PaymentType (INCOME/EXPENSE), Description, 
--           EmployeeID, BranchID. Cập nhật dữ liệu cũ.
-- ============================================================

USE DBFinoraV3;
GO

-- 1. Cho phép OrderID NULL
ALTER TABLE Payment ALTER COLUMN OrderID INT NULL;
GO

-- 2. Thêm cột PaymentType nếu chưa có
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Payment') AND name = N'PaymentType')
BEGIN
    ALTER TABLE Payment ADD PaymentType NVARCHAR(20) NULL;
END
GO

-- 3. Thêm cột Description nếu chưa có
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Payment') AND name = N'Description')
BEGIN
    ALTER TABLE Payment ADD Description NVARCHAR(500) NULL;
END
GO

-- 4. Thêm cột EmployeeID nếu chưa có
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Payment') AND name = N'EmployeeID')
BEGIN
    ALTER TABLE Payment ADD EmployeeID INT NULL FOREIGN KEY REFERENCES Employee(EmployeeID);
END
GO

-- 5. Thêm cột BranchID nếu chưa có
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'Payment') AND name = N'BranchID')
BEGIN
    ALTER TABLE Payment ADD BranchID INT NULL FOREIGN KEY REFERENCES Branch(BranchID);
END
GO

-- 6. Cập nhật dữ liệu cũ từ bảng Order
UPDATE p
SET 
    p.PaymentType = CASE WHEN o.OrderType = 'SALE' THEN 'INCOME' ELSE 'EXPENSE' END,
    p.Description = N'Thanh toán đơn hàng ' + o.OrderCode,
    p.EmployeeID = o.EmployeeID,
    p.BranchID = o.BranchID
FROM Payment p
INNER JOIN [Order] o ON p.OrderID = o.OrderID;
GO

-- Đối với bất kỳ thanh toán nào không khớp (nếu có), đặt giá trị mặc định để không bị NULL
UPDATE Payment 
SET 
    PaymentType = 'INCOME',
    Description = N'Giao dịch thu nhập hệ thống'
WHERE PaymentType IS NULL;
GO

-- 7. Thiết lập PaymentType thành NOT NULL và thêm CHECK constraint
ALTER TABLE Payment ALTER COLUMN PaymentType NVARCHAR(20) NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE parent_object_id = OBJECT_ID(N'Payment') AND type = 'C' AND name LIKE '%CK%PaymentType%')
BEGIN
    ALTER TABLE Payment ADD CONSTRAINT CK_Payment_PaymentType CHECK (PaymentType IN ('INCOME', 'EXPENSE'));
END
GO

PRINT 'Migration hoàn tất thành công.';
GO

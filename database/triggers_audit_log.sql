-- ============================================================
-- AUDIT LOG TRIGGERS - DBFinoraV3
-- Mô tả: Ghi lại mọi thay đổi (INSERT/UPDATE/DELETE) vào bảng AuditLog
--        với EmployeeID lấy tự động từ session context.
-- Cách dùng: Trước khi thực hiện các thao tác SQL trong 1 request,
--            App gọi lệnh này MỘT LẦN:
--     EXEC sp_set_session_context N'EmployeeID', @EmployeeID;
-- ============================================================
USE DBFinoraV3;
GO

-- ============================================================
-- HELPER: Lấy EmployeeID từ session context (set bởi app)
-- ============================================================

-- ============================================================
-- TRIGGER: Role
-- ============================================================
CREATE OR ALTER TRIGGER trg_Role_Audit
ON [Role]
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Action NVARCHAR(20);
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    -- INSERT
    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        SET @Action = 'INSERT';
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, @Action, 'Role', i.RoleID, NULL,
               (SELECT i.Name, i.Description FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    -- DELETE
    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        SET @Action = 'DELETE';
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, @Action, 'Role', d.RoleID,
               (SELECT d.Name, d.Description FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    -- UPDATE
    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        SET @Action = 'UPDATE';
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, @Action, 'Role', i.RoleID,
               (SELECT d.Name AS old_Name, d.Description AS old_Description FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Name AS new_Name, i.Description AS new_Description FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.RoleID = d.RoleID
        -- Chỉ ghi log nếu có thay đổi thực sự
        WHERE (i.Name <> d.Name OR (i.Name IS NULL AND d.Name IS NOT NULL) OR (i.Name IS NOT NULL AND d.Name IS NULL))
           OR (i.Description <> d.Description OR (i.Description IS NULL AND d.Description IS NOT NULL) OR (i.Description IS NOT NULL AND d.Description IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: Branch
-- ============================================================
CREATE OR ALTER TRIGGER trg_Branch_Audit
ON Branch
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Branch', i.BranchID, NULL,
               (SELECT i.Name, i.BranchCode, i.Address, i.Phone, i.Email, i.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Branch', d.BranchID,
               (SELECT d.Name, d.BranchCode, d.Address, d.Phone, d.Email, d.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Branch', i.BranchID,
               (SELECT d.Name AS old_Name, d.Status AS old_Status, d.Phone AS old_Phone, d.Email AS old_Email, d.Address AS old_Address
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Name AS new_Name, i.Status AS new_Status, i.Phone AS new_Phone, i.Email AS new_Email, i.Address AS new_Address
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.BranchID = d.BranchID
        WHERE (i.Name <> d.Name OR (i.Name IS NULL AND d.Name IS NOT NULL) OR (i.Name IS NOT NULL AND d.Name IS NULL))
           OR (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL))
           OR (i.Phone <> d.Phone OR (i.Phone IS NULL AND d.Phone IS NOT NULL) OR (i.Phone IS NOT NULL AND d.Phone IS NULL))
           OR (i.Email <> d.Email OR (i.Email IS NULL AND d.Email IS NOT NULL) OR (i.Email IS NOT NULL AND d.Email IS NULL))
           OR (i.Address <> d.Address OR (i.Address IS NULL AND d.Address IS NOT NULL) OR (i.Address IS NOT NULL AND d.Address IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: Employee
-- ============================================================
CREATE OR ALTER TRIGGER trg_Employee_Audit
ON Employee
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Employee', i.EmployeeID, NULL,
               (SELECT i.FullName, i.Email, i.Phone, i.RoleID, i.BranchID, i.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Employee', d.EmployeeID,
               (SELECT d.FullName, d.Email, d.Phone, d.RoleID, d.BranchID, d.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Employee', i.EmployeeID,
               (SELECT d.FullName AS old_FullName, d.Email AS old_Email, d.Phone AS old_Phone, d.Status AS old_Status, d.RoleID AS old_RoleID, d.BranchID AS old_BranchID
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.FullName AS new_FullName, i.Email AS new_Email, i.Phone AS new_Phone, i.Status AS new_Status, i.RoleID AS new_RoleID, i.BranchID AS new_BranchID
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.EmployeeID = d.EmployeeID
        WHERE (i.FullName <> d.FullName OR (i.FullName IS NULL AND d.FullName IS NOT NULL) OR (i.FullName IS NOT NULL AND d.FullName IS NULL))
           OR (i.Email <> d.Email OR (i.Email IS NULL AND d.Email IS NOT NULL) OR (i.Email IS NOT NULL AND d.Email IS NULL))
           OR (i.Phone <> d.Phone OR (i.Phone IS NULL AND d.Phone IS NOT NULL) OR (i.Phone IS NOT NULL AND d.Phone IS NULL))
           OR (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL))
           OR (i.RoleID <> d.RoleID OR (i.RoleID IS NULL AND d.RoleID IS NOT NULL) OR (i.RoleID IS NOT NULL AND d.RoleID IS NULL))
           OR (i.BranchID <> d.BranchID OR (i.BranchID IS NULL AND d.BranchID IS NOT NULL) OR (i.BranchID IS NOT NULL AND d.BranchID IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: Customer
-- ============================================================
CREATE OR ALTER TRIGGER trg_Customer_Audit
ON Customer
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Customer', i.CustomerID, NULL,
               (SELECT i.FullName, i.Email, i.Phone, i.CustomerType, i.TotalSpent
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Customer', d.CustomerID,
               (SELECT d.FullName, d.Email, d.Phone, d.CustomerType, d.TotalSpent
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Customer', i.CustomerID,
               (SELECT d.FullName AS old_FullName, d.Email AS old_Email, d.Phone AS old_Phone, d.CustomerType AS old_CustomerType, d.TotalSpent AS old_TotalSpent
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.FullName AS new_FullName, i.Email AS new_Email, i.Phone AS new_Phone, i.CustomerType AS new_CustomerType, i.TotalSpent AS new_TotalSpent
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.CustomerID = d.CustomerID
        WHERE (i.FullName <> d.FullName OR (i.FullName IS NULL AND d.FullName IS NOT NULL) OR (i.FullName IS NOT NULL AND d.FullName IS NULL))
           OR (i.Email <> d.Email OR (i.Email IS NULL AND d.Email IS NOT NULL) OR (i.Email IS NOT NULL AND d.Email IS NULL))
           OR (i.Phone <> d.Phone OR (i.Phone IS NULL AND d.Phone IS NOT NULL) OR (i.Phone IS NOT NULL AND d.Phone IS NULL))
           OR (i.CustomerType <> d.CustomerType OR (i.CustomerType IS NULL AND d.CustomerType IS NOT NULL) OR (i.CustomerType IS NOT NULL AND d.CustomerType IS NULL))
           OR (i.TotalSpent <> d.TotalSpent);
    END
END;
GO

-- ============================================================
-- TRIGGER: Product
-- ============================================================
CREATE OR ALTER TRIGGER trg_Product_Audit
ON Product
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Product', i.ProductID, NULL,
               (SELECT i.Name, i.Quantity, i.CategoryID, i.UnitID, i.SellingPrice, i.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Product', d.ProductID,
               (SELECT d.Name, d.Quantity, d.CategoryID, d.UnitID, d.SellingPrice, d.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Product', i.ProductID,
               (SELECT d.Name AS old_Name, d.Quantity AS old_Quantity, d.SellingPrice AS old_SellingPrice, d.Status AS old_Status, d.CategoryID AS old_CategoryID
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Name AS new_Name, i.Quantity AS new_Quantity, i.SellingPrice AS new_SellingPrice, i.Status AS new_Status, i.CategoryID AS new_CategoryID
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.ProductID = d.ProductID
        WHERE (i.Name <> d.Name OR (i.Name IS NULL AND d.Name IS NOT NULL) OR (i.Name IS NOT NULL AND d.Name IS NULL))
           OR (i.Quantity <> d.Quantity)
           OR (i.SellingPrice <> d.SellingPrice)
           OR (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL))
           OR (i.CategoryID <> d.CategoryID OR (i.CategoryID IS NULL AND d.CategoryID IS NOT NULL) OR (i.CategoryID IS NOT NULL AND d.CategoryID IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: [Order]
-- ============================================================
CREATE OR ALTER TRIGGER trg_Order_Audit
ON [Order]
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Order', i.OrderID, NULL,
               (SELECT i.OrderCode, i.OrderType, i.CustomerID, i.BranchID, i.EmployeeID, i.TotalAmount, i.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Order', d.OrderID,
               (SELECT d.OrderCode, d.OrderType, d.CustomerID, d.BranchID, d.EmployeeID, d.TotalAmount, d.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Order', i.OrderID,
               (SELECT d.Status AS old_Status, d.TotalAmount AS old_TotalAmount, d.EmployeeID AS old_EmployeeID
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Status AS new_Status, i.TotalAmount AS new_TotalAmount, i.EmployeeID AS new_EmployeeID
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.OrderID = d.OrderID
        WHERE (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL))
           OR (i.TotalAmount <> d.TotalAmount)
           OR (i.EmployeeID <> d.EmployeeID OR (i.EmployeeID IS NULL AND d.EmployeeID IS NOT NULL) OR (i.EmployeeID IS NOT NULL AND d.EmployeeID IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: Payment
-- ============================================================
CREATE OR ALTER TRIGGER trg_Payment_Audit
ON Payment
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Payment', i.PaymentID, NULL,
               (SELECT i.OrderID, i.PaymentMethod, i.PaymentAmount, i.PaymentStatus, i.TransactionCode, i.PaymentType
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Payment', d.PaymentID,
               (SELECT d.OrderID, d.PaymentMethod, d.PaymentAmount, d.PaymentStatus, d.TransactionCode, d.PaymentType
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Payment', i.PaymentID,
               (SELECT d.PaymentStatus AS old_PaymentStatus, d.PaymentAmount AS old_Amount
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.PaymentStatus AS new_PaymentStatus, i.PaymentAmount AS new_Amount
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.PaymentID = d.PaymentID
        WHERE (i.PaymentStatus <> d.PaymentStatus OR (i.PaymentStatus IS NULL AND d.PaymentStatus IS NOT NULL) OR (i.PaymentStatus IS NOT NULL AND d.PaymentStatus IS NULL))
           OR (i.PaymentAmount <> d.PaymentAmount);
    END
END;
GO

-- ============================================================
-- TRIGGER: Inventory (Stock level changes)
-- ============================================================
CREATE OR ALTER TRIGGER trg_Inventory_Audit
ON Inventory
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Inventory', i.InventoryID, NULL,
               (SELECT i.WarehouseID, i.ProductID, i.QuantityInStock
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Inventory', d.InventoryID,
               (SELECT d.WarehouseID, d.ProductID, d.QuantityInStock
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Inventory', i.InventoryID,
               (SELECT d.QuantityInStock AS old_Quantity FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.QuantityInStock AS new_Quantity FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.InventoryID = d.InventoryID
        WHERE (i.QuantityInStock <> d.QuantityInStock);
    END
END;
GO

-- ============================================================
-- TRIGGER: StockTransfer
-- ============================================================
CREATE OR ALTER TRIGGER trg_StockTransfer_Audit
ON StockTransfer
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'StockTransfer', i.StockTransferID, NULL,
               (SELECT i.TransferCode, i.FromWarehouseID, i.ToWarehouseID, i.Status, i.TotalAmount
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'StockTransfer', d.StockTransferID,
               (SELECT d.TransferCode, d.FromWarehouseID, d.ToWarehouseID, d.Status, d.TotalAmount
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'StockTransfer', i.StockTransferID,
               (SELECT d.Status AS old_Status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Status AS new_Status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.StockTransferID = d.StockTransferID
        WHERE (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: Cashbook
-- ============================================================
CREATE OR ALTER TRIGGER trg_Cashbook_Audit
ON Cashbook
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Cashbook', i.CashbookID, NULL,
               (SELECT i.BranchID, i.TransactionType, i.Amount, i.BalanceBefore, i.BalanceAfter, i.Description
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Cashbook', d.CashbookID,
               (SELECT d.BranchID, d.TransactionType, d.Amount, d.BalanceBefore, d.BalanceAfter, d.Description
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Cashbook', i.CashbookID,
               (SELECT d.Amount AS old_Amount, d.Description AS old_Description
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Amount AS new_Amount, i.Description AS new_Description
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.CashbookID = d.CashbookID
        WHERE (i.Amount <> d.Amount)
           OR (i.Description <> d.Description OR (i.Description IS NULL AND d.Description IS NOT NULL) OR (i.Description IS NOT NULL AND d.Description IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: Voucher
-- ============================================================
CREATE OR ALTER TRIGGER trg_Voucher_Audit
ON Voucher
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Voucher', i.VoucherID, NULL,
               (SELECT i.VoucherCode, i.VoucherName, i.DiscountType, i.DiscountValue, i.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Voucher', d.VoucherID,
               (SELECT d.VoucherCode, d.VoucherName, d.DiscountType, d.DiscountValue, d.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Voucher', i.VoucherID,
               (SELECT d.Status AS old_Status, d.DiscountValue AS old_Value FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Status AS new_Status, i.DiscountValue AS new_Value FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.VoucherID = d.VoucherID
        WHERE (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL))
           OR (i.DiscountValue <> d.DiscountValue);
    END
END;
GO

-- ============================================================
-- TRIGGER: Supplier
-- ============================================================
CREATE OR ALTER TRIGGER trg_Supplier_Audit
ON Supplier
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Supplier', i.SupplierID, NULL,
               (SELECT i.Name, i.Phone, i.Address, i.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Supplier', d.SupplierID,
               (SELECT d.Name, d.Phone, d.Address, d.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Supplier', i.SupplierID,
               (SELECT d.Name AS old_Name, d.Status AS old_Status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Name AS new_Name, i.Status AS new_Status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.SupplierID = d.SupplierID
        WHERE (i.Name <> d.Name OR (i.Name IS NULL AND d.Name IS NOT NULL) OR (i.Name IS NOT NULL AND d.Name IS NULL))
           OR (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: Category
-- ============================================================
CREATE OR ALTER TRIGGER trg_Category_Audit
ON Category
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Category', i.CategoryID, NULL,
               (SELECT i.Name, i.Description, i.ParentCategoryID, i.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Category', d.CategoryID,
               (SELECT d.Name, d.Description, d.ParentCategoryID, d.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Category', i.CategoryID,
               (SELECT d.Name AS old_Name, d.Status AS old_Status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Name AS new_Name, i.Status AS new_Status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.CategoryID = d.CategoryID
        WHERE (i.Name <> d.Name OR (i.Name IS NULL AND d.Name IS NOT NULL) OR (i.Name IS NOT NULL AND d.Name IS NULL))
           OR (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: Warehouse
-- ============================================================
CREATE OR ALTER TRIGGER trg_Warehouse_Audit
ON Warehouse
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'INSERT', 'Warehouse', i.WarehouseID, NULL,
               (SELECT i.Name, i.BranchID, i.Address, i.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'DELETE', 'Warehouse', d.WarehouseID,
               (SELECT d.Name, d.BranchID, d.Address, d.Status
                FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE()
        FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
        SELECT @EmployeeID, 'UPDATE', 'Warehouse', i.WarehouseID,
               (SELECT d.Name AS old_Name, d.Status AS old_Status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.Name AS new_Name, i.Status AS new_Status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.WarehouseID = d.WarehouseID
        WHERE (i.Name <> d.Name OR (i.Name IS NULL AND d.Name IS NOT NULL) OR (i.Name IS NOT NULL AND d.Name IS NULL))
           OR (i.Status <> d.Status OR (i.Status IS NULL AND d.Status IS NOT NULL) OR (i.Status IS NOT NULL AND d.Status IS NULL));
    END
END;
GO

-- ============================================================
-- TRIGGER: StockTransaction
-- ============================================================
CREATE OR ALTER TRIGGER trg_StockTransaction_Audit
ON StockTransaction
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @EmployeeID INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
    SELECT @EmployeeID, 'INSERT', 'StockTransaction', i.StockTransactionID, NULL,
           (SELECT i.WarehouseID, i.ProductID, i.ReferenceType, i.ReferenceID, i.TransactionType, i.Quantity, i.BeforeQuantity, i.AfterQuantity
            FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
           GETDATE()
    FROM inserted i;
END;
GO

PRINT '=== All audit log triggers created successfully! ===';
GO

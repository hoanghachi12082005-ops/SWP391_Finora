-- ============================================================
-- AUDIT LOG TRIGGERS - DBFinoraV3
-- Ghi INSERT/UPDATE/DELETE vao bang `audit_log`.
-- App goi 1 lan truoc khi thao tac:
--     EXEC sp_set_session_context N'EmployeeID', @emp_id;
-- ============================================================
USE DBFinoraV3;
GO

-- ============================================================
-- Ensure product table has status column (used by audit trigger)
-- ============================================================
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
GO

-- Set mặc định ACTIVE cho các product hiện có
UPDATE product SET status = 'ACTIVE' WHERE status IS NULL;
GO

-- ============================================================
-- Helper: kiem tra thay doi thuc su cho UPDATE
-- ============================================================
CREATE OR ALTER FUNCTION fn_has_changes(@old NVARCHAR(MAX), @new NVARCHAR(MAX))
RETURNS BIT
AS
BEGIN
    RETURN CASE WHEN @old <> @new THEN 1 ELSE 0 END
END;
GO

-- ============================================================
-- 1. role
-- ============================================================
CREATE OR ALTER TRIGGER trg_role_audit ON [role]
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'role', i.role_id, NULL,
               (SELECT i.role_id, i.role_name, i.discription FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'role', d.role_id,
               (SELECT d.role_id, d.role_name, d.discription FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'role', i.role_id,
               (SELECT d.role_id, d.role_name, d.discription FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.role_id, i.role_name, i.discription FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.role_id = d.role_id
        WHERE dbo.fn_has_changes(
            (SELECT d.role_name, d.discription FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.role_name, i.discription FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 2. branch
-- ============================================================
CREATE OR ALTER TRIGGER trg_branch_audit ON branch
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'branch', i.branch_id, NULL,
               (SELECT i.branch_id, i.branch_name, i.status, i.phone, i.email FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'branch', d.branch_id,
               (SELECT d.branch_id, d.branch_name, d.status, d.phone, d.email FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'branch', i.branch_id,
               (SELECT d.branch_name, d.status, d.phone, d.email FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.branch_name, i.status, i.phone, i.email FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.branch_id = d.branch_id
        WHERE dbo.fn_has_changes(
            (SELECT d.branch_name, d.status, d.phone, d.email FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.branch_name, i.status, i.phone, i.email FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 3. employee
-- ============================================================
CREATE OR ALTER TRIGGER trg_employee_audit ON employee
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'employee', i.emp_id, NULL,
               (SELECT i.emp_id, i.fullName, i.email, i.phone, i.status, i.role_id, i.branch_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'employee', d.emp_id,
               (SELECT d.emp_id, d.fullName, d.email, d.phone, d.status, d.role_id, d.branch_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'employee', i.emp_id,
               (SELECT d.fullName, d.email, d.phone, d.status, d.role_id, d.branch_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.fullName, i.email, i.phone, i.status, i.role_id, i.branch_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.emp_id = d.emp_id
        WHERE dbo.fn_has_changes(
            (SELECT d.fullName, d.email, d.phone, d.status, d.role_id, d.branch_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.fullName, i.email, i.phone, i.status, i.role_id, i.branch_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 4. customer
-- ============================================================
CREATE OR ALTER TRIGGER trg_customer_audit ON customer
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'customer', i.cus_id, NULL,
               (SELECT i.cus_id, i.full_name, i.email, i.phone, i.total_spent, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'customer', d.cus_id,
               (SELECT d.cus_id, d.full_name, d.email, d.phone, d.total_spent, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'customer', i.cus_id,
               (SELECT d.full_name, d.email, d.phone, d.total_spent, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.full_name, i.email, i.phone, i.total_spent, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.cus_id = d.cus_id
        WHERE dbo.fn_has_changes(
            (SELECT d.full_name, d.email, d.phone, d.total_spent, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.full_name, i.email, i.phone, i.total_spent, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 5. category
-- ============================================================
CREATE OR ALTER TRIGGER trg_category_audit ON category
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'category', i.category_id, NULL,
               (SELECT i.category_id, i.category_name, i.description, i.parent_category_id, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'category', d.category_id,
               (SELECT d.category_id, d.category_name, d.description, d.parent_category_id, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'category', i.category_id,
               (SELECT d.category_name, d.description, d.parent_category_id, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.category_name, i.description, i.parent_category_id, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.category_id = d.category_id
        WHERE dbo.fn_has_changes(
            (SELECT d.category_name, d.description, d.parent_category_id, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.category_name, i.description, i.parent_category_id, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 6. product
-- ============================================================
CREATE OR ALTER TRIGGER trg_product_audit ON product
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'product', i.product_id, NULL,
               (SELECT i.product_id, i.product_name, i.category_id, i.unit_id, i.selling_price, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'product', d.product_id,
               (SELECT d.product_id, d.product_name, d.category_id, d.unit_id, d.selling_price, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'product', i.product_id,
               (SELECT d.product_name, d.category_id, d.unit_id, d.selling_price, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.product_name, i.category_id, i.unit_id, i.selling_price, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.product_id = d.product_id
        WHERE dbo.fn_has_changes(
            (SELECT d.product_name, d.category_id, d.unit_id, d.selling_price, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.product_name, i.category_id, i.unit_id, i.selling_price, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 7. supplier
-- ============================================================
CREATE OR ALTER TRIGGER trg_supplier_audit ON supplier
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'supplier', i.supplier_id, NULL,
               (SELECT i.supplier_id, i.supplier_name, i.phone_number, i.address, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'supplier', d.supplier_id,
               (SELECT d.supplier_id, d.supplier_name, d.phone_number, d.address, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'supplier', i.supplier_id,
               (SELECT d.supplier_name, d.phone_number, d.address, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.supplier_name, i.phone_number, i.address, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.supplier_id = d.supplier_id
        WHERE dbo.fn_has_changes(
            (SELECT d.supplier_name, d.phone_number, d.address, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.supplier_name, i.phone_number, i.address, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 8. warehouse
-- ============================================================
CREATE OR ALTER TRIGGER trg_warehouse_audit ON warehouse
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'warehouse', i.warehouse_id, NULL,
               (SELECT i.warehouse_id, i.warehouse_name, i.branch_id, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'warehouse', d.warehouse_id,
               (SELECT d.warehouse_id, d.warehouse_name, d.branch_id, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'warehouse', i.warehouse_id,
               (SELECT d.warehouse_name, d.branch_id, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.warehouse_name, i.branch_id, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.warehouse_id = d.warehouse_id
        WHERE dbo.fn_has_changes(
            (SELECT d.warehouse_name, d.branch_id, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.warehouse_name, i.branch_id, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 9. voucher -- DA XOA (khong can thiet)
-- ============================================================

-- ============================================================
-- 10-14. Cac trigger cho bang nghiep vu (order, payment, inventory,
--        stock_transfer, stock_transaction) DA DUOC XOA bo nham giam
--        bot log khong can thiet. Du lieu cac bang nay da co the xem
--        duoc o cac tinh nang tuong ung (quan ly don hang, kho, v.v.)
-- ============================================================

PRINT N'=== Tat ca trigger audit log da tao thanh cong! ===';
GO
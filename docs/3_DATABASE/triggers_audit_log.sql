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
-- 9. voucher
-- ============================================================
CREATE OR ALTER TRIGGER trg_voucher_audit ON voucher
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'voucher', i.voucher_id, NULL,
               (SELECT i.voucher_id, i.voucher_code, i.voucher_name, i.discount_type, i.discount_value, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'voucher', d.voucher_id,
               (SELECT d.voucher_id, d.voucher_code, d.voucher_name, d.discount_type, d.discount_value, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'voucher', i.voucher_id,
               (SELECT d.voucher_code, d.voucher_name, d.discount_type, d.discount_value, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.voucher_code, i.voucher_name, i.discount_type, i.discount_value, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.voucher_id = d.voucher_id
        WHERE dbo.fn_has_changes(
            (SELECT d.voucher_code, d.voucher_name, d.discount_type, d.discount_value, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.voucher_code, i.voucher_name, i.discount_type, i.discount_value, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 10. [order]
-- ============================================================
CREATE OR ALTER TRIGGER trg_order_audit ON [order]
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'order', i.order_id, NULL,
               (SELECT i.order_code, i.order_type, i.customer_id, i.branch_id, i.emp_id, i.total_amount, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'order', d.order_id,
               (SELECT d.order_code, d.order_type, d.customer_id, d.branch_id, d.emp_id, d.total_amount, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'order', i.order_id,
               (SELECT d.status, d.total_amount, d.emp_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.status, i.total_amount, i.emp_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.order_id = d.order_id
        WHERE dbo.fn_has_changes(
            (SELECT d.status, d.total_amount, d.emp_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.status, i.total_amount, i.emp_id FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 11. payment
-- ============================================================
CREATE OR ALTER TRIGGER trg_payment_audit ON payment
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'payment', i.payment_id, NULL,
               (SELECT i.order_id, i.payment_amount, i.payment_status, i.transaction_code FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'payment', d.payment_id,
               (SELECT d.order_id, d.payment_amount, d.payment_status, d.transaction_code FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'payment', i.payment_id,
               (SELECT d.payment_amount, d.payment_status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.payment_amount, i.payment_status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.payment_id = d.payment_id
        WHERE dbo.fn_has_changes(
            (SELECT d.payment_amount, d.payment_status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.payment_amount, i.payment_status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 12. inventory
-- ============================================================
CREATE OR ALTER TRIGGER trg_inventory_audit ON inventory
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'inventory', i.inventory_id, NULL,
               (SELECT i.warehouse_id, i.product_id, i.quantity_in_stock, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'inventory', d.inventory_id,
               (SELECT d.warehouse_id, d.product_id, d.quantity_in_stock, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'inventory', i.inventory_id,
               (SELECT d.quantity_in_stock, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.quantity_in_stock, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.inventory_id = d.inventory_id
        WHERE dbo.fn_has_changes(
            (SELECT d.quantity_in_stock, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.quantity_in_stock, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 13. stock_transfer
-- ============================================================
CREATE OR ALTER TRIGGER trg_stock_transfer_audit ON stock_transfer
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    IF EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'INSERT', 'stock_transfer', i.stock_transfer_id, NULL,
               (SELECT i.from_warehouse_id, i.to_warehouse_id, i.transfer_code, i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE() FROM inserted i;
    END

    IF EXISTS (SELECT 1 FROM deleted) AND NOT EXISTS (SELECT 1 FROM inserted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'DELETE', 'stock_transfer', d.stock_transfer_id,
               (SELECT d.from_warehouse_id, d.to_warehouse_id, d.transfer_code, d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               NULL, GETDATE() FROM deleted d;
    END

    IF EXISTS (SELECT 1 FROM inserted) AND EXISTS (SELECT 1 FROM deleted)
    BEGIN
        INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
        SELECT @emp_id, 'UPDATE', 'stock_transfer', i.stock_transfer_id,
               (SELECT d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               (SELECT i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
               GETDATE()
        FROM inserted i INNER JOIN deleted d ON i.stock_transfer_id = d.stock_transfer_id
        WHERE dbo.fn_has_changes(
            (SELECT d.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
            (SELECT i.status FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
        ) = 1;
    END
END;
GO

-- ============================================================
-- 14. stock_transaction (INSERT-only)
-- ============================================================
CREATE OR ALTER TRIGGER trg_stock_transaction_audit ON stock_transaction
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp_id INT = CAST(SESSION_CONTEXT(N'EmployeeID') AS INT);

    INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
    SELECT @emp_id, 'INSERT', 'stock_transaction', i.stock_transaction_id, NULL,
           (SELECT i.warehouse_id, i.product_id, i.transaction_type, i.quantity, i.before_quantity, i.after_quantity
            FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
           GETDATE()
    FROM inserted i;
END;
GO

PRINT N'=== Tat ca trigger audit log da tao thanh cong! ===';
GO
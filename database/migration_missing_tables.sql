-- ============================================================
-- Migration: Create tables referenced by DAO code but missing from V3
-- These tables existed in V2 and were referenced by merged code.
-- Generated 2026-07-01 during Phase 1 stabilization.
-- ============================================================
USE DBFinoraV3;
GO

-- ============================================================
--  1. shift — POS shift management
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'shift')
BEGIN
    CREATE TABLE shift (
        shift_id       INT            IDENTITY(1,1) PRIMARY KEY,
        emp_id         INT            NOT NULL,
        branch_id      INT            NOT NULL,
        opening_cash   DECIMAL(18,2)  DEFAULT 0,
        closing_cash   DECIMAL(18,2)  DEFAULT 0,
        expected_cash  DECIMAL(18,2)  DEFAULT 0,
        status         NVARCHAR(20)   DEFAULT 'OPEN',
        opened_at      DATETIME       DEFAULT GETDATE(),
        closed_at      DATETIME,

        CONSTRAINT FK_Shift_Employee
            FOREIGN KEY (emp_id) REFERENCES Employee(emp_id),

        CONSTRAINT FK_Shift_Branch
            FOREIGN KEY (branch_id) REFERENCES Branch(branch_id)
    );
END
GO

-- ============================================================
--  2. cash_transaction — Cash drawer transactions within a shift
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'cash_transaction')
BEGIN
    CREATE TABLE cash_transaction (
        cash_transaction_id INT            IDENTITY(1,1) PRIMARY KEY,
        shift_id            INT            NOT NULL,
        type                NVARCHAR(20)   CHECK (type IN ('DEPOSIT','WITHDRAW')),
        amount              DECIMAL(18,2)  DEFAULT 0,
        note                NVARCHAR(500),
        created_at          DATETIME       DEFAULT GETDATE(),

        CONSTRAINT FK_CashTransaction_Shift
            FOREIGN KEY (shift_id) REFERENCES shift(shift_id)
    );
END
GO

-- ============================================================
--  3. inventory_ticket — Stock transfer/exchange tickets
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inventory_ticket')
BEGIN
    CREATE TABLE inventory_ticket (
        ticket_id              INT            IDENTITY(1,1) PRIMARY KEY,
        ticket_code            NVARCHAR(50)   UNIQUE,
        ticket_type            NVARCHAR(50),
        from_warehouse_id      INT,
        to_warehouse_id        INT,
        status                 NVARCHAR(30)   DEFAULT 'PENDING',
        note                   NVARCHAR(500),
        created_by             INT,
        created_at             DATETIME       DEFAULT GETDATE(),
        is_exported_by_sender  BIT            DEFAULT 0,
        is_imported_by_receiver BIT           DEFAULT 0,

        CONSTRAINT FK_InventoryTicket_FromWarehouse
            FOREIGN KEY (from_warehouse_id) REFERENCES warehouse(warehouse_id),

        CONSTRAINT FK_InventoryTicket_ToWarehouse
            FOREIGN KEY (to_warehouse_id) REFERENCES warehouse(warehouse_id),

        CONSTRAINT FK_InventoryTicket_Employee
            FOREIGN KEY (created_by) REFERENCES Employee(emp_id)
    );
END
GO

-- ============================================================
--  4. inventory_ticket_detail — Line items for inventory tickets
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inventory_ticket_detail')
BEGIN
    CREATE TABLE inventory_ticket_detail (
        detail_id      INT            IDENTITY(1,1) PRIMARY KEY,
        ticket_id      INT            NOT NULL,
        product_id     INT            NOT NULL,
        quantity       INT            DEFAULT 0,
        actual_quantity INT,
        action_type    NVARCHAR(50),

        CONSTRAINT FK_InventoryTicketDetail_Ticket
            FOREIGN KEY (ticket_id) REFERENCES inventory_ticket(ticket_id),

        CONSTRAINT FK_InventoryTicketDetail_Product
            FOREIGN KEY (product_id) REFERENCES [product](product_id)
    );
END
GO

-- ============================================================
--  5. supplier_product — Supplier-to-product pricing bridge
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'supplier_product')
BEGIN
    CREATE TABLE supplier_product (
        supplier_id   INT            NOT NULL,
        product_id    INT            NOT NULL,
        import_price  DECIMAL(18,2)  DEFAULT 0,

        CONSTRAINT PK_SupplierProduct
            PRIMARY KEY (supplier_id, product_id),

        CONSTRAINT FK_SupplierProduct_Supplier
            FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),

        CONSTRAINT FK_SupplierProduct_Product
            FOREIGN KEY (product_id) REFERENCES [product](product_id)
    );
END
GO

PRINT 'All missing tables created successfully.';
GO

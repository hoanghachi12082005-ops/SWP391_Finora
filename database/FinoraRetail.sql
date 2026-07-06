-- ============================================================
-- FinoraRetail — Complete Database Schema + Seed Data
-- Database: DBFinoraV3
-- Generated: 2026-07-02 (merged from all migration/patch files)
-- Compatible: SQL Server (SSMS), run on empty server
-- ============================================================

IF DB_ID(N'DBFinoraV3') IS NOT NULL
BEGIN
    ALTER DATABASE DBFinoraV3 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE DBFinoraV3;
END
GO

CREATE DATABASE DBFinoraV3;
GO

USE DBFinoraV3;
GO

-- ============================================================
--  1. ROLE
-- ============================================================
CREATE TABLE [Role] (
    role_id      INT           IDENTITY(1,1) PRIMARY KEY,
    role_name    NVARCHAR(100) NOT NULL,
    discription  NVARCHAR(255),
    created_at   DATETIME      DEFAULT GETDATE(),
    update_at    DATETIME      DEFAULT GETDATE()
);
GO

-- ============================================================
--  2. BRANCH
-- ============================================================
CREATE TABLE Branch (
    branch_id     INT             IDENTITY(1,1) PRIMARY KEY,
    branch_name   NVARCHAR(150)   NOT NULL,
    branch_code   NVARCHAR(50)    UNIQUE,
    address       NVARCHAR(300),
    district      NVARCHAR(150),
    city          NVARCHAR(150),
    phone         NVARCHAR(20)    UNIQUE,
    email         NVARCHAR(150)   UNIQUE,
    opening_time  NVARCHAR(10),
    closing_time  NVARCHAR(10),
    status        NVARCHAR(20)    DEFAULT 'ACTIVE'
                                  CHECK (status IN ('ACTIVE','INACTIVE')),
    image_URL      NVARCHAR(255),
    created_at    DATETIME        DEFAULT GETDATE(),
    update_at     DATETIME        DEFAULT GETDATE()
);
GO

-- ============================================================
--  3. EMPLOYEE
-- ============================================================
CREATE TABLE Employee (
    emp_id        INT           IDENTITY(1,1) PRIMARY KEY,
    branch_id     INT           NOT NULL,
    role_id       INT           NOT NULL,
    fullName      NVARCHAR(150) NOT NULL,
    gender        NVARCHAR(10),
    bod           DATE,
    address       NVARCHAR(300),
    email         NVARCHAR(150) UNIQUE,
    phone         NVARCHAR(20)  UNIQUE,
    passwordHash      NVARCHAR(255),
    image_URL         NVARCHAR(255),
    status            NVARCHAR(20)  DEFAULT 'ACTIVE'
                                    CHECK (status IN ('ACTIVE','INACTIVE')),
    failed_login_count INT          NOT NULL DEFAULT 0,
    created_at        DATETIME      DEFAULT GETDATE(),
    update_at         DATETIME      DEFAULT GETDATE(),

    CONSTRAINT FK_Employee_Branch
        FOREIGN KEY (branch_id) REFERENCES Branch(branch_id),

    CONSTRAINT FK_Employee_Role
        FOREIGN KEY (role_id) REFERENCES [Role](role_id)
);
GO

-- ============================================================
--  4. CUSTOMER
-- ============================================================
CREATE TABLE customer (
    cus_id      INT           IDENTITY(1,1) PRIMARY KEY,
    full_name   NVARCHAR(150) NOT NULL,
    gender      NVARCHAR(10),
    bod         DATE,
    address     NVARCHAR(300),
    email       NVARCHAR(150),
    phone       NVARCHAR(20) UNIQUE,
    total_spent DECIMAL(18,2) DEFAULT 0,
    status        NVARCHAR(20)  DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at  DATETIME      DEFAULT GETDATE(),
    updated_at  DATETIME      DEFAULT GETDATE()
);
GO

-- ============================================================
--  5. CUSTOMER_POINT
-- ============================================================
CREATE TABLE customer_point (
    cus_point_id    INT           IDENTITY(1,1) PRIMARY KEY,
    cus_id          INT           NOT NULL UNIQUE,
    current_points  INT           DEFAULT 0,
    lifetime_points INT           DEFAULT 0,
    updated_at      DATETIME      DEFAULT GETDATE(),

    CONSTRAINT FK_CustomerPoint_Customer
        FOREIGN KEY (cus_id) REFERENCES customer(cus_id)
);
GO

-- ============================================================
--  6. VOUCHER
-- ============================================================
CREATE TABLE voucher (
    voucher_id     INT            IDENTITY(1,1) PRIMARY KEY,
    voucher_code   NVARCHAR(50)   UNIQUE NOT NULL,
    voucher_name   NVARCHAR(150),
    discount_type  NVARCHAR(20)   CHECK (discount_type IN ('PERCENT','FIXED')),
    discount_value DECIMAL(18,2),
    used_quantity  INT            DEFAULT 0,
    start_date     DATE,
    end_date       DATE,
    status         NVARCHAR(20)   DEFAULT 'ACTIVE',
    created_at     DATETIME       DEFAULT GETDATE()
);
GO

-- ============================================================
--  7. SUPPLIER
-- ============================================================
CREATE TABLE supplier (
    supplier_id   INT           IDENTITY(1,1) PRIMARY KEY,
    supplier_name NVARCHAR(150) NOT NULL,
    phone_number  NVARCHAR(20),
    address       NVARCHAR(300),
    status        NVARCHAR(20) DEFAULT 'ACTIVE'
                               CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at    DATETIME DEFAULT GETDATE(),
    updated_at    DATETIME DEFAULT GETDATE()
);
GO

-- ============================================================
--  8. WAREHOUSE
-- ============================================================
CREATE TABLE warehouse (
    warehouse_id   INT           IDENTITY(1,1) PRIMARY KEY,
    warehouse_name NVARCHAR(150) NOT NULL,
    branch_id      INT           NOT NULL UNIQUE,
    address        NVARCHAR(300),
    status         NVARCHAR(20) DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at     DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_Warehouse_Branch
        FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);
GO

-- ============================================================
--  9. UNIT
-- ============================================================
CREATE TABLE unit (
    unit_id     INT           IDENTITY(1,1) PRIMARY KEY,
    unit_name   NVARCHAR(50)  NOT NULL,
    description NVARCHAR(255)
);
GO

-- ============================================================
--  10. CATEGORY
-- ============================================================
CREATE TABLE category (
    category_id        INT           IDENTITY(1,1) PRIMARY KEY,
    category_name      NVARCHAR(150) NOT NULL,
    description        NVARCHAR(255),
    parent_category_id INT,
    status             NVARCHAR(20) DEFAULT 'ACTIVE'
                                    CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at         DATETIME DEFAULT GETDATE(),
    update_at          DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_Category_ParentCategory
        FOREIGN KEY (parent_category_id)
        REFERENCES category(category_id)
);
GO

-- ============================================================
--  11. PRODUCT
-- ============================================================
CREATE TABLE [product] (
    product_id      INT            IDENTITY(1,1) PRIMARY KEY,
    product_codebar NVARCHAR(50)   UNIQUE,
    product_name    NVARCHAR(200)  NOT NULL,
    category_id     INT,
    unit_id         INT,
    selling_price   DECIMAL(18,2)  DEFAULT 0,
    ImageUrl        NVARCHAR(500),
    created_at      DATETIME       DEFAULT GETDATE(),
    update_at       DATETIME       DEFAULT GETDATE(),

    CONSTRAINT FK_Product_Category
        FOREIGN KEY (category_id)
        REFERENCES category(category_id),

    CONSTRAINT FK_Product_Unit
        FOREIGN KEY (unit_id)
        REFERENCES unit(unit_id)
);
GO

-- ============================================================
--  12. INVENTORY
-- ============================================================
CREATE TABLE inventory (
    inventory_id       INT              IDENTITY(1,1) PRIMARY KEY,
    warehouse_id       INT              NOT NULL,
    product_id         INT              NOT NULL,
    quantity_in_stock  INT              DEFAULT 0,
    status             NVARCHAR(20)     DEFAULT 'ACTIVE'
                                        CHECK (status IN ('ACTIVE','INACTIVE','OUT_OF_STOCK')),
    updated_at         DATETIME         DEFAULT GETDATE(),

    CONSTRAINT FK_Inventory_Warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouse(warehouse_id),

    CONSTRAINT FK_Inventory_Product
        FOREIGN KEY (product_id)
        REFERENCES [product](product_id),

    CONSTRAINT UQ_inventory_warehouse_product
        UNIQUE (warehouse_id, product_id)
);
GO

-- ============================================================
--  13. ORDER
-- ============================================================
CREATE TABLE [order] (
    order_id        INT            IDENTITY(1,1) PRIMARY KEY,
    order_code      NVARCHAR(50)   UNIQUE,
    order_type      NVARCHAR(30)   CHECK (order_type IN ('SALE','PURCHASE')),
    customer_id     INT,
    branch_id       INT,
    supplier_id     INT,
    emp_id          INT,
    voucher_id      INT,
    warehouse_id    INT,
    subtotal        DECIMAL(18,2)  DEFAULT 0,
    discount_amount DECIMAL(18,2)  DEFAULT 0,
    total_amount    DECIMAL(18,2)  DEFAULT 0,
    payment_method  NVARCHAR(50)   CHECK (payment_method IN ('CASH','BANK_TRANSFER')),
    status          NVARCHAR(30)   DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING','COMPLETED','CANCELLED')),
    created_at      DATETIME       DEFAULT GETDATE(),

    CONSTRAINT FK_Order_Customer
        FOREIGN KEY (customer_id)
        REFERENCES customer(cus_id),

    CONSTRAINT FK_Order_Branch
        FOREIGN KEY (branch_id)
        REFERENCES branch(branch_id),

    CONSTRAINT FK_Order_Supplier
        FOREIGN KEY (supplier_id)
        REFERENCES supplier(supplier_id),

    CONSTRAINT FK_Order_Employee
        FOREIGN KEY (emp_id)
        REFERENCES employee(emp_id),

    CONSTRAINT FK_Order_Voucher
        FOREIGN KEY (voucher_id)
        REFERENCES voucher(voucher_id),

    CONSTRAINT FK_Order_Warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouse(warehouse_id)
);
GO

-- ============================================================
--  14. ORDER_DETAIL
-- ============================================================
CREATE TABLE order_detail (
    order_detail_id INT            IDENTITY(1,1) PRIMARY KEY,
    order_id        INT            NOT NULL,
    product_id      INT            NOT NULL,
    quantity        INT            DEFAULT 1,
    unit_price      DECIMAL(18,2)  DEFAULT 0,
    total_price     DECIMAL(18,2)  DEFAULT 0,

    CONSTRAINT FK_OrderDetail_Order
        FOREIGN KEY (order_id)
        REFERENCES [order](order_id),

    CONSTRAINT FK_OrderDetail_Product
        FOREIGN KEY (product_id)
        REFERENCES [product](product_id)
);
GO

-- ============================================================
--  15. PAYMENT (with cashbook support)
-- ============================================================
CREATE TABLE payment (
    payment_id       INT            IDENTITY(1,1) PRIMARY KEY,
    order_id         INT            NULL,
    payment_amount   DECIMAL(18,2)  DEFAULT 0,
    payment_date     DATETIME       DEFAULT GETDATE(),
    payment_status   NVARCHAR(30)   DEFAULT 'PENDING'
                                    CHECK (payment_status IN ('PENDING','PAID','FAILED')),
    transaction_code NVARCHAR(100),
    PaymentType      NVARCHAR(20)   NOT NULL CONSTRAINT CK_Payment_PaymentType CHECK (PaymentType IN ('INCOME', 'EXPENSE')),
    Description      NVARCHAR(500)  NULL,
    EmployeeID       INT            NULL,
    BranchID         INT            NULL,

    CONSTRAINT FK_Payment_Order
        FOREIGN KEY (order_id)
        REFERENCES [order](order_id),

    CONSTRAINT FK_Payment_Employee
        FOREIGN KEY (EmployeeID) REFERENCES Employee(emp_id),

    CONSTRAINT FK_Payment_Branch
        FOREIGN KEY (BranchID) REFERENCES Branch(branch_id)
);
GO

-- ============================================================
--  16. POINT_TRANSACTION
-- ============================================================
CREATE TABLE point_transaction (
    point_transaction_id INT           IDENTITY(1,1) PRIMARY KEY,
    cus_point_id         INT           NOT NULL,
    order_id             INT,
    before_points        INT           DEFAULT 0,
    after_points         INT           DEFAULT 0,
    description          NVARCHAR(255),
    created_at           DATETIME      DEFAULT GETDATE(),

    CONSTRAINT FK_PointTransaction_CustomerPoint
        FOREIGN KEY (cus_point_id)
        REFERENCES customer_point(cus_point_id),

    CONSTRAINT FK_PointTransaction_Order
        FOREIGN KEY (order_id)
        REFERENCES [order](order_id)
);
GO

-- ============================================================
--  17. STOCK_TRANSFER
-- ============================================================
CREATE TABLE stock_transfer (
    stock_transfer_id  INT           IDENTITY(1,1) PRIMARY KEY,
    from_warehouse_id  INT           NOT NULL,
    to_warehouse_id    INT           NOT NULL,
    transfer_code      NVARCHAR(50),
    transfer_date      DATETIME      DEFAULT GETDATE(),
    status             NVARCHAR(30),
    note               NVARCHAR(500),
    created_by         INT,

    CONSTRAINT FK_StockTransfer_FromWarehouse
        FOREIGN KEY (from_warehouse_id)
        REFERENCES warehouse(warehouse_id),

    CONSTRAINT FK_StockTransfer_ToWarehouse
        FOREIGN KEY (to_warehouse_id)
        REFERENCES warehouse(warehouse_id),

    CONSTRAINT FK_StockTransfer_Employee
        FOREIGN KEY (created_by)
        REFERENCES employee(emp_id)
);
GO

-- ============================================================
--  18. STOCK_TRANSFER_DETAIL
-- ============================================================
CREATE TABLE stock_transfer_detail (
    stock_transfer_detail_id INT  IDENTITY(1,1) PRIMARY KEY,
    stock_transfer_id        INT  NOT NULL,
    product_id               INT  NOT NULL,
    quantity                 INT  DEFAULT 0,

    CONSTRAINT FK_StockTransferDetail_StockTransfer
        FOREIGN KEY (stock_transfer_id)
        REFERENCES stock_transfer(stock_transfer_id),

    CONSTRAINT FK_StockTransferDetail_Product
        FOREIGN KEY (product_id)
        REFERENCES [product](product_id)
);
GO

-- ============================================================
--  19. STOCK_TRANSACTION
-- ============================================================
CREATE TABLE stock_transaction (
    stock_transaction_id INT           IDENTITY(1,1) PRIMARY KEY,
    warehouse_id         INT           NOT NULL,
    product_id           INT           NOT NULL,
    reference_type       NVARCHAR(50),
    reference_id         INT,
    transaction_type     NVARCHAR(20),
    quantity             INT           DEFAULT 0,
    before_quantity      INT           DEFAULT 0,
    after_quantity       INT           DEFAULT 0,
    note                 NVARCHAR(500),
    created_by           INT,
    created_at           DATETIME      DEFAULT GETDATE(),

    CONSTRAINT FK_StockTransaction_Warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouse(warehouse_id),

    CONSTRAINT FK_StockTransaction_Product
        FOREIGN KEY (product_id)
        REFERENCES [product](product_id),

    CONSTRAINT FK_StockTransaction_Employee
        FOREIGN KEY (created_by)
        REFERENCES employee(emp_id)
);
GO

-- ============================================================
--  20. LOYALTY_POINT_SETTING
-- ============================================================
CREATE TABLE loyalty_point_setting (
    setting_id       INT           IDENTITY(1,1) PRIMARY KEY,
    amount_per_point DECIMAL(18,2) NOT NULL DEFAULT 100000,
    point_to_currency DECIMAL(18,2) DEFAULT 0,
    updated_by       INT           REFERENCES employee(emp_id),
    updated_at       DATETIME      DEFAULT GETDATE()
);
GO

-- ============================================================
--  21. AUDIT_LOG
-- ============================================================
CREATE TABLE audit_log (
    audit_log_id INT           IDENTITY(1,1) PRIMARY KEY,
    emp_id       INT,
    action_name  NVARCHAR(100),
    table_name   NVARCHAR(100),
    record_id    INT,
    old_data     NVARCHAR(MAX),
    new_data     NVARCHAR(MAX),
    created_at   DATETIME      DEFAULT GETDATE(),

    CONSTRAINT FK_AuditLog_Employee
        FOREIGN KEY (emp_id)
        REFERENCES employee(emp_id)
);
GO

-- ============================================================
--  22. SHIFT — POS shift management
-- ============================================================
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
GO

-- ============================================================
--  23. CASH_TRANSACTION — Cash drawer transactions within a shift
-- ============================================================
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
GO

-- ============================================================
--  24. INVENTORY_TICKET — Stock transfer/exchange tickets
-- ============================================================
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
GO

-- ============================================================
--  25. INVENTORY_TICKET_DETAIL — Line items for inventory tickets
-- ============================================================
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
GO

-- ============================================================
--  26. SUPPLIER_PRODUCT — Supplier-to-product pricing bridge
-- ============================================================
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
GO

-- ============================================================
--  INDEXES
-- ============================================================
CREATE INDEX IX_Employee_BranchID ON Employee(branch_id);
GO
CREATE INDEX IX_Employee_Status ON Employee(status);
GO
CREATE UNIQUE INDEX UQ_Order_OrderCode_NotNull ON [order](order_code) WHERE order_code IS NOT NULL;
GO
CREATE INDEX IX_Order_EmployeeID ON [order](emp_id);
GO
CREATE INDEX IX_Order_BranchID ON [order](branch_id);
GO

-- ============================================================
--  DEFAULT DATA
-- ============================================================
INSERT INTO loyalty_point_setting (amount_per_point, point_to_currency) VALUES (100000, 0);
GO

-- ============================================================
--  SEED DATA
-- ============================================================

-- 1. ROLES
INSERT INTO [Role] (role_name, discription) VALUES
('Admin',          N'System administrator with full access'),
('Owner',          N'Shop owner, views all business data'),
('StoreManager',   N'Manages a single branch and its employees'),
('SalesStaff',     N'Handles POS sales and customer service'),
('WarehouseStaff', N'Manages warehouse stock and inventory');
GO

-- 2. BRANCHES
INSERT INTO Branch (branch_name, branch_code, address, district, city, phone, email, opening_time, closing_time, status) VALUES
(N'Finora Hà Nội',     'BR-001', N'123 Trần Hưng Đạo', N'Hoàn Kiếm', N'Hà Nội',       '024-3822-0001', 'hn@finora.vn',    '07:00', '22:00', 'ACTIVE'),
(N'Finora Hồ Chí Minh','BR-002', N'456 Nguyễn Huệ',   N'Quận 1',    N'Hồ Chí Minh',  '028-3911-0002', 'hcm@finora.vn',   '07:00', '22:00', 'ACTIVE'),
(N'Finora Đà Nẵng',    'BR-003', N'789 Bạch Đằng',    N'Hải Châu',  N'Đà Nẵng',      '0236-3555-0003','dn@finora.vn',    '07:00', '21:30', 'ACTIVE');
GO

-- 3. EMPLOYEES
INSERT INTO Employee (branch_id, role_id, fullName, gender, bod, address, email, phone, passwordHash, status) VALUES
(1, 1, N'Nguyễn Văn An',     N'Nam',  '1990-05-15', N'Hà Nội',     'admin@finora.vn',        '090-100-0001', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 2, N'Trần Thị Bình',     N'Nữ',   '1985-08-20', N'Hà Nội',     'owner@finora.vn',        '090-100-0002', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 3, N'Lê Văn Cường',      N'Nam',  '1992-03-10', N'Hà Nội',     'cuong.lv@finora.vn',     '090-100-0003', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 3, N'Phạm Thị Dung',     N'Nữ',   '1991-07-22', N'Hồ Chí Minh','dung.pt@finora.vn',      '090-100-0004', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 3, N'Hoàng Văn Em',      N'Nam',  '1993-11-05', N'Đà Nẵng',   'em.hv@finora.vn',        '090-100-0005', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 4, N'Nguyễn Thị Phương',  N'Nữ',   '1996-02-14', N'Hà Nội',     'phuong.nt@finora.vn',    '090-100-0006', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 4, N'Vũ Văn Giang',      N'Nam',  '1995-09-30', N'Hà Nội',     'giang.vv@finora.vn',     '090-100-0007', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 4, N'Đỗ Thị Hoa',        N'Nữ',   '1997-06-18', N'Hà Nội',     'hoa.dt@finora.vn',       '090-100-0008', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 4, N'Bùi Văn Huy',       N'Nam',  '1994-12-01', N'Hà Nội',     'huy.bv@finora.vn',       '090-100-0009', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 4, N'Trương Thị Khanh',  N'Nữ',   '1998-04-25', N'Hồ Chí Minh','khanh.tt@finora.vn',     '090-100-0010', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 4, N'Đặng Văn Lâm',      N'Nam',  '1993-08-12', N'Hồ Chí Minh','lam.dv@finora.vn',       '090-100-0011', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 4, N'Võ Thị Mai',        N'Nữ',   '1996-01-07', N'Hồ Chí Minh','mai.vt@finora.vn',       '090-100-0012', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 4, N'Ngô Văn Nam',       N'Nam',  '1995-10-19', N'Đà Nẵng',   'nam.nv@finora.vn',       '090-100-0013', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 4, N'Dương Thị Oanh',    N'Nữ',   '1997-03-28', N'Đà Nẵng',   'oanh.dt@finora.vn',      '090-100-0014', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 4, N'Lý Văn Phúc',       N'Nam',  '1994-07-15', N'Đà Nẵng',   'phuc.lv@finora.vn',      '090-100-0015', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 5, N'Trần Văn Quân',     N'Nam',  '1993-04-10', N'Hà Nội',     'quan.tv@finora.vn',      '090-100-0016', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 5, N'Lê Thị Ráng',       N'Nữ',   '1995-09-22', N'Hồ Chí Minh','rang.lt@finora.vn',       '090-100-0017', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 5, N'Nguyễn Văn Sơn',    N'Nam',  '1994-12-05', N'Đà Nẵng',   'son.nv2@finora.vn',       '090-100-0018', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE');
GO

-- 4. SUPPLIERS
INSERT INTO supplier (supplier_name, phone_number, address, status) VALUES
(N'Công ty TNHH Thực phẩm Xanh',     '024-3888-1111', N'Số 10 Láng Hạ, Hà Nội',          'ACTIVE'),
(N'Công ty CP Đồ uống Việt',         '028-3999-2222', N'123 Lê Duẩn, Hồ Chí Minh',       'ACTIVE'),
(N'Công ty TNHH Hàng tiêu dùng Nam', '0236-3777-3333', N'45 Nguyễn Văn Linh, Đà Nẵng',   'ACTIVE'),
(N'Tổng công ty Thương mại Bắc',     '024-3666-4444', N'78 Kim Mã, Hà Nội',              'ACTIVE'),
(N'Công ty CP Phân phối Miền Nam',   '028-3555-5555', N'456 Cách Mạng Tháng 8, HCM',     'ACTIVE');
GO

-- 5. WAREHOUSES
INSERT INTO warehouse (warehouse_name, branch_id, address, status) VALUES
(N'Kho Hà Nội',     1, N'KCN Bắc Từ Liêm, Hà Nội',      'ACTIVE'),
(N'Kho Hồ Chí Minh',2, N'KCN Tân Bình, Hồ Chí Minh',    'ACTIVE'),
(N'Kho Đà Nẵng',    3, N'KCN Liên Chiểu, Đà Nẵng',      'ACTIVE');
GO

-- 6. UNITS
INSERT INTO unit (unit_name, description) VALUES
(N'Cái',    N'Piece'),
(N'Thùng',  N'Box'),
(N'Chai',   N'Bottle'),
(N'Gói',    N'Pack'),
(N'Kg',     N'Kilogram');
GO

-- 7. CATEGORIES
INSERT INTO category (category_name, description, status) VALUES
(N'Đồ uống',       N'Beverages — soft drinks, water, juice',                'ACTIVE'),
(N'Bánh kẹo',      N'Snacks & Candy — chips, cookies, chocolate',           'ACTIVE'),
(N'Thực phẩm',     N'Grocery — rice, oil, noodles, seasoning',              'ACTIVE'),
(N'Đồ gia dụng',   N'Household — cleaning supplies, kitchenware',           'ACTIVE'),
(N'Chăm sóc cá nhân', N'Personal Care — toothpaste, soap, shampoo',         'ACTIVE'),
(N'Đồ ăn liền',    N'Instant Food — instant noodles, canned food',          'ACTIVE');
GO

-- 8. PRODUCTS (50)
INSERT INTO [product] (product_codebar, product_name, category_id, unit_id, selling_price) VALUES
('8934567890123', N'Coca Cola 330ml',       1, 3, 10000),
('8934567890124', N'Pepsi 330ml',           1, 3, 10000),
('8934567890125', N'Aquafina 500ml',        1, 3, 5000),
('8934567890126', N'Milo 400g',             1, 1, 45000),
('8934567890127', N'Trà xanh C2 500ml',     1, 3, 8000),
('8934567890128', N'Sting dâu 330ml',       1, 3, 7000),
('8934567890129', N'Number 1 330ml',        1, 3, 8000),
('8934567890130', N'Tân Hiệp Phát Dr Thanh 330ml',1, 3, 9000),
('8934567890131', N'Sữa đậu nành Fami 200ml',1, 3, 6000),
('8934567890132', N'Nước cam ép Twister 330ml',1, 3, 12000),
('8934567890133', N'Oreo 97g',              2, 4, 7000),
('8934567890134', N'Oishi Khoai tây 60g',   2, 4, 5000),
('8934567890135', N'Lay''s 50g',            2, 4, 6000),
('8934567890136', N'Snickers 50g',           2, 4, 10000),
('8934567890137', N'Choco Pie 300g',        2, 1, 25000),
('8934567890138', N'Bánh quy Cosy 200g',    2, 4, 15000),
('8934567890139', N'Kẹo mút Chupa Chups',   2, 1, 2000),
('8934567890140', N'Bánh gạo One One 100g', 2, 4, 8000),
('8934567890141', N'Solite vị dâu 150g',    2, 4, 5000),
('8934567890142', N'Pocky 40g',             2, 4, 7000),
('8934567890143', N'Bánh AFC 150g',         2, 4, 6000),
('8934567890144', N'ST25 Gạo đặc sản 5kg',           3, 5, 120000),
('8934567890145', N'Nước mắm Nam Ngư 500ml',         3, 3, 25000),
('8934567890146', N'Dầu ăn Neptune 1l',              3, 3, 35000),
('8934567890147', N'Bột ngọt Ajinomoto 100g',        3, 4, 6000),
('8934567890148', N'Hạt nêm Knorr 400g',             3, 4, 28000),
('8934567890149', N'Đường trắng REI 1kg',            3, 5, 18000),
('8934567890150', N'Muối biển 500g',                 3, 4, 4000),
('8934567890151', N'Tương ớt Chinsu 300g',           3, 3, 12000),
('8934567890152', N'Sữa đặc Ông Thọ 380g',           3, 1, 22000),
('8934567890153', N'Mì gói Hảo Hảo 75g gói',         3, 4, 4000),
('8934567890154', N'Nui rỗng Vifon 200g',             3, 4, 8000),
('8934567890155', N'Nước rửa chén Sunlight 750ml',   4, 3, 28000),
('8934567890156', N'Nước lau sàn Vfresh 1l',         4, 3, 22000),
('8934567890157', N'Bột giặt Tide 800g',             4, 4, 35000),
('8934567890158', N'Khăn giấy ướt Bobby 80 tờ',      4, 4, 12000),
('8934567890159', N'Túi rác 45x50 30 cái',           4, 4, 15000),
('8934567890160', N'Nến thơm Air Wick 1 lọ',         4, 1, 25000),
('8934567890161', N'Kem đánh răng Colgate 120g',     5, 1, 25000),
('8934567890162', N'Sữa tắm Lifebuoy 450ml',         5, 3, 32000),
('8934567890163', N'Dầu gội Sunsilk 350ml',          5, 3, 38000),
('8934567890164', N'Xà bông Lifebuoy 90g',          5, 1, 8000),
('8934567890165', N'Lăn khử mùi Nivea 50ml',         5, 1, 35000),
('8934567890166', N'Tã bỉm Merries Size M 56 miếng', 5, 1, 180000),
('8934567890167', N'Mì tôm Hảo Hảo 30 gói',         6, 2, 80000),
('8934567890168', N'Mì ly Hảo Hảo 67g',             6, 1, 8000),
('8934567890169', N'Phở bò Vifon 65g',              6, 4, 5000),
('8934567890170', N'Miến dong Phú Hương 200g',       6, 4, 10000),
('8934567890171', N'Cháo gà Vifon 60g',             6, 4, 5000),
('8934567890172', N'Bim Bim Oishi 20 gói',          6, 2, 85000),
('8934567890173', N'Cá hộp Hải Long 155g',           6, 1, 18000),
('8934567890174', N'Đồ hộp thịt heo Đức Việt 150g',  6, 1, 15000);
GO

-- 9. INVENTORY (50 products x 3 warehouses)
INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status) VALUES
(1,1,150,'ACTIVE'),(1,2,200,'ACTIVE'),(1,3,300,'ACTIVE'),(1,4,80,'ACTIVE'),(1,5,250,'ACTIVE'),
(1,6,180,'ACTIVE'),(1,7,220,'ACTIVE'),(1,8,140,'ACTIVE'),(1,9,120,'ACTIVE'),(1,10,100,'ACTIVE'),
(1,11,160,'ACTIVE'),(1,12,240,'ACTIVE'),(1,13,190,'ACTIVE'),(1,14,90,'ACTIVE'),(1,15,70,'ACTIVE'),
(1,16,85,'ACTIVE'),(1,17,300,'ACTIVE'),(1,18,110,'ACTIVE'),(1,19,200,'ACTIVE'),(1,20,130,'ACTIVE'),
(1,21,95,'ACTIVE'),(1,22,50,'ACTIVE'),(1,23,60,'ACTIVE'),(1,24,75,'ACTIVE'),(1,25,180,'ACTIVE'),
(1,26,220,'ACTIVE'),(1,27,160,'ACTIVE'),(1,28,300,'ACTIVE'),(1,29,90,'ACTIVE'),(1,30,110,'ACTIVE'),
(1,31,70,'ACTIVE'),(1,32,85,'ACTIVE'),(1,33,40,'ACTIVE'),(1,34,55,'ACTIVE'),(1,35,200,'ACTIVE'),
(1,36,150,'ACTIVE'),(1,37,100,'ACTIVE'),(1,38,120,'ACTIVE'),(1,39,80,'ACTIVE'),(1,40,60,'ACTIVE'),
(1,41,90,'ACTIVE'),(1,42,45,'ACTIVE'),(1,43,35,'ACTIVE'),(1,44,65,'ACTIVE'),(1,45,140,'ACTIVE'),
(1,46,180,'ACTIVE'),(1,47,70,'ACTIVE'),(1,48,50,'ACTIVE'),(1,49,30,'ACTIVE'),(1,50,25,'ACTIVE'),
(2,1,120,'ACTIVE'),(2,2,180,'ACTIVE'),(2,3,250,'ACTIVE'),(2,4,60,'ACTIVE'),(2,5,220,'ACTIVE'),
(2,6,160,'ACTIVE'),(2,7,200,'ACTIVE'),(2,8,110,'ACTIVE'),(2,9,100,'ACTIVE'),(2,10,90,'ACTIVE'),
(2,11,140,'ACTIVE'),(2,12,220,'ACTIVE'),(2,13,170,'ACTIVE'),(2,14,80,'ACTIVE'),(2,15,60,'ACTIVE'),
(2,16,75,'ACTIVE'),(2,17,280,'ACTIVE'),(2,18,100,'ACTIVE'),(2,19,180,'ACTIVE'),(2,20,110,'ACTIVE'),
(2,21,85,'ACTIVE'),(2,22,45,'ACTIVE'),(2,23,55,'ACTIVE'),(2,24,65,'ACTIVE'),(2,25,160,'ACTIVE'),
(2,26,200,'ACTIVE'),(2,27,140,'ACTIVE'),(2,28,280,'ACTIVE'),(2,29,80,'ACTIVE'),(2,30,100,'ACTIVE'),
(2,31,60,'ACTIVE'),(2,32,75,'ACTIVE'),(2,33,35,'ACTIVE'),(2,34,50,'ACTIVE'),(2,35,180,'ACTIVE'),
(2,36,130,'ACTIVE'),(2,37,90,'ACTIVE'),(2,38,100,'ACTIVE'),(2,39,70,'ACTIVE'),(2,40,55,'ACTIVE'),
(2,41,80,'ACTIVE'),(2,42,40,'ACTIVE'),(2,43,30,'ACTIVE'),(2,44,55,'ACTIVE'),(2,45,120,'ACTIVE'),
(2,46,160,'ACTIVE'),(2,47,60,'ACTIVE'),(2,48,45,'ACTIVE'),(2,49,25,'ACTIVE'),(2,50,20,'ACTIVE'),
(3,1,100,'ACTIVE'),(3,2,150,'ACTIVE'),(3,3,200,'ACTIVE'),(3,4,50,'ACTIVE'),(3,5,180,'ACTIVE'),
(3,6,130,'ACTIVE'),(3,7,170,'ACTIVE'),(3,8,90,'ACTIVE'),(3,9,80,'ACTIVE'),(3,10,70,'ACTIVE'),
(3,11,120,'ACTIVE'),(3,12,190,'ACTIVE'),(3,13,140,'ACTIVE'),(3,14,60,'ACTIVE'),(3,15,50,'ACTIVE'),
(3,16,65,'ACTIVE'),(3,17,240,'ACTIVE'),(3,18,80,'ACTIVE'),(3,19,150,'ACTIVE'),(3,20,90,'ACTIVE'),
(3,21,70,'ACTIVE'),(3,22,40,'ACTIVE'),(3,23,50,'ACTIVE'),(3,24,55,'ACTIVE'),(3,25,140,'ACTIVE'),
(3,26,170,'ACTIVE'),(3,27,120,'ACTIVE'),(3,28,240,'ACTIVE'),(3,29,70,'ACTIVE'),(3,30,90,'ACTIVE'),
(3,31,50,'ACTIVE'),(3,32,65,'ACTIVE'),(3,33,30,'ACTIVE'),(3,34,45,'ACTIVE'),(3,35,150,'ACTIVE'),
(3,36,110,'ACTIVE'),(3,37,80,'ACTIVE'),(3,38,90,'ACTIVE'),(3,39,60,'ACTIVE'),(3,40,50,'ACTIVE'),
(3,41,70,'ACTIVE'),(3,42,35,'ACTIVE'),(3,43,25,'ACTIVE'),(3,44,50,'ACTIVE'),(3,45,100,'ACTIVE'),
(3,46,140,'ACTIVE'),(3,47,50,'ACTIVE'),(3,48,40,'ACTIVE'),(3,49,20,'ACTIVE'),(3,50,15,'ACTIVE');
GO

-- 10. CUSTOMERS (30)
INSERT INTO customer (full_name, gender, bod, address, email, phone, total_spent, status) VALUES
(N'Nguyễn Thị Thu Hà',    N'Nữ',  '1992-03-15', N'12 Lý Thường Kiệt, Hà Nội',       'thuha.nt@email.com',      '091-200-0001', 2500000, 'ACTIVE'),
(N'Trần Văn Minh',        N'Nam', '1988-07-22', N'45 Nguyễn Du, Hà Nội',            'minh.tv@email.com',       '091-200-0002', 1800000, 'ACTIVE'),
(N'Lê Thị Quyên',         N'Nữ',  '1995-11-08', N'78 Trần Phú, Hà Nội',             'quyen.lt@email.com',      '091-200-0003', 3200000, 'ACTIVE'),
(N'Phạm Văn Tuấn',        N'Nam', '1990-05-30', N'23 Hoàng Diệu, Hà Nội',           'tuan.pv@email.com',       '091-200-0004', 950000, 'ACTIVE'),
(N'Hoàng Thị Lan',        N'Nữ',  '1998-09-12', N'56 Hàng Bài, Hà Nội',             'lan.ht@email.com',        '091-200-0005', 4100000, 'ACTIVE'),
(N'Đỗ Văn Hùng',          N'Nam', '1985-01-25', N'90 Láng Hạ, Hà Nội',             'hung.dv@email.com',       '091-200-0006', 670000, 'ACTIVE'),
(N'Ngô Thị Mai',          N'Nữ',  '2000-06-18', N'15 Giải Phóng, Hà Nội',           'mai.nt2@email.com',       '091-200-0007', 5200000, 'ACTIVE'),
(N'Vũ Văn Khánh',         N'Nam', '1993-12-05', N'34 Bà Triệu, Hà Nội',             'khanh.vv@email.com',      '091-200-0008', 1100000, 'ACTIVE'),
(N'Lý Thị Hồng',          N'Nữ',  '1996-04-20', N'67 Nguyễn Trãi, Hà Nội',          'hong.lt@email.com',       '091-200-0009', 7800000, 'ACTIVE'),
(N'Trương Văn Đạt',       N'Nam', '1991-08-14', N'89 Bạch Mai, Hà Nội',            'dat.tv@email.com',        '091-200-0010', 340000, 'ACTIVE'),
(N'Phan Thị Ngọc',        N'Nữ',  '1994-02-28', N'12 Lê Lợi, HCM',                  'ngoc.pt@email.com',       '091-200-0011', 6100000, 'ACTIVE'),
(N'Huỳnh Văn Tài',        N'Nam', '1989-10-10', N'56 Nguyễn Huệ, HCM',              'tai.hv@email.com',        '091-200-0012', 2300000, 'ACTIVE'),
(N'Đặng Thị Thắm',        N'Nữ',  '1997-07-03', N'78 Đồng Khởi, HCM',              'tham.dt@email.com',       '091-200-0013', 890000, 'ACTIVE'),
(N'Bùi Văn Lộc',          N'Nam', '1986-03-22', N'23 Ngô Đức Kế, HCM',             'loc.bv@email.com',        '091-200-0014', 4500000, 'ACTIVE'),
(N'Dương Thị Ánh',        N'Nữ',  '2001-11-15', N'90 Nam Kỳ Khởi Nghĩa, HCM',      'anh.dt2@email.com',      '091-200-0015', 1500000, 'ACTIVE'),
(N'Lâm Văn Sơn',          N'Nam', '1992-05-08', N'34 Điện Biên Phủ, HCM',           'son.lv@email.com',        '091-200-0016', 8200000, 'ACTIVE'),
(N'Trịnh Thị Thư',        N'Nữ',  '1995-09-27', N'67 Hai Bà Trưng, HCM',            'tu.tt@email.com',         '091-200-0017', 370000, 'ACTIVE'),
(N'Đoàn Văn Hải',         N'Nam', '1987-12-30', N'15 Phạm Ngũ Lão, HCM',            'hai.dv@email.com',        '091-200-0018', 2900000, 'ACTIVE'),
(N'Tạ Thị Loan',          N'Nữ',  '1999-04-12', N'45 Cống Quỳnh, HCM',              'loan.tt@email.com',       '091-200-0019', 6300000, 'ACTIVE'),
(N'Mai Văn Cường',        N'Nam', '1993-08-19', N'89 Nguyễn Đình Chiểu, HCM',       'cuong.mv@email.com',      '091-200-0020', 500000, 'ACTIVE'),
(N'Đinh Thị Huế',         N'Nữ',  '1996-01-05', N'12 Bạch Đằng, Đà Nẵng',           'hue.dt@email.com',        '091-200-0021', 3400000, 'ACTIVE'),
(N'Nguyễn Văn Tùng',      N'Nam', '1994-06-17', N'45 Nguyễn Văn Linh, Đà Nẵng',    'tung.nv@email.com',       '091-200-0022', 1200000, 'ACTIVE'),
(N'Võ Thị Trang',         N'Nữ',  '1998-03-29', N'78 Hùng Vương, Đà Nẵng',          'trang.vt@email.com',      '091-200-0023', 5600000, 'ACTIVE'),
(N'Cao Văn Phú',          N'Nam', '1990-10-11', N'23 Ông Ích Khiêm, Đà Nẵng',       'phu.cv@email.com',        '091-200-0024', 780000, 'ACTIVE'),
(N'Tôn Thị Nhung',        N'Nữ',  '2002-07-24', N'56 Lê Duẩn, Đà Nẵng',             'nhung.tt@email.com',      '091-200-0025', 2100000, 'ACTIVE'),
(N'Lương Văn Đức',        N'Nam', '1991-11-02', N'90 Trưng Nữ Vương, Đà Nẵng',      'duc.lv@email.com',        '091-200-0026', 4700000, 'ACTIVE'),
(N'Hồ Thị Yến',           N'Nữ',  '1997-05-16', N'15 Lê Lợi, Đà Nẵng',              'yen.ht@email.com',        '091-200-0027', 1500000, 'ACTIVE'),
(N'Dương Văn Bằng',       N'Nam', '1988-09-09', N'34 Núi Thành, Đà Nẵng',           'bang.dv@email.com',       '091-200-0028', 6900000, 'ACTIVE'),
(N'Kiều Thị Ngân',        N'Nữ',  '1995-02-14', N'67 Phan Chu Trinh, Đà Nẵng',      'ngan.kt@email.com',       '091-200-0029', 920000, 'ACTIVE'),
(N'Phùng Văn Hiếu',       N'Nam', '1993-12-01', N'89 Quang Trung, Đà Nẵng',         'hieu.pv@email.com',       '091-200-0030', 2800000, 'ACTIVE');
GO

-- 11. CUSTOMER POINTS
INSERT INTO customer_point (cus_id, current_points, lifetime_points) VALUES
(1,25,250),(2,18,180),(3,32,320),(4,10,95),(5,41,410),
(6,7,67),(7,52,520),(8,11,110),(9,78,780),(10,3,34),
(11,61,610),(12,23,230),(13,9,89),(14,45,450),(15,15,150),
(16,82,820),(17,4,37),(18,29,290),(19,63,630),(20,5,50),
(21,34,340),(22,12,120),(23,56,560),(24,8,78),(25,21,210),
(26,47,470),(27,15,150),(28,69,690),(29,9,92),(30,28,280);
GO

-- 12. VOUCHERS
INSERT INTO voucher (voucher_code, voucher_name, discount_type, discount_value, used_quantity, start_date, end_date, status) VALUES
('SALE10',   N'Giảm 10% đơn hàng',              'PERCENT', 10,   8,  '2026-01-01','2026-12-31','ACTIVE'),
('SALE20',   N'Giảm 20% đơn hàng',              'PERCENT', 20,   4,  '2026-01-01','2026-06-30','ACTIVE'),
('FIXED50',  N'Giảm 50,000đ đơn từ 500k',       'FIXED',   50000,5,  '2026-01-01','2026-12-31','ACTIVE'),
('FIXED30',  N'Giảm 30,000đ đơn từ 300k',       'FIXED',   30000,7,  '2026-01-01','2026-12-31','ACTIVE'),
('NEWYEAR',  N'Khuyến mãi Tết 2026',             'PERCENT', 15,   3,  '2026-01-20','2026-02-10','ACTIVE'),
('SUMMER',   N'Mùa hè sôi động giảm 25%',        'PERCENT', 25,   6,  '2026-06-01','2026-08-31','ACTIVE'),
('WELCOME',  N'Khách hàng mới giảm 10%',        'PERCENT', 10,   10, '2026-01-01','2026-12-31','ACTIVE'),
('FIXED20',  N'Giảm 20,000đ',                    'FIXED',   20000,12, '2026-03-01','2026-12-31','ACTIVE'),
('FIXED100', N'Giảm 100,000đ đơn từ 1 triệu',   'FIXED',   100000,2, '2026-04-01','2026-09-30','ACTIVE'),
('SALE5',    N'Giảm 5% đơn hàng',               'PERCENT', 5,    15, '2026-01-01','2026-12-31','ACTIVE');
GO

-- 13. ORDERS (100 SALE orders, Jan-Jun 2026)
INSERT INTO [order] (order_code, order_type, customer_id, branch_id, emp_id, voucher_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at) VALUES
('ORD00001','SALE', 1, 1, 6, null, 1, 245000, 0,     245000,   'CASH',         'COMPLETED', '2026-01-05 09:30:00'),
('ORD00002','SALE', 2, 1, 7, 1,    1, 180000, 18000, 162000,   'CASH',         'COMPLETED', '2026-01-06 14:15:00'),
('ORD00003','SALE', 3, 1, 8, null, 1, 520000, 0,     520000,   'BANK_TRANSFER','COMPLETED', '2026-01-08 10:00:00'),
('ORD00004','SALE', 11,2, 10,null, 2, 310000, 0,     310000,   'CASH',         'COMPLETED', '2026-01-10 11:45:00'),
('ORD00005','SALE', 12,2, 11,3,    2, 680000, 50000, 630000,   'BANK_TRANSFER','COMPLETED', '2026-01-12 16:30:00'),
('ORD00006','SALE', 21,3, 13,null, 3, 150000, 0,     150000,   'CASH',         'COMPLETED', '2026-01-13 08:20:00'),
('ORD00007','SALE', 22,3, 14,2,    3, 420000, 84000, 336000,   'BANK_TRANSFER','COMPLETED', '2026-01-15 15:10:00'),
('ORD00008','SALE', 4, 1, 9, null, 1, 89000,  0,     89000,    'CASH',         'COMPLETED', '2026-01-17 17:00:00'),
('ORD00009','SALE', 13,2, 12,null, 2, 920000, 0,     920000,   'BANK_TRANSFER','COMPLETED', '2026-01-18 13:30:00'),
('ORD00010','SALE', 5, 1, 6, 4,    1, 560000, 30000, 530000,   'CASH',         'COMPLETED', '2026-01-20 10:30:00'),
('ORD00011','SALE', 14,2, 10,1,    2, 280000, 28000, 252000,   'CASH',         'COMPLETED', '2026-01-22 09:00:00'),
('ORD00012','SALE', 23,3, 15,null, 3, 740000, 0,     740000,   'BANK_TRANSFER','COMPLETED', '2026-01-23 14:45:00'),
('ORD00013','SALE', 6, 1, 7, null, 1, 125000, 0,     125000,   'CASH',         'COMPLETED', '2026-01-25 16:15:00'),
('ORD00014','SALE', 24,3, 13,3,    3, 450000, 50000, 400000,   'BANK_TRANSFER','COMPLETED', '2026-01-28 11:00:00'),
('ORD00015','SALE', 7, 1, 8, null, 1, 680000, 0,     680000,   'CASH',         'COMPLETED', '2026-01-30 08:45:00'),
('ORD00016','SALE', 15,2, 11,5,    2, 350000, 52500, 297500,   'CASH',         'COMPLETED', '2026-02-02 09:30:00'),
('ORD00017','SALE', 8, 1, 6, null, 1, 210000, 0,     210000,   'BANK_TRANSFER','COMPLETED', '2026-02-04 14:00:00'),
('ORD00018','SALE', 25,3, 14,4,    3, 620000, 30000, 590000,   'CASH',         'COMPLETED', '2026-02-05 10:30:00'),
('ORD00019','SALE', 16,2, 12,8,    2, 175000, 20000, 155000,   'CASH',         'COMPLETED', '2026-02-07 15:45:00'),
('ORD00020','SALE', 9, 1, 9, null, 1, 890000, 0,     890000,   'BANK_TRANSFER','COMPLETED', '2026-02-08 11:15:00'),
('ORD00021','SALE', 26,3, 15,1,    3, 480000, 48000, 432000,   'BANK_TRANSFER','COMPLETED', '2026-02-10 08:00:00'),
('ORD00022','SALE', 17,2, 10,null, 2, 320000, 0,     320000,   'CASH',         'COMPLETED', '2026-02-12 16:30:00'),
('ORD00023','SALE', 10,1, 7, 2,    1, 760000, 152000,608000,   'BANK_TRANSFER','COMPLETED', '2026-02-14 13:00:00'),
('ORD00024','SALE', 27,3, 13,null, 3, 195000, 0,     195000,   'CASH',         'COMPLETED', '2026-02-16 09:45:00'),
('ORD00025','SALE', 18,2, 11,6,    2, 560000, 140000,420000,   'BANK_TRANSFER','COMPLETED', '2026-02-18 14:30:00'),
('ORD00026','SALE', 1, 1, 8, null, 1, 130000, 0,     130000,   'CASH',         'COMPLETED', '2026-02-20 10:00:00'),
('ORD00027','SALE', 28,3, 14,null, 3, 810000, 0,     810000,   'BANK_TRANSFER','COMPLETED', '2026-02-22 11:30:00'),
('ORD00028','SALE', 19,2, 12,4,    2, 420000, 30000, 390000,   'CASH',         'COMPLETED', '2026-02-25 15:00:00'),
('ORD00029','SALE', 2, 1, 6, 7,    1, 290000, 29000, 261000,   'CASH',         'COMPLETED', '2026-02-28 08:15:00'),
('ORD00030','SALE', 29,3, 15,null, 3, 350000, 0,     350000,   'CASH',         'COMPLETED', '2026-03-02 09:00:00'),
('ORD00031','SALE', 3, 1, 9, 3,    1, 510000, 50000, 460000,   'BANK_TRANSFER','COMPLETED', '2026-03-03 14:30:00'),
('ORD00032','SALE', 20,2, 10,8,    2, 780000, 20000, 760000,   'BANK_TRANSFER','COMPLETED', '2026-03-05 11:15:00'),
('ORD00033','SALE', 30,3, 13,1,    3, 220000, 22000, 198000,   'CASH',         'COMPLETED', '2026-03-07 10:30:00'),
('ORD00034','SALE', 11,2, 11,null, 2, 140000, 0,     140000,   'CASH',         'COMPLETED', '2026-03-08 16:00:00'),
('ORD00035','SALE', 4, 1, 7, null, 1, 670000, 0,     670000,   'BANK_TRANSFER','COMPLETED', '2026-03-10 08:45:00'),
('ORD00036','SALE', 12,2, 12,2,    2, 480000, 96000, 384000,   'CASH',         'COMPLETED', '2026-03-12 13:30:00'),
('ORD00037','SALE', 21,3, 14,5,    3, 920000, 138000,782000,   'BANK_TRANSFER','COMPLETED', '2026-03-14 09:00:00'),
('ORD00038','SALE', 5, 1, 6, null, 1, 185000, 0,     185000,   'CASH',         'COMPLETED', '2026-03-15 15:15:00'),
('ORD00039','SALE', 13,2, 10,null, 2, 410000, 0,     410000,   'BANK_TRANSFER','COMPLETED', '2026-03-17 10:30:00'),
('ORD00040','SALE', 22,3, 15,4,    3, 260000, 30000, 230000,   'CASH',         'COMPLETED', '2026-03-19 14:00:00'),
('ORD00041','SALE', 6, 1, 8, null, 1, 720000, 0,     720000,   'BANK_TRANSFER','COMPLETED', '2026-03-20 11:45:00'),
('ORD00042','SALE', 23,3, 13,3,    3, 580000, 50000, 530000,   'BANK_TRANSFER','COMPLETED', '2026-03-22 08:30:00'),
('ORD00043','SALE', 14,2, 11,6,    2, 340000, 85000, 255000,   'CASH',         'COMPLETED', '2026-03-24 16:15:00'),
('ORD00044','SALE', 24,3, 14,null, 3, 160000, 0,     160000,   'CASH',         'COMPLETED', '2026-03-26 09:30:00'),
('ORD00045','SALE', 7, 1, 9, 10,   1, 430000, 21500, 408500,   'BANK_TRANSFER','COMPLETED', '2026-03-28 14:00:00'),
('ORD00046','SALE', 15,2, 12,null, 2, 650000, 0,     650000,   'BANK_TRANSFER','COMPLETED', '2026-03-30 10:15:00'),
('ORD00047','SALE', 25,3, 15,null, 3, 500000, 0,     500000,   'CASH',         'COMPLETED', '2026-04-01 09:00:00'),
('ORD00048','SALE', 8, 1, 6, 1,    1, 270000, 27000, 243000,   'CASH',         'COMPLETED', '2026-04-03 14:30:00'),
('ORD00049','SALE', 16,2, 10,9,    2, 1250000,100000,1150000,  'BANK_TRANSFER','COMPLETED', '2026-04-05 11:00:00'),
('ORD00050','SALE', 26,3, 13,null, 3, 380000, 0,     380000,   'BANK_TRANSFER','COMPLETED', '2026-04-06 15:45:00'),
('ORD00051','SALE', 9, 1, 7, 4,    1, 610000, 30000, 580000,   'CASH',         'COMPLETED', '2026-04-08 10:30:00'),
('ORD00052','SALE', 17,2, 11,5,    2, 290000, 43500, 246500,   'CASH',         'COMPLETED', '2026-04-10 08:15:00'),
('ORD00053','SALE', 27,3, 14,null, 3, 840000, 0,     840000,   'BANK_TRANSFER','COMPLETED', '2026-04-12 16:00:00'),
('ORD00054','SALE', 10,1, 8, 2,    1, 520000, 104000,416000,   'BANK_TRANSFER','COMPLETED', '2026-04-14 13:30:00'),
('ORD00055','SALE', 18,2, 12,null, 2, 195000, 0,     195000,   'CASH',         'COMPLETED', '2026-04-15 09:45:00'),
('ORD00056','SALE', 28,3, 15,8,    3, 730000, 20000, 710000,   'BANK_TRANSFER','COMPLETED', '2026-04-17 11:15:00'),
('ORD00057','SALE', 1, 1, 9, null, 1, 460000, 0,     460000,   'CASH',         'COMPLETED', '2026-04-19 14:00:00'),
('ORD00058','SALE', 19,2, 10,null, 2, 140000, 0,     140000,   'CASH',         'COMPLETED', '2026-04-21 10:30:00'),
('ORD00059','SALE', 29,3, 13,1,    3, 360000, 36000, 324000,   'BANK_TRANSFER','COMPLETED', '2026-04-23 08:00:00'),
('ORD00060','SALE', 2, 1, 6, 6,    1, 810000, 202500,607500,  'BANK_TRANSFER','COMPLETED', '2026-04-25 15:30:00'),
('ORD00061','SALE', 20,2, 11,3,    2, 570000, 50000, 520000,   'CASH',         'COMPLETED', '2026-04-27 09:00:00'),
('ORD00062','SALE', 30,3, 14,null, 3, 300000, 0,     300000,   'CASH',         'COMPLETED', '2026-04-29 14:15:00'),
('ORD00063','SALE', 3, 1, 7, null, 1, 430000, 0,     430000,   'BANK_TRANSFER','COMPLETED', '2026-04-30 10:00:00'),
('ORD00064','SALE', 21,3, 15,7,    3, 220000, 22000, 198000,   'CASH',         'COMPLETED', '2026-05-02 08:30:00'),
('ORD00065','SALE', 11,2, 10,null, 2, 690000, 0,     690000,   'BANK_TRANSFER','COMPLETED', '2026-05-04 11:00:00'),
('ORD00066','SALE', 22,3, 13,4,    3, 480000, 30000, 450000,   'BANK_TRANSFER','COMPLETED', '2026-05-05 14:45:00'),
('ORD00067','SALE', 4, 1, 8, null, 1, 155000, 0,     155000,   'CASH',         'COMPLETED', '2026-05-07 09:15:00'),
('ORD00068','SALE', 12,2, 11,5,    2, 840000, 126000,714000,   'BANK_TRANSFER','COMPLETED', '2026-05-09 16:00:00'),
('ORD00069','SALE', 23,3, 14,10,   3, 910000, 45500, 864500,   'BANK_TRANSFER','COMPLETED', '2026-05-11 10:30:00'),
('ORD00070','SALE', 5, 1, 9, 1,    1, 370000, 37000, 333000,   'CASH',         'COMPLETED', '2026-05-13 13:00:00'),
('ORD00071','SALE', 24,3, 15,null, 3, 130000, 0,     130000,   'CASH',         'COMPLETED', '2026-05-15 08:00:00'),
('ORD00072','SALE', 13,2, 12,2,    2, 660000, 132000,528000,   'BANK_TRANSFER','COMPLETED', '2026-05-17 15:30:00'),
('ORD00073','SALE', 6, 1, 6, null, 1, 520000, 0,     520000,   'CASH',         'COMPLETED', '2026-05-18 11:45:00'),
('ORD00074','SALE', 25,3, 13,null, 3, 380000, 0,     380000,   'BANK_TRANSFER','COMPLETED', '2026-05-20 09:30:00'),
('ORD00075','SALE', 14,2, 10,8,    2, 215000, 20000, 195000,   'CASH',         'COMPLETED', '2026-05-22 14:00:00'),
('ORD00076','SALE', 26,3, 14,3,    3, 750000, 50000, 700000,   'BANK_TRANSFER','COMPLETED', '2026-05-24 10:15:00'),
('ORD00077','SALE', 7, 1, 7, null, 1, 290000, 0,     290000,   'CASH',         'COMPLETED', '2026-05-25 16:30:00'),
('ORD00078','SALE', 15,2, 11,null, 2, 820000, 0,     820000,   'BANK_TRANSFER','COMPLETED', '2026-05-27 08:45:00'),
('ORD00079','SALE', 27,3, 15,6,    3, 460000, 115000,345000,   'CASH',         'COMPLETED', '2026-05-29 13:00:00'),
('ORD00080','SALE', 8, 1, 8, null, 1, 610000, 0,     610000,   'BANK_TRANSFER','COMPLETED', '2026-05-30 11:30:00'),
('ORD00081','SALE', 16,2, 12,5,    2, 390000, 58500, 331500,   'BANK_TRANSFER','COMPLETED', '2026-05-31 09:00:00'),
('ORD00082','SALE', 17,2, 10,null, 2, 540000, 0,     540000,   'CASH',         'COMPLETED', '2026-06-01 10:00:00'),
('ORD00083','SALE', 28,3, 13,1,    3, 280000, 28000, 252000,   'CASH',         'COMPLETED', '2026-06-03 14:30:00'),
('ORD00084','SALE', 9, 1, 9, null, 1, 720000, 0,     720000,   'BANK_TRANSFER','COMPLETED', '2026-06-04 09:15:00'),
('ORD00085','SALE', 29,3, 14,4,    3, 650000, 30000, 620000,   'BANK_TRANSFER','COMPLETED', '2026-06-06 11:45:00'),
('ORD00086','SALE', 10,1, 6, 2,    1, 420000, 84000, 336000,   'CASH',         'COMPLETED', '2026-06-08 16:00:00'),
('ORD00087','SALE', 18,2, 11,null, 2, 180000, 0,     180000,   'CASH',         'COMPLETED', '2026-06-09 08:30:00'),
('ORD00088','SALE', 30,3, 15,10,   3, 510000, 25500, 484500,   'BANK_TRANSFER','COMPLETED', '2026-06-11 13:00:00'),
('ORD00089','SALE', 1, 1, 7, 3,    1, 890000, 50000, 840000,   'BANK_TRANSFER','COMPLETED', '2026-06-13 10:30:00'),
('ORD00090','SALE', 19,2, 12,6,    2, 340000, 85000, 255000,   'CASH',         'COMPLETED', '2026-06-15 15:15:00'),
('ORD00091','SALE', 2, 1, 8, null, 1, 210000, 0,     210000,   'CASH',         'COMPLETED', '2026-06-16 09:00:00'),
('ORD00092','SALE', 20,2, 10,8,    2, 770000, 20000, 750000,   'BANK_TRANSFER','COMPLETED', '2026-06-18 14:45:00'),
('ORD00093','SALE', 21,3, 13,null, 3, 420000, 0,     420000,   'BANK_TRANSFER','COMPLETED', '2026-06-20 11:00:00'),
('ORD00094','SALE', 3, 1, 6, 5,    1, 580000, 87000, 493000,   'CASH',         'COMPLETED', '2026-06-22 08:15:00'),
('ORD00095','SALE', 22,3, 14,null, 3, 190000, 0,     190000,   'CASH',         'COMPLETED', '2026-06-23 16:30:00'),
('ORD00096','SALE', 4, 1, 9, 1,    1, 440000, 44000, 396000,   'BANK_TRANSFER','COMPLETED', '2026-06-25 10:00:00'),
('ORD00097','SALE', 23,3, 15,7,    3, 710000, 71000, 639000,   'BANK_TRANSFER','COMPLETED', '2026-06-26 13:45:00'),
('ORD00098','SALE', 5, 1, 7, null, 1, 350000, 0,     350000,   'CASH',         'COMPLETED', '2026-06-27 09:30:00'),
('ORD00099','SALE', 24,3, 13,2,    3, 280000, 56000, 224000,   'CASH',         'COMPLETED', '2026-06-28 14:00:00'),
('ORD00100','SALE', 6, 1, 8, null, 1, 630000, 0,     630000,   'BANK_TRANSFER','COMPLETED', '2026-06-29 11:15:00');
GO

-- 14. ORDER DETAILS (~400 records, each order has 1-5 products)
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(1,1,2,10000,20000),(1,13,1,10000,10000),(1,4,1,45000,45000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(2,5,3,8000,24000),(2,12,2,7000,14000),(2,16,1,25000,25000),(2,8,2,9000,18000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(3,22,2,120000,240000),(3,14,2,6000,12000),(3,6,1,45000,45000),(3,47,1,180000,180000),(3,49,2,18000,36000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(4,36,3,28000,84000),(4,9,2,8000,16000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(5,7,2,8000,16000),(5,42,1,32000,32000),(5,26,3,12000,36000),(5,44,2,15000,30000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(6,9,3,8000,24000),(6,16,1,25000,25000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(7,3,5,5000,25000),(7,20,2,7000,14000),(7,24,1,6000,6000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(8,8,2,9000,18000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(9,22,3,120000,360000),(9,15,2,7000,14000),(9,38,1,12000,12000),(9,45,1,38000,38000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(10,13,2,10000,20000),(10,42,1,32000,32000),(10,21,1,95000,95000);
GO
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(11,33,4,35000,140000),(11,5,2,8000,16000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(12,41,3,25000,75000),(12,7,2,8000,16000),(12,3,10,5000,50000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(13,40,2,12000,24000),(13,35,3,28000,84000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(14,44,3,15000,45000),(14,17,2,8000,16000),(14,11,4,6000,24000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(15,22,1,120000,120000),(15,27,2,4000,8000),(15,14,2,6000,12000),(15,5,4,8000,32000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(16,19,3,5000,15000),(16,42,1,32000,32000),(16,35,2,28000,56000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(17,10,2,12000,24000),(17,4,1,45000,45000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(18,36,2,28000,56000),(18,45,1,38000,38000),(18,1,3,10000,30000),(18,17,2,8000,16000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(19,8,2,9000,18000),(19,20,1,7000,7000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(20,22,2,120000,240000),(20,41,1,25000,25000),(20,15,2,7000,14000),(20,27,2,4000,8000),(20,6,1,45000,45000);
GO
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(86,40,1,12000,12000),(86,45,1,38000,38000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(87,11,4,6000,24000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(88,22,2,120000,240000),(88,33,1,35000,35000),(88,15,2,7000,14000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(89,22,3,120000,360000),(89,41,1,25000,25000),(89,7,3,8000,24000),(89,44,2,15000,30000),(89,13,2,10000,20000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(90,17,3,8000,24000),(90,10,2,12000,24000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(91,37,2,22000,44000),(91,3,4,5000,20000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(92,22,2,120000,240000),(92,33,1,35000,35000),(92,1,3,10000,30000),(92,5,2,8000,16000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(93,2,4,10000,40000),(93,19,2,5000,10000),(93,10,2,12000,24000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(94,22,1,120000,120000),(94,42,1,32000,32000),(94,36,2,28000,56000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(95,35,3,28000,84000),(95,11,2,6000,12000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(96,13,3,10000,30000),(96,44,2,15000,30000),(96,9,2,8000,16000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(97,22,2,120000,240000),(97,4,1,45000,45000),(97,33,1,35000,35000),(97,15,3,7000,21000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(98,20,3,7000,21000),(98,36,2,28000,56000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(99,37,2,22000,44000),(99,17,2,8000,16000),(99,27,2,4000,8000);
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(100,22,1,120000,120000),(100,9,4,8000,32000),(100,1,3,10000,30000),(100,35,2,28000,56000);
GO

-- 15. PAYMENTS (for all 100 orders)
INSERT INTO payment (order_id, payment_amount, payment_date, payment_status, transaction_code, PaymentType, Description, EmployeeID, BranchID)
SELECT o.order_id, o.total_amount, o.created_at, 'PAID', 'TXN-' + o.order_code,
       CASE WHEN o.order_type = 'SALE' THEN 'INCOME' ELSE 'EXPENSE' END,
       N'Thanh toán đơn hàng ' + o.order_code,
       o.emp_id, o.branch_id
FROM [order] o;
GO

-- 16. POINT TRANSACTIONS
INSERT INTO point_transaction (cus_point_id, order_id, before_points, after_points, description, created_at)
SELECT cp.cus_point_id, o.order_id,
       cp.current_points - (o.total_amount / 100000) AS before_points,
       cp.current_points AS after_points,
       N'Earned ' + CAST((o.total_amount / 100000) AS NVARCHAR) + N' points from Order ' + o.order_code,
       o.created_at
FROM [order] o
JOIN customer_point cp ON cp.cus_id = o.customer_id
WHERE o.status = 'COMPLETED' AND o.customer_id IS NOT NULL AND o.total_amount >= 100000;

INSERT INTO point_transaction (cus_point_id, order_id, before_points, after_points, description, created_at)
SELECT cp.cus_point_id, o.order_id, cp.current_points, cp.current_points,
       N'Earned 0 points from Order ' + o.order_code + N' (below minimum)', o.created_at
FROM [order] o
JOIN customer_point cp ON cp.cus_id = o.customer_id
WHERE o.status = 'COMPLETED' AND o.customer_id IS NOT NULL AND o.total_amount < 100000;
GO

-- 17. STOCK TRANSFERS (10 transfers)
INSERT INTO stock_transfer (from_warehouse_id, to_warehouse_id, transfer_code, transfer_date, status, note, created_by) VALUES
(1,2,'TRF-00001','2026-02-15 09:00:00','COMPLETED',N'Chuyển hàng từ Hà Nội vào HCM',3),
(1,3,'TRF-00002','2026-03-10 14:30:00','COMPLETED',N'Chuyển hàng từ Hà Nội vào Đà Nẵng',3),
(2,1,'TRF-00003','2026-04-05 10:00:00','COMPLETED',N'Chuyển hàng từ HCM ra Hà Nội',4),
(2,3,'TRF-00004','2026-04-20 15:45:00','COMPLETED',N'Chuyển hàng từ HCM vào Đà Nẵng',4),
(3,1,'TRF-00005','2026-05-12 08:30:00','COMPLETED',N'Chuyển hàng từ Đà Nẵng ra Hà Nội',5),
(3,2,'TRF-00006','2026-05-25 11:15:00','COMPLETED',N'Chuyển hàng từ Đà Nẵng vào HCM',5),
(1,2,'TRF-00007','2026-06-02 13:00:00','COMPLETED',N'Bổ sung hàng cho HCM đợt 2',3),
(2,3,'TRF-00008','2026-06-10 09:30:00','COMPLETED',N'Bổ sung hàng cho Đà Nẵng đợt 2',4),
(1,3,'TRF-00009','2026-06-18 14:00:00','COMPLETED',N'Bổ sung hàng cho Đà Nẵng đợt 3',3),
(2,1,'TRF-00010','2026-06-25 10:45:00','COMPLETED',N'Bổ sung hàng cho Hà Nội',4);
GO

INSERT INTO stock_transfer_detail (stock_transfer_id, product_id, quantity) VALUES
(1,1,30),(1,2,25),(1,3,40),
(2,22,10),(2,33,15),(2,45,20),
(3,11,50),(3,14,30),
(4,36,20),(4,42,15),
(5,5,40),(5,8,30),
(6,13,35),(6,20,25),
(7,1,20),(7,2,30),(7,22,15),
(8,36,15),(8,42,10),
(9,3,25),(9,5,20),
(10,11,30),(10,14,20);
GO

-- 18. STOCK TRANSACTIONS
INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by, created_at)
SELECT o.warehouse_id, od.product_id, 'ORDER', od.order_id, 'SALE_DEDUCT', od.quantity, 0, 0,
       N'Deducted ' + CAST(od.quantity AS NVARCHAR) + N' units for Order ' + o.order_code, o.emp_id, o.created_at
FROM order_detail od
JOIN [order] o ON od.order_id = o.order_id
WHERE o.status = 'COMPLETED';
GO

INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by, created_at)
SELECT st.from_warehouse_id, std.product_id, 'STOCK_TRANSFER', st.stock_transfer_id, 'TRANSFER_OUT', std.quantity, 0, 0, N'Transferred out to warehouse', st.created_by, st.transfer_date
FROM stock_transfer_detail std
JOIN stock_transfer st ON std.stock_transfer_id = st.stock_transfer_id;
GO

INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by, created_at)
SELECT st.to_warehouse_id, std.product_id, 'STOCK_TRANSFER', st.stock_transfer_id, 'TRANSFER_IN', std.quantity, 0, 0, N'Transferred in from warehouse', st.created_by, st.transfer_date
FROM stock_transfer_detail std
JOIN stock_transfer st ON std.stock_transfer_id = st.stock_transfer_id;
GO

-- 19. AUDIT LOGS
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT emp_id, 'LOGIN', 'Employee', emp_id, NULL, NULL, DATEADD(HOUR, -ABS(CHECKSUM(NEWID())) % 720, GETDATE())
FROM Employee WHERE emp_id <= 15;

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT CASE WHEN c.cus_id % 3 = 0 THEN 6 WHEN c.cus_id % 3 = 1 THEN 7 ELSE 8 END,
       'CREATE', 'Customer', c.cus_id, NULL, N'Created customer ' + c.full_name, DATEADD(DAY, -c.cus_id * 2, '2026-06-29')
FROM customer c;

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT 1, 'CREATE', 'Employee', e.emp_id, NULL, N'Created employee ' + e.fullName, '2026-01-01'
FROM Employee e WHERE e.emp_id > 1;

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT o.emp_id, 'CREATE', 'Order', o.order_id, NULL, N'POS checkout: ' + o.order_code, o.created_at
FROM [order] o WHERE o.status = 'COMPLETED';

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT 6 + (c.cus_id % 10), 'REDEEM_POINTS', 'Customer', c.cus_id, NULL, CAST(cp.current_points / 2 AS NVARCHAR), DATEADD(DAY, -5, GETDATE())
FROM customer c
JOIN customer_point cp ON cp.cus_id = c.cus_id
WHERE c.cus_id % 3 = 0 AND cp.current_points >= 50;

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
VALUES (1, 'LOCK', 'Employee', 5, 'ACTIVE', 'INACTIVE', '2026-03-15 10:30:00');

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
VALUES (1, 'UNLOCK', 'Employee', 5, 'INACTIVE', 'ACTIVE', '2026-03-20 14:00:00');

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT st.created_by, 'STOCK_TRANSFER', 'stock_transfer', st.stock_transfer_id, NULL, N'Transfer ' + st.transfer_code, st.transfer_date
FROM stock_transfer st;

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT 3 + (p.product_id % 3), 'UPDATE', 'Product', p.product_id,
       CAST(p.selling_price * 0.9 AS NVARCHAR), CAST(p.selling_price AS NVARCHAR),
       DATEADD(DAY, -p.product_id, '2026-06-01')
FROM product p WHERE p.product_id % 5 = 0;
GO

-- 20. SUPPLIER PRODUCT (sample data)
INSERT INTO supplier_product (supplier_id, product_id, import_price) VALUES
(1, 1, 100000.00), (1, 2, 200000.00), (1, 3, 250000.00),
(2, 4, 80000.00), (2, 8, 300000.00),
(3, 6, 120000.00), (3, 7, 150000.00);
GO

PRINT N'FinoraRetail database created successfully.';
GO

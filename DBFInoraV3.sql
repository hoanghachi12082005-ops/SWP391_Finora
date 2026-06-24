CREATE DATABASE DBFinoraV3

USE DBFinoraV3
GO

-- ============================================================
--  1. role
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
--  2. branch
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
--  3. employee
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
    passwordHash  NVARCHAR(255),
    image_URL     NVARCHAR(255),
    status        NVARCHAR(20)  DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at    DATETIME      DEFAULT GETDATE(),
    update_at     DATETIME      DEFAULT GETDATE(),

    CONSTRAINT FK_Employee_Branch
        FOREIGN KEY (branch_id) REFERENCES Branch(branch_id),

    CONSTRAINT FK_Employee_Role
        FOREIGN KEY (role_id) REFERENCES [Role](role_id)
);
GO

-- ============================================================
--  5. customer
-- ============================================================
CREATE TABLE customer (
    cus_id      INT           IDENTITY(1,1) PRIMARY KEY,
    full_name   NVARCHAR(150) NOT NULL,
    gender      NVARCHAR(10),
    bod         DATE,
    address     NVARCHAR(300),
    email       NVARCHAR(150),
    phone       NVARCHAR(20) UNIQUE,
    cus_type    NVARCHAR(50),
    total_spent DECIMAL(18,2) DEFAULT 0,
    created_at  DATETIME      DEFAULT GETDATE(),
    updated_at  DATETIME      DEFAULT GETDATE()
);
GO

-- ============================================================
--  6. customer_point
-- ============================================================
CREATE TABLE customer_point (
    cus_point_id    INT           IDENTITY(1,1) PRIMARY KEY,
    cus_id          INT           NOT NULL UNIQUE,
    current_points  INT           DEFAULT 0,
    lifetime_points INT           DEFAULT 0,
    level_name      NVARCHAR(50),
    updated_at      DATETIME      DEFAULT GETDATE(),

    CONSTRAINT FK_CustomerPoint_Customer
        FOREIGN KEY (cus_id) REFERENCES customer(cus_id)
);
GO

-- ============================================================
--  7. voucher
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
    status         NVARCHAR(20)   DEFAULT 'active',
    created_at     DATETIME       DEFAULT GETDATE()
);
GO

-- ============================================================
--  8. supplier
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
--  9. warehouse
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
--  10. unit
-- ============================================================
CREATE TABLE unit (
    unit_id     INT           IDENTITY(1,1) PRIMARY KEY,
    unit_name   NVARCHAR(50)  NOT NULL,
    description NVARCHAR(255)
);
GO

-- ============================================================
--  11. category
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
--  12. product
-- ============================================================
CREATE TABLE [product] (
    product_id      INT            IDENTITY(1,1) PRIMARY KEY,
    product_codebar NVARCHAR(50)   UNIQUE,
    product_name    NVARCHAR(200)  NOT NULL,
    category_id     INT,
    unit_id         INT,
    selling_price   DECIMAL(18,2)  DEFAULT 0,
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
--  13. inventory
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
--  14. order
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
--  15. order_detail
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
--  16. payment
-- ============================================================
CREATE TABLE payment (
    payment_id       INT            IDENTITY(1,1) PRIMARY KEY,
    order_id         INT            NOT NULL,
    payment_amount   DECIMAL(18,2)  DEFAULT 0,
    payment_date     DATETIME       DEFAULT GETDATE(),
    payment_status   NVARCHAR(30)   DEFAULT 'PENDING'
                                    CHECK (payment_status IN ('PENDING','PAID','FAILED')),
    transaction_code NVARCHAR(100),

    CONSTRAINT FK_Payment_Order
        FOREIGN KEY (order_id)
        REFERENCES [order](order_id)
);
GO

-- ============================================================
--  17. point_transaction
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
--  18. stock_transfer
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
--  19. stock_transfer_detail
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
--  20. stock_transaction
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
--  21. audit_log
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


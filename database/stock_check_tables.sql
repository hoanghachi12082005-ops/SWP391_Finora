USE DBFinoraV3
GO

-- ============================================================
--  1. stock_check
-- ============================================================
CREATE TABLE stock_check (
    stock_check_id  INT           IDENTITY(1,1) PRIMARY KEY,
    warehouse_id    INT           NOT NULL,
    check_code      NVARCHAR(50)  UNIQUE,
    check_date      DATETIME      DEFAULT GETDATE(),
    status          NVARCHAR(30)  DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    note            NVARCHAR(500),
    created_by      INT,
    approved_by     INT,
    approved_at     DATETIME,

    CONSTRAINT FK_StockCheck_Warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id),
    CONSTRAINT FK_StockCheck_CreatedBy
        FOREIGN KEY (created_by) REFERENCES employee(emp_id),
    CONSTRAINT FK_StockCheck_ApprovedBy
        FOREIGN KEY (approved_by) REFERENCES employee(emp_id)
);
GO

-- ============================================================
--  2. stock_check_detail
-- ============================================================
CREATE TABLE stock_check_detail (
    stock_check_detail_id  INT  IDENTITY(1,1) PRIMARY KEY,
    stock_check_id         INT  NOT NULL,
    product_id             INT  NOT NULL,
    system_quantity        INT  DEFAULT 0,
    actual_quantity        INT  DEFAULT 0,
    difference             AS (actual_quantity - system_quantity),
    note                   NVARCHAR(255),

    CONSTRAINT FK_StockCheckDetail_StockCheck
        FOREIGN KEY (stock_check_id) REFERENCES stock_check(stock_check_id),
    CONSTRAINT FK_StockCheckDetail_Product
        FOREIGN KEY (product_id) REFERENCES product(product_id)
);
GO

-- Insert sample data
INSERT INTO stock_check (warehouse_id, check_code, status, note, created_by)
VALUES (1, 'CHK-20260624-001', 'PENDING', N'Kiểm kê định kỳ tháng 6', 1);

INSERT INTO stock_check_detail (stock_check_id, product_id, system_quantity, actual_quantity, note)
VALUES 
(1, 1, 50, 48, N'Mất 2 sản phẩm chưa rõ nguyên nhân'),
(1, 2, 30, 32, N'Dư 2 sản phẩm do nhập nhầm từ lô trước');
GO

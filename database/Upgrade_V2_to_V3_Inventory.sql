-- =========================================================================
-- SCRIPT NÂNG CẤP PHẦN KHO (INVENTORY) TỪ BẢN V2 LÊN V3 (SỬ DỤNG RENAME)
-- Mục đích: Đổi tên cột, cập nhật cấu trúc bảng cho tương thích với Code V3
-- nhưng VẪN GIỮ NGUYÊN data và Khóa Ngoại (Foreign Key)
-- Chạy trên database: DBFinoraV2
-- =========================================================================
USE DBFinoraV2
GO

-- =========================================================================
-- 1. RENAME BẢNG WAREHOUSE VÀ CÁC CỘT
-- =========================================================================
EXEC sp_rename 'Warehouse.WarehouseID', 'warehouse_id', 'COLUMN';
EXEC sp_rename 'Warehouse.Name', 'warehouse_name', 'COLUMN';
EXEC sp_rename 'Warehouse.BranchID', 'branch_id', 'COLUMN';
EXEC sp_rename 'Warehouse.Address', 'address', 'COLUMN';
EXEC sp_rename 'Warehouse.Status', 'status', 'COLUMN';
EXEC sp_rename 'Warehouse.CreatedAt', 'created_at', 'COLUMN';
EXEC sp_rename 'Warehouse', 'warehouse';
GO

-- =========================================================================
-- 2. RENAME BẢNG INVENTORY VÀ CÁC CỘT
-- =========================================================================
EXEC sp_rename 'Inventory.InventoryID', 'inventory_id', 'COLUMN';
EXEC sp_rename 'Inventory.WarehouseID', 'warehouse_id', 'COLUMN';
EXEC sp_rename 'Inventory.ProductID', 'product_id', 'COLUMN';
EXEC sp_rename 'Inventory.QuantityInStock', 'quantity_in_stock', 'COLUMN';
EXEC sp_rename 'Inventory.UpdatedAt', 'updated_at', 'COLUMN';

-- Thêm cột status cho bảng inventory (bản V3 có thêm cột này)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Inventory') AND name = 'status')
BEGIN
    ALTER TABLE Inventory ADD status NVARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','OUT_OF_STOCK'));
END
GO
EXEC sp_rename 'Inventory', 'inventory';
GO

-- =========================================================================
-- 3. RENAME BẢNG STOCKTRANSFER VÀ CÁC CỘT
-- =========================================================================
EXEC sp_rename 'StockTransfer.StockTransferID', 'stock_transfer_id', 'COLUMN';
EXEC sp_rename 'StockTransfer.FromWarehouseID', 'from_warehouse_id', 'COLUMN';
EXEC sp_rename 'StockTransfer.ToWarehouseID', 'to_warehouse_id', 'COLUMN';
EXEC sp_rename 'StockTransfer.TransferCode', 'transfer_code', 'COLUMN';
EXEC sp_rename 'StockTransfer.TransferDate', 'transfer_date', 'COLUMN';
EXEC sp_rename 'StockTransfer.Status', 'status', 'COLUMN';
EXEC sp_rename 'StockTransfer.Note', 'note', 'COLUMN';
EXEC sp_rename 'StockTransfer.CreatedBy', 'created_by', 'COLUMN';
EXEC sp_rename 'StockTransfer', 'stock_transfer';
GO

-- =========================================================================
-- 4. RENAME BẢNG STOCKTRANSFERDETAIL VÀ CÁC CỘT
-- =========================================================================
EXEC sp_rename 'StockTransferDetail.StockTransferDetailID', 'stock_transfer_detail_id', 'COLUMN';
EXEC sp_rename 'StockTransferDetail.StockTransferID', 'stock_transfer_id', 'COLUMN';
EXEC sp_rename 'StockTransferDetail.ProductID', 'product_id', 'COLUMN';
EXEC sp_rename 'StockTransferDetail.Quantity', 'quantity', 'COLUMN';
EXEC sp_rename 'StockTransferDetail', 'stock_transfer_detail';
GO

-- =========================================================================
-- 5. RENAME BẢNG STOCKTRANSACTION VÀ CÁC CỘT
-- =========================================================================
EXEC sp_rename 'StockTransaction.StockTransactionID', 'stock_transaction_id', 'COLUMN';
EXEC sp_rename 'StockTransaction.WarehouseID', 'warehouse_id', 'COLUMN';
EXEC sp_rename 'StockTransaction.ProductID', 'product_id', 'COLUMN';
EXEC sp_rename 'StockTransaction.ReferenceType', 'reference_type', 'COLUMN';
EXEC sp_rename 'StockTransaction.ReferenceID', 'reference_id', 'COLUMN';
EXEC sp_rename 'StockTransaction.TransactionType', 'transaction_type', 'COLUMN';
EXEC sp_rename 'StockTransaction.Quantity', 'quantity', 'COLUMN';
EXEC sp_rename 'StockTransaction.BeforeQuantity', 'before_quantity', 'COLUMN';
EXEC sp_rename 'StockTransaction.AfterQuantity', 'after_quantity', 'COLUMN';
EXEC sp_rename 'StockTransaction.Note', 'note', 'COLUMN';
EXEC sp_rename 'StockTransaction.CreatedBy', 'created_by', 'COLUMN';
EXEC sp_rename 'StockTransaction.CreatedAt', 'created_at', 'COLUMN';
EXEC sp_rename 'StockTransaction', 'stock_transaction';
GO

-- =========================================================================
-- 6. TẠO BẢNG STOCK_CHECK VÀ STOCK_CHECK_DETAIL (TÍNH NĂNG MỚI CỦA V3)
-- =========================================================================

-- Bảng stock_check (Mới)
IF OBJECT_ID('stock_check', 'U') IS NULL
BEGIN
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
            FOREIGN KEY (created_by) REFERENCES Employee(EmployeeID),
        CONSTRAINT FK_StockCheck_ApprovedBy
            FOREIGN KEY (approved_by) REFERENCES Employee(EmployeeID)
    );
END
GO

-- Bảng stock_check_detail (Mới)
IF OBJECT_ID('stock_check_detail', 'U') IS NULL
BEGIN
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
            FOREIGN KEY (product_id) REFERENCES Product(ProductID)
    );
END
GO

-- =========================================================================
-- 7. THÊM DATA MẪU CHO PHIẾU KIỂM KHO DỰA TRÊN DATA CÓ SẴN (LẤY ĐỘNG)
-- =========================================================================
DECLARE @FirstWarehouseId INT;
DECLARE @FirstProductId INT;
DECLARE @SecondProductId INT;
DECLARE @SysQty1 INT;
DECLARE @SysQty2 INT;
DECLARE @AdminId INT;

-- Lấy ngẫu nhiên kho đầu tiên và 2 sản phẩm đầu tiên của kho đó
SELECT TOP 1 @FirstWarehouseId = warehouse_id FROM warehouse;
SELECT TOP 1 @FirstProductId = product_id, @SysQty1 = quantity_in_stock FROM inventory WHERE warehouse_id = @FirstWarehouseId ORDER BY product_id ASC;
SELECT TOP 1 @SecondProductId = product_id, @SysQty2 = quantity_in_stock FROM inventory WHERE warehouse_id = @FirstWarehouseId AND product_id <> @FirstProductId ORDER BY product_id ASC;
SELECT TOP 1 @AdminId = EmployeeID FROM Employee;

-- Chèn dữ liệu mẫu cho phiếu kiểm kho
IF @FirstWarehouseId IS NOT NULL AND @FirstProductId IS NOT NULL
BEGIN
    INSERT INTO stock_check (warehouse_id, check_code, status, note, created_by)
    VALUES (@FirstWarehouseId, 'CHK-20260624-001', 'PENDING', N'Kiểm kê định kỳ mẫu', @AdminId);

    DECLARE @CheckId INT = SCOPE_IDENTITY();

    INSERT INTO stock_check_detail (stock_check_id, product_id, system_quantity, actual_quantity, note)
    VALUES (@CheckId, @FirstProductId, @SysQty1, @SysQty1 - 2, N'Mất 2 sản phẩm chưa rõ nguyên nhân');

    IF @SecondProductId IS NOT NULL
    BEGIN
        INSERT INTO stock_check_detail (stock_check_id, product_id, system_quantity, actual_quantity, note)
        VALUES (@CheckId, @SecondProductId, @SysQty2, @SysQty2, N'Khớp số lượng');
    END
END
GO

PRINT 'Nang cap phan kho thanh cong!'

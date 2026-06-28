IF DB_ID(N'DBFinoraV2') IS NOT NULL
BEGIN
    ALTER DATABASE DBFinoraV2 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE DBFinoraV2;
END
GO

CREATE DATABASE DBFinoraV2;
GO

USE DBFinoraV2;
GO

-- ============================================================
--  1. Role
--  Naming matches Java code: RoleID, Name, Description
-- ============================================================
CREATE TABLE Role (
    RoleID      INT IDENTITY(1,1) PRIMARY KEY,
    Name        NVARCHAR(100) NOT NULL,
    Description NVARCHAR(255) NULL,
    CreatedAt   DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt   DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Role_Name UNIQUE (Name)
);
GO

-- Default roles used by UserManagementDao
INSERT INTO Role (Name, Description)
VALUES
    (N'Admin', N'System administrator'),
    (N'Owner', N'Business owner'),
    (N'StoreManager', N'Branch/store manager'),
    (N'SalesStaff', N'Sales employee'),
    (N'WarehouseStaff', N'Warehouse employee');
GO

-- ============================================================
--  2. Branch
--  Naming matches Java code: BranchID, Name, Address, Phone, Status
-- ============================================================
CREATE TABLE Branch (
    BranchID    INT IDENTITY(1,1) PRIMARY KEY,
    Name        NVARCHAR(150) NOT NULL,
    BranchCode  NVARCHAR(50) NULL,
    Address     NVARCHAR(300) NULL,
    Phone       NVARCHAR(20) NULL,
    Email       NVARCHAR(150) NULL,
    OpeningTime NVARCHAR(10) NULL, -- HH:mm
    ClosingTime NVARCHAR(10) NULL, -- HH:mm
    Status      NVARCHAR(20) NOT NULL DEFAULT N'active',
    CreatedAt   DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt   DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT CK_Branch_Status CHECK (Status IN (N'active', N'locked'))
);
GO

CREATE UNIQUE INDEX UQ_Branch_BranchCode_NotNull
ON Branch(BranchCode)
WHERE BranchCode IS NOT NULL;
GO

-- ============================================================
--  3. Employee
--  Naming matches Java code: EmployeeID, RoleID, BranchID, FullName,
--  Email, Phone, PasswordHash, Status, CreatedAt
--  BranchID allows NULL because Owner/Admin can be system-wide.
-- ============================================================
CREATE TABLE Employee (
    EmployeeID   INT IDENTITY(1,1) PRIMARY KEY,
    RoleID       INT NOT NULL,
    BranchID     INT NULL,
    FullName     NVARCHAR(150) NOT NULL,
    Gender       NVARCHAR(10) NULL,
    DOB          DATE NULL,
    Address      NVARCHAR(300) NULL,
    Email        NVARCHAR(150) NULL,
    Phone        NVARCHAR(20) NULL,
    PasswordHash NVARCHAR(255) NULL,
    Status       NVARCHAR(20) NOT NULL DEFAULT N'active',
    CreatedAt    DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt    DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Employee_Role
        FOREIGN KEY (RoleID) REFERENCES Role(RoleID),
    CONSTRAINT FK_Employee_Branch
        FOREIGN KEY (BranchID) REFERENCES Branch(BranchID),
    CONSTRAINT CK_Employee_Status
        CHECK (Status IN (N'active', N'locked', N'inactive', N'on_leave'))
);
GO

-- SQL Server UNIQUE allows only one NULL, so use filtered unique indexes.
CREATE UNIQUE INDEX UQ_Employee_Email_NotNull
ON Employee(Email)
WHERE Email IS NOT NULL;
GO

CREATE UNIQUE INDEX UQ_Employee_Phone_NotNull
ON Employee(Phone)
WHERE Phone IS NOT NULL;
GO

-- ============================================================
--  4. EmployeeRole
--  Naming matches Java code: EmployeeRole, EmployeeID, RoleID
-- ============================================================
CREATE TABLE EmployeeRole (
    EmployeeRoleID INT IDENTITY(1,1) PRIMARY KEY,
    EmployeeID     INT NOT NULL,
    RoleID         INT NOT NULL,
    AssignedAt     DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_EmployeeRole_Employee
        FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID),
    CONSTRAINT FK_EmployeeRole_Role
        FOREIGN KEY (RoleID) REFERENCES Role(RoleID),
    CONSTRAINT UQ_EmployeeRole_Employee_Role
        UNIQUE (EmployeeID, RoleID)
);
GO

-- ============================================================
--  5. Customer
-- ============================================================
CREATE TABLE Customer (
    CustomerID   INT IDENTITY(1,1) PRIMARY KEY,
    FullName     NVARCHAR(150) NOT NULL,
    Gender       NVARCHAR(10) NULL,
    DOB          DATE NULL,
    Address      NVARCHAR(300) NULL,
    Email        NVARCHAR(150) NULL,
    Phone        NVARCHAR(20) NULL,
    CustomerType NVARCHAR(50) NULL,
    TotalSpent   DECIMAL(18,2) NOT NULL DEFAULT 0,
    CreatedAt    DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt    DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================================
--  6. CustomerPoint
-- ============================================================
CREATE TABLE CustomerPoint (
    CustomerPointID INT IDENTITY(1,1) PRIMARY KEY,
    CustomerID      INT NOT NULL,
    CurrentPoints   INT NOT NULL DEFAULT 0,
    LifetimePoints  INT NOT NULL DEFAULT 0,
    LevelName       NVARCHAR(50) NULL,
    UpdatedAt       DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_CustomerPoint_Customer
        FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);
GO

-- ============================================================
--  7. Voucher
-- ============================================================
CREATE TABLE Voucher (
    VoucherID     INT IDENTITY(1,1) PRIMARY KEY,
    VoucherCode   NVARCHAR(50) NOT NULL,
    VoucherName   NVARCHAR(150) NULL,
    DiscountType  NVARCHAR(20) NULL,
    DiscountValue DECIMAL(18,2) NULL,
    UsedQuantity  INT NOT NULL DEFAULT 0,
    StartDate     DATE NULL,
    EndDate       DATE NULL,
    Status        NVARCHAR(20) NOT NULL DEFAULT N'active',
    CreatedAt     DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Voucher_VoucherCode UNIQUE (VoucherCode),
    CONSTRAINT CK_Voucher_DiscountType CHECK (DiscountType IN (N'PERCENT', N'FIXED'))
);
GO

-- ============================================================
--  8. Supplier
-- ============================================================
CREATE TABLE Supplier (
    SupplierID INT IDENTITY(1,1) PRIMARY KEY,
    Name       NVARCHAR(150) NOT NULL,
    Phone      NVARCHAR(20) NULL,
    Address    NVARCHAR(300) NULL,
    Status     NVARCHAR(20) NOT NULL DEFAULT N'active',
    CreatedAt  DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt  DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================================
--  9. Warehouse
-- ============================================================
CREATE TABLE Warehouse (
    WarehouseID INT IDENTITY(1,1) PRIMARY KEY,
    Name        NVARCHAR(150) NOT NULL,
    BranchID    INT NOT NULL,
    Address     NVARCHAR(300) NULL,
    Status      NVARCHAR(20) NOT NULL DEFAULT N'active',
    CreatedAt   DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Warehouse_Branch
        FOREIGN KEY (BranchID) REFERENCES Branch(BranchID)
);
GO

-- ============================================================
--  10. Unit
-- ============================================================
CREATE TABLE Unit (
    UnitID      INT IDENTITY(1,1) PRIMARY KEY,
    Name        NVARCHAR(50) NOT NULL,
    Description NVARCHAR(255) NULL
);
GO

-- ============================================================
--  11. Category
-- ============================================================
CREATE TABLE Category (
    CategoryID       INT IDENTITY(1,1) PRIMARY KEY,
    Name             NVARCHAR(150) NOT NULL,
    Description      NVARCHAR(255) NULL,
    ParentCategoryID INT NULL,
    Status           NVARCHAR(20) NOT NULL DEFAULT N'active',
    CreatedAt        DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt        DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Category_ParentCategory
        FOREIGN KEY (ParentCategoryID) REFERENCES Category(CategoryID)
);
GO

-- ============================================================
--  12. Product
-- ============================================================
CREATE TABLE Product (
    ProductID    INT IDENTITY(1,1) PRIMARY KEY,
    Name         NVARCHAR(200) NOT NULL,
    Quantity     INT NOT NULL DEFAULT 0,
    CategoryID   INT NULL,
    UnitID       INT NULL,
    SellingPrice DECIMAL(18,2) NOT NULL DEFAULT 0,
    Status       NVARCHAR(20) NOT NULL DEFAULT N'active',
    CreatedAt    DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt    DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Product_Category
        FOREIGN KEY (CategoryID) REFERENCES Category(CategoryID),
    CONSTRAINT FK_Product_Unit
        FOREIGN KEY (UnitID) REFERENCES Unit(UnitID)
);
GO

-- ============================================================
--  13. Inventory
-- ============================================================
CREATE TABLE Inventory (
    InventoryID     INT IDENTITY(1,1) PRIMARY KEY,
    WarehouseID     INT NOT NULL,
    ProductID       INT NOT NULL,
    QuantityInStock INT NOT NULL DEFAULT 0,
    UpdatedAt       DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Inventory_Warehouse
        FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID),
    CONSTRAINT FK_Inventory_Product
        FOREIGN KEY (ProductID) REFERENCES Product(ProductID),
    CONSTRAINT UQ_Inventory_Warehouse_Product
        UNIQUE (WarehouseID, ProductID)
);
GO

-- ============================================================
--  14. Order
--  Use [Order] because ORDER is a SQL keyword.
--  Naming matches ProfileDao/UserManagementDao after correction:
--  OrderID, BranchID, EmployeeID, TotalAmount
-- ============================================================
CREATE TABLE [Order] (
    OrderID        INT IDENTITY(1,1) PRIMARY KEY,
    OrderCode      NVARCHAR(50) NULL,
    OrderType      NVARCHAR(30) NULL,
    CustomerID     INT NULL,
    BranchID       INT NULL,
    SupplierID     INT NULL,
    EmployeeID     INT NULL,
    VoucherID      INT NULL,
    WarehouseID    INT NULL,
    Subtotal       DECIMAL(18,2) NOT NULL DEFAULT 0,
    DiscountAmount DECIMAL(18,2) NOT NULL DEFAULT 0,
    TotalAmount    DECIMAL(18,2) NOT NULL DEFAULT 0,
    PaymentMethod  NVARCHAR(50) NULL,
    Status         NVARCHAR(30) NOT NULL DEFAULT N'PENDING',
    CreatedAt      DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Order_Customer
        FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    CONSTRAINT FK_Order_Branch
        FOREIGN KEY (BranchID) REFERENCES Branch(BranchID),
    CONSTRAINT FK_Order_Supplier
        FOREIGN KEY (SupplierID) REFERENCES Supplier(SupplierID),
    CONSTRAINT FK_Order_Employee
        FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID),
    CONSTRAINT FK_Order_Voucher
        FOREIGN KEY (VoucherID) REFERENCES Voucher(VoucherID),
    CONSTRAINT FK_Order_Warehouse
        FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID)
);
GO

-- ============================================================
--  15. OrderDetail
-- ============================================================
CREATE TABLE OrderDetail (
    OrderDetailID INT IDENTITY(1,1) PRIMARY KEY,
    OrderID       INT NOT NULL,
    ProductID     INT NOT NULL,
    Quantity      INT NOT NULL DEFAULT 1,
    UnitPrice     DECIMAL(18,2) NOT NULL DEFAULT 0,
    TotalPrice    DECIMAL(18,2) NOT NULL DEFAULT 0,

    CONSTRAINT FK_OrderDetail_Order
        FOREIGN KEY (OrderID) REFERENCES [Order](OrderID),
    CONSTRAINT FK_OrderDetail_Product
        FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);
GO

-- ============================================================
--  16. Payment
-- ============================================================
CREATE TABLE Payment (
    PaymentID       INT IDENTITY(1,1) PRIMARY KEY,
    OrderID         INT NOT NULL,
    PaymentMethod   NVARCHAR(50) NULL,
    PaymentAmount   DECIMAL(18,2) NOT NULL DEFAULT 0,
    PaymentDate     DATETIME NOT NULL DEFAULT GETDATE(),
    PaymentStatus   NVARCHAR(30) NULL,
    TransactionCode NVARCHAR(100) NULL,

    CONSTRAINT FK_Payment_Order
        FOREIGN KEY (OrderID) REFERENCES [Order](OrderID)
);
GO

-- ============================================================
--  17. PointTransaction
-- ============================================================
CREATE TABLE PointTransaction (
    PointTransactionID INT IDENTITY(1,1) PRIMARY KEY,
    CustomerPointID    INT NOT NULL,
    OrderID            INT NULL,
    BeforePoints       INT NOT NULL DEFAULT 0,
    AfterPoints        INT NOT NULL DEFAULT 0,
    Description        NVARCHAR(255) NULL,
    CreatedAt          DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_PointTransaction_CustomerPoint
        FOREIGN KEY (CustomerPointID) REFERENCES CustomerPoint(CustomerPointID),
    CONSTRAINT FK_PointTransaction_Order
        FOREIGN KEY (OrderID) REFERENCES [Order](OrderID)
);
GO

-- ============================================================
--  18. StockTransfer
-- ============================================================
CREATE TABLE StockTransfer (
    StockTransferID  INT IDENTITY(1,1) PRIMARY KEY,
    FromWarehouseID  INT NOT NULL,
    ToWarehouseID    INT NOT NULL,
    TransferCode     NVARCHAR(50) NULL,
    TransferDate     DATETIME NOT NULL DEFAULT GETDATE(),
    Status           NVARCHAR(30) NULL,
    Note             NVARCHAR(500) NULL,
    CreatedBy        INT NULL,

    CONSTRAINT FK_StockTransfer_FromWarehouse
        FOREIGN KEY (FromWarehouseID) REFERENCES Warehouse(WarehouseID),
    CONSTRAINT FK_StockTransfer_ToWarehouse
        FOREIGN KEY (ToWarehouseID) REFERENCES Warehouse(WarehouseID),
    CONSTRAINT FK_StockTransfer_CreatedBy
        FOREIGN KEY (CreatedBy) REFERENCES Employee(EmployeeID)
);
GO

-- ============================================================
--  19. StockTransferDetail
-- ============================================================
CREATE TABLE StockTransferDetail (
    StockTransferDetailID INT IDENTITY(1,1) PRIMARY KEY,
    StockTransferID       INT NOT NULL,
    ProductID             INT NOT NULL,
    Quantity              INT NOT NULL DEFAULT 0,

    CONSTRAINT FK_StockTransferDetail_StockTransfer
        FOREIGN KEY (StockTransferID) REFERENCES StockTransfer(StockTransferID),
    CONSTRAINT FK_StockTransferDetail_Product
        FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);
GO

-- ============================================================
--  20. StockTransaction
-- ============================================================
CREATE TABLE StockTransaction (
    StockTransactionID INT IDENTITY(1,1) PRIMARY KEY,
    WarehouseID        INT NOT NULL,
    ProductID          INT NOT NULL,
    ReferenceType      NVARCHAR(50) NULL, -- ORDER / TRANSFER / ADJUSTMENT
    ReferenceID        INT NULL,
    TransactionType    NVARCHAR(20) NULL, -- IN / OUT
    Quantity           INT NOT NULL DEFAULT 0,
    BeforeQuantity     INT NOT NULL DEFAULT 0,
    AfterQuantity      INT NOT NULL DEFAULT 0,
    Note               NVARCHAR(500) NULL,
    CreatedBy          INT NULL,
    CreatedAt          DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_StockTransaction_Warehouse
        FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID),
    CONSTRAINT FK_StockTransaction_Product
        FOREIGN KEY (ProductID) REFERENCES Product(ProductID),
    CONSTRAINT FK_StockTransaction_CreatedBy
        FOREIGN KEY (CreatedBy) REFERENCES Employee(EmployeeID)
);
GO

-- ============================================================
--  21. AuditLog
-- ============================================================
CREATE TABLE AuditLog (
    AuditLogID INT IDENTITY(1,1) PRIMARY KEY,
    EmployeeID INT NULL,
    ActionName NVARCHAR(100) NULL,
    TableName  NVARCHAR(100) NULL,
    RecordID   INT NULL,
    OldData    NVARCHAR(MAX) NULL,
    NewData    NVARCHAR(MAX) NULL,
    CreatedAt  DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_AuditLog_Employee
        FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID)
);
GO

-- ============================================================
--  Useful indexes for user management/profile screens
-- ============================================================
CREATE INDEX IX_Employee_BranchID ON Employee(BranchID);
GO

CREATE INDEX IX_Employee_Status ON Employee(Status);
GO

CREATE INDEX IX_EmployeeRole_EmployeeID ON EmployeeRole(EmployeeID);
GO

CREATE INDEX IX_EmployeeRole_RoleID ON EmployeeRole(RoleID);
GO

CREATE UNIQUE INDEX UQ_Order_OrderCode_NotNull
ON [Order](OrderCode)
WHERE OrderCode IS NOT NULL;
GO

CREATE INDEX IX_Order_EmployeeID ON [Order](EmployeeID);
GO

CREATE INDEX IX_Order_BranchID ON [Order](BranchID);
GO

-- ============================================================
--  SAMPLE DATA
--  Use this section to test login, user list, employee profile,
--  employee sales overview, branch filter, role filter, inventory,
--  order detail and payment screens.
-- ============================================================

-- Branch sample
INSERT INTO Branch (Name, BranchCode, Address, Phone, Email, OpeningTime, ClosingTime, Status)
VALUES
    (N'FInora Hà Nội - Cầu Giấy', N'BR-HN01', N'123 Xuân Thủy, Cầu Giấy, Hà Nội', N'02411112222', N'hanoi@finora.vn', N'08:00', N'22:00', N'active'),
    (N'FInora Hà Đông', N'BR-HN02', N'50 Trần Phú, Hà Đông, Hà Nội', N'02433334444', N'hadong@finora.vn', N'08:00', N'22:00', N'active'),
    (N'FInora Hồ Chí Minh', N'BR-HCM01', N'45 Nguyễn Huệ, Quận 1, TP.HCM', N'02855556666', N'hcm@finora.vn', N'08:00', N'22:00', N'active');
GO

-- Employee sample
-- PasswordHash below is SHA-256 of plain password: 123456
-- If your PasswordUtil uses a different algorithm, update these values.
DECLARE @DefaultPasswordHash NVARCHAR(255) = N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92';

DECLARE @AdminRoleID INT = (SELECT RoleID FROM Role WHERE Name = N'Admin');
DECLARE @OwnerRoleID INT = (SELECT RoleID FROM Role WHERE Name = N'Owner');
DECLARE @ManagerRoleID INT = (SELECT RoleID FROM Role WHERE Name = N'StoreManager');
DECLARE @SalesRoleID INT = (SELECT RoleID FROM Role WHERE Name = N'SalesStaff');
DECLARE @WarehouseRoleID INT = (SELECT RoleID FROM Role WHERE Name = N'WarehouseStaff');

DECLARE @BranchHN01 INT = (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN01');
DECLARE @BranchHN02 INT = (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN02');
DECLARE @BranchHCM01 INT = (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HCM01');

INSERT INTO Employee (RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status)
VALUES
    (@AdminRoleID, NULL, N'Nguyễn Quản Trị', N'Male', '1995-01-10', N'Hà Nội', N'admin@finora.vn', N'0900000001', @DefaultPasswordHash, N'active'),
    (@OwnerRoleID, NULL, N'Lê Chủ Sở Hữu', N'Male', '1990-03-15', N'Hà Nội', N'owner@finora.vn', N'0900000002', @DefaultPasswordHash, N'active'),
    (@ManagerRoleID, @BranchHN01, N'Trần Minh Manager', N'Male', '1993-05-20', N'Cầu Giấy, Hà Nội', N'manager.hn@finora.vn', N'0900000003', @DefaultPasswordHash, N'active'),
    (@ManagerRoleID, @BranchHN02, N'Phạm Hà Manager', N'Female', '1994-07-18', N'Hà Đông, Hà Nội', N'manager.hadong@finora.vn', N'0900000004', @DefaultPasswordHash, N'active'),
    (@ManagerRoleID, @BranchHCM01, N'Võ Lan Manager', N'Female', '1992-09-08', N'Quận 1, TP.HCM', N'manager.hcm@finora.vn', N'0900000005', @DefaultPasswordHash, N'active'),
    (@SalesRoleID, @BranchHN01, N'Nguyễn Kho Staff', N'Male', '1999-11-01', N'Cầu Giấy, Hà Nội', N'kho.staff@finora.vn', N'0900000006', @DefaultPasswordHash, N'active'),
    (@SalesRoleID, @BranchHN01, N'Đỗ Mai Sales', N'Female', '2000-12-12', N'Nam Từ Liêm, Hà Nội', N'mai.sales@finora.vn', N'0900000007', @DefaultPasswordHash, N'active'),
    (@WarehouseRoleID, @BranchHN01, N'Hoàng Tuấn Warehouse', N'Male', '1998-04-22', N'Cầu Giấy, Hà Nội', N'tuan.warehouse@finora.vn', N'0900000008', @DefaultPasswordHash, N'active'),
    (@SalesRoleID, @BranchHN02, N'Bùi Ngọc Sales', N'Female', '2001-06-09', N'Hà Đông, Hà Nội', N'ngoc.sales@finora.vn', N'0900000009', @DefaultPasswordHash, N'active'),
    (@SalesRoleID, @BranchHCM01, N'Phan Vy Sales', N'Female', '2000-08-30', N'Quận 3, TP.HCM', N'vy.sales@finora.vn', N'0900000010', @DefaultPasswordHash, N'active'),
    (@WarehouseRoleID, @BranchHCM01, N'Lâm Nam Warehouse', N'Male', '1997-02-14', N'Bình Thạnh, TP.HCM', N'nam.warehouse@finora.vn', N'0900000011', @DefaultPasswordHash, N'active');
GO

-- EmployeeRole sample
INSERT INTO EmployeeRole (EmployeeID, RoleID)
SELECT EmployeeID, RoleID
FROM Employee;
GO

-- Add one employee with multiple roles to test EmployeeRole display: role1, role2
INSERT INTO EmployeeRole (EmployeeID, RoleID)
SELECT e.EmployeeID, r.RoleID
FROM Employee e
CROSS JOIN Role r
WHERE e.Email = N'kho.staff@finora.vn'
  AND r.Name = N'WarehouseStaff'
  AND NOT EXISTS (
      SELECT 1
      FROM EmployeeRole er
      WHERE er.EmployeeID = e.EmployeeID
        AND er.RoleID = r.RoleID
  );
GO

-- Customer sample
INSERT INTO Customer (FullName, Gender, DOB, Address, Email, Phone, CustomerType, TotalSpent)
VALUES
    (N'Nguyễn Văn An', N'Male', '1996-02-11', N'Cầu Giấy, Hà Nội', N'an.nguyen@example.com', N'0911000001', N'VIP', 0),
    (N'Trần Thị Bình', N'Female', '1998-05-21', N'Hà Đông, Hà Nội', N'binh.tran@example.com', N'0911000002', N'MEMBER', 0),
    (N'Lê Hoàng Cường', N'Male', '1994-10-02', N'Quận 1, TP.HCM', N'cuong.le@example.com', N'0911000003', N'MEMBER', 0),
    (N'Phạm Thu Dung', N'Female', '2001-01-19', N'Nam Từ Liêm, Hà Nội', N'dung.pham@example.com', N'0911000004', N'NEW', 0),
    (N'Vũ Hải Long', N'Male', '1999-03-27', N'Bình Thạnh, TP.HCM', N'long.vu@example.com', N'0911000005', N'NEW', 0);
GO

INSERT INTO CustomerPoint (CustomerID, CurrentPoints, LifetimePoints, LevelName)
SELECT CustomerID,
       CASE CustomerType WHEN N'VIP' THEN 1200 WHEN N'MEMBER' THEN 350 ELSE 50 END,
       CASE CustomerType WHEN N'VIP' THEN 2500 WHEN N'MEMBER' THEN 800 ELSE 50 END,
       CASE CustomerType WHEN N'VIP' THEN N'Gold' WHEN N'MEMBER' THEN N'Silver' ELSE N'Bronze' END
FROM Customer;
GO

-- Voucher sample
INSERT INTO Voucher (VoucherCode, VoucherName, DiscountType, DiscountValue, UsedQuantity, StartDate, EndDate, Status)
VALUES
    (N'WELCOME10', N'Giảm 10% cho khách mới', N'PERCENT', 10, 0, '2026-01-01', '2026-12-31', N'active'),
    (N'FINORA50K', N'Giảm 50.000 VNĐ', N'FIXED', 50000, 0, '2026-01-01', '2026-12-31', N'active'),
    (N'VIP15', N'Giảm 15% cho khách VIP', N'PERCENT', 15, 0, '2026-01-01', '2026-12-31', N'active');
GO

-- Supplier sample
INSERT INTO Supplier (Name, Phone, Address, Status)
VALUES
    (N'Công ty TNHH Thời Trang Việt', N'0922000001', N'Long Biên, Hà Nội', N'active'),
    (N'Công ty Mỹ Phẩm Á Châu', N'0922000002', N'Quận 7, TP.HCM', N'active'),
    (N'Nhà phân phối Phụ Kiện Fino', N'0922000003', N'Hà Đông, Hà Nội', N'active');
GO

-- Warehouse sample
INSERT INTO Warehouse (Name, BranchID, Address, Status)
VALUES
    (N'Kho Cầu Giấy', (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN01'), N'123 Xuân Thủy, Cầu Giấy, Hà Nội', N'active'),
    (N'Kho Hà Đông', (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN02'), N'50 Trần Phú, Hà Đông, Hà Nội', N'active'),
    (N'Kho Hồ Chí Minh', (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HCM01'), N'45 Nguyễn Huệ, Quận 1, TP.HCM', N'active');
GO

-- Unit sample
INSERT INTO Unit (Name, Description)
VALUES
    (N'Cái', N'Đơn vị chiếc/cái'),
    (N'Hộp', N'Đơn vị hộp'),
    (N'Chai', N'Đơn vị chai'),
    (N'Bộ', N'Đơn vị bộ');
GO

-- Category sample
INSERT INTO Category (Name, Description, ParentCategoryID, Status)
VALUES
    (N'Thời trang', N'Sản phẩm thời trang', NULL, N'active'),
    (N'Mỹ phẩm', N'Sản phẩm chăm sóc và làm đẹp', NULL, N'active'),
    (N'Phụ kiện', N'Phụ kiện cá nhân', NULL, N'active');
GO

-- Product sample
INSERT INTO Product (Name, Quantity, CategoryID, UnitID, SellingPrice, Status)
VALUES
    (N'Áo thun FInora Basic', 120, (SELECT CategoryID FROM Category WHERE Name = N'Thời trang'), (SELECT UnitID FROM Unit WHERE Name = N'Cái'), 159000, N'active'),
    (N'Áo sơ mi FInora Classic', 80, (SELECT CategoryID FROM Category WHERE Name = N'Thời trang'), (SELECT UnitID FROM Unit WHERE Name = N'Cái'), 299000, N'active'),
    (N'Quần jeans FInora Slim', 65, (SELECT CategoryID FROM Category WHERE Name = N'Thời trang'), (SELECT UnitID FROM Unit WHERE Name = N'Cái'), 399000, N'active'),
    (N'Sữa rửa mặt FInora Clean', 200, (SELECT CategoryID FROM Category WHERE Name = N'Mỹ phẩm'), (SELECT UnitID FROM Unit WHERE Name = N'Chai'), 129000, N'active'),
    (N'Son dưỡng FInora LipCare', 150, (SELECT CategoryID FROM Category WHERE Name = N'Mỹ phẩm'), (SELECT UnitID FROM Unit WHERE Name = N'Cái'), 99000, N'active'),
    (N'Túi tote FInora Daily', 90, (SELECT CategoryID FROM Category WHERE Name = N'Phụ kiện'), (SELECT UnitID FROM Unit WHERE Name = N'Cái'), 189000, N'active'),
    (N'Ví da FInora Mini', 60, (SELECT CategoryID FROM Category WHERE Name = N'Phụ kiện'), (SELECT UnitID FROM Unit WHERE Name = N'Cái'), 249000, N'active'),
    (N'Combo chăm sóc cá nhân', 40, (SELECT CategoryID FROM Category WHERE Name = N'Mỹ phẩm'), (SELECT UnitID FROM Unit WHERE Name = N'Bộ'), 459000, N'active');
GO

-- Inventory sample: every warehouse has stock for every product
INSERT INTO Inventory (WarehouseID, ProductID, QuantityInStock)
SELECT w.WarehouseID,
       p.ProductID,
       CASE
           WHEN w.Name = N'Kho Cầu Giấy' THEN p.Quantity
           WHEN w.Name = N'Kho Hà Đông' THEN p.Quantity / 2
           ELSE p.Quantity / 3
       END AS QuantityInStock
FROM Warehouse w
CROSS JOIN Product p;
GO

-- Order sample for employee profile and overview
INSERT INTO [Order] (OrderCode, OrderType, CustomerID, BranchID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt)
VALUES
    (N'OD202606001', N'SALE', (SELECT CustomerID FROM Customer WHERE Email = N'an.nguyen@example.com'), (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN01'), (SELECT EmployeeID FROM Employee WHERE Email = N'kho.staff@finora.vn'), (SELECT VoucherID FROM Voucher WHERE VoucherCode = N'VIP15'), (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Cầu Giấy'), 816000, 122400, 693600, N'CASH', N'COMPLETED', '2026-06-01T09:15:00'),
    (N'OD202606002', N'SALE', (SELECT CustomerID FROM Customer WHERE Email = N'binh.tran@example.com'), (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN01'), (SELECT EmployeeID FROM Employee WHERE Email = N'mai.sales@finora.vn'), (SELECT VoucherID FROM Voucher WHERE VoucherCode = N'FINORA50K'), (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Cầu Giấy'), 587000, 50000, 537000, N'BANKING', N'COMPLETED', '2026-06-02T10:40:00'),
    (N'OD202606003', N'SALE', (SELECT CustomerID FROM Customer WHERE Email = N'dung.pham@example.com'), (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN01'), (SELECT EmployeeID FROM Employee WHERE Email = N'kho.staff@finora.vn'), (SELECT VoucherID FROM Voucher WHERE VoucherCode = N'WELCOME10'), (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Cầu Giấy'), 576000, 57600, 518400, N'CASH', N'COMPLETED', '2026-06-03T14:20:00'),
    (N'OD202606004', N'SALE', (SELECT CustomerID FROM Customer WHERE Email = N'binh.tran@example.com'), (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN02'), (SELECT EmployeeID FROM Employee WHERE Email = N'ngoc.sales@finora.vn'), NULL, (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Hà Đông'), 798000, 0, 798000, N'CARD', N'COMPLETED', '2026-06-04T16:30:00'),
    (N'OD202606005', N'SALE', (SELECT CustomerID FROM Customer WHERE Email = N'cuong.le@example.com'), (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HCM01'), (SELECT EmployeeID FROM Employee WHERE Email = N'vy.sales@finora.vn'), (SELECT VoucherID FROM Voucher WHERE VoucherCode = N'FINORA50K'), (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Hồ Chí Minh'), 957000, 50000, 907000, N'BANKING', N'COMPLETED', '2026-06-05T11:05:00'),
    (N'OD202606006', N'SALE', (SELECT CustomerID FROM Customer WHERE Email = N'long.vu@example.com'), (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HCM01'), (SELECT EmployeeID FROM Employee WHERE Email = N'vy.sales@finora.vn'), NULL, (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Hồ Chí Minh'), 348000, 0, 348000, N'CASH', N'COMPLETED', '2026-06-06T18:45:00'),
    (N'OD202606007', N'SALE', (SELECT CustomerID FROM Customer WHERE Email = N'an.nguyen@example.com'), (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN01'), (SELECT EmployeeID FROM Employee WHERE Email = N'mai.sales@finora.vn'), NULL, (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Cầu Giấy'), 456000, 0, 456000, N'CASH', N'PENDING', '2026-06-07T12:00:00'),
    (N'OD202606008', N'SALE', (SELECT CustomerID FROM Customer WHERE Email = N'dung.pham@example.com'), (SELECT BranchID FROM Branch WHERE BranchCode = N'BR-HN02'), (SELECT EmployeeID FROM Employee WHERE Email = N'ngoc.sales@finora.vn'), (SELECT VoucherID FROM Voucher WHERE VoucherCode = N'WELCOME10'), (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Hà Đông'), 647000, 64700, 582300, N'BANKING', N'COMPLETED', '2026-06-08T15:10:00');
GO

-- OrderDetail sample
INSERT INTO OrderDetail (OrderID, ProductID, Quantity, UnitPrice, TotalPrice)
VALUES
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606001'), (SELECT ProductID FROM Product WHERE Name = N'Áo thun FInora Basic'), 2, 159000, 318000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606001'), (SELECT ProductID FROM Product WHERE Name = N'Quần jeans FInora Slim'), 1, 399000, 399000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606001'), (SELECT ProductID FROM Product WHERE Name = N'Son dưỡng FInora LipCare'), 1, 99000, 99000),

    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606002'), (SELECT ProductID FROM Product WHERE Name = N'Áo sơ mi FInora Classic'), 1, 299000, 299000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606002'), (SELECT ProductID FROM Product WHERE Name = N'Túi tote FInora Daily'), 1, 189000, 189000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606002'), (SELECT ProductID FROM Product WHERE Name = N'Son dưỡng FInora LipCare'), 1, 99000, 99000),

    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606003'), (SELECT ProductID FROM Product WHERE Name = N'Sữa rửa mặt FInora Clean'), 3, 129000, 387000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606003'), (SELECT ProductID FROM Product WHERE Name = N'Túi tote FInora Daily'), 1, 189000, 189000),

    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606004'), (SELECT ProductID FROM Product WHERE Name = N'Quần jeans FInora Slim'), 2, 399000, 798000),

    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606005'), (SELECT ProductID FROM Product WHERE Name = N'Combo chăm sóc cá nhân'), 1, 459000, 459000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606005'), (SELECT ProductID FROM Product WHERE Name = N'Ví da FInora Mini'), 2, 249000, 498000),

    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606006'), (SELECT ProductID FROM Product WHERE Name = N'Áo thun FInora Basic'), 1, 159000, 159000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606006'), (SELECT ProductID FROM Product WHERE Name = N'Túi tote FInora Daily'), 1, 189000, 189000),

    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606007'), (SELECT ProductID FROM Product WHERE Name = N'Sữa rửa mặt FInora Clean'), 2, 129000, 258000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606007'), (SELECT ProductID FROM Product WHERE Name = N'Son dưỡng FInora LipCare'), 2, 99000, 198000),

    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606008'), (SELECT ProductID FROM Product WHERE Name = N'Áo sơ mi FInora Classic'), 1, 299000, 299000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606008'), (SELECT ProductID FROM Product WHERE Name = N'Ví da FInora Mini'), 1, 249000, 249000),
    ((SELECT OrderID FROM [Order] WHERE OrderCode = N'OD202606008'), (SELECT ProductID FROM Product WHERE Name = N'Son dưỡng FInora LipCare'), 1, 99000, 99000);
GO

-- Payment sample
INSERT INTO Payment (OrderID, PaymentMethod, PaymentAmount, PaymentStatus, TransactionCode)
SELECT OrderID, PaymentMethod, TotalAmount, CASE WHEN Status = N'COMPLETED' THEN N'PAID' ELSE N'UNPAID' END, N'TXN-' + OrderCode
FROM [Order];
GO

-- Update customer total spent from completed orders
UPDATE c
SET TotalSpent = x.TotalSpent
FROM Customer c
JOIN (
    SELECT CustomerID, SUM(TotalAmount) AS TotalSpent
    FROM [Order]
    WHERE Status = N'COMPLETED'
    GROUP BY CustomerID
) x ON c.CustomerID = x.CustomerID;
GO

-- Point transaction sample
INSERT INTO PointTransaction (CustomerPointID, OrderID, BeforePoints, AfterPoints, Description)
SELECT cp.CustomerPointID,
       o.OrderID,
       cp.CurrentPoints,
       cp.CurrentPoints + CAST(o.TotalAmount / 10000 AS INT),
       N'Cộng điểm từ đơn hàng ' + o.OrderCode
FROM [Order] o
JOIN CustomerPoint cp ON cp.CustomerID = o.CustomerID
WHERE o.Status = N'COMPLETED';
GO

-- Stock transfer sample
INSERT INTO StockTransfer (FromWarehouseID, ToWarehouseID, TransferCode, Status, Note, CreatedBy)
VALUES
    ((SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Cầu Giấy'),
     (SELECT WarehouseID FROM Warehouse WHERE Name = N'Kho Hà Đông'),
     N'TF202606001', N'COMPLETED', N'Chuyển hàng bổ sung cho chi nhánh Hà Đông',
     (SELECT EmployeeID FROM Employee WHERE Email = N'tuan.warehouse@finora.vn'));
GO

INSERT INTO StockTransferDetail (StockTransferID, ProductID, Quantity)
VALUES
    ((SELECT StockTransferID FROM StockTransfer WHERE TransferCode = N'TF202606001'), (SELECT ProductID FROM Product WHERE Name = N'Áo thun FInora Basic'), 20),
    ((SELECT StockTransferID FROM StockTransfer WHERE TransferCode = N'TF202606001'), (SELECT ProductID FROM Product WHERE Name = N'Son dưỡng FInora LipCare'), 30);
GO

-- Stock transaction sample
INSERT INTO StockTransaction (WarehouseID, ProductID, ReferenceType, ReferenceID, TransactionType, Quantity, BeforeQuantity, AfterQuantity, Note, CreatedBy)
SELECT st.FromWarehouseID, std.ProductID, N'TRANSFER', st.StockTransferID, N'OUT', std.Quantity, 100, 100 - std.Quantity,
       N'Xuất kho chuyển hàng', st.CreatedBy
FROM StockTransfer st
JOIN StockTransferDetail std ON st.StockTransferID = std.StockTransferID;
GO

INSERT INTO StockTransaction (WarehouseID, ProductID, ReferenceType, ReferenceID, TransactionType, Quantity, BeforeQuantity, AfterQuantity, Note, CreatedBy)
SELECT st.ToWarehouseID, std.ProductID, N'TRANSFER', st.StockTransferID, N'IN', std.Quantity, 50, 50 + std.Quantity,
       N'Nhập kho từ chuyển hàng', st.CreatedBy
FROM StockTransfer st
JOIN StockTransferDetail std ON st.StockTransferID = std.StockTransferID;
GO

-- Audit log sample
INSERT INTO AuditLog (EmployeeID, ActionName, TableName, RecordID, OldData, NewData)
VALUES
    ((SELECT EmployeeID FROM Employee WHERE Email = N'admin@finora.vn'), N'CREATE_SAMPLE_DATABASE', N'Database', 1, NULL, N'Created sample data for DBFinoraV2'),
    ((SELECT EmployeeID FROM Employee WHERE Email = N'owner@finora.vn'), N'VIEW_EMPLOYEE_OVERVIEW', N'Employee', NULL, NULL, N'Owner viewed employee overview');
GO

-- ============================================================
--  Quick test queries
-- ============================================================
SELECT EmployeeID, FullName, Email, Phone, Status FROM Employee ORDER BY EmployeeID;
SELECT e.EmployeeID, e.FullName, b.Name AS BranchName, er.RoleID, r.Name AS RoleName
FROM Employee e
LEFT JOIN Branch b ON e.BranchID = b.BranchID
LEFT JOIN EmployeeRole er ON e.EmployeeID = er.EmployeeID
LEFT JOIN Role r ON er.RoleID = r.RoleID
ORDER BY e.EmployeeID, er.RoleID;
SELECT e.EmployeeID, e.FullName, COUNT(o.OrderID) AS TotalOrders, COALESCE(SUM(o.TotalAmount), 0) AS TotalRevenue
FROM Employee e
LEFT JOIN [Order] o ON e.EmployeeID = o.EmployeeID
GROUP BY e.EmployeeID, e.FullName
ORDER BY TotalRevenue DESC;
GO

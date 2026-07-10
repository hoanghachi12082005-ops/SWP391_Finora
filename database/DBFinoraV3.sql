CREATE DATABASE DBFinoraV3;
GO
USE DBFinoraV3;
GO

-- 1. Role
CREATE TABLE [Role] (
    RoleID      INT           IDENTITY(1,1) PRIMARY KEY,
    Name        NVARCHAR(100) NOT NULL,
    Description NVARCHAR(255),
    CreatedAt   DATETIME      DEFAULT GETDATE(),
    UpdatedAt   DATETIME      DEFAULT GETDATE()
);
GO

-- 2. Branch
CREATE TABLE Branch (
    BranchID     INT             IDENTITY(1,1) PRIMARY KEY,
    Name         NVARCHAR(150)   NOT NULL,
    BranchCode   NVARCHAR(50)    UNIQUE,
    Address      NVARCHAR(300),
    Phone        NVARCHAR(20)    UNIQUE,
    Email        NVARCHAR(150)   UNIQUE,
    OpeningTime  NVARCHAR(10),
    ClosingTime  NVARCHAR(10),
    Status       NVARCHAR(20)    DEFAULT 'active',
    CreatedAt    DATETIME        DEFAULT GETDATE(),
    UpdatedAt    DATETIME        DEFAULT GETDATE()
);
GO

-- 3. Employee
CREATE TABLE Employee (
    EmployeeID       INT           IDENTITY(1,1) PRIMARY KEY,
    RoleID           INT           NOT NULL FOREIGN KEY REFERENCES [Role](RoleID),
    BranchID         INT           FOREIGN KEY REFERENCES Branch(BranchID),
    FullName         NVARCHAR(150) NOT NULL,
    Gender           NVARCHAR(10),
    DOB              DATE,
    Address          NVARCHAR(300),
    Email            NVARCHAR(150) UNIQUE,
    Phone            NVARCHAR(20)  UNIQUE,
    PasswordHash     VARCHAR(100),
    Status           NVARCHAR(20)  DEFAULT 'active',
    CreatedAt        DATETIME      DEFAULT GETDATE(),
    UpdatedAt        DATETIME      DEFAULT GETDATE()
);
GO

-- 4. EmployeeRole
CREATE TABLE EmployeeRole (
    EmployeeRoleID INT      IDENTITY(1,1) PRIMARY KEY,
    EmployeeID     INT      NOT NULL FOREIGN KEY REFERENCES Employee(EmployeeID),
    RoleID         INT      NOT NULL FOREIGN KEY REFERENCES [Role](RoleID),
    AssignedAt     DATETIME DEFAULT GETDATE()
);
GO

-- 5. Customer
CREATE TABLE Customer (
    CustomerID   INT           IDENTITY(1,1) PRIMARY KEY,
    FullName     NVARCHAR(150) NOT NULL,
    Gender       NVARCHAR(10),
    DOB          DATE,
    Address      NVARCHAR(300),
    Email        NVARCHAR(150),
    Phone        NVARCHAR(20)  UNIQUE,
    CustomerType NVARCHAR(50),
    TotalSpent   DECIMAL(18,2) DEFAULT 0,
    CreatedAt    DATETIME      DEFAULT GETDATE(),
    UpdatedAt    DATETIME      DEFAULT GETDATE()
);
GO

-- 6. CustomerPoint
CREATE TABLE CustomerPoint (
    CustomerPointID INT          IDENTITY(1,1) PRIMARY KEY,
    CustomerID      INT          NOT NULL UNIQUE FOREIGN KEY REFERENCES Customer(CustomerID),
    CurrentPoints   INT          DEFAULT 0,
    LifetimePoints  INT          DEFAULT 0,
    LevelName       NVARCHAR(50),
    UpdatedAt       DATETIME     DEFAULT GETDATE()
);
GO

-- 7. Voucher
CREATE TABLE Voucher (
    VoucherID     INT            IDENTITY(1,1) PRIMARY KEY,
    VoucherCode   NVARCHAR(50)   UNIQUE NOT NULL,
    VoucherName   NVARCHAR(150),
    DiscountType  NVARCHAR(20)   CHECK (DiscountType IN ('PERCENT','FIXED')),
    DiscountValue DECIMAL(18,2),
    UsedQuantity  INT            DEFAULT 0,
    StartDate     DATE,
    EndDate       DATE,
    Status        NVARCHAR(20)   DEFAULT 'active',
    CreatedAt     DATETIME       DEFAULT GETDATE()
);
GO

-- 8. Supplier
CREATE TABLE Supplier (
    SupplierID INT           IDENTITY(1,1) PRIMARY KEY,
    Name       NVARCHAR(150) NOT NULL,
    Phone      NVARCHAR(20),
    Address    NVARCHAR(300),
    Status     NVARCHAR(20)  DEFAULT 'active',
    CreatedAt  DATETIME      DEFAULT GETDATE(),
    UpdatedAt  DATETIME      DEFAULT GETDATE()
);
GO

-- 9. Warehouse
CREATE TABLE Warehouse (
    WarehouseID INT           IDENTITY(1,1) PRIMARY KEY,
    Name        NVARCHAR(150) NOT NULL,
    BranchID    INT           NOT NULL UNIQUE FOREIGN KEY REFERENCES Branch(BranchID),
    Address     NVARCHAR(300),
    Status      NVARCHAR(20)  DEFAULT 'active',
    CreatedAt   DATETIME      DEFAULT GETDATE()
);
GO

-- 10. Unit
CREATE TABLE Unit (
    UnitID      INT          IDENTITY(1,1) PRIMARY KEY,
    Name        NVARCHAR(50) NOT NULL,
    Description NVARCHAR(255)
);
GO

-- 11. Category
CREATE TABLE Category (
    CategoryID       INT           IDENTITY(1,1) PRIMARY KEY,
    Name             NVARCHAR(150) NOT NULL,
    Description      NVARCHAR(255),
    ParentCategoryID INT           FOREIGN KEY REFERENCES Category(CategoryID),
    Status           NVARCHAR(20)  DEFAULT 'active',
    CreatedAt        DATETIME      DEFAULT GETDATE(),
    UpdatedAt        DATETIME      DEFAULT GETDATE()
);
GO

-- 12. Product
CREATE TABLE Product (
    ProductID    INT            IDENTITY(1,1) PRIMARY KEY,
    Name         NVARCHAR(200)  NOT NULL,
    Quantity     INT            DEFAULT 0,
    CategoryID   INT            FOREIGN KEY REFERENCES Category(CategoryID),
    UnitID       INT            FOREIGN KEY REFERENCES Unit(UnitID),
    SellingPrice DECIMAL(18,2)  DEFAULT 0,
    Status       NVARCHAR(20)   DEFAULT 'active',
    CreatedAt    DATETIME       DEFAULT GETDATE(),
    UpdatedAt    DATETIME       DEFAULT GETDATE()
);
GO

-- 12.5. SupplierProduct
CREATE TABLE SupplierProduct (
    SupplierID  INT NOT NULL FOREIGN KEY REFERENCES Supplier(SupplierID),
    ProductID   INT NOT NULL FOREIGN KEY REFERENCES Product(ProductID),
    ImportPrice DECIMAL(18,2) DEFAULT 0,
    PRIMARY KEY (SupplierID, ProductID)
);
GO

-- 13. Inventory
CREATE TABLE Inventory (
    InventoryID     INT      IDENTITY(1,1) PRIMARY KEY,
    WarehouseID     INT      NOT NULL FOREIGN KEY REFERENCES Warehouse(WarehouseID),
    ProductID       INT      NOT NULL FOREIGN KEY REFERENCES Product(ProductID),
    QuantityInStock INT      DEFAULT 0,
    UpdatedAt       DATETIME DEFAULT GETDATE(),
    UNIQUE (WarehouseID, ProductID)
);
GO

-- 14. Order
CREATE TABLE [Order] (
    OrderID        INT            IDENTITY(1,1) PRIMARY KEY,
    OrderCode      NVARCHAR(50)   UNIQUE,
    OrderType      NVARCHAR(30),
    CustomerID     INT            FOREIGN KEY REFERENCES Customer(CustomerID),
    BranchID       INT            FOREIGN KEY REFERENCES Branch(BranchID),
    SupplierID     INT            FOREIGN KEY REFERENCES Supplier(SupplierID),
    EmployeeID     INT            FOREIGN KEY REFERENCES Employee(EmployeeID),
    VoucherID      INT            FOREIGN KEY REFERENCES Voucher(VoucherID),
    WarehouseID    INT            FOREIGN KEY REFERENCES Warehouse(WarehouseID),
    Subtotal       DECIMAL(18,2)  DEFAULT 0,
    DiscountAmount DECIMAL(18,2)  DEFAULT 0,
    TotalAmount    DECIMAL(18,2)  DEFAULT 0,
    PaymentMethod  NVARCHAR(50),
    Status         NVARCHAR(30)   DEFAULT 'PENDING',
    CreatedAt      DATETIME       DEFAULT GETDATE()
);
GO

-- 15. OrderDetail
CREATE TABLE OrderDetail (
    OrderDetailID INT            IDENTITY(1,1) PRIMARY KEY,
    OrderID       INT            NOT NULL FOREIGN KEY REFERENCES [Order](OrderID),
    ProductID     INT            NOT NULL FOREIGN KEY REFERENCES Product(ProductID),
    Quantity      INT            DEFAULT 1,
    UnitPrice     DECIMAL(18,2)  DEFAULT 0,
    TotalPrice    DECIMAL(18,2)  DEFAULT 0
);
GO

-- 16. Payment
CREATE TABLE Payment (
    PaymentID       INT            IDENTITY(1,1) PRIMARY KEY,
    OrderID         INT            FOREIGN KEY REFERENCES [Order](OrderID),
    PaymentMethod   NVARCHAR(50),
    PaymentAmount   DECIMAL(18,2)  DEFAULT 0,
    PaymentDate     DATETIME       DEFAULT GETDATE(),
    PaymentStatus   NVARCHAR(30)   DEFAULT 'PENDING',
    TransactionCode NVARCHAR(100),
    PaymentType     NVARCHAR(20)   NOT NULL CHECK (PaymentType IN ('INCOME', 'EXPENSE')),
    Description     NVARCHAR(500),
    EmployeeID      INT            FOREIGN KEY REFERENCES Employee(EmployeeID),
    BranchID        INT            FOREIGN KEY REFERENCES Branch(BranchID)
);
GO

-- 17. PointTransaction
CREATE TABLE PointTransaction (
    PointTransactionID INT           IDENTITY(1,1) PRIMARY KEY,
    CustomerPointID    INT           NOT NULL FOREIGN KEY REFERENCES CustomerPoint(CustomerPointID),
    OrderID            INT           FOREIGN KEY REFERENCES [Order](OrderID),
    BeforePoints       INT           DEFAULT 0,
    AfterPoints        INT           DEFAULT 0,
    Description        NVARCHAR(255),
    CreatedAt          DATETIME      DEFAULT GETDATE()
);
GO

-- 18. StockTransfer
CREATE TABLE StockTransfer (
    StockTransferID INT           IDENTITY(1,1) PRIMARY KEY,
    FromWarehouseID INT           NOT NULL FOREIGN KEY REFERENCES Warehouse(WarehouseID),
    ToWarehouseID   INT           NOT NULL FOREIGN KEY REFERENCES Warehouse(WarehouseID),
    TransferCode    NVARCHAR(50),
    TransferDate    DATETIME      DEFAULT GETDATE(),
    Status          NVARCHAR(30),
    Note            NVARCHAR(500),
    CreatedBy       INT           FOREIGN KEY REFERENCES Employee(EmployeeID)
);
GO

-- 19. StockTransferDetail
CREATE TABLE StockTransferDetail (
    StockTransferDetailID INT IDENTITY(1,1) PRIMARY KEY,
    StockTransferID       INT NOT NULL FOREIGN KEY REFERENCES StockTransfer(StockTransferID),
    ProductID             INT NOT NULL FOREIGN KEY REFERENCES Product(ProductID),
    Quantity              INT DEFAULT 0
);
GO

-- 20. StockTransaction
CREATE TABLE StockTransaction (
    StockTransactionID INT           IDENTITY(1,1) PRIMARY KEY,
    WarehouseID        INT           NOT NULL FOREIGN KEY REFERENCES Warehouse(WarehouseID),
    ProductID          INT           NOT NULL FOREIGN KEY REFERENCES Product(ProductID),
    ReferenceType      NVARCHAR(50),
    ReferenceID        INT,
    TransactionType    NVARCHAR(20),
    Quantity           INT           DEFAULT 0,
    BeforeQuantity     INT           DEFAULT 0,
    AfterQuantity      INT           DEFAULT 0,
    Note               NVARCHAR(500),
    CreatedBy          INT           FOREIGN KEY REFERENCES Employee(EmployeeID),
    CreatedAt          DATETIME      DEFAULT GETDATE()
);
GO

-- 21. AuditLog
CREATE TABLE AuditLog (
    AuditLogID  INT           IDENTITY(1,1) PRIMARY KEY,
    EmployeeID  INT           FOREIGN KEY REFERENCES Employee(EmployeeID),
    ActionName  NVARCHAR(100),
    TableName   NVARCHAR(100),
    RecordID    INT,
    OldData     NVARCHAR(MAX),
    NewData     NVARCHAR(MAX),
    CreatedAt   DATETIME      DEFAULT GETDATE()
);
GO

-- ============================================================
--  SEED DATA INSERTS
-- ============================================================

-- Data for Role
SET IDENTITY_INSERT [Role] ON;
INSERT INTO [Role] (RoleID, Name, Description, CreatedAt, UpdatedAt) VALUES (1, 'Admin', 'System administrator', '2026-06-11 03:22:58.543', '2026-06-11 03:22:58.543');
INSERT INTO [Role] (RoleID, Name, Description, CreatedAt, UpdatedAt) VALUES (2, 'Owner', 'Business owner', '2026-06-11 03:22:58.543', '2026-06-11 03:22:58.543');
INSERT INTO [Role] (RoleID, Name, Description, CreatedAt, UpdatedAt) VALUES (3, 'StoreManager', 'Branch/store manager', '2026-06-11 03:22:58.543', '2026-06-11 03:22:58.543');
INSERT INTO [Role] (RoleID, Name, Description, CreatedAt, UpdatedAt) VALUES (4, 'SalesStaff', 'Sales employee', '2026-06-11 03:22:58.543', '2026-06-11 03:22:58.543');
INSERT INTO [Role] (RoleID, Name, Description, CreatedAt, UpdatedAt) VALUES (5, 'WarehouseStaff', 'Warehouse employee', '2026-06-11 03:22:58.543', '2026-06-11 03:22:58.543');
SET IDENTITY_INSERT [Role] OFF;
GO

-- Data for Branch
SET IDENTITY_INSERT Branch ON;
INSERT INTO Branch (BranchID, Name, BranchCode, Address, Phone, Email, OpeningTime, ClosingTime, Status, CreatedAt, UpdatedAt) VALUES (1, 'Finora Hà Nội - Cầu Giấy', 'BR-HN01', '233 Cầu Giấy, Hà Nội', '02411112222', 'hanoi@finora.vn', '08:00', '22:00', 'active', '2026-06-11 03:22:58.690', '2026-06-11 03:22:58.690');
INSERT INTO Branch (BranchID, Name, BranchCode, Address, Phone, Email, OpeningTime, ClosingTime, Status, CreatedAt, UpdatedAt) VALUES (2, 'Finora Hà Đông', 'BR-HN02', '30 Hà Đông, Hà Nội', '02433334444', 'hadong@finora.vn', '08:00', '22:00', 'active', '2026-06-11 03:22:58.690', '2026-06-11 03:22:58.690');
INSERT INTO Branch (BranchID, Name, BranchCode, Address, Phone, Email, OpeningTime, ClosingTime, Status, CreatedAt, UpdatedAt) VALUES (3, 'Finora TP Hồ Chí Minh', 'BR-HCM01', '40 Quận 1, TPHCM', '02855556666', 'hcm@finora.vn', '08:00', '22:00', 'active', '2026-06-11 03:22:58.690', '2026-06-11 03:22:58.690');
SET IDENTITY_INSERT Branch OFF;
GO

-- Data for Employee
SET IDENTITY_INSERT Employee ON;
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (1, 1, NULL, 'Nguyễn Văn A', 'Male', '1995-01-10', 'Hà Nội', 'admin@finora.vn', '0900000001', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-07-01 01:59:25.233');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (2, 2, NULL, 'La Văn Cầu', 'Male', '1990-03-15', 'Hà Nội', 'owner@finora.vn', '0900000002', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-07-01 01:47:53.740');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (3, 3, 1, 'Trần Quang Minh', 'Male', '1993-05-20', 'Cầu Giấy', 'manager.hn@finora.vn', '0900000003', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (4, 3, 2, 'Phạm Ánh Chi', 'Female', '1994-07-18', 'Hà Nội', 'manager.hadong@finora.vn', '0900000004', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (5, 3, 3, 'Vũ Lan Anh', 'Female', '1992-09-08', 'Quận 1, TPHCM', 'manager.hcm@finora.vn', '0900000005', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (6, 4, 1, 'Nguyễn Văn Khôi', 'Male', '1999-11-01', 'Hà Nội', 'kho.staff@finora.vn', '0900000006', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (7, 4, 1, 'Nguyễn Anh Mai', 'Female', '2000-12-12', 'Nam Từ Liêm', 'mai.sales@finora.vn', '0900000007', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (8, 5, 1, 'Hoàng Tuấn Anh', 'Male', '1998-04-22', 'Cầu Giấy', 'tuan.warehouse@finora.vn', '0900000008', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (9, 4, 2, 'Bùi Minh Ngọc', 'Female', '2001-06-09', 'Hà Nội', 'ngoc.sales@finora.vn', '0900000009', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (10, 4, 3, 'Phan Tường Vy', 'Female', '2000-08-30', 'Quận 3, TPHCM', 'vy.sales@finora.vn', '0900000010', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (11, 5, 3, 'Lâm Anh Vũ', 'Male', '1997-02-14', 'Nam Định', 'nam.warehouse@finora.vn', '0900000011', '$2a$12$AxaGhSfDQmoi644FUJk0UuukyWoY2q/aSy1Vd7u8aSJxaV19Riswm', 'active', '2026-06-11 03:22:58.733', '2026-06-11 03:22:58.733');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (13, 2, 1, 'Hoàng Hà Chi', 'Female', '2005-08-12', 'Tuyên Quang', 'hoanghachi12082005@gmail.com', '0862120805', '$2a$12$iPNCliy8L19C8.n7toEUsemLF951CoDhULddQ9i./CwoXSJW/pMf2', 'active', '2026-06-16 01:51:10.747', '2026-06-25 02:26:49.533');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (14, 2, 1, 'Trần Văn B', 'Male', NULL, NULL, 'vanba@gmail.com', '03333456789', '$2a$12$8c.n7QuUMqZpzXgpbYH03eiKVFmFAvUuX2NTdSg8Q6Ot8caLWLtO2', 'active', '2026-06-16 02:04:23.533', '2026-06-16 02:04:23.533');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (17, 2, 1, 'Nguyễn Văn Kiên', 'Male', NULL, NULL, 'nguyenvank@gmail.com', '0456123789', '$2a$12$6U.kIngAluwHH6l0CXQnf.Gbw8rQm5up.tuVXoLFckLqvgYlO35wy', 'active', '2026-06-16 03:27:21.050', '2026-06-16 03:27:21.050');
INSERT INTO Employee (EmployeeID, RoleID, BranchID, FullName, Gender, DOB, Address, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) VALUES (18, 2, 1, 'Nguyễn Trung Quyết', 'Male', NULL, NULL, 'hoiaheh@gmail.com', '0456123788', '$2a$12$Ja8UZtejO16I7glKWV33muT8TEzgT3bdvvyiIIjK6kYW09HedtbPu', 'active', '2026-06-16 03:28:54.520', '2026-06-16 03:28:54.520');
SET IDENTITY_INSERT Employee OFF;
GO

-- Data for EmployeeRole
SET IDENTITY_INSERT EmployeeRole ON;
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (1, 1, 1, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (2, 2, 2, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (3, 3, 3, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (4, 4, 3, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (5, 5, 3, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (6, 6, 4, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (7, 7, 4, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (8, 8, 5, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (9, 9, 4, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (10, 10, 4, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (11, 11, 5, '2026-06-11 03:22:58.750');
INSERT INTO EmployeeRole (EmployeeRoleID, EmployeeID, RoleID, AssignedAt) VALUES (12, 6, 5, '2026-06-11 03:22:58.767');
SET IDENTITY_INSERT EmployeeRole OFF;
GO

-- Data for Customer
SET IDENTITY_INSERT Customer ON;
INSERT INTO Customer (CustomerID, FullName, Gender, DOB, Address, Email, Phone, CustomerType, TotalSpent, CreatedAt, UpdatedAt) VALUES (1, 'Trần Văn An', 'Male', '1996-02-11', 'Cầu Giấy, Hà Nội', 'an.nguyen@example.com', '0911000001', 'VIP', 693600.00, '2026-06-11 03:22:58.773', '2026-06-11 03:22:58.773');
INSERT INTO Customer (CustomerID, FullName, Gender, DOB, Address, Email, Phone, CustomerType, TotalSpent, CreatedAt, UpdatedAt) VALUES (2, 'Nguyễn Thị Bình', 'Female', '1998-05-21', 'Hà Đông, Hà Nội', 'binh.tran@example.com', '0911000002', 'MEMBER', 1335000.00, '2026-06-11 03:22:58.773', '2026-06-11 03:22:58.773');
INSERT INTO Customer (CustomerID, FullName, Gender, DOB, Address, Email, Phone, CustomerType, TotalSpent, CreatedAt, UpdatedAt) VALUES (3, 'La Văn Cường', 'Male', '1994-10-02', 'Quận 1, TPHCM', 'cuong.le@example.com', '0911000003', 'MEMBER', 907000.00, '2026-06-11 03:22:58.773', '2026-06-11 03:22:58.773');
INSERT INTO Customer (CustomerID, FullName, Gender, DOB, Address, Email, Phone, CustomerType, TotalSpent, CreatedAt, UpdatedAt) VALUES (4, 'Phạm Thuỳ Dung', 'Female', '2001-01-19', 'Quận 3, TPHCM', 'dung.pham@example.com', '0911000004', 'NEW', 1100700.00, '2026-06-11 03:22:58.773', '2026-06-11 03:22:58.773');
INSERT INTO Customer (CustomerID, FullName, Gender, DOB, Address, Email, Phone, CustomerType, TotalSpent, CreatedAt, UpdatedAt) VALUES (5, 'Vũ Hạ Long', 'Male', '1999-03-27', 'Bình Chánh, TPHCM', 'long.vu@example.com', '0911000005', 'NEW', 348000.00, '2026-06-11 03:22:58.773', '2026-06-11 03:22:58.773');
SET IDENTITY_INSERT Customer OFF;
GO

-- Data for CustomerPoint
SET IDENTITY_INSERT CustomerPoint ON;
INSERT INTO CustomerPoint (CustomerPointID, CustomerID, CurrentPoints, LifetimePoints, LevelName, UpdatedAt) VALUES (1, 1, 1200, 2500, 'Gold', '2026-06-11 03:22:58.780');
INSERT INTO CustomerPoint (CustomerPointID, CustomerID, CurrentPoints, LifetimePoints, LevelName, UpdatedAt) VALUES (2, 2, 350, 800, 'Silver', '2026-06-11 03:22:58.780');
INSERT INTO CustomerPoint (CustomerPointID, CustomerID, CurrentPoints, LifetimePoints, LevelName, UpdatedAt) VALUES (3, 3, 350, 800, 'Silver', '2026-06-11 03:22:58.780');
INSERT INTO CustomerPoint (CustomerPointID, CustomerID, CurrentPoints, LifetimePoints, LevelName, UpdatedAt) VALUES (4, 4, 50, 50, 'Bronze', '2026-06-11 03:22:58.780');
INSERT INTO CustomerPoint (CustomerPointID, CustomerID, CurrentPoints, LifetimePoints, LevelName, UpdatedAt) VALUES (5, 5, 50, 50, 'Bronze', '2026-06-11 03:22:58.780');
SET IDENTITY_INSERT CustomerPoint OFF;
GO

-- Data for Voucher
SET IDENTITY_INSERT Voucher ON;
INSERT INTO Voucher (VoucherID, VoucherCode, VoucherName, DiscountType, DiscountValue, UsedQuantity, StartDate, EndDate, Status, CreatedAt) VALUES (1, 'WELCOME10', 'Giảm giá 10% khách hàng mới', 'PERCENT', 10.00, 0, '2026-01-01', '2026-12-31', 'active', '2026-06-11 03:22:58.787');
INSERT INTO Voucher (VoucherID, VoucherCode, VoucherName, DiscountType, DiscountValue, UsedQuantity, StartDate, EndDate, Status, CreatedAt) VALUES (2, 'FINORA50K', 'Giảm 50k VNFinora', 'FIXED', 50000.00, 0, '2026-01-01', '2026-12-31', 'active', '2026-06-11 03:22:58.787');
INSERT INTO Voucher (VoucherID, VoucherCode, VoucherName, DiscountType, DiscountValue, UsedQuantity, StartDate, EndDate, Status, CreatedAt) VALUES (3, 'VIP15', 'Giảm 15% khách hàng vip', 'PERCENT', 15.00, 0, '2026-01-01', '2026-12-31', 'active', '2026-06-11 03:22:58.787');
SET IDENTITY_INSERT Voucher OFF;
GO

-- Data for Supplier
SET IDENTITY_INSERT Supplier ON;
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (1, 'Công ty TNHH Thời Trang Việt', '0922000001', 'Long Biên, Hà Nội', 'active', '2026-06-11 03:22:58.790', '2026-06-11 03:22:58.790');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (2, 'Công ty Mỹ Phẩm Á Châu', '0922000002', 'Quận 7, TP.HCM', 'active', '2026-06-11 03:22:58.790', '2026-06-11 03:22:58.790');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (3, 'Nhà phân phối Phụ Kiện Fino', '0922000003', 'Hà Đông, Hà Nội', 'active', '2026-06-11 03:22:58.790', '2026-06-11 03:22:58.790');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (5, 'Cty Fami', '0366256248', 'Thanh Xuân, Hà Nội', 'active', '2026-06-18 07:39:15.230', '2026-06-18 07:40:01.373');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (6, 'HoruSoul', '0465896782', 'An Khánh, Hoài Đức, Hà Nội', 'active', '2026-06-18 08:07:57.933', '2026-06-18 08:07:57.933');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (7, 'Công ty Aikomi', '0487595625', 'Nam Từ Liêm, Hà Nội
', 'inactive', '2026-06-18 08:08:48.480', '2026-06-25 01:53:46.277');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (8, 'Công ty TNHH Masuto', '0745125458', 'Lê Trọng Tấn, Hà Nội', 'active', '2026-06-18 08:09:32.543', '2026-06-18 08:09:32.543');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (9, 'Công ty T ', '0557256258', 'Tây Hồ, Hà Nội', 'active', '2026-06-18 08:12:08.047', '2026-06-18 08:12:08.047');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (10, 'Công ty H', '0475458456', 'Quốc Oai, Hà Nội', 'active', '2026-06-18 08:12:31.857', '2026-06-18 08:12:31.857');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (11, 'Công ty Y', '0456285458', 'Chương Mỹ, Hà Nội', 'inactive', '2026-06-18 08:13:01.250', '2026-06-18 08:26:43.680');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (12, 'Cty Milo', '0458696585', 'Hà Nội
', 'active', '2026-06-18 08:17:08.327', '2026-06-18 08:17:08.327');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (13, 'Công ty Z', '0475458456', 'Quốc Oai, Hà Nội', 'active', '2026-06-23 03:10:44.220', '2026-06-23 03:10:58.710');
INSERT INTO Supplier (SupplierID, Name, Phone, Address, Status, CreatedAt, UpdatedAt) VALUES (14, 'Công ty ABC', '01223458796', 'Tuyên Quang', 'active', '2026-06-25 01:41:27.370', '2026-06-25 01:41:27.370');
SET IDENTITY_INSERT Supplier OFF;
GO

-- Data for Warehouse
SET IDENTITY_INSERT Warehouse ON;
INSERT INTO Warehouse (WarehouseID, Name, BranchID, Address, Status, CreatedAt) VALUES (1, 'Kho Cầu Giấy', 1, '123 Xuân Thủy, Cầu Giấy, Hà Nội', 'active', '2026-06-11 03:22:58.800');
INSERT INTO Warehouse (WarehouseID, Name, BranchID, Address, Status, CreatedAt) VALUES (2, 'Kho Hà Đông', 2, '50 Trần Phú, Hà Đông, Hà Nội', 'active', '2026-06-11 03:22:58.800');
INSERT INTO Warehouse (WarehouseID, Name, BranchID, Address, Status, CreatedAt) VALUES (3, 'Kho Hồ Chí Minh', 3, '45 Nguyễn Huệ, Quận 1, TP.HCM', 'active', '2026-06-11 03:22:58.800');
SET IDENTITY_INSERT Warehouse OFF;
GO

-- Data for Unit
SET IDENTITY_INSERT Unit ON;
INSERT INTO Unit (UnitID, Name, Description) VALUES (1, 'Cái', 'Đơn vị chiếc/cái');
INSERT INTO Unit (UnitID, Name, Description) VALUES (2, 'Hộp', 'Đơn vị hộp');
INSERT INTO Unit (UnitID, Name, Description) VALUES (3, 'Chai', 'Đơn vị chai');
INSERT INTO Unit (UnitID, Name, Description) VALUES (4, 'Bộ', 'Đơn vị bộ');
SET IDENTITY_INSERT Unit OFF;
GO

-- Data for Category
SET IDENTITY_INSERT Category ON;
INSERT INTO Category (CategoryID, Name, Description, ParentCategoryID, Status, CreatedAt, UpdatedAt) VALUES (1, 'Thời trang', 'Sản phẩm thời trang', NULL, 'active', '2026-06-11 03:22:58.810', '2026-06-11 03:22:58.810');
INSERT INTO Category (CategoryID, Name, Description, ParentCategoryID, Status, CreatedAt, UpdatedAt) VALUES (2, 'Mỹ Phẩm', 'Sản phẩm chăm sóc và làm đẹp', NULL, 'active', '2026-06-11 03:22:58.810', '2026-06-11 03:22:58.810');
INSERT INTO Category (CategoryID, Name, Description, ParentCategoryID, Status, CreatedAt, UpdatedAt) VALUES (3, 'Phụ kiện', 'Phụ kiện cá nhân', NULL, 'active', '2026-06-11 03:22:58.810', '2026-06-11 03:22:58.810');
SET IDENTITY_INSERT Category OFF;
GO

-- Data for Product
SET IDENTITY_INSERT Product ON;
INSERT INTO Product (ProductID, Name, Quantity, CategoryID, UnitID, SellingPrice, Status, CreatedAt, UpdatedAt) VALUES (1, 'Áo Thun', 120, 1, 1, 159000.00, 'Active', '2026-06-11 03:22:58.840', '2026-07-01 01:03:24.380');
INSERT INTO Product (ProductID, Name, Quantity, CategoryID, UnitID, SellingPrice, Status, CreatedAt, UpdatedAt) VALUES (2, 'Áo Sơ mi', 80, 1, 1, 299000.00, 'active', '2026-06-11 03:22:58.840', '2026-06-11 03:22:58.840');
INSERT INTO Product (ProductID, Name, Quantity, CategoryID, UnitID, SellingPrice, Status, CreatedAt, UpdatedAt) VALUES (3, 'Quần jean', 65, 1, 1, 399000.00, 'active', '2026-06-11 03:22:58.840', '2026-06-11 03:22:58.840');
INSERT INTO Product (ProductID, Name, Quantity, CategoryID, UnitID, SellingPrice, Status, CreatedAt, UpdatedAt) VALUES (4, 'Sữa rửa mặt', 200, 2, 3, 129000.00, 'active', '2026-06-11 03:22:58.840', '2026-06-11 03:22:58.840');
INSERT INTO Product (ProductID, Name, Quantity, CategoryID, UnitID, SellingPrice, Status, CreatedAt, UpdatedAt) VALUES (6, 'Túi tote', 90, 3, 1, 189000.00, 'active', '2026-06-11 03:22:58.840', '2026-06-11 03:22:58.840');
INSERT INTO Product (ProductID, Name, Quantity, CategoryID, UnitID, SellingPrice, Status, CreatedAt, UpdatedAt) VALUES (7, 'Ví da', 60, 3, 1, 249000.00, 'active', '2026-06-11 03:22:58.840', '2026-06-11 03:22:58.840');
INSERT INTO Product (ProductID, Name, Quantity, CategoryID, UnitID, SellingPrice, Status, CreatedAt, UpdatedAt) VALUES (8, 'Combo chăm sóc cá nhân', 40, 2, 4, 459000.00, 'active', '2026-06-11 03:22:58.840', '2026-06-11 03:22:58.840');
SET IDENTITY_INSERT Product OFF;
GO

-- Data for SupplierProduct
INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (1, 1, 100000.00);
INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (1, 2, 200000.00);
INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (1, 3, 250000.00);
INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (2, 4, 80000.00);
INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (2, 8, 300000.00);
INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (3, 6, 120000.00);
INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (3, 7, 150000.00);
GO

-- Data for Inventory
SET IDENTITY_INSERT Inventory ON;
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (1, 1, 1, 120, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (2, 1, 2, 80, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (3, 1, 3, 65, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (4, 1, 4, 200, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (6, 1, 6, 90, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (7, 1, 7, 60, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (8, 1, 8, 40, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (9, 2, 1, 60, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (10, 2, 2, 40, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (11, 2, 3, 32, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (12, 2, 4, 100, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (14, 2, 6, 45, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (15, 2, 7, 30, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (16, 2, 8, 20, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (17, 3, 1, 40, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (18, 3, 2, 26, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (19, 3, 3, 21, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (20, 3, 4, 66, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (22, 3, 6, 30, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (23, 3, 7, 20, '2026-06-11 03:22:58.847');
INSERT INTO Inventory (InventoryID, WarehouseID, ProductID, QuantityInStock, UpdatedAt) VALUES (24, 3, 8, 13, '2026-06-11 03:22:58.847');
SET IDENTITY_INSERT Inventory OFF;
GO

-- Data for Order
SET IDENTITY_INSERT [Order] ON;
INSERT INTO [Order] (OrderID, OrderCode, OrderType, CustomerID, BranchID, SupplierID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt) VALUES (1, 'OD202606001', 'SALE', 1, 1, NULL, 6, 3, 1, 816000.00, 122400.00, 693600.00, 'CASH', 'COMPLETED', '2026-06-01 09:15:00.000');
INSERT INTO [Order] (OrderID, OrderCode, OrderType, CustomerID, BranchID, SupplierID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt) VALUES (2, 'OD202606002', 'SALE', 2, 1, NULL, 7, 2, 1, 587000.00, 50000.00, 537000.00, 'BANKING', 'COMPLETED', '2026-06-02 10:40:00.000');
INSERT INTO [Order] (OrderID, OrderCode, OrderType, CustomerID, BranchID, SupplierID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt) VALUES (3, 'OD202606003', 'SALE', 4, 1, NULL, 6, 1, 1, 576000.00, 57600.00, 518400.00, 'CASH', 'COMPLETED', '2026-06-03 14:20:00.000');
INSERT INTO [Order] (OrderID, OrderCode, OrderType, CustomerID, BranchID, SupplierID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt) VALUES (4, 'OD202606004', 'SALE', 2, 2, NULL, 9, NULL, 2, 798000.00, 0.00, 798000.00, 'CARD', 'COMPLETED', '2026-06-04 16:30:00.000');
INSERT INTO [Order] (OrderID, OrderCode, OrderType, CustomerID, BranchID, SupplierID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt) VALUES (5, 'OD202606005', 'SALE', 3, 3, NULL, 10, 2, 3, 957000.00, 50000.00, 907000.00, 'BANKING', 'COMPLETED', '2026-06-05 11:05:00.000');
INSERT INTO [Order] (OrderID, OrderCode, OrderType, CustomerID, BranchID, SupplierID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt) VALUES (6, 'OD202606006', 'SALE', 5, 3, NULL, 10, NULL, 3, 348000.00, 0.00, 348000.00, 'CASH', 'COMPLETED', '2026-06-06 18:45:00.000');
INSERT INTO [Order] (OrderID, OrderCode, OrderType, CustomerID, BranchID, SupplierID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt) VALUES (7, 'OD202606007', 'SALE', 1, 1, NULL, 7, NULL, 1, 456000.00, 0.00, 456000.00, 'CASH', 'PENDING', '2026-06-07 12:00:00.000');
INSERT INTO [Order] (OrderID, OrderCode, OrderType, CustomerID, BranchID, SupplierID, EmployeeID, VoucherID, WarehouseID, Subtotal, DiscountAmount, TotalAmount, PaymentMethod, Status, CreatedAt) VALUES (8, 'OD202606008', 'SALE', 4, 2, NULL, 9, 1, 2, 647000.00, 64700.00, 582300.00, 'BANKING', 'COMPLETED', '2026-06-08 15:10:00.000');
SET IDENTITY_INSERT [Order] OFF;
GO

-- Data for OrderDetail
SET IDENTITY_INSERT OrderDetail ON;
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (1, 1, 1, 2, 159000.00, 318000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (2, 1, 3, 1, 399000.00, 399000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (4, 2, 2, 1, 299000.00, 299000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (5, 2, 6, 1, 189000.00, 189000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (7, 3, 4, 3, 129000.00, 387000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (8, 3, 6, 1, 189000.00, 189000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (9, 4, 3, 2, 399000.00, 798000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (10, 5, 8, 1, 459000.00, 459000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (11, 5, 7, 2, 249000.00, 498000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (12, 6, 1, 1, 159000.00, 159000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (13, 6, 6, 1, 189000.00, 189000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (14, 7, 4, 2, 129000.00, 258000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (16, 8, 2, 1, 299000.00, 299000.00);
INSERT INTO OrderDetail (OrderDetailID, OrderID, ProductID, Quantity, UnitPrice, TotalPrice) VALUES (17, 8, 7, 1, 249000.00, 249000.00);
SET IDENTITY_INSERT OrderDetail OFF;
GO

-- Data for Payment
SET IDENTITY_INSERT Payment ON;
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (1, 1, 'CASH', 693600.00, '2026-06-11 03:22:58.963', 'PAID', 'TXN-OD202606001', 'INCOME', 'Thanh toÃ¡n Ä‘Æ¡n hÃ ng OD202606001', 6, 1);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (2, 2, 'BANKING', 537000.00, '2026-06-11 03:22:58.963', 'PAID', 'TXN-OD202606002', 'INCOME', 'Thanh toÃ¡n Ä‘Æ¡n hÃ ng OD202606002', 7, 1);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (3, 3, 'CASH', 518400.00, '2026-06-11 03:22:58.963', 'PAID', 'TXN-OD202606003', 'INCOME', 'Thanh toÃ¡n Ä‘Æ¡n hÃ ng OD202606003', 6, 1);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (4, 4, 'CARD', 798000.00, '2026-06-11 03:22:58.963', 'PAID', 'TXN-OD202606004', 'INCOME', 'Thanh toÃ¡n Ä‘Æ¡n hÃ ng OD202606004', 9, 2);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (5, 5, 'BANKING', 907000.00, '2026-06-11 03:22:58.963', 'PAID', 'TXN-OD202606005', 'INCOME', 'Thanh toÃ¡n Ä‘Æ¡n hÃ ng OD202606005', 10, 3);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (6, 6, 'CASH', 348000.00, '2026-06-11 03:22:58.963', 'PAID', 'TXN-OD202606006', 'INCOME', 'Thanh toÃ¡n Ä‘Æ¡n hÃ ng OD202606006', 10, 3);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (7, 7, 'CASH', 456000.00, '2026-06-11 03:22:58.963', 'UNPAID', 'TXN-OD202606007', 'INCOME', 'Thanh toÃ¡n Ä‘Æ¡n hÃ ng OD202606007', 7, 1);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (8, 8, 'BANKING', 582300.00, '2026-06-11 03:22:58.963', 'PAID', 'TXN-OD202606008', 'INCOME', 'Thanh toÃ¡n Ä‘Æ¡n hÃ ng OD202606008', 9, 2);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (9, NULL, 'CASH', 200000.00, '2026-07-01 01:50:16.120', 'PAID', 'PC00001', 'EXPENSE', 'Thanh toán tiền điện', 2, NULL);
INSERT INTO Payment (PaymentID, OrderID, PaymentMethod, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID) VALUES (10, NULL, 'BANK_TRANSFER', 500000.00, '2026-07-01 01:52:57.640', 'PAID', 'PT00001', 'INCOME', 'Tiền thu công nợ ', 2, NULL);
SET IDENTITY_INSERT Payment OFF;
GO

-- Data for PointTransaction
SET IDENTITY_INSERT PointTransaction ON;
INSERT INTO PointTransaction (PointTransactionID, CustomerPointID, OrderID, BeforePoints, AfterPoints, Description, CreatedAt) VALUES (1, 1, 1, 1200, 1269, 'Cá»™ng Ä‘iá»ƒm tá»« Ä‘Æ¡n hÃ ng OD202606001', '2026-06-11 03:22:58.987');
INSERT INTO PointTransaction (PointTransactionID, CustomerPointID, OrderID, BeforePoints, AfterPoints, Description, CreatedAt) VALUES (2, 2, 2, 350, 403, 'Cá»™ng Ä‘iá»ƒm tá»« Ä‘Æ¡n hÃ ng OD202606002', '2026-06-11 03:22:58.987');
INSERT INTO PointTransaction (PointTransactionID, CustomerPointID, OrderID, BeforePoints, AfterPoints, Description, CreatedAt) VALUES (3, 2, 4, 350, 429, 'Cá»™ng Ä‘iá»ƒm tá»« Ä‘Æ¡n hÃ ng OD202606004', '2026-06-11 03:22:58.987');
INSERT INTO PointTransaction (PointTransactionID, CustomerPointID, OrderID, BeforePoints, AfterPoints, Description, CreatedAt) VALUES (4, 3, 5, 350, 440, 'Cá»™ng Ä‘iá»ƒm tá»« Ä‘Æ¡n hÃ ng OD202606005', '2026-06-11 03:22:58.987');
INSERT INTO PointTransaction (PointTransactionID, CustomerPointID, OrderID, BeforePoints, AfterPoints, Description, CreatedAt) VALUES (5, 4, 3, 50, 101, 'Cá»™ng Ä‘iá»ƒm tá»« Ä‘Æ¡n hÃ ng OD202606003', '2026-06-11 03:22:58.987');
INSERT INTO PointTransaction (PointTransactionID, CustomerPointID, OrderID, BeforePoints, AfterPoints, Description, CreatedAt) VALUES (6, 4, 8, 50, 108, 'Cá»™ng Ä‘iá»ƒm tá»« Ä‘Æ¡n hÃ ng OD202606008', '2026-06-11 03:22:58.987');
INSERT INTO PointTransaction (PointTransactionID, CustomerPointID, OrderID, BeforePoints, AfterPoints, Description, CreatedAt) VALUES (7, 5, 6, 50, 84, 'Cá»™ng Ä‘iá»ƒm tá»« Ä‘Æ¡n hÃ ng OD202606006', '2026-06-11 03:22:58.987');
SET IDENTITY_INSERT PointTransaction OFF;
GO

-- Data for StockTransfer
SET IDENTITY_INSERT StockTransfer ON;
INSERT INTO StockTransfer (StockTransferID, FromWarehouseID, ToWarehouseID, TransferCode, TransferDate, Status, Note, CreatedBy) VALUES (1, 1, 2, 'TF202606001', '2026-06-11 03:22:58.993', 'COMPLETED', 'Chuyá»ƒn hÃ ng bá»• sung cho chi nhÃ¡nh HÃ  ÄÃ´ng', 8);
SET IDENTITY_INSERT StockTransfer OFF;
GO

-- Data for StockTransferDetail
SET IDENTITY_INSERT StockTransferDetail ON;
INSERT INTO StockTransferDetail (StockTransferDetailID, StockTransferID, ProductID, Quantity) VALUES (1, 1, 1, 20);
SET IDENTITY_INSERT StockTransferDetail OFF;
GO

-- Data for StockTransaction
SET IDENTITY_INSERT StockTransaction ON;
INSERT INTO StockTransaction (StockTransactionID, WarehouseID, ProductID, ReferenceType, ReferenceID, TransactionType, Quantity, BeforeQuantity, AfterQuantity, Note, CreatedBy, CreatedAt) VALUES (1, 1, 1, 'TRANSFER', 1, 'OUT', 20, 100, 80, 'Xuáº¥t kho chuyá»ƒn hÃ ng', 8, '2026-06-11 03:22:59.020');
INSERT INTO StockTransaction (StockTransactionID, WarehouseID, ProductID, ReferenceType, ReferenceID, TransactionType, Quantity, BeforeQuantity, AfterQuantity, Note, CreatedBy, CreatedAt) VALUES (3, 2, 1, 'TRANSFER', 1, 'IN', 20, 50, 70, 'Nháº­p kho tá»« chuyá»ƒn hÃ ng', 8, '2026-06-11 03:22:59.030');
SET IDENTITY_INSERT StockTransaction OFF;
GO

-- Data for AuditLog
SET IDENTITY_INSERT AuditLog ON;
INSERT INTO AuditLog (AuditLogID, EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt) VALUES (1, 1, 'CREATE_SAMPLE_DATABASE', 'Database', 1, NULL, 'Created sample data for DBFinoraV2', '2026-06-11 03:22:59.040');
INSERT INTO AuditLog (AuditLogID, EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt) VALUES (2, 2, 'VIEW_EMPLOYEE_OVERVIEW', 'Employee', NULL, NULL, 'Owner viewed employee overview', '2026-06-11 03:22:59.040');
SET IDENTITY_INSERT AuditLog OFF;
GO


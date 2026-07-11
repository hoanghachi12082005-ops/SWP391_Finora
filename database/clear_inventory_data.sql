-- =============================================
-- Script: Làm sạch dữ liệu giao dịch và nạp dữ liệu tồn kho mẫu (Phân chia sản phẩm khác nhau giữa các kho)
-- Đồng thời nạp lịch sử giá nhập mẫu để liên kết Sản Phẩm với Nhà Cung Cấp
-- =============================================

-- Tắt kiểm tra khóa ngoại tạm thời
ALTER TABLE [point_transaction] NOCHECK CONSTRAINT ALL;
ALTER TABLE [payment] NOCHECK CONSTRAINT ALL;
ALTER TABLE [stock_transaction] NOCHECK CONSTRAINT ALL;
ALTER TABLE [stock_transfer_detail] NOCHECK CONSTRAINT ALL;
ALTER TABLE [stock_transfer] NOCHECK CONSTRAINT ALL;
ALTER TABLE [order_detail] NOCHECK CONSTRAINT ALL;
ALTER TABLE [order] NOCHECK CONSTRAINT ALL;
ALTER TABLE [inventory] NOCHECK CONSTRAINT ALL;

-- Xóa dữ liệu các bảng giao dịch cũ
DELETE FROM [point_transaction];
DELETE FROM [payment];
DELETE FROM [stock_transaction];
DELETE FROM [stock_transfer_detail];
DELETE FROM [stock_transfer];
DELETE FROM [order_detail];
DELETE FROM [order];
DELETE FROM [inventory];

-- Reset IDENTITY
DBCC CHECKIDENT ('point_transaction', RESEED, 0);
DBCC CHECKIDENT ('payment', RESEED, 0);
DBCC CHECKIDENT ('stock_transaction', RESEED, 0);
DBCC CHECKIDENT ('stock_transfer_detail', RESEED, 0);
DBCC CHECKIDENT ('stock_transfer', RESEED, 0);
DBCC CHECKIDENT ('order_detail', RESEED, 0);
DBCC CHECKIDENT ('order', RESEED, 0);
DBCC CHECKIDENT ('inventory', RESEED, 0);

-- Nạp tồn kho mẫu mới (Mỗi kho sở hữu các sản phẩm khác nhau để test điều chuyển chéo thực tế)
-- Kho 1: Chỉ có sản phẩm 1 đến 25
INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status) VALUES
(1,1,150,'ACTIVE'),(1,2,200,'ACTIVE'),(1,3,300,'ACTIVE'),(1,4,80,'ACTIVE'),(1,5,250,'ACTIVE'),
(1,6,180,'ACTIVE'),(1,7,220,'ACTIVE'),(1,8,140,'ACTIVE'),(1,9,120,'ACTIVE'),(1,10,100,'ACTIVE'),
(1,11,160,'ACTIVE'),(1,12,240,'ACTIVE'),(1,13,190,'ACTIVE'),(1,14,90,'ACTIVE'),(1,15,70,'ACTIVE'),
(1,16,85,'ACTIVE'),(1,17,300,'ACTIVE'),(1,18,110,'ACTIVE'),(1,19,200,'ACTIVE'),(1,20,130,'ACTIVE'),
(1,21,95,'ACTIVE'),(1,22,50,'ACTIVE'),(1,23,60,'ACTIVE'),(1,24,75,'ACTIVE'),(1,25,180,'ACTIVE');

-- Kho 2: Chỉ có sản phẩm 15 đến 38
INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status) VALUES
(2,15,60,'ACTIVE'),(2,16,75,'ACTIVE'),(2,17,280,'ACTIVE'),(2,18,100,'ACTIVE'),(2,19,180,'ACTIVE'),
(2,20,110,'ACTIVE'),(2,21,85,'ACTIVE'),(2,22,45,'ACTIVE'),(2,23,55,'ACTIVE'),(2,24,65,'ACTIVE'),
(2,25,160,'ACTIVE'),(2,26,200,'ACTIVE'),(2,27,140,'ACTIVE'),(2,28,280,'ACTIVE'),(2,29,80,'ACTIVE'),
(2,30,100,'ACTIVE'),(2,31,60,'ACTIVE'),(2,32,75,'ACTIVE'),(2,33,35,'ACTIVE'),(2,34,50,'ACTIVE'),
(2,35,180,'ACTIVE'),(2,36,130,'ACTIVE'),(2,37,90,'ACTIVE'),(2,38,100,'ACTIVE');

-- Kho 3: Chỉ có sản phẩm 30 đến 50
INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status) VALUES
(3,30,90,'ACTIVE'),(3,31,50,'ACTIVE'),(3,32,65,'ACTIVE'),(3,33,30,'ACTIVE'),(3,34,45,'ACTIVE'),
(3,35,150,'ACTIVE'),(3,36,110,'ACTIVE'),(3,37,80,'ACTIVE'),(3,38,90,'ACTIVE'),(3,39,60,'ACTIVE'),
(3,40,50,'ACTIVE'),(3,41,70,'ACTIVE'),(3,42,35,'ACTIVE'),(3,43,25,'ACTIVE'),(3,44,50,'ACTIVE'),
(3,45,100,'ACTIVE'),(3,46,140,'ACTIVE'),(3,47,50,'ACTIVE'),(3,48,40,'ACTIVE'),(3,49,20,'ACTIVE'),
(3,50,15,'ACTIVE');


-- NẠP ĐƠN NHẬP HÀNG MẪU (PURCHASE + COMPLETED) ĐỂ LIÊN KẾT NHÀ CUNG CẤP CHO SẢN PHẨM
-- NCC 1 (supplier_id = 1) liên kết với sản phẩm 1 đến 10
INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
VALUES ('PO-SEED-1', 'PURCHASE', 1, 1, 1, 1, 500000.0, 0.0, 500000.0, 'BANK_TRANSFER', 'COMPLETED', GETDATE());

INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price, supplier_id, supplier_status) VALUES
(1, 1, 1, 15000.0, 15000.0, 15000.0, 1, 'COMPLETED'),
(1, 2, 1, 20000.0, 20000.0, 20000.0, 1, 'COMPLETED'),
(1, 3, 1, 25000.0, 25000.0, 25000.0, 1, 'COMPLETED'),
(1, 4, 1, 30000.0, 30000.0, 30000.0, 1, 'COMPLETED'),
(1, 5, 1, 35000.0, 35000.0, 35000.0, 1, 'COMPLETED'),
(1, 6, 1, 40000.0, 40000.0, 40000.0, 1, 'COMPLETED'),
(1, 7, 1, 45000.0, 45000.0, 45000.0, 1, 'COMPLETED'),
(1, 8, 1, 50000.0, 50000.0, 50000.0, 1, 'COMPLETED'),
(1, 9, 1, 55000.0, 55000.0, 55000.0, 1, 'COMPLETED'),
(1, 10, 1, 60000.0, 60000.0, 60000.0, 1, 'COMPLETED');

-- NCC 2 (supplier_id = 2) liên kết với sản phẩm 11 đến 20
INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
VALUES ('PO-SEED-2', 'PURCHASE', 2, 1, 1, 1, 500000.0, 0.0, 500000.0, 'BANK_TRANSFER', 'COMPLETED', GETDATE());

INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price, supplier_id, supplier_status) VALUES
(2, 11, 1, 15000.0, 15000.0, 15000.0, 2, 'COMPLETED'),
(2, 12, 1, 20000.0, 20000.0, 20000.0, 2, 'COMPLETED'),
(2, 13, 1, 25000.0, 25000.0, 25000.0, 2, 'COMPLETED'),
(2, 14, 1, 30000.0, 30000.0, 30000.0, 2, 'COMPLETED'),
(2, 15, 1, 35000.0, 35000.0, 35000.0, 2, 'COMPLETED'),
(2, 16, 1, 40000.0, 40000.0, 40000.0, 2, 'COMPLETED'),
(2, 17, 1, 45000.0, 45000.0, 45000.0, 2, 'COMPLETED'),
(2, 18, 1, 50000.0, 50000.0, 50000.0, 2, 'COMPLETED'),
(2, 19, 1, 55000.0, 55000.0, 55000.0, 2, 'COMPLETED'),
(2, 20, 1, 60000.0, 60000.0, 60000.0, 2, 'COMPLETED');

-- NCC 3 (supplier_id = 3) liên kết với sản phẩm 21 đến 30
INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
VALUES ('PO-SEED-3', 'PURCHASE', 3, 1, 1, 1, 500000.0, 0.0, 500000.0, 'BANK_TRANSFER', 'COMPLETED', GETDATE());

INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price, supplier_id, supplier_status) VALUES
(3, 21, 1, 15000.0, 15000.0, 15000.0, 3, 'COMPLETED'),
(3, 22, 1, 20000.0, 20000.0, 20000.0, 3, 'COMPLETED'),
(3, 23, 1, 25000.0, 25000.0, 25000.0, 3, 'COMPLETED'),
(3, 24, 1, 30000.0, 30000.0, 30000.0, 3, 'COMPLETED'),
(3, 25, 1, 35000.0, 35000.0, 35000.0, 3, 'COMPLETED'),
(3, 26, 1, 40000.0, 40000.0, 40000.0, 3, 'COMPLETED'),
(3, 27, 1, 45000.0, 45000.0, 45000.0, 3, 'COMPLETED'),
(3, 28, 1, 50000.0, 50000.0, 50000.0, 3, 'COMPLETED'),
(3, 29, 1, 55000.0, 55000.0, 55000.0, 3, 'COMPLETED'),
(3, 30, 1, 60000.0, 60000.0, 60000.0, 3, 'COMPLETED');

-- NCC 4 (supplier_id = 4) liên kết với sản phẩm 31 đến 40
INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
VALUES ('PO-SEED-4', 'PURCHASE', 4, 1, 1, 1, 500000.0, 0.0, 500000.0, 'BANK_TRANSFER', 'COMPLETED', GETDATE());

INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price, supplier_id, supplier_status) VALUES
(4, 31, 1, 15000.0, 15000.0, 15000.0, 4, 'COMPLETED'),
(4, 32, 1, 20000.0, 20000.0, 20000.0, 4, 'COMPLETED'),
(4, 33, 1, 25000.0, 25000.0, 25000.0, 4, 'COMPLETED'),
(4, 34, 1, 30000.0, 30000.0, 30000.0, 4, 'COMPLETED'),
(4, 35, 1, 35000.0, 35000.0, 35000.0, 4, 'COMPLETED'),
(4, 36, 1, 40000.0, 40000.0, 40000.0, 4, 'COMPLETED'),
(4, 37, 1, 45000.0, 45000.0, 45000.0, 4, 'COMPLETED'),
(4, 38, 1, 50000.0, 50000.0, 50000.0, 4, 'COMPLETED'),
(4, 39, 1, 55000.0, 55000.0, 55000.0, 4, 'COMPLETED'),
(4, 40, 1, 60000.0, 60000.0, 60000.0, 4, 'COMPLETED');

-- NCC 5 (supplier_id = 5) liên kết với sản phẩm 41 đến 50
INSERT INTO [order] (order_code, order_type, supplier_id, emp_id, branch_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
VALUES ('PO-SEED-5', 'PURCHASE', 5, 1, 1, 1, 500000.0, 0.0, 500000.0, 'BANK_TRANSFER', 'COMPLETED', GETDATE());

INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price, supplier_id, supplier_status) VALUES
(5, 41, 1, 15000.0, 15000.0, 15000.0, 5, 'COMPLETED'),
(5, 42, 1, 20000.0, 20000.0, 20000.0, 5, 'COMPLETED'),
(5, 43, 1, 25000.0, 25000.0, 25000.0, 5, 'COMPLETED'),
(5, 44, 1, 30000.0, 30000.0, 30000.0, 5, 'COMPLETED'),
(5, 45, 1, 35000.0, 35000.0, 35000.0, 5, 'COMPLETED'),
(5, 46, 1, 40000.0, 40000.0, 40000.0, 5, 'COMPLETED'),
(5, 47, 1, 45000.0, 45000.0, 45000.0, 5, 'COMPLETED'),
(5, 48, 1, 50000.0, 50000.0, 50000.0, 5, 'COMPLETED'),
(5, 49, 1, 55000.0, 55000.0, 55000.0, 5, 'COMPLETED'),
(5, 50, 1, 60000.0, 60000.0, 60000.0, 5, 'COMPLETED');


-- Bật lại kiểm tra khóa ngoại
ALTER TABLE [point_transaction] CHECK CONSTRAINT ALL;
ALTER TABLE [payment] CHECK CONSTRAINT ALL;
ALTER TABLE [stock_transaction] CHECK CONSTRAINT ALL;
ALTER TABLE [stock_transfer_detail] CHECK CONSTRAINT ALL;
ALTER TABLE [stock_transfer] CHECK CONSTRAINT ALL;
ALTER TABLE [order_detail] CHECK CONSTRAINT ALL;
ALTER TABLE [order] CHECK CONSTRAINT ALL;
ALTER TABLE [inventory] CHECK CONSTRAINT ALL;

PRINT N'Đã làm sạch dữ liệu cũ và nạp lại tồn kho mẫu kèm lịch sử giá nhập liên kết NCC.';

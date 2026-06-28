-- ============================================================
--  SAMPLE DATA INSERTS FOR DBFinoraV3
-- ============================================================
USE DBFinoraV3
GO

-- 1. Role
INSERT INTO [Role] (role_name, discription) VALUES 
('Admin', 'Administrator with full access'),
('Manager', 'Branch Manager'),
('Staff', 'Regular Staff');
GO

-- 2. Branch
INSERT INTO Branch (branch_name, branch_code, address, district, city, phone, email, opening_time, closing_time, status) VALUES 
('Chi Nhanh Cau Giay', 'BR-CG-01', '123 Xuan Thuy', 'Cau Giay', 'Ha Noi', '0123456789', 'caugiay@finora.vn', '08:00', '22:00', 'ACTIVE'),
('Chi Nhanh Quan 1', 'BR-Q1-01', '456 Le Loi', 'Quan 1', 'TP.HCM', '0987654321', 'quan1@finora.vn', '08:00', '22:00', 'ACTIVE');
GO

-- 3. Employee
-- Employee password: $2a$12$DsMYzv6zgHkRuX/q/ufVZOpjO7QuvkpQ7n0CdJlFlC4AOrLxPu4Lm
INSERT INTO Employee (branch_id, role_id, fullName, gender, bod, address, email, phone, passwordHash, status) VALUES 
(1, 1, 'Nguyen Van Admin', 'Male', '1990-01-01', 'Ha Noi', 'admin@finora.vn', '0901000001', '$2a$12$DsMYzv6zgHkRuX/q/ufVZOpjO7QuvkpQ7n0CdJlFlC4AOrLxPu4Lm', 'ACTIVE'),
(1, 2, 'Tran Thi Manager', 'Female', '1992-05-15', 'Ha Noi', 'manager1@finora.vn', '0901000002', '$2a$12$DsMYzv6zgHkRuX/q/ufVZOpjO7QuvkpQ7n0CdJlFlC4AOrLxPu4Lm', 'ACTIVE'),
(2, 3, 'Le Van Staff', 'Male', '1995-10-20', 'TP.HCM', 'staff1@finora.vn', '0901000003', '$2a$12$DsMYzv6zgHkRuX/q/ufVZOpjO7QuvkpQ7n0CdJlFlC4AOrLxPu4Lm', 'ACTIVE');
GO

-- 5. Customer
INSERT INTO customer (full_name, gender, bod, address, email, phone, cus_type, total_spent) VALUES 
('Pham Khach Hang 1', 'Male', '1988-12-12', 'Ha Noi', 'kh1@gmail.com', '0911000001', 'VIP', 5000000),
('Hoang Khach Hang 2', 'Female', '1994-08-08', 'TP.HCM', 'kh2@gmail.com', '0911000002', 'NORMAL', 1000000),
('Vu Khach Hang 3', 'Male', '2000-02-02', 'Da Nang', 'kh3@gmail.com', '0911000003', 'NORMAL', 0);
GO

-- 6. Customer Point
INSERT INTO customer_point (cus_id, current_points, lifetime_points, level_name) VALUES 
(1, 500, 1000, 'Gold'),
(2, 100, 100, 'Silver'),
(3, 0, 0, 'Bronze');
GO

-- 7. Voucher
INSERT INTO voucher (voucher_code, voucher_name, discount_type, discount_value, used_quantity, start_date, end_date, status) VALUES 
('WELCOME2026', 'Welcome 2026', 'PERCENT', 10.00, 5, '2026-01-01', '2026-12-31', 'active'),
('MINUS50K', 'Giam 50K', 'FIXED', 50000.00, 10, '2026-06-01', '2026-07-01', 'active');
GO

-- 8. Supplier
INSERT INTO supplier (supplier_name, phone_number, address, status) VALUES 
('Cong ty TNHH Apple Viet Nam', '18001127', 'Quan 1, TP.HCM', 'ACTIVE'),
('Samsung Electronics Viet Nam', '1800588889', 'Thai Nguyen', 'ACTIVE');
GO

-- 9. Warehouse
INSERT INTO warehouse (warehouse_name, branch_id, address, status) VALUES 
('Kho Cau Giay', 1, '123 Xuan Thuy, Cau Giay', 'ACTIVE'),
('Kho Quan 1', 2, '456 Le Loi, Quan 1', 'ACTIVE');
GO

-- 10. Unit
INSERT INTO unit (unit_name, description) VALUES 
('Chiec', 'Don vi tinh tung cai/chiec'),
('Hop', 'Don vi tinh hop'),
('Bo', 'Don vi tinh bo');
GO

-- 11. Category
INSERT INTO category (category_name, description, parent_category_id, status) VALUES 
('Dien thoai di dong', 'Cac loai smartphone', NULL, 'ACTIVE'),
('May tinh xach tay', 'Cac loai laptop', NULL, 'ACTIVE'),
('Phu kien', 'Cap, sac, op lung...', NULL, 'ACTIVE'),
('iPhone', 'Dien thoai Apple', 1, 'ACTIVE'),
('Samsung', 'Dien thoai Samsung', 1, 'ACTIVE');
GO

-- 12. Product
INSERT INTO [product] (product_codebar, product_name, category_id, unit_id, selling_price) VALUES 
('89300001', 'iPhone 15 Pro Max 256GB', 4, 1, 30000000.00),
('89300002', 'Samsung Galaxy S24 Ultra', 5, 1, 28000000.00),
('89300003', 'MacBook Pro M3 14 inch', 2, 1, 40000000.00),
('89300004', 'Sac nhanh Apple 20W', 3, 1, 500000.00),
('89300005', 'Op lung Clear Case iPhone 15', 3, 1, 300000.00);
GO

-- 13. Inventory
INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status) VALUES 
(1, 1, 50, 'ACTIVE'),
(1, 2, 30, 'ACTIVE'),
(1, 4, 100, 'ACTIVE'),
(2, 3, 20, 'ACTIVE'),
(2, 5, 50, 'ACTIVE');
GO

-- 14. Order
INSERT INTO [order] (order_code, order_type, customer_id, branch_id, emp_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status) VALUES 
('ORD-20260624-001', 'SALE', 1, 1, 2, 1, 30500000.00, 50000.00, 30450000.00, 'BANK_TRANSFER', 'COMPLETED'),
('ORD-20260624-002', 'SALE', 2, 2, 3, 2, 40000000.00, 0.00, 40000000.00, 'CASH', 'PENDING');
GO

-- 15. Order Detail
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES 
(1, 1, 1, 30000000.00, 30000000.00),
(1, 4, 1, 500000.00, 500000.00),
(2, 3, 1, 40000000.00, 40000000.00);
GO

-- 16. Payment
INSERT INTO payment (order_id, payment_amount, payment_status, transaction_code) VALUES 
(1, 30450000.00, 'PAID', 'TXN-ABC-12345'),
(2, 0.00, 'PENDING', NULL);
GO

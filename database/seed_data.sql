-- ============================================================
-- DBFinoraV3 — Seed Data Script
-- Compatible with SQL Server (SSMS)
-- Generated: 2026-06-29
-- ============================================================

USE DBFinoraV3;
GO

-- ============================================================
-- 1. ROLES
-- ============================================================
INSERT INTO [Role] (role_name, discription) VALUES
('Admin',          N'System administrator with full access'),
('Owner',          N'Shop owner, views all business data'),
('StoreManager',   N'Manages a single branch and its employees'),
('SalesStaff',     N'Handles POS sales and customer service'),
('WarehouseStaff', N'Manages warehouse stock and inventory');
GO

-- ============================================================
-- 2. BRANCHES
-- ============================================================
INSERT INTO Branch (branch_name, branch_code, address, district, city, phone, email, opening_time, closing_time, status) VALUES
(N'Finora Hà Nội',     'BR-001', N'123 Trần Hưng Đạo', N'Hoàn Kiếm', N'Hà Nội',       '024-3822-0001', 'hn@finora.vn',    '07:00', '22:00', 'ACTIVE'),
(N'Finora Hồ Chí Minh','BR-002', N'456 Nguyễn Huệ',   N'Quận 1',    N'Hồ Chí Minh',  '028-3911-0002', 'hcm@finora.vn',   '07:00', '22:00', 'ACTIVE'),
(N'Finora Đà Nẵng',    'BR-003',  N'789 Bạch Đằng',    N'Hải Châu',  N'Đà Nẵng',      '0236-3555-0003','dn@finora.vn',    '07:00', '21:30', 'ACTIVE');
GO

-- ============================================================
-- 3. EMPLOYEES
-- ============================================================
INSERT INTO Employee (branch_id, role_id, fullName, gender, bod, address, email, phone, passwordHash, status) VALUES
-- Admin (branch 1)
(1, 1, N'Nguyễn Văn An',     N'Nam',  '1990-05-15', N'Hà Nội',     'admin@finora.vn',        '090-100-0001', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
-- Owner (branch 1)
(1, 2, N'Trần Thị Bình',     N'Nữ',   '1985-08-20', N'Hà Nội',     'owner@finora.vn',        '090-100-0002', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
-- Store Managers (one per branch)
(1, 3, N'Lê Văn Cường',      N'Nam',  '1992-03-10', N'Hà Nội',     'cuong.lv@finora.vn',     '090-100-0003', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 3, N'Phạm Thị Dung',     N'Nữ',   '1991-07-22', N'Hồ Chí Minh','dung.pt@finora.vn',      '090-100-0004', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 3, N'Hoàng Văn Em',      N'Nam',  '1993-11-05', N'Đà Nẵng',   'em.hv@finora.vn',        '090-100-0005', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
-- Sales Staff — Branch 1 (Hà Nội)
(1, 4, N'Nguyễn Thị Phương',  N'Nữ',   '1996-02-14', N'Hà Nội',     'phuong.nt@finora.vn',    '090-100-0006', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 4, N'Vũ Văn Giang',      N'Nam',  '1995-09-30', N'Hà Nội',     'giang.vv@finora.vn',     '090-100-0007', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 4, N'Đỗ Thị Hoa',        N'Nữ',   '1997-06-18', N'Hà Nội',     'hoa.dt@finora.vn',       '090-100-0008', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(1, 4, N'Bùi Văn Huy',       N'Nam',  '1994-12-01', N'Hà Nội',     'huy.bv@finora.vn',       '090-100-0009', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
-- Sales Staff — Branch 2 (Hồ Chí Minh)
(2, 4, N'Trương Thị Khanh',  N'Nữ',   '1998-04-25', N'Hồ Chí Minh','khanh.tt@finora.vn',     '090-100-0010', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 4, N'Đặng Văn Lâm',      N'Nam',  '1993-08-12', N'Hồ Chí Minh','lam.dv@finora.vn',       '090-100-0011', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 4, N'Võ Thị Mai',        N'Nữ',   '1996-01-07', N'Hồ Chí Minh','mai.vt@finora.vn',       '090-100-0012', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
-- Sales Staff — Branch 3 (Đà Nẵng)
(3, 4, N'Ngô Văn Nam',       N'Nam',  '1995-10-19', N'Đà Nẵng',   'nam.nv@finora.vn',       '090-100-0013', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 4, N'Dương Thị Oanh',    N'Nữ',   '1997-03-28', N'Đà Nẵng',   'oanh.dt@finora.vn',      '090-100-0014', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 4, N'Lý Văn Phúc',       N'Nam',  '1994-07-15', N'Đà Nẵng',   'phuc.lv@finora.vn',      '090-100-0015', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
-- Warehouse Staff — one per branch
(1, 5, N'Trần Văn Quân',     N'Nam',  '1993-04-10', N'Hà Nội',     'quan.tv@finora.vn',      '090-100-0016', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(2, 5, N'Lê Thị Ráng',       N'Nữ',   '1995-09-22', N'Hồ Chí Minh','rang.lt@finora.vn',       '090-100-0017', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE'),
(3, 5, N'Nguyễn Văn Sơn',    N'Nam',  '1994-12-05', N'Đà Nẵng',   'son.nv2@finora.vn',       '090-100-0018', '$2a$10$pPYncF3KjwYCFVeM6.R4GuEemqqHzz0VK29x2QjbPVRS1mILSQU6q', 'ACTIVE');
GO

-- ============================================================
-- 4. SUPPLIERS
-- ============================================================
INSERT INTO supplier (supplier_name, phone_number, address, status) VALUES
(N'Công ty TNHH Thực phẩm Xanh',     '024-3888-1111', N'Số 10 Láng Hạ, Hà Nội',          'ACTIVE'),
(N'Công ty CP Đồ uống Việt',         '028-3999-2222', N'123 Lê Duẩn, Hồ Chí Minh',       'ACTIVE'),
(N'Công ty TNHH Hàng tiêu dùng Nam', '0236-3777-3333', N'45 Nguyễn Văn Linh, Đà Nẵng',   'ACTIVE'),
(N'Tổng công ty Thương mại Bắc',     '024-3666-4444', N'78 Kim Mã, Hà Nội',              'ACTIVE'),
(N'Công ty CP Phân phối Miền Nam',   '028-3555-5555', N'456 Cách Mạng Tháng 8, HCM',     'ACTIVE');
GO

-- ============================================================
-- 5. WAREHOUSES (one per branch)
-- ============================================================
INSERT INTO warehouse (warehouse_name, branch_id, address, status) VALUES
(N'Kho Hà Nội',     1, N'KCN Bắc Từ Liêm, Hà Nội',      'ACTIVE'),
(N'Kho Hồ Chí Minh',2, N'KCN Tân Bình, Hồ Chí Minh',    'ACTIVE'),
(N'Kho Đà Nẵng',    3, N'KCN Liên Chiểu, Đà Nẵng',      'ACTIVE');
GO

-- ============================================================
-- 6. UNITS
-- ============================================================
INSERT INTO unit (unit_name, description) VALUES
(N'Cái',    N'Piece'),
(N'Thùng',  N'Box'),
(N'Chai',   N'Bottle'),
(N'Gói',    N'Pack'),
(N'Kg',     N'Kilogram');
GO

-- ============================================================
-- 7. CATEGORIES
-- ============================================================
INSERT INTO category (category_name, description, status) VALUES
(N'Đồ uống',       N'Beverages — soft drinks, water, juice',                'ACTIVE'),
(N'Bánh kẹo',      N'Snacks & Candy — chips, cookies, chocolate',           'ACTIVE'),
(N'Thực phẩm',     N'Grocery — rice, oil, noodles, seasoning',              'ACTIVE'),
(N'Đồ gia dụng',   N'Household — cleaning supplies, kitchenware',           'ACTIVE'),
(N'Chăm sóc cá nhân', N'Personal Care — toothpaste, soap, shampoo',         'ACTIVE'),
(N'Đồ ăn liền',    N'Instant Food — instant noodles, canned food',          'ACTIVE');
GO

-- ============================================================
-- 8. PRODUCTS (~50 products)
-- ============================================================
INSERT INTO [product] (product_codebar, product_name, category_id, unit_id, selling_price) VALUES
-- Beverages (cat 1)
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

-- Snacks (cat 2)
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

-- Grocery (cat 3)
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

-- Household (cat 4)
('8934567890155', N'Nước rửa chén Sunlight 750ml',   4, 3, 28000),
('8934567890156', N'Nước lau sàn Vfresh 1l',         4, 3, 22000),
('8934567890157', N'Bột giặt Tide 800g',             4, 4, 35000),
('8934567890158', N'Khăn giấy ướt Bobby 80 tờ',      4, 4, 12000),
('8934567890159', N'Túi rác 45x50 30 cái',           4, 4, 15000),
('8934567890160', N'Nến thơm Air Wick 1 lọ',         4, 1, 25000),

-- Personal Care (cat 5)
('8934567890161', N'Kem đánh răng Colgate 120g',     5, 1, 25000),
('8934567890162', N'Sữa tắm Lifebuoy 450ml',         5, 3, 32000),
('8934567890163', N'Dầu gội Sunsilk 350ml',          5, 3, 38000),
('8934567890164', N'Xà bông Lifebuoy 90g',          5, 1, 8000),
('8934567890165', N'Lăn khử mùi Nivea 50ml',         5, 1, 35000),
('8934567890166', N'Tã bỉm Merries Size M 56 miếng', 5, 1, 180000),

-- Instant Food (cat 6)
('8934567890167', N'Mì tôm Hảo Hảo 30 gói',         6, 2, 80000),
('8934567890168', N'Mì ly Hảo Hảo 67g',             6, 1, 8000),
('8934567890169', N'Phở bò Vifon 65g',              6, 4, 5000),
('8934567890170', N'Miến dong Phú Hương 200g',       6, 4, 10000),
('8934567890171', N'Cháo gà Vifon 60g',             6, 4, 5000),
('8934567890172', N'Bim Bim Oishi 20 gói',          6, 2, 85000),
('8934567890173', N'Cá hộp Hải Long 155g',           6, 1, 18000),
('8934567890174', N'Đồ hộp thịt heo Đức Việt 150g',  6, 1, 15000);
GO

-- ============================================================
-- 9. INVENTORY (all 50 products x 3 warehouses)
-- ============================================================
-- Warehouse 1 (Ha Noi): products 1-50
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
(1,46,180,'ACTIVE'),(1,47,70,'ACTIVE'),(1,48,50,'ACTIVE'),(1,49,30,'ACTIVE'),(1,50,25,'ACTIVE');
GO

-- Warehouse 2 (HCM): products 1-50
INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status) VALUES
(2,1,120,'ACTIVE'),(2,2,180,'ACTIVE'),(2,3,250,'ACTIVE'),(2,4,60,'ACTIVE'),(2,5,220,'ACTIVE'),
(2,6,160,'ACTIVE'),(2,7,200,'ACTIVE'),(2,8,110,'ACTIVE'),(2,9,100,'ACTIVE'),(2,10,90,'ACTIVE'),
(2,11,140,'ACTIVE'),(2,12,220,'ACTIVE'),(2,13,170,'ACTIVE'),(2,14,80,'ACTIVE'),(2,15,60,'ACTIVE'),
(2,16,75,'ACTIVE'),(2,17,280,'ACTIVE'),(2,18,100,'ACTIVE'),(2,19,180,'ACTIVE'),(2,20,110,'ACTIVE'),
(2,21,85,'ACTIVE'),(2,22,45,'ACTIVE'),(2,23,55,'ACTIVE'),(2,24,65,'ACTIVE'),(2,25,160,'ACTIVE'),
(2,26,200,'ACTIVE'),(2,27,140,'ACTIVE'),(2,28,280,'ACTIVE'),(2,29,80,'ACTIVE'),(2,30,100,'ACTIVE'),
(2,31,60,'ACTIVE'),(2,32,75,'ACTIVE'),(2,33,35,'ACTIVE'),(2,34,50,'ACTIVE'),(2,35,180,'ACTIVE'),
(2,36,130,'ACTIVE'),(2,37,90,'ACTIVE'),(2,38,100,'ACTIVE'),(2,39,70,'ACTIVE'),(2,40,55,'ACTIVE'),
(2,41,80,'ACTIVE'),(2,42,40,'ACTIVE'),(2,43,30,'ACTIVE'),(2,44,55,'ACTIVE'),(2,45,120,'ACTIVE'),
(2,46,160,'ACTIVE'),(2,47,60,'ACTIVE'),(2,48,45,'ACTIVE'),(2,49,25,'ACTIVE'),(2,50,20,'ACTIVE');
GO

-- Warehouse 3 (Da Nang): products 1-50
INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status) VALUES
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

-- ============================================================
-- 10. CUSTOMERS (30)
-- ============================================================
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
-- HCM customers
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
-- Da Nang customers
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

-- ============================================================
-- 11. CUSTOMER POINTS (one per customer)
-- ============================================================
INSERT INTO customer_point (cus_id, current_points, lifetime_points) VALUES
(1,25,250),(2,18,180),(3,32,320),(4,10,95),(5,41,410),
(6,7,67),(7,52,520),(8,11,110),(9,78,780),(10,3,34),
(11,61,610),(12,23,230),(13,9,89),(14,45,450),(15,15,150),
(16,82,820),(17,4,37),(18,29,290),(19,63,630),(20,5,50),
(21,34,340),(22,12,120),(23,56,560),(24,8,78),(25,21,210),
(26,47,470),(27,15,150),(28,69,690),(29,9,92),(30,28,280);
GO

-- ============================================================
-- 12. VOUCHERS (10)
-- ============================================================
INSERT INTO voucher (voucher_code, voucher_name, discount_type, discount_value, used_quantity, start_date, end_date, status) VALUES
('SALE10',   N'Giảm 10% đơn hàng',              'PERCENT', 10,   8,  '2026-01-01','2026-12-31','active'),
('SALE20',   N'Giảm 20% đơn hàng',              'PERCENT', 20,   4,  '2026-01-01','2026-06-30','active'),
('FIXED50',  N'Giảm 50,000đ đơn từ 500k',       'FIXED',   50000,5,  '2026-01-01','2026-12-31','active'),
('FIXED30',  N'Giảm 30,000đ đơn từ 300k',       'FIXED',   30000,7,  '2026-01-01','2026-12-31','active'),
('NEWYEAR',  N'Khuyến mãi Tết 2026',             'PERCENT', 15,   3,  '2026-01-20','2026-02-10','active'),
('SUMMER',   N'Mùa hè sôi động giảm 25%',        'PERCENT', 25,   6,  '2026-06-01','2026-08-31','active'),
('WELCOME',  N'Khách hàng mới giảm 10%',        'PERCENT', 10,   10, '2026-01-01','2026-12-31','active'),
('FIXED20',  N'Giảm 20,000đ',                    'FIXED',   20000,12, '2026-03-01','2026-12-31','active'),
('FIXED100', N'Giảm 100,000đ đơn từ 1 triệu',   'FIXED',   100000,2, '2026-04-01','2026-09-30','active'),
('SALE5',    N'Giảm 5% đơn hàng',               'PERCENT', 5,    15, '2026-01-01','2026-12-31','active');
GO

-- ============================================================
-- 13. ORDERS (100 SALE orders)
-- Spread across 3 branches, various employees/customers
-- ============================================================
-- Create all 100 orders with generated order codes
-- Since order_id is IDENTITY(1,1), these will get IDs 1-100

-- Helper: branch_id => warehouse_id mapping: 1->1, 2->2, 3->3
-- Employee mapping: branch 1: emp 3,6,7,8,9; branch 2: emp 4,10,11,12; branch 3: emp 5,13,14,15
-- Customers 1-10 -> branch 1, 11-20 -> branch 2, 21-30 -> branch 3
-- Vouchers applied randomly to ~40% of orders

-- I'll use a series of INSERT statements with various dates over 6 months

-- January 2026 (15 orders)
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
('ORD00015','SALE', 7, 1, 8, null, 1, 680000, 0,     680000,   'CASH',         'COMPLETED', '2026-01-30 08:45:00');

-- February 2026 (14 orders)
INSERT INTO [order] (order_code, order_type, customer_id, branch_id, emp_id, voucher_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at) VALUES
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
('ORD00029','SALE', 2, 1, 6, 7,    1, 290000, 29000, 261000,   'CASH',         'COMPLETED', '2026-02-28 08:15:00');

-- March 2026 (17 orders)
INSERT INTO [order] (order_code, order_type, customer_id, branch_id, emp_id, voucher_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at) VALUES
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
('ORD00046','SALE', 15,2, 12,null, 2, 650000, 0,     650000,   'BANK_TRANSFER','COMPLETED', '2026-03-30 10:15:00');

-- April 2026 (17 orders)
INSERT INTO [order] (order_code, order_type, customer_id, branch_id, emp_id, voucher_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at) VALUES
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
('ORD00063','SALE', 3, 1, 7, null, 1, 430000, 0,     430000,   'BANK_TRANSFER','COMPLETED', '2026-04-30 10:00:00');

-- May 2026 (18 orders)
INSERT INTO [order] (order_code, order_type, customer_id, branch_id, emp_id, voucher_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at) VALUES
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
('ORD00081','SALE', 16,2, 12,5,    2, 390000, 58500, 331500,   'BANK_TRANSFER','COMPLETED', '2026-05-31 09:00:00');

-- June 2026 (19 orders)
INSERT INTO [order] (order_code, order_type, customer_id, branch_id, emp_id, voucher_id, warehouse_id, subtotal, discount_amount, total_amount, payment_method, status, created_at) VALUES
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

-- ============================================================
-- 14. ORDER DETAILS (~400 records, each order has 1-5 products)
-- ============================================================
-- Order 1: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(1,1,2,10000,20000),(1,13,1,10000,10000),(1,4,1,45000,45000);
GO
-- Order 2: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(2,5,3,8000,24000),(2,12,2,7000,14000),(2,16,1,25000,25000),(2,8,2,9000,18000);
GO
-- Order 3: 5 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(3,22,2,120000,240000),(3,14,2,6000,12000),(3,6,1,45000,45000),(3,47,1,180000,180000),(3,49,2,18000,36000);
GO
-- Order 4: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(4,36,3,28000,84000),(4,9,2,8000,16000);
GO
-- Order 5: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(5,7,2,8000,16000),(5,42,1,32000,32000),(5,26,3,12000,36000),(5,44,2,15000,30000);
GO
-- Order 6: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(6,9,3,8000,24000),(6,16,1,25000,25000);
GO
-- Order 7: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(7,3,5,5000,25000),(7,20,2,7000,14000),(7,24,1,6000,6000);
GO
-- Order 8: 1 item
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(8,8,2,9000,18000);
GO
-- Order 9: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(9,22,3,120000,360000),(9,15,2,7000,14000),(9,38,1,12000,12000),(9,45,1,38000,38000);
GO
-- Order 10: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(10,13,2,10000,20000),(10,42,1,32000,32000),(10,21,1,95000,95000);
GO
-- Order 11: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(11,33,4,35000,140000),(11,5,2,8000,16000);
GO
-- Order 12: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(12,41,3,25000,75000),(12,7,2,8000,16000),(12,3,10,5000,50000);
GO
-- Order 13: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(13,40,2,12000,24000),(13,35,3,28000,84000);
GO
-- Order 14: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(14,44,3,15000,45000),(14,17,2,8000,16000),(14,11,4,6000,24000);
GO
-- Order 15: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(15,22,1,120000,120000),(15,27,2,4000,8000),(15,14,2,6000,12000),(15,5,4,8000,32000);
GO
-- Order 16: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(16,19,3,5000,15000),(16,42,1,32000,32000),(16,35,2,28000,56000);
GO
-- Order 17: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(17,10,2,12000,24000),(17,4,1,45000,45000);
GO
-- Order 18: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(18,36,2,28000,56000),(18,45,1,38000,38000),(18,1,3,10000,30000),(18,17,2,8000,16000);
GO
-- Order 19: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(19,8,2,9000,18000),(19,20,1,7000,7000);
GO
-- Order 20: 5 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(20,22,2,120000,240000),(20,41,1,25000,25000),(20,15,2,7000,14000),(20,27,2,4000,8000),(20,6,1,45000,45000);
GO
-- Order 21: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(21,11,4,6000,24000),(21,42,2,32000,64000),(21,26,3,12000,36000);
GO
-- Order 22: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(22,3,6,5000,30000),(22,13,1,10000,10000);
GO
-- Order 23: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(23,22,3,120000,360000),(23,8,2,9000,18000),(23,1,4,10000,40000),(23,43,1,8000,8000);
GO
-- Order 24: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(24,7,3,8000,24000),(24,20,2,7000,14000);
GO
-- Order 25: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(25,33,2,35000,70000),(25,7,2,8000,16000),(25,10,3,12000,36000);
GO
-- Order 26: 1 item
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(26,30,1,4000,4000);
GO
-- Order 27: 5 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(27,22,2,120000,240000),(27,49,2,18000,36000),(27,4,1,45000,45000),(27,9,3,8000,24000),(27,38,1,12000,12000);
GO
-- Order 28: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(28,19,2,5000,10000),(28,5,2,8000,16000),(28,34,1,35000,35000);
GO
-- Order 29: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(29,29,2,12000,24000),(29,16,1,25000,25000);
GO
-- Order 30: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(30,2,4,10000,40000),(30,12,2,7000,14000),(30,17,3,8000,24000);
GO
-- Order 31: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(31,24,2,6000,12000),(31,45,1,38000,38000),(31,40,1,12000,12000),(31,1,3,10000,30000);
GO
-- Order 32: 5 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(32,33,2,35000,70000),(32,9,2,8000,16000),(32,20,3,7000,21000),(32,4,1,45000,45000),(32,43,1,8000,8000);
GO
-- Order 33: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(33,35,2,28000,56000),(33,31,2,4000,8000);
GO
-- Order 34: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(34,11,3,6000,18000),(34,30,2,4000,8000);
GO
-- Order 35: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(35,22,1,120000,120000),(35,14,3,6000,18000),(35,5,4,8000,32000);
GO
-- Order 36: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(36,19,3,5000,15000),(36,42,1,32000,32000),(36,7,2,8000,16000),(36,33,1,35000,35000);
GO
-- Order 37: 5 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(37,22,3,120000,360000),(37,15,2,7000,14000),(37,44,2,15000,30000),(37,35,1,28000,28000),(37,1,3,10000,30000);
GO
-- Order 38: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(38,13,2,10000,20000),(38,20,3,7000,21000);
GO
-- Order 39: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(39,26,3,12000,36000),(39,3,6,5000,30000),(39,8,2,9000,18000);
GO
-- Order 40: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(40,17,2,8000,16000),(40,31,1,4000,4000);
GO
-- Order 41: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(41,22,2,120000,240000),(41,35,2,28000,56000),(41,10,3,12000,36000),(41,2,4,10000,40000);
GO
-- Order 42: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(42,6,2,45000,90000),(42,27,3,4000,12000),(42,14,1,6000,6000);
GO
-- Order 43: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(43,37,2,22000,44000),(43,5,2,8000,16000);
GO
-- Order 44: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(44,20,3,7000,21000),(44,11,2,6000,12000);
GO
-- Order 45: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(45,42,1,32000,32000),(45,39,2,12000,24000),(45,15,3,7000,21000);
GO
-- Order 46: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(46,22,2,120000,240000),(46,1,4,10000,40000),(46,4,1,45000,45000),(46,27,2,4000,8000);
GO
-- Order 47: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(47,13,3,10000,30000),(47,44,2,15000,30000),(47,7,4,8000,32000);
GO
-- Order 48: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(48,18,3,6000,18000),(48,21,1,95000,95000);
GO
-- Order 49: 5 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(49,22,4,120000,480000),(49,41,2,25000,50000),(49,8,3,9000,27000),(49,27,2,4000,8000),(49,3,6,5000,30000);
GO
-- Order 50: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(50,26,2,12000,24000),(50,6,1,45000,45000);
GO
-- Order 51: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(51,22,2,120000,240000),(51,33,1,35000,35000),(51,9,4,8000,32000);
GO
-- Order 52: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(52,37,2,22000,44000),(52,5,3,8000,24000);
GO
-- Order 53: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(53,9,4,8000,32000),(53,10,2,12000,24000),(53,6,1,45000,45000),(53,22,1,120000,120000);
GO
-- Order 54: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(54,44,2,15000,30000),(54,13,3,10000,30000),(54,17,2,8000,16000);
GO
-- Order 55: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(55,35,4,28000,112000),(55,31,2,4000,8000);
GO
-- Order 56: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(56,22,2,120000,240000),(56,15,3,7000,21000),(56,7,2,8000,16000);
GO
-- Order 57: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(57,3,5,5000,25000),(57,1,3,10000,30000),(57,19,2,5000,10000),(57,18,1,6000,6000);
GO
-- Order 58: 1 item
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(58,29,2,12000,24000);
GO
-- Order 59: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(59,11,4,6000,24000),(59,45,1,38000,38000),(59,20,2,7000,14000);
GO
-- Order 60: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(60,22,2,120000,240000),(60,8,3,9000,27000),(60,42,1,32000,32000),(60,13,2,10000,20000);
GO
-- Order 61: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(61,34,2,35000,70000),(61,36,1,28000,28000),(61,26,2,12000,24000);
GO
-- Order 62: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(62,5,3,8000,24000),(62,14,3,6000,18000);
GO
-- Order 63: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(63,2,4,10000,40000),(63,10,2,12000,24000),(63,28,2,18000,36000);
GO
-- Order 64: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(64,37,2,22000,44000),(64,16,1,25000,25000);
GO
-- Order 65: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(65,22,2,120000,240000),(65,1,3,10000,30000),(65,35,2,28000,56000),(65,20,2,7000,14000);
GO
-- Order 66: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(66,8,2,9000,18000),(66,17,2,8000,16000),(66,23,1,6000,6000);
GO
-- Order 67: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(67,11,4,6000,24000),(67,30,2,4000,8000);
GO
-- Order 68: 5 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(68,22,3,120000,360000),(68,4,1,45000,45000),(68,42,1,32000,32000),(68,15,2,7000,14000),(68,44,1,15000,15000);
GO
-- Order 69: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(69,22,2,120000,240000),(69,33,2,35000,70000),(69,9,3,8000,24000),(69,45,1,38000,38000);
GO
-- Order 70: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(70,39,2,12000,24000),(70,7,3,8000,24000);
GO
-- Order 71: 1 item
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(71,3,5,5000,25000);
GO
-- Order 72: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(72,2,4,10000,40000),(72,19,3,5000,15000),(72,13,2,10000,20000),(72,10,2,12000,24000);
GO
-- Order 73: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(73,22,1,120000,120000),(73,35,2,28000,56000),(73,27,2,4000,8000);
GO
-- Order 74: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(74,40,1,12000,12000),(74,26,2,12000,24000);
GO
-- Order 75: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(75,36,3,28000,84000),(75,1,4,10000,40000);
GO
-- Order 76: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(76,22,2,120000,240000),(76,41,1,25000,25000),(76,5,2,8000,16000),(76,17,3,8000,24000);
GO
-- Order 77: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(77,14,3,6000,18000),(77,8,2,9000,18000),(77,20,1,7000,7000);
GO
-- Order 78: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(78,15,2,7000,14000),(78,22,1,120000,120000),(78,44,2,15000,30000);
GO
-- Order 79: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(79,34,2,35000,70000),(79,7,2,8000,16000);
GO
-- Order 80: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(80,4,2,45000,90000),(80,20,3,7000,21000),(80,1,4,10000,40000),(80,27,2,4000,8000);
GO
-- Order 81: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(81,19,3,5000,15000),(81,42,1,32000,32000),(81,6,1,45000,45000);
GO
-- Order 82: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(82,22,2,120000,240000),(82,35,2,28000,56000),(82,5,2,8000,16000),(82,14,3,6000,18000);
GO
-- Order 83: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(83,33,2,35000,70000),(83,8,2,9000,18000);
GO
-- Order 84: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(84,22,2,120000,240000),(84,1,3,10000,30000),(84,4,1,45000,45000),(84,9,2,8000,16000);
GO
-- Order 85: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(85,38,2,12000,24000),(85,10,2,12000,24000),(85,22,1,120000,120000);
GO
-- Order 86: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(86,40,1,12000,12000),(86,45,1,38000,38000);
GO
-- Order 87: 1 item
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(87,11,4,6000,24000);
GO
-- Order 88: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(88,22,2,120000,240000),(88,33,1,35000,35000),(88,15,2,7000,14000);
GO
-- Order 89: 5 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(89,22,3,120000,360000),(89,41,1,25000,25000),(89,7,3,8000,24000),(89,44,2,15000,30000),(89,13,2,10000,20000);
GO
-- Order 90: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(90,17,3,8000,24000),(90,10,2,12000,24000);
GO
-- Order 91: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(91,37,2,22000,44000),(91,3,4,5000,20000);
GO
-- Order 92: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(92,22,2,120000,240000),(92,33,1,35000,35000),(92,1,3,10000,30000),(92,5,2,8000,16000);
GO
-- Order 93: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(93,2,4,10000,40000),(93,19,2,5000,10000),(93,10,2,12000,24000);
GO
-- Order 94: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(94,22,1,120000,120000),(94,42,1,32000,32000),(94,36,2,28000,56000);
GO
-- Order 95: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(95,35,3,28000,84000),(95,11,2,6000,12000);
GO
-- Order 96: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(96,13,3,10000,30000),(96,44,2,15000,30000),(96,9,2,8000,16000);
GO
-- Order 97: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(97,22,2,120000,240000),(97,4,1,45000,45000),(97,33,1,35000,35000),(97,15,3,7000,21000);
GO
-- Order 98: 2 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(98,20,3,7000,21000),(98,36,2,28000,56000);
GO
-- Order 99: 3 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(99,37,2,22000,44000),(99,17,2,8000,16000),(99,27,2,4000,8000);
GO
-- Order 100: 4 items
INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES
(100,22,1,120000,120000),(100,9,4,8000,32000),(100,1,3,10000,30000),(100,35,2,28000,56000);
GO

-- ============================================================
-- 15. PAYMENTS (one per completed order)
-- ============================================================
INSERT INTO payment (order_id, payment_amount, payment_date, payment_status, transaction_code)
SELECT o.order_id, o.total_amount, o.created_at, 'PAID',
       'TXN-' + o.order_code
FROM [order] o
WHERE o.status = 'COMPLETED'
ORDER BY o.order_id;
GO

-- ============================================================
-- 16. POINT TRANSACTIONS (one per SALE order)
-- amount_per_point = 100,000 VND
-- ============================================================
INSERT INTO point_transaction (cus_point_id, order_id, before_points, after_points, description, created_at)
SELECT
    cp.cus_point_id,
    o.order_id,
    cp.current_points - (o.total_amount / 100000) AS before_points,
    cp.current_points AS after_points,
    N'Earned ' + CAST((o.total_amount / 100000) AS NVARCHAR) + N' points from Order ' + o.order_code,
    o.created_at
FROM [order] o
JOIN customer_point cp ON cp.cus_id = o.customer_id
WHERE o.status = 'COMPLETED' AND o.customer_id IS NOT NULL AND o.total_amount >= 100000
ORDER BY o.order_id;
GO

-- Also insert point transactions for orders < 100k (0 points earned)
INSERT INTO point_transaction (cus_point_id, order_id, before_points, after_points, description, created_at)
SELECT
    cp.cus_point_id,
    o.order_id,
    cp.current_points,
    cp.current_points,
    N'Earned 0 points from Order ' + o.order_code + N' (below minimum)',
    o.created_at
FROM [order] o
JOIN customer_point cp ON cp.cus_id = o.customer_id
WHERE o.status = 'COMPLETED' AND o.customer_id IS NOT NULL AND o.total_amount < 100000
ORDER BY o.order_id;
GO

-- ============================================================
-- 17. STOCK TRANSFERS
-- 10 transfers between warehouses
-- ============================================================
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

-- ============================================================
-- 18. STOCK TRANSFER DETAILS (2-3 products per transfer)
-- ============================================================
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

-- ============================================================
-- 19. STOCK TRANSACTIONS (inventory movement history)
-- Sales deductions (one per order detail)
-- ============================================================
INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by, created_at)
SELECT
    o.warehouse_id,
    od.product_id,
    'ORDER',
    od.order_id,
    'SALE_DEDUCT',
    od.quantity,
    0,
    0,
    N'Deducted ' + CAST(od.quantity AS NVARCHAR) + N' units for Order ' + o.order_code,
    o.emp_id,
    o.created_at
FROM order_detail od
JOIN [order] o ON od.order_id = o.order_id
WHERE o.status = 'COMPLETED';
GO

-- Stock transfers (deduct from source)
INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by, created_at)
SELECT
    std.from_warehouse_id,
    std.product_id,
    'STOCK_TRANSFER',
    std.stock_transfer_id,
    'TRANSFER_OUT',
    std.quantity,
    0,
    0,
    N'Transferred out to warehouse',
    st.created_by,
    st.transfer_date
FROM (
    SELECT std.stock_transfer_detail_id AS id, std.stock_transfer_id, std.product_id, std.quantity,
           st.from_warehouse_id, st.transfer_date, st.created_by
    FROM stock_transfer_detail std
    JOIN stock_transfer st ON std.stock_transfer_id = st.stock_transfer_id
) std;
GO

-- Stock transfers (add to destination)
INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by, created_at)
SELECT
    std.to_warehouse_id,
    std.product_id,
    'STOCK_TRANSFER',
    std.stock_transfer_id,
    'TRANSFER_IN',
    std.quantity,
    0,
    0,
    N'Transferred in from warehouse',
    st.created_by,
    st.transfer_date
FROM (
    SELECT std.stock_transfer_detail_id AS id, std.stock_transfer_id, std.product_id, std.quantity,
           st.to_warehouse_id, st.transfer_date, st.created_by
    FROM stock_transfer_detail std
    JOIN stock_transfer st ON std.stock_transfer_id = st.stock_transfer_id
) std;
GO

-- ============================================================
-- 20. AUDIT LOGS (~200 records)
-- ============================================================
-- Login actions
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT emp_id, 'LOGIN', 'Employee', emp_id, NULL, NULL, DATEADD(HOUR, -ABS(CHECKSUM(NEWID())) % 720, GETDATE())
FROM Employee WHERE emp_id <= 15;
GO

-- Customer creation (one per customer)
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT
    CASE WHEN c.cus_id % 3 = 0 THEN 6 WHEN c.cus_id % 3 = 1 THEN 7 ELSE 8 END,
    'CREATE', 'Customer', c.cus_id, NULL, N'Created customer ' + c.full_name,
    DATEADD(DAY, -c.cus_id * 2, '2026-06-29')
FROM customer c;
GO

-- Employee creation (one per employee)
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT 1, 'CREATE', 'Employee', e.emp_id, NULL, N'Created employee ' + e.fullName, '2026-01-01'
FROM Employee e WHERE e.emp_id > 1;
GO

-- Order creation (one per order)
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT o.emp_id, 'CREATE', 'Order', o.order_id, NULL, N'POS checkout: ' + o.order_code, o.created_at
FROM [order] o WHERE o.status = 'COMPLETED';
GO

-- Redeem points (some customers)
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT
    6 + (c.cus_id % 10),
    'REDEEM_POINTS', 'Customer', c.cus_id, NULL, CAST(cp.current_points / 2 AS NVARCHAR),
    DATEADD(DAY, -5, GETDATE())
FROM customer c
JOIN customer_point cp ON cp.cus_id = c.cus_id
WHERE c.cus_id % 3 = 0 AND cp.current_points >= 50;
GO

-- Lock user actions
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT 1, 'LOCK', 'Employee', 5, 'ACTIVE', 'INACTIVE', '2026-03-15 10:30:00';
GO

INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT 1, 'UNLOCK', 'Employee', 5, 'INACTIVE', 'ACTIVE', '2026-03-20 14:00:00';
GO

-- Stock transfer actions
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT st.created_by, 'STOCK_TRANSFER', 'stock_transfer', st.stock_transfer_id, NULL,
       N'Transfer ' + st.transfer_code, st.transfer_date
FROM stock_transfer st;
GO

-- Product update actions
INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
SELECT
    3 + (p.product_id % 3),
    'UPDATE', 'Product', p.product_id,
    CAST(p.selling_price * 0.9 AS NVARCHAR),
    CAST(p.selling_price AS NVARCHAR),
    DATEADD(DAY, -p.product_id, '2026-06-01')
FROM product p
WHERE p.product_id % 5 = 0;
GO

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================
PRINT '=== DBFinoraV3 Seed Data Verification ===';
GO

-- 1. Total Employees
SELECT 'Total Employees' AS Metric, COUNT(*) AS Value FROM Employee;
GO

-- 2. Employees by Role
SELECT r.role_name, COUNT(*) AS Count
FROM Employee e
JOIN [Role] r ON e.role_id = r.role_id
GROUP BY r.role_name
ORDER BY r.role_name;
GO

-- 3. Total Customers
SELECT 'Total Customers' AS Metric, COUNT(*) AS Value FROM customer;
GO

-- 4. Customer Points Summary
SELECT
    'Avg Current Points' AS Metric,
    CAST(AVG(CAST(current_points AS DECIMAL(10,2))) AS DECIMAL(10,2)) AS Value
FROM customer_point
UNION ALL
SELECT 'Total Lifetime Points', CAST(SUM(lifetime_points) AS DECIMAL(10,2)) FROM customer_point;
GO

-- 5. Total Orders
SELECT 'Total Orders' AS Metric, COUNT(*) AS Value FROM [order];
GO

-- 6. Orders by Status
SELECT status, COUNT(*) AS Count FROM [order] GROUP BY status;
GO

-- 7. Sales by Employee
SELECT TOP 10
    e.fullName,
    COUNT(o.order_id) AS OrderCount,
    ISNULL(SUM(o.total_amount), 0) AS TotalSales
FROM Employee e
LEFT JOIN [order] o ON e.emp_id = o.emp_id AND o.status = 'COMPLETED'
GROUP BY e.fullName
ORDER BY TotalSales DESC;
GO

-- 8. Sales by Branch
SELECT
    b.branch_name,
    COUNT(o.order_id) AS OrderCount,
    ISNULL(SUM(o.total_amount), 0) AS TotalSales
FROM Branch b
LEFT JOIN [order] o ON b.branch_id = o.branch_id AND o.status = 'COMPLETED'
GROUP BY b.branch_name
ORDER BY TotalSales DESC;
GO

-- 9. Inventory by Warehouse
SELECT
    w.warehouse_name,
    COUNT(i.inventory_id) AS ProductCount,
    ISNULL(SUM(i.quantity_in_stock), 0) AS TotalStock
FROM warehouse w
LEFT JOIN inventory i ON w.warehouse_id = i.warehouse_id
GROUP BY w.warehouse_name
ORDER BY w.warehouse_name;
GO

-- 10. Products with Low Stock (< 50)
SELECT p.product_name, i.quantity_in_stock, w.warehouse_name
FROM inventory i
JOIN [product] p ON i.product_id = p.product_id
JOIN warehouse w ON i.warehouse_id = w.warehouse_id
WHERE i.quantity_in_stock < 50
ORDER BY i.quantity_in_stock;
GO

-- 11. Point Transaction Count
SELECT 'Point Transactions' AS Metric, COUNT(*) AS Value FROM point_transaction;
GO

-- 12. Payment Count & Total
SELECT
    COUNT(*) AS PaymentCount,
    ISNULL(SUM(payment_amount), 0) AS TotalPayments
FROM payment
WHERE payment_status = 'PAID';
GO

-- 13. Audit Log Count
SELECT 'Audit Log Entries' AS Metric, COUNT(*) AS Value FROM audit_log;
GO

-- 14. Verify every customer has exactly one customer_point
SELECT
    CASE
        WHEN COUNT(*) = (SELECT COUNT(*) FROM customer WHERE status = 'ACTIVE')
        THEN 'OK' ELSE 'MISSING'
    END AS CustomerPointIntegrity,
    COUNT(*) AS CustomerPointCount,
    (SELECT COUNT(*) FROM customer WHERE status = 'ACTIVE') AS TotalActiveCustomers
FROM customer_point;
GO

-- 15. Verify every completed order has a payment
SELECT
    CASE
        WHEN COUNT(*) = (SELECT COUNT(*) FROM [order] WHERE status = 'COMPLETED')
        THEN 'OK' ELSE 'MISSING'
    END AS PaymentIntegrity,
    COUNT(DISTINCT order_id) AS PaymentOrderCount,
    (SELECT COUNT(*) FROM [order] WHERE status = 'COMPLETED') AS CompletedOrderCount
FROM payment;
GO

-- 16. Product Categories
SELECT c.category_name, COUNT(p.product_id) AS ProductCount
FROM category c
LEFT JOIN [product] p ON c.category_id = p.category_id
GROUP BY c.category_name
ORDER BY c.category_name;
GO

-- 17. Stock Transfers Summary
SELECT
    'Total Transfers' AS Metric,
    COUNT(*) AS Value
FROM stock_transfer;
GO

-- 18. Stock Transfer Details Count
SELECT
    'Transfer Detail Items' AS Metric,
    COUNT(*) AS Value
FROM stock_transfer_detail;
GO

PRINT '=== Seed Data Generation Complete ===';
GO

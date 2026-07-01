# Chi Tiết Các Bảng Cơ Sở Dữ Liệu

**Dự án:** FinoraRetail (SWP391_Finora)  
**Cơ sở dữ liệu:** DBFinoraV2 trên SQL Server  
**Số lượng bảng:** 21 bảng  
**Phiên bản tài liệu:** 1.0  
**Ngày cập nhật:** 21/06/2026

---

## Mục Lục

1. [Nhóm Identity & Access (Nhận dạng và Truy cập)](#1-nhóm-identity--access)
2. [Nhóm Business Partners (Đối tác Kinh doanh)](#2-nhóm-business-partners)
3. [Nhóm Commerce (Thương mại)](#3-nhóm-commerce)
4. [Nhóm Warehouse & Stock (Kho hàng và Tồn kho)](#4-nhóm-warehouse--stock)
5. [Nhóm System (Hệ thống)](#5-nhóm-system)

---

## 1. Nhóm Identity & Access

Nhóm này bao gồm các bảng phục vụ hệ thống nhận dạng người dùng, phân quyền và quản lý chi nhánh của tổ chức.

### 1.1. Bảng `role`

**Mục đích:** Lưu trữ thông tin về các vai trò (role) trong hệ thống, phục vụ cho việc phân quyền người dùng. Mỗi vai trò đại diện cho một nhóm quyền hạn cụ thể mà nhân viên có thể được gán.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `role_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi vai trò |
| `role_name` | NVARCHAR(100) | NOT NULL | Tên vai trò (ví dụ: OWNER, ADMIN, CASHIER, WAREHOUSE_MANAGER) |
| `discription` | NVARCHAR(255) | | Mô tả chi tiết về vai trò và quyền hạn của vai trò đó |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo, mặc định là thời điểm hiện tại |
| `updated_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `role_id`

**Khóa ngoại:** Không có

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `role_id`

**Ghi chú:**
- Bảng này đóng vai trò là danh mục tĩnh, thường chỉ được thêm mới khi hệ thống mở rộng chức năng
- Tên cột `discription` là từ viết sai chính tả của "description" trong mã nguồn gốc, được giữ nguyên để đảm bảo tương thích ngược
- Các vai trò phổ biến bao gồm: OWNER (Chủ sở hữu), ADMIN (Quản trị viên), CASHIER (Thu ngân), WAREHOUSE_MANAGER (Quản lý kho)

---

### 1.2. Bảng `branch`

**Mục đích:** Lưu trữ thông tin về các chi nhánh (cửa hàng) trong hệ thống bán lẻ. Mỗi chi nhánh hoạt động như một đơn vị kinh doanh độc lập với địa chỉ, giờ mở cửa và kho hàng riêng.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `branch_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi chi nhánh |
| `branch_name` | NVARCHAR(150) | NOT NULL | Tên đầy đủ của chi nhánh (ví dụ: FinoraRetail Quận 1) |
| `branch_code` | NVARCHAR(50) | UNIQUE | Mã định danh ngắn gọn và duy nhất cho chi nhánh (ví dụ: FNR-HCM-01) |
| `address` | NVARCHAR(300) | | Địa chỉ vật lý đầy đủ của chi nhánh, bao gồm số nhà, đường, phường/xã, quận/huyện, thành phố |
| `phone` | NVARCHAR(20) | | Số điện thoại liên hệ của chi nhánh |
| `email` | NVARCHAR(150) | | Địa chỉ email chính thức của chi nhánh |
| `opening_time` | NVARCHAR(10) | | Giờ mở cửa theo định dạng HH:mm (ví dụ: 08:00) |
| `closing_time` | NVARCHAR(10) | | Giờ đóng cửa theo định dạng HH:mm (ví dụ: 22:00) |
| `status` | NVARCHAR(20) | DEFAULT 'active', CHECK | Trạng thái hoạt động của chi nhánh: 'active' (đang hoạt động) hoặc 'locked' (bị khóa) |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo |
| `update_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `branch_id`

**Khóa ngoại:** Không có

**Ràng buộc CHECK trên `status`:**
```
status IN ('active', 'locked')
```

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `branch_id`
- Chỉ mục unique trên `branch_code`

**Ghi chú:**
- Mỗi chi nhánh có thể có nhiều nhân viên và nhiều kho hàng
- Trạng thái 'locked' có thể được sử dụng khi chi nhánh tạm ngừng hoạt động hoặc bị đình chỉ
- Giờ mở/đóng cửa được lưu dưới dạng chuỗi thay vì TIME để dễ dàng hiển thị và xử lý trong ứng dụng

---

### 1.3. Bảng `employee`

**Mục đích:** Lưu trữ thông tin nhân viên của hệ thống, bao gồm thông tin cá nhân, thông tin đăng nhập và thông tin liên kết với chi nhánh. Đây là bảng trung tâm cho hệ thống nhận dạng và phân quyền người dùng.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `emp_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi nhân viên |
| `branch_id` | INT | FK -> branch(branch_id), NOT NULL | Liên kết đến chi nhánh mà nhân viên thuộc về |
| `full_name` | NVARCHAR(150) | NOT NULL | Họ và tên đầy đủ của nhân viên |
| `gender` | NVARCHAR(10) | | Giới tính của nhân viên (Nam/Nữ/Khác hoặc Male/Female/Other) |
| `bod` | DATE | | Ngày sinh của nhân viên |
| `address` | NVARCHAR(300) | | Địa chỉ thường trú hoặc liên hệ của nhân viên |
| `email` | NVARCHAR(150) | UNIQUE | Địa chỉ email cá nhân hoặc công ty của nhân viên, duy nhất không trùng lặp |
| `phone` | NVARCHAR(20) | | Số điện thoại liên hệ của nhân viên |
| `password_hash` | NVARCHAR(255) | | Chuỗi băm mật khẩu (hash) được lưu trữ để xác thực đăng nhập |
| `status` | NVARCHAR(20) | DEFAULT 'ACTIVE', CHECK | Trạng thái nhân viên: 'ACTIVE' (đang làm việc), 'INACTIVE' (ngừng việc), 'ON_LEAVE' (nghỉ phép) |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo |
| `update_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `emp_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `branch_id` | `branch(branch_id)` | NO ACTION | NO ACTION |

**Ràng buộc CHECK trên `status`:**
```
status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE')
```

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `emp_id`
- Chỉ mục unique trên `email`
- Khóa ngoại trên `branch_id` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- Mỗi nhân viên chỉ thuộc về một chi nhánh duy nhất tại một thời điểm
- Mật khẩu được lưu dưới dạng hash (không lưu plain text) để bảo mật
- Phân quyền của nhân viên được quản lý thông qua bảng trung gian `employee_role`
- Trạng thái 'ON_LEAVE' cho phép giữ lại thông tin nhân viên khi đang nghỉ phép dài hạn

---

### 1.4. Bảng `employee_role`

**Mục đích:** Bảng trung gian (junction table) thực hiện quan hệ nhiều-nhiều (many-to-many) giữa bảng `employee` và bảng `role`. Mỗi nhân viên có thể có nhiều vai trò và mỗi vai trò có thể được gán cho nhiều nhân viên.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `emp_role_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi liên kết |
| `emp_id` | INT | FK -> employee(emp_id), NOT NULL | Liên kết đến nhân viên được gán vai trò |
| `role_id` | INT | FK -> role(role_id), NOT NULL | Liên kết đến vai trò được gán |
| `assigned_at` | DATETIME | DEFAULT GETDATE() | Thời điểm vai trò được gán cho nhân viên |

**Khóa chính:** `emp_role_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `emp_id` | `employee(emp_id)` | CASCADE | NO ACTION |
| `role_id` | `role(role_id)` | NO ACTION | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `emp_role_id`
- Chỉ mục unique trên cặp (`emp_id`, `role_id`) để đảm bảo mỗi nhân viên không bị gán trùng vai trò

**Ghi chú:**
- Cặp (`emp_id`, `role_id`) nên có ràng buộc unique để tránh gán trùng vai trò cho cùng một nhân viên
- Khi xóa nhân viên, các liên kết vai trò tương ứng cũng được xóa (CASCADE)
- Khi xóa vai trò, các liên kết không bị xóa tự động (NO ACTION) để tránh mất dữ liệu vô tình

---

## 2. Nhóm Business Partners

Nhóm này bao gồm các bảng lưu trữ thông tin về khách hàng, nhà cung cấp và các đối tác kinh doanh khác.

### 2.1. Bảng `customer`

**Mục đích:** Lưu trữ thông tin khách hàng của hệ thống bán lẻ. Khách hàng có thể là cá nhân hoặc tổ chức, được theo dõi để phục vụ các chương trình khuyến mãi, tích điểm và chăm sóc khách hàng.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `cus_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi khách hàng |
| `full_name` | NVARCHAR(150) | NOT NULL | Họ và tên đầy đủ của khách hàng |
| `gender` | NVARCHAR(10) | | Giới tính của khách hàng |
| `bod` | DATE | | Ngày sinh của khách hàng |
| `address` | NVARCHAR(300) | | Địa chỉ giao hàng hoặc liên hệ của khách hàng |
| `email` | NVARCHAR(150) | | Địa chỉ email của khách hàng |
| `phone` | NVARCHAR(20) | | Số điện thoại liên hệ của khách hàng |
| `cus_type` | NVARCHAR(50) | | Loại khách hàng: RETAIL (bán lẻ), WHOLESALE (bán sỉ), VIP, MEMBER,... |
| `total_spent` | DECIMAL(18,2) | DEFAULT 0 | Tổng số tiền khách hàng đã chi tiêu tại hệ thống |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo (thời điểm khách hàng đăng ký) |
| `updated_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `cus_id`

**Khóa ngoại:** Không có

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `cus_id`

**Ghi chú:**
- Bảng này không có ràng buộc unique trên email hoặc phone, cho phép nhiều khách hàng không cung cấp thông tin liên hệ
- `total_spent` được cập nhật mỗi khi khách hàng thực hiện giao dịch mua hàng
- Mỗi khách hàng có bản ghi điểm thưởng tương ứng trong bảng `customer_point`
- Trường `cus_type` hỗ trợ phân loại khách hàng để áp dụng chính sách giá và khuyến mãi khác nhau

---

### 2.2. Bảng `customer_point`

**Mục đích:** Lưu trữ thông tin điểm thưởng và cấp bậc của khách hàng trong chương trình tích điểm. Mỗi khách hàng có tối đa một bản ghi điểm thưởng, thể hiện quan hệ một-một với bảng `customer`.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `cus_point_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi bản ghi điểm |
| `cus_id` | INT | FK -> customer(cus_id), NOT NULL | Liên kết đến khách hàng sở hữu điểm thưởng |
| `current_points` | INT | DEFAULT 0 | Số điểm hiện tại có thể sử dụng được |
| `lifetime_points` | INT | DEFAULT 0 | Tổng số điểm khách hàng đã tích được trong toàn bộ thời gian |
| `level_name` | NVARCHAR(50) | | Tên cấp bậc thành viên (ví dụ: Bronze, Silver, Gold, Platinum) |
| `updated_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `cus_point_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `cus_id` | `customer(cus_id)` | CASCADE | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `cus_point_id`
- Khóa ngoại trên `cus_id` tự động tạo chỉ mục non-clustered
- Nên có chỉ mục unique trên `cus_id` để đảm bảo mỗi khách hàng chỉ có một bản ghi điểm

**Ghi chú:**
- Quan hệ một-một với bảng `customer`: mỗi khách hàng có đúng một bản ghi điểm
- `current_points` giảm khi khách hàng đổi điểm, `lifetime_points` không giảm
- Lịch sử thay đổi điểm được ghi lại trong bảng `point_transaction`
- Hệ thống cấp bậc có thể được tính toán dựa trên `lifetime_points`

---

### 2.3. Bảng `voucher`

**Mục đích:** Lưu trữ thông tin về các voucher (mã giảm giá, phiếu khuyến mãi) trong hệ thống. Voucher có thể được áp dụng cho đơn hàng để giảm giá theo tỷ lệ phần trăm hoặc số tiền cố định.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `voucher_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi voucher |
| `voucher_code` | NVARCHAR(50) | UNIQUE, NOT NULL | Mã voucher duy nhất mà khách hàng nhập khi sử dụng |
| `voucher_name` | NVARCHAR(150) | | Tên/mô tả ngắn gọn của voucher (ví dụ: Giảm 10% Tết 2026) |
| `discount_type` | NVARCHAR(20) | CHECK | Loại giảm giá: 'PERCENT' (theo %) hoặc 'FIXED' (số tiền cố định) |
| `discount_value` | DECIMAL(18,2) | | Giá trị giảm giá: % hoặc số tiền VND tùy thuộc vào discount_type |
| `used_quantity` | INT | DEFAULT 0 | Số lượt voucher đã được sử dụng |
| `start_date` | DATE | | Ngày bắt đầu hiệu lực của voucher |
| `end_date` | DATE | | Ngày kết thúc hiệu lực của voucher |
| `status` | NVARCHAR(20) | DEFAULT 'active' | Trạng thái voucher: 'active', 'inactive', 'expired' |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo |

**Khóa chính:** `voucher_id`

**Khóa ngoại:** Không có

**Ràng buộc CHECK trên `discount_type`:**
```
discount_type IN ('PERCENT', 'FIXED')
```

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `voucher_id`
- Chỉ mục unique trên `voucher_code`

**Ghi chú:**
- `voucher_code` phải duy nhất để mỗi mã chỉ đại diện cho một voucher duy nhất
- Voucher có thể được thiết kế để giới hạn số lần sử dụng hoặc số lượt sử dụng tối đa
- Hệ thống cần kiểm tra `start_date` và `end_date` khi xác thực voucher
- Voucher được áp dụng cho đơn hàng thông qua khóa ngoại trong bảng `[order]`

---

### 2.4. Bảng `supplier`

**Mục đích:** Lưu trữ thông tin về các nhà cung cấp (nhà phân phối, nhà sản xuất) trong hệ thống quản lý chuỗi cung ứng. Thông tin này phục vụ cho việc quản lý nhập hàng và theo dõi nguồn gốc sản phẩm.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `supplier_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi nhà cung cấp |
| `supplier_name` | NVARCHAR(150) | NOT NULL | Tên đầy đủ của nhà cung cấp |
| `phone_number` | NVARCHAR(20) | | Số điện thoại liên hệ với nhà cung cấp |
| `address` | NVARCHAR(300) | | Địa chỉ văn phòng hoặc kho hàng của nhà cung cấp |
| `status` | NVARCHAR(20) | DEFAULT 'active' | Trạng thái của nhà cung cấp: 'active' (đang hợp tác) hoặc 'inactive' (ngừng hợp tác) |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo |
| `updated_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `supplier_id`

**Khóa ngoại:** Không có

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `supplier_id`

**Ghi chú:**
- Nhà cung cấp được liên kết với đơn nhập hàng trong bảng `[order]` (với order_type = 'PURCHASE')
- Trạng thái 'inactive' cho phép giữ lại thông tin nhà cung cấp trong lịch sử mà không hiển thị trong danh sách chọn

---

## 3. Nhóm Commerce

Nhóm này bao gồm các bảng phục vụ cho hoạt động thương mại: quản lý đơn hàng, thanh toán và giao dịch điểm thưởng.

### 3.1. Bảng `[order]`

**Mục đích:** Lưu trữ thông tin về đơn hàng trong hệ thống. Bảng này hỗ trợ nhiều loại đơn hàng: đơn bán hàng (SALE), đơn nhập hàng (PURCHASE), đơn trả hàng (RETURN). Đây là bảng trung tâm của hệ thống thương mại với nhiều khóa ngoại liên kết đến các bảng khác.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `order_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi đơn hàng |
| `order_code` | NVARCHAR(50) | UNIQUE | Mã đơn hàng duy nhất, thường có định dạng: ORD-YYYYMMDD-XXXXX |
| `order_type` | NVARCHAR(30) | | Loại đơn hàng: SALE (bán hàng), PURCHASE (nhập hàng), RETURN (trả hàng) |
| `customer_id` | INT | FK -> customer(cus_id) | Liên kết đến khách hàng (cho đơn SALE) |
| `branch_id` | INT | FK -> branch(branch_id) | Chi nhánh nơi đơn hàng được tạo/xử lý |
| `supplier_id` | INT | FK -> supplier(supplier_id) | Nhà cung cấp (cho đơn PURCHASE) |
| `emp_id` | INT | FK -> employee(emp_id) | Nhân viên tạo/xử lý đơn hàng |
| `voucher_id` | INT | FK -> voucher(voucher_id) | Voucher được áp dụng cho đơn hàng (nếu có) |
| `warehouse_id` | INT | FK -> warehouse(warehouse_id) | Kho hàng liên quan đến đơn hàng |
| `subtotal` | DECIMAL(18,2) | DEFAULT 0 | Tổng tiền hàng trước khi áp dụng giảm giá |
| `discount_amount` | DECIMAL(18,2) | DEFAULT 0 | Số tiền được giảm (từ voucher hoặc khuyến mãi khác) |
| `total_amount` | DECIMAL(18,2) | DEFAULT 0 | Tổng tiền khách hàng phải trả (subtotal - discount_amount) |
| `payment_method` | NVARCHAR(50) | | Phương thức thanh toán: CASH, CARD, TRANSFER, VNPAY, MOMO |
| `status` | NVARCHAR(30) | DEFAULT 'PENDING' | Trạng thái đơn hàng: PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm đơn hàng được tạo |

**Khóa chính:** `order_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `customer_id` | `customer(cus_id)` | SET NULL | NO ACTION |
| `branch_id` | `branch(branch_id)` | SET NULL | NO ACTION |
| `supplier_id` | `supplier(supplier_id)` | SET NULL | NO ACTION |
| `emp_id` | `employee(emp_id)` | SET NULL | NO ACTION |
| `voucher_id` | `voucher(voucher_id)` | SET NULL | NO ACTION |
| `warehouse_id` | `warehouse(warehouse_id)` | SET NULL | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `order_id`
- Chỉ mục unique trên `order_code`
- Khóa ngoại trên `customer_id`, `branch_id`, `emp_id`, `voucher_id`, `warehouse_id` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- Tên bảng sử dụng dấu ngoặc vuông `[order]` vì `ORDER` là từ khóa trong SQL
- Nhiều trường có thể là NULL tùy thuộc vào loại đơn hàng (ví dụ: customer_id chỉ có giá trị cho đơn SALE)
- Bảng chi tiết đơn hàng `order_detail` chứa các sản phẩm trong đơn
- Trạng thái đơn hàng được quản lý theo workflow: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED

---

### 3.2. Bảng `order_detail`

**Mục đích:** Lưu trữ chi tiết các sản phẩm trong mỗi đơn hàng. Mỗi bản ghi đại diện cho một sản phẩm cụ thể trong đơn hàng, bao gồm số lượng, đơn giá và thành tiền.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `order_detail_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi dòng chi tiết |
| `order_id` | INT | FK -> [order](order_id), NOT NULL | Liên kết đến đơn hàng cha |
| `product_id` | INT | FK -> product(product_id), NOT NULL | Liên kết đến sản phẩm được đặt mua |
| `quantity` | INT | DEFAULT 1 | Số lượng sản phẩm trong đơn |
| `unit_price` | DECIMAL(18,2) | DEFAULT 0 | Đơn giá tại thời điểm đặt hàng |
| `total_price` | DECIMAL(18,2) | DEFAULT 0 | Thành tiền (quantity * unit_price) |

**Khóa chính:** `order_detail_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `order_id` | `[order](order_id)` | CASCADE | NO ACTION |
| `product_id` | `product(product_id)` | NO ACTION | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `order_detail_id`
- Khóa ngoại trên `order_id` tự động tạo chỉ mục non-clustered
- Khóa ngoại trên `product_id` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- Khi xóa đơn hàng, các chi tiết tương ứng cũng được xóa tự động (CASCADE)
- `unit_price` được lưu tại thời điểm tạo đơn để đảm bảo lịch sử giá chính xác
- Tổng của `total_price` trong tất cả chi tiết của một đơn hàng phải bằng `subtotal` trong bảng `[order]`

---

### 3.3. Bảng `payment`

**Mục đích:** Lưu trữ thông tin thanh toán cho các đơn hàng. Mỗi đơn hàng có thể có một hoặc nhiều bản ghi thanh toán (trong trường hợp thanh toán nhiều lần hoặc kết hợp nhiều phương thức).

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `payment_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi giao dịch thanh toán |
| `order_id` | INT | FK -> [order](order_id), NOT NULL | Liên kết đến đơn hàng được thanh toán |
| `payment_method` | NVARCHAR(50) | | Phương thức thanh toán: CASH, CARD, BANK_TRANSFER, VNPAY, MOMO, ZALOPAY |
| `payment_amount` | DECIMAL(18,2) | DEFAULT 0 | Số tiền thanh toán trong giao dịch này |
| `payment_date` | DATETIME | DEFAULT GETDATE() | Thời điểm giao dịch thanh toán được thực hiện |
| `payment_status` | NVARCHAR(30) | | Trạng thái thanh toán: PENDING, COMPLETED, FAILED, REFUNDED |
| `transaction_code` | NVARCHAR(100) | | Mã giao dịch từ cổng thanh toán hoặc ngân hàng |

**Khóa chính:** `payment_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `order_id` | `[order](order_id)` | CASCADE | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `payment_id`
- Khóa ngoại trên `order_id` tự động tạo chỉ mục non-clustered
- Chỉ mục trên `transaction_code` (nếu cần tìm kiếm theo mã giao dịch)

**Ghi chú:**
- Một đơn hàng có thể có nhiều bản ghi thanh toán (ví dụ: trả trước 30%, còn lại khi nhận hàng)
- Tổng `payment_amount` của tất cả các bản ghi thanh toán cho một đơn hàng phải bằng `total_amount` của đơn hàng đó
- `transaction_code` hỗ trợ đối soát với cổng thanh toán (VNPAY, MOMO, ZaloPay)

---

### 3.4. Bảng `point_transaction`

**Mục đích:** Lưu trữ lịch sử giao dịch điểm thưởng của khách hàng. Mỗi bản ghi đại diện cho một lần tích hoặc đổi điểm, phục vụ cho việc theo dõi và kiểm toán điểm thưởng.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `point_transaction_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi giao dịch điểm |
| `cus_point_id` | INT | FK -> customer_point(cus_point_id), NOT NULL | Liên kết đến bản ghi điểm thưởng của khách hàng |
| `order_id` | INT | FK -> [order](order_id) | Liên kết đến đơn hàng liên quan (nếu có) |
| `before_points` | INT | DEFAULT 0 | Số điểm của khách hàng trước khi thực hiện giao dịch này |
| `after_points` | INT | DEFAULT 0 | Số điểm của khách hàng sau khi thực hiện giao dịch này |
| `description` | NVARCHAR(255) | | Mô tả giao dịch điểm (ví dụ: "Tích điểm từ đơn ORD-20260621-001", "Đổi 1000 điểm lấy voucher") |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm giao dịch điểm được thực hiện |

**Khóa chính:** `point_transaction_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `cus_point_id` | `customer_point(cus_point_id)` | CASCADE | NO ACTION |
| `order_id` | `[order](order_id)` | SET NULL | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `point_transaction_id`
- Khóa ngoại trên `cus_point_id` tự động tạo chỉ mục non-clustered
- Khóa ngoại trên `order_id` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- `before_points` và `after_points` lưu trữ trạng thái điểm tại thời điểm trước/sau giao dịch để hỗ trợ kiểm toán
- Hiệu số `after_points - before_points` cho biết số điểm được cộng (dương) hoặc trừ (âm) trong giao dịch
- Bảng này cho phép khôi phục số điểm chính xác tại bất kỳ thời điểm nào

---

## 4. Nhóm Warehouse & Stock

Nhóm này bao gồm các bảng phục vụ cho quản lý kho hàng, sản phẩm và các giao dịch tồn kho.

### 4.1. Bảng `warehouse`

**Mục đích:** Lưu trữ thông tin về các kho hàng trong hệ thống. Mỗi chi nhánh có thể có một hoặc nhiều kho hàng để quản lý tồn kho theo vị trí.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `warehouse_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi kho |
| `warehouse_name` | NVARCHAR(150) | NOT NULL | Tên kho hàng (ví dụ: Kho chính Quận 1, Kho phụ Quận 5) |
| `branch_id` | INT | FK -> branch(branch_id), NOT NULL | Liên kết đến chi nhánh sở hữu kho hàng này |
| `address` | NVARCHAR(300) | | Địa chỉ vị trí của kho hàng |
| `status` | NVARCHAR(20) | DEFAULT 'active' | Trạng thái kho: 'active' (đang hoạt động) hoặc 'inactive' (tạm ngưng) |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo |

**Khóa chính:** `warehouse_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `branch_id` | `branch(branch_id)` | NO ACTION | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `warehouse_id`
- Khóa ngoại trên `branch_id` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- Mỗi chi nhánh có thể có nhiều kho, nhưng mỗi kho chỉ thuộc về một chi nhánh
- Kho hàng được sử dụng trong các giao dịch nhập/xuất kho và chuyển kho

---

### 4.2. Bảng `unit`

**Mục đích:** Lưu trữ thông tin về các đơn vị tính của sản phẩm (cái, kg, lít, mét,...). Bảng này phục vụ cho việc chuẩn hóa đơn vị tính trong hệ thống.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `unit_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi đơn vị tính |
| `unit_name` | NVARCHAR(50) | NOT NULL | Tên đơn vị tính (ví dụ: Cái, Kg, Lít, Mét, Gói, Hộp) |
| `description` | NVARCHAR(255) | | Mô tả chi tiết hoặc ký hiệu viết tắt của đơn vị |

**Khóa chính:** `unit_id`

**Khóa ngoại:** Không có

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `unit_id`

**Ghi chú:**
- Bảng này là danh mục tĩnh, thường được seed dữ liệu ban đầu và ít khi thay đổi
- Có thể thêm mới đơn vị tính khi hệ thống mở rộng loại sản phẩm

---

### 4.3. Bảng `category`

**Mục đích:** Lưu trữ thông tin về danh mục sản phẩm với cấu trúc phân cấp (hierarchical). Mỗi danh mục có thể có một danh mục cha, cho phép xây dựng cây danh mục đa cấp (ví dụ: Thực phẩm -> Đồ uống -> Nước giải khát).

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `category_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi danh mục |
| `category_name` | NVARCHAR(150) | NOT NULL | Tên danh mục sản phẩm |
| `description` | NVARCHAR(255) | | Mô tả chi tiết về danh mục |
| `parent_category_id` | INT | FK -> category(category_id) | Liên kết đến danh mục cha (NULL nếu là danh mục gốc) |
| `status` | NVARCHAR(20) | DEFAULT 'active' | Trạng thái danh mục: 'active' hoặc 'inactive' |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo |
| `update_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `category_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `parent_category_id` | `category(category_id)` | SET NULL | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `category_id`
- Khóa ngoại trên `parent_category_id` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- Đây là bảng tự tham chiếu (self-referential): `parent_category_id` trỏ đến khóa chính của chính bảng đó
- Danh mục gốc (root category) có `parent_category_id = NULL`
- Hệ thống cần kiểm tra chu trình (cycle) khi cập nhật danh mục cha để tránh vòng lặp vô hạn
- Có thể sử dụng Common Table Expression (CTE) trong SQL Server để truy vấn cây danh mục

---

### 4.4. Bảng `product`

**Mục đích:** Lưu trữ thông tin về các sản phẩm trong hệ thống. Đây là bảng trung tâm cho quản lý sản phẩm, liên kết với danh mục, đơn vị tính và chứa thông tin giá cơ bản.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `product_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi sản phẩm |
| `product_name` | NVARCHAR(200) | NOT NULL | Tên sản phẩm |
| `quantity` | INT | DEFAULT 0 | Tổng số lượng sản phẩm trong tất cả các kho |
| `category_id` | INT | FK -> category(category_id) | Liên kết đến danh mục sản phẩm |
| `unit_id` | INT | FK -> unit(unit_id) | Liên kết đến đơn vị tính của sản phẩm |
| `selling_price` | DECIMAL(18,2) | DEFAULT 0 | Giá bán của sản phẩm |
| `status` | NVARCHAR(20) | DEFAULT 'active' | Trạng thái sản phẩm: 'active' hoặc 'inactive' |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được tạo |
| `update_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `product_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `category_id` | `category(category_id)` | SET NULL | NO ACTION |
| `unit_id` | `unit(unit_id)` | SET NULL | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `product_id`
- Khóa ngoại trên `category_id` tự động tạo chỉ mục non-clustered
- Khóa ngoại trên `unit_id` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- `quantity` là tổng hợp từ bảng `inventory`, có thể không được đồng bộ nếu có lỗi
- `selling_price` là giá bán mặc định; giá thực tế trong đơn hàng có thể khác do khuyến mãi
- Bảng này chỉ lưu thông tin cơ bản của sản phẩm; tồn kho chi tiết theo kho được quản lý trong bảng `inventory`

---

### 4.5. Bảng `inventory`

**Mục đích:** Lưu trữ thông tin tồn kho chi tiết theo từng sản phẩm và từng kho hàng. Bảng này cho phép theo dõi chính xác số lượng tồn kho của mỗi sản phẩm tại mỗi vị trí kho.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `inventory_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi bản ghi tồn kho |
| `warehouse_id` | INT | FK -> warehouse(warehouse_id), NOT NULL | Liên kết đến kho hàng |
| `product_id` | INT | FK -> product(product_id), NOT NULL | Liên kết đến sản phẩm |
| `quantity_in_stock` | INT | DEFAULT 0 | Số lượng sản phẩm hiện có trong kho này |
| `updated_at` | DATETIME | DEFAULT GETDATE() | Thời điểm bản ghi được cập nhật gần nhất |

**Khóa chính:** `inventory_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `warehouse_id` | `warehouse(warehouse_id)` | CASCADE | NO ACTION |
| `product_id` | `product(product_id)` | CASCADE | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `inventory_id`
- Khóa ngoại trên `warehouse_id` tự động tạo chỉ mục non-clustered
- Khóa ngoại trên `product_id` tự động tạo chỉ mục non-clustered
- Nên có chỉ mục unique trên cặp (`warehouse_id`, `product_id`) để đảm bảo mỗi kho chỉ có một bản ghi cho mỗi sản phẩm

**Ghi chú:**
- Cặp (`warehouse_id`, `product_id`) xác định duy nhất một bản ghi tồn kho
- Khi xóa kho hoặc sản phẩm, các bản ghi tồn kho tương ứng cũng bị xóa (CASCADE)
- Tổng `quantity_in_stock` của một sản phẩm trên tất cả các kho phải bằng `quantity` trong bảng `product`

---

### 4.6. Bảng `stock_transfer`

**Mục đích:** Lưu trữ thông tin về các phiếu chuyển kho, ghi nhận việc di chuyển hàng hóa từ kho này sang kho khác trong hệ thống.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `stock_transfer_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng, định danh duy nhất cho mỗi phiếu chuyển kho |
| `from_warehouse_id` | INT | FK -> warehouse(warehouse_id), NOT NULL | Liên kết đến kho nguồn (nơi hàng được chuyển đi) |
| `to_warehouse_id` | INT | FK -> warehouse(warehouse_id), NOT NULL | Liên kết đến kho đích (nơi hàng được chuyển đến) |
| `transfer_code` | NVARCHAR(50) | | Mã phiếu chuyển kho, duy nhất theo định dạng: TRF-YYYYMMDD-XXXXX |
| `transfer_date` | DATETIME | DEFAULT GETDATE() | Thời điểm thực hiện chuyển kho |
| `status` | NVARCHAR(30) | | Trạng thái phiếu chuyển: PENDING, IN_TRANSIT, COMPLETED, CANCELLED |
| `note` | NVARCHAR(500) | | Ghi chú bổ sung về lý do chuyển kho hoặc thông tin khác |
| `created_by` | INT | FK -> employee(emp_id) | Nhân viên tạo phiếu chuyển kho |

**Khóa chính:** `stock_transfer_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `from_warehouse_id` | `warehouse(warehouse_id)` | NO ACTION | NO ACTION |
| `to_warehouse_id` | `warehouse(warehouse_id)` | NO ACTION | NO ACTION |
| `created_by` | `employee(emp_id)` | SET NULL | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `stock_transfer_id`
- Khóa ngoại trên `from_warehouse_id` tự động tạo chỉ mục non-clustered
- Khóa ngoại trên `to_warehouse_id` tự động tạo chỉ mục non-clustered
- Khóa ngoại trên `created_by` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- `from_warehouse_id` và `to_warehouse_id` có thể trỏ đến cùng một kho trong trường hợp điều chỉnh nội bộ
- Chi tiết các sản phẩm được chuyển được lưu trong bảng `stock_transfer_detail`
- Trạng thái PENDING cho phép lưu nháp trước khi xác nhận chuyển kho

---

### 4.7. Bảng `stock_transfer_detail`

**Mục đích:** Lưu trữ chi tiết các sản phẩm trong mỗi phiếu chuyển kho. Mỗi bản ghi đại diện cho một sản phẩm cụ thể và số lượng được chuyển.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `stock_transfer_detail_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng |
| `stock_transfer_id` | INT | FK -> stock_transfer(stock_transfer_id), NOT NULL | Liên kết đến phiếu chuyển kho cha |
| `product_id` | INT | FK -> product(product_id), NOT NULL | Liên kết đến sản phẩm được chuyển |
| `quantity` | INT | DEFAULT 0 | Số lượng sản phẩm được chuyển |

**Khóa chính:** `stock_transfer_detail_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `stock_transfer_id` | `stock_transfer(stock_transfer_id)` | CASCADE | NO ACTION |
| `product_id` | `product(product_id)` | NO ACTION | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `stock_transfer_detail_id`
- Khóa ngoại trên `stock_transfer_id` tự động tạo chỉ mục non-clustered
- Khóa ngoại trên `product_id` tự động tạo chỉ mục non-clustered

**Ghi chú:**
- Khi xóa phiếu chuyển kho, các chi tiết tương ứng cũng được xóa (CASCADE)
- Số lượng `quantity` phải được kiểm tra với tồn kho thực tế của kho nguồn trước khi xác nhận

---

### 4.8. Bảng `stock_transaction`

**Mục đích:** Lưu trữ lịch sử tất cả các giao dịch tồn kho trong hệ thống, bao gồm nhập kho, xuất kho, điều chỉnh. Bảng này phục vụ cho việc kiểm toán và theo dõi biến động tồn kho.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `stock_transaction_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng |
| `warehouse_id` | INT | FK -> warehouse(warehouse_id), NOT NULL | Liên kết đến kho nơi giao dịch diễn ra |
| `product_id` | INT | FK -> product(product_id), NOT NULL | Liên kết đến sản phẩm được giao dịch |
| `reference_type` | NVARCHAR(50) | | Loại tham chiếu: ORDER, PURCHASE, RETURN, TRANSFER, ADJUSTMENT |
| `reference_id` | INT | | ID của bản ghi tham chiếu (ví dụ: order_id nếu reference_type = ORDER) |
| `transaction_type` | NVARCHAR(20) | | Loại giao dịch: IN (nhập kho) hoặc OUT (xuất kho) |
| `quantity` | INT | DEFAULT 0 | Số lượng sản phẩm trong giao dịch |
| `before_quantity` | INT | DEFAULT 0 | Số lượng tồn kho trước khi giao dịch |
| `after_quantity` | INT | DEFAULT 0 | Số lượng tồn kho sau khi giao dịch |
| `note` | NVARCHAR(500) | | Ghi chú bổ sung về giao dịch |
| `created_by` | INT | FK -> employee(emp_id) | Nhân viên thực hiện giao dịch |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm giao dịch được tạo |

**Khóa chính:** `stock_transaction_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `warehouse_id` | `warehouse(warehouse_id)` | NO ACTION | NO ACTION |
| `product_id` | `product(product_id)` | NO ACTION | NO ACTION |
| `created_by` | `employee(emp_id)` | SET NULL | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `stock_transaction_id`
- Khóa ngoại trên `warehouse_id` tự động tạo chỉ mục non-clustered
- Khóa ngoại trên `product_id` tự động tạo chỉ mục non-clustered
- Chỉ mục trên (`warehouse_id`, `product_id`, `created_at`) cho truy vấn lịch sử tồn kho

**Ghi chú:**
- `before_quantity` và `after_quantity` lưu trữ trạng thái tồn kho trước/sau giao dịch để hỗ trợ kiểm toán
- Hiệu số `after_quantity - before_quantity` phải bằng `quantity` với dấu phù hợp (dương cho IN, âm cho OUT)
- Bảng này là append-only, không nên cập nhật hoặc xóa các bản ghi đã tồn tại
- `reference_type` và `reference_id` cho phép liên kết ngược đến nguồn gốc giao dịch

---

## 5. Nhóm System

Nhóm này bao gồm bảng phục vụ cho việc ghi nhận và kiểm toán hệ thống.

### 5.1. Bảng `audit_log`

**Mục đích:** Lưu trữ nhật ký kiểm toán (audit log) ghi lại tất cả các thay đổi quan trọng trong hệ thống. Bảng này phục vụ cho việc theo dõi hoạt động của người dùng, khắc phục sự cố và tuân thủ quy định.

**Cấu trúc:**

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---------|--------------|-----------|-------|
| `audit_log_id` | INT | PK, IDENTITY(1,1), NOT NULL | Khóa chính tự động tăng |
| `emp_id` | INT | FK -> employee(emp_id) | Nhân viên thực hiện hành động |
| `action_name` | NVARCHAR(100) | | Tên hành động (ví dụ: INSERT, UPDATE, DELETE, LOGIN, LOGOUT) |
| `table_name` | NVARCHAR(100) | | Tên bảng bị tác động (ví dụ: customer, product, [order]) |
| `record_id` | INT | | ID của bản ghi bị tác động |
| `old_data` | NVARCHAR(MAX) | | Dữ liệu trước khi thay đổi (dạng JSON hoặc serialized) |
| `new_data` | NVARCHAR(MAX) | | Dữ liệu sau khi thay đổi (dạng JSON hoặc serialized) |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm hành động được thực hiện |

**Khóa chính:** `audit_log_id`

**Khóa ngoại:**

| Cột | Tham chiếu đến | ON DELETE | ON UPDATE |
|-----|---------------|-----------|-----------|
| `emp_id` | `employee(emp_id)` | SET NULL | NO ACTION |

**Chỉ mục:**
- Khóa chính tự động tạo chỉ mục clustered trên `audit_log_id`
- Khóa ngoại trên `emp_id` tự động tạo chỉ mục non-clustered
- Chỉ mục trên (`table_name`, `record_id`, `created_at`) cho truy vấn theo bảng và bản ghi
- Chỉ mục trên `created_at` cho truy vấn theo khoảng thời gian

**Ghi chú:**
- Bảng này là append-only, không nên cập nhật hoặc xóa các bản ghi
- `old_data` và `new_data` thường được lưu dưới dạng JSON để dễ dàng đọc và parse
- Với các bản ghi lớn (NVARCHAR(MAX)), nên cân nhắc lưu trữ dữ liệu thay đổi ngắn gọn hoặc sử dụng bảng riêng cho log chi tiết
- Hệ thống nên có cơ chế tự động ghi log thông qua trigger hoặc application-level interceptor

---

## Tổng Kết

Cơ sở dữ liệu DBFinoraV2 bao gồm 21 bảng được tổ chức thành 5 nhóm chức năng:

| Nhóm | Số bảng | Các bảng |
|------|---------|----------|
| **Identity & Access** | 4 | role, branch, employee, employee_role |
| **Business Partners** | 4 | customer, customer_point, voucher, supplier |
| **Commerce** | 4 | [order], order_detail, payment, point_transaction |
| **Warehouse & Stock** | 8 | warehouse, unit, category, product, inventory, stock_transfer, stock_transfer_detail, stock_transaction |
| **System** | 1 | audit_log |

**Quan hệ đặc biệt:**
- Bảng `employee_role` là bảng trung gian thực hiện quan hệ nhiều-nhiều giữa `employee` và `role`
- Bảng `category` có quan hệ tự tham chiếu (self-referential) qua `parent_category_id`
- Bảng `[order]` là bảng trung tâm của hệ thống thương mại với 6 khóa ngoại

**Kiểu dữ liệu phổ biến:**
- `INT IDENTITY(1,1)` cho khóa chính tự động tăng
- `NVARCHAR(n)` cho văn bản hỗ trợ tiếng Việt
- `DECIMAL(18,2)` cho số tiền và giá cả
- `DATETIME` cho thời điểm tạo/cập nhật
- `DATE` cho ngày tháng đơn thuần

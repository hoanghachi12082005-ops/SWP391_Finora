# Tổng Quan Lược Đồ Cơ Sở Dữ Liệu DBFinoraV3

## 1. Giới Thiệu

Cơ sở dữ liệu **DBFinoraV3** là hệ thống lưu trữ dữ liệu chính của dự án **FinoraRetail** (tên viết tắt: Finora), một ứng dụng quản lý bán lẻ được phát triển trên nền tảng Java Web với Apache Tomcat 10.1, sử dụng Jakarta Servlet/JSP API. Hệ thống được thiết kế để hỗ trợ hoạt động kinh doanh bán lẻ đa chi nhánh, bao gồm quản lý nhân viên, khách hàng, sản phẩm, kho hàng, đơn hàng, thanh toán và các nghiệp vụ liên quan đến điểm thưởng khách hàng.

Cơ sở dữ liệu được triển khai trên **Microsoft SQL Server**, tận dụng các tính năng như IDENTITY cho auto-increment, CHECK constraint cho ràng buộc nghiệp vụ, và NVARCHAR cho hỗ trợ đầy đủ ký tự tiếng Việt. Toàn bộ 21 bảng trong hệ thống được tổ chức theo 5 nhóm chức năng chính, phản ánh các lĩnh vực nghiệp vụ khác nhau của một doanh nghiệp bán lẻ.

## 2. Thông Tin Hệ Thống

| Thuộc tính | Giá trị |
|------------|---------|
| Tên cơ sở dữ liệu | DBFinoraV3 |
| Hệ quản trị CSDL | Microsoft SQL Server |
| Ngôn ngữ lập trình | Java (JDK 17) |
| Nền tảng runtime | Apache Tomcat 10.1 |
| Jakarta API | Servlet 5.0 / JSP 3.0 |
| Số lượng bảng | 21 |
| Số lượng nhóm chức năng | 5 |

## 3. Quy Ước Đặt Tên

Hệ thống tuân thủ một bộ quy ước đặt tên thống nhất nhằm đảm bảo tính nhất quán và dễ đọc trong toàn bộ cơ sở dữ liệu.

### 3.1. Quy Ước Tên Bảng

Tên bảng sử dụng định dạng **PascalCase**, viết hoa chữ cái đầu tiên của mỗi từ và không sử dụng ký tự gạch dưới. Ví dụ: `Customer`, `OrderDetail`, `StockTransfer`. Riêng bảng `order` được đặt trong cặp dấu ngoặc vuông `[order]` vì `ORDER` là từ khóa dành riêng của ngôn ngữ SQL.

### 3.2. Quy Ước Tên Cột

Tên cột sử dụng định dạng **snake_case**, với toàn bộ ký tự in thường và các từ được phân tách bằng dấu gạch dưới. Ví dụ: `customer_id`, `created_at`, `payment_method`. Định dạng này giúp phân biệt rõ ràng giữa tên bảng và tên cột trong câu truy vấn SQL.

### 3.3. Kiểu Dữ Liệu Văn Bản

Toàn bộ các trường văn bản trong cơ sở dữ liệu sử dụng kiểu **NVARCHAR** thay vì VARCHAR thông thường. NVARCHAR (Unicode VARCHAR) hỗ trợ đầy đủ bộ ký tự Unicode, đặc biệt cần thiết cho việc lưu trữ tiếng Việt có dấu với đầy đủ nguyên âm (ă, â, đ, ê, ô, ơ, ư) và thanh dấu. Kích thước NVARCHAR được chọn phù hợp với từng loại nội dung: NVARCHAR(10) cho các trường ngắn như giới tính, NVARCHAR(300) cho địa chỉ, NVARCHAR(MAX) cho các trường có thể chứa lượng dữ liệu lớn như old_data và new_data trong audit_log.

### 3.4. Kiểu Dữ Liệu Thời Gian

Các trường thời gian sử dụng kiểu **DATETIME** cho các trường hợp cần lưu cả ngày và giờ, và **DATE** cho các trường chỉ cần lưu ngày tháng. Hầu hết các bảng đều có cặp trường `created_at` và `updated_at` (hoặc `update_at` tùy bảng) để theo dõi thời điểm tạo mới và cập nhật cuối cùng của mỗi bản ghi.

## 4. Tổ Chức Bảng Theo Nhóm Chức Năng

Cơ sở dữ liệu DBFinoraV3 bao gồm 21 bảng, được phân thành 5 nhóm chức năng chính. Việc phân nhóm này phản ánh các lĩnh vực nghiệp vụ riêng biệt trong hệ thống quản lý bán lẻ và giúp việc bảo trì, tra cứu trở nên dễ dàng hơn.

### 4.1. Nhóm 1: Identity & Access (3 bảng)

Nhóm này chứa các bảng liên quan đến hệ thống nhận dạng và phân quyền người dùng, bao gồm quản lý vai trò và nhân viên.

| Tên bảng | Mô tả |
|----------|-------|
| `role` | Lưu trữ danh sách các vai trò trong hệ thống (ví dụ: Quản lý, Thu ngân, Kho) |
| `employee` | Lưu trữ thông tin nhân viên, bao gồm thông tin cá nhân và thông tin đăng nhập |
| `employee_role` | Bảng trung gian thiết lập quan hệ nhiều-nhiều giữa employee và role |

Nhóm Identity & Access là nền tảng cho hệ thống bảo mật, kiểm soát truy cập và phân quyền người dùng. Mỗi nhân viên có thể đảm nhận nhiều vai trò khác nhau thông qua bảng trung gian employee_role.

### 4.2. Nhóm 2: Business Partners (2 bảng)

Nhóm này quản lý thông tin khách hàng và chương trình tích điểm thưởng.

| Tên bảng | Mô tả |
|----------|-------|
| `customer` | Lưu trữ thông tin khách hàng, bao gồm thông tin liên hệ và lịch sử mua hàng |
| `customer_point` | Lưu trữ điểm thưởng hiện tại khả dụng của từng khách hàng |

Hệ thống điểm thưởng được thiết kế theo mô hình một-một với bảng customer, mỗi khách hàng có duy nhất một bản ghi điểm tương ứng trong bảng customer_point.

### 4.3. Nhóm 3: Commerce (9 bảng)

Đây là nhóm lớn nhất, chứa các bảng liên quan đến hoạt động thương mại từ quản lý nhà cung cấp đến xử lý thanh toán.

| Tên bảng | Mô tả |
|----------|-------|
| `supplier` | Lưu trữ thông tin nhà cung cấp sản phẩm |
| `category` | Lưu trữ danh mục sản phẩm với cấu trúc phân cấp cha-con |
| `product` | Lưu trữ thông tin sản phẩm |
| `unit` | Lưu trữ đơn vị tính của sản phẩm (cái, kg, lít,...) |
| `[order]` | Lưu trữ thông tin đơn hàng (bán hàng và nhập hàng) |
| `order_detail` | Lưu trữ chi tiết các sản phẩm trong mỗi đơn hàng |
| `payment` | Lưu trữ thông tin thanh toán của đơn hàng |
| `point_transaction` | Lưu trữ lịch sử giao dịch điểm thưởng |

Nhóm Commerce phản ánh luồng nghiệp vụ cốt lõi của hệ thống bán lẻ, từ quản lý sản phẩm đến xử lý đơn hàng và thanh toán.

### 4.4. Nhóm 4: Warehouse & Stock (6 bảng)

Nhóm này quản lý hệ thống kho hàng và các hoạt động liên quan đến tồn kho.

| Tên bảng | Mô tả |
|----------|-------|
| `branch` | Lưu trữ thông tin các chi nhánh cửa hàng |
| `warehouse` | Lưu trữ thông tin kho hàng tại mỗi chi nhánh |
| `inventory` | Lưu trữ số lượng tồn kho của từng sản phẩm tại từng kho |
| `stock_transfer` | Lưu trữ thông tin phiếu chuyển kho giữa các kho |
| `stock_transfer_detail` | Lưu trữ chi tiết các sản phẩm trong mỗi phiếu chuyển kho |
| `stock_transaction` | Lưu trữ lịch sử các giao dịch nhập/xuất kho |

Hệ thống kho hàng được thiết kế theo mô hình đa kho, mỗi chi nhánh có thể có một hoặc nhiều kho hàng riêng.

### 4.5. Nhóm 5: System (2 bảng)

Nhóm này chứa các bảng phục vụ mục đích hệ thống và giám sát.

| Tên bảng | Mô tả |
|----------|-------|
| `audit_log` | Lưu trữ nhật ký giám sát các thao tác thay đổi dữ liệu |
| `unit` | (Đã liệt kê ở nhóm Commerce - đơn vị tính sản phẩm) |

Bảng audit_log đóng vai trò quan trọng trong việc đảm bảo tính minh bạch và truy xuất nguồn gốc dữ liệu.

## 5. Tổng Quan Quan Hệ Giữa Các Bảng

Cơ sở dữ liệu DBFinoraV3 được thiết kế với nhiều loại quan hệ khác nhau, phản ánh các ràng buộc nghiệp vụ thực tế của hệ thống bán lẻ.

### 5.1. Quan Hệ Một-Nhiều (One-to-Many)

Đây là loại quan hệ phổ biến nhất trong hệ thống. Một số ví dụ điển hình:

- **branch** (1) → **employee** (N): Mỗi chi nhánh có thể có nhiều nhân viên, nhưng mỗi nhân viên chỉ thuộc về một chi nhánh.
- **branch** (1) → **warehouse** (N): Mỗi chi nhánh có thể quản lý nhiều kho hàng.
- **customer** (1) → **customer_point** (1): Mỗi khách hàng có duy nhất một hồ sơ điểm thưởng.
- **customer** (1) → **[order]** (N): Mỗi khách hàng có thể có nhiều đơn hàng.
- **category** (1) → **product** (N): Mỗi danh mục có thể chứa nhiều sản phẩm.
- **warehouse** (1) → **inventory** (N): Mỗi kho hàng theo dõi tồn kho của nhiều sản phẩm.
- **[order]** (1) → **order_detail** (N): Mỗi đơn hàng có thể chứa nhiều chi tiết sản phẩm.
- **[order]** (1) → **payment** (N): Mỗi đơn hàng có thể có nhiều giao dịch thanh toán.
- **stock_transfer** (1) → **stock_transfer_detail** (N): Mỗi phiếu chuyển kho có thể chứa nhiều sản phẩm.

### 5.2. Quan Hệ Nhiều-Nhiều (Many-to-Many)

Quan hệ nhiều-nhiều được thiết lập thông qua các bảng trung gian:

- **employee** (N) ↔ **role** (M): Mỗi nhân viên có thể đảm nhận nhiều vai trò, và mỗi vai trò có thể được gán cho nhiều nhân viên. Bảng trung gian **employee_role** lưu trữ các cặp emp_id và role_id cùng với thời điểm gán vai trò.

### 5.3. Quan Hệ Tự Tham Chiếu (Self-Referential)

Bảng **category** có quan hệ tự tham chiếu thông qua cột `parent_category_id`, cho phép thiết lập cấu trúc phân cấp cha-con giữa các danh mục. Ví dụ: Danh mục "Thực phẩm" có thể chứa các danh mục con như "Đồ uống", "Bánh kẹo", mỗi danh mục con lại có thể có danh mục con khác. Điều này hỗ trợ phân loại sản phẩm đa cấp, phù hợp với nhu cầu của các doanh nghiệp bán lẻ có danh mục sản phẩm phức tạp.

### 5.4. Quan Hệ Một-Một (One-to-One)

Bảng **customer** và **customer_point** có quan hệ một-một, mỗi khách hàng có duy nhất một bản ghi điểm thưởng. Thiết kế này tách biệt thông tin cá nhân của khách hàng với thông tin chương trình tích điểm, giúp quản lý và mở rộng tính năng dễ dàng hơn.

### 5.5. Quan Hệ Chuyển Kho

Bảng **stock_transfer** có hai khóa ngoại cùng trỏ đến bảng **warehouse**: `from_warehouse_id` (kho nguồn) và `to_warehouse_id` (kho đích). Điều này cho phép hệ thống theo dõi các phiếu chuyển kho nội bộ giữa các chi nhánh.

## 6. Các Ràng Buộc Nghiệp Vụ

Cơ sở dữ liệu sử dụng CHECK constraint để thực thi một số quy tắc nghiệp vụ ở cấp độ database:

- **branch.status**: Chỉ chấp nhận giá trị 'active' hoặc 'locked', đảm bảo trạng thái chi nhánh luôn nằm trong danh sách hợp lệ.
- **employee.status**: Chỉ chấp nhận 'ACTIVE', 'INACTIVE', hoặc 'ON_LEAVE', phản ánh ba trạng thái làm việc của nhân viên.


SQL Server mặc định sử dụng hành vi NO ACTION cho các khóa ngoại, nghĩa là không thể xóa hoặc cập nhật bản ghi cha nếu có bản ghi con tham chiếu đến nó. Điều này đảm bảo tính toàn vẹn tham chiếu của dữ liệu.

## 7. Mô Hình Dữ Liệu Tổng Quan

Dưới đây là sơ đồ mô tả mối quan hệ logic giữa các nhóm chức năng:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            DBFinoraV3 - Database Schema                         │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────────┐           ┌─────────────────────┐                       │
│  │ Identity & Access   │           │  Business Partners  │                       │
│  ├─────────────────────┤           ├─────────────────────┤                       │
│  │ role                │           │ customer            │───┐                   │
│  │ employee            │           │ customer_point      │◄──┘                   │
│  │ employee_role       │           └─────────────────────┘    (1:1)               │
│  └──────────┬──────────┘                                                        │
│             │ (1:N)                                                              │
│  ┌──────────┴──────────────────────────────────────────────────────────────────┐ │
│  │                              Commerce                                        │ │
│  ├──────────────────────────────────────────────────────────────────────────────┤ │
│  │ supplier          category                                                      │ │
│  │      │                                        │ ◄──┐ (self-ref)                  │ │
│  │      │                                        │     │ parent_category_id         │ │
│  │      └────────┬───────────────────────────────┘                             │ │
│  │               │                                 │──── product                  │ │
│  │               │                              │      │── unit                  │ │
│  │               │                              │      │                        │ │
│  │               ▼                              │      ▼                        │ │
│  │         [order]◄─────────────────────────────┴────────────► order_detail     │ │
│  │              ││                                                      │          │ │
│  │              │└──────────► payment                                     │          │ │
│  │              │                                                          ▼          │ │
│  │              └────────────────────────────────────────────────► point_transaction │
│  └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                       │                                              │
│  ┌─────────────────────────────────────┴──────────────────────────────────────────┐ │
│  │                         Warehouse & Stock                                      │ │
│  ├──────────────────────────────────────────────────────────────────────────────────┤ │
│  │ branch                                                                         │ │
│  │    │                                                                           │ │
│  │    ├─── employee ◄───────────────────────────────────────────────────────────┐  │ │
│  │    │                                                                          │  │ │
│  │    └─── warehouse ◄─────────────────────────┐                                │  │ │
│  │                │                              │                                │  │ │
│  │                ├─── inventory ◄─────── product                                 │  │ │
│  │                │                                                               │  │ │
│  │                ├─── stock_transfer ◄──────────────┐                           │  │ │
│  │                │        │                          │                           │  │ │
│  │                │        └─── stock_transfer_detail (N products)              │  │ │
│  │                │                                                               │  │ │
│  │                └─── stock_transaction ◄───────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────────────────────────────────┘ │
│                                       │                                              │
│  ┌─────────────────────────────────────┴──────────────────────────────────────────┐ │
│  │                              System                                             │ │
│  ├──────────────────────────────────────────────────────────────────────────────────┤ │
│  │ audit_log ◄── (tracks changes across all tables)                                │ │
│  └──────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

## 8. Các Bảng Đặc Biệt

### 8.1. Bảng [order]

Bảng đơn hàng là bảng duy nhất trong hệ thống có tên trùng với từ khóa SQL. Từ khóa `ORDER` được sử dụng trong mệnh đề `ORDER BY` để sắp xếp kết quả truy vấn, do đó SQL Server yêu cầu tên bảng phải được đặt trong cặp dấu ngoặc vuông `[order]` khi tham chiếu. Việc sử dụng từ khóa làm tên bảng là một lựa chọn thiết kế phản ánh nghiệp vụ thực tế, vì "đơn hàng" là thực thể trung tâm trong hệ thống bán lẻ.

### 8.2. Bảng audit_log

Bảng audit_log được thiết kế để ghi nhận toàn bộ các thao tác thay đổi dữ liệu trong hệ thống. Với các trường như `old_data` và `new_data` sử dụng kiểu NVARCHAR(MAX), bảng này có khả năng lưu trữ các đối tượng JSON mô tả trạng thái trước và sau của bản ghi. Tính năng audit log là cần thiết cho các nghiệp vụ yêu cầu tuân thủ quy định, truy xuất nguồn gốc dữ liệu và phát hiện các thay đổi bất thường.

## 9. Chiến Lược Khóa Chính

Tất cả 21 bảng trong cơ sở dữ liệu đều sử dụng chiến lược khóa chính đơn giản với cột ID tự tăng (IDENTITY):

- Tên cột khóa chính theo quy ước: `<ten_bang>_id` (ví dụ: `emp_id`, `product_id`, `order_id`)
- Kiểu dữ liệu: INT
- Thuộc tính IDENTITY: IDENTITY(1,1), bắt đầu từ 1 và tăng 1 cho mỗi bản ghi mới
- Không có khóa chính tổng hợp (composite key) trong hệ thống

Chiến lược này đơn giản hóa việc tham chiếu giữa các bảng và phù hợp với mô hình DAO (Data Access Object) được sử dụng trong tầng persistence của ứng dụng Java.

## 10. Mối Quan Hệ Với Tầng Ứng Dụng

Cơ sở dữ liệu DBFinoraV3 là thành phần trung tâm trong kiến trúc MVC của ứng dụng FinoraRetail:

- **Controller (Servlet)**: Nhận yêu cầu HTTP, xử lý validation và điều phối luồng dữ liệu
- **DAO (Data Access Object)**: Thực hiện các thao tác CRUD với cơ sở dữ liệu thông qua JDBC
- **Model**: Các đối tượng Java ánh xạ với các bảng trong database
- **JSP View**: Hiển thị dữ liệu được truyền từ servlet thông qua request attributes

Mỗi DAO class chịu trách nhiệm quản lý một hoặc nhiều bảng liên quan, tuân thủ nguyên tắc tách biệt trách nhiệm (Separation of Concerns) trong thiết kế phần mềm.

## 11. Phụ Lục: Danh Sách Đầy Đủ 21 Bảng

| STT | Tên bảng | Nhóm chức năng | Khóa chính | Số khóa ngoại |
|-----|----------|----------------|------------|---------------|
| 1 | role | Identity & Access | role_id | 0 |
| 2 | employee | Identity & Access | emp_id | 1 |
| 3 | employee_role | Identity & Access | emp_role_id | 2 |
| 4 | customer | Business Partners | cus_id | 0 |
| 5 | customer_point | Business Partners | cus_point_id | 1 |
| 6 | supplier | Commerce | supplier_id | 0 |
| 7 | category | Commerce | category_id | 1 (tự tham chiếu) |
| 8 | product | Commerce | product_id | 2 |
| 9 | unit | Commerce | unit_id | 0 |
| 10 | [order] | Commerce | order_id | 6 |
| 11 | order_detail | Commerce | order_detail_id | 2 |
| 12 | payment | Commerce | payment_id | 1 |
| 13 | point_transaction | Commerce | point_transaction_id | 2 |
| 14 | branch | Warehouse & Stock | branch_id | 0 |
| 15 | warehouse | Warehouse & Stock | warehouse_id | 1 |
| 16 | inventory | Warehouse & Stock | inventory_id | 2 |
| 17 | stock_transfer | Warehouse & Stock | stock_transfer_id | 3 |
| 18 | stock_transfer_detail | Warehouse & Stock | stock_transfer_detail_id | 2 |
| 19 | stock_transaction | Warehouse & Stock | stock_transaction_id | 3 |
| 20 | audit_log | System | audit_log_id | 1 |

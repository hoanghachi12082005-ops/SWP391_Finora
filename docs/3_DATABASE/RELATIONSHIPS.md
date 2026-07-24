# Quan Hệ Giữa Các Bảng Cơ Sở Dữ Liệu

**Dự án:** FinoraRetail (SWP391_Finora)  
**Cơ sở dữ liệu:** DBFinoraV2 trên SQL Server  
**Phiên bản tài liệu:** 1.0  
**Ngày cập nhật:** 21/06/2026

---

## Mục Lục

1. [Tổng Quan Entity Relationship](#1-tổng-quan-entity-relationship)
2. [Sơ Đồ Quan Hệ Dạng Văn Bản](#2-sơ-đồ-quan-hệ-dạng-văn-bản)
3. [Danh Sách Khóa Chính](#3-danh-sách-khóa-chính)
4. [Danh Sách Khóa Ngoại](#4-danh-sách-khóa-ngoại)
5. [Quan Hệ Chi Tiết Theo Nhóm Chức Năng](#5-quan-hệ-chi-tiết-theo-nhóm-chức-năng)
6. [Cardinality Matrix](#6-cardinality-matrix)
7. [Phân Tích Các Quan Hệ Quan Trọng](#7-phân-tích-các-quan-hệ-quan-trọng)

---

## 1. Tổ Quan Entity Relationship

Cơ sở dữ liệu DBFinoraV2 bao gồm 21 bảng với các loại quan hệ:

| Loại quan hệ | Số lượng | Mô tả |
|--------------|----------|--------|
| **One-to-Many (1:N)** | 15 | Quan hệ cha-con phổ biến nhất trong hệ thống |
| **Many-to-Many (N:M)** | 1 | Thông qua bảng trung gian employee_role |
| **One-to-One (1:1)** | 1 | customer <-> customer_point |
| **Self-Referential (Tự tham chiếu)** | 1 | category với parent_category_id |
| **Composite (Tổng hợp)** | 2 | inventory, stock_transfer |

---

## 2. Sơ Đồ Quan Hệ Dạng Văn Bản

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              NHÓM IDENTITY & ACCESS                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│    ┌──────────┐                    ┌────────────────┐                                 │
│    │   role   │◄───────────────────│ employee_role  │───────────┐                     │
│    └──────────┘                    └────────────────┘           │                     │
│         │                                  │                      │                     │
│         │                                  │                      │                     │
│         │                                  ▼                      ▼                     │
│         │                          ┌────────────────┐     ┌─────────────┐               │
│         └──────────────────────────│   employee     │────►│   branch    │               │
│                                    └────────────────┘     └─────────────┘               │
│                                         │                                             │
│                                         │                                             │
└─────────────────────────────────────────│─────────────────────────────────────────────┘
                                          │
                                          ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              NHÓM BUSINESS PARTNERS                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│    ┌───────────┐      ┌──────────────────┐     ┌────────────┐                           │
│    │ customer  │──────│ customer_point   │                                              │
│    └───────────┘      └──────────────────┘     └────────────┘                           │
│         │                                          │                                     │
│         │                                          │                                     │
│         └──────────────────────────────────────────┼───────────────────────────────────┤
│                                                    │                                   │
└────────────────────────────────────────────────────┼───────────────────────────────────┘
                                                     │
                                                     ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    NHÓM COMMERCE                                         │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│    ┌────────────────────┐                                                               │
│    │      supplier      │                                                               │
│    └─────────┬──────────┘                                                               │
│              │                                                                          │
│              │                                                                          │
│              ▼                                                                          │
│    ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│    │                                                                                 │   │
│    │                              [ order ]                                         │   │
│    │                                                                                 │   │
│    │    PK: order_id                                                                │   │
│    │    FK: customer_id ──────────► customer                                        │   │
│    │    FK: branch_id ─────────────► branch                                          │   │
│    │    FK: supplier_id ───────────► supplier                                        │   │
│    │    FK: emp_id ────────────────► employee                                        │   │

│    │    FK: warehouse_id ──────────► warehouse                                       │   │
│    │                                                                                 │   │
│    └─────────────────────────────────────────────────────────────────────────────────┘   │
│              │                                                                          │
│              │                                                                          │
│              ▼                                                                          │
│    ┌────────────────────┐      ┌─────────────┐                                        │
│    │    order_detail     │      │   payment   │                                        │
│    └────────────────────┘      └─────────────┘                                        │
│              │                                                                          │
│              ▼                                                                          │
│    ┌────────────────────┐                                                              │
│    │      product       │──────────────────────────────────────────────────────────┐ │
│    └────────────────────┘                                                          │   │
│                                                                                      │   │
│              │                                                                  │   │
│              ▼                                                                  ▼   │
│    ┌────────────────────┐      ┌─────────────┐      ┌─────────────────┐          │   │
│    │    inventory       │      │    unit     │      │    category     │◄─────────┘   │
│    └────────────────────┘      └─────────────┘      └─────────────────┘              │
│              │                                                                          │
│              ▼                                                                          │
│    ┌────────────────────┐                                                              │
│    │     warehouse      │──────────────────────────────────────────────────────────┤
│    └────────────────────┘                                                          │   │
│                                                                                      │   │
│              │                                                                  │   │
│              ▼                                                                  ▼   │
│    ┌────────────────────────────────┐      ┌────────────────────┐                    │   │
│    │       stock_transfer           │─────►│stock_transfer_detail│                 │   │
│    └────────────────────────────────┘      └────────────────────┘                    │   │
│              │                                                                          │
│              ▼                                                                          │
│    ┌────────────────────────────┐                                                       │
│    │    stock_transaction       │───────────────────────────────────────────────────┘
│    └────────────────────────────┘                                                       
│              │                                                                         
│              ▼                                                                         
│    ┌────────────────┐                                                                  
│    │    audit_log   │                                                                  
│    └────────────────┘                                                                  
│                                                                                         
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### Chú Thích Sơ Đồ

- **PK** = Primary Key (Khóa chính)
- **FK** = Foreign Key (Khóa ngoại)
- **─►** = Tham chiếu đến bảng khác (Many-to-One)
- **◄──** = Tham chiếu từ bảng khác (One-to-Many)

---

## 3. Danh Sách Khóa Chính

Mỗi bảng trong cơ sở dữ liệu có một khóa chính duy nhất:

| STT | Bảng | Khóa Chính | Kiểu dữ liệu | Tự động tăng |
|-----|------|-----------|--------------|--------------|
| 1 | `role` | `role_id` | INT | Có |
| 2 | `branch` | `branch_id` | INT | Có |
| 3 | `employee` | `emp_id` | INT | Có |
| 4 | `employee_role` | `emp_role_id` | INT | Có |
| 5 | `customer` | `cus_id` | INT | Có |
| 6 | `customer_point` | `cus_point_id` | INT | Có |
| 7 | `supplier` | `supplier_id` | INT | Có |
| 8 | `warehouse` | `warehouse_id` | INT | Có |
| 9 | `unit` | `unit_id` | INT | Có |
| 10 | `category` | `category_id` | INT | Có |
| 11 | `product` | `product_id` | INT | Có |
| 12 | `inventory` | `inventory_id` | INT | Có |
| 13 | `[order]` | `order_id` | INT | Có |
| 14 | `order_detail` | `order_detail_id` | INT | Có |
| 15 | `payment` | `payment_id` | INT | Có |
| 16 | `point_transaction` | `point_transaction_id` | INT | Có |
| 17 | `stock_transfer` | `stock_transfer_id` | INT | Có |
| 18 | `stock_transfer_detail` | `stock_transfer_detail_id` | INT | Có |
| 19 | `stock_transaction` | `stock_transaction_id` | INT | Có |
| 20 | `audit_log` | `audit_log_id` | INT | Có |

---

## 4. Danh Sách Khóa Ngoại

Tất cả 21 khóa ngoại trong cơ sở dữ liệu:

| STT | Bảng (Con) | Cột Khóa Ngoại | Bảng (Cha) | Mô tả |
|-----|------------|---------------|------------|-------|
| 1 | `employee` | `branch_id` | `branch` | Nhân viên thuộc chi nhánh |
| 2 | `employee_role` | `emp_id` | `employee` | Vai trò của nhân viên |
| 3 | `employee_role` | `role_id` | `role` | Nhân viên có vai trò |
| 4 | `customer_point` | `cus_id` | `customer` | Điểm thưởng của khách hàng |
| 5 | `[order]` | `customer_id` | `customer` | Đơn hàng của khách hàng |
| 6 | `[order]` | `branch_id` | `branch` | Đơn hàng tại chi nhánh |
| 7 | `[order]` | `supplier_id` | `supplier` | Đơn nhập hàng từ nhà cung cấp |
| 8 | `[order]` | `emp_id` | `employee` | Nhân viên xử lý đơn hàng |
| 9 | `[order]` | `warehouse_id` | `warehouse` | Kho xử lý đơn hàng |
| 11 | `order_detail` | `order_id` | `[order]` | Chi tiết thuộc đơn hàng |
| 12 | `order_detail` | `product_id` | `product` | Sản phẩm trong đơn hàng |
| 13 | `payment` | `order_id` | `[order]` | Thanh toán cho đơn hàng |
| 14 | `point_transaction` | `cus_point_id` | `customer_point` | Giao dịch điểm thuộc khách hàng |
| 15 | `point_transaction` | `order_id` | `[order]` | Giao dịch điểm từ đơn hàng |
| 16 | `warehouse` | `branch_id` | `branch` | Kho thuộc chi nhánh |
| 17 | `category` | `parent_category_id` | `category` | Danh mục cha (tự tham chiếu) |
| 18 | `product` | `category_id` | `category` | Sản phẩm thuộc danh mục |
| 19 | `product` | `unit_id` | `unit` | Đơn vị tính của sản phẩm |
| 20 | `inventory` | `warehouse_id` | `warehouse` | Tồn kho tại kho |
| 21 | `inventory` | `product_id` | `product` | Tồn kho của sản phẩm |
| 22 | `stock_transfer` | `from_warehouse_id` | `warehouse` | Kho nguồn chuyển kho |
| 23 | `stock_transfer` | `to_warehouse_id` | `warehouse` | Kho đích chuyển kho |
| 24 | `stock_transfer` | `created_by` | `employee` | Nhân viên tạo phiếu chuyển kho |
| 25 | `stock_transfer_detail` | `stock_transfer_id` | `stock_transfer` | Chi tiết thuộc phiếu chuyển kho |
| 26 | `stock_transfer_detail` | `product_id` | `product` | Sản phẩm được chuyển |
| 27 | `stock_transaction` | `warehouse_id` | `warehouse` | Giao dịch kho |
| 28 | `stock_transaction` | `product_id` | `product` | Giao dịch sản phẩm |
| 29 | `stock_transaction` | `created_by` | `employee` | Nhân viên thực hiện giao dịch |
| 30 | `audit_log` | `emp_id` | `employee` | Nhân viên thực hiện hành động |

---

## 5. Quan Hệ Chi Tiết Theo Nhóm Chức Năng

### 5.1. Nhóm Identity & Access

#### 5.1.1. employee -> branch

```
employee.branch_id ──────► branch.branch_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-Many (1:N) |
| Cardinality | Mỗi nhân viên thuộc về đúng một chi nhánh |
| | Mỗi chi nhánh có thể có nhiều nhân viên |
| Khóa ngoại | `employee.branch_id` |
| Ràng buộc xóa | NO ACTION |

**Phân tích:** Một nhân viên chỉ được phân công làm việc tại một chi nhánh duy nhất tại bất kỳ thời điểm nào. Khi chi nhánh bị xóa, các nhân viên vẫn được giữ lại nhưng `branch_id` sẽ trở thành NULL (hoặc cần xử lý riêng).

---

#### 5.1.2. employee_role -> employee + role

```
employee.emp_id ◄──────── employee_role.emp_id
role.role_id ◄────────── employee_role.role_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | Many-to-Many (N:M) qua bảng trung gian |
| Cardinality | Mỗi nhân viên có thể có nhiều vai trò |
| | Mỗi vai trò có thể được gán cho nhiều nhân viên |
| Bảng trung gian | `employee_role` |
| Khóa ngoại 1 | `employee_role.emp_id` -> `employee.emp_id` |
| Khóa ngoại 2 | `employee_role.role_id` -> `role.role_id` |
| Ràng buộc xóa (emp) | CASCADE (xóa nhân viên sẽ xóa các liên kết vai trò) |
| Ràng buộc xóa (role) | NO ACTION |

**Phân tích:** Đây là quan hệ nhiều-nhiều điển hình, cho phép một nhân viên vừa là "Thu ngân" vừa là "Quản lý kho". Bảng trung gian `employee_role` lưu trữ các cặp (emp_id, role_id) duy nhất.

---

#### 5.1.3. Tổng quan nhóm Identity & Access

```
branch (1) ──────────► (N) employee (N) ◄───────── (N) role (1)
                             │
                             └── (N) employee_role (1)
```

---

### 5.2. Nhóm Business Partners

#### 5.2.1. customer -> customer_point

```
customer.cus_id ──────► customer_point.cus_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-One (1:1) |
| Cardinality | Mỗi khách hàng có đúng một bản ghi điểm thưởng |
| | Mỗi bản ghi điểm thưởng thuộc về đúng một khách hàng |
| Khóa ngoại | `customer_point.cus_id` |
| Ràng buộc xóa | CASCADE (xóa khách hàng sẽ xóa điểm thưởng) |
| Ràng buộc unique | Nên có UNIQUE trên `customer_point.cus_id` |

**Phân tích:** Quan hệ một-một này đảm bảo mỗi khách hàng chỉ có một tài khoản điểm thưởng duy nhất. Ràng buộc UNIQUE trên `cus_id` trong bảng `customer_point` ngăn chặn việc một khách hàng có nhiều bản ghi điểm.

---

### 5.3. Nhóm Commerce

#### 5.3.1. order_detail -> [order]

```
[order].order_id ──────► order_detail.order_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-Many (1:N) |
| Cardinality | Mỗi đơn hàng có thể có nhiều dòng chi tiết |
| | Mỗi dòng chi tiết thuộc về đúng một đơn hàng |
| Khóa ngoại | `order_detail.order_id` |
| Ràng buộc xóa | CASCADE |

**Phân tích:** Khi xóa đơn hàng, tất cả các chi tiết đơn hàng tương ứng cũng được xóa tự động. Điều này đảm bảo tính nhất quán của dữ liệu.

---

#### 5.3.2. order_detail -> product

```
product.product_id ──────► order_detail.product_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-Many (1:N) |
| Cardinality | Mỗi sản phẩm có thể xuất hiện trong nhiều dòng chi tiết đơn hàng |
| | Mỗi dòng chi tiết tham chiếu đúng một sản phẩm |
| Khóa ngoại | `order_detail.product_id` |
| Ràng buộc xóa | NO ACTION |

**Phân tích:** Sản phẩm không thể bị xóa nếu có chi tiết đơn hàng tham chiếu đến nó. Điều này bảo toàn lịch sử đơn hàng.

---

#### 5.3.3. [order] với nhiều bảng

Bảng `[order]` là bảng trung tâm của hệ thống thương mại, có 5 khóa ngoại:

| Khóa ngoại | Bảng tham chiếu | Loại liên kết | Mô tả |
|-----------|----------------|---------------|-------|
| `customer_id` | `customer` | NULL-able | Khách hàng đặt hàng (cho đơn SALE) |
| `branch_id` | `branch` | NULL-able | Chi nhánh nơi đơn được tạo |
| `supplier_id` | `supplier` | NULL-able | Nhà cung cấp (cho đơn PURCHASE) |
| `emp_id` | `employee` | NULL-able | Nhân viên xử lý đơn hàng |
| `warehouse_id` | `warehouse` | NULL-able | Kho xử lý đơn hàng |

```
customer ◄────── (N) [order] ──────► (1) branch
          │                              │
          │                              │
          ▼                              ▼
supplier ◄┘                    ┌─────────────────┐
                               │                 │
                               │    [ order ]     │
                               │                 │
                               │  (5 khóa ngoại) │
                               │                 │
                               └────────┬────────┘
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        │                                │                                │
        ▼                                ▼                                ▼
    voucher                         employee                          warehouse
```

**Phân tích:** Thiết kế này cho phép đơn hàng linh hoạt với nhiều loại (SALE, PURCHASE, RETURN), mỗi loại sử dụng các khóa ngoại khác nhau. Tất cả các khóa ngoại đều có thể NULL để hỗ trợ các loại đơn hàng khác nhau.

---

#### 5.3.4. payment -> [order]

```
[order].order_id ──────► payment.order_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-Many (1:N) |
| Cardinality | Mỗi đơn hàng có thể có nhiều bản ghi thanh toán |
| | Mỗi bản ghi thanh toán thuộc về đúng một đơn hàng |
| Khóa ngoại | `payment.order_id` |
| Ràng buộc xóa | CASCADE |

**Phân tích:** Một đơn hàng có thể được thanh toán theo nhiều đ�t (ví dụ: trả trước 30%, còn lại khi nhận hàng) hoặc kết hợp nhiều phương thức thanh toán (tiền mặt + thẻ).

---

#### 5.3.5. point_transaction -> customer_point + [order]

```
customer_point.cus_point_id ──────► point_transaction.cus_point_id
[order].order_id ─────────────────► point_transaction.order_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | Hai One-to-Many |
| customer_point -> point_transaction | 1:N (mỗi khách hàng có nhiều giao dịch điểm) |
| [order] -> point_transaction | 1:N (mỗi đơn hàng có nhiều giao dịch điểm - hiếm) |
| Khóa ngoại 1 | `point_transaction.cus_point_id` |
| Khóa ngoại 2 | `point_transaction.order_id` (NULL-able) |

**Phân tích:** `order_id` trong `point_transaction` có thể NULL cho các giao dịch điểm không liên quan đến đơn hàng (ví dụ: điểm thưởng sinh nhật, điểm thưởng khuyến mãi).

---

### 5.4. Nhóm Warehouse & Stock

#### 5.4.1. product -> category

```
category.category_id ──────► product.category_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-Many (1:N) |
| Cardinality | Mỗi danh mục có thể chứa nhiều sản phẩm |
| | Mỗi sản phẩm thuộc về đúng một danh mục |
| Khóa ngoại | `product.category_id` |
| Ràng buộc xóa | SET NULL |

**Phân tích:** Khi xóa danh mục, các sản phẩm vẫn được giữ lại nhưng `category_id` trở thành NULL (sản phẩm không có danh mục).

---

#### 5.4.2. product -> unit

```
unit.unit_id ──────► product.unit_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-Many (1:N) |
| Cardinality | Mỗi đơn vị tính có thể áp dụng cho nhiều sản phẩm |
| | Mỗi sản phẩm có đúng một đơn vị tính |
| Khóa ngoại | `product.unit_id` |
| Ràng buộc xóa | SET NULL |

---

#### 5.4.3. category -> category (Self-Referential)

```
category.category_id ──────► category.parent_category_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | Self-Referential (Tự tham chiếu) |
| Cardinality | Mỗi danh mục có thể có một danh mục cha |
| | Mỗi danh mục có thể có nhiều danh mục con |
| Khóa ngoại | `category.parent_category_id` |
| Ràng buộc xóa | SET NULL |

**Cấu trúc cây danh mục:**
```
Danh mục gốc (parent_category_id = NULL)
├── Danh mục con 1
│   ├── Danh mục cháu 1.1
│   └── Danh mục cháu 1.2
├── Danh mục con 2
│   └── Danh mục cháu 2.1
└── Danh mục con 3
```

**Phân tích:** Thiết kế này cho phép xây dựng cây danh mục đa cấp với độ sâu không giới hạn. Cần kiểm tra chu trình (cycle) khi cập nhật `parent_category_id` để tránh vòng lặp vô hạn.

---

#### 5.4.4. warehouse -> branch

```
branch.branch_id ──────► warehouse.branch_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-Many (1:N) |
| Cardinality | Mỗi chi nhánh có thể có nhiều kho hàng |
| | Mỗi kho hàng thuộc về đúng một chi nhánh |
| Khóa ngoại | `warehouse.branch_id` |
| Ràng buộc xóa | NO ACTION |

---

#### 5.4.5. inventory -> warehouse + product (Composite)

```
warehouse.warehouse_id ──────► inventory.warehouse_id
product.product_id ──────────► inventory.product_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | Composite One-to-Many |
| warehouse -> inventory | 1:N (mỗi kho có nhiều bản ghi tồn kho) |
| product -> inventory | 1:N (mỗi sản phẩm có nhiều bản ghi tồn kho theo kho) |
| Khóa ngoại 1 | `inventory.warehouse_id` |
| Khóa ngoại 2 | `inventory.product_id` |
| Ràng buộc xóa | CASCADE |

**Phân tích:** Cặp (`warehouse_id`, `product_id`) xác định duy nhất một bản ghi tồn kho. Nên có ràng buộc UNIQUE trên cặp này. Mỗi sản phẩm tại mỗi kho có đúng một bản ghi tồn kho.

---

#### 5.4.6. stock_transfer -> warehouse (from + to)

```
warehouse.warehouse_id ──────► stock_transfer.from_warehouse_id
warehouse.warehouse_id ──────► stock_transfer.to_warehouse_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | Hai One-to-Many từ cùng một bảng |
| warehouse -> from | 1:N (mỗi kho có thể là kho nguồn của nhiều phiếu chuyển) |
| warehouse -> to | 1:N (mỗi kho có thể là kho đích của nhiều phiếu chuyển) |
| Khóa ngoại 1 | `stock_transfer.from_warehouse_id` |
| Khóa ngoại 2 | `stock_transfer.to_warehouse_id` |
| Ràng buộc xóa | NO ACTION |

**Phân tích:** Một kho có thể vừa là kho nguồn vừa là kho đích trong các phiếu chuyển kho khác nhau.

---

#### 5.4.7. stock_transfer_detail -> stock_transfer + product

```
stock_transfer.stock_transfer_id ──────► stock_transfer_detail.stock_transfer_id
product.product_id ─────────────────────► stock_transfer_detail.product_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | Hai One-to-Many |
| stock_transfer -> stock_transfer_detail | 1:N |
| product -> stock_transfer_detail | 1:N |
| Khóa ngoại 1 | `stock_transfer_detail.stock_transfer_id` |
| Khóa ngoại 2 | `stock_transfer_detail.product_id` |
| Ràng buộc xóa | CASCADE |

---

#### 5.4.8. stock_transaction -> warehouse + product + employee

```
warehouse.warehouse_id ──────► stock_transaction.warehouse_id
product.product_id ──────────► stock_transaction.product_id
employee.emp_id ─────────────► stock_transaction.created_by
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | Ba One-to-Many |
| warehouse -> stock_transaction | 1:N |
| product -> stock_transaction | 1:N |
| employee -> stock_transaction | 1:N |
| Khóa ngoại 1 | `stock_transaction.warehouse_id` |
| Khóa ngoại 2 | `stock_transaction.product_id` |
| Khóa ngoại 3 | `stock_transaction.created_by` |
| Ràng buộc xóa | NO ACTION (warehouse, product), SET NULL (employee) |

**Phân tích:** Bảng này ghi nhận mọi biến động tồn kho với đầy đủ ngữ cảnh: sản phẩm nào, tại kho nào, do ai thực hiện, liên quan đến đơn hàng/phiếu chuyển nào.

---

### 5.5. Nhóm System

#### 5.5.1. audit_log -> employee

```
employee.emp_id ──────► audit_log.emp_id
```

| Thuộc tính | Giá trị |
|-----------|---------|
| Loại quan hệ | One-to-Many (1:N) |
| Cardinality | Mỗi nhân viên có thể thực hiện nhiều hành động được ghi log |
| | Mỗi bản ghi audit thuộc về đúng một nhân viên |
| Khóa ngoại | `audit_log.emp_id` |
| Ràng buộc xóa | SET NULL |

**Phân tích:** Khi xóa nhân viên, các bản ghi audit log vẫn được giữ lại nhưng `emp_id` trở thành NULL để bảo toàn lịch sử.

---

## 6. Cardinality Matrix

Ma trận cardinality thể hiện số lượng tối thiểu và tối đa của mỗi thực thể trong quan hệ:

| Bảng A | Quan hệ | Bảng B | Min A | Max A | Min B | Max B | Loại |
|--------|---------|--------|-------|-------|-------|-------|------|
| `employee` | thuộc về | `branch` | 0 | N | 1 | 1 | 1:N |
| `employee` | có | `employee_role` | 0 | N | 1 | 1 | 1:N |
| `role` | được gán cho | `employee_role` | 0 | N | 1 | 1 | 1:N |
| `customer` | có | `customer_point` | 0 | 1 | 1 | 1 | 1:1 |
| `customer` | đặt | `[order]` | 0 | N | 0 | 1 | 1:N |
| `branch` | có | `employee` | 0 | N | 1 | 1 | 1:N |
| `branch` | có | `warehouse` | 0 | N | 1 | 1 | 1:N |
| `supplier` | cung cấp cho | `[order]` | 0 | N | 0 | 1 | 1:N |
| `employee` | xử lý | `[order]` | 0 | N | 0 | 1 | 1:N |

| `warehouse` | phục vụ | `[order]` | 0 | N | 0 | 1 | 1:N |
| `[order]` | chứa | `order_detail` | 1 | 1 | 1 | N | 1:N |
| `[order]` | nhận | `payment` | 0 | N | 1 | 1 | 1:N |
| `product` | xuất hiện trong | `order_detail` | 0 | N | 1 | 1 | 1:N |
| `category` | chứa | `product` | 0 | N | 0 | 1 | 1:N |
| `category` | có cha là | `category` | 0 | N | 0 | 1 | Self-Ref |
| `unit` | áp dụng cho | `product` | 0 | N | 0 | 1 | 1:N |
| `warehouse` | lưu trữ | `inventory` | 0 | N | 0 | 1 | 1:N |
| `product` | được lưu tại | `inventory` | 0 | N | 0 | 1 | 1:N |
| `warehouse` | là nguồn của | `stock_transfer` | 0 | N | 0 | 1 | 1:N |
| `warehouse` | là đích của | `stock_transfer` | 0 | N | 0 | 1 | 1:N |
| `employee` | tạo | `stock_transfer` | 0 | N | 0 | 1 | 1:N |
| `stock_transfer` | chứa | `stock_transfer_detail` | 1 | 1 | 1 | N | 1:N |
| `product` | được chuyển trong | `stock_transfer_detail` | 0 | N | 1 | 1 | 1:N |
| `warehouse` | phát sinh | `stock_transaction` | 0 | N | 0 | 1 | 1:N |
| `product` | biến động trong | `stock_transaction` | 0 | N | 0 | 1 | 1:N |
| `employee` | thực hiện | `stock_transaction` | 0 | N | 0 | 1 | 1:N |
| `employee` | được ghi trong | `audit_log` | 0 | N | 0 | 1 | 1:N |
| `customer_point` | phát sinh | `point_transaction` | 0 | N | 1 | 1 | 1:N |
| `[order]` | sinh ra | `point_transaction` | 0 | N | 0 | 1 | 1:N |

---

## 7. Phân Tích Các Quan Hệ Quan Trọng

### 7.1. Quan Hệ Nhiều-Nhiều (Many-to-Many)

**employee <-> role thông qua employee_role**

Đây là quan hệ nhiều-nhiều duy nhất trong hệ thống. Mỗi nhân viên có thể có nhiều vai trò và mỗi vai trò có thể được gán cho nhiều nhân viên.

```
employee ───────┐
                ├── employee_role ────┐
role ───────────┘                     │
                                    │
                                    ▼
                              (emp_id, role_id) UNIQUE
```

**Ví dụ:**
- Nhân viên A: vai trò [CASHIER, WAREHOUSE_VIEWER]
- Nhân viên B: vai trò [CASHIER, INVENTORY_MANAGER]
- Vai trò CASHIER: được gán cho [Nhân viên A, Nhân viên B, ...]

---

### 7.2. Quan Hệ Một-Một (One-to-One)

**customer <-> customer_point**

Mỗi khách hàng có đúng một bản ghi điểm thưởng. Ràng buộc UNIQUE trên `customer_point.cus_id` đảm bảo tính toàn vẹn.

```
customer ──────────────────────────────────────────┐
                                                │
                                                │ (1:1)
                                                ▼
                              ┌───────────────────────┐
                              │   customer_point     │
                              │   PK: cus_point_id   │
                              │   FK: cus_id (UNIQUE) │
                              └───────────────────────┘
```

---

### 7.3. Quan Hệ Tự Tham Chiếu (Self-Referential)

**category (self-reference qua parent_category_id)**

Cho phép xây dựng cây danh mục phân cấp:

```
category
├── Thực phẩm (category_id = 1, parent = NULL)
│   ├── Đồ uống (category_id = 2, parent = 1)
│   │   ├── Nước giải khát (category_id = 5, parent = 2)
│   │   └── Trà, cà phê (category_id = 6, parent = 2)
│   └── Đồ ăn vặt (category_id = 3, parent = 1)
│       ├── Bánh (category_id = 7, parent = 3)
│       └── Kẹo (category_id = 8, parent = 3)
└── Đồ gia dụng (category_id = 4, parent = NULL)
```

---

### 7.4. Bảng Trung Tâm Quan Hệ

**Bảng [order] với 6 khóa ngoại**

Bảng `[order]` là bảng trung tâm nhất trong hệ thống thương mại, liên kết với nhiều bảng khác:

```
                    ┌─────────────┐
                    │   supplier   │
                    └──────┬──────┘
                           │
                           │ supplier_id
                           ▼
┌─────────┐  customer_id ┌─────────────┐
│customer ├─────────────►│             │
└─────────┘              │             │
                         │   [order]  │
┌─────────┐  emp_id      │             │              ┌──────────┐
│employee ├─────────────►│             │◄─────────────┤ warehouse│
└─────────┘              └──────┬──────┘              └──────────┘
                                │
                                │ branch_id
                                ▼
                         ┌─────────────┐
                         │   branch    │
                         └─────────────┘
```

---

### 7.5. Quan Hệ Composite

**inventory (warehouse + product)**

Mỗi bản ghi tồn kho được xác định bởi cặp (warehouse, product):

```
┌─────────────────────────────────────────────────────────────┐
│                         INVENTORY                           │
│  PK: inventory_id                                           │
│  FK: warehouse_id ────────► warehouse                       │
│  FK: product_id ───────────► product                        │
│  UNIQUE: (warehouse_id, product_id)                         │
│                                                             │
│  Ví dụ:                                                     │
│  ┌────────────────┬─────────────┬──────────────────┐       │
│  │ warehouse_id    │ product_id  │ quantity_in_stock │       │
│  ├────────────────┼─────────────┼──────────────────┤       │
│  │ 1 (Kho Q1)     │ 101 (Sữa)  │ 50               │       │
│  │ 1 (Kho Q1)     │ 102 (Bánh) │ 30               │       │
│  │ 2 (Kho Q5)     │ 101 (Sữa)  │ 20               │       │
│  │ 2 (Kho Q5)     │ 102 (Bánh) │ 40               │       │
│  └────────────────┴─────────────┴──────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

---

## Tổng Kết

Cơ sở dữ liệu DBFinoraV2 có 21 bảng với 30 khóa ngoại, được tổ chức theo 5 nhóm chức năng. Các đặc điểm nổi bật:

1. **Một bảng trung gian** (`employee_role`) thực hiện quan hệ nhiều-nhiều
2. **Một bảng tự tham chiếu** (`category`) với cấu trúc cây phân cấp
3. **Một bảng tổng hợp** (`[order]`) với 6 khóa ngoại, hỗ trợ nhiều loại đơn hàng
4. **Một bảng quan hệ một-một** (`customer_point`) với khách hàng
5. **Một bảng composite** (`inventory`) xác định bởi hai khóa ngoại

# Store Module - FinoraRetail

## 1. Tổng quan Module

Module Store (Quản lý Cửa hàng/Chi nhánh) là module quản lý thông tin các cửa hàng và chi nhánh trong hệ thống FinoraRetail. Module này chịu trách nhiệm lưu trữ và quản lý hồ sơ cửa hàng, bao gồm thông tin địa chỉ, giờ mở cửa, thông tin liên hệ, trạng thái hoạt động, và các cấu hình liên quan đến từng điểm bán.

Trong hệ thống bán lẻ đa chi nhánh, Store Module đóng vai trò nền tảng vì mỗi chi nhánh là một đơn vị kinh doanh độc lập với kho hàng riêng, nhân viên riêng, và báo cáo riêng. Module này liên kết chặt chẽ với Inventory Module (quản lý kho theo chi nhánh), User Module (nhân viên làm việc tại chi nhánh), Order Module (đơn hàng từ chi nhánh), và Report Module (báo cáo theo chi nhánh).

---

## 2. Thông tin kỹ thuật

### 2.1. Route và Controller

| Thuộc tính | Chi tiết |
|---|---|
| **Route chính** | `/stores/*` |
| **Controller** | `StoreController.java` |
| **Package** | `controller.store` |
| **Trạng thái** | Skeleton |

Controller sử dụng Front Controller pattern để điều hướng các request liên quan đến cửa hàng. Hiện tại, controller chỉ đóng vai trò định tuyến cơ bản mà chưa triển khai logic nghiệp vụ.

### 2.2. Model

| Thuộc tính | Chi tiết |
|---|---|
| **Tệp** | `Store.java` |
| **Package** | `model.store` |

Model định nghĩa các thuộc tính của cửa hàng/chi nhánh bao gồm thông tin định danh, địa chỉ, liên hệ, và cấu hình hoạt động.

### 2.3. DAO (Data Access Object)

| Thuộc tính | Chi tiết |
|---|---|
| **Tệp** | `StoreDAO.java` |
| **Package** | `dao.store` |
| **Trạng thái** | Skeleton (chưa triển khai SQL) |

DAO cung cấp các phương thức truy cập cơ sở dữ liệu chuẩn hóa cho Store Model.

---

## 3. Views (JSP)

Module sử dụng 4 file JSP để hiển thị giao diện người dùng:

| Tệp JSP | Mô tả |
|---|---|
| `list.jsp` | Trang danh sách cửa hàng với tìm kiếm, lọc theo trạng thái, phân trang |
| `add.jsp` | Form thêm mới cửa hàng |
| `edit.jsp` | Form chỉnh sửa thông tin cửa hàng |
| `detail.jsp` | Trang chi tiết cửa hàng với thông tin đầy đủ và thống kê |

---

## 4. Cơ sở dữ liệu

### 4.1. Bảng chính: `branch`

**Lưu ý quan trọng:** Trong schema cơ sở dữ liệu, bảng được đặt tên là `branch` thay vì `store`. Trong giao tiếp và code, hai thuật ngữ này được sử dụng thay thế cho nhau (interchangeable).

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `branch_id` | INT | Khóa chính, tự động tăng |
| `branch_name` | NVARCHAR(100) | Tên chi nhánh |
| `branch_code` | VARCHAR(20) | Mã chi nhánh (duy nhất) |
| `address` | NVARCHAR(500) | Địa chỉ chi tiết |
| `province` | NVARCHAR(100) | Tỉnh/Thành phố |
| `district` | NVARCHAR(100) | Quận/Huyện |
| `ward` | NVARCHAR(100) | Phường/Xã |
| `phone` | VARCHAR(20) | Số điện thoại |
| `email` | VARCHAR(100) | Email liên hệ |
| `manager_name` | NVARCHAR(100) | Tên người quản lý |
| `manager_phone` | VARCHAR(20) | SĐT người quản lý |
| `open_time` | TIME | Giờ mở cửa |
| `close_time` | TIME | Giờ đóng cửa |
| `opening_date` | DATE | Ngày khai trương |
| `latitude` | DECIMAL(10,8) | Vĩ độ (cho bản đồ) |
| `longitude` | DECIMAL(11,8) | Kinh độ (cho bản đồ) |
| `status` | VARCHAR(20) | Trạng thái hoạt động |
| `created_at` | DATETIME | Thời điểm tạo |
| `updated_at` | DATETIME | Thời điểm cập nhật cuối |

---

## 5. Tính năng chính

### 5.1. Quản lý cửa hàng (CRUD)

- **Tạo mới**: Thêm cửa hàng mới với thông tin đầy đủ
- **Xem danh sách**: Liệt kê cửa hàng với phân trang, tìm kiếm theo tên/mã
- **Xem chi tiết**: Hiển thị thông tin đầy đủ kèm thống kê
- **Cập nhật**: Chỉnh sửa thông tin cửa hàng
- **Xóa/Vô hiệu hóa**: Deactivate cửa hàng (không xóa vĩnh viễn)

### 5.2. Quản lý trạng thái

| Trạng thái | Mã | Mô tả |
|---|---|---|
| Hoạt động | `active` | Cửa hàng đang mở cửa bình thường |
| Tạm đóng | `temporary_closed` | Đóng cửa tạm thời (bảo trì, dịch Covid...) |
| Đã đóng | `closed` | Không còn hoạt động |
| Sắp khai trương | `pre_open` | Đang chuẩn bị khai trương |

### 5.3. Thông tin vận hành

- **Giờ mở cửa**: Thời gian bắt đầu và kết thúc làm việc mỗi ngày
- **Địa chỉ đầy đủ**: Bao gồm tỉnh/thành, quận/huyện, phường/xã (hỗ trợ địa chỉ tổ ong)
- **Tọa độ**: Vĩ độ, kinh độ để hiển thị trên bản đồ
- **Người quản lý**: Thông tin người phụ trách chi nhánh

---

## 6. Quy tắc nghiệp vụ

### 6.1. Mã chi nhánh

- Mã chi nhánh (`branch_code`) phải là duy nhất trong toàn hệ thống
- Định dạng đề xuất: `CN-XXX` (VD: CN-HCM, CN-HNI, CN-DNG)
- Không cho phép thay đổi mã sau khi tạo

### 6.2. Địa chỉ

- Địa chỉ phải bao gồm đầy đủ các cấp: số nhà, đường, phường/xã, quận/huyện, tỉnh/thành
- Hỗ trợ địa chỉ tổ ong (province, district, ward) để tích hợp API giao hàng

### 6.3. Giờ mở cửa

- `open_time` phải nhỏ hơn `close_time`
- Hỗ trợ cửa hàng 24/7 bằng cách đặt giờ đặc biệt hoặc cờ `is_24h`

---

## 7. Liên kết với các thực thể khác

### 7.1. Warehouse (Kho hàng)

Mỗi chi nhánh có thể có một hoặc nhiều kho hàng:

```
Branch (Chi nhánh)
    ├── Main Warehouse (Kho chính)
    ├── Sub Warehouse (Kho phụ - nếu có)
    └── ...
```

### 7.2. Users (Nhân viên)

Nhân viên được gán với chi nhánh cụ thể thông qua `user_branch` mapping:

```
Branch (Chi nhánh)
    └── Users (Nhân viên)
          ├── Cashier 1
          ├── Cashier 2
          └── Manager
```

### 7.3. Orders (Đơn hàng)

Đơn hàng được tạo tại chi nhánh và liên kết qua `branch_id`.

---

## 8. Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| StoreController | Skeleton | Chỉ có routing |
| Store.java | Skeleton | Model định nghĩa cơ bản |
| StoreDAO.java | Skeleton | Chưa triển khai SQL |
| Views (4 files) | Template | Template HTML, chưa kết nối backend |

**Đánh giá tổng thể:** Module đang ở mức Skeleton - đã có cấu trúc cơ bản nhưng chưa triển khai logic nghiệp vụ.

---

## 9. Phụ thuộc module

- **Inventory Module**: Chi nhánh sở hữu các kho hàng
- **User Module**: Nhân viên làm việc tại chi nhánh
- **Order Module**: Đơn hàng được tạo tại chi nhánh
- **Report Module**: Báo cáo theo chi nhánh

---

## 10. Mở rộng trong tương lai

- Hỗ trợ đa ngôn ngữ cho tên chi nhánh
- Tích hợp bản đồ vị trí chi nhánh
- Quản lý thiết bị POS theo chi nhánh
- Cấu hình phương thức thanh toán theo chi nhánh
- Báo cáo so sánh hiệu suất giữa các chi nhánh

---

## 11. Thuật ngữ

| Thuật ngữ | Mô tả |
|---|---|
| Store | Từ tiếng Anh, thường dùng trong code và route |
| Branch | Tên bảng trong database |
| Chi nhánh | Từ tiếng Việt, dùng trong giao diện người dùng |
| Cửa hàng | Từ tiếng Việt, thường dùng cho retail outlet |

*Cả bốn thuật ngữ trên đều chỉ cùng một thực thể trong hệ thống.*

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

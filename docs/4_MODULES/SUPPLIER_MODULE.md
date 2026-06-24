# Supplier Module - FinoraRetail

## 1. Tổng quan Module

Module Supplier (Quản lý Nhà cung cấp) là module quản lý thông tin các nhà cung cấp hàng hóa trong hệ thống FinoraRetail. Module này chịu trách nhiệm lưu trữ và quản lý hồ sơ nhà cung cấp, thông tin liên hệ, điều khoản hợp tác, và lịch sử giao dịch mua hàng với từng nhà cung cấp.

Trong chuỗi cung ứng bán lẻ, Supplier Module đóng vai trò quan trọng trong việc duy trì mối quan hệ với các đối tác cung ứng, theo dõi các đơn đặt hàng (Purchase Orders), và quản lý quy trình nhập hàng vào kho. Module này liên kết chặt chẽ với Inventory Module (để cập nhật tồn kho khi nhập hàng) và Product Module (để liên kết sản phẩm với nhà cung cấp).

---

## 2. Thông tin kỹ thuật

### 2.1. Route và Controller

| Thuộc tính | Chi tiết |
|---|---|
| **Route chính** | `/suppliers/*` |
| **Controller** | `SupplierController.java` |
| **Package** | `controller.supplier` |
| **Trạng thái** | Skeleton |

Controller sử dụng Front Controller pattern để điều hướng các request liên quan đến nhà cung cấp. Hiện tại, controller chỉ đóng vai trò định tuyến cơ bản mà chưa triển khai logic nghiệp vụ.

### 2.2. Model

| Thuộc tính | Chi tiết |
|---|---|
| **Tệp** | `Supplier.java` |
| **Package** | `model.supplier` |

Model định nghĩa các thuộc tính của nhà cung cấp bao gồm thông tin công ty, thông tin liên hệ, và các thuộc tính liên quan đến quan hệ hợp tác.

### 2.3. DAO (Data Access Object)

| Thuộc tính | Chi tiết |
|---|---|
| **Tệp** | `SupplierDAO.java` |
| **Package** | `dao.supplier` |
| **Trạng thái** | Skeleton (chưa triển khai SQL) |

DAO cung cấp các phương thức truy cập cơ sở dữ liệu chuẩn hóa cho Supplier Model.

---

## 3. Views (JSP)

Module sử dụng 4 file JSP để hiển thị giao diện người dùng:

| Tệp JSP | Mô tả |
|---|---|
| `list.jsp` | Trang danh sách nhà cung cấp với tìm kiếm, lọc, phân trang |
| `create.jsp` | Form tạo mới nhà cung cấp |
| `edit.jsp` | Form chỉnh sửa thông tin nhà cung cấp |
| `detail.jsp` | Trang chi tiết nhà cung cấp với lịch sử giao dịch (tùy chọn) |

---

## 4. Cơ sở dữ liệu

### 4.1. Bảng chính: `supplier`

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `supplier_id` | INT | Khóa chính, tự động tăng |
| `supplier_name` | NVARCHAR(255) | Tên công ty nhà cung cấp |
| `contact_name` | NVARCHAR(100) | Tên người liên hệ |
| `email` | VARCHAR(100) | Email liên hệ |
| `phone` | VARCHAR(20) | Số điện thoại |
| `address` | NVARCHAR(500) | Địa chỉ công ty |
| `tax_code` | VARCHAR(20) | Mã số thuế |
| `payment_terms` | NVARCHAR(255) | Điều khoản thanh toán |
| `status` | VARCHAR(20) | Trạng thái (active/inactive) |
| `created_at` | DATETIME | Thời điểm tạo |
| `updated_at` | DATETIME | Thời điểm cập nhật |

---

## 5. Tính năng chính

### 5.1. Quản lý hồ sơ nhà cung cấp (CRUD)

- **Tạo mới**: Thêm nhà cung cấp với thông tin công ty và người liên hệ
- **Xem danh sách**: Liệt kê nhà cung cấp với phân trang, tìm kiếm theo tên/mã số thuế
- **Xem chi tiết**: Hiển thị thông tin đầy đủ của nhà cung cấp
- **Cập nhật**: Chỉnh sửa thông tin nhà cung cấp
- **Xóa/Vô hiệu hóa**: Soft delete hoặc deactivate nhà cung cấp

### 5.2. Quản lý trạng thái

| Trạng thái | Mô tả |
|---|---|
| `active` | Nhà cung cấp đang hoạt động, có thể tạo đơn đặt hàng |
| `inactive` | Nhà cung cấp tạm ngưng hợp tác |
| `blocked` | Nhà cung cấp bị chặn do vi phạm hoặc vấn đề chất lượng |

### 5.3. Thông tin bổ sung

- **Mã số thuế**: Định danh pháp lý của công ty
- **Điều khoản thanh toán**: Thời hạn thanh toán, phương thức thanh toán ưu tiên
- **Lịch sử giao dịch**: Số lượng đơn đặt hàng, tổng giá trị đã mua

---

## 6. Quy tắc nghiệp vụ

### 6.1. Quản lý trạng thái

- Nhà cung cấp ở trạng thái `inactive` không hiển thị trong danh sách chọn khi tạo đơn đặt hàng mới
- Khi deactivate nhà cung cấp, cần xác nhận không có đơn đặt hàng đang xử lý
- Không cho phép xóa vĩnh viễn (hard delete) nhà cung cấp có lịch sử giao dịch

### 6.2. Thông tin bắt buộc

- Tên công ty là bắt buộc và phải là duy nhất
- Ít nhất một trong hai: email hoặc số điện thoại phải được cung cấp
- Mã số thuế (nếu cung cấp) phải đúng định dạng

---

## 7. Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| SupplierController | Skeleton | Chỉ có routing |
| Supplier.java | Skeleton | Model định nghĩa cơ bản |
| SupplierDAO.java | Skeleton | Chưa triển khai SQL |
| Views (4 files) | Template | Template HTML, chưa kết nối backend |

**Đánh giá tổng thể:** Module đang ở mức Skeleton - đã có cấu trúc cơ bản nhưng chưa triển khai logic nghiệp vụ.

---

## 8. Phụ thuộc module

- **Product Module**: Sản phẩm có thể được liên kết với nhà cung cấp mặc định
- **Inventory Module**: Nhập hàng từ nhà cung cấp cập nhật tồn kho
- **Purchase Order Module**: Quản lý đơn đặt hàng với nhà cung cấp

---

## 9. Mở rộng trong tương lai

- Theo dõi đánh giá/chất lượng nhà cung cấp
- Quản lý hợp đồng với nhà cung cấp
- Cảnh báo khi đến hạn thanh toán
- Báo cáo thống kê theo nhà cung cấp

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

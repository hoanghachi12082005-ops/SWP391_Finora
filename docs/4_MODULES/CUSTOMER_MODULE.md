# Customer Module - FinoraRetail

## 1. Tổng quan Module

Module Customer (Quản lý Khách hàng) là module quản lý thông tin khách hàng trong hệ thống FinoraRetail. Module này chịu trách nhiệm lưu trữ và quản lý hồ sơ khách hàng, theo dõi lịch sử giao dịch, quản lý điểm tích lũy (loyalty points), và phân loại khách hàng theo các cấp bậc trong chương trình khách hàng thân thiết.

Trong hệ thống bán lẻ, Customer Module đóng vai trò quan trọng trong việc xây dựng mối quan hệ với khách hàng, hỗ trợ các chiến lược marketing cá nhân hóa, và thúc đẩy doanh số thông qua chương trình tích điểm. Module này liên kết chặt chẽ với Order Module (để theo dõi lịch sử mua hàng) và Payment Module (để xử lý thanh toán và tích điểm).

---

## 2. Thông tin kỹ thuật

### 2.1. Route và Controller

| Thuộc tính | Chi tiết |
|---|---|
| **Route chính** | `/customers/*` |
| **Controller** | `CustomerController.java` |
| **Package** | `controller.customer` |
| **Trạng thái** | Skeleton |

Controller sử dụng Front Controller pattern để điều hướng các request liên quan đến khách hàng. Hiện tại, controller chỉ đóng vai trò định tuyến cơ bản mà chưa triển khai logic nghiệp vụ.

### 2.2. Model

| Thuộc tính | Chi tiết |
|---|---|
| **Tệp** | `Customer.java` |
| **Package** | `model.customer` |

Model định nghĩa các thuộc tính của khách hàng bao gồm thông tin cá nhân, thông tin liên hệ, và các thuộc tính liên quan đến chương trình loyalty.

### 2.3. DAO (Data Access Object)

| Thuộc tính | Chi tiết |
|---|---|
| **Tệp** | `CustomerDAO.java` |
| **Package** | `dao.customer` |
| **Trạng thái** | Skeleton (chưa triển khai SQL) |

DAO cung cấp các phương thức truy cập cơ sở dữ liệu chuẩn hóa cho Customer Model, bao gồm các thao tác CRUD và các truy vấn phức tạp phục vụ báo cáo và phân tích.

---

## 3. Views (JSP)

Module sử dụng 5 file JSP để hiển thị giao diện người dùng:

| Tệp JSP | Mô tả |
|---|---|
| `list.jsp` | Trang danh sách khách hàng với tìm kiếm, lọc, phân trang |
| `add.jsp` | Form thêm mới khách hàng |
| `edit.jsp` | Form chỉnh sửa thông tin khách hàng |
| `detail.jsp` | Trang chi tiết khách hàng với lịch sử giao dịch |
| `ranking.jsp` | Trang xếp hạng và phân loại khách hàng VIP |

Các view được xây dựng với template động, sử dụng JSP và JSTL cho việc render dữ liệu động từ server.

---

## 4. Cơ sở dữ liệu

### 4.1. Bảng chính: `customer`

Bảng lưu trữ thông tin cơ bản của khách hàng bao gồm:

- Thông tin cá nhân (họ tên, ngày sinh, giới tính)
- Thông tin liên hệ (email, số điện thoại, địa chỉ)
- Thông tin tài khoản (username, mật khẩu mã hóa - nếu có)
- Trạng thái tài khoản và thời điểm tham gia

### 4.2. Bảng điểm tích lũy: `customer_point`

Bảng theo dõi điểm loyalty của khách hàng:

| Trường | Mô tả |
|---|---|
| `cus_id` | Khóa ngoại tham chiếu đến customer |
| `current_points` | Số điểm hiện tại khả dụng để mua sắm |
| `updated_at` | Thời điểm cập nhật cuối |

### 4.3. Bảng giao dịch điểm: `point_transaction`

Bảng ghi nhận chi tiết các giao dịch liên quan đến điểm:

| Trường | Mô tả |
|---|---|
| `transaction_id` | Mã giao dịch |
| `customer_id` | Khóa ngoại đến customer |
| `order_id` | Mã đơn hàng liên quan (nếu có) |
| `points` | Số điểm (+/-) |
| `transaction_type` | Loại giao dịch (earn/redeem/expire/adjust) |
| `description` | Mô tả giao dịch |
| `created_at` | Thời điểm giao dịch |

---

## 5. Tính năng chính

### 5.1. Quản lý hồ sơ khách hàng (CRUD)

- **Tạo mới**: Đăng ký khách hàng mới với thông tin cơ bản
- **Xem danh sách**: Liệt kê khách hàng với phân trang, tìm kiếm theo tên/số điện thoại/email
- **Xem chi tiết**: Hiển thị thông tin đầy đủ kèm lịch sử giao dịch
- **Cập nhật**: Chỉnh sửa thông tin khách hàng
- **Xóa/Vô hiệu hóa**: Soft delete hoặc deactivate tài khoản

### 5.2. Quản lý điểm tích lũy (Loyalty Points)

- **Tích điểm**: Tự động tích điểm khi mua hàng (theo tỷ lệ quy định)
- **Đổi điểm**: Cho phép khách hàng sử dụng điểm để giảm giá
- **Hết hạn điểm**: Xử lý điểm hết hạn theo chính sách
- **Điều chỉnh điểm**: Admin có thể điều chỉnh điểm thủ công

### 5.3. Xếp hạng khách hàng (Loyalty Ranking)

Hệ thống phân loại khách hàng theo các cấp bậc dựa trên tổng điểm tích lũy hoặc tổng chi tiêu:

| Cấp bậc | Yêu cầu | Quyền lợi |
|---|---|---|
| Bronze | 0 - 999 điểm | Tích 1% giá trị |
| Silver | 1000 - 4999 điểm | Tích 1.5% giá trị |
| Gold | 5000 - 9999 điểm | Tích 2% giá trị |
| Platinum | 10000+ điểm | Tích 3% giá trị + ưu tiên |

---

## 6. Quy tắc nghiệp vụ

### 6.1. Tích điểm

- Mỗi 10,000 VND giá trị đơn hàng = 1 điểm (tỷ lệ có thể điều chỉnh)
- Điểm chỉ được tích sau khi đơn hàng hoàn thành (không tích khi đang xử lý)
- Điểm bị hoàn lại nếu đơn hàng bị hủy

### 6.2. Đổi điểm

- 100 điểm = 1,000 VND giảm giá (tỷ lệ cố định)
- Tối thiểu 500 điểm để được đổi
- Điểm sử dụng sẽ bị trừ khỏi tài khoản ngay lập tức

### 6.3. Hết hạn điểm

- Điểm có thời hạn sử dụng 12 tháng kể từ ngày tích
- Điểm hết hạn được xử lý tự động hàng tháng

---

## 7. Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| CustomerController | Skeleton | Chỉ có routing |
| Customer.java | Skeleton | Model định nghĩa cơ bản |
| CustomerDAO.java | Skeleton | Chưa triển khai SQL |
| Views (5 files) | Template | Template HTML, chưa kết nối backend |

**Đánh giá tổng thể:** Module đang ở mức Skeleton - đã có cấu trúc cơ bản nhưng chưa triển khai logic nghiệp vụ.

---

## 8. Phụ thuộc module

- **Order Module**: Khách hàng được liên kết với đơn hàng; lịch sử mua hàng ảnh hưởng đến xếp hạng
- **Payment Module**: Thanh toán liên quan đến tích điểm và đổi điểm


---

## 9. Lưu ý bảo mật

- Thông tin cá nhân khách hàng cần được bảo vệ theo quy định về dữ liệu cá nhân
- Mật khẩu (nếu có) phải được mã hóa sử dụng bcrypt hoặc tương đương
- Các thao tác điều chỉnh điểm cần được ghi log đầy đủ

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

# Order Module - FinoraRetail

## 1. Tổng quan Module

Module Order (Quản lý Đơn hàng) là một trong những module cốt lõi của hệ thống FinoraRetail, chịu trách nhiệm quản lý toàn bộ quy trình xử lý đơn hàng từ khi khách hàng đặt hàng cho đến khi hoàn thành giao dịch. Module này bao gồm việc tạo đơn hàng, theo dõi trạng thái, xử lý thanh toán, quản lý chi tiết sản phẩm trong đơn, và xử lý hủy đơn hàng.

Trong hệ thống bán lẻ, Order Module đóng vai trò trung tâm vì nó liên quan trực tiếp đến hầu hết các module khác: Customer Module (thông tin khách hàng, điểm tích lũy), Product Module (sản phẩm trong đơn), Inventory Module (cập nhật tồn kho), Payment Module (xử lý thanh toán). Quy trình đơn hàng là cầu nối chính giữa các hoạt động kinh doanh và quản lý nội bộ.

---

## 2. Thông tin kỹ thuật

### 2.1. Route và Controller

| Thuộc tính | Chi tiết |
|---|---|
| **Route chính** | `/orders/*` |
| **Controller** | `OrderController.java` |
| **Package** | `controller.order` |
| **Trạng thái** | Skeleton |

Controller sử dụng Front Controller pattern để điều hướng các request liên quan đến đơn hàng. Hiện tại, controller chỉ đóng vai trò định tuyến cơ bản mà chưa triển khai logic nghiệp vụ.

### 2.2. Models

| Thuộc tính | Chi tiết |
|---|---|
| **Order.java** | Model cho thông tin đơn hàng chính |
| **OrderDetail.java** | Model cho chi tiết sản phẩm trong đơn hàng |
| **Package** | `model.order` |

**Order Model - các trường chính:**

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `orderId` | Integer | Khóa chính |
| `customerId` | Integer | Khóa ngoại đến customer |
| `orderDate` | Timestamp | Thời điểm tạo đơn |
| `totalAmount` | BigDecimal | Tổng giá trị đơn hàng |
| `discountAmount` | BigDecimal | Số tiền giảm giá |
| `finalAmount` | BigDecimal | Số tiền phải trả |
| `status` | String | Trạng thái đơn hàng |
| `paymentStatus` | String | Trạng thái thanh toán |
| `paymentMethod` | String | Phương thức thanh toán |
| `createdBy` | Integer | Người tạo đơn |
| `createdAt` | Timestamp | Thời điểm tạo |
| `updatedAt` | Timestamp | Thời điểm cập nhật cuối |

**OrderDetail Model - các trường chính:**

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `detailId` | Integer | Khóa chính |
| `orderId` | Integer | Khóa ngoại đến order |
| `productId` | Integer | Khóa ngoại đến product |
| `productName` | String | Tên sản phẩm (snapshot) |
| `quantity` | Integer | Số lượng |
| `unitPrice` | BigDecimal | Đơn giá tại thời điểm đặt |
| `discount` | BigDecimal | Giảm giá cho sản phẩm này |
| `subtotal` | BigDecimal | Thành tiền |

### 2.3. DAOs (Data Access Objects)

| Thuộc tính | Chi tiết |
|---|---|
| **OrderDAO.java** | Truy cập dữ liệu đơn hàng chính |
| **OrderDetailDAO.java** | Truy cập dữ liệu chi tiết đơn hàng |
| **Package** | `dao.order` |
| **Trạng thái** | Skeleton (chưa triển khai SQL) |

---

## 3. Views (JSP)

Module sử dụng 5 file JSP để hiển thị giao diện người dùng:

| Tệp JSP | Mô tả |
|---|---|
| `list.jsp` | Trang danh sách đơn hàng với tìm kiếm, lọc theo trạng thái, phân trang |
| `create.jsp` | Form tạo mới đơn hàng (POS interface) |
| `detail.jsp` | Trang chi tiết đơn hàng với thông tin đầy đủ |
| `update.jsp` | Form cập nhật thông tin/trạng thái đơn hàng |
| `cancel.jsp` | Form xác nhận hủy đơn hàng với lý do |

---

## 4. Cơ sở dữ liệu

### 4.1. Bảng chính: `[order]`

Tên bảng sử dụng dấu ngoặc vuông để tránh xung đột với từ khóa SQL:

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `order_id` | INT | Khóa chính, tự động tăng |
| `customer_id` | INT | Khóa ngoại đến customer |
| `order_date` | DATETIME | Thời điểm tạo đơn |
| `subtotal` | DECIMAL(18,2) | Tổng phụ (trước giảm giá) |
| `tax_amount` | DECIMAL(18,2) | Thuế (nếu có) |
| `discount_amount` | DECIMAL(18,2) | Giảm giá |
| `total_amount` | DECIMAL(18,2) | Tổng cộng |
| `points_used` | INT | Điểm tích lũy đã sử dụng |
| `points_earned` | INT | Điểm tích lũy được nhận |
| `payment_method` | VARCHAR(50) | Phương thức thanh toán |
| `payment_status` | VARCHAR(20) | Trạng thái thanh toán |
| `order_status` | VARCHAR(20) | Trạng thái đơn hàng |
| `shipping_address` | NVARCHAR(500) | Địa chỉ giao hàng |
| `notes` | NVARCHAR(1000) | Ghi chú đơn hàng |
| `created_by` | INT | Nhân viên tạo đơn |
| `created_at` | DATETIME | Thời điểm tạo |
| `updated_at` | DATETIME | Thời điểm cập nhật |

### 4.2. Bảng chi tiết: `order_detail`

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `detail_id` | INT | Khóa chính |
| `order_id` | INT | Khóa ngoại đến order |
| `product_id` | INT | Khóa ngoại đến product |
| `product_name` | NVARCHAR(255) | Tên sản phẩm (snapshot) |
| `quantity` | INT | Số lượng |
| `unit_price` | DECIMAL(18,2) | Đơn giá |
| `discount_amount` | DECIMAL(18,2) | Giảm giá |
| `subtotal` | DECIMAL(18,2) | Thành tiền |

### 4.3. Bảng liên quan

| Bảng | Mối quan hệ | Mô tả |
|---|---|---|
| `payment` | order_id | Thông tin thanh toán chi tiết |
| `point_transaction` | order_id | Giao dịch tích/đổi điểm |


---

## 5. Quy trình nghiệp vụ (Order Workflow)

### 5.1. Các trạng thái đơn hàng

```
Tạo mới (Created) 
    → Đang xử lý (Processing) 
    → Đã thanh toán (Paid) 
    → Đang giao hàng (Shipping) 
    → Hoàn thành (Completed)
    
           ↓ (nếu hủy)
    → Đã hủy (Cancelled)
```

| Trạng thái | Mã | Mô tả |
|---|---|---|
| Tạo mới | `created` | Đơn hàng vừa được tạo, chưa xác nhận |
| Đang xử lý | `processing` | Đơn đã được xác nhận, đang chuẩn bị |
| Đã thanh toán | `paid` | Thanh toán thành công |
| Đang giao | `shipping` | Đơn hàng đang được vận chuyển |
| Hoàn thành | `completed` | Đơn hàng đã giao và xác nhận |
| Đã hủy | `cancelled` | Đơn hàng bị hủy |

### 5.2. Quy trình xử lý chi tiết

#### Bước 1: Tạo đơn hàng (Create)

1. Khách hàng chọn sản phẩm và thêm vào giỏ hàng
2. Nhập thông tin giao hàng (nếu chưa có)
3. Áp dụng mã giảm giá (nếu có)
4. Chọn phương thức thanh toán
5. Xác nhận và tạo đơn hàng
6. Hệ thống tạo bản ghi order và order_detail
7. Cập nhật trạng thái: `created`

#### Bước 2: Xử lý đơn hàng (Process)

1. Nhân viên xác nhận đơn hàng
2. Kiểm tra và giữ sản phẩm trong kho
3. Cập nhật trạng thái: `processing`
4. Trừ tồn kho tạm thời (reserved inventory)

#### Bước 3: Thanh toán (Payment)

1. Khách hàng thực hiện thanh toán
2. Hệ thống xác nhận thanh toán
3. Cập nhật payment_status: `paid`
4. Tích điểm loyalty cho khách hàng
5. Ghi nhận point_transaction

#### Bước 4: Giao hàng (Fulfillment)

1. Đóng gói và bàn giao cho đơn vị vận chuyển
2. Cập nhật trạng thái: `shipping`
3. Cập nhật tồn kho thực (actual inventory deduction)

#### Bước 5: Hoàn thành/Hủy

- **Hoàn thành (Completed)**: Khách hàng xác nhận đã nhận hàng
- **Hủy (Cancelled)**: Đơn hàng bị hủy với lý do cụ thể, hoàn tiền nếu đã thanh toán, hoàn điểm đã tích

---

## 6. Tính năng chính

### 6.1. Tạo đơn hàng

- Tạo đơn hàng mới từ POS interface
- Hỗ trợ khách hàng có tài khoản hoặc khách vãng lai
- Thêm/bớt sản phẩm trong đơn
- Áp dụng mã giảm giá
- Sử dụng điểm tích lũy để thanh toán
- Chọn phương thức thanh toán

### 6.2. Theo dõi trạng thái

- Xem danh sách đơn hàng theo trạng thái
- Cập nhật trạng thái đơn hàng
- Xem chi tiết đơn hàng
- In hóa đơn/đơn hàng

### 6.3. Xử lý hủy đơn

- Yêu cầu xác nhận với lý do hủy
- Hoàn tiền nếu đã thanh toán
- Hoàn điểm đã tích
- Khôi phục tồn kho

---

## 7. Phương thức thanh toán

| Phương thức | Mã | Mô tả |
|---|---|---|
| Tiền mặt | `cash` | Thanh toán bằng tiền mặt tại quầy |
| Chuyển khoản | `bank_transfer` | Chuyển khoản ngân hàng |
| Thẻ | `card` | Thanh toán bằng thẻ (ATM, credit) |
| Ví điện tử | `e_wallet` | Momo, ZaloPay, VNPay,... |
| Kết hợp | `combined` | Kết hợp nhiều phương thức |

---

## 8. Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| OrderController | Skeleton | Chỉ có routing |
| Order.java | Hoàn chỉnh | Model định nghĩa đầy đủ |
| OrderDetail.java | Hoàn chỉnh | Model định nghĩa đầy đủ |
| OrderDAO.java | Skeleton | Chưa triển khai SQL |
| OrderDetailDAO.java | Skeleton | Chưa triển khai SQL |
| Views (5 files) | Template | Template HTML, chưa kết nối backend |

**Đánh giá tổng thể:** Module đang ở mức Skeleton - đã có cấu trúc model và workflow đầy đủ nhưng chưa triển khai logic nghiệp vụ.

---

## 9. Phụ thuộc module

- **Customer Module**: Thông tin khách hàng, điểm loyalty
- **Product Module**: Sản phẩm trong đơn, giá, tồn kho
- **Inventory Module**: Cập nhật số lượng tồn kho
- **Payment Module**: Xử lý thanh toán, ghi nhận giao dịch


---

## 10. Lưu ý quan trọng

- **Tồn kho**: Cần cập nhật tồn kho khi đơn chuyển sang trạng thái `completed` hoặc hoàn kho khi hủy
- **Điểm loyalty**: Chỉ tích điểm khi thanh toán thành công; hoàn điểm khi hủy
- **Snapshot giá**: Lưu giá sản phẩm tại thời điểm đặt hàng (không cập nhật theo giá hiện tại)
- **Số lượng âm**: Kiểm tra tồn kho đủ trước khi tạo đơn

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

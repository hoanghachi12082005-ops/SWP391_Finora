# Payment Module - FinoraRetail

## Tổng quan Module

Module Payment (Thanh toán) chịu trách nhiệm quản lý các giao dịch thanh toán trong hệ thống FinoraRetail. Module này bao gồm ghi nhận thanh toán, theo dõi trạng thái thanh toán, và tích hợp với module hóa đơn để tạo ra các chứng từ thanh toán hoàn chỉnh cho khách hàng.

**Lưu ý:** Đây là **Protected Area**. Schema thanh toán và tài chính thuộc vùng được bảo vệ — không chỉnh sửa trừ khi có yêu cầu rõ ràng từ người dùng.

---

## Thông tin cơ bản

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Payment (Thanh toán) |
| **Trạng thái** | `Skeleton` |
| **Package** | `controller.payment`, `dao.payment`, `model.payment` |
| **Route chính** | `/payments/*`, `/invoices/*` |
| **Bảng DB** | `payment`, `order`, `invoice` |
| **Module cha** | Finance & Payment Group |

---

## Routes và Endpoints

### Payment Routes (`/payments/*`)

| Route | Method | Mô tả | Trạng thái |
|---|---|---|---|
| `/payments` | GET | Danh sách tất cả giao dịch thanh toán | Skeleton |
| `/payments/new` | GET | Form tạo thanh toán mới | Skeleton |
| `/payments/create` | POST | Xử lý tạo thanh toán | Skeleton |
| `/payments/{id}` | GET | Chi tiết một giao dịch thanh toán | Skeleton |
| `/payments/{id}/edit` | GET | Form chỉnh sửa thanh toán | Skeleton |
| `/payments/{id}/update` | POST | Xử lý cập nhật thanh toán | Skeleton |
| `/payments/{id}/delete` | POST | Xóa thanh toán | Skeleton |
| `/payments/by-order/{orderId}` | GET | Lấy thanh toán theo đơn hàng | Skeleton |
| `/payments/status/{status}` | GET | Lọc thanh toán theo trạng thái | Skeleton |

### Invoice Routes (`/invoices/*`)

| Route | Method | Mô tả | Trạng thái |
|---|---|---|---|
| `/invoices` | GET | Danh sách tất cả hóa đơn | Skeleton |
| `/invoices/new` | GET | Form tạo hóa đơn mới | Skeleton |
| `/invoices/create` | POST | Xử lý tạo hóa đơn | Skeleton |
| `/invoices/{id}` | GET | Chi tiết một hóa đơn | Skeleton |
| `/invoices/{id}/print` | GET | In hóa đơn | Skeleton |
| `/invoices/{id}/export` | GET | Xuất hóa đơn (PDF/Excel) | Skeleton |
| `/invoices/by-order/{orderId}` | GET | Lấy hóa đơn theo đơn hàng | Skeleton |

---

## Controllers

### PaymentInvoiceController

**Package:** `controller.payment`

**Tệp:** `PaymentInvoiceController.java`

**Mô tả:** Controller chính xử lý các yêu cầu liên quan đến thanh toán và hóa đơn. Sử dụng annotation `@WebServlet` để đăng ký các URL patterns.

**Trách nhiệm:**
- Điều phối request giữa payment và invoice
- Phân tích tham số request (action, id, status)
- Gọi DAO để truy xuất hoặc cập nhật dữ liệu
- Đặt attributes cho JSP và forward đến view tương ứng
- Xử lý flash messages cho feedback người dùng

**Trạng thái hiện tại:** Skeleton — chưa có logic nghiệp vụ thực tế.

```java
@WebServlet(name = "PaymentInvoiceController", urlPatterns = {
    "/payments/*", "/invoices/*"
})
public class PaymentInvoiceController extends HttpServlet {
    // Skeleton implementation
}
```

---

## Models

### Payment.java

**Package:** `model.payment`

**Mô tả:** Model đại diện cho một giao dịch thanh toán trong hệ thống.

**Thuộc tính chính:**

| Thuộc tính | Kiểu | Mô tả |
|---|---|---|
| `paymentId` | Integer | ID giao dịch thanh toán (Primary Key) |
| `orderId` | Integer | ID đơn hàng liên quan (Foreign Key) |
| `amount` | BigDecimal | Số tiền thanh toán |
| `paymentMethod` | String | Phương thức thanh toán (CASH, CARD, BANK_TRANSFER, E_WALLET) |
| `paymentStatus` | String | Trạng thái thanh toán (PENDING, COMPLETED, FAILED, REFUNDED) |
| `transactionCode` | String | Mã giao dịch từ cổng thanh toán |
| `paymentDate` | LocalDateTime | Ngày giờ thanh toán |
| `employeeId` | Integer | ID nhân viên thực hiện thanh toán |
| `notes` | String | Ghi chú thanh toán |

**Trạng thái:** Skeleton — class tồn tại với getters/setters, chưa có validation logic.

### Invoice.java

**Package:** `model.payment`

**Mô tả:** Model đại diện cho hóa đơn trong hệ thống.

**Thuộc tính chính:**

| Thuộc tính | Kiểu | Mô tả |
|---|---|---|
| `invoiceId` | Integer | ID hóa đơn (Primary Key) |
| `invoiceNumber` | String | Số hóa đơn (định dạng INV-YYYYMMDD-XXXX) |
| `orderId` | Integer | ID đơn hàng liên quan (Foreign Key) |
| `customerId` | Integer | ID khách hàng (Foreign Key) |
| `subtotal` | BigDecimal | Tổng phụ trước thuế |
| `taxAmount` | BigDecimal | Số tiền thuế |
| `discountAmount` | BigDecimal | Số tiền giảm giá |
| `totalAmount` | BigDecimal | Tổng cộng sau thuế |
| `paymentStatus` | String | Trạng thái thanh toán (PAID, UNPAID, PARTIAL) |
| `invoiceDate` | LocalDateTime | Ngày lập hóa đơn |
| `dueDate` | LocalDate | Hạn thanh toán |
| `storeId` | Integer | ID cửa hàng xuất hóa đơn |

**Trạng thái:** Skeleton.

---

## DAOs

### PaymentDAO.java

**Package:** `dao.payment`

**Mô tả:** Data Access Object xử lý các thao tác CRUD với bảng `payment` trong SQL Server.

**Interface:** Implements `ICrudDAO<Payment, Integer>`

**Các phương thức chính cần triển khai:**

| Phương thức | Mô tả | Trạng thái |
|---|---|---|
| `findAll()` | Lấy danh sách tất cả thanh toán | Skeleton |
| `findById(Integer id)` | Tìm thanh toán theo ID | Skeleton |
| `findByOrderId(Integer orderId)` | Tìm thanh toán theo đơn hàng | Skeleton |
| `findByStatus(String status)` | Lọc thanh toán theo trạng thái | Skeleton |
| `findByDateRange(LocalDateTime from, LocalDateTime to)` | Tìm thanh toán trong khoảng ngày | Skeleton |
| `findByPaymentMethod(String method)` | Lọc theo phương thức thanh toán | Skeleton |
| `insert(Payment payment)` | Tạo mới thanh toán | Skeleton |
| `update(Payment payment)` | Cập nhật thanh toán | Skeleton |
| `delete(Integer id)` | Xóa thanh toán | Skeleton |
| `countByStatus()` | Đếm thanh toán theo trạng thái | Skeleton |
| `sumAmountByDateRange(LocalDateTime from, LocalDateTime to)` | Tổng tiền thanh toán theo khoảng ngày | Skeleton |

**Trạng thái:** Skeleton — interface và stub methods tồn tại, chưa có SQL thực tế.

### InvoiceDAO.java

**Package:** `dao.payment`

**Mô tả:** Data Access Object xử lý các thao tác CRUD với bảng `invoice` trong SQL Server.

**Interface:** Implements `ICrudDAO<Invoice, Integer>`

**Các phương thức chính cần triển khai:**

| Phương thức | Mô tả | Trạng thái |
|---|---|---|
| `findAll()` | Lấy danh sách tất cả hóa đơn | Skeleton |
| `findById(Integer id)` | Tìm hóa đơn theo ID | Skeleton |
| `findByInvoiceNumber(String invoiceNumber)` | Tìm theo số hóa đơn | Skeleton |
| `findByOrderId(Integer orderId)` | Tìm hóa đơn theo đơn hàng | Skeleton |
| `findByCustomerId(Integer customerId)` | Tìm hóa đơn theo khách hàng | Skeleton |
| `findByStatus(String status)` | Lọc theo trạng thái | Skeleton |
| `findByDateRange(LocalDateTime from, LocalDateTime to)` | Tìm trong khoảng ngày | Skeleton |
| `findByStoreId(Integer storeId)` | Tìm theo cửa hàng | Skeleton |
| `generateInvoiceNumber()` | Tạo số hóa đơn tự động | Skeleton |
| `insert(Invoice invoice)` | Tạo mới hóa đơn | Skeleton |
| `update(Invoice invoice)` | Cập nhật hóa đơn | Skeleton |
| `delete(Integer id)` | Xóa hóa đơn | Skeleton |

**Trạng thái:** Skeleton.

---

## Views

### views/payments/index.jsp

**Vị trí:** `web/WEB-INF/views/payments/index.jsp`

**Mô tả:** Trang chính hiển thị danh sách các giao dịch thanh toán với các chức năng lọc và tìm kiếm.

**Các thành phần UI dự kiến:**
- Bảng danh sách thanh toán với các cột: Mã GD, Đơn hàng, Số tiền, Phương thức, Trạng thái, Ngày, Nhân viên
- Thanh tìm kiếm theo mã giao dịch, số đơn hàng
- Bộ lọc theo trạng thái (PENDING, COMPLETED, FAILED, REFUNDED)
- Bộ lọc theo phương thức thanh toán
- Bộ lọc theo khoảng ngày
- Nút tạo thanh toán mới
- Phân trang kết quả
- Tổng quan thống kê (tổng thanh toán hôm nay, tổng PENDING)

**Trạng thái:** Skeleton — file tồn tại với template HTML Bootstrap.

### views/invoices/list.jsp

**Vị trí:** `web/WEB-INF/views/invoices/list.jsp`

**Mô tả:** Trang danh sách hóa đơn với chức năng xem, in, và xuất hóa đơn.

**Các thành phần UI dự kiến:**
- Bảng danh sách hóa đơn với các cột: Số hóa đơn, Khách hàng, Tổng tiền, Trạng thái, Ngày lập, Hạn thanh toán
- Thanh tìm kiếm theo số hóa đơn, tên khách hàng
- Bộ lọc theo trạng thái (PAID, UNPAID, PARTIAL)
- Bộ lọc theo khoảng ngày
- Nút tạo hóa đơn mới
- Nút in và xuất hóa đơn (PDF/Excel)
- Phân trang

**Trạng thái:** Skeleton.

---

## Database Schema

### Bảng `payment`

```sql
CREATE TABLE payment (
    payment_id INT PRIMARY KEY IDENTITY,
    order_id INT NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    payment_method NVARCHAR(50) NOT NULL,  -- CASH, CARD, BANK_TRANSFER, E_WALLET
    payment_status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING, COMPLETED, FAILED, REFUNDED
    transaction_code NVARCHAR(100),
    payment_date DATETIME NOT NULL DEFAULT GETDATE(),
    employee_id INT,
    notes NVARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);
```

### Bảng `invoice` (tương lai)

```sql
CREATE TABLE invoice (
    invoice_id INT PRIMARY KEY IDENTITY,
    invoice_number NVARCHAR(50) NOT NULL UNIQUE,
    order_id INT NOT NULL,
    customer_id INT,
    subtotal DECIMAL(18, 2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    payment_status NVARCHAR(50) NOT NULL DEFAULT 'UNPAID',  -- PAID, UNPAID, PARTIAL
    invoice_date DATETIME NOT NULL DEFAULT GETDATE(),
    due_date DATE,
    store_id INT,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (store_id) REFERENCES stores(store_id)
);
```

---

## Tính năng chính

### 1. Ghi nhận thanh toán (Payment Recording)

- Tạo giao dịch thanh toán liên kết với đơn hàng
- Hỗ trợ nhiều phương thức: Tiền mặt, Thẻ, Chuyển khoản, Ví điện tử
- Tự động ghi nhận ngày giờ, nhân viên thực hiện
- Sinh mã giao dịch duy nhất từ cổng thanh toán

### 2. Theo dõi trạng thái thanh toán (Payment Status Tracking)

- Trạng thái: PENDING → COMPLETED / FAILED / REFUNDED
- Cập nhật trạng thái khi có phản hồi từ cổng thanh toán
- Thông báo khi thanh toán thất bại hoặc cần hoàn tiền
- Lịch sử thay đổi trạng thái theo thời gian

### 3. Tạo hóa đơn (Invoice Generation)

- Tự động tạo hóa đơn khi thanh toán hoàn tất (tùy cấu hình)
- Sinh số hóa đơn theo định dạng: INV-YYYYMMDD-XXXX
- Tính toán thuế, giảm giá, tổng cộng
- Hỗ trợ hóa đơn gốc, hóa đơn điều chỉnh

### 4. In và xuất hóa đơn

- In hóa đơn với định dạng chuẩn
- Xuất hóa đơn ra PDF
- Xuất danh sách hóa đơn ra Excel

---

## Phụ thuộc vào Module khác

| Module | Mối quan hệ |
|---|---|
| **Order Module** | Payment gắn với Order; Invoice gắn với Order. Cần OrderDAO để lấy thông tin đơn hàng |
| **Customer Module** | Invoice liên kết với Customer. Cần CustomerDAO để lấy thông tin khách hàng |
| **Store Module** | Invoice gắn với Store. Cần StoreDAO để lấy thông tin cửa hàng xuất hóa đơn |
| **Employee Module** | Payment gắn với Employee. Cần EmployeeDAO để lấy thông tin nhân viên |
| **Export Module** | Sử dụng ExportUtil để xuất hóa đơn PDF/Excel |

---

## Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| PaymentController | Skeleton | Cần triển khai business logic |
| InvoiceController | Skeleton | Cần triển khai business logic |
| Payment.java (Model) | Skeleton | Cần validation |
| Invoice.java (Model) | Skeleton | Cần validation |
| PaymentDAO | Skeleton | Cần triển khai SQL |
| InvoiceDAO | Skeleton | Cần triển khai SQL |
| PaymentService | Skeleton | Cần triển khai service layer |
| views/payments/index.jsp | Skeleton | UI template sẵn có |
| views/invoices/list.jsp | Skeleton | UI template sẵn có |

---

## Open Questions

1. Cổng thanh toán nào sẽ được tích hợp (VNPay, MoMo, Stripe)?
2. Hóa đơn có cần tích hợp với hệ thống thuế điện tử không?
3. Có cần hỗ trợ thanh toán trả góp không?
4. Quy tắc tạo số hóa đơn có cần định dạng theo quy định Việt Nam không?
5. Module thanh toán có cần webhook handler cho payment gateway không?

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

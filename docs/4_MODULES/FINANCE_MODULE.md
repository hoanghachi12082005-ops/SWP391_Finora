# Finance Module - FinoraRetail

## Tổng quan Module

Module Finance (Tài chính) là module tổng hợp quản lý thu nhập và chi phí của hệ thống FinoraRetail. Module này cung cấp khả năng theo dõi dòng tiền, phân loại thu chi, và tổng hợp báo cáo tài chính cơ bản phục vụ cho việc ra quyết định quản lý.

**Lưu ý:** Đây là **Protected Area**. Schema tài chính và thanh toán thuộc vùng được bảo vệ — không chỉnh sửa trừ khi có yêu cầu rõ ràng từ người dùng.

---

## Thông tin cơ bản

| Thuộc tính | Chi tiết |
|---|---|
| **Tên module** | Finance (Tài chính) |
| **Trạng thái** | `Skeleton` |
| **Package** | `controller.finance`, `dao.finance`, `model.finance` |
| **Route chính** | `/income/*`, `/expenses/*` |
| **Bảng DB** | Thu nhập từ dữ liệu order, Chi phí (cấu trúc bảng đang chờ) |
| **Module cha** | Finance & Payment Group |

---

## Routes và Endpoints

### Income Routes (`/income/*`)

| Route | Method | Mô tả | Trạng thái |
|---|---|---|---|
| `/income` | GET | Danh sách tất cả khoản thu nhập | Skeleton |
| `/income/new` | GET | Form tạo khoản thu nhập mới | Skeleton |
| `/income/create` | POST | Xử lý tạo khoản thu nhập | Skeleton |
| `/income/{id}` | GET | Chi tiết một khoản thu nhập | Skeleton |
| `/income/{id}/edit` | GET | Form chỉnh sửa | Skeleton |
| `/income/{id}/update` | POST | Xử lý cập nhật | Skeleton |
| `/income/{id}/delete` | POST | Xóa khoản thu nhập | Skeleton |
| `/income/by-date` | GET | Lọc thu nhập theo khoảng ngày | Skeleton |
| `/income/by-type` | GET | Lọc theo loại thu nhập | Skeleton |
| `/income/summary` | GET | Tổng hợp thu nhập | Skeleton |

### Expense Routes (`/expenses/*`)

| Route | Method | Mô tả | Trạng thái |
|---|---|---|---|
| `/expenses` | GET | Danh sách tất cả chi phí | Skeleton |
| `/expenses/new` | GET | Form tạo chi phí mới | Skeleton |
| `/expenses/create` | POST | Xử lý tạo chi phí | Skeleton |
| `/expenses/{id}` | GET | Chi tiết một khoản chi phí | Skeleton |
| `/expenses/{id}/edit` | GET | Form chỉnh sửa | Skeleton |
| `/expenses/{id}/update` | POST | Xử lý cập nhật | Skeleton |
| `/expenses/{id}/delete` | POST | Xóa chi phí | Skeleton |
| `/expenses/by-date` | GET | Lọc chi phí theo khoảng ngày | Skeleton |
| `/expenses/by-category` | GET | Lọc theo loại chi phí | Skeleton |
| `/expenses/by-store` | GET | Lọc theo cửa hàng | Skeleton |
| `/expenses/summary` | GET | Tổng hợp chi phí | Skeleton |

---

## Controllers

### IncomeExpenseController

**Package:** `controller.finance`

**Tệp:** `IncomeExpenseController.java`

**Mô tả:** Controller trung tâm xử lý các yêu cầu liên quan đến thu nhập và chi phí. Controller này tách biệt hai nhánh nghiệp vụ thu (income) và chi (expense) trong cùng một servlet dựa trên URL path.

**Trách nhiệm:**
- Điều phối request giữa income và expense dựa trên URL
- Phân tích tham số request (action, id, dateFrom, dateTo, type, categoryId)
- Gọi IncomeDAO và ExpenseDAO để truy xuất dữ liệu
- Tính toán tổng hợp tài chính (tổng thu, tổng chi, lợi nhuận gộp)
- Đặt attributes cho JSP và forward đến view tương ứng
- Xử lý flash messages

**Trạng thái hiện tại:** Skeleton — routing và forward tồn tại, logic nghiệp vụ chưa triển khai.

```java
@WebServlet(name = "IncomeExpenseController", urlPatterns = {
    "/income/*", "/expenses/*"
})
public class IncomeExpenseController extends HttpServlet {
    // Skeleton implementation
}
```

---

## Models

### Income.java

**Package:** `model.finance`

**Mô tả:** Model đại diện cho một khoản thu nhập trong hệ thống.

**Thuộc tính chính:**

| Thuộc tính | Kiểu | Mô tả |
|---|---|---|
| `incomeId` | Integer | ID khoản thu (Primary Key) |
| `incomeType` | String | Loại thu nhập (SALE, SERVICE, REFUND, OTHER) |
| `amount` | BigDecimal | Số tiền thu |
| `description` | String | Mô tả khoản thu |
| `sourceId` | Integer | ID nguồn thu (order_id nếu từ bán hàng) |
| `sourceType` | String | Loại nguồn (ORDER, MANUAL, OTHER) |
| `incomeDate` | LocalDateTime | Ngày ghi nhận thu |
| `storeId` | Integer | ID cửa hàng ghi nhận thu |
| `employeeId` | Integer | ID nhân viên ghi nhận |
| `categoryId` | Integer | ID loại thu nhập |
| `createdAt` | LocalDateTime | Thời điểm tạo |

**Trạng thái:** Skeleton.

### Expense.java

**Package:** `model.finance`

**Mô tả:** Model đại diện cho một khoản chi phí trong hệ thống.

**Thuộc tính chính:**

| Thuộc tính | Kiểu | Mô tả |
|---|---|---|
| `expenseId` | Integer | ID khoản chi (Primary Key) |
| `expenseType` | String | Loại chi phí (OPERATING, SALARY, RENT, UTILITY, SUPPLY, MARKETING, OTHER) |
| `amount` | BigDecimal | Số tiền chi |
| `description` | String | Mô tả khoản chi |
| `expenseDate` | LocalDateTime | Ngày ghi nhận chi |
| `dueDate` | LocalDate | Hạn thanh toán (nếu có) |
| `storeId` | Integer | ID cửa hàng phát sinh chi |
| `employeeId` | Integer | ID nhân viên ghi nhận |
| `categoryId` | Integer | ID loại chi phí |
| `supplierId` | Integer | ID nhà cung cấp (nếu liên quan) |
| `receiptImage` | String | Đường dẫn ảnh hóa đơn chi |
| `status` | String | Trạng thái (PENDING, APPROVED, PAID) |
| `createdAt` | LocalDateTime | Thời điểm tạo |

**Trạng thái:** Skeleton.

---

## DAOs

### IncomeDAO.java

**Package:** `dao.finance`

**Mô tả:** Data Access Object xử lý các thao tác CRUD với dữ liệu thu nhập trong SQL Server.

**Interface:** Implements `ICrudDAO<Income, Integer>`

**Các phương thức chính cần triển khai:**

| Phương thức | Mô tả | Trạng thái |
|---|---|---|
| `findAll()` | Lấy danh sách tất cả thu nhập | Skeleton |
| `findById(Integer id)` | Tìm theo ID | Skeleton |
| `findByType(String type)` | Lọc theo loại thu nhập | Skeleton |
| `findByDateRange(LocalDateTime from, LocalDateTime to)` | Tìm trong khoảng ngày | Skeleton |
| `findByStoreId(Integer storeId)` | Tìm theo cửa hàng | Skeleton |
| `findBySourceId(Integer sourceId, String sourceType)` | Tìm theo nguồn | Skeleton |
| `sumAmountByDateRange(LocalDateTime from, LocalDateTime to)` | Tổng thu theo khoảng ngày | Skeleton |
| `sumAmountByType(String type, LocalDateTime from, LocalDateTime to)` | Tổng theo loại | Skeleton |
| `insert(Income income)` | Tạo mới | Skeleton |
| `update(Income income)` | Cập nhật | Skeleton |
| `delete(Integer id)` | Xóa | Skeleton |

**Chiến lược dữ liệu:** Thu nhập chủ yếu được tính toán từ dữ liệu `orders` và `payments`. IncomeDAO sẽ:
1. Đọc trực tiếp từ bảng `orders` với trạng thái COMPLETED
2. Đọc từ bảng `payments` với trạng thái COMPLETED
3. Hỗ trợ ghi nhận thu nhập thủ công (không từ đơn hàng)

**Trạng thái:** Skeleton.

### ExpenseDAO.java

**Package:** `dao.finance`

**Mô tả:** Data Access Object xử lý các thao tác CRUD với dữ liệu chi phí trong SQL Server.

**Interface:** Implements `ICrudDAO<Expense, Integer>`

**Các phương thức chính cần triển khai:**

| Phương thức | Mô tả | Trạng thái |
|---|---|---|
| `findAll()` | Lấy danh sách tất cả chi phí | Skeleton |
| `findById(Integer id)` | Tìm theo ID | Skeleton |
| `findByType(String type)` | Lọc theo loại chi phí | Skeleton |
| `findByCategoryId(Integer categoryId)` | Lọc theo loại | Skeleton |
| `findByDateRange(LocalDateTime from, LocalDateTime to)` | Tìm trong khoảng ngày | Skeleton |
| `findByStoreId(Integer storeId)` | Tìm theo cửa hàng | Skeleton |
| `findByStatus(String status)` | Lọc theo trạng thái | Skeleton |
| `sumAmountByDateRange(LocalDateTime from, LocalDateTime to)` | Tổng chi theo khoảng ngày | Skeleton |
| `sumAmountByType(String type, LocalDateTime from, LocalDateTime to)` | Tổng theo loại | Skeleton |
| `insert(Expense expense)` | Tạo mới | Skeleton |
| `update(Expense expense)` | Cập nhật | Skeleton |
| `delete(Integer id)` | Xóa | Skeleton |

**Trạng thái:** Skeleton — cấu trúc bảng đang chờ định nghĩa.

---

## Views

### views/income/list.jsp

**Vị trí:** `web/WEB-INF/views/income/list.jsp`

**Mô tả:** Trang danh sách thu nhập với các chức năng lọc, thống kê tổng quan.

**Các thành phần UI dự kiến:**
- Thẻ thống kê: Tổng thu hôm nay, Tổng thu tháng, Tổng thu theo loại
- Bảng danh sách thu nhập: Mã, Loại, Số tiền, Mô tả, Ngày, Cửa hàng, Nguồn
- Bộ lọc theo khoảng ngày
- Bộ lọc theo loại thu nhập
- Bộ lọc theo cửa hàng
- Thanh tìm kiếm
- Nút tạo thu nhập thủ công

**Trạng thái:** Skeleton.

### views/expenses/list.jsp

**Vị trí:** `web/WEB-INF/views/expenses/list.jsp`

**Mô tả:** Trang danh sách chi phí với các chức năng lọc và duyệt chi.

**Các thành phần UI dự kiến:**
- Thẻ thống kê: Tổng chi hôm nay, Tổng chi tháng, Tổng chi theo loại
- Bảng danh sách chi phí: Mã, Loại, Số tiền, Mô tả, Ngày, Hạn, Trạng thái, Cửa hàng
- Bộ lọc theo khoảng ngày
- Bộ lọc theo loại chi phí
- Bộ lọc theo cửa hàng
- Bộ lọc theo trạng thái (PENDING, APPROVED, PAID)
- Thanh tìm kiếm
- Nút tạo chi phí mới
- Nút duyệt chi cho PENDING

**Trạng thái:** Skeleton.

### views/expenses/add.jsp

**Vị trí:** `web/WEB-INF/views/expenses/add.jsp`

**Mô tả:** Form tạo chi phí mới với các trường thông tin đầy đủ.

**Các thành phần UI dự kiến:**
- Form nhập: Loại chi phí, Số tiền, Mô tả, Ngày chi, Hạn thanh toán, Cửa hàng, Loại chi tiết
- Upload hình ảnh hóa đơn
- Nút lưu và hủy

**Trạng thái:** Skeleton.

---

## Database Schema

### Bảng `expense` (đang chờ định nghĩa)

```sql
-- Cấu trúc dự kiến - chưa triển khai trong schema
CREATE TABLE expense (
    expense_id INT PRIMARY KEY IDENTITY,
    expense_type NVARCHAR(50) NOT NULL,  -- OPERATING, SALARY, RENT, UTILITY, SUPPLY, MARKETING, OTHER
    amount DECIMAL(18, 2) NOT NULL,
    description NVARCHAR(500),
    expense_date DATETIME NOT NULL DEFAULT GETDATE(),
    due_date DATE,
    store_id INT,
    employee_id INT,
    category_id INT,
    supplier_id INT,
    receipt_image NVARCHAR(255),
    status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING, APPROVED, PAID
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (store_id) REFERENCES stores(store_id),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);
```

### Nguồn dữ liệu thu nhập

Thu nhập không lưu trong bảng riêng mà được tính toán từ:

1. **Bảng `orders`** — tổng `total_amount` của các đơn hàng có `status = 'COMPLETED'`
2. **Bảng `payments`** — các thanh toán có `payment_status = 'COMPLETED'`
3. **Bảng `order_details`** — chi tiết bán hàng theo sản phẩm

```sql
-- Ví dụ query tính thu nhập từ đơn hàng
SELECT 
    o.order_id,
    o.total_amount,
    o.order_date,
    o.store_id,
    o.status
FROM orders o
WHERE o.status = 'COMPLETED'
    AND o.order_date BETWEEN @fromDate AND @toDate;
```

---

## Tính năng chính

### 1. Theo dõi thu nhập (Income Tracking)

- Tự động tính thu nhập từ đơn hàng hoàn tất
- Phân loại thu nhập theo loại (bán hàng, dịch vụ, hoàn tiền, khác)
- Tổng hợp thu nhập theo khoảng thời gian, cửa hàng, loại
- Hỗ trợ ghi nhận thu nhập thủ công (phí dịch vụ, thu khác)

### 2. Theo dõi chi phí (Expense Tracking)

- Ghi nhận chi phí hoạt động hàng ngày
- Phân loại chi phí (vận hành, lương, thuê mặt bằng, điện nước, vật tư, marketing)
- Quy trình duyệt chi (PENDING → APPROVED → PAID)
- Upload hình ảnh hóa đơn chi cho minh chứng

### 3. Tổng hợp tài chính (Financial Summary)

- Tính lợi nhuận gộp = Tổng thu - Tổng chi
- Báo cáo thu chi theo ngày, tuần, tháng, quý
- So sánh hiệu suất giữa các cửa hàng
- Biểu đồ dòng tiền theo thời gian

### 4. Phân tích thu chi (Income/Expense Analysis)

- Tỷ lệ chi phí trên doanh thu
- Top chi phí lớn nhất
- Xu hướng thu chi theo thời gian

---

## Phụ thuộc vào Module khác

| Module | Mối quan hệ |
|---|---|
| **Order Module** | Nguồn dữ liệu thu nhập chính. Cần OrderDAO để tính tổng thu |
| **Payment Module** | Xác nhận thanh toán hoàn tất để ghi nhận thu |
| **Store Module** | Phân bổ thu chi theo cửa hàng. Cần StoreDAO |
| **Employee Module** | Người ghi nhận giao dịch. Cần EmployeeDAO |
| **Supplier Module** | Chi phí liên quan đến nhà cung cấp. Cần SupplierDAO |
| **Report Module** | Sử dụng dữ liệu tài chính để tạo báo cáo tổng hợp |

---

## Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| IncomeExpenseController | Skeleton | Cần triển khai business logic |
| Income.java (Model) | Skeleton | Cần validation |
| Expense.java (Model) | Skeleton | Cần validation |
| IncomeDAO | Skeleton | Cần triển khai SQL, dựa trên orders table |
| ExpenseDAO | Skeleton | Cần định nghĩa schema và triển khai SQL |
| IncomeService | Skeleton | Cần triển khai service layer |
| ExpenseService | Skeleton | Cần triển khai service layer |
| views/income/list.jsp | Skeleton | UI template sẵn có |
| views/expenses/list.jsp | Skeleton | UI template sẵn có |
| views/expenses/add.jsp | Skeleton | UI template sẵn có |

---

## Open Questions

1. Chi phí có cần quy trình duyệt nhiều cấp (manager duyệt) không?
2. Có cần tích hợp với module kế toán không?
3. Bảng expense có cần định nghĩa trong schema SQL hay chỉ cần DAO trên bảng có sẵn?
4. Có cần hỗ trợ chi phí định kỳ (hàng tháng tự động) không?
5. Thu nhập từ đơn hàng có cần tạo record trong bảng income hay chỉ query trực tiếp?

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

# Product Module - FinoraRetail

## 1. Tổng quan Module

Module Product (Quản lý Sản phẩm) là một trong những module cốt lõi của hệ thống FinoraRetail, chịu trách nhiệm quản lý toàn bộ thông tin về sản phẩm trong danh mục hàng hóa của doanh nghiệp. Module này cung cấp các chức năng CRUD (Create, Read, Update, Delete) cho sản phẩm, quản lý hình ảnh, phân loại theo danh mục, thiết lập giá bán, và theo dõi số lượng tồn kho.

Trong kiến trúc hệ thống, Product Module đóng vai trò trung tâm vì nó liên quan trực tiếp đến nhiều module khác như Category Module (phân loại sản phẩm), Inventory Module (quản lý tồn kho), Order Module (tạo đơn hàng), và Supplier Module (nhập hàng từ nhà cung cấp). Dữ liệu sản phẩm là nền tảng cho hầu hết các nghiệp vụ trong hệ thống bán lẻ.

---

## 2. Thông tin kỹ thuật

### 2.1. Route và Controller

| Thuộc tính | Chi tiết |
|---|---|
| **Route chính** | `/products/*` |
| **Controller** | `ProductController.java` |
| **Package** | `controller.product` |
| **Trạng thái** | Skeleton (chỉ có routing, chưa triển khai logic thực) |

Controller sử dụng kiểu Front Controller pattern, điều hướng các request dựa trên action parameter hoặc path info. Hiện tại, `ProductController` chỉ đóng vai trò định tuyến request đến các view tương ứng mà chưa có logic xử lý nghiệp vụ thực sự.

### 2.2. Model (POJO)

| Thuộc tính | Chi tiết |
|---|---|
| **Tệp** | `Product.java` |
| **Package** | `model.product` |

**Các trường dữ liệu:**

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `productId` | Integer | Khóa chính, tự động tăng |
| `productName` | String | Tên sản phẩm |
| `quantity` | Integer | Số lượng tồn kho hiện tại |
| `categoryId` | Integer | Khóa ngoại tham chiếu đến bảng category |
| `sellingPrice` | BigDecimal | Giá bán của sản phẩm |
| `status` | String | Trạng thái sản phẩm (active/inactive) |
| `createdAt` | Timestamp | Thời điểm tạo sản phẩm |
| `updatedAt` | Timestamp | Thời điểm cập nhật cuối cùng |

### 2.3. DAO (Data Access Object)

| Thuộc tính | Chi tiết |
|---|---|
| **Tệp** | `ProductDAO.java` |
| **Package** | `dao.product` |
| **Trạng thái** | Skeleton (chưa triển khai SQL) |

DAO cung cấp các phương thức truy cập cơ sở dữ liệu chuẩn hóa cho Product Model. Các phương thức cần triển khai bao gồm CRUD operations, tìm kiếm, phân trang, và các truy vấn liên quan đến danh mục.

---

## 3. Views (JSP)

Module sử dụng 7 file JSP để hiển thị giao diện người dùng, đặt trong thư mục `views/products/`:

| Tệp JSP | Mô tả |
|---|---|
| `list.jsp` | Trang danh sách sản phẩm với chức năng tìm kiếm, lọc, phân trang |
| `add.jsp` | Form thêm mới sản phẩm |
| `edit.jsp` | Form chỉnh sửa thông tin sản phẩm |
| `detail.jsp` | Trang chi tiết sản phẩm |
| `stock-adjustment.jsp` | Trang điều chỉnh số lượng tồn kho |
| `import-receipt.jsp` | Trang tạo phiếu nhập hàng |
| `showcase.jsp` | Trang trưng bày sản phẩm (dành cho POS/hiển thị cửa hàng) |

Tất cả các view đều sử dụng template động với server-side rendering thông qua JSP và JSTL. Các view này là template HTML tĩnh, chưa có kết nối backend thực sự.

---

## 4. Cơ sở dữ liệu

### 4.1. Bảng chính: `product`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `product_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Mã sản phẩm |
| `product_name` | NVARCHAR(255) | NOT NULL | Tên sản phẩm |
| `quantity` | INT | DEFAULT 0 | Số lượng tồn kho |
| `category_id` | INT | FOREIGN KEY | Mã danh mục |
| `unit_id` | INT | FOREIGN KEY | Mã đơn vị tính |
| `selling_price` | DECIMAL(18,2) | NOT NULL | Giá bán |
| `status` | VARCHAR(20) | DEFAULT 'active' | Trạng thái |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `update_at` | DATETIME | ON UPDATE CURRENT_TIMESTAMP | Thời điểm cập nhật |

### 4.2. Bảng liên quan

| Bảng | Mối quan hệ | Mô tả |
|---|---|---|
| `category` | category_id | Phân loại sản phẩm theo danh mục |
| `unit` | unit_id | Đơn vị tính (cái, kg, lít,...) |
| `inventory` | product_id | Thông tin tồn kho chi tiết theo kho |

---

## 5. Tệp mở rộng

### 5.1. ProductServlet (Legacy)

**Đường dẫn:** `category/ProductServlet.java`

Đây là một servlet hoàn chỉnh standalone xử lý các endpoint `/admin/products`, được phát triển trước khi tái cấu trúc module. Servlet này chứa đầy đủ logic xử lý cho các chức năng:

- **add**: Thêm sản phẩm mới
- **view**: Xem chi tiết sản phẩm
- **search**: Tìm kiếm sản phẩm
- **list**: Liệt kê danh sách sản phẩm
- **delete**: Xóa sản phẩm

Servlet sử dụng package `com.kiotretail` và đang chờ được tích hợp vào kiến trúc module chính thức. Việc tích hợp cần thực hiện refactor để:

1. Chuyển package từ `com.kiotretail` sang `controller.product`
2. Tách logic DAO ra `ProductDAO.java`
3. Chuyển Model sang `model.product`
4. Cập nhật đường dẫn route phù hợp

---

## 6. Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| ProductController | Skeleton | Chỉ có routing, chưa có logic |
| Product.java | Hoàn chỉnh | Model định nghĩa đầy đủ |
| ProductDAO.java | Skeleton | Chưa triển khai SQL |
| Views (7 files) | Template | Template HTML, chưa kết nối backend |
| ProductServlet | Hoàn chỉnh | Standalone, chờ tích hợp |

**Đánh giá tổng thể:** Module đang ở mức Skeleton - đã có cấu trúc và view template nhưng chưa có logic nghiệp vụ thực tế.

---

## 7. Nghiệp vụ chính

### 7.1. Quản lý sản phẩm (CRUD)

- **Tạo mới**: Thêm sản phẩm với thông tin cơ bản (tên, giá, danh mục, đơn vị)
- **Xem danh sách**: Liệt kê sản phẩm với phân trang, tìm kiếm, lọc theo danh mục
- **Xem chi tiết**: Hiển thị thông tin đầy đủ của một sản phẩm
- **Cập nhật**: Chỉnh sửa thông tin sản phẩm
- **Xóa**: Vô hiệu hóa hoặc xóa sản phẩm (soft delete)

### 7.2. Quản lý tồn kho

- **Điều chỉnh số lượng**: Tăng/giảm số lượng tồn kho thủ công
- **Nhập hàng**: Tạo phiếu nhập khi nhận hàng từ nhà cung cấp
- **Theo dõi tồn kho**: Liên kết với Inventory Module để theo dõi chi tiết

### 7.3. Liên kết danh mục

- Phân loại sản phẩm theo cấu trúc cây danh mục
- Hỗ trợ danh mục cha - danh mục con
- Lọc sản phẩm theo danh mục

---

## 8. Phụ thuộc module

- **Category Module**: Sản phẩm thuộc về một danh mục; danh mục có thể chứa nhiều sản phẩm
- **Inventory Module**: Sản phẩm có thông tin tồn kho chi tiết theo kho
- **Order Module**: Sản phẩm được đưa vào đơn hàng; số lượng tồn kho giảm khi đơn hàng hoàn thành
- **Supplier Module**: Sản phẩm được nhập từ nhà cung cấp

---

## 9. Mở rộng trong tương lai

- Hỗ trợ hình ảnh sản phẩm (product_images table)
- Quản lý biến thể sản phẩm (kích thước, màu sắc)
- Theo dõi lịch sử giá
- Barcode/QR code cho sản phẩm
- Tích hợp với hệ thống POS

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

# Inventory Module - FinoraRetail

## 1. Tổng quan Module

Module Inventory (Quản lý Kho hàng) là module quản lý toàn bộ hoạt động liên quan đến tồn kho trong hệ thống FinoraRetail. Module này chịu trách nhiệm theo dõi số lượng hàng hóa tại các kho hàng, xử lý các nghiệp vụ nhập kho (stock in), xuất kho (stock out), chuyển kho giữa các chi nhánh (stock transfer), và cung cấp dữ liệu báo cáo tồn kho.

Trong hệ thống bán lẻ đa chi nhánh, Inventory Module đóng vai trò quan trọng trong việc đảm bảo hàng hóa luôn sẵn sàng đáp ứng nhu cầu khách hàng, tối ưu hóa lượng tồn kho, và ngăn ngừa tình trạng thiếu hụt hoặc thừa hàng. Module này liên kết chặt chẽ với Store Module (quản lý kho theo chi nhánh), Product Module (thông tin sản phẩm), Supplier Module (nhập hàng từ nhà cung cấp), và Order Module (xuất hàng khi bán).

---

## 2. Thông tin kỹ thuật

### 2.1. Route và Controller

| Thuộc tính | Chi tiết |
|---|---|
| **Route chính** | `/inventory/*` |
| **Controller** | `InventoryController.java` |
| **Package** | `controller.inventory` |
| **Trạng thái** | Skeleton |

Controller sử dụng Front Controller pattern để điều hướng các request liên quan đến kho hàng. Hiện tại, controller chỉ đóng vai trò định tuyến cơ bản mà chưa triển khai logic nghiệp vụ.

### 2.2. Models

| Thuộc tính | Chi tiết |
|---|---|
| **InventoryItem.java** | Model cho thông tin tồn kho của sản phẩm tại kho |
| **StockTransaction.java** | Model cho giao dịch nhập/xuất kho |
| **Package** | `model.inventory` |

**InventoryItem Model - các trường chính:**

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `itemId` | Integer | Khóa chính |
| `warehouseId` | Integer | Khóa ngoại đến warehouse |
| `productId` | Integer | Khóa ngoại đến product |
| `quantity` | Integer | Số lượng tồn kho hiện tại |
| `reservedQuantity` | Integer | Số lượng đã giữ (chờ xử lý) |
| `availableQuantity` | Integer | Số lượng khả dụng (quantity - reserved) |
| `minStockLevel` | Integer | Mức tồn kho tối thiểu (cảnh báo) |
| `maxStockLevel` | Integer | Mức tồn kho tối đa |
| `updatedAt` | Timestamp | Thời điểm cập nhật cuối |

**StockTransaction Model - các trường chính:**

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `transactionId` | Integer | Khóa chính |
| `warehouseId` | Integer | Khóa ngoại đến warehouse |
| `productId` | Integer | Khóa ngoại đến product |
| `transactionType` | String | Loại giao dịch |
| `quantity` | Integer | Số lượng (+/-) |
| `referenceType` | String | Loại tham chiếu (PO, Order, Adjustment) |
| `referenceId` | Integer | ID tham chiếu |
| `notes` | String | Ghi chú |
| `createdBy` | Integer | Người thực hiện |
| `createdAt` | Timestamp | Thời điểm giao dịch |

### 2.3. DAOs (Data Access Objects)

| Thuộc tính | Chi tiết |
|---|---|
| **InventoryItemDAO.java** | Truy cập dữ liệu tồn kho |
| **StockTransactionDAO.java** | Truy cập dữ liệu giao dịch kho |
| **Package** | `dao.inventory` |
| **Trạng thái** | Skeleton (chưa triển khai SQL) |

---

## 3. Views (JSP)

Module sử dụng 5 file JSP để hiển thị giao diện người dùng:

| Tệp JSP | Mô tả |
|---|---|
| `dashboard.jsp` | Trang tổng quan tồn kho với thống kê và cảnh báo |
| `import.jsp` | Form nhập kho (nhận hàng từ nhà cung cấp) |
| `export.jsp` | Form xuất kho (xuất hàng bán hoặc hao hụt) |
| `transfer.jsp` | Form chuyển kho giữa các chi nhánh |
| `report.jsp` | Trang báo cáo tồn kho chi tiết |

---

## 4. Cơ sở dữ liệu

### 4.1. Bảng kho hàng: `warehouse`

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `warehouse_id` | INT | Khóa chính |
| `warehouse_name` | NVARCHAR(100) | Tên kho |
| `branch_id` | INT | Khóa ngoại đến branch |
| `address` | NVARCHAR(255) | Địa chỉ kho |
| `manager_id` | INT | Người quản lý kho |
| `status` | VARCHAR(20) | Trạng thái hoạt động |
| `created_at` | DATETIME | Thời điểm tạo |

### 4.2. Bảng tồn kho: `inventory`

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `inventory_id` | INT | Khóa chính |
| `warehouse_id` | INT | Khóa ngoại đến warehouse |
| `product_id` | INT | Khóa ngoại đến product |
| `quantity` | INT | Số lượng tồn |
| `reserved_quantity` | INT | Số lượng giữ chỗ |
| `min_stock_level` | INT | Mức tồn kho tối thiểu |
| `updated_at` | DATETIME | Thời điểm cập nhật cuối |

### 4.3. Bảng giao dịch kho: `stock_transaction`

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `transaction_id` | INT | Khóa chính |
| `warehouse_id` | INT | Khóa ngoại đến warehouse |
| `product_id` | INT | Khóa ngoại đến product |
| `transaction_type` | VARCHAR(20) | Loại giao dịch |
| `quantity` | INT | Số lượng (dương: nhập, âm: xuất) |
| `reference_type` | VARCHAR(50) | Loại tham chiếu |
| `reference_id` | INT | ID tham chiếu |
| `notes` | NVARCHAR(500) | Ghi chú |
| `created_by` | INT | Người thực hiện |
| `created_at` | DATETIME | Thời điểm tạo |

### 4.4. Bảng chuyển kho: `stock_transfer`

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `transfer_id` | INT | Khóa chính |
| `from_warehouse_id` | INT | Kho nguồn |
| `to_warehouse_id` | INT | Kho đích |
| `status` | VARCHAR(20) | Trạng thái chuyển |
| `requested_by` | INT | Người yêu cầu |
| `approved_by` | INT | Người phê duyệt |
| `notes` | NVARCHAR(500) | Ghi chú |
| `created_at` | DATETIME | Thời điểm tạo |
| `completed_at` | DATETIME | Thời điểm hoàn thành |

### 4.5. Bảng chi tiết chuyển kho: `stock_transfer_detail`

| Trường | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `detail_id` | INT | Khóa chính |
| `transfer_id` | INT | Khóa ngoại đến stock_transfer |
| `product_id` | INT | Khóa ngoại đến product |
| `quantity` | INT | Số lượng chuyển |

---

## 5. Mối quan hệ Chi nhánh - Kho

Mỗi chi nhánh (branch) trong hệ thống có thể có một hoặc nhiều kho hàng (warehouse):

```
Branch (Chi nhánh)
    ├── Warehouse 1 (Kho chính)
    │     └── Inventory Items (Tồn kho)
    ├── Warehouse 2 (Kho phụ)
    │     └── Inventory Items (Tồn kho)
    └── ...
```

Điều này cho phép:
- Tách biệt kho theo chức năng (kho bán lẻ, kho trung chuyển)
- Quản lý tồn kho riêng cho từng khu vực trong chi nhánh
- Linh hoạt trong việc chuyển hàng nội bộ

---

## 6. Nghiệp vụ chính

### 6.1. Nhập kho (Stock In)

**Các loại nhập kho:**

| Loại | Mã | Mô tả |
|---|---|---|
| Nhập từ nhà cung cấp | `import_po` | Nhận hàng từ Purchase Order |
| Nhập chuyển kho | `transfer_in` | Nhận hàng từ kho khác |
| Nhập điều chỉnh | `adjustment_in` | Điều chỉnh tăng tồn kho |
| Nhập trả lại | `return_in` | Nhận hàng trả lại từ khách |

**Quy trình nhập kho:**
1. Tạo phiếu nhập kho với danh sách sản phẩm
2. Hệ thống kiểm tra và xác nhận thông tin
3. Cập nhật số lượng tồn kho
4. Ghi nhận stock_transaction
5. Cập nhật trạng thái phiếu nhập

### 6.2. Xuất kho (Stock Out)

**Các loại xuất kho:**

| Loại | Mã | Mô tả |
|---|---|---|
| Xuất bán | `sale` | Xuất kho khi bán hàng |
| Xuất chuyển kho | `transfer_out` | Chuyển hàng sang kho khác |
| Xuất điều chỉnh | `adjustment_out` | Điều chỉnh giảm tồn kho |
| Xuất hủy | `discard` | Hàng hỏng, hết hạn |

**Quy trình xuất kho:**
1. Kiểm tra số lượng tồn kho khả dụng
2. Tạo phiếu xuất kho với danh sách sản phẩm
3. Cập nhật số lượng tồn kho
4. Ghi nhận stock_transaction
5. Cập nhật trạng thái phiếu xuất

### 6.3. Chuyển kho (Stock Transfer)

**Quy trình chuyển kho giữa các chi nhánh:**

```
Kho nguồn (From Warehouse)
    → Tạo yêu cầu chuyển kho
    → Giảm tồn kho tại kho nguồn (reserved)
    
Kho đích (To Warehouse)
    → Phê duyệt yêu cầu
    → Xác nhận nhận hàng
    → Tăng tồn kho tại kho đích
    → Hoàn tất giao dịch
```

| Trạng thái | Mô tả |
|---|---|
| `pending` | Chờ phê duyệt |
| `approved` | Đã phê duyệt, đang vận chuyển |
| `completed` | Đã hoàn thành |
| `rejected` | Bị từ chối |
| `cancelled` | Đã hủy |

---

## 7. Cảnh báo tồn kho

### 7.1. Cảnh báo mức tồn kho

| Loại | Điều kiện | Hành động |
|---|---|---|
| Cảnh báo sắp hết | `available_quantity <= min_stock_level` | Hiển thị thông báo trên dashboard |
| Hết hàng | `available_quantity = 0` | Đánh dấu sản phẩm là "out of stock" |
| Vượt mức tối đa | `quantity > max_stock_level` | Cảnh báo tồn kho quá nhiều |

### 7.2. Dashboard tồn kho

Trang dashboard cung cấp:
- Tổng quan số lượng sản phẩm đang tồn
- Danh sách sản phẩm sắp hết hàng
- Danh sách sản phẩm vượt mức tối đa
- Biểu đồ tồn kho theo danh mục
- So sánh tồn kho giữa các chi nhánh

---

## 8. Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| InventoryController | Skeleton | Chỉ có routing |
| InventoryItem.java | Hoàn chỉnh | Model định nghĩa đầy đủ |
| StockTransaction.java | Hoàn chỉnh | Model định nghĩa đầy đủ |
| InventoryItemDAO.java | Skeleton | Chưa triển khai SQL |
| StockTransactionDAO.java | Skeleton | Chưa triển khai SQL |
| Views (5 files) | Template | Template HTML, chưa kết nối backend |

**Đánh giá tổng thể:** Module đang ở mức Skeleton - đã có cấu trúc model và workflow đầy đủ nhưng chưa triển khai logic nghiệp vụ.

---

## 9. Phụ thuộc module

- **Store Module**: Kho hàng thuộc về chi nhánh
- **Product Module**: Thông tin sản phẩm trong tồn kho
- **Supplier Module**: Nhập hàng từ nhà cung cấp
- **Order Module**: Xuất hàng khi bán, trừ tồn kho
- **Purchase Order Module**: Tạo phiếu nhập kho

---

## 10. Lưu ý quan trọng

- **Concurrency**: Cần sử dụng transaction và locking khi cập nhật tồn kho để tránh race condition
- **Audit Trail**: Tất cả giao dịch tồn kho phải được ghi log đầy đủ
- **Negative Stock**: Không cho phép tồn kho âm; kiểm tra trước khi xuất
- **Reserved Quantity**: Tách biệt số lượng giữ chỗ và số lượng khả dụng

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*

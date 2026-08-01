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
| Nhập từ nhà cung cấp | `import_po` | Nhận hàng từ Purchase Order (hỗ trợ nhập theo từng Nhà Cung Cấp) |
| Nhập chuyển kho | `transfer_in` | Nhận hàng từ kho khác |
| Nhập điều chỉnh | `adjustment_in` | Điều chỉnh tăng tồn kho |
| Nhập trả lại | `return_in` | Nhận hàng trả lại từ khách |

**Quy trình nhập kho đa Nhà Cung Cấp (Multi-Supplier PO Flow):**
1. **Duyệt đơn tổng (Owner / Store Manager)**: Đơn hàng nhập (`PO-...`) chứa sản phẩm từ một hoặc nhiều Nhà cung cấp được duyệt dưới dạng 1 phiếu tổng chung. Trạng thái chuyển sang `IN_TRANSIT` (Đang vận chuyển).
2. **Kiểm kho & Nhập kho thực tế (Warehouse Staff)**:
   - Trong popup *Xác Nhận Nhập Kho*, hệ thống phân nhóm sản phẩm theo từng **Nhà Cung Cấp** (`supplier_id`).
   - Kho bấm xác nhận nhập cho riêng từng Nhà Cung Cấp khi giao hàng ở các thời điểm khác nhau.
   - Với mỗi đợt nhập của NCC:
     - Tăng số lượng tồn kho sản phẩm của NCC đó.
     - Ghi nhận `stock_transaction` với diễn giải NCC tương ứng.
     - Tạo **Phiếu chi Sổ quỹ** cho riêng tổng tiền của NCC đó.
     - Cập nhật `supplier_status = 'COMPLETED'` cho các dòng sản phẩm của NCC đó.
3. **Hoàn tất đơn tổng**: Nếu còn NCC chưa giao, đơn hàng giữ trạng thái `IN_TRANSIT`. Khi TẤT CẢ các NCC đã nhập kho thành công ➔ Đơn tổng tự động chuyển sang `COMPLETED`.

---

## 7. Cảnh báo tồn kho & Bản in phiếu

### 7.1. Cảnh báo mức tồn kho

| Loại | Điều kiện | Hành động |
|---|---|---|
| Cảnh báo sắp hết | `available_quantity <= min_stock_level` | Hiển thị thông báo trên dashboard |
| Hết hàng | `available_quantity = 0` | Đánh dấu sản phẩm là "out of stock" |
| Vượt mức tối đa | `quantity > max_stock_level` | Cảnh báo tồn kho quá nhiều |

### 7.2. In phiếu & Chứng từ hóa đơn (`_print_order.jsp`)
- Đảm bảo mẫu in hóa đơn đen trắng truyền thống (Font `Times New Roman`, viền bảng hóa đơn, chữ ký 2 bên).
- Khi đơn hàng ở trạng thái `COMPLETED`: Mẫu in tự động hiển thị cột **SL Đặt** và **SL Nhập** thực tế để lưu chứng từ kiểm kê kho.

---

## 8. Trạng thái triển khai

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| InventoryController | Hoàn chỉnh | Định tuyến & chuyển tiếp các tab tồn kho |
| OrderVoucherController | Hoàn chỉnh | Xử lý duyệt đơn, nhập kho thực tế theo NCC, in phiếu |
| StockController | Hoàn chỉnh | Điều hướng tạo đơn nhập kho trực tiếp về `tab=transfer&warehouseId=X` |
| InventoryExecutionService | Hoàn chỉnh | Xử lý transaction tăng kho, tạo phiếu chi Sổ quỹ theo NCC, tự động chuyển `COMPLETED` |
| OrderDAO | Hoàn chỉnh | PreparedStatement an toàn, map `actual_quantity`, `supplier_id`, `supplier_status` |
| Views & Modals | Hoàn chỉnh | Giao diện gom nhóm NCC, tự động tính chênh lệch realtime JS, ẩn alert rườm rà |

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

*Document version: 3.0*
*Last updated: 2026-07-31*
*Project: SWP391_Finora (FinoraRetail)*

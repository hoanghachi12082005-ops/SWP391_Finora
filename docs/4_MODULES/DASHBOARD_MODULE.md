# Dashboard Module - FinoraRetail

## 1. Tổng quan Module

Module Dashboard cung cấp giao diện tổng quan trực quan cho người quản lý theo dõi các chỉ số kinh doanh quan trọng của cửa hàng bán lẻ. Module bao gồm ba giao diện dashboard chuyên biệt phục vụ ba nhóm người dùng khác nhau: chủ cửa hàng (Owner), nhân viên kho (Inventory Staff), và nhân viên tài chính (Financial Staff).

**Trạng thái hiện tại:** `Skeleton` (template HTML tĩnh, chưa kết nối dữ liệu thực)

**Package:** `controller.dashboard`

**Vị trí tệp:** `src/main/java/controller/dashboard/DashboardController.java`

**Views:** `WEB-INF/views/dashboard/owner.jsp`, `inventory.jsp`, `financial.jsp`

---

## 2. Kiến trúc Module

### 2.1 Các thành phần chính

| Thành phần | Loại | Vị trí |
|---|---|---|
| DashboardController | Servlet | `controller.dashboard.DashboardController` |
| owner.jsp | View | `WEB-INF/views/dashboard/owner.jsp` |
| inventory.jsp | View | `WEB-INF/views/dashboard/inventory.jsp` |
| financial.jsp | View | `WEB-INF/views/dashboard/financial.jsp` |

### 2.2 Sơ đồ cấu trúc

```
/dashboard/
├── /owner        ──► owner.jsp      ──► OwnerDashboardController
├── /inventory   ──► inventory.jsp   ──► InventoryDashboardController
└── /financial   ──► financial.jsp   ──► FinancialDashboardController
```

### 2.3 Route mapping

| Route | Method | View | Controller |
|---|---|---|---|
| `/dashboard/owner` | GET | `owner.jsp` | DashboardController |
| `/dashboard/inventory` | GET | `inventory.jsp` | DashboardController |
| `/dashboard/financial` | GET | `financial.jsp` | DashboardController |

---

## 3. Owner Dashboard (`/dashboard/owner`)

### 3.1 Mục đích

Owner Dashboard cung cấp bức tranh tổng quan toàn diện về hiệu suất kinh doanh của cửa hàng, giúp chủ cửa hàng đưa ra quyết định chiến lược dựa trên dữ liệu thực tế.

### 3.2 Các chỉ số hiển thị (dự kiến)

| Chỉ số | Kiểu dữ liệu | Mô tả | Nguồn dữ liệu |
|---|---|---|---|
| Total Sales | Currency (VND) | Tổng doanh số trong kỳ | Bảng orders |
| Total Orders | Integer | Số lượng đơn hàng trong kỳ | Bảng orders |
| Total Customers | Integer | Số khách hàng đã giao dịch | Bảng customers |
| Revenue Today | Currency (VND) | Doanh thu trong ngày | Bảng orders |
| Revenue This Month | Currency (VND) | Doanh thu trong tháng | Bảng orders |
| Revenue This Year | Currency (VND) | Doanh thu trong năm | Bảng orders |
| Average Order Value | Currency (VND) | Giá trị trung bình mỗi đơn | Bảng orders |
| Top Selling Products | List | Top 5 sản phẩm bán chạy | Bảng order_details |
| Recent Orders | List | 10 đơn hàng gần nhất | Bảng orders |

### 3.3 Giao diện dự kiến

```
┌─────────────────────────────────────────────────────────┐
│  OWNER DASHBOARD                              [User] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Total Sales  │  │ Total Orders │  │  Customers   │  │
│  │  125,450,000 │  │     342      │  │     156     │  │
│  │   VND        │  │             │  │             │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                         │
│  ┌────────────────────────┐  ┌───────────────────────┐ │
│  │   Revenue Chart       │  │   Top Products        │ │
│  │   (Line/Bar Chart)    │  │   1. Sản phẩm A      │ │
│  │                       │  │   2. Sản phẩm B      │ │
│  └────────────────────────┘  └───────────────────────┘ │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │   Recent Orders                                  │  │
│  │   #001  - 12,500,000 VND  - Đã giao            │  │
│  │   #002  -   8,200,000 VND  - Đang xử lý       │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 4. Inventory Dashboard (`/dashboard/inventory`)

### 4.1 Mục đích

Inventory Dashboard cung cấp thông tin chi tiết về tình trạng tồn kho, giúp nhân viên kho theo dõi và quản lý hàng hóa một cách hiệu quả, đồng thời đưa ra cảnh báo kịp thời khi hàng sắp hết.

### 4.2 Các chỉ số hiển thị (dự kiến)

| Chỉ số | Kiểu dữ liệu | Mô tả | Nguồn dữ liệu |
|---|---|---|---|
| Total Products | Integer | Tổng số sản phẩm trong kho | Bảng products |
| Total Stock Value | Currency (VND) | Tổng giá trị hàng tồn kho | Bảng inventory |
| Low Stock Items | Integer | Số sản phẩm sắp hết hàng | Bảng products (threshold) |
| Out of Stock Items | Integer | Số sản phẩm đã hết hàng | Bảng products (quantity = 0) |
| Categories Count | Integer | Số danh mục đang sử dụng | Bảng category |
| Stock Alerts | List | Danh sách cảnh báo tồn kho | Bảng inventory_logs |
| Recent Stock Movements | List | Các biến động kho gần đây | Bảng inventory_logs |
| Top Low Stock | List | Top 10 sản phẩm sắp hết | Bảng products |

### 4.3 Ngưỡng cảnh báo (dự kiến)

| Mức cảnh báo | Ngưỡng | Màu sắc |
|---|---|---|
| Critical (Hết hàng) | quantity = 0 | Đỏ |
| Low (Sắp hết) | quantity <= min_threshold | Cam |
| Warning (Cảnh báo) | quantity <= 2 * min_threshold | Vàng |

### 4.4 Giao diện dự kiến

```
┌─────────────────────────────────────────────────────────┐
│  INVENTORY DASHBOARD                          [User] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Total Stock  │  │ Low Stock     │  │ Out of Stock │  │
│  │   Value      │  │   Items       │  │   Items      │  │
│  │ 85,200,000   │  │     23        │  │      5       │  │
│  │   VND        │  │              │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                         │
│  ┌────────────────────────┐  ┌───────────────────────┐ │
│  │   Stock by Category     │  │   Stock Alerts        │ │
│  │   (Pie Chart)           │  │   ⚠️ Sản phẩm X < 5   │ │
│  │                         │  │   🚨 Sản phẩm Y = 0   │ │
│  └────────────────────────┘  └───────────────────────┘ │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │   Recent Stock Movements                          │  │
│  │   [+50] Sản phẩm A  - 21/06/2026 10:30          │  │
│  │   [-10] Sản phẩm B  - 21/06/2026 09:15          │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 5. Financial Dashboard (`/dashboard/financial`)

### 5.1 Mục đích

Financial Dashboard cung cấp bức tranh tài chính toàn diện bao gồm thu nhập, chi phí và lợi nhuận, giúp theo dõi sức khỏe tài chính của cửa hàng theo thời gian thực.

### 5.2 Các chỉ số hiển thị (dự kiến)

| Chỉ số | Kiểu dữ liệu | Mô tả | Nguồn dữ liệu |
|---|---|---|---|
| Total Income | Currency (VND) | Tổng thu nhập trong kỳ | Bảng income |
| Total Expenses | Currency (VND) | Tổng chi phí trong kỳ | Bảng expenses |
| Net Profit | Currency (VND) | Lợi nhuận ròng (Income - Expenses) | Tính toán |
| Profit Margin | Percentage (%) | Tỷ lệ lợi nhuận trên doanh thu | Tính toán |
| Income by Category | List | Thu nhập theo danh mục | Bảng income |
| Expenses by Category | List | Chi phí theo danh mục | Bảng expenses |
| Daily Revenue | List | Doanh thu theo ngày | Bảng orders |
| Monthly Summary | Table | Tổng hợp thu chi theo tháng | Bảng income, expenses |

### 5.3 Công thức tính toán

```
Net Profit = Total Income - Total Expenses
Profit Margin (%) = (Net Profit / Total Income) × 100
Daily Revenue = SUM(order_total) WHERE order_date = today
Monthly Revenue = SUM(order_total) WHERE MONTH(order_date) = current_month
```

### 5.4 Giao diện dự kiến

```
┌─────────────────────────────────────────────────────────┐
│  FINANCIAL DASHBOARD                            [User] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Total Income │  │Total Expenses │  │ Net Profit  │  │
│  │ 185,450,000  │  │  98,230,000   │  │  87,220,000 │  │
│  │    VND ↑     │  │    VND ↓      │  │   VND 47%   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                         │
│  ┌────────────────────────┐  ┌───────────────────────┐ │
│  │   Income vs Expenses   │  │   Expenses Breakdown   │ │
│  │   (Bar Chart)          │  │   📦 Kho: 40%          │ │
│  │   ████████ vs █████    │  │   💼 Lương: 30%        │ │
│  └────────────────────────┘  │   🏠 Thuê: 20%        │ │
│                               │   📝 Khác: 10%        │ │
│                               └───────────────────────┘ │
│  ┌──────────────────────────────────────────────────┐  │
│  │   Monthly Summary - 2026                         │  │
│  │   Month    Income    Expenses    Profit          │  │
│  │   Jan      45M       28M          17M           │  │
│  │   Feb      52M       30M          22M           │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 6. DashboardController

### 6.1 Cấu trúc Servlet

```java
@WebServlet(name = "DashboardController", urlPatterns = {
    "/dashboard/owner",
    "/dashboard/inventory",
    "/dashboard/financial"
})
public class DashboardController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getServletPath();

        switch (pathInfo) {
            case "/dashboard/owner":
                loadOwnerDashboard(request);
                request.getRequestDispatcher("/WEB-INF/views/dashboard/owner.jsp")
                       .forward(request, response);
                break;
            case "/dashboard/inventory":
                loadInventoryDashboard(request);
                request.getRequestDispatcher("/WEB-INF/views/dashboard/inventory.jsp")
                       .forward(request, response);
                break;
            case "/dashboard/financial":
                loadFinancialDashboard(request);
                request.getRequestDispatcher("/WEB-INF/views/dashboard/financial.jsp")
                       .forward(request, response);
                break;
        }
    }
}
```

### 6.2 Các phương thức load data (cần triển khai)

```java
// Phương thức cần triển khai khi kết nối database
private void loadOwnerDashboard(HttpServletRequest request) {
    // request.setAttribute("totalSales", orderDAO.getTotalSales());
    // request.setAttribute("totalOrders", orderDAO.countOrders());
    // request.setAttribute("totalCustomers", customerDAO.countCustomers());
}

private void loadInventoryDashboard(HttpServletRequest request) {
    // request.setAttribute("totalStockValue", inventoryDAO.getTotalValue());
    // request.setAttribute("lowStockItems", productDAO.countLowStock());
}

private void loadFinancialDashboard(HttpServletRequest request) {
    // request.setAttribute("totalIncome", incomeDAO.getTotalIncome());
    // request.setAttribute("totalExpenses", expenseDAO.getTotalExpenses());
}
```

---

## 7. AuthFilter Integration

Tất cả các route dashboard được bảo vệ bởi `AuthFilter`:

```
/dashboard/owner      ──► AuthFilter ──► ✅ currentUser exists ──► owner.jsp
/dashboard/inventory ──► AuthFilter ──► ✅ currentUser exists ──► inventory.jsp
/dashboard/financial ──► AuthFilter ──► ✅ currentUser exists ──► financial.jsp
                            │
                            └── ❌ currentUser == null ──► redirect /login
```

---

## 8. Trạng thái hiện tại và công việc cần làm

### 8.1 Trạng thái skeleton

| Thành phần | Trạng thái | Ghi chú |
|---|---|---|
| DashboardController | Skeleton | Chỉ có cấu trúc, chưa load data |
| owner.jsp | Skeleton | Template HTML tĩnh |
| inventory.jsp | Skeleton | Template HTML tĩnh |
| financial.jsp | Skeleton | Template HTML tĩnh |
| Kết nối Database | Chưa có | Chưa gọi DAO |
| Business Logic | Chưa có | Chưa tính toán chỉ số |

### 8.2 Công việc cần triển khai

| Công việc | Ưu tiên | Phụ thuộc |
|---|---|---|
| Triển khai OrderDAO | Cao | Bảng orders, order_details |
| Triển khai CustomerDAO | Cao | Bảng customers |
| Triển khai ProductDAO | Cao | Bảng products, inventory |
| Triển khai IncomeDAO | Cao | Bảng income |
| Triển khai ExpenseDAO | Cao | Bảng expenses |
| Triển khai InventoryDAO | Trung bình | Bảng inventory, inventory_logs |
| Tích hợp Chart.js/CanvasJS | Trung bình | Thư viện JavaScript |
| Triển khai Real-time updates | Thấp | WebSocket hoặc polling |

---

## 9. Công nghệ biểu đồ (gợi ý)

### 9.1 Biểu đồ cho Owner Dashboard
- **Line Chart:** Doanh thu theo thời gian (ngày/tuần/tháng)
- **Bar Chart:** So sánh doanh số theo danh mục
- **Doughnut Chart:** Phân bổ doanh thu theo kênh

### 9.2 Biểu đồ cho Inventory Dashboard
- **Pie Chart:** Tồn kho theo danh mục
- **Bar Chart:** Top sản phẩm tồn kho cao/thấp
- **Gauge:** Mức độ sử dụng kho

### 9.3 Biểu đồ cho Financial Dashboard
- **Stacked Bar Chart:** Thu nhập vs Chi phí
- **Line Chart:** Lợi nhuận theo thời gian
- **Pie Chart:** Phân bổ chi phí theo danh mục

---

## 10. Bảng cơ sở dữ liệu liên quan

| Bảng | Mục đích | Module |
|---|---|---|
| `orders` | Thông tin đơn hàng | Order |
| `order_details` | Chi tiết sản phẩm trong đơn | Order |
| `customers` | Thông tin khách hàng | Customer |
| `products` | Thông tin sản phẩm | Product |
| `inventory` | Số lượng tồn kho | Inventory |
| `inventory_logs` | Nhật ký biến động kho | Inventory |
| `income` | Bản ghi thu nhập | Income |
| `expenses` | Bản ghi chi phí | Expense |

---

## 11. Yêu cầu về hiệu suất

| Chỉ tiêu | Mục tiêu | Ghi chú |
|---|---|---|
| Thời gian load dashboard | < 2 giây | Bao gồm truy vấn data |
| Thời gian load biểu đồ | < 1 giây | Sau khi data đã sẵn sàng |
| Tần suất refresh data | 5 phút | Hoặc real-time nếu có WebSocket |
| Cache dashboard data | 5 phút | Giảm tải database |

---

*Document version: 1.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*
*Module owner: Dashboard Team*

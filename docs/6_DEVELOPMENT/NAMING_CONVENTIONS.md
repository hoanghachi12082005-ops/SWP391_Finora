# Quy Ước Đặt Tên - Naming Conventions

> **Mục đích:** Tài liệu này quy định các quy ước đặt tên thống nhất cho toàn bộ thành phần trong dự án FinoraRetail (SWP391_Finora), bao gồm mã nguồn Java, cơ sở dữ liệu, URL routes, file JSP, artifact build, và tài liệu. Việc tuân thủ quy ước đặt tên thống nhất giúp code dễ đọc, dễ bảo trì, và hỗ trợ onboarding cho thành viên mới.

---

## 1. Quy Ước Java

### 1.1. Package

- Viết **thường hoàn toàn** (lowercase).
- Phân cách bằng dấu chấm.
- Cấu trúc theo feature/module, không theo tầng.

```
com.storemanagement.controller
com.storemanagement.controller.category
com.storemanagement.controller.product
com.storemanagement.dao
com.storemanagement.dao.category
com.storemanagement.dao.product
com.storemanagement.model
com.storemanagement.service
com.storemanagement.util
com.storemanagement.filter
com.storemanagement.dto
```

**Nguyên tắc:**

- Không đặt class trực tiếp vào package gốc `com.storemanagement`.
- Nếu module nhỏ và ít class, có thể gom chung vào package cha (vd: `dao.category` chỉ có 1-2 class thì dùng `dao`).
- Nếu module lớn, tạo package con rõ ràng (vd: `controller.product`).

### 1.2. Class

- **PascalCase** — Viết hoa chữ cái đầu mỗi từ, không gạch nối.
- Thêm hậu tố phản ánh loại class:

| Loại class | Hậu tố | Ví dụ |
|---|---|---|
| Servlet / Controller | `Servlet` | `CategoryServlet`, `ProductServlet` |
| Data Access Object | `DAO` | `CategoryDAO`, `ProductDAO` |
| Service | `Service` | `CategoryService`, `ReportService` |
| Filter | `Filter` | `AuthFilter`, `EncodingFilter` |
| Utility | `Util` | `ValidationUtil`, `DateUtil`, `StringUtil` |
| Model / Entity | Không hậu tố | `Category`, `Product`, `Employee` |
| DTO | `DTO` | `CategoryDTO`, `ProductListDTO` |
| Servlet Context Listener | `Listener` | `AppListener` |

### 1.3. Method

- **camelCase** — Viết thường chữ cái đầu, viết hoa đầu mỗi từ tiếp theo.
- Tiền tố hành động rõ ràng:

| Tiền tố | Ngữ cảnh | Ví dụ |
|---|---|---|
| `get` | Lấy 1 bản ghi theo khóa duy nhất | `getCategoryById(int id)` |
| `find` | Tìm kiếm theo nhiều tiêu chí | `findProductsByName(String name)` |
| `search` | Tìm kiếm phức tạp, nhiều filter | `searchProducts(String keyword, Integer categoryId, BigDecimal minPrice)` |
| `list` | Lấy tất cả hoặc danh sách theo trạng thái | `listAllCategories()`, `listActiveProducts()` |
| `count` | Đếm số lượng | `countActiveCategories()`, `countProductsByCategory(int categoryId)` |
| `add` / `insert` / `create` | Thêm bản ghi mới | `addProduct(Product p)`, `insertCategory(Category c)` |
| `update` / `edit` | Cập nhật bản ghi hiện có | `updateProduct(Product p)`, `editCategory(Category c)` |
| `delete` / `remove` | Xóa bản ghi | `deleteCategory(int id)`, `removeProduct(int id)` |
| `validate` | Kiểm tra tính hợp lệ | `validateCategoryInput(HttpServletRequest request)` |
| `extract` | Ánh xạ ResultSet → Entity (private) | `extractCategory(ResultSet rs)` |
| `init` | Khởi tạo / setup | `initFilter(FilterConfig config)` |

**Không đặt tên method quá ngắn hoặc quá chung chung:**

```java
// ❌ Tránh
void doIt();
void process();
Object get();

// ✅ Nên
void processCategoryImport(File uploadedFile);
Product getProductWithInventory(int productId);
```

### 1.4. Biến (Variable)

- **camelCase** — Thường chữ cái đầu, viết hoa đầu từ tiếp theo.
- Tên biến phải phản ánh ý nghĩa, không viết tắt không rõ nghĩa.

| Loại biến | Quy ước | Ví dụ |
|---|---|---|
| Biến local | camelCase | `categoryName`, `totalAmount`, `currentPage` |
| Biến entity/model | camelCase theo thuộc tính | `category.categoryName`, `product.productPrice` |
| Biến request attribute | camelCase | `categories`, `productList`, `flashMessage` |
| Biến session | camelCase, có prefix rõ ràng nếu cần | `session.getAttribute("currentUser")` |
| Biến đếm | `count` hoặc `total` prefix | `countCategories`, `totalRevenue` |
| Biến boolean | is/has/can prefix hoặc adjective | `isActive`, `hasPermission`, `isDeleted` |

```java
// ❌ Tránh
int x;
String n;
boolean f;

// ✅ Nên
int categoryId;
String categoryName;
boolean isActive;
```

### 1.5. Hằng Số (Constant)

- **UPPER_SNAKE_CASE** — Viết hoa, phân cách bằng dấu gạch dưới.
- Đặt trong class dưới dạng `static final`.

```java
public static final int MAX_PAGE_SIZE = 100;
public static final int DEFAULT_PAGE_SIZE = 10;
public static final String DEFAULT_STATUS = "ACTIVE";
public static final String FLASH_MESSAGE_SUCCESS = "success";
public static final String FLASH_MESSAGE_ERROR = "danger";
```

---

## 2. Quy Ước Cơ Sở Dữ Liệu

### 2.1. Bảng (Table)

- **PascalCase** — Viết hoa chữ cái đầu mỗi từ.
- **Số ít** — Tên bảng phản ánh một bản ghi duy nhất.

| Đúng | Sai |
|---|---|
| `Category` | `Categories` |
| `Product` | `Products` |
| `Employee` | `Employees` |
| `OrderDetail` | `OrderDetails` |
| `FinanceTransaction` | `FinanceTransactions` |

### 2.2. Cột (Column)

- **PascalCase** — Viết hoa chữ cái đầu mỗi từ.
- Tên cột phải mô tả rõ ý nghĩa, tránh từ khóa SQL.

| Đúng | Sai |
|---|---|
| `CategoryName` | `Name`, `CatName` |
| `ProductCode` | `Code`, `ProdCode` |
| `CreatedAt` | `Date`, `CreatedDate`, `CreateDate` |
| `UpdatedAt` | `DateModified` |
| `IsActive` | `Active`, `Status` (nếu là boolean) |
| `UnitPrice` | `Price` |

### 2.3. Khóa Chính (Primary Key)

- **ID suffix** — Tên bảng + `ID`.

| Bảng | Khóa chính |
|---|---|
| `Category` | `CategoryID` |
| `Product` | `ProductID` |
| `Employee` | `EmployeeID` |
| `Order` | `OrderID` |
| `Warehouse` | `WarehouseID` |

### 2.4. Khóa Ngoại (Foreign Key)

- **`<Entity>ID` suffix** — Tham chiếu đến khóa chính của bảng kia.

| Bảng | Khóa ngoại |
|---|---|
| `Product` | `CategoryID` (tham chiếu `Category.CategoryID`) |
| `Order` | `EmployeeID`, `WarehouseID` |
| `OrderDetail` | `OrderID`, `ProductID` |

### 2.5. Index

- **IDX_<Table>_<Column>** — Prefix `IDX_` cố định.

```
IDX_Category_IsActive
IDX_Product_CategoryID
IDX_Product_ProductCode
IDX_Employee_IsActive
```

### 2.6. Constraint

- **CK_<Table>_<Description>** — Prefix `CK_` cố định.

```
CK_Product_UnitPrice_Positive
CK_Employee_Salary_Positive
CK_Category_Status_Valid
```

### 2.7. Stored Procedure / Trigger

- **usp_<Table>_<Action>** cho stored procedure.
- **trg_<Table>_<Action>** cho trigger.

```
usp_Category_Insert
usp_Product_Search
trg_Order_UpdateTotal
```

---

## 3. Quy Ước URL Routes

### 3.1. Route Phía Server (JSP/Servlet)

- **Kebab-case** — Chữ thường, phân cách bằng dấu gạch ngang.
- **Không chứa động từ** trong route — sử dụng danh từ phản ánh resource.

| URL Pattern | Mô tả |
|---|---|
| `/category-management` | Danh sách/quản lý danh mục |
| `/category-create` | Form tạo danh mục |
| `/category-edit` | Form chỉnh sửa danh mục |
| `/product-list` | Danh sách sản phẩm |
| `/product-detail` | Chi tiết sản phẩm |
| `/product-create` | Form tạo sản phẩm |
| `/order-create` | Form tạo đơn hàng |
| `/order-list` | Danh sách đơn hàng |
| `/warehouse-inventory` | Tồn kho theo kho |

### 3.2. Route API (Tương Lai)

- **Khi API JSON được triển khai**, tuân thủ RESTful conventions:
  - Plural nouns cho resource.
  - Kebab-case cho multi-word.
  - HTTP method phản ánh hành động.

| Method | Route | Mô tả |
|---|---|---|
| `GET` | `/api/categories` | Lấy danh sách categories |
| `GET` | `/api/categories/{id}` | Lấy category theo ID |
| `POST` | `/api/categories` | Tạo category mới |
| `PUT` | `/api/categories/{id}` | Cập nhật category |
| `DELETE` | `/api/categories/{id}` | Xóa category |
| `GET` | `/api/products?category=5&page=1` | Tìm kiếm với filter |

---

## 4. Quy Ước File JSP

### 4.1. Tên File

- **Kebab-case** — Chữ thường, phân cách bằng dấu gạch ngang.
- File phản ánh chức năng rõ ràng.

| Tên file | Mô tả |
|---|---|
| `category-list.jsp` | Danh sách danh mục |
| `category-detail.jsp` | Chi tiết danh mục |
| `category-form.jsp` | Form thêm/sửa danh mục |
| `product-list.jsp` | Danh sách sản phẩm |
| `product-detail.jsp` | Chi tiết sản phẩm |
| `order-create.jsp` | Form tạo đơn hàng |
| `dashboard.jsp` | Trang tổng quan |
| `error-404.jsp` | Trang lỗi 404 |

### 4.2. Cấu Trúc Thư Mục

```
web/WEB-INF/views/
├── categories/
│   ├── list.jsp
│   ├── detail.jsp
│   └── form.jsp
├── products/
│   ├── list.jsp
│   ├── detail.jsp
│   └── form.jsp
├── orders/
│   ├── list.jsp
│   ├── detail.jsp
│   └── create.jsp
├── layouts/
│   └── admin-layout.jsp
└── common/
    ├── header.jsp
    ├── footer.jsp
    └── pagination.jsp
```

**Nguyên tắc:**

- File nằm trong `WEB-INF/views` để tránh truy cập trực tiếp.
- Mỗi module có thư mục con riêng.
- Layout và common components có thư mục riêng.

---

## 5. Quy Ước Build Artifacts

### 5.1. WAR File

- **Tên WAR** theo `finalName` trong `pom.xml`: `StoreManagementNetBeans.war`
- **Thư mục output**: `target/StoreManagementNetBeans.war`

```xml
<!-- pom.xml -->
<build>
    <finalName>StoreManagementNetBeans</finalName>
</build>
```

### 5.2. Thư Mục Build

| Thư mục | Mục đích |
|---|---|
| `target/` | Output của Maven build |
| `build/` | Output của IDE (NetBeans) |
| `dist/` | Output deployment của IDE |
| `build/classes/` | File `.class` sau khi compile |

**Không chỉnh sửa thủ công các file trong các thư mục trên.**

---

## 6. Quy Ước Tài Liệu

### 6.1. File Governance (Chính Sách, Tiêu Chuẩn)

- **UPPER_SNAKE_CASE** cho tên file.
- Phản ánh nội dung rõ ràng.

| File | Mô tả |
|---|---|
| `CODING_STANDARDS.md` | Tiêu chuẩn lập trình |
| `NAMING_CONVENTIONS.md` | Quy ước đặt tên |
| `API_CONVENTIONS.md` | Quy ước API |
| `REFACTOR_POLICY.md` | Chính sách tái cấu trúc |
| `PROTECTED_MODULES.md` | Danh sách module bảo vệ |

### 6.2. File Plan (Kế Hoạch)

- **`<FEATURE>_IMPLEMENTATION_PLAN.md`** — Mô tả rõ tính năng.
- Thư mục chứa: `docs/planning/<TOPIC>/`

```
docs/planning/CATEGORY_REFACTOR/
├── CATEGORY_REFACTOR_PLAN.md
└── MIGRATION_STEPS.md
```

### 6.3. File Chương Trình Hóa (Status, Architecture)

- **PascalCase hoặc Title Case** cho các file chương trình hóa dự án.
- Mô tả rõ nội dung trong tên file.

| File | Mô tả |
|---|---|
| `CURRENT_STATUS.md` | Trạng thái hiện tại dự án |
| `IMPLEMENTED_FEATURES.md` | Các tính năng đã triển khai |
| `TECH_DEBT.md` | Kỹ thuật nợ |
| `SYSTEM_ARCHITECTURE.md` | Kiến trúc hệ thống |
| `MODULE_BOUNDARIES.md` | Ranh giới module |
| `FOLDER_STRUCTURE.md` | Cấu trúc thư mục |

---

## 7. Tóm Tắt Bảng Tra Nhanh

| Thành phần | Quy ước | Ví dụ |
|---|---|---|
| Package | lowercase | `com.storemanagement.dao` |
| Class Java | PascalCase | `CategoryDAO` |
| Method | camelCase + verb prefix | `getCategoryById` |
| Biến | camelCase | `categoryName` |
| Hằng số | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| Bảng DB | PascalCase, số ít | `Category` |
| Cột DB | PascalCase | `CategoryName` |
| Khóa chính | `<Entity>ID` | `CategoryID` |
| Khóa ngoại | `<Entity>ID` | `CategoryID` |
| Index | `IDX_<Table>_<Column>` | `IDX_Category_IsActive` |
| Route | kebab-case, danh từ | `/category-management` |
| API Route | `/api/` + plural nouns | `/api/categories` |
| File JSP | kebab-case | `category-list.jsp` |
| File chính sách | UPPER_SNAKE_CASE | `CODING_STANDARDS.md` |
| File plan | `<FEATURE>_IMPLEMENTATION_PLAN.md` | `AUTH_IMPLEMENTATION_PLAN.md` |

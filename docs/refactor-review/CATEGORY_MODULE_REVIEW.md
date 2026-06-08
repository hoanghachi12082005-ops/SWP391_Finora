# Category Module Refactor Review

## Chức năng module

Category Management dùng để quản lý nhóm hàng trong hệ thống KiotRetail:

- Xem danh sách nhóm hàng.
- Tìm kiếm theo từ khóa.
- Lọc theo trạng thái.
- Lọc theo nhóm cha.
- Xem thống kê tổng nhóm hàng, nhóm gốc, và số sản phẩm liên kết.
- Thêm nhóm hàng mới.
- Cập nhật thông tin nhóm hàng.
- Mở chế độ in danh sách.

## Luồng xử lý

1. Người dùng truy cập `/category-management`.
2. `CategoryManagementServlet` đọc query string: `keyword`, `status`, `parentName`, `page`, `printMode`.
3. Servlet gọi `CategoryDAO` để lấy dữ liệu danh sách, thống kê, và nhóm cha.
4. Servlet đặt dữ liệu vào request attributes.
5. JSP `category-management/index.jsp` hiển thị giao diện.
6. Khi người dùng thêm hoặc sửa nhóm hàng, form POST về `/category-management`.
7. Servlet validate dữ liệu, gọi DAO để ghi database, lưu flash message vào session.
8. Servlet redirect lại `/category-management` để tránh submit lại form khi refresh.

## Các class tham gia

### `CategoryManagementServlet`

Trách nhiệm:

- Nhận request GET/POST.
- Đọc và validate tham số người dùng nhập.
- Điều phối luồng thêm/cập nhật nhóm hàng.
- Chuẩn bị dữ liệu cho JSP.
- Tạo flash message sau thao tác.

### `CategoryDAO`

Trách nhiệm:

- Chứa toàn bộ SQL của module category.
- Query danh sách nhóm hàng có phân trang.
- Query thống kê.
- Thêm/cập nhật nhóm hàng.
- Kiểm tra trùng tên.
- Kiểm tra quan hệ cha-con để tránh vòng lặp cây danh mục.
- Mapping `ResultSet` thành `Category` model.

### `Category`

Trách nhiệm:

- Lưu dữ liệu nhóm hàng trong Java.
- Di chuyển dữ liệu giữa DAO, servlet, và JSP.
- Cung cấp helper `isActive()` cho trạng thái hoạt động.

### `CategoryManagementService`

Trách nhiệm hiện tại:

- Ghi rõ quyết định kiến trúc: module hiện chỉ cần một DAO, chưa cần service chứa business transaction.
- Tránh tạo service giả hoặc logic thừa.

### `index.jsp`

Trách nhiệm:

- Render giao diện category management.
- Hiển thị filter, bảng dữ liệu, phân trang, modal thêm/sửa, và chế độ in.
- Không truy cập database.

## Dữ liệu đầu vào

### GET `/category-management`

- `keyword`: từ khóa tìm kiếm tên/mô tả.
- `status`: trạng thái `active` hoặc `inactive`.
- `parentName`: tên nhóm cha hoặc `gốc` để lọc nhóm gốc.
- `page`: trang hiện tại.
- `printMode`: bật giao diện in danh sách.

### POST `/category-management`

- `action`: `add` hoặc `update`.
- `categoryId`: mã nhóm hàng khi cập nhật.
- `name`: tên nhóm hàng.
- `parentName`: tên nhóm cha.
- `status`: trạng thái nhóm hàng.
- `description`: mô tả.

## Dữ liệu đầu ra

### Request attributes cho JSP

- `categories`: danh sách nhóm hàng hiển thị trong bảng.
- `parentOptions`: danh sách nhóm hàng active dùng trong dropdown nhóm cha.
- `totalItems`: tổng số nhóm hàng theo bộ lọc.
- `totalRootCategories`: số nhóm gốc theo bộ lọc.
- `totalLinkedProducts`: số sản phẩm liên kết theo bộ lọc.
- `currentPage`: trang hiện tại.
- `totalPages`: tổng số trang.
- `keyword`: từ khóa đang lọc.
- `selectedStatus`: trạng thái đang lọc.
- `parentNameFilter`: nhóm cha đang lọc.
- `printMode`: trạng thái in danh sách.

### Session flash messages

- `message`: nội dung thông báo sau thao tác.
- `messageType`: kiểu thông báo `success`, `danger`, hoặc `warning`.

## Sơ đồ luồng xử lý dạng text

```text
Browser
  ↓
/category-management
  ↓
CategoryManagementServlet
  ↓
CategoryDAO
  ↓
DBFinora Category / Product tables
  ↓
CategoryDAO maps ResultSet to Category
  ↓
CategoryManagementServlet sets request attributes
  ↓
web/WEB-INF/views/category-management/index.jsp
  ↓
HTML response to Browser
```

## Luồng thêm/cập nhật

```text
Add/Edit Modal
  ↓
POST /category-management
  ↓
CategoryManagementServlet
  ↓
Validate required fields, duplicate name, and parent-child safety
  ↓
CategoryDAO.addCategory() or CategoryDAO.updateCategory()
  ↓
Database
  ↓
Set session flash message
  ↓
Redirect /category-management
```

# Refactor Summary

## Những gì đã thay đổi

- Refactor category model vào source chính thức `src/java/category/model/Category.java`.
- Refactor category DAO vào `src/java/category/dao/CategoryDAO.java` với tên hàm rõ ràng và helper nhỏ hơn.
- Thay controller skeleton bằng `CategoryManagementServlet` xử lý route `/category-management`.
- Chuyển giao diện hoàn chỉnh từ `category/categories.jsp` sang `web/WEB-INF/views/category-management/index.jsp`.
- Cập nhật `web.xml` để `/category-management` đi vào controller thật thay vì skeleton page.
- Bổ sung tài liệu review module tại `docs/refactor-review/CATEGORY_MODULE_REVIEW.md`.
- Bổ sung kế hoạch repository-local tại `docs/planning/category-refactor/CATEGORY_REFACTOR_PLAN.md`.

## Những gì giữ nguyên

- Không refactor `ProductServlet.java` theo xác nhận của user.
- Không thay đổi schema database.
- Không thay đổi protected modules như authentication, authorization, payment, finance.
- Không thay đổi tên bảng/cột SQL đang được DAO sử dụng.
- Giữ nguyên nghiệp vụ chính của category:
  - danh sách có filter và phân trang,
  - thống kê,
  - thêm nhóm hàng,
  - cập nhật nhóm hàng,
  - kiểm tra trùng tên,
  - kiểm tra quan hệ cha-con để tránh vòng lặp.

## Lý do thay đổi

- Code cũ nằm ngoài source chính thức nên khó build, review, và bảo trì.
- DAO skeleton chưa map dữ liệu thật.
- Controller skeleton chưa thể hiện luồng xử lý category.
- JSP chính thức chỉ là TODO, chưa có giao diện hoàn chỉnh.
- Tên method và tổ chức code cần rõ hơn để junior developer đọc được luồng xử lý.

## Lợi ích đạt được

- Người review có thể đọc theo từng lớp: Controller → DAO → Database → JSP.
- Mỗi method có trách nhiệm rõ ràng hơn.
- SQL filter dùng chung giữa list và count nên dễ kiểm tra tính nhất quán.
- Form add/update có luồng validate rõ ràng.
- Route `/category-management` khớp cấu trúc module hiện tại.
- Tài liệu giải thích module giúp người mới trình bày lại luồng xử lý với team lead hoặc quản lý.

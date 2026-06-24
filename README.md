# StoreManagementNetBeans

Project Java Web MVC mở được bằng NetBeans.

## Công nghệ
- Java 17
- Maven Web Application `.war`
- Jakarta Servlet/JSP dùng cho Tomcat 10+
- SQL Server JDBC Driver

## Cấu trúc chính
```text
src/main/java/com/storemanagement/
  controller/   Servlet điều hướng request, chia theo module: auth, user, product, inventory...
  model/        Entity / JavaBean
  dao/          DAO chia theo module: user, product, customer, finance...
  service/      Business logic chia theo module tương ứng DAO
  util/         Chia theo nhóm: database, security, validation và util riêng từng module
  filter/       Authentication / authorization filter

src/main/webapp/
  views/        JSP theo từng module
  assets/       CSS, JS, images
  WEB-INF/      web.xml
```

## Cách mở trong NetBeans
1. Giải nén file `.zip`.
2. Mở NetBeans → File → Open Project.
3. Chọn thư mục `StoreManagement_NetBeans`.
4. Chuột phải project → Properties → Run → chọn Tomcat 10+.
5. Sửa thông tin SQL Server trong `src/main/java/com/storemanagement/util/database/DBContext.java`.
6. Run project.

## Lưu ý nếu bạn dùng Tomcat 9
Project hiện đang dùng `jakarta.servlet.*` cho Tomcat 10. Nếu dùng Tomcat 9, đổi toàn bộ `jakarta.servlet` thành `javax.servlet` và đổi dependency servlet trong `pom.xml` sang `javax.servlet-api`.

## Cấu trúc package theo chức năng
Xem thêm `PACKAGE_STRUCTURE.md` để biết chi tiết controller/dao/service/util của từng nhóm chức năng.

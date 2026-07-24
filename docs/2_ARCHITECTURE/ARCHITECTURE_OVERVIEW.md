# Tổng Quan Kiến Trúc Hệ Thống FinoraRetail

## 1. Giới Thiệu

FinoraRetail là hệ thống quản lý bán lẻ được xây dựng trên nền tảng Java Enterprise với kiến trúc MVC (Model-View-Controller) theo phong cách lập trình hướng đối tượng. Dự án sử dụng Apache Tomcat 10.1 làm servlet container, Jakarta Servlet/JSP API phiên bản 6.0, Java 17, và Microsoft SQL Server 2022 làm hệ quản trị cơ sở dữ liệu với tên cơ sở dữ liệu DBFinoraV2. Hệ thống được đóng gói dưới dạng WAR (Web Archive) và triển khai trên máy chủ Tomcat thông qua Maven build system.

Dự án được thiết kế theo mô hình phân lớp (layered architecture) với các tầng rõ ràng: Controller xử lý điều hướng và nhận yêu cầu từ phía người dùng, Service chứa logic nghiệp vụ, DAO (Data Access Object) đảm nhiệm việc truy cập cơ sở dữ liệu, và Model đại diện cho các thực thể trong hệ thống. Kiến trúc này đảm bảo tính tách biệt trách nhiệm (separation of concerns), giúp hệ thống dễ bảo trì, mở rộng và kiểm thử.

Hiện tại, hệ thống đã hoàn thành phần nền tảng foundation bao gồm toàn bộ cấu trúc package, 19 domain model, 17 DAO class (đang ở dạng skeleton), 17 Controller servlet, và 1 AuthFilter xác thực người dùng. Lớp Service đang ở giai đoạn skeleton với 19 service class chờ triển khai logic nghiệp vụ thực tế. Hệ thống có 65 JSP view phục vụ giao diện người dùng cho các chức năng khác nhau.

## 2. Mô Hình MVC (Model-View-Controller)

### 2.1. Tổng Quan Mô Hình MVC

Mô hình MVC là kiến trúc phần mềm phổ biến trong phát triển ứng dụng web, đặc biệt hiệu quả với các framework sử dụng Jakarta Servlet/JSP. MVC chia ứng dụng thành ba thành phần chính: Model (dữ liệu và logic nghiệp vụ), View (hiển thị giao diện người dùng), và Controller (điều phối luồng xử lý giữa Model và View). Trong ngữ cảnh Jakarta EE, servlet đóng vai trò Controller, JSP đóng vai trò View, và các POJO (Plain Old Java Object) cùng Service class đóng vai trò Model.

Mô hình MVC mang lại nhiều lợi ích quan trọng cho hệ thống FinoraRetail. Thứ nhất, sự phân tách rõ ràng giữa các thành phần cho phép đội phát triển làm việc độc lập trên từng lớp mà không ảnh hưởng đến các lớp khác. Thứ hai, việc tách biệt logic nghiệp vụ khỏi giao diện giúp dễ dàng thay đổi UI mà không cần sửa đổi code nghiệp vụ. Thứ ba, kiến trúc này tạo điều kiện thuận lợi cho việc kiểm thử đơn vị (unit testing) vì mỗi thành phần có thể được test riêng biệt. Cuối cùng, tính modular của MVC hỗ trợ việc tái sử dụng code hiệu quả.

### 2.2. Model Trong FinoraRetail

Tầng Model trong FinoraRetail bao gồm các domain entity và service class. Domain entity là các POJO chứa dữ liệu của các đối tượng trong hệ thống như User, Product, Category, Customer, Order, v.v. Các entity này được đặt trong package `com.storemanagement.model` và được thiết kế theo nguyên tắc JavaBean với các getter/setter method và constructor mặc định. Tầng Model cũng bao gồm 19 service class trong package `com.storemanagement.service`, hiện đang ở dạng skeleton chờ triển khai logic nghiệp vụ.

Hệ thống có 19 domain model chính: User, Role, Product, Category, Customer, Supplier, Store, Order, OrderDetail, Payment, Invoice, Expense, Income, InventoryItem, StockTransaction, PurchaseOrder, PurchaseDetail, ActivityLog, và Notification. Mỗi model tương ứng với một bảng trong cơ sở dữ liệu SQL Server và được thiết kế để phản ánh chính xác cấu trúc dữ liệu nghiệp vụ. Một số model có quan hệ composition với nhau, ví dụ Order chứa danh sách OrderDetail, PurchaseOrder chứa danh sách PurchaseDetail.

### 2.3. View Trong FinoraRetail

Tầng View được triển khai thông qua 65 file JSP nằm trong thư mục `src/main/webapp/views/`. Mỗi JSP file chịu trách nhiệm hiển thị giao diện người dùng cho một chức năng cụ thể, sử dụng JSTL (JSP Standard Tag Library) và EL (Expression Language) để render dữ liệu được truyền từ servlet thông qua request attribute. Các JSP được tổ chức theo cấu trúc thư mục theo chức năng, giúp dễ dàng quản lý và định vị source code.

Thư mục `views/` được chia thành nhiều subdirectory theo module nghiệp vụ. Thư mục `auth/` chứa các JSP liên quan đến xác thực như login.jsp, register.jsp, và forgot-password.jsp. Thư mục `common/` chứa các thành phần dùng chung như header.jsp, sidebar.jsp, và footer.jsp được include vào các trang khác. Các thư mục còn lại tương ứng với từng module: dashboard, categories, customers, products, suppliers, stores, orders, inventory, payments, invoices, expenses, income, purchase-orders, reports, users, roles, profile, activity-log, notifications, configuration, seo, và about.

Nguyên tắc quan trọng trong thiết kế View là JSP không được phép truy cập trực tiếp vào cơ sở dữ liệu, không được khởi tạo DAO, và không chứa logic nghiệp vụ phức tạp. JSP chỉ đóng vai trò hiển thị dữ liệu từ request attribute và xử lý các thao tác UI đơn giản như hiển thị điều kiện và vòng lặp. Mọi xử lý dữ liệu phức tạp phải được thực hiện ở tầng Controller hoặc Service trước khi truyền xuống View.

### 2.4. Controller Trong FinoraRetail

Tầng Controller được triển khai bằng 17 servlet class đặt trong package `com.storemanagement.controller`. Mỗi servlet xử lý các HTTP request cho một nhóm chức năng liên quan, sử dụng annotation `@WebServlet` để khai báo URL pattern thay vì cấu hình trong web.xml. Điều này giúp code gọn gàng hơn và mỗi servlet tự định nghĩa các endpoint mà nó xử lý.

Tất cả controller trong hệ thống đều kế thừa từ `BaseController` trong package `com.storemanagement.controller.common`. BaseController cung cấp hai phương thức helper là `forward()` và `redirect()` giúp điều hướng request một cách nhất quán. Phương thức `forward()` chuyển tiếp request đến JSP view trong thư mục `/views/`, còn `redirect()` gửi HTTP redirect về browser để chuyển hướng đến URL khác. Việc kế thừa từ BaseController đảm bảo tất cả servlet có cùng cách xử lý điều hướng và giảm code trùng lặp.

Các servlet chính trong hệ thống bao gồm: AuthController xử lý đăng nhập/đăng ký/đăng xuất, DashboardController quản lý trang tổng quan, UserController quản lý người dùng, CategoryController quản lý danh mục sản phẩm, ProductController quản lý sản phẩm, CustomerController quản lý khách hàng, SupplierController quản lý nhà cung cấp, StoreController quản lý cửa hàng, OrderController quản lý đơn hàng, InventoryController quản lý tồn kho, PaymentController quản lý thanh toán, InvoiceController quản lý hóa đơn, ExpenseController và IncomeController quản lý chi phí và thu nhập, PurchaseOrderController quản lý đơn đặt hàng, ReportController tạo báo cáo, ProfileController quản lý thông tin cá nhân, StaticPageController xử lý trang tĩnh, và ActivityLogController ghi log hoạt động.

## 3. Vòng Đời Request (Request Lifecycle)

### 3.1. Chuỗi Xử Lý Request Từ Browser Đến Database

Vòng đời của một HTTP request trong FinoraRetail tuân theo một chuỗi xử lý có trật tự rõ ràng từ khi user gửi yêu cầu từ trình duyệt cho đến khi nhận được phản hồi. Chuỗi này đảm bảo mọi request đều được xác thực trước khi truy cập tài nguyên bảo mật, và mọi thao tác cơ sở dữ liệu đều đi qua tầng DAO để đảm bảo tính nhất quán và an toàn dữ liệu.

Bước đầu tiên, trình duyệt của người dùng gửi HTTP request đến server Tomcat thông qua cổng 8080 (mặc định) hoặc cổng được cấu hình. Request này có thể là GET để yêu cầu hiển thị trang hoặc POST để gửi dữ liệu form. Tomcat tiếp nhận request và chuyển đến servlet container để xử lý.

Bước thứ hai, AuthFilter kiểm tra tính hợp lệ của request. AuthFilter được ánh xạ (mapped) đến 21 route pattern bao gồm /dashboard/*, /users/*, /suppliers/*, /customers/*, /products/*, /stores/*, /orders/*, /inventory/*, /reports/*, /profile/*, /roles/*, /categories/*, /payments/*, /invoices/*, /expenses/*, /income/*, /purchase-orders/*, /activity-log/*, /notifications/*, /configuration/*, và /seo/*. Khi request đến các URL này, AuthFilter kiểm tra HttpSession để xác định user đã đăng nhập hay chưa bằng cách đọc attribute "currentUser". Nếu session không tồn tại hoặc không có currentUser, filter chuyển hướng (redirect) user đến trang /login. Ngược lại, request được cho phép đi tiếp trong filter chain.

Bước thứ ba, servlet tương ứng với URL pattern nhận request. Ví dụ, request đến /products/list sẽ được xử lý bởi ProductController. Servlet phân tích request parameters, validates dữ liệu đầu vào, và xác định action cần thực hiện dựa trên HTTP method (GET/POST) và path info. Với các request GET, servlet thường gọi DAO để lấy dữ liệu và thiết lập request attributes. Với các request POST, servlet xử lý dữ liệu form, gọi Service/DAO để thực hiện nghiệp vụ, và redirect về trang danh sách hoặc hiển thị thông báo thành công.

Bước thứ tư, DAO thực hiện truy vấn cơ sở dữ liệu. Mỗi DAO có quyền sở hữu SQL queries liên quan đến domain của nó và chịu trách nhiệm mở kết nối đến SQL Server thông qua DBContext, thực thi PreparedStatement để truy vấn hoặc cập nhật dữ liệu, xử lý ResultSet để chuyển đổi thành domain object, và đóng kết nối trong finally block để đảm bảo tài nguyên được giải phóng. Tất cả SQL queries phải sử dụng PreparedStatement với parameter binding để ngăn chặn SQL injection.

Bước thứ năm, servlet nhận kết quả từ DAO và thiết lập các request attributes. Servlet đặt dữ liệu cần hiển thị vào request scope thông qua phương thức `setAttribute()`. Các thuộc tính phổ biến bao gồm: "items" chứa danh sách đối tượng để hiển thị, "item" chứa một đối tượng cụ thể cho trang chi tiết, "message" hoặc "success" để hiển thị thông báo, và "errors" để hiển thị validation errors. Đối với flash messages (thông báo hiển thị sau khi redirect), servlet sử dụng session attribute thay vì request attribute.

Bước thứ sáu, servlet forward request đến JSP view tương ứng. Servlet gọi phương thức `forward()` từ BaseController với đường dẫn đến JSP trong thư mục /views/. Ví dụ, để hiển thị trang danh sách sản phẩm, servlet forward đến "products/list". Cơ chế forward của Servlet API cho phép request và response object được chuyển tiếp mà không cần tạo request mới, giữ nguyên các attributes đã thiết lập.

Bước thứ bảy, JSP render giao diện người dùng. JSP đọc các attributes từ request scope sử dụng EL (Expression Language) như `${items}` hoặc `${item.name}`. JSP sử dụng JSTL tags như `<c:forEach>`, `<c:if>`, `<c:choose>` để hiển thị dữ liệu động. JSP có thể include các fragment như header, sidebar, footer từ thư mục common/. Kết quả render là HTML response được gửi về trình duyệt.

### 3.2. Minh Họa Chi Tiết Với Ví Dụ Thực Tế

Để minh họa rõ hơn vòng đời request, chúng ta theo dõi một yêu cầu hiển thị danh sách sản phẩm. Đầu tiên, user điều hướng đến URL http://localhost:8080/FinoraRetail/products/list. Trình duyệt gửi GET request đến Tomcat. AuthFilter kiểm tra session thấy có currentUser, cho phép request đi tiếp. ProductController nhận request, xác định action là hiển thị danh sách, và gọi ProductDAO.findAll().

ProductDAO mở kết nối đến SQL Server DBFinoraV2, thực thi SQL query "SELECT * FROM Products" sử dụng PreparedStatement, duyệt qua ResultSet và tạo danh sách Product object, sau đó đóng kết nối. ProductDAO trả về List<Product> cho ProductController. ProductController đặt danh sách này vào request attribute: `request.setAttribute("products", productList)`. Controller sau đó forward đến JSP: `forward(request, response, "products/list")`.

JSP products/list.jsp nhận request, đọc attribute `${products}` và render HTML table hiển thị danh sách sản phẩm. Trong quá trình render, JSP include header.jsp và sidebar.jsp từ common/ để tạo layout hoàn chỉnh. Kết quả HTML được gửi về trình duyệt và hiển thị cho user.

## 4. Kiến Trúc Phân Lớp (Layered Architecture)

### 4.1. Tầng Controller

Tầng Controller đóng vai trò điều phối trung tâm trong kiến trúc FinoraRetail, tiếp nhận mọi HTTP request từ người dùng và điều hướng chúng đến các tầng xử lý phù hợp. Controller không chứa logic nghiệp vụ phức tạp mà tập trung vào các nhiệm vụ: phân tích request parameters và headers, xác thực dữ liệu đầu vào cơ bản, gọi Service hoặc DAO để xử lý nghiệp vụ, thiết lập request/session attributes cho View, và quyết định điều hướng (forward hoặc redirect).

Tất cả 17 servlet controller đều được đánh dấu bằng annotation `@WebServlet` với các URL pattern cụ thể. Ví dụ, ProductController được ánh xạ đến "/products/*", có nghĩa nó xử lý mọi request bắt đầu bằng /products/. Bên trong servlet, method `doGet()` xử lý các request đọc (hiển thị form, danh sách, chi tiết), còn `doPost()` xử lý các request ghi (tạo mới, cập nhật, xóa). Pattern này tuân theo nguyên tắc RESTful cơ bản với GET cho resource retrieval và POST cho state-changing operations.

Controller có quyền gọi trực tiếp DAO cho các thao tác đơn giản, nhưng với các nghiệp vụ phức tạp liên quan đến nhiều DAO, Controller nên thông qua Service layer. Chính sách này được ghi chú rõ: "controllers may call DAOs directly for simple flows, real service logic only for multi-DAO workflows". Ví dụ, hiển thị danh sách sản phẩm là thao tác đơn giản nên Controller có thể gọi ProductDAO trực tiếp, nhưng tạo đơn hàng với nhiều validation và cập nhật inventory nên được xử lý qua OrderService.

### 4.2. Tầng Service

Tầng Service trong FinoraRetail hiện có 19 service class đặt trong package `com.storemanagement.service` và các subpackage theo domain. Các service class được thiết kế để chứa logic nghiệp vụ chính của ứng dụng, nhưng hiện tại đang ở giai đoạn skeleton meaning chưa có triển khai thực tế. Mỗi domain có một service tương ứng: UserService, CategoryService, ProductService, CustomerService, SupplierService, StoreService, OrderService, InventoryItemService, PaymentService, InvoiceService, ExpenseService, IncomeService, PurchaseOrderService, v.v.

Service layer đóng vai trò quan trọng trong việc tách biệt logic nghiệp vụ khỏi presentation layer (Controller) và data access layer (DAO). Khi Service được triển khai đầy đủ, nó sẽ chịu trách nhiệm về: business rules enforcement (ví dụ: kiểm tra tồn kho trước khi tạo đơn hàng), transaction management (đảm bảo tính nhất quán khi cập nhật nhiều bảng), coordination giữa nhiều DAO (ví dụ: tạo đơn hàng cần OrderDAO và InventoryItemDAO), và validation phức tạp không thể thực hiện ở Controller.

Trong cấu trúc package hiện tại, các service được tổ chức theo domain module: service.user, service.product, service.customer, service.supplier, service.store, service.sales, service.purchase, service.finance, service.inventory, service.system. Ngoài ra có service.common.GenericService và các service trong service.common. Cấu trúc này nhất quán với cách tổ chức DAO và Controller, giúp developer dễ dàng định vị code liên quan đến một domain.

### 4.3. Tầng DAO (Data Access Object)

Tầng DAO là nơi duy nhất trong ứng dụng được phép tương tác trực tiếp với cơ sở dữ liệu SQL Server. FinoraRetail có 17 DAO class với cấu trúc nhất quán: mỗi DAO extends từ `BaseDAO<T>` trong package `com.storemanagement.dao.common` và implement `ICrudDAO<T>` interface. BaseDAO cung cấp phương thức `getConnection()` để lấy kết nối từ DBContext, và skeleton implementation cho 5 CRUD operations: findAll(), findById(), insert(), update(), delete().

Các DAO class được phân theo domain và subpackage: dao.user (UserDAO, RoleDAO), dao.product (ProductDAO, CategoryDAO), dao.customer (CustomerDAO), dao.supplier (SupplierDAO), dao.store (StoreDAO), dao.sales (OrderDAO), dao.purchase (PurchaseOrderDAO), dao.finance (PaymentDAO, InvoiceDAO, ExpenseDAO, IncomeDAO), dao.inventory (InventoryItemDAO, StockTransactionDAO), dao.system (ActivityLogDAO, BusinessConfigurationDAO). Mỗi DAO chịu trách nhiệm quản lý một hoặc một nhóm bảng liên quan trong cơ sở dữ liệu.

Nguyên tắc quan trọng nhất của tầng DAO là không được phép import bất kỳ servlet API nào (jakarta.servlet.*). DAO phải là pure Java class không phụ thuộc vào HTTP request/response/session. Điều này đảm bảo DAO có thể được sử dụng trong nhiều context khác nhau: trong servlet, trong service layer, hoặc trong batch processing. Ngoài ra, tất cả SQL queries phải sử dụng PreparedStatement với parameter binding để ngăn chặn SQL injection attacks.

### 4.4. Tầng Database

Cơ sở dữ liệu DBFinoraV2 trong SQL Server chứa 21 bảng biểu diễn các thực thể trong hệ thống quản lý bán lẻ. Schema được thiết kế để hỗ trợ nghiệp vụ multi-store (nhiều cửa hàng), với các bảng core như Users, Roles, Stores, và các bảng nghiệp vụ như Products, Categories, Customers, Suppliers, Orders, OrderDetails, Payments, Invoices, Expenses, Income, InventoryItems, StockTransactions, PurchaseOrders, PurchaseDetails, ActivityLogs, Notifications, và BusinessConfigurations.

Kết nối đến database được quản lý thông qua lớp DBContext trong package `util.database`. DBContext sử dụng JDBC Driver của SQL Server (mssql-jdbc) với connection string kết nối đến server localhost:1433 và database DBFinoraV2. Các thông số kết nối được cấu hình trong DBContext với username "sa" và password "12345". Cấu hình này phù hợp cho môi trường phát triển, nhưng trong production cần sử dụng external configuration hoặc environment variables để tránh hardcode credentials.

## 5. Cấu Trúc Package

## 5. Cấu Trúc Package

### 5.1. Thư Mục Nguồn `src/main/java`

Toàn bộ mã nguồn Java được tổ chức dưới dạng các package gốc trực tiếp (`controller`, `dao`, `model`, `service`, `filter`, `util`, `dto`, `constant`). Dự án không sử dụng tiền tố `com.storemanagement`.

### 5.2. Package Controller (`controller/`)

Chứa các Servlet điều khiển xử lý HTTP requests (30+ servlets chia theo domain: `auth`, `branch`, `common`, `customer`, `dashboard`, `finance`, `inventory`, `pos`, `product`, `purchase`, `report`, `sales`, `supplier`, `system`, `user`, `vnpay`, `warehouse`, `website`). Kế thừa từ `controller.common.BaseController` hoặc `HttpServlet`.

### 5.3. Package DAO (`dao/`)

Chứa các Data Access Objects truy xuất dữ liệu SQL Server (`DBFinoraV2`). Tất cả các DAO kế thừa từ `util.database.DBContext` và triển khai interface `dao.common.ICrudDAO`. Phân chia theo domain: `branch`, `customer`, `dashboard`, `employee`, `finance`, `inventory`, `product`, `purchase`, `report`, `sales`, `supplier`, `system`, `user`.

### 5.4. Package Model (`model/`)

Chứa 51 POJO domain model entities đại diện cho dữ liệu trong DB và các báo cáo tổng quan (`ActivityLog`, `AuditLog`, `Branch`, `Category`, `Customer`, `Employee`, `Inventory`, `Invoice`, `Order`, `Payment`, `Product`, `PurchaseOrder`, `SalesTransaction`, `Shift`, `StockTransfer`, `Supplier`, `Warehouse`, v.v.).

### 5.5. Package Service (`service/`)

Chứa tầng xử lý logic nghiệp vụ trung gian bridging giữa Controller và DAO. Phân chia theo các domain: `customer`, `employee`, `finance`, `inventory`, `purchase`, `supplier`, `system`, `vnpay`.

### 5.6. Package Filter (`filter/`)

Chứa `SecurityFilter.java` (`@WebFilter(urlPatterns = "/*")`). Quản lý tập trung authentication, phân quyền RBAC (`ROLE_MAP`), kiểm tra CSRF token cho POST requests, thiết lập Security Headers, và kích hoạt audit logging qua `ActivityLogDAO`.

### 5.7. Package Util (`util/`)

Chứa các lớp tiện ích toàn hệ thống: `util.database.DBContext` (kết nối DB & Migration Listener), `util.security.PasswordUtil` (BCrypt), `util.email.EmailUtil`, `util.finance.MoneyUtil`, `util.inventory` (POI Excel import/export), `util.report` (OpenPDF export), `util.vnpay` (VNPay Config), `util.pagination` (PageUtil).

Nguyên tắc khi thêm utility class: chỉ tạo utility khi có ít nhất hai call site thực sự cần sử dụng nó. Utility không nên chứa business logic mà chỉ nên chứa helper methods cho các thao tác chung như format, validation, conversion. Utility nên là stateless và thread-safe nếu có thể.

## 6. Định Tuyến Servlet (Servlet Routing)

### 6.1. Cơ Chế Annotation-Based Routing

FinoraRetail sử dụng annotation `@WebServlet` để khai báo servlet mapping thay vì cấu hình trong web.xml. Cơ chế này được giới thiệu từ Servlet 3.0 và là best practice hiện đại trong Jakarta EE. Mỗi servlet class được annotate với URL pattern(s) mà nó xử lý, và container tự động discover và register servlet khi ứng dụng deploy.

Ưu điểm của annotation-based routing bao gồm: code và configuration cùng một nơi dễ maintain, không cần chỉnh sửa web.xml khi thêm/sửa servlet, IDE hỗ trợ autocompletion và refactoring tốt hơn, và convention rõ ràng về URL structure. Tuy nhiên, với các servlet có logic phức tạp xử lý nhiều sub-paths, cần có thêm code để phân biệt action dựa trên request URI hoặc path info bên trong servlet.

### 6.2. URL Pattern Conventions

Hệ thống tuân theo convention URL pattern nhất quán: servlet được ánh xạ đến đường dẫn dạng /{resource}/* hoặc /{resource}/{action}. Ví dụ, ProductController xử lý /products/*, có nghĩa tất cả request như /products/list, /products/add, /products/edit?id=1 đều được chuyển đến ProductController. Bên trong servlet, code phân biệt action dựa trên request URI hoặc path info.

Các URL patterns được sử dụng: /login, /register, /forgot-password, /logout (AuthController), /dashboard/* (DashboardController), /users/* (UserController), /products/* (ProductController), /categories/* (CategoryController), /customers/* (CustomerController), /suppliers/* (SupplierController), /stores/* (StoreController), /orders/* (OrderController), /inventory/* (InventoryController), /payments/* (PaymentController), /invoices/* (InvoiceController), /expenses/* (ExpenseController), /income/* (IncomeController), /purchase-orders/* (PurchaseOrderController), /reports/* (ReportController), /profile/* (ProfileController), /roles/* (RoleController), /activity-log/* (ActivityLogController), /notifications/* (NotificationController), /configuration/* (ConfigurationController), /seo/* (SeoController).

### 6.3. Cấu Hình web.xml

Mặc dù sử dụng annotation cho servlet mapping, web.xml vẫn được sử dụng cho các cấu hình ứng dụng khác. File `src/main/webapp/WEB-INF/web.xml` hiện chứa: display-name "Store Management", welcome-file-list với index.jsp là default file, và session-config với session-timeout 30 phút. File này tuân theo Jakarta EE 6.0 schema.

web.xml cũng là nơi khai báo filter mapping cho AuthFilter nếu không sử dụng annotation. Tuy nhiên, trong cấu hình hiện tại, AuthFilter sử dụng `@WebFilter` annotation nên không cần khai báo trong web.xml. Các servlet filter cần cấu hình initialization parameters hoặc có nhiều URL patterns phức tạp có thể được khai báo trong web.xml thay vì annotation.

## 7. Vị Trí JSP Views

### 7.1. Cấu Trúc Thư Mục Views

Tất cả JSP view được đặt trong thư mục `src/main/webapp/views/` với cấu trúc subdirectory theo module/feature. Mỗi subdirectory chứa các JSP file cho các chức năng cụ thể của module đó. View được tổ chức theo nguyên tắc: mỗi feature có trang list, trang detail (nếu cần), trang add/create, và trang edit/update. Ngoài ra có các trang action như cancel, transfer, export tùy theo nghiệp vụ.

Thư mục auth/ chứa login.jsp, register.jsp, forgot-password.jsp cho chức năng xác thực. Thư mục common/ chứa header.jsp, sidebar.jsp, footer.jsp là các fragment được include vào các trang khác. Thư mục dashboard/ chứa owner.jsp, financial.jsp, inventory.jsp cho các loại dashboard khác nhau. Thư mục categories/ chứa list.jsp, add.jsp, edit.jsp. Thư mục products/ chứa list.jsp, add.jsp, edit.jsp, detail.jsp, showcase.jsp, import-receipt.jsp, stock-adjustment.jsp. Thư mục customers/ chứa list.jsp, add.jsp, edit.jsp, detail.jsp, ranking.jsp. Thư mục suppliers/ chứa list.jsp, create.jsp, edit.jsp. Thư mục stores/ chứa list.jsp, add.jsp, edit.jsp, detail.jsp. Thư mục orders/ chứa list.jsp, create.jsp, detail.jsp, update.jsp, cancel.jsp. Thư mục inventory/ chứa dashboard.jsp, report.jsp, transfer.jsp, export.jsp, import.jsp. Thư mục payments/ chứa index.jsp. Thư mục invoices/ chứa list.jsp. Thư mục expenses/ chứa list.jsp, add.jsp. Thư mục income/ chứa list.jsp. Thư mục purchase-orders/ chứa list.jsp, detail.jsp. Thư mục reports/ chứa sales-by-store.jsp, customer-loyal.jsp, inventory.jsp, employee-sales.jsp, export.jsp. Thư mục users/ chứa list.jsp, create.jsp, edit.jsp. Thư mục roles/ chứa list.jsp. Thư mục profile/ chứa index.jsp, change-password.jsp. Thư mục activity-log/ chứa list.jsp. Thư mục notifications/ chứa list.jsp. Thư mục configuration/ chứa business.jsp. Thư mục seo/ chứa index.jsp. Ngoài ra还有contact.jsp và about.jsp ở root views/.

### 7.2. Cơ Chế Forward Từ Servlet

Servlet forward đến JSP sử dụng phương thức `request.getRequestDispatcher("/views/" + view + ".jsp").forward(request, response)`. BaseController cung cấp helper method `forward(request, response, "products/list")` để forward đến `/views/products/list.jsp`. Cơ chế forward giữ nguyên request object nên các attributes được set trước đó vẫn có thể đọc được trong JSP.

Khi servlet xử lý POST request và thực hiện thay đổi dữ liệu, pattern phổ biến là redirect sau POST (Post-Redirect-Get pattern) thay vì forward trực tiếp. PRG pattern giúp tránh duplicate form submission khi user refresh trang. Để hiển thị thông báo thành công sau redirect, servlet đặt message vào session attribute, và JSP đọc từ session rồi xóa đi (flash message pattern).

### 7.3. Common Fragments

Thư mục views/common/ chứa các JSP fragment dùng chung cho toàn bộ ứng dụng. File header.jsp chứa phần đầu trang với logo, navigation bar, user menu, và notification icon. File sidebar.jsp chứa menu điều hướng bên trái với các link đến các module khác nhau. File footer.jsp chứa phần chân trang với copyright và version info.

Các trang JSP include common fragments sử dụng JSP include directive: `<%@ include file="../common/header.jsp" %>` hoặc `<jsp:include page="../common/header.jsp" />`. Header và sidebar thường chứa dynamic content nên sử dụng `<jsp:include>` để cho phép servlet xử lý trước khi include. Common fragments có thể đọc session attributes để hiển thị thông tin user hiện tại trong header và active menu item trong sidebar dựa trên request URI.

## 8. Trạng Thái Kiến Trúc Hiện Tại

### 8.1. Foundation Hoàn Chỉnh

Hệ thống đã hoàn thành phần nền tảng foundation với toàn bộ cấu trúc package, infrastructure classes, và skeleton cho tất cả layers. Foundation bao gồm: 19 domain model với đầy đủ class definitions và field declarations, 17 DAO class với BaseDAO và ICrudDAO infrastructure, 17 Controller servlet với BaseController và routing conventions, 1 AuthFilter với 21 protected route patterns, 10 utility class cho cross-cutting concerns, 65 JSP view với layout fragments, và web.xml/context.xml configuration.

AuthController là servlet duy nhất có triển khai thực tế (demo login) ngoài skeleton. Demo login tạo một User object với username từ request parameter và role "OWNER", đặt vào session attribute "currentUser", và redirect đến /dashboard/owner. Điều này cho phép testing ứng dụng mà không cần database connection hoàn chỉnh. Các servlet khác chỉ có skeleton code với placeholder comments.

### 8.2. Service Layer Skeleton

19 service class hiện đang ở dạng skeleton với class declaration và method signatures nhưng chưa có triển khai logic. Mỗi service class được tạo theo convention: package theo domain, tên class theo pattern {Domain}Service, kế thừa hoặc sử dụng related DAO. Service methods có thể bao gồm: findAll(), findById(), create(), update(), delete(), và các method nghiệp vụ đặc thù như findByCategory(), search(), generateReport().

Priority cho việc triển khai service layer nên theo thứ tự: đầu tiên là UserService và RoleService vì auth phụ thuộc vào nó, sau đó là CategoryService và ProductService vì là core domain, tiếp theo là OrderService và InventoryItemService vì liên quan đến nghiệp vụ chính, và cuối cùng là các service khác cho finance, reports, và system features.

### 8.3. Next Steps Cho Development

Bước phát triển tiếp theo được khuyến nghị: triển khai UserDAO với password hashing và authentication thực sự thay thế demo login, triển khai CategoryDAO và ProductDAO để enable CRUD operations cho sản phẩm, triển khai OrderService và OrderDAO cho nghiệp vụ bán hàng core, triển khai InventoryItemService và StockTransactionDAO để theo dõi tồn kho chính xác, triển khai các finance service cho payment, invoice, expense, income, và cuối cùng là reporting và dashboard features.

## 9. Các Quyết Định Thiết Kế Quan Trọng

### 9.1. DAO Sở Hữu SQL

Quyết định thiết kế cốt lõi là mỗi DAO class sở hữu hoàn toàn SQL queries liên quan đến domain của nó. Không DAO nào được phép truy cập bảng của DAO khác. Điều này đảm bảo single responsibility principle: mỗi DAO chỉ quan tâm đến một domain và các bảng thuộc domain đó. Nếu cần lấy data từ nhiều domain, code phải đi qua Service layer để coordinate giữa các DAO.

Quy tắc này có ngoại lệ với BaseDAO và ICrudDAO - chúng thuộc về infrastructure và có thể được sử dụng/chia sẻ bởi mọi domain DAO. Ngoài ra, một số DAO có thể có relationship tự nhiên với nhau (ví dụ: OrderDAO và OrderDetailDAO), nhưng vẫn nên tách biệt rõ ràng trong implementation.

### 9.2. Request Attributes Cho Page Data

Dữ liệu cần hiển thị trên trang JSP được truyền thông qua request attributes thay vì session attributes. Request attributes chỉ tồn tại trong một request-response cycle, phù hợp cho dữ liệu page-specific như danh sách items, pagination info, search results. Session attributes được reserved cho dữ liệu user-specific tồn tại across requests như currentUser, user preferences.

Pattern truyền data phổ biến: Controller gọi DAO/Service để lấy data, Controller set data vào request attribute (`request.setAttribute("items", list)`), Controller forward đến JSP, JSP đọc attribute (`${items}` hoặc JSTL `<c:forEach>`). Flash messages (hiển thị sau redirect) sử dụng session attribute với pattern: set vào session, redirect, JSP đọc và xóa.

### 9.3. Không Direct DB Access Trong JSP

Quy tắc nghiêm ngặt: JSP không được phép mở database connection, không được khởi tạo DAO object, không được viết SQL query. JSP chỉ đọc data từ request/session/pageContext attributes đã được set bởi servlet. Điều này đảm bảo separation of concerns, security (tránh SQL injection trong JSP), và maintainability (logic truy cập data tập trung trong Java code được type-checked và version-controlled).

JSTL và EL được sử dụng để hiển thị data động trong JSP. JSTL c.tags cho logic như conditionals và loops, EL cho property access. Nếu cần format data (dates, numbers, currencies), sử dụng JSTL fmt tags hoặc custom taglibs thay vì scriptlet code.

### 9.4. PreparedStatement Bắt Buộc

Tất cả SQL queries trong DAO phải sử dụng PreparedStatement với parameter binding thay vì String concatenation. Pattern bắt buộc: `PreparedStatement stmt = connection.prepareStatement(sql); stmt.setString(1, param); stmt.executeQuery();`. Việc này ngăn chặn SQL injection attacks và cũng improves performance với repeated queries.

Connection và Statement resources phải được đóng trong finally block hoặc sử dụng try-with-resources để đảm bảo không leak. BaseDAO cung cấp `getConnection()` helper method, nhưng mỗi DAO method phải quản lý connection lifecycle của riêng nó: get connection, use it, close it in finally.

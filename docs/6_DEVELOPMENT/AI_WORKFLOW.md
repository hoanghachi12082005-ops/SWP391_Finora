# Quy Trình Làm Việc Cho Agent AI - AI Workflow

> **Mục đích:** Tài liệu này quy định quy trình bắt buộc mà mọi agent AI phải tuân thủ khi thực hiện thay đổi trong dự án FinoraRetail (SWP391_Finora). Việc tuân thủ quy trình đảm bảo tính nhất quán, giảm rủi ro phá vỡ mã hiện có, và duy trì chất lượng kiến trúc hệ thống.

---

## 1. Phân Tích Trước Khi Thực Hiện (PRE-IMPLEMENTATION ANALYSIS)

### 1.1. Bước Bắt Buộc Trước Mọi Thay Đổi

**Đọc và tuân thủ AGENTS.md trước tiên.**

Mọi thay đổi, dù lớn hay nhỏ, đều phải bắt đầu bằng việc đọc kỹ `AGENTS.md` tại thư mục gốc của repository. File này chứa hợp đồng kiến trúc, ranh giới bảo vệ (protected modules), và các quy tắc bắt buộc của dự án.

### 1.2. Các Nguồn Tài Liệu Phải Xem Xét

Tùy theo phạm vi thay đổi, agent phải đọc các file sau theo thứ tự ưu tiên:

**1. Kiến trúc và cấu hình:**

- `pom.xml` — Cấu hình Maven, dependencies, plugin.
- `web/WEB-INF/web.xml` — Cấu hình servlet, URL mapping, welcome files.
- `web/META-INF/context.xml` — Tomcat context, data source, credentials.
- `build.xml` (nếu có) — Cấu hình Ant build.

**2. Mã nguồn liên quan:**

- Các servlet/controller liên quan trong `src/java/controller`.
- Các DAO class liên quan trong `src/java/dao`.
- Các model/entity liên quan trong `src/java/model`.
- Các JSP view liên quan trong `web/WEB-INF/views`.

**3. Cơ sở dữ liệu:**

- File SQL schema dưới `sql/` (ví dụ: `database/DBFInoraV2.sql`).

**4. Tài liệu quản trị:**

- `docs/architecture/SYSTEM_ARCHITECTURE.md` — Kiến trúc hệ thống.
- `docs/architecture/MODULE_BOUNDARIES.md` — Ranh giới giữa các module.
- `docs/architecture/FOLDER_STRUCTURE.md` — Cấu trúc thư mục.
- `docs/rules/PROTECTED_MODULES.md` — Danh sách module được bảo vệ.
- `docs/rules/CODING_STANDARDS.md` — Tiêu chuẩn lập trình.
- `docs/status/CURRENT_STATUS.md` — Trạng thái hiện tại của dự án.
- `docs/status/IMPLEMENTED_FEATURES.md` — Các tính năng đã triển khai.

### 1.3. Hiểu Trạng Thái Hiện Tại

Trước khi viết bất kỳ dòng code nào, agent phải:

- Hiểu cách tính năng hiện tại được triển khai.
- Xác định các call sites của class/method định thay đổi.
- Xác định liệu module dự định thay đổi có phải là protected area hay không.
- Đánh giá tác động lan tỏa của thay đổi.

---

## 2. Yêu Cầu Về Lập Kế Hoạch (PLANNING REQUIREMENTS)

### 2.1. Khi Nào Cần Tạo Plan

**Phải tạo plan document cho mọi thay đổi không tầm thường (non-trivial).** Các thay đổi tầm thường bao gồm: sửa lỗi chính tả, cập nhật comment, thay đổi tên biến cục bộ, thêm log message.

### 2.2. Vị Trí và Đặt Tên Plan

Plan document phải được tạo tại:

```
docs/planning/<TOPIC>/<FEATURE>_IMPLEMENTATION_PLAN.md
```

Quy ước đặt tên:

- Tên thư mục: `UPPERCASE_SNAKE_CASE` theo chủ đề.
- Tên file: `UPPERCASE_SNAKE_CASE` mô tả tính năng.
- Ví dụ: `docs/planning/CATEGORY_REFACTOR/CATEGORY_REFACTOR_PLAN.md`

### 2.3. Cấu Trúc Bắt Buộc Của Plan

Mỗi plan phải bao gồm đầy đủ các mục sau:

**1. Mục đích và Phạm vi (Purpose & Scope):**

- Mục tiêu của thay đổi.
- Phạm vi công việc cụ thể.
-边界 (boundaries) — những gì nằm trong và ngoài phạm vi.

**2. Phân tích trạng thái hiện tại (Current State Analysis):**

- Mô tả cách tính năng hoạt động hiện tại.
- Các vấn đề hoặc hạn chế của implementation hiện tại.
- Lý do cần thay đổi.

**3. Các module bị ảnh hưởng (Affected Modules):**

- Danh sách cụ thể các file/class cần thay đổi.
- Danh sách cụ thể các file/class cần tạo mới.
- Các call sites cần được cập nhật.

**4. Tác động đến protected area (Protected-Area Impact):**

- Liệt kê các protected modules có thể bị ảnh hưởng.
- Đánh giá mức độ tác động.
- Các bước bảo vệ được thực hiện.

**5. Các bước thực hiện (Implementation Steps):**

- Danh sách các bước theo thứ tự thực hiện.
- Mỗi bước phải có mô tả rõ ràng đầu ra.
- Ưu tiên thay đổi nhỏ, có thể verify sau mỗi bước.

**6. Chiến lược xác minh (Validation Strategy):**

- Cách xác minh từng bước.
- Tiêu chí thành công cụ thể.
- Cách xác minh không phá vỡ tính năng hiện có.

**7. Cập nhật tài liệu (Documentation Updates):**

- Danh sách tài liệu cần cập nhật sau khi thực hiện.
- Các file nào cần tạo mới.

**8. Câu hỏi mở (Open Questions):**

- Các vấn đề chưa có đáp án.
- Các quyết định kiến trúc cần được xác nhận.

### 2.4. Cập Nhật Index Planning

Sau khi tạo plan mới, agent phải cập nhật:

- `docs/planning/ACTIVE_TASKS.md` — nếu đây là task đang được thực hiện.
- `docs/planning/BACKLOG.md` — nếu đây là task dự kiến.
- `docs/planning/ROADMAP.md` — nếu task ảnh hưởng đến timeline lớn.

---

## 3. Yêu Cầu Thực Hiện (IMPLEMENTATION REQUIREMENTS)

### 3.1. Nguyên Tắc Thay Đổi Tối Thiểu

**Luôn ưu tiên thay đổi nhỏ nhất đúng đắn.** Không tạo abstraction khi chưa cần, không viết lại code đang hoạt động tốt.

Trước khi thêm code mới, hãy hỏi:

- Thay đổi này có giải quyết được vấn đề không?
- Đây có phải là thay đổi nhỏ nhất có thể không?
- Có ảnh hưởng đến code hiện có không?

### 3.2. Tái Sử Dụng Pattern Hiện Có

**Ưu tiên tái sử dụng pattern và cấu trúc đã có** trước khi tạo abstraction mới.

- Tham khảo `docs/patterns/REUSABLE_PATTERNS.md` và `docs/patterns/SERVICE_PATTERNS.md`.
- Bám sát các pattern đã được chấp thuận trong dự án.
- Nếu cần pattern mới, phải nêu rõ lý do trong plan.

### 3.3. Các Vùng Cấm Chỉnh Sửa

**Tuyệt đối không chỉnh sửa các thư mục và file sau:**

- `build/`, `dist/`, `target/` — Thư mục artifact của quá trình build.
- `.git/` — Thư mục quản lý phiên bản Git.
- Thư mục và file private của IDE (`nbproject/`, `.idea/`, `*.iml`, v.v.).

### 3.4. Bảo Toàn Mã Hóa Ký Tự

**Tất cả file mới tạo phải sử dụng UTF-8 without BOM.**

- Kiểm tra cấu hình editor/IDE để đảm bảo.
- Xác minh bằng hex editor nếu cần.

### 3.5. Kích Thước Method

- Method phải đủ nhỏ để có thể review trong một màn hình.
- Nếu method vượt quá ~50 dòng, cân nhắc tách thành các helper method.
- Helper method phải có single responsibility, không có side effect.

---

## 4. Yêu Cầu Xác Minh (VERIFICATION REQUIREMENTS)

### 4.1. Chuỗi Xác Minh Chuẩn

Sau khi thực hiện thay đổi mã nguồn, xác minh theo thứ tự ưu tiên sau:

**Ưu tiên 1 — Maven build:**

```powershell
mvn clean package -DskipTests
```

Build phải thành công, không có lỗi compilation hoặc test failure liên quan đến thay đổi.

**Ưu tiên 2 — Java compile smoke test:**

Nếu Maven không khả dụng, sử dụng javac trực tiếp với Tomcat jars:

```powershell
javac -encoding UTF-8 -cp "C:/Tomcat 10.1_Tomcat/lib/servlet-api.jar;C:/Tomcat 10.1_Tomcat/lib/jsp-api.jar;C:/Tomcat 10.1_Tomcat/lib/el-api.jar" -d build/check-classes <all java files>
```

**Ưu tiên 3 — Xác minh thủ công:**

Nếu không thể chạy build locally, ghi rõ lý do và residual risk trong response.

### 4.2. Dọn Dẹp Sau Xác Minh

Sau khi smoke test hoàn tất, xóa các thư mục tạm:

```powershell
Remove-Item -Recurse -Force build/check-classes
```

Chỉ giữ lại thư mục tạm nếu cần thiết cho debugging và nêu rõ lý do.

### 4.3. Xác Minh Servlet/JSP/Config

Đối với thay đổi liên quan đến servlet, JSP, hoặc cấu hình:

- Triển khai (deploy) lên Tomcat local nếu có thể.
- Hoặc kiểm tra cấu hình thủ công bằng cách đọc lại các file liên quan.
- Đảm bảo URL mapping trong `web.xml` khớp với servlet class.

---

## 5. Yêu Cầu Tài Liệu Hóa (DOCUMENTATION REQUIREMENTS)

### 5.1. Khi Nào Cần Cập Nhật Tài Liệu

**Cập nhật tài liệu sau mỗi thay đổi ảnh hưởng đến:**

- Kiến trúc hệ thống (cấu trúc thư mục, module boundaries).
- Ranh giới và luồng phụ thuộc.
- Tiêu chuẩn lập trình hoặc workflow.
- Pattern được sử dụng.
- Trạng thái tính năng (đã triển khai, đang phát triển, đã lên kế hoạch).
- Cấu hình deployment hoặc build.

### 5.2. Các File Cần Cập Nhật Thường Xuyên

| Loại thay đổi | File cần cập nhật |
|---|---|
| Tính năng mới | `docs/status/IMPLEMENTED_FEATURES.md`, `docs/planning/ACTIVE_TASKS.md` |
| Thay đổi kiến trúc | `docs/architecture/SYSTEM_ARCHITECTURE.md`, `docs/architecture/FOLDER_STRUCTURE.md` |
| Thay đổi module | `docs/architecture/MODULE_BOUNDARIES.md`, `docs/rules/PROTECTED_MODULES.md` |
| Thay đổi workflow | `docs/6_DEVELOPMENT/AI_WORKFLOW.md`, `docs/rules/AI_WORKFLOW_RULES.md` |
| Thay đổi coding standards | `docs/6_DEVELOPMENT/CODING_STANDARDS.md` |
| Technical debt mới | `docs/status/TECH_DEBT.md` |

### 5.3. Drift Tài Liệu Là Kỹ Thuật Nợ

**Documentation drift — hiện tượng tài liệu không còn phản ánh thực tế — được coi là technical debt.** 

Khi phát hiện tài liệu không chính xác, agent phải:

1. Cập nhật tài liệu ngay nếu nằm trong phạm vi công việc hiện tại.
2. Hoặc ghi nhận và báo cáo trong phần open questions của task.

---

## 6. Tóm Tắt Checklist

### Trước Khi Bắt Đầu

- [ ] Đọc `AGENTS.md`.
- [ ] Đọc các nguồn liên quan (source, SQL, docs, config).
- [ ] Xác định protected modules có thể bị ảnh hưởng.
- [ ] Tạo plan document cho thay đổi non-trivial.
- [ ] Cập nhật `docs/planning/ACTIVE_TASKS.md` hoặc `BACKLOG.md`.

### Trong Quá Trình Thực Hiện

- [ ] Thực hiện thay đổi nhỏ nhất đúng đắn.
- [ ] Tái sử dụng pattern hiện có.
- [ ] Không chỉnh sửa file trong `build/`, `dist/`, `target/`, `.git/`.
- [ ] Lưu file UTF-8 without BOM.

### Sau Khi Thực Hiện

- [ ] Chạy `mvn clean package -DskipTests` (hoặc smoke test).
- [ ] Dọn thư mục tạm sau test.
- [ ] Cập nhật tài liệu bị ảnh hưởng.
- [ ] Xác minh không phá vỡ tính năng hiện có.

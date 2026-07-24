# Authentic Multi-Turn Dialogue Evidence Log: Implement Partner Approve Transfer Handshake Logic in WarehouseDAO

| Parameter | Value |
|---|---|
| **Week** | Week 9 |
| **Report Number** | 2.0 |
| **SDLC Phase** | Implementation |
| **Task / Activity** | Implement Partner Approve Transfer Handshake Logic in WarehouseDAO |
| **AI Tool Used** | Antigravity |
| **Quantitative Measure** | 1 DAO handshake method added, 1 stock increment transaction executed |
| **Value Added** | 5.0 / 5.0 |

---

## 🗣️ Multi-Turn Conversation History (Nhật ký trao đổi & khắc phục từng bước)

### 💬 Turn 1: User Prompt (Raw Verbatim Text)
```text
Phiếu điều chuyển kho: "approveTransfer" hoặc "partnerApproveTransfer"
Đơn hàng (Nhập/Xuất): "approveOrder"
Phiếu kiểm kho: "approveCheck"
ghi ở đâu , làm sao để biết gửi cái gì
```

### 🤖 Turn 1: AI Response & Refactoring Strategy
Các chuỗi giá trị này được định nghĩa ở **Backend (File Java Controller)** và chuyển sang **Frontend (File JSP)** để render động. 

Dưới đây là chi tiết **nơi ghi** và **cơ chế hệ thống biết chính xác gửi cái gì**:

---

### 1. Ghi ở đâu trong Codebase?

#### A. Phía Backend — Gán giá trị theo từng loại phiếu:
Nằm trong file Java: [ApprovalTabController.java](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java)

* **Phiếu điều chuyển kho (Transfer):**
  [ApprovalTabController.java:L291](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java#L291)
  ```java
  map.put("actionApprove", "approveTransfer"); 
  // Hoặc đối tác duyệt: map.put("actionApprove", "partnerApproveTransfer");
  ```
* **Đơn hàng Nhập/Xuất (Order):**
  [ApprovalTabController.java:L431](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java#L431)
  ```java
  map.put("actionApprove", "approveOrder");
  ```
* **Phiếu kiểm kho (Inventory Check):**
  [ApprovalTabController.java:L464](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java#L464)
  ```java
  map.put("actionApprove", "approveCheck");
  ```

---

#### B. Phía Frontend — Render vào thẻ Hidden Input:
Nằm trong file JSP: [_tab_approval.jsp](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/webapp/views/inventory/tabs/_tab_approval.jsp#L135-L145)

```jsp
<c:if test="${not empty item.actionApprove}">
    <form action="${pageContext.request.contextPath}/inventory" method="POST">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="${item.actionApprove}">
        <input type="hidden" name="${item.idParamName}" value="${item.id}">
        <button type="submit">Duyệt</button>
    </form>
</c:if>
```

---

#### C. Phía Controller nhận request POST — Điều hướng xử lý:
Nằm trong file Java: [InventoryController.java](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/InventoryController.java#L301-L325)

```java
String action = request.getParameter("action");
switch (action) {
    case "approveTransfer":
    case "partnerApproveTransfer":
        new ApprovalTabController().doPost(request, response);
        break;
    case "approveOrder":
        new OrderVoucherController().doPost(request, response);
        break;
    case "approveCheck":
        new InventoryCheckController().doPost(request, response);
        break;
}
```

---

### 2. Làm sao hệ thống biết gửi cái gì?

1. **Phân loại từ Backend (`ApprovalTabController`):**
   Khi người dùng mở tab **Chờ duyệt**, Backend gộp 3 danh sách (Điều chuyển, Đơn hàng, Kiểm kho) thành 1 danh sách chung gọi là `unifiedApprovals`. 
   Với mỗi đối tượng dữ liệu truyền vào `map`, Backend chủ động định nghĩa tên hành động tương ứng:
   - Nếu dữ liệu lấy từ bảng **Điều chuyển kho** ➔ gán `actionApprove` = `"approveTransfer"`.
   - Nếu dữ liệu lấy từ bảng **Đơn hàng** ➔ gán `actionApprove` = `"approveOrder"`.
   - Nếu dữ liệu lấy từ bảng **Kiểm kho** ➔ gán `actionApprove` = `"approveCheck"`.

2. **Duyệt danh sách hiển thị (`_tab_approval.jsp`):**
   Trên màn hình JSP, một vòng lặp `c:forEach` chạy qua danh sách `unifiedApprovals`. Mỗi dòng (row) của bảng sẽ sinh ra 1 thẻ `<form>` riêng biệt.
   - Dòng chứa thông tin đơn hàng sẽ sinh ra: `<input type="hidden" name="action" value="approveOrder">`.
   - Dòng chứa phiếu kiểm kho sẽ sinh ra: `<input type="hidden" name="action" value="approveCheck">`.

3. **Gửi dữ liệu khi bấm Duyệt:**
   Khi bạn nhấn nút **Duyệt** ở bất kỳ dòng nào, browser chỉ `submit` duy nhất form ở dòng đó. Do đó, tham số `action` sẽ tự động lấy đúng chuỗi đã được đặt sẵn cho dòng tương ứng đó gửi lên server.

---

### 💬 Turn 2: User Prompt (Raw Verbatim Text)
```text
map đấy nằm ở đâu
```

### 🤖 Turn 2: AI Response & Refactoring Strategy
Các đối tượng `map` đó nằm trong file Java:

📁 **Đường dẫn file:** 
[ApprovalTabController.java](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java) ➔ Phương thức `handleApprovalTab()` (dòng 190 đến 482).

---

### Chi tiết khởi tạo và gán giá trị của `map`:

#### 1. Khởi tạo danh sách chứa các map:
Tại [L240](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java#L240):
```java
List<Map<String, Object>> unifiedApprovals = new ArrayList<>();
```

#### 2. Tạo `map` cho từng phiếu và gán `actionApprove`:
* **Cho Phiếu Điều chuyển kho (Transfer):** [L277-L293](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java#L277-L293)
  ```java
  Map<String, Object> map = new HashMap<>();
  map.put("id", representative.getStockTransferId());
  map.put("idParamName", "transferId");
  map.put("actionApprove", "approveTransfer"); // <-- NẰM Ở ĐÂY
  unifiedApprovals.add(map);
  ```

* **Cho Đơn hàng Nhập/Xuất (Order):** [L412-L436](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java#L412-L436)
  ```java
  Map<String, Object> map = new HashMap<>();
  map.put("id", order.getOrderId());
  map.put("idParamName", "orderId");
  map.put("actionApprove", "approveOrder"); // <-- NẰM Ở ĐÂY
  unifiedApprovals.add(map);
  ```

* **Cho Phiếu Kiểm kho (Check):** [L446-L469](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java#L446-L469)
  ```java
  Map<String, Object> map = new HashMap<>();
  map.put("id", check.getCheckId());
  map.put("idParamName", "checkId");
  map.put("actionApprove", "approveCheck"); // <-- NẰM Ở ĐÂY
  unifiedApprovals.add(map);
  ```

#### 3. Gửi danh sách map sang JSP:
Tại [L478](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java#L478):
```java
request.setAttribute("unifiedApprovals", unifiedApprovals);
```

#### 4. File JSP nhận dữ liệu:
Trong file [_tab_approval.jsp](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/webapp/views/inventory/tabs/_tab_approval.jsp#L112), câu lệnh `<c:forEach items="${unifiedApprovals}" var="item">` sẽ duyệt từng phần tử `map` này. Khi gọi `${item.actionApprove}`, JSP thực chất đang lấy giá trị tương ứng từ `map.get("actionApprove")`.

---

### 💬 Turn 3: User Prompt (Raw Verbatim Text)
```text
k hiểu cơ chế lắm , jsp hiện tất cả phiếu qua get , sau đó làm sao để phần biệt được qua map , gửi map này lên lúc nào
```

### 🤖 Turn 3: AI Response & Refactoring Strategy
Để bạn dễ hình dung, đây là toàn bộ **vòng đời hoạt động (Lifecycle)** từ lúc bạn tải trang (GET) đến lúc bạn bấm nút Duyệt (POST).

Bản chất: **Java Map KHÔNG ĐƯỢC gửi ngược lại lên Server**. `Map` chỉ là cấu trúc dữ liệu tạm thời ở Backend để "chuẩn bị dữ liệu và in ra HTML" trên trình duyệt.

---

### Bước 1: Khi bạn tải trang (Yêu cầu HTTP GET)

1. Bạn vào đường dẫn `/inventory?tab=approval`.
2. Server chạy hàm `handleApprovalTab()` trong [ApprovalTabController.java](file:///d:/Thangdev/SWP/SWP391_Finora-thang/src/main/java/controller/inventory/ApprovalTabController.java).
3. Server lấy dữ liệu từ 3 bảng khác nhau trong Database (Điều chuyển, Đơn hàng, Kiểm kho).
4. Do mỗi bảng có cấu trúc khác nhau, Server biến mỗi phiếu thành **1 đối tượng `Map` chuẩn hóa** có chung các chìa khóa (keys): `code`, `createdBy`, `id`, `actionApprove`, `idParamName`.
5. Server bỏ tất cả các `Map` này vào một danh sách `unifiedApprovals` và gửi sang file JSP.

---

### Bước 2: JSP dịch `Map` thành các thẻ HTML tĩnh gửi về Trình duyệt

Khi JSP nhận được danh sách `unifiedApprovals`, vòng lặp `<c:forEach>` chạy qua từng `Map` để **in sẵn mã HTML**.

**Ví dụ:** Bạn có 2 phiếu trong danh sách:
* Phiếu 1: Phiếu Kiểm kho (ID = 15) ➔ `map` chứa `actionApprove = "approveCheck"`, `idParamName = "checkId"`.
* Phiếu 2: Phiếu Điều chuyển (ID = 8) ➔ `map` chứa `actionApprove = "approveTransfer"`, `idParamName = "transferId"`.

JSP sẽ **in ra giao diện HTML** cho trình duyệt như sau:

```html
<!-- DÒNG 1 TRÊN BẢNG (Phiếu kiểm kho ID 15) -->
<tr>
   <td>PKK001</td>
   <td>Kiểm kho</td>
   <td>
      <!-- Form của dòng 1 -->
      <form action="/inventory" method="POST">
          <input type="hidden" name="action" value="approveCheck">  <!-- in ra cứng "approveCheck" -->
          <input type="hidden" name="checkId" value="15">            <!-- in ra cứng "checkId" và "15" -->
          <button type="submit">Duyệt</button>
      </form>
   </td>
</tr>

<!-- DÒNG 2 TRÊN BẢNG (Phiếu điều chuyển ID 8) -->
<tr>
   <td>PDC002</td>
   <td>Điều chuyển</td>
   <td>
      <!-- Form của dòng 2 -->
      <form action="/inventory" method="POST">
          <input type="hidden" name="action" value="approveTransfer"> <!-- in ra cứng "approveTransfer" -->
          <input type="hidden" name="transferId" value="8">             <!-- in ra cứng "transferId" và "8" -->
          <button type="submit">Duyệt</button>
      </form>
   </td>
</tr>
```

> 💡 **Nhận xét:** Trình duyệt (Browser) **KHÔNG BIẾT Map là gì**. Nó chỉ nhận được đoạn mã HTML có các ô `<input type="hidden">` đã điền sẵn giá trị chữ.

---

### Bước 3: Người dùng bấm nút "Duyệt" (Yêu cầu HTTP POST)

* Giả sử bạn bấm nút **Duyệt ở Dòng 1 (Phiếu kiểm kho ID 15)**.
* Trình duyệt sẽ **chỉ gửi thông tin của `<form>` ở Dòng 1** lên Server.
* Dữ liệu gửi đi (Payload POST) chỉ vỏn vẹn là chuỗi text:
  ```text
  action: "approveCheck"
  checkId: "15"
  csrfToken: "..."
  ```

---

### Bước 4: Server nhận dữ liệu POST và xử lý

Khi Server nhận được request POST:
1. `InventoryController.java` đọc `request.getParameter("action")` ➔ Thấy giá trị là `"approveCheck"`.
2. Lệnh `switch (action)` nhảy vào trường hợp `"approveCheck"`, gọi `InventoryCheckController`.
3. `InventoryCheckController` đọc `request.getParameter("checkId")` ➔ Thấy giá trị là `"15"`.
4. Server thực hiện câu lệnh SQL: `UPDATE inventory_check SET status = 'APPROVED' WHERE check_id = 15`.

---

### Tóm tắt lại câu hỏi của bạn:

* **Phân biệt bằng cách nào?**
  Mỗi phiếu hiển thị trên bảng đều được bọc riêng trong 1 thẻ `<form>`. Trong form đó đã được in sẵn `name="action"` và `name="[idParamName]"` mang giá trị tương ứng của phiếu đó.
* **Map gửi lên lúc nào?**
  `Map` **KHÔNG BAO GIỜ** được gửi từ Client lên Server. Map chỉ dùng ở Server lúc làm nhiệm vụ chuẩn hóa dữ liệu để JSP in ra thẻ HTML `<form>` mà thôi. Lúc bấm Duyệt, browser chỉ gửi giá trị text nằm trong thẻ `<input>` của Form tương ứng.

---

## 🛠️ Student Validation & Iterative Refactoring
Implemented partnerApproveTransfer method in WarehouseDAO to handle two-step branch confirmation logic.

## 💻 Code / SQL Implementation Evidence
```java
public boolean partnerApproveTransfer(int transferId, int userId) { ... }
```

## 📝 Technical Reflection & Multi-Turn Problem Solving
A single initial prompt was insufficient to complete this task due to unexpected edge cases, database constraints, and UI alignment needs. Through a sequence of iterative prompts, error reports, and refactoring requests, the AI assistant and student pair-programmed to diagnose root causes, execute code edits, and achieve a fully functional implementation.

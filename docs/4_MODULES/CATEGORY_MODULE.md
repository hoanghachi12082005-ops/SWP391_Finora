# Module Quan ly Nhom Hang — FinoraRetail

## 1. Tong quan Module

Module Category (Nhom hang) la module dau tien trong he thong FinoraRetail duoc trien khai day du. Module cung cap chuc nang quan ly phan loai san pham voi cau truc cay phan cap (hierarchical tree), cho phep to chuc san pham theo danh muc cha-con mot cach co he thong.

**Trang thai:** `Implemented` (Da tich hop vao he thong)

**Package:** `com.storemanagement.controller.product`, `com.storemanagement.dao.product`, `com.storemanagement.model`

**Route:** `/admin/categories`

---

## 2. Kien truc Module

### 2.1 Cac thanh phan chinh

| Thanh phan | Loai | Vi tri |
|---|---|---|
| `CategoryServlet` | Servlet | `src/main/java/com/storemanagement/controller/product/CategoryServlet.java` |
| `CategoryDAO` | DAO | `src/main/java/com/storemanagement/dao/product/CategoryDAO.java` |
| `Category` | Model | `src/main/java/com/storemanagement/model/Category.java` |
| `categories.css` | Stylesheet | `src/main/webapp/assets/css/categories.css` |
| `list.jsp` | View | `src/main/webapp/views/categories/list.jsp` |

### 2.2 So do kien truc MVC

```
Browser -> AuthFilter -> CategoryServlet -> CategoryDAO -> SQL Server DBFinoraV2
                            |
                            v
                       Category.java (Model)
                            |
                            v
                  list.jsp (View) -> Browser
```

### 2.3 Cau truc cay phan cap

Module ho tro cau truc danh muc nhieu cap:

```
Danh muc goc (Root)
├── Thuc pham
│   ├── Thuc pham tuoi
│   └── Thuc pham chế bien
├── Do uong
│   ├── Nuoc ngot
│   └── Trai cay
└── Hang gia dung
```

---

## 3. Model — Category.java

### 3.1 Cau truc lop

```java
public class Category {
    private int categoryId;        // ID danh muc
    private String name;           // Ten danh muc (map tu CategoryName)
    private String description;     // Mo ta danh muc
    private Integer parentId;      // ID danh muc cha (map tu ParentCategoryID)
    private String parentName;     // Ten danh muc cha
    private String status;          // active / inactive
    private int productCount;      // So san pham lien ket
}
```

### 3.2 Cac truong du lieu

| Truong | Kieu | Ràng buoc | Mo ta |
|---|---|---|---|
| `categoryId` | int | PK, Auto-increment | Khoa chinh |
| `name` | String | map tu `CategoryName` | Ten danh muc |
| `description` | String | map tu `Description` | Mo ta |
| `parentId` | Integer | map tu `ParentCategoryID`, FK tu tham chieu | ID danh muc cha |
| `parentName` | String | computed JOIN | Ten danh muc cha |
| `status` | String | `active` / `inactive` | Trang thai hoat dong |
| `productCount` | int | computed COUNT | So san pham lien ket |

---

## 4. DAO — CategoryDAO.java

### 4.1 Cac phuong thuc da trien khai

| Phuong thuc | Mo ta | Tra ve |
|---|---|---|
| `getCategories(keyword, status, parentName, page, limit)` | Tim kiem phan trang voi bo loc | `List<Category>` |
| `getAllCategories()` | Lay tat ca danh muc | `List<Category>` |
| `getActiveCategories()` | Lay danh muc dang hoat dong | `List<Category>` |
| `countCategories()` | Dem tong so danh muc | `int` |
| `countRootCategories()` | Dem danh muc goc (khong co cha) | `int` |
| `countLinkedProducts()` | Dem san pham da lien ket | `int` |
| `getCategoryById(id)` | Lay danh muc theo ID | `Category` |
| `addCategory(category)` | Them danh muc moi | `boolean` |
| `updateCategory(category)` | Cap nhat danh muc | `boolean` |
| `existsById(id)` | Kiem tra ton tai theo ID | `boolean` |
| `isCategoryNameExists(name, excludeId)` | Kiem tra ten trung lap | `boolean` |
| `getCategoryIdByName(name)` | Lay ID theo ten | `Integer` |
| `isDescendant(categoryId, candidateParentId)` | Kiem tra chu trinh cha-con | `boolean` |

### 4.2 SQL — getCategories (tim kiem phan trang)

Su dung `OFFSET ... FETCH NEXT` cho SQL Server phan trang:

```sql
SELECT c.CategoryID, c.CategoryName, c.Description,
       c.ParentCategoryID, c.Status, p.CategoryName AS ParentName,
       COUNT(pr.ProductID) AS ProductCount
FROM Category c
LEFT JOIN Category p ON c.ParentCategoryID = p.CategoryID
LEFT JOIN Product pr ON c.CategoryID = pr.CategoryID
WHERE 1 = 1
  AND (c.CategoryName LIKE ? OR c.Description LIKE ?)
  AND c.Status = ?
  AND p.CategoryName = ?
GROUP BY c.CategoryID, c.CategoryName, c.Description,
         c.ParentCategoryID, c.Status, p.CategoryName
ORDER BY c.CategoryID ASC
OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
```

### 4.3 SQL — isDescendant (CTE — ngan vong tron)

Su dung Common Table Expression (CTE) de duyet cay danh muc:

```sql
WITH CategoryTree AS (
    SELECT CategoryID, ParentCategoryID
    FROM Category WHERE ParentCategoryID = ?
    UNION ALL
    SELECT c.CategoryID, c.ParentCategoryID
    FROM Category c
    INNER JOIN CategoryTree ct ON c.ParentCategoryID = ct.CategoryID
)
SELECT CategoryID FROM CategoryTree WHERE CategoryID = ?
```

**Y nghia:** Kiem tra xem `candidateParentId` co phai la con/chau cua `categoryId` hay khong. Neu co, khong cho phep dat cha-con do vi se tao vong tron.

---

## 5. Database Schema

### 5.1 Bang category

```sql
CREATE TABLE category (
    category_id        INT IDENTITY(1,1) PRIMARY KEY,
    category_name      NVARCHAR(150) NOT NULL,
    description        NVARCHAR(255),
    parent_category_id INT REFERENCES category(category_id),
    status             NVARCHAR(20) DEFAULT 'active',
    created_at         DATETIME DEFAULT GETDATE(),
    update_at          DATETIME DEFAULT GETDATE()
);
```

### 5.2 Rang buoc du lieu

| Rang buoc | Loai | Mo ta |
|---|---|---|
| `PK_category` | PRIMARY KEY | `category_id` |
| `FK_category_parent` | FOREIGN KEY | `parent_category_id` tham chieu chinh no |
| Status values | CHECK | `active` / `inactive` |

### 5.3 Quan he voi bang product

Bang `product` co `category_id` tham chieu den `category(category_id)`. Moi san pham thuoc mot danh muc.

---

## 6. Route va Controller

### 6.1 Route Mapping

Servlet annotation: `@WebServlet(name = "CategoryServlet", urlPatterns = {"/admin/categories"})`

| Route | Method | Hanh dong |
|---|---|---|
| `/admin/categories` | GET | Hien thi danh sach (mac dinh action=list) |
| `/admin/categories?action=list` | GET | Hien thi danh sach |
| POST (action=add) | POST | Them danh muc moi |
| POST (action=update) | POST | Cap nhat danh muc |

### 6.2 Request Parameters

| Tham so | Mo ta |
|---|---|
| `keyword` | Tu khoa tim kiem (ten, mo ta) |
| `status` | Loc theo trang thai (`active` / `inactive`) |
| `parentName` | Loc theo ten danh muc cha |
| `page` | So trang (bat dau tu 1) |
| `limit` | So ban ghi moi trang (mac dinh 20) |

---

## 7. Tinh nang da trien khai

### 7.1 Tim kiem va loc

| Tinh nang | Mo ta | Tham so |
|---|---|---|
| Tim kiem theo tu khoa | Tim trong ten va mo ta | `keyword` |
| Loc theo trang thai | Hoat dong hoac ngung su dung | `status` |
| Loc theo danh muc cha | Chi hien thi con cua mot danh muc | `parentName` |
| Loc danh muc goc | Chi hien thi danh muc khong co cha | `parentName=go'c` |
| Phan trang | Gioi han so ban ghi moi trang | `page`, `limit` |

### 7.2 Thao tac CRUD

| Thao tac | Trang thai | Mo ta |
|---|---|---|
| Xem danh sach | Da hoan thanh | Phan trang, tim kiem, loc |
| Them danh muc | Da hoan thanh | Kiem tra ten trung, ngan vong tron |
| Sua danh muc | Da hoan thanh | Giu nguyen parent neu khong doi |
| Doi trang thai | Da hoan thanh | active / inactive |

### 7.3 Kiem tra nghiep vu

| Quy tac | Mo ta | Xu ly |
|---|---|---|
| Khong trung ten | Ten danh muc khong duoc trung cung cap | `isCategoryNameExists()` |
| Khong tu lam cha | Danh muc khong the la cha cua chinh no | Kiem tra trong `updateCategory()` |
| Ngan vong tron | Danh muc A khong the la con cua B neu B la con cua A | `isDescendant()` voi CTE |
| Khong xoa cha co san pham | Khong cho phep xoa danh muc da gan san pham | Kiem tra `productCount` |

### 7.4 Thong ke hien thi

| Chi so | Mo ta |
|---|---|
| Tong so danh muc | `countCategories()` |
| So danh muc goc | `countRootCategories()` |
| So san pham da lien ket | `countLinkedProducts()` |

---

## 8. View — list.jsp

Giao dien chinh cua module bao gom:

- **Hero Header:** Tieu de trang, nut them moi
- **Stats Cards:** 3 the thong ke (tong danh muc, danh muc goc, san pham)
- **Filter Bar:** Tim kiem theo tu khoa, loc theo trang thai va danh muc cha
- **Data Table:** Bang danh sach voi cac cot: ma, ten, danh muc cha, mo ta, so san pham, trang thai, thao tac
- **Modals:** Hop thoai them moi va chinh sua (Bootstrap modal)
- **Pagination:** Dieu khien phan trang

### 8.1 CSS

Toan bo CSS duoc tach ra file rieng: `assets/css/categories.css`

### 8.2 Flash Messages

Su dung session de hien thi thong bao sau khi thuc hien thao tac:

- POST -> Servlet -> DAO -> set session message -> redirect -> GET -> hien thi -> remove session

---

## 9. Protected Area

Module Category **khong** thuoc Protected Area. Tuy nhien, cac thanh phan sau can duoc bao ve:

- **Bang category** trong schema SQL — khong xoa hoac thay doi cau truc khong qua migration
- **DBContext** — khong sua tru khi can thay doi cau hinh ket noi

---

## 10. Mo rong trong tuong lai

| Tinh nang | Uu tien | Mo ta |
|---|---|---|
| Xoa mem (Soft delete) | Cao | Khong xoa vinh vien, chi danh dau DELETED |
| Slug/URL friendly | Trung binh | Tao URL slug cho SEO |
| Hinh anh danh muc | Thap | Upload icon/anh cho danh muc |
| Import danh muc | Thap | Import tu JSON/Excel |

---

*Document version: 2.0*
*Last updated: 2026-06-21*
*Project: SWP391_Finora (FinoraRetail)*
*Status: Implemented and Integrated*

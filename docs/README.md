# Tai lieu Du an FinoraRetail

> **Ten du an:** SWP391_Finora / FinoraRetail
> **Muc dich:** Nguon tai lieu tham khao chinh thuc va duy nhat cho toan bo du an
> **Ngon ngu:** Tieng Viet (Viet Nam Academic Writing)
> **Phien ban:** 1.0

---

## 1. Gioi thieu

Tai lieu nay duoc xay dung nham phuc vu cac thanh vien phat trien, quan ly du an, va cac ben lien quan trong suot vong doi cua he thong FinoraRetail. Moi quyet dinh kien truc, thay doi ma nguon, va cau hinh trien khai deu phai duoc phan anh trong tai lieu tuong ung tai day.

**Nguyen tac bao tri:** Khi co thay doi ve hanh vi ma nguon, cau truc goi (package layout), quy trinh build, hoac cau hinh trien khai, tai lieu lien quan phai duoc cap nhat dong thoi trong cung mot thay doi (change). Khong duoc de tai lieu loi thoi so voi ma nguon.

---

## 2. Cau truc tai lieu (8 phan)

```
docs/
├── README.md                       — Chi muc nay
│
├── 1_OVERVIEW/                    — Tong quan he thong va cong nghe
│   ├── SYSTEM_OVERVIEW.md         — Gioi thieu tong quan, mo hinh kinh doanh, nguoi dung
│   └── TECHNOLOGY_STACK.md         — Cac thanh phan cong nghe, phien ban, vai tro
│
├── 2_ARCHITECTURE/               — Kien truc he thong chi tiet
│   ├── ARCHITECTURE_OVERVIEW.md  — So do kien truc tong the, MVC flow
│   ├── FOLDER_STRUCTURE.md       — Giai thich cau truc thu muc ma nguon
│   └── DEPENDENCY_RULES.md       — Luong phu thuoc giua cac lop
│
├── 3_DATABASE/                   — Co so du lieu
│   ├── SCHEMA_OVERVIEW.md        — Tong quan 21 bang
│   ├── TABLE_DETAILS.md          — Chi tiet tung bang
│   ├── RELATIONSHIPS.md          — Quan he giua cac bang
│   └── DATA_FLOW.md              — Luong du lieu trong he thong
│
├── 4_MODULES/                    — Tai lieu tung module chuc nang
│   ├── MODULE_INDEX.md           — Chi muc cac module
│   ├── AUTH_MODULE.md            — Module xac thuc
│   ├── DASHBOARD_MODULE.md        — Module dashboard
│   ├── CATEGORY_MODULE.md        — Module quan ly nhom hang
│   ├── PRODUCT_MODULE.md          — Module quan ly san pham
│   ├── CUSTOMER_MODULE.md        — Module quan ly khach hang
│   ├── SUPPLIER_MODULE.md        — Module quan ly nha cung cap
│   ├── STORE_MODULE.md           — Module quan ly cua hang
│   ├── ORDER_MODULE.md           — Module quan ly don hang
│   ├── INVENTORY_MODULE.md       — Module quan ly ton kho
│   ├── PAYMENT_MODULE.md         — Module thanh toan
│   ├── FINANCE_MODULE.md         — Module tai chinh
│   └── REPORT_MODULE.md          — Module bao cao
│
├── 5_IMPLEMENTATION/             — Tien do hien tai
│   ├── CURRENT_STATUS.md         — Trang thai hien tai tong quan
│   ├── IMPLEMENTED_FEATURES.md   — Danh sach tinh nang da trien khai
│   └── TECH_DEBT.md              — Ky thuat no (technical debt)
│
├── 6_DEVELOPMENT/                — Quy tac phat trien
│   ├── CODING_STANDARDS.md       — Tieu chuan viet ma
│   ├── NAMING_CONVENTIONS.md     — Quy uoc dat ten
│   ├── AI_WORKFLOW.md            — Quy tac lam viec cho AI agent
│   └── REFACTOR_POLICY.md        — Chinh sach tai cau truc
│
├── 7_API/                        — Tieu chuan API (tuong lai)
│   └── API_CONVENTIONS.md        — Quy uoc cho cac endpoint JSON
│
└── 8_REFERENCES/
    └── EXTERNAL_LINKS.md        — Lien ket tham khao, dependency list
```

---

## 3. Thong tin he thong

| Thuoc tinh | Gia tri |
|---|---|
| **Ten du an** | FinoraRetail (SWP391_Finora) |
| **Loai ung dung** | Maven Java WAR Web Application |
| **Java version** | JDK 17 |
| **Servlet API** | Jakarta Servlet 6.0 |
| **Server** | Apache Tomcat 10.1 |
| **Co so du lieu** | SQL Server — Database: `DBFinoraV2` |
| **JDBC Driver** | mssql-jdbc 12.6.1.jre11 |
| **Build tool** | Maven 3.x |
| **WAR output** | `target/StoreManagementNetBeans.war` |
| **Context path** | `/FinoraRetail` |
| **Package root** | `com.storemanagement` |

---

## 4. Thong ke du an

| Thanh phan | So luong |
|---|---|
| JSP Views (`views/`) | 65+ |
| Model classes | 19 |
| DAO classes | 17 (skeleton) |
| Service classes | 19 (skeleton) |
| Controller classes | 18 (AuthController hoat dong) |
| Bang CSDL | 21 |
| Route duoc bao ve (AuthFilter) | 21 patterns |
| Bootstrap CSS Framework | Phien ban 5 |
| Material Icons | Google Fonts |

---

## 5. Quy tac cap nhat tai lieu

### 5.1. Khi nao can cap nhat

Tai lieu phai duoc cap nhat khi co thay doi thuoc mot trong cac danh muc sau:

1. **Thay doi kien truc** — Them module moi, gop module, thay doi luong phu thuoc
2. **Thay doi cau truc goi** — Di chuyen class giua cac package
3. **Thay doi quy tac lap trinh** — Bo sung hoac sua doi coding standards
4. **Thay doi build/deploy** — Thay doi pom.xml, cau hinh Tomcat, context path
5. **Trien khai tinh nang moi** — Cap nhat `5_IMPLEMENTATION/` va `4_MODULES/`
6. **Phat sinh ky thuat no** — Ghi nhan vao `5_IMPLEMENTATION/TECH_DEBT.md`

### 5.2. Ai co trah nhiem

- **Lap trinh vien thuc hien thay doi:** Cap nhat tai lieu lien quan trong cung commit
- **Tech lead / Kien truc su:** Phe duyet cac thay doi kien truc
- **Project manager:** Cap nhat `5_IMPLEMENTATION/` khi co thay doi ve ke hoach

---

## 6. Lien ket nhanh

| Tai lieu | Mo ta |
|---|---|
| `1_OVERVIEW/SYSTEM_OVERVIEW.md` | Tong quan he thong FinoraRetail |
| `1_OVERVIEW/TECHNOLOGY_STACK.md` | Danh sach cong nghe su dung |
| `2_ARCHITECTURE/ARCHITECTURE_OVERVIEW.md` | Kien truc MVC, luong request |
| `3_DATABASE/SCHEMA_OVERVIEW.md` | Tong quan 21 bang CSDL |
| `4_MODULES/CATEGORY_MODULE.md` | Chi tiet module quan ly nhom hang |
| `5_IMPLEMENTATION/CURRENT_STATUS.md` | Trang thai hien tai cua du an |
| `6_DEVELOPMENT/CODING_STANDARDS.md` | Tieu chuan viet ma nguon |

---

## 7. Nguoi lien he va dong gop

Moi thac mac hoac de xuat cai tien tai lieu, vui long tao issue hoac pull request trong repository cua du an.

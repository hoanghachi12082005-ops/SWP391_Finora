import util.database.DBContext;
import java.sql.Connection;
import java.sql.Statement;

public class DataSeeder {
    public static void main(String[] args) {
        try (Connection conn = DBContext.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Bắt đầu insert dữ liệu...");

            String sql1 = "INSERT INTO Branch (Name, Address, Phone, Status, CreatedAt) VALUES " +
                          "(N'Chi nhánh Miền Bắc 1', N'Từ Liêm, Hà Nội', '0901234567', 'active', GETDATE()), " +
                          "(N'Chi nhánh Miền Bắc 2', N'Long Biên, Hà Nội', '0901234568', 'active', GETDATE()), " +
                          "(N'Chi nhánh Miền Nam 1', N'Quận 1, HCM', '0901234569', 'active', GETDATE()), " +
                          "(N'Chi nhánh Miền Nam 2', N'Quận 7, HCM', '0901234570', 'active', GETDATE()), " +
                          "(N'Chi nhánh Miền Trung', N'Hải Châu, Đà Nẵng', '0901234571', 'active', GETDATE()); " +
                          "INSERT INTO warehouse (warehouse_name, branch_id, address, status, created_at) " +
                          "SELECT N'Kho ' + Name, BranchID, Address, 'ACTIVE', GETDATE() " +
                          "FROM Branch WHERE Name LIKE N'Chi nhánh Miền%';";
            stmt.executeUpdate(sql1);
            System.out.println("Đã insert 5 kho mới.");

            String sql2 = "DECLARE @CatID INT = 1; DECLARE @UnitID INT = 1; " +
                          "INSERT INTO Product (Name, CategoryID, UnitID, SellingPrice, Quantity, Status, CreatedAt) VALUES " +
                          "(N'Áo Khoác Nam', @CatID, @UnitID, 500000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Quần Kaki Nam', @CatID, @UnitID, 300000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Váy Chữ A', @CatID, @UnitID, 250000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Đầm Dự Tiệc', @CatID, @UnitID, 800000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Áo Len Cổ Lọ', @CatID, @UnitID, 400000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Quần Tây Nữ', @CatID, @UnitID, 350000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Sơ Mi Trắng Nam', @CatID, @UnitID, 200000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Sơ Mi Lụa Nữ', @CatID, @UnitID, 320000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Giày Thể Thao', @CatID, @UnitID, 900000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Giày Cao Gót', @CatID, @UnitID, 750000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Thắt Lưng Da', @CatID, @UnitID, 150000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Ví Nam Cầm Tay', @CatID, @UnitID, 450000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Mũ Lưỡi Trai', @CatID, @UnitID, 120000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Kính Mát Thời Trang', @CatID, @UnitID, 250000, 100, 'ACTIVE', GETDATE()), " +
                          "(N'Đồng Hồ Nữ', @CatID, @UnitID, 1200000, 100, 'ACTIVE', GETDATE());";
            stmt.executeUpdate(sql2);
            System.out.println("Đã insert 15 sản phẩm mới.");

            String sql3 = "INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status) " +
                          "SELECT w.warehouse_id, p.ProductID, ROUND(RAND(CHECKSUM(NEWID())) * 100, 0), 'ACTIVE' " +
                          "FROM warehouse w CROSS JOIN Product p " +
                          "WHERE NOT EXISTS (" +
                          "    SELECT 1 FROM inventory i WHERE i.warehouse_id = w.warehouse_id AND i.product_id = p.ProductID" +
                          ");";
            int rows = stmt.executeUpdate(sql3);
            System.out.println("Đã cập nhật bảng Inventory: " + rows + " bản ghi.");
            
            System.out.println("Hoàn tất!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

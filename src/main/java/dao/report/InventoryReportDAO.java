package dao.report;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.InventoryReportItem;
import model.InventoryReportOverview;
import util.database.DBContext;

public class InventoryReportDAO {

    private static final String INVENTORY_SELECT =
            "SELECT " +
            "    p.product_id, " +
            "    p.product_name, " +
            "    p.ImageUrl, " +
            "    w.warehouse_name, " +
            "    b.branch_name AS BranchName, " +
            "    i.quantity_in_stock, " +
            "    p.selling_price, " +
            "    (i.quantity_in_stock * p.selling_price) AS TotalValue, " +
            "    i.status ";

    private static final String INVENTORY_FROM =
            "FROM inventory i " +
            "JOIN [product] p ON i.product_id = p.product_id " +
            "JOIN warehouse w ON i.warehouse_id = w.warehouse_id " +
            "JOIN branch b ON w.branch_id = b.branch_id " +
            "WHERE (? IS NULL OR p.product_name LIKE ? OR p.product_codebar LIKE ?) " +
            "AND (? IS NULL OR b.branch_id = ?)";

    public List<InventoryReportItem> getInventoryReport(String keyword,
                                                         String branchFilter,
                                                         int page,
                                                         int pageSize) {
        List<InventoryReportItem> list = new ArrayList<>();
        String sql = INVENTORY_SELECT + INVENTORY_FROM +
                " ORDER BY i.quantity_in_stock ASC, p.product_name ASC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearchAndBranch(ps, 1, keyword, branchFilter);

            int offset = (page - 1) * pageSize;
            ps.setInt(6, offset);
            ps.setInt(7, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countInventoryReport(String keyword, String branchFilter) {
        String sql = "SELECT COUNT(*) AS Total " + INVENTORY_FROM;

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearchAndBranch(ps, 1, keyword, branchFilter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public InventoryReportOverview getReportOverview(String keyword, String branchFilter) {
        InventoryReportOverview overview = new InventoryReportOverview();
        
        String sql = "SELECT " +
                "    COUNT(DISTINCT i.product_id) AS TotalProducts, " +
                "    SUM(i.quantity_in_stock) AS TotalQuantity, " +
                "    SUM(i.quantity_in_stock * p.selling_price) AS TotalValue, " +
                "    SUM(CASE WHEN i.quantity_in_stock <= 10 AND i.quantity_in_stock > 0 THEN 1 ELSE 0 END) AS LowStockCount, " +
                "    SUM(CASE WHEN i.quantity_in_stock = 0 THEN 1 ELSE 0 END) AS OutOfStockCount " +
                INVENTORY_FROM;

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearchAndBranch(ps, 1, keyword, branchFilter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalProducts(rs.getInt("TotalProducts"));
                    overview.setTotalQuantity(rs.getInt("TotalQuantity"));
                    BigDecimal val = rs.getBigDecimal("TotalValue");
                    overview.setTotalValue(val == null ? BigDecimal.ZERO : val);
                    overview.setLowStockCount(rs.getInt("LowStockCount"));
                    overview.setOutOfStockCount(rs.getInt("OutOfStockCount"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return overview;
    }

    private void bindSearchAndBranch(PreparedStatement ps, int startIndex, String keyword, String branchFilter)
            throws SQLException {
        if (keyword == null || keyword.isEmpty()) {
            ps.setNull(startIndex, Types.VARCHAR);
            ps.setNull(startIndex + 1, Types.VARCHAR);
            ps.setNull(startIndex + 2, Types.VARCHAR);
        } else {
            String match = "%" + keyword + "%";
            ps.setString(startIndex, match);
            ps.setString(startIndex + 1, match);
            ps.setString(startIndex + 2, match);
        }

        if (branchFilter == null || branchFilter.isEmpty() || "-1".equals(branchFilter)) {
            ps.setNull(startIndex + 3, Types.INTEGER);
            ps.setNull(startIndex + 4, Types.INTEGER);
        } else {
            int bid = Integer.parseInt(branchFilter);
            ps.setInt(startIndex + 3, bid);
            ps.setInt(startIndex + 4, bid);
        }
    }

    private InventoryReportItem mapItem(ResultSet rs) throws SQLException {
        InventoryReportItem item = new InventoryReportItem();
        item.setProductId(rs.getInt("product_id"));
        item.setProductName(rs.getString("product_name"));
        String rawJson = rs.getString("ImageUrl");
        java.util.List<String> urls = model.Product.parseJsonArray(rawJson);
        if (!urls.isEmpty()) {
            item.setImageUrl(urls.get(0));
        } else {
            item.setImageUrl(null);
        }
        item.setWarehouseName(rs.getString("warehouse_name"));
        item.setBranchName(rs.getString("BranchName"));
        item.setQuantityInStock(rs.getInt("quantity_in_stock"));
        item.setSellingPrice(rs.getBigDecimal("selling_price"));
        item.setTotalValue(rs.getBigDecimal("TotalValue"));
        item.setStatus(rs.getString("status"));
        return item;
    }
}

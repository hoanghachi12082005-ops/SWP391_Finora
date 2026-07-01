package dao.customer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Customer;
import model.CustomerOverview;
import model.Branch;
import model.LoyaltyPointSetting;
import util.database.DBContext;

public class CustomerDAO {

    private static final Logger LOGGER = Logger.getLogger(CustomerDAO.class.getName());
    private static final String CURRENT_POINTS_COLUMN = "current_points";
    private static final String LIFETIME_POINTS_COLUMN = "lifetime_points";

    // =====================================================
    // COMMON SELECT PART
    // =====================================================

    private static final String CUSTOMER_SELECT =
            "SELECT c.cus_id, c.full_name, c.gender, c.bod, c.address, " +
            "c.email, c.phone, c.total_spent, c.created_at, c.updated_at, " +
            "c.status, cp.current_points, cp.lifetime_points " +
            "FROM customer c " +
            "LEFT JOIN customer_point cp ON cp.cus_id = c.cus_id ";

    // =====================================================
    // LIST WITH PAGINATION, SEARCH & FILTER BY BRANCH
    // =====================================================

    public List<Customer> getCustomers(String keyword,
                                       String branchIdFilter,
                                       int page,
                                       int pageSize) {
        List<Customer> list = new ArrayList<>();

        String sql =
                CUSTOMER_SELECT +
                "WHERE c.status = 'ACTIVE' " +
                "AND ( " +
                "    ? IS NULL " +
                "    OR c.phone LIKE ? " +
                "    OR c.full_name LIKE ? " +
                "    OR c.email LIKE ? " +
                ") " +
                "AND (? IS NULL OR EXISTS (SELECT 1 FROM [order] o WHERE o.customer_id = c.cus_id AND o.branch_id = ?)) " +
                "ORDER BY " +
                "  CASE " +
                "    WHEN c.phone = ? THEN 1 " +
                "    WHEN c.phone LIKE ? THEN 2 " +
                "    WHEN c.full_name LIKE ? THEN 3 " +
                "    WHEN c.email LIKE ? THEN 4 " +
                "    ELSE 5 " +
                "  END, c.created_at DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            String searchValue = normalizeLike(keyword);
            String rawSearchValue = normalize(keyword);
            Integer branchId = parseInteger(branchIdFilter);

            int offset = (page - 1) * pageSize;

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);

            setNullableInt(ps, 5, branchId);
            setNullableInt(ps, 6, branchId);

            // Order by parameters
            ps.setString(7, rawSearchValue);
            ps.setString(8, searchValue);
            ps.setString(9, searchValue);
            ps.setString(10, searchValue);

            ps.setInt(11, offset);
            ps.setInt(12, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi getCustomers", e);
        }

        return list;
    }

    public int countCustomers(String keyword,
                              String branchIdFilter) {
        String sql =
                "SELECT COUNT(*) AS Total " +
                "FROM customer c " +
                "WHERE c.status = 'ACTIVE' " +
                "AND ( " +
                "    ? IS NULL " +
                "    OR c.phone LIKE ? " +
                "    OR c.full_name LIKE ? " +
                "    OR c.email LIKE ? " +
                ") " +
                "AND (? IS NULL OR EXISTS (SELECT 1 FROM [order] o WHERE o.customer_id = c.cus_id AND o.branch_id = ?))";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            String searchValue = normalizeLike(keyword);
            Integer branchId = parseInteger(branchIdFilter);

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);

            setNullableInt(ps, 5, branchId);
            setNullableInt(ps, 6, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi countCustomers", e);
        }

        return 0;
    }

    // =====================================================
    // SEARCH IN POS (Priority: 1. Phone, 2. Name, 3. Email)
    // =====================================================

    public List<Customer> searchCustomersForPOS(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql =
                CUSTOMER_SELECT +
                "WHERE c.status = 'ACTIVE' " +
                "AND ( " +
                "    ? IS NULL " +
                "    OR c.phone LIKE ? " +
                "    OR c.full_name LIKE ? " +
                "    OR c.email LIKE ? " +
                ") " +
                "ORDER BY " +
                "  CASE " +
                "    WHEN c.phone = ? THEN 1 " +
                "    WHEN c.phone LIKE ? THEN 2 " +
                "    WHEN c.full_name LIKE ? THEN 3 " +
                "    WHEN c.email LIKE ? THEN 4 " +
                "    ELSE 5 " +
                "  END, c.created_at DESC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            String searchValue = normalizeLike(keyword);
            String rawSearchValue = normalize(keyword);

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);

            ps.setString(5, rawSearchValue);
            ps.setString(6, searchValue);
            ps.setString(7, searchValue);
            ps.setString(8, searchValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi searchCustomersForPOS", e);
        }
        return list;
    }

    // =====================================================
    // SOFT DELETE
    // =====================================================

    public boolean softDelete(int customerId) {
        String sql = "UPDATE customer SET status = 'INACTIVE', updated_at = GETDATE() WHERE cus_id = ?";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi softDelete customer", e);
            return false;
        }
    }

    // =====================================================
    // OVERVIEW
    // =====================================================

    public CustomerOverview getCustomerOverview() {
        CustomerOverview overview = new CustomerOverview();

        loadTotalInfo(overview);
        loadTopCustomer(overview);

        return overview;
    }

    private void loadTotalInfo(CustomerOverview overview) {
        String sql =
                "SELECT " +
                "    COUNT(*) AS TotalCustomers, " +
                "    COUNT(CASE WHEN c.created_at >= DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0) THEN 1 END) AS NewThisMonth, " +
                "    COALESCE(SUM(c.total_spent), 0) AS TotalSpent " +
                "FROM customer c " +
                "WHERE c.status = 'ACTIVE'";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                overview.setTotalCustomers(rs.getInt("TotalCustomers"));
                overview.setNewCustomersThisMonth(rs.getInt("NewThisMonth"));

                BigDecimal spent = rs.getBigDecimal("TotalSpent");
                overview.setTotalSpent(spent == null ? BigDecimal.ZERO : spent);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi loadTotalInfo", e);
        }
    }

    private void loadTopCustomer(CustomerOverview overview) {
        // Lấy customer có current_points cao nhất
        String sql =
                "SELECT TOP 1 " +
                "    c.full_name, " +
                "    COALESCE(cp.current_points, 0) AS CurrentPoints " +
                "FROM customer c " +
                "LEFT JOIN customer_point cp ON cp.cus_id = c.cus_id " +
                "WHERE c.status = 'ACTIVE' " +
                "ORDER BY cp.current_points DESC, c.total_spent DESC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                overview.setTopCustomerName(rs.getString("full_name"));
                overview.setTopCustomerPoints(rs.getInt("CurrentPoints"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi loadTopCustomer", e);
        }
    }

    // =====================================================
    // FIND BY ID
    // =====================================================

    public Customer findById(int customerId) {
        String sql = CUSTOMER_SELECT + "WHERE c.cus_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi findById customer", e);
        }
        return null;
    }

    // =====================================================
    // INSERT
    // =====================================================

    public boolean insert(Customer customer) {
        String sql = "INSERT INTO customer (full_name, gender, bod, address, email, phone, total_spent, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', GETDATE(), GETDATE())";

        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, customer.getFullName());
                ps.setString(2, customer.getGender());
                ps.setObject(3, customer.getDateOfBirth());
                ps.setString(4, customer.getAddress());
                ps.setString(5, customer.getEmail());
                ps.setString(6, customer.getPhone());
                ps.setBigDecimal(7, customer.getTotalSpent() != null ? customer.getTotalSpent() : BigDecimal.ZERO);

                int affected = ps.executeUpdate();
                if (affected <= 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int customerId = generatedKeys.getInt(1);
                        insertPointRecord(connection, customerId);
                        connection.commit();
                        return true;
                    }
                }
                connection.rollback();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi insert customer", e);
        }
        return false;
    }

    // =====================================================
    // UPDATE (Allows editing point records for Admin)
    // =====================================================

    public boolean update(Customer customer, boolean updatePoints, int currentPoints, int lifetimePoints) {
        String sql = "UPDATE customer SET full_name = ?, gender = ?, bod = ?, address = ?, email = ?, phone = ?, total_spent = ?, updated_at = GETDATE() WHERE cus_id = ?";
        String sqlPoint = "UPDATE customer_point SET current_points = ?, lifetime_points = ?, updated_at = GETDATE() WHERE cus_id = ?";

        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, customer.getFullName());
                    ps.setString(2, customer.getGender());
                    ps.setObject(3, customer.getDateOfBirth());
                    ps.setString(4, customer.getAddress());
                    ps.setString(5, customer.getEmail());
                    ps.setString(6, customer.getPhone());
                    ps.setBigDecimal(7, customer.getTotalSpent() != null ? customer.getTotalSpent() : BigDecimal.ZERO);
                    ps.setInt(8, customer.getCustomerId());
                    ps.executeUpdate();
                }

                if (updatePoints) {
                    try (PreparedStatement psPoint = connection.prepareStatement(sqlPoint)) {
                        psPoint.setInt(1, currentPoints);
                        psPoint.setInt(2, lifetimePoints);
                        psPoint.setInt(3, customer.getCustomerId());
                        psPoint.executeUpdate();
                    }
                }

                connection.commit();
                return true;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi update customer", e);
            return false;
        }
    }

    // =====================================================
    // DUPLICATE CHECK
    // =====================================================

    public boolean isEmailOrPhoneExists(String email, String phone, Integer excludeCustomerId) {
        String normalizedEmail = normalize(email);
        String normalizedPhone = normalize(phone);

        if (normalizedEmail == null && normalizedPhone == null) {
            return false;
        }

        String sql =
                "SELECT COUNT(*) AS Total " +
                "FROM customer " +
                "WHERE status = 'ACTIVE' " +
                "AND ( " +
                "    (? IS NOT NULL AND email = ?) " +
                "    OR (? IS NOT NULL AND phone = ?) " +
                ") " +
                "AND (? IS NULL OR cus_id <> ?)";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            ps.setString(2, normalizedEmail);
            ps.setString(3, normalizedPhone);
            ps.setString(4, normalizedPhone);

            if (excludeCustomerId == null) {
                ps.setNull(5, Types.INTEGER);
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(5, excludeCustomerId);
                ps.setInt(6, excludeCustomerId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total") > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi isEmailOrPhoneExists", e);
        }

        return true;
    }

    // =====================================================
    // LOYALTY: REDEEM POINTS
    // =====================================================

    public boolean redeemPoints(int customerId, int pointsToRedeem, String description) {
        if (pointsToRedeem <= 0) {
            return false;
        }

        String pointSql = "SELECT cus_point_id, current_points FROM customer_point WHERE cus_id = ?";
        String updateSql = "UPDATE customer_point SET current_points = ?, updated_at = GETDATE() WHERE cus_point_id = ?";
        String transactionSql = "INSERT INTO point_transaction (cus_point_id, order_id, before_points, after_points, description, created_at) VALUES (?, NULL, ?, ?, ?, GETDATE())";

        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(pointSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }

                    int cusPointId = rs.getInt("cus_point_id");
                    int currentPoints = rs.getInt(CURRENT_POINTS_COLUMN);
                    if (currentPoints < pointsToRedeem) {
                        connection.rollback();
                        return false;
                    }

                    int afterPoints = currentPoints - pointsToRedeem;
                    try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
                        updatePs.setInt(1, afterPoints);
                        updatePs.setInt(2, cusPointId);
                        updatePs.executeUpdate();
                    }

                    try (PreparedStatement transactionPs = connection.prepareStatement(transactionSql)) {
                        transactionPs.setInt(1, cusPointId);
                        transactionPs.setInt(2, currentPoints);
                        transactionPs.setInt(3, afterPoints);
                        transactionPs.setString(4, description == null || description.isBlank() ? "Đổi điểm" : description);
                        transactionPs.executeUpdate();
                    }
                    connection.commit();
                    return true;
                }
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi redeemPoints", e);
            return false;
        }
    }

    // =====================================================
    // LOYALTY: SYNC FROM PAID ORDERS
    // =====================================================

    public void syncLoyaltyFromPaidOrders(int customerId) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) AS spent "
                + "FROM [order] o "
                + "WHERE o.customer_id = ? AND (LOWER(COALESCE(o.status, '')) = 'paid' OR EXISTS (SELECT 1 FROM payment p WHERE p.order_id = o.order_id AND LOWER(COALESCE(p.payment_status, '')) = 'paid'))";
        String pointSql = "SELECT current_points, lifetime_points FROM customer_point WHERE cus_id = ?";
        String updateSql = "UPDATE customer SET total_spent = ?, updated_at = GETDATE() WHERE cus_id = ?";
        String upsertPointSql = "MERGE customer_point AS target "
                + "USING (SELECT ? AS cus_id) AS source ON target.cus_id = source.cus_id "
                + "WHEN MATCHED THEN UPDATE SET current_points = ?, lifetime_points = ?, updated_at = GETDATE() "
                + "WHEN NOT MATCHED THEN INSERT (cus_id, current_points, lifetime_points, updated_at) VALUES (?, ?, ?, GETDATE());";

        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal spent = BigDecimal.ZERO;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            spent = rs.getBigDecimal("spent");
                        }
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setBigDecimal(1, spent);
                    ps.setInt(2, customerId);
                    ps.executeUpdate();
                }

                int points = calculatePoints(spent);
                try (PreparedStatement ps = connection.prepareStatement(pointSql)) {
                    ps.setInt(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int currentPoints = rs.getInt(CURRENT_POINTS_COLUMN);
                            int lifetimePoints = rs.getInt(LIFETIME_POINTS_COLUMN);
                            if (points > currentPoints) {
                                currentPoints = points;
                            }
                            if (points > lifetimePoints) {
                                lifetimePoints = points;
                            }
                            try (PreparedStatement updatePs = connection.prepareStatement(upsertPointSql)) {
                                updatePs.setInt(1, customerId);
                                updatePs.setInt(2, currentPoints);
                                updatePs.setInt(3, lifetimePoints);
                                updatePs.setInt(4, customerId);
                                updatePs.setInt(5, currentPoints);
                                updatePs.setInt(6, lifetimePoints);
                                updatePs.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement insertPs = connection.prepareStatement(upsertPointSql)) {
                                insertPs.setInt(1, customerId);
                                insertPs.setInt(2, points);
                                insertPs.setInt(3, points);
                                insertPs.setInt(4, customerId);
                                insertPs.setInt(5, points);
                                insertPs.setInt(6, points);
                                insertPs.executeUpdate();
                            }
                        }
                    }
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi syncLoyaltyFromPaidOrders", e);
        }
    }

    // =====================================================
    // TRANSACTION HISTORY
    // =====================================================

    public List<Map<String, Object>> getPointTransactions(int customerId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
                "SELECT pt.point_transaction_id, pt.before_points, pt.after_points, pt.description, pt.created_at, o.order_code " +
                "FROM point_transaction pt " +
                "JOIN customer_point cp ON pt.cus_point_id = cp.cus_point_id " +
                "LEFT JOIN [order] o ON pt.order_id = o.order_id " +
                "WHERE cp.cus_id = ? " +
                "ORDER BY pt.created_at DESC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("transactionId", rs.getInt("point_transaction_id"));
                    map.put("beforePoints", rs.getInt("before_points"));
                    map.put("afterPoints", rs.getInt("after_points"));
                    map.put("description", rs.getString("description"));
                    map.put("createdAt", rs.getTimestamp("created_at"));
                    map.put("orderCode", rs.getString("order_code"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi getPointTransactions", e);
        }
        return list;
    }

    // =====================================================
    // ORDER HISTORY
    // =====================================================

    public List<Map<String, Object>> getOrderHistory(int customerId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
                "SELECT o.order_id, o.order_code, o.order_type, o.total_amount, o.status, o.created_at, b.branch_name " +
                "FROM [order] o " +
                "LEFT JOIN branch b ON o.branch_id = b.branch_id " +
                "WHERE o.customer_id = ? " +
                "ORDER BY o.created_at DESC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", rs.getInt("order_id"));
                    map.put("orderCode", rs.getString("order_code"));
                    map.put("orderType", rs.getString("order_type"));
                    map.put("totalAmount", rs.getBigDecimal("total_amount"));
                    map.put("status", rs.getString("status"));
                    map.put("createdAt", rs.getTimestamp("created_at"));
                    map.put("branchName", rs.getString("branch_name"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi getOrderHistory", e);
        }
        return list;
    }

    // =====================================================
    // LOYALTY: EARN POINTS (connection-aware, for transactional checkout)
    // =====================================================

    public void earnPoints(Connection conn, int customerId, int earnedPoints, int orderId) throws SQLException {
        String findSql = "SELECT cus_point_id, current_points, lifetime_points FROM customer_point WHERE cus_id = ?";
        String updateSql = "UPDATE customer_point SET current_points = ?, lifetime_points = ?, updated_at = GETDATE() WHERE cus_point_id = ?";
        String txSql = "INSERT INTO point_transaction (cus_point_id, order_id, before_points, after_points, description, created_at) VALUES (?, ?, ?, ?, ?, GETDATE())";

        int cusPointId;
        int beforePoints;
        int lifetimePoints;

        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cusPointId = rs.getInt("cus_point_id");
                    beforePoints = rs.getInt(CURRENT_POINTS_COLUMN);
                    lifetimePoints = rs.getInt(LIFETIME_POINTS_COLUMN);
                } else {
                    cusPointId = -1;
                    beforePoints = 0;
                    lifetimePoints = 0;
                }
            }
        }

        if (cusPointId < 0) {
            String insertSql = "INSERT INTO customer_point (cus_id, current_points, lifetime_points, updated_at) VALUES (?, ?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.setInt(2, earnedPoints);
                ps.setInt(3, earnedPoints);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) cusPointId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(txSql)) {
                ps.setInt(1, cusPointId);
                ps.setInt(2, orderId);
                ps.setInt(3, 0);
                ps.setInt(4, earnedPoints);
                ps.setString(5, "Earned " + earnedPoints + " points from POS Order #" + orderId);
                ps.executeUpdate();
            }
        } else {
            int afterPoints = beforePoints + earnedPoints;
            int afterLifetime = lifetimePoints + earnedPoints;
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, afterPoints);
                ps.setInt(2, afterLifetime);
                ps.setInt(3, cusPointId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(txSql)) {
                ps.setInt(1, cusPointId);
                ps.setInt(2, orderId);
                ps.setInt(3, beforePoints);
                ps.setInt(4, afterPoints);
                ps.setString(5, "Earned " + earnedPoints + " points from POS Order #" + orderId);
                ps.executeUpdate();
            }
        }
    }

    // =====================================================
    // HELPER: GET ALL BRANCHES
    // =====================================================

    public List<model.Branch> getAllBranches() {
        List<model.Branch> list = new ArrayList<>();
        String sql = "SELECT branch_id, branch_name FROM Branch WHERE status = 'ACTIVE' ORDER BY branch_name";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.Branch b = new model.Branch();
                b.setBranchID(rs.getInt("branch_id"));
                b.setName(rs.getString("branch_name"));
                list.add(b);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi getAllBranches", e);
        }
        return list;
    }

    // =====================================================
    // PRIVATE HELPER METHODS
    // =====================================================

    private void insertPointRecord(Connection connection, int customerId) throws SQLException {
        String sql = "INSERT INTO customer_point (cus_id, current_points, lifetime_points, updated_at) VALUES (?, 0, 0, GETDATE())";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    private int calculatePoints(BigDecimal spent) {
        if (spent == null) return 0;
        LoyaltyPointSetting setting = new LoyaltyPointSettingDAO().getSetting();
        java.math.BigDecimal amountPerPoint = setting.getAmountPerPoint();
        if (amountPerPoint.compareTo(java.math.BigDecimal.ZERO) <= 0) return 0;
        return spent.divide(amountPerPoint, 0, java.math.RoundingMode.DOWN).intValue();
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("cus_id"));
        customer.setFullName(rs.getString("full_name"));
        customer.setGender(rs.getString("gender"));
        java.time.LocalDate bod = rs.getObject("bod", java.time.LocalDate.class);
        customer.setDateOfBirth(bod);
        customer.setAddress(rs.getString("address"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setTotalSpent(rs.getBigDecimal("total_spent"));
        customer.setStatus(rs.getString("status"));
        customer.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        customer.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        customer.setLoyaltyPoint(rs.getInt("current_points"));
        customer.setLifetimePoints(rs.getInt("lifetime_points"));

        return customer;
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String normalizeLike(String value) {
        String normalized = normalize(value);

        if (normalized == null) {
            return null;
        }

        normalized = normalized.replaceAll("\\s+", " ");

        return "%" + normalized + "%";
    }

    private Integer parseInteger(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value)
            throws SQLException {
        if (value == null || value <= 0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}

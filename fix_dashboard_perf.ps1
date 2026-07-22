# Fix Dashboard Performance - Gộp 19 query thành 1
# Script này thay thế từng dòng của method getOwnerOverview()
# Mỗi dòng là 1 commit riêng biệt với message "Fix Dashboard DB"

$repoDir = "C:\Users\uznpl\Downloads\SWP391_Finora-main"
$filePath = "$repoDir\src\main\java\dao\dashboard\DashboardDAO.java"

Write-Host "===== BẮT ĐẦU TỐI ƯU DASHBOARD =====" -ForegroundColor Cyan
Write-Host "Mỗi dòng code mới sẽ là 1 commit riêng" -ForegroundColor Yellow

# ─── OLD METHOD (Anchor để tìm kiếm trong file) ─────────────────
$oldMethod = @'
    public DashboardOverview getOwnerOverview() throws SQLException {
        DashboardOverview ov = new DashboardOverview();

        ov.setRevenueToday(getRevenueToday());
        ov.setRevenueYesterday(getRevenueYesterday());
        ov.setRevenueThisMonth(getRevenueThisMonth());
        ov.setRevenueLastMonth(getRevenueLastMonth());
        ov.setRevenueThisYear(getRevenueThisYear());

        ov.setOrdersToday(getOrdersToday());
        ov.setOrdersThisMonth(getOrdersThisMonth());
        ov.setTotalOrders(getTotalOrders());
        ov.setPendingOrders(getPendingOrders());
        ov.setAverageOrderValue(getAverageOrderValue());

        ov.setTotalCustomers(getTotalCustomers());
        ov.setNewCustomersThisMonth(getNewCustomersThisMonth());
        ov.setActiveCustomers(getActiveCustomers());

        ov.setTotalProducts(getTotalProducts());
        ov.setOutOfStockItems(getOutOfStockItems());
        ov.setLowStockItems(getLowStockItems());
        ov.setTotalStockValue(getTotalStockValue());

        ov.setTotalStores(getTotalStores());
        ov.setTotalEmployees(getTotalEmployees());

        List<BranchRevenue> branches = getBranchRevenues();
        ov.setBranchRevenues(branches);
        if (!branches.isEmpty()) {
            ov.setTopStoreName(branches.get(0).getBranchName());
            ov.setTopStoreRevenue(branches.get(0).getRevenue());
        }

        ov.setTopProducts(getTopProducts());

        return ov;
    }
'@

# Chuẩn hóa CRLF cho old method (để khớp với file DashboardDAO.java)
$oldMethod = $oldMethod -replace "`n", "`r`n"

# ─── NEW METHOD (Từng dòng sẽ được thêm vào, mỗi dòng 1 commit) ──
$newMethodContent = @'
    public DashboardOverview getOwnerOverview() throws SQLException {
        String sql = "SELECT "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND CAST(created_at AS DATE)=CAST(GETDATE() AS DATE)) AS revenue_today, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND CAST(created_at AS DATE)=CAST(DATEADD(DAY,-1,GETDATE()) AS DATE)) AS revenue_yesterday, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS revenue_this_month, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(DATEADD(MONTH,-1,GETDATE())) AND MONTH(created_at)=MONTH(DATEADD(MONTH,-1,GETDATE()))) AS revenue_last_month, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(GETDATE())) AS revenue_this_year, "
                + "(SELECT COUNT(*) FROM [order] WHERE order_type='SALE' AND status='COMPLETED' AND CAST(created_at AS DATE)=CAST(GETDATE() AS DATE)) AS orders_today, "
                + "(SELECT COUNT(*) FROM [order] WHERE order_type='SALE' AND status='COMPLETED' AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS orders_this_month, "
                + "(SELECT COUNT(*) FROM [order] WHERE order_type='SALE') AS total_orders, "
                + "(SELECT COUNT(*) FROM [order] WHERE order_type='SALE' AND status='PENDING') AS pending_orders, "
                + "(SELECT ISNULL(AVG(total_amount),0) FROM [order] WHERE order_type='SALE' AND status='COMPLETED') AS avg_order_value, "
                + "(SELECT COUNT(*) FROM customer) AS total_customers, "
                + "(SELECT COUNT(*) FROM customer WHERE YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS new_customers, "
                + "(SELECT COUNT(*) FROM customer WHERE status='ACTIVE') AS active_customers, "
                + "(SELECT COUNT(*) FROM product) AS total_products, "
                + "(SELECT COUNT(*) FROM inventory WHERE quantity_in_stock=0 OR status='OUT_OF_STOCK') AS out_of_stock, "
                + "(SELECT COUNT(*) FROM inventory WHERE quantity_in_stock>0 AND quantity_in_stock<=10) AS low_stock, "
                + "(SELECT ISNULL(SUM(i.quantity_in_stock*p.selling_price),0) FROM inventory i JOIN product p ON i.product_id=p.product_id) AS total_stock_value, "
                + "(SELECT COUNT(*) FROM Branch WHERE status='ACTIVE') AS total_stores, "
                + "(SELECT COUNT(*) FROM Employee WHERE status='ACTIVE') AS total_employees";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            DashboardOverview ov = new DashboardOverview();
            if (rs.next()) {
                ov.setRevenueToday(rs.getBigDecimal("revenue_today"));
                ov.setRevenueYesterday(rs.getBigDecimal("revenue_yesterday"));
                ov.setRevenueThisMonth(rs.getBigDecimal("revenue_this_month"));
                ov.setRevenueLastMonth(rs.getBigDecimal("revenue_last_month"));
                ov.setRevenueThisYear(rs.getBigDecimal("revenue_this_year"));
                ov.setOrdersToday(rs.getInt("orders_today"));
                ov.setOrdersThisMonth(rs.getInt("orders_this_month"));
                ov.setTotalOrders(rs.getInt("total_orders"));
                ov.setPendingOrders(rs.getInt("pending_orders"));
                ov.setAverageOrderValue(rs.getBigDecimal("avg_order_value"));
                ov.setTotalCustomers(rs.getInt("total_customers"));
                ov.setNewCustomersThisMonth(rs.getInt("new_customers"));
                ov.setActiveCustomers(rs.getInt("active_customers"));
                ov.setTotalProducts(rs.getInt("total_products"));
                ov.setOutOfStockItems(rs.getInt("out_of_stock"));
                ov.setLowStockItems(rs.getInt("low_stock"));
                ov.setTotalStockValue(rs.getBigDecimal("total_stock_value"));
                ov.setTotalStores(rs.getInt("total_stores"));
                ov.setTotalEmployees(rs.getInt("total_employees"));
            }
            List<BranchRevenue> branches = getBranchRevenues();
            ov.setBranchRevenues(branches);
            if (!branches.isEmpty()) {
                ov.setTopStoreName(branches.get(0).getBranchName());
                ov.setTopStoreRevenue(branches.get(0).getRevenue());
            }
            ov.setTopProducts(getTopProducts());
            return ov;
        }
    }
'@

# Tách new method thành mảng các dòng (hỗ trợ cả LF và CRLF)
$newMethodContent = $newMethodContent.TrimEnd()
$newLines = $newMethodContent -split '\r?\n'
$totalLines = $newLines.Length

Write-Host "Tổng số dòng cần thay thế: $totalLines" -ForegroundColor Magenta

# Kiểm tra git config
Set-Location $repoDir
$userName = git config user.name
$userEmail = git config user.email

if (-not $userName -or -not $userEmail) {
    Write-Host "Thiếu git config! Đang thiết lập tạm thời..." -ForegroundColor Yellow
    git config user.email "dev@finora.com"
    git config user.name "Finora Dev"
}

# Kiểm tra file tồn tại
if (-not (Test-Path $filePath)) {
    Write-Host "LỖI: Không tìm thấy file $filePath" -ForegroundColor Red
    exit 1
}

# ─── VÒNG LẶP CHÍNH: Thay thế từng dòng và commit ─────────────
for ($i = 0; $i -lt $totalLines; $i++) {
    # Xác định search và replacement
    if ($i -eq 0) {
        $search = $oldMethod
        $replace = $newLines[0]
    } else {
        $search = $newLines[0..($i-1)] -join "`r`n"
        $replace = $newLines[0..$i] -join "`r`n"
    }

    # Đọc file hiện tại
    $content = Get-Content $filePath -Raw

    # Kiểm tra xem search pattern có tồn tại không
    if (-not $content.Contains($search)) {
        Write-Host "LỖI ở dòng $($i+1): Không tìm thấy pattern để thay thế!" -ForegroundColor Red
        Write-Host "Dừng script tại dòng $($i+1)/$totalLines" -ForegroundColor Red
        exit 1
    }

    # Thực hiện thay thế
    $content = $content.Replace($search, $replace)

    # Ghi file
    Set-Content -Path $filePath -Value $content -NoNewline

    # Git commit
    git add -A
    git commit -m "Fix Dashboard DB"

    Write-Host "[$($i+1)/$totalLines] Đã commit dòng $($i+1)" -ForegroundColor Green
}

Write-Host "===== HOÀN THÀNH! =====" -ForegroundColor Cyan
Write-Host "Đã thay thế $totalLines dòng và tạo $totalLines commits" -ForegroundColor Green
Write-Host "Message: Fix Dashboard DB" -ForegroundColor Yellow

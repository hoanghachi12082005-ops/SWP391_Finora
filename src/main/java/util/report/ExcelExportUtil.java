package util.report;

import model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ExcelExportUtil {

    private static final String COMPANY = "Finora Retail";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ExcelExportUtil() {}

    public static byte[] generateInventoryReport(
            String generatedBy, List<InventoryReportItem> rows, InventoryReportOverview overview,
            String keyword, String branchName) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Inventory Report");
            setupReport(wb, sheet, "Inventory Report", generatedBy, keyword, branchName, null, null);
            String[] headers = {"Product ID", "Product Name", "Warehouse", "Branch", "Qty In Stock", "Selling Price", "Total Value"};
            int[] widths = {12, 35, 20, 20, 15, 18, 22};
            int rowNum = fillHeader(wb, sheet, headers, widths, 4);
            CellStyle currencyStyle = createCurrencyStyle(wb);
            for (InventoryReportItem r : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getProductId());
                row.createCell(1).setCellValue(r.getProductName());
                row.createCell(2).setCellValue(nullToDash(r.getWarehouseName()));
                row.createCell(3).setCellValue(nullToDash(r.getBranchName()));
                row.createCell(4).setCellValue(r.getQuantityInStock());
                Cell priceCell = row.createCell(5);
                priceCell.setCellValue(doubleValue(r.getSellingPrice()));
                priceCell.setCellStyle(currencyStyle);
                Cell valCell = row.createCell(6);
                valCell.setCellValue(doubleValue(r.getTotalValue()));
                valCell.setCellStyle(currencyStyle);
            }
            addInventorySummary(sheet, rowNum + 1, overview, currencyStyle);
            return toBytes(wb);
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    public static byte[] generateCustomerLoyaltyReport(
            String generatedBy, List<LoyalCustomerSummary> rows, LoyalCustomerOverview overview, String keyword) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Customer Loyalty Report");
            setupReport(wb, sheet, "Customer Loyalty Report", generatedBy, keyword, null, null, null);
            String[] headers = {"Customer", "Phone", "Email", "Total Orders", "Current Points", "Lifetime Points", "Total Spent"};
            int[] widths = {30, 18, 30, 15, 15, 15, 20};
            int rowNum = fillHeader(wb, sheet, headers, widths, 4);
            CellStyle currencyStyle = createCurrencyStyle(wb);
            for (LoyalCustomerSummary r : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getFullName());
                row.createCell(1).setCellValue(nullToDash(r.getPhone()));
                row.createCell(2).setCellValue(nullToDash(r.getEmail()));
                row.createCell(3).setCellValue(r.getTotalOrders());
                row.createCell(4).setCellValue(r.getCurrentPoints());
                row.createCell(5).setCellValue(r.getLifetimePoints());
                Cell spentCell = row.createCell(6);
                spentCell.setCellValue(doubleValue(r.getTotalSpent()));
                spentCell.setCellStyle(currencyStyle);
            }
            addLoyaltySummary(sheet, rowNum + 1, overview, currencyStyle);
            return toBytes(wb);
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    private static void setupReport(Workbook wb, Sheet sheet, String title,
                                     String generatedBy, String keyword, String branchName,
                                     LocalDate dateFrom, LocalDate dateTo) {
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setFont(titleFont);

        Row titleRow = sheet.createRow(0);
        Cell tc = titleRow.createCell(0);
        tc.setCellValue(title);
        tc.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        sheet.createRow(1).createCell(0).setCellValue(COMPANY);

        Row metaRow = sheet.createRow(2);
        String meta = "Generated: " + LocalDateTime.now().format(DATETIME_FMT) + " | By: " + generatedBy;
        if (keyword != null && !keyword.isEmpty()) meta += " | Search: " + keyword;
        metaRow.createCell(0).setCellValue(meta);

        Row filterRow = sheet.createRow(3);
        String filter = "Branch: " + (branchName != null ? branchName : "All") +
                " | Period: " + (dateFrom != null ? dateFrom.format(DATE_FMT) : "Earliest") +
                " — " + (dateTo != null ? dateTo.format(DATE_FMT) : "Latest");
        filterRow.createCell(0).setCellValue(filter);
    }

    private static int fillHeader(Workbook wb, Sheet sheet, String[] headers, int[] widths, int startRow) {
        CellStyle headerStyle = createHeaderStyle(wb);
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
        Row row = sheet.createRow(startRow);
        for (int i = 0; i < headers.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }
        return startRow + 1;
    }

    private static void addInventorySummary(Sheet sheet, int rowNum, InventoryReportOverview overview, CellStyle currencyStyle) {
        if (overview == null) return;
        rowNum++;
        Row title = sheet.createRow(rowNum++);
        title.createCell(0).setCellValue("Summary");
        addSumRow(sheet, rowNum++, "Total Products", overview.getTotalProducts(), null);
        addSumRow(sheet, rowNum++, "Total Quantity", overview.getTotalQuantity(), null);
        addSumRow(sheet, rowNum++, "Total Value", doubleValue(overview.getTotalValue()), currencyStyle);
        addSumRow(sheet, rowNum++, "Low Stock Items", overview.getLowStockCount(), null);
        addSumRow(sheet, rowNum++, "Out of Stock Items", overview.getOutOfStockCount(), null);
    }

    private static void addLoyaltySummary(Sheet sheet, int rowNum, LoyalCustomerOverview overview, CellStyle currencyStyle) {
        if (overview == null) return;
        rowNum++;
        Row title = sheet.createRow(rowNum++);
        title.createCell(0).setCellValue("Summary");
        addSumRow(sheet, rowNum++, "Total Customers", overview.getTotalCustomers(), null);
        addSumRow(sheet, rowNum++, "Total Points", overview.getTotalPoints(), null);
        addSumRow(sheet, rowNum++, "Total Spent", doubleValue(overview.getTotalSpent()), currencyStyle);
        if (overview.getTopCustomerName() != null) {
            addSumRow(sheet, rowNum++, "Top Customer", overview.getTopCustomerName());
            addSumRow(sheet, rowNum++, "Highest Spent", doubleValue(overview.getTopCustomerSpent()), currencyStyle);
        }
    }

    private static void addSumRow(Sheet sheet, int rowNum, String label, double value, CellStyle style) {
        Row r = sheet.createRow(rowNum);
        r.createCell(0).setCellValue(label);
        Cell vc = r.createCell(1);
        vc.setCellValue(value);
        if (style != null) vc.setCellStyle(style);
    }

    private static void addSumRow(Sheet sheet, int rowNum, String label, String value) {
        Row r = sheet.createRow(rowNum);
        r.createCell(0).setCellValue(label);
        r.createCell(1).setCellValue(value);
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static CellStyle createCurrencyStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static String nullToDash(String s) {
        return s == null || s.trim().isEmpty() ? "\u2014" : s.trim();
    }

    private static double doubleValue(BigDecimal v) {
        return v == null ? 0.0 : v.doubleValue();
    }

    private static String formatCurrency(BigDecimal v) {
        if (v == null) return "0";
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
        return df.format(v);
    }

    public static byte[] generateSupplierReport(
            String generatedBy, List<Supplier> rows, String keyword) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Supplier Report");
            setupReport(wb, sheet, "Supplier Report", generatedBy, keyword, null, null, null);
            String[] headers = {"Mã NCC", "Tên nhà cung cấp", "Số điện thoại", "Địa chỉ", "Trạng thái"};
            int[] widths = {12, 35, 18, 45, 18};
            int rowNum = fillHeader(wb, sheet, headers, widths, 4);
            for (Supplier r : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("NCC" + r.getSupplierID());
                row.createCell(1).setCellValue(r.getName());
                row.createCell(2).setCellValue(nullToDash(r.getPhone()));
                row.createCell(3).setCellValue(nullToDash(r.getAddress()));
                row.createCell(4).setCellValue("ACTIVE".equalsIgnoreCase(r.getStatus()) ? "Đang hoạt động" : "Ngừng hoạt động");
            }
            return toBytes(wb);
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    private static byte[] toBytes(Workbook wb) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write Excel", e);
        }
    }

    public static byte[] generateSalesTransactionReport(
            String generatedBy, List<SalesTransaction> rows, SalesTransactionKpi kpi, String filterDesc) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Báo cáo giao dịch & doanh thu");
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);

            int r = 0;
            Row titleRow = sheet.createRow(r++);
            titleRow.createCell(0).setCellValue("BÁO CÁO GIAO DỊCH & DOANH THU");
            titleRow.getCell(0).setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            sheet.createRow(r++).createCell(0).setCellValue("Ngày tạo: " + LocalDateTime.now().format(DATETIME_FMT) + " | Người tạo: " + generatedBy);
            sheet.createRow(r++).createCell(0).setCellValue("Bộ lọc: " + filterDesc);

            r++;
            Row kpiTitle = sheet.createRow(r++);
            kpiTitle.createCell(0).setCellValue("TỔNG QUAN KPI");
            kpiTitle.getCell(0).setCellStyle(titleStyle);

            CellStyle labelStyle = wb.createCellStyle();
            Font labelFont = wb.createFont();
            labelFont.setBold(true);
            labelFont.setFontHeightInPoints((short) 10);
            labelStyle.setFont(labelFont);

            r = writeKpiRow(sheet, r, labelStyle, "Tổng số giao dịch", String.valueOf(kpi.getTotalTransactions()));
            r = writeKpiRow(sheet, r, labelStyle, "Tổng doanh thu", String.format("%,.0f ₫", kpi.getTotalRevenue()));
            r = writeKpiRow(sheet, r, labelStyle, "Tổng chi phí", String.format("%,.0f ₫", kpi.getTotalExpense()));
            r = writeKpiRow(sheet, r, labelStyle, "Dòng tiền ròng", String.format("%,.0f ₫", kpi.getNetCashFlow()));
            r = writeKpiRow(sheet, r, labelStyle, "Giá trị giao dịch TB", String.format("%,.0f ₫", kpi.getAvgTransactionValue()));
            r = writeKpiRow(sheet, r, labelStyle, "Tổng đơn hàng", String.valueOf(kpi.getTotalSalesOrders()));

            r++;
            CellStyle headerStyle = createHeaderStyle(wb);
            Row header = sheet.createRow(r++);
            String[] cols = {"Mã giao dịch", "Mã đơn hàng", "Loại đơn", "Ngày", "Loại", "Phương thức", "Số tiền", "Mô tả", "Chi nhánh", "Nhân viên", "Trạng thái"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            CellStyle currencyStyle = createCurrencyStyle(wb);
            for (SalesTransaction t : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(t.getTransactionCode());
                row.createCell(1).setCellValue(t.getOrderCode() != null ? t.getOrderCode() : "");
                row.createCell(2).setCellValue(t.getOrderType() != null ? t.getOrderType() : "");
                row.createCell(3).setCellValue(t.getPaymentDate() != null ? t.getPaymentDate().toString() : "");
                row.createCell(4).setCellValue(t.getTransactionType());
                row.createCell(5).setCellValue(t.getPaymentMethod());
                Cell amt = row.createCell(6);
                amt.setCellValue(t.getAmount());
                amt.setCellStyle(currencyStyle);
                row.createCell(7).setCellValue(t.getDescription() != null ? t.getDescription() : "");
                row.createCell(8).setCellValue(t.getBranchName() != null ? t.getBranchName() : "");
                row.createCell(9).setCellValue(t.getEmployeeName() != null ? t.getEmployeeName() : "");
                row.createCell(10).setCellValue(t.getStatus() != null ? t.getStatus() : "");
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            return toBytes(wb);
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    private static int writeKpiRow(Sheet sheet, int r, CellStyle style, String label, String value) {
        Row row = sheet.createRow(r);
        row.createCell(0).setCellValue(label);
        row.getCell(0).setCellStyle(style);
        row.createCell(1).setCellValue(value);
        return r + 1;
    }
}
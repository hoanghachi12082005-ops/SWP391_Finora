package util.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import model.BranchKpi;
import model.EmployeeKpi;
import model.InventoryReportItem;
import model.InventoryReportOverview;
import model.LoyalCustomerSummary;
import model.LoyalCustomerOverview;
import model.Order;
import model.OrderReportFilter;
import model.OrderReportKpi;
import model.SalesTransaction;
import model.SalesTransactionKpi;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class PdfReportUtil {

    private static final float PAGE_MARGIN = 36;
    private static Font TITLE_FONT;
    private static Font HEADER_FONT;
    private static Font NORMAL_FONT;
    private static Font SMALL_FONT;
    private static Font TABLE_HEADER_FONT;
    private static Font TABLE_CELL_FONT;
    private static Font FOOTER_FONT;

    private PdfReportUtil() {}

    private static void initFonts() {
        BaseFont unicodeFont = null;

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            java.io.File fontFile = new java.io.File("C:\\Windows\\Fonts\\arial.ttf");
            if (fontFile.isFile()) {
                try {
                    unicodeFont = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                } catch (Exception ignored) {}
            }
        } else {
            String[] candidates = {
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
            };
            for (String path : candidates) {
                java.io.File fontFile = new java.io.File(path);
                if (fontFile.isFile()) {
                    try {
                        unicodeFont = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                        break;
                    } catch (Exception ignored) {}
                }
            }
            if (unicodeFont == null) {
                try (InputStream is = PdfReportUtil.class.getResourceAsStream("/fonts/DejaVuSans.ttf")) {
                    if (is != null) {
                        unicodeFont = BaseFont.createFont("/fonts/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    }
                } catch (Exception ignored) {}
            }
        }

        if (unicodeFont == null) {
            try {
                unicodeFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new RuntimeException("No font available for PDF generation", e);
            }
        }

        TITLE_FONT = new Font(unicodeFont, 18, Font.BOLD, new Color(0x1a, 0x1a, 0x2e));
        HEADER_FONT = new Font(unicodeFont, 10, Font.BOLD, new Color(0x1a, 0x1a, 0x2e));
        NORMAL_FONT = new Font(unicodeFont, 9, Font.NORMAL, Color.BLACK);
        SMALL_FONT = new Font(unicodeFont, 8, Font.NORMAL, new Color(0x66, 0x66, 0x66));
        TABLE_HEADER_FONT = new Font(unicodeFont, 9, Font.BOLD, Color.WHITE);
        TABLE_CELL_FONT = new Font(unicodeFont, 9, Font.NORMAL, Color.BLACK);
        FOOTER_FONT = new Font(unicodeFont, 7, Font.NORMAL, new Color(0x99, 0x99, 0x99));
    }

    private static PdfPCell createCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_CELL_FONT));
        cell.setPadding(4);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(0.3f);
        cell.setBorderColor(new Color(0xcc, 0xcc, 0xcc));
        return cell;
    }

    private static void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, TABLE_CELL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(2);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, TABLE_CELL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(2);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private static String nullToDash(String s) {
        return s == null || s.trim().isEmpty() ? "—" : s.trim();
    }

    // ---- Page Event Handler ----

    private static void addHorizontalRule(Document document) throws DocumentException {
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setFixedHeight(1f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(new Color(0xcc, 0xcc, 0xcc));
        rule.addCell(cell);
        document.add(rule);
    }

    private static class HeaderFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle rect = document.getPageSize();

            // Footer text
            Phrase footer = new Phrase("Xuất từ Finora Retail Management System", FOOTER_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (rect.getLeft() + rect.getRight()) / 2,
                    rect.getBottom() + 12, 0);

            // Page number
            Phrase pageNum = new Phrase("Trang " + writer.getPageNumber(), FOOTER_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    pageNum,
                    rect.getRight() - PAGE_MARGIN,
                    rect.getBottom() + 12, 0);
        }
    }

    public static byte[] generateInventoryReport(
            String companyName,
            String generatedBy,
            List<InventoryReportItem> rows,
            InventoryReportOverview overview,
            String keyword,
            String branchName) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, PAGE_MARGIN, PAGE_MARGIN, 50, 50);

        try {
            initFonts();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new HeaderFooter());
            document.open();

            // Header
            Paragraph title = new Paragraph("Báo cáo tồn kho", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph company = new Paragraph(companyName, HEADER_FONT);
            company.setAlignment(Element.ALIGN_CENTER);
            company.setSpacingAfter(2);
            document.add(company);

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph meta = new Paragraph("Generated: " + now + "  |  By: " + generatedBy, SMALL_FONT);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(6);
            document.add(meta);

            addHorizontalRule(document);
            document.add(new Paragraph(" "));

            // Filter info
            Paragraph filterText = new Paragraph("Chi nhánh: " + (branchName != null && !branchName.isEmpty() ? branchName : "Tất cả chi nhánh"), SMALL_FONT);
            filterText.setSpacingAfter(8);
            document.add(filterText);

            // Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 5, 3, 3, 2, 3, 3});
            table.setHeaderRows(1);
            
            // Header cells
            String[] headers = {"Mã SP", "Tên sản phẩm", "Kho hàng", "Chi nhánh", "Tồn", "Giá bán", "Tổng giá trị"};
            Color bg = new Color(0x1a, 0x1a, 0x2e);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER_FONT));
                cell.setBackgroundColor(bg);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setGroupingUsed(true);
            nf.setMaximumFractionDigits(0);

            if (rows == null || rows.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Không có dữ liệu báo cáo.", TABLE_CELL_FONT));
                emptyCell.setColspan(7);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(12);
                table.addCell(emptyCell);
            } else {
                for (InventoryReportItem row : rows) {
                    table.addCell(createCell(String.valueOf(row.getProductId()), Element.ALIGN_CENTER));
                    table.addCell(createCell(row.getProductName(), Element.ALIGN_LEFT));
                    table.addCell(createCell(row.getWarehouseName(), Element.ALIGN_LEFT));
                    table.addCell(createCell(row.getBranchName(), Element.ALIGN_LEFT));
                    table.addCell(createCell(String.valueOf(row.getQuantityInStock()), Element.ALIGN_RIGHT));
                    table.addCell(createCell(nf.format(row.getSellingPrice()) + " ₫", Element.ALIGN_RIGHT));
                    table.addCell(createCell(nf.format(row.getTotalValue()) + " ₫", Element.ALIGN_RIGHT));
                }
            }
            document.add(table);

            // Summary
            if (overview != null) {
                addHorizontalRule(document);
                document.add(new Paragraph(" "));
                Paragraph sumTitle = new Paragraph("Tổng kết", HEADER_FONT);
                sumTitle.setSpacingAfter(4);
                document.add(sumTitle);

                PdfPTable summaryTable = new PdfPTable(2);
                summaryTable.setWidthPercentage(60);
                summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                summaryTable.setWidths(new float[]{3, 3});

                addSummaryRow(summaryTable, "Tổng số sản phẩm", String.valueOf(overview.getTotalProducts()));
                addSummaryRow(summaryTable, "Tổng số lượng tồn", String.valueOf(overview.getTotalQuantity()));
                addSummaryRow(summaryTable, "Tổng giá trị tồn kho", nf.format(overview.getTotalValue()) + " ₫");
                addSummaryRow(summaryTable, "Sản phẩm sắp hết hàng (<=10)", String.valueOf(overview.getLowStockCount()));
                addSummaryRow(summaryTable, "Sản phẩm hết hàng (0)", String.valueOf(overview.getOutOfStockCount()));
                document.add(summaryTable);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            try { if (document.isOpen()) document.close(); } catch (Exception ignored) {}
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateCustomerLoyaltyReport(
            String companyName,
            String generatedBy,
            List<LoyalCustomerSummary> rows,
            LoyalCustomerOverview overview,
            String keyword,
            String branchName,
            LocalDate dateFrom,
            LocalDate dateTo,
            String datePreset) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, PAGE_MARGIN, PAGE_MARGIN, 50, 50);

        try {
            initFonts();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new HeaderFooter());
            document.open();

            // Header
            Paragraph title = new Paragraph("Báo cáo khách hàng thân thiết", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph company = new Paragraph(companyName, HEADER_FONT);
            company.setAlignment(Element.ALIGN_CENTER);
            company.setSpacingAfter(2);
            document.add(company);

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph meta = new Paragraph("Generated: " + now + "  |  By: " + generatedBy, SMALL_FONT);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(6);
            document.add(meta);

            addHorizontalRule(document);
            document.add(new Paragraph(" "));

            // Filter info
            StringBuilder filters = new StringBuilder();
            if (branchName != null && !branchName.isEmpty()) {
                filters.append("Chi nhánh: ").append(branchName).append("  |  ");
            }
            if (datePreset != null && !datePreset.isEmpty()) {
                filters.append("Thời gian: ").append(getDatePresetLabel(datePreset));
                if (dateFrom != null && dateTo != null) {
                    filters.append(" (").append(dateFrom.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                           .append(" - ").append(dateTo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append(")");
                }
                filters.append("  |  ");
            } else if (dateFrom != null || dateTo != null) {
                filters.append("Thời gian: ");
                if (dateFrom != null) filters.append("Từ ").append(dateFrom.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append(" ");
                if (dateTo != null) filters.append("Đến ").append(dateTo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                filters.append("  |  ");
            }
            if (keyword != null && !keyword.isEmpty()) {
                filters.append("Tìm kiếm: ").append(keyword).append("  |  ");
            }
            
            String filterStr = filters.toString();
            if (filterStr.endsWith("  |  ")) {
                filterStr = filterStr.substring(0, filterStr.length() - 5);
            }
            
            if (!filterStr.isEmpty()) {
                Paragraph filterText = new Paragraph(filterStr, SMALL_FONT);
                filterText.setSpacingAfter(8);
                document.add(filterText);
            }

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 3, 4, 2, 2, 3});
            table.setHeaderRows(1);
            
            // Header cells
            String[] headers = {"Khách hàng", "Số điện thoại", "Email", "Tổng đơn", "Điểm hiện tại", "Tổng chi tiêu"};
            Color bg = new Color(0x1a, 0x1a, 0x2e);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER_FONT));
                cell.setBackgroundColor(bg);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setGroupingUsed(true);
            nf.setMaximumFractionDigits(0);

            if (rows == null || rows.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Không có dữ liệu báo cáo.", TABLE_CELL_FONT));
                emptyCell.setColspan(6);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(12);
                table.addCell(emptyCell);
            } else {
                for (LoyalCustomerSummary row : rows) {
                    table.addCell(createCell(row.getFullName(), Element.ALIGN_LEFT));
                    table.addCell(createCell(row.getPhone(), Element.ALIGN_LEFT));
                    table.addCell(createCell(nullToDash(row.getEmail()), Element.ALIGN_LEFT));
                    table.addCell(createCell(String.valueOf(row.getTotalOrders()), Element.ALIGN_RIGHT));
                    table.addCell(createCell(String.valueOf(row.getCurrentPoints()), Element.ALIGN_RIGHT));
                    table.addCell(createCell(nf.format(row.getTotalSpent()) + " ₫", Element.ALIGN_RIGHT));
                }
            }
            document.add(table);

            // Summary
            if (overview != null) {
                addHorizontalRule(document);
                document.add(new Paragraph(" "));
                Paragraph sumTitle = new Paragraph("Tổng kết", HEADER_FONT);
                sumTitle.setSpacingAfter(4);
                document.add(sumTitle);

                PdfPTable summaryTable = new PdfPTable(2);
                summaryTable.setWidthPercentage(60);
                summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                summaryTable.setWidths(new float[]{3, 3});

                addSummaryRow(summaryTable, "Tổng số khách hàng", String.valueOf(overview.getTotalCustomers()));
                addSummaryRow(summaryTable, "Tổng chi tiêu khách hàng", nf.format(overview.getTotalSpent()) + " ₫");
                if (overview.getTopCustomerName() != null) {
                    addSummaryRow(summaryTable, "Khách hàng VIP nhất", overview.getTopCustomerName());
                    addSummaryRow(summaryTable, "Chi tiêu nhiều nhất", nf.format(overview.getTopCustomerSpent()) + " ₫");
                }
                document.add(summaryTable);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            try { if (document.isOpen()) document.close(); } catch (Exception ignored) {}
            throw new RuntimeException(e);
        }
    }

    // ── Order Report PDF ────────────────────────────────────────

    public static byte[] generateOrderReportPdf(
            String companyName,
            String generatedBy,
            List<Order> rows,
            OrderReportFilter filter,
            String datePreset,
            String filterLines,
            OrderReportKpi kpi,
            EmployeeKpi empKpi,
            List<BranchKpi> branchKpis,
            boolean isOwner) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), PAGE_MARGIN, PAGE_MARGIN, 50, 50);

        try {
            initFonts();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new HeaderFooter());
            document.open();

            Paragraph title = new Paragraph("BÁO CÁO ĐƠN HÀNG", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph company = new Paragraph(companyName, HEADER_FONT);
            company.setAlignment(Element.ALIGN_CENTER);
            company.setSpacingAfter(2);
            document.add(company);

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph meta = new Paragraph("Ngày xuất: " + now + "  |  Người xuất: " + generatedBy, SMALL_FONT);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(6);
            document.add(meta);

            addHorizontalRule(document);
            document.add(new Paragraph(" "));

            if (filterLines != null && !filterLines.isEmpty()) {
                Paragraph filterTitle = new Paragraph("Bộ lọc áp dụng:", HEADER_FONT);
                filterTitle.setSpacingAfter(4);
                document.add(filterTitle);

                Paragraph filterPara = new Paragraph(filterLines, SMALL_FONT);
                filterPara.setSpacingAfter(8);
                document.add(filterPara);
            }

            // KPI Summary
            if (kpi != null) {
                addHorizontalRule(document);
                document.add(new Paragraph(" "));
                Paragraph kpiTitle = new Paragraph("KPI SUMMARY", HEADER_FONT);
                kpiTitle.setSpacingAfter(6);
                document.add(kpiTitle);

                PdfPTable kpiSummaryTable = new PdfPTable(2);
                kpiSummaryTable.setWidthPercentage(60);
                kpiSummaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                kpiSummaryTable.setWidths(new float[]{3, 3});

                NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
                nf.setGroupingUsed(true);
                nf.setMaximumFractionDigits(0);

                addSummaryRow(kpiSummaryTable, "Tổng số đơn hàng", String.valueOf(kpi.getTotalOrders()));
                addSummaryRow(kpiSummaryTable, "Tổng doanh thu", nf.format(kpi.getTotalRevenue()) + " ₫");
                addSummaryRow(kpiSummaryTable, "Giá trị trung bình (AOV)", nf.format(kpi.getAov()) + " ₫");
                addSummaryRow(kpiSummaryTable, "Đơn hoàn thành", String.valueOf(kpi.getCompletedOrders()));
                addSummaryRow(kpiSummaryTable, "Đơn đã hủy", String.valueOf(kpi.getCancelledOrders()));
                addSummaryRow(kpiSummaryTable, "Tỉ lệ hoàn thành", String.format("%.1f%%", kpi.getCompletionRate()));
                document.add(kpiSummaryTable);

                if (empKpi != null) {
                    document.add(new Paragraph(" "));
                    Paragraph empTitle = new Paragraph("NHÂN VIÊN: " + empKpi.getEmployeeName(), HEADER_FONT);
                    empTitle.setSpacingAfter(4);
                    document.add(empTitle);

                    PdfPTable empTable = new PdfPTable(2);
                    empTable.setWidthPercentage(60);
                    empTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                    empTable.setWidths(new float[]{3, 3});

                    addSummaryRow(empTable, "Đơn hoàn thành", String.valueOf(empKpi.getCompletedOrders()));
                    addSummaryRow(empTable, "Đơn đã hủy", String.valueOf(empKpi.getCancelledOrders()));
                    addSummaryRow(empTable, "Doanh thu", nf.format(empKpi.getRevenue()) + " ₫");
                    addSummaryRow(empTable, "AOV", nf.format(empKpi.getAov()) + " ₫");
                    addSummaryRow(empTable, "Tỉ lệ hoàn thành", String.format("%.1f%%", empKpi.getCompletionRate()));
                    document.add(empTable);
                }

                if (isOwner && branchKpis != null && !branchKpis.isEmpty()) {
                    document.add(new Paragraph(" "));
                    Paragraph branchTitle = new Paragraph("DOANH THU THEO CHI NHÁNH", HEADER_FONT);
                    branchTitle.setSpacingAfter(4);
                    document.add(branchTitle);

                    PdfPTable branchTable = new PdfPTable(3);
                    branchTable.setWidthPercentage(80);
                    branchTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                    branchTable.setWidths(new float[]{3, 2, 2});

                    Color branchHeaderBg = new Color(0x1a, 0x1a, 0x2e);
                    String[] branchHeaders = {"Chi nhánh", "Doanh thu", "Tỉ lệ"};
                    for (String h : branchHeaders) {
                        PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER_FONT));
                        cell.setBackgroundColor(branchHeaderBg);
                        cell.setPadding(4);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBorder(Rectangle.NO_BORDER);
                        branchTable.addCell(cell);
                    }

                    for (BranchKpi bk : branchKpis) {
                        branchTable.addCell(createCell(bk.getBranchName(), Element.ALIGN_LEFT));
                        branchTable.addCell(createCell(nf.format(bk.getRevenue()) + " ₫", Element.ALIGN_RIGHT));
                        branchTable.addCell(createCell(String.format("%.1f%%", bk.getRevenuePercent()), Element.ALIGN_RIGHT));
                    }
                    document.add(branchTable);
                }

                document.add(new Paragraph(" "));
                addHorizontalRule(document);
                document.add(new Paragraph(" "));
            }

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 2.5f, 2f, 2f, 2.5f, 2.5f, 2f, 2f, 2.5f});
            table.setHeaderRows(1);

            String[] headers = {"#", "Mã đơn", "Chi nhánh", "Nhân viên", "Khách hàng", "Tổng tiền", "Phương thức", "Trạng thái", "Ngày tạo"};
            Color headerBg = new Color(0x1a, 0x1a, 0x2e);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER_FONT));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setGroupingUsed(true);
            nf.setMaximumFractionDigits(0);

            Color altColor = new Color(0xF5, 0xF5, 0xFA);

            if (rows == null || rows.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Không tìm thấy đơn hàng nào.", TABLE_CELL_FONT));
                emptyCell.setColspan(9);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(12);
                table.addCell(emptyCell);
            } else {
                int idx = 1;
                for (Order o : rows) {
                    boolean odd = (idx % 2 != 0);
                    table.addCell(colCell(String.valueOf(idx++), Element.ALIGN_CENTER, odd, altColor));
                    table.addCell(colCell(nullToDash(o.getOrderCode()), Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(nullToDash(o.getBranchName()), Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(nullToDash(o.getEmployeeName()), Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(o.getCustomerName() != null ? o.getCustomerName() : "Khách vãng lai", Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(nf.format(o.getTotalAmount()) + " ₫", Element.ALIGN_RIGHT, odd, altColor));
                    table.addCell(colCell(paymentLabel(o.getPaymentMethod()), Element.ALIGN_CENTER, odd, altColor));
                    table.addCell(colCell(o.getStatus() != null ? o.getStatus().getDisplayName() : "", Element.ALIGN_CENTER, odd, altColor));
                    table.addCell(colCell(formatCreatedAt(o.getCreatedAt()), Element.ALIGN_CENTER, odd, altColor));
                }
            }
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            try { if (document.isOpen()) document.close(); } catch (Exception ignored) {}
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateSalesTransactionReport(
            String companyName, String generatedBy, List<SalesTransaction> rows,
            SalesTransactionKpi kpi, String filterDesc) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 50, 50);
        try {
            initFonts();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new HeaderFooter());
            document.open();

            Paragraph title = new Paragraph("BÁO CÁO GIAO DỊCH & DOANH THU", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);
            document.add(new Paragraph(companyName, HEADER_FONT));
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph meta = new Paragraph("Ngày tạo: " + now + " | Người tạo: " + generatedBy, SMALL_FONT);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(6);
            document.add(meta);
            addHorizontalRule(document);
            document.add(new Paragraph(" "));

            if (filterDesc != null && !filterDesc.isEmpty()) {
                Paragraph fp = new Paragraph("Bộ lọc: " + filterDesc, SMALL_FONT);
                fp.setSpacingAfter(8);
                document.add(fp);
            }

            if (kpi != null) {
                Paragraph kpiTitle = new Paragraph("TỔNG QUAN KPI", HEADER_FONT);
                kpiTitle.setSpacingAfter(6);
                document.add(kpiTitle);
                PdfPTable kt = new PdfPTable(2);
                kt.setWidthPercentage(60);
                kt.setHorizontalAlignment(Element.ALIGN_LEFT);
                kt.setWidths(new float[]{3, 3});
                NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
                nf.setGroupingUsed(true);
                nf.setMaximumFractionDigits(0);
                addSummaryRow(kt, "Tổng số giao dịch", String.valueOf(kpi.getTotalTransactions()));
                addSummaryRow(kt, "Tổng doanh thu", nf.format(kpi.getTotalRevenue()) + " ₫");
                addSummaryRow(kt, "Tổng chi phí", nf.format(kpi.getTotalExpense()) + " ₫");
                addSummaryRow(kt, "Dòng tiền ròng", nf.format(kpi.getNetCashFlow()) + " ₫");
                addSummaryRow(kt, "Giá trị giao dịch TB", nf.format(kpi.getAvgTransactionValue()) + " ₫");
                addSummaryRow(kt, "Tổng đơn hàng", String.valueOf(kpi.getTotalSalesOrders()));
                document.add(kt);
                addHorizontalRule(document);
                document.add(new Paragraph(" "));
            }

            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 1.5f, 2.5f, 1.5f, 2, 2, 3, 2.5f, 2.5f, 1.5f});
            table.setHeaderRows(1);
            String[] headers = {"Mã phiếu", "Mã đơn", "Loại đơn", "Ngày", "Loại", "Phương thức", "Số tiền", "Mô tả", "Chi nhánh", "Nhân viên", "Trạng thái"};
            Color bg = new Color(0x1a, 0x1a, 0x2e);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER_FONT));
                cell.setBackgroundColor(bg);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setGroupingUsed(true);
            nf.setMaximumFractionDigits(0);
            Color altColor = new Color(0xF5, 0xF5, 0xFA);
            if (rows == null || rows.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Không có dữ liệu giao dịch.", TABLE_CELL_FONT));
                emptyCell.setColspan(11);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(12);
                table.addCell(emptyCell);
            } else {
                int idx = 1;
                for (SalesTransaction t : rows) {
                    boolean odd = (idx++ % 2 != 0);
                    table.addCell(colCell(nullToDash(t.getTransactionCode()), Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(nullToDash(t.getOrderCode()), Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(nullToDash(t.getOrderType()), Element.ALIGN_CENTER, odd, altColor));
                    table.addCell(colCell(t.getPaymentDate() != null ? t.getPaymentDate().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "", Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(t.getTransactionType(), Element.ALIGN_CENTER, odd, altColor));
                    table.addCell(colCell(t.getPaymentMethod(), Element.ALIGN_CENTER, odd, altColor));
                    table.addCell(colCell(nf.format(t.getAmount()) + " ₫", Element.ALIGN_RIGHT, odd, altColor));
                    table.addCell(colCell(nullToDash(t.getDescription()), Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(nullToDash(t.getBranchName()), Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(nullToDash(t.getEmployeeName()), Element.ALIGN_LEFT, odd, altColor));
                    table.addCell(colCell(nullToDash(t.getStatus()), Element.ALIGN_CENTER, odd, altColor));
                }
            }
            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            try { if (document.isOpen()) document.close(); } catch (Exception ignored) {}
            throw new RuntimeException(e);
        }
    }

    private static PdfPCell colCell(String text, int align, boolean odd, Color alt) {
        PdfPCell cell = createCell(text, align);
        if (!odd) cell.setBackgroundColor(alt);
        return cell;
    }

    private static String paymentLabel(String method) {
        if (method == null) return "—";
        return switch (method) {
            case "CASH" -> "Tiền mặt";
            case "CARD" -> "Thẻ";
            case "TRANSFER" -> "Chuyển khoản";
            default -> method;
        };
    }

    private static String formatCreatedAt(String createdAt) {
        if (createdAt == null || createdAt.length() < 16) return createdAt != null ? createdAt : "";
        try {
            LocalDateTime dt = LocalDateTime.parse(createdAt.substring(0, 16), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return createdAt;
        }
    }

    private static String getDatePresetLabel(String preset) {
        if (preset == null || preset.isEmpty()) return "";
        return switch (preset) {
            case "today" -> "Hôm nay";
            case "yesterday" -> "Hôm qua";
            case "7days" -> "7 ngày qua";
            case "30days" -> "30 ngày qua";
            case "this_month" -> "Tháng này";
            case "last_month" -> "Tháng trước";
            case "this_year" -> "Năm nay";
            case "1year" -> "1 năm qua";
            default -> preset;
        };
    }
}

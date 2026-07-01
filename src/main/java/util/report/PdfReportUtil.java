package util.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;

import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import model.EmployeeOverview;
import model.EmployeeSalesSummary;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public static byte[] generateEmployeeSalesReport(
            String companyName,
            String generatedBy,
            List<EmployeeSalesSummary> rows,
            EmployeeOverview overview,
            String keyword,
            String branchName,
            LocalDate dateFrom,
            LocalDate dateTo) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, PAGE_MARGIN, PAGE_MARGIN, 50, 50);

        try {
            initFonts();

            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new HeaderFooter());

            document.open();

            addHeader(document, companyName, generatedBy);
            addFilterInfo(document, keyword, branchName, dateFrom, dateTo);
            addEmployeeTable(document, rows);
            addSummary(document, rows, overview);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            try { if (document.isOpen()) document.close(); } catch (Exception ignored) {}
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

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

    private static void addHeader(Document document, String companyName, String generatedBy) throws DocumentException {
        Paragraph title = new Paragraph("Báo cáo doanh số nhân viên", TITLE_FONT);
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
        document.add(new Paragraph(" ")); // spacer
    }

    private static void addFilterInfo(Document document, String keyword, String branchName,
                                       LocalDate dateFrom, LocalDate dateTo) throws DocumentException {
        Paragraph filters = new Paragraph("Filters", HEADER_FONT);
        filters.setSpacingAfter(3);
        document.add(filters);

        StringBuilder sb = new StringBuilder();
        sb.append("Date Range: ");
        if (dateFrom != null) sb.append(dateFrom.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        else sb.append("Earliest");
        sb.append(" — ");
        if (dateTo != null) sb.append(dateTo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        else sb.append("Latest");

        sb.append("  |  Branch: ").append(branchName != null && !branchName.isEmpty() ? branchName : "All Branches");
        sb.append("  |  Period: ")
                .append(dateFrom != null ? dateFrom.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—")
                .append(" to ")
                .append(dateTo != null ? dateTo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");

        Paragraph filterText = new Paragraph(sb.toString(), SMALL_FONT);
        filterText.setSpacingAfter(8);
        document.add(filterText);
    }

    private static void addEmployeeTable(Document document, List<EmployeeSalesSummary> rows) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 5, 3, 3, 3, 3, 3});
        table.setHeaderRows(1);
        table.setKeepTogether(false);
        table.setSplitLate(false);

        addTableHeader(table);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setGroupingUsed(true);

        if (rows == null || rows.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No report data available.", TABLE_CELL_FONT));
            emptyCell.setColspan(7);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(12);
            table.addCell(emptyCell);
        } else {
            for (EmployeeSalesSummary row : rows) {
                table.addCell(createCell(row.getFullName(), Element.ALIGN_LEFT));
                table.addCell(createCell(nullToDash(row.getBranchName()), Element.ALIGN_LEFT));
                table.addCell(createCell(nullToDash(row.getRoleName()), Element.ALIGN_LEFT));
                table.addCell(createCell(String.valueOf(row.getTotalOrders()), Element.ALIGN_RIGHT));
                table.addCell(createCell(nf.format(row.getTotalRevenue()) + " ₫", Element.ALIGN_RIGHT));
                table.addCell(createCell(nf.format(row.getAverageOrderValue()) + " ₫", Element.ALIGN_RIGHT));
                table.addCell(createCell(String.valueOf(row.getCompletedOrders()), Element.ALIGN_RIGHT));
            }
        }

        document.add(table);
    }

    private static void addTableHeader(PdfPTable table) {
        String[] headers = {"Employee", "Branch", "Role", "Orders", "Revenue", "Avg. Order", "Completed"};
        Color bg = new Color(0x1a, 0x1a, 0x2e);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER_FONT));
            cell.setBackgroundColor(bg);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }
    }

    private static PdfPCell createCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_CELL_FONT));
        cell.setPadding(4);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(0.3f);
        cell.setBorderColor(new Color(0xcc, 0xcc, 0xcc));
        return cell;
    }

    private static void addSummary(Document document, List<EmployeeSalesSummary> rows, EmployeeOverview overview) throws DocumentException {
        if (overview == null) return;

        addHorizontalRule(document);
        document.add(new Paragraph(" "));

        Paragraph title = new Paragraph("Summary", HEADER_FONT);
        title.setSpacingAfter(4);
        document.add(title);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setGroupingUsed(true);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        summaryTable.setWidths(new float[]{3, 3});

        BigDecimal totalRevenue = overview.getTotalRevenue();
        int totalEmployees = overview.getTotalEmployees();
        BigDecimal avgRevenue = totalEmployees > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalEmployees), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        addSummaryRow(summaryTable, "Total Employees", String.valueOf(totalEmployees));
        addSummaryRow(summaryTable, "Total Orders", String.valueOf(overview.getTotalOrders()));
        addSummaryRow(summaryTable, "Total Revenue", nf.format(totalRevenue) + " ₫");
        addSummaryRow(summaryTable, "Avg Revenue / Employee", nf.format(avgRevenue) + " ₫");

        if (overview.getTopEmployeeName() != null) {
            addSummaryRow(summaryTable, "Top Employee", overview.getTopEmployeeName());
            addSummaryRow(summaryTable, "Highest Revenue", nf.format(overview.getTopEmployeeRevenue()) + " ₫");
        }
        if (overview.getLowestEmployeeName() != null) {
            addSummaryRow(summaryTable, "Lowest Employee", overview.getLowestEmployeeName());
            addSummaryRow(summaryTable, "Lowest Revenue", nf.format(overview.getLowestEmployeeRevenue()) + " ₫");
        }

        document.add(summaryTable);
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
            Phrase footer = new Phrase("Generated by Finora Retail Management System", FOOTER_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (rect.getLeft() + rect.getRight()) / 2,
                    rect.getBottom() + 12, 0);

            // Page number
            Phrase pageNum = new Phrase("Page " + writer.getPageNumber(), FOOTER_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    pageNum,
                    rect.getRight() - PAGE_MARGIN,
                    rect.getBottom() + 12, 0);
        }
    }
}

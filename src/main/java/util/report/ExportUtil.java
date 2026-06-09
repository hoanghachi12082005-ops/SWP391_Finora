package com.storemanagement.util.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExportUtil {
    private ExportUtil() {}

    public static String buildExportFileName(String prefix) {
        return prefix + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}

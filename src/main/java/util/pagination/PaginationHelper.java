package util.pagination;

import jakarta.servlet.http.HttpServletRequest;

public final class PaginationHelper {

    private static final int[] SIZE_VALUES = {10, 30, 100, 100000};

    private PaginationHelper() {}

    public static PageResult compute(int totalRecords, int page, int sizeValue) {
        PageResult r = new PageResult();
        r.totalRecords = totalRecords;

        sizeValue = validateSizeValue(sizeValue);
        r.sizeValue = sizeValue;

        int pageSize = (sizeValue >= 100000) ? (totalRecords > 0 ? totalRecords : 100000) : sizeValue;
        r.pageSize = pageSize;

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages < 1) totalPages = 1;
        r.totalPages = totalPages;

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        r.currentPage = page;

        r.startRecord = totalRecords == 0 ? 0 : (page - 1) * pageSize + 1;
        r.endRecord = Math.min(page * pageSize, totalRecords);

        r.option30 = 10;
        r.option50 = 30;
        r.option70 = 100;
        r.option100 = totalRecords;

        return r;
    }

    private static int validateSizeValue(int sizeValue) {
        for (int v : SIZE_VALUES) {
            if (v == sizeValue) return v;
        }
        return 10;
    }

    public static class PageResult {
        private int currentPage;
        private int totalPages;
        private int pageSize;
        private int sizeValue;
        private int startRecord;
        private int endRecord;
        private int totalRecords;
        private int option30;
        private int option50;
        private int option70;
        private int option100;

        public int getCurrentPage()   { return currentPage; }
        public int getTotalPages()     { return totalPages; }
        public int getPageSize()       { return pageSize; }
        public int getSizeValue()      { return sizeValue; }
        public int getStartRecord()    { return startRecord; }
        public int getEndRecord()      { return endRecord; }
        public int getTotalRecords()   { return totalRecords; }
        public int getOption30()       { return option30; }
        public int getOption50()       { return option50; }
        public int getOption70()       { return option70; }
        public int getOption100()      { return option100; }

        public void setAttributes(HttpServletRequest req) {
            req.setAttribute("currentPage",   currentPage);
            req.setAttribute("totalPages",    totalPages);
            req.setAttribute("pageSize",      pageSize);
            req.setAttribute("sizeValue",     sizeValue);
            req.setAttribute("startRecord",   startRecord);
            req.setAttribute("endRecord",     endRecord);
            req.setAttribute("totalRecords",  totalRecords);
            req.setAttribute("option30",      option30);
            req.setAttribute("option50",      option50);
            req.setAttribute("option70",      option70);
            req.setAttribute("option100",     option100);
        }
    }
}

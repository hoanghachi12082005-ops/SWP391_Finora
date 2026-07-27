import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.io.InputStream;

/**
 * Tu dong tai anh san pham tu Tiki API.
 * Luu vao assets/images/product/ va cap nhat DB.
 */
public class DownloadProductImages {

    static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=DBFinoraV3;encrypt=false;trustServerCertificate=true;";
    static final String DB_USER = "sa";
    static final String DB_PASS = "1234";
    static final String IMAGE_DIR = "src/main/webapp/assets/images/product/";
    static final String CONTEXT_PATH = "/SWP391_Finora";
    static final String IMAGE_URL_PREFIX = CONTEXT_PATH + "/assets/images/product/";

    static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    static class Product {
        int id;
        String name;
        String category;
    }

    public static void main(String[] args) throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        // 1. Lay tat ca san pham tu DB
        List<Product> products = getProductsWithoutImages();
        System.out.println("Tong so san pham can xu ly: " + products.size());

        // 2. Tao thu muc anh neu chua co
        Files.createDirectories(Paths.get(IMAGE_DIR));

        int success = 0;
        int failed = 0;

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            System.out.printf("%n[%d/%d] Xu ly: %s (ID=%d)%n", i + 1, products.size(), p.name, p.id);

            // Tim anh tu Tiki API
            String thumbnailUrl = searchOnTiki(p.name);

            if (thumbnailUrl == null) {
                System.out.println("  -> KHONG tim thay anh tu Tiki!");
                failed++;
            } else {
                // Download anh
                String fileName = downloadImage(thumbnailUrl, p.id);
                if (fileName == null) {
                    System.out.println("  -> Tai anh THAT BAI!");
                    failed++;
                } else {
                    // Cap nhat DB
                    updateImageInDB(p.id, fileName);
                    System.out.println("  -> THANH CONG: " + fileName);
                    success++;
                }
            }

            // Delay giua cac request de tranh rate limit
            if (i < products.size() - 1) {
                long delay = 2000 + (long)(Math.random() * 1000);
                System.out.println("  -> Cho " + delay + "ms...");
                Thread.sleep(delay);
            }
        }

        System.out.printf("%n=== KET QUA: Thanh cong: %d, That bai: %d ===%n", success, failed);
    }

    static List<Product> getProductsWithoutImages() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, c.category_name " +
                     "FROM product p " +
                     "LEFT JOIN category c ON p.category_id = c.category_id " +
                     "WHERE (p.ImageUrl IS NULL OR p.ImageUrl = '' OR p.ImageUrl = '[]') " +
                     "ORDER BY p.product_id ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Product p = new Product();
                p.id = rs.getInt("product_id");
                p.name = rs.getString("product_name");
                p.category = rs.getString("category_name");
                list.add(p);
            }
        }
        return list;
    }

    /** Tim kiem tren Tiki API va lay thumbnail_url */
    static String searchOnTiki(String productName) {
        try {
            // Tao query: product name
            String query = productName;

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://tiki.vn/api/v2/products?q=" + encodedQuery + "&limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            String body = response.body();
            // Parse JSON bang tay de lay thumbnail_url
            return extractThumbnailUrl(body);
        } catch (Exception e) {
            System.out.println("  -> Loi goi Tiki API: " + e.getMessage());
            return null;
        }
    }

    /** Thu tim kiem truc tiep khong qua API */
    static String searchOnTikiDirect(String productName) {
        try {
            String query = productName + " san pham";
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://tiki.vn/api/v2/products?q=" + encodedQuery + "&limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            return extractThumbnailUrl(response.body());
        } catch (Exception e) {
            return null;
        }
    }

    /** Trich xuat thumbnail_url tu JSON response cua Tiki */
    static String extractThumbnailUrl(String json) {
        if (json == null || json.isBlank()) return null;

        // Tim "thumbnail_url":"... trong JSON
        String key = "\"thumbnail_url\":\"";
        int start = json.indexOf(key);
        if (start < 0) return null;

        start += key.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;

        String url = json.substring(start, end);
        // Giai ma Unicode escapes
        url = decodeUnicode(url);
        // Xu ly escaped chars
        url = url.replace("\\/", "/");
        // Chuyen ve kich thuoc lon hon (800x800 thay vi 280x280)
        url = url.replace("/cache/280x280/", "/cache/800x800/");
        // Bo cache busting parameter
        int qmark = url.indexOf('?');
        if (qmark > 0) url = url.substring(0, qmark);

        return url;
    }

    static String decodeUnicode(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 5 < s.length() && s.charAt(i + 1) == 'u') {
                String hex = s.substring(i + 2, i + 6);
                try {
                    sb.append((char) Integer.parseInt(hex, 16));
                    i += 6;
                    continue;
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    static String downloadImage(String imageUrl, int productId) {
        try {
            String ext = guessExtension(imageUrl);
            String fileName = "product_" + productId + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;

            Path targetPath = Paths.get(IMAGE_DIR, fileName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", "https://tiki.vn/")
                    .timeout(java.time.Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                System.out.println("  -> HTTP " + response.statusCode());
                return null;
            }

            // Kiem tra content-type
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (!contentType.startsWith("image/")) {
                System.out.println("  -> Khong phai anh: " + contentType);
                return null;
            }

            try (InputStream in = response.body()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Kiem tra file tai ve co hop le khong
            long fileSize = Files.size(targetPath);
            if (fileSize < 1024) {
                Files.deleteIfExists(targetPath);
                System.out.println("  -> File qua nho: " + fileSize + " bytes");
                return null;
            }

            return fileName;
        } catch (Exception e) {
            System.out.println("  -> Loi tai anh: " + e.getMessage());
            return null;
        }
    }

    static String guessExtension(String url) {
        String lower = url.toLowerCase();
        int q = lower.indexOf('?');
        if (q > 0) lower = lower.substring(0, q);

        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpg";
        if (lower.endsWith(".png")) return "png";
        if (lower.endsWith(".webp")) return "webp";
        if (lower.endsWith(".gif")) return "gif";
        // Kiem tra content-type trong URL
        if (lower.contains("png")) return "png";
        if (lower.contains("webp")) return "webp";
        return "jpg";
    }

    static void updateImageInDB(int productId, String fileName) throws SQLException {
        String imageJson = "[\"" + IMAGE_URL_PREFIX + fileName + "\"]";

        String sql = "UPDATE product SET ImageUrl = ?, update_at = GETDATE() WHERE product_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, imageJson);
            stmt.setInt(2, productId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("  -> Da cap nhat DB cho product_id=" + productId);
            }
        }
    }
}

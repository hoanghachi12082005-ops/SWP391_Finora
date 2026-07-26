import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quet thu muc assets/images/product/ va cap nhat ImageUrl vao DB.
 * 
 * Cach dung:
 *   1. Build project -> copy file JAR nay vao server
 *   2. Chay: java SyncProductImages
 *   3. No se tu dong quet folder anh va cap nhat DB
 * 
 * Luu y: Cau hinh DB o cac bien DB_* ben duoi
 */
public class SyncProductImages {

    // ===== CAU HINH - SUA LAI CHO DB CUA BAN =====
    static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=DBFinoraV3;encrypt=false;trustServerCertificate=true;";
    static final String DB_USER = "sa";
    static final String DB_PASS = "your_password";
    static final String CONTEXT_PATH = "/SWP391_Finora";
    static final String IMAGE_DIR = "assets/images/product/"; // relative path

    public static void main(String[] args) throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        // 1. Tim tat ca file anh trong thu muc
        Path imagePath = Paths.get(IMAGE_DIR);
        if (!Files.exists(imagePath)) {
            System.out.println("Khong tim thay thu muc: " + Paths.get("").toAbsolutePath() + "/" + IMAGE_DIR);
            return;
        }
        
        // Pattern: product_{ID}_{uuid}.{ext}
        Pattern pattern = Pattern.compile("product_(\\d+)_.+\\.\\w+$");
        
        Map<Integer, List<String>> productImages = new HashMap<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(imagePath, "product_*")) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                Matcher m = pattern.matcher(fileName);
                if (m.find()) {
                    int productId = Integer.parseInt(m.group(1));
                    String imageUrl = CONTEXT_PATH + "/" + IMAGE_DIR + fileName;
                    productImages.computeIfAbsent(productId, k -> new ArrayList<>()).add(imageUrl);
                }
            }
        }
        
        System.out.println("Tim thay " + productImages.size() + " san pham co anh trong thu muc.");
        
        // 2. Cap nhat vao DB
        String updateSql = "UPDATE product SET ImageUrl = ?, update_at = GETDATE() WHERE product_id = ?";
        int updated = 0;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            
            for (Map.Entry<Integer, List<String>> entry : productImages.entrySet()) {
                int productId = entry.getKey();
                List<String> urls = entry.getValue();
                
                // Tao JSON array: ["url1","url2",...]
                String jsonArray = toJsonArray(urls);
                
                stmt.setString(1, jsonArray);
                stmt.setInt(2, productId);
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("  -> product_id=" + productId + ": " + jsonArray);
                    updated++;
                } else {
                    System.out.println("  -> product_id=" + productId + ": KHONG tim thay san pham trong DB!");
                }
            }
        }
        
        System.out.println("Hoan tat! Da cap nhat " + updated + " san pham.");
    }
    
    static String toJsonArray(List<String> urls) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < urls.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(urls.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
}

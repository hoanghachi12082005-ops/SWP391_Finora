package util.branch;

import model.Branch;

import jakarta.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import javax.imageio.ImageIO;

import java.util.HashMap;
import java.util.Map;

/**
 * Kiểm tra dữ liệu chi nhánh (thêm / sửa).
 */
public final class BranchValidator {

    @FunctionalInterface
    public interface CodeDuplicateChecker {
        boolean isCodeDuplicate(String code, int excludeBranchId);
    }

    private BranchValidator() {
    }

    public static Map<String, String> validateForInsert(Branch b, CodeDuplicateChecker duplicateChecker) {
        return validate(b, 0, duplicateChecker, false);
    }

    public static Map<String, String> validateForUpdate(Branch b, int branchId, CodeDuplicateChecker duplicateChecker) {
        return validate(b, branchId, duplicateChecker, true);
    }
    
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".webp"
    );

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    public static String validateImage(Part imagePart) {

        if (imagePart == null || imagePart.getSize() == 0) {
            return null; // Không chọn ảnh cũng không báo lỗi
        }

        if (imagePart.getSize() > MAX_IMAGE_SIZE) {
            return "Kích thước ảnh không được vượt quá 5MB.";
        }

        String fileName = imagePart.getSubmittedFileName();

        if (fileName == null || fileName.isBlank()) {
            return "Tên file không hợp lệ.";
        }

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            return "File phải có phần mở rộng.";
        }

        String extension = fileName.substring(dotIndex).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return "Chỉ chấp nhận JPG, JPEG, PNG, GIF hoặc WEBP.";
        }

        String contentType = imagePart.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            return "Định dạng hình ảnh không hợp lệ.";
        }

        try {
            BufferedImage image = ImageIO.read(imagePart.getInputStream());

            if (image == null) {
                return "File tải lên không phải hình ảnh hợp lệ.";
            }

        } catch (IOException ex) {
            return "Không thể đọc file hình ảnh.";
        }

        return null;
    }
    
    static Map<String, String> validate(Branch b, int excludeBranchId, 
                                        CodeDuplicateChecker duplicateChecker, boolean isUpdate) {
        Map<String, String> errors = new HashMap<>();
        
        if (isUpdate && excludeBranchId <= 0) {
            errors.put("id", "Không xác định được chi nhánh cần cập nhật.");
        }

        if (b.getBranchName() == null || b.getBranchName().isBlank()) {
            errors.put("branchName", "Tên cửa hàng không được để trống.");
        }

        if (b.getBranchCode() == null || b.getBranchCode().isBlank()) {
            errors.put("branchCode", "Mã cửa hàng không được để trống.");
            
        } else if (!b.getBranchCode().matches("BR-\\d{3,}")) {
            errors.put("branchCode", "Mã cửa hàng phải có dạng BR-001.");
            
        } else if (duplicateChecker != null
                && duplicateChecker.isCodeDuplicate(b.getBranchCode(), excludeBranchId)) {
            errors.put("branchCode", "Mã cửa hàng đã tồn tại.");
        }

        if (b.getCity() == null || b.getCity().isBlank() || "Chọn tỉnh thành".equals(b.getCity())){
            errors.put("city", "Vui lòng chọn Tỉnh/Thành phố!!");
        }
        
        if (b.getDistrict() == null || b.getDistrict().isBlank() || "Chọn quận huyện".equals(b.getDistrict())){
            errors.put ("district", "Vui lòng chọn Quận/Huyện!!");
        }
        
        if (b.getOpeningTime() != null && !b.getOpeningTime().isBlank()
                && !b.getOpeningTime().matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            errors.put("openingTime", "Giờ mở cửa không hợp lệ (HH:mm).");
        }

        if (b.getClosingTime() != null && !b.getClosingTime().isBlank()
                && !b.getClosingTime().matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            errors.put("closingTime", "Giờ đóng cửa không hợp lệ (HH:mm).");
        }

        return errors;
    }
}

package util.branch;

import model.Branch;

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

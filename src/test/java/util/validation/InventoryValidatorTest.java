package util.validation;

import model.Employee;
import model.StockTransferDetail;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryValidatorTest {

    @Test
    public void testStaffApprovalBlocked() {
        Employee staff = new Employee();
        staff.setRoleName("Staff");

        ValidationResult res = InventoryValidator.validateStaffApprovalPermission(staff, "approveCheck");
        assertFalse(res.isValid(), "Staff should be blocked from approveCheck");
        assertTrue(res.getFirstError().contains("Cảnh báo bảo mật"), "Error message should mention security warning");

        ValidationResult res2 = InventoryValidator.validateStaffApprovalPermission(staff, "confirmSend");
        assertFalse(res2.isValid(), "Staff should be blocked from confirmSend");
    }

    @Test
    public void testManagerApprovalAllowed() {
        Employee manager = new Employee();
        manager.setRoleName("StoreManager");

        ValidationResult res = InventoryValidator.validateStaffApprovalPermission(manager, "approveCheck");
        assertTrue(res.isValid(), "Manager should be allowed to approve check");
    }

    @Test
    public void testImportValidationNegativeQty() {
        String[] pIds = {"1"};
        String[] qtys = {"-5"};
        String[] sIds = {"2"};
        String[] prices = {"10000"};

        ValidationResult res = InventoryValidator.validateImportRequest(1, pIds, qtys, sIds, prices, "Note");
        assertFalse(res.isValid(), "Import request with negative quantity should fail");
        assertTrue(res.getFirstError().contains("số nguyên dương"), "Error message should mention positive integer");
    }

    @Test
    public void testImportValidationDuplicateProduct() {
        String[] pIds = {"10", "10"};
        String[] qtys = {"5", "3"};
        String[] sIds = {"1", "1"};
        String[] prices = {"1000", "1000"};

        ValidationResult res = InventoryValidator.validateImportRequest(1, pIds, qtys, sIds, prices, "Note");
        assertFalse(res.isValid(), "Duplicate product in same import slip should fail");
        assertTrue(res.getFirstError().contains("lặp lại"), "Error should mention duplicate product");
    }

    @Test
    public void testTransferCreationSameWarehouse() {
        String[] pIds = {"1"};
        String[] partnerWIds = {"1"}; // Same as fromWarehouseId
        String[] qtys = {"5"};

        ValidationResult res = InventoryValidator.validateTransferCreation(1, pIds, partnerWIds, qtys, "Note", null);
        assertFalse(res.isValid(), "Transfer to same warehouse should fail");
        assertTrue(res.getFirstError().contains("không được trùng"), "Error should state warehouses cannot match");
    }

    @Test
    public void testCheckValidationNegativeActualQty() {
        String[] pIds = {"1"};
        String[] actualQtys = {"-10"};
        String[] notes = {"Note"};

        ValidationResult res = InventoryValidator.validateCheckRequest(1, pIds, actualQtys, notes);
        assertFalse(res.isValid(), "Check with negative actual qty should fail");
        assertTrue(res.getFirstError().contains("số nguyên không âm"), "Error should state non-negative integer required");
    }
}

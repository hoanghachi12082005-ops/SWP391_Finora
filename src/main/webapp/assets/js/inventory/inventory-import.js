/**
 * Inventory Import Screen Logic
 */

document.addEventListener("DOMContentLoaded", function() {
    filterProducts();

    // Attach listeners
    const supplierSelect = document.getElementById("supplierSelect");
    if (supplierSelect) {
        supplierSelect.addEventListener("change", filterProducts);
    }

    const showAllProducts = document.getElementById("showAllProducts");
    if (showAllProducts) {
        showAllProducts.addEventListener("change", filterProducts);
    }

    const importForm = document.getElementById("importForm");
    if (importForm) {
        importForm.addEventListener("submit", function(e) {
            const checkboxes = document.querySelectorAll(".check-import:checked");
            if (checkboxes.length === 0) {
                e.preventDefault();
                alert("Vui lòng tích chọn ít nhất 1 sản phẩm để nhập hàng!");
                return;
            }
            
            if (!confirm("Xác nhận hoàn tất phiếu nhập hàng?")) {
                e.preventDefault();
            }
        });
    }
});

// Getter helper for supplierProducts map
const getSupplierProductsMap = () => window.IMPORT_CONFIG?.supplierProducts || {};

/**
 * Lọc sản phẩm cục bộ dựa trên Nhà cung cấp được chọn
 */
function filterProducts() {
    const supplierSelect = document.getElementById("supplierSelect");
    if (!supplierSelect) return;
    const supplierId = supplierSelect.value;
    
    const showAllCheckbox = document.getElementById("showAllProducts");
    const showAll = showAllCheckbox ? showAllCheckbox.checked : false;
    
    const rows = document.querySelectorAll(".product-row");
    const supplierProducts = getSupplierProductsMap();
    
    let visibleCount = 0;
    
    rows.forEach(row => {
        const productId = row.getAttribute("data-product-id");
        const checkbox = row.querySelector(".check-import");
        const importPriceInput = row.querySelector(".import-price-input");
        
        let isVisible = false;
        let preNegotiatedPrice = null;
        
        if (showAll || !supplierId) {
            // Nếu chưa chọn nhà cung cấp hoặc bật chế độ xem tất cả, cho hiển thị hết
            isVisible = true;
            
            // Nếu có liên kết với nhà cung cấp hiện tại, vẫn lấy giá đàm phán
            if (supplierId && supplierProducts[supplierId] && supplierProducts[supplierId][productId] !== undefined) {
                preNegotiatedPrice = supplierProducts[supplierId][productId];
            }
        } else {
            // Lọc sản phẩm thuộc nhà cung cấp này
            if (supplierProducts[supplierId] && supplierProducts[supplierId][productId] !== undefined) {
                isVisible = true;
                preNegotiatedPrice = supplierProducts[supplierId][productId];
            }
        }
        
        if (isVisible) {
            row.classList.remove("d-none");
            visibleCount++;
            
            // Nếu sản phẩm được hiển thị và có giá đàm phán, tự điền sẵn vào ô giá nhập
            if (preNegotiatedPrice !== null && !checkbox.checked) {
                importPriceInput.value = Math.round(preNegotiatedPrice);
            } else if (!checkbox.checked) {
                // Nếu không có giá đàm phán và chưa được tick, đặt mặc định bằng 70% giá bán lẻ
                const sellingPrice = parseFloat(row.getAttribute("data-selling-price"));
                importPriceInput.value = Math.round(sellingPrice * 0.7);
            }
        } else {
            row.classList.add("d-none");
            // Nếu bị ẩn đi, bỏ tick checkbox
            if (checkbox.checked) {
                checkbox.checked = false;
                toggleProductRow(productId);
            }
        }
    });
    
    const countBadge = document.getElementById("productCountBadge");
    if (countBadge) countBadge.innerText = visibleCount + " sản phẩm";
    calculateGrandTotal();
}

/**
 * Bật/Tắt hoạt động dòng sản phẩm khi được tick chọn nhập hàng
 */
function toggleProductRow(productId) {
    const row = document.getElementById("row_" + productId);
    const checkbox = document.getElementById("check_" + productId);
    const priceInput = document.getElementById("importPrice_" + productId);
    const qtyInput = document.getElementById("quantity_" + productId);
    
    if (!row || !checkbox || !priceInput || !qtyInput) return;

    if (checkbox.checked) {
        row.classList.add("import-row-active");
        priceInput.disabled = false;
        qtyInput.disabled = false;
        qtyInput.focus();
    } else {
        row.classList.remove("import-row-active");
        priceInput.disabled = true;
        qtyInput.disabled = true;
    }
    
    calculateRow(productId);
}

/**
 * Tính toán thành tiền của một dòng sản phẩm
 */
function calculateRow(productId) {
    const checkbox = document.getElementById("check_" + productId);
    const priceInput = document.getElementById("importPrice_" + productId);
    const qtyInput = document.getElementById("quantity_" + productId);
    const subtotalTd = document.getElementById("subtotal_" + productId);
    
    if (!checkbox || !priceInput || !qtyInput || !subtotalTd) return;

    if (!checkbox.checked) {
        subtotalTd.innerText = "0 đ";
        calculateGrandTotal();
        return;
    }
    
    const price = parseFloat(priceInput.value) || 0;
    const qty = parseInt(qtyInput.value) || 0;
    const subtotal = price * qty;
    
    subtotalTd.innerText = formatCurrency(subtotal);
    calculateGrandTotal();
}

/**
 * Tính toán tổng giá trị phiếu nhập
 */
function calculateGrandTotal() {
    const checkboxes = document.querySelectorAll(".check-import:checked");
    let grandTotal = 0;
    let totalQty = 0;
    let selectedCount = checkboxes.length;
    
    checkboxes.forEach(checkbox => {
        const productId = checkbox.value;
        const priceInput = document.getElementById("importPrice_" + productId);
        const qtyInput = document.getElementById("quantity_" + productId);
        
        if (priceInput && qtyInput) {
            const price = parseFloat(priceInput.value) || 0;
            const qty = parseInt(qtyInput.value) || 0;
            
            grandTotal += price * qty;
            totalQty += qty;
        }
    });
    
    const grandTotalDisplay = document.getElementById("grandTotalDisplay");
    const selectedCountDisplay = document.getElementById("selectedCountDisplay");
    const totalQtyDisplay = document.getElementById("totalQtyDisplay");

    if (grandTotalDisplay) grandTotalDisplay.innerText = formatCurrency(grandTotal);
    if (selectedCountDisplay) selectedCountDisplay.innerText = selectedCount;
    if (totalQtyDisplay) totalQtyDisplay.innerText = totalQty;
}

function formatCurrency(amount) {
    return amount.toLocaleString('vi-VN') + " đ";
}

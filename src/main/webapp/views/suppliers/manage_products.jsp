<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Liên kết sản phẩm nhà cung cấp"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="../common/sidebar.jsp"/>

    <main class="main-content">
        <div class="container-fluid py-4">
            
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold text-dark">Sản Phẩm Cung Cấp</h2>
                    <small class="text-muted">
                        Quản lý danh mục sản phẩm và giá nhập đàm phán của nhà cung cấp: <strong>${supplier.name}</strong>
                    </small>
                </div>
                <div>
                    <a href="suppliers" class="btn btn-outline-secondary" style="border-radius: 8px;">
                        <span class="material-icons align-middle" style="font-size: 1.1rem; margin-right: 4px;">arrow_back</span> Quay lại
                    </a>
                </div>
            </div>

            <div class="card shadow-sm border-0" style="border-radius: 12px;">
                <div class="card-body p-4">
                    
                    <!-- Section: Add Product Form (Local UI) -->
                    <div class="row g-2 mb-4 align-items-end p-3 bg-light rounded-3 border border-light-subtle">
                        <div class="col-md-6">
                            <label class="form-label small fw-bold text-muted mb-1">Thêm sản phẩm mới</label>
                            <select id="addProductSelect" class="form-select form-select-sm" style="border-radius: 8px; height: 38px;">
                                <option value="">-- Chọn sản phẩm --</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small fw-bold text-muted mb-1">Giá nhập</label>
                            <div class="input-group input-group-sm">
                                <input type="number" id="addProductPrice" class="form-control text-end fw-bold" value="0" min="0" step="1" style="border-top-left-radius: 8px; border-bottom-left-radius: 8px; height: 38px;">
                                <span class="input-group-text small text-muted" style="border-top-right-radius: 8px; border-bottom-right-radius: 8px;">đ</span>
                            </div>
                        </div>
                        <div class="col-md-2">
                            <button type="button" class="btn btn-danger w-100 fw-semibold text-white" id="btnAddLocalProduct" style="border-radius: 8px; height: 38px;">Thêm</button>
                        </div>
                    </div>

                    <!-- Section: Main Form Submission -->
                    <form action="suppliers" method="post" id="supplierProductsForm">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="save-products">
                        <input type="hidden" name="id" value="${supplier.supplierID}">

                        <div class="table-responsive" style="border-radius: 8px; border: 1px solid #e5e7eb;">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr style="font-size: 13px; text-transform: uppercase; font-weight: 700;">
                                        <th width="120" class="text-center">Mã SP</th>
                                        <th>Tên sản phẩm</th>
                                        <th width="220" class="text-end">Giá Nhập Đàm Phán</th>
                                        <th width="100" class="text-center">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody id="productsTableBody" style="font-size: 14px;">
                                    <!-- Dynamic rows will be inserted here -->
                                </tbody>
                            </table>
                        </div>

                        <div class="d-flex justify-content-end gap-2 mt-4">
                            <a href="suppliers" class="btn btn-outline-secondary px-4" style="border-radius: 8px;">Hủy</a>
                            <button type="submit" class="btn btn-danger px-4" style="border-radius: 8px;">Lưu liên kết sản phẩm</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>

<script>
    // Raw lists rendered from Server side
    const allProducts = [
        <c:forEach var="p" items="${allProducts}" varStatus="status">
            {
                productId: ${p.productID},
                productName: "${fn:escapeXml(p.name)}",
                sellingPrice: ${p.sellingPrice != null ? p.sellingPrice : 0}
            }<c:if test="${not status.last}">,</c:if>
        </c:forEach>
    ];

    // Map of currently linked products
    const linkedProducts = {
        <c:forEach var="entry" items="${linkedProducts}" varStatus="status">
            "${entry.key}": ${entry.value}<c:if test="${not status.last}">,</c:if>
        </c:forEach>
    };

    // Tracks current state of products linked locally
    let currentLinked = [];

    document.addEventListener("DOMContentLoaded", function() {
        // Initialize currentLinked state from linkedProducts map
        allProducts.forEach(p => {
            if (linkedProducts[p.productId.toString()] !== undefined) {
                currentLinked.push({
                    productId: p.productId,
                    productName: p.productName,
                    importPrice: linkedProducts[p.productId.toString()]
                });
            }
        });

        // Initial render
        renderAll();

        // Setup dropdown default change helper
        const select = document.getElementById('addProductSelect');
        const priceInput = document.getElementById('addProductPrice');
        if (select && priceInput) {
            select.onchange = () => {
                const opt = select.options[select.selectedIndex];
                if (opt && opt.value !== "") {
                    const productId = parseInt(opt.value);
                    const prod = allProducts.find(p => p.productId === productId);
                    if (prod) {
                        priceInput.value = Math.round(prod.sellingPrice * 0.7);
                    }
                } else {
                    priceInput.value = '0';
                }
            };
        }

        // Add Product button handler
        const btnAdd = document.getElementById('btnAddLocalProduct');
        if (btnAdd) {
            btnAdd.onclick = () => {
                const selectEl = document.getElementById('addProductSelect');
                const priceInputEl = document.getElementById('addProductPrice');
                
                const productId = parseInt(selectEl.value);
                const price = parseFloat(priceInputEl.value);
                
                if (!productId) {
                    alert('Vui lòng chọn sản phẩm!');
                    return;
                }
                if (isNaN(price) || price < 0) {
                    alert('Giá nhập không hợp lệ!');
                    return;
                }
                
                const prod = allProducts.find(p => p.productId === productId);
                if (prod) {
                    currentLinked.push({
                        productId: prod.productId,
                        productName: prod.productName,
                        importPrice: price
                    });
                    
                    // Reset inputs
                    selectEl.value = '';
                    priceInputEl.value = '0';
                    
                    // Rerender UI
                    renderAll();
                }
            };
        }
    });

    function removeLinkedProduct(productId) {
        currentLinked = currentLinked.filter(item => item.productId !== productId);
        renderAll();
    }

    function renderAll() {
        renderTable();
        renderDropdown();
    }

    function renderTable() {
        const tbody = document.getElementById('productsTableBody');
        tbody.innerHTML = '';
        
        if (currentLinked.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">Chưa có sản phẩm nào được liên kết. Chọn sản phẩm phía trên để thêm.</td></tr>';
            return;
        }

        // Sort by product name
        currentLinked.sort((a, b) => a.productName.localeCompare(b.productName));

        currentLinked.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="text-center fw-semibold">
                    SP\${item.productId}
                    <input type="hidden" name="productIds" value="\${item.productId}">
                </td>
                <td class="text-start">\${item.productName}</td>
                <td class="text-end">
                    <div class="input-group input-group-sm ms-auto" style="width: 160px;">
                        <input type="number" name="price_\${item.productId}" class="form-control text-end fw-bold" value="\${item.importPrice}" min="0" step="1" style="border-top-left-radius: 8px; border-bottom-left-radius: 8px;" required>
                        <span class="input-group-text text-muted small" style="border-top-right-radius: 8px; border-bottom-right-radius: 8px;">đ</span>
                    </div>
                </td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-1" title="Xóa sản phẩm" style="width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center;" onclick="removeLinkedProduct(\${item.productId})">
                        <span class="material-icons" style="font-size: 18px;">delete</span>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    function renderDropdown() {
        const select = document.getElementById('addProductSelect');
        select.innerHTML = '<option value="">-- Chọn sản phẩm --</option>';
        
        // Find all active products that are NOT currently linked
        const linkedIds = currentLinked.map(item => item.productId);
        
        allProducts.forEach(p => {
            if (!linkedIds.includes(p.productId)) {
                const opt = document.createElement('option');
                opt.value = p.productId;
                opt.innerText = `SP\${p.productId} - \${p.productName}`;
                select.appendChild(opt);
            }
        });
    }
</script>

<jsp:include page="../common/footer.jsp" />

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : 'Nhân viên'}" />

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Danh sách nhà cung cấp"/>
</jsp:include>
<div class="app-container">

    <jsp:include page="../common/sidebar.jsp"/>

    <main class="main-content">

        <div class="container-fluid py-4">

            <!-- Alert Messages -->
            <c:if test="${not empty sessionScope.message and empty sessionScope.modalAction}">
                <div class="alert alert-${sessionScope.messageType} alert-dismissible fade show" role="alert">
                    ${sessionScope.message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>

            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold">Danh sách Nhà cung cấp</h2>
                    <small class="text-muted">
                        Quản lý toàn bộ nhà cung cấp trong hệ thống
                    </small>
                </div>

                <div>
                    <a href="#" class="btn btn-outline-danger me-2">
                        <span class="material-icons" style="font-size: 1rem; vertical-align: middle;">file_download</span> Xuất file
                    </a>

                    <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
                        <button type="button" class="btn btn-danger" onclick="openSupplierModal('create')">+ Thêm nhà cung cấp</button>
                    </c:if>
                </div>
            </div>

            <!-- Statistic Cards -->

            <div class="row mb-4">

                <div class="col-md-4">
                    <div class="card shadow-sm border-0">
                        <div class="card-body">
                            <small class="text-muted">
                                TỔNG NHÀ CUNG CẤP
                            </small>

                            <h2 class="fw-bold text-danger">${totalSupplier}</h2>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="card shadow-sm border-0">
                        <div class="card-body">

                            <small class="text-muted">
                                ĐANG HOẠT ĐỘNG
                            </small>

                            <h2 class="fw-bold text-success"> ${activeCount} </h2>

                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="card shadow-sm border-0">
                        <div class="card-body">

                            <small class="text-muted">
                                NGƯNG HOẠT ĐỘNG
                            </small>

                            <h2 class="fw-bold text-secondary">${inactiveCount} </h2>
                        </div>
                    </div>
                </div>

            </div>

            <!-- Search -->

            <div class="card shadow-sm border-0">

                <div class="card-body">

                    <form method="get" action="suppliers">

                        <div class="row">

                            <div class="col-md-6">
                                <input type="text" name="keyword" value="${keyword}" class="form-control" placeholder="Nhập NCC muốn tìm...">
                            </div>

                            <div class="col-md-3">
                                <select name="status" class="form-select" onchange="this.form.submit()">
                                    <option value="" ${empty status ? 'selected' : ''}>
                                        Tất cả trạng thái
                                    </option>
                                    <option value="active" ${status eq 'active' ? 'selected' : ''}>
                                        Active
                                    </option>
                                    <option value="inactive" ${status eq 'inactive' ? 'selected' : ''}>
                                        Inactive
                                    </option>
                                </select>
                            </div>

                            <div class="col-md-3">
                                <button class="btn btn-danger w-100"> Tìm kiếm </button>
                            </div>

                        </div>

                    </form>

                    <hr>

                    <table class="table align-middle">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Tên Nhà cung cấp</th>
                                <th>Điện thoại</th>
                                <th>Địa chỉ</th>
                                <th>Trạng thái</th>
                                <th width="280">Thao tác</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach items="${list}" var="s">
                                <tr>
                                    <td>
                                        ${s.supplierID}
                                    </td>
                                    <td>
                                        <strong>${s.name}</strong>
                                    </td>
                                    <td>
                                        ${s.phone}
                                    </td>
                                    <td>
                                        ${s.address}
                                    </td>
                                    <td>
                                         <c:choose>
                                             <c:when test="${fn:toUpperCase(s.status) eq 'ACTIVE'}">
                                                 <span class="status-badge active">Hoạt động</span>
                                             </c:when>
                                             <c:otherwise>
                                                 <span class="status-badge locked">Ngưng hoạt động</span>
                                             </c:otherwise>
                                         </c:choose>
                                    </td>

                                    <td>
                                        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
                                            <button type="button" 
                                                    class="btn btn-sm btn-warning btn-edit-supplier" 
                                                    data-id="${s.supplierID}"
                                                    data-name="<c:out value='${s.name}'/>"
                                                    data-phone="<c:out value='${s.phone}'/>"
                                                    data-address="<c:out value='${s.address}'/>"
                                                    data-status="<c:out value='${s.status}'/>">
                                                Sửa
                                            </button>
                                        </c:if>

                                        <button type="button" 
                                                class="btn btn-sm btn-outline-info" 
                                                onclick="showSupplierProducts(${s.supplierID}, '<c:out value="${s.name}"/>')">
                                            Sản phẩm
                                        </button>

                                        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
                                            <a href="suppliers?action=delete&id=${s.supplierID}&page=${page}&keyword=${keyword}" class="btn btn-sm btn-danger" onclick="return confirm('Xóa nhà cung cấp này?')"> Xóa</a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>

                        </tbody>

                    </table>

                    <!-- Pagination -->
                    <jsp:include page="/views/common/pagination.jsp">
                        <jsp:param name="baseUrl" value="suppliers"/>
                        <jsp:param name="queryString" value="&keyword=${empty keyword ? '' : keyword}&status=${empty status ? '' : status}"/>
                    </jsp:include>

                </div>

            </div>
    </main>
</div>

<!-- Modal Thêm/Sửa nhà cung cấp -->
<div class="modal fade" id="supplierModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header d-flex flex-column align-items-start">
        <div class="d-flex justify-content-between w-100 align-items-center">
            <h5 class="modal-title" id="modal-title">Thêm nhà cung cấp mới</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" onclick="closeSupplierModal()"></button>
        </div>
        <c:if test="${not empty sessionScope.modalAction and sessionScope.messageType eq 'danger'}">
            <div class="alert alert-danger w-100 py-2 px-3 mt-2 mb-0" role="alert" style="font-size: 14px;">
                <span class="material-icons me-1" style="font-size: 1.2rem; vertical-align: middle;">info</span> ${sessionScope.message}
            </div>
        </c:if>
      </div>
      <div class="modal-body">
        <form action="suppliers" method="post" id="supplier-form">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" id="modal-action" value="create">
            <input type="hidden" name="id" id="modal-id">

            <div class="mb-3">
                <label class="form-label">Tên nhà cung cấp</label>
                <input type="text" id="modal-name" name="name" class="form-control" required placeholder="VD: Công ty A">
            </div>
            <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" id="modal-phone" name="phone" class="form-control" required placeholder="VD: 0987654321">
            </div>
            <div class="mb-3">
                <label class="form-label">Địa chỉ</label>
                <textarea id="modal-address" name="address" class="form-control" rows="3" placeholder="Nhập địa chỉ..."></textarea>
            </div>
            <div class="mb-3">
                <label class="form-label">Trạng thái</label>
                <select id="modal-status" name="status" class="form-select" required>
                    <option value="active">Active</option>
                    <option value="inactive">Inactive</option>
                </select>
            </div>
            <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal" onclick="closeSupplierModal()">Huỷ</button>
                <button type="submit" class="btn btn-danger" id="modal-submit">Lưu nhà cung cấp</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script>
    const userRole = '${roleName}';
    const canEditSuppliers = (userRole === 'Admin' || userRole === 'Owner' || userRole === 'StoreManager');
    let bsModal;
    document.addEventListener("DOMContentLoaded", function() {
        if (typeof bootstrap !== 'undefined') {
            bsModal = new bootstrap.Modal(document.getElementById('supplierModal'));

            // Tự động hiển thị lại modal nếu xảy ra lỗi trùng lặp khi thêm mới từ Server
            <c:if test="${not empty sessionScope.modalAction}">
                openSupplierModal(
                    '${sessionScope.modalAction}',
                    '${sessionScope.modalId}',
                    '<c:out value="${sessionScope.modalName}"/>',
                    '<c:out value="${sessionScope.modalPhone}"/>',
                    '<c:out value="${sessionScope.modalAddress}"/>',
                    '${sessionScope.modalStatus}'
                );
                // Xóa dữ liệu tạm trong session ngay sau khi hiển thị
                <c:remove var="modalAction" scope="session"/>
                <c:remove var="modalId" scope="session"/>
                <c:remove var="modalName" scope="session"/>
                <c:remove var="modalPhone" scope="session"/>
                <c:remove var="modalAddress" scope="session"/>
                <c:remove var="modalStatus" scope="session"/>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>
        } else {
            console.warn("Bootstrap JS is not loaded!");
        }
    });

    function openSupplierModal(action, id, name, phone, address, status) {
        document.getElementById('modal-action').value = action;
        if (action === 'edit') {
            document.getElementById('modal-title').innerText = 'Chỉnh sửa nhà cung cấp';
            document.getElementById('modal-submit').innerText = 'Cập nhật';
            document.getElementById('modal-id').value = id;
            document.getElementById('modal-name').value = name;
            document.getElementById('modal-phone').value = phone;
            document.getElementById('modal-address').value = address;
            
            let normStatus = "active";
            if (status) {
                let s = status.trim().toLowerCase();
                if (s === "inactive") {
                    normStatus = "inactive";
                }
            }
            document.getElementById('modal-status').value = normStatus;
        } else {
            document.getElementById('modal-title').innerText = 'Thêm nhà cung cấp mới';
            document.getElementById('modal-submit').innerText = 'Lưu nhà cung cấp';
            document.getElementById('modal-id').value = '';
            
            if (name || phone || address || status) {
                document.getElementById('modal-name').value = name || '';
                document.getElementById('modal-phone').value = phone || '';
                document.getElementById('modal-address').value = address || '';
                
                let normStatus = "active";
                if (status) {
                    let s = status.trim().toLowerCase();
                    if (s === "inactive") {
                        normStatus = "inactive";
                    }
                }
                document.getElementById('modal-status').value = normStatus;
            } else {
                document.getElementById('supplier-form').reset();
                document.getElementById('modal-status').value = 'active';
            }
        }
        if(bsModal) bsModal.show();
    }

    function closeSupplierModal() { 
        if(bsModal) bsModal.hide(); 
    }

    document.addEventListener("click", function(event) {
        let btn = event.target.closest(".btn-edit-supplier");
        if (btn) {
            const id = btn.getAttribute("data-id");
            const name = btn.getAttribute("data-name");
            const phone = btn.getAttribute("data-phone");
            const address = btn.getAttribute("data-address");
            const status = btn.getAttribute("data-status");
            
            openSupplierModal('edit', id, name, phone, address, status);
        }
    });

    let currentSupplierId = null;
    let currentSupplierName = '';
    let allProducts = [];

    function showSupplierProducts(supplierId, supplierName) {
        currentSupplierId = supplierId;
        currentSupplierName = supplierName;
        
        document.getElementById('spModalTitle').innerText = 'Sản phẩm của: ' + supplierName;
        const tbody = document.getElementById('spModalTableBody');
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-3">Đang tải dữ liệu...</td></tr>';
        
        const myModal = new bootstrap.Modal(document.getElementById('supplierProductsModal'));
        myModal.show();
        
        fetch('suppliers?action=get-products-api&id=' + supplierId)
            .then(res => res.json())
            .then(data => {
                tbody.innerHTML = '';
                const linkedProductIds = [];
                if (!data || data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">Nhà cung cấp này chưa từng liên kết sản phẩm nào.</td></tr>';
                } else {
                    data.forEach(item => {
                        linkedProductIds.push(item.productId);
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td class="text-center fw-semibold">SP\${item.productId}</td>
                            <td class="text-start">\${item.productName}</td>
                            <td class="text-end">
                                <div class="input-group input-group-sm ms-auto" style="width: 160px;">
                                    <input type="number" class="form-control text-end fw-bold i-row-price" value="\${item.importPrice}" min="0" step="1000" style="border-top-left-radius: 8px; border-bottom-left-radius: 8px;" \${canEditSuppliers ? '' : 'disabled'}>
                                    <span class="input-group-text text-muted small" style="border-top-right-radius: 8px; border-bottom-right-radius: 8px;">đ</span>
                                </div>
                            </td>
                            \${canEditSuppliers ? `
                            <td class="text-center">
                                <button type="button" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-1 i-row-delete-btn" title="Xóa sản phẩm" style="width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center;">
                                    <span class="material-icons" style="font-size: 18px;">delete</span>
                                </button>
                            </td>
                            ` : ''}
                        `;
                        
                        if (canEditSuppliers) {
                            const priceInput = tr.querySelector('.i-row-price');
                            if (priceInput) {
                                priceInput.onchange = () => {
                                    const newPrice = parseFloat(priceInput.value);
                                    updateSupplierProductPrice(supplierId, item.productId, newPrice);
                                };
                            }

                            const deleteBtn = tr.querySelector('.i-row-delete-btn');
                            if (deleteBtn) {
                                deleteBtn.onclick = () => {
                                    if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi danh sách của nhà cung cấp?')) {
                                        deleteSupplierProduct(supplierId, item.productId, tr);
                                    }
                                };
                            }
                        }
                        
                        tbody.appendChild(tr);
                    });
                }
                loadActiveProducts(linkedProductIds);
            })
            .catch(err => {
                console.error(err);
                tbody.innerHTML = '<tr><td colspan="4" class="text-center py-3 text-danger">Có lỗi xảy ra khi tải danh sách sản phẩm.</td></tr>';
            });
    }

    function loadActiveProducts(currentLinkedProductIds = []) {
        const select = document.getElementById('addProductSelect');
        if (!select) return;
        select.innerHTML = '<option value="">-- Đang tải sản phẩm --</option>';
        
        fetch('suppliers?action=get-active-products-api')
            .then(res => res.json())
            .then(data => {
                allProducts = data;
                select.innerHTML = '<option value="">-- Chọn sản phẩm --</option>';
                allProducts.forEach(p => {
                    if (!currentLinkedProductIds.includes(p.productId)) {
                        const opt = document.createElement('option');
                        opt.value = p.productId;
                        opt.setAttribute('data-selling-price', p.sellingPrice || 0);
                        opt.innerText = `SP\${p.productId} - \${p.productName}`;
                        select.appendChild(opt);
                    }
                });
            })
            .catch(err => {
                console.error(err);
                select.innerHTML = '<option value="">-- Lỗi tải sản phẩm --</option>';
            });
    }

    function updateSupplierProductPrice(supplierId, productId, price) {
        fetch(`suppliers?action=update-price-api&supplierId=\${supplierId}&productId=\${productId}&price=\${price}`)
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    console.log('Cập nhật giá nhập thành công.');
                } else {
                    alert('Lỗi cập nhật giá nhập!');
                }
            })
            .catch(err => {
                console.error(err);
                alert('Có lỗi xảy ra khi kết nối server.');
            });
    }

    function deleteSupplierProduct(supplierId, productId, rowEl) {
        fetch(`suppliers?action=delete-product-api&supplierId=\${supplierId}&productId=\${productId}`)
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    rowEl.remove();
                    showSupplierProducts(currentSupplierId, currentSupplierName);
                } else {
                    alert('Lỗi khi xóa sản phẩm!');
                }
            })
            .catch(err => {
                console.error(err);
                alert('Có lỗi xảy ra.');
            });
    }

    document.addEventListener("DOMContentLoaded", function() {
        const select = document.getElementById('addProductSelect');
        const priceInput = document.getElementById('addProductPrice');
        if (select && priceInput) {
            select.onchange = () => {
                const opt = select.options[select.selectedIndex];
                if (opt && opt.value !== "") {
                    const sellingPrice = parseFloat(opt.getAttribute('data-selling-price')) || 0;
                    priceInput.value = Math.round(sellingPrice * 0.7);
                } else {
                    priceInput.value = '0';
                }
            };
        }

        const btnAdd = document.getElementById('btnAddSupplierProduct');
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
                
                fetch(`suppliers?action=add-product-api&supplierId=\${currentSupplierId}&productId=\${productId}&price=\${price}`)
                    .then(res => res.json())
                    .then(data => {
                        if (data.success) {
                            showSupplierProducts(currentSupplierId, currentSupplierName);
                            priceInputEl.value = '0';
                        } else {
                            alert('Lỗi thêm sản phẩm!');
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        alert('Có lỗi xảy ra.');
                    });
            };
        }
    });
</script>

<!-- Modal Xem danh sách sản phẩm nhà cung cấp -->
<div class="modal fade" id="supplierProductsModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content" style="border-radius: 12px; border: none; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
      <div class="modal-header border-bottom-0 pb-0">
        <h5 class="modal-title fw-bold" id="spModalTitle" style="color: #111827;">Danh sách sản phẩm</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body pt-3">
        <!-- Add Product Section -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
        <div class="row g-2 mb-3 align-items-end p-3 bg-light rounded-3 border border-light-subtle">
           <div class="col-md-6">
             <label class="form-label small fw-bold text-muted mb-1">Thêm sản phẩm mới</label>
             <select id="addProductSelect" class="form-select form-select-sm" style="border-radius: 8px;">
               <option value="">-- Chọn sản phẩm --</option>
             </select>
           </div>
           <div class="col-md-4">
             <label class="form-label small fw-bold text-muted mb-1">Giá nhập</label>
             <div class="input-group input-group-sm">
               <input type="number" id="addProductPrice" class="form-control text-end fw-bold" value="0" min="0" step="1000" style="border-top-left-radius: 8px; border-bottom-left-radius: 8px;">
               <span class="input-group-text small text-muted" style="border-top-right-radius: 8px; border-bottom-right-radius: 8px;">đ</span>
             </div>
           </div>
           <div class="col-md-2">
             <button type="button" class="btn btn-sm btn-danger w-100 fw-semibold text-white" id="btnAddSupplierProduct" style="border-radius: 8px; height: 31px;">Thêm</button>
           </div>
        </div>
        </c:if>

        <div class="table-responsive" style="border-radius: 8px; border: 1px solid #e5e7eb;">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light text-center" style="font-size: 13px; text-transform: uppercase; font-weight: 700;">
              <tr>
                <th width="100">Mã SP</th>
                <th class="text-start">Tên Sản Phẩm</th>
                <th width="220">Giá Nhập</th>
                <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
                <th width="100">Thao tác</th>
                </c:if>
              </tr>
            </thead>
            <tbody id="spModalTableBody" style="font-size: 14px;">
              <!-- Dynamically populated via AJAX -->
            </tbody>
          </table>
        </div>
      </div>
      <div class="modal-footer border-top-0 pt-0">
        <c:choose>
            <c:when test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
                <button type="button" class="btn btn-danger" onclick="location.reload();" style="border-radius: 8px; font-weight: 500;">Lưu</button>
            </c:when>
            <c:otherwise>
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style="border-radius: 8px; font-weight: 500;">Đóng</button>
            </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../common/footer.jsp"/>
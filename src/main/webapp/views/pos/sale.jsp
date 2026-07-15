<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("pageTitle", "POS - Bán hàng Tại Quầy"); %>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />
<main class="main">
    <style>
        .pos-layout { display: flex; gap: 20px; flex-wrap: wrap; }
        .pos-left { flex: 1; min-width: 320px; }
        .pos-right { flex: 0 0 380px; }
        .customer-card { background: #fff; border-radius: 8px; padding: 16px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
        .customer-card label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
        .search-wrapper { position: relative; display: flex; gap: 8px; }
        .search-wrapper input { flex: 1; padding: 10px 12px; border: 1px solid #d0d0d0; border-radius: 6px; font-size: 14px; }
        .search-wrapper input:focus { outline: none; border-color: #1a1a2e; }
        .search-wrapper .clear-btn { position: absolute; right: 80px; top: 50%; transform: translateY(-50%); cursor: pointer; color: #999; display: none; background: none; border: none; font-size: 18px; }
        .customer-info { margin-top: 10px; padding: 12px; background: #f8f9fa; border-radius: 6px; display: none; }
        .customer-info h4 { margin: 0 0 4px; font-size: 15px; }
        .customer-info p { margin: 2px 0; font-size: 13px; color: #666; }
        .customer-info .points { color: #1a1a2e; font-weight: 600; }
        .no-customer { margin-top: 10px; padding: 12px; text-align: center; background: #fff3e0; border-radius: 6px; display: none; }
        .no-customer p { margin: 0 0 6px; font-size: 13px; color: #e65100; }
        .no-customer button { background: #1a1a2e; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 13px; }
        .no-customer button:hover { opacity: .9; }
        .spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid #ddd; border-top-color: #1a1a2e; border-radius: 50%; animation: spin .6s linear infinite; margin-left: 8px; display: none; }
        @keyframes spin { to { transform: rotate(360deg); } }

        .modal-overlay-pos { display: none; position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 1000; align-items: center; justify-content: center; }
        .modal-overlay-pos.active { display: flex; }
        .modal-box-pos { background: #fff; border-radius: 12px; padding: 24px; width: 480px; max-width: 94vw; max-height: 90vh; overflow-y: auto; }
        .modal-header-pos { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
        .modal-header-pos h3 { margin: 0; font-size: 18px; }
        .modal-close-pos { background: none; border: none; font-size: 22px; cursor: pointer; color: #999; }
        .modal-close-pos:hover { color: #333; }
        .modal-body-pos .form-group { margin-bottom: 14px; }
        .modal-body-pos label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 4px; color: #555; }
        .modal-body-pos input { width: 100%; padding: 9px 12px; border: 1px solid #d0d0d0; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
        .modal-body-pos input:focus { outline: none; border-color: #1a1a2e; }
        .modal-footer-pos { display: flex; gap: 10px; justify-content: flex-end; margin-top: 16px; padding-top: 12px; border-top: 1px solid #eee; }
        .modal-footer-pos .btn { padding: 9px 20px; border-radius: 6px; font-size: 14px; cursor: pointer; border: none; }
        .modal-footer-pos .btn-primary { background: #1a1a2e; color: #fff; }
        .modal-footer-pos .btn-secondary { background: #f0f0f0; color: #333; }
        .modal-message { display: none; padding: 8px 12px; border-radius: 6px; margin-bottom: 12px; font-size: 13px; }
        .modal-message.error { display: block; background: #fdecea; color: #b71c1c; }
        .modal-message.success { display: block; background: #e8f5e9; color: #1b5e20; }

        @keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
        .animate-fadeIn { animation: fadeIn 0.15s ease-out; }
        @media (max-width: 768px) { .pos-right { flex: 1; min-width: unset; } }
    </style>

    <div class="card" style="padding: 24px;">
        <h1 style="margin: 0 0 20px; font-size: 22px;">POS — Bán hàng Tại Quầy</h1>

        <div class="pos-layout">
            <div class="pos-left">
                <div class="customer-card" id="customerSection">
                    <label>Khách hàng</label>
                    <div class="search-wrapper">
                        <input type="text" id="phoneSearch" placeholder="Search phone number..." autocomplete="off"/>
                        <button class="clear-btn" id="clearSearchBtn">&times;</button>
                        <span class="spinner" id="searchSpinner"></span>
                        <div id="posSearchDropdown" class="hidden absolute top-full left-0 right-0 mt-1 bg-surface-container-lowest rounded-xl shadow-xl border border-outline-variant z-50 animate-fadeIn" style="max-height:320px;overflow-y:auto;"></div>
                    </div>

                    <div class="customer-info" id="customerInfo" style="display:none;">
                        <div style="display:flex;align-items:center;justify-content:space-between;">
                            <div>
                                <h4 id="custName" style="margin:0 0 4px;font-size:15px;"></h4>
                                <p id="custPhone" style="margin:2px 0;font-size:13px;color:#666;"></p>
                                <p class="points" id="custPoints" style="margin:2px 0;font-size:13px;color:#1a1a2e;font-weight:600;"></p>
                            </div>
                            <button id="removeCustomerBtn" style="background:none;border:none;font-size:20px;cursor:pointer;color:#999;padding:4px 8px;border-radius:4px;display:none;" title="Bỏ chọn khách hàng">&times;</button>
                        </div>
                    </div>

                    <div class="no-customer" id="noCustomer" style="display:none;">
                        <p>No customer found.</p>
                        <button id="addCustomerBtn">
                            <span class="material-symbols-outlined" style="font-size:16px;vertical-align:middle;">person_add</span>
                            + Add New Customer
                        </button>
                    </div>
                </div>

                <div class="card" style="padding:16px; min-height:200px;">
                    <p style="color:#888;text-align:center;padding:40px 0;">Product area — awaiting cart implementation</p>
                </div>
            </div>

            <div class="pos-right">
                <div class="card" style="padding:16px; min-height:300px;">
                    <h3 style="margin:0 0 12px;font-size:16px;">Cart</h3>
                    <p style="color:#888;text-align:center;padding:40px 0;">Cart area — awaiting cart implementation</p>
                </div>
            </div>
        </div>
    </div>

    <%-- New Customer Modal --%>
    <div class="modal-overlay-pos" id="newCustomerModal">
        <div class="modal-box-pos">
            <div class="modal-header-pos">
                <h3>Add New Customer</h3>
                <button class="modal-close-pos" id="closeModalBtn">&times;</button>
            </div>
            <div class="modal-message" id="modalMessage"></div>
            <div class="modal-body-pos">
                <input type="hidden" id="modalAction" value="create"/>
                <input type="hidden" id="modalCustomerId" value=""/>
                <div class="form-group">
                    <label>Full Name *</label>
                    <input type="text" id="modalFullName" placeholder="Enter full name" required/>
                </div>
                <div class="form-group">
                    <label>Phone *</label>
                    <input type="text" id="modalPhone" placeholder="Enter phone number" required/>
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="modalEmail" placeholder="Enter email"/>
                </div>
            </div>
            <div class="modal-footer-pos">
                <button class="btn btn-secondary" id="cancelModalBtn">Cancel</button>
                <button class="btn btn-primary" id="saveCustomerBtn">Save Customer</button>
            </div>
        </div>
    </div>

    <script>
        (function() {
            var searchInput = document.getElementById('phoneSearch');
            var clearBtn = document.getElementById('clearSearchBtn');
            var spinner = document.getElementById('searchSpinner');
            var dropdown = document.getElementById('posSearchDropdown');
            var customerInfo = document.getElementById('customerInfo');
            var noCustomer = document.getElementById('noCustomer');
            var custName = document.getElementById('custName');
            var custPhone = document.getElementById('custPhone');
            var custPoints = document.getElementById('custPoints');
            var removeBtn = document.getElementById('removeCustomerBtn');
            var addCustomerBtn = document.getElementById('addCustomerBtn');

            var modal = document.getElementById('newCustomerModal');
            var closeModalBtn = document.getElementById('closeModalBtn');
            var cancelModalBtn = document.getElementById('cancelModalBtn');
            var saveBtn = document.getElementById('saveCustomerBtn');
            var modalFullName = document.getElementById('modalFullName');
            var modalPhone = document.getElementById('modalPhone');
            var modalEmail = document.getElementById('modalEmail');
            var modalMessage = document.getElementById('modalMessage');

            var selectedCustomer = null;
            var searchTimeout = null;
            var selectedIndex = -1;

            function resetCustomerUI() {
                dropdown.classList.add('hidden');
                dropdown.innerHTML = '';
                customerInfo.style.display = 'none';
                noCustomer.style.display = 'none';
                selectedCustomer = null;
                selectedIndex = -1;
            }

            function showCustomer(customer) {
                customerInfo.style.display = 'block';
                noCustomer.style.display = 'none';
                custName.textContent = customer.fullName;
                custPhone.textContent = customer.phone;
                custPoints.textContent = (customer.loyaltyPoint || 0) + ' Points';
                removeBtn.style.display = 'block';
                selectedCustomer = customer;
            }

            function buildDropdown(customers) {
                dropdown.innerHTML = '';
                if (customers.length === 0) {
                    var div = document.createElement('div');
                    div.className = 'px-4 py-3 text-caption text-outline text-center';
                    div.textContent = 'No customer found';
                    dropdown.appendChild(div);
                    var btn = document.createElement('button');
                    btn.className = 'w-full flex items-center gap-2 px-4 py-3 text-label-md text-primary hover:bg-surface-container-high transition-colors border-t border-outline-variant/50';
                    btn.innerHTML = '<span class="material-symbols-outlined" style="font-size:16px;vertical-align:middle;">person_add</span> + Add New Customer';
                    btn.onclick = function() {
                        modalPhone.value = searchInput.value.trim();
                        dropdown.classList.add('hidden');
                        modalFullName.value = '';
                        modalEmail.value = '';
                        modalMessage.className = 'modal-message';
                        modalMessage.style.display = 'none';
                        modalFullName.focus();
                        modal.classList.add('active');
                    };
                    dropdown.appendChild(btn);
                } else {
                    customers.forEach(function(c, i) {
                        var item = document.createElement('button');
                        item.className = 'w-full flex flex-col gap-0.5 px-4 py-3 hover:bg-surface-container-high transition-colors text-left border-b border-outline-variant/30 last:border-0';
                        item.dataset.index = i;
                        item.innerHTML =
                            '<div style="display:flex;align-items:center;gap:6px;"><span class="material-symbols-outlined" style="font-size:16px;color:#666;">person</span><span style="font-size:14px;font-weight:600;">' + escHtml(c.fullName) + '</span></div>' +
                            '<div style="display:flex;align-items:center;gap:6px;padding-left:22px;"><span class="material-symbols-outlined" style="font-size:14px;color:#666;">call</span><span style="font-size:13px;color:#666;">' + escHtml(c.phone) + '</span></div>' +
                            '<div style="display:flex;align-items:center;gap:6px;padding-left:22px;"><span class="material-symbols-outlined" style="font-size:14px;color:#1a1a2e;">stars</span><span style="font-size:13px;color:#1a1a2e;font-weight:600;">' + (c.loyaltyPoint || 0) + ' Loyalty Points</span></div>';
                        item.onclick = function() { selectCustomer(c); };
                        dropdown.appendChild(item);
                    });
                }
                dropdown.classList.remove('hidden');
            }

            function selectCustomer(c) {
                dropdown.classList.add('hidden');
                showCustomer(c);
                searchInput.value = c.phone;
                clearBtn.style.display = 'block';
            }

            function doSearch(phone) {
                if (!phone || phone.trim() === '') {
                    resetCustomerUI();
                    clearBtn.style.display = 'none';
                    return;
                }
                clearBtn.style.display = 'block';
                spinner.style.display = 'inline-block';
                resetCustomerUI();

                var ctx = '<%= request.getContextPath() %>';
                fetch(ctx + '/customers?action=search-pos&phone=' + encodeURIComponent(phone.trim()))
                    .then(function(r) { return r.json(); })
                    .then(function(data) {
                        spinner.style.display = 'none';
                        buildDropdown(data || []);
                    })
                    .catch(function() {
                        spinner.style.display = 'none';
                        resetCustomerUI();
                    });
            }

            function escHtml(s) {
                if (!s) return '';
                var d = document.createElement('div');
                d.textContent = s;
                return d.innerHTML;
            }

            searchInput.addEventListener('input', function() {
                clearTimeout(searchTimeout);
                var val = this.value.trim();
                if (val === '') {
                    resetCustomerUI();
                    clearBtn.style.display = 'none';
                    return;
                }
                searchTimeout = setTimeout(function() { doSearch(val); }, 300);
            });

            searchInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    clearTimeout(searchTimeout);
                    var items = dropdown.querySelectorAll('button[data-index]');
                    if (selectedIndex >= 0 && selectedIndex < items.length) {
                        items[selectedIndex].click();
                    } else {
                        doSearch(this.value);
                    }
                } else if (e.key === 'ArrowDown') {
                    e.preventDefault();
                    var items = dropdown.querySelectorAll('button[data-index]');
                    if (items.length === 0) return;
                    selectedIndex = (selectedIndex + 1) % items.length;
                    items.forEach(function(el, i) { el.classList.toggle('bg-surface-container-high', i === selectedIndex); });
                } else if (e.key === 'ArrowUp') {
                    e.preventDefault();
                    var items = dropdown.querySelectorAll('button[data-index]');
                    if (items.length === 0) return;
                    selectedIndex = selectedIndex <= 0 ? items.length - 1 : selectedIndex - 1;
                    items.forEach(function(el, i) { el.classList.toggle('bg-surface-container-high', i === selectedIndex); });
                }
            });

            document.addEventListener('click', function(e) {
                if (!dropdown.classList.contains('hidden') && !searchInput.contains(e.target) && !dropdown.contains(e.target)) {
                    dropdown.classList.add('hidden');
                }
            });

            clearBtn.addEventListener('click', function() {
                searchInput.value = '';
                searchInput.focus();
                resetCustomerUI();
                clearBtn.style.display = 'none';
            });

            removeBtn.addEventListener('click', function() {
                searchInput.value = '';
                searchInput.focus();
                resetCustomerUI();
                clearBtn.style.display = 'none';
            });

            addCustomerBtn.addEventListener('click', function() {
                modalPhone.value = searchInput.value.trim();
                modalFullName.value = '';
                modalEmail.value = '';
                modalMessage.className = 'modal-message';
                modalMessage.style.display = 'none';
                modalFullName.focus();
                modal.classList.add('active');
            });

            function closeModal() {
                modal.classList.remove('active');
                modalMessage.className = 'modal-message';
                modalMessage.style.display = 'none';
            }

            closeModalBtn.addEventListener('click', closeModal);
            cancelModalBtn.addEventListener('click', closeModal);
            modal.addEventListener('click', function(e) {
                if (e.target === modal) closeModal();
            });

            saveBtn.addEventListener('click', function() {
                var fullName = modalFullName.value.trim();
                var phone = modalPhone.value.trim();
                var email = modalEmail.value.trim();

                if (!fullName) {
                    showModalError('Please enter full name.');
                    return;
                }
                if (!phone) {
                    showModalError('Please enter phone number.');
                    return;
                }

                saveBtn.disabled = true;
                saveBtn.textContent = 'Saving...';

                var params = new URLSearchParams();
                params.append('action', 'create-api');
                params.append('fullName', fullName);
                params.append('phone', phone);
                params.append('email', email);

                var ctx = '<%= request.getContextPath() %>';
                fetch(ctx + '/customers', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params.toString()
                })
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    saveBtn.disabled = false;
                    saveBtn.textContent = 'Save Customer';
                    if (data.status === 'success' && data.customerId) {
                        closeModal();
                        showCustomer(data);
                        searchInput.value = data.phone;
                        clearBtn.style.display = 'block';
                    } else if (data.status === 'success') {
                        closeModal();
                        if (data.customerId) {
                            showCustomer(data);
                            searchInput.value = data.phone;
                            clearBtn.style.display = 'block';
                        } else {
                            doSearch(phone);
                        }
                    } else {
                        showModalError(data.message || 'Cannot create customer.');
                    }
                })
                .catch(function() {
                    saveBtn.disabled = false;
                    saveBtn.textContent = 'Save Customer';
                    showModalError('Network error. Please try again.');
                });
            });

            function showModalError(msg) {
                modalMessage.textContent = msg;
                modalMessage.className = 'modal-message error';
                modalMessage.style.display = 'block';
            }

            window.selectCustomer = function(customer) {
                selectCustomer(customer);
            };

            window.getSelectedCustomer = function() {
                return selectedCustomer;
            };
        })();
    </script>
</main>
<jsp:include page="/views/common/footer.jsp" />
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Bán hàng POS"/>
</jsp:include>

<style>
    /* POS Specific Styles */
    .pos-container {
        display: grid;
        grid-template-columns: 1.8fr 1.2fr;
        gap: 20px;
        height: calc(100vh - var(--header-height) - 64px);
    }
    
    .pos-products {
        background-color: var(--card-bg);
        border: 1px solid var(--border-color);
        border-radius: 12px;
        padding: 20px;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }
    
    .pos-cart {
        background-color: var(--card-bg);
        border: 1px solid var(--border-color);
        border-radius: 12px;
        padding: 20px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        overflow: hidden;
        box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
    }
    
    .pos-tabs {
        display: flex;
        gap: 8px;
        margin-bottom: 16px;
        overflow-x: auto;
        padding-bottom: 8px;
    }
    
    .pos-product-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16px;
        overflow-y: auto;
        flex: 1;
        padding-right: 4px;
    }
    
    @media (max-width: 1200px) {
        .pos-product-grid {
            grid-template-columns: repeat(2, 1fr);
        }
    }
    
    .pos-product-card {
        border: 1px solid var(--border-color);
        border-radius: 10px;
        padding: 14px;
        text-align: center;
        cursor: pointer;
        transition: all 0.2s ease;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        align-items: center;
        background-color: #f8fafc;
    }
    
    .pos-product-card:hover {
        border-color: var(--primary-color);
        box-shadow: 0 4px 12px rgba(147, 0, 11, 0.06);
        transform: translateY(-2px);
        background-color: #fff;
    }
    
    .pos-product-icon {
        width: 54px;
        height: 54px;
        background-color: var(--primary-light);
        color: var(--primary-color);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
    }
    
    .pos-product-card h6 {
        font-size: 13px;
        font-weight: 700;
        margin-bottom: 6px;
        color: var(--text-main);
    }
    
    .pos-product-card .price {
        font-size: 14px;
        font-weight: 800;
        color: var(--primary-color);
    }
    
    /* Cart panel style */
    .cart-title {
        border-bottom: 1.5px solid var(--border-color);
        padding-bottom: 12px;
        margin-bottom: 16px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .cart-items-list {
        flex: 1;
        overflow-y: auto;
        margin-bottom: 16px;
        padding-right: 4px;
    }
    
    .cart-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid var(--border-color);
    }
    
    .cart-item-details h6 {
        font-size: 13px;
        font-weight: 700;
        margin-bottom: 2px;
    }
    
    .cart-item-details small {
        color: var(--text-muted);
        font-size: 11px;
    }
    
    .cart-item-qty {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    
    .qty-btn {
        width: 24px;
        height: 24px;
        border-radius: 4px;
        border: 1px solid var(--border-color);
        background-color: #f1f5f9;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        font-weight: bold;
    }
    
    .qty-btn:hover {
        background-color: var(--primary-color);
        color: white;
        border-color: var(--primary-color);
    }
    
    .cart-item-price {
        font-weight: 700;
        font-size: 13px;
        color: var(--text-main);
        min-width: 65px;
        text-align: right;
    }
    
    .cart-summary {
        border-top: 1.5px solid var(--border-color);
        padding-top: 16px;
    }
    
    .summary-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 13px;
        color: var(--text-muted);
    }
    
    .summary-row.total {
        margin-top: 12px;
        font-size: 18px;
        font-weight: 800;
        color: var(--primary-color);
    }
    
    .cart-actions {
        margin-top: 16px;
        display: flex;
        gap: 10px;
    }
    
    .btn-checkout {
        flex: 1;
        background-color: var(--primary-color);
        color: white;
        border: none;
        padding: 12px;
        border-radius: 8px;
        font-weight: 700;
        font-size: 14px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        transition: all 0.2s ease;
    }
    
    .btn-checkout:hover {
        background-color: var(--primary-hover);
        box-shadow: 0 4px 12px rgba(147, 0, 11, 0.2);
    }
    
    .btn-clear {
        background-color: #f1f5f9;
        color: #475569;
        border: 1px solid var(--border-color);
        padding: 12px;
        border-radius: 8px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s ease;
    }
    
    .btn-clear:hover {
        background-color: #e2e8f0;
    }
</style>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    
    <div class="main-content">
        <jsp:include page="/views/common/topbar.jsp" />
        
        <div class="page-container" style="padding-bottom: 12px;">
            <!-- Breadcrumbs -->
            <div class="page-breadcrumb">
                <a href="#">Bán hàng</a>
                <span class="material-icons">chevron_right</span>
                <span>Tạo hóa đơn</span>
            </div>

            <!-- POS Area -->
            <div class="pos-container">
                <!-- Left: Product Showcase -->
                <div class="pos-products">
                    <div class="pos-tabs">
                        <button class="filter-tab active">Tất cả</button>
                        <button class="filter-tab">Cà phê</button>
                        <button class="filter-tab">Trà trái cây</button>
                        <button class="filter-tab">Bánh ngọt</button>
                        <button class="filter-tab">Nước đóng chai</button>
                    </div>
                    
                    <div class="pos-product-grid">
                        <!-- Product 1 -->
                        <div class="pos-product-card" onclick="addToCart('Cà phê Đen Đá', 29000, 'COF-01')">
                            <div class="pos-product-icon">
                                <span class="material-icons">coffee</span>
                            </div>
                            <h6>Cà phê Đen Đá</h6>
                            <span class="price">29,000 đ</span>
                        </div>
                        
                        <!-- Product 2 -->
                        <div class="pos-product-card" onclick="addToCart('Cà phê Sữa Đá', 35000, 'COF-02')">
                            <div class="pos-product-icon">
                                <span class="material-icons">coffee</span>
                            </div>
                            <h6>Cà phê Sữa Đá</h6>
                            <span class="price">35,000 đ</span>
                        </div>

                        <!-- Product 3 -->
                        <div class="pos-product-card" onclick="addToCart('Trà Đào Cam Sả', 45000, 'TEA-01')">
                            <div class="pos-product-icon">
                                <span class="material-icons">local_cafe</span>
                            </div>
                            <h6>Trà Đào Cam Sả</h6>
                            <span class="price">45,000 đ</span>
                        </div>

                        <!-- Product 4 -->
                        <div class="pos-product-card" onclick="addToCart('Bánh Croissant', 32000, 'BAK-01')">
                            <div class="pos-product-icon">
                                <span class="material-icons">cake</span>
                            </div>
                            <h6>Bánh Croissant</h6>
                            <span class="price">32,000 đ</span>
                        </div>

                        <!-- Product 5 -->
                        <div class="pos-product-card" onclick="addToCart('Bánh Mousse Tiramisu', 49000, 'BAK-02')">
                            <div class="pos-product-icon">
                                <span class="material-icons">cake</span>
                            </div>
                            <h6>Bánh Tiramisu</h6>
                            <span class="price">49,000 đ</span>
                        </div>

                        <!-- Product 6 -->
                        <div class="pos-product-card" onclick="addToCart('Nước Suối 500ml', 10000, 'BOT-01')">
                            <div class="pos-product-icon">
                                <span class="material-icons">water_drop</span>
                            </div>
                            <h6>Nước Suối 500ml</h6>
                            <span class="price">10,000 đ</span>
                        </div>
                    </div>
                </div>

                <!-- Right: Cart & Pay -->
                <div class="pos-cart">
                    <div>
                        <div class="cart-title">
                            <h5 style="margin: 0; display: flex; align-items: center; gap: 8px;">
                                <span class="material-icons" style="color: var(--primary-color)">shopping_cart</span>
                                <span>Giỏ hàng</span>
                            </h5>
                            <span id="cart-count" class="badge bg-danger" style="border-radius: 12px; padding: 4px 8px;">0 món</span>
                        </div>
                        
                        <!-- List of Cart Items -->
                        <div class="cart-items-list" id="cart-items">
                            <div style="text-align: center; color: var(--text-muted); padding-top: 60px;" id="cart-empty-message">
                                <span class="material-icons" style="font-size: 48px; margin-bottom: 8px; color: #cbd5e1;">shopping_basket</span>
                                <p>Giỏ hàng đang trống.<br>Hãy chọn món từ danh sách phía bên trái.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Payment summary -->
                    <div class="cart-summary">
                        <div style="margin-bottom: 12px;">
                            <label style="font-size: 12px; color: var(--text-muted); font-weight: 600;">Khách hàng</label>
                            <select style="font-size: 13px; padding: 8px; border-radius: 6px;">
                                <option>Khách vãng lai</option>
                                <option>Nguyễn Văn A (Thành viên Thân thiết)</option>
                                <option>Trần Thị B</option>
                            </select>
                        </div>
                        
                        <div style="margin-bottom: 16px;">
                            <label style="font-size: 12px; color: var(--text-muted); font-weight: 600;">Phương thức thanh toán</label>
                            <select style="font-size: 13px; padding: 8px; border-radius: 6px;">
                                <option>Tiền mặt</option>
                                <option>Chuyển khoản QR (VietQR)</option>
                                <option>Thẻ ngân hàng (POS)</option>
                            </select>
                        </div>

                        <div class="summary-row">
                            <span>Tạm tính</span>
                            <span id="summary-subtotal">0 đ</span>
                        </div>
                        <div class="summary-row">
                            <span>Thuế VAT (8%)</span>
                            <span id="summary-tax">0 đ</span>
                        </div>
                        <div class="summary-row total">
                            <span>Tổng tiền</span>
                            <span id="summary-total">0 đ</span>
                        </div>

                        <div class="cart-actions">
                            <button class="btn-clear" onclick="clearCart()">Xóa hết</button>
                            <button class="btn-checkout" onclick="checkout()">
                                <span class="material-icons">receipt_long</span>
                                <span>Thanh toán & In</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
        </div>
    </div>
</div>

<script>
    // Real-time Interactive Cart JS
    let cart = [];

    function addToCart(name, price, sku) {
        let existingItem = cart.find(item => item.sku === sku);
        if (existingItem) {
            existingItem.qty++;
        } else {
            cart.push({ name, price, sku, qty: 1 });
        }
        renderCart();
    }

    function changeQty(sku, delta) {
        let item = cart.find(item => item.sku === sku);
        if (item) {
            item.qty += delta;
            if (item.qty <= 0) {
                cart = cart.filter(i => i.sku !== sku);
            }
        }
        renderCart();
    }

    function clearCart() {
        cart = [];
        renderCart();
    }

    function renderCart() {
        const cartItemsContainer = document.getElementById('cart-items');
        const emptyMsg = document.getElementById('cart-empty-message');
        const cartCount = document.getElementById('cart-count');
        
        if (cart.length === 0) {
            cartItemsContainer.innerHTML = `
                <div style="text-align: center; color: var(--text-muted); padding-top: 60px;" id="cart-empty-message">
                    <span class="material-icons" style="font-size: 48px; margin-bottom: 8px; color: #cbd5e1;">shopping_basket</span>
                    <p>Giỏ hàng đang trống.<br>Hãy chọn món từ danh sách phía bên trái.</p>
                </div>
            `;
            cartCount.innerText = "0 món";
            updateSummary(0);
            return;
        }

        cartCount.innerText = cart.reduce((sum, item) => sum + item.qty, 0) + " món";
        
        let html = '';
        let subtotal = 0;
        
        cart.forEach(item => {
            const itemTotal = item.price * item.qty;
            subtotal += itemTotal;
            html += `
                <div class="cart-item">
                    <div class="cart-item-details">
                        <h6>${item.name}</h6>
                        <small>${item.price.toLocaleString()} đ</small>
                    </div>
                    <div class="cart-item-qty">
                        <div class="qty-btn" onclick="changeQty('${item.sku}', -1)">-</div>
                        <span style="font-weight:700; font-size:13px; min-width:16px; text-align:center;">${item.qty}</span>
                        <div class="qty-btn" onclick="changeQty('${item.sku}', 1)">+</div>
                    </div>
                    <div class="cart-item-price">${itemTotal.toLocaleString()} đ</div>
                </div>
            `;
        });
        
        cartItemsContainer.innerHTML = html;
        updateSummary(subtotal);
    }

    function updateSummary(subtotal) {
        const tax = Math.round(subtotal * 0.08);
        const total = subtotal + tax;
        
        document.getElementById('summary-subtotal').innerText = subtotal.toLocaleString() + " đ";
        document.getElementById('summary-tax').innerText = tax.toLocaleString() + " đ";
        document.getElementById('summary-total').innerText = total.toLocaleString() + " đ";
    }

    function checkout() {
        if (cart.length === 0) {
            alert('Giỏ hàng trống! Vui lòng chọn món ăn/nước uống.');
            return;
        }
        alert('Đặt hàng & Thanh toán thành công!\nHóa đơn đã được in ra.');
        clearCart();
    }
</script>

<jsp:include page="/views/common/footer.jsp" />

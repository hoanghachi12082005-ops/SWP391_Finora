<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Đăng ký dùng thử"/>
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/register.css">

<div class="container-fluid p-0 register-container">

    <div class="row g-0 min-vh-100">
        <!-- LEFT SIDE -->
        <div class="col-md-6 d-none d-md-flex register-left">

            <div class="register-overlay"></div>

            <div class="register-left-content">
                <h1>
                    Bắt đầu kinh doanh<br>
                    cùng FinoraRetail
                </h1>

                <p>
                    Quản lý bán hàng, kho hàng, khách hàng
                    và doanh thu trên một nền tảng duy nhất.
                </p>
            </div>

        </div>

        <!-- RIGHT SIDE -->
        <div class="col-12 col-md-6 register-right">

            <div class="register-card">
                <form id="registerForm" action="${pageContext.request.contextPath}/register" method="post">

                    <div class="text-center mb-4">

                        <div class="brand-icon">
                            <span class="material-icons">storefront</span>
                        </div>

                        <h2 class="brand-title">
                            FinoraRetail
                        </h2>

                    </div>

                    <!-- STEP INDICATOR -->

                    <div class="wizard-step mb-4">

                        <div class="step-item active" id="indicator1">1</div>
                        <div class="step-line"></div>

                        <div class="step-item" id="indicator2">2</div>
                        <div class="step-line"></div>

                        <div class="step-item" id="indicator3">3</div>
                        <div class="step-line"></div>

                        <div class="step-item" id="indicator4">4</div>

                    </div>

                    <!-- STEP 1 -->

                    <div class="wizard-content active" id="step1">

                        <div class="mb-3">
                            <label class="form-label">Họ và tên</label>
                            <input type="text" id="fullName"  name="fullName" class="form-control custom-input">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Số điện thoại</label>
                            <input type="text" id="phone"  name="phone" class="form-control custom-input">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input type="email" id="email" name="email" class="form-control custom-input">
                        </div>



                        <div class="mb-3">
                            <label class="form-label">Khu vực</label>

                            <select id="province"  name="province" class="form-select custom-input">
                                <option value="">-- Chọn tỉnh thành --</option>
                            </select>
                        </div>

                        <div class="text-end">

                             <button type="button" type="button"
                                    class="btn btn-finora"
                                    onclick="nextStep(2)">
                                Tiếp tục
                            </button>

                        </div>

                    </div>

                    <!-- STEP 2 -->

                    <div class="wizard-content" id="step2">

                        <div class="text-center mb-5">
                            <h3 class="fw-bold">
                                Hãy chọn ngành hàng kinh doanh của bạn
                            </h3>
                        </div>

                        <div class="row g-4">

                            <!-- BÁN BUÔN BÁN LẺ -->
                            <div class="col-lg-4">

                                <div class="industry-category">

                                    <div class="industry-header">

                                        <div class="industry-icon">
                                            <span class="material-icons">
                                                storefront
                                            </span>
                                        </div>

                                        <h4>Bán buôn, bán lẻ</h4>

                                    </div>

                                    <div class="industry-list">

                                         <button type="button" type="button"  class="industry-item"
                                                onclick="selectIndustry('Thời trang')">
                                            <span class="material-icons">checkroom</span>
                                            Thời trang
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Tạp hóa & Siêu thị')">
                                            <span class="material-icons">shopping_cart</span>
                                            Tạp hóa & Siêu thị
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Điện thoại & Điện máy')">
                                            <span class="material-icons">phone_android</span>
                                            Điện thoại & Điện máy
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Mỹ phẩm')">
                                            <span class="material-icons">spa</span>
                                            Mỹ phẩm
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Vật liệu xây dựng')">
                                            <span class="material-icons">construction</span>
                                            Vật liệu xây dựng
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Nông sản & Thực phẩm')">
                                            <span class="material-icons">eco</span>
                                            Nông sản & Thực phẩm
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Nhà thuốc')">
                                            <span class="material-icons">local_pharmacy</span>
                                            Nhà thuốc
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Xe, Máy móc')">
                                            <span class="material-icons">two_wheeler</span>
                                            Xe, Máy móc
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Mẹ & Bé')">
                                            <span class="material-icons">child_care</span>
                                            Mẹ & Bé
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Nội thất & Gia dụng')">
                                            <span class="material-icons">chair</span>
                                            Nội thất & Gia dụng
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Sách & Văn phòng phẩm')">
                                            <span class="material-icons">menu_book</span>
                                            Sách & Văn phòng phẩm
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Hoa & Quà tặng')">
                                            <span class="material-icons">card_giftcard</span>
                                            Hoa & Quà tặng
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Sản xuất')">
                                            <span class="material-icons">precision_manufacturing</span>
                                            Sản xuất
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Khác')">
                                            <span class="material-icons">apps</span>
                                            Khác
                                        </button>

                                    </div>

                                </div>

                            </div>

                            <!-- ĂN UỐNG GIẢI TRÍ -->

                            <div class="col-lg-4">

                                <div class="industry-category">

                                    <div class="industry-header">

                                        <div class="industry-icon">
                                            <span class="material-icons">
                                                restaurant
                                            </span>
                                        </div>

                                        <h4>Ăn uống, giải trí</h4>

                                    </div>

                                    <div class="industry-list">

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Nhà hàng')">
                                            <span class="material-icons">restaurant</span>
                                            Nhà hàng
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Quán ăn')">
                                            <span class="material-icons">lunch_dining</span>
                                            Quán ăn
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Cafe, Trà sữa')">
                                            <span class="material-icons">local_cafe</span>
                                            Cafe, Trà sữa
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Karaoke, Bida')">
                                            <span class="material-icons">mic</span>
                                            Karaoke, Bida
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Bar, Pub & Club')">
                                            <span class="material-icons">local_bar</span>
                                            Bar, Pub & Club
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Căng tin')">
                                            <span class="material-icons">fastfood</span>
                                            Căng tin
                                        </button>

                                    </div>

                                </div>

                            </div>

                            <!-- LƯU TRÚ LÀM ĐẸP -->

                            <div class="col-lg-4">

                                <div class="industry-category">

                                    <div class="industry-header">

                                        <div class="industry-icon">
                                            <span class="material-icons">
                                                hotel
                                            </span>
                                        </div>

                                        <h4>Lưu trú, làm đẹp</h4>

                                    </div>

                                    <div class="industry-list">

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Beauty Spa & Massage')">
                                            <span class="material-icons">spa</span>
                                            Beauty Spa & Massage
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Hair Salon & Nails')">
                                            <span class="material-icons">content_cut</span>
                                            Hair Salon & Nails
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Khách sạn')">
                                            <span class="material-icons">apartment</span>
                                            Khách sạn
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Homestay & Resort')">
                                            <span class="material-icons">villa</span>
                                            Homestay & Resort
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Fitness & Yoga')">
                                            <span class="material-icons">fitness_center</span>
                                            Fitness & Yoga
                                        </button>

                                         <button type="button" type="button" class="industry-item"
                                                onclick="selectIndustry('Phòng khám')">
                                            <span class="material-icons">medical_services</span>
                                            Phòng khám
                                        </button>

                                    </div>

                                </div>

                            </div>

                        </div>

                    </div>

                    <!-- STEP 3 -->

                    <div class="wizard-content" id="step3">

                        <div class="mb-3">

                            <label class="form-label">
                                Ngành hàng
                            </label>

                            <input type="text" id="selectedIndustry" name="industry" readonly class="form-control custom-input">

                        </div>

                        <div class="mb-3">

                            <label class="form-label">
                                Tên cửa hàng
                            </label>

                            <input type="text" id="shopName"  name="shopName" class="form-control custom-input">

                        </div>

                        <div class="mb-4">

                            <label class="form-label">
                                Mật khẩu
                            </label>

                            <div class="input-group">

                                <input type="password" id="password" name="password" class="form-control custom-input">

                                 <button type="button" class="btn btn-outline-secondary" type="button" onclick="togglePassword()">

                                    <span class="material-icons">
                                        visibility
                                    </span>

                                </button>

                            </div>

                        </div>

                        <div class="d-flex justify-content-between">

                             <button type="button" class="btn btn-light" onclick="nextStep(2)">
                                Quay lại
                            </button>

                             <button type="button" class="btn btn-finora" onclick="submitRegister()">
                                Tạo cửa hàng
                            </button>

                        </div>

                    </div>

                    <!-- STEP 4 -->

                    <div class="wizard-content text-center" id="step4">

                        <div class="success-icon mb-4"> ✓ </div>

                        <h4 class="mb-4">Cửa hàng đã được tạo</h4>

                        <div class="result-card">

                            <div>
                                <strong>Tên cửa hàng:</strong>
                                <span id="resShopName">${resShopName}</span>
                            </div>

                            <div>
                                <strong>Tài khoản:</strong>
                                <span id="resUsername">${resUsername}</span>
                            </div>

                            <div>
                                <strong>Mật khẩu:</strong>
                                <span id="resPassword">${resPassword}</span>
                            </div>

                        </div>

                         <button type="button" class="btn btn-finora w-100 mt-4">
                            Bắt đầu quản lý
                        </button>

                    </div>
                </form>
            </div>

        </div>

    </div>

</div>

<script src="${pageContext.request.contextPath}/assets/js/register.js"></script>
      
<jsp:include page="../common/footer.jsp"/>

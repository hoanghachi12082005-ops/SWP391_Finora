<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="${branch == null ? 'Thêm chi nhánh' : 'Cập nhật chi nhánh'}"/>
    <jsp:param name="additionalCSS" value="branch.css"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <main class="main-content">
        
        
        <div class="container-fluid py-4">
            <div class="page-header">
                <div class="page-title">
                    <h1>
                        ${branch != null && branch.branchId > 0
                          ? 'Cập nhật chi nhánh'  
                          : 'Thêm chi nhánh mới'}

                    </h1>
                    <p>
                        Quản lý thông tin chi nhánh
                    </p>
                </div>
            </div>

            <c:if test="${not empty errors.general}">
                <div style="background:#fee2e2;color:#b91c1c;padding:12px 16px;margin-bottom:16px;border-radius:8px;">
                    ${errors.general}
                </div>
            </c:if>

            <c:url var="branchAction" value="branch">
                <c:param name="csrfToken" value="${sessionScope.csrfToken}"/>
            </c:url>

            <form action="${branchAction}"
                  method="post"
                  enctype="multipart/form-data"
                  class="branch-form">
                
                <input type="hidden" name="returnPage" value="${page}">
<input type="hidden" name="returnSizeValue" value="${sizeValue}">
<input type="hidden" name="returnKeyword" value="${keyword}">
<input type="hidden" name="returnStatus" value="${status}">
<input type="hidden" name="returnCity" value="${city}">
                
                <c:choose>
                    <c:when test="${branch == null || branch.branchId <= 0}">

                        <input type="hidden"
                               name="action"
                               value="insert">
                    </c:when>

                    <c:otherwise>
                        <input type="hidden"
                               name="action"
                               value="update">

                        <input type="hidden"
                               name="id"
                               value="${branch.branchId}">
                    </c:otherwise>
                </c:choose>

                <!-- THÔNG TIN CƠ BẢN -->

                <div class="form-card"> 
                    <h2>Thông tin cơ bản</h2>
                    <div class="form-grid">
                        <div class="form-group">
                            <label>Tên chi nhánh *</label>
                            <input type="text"
                                   name="branchName"
                                   value="${branch.branchName}"
                                   required>
                            <c:if test="${not empty errors.branchName}">
                                <span style="color:red; font-size:13px; margin-top:5px;">${errors.branchName}</span>
                            </c:if>
                        </div>

                        <div class="form-group">
                            <label>Mã chi nhánh *</label>
                            <input type="text"
                                   name="branchCode"
                                   value="${branch.branchCode}"
                                   required>
                            <c:if test="${not empty errors.branchCode}">
                                <span style="color:red; font-size:13px; margin-top:5px;">${errors.branchCode}</span>
                            </c:if>
                        </div>

                        <div class="form-group full-width">
                            <label>Quản lý chi nhánh</label>
                            <select name="managerId">
                                <option value="">
                                    Chọn quản lý
                                </option>
                                <c:forEach items="${employeeList}"
                                           var="emp">
                                    <option value="${emp.empId}">
                                        ${emp.fullName}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Giờ mở cửa</label>

                            <input type="text"
                                   name="openingTime"
                                   value="${branch.openingTime}"
                                   placeholder="08:00"
                                   maxlength="5">
                            <c:if test="${not empty errors.openingTime}">
                                <span style="color:red; font-size:13px; margin-top:5px;">${errors.openingTime}</span>
                            </c:if>

                        </div>

                        <div class="form-group">
                            <label>Giờ đóng cửa</label>

                            <input type="text"
                                   name="closingTime"
                                   value="${branch.closingTime}"
                                   placeholder="22:00"
                                   maxlength="5">
                            <c:if test="${not empty errors.closingTime}">
                                <span style="color:red; font-size:13px; margin-top:5px;">${errors.closingTime}</span>
                            </c:if>
                        </div>
                    </div>
                </div>

                <!-- THÔNG TIN LIÊN HỆ -->

                <div class="form-card">

                    <h2>Thông tin liên hệ</h2>
                    <div class="form-grid">
                        <div class="form-group">
                            <label>Số điện thoại *</label>
                            <input type="text"
                                   name="phone"
                                   value="${branch.phone}"
                                   required>
                            <c:if test="${not empty errors.general}">
                                <%-- <span style="color:red; font-size:13px; margin-top:5px;">${errors.general}</span> --%>
                            </c:if>
                        </div>

                        <div class="form-group">
                            <label>Email</label>
                            <input type="email"
                                   name="email"
                                   value="${branch.email}">
                            <c:if test="${not empty errors.general}">
                                <%-- <span style="color:red; font-size:13px; margin-top:5px;">${errors.general}</span> --%>
                            </c:if>
                        </div>

                        <div class="form-group full-width">
                            <label>Địa chỉ</label>

                            <textarea name="address"
                                      rows="4">${branch.address}</textarea>
                        </div>
                    </div>
                </div>

                <!-- PHÂN LOẠI -->

                <div class="form-card">
                    <h2>Phân loại & Trạng thái</h2>
                    <div class="form-grid">
                        <input type="hidden"
                               id="selectedCity"
                               value="${branch.city}">

                        <input type="hidden"
                               id="selectedDistrict"
                               value="${branch.district}">
                        <div class="form-group">

                            <label>Tỉnh / Thành phố</label>

                            <select id="city"
                                    name="city">

                                <option value="${branch.city}">
                                    Chọn tỉnh thành
                                </option>
                            </select>
                            <c:if test="${not empty errors.city}">
                                <span style="color:red; font-size:13px; margin-top:5px;">${errors.city}</span>
                            </c:if>
                        </div>

                        <div class="form-group">
                            <label>Quận / Huyện</label>
                            <select id="district"
                                    name="district">
                                <option value="${branch.district}">
                                    Chọn quận huyện
                                </option>
                            </select>
                            <c:if test="${not empty errors.district }">
                                <span style="color:red; font-size:13px; margin-top:5px;">${errors.district}</span>
                            </c:if>
                        </div>

                        <div class="form-group full-width">
                            <label>Trạng thái</label>

                            <c:set var="branchStatus"
                                   value="${branch != null ? fn:toLowerCase(branch.status) : 'active'}"/> 

                            <div class="radio-group">
                                <label>
                                    <input type="radio"
                                           name="status"
                                           value="ACTIVE"
                                           ${branchStatus == 'active' ? 'checked' : ''}>

                                    Hoạt động
                                </label>

                                <label>
                                    <input type="radio"
                                           name="status"
                                           value="INACTIVE"
                                           ${branchStatus != 'active' ? 'checked' : ''}>
                                    Tạm khóa
                                </label>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- HÌNH ẢNH -->

                <h2>Hình ảnh chi nhánh</h2>

                <div class="form-group">

                    <label>Ảnh chi nhánh</label>

                    <input
                        id="imageInput"
                        type="file"
                        name="image"
                        accept="image/*">

                    <c:if test="${not empty errors.image}">
                        <span style="color:red;font-size:13px;">
                            ${errors.image}
                        </span>
                    </c:if>

                    <div class="branch-preview" style="margin-top: 15px;">
                        <img
                            id="previewImage"
                            src="${pageContext.request.contextPath}/assets/images/images_branch/${branch.imageUrl}"
                            alt="${branch.branchName}"
                            style="${empty branch.imageUrl ? 'display: none;' : ''}; max-width: 220px; border-radius: 12px; border: 1px solid #ddd;">
                    </div>

                </div>

                <!-- BUTTON -->

                <div class="form-actions">

                    <a href="branch?action=list&page=${page}&sizeValue=${sizeValue}&keyword=${keyword} &status=${status}&city=${city}"
                       class="btn-cancel">
                        Hủy
                    </a>  

                    <button type="submit"
                            class="btn-save">
                        Lưu chi nhánh
                    </button>
                </div>
            </form>             
        </div>

        <script>
            document.addEventListener("DOMContentLoaded", function () {
                const citySelect = document.getElementById("city");
                const districtSelect = document.getElementById("district");
                citySelect.addEventListener("change", loadDistricts);
                loadProvinces();

                async function loadProvinces() {
                    try {
                        const response = await fetch("https://provinces.open-api.vn/api/p/");
                        const data = await response.json();
                        const selectedCity = document.getElementById("selectedCity").value;

                        data.forEach(province => {
                            const option = document.createElement("option");

                            option.value = province.name;
                            option.textContent = province.name;
                            option.dataset.code = province.code;

                            if (province.name === selectedCity) {
                                option.selected = true;
                            }

                            citySelect.appendChild(option);
                        });
                        if (selectedCity) {
                            await loadDistricts();
                        }
                    } catch (error) {
                        console.error("Lỗi tải tỉnh thành:", error);
                    }
                }

                async function loadDistricts() {
                    districtSelect.innerHTML = '<option value="">Chọn quận huyện</option>';
                    const selectedDistrict = document.getElementById("selectedDistrict").value;
                    const provinceCode = citySelect.options[citySelect.selectedIndex].dataset.code;
                    if (!provinceCode)
                        return;
                    try {
                        const url = "https://provinces.open-api.vn/api/p/" + provinceCode + "?depth=2";
                        const response = await fetch(url);
                        const province = await response.json();
                        if (!province.districts || province.districts.length === 0)
                            return;
                        province.districts.forEach(district => {
                            const option = document.createElement("option");

                            option.value = district.name;
                            option.textContent = district.name;

                            if (district.name === selectedDistrict) {
                                option.selected = true;
                            }

                            districtSelect.appendChild(option);
                        });
                    } catch (error) {
                        console.error("Lỗi tải quận huyện:", error);
                    }
                }
                const imageInput = document.getElementById("imageInput");
                const previewImage = document.getElementById("previewImage");
                imageInput.addEventListener("change", function () {
                    const file = this.files[0];
                    if (!file) {
                        return;
                    }
                    // Tạo đường dẫn tạm thời từ file ảnh vừa chọn trong máy tính
                    previewImage.src = URL.createObjectURL(file);
                    // Chuyển trạng thái thẻ img từ ẩn sang hiện để người dùng nhìn thấy ảnh
                    previewImage.style.display = 'block';
                });
            });
        </script>
        </div>
    </main>
</div>
<jsp:include page="/views/common/footer.jsp"/>
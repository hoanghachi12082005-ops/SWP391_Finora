/*
====================================================
FINORA RETAIL REGISTER
====================================================
*/

let currentStep = 1;

/*
====================================================
DANH SÁCH TỈNH THÀNH
====================================================
*/

const provinces = [
    "Hà Nội",
    "TP Hồ Chí Minh",
    "Đà Nẵng",
    "Hải Phòng",
    "Cần Thơ",
    "An Giang",
    "Bà Rịa - Vũng Tàu",
    "Bắc Giang",
    "Bắc Kạn",
    "Bạc Liêu",
    "Bắc Ninh",
    "Bến Tre",
    "Bình Định",
    "Bình Dương",
    "Bình Phước",
    "Bình Thuận",
    "Cà Mau",
    "Cao Bằng",
    "Đắk Lắk",
    "Đắk Nông",
    "Điện Biên",
    "Đồng Nai",
    "Đồng Tháp",
    "Gia Lai",
    "Hà Giang",
    "Hà Nam",
    "Hà Tĩnh",
    "Hải Dương",
    "Hậu Giang",
    "Hòa Bình",
    "Hưng Yên",
    "Khánh Hòa",
    "Kiên Giang",
    "Kon Tum",
    "Lai Châu",
    "Lâm Đồng",
    "Lạng Sơn",
    "Lào Cai",
    "Long An",
    "Nam Định",
    "Nghệ An",
    "Ninh Bình",
    "Ninh Thuận",
    "Phú Thọ",
    "Phú Yên",
    "Quảng Bình",
    "Quảng Nam",
    "Quảng Ngãi",
    "Quảng Ninh",
    "Quảng Trị",
    "Sóc Trăng",
    "Sơn La",
    "Tây Ninh",
    "Thái Bình",
    "Thái Nguyên",
    "Thanh Hóa",
    "Thừa Thiên Huế",
    "Tiền Giang",
    "Trà Vinh",
    "Tuyên Quang",
    "Vĩnh Long",
    "Vĩnh Phúc",
    "Yên Bái"
];

/*
====================================================
LOAD DATA
====================================================
*/

document.addEventListener("DOMContentLoaded", function () {

    loadProvinces();

});

/*
====================================================
LOAD PROVINCES
====================================================
*/

function loadProvinces() {

    const provinceSelect =
            document.getElementById("province");

    if (!provinceSelect)
        return;

    provinces.forEach(province => {

        const option =
                document.createElement("option");

        option.value = province;
        option.textContent = province;

        provinceSelect.appendChild(option);

    });

}

/*
====================================================
CHUYỂN STEP
====================================================
*/

function nextStep(step) {

    /*
     STEP 1 -> STEP 2
     */
    if (step === 2) {

        if (!validateStep1()) {
            return;
        }
    }

    /*
     STEP 2 -> STEP 3
     */
    if (step === 3) {

        const industry =
                document.getElementById("selectedIndustry");

        if (!industry || industry.value.trim() === "") {

            alert("Vui lòng chọn ngành hàng.");

            return;
        }
    }

    showStep(step);

}

/*
====================================================
SHOW STEP
====================================================
*/

function showStep(step) {

    currentStep = step;

    /*
     ẨN TOÀN BỘ STEP
     */
    document.querySelectorAll(".wizard-content")
            .forEach(item => {

                item.classList.remove("active");

            });

    /*
     HIỆN STEP ĐƯỢC CHỌN
     */
    const current =
            document.getElementById("step" + step);

    if (current) {

        current.classList.add("active");

    }

    /*
     STEP INDICATOR
     */
    document.querySelectorAll(".step-item")
            .forEach(item => {

                item.classList.remove("active");

            });

    for (let i = 1; i <= step; i++) {

        const indicator =
                document.getElementById("indicator" + i);

        if (indicator) {

            indicator.classList.add("active");

        }
    }

}

/*
====================================================
VALIDATE STEP 1
====================================================
*/

function validateStep1() {

    const fullName =
            document.getElementById("fullName");

    const phone =
            document.getElementById("phone");

    const email =
            document.getElementById("email");

    const province =
            document.getElementById("province");

    /*
     HỌ TÊN
     */
    if (!fullName || fullName.value.trim() === "") {

        alert("Vui lòng nhập họ và tên.");

        fullName.focus();

        return false;
    }

    /*
     PHONE
     */
    if (!phone || phone.value.trim() === "") {

        alert("Vui lòng nhập số điện thoại.");

        phone.focus();

        return false;
    }

    const phoneRegex = /^[0-9]{10,11}$/;

    if (!phoneRegex.test(phone.value.trim())) {

        alert("Số điện thoại không hợp lệ.");

        phone.focus();

        return false;
    }

    /*
     EMAIL
     */
    if (!email || email.value.trim() === "") {

        alert("Vui lòng nhập email.");

        email.focus();

        return false;
    }

    const emailRegex =
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailRegex.test(email.value.trim())) {

        alert("Email không hợp lệ.");

        email.focus();

        return false;
    }

    /*
     PROVINCE
     */
    if (!province || province.value === "") {

        alert("Vui lòng chọn tỉnh/thành phố.");

        province.focus();

        return false;
    }

    return true;

}

/*
====================================================
CHỌN NGÀNH HÀNG
====================================================
*/

function selectIndustry(industryName) {

    const industryInput =
            document.getElementById("selectedIndustry");

    if (industryInput) {

        industryInput.value = industryName;

    }

    nextStep(3);

}

/*
====================================================
ẨN HIỆN PASSWORD
====================================================
*/

function togglePassword() {

    const password =
            document.getElementById("password");

    if (!password)
        return;

    if (password.type === "password") {

        password.type = "text";

    } else {

        password.type = "password";

    }

}

/*
====================================================
SUBMIT
====================================================
*/

function submitRegister() {

    const shopName = document.getElementById("shopName");
    const password = document.getElementById("password");

    if (!shopName || shopName.value.trim() === "") {
        alert("Vui lòng nhập tên cửa hàng.");
        return;
    }

    if (!password || password.value.trim().length < 6) {
        alert("Mật khẩu tối thiểu 6 ký tự.");
        return;
    }

    document.getElementById("registerForm").submit();
}
/*
====================================================
BUTTON STEP 4
====================================================
*/

document.addEventListener("click", function (e) {

    if (e.target.innerText === "Bắt đầu quản lý") {

        window.location.href = "login";

    }

});
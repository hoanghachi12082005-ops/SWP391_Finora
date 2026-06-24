<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Đăng nhập"/>
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login-custom.css">

<div class="container-fluid p-0 login-container">
    <div class="row g-0 min-vh-100">

        <!-- Left Side -->
        <div class="col-md-6 d-none d-md-flex login-banner">
            <div class="overlay"></div>

            <div class="banner-content">
                <h1>
                    Quản trị toàn diện.<br>
                    Vận hành tối ưu.
                </h1>

                <p>
                    Hệ thống bán lẻ chuyên nghiệp dành cho doanh nghiệp hiện đại.
                    Bảo mật, nhanh chóng và chính xác.
                </p>
            </div>
        </div>

        <!-- Right Side -->
        <div class="col-12 col-md-6 login-form-wrapper">

            <div class="login-card">

                <div class="text-center mb-4">
                    <div class="brand-icon">
                        <span class="material-icons">store</span>
                    </div>

                    <h2 class="brand-title">FinoraRetail</h2>

                    <p class="text-muted">
                        Đăng nhập để truy cập hệ thống quản trị
                    </p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger login-alert">
                        <i class="material-icons me-2">error</i>
                        ${error}
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login" autocomplete="off">

                    <!-- Username -->
                    <div class="mb-3">
                        <label class="form-label">
                            Email hoặc Số điện thoại
                        </label>

                        <div class="input-group login-input-group">
                            <span class="input-group-text">
                                <i class="material-icons">contact_mail</i>
                            </span>

                            <input type="text" id="username" name="username" class="form-control" placeholder="Nhập email hoặc số điện thoại" value="${username}" required autofocus>
                        </div>
                    </div>

                    <!-- Password -->
                    <div class="mb-3">
                        <label class="form-label">
                            Mật khẩu
                        </label>

                        <div class="input-group login-input-group">
                            <span class="input-group-text">
                                <i class="material-icons">lock_open</i>
                            </span>

                            <input type="password" id="password" name="password" class="form-control" placeholder="Nhập mật khẩu" required>

                            <button type="button" id="togglePassword" class="btn btn-light border-0">
                                <i class="material-icons">visibility</i>
                            </button>
                        </div>
                    </div>

                    <!-- Remember -->
                    <div class="d-flex justify-content-between align-items-center mb-4">

                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="remember-me" name="remember-me" ${rememberMe ? 'checked' : ''}>

                            <label class="form-check-label" for="remember-me">
                                Ghi nhớ đăng nhập
                            </label>
                        </div>

                        <a href="${pageContext.request.contextPath}/forgot-password" class="forgot-link">
                            Quên mật khẩu?
                        </a>

                    </div>

                    <button type="submit" class="btn login-btn w-100">
                        Đăng nhập
                    </button>

                </form>


            </div>
        </div>
    </div>
</div>

<script>
    const toggleBtn = document.getElementById("togglePassword");
    const password = document.getElementById("password");

    toggleBtn.addEventListener("click", () => {
        const icon = toggleBtn.querySelector("i");

        if (password.type === "password") {
            password.type = "text";
            icon.textContent = "visibility_off";
        } else {
            password.type = "password";
            icon.textContent = "visibility";
        }
    });
</script>

<jsp:include page="../common/footer.jsp"/>
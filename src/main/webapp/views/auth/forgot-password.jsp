<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Quên mật khẩu"/>
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/forgot-password.css">

<div class="container-fluid p-0 forgot-container">

    <div class="row g-0 min-vh-100">

        <!-- LEFT SIDE -->
        <div class="col-md-6 d-none d-md-flex forgot-left">

            <div class="forgot-overlay"></div>

            <div class="forgot-left-content">

                <div class="brand-area">

                    <div class="brand-icon">
                        <span class="material-icons">storefront</span>
                    </div>

                    <h2>FinoraRetail</h2>

                </div>

                <h1>
                    Khôi phục quyền truy cập<br>
                    hệ thống quản lý
                </h1>

                <p>
                    Xác minh thông tin tài khoản để nhận mã OTP
                    và thiết lập mật khẩu mới một cách an toàn.
                </p>

            </div>

        </div>

        <!-- RIGHT SIDE -->
        <div class="col-12 col-md-6 forgot-right">

            <div class="forgot-card">

                <div class="text-center mb-4">

                    <div class="forgot-card-icon">
                        <span class="material-icons">
                            lock_reset
                        </span>
                    </div>

                    <h2 class="forgot-title">
                        Quên mật khẩu
                    </h2>

                    <p class="forgot-subtitle">
                        Nhập thông tin để nhận mã OTP xác thực
                    </p>

                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger border-0 shadow-sm">

                        <div class="d-flex align-items-center">

                            <span class="material-icons me-2">
                                error
                            </span>

                            ${error}

                        </div>

                    </div>
                </c:if>

                <form method="post"
                      action="${pageContext.request.contextPath}/forgot-password">

                    <input type="hidden" name="action" value="send-otp">

                    <div class="mb-3">

                        <label for="fullName" class="form-label">Họ và tên nhân viên</label>

                        <div class="input-group custom-group">

                            <span class="input-group-text">
                                <span class="material-icons">
                                    person
                                </span>
                            </span>

                            <input type="text" id="fullName" name="fullName" class="form-control" placeholder="Nhập họ và tên" required>

                        </div>

                    </div>

                    <div class="mb-4">

                        <label for="email" class="form-label">Email đã đăng ký</label>

                        <div class="input-group custom-group">

                            <span class="input-group-text">
                                <span class="material-icons">
                                    mail
                                </span>
                            </span>

                            <input type="email" id="email" name="email" class="form-control" placeholder="example@gmail.com" required>

                        </div>

                    </div>

                    <button type="submit" class="btn btn-finora w-100">
                        Gửi mã OTP
                    </button>

                </form>

                <div class="text-center mt-4">

                    <a href="${pageContext.request.contextPath}/login" class="back-login">
                        ← Quay lại đăng nhập
                    </a>

                </div>

            </div>

        </div>

    </div>

</div>

<jsp:include page="../common/footer.jsp"/>

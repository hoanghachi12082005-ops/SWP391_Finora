    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <!-- jQuery (optional, for easier DOM manipulation) -->
    <script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>

    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <!-- Global Session Messages Handler -->
    <c:set var="globalMsg" value="${not empty sessionScope.message ? sessionScope.message : requestScope.message}" />
    <c:set var="globalSuccessMsg" value="${not empty sessionScope.successMessage ? sessionScope.successMessage : requestScope.successMessage}" />
    <c:set var="globalErr" value="${not empty sessionScope.error ? sessionScope.error : requestScope.error}" />
    <c:set var="globalErrMsg" value="${not empty sessionScope.errorMessage ? sessionScope.errorMessage : requestScope.errorMessage}" />

    <c:if test="${not empty globalMsg}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                Swal.fire({
                    icon: 'success',
                    title: 'Thành công',
                    text: '${globalMsg}',
                    confirmButtonColor: '#10b981',
                    timer: 3000,
                    timerProgressBar: true
                });
            });
        </script>
        <c:remove var="message" scope="session" />
    </c:if>
    <c:if test="${not empty globalSuccessMsg}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                Swal.fire({
                    icon: 'success',
                    title: 'Thành công',
                    text: '${globalSuccessMsg}',
                    confirmButtonColor: '#10b981',
                    timer: 3000,
                    timerProgressBar: true
                });
            });
        </script>
        <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty globalErr}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                Swal.fire({
                    icon: 'error',
                    title: 'Đã xảy ra lỗi',
                    text: '${globalErr}',
                    confirmButtonColor: '#dc3545'
                });
            });
        </script>
        <c:remove var="error" scope="session" />
    </c:if>
    <c:if test="${not empty globalErrMsg}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                Swal.fire({
                    icon: 'error',
                    title: 'Đã xảy ra lỗi',
                    text: '${globalErrMsg}',
                    confirmButtonColor: '#dc3545'
                });
            });
        </script>
        <c:remove var="errorMessage" scope="session" />
    </c:if>

    <!-- Custom JS -->
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>

    <c:if test="${param.additionalJS != null}">
        <script src="${pageContext.request.contextPath}/assets/js/${param.additionalJS}"></script>
    </c:if>
</body>
</html>

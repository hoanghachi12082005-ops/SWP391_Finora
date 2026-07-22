<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    
    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <!-- Global Session Messages Handler -->
    <script>
        const Toast = Swal.mixin({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true,
            showCloseButton: true,
            didOpen: (toast) => {
                toast.onmouseenter = Swal.stopTimer;
                toast.onmouseleave = Swal.resumeTimer;
            }
        });
    </script>
    <c:if test="${not empty message or not empty sessionScope.message}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${not empty message ? message : sessionScope.message}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'success',
                        title: msg
                    });
                }
            });
        </script>
        <c:remove var="message" scope="session" />
        <c:remove var="message" scope="request" />
        <c:remove var="message" />
    </c:if>
    <c:if test="${not empty successMessage or not empty sessionScope.successMessage}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${not empty successMessage ? successMessage : sessionScope.successMessage}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'success',
                        title: msg
                    });
                }
            });
        </script>
        <c:remove var="successMessage" scope="session" />
        <c:remove var="successMessage" scope="request" />
        <c:remove var="successMessage" />
    </c:if>
    <c:if test="${not empty error or not empty sessionScope.error}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${not empty error ? error : sessionScope.error}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'error',
                        title: msg
                    });
                }
            });
        </script>
        <c:remove var="error" scope="session" />
        <c:remove var="error" scope="request" />
        <c:remove var="error" />
    </c:if>
    <c:if test="${not empty errorMessage or not empty sessionScope.errorMessage}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${not empty errorMessage ? errorMessage : sessionScope.errorMessage}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'error',
                        title: msg
                    });
                }
            });
        </script>
        <c:remove var="errorMessage" scope="session" />
        <c:remove var="errorMessage" scope="request" />
        <c:remove var="errorMessage" />
    </c:if>
    <c:if test="${not empty warning or not empty sessionScope.warning}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${not empty warning ? warning : sessionScope.warning}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'warning',
                        title: msg,
                        timer: 5000
                    });
                }
            });
        </script>
        <c:remove var="warning" scope="session" />
        <c:remove var="warning" scope="request" />
        <c:remove var="warning" />
    </c:if>
    <c:if test="${not empty warningMessage or not empty sessionScope.warningMessage}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${not empty warningMessage ? warningMessage : sessionScope.warningMessage}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'warning',
                        title: msg,
                        timer: 5000
                    });
                }
            });
        </script>
        <c:remove var="warningMessage" scope="session" />
        <c:remove var="warningMessage" scope="request" />
        <c:remove var="warningMessage" />
    </c:if>

    <!-- Custom JS -->
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>

    <c:if test="${param.additionalJS != null}">
        <script src="${pageContext.request.contextPath}/assets/js/${param.additionalJS}"></script>
    </c:if>
</body>
</html>

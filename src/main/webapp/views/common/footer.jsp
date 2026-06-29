<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <!-- jQuery (optional, for easier DOM manipulation) -->
    <script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>

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
    <c:if test="${not empty message}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${message}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'success',
                        title: msg
                    });
                }
            });
        </script>
        <c:remove var="message" scope="session" />
    </c:if>
    <c:if test="${not empty successMessage}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${successMessage}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'success',
                        title: msg
                    });
                }
            });
        </script>
        <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty error}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${error}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'error',
                        title: msg
                    });
                }
            });
        </script>
        <c:remove var="error" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var msg = '${sessionScope.errorMessage}'.trim();
                if (msg && msg !== 'null') {
                    Toast.fire({
                        icon: 'error',
                        title: msg
                    });
                }
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

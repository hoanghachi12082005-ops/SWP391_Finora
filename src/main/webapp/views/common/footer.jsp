<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
        <div id="swalMessageMsg" style="display:none;"><c:out value="${not empty message ? message : sessionScope.message}"/></div>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var el = document.getElementById("swalMessageMsg");
                var msg = el ? el.textContent.trim() : '';
                if (msg && msg !== 'null') {
                    Toast.fire({ icon: 'success', title: msg });
                }
            });
        </script>
        <c:remove var="message" scope="session" />
        <c:remove var="message" scope="request" />
        <c:remove var="message" />
    </c:if>
    <c:if test="${not empty successMessage or not empty sessionScope.successMessage}">
        <div id="swalSuccessMsg" style="display:none;"><c:out value="${not empty successMessage ? successMessage : sessionScope.successMessage}"/></div>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var el = document.getElementById("swalSuccessMsg");
                var msg = el ? el.textContent.trim() : '';
                if (msg && msg !== 'null') {
                    Toast.fire({ icon: 'success', title: msg });
                }
            });
        </script>
        <c:remove var="successMessage" scope="session" />
        <c:remove var="successMessage" scope="request" />
        <c:remove var="successMessage" />
    </c:if>
    <c:if test="${not empty error or not empty sessionScope.error}">
        <div id="swalErrorMsg" style="display:none;"><c:out value="${not empty error ? error : sessionScope.error}"/></div>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var el = document.getElementById("swalErrorMsg");
                var msg = el ? el.textContent.trim() : '';
                if (msg && msg !== 'null') {
                    Toast.fire({ icon: 'error', title: msg });
                }
            });
        </script>
        <c:remove var="error" scope="session" />
        <c:remove var="error" scope="request" />
        <c:remove var="error" />
    </c:if>
    <c:if test="${not empty errorMessage or not empty sessionScope.errorMessage}">
        <div id="swalErrorMessageMsg" style="display:none;"><c:out value="${not empty errorMessage ? errorMessage : sessionScope.errorMessage}"/></div>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var el = document.getElementById("swalErrorMessageMsg");
                var msg = el ? el.textContent.trim() : '';
                if (msg && msg !== 'null') {
                    Toast.fire({ icon: 'error', title: msg });
                }
            });
        </script>
        <c:remove var="errorMessage" scope="session" />
        <c:remove var="errorMessage" scope="request" />
        <c:remove var="errorMessage" />
    </c:if>
    <c:if test="${not empty warning or not empty sessionScope.warning}">
        <div id="swalWarningMsg" style="display:none;"><c:out value="${not empty warning ? warning : sessionScope.warning}"/></div>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var el = document.getElementById("swalWarningMsg");
                var msg = el ? el.textContent.trim() : '';
                if (msg && msg !== 'null') {
                    Toast.fire({ icon: 'warning', title: msg, timer: 5000 });
                }
            });
        </script>
        <c:remove var="warning" scope="session" />
        <c:remove var="warning" scope="request" />
        <c:remove var="warning" />
    </c:if>
    <c:if test="${not empty warningMessage or not empty sessionScope.warningMessage}">
        <div id="swalWarningMessageMsg" style="display:none;"><c:out value="${not empty warningMessage ? warningMessage : sessionScope.warningMessage}"/></div>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                var el = document.getElementById("swalWarningMessageMsg");
                var msg = el ? el.textContent.trim() : '';
                if (msg && msg !== 'null') {
                    Toast.fire({ icon: 'warning', title: msg, timer: 5000 });
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

    <!-- Flatpickr JS -->
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <script src="https://npmcdn.com/flatpickr/dist/l10n/vn.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            flatpickr("input[type='date']", {
                altInput: true,
                altFormat: "d/m/Y",
                dateFormat: "Y-m-d",
                locale: "vn",
                allowInput: true
            });
        });
    </script>
</body>
</html>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Thêm nhà cung cấp"/>
</jsp:include>

<div class="container py-4">

    <div class="card shadow-sm">

        <div class="card-header bg-danger text-white">
            Thêm nhà cung cấp
        </div>

        <div class="card-body">

            <form method="post" action="suppliers">

                <input type="hidden"
                       name="action"
                       value="create">

                <div class="mb-3">

                    <label>Tên nhà cung cấp</label>

                    <input type="text"
                           name="name"
                           class="form-control"
                           required>

                </div>

                <div class="mb-3">

                    <label>Số điện thoại</label>

                    <input type="text"
                           name="phone"
                           class="form-control"
                           required>

                </div>

                <div class="mb-3">

                    <label>Địa chỉ</label>

                    <textarea name="address"
                              class="form-control"
                              rows="3"></textarea>

                </div>

                <div class="mb-3">

                    <label>Trạng thái</label>

                    <select name="status"
                            class="form-select">

                        <option value="active">
                            Active
                        </option>

                        <option value="inactive">
                            Inactive
                        </option>

                    </select>

                </div>

                <button class="btn btn-danger">
                    Lưu
                </button>

                <a href="suppliers"
                   class="btn btn-secondary">
                    Quay lại
                </a>

            </form>

        </div>

    </div>

</div>

<jsp:include page="../common/footer.jsp"/>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<div id="deleteModal"
     class="modal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="deleteModalTitle">

    <div class="modal-content modal-content--delete">

        <h2 id="deleteModalTitle">Xác nhận xóa chi nhánh</h2>

        <p class="modal-text">
            Bạn sắp xóa chi nhánh:
            <strong id="deleteBranchName"></strong>
        </p>

        <p class="modal-text modal-text--hint">
            Hành động này không thể hoàn tác. Nhập
            <code class="confirm-phrase">XAC NHAN</code>
            (viết hoa, không dấu) để tiếp tục:
        </p>

        <input type="text"
               id="deleteConfirmInput"
               class="delete-confirm-input"
               placeholder="Nhập XAC NHAN"
               autocomplete="off"
               spellcheck="false">

        <p id="deleteConfirmError" class="delete-confirm-error" hidden>
            Vui lòng nhập đúng <strong>XAC NHAN</strong>.
        </p>

        <div class="modal-actions">

            <button type="button"
                    class="modal-btn-cancel"
                    onclick="closeDeleteModal()">
                Hủy
            </button>

            <button type="button"
                    id="deleteConfirmBtn"
                    class="btn-delete-confirm"
                    disabled>
                Xóa vĩnh viễn
            </button>
        </div>
    </div>
</div>

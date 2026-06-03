/**
 * Category Management Module - JavaScript
 */

(function() {
    'use strict';

    /**
     * Open Add Category Modal
     */
    window.openAddModal = function() {
        var modal = document.getElementById('categoryModal');
        if (!modal) return;

        // Reset all form fields
        document.getElementById('modalAction').value = 'add';
        document.getElementById('editCategoryId').value = '';
        document.getElementById('inputName').value = '';
        document.getElementById('inputDescription').value = '';
        document.getElementById('inputParentName').value = '';
        document.getElementById('inputStatus').value = 'active';

        // Update modal header for Add mode
        document.getElementById('modalTitle').textContent = 'Thêm nhóm hàng mới';
        document.getElementById('modalSubtitle').textContent = 'Tạo danh mục phân loại sản phẩm';
        
        var modalIcon = document.getElementById('modalIcon');
        modalIcon.className = 'cat-modal-icon add';
        modalIcon.innerHTML = '<span class="material-icons">create_new_folder</span>';

        // Show modal
        modal.style.display = 'block';
    };

    /**
     * Open Edit Category Modal
     */
    window.openEditModal = function(id, name, description, parentName, status) {
        var modal = document.getElementById('categoryModal');
        if (!modal) return;

        // Fill form fields with category data
        document.getElementById('modalAction').value = 'update';
        document.getElementById('editCategoryId').value = id;
        document.getElementById('inputName').value = name || '';
        document.getElementById('inputDescription').value = description || '';
        document.getElementById('inputStatus').value = status || 'active';

        // Set parent dropdown to match category's parent
        var parentSelect = document.getElementById('inputParentName');
        var parentValue = parentName || '';
        for (var i = 0; i < parentSelect.options.length; i++) {
            if (parentSelect.options[i].value === parentValue) {
                parentSelect.selectedIndex = i;
                break;
            }
        }

        // Update modal header for Edit mode
        document.getElementById('modalTitle').textContent = 'Cập nhật nhóm hàng';
        document.getElementById('modalSubtitle').textContent = 'Chỉnh sửa thông tin danh mục';
        
        var modalIcon = document.getElementById('modalIcon');
        modalIcon.className = 'cat-modal-icon edit';
        modalIcon.innerHTML = '<span class="material-icons">edit_note</span>';

        // Show modal
        modal.style.display = 'block';
    };

    /**
     * Close Modal
     */
    window.closeModal = function() {
        var modal = document.getElementById('categoryModal');
        if (modal) {
            modal.style.display = 'none';
        }
    };

    /**
     * Open Print View in new window
     */
    window.openPrintView = function() {
        var url = new URL(window.location.href);
        url.searchParams.delete('page');
        url.searchParams.set('printMode', 'true');
        window.open(url.toString(), '_blank');
    };

    // ==================== INITIALIZATION ====================

    document.addEventListener('DOMContentLoaded', function() {
        // Add print mode class if URL has printMode=true
        var urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('printMode') === 'true') {
            document.body.classList.add('cat-print-mode');
        }

        // Close modal when clicking on overlay background
        var modal = document.getElementById('categoryModal');
        if (modal) {
            modal.addEventListener('click', function(event) {
                if (event.target === modal) {
                    closeModal();
                }
            });
        }

        // Close modal on Escape key press
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                closeModal();
            }
        });
    });

})();

/**
 * Main JavaScript for KiotRetail / FinoraRetail
 */

document.addEventListener('DOMContentLoaded', function() {
    // Auto-dismiss toast messages after 4 seconds
    const toasts = document.querySelectorAll('.cat-toast');
    toasts.forEach(function(toast) {
        setTimeout(function() {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(-10px)';
            toast.style.transition = 'all 0.4s ease';
            
            setTimeout(function() {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 400);
        }, 4000);
    });
});

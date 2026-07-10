/**
 * Main JavaScript for KiotRetail / FinoraRetail
 */

// Monkey-patch window.fetch to automatically include CSRF token header for all POST requests
(function() {
    const originalFetch = window.fetch;
    window.fetch = function(url, options) {
        if (options && options.method && options.method.toUpperCase() === 'POST') {
            const token = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
            if (token) {
                options.headers = options.headers || {};
                if (options.headers instanceof Headers) {
                    if (!options.headers.has('X-CSRF-Token')) {
                        options.headers.append('X-CSRF-Token', token);
                    }
                } else if (Array.isArray(options.headers)) {
                    if (!options.headers.some(h => h[0].toLowerCase() === 'x-csrf-token')) {
                        options.headers.push(['X-CSRF-Token', token]);
                    }
                } else {
                    let hasToken = false;
                    for (let key in options.headers) {
                        if (key.toLowerCase() === 'x-csrf-token') {
                            hasToken = true;
                            break;
                        }
                    }
                    if (!hasToken) {
                        options.headers['X-CSRF-Token'] = token;
                    }
                }
            }
        }
        return originalFetch(url, options);
    };
})();

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

    // Automatically inject CSRF token hidden input on form submit for POST forms
    document.addEventListener('submit', function(event) {
        const form = event.target;
        if (form.tagName === 'FORM' && (form.method.toLowerCase() === 'post' || form.getAttribute('method')?.toLowerCase() === 'post')) {
            const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
            if (csrfToken) {
                let csrfInput = form.querySelector('input[name="csrfToken"]');
                if (!csrfInput) {
                    csrfInput = document.createElement('input');
                    csrfInput.type = 'hidden';
                    csrfInput.name = 'csrfToken';
                    form.appendChild(csrfInput);
                }
                csrfInput.value = csrfToken;
            }
        }
    });
});

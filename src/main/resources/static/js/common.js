// CSRF
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.content;
}
function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]')?.content;
}

// PWA Service Worker
if ('serviceWorker' in navigator && location.protocol === 'https:') {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js').catch(() => { /* swallow */ });
    });
}

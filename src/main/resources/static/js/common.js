// CSRF
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.content;
}
function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]')?.content;
}

// SALM Service Worker
// 정책:
//   - 정적 자원(/css, /js, /img, /favicon.ico): cache-first
//   - HTML 페이지: network-first (오프라인이면 캐시 폴백)
//   - 썸네일(/media/thumbnails/, /thumbnails/): stale-while-revalidate
//   - HLS 매니페스트/세그먼트(*.m3u8, *.ts), API: 항상 네트워크 (캐시 금지)

const VERSION = 'salm-v1';
const STATIC_CACHE = `${VERSION}-static`;
const HTML_CACHE = `${VERSION}-html`;
const IMG_CACHE = `${VERSION}-img`;

const STATIC_ASSETS = [
  '/',
  '/css/common.css',
  '/js/common.js',
  '/manifest.webmanifest',
  '/img/icon.svg',
];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(STATIC_CACHE).then((c) => c.addAll(STATIC_ASSETS)).catch(() => {}));
  self.skipWaiting();
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys.filter((k) => !k.startsWith(VERSION)).map((k) => caches.delete(k))
      )
    )
  );
  self.clients.claim();
});

function isHls(url) {
  return /\.(m3u8|ts|mp4)$/i.test(url.pathname);
}

function isApi(url) {
  return url.pathname.startsWith('/api/') || url.pathname.startsWith('/actuator/');
}

function isThumbnail(url) {
  return url.pathname.startsWith('/media/thumbnails/') || url.pathname.startsWith('/thumbnails/');
}

function isStaticAsset(url) {
  return (
    url.pathname.startsWith('/css/') ||
    url.pathname.startsWith('/js/') ||
    url.pathname.startsWith('/img/') ||
    url.pathname === '/favicon.ico' ||
    url.pathname === '/manifest.webmanifest'
  );
}

self.addEventListener('fetch', (event) => {
  const req = event.request;
  if (req.method !== 'GET') return;

  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;

  if (isHls(url) || isApi(url)) {
    return; // 기본 fetch (캐시 안 함)
  }

  if (isThumbnail(url)) {
    event.respondWith(
      caches.open(IMG_CACHE).then((cache) =>
        cache.match(req).then((cached) => {
          const network = fetch(req)
            .then((res) => {
              if (res.ok) cache.put(req, res.clone());
              return res;
            })
            .catch(() => cached);
          return cached || network;
        })
      )
    );
    return;
  }

  if (isStaticAsset(url)) {
    event.respondWith(
      caches.match(req).then((cached) => cached || fetch(req).then((res) => {
        const copy = res.clone();
        caches.open(STATIC_CACHE).then((c) => c.put(req, copy));
        return res;
      }))
    );
    return;
  }

  // HTML — network-first
  if (req.mode === 'navigate' || (req.headers.get('accept') || '').includes('text/html')) {
    event.respondWith(
      fetch(req)
        .then((res) => {
          const copy = res.clone();
          caches.open(HTML_CACHE).then((c) => c.put(req, copy));
          return res;
        })
        .catch(() => caches.match(req).then((cached) => cached || caches.match('/')))
    );
  }
});

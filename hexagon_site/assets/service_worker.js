var dataCacheName = 'template-pwa';
var cacheName = 'template-pwa';
var filesToCache = [
    '/',
    "./img",
    "./img/architecture.svg",
    "./img/logo.svg",
    "./img/logo_white.svg",
    "./img/logo_wide.svg",
    "./index.html",
    "./manifest.json",
    "./service_worker.js",
    "./favicon.ico",
    "./tile-large.png",
    "./tile-medium.png",
    "./tile-small.png",
    "./css",
    "./css/base.css",
    "./css/index.css",
    "./css/page.css"
];

self.addEventListener('install', function(e) {
    console.log('[ServiceWorker] Install');
    e.waitUntil(
        caches.open(cacheName).then(function(cache) {
            console.log('[ServiceWorker] Caching app shell');
            return cache.addAll(filesToCache);
        })
    );
});

self.addEventListener('activate', function(e) {
    console.log('[ServiceWorker] Activate');
    e.waitUntil(
        caches.keys().then(function(keyList) {
            return Promise.all(keyList.map(function(key) {
                if (key !== cacheName && key !== dataCacheName) {
                    console.log('[ServiceWorker] Removing old cache', key);
                    return caches.delete(key);
                }
            }));
        })
    );
    return self.clients.claim();
});

self.addEventListener('fetch', function(e) {
    console.log('[Service Worker] Fetch', e.request.url);
    e.respondWith(
        caches.match(e.request).then(function(response) {
            return response || fetch(e.request);
        })
    );
});

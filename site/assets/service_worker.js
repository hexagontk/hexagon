
var dataCacheName = 'hexagon_pwa';
var cacheName = dataCacheName;

var filesToCache = [
    '/',
    "/css/base.css",
    "/css/index.css",
    "/css/page.css",
    "/img/architecture.svg",
    "/img/logo.svg",
    "/img/logo_white.svg",
    "/img/logo_wide.svg",
    "/index.html",
    "/manifest.json",
    "/service_worker.js",
    "/favicon.ico",
    "/tile-large.png",
    "/tile-medium.png",
    "/tile-small.png"
];

self.addEventListener('install', function(e) {
    console.log('Worker Install');
    e.waitUntil(
        caches.open(cacheName).then(function(cache) {
            console.log('Worker Caching app shell');
            return cache.addAll(filesToCache);
        })
    );
});

self.addEventListener('activate', function(e) {
    console.log('Worker Activate');
    e.waitUntil(
        caches.keys().then(function(keyList) {
            return Promise.all(keyList.map(function(key) {
                if (key !== cacheName && key !== dataCacheName) {
                    console.log('Worker Removing old cache', key);
                    return caches.delete(key);
                }
            }));
        })
    );
    return self.clients.claim();
});

self.addEventListener('fetch', function(e) {
    console.log('Worker Fetch', e.request.url);
    e.respondWith(
        caches.match(e.request).then(function(response) {
            return response || fetch(e.request);
        })
    );
});

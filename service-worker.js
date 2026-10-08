const CACHE_NAME='aiweerhub-v76-32';
const APP_SHELL=[
 './','./index.html','./manifest.json',
 './app/part01.html','./app/part02.html','./app/part03.html','./app/part04.html',
 './app/part05.html','./app/part06.html','./app/part07.html','./app/part08.html',
 './icons/icon-192.svg','./icons/icon-512.svg'
];
self.addEventListener('install',e=>{e.waitUntil(caches.open(CACHE_NAME).then(c=>c.addAll(APP_SHELL)));self.skipWaiting()});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE_NAME).map(k=>caches.delete(k)))));self.clients.claim()});
self.addEventListener('fetch',e=>{
 if(e.request.method!=='GET')return;
 e.respondWith(fetch(e.request).then(r=>{
   const copy=r.clone();caches.open(CACHE_NAME).then(c=>c.put(e.request,copy));return r;
 }).catch(()=>caches.match(e.request)));
});
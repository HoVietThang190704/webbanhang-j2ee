(function () {
  'use strict';

  // Simple debounce
  function debounce(fn, wait) {
    var t;
    return function () {
      var args = arguments, ctx = this;
      clearTimeout(t);
      t = setTimeout(function () { fn.apply(ctx, args); }, wait || 250);
    };
  }

  // Suggest search: non-destructive minimal implementation so it won't break if server doesn't provide suggestion endpoint
  function suggestSearch(e) {
    var q = '';
    if (e && e.type === 'submit') return true; // allow form submit
    if (e && e.target) q = e.target.value || '';
    else q = (document.getElementById('skw') && document.getElementById('skw').value) || '';

    var results = document.getElementById('search-result');
    if (!results) return false;

    q = q.trim();
    if (q.length < 2) {
      results.classList.add('hidden');
      results.classList.remove('opacity-100');
      results.innerHTML = '';
      return false;
    }

    // Minimal placeholder UI for suggestions. Replace with real AJAX if available.
    results.innerHTML = '<div class="p-2 text-sm text-gray-700">Tìm: ' + escapeHtml(q) + '</div>';
    results.classList.remove('hidden');
    results.classList.remove('opacity-0');
    results.classList.add('opacity-100');

    return false; // prevent default for onkeyup suggestions
  }

  // Very small HTML escaper
  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, function (m) { return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;"}[m]; });
  }

  // Expose suggestSearch if not already defined
  if (!window.suggestSearch) window.suggestSearch = debounce(suggestSearch, 150);


  function onHeaderReady() {
    // attach handlers
    // show/hide mini cart popup on hover
    var cartLink = document.querySelector('a[href$="/cart"]');
    if (cartLink) {
      var view = cartLink.querySelector('.view-cart');
      cartLink.addEventListener('mouseenter', function () { if (view) view.classList.remove('hidden'); });
      cartLink.addEventListener('mouseleave', function () { if (view) view.classList.add('hidden'); });
    }

    // prevent click inside search-result from bubbling
    var sr = document.getElementById('search-result');
    if (sr) sr.addEventListener('click', function (e) { e.stopPropagation(); });
    // show/hide mini cart popup on hover
    var cartLink = document.querySelector('a[href$="/cart"]');
    if (cartLink) {
      var view = cartLink.querySelector('.view-cart');
      cartLink.addEventListener('mouseenter', function () { if (view) view.classList.remove('hidden'); });
      cartLink.addEventListener('mouseleave', function () { if (view) view.classList.add('hidden'); });
    }

    // close popup when clicking outside
    document.addEventListener('click', function (e) {
      var rem = document.querySelector('.remind_location');
      if (!rem) return;
      if (!rem.contains(e.target)) { rem.classList.add('hidden'); rem.classList.add('opacity-0'); }
    });

    // prevent click inside search-result from bubbling
    var sr = document.getElementById('search-result');
    if (sr) sr.addEventListener('click', function (e) { e.stopPropagation(); });
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', onHeaderReady); else onHeaderReady();

  // Expose for manual invocation/tests
  window.initHeader = onHeaderReady;
})();
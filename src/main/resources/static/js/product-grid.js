(function () {
  const updateButtonState = (btn, disabled) => {
    if (!btn) {
      return;
    }
    btn.disabled = disabled;
    btn.classList.toggle("opacity-50", disabled);
    btn.classList.toggle("pointer-events-none", disabled);
    btn.setAttribute("aria-disabled", String(disabled));
  };

  const initProductSlider = (root) => {
    const grid = root.querySelector("[data-product-grid]");
    const items = grid ? Array.from(grid.querySelectorAll("[data-product-item]")) : [];
    const perPage = Number(root.dataset.productsPerPage) || 10;
    const prevBtn = root.querySelector("[data-product-prev]");
    const nextBtn = root.querySelector("[data-product-next]");
    const indicator = root.querySelector("[data-product-indicator]");

    if (!grid || items.length === 0) {
      if (prevBtn) prevBtn.hidden = true;
      if (nextBtn) nextBtn.hidden = true;
      if (indicator) indicator.hidden = true;
      return;
    }

    const totalPages = Math.ceil(items.length / perPage);
    let currentPage = 0;

    const applyPage = () => {
      items.forEach((item, index) => {
        const pageIndex = Math.floor(index / perPage);
        item.style.display = pageIndex === currentPage ? "" : "none";
      });

      const atStart = currentPage === 0;
      const atEnd = currentPage >= totalPages - 1;
      updateButtonState(prevBtn, atStart);
      updateButtonState(nextBtn, atEnd);

      if (indicator) {
        indicator.textContent = `Trang ${currentPage + 1}/${totalPages}`;
      }
    };

    const goToPage = (page) => {
      const target = Math.min(Math.max(page, 0), totalPages - 1);
      if (target === currentPage) {
        return;
      }
      currentPage = target;
      applyPage();
    };

    if (totalPages <= 1) {
      if (prevBtn) prevBtn.hidden = true;
      if (nextBtn) nextBtn.hidden = true;
      if (indicator) indicator.hidden = true;
      return;
    }

    if (prevBtn) {
      prevBtn.hidden = false;
      prevBtn.addEventListener("click", () => goToPage(currentPage - 1));
    }

    if (nextBtn) {
      nextBtn.hidden = false;
      nextBtn.addEventListener("click", () => goToPage(currentPage + 1));
    }

    if (indicator) {
      indicator.hidden = false;
    }

    applyPage();
  };

  document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-product-slider]").forEach(initProductSlider);
  });
})();

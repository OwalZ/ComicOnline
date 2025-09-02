// App core interactions (búsqueda global, tema, utilidades UI)
(function () {
  const root = document.documentElement;
  const body = document.body;
  const THEME_KEY = "comicTheme";
  function applyTheme(t) {
    body.classList.forEach((c) => {
      if (c.startsWith("theme-")) body.classList.remove(c);
    });
    body.classList.add("theme-" + t);
  }
  const saved = localStorage.getItem(THEME_KEY) || "aurora";
  applyTheme(saved);
  // Fallback avatar
  document.querySelectorAll("img[data-avatar]").forEach((img) => {
    img.addEventListener("error", () => {
      const fb = img.parentElement?.querySelector("[data-avatar-fallback]");
      if (fb) fb.style.display = "";
      img.remove();
    });
  });

  // Búsqueda global (cliente simple por títulos existentes en la página)
  const globalSearch = document.getElementById("globalSearch");
  if (globalSearch) {
    // skip if removed from layout
    let resultsBox = null;
    function ensureBox() {
      if (resultsBox) return resultsBox;
      resultsBox = document.createElement("div");
      resultsBox.className = "app-search-results";
      globalSearch.parentElement.appendChild(resultsBox);
      return resultsBox;
    }
    function collectItems() {
      return Array.from(document.querySelectorAll(".card .card-title")).map(
        (el) => ({
          el,
          text: el.textContent.trim(),
          link: el.closest(".card")?.querySelector("a.stretched-link, a.btn"),
        })
      );
    }
    let cache = collectItems();
    let cacheTimer = null;
    function scheduleRefresh() {
      if (cacheTimer) clearTimeout(cacheTimer);
      cacheTimer = setTimeout(() => {
        cache = collectItems();
      }, 800);
    }
    const obs = new MutationObserver(scheduleRefresh);
    obs.observe(document.body, { childList: true, subtree: true });
    globalSearch.addEventListener("input", () => {
      const q = globalSearch.value.trim().toLowerCase();
      const box = ensureBox();
      if (!q) {
        box.innerHTML = "";
        box.style.display = "none";
        return;
      }
      const matches = cache
        .filter((i) => i.text.toLowerCase().includes(q))
        .slice(0, 8);
      if (!matches.length) {
        box.innerHTML = '<div class="empty text-dim">Sin resultados</div>';
        box.style.display = "block";
        return;
      }
      box.innerHTML = matches
        .map(
          (m) =>
            `<button type="button" class="res-item" data-href="${
              m.link?.getAttribute("href") || "#"
            }">${m.text}</button>`
        )
        .join("");
      box.style.display = "block";
    });
    document.addEventListener("click", (e) => {
      if (!resultsBox) return;
      if (resultsBox.contains(e.target)) {
        const btn = e.target.closest(".res-item");
        if (btn) {
          const href = btn.getAttribute("data-href");
          if (href && href !== "#") window.location.href = href;
        }
      } else if (!globalSearch.contains(e.target)) {
        resultsBox.style.display = "none";
      }
    });
    globalSearch.addEventListener("keydown", (e) => {
      if (e.key === "ArrowDown" && resultsBox && resultsBox.firstElementChild) {
        e.preventDefault();
        resultsBox.firstElementChild.focus();
      }
    });
  }
})();

(() => {
  const PAGE_SIZE = 20;
  const state = {
    page: 0,
    totalPages: 1,
    totalElements: 0,
    logs: []
  };

  const $ = (s) => document.querySelector(s);

  const flash = (msg, type = "error") => {
    const el = $("#flash");
    if (!el) return;
    el.textContent = msg;
    el.className = `flash ${type}`;
    el.hidden = false;
    setTimeout(() => {
      el.hidden = true;
    }, 3500);
  };

  const esc = (s) =>
    String(s ?? "").replace(/[&<>"']/g, (m) => (
      { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[m]
    ));

  async function fetchLogs(page = 0) {
    const headers = {
      ...window.__auth.authHeader(),
      "Content-Type": "application/json"
    };
    const url = `/api/admin/logs?page=${page}&size=${PAGE_SIZE}`;
    const res = await fetch(url, { headers });
    if (!res.ok) {
      const txt = await res.text();
      throw new Error(txt || "could_not_load_logs");
    }
    const data = await res.json();
    return data;
  }

  function renderLogs() {
    const wrap = $("#logsWrap");
    const countEl = $("#logsCount");
    wrap.innerHTML = "";

    if (!state.logs.length) {
      wrap.innerHTML = `<div class="card muted">Kayıt bulunamadı.</div>`;
      countEl.textContent = "";
      renderPager();
      return;
    }

    countEl.textContent = `${state.totalElements} kayıt`;

    const rows = state.logs.map((log) => {
      const ts = log.timestamp ? new Date(log.timestamp).toLocaleString() : "-";
      return `
        <tr>
          <td>${esc(log.id ?? "")}</td>
          <td>${esc(ts)}</td>
          <td>${esc(log.actor ?? "")}</td>
          <td>${esc(log.action ?? "")}</td>
          <td>${esc(log.details ?? "")}</td>
          <td>${esc(log.ip ?? "")}</td>
        </tr>
      `;
    }).join("");

    wrap.innerHTML = `
      <table class="logs-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Zaman</th>
            <th>Kullanıcı</th>
            <th>Eylem</th>
            <th>Detay</th>
            <th>IP</th>
          </tr>
        </thead>
        <tbody>
          ${rows}
        </tbody>
      </table>
    `;

    renderPager();
  }

  function renderPager() {
    const pager = $("#logsPager");
    pager.innerHTML = "";

    if ((state.totalPages || 1) <= 1) {
      return;
    }

    pager.innerHTML = `
      <button class="btn ghost first" ${state.page > 0 ? "" : "disabled"}>« İlk</button>
      <button class="btn ghost prev" ${state.page > 0 ? "" : "disabled"}>‹ Önceki</button>
      <span class="btn ghost" style="background:#fff;color:#3b4465;">Sayfa ${state.page + 1} / ${state.totalPages}</span>
      <button class="btn ghost next" ${state.page < state.totalPages - 1 ? "" : "disabled"}>Sonraki ›</button>
      <button class="btn ghost last" ${state.page < state.totalPages - 1 ? "" : "disabled"}>Son »</button>
    `;

    pager.querySelector(".first").onclick = () => loadPage(0);
    pager.querySelector(".prev").onclick = () => loadPage(Math.max(0, state.page - 1));
    pager.querySelector(".next").onclick = () => loadPage(Math.min(state.totalPages - 1, state.page + 1));
    pager.querySelector(".last").onclick = () => loadPage(state.totalPages - 1);
  }

  async function loadPage(page = 0) {
    try {
      const data = await fetchLogs(page);

      if (Array.isArray(data)) {
        state.logs = data;
        state.page = page;
        state.totalPages = 1;
        state.totalElements = data.length;
      } else {
        state.logs = Array.isArray(data.content) ? data.content : [];
        state.page = page;
        state.totalPages = Number(data.totalPages || 1);
        state.totalElements = Number(data.totalElements || state.logs.length);
      }

      renderLogs();
    } catch (e) {
      flash("Loglar yüklenemedi.");
      console.error(e);
    }
  }

  document.addEventListener("DOMContentLoaded", async () => {
    const a = window.__auth?.requireAuthOrRedirect();
    if (!a) return;

    if (!window.__auth.isAdmin()) {
      window.location.replace("/products.html");
      return;
    }

    $("#logoutBtn")?.addEventListener("click", window.__auth.logout);
    $("#reloadBtn")?.addEventListener("click", () => loadPage(state.page));

    await loadPage(0);
  });
})();

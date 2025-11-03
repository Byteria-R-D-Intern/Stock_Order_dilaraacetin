(() => {
  const $ = (s) => document.querySelector(s);

  const state = {
    all: [],
    page: 0,
    size: 20,
    filterUnread: "all",     // "all" | "unread"
    filterType: "ALL"        // ALL | ORDER | PAYMENT | PRODUCT | ACCOUNT
  };

  // fetch helper: boş 200/204 destekli
  async function api(url, opts = {}) {
    const res = await fetch(url, {
      ...opts,
      headers: { ...(opts.headers || {}), ...window.__auth.authHeader(), "Content-Type": "application/json" }
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    if (res.status === 204) return {};
    const ct = (res.headers.get("content-type") || "").toLowerCase();
    if (ct.includes("application/json")) return res.json();
    const txt = await res.text();
    try { return txt ? JSON.parse(txt) : {}; } catch { return {}; }
  }

  const flash = (msg, type="error") => {
    const el = $("#flash");
    el.textContent = msg;
    el.className = `flash ${type}`;
    el.hidden = false;
    setTimeout(()=> el.hidden = true, 2500);
  };

  const esc = (s) => String(s ?? "").replace(/[&<>"']/g, m => ({
    "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"
  }[m]));

  function groupByDate(list){
    const map = new Map();
    list.forEach(n => {
      const d = new Date(n.createdAt);
      const key = d.toLocaleDateString(undefined, { day:"2-digit", month:"long", year:"numeric" });
      if (!map.has(key)) map.set(key, []);
      map.get(key).push(n);
    });
    return [...map.entries()];
  }

  function paginate(list, page=0, size=20) {
    const total = list.length;
    const totalPages = Math.max(1, Math.ceil(total/size));
    const p = Math.min(Math.max(0,page), totalPages-1);
    return {
      page: p,
      size,
      totalElements: total,
      totalPages,
      hasPrevious: p > 0,
      hasNext: p < totalPages - 1,
      content: list.slice(p*size, p*size + size)
    };
  }

  function applyFilters() {
    let arr = [...state.all];
    if (state.filterUnread === "unread") arr = arr.filter(n => !n.read);
    if (state.filterType !== "ALL") arr = arr.filter(n => n.type === state.filterType);
    return arr;
  }

  async function loadAll() {
    try {
      const data = await api("/api/notifications");
      const list = Array.isArray(data?.content) ? data.content
                 : Array.isArray(data)          ? data
                 : [];
      state.all = list.map(x => ({
        id: x.id,
        type: x.type,
        title: x.title,
        message: x.message,
        read: !!x.read,
        createdAt: x.createdAt
      }));
      state.page = 0;
      render();
    } catch {
      flash("Failed to load notifications");
    }
  }

  function render() {
    const listEl = $("#list");
    const unreadBadge = $("#unreadBadge");
    const filtered = applyFilters();
    const meta = paginate(filtered, state.page, state.size);

    // unread badge
    const unread = state.all.filter(n => !n.read).length;
    unreadBadge.textContent = `Unread: ${unread}`;

    listEl.innerHTML = "";
    if (!meta.content.length) {
      listEl.innerHTML = `<div class="card muted">No notifications</div>`;
      renderPager(meta);
      return;
    }

    const groups = groupByDate(meta.content);
    groups.forEach(([day, items]) => {
      const grp = document.createElement("div");
      grp.className = "date-group";
      grp.innerHTML = `<div class="date-title">${day}</div>`;
      items.forEach(n => {
        const card = document.createElement("div");
        card.className = `note ${n.read ? "is-read" : ""}`;
        card.innerHTML = `
          <div class="note-top">
            <div class="note-meta">
              <span class="pill">${esc(n.type)}</span>
              <span class="time">${new Date(n.createdAt).toLocaleTimeString()}</span>
            </div>
            <div class="note-actions">
              <button class="btn ghost btn-read">Read</button>
              <button class="btn danger btn-del">Delete</button>
            </div>
          </div>
          <div class="note-title">${esc(n.title)}</div>
          <p class="note-text">${esc(n.message)}</p>
        `;
        // read
        card.querySelector(".btn-read").addEventListener("click", async () => {
          try {
            await api(`/api/notifications/${n.id}/read`, { method:"PUT" });
            const inAll = state.all.find(x => x.id === n.id);
            if (inAll) inAll.read = true;
            render();
          } catch {
            flash("Could not mark as read");
          }
        });
        // delete
        card.querySelector(".btn-del").addEventListener("click", async () => {
          if (!confirm("Delete this notification?")) return;
          try {
            await api(`/api/notifications/${n.id}`, { method:"DELETE" });
            state.all = state.all.filter(x => x.id !== n.id);
            render();
          } catch {
            flash("Could not delete");
          }
        });
        grp.appendChild(card);
      });
      listEl.appendChild(grp);
    });

    renderPager(meta);
  }

  function renderPager(meta) {
    const pager = $("#pager");
    pager.innerHTML = `
      <button class="btn ghost first" ${meta.hasPrevious ? "" : "disabled"}>« First</button>
      <button class="btn ghost prev" ${meta.hasPrevious ? "" : "disabled"}>‹ Prev</button>
      <span class="btn ghost" style="background:#fff;color:#3b4465;">Page ${meta.page+1} / ${meta.totalPages}</span>
      <button class="btn ghost next" ${meta.hasNext ? "" : "disabled"}>Next ›</button>
      <button class="btn ghost last" ${meta.hasNext ? "" : "disabled"}>Last »</button>
    `;
    pager.querySelector(".first").onclick = () => { state.page = 0; render(); };
    pager.querySelector(".prev").onclick  = () => { if (meta.hasPrevious){ state.page--; render(); } };
    pager.querySelector(".next").onclick  = () => { if (meta.hasNext){ state.page++; render(); } };
    pager.querySelector(".last").onclick  = () => { state.page = meta.totalPages - 1; render(); };
  }

  function wireToolbar() {
    $("#filterUnread").addEventListener("change", (e) => {
      state.filterUnread = e.target.value === "unread" ? "unread" : "all";
      state.page = 0;
      render();
    });
    $("#filterType").addEventListener("change", (e) => {
      state.filterType = e.target.value || "ALL";
      state.page = 0;
      render();
    });
    $("#reloadBtn").addEventListener("click", loadAll);
    $("#markAllBtn").addEventListener("click", async () => {
      try {
        await api("/api/notifications/read-all", { method:"PUT" });
        state.all.forEach(n => n.read = true);
        render();
      } catch {
        flash("Could not mark all as read");
      }
    });
    $("#pageSize").addEventListener("change", (e) => {
      state.size = Number(e.target.value) || 20;
      state.page = 0;
      render();
    });
  }

  document.addEventListener("DOMContentLoaded", async () => {
    if (!window.__auth?.requireAuthOrRedirect()) return;
    $("#logoutBtn")?.addEventListener("click", window.__auth.logout);
    wireToolbar();
    await loadAll();
  });
})();

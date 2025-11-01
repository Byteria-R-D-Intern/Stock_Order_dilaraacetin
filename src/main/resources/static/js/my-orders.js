(() => {
  const $  = (s) => document.querySelector(s);

  const state = {
    orders: [],
    page: 0,
    size: 20,
    filterId: ""
  };

  const flash = (msg, type="error") => {
    const el = $("#flash");
    el.textContent = msg;
    el.className = `flash ${type}`;
    el.hidden = false;
    setTimeout(()=> el.hidden = true, 3500);
  };

  const money = (n) => new Intl.NumberFormat("tr-TR",{style:"currency",currency:"TRY"}).format(n);
  const esc = (s) => String(s ?? "").replace(/[&<>"']/g, m => ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[m]));

  const paginate = (list, page=0, size=20) => {
    const total = list.length;
    const totalPages = Math.max(1, Math.ceil(total/size));
    const safe = Math.min(Math.max(0,page), totalPages-1);
    const start = safe*size;
    return {
      page: safe, size,
      totalElements: total,
      totalPages,
      hasNext: safe < totalPages-1,
      hasPrevious: safe > 0,
      content: list.slice(start, start+size)
    };
  };

  async function api(url, opts={}) {
    const res = await fetch(url, {
      ...opts,
      headers: { ...(opts.headers||{}), ...window.__auth.authHeader(), "Content-Type":"application/json" }
    });
    if (!res.ok) {
      const txt = await res.text();
      throw new Error(txt || "request_failed");
    }
    return res.status !== 204 ? res.json() : {};
  }

  async function loadOrders() {
    const id = (state.filterId || "").trim();
    if (id) {
      try {
        const single = await api(`/api/orders/${encodeURIComponent(id)}`); 
        state.orders = [normalize(single)];
      } catch {
        state.orders = [];
      }
    } else {
      const list = await api("/api/orders"); 
      state.orders = Array.isArray(list) ? list.map(normalize) : [];
    }
    state.page = 0;
    renderOrders();
  }

  function normalize(o) {
    return {
      id: o.orderId ?? o.id,
      status: o.status,
      total: Number(o.totalAmount ?? 0),
      createdAt: o.createdAt ?? null,
      updatedAt: o.updatedAt ?? null,
      items: Array.isArray(o.items) ? o.items : []
    };
  }

  function renderOrders() {
    const wrap  = $("#ordersWrap");
    const pager = $("#ordersPager");
    wrap.innerHTML = ""; pager.innerHTML = "";

    const meta = paginate(state.orders, state.page, state.size);

    if (!meta.content.length) {
      wrap.innerHTML = `<div class="card muted">No orders found</div>`;
    } else {
      meta.content.forEach(o => {
        const card = document.createElement("div");
        card.className = "card";
        const createdStr = o.createdAt ? new Date(o.createdAt).toLocaleString() : "-";
        const updatedStr = o.updatedAt ? new Date(o.updatedAt).toLocaleString() : "-";

        card.innerHTML = `
          <div class="row">
            <strong>Order #${esc(o.id)}</strong>
            <span class="chip">Status: ${esc(o.status)}</span>
            <span class="chip">Total: ${money(o.total)}</span>
          </div>
          <div class="small muted">Created: ${esc(createdStr)} • Updated: ${esc(updatedStr)}</div>
          <div class="items">
            ${renderItemsTable(o.items)}
          </div>
        `;
        wrap.appendChild(card);
      });
    }

    pager.appendChild(makePager(meta, (p)=>{ state.page = p; renderOrders(); }));
  }

  function renderItemsTable(items) {
    if (!items?.length) return `<div class="muted small">No items</div>`;
    const rows = items.map(i => `
      <tr>
        <td>${esc(i.name ?? "")}</td>
        <td class="small muted">${esc(i.sku ?? "")}</td>
        <td>${money(Number(i.unitPrice ?? 0))}</td>
        <td>${Number(i.quantity ?? 0)}</td>
        <td>${money(Number(i.lineTotal ?? (Number(i.unitPrice||0)*Number(i.quantity||0))))}</td>
      </tr>
    `).join("");
    return `
      <table>
        <thead>
          <tr>
            <th>Product</th><th>SKU</th><th>Unit</th><th>Qty</th><th>Line Total</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
    `;
  }

  function makePager(meta, onJump) {
    const d = document.createElement("div");
    d.className = "pager";
    d.innerHTML = `
      <button class="btn ghost first" ${meta.hasPrevious?"":"disabled"}>« First</button>
      <button class="btn ghost prev"  ${meta.hasPrevious?"":"disabled"}>‹ Prev</button>
      <span class="badge">Page ${meta.page+1} / ${meta.totalPages}</span>
      <button class="btn ghost next"  ${meta.hasNext?"":"disabled"}>Next ›</button>
      <button class="btn ghost last"  ${meta.hasNext?"":"disabled"}>Last »</button>
    `;
    d.querySelector(".first").onclick = ()=> onJump(0);
    d.querySelector(".prev").onclick  = ()=> onJump(Math.max(0, meta.page-1));
    d.querySelector(".next").onclick  = ()=> onJump(Math.min(meta.totalPages-1, meta.page+1));
    d.querySelector(".last").onclick  = ()=> onJump(meta.totalPages-1);
    return d;
  }

  $("#applyFilter").addEventListener("click", loadFromFilter);
  $("#clearFilter").addEventListener("click", async () => {
    $("#orderIdInput").value = "";
    state.filterId = "";
    await loadOrders();
  });
  $("#pageSize").addEventListener("change", (e) => {
    state.size = Number(e.target.value) || 20;
    state.page = 0;
    renderOrders();
  });

  function loadFromFilter() {
    state.filterId = $("#orderIdInput").value.trim();
    loadOrders().catch(()=> flash("Could not load orders"));
  }

  document.addEventListener("DOMContentLoaded", async () => {
    if (!window.__auth.requireAuthOrRedirect()) return;
    $("#logoutBtn")?.addEventListener("click", window.__auth.logout);
    $("#pageSize").value = String(state.size);
    await loadOrders();
  });
})();

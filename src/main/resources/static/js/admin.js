(() => {
  const $  = (s) => document.querySelector(s);
  const $$ = (s) => document.querySelectorAll(s);

  const PAGE_SIZE = 20;
  const state = {
    products: [],
    orders:   [],
    users:    [],
    pages: { products: 1, orders: 1, users: 1 }
  };

  const flash = (msg, type="ok") => {
    const el = $("#globalFlash");
    if (!el) return;
    el.textContent = msg;
    el.className = `flash ${type}`;
    el.hidden = false;
    setTimeout(() => el.hidden = true, 3000);
  };
  const money = (v) => {
    try { return new Intl.NumberFormat("tr-TR",{style:"currency",currency:"TRY"}).format(v); }
    catch { return `${v} ₺`; }
  };
  const esc = (s) => String(s ?? "").replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
  const toInt = (x, d=0) => Number.isFinite(parseInt(x,10)) ? parseInt(x,10) : d;
  const toNum = (x) => { const n = Number(x); return Number.isFinite(n) ? n : NaN; };

  const authFetch = async (url, init={}) => {
    const res = await fetch(url, {
      ...init,
      headers: { "Content-Type":"application/json", ...(init.headers||{}), ...window.__auth.authHeader() }
    });
    if (!res.ok) {
      let msg = `HTTP ${res.status}`;
      try {
        const ct = res.headers.get("content-type") || "";
        if (ct.includes("json")) {
          const j = await res.json();
          msg = j?.message || JSON.stringify(j) || msg;
        } else {
          msg = await res.text();
        }
      } catch {}
      throw new Error(msg);
    }
    return res.status === 204 ? null : res.json();
  };

  function initTabs() {
    $$(".tab").forEach(btn => {
      btn.addEventListener("click", () => {
        const key = btn.dataset.tab; 
        $$(".tab").forEach(b => b.classList.remove("active"));
        $$(".tabpane").forEach(p => p.classList.remove("active"));
        btn.classList.add("active");
        $(`#pane-${key}`).classList.add("active");
        render(key); 
      });
    });
  }

  async function loadProducts() {
    try {
      state.products = await authFetch("/api/products");
    } catch {
      try { state.products = await authFetch("/api/catalog/products"); }
      catch { state.products = []; }
    }
    $("#prodCount").textContent = `Products (${state.products.length})`;
  }

  function renderProducts() {
    const listEl  = $("#prodList");
    const pagerEl = $("#prodPager");
    listEl.innerHTML = "";
    pagerEl.innerHTML = "";

    const page = state.pages.products;
    const total = state.products.length;
    const start = (page - 1) * PAGE_SIZE;
    const pageItems = state.products.slice(start, start + PAGE_SIZE);

    if (!pageItems.length) {
      listEl.innerHTML = `<div class="card muted">No products</div>`;
    } else {
      listEl.innerHTML = pageItems.map(p => `
        <div class="card">
          <div class="row" style="justify-content:space-between;width:100%">
            <div>
              <strong>${esc(p.name)}</strong> — ${esc(p.sku)}
              <div class="muted small">ID: ${p.id}</div>
            </div>
            <div class="row">
              <button class="btn ghost" data-act="edit" data-id="${p.id}">Edit</button>
              <button class="btn danger" data-act="del" data-id="${p.id}">Delete</button>
            </div>
          </div>
          <div>Price: ${money(p.price)}</div>
          <div>Stock: ${p.quantityOnHand ?? 0}</div>
          <div>Status: ${p.status}</div>
          <div class="row">
            <input class="delta" id="delta-${p.id}" type="number" min="1" placeholder="Δ stock" style="width:120px">
            <button class="btn primary" data-act="inc" data-id="${p.id}">+ Increase</button>
            <button class="btn warning" data-act="dec" data-id="${p.id}">− Decrease</button>
          </div>
        </div>
      `).join("");
    }

    listEl.querySelectorAll("[data-act='inc']").forEach(b => b.onclick = () => adjustStock(b.dataset.id, "increase"));
    listEl.querySelectorAll("[data-act='dec']").forEach(b => b.onclick = () => adjustStock(b.dataset.id, "decrease"));
    listEl.querySelectorAll("[data-act='del']").forEach(b => b.onclick = () => deleteProduct(b.dataset.id));
    listEl.querySelectorAll("[data-act='edit']").forEach(b => b.onclick = () => openEditProduct(b.dataset.id));

    buildPager(pagerEl, total, page, (next) => { state.pages.products = next; renderProducts(); });
  }

  async function adjustStock(id, action) {
    const delta = toInt($(`#delta-${id}`)?.value, 0);
    if (delta <= 0) return flash("Enter positive delta", "error");
    await authFetch(`/api/products/${id}/stock/${action}`, { method:"POST", body: JSON.stringify({ delta }) });
    flash(`Stock ${action}d`, "ok");
    await loadProducts();
    fixPageAfterChange("products", state.products.length);
    renderProducts();
  }

  async function deleteProduct(id) {
    if (!confirm("Delete this product?")) return;
    await authFetch(`/api/products/${id}`, { method:"DELETE" });
    flash("Product deleted", "ok");
    await loadProducts();
    fixPageAfterChange("products", state.products.length);
    renderProducts();
  }

  const dlg = $("#productDialog");
  const frm = $("#productForm");
  const dlgTitle = $("#dlgTitle");
  const btnCancelDlg = $("#btnCancelDlg");
  const initialQtyWrap = $("#initialQtyWrap");
  let editingId = null;

  $("#btnNewProduct")?.addEventListener("click", () => openCreateProduct());
  btnCancelDlg?.addEventListener("click", () => dlg.close());

  frm?.addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      const body = collectBody(frm);
      if (editingId) {
        await authFetch(`/api/products/${editingId}`, { method:"PUT", body: JSON.stringify(body.update) });
        flash("Product updated", "ok");
      } else {
        await authFetch(`/api/products`, { method:"POST", body: JSON.stringify(body.create) });
        flash("Product created", "ok");
      }
      dlg.close();
      await loadProducts();
      renderProducts();
    } catch (err) {
      flash(err?.message || "Operation failed", "error");
    }
  });

  function openCreateProduct() {
    editingId = null;
    dlgTitle.textContent = "New Product";
    frm.reset();
    frm.sku.disabled = false;
    initialQtyWrap.style.display = "";
    dlg.showModal();
  }

  async function openEditProduct(id) {
    try {
      const p = await authFetch(`/api/products/${id}`);
      editingId = id;
      dlgTitle.textContent = `Edit Product #${id}`;
      frm.sku.value = p.sku || "";
      frm.name.value = p.name || "";
      frm.description.value = p.description || "";
      frm.price.value = p.price ?? 0;
      frm.status.value = p.status || "ACTIVE";

      frm.sku.disabled = true;
      initialQtyWrap.style.display = "none";
      dlg.showModal();
    } catch (e) {
      flash("Failed to load product", "error");
    }
  }

  function collectBody(form) {
    const price = toNum(form.price.value);
    if (!(price > 0)) throw new Error("Price must be greater than 0");
    const base = {
      name: (form.name.value || "").trim(),
      description: (form.description.value || "").trim() || null,
      price,
      status: form.status.value || "ACTIVE"
    };
    return {
      create: {
        sku: (form.sku.value || "").trim(),
        ...base,
        initialQuantity: toInt(form.initialQuantity.value, 0)
      },
      update: base
    };
  }

  async function loadOrders() {
    try { state.orders = await authFetch("/api/admin/orders"); }
    catch { state.orders = []; }
    $("#orderCount").textContent = `Orders (${state.orders.length})`;
  }

  function renderOrders() {
    const listEl  = $("#orderList");
    const pagerEl = $("#orderPager");
    listEl.innerHTML = "";
    pagerEl.innerHTML = "";

    const page = state.pages.orders;
    const total = state.orders.length;
    const start = (page - 1) * PAGE_SIZE;
    const items = state.orders.slice(start, start + PAGE_SIZE);

    if (!items.length) {
      listEl.innerHTML = `<div class="card muted">No orders</div>`;
    } else {
      listEl.innerHTML = items.map(o => `
        <div class="card">
          <div class="row" style="justify-content:space-between;width:100%">
            <div>
              <strong>Order #${o.id}</strong> — User ${o.userId}
              <div class="muted small">Total: ${money(o.totalAmount ?? 0)}</div>
            </div>
            <div class="row">
              <select id="ord-st-${o.id}">
                ${["PENDING","PAID","FAILED","CANCELLED","SHIPPED"].map(s => `<option value="${s}" ${o.status===s?"selected":""}>${s}</option>`).join("")}
              </select>
              <button class="btn primary" data-id="${o.id}" data-act="order-save">Save</button>
            </div>
          </div>
        </div>
      `).join("");
    }

    listEl.querySelectorAll("[data-act='order-save']").forEach(b => {
      b.onclick = async () => {
        const st = $(`#ord-st-${b.dataset.id}`).value;
        await authFetch(`/api/admin/orders/${b.dataset.id}/status?status=${encodeURIComponent(st)}`, { method:"POST" });
        flash("Order updated", "ok");
        await loadOrders();
        renderOrders();
      };
    });

    buildPager(pagerEl, total, page, (next) => { state.pages.orders = next; renderOrders(); });
  }

  async function loadUsers() {
    try { state.users = await authFetch("/api/admin/users"); }
    catch { state.users = []; }
    $("#userCount").textContent = `Users (${state.users.length})`;
  }

  function renderUsers() {
    const listEl  = $("#userList");
    const pagerEl = $("#userPager");
    listEl.innerHTML = "";
    pagerEl.innerHTML = "";

    const page = state.pages.users;
    const total = state.users.length;
    const start = (page - 1) * PAGE_SIZE;
    const items = state.users.slice(start, start + PAGE_SIZE);

    if (!items.length) {
      listEl.innerHTML = `<div class="card muted">No users</div>`;
    } else {
      listEl.innerHTML = items.map(u => `
        <div class="card">
          <div class="row" style="justify-content:space-between;width:100%">
            <div>
              <strong>${esc(u.email)}</strong>
              <div class="muted small">ID: ${u.id} • ${u.active ? "Active" : "Deactivated"}</div>
            </div>
            <div class="row">
              <select id="role-${u.id}">
                <option value="USER"  ${u.role==="USER"?"selected":""}>USER</option>
                <option value="ADMIN" ${u.role==="ADMIN"?"selected":""}>ADMIN</option>
              </select>
              <button class="btn primary" data-act="role" data-id="${u.id}">Save</button>
              ${u.active
                ? `<button class="btn warning" data-act="deact" data-id="${u.id}">Deactivate</button>`
                : `<button class="btn ghost" data-act="act" data-id="${u.id}">Activate</button>`}
            </div>
          </div>
        </div>
      `).join("");
    }

    listEl.querySelectorAll("[data-act='role']").forEach(b => b.onclick = () => changeRole(b.dataset.id));
    listEl.querySelectorAll("[data-act='act']").forEach(b => b.onclick = () => activateUser(b.dataset.id));
    listEl.querySelectorAll("[data-act='deact']").forEach(b => b.onclick = () => deactivateUser(b.dataset.id));

    buildPager(pagerEl, total, page, (next) => { state.pages.users = next; renderUsers(); });
  }

  async function changeRole(id) {
    const role = $(`#role-${id}`).value;
    await authFetch(`/api/admin/users/${id}/role`, { method:"PUT", body: JSON.stringify({ role }) });
    flash("Role updated", "ok");
    await loadUsers(); renderUsers();
  }
  async function activateUser(id) {
    await authFetch(`/api/admin/users/${id}/activate`, { method:"PUT" });
    flash("User activated", "ok");
    await loadUsers(); renderUsers();
  }
  async function deactivateUser(id) {
    await authFetch(`/api/admin/users/${id}/deactivate`, { method:"PUT" });
    flash("User deactivated", "ok");
    await loadUsers(); renderUsers();
  }

  function buildPager(el, totalItems, current, onGoto) {
    const totalPages = Math.max(1, Math.ceil(totalItems / PAGE_SIZE));
    const c = Math.min(Math.max(1, current), totalPages);
    const btn = (label, page, disabled=false, primary=false) =>
      `<button class="btn ${primary?"primary":"ghost"}" data-page="${page}" ${disabled?"disabled":""}>${label}</button>`;

    const winStart = Math.max(1, c - 2);
    const winEnd   = Math.min(totalPages, c + 2);
    const pagesHtml = [];
    for (let p = winStart; p <= winEnd; p++) {
      pagesHtml.push(btn(p, p, false, p===c));
    }

    el.innerHTML = [
      btn("⟨ Prev", c-1, c<=1),
      ...pagesHtml,
      btn("Next ⟩", c+1, c>=totalPages)
    ].join("");

    el.querySelectorAll("button[data-page]").forEach(b => {
      b.addEventListener("click", () => onGoto(toInt(b.dataset.page, c)));
    });
  }

  function fixPageAfterChange(key, total) {
    const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
    if (state.pages[key] > totalPages) state.pages[key] = totalPages;
  }

  document.addEventListener("DOMContentLoaded", async () => {
    const a = window.__auth.requireAuthOrRedirect();
    if (!a) return;
    if (!window.__auth.isAdmin()) { window.location.replace("/products.html"); return; }
    $("#logoutBtn")?.addEventListener("click", window.__auth.logout);

    initTabs();

    await Promise.all([loadProducts(), loadOrders(), loadUsers()]);
    render("products"); 
  });

  function render(which) {
    if (which === "products") renderProducts();
    else if (which === "orders") renderOrders();
    else if (which === "users") renderUsers();
  }
})();

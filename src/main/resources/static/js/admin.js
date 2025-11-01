(() => {
  const $  = (sel) => document.querySelector(sel);
  const $$ = (sel) => document.querySelectorAll(sel);

  const esc = (s) =>
    String(s ?? "").replace(/[&<>"']/g, (m) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[m])
    );

  const money = (n) =>
    new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(Number(n || 0));

  const flash = (msg, type = "ok") => {
    const el = document.createElement("div");
    el.className = `flash ${type}`;
    el.textContent = msg;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2500);
  };

  const api = async (url, options = {}) => {
    const res = await fetch(url, {
      ...options,
      headers: {
        ...(options.headers || {}),
        ...window.__auth.authHeader(),
        "Content-Type": "application/json",
      },
    });
    if (!res.ok) {
      let err = {};
      try { err = await res.json(); } catch {}
      const msg = err?.message || `HTTP ${res.status}`;
      throw new Error(msg);
    }
    if (res.status === 204) return {};
    const text = await res.text();
    return text ? JSON.parse(text) : {};
  };

  const paginate = (list, page = 0, size = 20) => {
    const total = list.length;
    const totalPages = Math.max(1, Math.ceil(total / size));
    const safePage = Math.min(Math.max(0, page), totalPages - 1);
    const start = safePage * size;
    const end = start + size;
    return {
      page: safePage,
      size,
      totalElements: total,
      totalPages,
      hasNext: safePage < totalPages - 1,
      hasPrevious: safePage > 0,
      content: list.slice(start, end),
    };
  };

  const state = {
    size: 20,

    productsAll: [],
    productsPage: 0,

    ordersAll: [],
    ordersPage: 0,

    usersAll: [],
    usersPage: 0,
  };

  $$(".tab").forEach((btn) => {
    btn.addEventListener("click", () => {
      $$(".tab").forEach((b) => b.classList.remove("active"));
      $$(".tab-content").forEach((c) => c.classList.remove("active"));
      btn.classList.add("active");
      $(`#tab-${btn.dataset.tab}`).classList.add("active");
    });
  });

  async function loadAllProducts() {
    const list = await api("/api/products");
    state.productsAll = Array.isArray(list) ? list : [];
    state.productsPage = 0;
    renderProducts();
  }

  async function loadProductById(id) {
    try {
      const p = await api(`/api/products/${id}`);
      state.productsAll = [p];
      state.productsPage = 0;
      renderProducts();
    } catch (e) {
      flash("Product not found", "error");
    }
  }

  function renderProducts() {
    const wrap = $("#productsWrap");
    wrap.innerHTML = "";

    const meta = paginate(state.productsAll, state.productsPage, state.size);
    $("#prodCount").textContent = `${meta.totalElements} item(s)`;

    if (!meta.content.length) {
      wrap.innerHTML = `<div class="card muted">No products</div>`;
    } else {
      meta.content.forEach((p) => {
        const qoh = p.quantityOnHand ?? 0;
        const card = document.createElement("div");
        card.className = "card";
        card.innerHTML = `
          <strong>${esc(p.name)}</strong> — ${esc(p.sku)}
          <div>Price: <b>${money(p.price)}</b></div>
          <div>Stock: <b>${qoh}</b></div>
          <div>Status: <b>${esc(p.status || "N/A")}</b></div>
          <div class="row">
            <input type="number" id="delta-${p.id}" placeholder="Δ stock" min="1" step="1" />
            <button data-id="${p.id}" class="btn primary inc">+ Increase</button>
            <button data-id="${p.id}" class="btn warning dec">− Decrease</button>
          </div>
          <div class="row">
            <button data-id="${p.id}" class="btn ghost edit">Edit</button>
            <button data-id="${p.id}" class="btn danger del">Delete</button>
          </div>
        `;
        wrap.appendChild(card);
      });

      wrap.querySelectorAll(".inc").forEach((b) =>
        b.addEventListener("click", () => adjustStock(b.dataset.id, "increase"))
      );
      wrap.querySelectorAll(".dec").forEach((b) =>
        b.addEventListener("click", () => adjustStock(b.dataset.id, "decrease"))
      );
      wrap.querySelectorAll(".del").forEach((b) =>
        b.addEventListener("click", () => deleteProduct(b.dataset.id))
      );
      wrap.querySelectorAll(".edit").forEach((b) =>
        b.addEventListener("click", () => openProductDialog("edit", b.dataset.id))
      );
    }

    const pager = $("#productsPager");
    pager.hidden = meta.totalPages <= 1;
    $("#prodPageInfo").textContent = `Page ${meta.page + 1} / ${meta.totalPages}`;
    $("#prodPrev").disabled = !meta.hasPrevious;
    $("#prodNext").disabled = !meta.hasNext;
    $("#prodPrev").onclick = () => {
      if (meta.hasPrevious) {
        state.productsPage -= 1;
        renderProducts();
      }
    };
    $("#prodNext").onclick = () => {
      if (meta.hasNext) {
        state.productsPage += 1;
        renderProducts();
      }
    };
  }

  async function adjustStock(id, action) {
    const delta = Number($(`#delta-${id}`).value || 0);
    if (!Number.isFinite(delta) || delta <= 0) return flash("Enter positive delta", "error");
    await api(`/api/products/${id}/stock/${action}`, {
      method: "POST",
      body: JSON.stringify({ delta }),
    });
    flash(`Stock ${action}d`, "ok");
    await loadAllProducts();
  }

  async function deleteProduct(id) {
    if (!confirm("Delete product?")) return;
    await api(`/api/products/${id}`, { method: "DELETE" });
    flash("Deleted", "ok");
    await loadAllProducts();
  }

  function openProductDialog(mode, id = null) {
    const dlg = $("#productDialog");
    const form = $("#productForm");
    form.reset();
    form.editingId.value = id || "";
    $("#productFormTitle").textContent = mode === "edit" ? "Edit Product" : "New Product";
    $("#initialQtyRow").style.display = mode === "edit" ? "none" : "grid";

    if (mode === "edit" && id) {
      const prod = state.productsAll.find((x) => String(x.id) === String(id));
      if (prod) {
        form.sku.value = prod.sku ?? "";
        form.name.value = prod.name ?? "";
        form.description.value = prod.description ?? "";
        form.price.value = prod.price ?? 0;
        form.status.value = (prod.status || "ACTIVE");
      }
    }

    dlg.showModal();
  }

  async function submitProductForm(ev) {
    ev.preventDefault();
    const form = ev.target;
    const editingId = form.editingId.value || null;
    const bodyCommon = {
      sku: form.sku.value.trim(),
      name: form.name.value.trim(),
      description: form.description.value.trim() || null,
      price: Number(form.price.value),
      status: form.status.value || "ACTIVE",
    };

    try {
      if (editingId) {
        const payload = {
          name: bodyCommon.name,
          description: bodyCommon.description,
          price: bodyCommon.price,
          status: bodyCommon.status,
        };
        await api(`/api/products/${editingId}`, { method: "PUT", body: JSON.stringify(payload) });
        flash("Product updated", "ok");
      } else {
        const payload = {
          ...bodyCommon,
          initialQuantity: Number(form.initialQuantity.value || 0),
        };
        await api(`/api/products`, { method: "POST", body: JSON.stringify(payload) });
        flash("Product created", "ok");
      }
      $("#productDialog").close();
      await loadAllProducts();
    } catch (e) {
      flash(e.message || "Save failed", "error");
    }
  }

  async function loadAllOrders() {
    const list = await api("/api/admin/orders");
    state.ordersAll = Array.isArray(list) ? list : [];
    state.ordersPage = 0;
    renderOrders();
  }

  async function loadOrderById(id) {
    try {
      const o = await api(`/api/admin/orders/${id}`);
      state.ordersAll = [o];
      state.ordersPage = 0;
      renderOrders();
    } catch {
      flash("Order not found", "error");
    }
  }

  function renderOrders() {
    const wrap = $("#ordersWrap");
    wrap.innerHTML = "";

    const meta = paginate(state.ordersAll, state.ordersPage, state.size);
    $("#ordCount").textContent = `${meta.totalElements} order(s)`;

    if (!meta.content.length) {
      wrap.innerHTML = `<div class="card muted">No orders</div>`;
    } else {
      meta.content.forEach((o) => {
        const card = document.createElement("div");
        card.className = "card";
        card.innerHTML = `
          <strong>Order #${o.id}</strong> — User ${o.userId}
          <div>Total: <b>${money(o.totalAmount)}</b></div>
          <div class="row">
            <select id="st-${o.id}">
              ${["PENDING","PAID","FAILED","CANCELLED","SHIPPED"].map(s => `<option value="${s}" ${o.status===s?"selected":""}>${s}</option>`).join("")}
            </select>
            <button data-id="${o.id}" class="btn primary save-status">Save</button>
          </div>
        `;
        wrap.appendChild(card);
      });

      wrap.querySelectorAll(".save-status").forEach((b) =>
        b.addEventListener("click", async () => {
          const st = $(`#st-${b.dataset.id}`).value;
          await api(`/api/admin/orders/${b.dataset.id}/status?status=${encodeURIComponent(st)}`, { method: "POST" });
          flash("Order updated", "ok");
          await loadAllOrders();
        })
      );
    }

    const pager = $("#ordersPager");
    pager.hidden = meta.totalPages <= 1;
    $("#ordPageInfo").textContent = `Page ${meta.page + 1} / ${meta.totalPages}`;
    $("#ordPrev").disabled = !meta.hasPrevious;
    $("#ordNext").disabled = !meta.hasNext;
    $("#ordPrev").onclick = () => {
      if (meta.hasPrevious) {
        state.ordersPage -= 1;
        renderOrders();
      }
    };
    $("#ordNext").onclick = () => {
      if (meta.hasNext) {
        state.ordersPage += 1;
        renderOrders();
      }
    };
  }

  async function loadUsers() {
    const list = await api("/api/admin/users");
    state.usersAll = Array.isArray(list) ? list : [];
    state.usersPage = 0;
    renderUsers();
  }

  function renderUsers() {
    const wrap = $("#usersWrap");
    wrap.innerHTML = "";

    const meta = paginate(state.usersAll, state.usersPage, state.size);
    $("#usrCount").textContent = `${meta.totalElements} user(s)`;

    if (!meta.content.length) {
      wrap.innerHTML = `<div class="card muted">No users</div>`;
    } else {
      meta.content.forEach((u) => {
        const card = document.createElement("div");
        card.className = "card";
        card.innerHTML = `
          <strong>${esc(u.email)}</strong> — ${esc(u.role)} ${!u.active ? "(deactivated)" : ""}
          <div class="row">
            <select id="role-${u.id}">
              <option value="USER" ${u.role==="USER"?"selected":""}>USER</option>
              <option value="ADMIN" ${u.role==="ADMIN"?"selected":""}>ADMIN</option>
            </select>
            <button data-id="${u.id}" class="btn primary change-role">Save</button>
            ${
              u.active
                ? `<button data-id="${u.id}" class="btn warning deact">Deactivate</button>`
                : `<button data-id="${u.id}" class="btn primary act">Activate</button>`
            }
          </div>
        `;
        wrap.appendChild(card);
      });

      wrap.querySelectorAll(".change-role").forEach((b) =>
        b.addEventListener("click", async () => {
          const role = $(`#role-${b.dataset.id}`).value;
          await api(`/api/admin/users/${b.dataset.id}/role`, {
            method: "PUT",
            body: JSON.stringify({ role }),
          });
          flash("Role updated", "ok");
          await loadUsers();
        })
      );

      wrap.querySelectorAll(".act").forEach((b) =>
        b.addEventListener("click", async () => {
          await api(`/api/admin/users/${b.dataset.id}/activate`, { method: "PUT" });
          flash("User activated", "ok");
          await loadUsers();
        })
      );

      wrap.querySelectorAll(".deact").forEach((b) =>
        b.addEventListener("click", async () => {
          await api(`/api/admin/users/${b.dataset.id}/deactivate`, { method: "PUT" });
          flash("User deactivated", "ok");
          await loadUsers();
        })
      );
    }

    const pager = $("#usersPager");
    pager.hidden = meta.totalPages <= 1;
    $("#usrPageInfo").textContent = `Page ${meta.page + 1} / ${meta.totalPages}`;
    $("#usrPrev").disabled = !meta.hasPrevious;
    $("#usrNext").disabled = !meta.hasNext;
    $("#usrPrev").onclick = () => {
      if (meta.hasPrevious) {
        state.usersPage -= 1;
        renderUsers();
      }
    };
    $("#usrNext").onclick = () => {
      if (meta.hasNext) {
        state.usersPage += 1;
        renderUsers();
      }
    };
  }

  document.addEventListener("DOMContentLoaded", async () => {
    const a = window.__auth.requireAuthOrRedirect();
    if (!a) return;
    if (!window.__auth.isAdmin()) {
      window.location.replace("/products.html");
      return;
    }

    $("#logoutBtn")?.addEventListener("click", window.__auth.logout);

    $("#btnNewProduct")?.addEventListener("click", () => openProductDialog("create"));
    $("#cancelProductForm")?.addEventListener("click", () => $("#productDialog").close());
    $("#productForm")?.addEventListener("submit", submitProductForm);

    $("#applyProdFilter")?.addEventListener("click", () => {
      const val = ($("#prodFilterId")?.value || "").trim();
      if (!val) { loadAllProducts(); return; }
      const id = Number(val);
      if (!id || id < 1) { flash("Enter a valid product ID", "error"); return; }
      loadProductById(id);
    });
    $("#clearProdFilter")?.addEventListener("click", () => {
      const inp = $("#prodFilterId");
      if (inp) inp.value = "";
      loadAllProducts();
    });

    $("#applyOrdFilter")?.addEventListener("click", () => {
      const val = ($("#ordFilterId")?.value || "").trim();
      if (!val) { loadAllOrders(); return; }
      const id = Number(val);
      if (!id || id < 1) { flash("Enter a valid order ID", "error"); return; }
      loadOrderById(id);
    });
    $("#clearOrdFilter")?.addEventListener("click", () => {
      const inp = $("#ordFilterId");
      if (inp) inp.value = "";
      loadAllOrders();
    });

    await loadAllProducts();
    await loadAllOrders();
    await loadUsers();
  });
})();

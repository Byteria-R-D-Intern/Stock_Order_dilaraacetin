(() => {
  const $ = (sel) => document.querySelector(sel);
  const $$ = (sel) => document.querySelectorAll(sel);

  const api = async (url, options = {}) => {
    const res = await fetch(url, {
      ...options,
      headers: {
        ...(options.headers || {}),
        ...window.__auth.authHeader(),
        "Content-Type": "application/json"
      }
    });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(text);
    }
    return res.status !== 204 ? res.json() : {};
  };

  const flash = (msg, type = "ok") => {
    const el = document.createElement("div");
    el.className = `flash ${type}`;
    el.textContent = msg;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2500);
  };

  $$(".tab").forEach(btn => {
    btn.addEventListener("click", () => {
      $$(".tab").forEach(b => b.classList.remove("active"));
      $$(".tab-content").forEach(c => c.classList.remove("active"));
      btn.classList.add("active");
      $(`#tab-${btn.dataset.tab}`).classList.add("active");
    });
  });

  async function loadProducts() {
    const products = await api("/api/products");
    const container = $("#tab-products");
    container.innerHTML = products.map(p => `
      <div class="card">
        <strong>${p.name}</strong> — ${p.sku}
        <div>Price: ${p.price} ₺</div>
        <div>Stock: ${p.quantityOnHand}</div>
        <div>Status: ${p.status}</div>
        <input type="number" id="delta-${p.id}" placeholder="Δ stok" />
        <button data-id="${p.id}" class="btn primary inc">+ Increase</button>
        <button data-id="${p.id}" class="btn warning dec">− Decrease</button>
        <button data-id="${p.id}" class="btn danger del">Delete</button>
      </div>
    `).join("");

    container.querySelectorAll(".inc").forEach(b => b.onclick = () => adjustStock(b.dataset.id, "increase"));
    container.querySelectorAll(".dec").forEach(b => b.onclick = () => adjustStock(b.dataset.id, "decrease"));
    container.querySelectorAll(".del").forEach(b => b.onclick = () => deleteProduct(b.dataset.id));
  }

  async function adjustStock(id, action) {
    const delta = Number($(`#delta-${id}`).value || 0);
    if (delta <= 0) return flash("Enter positive delta", "error");
    await api(`/api/products/${id}/stock/${action}`, {
      method: "POST",
      body: JSON.stringify({ delta })
    });
    flash(`Stock ${action}d`, "ok");
    await loadProducts();
  }

  async function deleteProduct(id) {
    if (!confirm("Delete product?")) return;
    await api(`/api/products/${id}`, { method: "DELETE" });
    flash("Deleted", "ok");
    await loadProducts();
  }

  async function loadOrders() {
    const orders = await api("/api/admin/orders");
    const container = $("#tab-orders");
    container.innerHTML = orders.map(o => `
      <div class="card">
        <strong>Order #${o.id}</strong> — User ${o.userId}
        <div>Status: ${o.status}</div>
        <select id="st-${o.id}">
          <option value="PENDING" ${o.status==="PENDING"?"selected":""}>PENDING</option>
          <option value="SHIPPED" ${o.status==="SHIPPED"?"selected":""}>SHIPPED</option>
          <option value="DELIVERED" ${o.status==="DELIVERED"?"selected":""}>DELIVERED</option>
        </select>
        <button data-id="${o.id}" class="btn primary save-status">Save</button>
      </div>
    `).join("");

    container.querySelectorAll(".save-status").forEach(b => {
      b.onclick = async () => {
        const st = $(`#st-${b.dataset.id}`).value;
        await api(`/api/admin/orders/${b.dataset.id}/status?status=${st}`, { method: "POST" });
        flash("Order updated", "ok");
        await loadOrders();
      };
    });
  }

  async function loadUsers() {
    const res = await fetch("/api/users"); 
    const users = res.ok ? await res.json() : [];

    const container = $("#tab-users");
    container.innerHTML = users.map(u => `
      <div class="card">
        <strong>${u.email}</strong> — ${u.role} ${!u.active ? "(deactivated)" : ""}
        <select id="role-${u.id}">
          <option value="USER" ${u.role==="USER"?"selected":""}>USER</option>
          <option value="ADMIN" ${u.role==="ADMIN"?"selected":""}>ADMIN</option>
        </select>
        <button data-id="${u.id}" class="btn primary change-role">Save</button>
        ${u.active
          ? `<button data-id="${u.id}" class="btn warning deact">Deactivate</button>`
          : `<button data-id="${u.id}" class="btn primary act">Activate</button>`}
      </div>
    `).join("");

    container.querySelectorAll(".change-role").forEach(b => b.onclick = () => changeRole(b.dataset.id));
    container.querySelectorAll(".act").forEach(b => b.onclick = () => activateUser(b.dataset.id));
    container.querySelectorAll(".deact").forEach(b => b.onclick = () => deactivateUser(b.dataset.id));
  }

  async function changeRole(id) {
    const role = $(`#role-${id}`).value;
    await api(`/api/admin/users/${id}/role`, { method: "PUT", body: JSON.stringify({ role }) });
    flash("Role updated", "ok");
    await loadUsers();
  }

  async function activateUser(id) {
    await api(`/api/admin/users/${id}/activate`, { method: "PUT" });
    flash("User activated", "ok");
    await loadUsers();
  }

  async function deactivateUser(id) {
    await api(`/api/admin/users/${id}/deactivate`, { method: "PUT" });
    flash("User deactivated", "ok");
    await loadUsers();
  }

  document.addEventListener("DOMContentLoaded", async () => {
    const a = window.__auth.requireAuthOrRedirect();
    if (!a) return;
    if (!window.__auth.isAdmin()) {
      window.location.replace("/products.html");
      return;
    }

    $("#logoutBtn")?.addEventListener("click", window.__auth.logout);

    await loadProducts();
    await loadOrders();
    await loadUsers();
  });
})();

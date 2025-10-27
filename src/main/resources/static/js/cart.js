(() => {
  const $ = (s) => document.querySelector(s);
  const itemsWrap = $("#cartItems");
  const subtotalEl = $("#subtotal");

  const flash = (msg, type = "error") => {
    const el = $("#flash");
    el.textContent = msg;
    el.className = `flash ${type}`;
    el.hidden = false;
    setTimeout(() => (el.hidden = true), 3500);
  };

  document.addEventListener("DOMContentLoaded", () => {
    const auth = window.__auth?.requireAuthOrRedirect();
    if (!auth) return;

    $("#logoutBtn")?.addEventListener("click", window.__auth.logout);
    $("#clearCart")?.addEventListener("click", onClearCart);
    $("#goCheckout")?.addEventListener("click", () => { window.location = "/checkout.html"; });

    loadCart();
  });

  async function loadCart() {
    try {
      const res = await fetch("/api/cart", { headers: window.__auth.authHeader() });
      if (!res.ok) throw new Error("failed");
      const cart = await res.json();
      renderCart(cart);
    } catch {
      flash("Could not load your cart.");
    }
  }

  function renderCart(cart) {
    itemsWrap.innerHTML = "";
    const items = cart.items || [];

    if (!items.length) {
      itemsWrap.innerHTML = `<div class="card muted">Your cart is empty.</div>`;
      subtotalEl.textContent = money(0);
      return;
    }

    let subtotal = 0;

    items.forEach((it) => {
      const unit = Number(it.unitPrice);
      const qty  = Number(it.quantity || 0);
      const line = unit * qty;
      subtotal += line;

      const row = document.createElement("div");
      row.className = "card cart-item";
      row.innerHTML = `
        <div class="ci-left">
          <div class="ci-title">${esc(it.name)}</div>
          <div class="ci-meta muted small">SKU: ${esc(it.sku)}</div>
        </div>
        <div class="ci-mid">
          <div class="price">${money(unit)}</div>
          <input class="qty" type="number" min="0" step="1" value="${qty}" />
          <div class="line">${money(line)}</div>
        </div>
        <button class="icon-btn remove" title="Remove">×</button>
      `;

      row.querySelector(".qty").addEventListener("change", async (e) => {
        let q = parseInt(e.target.value, 10);
        if (isNaN(q) || q < 0) q = 0;
        try {
          await updateQty(it.productId, q);
          await loadCart();
        } catch {
          flash("Could not update quantity.");
          e.target.value = it.quantity;
        }
      });

      row.querySelector(".remove").addEventListener("click", async () => {
        try {
          await updateQty(it.productId, 0);
          await loadCart();
        } catch {
          flash("Could not remove item.");
        }
      });

      itemsWrap.appendChild(row);
    });

    subtotalEl.textContent = money(subtotal);
  }

  async function updateQty(productId, quantity) {
    const body = { productId, quantity };
    const res = await fetch("/api/cart/items", {
      method: "PUT",
      headers: { ...window.__auth.authHeader(), "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error("update_failed");
  }

  async function onClearCart() {
    try {
      const res = await fetch("/api/cart", {
        method: "DELETE",
        headers: window.__auth.authHeader(),
      });
      if (!res.ok) throw new Error();
      await loadCart();
      flash("Cart cleared.", "ok");
    } catch {
      flash("Could not clear the cart.");
    }
  }

  function money(n) {
    return new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(n);
  }
  function esc(s) {
    return String(s ?? "").replace(/[&<>"']/g, (m) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[m]));
  }
})();

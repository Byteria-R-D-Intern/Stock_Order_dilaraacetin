const storageKey = "auth";

const state = {
  auth: null,
  products: [],
  page: 0,
  size: 20
};

function paginate(list, page = 0, size = 20) {
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
    content: list.slice(start, end)
  };
}

function getAuth() {
  try { return JSON.parse(localStorage.getItem(storageKey) || "null"); }
  catch { return null; }
}

function requireAuth() {
  const a = getAuth();
  if (!a?.token) {
    window.location.replace("/login.html");
    return null;
  }
  return a;
}

function fmtMoney(v) {
  try { return new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY' }).format(v); }
  catch { return `${v} ₺`; }
}

function showMsg(text, ok=false) {
  const el = document.getElementById("msg");
  if (!el) return;
  el.textContent = text || "";
  el.classList.toggle("ok", !!ok);
}

async function fetchCartCount() {
  try {
    const res = await fetch(`/api/cart`, {
      headers: { "Authorization": `${state.auth.tokenType || "Bearer"} ${state.auth.token}` }
    });
    if (!res.ok) return;
    const data = await res.json();
    const count = (data?.items || []).reduce((s, i) => s + (i.quantity || 0), 0);
    document.getElementById("cartCount").textContent = count;
  } catch {}
}

async function loadProducts() {
  showMsg("Loading products...");
  try {
    const res = await fetch(`/api/catalog/products`);
    if (!res.ok) {
      const err = await res.json().catch(()=> ({}));
      showMsg(err?.message || "Failed to load products");
      return;
    }
    const list = await res.json();
    state.products = Array.isArray(list) ? list : [];
    renderProducts();
    showMsg("");
  } catch {
    showMsg("Network error");
  }
}

function renderProducts() {
  const grid = document.getElementById("productsGrid");
  grid.innerHTML = "";

  const meta = paginate(state.products, state.page, state.size);

  if (!meta.content.length) {
    grid.innerHTML = `<div class="empty">No products</div>`;
  } else {
    meta.content.forEach(p => {
      const qoh = p.quantityOnHand ?? 0;
      const disabled = qoh <= 0 || p.status !== "ACTIVE";
      const card = document.createElement("div");
      card.className = "card";
      card.innerHTML = `
        <div class="name">${p.name}</div>
        <div class="sku">SKU: ${p.sku}</div>
        ${p.description ? `<div class="desc">${p.description}</div>` : ""}
        <div class="chips">
          <span class="chip">Price: ${fmtMoney(p.price)}</span>
          <span class="chip">Stock: ${qoh}</span>
          <span class="chip">Status: ${p.status || "N/A"}</span>
        </div>
        <div class="row">
          <input class="qty" type="number" min="1" value="${qoh>0?1:0}" ${disabled?"disabled":""} />
          <button class="add" ${disabled?"disabled":""}>Add to Cart</button>
        </div>
      `;
      const qtyInput = card.querySelector(".qty");
      const addBtn = card.querySelector(".add");
      addBtn?.addEventListener("click", async () => {
        const qty = Math.max(1, parseInt(qtyInput.value || "1", 10));
        if (qty > qoh) { showMsg("Not enough stock"); return; }
        addBtn.disabled = true;
        showMsg("");
        try {
          const res = await fetch(`/api/cart/items`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "Authorization": `${state.auth.tokenType || "Bearer"} ${state.auth.token}`
            },
            body: JSON.stringify({ productId: p.id, quantity: qty })
          });
          if (!res.ok) {
            const err = await res.json().catch(()=> ({}));
            showMsg(err?.message || "Failed to add item");
          } else {
            showMsg("Added to cart", true);
            fetchCartCount();
          }
        } catch {
          showMsg("Network error");
        } finally {
          addBtn.disabled = false;
        }
      });
      grid.appendChild(card);
    });
  }

  renderPager(meta); 
}

function renderPager(meta){
  const wrap = document.getElementById("pagerBar");
  if (!wrap) return;
  wrap.innerHTML = ""; 
  const bar = document.createElement("div");
  bar.className = "row";
  bar.style.margin = "20px 0";
  bar.style.justifyContent = "center";
  bar.innerHTML = `
    <button class="btn secondary prev" ${meta.hasPrevious ? "" : "disabled"}>Prev</button>
    <span class="badge">Page ${meta.page + 1} / ${meta.totalPages}</span>
    <button class="btn secondary next" ${meta.hasNext ? "" : "disabled"}>Next</button>
  `;
  bar.querySelector(".prev").onclick = () => {
    if (meta.hasPrevious){ state.page -= 1; renderProducts(); }
  };
  bar.querySelector(".next").onclick = () => {
    if (meta.hasNext){ state.page += 1; renderProducts(); }
  };
  wrap.appendChild(bar);
}

function wireHeader() {
  const email = state.auth?.email || "";
  if (email) document.getElementById("userEmail").textContent = email;

  document.getElementById("logoutBtn")?.addEventListener("click", () => {
    localStorage.removeItem(storageKey);
    window.location.replace("/login.html");
  });

  window.__auth?.ensureAdminButton?.();
  document.getElementById("profileBtn")?.classList.remove("hidden");
}

(function init(){
  const auth = requireAuth();
  if (!auth) return;
  state.auth = auth;
  wireHeader();
  loadProducts();
  fetchCartCount();
})();

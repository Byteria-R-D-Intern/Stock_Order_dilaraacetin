const storageKey = "auth";

const state = {
  auth: null,

  productsAll: [],
  page: 0,
  size: 20,
};

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
const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (m) =>
  ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[m])
);
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

async function apiGetJson(url) {
  const res = await fetch(url);
  if (!res.ok) {
    let e = {};
    try { e = await res.json(); } catch {}
    throw new Error(e?.message || `HTTP ${res.status}`);
  }
  return res.json();
}

async function loadAllProducts() {
  showMsg("Loading products...");
  try {
    const list = await apiGetJson(`/api/catalog/products`);
    state.productsAll = Array.isArray(list) ? list : [];
    state.page = 0;
    renderProducts();
    showMsg("");
  } catch (e) {
    showMsg(e.message || "Failed to load products");
  }
}

async function loadProductById(id) {
  showMsg("Loading product...");
  try {
    const p = await apiGetJson(`/api/catalog/products/${id}`);
    state.productsAll = p ? [p] : [];
    state.page = 0;
    renderProducts();
    showMsg("");
  } catch {
    state.productsAll = [];
    state.page = 0;
    renderProducts();
    showMsg("Product not found");
  }
}

function renderProducts() {
  const grid = document.getElementById("productsGrid");
  grid.innerHTML = "";

  const meta = paginate(state.productsAll, state.page, state.size);
  const countBadge = document.getElementById("prodCount");
  countBadge.textContent = `${meta.totalElements} item(s)`;

  if (!meta.content.length) {
    grid.innerHTML = `<div class="empty">No products</div>`;
  } else {
    meta.content.forEach(p => {
      const qoh = p.quantityOnHand ?? 0;
      const disabled = qoh <= 0 || p.status !== "ACTIVE";
      const card = document.createElement("div");
      card.className = "card";
      card.innerHTML = `
        <div class="name">${esc(p.name)}</div>
        <div class="sku">SKU: ${esc(p.sku)}</div>
        ${p.description ? `<div class="desc">${esc(p.description)}</div>` : ""}
        <div class="chips">
          <span class="chip">Price: ${fmtMoney(p.price)}</span>
          <span class="chip">Stock: ${qoh}</span>
          <span class="chip">Status: ${esc(p.status || "N/A")}</span>
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
  const pager = document.getElementById("productsPager");
  const info  = document.getElementById("prodPageInfo");
  const prev  = document.getElementById("prodPrev");
  const next  = document.getElementById("prodNext");

  pager.hidden = meta.totalPages <= 1;
  info.textContent = `Page ${meta.page + 1} / ${meta.totalPages}`;
  prev.disabled = !meta.hasPrevious;
  next.disabled = !meta.hasNext;

  prev.onclick = () => { if (meta.hasPrevious){ state.page -= 1; renderProducts(); } };
  next.onclick = () => { if (meta.hasNext){ state.page += 1; renderProducts(); } };
}

function wireHeader() {
  const email = state.auth?.email || "";
  if (email) document.getElementById("userEmail").textContent = email;

  document.getElementById("logoutBtn")?.addEventListener("click", () => {
    localStorage.removeItem(storageKey);
    window.location.replace("/login.html");
  });

  window.__auth?.ensureAdminButton?.();
}

function wireFilter() {
  const applyBtn = document.getElementById("applyProdFilter");
  const clearBtn = document.getElementById("clearProdFilter");
  const input    = document.getElementById("prodFilterId");

  applyBtn?.addEventListener("click", () => {
    const val = (input?.value || "").trim();
    if (!val) { loadAllProducts(); return; }
    const id = Number(val);
    if (!id || id < 1) { showMsg("Enter a valid product ID"); return; }
    loadProductById(id);
  });

  clearBtn?.addEventListener("click", () => {
    if (input) input.value = "";
    loadAllProducts();
  });

  input?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      applyBtn?.click();
    }
  });
}

(function init(){
  const auth = requireAuth();
  if (!auth) return;
  state.auth = auth;

  wireHeader();
  wireFilter();
  loadAllProducts();
  fetchCartCount();
})();

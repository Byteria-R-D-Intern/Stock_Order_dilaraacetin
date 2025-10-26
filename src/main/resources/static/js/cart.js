const storageKey = "auth";
const state = { auth:null, cart:null };

function getAuth(){ try{return JSON.parse(localStorage.getItem(storageKey)||"null");}catch{return null;} }
function requireAuth(){
  const a=getAuth();
  if(!a?.token){ window.location.replace("/login.html"); return null; }
  return a;
}
function money(v){ try{ return new Intl.NumberFormat('tr-TR',{style:'currency',currency:'TRY'}).format(v); }catch{ return `${v} ₺`; } }
function msg(text, ok=false){ const el=document.getElementById("msg"); el.textContent=text||""; el.classList.toggle("ok",!!ok); }

function wireHeader(){
  document.getElementById("logoutBtn")?.addEventListener("click",()=>{
    localStorage.removeItem(storageKey);
    window.location.replace("/login.html");
  });
}

async function loadCart(){
  msg("Loading cart...");
  try{
    const res = await fetch("/api/cart",{ headers:{ "Authorization": `${state.auth.tokenType||"Bearer"} ${state.auth.token}` }});
    if(!res.ok){ const err=await res.json().catch(()=>({})); msg(err?.message||"Failed to load cart"); return; }
    state.cart = await res.json(); 
    renderCart();
    msg("");
  }catch{ msg("Network error"); }
}

function renderCart(){
  const c = document.getElementById("cartContainer");
  c.innerHTML = "";
  const items = state.cart?.items || [];
  if(items.length===0){
    c.innerHTML = `<div class="cart-empty">Your cart is empty.</div>`;
    document.getElementById("subtotal").textContent = money(0);
    return;
  }

  let subtotal = 0;
  items.forEach(it=>{
    const unit = Number(it.unitPrice);
    const line = unit * Number(it.quantity||0);
    subtotal += line;

    const row = document.createElement("div");
    row.className = "cart-item";
    row.innerHTML = `
      <div>
        <div class="name">${it.name}</div>
        <div class="sku">SKU: ${it.sku}</div>
      </div>
      <div class="price">${money(unit)}</div>
      <div>
        <input class="qty" type="number" min="0" value="${it.quantity}">
      </div>
      <div class="line">${money(line)}</div>
      <button class="remove" title="Remove">×</button>
    `;
    const qty = row.querySelector(".qty");
    const lineEl = row.querySelector(".line");
    qty.addEventListener("input", ()=>{
      const q = Math.max(0, Number(qty.value||0));
      lineEl.textContent = money(unit*q);
    });
    qty.addEventListener("change", ()=> updateQty(it.productId, Number(qty.value||0)));
    row.querySelector(".remove").addEventListener("click", ()=> updateQty(it.productId, 0));
    c.appendChild(row);
  });
  document.getElementById("subtotal").textContent = money(subtotal);
}

async function updateQty(productId, quantity){
  try{
    const res = await fetch("/api/cart/items",{
      method:"PUT",
      headers:{
        "Content-Type":"application/json",
        "Authorization": `${state.auth.tokenType||"Bearer"} ${state.auth.token}`
      },
      body: JSON.stringify({ productId, quantity })
    });
    if(!res.ok){ const err=await res.json().catch(()=>({})); msg(err?.message||"Failed to update item"); return; }
    state.cart = await res.json();
    renderCart();
    msg("Cart updated", true);
  }catch{ msg("Network error"); }
}

async function clearCart(){
  try{
    const res = await fetch("/api/cart",{ method:"DELETE", headers:{ "Authorization": `${state.auth.tokenType||"Bearer"} ${state.auth.token}` }});
    if(!res.ok){ const err=await res.json().catch(()=>({})); msg(err?.message||"Failed to clear"); return; }
    await loadCart();
    msg("Cart cleared", true);
  }catch{ msg("Network error"); }
}

async function checkout(){
  msg("Preparing checkout...");
  try{
    const methodsRes = await fetch("/api/payments/methods", { headers:{ "Authorization": `${state.auth.tokenType||"Bearer"} ${state.auth.token}` }});
    if(!methodsRes.ok){
      msg("No saved payment method. Please add one on Payments.", false);
      return;
    }
    const methods = await methodsRes.json();
    if(!methods.length){ msg("Please save a payment method first.", false); return; }

    const body = { savedPaymentMethodId: methods[0].id };
    const res = await fetch("/api/orders/checkout/saved",{
      method:"POST",
      headers:{
        "Content-Type":"application/json",
        "Authorization": `${state.auth.tokenType||"Bearer"} ${state.auth.token}`
      },
      body: JSON.stringify(body)
    });
    if(!res.ok){
      const err=await res.json().catch(()=>({}));
      msg(err?.message || "Checkout failed");
      return;
    }
    const order = await res.json();
    msg(`Order #${order.orderId} created.`, true);
    await loadCart();
  }catch{ msg("Network error"); }
}

(function init(){
  const auth = requireAuth(); if(!auth) return;
  state.auth = auth;
  wireHeader();
  document.getElementById("clearBtn")?.addEventListener("click", clearCart);
  document.getElementById("checkoutBtn")?.addEventListener("click", checkout);
  loadCart();
})();

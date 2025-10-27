(() => {
  const state = {
    addresses: [],
    methods: [],
    selectedAddressId: null,
    selectedMethodId: null,          
    tokenPayment: null,              
    subtotal: 0
  };

  const $ = (sel) => document.querySelector(sel);
  const flash = (msg, type="error") => {
    const el = $("#flash");
    el.textContent = msg;
    el.className = `flash ${type}`;
    el.hidden = false;
    setTimeout(()=> el.hidden = true, 4000);
  };

  document.addEventListener("DOMContentLoaded", () => {
    const auth = window.__auth?.requireAuthOrRedirect();
    if (!auth) return;

    $("#logoutBtn")?.addEventListener("click", window.__auth.logout);

    loadSubtotal();
    loadAddresses();

    const addressForm = $("#addressForm");
    if (addressForm) addressForm.hidden = true;

    $("#toggleAddressForm").onclick = () => {
      addressForm.hidden = !addressForm.hidden;
      if (!addressForm.hidden) addressForm.querySelector("input[name='title']")?.focus();
    };
    $("#cancelAddressForm").onclick = () => (addressForm.hidden = true);
    $("#addressForm").onsubmit = saveAddress;

    $("#cardForm").onsubmit = useNewCard;

    $("#checkoutBtn").onclick = onCheckout;
    updateCheckoutButton();
  });

  async function loadSubtotal(){
    try{
      const res = await fetch("/api/cart",{headers: window.__auth.authHeader()});
      if(!res.ok){ throw new Error("Failed to load cart"); }
      const cart = await res.json();
      state.subtotal = (cart.items||[]).reduce((a,i)=> a + Number(i.unitPrice) * Number(i.quantity), 0);
      $("#subtotal").textContent = money(state.subtotal);
    }catch{ flash("Could not load cart subtotal."); }
  }

  async function loadAddresses(selectId=null){
    try{
      const res = await fetch("/api/profile/addresses",{headers: window.__auth.authHeader()});
      if(!res.ok) throw new Error();
      state.addresses = await res.json();
      if (selectId) state.selectedAddressId = selectId;
      renderAddresses();
    }catch{ flash("Could not load addresses."); }
  }

  async function loadMethods(){
    try{
      const res = await fetch("/api/payments/methods",{headers: window.__auth.authHeader()});
      if(!res.ok) throw new Error();
      state.methods = await res.json();
      renderMethods();
    }catch{ flash("Could not load payment methods."); }
  }

  function renderAddresses(){
    const wrap = $("#addressList");
    wrap.innerHTML = "";
    if(!state.addresses.length){
      wrap.innerHTML = `<div class="muted">No saved addresses. Use “Add address”.</div>`;
      state.selectedAddressId = null;
      updateCheckoutButton();
      return;
    }

    let selectedId = state.selectedAddressId;
    if (!selectedId) {
      const def = state.addresses.find(a => !!a.isDefault);
      selectedId = def?.id ?? state.addresses[0].id;
      state.selectedAddressId = selectedId;
    }

    state.addresses.forEach((a) => {
      const checked = String(a.id) === String(selectedId);
      const div = document.createElement("div");
      div.className = "item";
      div.innerHTML = `
        <div class="meta">
          <input type="radio" name="addr" value="${a.id}" ${checked ? "checked":""}>
          <div>
            <div><strong>${esc(a.title)}</strong> ${a.isDefault ? '<span class="badge">Default</span>':''}</div>
            <div class="muted small">${esc(a.recipientName)} — ${esc(a.line1)} ${a.line2?esc(a.line2):""}, ${esc(a.city)} ${a.state?esc(a.state):""} ${a.postalCode?esc(a.postalCode):""}, ${esc(a.country)} • ${esc(a.phone)}</div>
          </div>
        </div>`;
      div.querySelector('input').addEventListener('change', ()=>{
        state.selectedAddressId = a.id;
        updateCheckoutButton();
      });
      wrap.appendChild(div);
    });

    updateCheckoutButton();
  }

  function renderMethods(){
    const wrap = $("#methodList");
    wrap.innerHTML = "";
    if(!state.methods.length){
      wrap.innerHTML = `<div class="muted">No saved cards.</div>`;
    }

    state.methods.forEach(m=>{
      const div = document.createElement("div");
      div.className = "item";
      div.innerHTML = `
        <div class="meta">
          <input type="radio" name="pm" value="${m.id}">
          <div><strong>${esc(m.brand)}</strong> •••• ${esc(m.last4)} (exp ${esc(m.expiryMonth)}/${esc(m.expiryYear)})</div>
        </div>`;
      div.querySelector('input').addEventListener('change', ()=>{
        state.selectedMethodId = m.id;
        state.tokenPayment = null; 
        showTokenInfo(null);
        updateCheckoutButton();
      });
      wrap.appendChild(div);
    });

    updateCheckoutButton();
  }

  async function saveAddress(ev){
    ev.preventDefault();
    const f = ev.target;
    const body = {
      title: f.title.value.trim(),
      recipientName: f.recipientName.value.trim(),
      line1: f.line1.value.trim(),
      line2: f.line2.value.trim() || null,
      city: f.city.value.trim(),
      state: f.state.value.trim() || null,
      postalCode: f.postalCode.value.trim() || null,
      country: f.country.value.trim(),
      phone: f.phone.value.trim(),
      isDefault: !!f.isDefault.checked
    };
    try{
      const res = await fetch("/api/profile/addresses", {
        method:"POST",
        headers:{...window.__auth.authHeader(),"Content-Type":"application/json"},
        body: JSON.stringify(body)
      });
      if(!res.ok){
        const e=await res.json().catch(()=>({}));
        throw new Error(e.message||"failed");
      }
      const saved = await res.json();
      flash("Address saved", "ok");

      f.reset();
      f.hidden = true;

      await loadAddresses(saved.id);
    }catch{
      flash("Could not save address.");
    }
  }

  async function useNewCard(ev){
    ev.preventDefault();
    const f = ev.target;
    const body = {
      cardNumber: f.cardNumber.value.replace(/\s+/g, ""),
      expiryMonth: f.expiryMonth.value,
      expiryYear: f.expiryYear.value,
      cvv: f.cvv.value,
      save: !!f.save.checked
    };
    try{
      const res = await fetch("/api/payments/tokenize", {
        method:"POST", headers:{...window.__auth.authHeader(),"Content-Type":"application/json"},
        body: JSON.stringify(body)
      });
      if(!res.ok){ const e=await res.json().catch(()=>({})); throw new Error(e.message||"failed"); }
      const tok = await res.json();
      state.tokenPayment = {
        token: tok.token,
        last4: tok.last4,
        brand: tok.brand,
        expiresAt: tok.expiresAtEpochMs ?? null
      };
      state.selectedMethodId = null; 
      showTokenInfo(state.tokenPayment);
      flash("Card is ready for checkout", "ok");

      if(body.save){ await loadMethods(); }
      updateCheckoutButton();
    }catch{
      flash("Could not tokenize card.");
    }
  }

  async function onCheckout(){
    if(!state.selectedAddressId){ flash("Please select a shipping address."); return; }
    const payingWithSaved = !!state.selectedMethodId;
    const payingWithToken = !!state.tokenPayment;

    if(!payingWithSaved && !payingWithToken){
      flash("Please select a payment method or enter a new card.");
      return;
    }

    if(payingWithToken && tokenExpired(state.tokenPayment)){
      flash("Your one-time card token expired. Please re-enter the card.");
      state.tokenPayment = null;
      showTokenInfo(null);
      updateCheckoutButton();
      return;
    }

    try{
      let res;
      if(payingWithSaved){
        res = await fetch("/api/orders/checkout/saved", {
          method:"POST", headers:{...window.__auth.authHeader(),"Content-Type":"application/json"},
          body: JSON.stringify({
            savedPaymentMethodId: state.selectedMethodId,
            shippingAddressId: state.selectedAddressId
          })
        });
      }else{
        res = await fetch("/api/orders/checkout/token", {
          method:"POST", headers:{...window.__auth.authHeader(),"Content-Type":"application/json"},
          body: JSON.stringify({
            paymentToken: state.tokenPayment.token,
            shippingAddressId: state.selectedAddressId
          })
        });
      }

      if(!res.ok){
        const e = await res.json().catch(()=>({}));
        if(e?.message?.toLowerCase?.().includes("token")){
          flash("Payment token invalid or expired. Please re-enter your card.");
          state.tokenPayment = null;
          showTokenInfo(null);
          updateCheckoutButton();
          return;
        }
        throw new Error(e.message || "checkout_failed");
      }
      const order = await res.json();
      flash("Order completed!", "ok");
      setTimeout(()=> window.location = `/orders.html?id=${order.orderId ?? ""}`, 800);
    }catch{
      flash("Checkout failed. Please try again.");
    }
  }

  function updateCheckoutButton(){
    const hasAddress = !!state.selectedAddressId;
    const hasPayment = !!state.selectedMethodId || (!!state.tokenPayment && !tokenExpired(state.tokenPayment));
    $("#checkoutBtn").disabled = !(hasAddress && hasPayment);
  }

  function money(n){ return new Intl.NumberFormat('tr-TR',{style:'currency',currency:'TRY'}).format(n); }
  function esc(s){ return String(s??"").replace(/[&<>"']/g, m=>({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[m])); }
  function tokenExpired(tp){ if(!tp?.expiresAt) return false; return Date.now() > Number(tp.expiresAt); }
  function showTokenInfo(tp){
    const el = $("#tokenInfo");
    if(!el) return;
    el.textContent = tp
      ? `One-time card ready: ${tp.brand} •••• ${tp.last4}${tp.expiresAt ? " • expires at " + new Date(tp.expiresAt).toLocaleTimeString() : ""}`
      : "";
  }
})();

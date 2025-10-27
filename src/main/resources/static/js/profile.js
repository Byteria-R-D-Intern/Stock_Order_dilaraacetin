(() => {
  const $ = (s) => document.querySelector(s);

  const flash = (msg, type = "error") => {
    const el = $("#flash");
    el.textContent = msg;
    el.className = `flash ${type}`;
    el.hidden = false;
    setTimeout(() => (el.hidden = true), 4000);
  };

  const api = {
    async getAddresses() {
      return fetch("/api/profile/addresses", {
        headers: __auth.authHeader(),
      });
    },
    async createAddress(body) {
      return fetch("/api/profile/addresses", {
        method: "POST",
        headers: {
          ...__auth.authHeader(),
          "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
      });
    },
    async updateAddress(id, body) {
      return fetch(`/api/profile/addresses/${id}`, {
        method: "PUT",
        headers: {
          ...__auth.authHeader(),
          "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
      });
    },
    async deleteAddress(id) {
      return fetch(`/api/profile/addresses/${id}`, {
        method: "DELETE",
        headers: __auth.authHeader(),
      });
    },
    async makeDefaultAddress(id) {
      return fetch(`/api/profile/addresses/${id}/default`, {
        method: "PUT",
        headers: __auth.authHeader(),
      });
    },
    async getCards() {
      return fetch("/api/payments/methods", {
        headers: __auth.authHeader(),
      });
    },
    async deleteCard(id) {
      return fetch(`/api/payments/methods/${id}`, {
        method: "DELETE",
        headers: __auth.authHeader(),
      });
    },
  };

  const state = { addresses: [], cards: [] };

  document.addEventListener("DOMContentLoaded", async () => {
    if (!__auth.requireAuthOrRedirect()) return;
    $("#logoutBtn")?.addEventListener("click", __auth.logout);

    await Promise.all([loadAddresses(), loadCards()]);

    $("#toggleAddrForm").onclick = () =>
      ($("#addrForm").hidden = !$("#addrForm").hidden);
    $("#cancelAddrForm").onclick = () => ($("#addrForm").hidden = true);
    $("#addrForm").onsubmit = onCreateOrUpdateAddress;
  });

  async function loadAddresses() {
    try {
      const res = await api.getAddresses();
      if (!res.ok) throw 0;
      state.addresses = await res.json();
      renderAddresses();
    } catch {
      flash("Could not load addresses");
    }
  }

  function renderAddresses() {
    const wrap = $("#addrList");
    wrap.innerHTML = "";

    if (!state.addresses.length) {
      wrap.innerHTML = `<div class="muted">No saved addresses</div>`;
      return;
    }

    state.addresses.forEach((a) => {
      const div = document.createElement("div");
      div.className = "item";
      div.innerHTML = `
        <div class="meta">
          <div>
            <div class="title">
              ${esc(a.title)} ${
        a.isDefault ? '<span class="badge small">Default</span>' : ""
      }
            </div>
            <div class="muted small">
              ${esc(a.recipientName)} — ${esc(a.line1)} ${
        a.line2 ? esc(a.line2) : ""
      }, ${esc(a.city)} ${a.state ? esc(a.state) : ""} ${
        a.postalCode ? esc(a.postalCode) : ""
      }, ${esc(a.country)} • ${esc(a.phone)}
            </div>
          </div>
        </div>
        <div class="row">
          ${
            a.isDefault
              ? ""
              : '<button class="btn ghost makeDefault">Make default</button>'
          }
          <button class="btn ghost edit">Edit</button>
          <button class="btn danger del">Delete</button>
        </div>
      `;

      div.querySelector(".makeDefault")?.addEventListener("click", async () => {
        try {
          const r = await api.makeDefaultAddress(a.id);
          if (!r.ok) throw 0;
          flash("Default address updated", "ok");
          await loadAddresses();
        } catch {
          flash("Could not set default address");
        }
      });

      div.querySelector(".edit").addEventListener("click", () => {
        const f = $("#addrForm");
        f.reset();
        f.dataset.editingId = a.id;
        f.title.value = a.title ?? "";
        f.recipientName.value = a.recipientName ?? "";
        f.line1.value = a.line1 ?? "";
        f.line2.value = a.line2 ?? "";
        f.city.value = a.city ?? "";
        f.state.value = a.state ?? "";
        f.postalCode.value = a.postalCode ?? "";
        f.country.value = a.country ?? "";
        f.phone.value = a.phone ?? "";
        f.isDefault.checked = !!a.isDefault;
        f.hidden = false;
        f.querySelector("button[type='submit']").textContent = "Update Address";
      });

      div.querySelector(".del").addEventListener("click", async () => {
        if (!confirm("Delete this address?")) return;
        try {
          const r = await api.deleteAddress(a.id);
          if (!r.ok) throw 0;
          flash("Address deleted", "ok");
          await loadAddresses();
        } catch {
          flash("Could not delete address");
        }
      });

      wrap.appendChild(div);
    });
  }

  async function onCreateOrUpdateAddress(ev) {
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
      isDefault: !!f.isDefault.checked,
    };
    try {
      let res;
      const editingId = f.dataset.editingId;
      if (editingId) res = await api.updateAddress(editingId, body);
      else res = await api.createAddress(body);

      if (!res.ok) {
        const e = await res.json().catch(() => ({}));
        throw new Error(e.message || "failed");
      }
      flash(editingId ? "Address updated" : "Address saved", "ok");
      f.reset();
      f.hidden = true;
      delete f.dataset.editingId;
      f.querySelector("button[type='submit']").textContent = "Save Address";
      await loadAddresses();
    } catch {
      flash("Could not save address");
    }
  }

  async function loadCards() {
    try {
      const res = await api.getCards();
      if (!res.ok) throw 0;
      state.cards = await res.json();
      renderCards();
    } catch {}
  }

  function renderCards() {
    const wrap = $("#pmList");
    wrap.innerHTML = "";
    const list = state.cards || [];
    if (!list.length) {
      wrap.innerHTML = `<div class="muted">No saved cards</div>`;
      return;
    }
    list.forEach((m) => {
      const div = document.createElement("div");
      div.className = "item";
      div.innerHTML = `
        <div class="meta">
          <div>
            <strong>${esc(m.brand || "CARD")}</strong> •••• ${esc(m.last4)}
            <span class="muted small">(exp ${esc(m.expiryMonth)}/${esc(
        m.expiryYear
      )})</span>
          </div>
        </div>
        <div class="row">
          <button class="btn danger del">Delete</button>
        </div>
      `;
      div.querySelector(".del").addEventListener("click", async () => {
        if (!confirm("Delete this card?")) return;
        try {
          const r = await api.deleteCard(m.id);
          if (!r.ok) throw 0;
          flash("Card deleted", "ok");
          await loadCards();
        } catch {
          flash("Could not delete card");
        }
      });
      wrap.appendChild(div);
    });
  }

  function esc(s) {
    return String(s ?? "").replace(
      /[&<>"']/g,
      (m) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#39;",
        }[m])
    );
  }
})();

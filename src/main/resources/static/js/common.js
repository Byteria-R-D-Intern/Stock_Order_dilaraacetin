(function () {
  const storageKey = "auth";

  function getAuth() {
    try { return JSON.parse(localStorage.getItem(storageKey) || "null"); }
    catch { return null; }
  }

  function requireAuthOrRedirect() {
    const a = getAuth();
    if (!a?.token) {
      window.location.replace("/login.html");
      return null;
    }
    return a;
  }

  function authHeader() {
    const a = getAuth();
    if (!a?.token) return {};
    return { "Authorization": `${a.tokenType || "Bearer"} ${a.token}` };
  }

  function logout() {
    try { localStorage.removeItem(storageKey); } catch {}
    window.location.replace("/login.html");
  }

  function b64urlToStr(b64url){
    try{
      const pad = '='.repeat((4 - (b64url.length % 4)) % 4);
      const b64 = (b64url + pad).replace(/-/g,'+').replace(/_/g,'/');
      return decodeURIComponent(escape(atob(b64)));
    }catch{ return "{}"; }
  }
  function parseJwt(token){
    try{
      const [, payload] = token.split('.');
      return JSON.parse(b64urlToStr(payload || ""));
    }catch{ return {}; }
  }

  function rolesFromToken(){
    const a = getAuth();
    if(!a?.token) return [];
    const payload = parseJwt(a.token);

    let roles = [];
    if (payload.role) roles = [payload.role];
    if (Array.isArray(payload.roles)) roles = roles.concat(payload.roles);
    if (Array.isArray(payload.authorities)) roles = roles.concat(payload.authorities);
    if (typeof payload.scope === "string") roles = roles.concat(payload.scope.split(/\s+/));

    return roles.map(r => String(r).toUpperCase());
  }

  function isAdmin(){
    const r = rolesFromToken();
    return r.includes("ADMIN") || r.includes("ROLE_ADMIN");
  }

  function ensureAdminButton(){
    const el = document.getElementById("adminBtn");
    if (!el) return;
    if (isAdmin()) el.classList.remove("hidden");
    else el.classList.add("hidden");
  }

  let __notifTimer = null;

  async function refreshUnreadBadge() {
    const badge = document.getElementById("notifBadge");
    if (!badge) return;
    try {
      const res = await fetch("/api/notifications/unread-count", { headers: authHeader() });
      if (!res.ok) throw 0;
      const data = await res.json();
      const n = Number(data?.unread || 0);
      if (n > 0) {
        badge.textContent = n > 99 ? "99+" : String(n);
        badge.style.display = "inline-block";
      } else {
        badge.textContent = "0";
        badge.style.display = "none";
      }
    } catch {
    }
  }

  function wireNotificationsBadge() {
    refreshUnreadBadge();
    clearInterval(__notifTimer);
    __notifTimer = setInterval(refreshUnreadBadge, 30000);
    window.addEventListener("visibilitychange", () => {
      if (!document.hidden) refreshUnreadBadge();
    });
  }

  window.__auth = {
    getAuth,
    requireAuthOrRedirect,
    authHeader,
    logout,
    parseJwt,
    rolesFromToken,
    isAdmin,
    ensureAdminButton,
    refreshUnreadBadge,
    wireNotificationsBadge
  };
})();

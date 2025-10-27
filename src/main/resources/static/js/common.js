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

  window.__auth = { getAuth, requireAuthOrRedirect, authHeader, logout };
})();

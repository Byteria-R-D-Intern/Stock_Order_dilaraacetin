const switchers = [...document.querySelectorAll('.switcher')];
switchers.forEach(item => {
  item.addEventListener('click', function() {
    switchers.forEach(s => s.parentElement.classList.remove('is-active'));
    this.parentElement.classList.add('is-active');
  });
});

const apiBase = "";
const storageKey = "auth"; 

const loginForm   = document.getElementById("loginForm");
const signupForm  = document.getElementById("signupForm");
const loginMsg    = document.getElementById("loginMessage");
const signupMsg   = document.getElementById("signupMessage");

const showMsg = (el, text, ok = false) => {
  if (!el) return;
  el.textContent = text || "";
  el.classList.toggle("ok", !!ok);
};

const saveAuth = (payload) => {
  try { localStorage.setItem(storageKey, JSON.stringify(payload)); } catch {}
};

loginForm?.addEventListener("submit", async (e) => {
  e.preventDefault();
  showMsg(loginMsg, "");

  const email = document.getElementById("login-email")?.value.trim();
  const password = document.getElementById("login-password")?.value;

  try {
    const res = await fetch(`${apiBase}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      showMsg(loginMsg, err?.message || "Login failed");
      return;
    }
    const data = await res.json(); 
    saveAuth(data);
    showMsg(loginMsg, "Login successful!", true);
    setTimeout(() => { window.location.href = "/products.html"; }, 600);
  } catch {
    showMsg(loginMsg, "Network error");
  }
});

signupForm?.addEventListener("submit", async (e) => {
  e.preventDefault();
  showMsg(signupMsg, "");

  const email = document.getElementById("signup-email")?.value.trim();
  const pass1 = document.getElementById("signup-password")?.value;
  const pass2 = document.getElementById("signup-password-confirm")?.value;

  if (pass1 !== pass2) {
    showMsg(signupMsg, "Passwords do not match");
    return;
  }

  try {
    const res = await fetch(`${apiBase}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password: pass1 })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      showMsg(signupMsg, err?.message || "Registration failed");
      return;
    }
    showMsg(signupMsg, "Registered successfully. Please login.", true);
    document.querySelector(".switcher-login")?.click();
  } catch {
    showMsg(signupMsg, "Network error");
  }
});

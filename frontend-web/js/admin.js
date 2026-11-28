// Lógica independiente del Panel Administrativo de GestoPago
const ADMIN_API_BASE = 'https://gestopago-i7hw.onrender.com';

const adminApp = {
  cuentas: [],
  filtro: '',
  abonosSesion: 0,

  getToken() {
    return localStorage.getItem('gestopago_admin_token');
  },

  setToken(token, correo) {
    if (token) {
      localStorage.setItem('gestopago_admin_token', token);
      if (correo) localStorage.setItem('gestopago_admin_email', correo);
    } else {
      localStorage.removeItem('gestopago_admin_token');
      localStorage.removeItem('gestopago_admin_email');
    }
  },

  formatoMoneda(n) {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(Number(n || 0));
  },

  async request(endpoint, options = {}) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 25000);
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const token = this.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    try {
      const resp = await fetch(`${ADMIN_API_BASE}${endpoint}`, {
        ...options,
        headers,
        signal: controller.signal
      });
      clearTimeout(timeoutId);
      const data = await resp.json().catch(() => ({}));

      if ((resp.status === 401 || resp.status === 403) && !endpoint.includes('/login')) {
        this.setToken(null);
        this.mostrarLogin(data.mensaje || 'Tu sesión administrativa expiró. Vuelve a iniciar sesión.');
        return { ok: false, status: resp.status, data: null, mensaje: data.mensaje || 'Sesión expirada' };
      }

      return {
        ok: resp.ok,
        status: resp.status,
        data: data.datos !== undefined ? data.datos : data,
        mensaje: data.mensaje || ''
      };
    } catch (_) {
      clearTimeout(timeoutId);
      return {
        ok: false,
        status: 0,
        data: null,
        mensaje: 'No pudimos conectar con el servicio. Intenta de nuevo en unos segundos.'
      };
    }
  },

  async init() {
    this.configurarValidaciones();
    const token = this.getToken();
    if (token) {
      const check = await this.request('/api/admin/sesion', { method: 'GET' });
      if (check.ok) {
        this.mostrarDashboard();
      } else {
        this.mostrarLogin();
      }
    } else {
      this.mostrarLogin();
    }
    if (window.lucide) lucide.createIcons();
  },

  configurarValidaciones() {
    const ctaEl = document.getElementById('admin-cuenta');
    const montoEl = document.getElementById('admin-monto');

    if (ctaEl) {
      ctaEl.addEventListener('input', () => {
        ctaEl.value = ctaEl.value.replace(/\D/g, '').slice(0, 10);
        this.validarCuenta();
      });
    }
    if (montoEl) {
      montoEl.addEventListener('input', () => {
        const limpio = montoEl.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');
        const p = limpio.split('.');
        montoEl.value = p.length > 1 ? `${p[0]}.${p[1].slice(0, 2)}` : limpio;
        this.validarMonto();
      });
    }
  },

  validarCuenta() {
    const el = document.getElementById('admin-cuenta');
    const err = document.getElementById('err-admin-cuenta');
    const v = (el?.value || '').trim();
    let msg = '';
    if (!v) msg = 'Ingresa el número de cuenta';
    else if (!/^\d{10}$/.test(v)) msg = `Debe tener exactamente 10 dígitos (llevas ${v.length})`;
    this.pintarError(el, err, msg);
    return !msg;
  },

  validarMonto() {
    const el = document.getElementById('admin-monto');
    const err = document.getElementById('err-admin-monto');
    const v = (el?.value || '').trim();
    const n = Number(v);
    let msg = '';
    if (!v) msg = 'Ingresa el monto a abonar';
    else if (Number.isNaN(n) || n <= 0) msg = 'El monto debe ser mayor a $0.00';
    else if (n > 500000) msg = 'El abono máximo por operación es de $500,000 MXN';
    this.pintarError(el, err, msg);
    return !msg;
  },

  pintarError(inputEl, errEl, msg) {
    if (!inputEl || !errEl) return;
    if (msg) {
      inputEl.classList.add('border-red-400', 'ring-1', 'ring-red-300');
      errEl.textContent = msg;
      errEl.classList.remove('hidden');
    } else {
      inputEl.classList.remove('border-red-400', 'ring-1', 'ring-red-300');
      errEl.textContent = '';
      errEl.classList.add('hidden');
    }
  },

  togglePassword(id, btn) {
    const el = document.getElementById(id);
    if (!el) return;
    const show = el.type === 'password';
    el.type = show ? 'text' : 'password';
    if (btn) {
      btn.innerHTML = `<i data-lucide="${show ? 'eye-off' : 'eye'}" class="w-4 h-4"></i>`;
      if (window.lucide) lucide.createIcons();
    }
  },

  mostrarLogin(mensajeError = '') {
    document.getElementById('admin-login-view').classList.remove('hidden');
    document.getElementById('admin-dashboard-view').classList.add('hidden');
    const alert = document.getElementById('admin-login-alert');
    if (mensajeError && alert) {
      alert.className = 'mb-4 p-3 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200';
      alert.textContent = mensajeError;
      alert.classList.remove('hidden');
    } else if (alert) {
      alert.classList.add('hidden');
    }
    if (window.lucide) lucide.createIcons();
  },

  mostrarDashboard() {
    document.getElementById('admin-login-view').classList.add('hidden');
    document.getElementById('admin-dashboard-view').classList.remove('hidden');
    const emailEl = document.getElementById('admin-header-email');
    if (emailEl) {
      emailEl.textContent = localStorage.getItem('gestopago_admin_email') || 'admin@gestopago.com';
    }
    this.cargarCuentas();
    if (window.lucide) lucide.createIcons();
  },

  async handleLogin(e) {
    e.preventDefault();
    const correo = document.getElementById('admin-login-email').value.trim();
    const password = document.getElementById('admin-login-password').value;
    const alert = document.getElementById('admin-login-alert');
    const btn = document.getElementById('btn-admin-login');

    if (!correo || !password) {
      alert.className = 'mb-4 p-3 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200';
      alert.textContent = 'Ingresa tu correo y contraseña de administrador.';
      alert.classList.remove('hidden');
      return;
    }

    btn.disabled = true;
    btn.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> <span>Verificando...</span>`;
    alert.classList.add('hidden');

    const resp = await this.request('/api/admin/login', {
      method: 'POST',
      body: JSON.stringify({ correo, password })
    });

    btn.disabled = false;
    btn.innerHTML = `<i data-lucide="shield-check" class="w-4 h-4"></i> <span>Ingresar al Panel Admin</span>`;
    if (window.lucide) lucide.createIcons();

    if (resp.ok && resp.data?.token) {
      this.setToken(resp.data.token, resp.data.correo || correo);
      document.getElementById('admin-login-password').value = '';
      this.mostrarDashboard();
    } else {
      alert.className = 'mb-4 p-3 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200';
      alert.textContent = resp.mensaje || 'Credenciales de administrador incorrectas.';
      alert.classList.remove('hidden');
    }
  },

  logout() {
    this.setToken(null);
    this.cuentas = [];
    this.abonosSesion = 0;
    this.mostrarLogin();
  },

  async cargarCuentas() {
    const tbody = document.getElementById('admin-cuentas-tbody');
    tbody.innerHTML = `<tr><td colspan="4" class="py-10 text-center text-slate-400">Consultando clientes y saldos...</td></tr>`;

    const resp = await this.request('/api/admin/cuentas', { method: 'GET' });
    if (resp.ok && Array.isArray(resp.data)) {
      this.cuentas = resp.data;
      const saldoTotal = this.cuentas.reduce((acc, c) => acc + Number(c.saldoActual || 0), 0);
      document.getElementById('kpi-total-cuentas').textContent = String(this.cuentas.length);
      document.getElementById('kpi-saldo-total').textContent = this.formatoMoneda(saldoTotal);
      this.renderTabla();
    } else {
      tbody.innerHTML = `<tr><td colspan="4" class="py-10 text-center text-red-500 font-semibold">${resp.mensaje || 'No se pudieron cargar las cuentas.'}</td></tr>`;
    }
  },

  filtrar(q) {
    this.filtro = (q || '').trim().toLowerCase();
    this.renderTabla();
  },

  renderTabla() {
    const tbody = document.getElementById('admin-cuentas-tbody');
    const q = this.filtro;
    const filtradas = q
      ? this.cuentas.filter(c =>
          (c.nombreCompleto || '').toLowerCase().includes(q) ||
          (c.correo || '').toLowerCase().includes(q) ||
          String(c.numeroCuenta || '').includes(q)
        )
      : this.cuentas;

    if (!filtradas.length) {
      tbody.innerHTML = `<tr><td colspan="4" class="py-10 text-center text-slate-400">No se encontraron cuentas con ese criterio.</td></tr>`;
      return;
    }

    tbody.innerHTML = filtradas.map(c => `
      <tr class="hover:bg-brand-50/40 transition-colors cursor-pointer" onclick="adminApp.seleccionar('${c.numeroCuenta}')">
        <td class="py-3.5 px-5">
          <span class="block font-bold text-slate-900">${c.nombreCompleto || 'Cliente'}</span>
          <span class="text-[11px] text-slate-500">${c.correo || 'Sin correo'}</span>
        </td>
        <td class="py-3.5 px-4 font-mono font-bold text-brand-700">${c.numeroCuenta}</td>
        <td class="py-3.5 px-4 text-right font-extrabold text-emerald-700">${this.formatoMoneda(c.saldoActual)}</td>
        <td class="py-3.5 px-5 text-center">
          <button type="button" onclick="event.stopPropagation(); adminApp.seleccionar('${c.numeroCuenta}')" class="px-3 py-1.5 rounded-lg bg-brand-50 hover:bg-brand-100 text-brand-700 font-bold text-[11px]">
            Seleccionar
          </button>
        </td>
      </tr>
    `).join('');
  },

  seleccionar(numeroCuenta) {
    const el = document.getElementById('admin-cuenta');
    if (el) {
      el.value = numeroCuenta;
      this.validarCuenta();
    }
    document.getElementById('admin-monto')?.focus();
  },

  setMonto(m) {
    const el = document.getElementById('admin-monto');
    if (el) {
      el.value = String(m);
      this.validarMonto();
    }
  },

  mostrarAlertaAbono(tipo, msg) {
    const box = document.getElementById('admin-abono-alert');
    box.className = tipo === 'success'
      ? 'p-3 rounded-xl text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200'
      : 'p-3 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200';
    box.textContent = msg;
    box.classList.remove('hidden');
  },

  async handleAbono(e) {
    e.preventDefault();
    const okCta = this.validarCuenta();
    const okMonto = this.validarMonto();
    if (!okCta || !okMonto) {
      this.mostrarAlertaAbono('error', 'Corrige los campos marcados en rojo.');
      return;
    }

    const numeroCuenta = document.getElementById('admin-cuenta').value.trim();
    const monto = Number(document.getElementById('admin-monto').value);
    const btn = document.getElementById('btn-admin-abonar');

    btn.disabled = true;
    btn.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> <span>Aplicando abono...</span>`;

    const resp = await this.request('/api/admin/abono', {
      method: 'POST',
      body: JSON.stringify({ numeroCuenta, monto })
    });

    btn.disabled = false;
    btn.innerHTML = `<i data-lucide="plus-circle" class="w-4 h-4"></i> <span>Aplicar Abono</span>`;
    if (window.lucide) lucide.createIcons();

    if (resp.ok) {
      this.abonosSesion++;
      document.getElementById('kpi-abonos-sesion').textContent = String(this.abonosSesion);
      document.getElementById('admin-monto').value = '';
      this.mostrarAlertaAbono('success', `✓ Abono de ${this.formatoMoneda(monto)} acreditado a la cuenta ${numeroCuenta}.`);
      await this.cargarCuentas();
    } else {
      this.mostrarAlertaAbono('error', resp.mensaje || 'No se pudo aplicar el abono.');
    }
  }
};

document.addEventListener('DOMContentLoaded', () => adminApp.init());

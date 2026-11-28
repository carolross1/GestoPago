// Cliente API REST de GestoPago (Conectado al backend en la nube: Render + Neon PostgreSQL)
const API_BASE = 'https://gestopago-i7hw.onrender.com';

const API = {
  onSessionExpired: null,

  getToken() {
    return localStorage.getItem('gestopago_token');
  },

  setToken(token) {
    if (token) {
      localStorage.setItem('gestopago_token', token);
    } else {
      localStorage.removeItem('gestopago_token');
    }
  },

  getUsuario() {
    const raw = localStorage.getItem('gestopago_usuario');
    return raw ? JSON.parse(raw) : null;
  },

  setUsuario(usuario) {
    if (usuario) {
      localStorage.setItem('gestopago_usuario', JSON.stringify(usuario));
    } else {
      localStorage.removeItem('gestopago_usuario');
    }
  },

  getCuenta() {
    const raw = localStorage.getItem('gestopago_cuenta');
    return raw ? JSON.parse(raw) : null;
  },

  setCuenta(cuenta) {
    if (cuenta) {
      localStorage.setItem('gestopago_cuenta', JSON.stringify(cuenta));
    } else {
      localStorage.removeItem('gestopago_cuenta');
    }
  },

  logout() {
    localStorage.removeItem('gestopago_token');
    localStorage.removeItem('gestopago_usuario');
    localStorage.removeItem('gestopago_cuenta');
  },

  async request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 25000);

    try {
      const response = await fetch(url, {
        ...options,
        headers,
        signal: controller.signal
      });
      clearTimeout(timeoutId);

      const data = await response.json().catch(() => ({}));

      if (response.status === 401 && !endpoint.includes('/auth/login') && !endpoint.includes('/auth/logout')) {
        const msgSesion = data.mensaje || 'Tu sesión fue cerrada porque se inició sesión en otro dispositivo.';
        const teniaToken = Boolean(this.getToken());
        this.logout();
        if (teniaToken && typeof this.onSessionExpired === 'function') {
          this.onSessionExpired(msgSesion);
        }
        return {
          ok: false,
          status: 401,
          data: null,
          mensaje: msgSesion,
          errores: []
        };
      }

      const mensajeAmigable = data.mensaje || (response.ok ? '' : 'No pudimos completar tu solicitud. Intenta de nuevo.');
      return {
        ok: response.ok,
        status: response.status,
        data: data.datos !== undefined ? data.datos : data,
        mensaje: mensajeAmigable,
        errores: Array.isArray(data.errores) ? data.errores : []
      };
    } catch (err) {
      clearTimeout(timeoutId);
      console.error(`Error en petición a ${endpoint}:`, err);
      const mensajeUsuario = err && err.name === 'AbortError'
        ? 'El servicio tardó demasiado en responder. Intenta de nuevo en unos segundos.'
        : 'No pudimos conectar con el servicio. Verifica tu conexión a internet e intenta de nuevo.';
      return {
        ok: false,
        status: 0,
        data: null,
        mensaje: mensajeUsuario,
        errores: []
      };
    }
  },

  // Auth
  async login(correo, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ correo, password })
    });
  },

  async registrar(correo, password) {
    return this.request('/auth/registro', {
      method: 'POST',
      body: JSON.stringify({ correo, password })
    });
  },

  async verificarCorreo(correo) {
    return this.request('/auth/verificar-correo', {
      method: 'POST',
      body: JSON.stringify({ correo })
    });
  },

  async verificarSesion() {
    return this.request('/auth/sesion', { method: 'GET' });
  },

  async cerrarSesionRemota() {
    return this.request('/auth/logout', { method: 'POST' });
  },

  async crearCliente(payload) {
    return this.request('/clientes', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async consultarCodigoPostal(cp) {
    const resp = await this.request(`/catalogos/cp/${encodeURIComponent(cp)}`, {
      method: 'GET'
    });
    if (resp.ok && resp.data) {
      return { ok: true, data: resp.data };
    }
    // Respaldo directo a Zippopotam si el backend aún está desplegando
    try {
      const r = await fetch(`https://api.zippopotam.us/mx/${encodeURIComponent(cp)}`);
      if (!r.ok) return { ok: false, data: null };
      const j = await r.json();
      const places = j.places || [];
      if (!places.length) return { ok: false, data: null };
      let estado = places[0].state || '';
      if (estado.toLowerCase() === 'distrito federal') estado = 'Ciudad de Mexico';
      const colonias = [...new Set(places.map(p => (p['place name'] || '').trim()).filter(Boolean))];
      return {
        ok: true,
        data: { codigoPostal: cp, pais: 'Mexico', estado, municipio: estado, colonias }
      };
    } catch (_) {
      return { ok: false, data: null };
    }
  },

  async obtenerProfesiones() {
    return this.request('/catalogos/profesiones', { method: 'GET' });
  },

  // Clientes y Cuentas
  async buscarCliente(correo) {
    const resp = await this.request('/clientes/buscar', {
      method: 'POST',
      body: JSON.stringify({ correo })
    });
    if (resp.ok && resp.data) {
      const lista = resp.data.contenido || resp.data.elementos || (Array.isArray(resp.data) ? resp.data : []);
      if (lista.length > 0) {
        return { ok: true, data: lista[0] };
      }
    }
    return { ok: false, data: null, mensaje: 'Cliente no encontrado' };
  },

  async buscarCuenta(clienteId) {
    const resp = await this.request('/cuentas/buscar', {
      method: 'POST',
      body: JSON.stringify({ clienteId: Number(clienteId) })
    });
    if (resp.ok && resp.data) {
      const lista = resp.data.contenido || resp.data.elementos || (Array.isArray(resp.data) ? resp.data : []);
      if (lista.length > 0) {
        return { ok: true, data: lista[0] };
      }
    }
    return { ok: false, data: null, mensaje: 'Cuenta no encontrada' };
  },

  // Administración (Listar clientes/cuentas y abonar saldo)
  async listarClientes() {
    const resp = await this.request('/clientes/buscar', {
      method: 'POST',
      body: JSON.stringify({ pagina: 0, tamano: 100 })
    });
    if (resp.ok && resp.data) {
      const lista = resp.data.contenido || resp.data.elementos || (Array.isArray(resp.data) ? resp.data : []);
      return { ok: true, data: lista };
    }
    return { ok: false, data: [], mensaje: resp.mensaje || 'No se pudieron consultar los clientes' };
  },

  async listarCuentas() {
    const resp = await this.request('/cuentas/buscar', {
      method: 'POST',
      body: JSON.stringify({ pagina: 0, tamano: 100 })
    });
    if (resp.ok && resp.data) {
      const lista = resp.data.contenido || resp.data.elementos || (Array.isArray(resp.data) ? resp.data : []);
      return { ok: true, data: lista };
    }
    return { ok: false, data: [], mensaje: resp.mensaje || 'No se pudieron consultar las cuentas' };
  },

  async abonarCuenta(numeroCuenta, monto) {
    return this.request('/cuentas/abono', {
      method: 'POST',
      body: JSON.stringify({
        numeroCuenta: String(numeroCuenta).trim(),
        monto: Number(monto)
      })
    });
  },

  // Catálogo de Productos
  async obtenerProductos() {
    return this.request('/productos', {
      method: 'GET'
    });
  },

  // Transacciones y Pagos
  async validarReferencia(idServicio, idProducto, referencia, tipoDestino) {
    return this.request('/transacciones/referencia/validar', {
      method: 'POST',
      body: JSON.stringify({
        idServicio: Number(idServicio),
        idProducto: Number(idProducto),
        referencia: String(referencia).trim(),
        tipoDestino
      })
    });
  },

  async solicitar2fa(numeroCuenta, enviarCorreo = true) {
    return this.request('/transacciones/2fa', {
      method: 'POST',
      body: JSON.stringify({ numeroCuenta, enviarCorreo })
    });
  },

  async procesarPago(pagoData) {
    return this.request('/transacciones/pago', {
      method: 'POST',
      body: JSON.stringify(pagoData)
    });
  },

  async obtenerHistorial(numeroCuenta) {
    return this.request('/transacciones/historial', {
      method: 'POST',
      body: JSON.stringify({ numeroCuenta })
    });
  },

  // Helper para resolver logotipos oficiales desde GestoPago Portal Ventas por idServicio real
  obtenerLogoUrl(servicio, idServicio) {
    if (idServicio && Number(idServicio) > 0) {
      return `https://gestopago.portalventas.net/sistema/images/gestopago/servicios/${idServicio}.png`;
    }
    const s = (servicio || '').toUpperCase();
    if (s.includes('TELCEL')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/133.png';
    if (s.includes('AT&T') || s.includes('ATT')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/115.png';
    if (s.includes('MOVISTAR')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/102.png';
    if (s.includes('UNEFON')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/116.png';
    if (s.includes('TELMEX')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/107.png';
    if (s.includes('CFE')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/166.png';
    if (s.includes('NATURGY') || s.includes('GAS NATURAL')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/64.png';
    if (s.includes('SKY')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/168.png';
    if (s.includes('MEGACABLE')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/167.png';
    if (s.includes('TOTALPLAY')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/85.png';
    if (s.includes('IZZI')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/76.png';
    if (s.includes('DISH')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/61.png';
    if (s.includes('NETFLIX')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/178.png';
    if (s.includes('AGUA')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/55.png';
    if (s.includes('TELEVIA')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/38.png';
    if (s.includes('PASE')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/154.png';
    if (s.includes('INFONAVIT')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/53.png';
    if (s.includes('AVON')) return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/79.png';
    return 'img/logos/cfe.png';
  }
};

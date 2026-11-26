// Cliente API REST de GestoPago (Conectado al backend en la nube: Render + Neon PostgreSQL)
const API_BASE = 'https://gestopago-i7hw.onrender.com';

const API = {
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

    try {
      const response = await fetch(url, {
        ...options,
        headers
      });

      const data = await response.json().catch(() => ({}));

      if (response.status === 401 && !endpoint.includes('/auth/login')) {
        this.logout();
        window.location.reload();
        throw new Error('Sesión expirada o token inválido');
      }

      return {
        ok: response.ok,
        status: response.status,
        data: data.datos !== undefined ? data.datos : data,
        mensaje: data.mensaje || '',
        errores: data.errores || []
      };
    } catch (err) {
      console.error(`Error en petición a ${endpoint}:`, err);
      return {
        ok: false,
        status: 0,
        data: null,
        mensaje: err.message || 'Error de conexión con el servidor',
        errores: [err.message]
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

  async crearCliente(payload) {
    return this.request('/clientes', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
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

  async solicitar2fa(numeroCuenta) {
    return this.request('/transacciones/2fa', {
      method: 'POST',
      body: JSON.stringify({ numeroCuenta })
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

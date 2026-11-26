// Lógica de la Plataforma Web GestoPago
const app = {
  usuario: null,
  cuenta: null,
  mostrarSaldo: true,
  productos: [],
  categoriaActiva: null,
  queryBusqueda: '',
  historial: [],
  referenciaValidada: false,
  datosValidacion: null,
  pagoPendiente: null,

  init() {
    this.usuario = API.getUsuario();
    this.cuenta = API.getCuenta();

    const token = API.getToken();
    if (token && this.usuario) {
      this.mostrarVistaApp();
      this.cargarDatosUsuario();
    } else {
      this.mostrarVistaAuth();
    }

    if (window.lucide) {
      lucide.createIcons();
    }
  },

  // -------------------------------------------------------------
  // VISTAS Y NAVEGACIÓN
  // -------------------------------------------------------------
  mostrarVistaAuth() {
    document.getElementById('view-auth').classList.remove('hidden');
    document.getElementById('view-app').classList.add('hidden');
  },

  mostrarVistaApp() {
    document.getElementById('view-auth').classList.add('hidden');
    document.getElementById('view-app').classList.remove('hidden');
    this.actualizarUIUsuario();
  },

  setAuthTab(tab) {
    const tabLogin = document.getElementById('tab-login');
    const tabReg = document.getElementById('tab-registro');
    const formLogin = document.getElementById('form-login');
    const formReg = document.getElementById('form-registro');
    const alertBox = document.getElementById('auth-alert');
    alertBox.classList.add('hidden');

    if (tab === 'login') {
      tabLogin.className = 'flex-1 py-2.5 text-xs font-bold rounded-lg transition-all duration-200 bg-white text-brand-700 shadow-sm';
      tabReg.className = 'flex-1 py-2.5 text-xs font-bold rounded-lg transition-all duration-200 text-slate-500 hover:text-slate-800';
      formLogin.classList.remove('hidden');
      formReg.classList.add('hidden');
    } else {
      tabReg.className = 'flex-1 py-2.5 text-xs font-bold rounded-lg transition-all duration-200 bg-white text-brand-700 shadow-sm';
      tabLogin.className = 'flex-1 py-2.5 text-xs font-bold rounded-lg transition-all duration-200 text-slate-500 hover:text-slate-800';
      formReg.classList.remove('hidden');
      formLogin.classList.add('hidden');
    }
    if (window.lucide) lucide.createIcons();
  },

  setAppSection(section) {
    const sections = ['dashboard', 'catalogo', 'historial'];
    sections.forEach(s => {
      const el = document.getElementById(`section-${s}`);
      const navBtn = document.getElementById(`nav-${s}`);
      if (s === section) {
        el.classList.remove('hidden');
        navBtn.className = 'w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all bg-brand-700 text-white shadow-sm';
      } else {
        el.classList.add('hidden');
        navBtn.className = 'w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all text-slate-300 hover:bg-slate-800 hover:text-white';
      }
    });

    const titles = {
      dashboard: 'Dashboard General',
      catalogo: 'Catálogo de Productos y Servicios',
      historial: 'Historial de Transacciones'
    };
    document.getElementById('section-title').textContent = titles[section] || 'GestoPago';

    if (section === 'catalogo' && this.productos.length === 0) {
      this.cargarCatalogo();
    }
    if (section === 'historial') {
      this.cargarHistorial();
    }

    if (window.lucide) lucide.createIcons();
  },

  // -------------------------------------------------------------
  // AUTENTICACIÓN
  // -------------------------------------------------------------
  async handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    const btn = document.getElementById('btn-login-submit');
    const alertBox = document.getElementById('auth-alert');

    btn.disabled = true;
    btn.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> <span>Iniciando...</span>`;
    alertBox.classList.add('hidden');

    try {
      const resp = await API.login(email, password);
      if (resp.ok && resp.data?.token) {
        API.setToken(resp.data.token);

        // Obtener datos del cliente
        const clienteResp = await API.buscarCliente(email);
        if (clienteResp.ok && clienteResp.data) {
          const cliente = clienteResp.data;
          API.setUsuario(cliente);
          this.usuario = cliente;

          // Obtener cuenta y saldo
          const cuentaResp = await API.buscarCuenta(cliente.id);
          if (cuentaResp.ok && cuentaResp.data) {
            API.setCuenta(cuentaResp.data);
            this.cuenta = cuentaResp.data;
          }
        }

        this.mostrarVistaApp();
        this.cargarDatosUsuario();
      } else {
        this.mostrarAlertaAuth('error', resp.mensaje || (resp.errores && resp.errores[0]) || 'Credenciales inválidas');
      }
    } catch (err) {
      this.mostrarAlertaAuth('error', err.message || 'Error de conexión');
    } finally {
      btn.disabled = false;
      btn.innerHTML = `<span>Ingresar al Portal</span> <i data-lucide="arrow-right" class="w-4 h-4"></i>`;
      if (window.lucide) lucide.createIcons();
    }
  },

  async handleRegistro(e) {
    e.preventDefault();
    const email = document.getElementById('reg-email').value.trim();
    const pass = document.getElementById('reg-password').value;
    const passConf = document.getElementById('reg-password-confirm').value;
    const btn = document.getElementById('btn-reg-submit');

    if (pass !== passConf) {
      this.mostrarAlertaAuth('error', 'Las contraseñas no coinciden');
      return;
    }

    btn.disabled = true;
    btn.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-slate-900 border-t-transparent rounded-full"></span> <span>Registrando...</span>`;

    try {
      const resp = await API.registrar(email, pass);
      if (resp.ok) {
        this.mostrarAlertaAuth('success', '¡Contraseña registrada exitosamente! Ya puedes iniciar sesión.');
        document.getElementById('form-registro').reset();
        setTimeout(() => this.setAuthTab('login'), 2000);
      } else {
        this.mostrarAlertaAuth('error', resp.mensaje || (resp.errores && resp.errores[0]) || 'Error al registrar credenciales');
      }
    } catch (err) {
      this.mostrarAlertaAuth('error', err.message || 'Error al conectar');
    } finally {
      btn.disabled = false;
      btn.innerHTML = `<span>Crear Credenciales de Acceso</span> <i data-lucide="user-plus" class="w-4 h-4"></i>`;
      if (window.lucide) lucide.createIcons();
    }
  },

  mostrarAlertaAuth(tipo, mensaje) {
    const alertBox = document.getElementById('auth-alert');
    alertBox.classList.remove('hidden');
    if (tipo === 'success') {
      alertBox.className = 'mb-5 p-3.5 rounded-xl text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-2.5';
      alertBox.innerHTML = `<i data-lucide="check-circle" class="w-4 h-4"></i> <span>${mensaje}</span>`;
    } else {
      alertBox.className = 'mb-5 p-3.5 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200 flex items-center gap-2.5';
      alertBox.innerHTML = `<i data-lucide="alert-triangle" class="w-4 h-4"></i> <span>${mensaje}</span>`;
    }
    if (window.lucide) lucide.createIcons();
  },

  handleLogout() {
    API.logout();
    this.usuario = null;
    this.cuenta = null;
    this.productos = [];
    this.historial = [];
    this.mostrarVistaAuth();
  },

  // -------------------------------------------------------------
  // CARGA Y DASHBOARD
  // -------------------------------------------------------------
  async cargarDatosUsuario() {
    if (!this.usuario) return;

    // Actualizar cuenta y saldo
    if (this.usuario.id) {
      const cuentaResp = await API.buscarCuenta(this.usuario.id);
      if (cuentaResp.ok && cuentaResp.data) {
        this.cuenta = cuentaResp.data;
        API.setCuenta(cuentaResp.data);
      }
    }

    this.actualizarUIUsuario();
    this.cargarCatalogo();
    if (this.cuenta?.numeroCuenta) {
      this.cargarHistorial();
    }
  },

  actualizarUIUsuario() {
    const nombre = this.usuario ? `${this.usuario.nombre} ${this.usuario.apellidoPaterno || ''}` : 'Usuario';
    const email = this.usuario?.correo || '';
    const inicial = nombre.trim().charAt(0).toUpperCase() || 'U';

    document.getElementById('user-name').textContent = nombre;
    document.getElementById('user-email').textContent = email;
    document.getElementById('user-avatar').textContent = inicial;
    document.getElementById('dash-titular').textContent = nombre;

    const numCuenta = this.cuenta?.numeroCuenta || 'No disponible';
    document.getElementById('header-cuenta-num').textContent = numCuenta;
    document.getElementById('dash-cuenta').textContent = numCuenta;

    const saldo = this.cuenta?.saldoActual !== undefined ? Number(this.cuenta.saldoActual) : 0;
    document.getElementById('dash-saldo').textContent = this.mostrarSaldo ? this.formatoMoneda(saldo) : '••••••';
    document.getElementById('pago-saldo-disponible').textContent = this.formatoMoneda(saldo);
  },

  toggleMostrarSaldo() {
    this.mostrarSaldo = !this.mostrarSaldo;
    const saldo = this.cuenta?.saldoActual !== undefined ? Number(this.cuenta.saldoActual) : 0;
    document.getElementById('dash-saldo').textContent = this.mostrarSaldo ? this.formatoMoneda(saldo) : '••••••';
    const icon = document.getElementById('dash-saldo-icon');
    icon.setAttribute('data-lucide', this.mostrarSaldo ? 'eye' : 'eye-off');
    if (window.lucide) lucide.createIcons();
  },

  formatoMoneda(monto) {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(monto || 0);
  },

  // -------------------------------------------------------------
  // CATÁLOGO DE SERVICIOS
  // -------------------------------------------------------------
  async cargarCatalogo() {
    const contador = document.getElementById('catalogo-contador');
    contador.textContent = 'Consultando servicios con GestoPago...';

    const resp = await API.obtenerProductos();
    if (resp.ok && resp.data) {
      // resp.data puede ser CatalogoAgrupadoDTO con productosPorTipoFront o el mapa directo
      const agrupados = resp.data.productosPorTipoFront || resp.data;
      const listaPlana = [];
      if (typeof agrupados === 'object' && agrupados !== null) {
        Object.keys(agrupados).forEach(tipoKey => {
          const prods = agrupados[tipoKey];
          if (Array.isArray(prods)) {
            listaPlana.push(...prods);
          }
        });
      }
      this.productos = listaPlana;
      contador.textContent = `${this.productos.length} productos y servicios disponibles`;
      this.renderCatalogo();
      this.renderServiciosPopularesDashboard();
    } else {
      contador.textContent = 'Error al cargar catálogo';
    }
  },

  filtrarCategoria(tipoFront) {
    this.categoriaActiva = tipoFront;
    document.querySelectorAll('.cat-pill').forEach(btn => {
      btn.className = 'cat-pill px-4 py-2 rounded-xl bg-slate-100 text-slate-700 hover:bg-slate-200';
    });
    event.target.className = 'cat-pill active px-4 py-2 rounded-xl bg-brand-700 text-white';
    this.renderCatalogo();
  },

  handleSearchCatalogo(query) {
    this.queryBusqueda = query.toLowerCase();
    this.renderCatalogo();
  },

  renderCatalogo() {
    const grid = document.getElementById('catalogo-grid');
    grid.innerHTML = '';

    let filtrados = this.productos;
    if (this.categoriaActiva !== null) {
      filtrados = filtrados.filter(p => Number(p.tipoFront) === Number(this.categoriaActiva));
    }
    if (this.queryBusqueda.trim()) {
      filtrados = filtrados.filter(p =>
        (p.servicio && p.servicio.toLowerCase().includes(this.queryBusqueda)) ||
        (p.producto && p.producto.toLowerCase().includes(this.queryBusqueda))
      );
    }

    if (filtrados.length === 0) {
      grid.innerHTML = `
        <div class="col-span-full py-16 text-center">
          <i data-lucide="inbox" class="w-12 h-12 text-slate-300 mx-auto mb-3"></i>
          <p class="text-sm font-bold text-slate-700">No se encontraron servicios</p>
          <p class="text-xs text-slate-400 mt-1">Intenta con otro término de búsqueda o categoría.</p>
        </div>
      `;
      if (window.lucide) lucide.createIcons();
      return;
    }

    filtrados.forEach(prod => {
      const card = document.createElement('div');
      card.className = 'bg-white p-6 rounded-3xl border border-purple-100 shadow-sm hover:shadow-xl hover:border-brand-500 hover:-translate-y-1 transition-all duration-200 flex flex-col justify-between text-center';
      const logoUrl = API.obtenerLogoUrl(prod.servicio, prod.idServicio);

      card.innerHTML = `
        <div class="flex flex-col items-center">
          <div class="w-16 h-16 logo-box mx-auto mb-3.5 shadow-sm border border-purple-100">
            <img src="${logoUrl}" alt="${prod.servicio}" class="p-1 object-contain" onerror="this.onerror=null; this.src='img/logos/cfe.png'">
          </div>
          <h4 class="font-extrabold text-base text-slate-900 leading-tight mb-1">${prod.servicio}</h4>
          <span class="text-xs text-slate-500 line-clamp-1 mb-3">${prod.producto}</span>
          <span class="inline-block text-[11px] font-semibold text-brand-700 bg-brand-50 px-3 py-1 rounded-full mb-4">
            ${prod.tipoFront === 1 ? 'Recarga Móvil (10 dígitos)' : 'Recibo / Referencia'}
          </span>
        </div>
        <button onclick="app.abrirModalPago(${prod.idServicio}, ${prod.idProducto}, '${encodeURIComponent(prod.servicio)}', '${encodeURIComponent(prod.producto)}', ${prod.tipoFront}, '${prod.precio}')" class="w-full py-3 px-4 bg-gradient-to-r from-brand-700 to-brand-600 hover:from-brand-800 hover:to-brand-700 text-white rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-2 shadow-md shadow-brand-700/20">
          <i data-lucide="credit-card" class="w-4 h-4"></i>
          <span>Pagar Servicio</span>
        </button>
      `;
      grid.appendChild(card);
    });

    if (window.lucide) lucide.createIcons();
  },

  renderServiciosPopularesDashboard() {
    const carousel = document.getElementById('dash-servicios-carousel') || document.getElementById('dash-servicios-grid');
    if (!carousel) return;
    carousel.innerHTML = '';

    const ordenDestacados = [166, 133, 115, 107, 102, 85, 76, 168, 178, 64, 55, 38, 154, 53, 164, 79];
    const vistos = new Set();
    const destacados = [];
    ordenDestacados.forEach(idDest => {
      const found = this.productos.find(p => Number(p.idServicio) === idDest);
      if (found && !vistos.has(found.idServicio)) {
        vistos.add(found.idServicio);
        destacados.push(found);
      }
    });
    this.productos.forEach(p => {
      if (destacados.length < 16 && !vistos.has(p.idServicio)) {
        vistos.add(p.idServicio);
        destacados.push(p);
      }
    });

    destacados.forEach(prod => {
      const bubble = document.createElement('div');
      bubble.className = 'story-avatar-wrapper flex-shrink-0';
      bubble.onclick = () => this.abrirModalPago(
        prod.idServicio,
        prod.idProducto,
        encodeURIComponent(prod.servicio),
        encodeURIComponent(prod.producto),
        prod.tipoFront,
        prod.precio
      );
      const logoUrl = API.obtenerLogoUrl(prod.servicio, prod.idServicio);

      bubble.innerHTML = `
        <div class="story-ring mb-2">
          <div class="story-avatar">
            <img src="${logoUrl}" alt="${prod.servicio}" onerror="this.onerror=null; this.src='img/logos/cfe.png'">
          </div>
        </div>
        <span class="text-xs font-bold text-slate-800 tracking-tight text-center max-w-[70px] truncate block">${prod.servicio}</span>
        <span class="text-[10px] font-semibold text-brand-700 bg-brand-50 px-2 py-0.5 rounded-full mt-1 block truncate">${prod.tipoFront === 1 ? 'Móvil' : 'Recibo'}</span>
      `;
      carousel.appendChild(bubble);
    });
    if (window.lucide) lucide.createIcons();
  },

  // -------------------------------------------------------------
  // FLUJO DE PAGO Y VALIDACIÓN DE REFERENCIAS
  // -------------------------------------------------------------
  abrirModalPagoDirecto() {
    if (this.productos.length > 0) {
      const p = this.productos[0];
      this.abrirModalPago(p.idServicio, p.idProducto, encodeURIComponent(p.servicio), encodeURIComponent(p.producto), p.tipoFront, p.precio);
    } else {
      this.setAppSection('catalogo');
    }
  },

  abrirModalPago(idServicio, idProducto, servicioEnc, productoEnc, tipoFront, precioSugerido) {
    const servicio = decodeURIComponent(servicioEnc);
    const producto = decodeURIComponent(productoEnc);

    document.getElementById('pago-id-servicio').value = idServicio;
    document.getElementById('pago-id-producto').value = idProducto;
    document.getElementById('pago-tipo-front').value = tipoFront;

    document.getElementById('pago-servicio-nombre').textContent = servicio;
    document.getElementById('pago-producto-nombre').textContent = producto;
    document.getElementById('pago-img-logo').src = API.obtenerLogoUrl(servicio, idServicio);

    const esTelefono = Number(tipoFront) === 1;
    document.getElementById('pago-label-destino').textContent = esTelefono
      ? '1. Número Telefónico (10 dígitos)'
      : '1. Referencia del Recibo o Factura';
    document.getElementById('pago-input-destino').placeholder = esTelefono ? 'Ej: 5512345678' : 'Ej: 018001234567';
    document.getElementById('pago-ayuda-destino').textContent = esTelefono
      ? 'Debe contener exactamente 10 dígitos numéricos.'
      : 'Ingresa la referencia numérica o alfanumérica de tu recibo.';

    document.getElementById('pago-input-destino').value = '';
    const inputMonto = document.getElementById('pago-input-monto');
    const precioNum = parseFloat(precioSugerido || '0');
    if (precioNum > 0) {
      inputMonto.value = precioNum.toFixed(2);
      inputMonto.readOnly = true;
      inputMonto.classList.add('bg-purple-50', 'text-brand-800', 'cursor-not-allowed');
      this.precioFijoActual = precioNum;
    } else {
      inputMonto.value = '';
      inputMonto.readOnly = false;
      inputMonto.classList.remove('bg-purple-50', 'text-brand-800', 'cursor-not-allowed');
      this.precioFijoActual = null;
    }

    this.resetValidacionReferencia();

    // Mostrar modal paso formulario
    document.getElementById('modal-step-form').classList.remove('hidden');
    document.getElementById('modal-step-2fa').classList.add('hidden');
    document.getElementById('modal-step-comprobante').classList.add('hidden');
    document.getElementById('modal-pago').classList.remove('hidden');

    if (window.lucide) lucide.createIcons();
  },

  resetValidacionReferencia() {
    this.referenciaValidada = false;
    this.datosValidacion = null;
    const banner = document.getElementById('banner-ref-estado');
    banner.classList.add('hidden');
    const btnProceder = document.getElementById('btn-proceder-pago');
    btnProceder.disabled = true;
    btnProceder.className = 'w-full mt-4 py-3.5 px-4 bg-slate-300 text-slate-500 cursor-not-allowed font-bold rounded-xl text-sm transition-all flex items-center justify-center gap-2';
    btnProceder.innerHTML = `<i data-lucide="shield" class="w-4 h-4"></i> <span>Valida la referencia para continuar</span>`;
    if (window.lucide) lucide.createIcons();
  },

  async validarReferenciaActual() {
    const destino = document.getElementById('pago-input-destino').value.trim();
    const idServicio = document.getElementById('pago-id-servicio').value;
    const idProducto = document.getElementById('pago-id-producto').value;
    const tipoFront = Number(document.getElementById('pago-tipo-front').value);
    const esTelefono = tipoFront === 1;
    const banner = document.getElementById('banner-ref-estado');
    const btnValidar = document.getElementById('btn-validar-ref');

    if (!destino) {
      banner.className = 'p-3.5 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200 flex items-center gap-2';
      banner.innerHTML = `<i data-lucide="alert-circle" class="w-4 h-4"></i> <span>Por favor ingresa la referencia antes de validar.</span>`;
      banner.classList.remove('hidden');
      if (window.lucide) lucide.createIcons();
      return;
    }

    if (esTelefono && destino.length !== 10) {
      banner.className = 'p-3.5 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200 flex items-center gap-2';
      banner.innerHTML = `<i data-lucide="alert-circle" class="w-4 h-4"></i> <span>El número telefónico debe contener exactamente 10 dígitos.</span>`;
      banner.classList.remove('hidden');
      if (window.lucide) lucide.createIcons();
      return;
    }

    btnValidar.disabled = true;
    btnValidar.innerHTML = `<span class="animate-spin inline-block w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full"></span>`;

    try {
      const resp = await API.validarReferencia(idServicio, idProducto, destino, esTelefono ? 'TELEFONO' : 'TRANSFERENCIA');
      btnValidar.disabled = false;
      btnValidar.innerHTML = `<i data-lucide="search" class="w-4 h-4"></i> <span>Validar</span>`;

      if (resp.ok && resp.data?.valida) {
        this.referenciaValidada = true;
        this.datosValidacion = resp.data;

        banner.className = 'p-3.5 rounded-xl text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-start gap-2.5';
        banner.innerHTML = `
          <i data-lucide="check-circle" class="w-4 h-4 mt-0.5 text-emerald-600 flex-shrink-0"></i>
          <div>
            <span class="block font-bold">Referencia Verificada Exitosamente</span>
            <span class="font-normal text-[11px] text-emerald-800">${resp.data.mensaje}</span>
            ${resp.data.adeudoSugerido ? `<span class="block mt-1 font-bold">Adeudo sugerido: ${this.formatoMoneda(resp.data.adeudoSugerido)}</span>` : ''}
          </div>
        `;
        banner.classList.remove('hidden');

        // Autocompletar adeudo si el monto estaba en blanco
        const inputMonto = document.getElementById('pago-input-monto');
        if (!inputMonto.value && resp.data.adeudoSugerido) {
          inputMonto.value = resp.data.adeudoSugerido;
        }

        // Habilitar botón de continuar a 2FA
        const btnProceder = document.getElementById('btn-proceder-pago');
        btnProceder.disabled = false;
        btnProceder.className = 'w-full mt-4 py-3.5 px-4 btn-glow text-white font-bold rounded-xl text-sm flex items-center justify-center gap-2';
        btnProceder.innerHTML = `<i data-lucide="shield-check" class="w-4 h-4"></i> <span>Proceder al Pago con Código 2FA</span>`;
      } else {
        this.resetValidacionReferencia();
        banner.className = 'p-3.5 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200 flex items-center gap-2';
        banner.innerHTML = `<i data-lucide="alert-octagon" class="w-4 h-4 flex-shrink-0"></i> <span>${resp.data?.mensaje || resp.mensaje || 'Referencia rechazada por el proveedor.'}</span>`;
        banner.classList.remove('hidden');
      }
    } catch (err) {
      btnValidar.disabled = false;
      btnValidar.innerHTML = `<i data-lucide="search" class="w-4 h-4"></i> <span>Validar</span>`;
      this.resetValidacionReferencia();
      banner.className = 'p-3.5 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200 flex items-center gap-2';
      banner.innerHTML = `<i data-lucide="wifi-off" class="w-4 h-4 flex-shrink-0"></i> <span>${err.message || 'Error de conexión con el servicio de validación.'}</span>`;
      banner.classList.remove('hidden');
    }

    if (window.lucide) lucide.createIcons();
  },

  async handleIniciarPago(e) {
    e.preventDefault();
    if (!this.referenciaValidada) {
      alert('Primero debes validar la referencia');
      return;
    }

    const destino = document.getElementById('pago-input-destino').value.trim();
    const monto = parseFloat(document.getElementById('pago-input-monto').value);
    const idServicio = Number(document.getElementById('pago-id-servicio').value);
    const idProducto = Number(document.getElementById('pago-id-producto').value);
    const tipoFront = Number(document.getElementById('pago-tipo-front').value);
    const esTelefono = tipoFront === 1;

    const saldoActual = Number(this.cuenta?.saldoActual || 0);
    if (this.precioFijoActual && Math.abs(monto - this.precioFijoActual) > 0.01) {
      alert(`Este paquete tiene un precio fijo de ${this.formatoMoneda(this.precioFijoActual)}. No puede modificarse.`);
      return;
    }
    if (monto > saldoActual) {
      alert(`Saldo insuficiente (${this.formatoMoneda(saldoActual)}) para cubrir este pago de ${this.formatoMoneda(monto)}`);
      return;
    }

    const btnProceder = document.getElementById('btn-proceder-pago');
    btnProceder.disabled = true;
    btnProceder.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> <span>Enviando código 2FA...</span>`;

    try {
      const resp2fa = await API.solicitar2fa(this.cuenta.numeroCuenta);
      if (resp2fa.ok && resp2fa.data) {
        document.getElementById('correo-enmascarado-2fa').textContent = resp2fa.data.correoEnmascarado || 'tu correo registrado';
        document.getElementById('2fa-resumen-servicio').textContent = document.getElementById('pago-servicio-nombre').textContent;
        document.getElementById('2fa-resumen-ref').textContent = destino;
        document.getElementById('2fa-resumen-monto').textContent = this.formatoMoneda(monto);

        const bannerRespaldo = document.getElementById('banner-codigo-respaldo-2fa');
        const valorRespaldo = document.getElementById('valor-codigo-respaldo-2fa');
        this.codigoRespaldo2fa = resp2fa.data.codigoParaPruebas || null;
        if (this.codigoRespaldo2fa && bannerRespaldo && valorRespaldo) {
          valorRespaldo.textContent = this.codigoRespaldo2fa;
          bannerRespaldo.classList.remove('hidden');
        } else if (bannerRespaldo) {
          bannerRespaldo.classList.add('hidden');
        }

        this.pagoPendiente = {
          numeroCuenta: this.cuenta.numeroCuenta,
          idServicio,
          idProducto,
          monto,
          tipoDestino: esTelefono ? 'TELEFONO' : 'TRANSFERENCIA',
          telefono: esTelefono ? destino : null,
          referencia: !esTelefono ? destino : null,
          servicioNombre: document.getElementById('pago-servicio-nombre').textContent,
          productoNombre: document.getElementById('pago-producto-nombre').textContent
        };

        // Cambiar a paso 2FA
        document.getElementById('modal-step-form').classList.add('hidden');
        document.getElementById('modal-step-2fa').classList.remove('hidden');
        document.getElementById('input-codigo-2fa').value = '';
        document.getElementById('alerta-error-2fa').classList.add('hidden');
      } else {
        alert(resp2fa.mensaje || 'Error al enviar código 2FA');
      }
    } catch (err) {
      alert(err.message || 'Error al enviar código 2FA');
    } finally {
      btnProceder.disabled = false;
      btnProceder.innerHTML = `<i data-lucide="shield-check" class="w-4 h-4"></i> <span>Proceder al Pago con Código 2FA</span>`;
      if (window.lucide) lucide.createIcons();
    }
  },

  usarCodigoRespaldo2fa() {
    if (this.codigoRespaldo2fa) {
      document.getElementById('input-codigo-2fa').value = this.codigoRespaldo2fa;
    }
  },

  volverPasoFormulario() {
    document.getElementById('modal-step-2fa').classList.add('hidden');
    document.getElementById('modal-step-form').classList.remove('hidden');
    if (window.lucide) lucide.createIcons();
  },

  async confirmarPagoFinal() {
    const codigo = document.getElementById('input-codigo-2fa').value.trim();
    const alerta = document.getElementById('alerta-error-2fa');
    const btnConfirmar = document.getElementById('btn-confirmar-2fa');

    if (!codigo || codigo.length < 6) {
      alerta.textContent = 'Ingresa el código de 6 dígitos enviado a tu correo.';
      alerta.classList.remove('hidden');
      return;
    }

    btnConfirmar.disabled = true;
    btnConfirmar.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> <span>Procesando...</span>`;
    alerta.classList.add('hidden');

    try {
      const datosEnvio = {
        numeroCuenta: this.pagoPendiente.numeroCuenta,
        idServicio: Number(this.pagoPendiente.idServicio),
        idProducto: Number(this.pagoPendiente.idProducto),
        monto: Number(this.pagoPendiente.monto),
        tipoDestino: this.pagoPendiente.tipoDestino,
        codigo2fa: codigo
      };
      if (this.pagoPendiente.tipoDestino === 'TELEFONO') {
        datosEnvio.telefono = this.pagoPendiente.telefono;
      } else {
        datosEnvio.referencia = this.pagoPendiente.referencia;
      }

      const resp = await API.procesarPago(datosEnvio);
      if (resp.ok && resp.data) {
        const p = resp.data;

        // Actualizar saldo de la cuenta local
        if (this.cuenta) {
          this.cuenta.saldoActual = p.saldoRestante;
          API.setCuenta(this.cuenta);
          this.actualizarUIUsuario();
        }

        // Mostrar Comprobante Digital con Folio UPC
        document.getElementById('recibo-upc').textContent = `UPC-${p.idTx?.slice(-6) || 'OK'}`;
        document.getElementById('recibo-id-tx').textContent = p.idTx || '-';
        document.getElementById('recibo-auth').textContent = p.numAutorizacion || '-';
        document.getElementById('recibo-servicio').textContent = this.pagoPendiente.servicioNombre;
        document.getElementById('recibo-destino').textContent = p.destino || '-';
        document.getElementById('recibo-fecha').textContent = `${p.fecha || ''} ${p.hora || ''}`;
        document.getElementById('recibo-monto').textContent = this.formatoMoneda(p.monto);
        document.getElementById('recibo-nuevo-saldo').textContent = this.formatoMoneda(p.saldoRestante);

        document.getElementById('modal-step-2fa').classList.add('hidden');
        document.getElementById('modal-step-comprobante').classList.remove('hidden');
      } else {
        alerta.textContent = resp.mensaje || (resp.errores && resp.errores[0]) || 'Código 2FA incorrecto o pago rechazado';
        alerta.classList.remove('hidden');
      }
    } catch (err) {
      alerta.textContent = err.message || 'Error al procesar el pago';
      alerta.classList.remove('hidden');
    } finally {
      btnConfirmar.disabled = false;
      btnConfirmar.innerHTML = `<span>Autorizar Pago</span>`;
      if (window.lucide) lucide.createIcons();
    }
  },

  cerrarModalPago() {
    document.getElementById('modal-pago').classList.add('hidden');
  },

  cerrarModalPagoYRefrescar() {
    this.cerrarModalPago();
    this.cargarDatosUsuario();
    this.cargarHistorial();
  },

  // -------------------------------------------------------------
  // HISTORIAL DE TRANSACCIONES
  // -------------------------------------------------------------
  async cargarHistorial() {
    if (!this.cuenta?.numeroCuenta) return;

    const tbody = document.getElementById('historial-tbody');
    const dashCont = document.getElementById('dash-movimientos-container');
    tbody.innerHTML = `<tr><td colspan="7" class="py-8 text-center text-slate-400">Consultando historial en GestoPago...</td></tr>`;

    const resp = await API.obtenerHistorial(this.cuenta.numeroCuenta);
    if (resp.ok && Array.isArray(resp.data)) {
      this.historial = resp.data;
      this.renderHistorial();
      this.renderUltimosMovimientosDashboard();
    } else {
      tbody.innerHTML = `<tr><td colspan="7" class="py-8 text-center text-red-500 font-semibold">Error al cargar historial: ${resp.mensaje}</td></tr>`;
      dashCont.innerHTML = `<div class="p-6 text-center text-xs text-slate-400">No se pudieron cargar los movimientos recientes.</div>`;
    }
  },

  renderHistorial() {
    const tbody = document.getElementById('historial-tbody');
    tbody.innerHTML = '';

    if (this.historial.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" class="py-12 text-center text-slate-400">
            <i data-lucide="receipt" class="w-8 h-8 text-slate-300 mx-auto mb-2"></i>
            <span class="block font-bold text-slate-600">Aún no tienes pagos de servicios registrados</span>
            <span class="text-[11px]">Realiza tu primer pago desde el catálogo.</span>
          </td>
        </tr>
      `;
      if (window.lucide) lucide.createIcons();
      return;
    }

    this.historial.forEach((mov, idx) => {
      const tr = document.createElement('tr');
      tr.className = 'hover:bg-slate-50 transition-colors cursor-pointer';
      tr.onclick = () => this.abrirModalDetalle(idx);
      const logoUrl = mov.urlLogo || API.obtenerLogoUrl(mov.servicio);

      tr.innerHTML = `
        <td class="py-3.5 px-6 font-bold text-slate-900 flex items-center gap-3">
          <div class="w-8 h-8 logo-box flex-shrink-0">
            <img src="${logoUrl}" alt="${mov.servicio}">
          </div>
          <div>
            <span class="block font-bold">${mov.servicio}</span>
            <span class="text-[10px] text-slate-400 font-normal line-clamp-1">${mov.producto}</span>
          </div>
        </td>
        <td class="py-3.5 px-4 font-mono font-bold text-brand-700">${mov.folio}</td>
        <td class="py-3.5 px-4 text-slate-600 font-medium">${mov.referencia || 'N/D'}</td>
        <td class="py-3.5 px-4 text-slate-500">${mov.fecha} <span class="text-[10px] text-slate-400 block">${mov.hora}</span></td>
        <td class="py-3.5 px-4 text-right font-extrabold text-slate-900">-${this.formatoMoneda(mov.monto)}</td>
        <td class="py-3.5 px-4 text-center">
          <span class="px-2.5 py-1 rounded-full text-[10px] font-bold text-emerald-700 bg-emerald-100">${mov.estado}</span>
        </td>
        <td class="py-3.5 px-6 text-center">
          <button class="text-brand-700 hover:text-brand-900 font-bold text-xs underline">Detalle</button>
        </td>
      `;
      tbody.appendChild(tr);
    });

    if (window.lucide) lucide.createIcons();
  },

  renderUltimosMovimientosDashboard() {
    const cont = document.getElementById('dash-movimientos-container');
    cont.innerHTML = '';
    const ultimos = this.historial.slice(0, 5);

    if (ultimos.length === 0) {
      cont.innerHTML = `
        <div class="p-8 text-center text-slate-400 text-xs">
          <p class="font-bold text-slate-600">Sin movimientos recientes</p>
          <p class="text-[11px] mt-0.5">Tus pagos de CFE, recargas y servicios aparecerán aquí.</p>
        </div>
      `;
      return;
    }

    ultimos.forEach((mov, idx) => {
      const item = document.createElement('div');
      item.className = 'p-4 hover:bg-slate-50 transition-colors flex items-center justify-between cursor-pointer';
      item.onclick = () => this.abrirModalDetalle(idx);
      const logoUrl = mov.urlLogo || API.obtenerLogoUrl(mov.servicio);

      item.innerHTML = `
        <div class="flex items-center gap-3.5">
          <div class="w-10 h-10 logo-box flex-shrink-0">
            <img src="${logoUrl}" alt="${mov.servicio}">
          </div>
          <div>
            <h5 class="text-xs font-bold text-slate-900">${mov.servicio}</h5>
            <span class="text-[11px] text-slate-500">${mov.fecha} • Folio: ${mov.folio}</span>
          </div>
        </div>
        <div class="text-right">
          <span class="text-xs font-extrabold text-slate-900 block">-${this.formatoMoneda(mov.monto)}</span>
          <span class="text-[10px] font-bold text-emerald-600 uppercase">Exitosa</span>
        </div>
      `;
      cont.appendChild(item);
    });
  },

  abrirModalDetalle(index) {
    const mov = this.historial[index];
    if (!mov) return;

    document.getElementById('detalle-servicio').textContent = mov.servicio;
    document.getElementById('detalle-img-logo').src = mov.urlLogo || API.obtenerLogoUrl(mov.servicio);
    document.getElementById('detalle-folio').textContent = mov.folio;
    document.getElementById('detalle-referencia').textContent = mov.referencia || 'N/D';
    document.getElementById('detalle-concepto').textContent = mov.concepto || mov.producto;
    document.getElementById('detalle-fecha').textContent = `${mov.fecha} • ${mov.hora}`;
    document.getElementById('detalle-cuenta').textContent = mov.numeroCuenta || this.cuenta?.numeroCuenta || '-';
    document.getElementById('detalle-monto').textContent = `-${this.formatoMoneda(mov.monto)}`;
    document.getElementById('detalle-saldo-resultante').textContent = this.formatoMoneda(mov.saldoResultante);

    document.getElementById('modal-detalle-historial').classList.remove('hidden');
    if (window.lucide) lucide.createIcons();
  },

  cerrarModalDetalle() {
    document.getElementById('modal-detalle-historial').classList.add('hidden');
  }
};

// Arrancar aplicación al cargar el DOM
document.addEventListener('DOMContentLoaded', () => {
  app.init();
});

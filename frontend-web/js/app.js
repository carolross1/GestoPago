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
  ultimoCpConsultado: '',
  correoDisponibleWeb: null,
  timerCorreoWeb: null,
  ultimoCorreoVerificadoWeb: '',
  codigo2faSolicitadoWeb: false,
  adminCuentas: [],
  adminFiltro: '',
  ultimoPrefijoCurpWeb: '',
  ultimoPrefijoRfcWeb: '',
  tabuladoresSueldo: [
    { id: 'TAB_1', etiqueta: 'De $0 a $10,000 MXN', minimo: 0.01, maximo: 10000 },
    { id: 'TAB_2', etiqueta: 'De $10,001 a $25,000 MXN', minimo: 10001, maximo: 25000 },
    { id: 'TAB_3', etiqueta: 'De $25,001 a $50,000 MXN', minimo: 25001, maximo: 50000 },
    { id: 'TAB_4', etiqueta: 'De $50,001 a $100,000 MXN', minimo: 50001, maximo: 100000 },
    { id: 'TAB_5', etiqueta: 'De $100,001 a $250,000 MXN', minimo: 100001, maximo: 250000 }
  ],
  catalogoProfesiones: [
    { nombre: 'Estudiante', categoria: 'Educación', tabuladorSugerido: 'TAB_1' },
    { nombre: 'Trabajo del Hogar', categoria: 'Servicios', tabuladorSugerido: 'TAB_1' },
    { nombre: 'Jubilado o Pensionado', categoria: 'Pensiones', tabuladorSugerido: 'TAB_1' },
    { nombre: 'Auxiliar Administrativo', categoria: 'Administración', tabuladorSugerido: 'TAB_2' },
    { nombre: 'Comerciante', categoria: 'Comercio', tabuladorSugerido: 'TAB_2' },
    { nombre: 'Docente o Profesor', categoria: 'Educación', tabuladorSugerido: 'TAB_2' },
    { nombre: 'Enfermero', categoria: 'Salud', tabuladorSugerido: 'TAB_2' },
    { nombre: 'Disenador Grafico o Digital', categoria: 'Creatividad', tabuladorSugerido: 'TAB_2' },
    { nombre: 'Contador Publico', categoria: 'Finanzas', tabuladorSugerido: 'TAB_3' },
    { nombre: 'Abogado', categoria: 'Derecho', tabuladorSugerido: 'TAB_3' },
    { nombre: 'Arquitecto', categoria: 'Construcción', tabuladorSugerido: 'TAB_3' },
    { nombre: 'Ingeniero en Sistemas', categoria: 'Tecnología', tabuladorSugerido: 'TAB_3' },
    { nombre: 'Desarrollador de Software', categoria: 'Tecnología', tabuladorSugerido: 'TAB_3' },
    { nombre: 'Medico General o Especialista', categoria: 'Salud', tabuladorSugerido: 'TAB_4' },
    { nombre: 'Empresario', categoria: 'Dirección', tabuladorSugerido: 'TAB_4' },
    { nombre: 'Director Ejecutivo', categoria: 'Dirección', tabuladorSugerido: 'TAB_5' }
  ],

  init() {
    this.usuario = API.getUsuario();
    this.cuenta = API.getCuenta();
    this.sessionMonitorTimer = null;

    API.onSessionExpired = (mensaje) => this.manejarSesionCerradaRemota(mensaje);

    document.addEventListener('visibilitychange', () => {
      if (!document.hidden && API.getToken()) {
        API.verificarSesion();
      }
    });
    window.addEventListener('focus', () => {
      if (API.getToken()) {
        API.verificarSesion();
      }
    });

    this.poblarCatalogoProfesionesUI();
    this.cargarCatalogoProfesionesRemoto();
    this.configurarValidacionesEnVivo();

    const token = API.getToken();
    if (token && this.usuario) {
      this.mostrarVistaApp();
      this.iniciarMonitorSesion();
      this.cargarDatosUsuario();
    } else {
      this.mostrarVistaAuth();
    }

    if (window.lucide) {
      lucide.createIcons();
    }
  },

  iniciarMonitorSesion() {
    this.detenerMonitorSesion();
    this.sessionMonitorTimer = setInterval(() => {
      if (API.getToken()) {
        API.verificarSesion();
      } else {
        this.detenerMonitorSesion();
      }
    }, 12000);
  },

  detenerMonitorSesion() {
    if (this.sessionMonitorTimer) {
      clearInterval(this.sessionMonitorTimer);
      this.sessionMonitorTimer = null;
    }
  },

  manejarSesionCerradaRemota(mensaje) {
    this.detenerMonitorSesion();
    this.usuario = null;
    this.cuenta = null;
    this.productos = [];
    this.historial = [];
    const modalPago = document.getElementById('modal-pago');
    const modalDetalle = document.getElementById('modal-detalle-historial');
    if (modalPago) modalPago.classList.add('hidden');
    if (modalDetalle) modalDetalle.classList.add('hidden');
    this.mostrarVistaAuth();
    this.setAuthTab('login');
    this.mostrarAlertaAuth(
      'error',
      mensaje || 'Tu sesión fue cerrada porque se inició sesión en otro dispositivo.'
    );
  },

  getTabuladorActual() {
    const sel = document.getElementById('new-tabulador');
    const id = sel ? sel.value : 'TAB_1';
    return this.tabuladoresSueldo.find(t => t.id === id) || this.tabuladoresSueldo[0];
  },

  poblarCatalogoProfesionesUI() {
    const selProf = document.getElementById('new-ocupacion');
    const selTab = document.getElementById('new-tabulador');
    if (selProf) {
      const actual = selProf.value;
      selProf.innerHTML = this.catalogoProfesiones
        .map(p => `<option value="${p.nombre}">${p.nombre} (${p.categoria})</option>`)
        .join('');
      if (actual && this.catalogoProfesiones.some(p => p.nombre === actual)) {
        selProf.value = actual;
      }
    }
    if (selTab) {
      const actualTab = selTab.value;
      selTab.innerHTML = this.tabuladoresSueldo
        .map(t => `<option value="${t.id}">${t.etiqueta}</option>`)
        .join('');
      if (actualTab && this.tabuladoresSueldo.some(t => t.id === actualTab)) {
        selTab.value = actualTab;
      }
    }
    this.actualizarHintTabulador();
  },

  async cargarCatalogoProfesionesRemoto() {
    const resp = await API.obtenerProfesiones();
    if (resp.ok && resp.data) {
      if (Array.isArray(resp.data.tabuladores) && resp.data.tabuladores.length) {
        this.tabuladoresSueldo = resp.data.tabuladores.map(t => ({
          id: t.id,
          etiqueta: t.etiqueta,
          minimo: Number(t.minimo),
          maximo: Number(t.maximo)
        }));
      }
      if (Array.isArray(resp.data.profesiones) && resp.data.profesiones.length) {
        this.catalogoProfesiones = resp.data.profesiones;
      }
      this.poblarCatalogoProfesionesUI();
    }
  },

  handleProfesionChange() {
    const selProf = document.getElementById('new-ocupacion');
    const selTab = document.getElementById('new-tabulador');
    if (!selProf || !selTab) return;
    const prof = this.catalogoProfesiones.find(p => p.nombre === selProf.value);
    if (prof && prof.tabuladorSugerido && this.tabuladoresSueldo.some(t => t.id === prof.tabuladorSugerido)) {
      selTab.value = prof.tabuladorSugerido;
    }
    this.handleTabuladorChange();
  },

  handleTabuladorChange() {
    this.actualizarHintTabulador();
    const ingEl = document.getElementById('new-ingreso');
    if (ingEl && ingEl.value) {
      const tab = this.getTabuladorActual();
      if (Number(ingEl.value) > tab.maximo) {
        ingEl.value = '';
      }
      if (ingEl._liveValidate) ingEl._liveValidate();
    }
  },

  actualizarHintTabulador() {
    const tab = this.getTabuladorActual();
    const hint = document.getElementById('tabulador-rango-hint');
    const ingEl = document.getElementById('new-ingreso');
    if (hint && tab) {
      hint.textContent = `Rango permitido: $${tab.minimo.toLocaleString('es-MX')} a $${tab.maximo.toLocaleString('es-MX')} MXN (no puedes exceder el límite)`;
    }
    if (ingEl && tab) {
      ingEl.placeholder = `Entre $${Math.floor(tab.minimo)} y $${tab.maximo}`;
    }
  },

  handleColoniaSelectChange() {
    const sel = document.getElementById('new-colonia-select');
    const input = document.getElementById('new-colonia');
    if (!sel || !input) return;
    if (sel.value === '__OTRA__') {
      input.classList.remove('hidden');
      input.value = '';
      input.focus();
    } else {
      input.value = sel.value;
      input.classList.add('hidden');
      if (input._liveValidate) input._liveValidate();
    }
  },

  async consultarCpWeb(cp) {
    const limpio = (cp || '').trim();
    const msgEl = document.getElementById('cp-status-msg');
    if (limpio.length !== 5 || !/^\d{5}$/.test(limpio)) {
      if (msgEl) msgEl.classList.add('hidden');
      this.ultimoCpConsultado = '';
      return;
    }
    if (limpio === this.ultimoCpConsultado) return;
    this.ultimoCpConsultado = limpio;

    if (msgEl) {
      msgEl.className = 'text-[11px] font-semibold mt-1 text-brand-700';
      msgEl.textContent = 'Buscando dirección por código postal...';
      msgEl.classList.remove('hidden');
    }

    const resp = await API.consultarCodigoPostal(limpio);
    const estEl = document.getElementById('new-estado');
    const munEl = document.getElementById('new-municipio');
    const paisEl = document.getElementById('new-pais');
    const colSel = document.getElementById('new-colonia-select');
    const colInput = document.getElementById('new-colonia');

    if (resp.ok && resp.data) {
      const { estado, municipio, pais, colonias } = resp.data;
      if (estEl) { estEl.value = estado || ''; if (estEl._liveValidate) estEl._liveValidate(); }
      if (munEl) { munEl.value = municipio || estado || ''; if (munEl._liveValidate) munEl._liveValidate(); }
      if (paisEl) { paisEl.value = pais || 'Mexico'; if (paisEl._liveValidate) paisEl._liveValidate(); }

      if (Array.isArray(colonias) && colonias.length > 0 && colSel && colInput) {
        colSel.innerHTML = colonias.map(c => `<option value="${c}">${c}</option>`).join('')
          + `<option value="__OTRA__">Otra colonia (escribir manualmente)...</option>`;
        colSel.classList.remove('hidden');
        colSel.value = colonias[0];
        colInput.value = colonias[0];
        colInput.classList.add('hidden');
        if (colInput._liveValidate) colInput._liveValidate();
      } else if (colSel && colInput) {
        colSel.classList.add('hidden');
        colInput.classList.remove('hidden');
      }

      if (msgEl) {
        msgEl.className = 'text-[11px] font-semibold mt-1 text-emerald-600';
        msgEl.textContent = `✓ Dirección autocompletada (${(colonias || []).length} colonias en ${estado || 'México'}).`;
      }
    } else {
      if (colSel && colInput) {
        colSel.classList.add('hidden');
        colInput.classList.remove('hidden');
      }
      if (msgEl) {
        msgEl.className = 'text-[11px] font-semibold mt-1 text-amber-600';
        msgEl.textContent = 'CP no encontrado automáticamente; puedes escribir los datos manualmente.';
      }
    }
  },

  configurarValidacionesEnVivo() {
    if (typeof Validators === 'undefined') return;

    // Login
    Validators.bind('login-email', v => Validators.correo(v));
    Validators.bind('login-password', v => Validators.password(v));

    // Activar Portal
    Validators.bind('reg-email', v => Validators.correo(v));
    Validators.bind('reg-password', v => {
      const conf = document.getElementById('reg-password-confirm');
      if (conf && conf.value && conf._liveValidate) conf._liveValidate();
      return Validators.password(v);
    });
    Validators.bind('reg-password-confirm', v =>
      Validators.confirmarPassword(v, document.getElementById('reg-password')?.value || '')
    );

    // Crear Cuenta
    Validators.bind('new-nombre', v => Validators.primerNombre(v), {
      transform: v => v.replace(/\s+/g, '')
    });
    Validators.bind('new-segundo-nombre', v => Validators.segundoNombre(v), {
      opcional: true,
      transform: v => v.replace(/\s+/g, '')
    });
    Validators.bind('new-ap-paterno', v => Validators.apellido(v, 'tu apellido paterno'));
    Validators.bind('new-ap-materno', v => Validators.apellido(v, 'tu apellido materno'));
    Validators.bind('new-fecha-nac', v => Validators.fechaNacimiento(v));

    // Recalcular prefijos de CURP y RFC en vivo al cambiar datos personales
    ['new-nombre', 'new-segundo-nombre', 'new-ap-paterno', 'new-ap-materno', 'new-fecha-nac', 'new-sexo'].forEach(id => {
      const el = document.getElementById(id);
      if (el) {
        el.addEventListener('input', () => this.recalcularCurpYRfcWeb());
        el.addEventListener('change', () => this.recalcularCurpYRfcWeb());
      }
    });
    Validators.bind('new-curp', v => Validators.curp(v), {
      transform: v => v.replace(/[^A-Za-z0-9]/g, '').toUpperCase().slice(0, 18)
    });
    Validators.bind('new-rfc', v => Validators.rfc(v), {
      transform: v => v.replace(/[^A-Za-zÑñ&0-9]/g, '').toUpperCase().slice(0, 13)
    });
    Validators.bind('new-cp', v => Validators.codigoPostal(v), {
      transform: v => v.replace(/\D/g, '').slice(0, 5),
      onValid: v => this.consultarCpWeb(v)
    });
    Validators.bind('new-colonia', v => Validators.textoDir(v, 'tu colonia'));
    Validators.bind('new-municipio', v => Validators.soloLetras(v, 'tu municipio'));
    Validators.bind('new-estado', v => Validators.soloLetras(v, 'tu estado'));
    Validators.bind('new-calle', v => Validators.textoDir(v, 'tu calle'));
    Validators.bind('new-num-ext', v => Validators.numeroDomicilio(v), {
      transform: v => v.replace(/\D/g, '').slice(0, 6)
    });
    Validators.bind('new-pais', v => Validators.soloLetras(v, 'tu país'));
    Validators.bind('new-empresa', v => Validators.soloLetras(v, 'tu empresa o institución'));
    Validators.bind('new-ingreso', v => {
      const tab = this.getTabuladorActual();
      return Validators.ingresoEnRango(v, tab.minimo, tab.maximo);
    }, {
      transform: v => {
        const limpio = v.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');
        const partes = limpio.split('.');
        const acotado = partes.length > 1 ? `${partes[0]}.${partes[1].slice(0, 2)}` : limpio;
        const tab = this.getTabuladorActual();
        if (acotado && Number(acotado) > tab.maximo) {
          return String(tab.maximo);
        }
        return acotado;
      }
    });
    Validators.bind('new-email', v => {
      const errFormato = Validators.correo(v);
      const limpio = (v || '').trim().toLowerCase();
      if (errFormato) {
        this.limpiarEstadoCorreoWeb();
        return errFormato;
      }
      if (limpio !== this.ultimoCorreoVerificadoWeb) {
        this.programarVerificacionCorreoWeb(limpio);
      } else if (this.correoDisponibleWeb === false) {
        return 'Este correo ya está registrado. Usa otro correo o inicia sesión.';
      }
      return null;
    });
    Validators.bind('new-telefono', v => Validators.telefonoMexico(v), {
      transform: v => v.replace(/\D/g, '').slice(0, 10)
    });
    Validators.bind('new-password', v => {
      const conf = document.getElementById('new-password-confirm');
      if (conf && conf.value && conf._liveValidate) conf._liveValidate();
      return Validators.password(v);
    });
    Validators.bind('new-password-confirm', v =>
      Validators.confirmarPassword(v, document.getElementById('new-password')?.value || '')
    );

    // Módulo Admin (Abonos)
    Validators.bind('admin-cuenta', v => {
      const s = (v || '').trim();
      if (!s) return 'Ingresa el número de cuenta';
      if (!/^\d+$/.test(s)) return 'Solo dígitos numéricos';
      if (s.length < 10) return `Faltan ${10 - s.length} dígitos (${s.length}/10)`;
      return null;
    }, {
      transform: v => v.replace(/\D/g, '').slice(0, 10)
    });
    Validators.bind('admin-monto', v => {
      const s = (v || '').trim();
      if (!s) return 'Ingresa el monto a abonar';
      const n = Number(s);
      if (Number.isNaN(n) || n <= 0) return 'El monto debe ser mayor a $0.00';
      if (n > 500000) return 'El abono máximo por operación es de $500,000 MXN';
      return null;
    }, {
      transform: v => {
        const limpio = v.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');
        const partes = limpio.split('.');
        return partes.length > 1 ? `${partes[0]}.${partes[1].slice(0, 2)}` : limpio;
      }
    });

    // Pago de servicios y 2FA
    Validators.bind('pago-input-destino', v => {
      const tipoFront = Number(document.getElementById('pago-tipo-front')?.value || 1);
      if (tipoFront === 1) return Validators.telefonoMexico(v);
      const s = (v || '').trim();
      if (!s) return 'Ingresa la referencia del recibo';
      if (s.length < 5) return `Mínimo 5 caracteres (llevas ${s.length})`;
      if (/^(.)\1{4,}$/.test(s)) return 'No puede ser un solo carácter repetido';
      return null;
    });
    Validators.bind('pago-input-monto', v => {
      const n = Number(v);
      const saldo = Number(this.cuenta?.saldoActual || 0);
      if (!v || Number.isNaN(n) || n <= 0) return 'Ingresa un monto mayor a $0.00';
      if (this.precioFijoActual && Math.abs(n - this.precioFijoActual) > 0.01) {
        return `Este paquete tiene precio fijo de ${this.formatoMoneda(this.precioFijoActual)}`;
      }
      if (n > saldo) return `Excede tu saldo disponible (${this.formatoMoneda(saldo)})`;
      return null;
    });
    Validators.bind('input-codigo-2fa', v => {
      const s = (v || '').trim();
      if (!s) return 'Ingresa el código de 6 dígitos';
      if (!/^\d+$/.test(s)) return 'Solo dígitos numéricos';
      if (s.length < 6) return `Faltan ${6 - s.length} dígitos`;
      return null;
    }, {
      transform: v => v.replace(/\D/g, '').slice(0, 6)
    });
  },

  recalcularCurpYRfcWeb() {
    if (typeof Validators === 'undefined' || !Validators.calcularPrefijosCurpRfc) return;
    const { curpPrefijo, rfcPrefijo } = Validators.calcularPrefijosCurpRfc({
      primerNombre: document.getElementById('new-nombre')?.value || '',
      segundoNombre: document.getElementById('new-segundo-nombre')?.value || '',
      apellidoPaterno: document.getElementById('new-ap-paterno')?.value || '',
      apellidoMaterno: document.getElementById('new-ap-materno')?.value || '',
      fechaNac: document.getElementById('new-fecha-nac')?.value || '',
      sexo: document.getElementById('new-sexo')?.value || ''
    });

    const curpEl = document.getElementById('new-curp');
    const rfcEl = document.getElementById('new-rfc');
    const curpHint = document.getElementById('curp-auto-hint');
    const rfcHint = document.getElementById('rfc-auto-hint');

    if (curpPrefijo && curpEl) {
      const actual = curpEl.value.trim().toUpperCase();
      if (!actual || (this.ultimoPrefijoCurpWeb && actual.startsWith(this.ultimoPrefijoCurpWeb))) {
        const sufijo = (this.ultimoPrefijoCurpWeb && actual.length > this.ultimoPrefijoCurpWeb.length)
          ? actual.slice(this.ultimoPrefijoCurpWeb.length)
          : '';
        curpEl.value = `${curpPrefijo}${sufijo}`.slice(0, 18);
        if (curpEl._liveValidate) curpEl._liveValidate();
      }
      this.ultimoPrefijoCurpWeb = curpPrefijo;
      if (curpHint) {
        curpHint.textContent = `✓ Inicio calculado (${curpPrefijo.length}/18) — completa los últimos ${18 - curpPrefijo.length} caracteres.`;
      }
    }

    if (rfcPrefijo && rfcEl) {
      const actual = rfcEl.value.trim().toUpperCase();
      if (!actual || (this.ultimoPrefijoRfcWeb && actual.startsWith(this.ultimoPrefijoRfcWeb))) {
        const sufijo = (this.ultimoPrefijoRfcWeb && actual.length > this.ultimoPrefijoRfcWeb.length)
          ? actual.slice(this.ultimoPrefijoRfcWeb.length)
          : '';
        rfcEl.value = `${rfcPrefijo}${sufijo}`.slice(0, 13);
        if (rfcEl._liveValidate) rfcEl._liveValidate();
      }
      this.ultimoPrefijoRfcWeb = rfcPrefijo;
      if (rfcHint) {
        rfcHint.textContent = `✓ Inicio calculado (${rfcPrefijo.length}/13) — agrega tu homoclave (3 dígitos).`;
      }
    }
  },

  limpiarEstadoCorreoWeb() {
    clearTimeout(this.timerCorreoWeb);
    this.correoDisponibleWeb = null;
    this.ultimoCorreoVerificadoWeb = '';
    const msgEl = document.getElementById('new-email-status-msg');
    if (msgEl) {
      msgEl.classList.add('hidden');
      msgEl.textContent = '';
    }
  },

  programarVerificacionCorreoWeb(correo) {
    clearTimeout(this.timerCorreoWeb);
    this.correoDisponibleWeb = null;
    const msgEl = document.getElementById('new-email-status-msg');
    if (msgEl) {
      msgEl.className = 'text-[11px] font-semibold mt-1 text-brand-700';
      msgEl.textContent = 'Verificando disponibilidad del correo...';
      msgEl.classList.remove('hidden');
    }
    this.timerCorreoWeb = setTimeout(() => this.verificarCorreoEnVivoWeb(correo), 450);
  },

  async verificarCorreoEnVivoWeb(correo) {
    const input = document.getElementById('new-email');
    const msgEl = document.getElementById('new-email-status-msg');
    if (!input || input.value.trim().toLowerCase() !== correo) return;

    const resp = await API.verificarCorreo(correo);
    if (!input || input.value.trim().toLowerCase() !== correo) return;

    if (resp.ok && resp.data && typeof resp.data.disponible === 'boolean') {
      this.ultimoCorreoVerificadoWeb = correo;
      this.correoDisponibleWeb = resp.data.disponible;
      if (resp.data.disponible) {
        if (msgEl) {
          msgEl.className = 'text-[11px] font-semibold mt-1 text-emerald-600';
          msgEl.textContent = '✓ Correo disponible para registro';
          msgEl.classList.remove('hidden');
        }
        Validators.setFeedback(input, null);
      } else {
        if (msgEl) msgEl.classList.add('hidden');
        Validators.setFeedback(input, 'Este correo ya está registrado. Usa otro correo o inicia sesión.');
      }
    } else if (msgEl) {
      msgEl.classList.add('hidden');
    }
  },

  validarCampos(ids) {
    let todoOk = true;
    for (const id of ids) {
      const el = document.getElementById(id);
      if (el && typeof el._liveValidate === 'function') {
        if (!el._liveValidate()) todoOk = false;
      }
    }
    return todoOk;
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
    this.iniciarMonitorSesion();
  },

  togglePasswordVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const mostrar = input.type === 'password';
    input.type = mostrar ? 'text' : 'password';
    if (btn) {
      btn.innerHTML = `<i data-lucide="${mostrar ? 'eye-off' : 'eye'}" class="w-4 h-4"></i>`;
      if (window.lucide) lucide.createIcons();
    }
  },

  setAuthTab(tab) {
    const tabLogin = document.getElementById('tab-login');
    const tabCrear = document.getElementById('tab-crear');
    const tabReg = document.getElementById('tab-registro');
    const formLogin = document.getElementById('form-login');
    const formCrear = document.getElementById('form-crear');
    const formReg = document.getElementById('form-registro');
    const authCard = document.getElementById('auth-card');
    const alertBox = document.getElementById('auth-alert');
    alertBox.classList.add('hidden');

    const activeCls = 'flex-1 py-2.5 text-xs font-bold rounded-lg transition-all duration-200 bg-white text-brand-700 shadow-sm';
    const inactiveCls = 'flex-1 py-2.5 text-xs font-bold rounded-lg transition-all duration-200 text-slate-500 hover:text-slate-800';

    tabLogin.className = tab === 'login' ? activeCls : inactiveCls;
    if (tabCrear) tabCrear.className = tab === 'crear' ? activeCls : inactiveCls;
    tabReg.className = tab === 'registro' ? activeCls : inactiveCls;

    formLogin.classList.toggle('hidden', tab !== 'login');
    if (formCrear) formCrear.classList.toggle('hidden', tab !== 'crear');
    formReg.classList.toggle('hidden', tab !== 'registro');

    if (authCard) {
      authCard.classList.toggle('max-w-2xl', tab === 'crear');
      authCard.classList.toggle('max-w-md', tab !== 'crear');
    }
    if (window.lucide) lucide.createIcons();
  },

  setAppSection(section) {
    const sections = ['dashboard', 'catalogo', 'historial', 'admin'];
    sections.forEach(s => {
      const el = document.getElementById(`section-${s}`);
      const navBtn = document.getElementById(`nav-${s}`);
      if (!el || !navBtn) return;
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
      historial: 'Historial de Transacciones',
      admin: 'Módulo Admin • Abonos a Clientes'
    };
    document.getElementById('section-title').textContent = titles[section] || 'GestoPago';

    if (section === 'catalogo' && this.productos.length === 0) {
      this.cargarCatalogo();
    }
    if (section === 'historial') {
      this.cargarHistorial();
    }
    if (section === 'admin') {
      this.cargarCuentasAdmin();
    }

    if (window.lucide) lucide.createIcons();
  },

  // -------------------------------------------------------------
  // AUTENTICACIÓN
  // -------------------------------------------------------------
  async handleLogin(e) {
    e.preventDefault();
    if (!this.validarCampos(['login-email', 'login-password'])) {
      this.mostrarAlertaAuth('error', 'Revisa los campos marcados en rojo antes de continuar.');
      return;
    }
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
    } catch (_) {
      this.mostrarAlertaAuth('error', 'No pudimos iniciar sesión en este momento. Intenta de nuevo.');
    } finally {
      btn.disabled = false;
      btn.innerHTML = `<span>Ingresar al Portal</span> <i data-lucide="arrow-right" class="w-4 h-4"></i>`;
      if (window.lucide) lucide.createIcons();
    }
  },

  async handleCrearCuenta(e) {
    e.preventDefault();
    const camposCrear = [
      'new-nombre', 'new-segundo-nombre', 'new-ap-paterno', 'new-ap-materno',
      'new-fecha-nac', 'new-curp', 'new-rfc', 'new-cp', 'new-colonia',
      'new-municipio', 'new-estado', 'new-calle', 'new-num-ext', 'new-pais',
      'new-empresa', 'new-ingreso', 'new-email', 'new-telefono',
      'new-password', 'new-password-confirm'
    ];
    if (!this.validarCampos(camposCrear)) {
      this.mostrarAlertaAuth('error', 'Corrige los campos marcados en rojo antes de enviar.');
      return;
    }
    const correoCheck = document.getElementById('new-email').value.trim().toLowerCase();
    if (this.ultimoCorreoVerificadoWeb !== correoCheck) {
      await this.verificarCorreoEnVivoWeb(correoCheck);
    }
    if (this.correoDisponibleWeb === false) {
      this.mostrarAlertaAuth('error', 'Este correo ya está registrado. Usa otro correo o inicia sesión.');
      return;
    }
    const pass = document.getElementById('new-password').value;
    const passConf = document.getElementById('new-password-confirm').value;
    const btn = document.getElementById('btn-crear-submit');

    if (pass !== passConf) {
      this.mostrarAlertaAuth('error', 'Las contraseñas no coinciden');
      return;
    }

    const limpiar = (v) => (v || '').trim().replace(/\s+/g, ' ');
    const fechaRaw = document.getElementById('new-fecha-nac').value; // yyyy-MM-dd
    const fechaFormateada = fechaRaw ? fechaRaw.replace(/-/g, '/') : '';
    const correo = document.getElementById('new-email').value.trim().toLowerCase();

    const payload = {
      nombre: limpiar(document.getElementById('new-nombre').value),
      apellidoPaterno: limpiar(document.getElementById('new-ap-paterno').value),
      apellidoMaterno: limpiar(document.getElementById('new-ap-materno').value),
      fechaNacimiento: fechaFormateada,
      curp: document.getElementById('new-curp').value.trim().toUpperCase(),
      rfc: document.getElementById('new-rfc').value.trim().toUpperCase(),
      sexo: document.getElementById('new-sexo').value,
      nacionalidad: document.getElementById('new-nacionalidad').value,
      estadoCivil: document.getElementById('new-estado-civil').value,
      correo,
      telefonoMovil: Number(document.getElementById('new-telefono').value.trim()),
      domicilio: {
        calle: limpiar(document.getElementById('new-calle').value),
        numeroExterior: Number(document.getElementById('new-num-ext').value),
        colonia: limpiar(document.getElementById('new-colonia').value),
        municipio: limpiar(document.getElementById('new-municipio').value),
        estado: limpiar(document.getElementById('new-estado').value),
        codigoPostal: Number(document.getElementById('new-cp').value),
        pais: limpiar(document.getElementById('new-pais').value)
      },
      ocupacion: limpiar(document.getElementById('new-ocupacion').value),
      empresa: limpiar(document.getElementById('new-empresa').value),
      ingresoMensual: Number(document.getElementById('new-ingreso').value).toFixed(2)
    };

    const segNombre = limpiar(document.getElementById('new-segundo-nombre').value);
    if (segNombre) payload.segundoNombre = segNombre;

    btn.disabled = true;
    btn.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> <span>Creando cuenta...</span>`;

    try {
      const respCliente = await API.crearCliente(payload);
      if (!respCliente.ok) {
        const msg = (respCliente.errores && respCliente.errores.length > 0)
          ? respCliente.errores.join(' • ')
          : (respCliente.mensaje || 'No se pudo crear el cliente. Revisa tus datos.');
        this.mostrarAlertaAuth('error', msg);
        return;
      }

      const numCuenta = respCliente.data?.numeroCuenta || '';
      const respPortal = await API.registrar(correo, pass);
      if (!respPortal.ok) {
        this.mostrarAlertaAuth('error', `Cuenta ${numCuenta} creada, pero falló la contraseña: ${respPortal.mensaje}`);
        return;
      }

      this.mostrarAlertaAuth('success', `¡Cuenta creada exitosamente! Número de cuenta asignado: ${numCuenta}. Ya puedes iniciar sesión.`);
      document.getElementById('form-crear').reset();
      this.limpiarEstadoCorreoWeb();
      document.getElementById('login-email').value = correo;
      setTimeout(() => this.setAuthTab('login'), 2500);
    } catch (_) {
      this.mostrarAlertaAuth('error', 'No pudimos crear la cuenta en este momento. Intenta de nuevo.');
    } finally {
      btn.disabled = false;
      btn.innerHTML = `<span>Crear Cuenta y Activar Acceso</span> <i data-lucide="user-plus" class="w-4 h-4"></i>`;
      if (window.lucide) lucide.createIcons();
    }
  },

  async handleRegistro(e) {
    e.preventDefault();
    if (!this.validarCampos(['reg-email', 'reg-password', 'reg-password-confirm'])) {
      this.mostrarAlertaAuth('error', 'Revisa los campos marcados en rojo.');
      return;
    }
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
    } catch (_) {
      this.mostrarAlertaAuth('error', 'No pudimos completar el registro. Verifica tu conexión e intenta de nuevo.');
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

  async handleLogout() {
    this.detenerMonitorSesion();
    try {
      if (API.getToken()) {
        await API.cerrarSesionRemota();
      }
    } catch (_) {}
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
    } catch (_) {
      btnValidar.disabled = false;
      btnValidar.innerHTML = `<i data-lucide="search" class="w-4 h-4"></i> <span>Validar</span>`;
      this.resetValidacionReferencia();
      banner.className = 'p-3.5 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200 flex items-center gap-2';
      banner.innerHTML = `<i data-lucide="wifi-off" class="w-4 h-4 flex-shrink-0"></i> <span>No pudimos validar la referencia en este momento. Intenta de nuevo.</span>`;
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

    // No enviamos correo 2FA todavía: el usuario lo solicita explícitamente en el siguiente paso
    this.codigo2faSolicitadoWeb = false;
    this.codigoRespaldo2fa = null;

    document.getElementById('2fa-resumen-servicio').textContent = document.getElementById('pago-servicio-nombre').textContent;
    document.getElementById('2fa-resumen-ref').textContent = destino;
    document.getElementById('2fa-resumen-monto').textContent = this.formatoMoneda(monto);
    document.getElementById('input-codigo-2fa').value = '';
    document.getElementById('alerta-error-2fa').classList.add('hidden');
    document.getElementById('banner-codigo-respaldo-2fa').classList.add('hidden');
    document.getElementById('bloque-solicitar-2fa').classList.remove('hidden');
    document.getElementById('bloque-input-2fa').classList.add('hidden');
    document.getElementById('btn-confirmar-2fa').classList.add('hidden');

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

    document.getElementById('modal-step-form').classList.add('hidden');
    document.getElementById('modal-step-2fa').classList.remove('hidden');
    if (window.lucide) lucide.createIcons();
  },

  async solicitarCodigo2faWeb() {
    if (!this.cuenta?.numeroCuenta) return;
    const btnEnviar = document.getElementById('btn-enviar-codigo-2fa');
    const alerta = document.getElementById('alerta-error-2fa');
    alerta.classList.add('hidden');

    if (btnEnviar) {
      btnEnviar.disabled = true;
      btnEnviar.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> <span>Enviando código...</span>`;
    }

    try {
      const resp2fa = await API.solicitar2fa(this.cuenta.numeroCuenta, true);
      if (resp2fa.ok && resp2fa.data) {
        this.codigo2faSolicitadoWeb = true;
        document.getElementById('correo-enmascarado-2fa').textContent = resp2fa.data.correoEnmascarado || 'tu correo registrado';

        const bannerRespaldo = document.getElementById('banner-codigo-respaldo-2fa');
        const valorRespaldo = document.getElementById('valor-codigo-respaldo-2fa');
        this.codigoRespaldo2fa = resp2fa.data.codigoParaPruebas || null;
        if (this.codigoRespaldo2fa && bannerRespaldo && valorRespaldo) {
          valorRespaldo.textContent = this.codigoRespaldo2fa;
          bannerRespaldo.classList.remove('hidden');
        } else if (bannerRespaldo) {
          bannerRespaldo.classList.add('hidden');
        }

        document.getElementById('bloque-solicitar-2fa').classList.add('hidden');
        document.getElementById('bloque-input-2fa').classList.remove('hidden');
        document.getElementById('btn-confirmar-2fa').classList.remove('hidden');
        document.getElementById('input-codigo-2fa').focus();
      } else {
        alerta.textContent = resp2fa.mensaje || 'No pudimos enviar el código de seguridad. Intenta de nuevo.';
        alerta.classList.remove('hidden');
      }
    } catch (_) {
      alerta.textContent = 'No pudimos enviar el código de seguridad. Intenta de nuevo.';
      alerta.classList.remove('hidden');
    } finally {
      if (btnEnviar) {
        btnEnviar.disabled = false;
        btnEnviar.innerHTML = `<i data-lucide="mail" class="w-4 h-4"></i> <span>Enviar código 2FA a mi correo</span>`;
      }
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

        // Mostrar Ticket Digital (Aprobado o OPERACIÓN RECHAZADA -1)
        const aprobada = p.aprobada !== false && p.codigoProveedor !== '-1' && p.estado !== 'RECHAZADA';
        const iconBox = document.getElementById('recibo-icono-box');
        const tituloEl = document.getElementById('recibo-titulo');
        const subtituloEl = document.getElementById('recibo-subtitulo');
        const estadoEl = document.getElementById('recibo-estado');
        const leyendaBox = document.getElementById('recibo-leyenda-box');
        const leyendaTitulo = document.getElementById('recibo-leyenda-titulo');

        if (aprobada) {
          iconBox.className = 'w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 mx-auto flex items-center justify-center';
          iconBox.innerHTML = `<i data-lucide="check-circle" class="w-9 h-9"></i>`;
          tituloEl.className = 'text-lg font-extrabold text-slate-900';
          tituloEl.textContent = '¡Pago Procesado Exitosamente!';
          subtituloEl.textContent = 'Ticket enviado automáticamente a tu correo electrónico';
          estadoEl.className = 'font-extrabold text-emerald-600';
          estadoEl.textContent = 'EXITOSA';
          document.getElementById('recibo-monto-label').textContent = 'Importe Pagado:';
          leyendaBox.className = 'mt-3 p-3 rounded-xl bg-brand-50 border border-brand-200 text-[11px] text-slate-700 leading-relaxed';
          leyendaTitulo.className = 'block text-brand-700 mb-0.5';
          leyendaTitulo.textContent = 'Dudas y aclaraciones:';
          document.getElementById('row-recibo-auth').classList.remove('hidden');
          document.getElementById('row-recibo-codigo').classList.add('hidden');
        } else {
          iconBox.className = 'w-16 h-16 rounded-full bg-red-100 text-red-600 mx-auto flex items-center justify-center';
          iconBox.innerHTML = `<i data-lucide="x-circle" class="w-9 h-9"></i>`;
          tituloEl.className = 'text-lg font-extrabold text-red-600';
          tituloEl.textContent = 'OPERACIÓN RECHAZADA';
          subtituloEl.textContent = `${p.mensajeProveedor || 'Transacción no válida'} • No se realizó ningún cargo • Se envió ticket a tu correo`;
          estadoEl.className = 'font-extrabold text-red-600';
          estadoEl.textContent = 'OPERACIÓN RECHAZADA';
          document.getElementById('recibo-monto-label').textContent = 'Importe Intentado:';
          leyendaBox.className = 'mt-3 p-3 rounded-xl bg-red-50 border border-red-200 text-[11px] text-red-900 leading-relaxed';
          leyendaTitulo.className = 'block text-red-700 mb-0.5';
          leyendaTitulo.textContent = 'Dudas y aclaraciones:';
          document.getElementById('row-recibo-auth').classList.add('hidden');
          document.getElementById('row-recibo-codigo').classList.remove('hidden');
          document.getElementById('recibo-codigo').textContent = p.codigoProveedor || '-1';
        }

        document.getElementById('recibo-mensaje').textContent = p.mensajeProveedor || (aprobada ? 'Operación exitosa' : 'Transacción no válida');
        document.getElementById('recibo-upc').textContent = p.upc || '-';
        document.getElementById('recibo-fecha').textContent = `${p.fecha || ''} ${p.hora || ''}`;
        document.getElementById('recibo-servicio').textContent = this.pagoPendiente.servicioNombre;
        document.getElementById('recibo-producto').textContent = this.pagoPendiente.productoNombre;
        document.getElementById('recibo-auth').textContent = p.numAutorizacion || '-';
        document.getElementById('recibo-destino').textContent = p.destino || '-';
        document.getElementById('recibo-monto').textContent = this.formatoMoneda(p.monto);
        document.getElementById('recibo-nuevo-saldo').textContent = this.formatoMoneda(p.saldoRestante);
        document.getElementById('recibo-leyenda').textContent = p.leyenda || 'Conserva este ticket con tu UPC de 10 dígitos para dudas y aclaraciones.';

        document.getElementById('modal-step-2fa').classList.add('hidden');
        document.getElementById('modal-step-comprobante').classList.remove('hidden');
      } else {
        alerta.textContent = resp.mensaje || (resp.errores && resp.errores[0]) || 'Código 2FA incorrecto o pago rechazado';
        alerta.classList.remove('hidden');
      }
    } catch (_) {
      alerta.textContent = 'No pudimos procesar el pago en este momento. Intenta de nuevo.';
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
  },

  // -------------------------------------------------------------
  // MÓDULO ADMIN (ABONOS A CLIENTES)
  // -------------------------------------------------------------
  async cargarCuentasAdmin() {
    const tbody = document.getElementById('admin-cuentas-tbody');
    if (tbody) {
      tbody.innerHTML = `<tr><td colspan="4" class="py-8 text-center text-slate-400">Cargando clientes y cuentas...</td></tr>`;
    }

    const [respClientes, respCuentas] = await Promise.all([
      API.listarClientes(),
      API.listarCuentas()
    ]);

    const mapaClientes = new Map();
    if (respClientes.ok && Array.isArray(respClientes.data)) {
      respClientes.data.forEach(c => {
        if (c && c.id != null) mapaClientes.set(Number(c.id), c);
      });
    }

    if (respCuentas.ok && Array.isArray(respCuentas.data)) {
      this.adminCuentas = respCuentas.data.map(cta => {
        const cli = mapaClientes.get(Number(cta.clienteId)) || {};
        const nombreCompleto = [cli.nombre, cli.segundoNombre, cli.apellidoPaterno, cli.apellidoMaterno]
          .filter(Boolean)
          .join(' ') || `Cliente #${cta.clienteId || '-'}`;
        return {
          numeroCuenta: String(cta.numeroCuenta || ''),
          saldoActual: Number(cta.saldoActual || 0),
          nombre: nombreCompleto,
          correo: cli.correo || ''
        };
      });
      this.renderCuentasAdmin();
    } else if (tbody) {
      tbody.innerHTML = `<tr><td colspan="4" class="py-8 text-center text-red-500 font-semibold">${respCuentas.mensaje || 'No se pudieron cargar las cuentas.'}</td></tr>`;
    }
  },

  filtrarCuentasAdmin(q) {
    this.adminFiltro = (q || '').trim().toLowerCase();
    this.renderCuentasAdmin();
  },

  renderCuentasAdmin() {
    const tbody = document.getElementById('admin-cuentas-tbody');
    if (!tbody) return;
    const q = this.adminFiltro;
    const filtradas = q
      ? this.adminCuentas.filter(item =>
          item.nombre.toLowerCase().includes(q) ||
          item.correo.toLowerCase().includes(q) ||
          item.numeroCuenta.includes(q)
        )
      : this.adminCuentas;

    if (!filtradas.length) {
      tbody.innerHTML = `<tr><td colspan="4" class="py-8 text-center text-slate-400">No se encontraron cuentas con ese criterio.</td></tr>`;
      return;
    }

    tbody.innerHTML = filtradas.map(item => `
      <tr class="hover:bg-brand-50/40 transition-colors cursor-pointer" onclick="app.seleccionarCuentaAdmin('${item.numeroCuenta}')">
        <td class="py-3 px-5">
          <span class="block font-bold text-slate-900">${item.nombre}</span>
          <span class="text-[11px] text-slate-500">${item.correo || 'Sin correo'}</span>
        </td>
        <td class="py-3 px-4 font-mono font-bold text-brand-700">${item.numeroCuenta}</td>
        <td class="py-3 px-4 text-right font-extrabold text-emerald-700">${this.formatoMoneda(item.saldoActual)}</td>
        <td class="py-3 px-5 text-center">
          <button type="button" onclick="event.stopPropagation(); app.seleccionarCuentaAdmin('${item.numeroCuenta}')" class="px-3 py-1.5 rounded-lg bg-brand-50 hover:bg-brand-100 text-brand-700 font-bold text-[11px]">
            Seleccionar
          </button>
        </td>
      </tr>
    `).join('');
  },

  seleccionarCuentaAdmin(numCuenta) {
    const el = document.getElementById('admin-cuenta');
    if (el) {
      el.value = numCuenta;
      if (el._liveValidate) el._liveValidate();
    }
    const montoEl = document.getElementById('admin-monto');
    if (montoEl) montoEl.focus();
  },

  setMontoAdmin(monto) {
    const el = document.getElementById('admin-monto');
    if (el) {
      el.value = String(monto);
      if (el._liveValidate) el._liveValidate();
    }
  },

  mostrarAlertaAdmin(tipo, mensaje) {
    const box = document.getElementById('admin-abono-alert');
    if (!box) return;
    box.classList.remove('hidden');
    if (tipo === 'success') {
      box.className = 'p-3 rounded-xl text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200';
    } else {
      box.className = 'p-3 rounded-xl text-xs font-semibold bg-red-50 text-red-700 border border-red-200';
    }
    box.textContent = mensaje;
  },

  async handleAbonoAdmin(e) {
    e.preventDefault();
    if (!this.validarCampos(['admin-cuenta', 'admin-monto'])) {
      this.mostrarAlertaAdmin('error', 'Revisa los campos marcados en rojo antes de aplicar el abono.');
      return;
    }

    const numeroCuenta = document.getElementById('admin-cuenta').value.trim();
    const monto = Number(document.getElementById('admin-monto').value);
    const btn = document.getElementById('btn-admin-abonar');

    btn.disabled = true;
    btn.innerHTML = `<span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> <span>Aplicando abono...</span>`;

    try {
      const resp = await API.abonarCuenta(numeroCuenta, monto);
      if (resp.ok) {
        this.mostrarAlertaAdmin('success', `✓ Abono de ${this.formatoMoneda(monto)} aplicado a la cuenta ${numeroCuenta}.`);
        document.getElementById('admin-monto').value = '';
        await this.cargarCuentasAdmin();
        if (this.cuenta && this.cuenta.numeroCuenta === numeroCuenta) {
          await this.cargarDatosUsuario();
        }
      } else {
        this.mostrarAlertaAdmin('error', resp.mensaje || 'No se pudo aplicar el abono a esa cuenta.');
      }
    } catch (_) {
      this.mostrarAlertaAdmin('error', 'No pudimos aplicar el abono en este momento. Intenta de nuevo.');
    } finally {
      btn.disabled = false;
      btn.innerHTML = `<i data-lucide="plus-circle" class="w-4 h-4"></i> <span>Aplicar Abono</span>`;
      if (window.lucide) lucide.createIcons();
    }
  }
};

// Arrancar aplicación al cargar el DOM
document.addEventListener('DOMContentLoaded', () => {
  app.init();
});

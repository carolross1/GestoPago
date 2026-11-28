// Validaciones en tiempo real compartidas por todos los formularios del Portal Web GestoPago
const Validators = {
  unaPalabraLetras: /^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ]+$/,
  soloLetrasEspacios: /^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ]+( [A-Za-zÁÉÍÓÚÜáéíóúüÑñ]+)*$/,
  textoDireccion: /^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ0-9]+([ .,-][A-Za-zÁÉÍÓÚÜáéíóúüÑñ0-9]+)*$/,
  curpRegex: /^[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d$/,
  rfcRegex: /^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{2,3}$/,
  correoRegex: /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,10}$/,

  tldsValidos: new Set([
    'com', 'mx', 'net', 'org', 'edu', 'gob', 'gov', 'io', 'co', 'es',
    'me', 'info', 'us', 'uk', 'ca', 'ar', 'cl', 'pe', 'app', 'dev',
    'tech', 'xyz', 'biz', 'tv', 'pro', 'live', 'online', 'store', 'ai'
  ]),

  typosDominios: {
    'gamil.com': 'gmail.com',
    'gmial.com': 'gmail.com',
    'gmal.com': 'gmail.com',
    'gmail.con': 'gmail.com',
    'gmail.co': 'gmail.com',
    'hotmial.com': 'hotmail.com',
    'hotmal.com': 'hotmail.com',
    'outlok.com': 'outlook.com',
    'outllok.com': 'outlook.com',
    'yaho.com': 'yahoo.com',
    'icloud.con': 'icloud.com'
  },

  limpiarEspacios(v) {
    return (v || '').trim().replace(/\s+/g, ' ');
  },

  primerNombre(val) {
    const v = (val || '').trim();
    if (!v) return 'El primer nombre es obligatorio';
    if (v.includes(' ')) return 'Solo un nombre aquí (si tienes dos, usa Segundo Nombre)';
    if (!this.unaPalabraLetras.test(v)) return 'Solo letras sin números ni símbolos';
    if (v.length < 2) return `Debe tener mínimo 2 caracteres (llevas ${v.length})`;
    if (v.length > 50) return 'Máximo 50 caracteres';
    return null;
  },

  segundoNombre(val) {
    const v = (val || '').trim();
    if (!v) return null; // Opcional
    if (v.includes(' ')) return 'Solo un segundo nombre (máximo 2 nombres en total)';
    if (!this.unaPalabraLetras.test(v)) return 'Solo letras sin números ni símbolos';
    if (v.length < 2) return 'Si lo escribes, debe tener mínimo 2 caracteres';
    if (v.length > 50) return 'Máximo 50 caracteres';
    return null;
  },

  apellido(val, etiqueta) {
    const v = this.limpiarEspacios(val);
    if (!v) return `Ingresa ${etiqueta}`;
    if (!this.soloLetrasEspacios.test(v)) return 'Solo letras y espacios, sin números ni símbolos';
    if (v.length < 3) return `Debe tener mínimo 3 caracteres (llevas ${v.length})`;
    if (v.length > 50) return 'Máximo 50 caracteres';
    return null;
  },

  fechaNacimiento(val) {
    if (!val) return 'Selecciona tu fecha de nacimiento';
    const [y, m, d] = val.split('-').map(Number);
    const fecha = new Date(y, m - 1, d);
    const hoy = new Date();
    let edad = hoy.getFullYear() - fecha.getFullYear();
    const diffMes = hoy.getMonth() - fecha.getMonth();
    if (diffMes < 0 || (diffMes === 0 && hoy.getDate() < fecha.getDate())) {
      edad--;
    }
    if (fecha > hoy) return 'La fecha de nacimiento no puede ser futura';
    if (edad < 18) return `Debes ser mayor de 18 años (edad calculada: ${edad})`;
    return null;
  },

  curp(val) {
    const v = (val || '').trim().toUpperCase();
    if (!v) return 'Ingresa tu CURP (18 caracteres)';
    if (v.length < 18) return `Faltan ${18 - v.length} caracteres (${v.length}/18)`;
    if (!this.curpRegex.test(v)) return 'CURP inválida (ej. PELJ990131HGTRPN09)';
    return null;
  },

  rfc(val) {
    const v = (val || '').trim().toUpperCase();
    if (!v) return 'Ingresa tu RFC (12 o 13 caracteres)';
    if (v.length < 12) return `Faltan ${12 - v.length} caracteres (mín. 12)`;
    if (!this.rfcRegex.test(v)) return 'RFC inválido (ej. PELJ990131AB1)';
    return null;
  },

  codigoPostal(val) {
    const v = (val || '').trim();
    if (!v) return 'Ingresa el código postal';
    if (!/^\d+$/.test(v)) return 'Solo dígitos numéricos';
    if (v.length < 5) return `Faltan ${5 - v.length} dígitos (5 en total)`;
    const n = Number(v);
    if (n < 1000 || n > 99999) return 'CP inválido (01000 a 99999)';
    return null;
  },

  textoDir(val, etiqueta) {
    const v = this.limpiarEspacios(val);
    if (!v) return `Ingresa ${etiqueta}`;
    if (!this.textoDireccion.test(v)) return 'Solo letras, números y espacios válidos';
    if (v.length < 2) return 'Debe tener mínimo 2 caracteres';
    return null;
  },

  soloLetras(val, etiqueta, minLen = 2) {
    const v = this.limpiarEspacios(val);
    if (!v) return `Ingresa ${etiqueta}`;
    if (!this.soloLetrasEspacios.test(v)) return 'Solo letras y espacios, sin números ni símbolos';
    if (v.length < minLen) return `Debe tener mínimo ${minLen} caracteres`;
    return null;
  },

  numeroDomicilio(val) {
    const v = (val || '').trim();
    if (!v) return 'Obligatorio';
    const n = Number(v);
    if (!Number.isInteger(n) || n < 1 || n > 999999) return 'Entero entre 1 y 999999';
    return null;
  },

  correo(val) {
    const c = (val || '').trim().toLowerCase();
    if (!c) return 'Ingresa tu correo electrónico';
    if (!this.correoRegex.test(c)) return 'Formato inválido (ej. nombre@gmail.com)';
    const dominio = c.split('@').pop();
    if (this.typosDominios[dominio]) {
      return `Dominio inválido. ¿Quisiste escribir ${this.typosDominios[dominio]}?`;
    }
    const partes = dominio.split('.');
    const tld = partes[partes.length - 1];
    if (partes[0].length < 2 || !this.tldsValidos.has(tld)) {
      return 'Usa un dominio válido (ej. @gmail.com, @outlook.com, .edu.mx)';
    }
    return null;
  },

  telefonoMexico(val) {
    const t = (val || '').trim();
    if (!t) return 'Ingresa tu teléfono de 10 dígitos';
    if (!/^\d+$/.test(t)) return 'Solo se permiten dígitos';
    if (t.length < 10) return `Faltan ${10 - t.length} dígitos (${t.length}/10)`;
    if (t.length > 10) return 'Debe tener exactamente 10 dígitos';
    if (t.startsWith('0') || t.startsWith('1')) return 'No puede iniciar con 0 ni 1';
    if (/^(\d)\1{9}$/.test(t)) return 'No puede tener los 10 dígitos repetidos';
    if (['1234567890', '0123456789', '9876543210', '2345678901'].includes(t)) {
      return 'Ingresa un número telefónico real';
    }
    return null;
  },

  password(val) {
    const p = val || '';
    if (!p) return 'Ingresa tu contraseña';
    if (p.length < 8) return `Mínimo 8 caracteres (faltan ${8 - p.length})`;
    return null;
  },

  confirmarPassword(val, original) {
    if (!val) return 'Confirma tu contraseña';
    if (val !== original) return 'Las contraseñas no coinciden';
    return null;
  },

  ingresoEnRango(val, min, max) {
    const s = (val || '').trim();
    if (!s) return 'Ingresa tu sueldo mensual dentro del rango';
    const n = Number(s);
    if (Number.isNaN(n) || n <= 0) return 'Ingresa una cantidad mayor a cero';
    if (n < min) return `Mínimo permitido en este tabulador: $${min.toLocaleString('es-MX')} MXN`;
    if (n > max) return `Máximo permitido en este tabulador: $${max.toLocaleString('es-MX')} MXN`;
    return null;
  },

  // Renderiza feedback visual en vivo debajo del input
  setFeedback(inputEl, errorMsg, esOpcionalVacio = false) {
    if (!inputEl) return true;
    const container = inputEl.closest('.relative') ? inputEl.closest('.relative').parentElement : inputEl.parentElement;
    if (!container) return !errorMsg;

    let msgEl = container.querySelector('.live-validation-msg');
    if (!msgEl) {
      msgEl = document.createElement('p');
      msgEl.className = 'live-validation-msg text-[11px] font-semibold mt-1 leading-tight';
      container.appendChild(msgEl);
    }

    inputEl.classList.remove('border-red-400', 'ring-1', 'ring-red-300', 'border-emerald-400', 'border-slate-200');

    if (errorMsg) {
      inputEl.classList.add('border-red-400', 'ring-1', 'ring-red-300');
      msgEl.className = 'live-validation-msg text-[11px] font-semibold mt-1 text-red-600 leading-tight';
      msgEl.textContent = errorMsg;
      return false;
    } else if (esOpcionalVacio) {
      inputEl.classList.add('border-slate-200');
      msgEl.textContent = '';
      return true;
    } else {
      inputEl.classList.add('border-emerald-400');
      msgEl.className = 'live-validation-msg text-[11px] font-semibold mt-1 text-emerald-600 leading-tight';
      msgEl.textContent = '✓ Válido';
      return true;
    }
  },

  bind(inputId, validatorFn, { opcional = false, transform = null, onValid = null } = {}) {
    const el = document.getElementById(inputId);
    if (!el) return;
    const run = () => {
      if (transform) {
        const nextVal = transform(el.value);
        if (nextVal !== el.value) el.value = nextVal;
      }
      const val = el.value;
      if (opcional && !val.trim()) {
        return this.setFeedback(el, null, true);
      }
      const err = validatorFn(val);
      const ok = this.setFeedback(el, err, false);
      if (ok && onValid) onValid(val);
      return ok;
    };
    el.addEventListener('input', run);
    el.addEventListener('blur', run);
    el._liveValidate = run;
  },

  // Calculador oficial (RENAPO / SAT) del prefijo base de CURP (11) y RFC (10)
  calcularPrefijosCurpRfc({ primerNombre, segundoNombre, apellidoPaterno, apellidoMaterno, fechaNac, sexo }) {
    const particulas = new Set([
      'DA', 'DAS', 'DE', 'DEL', 'DER', 'DI', 'DIE', 'DD',
      'EL', 'LA', 'LOS', 'LAS', 'LE', 'LES', 'MAC', 'MC',
      'VAN', 'VON', 'Y'
    ]);
    const nombresComunes = new Set(['JOSE', 'MARIA', 'MA', 'J']);
    const inconvenientes = new Set([
      'BACA', 'BAKA', 'BUEI', 'BUEY', 'CACA', 'CACO', 'CAGA', 'CAGO',
      'CAKA', 'CAKO', 'COGE', 'COGI', 'COJA', 'COJE', 'COJI', 'COJO',
      'COLA', 'CULO', 'FALO', 'FETO', 'GETA', 'GUEI', 'GUEY', 'JETA',
      'JOTO', 'KACA', 'KACO', 'KAGA', 'KAGO', 'KAKA', 'KAKO', 'KOGE',
      'KOGI', 'KOJA', 'KOJE', 'KOJI', 'KOJO', 'KOLA', 'KULO', 'LILO',
      'LOCA', 'LOCO', 'LOKA', 'LOKO', 'MAME', 'MAMO', 'MEAR', 'MEAS',
      'MEON', 'MIAR', 'MION', 'MOCO', 'MOKO', 'MULA', 'MULO', 'NACA',
      'NACO', 'PEDA', 'PEDO', 'PENE', 'PIPI', 'PITO', 'POPO', 'PUTA',
      'PUTO', 'QULO', 'RATA', 'ROBA', 'ROBE', 'ROBO', 'RUIN', 'SENO',
      'TETA', 'VACA', 'VAGA', 'VAGO', 'VAKA', 'VUEI', 'VUEY', 'WUEI',
      'WUEY'
    ]);

    const normalizar = (txt) => (txt || '')
      .trim()
      .toUpperCase()
      .replace(/Ñ/g, 'X')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^A-Z\s]/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();

    const palabraPrincipal = (txt, esNombre = false) => {
      const n = normalizar(txt);
      if (!n) return '';
      const words = n.split(' ').filter(Boolean);
      const filtradas = words.filter(w => !particulas.has(w));
      const cand = filtradas.length ? filtradas : words;
      if (esNombre && cand.length > 1 && nombresComunes.has(cand[0])) {
        return cand[1];
      }
      return cand[0] || '';
    };

    const nom = palabraPrincipal(`${primerNombre || ''} ${segundoNombre || ''}`, true);
    const pat = palabraPrincipal(apellidoPaterno || '');
    const mat = palabraPrincipal(apellidoMaterno || '');
    if (nom.length < 2 || pat.length < 2 || mat.length < 2 || !fechaNac || !/^\d{4}-\d{2}-\d{2}$/.test(fechaNac)) {
      return { curpPrefijo: '', rfcPrefijo: '' };
    }

    let vocal = 'X';
    for (let i = 1; i < pat.length; i++) {
      if ('AEIOU'.includes(pat[i])) {
        vocal = pat[i];
        break;
      }
    }
    const base4 = `${pat[0]}${vocal}${mat[0]}${nom[0]}`;
    let letrasCurp = base4;
    let letrasRfc = base4;
    if (inconvenientes.has(base4)) {
      letrasCurp = `${base4[0]}X${base4.slice(2)}`;
      letrasRfc = `${base4.slice(0, 3)}X`;
    }

    const [yyyy, mm, dd] = fechaNac.split('-');
    const fecha6 = `${yyyy.slice(-2)}${mm}${dd}`;
    const sexoChar = (sexo === 'H' || sexo === 'M') ? sexo : '';

    return {
      curpPrefijo: `${letrasCurp}${fecha6}${sexoChar}`,
      rfcPrefijo: `${letrasRfc}${fecha6}`
    };
  }
};

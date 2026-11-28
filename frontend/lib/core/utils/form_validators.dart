import 'package:flutter/services.dart';

/// Validaciones en tiempo real compartidas por todos los formularios de la app.
class FormValidators {
  static final RegExp _unaPalabraLetras =
      RegExp(r'^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ]+$');
  static final RegExp _soloLetrasEspacios =
      RegExp(r'^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ]+( [A-Za-zÁÉÍÓÚÜáéíóúüÑñ]+)*$');
  static final RegExp _textoDireccion =
      RegExp(r'^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ0-9]+([ .,-][A-Za-zÁÉÍÓÚÜáéíóúüÑñ0-9]+)*$');
  static final RegExp _curpRegex =
      RegExp(r'^[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d$');
  static final RegExp _rfcRegex =
      RegExp(r'^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{2,3}$');
  static final RegExp _correoRegex =
      RegExp(r'^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,10}$');

  static const Set<String> _tldsValidos = {
    'com', 'mx', 'net', 'org', 'edu', 'gob', 'gov', 'io', 'co', 'es',
    'me', 'info', 'us', 'uk', 'ca', 'ar', 'cl', 'pe', 'app', 'dev',
    'tech', 'xyz', 'biz', 'tv', 'pro', 'live', 'online', 'store', 'ai',
  };

  static const Map<String, String> _typosDominios = {
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
    'icloud.con': 'icloud.com',
  };

  static String limpiarEspacios(String v) =>
      v.trim().replaceAll(RegExp(r'\s+'), ' ');

  static String? primerNombre(String? val) {
    final limpio = (val ?? '').trim();
    if (limpio.isEmpty) return 'El nombre es obligatorio';
    if (limpio.contains(' ')) {
      return 'Solo un nombre aquí (si tienes dos, usa Segundo nombre)';
    }
    if (!_unaPalabraLetras.hasMatch(limpio)) {
      return 'Solo se permiten letras sin números ni símbolos';
    }
    if (limpio.length < 2) {
      return 'El nombre debe tener mínimo 2 caracteres';
    }
    if (limpio.length > 50) return 'Máximo 50 caracteres';
    return null;
  }

  static String? segundoNombre(String? val) {
    final limpio = (val ?? '').trim();
    if (limpio.isEmpty) return null; // Opcional
    if (limpio.contains(' ')) {
      return 'Solo un segundo nombre (máximo 2 nombres en total)';
    }
    if (!_unaPalabraLetras.hasMatch(limpio)) {
      return 'Solo se permiten letras sin números ni símbolos';
    }
    if (limpio.length < 2) {
      return 'Si lo escribes, debe tener mínimo 2 caracteres';
    }
    if (limpio.length > 50) return 'Máximo 50 caracteres';
    return null;
  }

  static String? apellido(String? val, String etiqueta) {
    final limpio = limpiarEspacios(val ?? '');
    if (limpio.isEmpty) return 'Ingresa $etiqueta';
    if (!_soloLetrasEspacios.hasMatch(limpio)) {
      return 'Solo letras y espacios, sin números ni símbolos';
    }
    if (limpio.length < 3) {
      return 'Debe tener mínimo 3 caracteres (llevas ${limpio.length})';
    }
    if (limpio.length > 50) return 'Máximo 50 caracteres';
    return null;
  }

  static String? soloLetras(String? val, String etiqueta, {int minLen = 2, int maxLen = 100}) {
    final limpio = limpiarEspacios(val ?? '');
    if (limpio.isEmpty) return 'Ingresa $etiqueta';
    if (!_soloLetrasEspacios.hasMatch(limpio)) {
      return 'Solo letras y espacios, sin números ni símbolos';
    }
    if (limpio.length < minLen) {
      return 'Debe tener mínimo $minLen caracteres (llevas ${limpio.length})';
    }
    if (limpio.length > maxLen) return 'Máximo $maxLen caracteres';
    return null;
  }

  static String? textoDireccion(String? val, String etiqueta, {int minLen = 2}) {
    final limpio = limpiarEspacios(val ?? '');
    if (limpio.isEmpty) return 'Ingresa $etiqueta';
    if (!_textoDireccion.hasMatch(limpio)) {
      return 'Solo letras, números y espacios válidos';
    }
    if (limpio.length < minLen) {
      return 'Debe tener mínimo $minLen caracteres';
    }
    if (limpio.length > 100) return 'Máximo 100 caracteres';
    return null;
  }

  static String? correo(String? val) {
    final c = (val ?? '').trim().toLowerCase();
    if (c.isEmpty) return 'Ingresa tu correo electrónico';
    if (!_correoRegex.hasMatch(c)) {
      return 'Formato inválido (ej. nombre@gmail.com)';
    }
    final partes = c.split('@');
    final dominio = partes.last;
    if (_typosDominios.containsKey(dominio)) {
      return 'Dominio inválido. ¿Quisiste escribir ${_typosDominios[dominio]}?';
    }
    final segmentos = dominio.split('.');
    final tld = segmentos.last;
    if (segmentos.first.length < 2 || !_tldsValidos.contains(tld)) {
      return 'Usa un dominio válido (ej. @gmail.com, @outlook.com, .edu.mx)';
    }
    return null;
  }

  static String? telefonoMexico(String? val, {bool opcional = false}) {
    final t = (val ?? '').trim();
    if (t.isEmpty) return opcional ? null : 'Ingresa tu teléfono de 10 dígitos';
    if (!RegExp(r'^\d+$').hasMatch(t)) return 'Solo se permiten dígitos numéricos';
    if (t.length < 10) return 'Faltan ${10 - t.length} dígitos (10 en total)';
    if (t.length > 10) return 'Debe tener exactamente 10 dígitos';
    if (t.startsWith('0') || t.startsWith('1')) {
      return 'Un teléfono móvil en México no inicia con 0 ni 1';
    }
    if (RegExp(r'^(\d)\1{9}$').hasMatch(t)) {
      return 'No puede tener los 10 dígitos repetidos';
    }
    const secuencias = {'1234567890', '0123456789', '9876543210', '2345678901'};
    if (secuencias.contains(t)) {
      return 'Ingresa un número telefónico real';
    }
    return null;
  }

  static String? curp(String? val) {
    final c = (val ?? '').trim().toUpperCase();
    if (c.isEmpty) return 'Ingresa tu CURP (18 caracteres)';
    if (c.length < 18) return 'Faltan ${18 - c.length} caracteres (${c.length}/18)';
    if (!_curpRegex.hasMatch(c)) {
      return 'Formato de CURP inválido (ej. PELJ990131HGTRPN09)';
    }
    return null;
  }

  static String? rfc(String? val) {
    final r = (val ?? '').trim().toUpperCase();
    if (r.isEmpty) return 'Ingresa tu RFC (12 o 13 caracteres)';
    if (r.length < 12) return 'Faltan ${12 - r.length} caracteres (mín. 12)';
    if (!_rfcRegex.hasMatch(r)) {
      return 'Formato de RFC inválido (ej. PELJ990131AB1)';
    }
    return null;
  }

  static String? codigoPostal(String? val) {
    final cp = (val ?? '').trim();
    if (cp.isEmpty) return 'Ingresa el código postal';
    if (cp.length < 5) return 'Faltan ${5 - cp.length} dígitos (5 en total)';
    final numCp = int.tryParse(cp);
    if (numCp == null || numCp < 1000 || numCp > 99999) {
      return 'CP inválido (01000 a 99999)';
    }
    return null;
  }

  static String? numeroDomicilio(String? val, {bool opcional = false}) {
    final s = (val ?? '').trim();
    if (s.isEmpty) return opcional ? null : 'Obligatorio';
    final n = int.tryParse(s);
    if (n == null || n < 1 || n > 999999) return 'Número entre 1 y 999999';
    return null;
  }

  static String? password(String? val) {
    final p = val ?? '';
    if (p.isEmpty) return 'Ingresa tu contraseña';
    if (p.length < 8) {
      return 'Mínimo 8 caracteres (faltan ${8 - p.length})';
    }
    return null;
  }

  static String? confirmarPassword(String? val, String original) {
    if (val == null || val.isEmpty) return 'Confirma tu contraseña';
    if (val != original) return 'Las contraseñas no coinciden';
    return null;
  }

  static String? ingresoEnRango(String? val, double minimo, double maximo) {
    final s = (val ?? '').trim();
    if (s.isEmpty) return 'Ingresa tu sueldo mensual dentro del rango';
    final monto = double.tryParse(s);
    if (monto == null || monto <= 0) return 'Ingresa una cantidad mayor a cero';
    if (monto < minimo) {
      return 'Mínimo permitido en este tabulador: \$${minimo.toStringAsFixed(0)} MXN';
    }
    if (monto > maximo) {
      return 'Máximo permitido en este tabulador: \$${maximo.toStringAsFixed(0)} MXN';
    }
    return null;
  }
}

/// Impide escribir un número mayor al límite máximo del tabulador salarial.
class MaxValueInputFormatter extends TextInputFormatter {
  final double maxValue;

  MaxValueInputFormatter(this.maxValue);

  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    final text = newValue.text.trim();
    if (text.isEmpty) return newValue;
    if (!RegExp(r'^\d+\.?\d{0,2}$').hasMatch(text)) {
      return oldValue;
    }
    final parsed = double.tryParse(text);
    if (parsed != null && parsed > maxValue) {
      return oldValue;
    }
    return newValue;
  }
}

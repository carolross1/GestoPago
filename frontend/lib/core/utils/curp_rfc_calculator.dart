/// Calculador oficial (reglas RENAPO / SAT) del prefijo base de CURP (11 caracteres)
/// y RFC (10 caracteres) a partir de los datos personales del usuario, para que solo
/// complete el resto de los dígitos (entidad/consonantes/homoclave).
class CurpRfcCalculator {
  static const Set<String> _particulas = {
    'DA', 'DAS', 'DE', 'DEL', 'DER', 'DI', 'DIE', 'DD',
    'EL', 'LA', 'LOS', 'LAS', 'LE', 'LES', 'MAC', 'MC',
    'VAN', 'VON', 'Y',
  };

  static const Set<String> _nombresComunes = {'JOSE', 'MARIA', 'MA', 'J'};

  static const Set<String> _palabrasInconvenientes = {
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
    'WUEY',
  };

  static String _normalizar(String texto) {
    final upper = texto.trim().toUpperCase();
    const mapa = {
      'Á': 'A', 'À': 'A', 'Ä': 'A', 'Â': 'A',
      'É': 'E', 'È': 'E', 'Ë': 'E', 'Ê': 'E',
      'Í': 'I', 'Ì': 'I', 'Ï': 'I', 'Î': 'I',
      'Ó': 'O', 'Ò': 'O', 'Ö': 'O', 'Ô': 'O',
      'Ú': 'U', 'Ù': 'U', 'Ü': 'U', 'Û': 'U',
      'Ñ': 'X',
    };
    final sb = StringBuffer();
    for (final rune in upper.runes) {
      final ch = String.fromCharCode(rune);
      sb.write(mapa[ch] ?? ch);
    }
    return sb
        .toString()
        .replaceAll(RegExp(r'[^A-Z\s]'), ' ')
        .replaceAll(RegExp(r'\s+'), ' ')
        .trim();
  }

  static String _palabraPrincipal(String texto, {bool esNombre = false}) {
    final norm = _normalizar(texto);
    if (norm.isEmpty) return '';
    final palabras = norm.split(' ').where((w) => w.isNotEmpty).toList();
    if (palabras.isEmpty) return '';

    final filtradas = palabras.where((w) => !_particulas.contains(w)).toList();
    final candidatas = filtradas.isNotEmpty ? filtradas : palabras;

    if (esNombre && candidatas.length > 1 && _nombresComunes.contains(candidatas.first)) {
      return candidatas[1];
    }
    return candidatas.first;
  }

  static String _primeraVocalInterna(String palabra) {
    for (int i = 1; i < palabra.length; i++) {
      final c = palabra[i];
      if ('AEIOU'.contains(c)) return c;
    }
    return 'X';
  }

  static ({String curpPrefijo, String rfcPrefijo}) calcularPrefijos({
    required String primerNombre,
    String segundoNombre = '',
    required String apellidoPaterno,
    required String apellidoMaterno,
    required DateTime? fechaNacimiento,
    required String sexo,
  }) {
    final nombreComb = '$primerNombre $segundoNombre'.trim();
    final nom = _palabraPrincipal(nombreComb, esNombre: true);
    final pat = _palabraPrincipal(apellidoPaterno);
    final mat = _palabraPrincipal(apellidoMaterno);

    if (nom.length < 2 || pat.length < 2 || mat.length < 2 || fechaNacimiento == null) {
      return (curpPrefijo: '', rfcPrefijo: '');
    }

    final l1 = pat[0];
    final l2 = _primeraVocalInterna(pat);
    final l3 = mat[0];
    final l4 = nom[0];
    final base4 = '$l1$l2$l3$l4';

    String letrasCurp = base4;
    String letrasRfc = base4;
    if (_palabrasInconvenientes.contains(base4)) {
      letrasCurp = '${base4[0]}X${base4.substring(2)}';
      letrasRfc = '${base4.substring(0, 3)}X';
    }

    final yy = (fechaNacimiento.year % 100).toString().padLeft(2, '0');
    final mm = fechaNacimiento.month.toString().padLeft(2, '0');
    final dd = fechaNacimiento.day.toString().padLeft(2, '0');
    final fecha6 = '$yy$mm$dd';

    final sexoChar = (sexo == 'H' || sexo == 'M') ? sexo : '';

    return (
      curpPrefijo: '$letrasCurp$fecha6$sexoChar',
      rfcPrefijo: '$letrasRfc$fecha6',
    );
  }
}

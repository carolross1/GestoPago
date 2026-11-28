import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/form_validators.dart';
import '../../providers/auth_provider.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';
import '../home/home_screen.dart';

class CrearCuentaScreen extends StatefulWidget {
  const CrearCuentaScreen({super.key});

  @override
  State<CrearCuentaScreen> createState() => _CrearCuentaScreenState();
}

class _CrearCuentaScreenState extends State<CrearCuentaScreen> {
  final _step1Key = GlobalKey<FormState>();
  final _step2Key = GlobalKey<FormState>();
  final _step3Key = GlobalKey<FormState>();

  int _pasoActual = 0;
  bool _isLoading = false;

  // Paso 1: Datos Personales
  final _nombreCtrl = TextEditingController();
  final _segundoNombreCtrl = TextEditingController();
  final _apellidoPaternoCtrl = TextEditingController();
  final _apellidoMaternoCtrl = TextEditingController();
  final _fechaNacCtrl = TextEditingController();
  final _curpCtrl = TextEditingController();
  final _rfcCtrl = TextEditingController();
  DateTime? _fechaNacimiento;
  String _sexo = 'M';
  String _estadoCivil = 'SOLTERO';
  String _nacionalidad = 'MEX';
  List<Map<String, String>> _nacionalidades = const [
    {'codigo': 'MEX', 'nombre': 'México'},
    {'codigo': 'USA', 'nombre': 'Estados Unidos'},
    {'codigo': 'CAN', 'nombre': 'Canadá'},
    {'codigo': 'COL', 'nombre': 'Colombia'},
    {'codigo': 'ESP', 'nombre': 'España'},
    {'codigo': 'ARG', 'nombre': 'Argentina'},
  ];

  // Paso 2: Domicilio (con API de Código Postal) y Profesión/Tabulador Salarial
  final _cpCtrl = TextEditingController();
  final _calleCtrl = TextEditingController();
  final _numExtCtrl = TextEditingController();
  final _numIntCtrl = TextEditingController();
  final _coloniaCtrl = TextEditingController();
  final _municipioCtrl = TextEditingController();
  final _estadoCtrl = TextEditingController();
  final _paisCtrl = TextEditingController(text: 'Mexico');

  bool _buscandoCp = false;
  String? _mensajeCp;
  bool _cpEncontrado = false;
  String _ultimoCpBuscado = '';
  List<String> _coloniasDisponibles = [];
  String? _coloniaSeleccionada;
  bool _coloniaManual = false;

  // Catálogo oficial de profesiones (INEGI SINCO) + Tabulador salarial
  List<Map<String, dynamic>> _tabuladores = [
    {'id': 'TAB_1', 'etiqueta': 'De \$0 a \$10,000 MXN', 'minimo': 0.01, 'maximo': 10000.0},
    {'id': 'TAB_2', 'etiqueta': 'De \$10,001 a \$25,000 MXN', 'minimo': 10001.0, 'maximo': 25000.0},
    {'id': 'TAB_3', 'etiqueta': 'De \$25,001 a \$50,000 MXN', 'minimo': 25001.0, 'maximo': 50000.0},
    {'id': 'TAB_4', 'etiqueta': 'De \$50,001 a \$100,000 MXN', 'minimo': 50001.0, 'maximo': 100000.0},
    {'id': 'TAB_5', 'etiqueta': 'De \$100,001 a \$250,000 MXN', 'minimo': 100001.0, 'maximo': 250000.0},
  ];

  List<Map<String, dynamic>> _profesiones = [
    {'nombre': 'Estudiante', 'categoria': 'Educación', 'tabuladorSugerido': 'TAB_1'},
    {'nombre': 'Trabajo del Hogar', 'categoria': 'Servicios', 'tabuladorSugerido': 'TAB_1'},
    {'nombre': 'Jubilado o Pensionado', 'categoria': 'Pensiones', 'tabuladorSugerido': 'TAB_1'},
    {'nombre': 'Auxiliar Administrativo', 'categoria': 'Administración', 'tabuladorSugerido': 'TAB_2'},
    {'nombre': 'Comerciante', 'categoria': 'Comercio', 'tabuladorSugerido': 'TAB_2'},
    {'nombre': 'Docente o Profesor', 'categoria': 'Educación', 'tabuladorSugerido': 'TAB_2'},
    {'nombre': 'Enfermero', 'categoria': 'Salud', 'tabuladorSugerido': 'TAB_2'},
    {'nombre': 'Disenador Grafico o Digital', 'categoria': 'Creatividad', 'tabuladorSugerido': 'TAB_2'},
    {'nombre': 'Contador Publico', 'categoria': 'Finanzas', 'tabuladorSugerido': 'TAB_3'},
    {'nombre': 'Abogado', 'categoria': 'Derecho', 'tabuladorSugerido': 'TAB_3'},
    {'nombre': 'Arquitecto', 'categoria': 'Construcción', 'tabuladorSugerido': 'TAB_3'},
    {'nombre': 'Ingeniero en Sistemas', 'categoria': 'Tecnología', 'tabuladorSugerido': 'TAB_3'},
    {'nombre': 'Desarrollador de Software', 'categoria': 'Tecnología', 'tabuladorSugerido': 'TAB_3'},
    {'nombre': 'Medico General o Especialista', 'categoria': 'Salud', 'tabuladorSugerido': 'TAB_4'},
    {'nombre': 'Empresario', 'categoria': 'Dirección', 'tabuladorSugerido': 'TAB_4'},
    {'nombre': 'Director Ejecutivo', 'categoria': 'Dirección', 'tabuladorSugerido': 'TAB_5'},
  ];

  String _profesionSeleccionada = 'Estudiante';
  String _tabuladorId = 'TAB_1';
  final _empresaCtrl = TextEditingController();
  final _ingresoCtrl = TextEditingController();

  // Paso 3: Contacto y Acceso
  final _correoCtrl = TextEditingController();
  final _telefonoMovilCtrl = TextEditingController();
  final _telefonoAltCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _confirmPasswordCtrl = TextEditingController();

  Map<String, dynamic> get _tabuladorActual {
    return _tabuladores.firstWhere(
      (t) => t['id'] == _tabuladorId,
      orElse: () => _tabuladores.first,
    );
  }

  double get _sueldoMin => (_tabuladorActual['minimo'] as num).toDouble();
  double get _sueldoMax => (_tabuladorActual['maximo'] as num).toDouble();

  @override
  void initState() {
    super.initState();
    _cargarCatalogos();
  }

  Future<void> _cargarCatalogos() async {
    final auth = Provider.of<AuthProvider>(context, listen: false);
    final results = await Future.wait([
      auth.obtenerNacionalidades(),
      auth.obtenerCatalogoProfesiones(),
    ]);
    if (!mounted) return;

    final listaNac = results[0] as List<Map<String, String>>;
    final catProf = results[1] as Map<String, dynamic>?;

    setState(() {
      if (listaNac.isNotEmpty) {
        _nacionalidades = listaNac;
        if (!_nacionalidades.any((n) => n['codigo'] == _nacionalidad)) {
          _nacionalidad = _nacionalidades.first['codigo']!;
        }
      }
      if (catProf != null) {
        final tabs = catProf['tabuladores'];
        if (tabs is List && tabs.isNotEmpty) {
          _tabuladores = tabs
              .whereType<Map<String, dynamic>>()
              .map((t) => {
                    'id': t['id'].toString(),
                    'etiqueta': t['etiqueta'].toString(),
                    'minimo': (t['minimo'] as num).toDouble(),
                    'maximo': (t['maximo'] as num).toDouble(),
                  })
              .toList();
        }
        final profs = catProf['profesiones'];
        if (profs is List && profs.isNotEmpty) {
          _profesiones = profs
              .whereType<Map<String, dynamic>>()
              .map((p) => {
                    'nombre': p['nombre'].toString(),
                    'categoria': (p['categoria'] ?? '').toString(),
                    'tabuladorSugerido': (p['tabuladorSugerido'] ?? 'TAB_1').toString(),
                  })
              .toList();
          if (!_profesiones.any((p) => p['nombre'] == _profesionSeleccionada)) {
            _profesionSeleccionada = _profesiones.first['nombre'].toString();
          }
        }
      }
    });
  }

  Future<void> _buscarDireccionPorCp(String cp) async {
    final limpio = cp.trim();
    if (limpio.length != 5 || !RegExp(r'^\d{5}$').hasMatch(limpio)) {
      if (_cpEncontrado || _mensajeCp != null) {
        setState(() {
          _cpEncontrado = false;
          _mensajeCp = null;
          _ultimoCpBuscado = '';
          _coloniasDisponibles = [];
          _coloniaSeleccionada = null;
        });
      }
      return;
    }

    if (limpio == _ultimoCpBuscado) return;
    _ultimoCpBuscado = limpio;

    setState(() {
      _buscandoCp = true;
      _mensajeCp = null;
      _cpEncontrado = false;
    });

    final auth = Provider.of<AuthProvider>(context, listen: false);
    Map<String, dynamic>? datos = await auth.consultarCodigoPostal(limpio);

    // Respaldo directo a Zippopotam si el backend aún está desplegando
    datos ??= await _consultarZippopotamDirecto(limpio);

    if (!mounted) return;

    if (datos != null) {
      final estado = (datos['estado'] ?? '').toString();
      final municipio = (datos['municipio'] ?? '').toString();
      final pais = (datos['pais'] ?? 'Mexico').toString();
      final rawCols = datos['colonias'];
      final cols = rawCols is List
          ? rawCols.map((e) => e.toString()).where((e) => e.isNotEmpty).toList()
          : <String>[];

      setState(() {
        _buscandoCp = false;
        _cpEncontrado = true;
        _estadoCtrl.text = estado;
        _municipioCtrl.text = municipio.isNotEmpty ? municipio : estado;
        _paisCtrl.text = pais.isNotEmpty ? pais : 'Mexico';
        _coloniasDisponibles = cols;
        if (cols.isNotEmpty) {
          _coloniaSeleccionada = cols.first;
          _coloniaCtrl.text = cols.first;
          _coloniaManual = false;
          _mensajeCp =
              'Dirección autocompletada: ${cols.length} colonia(s) encontradas en $_estadoCtrlText.';
        } else {
          _coloniaSeleccionada = null;
          _coloniaManual = true;
          _mensajeCp = 'Estado y municipio completados; escribe tu colonia.';
        }
      });
      _step2Key.currentState?.validate();
    } else {
      setState(() {
        _buscandoCp = false;
        _cpEncontrado = false;
        _coloniasDisponibles = [];
        _coloniaSeleccionada = null;
        _coloniaManual = true;
        _mensajeCp = 'No encontramos ese CP en el catálogo; puedes llenar los campos manualmente.';
      });
    }
  }

  String get _estadoCtrlText =>
      _estadoCtrl.text.isNotEmpty ? _estadoCtrl.text : 'México';

  Future<Map<String, dynamic>?> _consultarZippopotamDirecto(String cp) async {
    try {
      final resp = await http
          .get(Uri.parse('https://api.zippopotam.us/mx/$cp'))
          .timeout(const Duration(seconds: 6));
      if (resp.statusCode != 200) return null;
      final body = jsonDecode(resp.body) as Map<String, dynamic>;
      final places = body['places'] as List<dynamic>? ?? [];
      if (places.isEmpty) return null;
      String estado = '';
      final colonias = <String>{};
      for (final p in places) {
        if (p is Map<String, dynamic>) {
          if (estado.isEmpty) {
            estado = (p['state'] ?? '').toString();
            if (estado.toLowerCase() == 'distrito federal') {
              estado = 'Ciudad de Mexico';
            }
          }
          final c = (p['place name'] ?? '').toString().trim();
          if (c.isNotEmpty) colonias.add(c);
        }
      }
      return {
        'codigoPostal': cp,
        'pais': 'Mexico',
        'estado': estado,
        'municipio': estado,
        'colonias': colonias.toList(),
      };
    } catch (_) {
      return null;
    }
  }

  @override
  void dispose() {
    _nombreCtrl.dispose();
    _segundoNombreCtrl.dispose();
    _apellidoPaternoCtrl.dispose();
    _apellidoMaternoCtrl.dispose();
    _fechaNacCtrl.dispose();
    _curpCtrl.dispose();
    _rfcCtrl.dispose();
    _calleCtrl.dispose();
    _numExtCtrl.dispose();
    _numIntCtrl.dispose();
    _coloniaCtrl.dispose();
    _municipioCtrl.dispose();
    _estadoCtrl.dispose();
    _cpCtrl.dispose();
    _paisCtrl.dispose();
    _empresaCtrl.dispose();
    _ingresoCtrl.dispose();
    _correoCtrl.dispose();
    _telefonoMovilCtrl.dispose();
    _telefonoAltCtrl.dispose();
    _passwordCtrl.dispose();
    _confirmPasswordCtrl.dispose();
    super.dispose();
  }

  Future<void> _seleccionarFecha() async {
    final hoy = DateTime.now();
    final fechaMax = DateTime(hoy.year - 18, hoy.month, hoy.day);
    final inicial = _fechaNacimiento ?? DateTime(hoy.year - 22, 1, 15);

    final seleccionada = await showDatePicker(
      context: context,
      initialDate: inicial.isAfter(fechaMax) ? fechaMax : inicial,
      firstDate: DateTime(1920),
      lastDate: fechaMax,
      helpText: 'Fecha de nacimiento (mayor de 18 años)',
    );

    if (seleccionada != null) {
      setState(() {
        _fechaNacimiento = seleccionada;
        final y = seleccionada.year.toString().padLeft(4, '0');
        final m = seleccionada.month.toString().padLeft(2, '0');
        final d = seleccionada.day.toString().padLeft(2, '0');
        _fechaNacCtrl.text = '$y/$m/$d';
      });
      _step1Key.currentState?.validate();
    }
  }

  void _siguientePaso() {
    if (_pasoActual == 0 && _step1Key.currentState!.validate()) {
      setState(() => _pasoActual = 1);
    } else if (_pasoActual == 1 && _step2Key.currentState!.validate()) {
      setState(() => _pasoActual = 2);
    }
  }

  Future<void> _crearCuenta() async {
    if (!_step3Key.currentState!.validate()) return;

    setState(() => _isLoading = true);
    final auth = Provider.of<AuthProvider>(context, listen: false);

    final domicilio = <String, dynamic>{
      'calle': FormValidators.limpiarEspacios(_calleCtrl.text),
      'numeroExterior': int.parse(_numExtCtrl.text.trim()),
      'colonia': FormValidators.limpiarEspacios(_coloniaCtrl.text),
      'municipio': FormValidators.limpiarEspacios(_municipioCtrl.text),
      'estado': FormValidators.limpiarEspacios(_estadoCtrl.text),
      'codigoPostal': int.parse(_cpCtrl.text.trim()),
      'pais': FormValidators.limpiarEspacios(_paisCtrl.text),
    };
    if (_numIntCtrl.text.trim().isNotEmpty) {
      domicilio['numeroInterior'] = int.parse(_numIntCtrl.text.trim());
    }

    final payload = <String, dynamic>{
      'nombre': _nombreCtrl.text.trim(),
      'apellidoPaterno': FormValidators.limpiarEspacios(_apellidoPaternoCtrl.text),
      'apellidoMaterno': FormValidators.limpiarEspacios(_apellidoMaternoCtrl.text),
      'fechaNacimiento': _fechaNacCtrl.text.trim(),
      'curp': _curpCtrl.text.trim().toUpperCase(),
      'rfc': _rfcCtrl.text.trim().toUpperCase(),
      'sexo': _sexo,
      'nacionalidad': _nacionalidad,
      'estadoCivil': _estadoCivil,
      'correo': _correoCtrl.text.trim().toLowerCase(),
      'telefonoMovil': int.parse(_telefonoMovilCtrl.text.trim()),
      'domicilio': domicilio,
      'ocupacion': _profesionSeleccionada,
      'empresa': FormValidators.limpiarEspacios(_empresaCtrl.text),
      'ingresoMensual': double.parse(_ingresoCtrl.text.trim()).toStringAsFixed(2),
    };

    if (_segundoNombreCtrl.text.trim().isNotEmpty) {
      payload['segundoNombre'] = _segundoNombreCtrl.text.trim();
    }
    if (_telefonoAltCtrl.text.trim().isNotEmpty) {
      payload['telefonoAlternativo'] = int.parse(_telefonoAltCtrl.text.trim());
    }

    final correo = _correoCtrl.text.trim().toLowerCase();
    final password = _passwordCtrl.text;

    final res = await auth.crearCuentaCompleta(
      clientePayload: payload,
      password: password,
    );

    if (!mounted) return;
    setState(() => _isLoading = false);

    if (res.exito) {
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (ctx) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: const Row(
            children: [
              Icon(Icons.check_circle_rounded, color: AppColors.success, size: 28),
              SizedBox(width: 10),
              Expanded(
                child: Text(
                  '¡Cuenta Creada!',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
                ),
              ),
            ],
          ),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Tu cuenta digital GestoPago y tus credenciales de acceso están listas.',
                style: TextStyle(fontSize: 13, color: AppColors.textSecondary, height: 1.4),
              ),
              if (res.numeroCuenta != null && res.numeroCuenta!.isNotEmpty) ...[
                const SizedBox(height: 14),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.accentSoft,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'NÚMERO DE CUENTA ASIGNADO',
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.w700,
                          color: AppColors.textSecondary,
                          letterSpacing: 0.8,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        res.numeroCuenta!,
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w900,
                          color: AppColors.accent,
                          letterSpacing: 1.5,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ],
          ),
          actions: [
            ElevatedButton(
              onPressed: () async {
                Navigator.pop(ctx);
                final ok = await auth.login(correo, password);
                if (!mounted) return;
                if (ok) {
                  Navigator.pushAndRemoveUntil(
                    context,
                    MaterialPageRoute(builder: (_) => const HomeScreen()),
                    (route) => false,
                  );
                } else {
                  Navigator.pop(context);
                }
              },
              child: const Text('Entrar a mi Cuenta'),
            ),
          ],
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(res.error ?? 'Error al crear cuenta'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
          duration: const Duration(seconds: 5),
        ),
      );
    }
  }

  Widget _buildIndicadorPasos() {
    const titulos = ['Personal', 'Domicilio y Laboral', 'Acceso'];
    return Row(
      children: List.generate(3, (i) {
        final activo = i <= _pasoActual;
        return Expanded(
          child: Padding(
            padding: EdgeInsets.only(right: i < 2 ? 8 : 0),
            child: Column(
              children: [
                Container(
                  height: 5,
                  decoration: BoxDecoration(
                    color: activo ? AppColors.accent : AppColors.border,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  '${i + 1}. ${titulos[i]}',
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: activo ? FontWeight.w800 : FontWeight.w500,
                    color: activo ? AppColors.accent : AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        );
      }),
    );
  }

  Widget _buildDropdown<T>({
    required String label,
    required T value,
    required List<DropdownMenuItem<T>> items,
    required ValueChanged<T?> onChanged,
    String? helperText,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w600,
            color: AppColors.textPrimary,
          ),
        ),
        const SizedBox(height: 8),
        DropdownButtonFormField<T>(
          key: ValueKey('$label-$value'),
          initialValue: value,
          isExpanded: true,
          items: items,
          onChanged: onChanged,
          decoration: InputDecoration(
            filled: true,
            fillColor: const Color(0xFFFAF9FF),
            helperText: helperText,
            helperMaxLines: 2,
            contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(15),
              borderSide: const BorderSide(color: Color(0xFFE7E1FF)),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(15),
              borderSide: const BorderSide(color: Color(0xFFE7E1FF)),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildPaso1() {
    return Form(
      key: _step1Key,
      autovalidateMode: AutovalidateMode.onUserInteraction,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CustomTextField(
            label: 'Primer nombre *',
            hint: 'Ej. Carol',
            helperText: 'Mínimo 2 letras, un solo nombre (sin espacios)',
            controller: _nombreCtrl,
            prefixIcon: Icons.person_outline_rounded,
            inputFormatters: [
              FilteringTextInputFormatter.deny(RegExp(r'\s')),
              LengthLimitingTextInputFormatter(50),
            ],
            validator: FormValidators.primerNombre,
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Segundo nombre (opcional)',
            hint: 'Ej. Guadalupe',
            helperText: 'Opcional; máximo 2 nombres en total',
            controller: _segundoNombreCtrl,
            prefixIcon: Icons.person_outline_rounded,
            inputFormatters: [
              FilteringTextInputFormatter.deny(RegExp(r'\s')),
              LengthLimitingTextInputFormatter(50),
            ],
            validator: FormValidators.segundoNombre,
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Apellido paterno *',
            hint: 'Ej. Rios',
            helperText: 'Mínimo 3 caracteres',
            controller: _apellidoPaternoCtrl,
            prefixIcon: Icons.badge_outlined,
            validator: (v) => FormValidators.apellido(v, 'tu apellido paterno'),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Apellido materno *',
            hint: 'Ej. Lopez',
            helperText: 'Mínimo 3 caracteres',
            controller: _apellidoMaternoCtrl,
            prefixIcon: Icons.badge_outlined,
            validator: (v) => FormValidators.apellido(v, 'tu apellido materno'),
          ),
          const SizedBox(height: 14),
          GestureDetector(
            onTap: _seleccionarFecha,
            child: AbsorbPointer(
              child: CustomTextField(
                label: 'Fecha de nacimiento *',
                hint: 'yyyy/MM/dd',
                helperText: 'Debes ser mayor de 18 años',
                controller: _fechaNacCtrl,
                readOnly: true,
                prefixIcon: Icons.calendar_today_rounded,
                validator: (v) =>
                    (v == null || v.trim().isEmpty) ? 'Selecciona tu fecha de nacimiento' : null,
              ),
            ),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'CURP (18 caracteres) *',
            hint: 'Ej. PELJ990131HGTRPN09',
            controller: _curpCtrl,
            prefixIcon: Icons.fingerprint_rounded,
            inputFormatters: [
              LengthLimitingTextInputFormatter(18),
              FilteringTextInputFormatter.allow(RegExp(r'[A-Za-z0-9]')),
              TextInputFormatter.withFunction((oldVal, newVal) =>
                  newVal.copyWith(text: newVal.text.toUpperCase())),
            ],
            validator: FormValidators.curp,
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'RFC (12 o 13 caracteres) *',
            hint: 'Ej. PELJ990131AB1',
            controller: _rfcCtrl,
            prefixIcon: Icons.receipt_long_outlined,
            inputFormatters: [
              LengthLimitingTextInputFormatter(13),
              FilteringTextInputFormatter.allow(RegExp(r'[A-Za-zÑñ&0-9]')),
              TextInputFormatter.withFunction((oldVal, newVal) =>
                  newVal.copyWith(text: newVal.text.toUpperCase())),
            ],
            validator: FormValidators.rfc,
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                child: _buildDropdown<String>(
                  label: 'Sexo',
                  value: _sexo,
                  items: const [
                    DropdownMenuItem(value: 'M', child: Text('Mujer')),
                    DropdownMenuItem(value: 'H', child: Text('Hombre')),
                    DropdownMenuItem(value: 'Otros', child: Text('Otros')),
                  ],
                  onChanged: (v) => setState(() => _sexo = v ?? 'M'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _buildDropdown<String>(
                  label: 'Estado civil',
                  value: _estadoCivil,
                  items: const [
                    DropdownMenuItem(value: 'SOLTERO', child: Text('Soltero/a')),
                    DropdownMenuItem(value: 'CASADO', child: Text('Casado/a')),
                    DropdownMenuItem(value: 'UNION_LIBRE', child: Text('Unión libre')),
                    DropdownMenuItem(value: 'DIVORCIADO', child: Text('Divorciado/a')),
                    DropdownMenuItem(value: 'VIUDO', child: Text('Viudo/a')),
                    DropdownMenuItem(value: 'SEPARADO', child: Text('Separado/a')),
                  ],
                  onChanged: (v) => setState(() => _estadoCivil = v ?? 'SOLTERO'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          _buildDropdown<String>(
            label: 'Nacionalidad',
            value: _nacionalidad,
            items: _nacionalidades
                .map((n) => DropdownMenuItem<String>(
                      value: n['codigo']!,
                      child: Text(
                        '${n['nombre']} (${n['codigo']})',
                        overflow: TextOverflow.ellipsis,
                      ),
                    ))
                .toList(),
            onChanged: (v) => setState(() => _nacionalidad = v ?? 'MEX'),
          ),
        ],
      ),
    );
  }

  Widget _buildPaso2() {
    return Form(
      key: _step2Key,
      autovalidateMode: AutovalidateMode.onUserInteraction,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // 1. Código Postal primero para autocompletar con API
          CustomTextField(
            label: 'Código Postal (5 dígitos) *',
            hint: 'Ej. 37800',
            helperText: 'Al escribir 5 dígitos se llenan estado, municipio y colonias automáticamente',
            controller: _cpCtrl,
            keyboardType: TextInputType.number,
            prefixIcon: Icons.markunread_mailbox_outlined,
            suffixIcon: _buscandoCp
                ? const Padding(
                    padding: EdgeInsets.all(12),
                    child: SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    ),
                  )
                : (_cpEncontrado
                    ? const Icon(Icons.check_circle_rounded, color: AppColors.success)
                    : null),
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
              LengthLimitingTextInputFormatter(5),
            ],
            onChanged: _buscarDireccionPorCp,
            validator: FormValidators.codigoPostal,
          ),

          if (_mensajeCp != null) ...[
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: _cpEncontrado ? AppColors.successBg : AppColors.warningBg,
                borderRadius: BorderRadius.circular(10),
              ),
              child: Text(
                _mensajeCp!,
                style: TextStyle(
                  fontSize: 11.5,
                  fontWeight: FontWeight.w600,
                  color: _cpEncontrado ? AppColors.success : AppColors.warning,
                ),
              ),
            ),
          ],

          const SizedBox(height: 14),

          // Colonia: desplegable si la API devolvió colonias, o texto manual
          if (_coloniasDisponibles.isNotEmpty && !_coloniaManual) ...[
            _buildDropdown<String>(
              label: 'Colonia *',
              value: _coloniaSeleccionada ?? _coloniasDisponibles.first,
              items: [
                ..._coloniasDisponibles.map(
                  (c) => DropdownMenuItem(
                    value: c,
                    child: Text(c, overflow: TextOverflow.ellipsis),
                  ),
                ),
                const DropdownMenuItem(
                  value: '__OTRA__',
                  child: Text('Otra colonia (escribir manualmente)...'),
                ),
              ],
              onChanged: (v) {
                if (v == '__OTRA__') {
                  setState(() {
                    _coloniaManual = true;
                    _coloniaCtrl.clear();
                  });
                } else if (v != null) {
                  setState(() {
                    _coloniaSeleccionada = v;
                    _coloniaCtrl.text = v;
                  });
                }
              },
            ),
          ] else ...[
            CustomTextField(
              label: 'Colonia *',
              hint: 'Ej. Centro',
              controller: _coloniaCtrl,
              prefixIcon: Icons.location_city_rounded,
              validator: (v) => FormValidators.textoDireccion(v, 'tu colonia'),
            ),
          ],

          const SizedBox(height: 14),
          CustomTextField(
            label: 'Municipio *',
            hint: 'Ej. Dolores Hidalgo',
            controller: _municipioCtrl,
            prefixIcon: Icons.map_outlined,
            validator: (v) => FormValidators.soloLetras(v, 'tu municipio'),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                flex: 3,
                child: CustomTextField(
                  label: 'Estado *',
                  hint: 'Ej. Guanajuato',
                  controller: _estadoCtrl,
                  validator: (v) => FormValidators.soloLetras(v, 'tu estado'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                flex: 2,
                child: CustomTextField(
                  label: 'País *',
                  hint: 'Mexico',
                  controller: _paisCtrl,
                  validator: (v) => FormValidators.soloLetras(v, 'tu país'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Calle *',
            hint: 'Ej. Hidalgo',
            controller: _calleCtrl,
            prefixIcon: Icons.home_outlined,
            validator: (v) => FormValidators.textoDireccion(v, 'tu calle'),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                child: CustomTextField(
                  label: 'Núm. exterior *',
                  hint: 'Ej. 120',
                  controller: _numExtCtrl,
                  keyboardType: TextInputType.number,
                  inputFormatters: [
                    FilteringTextInputFormatter.digitsOnly,
                    LengthLimitingTextInputFormatter(6),
                  ],
                  validator: (v) => FormValidators.numeroDomicilio(v),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: CustomTextField(
                  label: 'Núm. interior (opc.)',
                  hint: 'Ej. 4',
                  controller: _numIntCtrl,
                  keyboardType: TextInputType.number,
                  inputFormatters: [
                    FilteringTextInputFormatter.digitsOnly,
                    LengthLimitingTextInputFormatter(6),
                  ],
                  validator: (v) => FormValidators.numeroDomicilio(v, opcional: true),
                ),
              ),
            ],
          ),

          const Divider(height: 28),

          // Catálogo Oficial de Profesiones (INEGI SINCO)
          _buildDropdown<String>(
            label: 'Profesión / Ocupación (Catálogo SINCO) *',
            value: _profesionSeleccionada,
            items: _profesiones
                .map((p) => DropdownMenuItem<String>(
                      value: p['nombre'].toString(),
                      child: Text(
                        '${p['nombre']} (${p['categoria']})',
                        overflow: TextOverflow.ellipsis,
                      ),
                    ))
                .toList(),
            onChanged: (v) {
              if (v == null) return;
              final prof = _profesiones.firstWhere(
                (p) => p['nombre'] == v,
                orElse: () => _profesiones.first,
              );
              final tabSugerido = (prof['tabuladorSugerido'] ?? 'TAB_1').toString();
              setState(() {
                _profesionSeleccionada = v;
                if (_tabuladores.any((t) => t['id'] == tabSugerido)) {
                  _tabuladorId = tabSugerido;
                }
                // Si el ingreso escrito supera el nuevo máximo, limpiarlo para evitar inconsistencia
                final actual = double.tryParse(_ingresoCtrl.text.trim());
                if (actual != null && actual > _sueldoMax) {
                  _ingresoCtrl.clear();
                }
              });
              _step2Key.currentState?.validate();
            },
          ),

          const SizedBox(height: 14),

          // Tabulador Salarial
          _buildDropdown<String>(
            label: 'Tabulador de Sueldo Mensual *',
            value: _tabuladorId,
            helperText:
                'Tu ingreso mensual deberá estar dentro de este rango y no podrás exceder el límite.',
            items: _tabuladores
                .map((t) => DropdownMenuItem<String>(
                      value: t['id'].toString(),
                      child: Text(
                        t['etiqueta'].toString(),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ))
                .toList(),
            onChanged: (v) {
              if (v == null) return;
              setState(() {
                _tabuladorId = v;
                final actual = double.tryParse(_ingresoCtrl.text.trim());
                if (actual != null && actual > _sueldoMax) {
                  _ingresoCtrl.clear();
                }
              });
              _step2Key.currentState?.validate();
            },
          ),

          const SizedBox(height: 14),

          // Ingreso Mensual bloqueado al máximo del tabulador seleccionado
          CustomTextField(
            label: 'Ingreso mensual exacto (\$ MXN) *',
            hint: 'Entre \$${_sueldoMin.toStringAsFixed(0)} y \$${_sueldoMax.toStringAsFixed(0)}',
            helperText:
                'Límite permitido por tu tabulador: \$${_sueldoMin.toStringAsFixed(0)} a \$${_sueldoMax.toStringAsFixed(0)} MXN',
            controller: _ingresoCtrl,
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
            prefixIcon: Icons.attach_money_rounded,
            inputFormatters: [
              MaxValueInputFormatter(_sueldoMax),
            ],
            validator: (v) => FormValidators.ingresoEnRango(v, _sueldoMin, _sueldoMax),
          ),

          const SizedBox(height: 14),
          CustomTextField(
            label: 'Empresa o institución *',
            hint: 'Ej. Universidad o Independiente',
            helperText: 'Mínimo 2 letras, sin números ni símbolos',
            controller: _empresaCtrl,
            prefixIcon: Icons.business_outlined,
            validator: (v) => FormValidators.soloLetras(v, 'tu empresa o institución'),
          ),
        ],
      ),
    );
  }

  Widget _buildPaso3() {
    return Form(
      key: _step3Key,
      autovalidateMode: AutovalidateMode.onUserInteraction,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CustomTextField(
            label: 'Correo electrónico *',
            hint: 'tu.correo@gmail.com',
            helperText: 'Debe tener un dominio real válido (ej. @gmail.com, @outlook.com)',
            controller: _correoCtrl,
            keyboardType: TextInputType.emailAddress,
            prefixIcon: Icons.alternate_email_rounded,
            validator: FormValidators.correo,
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Teléfono móvil (10 dígitos) *',
            hint: 'Ej. 4181234567',
            helperText: '10 dígitos válidos en México (sin 0 ni 1 al inicio)',
            controller: _telefonoMovilCtrl,
            keyboardType: TextInputType.phone,
            prefixIcon: Icons.phone_android_rounded,
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
              LengthLimitingTextInputFormatter(10),
            ],
            validator: (v) => FormValidators.telefonoMexico(v),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Teléfono alternativo (opcional)',
            hint: '10 dígitos',
            controller: _telefonoAltCtrl,
            keyboardType: TextInputType.phone,
            prefixIcon: Icons.phone_outlined,
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
              LengthLimitingTextInputFormatter(10),
            ],
            validator: (v) => FormValidators.telefonoMexico(v, opcional: true),
          ),
          const Divider(height: 28),
          CustomTextField(
            label: 'Contraseña del portal *',
            hint: 'Mínimo 8 caracteres',
            controller: _passwordCtrl,
            isPassword: true,
            prefixIcon: Icons.lock_outline_rounded,
            onChanged: (_) {
              if (_confirmPasswordCtrl.text.isNotEmpty) {
                _step3Key.currentState?.validate();
              }
            },
            validator: FormValidators.password,
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Confirmar contraseña *',
            hint: 'Repite tu contraseña',
            controller: _confirmPasswordCtrl,
            isPassword: true,
            prefixIcon: Icons.lock_reset_rounded,
            validator: (v) => FormValidators.confirmarPassword(v, _passwordCtrl.text),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Crear Cuenta Nueva'),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 18),
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 460),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Text(
                    'Abre tu cuenta GestoPago',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.w800,
                      color: AppColors.textPrimary,
                      letterSpacing: -0.4,
                    ),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Validamos tus datos al instante para que completes tu registro sin contratiempos.',
                    style: TextStyle(
                      fontSize: 13,
                      color: AppColors.textSecondary,
                      height: 1.35,
                    ),
                  ),
                  const SizedBox(height: 18),
                  _buildIndicadorPasos(),
                  const SizedBox(height: 18),
                  Container(
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: AppColors.border),
                    ),
                    child: _pasoActual == 0
                        ? _buildPaso1()
                        : _pasoActual == 1
                            ? _buildPaso2()
                            : _buildPaso3(),
                  ),
                  const SizedBox(height: 20),
                  Row(
                    children: [
                      if (_pasoActual > 0) ...[
                        Expanded(
                          child: CustomButton(
                            text: 'Atrás',
                            isSecondary: true,
                            onPressed: _isLoading
                                ? null
                                : () => setState(() => _pasoActual--),
                          ),
                        ),
                        const SizedBox(width: 12),
                      ],
                      Expanded(
                        flex: 2,
                        child: CustomButton(
                          text: _pasoActual < 2 ? 'Continuar' : 'Crear mi Cuenta',
                          icon: _pasoActual < 2
                              ? Icons.arrow_forward_rounded
                              : Icons.check_circle_outline_rounded,
                          isLoading: _isLoading,
                          onPressed: _pasoActual < 2 ? _siguientePaso : _crearCuenta,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

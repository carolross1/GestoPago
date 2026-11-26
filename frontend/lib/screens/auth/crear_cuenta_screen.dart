import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
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

  // Paso 2: Domicilio y Datos Laborales
  final _calleCtrl = TextEditingController();
  final _numExtCtrl = TextEditingController();
  final _numIntCtrl = TextEditingController();
  final _coloniaCtrl = TextEditingController();
  final _municipioCtrl = TextEditingController();
  final _estadoCtrl = TextEditingController();
  final _cpCtrl = TextEditingController();
  final _paisCtrl = TextEditingController(text: 'Mexico');
  final _ocupacionCtrl = TextEditingController();
  final _empresaCtrl = TextEditingController();
  final _ingresoCtrl = TextEditingController();

  // Paso 3: Contacto y Acceso
  final _correoCtrl = TextEditingController();
  final _telefonoMovilCtrl = TextEditingController();
  final _telefonoAltCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _confirmPasswordCtrl = TextEditingController();

  static final RegExp _soloLetrasRegex =
      RegExp(r'^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ]+( [A-Za-zÁÉÍÓÚÜáéíóúüÑñ]+)*$');
  static final RegExp _curpRegex =
      RegExp(r'^[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d$');
  static final RegExp _rfcRegex =
      RegExp(r'^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{2,3}$');

  @override
  void initState() {
    super.initState();
    _cargarNacionalidades();
  }

  Future<void> _cargarNacionalidades() async {
    final auth = Provider.of<AuthProvider>(context, listen: false);
    final lista = await auth.obtenerNacionalidades();
    if (!mounted || lista.isEmpty) return;
    setState(() {
      _nacionalidades = lista;
      if (!_nacionalidades.any((n) => n['codigo'] == _nacionalidad)) {
        _nacionalidad = _nacionalidades.first['codigo']!;
      }
    });
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
    _ocupacionCtrl.dispose();
    _empresaCtrl.dispose();
    _ingresoCtrl.dispose();
    _correoCtrl.dispose();
    _telefonoMovilCtrl.dispose();
    _telefonoAltCtrl.dispose();
    _passwordCtrl.dispose();
    _confirmPasswordCtrl.dispose();
    super.dispose();
  }

  String _limpiarEspacios(String valor) =>
      valor.trim().replaceAll(RegExp(r'\s+'), ' ');

  String? _validarSoloLetras(String? val, String etiqueta, {bool opcional = false, int minLen = 1}) {
    final limpio = _limpiarEspacios(val ?? '');
    if (limpio.isEmpty) {
      return opcional ? null : 'Ingresa $etiqueta';
    }
    if (limpio.length < minLen) {
      return 'Debe tener al menos $minLen caracteres';
    }
    if (!_soloLetrasRegex.hasMatch(limpio)) {
      return 'Solo letras y espacios (sin números ni símbolos)';
    }
    return null;
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
      'calle': _limpiarEspacios(_calleCtrl.text),
      'numeroExterior': int.parse(_numExtCtrl.text.trim()),
      'colonia': _limpiarEspacios(_coloniaCtrl.text),
      'municipio': _limpiarEspacios(_municipioCtrl.text),
      'estado': _limpiarEspacios(_estadoCtrl.text),
      'codigoPostal': int.parse(_cpCtrl.text.trim()),
      'pais': _limpiarEspacios(_paisCtrl.text),
    };
    if (_numIntCtrl.text.trim().isNotEmpty) {
      domicilio['numeroInterior'] = int.parse(_numIntCtrl.text.trim());
    }

    final payload = <String, dynamic>{
      'nombre': _limpiarEspacios(_nombreCtrl.text),
      'apellidoPaterno': _limpiarEspacios(_apellidoPaternoCtrl.text),
      'apellidoMaterno': _limpiarEspacios(_apellidoMaternoCtrl.text),
      'fechaNacimiento': _fechaNacCtrl.text.trim(),
      'curp': _curpCtrl.text.trim().toUpperCase(),
      'rfc': _rfcCtrl.text.trim().toUpperCase(),
      'sexo': _sexo,
      'nacionalidad': _nacionalidad,
      'estadoCivil': _estadoCivil,
      'correo': _correoCtrl.text.trim().toLowerCase(),
      'telefonoMovil': int.parse(_telefonoMovilCtrl.text.trim()),
      'domicilio': domicilio,
      'ocupacion': _limpiarEspacios(_ocupacionCtrl.text),
      'empresa': _limpiarEspacios(_empresaCtrl.text),
      'ingresoMensual': double.parse(_ingresoCtrl.text.trim()).toStringAsFixed(2),
    };

    if (_limpiarEspacios(_segundoNombreCtrl.text).isNotEmpty) {
      payload['segundoNombre'] = _limpiarEspacios(_segundoNombreCtrl.text);
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
    const titulos = ['Personal', 'Domicilio', 'Acceso'];
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
          initialValue: value,
          isExpanded: true,
          items: items,
          onChanged: onChanged,
          decoration: InputDecoration(
            filled: true,
            fillColor: AppColors.surfaceElevated,
            contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(14),
              borderSide: const BorderSide(color: AppColors.border),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(14),
              borderSide: const BorderSide(color: AppColors.border),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildPaso1() {
    return Form(
      key: _step1Key,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CustomTextField(
            label: 'Nombre',
            hint: 'Ej. Carol',
            controller: _nombreCtrl,
            prefixIcon: Icons.person_outline_rounded,
            validator: (v) => _validarSoloLetras(v, 'tu nombre', minLen: 2),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Segundo nombre (opcional)',
            hint: 'Ej. Guadalupe',
            controller: _segundoNombreCtrl,
            prefixIcon: Icons.person_outline_rounded,
            validator: (v) => _validarSoloLetras(v, 'tu segundo nombre', opcional: true),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Apellido paterno',
            hint: 'Ej. Rios',
            controller: _apellidoPaternoCtrl,
            prefixIcon: Icons.badge_outlined,
            validator: (v) => _validarSoloLetras(v, 'tu apellido paterno', minLen: 2),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Apellido materno',
            hint: 'Ej. Lopez',
            controller: _apellidoMaternoCtrl,
            prefixIcon: Icons.badge_outlined,
            validator: (v) => _validarSoloLetras(v, 'tu apellido materno', minLen: 2),
          ),
          const SizedBox(height: 14),
          GestureDetector(
            onTap: _seleccionarFecha,
            child: AbsorbPointer(
              child: CustomTextField(
                label: 'Fecha de nacimiento',
                hint: 'yyyy/MM/dd',
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
            label: 'CURP (18 caracteres)',
            hint: 'Ej. PELJ990131HGTRPN09',
            controller: _curpCtrl,
            prefixIcon: Icons.fingerprint_rounded,
            inputFormatters: [
              LengthLimitingTextInputFormatter(18),
              FilteringTextInputFormatter.allow(RegExp(r'[A-Za-z0-9]')),
              TextInputFormatter.withFunction((oldVal, newVal) =>
                  newVal.copyWith(text: newVal.text.toUpperCase())),
            ],
            validator: (v) {
              final curp = (v ?? '').trim().toUpperCase();
              if (!_curpRegex.hasMatch(curp)) {
                return 'CURP inválida (18 caracteres, formato oficial)';
              }
              return null;
            },
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'RFC (12 o 13 caracteres)',
            hint: 'Ej. PELJ990131AB1',
            controller: _rfcCtrl,
            prefixIcon: Icons.receipt_long_outlined,
            inputFormatters: [
              LengthLimitingTextInputFormatter(13),
              FilteringTextInputFormatter.allow(RegExp(r'[A-Za-zÑñ&0-9]')),
              TextInputFormatter.withFunction((oldVal, newVal) =>
                  newVal.copyWith(text: newVal.text.toUpperCase())),
            ],
            validator: (v) {
              final rfc = (v ?? '').trim().toUpperCase();
              if (!_rfcRegex.hasMatch(rfc)) {
                return 'RFC inválido (12 o 13 caracteres, formato oficial)';
              }
              return null;
            },
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
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CustomTextField(
            label: 'Calle',
            hint: 'Ej. Hidalgo',
            controller: _calleCtrl,
            prefixIcon: Icons.home_outlined,
            validator: (v) => _validarSoloLetras(v, 'tu calle'),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                child: CustomTextField(
                  label: 'Núm. exterior',
                  hint: 'Ej. 120',
                  controller: _numExtCtrl,
                  keyboardType: TextInputType.number,
                  inputFormatters: [
                    FilteringTextInputFormatter.digitsOnly,
                    LengthLimitingTextInputFormatter(6),
                  ],
                  validator: (v) {
                    final n = int.tryParse((v ?? '').trim());
                    if (n == null || n < 1 || n > 999999) {
                      return 'Número inválido';
                    }
                    return null;
                  },
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
                  validator: (v) {
                    if (v == null || v.trim().isEmpty) return null;
                    final n = int.tryParse(v.trim());
                    if (n == null || n < 1 || n > 999999) {
                      return 'Número inválido';
                    }
                    return null;
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Colonia',
            hint: 'Ej. Centro',
            controller: _coloniaCtrl,
            prefixIcon: Icons.location_city_rounded,
            validator: (v) => _validarSoloLetras(v, 'tu colonia'),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Municipio',
            hint: 'Ej. Dolores Hidalgo',
            controller: _municipioCtrl,
            prefixIcon: Icons.map_outlined,
            validator: (v) => _validarSoloLetras(v, 'tu municipio'),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                flex: 3,
                child: CustomTextField(
                  label: 'Estado',
                  hint: 'Ej. Guanajuato',
                  controller: _estadoCtrl,
                  validator: (v) => _validarSoloLetras(v, 'tu estado'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                flex: 2,
                child: CustomTextField(
                  label: 'Código Postal',
                  hint: '37800',
                  controller: _cpCtrl,
                  keyboardType: TextInputType.number,
                  inputFormatters: [
                    FilteringTextInputFormatter.digitsOnly,
                    LengthLimitingTextInputFormatter(5),
                  ],
                  validator: (v) {
                    final cp = int.tryParse((v ?? '').trim());
                    if (cp == null || cp < 1000 || cp > 99999) {
                      return 'CP de 5 dígitos';
                    }
                    return null;
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'País',
            hint: 'Mexico',
            controller: _paisCtrl,
            prefixIcon: Icons.public_rounded,
            validator: (v) => _validarSoloLetras(v, 'tu país'),
          ),
          const Divider(height: 28),
          CustomTextField(
            label: 'Ocupación',
            hint: 'Ej. Estudiante o Ingeniero',
            controller: _ocupacionCtrl,
            prefixIcon: Icons.work_outline_rounded,
            validator: (v) => _validarSoloLetras(v, 'tu ocupación'),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Empresa o institución',
            hint: 'Ej. Universidad o Independiente',
            controller: _empresaCtrl,
            prefixIcon: Icons.business_outlined,
            validator: (v) => _validarSoloLetras(v, 'tu empresa'),
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Ingreso mensual (\$ MXN)',
            hint: 'Ej. 15000.00',
            controller: _ingresoCtrl,
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
            prefixIcon: Icons.attach_money_rounded,
            inputFormatters: [
              FilteringTextInputFormatter.allow(RegExp(r'^\d+\.?\d{0,2}')),
            ],
            validator: (v) {
              final val = double.tryParse((v ?? '').trim());
              if (val == null || val <= 0) {
                return 'Ingresa un monto mayor a cero';
              }
              return null;
            },
          ),
        ],
      ),
    );
  }

  Widget _buildPaso3() {
    return Form(
      key: _step3Key,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CustomTextField(
            label: 'Correo electrónico',
            hint: 'tu.correo@ejemplo.com',
            controller: _correoCtrl,
            keyboardType: TextInputType.emailAddress,
            prefixIcon: Icons.mail_outline_rounded,
            validator: (v) {
              final correo = (v ?? '').trim();
              if (correo.isEmpty || !correo.contains('@') || !correo.contains('.')) {
                return 'Ingresa un correo electrónico válido';
              }
              return null;
            },
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Teléfono móvil (10 dígitos)',
            hint: 'Ej. 4181234567',
            controller: _telefonoMovilCtrl,
            keyboardType: TextInputType.phone,
            prefixIcon: Icons.phone_android_rounded,
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
              LengthLimitingTextInputFormatter(10),
            ],
            validator: (v) {
              final t = (v ?? '').trim();
              if (t.length != 10 || t.startsWith('0')) {
                return 'Ingresa un número de 10 dígitos válido';
              }
              return null;
            },
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
            validator: (v) {
              final t = (v ?? '').trim();
              if (t.isEmpty) return null;
              if (t.length != 10 || t.startsWith('0')) {
                return 'Debe tener 10 dígitos válidos';
              }
              return null;
            },
          ),
          const Divider(height: 28),
          CustomTextField(
            label: 'Contraseña del portal',
            hint: 'Mínimo 8 caracteres',
            controller: _passwordCtrl,
            isPassword: true,
            prefixIcon: Icons.lock_outline_rounded,
            validator: (v) {
              if (v == null || v.length < 8) {
                return 'La contraseña debe tener al menos 8 caracteres';
              }
              return null;
            },
          ),
          const SizedBox(height: 14),
          CustomTextField(
            label: 'Confirmar contraseña',
            hint: 'Repite tu contraseña',
            controller: _confirmPasswordCtrl,
            isPassword: true,
            prefixIcon: Icons.lock_reset_rounded,
            validator: (v) {
              if (v != _passwordCtrl.text) {
                return 'Las contraseñas no coinciden';
              }
              return null;
            },
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
                    'Completa estos 3 pasos para generar tu cuenta bancaria digital y tu acceso inmediato.',
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

import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import '../../core/constants/api_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/currency_formatter.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';

/// Módulo Administrativo independiente y protegido por credenciales + JWT de rol ADMIN.
/// No utiliza ni interfiere con la sesión del cliente.
class AdminAbonosScreen extends StatefulWidget {
  const AdminAbonosScreen({super.key});

  @override
  State<AdminAbonosScreen> createState() => _AdminAbonosScreenState();
}

class _AdminAbonosScreenState extends State<AdminAbonosScreen> {
  // Login Admin
  final _loginFormKey = GlobalKey<FormState>();
  final _adminEmailCtrl = TextEditingController();
  final _adminPassCtrl = TextEditingController();
  bool _autenticando = false;
  String? _errorLogin;
  String? _adminToken;
  String? _adminCorreo;

  // Consola de Abonos
  final _abonoFormKey = GlobalKey<FormState>();
  final _cuentaCtrl = TextEditingController();
  final _montoCtrl = TextEditingController();
  final _busquedaCtrl = TextEditingController();
  bool _cargandoCuentas = false;
  bool _enviandoAbono = false;
  String? _errorCuentas;
  List<Map<String, dynamic>> _cuentas = [];

  @override
  void dispose() {
    _adminEmailCtrl.dispose();
    _adminPassCtrl.dispose();
    _cuentaCtrl.dispose();
    _montoCtrl.dispose();
    _busquedaCtrl.dispose();
    super.dispose();
  }

  Future<Map<String, dynamic>> _requestAdmin(
    String method,
    String endpoint, {
    Map<String, dynamic>? body,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$endpoint');
    final headers = <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
      'Accept': 'application/json',
      if (_adminToken != null && _adminToken!.isNotEmpty)
        'Authorization': 'Bearer $_adminToken',
    };

    try {
      final resp = method == 'GET'
          ? await http.get(uri, headers: headers).timeout(const Duration(seconds: 25))
          : await http
              .post(uri, headers: headers, body: body != null ? jsonEncode(body) : null)
              .timeout(const Duration(seconds: 25));

      final decoded = jsonDecode(utf8.decode(resp.bodyBytes));
      final map = decoded is Map<String, dynamic> ? decoded : <String, dynamic>{};

      if ((resp.statusCode == 401 || resp.statusCode == 403) && !endpoint.contains('/login')) {
        if (mounted) {
          setState(() {
            _adminToken = null;
            _adminCorreo = null;
            _errorLogin = map['mensaje']?.toString() ??
                'Tu sesión administrativa expiró. Vuelve a iniciar sesión.';
          });
        }
      }

      return {
        'ok': resp.statusCode >= 200 && resp.statusCode < 300,
        'status': resp.statusCode,
        'datos': map['datos'],
        'mensaje': map['mensaje']?.toString() ?? '',
      };
    } on TimeoutException {
      return {
        'ok': false,
        'status': 503,
        'datos': null,
        'mensaje': 'El servicio tardó demasiado en responder. Intenta de nuevo.',
      };
    } catch (_) {
      return {
        'ok': false,
        'status': 503,
        'datos': null,
        'mensaje': 'No pudimos conectar con el servicio. Verifica tu conexión.',
      };
    }
  }

  Future<void> _handleAdminLogin() async {
    if (!_loginFormKey.currentState!.validate()) return;
    setState(() {
      _autenticando = true;
      _errorLogin = null;
    });

    final resp = await _requestAdmin('POST', ApiConstants.adminLogin, body: {
      'correo': _adminEmailCtrl.text.trim(),
      'password': _adminPassCtrl.text,
    });

    if (!mounted) return;
    setState(() => _autenticando = false);

    final datos = resp['datos'];
    if (resp['ok'] == true && datos is Map<String, dynamic> && datos['token'] != null) {
      setState(() {
        _adminToken = datos['token'].toString();
        _adminCorreo = datos['correo']?.toString() ?? _adminEmailCtrl.text.trim();
        _adminPassCtrl.clear();
      });
      _cargarCuentas();
    } else {
      setState(() {
        _errorLogin = (resp['mensaje']?.toString().isNotEmpty ?? false)
            ? resp['mensaje'].toString()
            : 'Credenciales de administrador incorrectas.';
      });
    }
  }

  void _cerrarSesionAdmin() {
    setState(() {
      _adminToken = null;
      _adminCorreo = null;
      _cuentas = [];
      _cuentaCtrl.clear();
      _montoCtrl.clear();
      _errorLogin = null;
    });
  }

  Future<void> _cargarCuentas() async {
    setState(() {
      _cargandoCuentas = true;
      _errorCuentas = null;
    });

    final resp = await _requestAdmin('GET', ApiConstants.adminCuentas);
    if (!mounted) return;

    if (resp['ok'] == true && resp['datos'] is List) {
      final list = (resp['datos'] as List)
          .whereType<Map<String, dynamic>>()
          .toList();
      setState(() {
        _cuentas = list;
        _cargandoCuentas = false;
      });
    } else {
      setState(() {
        _cargandoCuentas = false;
        _errorCuentas = (resp['mensaje']?.toString().isNotEmpty ?? false)
            ? resp['mensaje'].toString()
            : 'No se pudieron cargar las cuentas.';
      });
    }
  }

  Future<void> _aplicarAbono() async {
    if (!_abonoFormKey.currentState!.validate()) return;
    final numeroCuenta = _cuentaCtrl.text.trim();
    final monto = double.tryParse(_montoCtrl.text.trim()) ?? 0;

    setState(() => _enviandoAbono = true);
    final resp = await _requestAdmin('POST', ApiConstants.adminAbono, body: {
      'numeroCuenta': numeroCuenta,
      'monto': monto,
    });

    if (!mounted) return;
    setState(() => _enviandoAbono = false);

    if (resp['ok'] == true) {
      _montoCtrl.clear();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Abono de ${CurrencyFormatter.format(monto)} aplicado a la cuenta $numeroCuenta',
          ),
          backgroundColor: AppColors.success,
          behavior: SnackBarBehavior.floating,
        ),
      );
      await _cargarCuentas();
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            (resp['mensaje']?.toString().isNotEmpty ?? false)
                ? resp['mensaje'].toString()
                : 'No se pudo aplicar el abono.',
          ),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  List<Map<String, dynamic>> get _cuentasFiltradas {
    final q = _busquedaCtrl.text.trim().toLowerCase();
    if (q.isEmpty) return _cuentas;
    return _cuentas.where((c) {
      final nom = (c['nombreCompleto'] ?? '').toString().toLowerCase();
      final cor = (c['correo'] ?? '').toString().toLowerCase();
      final cta = (c['numeroCuenta'] ?? '').toString().toLowerCase();
      return nom.contains(q) || cor.contains(q) || cta.contains(q);
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    final autenticado = _adminToken != null && _adminToken!.isNotEmpty;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(
          autenticado ? 'Consola Admin • Abonos' : 'Acceso Administrativo',
          style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 17),
        ),
        backgroundColor: const Color(0xFF1E1035),
        foregroundColor: Colors.white,
        elevation: 0,
        actions: autenticado
            ? [
                IconButton(
                  tooltip: 'Actualizar cuentas',
                  icon: const Icon(Icons.refresh_rounded),
                  onPressed: _cargarCuentas,
                ),
                IconButton(
                  tooltip: 'Cerrar sesión Admin',
                  icon: const Icon(Icons.logout_rounded),
                  onPressed: _cerrarSesionAdmin,
                ),
              ]
            : null,
      ),
      body: autenticado ? _buildConsolaAdmin() : _buildLoginAdmin(),
    );
  }

  Widget _buildLoginAdmin() {
    return SafeArea(
      child: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(24),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF1E1035).withValues(alpha: 0.08),
                  blurRadius: 24,
                  offset: const Offset(0, 10),
                ),
              ],
            ),
            child: Form(
              key: _loginFormKey,
              autovalidateMode: AutovalidateMode.onUserInteraction,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    width: 60,
                    height: 60,
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [Color(0xFF2E1065), Color(0xFF7C3AED)],
                      ),
                      borderRadius: BorderRadius.circular(18),
                    ),
                    child: const Icon(
                      Icons.admin_panel_settings_rounded,
                      color: Color(0xFFFBBF24),
                      size: 32,
                    ),
                  ),
                  const SizedBox(height: 14),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.warningBg,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: const Text(
                      'ACCESO RESTRINGIDO',
                      style: TextStyle(
                        fontSize: 10.5,
                        fontWeight: FontWeight.w800,
                        color: AppColors.warning,
                        letterSpacing: 0.8,
                      ),
                    ),
                  ),
                  const SizedBox(height: 10),
                  const Text(
                    'Panel Administrativo',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.w900,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Ingresa tus credenciales de administrador para abonar saldo a clientes.',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 12.5, color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: 20),
                  if (_errorLogin != null) ...[
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(12),
                      margin: const EdgeInsets.only(bottom: 14),
                      decoration: BoxDecoration(
                        color: AppColors.errorBg,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: AppColors.error.withValues(alpha: 0.3)),
                      ),
                      child: Text(
                        _errorLogin!,
                        style: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                          color: AppColors.error,
                        ),
                      ),
                    ),
                  ],
                  CustomTextField(
                    label: 'Correo de Administrador',
                    hint: 'admin@gestopago.com',
                    controller: _adminEmailCtrl,
                    keyboardType: TextInputType.emailAddress,
                    prefixIcon: Icons.mail_outline_rounded,
                    validator: (v) {
                      final s = (v ?? '').trim();
                      if (s.isEmpty) return 'Ingresa el correo de administrador';
                      if (!s.contains('@')) return 'Formato de correo inválido';
                      return null;
                    },
                  ),
                  const SizedBox(height: 14),
                  CustomTextField(
                    label: 'Contraseña Administrativa',
                    hint: '••••••••••••',
                    controller: _adminPassCtrl,
                    isPassword: true,
                    prefixIcon: Icons.lock_outline_rounded,
                    validator: (v) =>
                        (v == null || v.isEmpty) ? 'Ingresa tu contraseña administrativa' : null,
                  ),
                  const SizedBox(height: 20),
                  CustomButton(
                    text: 'Ingresar como Administrador',
                    icon: Icons.shield_rounded,
                    isLoading: _autenticando,
                    onPressed: _handleAdminLogin,
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildConsolaAdmin() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        // Banner de sesión admin activa
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
          decoration: BoxDecoration(
            color: const Color(0xFF1E1035),
            borderRadius: BorderRadius.circular(14),
          ),
          child: Row(
            children: [
              const Icon(Icons.verified_user_rounded, color: Color(0xFFFBBF24), size: 18),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  'Sesión Admin: ${_adminCorreo ?? "admin@gestopago.com"}',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),

        // Tarjeta de formulario de abono
        Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(20),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.04),
                blurRadius: 14,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Form(
            key: _abonoFormKey,
            autovalidateMode: AutovalidateMode.onUserInteraction,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text(
                  'Abonar Saldo a Cuenta',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w800,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 14),
                CustomTextField(
                  label: 'Número de cuenta (10 dígitos)',
                  hint: 'Selecciona un cliente abajo o escribe la cuenta',
                  controller: _cuentaCtrl,
                  keyboardType: TextInputType.number,
                  prefixIcon: Icons.credit_card_rounded,
                  inputFormatters: [
                    FilteringTextInputFormatter.digitsOnly,
                    LengthLimitingTextInputFormatter(10),
                  ],
                  validator: (v) {
                    final s = (v ?? '').trim();
                    if (s.isEmpty) return 'Ingresa el número de cuenta';
                    if (s.length < 10) return 'Faltan ${10 - s.length} dígitos (${s.length}/10)';
                    return null;
                  },
                ),
                const SizedBox(height: 12),
                CustomTextField(
                  label: 'Monto a abonar (\$ MXN)',
                  hint: 'Ej. 5000.00',
                  controller: _montoCtrl,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  prefixIcon: Icons.attach_money_rounded,
                  inputFormatters: [
                    FilteringTextInputFormatter.allow(RegExp(r'^\d*\.?\d{0,2}')),
                  ],
                  validator: (v) {
                    final s = (v ?? '').trim();
                    if (s.isEmpty) return 'Ingresa el monto a abonar';
                    final n = double.tryParse(s);
                    if (n == null || n <= 0) return 'El monto debe ser mayor a \$0.00';
                    if (n > 500000) return 'El abono máximo por operación es \$500,000 MXN';
                    return null;
                  },
                ),
                const SizedBox(height: 10),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [500, 1000, 5000, 10000].map((m) {
                    return ActionChip(
                      label: Text(
                        '+\$${m.toString()}',
                        style: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                          color: AppColors.accent,
                        ),
                      ),
                      backgroundColor: AppColors.accentSoft,
                      side: BorderSide.none,
                      onPressed: () {
                        _montoCtrl.text = m.toString();
                        _abonoFormKey.currentState?.validate();
                      },
                    );
                  }).toList(),
                ),
                const SizedBox(height: 16),
                CustomButton(
                  text: 'Aplicar Abono',
                  icon: Icons.add_circle_outline_rounded,
                  isLoading: _enviandoAbono,
                  onPressed: _aplicarAbono,
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 18),

        // Buscador y lista de clientes
        TextField(
          controller: _busquedaCtrl,
          onChanged: (_) => setState(() {}),
          decoration: InputDecoration(
            hintText: 'Buscar por nombre, correo o cuenta...',
            prefixIcon: const Icon(Icons.search_rounded),
            filled: true,
            fillColor: Colors.white,
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
        const SizedBox(height: 12),

        if (_cargandoCuentas)
          const Padding(
            padding: EdgeInsets.all(24),
            child: Center(child: CircularProgressIndicator()),
          )
        else if (_errorCuentas != null)
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              _errorCuentas!,
              textAlign: TextAlign.center,
              style: const TextStyle(color: AppColors.error, fontWeight: FontWeight.w600),
            ),
          )
        else if (_cuentasFiltradas.isEmpty)
          const Padding(
            padding: EdgeInsets.all(24),
            child: Text(
              'No se encontraron cuentas.',
              textAlign: TextAlign.center,
              style: TextStyle(color: AppColors.textSecondary),
            ),
          )
        else
          ..._cuentasFiltradas.map((item) {
            final numCta = (item['numeroCuenta'] ?? '').toString();
            final saldo = (item['saldoActual'] is num)
                ? (item['saldoActual'] as num).toDouble()
                : double.tryParse('${item['saldoActual']}') ?? 0.0;
            final seleccionada = _cuentaCtrl.text.trim() == numCta;

            return Container(
              margin: const EdgeInsets.only(bottom: 10),
              decoration: BoxDecoration(
                color: seleccionada ? AppColors.accentSoft : Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: seleccionada ? AppColors.accent : AppColors.border,
                  width: seleccionada ? 1.5 : 1,
                ),
              ),
              child: ListTile(
                onTap: () {
                  setState(() => _cuentaCtrl.text = numCta);
                  _abonoFormKey.currentState?.validate();
                },
                leading: CircleAvatar(
                  backgroundColor: AppColors.accent.withValues(alpha: 0.14),
                  child: const Icon(Icons.person_rounded, color: AppColors.accent, size: 20),
                ),
                title: Text(
                  (item['nombreCompleto'] ?? 'Cliente').toString(),
                  style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13.5),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                subtitle: Text(
                  '${item['correo'] ?? ''}\nCta: $numCta',
                  style: const TextStyle(fontSize: 11.5, color: AppColors.textSecondary),
                ),
                isThreeLine: true,
                trailing: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      CurrencyFormatter.format(saldo),
                      style: const TextStyle(
                        fontWeight: FontWeight.w800,
                        fontSize: 13.5,
                        color: AppColors.success,
                      ),
                    ),
                    const Text(
                      'Seleccionar',
                      style: TextStyle(
                        fontSize: 10.5,
                        fontWeight: FontWeight.w700,
                        color: AppColors.accent,
                      ),
                    ),
                  ],
                ),
              ),
            );
          }),
      ],
    );
  }
}

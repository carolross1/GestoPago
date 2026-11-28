import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../core/constants/api_constants.dart';
import '../../core/network/api_client.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/currency_formatter.dart';
import '../../providers/auth_provider.dart';
import '../../providers/cuenta_provider.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';

class AdminAbonosScreen extends StatefulWidget {
  const AdminAbonosScreen({super.key});

  @override
  State<AdminAbonosScreen> createState() => _AdminAbonosScreenState();
}

class _AdminAbonosScreenState extends State<AdminAbonosScreen> {
  final _formKey = GlobalKey<FormState>();
  final _cuentaCtrl = TextEditingController();
  final _montoCtrl = TextEditingController();
  final _busquedaCtrl = TextEditingController();
  final ApiClient _api = ApiClient();

  bool _cargandoLista = false;
  bool _procesandoAbono = false;
  List<Map<String, dynamic>> _cuentasClientes = [];
  String _filtro = '';

  @override
  void initState() {
    super.initState();
    _cargarCuentasClientes();
  }

  @override
  void dispose() {
    _cuentaCtrl.dispose();
    _montoCtrl.dispose();
    _busquedaCtrl.dispose();
    super.dispose();
  }

  Future<void> _cargarCuentasClientes() async {
    setState(() => _cargandoLista = true);

    final respClientes = await _api.post<Map<String, dynamic>>(
      ApiConstants.buscarClientes,
      body: {'pagina': 0, 'tamano': 100},
      fromJson: (j) => j is Map<String, dynamic> ? j : <String, dynamic>{},
    );
    final respCuentas = await _api.post<Map<String, dynamic>>(
      ApiConstants.buscarCuentas,
      body: {'pagina': 0, 'tamano': 100},
      fromJson: (j) => j is Map<String, dynamic> ? j : <String, dynamic>{},
    );

    if (!mounted) return;

    final Map<int, Map<String, dynamic>> clientesPorId = {};
    final listaCli = respClientes.datos?['contenido'] ?? respClientes.datos?['elementos'];
    if (listaCli is List) {
      for (final item in listaCli) {
        if (item is Map<String, dynamic> && item['id'] != null) {
          clientesPorId[(item['id'] as num).toInt()] = item;
        }
      }
    }

    final List<Map<String, dynamic>> combinados = [];
    final listaCtas = respCuentas.datos?['contenido'] ?? respCuentas.datos?['elementos'];
    if (listaCtas is List) {
      for (final item in listaCtas) {
        if (item is Map<String, dynamic>) {
          final cliId = (item['clienteId'] as num?)?.toInt();
          final cli = cliId != null ? clientesPorId[cliId] : null;
          final nombre = cli != null
              ? '${cli['nombre'] ?? ''} ${cli['apellidoPaterno'] ?? ''}'.trim()
              : 'Cliente';
          final correo = cli?['correo']?.toString() ?? '';
          combinados.add(<String, dynamic>{
            'numeroCuenta': item['numeroCuenta']?.toString() ?? '',
            'estatus': item['estatus']?.toString() ?? 'ACTIVA',
            'saldoActual': double.tryParse(item['saldoActual']?.toString() ?? '0') ?? 0.0,
            'nombre': nombre.isNotEmpty ? nombre : 'Cliente GestoPago',
            'correo': correo,
          });
        }
      }
    }

    setState(() {
      _cuentasClientes = combinados;
      _cargandoLista = false;
    });
  }

  Future<void> _aplicarAbono() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _procesandoAbono = true);
    final numCuenta = _cuentaCtrl.text.trim();
    final monto = double.parse(_montoCtrl.text.trim());

    final resp = await _api.post<Map<String, dynamic>>(
      ApiConstants.abonarCuenta,
      body: {
        'numeroCuenta': numCuenta,
        'monto': double.parse(monto.toStringAsFixed(2)),
      },
      fromJson: (j) => j is Map<String, dynamic> ? j : <String, dynamic>{},
    );

    if (!mounted) return;
    setState(() => _procesandoAbono = false);

    if (resp.exito && resp.datos != null) {
      final nuevoSaldo =
          double.tryParse(resp.datos!['nuevoSaldo']?.toString() ?? '0') ?? 0.0;

      // Si es la cuenta del usuario logueado, refrescar saldo en Home al instante
      final cuentaProv = Provider.of<CuentaProvider>(context, listen: false);
      final authProv = Provider.of<AuthProvider>(context, listen: false);
      if (cuentaProv.cuenta?.numeroCuenta == numCuenta) {
        cuentaProv.actualizarSaldo(nuevoSaldo);
      } else if (authProv.currentCliente != null) {
        cuentaProv.cargarCuenta(authProv.currentCliente!.id);
      }

      _montoCtrl.clear();
      _cargarCuentasClientes();

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Abono de ${CurrencyFormatter.format(monto)} aplicado a $numCuenta. Nuevo saldo: ${CurrencyFormatter.format(nuevoSaldo)}',
          ),
          backgroundColor: AppColors.success,
          behavior: SnackBarBehavior.floating,
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(resp.mensaje.isNotEmpty ? resp.mensaje : 'No se pudo aplicar el abono'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final filtradas = _cuentasClientes.where((c) {
      if (_filtro.isEmpty) return true;
      final q = _filtro.toLowerCase();
      return c['numeroCuenta'].toString().contains(q) ||
          c['nombre'].toString().toLowerCase().contains(q) ||
          c['correo'].toString().toLowerCase().contains(q);
    }).toList();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Módulo Admin • Abonos'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            tooltip: 'Actualizar cuentas',
            onPressed: _cargarCuentasClientes,
          ),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Formulario de Abono
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: AppColors.border),
                ),
                child: Form(
                  key: _formKey,
                  autovalidateMode: AutovalidateMode.onUserInteraction,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const Row(
                        children: [
                          Icon(Icons.admin_panel_settings_rounded, color: AppColors.accent, size: 22),
                          SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              'Abonar Saldo a Cuenta de Cliente',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                                color: AppColors.textPrimary,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      CustomTextField(
                        label: 'Número de cuenta (10 dígitos) *',
                        hint: 'Selecciona abajo o escribe 10 dígitos',
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
                          if (s.length < 10) return 'Faltan ${10 - s.length} dígitos (10 en total)';
                          return null;
                        },
                      ),
                      const SizedBox(height: 14),
                      CustomTextField(
                        label: 'Monto a abonar (\$ MXN) *',
                        hint: 'Ej. 5000.00',
                        controller: _montoCtrl,
                        keyboardType: const TextInputType.numberWithOptions(decimal: true),
                        prefixIcon: Icons.attach_money_rounded,
                        inputFormatters: [
                          FilteringTextInputFormatter.allow(RegExp(r'^\d+\.?\d{0,2}')),
                        ],
                        validator: (v) {
                          final n = double.tryParse((v ?? '').trim());
                          if (n == null || n <= 0) return 'Ingresa un monto mayor a \$0.00';
                          if (n > 500000) return 'El abono máximo por operación es \$500,000 MXN';
                          return null;
                        },
                      ),
                      const SizedBox(height: 10),
                      Wrap(
                        spacing: 8,
                        runSpacing: 6,
                        children: [500, 1000, 5000, 10000].map((m) {
                          return ActionChip(
                            label: Text('+\$$m'),
                            backgroundColor: AppColors.accentSoft,
                            labelStyle: const TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w700,
                              color: AppColors.accent,
                            ),
                            onPressed: () {
                              _montoCtrl.text = m.toString();
                              _formKey.currentState?.validate();
                            },
                          );
                        }).toList(),
                      ),
                      const SizedBox(height: 18),
                      CustomButton(
                        text: 'Aplicar Abono',
                        icon: Icons.add_card_rounded,
                        isLoading: _procesandoAbono,
                        onPressed: _aplicarAbono,
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 22),

              // Directorio de Clientes y Cuentas
              const Text(
                'Cuentas Registradas',
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 8),
              CustomTextField(
                hint: 'Buscar por nombre, correo o cuenta...',
                controller: _busquedaCtrl,
                prefixIcon: Icons.search_rounded,
                onChanged: (v) => setState(() => _filtro = v.trim()),
              ),
              const SizedBox(height: 12),

              if (_cargandoLista)
                const Padding(
                  padding: EdgeInsets.all(24),
                  child: Center(child: CircularProgressIndicator()),
                )
              else if (filtradas.isEmpty)
                const Padding(
                  padding: EdgeInsets.all(20),
                  child: Center(
                    child: Text(
                      'No se encontraron cuentas.',
                      style: TextStyle(color: AppColors.textSecondary),
                    ),
                  ),
                )
              else
                ...filtradas.map((c) {
                  final cuentaNum = c['numeroCuenta'].toString();
                  final seleccionada = _cuentaCtrl.text.trim() == cuentaNum;
                  return InkWell(
                    borderRadius: BorderRadius.circular(16),
                    onTap: () {
                      setState(() => _cuentaCtrl.text = cuentaNum);
                      _formKey.currentState?.validate();
                    },
                    child: Container(
                      margin: const EdgeInsets.only(bottom: 10),
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: seleccionada ? AppColors.accentSoft.withValues(alpha: 0.45) : AppColors.surface,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: seleccionada ? AppColors.accent : AppColors.border,
                          width: seleccionada ? 1.5 : 1,
                        ),
                      ),
                      child: Row(
                        children: [
                          CircleAvatar(
                            radius: 20,
                            backgroundColor: AppColors.accentSoft,
                            child: Text(
                              c['nombre'].toString().isNotEmpty
                                  ? c['nombre'].toString()[0].toUpperCase()
                                  : 'C',
                              style: const TextStyle(
                                color: AppColors.accent,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  c['nombre'].toString(),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    fontSize: 13.5,
                                    fontWeight: FontWeight.w700,
                                    color: AppColors.textPrimary,
                                  ),
                                ),
                                Text(
                                  c['correo'].toString(),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    fontSize: 11.5,
                                    color: AppColors.textSecondary,
                                  ),
                                ),
                                Text(
                                  'Cta: $cuentaNum',
                                  style: const TextStyle(
                                    fontSize: 11.5,
                                    fontFamily: 'monospace',
                                    fontWeight: FontWeight.w700,
                                    color: AppColors.accent,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            CurrencyFormatter.format(c['saldoActual'] as double),
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w800,
                              color: AppColors.success,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                }),
            ],
          ),
        ),
      ),
    );
  }
}

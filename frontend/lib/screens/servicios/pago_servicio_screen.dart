import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/currency_formatter.dart';
import '../../models/producto_model.dart';
import '../../models/transaccion_model.dart';
import '../../providers/cuenta_provider.dart';
import '../../providers/pago_provider.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';
import '../../widgets/brand_logo_widget.dart';
import '../transacciones/modal_2fa.dart';

class PagoServicioScreen extends StatefulWidget {
  final ProductoModel producto;

  const PagoServicioScreen({super.key, required this.producto});

  @override
  State<PagoServicioScreen> createState() => _PagoServicioScreenState();
}

class _PagoServicioScreenState extends State<PagoServicioScreen> {
  final _formKey = GlobalKey<FormState>();
  final _destinoController = TextEditingController();
  final _montoController = TextEditingController();

  bool _referenciaValidada = false;
  ValidarReferenciaResponse? _infoValidacion;
  String? _errorValidacion;

  bool get _esTelefono => widget.producto.tipoFront == 1;

  double? get _precioFijo {
    final parsed = double.tryParse(widget.producto.precio.trim());
    return (parsed != null && parsed > 0) ? parsed : null;
  }

  bool get _esPrecioFijo => _precioFijo != null;

  @override
  void initState() {
    super.initState();
    if (_esPrecioFijo) {
      _montoController.text = _precioFijo!.toStringAsFixed(2);
    }
  }

  @override
  void dispose() {
    _destinoController.dispose();
    _montoController.dispose();
    super.dispose();
  }

  void _onDestinoChanged(String valor) {
    if (_referenciaValidada) {
      setState(() {
        _referenciaValidada = false;
        _infoValidacion = null;
        _errorValidacion = null;
      });
    }
  }

  Future<void> _validarReferencia() async {
    final destino = _destinoController.text.trim();
    if (destino.isEmpty) {
      setState(() {
        _errorValidacion = _esTelefono
            ? 'Ingresa el número telefónico a 10 dígitos'
            : 'Ingresa la referencia del recibo o servicio';
        _referenciaValidada = false;
      });
      return;
    }

    if (_esTelefono && destino.length != 10) {
      setState(() {
        _errorValidacion = 'El número de teléfono debe tener exactamente 10 dígitos';
        _referenciaValidada = false;
      });
      return;
    }

    FocusScope.of(context).unfocus();
    final pagoProv = Provider.of<PagoProvider>(context, listen: false);

    final req = ValidarReferenciaRequest(
      idServicio: widget.producto.idServicio,
      idProducto: widget.producto.idProducto,
      referencia: destino,
      tipoDestino: _esTelefono ? 'TELEFONO' : 'TRANSFERENCIA',
    );

    final resultado = await pagoProv.validarReferencia(req);

    if (!mounted) return;

    if (resultado != null && resultado.valida) {
      setState(() {
        _referenciaValidada = true;
        _infoValidacion = resultado;
        _errorValidacion = null;
        if ((_montoController.text.isEmpty ||
                _montoController.text == '0.00' ||
                _montoController.text == '0') &&
            resultado.adeudoSugerido != null &&
            resultado.adeudoSugerido! > 0) {
          _montoController.text =
              resultado.adeudoSugerido!.toStringAsFixed(2);
        }
      });
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Referencia verificada correctamente con el proveedor'),
          backgroundColor: AppColors.success,
          behavior: SnackBarBehavior.floating,
        ),
      );
    } else {
      setState(() {
        _referenciaValidada = false;
        _infoValidacion = null;
        _errorValidacion = (resultado?.mensaje.isNotEmpty == true)
            ? resultado!.mensaje
            : (pagoProv.errorMessage ??
                'La referencia no pudo ser validada. Verifica el formato e intenta nuevamente.');
      });
    }
  }

  Future<void> _iniciarFlujoPago() async {
    if (!_referenciaValidada) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Primero debes validar la referencia antes de pagar'),
          backgroundColor: AppColors.warning,
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    if (!_formKey.currentState!.validate()) return;

    final cuentaProv = Provider.of<CuentaProvider>(context, listen: false);
    final pagoProv = Provider.of<PagoProvider>(context, listen: false);

    final numeroCuenta = cuentaProv.cuenta?.numeroCuenta;
    if (numeroCuenta == null || numeroCuenta.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('No se tiene una cuenta activa vinculada'),
          backgroundColor: AppColors.error,
        ),
      );
      return;
    }

    final monto = double.tryParse(_montoController.text.trim()) ?? 0.0;
    if (cuentaProv.cuenta != null && monto > cuentaProv.cuenta!.saldoActual) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
              'Saldo insuficiente (${CurrencyFormatter.format(cuentaProv.cuenta!.saldoActual)}) para cubrir este pago.'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    // Paso: Enviar código 2FA por correo
    final enviado = await pagoProv.solicitarCodigo2fa(numeroCuenta);

    if (!mounted) return;

    if (enviado) {
      final tipoDestino = _esTelefono ? 'TELEFONO' : 'TRANSFERENCIA';
      final destino = _destinoController.text.trim();

      showModalBottomSheet(
        context: context,
        isScrollControlled: true,
        backgroundColor: Colors.transparent,
        builder: (_) => Modal2FA(
          numeroCuenta: numeroCuenta,
          idServicio: widget.producto.idServicio,
          idProducto: widget.producto.idProducto,
          monto: monto,
          tipoDestino: tipoDestino,
          telefono: _esTelefono ? destino : null,
          referencia: !_esTelefono ? destino : null,
          servicioNombre: widget.producto.servicio,
          productoNombre: widget.producto.producto,
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(pagoProv.errorMessage ?? 'Error al solicitar código 2FA'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final cuentaProv = Provider.of<CuentaProvider>(context);
    final pagoProv = Provider.of<PagoProvider>(context);
    final saldoDisponible = cuentaProv.cuenta?.saldoActual ?? 0.0;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(
          widget.producto.servicio,
          style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 18),
        ),
        centerTitle: true,
        elevation: 0,
        backgroundColor: AppColors.surface,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Tarjeta informativa del Servicio
              Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: AppColors.border, width: 1),
                ),
                child: Row(
                  children: [
                    BrandLogoWidget(
                      servicio: widget.producto.servicio,
                      size: 56,
                      borderRadius: 16,
                      urlLogo: widget.producto.urlLogo,
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            widget.producto.servicio,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w800,
                              color: AppColors.textPrimary,
                            ),
                          ),
                          const SizedBox(height: 3),
                          Text(
                            widget.producto.producto,
                            style: const TextStyle(
                              fontSize: 13,
                              color: AppColors.textSecondary,
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 20),

              // Tarjeta de Saldo Disponible
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                decoration: BoxDecoration(
                  color: AppColors.primary.withValues(alpha: 0.05),
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(
                    color: AppColors.primary.withValues(alpha: 0.15),
                    width: 1,
                  ),
                ),
                child: Wrap(
                  alignment: WrapAlignment.spaceBetween,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  spacing: 12,
                  runSpacing: 6,
                  children: [
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: const [
                        Icon(Icons.account_balance_wallet_outlined,
                            size: 18, color: AppColors.primary),
                        SizedBox(width: 8),
                        Text(
                          'Saldo en cuenta:',
                          style: TextStyle(
                            fontSize: 13,
                            color: AppColors.textSecondary,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                    Text(
                      CurrencyFormatter.format(saldoDisponible),
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),

              // Campo de Destino (Teléfono o Referencia)
              Text(
                _esTelefono
                    ? '1. Número Telefónico (10 dígitos)'
                    : '1. Referencia del Recibo o Servicio',
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 8),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: CustomTextField(
                      controller: _destinoController,
                      hintText: _esTelefono
                          ? 'Ej: 5512345678'
                          : 'Ej: 01800123456789',
                      prefixIcon: _esTelefono
                          ? Icons.phone_android_rounded
                          : Icons.confirmation_number_outlined,
                      keyboardType: _esTelefono ? TextInputType.number : TextInputType.text,
                      inputFormatters: [
                        if (_esTelefono) FilteringTextInputFormatter.digitsOnly,
                        LengthLimitingTextInputFormatter(_esTelefono ? 10 : 30),
                      ],
                      onChanged: _onDestinoChanged,
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return 'Campo obligatorio';
                        }
                        if (_esTelefono && value.trim().length != 10) {
                          return 'Debe tener exactamente 10 dígitos';
                        }
                        if (!_esTelefono && value.trim().length < 5) {
                          return 'Debe tener al menos 5 dígitos';
                        }
                        return null;
                      },
                    ),
                  ),
                  const SizedBox(width: 10),
                  SizedBox(
                    height: 52,
                    child: ElevatedButton(
                      onPressed: pagoProv.isValidandoReferencia
                          ? null
                          : _validarReferencia,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: _referenciaValidada
                            ? AppColors.success
                            : AppColors.primary,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                        ),
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                      ),
                      child: pagoProv.isValidandoReferencia
                          ? const SizedBox(
                              width: 18,
                              height: 18,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : Icon(
                              _referenciaValidada
                                  ? Icons.check_circle_rounded
                                  : Icons.search_rounded,
                              color: Colors.white,
                              size: 20,
                            ),
                    ),
                  ),
                ],
              ),

              // Banner de estado de validación
              if (_referenciaValidada && _infoValidacion != null) ...[
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: AppColors.success.withValues(alpha: 0.08),
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(
                      color: AppColors.success.withValues(alpha: 0.3),
                      width: 1,
                    ),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.verified_rounded,
                          color: AppColors.success, size: 22),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Referencia Validada con Éxito',
                              style: TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.w700,
                                color: AppColors.success,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              _infoValidacion!.mensaje,
                              style: const TextStyle(
                                fontSize: 12,
                                color: AppColors.textSecondary,
                              ),
                            ),
                            if (_infoValidacion!.adeudoSugerido != null &&
                                _infoValidacion!.adeudoSugerido! > 0) ...[
                              const SizedBox(height: 4),
                              Text(
                                'Adeudo sugerido: ${CurrencyFormatter.format(_infoValidacion!.adeudoSugerido!)}',
                                style: const TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w700,
                                  color: AppColors.textPrimary,
                                ),
                              ),
                            ],
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ] else if (_errorValidacion != null) ...[
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: AppColors.error.withValues(alpha: 0.08),
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(
                      color: AppColors.error.withValues(alpha: 0.3),
                      width: 1,
                    ),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.error_outline_rounded,
                          color: AppColors.error, size: 22),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          _errorValidacion!,
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.error,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],

              const SizedBox(height: 24),

              // Campo de Monto a Pagar
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Flexible(
                    child: Text(
                      '2. Monto a Pagar',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ),
                  if (_esPrecioFijo) ...[
                    const SizedBox(width: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: AppColors.accentSoft,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Text(
                        'Precio fijo',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w700,
                          color: AppColors.accent,
                        ),
                      ),
                    ),
                  ],
                ],
              ),
              const SizedBox(height: 8),
              CustomTextField(
                controller: _montoController,
                hintText: '0.00',
                prefixIcon: Icons.attach_money_rounded,
                readOnly: _esPrecioFijo,
                suffixIcon: _esPrecioFijo
                    ? const Icon(Icons.lock_outline_rounded, size: 18, color: AppColors.accent)
                    : null,
                keyboardType:
                    const TextInputType.numberWithOptions(decimal: true),
                inputFormatters: [
                  FilteringTextInputFormatter.allow(RegExp(r'^\d+\.?\d{0,2}')),
                ],
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Ingresa un monto válido';
                  }
                  final monto = double.tryParse(value.trim());
                  if (monto == null || monto <= 0) {
                    return 'El monto debe ser mayor a \$0.00';
                  }
                  if (_esPrecioFijo && (monto - _precioFijo!).abs() > 0.01) {
                    return 'El precio fijo del paquete es ${CurrencyFormatter.format(_precioFijo!)}';
                  }
                  if (monto > saldoDisponible) {
                    return 'El monto excede tu saldo disponible';
                  }
                  return null;
                },
              ),
              if (_esPrecioFijo) ...[
                const SizedBox(height: 6),
                Text(
                  'Este paquete tiene un importe fijo de ${CurrencyFormatter.format(_precioFijo!)} y no puede modificarse.',
                  style: const TextStyle(
                    fontSize: 11,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],

              const SizedBox(height: 32),

              // Botón de Pago con 2FA
              CustomButton(
                text: _referenciaValidada
                    ? 'Confirmar Pago con Código 2FA'
                    : 'Valida la Referencia para Continuar',
                icon: Icons.security_rounded,
                isLoading: pagoProv.isEnviando2fa,
                onPressed: _referenciaValidada ? _iniciarFlujoPago : _validarReferencia,
                backgroundColor: _referenciaValidada
                    ? AppColors.primary
                    : AppColors.primary.withValues(alpha: 0.6),
              ),

              const SizedBox(height: 16),

              const Center(
                child: Text(
                  'Por seguridad, se enviará un código 2FA a tu correo registrado para autorizar el cobro.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 11,
                    color: AppColors.textSecondary,
                    height: 1.4,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

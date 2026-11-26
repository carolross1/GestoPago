import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/currency_formatter.dart';
import '../../models/transaccion_model.dart';
import '../../providers/cuenta_provider.dart';
import '../../providers/pago_provider.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';
import 'comprobante_screen.dart';

class Modal2FA extends StatefulWidget {
  final String numeroCuenta;
  final int idServicio;
  final int idProducto;
  final double monto;
  final String tipoDestino;
  final String? telefono;
  final String? referencia;
  final String servicioNombre;
  final String productoNombre;

  const Modal2FA({
    super.key,
    required this.numeroCuenta,
    required this.idServicio,
    required this.idProducto,
    required this.monto,
    required this.tipoDestino,
    this.telefono,
    this.referencia,
    required this.servicioNombre,
    required this.productoNombre,
  });

  @override
  State<Modal2FA> createState() => _Modal2FAState();
}

class _Modal2FAState extends State<Modal2FA> {
  final _codigoController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _codigoController.dispose();
    super.dispose();
  }

  Future<void> _confirmarPago() async {
    if (!_formKey.currentState!.validate()) return;

    final pagoProv = Provider.of<PagoProvider>(context, listen: false);
    final cuentaProv = Provider.of<CuentaProvider>(context, listen: false);

    final request = PagoRequest(
      numeroCuenta: widget.numeroCuenta,
      idServicio: widget.idServicio,
      idProducto: widget.idProducto,
      monto: widget.monto,
      tipoDestino: widget.tipoDestino,
      telefono: widget.telefono,
      referencia: widget.referencia,
      codigo2fa: _codigoController.text.trim(),
    );

    final success = await pagoProv.pagar(request);

    if (!mounted) return;

    if (success && pagoProv.ultimoPagoExitoso != null) {
      final pago = pagoProv.ultimoPagoExitoso!;
      // Actualizar el saldo localmente
      cuentaProv.actualizarSaldo(pago.saldoRestante);

      Navigator.pop(context); // Cierra modal
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => ComprobanteScreen(
            pago: pago,
            servicioNombre: widget.servicioNombre,
            productoNombre: widget.productoNombre,
            idServicio: widget.idServicio,
          ),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(pagoProv.errorMessage ?? 'Código 2FA incorrecto o expirado'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final pagoProv = Provider.of<PagoProvider>(context);
    final correo = pagoProv.solicitud2fa?.correoEnmascarado ?? 'tu correo registrado';
    final codigoRespaldo = pagoProv.solicitud2fa?.codigoParaPruebas;
    final maxModalHeight = MediaQuery.of(context).size.height * 0.90;

    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
      child: ConstrainedBox(
        constraints: BoxConstraints(maxHeight: maxModalHeight),
        child: Container(
          decoration: const BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
          ),
          child: SafeArea(
            top: false,
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(24, 16, 24, 24),
              child: Form(
                key: _formKey,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Barra superior
                    Center(
                      child: Container(
                        width: 40,
                        height: 4,
                        decoration: BoxDecoration(
                          color: AppColors.border,
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Icono de candado / 2FA
                    Center(
                      child: Container(
                        width: 52,
                        height: 52,
                        decoration: BoxDecoration(
                          color: AppColors.accent.withValues(alpha: 0.1),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.verified_user_rounded,
                          color: AppColors.accent,
                          size: 28,
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),

                    const Text(
                      'Autorización de Seguridad 2FA',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                        letterSpacing: -0.3,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      'Hemos enviado un código de 6 dígitos a $correo para autorizar tu operación.',
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        fontSize: 13,
                        color: AppColors.textSecondary,
                        height: 1.35,
                      ),
                    ),
                    const SizedBox(height: 14),

                    // Resumen de la Operación (Requerimiento: confirmación explícita con datos visibles)
                    Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: AppColors.surfaceElevated,
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(color: AppColors.border),
                      ),
                      child: Column(
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              const Text('Servicio:', style: TextStyle(fontSize: 12, color: AppColors.textSecondary)),
                              const SizedBox(width: 10),
                              Flexible(
                                child: Text(
                                  widget.servicioNombre,
                                  textAlign: TextAlign.end,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: AppColors.textPrimary),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 6),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text(
                                widget.tipoDestino == 'TELEFONO' ? 'Teléfono:' : 'Referencia:',
                                style: const TextStyle(fontSize: 12, color: AppColors.textSecondary),
                              ),
                              const SizedBox(width: 10),
                              Flexible(
                                child: Text(
                                  widget.telefono ?? widget.referencia ?? '-',
                                  textAlign: TextAlign.end,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: AppColors.textPrimary),
                                ),
                              ),
                            ],
                          ),
                          const Divider(height: 14),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              const Text('Total a pagar:', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w700, color: AppColors.textPrimary)),
                              const SizedBox(width: 10),
                              Flexible(
                                child: Text(
                                  CurrencyFormatter.format(widget.monto),
                                  textAlign: TextAlign.end,
                                  style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w900, color: AppColors.accent),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),

                    if (codigoRespaldo != null && codigoRespaldo.isNotEmpty) ...[
                      const SizedBox(height: 12),
                      InkWell(
                        borderRadius: BorderRadius.circular(12),
                        onTap: () {
                          _codigoController.text = codigoRespaldo;
                        },
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          decoration: BoxDecoration(
                            color: AppColors.accentSoft,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: AppColors.accent.withValues(alpha: 0.3)),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.key_rounded, color: AppColors.accent, size: 18),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const Text(
                                      'Código de verificación (respaldo en pantalla)',
                                      style: TextStyle(
                                        fontSize: 11,
                                        color: AppColors.textSecondary,
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                    Text(
                                      codigoRespaldo,
                                      style: const TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.w900,
                                        color: AppColors.accent,
                                        letterSpacing: 3,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              const Text(
                                'Autollenar',
                                style: TextStyle(
                                  fontSize: 11,
                                  fontWeight: FontWeight.w800,
                                  color: AppColors.accent,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                    const SizedBox(height: 14),

                    // Campo de código de 6 dígitos
                    CustomTextField(
                      label: 'Código de 6 dígitos',
                      hint: '123456',
                      controller: _codigoController,
                      keyboardType: TextInputType.number,
                      prefixIcon: Icons.key_rounded,
                      validator: (val) {
                        if (val == null || val.trim().length != 6) {
                          return 'Ingresa el código de 6 dígitos';
                        }
                        return null;
                      },
                    ),

                    const SizedBox(height: 20),

                    CustomButton(
                      text: 'Confirmar y Pagar',
                      icon: Icons.check_circle_outline_rounded,
                      isLoading: pagoProv.isProcesandoPago,
                      onPressed: _confirmarPago,
                    ),
                    const SizedBox(height: 10),
                    CustomButton(
                      text: 'Cancelar',
                      isSecondary: true,
                      onPressed: () => Navigator.pop(context),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}


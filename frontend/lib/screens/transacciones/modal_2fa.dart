import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/services/biometric_service.dart';
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

  bool _biometriaDisponible = false;
  bool _usarCodigoManual = false;
  bool _autenticandoHuella = false;
  bool _codigoCorreoEnviado = false;

  @override
  void initState() {
    super.initState();
    _verificarBiometria();
  }

  Future<void> _verificarBiometria() async {
    final puedeHuella = await BiometricService.instance.canUseBiometricPayments();
    if (!mounted) return;
    setState(() {
      _biometriaDisponible = puedeHuella;
      _usarCodigoManual = !puedeHuella;
    });
  }

  @override
  void dispose() {
    _codigoController.dispose();
    super.dispose();
  }

  Future<void> _solicitarCodigoPorCorreo() async {
    final pagoProv = Provider.of<PagoProvider>(context, listen: false);
    final ok = await pagoProv.solicitarCodigo2fa(widget.numeroCuenta, enviarCorreo: true);
    if (!mounted) return;
    if (ok) {
      setState(() => _codigoCorreoEnviado = true);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Código enviado a ${pagoProv.solicitud2fa?.correoEnmascarado ?? 'tu correo registrado'}.',
          ),
          backgroundColor: AppColors.success,
          behavior: SnackBarBehavior.floating,
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(pagoProv.errorMessage ?? 'No se pudo enviar el código de verificación'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  Future<void> _procesarPagoConCodigo(String codigo) async {
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
      codigo2fa: codigo.trim(),
    );

    final success = await pagoProv.pagar(request);

    if (!mounted) return;

    if (success && pagoProv.ultimoPagoExitoso != null) {
      final pago = pagoProv.ultimoPagoExitoso!;
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

  Future<void> _confirmarConHuella() async {
    if (_autenticandoHuella) return;

    setState(() => _autenticandoHuella = true);
    final resultado = await BiometricService.instance.authenticate(
      reason:
          'Autoriza tu pago de ${CurrencyFormatter.format(widget.monto)} a ${widget.servicioNombre}',
    );
    if (!mounted) return;

    if (!resultado.success) {
      setState(() => _autenticandoHuella = false);
      if (resultado.errorMessage != null) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(resultado.errorMessage!),
            backgroundColor: AppColors.error,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
      return;
    }

    // Genera token de un solo uso SIN enviar correo para no saturar la bandeja del usuario
    final pagoProv = Provider.of<PagoProvider>(context, listen: false);
    final okToken = await pagoProv.solicitarCodigo2fa(
      widget.numeroCuenta,
      enviarCorreo: false,
    );
    if (!mounted) return;
    setState(() => _autenticandoHuella = false);

    final codigoToken = pagoProv.solicitud2fa?.codigoParaPruebas;
    if (okToken && codigoToken != null && codigoToken.isNotEmpty) {
      await _procesarPagoConCodigo(codigoToken);
    } else {
      setState(() => _usarCodigoManual = true);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            pagoProv.errorMessage ?? 'Solicita tu código 2FA por correo para completar el pago.',
          ),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  Future<void> _confirmarConCodigo() async {
    if (!_formKey.currentState!.validate()) return;
    await _procesarPagoConCodigo(_codigoController.text.trim());
  }

  @override
  Widget build(BuildContext context) {
    final pagoProv = Provider.of<PagoProvider>(context);
    final correo = pagoProv.solicitud2fa?.correoEnmascarado ?? 'tu correo registrado';
    final maxModalHeight = MediaQuery.of(context).size.height * 0.90;
    final modoHuella = _biometriaDisponible && !_usarCodigoManual;

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

                    // Icono de huella digital o 2FA
                    Center(
                      child: Container(
                        width: 58,
                        height: 58,
                        decoration: BoxDecoration(
                          color: AppColors.accent.withValues(alpha: 0.12),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(
                          modoHuella
                              ? Icons.fingerprint_rounded
                              : Icons.verified_user_rounded,
                          color: AppColors.accent,
                          size: 32,
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),

                    Text(
                      modoHuella
                          ? 'Autorización con Huella Digital'
                          : 'Autorización de Seguridad 2FA',
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                        letterSpacing: -0.3,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      modoHuella
                          ? 'Verifica los datos y confirma el pago usando tu huella digital.'
                          : (_codigoCorreoEnviado
                              ? 'Ingresa el código de 6 dígitos enviado a $correo.'
                              : 'Solicita el envío del código de 6 dígitos a tu correo solo cuando lo necesites.'),
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        fontSize: 13,
                        color: AppColors.textSecondary,
                        height: 1.35,
                      ),
                    ),
                    const SizedBox(height: 14),

                    // Resumen de la Operación
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

                    if (modoHuella) ...[
                      const SizedBox(height: 20),
                      CustomButton(
                        text: 'Autorizar con Huella Digital',
                        icon: Icons.fingerprint_rounded,
                        isLoading: _autenticandoHuella || pagoProv.isProcesandoPago || pagoProv.isEnviando2fa,
                        onPressed: _confirmarConHuella,
                      ),
                      const SizedBox(height: 8),
                      TextButton(
                        onPressed: () => setState(() => _usarCodigoManual = true),
                        child: const Text(
                          'Usar código 2FA por correo en su lugar',
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: AppColors.accent,
                          ),
                        ),
                      ),
                    ] else ...[
                      const SizedBox(height: 16),
                      if (!_codigoCorreoEnviado) ...[
                        CustomButton(
                          text: 'Enviar código 2FA a mi correo',
                          icon: Icons.mail_outline_rounded,
                          isLoading: pagoProv.isEnviando2fa,
                          onPressed: _solicitarCodigoPorCorreo,
                        ),
                      ] else ...[
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
                        const SizedBox(height: 8),
                        Align(
                          alignment: Alignment.centerRight,
                          child: TextButton(
                            onPressed: pagoProv.isEnviando2fa ? null : _solicitarCodigoPorCorreo,
                            child: const Text(
                              'Reenviar código a mi correo',
                              style: TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.w700,
                                color: AppColors.accent,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(height: 8),
                        CustomButton(
                          text: 'Confirmar y Pagar',
                          icon: Icons.check_circle_outline_rounded,
                          isLoading: pagoProv.isProcesandoPago,
                          onPressed: _confirmarConCodigo,
                        ),
                      ],
                      if (_biometriaDisponible) ...[
                        const SizedBox(height: 8),
                        TextButton.icon(
                          onPressed: () => setState(() => _usarCodigoManual = false),
                          icon: const Icon(Icons.fingerprint_rounded, size: 18, color: AppColors.accent),
                          label: const Text(
                            'Volver a huella digital',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w700,
                              color: AppColors.accent,
                            ),
                          ),
                        ),
                      ],
                    ],

                    const SizedBox(height: 6),
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

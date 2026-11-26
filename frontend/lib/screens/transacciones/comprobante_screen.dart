import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/currency_formatter.dart';
import '../../models/transaccion_model.dart';
import '../../widgets/brand_logo_widget.dart';
import '../home/home_screen.dart';

class ComprobanteScreen extends StatelessWidget {
  final PagoResponse pago;
  final String servicioNombre;
  final String productoNombre;
  final int? idServicio;

  const ComprobanteScreen({
    super.key,
    required this.pago,
    required this.servicioNombre,
    required this.productoNombre,
    this.idServicio,
  });

  String _enmascararCuenta(String cuenta) {
    if (cuenta.length <= 4) return cuenta;
    return '•••• ${cuenta.substring(cuenta.length - 4)}';
  }

  Widget _buildDatoRow(String etiqueta, String valor, {bool isBold = false, Color? color}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            etiqueta,
            style: const TextStyle(
              fontSize: 13,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w500,
            ),
          ),
          Flexible(
            child: Text(
              valor,
              textAlign: TextAlign.end,
              style: TextStyle(
                fontSize: 14,
                fontWeight: isBold ? FontWeight.w700 : FontWeight.w600,
                color: color ?? AppColors.textPrimary,
              ),
            ),
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
        title: const Text('Comprobante Digital'),
        automaticallyImplyLeading: false,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 20.0),
          child: Column(
            children: [
              // Card Principal del Comprobante
              Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(24),
                  border: Border.all(color: AppColors.border),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.04),
                      blurRadius: 20,
                      offset: const Offset(0, 6),
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    // Icono de éxito
                    Container(
                      width: 64,
                      height: 64,
                      decoration: const BoxDecoration(
                        color: AppColors.successBg,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.check_circle_rounded,
                        color: AppColors.success,
                        size: 38,
                      ),
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      '¡Pago Realizado con Éxito!',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                        letterSpacing: -0.4,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      CurrencyFormatter.format(pago.monto),
                      style: const TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.w900,
                        color: AppColors.accent,
                        letterSpacing: -0.5,
                      ),
                    ),
                    const SizedBox(height: 24),

                    // Línea divisoria decorativa con dientes estilo ticket
                    Row(
                      children: List.generate(
                        26,
                        (index) => Expanded(
                          child: Container(
                            height: 1.5,
                            color: index % 2 == 0 ? AppColors.border : Colors.transparent,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 20),

                    // Logotipo del Servicio y Datos de Transacción
                    Center(
                      child: Padding(
                        padding: const EdgeInsets.only(bottom: 12.0),
                        child: BrandLogoWidget(
                          servicio: servicioNombre,
                          idServicio: idServicio,
                          size: 56,
                          borderRadius: 16,
                        ),
                      ),
                    ),
                    _buildDatoRow('Estado', 'EXITOSA', isBold: true, color: AppColors.success),
                    _buildDatoRow('Servicio', servicioNombre, isBold: true),
                    _buildDatoRow('Producto', productoNombre),
                    _buildDatoRow(
                      pago.tipoDestino == 'TELEFONO' ? 'Teléfono destino' : 'Referencia',
                      pago.destino,
                    ),
                    _buildDatoRow('Cuenta emisora', _enmascararCuenta(pago.numeroCuenta)),
                    _buildDatoRow('ID Transacción (GestoPago)', pago.idTx),
                    _buildDatoRow('No. de Autorización', pago.numAutorizacion, isBold: true),
                    _buildDatoRow('Fecha y Hora', '${pago.fecha ?? ""} ${pago.hora ?? ""}'),
                    const Divider(height: 24),

                    _buildDatoRow(
                      'Saldo restante en cuenta',
                      CurrencyFormatter.format(pago.saldoRestante),
                      isBold: true,
                      color: AppColors.success,
                    ),

                    const SizedBox(height: 20),

                    // Folio UPC
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                      decoration: BoxDecoration(
                        color: AppColors.surfaceElevated,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: AppColors.border),
                      ),
                      child: Column(
                        children: [
                          const Text(
                            'FOLIO DIGITAL UPC',
                            style: TextStyle(
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                              color: AppColors.textSecondary,
                              letterSpacing: 1.2,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            pago.numAutorizacion.isNotEmpty
                                ? 'UPC-${pago.numAutorizacion}'
                                : 'UPC-TX-${pago.idTx}',
                            style: const TextStyle(
                              fontSize: 15,
                              fontFamily: 'monospace',
                              fontWeight: FontWeight.w700,
                              letterSpacing: 2,
                              color: AppColors.textPrimary,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 28),

              // Botones de acción
              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton.icon(
                  onPressed: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Comprobante enviado a tu correo electrónico registrado.'),
                        backgroundColor: AppColors.success,
                        behavior: SnackBarBehavior.floating,
                      ),
                    );
                  },
                  icon: const Icon(Icons.share_outlined, size: 20),
                  label: const Text('Enviar a mi Correo'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                ),
              ),

              const SizedBox(height: 12),

              SizedBox(
                width: double.infinity,
                height: 52,
                child: OutlinedButton(
                  onPressed: () {
                    Navigator.pushAndRemoveUntil(
                      context,
                      MaterialPageRoute(builder: (_) => const HomeScreen()),
                      (route) => false,
                    );
                  },
                  style: OutlinedButton.styleFrom(
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                  child: const Text('Volver al Inicio'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}


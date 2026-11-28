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
      padding: const EdgeInsets.symmetric(vertical: 7.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Flexible(
            child: Text(
              etiqueta,
              style: const TextStyle(
                fontSize: 12.5,
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
          const SizedBox(width: 12),
          Flexible(
            child: Text(
              valor,
              textAlign: TextAlign.end,
              style: TextStyle(
                fontSize: 13.5,
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
    final bool aprobada = pago.aprobada;
    final Color statusColor = aprobada ? AppColors.success : AppColors.error;
    final Color statusBg = aprobada ? AppColors.successBg : AppColors.errorBg;
    final String upcMostrar = pago.upc.isNotEmpty
        ? pago.upc
        : (pago.numAutorizacion.isNotEmpty ? pago.numAutorizacion : pago.idTx);
    final int? servId = pago.idServicio ?? idServicio;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(aprobada ? 'Ticket Digital' : 'Ticket de Operación Rechazada'),
        automaticallyImplyLeading: false,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 20.0),
          child: Column(
            children: [
              // Card Principal del Comprobante / Ticket
              Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(24),
                  border: Border.all(
                    color: aprobada ? AppColors.border : AppColors.error.withValues(alpha: 0.35),
                  ),
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
                    // Icono de estado
                    Container(
                      width: 64,
                      height: 64,
                      decoration: BoxDecoration(
                        color: statusBg,
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        aprobada ? Icons.check_circle_rounded : Icons.cancel_rounded,
                        color: statusColor,
                        size: 38,
                      ),
                    ),
                    const SizedBox(height: 14),
                    Text(
                      aprobada ? '¡Pago Realizado con Éxito!' : 'OPERACIÓN RECHAZADA',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                        color: aprobada ? AppColors.textPrimary : AppColors.error,
                        letterSpacing: -0.4,
                      ),
                    ),
                    if (!aprobada) ...[
                      const SizedBox(height: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
                        decoration: BoxDecoration(
                          color: AppColors.errorBg,
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Text(
                          '${pago.mensajeProveedor} (Código ${pago.codigoProveedor})',
                          textAlign: TextAlign.center,
                          style: const TextStyle(
                            fontSize: 12.5,
                            fontWeight: FontWeight.w700,
                            color: AppColors.error,
                          ),
                        ),
                      ),
                    ],
                    const SizedBox(height: 8),
                    Text(
                      CurrencyFormatter.format(pago.monto),
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.w900,
                        color: aprobada ? AppColors.accent : AppColors.error,
                        letterSpacing: -0.5,
                      ),
                    ),
                    if (!aprobada) ...[
                      const SizedBox(height: 4),
                      const Text(
                        'No se descontó ningún cargo de tu saldo.',
                        style: TextStyle(
                          fontSize: 12,
                          color: AppColors.textSecondary,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                    const SizedBox(height: 20),

                    // Línea divisoria estilo ticket
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
                    const SizedBox(height: 18),

                    // Logotipo del Servicio
                    Center(
                      child: Padding(
                        padding: const EdgeInsets.only(bottom: 10.0),
                        child: BrandLogoWidget(
                          servicio: servicioNombre,
                          idServicio: servId,
                          size: 54,
                          borderRadius: 16,
                        ),
                      ),
                    ),

                    _buildDatoRow(
                      'Estado',
                      aprobada ? 'EXITOSA' : 'OPERACIÓN RECHAZADA',
                      isBold: true,
                      color: statusColor,
                    ),
                    _buildDatoRow('Mensaje', pago.mensajeProveedor, isBold: !aprobada),
                    _buildDatoRow('Servicio', servicioNombre, isBold: true),
                    _buildDatoRow('Producto', productoNombre),
                    _buildDatoRow(
                      pago.tipoDestino == 'TELEFONO' ? 'Teléfono destino' : 'Referencia',
                      pago.destino,
                    ),
                    _buildDatoRow('Cuenta emisora', _enmascararCuenta(pago.numeroCuenta)),
                    _buildDatoRow('Fecha y Hora', '${pago.fecha ?? ""} ${pago.hora ?? ""}'),
                    if (aprobada)
                      _buildDatoRow('No. de Autorización', pago.numAutorizacion, isBold: true)
                    else
                      _buildDatoRow('Código de respuesta', pago.codigoProveedor, isBold: true, color: AppColors.error),
                    const Divider(height: 22),

                    _buildDatoRow(
                      aprobada ? 'Saldo restante en cuenta' : 'Saldo disponible (sin cargo)',
                      CurrencyFormatter.format(pago.saldoRestante),
                      isBold: true,
                      color: AppColors.success,
                    ),

                    const SizedBox(height: 16),

                    // Folio UPC 10 dígitos
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                      decoration: BoxDecoration(
                        color: AppColors.surfaceElevated,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: AppColors.border),
                      ),
                      child: Column(
                        children: [
                          const Text(
                            'FOLIO UPC (10 DÍGITOS)',
                            style: TextStyle(
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                              color: AppColors.textSecondary,
                              letterSpacing: 1.2,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            upcMostrar,
                            style: const TextStyle(
                              fontSize: 18,
                              fontFamily: 'monospace',
                              fontWeight: FontWeight.w800,
                              letterSpacing: 2.5,
                              color: AppColors.textPrimary,
                            ),
                          ),
                        ],
                      ),
                    ),

                    const SizedBox(height: 14),

                    // Leyenda del producto para aclaraciones / reclamaciones
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: aprobada ? const Color(0xFFF5F3FF) : AppColors.errorBg,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: aprobada
                              ? const Color(0xFFDDD6FE)
                              : AppColors.error.withValues(alpha: 0.35),
                        ),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Dudas y aclaraciones',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.w800,
                              color: aprobada ? AppColors.primary : AppColors.error,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            pago.leyenda,
                            style: TextStyle(
                              fontSize: 11.5,
                              height: 1.4,
                              color: aprobada ? AppColors.textSecondary : const Color(0xFF991B1B),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),

              // Aviso de envío de correo automático
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.border),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.mark_email_read_outlined, color: AppColors.accent, size: 18),
                    SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        'Se envió una copia automática de este ticket a tu correo electrónico registrado.',
                        style: TextStyle(
                          fontSize: 12,
                          color: AppColors.textSecondary,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 14),

              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  onPressed: () {
                    Navigator.pushAndRemoveUntil(
                      context,
                      MaterialPageRoute(builder: (_) => const HomeScreen()),
                      (route) => false,
                    );
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
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

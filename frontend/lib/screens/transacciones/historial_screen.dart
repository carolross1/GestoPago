import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/currency_formatter.dart';
import '../../models/transaccion_model.dart';
import '../../providers/cuenta_provider.dart';
import '../../providers/pago_provider.dart';
import '../../widgets/brand_logo_widget.dart';

class HistorialScreen extends StatefulWidget {
  final bool isEmbedded;
  const HistorialScreen({super.key, this.isEmbedded = false});

  @override
  State<HistorialScreen> createState() => _HistorialScreenState();
}

class _HistorialScreenState extends State<HistorialScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _cargarHistorial();
    });
  }

  void _cargarHistorial() {
    final cuentaProv = Provider.of<CuentaProvider>(context, listen: false);
    final numeroCuenta = cuentaProv.cuenta?.numeroCuenta;
    if (numeroCuenta != null && numeroCuenta.isNotEmpty) {
      Provider.of<PagoProvider>(context, listen: false)
          .cargarHistorial(numeroCuenta);
    }
  }

  void _mostrarDetalleTransaccion(
      BuildContext context, MovimientoHistorialResponse item) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) {
        return Container(
          decoration: const BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Center(
                child: Container(
                  width: 44,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 20),
              Row(
                children: [
                  _buildLogoAvatar(item.urlLogo, item.servicio, 48),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          item.servicio,
                          style: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w800,
                            color: AppColors.textPrimary,
                          ),
                        ),
                        Text(
                          item.producto,
                          style: const TextStyle(
                            fontSize: 13,
                            color: AppColors.textSecondary,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.success.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      item.estado,
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                        color: AppColors.success,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              Center(
                child: Column(
                  children: [
                    const Text(
                      'Importe Pagado',
                      style: TextStyle(
                        fontSize: 12,
                        color: AppColors.textSecondary,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '-${CurrencyFormatter.format(item.monto)}',
                      style: const TextStyle(
                        fontSize: 26,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                        letterSpacing: -0.5,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              const Divider(color: AppColors.border, height: 1),
              const SizedBox(height: 16),
              _buildFilaDetalle('Folio UPC:', item.folio),
              _buildFilaDetalle('Referencia / Teléfono:', item.referencia ?? 'N/D'),
              _buildFilaDetalle('Fecha de Operación:', item.fecha),
              _buildFilaDetalle('Hora:', item.hora),
              _buildFilaDetalle('Cuenta Origen:', item.numeroCuenta),
              _buildFilaDetalle(
                  'Saldo Resultante:', CurrencyFormatter.format(item.saldoResultante)),
              const SizedBox(height: 28),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => Navigator.pop(ctx),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                  child: const Text(
                    'Cerrar Detalle',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 12),
            ],
          ),
        );
      },
    );
  }

  Widget _buildFilaDetalle(String label, String valor) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 7.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: const TextStyle(
              fontSize: 13,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w500,
            ),
          ),
          Flexible(
            child: Text(
              valor,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary,
              ),
              textAlign: TextAlign.right,
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLogoAvatar(String? urlLogo, String servicio, double size) {
    return BrandLogoWidget(
      servicio: servicio,
      size: size,
      borderRadius: 12,
      urlLogo: urlLogo,
    );
  }

  @override
  Widget build(BuildContext context) {
    final pagoProv = Provider.of<PagoProvider>(context);
    final cuentaProv = Provider.of<CuentaProvider>(context);

    final Widget bodyContent = RefreshIndicator(
      onRefresh: () async => _cargarHistorial(),
      child: pagoProv.isLoadingHistorial
          ? const Center(
              child: CircularProgressIndicator(color: AppColors.accent),
            )
          : pagoProv.historial.isEmpty
              ? _buildEmptyState(cuentaProv.cuenta?.numeroCuenta)
              : ListView.separated(
                  padding: const EdgeInsets.only(
                      left: 20, right: 20, top: 16, bottom: 96),
                  itemCount: pagoProv.historial.length,
                  separatorBuilder: (_, index) => const SizedBox(height: 12),
                  itemBuilder: (ctx, index) {
                    final item = pagoProv.historial[index];
                      return _buildHistorialCard(ctx, item);
                    },
                  ),
      );

    if (widget.isEmbedded) {
      return bodyContent;
    }

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text(
          'Historial de Transacciones',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w800,
            letterSpacing: -0.3,
          ),
        ),
        centerTitle: true,
        backgroundColor: AppColors.surface,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            tooltip: 'Actualizar historial',
            onPressed: _cargarHistorial,
          ),
        ],
      ),
      body: bodyContent,
    );
  }

  Widget _buildHistorialCard(
      BuildContext context, MovimientoHistorialResponse item) {
    return InkWell(
      onTap: () => _mostrarDetalleTransaccion(context, item),
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.border, width: 1),
        ),
        child: Row(
          children: [
            _buildLogoAvatar(item.urlLogo, item.servicio, 44),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    item.servicio,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    'Folio: ${item.folio}',
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    '${item.fecha} • ${item.hora}',
                    style: const TextStyle(
                      fontSize: 11,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '-${CurrencyFormatter.format(item.monto)}',
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w800,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 4),
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                  decoration: BoxDecoration(
                    color: AppColors.success.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(
                    item.estado,
                    style: const TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.w700,
                      color: AppColors.success,
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState(String? numeroCuenta) {
    return Center(
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: AppColors.primary.withValues(alpha: 0.08),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.receipt_long_outlined,
                size: 48,
                color: AppColors.primary,
              ),
            ),
            const SizedBox(height: 20),
            const Text(
              'Sin movimientos registrados',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w800,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              numeroCuenta != null
                  ? 'La cuenta $numeroCuenta aún no tiene pagos de servicios procesados.'
                  : 'No se encontró una cuenta activa vinculada.',
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 13,
                color: AppColors.textSecondary,
                height: 1.4,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

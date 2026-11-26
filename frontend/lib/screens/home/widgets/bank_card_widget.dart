import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../core/utils/currency_formatter.dart';
import '../../../models/cliente_model.dart';
import '../../../models/cuenta_model.dart';
import '../../../widgets/gestopago_logo.dart';

class BankCardWidget extends StatelessWidget {
  final CuentaResponse? cuenta;
  final ClienteResponse? cliente;
  final bool mostrarSaldo;
  final VoidCallback onToggleSaldo;

  const BankCardWidget({
    super.key,
    required this.cuenta,
    required this.cliente,
    required this.mostrarSaldo,
    required this.onToggleSaldo,
  });

  String _formatearNumeroCuenta(String? num) {
    if (num == null || num.isEmpty) return '•••• ••••';
    if (num.length > 4) {
      return '•••• ${num.substring(num.length - 4)}';
    }
    return num;
  }

  @override
  Widget build(BuildContext context) {
    final saldoTexto = mostrarSaldo
        ? CurrencyFormatter.format(cuenta?.saldoActual ?? 0.0)
        : '\$ •••••• MXN';

    return Container(
      width: double.infinity,
      height: 222,
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFF170B2E),
            Color(0xFF34125C),
            Color(0xFF5B21B6),
            Color(0xFF7C3AED),
          ],
          stops: [0.0, 0.34, 0.72, 1.0],
        ),
        borderRadius: BorderRadius.circular(28),
        border: Border.all(
          color: Colors.white.withValues(alpha: 0.24),
          width: 1.2,
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF5B21B6).withValues(alpha: 0.42),
            blurRadius: 34,
            spreadRadius: -4,
            offset: const Offset(0, 18),
          ),
          BoxShadow(
            color: const Color(0xFFA78BFA).withValues(alpha: 0.18),
            blurRadius: 18,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(28),
        child: Stack(
          children: [
            // Brillos especulares estilo tarjeta metálica Ultraviolet
            Positioned(
              top: -65,
              right: -45,
              child: Container(
                width: 210,
                height: 210,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      const Color(0xFFC4B5FD).withValues(alpha: 0.32),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),
            Positioned(
              bottom: -75,
              left: 20,
              child: Container(
                width: 190,
                height: 190,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      Colors.white.withValues(alpha: 0.12),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(22),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // Fila superior: Chip metálico + Logo GestoPago
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Container(
                            width: 40,
                            height: 28,
                            decoration: BoxDecoration(
                              gradient: const LinearGradient(
                                begin: Alignment.topLeft,
                                end: Alignment.bottomRight,
                                colors: [
                                  Color(0xFFF6DF97),
                                  Color(0xFFD9A74A),
                                  Color(0xFFB8832E),
                                ],
                              ),
                              borderRadius: BorderRadius.circular(7),
                              border: Border.all(
                                color: Colors.white.withValues(alpha: 0.35),
                                width: 0.8,
                              ),
                            ),
                            child: Stack(
                              children: [
                                Positioned(
                                  top: 8,
                                  left: 0,
                                  right: 0,
                                  child: Container(height: 0.8, color: Colors.black26),
                                ),
                                Positioned(
                                  top: 18,
                                  left: 0,
                                  right: 0,
                                  child: Container(height: 0.8, color: Colors.black26),
                                ),
                                Positioned(
                                  top: 0,
                                  bottom: 0,
                                  left: 14,
                                  child: Container(width: 0.8, color: Colors.black26),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(width: 10),
                          Icon(
                            Icons.contactless_rounded,
                            color: Colors.white.withValues(alpha: 0.72),
                            size: 22,
                          ),
                        ],
                      ),
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 4),
                            decoration: BoxDecoration(
                              color: Colors.white.withValues(alpha: 0.12),
                              borderRadius: BorderRadius.circular(20),
                              border: Border.all(
                                color: Colors.white.withValues(alpha: 0.16),
                              ),
                            ),
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Container(
                                  width: 6,
                                  height: 6,
                                  decoration: const BoxDecoration(
                                    color: Color(0xFF34D399),
                                    shape: BoxShape.circle,
                                  ),
                                ),
                                const SizedBox(width: 5),
                                Text(
                                  cuenta?.estatus ?? 'ACTIVA',
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 10,
                                    fontWeight: FontWeight.w700,
                                    letterSpacing: 0.6,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(width: 8),
                          const GestoPagoLogo(size: 24, lightOnDark: true),
                          const SizedBox(width: 6),
                          const Text(
                            'GestoPago',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 15,
                              fontWeight: FontWeight.w800,
                              letterSpacing: -0.2,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),

                  // Fila del Saldo
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Text(
                            'Saldo disponible',
                            style: TextStyle(
                              color: Colors.white.withValues(alpha: 0.68),
                              fontSize: 12,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          const SizedBox(width: 8),
                          GestureDetector(
                            onTap: onToggleSaldo,
                            child: Icon(
                              mostrarSaldo
                                  ? Icons.visibility_outlined
                                  : Icons.visibility_off_outlined,
                              color: Colors.white70,
                              size: 16,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        saldoTexto,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 28,
                          fontWeight: FontWeight.w800,
                          letterSpacing: -0.7,
                        ),
                      ),
                    ],
                  ),

                  // Fila inferior: Titular y Número de Cuenta enmascarado
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'TITULAR DE CUENTA',
                              style: TextStyle(
                                color: Colors.white.withValues(alpha: 0.48),
                                fontSize: 9,
                                fontWeight: FontWeight.w700,
                                letterSpacing: 1.0,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              cliente?.nombreCompleto.toUpperCase() ?? 'CLIENTE GESTOPAGO',
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 13,
                                fontWeight: FontWeight.w600,
                                letterSpacing: 0.3,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(width: 12),
                      InkWell(
                        borderRadius: BorderRadius.circular(10),
                        onTap: () {
                          if (cuenta?.numeroCuenta != null) {
                            Clipboard.setData(ClipboardData(text: cuenta!.numeroCuenta));
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Número de cuenta copiado al portapapeles'),
                                duration: Duration(seconds: 2),
                              ),
                            );
                          }
                        },
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                          decoration: BoxDecoration(
                            color: Colors.white.withValues(alpha: 0.12),
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Text(
                                _formatearNumeroCuenta(cuenta?.numeroCuenta),
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 12.5,
                                  fontFamily: 'monospace',
                                  fontWeight: FontWeight.w600,
                                  letterSpacing: 0.9,
                                ),
                              ),
                              const SizedBox(width: 5),
                              const Icon(
                                Icons.copy_rounded,
                                size: 13,
                                color: Colors.white70,
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

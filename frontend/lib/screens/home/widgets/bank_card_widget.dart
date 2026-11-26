import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../core/utils/currency_formatter.dart';
import '../../../models/cliente_model.dart';
import '../../../models/cuenta_model.dart';

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
      height: 220,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFF1E1035),
            Color(0xFF38125E),
            Color(0xFF5B21B6),
            Color(0xFF7C3AED),
          ],
        ),
        borderRadius: BorderRadius.circular(26),
        border: Border.all(
          color: Colors.white.withValues(alpha: 0.22),
          width: 1.2,
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF7C3AED).withValues(alpha: 0.42),
            blurRadius: 36,
            spreadRadius: -2,
            offset: const Offset(0, 16),
          ),
          BoxShadow(
            color: const Color(0xFFA78BFA).withValues(alpha: 0.20),
            blurRadius: 20,
            spreadRadius: 2,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Fila superior: Logo GestoPago y Chip
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    width: 38,
                    height: 28,
                    decoration: BoxDecoration(
                      color: const Color(0xFFE2B755), // Dorado chip
                      borderRadius: BorderRadius.circular(6),
                      border: Border.all(color: Colors.black12, width: 1),
                    ),
                    child: Stack(
                      children: [
                        Positioned(
                          top: 8,
                          left: 0,
                          right: 0,
                          child: Container(height: 1, color: Colors.black26),
                        ),
                        Positioned(
                          top: 18,
                          left: 0,
                          right: 0,
                          child: Container(height: 1, color: Colors.black26),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 12),
                  const Icon(
                    Icons.contactless_rounded,
                    color: Colors.white70,
                    size: 24,
                  ),
                ],
              ),
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.white.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      cuenta?.estatus ?? 'ACTIVA',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                        letterSpacing: 0.5,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  const Text(
                    'GestoPago',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      letterSpacing: 0.5,
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
                  const Text(
                    'Saldo disponible',
                    style: TextStyle(
                      color: Colors.white60,
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
                  fontSize: 26,
                  fontWeight: FontWeight.w800,
                  letterSpacing: -0.5,
                ),
              ),
            ],
          ),

          // Fila inferior: Número de Cuenta y Titular
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'TITULAR DE CUENTA',
                      style: TextStyle(
                        color: Colors.white38,
                        fontSize: 9,
                        fontWeight: FontWeight.w700,
                        letterSpacing: 0.8,
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
                child: Row(
                  children: [
                    Text(
                      _formatearNumeroCuenta(cuenta?.numeroCuenta),
                      style: const TextStyle(
                        color: Colors.white70,
                        fontSize: 13,
                        fontFamily: 'monospace',
                        fontWeight: FontWeight.w600,
                        letterSpacing: 1,
                      ),
                    ),
                    const SizedBox(width: 6),
                    const Icon(
                      Icons.copy_rounded,
                      size: 14,
                      color: Colors.white60,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}


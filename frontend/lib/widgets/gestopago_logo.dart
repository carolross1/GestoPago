import 'dart:math' as math;
import 'package:flutter/material.dart';
import '../core/theme/app_colors.dart';

/// Logotipo vectorial minimalista de GestoPago (monograma geométrico "G" con trazo de flujo de pago).
/// Sin iconos genéricos: identidad visual estilo neo-banco moderno.
class GestoPagoLogo extends StatelessWidget {
  final double size;
  final bool showWordmark;
  final bool lightOnDark;
  final String? subtitle;

  const GestoPagoLogo({
    super.key,
    this.size = 64,
    this.showWordmark = false,
    this.lightOnDark = false,
    this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    final badge = Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFF1E1035),
            Color(0xFF4C1D95),
            Color(0xFF7C3AED),
          ],
        ),
        borderRadius: BorderRadius.circular(size * 0.28),
        border: Border.all(
          color: Colors.white.withValues(alpha: lightOnDark ? 0.28 : 0.35),
          width: 1.2,
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF7C3AED).withValues(alpha: lightOnDark ? 0.45 : 0.30),
            blurRadius: size * 0.42,
            spreadRadius: -size * 0.06,
            offset: Offset(0, size * 0.16),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(size * 0.28),
        child: CustomPaint(
          painter: _GestoPagoMarkPainter(),
        ),
      ),
    );

    if (!showWordmark) return badge;

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        badge,
        SizedBox(height: size * 0.24),
        RichText(
          textAlign: TextAlign.center,
          text: TextSpan(
            style: TextStyle(
              fontSize: 27,
              fontWeight: FontWeight.w800,
              letterSpacing: -0.9,
              color: lightOnDark ? Colors.white : AppColors.textPrimary,
            ),
            children: [
              const TextSpan(text: 'Gesto'),
              TextSpan(
                text: 'Pago',
                style: TextStyle(
                  color: lightOnDark ? const Color(0xFFC4B5FD) : AppColors.accent,
                ),
              ),
            ],
          ),
        ),
        if (subtitle != null && subtitle!.isNotEmpty) ...[
          const SizedBox(height: 6),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: lightOnDark
                  ? Colors.white.withValues(alpha: 0.10)
                  : AppColors.accentSoft.withValues(alpha: 0.85),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(
                color: lightOnDark
                    ? Colors.white.withValues(alpha: 0.16)
                    : AppColors.accent.withValues(alpha: 0.18),
              ),
            ),
            child: Text(
              subtitle!.toUpperCase(),
              style: TextStyle(
                fontSize: 10,
                fontWeight: FontWeight.w800,
                letterSpacing: 1.6,
                color: lightOnDark ? const Color(0xFFDDD6FE) : AppColors.accent,
              ),
            ),
          ),
        ],
      ],
    );
  }
}

class _GestoPagoMarkPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;
    final cx = w * 0.5;
    final cy = h * 0.5;

    // Brillo ambiental superior sutil dentro del badge
    final glowPaint = Paint()
      ..shader = RadialGradient(
        center: const Alignment(-0.55, -0.65),
        radius: 1.1,
        colors: [
          Colors.white.withValues(alpha: 0.22),
          Colors.transparent,
        ],
      ).createShader(Rect.fromLTWH(0, 0, w, h));
    canvas.drawRect(Rect.fromLTWH(0, 0, w, h), glowPaint);

    // Anillo geométrico abierto ("G" minimalista de precisión)
    final radius = w * 0.27;
    final strokeW = w * 0.095;

    final arcPaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeW
      ..strokeCap = StrokeCap.round
      ..shader = const LinearGradient(
        begin: Alignment.topLeft,
        end: Alignment.bottomRight,
        colors: [
          Color(0xFFFFFFFF),
          Color(0xFFEDE9FE),
          Color(0xFFC4B5FD),
        ],
      ).createShader(Rect.fromCircle(center: Offset(cx, cy), radius: radius));

    canvas.drawArc(
      Rect.fromCircle(center: Offset(cx, cy), radius: radius),
      -math.pi * 0.22, // apertura superior derecha
      -math.pi * 1.56, // barrido limpio anti-horario
      false,
      arcPaint,
    );

    // Barra horizontal de la "G" con sensación de velocidad/transferencia
    final barPaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeW
      ..strokeCap = StrokeCap.round
      ..color = Colors.white;

    canvas.drawLine(
      Offset(cx - w * 0.01, cy + h * 0.02),
      Offset(cx + radius * 0.96, cy + h * 0.02),
      barPaint,
    );

    // Destello esmeralda minimalista (pulso de transacción aprobada)
    final dotCenter = Offset(cx + radius * 0.88, cy - radius * 0.72);
    final dotGlow = Paint()
      ..color = const Color(0xFF34D399).withValues(alpha: 0.45)
      ..maskFilter = MaskFilter.blur(BlurStyle.normal, w * 0.08);
    canvas.drawCircle(dotCenter, w * 0.075, dotGlow);

    final dotCore = Paint()..color = const Color(0xFF34D399);
    canvas.drawCircle(dotCenter, w * 0.055, dotCore);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

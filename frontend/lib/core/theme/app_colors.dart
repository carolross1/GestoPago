import 'package:flutter/material.dart';

/// Paleta de diseño personalizada en Tonos Lila & Violeta Profundo combinado con Acentos Esmeralda.
/// Look premium moderno estilo NeoBanco / Super-App Financiera.
class AppColors {
  // Fondos principales
  static const Color background = Color(0xFFF9F8FF); // Perla con suave tinte lila
  static const Color surface = Color(0xFFFFFFFF);    // Blanco Puro
  static const Color surfaceElevated = Color(0xFFF5F3FF); // Lila pastel suave 50

  // Primario y Acentos de Marca (Lila & Violeta)
  static const Color primary = Color(0xFF2E1065);      // Violeta oscuro bancario (Violet 950)
  static const Color primaryLight = Color(0xFF4C1D95); // Violeta 900
  static const Color accent = Color(0xFF7C3AED);       // Lila vibrante corporativo (Violet 600)
  static const Color accentLight = Color(0xFFA78BFA);  // Lila luminoso (Violet 400)
  static const Color accentSoft = Color(0xFFEDE9FE);   // Lila pastel de fondo (Violet 100)

  // Tarjeta Bancaria y Gradientes
  static const LinearGradient cardGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFF2E1065), // Violeta profundo noche
      Color(0xFF5B21B6), // Púrpura real
      Color(0xFF7C3AED), // Lila brillante
    ],
  );

  static const LinearGradient accentGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFF7C3AED),
      Color(0xFF8B5CF6),
    ],
  );

  // Estados financieros
  static const Color success = Color(0xFF059669); // Esmeralda FinTech
  static const Color successBg = Color(0xFFECFDF5);
  static const Color warning = Color(0xFFD97706); // Ámbar dorado
  static const Color warningBg = Color(0xFFFFFBEB);
  static const Color error = Color(0xFFDC2626);   // Rojo seguridad
  static const Color errorBg = Color(0xFFFEF2F2);
  static const Color info = Color(0xFF6366F1);    // Índigo moderno

  // Textos y Bordes
  static const Color textPrimary = Color(0xFF1E1B4B);   // Texto oscuro con profundidad violeta
  static const Color textSecondary = Color(0xFF6B7280); // Texto secundario
  static const Color textMuted = Color(0xFF9CA3AF);     // Texto atenuado
  static const Color border = Color(0xFFEDE9FE);        // Borde suave lila
  static const Color borderFocus = Color(0xFF7C3AED);   // Foco activo en lila
}

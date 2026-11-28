import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/services/biometric_service.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/form_validators.dart';
import '../../providers/auth_provider.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';
import '../../widgets/gestopago_logo.dart';
import '../admin/admin_abonos_screen.dart';
import '../home/home_screen.dart';
import 'crear_cuenta_screen.dart';
import 'registro_portal_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _correoController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _isLoading = false;
  bool _canUseBiometric = false;
  String? _maskedBiometricEmail;

  @override
  void initState() {
    super.initState();
    _verificarBiometriaDisponible();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final notice = authProvider.consumeSessionClosedNotice();
      if (notice != null && notice.isNotEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Row(
              children: [
                const Icon(Icons.devices_rounded, color: Colors.white, size: 18),
                const SizedBox(width: 10),
                Expanded(child: Text(notice)),
              ],
            ),
            backgroundColor: AppColors.error,
            behavior: SnackBarBehavior.floating,
            duration: const Duration(seconds: 6),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
        );
      }
    });
  }

  Future<void> _verificarBiometriaDisponible() async {
    final disponible = await BiometricService.instance.canUseBiometricLogin();
    final correoEnmascarado =
        disponible ? await BiometricService.instance.getSavedEmailMasked() : null;
    if (!mounted) return;
    setState(() {
      _canUseBiometric = disponible;
      _maskedBiometricEmail = correoEnmascarado;
    });
  }

  @override
  void dispose() {
    _correoController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleBiometricLogin() async {
    if (_isLoading) return;

    final authResult = await BiometricService.instance.authenticate(
      reason: 'Verifica tu huella digital para entrar a GestoPago',
    );

    if (!mounted) return;

    if (!authResult.success) {
      if (authResult.errorMessage != null) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(authResult.errorMessage!),
            backgroundColor: AppColors.error,
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          ),
        );
      }
      return;
    }

    final creds = await BiometricService.instance.getSavedCredentials();
    if (!mounted) return;

    if (creds == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('Inicia sesión con tu contraseña una vez para vincular tu huella.'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        ),
      );
      setState(() => _canUseBiometric = false);
      return;
    }

    setState(() => _isLoading = true);
    final authProvider = Provider.of<AuthProvider>(context, listen: false);
    final success = await authProvider.login(creds.correo, creds.password);
    if (!mounted) return;
    setState(() => _isLoading = false);

    if (success) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (_) => const HomeScreen()),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(authProvider.errorMessage ?? 'Error al iniciar sesión con huella'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        ),
      );
    }
  }

  Future<void> _handleLogin() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);
    final authProvider = Provider.of<AuthProvider>(context, listen: false);

    final success = await authProvider.login(
      _correoController.text.trim(),
      _passwordController.text,
    );

    setState(() => _isLoading = false);

    if (!mounted) return;

    if (success) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (_) => const HomeScreen()),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(authProvider.errorMessage ?? 'Error al iniciar sesión'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: Stack(
        children: [
          // Fondo ambiental suave estilo Neo-Banco (esferas difusas lila/violeta)
          Positioned(
            top: -110,
            right: -90,
            child: Container(
              width: 280,
              height: 280,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    const Color(0xFF7C3AED).withValues(alpha: 0.16),
                    const Color(0xFFA78BFA).withValues(alpha: 0.04),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
          Positioned(
            bottom: -120,
            left: -100,
            child: Container(
              width: 320,
              height: 320,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    const Color(0xFF8B5CF6).withValues(alpha: 0.13),
                    const Color(0xFFEDE9FE).withValues(alpha: 0.03),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),

          SafeArea(
            child: Center(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 28.0),
                child: ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 430),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        // Logotipo vectorial GestoPago + Wordmark
                        const Center(
                          child: GestoPagoLogo(
                            size: 68,
                            showWordmark: true,
                            subtitle: 'Banca Digital',
                          ),
                        ),
                        const SizedBox(height: 12),
                        const Text(
                          'Gestiona y paga tus servicios al instante con seguridad biométrica',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 13.5,
                            color: AppColors.textSecondary,
                            height: 1.45,
                          ),
                        ),
                        const SizedBox(height: 28),

                        // Card de Formulario Glass-White Elevada
                        Container(
                          padding: const EdgeInsets.all(24),
                          decoration: BoxDecoration(
                            color: Colors.white.withValues(alpha: 0.94),
                            borderRadius: BorderRadius.circular(26),
                            border: Border.all(
                              color: const Color(0xFFE9E3FF),
                              width: 1.2,
                            ),
                            boxShadow: [
                              BoxShadow(
                                color: const Color(0xFF5B21B6).withValues(alpha: 0.08),
                                blurRadius: 34,
                                spreadRadius: -6,
                                offset: const Offset(0, 14),
                              ),
                              BoxShadow(
                                color: Colors.black.withValues(alpha: 0.02),
                                blurRadius: 8,
                                offset: const Offset(0, 2),
                              ),
                            ],
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              const Text(
                                'Inicia sesión',
                                style: TextStyle(
                                  fontSize: 18,
                                  fontWeight: FontWeight.w800,
                                  color: AppColors.textPrimary,
                                  letterSpacing: -0.4,
                                ),
                              ),
                              const SizedBox(height: 4),
                              const Text(
                                'Ingresa tus credenciales de acceso al portal',
                                style: TextStyle(
                                  fontSize: 12.5,
                                  color: AppColors.textSecondary,
                                ),
                              ),
                              const SizedBox(height: 20),
                              CustomTextField(
                                label: 'Correo electrónico',
                                hint: 'tu.correo@ejemplo.com',
                                controller: _correoController,
                                keyboardType: TextInputType.emailAddress,
                                prefixIcon: Icons.alternate_email_rounded,
                                validator: FormValidators.correo,
                              ),
                              const SizedBox(height: 16),
                              CustomTextField(
                                label: 'Contraseña',
                                hint: 'Mínimo 8 caracteres',
                                controller: _passwordController,
                                isPassword: true,
                                prefixIcon: Icons.lock_outline_rounded,
                                validator: FormValidators.password,
                              ),
                              const SizedBox(height: 24),
                              CustomButton(
                                text: 'Iniciar Sesión',
                                icon: Icons.arrow_forward_rounded,
                                isLoading: _isLoading,
                                onPressed: _handleLogin,
                              ),
                              if (_canUseBiometric) ...[
                                const SizedBox(height: 12),
                                OutlinedButton.icon(
                                  onPressed: _isLoading ? null : _handleBiometricLogin,
                                  icon: const Icon(
                                    Icons.fingerprint_rounded,
                                    size: 22,
                                    color: AppColors.accent,
                                  ),
                                  label: Text(
                                    _maskedBiometricEmail != null
                                        ? 'Entrar con huella ($_maskedBiometricEmail)'
                                        : 'Entrar con huella digital',
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w700,
                                      fontSize: 13,
                                      color: AppColors.accent,
                                    ),
                                  ),
                                  style: OutlinedButton.styleFrom(
                                    padding: const EdgeInsets.symmetric(
                                      vertical: 14,
                                      horizontal: 16,
                                    ),
                                    side: BorderSide(
                                      color: AppColors.accent.withValues(alpha: 0.35),
                                      width: 1.3,
                                    ),
                                    backgroundColor: AppColors.accentSoft.withValues(alpha: 0.45),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(16),
                                    ),
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),

                        const SizedBox(height: 20),

                        // Crear Cuenta Nueva
                        SizedBox(
                          width: double.infinity,
                          child: OutlinedButton.icon(
                            onPressed: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (_) => const CrearCuentaScreen(),
                                ),
                              );
                            },
                            icon: const Icon(
                              Icons.person_add_alt_1_rounded,
                              size: 19,
                              color: AppColors.textPrimary,
                            ),
                            label: const Text(
                              '¿Eres nuevo? Crear Cuenta',
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w700,
                                color: AppColors.textPrimary,
                              ),
                            ),
                            style: OutlinedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 15),
                              side: const BorderSide(color: Color(0xFFE5DEFF), width: 1.3),
                              backgroundColor: Colors.white.withValues(alpha: 0.9),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(16),
                              ),
                            ),
                          ),
                        ),

                        const SizedBox(height: 14),

                        // Opciones de Activación
                        Wrap(
                          alignment: WrapAlignment.center,
                          crossAxisAlignment: WrapCrossAlignment.center,
                          spacing: 4,
                          runSpacing: 4,
                          children: [
                            const Text(
                              '¿Ya tienes cuenta pero sin contraseña?',
                              style: TextStyle(
                                fontSize: 12.5,
                                color: AppColors.textSecondary,
                              ),
                            ),
                            GestureDetector(
                              onTap: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (_) => const RegistroPortalScreen(),
                                  ),
                                );
                              },
                              child: const Text(
                                'Activar portal',
                                style: TextStyle(
                                  fontSize: 12.5,
                                  fontWeight: FontWeight.w800,
                                  color: AppColors.accent,
                                ),
                              ),
                            ),
                          ],
                        ),

                        const SizedBox(height: 22),

                        // Sello discreto de seguridad
                        const Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.verified_user_outlined,
                              size: 14,
                              color: AppColors.success,
                            ),
                            SizedBox(width: 6),
                            Text(
                              'Conexión cifrada • Biometría & 2FA',
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w600,
                                color: AppColors.textMuted,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 12),
                        TextButton.icon(
                          onPressed: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => const AdminAbonosScreen(),
                              ),
                            );
                          },
                          icon: const Icon(
                            Icons.admin_panel_settings_outlined,
                            size: 15,
                            color: AppColors.textMuted,
                          ),
                          label: const Text(
                            'Acceso Administrativo',
                            style: TextStyle(
                              fontSize: 11.5,
                              fontWeight: FontWeight.w700,
                              color: AppColors.textMuted,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}


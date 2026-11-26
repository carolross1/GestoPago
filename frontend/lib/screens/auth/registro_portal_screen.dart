import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/auth_provider.dart';
import '../../widgets/custom_button.dart';
import '../../widgets/custom_text_field.dart';

class RegistroPortalScreen extends StatefulWidget {
  const RegistroPortalScreen({super.key});

  @override
  State<RegistroPortalScreen> createState() => _RegistroPortalScreenState();
}

class _RegistroPortalScreenState extends State<RegistroPortalScreen> {
  final _formKey = GlobalKey<FormState>();
  final _correoController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  bool _isLoading = false;

  @override
  void dispose() {
    _correoController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _handleRegistro() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);
    final authProvider = Provider.of<AuthProvider>(context, listen: false);

    final success = await authProvider.registrarPortal(
      _correoController.text.trim(),
      _passwordController.text,
    );

    setState(() => _isLoading = false);

    if (!mounted) return;

    if (success) {
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (ctx) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
          title: const Row(
            children: [
              Icon(Icons.check_circle_rounded, color: AppColors.success, size: 28),
              SizedBox(width: 10),
              Text('¡Registro Exitoso!', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            ],
          ),
          content: const Text(
            'Tu contraseña de acceso ha sido registrada exitosamente. Ya puedes iniciar sesión con tu cuenta.',
            style: TextStyle(fontSize: 14, color: AppColors.textSecondary),
          ),
          actions: [
            ElevatedButton(
              onPressed: () {
                Navigator.pop(ctx);
                Navigator.pop(context); // Regresar al Login
              },
              child: const Text('Ir a Iniciar Sesión'),
            ),
          ],
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(authProvider.errorMessage ?? 'Error al registrar contraseña'),
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Activar Portal Cliente'),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 24.0),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Text(
                      'Crea tu contraseña de portal',
                      style: TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                        letterSpacing: -0.5,
                      ),
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Ingresa el correo electrónico que tienes registrado en tu cuenta de cliente para crear tu clave de acceso seguro.',
                      style: TextStyle(
                        fontSize: 14,
                        color: AppColors.textSecondary,
                        height: 1.4,
                      ),
                    ),
                    const SizedBox(height: 28),

                    Container(
                      padding: const EdgeInsets.all(24),
                      decoration: BoxDecoration(
                        color: AppColors.surface,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(color: AppColors.border, width: 1),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          CustomTextField(
                            label: 'Correo registrado',
                            hint: 'ejemplo@banca.com',
                            controller: _correoController,
                            keyboardType: TextInputType.emailAddress,
                            prefixIcon: Icons.mail_outline_rounded,
                            validator: (val) {
                              if (val == null || val.trim().isEmpty) {
                                return 'Ingresa tu correo';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 20),
                          CustomTextField(
                            label: 'Nueva contraseña',
                            hint: 'Mínimo 8 caracteres',
                            controller: _passwordController,
                            isPassword: true,
                            prefixIcon: Icons.lock_outline_rounded,
                            validator: (val) {
                              if (val == null || val.length < 8) {
                                return 'Mínimo 8 caracteres';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 20),
                          CustomTextField(
                            label: 'Confirmar contraseña',
                            hint: 'Repite tu nueva contraseña',
                            controller: _confirmPasswordController,
                            isPassword: true,
                            prefixIcon: Icons.lock_reset_rounded,
                            validator: (val) {
                              if (val != _passwordController.text) {
                                return 'Las contraseñas no coinciden';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 28),
                          CustomButton(
                            text: 'Confirmar y Activar',
                            isLoading: _isLoading,
                            onPressed: _handleRegistro,
                          ),
                        ],
                      ),
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


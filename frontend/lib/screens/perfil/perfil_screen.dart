import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/auth_provider.dart';
import '../../providers/cuenta_provider.dart';
import '../auth/login_screen.dart';

class PerfilScreen extends StatelessWidget {
  final bool isEmbedded;
  const PerfilScreen({super.key, this.isEmbedded = false});

  String _enmascararCuenta(String? cuenta) {
    if (cuenta == null || cuenta.isEmpty) return 'No asignada';
    if (cuenta.length <= 4) return cuenta;
    return '•••• ${cuenta.substring(cuenta.length - 4)}';
  }

  Widget _buildSeccionItem({
    required IconData icon,
    required String titulo,
    required String subtitulo,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10.0),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: AppColors.surfaceElevated,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, size: 20, color: AppColors.textPrimary),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  titulo,
                  style: const TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  subtitulo,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: AppColors.textPrimary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final auth = Provider.of<AuthProvider>(context);
    final cuentaProv = Provider.of<CuentaProvider>(context);
    final cliente = auth.currentCliente;
    final cuenta = cuentaProv.cuenta;

    final Widget bodyContent = SafeArea(
      child: SingleChildScrollView(
        padding: const EdgeInsets.only(left: 20, right: 20, top: 16, bottom: 96),
        child: Column(
          children: [
              // Avatar y nombre
              Center(
                child: Column(
                  children: [
                    CircleAvatar(
                      radius: 42,
                      backgroundColor: AppColors.primary,
                      child: Text(
                        cliente?.nombre.isNotEmpty == true
                            ? cliente!.nombre[0].toUpperCase()
                            : 'C',
                        style: const TextStyle(
                          fontSize: 34,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ),
                    const SizedBox(height: 14),
                    Text(
                      cliente?.nombreCompleto ?? 'Cliente GestoPago',
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      cliente?.correo ?? '',
                      style: const TextStyle(
                        fontSize: 13,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 28),

              // Datos de la Cuenta
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: AppColors.border),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Información de Cuenta',
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 10),
                    _buildSeccionItem(
                      icon: Icons.credit_card_rounded,
                      titulo: 'Número de Cuenta',
                      subtitulo: _enmascararCuenta(cuenta?.numeroCuenta),
                    ),
                    const Divider(),
                    _buildSeccionItem(
                      icon: Icons.calendar_today_rounded,
                      titulo: 'Fecha de Apertura',
                      subtitulo: cuenta?.fechaApertura ?? '2026/10/02',
                    ),
                    const Divider(),
                    _buildSeccionItem(
                      icon: Icons.check_circle_outline_rounded,
                      titulo: 'Estatus de Cuenta',
                      subtitulo: cuenta?.estatus ?? 'ACTIVA',
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 18),

              // Datos Personales
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: AppColors.border),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Datos de Identidad (Cifrados)',
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 10),
                    _buildSeccionItem(
                      icon: Icons.badge_outlined,
                      titulo: 'CURP',
                      subtitulo: cliente?.curp ?? '••••••••••••••••••',
                    ),
                    const Divider(),
                    _buildSeccionItem(
                      icon: Icons.receipt_outlined,
                      titulo: 'RFC',
                      subtitulo: cliente?.rfc ?? '•••••••••••••',
                    ),
                    const Divider(),
                    _buildSeccionItem(
                      icon: Icons.public_rounded,
                      titulo: 'Nacionalidad',
                      subtitulo: cliente?.nacionalidadNombre ?? 'México (MEX)',
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),

              // Botón Cerrar Sesión
              SizedBox(
                width: double.infinity,
                height: 52,
                child: OutlinedButton.icon(
                  onPressed: () {
                    showDialog(
                      context: context,
                      builder: (ctx) => AlertDialog(
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(18)),
                        title: const Text('¿Cerrar Sesión?'),
                        content: const Text(
                            'Tendrás que ingresar nuevamente tus credenciales para acceder.'),
                        actions: [
                          TextButton(
                            onPressed: () => Navigator.pop(ctx),
                            child: const Text('Cancelar'),
                          ),
                          ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppColors.error,
                            ),
                            onPressed: () async {
                              Navigator.pop(ctx);
                              await auth.logout();
                              if (context.mounted) {
                                Navigator.pushAndRemoveUntil(
                                  context,
                                  MaterialPageRoute(
                                      builder: (_) => const LoginScreen()),
                                  (route) => false,
                                );
                              }
                            },
                            child: const Text('Cerrar Sesión'),
                          ),
                        ],
                      ),
                    );
                  },
                  icon: const Icon(Icons.logout_rounded, color: AppColors.error, size: 20),
                  label: const Text(
                    'Cerrar Sesión',
                    style: TextStyle(
                      color: AppColors.error,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  style: OutlinedButton.styleFrom(
                    side: const BorderSide(color: AppColors.error, width: 1.2),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 20),
            ],
          ),
        ),
      );

    if (isEmbedded) {
      return bodyContent;
    }

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Mi Perfil y Seguridad'),
      ),
      body: bodyContent,
    );
  }
}


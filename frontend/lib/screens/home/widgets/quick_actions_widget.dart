import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';

class QuickActionsWidget extends StatelessWidget {
  final VoidCallback onPagarServicios;
  final VoidCallback onRecargas;
  final VoidCallback onTransferir;
  final VoidCallback onExplorarCatalogo;

  const QuickActionsWidget({
    super.key,
    required this.onPagarServicios,
    required this.onRecargas,
    required this.onTransferir,
    required this.onExplorarCatalogo,
  });

  Widget _buildItem({
    required String title,
    required IconData icon,
    required Color color,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 8),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.border, width: 1),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.02),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: color.withValues(alpha: 0.12),
                shape: BoxShape.circle,
              ),
              child: Icon(icon, color: color, size: 22),
            ),
            const SizedBox(height: 10),
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return GridView.count(
      crossAxisCount: 4,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisSpacing: 10,
      mainAxisSpacing: 10,
      children: [
        _buildItem(
          title: 'Servicios',
          icon: Icons.receipt_long_rounded,
          color: AppColors.accent,
          onTap: onPagarServicios,
        ),
        _buildItem(
          title: 'Recargas',
          icon: Icons.phone_android_rounded,
          color: const Color(0xFF6366F1), // Indigo
          onTap: onRecargas,
        ),
        _buildItem(
          title: 'Transferir',
          icon: Icons.swap_horiz_rounded,
          color: AppColors.success,
          onTap: onTransferir,
        ),
        _buildItem(
          title: 'Catálogo',
          icon: Icons.grid_view_rounded,
          color: AppColors.primary,
          onTap: onExplorarCatalogo,
        ),
      ],
    );
  }
}


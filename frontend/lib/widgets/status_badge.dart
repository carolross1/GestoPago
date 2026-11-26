import 'package:flutter/material.dart';
import '../core/theme/app_colors.dart';

class StatusBadge extends StatelessWidget {
  final String text;
  final bool isSuccess;
  final bool isWarning;

  const StatusBadge({
    super.key,
    required this.text,
    this.isSuccess = true,
    this.isWarning = false,
  });

  @override
  Widget build(BuildContext context) {
    Color bg;
    Color textColor;

    if (isWarning) {
      bg = AppColors.warningBg;
      textColor = AppColors.warning;
    } else if (isSuccess) {
      bg = AppColors.successBg;
      textColor = AppColors.success;
    } else {
      bg = AppColors.errorBg;
      textColor = AppColors.error;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: textColor.withValues(alpha: 0.3), width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 6,
            height: 6,
            decoration: BoxDecoration(
              color: textColor,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 6),
          Text(
            text,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: textColor,
            ),
          ),
        ],
      ),
    );
  }
}


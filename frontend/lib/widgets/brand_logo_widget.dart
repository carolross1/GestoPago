import 'package:flutter/material.dart';

class BrandLogoInfo {
  final String nombre;
  final String shortName;
  final String key;
  final Color primaryColor;
  final Color secondaryColor;
  final IconData icon;
  final int? gestoId;

  const BrandLogoInfo({
    required this.nombre,
    required this.shortName,
    required this.key,
    required this.primaryColor,
    required this.secondaryColor,
    required this.icon,
    this.gestoId,
  });
}

class BrandLogoWidget extends StatelessWidget {
  final String servicio;
  final double size;
  final double borderRadius;
  final String? urlLogo;
  final int? idServicio;

  const BrandLogoWidget({
    super.key,
    required this.servicio,
    this.size = 48,
    this.borderRadius = 14,
    this.urlLogo,
    this.idServicio,
  });

  static BrandLogoInfo getBrandInfo(String servicio) {
    final s = servicio.toUpperCase().trim();
    if (s.contains('TELCEL')) {
      return const BrandLogoInfo(
        nombre: 'Telcel',
        shortName: 'Telcel',
        key: 'telcel',
        primaryColor: Color(0xFF002F6C),
        secondaryColor: Color(0xFF001F48),
        icon: Icons.phone_android_rounded,
        gestoId: 133,
      );
    }
    if (s.contains('AT&T') || s.contains('ATT')) {
      return const BrandLogoInfo(
        nombre: 'AT&T',
        shortName: 'AT&T',
        key: 'att',
        primaryColor: Color(0xFF00A8E0),
        secondaryColor: Color(0xFF007FA8),
        icon: Icons.signal_cellular_alt_rounded,
        gestoId: 115,
      );
    }
    if (s.contains('MOVISTAR')) {
      return const BrandLogoInfo(
        nombre: 'Movistar',
        shortName: 'Movistar',
        key: 'movistar',
        primaryColor: Color(0xFF019BDE),
        secondaryColor: Color(0xFF0073A8),
        icon: Icons.phonelink_ring_rounded,
        gestoId: 102,
      );
    }
    if (s.contains('TELMEX')) {
      return const BrandLogoInfo(
        nombre: 'Telmex',
        shortName: 'Telmex',
        key: 'telmex',
        primaryColor: Color(0xFF0072CE),
        secondaryColor: Color(0xFF00549A),
        icon: Icons.wifi_rounded,
        gestoId: 107,
      );
    }
    if (s.contains('NATURGY') || s.contains('GAS NATURAL')) {
      return const BrandLogoInfo(
        nombre: 'Naturgy',
        shortName: 'Naturgy',
        key: 'naturgy',
        primaryColor: Color(0xFFE84D0E),
        secondaryColor: Color(0xFFB53805),
        icon: Icons.local_fire_department_rounded,
        gestoId: 64,
      );
    }
    if (s.contains('TOTALPLAY')) {
      return const BrandLogoInfo(
        nombre: 'Totalplay',
        shortName: 'Totalplay',
        key: 'totalplay',
        primaryColor: Color(0xFFEC008C),
        secondaryColor: Color(0xFFB3006A),
        icon: Icons.router_rounded,
        gestoId: 85,
      );
    }
    if (s.contains('IZZI')) {
      return const BrandLogoInfo(
        nombre: 'Izzi',
        shortName: 'izzi',
        key: 'izzi',
        primaryColor: Color(0xFFD81E05),
        secondaryColor: Color(0xFFA11400),
        icon: Icons.connected_tv_rounded,
        gestoId: 76,
      );
    }
    if (s.contains('AGUA')) {
      return const BrandLogoInfo(
        nombre: 'Agua',
        shortName: 'Agua',
        key: 'agua',
        primaryColor: Color(0xFF0288D1),
        secondaryColor: Color(0xFF01579B),
        icon: Icons.water_drop_rounded,
        gestoId: 55,
      );
    }
    if (s.contains('TELEVIA')) {
      return const BrandLogoInfo(
        nombre: 'Televía',
        shortName: 'Televía',
        key: 'televia',
        primaryColor: Color(0xFF00953B),
        secondaryColor: Color(0xFF006B2B),
        icon: Icons.directions_car_filled_rounded,
        gestoId: 38,
      );
    }
    if (s.contains('CFE')) {
      return const BrandLogoInfo(
        nombre: 'CFE',
        shortName: 'CFE',
        key: 'cfe',
        primaryColor: Color(0xFF008000),
        secondaryColor: Color(0xFF005A00),
        icon: Icons.bolt_rounded,
        gestoId: 166,
      );
    }
    if (s.contains('SKY')) {
      return const BrandLogoInfo(
        nombre: 'Sky',
        shortName: 'Sky',
        key: 'sky',
        primaryColor: Color(0xFF00205B),
        secondaryColor: Color(0xFF001236),
        icon: Icons.tv_rounded,
        gestoId: 168,
      );
    }
    return BrandLogoInfo(
      nombre: servicio.isNotEmpty ? servicio : 'Servicio',
      shortName: servicio.length > 6 ? servicio.substring(0, 6) : servicio,
      key: '',
      primaryColor: const Color(0xFF7C3AED),
      secondaryColor: const Color(0xFF2E1065),
      icon: Icons.receipt_long_rounded,
      gestoId: null,
    );
  }

  Widget _buildBadge(BrandLogoInfo info) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [info.primaryColor, info.secondaryColor],
        ),
      ),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              info.icon,
              color: Colors.white,
              size: size * 0.42,
            ),
            if (size >= 44) ...[
              const SizedBox(height: 1),
              Text(
                info.shortName,
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w900,
                  fontSize: size * 0.20,
                  letterSpacing: -0.2,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final info = getBrandInfo(servicio);
    final String? networkUrl = (urlLogo != null && urlLogo!.isNotEmpty)
        ? urlLogo
        : (idServicio != null && idServicio! > 0
            ? 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/$idServicio.png'
            : (info.gestoId != null
                ? 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/${info.gestoId}.png'
                : null));

    Widget buildFallback() {
      if (info.key.isNotEmpty) {
        return Image.asset(
          'assets/logos/${info.key}.png',
          fit: BoxFit.contain,
          errorBuilder: (_, error2, stack2) => _buildBadge(info),
        );
      }
      return _buildBadge(info);
    }

    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(borderRadius),
        boxShadow: [
          BoxShadow(
            color: info.primaryColor.withValues(alpha: 0.18),
            blurRadius: 8,
            offset: const Offset(0, 3),
          ),
        ],
        border: Border.all(color: const Color(0xFFE2E8F0), width: 1),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(borderRadius - 1),
        child: Padding(
          padding: const EdgeInsets.all(4.0),
          child: networkUrl != null
              ? Image.network(
                  networkUrl,
                  fit: BoxFit.contain,
                  errorBuilder: (_, error, stack) => buildFallback(),
                )
              : buildFallback(),
        ),
      ),
    );
  }
}

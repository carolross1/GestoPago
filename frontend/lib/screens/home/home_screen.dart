import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/utils/currency_formatter.dart';
import '../../providers/auth_provider.dart';
import '../../providers/cuenta_provider.dart';
import '../../providers/pago_provider.dart';
import '../../providers/productos_provider.dart';
import '../../widgets/brand_logo_widget.dart';
import '../admin/admin_abonos_screen.dart';
import '../auth/login_screen.dart';
import '../perfil/perfil_screen.dart';
import '../servicios/catalogo_servicios_screen.dart';
import '../servicios/pago_servicio_screen.dart';
import '../transacciones/historial_screen.dart';
import 'widgets/bank_card_widget.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  int _currentSection = 0; // 0: Inicio, 1: Servicios, 2: Historial, 3: Perfil

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final auth = Provider.of<AuthProvider>(context, listen: false);
      if (auth.currentCliente != null) {
        final cuentaProv = Provider.of<CuentaProvider>(context, listen: false);
        cuentaProv.cargarCuenta(auth.currentCliente!.id).then((_) {
          if (!mounted) return;
          if (cuentaProv.cuenta?.numeroCuenta != null) {
            Provider.of<PagoProvider>(context, listen: false)
                .cargarHistorial(cuentaProv.cuenta!.numeroCuenta);
          }
        });
      }
      Provider.of<ProductosProvider>(context, listen: false).cargarProductos();
    });
  }

  void _abrirSeccion(int seccion) {
    setState(() => _currentSection = seccion);
    Navigator.of(context).pop(); // Cierra el Drawer
  }

  Widget _buildDrawer(
    BuildContext context,
    AuthProvider auth,
    CuentaProvider cuentaProv,
  ) {
    final cliente = auth.currentCliente;
    final nombre = cliente?.nombreCompleto ?? 'Cliente GestoPago';
    final correo = cliente?.correo ?? 'usuario@correo.com';
    final numeroCuenta = cuentaProv.cuenta?.numeroCuenta ?? '•••• •••• ••';

    return Drawer(
      backgroundColor: const Color(0xFF1E1035), // Violeta Noche Profundo
      child: SafeArea(
        child: Column(
          children: [
            // Header del Drawer estilo Apple ID / VIP Banking
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
              decoration: BoxDecoration(
                border: Border(
                  bottom: BorderSide(
                    color: Colors.white.withValues(alpha: 0.1),
                    width: 1,
                  ),
                ),
              ),
              child: Row(
                children: [
                  Container(
                    width: 58,
                    height: 58,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      gradient: const LinearGradient(
                        colors: [Color(0xFFA78BFA), Color(0xFF7C3AED)],
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF7C3AED).withValues(alpha: 0.4),
                          blurRadius: 16,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: Center(
                      child: Text(
                        nombre.isNotEmpty ? nombre[0].toUpperCase() : 'U',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 24,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          nombre,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 16,
                            fontWeight: FontWeight.w800,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 3),
                        Text(
                          correo,
                          style: TextStyle(
                            color: Colors.white.withValues(alpha: 0.65),
                            fontSize: 12,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 6),
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 2.5),
                          decoration: BoxDecoration(
                            color: AppColors.success.withValues(alpha: 0.18),
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(
                              color: AppColors.success.withValues(alpha: 0.35),
                            ),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Container(
                                width: 6,
                                height: 6,
                                decoration: const BoxDecoration(
                                  color: AppColors.success,
                                  shape: BoxShape.circle,
                                ),
                              ),
                              const SizedBox(width: 6),
                              Flexible(
                                child: Text(
                                  numeroCuenta.length > 4
                                      ? 'Cta: •••• ${numeroCuenta.substring(numeroCuenta.length - 4)}'
                                      : 'Cta: $numeroCuenta',
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    color: AppColors.success,
                                    fontSize: 10,
                                    fontWeight: FontWeight.w700,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),

            // Opciones de Navegación del Drawer
            Expanded(
              child: ListView(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 18),
                children: [
                  _buildDrawerItem(
                    icon: Icons.account_balance_wallet_rounded,
                    title: 'Inicio / Wallet',
                    isSelected: _currentSection == 0,
                    onTap: () => _abrirSeccion(0),
                  ),
                  _buildDrawerItem(
                    icon: Icons.grid_view_rounded,
                    title: 'Catálogo de Servicios',
                    isSelected: _currentSection == 1,
                    onTap: () => _abrirSeccion(1),
                  ),
                  _buildDrawerItem(
                    icon: Icons.history_rounded,
                    title: 'Historial de Pagos',
                    isSelected: _currentSection == 2,
                    onTap: () => _abrirSeccion(2),
                  ),
                  _buildDrawerItem(
                    icon: Icons.security_rounded,
                    title: 'Mi Perfil & Seguridad',
                    isSelected: _currentSection == 3,
                    onTap: () => _abrirSeccion(3),
                  ),
                  const Divider(color: Colors.white12, height: 24),
                  _buildDrawerItem(
                    icon: Icons.admin_panel_settings_rounded,
                    title: 'Módulo Admin (Abonos)',
                    isSelected: false,
                    onTap: () {
                      Navigator.of(context).pop();
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (_) => const AdminAbonosScreen()),
                      );
                    },
                  ),
                ],
              ),
            ),

            // Botón de Cerrar Sesión en el Drawer
            Padding(
              padding: const EdgeInsets.all(20),
              child: InkWell(
                borderRadius: BorderRadius.circular(16),
                onTap: () {
                  Navigator.of(context).pop();
                  _confirmarLogout(context, auth);
                },
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(vertical: 14, horizontal: 16),
                  decoration: BoxDecoration(
                    color: AppColors.error.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: AppColors.error.withValues(alpha: 0.3),
                    ),
                  ),
                  child: const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.logout_rounded,
                          color: Color(0xFFF87171), size: 20),
                      SizedBox(width: 10),
                      Text(
                        'Cerrar Sesión',
                        style: TextStyle(
                          color: Color(0xFFF87171),
                          fontWeight: FontWeight.w800,
                          fontSize: 13,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDrawerItem({
    required IconData icon,
    required String title,
    required bool isSelected,
    required VoidCallback onTap,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 6),
      decoration: BoxDecoration(
        color: isSelected
            ? const Color(0xFF7C3AED).withValues(alpha: 0.25)
            : Colors.transparent,
        borderRadius: BorderRadius.circular(14),
        border: isSelected
            ? Border.all(color: const Color(0xFFA78BFA).withValues(alpha: 0.4))
            : null,
      ),
      child: ListTile(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        leading: Icon(
          icon,
          color: isSelected ? const Color(0xFFA78BFA) : Colors.white60,
          size: 22,
        ),
        title: Text(
          title,
          style: TextStyle(
            color: isSelected ? Colors.white : Colors.white70,
            fontSize: 14,
            fontWeight: isSelected ? FontWeight.w800 : FontWeight.w500,
          ),
        ),
        onTap: onTap,
      ),
    );
  }

  void _confirmarLogout(BuildContext context, AuthProvider auth) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(22)),
        title: const Text(
          '¿Cerrar Sesión?',
          style: TextStyle(fontWeight: FontWeight.w800),
        ),
        content: const Text(
            'Tendrás que ingresar nuevamente tus credenciales de GestoPago.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.error,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: () async {
              Navigator.pop(ctx);
              await auth.logout();
              if (context.mounted) {
                Navigator.pushAndRemoveUntil(
                  context,
                  MaterialPageRoute(builder: (_) => const LoginScreen()),
                  (route) => false,
                );
              }
            },
            child: const Text('Cerrar Sesión',
                style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  // Píldora de Acción Rápida Flotante
  Widget _buildActionPill({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
  }) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(18),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 13, horizontal: 10),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(18),
            border: Border.all(
              color: const Color(0xFFEDE9FE),
              width: 1.2,
            ),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF7C3AED).withValues(alpha: 0.08),
                blurRadius: 14,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 38,
                height: 38,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: AppColors.accentSoft,
                ),
                child: Icon(icon, color: AppColors.accent, size: 20),
              ),
              const SizedBox(height: 8),
              Text(
                label,
                style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary,
                ),
                textAlign: TextAlign.center,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildFloatingGlassDock() {
    return Container(
      margin: const EdgeInsets.only(left: 20, right: 20, bottom: 18),
      height: 64,
      decoration: BoxDecoration(
        color: const Color(0xFF1E1035).withValues(alpha: 0.95), // Violeta Noche Cristalino
        borderRadius: BorderRadius.circular(32),
        border: Border.all(
          color: Colors.white.withValues(alpha: 0.16),
          width: 1.2,
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF2E1065).withValues(alpha: 0.4),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _buildDockItem(
            index: 0,
            icon: Icons.account_balance_wallet_rounded,
            label: 'Inicio',
          ),
          _buildDockItem(
            index: 1,
            icon: Icons.grid_view_rounded,
            label: 'Servicios',
          ),
          _buildDockItem(
            index: 2,
            icon: Icons.receipt_long_rounded,
            label: 'Historial',
          ),
          _buildDockItem(
            index: 3,
            icon: Icons.person_rounded,
            label: 'Perfil',
          ),
        ],
      ),
    );
  }

  Widget _buildDockItem({
    required int index,
    required IconData icon,
    required String label,
  }) {
    final isSelected = _currentSection == index;
    return InkWell(
      borderRadius: BorderRadius.circular(24),
      onTap: () => setState(() => _currentSection = index),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: EdgeInsets.symmetric(
          horizontal: isSelected ? 16 : 12,
          vertical: 8,
        ),
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF7C3AED) : Colors.transparent,
          borderRadius: BorderRadius.circular(24),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: const Color(0xFF7C3AED).withValues(alpha: 0.5),
                    blurRadius: 10,
                    offset: const Offset(0, 3),
                  ),
                ]
              : null,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 20,
              color: isSelected ? Colors.white : Colors.white60,
            ),
            if (isSelected) ...[
              const SizedBox(width: 6),
              Text(
                label,
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w800,
                  fontSize: 12,
                  letterSpacing: -0.2,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final auth = Provider.of<AuthProvider>(context);
    final cuentaProv = Provider.of<CuentaProvider>(context);

    return Scaffold(
      key: _scaffoldKey,
      extendBody: true,
      backgroundColor: const Color(0xFFF8F6FF), // Perla con brillo ambiental lila
      drawer: _buildDrawer(context, auth, cuentaProv),
      bottomNavigationBar: _buildFloatingGlassDock(),
      body: _buildCurrentBody(context, auth, cuentaProv),
    );
  }

  Widget _buildCurrentBody(
    BuildContext context,
    AuthProvider auth,
    CuentaProvider cuentaProv,
  ) {
    switch (_currentSection) {
      case 1:
        return Column(
          children: [
            _buildInternalHeader('Catálogo de Servicios'),
            const Expanded(child: CatalogoServiciosScreen(isEmbedded: true)),
          ],
        );
      case 2:
        return Column(
          children: [
            _buildInternalHeader('Historial de Pagos'),
            const Expanded(child: HistorialScreen(isEmbedded: true)),
          ],
        );
      case 3:
        return Column(
          children: [
            _buildInternalHeader('Mi Perfil & Seguridad'),
            const Expanded(child: PerfilScreen(isEmbedded: true)),
          ],
        );
      default:
        return _buildAppleWalletHeroDashboard(context, auth, cuentaProv);
    }
  }

  Widget _buildInternalHeader(String title) {
    return Container(
      color: Colors.white,
      padding: EdgeInsets.only(
        top: MediaQuery.of(context).padding.top + 10,
        bottom: 12,
        left: 16,
        right: 16,
      ),
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.arrow_back_rounded, color: AppColors.textPrimary),
            onPressed: () => setState(() => _currentSection = 0),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w800,
                color: AppColors.textPrimary,
                letterSpacing: -0.3,
              ),
            ),
          ),
        ],
      ),
    );
  }

  // =========================================================================
  // DISTRIBUCIÓN ESTILO APPLE WALLET + HOJA DESLIZABLE (SHEET / DRAWER)
  // =========================================================================
  Widget _buildAppleWalletHeroDashboard(
    BuildContext context,
    AuthProvider auth,
    CuentaProvider cuentaProv,
  ) {
    final cliente = auth.currentCliente;
    final nombre = cliente?.nombre ?? 'Cliente';

    return SafeArea(
      child: RefreshIndicator(
        onRefresh: () async {
          if (cliente != null) {
            await cuentaProv.cargarCuenta(cliente.id);
            if (cuentaProv.cuenta?.numeroCuenta != null && context.mounted) {
              await Provider.of<PagoProvider>(context, listen: false)
                  .cargarHistorial(cuentaProv.cuenta!.numeroCuenta);
            }
          }
          if (!context.mounted) return;
          await Provider.of<ProductosProvider>(context, listen: false)
              .cargarProductos();
        },
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // -------------------------------------------------------------
              // ZONA HERO SUPERIOR (APPLE CARD 3D GLOW + PILL BUTTONS)
              // -------------------------------------------------------------
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
                child: Column(
                  children: [
                    // Barra Superior con Botón Drawer de Hamburguesa y Avatar
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        InkWell(
                          borderRadius: BorderRadius.circular(14),
                          onTap: () => _scaffoldKey.currentState?.openDrawer(),
                          child: Container(
                            width: 44,
                            height: 44,
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(14),
                              border: Border.all(
                                color: const Color(0xFFEDE9FE),
                                width: 1.2,
                              ),
                              boxShadow: [
                                BoxShadow(
                                  color: const Color(0xFF7C3AED)
                                      .withValues(alpha: 0.08),
                                  blurRadius: 10,
                                  offset: const Offset(0, 3),
                                ),
                              ],
                            ),
                            child: const Icon(
                              Icons.menu_rounded,
                              color: Color(0xFF2E1065),
                              size: 24,
                            ),
                          ),
                        ),
                        Expanded(
                          child: Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 10),
                            child: Column(
                              children: [
                                Text(
                                  'Hola, $nombre',
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    fontSize: 17,
                                    fontWeight: FontWeight.w800,
                                    color: Color(0xFF1E1B4B),
                                    letterSpacing: -0.4,
                                  ),
                                ),
                                const Text(
                                  'GestoPago Banca Digital',
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                    fontSize: 11,
                                    fontWeight: FontWeight.w600,
                                    color: Color(0xFF7C3AED),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                        InkWell(
                          borderRadius: BorderRadius.circular(14),
                          onTap: () => setState(() => _currentSection = 3),
                          child: Container(
                            width: 44,
                            height: 44,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              gradient: const LinearGradient(
                                colors: [Color(0xFFA78BFA), Color(0xFF7C3AED)],
                              ),
                              boxShadow: [
                                BoxShadow(
                                  color: const Color(0xFF7C3AED)
                                      .withValues(alpha: 0.35),
                                  blurRadius: 10,
                                  offset: const Offset(0, 3),
                                ),
                              ],
                            ),
                            child: Center(
                              child: Text(
                                nombre.isNotEmpty
                                    ? nombre[0].toUpperCase()
                                    : 'U',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 17,
                                  fontWeight: FontWeight.w900,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 18),

                    // Tarjeta Bancaria 3D Apple Card con Glow Holográfico
                    BankCardWidget(
                      cuenta: cuentaProv.cuenta,
                      cliente: cliente,
                      mostrarSaldo: cuentaProv.mostrarSaldo,
                      onToggleSaldo: cuentaProv.toggleMostrarSaldo,
                    ),

                    const SizedBox(height: 18),

                    // Botones Flotantes Tipo Píldora (Pill Buttons)
                    Row(
                      children: [
                        _buildActionPill(
                          icon: Icons.credit_card_rounded,
                          label: 'Pagar',
                          onTap: () => setState(() => _currentSection = 1),
                        ),
                        const SizedBox(width: 12),
                        _buildActionPill(
                          icon: Icons.phone_android_rounded,
                          label: 'Recargas',
                          onTap: () {
                            Provider.of<ProductosProvider>(context,
                                    listen: false)
                                .seleccionarCategoria(1);
                            setState(() => _currentSection = 1);
                          },
                        ),
                        const SizedBox(width: 12),
                        _buildActionPill(
                          icon: Icons.receipt_long_rounded,
                          label: 'Historial',
                          onTap: () => setState(() => _currentSection = 2),
                        ),
                      ],
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 10),

              // -------------------------------------------------------------
              // ZONA HOJA DESLIZABLE CURVADA (BOTTOM SHEET CURVED CONTAINER)
              // -------------------------------------------------------------
              Container(
                width: double.infinity,
                padding: const EdgeInsets.only(top: 14, left: 20, right: 20, bottom: 90),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: const BorderRadius.vertical(top: Radius.circular(32)),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFF7C3AED).withValues(alpha: 0.08),
                      blurRadius: 26,
                      spreadRadius: -4,
                      offset: const Offset(0, -10),
                    ),
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Indicador Táctil (Drag Handle)
                    Center(
                      child: Container(
                        width: 44,
                        height: 4.5,
                        decoration: BoxDecoration(
                          color: const Color(0xFFE2E8F0),
                          borderRadius: BorderRadius.circular(3),
                        ),
                      ),
                    ),

                    const SizedBox(height: 20),

                    // Encabezado Carrusel Stories
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Servicios Frecuentes',
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w900,
                                  color: Color(0xFF1E1B4B),
                                  letterSpacing: -0.3,
                                ),
                              ),
                              SizedBox(height: 2),
                              Text(
                                'Toca cualquier burbuja para pagar',
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(
                                  fontSize: 11,
                                  color: Color(0xFF6B7280),
                                ),
                              ),
                            ],
                          ),
                        ),
                        TextButton(
                          onPressed: () => setState(() => _currentSection = 1),
                          child: const Text(
                            'Ver todos →',
                            style: TextStyle(
                              color: Color(0xFF7C3AED),
                              fontWeight: FontWeight.w800,
                              fontSize: 12,
                            ),
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 12),

                    // Carrusel Horizontal Deslizable de Burbujas / Stories
                    Consumer<ProductosProvider>(
                      builder: (context, prodProv, _) {
                        if (prodProv.isLoading) {
                          return const Center(
                            child: Padding(
                              padding: EdgeInsets.all(20),
                              child: CircularProgressIndicator(),
                            ),
                          );
                        }

                        final todos = prodProv.catalogo?.productosPorTipoFront.values
                                .expand((e) => e)
                                .toList() ??
                            prodProv.productosFiltrados;
                        const ordenDestacados = [
                          166, 133, 115, 107, 102, 85, 76, 168, 178, 64, 55, 38, 154, 53, 164, 79
                        ];
                        final vistos = <int>{};
                        final destacados = <dynamic>[];
                        for (final idDest in ordenDestacados) {
                          for (final item in todos) {
                            if (item.idServicio == idDest && vistos.add(item.idServicio)) {
                              destacados.add(item);
                              break;
                            }
                          }
                        }
                        for (final item in todos) {
                          if (destacados.length >= 16) break;
                          if (vistos.add(item.idServicio)) {
                            destacados.add(item);
                          }
                        }
                        final productos = destacados;

                        if (productos.isEmpty) {
                          return Container(
                            padding: const EdgeInsets.all(18),
                            decoration: BoxDecoration(
                              color: const Color(0xFFFAF8FF),
                              borderRadius: BorderRadius.circular(16),
                            ),
                            child: const Center(
                              child: Text(
                                'Catálogo sincronizado con GestoPago',
                                style: TextStyle(
                                  color: Color(0xFF6B7280),
                                  fontSize: 12,
                                ),
                              ),
                            ),
                          );
                        }

                        return SizedBox(
                          height: 120,
                          child: ListView.separated(
                            scrollDirection: Axis.horizontal,
                            physics: const BouncingScrollPhysics(),
                            itemCount: productos.length,
                            separatorBuilder: (context, index) =>
                                const SizedBox(width: 16),
                            itemBuilder: (context, idx) {
                              final p = productos[idx];
                              return InkWell(
                                borderRadius: BorderRadius.circular(35),
                                onTap: () {
                                  Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                      builder: (_) =>
                                          PagoServicioScreen(producto: p),
                                    ),
                                  );
                                },
                                child: Column(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    // Anillo iridiscente con burbuja circular
                                    Container(
                                      padding: const EdgeInsets.all(2.5),
                                      decoration: BoxDecoration(
                                        shape: BoxShape.circle,
                                        gradient: const LinearGradient(
                                          begin: Alignment.topLeft,
                                          end: Alignment.bottomRight,
                                          colors: [
                                            Color(0xFFA78BFA),
                                            Color(0xFF7C3AED),
                                            Color(0xFFC084FC),
                                          ],
                                        ),
                                        boxShadow: [
                                          BoxShadow(
                                            color: const Color(0xFF7C3AED)
                                                .withValues(alpha: 0.35),
                                            blurRadius: 10,
                                            offset: const Offset(0, 4),
                                          ),
                                        ],
                                      ),
                                      child: Container(
                                        width: 62,
                                        height: 62,
                                        decoration: const BoxDecoration(
                                          color: Colors.white,
                                          shape: BoxShape.circle,
                                        ),
                                        padding: const EdgeInsets.all(4),
                                        child: ClipOval(
                                          child: BrandLogoWidget(
                                            servicio: p.servicio,
                                            size: 54,
                                            borderRadius: 27,
                                            urlLogo: p.urlLogo,
                                          ),
                                        ),
                                      ),
                                    ),
                                    const SizedBox(height: 7),
                                    SizedBox(
                                      width: 72,
                                      child: Text(
                                        p.servicio,
                                        textAlign: TextAlign.center,
                                        style: const TextStyle(
                                          fontSize: 12,
                                          fontWeight: FontWeight.w800,
                                          color: Color(0xFF1E1B4B),
                                        ),
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ),
                                    const SizedBox(height: 2),
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                          horizontal: 6, vertical: 1.5),
                                      decoration: BoxDecoration(
                                        color: AppColors.accentSoft,
                                        borderRadius: BorderRadius.circular(6),
                                      ),
                                      child: Text(
                                        p.tipoFront == 1 ? 'Móvil' : 'Recibo',
                                        style: const TextStyle(
                                          fontSize: 9,
                                          fontWeight: FontWeight.w700,
                                          color: AppColors.accent,
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              );
                            },
                          ),
                        );
                      },
                    ),

                    const SizedBox(height: 24),

                    // Banner de Seguridad 2FA con Escudo
                    Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFAF8FF),
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color: const Color(0xFFEDE9FE),
                          width: 1.2,
                        ),
                      ),
                      child: Row(
                        children: [
                          Container(
                            width: 44,
                            height: 44,
                            decoration: BoxDecoration(
                              color: AppColors.successBg,
                              borderRadius: BorderRadius.circular(14),
                            ),
                            child: const Icon(
                              Icons.fingerprint_rounded,
                              color: AppColors.success,
                              size: 26,
                            ),
                          ),
                          const SizedBox(width: 14),
                          const Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Protección Biométrica & 2FA',
                                  style: TextStyle(
                                    fontSize: 13,
                                    fontWeight: FontWeight.w800,
                                    color: Color(0xFF1E1B4B),
                                  ),
                                ),
                                SizedBox(height: 2),
                                Text(
                                  'Autoriza tu acceso y tus pagos con huella digital o código de un solo uso.',
                                  style: TextStyle(
                                    fontSize: 11,
                                    color: Color(0xFF6B7280),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),

                    const SizedBox(height: 24),

                    // Sección de Últimas Transacciones
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Expanded(
                          child: Text(
                            'Últimos Movimientos',
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w900,
                              color: Color(0xFF1E1B4B),
                              letterSpacing: -0.3,
                            ),
                          ),
                        ),
                        TextButton(
                          onPressed: () => setState(() => _currentSection = 2),
                          child: const Text(
                            'Historial completo →',
                            style: TextStyle(
                              color: Color(0xFF7C3AED),
                              fontWeight: FontWeight.w800,
                              fontSize: 12,
                            ),
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 10),

                    Consumer<PagoProvider>(
                      builder: (context, pagoProv, _) {
                        final historial = pagoProv.historial.take(3).toList();

                        if (historial.isEmpty) {
                          return Container(
                            padding: const EdgeInsets.all(22),
                            decoration: BoxDecoration(
                              color: const Color(0xFFFAF8FF),
                              borderRadius: BorderRadius.circular(18),
                              border: Border.all(
                                color: const Color(0xFFEDE9FE),
                              ),
                            ),
                            child: const Center(
                              child: Text(
                                'Aún no tienes pagos registrados',
                                style: TextStyle(
                                  color: Color(0xFF6B7280),
                                  fontSize: 12,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ),
                          );
                        }

                        return Column(
                          children: historial.map((mov) {
                            return Container(
                              margin: const EdgeInsets.only(bottom: 10),
                              padding: const EdgeInsets.all(14),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(18),
                                border: Border.all(
                                  color: const Color(0xFFEDE9FE),
                                ),
                                boxShadow: [
                                  BoxShadow(
                                    color: const Color(0xFF7C3AED)
                                        .withValues(alpha: 0.04),
                                    blurRadius: 10,
                                    offset: const Offset(0, 3),
                                  ),
                                ],
                              ),
                              child: Row(
                                children: [
                                  BrandLogoWidget(
                                    servicio: mov.servicio,
                                    size: 42,
                                    borderRadius: 12,
                                    urlLogo: mov.urlLogo,
                                  ),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          mov.servicio,
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                          style: const TextStyle(
                                            fontSize: 14,
                                            fontWeight: FontWeight.w800,
                                            color: Color(0xFF1E1B4B),
                                          ),
                                        ),
                                        const SizedBox(height: 2),
                                        Text(
                                          'Folio: ${mov.folio}',
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                          style: const TextStyle(
                                            fontSize: 11,
                                            color: Color(0xFF6B7280),
                                            fontWeight: FontWeight.w500,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  const SizedBox(width: 10),
                                  Column(
                                    crossAxisAlignment: CrossAxisAlignment.end,
                                    children: [
                                      Text(
                                        '-${CurrencyFormatter.format(mov.monto)}',
                                        style: const TextStyle(
                                          fontSize: 14,
                                          fontWeight: FontWeight.w900,
                                          color: Color(0xFF1E1B4B),
                                        ),
                                      ),
                                      const SizedBox(height: 3),
                                      Container(
                                        padding: const EdgeInsets.symmetric(
                                            horizontal: 7, vertical: 2),
                                        decoration: BoxDecoration(
                                          color: AppColors.success
                                              .withValues(alpha: 0.12),
                                          borderRadius:
                                              BorderRadius.circular(6),
                                        ),
                                        child: Text(
                                          mov.estado,
                                          style: const TextStyle(
                                            fontSize: 9,
                                            fontWeight: FontWeight.w800,
                                            color: AppColors.success,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            );
                          }).toList(),
                        );
                      },
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

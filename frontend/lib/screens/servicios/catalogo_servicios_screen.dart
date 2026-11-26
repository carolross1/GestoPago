import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_colors.dart';
import '../../providers/productos_provider.dart';
import '../../widgets/brand_logo_widget.dart';
import 'pago_servicio_screen.dart';

class CatalogoServiciosScreen extends StatefulWidget {
  final bool isEmbedded;
  const CatalogoServiciosScreen({super.key, this.isEmbedded = false});

  @override
  State<CatalogoServiciosScreen> createState() =>
      _CatalogoServiciosScreenState();
}

class _CatalogoServiciosScreenState extends State<CatalogoServiciosScreen> {
  final _searchController = TextEditingController();

  final List<Map<String, dynamic>> _categorias = [
    {'id': null, 'nombre': 'Todos', 'icon': Icons.apps_rounded},
    {'id': 1, 'nombre': 'Telefonía', 'icon': Icons.phone_android_rounded},
    {'id': 2, 'nombre': 'Luz / CFE', 'icon': Icons.bolt_rounded},
    {'id': 3, 'nombre': 'Agua', 'icon': Icons.water_drop_rounded},
    {'id': 4, 'nombre': 'Internet / TV', 'icon': Icons.wifi_rounded},
    {'id': 5, 'nombre': 'Streaming', 'icon': Icons.play_circle_outline_rounded},
    {'id': 6, 'nombre': 'Peajes', 'icon': Icons.directions_car_rounded},
    {'id': 7, 'nombre': 'Gobierno', 'icon': Icons.account_balance_rounded},
  ];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final prodProv = Provider.of<ProductosProvider>(context, listen: false);
      if (prodProv.catalogo == null || prodProv.productosFiltrados.isEmpty) {
        prodProv.cargarProductos();
      }
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final prodProv = Provider.of<ProductosProvider>(context);

    final Widget bodyContent = Column(
      children: [
        // Barra de búsqueda
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 12.0),
          child: TextField(
            controller: _searchController,
            onChanged: (val) => prodProv.filtrarBusqueda(val),
            decoration: InputDecoration(
              hintText: 'Buscar por servicio (CFE, Telcel, etc.)...',
              prefixIcon: const Icon(Icons.search_rounded, color: AppColors.textSecondary),
              suffixIcon: _searchController.text.isNotEmpty
                  ? IconButton(
                      icon: const Icon(Icons.clear_rounded, size: 20),
                      onPressed: () {
                        _searchController.clear();
                        prodProv.filtrarBusqueda('');
                      },
                    )
                  : null,
              filled: true,
              fillColor: AppColors.surface,
            ),
          ),
        ),

        // Chips de categorías horizontales
        SizedBox(
          height: 44,
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            scrollDirection: Axis.horizontal,
            itemCount: _categorias.length,
            separatorBuilder: (context, index) => const SizedBox(width: 8),
            itemBuilder: (context, index) {
              final cat = _categorias[index];
              final isSelected = prodProv.categoriaSeleccionada == cat['id'];

              return ChoiceChip(
                label: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      cat['icon'] as IconData,
                      size: 16,
                      color: isSelected ? Colors.white : AppColors.textSecondary,
                    ),
                    const SizedBox(width: 6),
                    Text(cat['nombre'] as String),
                  ],
                ),
                selected: isSelected,
                selectedColor: AppColors.accent,
                backgroundColor: AppColors.surface,
                labelStyle: TextStyle(
                  color: isSelected ? Colors.white : AppColors.textPrimary,
                  fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                  fontSize: 13,
                ),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20),
                  side: BorderSide(
                    color: isSelected ? AppColors.accent : AppColors.border,
                  ),
                ),
                onSelected: (_) {
                  prodProv.seleccionarCategoria(cat['id'] as int?);
                },
              );
            },
          ),
        ),

        const SizedBox(height: 12),

        // Lista de Productos
        Expanded(
          child: prodProv.isLoading
              ? const Center(child: CircularProgressIndicator(color: AppColors.accent))
              : RefreshIndicator(
                  onRefresh: () => prodProv.cargarProductos(),
                  child: Builder(
                    builder: (context) {
                      final productos = prodProv.productosFiltrados;

                      if (productos.isEmpty) {
                        return ListView(
                          padding: const EdgeInsets.only(top: 40, bottom: 90),
                          children: [
                            Center(
                              child: Column(
                                children: [
                                  Container(
                                    width: 72,
                                    height: 72,
                                    decoration: BoxDecoration(
                                      color: AppColors.accentSoft,
                                      shape: BoxShape.circle,
                                    ),
                                    child: const Icon(
                                      Icons.search_off_rounded,
                                      size: 38,
                                      color: AppColors.accent,
                                    ),
                                  ),
                                  const SizedBox(height: 16),
                                  const Text(
                                    'No se encontraron servicios',
                                    style: TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.w800,
                                      color: AppColors.textPrimary,
                                    ),
                                  ),
                                  const SizedBox(height: 6),
                                  const Text(
                                    'Comprueba tu conexión o presiona el botón para recargar',
                                    style: TextStyle(
                                      fontSize: 12,
                                      color: AppColors.textSecondary,
                                    ),
                                  ),
                                  const SizedBox(height: 16),
                                  ElevatedButton.icon(
                                    onPressed: () => prodProv.cargarProductos(),
                                    icon: const Icon(Icons.refresh_rounded, size: 18),
                                    label: const Text('Recargar Catálogo'),
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: AppColors.accent,
                                      foregroundColor: Colors.white,
                                      shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.circular(14),
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        );
                      }

                      return LayoutBuilder(
                        builder: (context, constraints) {
                          final int crossAxisCount =
                              constraints.maxWidth > 600 ? 3 : 2;

                          return GridView.builder(
                            padding: const EdgeInsets.only(
                                left: 20, right: 20, top: 12, bottom: 96),
                            gridDelegate:
                                SliverGridDelegateWithFixedCrossAxisCount(
                              crossAxisCount: crossAxisCount,
                              crossAxisSpacing: 14,
                              mainAxisSpacing: 14,
                              childAspectRatio: crossAxisCount == 2 ? 0.74 : 0.82,
                            ),
                              itemCount: productos.length,
                              itemBuilder: (context, index) {
                                final p = productos[index];

                                return InkWell(
                                  borderRadius: BorderRadius.circular(22),
                                  onTap: () {
                                    Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder: (_) =>
                                            PagoServicioScreen(producto: p),
                                      ),
                                    );
                                  },
                                  child: Container(
                                    padding: const EdgeInsets.all(14.0),
                                    decoration: BoxDecoration(
                                      color: AppColors.surface,
                                      borderRadius: BorderRadius.circular(22),
                                      border: Border.all(
                                          color: AppColors.border, width: 1.2),
                                      boxShadow: [
                                        BoxShadow(
                                          color: AppColors.accent
                                              .withValues(alpha: 0.05),
                                          blurRadius: 12,
                                          offset: const Offset(0, 4),
                                        ),
                                      ],
                                    ),
                                    child: Column(
                                      mainAxisAlignment:
                                          MainAxisAlignment.spaceBetween,
                                      children: [
                                        Column(
                                          children: [
                                            const SizedBox(height: 4),
                                            BrandLogoWidget(
                                              servicio: p.servicio,
                                              size: 56,
                                              borderRadius: 18,
                                              urlLogo: p.urlLogo,
                                            ),
                                            const SizedBox(height: 10),
                                            Text(
                                              p.servicio,
                                              textAlign: TextAlign.center,
                                              style: const TextStyle(
                                                fontSize: 14,
                                                fontWeight: FontWeight.w800,
                                                color: AppColors.textPrimary,
                                              ),
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis,
                                            ),
                                            const SizedBox(height: 2),
                                            Text(
                                              p.producto,
                                              textAlign: TextAlign.center,
                                              style: const TextStyle(
                                                fontSize: 11,
                                                color: AppColors.textSecondary,
                                              ),
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis,
                                            ),
                                          ],
                                        ),
                                        Column(
                                          children: [
                                            Container(
                                              padding:
                                                  const EdgeInsets.symmetric(
                                                      horizontal: 8, vertical: 3),
                                              decoration: BoxDecoration(
                                                color: AppColors.surfaceElevated,
                                                borderRadius:
                                                    BorderRadius.circular(8),
                                              ),
                                              child: Text(
                                                p.tipoFront == 1
                                                    ? 'Móvil / 10 dígitos'
                                                    : 'Recibo / Referencia',
                                                maxLines: 1,
                                                overflow: TextOverflow.ellipsis,
                                                style: const TextStyle(
                                                  fontSize: 10,
                                                  fontWeight: FontWeight.w600,
                                                  color: AppColors.textSecondary,
                                                ),
                                              ),
                                            ),
                                            const SizedBox(height: 8),
                                            Container(
                                              width: double.infinity,
                                              padding:
                                                  const EdgeInsets.symmetric(
                                                      vertical: 8),
                                              decoration: BoxDecoration(
                                                gradient:
                                                    AppColors.accentGradient,
                                                borderRadius:
                                                    BorderRadius.circular(12),
                                              ),
                                              child: const Center(
                                                child: Text(
                                                  'Pagar Servicio',
                                                  style: TextStyle(
                                                    color: Colors.white,
                                                    fontWeight: FontWeight.w700,
                                                    fontSize: 11,
                                                  ),
                                                ),
                                              ),
                                            ),
                                          ],
                                        ),
                                      ],
                                    ),
                                  ),
                                );
                              },
                            );
                          },
                        );
                      },
                    ),
                  ),
          ),
        ],
      );

    if (widget.isEmbedded) {
      return bodyContent;
    }

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Pago de Servicios'),
        elevation: 0,
      ),
      body: bodyContent,
    );
  }
}



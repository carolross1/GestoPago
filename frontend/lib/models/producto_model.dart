class ProductoModel {
  final String servicio;
  final String producto;
  final int idServicio;
  final int idProducto;
  final int? idCatTipoServicio;
  final int? tipoFront;
  final String precio;
  final String? tipoReferencia;
  final String? legend;

  ProductoModel({
    required this.servicio,
    required this.producto,
    required this.idServicio,
    required this.idProducto,
    this.idCatTipoServicio,
    this.tipoFront,
    required this.precio,
    this.tipoReferencia,
    this.legend,
  });

  factory ProductoModel.fromJson(Map<String, dynamic> json) {
    return ProductoModel(
      servicio: json['servicio'] as String? ?? 'Servicio',
      producto: json['producto'] as String? ?? 'Producto',
      idServicio: (json['idServicio'] as num?)?.toInt() ?? 0,
      idProducto: (json['idProducto'] as num?)?.toInt() ?? 0,
      idCatTipoServicio: (json['idCatTipoServicio'] as num?)?.toInt(),
      tipoFront: (json['tipoFront'] as num?)?.toInt(),
      precio: json['precio']?.toString() ?? '0.00',
      tipoReferencia: json['tipoReferencia'] as String?,
      legend: json['legend'] as String?,
    );
  }

  String get urlLogo {
    if (idServicio > 0) {
      return 'https://gestopago.portalventas.net/sistema/images/gestopago/servicios/$idServicio.png';
    }
    return '';
  }

  // Obtener nombre amigable de categoría basado en tipoFront
  static String getNombreCategoria(int? tipo) {
    switch (tipo) {
      case 1:
        return 'Telefonía y Recargas';
      case 2:
        return 'Luz y Energía';
      case 3:
        return 'Agua y Saneamiento';
      case 4:
        return 'Internet y Cable';
      case 5:
        return 'Entretenimiento y Streaming';
      case 6:
        return 'Peajes y TAG';
      case 7:
        return 'Gobierno e Impuestos';
      default:
        return 'Otros Servicios';
    }
  }
}

class CatalogoAgrupadoModel {
  final Map<int, List<ProductoModel>> productosPorTipoFront;

  CatalogoAgrupadoModel({required this.productosPorTipoFront});

  factory CatalogoAgrupadoModel.fromJson(Map<String, dynamic> json) {
    final map = <int, List<ProductoModel>>{};
    final dynamic fuente = json.containsKey('productosPorTipoFront') && json['productosPorTipoFront'] is Map
        ? json['productosPorTipoFront']
        : json;

    if (fuente is Map) {
      fuente.forEach((key, value) {
        final k = int.tryParse(key.toString());
        if (k != null && value is List) {
          map[k] = value
              .map((item) => ProductoModel.fromJson(item as Map<String, dynamic>))
              .toList();
        }
      });
    }
    return CatalogoAgrupadoModel(productosPorTipoFront: map);
  }
}

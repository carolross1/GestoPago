import 'package:flutter/material.dart';
import '../../core/constants/api_constants.dart';
import '../../core/network/api_client.dart';
import '../../models/producto_model.dart';

class ProductosProvider extends ChangeNotifier {
  final ApiClient _api = ApiClient();
  CatalogoAgrupadoModel? _catalogo;
  bool _isLoading = false;
  String? _errorMessage;
  int? _categoriaSeleccionada;
  String _busquedaQuery = '';

  CatalogoAgrupadoModel? get catalogo => _catalogo;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  int? get categoriaSeleccionada => _categoriaSeleccionada;
  String get busquedaQuery => _busquedaQuery;

  void seleccionarCategoria(int? tipoFront) {
    _categoriaSeleccionada = tipoFront;
    notifyListeners();
  }

  void filtrarBusqueda(String query) {
    _busquedaQuery = query;
    notifyListeners();
  }

  List<ProductoModel> get productosFiltrados {
    if (_catalogo == null) return [];
    List<ProductoModel> todos = [];

    if (_categoriaSeleccionada != null) {
      todos = _catalogo!.productosPorTipoFront[_categoriaSeleccionada] ?? [];
    } else {
      for (final lista in _catalogo!.productosPorTipoFront.values) {
        todos.addAll(lista);
      }
    }

    if (_busquedaQuery.trim().isEmpty) return todos;

    final q = _busquedaQuery.toLowerCase();
    return todos.where((p) {
      return p.servicio.toLowerCase().contains(q) ||
          p.producto.toLowerCase().contains(q);
    }).toList();
  }

  Future<void> cargarProductos() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final response = await _api.get<CatalogoAgrupadoModel>(
      ApiConstants.productos,
      fromJson: (json) => CatalogoAgrupadoModel.fromJson(json as Map<String, dynamic>),
    );

    _isLoading = false;
    if (response.exito && response.datos != null) {
      _catalogo = response.datos;
    } else {
      _errorMessage = response.mensaje.isNotEmpty
          ? response.mensaje
          : 'No se pudo obtener el catálogo de servicios';
    }
    notifyListeners();
  }
}


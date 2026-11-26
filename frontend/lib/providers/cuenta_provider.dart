import 'package:flutter/material.dart';
import '../../core/constants/api_constants.dart';
import '../../core/network/api_client.dart';
import '../../models/cuenta_model.dart';

class CuentaProvider extends ChangeNotifier {
  final ApiClient _api = ApiClient();
  CuentaResponse? _cuenta;
  bool _isLoading = false;
  bool _mostrarSaldo = true;
  String? _errorMessage;

  CuentaResponse? get cuenta => _cuenta;
  bool get isLoading => _isLoading;
  bool get mostrarSaldo => _mostrarSaldo;
  String? get errorMessage => _errorMessage;

  void toggleMostrarSaldo() {
    _mostrarSaldo = !_mostrarSaldo;
    notifyListeners();
  }

  Future<void> cargarCuenta(int clienteId) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final response = await _api.post<List<CuentaResponse>>(
      ApiConstants.buscarCuentas,
      body: {'clienteId': clienteId},
      fromJson: (json) {
        if (json is Map && json['contenido'] is List) {
          return (json['contenido'] as List)
              .map((item) => CuentaResponse.fromJson(item as Map<String, dynamic>))
              .toList();
        } else if (json is List) {
          return json
              .map((item) => CuentaResponse.fromJson(item as Map<String, dynamic>))
              .toList();
        }
        return [];
      },
    );

    _isLoading = false;
    if (response.exito && response.datos != null && response.datos!.isNotEmpty) {
      _cuenta = response.datos!.first;
    } else {
      _errorMessage = response.mensaje.isNotEmpty
          ? response.mensaje
          : 'No se encontró la cuenta asignada';
    }
    notifyListeners();
  }

  void actualizarSaldo(double nuevoSaldo) {
    if (_cuenta != null) {
      _cuenta = CuentaResponse(
        numeroCuenta: _cuenta!.numeroCuenta,
        clienteId: _cuenta!.clienteId,
        estatus: _cuenta!.estatus,
        saldoActual: nuevoSaldo,
        fechaApertura: _cuenta!.fechaApertura,
        horaApertura: _cuenta!.horaApertura,
      );
      notifyListeners();
    }
  }
}


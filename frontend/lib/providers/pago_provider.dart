import 'package:flutter/material.dart';
import '../../core/constants/api_constants.dart';
import '../../core/network/api_client.dart';
import '../../models/transaccion_model.dart';

class PagoProvider extends ChangeNotifier {
  final ApiClient _api = ApiClient();
  bool _isEnviando2fa = false;
  bool _isProcesandoPago = false;
  bool _isValidandoReferencia = false;
  bool _isLoadingHistorial = false;

  String? _errorMessage;
  Solicitar2faResponse? _solicitud2fa;
  PagoResponse? _ultimoPagoExitoso;
  ValidarReferenciaResponse? _resultadoValidacion;
  List<MovimientoHistorialResponse> _historial = [];

  bool get isEnviando2fa => _isEnviando2fa;
  bool get isProcesandoPago => _isProcesandoPago;
  bool get isValidandoReferencia => _isValidandoReferencia;
  bool get isLoadingHistorial => _isLoadingHistorial;

  String? get errorMessage => _errorMessage;
  Solicitar2faResponse? get solicitud2fa => _solicitud2fa;
  PagoResponse? get ultimoPagoExitoso => _ultimoPagoExitoso;
  ValidarReferenciaResponse? get resultadoValidacion => _resultadoValidacion;
  List<MovimientoHistorialResponse> get historial => _historial;

  Future<ValidarReferenciaResponse?> validarReferencia(ValidarReferenciaRequest request) async {
    _isValidandoReferencia = true;
    _errorMessage = null;
    _resultadoValidacion = null;
    notifyListeners();

    final response = await _api.post<ValidarReferenciaResponse>(
      ApiConstants.validarReferencia,
      body: request.toJson(),
      fromJson: (json) => ValidarReferenciaResponse.fromJson(json as Map<String, dynamic>),
    );

    _isValidandoReferencia = false;
    if (response.exito && response.datos != null) {
      _resultadoValidacion = response.datos;
      notifyListeners();
      return _resultadoValidacion;
    } else {
      _errorMessage = response.mensaje.isNotEmpty
          ? response.mensaje
          : (response.errores.isNotEmpty ? response.errores.first : 'Referencia inválida');
      notifyListeners();
      return null;
    }
  }

  Future<bool> solicitarCodigo2fa(
    String numeroCuenta, {
    bool enviarCorreo = true,
  }) async {
    _isEnviando2fa = true;
    _errorMessage = null;
    notifyListeners();

    final response = await _api.post<Solicitar2faResponse>(
      ApiConstants.solicitar2fa,
      body: Solicitar2faRequest(
        numeroCuenta: numeroCuenta,
        enviarCorreo: enviarCorreo,
      ).toJson(),
      fromJson: (json) => Solicitar2faResponse.fromJson(json as Map<String, dynamic>),
    );

    _isEnviando2fa = false;
    if (response.exito && response.datos != null) {
      _solicitud2fa = response.datos;
      notifyListeners();
      return true;
    } else {
      _errorMessage = response.mensaje.isNotEmpty
          ? response.mensaje
          : (response.errores.isNotEmpty ? response.errores.first : 'Error al enviar código 2FA');
      notifyListeners();
      return false;
    }
  }

  Future<bool> pagar(PagoRequest request) async {
    _isProcesandoPago = true;
    _errorMessage = null;
    notifyListeners();

    final response = await _api.post<PagoResponse>(
      ApiConstants.pagarTransaccion,
      body: request.toJson(),
      fromJson: (json) => PagoResponse.fromJson(json as Map<String, dynamic>),
    );

    _isProcesandoPago = false;
    if (response.exito && response.datos != null) {
      _ultimoPagoExitoso = response.datos;
      notifyListeners();
      return true;
    } else {
      _errorMessage = response.mensaje.isNotEmpty
          ? response.mensaje
          : (response.errores.isNotEmpty ? response.errores.first : 'Transacción rechazada');
      notifyListeners();
      return false;
    }
  }

  Future<void> cargarHistorial(String numeroCuenta) async {
    _isLoadingHistorial = true;
    _errorMessage = null;
    notifyListeners();

    final response = await _api.post<List<dynamic>>(
      ApiConstants.historialTransacciones,
      body: HistorialTransaccionesRequest(numeroCuenta: numeroCuenta).toJson(),
    );

    _isLoadingHistorial = false;
    if (response.exito && response.datos != null) {
      _historial = response.datos!
          .map((item) => MovimientoHistorialResponse.fromJson(item as Map<String, dynamic>))
          .toList();
    } else {
      _historial = [];
      _errorMessage = response.mensaje;
    }
    notifyListeners();
  }

  void limpiar() {
    _solicitud2fa = null;
    _ultimoPagoExitoso = null;
    _resultadoValidacion = null;
    _errorMessage = null;
    notifyListeners();
  }
}

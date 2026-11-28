import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../../models/api_response.dart';
import '../constants/api_constants.dart';

class ApiClient {
  static final ApiClient _instance = ApiClient._internal();
  factory ApiClient() => _instance;
  ApiClient._internal();

  static const Duration _timeout = Duration(seconds: 25);

  String? _token;
  String? get token => _token;

  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('auth_token');
  }

  Future<void> setToken(String? token) async {
    _token = token;
    final prefs = await SharedPreferences.getInstance();
    if (token != null) {
      await prefs.setString('auth_token', token);
    } else {
      await prefs.remove('auth_token');
    }
  }

  Map<String, String> _buildHeaders() {
    final headers = {
      'Content-Type': 'application/json; charset=UTF-8',
      'Accept': 'application/json',
    };
    if (_token != null && _token!.isNotEmpty) {
      headers['Authorization'] = 'Bearer $_token';
    }
    return headers;
  }

  /// Convierte excepciones técnicas en mensajes cortos y amigables sin URLs ni trazas.
  String _mensajeAmigable(Object e) {
    if (e is TimeoutException) {
      return 'El servicio tardó demasiado en responder. Intenta de nuevo en unos segundos.';
    }
    return 'No pudimos conectar con el servicio. Verifica tu conexión a internet e intenta de nuevo.';
  }

  Future<ApiResponse<T>> get<T>(
    String endpoint, {
    T Function(dynamic)? fromJson,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$endpoint');
    try {
      final response = await http.get(uri, headers: _buildHeaders()).timeout(_timeout);
      return _processResponse<T>(response, fromJson);
    } catch (e) {
      final msg = _mensajeAmigable(e);
      return ApiResponse<T>(
        exito: false,
        codigo: 503,
        mensaje: msg,
        errores: [msg],
      );
    }
  }

  Future<ApiResponse<T>> post<T>(
    String endpoint, {
    Map<String, dynamic>? body,
    T Function(dynamic)? fromJson,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$endpoint');
    try {
      final response = await http
          .post(
            uri,
            headers: _buildHeaders(),
            body: body != null ? jsonEncode(body) : null,
          )
          .timeout(_timeout);
      return _processResponse<T>(response, fromJson);
    } catch (e) {
      final msg = _mensajeAmigable(e);
      return ApiResponse<T>(
        exito: false,
        codigo: 503,
        mensaje: msg,
        errores: [msg],
      );
    }
  }

  ApiResponse<T> _processResponse<T>(
    http.Response response,
    T Function(dynamic)? fromJson,
  ) {
    try {
      final decoded = jsonDecode(utf8.decode(response.bodyBytes));
      if (decoded is Map<String, dynamic>) {
        if (decoded.containsKey('exito') && decoded.containsKey('codigo')) {
          return ApiResponse<T>.fromJson(decoded, fromJson);
        }

        final bool isSuccess = response.statusCode >= 200 && response.statusCode < 300;
        final dynamic rawData = decoded.containsKey('datos') ? decoded['datos'] : decoded;
        return ApiResponse<T>(
          exito: isSuccess,
          codigo: response.statusCode,
          mensaje: decoded['mensaje']?.toString() ?? '',
          datos: (rawData != null && fromJson != null)
              ? fromJson(rawData)
              : (rawData as T?),
        );
      }
      return ApiResponse<T>(
        exito: response.statusCode >= 200 && response.statusCode < 300,
        codigo: response.statusCode,
        mensaje: 'Respuesta recibida',
      );
    } catch (_) {
      final msg = response.statusCode >= 500
          ? 'El servicio no está disponible en este momento. Intenta en unos segundos.'
          : 'No pudimos completar tu solicitud. Intenta de nuevo.';
      return ApiResponse<T>(
        exito: false,
        codigo: response.statusCode,
        mensaje: msg,
        errores: [msg],
      );
    }
  }
}

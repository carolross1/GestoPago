import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../../models/api_response.dart';
import '../constants/api_constants.dart';

class ApiClient {
  static final ApiClient _instance = ApiClient._internal();
  factory ApiClient() => _instance;
  ApiClient._internal();

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

  Future<ApiResponse<T>> get<T>(
    String endpoint, {
    T Function(dynamic)? fromJson,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$endpoint');
    try {
      final response = await http.get(uri, headers: _buildHeaders());
      return _processResponse<T>(response, fromJson);
    } catch (e) {
      return ApiResponse<T>(
        exito: false,
        codigo: 500,
        mensaje: 'Error de conexión: $e',
        errores: [e.toString()],
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
      final response = await http.post(
        uri,
        headers: _buildHeaders(),
        body: body != null ? jsonEncode(body) : null,
      );
      return _processResponse<T>(response, fromJson);
    } catch (e) {
      return ApiResponse<T>(
        exito: false,
        codigo: 500,
        mensaje: 'Error de conexión: $e',
        errores: [e.toString()],
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
        // Si viene envuelto en el formato estándar RespuestaApi { exito, codigo, datos, ... }
        if (decoded.containsKey('exito') && decoded.containsKey('codigo')) {
          return ApiResponse<T>.fromJson(decoded, fromJson);
        }

        // Si es una respuesta directa (como CatalogoAgrupadoDTO de GET /productos)
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
    } catch (e) {
      return ApiResponse<T>(
        exito: false,
        codigo: response.statusCode,
        mensaje: 'Error al interpretar respuesta: ${response.body}',
        errores: [e.toString()],
      );
    }
  }
}


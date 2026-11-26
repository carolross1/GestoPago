import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/api_constants.dart';
import '../../core/network/api_client.dart';
import '../../core/services/biometric_service.dart';
import '../../models/auth_model.dart';
import '../../models/cliente_model.dart';

enum AuthStatus { initial, authenticating, authenticated, unauthenticated, error }

class AuthProvider extends ChangeNotifier {
  final ApiClient _api = ApiClient();
  AuthStatus _status = AuthStatus.initial;
  String? _errorMessage;
  ClienteResponse? _currentCliente;
  String? _token;

  AuthStatus get status => _status;
  bool get isAuthenticated => _status == AuthStatus.authenticated;
  String? get errorMessage => _errorMessage;
  ClienteResponse? get currentCliente => _currentCliente;
  String? get token => _token;

  Future<void> checkExistingSession() async {
    await _api.init();
    final prefs = await SharedPreferences.getInstance();
    final savedToken = prefs.getString('auth_token');
    final savedEmail = prefs.getString('auth_email');

    if (savedToken != null && savedToken.isNotEmpty && savedEmail != null) {
      _token = savedToken;
      // Intenta cargar la información del cliente con el token almacenado
      final ok = await _cargarPerfilCliente(savedEmail);
      if (ok) {
        _status = AuthStatus.authenticated;
      } else {
        await logout();
      }
    } else {
      _status = AuthStatus.unauthenticated;
    }
    notifyListeners();
  }

  Future<bool> login(String correo, String password) async {
    _status = AuthStatus.authenticating;
    _errorMessage = null;
    notifyListeners();

    final response = await _api.post<LoginResponse>(
      ApiConstants.login,
      body: LoginRequest(correo: correo, password: password).toJson(),
      fromJson: (json) => LoginResponse.fromJson(json as Map<String, dynamic>),
    );

    if (response.exito && response.datos != null) {
      _token = response.datos!.token;
      await _api.setToken(_token);
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('auth_email', correo.trim().toLowerCase());

      // Guardar credenciales cifradas para futuros inicios de sesión con huella
      await BiometricService.instance.saveCredentialsForBiometric(
        correo: correo.trim().toLowerCase(),
        password: password,
      );

      // Cargar información del cliente
      await _cargarPerfilCliente(correo.trim().toLowerCase());
      _status = AuthStatus.authenticated;
      notifyListeners();
      return true;
    } else {
      _status = AuthStatus.error;
      _errorMessage = response.mensaje.isNotEmpty
          ? response.mensaje
          : (response.errores.isNotEmpty ? response.errores.first : 'Credenciales inválidas');
      notifyListeners();
      return false;
    }
  }

  Future<bool> registrarPortal(String correo, String password) async {
    _errorMessage = null;
    notifyListeners();

    final response = await _api.post<void>(
      ApiConstants.registroPortal,
      body: RegistroLoginRequest(correo: correo, password: password).toJson(),
    );

    if (response.exito) {
      return true;
    } else {
      _errorMessage = response.mensaje.isNotEmpty
          ? response.mensaje
          : (response.errores.isNotEmpty ? response.errores.first : 'Error en el registro');
      notifyListeners();
      return false;
    }
  }

  /// Crea un nuevo cliente (con domicilio y cuenta bancaria) y activa su contraseña del portal.
  Future<({bool exito, String? numeroCuenta, String? error})> crearCuentaCompleta({
    required Map<String, dynamic> clientePayload,
    required String password,
  }) async {
    _errorMessage = null;
    notifyListeners();

    final respCliente = await _api.post<Map<String, dynamic>>(
      ApiConstants.crearCliente,
      body: clientePayload,
      fromJson: (json) => json is Map<String, dynamic> ? json : <String, dynamic>{},
    );

    if (!respCliente.exito) {
      final detalle = respCliente.errores.isNotEmpty
          ? respCliente.errores.join('\n')
          : (respCliente.mensaje.isNotEmpty
              ? respCliente.mensaje
              : 'No se pudo crear la cuenta. Revisa tus datos.');
      _errorMessage = detalle;
      notifyListeners();
      return (exito: false, numeroCuenta: null, error: detalle);
    }

    final numeroCuenta = respCliente.datos?['numeroCuenta']?.toString();
    final correo = (clientePayload['correo'] as String).trim().toLowerCase();

    final respPortal = await _api.post<void>(
      ApiConstants.registroPortal,
      body: RegistroLoginRequest(correo: correo, password: password).toJson(),
    );

    if (!respPortal.exito) {
      final detalle = respPortal.mensaje.isNotEmpty
          ? 'Tu cliente y cuenta ($numeroCuenta) fueron creados, pero ocurrió un error al activar tu contraseña: ${respPortal.mensaje}'
          : 'Tu cliente fue creado, pero no se pudo registrar la contraseña. Usa "Activar portal".';
      _errorMessage = detalle;
      notifyListeners();
      return (exito: false, numeroCuenta: numeroCuenta, error: detalle);
    }

    return (exito: true, numeroCuenta: numeroCuenta, error: null);
  }

  Future<List<Map<String, String>>> obtenerNacionalidades() async {
    final response = await _api.get<List<dynamic>>(ApiConstants.nacionalidades);
    if (response.exito && response.datos != null && response.datos!.isNotEmpty) {
      return response.datos!
          .whereType<Map<String, dynamic>>()
          .map((e) => {
                'codigo': (e['codigo'] ?? '').toString(),
                'nombre': (e['nombre'] ?? '').toString(),
              })
          .where((e) => e['codigo']!.isNotEmpty && e['nombre']!.isNotEmpty)
          .toList();
    }
    return const [
      {'codigo': 'MEX', 'nombre': 'México'},
      {'codigo': 'USA', 'nombre': 'Estados Unidos'},
      {'codigo': 'CAN', 'nombre': 'Canadá'},
      {'codigo': 'COL', 'nombre': 'Colombia'},
      {'codigo': 'ESP', 'nombre': 'España'},
      {'codigo': 'ARG', 'nombre': 'Argentina'},
    ];
  }

  Future<bool> _cargarPerfilCliente(String correo) async {
    final response = await _api.post<List<ClienteResponse>>(
      ApiConstants.buscarClientes,
      body: {'correo': correo},
      fromJson: (json) {
        if (json is Map && json['contenido'] is List) {
          return (json['contenido'] as List)
              .map((c) => ClienteResponse.fromJson(c as Map<String, dynamic>))
              .toList();
        } else if (json is List) {
          return json
              .map((c) => ClienteResponse.fromJson(c as Map<String, dynamic>))
              .toList();
        }
        return [];
      },
    );

    if (response.exito && response.datos != null && response.datos!.isNotEmpty) {
      _currentCliente = response.datos!.first;
      return true;
    }
    return false;
  }

  Future<void> logout() async {
    _token = null;
    _currentCliente = null;
    await _api.setToken(null);
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('auth_token');
    await prefs.remove('auth_email');
    _status = AuthStatus.unauthenticated;
    notifyListeners();
  }
}


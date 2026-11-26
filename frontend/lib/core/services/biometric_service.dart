import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:local_auth/local_auth.dart';
import 'package:shared_preferences/shared_preferences.dart';

class BiometricAuthResult {
  final bool success;
  final String? errorMessage;

  const BiometricAuthResult({required this.success, this.errorMessage});
}

class BiometricService {
  BiometricService._internal();
  static final BiometricService instance = BiometricService._internal();

  static const String _keyEnabled = 'biometric_enabled';
  static const String _keySecureEmail = 'biometric_secure_email';
  static const String _keySecurePassword = 'biometric_secure_password';

  final LocalAuthentication _localAuth = LocalAuthentication();
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  /// Indica si el dispositivo soporta autenticación biométrica o bloqueo seguro.
  Future<bool> isBiometricAvailable() async {
    if (kIsWeb) return false;
    try {
      final canCheck = await _localAuth.canCheckBiometrics;
      final isSupported = await _localAuth.isDeviceSupported();
      if (!canCheck && !isSupported) return false;
      final available = await _localAuth.getAvailableBiometrics();
      return available.isNotEmpty || isSupported;
    } catch (_) {
      return false;
    }
  }

  /// Indica si el usuario tiene activada la preferencia de huella digital.
  Future<bool> isBiometricEnabled() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      return prefs.getBool(_keyEnabled) ?? true;
    } catch (_) {
      return false;
    }
  }

  Future<void> setBiometricEnabled(bool enabled) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyEnabled, enabled);
    if (!enabled) {
      await clearSavedCredentials();
    }
  }

  /// Guarda credenciales cifradas en Keystore/Keychain para inicio de sesión con huella.
  Future<void> saveCredentialsForBiometric({
    required String correo,
    required String password,
  }) async {
    if (kIsWeb) return;
    try {
      final prefs = await SharedPreferences.getInstance();
      final prefEnabled = prefs.getBool(_keyEnabled);
      // Si el usuario la desactivó explícitamente en Perfil, no la re-activamos sola.
      if (prefEnabled == false) return;

      await _secureStorage.write(key: _keySecureEmail, value: correo.trim().toLowerCase());
      await _secureStorage.write(key: _keySecurePassword, value: password);
      await prefs.setBool(_keyEnabled, true);
    } catch (_) {
      // No bloquear el login si el almacenamiento seguro falla en un entorno de pruebas.
    }
  }

  Future<({String correo, String password})?> getSavedCredentials() async {
    if (kIsWeb) return null;
    try {
      final email = await _secureStorage.read(key: _keySecureEmail);
      final password = await _secureStorage.read(key: _keySecurePassword);
      if (email != null && email.isNotEmpty && password != null && password.isNotEmpty) {
        return (correo: email, password: password);
      }
      return null;
    } catch (_) {
      return null;
    }
  }

  Future<String?> getSavedEmailMasked() async {
    final creds = await getSavedCredentials();
    if (creds == null) return null;
    final correo = creds.correo;
    final atIndex = correo.indexOf('@');
    if (atIndex <= 1) return correo;
    final user = correo.substring(0, atIndex);
    final domain = correo.substring(atIndex);
    if (user.length <= 2) return '${user[0]}***$domain';
    return '${user[0]}***${user[user.length - 1]}$domain';
  }

  Future<bool> canUseBiometricLogin() async {
    if (!await isBiometricAvailable()) return false;
    if (!await isBiometricEnabled()) return false;
    final creds = await getSavedCredentials();
    return creds != null;
  }

  Future<bool> canUseBiometricPayments() async {
    if (!await isBiometricAvailable()) return false;
    return await isBiometricEnabled();
  }

  Future<void> clearSavedCredentials() async {
    if (kIsWeb) return;
    try {
      await _secureStorage.delete(key: _keySecureEmail);
      await _secureStorage.delete(key: _keySecurePassword);
    } catch (_) {}
  }

  /// Solicita autenticación biométrica nativa (huella digital / biometría del dispositivo).
  Future<BiometricAuthResult> authenticate({
    required String reason,
    bool biometricOnly = false,
  }) async {
    if (kIsWeb) {
      return const BiometricAuthResult(
        success: false,
        errorMessage: 'La huella digital solo está disponible en la app móvil.',
      );
    }

    try {
      final authenticated = await _localAuth.authenticate(
        localizedReason: reason,
        biometricOnly: biometricOnly,
        sensitiveTransaction: true,
        persistAcrossBackgrounding: true,
      );

      if (authenticated) {
        return const BiometricAuthResult(success: true);
      }
      return const BiometricAuthResult(
        success: false,
        errorMessage: 'Autenticación biométrica cancelada.',
      );
    } on LocalAuthException catch (e) {
      final msg = switch (e.code) {
        LocalAuthExceptionCode.noBiometricsEnrolled =>
          'No tienes huellas registradas en este teléfono. Configúrala en Ajustes o usa el código 2FA.',
        LocalAuthExceptionCode.noBiometricHardware ||
        LocalAuthExceptionCode.biometricHardwareTemporarilyUnavailable =>
          'El sensor de huella no está disponible en este momento.',
        LocalAuthExceptionCode.temporaryLockout ||
        LocalAuthExceptionCode.biometricLockout =>
          'Demasiados intentos fallidos. Desbloquea tu teléfono o usa el código 2FA.',
        LocalAuthExceptionCode.userCanceled ||
        LocalAuthExceptionCode.systemCanceled =>
          'Verificación cancelada.',
        _ => 'No se pudo validar tu huella digital. Intenta de nuevo o usa el código 2FA.',
      };
      return BiometricAuthResult(success: false, errorMessage: msg);
    } on PlatformException catch (_) {
      return const BiometricAuthResult(
        success: false,
        errorMessage: 'No se pudo iniciar el lector de huella. Usa el código 2FA.',
      );
    } catch (_) {
      return const BiometricAuthResult(
        success: false,
        errorMessage: 'Ocurrió un error al validar la huella digital.',
      );
    }
  }
}

class ApiConstants {
  // Backend en la nube (Render + Neon PostgreSQL) para funcionar desde cualquier red sin depender de la laptop
  static const String cloudUrl = 'https://gestopago-i7hw.onrender.com';

  // Opcional: si deseas probar contra tu servidor local, asigna aquí tu URL (ej. 'http://localhost:8080')
  static String? ipManual;

  static String get baseUrl {
    if (ipManual != null && ipManual!.isNotEmpty) return ipManual!;
    return cloudUrl;
  }

  // Auth
  static const String login = '/auth/login';
  static const String registroPortal = '/auth/registro';
  static const String verificarCorreo = '/auth/verificar-correo';
  static const String verificarSesion = '/auth/sesion';
  static const String logout = '/auth/logout';

  // Clientes
  static const String crearCliente = '/clientes';
  static const String buscarClientes = '/clientes/buscar';
  static const String actualizarCliente = '/clientes';
  static const String bajaCliente = '/clientes/baja';

  // Cuentas y Saldos
  static const String buscarCuentas = '/cuentas/buscar';
  static const String abonarCuenta = '/cuentas/abono';

  // Catálogos y Productos
  static const String productos = '/productos';
  static const String nacionalidades = '/catalogos/nacionalidades';
  static const String codigoPostal = '/catalogos/cp';
  static const String profesiones = '/catalogos/profesiones';

  // Transacciones
  static const String solicitar2fa = '/transacciones/2fa';
  static const String pagarTransaccion = '/transacciones/pago';
  static const String validarReferencia = '/transacciones/referencia/validar';
  static const String historialTransacciones = '/transacciones/historial';

  // Panel Administrativo (acceso restringido con JWT ADMIN)
  static const String adminLogin = '/api/admin/login';
  static const String adminCuentas = '/api/admin/cuentas';
  static const String adminAbono = '/api/admin/abono';
}

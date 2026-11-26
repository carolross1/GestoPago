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

  // Clientes
  static const String crearCliente = '/clientes';
  static const String buscarClientes = '/clientes/buscar';
  static const String actualizarCliente = '/clientes';
  static const String bajaCliente = '/clientes/baja';

  // Cuentas y Saldos
  static const String buscarCuentas = '/cuentas/buscar';

  // Catálogos y Productos
  static const String productos = '/productos';
  static const String nacionalidades = '/catalogos/nacionalidades';

  // Transacciones
  static const String solicitar2fa = '/transacciones/2fa';
  static const String pagarTransaccion = '/transacciones/pago';
  static const String validarReferencia = '/transacciones/referencia/validar';
  static const String historialTransacciones = '/transacciones/historial';
}

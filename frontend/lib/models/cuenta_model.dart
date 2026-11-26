class CuentaResponse {
  final String numeroCuenta;
  final int clienteId;
  final String estatus;
  final double saldoActual;
  final String? fechaApertura;
  final String? horaApertura;

  CuentaResponse({
    required this.numeroCuenta,
    required this.clienteId,
    required this.estatus,
    required this.saldoActual,
    this.fechaApertura,
    this.horaApertura,
  });

  factory CuentaResponse.fromJson(Map<String, dynamic> json) {
    return CuentaResponse(
      numeroCuenta: json['numeroCuenta']?.toString() ?? '',
      clienteId: (json['clienteId'] as num?)?.toInt() ?? 0,
      estatus: json['estatus'] as String? ?? 'ACTIVA',
      saldoActual: double.tryParse(json['saldoActual']?.toString() ?? '0.00') ?? 0.0,
      fechaApertura: json['fechaApertura'] as String?,
      horaApertura: json['horaApertura'] as String?,
    );
  }
}


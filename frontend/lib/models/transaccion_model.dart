class Solicitar2faRequest {
  final String numeroCuenta;

  Solicitar2faRequest({required this.numeroCuenta});

  Map<String, dynamic> toJson() => {
        'numeroCuenta': numeroCuenta,
      };
}

class Solicitar2faResponse {
  final String correoEnmascarado;
  final int vigenciaMinutos;
  final String? expiracion;
  final String? codigoParaPruebas;

  Solicitar2faResponse({
    required this.correoEnmascarado,
    required this.vigenciaMinutos,
    this.expiracion,
    this.codigoParaPruebas,
  });

  factory Solicitar2faResponse.fromJson(Map<String, dynamic> json) {
    return Solicitar2faResponse(
      correoEnmascarado: json['correoEnmascarado'] as String? ?? '***@***.com',
      vigenciaMinutos: (json['vigenciaMinutos'] as num?)?.toInt() ?? 5,
      expiracion: json['expiracion'] as String?,
      codigoParaPruebas: json['codigoParaPruebas'] as String?,
    );
  }
}

class ValidarReferenciaRequest {
  final int idServicio;
  final int idProducto;
  final String referencia;
  final String tipoDestino; // 'TELEFONO' o 'TRANSFERENCIA'

  ValidarReferenciaRequest({
    required this.idServicio,
    required this.idProducto,
    required this.referencia,
    required this.tipoDestino,
  });

  Map<String, dynamic> toJson() => {
        'idServicio': idServicio,
        'idProducto': idProducto,
        'referencia': referencia.trim(),
        'tipoDestino': tipoDestino,
      };
}

class ValidarReferenciaResponse {
  final bool valida;
  final String mensaje;
  final String? servicio;
  final String? producto;
  final String? referencia;
  final String? tipoReferencia;
  final double? adeudoSugerido;
  final String? titularSugerido;
  final String? urlLogo;

  ValidarReferenciaResponse({
    required this.valida,
    required this.mensaje,
    this.servicio,
    this.producto,
    this.referencia,
    this.tipoReferencia,
    this.adeudoSugerido,
    this.titularSugerido,
    this.urlLogo,
  });

  factory ValidarReferenciaResponse.fromJson(Map<String, dynamic> json) {
    return ValidarReferenciaResponse(
      valida: json['valida'] as bool? ?? false,
      mensaje: json['mensaje'] as String? ?? '',
      servicio: json['servicio'] as String?,
      producto: json['producto'] as String?,
      referencia: json['referencia'] as String?,
      tipoReferencia: json['tipoReferencia'] as String?,
      adeudoSugerido: json['adeudoSugerido'] != null
          ? double.tryParse(json['adeudoSugerido'].toString())
          : null,
      titularSugerido: json['titularSugerido'] as String?,
      urlLogo: json['urlLogo'] as String?,
    );
  }
}

class HistorialTransaccionesRequest {
  final String numeroCuenta;

  HistorialTransaccionesRequest({required this.numeroCuenta});

  Map<String, dynamic> toJson() => {
        'numeroCuenta': numeroCuenta,
      };
}

class MovimientoHistorialResponse {
  final int? id;
  final String numeroCuenta;
  final String tipoMovimiento;
  final String? concepto;
  final String servicio;
  final String producto;
  final String? referencia;
  final String folio;
  final String estado;
  final double monto;
  final double saldoResultante;
  final String? urlLogo;
  final String fecha;
  final String hora;

  MovimientoHistorialResponse({
    this.id,
    required this.numeroCuenta,
    required this.tipoMovimiento,
    this.concepto,
    required this.servicio,
    required this.producto,
    this.referencia,
    required this.folio,
    required this.estado,
    required this.monto,
    required this.saldoResultante,
    this.urlLogo,
    required this.fecha,
    required this.hora,
  });

  factory MovimientoHistorialResponse.fromJson(Map<String, dynamic> json) {
    return MovimientoHistorialResponse(
      id: (json['id'] as num?)?.toInt(),
      numeroCuenta: json['numeroCuenta']?.toString() ?? '',
      tipoMovimiento: json['tipoMovimiento']?.toString() ?? 'PAGO',
      concepto: json['concepto'] as String?,
      servicio: json['servicio'] as String? ?? 'Servicio',
      producto: json['producto'] as String? ?? 'Pago de Servicio',
      referencia: json['referencia'] as String?,
      folio: json['folio'] as String? ?? 'UPC-PENDIENTE',
      estado: json['estado'] as String? ?? 'EXITOSA',
      monto: double.tryParse(json['monto']?.toString() ?? '0.00') ?? 0.0,
      saldoResultante:
          double.tryParse(json['saldoResultante']?.toString() ?? '0.00') ?? 0.0,
      urlLogo: json['urlLogo'] as String?,
      fecha: json['fecha']?.toString() ?? '',
      hora: json['hora']?.toString() ?? '',
    );
  }
}

class PagoRequest {
  final String numeroCuenta;
  final int idServicio;
  final int idProducto;
  final double monto;
  final String tipoDestino; // 'TELEFONO' o 'TRANSFERENCIA'
  final String? telefono;
  final String? referencia;
  final String codigo2fa;

  PagoRequest({
    required this.numeroCuenta,
    required this.idServicio,
    required this.idProducto,
    required this.monto,
    required this.tipoDestino,
    this.telefono,
    this.referencia,
    required this.codigo2fa,
  });

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> map = {
      'numeroCuenta': numeroCuenta,
      'idServicio': idServicio,
      'idProducto': idProducto,
      'monto': monto,
      'tipoDestino': tipoDestino,
      'codigo2fa': codigo2fa.trim(),
    };
    if (telefono != null && telefono!.isNotEmpty) {
      map['telefono'] = telefono;
    }
    if (referencia != null && referencia!.isNotEmpty) {
      map['referencia'] = referencia;
    }
    return map;
  }
}

class PagoResponse {
  final String idTx;
  final String numAutorizacion;
  final String codigoProveedor;
  final String mensajeProveedor;
  final String numeroCuenta;
  final String tipoDestino;
  final String destino;
  final double monto;
  final double saldoAnterior;
  final double saldoRestante;
  final String? fecha;
  final String? hora;

  PagoResponse({
    required this.idTx,
    required this.numAutorizacion,
    required this.codigoProveedor,
    required this.mensajeProveedor,
    required this.numeroCuenta,
    required this.tipoDestino,
    required this.destino,
    required this.monto,
    required this.saldoAnterior,
    required this.saldoRestante,
    this.fecha,
    this.hora,
  });

  factory PagoResponse.fromJson(Map<String, dynamic> json) {
    return PagoResponse(
      idTx: json['idTx']?.toString() ?? '',
      numAutorizacion: json['numAutorizacion']?.toString() ?? '',
      codigoProveedor: json['codigoProveedor']?.toString() ?? '',
      mensajeProveedor: json['mensajeProveedor'] as String? ?? 'Transacción exitosa',
      numeroCuenta: json['numeroCuenta']?.toString() ?? '',
      tipoDestino: json['tipoDestino']?.toString() ?? 'TRANSFERENCIA',
      destino: json['destino']?.toString() ?? '',
      monto: double.tryParse(json['monto']?.toString() ?? '0.00') ?? 0.0,
      saldoAnterior: double.tryParse(json['saldoAnterior']?.toString() ?? '0.00') ?? 0.0,
      saldoRestante: double.tryParse(json['saldoRestante']?.toString() ?? '0.00') ?? 0.0,
      fecha: json['fecha'] as String?,
      hora: json['hora'] as String?,
    );
  }
}

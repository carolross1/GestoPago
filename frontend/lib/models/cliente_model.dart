class ClienteResponse {
  final int id;
  final String nombre;
  final String? segundoNombre;
  final String apellidoPaterno;
  final String? apellidoMaterno;
  final String? fechaNacimiento;
  final String? curp;
  final String? rfc;
  final String? sexo;
  final String? nacionalidad;
  final String? nacionalidadNombre;
  final String? estadoCivil;
  final String correo;
  final int? telefonoMovil;
  final String? ocupacion;
  final String? empresa;
  final double? ingresoMensual;
  final bool activo;
  final String? numeroCuenta;

  ClienteResponse({
    required this.id,
    required this.nombre,
    this.segundoNombre,
    required this.apellidoPaterno,
    this.apellidoMaterno,
    this.fechaNacimiento,
    this.curp,
    this.rfc,
    this.sexo,
    this.nacionalidad,
    this.nacionalidadNombre,
    this.estadoCivil,
    required this.correo,
    this.telefonoMovil,
    this.ocupacion,
    this.empresa,
    this.ingresoMensual,
    required this.activo,
    this.numeroCuenta,
  });

  String get nombreCompleto {
    final parts = [
      nombre,
      if (segundoNombre != null && segundoNombre!.isNotEmpty) segundoNombre,
      apellidoPaterno,
      if (apellidoMaterno != null && apellidoMaterno!.isNotEmpty) apellidoMaterno,
    ];
    return parts.where((p) => p != null).join(' ');
  }

  factory ClienteResponse.fromJson(Map<String, dynamic> json) {
    return ClienteResponse(
      id: (json['id'] as num?)?.toInt() ?? 0,
      nombre: json['nombre'] as String? ?? '',
      segundoNombre: json['segundoNombre'] as String?,
      apellidoPaterno: json['apellidoPaterno'] as String? ?? '',
      apellidoMaterno: json['apellidoMaterno'] as String?,
      fechaNacimiento: json['fechaNacimiento'] as String?,
      curp: json['curp'] as String?,
      rfc: json['rfc'] as String?,
      sexo: json['sexo'] as String?,
      nacionalidad: json['nacionalidad'] as String?,
      nacionalidadNombre: json['nacionalidadNombre'] as String?,
      estadoCivil: json['estadoCivil'] as String?,
      correo: json['correo'] as String? ?? '',
      telefonoMovil: (json['telefonoMovil'] as num?)?.toInt(),
      ocupacion: json['ocupacion'] as String?,
      empresa: json['empresa'] as String?,
      ingresoMensual: double.tryParse(json['ingresoMensual']?.toString() ?? '0.00'),
      activo: json['activo'] as bool? ?? true,
      numeroCuenta: json['numeroCuenta'] as String?,
    );
  }
}


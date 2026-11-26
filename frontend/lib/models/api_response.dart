class ApiResponse<T> {
  final bool exito;
  final int codigo;
  final String mensaje;
  final String? fecha;
  final String? hora;
  final String? ruta;
  final T? datos;
  final List<String> errores;

  ApiResponse({
    required this.exito,
    required this.codigo,
    required this.mensaje,
    this.fecha,
    this.hora,
    this.ruta,
    this.datos,
    this.errores = const [],
  });

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T Function(dynamic json)? fromJsonT,
  ) {
    return ApiResponse<T>(
      exito: json['exito'] as bool? ?? false,
      codigo: json['codigo'] as int? ?? 500,
      mensaje: json['mensaje'] as String? ?? '',
      fecha: json['fecha'] as String?,
      hora: json['hora'] as String?,
      ruta: json['ruta'] as String?,
      datos: (json['datos'] != null && fromJsonT != null)
          ? fromJsonT(json['datos'])
          : (json['datos'] as T?),
      errores: (json['errores'] as List<dynamic>?)
              ?.map((e) => e.toString())
              .toList() ??
          [],
    );
  }
}


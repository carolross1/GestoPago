class LoginResponse {
  final String token;
  final String tipo;
  final int expiracionMs;

  LoginResponse({
    required this.token,
    this.tipo = 'Bearer',
    required this.expiracionMs,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      token: json['token'] as String? ?? '',
      tipo: json['tipo'] as String? ?? 'Bearer',
      expiracionMs: (json['expiracionMs'] as num?)?.toInt() ?? 0,
    );
  }
}

class LoginRequest {
  final String correo;
  final String password;

  LoginRequest({required this.correo, required this.password});

  Map<String, dynamic> toJson() => {
        'correo': correo.trim().toLowerCase(),
        'password': password,
      };
}

class RegistroLoginRequest {
  final String correo;
  final String password;

  RegistroLoginRequest({required this.correo, required this.password});

  Map<String, dynamic> toJson() => {
        'correo': correo.trim().toLowerCase(),
        'password': password,
      };
}


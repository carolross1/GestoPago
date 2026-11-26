import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/theme/app_theme.dart';
import 'providers/auth_provider.dart';
import 'providers/cuenta_provider.dart';
import 'providers/pago_provider.dart';
import 'providers/productos_provider.dart';
import 'screens/splash_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const GestoPagoApp());
}

class GestoPagoApp extends StatelessWidget {
  const GestoPagoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => CuentaProvider()),
        ChangeNotifierProvider(create: (_) => ProductosProvider()),
        ChangeNotifierProvider(create: (_) => PagoProvider()),
      ],
      child: MaterialApp(
        title: 'GestoPago Banca Digital',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.lightTheme,
        home: const SplashScreen(),
      ),
    );
  }
}


import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:gestopago_app/main.dart';

void main() {
  testWidgets('GestoPagoApp render smoke test', (WidgetTester tester) async {
    SharedPreferences.setMockInitialValues({});
    await tester.pumpWidget(const GestoPagoApp());
    await tester.pump(const Duration(milliseconds: 1500));
    expect(find.byType(GestoPagoApp), findsOneWidget);
  });
}

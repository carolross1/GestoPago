import 'package:flutter_test/flutter_test.dart';
import 'package:gestopago_app/core/utils/curp_rfc_calculator.dart';

void main() {
  group('CurpRfcCalculator', () {
    test('calcula prefijo CURP (11) y RFC (10) con reglas RENAPO/SAT', () {
      final res = CurpRfcCalculator.calcularPrefijos(
        primerNombre: 'Carol',
        apellidoPaterno: 'Ríos',
        apellidoMaterno: 'López',
        fechaNacimiento: DateTime(2001, 5, 15),
        sexo: 'M',
      );
      expect(res.rfcPrefijo, 'RILC010515');
      expect(res.curpPrefijo, 'RILC010515M');
    });

    test('omite MARIA/JOSE cuando existe segundo nombre', () {
      final res = CurpRfcCalculator.calcularPrefijos(
        primerNombre: 'María',
        segundoNombre: 'Guadalupe',
        apellidoPaterno: 'Pérez',
        apellidoMaterno: 'García',
        fechaNacimiento: DateTime(1999, 1, 31),
        sexo: 'M',
      );
      expect(res.rfcPrefijo, 'PEGG990131');
      expect(res.curpPrefijo, 'PEGG990131M');
    });

    test('reemplaza palabras inconvenientes con X', () {
      final res = CurpRfcCalculator.calcularPrefijos(
        primerNombre: 'Ana',
        apellidoPaterno: 'Baca',
        apellidoMaterno: 'Castro',
        fechaNacimiento: DateTime(1995, 12, 5),
        sexo: 'H',
      );
      expect(res.curpPrefijo, 'BXCA951205H');
      expect(res.rfcPrefijo, 'BACX951205');
    });
  });
}

import 'package:intl/intl.dart';

class CurrencyFormatter {
  static final NumberFormat _currencyFormat = NumberFormat.currency(
    locale: 'es_MX',
    symbol: '\$',
    decimalDigits: 2,
  );

  static String format(double amount) {
    return '${_currencyFormat.format(amount)} MXN';
  }

  static String formatShort(double amount) {
    return _currencyFormat.format(amount);
  }
}


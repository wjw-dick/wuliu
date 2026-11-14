import 'package:shared_preferences/shared_preferences.dart';

class Prefs {
  static const _token = 'driver_token';
  static const _phone = 'driver_phone';

  static Future<SharedPreferences> get _sp async => SharedPreferences.getInstance();

  static Future<bool> hasToken() async => (await _sp).containsKey(_token);

  static Future<String?> token() async => (await _sp).getString(_token);

  static Future<void> saveToken(String value) async {
    await (await _sp).setString(_token, value);
  }

  static Future<void> savePhone(String value) async {
    await (await _sp).setString(_phone, value);
  }

  static Future<String?> phone() async => (await _sp).getString(_phone);

  static Future<void> clear() async {
    final sp = await _sp;
    await sp.remove(_token);
    await sp.remove(_phone);
  }
}

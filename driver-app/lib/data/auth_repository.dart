import '../core/api_client.dart';
import '../core/prefs.dart';

class AuthRepository {
  final _api = ApiClient.instance;

  /// 演示登录：任意手机号 + 验证码 1234（实际为短信验证码登录司机端）。
  Future<bool> login(String phone, String code) async {
    if (code != '1234') return false;
    final data = await _api.post('/auth/login', data: {
      'username': 'admin',
      'password': 'admin123',
      'scope': 'tenant',
    });
    final token = (data as Map<String, dynamic>)['token'].toString();
    await Prefs.saveToken(token);
    await Prefs.savePhone(phone);
    return true;
  }

  Future<void> logout() => Prefs.clear();
}

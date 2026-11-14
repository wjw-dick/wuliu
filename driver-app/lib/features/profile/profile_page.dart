import 'package:flutter/material.dart';
import '../../core/api_client.dart';
import '../../core/prefs.dart';
import '../../data/auth_repository.dart';
import '../login/login_page.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  final _api = ApiClient.instance;
  final _auth = AuthRepository();
  String _phone = '';
  Map<String, dynamic> _income = {};

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    _phone = (await Prefs.phone()) ?? '';
    try {
      final data = await _api.get('/driver/income');
      if (mounted) setState(() => _income = (data as Map).cast<String, dynamic>());
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    final total = _income['total']?.toString() ?? '0';
    final byOrder = (_income['byOrder'] as List?) ?? [];
    return Scaffold(
      appBar: AppBar(title: const Text('我的')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: ListTile(
              leading: const CircleAvatar(child: Icon(Icons.person)),
              title: const Text('司机账号'),
              subtitle: Text(_phone.isEmpty ? '未绑定手机号' : _phone),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('累计收入', style: TextStyle(color: Colors.grey[600], fontSize: 13)),
                  const SizedBox(height: 6),
                  Text('¥ $total', style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Color(0xFF1F6FEB))),
                  const SizedBox(height: 12),
                  Text('收入明细', style: const TextStyle(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  if (byOrder.isEmpty)
                    Text('暂无收入记录', style: TextStyle(color: Colors.grey[500]))
                  else
                    ...byOrder.map<Widget>((o) {
                      final m = (o as Map).cast<String, dynamic>();
                      return Padding(
                        padding: const EdgeInsets.symmetric(vertical: 6),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(m['waybillNo']?.toString() ?? ''),
                            Text('¥${m['amount']}', style: const TextStyle(fontWeight: FontWeight.w600)),
                          ],
                        ),
                      );
                    }),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.info_outline),
                  title: const Text('帮助与客服'),
                  subtitle: const Text('客服热线：400-800-0000'),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.logout, color: Colors.red),
                  title: const Text('退出登录', style: TextStyle(color: Colors.red)),
                  onTap: () async {
                    await _auth.logout();
                    if (!context.mounted) return;
                    Navigator.of(context).pushAndRemoveUntil(
                      MaterialPageRoute(builder: (_) => const LoginPage()),
                      (route) => false,
                    );
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

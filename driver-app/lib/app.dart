import 'package:flutter/material.dart';
import 'features/login/login_page.dart';
import 'features/shell/main_shell.dart';
import 'core/prefs.dart';

class DriverApp extends StatelessWidget {
  const DriverApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '司机端',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF1F6FEB)),
        useMaterial3: true,
      ),
      home: FutureBuilder<bool>(
        future: Prefs.hasToken(),
        builder: (context, snap) {
          if (snap.connectionState != ConnectionState.done) {
            return const Scaffold(body: Center(child: CircularProgressIndicator()));
          }
          return (snap.data ?? false) ? const MainShell() : const LoginPage();
        },
      ),
    );
  }
}

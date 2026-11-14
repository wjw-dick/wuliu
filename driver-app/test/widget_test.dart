import 'package:flutter_test/flutter_test.dart';
import 'package:driver_app/app.dart';

void main() {
  testWidgets('应用可构建', (WidgetTester tester) async {
    await tester.pumpWidget(const DriverApp());
    await tester.pumpAndSettle();
    expect(find.text('司机端'), findsOneWidget);
  });
}

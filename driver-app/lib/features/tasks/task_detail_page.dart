import 'package:flutter/material.dart';
import '../../data/task_repository.dart';
import '../../models/task.dart';

class TaskDetailPage extends StatefulWidget {
  final TaskItem task;
  final TaskRepository repo;
  const TaskDetailPage({super.key, required this.task, required this.repo});

  @override
  State<TaskDetailPage> createState() => _TaskDetailPageState();
}

class _TaskDetailPageState extends State<TaskDetailPage> {
  bool _busy = false;

  Future<void> _run(String action, Future<void> Function() fn, String success) async {
    setState(() => _busy = true);
    try {
      await fn();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(success)));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('操作失败：$e')));
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  void _showInput(String title, Future<void> Function(String) onDone) {
    final ctrl = TextEditingController();
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(title),
        content: TextField(controller: ctrl, autofocus: true),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('取消')),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              _run(title, () => onDone(ctrl.text), '$title成功');
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final t = widget.task;
    return Scaffold(
      appBar: AppBar(title: Text('任务 · ${t.waybillNo}')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(children: [
                    Expanded(child: Text('${t.origin} → ${t.destination}', style: const TextStyle(fontSize: 17, fontWeight: FontWeight.bold))),
                    Chip(label: Text(t.status), visualDensity: VisualDensity.compact),
                  ]),
                  const Divider(height: 24),
                  _row('运单号', t.waybillNo),
                  _row('车牌', t.plateNo),
                  _row('提货时间', t.pickupTime),
                  _row('货物', '${t.cargoName} · ${t.weight}kg · ${t.volume}m³ · ${t.pieces}件'),
                  if (t.codAmount > 0) _row('代收货款', '${t.codAmount} 元'),
                  _row('发货人', '${t.senderName} ${t.senderAddress}'),
                  _row('收货人', '${t.receiverName} ${t.receiverPhone} ${t.receiverAddress}'),
                  if (t.remark.isNotEmpty) _row('备注', t.remark),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Text('作业操作', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 12),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: [
              FilledButton.icon(
                onPressed: _busy
                    ? null
                    : () => _showInput('到达提货点（输入地址）', (v) => widget.repo.checkin(t.id, 31.23, 121.47, v)),
                icon: const Icon(Icons.location_on),
                label: const Text('提货签到'),
              ),
              FilledButton.tonalIcon(
                onPressed: _busy ? null : () => _run('装车发运', () => widget.repo.load(t.id), '装车发运成功'),
                icon: const Icon(Icons.local_shipping),
                label: const Text('装车发运'),
              ),
              FilledButton.tonalIcon(
                onPressed: _busy ? null : () => _run('上报节点', () => widget.repo.reportNode(t.id, '已发车'), '节点已上报'),
                icon: const Icon(Icons.route),
                label: const Text('上报节点'),
              ),
              FilledButton.icon(
                onPressed: _busy ? null : () => _showInput('电子签收（输入签收人）', (v) => widget.repo.sign(t.id, v, t.receiverPhone, '')),
                icon: const Icon(Icons.check_circle),
                label: const Text('电子签收'),
              ),
            ],
          ),
          const SizedBox(height: 20),
          Text('异常上报', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: _busy
                ? null
                : () => _showInput('异常描述', (v) => widget.repo.reportException(t.id, '延误', v)),
            icon: const Icon(Icons.warning_amber),
            label: const Text('上报异常'),
            style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
          ),
        ],
      ),
    );
  }

  Widget _row(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(width: 90, child: Text(label, style: TextStyle(color: Colors.grey[600], fontSize: 13))),
          Expanded(child: Text(value, style: const TextStyle(fontSize: 14))),
        ],
      ),
    );
  }
}

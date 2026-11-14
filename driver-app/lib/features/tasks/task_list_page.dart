import 'package:flutter/material.dart';
import '../../data/task_repository.dart';
import '../../models/task.dart';
import 'task_detail_page.dart';

class TaskListPage extends StatefulWidget {
  const TaskListPage({super.key});

  @override
  State<TaskListPage> createState() => _TaskListPageState();
}

class _TaskListPageState extends State<TaskListPage> {
  final _repo = TaskRepository();
  List<TaskItem> _tasks = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final list = await _repo.tasks();
      setState(() => _tasks = list);
    } catch (e) {
      setState(() => _error = '加载失败：$e');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Color _statusColor(String s) {
    switch (s) {
      case '已签收':
        return Colors.green;
      case '已异常':
        return Colors.red;
      case '已取消':
        return Colors.grey;
      default:
        return const Color(0xFF1F6FEB);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('我的任务'), actions: [
        IconButton(onPressed: _load, icon: const Icon(Icons.refresh)),
      ]),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!))
              : _tasks.isEmpty
                  ? const Center(child: Text('暂无任务'))
                  : RefreshIndicator(
                      onRefresh: _load,
                      child: ListView.separated(
                        padding: const EdgeInsets.all(12),
                        itemCount: _tasks.length,
                        separatorBuilder: (_, _) => const SizedBox(height: 12),
                        itemBuilder: (context, i) {
                          final t = _tasks[i];
                          return Card(
                            child: InkWell(
                              onTap: () => Navigator.of(context).push(
                                MaterialPageRoute(builder: (_) => TaskDetailPage(task: t, repo: _repo)),
                              ).then((_) => _load()),
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        Expanded(
                                          child: Text(t.waybillNo,
                                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                                        ),
                                        Chip(
                                          label: Text(t.status),
                                          labelStyle: TextStyle(color: _statusColor(t.status), fontSize: 12),
                                          backgroundColor: _statusColor(t.status).withValues(alpha: 0.1),
                                          side: BorderSide.none,
                                          visualDensity: VisualDensity.compact,
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 8),
                                    Text('${t.origin} → ${t.destination}',
                                        style: const TextStyle(fontSize: 15, color: Colors.black87)),
                                    const SizedBox(height: 6),
                                    Text('收货人：${t.receiverName}  ${t.receiverPhone}',
                                        style: TextStyle(color: Colors.grey[700], fontSize: 13)),
                                    const SizedBox(height: 4),
                                    Text(
                                        '货物：${t.cargoName} · ${t.weight}kg · ${t.pieces}件'
                                        '${t.codAmount > 0 ? ' · 代收 ${t.codAmount}元' : ''}',
                                        style: TextStyle(color: Colors.grey[600], fontSize: 13)),
                                  ],
                                ),
                              ),
                            ),
                          );
                        },
                      ),
                    ),
    );
  }
}

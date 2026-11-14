import '../core/api_client.dart';
import '../models/task.dart';

class TaskRepository {
  final _api = ApiClient.instance;

  Future<List<TaskItem>> tasks({String? status}) async {
    final data = await _api.get('/driver/tasks', query: {
      if (status != null && status.isNotEmpty) 'status': status,
    });
    final list = (data as List).cast<Map<String, dynamic>>();
    return list.map(TaskItem.fromJson).toList();
  }

  Future<void> checkin(int id, double lat, double lng, String address) async {
    await _api.post('/driver/tasks/$id/checkin', data: {'lat': lat, 'lng': lng, 'address': address});
  }

  Future<void> load(int id) async {
    await _api.post('/driver/tasks/$id/load', data: {'photos': <String>[]});
  }

  Future<void> sign(int id, String signerName, String signerPhone, String signImage) async {
    await _api.post('/driver/tasks/$id/sign', data: {
      'signerName': signerName,
      'signerPhone': signerPhone,
      'signImage': signImage,
      'lat': 31.23,
      'lng': 121.47,
    });
  }

  Future<void> reportException(int id, String type, String description) async {
    await _api.post('/driver/tasks/$id/exception', data: {
      'type': type,
      'description': description,
      'images': <String>[],
    });
  }

  Future<void> reportNode(int id, String node) async {
    await _api.post('/driver/tasks/$id/nodes', data: {'node': node, 'lat': 31.23, 'lng': 121.47});
  }
}

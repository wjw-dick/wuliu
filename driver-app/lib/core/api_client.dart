import 'package:dio/dio.dart';
import 'prefs.dart';

class ApiClient {
  ApiClient._();
  static final ApiClient instance = ApiClient._();

  static const baseUrl = 'https://www.quantaida.cloud/api';

  Dio get dio {
    final d = Dio(BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 20),
    ));
    d.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await Prefs.token();
        if (token != null && token.isNotEmpty) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onResponse: (response, handler) {
        final body = response.data;
        if (body is Map && body['code'] == 0) {
          response.data = body['data'];
          handler.next(response);
        } else {
          handler.reject(DioException(
            requestOptions: response.requestOptions,
            message: (body is Map && body['message'] != null) ? body['message'].toString() : '请求失败',
          ));
        }
      },
      onError: (e, handler) {
        handler.next(e);
      },
    ));
    return d;
  }

  Future<dynamic> get(String path, {Map<String, dynamic>? query}) async {
    final r = await dio.get(path, queryParameters: query);
    return r.data;
  }

  Future<dynamic> post(String path, {Object? data}) async {
    final r = await dio.post(path, data: data);
    return r.data;
  }
}

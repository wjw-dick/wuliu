class TaskItem {
  final int id;
  final String waybillNo;
  final String status;
  final String statusName;
  final String origin;
  final String destination;
  final String senderName;
  final String senderAddress;
  final String receiverName;
  final String receiverAddress;
  final String receiverPhone;
  final String cargoName;
  final double weight;
  final double volume;
  final int pieces;
  final double codAmount;
  final String plateNo;
  final String pickupTime;
  final String remark;

  TaskItem({
    required this.id,
    required this.waybillNo,
    required this.status,
    required this.statusName,
    required this.origin,
    required this.destination,
    required this.senderName,
    required this.senderAddress,
    required this.receiverName,
    required this.receiverAddress,
    required this.receiverPhone,
    required this.cargoName,
    required this.weight,
    required this.volume,
    required this.pieces,
    required this.codAmount,
    required this.plateNo,
    required this.pickupTime,
    required this.remark,
  });

  factory TaskItem.fromJson(Map<String, dynamic> j) {
    double d(Object? v) {
      if (v == null) return 0;
      if (v is num) return v.toDouble();
      return double.tryParse(v.toString()) ?? 0;
    }

    int i(Object? v) {
      if (v == null) return 0;
      if (v is num) return v.toInt();
      return int.tryParse(v.toString()) ?? 0;
    }

    return TaskItem(
      id: i(j['id']),
      waybillNo: (j['waybillNo'] ?? '').toString(),
      status: (j['status'] ?? '').toString(),
      statusName: (j['statusName'] ?? '').toString(),
      origin: (j['origin'] ?? '').toString(),
      destination: (j['destination'] ?? '').toString(),
      senderName: (j['senderName'] ?? '').toString(),
      senderAddress: (j['senderAddress'] ?? '').toString(),
      receiverName: (j['receiverName'] ?? '').toString(),
      receiverAddress: (j['receiverAddress'] ?? '').toString(),
      receiverPhone: (j['receiverPhone'] ?? '').toString(),
      cargoName: (j['cargoName'] ?? '').toString(),
      weight: d(j['weight']),
      volume: d(j['volume']),
      pieces: i(j['pieces']),
      codAmount: d(j['codAmount']),
      plateNo: (j['plateNo'] ?? '').toString(),
      pickupTime: (j['pickupTime'] ?? '').toString(),
      remark: (j['remark'] ?? '').toString(),
    );
  }
}

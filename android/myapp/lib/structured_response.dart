class StructuredResponse {
  String mStatus;
  String mMessage;
  Object mData;

  StructuredResponse({
    required this.mStatus,
    required this.mMessage,
    required this.mData,
  });

  factory StructuredResponse.fromJson(Map<String, dynamic> json) {
    return StructuredResponse(
      mStatus: json['mStatus'] as String,
      mMessage: json['mMessage'] as String,
      mData: json['mData'] as Object,
    );
  }

  Map toMap() {
    var map = new Map();
    map["mStatus"] = mStatus;
    map["mMessage"] = mMessage;
    map["mData"] = mData;

    return map;
  }
}

class BuzzUser {
  String mUserID;
  String mBio;

  BuzzUser({
    required this.mUserID,
    required this.mBio,
  });

  factory BuzzUser.fromJson(Map<String, dynamic> json) {
    return BuzzUser(
      mUserID: json['mUserID'] as String,
      mBio: json['mBio'] as String,
    );
  }

  Map toMap() {
    var map = new Map();
    map["mUserID"] = mUserID;
    map["mBio"] = mBio;

    return map;
  }
}

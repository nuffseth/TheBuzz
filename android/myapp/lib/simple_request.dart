class SimpleRequest {
  String mMessage;
  String mSessionKey;
  String mEmail;
  //jsonFile mFiles;
  int messageLink;
  int commentLink;

  SimpleRequest(
      {required this.mMessage,
      required this.mSessionKey,
      required this.mEmail,
      //required this.mFiles,
      required this.messageLink,
      required this.commentLink});

  factory SimpleRequest.fromJson(Map<String, dynamic> json) {
    return SimpleRequest(
      mMessage: json['mMessage'] as String,
      mSessionKey: json['mSessionKey'] as String,
      mEmail: json['mEmail'] as String,
      // mFiles: json['mFiles'] as jsonFile,
      messageLink: json['messageLink'] as int,
      commentLink: json['commentLink'] as int,
    );
  }

  Map toMap() {
    var map = new Map();
    map["mMessage"] = mMessage;
    map["mSessionKey"] = mSessionKey;
    map["mEmail"] = mEmail;
    // map["mFiles"] = mFiles;
    map["messageLink"] = messageLink;
    map["commentLink"] = commentLink;

    return map;
  }
}

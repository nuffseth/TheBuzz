class BuzzPost {
  String mUserID;
  int mMsgId;
  String mContent;
  int mNumLikes;
  // int mMsgLink;
  // int mCmtLink;
  // mCmtList;
  // mFileList

  BuzzPost({
    required this.mUserID,
    required this.mMsgId,
    required this.mContent,
    required this.mNumLikes,
    // required this.mMsgLink,
    // required this.mCmtLink
    // mCmtList,
    // mFileList
  });

  factory BuzzPost.fromJson(Map<String, dynamic> json) {
    return BuzzPost(
      mUserID: json['mUserID'] as String,
      mMsgId: json['mMsgID'] as int,
      mContent: json['mContent'] as String,
      mNumLikes: json['mNumLikes'] as int,
      // mMsgLink: json['mMsgLink'] as int,
      // mCmtLink: json['mCmtLink'] as int,
    );
  }

  Map toMap() {
    var map = new Map();
    map["mUserID"] = mUserID;
    map["mMsgID"] = mMsgId;
    map["mContent"] = mContent;
    map["mNumLikes"] = mNumLikes;

    return map;
  }
}

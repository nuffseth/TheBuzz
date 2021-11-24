class BuzzPost {
  // String mUserID;
  int mId;
  String mMessage;
  int mLikes;
  // int mMsgLink;
  // int mCmtLink;
  // mCmtList;
  // mFileList

  BuzzPost({
    // required this.mUserID,
    required this.mId,
    required this.mMessage,
    required this.mLikes,
    // required this.mMsgLink,
    // required this.mCmtLink
    // mCmtList,
    // mFileList
  });

  factory BuzzPost.fromJson(Map<String, dynamic> json) {
    return BuzzPost(
      //mUserID: json['mUserID'] as String,
      mId: json['mMsgID'] as int,
      mMessage: json['mContent'] as String,
      mLikes: json['mNumLikes'] as int,
      // mMsgLink: json['mMsgLink'] as int,
      // mCmtLink: json['mCmtLink'] as int,
    );
  }
}

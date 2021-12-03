class BuzzComment {
  String mUserID;
  int mCmtId;
  String mContent;
  // int mMsgLink;
  // int mCmtLink;
  bool isFlagged;

  BuzzComment({
    required this.mUserID,
    required this.mCmtId,
    required this.mContent,
    // required this.mMsgLink,
    // required this.mCmtLink
    // mCmtList,
    // mFileList
    required this.isFlagged,
  });

  factory BuzzComment.fromJson(Map<String, dynamic> json) {
    return BuzzComment(
      mUserID: json['mUserID'] as String,
      mCmtId: json['mCommentID'] as int,
      mContent: json['mContent'] as String,
      // mMsgLink: json['mMsgLink'] as int,
      // mCmtLink: json['mCmtLink'] as int,
      isFlagged: false,
    );
  }
}

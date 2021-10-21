class BuzzPost {
  int mId;
  String mMessage;
  int mLikes;

  BuzzPost({
    required this.mId,
    required this.mMessage,
    required this.mLikes,
  });

  factory BuzzPost.fromJson(Map<String, dynamic> json) {
    return BuzzPost(
      mId: json['mId'] as int,
      mMessage: json['mMessage'] as String,
      mLikes: json['mLikes'] as int,
    );
  }
}

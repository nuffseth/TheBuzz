// ignore_for_file: file_names

class BuzzPost {
    final int msgID;
    final String msg;
    final int numLikes;

  BuzzPost({
    required this.msgID,
    required this.msg,
    required this.numLikes,
  });

  factory BuzzPost.fromJson(Map<String, dynamic> json) {
    return BuzzPost(
      msgID: json['msgID'],
      msg: json['msg'],
      numLikes: json['numlikes'],
    );
  }
}
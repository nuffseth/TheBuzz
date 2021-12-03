import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myapp/buzz_comment.dart';
import 'package:myapp/buzz_post.dart';
import 'package:myapp/buzz_user.dart';
import 'package:myapp/main.dart';

void main() {
  test('testing BuzzPost class', () {
    BuzzPost testPost = BuzzPost(
        mMsgId: 0,
        mUserID: 'test_user',
        mContent: 'testing BuzzPost class',
        mNumLikes: 0,
        isFlagged: false,
        ad: '');

    String contentTest = testPost.mContent;
    expect(contentTest, 'testing BuzzPost class');
  });

  test('testing BuzzComment class creation', () {
    BuzzComment testComment = BuzzComment(
        mCmtId: 0,
        mUserID: "test_user",
        mContent: 'testing BuzzComment class',
        isFlagged: false);

    String contentTest = testComment.mContent;
    expect(contentTest, 'testing BuzzComment class');
  });

  test('testing BuzzUser class creation', () {
    BuzzUser testUser =
        BuzzUser(mUserID: "test_user", mBio: "testing BuzzUser class");

    String bioTest = testUser.mBio;
    expect(bioTest, 'testing BuzzUser class');
  });
}

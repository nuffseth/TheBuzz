import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:myapp/buzz_user.dart';
import 'package:myapp/main.dart';
import 'constants.dart';

// everything we need to communicate with the backend server exists here

import 'buzz_comment.dart';
import 'buzz_post.dart';

class DataModel {
  // singleton implementation
  static DataModel model = DataModel();

  Future<List<BuzzPost>> fetchBuzzList() async {
    final List<BuzzPost> _messages = [];
    // parses the json found on the heroku link
    final response = await http.get(
        Uri.parse('https://limitless-caverns-65131.herokuapp.com/messages'));

    // 200 response = we chillin
    if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
      // this will contain all of our messages but in json language
      final _allPosts = jsonDecode(response.body);
      // need to go through each one of those yucky json things and decode it
      for (var e in _allPosts["mData"]) {
        _messages.add(BuzzPost.fromJson(e));
      }
      return _messages;
    } else {
      // not 200 response = we are not, in fact, chillin
      throw Exception('Failed to load');
    }
  }

  Future<BuzzPost> postMessage(String url, {required Map body}) async {
    return http.post(Uri.parse(url), body: body).then((http.Response response) {
      final int statusCode = response.statusCode;

      if (statusCode < 200 || statusCode > 400 || json == null) {
        throw Exception("Error while fetching data");
      }
      return BuzzPost.fromJson(json.decode(response.body));
    });
  }

  Future<List<BuzzComment>> fetchCommentsList(int msgID) async {
    final List<BuzzComment> _comments = [];
    // parses the json found on the heroku link
    final response = await http.get(Uri.parse(
        'https://limitless-caverns-65131.herokuapp.com/messages/' +
            msgID.toString() +
            '/comments'));

    // 200 response = we chillin
    if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
      // this will contain all of our messages but in json language
      final _allComments = jsonDecode(response.body);
      // need to go through each one of those yucky json things and decode it
      for (var e in _allComments["mData"]) {
        _comments.add(BuzzComment.fromJson(e));
      }
      return _comments;
    } else {
      // not 200 response = we are not, in fact, chillin
      throw Exception('Failed to load');
    }
  }

  Future<BuzzUser> fetchUser(String userID) async {
    // parses the json found on the heroku link
    final response =
        await http.get(Uri.parse(Constants.url + '/users/' + userID));

    // BuzzUser user;
    // 200 response = we chillin
    if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
      // this will contain all of our messages but in json language
      final _user = jsonDecode(response.body);
      for (var e in _user["mData"]) {
        return BuzzUser.fromJson(e);
      }
    } else {
      // not 200 response = we are not, in fact, chillin
      throw Exception('Failed to load user ' + userID);
    }
    throw Exception('something weird happened');
  }

  // Future<bool> isFlagged(int msgID) async {}

  Future<int> setFlagMsg(int msgID, String userID) async {
    // parses the json found on the heroku link
    final response = await http.put(
        Uri.parse(Constants.url + '/messages/' + msgID.toString() + '/flags'));

    // 200 response = we chillin
    if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
      return 0;
    } else {
      // not 200 response = we are not, in fact, chillin
      throw Exception('Failed to flag messsage');
    }
  }

  void setFlagCmt(int msgID, int cmtID, String userID) async {
    // parses the json found on the heroku link
    final response = await http.put(Uri.parse(Constants.url +
        '/messages/' +
        msgID.toString() +
        '/comments/' +
        cmtID.toString() +
        '/flags'));

    // 200 response = we chillin
    if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
      return;
    } else {
      // not 200 response = we are not, in fact, chillin
      throw Exception('Failed to flag comment');
    }
  }
}

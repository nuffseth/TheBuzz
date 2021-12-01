//call data model's post mthod to submit the

// Datamodel.model.postmsg
// i think each like needs to be a post to the backend server that adds to mLikes but this can just be an empty json?
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:myapp/buzz_post.dart';
import 'package:myapp/structured_response.dart';

import 'constants.dart';

void login(String mMessage) async {
  String sessionKey = "";
  // parses the json found on the heroku link
  final response = await http.post(
    Uri.parse(Constants.url + '/login'),
    // headers: <String, String>{
    //   'Content-Type': 'application/json; charset=UTF-8',
    // },
    body: jsonEncode(<String, String>{
      "mMessage": Constants.idTokenString,
    }),
  );

  // 200 response = we chillin
  if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
    // get the session key from the response
    var parsedJson = jsonDecode(response.body);
    Constants.sessionKey = parsedJson['mData'];
    print(Constants.sessionKey);
  } else {
    // not 200 response = we are not, in fact, chillin
    throw Exception('Failed to flag messsage');
  }
}

Future<http.Response> createPost(String mMessage) async {
  return http.post(
    Uri.parse(Constants.url + '/messages'),
    body: jsonEncode(<String, String>{
      "mMessage": mMessage,
      "mSessionKey": Constants.sessionKey,
      "mEmail": Constants.username,
    }),
  );
}

Future<http.Response> editPost(String mMessage, int mId) async {
  final response = await http.put(
    Uri.parse(Constants.url + '/messages/' + mId.toString()),
    body: jsonEncode(<String, String>{
      "mMessage": mMessage,
      "mSessionKey": Constants.sessionKey,
      "mEmail": Constants.username,
    }),
  );
  if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
    // get the session key from the response
    var parsedJson = jsonDecode(response.body);
  } else {
    // not 200 response = we are not, in fact, chillin
    throw Exception('Failed to edit messsage');
  }
  print(response.body);
  return response;
}

Future<http.Response> deletePost(int mId) async {
  final response = await http.delete(
    Uri.parse(Constants.url + '/messages/' + mId.toString()),
    body: jsonEncode(<String, String>{
      "mSessionKey": Constants.sessionKey,
      "mEmail": Constants.username,
    }),
  );
  if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
    // get the session key from the response
    var parsedJson = jsonDecode(response.body);
  } else {
    // not 200 response = we are not, in fact, chillin
    throw Exception('Failed to delete messsage');
  }
  print(response.body);
  return response;
}

Future<http.Response> incrementLikes(String mId) {
  return http.post(
    Uri.parse(Constants.url + '/messages/' + mId + '/likes'),
    body: jsonEncode(<String, String>{
      "mSessionKey": Constants.sessionKey,
      "mEmail": Constants.username,
    }),
  );
}

Future<http.Response> decrementLikes(String mId) {
  return http.post(
    Uri.parse(Constants.url + '/messages/' + mId + '/dislikes'),
    body: jsonEncode(<String, String>{
      "mSessionKey": Constants.sessionKey,
      "mEmail": Constants.username,
    }),
  );
}

Future<http.Response> getComments(int mMsgId) {
  return http.get(
    Uri.parse(Constants.url + '/messages/' + mMsgId.toString() + '/comments'),
    headers: <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
    },
  );
}

Future<http.Response> flagMsg(int mId) async {
  final response = await http.put(
    Uri.parse(Constants.url + '/messages/' + mId.toString() + '/flags'),
    body: jsonEncode(<String, String>{
      "mSessionKey": Constants.sessionKey,
      "mEmail": Constants.username,
    }),
  );
  if (response.statusCode == 200 && jsonDecode(response.body) != Null) {
    // get the session key from the response
    var parsedJson = jsonDecode(response.body);
  } else {
    // not 200 response = we are not, in fact, chillin
    throw Exception('Failed to flag messsage');
  }
  print(response.body);
  return response;
}

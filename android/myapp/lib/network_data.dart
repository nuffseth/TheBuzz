//call data model's post mthod to submit the

// Datamodel.model.postmsg
// i think each like needs to be a post to the backend server that adds to mLikes but this can just be an empty json?
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:myapp/buzz_post.dart';

Future<http.Response> createPost(String mMessage) async {
  return http.post(
    Uri.parse('https://localhost:4567/messages'),
    headers: <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
    },
    body: jsonEncode(<String, String>{
      'mMessage': mMessage,
    }),
  );
}

Future<http.Response> editPost(String mMessage, String mId) async {
  return http.put(
    Uri.parse('https://localhost:4567/messages/' + mId),
    headers: <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
    },
    body: jsonEncode(<String, String>{
      'mMessage': mMessage,
    }),
  );
}

Future<http.Response> deletePost(String mId) async {
  return http.delete(
    Uri.parse('https://localhost:4567/messages/' + mId),
    headers: <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
    },
  );
}

Future<http.Response> incrementLikes(String mId) {
  return http.post(
    Uri.parse('https://localhost:4567/messages/' + mId + '/likes'),
    headers: <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
    },
  );
}

Future<http.Response> decrementLikes(String mId) {
  return http.post(
    Uri.parse('https://localhost:4567/messages/' + mId + '/dislikes'),
    headers: <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
    },
  );
}

Future<http.Response> getComments(String mId) {
  return http.get(
    Uri.parse('https://localhost:4567/messages/' + mId + '/comments'),
    headers: <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
    },
  );
}

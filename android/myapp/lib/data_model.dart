import 'dart:convert';
import 'package:http/http.dart' as http;
//import 'package:myapp/main.dart';

// everything we need to communicate with the backend server exists here

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
    if (response.statusCode == 200) {
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
}

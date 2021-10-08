import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:myapp/main.dart';

// everything we need to communicate with the backend server exists here


import 'buzz_post.dart';

class DataModel {
  // singleton implementation
  static DataModel model = new DataModel();

  // will hold our decoded messages
  final Future<BuzzPost> _messages = [];

  Future<BuzzPost> fetchBuzzList() async {
    // parses the json found on the heroku link
    final response = await http.get (
      Uri.parse('https://limitless-caverns-65131.herokuapp.com/messages')
    );

    // 200 response = we chillin
    if (response.statusCode == 200) {      
      // this will contain all of our messages but in json language
      final _allPosts = jsonDecode(response.body);
      // need to go through each one of those yucky json things and decode it
      for(var e in _allPosts) {
        BuzzPost.fromJson(e);
        //_messages.add(e);
      }

      return _messages;
      // return BuzzPost.fromJson(jsonDecode(response.body));
    } else { // not 200 response = we are not, in fact, chillin
      throw Exception('Failed to load your shitty shit');
    }
  }

  // the link u need is the heroku  url/ message/function?
}
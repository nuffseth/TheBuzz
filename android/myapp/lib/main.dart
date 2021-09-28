import 'package:flutter/material.dart';
import 'package:myapp/data_model.dart';

import 'buzz_post.dart';
import 'data_model.dart';

// void main() { runApp(const MyApp()); }
void main() => runApp(MyApp());

/* 
*
* 
* 
* 
* 
*/
class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // ultimate root of the app
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'The Buzz',
      theme: ThemeData( primarySwatch: Colors.purple ),
      // declares home screen of our app
      // we can pass it in the "constructor" of another class so we can put its
      // functionality in there
      home: const Buzz(title: 'The Buzz Home Page'),
    );
  }
}

/* 
*
* 
* 
* 
* 
*/
// homepage widget that gets "passed in"/returned in the home argument in app.build
class Buzz extends StatefulWidget {
  const Buzz({Key? key, required this.title}) : super(key: key);
/*
  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".
*/
  final String title;

  // the call in app.build come here i think
  @override
  State<Buzz> createState() => _BuzzPostsState();
}


/* 
*
* 
* 
* 
* 
*/
class _BuzzPostsState extends State<Buzz> {
  late Future<BuzzPost> jsonPosts;

  // list of posts
  final _posts = [];
  final _liked = [];
  
  @override
  void initState(){
    super.initState();

    // this should still be a yucky list of json shit that we still wanna convert
    jsonPosts = DataModel.model.fetchBuzzList();
  }
  


  /* build function
   *
   * 
   * 
   * 
   * 
   */
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar (
        backgroundColor: Colors.deepPurple,
        title: const Text('Buzz Buzz Buzz'),
      ),
      //body: _buildPosts(), 
      body: Center(
        child: FutureBuilder<BuzzPost> (
          future: jsonPosts,
          builder: (context, snapshot){ 
            if (snapshot.hasData) {
                return Text(snapshot.data!.msg);
              } else if (snapshot.hasError){
                return Text('uh oh there was a fucky wucky: ${snapshot.error}');
              }
              // default return is just a loading bouncy ball of death
              return const CircularProgressIndicator();
          },
        )

      )
    );
  }


  /* buildPosts function
   *
   * 
   * 
   * 
   * 
   */
  Widget _buildPosts() {
    // we use listview.builder since we don't know how many items we'll have 
    // in the list, and it could technically be infinite
    // https://api.flutter.dev/flutter/widgets/ListView/ListView.builder.html
    return ListView.builder(
      padding: const EdgeInsets.all(16.0),
      scrollDirection: Axis.vertical,
      // will be called once per post - this is what actually creates the list items
      itemBuilder: (context, i) {
        final idx = i; // maybe/probably redundant but putting it here in case we wanna do funky pair stuff

        // this is where the adding of posts to our list storing them takes place

        // i think dave said this is wrong
        _posts.add(DataModel.model.fetchBuzzList());

        return _buildRow(_posts[idx]);
      },
      // how many items it should expect to build
      itemCount: _posts.length,
    );

  }


  /* buildRow function
   *
   * 
   * 
   * 
   * 
   */

                          // v NOTE: STRING PLACEHOLDER
  Widget _buildRow(String post) {
    final alreadyLiked = _liked.contains(post);
    
    return ListTile (
      title: Text(post),
      trailing: Icon( // this is what puts the correct icon w/in the posts
        // ============ handles placement of icon on screen
        alreadyLiked ? Icons.favorite : Icons.favorite_border,
        color: alreadyLiked ? Colors.orange : Colors.green,
      ),
      // ============ handles interactivity of icon
      onTap: (){
        setState(() {
          if (alreadyLiked) {
            _liked.remove(post);
          } else {
            _liked.add(post);
          }
        });
      },
    );
  }

}
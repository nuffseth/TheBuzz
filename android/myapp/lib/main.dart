import 'package:flutter/material.dart';
import 'package:myapp/comment.dart';
import 'package:myapp/data_model.dart';
//import 'package:myapp/login.dart';
import 'package:myapp/network_data.dart';
import 'package:myapp/camera.dart';
import 'package:camera/camera.dart';
//import 'package:myapp/profile.dart';
//import 'package:myapp/splash.dart';

import 'buzz_post.dart';
import 'data_model.dart';
import 'network_data.dart';
import 'add_post.dart';

late var cameras;
// void main() => runApp(const MyApp());
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  cameras = await availableCameras();
  runApp(const MyApp());
}

/* 
* This class is the ultimate root of the app
*/
class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'The Buzz',
      theme: ThemeData(primarySwatch: Colors.purple),
      // declares home screen of our app
      // we can pass it in the "constructor" of another class so we can put its
      // functionality in there
      routes: {
        '/': (_) => const MyHomePage(title: 'The Buzz Home Page'),
        //const SplashScreen(),
        //'/login': (_) => const LoginScreen(),
        '/home': (_) => const MyHomePage(title: 'The Buzz Home Page'),
        //'/profile': (_) => const ProfileScreen(),
        '/comments': (_) => const CommentScreen(),

        '/camera': (_) => TakePictureScreen(camera: cameras[0]),
      },
    );
  }
}

/* 
* This widget is the home page of your application. It is stateful, meaning
* that it has a State object (defined below) that contains fields that affect
* how it looks.
*
* This class is the configuration for the state. It holds the values (in this
* ase the title) provided by the parent (in this case the App widget) and
* used by the build method of the State. Fields in a Widget subclass are
* always marked "final".
*/
// homepage widget that gets "passed in"/returned in the home argument in app.build

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  // the call in app.build come here i think
  @override
  State<MyHomePage> createState() => _BuzzPostsState();
}

/* 
*
* 
*/

late Future<List<BuzzPost>> jsonPosts;

class _BuzzPostsState extends State<MyHomePage> {
  // late Future<List<BuzzPost>> jsonPosts;

  // list of posts
  final _posts = [];
  final _liked = [];
  final _disliked = [];
  final addController = TextEditingController();
  final editController = TextEditingController();

  @override
  void dispose() {
    // Clean up the controller when the widget is disposed.
    addController.dispose();
    editController.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();

    // this should still be a yucky list of json shit that we still wanna convert
    jsonPosts = DataModel.model.fetchBuzzList();
  }

  /*
   * Primary build function that calls different other functions
   */
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Buzz Buzz Buzz!!!'),
          backgroundColor: Colors.amber,
          actions: <Widget>[
            IconButton(
                icon: Icon(Icons.account_circle),
                onPressed: () {
                  //Navigator.pushReplacementNamed(context, '/login');
                }),
          ],
        ),
        //body: _buildPosts(),\

        // display all messages
        body: Center(
            child: FutureBuilder<List<BuzzPost>>(
          future: jsonPosts,
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return Scrollbar(
                child: RefreshIndicator(
                  onRefresh: () async {
                    _refreshData();
                  },
                  child: _buildPosts(snapshot.data!),
                ),
              );
            } else if (snapshot.hasError) {
              return Text('Error: ${snapshot.error}');
            }
            // default return is just a loading bouncy ball of death
            return const CircularProgressIndicator();
          },
        )),

        // post a new message
        floatingActionButton: FloatingActionButton(
          child: Icon(Icons.add_outlined),
          backgroundColor: Colors.deepPurple,
          foregroundColor: Colors.white,
          onPressed: () {
            showDialog(
                context: context,
                builder: (BuildContext context) {
                  return AlertDialog(
                      scrollable: true,
                      title: Text('Add Post'),
                      content: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Form(
                          child: Column(
                            children: <Widget>[
                              TextFormField(
                                controller: addController,
                                decoration: InputDecoration(
                                  labelText: 'Message',
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      actions: [
                        ElevatedButton(
                            child: Text('Cancel'),
                            onPressed: () => Navigator.pop(context)),
                        ElevatedButton(
                            child: Text('Submit'),
                            onPressed: () {
                              createPost(addController.text);
                              _refreshData();
                              Navigator.pop(context);
                            })
                      ]);
                });
          },
        ));
  }

  /* buildPosts function
   */
  Widget _buildPosts(List<BuzzPost> postData) {
    // we use listview.builder since we don't know how many items we'll have
    // in the list, and it could technically be infinite
    // https://api.flutter.dev/flutter/widgets/ListView/ListView.builder.html
    return ListView.builder(
      padding: const EdgeInsets.all(16.0),
      scrollDirection: Axis.vertical,
      itemCount: postData.length,
      // will be called once per post - this is what actually creates the list items
      itemBuilder: (context, i) {
        _posts.add(postData[i].mContent);
        return _buildRow(postData[i]);
      },
      // how many items it should expect to build
    );
  }

  /* buildRow function
   */

  // v NOTE: STRING PLACEHOLDER
  Widget _buildRow(BuzzPost post) {
    bool alreadyLiked = _liked.contains(post.mMsgId);
    bool alreadyDisliked = _disliked.contains(post.mMsgId);
    final editController = TextEditingController(text: post.mContent);

    return ListTile(
      title: Text(post.mContent),
      /*
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          IconButton(
              icon: Icon(Icons.edit),
              onPressed: () {
                showDialog(
                    context: context,
                    builder: (BuildContext context) {
                      return AlertDialog(
                          scrollable: true,
                          title: Text('Edit Post'),
                          content: Padding(
                            padding: const EdgeInsets.all(8.0),
                            child: Form(
                              child: Column(
                                children: <Widget>[
                                  TextFormField(
                                    controller: editController,
                                    decoration: InputDecoration(
                                      labelText: 'Message',
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                          actions: [
                            ElevatedButton(
                                child: Text('Cancel'),
                                onPressed: () => Navigator.pop(context)),
                            ElevatedButton(
                                child: Text('Submit'),
                                onPressed: () {
                                  editPost(
                                      editController.text, post.mId.toString());
                                  _refreshData();
                                  Navigator.pop(context);
                                })
                          ]);
                    });
              }),
          IconButton(
            icon: Icon(Icons.delete),
            onPressed: () => showDialog<String>(
              context: context,
              builder: (BuildContext context) => AlertDialog(
                title: const Text('Warning'),
                content: const Text(
                    'Are you sure you would like to delete the message?'),
                actions: <Widget>[
                  TextButton(
                    onPressed: () => Navigator.pop(context, 'No'),
                    child: const Text('No'),
                  ),
                  TextButton(
                    onPressed: () {
                      deletePost(post.mId.toString());
                      _refreshData();
                      Navigator.pop(context, 'Yes');
                    },
                    child: const Text('Yes'),
                  ),
                ],
              ),
            ),
          ),
          IconButton(
              icon: Icon(Icons.add_box),
              onPressed: () {
                // setMessageID(post.mId);
                Navigator.pushReplacementNamed(context, '/comments');
              }),
          IconButton(
              icon: Icon(Icons.add_a_photo_rounded),
              onPressed: () {
                Navigator.pushReplacementNamed(context, '/camera');
              }),
          IconButton(
            icon: Icon(
              Icons.arrow_circle_up,
              color: alreadyLiked ? Colors.green : Colors.black,
            ),
            onPressed: () {
              setState(() {
                alreadyLiked = _liked.contains(post.mId);
                alreadyDisliked = _disliked.contains(post.mId);
              });
              if (alreadyLiked) {
                _liked.remove(post.mId);
                decrementLikes(post.mId.toString());
                post.mLikes--;
              } else {
                if (alreadyDisliked) {
                  _liked.add(post.mId);
                  _disliked.remove(post.mId);
                  incrementLikes(post.mId.toString());
                  incrementLikes(post.mId.toString());
                  post.mLikes = post.mLikes + 2;
                } else {
                  _liked.add(post.mId);
                  incrementLikes(post.mId.toString());
                  post.mLikes++;
                }
              }
            },
          ),
          Text(post.mLikes.toString()),
          IconButton(
              icon: Icon(Icons.arrow_circle_down,
                  color: alreadyDisliked ? Colors.red : Colors.black),
              onPressed: () {
                setState(() {
                  alreadyLiked = _liked.contains(post.mId);
                  alreadyDisliked = _disliked.contains(post.mId);
                });
                if (alreadyDisliked) {
                  _disliked.remove(post.mId);
                  incrementLikes(post.mId.toString());
                  post.mLikes++;
                } else {
                  if (alreadyLiked) {
                    _disliked.add(post.mId);
                    _liked.remove(post.mId);
                    decrementLikes(post.mId.toString());
                    decrementLikes(post.mId.toString());
                    post.mLikes = post.mLikes - 2;
                  } else {
                    _disliked.add(post.mId);
                    decrementLikes(post.mId.toString());
                    post.mLikes--;
                  }
                }
              }),
        ],
      ),
      */
      //more stuff
    );
  }

  Future _refreshData() async {
    await Future.delayed(Duration(seconds: 1));
    jsonPosts = DataModel.model.fetchBuzzList();
    setState(() {});
  }
}

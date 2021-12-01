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
import 'constants.dart';
import 'data_model.dart';
import 'network_data.dart';
import 'add_post.dart';

// ignore: prefer_typing_uninitialized_variables
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
  final newThing = Constants.sessionKey;
  final sessionKey = "57bd40b6-34b3-464c-b47f-03430b0b31db";
  final userEmail = "arg422";

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

    // set session key
    login(Constants.idTokenString);
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
                icon: const Icon(Icons.account_circle),
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
          child: const Icon(Icons.add_outlined),
          backgroundColor: Colors.red,
          foregroundColor: Colors.white,
          onPressed: () {
            showDialog(
                context: context,
                builder: (BuildContext context) {
                  return AlertDialog(
                      scrollable: true,
                      title: const Text('Add Post'),
                      content: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Form(
                          child: Column(
                            children: <Widget>[
                              TextFormField(
                                controller: addController,
                                decoration: const InputDecoration(
                                  labelText: 'Message',
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      actions: [
                        ElevatedButton(
                            child: const Text('Cancel'),
                            onPressed: () => Navigator.pop(context)),
                        ElevatedButton(
                            child: const Text('Submit'),
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
    int _likes = post.mNumLikes;

    return ExpansionTile(
        title: Text(post.mContent),
        leading: Text(
          '$_likes',
        ),
        // trailing: Icon(Icons.more_vert),
        // onTap: () {
        //   setState(() {
        //     return ListTile(title: "testing")
        //   });
        // },
        //leading: Text(post.mNumLikes.toString()),
        children: [
          Row(
            children: [
              // edit button
              IconButton(
                  icon: const Icon(Icons.edit),
                  onPressed: () {
                    showDialog(
                        context: context,
                        builder: (BuildContext context) {
                          return AlertDialog(
                              scrollable: true,
                              title: const Text('Edit Post'),
                              content: Padding(
                                padding: const EdgeInsets.all(8.0),
                                child: Form(
                                  child: Column(
                                    children: <Widget>[
                                      TextFormField(
                                        controller: editController,
                                        decoration: const InputDecoration(
                                          labelText: 'Message',
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                              actions: [
                                ElevatedButton(
                                    child: const Text('Cancel'),
                                    onPressed: () => Navigator.pop(context)),
                                ElevatedButton(
                                    child: const Text('Submit'),
                                    onPressed: () {
                                      editPost(
                                          editController.text, post.mMsgId);
                                      _refreshData();
                                      Navigator.pop(context);
                                    })
                              ]);
                        });
                  }),
              // delete button
              IconButton(
                icon: const Icon(Icons.delete),
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
                          deletePost(post.mMsgId);
                          _refreshData();
                          Navigator.pop(context, 'Yes');
                        },
                        child: const Text('Yes'),
                      ),
                    ],
                  ),
                ),
              ),
              // comments button
              IconButton(
                  icon: const Icon(Icons.chat_bubble),
                  onPressed: () {
                    Constants.currentMsg = post.mMsgId;
                    Navigator.pushReplacementNamed(context, '/comments');
                  }),
              // camera button
              IconButton(
                  icon: const Icon(Icons.add_a_photo_rounded),
                  onPressed: () {
                    Navigator.pushReplacementNamed(context, '/camera');
                  }),
              // like button
              IconButton(
                icon: Icon(
                  Icons.arrow_circle_up,
                  color: alreadyLiked ? Colors.green : Colors.black,
                ),
                onPressed: () {
                  setState(() {
                    alreadyLiked = _liked.contains(post.mMsgId);
                    alreadyDisliked = _disliked.contains(post.mMsgId);
                  });
                  if (alreadyLiked) {
                    _liked.remove(post.mMsgId);
                    decrementLikes(post.mMsgId.toString());
                    post.mNumLikes--;
                  } else {
                    if (alreadyDisliked) {
                      _liked.add(post.mMsgId);
                      _disliked.remove(post.mMsgId);
                      incrementLikes(post.mMsgId.toString());
                      incrementLikes(post.mMsgId.toString());
                      post.mNumLikes = post.mNumLikes + 2;
                    } else {
                      _liked.add(post.mMsgId);
                      incrementLikes(post.mMsgId.toString());
                      post.mNumLikes++;
                    }
                  }
                },
              ),
              // dislike button
              IconButton(
                  icon: Icon(Icons.arrow_circle_down,
                      color: alreadyDisliked ? Colors.red : Colors.black),
                  onPressed: () {
                    setState(() {
                      alreadyLiked = _liked.contains(post.mMsgId);
                      alreadyDisliked = _disliked.contains(post.mMsgId);
                    });
                    if (alreadyDisliked) {
                      _disliked.remove(post.mMsgId);
                      incrementLikes(post.mMsgId.toString());
                      post.mNumLikes++;
                    } else {
                      if (alreadyLiked) {
                        _disliked.add(post.mMsgId);
                        _liked.remove(post.mMsgId);
                        decrementLikes(post.mMsgId.toString());
                        decrementLikes(post.mMsgId.toString());
                        post.mNumLikes = post.mNumLikes - 2;
                      } else {
                        _disliked.add(post.mMsgId);
                        decrementLikes(post.mMsgId.toString());
                        post.mNumLikes--;
                      }
                    }
                  }),
              //flag content button
              IconButton(
                  icon: const Icon(Icons.flag),
                  onPressed: () {
                    // send POST request to backend to flag this message
                    flagMsg(post.mMsgId);
                  }),
            ],
          )
        ]
        //more stuff
        );
  }

  Future _refreshData() async {
    await Future.delayed(const Duration(seconds: 1));
    jsonPosts = DataModel.model.fetchBuzzList();
    setState(() {});
  }
}

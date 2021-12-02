import 'package:flutter/material.dart';
import 'package:myapp/buzz_user.dart';
import 'package:myapp/constants.dart';
import 'package:myapp/data_model.dart';
import 'package:myapp/network_data.dart';

import 'buzz_comment.dart';

class UserScreen extends StatefulWidget {
  const UserScreen({Key? key}) : super(key: key);

  @override
  _UserScreenState createState() => _UserScreenState();
}

late Future<BuzzUser> user;

class _UserScreenState extends State<UserScreen> {
  //final _comments = [];
  final userController = TextEditingController();

  @override
  void initState() {
    super.initState();

    // get all the comments for a message
    // TODO: currently msgID is hardcoded in as 1, need to change this!!!
    //comments = DataModel.model.fetchCommentsList(Constants.currentMsg);
    user = DataModel.model.fetchUser(Constants.currentUser);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          title: const Text("User Profile"),
          centerTitle: true,
          leading: IconButton(
              icon: const Icon(Icons.arrow_back),
              onPressed: () {
                Navigator.pushReplacementNamed(context, '/home');
              })),
      body: Center(
        child: Text(Constants.currentUser),
        // child: Text(getBio(Constants.currentUser) as String),
        // child: Row( children: [
        //   const Icon(Icons.favorite),
        //   Text(Constants.currentUser),
        // ])
        //   child: FutureBuilder<BuzzUser>(
        //   future: user,
        //   builder: (context, snapshot) {
        //     if (snapshot.hasData) {
        //       return Scrollbar(
        //         child: RefreshIndicator(
        //           onRefresh: () async {
        //             _refreshData();
        //           },
        //           child: _buildUser(snapshot.data!),
        //         ),
        //       );
        //     } else if (snapshot.hasError) {
        //       return Text('Error: ${snapshot.error}');
        //     }
        //     // default return is just a loading bouncy ball of death
        //     return const CircularProgressIndicator();
        //   },
        // )
      ),
      /*
        floatingActionButton: FloatingActionButton(
          child: const Icon(Icons.add_outlined),
          backgroundColor: Colors.deepPurple,
          foregroundColor: Colors.white,
          onPressed: () {
            showDialog(
                context: context,
                builder: (BuildContext context) {
                  return AlertDialog(
                      scrollable: true,
                      title: const Text('Add Comment'),
                      content: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Form(
                          child: Column(
                            children: <Widget>[
                              TextFormField(
                                controller: commentController,
                                decoration: const InputDecoration(
                                  labelText: 'Comments',
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
                              createComment(commentController.text);
                              _refreshData();
                              Navigator.pop(context);
                            })
                      ]);
                });
          },
        )
        */
    );
  }

  // Widget _buildUsers(BuzzUser userData) {
  //   // we use listview.builder since we don't know how many items we'll have
  //   // in the list, and it could technically be infinite
  //   // https://api.flutter.dev/flutter/widgets/ListView/ListView.builder.html
  //   return ListView.builder(
  //     padding: const EdgeInsets.all(16.0),
  //     scrollDirection: Axis.vertical,
  //     itemCount: 1,
  //     // will be called once per post - this is what actually creates the list items
  //     itemBuilder: (context, i) {
  //       _user.add(userData[i].mContent);
  //       return _buildRow(commentData[i]);
  //     },
  //     // how many items it should expect to build
  //   );
  // }

//   Widget _buildUser(BuzzUser user) {
//     return ListTile(title: user.mUserID),
//   }

  Future<void> _refreshData() async {
    await Future.delayed(const Duration(seconds: 1));
    //comments = DataModel.model.fetchCommentsList(Constants.currentMsg);
    setState(() {});
  }
}

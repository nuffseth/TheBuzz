import 'package:flutter/material.dart';
import 'package:myapp/buzz_post.dart';
import 'package:myapp/data_model.dart';

import 'buzz_comment.dart';

class CommentScreen extends StatefulWidget {
  const CommentScreen({Key? key}) : super(key: key);

  @override
  _CommentScreenState createState() => _CommentScreenState();
}

late Future<List<BuzzComment>> comments;

class _CommentScreenState extends State<CommentScreen> {
  final _comments = [];
  final commentController = TextEditingController();

  @override
  void initState() {
    super.initState();

    // get all the comments for a message
    // currently msgID is hardcoded in as 1, need to change this!!!
    comments = DataModel.model.fetchCommentsList(1);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
            title: Text("Comments"),
            centerTitle: true,
            leading: IconButton(
                icon: Icon(Icons.arrow_back),
                onPressed: () {
                  Navigator.pushReplacementNamed(context, '/home');
                })),
        body: Center(
            child: FutureBuilder<List<BuzzComment>>(
          future: comments,
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return Scrollbar(
                child: RefreshIndicator(
                  onRefresh: () async {
                    _refreshData();
                  },
                  child: _buildComments(snapshot.data!),
                ),
              );
            } else if (snapshot.hasError) {
              return Text('Error: ${snapshot.error}');
            }
            // default return is just a loading bouncy ball of death
            return const CircularProgressIndicator();
          },
        )),
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
                      title: Text('Add Comment'),
                      content: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Form(
                          child: Column(
                            children: <Widget>[
                              TextFormField(
                                controller: commentController,
                                decoration: InputDecoration(
                                  labelText: 'Comments',
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
                              createComment(commentController.text);
                              _refreshData();
                              Navigator.pop(context);
                            })
                      ]);
                });
          },
        ));
  }

  void createComment(String text) {
    Widget build(BuildContext context) {
      return Scaffold(
        appBar: AppBar(
            title: Text("Comments"),
            centerTitle: true,
            leading: IconButton(
                icon: Icon(Icons.arrow_back),
                onPressed: () {
                  Navigator.pushReplacementNamed(context, '/home');
                })),
        body: Column(children: [
          //this is where all the comments go
        ]),
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
                      title: Text('Add Comment'),
                      content: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Form(
                          child: Column(
                            children: <Widget>[
                              TextFormField(
                                controller: commentController,
                                decoration: InputDecoration(
                                  labelText: 'Comments',
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
                              createComment(commentController.text);
                              _refreshData();
                              Navigator.pop(context);
                            })
                      ]);
                });
          },
        ),
      );
    }
  }

  /* buildPosts function
   */
  Widget _buildComments(List<BuzzComment> commentData) {
    // we use listview.builder since we don't know how many items we'll have
    // in the list, and it could technically be infinite
    // https://api.flutter.dev/flutter/widgets/ListView/ListView.builder.html
    return ListView.builder(
      padding: const EdgeInsets.all(16.0),
      scrollDirection: Axis.vertical,
      itemCount: commentData.length,
      // will be called once per post - this is what actually creates the list items
      itemBuilder: (context, i) {
        _comments.add(commentData[i].mContent);
        return _buildRow(commentData[i]);
      },
      // how many items it should expect to build
    );
  }

  Widget _buildRow(BuzzComment comment) {
    return ExpansionTile(title: Text(comment.mContent), children: [
      Row(
        children: [
          // add buttons here if u want
        ],
      )
    ]
        //more stuff
        );
  }

  void _refreshData() {}
}

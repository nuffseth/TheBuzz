import 'package:flutter/material.dart';
import 'package:myapp/constants.dart';
import 'package:myapp/data_model.dart';
import 'package:myapp/network_data.dart';

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
    // TODO: currently msgID is hardcoded in as 1, need to change this!!!
    comments = DataModel.model.fetchCommentsList(Constants.currentMsg);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
            title: const Text("Comments"),
            centerTitle: true,
            leading: IconButton(
                icon: const Icon(Icons.arrow_back),
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
        ));
  }

  void createComment(String text) {
    Widget build(BuildContext context) {
      return Scaffold(
        appBar: AppBar(
            title: const Text("Comments"),
            centerTitle: true,
            leading: IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () {
                  Navigator.pushReplacementNamed(context, '/home');
                })),
        body: Column(children: const [
          //this is where all the comments go
        ]),
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
                                    controller: commentController,
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
                                  editComment(commentController.text,
                                      Constants.currentMsg, comment.mCmtId);
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
                    'Are you sure you would like to delete the comment?'),
                actions: <Widget>[
                  TextButton(
                    onPressed: () => Navigator.pop(context, 'No'),
                    child: const Text('No'),
                  ),
                  TextButton(
                    onPressed: () {
                      deleteComment(Constants.currentMsg, comment.mCmtId);
                      _refreshData();
                      Navigator.pop(context, 'Yes');
                    },
                    child: const Text('Yes'),
                  ),
                ],
              ),
            ),
          ),
        ],
      )
    ]
        //more stuff
        );
  }

  Future<void> _refreshData() async {
    await Future.delayed(const Duration(seconds: 1));
    comments = DataModel.model.fetchCommentsList(Constants.currentMsg);
    setState(() {});
  }
}

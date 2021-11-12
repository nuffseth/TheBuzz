import 'package:flutter/material.dart';
import 'package:myapp/buzz_post.dart';
import 'package:myapp/data_model.dart';

class CommentScreen extends StatefulWidget {
  const CommentScreen({Key? key}) : super(key: key);

  @override
  _CommentScreenState createState() => _CommentScreenState();
}

class _CommentScreenState extends State<CommentScreen> {
  final commentController = TextEditingController();

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
        ));
  }

  void createComment(String text) {}

  void _refreshData() {}
}

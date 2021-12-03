import 'package:flutter/material.dart';
import 'package:myapp/buzz_user.dart';
import 'package:myapp/constants.dart';
import 'package:myapp/data_model.dart';
import 'package:myapp/network_data.dart';
import 'package:http/http.dart' as http;
import 'dart:async';
import 'dart:convert';
import 'buzz_comment.dart';

class UserScreen extends StatefulWidget {
  const UserScreen({Key? key}) : super(key: key);

  @override
  _UserScreenState createState() => _UserScreenState();
}

class _UserScreenState extends State<UserScreen> {
  //final _comments = [];
  // ignore: prefer_typing_uninitialized_variables
  late Future<BuzzUser> user;
  final userController = TextEditingController();

  @override
  void initState() {
    super.initState();
    // get the user
    user = DataModel.model.fetchUser(Constants.currentUser);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          title: Text(Constants.currentUser),
          centerTitle: true,
          leading: IconButton(
              icon: const Icon(Icons.arrow_back),
              onPressed: () {
                Navigator.pushReplacementNamed(context, '/home');
              }),
          actions: <Widget>[
            IconButton(
              icon: const Icon(Icons.block),
              tooltip: 'Block this user',
              onPressed: () {
                // block the user
                blockUser(Constants.currentUser);
              },
            )
          ]),
      body: Center(
        child: FutureBuilder<BuzzUser>(
          future: user,
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return Text(snapshot.data!.mBio);
            } else if (snapshot.hasError) {
              return Text('${snapshot.error}');
            }
            // By default, show a loading spinner.
            return const CircularProgressIndicator();
          },
        ),
      ),
    );
  }

  Future<void> _refreshData() async {
    await Future.delayed(const Duration(seconds: 1));
    //comments = DataModel.model.fetchCommentsList(Constants.currentMsg);
    setState(() {});
  }
}

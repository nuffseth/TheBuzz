// ignore_for_file: avoid_print

import 'package:flutter/material.dart';
import 'package:google_sign_in/google_sign_in.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({Key? key}) : super(key: key);

  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  GoogleSignIn googleSignIn = GoogleSignIn(
      clientId:
          '517754603516-8p8sh7b18oa9o62raoi6chiolj5hd5o6.apps.googleusercontent.com');

  @override
  Widget build(BuildContext context) {
    return Material(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          const Text(
            'Sign in with Google!',
            style: TextStyle(fontSize: 30),
          ),
          ElevatedButton(
            child: const Text('Tap to Sign in'),
            onPressed: () {
              statrSignIn();
            },
          ),
        ],
      ),
    );
  }

  void statrSignIn() async {
    GoogleSignInAccount? user = await googleSignIn.signIn();
    if (user == null) {
      print("Sign In Failed");
    } else {
      Navigator.pushReplacementNamed(context, '/home');
    }
  }
}

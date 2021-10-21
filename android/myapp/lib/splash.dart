import 'package:flutter/material.dart';
import 'package:google_sign_in/google_sign_in.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({Key? key}) : super(key: key);

  @override
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  GoogleSignIn googleSignIn = GoogleSignIn(
      clientId:
          '517754603516-8p8sh7b18oa9o62raoi6chiolj5hd5o6.apps.googleusercontent.com');

  @override
  void initState() {
    checkSignInStatus();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          Text(
            'Welcome !',
            style: TextStyle(fontSize: 30),
          ),
          CircularProgressIndicator(),
        ],
      ),
    );
  }

  void checkSignInStatus() async {
    bool isSignedIn = await googleSignIn.isSignedIn();
    await Future.delayed(Duration(seconds: 2));
    if (isSignedIn) {
      print("user signed in");
      Navigator.pushReplacementNamed(context, '/home')
    } else {
      Navigator.pushReplacementNamed(context, '/login');
    }
  }
}

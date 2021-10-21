import 'package:flutter/material.dart';
import 'package:google_sign_in/google_sign_in.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({Key? key}) : super(key: key);

  @override
  _ProfileScreenState createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  GoogleSignIn googleSignIn = GoogleSignIn(
      clientId:
          '517754603516-8p8sh7b18oa9o62raoi6chiolj5hd5o6.apps.googleusercontent.com');
  late GoogleSignInAccount account;
  late GoogleSignInAuthentication auth;
  bool gotProfile = false;

  @override
  void initState() {
    super.initState();
    getProfile();
  }

  @override
  Widget build(BuildContext context) {
    return gotProfile
        ? Scaffold(
            appBar: AppBar(
              title: Text(" Your Profile "),
              centerTitle: true,
              actions: [
                IconButton(icon: Icon(Icons.exit_to_app), 
                onPressed: () async {
                  await googleSignIn.signOut();
                  Navigator.pushNamedAndRemoveUntil(context, '/', (route) => false);
                })
              ],
            ),
            body: Column(children: [
              Image.network(account.photoUrl, height: 150),
            ]
            Text(account.displayName),
            Text(account.email),
            Text(auth.idToken),
            ),
          )
        : LinearProgressIndicator();
  }

  void getProfile() async {
    await googleSignIn.signInSilently();
    account = googleSignIn.currentUser;
    auth = await account.authentication;
    setState(() {
      gotProfile = true;
    });
  }
}

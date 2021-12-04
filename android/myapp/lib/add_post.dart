/*
* Page to add new BuzzPost
*/
import 'package:flutter/material.dart';
import 'package:myapp/simple_request.dart';
import 'package:myapp/data_model.dart';

import 'buzz_post.dart';

// Define a custom Form widget.
class MyAddForm extends StatefulWidget {
  const MyAddForm({Key? key}) : super(key: key);

  @override
  MyAddFormState createState() {
    return MyAddFormState();
  }
}

// Define a corresponding State class.
// This class holds data related to the form.
class MyAddFormState extends State<MyAddForm> {
  // Create a global key that uniquely identifies the Form widget
  // and allows validation of the form.
  //
  // Note: This is a `GlobalKey<FormState>`,
  // not a GlobalKey<MyCustomFormState>.
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    // Build a Form widget using the _formKey created above.
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextFormField(
            // The validator receives the text that the user has entered.
            validator: (value) {
              if (value == null || value.isEmpty) {
                return 'Please enter some text';
              }
              return null;
            },
          ),
          TextFormField(
            // The validator receives the text that the user has entered.
            validator: (value) {
              if (value == null || value.isEmpty) {
                return 'Please enter some text';
              }
              return null;
            },
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 16.0),
            child: ElevatedButton(
              onPressed: () async {
                // Validate returns true if the form is valid, or false otherwise.
                if (_formKey.currentState!.validate()) {
                  // If the form is valid, display a snackbar. In the real world,
                  // you'd often call a server or save the information in a database.
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Processing Data')),
                  );
                  // call the actual POST request
                  SimpleRequest request = new SimpleRequest(
                    mMessage: "testing harcoded message post",
                    mSessionKey: "57bd40b6-34b3-464c-b47f-03430b0b31db",
                    mEmail: "arg422",
                    messageLink: 0,
                    commentLink: 0,
                  );
                  BuzzPost response = await DataModel.model.postMessage(
                      'https://limitless-caverns-65131.herokuapp.com/messages',
                      body: request.toMap());
                  print(response.mContent);
                }
              },
              child: const Text('Submit'),
            ),
          ),
        ],
      ),
    );
  }
}

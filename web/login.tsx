import * as React from "react";
import GoogleLogin from "react-google-login";
import * as ReactDOM from "react-dom";

var GoogleAuth; // Google Auth object.

export class Login extends React.Component {


    // initClient() {
    //     gapi.client.init({
    //         clientId: '517754603516-8p8sh7b18oa9o62raoi6chiolj5hd5o6.apps.googleusercontent.com',
    //         scope: 'profile email',
    //         response_type: 'id_token'
    //     }).then(function () {
    //         GoogleAuth = gapi.auth2.getAuthInstance();
    
    //         // Listen for sign-in state changes.
    //         GoogleAuth.isSignedIn.listen(updateSigninStatus);
    //     });
    // }

    responseGoogle = (response: any) => {
        console.log(response);
        console.log(response.profileObj);
        console.log(response.getAuthResponse().id_token);
        
      }

    render(){
        console.log("Hello");
        return (
            <div>
            <GoogleLogin
            clientId = '517754603516-8p8sh7b18oa9o62raoi6chiolj5hd5o6.apps.googleusercontent.com'
            buttonText = "Login"
            onSuccess = {this.responseGoogle}
            onFailure = {this.responseGoogle}
            cookiePolicy =  {'single_host_origin'}
            
            />
          </div>
        );
    }

}
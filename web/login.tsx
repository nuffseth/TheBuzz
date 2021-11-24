import * as React from "react";
import GoogleLogin from "react-google-login";
import * as ReactDOM from "react-dom";

export class Login extends React.Component {

    responseGoogle = (response: any) => {
        // console.log(response);
        // console.log(response.profileObj);
        const email = response.profileObj.email;
        //response.getAuthResponse().id_token)
        $.ajax({
            type: "POST",
            url: "/login",
            dataType: "json",
            data: JSON.stringify({mMessage: response.getAuthResponse().id_token}),
            success: function(result: any) {
                result.body
                //console.log(response);
                //TODO: Figure out how to get session key.
                console.log(result.mData);
            }
        });
      }

    render(){
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
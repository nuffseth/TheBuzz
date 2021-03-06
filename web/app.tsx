// Prevent compiler errors when using jQuery.  "$" will be given a type of 
// "any", so that we can use it anywhere, and assume it has any fields or
// methods, without the compiler producing an error.

import * as React from "react";
import * as ReactDOM from "react-dom";
import { Like } from "./like";
import { Login } from "./login";
import GoogleLogin from 'react-google-login';

// The 'this' keyword does not behave in JavaScript/TypeScript like it does in
// Java.  Since there is only one NewEntryForm, we will save it to a global, so
// that we can reference it from methods of the NewEntryForm in situations where
// 'this' won't work correctly.
var newEntryForm: NewEntryForm;
/**
 * NewEntryForm encapsulates all of the code for the form for adding an entry
 */
class NewEntryForm {
    /**
     * To initialize the object, we say what method of NewEntryForm should be
     * run in response to each of the form's buttons being clicked.
     */
    constructor(){
        $("#addCancel").click(this.clearForm);
        $("#addButton").click(this.submitForm);
    }

    /**
     * Clear the form's input fields
     */
    clearForm(){
        $("#newMessage").val("");
        // reset the UI
        $("#addElement").hide();
        $("#editElement").hide();
        $("#showElements").show();
    }

    /**
     * Check if the input fields are both valid, and if so, do an AJAX call.
     */
    submitForm(){
        // get the values of the two fields, force them to be strings, and check 
        // that neither is empty
        let msg = "" + $("#newMessage").val();
        if (msg === "") {
            window.alert("Error: title or message is not valid");
            return;
        }
        // set up an AJAX post.  When the server replies, the result will go to
        // onSubmitResponse
        $.ajax({
            type: "POST",
            url: "/messages",
            dataType: "json",
            data: JSON.stringify({mMessage: msg}),
            success: newEntryForm.onSubmitResponse
        });
    }

    /**
     * onSubmitResponse runs when the AJAX call in submitForm() returns a 
     * result.
     * 
     * @param data The object returned by the server
     */
    private onSubmitResponse(data: any) {
        // If we get an "ok" message, clear the form
        if (data.mStatus === "ok") {
            newEntryForm.clearForm();
            mainList.refresh()

        }
        // Handle explicit errors with a detailed popup message
        else if (data.mStatus === "error") {
            window.alert("The server replied with an error:\n" + data.mMessage);
        }
        // Handle other errors with a less-detailed popup message
        else {
            window.alert("Unspecified error");
        }
    }
} // end class NewEntryForm

// a global for the EditEntryForm of the program.  See newEntryForm for 
// explanation
var editEntryForm: EditEntryForm;

/**
 * EditEntryForm encapsulates all of the code for the form for editing an entry
 */
class EditEntryForm {
    /**
     * To initialize the object, we say what method of EditEntryForm should be
     * run in response to each of the form's buttons being clicked.
     */
    constructor() {
        $("#editCancel").click(this.clearForm);
        $("#editButton").click(this.submitForm);
    }

    /**
     * init() is called from an AJAX GET, and should populate the form if and 
     * only if the GET did not have an error
     */
    init(data: any) {
        if (data.mStatus === "ok") {
            $("#editMessage").val(data.mData.mContent);
            $("#editId").val(data.mData.mId);
            $("#editCreated").text(data.mData.mCreated);
            // show the edit form
            $("#addElement").hide();
            $("#editElement").show();
            $("#showElements").hide();
        }
        else if (data.mStatus === "error") {
            window.alert("Error: " + data.mMessage);
        }
        else {
            window.alert("An unspecified error occurred");
        }
    }

    /**
     * Clear the form's input fields
     */
    clearForm() {
        $("#editMessage").val("");
        $("#editId").val("");
        $("#editCreated").text("");
        // reset the UI
        $("#addElement").hide();
        $("#editElement").hide();
        $("#showElements").show();
    }

    /**
     * Check if the input fields are both valid, and if so, do an AJAX call.
     */
    submitForm() {
        // get the values of the two fields, force them to be strings, and check 
        // that neither is empty
        let msg = "" + $("#editMessage").val();
        // NB: we assume that the user didn't modify the value of #editId
        let id = "" + $("#editId").val();
        if (msg === "") {
            window.alert("Error: title or message is not valid");
            return;
        }
        // set up an AJAX post.  When the server replies, the result will go to
        // onSubmitResponse
        $.ajax({
            type: "PUT",
            url: "/messages/" + id,
            dataType: "json",
            data: JSON.stringify({mMessage: msg}),
            success: editEntryForm.onSubmitResponse
        });
    }

    /**
     * onSubmitResponse runs when the AJAX call in submitForm() returns a 
     * result.
     * 
     * @param data The object returned by the server
     */
    private onSubmitResponse(data: any) {
        // If we get an "ok" message, clear the form and refresh the main 
        // listing of messages
        if (data.mStatus === "ok") {
            editEntryForm.clearForm();
            mainList.refresh();
        }
        // Handle explicit errors with a detailed popup message
        else if (data.mStatus === "error") {
            window.alert("The server replied with an error:\n" + data.mMessage);
        }
        // Handle other errors with a less-detailed popup message
        else {
            window.alert("Unspecified error");
        }
    }
} // end class EditEntryForm

var loginEntryForm: LoginEntryForm

class LoginEntryForm {

    constructor(){
        $("#loginFormButton").click(this.submitForm);
    }

    clearForm(){

    }

    submitForm(){
        let logins = document.createElement('div');
        ReactDOM.render(<Login />, logins);
        document.body.appendChild(logins);
    }
    
}

// a global for the main ElementList of the program.  See newEntryForm for 
// explanation
var mainList: ElementList;

/**
 * ElementList provides a way of seeing all of the data stored on the server.
 */
class ElementList {
    /**
     * refresh is the public method for updating messageList
     */
    refresh() {
        // Issue a GET, and then pass the result to update()
        $.ajax({
            type: "GET",
            url: "/messages",
            dataType: "json",
            success: mainList.update
        });
    }

    /**
     * update is the private method used by refresh() to update messageList
     */
    private update(data: any) {
        $("#messageList").html("<table>");
        for (let i = 0; i < data.mData.length; ++i) {
            // Create a new table row element:
            let tr = document.createElement('tr');
            // Inset all of the message data into the table row:
            tr.innerHTML = '<td>' + data.mData[i].mMessage + '</td><td>' + mainList.buttons(data.mData[i].mId) + '</td>';
            // Create a span element to bind the likes display:
            // <td>'+ data.mData[i].mLikes+ '</td>'
            let likes = document.createElement('span');
            ReactDOM.render(<Like mId={data.mData[i].mId} mLikes={data.mData[i].mLikes} />, likes);
            // Show the table row:
            tr.appendChild(likes);
            $("#messageList").append(tr);
        }
        $("#messageList").append("</table>");
        // Find all of the delete buttons, and set their behavior
        $(".delbtn").click(mainList.clickDelete);
        // Find all of the Edit buttons, and set their behavior
        $(".editbtn").click(mainList.clickEdit);
    }

    /**
     * buttons() adds a 'edit','delete', and 'like button to the HTML for each row
     */
    private buttons(id: string): string {
        return "<td><button class='editbtn' data-value='" + id + "'>Edit</button></td>" + "<td><button class='delbtn' data-value='" + id + "'>Delete</button></td>" + "<td><button class='commentbtn' data-value='" + id + "'>Comment</button></button></td>";
    }

    /**
     * clickDelete is the code we run in response to a click of a delete button
     */
    private clickDelete() {
        let id = $(this).data("value");
        $.ajax({
            type: "DELETE",
            url: "/messages/" + id,
            dataType: "json",
            success: mainList.refresh
        })
    }
    /**
     * clickEdit is the code we run in response to a click of a edit button
     */
    private clickEdit() {
        // as in clickDelete, we need the ID of the row
        let id = $(this).data("value");
        $.ajax({
            type: "GET",
            url: "/messages/" + id,
            dataType: "json",
            success: editEntryForm.init
        })
    }
} // end class ElementList



// Run some configuration code when the web page loads
$(window).on("load", function () {
    console.log('This is running');
    // Create the object that controls the "New Entry" form
    newEntryForm = new NewEntryForm();
    // Create the object for the main data list, and populate it with data from
    // the server
    mainList = new ElementList();
    mainList.refresh();

    // Create the object that controls the "Edit Entry" form
    editEntryForm = new EditEntryForm();

    loginEntryForm = new LoginEntryForm();

    // set up initial UI state
    $("#editElement").hide();
    $("#addElement").hide();
    $("#loginElement").hide();
    $("#showElements").show();
    

    // set up the "Add Message" button
    $("#showFormButton").click(function () {
        $("#addElement").show();
        $("#showElements").hide();
    });
});
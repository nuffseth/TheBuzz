// Prevent compiler errors when using jQuery.  "$" will be given a type of 
// "any", so that we can use it anywhere, and assume it has any fields or
// methods, without the compiler producing an error.

import * as React from "react";
import * as ReactDOM from "react-dom";
import { Like } from "./like";
import { Login } from "./login";
import GoogleLogin from 'react-google-login';
import { AttachmentFile } from "./attachment-file"


// The 'this' keyword does not behave in JavaScript/TypeScript like it does in
// Java.  Since there is only one NewEntryForm, we will save it to a global, so
// that we can reference it from methods of the NewEntryForm in situations where
// 'this' won't work correctly.
var newEntryForm: NewEntryForm;
/**
 * NewEntryForm encapsulates all of the code for the form for adding an entry
 */
class NewEntryForm {
    // has content from filePicker
    private attachmentFiles: AttachmentFile[]; 

    /**
     * To initialize the object, we say what method of NewEntryForm should be
     * run in response to each of the form's buttons being clicked.
     */
    constructor(){
        $("#addCancel").click(this.clearForm.bind(this));
        $("#addButton").click(this.submitForm.bind(this));
        // file picker (event listener, event listening for change, every change results in converting file to base64 string array)
        document.getElementById("filePicker").addEventListener('change', this.onFilePickerChange.bind(this));
        this.getMessages();
        this.getComments();
        this.attachmentFiles = [];
    }

    // function to get messages from backend
    private getMessages() {
        //Issue a GET, and then pass the result to update()
        // UNCOMMENTED AJAX
        $.ajax({
            type: "GET",
            url: "/messages",
            dataType: "json",
            success: this.updateMsgSelect.bind(this)
        });
        window.setTimeout(() => {this.updateMsgSelect({
            mData:[{mId:"1", mMessage:"msg1", mLikes:0, mFiles:[{mimeType:"image/", id:"1"}]}, {mId:"2", mMessage:"msg2", mLikes:0, mFiles:[{mimeType:"application/pdf", id:"2"}]}, {mId:"3", mMessage:"msg3", mLikes:0, mFiles:[]}, {mId:"4", mMessage:"msg4", mLikes:0, mFiles:[{mimeType:"application/pdf", id:"3"}, {mimeType:"image/", id:"4"}]}]
        });},1000);
    }

     // function to get comments from backend
     private getComments() {
        //Issue a GET, and then pass the result to update()
        // UNCOMMENTED AJAX
        $.ajax({
            type: "GET",
            url: "/messages/comments/",
            dataType: "json",
            success: this.updateCommentSelect.bind(this)
        })
        window.setTimeout(() => {this.updateCommentSelect({
            mData:[{mId:"1", mMessage:"msg1", commentId: "1", commentContent: "comment1"}, {mId:"2", mMessage:"msg2", commentId: "2", commentContent: "comment2"}]
        });},1000);
    }

    // fn to update the msg link select drop down
    private updateMsgSelect(msgData:any) {
        const select = document.getElementById("msgLink");
        for (let index = select.children.length-1; index > 0; index--) {
            select.removeChild(select.children[index]); 
        }

        for (let index = 0; index < msgData.mData.length; index++) {
            const msgElements = msgData.mData[index];
            let option = document.createElement("option");
            option.textContent = msgElements.mMessage;
            option.value = msgElements.mId;
            select.appendChild(option); 
        }
    }

    // fn to update the comment link select drop down
    private updateCommentSelect(msgData:any) {
        const select = document.getElementById("commentLink");
        for (let index = select.children.length-1; index > 0; index--) {
            select.removeChild(select.children[index]); 
        }

        for (let index = 0; index < msgData.mData.length; index++) {
            const msgElements = msgData.mData[index];
            let option = document.createElement("option");
            option.textContent = msgElements.commentContent;
            option.value = msgElements.commentId;
            select.appendChild(option); 
        }
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
    // ajax means interacting with backend
    submitForm(){
        // get the values of the two fields, force them to be strings, and check 
        // that neither is empty
        let msg = "" + $("#newMessage").val();
        if (msg === "") {
            window.alert("Error: title or message is not valid");
            return;
        }
        
        const msgLinkID:string|null = $("#msgLink").val() !== "" ? $("#msgLink").val() as string:null;
        const commentLinkID:string|null = $("#commentLink").val() !== "" ? $("#commentLink").val() as string:null;

        console.log(`msgLinkID = ${msgLinkID}`);
        console.log(`commentLinkID = ${commentLinkID}`);
        console.log('Attached files:', this.attachmentFiles);
        // set up an AJAX post.  When the server replies, the result will go to
        // onSubmitResponse
        $.ajax({
            type: "POST",
            url: "/messages",
            contentType: 'application/json',
            dataType: "json",
            // include msg link and comment link if they exist
            data: JSON.stringify({mMessage: msg, mFiles: this.attachmentFiles, mMsgLink: msgLinkID, mCommentLink: commentLinkID}),
            //data: JSON.stringify({mMessage: msg}),
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
    
    // function that reads files and convertes to base64 when uploaded
    onFilePickerChange() {
        this.attachmentFiles.length = 0;

        //const fileName = (document.getElementById('filePicker') as HTMLInputElement).files[0];
        const fileNames = (document.getElementById('filePicker') as HTMLInputElement).files;
        
        for (let i = 0; i < fileNames.length; i++) {
            let indivFile = fileNames.item(i);
            console.log(indivFile.name);
            const read = new FileReader();
            read.readAsBinaryString(indivFile);
            read.onloadend = () => {
                // console.log(`type of result ${typeof read.result}`);
                const base64 = btoa(read.result as string);
                console.log(`base64 is  ${base64}`);
                // console.log(`mime type is: ${indivFile.type}`);
                const attachmentFile = new AttachmentFile (indivFile.name, base64, indivFile.type);
                this.attachmentFiles.push(attachmentFile);
            }
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
    // has content from filePicker
    private attachmentFiles: AttachmentFile[]; 
    /**
     * To initialize the object, we say what method of EditEntryForm should be
     * run in response to each of the form's buttons being clicked.
     */
    constructor() {
        $("#editCancel").click(this.clearForm.bind(this));
        $("#editButton").click(this.submitForm.bind(this));
        document.getElementById("filePicker").addEventListener('change', this.onFilePickerChange.bind(this));
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
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify({mMessage: msg, mFiles: this.attachmentFiles}),
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

    // function that reads files and convertes to base64 when uploaded
    onFilePickerChange() {
        this.attachmentFiles=[];

        //const fileName = (document.getElementById('filePicker') as HTMLInputElement).files[0];
        const fileNames = (document.getElementById('editFilePicker') as HTMLInputElement).files;
        
        for (let i = 0; i < fileNames.length; i++) {
            let indivFile = fileNames.item(i);
            console.log(indivFile.name);
            const read = new FileReader();
            read.readAsBinaryString(indivFile);
            read.onloadend = () => {
                // console.log(`type of result ${typeof read.result}`);
                const base64 = btoa(read.result as string);
                // console.log(`base64 is  ${base64}`);
                // console.log(`mime type is: ${indivFile.type}`);
                const attachmentFile = new AttachmentFile (indivFile.name, base64, indivFile.type);
                this.attachmentFiles.push(attachmentFile);
            }
        }
        console.log('Attached:', this.attachmentFiles);
    }
} // end class EditEntryForm


//Login entry form
var loginEntryForm: LoginEntryForm

class LoginEntryForm {

    constructor(){ //Login buttons
        $("#loginFormButton").click(this.submitForm);
        $("#loginButton").click(this.login);
        $("#loginCancel").click(this.goBack);
    }

    submitForm(){ //When the initial button is clicked
        $("#loginElement").show();
        $("#showElements").hide();
    }

    login(){ //When the login button is clicked
        let logins = document.createElement('div');
        ReactDOM.render(<Login />, logins);
        document.body.appendChild(logins);
    }
    
    goBack(){ //When the cancel button is clicked
        $("#addElement").hide();
        $("#loginElement").hide();
        $("#showElements").show();
    }

}

var profileEntryForm: ProfileEntryForm //The profile form

class ProfileEntryForm { //The profile page
    
    constructor(){ //Profile buttons
        $("#profileButton").click(this.goToProfile);
        $("#profileCancel").click(this.goBack);
    }

    goToProfile(){ //When profile button is clicked
        $("#showElements").hide();
        $("#profileElement").show();
    }

    goBack(){ //When profile cancel button is clicked
        $("#showElements").show();
        $("#profileElement").hide();
    }
}

var commentEntryForm: CommentEntryForm //The comment form

class CommentEntryForm { //Comment forms
    
    constructor(){ //Initializes all the buttons
        $("#commentsButton").click(this.submitForm);
        $("#commentCancel").click(this.goBack);
    }

    submitForm(){ //When comment page is diplayed
        $("#commentElement").show();
        $("showElements").hide();
    }

    init(){

    }

    goBack(){ //When comment cancel button is clicked
        $("#showElements").show();
        $("#commentElement").hide();
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
        // UNCOMMENTED AJAX
        $.ajax({
            type: "GET",
            url: "/messages",
            dataType: "json",
            success: mainList.update
        });
        window.setTimeout(() => {mainList.update({
            mData:[{mId:"1", mMessage:"msg1", mLikes:0, mFiles:[{mimeType:"image/", id:"1"}]}, {mId:"2", mMessage:"msg2", mLikes:0, mFiles:[{mimeType:"application/pdf", id:"2"}]}, {mId:"3", mMessage:"msg3", mLikes:0, mFiles:[]}, {mId:"4", mMessage:"msg4", mLikes:0, mFiles:[{mimeType:"application/pdf", id:"3"}, {mimeType:"image/", id:"4"}]}]
        });},1000);
    }

    //data: {mMessage: msg, mFiles: this.attachmentFiles, mMsgLink: msgLinkID},
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

            for(let file of data.mData[i].mFiles) {
                // THIS WAS IN THE IF BLOCK BEFORE - MOVED FOR ELSE SCOPE
                let img = document.createElement('img');
                if (file.mimeType.startsWith('image/')) {
                    tr.appendChild(img);
                    // UNCOMMENTED AJAX
                    $.ajax({
                        type: "GET",
                        url: "/files/" + file.id,
                        dataType: "json",
                        success: (fileData) => {
                            img.src = 'thing in slide "data:image/png;base64,base64string';
                        }
                    });
                    window.setTimeout(() => {img.src = "data:image/png;base64,                   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"; },1000);
                }
                else {
                    let downloadButton = document.createElement('button');
                    downloadButton.textContent = "Download";
                    downloadButton.classList.add('download-button');

                    tr.appendChild(downloadButton);
                    downloadButton.addEventListener("click", (event) => {
                        // UNCOMMENTED AJAX    
                        $.ajax({
                            type: "GET",
                            url: "/files/" + file.id,
                            dataType: "json",
                            success: (fileData) => {
                                img.src = 'thing in slide "data:image/png;base64,base64string';
                                const pdfBinary = atob(fileData);
                            }
                        });
                        window.setTimeout(() => {
                  
                            const pdfBase64 = "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"; // filedata.content;
                            const pdfBinary = atob(pdfBase64);
                            this.saveFile("downloaded.pdf", "application/pdf", pdfBinary);
                        }, 1000);

                    });
                }
            }
        }
        $("#messageList").append("</table>");
        // Find all of the delete buttons, and set their behavior
        $(".delbtn").click(mainList.clickDelete);
        // Find all of the Edit buttons, and set their behavior
        $(".editbtn").click(mainList.clickEdit);
        $(".commentBtn").click(mainList.clickComment);
    }
    
    // citation: https://jsfiddle.net/efu5vhaf/, https://riptutorial.com/javascript/example/1392/creating-a-typedarray-from-a-base64-string
    private saveFile(name: string, type: string, characters: string) {
        var array = new Uint8Array(characters.length);
        for (var i = 0; i < characters.length; i++) {
            array[i] = characters.charCodeAt(i);
        }
        var a = $("<a style='display: none;'/>");
        var url = window.URL.createObjectURL(new Blob([array], { type: type }));
        a.attr("href", url);
        a.attr("download", name);
        $("body").append(a);
        a[0].click();
        window.URL.revokeObjectURL(url);
        a.remove();
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

    private clickComment(){
        let id = $(this).data("value");
        $.ajax({
            type: "GET",
            url: "/messages/" + id + "/comments/",
            dataType: "json",
            success: commentEntryForm.init
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

    profileEntryForm = new ProfileEntryForm();

    commentEntryForm = new CommentEntryForm();

    // set up initial UI state
    $("#editElement").hide();
    $("#addElement").hide();
    $("#loginElement").hide();
    $("#showElements").show();
    $("#loginElement").hide();
    $("#profileElement").hide();
    $("#commentElement").hide();

    // set up the "Add Message" button
    $("#showFormButton").click(function () {
        $("#addElement").show();
        $("#showElements").hide();
    });
});
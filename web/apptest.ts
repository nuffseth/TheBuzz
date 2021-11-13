var describe: any;
var it: any;
var expect: any;

describe("Tests of basic math functions", function() {
    it("Adding 1 should work", function() {
        var foo = 0;
        foo += 1;
        expect(foo).toEqual(1);
    });

    it("Subtracting 1 should work", function () {
        var foo = 0;
        foo -= 1;
        expect(foo).toEqual(-1);
    });

    it("UI Test: Add Button Hides Listing", function(){
        // click the button for showing the add button
        $('#showFormButton').click();
        // expect that the add form is not hidden
        expect($("#addElement").attr("style").indexOf("display: none;")).toEqual(-4);
        // expect tha tthe element listing is hidden
        expect($("#showElements").attr("style").indexOf("display: none;")).toEqual(0);
        // reset the UI, so we don't mess up the next test
        $('#addCancel').click();        
    }); 
}

describe("Testing Phase3", function() {


    // create a blob and pass into class NewEntryForm, function submitForm. The attachmentFiles should get the name, type, and base64, which is sent in the JSON
    it("Blob should return correct name, type, base64", function() {
        // create blob of plain text
        var blob = new Blob(['foo', 'bar'], { type: 'text/plain',
                                      endings: 'native' });

        //pass blob into submitForm and onFilePickerChange, which extracts name, type, and base64 in attachmentFiles

        expect(attachmentFiles).toEqual("name, text/plain, base64")
    })

    // test if all messages display as options by creating arbitrary messages in mData comparing to msgData.mData.length
    it ("message link select testing should display right number of messages", function(){
        // create arbitrary mData
        mData:[{mId:"2", mMessage:"msg2", mLikes:0, mFiles:[{mimeType:"application/pdf", id:"2"}]}, {mId:"3", mMessage:"msg3", mLikes:0, mFiles:[]}, {mId:"4", mMessage:"msg4", mLikes:0, mFiles:[{mimeType:"application/pdf", id:"3"}, {mimeType:"image/", id:"4"}]}]

        // call updateMsgSelect and see msgData.mData.length
        expect(updateMsgSelect msgData.mData.length).toEqual(mData.length);
    })

    // test if all comments display as options by creating arbitrary comments in mData comparing to msgData.mData.length
    it ("comment link select testing should display right number of comments", function(){
        // create arbitrary mData
        mData:[{mId:"1", mMessage:"msg1", commentId: "1", commentContent: "comment1"}, {mId:"2", mMessage:"msg2", commentId: "2", commentContent: "comment2"}]
        // call updateMsgSelect and see msgData.mData.length
        expect(updateCommentSelect commentData.mData.length).toEqual(mData.length);
    })

    // test downloading files by seeing if converting pdfBinary back to base64 is the same as original base64
    it ("Test downloading files by converting the binary back to base64 and seeing if same as original", function(){
        // get base64 of file and convert to binary
        // call saveFile function
        // read base64 of saved file
        // compare to original base64

    })
})
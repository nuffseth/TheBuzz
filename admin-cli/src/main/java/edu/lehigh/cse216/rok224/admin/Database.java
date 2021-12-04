package edu.lehigh.cse216.rok224.admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.*;
import java.io.ByteArrayOutputStream;

// import javax.swing.plaf.metal.MetalComboBoxButton;

import java.io.IOException;
import java.io.OutputStream;
// import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.api.client.http.FileContent;
// import com.google.api.client.auth.oauth2.Credential;
// import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
// import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
// import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// import com.google.api.client.http.javanet.NetHttpTransport;
// import com.google.api.client.json.JsonFactory;
// import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
// import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class Database {
    /**
     * The connection to the database.  When there is no connection, it should
     * be null.  Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    /**
     * The connection to the Google Drive service. 
     */
    private Drive mService;

    /* prepared statements from phase 0 - do we still need these?
    // private PreparedStatement mSelectAll;
    // private PreparedStatement mSelectOne;
    // private PreparedStatement mDeleteOne;
    // private PreparedStatement mInsertOne; 
    // private PreparedStatement mUpdateOne;
    */

    // prepared statements from phase 1 - do we still need these?
    private PreparedStatement psCreateTable;
    private PreparedStatement psDropTable;
    private PreparedStatement psIncrementLikes;
    private PreparedStatement psDecrementLikes;

    // TABLES
    private PreparedStatement psUserTable;
    private PreparedStatement psMessageTable;
    private PreparedStatement psLikesTable;
    private PreparedStatement psCommentTable;
    private PreparedStatement psMsgFileTable;
    private PreparedStatement psCmtFileTable;

    // USER PREPARED STATEMENTS 
    private PreparedStatement psInsertUser;
    private PreparedStatement psSelectUser;
    private PreparedStatement psSelectAllUsers;
    private PreparedStatement psUpdateUser;
    private PreparedStatement psSelectBlockedUser;

    // MESSAGE PREPARED STATEMENTS
    private PreparedStatement psInsertMessage;
    private PreparedStatement psSelectMessage;
    private PreparedStatement psGetMsgLikes;
    private PreparedStatement psGetMsgComments;
    private PreparedStatement psGetMsgFileData;
    private PreparedStatement psSelectAllMessages;
    private PreparedStatement psSelectAllMessagesWithFlags;
    private PreparedStatement psUpdateMessage;
    private PreparedStatement psDeleteMessage;
    private PreparedStatement psDeleteMessageFlag;

    // LIKE PREPARED STATEMENTS
    private PreparedStatement psInsertLike;
    private PreparedStatement psSelectLike;
    private PreparedStatement psUpdateLike;
    private PreparedStatement psSelectAllLikes; 

    // COMMENT PREPARED STATEMENTS
    private PreparedStatement psInsertComment;
    private PreparedStatement psSelectComment;
    private PreparedStatement psSelectAllComments;
    private PreparedStatement psSelectAllCommentsWithFlags;
    private PreparedStatement psGetCmtFileData;
    private PreparedStatement psUpdateComment;
    private PreparedStatement psDeleteComment;
    private PreparedStatement psDeleteCommentFlag;

    // FILE PREPARED STATEMENTS
    private PreparedStatement psInsertMessageFile;
    private PreparedStatement psSelectMessageFile;
    private PreparedStatement psSelectAllMsgFiles;
    private PreparedStatement psDeleteMsgFile;
    private PreparedStatement psGetMsgFileID;

    private PreparedStatement psInsertCommentFile;
    private PreparedStatement psSelectCommentFile;
    private PreparedStatement psSelectAllCmtFiles;
    private PreparedStatement psDeleteCmtFile;
    private PreparedStatement psGetCmtFileID;

    // MSG FLAG PREPARD STATEMENTS
    private PreparedStatement psInsertMsgFlag;
    private PreparedStatement psSelectMessageFlag;

    // CMT FLAG PREPARED STATEMENTS
    private PreparedStatement psInsertCmtFlag;
    private PreparedStatement psSelectCommentFlag;

    // CHECK MSGID PREPARED STATEMENT
    private PreparedStatement psCheckMsgIDFlag;

    // BLOCK USER PREPARED STATEMENT
    private PreparedStatement psBlockUser;
    private PreparedStatement psSelectAllBlockedUsers;
    private PreparedStatement psDeleteBlockedUser;

    /** DEPRECATED PREPARED STATEMENTS
    private PreparedStatement mCommentTableUpdateContent;
    private PreparedStatement mCommentTableUpdateUserID;
    private PreparedStatement mCommentTableUpdateMsgID;
    //-----------------------=
    private PreparedStatement mLikesTableUpdateStatus;
    private PreparedStatement mLikesTableUpdateUserID;
    private PreparedStatement mLikesTableUpdateMsgID;
    //-----------------------=
    private PreparedStatement mMessageTableUpdateContent;
    private PreparedStatement mMessageTableUpdateUserID;
    private PreparedStatement mInsertOneUser;
    private PreparedStatement mInsertOneComment;
    private PreparedStatement mInsertOneLike;
    private PreparedStatement mInsertOneMessage;
    private PreparedStatement mSelectOneBio;
    */ 

    int createAllTables() {
        try {
            psUserTable.executeQuery();
            psMessageTable.executeQuery();
            psLikesTable.executeQuery();
            psCommentTable.executeQuery();
            psMsgFileTable.executeQuery();
            psCmtFileTable.executeQuery();
        } catch  (SQLException e) {
            return -1;
        }
        return 1;
    }
    /**
     * All objects for the User table
     */
    public class User {
        String mUserID;
        String mBio;

        public User(String userID, String bio) {
            mUserID = userID;
            mBio = bio;
        }
    }

    /**
     * Add a new user to the database.
     * @param user  String of the username to add to the database (primary key of the database)
     * @param bio   String of the user bio to add to the database (optional)
     * @return
     */
    int insertUser (String user, String bio) {
        int ret = 0;
        if (testString(user) == false){ // generic validity check on both params
            return -1;
        }

        User existingUser = selectUser(user);
        // check if user already exists before trying to add to the database
        if (existingUser != null) {
            return 0;
        } 
        else {
            try {
                System.out.println("trying to add user to database...");
                psInsertUser.setString(1, user);  // first param is being set as user
                psInsertUser.setString(2, bio);   // second param is being set as bio
                ret += psInsertUser.executeUpdate();
            } catch (SQLException e) {
                // // if error bc user already exists, return 0
                // if (e.toString().contains("Key (userid)=(" + user + ") already exists.")) {
                //     ret = 0;
                // }
                // else {
                //     e.printStackTrace();
                // }
                e.printStackTrace();
                return -1;
            }
        }
        return ret;
    }

    /**
     * Get all data for a specific user in the database from their username.
     * @param user  String of the username to get data about    
     * @return      A Database.User objcet, which includes fields mUserID and mBio
     */
    User selectUser(String user) {
        User res = null;
        try {
            psSelectUser.setString(1, user);
            ResultSet rs = psSelectUser.executeQuery();
            if(rs.next()){
                res = new User(rs.getString("userID"), rs.getString("bio"));
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return res;
    }

    User selectBlockedUser(String blockedUser) {
        User res = null;
        try {
            psSelectBlockedUser.setString(1, blockedUser);
            ResultSet rs = psSelectUser.executeQuery();
            if(rs.next()){
                res = new User(rs.getString("user_blocked"), rs.getString("user_blocker"));
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return res;
    }

    /**
     * Select all the users in the database. (should only be accessible from admin CLI)
     * @return      An ArrayList of Database.User objects, where each Database.User includes mUserID and mBio
     */
    ArrayList<User> selectAllUsers() {
        ArrayList<User> res = new ArrayList<User>();
        try {
            ResultSet rs = psSelectAllUsers.executeQuery();
            while (rs.next()){
                res.add(new User( rs.getString("userID"), rs.getString("bio")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update the user profile information of a specific user.
     * @param user  String of the username of the user to update (primary key in the database)
     * @param bio   String of the bio of the user account to update.
     * @return
     */
    int updateUser (String user, String bio) {
        int ret = 0;
        if (testString(bio) == false || testString(user) == false) {
            return -1;
        }
        try {
            psUpdateUser.setString(2, user);
            psUpdateUser.setString(1, bio);
            ret += psUpdateUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    /**
     * All objects and functions for the Comment object.
     * Includes mUserID, mCommentID, mMsgID, mContent, mFileData, mMsgLink, and mCmtLink.
     */
    public class Comment {
        String mUserID;
        int mCommentID;
        int mMsgID;
        String mContent;
        ArrayList<MyFile> mFileData;
        int mMsgLink;
        int mCmtLink;
        int mNumFlags;

        public Comment(int commentID, String userID, int msgID, String content, int msgLink, int cmtLink, ArrayList<MyFile> fileData, int numFlags) {
            mUserID = userID;
            mCommentID = commentID;
            mMsgID = msgID;
            mContent = content;
            mFileData = fileData;
            mMsgLink = msgLink;
            mCmtLink = cmtLink;
            mNumFlags = numFlags;
        }
    }

    /**
     * Adds a comment to the comment database.
     * @param msgID     The message id of the message that this comment refers to. (Must exist in messages table).
     * @param userID    The username of the user who is posting the comment.
     * @param content   String of the content of the comment.
     * @param msgLink   Optional message id to link this comment to another message in the database.
     * @param cmtLink   Option comment id to link this comment to another comment in the database.
     * @return          New comment ID of the created comment on success, -1 on failure.
     */
    int insertComment (int msgID, String userID, String content, int msgLink, int cmtLink) {
        int ret = 0;
        if ( !testString(content) || !testString(userID) ) {   // generic validity check 
            return -1;
        } 

        try {
            psInsertComment.setInt(2, msgID);
            psInsertComment.setString(3, userID);
            psInsertComment.setString(4, content);
            psInsertComment.setInt(5, msgLink);
            psInsertComment.setInt(6, cmtLink);

            ret += psInsertComment.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // get file matadata for a specific comment
    /**
     * Select all the files associated with a particular comment
     * @param cmtID     ID of the comment from which to select files.
     * @return          Arraylist of Database.MyFile objects, where each Database.MyFile includes:
     *                  mFileID, mMsgCmtID, mMime, and mFilename.
     */
    ArrayList<MyFile> getCmtFiles(int cmtID) {
        ArrayList<MyFile> res = new ArrayList<MyFile>();
        try {
            psGetCmtFileData.setInt(1, cmtID);
            ResultSet rs = psGetCmtFileData.executeQuery();
            while (rs.next()){
                res.add(new MyFile( rs.getString("fileID"), rs.getInt("cmtID"), rs.getString("mime"), rs.getString("filename")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    // select one comment 
    /**
     * Select a specific comment from the database.
     * @param cmtID     The comment ID of the comment to select.
     * @return          A Database.Comment object, which includes:
     *                  mUserID, mCommentID, mMsgID, mContent, mFileData, mMsgLink, and mCmtLink.
     */
    Comment selectComment(int cmtID) {
        Comment res = null;
        try {
            psSelectComment.setInt(1, cmtID);
            ResultSet rs = psSelectComment.executeQuery();
            if(rs.next()){
                ArrayList<MyFile> fileData = getCmtFiles(cmtID);
                res = new Comment(rs.getInt("cmtID"), rs.getString("userID"), rs.getInt("msgID"), 
                    rs.getString("content"), rs.getInt("msgLink"), rs.getInt("cmtLink"), fileData, rs.getInt("flag_count"));
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return res;
    }

    // select all comments in the database (only used by admin CLI)
    ArrayList<Comment> selectAllComments(){
        ArrayList<Comment> res = new ArrayList<Comment>();
        try {
            ResultSet rs = psSelectAllComments.executeQuery();
            while (rs.next()){
                ArrayList<MyFile> fileData = getCmtFiles(rs.getInt("cmtID"));
                res.add(new Comment(rs.getInt("cmtID"), rs.getString("userID"), rs.getInt("msgID"), rs.getString("content"), 
                        rs.getInt("msgLink"), rs.getInt("cmtLink"), fileData, rs.getInt("flag_count")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<Comment> selectAllCommentsWithFlags(){
        ArrayList<Comment> res = new ArrayList<Comment>();
        try {
            ResultSet rs = psSelectAllCommentsWithFlags.executeQuery();
            while (rs.next()){
                // for each message, get all comments and like count
                int this_cmt = rs.getInt("cmtID");
                ArrayList<MyFile> fileData = getCmtFiles(this_cmt);
                Comment thisComment = new Comment(rs.getInt("cmtID"), rs.getString("userID"), rs.getInt("msgID"), rs.getString("content"), rs.getInt("msgLink"), rs.getInt("cmtLink"), fileData, rs.getInt("flag_count"));
                res.add(thisComment);   
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    // update a comment
    int updateComment (int cmtID, String content) {
        int ret = 0;
        if (testString(content) == false) {
            return -1;
        }
        try {
            psUpdateComment.setInt(2, cmtID);
            psUpdateComment.setString(1, content);
            ret += psUpdateComment.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // delete a comment
    int deleteComment(int cmtID){
        int res = -1;
        try {
            psDeleteComment.setInt(1, cmtID);
            res = psDeleteComment.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
        return res;
    }

    /**
     * Object type for a single row in the Likes table
     */
    public class Like {
        String mUserID;
        int mMsgID;
        int mStatus;

        public Like(int msgID, String userID, int status) {
            mMsgID = msgID;
            mUserID = userID;
            mStatus = status;
        }
    }

    // add a new like to the table
    int insertLike (int status, String userID, int msgID) {
        int ret = 0;
        if (testString(userID) == false) {  // general validity check
            return -1;
        }
        try {
            psInsertLike.setInt(1, msgID);
            psInsertLike.setString(2, userID);
            psInsertLike.setInt(3, status);
            ret += psInsertLike.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // select a specific like
    Like selectLike(int msgID, String userID) {
        Like res = null; 
        try {
            psSelectLike.setInt(1, msgID);
            psSelectLike.setString(2, userID);
            ResultSet rs = psSelectLike.executeQuery();
            if(rs.next()){
                res = new Like(rs.getInt("msgID"), rs.getString("userID"), rs.getInt("status"));
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
        return res;
    }  

    // update a like in the table
    int updateLike(int msgID, String userID, int status) {
        int ret = 0; 
        try {
            psUpdateLike.setInt(1, status);
            psUpdateLike.setInt(2, msgID);
            psUpdateLike.setString(3, userID);
            ret += psUpdateLike.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    /**
     * Object for a single row in the Messages table
     */
    public class Message {
        String mUserID;
        int mMsgID;
        String mContent;
        int mNumLikes;
        int mMsgLink;
        int mCmtLink;
        ArrayList<Comment> mComments;
        ArrayList<MyFile> mFileData;
        int mNumFlags;
        String mAd;
        boolean mIsEven;

        // TODO : add a class variable for the number of flags
        // DONE.

        public Message(int msgID, String userID, String content, int numLikes, int msgLink, int cmtLink, ArrayList<Comment> comments, ArrayList<MyFile> fileData, int numFlags, String ad, boolean is_even) {
            mMsgID = msgID;
            mUserID = userID;
            mContent = content;
            mNumLikes = numLikes;
            mMsgLink = msgLink;
            mCmtLink = cmtLink;
            mComments = comments; 
            mFileData = fileData;
            mNumFlags = numFlags;
            mAd = ad;
            mIsEven = is_even;
        }

        public Message(int msgID, String userID, String content, int numLikes, int msgLink, int cmtLink, int numFlags, String ad, boolean is_even) {
            mMsgID = msgID;
            mUserID = userID;
            mContent = content;
            mNumLikes = numLikes;
            mMsgLink = msgLink;
            mCmtLink = cmtLink;
            mNumFlags = numFlags;
            mAd = ad;
            mIsEven = is_even;
        }
    }

    // TODO : add a constructor trhat does not require ArrayList<Comments> or ArrayList<MyFile> 
    // DONE.

    // add a new message
    int insertMessage (String userID, String content, int msgLink, int cmtLink) {
        int ret = 0; 
        if (testString(content) == false || testString(userID) == false) { // generic validity check
            return -1;
        }
        
        try {
            psInsertMessage.setString(1, userID);
            psInsertMessage.setString(2, content);
            psInsertMessage.setInt(3, msgLink);
            psInsertMessage.setInt(4, cmtLink);
            ret += psInsertMessage.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // select all comments for a specific message
    ArrayList<Comment> getComments(int msgID){
        ArrayList<Comment> res = new ArrayList<Comment>();
        try {
            psGetMsgComments.setInt(1, msgID);
            ResultSet rs = psGetMsgComments.executeQuery();
            while (rs.next()){
                ArrayList<MyFile> fileData = getCmtFiles(rs.getInt("cmtID"));
                res.add(new Comment(rs.getInt("cmtID"), rs.getString("userID"), rs.getInt("msgID"), rs.getString("content"), rs.getInt("msgLink"), rs.getInt("cmtLink"), fileData, rs.getInt("flag_count")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    // count all the likes on a specific message (subtract any dislikes)
    int countLikes(int msgID) {
        int like_count = 0;
        try {
            psGetMsgLikes.setInt(1, msgID);
            ResultSet rs = psGetMsgLikes.executeQuery();
            while (rs.next()){
                int status = rs.getInt("status");
                like_count += status;
            }
            rs.close();
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
        return like_count;
    }

    // get file matadata for a specific comment
    ArrayList<MyFile> getMsgFiles(int msgID) {
        ArrayList<MyFile> res = new ArrayList<MyFile>();
        try {
            psGetMsgFileData.setInt(1, msgID);
            ResultSet rs = psGetMsgFileData.executeQuery();
            while (rs.next()){
                res.add(new MyFile( rs.getString("fileID"), rs.getInt("msgID"), rs.getString("mime"), rs.getString("filename")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    // select a specific message
    Message selectMessage(int msgID) {
        Message res = null;
        try {
            psSelectMessage.setInt(1, msgID);
            ResultSet rs = psSelectMessage.executeQuery();
            if(rs.next()){
                // populate ArrayList of all of this message's comments
                ArrayList<Comment> allComments = getComments(msgID);
                int likes = countLikes(msgID);
                ArrayList<MyFile> fileData = getMsgFiles(msgID);
                res = new Message(rs.getInt("msgID"), rs.getString("userID"), rs.getString("content"), 
                        likes, rs.getInt("msgLink"), rs.getInt("cmtLink"), allComments, fileData, rs.getInt("flag_count"), rs.getString("ad"), rs.getBoolean("is_even"));
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return res;
    }

    // select all messages
    ArrayList<Message> selectAllMessages(){
        ArrayList<Message> res = new ArrayList<Message>();
        try {
            ResultSet rs = psSelectAllMessages.executeQuery();
            while (rs.next()){
                // for each message, get all comments and like count
                int this_msg = rs.getInt("msgID");
                ArrayList<Comment> comments = getComments(this_msg);
                int likes = countLikes(this_msg);
                ArrayList<MyFile> fileData = getMsgFiles(this_msg);

                // create Message object and add to our ArrayList
                Message thisMessage = new Message(rs.getInt("msgID"), rs.getString("userID"), rs.getString("content"), likes, rs.getInt("msgLink"), rs.getInt("cmtLink"), comments, fileData, rs.getInt("flag_count"), rs.getString("ad"), rs.getBoolean("is_even"));
                res.add(thisMessage);
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<Message> selectAllMessagesWithFlags(){
        ArrayList<Message> res = new ArrayList<Message>();
        try {
            ResultSet rs = psSelectAllMessagesWithFlags.executeQuery();
            while (rs.next()){
                // for each message, get all comments and like count
                int this_msg = rs.getInt("msgID");
                int likes = countLikes(this_msg);
                Message thisMessage = new Message(rs.getInt("msgID"), rs.getString("userID"), rs.getString("content"), likes, rs.getInt("msgLink"), rs.getInt("cmtLink"), rs.getInt("flag_count"), rs.getString("ad"), rs.getBoolean("is_even"));
                res.add(thisMessage);   
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    // TODO create a ArrayList<Message> selectAllMessagesWithFlags()  function
    // like selectAllMessages(), but only select where flag_count > 0 and 
    // does not call countLikes() or getMessageFiles().  Hint use the new constructor
    // DONE.


    // update a message
    int updateMessage (int msgID, String content) {
        int ret = 0;
        if (testString(content) == false) {
            return -1;
        }
        try {
            psUpdateMessage.setInt(2, msgID);
            psUpdateMessage.setString(1, content);
            ret += psUpdateMessage.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // delete a message
    int deleteMessage(int msgID){
        int res = -1;
        try {
            // TODO update this function to call new stored procedure
            // that removes messages from database that does not leave 
            // the database with referential integrity issues
            // DONE? -- didnt change anything here, changed the prepared statement

            psDeleteMessage.setInt(1, msgID);
            res = psDeleteMessage.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
        return res;
    }

    // object to store file metadata
    // NOTE: does not include the file content, that must be accessed through the Drive API using the fileID
    public class MyFile{
        String mFileID;
        int mMsgCmtID;
        String mMime;
        String mFilename;

        public MyFile(String fileID, int msg_cmtID, String mime, String filename) {
            mFileID = fileID;
            mMsgCmtID = msg_cmtID;
            mMime = mime;
            mFilename = filename;
        }
    }

    // function to get the drive quota
    int driveQuota() throws IOException {
        Object quota = mService.about().get().setFields("storageQuota").execute();

        System.out.println(quota);
        return -1;
    }

    List<File> getLRU(int num) {
        List<File> files = null;
        FileList result;
        try {
            result = mService.files().list()
                .setFields("nextPageToken, files(id, name, modifiedTime)")
                .execute();
            files = result.getFiles();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    // function to test drive connection
    List<File> getAllDriveFiles() {
        FileList result;
        List<File> files = null;
        try {
            result = mService.files().list()
                .setFields("nextPageToken, files(id, name, modifiedTime)")
                .execute();
            files = result.getFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    String uploadFile(java.io.File content, String filename, String mime) {
        // create metadata for the file
        File fileMetadata = new File();
        fileMetadata.setName(filename);
        fileMetadata.setMimeType(mime);
        // create file content
        FileContent mediaContent = new FileContent(mime, content);

        // upload the file to the drive
        String fileID = null;
        try {
            File file = mService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
            fileID = file.getId();
            System.out.println("Entire file object:");
            System.out.println(file);
        } catch (IOException e) {
            System.out.println("File upload error.");
            e.printStackTrace();
        }
        return fileID;
    }
    // TODO: work with backend to create a method to download a file from the drive
    byte[] downloadFile(String fileID) {
        if (!validFileID(fileID)) { // make sure fileID is valid
            return null;
        }
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            mService.files().get(fileID).executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // convert output stream to byte array
        ByteArrayOutputStream bytes_data= (ByteArrayOutputStream) outputStream;
        byte[] data = bytes_data.toByteArray();
        return data;

    }

    // method to delete a file from the database (helper function for deleteMsgFile and deleteCmtFile)
    int deleteFile(String fileID) {
        if (!validFileID(fileID)) { // make sure fileID is valid
            return -1;
        }
        int ret = 0;
        try {
            mService.files().delete(fileID).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
        return ret;
    }

    int insertMsgFile(int msgID, java.io.File file) {
        String fileID = null;
        int ret = 0; 

        // get filename and mime
        String filename = file.getName();
        Path path = file.toPath();
        String mime = "";
        try {
            mime = Files.probeContentType(path);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (testString(mime) == false || testString(filename) == false) { // generic validity check
            return -1;
        }
        
        // upload file and get fileID
        fileID = uploadFile(file, filename, mime); //upload file to the drive
        if (fileID == null) {
            return -1;
        }
        try { //add file metadata to the database
            psInsertMessageFile.setString(1, fileID);
            psInsertMessageFile.setInt(2, msgID);
            psInsertMessageFile.setString(3, mime);
            psInsertMessageFile.setString(4, filename);
            ret += psInsertMessageFile.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // returns an object of all msg file metadata 
    // in order to obtain file contents, need to use downloadFile (connection to Drive)
    MyFile selectMsgFile(String fileID) {
        if (!validFileID(fileID)) { // make sure fileID is valid
            return null;
        }
        MyFile res = null;
        try {
            psSelectMessageFile.setString(1, fileID);
            ResultSet rs = psSelectMessageFile.executeQuery();
            if(rs.next()){
                res = new MyFile(rs.getString("fileID"), rs.getInt("msgID"), rs.getString("mime"), rs.getString("filename"));
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return res;
    }

    ArrayList<String> getMsgFileID(String filename) {
        ArrayList<String> fileIDs = new ArrayList<String>();
        try {
            psGetMsgFileID.setString(1, filename);
            ResultSet rs = psGetMsgFileID.executeQuery();
            while (rs.next()){
                fileIDs.add(rs.getString("fileID"));
            }
            rs.close();
            return fileIDs;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<MyFile> selectAllMsgFiles() {
        ArrayList<MyFile> res = new ArrayList<MyFile>();
        try {
            ResultSet rs = psSelectAllMsgFiles.executeQuery();
            while (rs.next()){
                res.add(new MyFile(rs.getString("fileID"), rs.getInt("msgID"), rs.getString("mime"), rs.getString("filename")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    int deleteMsgFile(String fileID) {
        if (!validFileID(fileID)) { // make sure fileID is valid
            return -1;
        }
        // delete the file from the Drive
        int res = deleteFile(fileID);
        if (res == -1) {
            System.out.println("Unable to delete file from the Drive.");
            return -1;
        }

        // delete the file from the msgFile database
        try {
            psDeleteMsgFile.setString(1, fileID);
            res = psDeleteMsgFile.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
        return res;
    }

    int insertCmtFile(int cmtID, java.io.File file) {
        String fileID = null;
        int ret = 0; 

        // get filename and mime
        String filename = file.getName();
        Path path = file.toPath();
        String mime = "";
        try {
            mime = Files.probeContentType(path);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println("Mime: " + mime);
        System.out.println("Filename: " + filename);
        if (mime == null) {
            System.out.println("Error, content type could not be determined.");
            return -1;
        }
        if (testString(mime) == false || testString(filename) == false) { // generic validity check
            return -1;
        }

        try {
            // upload file and get fileID
            fileID = uploadFile(file, filename, mime); //upload file to the drive
            if (fileID == null) {
                return -1;
            }
            psInsertCommentFile.setString(1, fileID);
            psInsertCommentFile.setInt(2, cmtID);
            psInsertCommentFile.setString(3, mime);
            psInsertCommentFile.setString(4, filename);
            ret += psInsertCommentFile.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // returns an object of all msg file metadata 
    // in order to obtain file contents, need to use downloadFile (connection to Drive)
    MyFile selectCmtFile(String fileID) {
        if (!validFileID(fileID)) { // make sure fileID is valid
            return null;
        }
        MyFile res = null;
        try {
            psSelectCommentFile.setString(1, fileID);
            ResultSet rs = psSelectCommentFile.executeQuery();
            if(rs.next()){
                res = new MyFile(rs.getString("fileID"), rs.getInt("cmtID"), rs.getString("mime"), rs.getString("filename"));
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return res;
    }

    ArrayList<String> getCmtFileID(String filename) {
        ArrayList<String> fileIDs = new ArrayList<String>();
        try {
            psGetCmtFileID.setString(1, filename);
            ResultSet rs = psGetCmtFileID.executeQuery();
            while (rs.next()){
                fileIDs.add(rs.getString("fileID"));
            }
            rs.close();
            return fileIDs;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<MyFile> selectAllCmtFiles() {
        ArrayList<MyFile> res = new ArrayList<MyFile>();
        try {
            ResultSet rs = psSelectAllCmtFiles.executeQuery();
            while (rs.next()){
                res.add(new MyFile(rs.getString("fileID"), rs.getInt("cmtID"), rs.getString("mime"), rs.getString("filename")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    int deleteCmtFile(String fileID) {
        if (!validFileID(fileID)) { // make sure fileID is valid
            return -1;
        }
        // delete the file from the Drive
        int res = deleteFile(fileID);
        if (res == -1) {
            System.out.println("Unable to delete file from the Drive.");
            return -1;
        }

        // delete the file from the msgFile database
        try {
            psDeleteCmtFile.setString(1, fileID);
            res = psDeleteCmtFile.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
        return res;
    }

    // class for flagged messages
    public class MessageFlag {
        String mUserID;
        int mMsgID;

        public MessageFlag(int msgID, String userID) {
            mMsgID = msgID;
            mUserID = userID;
        }
    }

    // add a new flag to a message 
    int insertMessageFlag (String userID, int msgID) {
        int ret = 0;
        if (testString(userID) == false) {  // general validity check
            return -1;
        }
        try {
            psInsertMsgFlag.setInt(1, msgID);
            psInsertMsgFlag.setString(2, userID);
            ret += psInsertMsgFlag.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

     // select a specific flagged message
     MessageFlag selectMessageFlag(String userID, int msgID) {
        MessageFlag res = null; 
        try {
            psSelectMessageFlag.setInt(1, msgID);
            psSelectMessageFlag.setString(2, userID);
            ResultSet rs = psSelectMessageFlag.executeQuery();
            if(rs.next()){
                res = new MessageFlag(rs.getInt("msgID"), rs.getString("userID"));
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
        return res;
    } 




    // class for flagged comments
    public class CommentFlag {
        String mUserID;
        int mCmtID;

        public CommentFlag(int cmtID, String userID) {
            mCmtID = cmtID;
            mUserID = userID;
        }
    }

    // add a new flag to a comment 
    int insertCommentFlag (String userID, int cmtID) {
        int ret = 0;
        if (testString(userID) == false) {  // general validity check
            return -1;
        }
        try {
            psInsertCmtFlag.setInt(1, cmtID);
            psInsertCmtFlag.setString(2, userID);
            ret += psInsertCmtFlag.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // select a specific flagged comment
    CommentFlag selectCommentFlag(String userID, int cmtID) {
        CommentFlag res = null; 
        try {
            psSelectCommentFlag.setInt(1, cmtID);
            psSelectCommentFlag.setString(2, userID);
            ResultSet rs = psSelectCommentFlag.executeQuery();
            if(rs.next()){
                res = new CommentFlag(rs.getInt("cmtID"), rs.getString("userID"));
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
        return res;
    } 

    // block a user
    int addBlockedUser(String user_blocked, String user_blocker){
        int res = -1;
        try {
            psBlockUser.setString(1, user_blocked);
            psBlockUser.setString(2, user_blocker);
            res = psBlockUser.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
        return res;
    }
    
    // select all blocked users
    ArrayList<User> selectAllBlockedUsers(){
        ArrayList<User> res = new ArrayList<User>();
        try {
            ResultSet rs = psSelectAllBlockedUsers.executeQuery();
            while (rs.next()){
                res.add(new User( rs.getString("user_blocked"), rs.getString("user_blocker")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * The Database constructor is private: we only create Database objects 
     * through the getDatabase() method.
     */
    private Database(Drive service) {
        mService = service;
    }

    /**
     * Get a fully-configured connection to the database
     * 
     * @param ip   The IP address of the database server
     * @param port The port on the database server to which connection requests
     *             should be sent
     * @param user The user ID to use when connecting
     * @param pass The password to use when connecting
     * 
     * @return A Database object, or null if we cannot connect properly
     */
    static Database getDatabase(String url, Drive service) {
        // Create an un-configured Database object
        Database db = new Database(service);

        // Give the Database object a connection, fail if we cannot get one
        try {
            Class.forName("org.postgresql.Driver");
            URI dbUri = new URI(url);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Unable to find postgresql driver");
            return null;
        } catch (URISyntaxException s) {
            System.out.println("URI Syntax Error");
            return null;
        }

        // If any of our prepared statements fail, the whole getDatabase() call should fail
        try {
            // Note: multiple executions will cause an exception
            // TODO: I don't think we need this??
            db.psCreateTable = db.mConnection.prepareStatement("CREATE TABLE tblData (id SERIAL PRIMARY KEY, message VARCHAR(500) NOT NULL, likes INT)"); //Creates the table
            db.psDropTable = db.mConnection.prepareStatement("DROP TABLE tblData"); //Deletes the table

            // Create all the tables we need 
            db.psUserTable = db.mConnection.prepareStatement(
                "CREATE TABLE users (userID VARCHAR(500) NOT NULL PRIMARY KEY, bio TEXT)");

            db.psMessageTable = db.mConnection.prepareStatement(
                "CREATE TABLE messages (msgID SERIAL PRIMARY KEY, userID VARCHAR(500), content TEXT NOT NULL, CONSTRAINT user_key FOREIGN KEY(userID) REFERENCES users(userID))");

            db.psCommentTable = db.mConnection.prepareStatement(
                "CREATE TABLE comments (cmtID SERIAL PRIMARY KEY, msgID INT, userID VARCHAR(500), content TEXT NOT NULL, " + 
                "CONSTRAINT msg_key FOREIGN KEY(msgID) REFERENCES messages(msgID), CONSTRAINT user_key FOREIGN KEY(userID) REFERENCES users(userID))"); 

            db.psLikesTable = db.mConnection.prepareStatement(
                "CREATE TABLE likes (msgID INT, userID VARCHAR(500), status INT, " + 
                "CONSTRAINT msg_key FOREIGN KEY(msgID) REFERENCES messages(msgID), CONSTRAINT user_key FOREIGN KEY(userID) REFERENCES users(userID), " + 
                "CONSTRAINT like_key PRIMARY KEY (userID, msgID))");  // likes table uses the userID/msgID combo as the primary key

            db.psMsgFileTable = db.mConnection.prepareStatement(
                "CREATE TABLE msgfiles (fileID TEXT PRIMARY KEY, msgID INT, mime TEXT, filename VARCHAR(500), " +
                "CONSTRAINT msg_key FOREIGN KEY(msgID) REFERENCES messages(msgID) )"
            );

            db.psCmtFileTable = db.mConnection.prepareStatement(
                "CREATE TABLE cmtfiles (fileID TEXT PRIMARY KEY, cmtID INT, mime TEXT, filename VARCHAR(500), " +
                "CONSTRAINT cmt_key FOREIGN KEY(cmtID) REFERENCES comments(cmtID) )"
            );

            // USER prepared statements
            db.psInsertUser = db.mConnection.prepareStatement("INSERT INTO users VALUES (?, ?)");  
            db.psSelectUser = db.mConnection.prepareStatement("SELECT * from users where userID = ?");
            db.psSelectAllUsers = db.mConnection.prepareStatement("SELECT * from users");
            db.psUpdateUser = db.mConnection.prepareStatement("UPDATE users SET bio = ? WHERE userID = ?");
            db.psSelectBlockedUser = db.mConnection.prepareStatement("SELECT * FROM blocked WHERE user_blocked = ?");

            // MESSAGE prepared statements
            db.psInsertMessage = db.mConnection.prepareStatement("INSERT INTO messages VALUES (default, ?, ?, ?, ?)");
            db.psGetMsgLikes = db.mConnection.prepareStatement("SELECT * from likes WHERE msgID = ?");
            db.psGetMsgComments = db.mConnection.prepareStatement("SELECT * from comments WHERE msgID = ?");
            db.psGetMsgFileData = db.mConnection.prepareStatement("SELECT * from msgfiles WHERE msgID = ?");
            db.psSelectMessage = db.mConnection.prepareStatement("SELECT * from messages WHERE msgID = ?");
            db.psSelectAllMessages = db.mConnection.prepareStatement("SELECT * FROM messages");
            db.psSelectAllMessagesWithFlags = db.mConnection.prepareStatement("SELECT * FROM messages WHERE flag_count > 0 ORDER BY flag_count DESC");
            db.psUpdateMessage = db.mConnection.prepareStatement("UPDATE messages SET content = ? WHERE msgID = ?");
            // db.psDeleteMessage = db.mConnection.prepareStatement("DELETE FROM messages WHERE msgID = ?");
            db.psDeleteMessage = db.mConnection.prepareStatement("call delete_msg(?)");
            // TODO fix above to call stored procedure that deletes with ref integrity
            // DONE.
  
            // LIKE prepared statements
            db.psInsertLike = db.mConnection.prepareStatement("INSERT INTO likes VALUES (?, ?, ?)");
            db.psUpdateLike = db.mConnection.prepareStatement("UPDATE likes SET status = ? WHERE msgID = ? AND userID = ?");
            db.psSelectLike = db.mConnection.prepareStatement("SELECT * from likes where msgID = ? AND userID = ?");
            db.psSelectAllLikes = db.mConnection.prepareStatement("SELECT * from likes WHERE msgID = ?");

            // COMMENT prepared statements
            db.psInsertComment = db.mConnection.prepareStatement("INSERT INTO comments VALUES (default, ?, ?, ?, ?, ?)");
            db.psGetCmtFileData = db.mConnection.prepareStatement("SELECT * from cmtfiles WHERE cmtID = ?");
            db.psSelectComment = db.mConnection.prepareStatement("SELECT * from comments WHERE cmtID = ?");
            db.psSelectAllComments = db.mConnection.prepareStatement("SELECT * from comments");
            db.psSelectAllCommentsWithFlags = db.mConnection.prepareStatement("SELECT * FROM comments WHERE flag_count > 0 ORDER BY flag_count DESC");
            db.psUpdateComment = db.mConnection.prepareStatement("UPDATE comments SET content = ? WHERE cmtID = ?");
            db.psDeleteComment = db.mConnection.prepareStatement("DELETE FROM comments WHERE cmtID = ?");

            // FILE prepared statements (two tables: message files and comment files)
            db.psInsertMessageFile = db.mConnection.prepareStatement("INSERT INTO msgfiles VALUES (?, ?, ?, ?)");
            db.psSelectMessageFile = db.mConnection.prepareStatement("SELECT * from msgfiles WHERE fileID = ?");
            db.psGetMsgFileID = db.mConnection.prepareStatement("SELECT fileID from msgfiles WHERE filename = ?");
            db.psSelectAllMsgFiles = db.mConnection.prepareStatement("SELECT * from msgfiles");
            db.psDeleteMsgFile = db.mConnection.prepareStatement("DELETE FROM msgfiles WHERE fileID = ?");

            db.psInsertCommentFile = db.mConnection.prepareStatement("INSERT INTO cmtfiles VALUES (?, ?, ?, ?)");
            db.psSelectCommentFile = db.mConnection.prepareStatement("SELECT * from cmtfiles WHERE fileID = ?");
            db.psGetCmtFileID = db.mConnection.prepareStatement("SELECT fileID from cmtfiles WHERE filename = ?");
            db.psSelectAllCmtFiles = db.mConnection.prepareStatement("SELECT * from cmtfiles");
            db.psDeleteCmtFile = db.mConnection.prepareStatement("DELETE FROM cmtfiles WHERE fileID = ?");

            // MSG FLAG prepared statements
            db.psInsertMsgFlag = db.mConnection.prepareStatement("call add_new_flagged_msg(?, ?)");
            db.psSelectMessageFlag = db.mConnection.prepareStatement("SELECT * from flagged_msgs where msgID = ? AND userID = ?");

            // CMT FLAG prepared statements
            db.psInsertCmtFlag = db.mConnection.prepareStatement("call add_new_flagged_comments(?, ?)");
            db.psSelectCommentFlag = db.mConnection.prepareStatement("SELECT * from flagged_comments where cmtID = ? AND userID = ?");

            //check if msgid exists in flagged messages table
            db.psCheckMsgIDFlag = db.mConnection.prepareStatement("SELECT * FROM flagged_msgs WHERE msgid = ?");

            // block user
            db.psBlockUser = db.mConnection.prepareStatement("call add_new_blocked_user(?, ?)");
            db.psSelectAllBlockedUsers = db.mConnection.prepareStatement("SELECT * FROM blocked");

            // I commented these out because we may not need all of them

            // db.psUserTableUpdateName = db.mConnection.prepareStatement("UPDATE user SET username ?");    // this makes sense yes
            //     db.psUserTableUpdateBio = db.mConnection.prepareStatement("UPDATE user SET bio = ? WHERE user = ?");
            //     db.psCommentTableUpdateContent = db.mConnection.prepareStatement("UPDATE comment SET content = ? WHERE id = ?");
            //     db.psCommentTableUpdateMsgID = db.mConnection.prepareStatement("UPDATE comment SET msgID = ? WHERE id = ?");
            //     db.psCommentTableUpdateUserID = db.mConnection.prepareStatement("UPDATE comment SET userID = ? WHERE id = ?");
            //     db.psLikesTableUpdateMsgID = db.mConnection.prepareStatement("UPDATE likes SET msgID = ? WHERE id = ?");
            //     db.psLikesTableUpdateUserID = db.mConnection.prepareStatement("UPDATE likes SET userID = ? WHERE id = ?");
            //     db.psLikesTableUpdateStatus = db.mConnection.prepareStatement("UPDATE likes SET status = ? WHERE id = ?");
            // // USERS needs a way to, given the name, get the bio
            // // expecting to send in the email string and get the user id, but we might just make a user id, but we might not need that
            //     db.psMessageTableUpdateContent = db.mConnection.prepareStatement("UPDATE message SET content = ? WHERE id = ?");
            //     db.psMessageTableUpdateUserID = db.mConnection.prepareStatement("UPDATE message SET userID = ? WHERE id = ?");
            // // Standard CRUD operations
            // db.psDeleteOne = db.mConnection.prepareStatement("DELETE FROM ? WHERE id = ?");                          //Deletes a row
            // // db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO ? VALUES (default, ?, ?)");                //Inserts a row 
            // db.psSelectAll = db.mConnection.prepareStatement("SELECT * FROM ?");                                     //Selects all the rows

            // db.mSelectOne = db.mConnection.prepareStatement("SELECT * from ? WHERE id = ?");                        //Selects a specific row
            // db.mSelectOneBio = db.mConnection.prepareStatement("SELECT * from ? WHERE username = ?");                        //Selects a specific row
            // db.mInsertOneUser = db.mConnection.prepareStatement("INSERT INTO user VALUES (?, ?)");                  
            // db.mInsertOneComment = db.mConnection.prepareStatement("INSERT INTO comment VALUES (default, ?, ?, ?");
            // db.mInsertOneLike = db.mConnection.prepareStatement("INSERT INTO likes VALUES (default, ?, ?, ?)");
            // db.mInsertOneMessage = db.mConnection.prepareStatement("INSERT INTO message VALUES (default, ?, ?");
            // // db.mUpdateOne = db.mConnection.prepareStatement("UPDATE ? SET message = ? WHERE id = ?");               //Updates a row
            // // db.mIncrementLikes = db.mConnection.prepareStatement("UPDATE ? SET likes = likes + 1 WHERE id = ?");    //Increments the likes column
            // // db.mDecrementLikes = db.mConnection.prepareStatement("UPDATE ? SET likes = likes - 1 WHERE id = ?");    //Decrements the likes column

        } catch (SQLException e){
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
            return null;
        }
        return db;
    }

    /**
     * Close the current connection to the database, if one exists.
     * 
     * NB: The connection will always be null after this call, even if an 
     *     error occurred during the closing operation.
     * 
     * @return True if the connection was cleanly closed, false otherwise
     */
    boolean disconnect(){
        if (mConnection == null){
            System.err.println("Unable to close connection: Connection was null");
            return false;
        }
        try {
            mConnection.close();
        } catch (SQLException e){
            System.err.println("Error: Connection.close() threw a SQLException");
            e.printStackTrace();
            mConnection = null;
            return false;
        }
        mConnection = null;
        return true;
    }

    /**
     * Increments the like value of a row
     * @param id: the id of the message
     */
    int createTable(){
        try {
            psCreateTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    int createMsgTable() {
        try {
            psMessageTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    int createLikesTable() {
        try {
            psLikesTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    int createCommentsTable() {
        try {
            psCommentTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    int createUserTable() {
        try {
            psUserTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    /**
     * Decrements the like value of a row
     * @param id: the id of the message
     */
    int dropTable(){
        try {
            psDropTable.execute();
            return 1;
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Increments the like value of a row
     * @param id: the id of the message
     */
    int incrementLikes(int id){
        try {
            psIncrementLikes.setInt(1, id);
            psIncrementLikes.execute();
            return 1;
        } catch(SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Decrements the like value of a row
     * @param id: the id of the message
     */
    int decrementLikes(int id){
        try {
            psDecrementLikes.setInt(1, id);
            psDecrementLikes.execute();
            return 1;
        } catch(SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    // int countMessageFlags(int id) {
    //     try {
    //         psCountMessageFlags.setInt(1, id);
    //         ResultSet rs = psCountMessageFlags.executeQuery();
    //         while (rs.next()){
    //             res.add( rs.getInt("flag_count")));
    //         }
    //         rs.close();
    //         return res;
    //         return 0;
    //     } catch(SQLException e) {
    //         e.printStackTrace();
    //         return -1;
    //     }
    // }

    /**
     * Tests to see if a message is valid
     * @param message: The message being checked
     * @return: Returns true if valid and false if invalid
     */
    public static boolean testString(String message){
        try {
            if(message == null || message.equals("")){
                throw new InvalidMessageException();
            }
        } catch(InvalidMessageException e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean validFileID(String fileID) {
        fileID = fileID.strip();
        if (fileID.contains(" ")) { // file ID should not include any spaces
            return false;
        }
        int length = fileID.length();
        if (length != 33) { // file ID generated by Drive API should be 33 characters
            return false;
        }
        return true;
    }
}

//Exception to see if invalid message is passed
class InvalidMessageException extends Exception {
    InvalidMessageException(){
        super("Invalid String input");
    }
    
    InvalidMessageException(String message){
        super(message);
    }
}
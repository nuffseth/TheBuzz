package edu.lehigh.cse216.rok224.admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import javax.swing.plaf.metal.MetalComboBoxButton;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;

public class Database {
    /**
     * The connection to the database.  When there is no connection, it should
     * be null.  Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    // prepared statements from phase 0 - do we still need these?
    // private PreparedStatement mSelectAll;
    // private PreparedStatement mSelectOne;
    // private PreparedStatement mDeleteOne;
    // private PreparedStatement mInsertOne; 
    // private PreparedStatement mUpdateOne;

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
    private PreparedStatement psUpdateUser;

    // MESSAGE PREPARED STATEMENTS
    private PreparedStatement psInsertMessage;
    private PreparedStatement psSelectMessage;
    private PreparedStatement psGetMsgLikes;
    private PreparedStatement psGetMsgComments;
    private PreparedStatement psSelectAllMessages;
    private PreparedStatement psUpdateMessage;
    private PreparedStatement psDeleteMessage;

    // LIKE PREPARED STATEMENTS
    private PreparedStatement psInsertLike;
    private PreparedStatement psSelectLike;
    private PreparedStatement psUpdateLike;
    private PreparedStatement psSelectAllLikes; 

    // COMMENT PREPARED STATEMENTS
    private PreparedStatement psInsertComment;
    private PreparedStatement psSelectComment;
    private PreparedStatement psSelectAllComments;
    private PreparedStatement psUpdateComment;
    private PreparedStatement psDeleteComment;

    // FILE PREPARED STATEMENTS
    private PreparedStatement psInsertMessageFile;
    private PreparedStatement psSelectMessageFile;
    private PreparedStatement psInsertCommentFile;
    private PreparedStatement psSelectCommentFile;

    // PHASE 3 - SQL RUN ON HEROKU DATA EXPLORER TO EDIT TABLES
    // ALTER TABLE messages ADD COLUMN msgLink INT
    // ALTER TABLE messages ADD COLUMN cmtLink INT
    // ALTER TABLE messages ADD CONSTRAINT msg_link_key FOREIGN KEY(msgLink) REFERENCES messages(msgID)
    // ALTER TABLE messages ADD CONSTRAINT cmt_link_key FOREIGN KEY(cmtLink) REFERENCES comments(cmtID)

    // ALTER TABLE comments ADD COLUMN msgLink INT
    // ALTER TABLE comments ADD COLUMN cmtLink INT
    // ALTER TABLE comments ADD CONSTRAINT msg_link_key FOREIGN KEY(msgLink) REFERENCES messages(msgID)
    // ALTER TABLE comments ADD CONSTRAINT cmt_link_key FOREIGN KEY(cmtLink) REFERENCES comments(cmtID)


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

    // add new user
    int insertUser (String user, String bio) {
        // TODO: NEED TO CHECK TO SEE IF THE USER EMAIL ALREADY EXISTS
        // i need to both check the overall validity of the strings getting passed in,
        // as well as, in the case that we do get a valid string, if it already exists
        
        int ret = 0;

        if (testString(user) == false){ // generic validity check on both params
            return -1;
        }

        // TODO: what do the executeQuery things return? if user is not found, is that an error??
        // check to see if user already exists in the User table
        ResultSet rs = null;
        // try {
        //     System.out.println("checking if user is in database...");
        //     psSelectUser.setString(1, user);
        //     rs = psSelectUser.executeQuery();
        // } catch (SQLException e1) {
        //     e1.printStackTrace();
        // } 
        // System.out.println(rs);
        // if (rs != null) { // if user is found in the table, return 1
        //     return 1;
        // }
        
        try {
            System.out.println("trying to add user to database...");
            psInsertUser.setString(1, user);  // first param is being set as user
            psInsertUser.setString(2, bio);   // second param is being set as bio
            ret += psInsertUser.executeUpdate();
        } catch (SQLException e) {
            if (e.toString().contains("Key (userid)=(" + user + ") already exists.")) {
                ret = 0;
            }
            else {
                e.printStackTrace();
            }
        }
        return ret;
    }

    // get user from ID
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

    // update user bio of a given user ID
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
     * All objects and functions for the Comment table
     */
    public class Comment {
        String mUserID;
        int mCommentID;
        int mMsgID;
        String mContent;

        public Comment(int commentID, String userID, int msgID, String content) {
            mUserID = userID;
            mCommentID = commentID;
            mMsgID = msgID;
            mContent = content;
        }
    }

    // add a comment to the table
    int insertComment (int msgID, String userID, String content) {
        int ret = 0;
        // TODO: is there a testInt method to use to test msgID?
        if ( !testString(content) || !testString(userID) ) {   // generic validity check 
            return -1;
        } 

        try {
            psInsertComment.setInt(2, msgID);
            psInsertComment.setString(3, userID);
            psInsertComment.setString(4, content);

            ret += psInsertComment.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return ret;
    }

    // select one comment 
    Comment selectComment(int cmtID) {
        Comment res = null;
        try {
            psSelectComment.setInt(1, cmtID);
            ResultSet rs = psSelectComment.executeQuery();
            if(rs.next()){
                res = new Comment(rs.getInt("cmtID"), rs.getString("userID"), rs.getInt("msgID"), rs.getString("bio"));
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return res;
    }

    // select all comments for a specific message
    // this is exactly the same as getMsgComments ...
    ArrayList<Comment> selectAllComments(int msgID){
        ArrayList<Comment> res = new ArrayList<Comment>();
        try {
            psGetMsgComments.setInt(1, msgID);
            ResultSet rs = psGetMsgComments.executeQuery();
            while (rs.next()){
                res.add(new Comment(rs.getInt("cmtID"), 
                        rs.getString("userID"), rs.getInt("msgID"), rs.getString("content")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    // update a comment
    // TODO: should we do a testInt for cmtID?
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
        ArrayList<Comment> mComments;

        public Message(int msgID, String userID, String content, int numLikes, ArrayList<Comment> comments) {
            mMsgID = msgID;
            mUserID = userID;
            mContent = content;
            mNumLikes = numLikes;
            mComments = comments; // this is maybe probably wrong b/c arraylists :D
        }
    }

    // add a new message
    int insertMessage (String userID, String content) {
        int ret = 0; 
        if (testString(content) == false || testString(userID) == false) { // generic validity check
            return -1;
        }
        
        try {
            psInsertMessage.setString(2, userID);
            psInsertMessage.setString(3, content);
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
                res.add(new Comment(rs.getInt("cmtID"), 
                        rs.getString("userID"), rs.getInt("msgID"), rs.getString("content")));
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
                res = new Message(rs.getInt("msgID"), rs.getString("userID"), rs.getString("content"), likes, allComments);
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

                // create Message object and add to our ArrayList
                Message thisMessage = new Message(rs.getInt("msgID"), rs.getString("userID"), rs.getString("content"), likes, comments);
                res.add(thisMessage);
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    // update a message
    // TODO: should we do a testInt for msgID?
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

    // TODO: work with backend to create a method to upload a file from the drive
    String postFileContent(String file_content, String mime) {
        // file_content is a bigass string of base64 stuff
        String fileID = null;
        return fileID;
    }
    // TODO: work with backend to create a method to download a file from the drive
    String getFileContent(String fileID) {
        String file_content = null;
        return file_content;
    }

    int insertMessageFile(String fileID, int msgID, String mime, String filename) {
        int ret = 0; 
        if (testString(mime) == false || testString(filename) == false) { // generic validity check
            return -1;
        }
        
        try {
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
    // in order to obtain file contents, need to use getFileContent (connection to Drive)
    MyFile selectMsgFile(String fileID) {
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

    int insertCommentFile(String fileID, int cmtID, String mime, String filename) {
        int ret = 0; 
        if (testString(mime) == false || testString(filename) == false) { // generic validity check
            return -1;
        }
        
        try {
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
    // in order to obtain file contents, need to use getFileContent (connection to Drive)
    MyFile selectCmtFile(String fileID) {
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

    /**
     * The Database constructor is private: we only create Database objects 
     * through the getDatabase() method.
     */
    private Database() {
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
    static Database getDatabase(String url) {
        // Create an un-configured Database object
        Database db = new Database();

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
                "CREATE TABLE msgfiles (fileID TEXT, msgID INT, mime TEXT, filename VARCHAR(500), " +
                "CONSTRAINT msg_key FOREIGN KEY(msgID) REFERENCES messages(msgID) )"
            );

            db.psCmtFileTable = db.mConnection.prepareStatement(
                "CREATE TABLE cmtfiles (fileID TEXT, cmtID INT, mime TEXT, filename VARCHAR(500), " +
                "CONSTRAINT cmt_key FOREIGN KEY(cmtID) REFERENCES comments(cmtID) )"
            );

            // USER prepared statements
            db.psInsertUser = db.mConnection.prepareStatement("INSERT INTO users VALUES (?, ?)");  
            db.psSelectUser = db.mConnection.prepareStatement("SELECT * from users where userID = ?");
            db.psUpdateUser = db.mConnection.prepareStatement("UPDATE users SET bio = ? WHERE userID = ?");

            // MESSAGE prepared statements
            db.psInsertMessage = db.mConnection.prepareStatement("INSERT INTO messages VALUES (default, ?, ?)");
            db.psGetMsgLikes = db.mConnection.prepareStatement("SELECT * from likes WHERE msgID = ?");
            db.psGetMsgComments = db.mConnection.prepareStatement("SELECT * from comments WHERE msgID = ?");
            db.psSelectMessage = db.mConnection.prepareStatement("SELECT * from messages WHERE msgID = ?");
            db.psSelectAllMessages = db.mConnection.prepareStatement("SELECT * FROM messages");
            db.psUpdateMessage = db.mConnection.prepareStatement("UPDATE messages SET content = ? WHERE msgID = ?");
            db.psDeleteMessage = db.mConnection.prepareStatement("DELETE FROM messages WHERE msgID = ?");
  
            // LIKE prepared statements
            db.psInsertLike = db.mConnection.prepareStatement("INSERT INTO likes VALUES (?, ?, ?)");
            // TODO: HOW TO UPDATE A LIKE WHEN WE ARE USING A JOINT PRIMARY KEY?
            db.psUpdateLike = db.mConnection.prepareStatement("UPDATE likes SET status = ? WHERE msgID = ? AND userID = ?");
            db.psSelectLike = db.mConnection.prepareStatement("SELECT * from likes where msgID = ? AND userID = ?");
            db.psSelectAllLikes = db.mConnection.prepareStatement("SELECT * from likes WHERE msgID = ?");

            // COMMENT prepared statements
            db.psInsertComment = db.mConnection.prepareStatement("INSERT INTO comments VALUES (default, ?, ?, ?)");
            db.psSelectComment = db.mConnection.prepareStatement("SELECT * from comments WHERE cmtID = ?");
            db.psUpdateComment = db.mConnection.prepareStatement("UPDATE comments SET content = ? WHERE cmtID = ?");
            db.psDeleteComment = db.mConnection.prepareStatement("DELETE FROM comments WHERE cmtID = ?");

            // FILE prepared statements (two tables: message files and comment files)
            db.psInsertMessageFile = db.mConnection.prepareStatement("INSERT INTO msgfiles VALUES (?, ?, ?, ?)");
            db.psSelectMessageFile = db.mConnection.prepareStatement("SELECT * from msgfiles WHERE fileID = ?");

            db.psInsertCommentFile = db.mConnection.prepareStatement("INSERT INTO cmtfiles VALUES (?, ?, ?, ?)");
            db.psSelectCommentFile = db.mConnection.prepareStatement("SELECT * from cmtfiles WHERE fileID = ?");

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

    /**
     * Tests to see if a message is valid
     * @param message: The message being checked
     * @return: Returns true if valid and false if invalid
     */
    public static boolean testString(String message){
        try {
            if(message.equals("") || message == null){
                throw new InvalidMessageException();
            }
        } catch(InvalidMessageException e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
}

//Exception to see if invalid message is passed
class InvalidMessageException extends Exception {
    InvalidMessageException(){
        super("Invalid Message");
    }
    
    InvalidMessageException(String message){
        super(message);
    }
}
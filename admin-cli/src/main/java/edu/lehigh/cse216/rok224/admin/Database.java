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

    /**
     * Update the message for a row in the database
     * 
     * @param id The id of the row to update
     * @param message The new message contents
     * 
     * @return The number of rows that were updated.  -1 indicates an error.
     */

    /**
     * Delete a row by ID
     * 
     * @param id The id of the row to delete
     * 
     * @return The number of rows that were deleted.  -1 indicates an error.
     */

    /**
     * Query the database for a list of all their IDs
     * 
     * @return All rows, as an ArrayList
     */

    /**
     * Get all data for a specific row, by ID
     * 
     * @param id The id of the row being requested
     * 
     * @return The data for the requested row, or null if the ID was invalid
     */

    /**
     * Insert a row into the database
     * 
     * @param message The message body for this new row
     * @param likes The amount of likes a message has
     * 
     * @return The number of rows that were inserted
     */

    /**
     * RowData is like a struct in C: we use it to hold data, and we allow 
     * direct access to its fields.  In the context of this Database, RowData 
     * represents the data we'd see in a row.
     * 
     * We make RowData a static class of Database because we don't really want
     * to encourage users to think of RowData as being anything other than an
     * abstract representation of a row of the database.  RowData and the 
     * Database are tightly coupled: if one changes, the other should too.
     */  

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


    // USER PREPARED STATEMENTS 
    private PreparedStatement psInsertUser;
    private PreparedStatement psSelectUser;
    private PreparedStatement psUpdateUser;

    // MESSAGE PREPARED STATEMENTS
    private PreparedStatement psInsertMessage;
    private PreparedStatement psSelectMessage;
    private PreparedStatement psSelectAllMessages;
    private PreparedStatement psUpdateMessage;
    private PreparedStatement psDeleteMessage;

    // LIKE PREPARED STATEMENTS
    private PreparedStatement psInsertLike;
    private PreparedStatement psUpdateLike;

    // COMMENT PREPARED STATEMENTS
    private PreparedStatement psInsertComment;
    private PreparedStatement psSelectComment;
    private PreparedStatement psSelectAllComments;
    private PreparedStatement psUpdateComment;
    private PreparedStatement psDeleteComment;


    // DEPRECATED
    // private PreparedStatement mCommentTableUpdateContent;
    // private PreparedStatement mCommentTableUpdateUserID;
    // private PreparedStatement mCommentTableUpdateMsgID;
    // //-----------------------=
    // private PreparedStatement mLikesTableUpdateStatus;
    // private PreparedStatement mLikesTableUpdateUserID;
    // private PreparedStatement mLikesTableUpdateMsgID;
    // //-----------------------=
    // private PreparedStatement mMessageTableUpdateContent;
    // private PreparedStatement mMessageTableUpdateUserID;
    // private PreparedStatement mInsertOneUser;
    // private PreparedStatement mInsertOneComment;
    // private PreparedStatement mInsertOneLike;
    // private PreparedStatement mInsertOneMessage;
    // private PreparedStatement mSelectOneBio;
    

    /**
     * All objects and functions for the User table
     */
    public class User {
        String mUserID;
        String mBio;

        public User(String userID, String bio) {
            mUserID = userID;
            mBio = bio;
        }

        // add new user
        int insertRowUser (String user, String bio) {
        
            // TODO: NEED TO CHECK TO SEE IF THE USER EMAIL ALREADY EXISTS
            // i need to both check the overall validity of the strings getting passed in,
            // as well as, in the case that we do get a valid string, if it already exists
            
            int ret = 0;
    
            if (testString(user) == false || testString(bio) == false){ // generic validity check on both params
                return -1;
            }
    
            // check to see if user already exists in the User table
            if (true) { 
            }
            
            try {
                mInsertOneUser.setString(1, user);  // first param is being set as user
                mInsertOneUser.setString(2, bio);   // second param is being set as bio
                ret += mInsertOneUser.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return ret;
        }

        // get user from ID

        int updateBioUserTable (String bio, String user) {
            int ret = 0;
    
            if (testString(bio) == false || testString(user) == false) {
                return -1;
            }
            
            try {
                mUserTableUpdateBio.setString(1, bio);
                mUserTableUpdateBio.setString(2, user);
                ret += mUserTableUpdateBio.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
            return ret;
        }
    }

    /**
     * All objects and functions for the Comment table
     */
    public class Comment {
        String mUserID;
        int mCommentID;
        int mMsgID;
        String mContent;

        public Comment(String userID, int commentID, int msgID, String content) {
            mUserID = userID;
            mCommentID = commentID;
            mMsgID = msgID;
            mContent = content;
        }

        // add a comment to the table

        // select one comment ?

        // select all comments for a specific message

        // update a comment

        // delete a comment

        int insertRowComments (String content, String userID, int msgID) {
            int ret = 0;
            
            if (testString(content) == false || testString(userID) == false) {   // generic validity check 
                return -1;
            } 
    
            try {
                mInsertOneComment.setString(1, content);
                mInsertOneComment.setString(2, userID);
                mInsertOneComment.setInt(3, msgID);
                ret += mInsertOneComment.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
            return ret;
        }
    
        int updateContentCommentsTable(String content, int msgID) {
            int ret = 0;
            
            if (testString(content) == false || msgID >= 0) {
                return -1;
            }
    
            try {
                mCommentTableUpdateContent.setString(1, content);
                mCommentTableUpdateContent.setInt(2, msgID);
                ret += mCommentTableUpdateContent.executeUpdate();
            } catch(SQLException e) {
                e.printStackTrace();
            }
     
            return ret;
        }

        static ArrayList<RowDataComments> selectAllComments() {
            ArrayList<RowDataComments> ret = new ArrayList<RowDataComments>();
    
            try {
                ResultSet rs = mSelectAll.executeQuery("comment"); 
                while (rs.next()) { 
                    ret.add(new RowDataComments(rs.getString("userID"), rs.getInt("id"), rs.getInt("msgID"), rs.getString("content")));
                }
                rs.close();
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Object type for a single row in the Likes table
     */
    public class Like {
        String mUserID;
        int mLikeID;
        int mMsgID;
        int mStatus;

        public Like(String userID, int likeID, int msgID, int status) {
            mUserID = userID;
            mLikeID = likeID;
            mMsgID = msgID;
            mStatus = status;
        }

        // add a new like to the table

        // update a like in the table

        int insertRowLikes (int status, String userID, int msgID) {
            int ret = 0;
    
            if (testString(userID) == false) {  // generi validity check
                return -1;
            }
    
            try {
                mInsertOneLike.setInt(1, status);
                mInsertOneLike.setString(2, userID);
                mInsertOneLike.setInt(3, msgID);
                ret += mInsertOneLike.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
            return ret;
        }

        int updateStatusLikesTable(int status, int id) {
            int ret = 0;
            
            // TODO: WOULD THIS STILL NEED An IF BLOCK TO CHECK RETURN
    
            try {
                mLikesTableUpdateStatus.setInt(1, status);
                mLikesTableUpdateStatus.setInt(2, id);
                ret += mLikesTableUpdateStatus.executeUpdate();
            } catch(SQLException e) {
                e.printStackTrace();
            }
    
            return ret;
        }
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


        public Message(String userID, int msgID, String content, int numLikes, ArrayList<Comment> comments) {
            mUserID = userID;
            mMsgID = msgID;
            mContent = content;
            mNumLikes = numLikes;
            mComments = comments; // this is maybe probably wrong b/c arraylists :D
        }

        // add a new message

        // select a specific message

        // select all messages

        // update a message

        // delete a message

        int insertRowMessages (String content, String userID) {
            int ret = 0;
            
            if (testString(content) == false || testString(userID) == false) { // generic validity check
                return -1;
            }
            
            try {
                mInsertOneMessage.setString(1, content);
                mInsertOneMessage.setString(2, userID);
                ret += mInsertOneMessage.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
            return ret;
        }

        static ArrayList<RowDataMessages> selectAllMessages() {
            ArrayList<RowDataMessages> ret = new ArrayList<RowDataMessages>();
    
            try {
                ResultSet rs = mSelectAll.executeQuery("message");
    
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    
        int countAllLikesOnMessage(int msgID) {
            int ret = -1; 
    
            return ret;
        }

        int updateContentMessageTable(String content, int id){ 
            int ret = 0;
            
            if (testString(content) == false) {
                return -1;
            }
    
            try {
                mMessageTableUpdateContent.setString(1, content);
                mMessageTableUpdateContent.setInt(2, id);
                ret += mMessageTableUpdateContent.executeUpdate();
            } catch(SQLException e) {
                e.printStackTrace();
            }
    
            return ret;
        }
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

        // Attempt to create all of our prepared statements.  If any of these 
        // fail, the whole getDatabase() call should fail
        try {
            // NB: we can easily get ourselves in trouble here by typing the
            //     SQL incorrectly.  We really should have things like "tblData"
            //     as constants, and then build the strings for the statements
            //     from those constants.

            // Note: no "IF NOT EXISTS" or "IF EXISTS" checks on table 
            // creation/deletion, so multiple executions will cause an exception
            // TODO: I don't think we need this??
            db.psCreateTable = db.mConnection.prepareStatement("CREATE TABLE tblData (id SERIAL PRIMARY KEY, message VARCHAR(500) NOT NULL, likes INT)"); //Creates the table
            db.psDropTable = db.mConnection.prepareStatement("DROP TABLE tblData"); //Deletes the table

            // Create all the tables we need 
            db.psUserTable = db.mConnection.prepareStatement("CREATE TABLE user (" + 
                            "userID VARCHAR(500) NOT NULL PRIMARY KEY, " + 
                            "bio TEXT)");
            db.psMessageTable = db.mConnection.prepareStatement("CREATE TABLE message (" + 
                            "id SERIAL PRIMARY KEY, " + 
                            "userID VARCHAR(500) FOREIGN KEY REFERENCES user(userID), " + 
                            "content TEXT NOT NULL)");
            db.psCommentTable = db.mConnection.prepareStatement("CREATE TABLE comment (" + 
                            "id SERIAL PRIMARY KEY, " + 
                            "msgID SERIAL FOREIGN KEY REFERENCES message(msgID), " + 
                            "userID VARCHAR(500) FOREIGN KEY REFERENCES user(userID), " + 
                            "content TEXT NOT NULL)");    
            db.psLikesTable = db.mConnection.prepareStatement("CREATE TABLE likes (" +
                            "userID VARCHAR(500) FOREIGN KEY REFERENCES user(userID), " + 
                            "msgID SERIAL FOREIGN KEY REFERENCES message(msgID), " +
                            "status INT, " + 
                            "CONSTRAINT like_key PRIMARY KEY (userID, msgID))");  

            // USER prepared statements
            db.psInsertUser = db.mConnection.prepareStatement("INSERT INTO user VALUES (?, ?)");  
            db.psSelectUser = db.mConnection.prepareStatement("SELECT * from user where userID = ?");
            db.psUpdateUser = db.mConnection.prepareStatement("UPDATE user SET bio = ? WHERE userID = ?");

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
     * Create tblData.  If it already exists, this will print an error
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
     * Remove tblData from the database.  If it does not exist, this will print
     * an error.
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
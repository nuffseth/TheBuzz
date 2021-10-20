package edu.lehigh.cse216.rok224.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;

public class Database {
    /**
     * The connection to the database.  When there is no connection, it should
     * be null.  Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    /**
     * A prepared statement for getting all data in the database
     */
    private PreparedStatement mSelectAll;

    /**
     * A prepared statement for getting one row from the database
     */
    private PreparedStatement mSelectOne;

    /**
     * A prepared statement for deleting a row from the database
     */
    private PreparedStatement mDeleteOne;

    /**
     * A prepared statement for inserting into the database
     */
    // private PreparedStatement mInsertOne;
    // replaced with specific inserts for each table

    /**
     * A prepared statement for updating a single row in the database
     */
    // private PreparedStatement mUpdateOne;
    // replaced with specific updates for each part of each table

    /**
     * A prepared statement for creating the table in our database
     */
    private PreparedStatement mCreateTable;

    /**
     * A prepared statement for dropping the table in our database
     */
    private PreparedStatement mDropTable;

    /**
     * A prepared statement to increment likes
     */
    private PreparedStatement mIncrementLikes;

    /**
     * A prepared statement to decrement likes
     */
    private PreparedStatement mDecrementLikes;

    // TABLES
    private PreparedStatement mUserTable;
    private PreparedStatement mMessageTable;
    private PreparedStatement mLikesTable;
    private PreparedStatement mCommentTable;


    // UPDATE TABLES 
    private PreparedStatement mUserTableUpdateName;
    private PreparedStatement mUserTableUpdateBio;
    //-----------------------=
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
    public static class RowData {
        /**
         * The ID of this row of the database
         */
        int mId;

        /**
         * The message stored in this row
         */
        String mMessage;

        /**
         * The amount of likes for the message
         */
        int mLikes;


        // TODO: MODIFY ROWDATA

        /**
         * Constructor for RowData
         * @param id: Id of post
         * @param message: The message itself
         * @param likes: The amount of likes it has
         */
        public RowData(int id, String message, int likes){
            mId = id;
            mMessage = message;
            mLikes = likes;    
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
            db.mCreateTable = db.mConnection.prepareStatement("CREATE TABLE tblData (id SERIAL PRIMARY KEY, message VARCHAR(500) NOT NULL, likes INT)"); //Creates the table
            db.mDropTable = db.mConnection.prepareStatement("DROP TABLE tblData"); //Deletes the table

            // table management 
            db.mUserTable = db.mConnection.prepareStatement("CREATE TABLE user (username VARCHAR(500) NOT NULL, bio VARCHAR(500))");
                db.mUserTableUpdateName = db.mConnection.prepareStatement("UPDATE user SET username ?");    // this makes sense yes
                db.mUserTableUpdateBio = db.mConnection.prepareStatement("UPDATE user SET bio = ? WHERE user = ?");
            db.mCommentTable = db.mConnection.prepareStatement("CREATE TABLE comment (id SERIAL PRIMARY KEY, content VARCHAR(500) NOT NULL, userID VARCHAR(500), msgID INT)");
                db.mCommentTableUpdateContent = db.mConnection.prepareStatement("UPDATE comment SET content = ? WHERE id = ?");
                db.mCommentTableUpdateMsgID = db.mConnection.prepareStatement("UPDATE comment SET msgID = ? WHERE id = ?");
                db.mCommentTableUpdateUserID = db.mConnection.prepareStatement("UPDATE comment SET userID = ? WHERE id = ?");
            db.mLikesTable = db.mConnection.prepareStatement("CREATE TABLE likes (id SERIAL PRIMARY KEY, status INT, userID INT, msgID VARCHAR(500))");
                db.mLikesTableUpdateMsgID = db.mConnection.prepareStatement("UPDATE likes SET msgID = ? WHERE id = ?");
                db.mLikesTableUpdateUserID = db.mConnection.prepareStatement("UPDATE likes SET userID = ? WHERE id = ?");
                db.mLikesTableUpdateStatus = db.mConnection.prepareStatement("UPDATE likes SET status = ? WHERE id = ?");
            // USERS needs a way to, given the name, get the bio
            // expecting to send in the email string and get the user id, but we might just make a user id, but we might not need that
            db.mMessageTable = db.mConnection.prepareStatement("CREATE TABLE message (id SERIAL PRIMARY KEY, content VARCHAR(500) NOT NULL, userID VARCHAR(500))");
                db.mMessageTableUpdateContent = db.mConnection.prepareStatement("UPDATE message SET content = ? WHERE id = ?");
                db.mMessageTableUpdateUserID = db.mConnection.prepareStatement("UPDATE message SET userID = ? WHERE id = ?");

            // Standard CRUD operations
            db.mDeleteOne = db.mConnection.prepareStatement("DELETE FROM ? WHERE id = ?");                          //Deletes a row
            // db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO ? VALUES (default, ?, ?)");                //Inserts a row
            db.mSelectAll = db.mConnection.prepareStatement("SELECT * from ?");                                     //Selects all the rows
            db.mSelectOne = db.mConnection.prepareStatement("SELECT * from ? WHERE id = ?");                        //Selects a specific row
            db.mSelectOneBio = db.mConnection.prepareStatement("SELECT * from ? WHERE username = ?");                        //Selects a specific row
                                                                                                                    
                                                                                                                    // insertOne but for specific tables
            // ? gets filled in with java later
            db.mInsertOneUser = db.mConnection.prepareStatement("INSERT INTO user VALUES (?, ?)");                  
            db.mInsertOneComment = db.mConnection.prepareStatement("INSERT INTO comment VALUES (default, ?, ?, ?");
            db.mInsertOneLike = db.mConnection.prepareStatement("INSERT INTO likes VALUES (default, ?, ?, ?)");
            db.mInsertOneMessage = db.mConnection.prepareStatement("INSERT INTO message VALUES (default, ?, ?");
            // db.mUpdateOne = db.mConnection.prepareStatement("UPDATE ? SET message = ? WHERE id = ?");               //Updates a row
            // db.mIncrementLikes = db.mConnection.prepareStatement("UPDATE ? SET likes = likes + 1 WHERE id = ?");    //Increments the likes column
            // db.mDecrementLikes = db.mConnection.prepareStatement("UPDATE ? SET likes = likes - 1 WHERE id = ?");    //Decrements the likes column

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

    // /**
    //  * Insert a row into the database
    //  * 
    //  * @param message The message body for this new row
    //  * @param likes The amount of likes a message has
    //  * 
    //  * @return The number of rows that were inserted
    //  */
    // int insertRow(String message, int likes){
    //     int count = 0;
    //     if(testMessage(message) == false){
    //         return -1;
    //     }
    //     try {
    //         mInsertOne.setString(1, message);
    //         mInsertOne.setInt(2, likes);
    //         count += mInsertOne.executeUpdate();
    //     } catch (SQLException e){
    //         e.printStackTrace();
    //     }
    //     return count;
    // }

    // TODO: RETURN -1/NULL INSTEAD OF 
    void insertRowUser (String user, String bio) {
        // TODO: NEED TO CHECK TO SEE IF THE USER EMAIL ALREADY EXISTS
        


        try {
            mInsertOneUser.setString(1, user);  // first param is being set as user
            mInsertOneUser.setString(2, bio);   // second param is being set as bio
            mInsertOneUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void insertRowComments (String content, String userID, int msgID) {
        try {
            mInsertOneComment.setString(1, content);
            mInsertOneComment.setString(2, userID);
            mInsertOneComment.setInt(3, msgID);
            mInsertOneComment.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void insertRowLikes (int status, String userID, int msgID) {
        try {
            mInsertOneLike.setInt(1, status);
            mInsertOneLike.setString(2, userID);
            mInsertOneLike.setInt(3, msgID);
            mInsertOneLike.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void insertRowMessages (String content, int userID) {
        try {
            mInsertOneMessage.setString(1, content);
            mInsertOneMessage.setInt(2, userID);
            mInsertOneMessage.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Query the database for a list of all their IDs
     * 
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAll(){
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAll.executeQuery();
            while (rs.next()){
                res.add(new RowData(rs.getInt("id"), rs.getString("message"), rs.getInt("likes")));
            }
            rs.close();
            return res;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all data for a specific row, by ID
     * 
     * @param id The id of the row being requested
     * 
     * @return The data for the requested row, or null if the ID was invalid
     */
    RowData selectOne(int id, String table){
        RowData res = null;
        try {
            mSelectOne.setInt(2, id);
            mSelectOne.setString(1, table);
            ResultSet rs = mSelectOne.executeQuery();
            if(rs.next()){
                res = new RowData(rs.getInt("id"), rs.getString("message"), rs.getInt("likes"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Delete a row by ID
     * 
     * @param id The id of the row to delete
     * 
     * @return The number of rows that were deleted.  -1 indicates an error.
     */
    int deleteRow(int id, String table){
        int res = -1;
        try {
            mDeleteOne.setInt(2, id);
            mDeleteOne.setString(1, table);

            res = mDeleteOne.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return res;
    }

    // /**
    //  * Update the message for a row in the database
    //  * 
    //  * @param id The id of the row to update
    //  * @param message The new message contents
    //  * 
    //  * @return The number of rows that were updated.  -1 indicates an error.
    //  */
    // int updateOne(int id, String message){
    //     int res = -1;

    //     if(testMessage(message) == false){
    //         return res;
    //     }
    //     try {
    //         mUpdateOne.setString(1, message);
    //         mUpdateOne.setInt(2, id);
    //         res = mUpdateOne.executeUpdate();
    //     } catch (SQLException e){
    //         e.printStackTrace();
    //     }
    //     return res;
    // }

    void updateNameUserTable (String username) {
        try {
            mUserTableUpdateName.setString(1, username);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    void updateBioUserTable (String bio, String user) {
        try {
            mUserTableUpdateBio.setString(1, bio);
            mUserTableUpdateBio.setString(2, user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //======================================================================  
    void updateContentCommentsTable(String content, String userID) {
        try {
            mCommentTableUpdateContent.setString(1, content);
            mCommentTableUpdateContent.setString(2, userID);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    void updateMsgIDCommentsTable(int msgID, int id) {
        try {
            mCommentTableUpdateMsgID.setInt(1, msgID);
            mCommentTableUpdateMsgID.setInt(2, id);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    void updateUserIDCommentsTable(String userID, int id) {
        try {
            mCommentTableUpdateUserID.setString(1, userID);
            mCommentTableUpdateUserID.setInt(2, id);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    //======================================================================  

    void updateMsgIDLikesTable(int msgID, int id) {
        try {
            mLikesTableUpdateMsgID.setInt(1, msgID);
            mLikesTableUpdateMsgID.setInt(2, id);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    void updateUserIDLikesTable(String userID, int id) {
        try {
            mLikesTableUpdateUserID.setString(1, userID);
            mLikesTableUpdateUserID.setInt(2, id);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    void updateStatusLikesTable(int status, int id) {
        try {
            mLikesTableUpdateStatus.setInt(1, status);
            mLikesTableUpdateStatus.setInt(2, id);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    //======================================================================
    void updateContentMessageTable(String content, int id){ 
        try {
            mMessageTableUpdateContent.setString(1, content);
            mMessageTableUpdateContent.setInt(2, id);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }


    void updateUserIDMessageTable(String userID, int id){ 
        try {
            mMessageTableUpdateUserID.setString(1, userID);
            mMessageTableUpdateUserID.setInt(2, id);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increments the like value of a row
     * @param id: the id of the message
     */
    int createTable(){
        try {
            mCreateTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    int createMsgTable() {
        try {
            mMessageTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    int createLikesTable() {
        try {
            mLikesTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    int createCommentsTable() {
        try {
            mCommentTable.execute();
            return 1;
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    int createUserTable() {
        try {
            mUserTable.execute();
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
            mDropTable.execute();
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
            mIncrementLikes.setInt(1, id);
            mIncrementLikes.execute();
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
            mDecrementLikes.setInt(1, id);
            mDecrementLikes.execute();
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
    public boolean testMessage(String message){
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
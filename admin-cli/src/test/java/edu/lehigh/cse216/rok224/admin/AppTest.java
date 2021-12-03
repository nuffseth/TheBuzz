package edu.lehigh.cse216.rok224.admin;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import java.util.Map;
/**
 * Unit test for simple App.
 */

import com.google.api.services.docs.v1.model.SuggestedTableCellStyle;
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite(AppTest.class);
    }

    /**
     * Tests to see if the proper error will happen if you make a message with no characters
     */
    // public void testInvalidMessage(){
    //     try {
    //         Map<String, String> env = System.getenv();
    //         Database db = Database.getDatabase(env.get("DATABASE_URL"));
    //         assertEquals(-1, db.insertRow("", 0));
    //         assertEquals(-1, db.updateOne(0, ""));
    //     } catch(Exception e){
    //         System.out.println(e.getMessage());
    //         assertFalse(true);
    //     }
       
    // }

    /**
     * Tests to see if only one table can be made
     */
    // public void testTableCreation(){
    //     try {
    //         Map<String, String> env = System.getenv();
    //         Database db = Database.getDatabase(env.get("DATABASE_URL"), null);
    //         assertEquals(-1, db.createTable());

    //     } catch(Exception e){
    //         System.out.println(e.getMessage());
    //         // assertFalse(true);
    //     }
    // }

    public void testFileID() {
        String validID = "1OgMeG1K1jV2Mkfha-AximB823SSTS66h";
        String invalidID = "gosh buckley is so cool";

        assert(Database.validFileID(validID) == true);
        assert(Database.validFileID(invalidID) == false);
    }
    
    // focus on errors
    // test if msg id is valid when trying to add a flag
    // invalid id (-1) should return null
    public void testInsertMessageFlag() {
        int invalidMsgID = -1;
        assert(Database.psCheckMsgIDFlag(invalidMsgID) == null);
    }

    // insert 2 messages, one with flag
    // assert that selectMessageFlag for flagged message is not null, the other is null
    public void testSelectMessageFlag() {
        String msg1 = "test msg 1";
        String msg2 = "test msg 2";
        String user1 = "testUser1";
        String user2 = "testUser2";
        int msgid1 = 10;
        int msgid2 = 11;

        // Database.insertMessage(user1, msg1, 0, 0);
        // Database.insertMessage(user2, msg2, 0, 0);
        
        // Database.insertMessageFlag(user1, msgid1);

        // assert msg1 is flagged, msg2 is not
        // assert(Database.selectMessageFlag(user1, msgid1) != null);
        // assert(Database.selectMessageFlag(user2, msgid2) == null);
        
    }

    // create a user then block them
    // assert that selectBlockedUser is not null when checking that user, meaning they were successfully blocked
    public void testAddBlockedUser() {
        
        String user = "user1";
        String bio = "bio1";
        String blocker  = "blocker";

        // Database.insertUser(user, bio);
        // Database.insertUser(blocker, bio);
        // Database.addBlockedUser(user, blocker);
        // assert(Database.selectBlockedUser(user) != null);
    }


}

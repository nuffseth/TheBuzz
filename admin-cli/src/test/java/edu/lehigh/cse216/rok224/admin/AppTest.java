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

    // create new message, insert a flag on the message, assert that the number of flags on message == 1
    public void testInsertMessageFlag() {
        String userID = "lindsey";
        String content = "test content";
        // int msgLink = null;
        // int cmtLink = null;
        int msgID = 1;

        //Database.insertMessage(userID, content, msgLink, cmtLink);
        //Database.insertMessageFlag(userID, msgID);

    }
        

}

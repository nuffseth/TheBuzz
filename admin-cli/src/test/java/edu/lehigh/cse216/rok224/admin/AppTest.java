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
    public void testTableCreation(){
        try {
            Map<String, String> env = System.getenv();
            Database db = Database.getDatabase(env.get("DATABASE_URL"));
            assertEquals(-1, db.createTable());

        } catch(Exception e){
            System.out.println(e.getMessage());
            // assertFalse(true);
        }
    }
}

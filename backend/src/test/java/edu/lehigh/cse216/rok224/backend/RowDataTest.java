package edu.lehigh.cse216.rok224.backend;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class RowDataTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public RowDataTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(RowDataTest.class);
    }

    /**
     * Ensure that the constructor populates every field of the object it
     * creates
     */
    public void testConstructor() {
        String content = "Test Content";
        int id = 17;
        int likes = 2;
        Database.RowData d = new Database.RowData(id, content, likes);

        assertTrue(d.mMessage.equals(content));
        assertTrue(d.mId == id);
        assertTrue(d.mLikes == likes);
    }

    /**
     * Ensure that the copy constructor works correctly
     */
    /*public void testCopyconstructor() {
        String title = "Test Title For Copy";
        String content = "Test Content For Copy";
        int id = 177;
        RowData d = new RowData(id, title, content);
        RowData d2 = new RowData(d);
        assertTrue(d2.mTitle.equals(d.mTitle));
        assertTrue(d2.mContent.equals(d.mContent));
        assertTrue(d2.mId == d.mId);
        assertTrue(d2.mCreated.equals(d.mCreated));
    }*/
}
package edu.lehigh.cse216.rok224.backend;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.google.gson.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    // test that SimpleRequest deserializes json to gson
    public void testSimpleRequest() {
        Gson gson = new Gson();
        SimpleRequest request = gson.fromJson("{\"test\":0}", SimpleRequest.class);
        assertTrue(request instanceof SimpleRequest);
    }

    // test that StructuredResponse serializes 
    public void testStructuredResponse() {
        Gson gson = new Gson();
        String response = gson.toJson(new StructuredResponse("ok", null, null));
        assertTrue(response.equals("{\"mStatus\":\"ok\"}"));
    }
}

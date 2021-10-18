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
    // by asserting that request is of type SimpleRequest, the JSON must have been deserialized to gson 
    public void testSimpleRequest() {
        Gson gson = new Gson();
        SimpleRequest request = gson.fromJson("{\"test\":0}", SimpleRequest.class);
        assertTrue(request instanceof SimpleRequest);
    }

    // test that StructuredResponse serializes 
    // by asserting that the response string was in JSON format, the gson must have been serialized to JSON
    public void testStructuredResponse() {
        Gson gson = new Gson();
        String response = gson.toJson(new StructuredResponse("ok", null, null));
        System.out.println(response);
        assertTrue(response.equals("{\"mStatus\":\"ok\"}"));
    }

    // isolate hash table checking and check for errors in that function

    // isolate oauth token validation, make sure feeding in bogus token results in error
}

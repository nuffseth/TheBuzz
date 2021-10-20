package edu.lehigh.cse216.rok224.backend;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.google.gson.*;
import java.util.UUID;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import java.util.Collections;
import java.util.Map;

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
    public void testHashTable() {
        String valid_user = "valid123";
        String valid_key = UUID.randomUUID().toString();
        String invalid_user = "invalid456";
        String invalid_key = UUID.randomUUID().toString();

        // add valid user and valid key to the hash table
        App.hash_map.put(valid_key, valid_user);

        // try to authenticate invalid user and key combination
        boolean result = App.authenticate(invalid_key, invalid_user);
        assertTrue(result == false); // invalid key/user combo should return false

        result = App.authenticate(valid_key, valid_user);
        assertTrue(result == true); // valid key/user combo should return true
    }

    // isolate oauth token validation, make sure feeding in bogus token results in error
    public void testOAuthVerify() {
        // get environment variables
        Map<String, String> env = System.getenv();
        String client_id = env.get("CLIENT_ID");

        // set up the verifier (from Google OAuth API) to use to verify the id token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(App.transport, App.jsonFactory)
        // Specify the CLIENT_ID of the app that accesses the backend:
        .setAudience(Collections.singletonList(client_id)) //client_id from env
        // Or, if multiple clients access the backend:
        //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
        .build();

        // use our verifier to test bogus id_token, should return null
        String result = App.verifyIdToken(verifier, "adyn-is-$uperCool");
        assertTrue(result == null);
    }
}

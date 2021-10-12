package edu.lehigh.cse216.rok224.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import spark.Spark;
import java.util.*;
import java.security.SecureRandom;

// Import Google's JSON library and Oauth
import com.google.gson.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

/**
 * For now, our app creates an HTTP server that can only get and add data.
 */
public class App {

    private static final JsonFactory jsonFactory = new GsonFactory();
    private static final HttpTransport transport = new NetHttpTransport();

    /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     * @envar      The name of the environment variable to get.
     * @defaultVal The integer value to use as the default if envar isn't found
     * @returns The best answer we could come up with for a value for envar
     */
    static int getIntFromEnv(String envar, int defaultVal) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get(envar) != null) {
            return Integer.parseInt(processBuilder.environment().get(envar));
        }
        return defaultVal;
    }

    public static void main(String[] args) {

        // gson provides us with a way to turn JSON into objects, and objects into JSON.
        // must be final, so that it can be accessed from our lambdas
        final Gson gson = new Gson();

        // connect to the Heroku database using environment variables
        Map<String, String> env = System.getenv();
        String url = env.get("DATABASE_URL");
        final Database dataBase = Database.getDatabase(url);

        // store OAuth variables 
        String client_id = env.get("CLIENT_ID");

        // create local hash table for storing temporary session keys
        HashMap<String, SecureRandom> hash_map = new HashMap<String, SecureRandom>();

        // Set up the location for serving static files
        Spark.staticFileLocation("/web");

        String static_location_override = System.getenv("STATIC_LOCATION");
        if (static_location_override == null){
            Spark.staticFileLocation("/web");
        } else {
            Spark.staticFiles.externalLocation(static_location_override);
        }
        
        // Get the port on which to listen for requests
        Spark.port(getIntFromEnv("PORT", 4567));

        // __  __          _____ _   _ 
        // |  \/  |   /\   |_   _| \ | |
        // | \  / |  /  \    | | |  \| |
        // | |\/| | / /\ \   | | | . ` |
        // | |  | |/ ____ \ _| |_| |\  |
        // |_|  |_/_/    \_\_____|_| \_|

        // Set up a route for serving the main page
        Spark.get("/", (req, res) -> {
            res.redirect("/index.html");
            return "";
        });

        // POST to get the OAuth id token from the frontend and authenticate user
        Spark.post("/login", (request, response) -> {
            // This route is where all OAuth authentication occurs. The user's id_token should be sent
            // to this route as a part of the request object, which is a JSON. The field that holds the
            // id token is mMessage. This route then takes that id token, verifies it, and authenticates the user.

            // get request
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            String idTokenString = req.mMessage; // get id token from the frontend

            // set up the verifier (from Google OAuth API) to use to verify the id token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(client_id)) //client_id from env
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();
            
            // verify the id token sent to us from the frontend
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) { 
                // check if id token is null
                System.out.println("Invalid ID token.");
                // return an error
            } 
            // NOTE: might be able to get rid of all of these else if statements, 
            // might be done automatically with verifier.verify ^^^
            // else if ( false ) { 
            //     // verify token signature using Google's publickeys
            // } else if ( false ) {
            //     // make sure token's aud matches our client id
            // } else if ( false ) {
            //     // make sure the iss of the token is equal to accounts.google.com or https://accounts.google.com
            // } else if ( false ) {
            //     // make sure id token has not yet expired (check exp)
            // } else if ( false ) {
            //     // make sure hd claim matches @lehigh.edu
            // } 
            
            Payload payload = idToken.getPayload(); 
            
            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // NOTE: we don't need all of this stuff, need to sort through and figure out what we DO need
        
            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            
            // save email and session key in local hash table
            SecureRandom session_key = new SecureRandom();
            hash_map.put(payload.getEmail(), session_key);

            // check if user is already in Database

            // 

            // // ensure status 200 OK, with a MIME type of JSON
            // response.status(200);
            // response.type("application/json");
            return "";
        });

        // __  __ ______  _____ _____         _____ ______  _____ 
        // |  \/  |  ____|/ ____/ ____|  /\   / ____|  ____|/ ____|
        // | \  / | |__  | (___| (___   /  \ | |  __| |__  | (___  
        // | |\/| |  __|  \___ \\___ \ / /\ \| | |_ |  __|  \___ \ 
        // | |  | | |____ ____) |___) / ____ \ |__| | |____ ____) |
        // |_|  |_|______|_____/_____/_/    \_\_____|______|_____/                                                       

        // GET route that returns a JSON of all messages.  
        Spark.get("/messages", (request, response) -> {
            // All we do is get the data, embed it in a StructuredResponse, turn it into JSON, and 
            // return it.  If there's no data, we return "[]", so there's no need for error handling.

            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            return gson.toJson(new StructuredResponse("ok", null, dataBase.selectAll()));
        });

        // GET route that returns everything for a single message.
        Spark.get("/messages/:id", (request, response) -> {
            // The ":id" suffix in the first parameter to get() becomes 
            // request.params("id"), so that we can get the requested row ID.  If 
            // ":id" isn't a number, Spark will reply with a status 500 Internal
            // Server Error.  Otherwise, we have an integer, and the only possible 
            // error is that it doesn't correspond to a row with data.         

            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            //DataRow data = dataStore.readOne(idx);
            Database.RowData data = dataBase.selectOne(idx);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // POST route for adding a new message.  
        Spark.post("/messages", (request, response) -> {
            // This will read JSON from the body of the request, turn it into a 
            // SimpleRequest object, extract the title and message, insert them, and return the 
            // ID of the newly created row.
        
            // NB: if gson.Json fails, Spark will reply with status 500 Internal 
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message
            //int newId = dataStore.createEntry(req.mTitle, req.mMessage);
            int newId = dataBase.insertRow(req.mMessage, 0);
            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + newId, null));
            }
        });

        // PUT route for updating a message. 
        Spark.put("/messages/:id", (request, response) -> {
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            //DataRow result = dataStore.updateOne(idx, req.mTitle, req.mMessage);
            Database.RowData result = dataBase.selectOne(dataBase.updateOne(idx, req.mMessage));
            if (result == null) {
                return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, result));
            }
        });

        // DELETE route for removing a message from the database.
        Spark.delete("/messages/:id", (request, response) -> {
            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            // NB: we won't concern ourselves too much with the quality of the 
            //     message sent on a successful delete
            //boolean result = dataStore.deleteOne(idx);
            int result = dataBase.deleteRow(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        // _      _____ _  ________  _____ 
        // | |    |_   _| |/ /  ____|/ ____|
        // | |      | | | ' /| |__  | (___  
        // | |      | | |  < |  __|  \___ \ 
        // | |____ _| |_| . \| |____ ____) |
        // |______|_____|_|\_\______|_____/ 

        // POST route for liking a message
        Spark.post("/messages/:id/likes", (request, response) -> {
            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            //call incrementLikes function from Database.java
            int result = dataBase.incrementLikes(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        // POST route for disliking a message
        Spark.post("/messages/:id/dislikes", (request, response) -> {
            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            //call decrementLikes function from Database.java
            int result = dataBase.decrementLikes(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });
    
        // PUT route for updating a like's status
        Spark.put("/messages/:id/likes", (request, response) -> {
            // NOTE: this currently does the same thing as the original implementation of POSTing a like
            // THIS NEEDS UPDATED!!

            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            //call incrementLikes function from Database.java
            int result = dataBase.incrementLikes(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

    //     _____ ____  __  __ __  __ ______ _   _ _______ _____ 
    //     / ____/ __ \|  \/  |  \/  |  ____| \ | |__   __/ ____|
    //    | |   | |  | | \  / | \  / | |__  |  \| |  | | | (___  
    //    | |   | |  | | |\/| | |\/| |  __| | . ` |  | |  \___ \ 
    //    | |___| |__| | |  | | |  | | |____| |\  |  | |  ____) |
    //     \_____\____/|_|  |_|_|  |_|______|_| \_|  |_| |_____/ 

        // GET route that returns all comments for a message with given message id.
        Spark.get("/messages/:id/comments", (request, response) -> {
            // The ":id" suffix in the first parameter to get() becomes 
            // request.params("id"), so that we can get the requested row ID.  If 
            // ":id" isn't a number, Spark will reply with a status 500 Internal
            // Server Error.  Otherwise, we have an integer, and the only possible 
            // error is that it doesn't correspond to a row with data.         

            int idx = Integer.parseInt(request.params("id"));
            Database.RowData data = dataBase.selectOne(idx);

            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            // currently, this code does the same thing as GET '/messages', need to modify
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }


        });
        
        // POST route for adding a new comment to a message
        Spark.post("/messages/:id/comments", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            // NOTE: currently, this just does the same thing as POST '/messages', need to modify

            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message
            //int newId = dataStore.createEntry(req.mTitle, req.mMessage);
            int newId = dataBase.insertRow(req.mMessage, 0);
            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + newId, null));
            }
        });
    
        // PUT route for updating a comment on a message
        Spark.put("/messages/:id/comments'comment_id", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            // NOTE: currently, this just does the same thing as POST '/messages', need to modify

            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message
            //int newId = dataStore.createEntry(req.mTitle, req.mMessage);
            int newId = dataBase.insertRow(req.mMessage, 0);
            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + newId, null));
            }
        });

        // DELETE route for removing a comment from the database.
        Spark.delete("/messages/:id/comments/:comment_id", (request, response) -> {
            // NOTE: need to update this!!! currently just copied from DELETE '/message/:id'

            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            // NB: we won't concern ourselves too much with the quality of the 
            //     message sent on a successful delete
            //boolean result = dataStore.deleteOne(idx);
            int result = dataBase.deleteRow(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });
    }
}
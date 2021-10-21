package edu.lehigh.cse216.rok224.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import spark.Spark;

import java.io.IOException;
import java.util.*;
import java.util.UUID;

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

    protected static final JsonFactory jsonFactory = new GsonFactory(); // protected so able to use in AppTest
    protected static final HttpTransport transport = new NetHttpTransport(); // protected so able to use in AppTest

    // create local hash table for storing temporary session keys and corresponding user email
    // map user email to session key
    protected static HashMap<String, String> hash_map = new HashMap<String, String>();

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

    /**
     * Searches the hash map to see if the provided session key mathes a user that has been added
     * to the hash map. If the user is found, return the user email, if not, return null.
     * @param email         String user email to search for in the provided hash map
     * @param session_key   String key to search for in the provided hash map
     * @param map           Hash map that maps session key to user email
     * @return              true if user/key combo is found in hash map, false otherwise
     */
    protected static boolean authenticate(String email, String session_key) {
        // search the provided hash map for the session_key and make sure it matches the email
        String map_value = hash_map.get(email);
        // make sure the session key sent matches the value on the hash map
        if ( map_value == null ) { // if email not found, return false
            return false;
        }
        if ( map_value.equals(session_key)) { // if user/session_key combo is valid, return true
            return true; 
        }
        return false; // email/session_key pair not found in hash map
    }

    protected static String verifyIdToken(GoogleIdTokenVerifier verifier, String idTokenString) {
        // verify the id token sent to us from the frontend
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
            if (idToken == null) { // check if id token not verified, give error
                return null;
            } 
            Payload payload = idToken.getPayload(); 
            String email = payload.getEmail();
 
            System.out.println("email: " + email);
            return email;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } 
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

        // set up the verifier (from Google OAuth API) to use to verify the id token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            // Specify the CLIENT_ID of the app that accesses the backend:
            .setAudience(Collections.singletonList(client_id)) //client_id from env
            // Or, if multiple clients access the backend:
            //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
            .build();

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

        //
        //// GENERAL ROUTES
        //

        // Set up a route for serving the main page
        Spark.get("/", (req, res) -> {
            res.redirect("/index.html");
            return "";
        });

        // POST to get the OAuth id token from the frontend and authenticate user
        // TO DO: check OAuth verification now that we have OAuth on the frontend
        // TO DO: make sure dataBase.insertRowUser works (admin)
        Spark.post("/login", (request, response) -> {
            System.out.println("entered /login request");
            // This route is where all OAuth authentication occurs. The user's id_token should be sent
            // to this route as a part of the request object, which is a JSON. The field that holds the
            // id token is mMessage. This route then takes that id token, verifies it, and authenticates the user.

            // get request info
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            String idTokenString = req.mMessage; // get id token from the frontend
            System.out.println("idTokenString = " + idTokenString);

            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            
            // verify the id token sent to us from the frontend
            String email = verifyIdToken(verifier, idTokenString);

            // check that email ends in @lehigh.edu
            String[] values = email.split("@", 0); 

            // values[0] should be username (ex: arg422), values[1] should be "lehigh.edu"
            if ( values.length != 2 || !values[1].equals("lehigh.edu") ) {
                response.status(403);
                return gson.toJson(new StructuredResponse("error", "invalid email (must be @lehigh.edu)", null));
            }

            String username = values[0]; // username is first half of email address
            System.out.println("username taken from email: " + username);
            
            // save user and session key in local hash table
            String session_key = UUID.randomUUID().toString(); // make a random string
            System.out.println("random session key: " + session_key);
            hash_map.put(username, session_key);

            // add user to user table, Database.java won't add duplicates
            System.out.println("inserting user into database...");
            dataBase.insertRowUser(username, "");

            // send the session key back to the frontend
            return gson.toJson(new StructuredResponse("ok", null, session_key));
        });

        //
        //// MESSAGE ROUTES
        //

        // GET route that returns a JSON of all messages. 
        // TO DO: make sure dataBase.selectAll() works (admin) 
        Spark.get("/messages", (request, response) -> {
            // All we do is get the data, embed it in a StructuredResponse, turn it into JSON, and 
            // return it.  If there's no data, we return "[]", so there's no need for error handling.

            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            return gson.toJson(new StructuredResponse("ok", null, dataBase.selectAll()));
        });

        // GET route that returns everything for a single message.
        // TO DO: make sure dataBase.selectOne works for a message (admin)
        Spark.get("/messages/:id", (request, response) -> {        
            // get id from URL and find in database
            int idx = Integer.parseInt(request.params("id")); // if id not an int, 500 error

            Database.RowData data = dataBase.selectOne(idx, "message"); // get one message object

            // ensure status 200 OK, with a MIME type of JSON, and return
            response.status(200);
            response.type("application/json");
            if (data == null) { // return an error if id not found
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // POST route for adding a new message. 
        // TO DO: make sure dataBase.insertRowMessage works (needs to add user email, not user id) (admin) 
        Spark.post("/messages", (request, response) -> {
            // This will read JSON from the body of the request, turn it into a 
            // SimpleRequest object, extract the title and message, insert them, and return the 
            // ID of the newly created row.
        
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            
            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // add input message and current user to messages table
            dataBase.insertRowMessages(req.mMessage, req.mEmail); // Database.java handles error checking

            return gson.toJson(new StructuredResponse("ok", "", null));  
        });

        // PUT route for updating a message. 
        // TO DO: make sure dataBase.selectOne works for message (admin)
        // TO DO: make sure dataBase.selectOne returns a RowData that includes userEmail field
        // TO DO: make sure dataBase.updateContentMessageTable does error checking or returns something on error
        Spark.put("/messages/:id", (request, response) -> {
            // get all info from request
            int idx = Integer.parseInt(request.params("id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            
            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // make sure current user matches the one who created the message
            if (dataBase.selectOne(idx, "message").userEmail != req.mEmail) {
                return gson.toJson(new StructuredResponse("error", "user mismatch, row  " + idx, null));
            }

            int result = dataBase.updateContentMessageTable(req.mMessage, idx);

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        // DELETE route for removing a message from the database.
        // TO DO: make sure dataBase.selectOne returns a RowData that includes userEmail field (admin)
        // TO DO: make sure dataBase.deleteRow works for message table (admin)
        Spark.delete("/messages/:id", (request, response) -> {
            // get all info from request
            int idx = Integer.parseInt(request.params("id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // make sure current user matches the one who created the message
            if (dataBase.selectOne(idx, "message").userEmail != req.mEmail) {
                return gson.toJson(new StructuredResponse("error", "user mismatch, row  " + idx, null));
            }

            // // if user matches, delete the message
            int result = dataBase.deleteRow(idx, "message");

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        //
        //// LIKES ROUTES
        //

        // POST route for liking a message
        // TO DO: need a Database.java method to select one like based on user email and message index (admin)
        // TO DO: make sure dataBase.insertRowLikes works (should be based on user email instead of userID) (admin)
        // TO DO: make sure dataBase.insertRowLikes returns -1 on error
        Spark.post("/messages/:id/likes", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // check if like for this user and message already exists 
            if ( dataBase.selectOneLike( req.mEmail, msg_idx )) {
                // since this is a POST, we aren't updating the data, so return an error
                return gson.toJson(new StructuredResponse("error", "message " + msg_idx + " already has like status, try put", null));
            }

            // // create a new like with status 1 for the given message and current user in Likes table
            // int result = dataBase.insertRowLikes( 1, req.mEmail, msg_idx ));
            int result = -1;
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + msg_idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        // POST route for disliking a message
        // TO DO: need a Database.java method to select one like based on user email and message index (admin)
        // TO DO: make sure dataBase.insertRowLikes works (should be based on user email instead of userID) (admin)
        // TO DO: make sure dataBase.insertRowLikes returns -1 on error
        Spark.post("/messages/:id/dislikes", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // check if like for this user and message already exists 
            if ( dataBase.selectOneLike( req.mEmail, msg_idx )) {
                // since this is a POST, we aren't updating the data, so return an error
                return gson.toJson(new StructuredResponse("error", "message " + msg_idx + " already has like status, try put", null));
            }

            // // create a new like with status -1 for the given message and current user in Likes table
            // int result = dataBase.insertRowLikes( -1, req.mEmail, msg_idx ));
            int result = -1;
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + msg_idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });
    
        // PUT route for updating a like's status
        // TO DO: make sure web and flutter know what to send in the JSON about which button was pressed
        // TO DO: need some way to get the status of a particular like, maybe a selectOne update would do it? (admin)
        // TO DO: make sure updateStatusLikesTable works (should return an int) (admin)
        Spark.put("/messages/:id/likes", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // did the user click the 'like' or the 'dislike' button?
            String button = req.mMessage; // button should now be "like" or "dislike" to indicate which button pressed
            int status = 0;
            if (button == "like") {
                status = 1;
            } else if (button == "dislike") {
                status = -1;
            } else { // what did you send me in mMessage lol it doesn't make sense
                return gson.toJson(new StructuredResponse("error", "invalid like status sent", null));
            }

            int result = 0;
            int like_id = getLikeId( req.mEmail, msg_idx );
            // check if like for this user and message doesn't exist yet 
            if ( dataBase.selectOne( like_id, "likes" ) == null) {
                // if the like doesn't already exist, create it with appropriate status
                result = dataBase.insertRowLikes( status, req.mEmail, msg_idx ));
            } else {
                // if like already does exist, update it
                int old_status = dataBase.getLikeStaus(like_id);
                int new_status = 0;
                if (status == 1) { // if like button was clicked
                    if (old_status >= 0) { // if previous status was neutral or like, should result in a like
                        new_status = 1;
                    } else { // if previous status was a dislike, should result in a neutral status
                        new_status = 0;
                    }
                } else { // if dislike button was clicked
                    if (old_status <= 0) { // if previous status was neutral or dislike, should result in a dislike
                        new_status = -1;
                    } else { // if previous status was a like, should result in a neutral status
                        new_status = 0;
                    }
                }
                // update the like row accordingly
                result = dataBase.updateStatusLikesTable(status, like_id);
            }

            // send result
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update like", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

    //
    //// COMMENTS ROUTES
    //

        // GET route that returns all comments for a message with given message id.
        // TO DO: need a way to select all comments with the same message id (admin)
        Spark.get("/messages/:id/comments", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");        

            // // collect all comments with the given message id
            // ArrayList<Database.RowData> data = dataBase.selectAllComments(msg_idx);
            Object data = null;

            if (data == null) {
                return gson.toJson(new StructuredResponse("error", msg_idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });
        
        // POST route for adding a new comment to a message
        // TO DO: make sure dataBase.insertRowComments works (should return an int) (admin)
        Spark.post("/messages/:id/comments", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json"); 
            
            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // add a new comment to the current message with provided content and current user
            int result = dataBase.insertRowComments(req.mMessage, req.mEmail, msg_idx); 

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "", null));
            }
        });
    
        // PUT route for updating a comment on a message
        // TO DO: make sure selectOne works for comments (admin)
        // TO DO: need a way to get the email of user who posted a comment (maybe an update to selectOne) ?
        // TO DO: make sure updateContentCommentsTable works (should take in comment id instead of user id) (admin)
        Spark.put("/messages/:id/comments/comment_id", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails
            int comment_idx = Integer.parseInt(request.params("comment_id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json"); 

            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // make sure comment exists
            if ( dataBase.selectOne(comment_idx, "comments") == null ) {
                return gson.toJson(new StructuredResponse("error", "comment " + comment_idx + " not found", null));
            }
            // make sure current user matches the one who created the comment
            if (dataBase.selectOne(comment_idx, "comments").mEmail != req.mEmail) {
                return gson.toJson(new StructuredResponse("error", "user mismatch, comment id  " + comment_idx, null));
            }

            // // update the comment according to the input message
            int result = dataBase.updateContentCommentsTable(req.mMessage, comment_idx);

            int result = -1;
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "", null));
            }
        });

        // DELETE route for removing a comment from the database.
        // TO DO: make sure selectOne works for comments (admin)
        // TO DO: need a way to get the email of user who posted a comment (maybe an update to selectOne) ?
        // TO DO: make sure dataBase.deleteOne works for comments (admin)
        Spark.delete("/messages/:id/comments/:comment_id", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails
            int comment_idx = Integer.parseInt(request.params("comment_id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json"); 

            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // make sure comment exists
            if ( dataBase.selectOne(comment_idx, "comments") == null ) {
                return gson.toJson(new StructuredResponse("error", "comment " + comment_idx + " not found", null));
            }
            // make sure current user matches the one who created the comment
            if (dataBase.selectOne(comment_idx, "comments").mEmail != req.mEmail) {
                return gson.toJson(new StructuredResponse("error", "user mismatch, comment id  " + comment_idx, null));
            }

            // update the comment according to the input message
            int result = dataBase.deleteRow(comment_idx, "comments");

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "error deleting comment " + comment_idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "", null));
            }
        });

        //
        //// USERS ROUTES
        //

        // GET route that returns a specific user's profile.
        // TO DO: need a route to get all user data for a specific user
        Spark.get("/users/:username", (request, response) -> {  
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails    
            // ensure status 200 OK, with a MIME type of JSON, and return
            response.status(200);
            response.type("application/json");

            if ( !authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // get username from URL and find in database
            String username = request.params("username"); 

            // // make sure username requested matches current user
            if ( !username.equals(req.mEmail) ) {
                return gson.toJson(new StructuredResponse("error", "current user is not " + username, null));
            }
            // // TO DO: UPDATE ONCE IMPLEMENTED BY ADMIN
            Database.RowData data = dataBase.selectOneUser(username); // get the user object

            if (data == null) { // return an error if id not found
                return gson.toJson(new StructuredResponse("error", username + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });
    }
}
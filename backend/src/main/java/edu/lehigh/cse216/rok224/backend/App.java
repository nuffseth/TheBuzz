package edu.lehigh.cse216.rok224.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import spark.Spark;

// commented out IOException import, was unused
// import java.io.IOException;
import java.util.*;
import java.util.UUID;

// Import Google's JSON library and Oauth
import com.google.gson.*;

import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.http.impl.execchain.MainClientExec;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.code.ssm.CacheFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import java.lang.InterruptedException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.apache.commons.io.FileUtils;

import com.google.api.client.http.FileContent;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
/**
 * For now, our app creates an HTTP server that can only get and add data.
 */
public class App {

    protected static final JsonFactory jsonFactory = new GsonFactory(); // protected so able to use in AppTest
    protected static final HttpTransport transport = new NetHttpTransport(); // protected so able to use in AppTest

    // create local hash table for storing temporary session keys and corresponding user email
    // map user email to session key
    //protected static HashMap<String, String> hash_map = new HashMap<String, String>();
    static MemcachedClient mc;

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
        String map_value;
        try {
            map_value = mc.get(email);
            // make sure the session key sent matches the value on the hash map
            if ( map_value == null ) { // if email not found, return false
                return false;
            }
            if ( map_value.equals(session_key)) { // if user/session_key combo is valid, return true
                return true; 
            }
        } catch (TimeoutException te) {
            System.err.println("Timeout during set or get: " +
                            te.getMessage());
        } catch (InterruptedException ie) {
            System.err.println("Interrupt during set or get: " +
                            ie.getMessage());
        } catch (MemcachedException me) {
            System.err.println("Memcached error during get or set: " +
                            me.getMessage());
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
    
    /**
     * Google Drive API Setup Variables
     */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

     /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static HttpRequestInitializer getCredentials() throws IOException {
        // Load client secrets.
        System.out.println(CREDENTIALS_FILE_PATH);
        InputStream in = App.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        // GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(ServiceAccountCredentials.fromStream(in)
            .createScoped(Collections.singletonList(DriveScopes.DRIVE)));
         return requestInitializer;
        // TODO: THIS IS WHAT IS CAUSING THE DATABASE CREATION TO FAIL
        // Build flow and trigger user authorization request.
        // GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        //         HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        //         .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        //         .setAccessType("offline")
        //         .build();
        // LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        // return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) {
        // gson provides us with a way to turn JSON into objects, and objects into JSON.
        // must be final, so that it can be accessed from our lambdas
        final Gson gson = new Gson();

        // connect to the Heroku database using environment variables
        Map<String, String> env = System.getenv();
        String url = env.get("DATABASE_URL");

        // NOTE: admin's Database.java was incomplete, so I added to it to create MyDatabase.java
        // MyDatabase.java is the same as admin's Database.java, but with additional empty functions that 
        // needed to be implemented. I created them as empty functions so the backend code compiles.
        // final MyDatabase dataBase = MyDatabase.getDatabase(url);

        // Memcache Setup
        List<InetSocketAddress> servers =
            AddrUtil.getAddresses(env.get("MEMCACHIER_SERVERS").replace(",", " "));
        AuthInfo authInfo =
            AuthInfo.plain(env.get("MEMCACHIER_USERNAME"),
                           env.get("MEMCACHIER_PASSWORD"));
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(servers);

        // Configure SASL auth for each server
        for(InetSocketAddress server : servers) {
            builder.addAuthInfo(server, authInfo);
        }

        // Use binary protocol
        builder.setCommandFactory(new BinaryCommandFactory());
        // Connection timeout in milliseconds (default: )
        builder.setConnectTimeout(1000);
        // Reconnect to servers (default: true)
        builder.setEnableHealSession(true);
        // Delay until reconnect attempt in milliseconds (default: 2000)
        builder.setHealSessionInterval(2000);

        try {
            MemcachedClient mc = builder.build();
            try {
                mc.set("foo", 0, "bar");
                String val = mc.get("foo");
                System.out.println(val);
            } catch (TimeoutException te) {
                System.err.println("Timeout during set or get: " +
                                te.getMessage());
            } catch (InterruptedException ie) {
                System.err.println("Interrupt during set or get: " +
                                ie.getMessage());
            } catch (MemcachedException me) {
                System.err.println("Memcached error during get or set: " +
                                me.getMessage());
            }
        } catch (IOException ioe) {
        System.err.println("Couldn't create a connection to MemCachier: " +
                            ioe.getMessage());
        }

        // store OAuth variables 
        String client_id = env.get("CLIENT_ID");
        // set up the verifier (from Google OAuth API) to use to verify the id token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            // Specify the CLIENT_ID of the app that accesses the backend:
            .setAudience(Collections.singletonList(client_id)) //client_id from env
            // Or, if multiple clients access the backend:
            //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
            .build();

        Drive service = null;
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            //Print the names and IDs for up to 10 files.
            FileList result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }
        } catch (Exception e) {
            System.out.println("Unable to connect to Google Drive, file uploads/downloads won't work");
            e.printStackTrace();
        }

        // uncomment this and delete MyDatabase.java once Database.java is implemented
        final Database dataBase = Database.getDatabase(url, service);
        
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
            mc.set(username, 3600, session_key);

            // add user to user table, Database.java shouldn't add duplicates
            System.out.println("inserting user into database...");
            int result = dataBase.insertUser(username, "");

            // send the session key back to the frontend
            if (result == -1) { // return an error if unable to add user
                return gson.toJson(new StructuredResponse("error", "authenticated, unable to add to database", session_key));
            }
            else if(result == 0){ // return user already exists in database
                return gson.toJson(new StructuredResponse("ok", "user already exists in database", session_key));
            } 
            else {
                return gson.toJson(new StructuredResponse("ok", null, session_key));
            }
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

            ArrayList<Database.Message> data = dataBase.selectAllMessages();

            if (data == null) { // return an error if id not found
                return gson.toJson(new StructuredResponse("error", "unable to select all messeges from database", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // GET route that returns everything for a single message.
        // TO DO: make sure dataBase.selectOne works for a message (admin)
        Spark.get("/messages/:id", (request, response) -> {        
            // get id from URL and find in database
            int idx = Integer.parseInt(request.params("id")); // if id not an int, 500 error


            Database.Message data = dataBase.selectMessage(idx); // get one message object
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
            int result = dataBase.insertMessage(req.mMessage, req.mEmail, req.messageLink, req.commentLink);
            

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to add message to database", null));
            } else {
                // inserts file
                if (req.mFiles != null ){
                    java.io.File newFile = new java.io.File(req.mFiles.fileName);
                    // checks if file is in memcachier
                    if(mc.get(req.mFiles.fileName) == null){
                        mc.set(req.mFiles.fileName, 3600, req.mFiles);
                    }
                    dataBase.insertMsgFile(result, newFile);
                }
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
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
            if (dataBase.selectMessage(idx).mUserID != req.mEmail) {
                return gson.toJson(new StructuredResponse("error", "user mismatch, row  " + idx, null));
            }

            int result = dataBase.updateMessage(idx, req.mMessage);


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

            // make sure message exists
            if (dataBase.selectMessage(idx) == null) {
                return gson.toJson(new StructuredResponse("error", "unable to select message " + idx, null));
            }
            // make sure current user matches the one who created the message
            if (dataBase.selectMessage(idx).mUserID != req.mEmail) {
                return gson.toJson(new StructuredResponse("error", "user mismatch, row  " + idx, null));
            }

            // if user matches, delete the message
            int result = dataBase.deleteMessage(idx);

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                //Deletes files associated with message.
                //dataBase.deleteMsgFile(getMsgFileID());
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });



        //
        //// Flag routes
        //

        Spark.get("/messages/:id/flags", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if(dataBase.selectMessageFlag(req.mEmail, msg_idx) == null){
                return gson.toJson(new StructuredResponse("ok", null, false));
            } else { //If the message is already flagged, remove the flag.
                return gson.toJson(new StructuredResponse("ok", null, true));
            }
        });

        Spark.get("/messages/:id/comments/:comment_id/flags", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if(dataBase.selectMessageFlag(req.mEmail, msg_idx) == null){
                return gson.toJson(new StructuredResponse("ok", null, false));
            } else { //If the message is already flagged, remove the flag.
                return gson.toJson(new StructuredResponse("ok", null, true));
            }
        });

        Spark.put("/messages/:id/flags", (request, response) -> {
            int msg_idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if(!authenticate(req.mEmail, req.mSessionKey)){ // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            int result = 0;
            
            if(dataBase.selectMessageFlag(req.mEmail, msg_idx) == null){
                result = dataBase.insertMessageFlag(req.mEmail, msg_idx);
            } else { //If the message is already flagged, remove the flag.
                result = dataBase.deleteMessageFlag(msg_idx);
            }

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to insert/update flag", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        Spark.put("messages/:id/comments/:comment_id/flags", (request, response) -> {
            int comment_idx = Integer.parseInt(request.params("comment_id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if(!authenticate(req.mEmail, req.mSessionKey)){ // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            if(dataBase.selectComment(comment_idx) == null){
                return gson.toJson(new StructuredResponse("error", "comment " + comment_idx + " not found", null));
            }

            int result = 0;
            
            if(dataBase.selectCommentFlag(req.mEmail, comment_idx) == null){
                result = dataBase.insertCommentFlag(req.mEmail, comment_idx);
            } else { //If the message is already flagged, remove the flag.
                result = dataBase.deleteCommentFlag(comment_idx);
            }

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to insert/update flag", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        //
        //// Blocked User routes
        //
        Spark.put("/users/:id/block", (request, response) -> {
            String user_idx = request.params("id");
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if(!authenticate(req.mEmail, req.mSessionKey)){ // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            if(dataBase.selectUser(req.mEmail) == null){
                return gson.toJson(new StructuredResponse("error", "user " + user_idx + " not found", null));
            }

            int result = 0;
            
            if(dataBase.selectBlockedUser(req.mEmail) == null){
                result = dataBase.addBlockedUser(user_idx, req.mEmail);
            } else { //If the user is already blocked unblock them.
                result = dataBase.deleteBlockedUser(req.mEmail);
            }

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to insert/update block", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        Spark.get("users/:id/block", (request, response) -> {
            String user_idx = request.params("id");
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);

            response.status(200);
            response.type("application/json");

            if(!authenticate(req.mEmail, req.mSessionKey)){ // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            if(dataBase.selectUser(req.mEmail) == null){
                return gson.toJson(new StructuredResponse("error", "user " + user_idx + " not found", null));
            }

            if(dataBase.selectBlockedUser(req.mEmail) == null){
                return gson.toJson(new StructuredResponse("ok", null, null));
            } else { //If the user is already blocked unblock them.
                return gson.toJson(new StructuredResponse("ok", null, dataBase.selectBlockedUser(user_idx)));
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
            if ( dataBase.selectLike( msg_idx, req.mEmail ) != null) {
                // since this is a POST, we aren't updating the data, so return an error
                return gson.toJson(new StructuredResponse("error", "message " + msg_idx + " already has like status, try put", null));
            }

            // create a new like with status 1 for the given message and current user in Likes table
            int result = dataBase.insertLike( msg_idx, req.mEmail, 1 );

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to add like", null));
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
            if ( dataBase.selectLike( msg_idx, req.mEmail ) != null) {
                // since this is a POST, we aren't updating the data, so return an error
                return gson.toJson(new StructuredResponse("error", "message " + msg_idx + " already has like status, try put", null));
            }

            // create a new like with status -1 for the given message and current user in Likes table
            int result = dataBase.insertLike( msg_idx, req.mEmail, -1 );

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to add dislike", null));
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
            // check if like for this user and message doesn't exist yet 
            if ( dataBase.selectLike( msg_idx, req.mEmail ) == null) {
                // if the like doesn't already exist, create it with appropriate status
                result = dataBase.insertLike( msg_idx, req.mEmail, status );
            } else {
                // if like already does exist, update it
                int old_status = dataBase.selectLike( msg_idx, req.mEmail ).mStatus;
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
                result = dataBase.updateLike(msg_idx, req.mEmail, new_status);
            }

            // send result
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to insert/update like", null));
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
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");      
            
            // check to make sure message with given id exists
            System.out.println("trying to fetch message " + msg_idx);
            Database.Message message = dataBase.selectMessage(msg_idx);
            if (message == null) {
                return gson.toJson(new StructuredResponse("error", "unable to select message " + msg_idx, null));
            }

            System.out.println("Message successfully obtained:");
            System.out.println(message.mComments);

            // collect all comments with the given message id
            ArrayList<Database.Comment> data = dataBase.getComments(msg_idx);
            System.out.println("data from database:");
            System.out.println(data);

            if (data == null) {
                return gson.toJson(new StructuredResponse("error", "unable to find all comments for message " + msg_idx, null));
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
            int result = dataBase.insertComment(msg_idx, req.mEmail, req.mMessage, req.messageLink, req.commentLink); 

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                java.io.File newFile = new java.io.File(req.mFiles.fileName);
                // checks if file is in memcachier
                if(mc.get(req.mFiles.fileName) == null){
                    mc.set(req.mFiles.fileName, 3600, req.mFiles);
                }
                dataBase.insertCmtFile(result, newFile);
                return gson.toJson(new StructuredResponse("ok", "", null));
            }
        });
    
        // PUT route for updating a comment on a message
        // TO DO: make sure selectOne works for comments (admin)
        // TO DO: need a way to get the email of user who posted a comment (maybe an update to selectOne) ?
        // TO DO: make sure updateContentCommentsTable works (should take in comment id instead of user id) (admin)
        Spark.put("/messages/:id/comments/:comment_id", (request, response) -> {
            // get all info from request
            int msg_idx = Integer.parseInt(request.params("id")); // 500 error if fails
            int comment_idx = Integer.parseInt(request.params("comment_id")); // 500 error if fails
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class); // 500 error if fails
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json"); 

            if(!authenticate(req.mEmail, req.mSessionKey)){ // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // make sure comment exists
            if(dataBase.selectComment(comment_idx) == null ){
                return gson.toJson(new StructuredResponse("error", "comment " + comment_idx + " not found", null));
            }
            // make sure current user matches the one who created the comment
            if(dataBase.selectComment(comment_idx).mUserID != req.mEmail){
                return gson.toJson(new StructuredResponse("error", "user mismatch, comment id  " + comment_idx, null));
            }

            // update the comment according to the input message
            int result = dataBase.updateComment(comment_idx, req.mMessage);

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

            if (!authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // // make sure comment exists
            if ( dataBase.selectComment(comment_idx) == null ) {
                return gson.toJson(new StructuredResponse("error", "comment " + comment_idx + " not found", null));
            }
            // make sure current user matches the one who created the comment
            if (dataBase.selectComment(comment_idx).mUserID != req.mEmail) {
                return gson.toJson(new StructuredResponse("error", "user mismatch, comment id  " + comment_idx, null));
            }

            // update the comment according to the input message
            int result = dataBase.deleteComment(comment_idx);

            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "error deleting comment " + comment_idx, null));
            } else {
                // delete file associated with comment
                // dataBase.deleteCmtFile(fileID);
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

            if (!authenticate(req.mEmail, req.mSessionKey)) { // error if session key not found (not logged in)
                return gson.toJson(new StructuredResponse("error", "invalid session key/user combination", null));
            }

            // get username from URL and find in database
            String username = request.params("username"); 

            // // make sure username requested matches current user
            if (!username.equals(req.mEmail) ) {
                return gson.toJson(new StructuredResponse("error", "current user is not " + username, null));
            }

            Database.User data = dataBase.selectUser(username); // get the user object

            if (data == null) { // return an error if id not found
                return gson.toJson(new StructuredResponse("error", username + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });
        // GET route for downloading files
        Spark.get("/messages/:id/File", (request, response) -> {
            // get id from URL and find in database
            int idx = Integer.parseInt(request.params("id")); // if id not an int, 500 error

            //ArrayList<DataBase.MyFile> fileInfo = dataBase.getMsgFiles(idx); // get file
            //byte[] fileData = dataBase.downloadFile(fileInfo.get(0).mFileID);
            //byte[] encoded = Base64.getEncoder().encode(fileData);

            ArrayList<Database.MyFile> data = dataBase.getMsgFiles(idx);
            // ensure status 200 OK, with a MIME type of JSON, and return
            response.status(200);
            response.type("application/json");
            if (data == null) { // return an error if id not found
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        //
        // ISEVEN ROUTES
        //
        
        // GET route that returns the ad and whether or not a number is even
        Spark.get("https://api.isevenapi.xyz/api/iseven/:number", (request, response) -> {
            // get number from URL and find in database
            int number = Integer.parseInt(request.params("number")); // if id not an int, 500 error

            // ensure status 200 OK, with a MIME type of JSON, and return
            response.status(200);
            response.type("application/json");
            return gson.toJson(new StructuredResponse("ok", null, null));//Placeholder
        });

        //
        // B
    }
}
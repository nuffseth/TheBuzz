package edu.lehigh.cse216.rok224.admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Map;

// all imports for Google Drive connection
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.Collections;
import java.util.List;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

/**
 * App is our basic admin app.  For now, it is a demonstration of the six key 
 * operations on a database: connect, insert, update, query, delete, disconnect
 */
public class App {

    /**
     * Set up connection to the Google Service Account Drive for file upload
     */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

     /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        System.out.println(CREDENTIALS_FILE_PATH);
        InputStream in = App.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // TODO: THIS IS WHAT IS CAUSING THE DATABASE CREATION TO FAIL
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        System.out.println("made flow");
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        System.out.println("made receiver");
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Print the menu for our program
     */
    static void menu() {
        System.out.println("Main Menu");
        System.out.println("  [T] Create tblData");
        System.out.println("  [D] Drop tblData");
        System.out.println("  [1] Query for a specific row");
        System.out.println("  [*] Query for all rows");
        System.out.println("  [-] Delete a row");
        System.out.println("  [+] Insert a new row");
        System.out.println("  [~] Update a row");
        System.out.println("  [q] Quit Program");
        System.out.println("  [?] Help (this message)");
        System.out.println("  [i] Increment likes for a specific row");
        System.out.println("  [d] Decrement likes for a specific row");

        System.out.println("  [L] Create likes table");
        System.out.println("  [M] Create messages table");
        System.out.println("  [C] Create comments table");
        System.out.println("  [U] Create user table");
    }

    /**
     * Ask the user to enter a menu option; repeat until we get a valid option
     * 
     * @param in A BufferedReader, for reading from the keyboard
     * 
     * @return The character corresponding to the chosen menu option
     */
    static char prompt(BufferedReader in) {
        // The valid actions:
        String actions = "TD1*-+~q?id";

        // We repeat until a valid single-character option is selected        
        while (true) {
            System.out.print("[" + actions + "] :> ");
            String action;
            try {
                action = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (action.length() != 1)
                continue;
            if (actions.contains(action)) {
                return action.charAt(0);
            }
            System.out.println("Invalid Command");
        }
    }

    /**
     * Ask the user to enter a String message
     * 
     * @param in A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     * 
     * @return The string that the user provided.  May be "".
     */
    static String getString(BufferedReader in, String message){
        String s;
        try {
            System.out.print(message + " :> ");
            s = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return s;
    }

    /**
     * Ask the user to enter an integer
     * 
     * @param in A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     * 
     * @return The integer that the user provided.  On error, it will be -1
     */
    static int getInt(BufferedReader in, String message){
        int i = -1;
        try {
            System.out.print(message + " :> ");
            i = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
        return i;
    }

    /**
     * The main routine runs a loop that gets a request from the user and
     * processes it
     * 
     * @param argv Command-line options.  Ignored by this program.
     */
    public static void main(String[] argv){
        // get the database url from the environment
        Map<String, String> env = System.getenv();
        String db_url = env.get("DATABASE_URL");

        // get the google service connection
        // Build a new authorized API client service.
        Drive service = null;
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // // Print the names and IDs for up to 10 files.
            // FileList result = service.files().list()
            //         .setPageSize(10)
            //         .setFields("nextPageToken, files(id, name)")
            //         .execute();
            // List<File> files = result.getFiles();
            // if (files == null || files.isEmpty()) {
            //     System.out.println("No files found.");
            // } else {
            //     System.out.println("Files:");
            //     for (File file : files) {
            //         System.out.printf("%s (%s)\n", file.getName(), file.getId());
            //     }
            // }

        } catch (Exception e) {
            System.out.println("Unable to create database");
            System.out.println(e.toString());
        }

        // create the database with the google drive service
        Database db = Database.getDatabase(db_url, service);
        if (db == null)
            return;

        // Start our basic command-line interpreter:
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            // Get the user's request, and do it
            //
            // NB: for better testability, each action should be a separate
            //     function call
            char action = prompt(in);
            // help menu
            if(action == '?'){
                menu();
            } else if (action == 'q'){                      // quit --------------------------------------
                break;
            } else if (action == '1'){                      // query specific row --------------------------------------
                int id = getInt(in, "Enter the message ID");
                if(id == -1){
                    continue;
                } 
                Database.Message res = db.selectMessage(id);
                if(res != null) {
                    System.out.println(res);
                }
            } else if(action == '*') {                      // query for all rows --------------------------------------
                ArrayList<Database.Message> res = db.selectAllMessages();
                if (res == null){
                    continue;
                }
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                System.out.println(res);
            } else if(action == '-') {                      // delete a row --------------------------------------
                int id = getInt(in, "Enter the row ID");
                if (id == -1){
                    continue;
                }
                String table = getString(in, "Enter table name");
                if ( !(table.equals("user") || table.equals("comment") || table.equals("likes") || table.equals("message") )) {
                    continue;
                }

                int res = db.deleteMessage(id);
                if(res == -1){
                    continue;
                }
                System.out.println("  " + res + " rows deleted");
            } else if(action == '+'){                       // insert a row --------------------------------------
                String message = getString(in, "Enter the message");
                if(message == null){
                    System.out.println("Can't make an empty message");
                    continue;
                }
                System.out.println("unimplemented");
            } else if(action == '~'){                       // update a row --------------------------------------
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1){
                    continue;
                }
                String newMessage = getString(in, "Enter the new message");
                int res = db.updateMessage(id, newMessage);
                if (res == -1){
                    continue;
                }
                System.out.println("  " + res + " rows updated");
            } else if (action == 'i'){                      // increment likes for a specific row --------------------------------------
                int id = getInt(in, "Enter the row ID :> ");
                if(id == -1){
                    continue;
                }
                db.incrementLikes(id);
            } else if (action == 'd'){                      // decrement likes for a specific row --------------------------------------
                int id = getInt(in, "Enter the row ID :> ");
                if(id == -1){
                    continue;
                }
                int res = db.decrementLikes(id);
                if(res == -1){
                    continue;
                }
                System.out.println("Row has been updated");
            } else if (action == 'L') {                     // create likes table --------------------------------------
                int res = db.createLikesTable();
                if(res == -1){
                    continue;
                }
                System.out.println("Likes table has been created");
            } else if (action == 'M') {                     // create message table --------------------------------------
                int res = db.createMsgTable();
                if(res == -1){
                    continue;
                }
                System.out.println("Message table has been created");
            } else if (action == 'C') {                     // create comments table
                int res = db.createCommentsTable();
                if(res == -1){
                    continue;
                }
                System.out.println("Comments table has been created");
            } else if (action == 'U') {                     // create user table
                int res = db.createUserTable();
                if(res == -1){
                    continue;
                }
                System.out.println("User table has been created");
            }
        }
        // Always remember to disconnect from the database when the program 
        // exits
        db.disconnect();
    }
}
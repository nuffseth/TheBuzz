package edu.lehigh.cse216.rok224.admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Map;

/**
 * App is our basic admin app.  For now, it is a demonstration of the six key 
 * operations on a database: connect, insert, update, query, delete, disconnect
 */
public class App {

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
        // get the Postgres configuration from the environment
        Map<String, String> env = System.getenv();

        String db_url = env.get("DATABASE_URL");
        // Get a fully-configured connection to the database, or exit 
        // immediately
        Database db = Database.getDatabase(db_url);
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
            if(action == '?'){
                menu();
            } else if (action == 'q'){
                break;
            } else if (action == 'T'){
                int res = db.createTable();
                if(res == -1){
                    continue;
                }
                System.out.println("Table has been created");
            } else if (action == 'D'){
                int res = db.dropTable();
                if(res == -1){
                    continue;
                }
                System.out.println("Table has been dropped");
            } else if (action == '1'){
                int id = getInt(in, "Enter the row ID");
                if(id == -1){
                    continue;
                }
                Database.RowData res = db.selectOne(id);
                if(res != null) {
                    System.out.println(" ID [" + res.mId + "] ");
                    System.out.println(" Message --> " + res.mMessage);
                    System.out.println(" Likes --> " + res.mLikes);
                }
            } else if(action == '*') {
                ArrayList<Database.RowData> res = db.selectAll();
                if (res == null){
                    continue;
                }
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res){
                    System.out.println(" ID --> [" + rd.mId + "] ");
                    System.out.println(" Message --> [" + rd.mMessage + "] ");
                    System.out.println(" Likes --> [" + rd.mLikes + "] ");
                }
            } else if(action == '-') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1){
                    continue;
                }
                int res = db.deleteRow(id);
                if(res == -1){
                    continue;
                }
                System.out.println("  " + res + " rows deleted");
            } else if(action == '+'){
                String message = getString(in, "Enter the message");
                if(message == null){
                    System.out.println("Can't make an empty message");
                    continue;
                }
                int res = db.insertRow(message, 0);
                System.out.println(res + " rows added");
            } else if(action == '~'){
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1){
                    continue;
                }
                String newMessage = getString(in, "Enter the new message");
                int res = db.updateOne(id, newMessage);
                if (res == -1){
                    continue;
                }
                System.out.println("  " + res + " rows updated");
            } else if (action == 'i'){ //If incrementing likes
                int id = getInt(in, "Enter the row ID :> ");
                if(id == -1){
                    continue;
                }
                db.incrementLikes(id);
            } else if (action == 'd'){ //Decrementing likes
                int id = getInt(in, "Enter the row ID :> ");
                if(id == -1){
                    continue;
                }
                int res = db.decrementLikes(id);
                if(res == -1){
                    continue;
                }
                System.out.println("Row has been updated");
            } else if (action == 'L') {
                int res = db.createLikesTable();
                if(res == -1){
                    continue;
                }
                System.out.println("Likes table has been created");
            } else if (action == 'M') {
                int res = db.createMsgTable();
                if(res == -1){
                    continue;
                }
                System.out.println("Message table has been created");
            } else if (action == 'C') {
                int res = db.createCommentsTable();
                if(res == -1){
                    continue;
                }
                System.out.println("Comments table has been created");
            } else if (action == 'U') {
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
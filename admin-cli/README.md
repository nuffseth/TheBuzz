# Administrative Interface

# Phase 4

New features:
Added tables for flagged messages, functionality for flagging messages and comments, blocking users, added to admin CLI, isEven API

Fixed previous technical debt:
In Database.java, fixed the way that insertUser works to check if the user already exists before trying to add it to the database. Before it checked in the SQL catch, now it checks before calling the prepared statement for inserting a user. This is a more efficient way of doing this.

Current technical debt:
Unit testing is not working because I am getting the error "Cannot make a static reference to the non-static method insertUser(String, String) from the type Database". Unit tests are outlined, just commented out because of the error. Tried to connect to database and set up JUnit, but did not have time to finish. Functionality for deleting flags and blocked users still needs to be implemented.




# Older Phases

WEB

No unit tests

FLUTTER

No unit tests

BACKEND

Unit tests show functionality with OAuth (null is returned with an incorrect id_token) and functionality with the hash table (invalid user+key combo returns false, and valid combo returns true).


PAIR PROGRAMMING

Previous admin used experience with SQL and knowledege of Database.java to guide Phase 2 admin through the proper creation of tables according to the new schema.
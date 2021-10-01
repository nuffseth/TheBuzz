# CSE 216 Group 4 Phase 2

## https://trello.com/invite/b/qx0dDNpf/ef397a1a88f32d93c6a4924f2cb10d01/phase2 

## Contributors
1. Rohan Kakkilaya (rok224) - Web  
2. Adyn Gallagher (arg422) - Backend  
3. Lindsey Seroka (lbs222) - PM  
4. Allan Lee (all418) - Flutter  
5. Sebastian Chavarro (sec223) - Admin

---------------------------------------------------------

# CSE 216 Group 4 Phase 1

## https://trello.com/b/dW4jXh1z/216-phase-1 

## Contributors
1. Rohan Kakkilaya (rok224) - Admin  
2. Adyn Gallagher (arg422) - PM  
3. Lindsey Seroka (lbs222) - Backend  
4. Allan Lee (all418) - Web  
5. Sebastian Chavarro (sec223) - Android  

## Documentation 
https://docs.google.com/document/d/17MSxgQiVPeYMNp3QzWc_86U_O2uVrKcM3t6vyfBpS-w/edit?usp=sharing 

The general API structure is bolded and can be found in the Document Outline 
Functionality that still needs to be implemented by backend developer is highlighted in purple 
Unused routes (and routes not required for phase 1) are italicized 

### /messages 
GET ‘/messages’ 
Returns a JSON object of all messages stored in the database 
Needs to be updated to show the likes of every message (might be handled by Database.java automatically?) 
POST ‘/messages’ 
Takes a JSON object (in req.body) and adds it to the database 
The message’s likes start out at 0 

### /messages/id 
PUT ‘/messages/:id’ 
Updates a message with message_id = id with the contents of req.body  
If message_id doesn’t exist, creates a new message with provided id 
DELETE ‘/messages/:id’ 
Deletes the message with message_id = id 
If the message_id does not exist, returns an error  

### /messages/:id/likes 
POST ‘/messages/:id/likes’ 
Increment the ‘likes’ variable for a message with message_id=id by 1 
If the message_id does not exist, returns an error  
If like successfully added, response code is 200 (success) 
POST ‘messages/:id/dislikes’ 
Decrements the ‘likes’ variable for a message with message_id=id by 1 
If the message_id does not exist, returns an error 
If successfully decremented, response code is 200 (success) 
 
SELECT 
   table_name, 
   column_name, 
   data_type
FROM 
   information_schema.columns

WHERE 
   table_name in ( 'likes', 'users' , 'messages', 'flagged_msgs', 'flagged_comments', 'comments', 'blocked')

ORDER BY table_name ;


SELECT c.table_name, c.column_name, c.data_type
FROM information_schema.table_constraints tc 
JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name) 
JOIN information_schema.columns AS c ON c.table_schema = tc.constraint_schema
  AND tc.table_name = c.table_name AND ccu.column_name = c.column_name
WHERE constraint_type = 'PRIMARY KEY' and tc.table_name in ( 'likes', 'users' , 'messages', 'flagged_msgs', 'flagged_comments', 'comments');

call add_new_flagged_msg(1, 'adyn2');
call add_new_flagged_msg(2, 'adyn');
call add_new_flagged_msg(2, 'adyn2');

call add_new_flagged_comments(1, 'adyn2');

SELECT * FROM flagged_msgs;
SELECT * FROM flagged_comments;
SELECT * FROM messages;
SELECT * FROM comments;

call delete_msg(1);
SELECT * FROM flagged_msgs;
SELECT * FROM flagged_comments;
SELECT * FROM messages;
SELECT * FROM comments;

call add_new_blocked_user('adyn', 'adyn2');
SELECT * FROM blocked;

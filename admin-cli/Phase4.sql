DROP TABLE flagged_msgs;
DROP TABLE flagged_comments;
DROP TABLE blocked;

CREATE TABLE flagged_msgs (
    msgid INT, 
    userid VARCHAR(500), 
    -- status INT, 
    primary key (msgid, userid),
    CONSTRAINT msg_key FOREIGN KEY(msgid) REFERENCES messages(msgid),
    CONSTRAINT user_key FOREIGN KEY(userid) REFERENCES users(userid)
);

/**/
SELECT msgID, COUNT(flagged_msgs.userid) AS flag_count FROM messages INNER JOIN flagged_msgs ON messages.msgId = flagged_msgs.msgid GROUP BY msgId ORDER BY flag_count DESC;

SELECT COUNT(flagged_msgs.userid) AS flag_count FROM messages INNER JOIN flagged_msgs ON messages.msgId = flagged_msgs.msgid GROUP BY msgId WHERE messages.msgID = ?;
/**/

CREATE TABLE flagged_comments (
    cmtid INT, 
    userid VARCHAR(500), 
    -- status INT, 
    primary key (cmtid, userid),
    CONSTRAINT comment_key FOREIGN KEY(cmtid) REFERENCES comments(cmtid),
    CONSTRAINT user_key FOREIGN KEY(userid) REFERENCES users(userid)
);

CREATE TABLE blocked (
    user_blocked VARCHAR(500),
    user_blocker VARCHAR(500),
    primary key (user_blocked, user_blocker),
    CONSTRAINT user_key_blocked FOREIGN KEY(user_blocked) REFERENCES users(userid),
    CONSTRAINT user_key_blocker FOREIGN KEY(user_blocker) REFERENCES users(userid)
);

ALTER TABLE messages DROP COLUMN flag_count ;
ALTER TABLE comments DROP COLUMN flag_count ;
ALTER TABLE messages DROP COLUMN ad ;
ALTER TABLE messages DROP COLUMN is_even ;

ALTER TABLE messages ADD COLUMN flag_count INT;
ALTER TABLE comments ADD COLUMN flag_count INT;

ALTER TABLE messages ADD COLUMN ad VARCHAR(500);
ALTER TABLE messages ADD COLUMN is_even BOOLEAN;

-- TODO add index to flag_count in the messages and comments tables
-- add new flagged messages
drop procedure add_new_flagged_msg;
create or replace procedure add_new_flagged_msg(
    _msgid INT, 
    _userid VARCHAR(500)
)
language plpgsql    
as $$
begin
    INSERT INTO flagged_msgs(msgid, userid)
    VALUES(_msgid, _userid); 
    
    UPDATE messages
    SET flag_count = flag_count + 1
    WHERE msgid = _msgid AND flag_count >0;

    UPDATE messages
    SET flag_count = 1
    WHERE msgid = _msgid AND flag_count is null;

    commit;
end;$$;

-- add new flagged comments
drop procedure add_new_flagged_comments;
create or replace procedure add_new_flagged_comments(
    _cmtid INT, 
    _userid VARCHAR(500)
)
language plpgsql    
as $$
begin
    INSERT INTO flagged_comments(cmtid, userid)
    VALUES(_cmtid, _userid); 
    
    UPDATE comments
    SET flag_count = flag_count + 1
    WHERE cmtid = _cmtid AND flag_count >0;
    
    UPDATE comments
    SET flag_count = 1
    WHERE cmtid = _cmtid AND flag_count is null;

    commit;
end;$$;

-- deleting messages
-- drop procedure delete_msg;
-- create or replace procedure delete_msg(
--     _msgid INT
-- )
-- language plpgsql    
-- as $$
-- begin
--     DELETE FROM flagged_msgs WHERE msgid = _msgid;
--     DELETE FROM cmtfiles WHERE cmtid in (select cmtid from comments where msgid=_msgid);
--     DELETE FROM comments WHERE msgid=_msgid;
--     DELETE FROM msgfiles WHERE msgid=_msgid;
--     DELETE FROM likes WHERE msgid=_msgid;
--     DELETE FROM messages WHERE msgid=_msgid;

--     commit;
-- end;$$;

-- add new blocked user
drop procedure add_new_blocked_user;
create or replace procedure add_new_blocked_user(
    _user_blocked VARCHAR(500), 
    _user_blocker VARCHAR(500)
)
language plpgsql    
as $$
begin
    INSERT INTO blocked(user_blocked, user_blocker)
    VALUES(_user_blocked, _user_blocker); 

    commit;
end;$$;

-- delete blocked user
-- add blocked to cli
-- isEven api
-- add new collum in messages that has the ad string, column for boolean, change Message fields
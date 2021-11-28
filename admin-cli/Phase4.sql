DROP TABLE flagged_msgs;
DROP TABLE flagged_comments;

CREATE TABLE flagged_msgs (
    msgid INT, 
    userid VARCHAR(500), 
    -- status INT, 
    primary key (msgid, userid),
    CONSTRAINT msg_key FOREIGN KEY(msgid) REFERENCES messages(msgid),
    CONSTRAINT user_key FOREIGN KEY(userid) REFERENCES users(userid)
);

CREATE TABLE flagged_comments (
    cmtid INT, 
    userid VARCHAR(500), 
    -- status INT, 
    primary key (cmtid, userid),
    CONSTRAINT comment_key FOREIGN KEY(cmtid) REFERENCES comments(cmtid),
    CONSTRAINT user_key FOREIGN KEY(userid) REFERENCES users(userid)
);

ALTER TABLE messages DROP COLUMN flagcount ;
ALTER TABLE comments DROP COLUMN flagcount ;
ALTER TABLE messages DROP COLUMN flag_count ;
ALTER TABLE comments DROP COLUMN flag_count ;

ALTER TABLE messages ADD COLUMN flag_count INT;
ALTER TABLE comments ADD COLUMN flag_count INT;

-- TODO add index to flag_count in the messages and comments tables
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
end;$$
;

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
end;$$
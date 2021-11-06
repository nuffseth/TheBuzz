-- commands run to add  links to message and comment tables 

ALTER TABLE messages ADD COLUMN msgLink INT
ALTER TABLE messages ADD COLUMN cmtLink INT
ALTER TABLE messages ADD CONSTRAINT msg_link_key FOREIGN KEY(msgLink) REFERENCES messages(msgID)
ALTER TABLE messages ADD CONSTRAINT cmt_link_key FOREIGN KEY(cmtLink) REFERENCES comments(cmtID)

ALTER TABLE comments ADD COLUMN msgLink INT
ALTER TABLE comments ADD COLUMN cmtLink INT
ALTER TABLE comments ADD CONSTRAINT msg_link_key FOREIGN KEY(msgLink) REFERENCES messages(msgID)
ALTER TABLE comments ADD CONSTRAINT cmt_link_key FOREIGN KEY(cmtLink) REFERENCES comments(cmtID)

-- commands run to create two new tables: one for message files, and one for comment files

CREATE TABLE msgfiles (fileID TEXT, msgID INT, mime TEXT, filename VARCHAR(500), CONSTRAINT msg_key FOREIGN KEY(msgID) REFERENCES messages(msgID))
CREATE TABLE cmtfiles (fileID TEXT, cmtID INT, mime TEXT, filename VARCHAR(500), CONSTRAINT cmt_key FOREIGN KEY(cmtID) REFERENCES comments(cmtID) )
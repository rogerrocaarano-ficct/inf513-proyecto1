-- Attachments keep only a reference to their stored file; the bytes live in file storage (issue #9).
-- There is no data to keep: the attachments recorded so far are dropped.
DELETE FROM received_email_attachment;

ALTER TABLE received_email_attachment DROP COLUMN data;
ALTER TABLE received_email_attachment DROP COLUMN name;
ALTER TABLE received_email_attachment
    ADD COLUMN stored_file_id UUID NOT NULL UNIQUE REFERENCES stored_file (id);

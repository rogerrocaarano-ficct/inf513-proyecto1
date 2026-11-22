-- Initial documents (UC-002) and project profiles (UC-003) keep only a reference to their stored file;
-- the PDF lives in file storage and its name in stored_file.original_name (issue #9).
-- There is no data to keep: procedures, documents and projects recorded so far are dropped.
DELETE FROM degree_project;
DELETE FROM initial_document;
DELETE FROM degree_procedure;

ALTER TABLE initial_document DROP COLUMN content;
ALTER TABLE initial_document DROP COLUMN filename;
ALTER TABLE initial_document
    ADD COLUMN stored_file_id UUID NOT NULL UNIQUE REFERENCES stored_file (id);

ALTER TABLE degree_project DROP COLUMN profile_content;
ALTER TABLE degree_project DROP COLUMN profile_filename;
ALTER TABLE degree_project
    ADD COLUMN profile_stored_file_id UUID NOT NULL UNIQUE REFERENCES stored_file (id);

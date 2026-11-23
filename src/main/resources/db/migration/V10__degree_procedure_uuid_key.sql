-- UC-002: the UUID becomes the key of a graduation procedure, and its reference becomes
-- <semester>-<registration number> (for example 2026-2-218001234), built by the domain when it opens.
-- Existing rows get the semester of their opening date (January to June is period 1).

ALTER TABLE degree_procedure ADD COLUMN code VARCHAR(20);
UPDATE degree_procedure p
   SET code = to_char(p.opened_at, 'YYYY') || '-'
           || CASE WHEN extract(MONTH FROM p.opened_at) <= 6 THEN '1' ELSE '2' END || '-'
           || u.uagrm_code
  FROM user_account u
 WHERE u.id = p.student_id;

-- Initial documents point to the procedure by its UUID. Dropping the old column also drops its
-- foreign key and the unique (procedure_reference, document_type).
ALTER TABLE initial_document ADD COLUMN procedure_id UUID;
UPDATE initial_document d
   SET procedure_id = p.id
  FROM degree_procedure p
 WHERE p.reference = d.procedure_reference;
ALTER TABLE initial_document ALTER COLUMN procedure_id SET NOT NULL;
ALTER TABLE initial_document DROP COLUMN procedure_reference;

-- The UUID replaces the generated number as the primary key.
ALTER TABLE degree_project DROP CONSTRAINT degree_project_procedure_id_fkey;
ALTER TABLE degree_procedure DROP CONSTRAINT degree_procedure_pkey;
ALTER TABLE degree_procedure DROP CONSTRAINT degree_procedure_id_key;
ALTER TABLE degree_procedure DROP COLUMN reference;
ALTER TABLE degree_procedure ADD CONSTRAINT degree_procedure_pkey PRIMARY KEY (id);

ALTER TABLE degree_procedure RENAME COLUMN code TO reference;
ALTER TABLE degree_procedure ALTER COLUMN reference SET NOT NULL;
ALTER TABLE degree_procedure ADD CONSTRAINT degree_procedure_reference_key UNIQUE (reference);

ALTER TABLE initial_document
    ADD CONSTRAINT initial_document_procedure_id_fkey FOREIGN KEY (procedure_id) REFERENCES degree_procedure (id);
ALTER TABLE initial_document
    ADD CONSTRAINT initial_document_procedure_id_document_type_key UNIQUE (procedure_id, document_type);
ALTER TABLE degree_project
    ADD CONSTRAINT degree_project_procedure_id_fkey FOREIGN KEY (procedure_id) REFERENCES degree_procedure (id);

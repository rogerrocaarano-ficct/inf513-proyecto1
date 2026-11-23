-- UC-002: the documents of a procedure are its opening requirements, values of the procedure with no
-- identity of their own. Who uploaded each one and when is left to the file storage.

CREATE TABLE degree_procedure_requirement (
    procedure_id   UUID        NOT NULL REFERENCES degree_procedure (id),
    requirement    VARCHAR(10) NOT NULL CHECK (requirement IN ('LETTER', 'RESUME', 'IDENTITY', 'ENROLLMENT')),
    stored_file_id UUID        NOT NULL UNIQUE REFERENCES stored_file (id),
    PRIMARY KEY (procedure_id, requirement)
);

INSERT INTO degree_procedure_requirement (procedure_id, requirement, stored_file_id)
SELECT procedure_id, document_type, stored_file_id
  FROM initial_document;

DROP TABLE initial_document;

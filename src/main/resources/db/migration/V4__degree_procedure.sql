CREATE TABLE degree_procedure (
    reference BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES user_account(id),
    opened_by UUID NOT NULL REFERENCES user_account(id),
    opened_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX degree_procedure_one_open_per_student
    ON degree_procedure (student_id) WHERE closed_at IS NULL;

CREATE TABLE initial_document (
    procedure_reference BIGINT NOT NULL REFERENCES degree_procedure(reference),
    document_type VARCHAR(10) NOT NULL CHECK (document_type IN ('LETTER', 'RESUME', 'IDENTITY', 'ENROLLMENT')),
    filename TEXT NOT NULL,
    content BYTEA NOT NULL,
    updated_by UUID NOT NULL REFERENCES user_account(id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (procedure_reference, document_type)
);

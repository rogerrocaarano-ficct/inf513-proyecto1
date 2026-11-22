CREATE TABLE degree_project (
    id UUID PRIMARY KEY,
    procedure_id UUID NOT NULL UNIQUE REFERENCES degree_procedure(id),
    title TEXT NOT NULL,
    profile_filename TEXT NOT NULL,
    profile_content BYTEA NOT NULL,
    profile_approved BOOLEAN NOT NULL DEFAULT FALSE,
    tutor_id UUID REFERENCES user_account(id),
    CHECK (tutor_id IS NULL OR profile_approved)
);

CREATE TABLE graduation_processes (
    id                          UUID        NOT NULL,
    student_account_id          UUID        NOT NULL,
    status                      VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    stage                       VARCHAR(30) NOT NULL DEFAULT 'OPENED',
    opened_at                   TIMESTAMPTZ NOT NULL,
    closed_at                   TIMESTAMPTZ,
    physical_copies_received_at TIMESTAMPTZ,
    pending_reassignment        BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT graduation_processes_pk PRIMARY KEY (id),
    CONSTRAINT graduation_processes_status_ck CHECK (status IN ('IN_PROGRESS', 'CLOSED')),
    CONSTRAINT graduation_processes_closed_ck CHECK (status = 'IN_PROGRESS'
        OR (closed_at IS NOT NULL AND physical_copies_received_at IS NOT NULL))
);

-- One graduation process in progress per student (UC-003 E4, FR-056).
CREATE UNIQUE INDEX graduation_processes_student_in_progress_uk
    ON graduation_processes (student_account_id) WHERE status = 'IN_PROGRESS';

CREATE TABLE graduation_process_participants (
    graduation_process_id UUID        NOT NULL,
    user_account_id       UUID        NOT NULL,
    kind                  VARCHAR(20) NOT NULL,
    CONSTRAINT graduation_process_participants_pk PRIMARY KEY (graduation_process_id, user_account_id, kind),
    CONSTRAINT graduation_process_participants_process_fk FOREIGN KEY (graduation_process_id)
        REFERENCES graduation_processes (id) ON DELETE CASCADE,
    CONSTRAINT graduation_process_participants_kind_ck CHECK (kind IN ('TUTOR', 'TRIBUNAL_MEMBER'))
);

CREATE INDEX graduation_process_participants_user_idx ON graduation_process_participants (user_account_id, kind);

CREATE TABLE initial_documents (
    id                    UUID         NOT NULL,
    graduation_process_id UUID         NOT NULL,
    type                  VARCHAR(20)  NOT NULL,
    file_name             VARCHAR(255) NOT NULL,
    content_type          VARCHAR(100) NOT NULL,
    size_bytes            INTEGER      NOT NULL,
    storage_key           VARCHAR(500) NOT NULL,
    uploaded_at           TIMESTAMPTZ  NOT NULL,
    CONSTRAINT initial_documents_pk PRIMARY KEY (id),
    CONSTRAINT initial_documents_process_fk FOREIGN KEY (graduation_process_id)
        REFERENCES graduation_processes (id) ON DELETE CASCADE,
    CONSTRAINT initial_documents_type_ck CHECK (type IN ('REQUEST_LETTER', 'CURRICULUM_VITAE', 'ID_CARD_COPY',
        'ENROLLMENT_SLIP')),
    CONSTRAINT initial_documents_content_type_ck CHECK (content_type IN ('image/png', 'image/jpeg', 'application/pdf')),
    CONSTRAINT initial_documents_size_ck CHECK (size_bytes BETWEEN 1 AND 1048576)
);

CREATE UNIQUE INDEX initial_documents_process_type_uk ON initial_documents (graduation_process_id, type);
CREATE UNIQUE INDEX initial_documents_storage_key_uk ON initial_documents (storage_key);

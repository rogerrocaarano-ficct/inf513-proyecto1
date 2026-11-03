-- The graduation project of a process, with its current document (UC-004). Earlier profiles and
-- versions are in change_log_entries; the files are in the file system (AD-012).
CREATE TABLE graduation_projects (
    id                    UUID         NOT NULL,
    graduation_process_id UUID         NOT NULL,
    title                 VARCHAR(300) NOT NULL,
    document_kind         VARCHAR(30)  NOT NULL,
    file_name             VARCHAR(255) NOT NULL,
    size_bytes            INTEGER      NOT NULL,
    storage_key           VARCHAR(500) NOT NULL,
    version_number        INTEGER,
    general_comment       TEXT,
    submitted_at          TIMESTAMPTZ  NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'PROFILE_PENDING',
    version               BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT graduation_projects_pk PRIMARY KEY (id),
    CONSTRAINT graduation_projects_process_fk FOREIGN KEY (graduation_process_id)
        REFERENCES graduation_processes (id) ON DELETE CASCADE,
    CONSTRAINT graduation_projects_kind_ck CHECK (document_kind IN ('PROJECT_PROFILE', 'PROJECT_DOCUMENT',
        'FINAL_DEFENSE_VERSION')),
    CONSTRAINT graduation_projects_status_ck CHECK (status IN ('PROFILE_PENDING', 'PROFILE_OBSERVED',
        'PROFILE_REJECTED', 'PROFILE_APPROVED', 'TUTOR_REVIEW', 'TUTOR_OBSERVED', 'TRIBUNAL_REVIEW',
        'TRIBUNAL_OBSERVED', 'APPROVED')),
    -- A profile has no version number, and a version always has one.
    CONSTRAINT graduation_projects_version_number_ck CHECK ((document_kind = 'PROJECT_PROFILE') = (version_number IS NULL)),
    CONSTRAINT graduation_projects_comment_ck CHECK (document_kind = 'PROJECT_PROFILE' OR general_comment IS NOT NULL),
    -- Profile statuses only for a profile, and review statuses only for a version.
    CONSTRAINT graduation_projects_status_kind_ck CHECK ((document_kind = 'PROJECT_PROFILE') = (status LIKE 'PROFILE\_%')),
    CONSTRAINT graduation_projects_size_ck CHECK (size_bytes BETWEEN 1 AND 26214400)
);

-- One graduation project per process, looked up by its process (UC-004).
CREATE UNIQUE INDEX graduation_projects_process_uk ON graduation_projects (graduation_process_id);
CREATE UNIQUE INDEX graduation_projects_storage_key_uk ON graduation_projects (storage_key);

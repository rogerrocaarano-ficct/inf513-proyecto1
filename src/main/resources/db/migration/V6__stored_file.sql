-- Metadata of the files kept on disk (issue #9). The bytes are never stored in the database.
CREATE TABLE stored_file (
    id               UUID         PRIMARY KEY,
    path             TEXT         NOT NULL UNIQUE,
    original_name    TEXT         NOT NULL,
    content_type     VARCHAR(255) NOT NULL,
    size             BIGINT       NOT NULL CHECK (size > 0),
    stored_at        TIMESTAMPTZ  NOT NULL,
    first_version_id UUID         NOT NULL REFERENCES stored_file (id),
    -- A version has at most one next version, so a chain never forks.
    next_version_id  UUID         UNIQUE REFERENCES stored_file (id)
);

CREATE INDEX stored_file_first_version_idx ON stored_file (first_version_id);

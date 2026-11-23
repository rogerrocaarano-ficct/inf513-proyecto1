-- UC-004: a degree project keeps every version of its profile with its evaluation. A version is a
-- value of its project, numbered from 1. Its PDF is an ordinary stored file: the versions are kept
-- here, not chained in the file storage.

CREATE TABLE degree_project_profile_version (
    project_id     UUID        NOT NULL REFERENCES degree_project (id),
    version        INTEGER     NOT NULL CHECK (version >= 1),
    stored_file_id UUID        NOT NULL UNIQUE REFERENCES stored_file (id),
    title          TEXT        NOT NULL,
    status         VARCHAR(10) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'OBSERVED', 'REJECTED')),
    observations   TEXT,
    evaluated_on   TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (project_id, version),
    CHECK ((status = 'PENDING') = (evaluated_on IS NULL)),
    CHECK (status NOT IN ('OBSERVED', 'REJECTED') OR observations IS NOT NULL)
);

-- The profile recorded so far becomes version 1. An approval recorded before UC-004 has no date of
-- its own, so it takes the time of this migration.
INSERT INTO degree_project_profile_version (project_id, version, stored_file_id, title, status, evaluated_on)
SELECT id, 1, profile_stored_file_id, title,
       CASE WHEN profile_approved THEN 'APPROVED' ELSE 'PENDING' END,
       CASE WHEN profile_approved THEN now() END
  FROM degree_project;

-- Dropping profile_approved also drops the CHECK of V5 that used it: the rule "tutor only with an
-- approved profile" stays in the domain, because it now spans two tables.
ALTER TABLE degree_project DROP COLUMN profile_stored_file_id;
ALTER TABLE degree_project DROP COLUMN profile_approved;

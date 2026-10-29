CREATE TABLE user_accounts (
    id                          UUID         NOT NULL,
    full_name                   VARCHAR(150) NOT NULL,
    email                       VARCHAR(254) NOT NULL,
    student_registration_number VARCHAR(20),
    id_card                     VARCHAR(20),
    phone                       VARCHAR(20),
    profile_photo               BYTEA,
    status                      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT user_accounts_pk PRIMARY KEY (id),
    CONSTRAINT user_accounts_status_ck CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT user_accounts_email_lower_ck CHECK (email = lower(email))
);

CREATE UNIQUE INDEX user_accounts_email_uk ON user_accounts (email);
CREATE UNIQUE INDEX user_accounts_registration_uk ON user_accounts (student_registration_number);

CREATE TABLE user_account_roles (
    user_account_id UUID        NOT NULL,
    role            VARCHAR(30) NOT NULL,
    CONSTRAINT user_account_roles_pk PRIMARY KEY (user_account_id, role),
    CONSTRAINT user_account_roles_user_account_fk FOREIGN KEY (user_account_id)
        REFERENCES user_accounts (id) ON DELETE CASCADE,
    CONSTRAINT user_account_roles_role_ck CHECK (role IN ('STUDENT', 'PROFESSOR', 'THESIS_WORKSHOP_PROFESSOR',
        'PROGRAM_SECRETARY', 'PROGRAM_DIRECTOR', 'ADMINISTRATOR'))
);

CREATE INDEX user_account_roles_role_idx ON user_account_roles (role);

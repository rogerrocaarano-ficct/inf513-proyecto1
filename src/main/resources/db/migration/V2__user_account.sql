-- User accounts (UC-001). "user" is a reserved word in PostgreSQL.
CREATE TABLE user_account (
    id               UUID PRIMARY KEY,
    email_address    VARCHAR(320) NOT NULL UNIQUE,
    role             VARCHAR(30)  NOT NULL,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    second_last_name VARCHAR(100),
    ci               VARCHAR(7),
    uagrm_code       VARCHAR(10) UNIQUE,
    phone_number     VARCHAR(8),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

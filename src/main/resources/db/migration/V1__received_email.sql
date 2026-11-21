CREATE TABLE received_email (
    id           UUID PRIMARY KEY,
    mailbox_id   VARCHAR(255) NOT NULL UNIQUE,
    from_address VARCHAR(320),
    subject      TEXT,
    body         TEXT,
    state        VARCHAR(20)  NOT NULL,
    received_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX received_email_pending_idx ON received_email (state, received_at);

CREATE TABLE received_email_attachment (
    id                UUID PRIMARY KEY,
    received_email_id UUID NOT NULL REFERENCES received_email (id) ON DELETE CASCADE,
    position          INTEGER NOT NULL,
    name              VARCHAR(255),
    data              BYTEA   NOT NULL
);

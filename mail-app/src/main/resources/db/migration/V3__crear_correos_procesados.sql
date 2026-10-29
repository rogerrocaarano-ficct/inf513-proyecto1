CREATE TABLE processed_mail_messages (
    id           UUID          NOT NULL,
    message_id   VARCHAR(998)  NOT NULL,
    processed_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT processed_mail_messages_pk PRIMARY KEY (id)
);

CREATE UNIQUE INDEX processed_mail_messages_message_id_uk ON processed_mail_messages (message_id);

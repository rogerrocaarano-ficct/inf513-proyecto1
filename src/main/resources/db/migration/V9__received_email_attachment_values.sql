-- An attachment is a value of its email, with no identity of its own: it is identified by its
-- position in the email. The rows are kept; dropping the id column also drops its primary key.
ALTER TABLE received_email_attachment DROP COLUMN id;
ALTER TABLE received_email_attachment ADD PRIMARY KEY (received_email_id, position);

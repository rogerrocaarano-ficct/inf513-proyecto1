CREATE TABLE change_log_entries (
    id          UUID        NOT NULL,
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    actor_id    UUID        NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id   UUID        NOT NULL,
    field       VARCHAR(50) NOT NULL,
    old_value   TEXT,
    new_value   TEXT,
    CONSTRAINT change_log_entries_pk PRIMARY KEY (id)
);

CREATE INDEX change_log_entries_target_idx ON change_log_entries (target_type, target_id, changed_at);
CREATE INDEX change_log_entries_actor_idx ON change_log_entries (actor_id, changed_at);

-- The change log is append-only. The application uses a single database user,
-- so a trigger rejects any update or delete.
CREATE FUNCTION change_log_entries_reject_change() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'change_log_entries is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER change_log_entries_append_only
    BEFORE UPDATE OR DELETE ON change_log_entries
    FOR EACH ROW EXECUTE FUNCTION change_log_entries_reject_change();

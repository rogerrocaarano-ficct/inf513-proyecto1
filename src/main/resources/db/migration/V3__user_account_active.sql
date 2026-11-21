-- Account status (UC-001 A2, FR-032). Existing accounts stay active.
ALTER TABLE user_account ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

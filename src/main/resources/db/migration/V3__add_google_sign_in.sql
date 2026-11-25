ALTER TABLE users ADD COLUMN google_subject varchar(255) UNIQUE;

CREATE TABLE google_sign_in_codes (
    code_hash char(43) PRIMARY KEY,
    browser_challenge char(43) NOT NULL,
    user_id bigint NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    expires_at timestamptz NOT NULL
);

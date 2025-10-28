CREATE TABLE links (
    id BIGSERIAL PRIMARY KEY,
    short_key VARCHAR(12) UNIQUE NOT NULL,
    original_url VARCHAR(128) UNIQUE NOT NULL,
    click_count INT DEFAULT 0
);
CREATE INDEX idx_short_key ON links(short_key);
CREATE INDEX idx_original_url ON links(original_url);
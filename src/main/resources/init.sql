CREATE TABLE IF NOT EXISTS url_mapping_0 (
    id          BIGINT       NOT NULL PRIMARY KEY,
    short_code  VARCHAR(16)  NOT NULL,
    long_url    TEXT         NOT NULL,
    created_at  DATETIME     NOT NULL,
    access_count BIGINT      NOT NULL DEFAULT 0,
    INDEX idx_short_code (short_code)
);

CREATE TABLE IF NOT EXISTS url_mapping_1 LIKE url_mapping_0;
CREATE TABLE IF NOT EXISTS url_mapping_2 LIKE url_mapping_0;
CREATE TABLE IF NOT EXISTS url_mapping_3 LIKE url_mapping_0;
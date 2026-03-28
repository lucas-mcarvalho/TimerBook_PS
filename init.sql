-- table: books
CREATE TABLE IF NOT EXISTS books (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description TEXT,
    cover_url   VARCHAR(500),
    data_path   VARCHAR(500)
);

-- table: reading
CREATE TABLE IF NOT EXISTS reading (
    id           BIGSERIAL PRIMARY KEY,
    book_id      BIGINT NOT NULL,
    current_page INT NOT NULL DEFAULT 0,
    started_at   TIMESTAMP NOT NULL,
    finished_at  TIMESTAMP NULL,
    CONSTRAINT fk_reading_book 
        FOREIGN KEY (book_id) 
        REFERENCES books(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_reading_dates 
        CHECK (finished_at IS NULL OR finished_at >= started_at)
);

-- table: reading_session
CREATE TABLE IF NOT EXISTS reading_session (
    id           BIGSERIAL PRIMARY KEY,
    reading_id   BIGINT NOT NULL,
    start_page   INT NOT NULL,
    end_page     INT NOT NULL,
    started_at   TIMESTAMP NOT NULL,
    ended_at     TIMESTAMP NULL,
    CONSTRAINT fk_reading_session_reading 
        FOREIGN KEY (reading_id) 
        REFERENCES reading(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_pages 
        CHECK (end_page >= start_page),
    CONSTRAINT chk_session_dates 
        CHECK (ended_at IS NULL OR ended_at >= started_at)
);
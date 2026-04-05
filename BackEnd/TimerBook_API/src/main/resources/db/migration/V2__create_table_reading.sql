CREATE TABLE reading (
    id           BIGSERIAL PRIMARY KEY,
    book_id      BIGINT NOT NULL,
    current_page INT NOT NULL DEFAULT 0,
    started_at   TIMESTAMP NOT NULL,
    finished_at  TIMESTAMP NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reading_book
        FOREIGN KEY (book_id)
        REFERENCES books(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_reading_dates
        CHECK (finished_at IS NULL OR finished_at >= started_at)
);
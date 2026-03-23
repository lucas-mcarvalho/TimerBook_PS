CREATE DATABASE IF NOT EXISTS timerbook;
USE timerbook;

CREATE TABLE IF NOT EXISTS books (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255),
    description TEXT,
    cover_url   VARCHAR(500),
    data_path   VARCHAR(500)
);

-- table: reading
CREATE TABLE IF NOT EXISTS reading (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id     BIGINT NOT NULL,
    current_page INT NOT NULL DEFAULT 0,
    started_at  DATETIME NOT NULL,
    finished_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_reading_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CHECK (finished_at IS NULL OR finished_at >= started_at)
);

-- table: reading_session
CREATE TABLE IF NOT EXISTS reading_session (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    reading_id  BIGINT NOT NULL,
    start_page  INT NOT NULL,
    end_page    INT NOT NULL,
    started_at  DATETIME NOT NULL,
    ended_at    DATETIME DEFAULT NULL,
    CONSTRAINT fk_reading_session_reading FOREIGN KEY (reading_id) REFERENCES reading(id) ON DELETE CASCADE,
    CHECK (end_page >= start_page),
    CHECK (ended_at IS NULL OR ended_at >= started_at)
);

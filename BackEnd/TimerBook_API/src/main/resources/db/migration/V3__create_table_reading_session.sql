CREATE TABLE reading_session (
                                 id           BIGSERIAL PRIMARY KEY,
                                 reading_id   BIGINT NOT NULL,
                                 start_page   INT NOT NULL,
                                 end_page     INT NOT NULL,
                                 started_at   TIMESTAMP NOT NULL,
                                 ended_at     TIMESTAMP NULL,
                                 created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_reading_session_reading
                                     FOREIGN KEY (reading_id)
                                         REFERENCES reading(id)
                                         ON DELETE CASCADE,
                                 CONSTRAINT chk_pages
                                     CHECK (end_page >= start_page),
                                 CONSTRAINT chk_session_dates
                                     CHECK (ended_at IS NULL OR ended_at >= started_at)
);